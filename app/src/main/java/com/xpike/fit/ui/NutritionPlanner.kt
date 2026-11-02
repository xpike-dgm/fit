package com.xpike.fit.ui

import java.time.LocalDate
import kotlin.math.sqrt
import kotlin.math.roundToInt

internal data class DailyTargets(
    val calories: Int,
    val protein: Int,
    val fat: Int,
    val calorieAdjustment: Int = 0,
    val status: String? = null,
    val gainPlanStopped: Boolean = false,
) {
    val carbs: Double
        get() = (calories - (4 * protein) - (9 * fat)) / 4.0
}

internal data class ReviewResult(
    val message: String,
    val updated: Boolean,
)

internal fun planDayFor(date: LocalDate, startDate: LocalDate): Int =
    (java.time.temporal.ChronoUnit.DAYS.between(startDate, date).toInt() + 1).coerceAtLeast(1)

internal fun baseTargetsForPlanDay(planDay: Int, store: FitLocalStore): DailyTargets {
    val saved = store.loadTargetChanges().lastOrNull { it.effectivePlanDay <= planDay }
    return DailyTargets(
        calories = saved?.calories ?: 3000,
        protein = saved?.protein ?: 120,
        fat = saved?.fat ?: 90,
    )
}

/**
 * Applies only a same-calendar-week reduction for completed, recorded calorie overages.
 * Missing days are never treated as zero intake and deficits never raise a later target.
 */
internal fun dailyTargetsFor(date: LocalDate, planStart: LocalDate, store: FitLocalStore): DailyTargets {
    val currentPlanDay = planDayFor(date, planStart)
    val currentBase = baseTargetsForPlanDay(currentPlanDay, store)
    val weekStart = date.minusDays((date.dayOfWeek.value - 1).toLong())
    var cursor = maxOf(weekStart, planStart)
    var balance = 0

    while (cursor.isBefore(date)) {
        val key = cursor.toString()
        if (!store.loadDayComplete(key)) {
            return currentBase.copy(status = "Önceki gün kaydı eksik · hedef korunuyor")
        }
        val historicalBase = baseTargetsForPlanDay(planDayFor(cursor, planStart), store)
        val actualCalories = store.loadMeals(key).sumOf { it.calories }
        balance = (balance + actualCalories - historicalBase.calories).coerceAtLeast(0)
        cursor = cursor.plusDays(1)
    }

    if (balance == 0) return currentBase
    val daysRemainingIncludingToday = 8 - date.dayOfWeek.value
    val reduction = minOf(100, balance / daysRemainingIncludingToday.coerceAtLeast(1))
    if (reduction <= 0) return currentBase
    return currentBase.copy(
        calories = currentBase.calories - reduction,
        calorieAdjustment = -reduction,
        status = "Bu haftaki fazla alım için günlük −$reduction kcal",
    )
}

/** Runs a conditional target review at the beginning of the period after the review day. */
internal fun evaluateTargetReview(
    currentPlanDay: Int,
    planStart: LocalDate,
    weights: List<WeightEntry>,
    store: FitLocalStore,
): ReviewResult? {
    if (store.loadGoalThresholdDate() != null) {
        return ReviewResult("73,0 kg eşiği daha önce görüldü · yeni kalori fazlası otomatik eklenmiyor.", false)
    }
    val reviewDay = when (currentPlanDay) {
        22, 36, 50, 64, 78 -> currentPlanDay - 1
        else -> return null
    }
    if (store.loadTargetChanges().any { it.effectivePlanDay == currentPlanDay }) return null
    if (reviewDay == 21) {
        return ReviewResult("Kreatin başlangıç tarihi bilinmediği için ilk kalori değerlendirmesi 35. güne ertelendi.", false)
    }

    val currentWeek = averageForPlanDays(weights, planStart, reviewDay - 6, reviewDay)
        ?: return ReviewResult("Kalori hedefi korunuyor · karşılaştırma haftasında en az 5 tartı günü gerekli.", false)
    if (currentWeek >= 73.0) {
        return ReviewResult("7 günlük ortalama 73,0 kg eşiğini gördü · kalori artırma dönemi durduruldu.", false)
    }
    val previousWeek = averageForPlanDays(weights, planStart, reviewDay - 20, reviewDay - 14)
        ?: return ReviewResult("Kalori hedefi korunuyor · önceki karşılaştırma haftasında en az 5 tartı günü gerekli.", false)

    val recentDays = (reviewDay - 13..reviewDay).map { planStart.plusDays((it - 1).toLong()) }
    if (recentDays.any { store.loadWeightDisturbance(it.toString()) }) {
        return ReviewResult("Kalori hedefi korunuyor · yakın dönemde tartıyı etkileyen durum işaretlenmiş.", false)
    }
    val completedDays = recentDays.filter { store.loadDayComplete(it.toString()) }
    if (completedDays.size < 12) {
        return ReviewResult("Kalori hedefi korunuyor · son 14 gün içinde en az 12 tam kayıt gerekli.", false)
    }

    val intakeAverage = completedDays.map { day -> store.loadMeals(day.toString()).sumOf { it.calories }.toDouble() }.average()
    val targetAverage = completedDays.map { day -> dailyTargetsFor(day, planStart, store).calories.toDouble() }.average()
    if (targetAverage <= 0 || intakeAverage !in (targetAverage * 0.95)..(targetAverage * 1.05)) {
        return ReviewResult("Kalori hedefi korunuyor · kayıtlı ortalama hedefin ±%5 aralığında değil.", false)
    }

    val weeklyRatePercent = 100.0 * (sqrt(currentWeek / previousWeek) - 1.0)
    val active = baseTargetsForPlanDay(currentPlanDay - 1, store)
    val calorieDelta = when {
        weeklyRatePercent < 0.25 -> 100
        weeklyRatePercent > 0.50 -> -100
        else -> 0
    }
    val energy = active.calories + calorieDelta
    val protein = roundToFive(2.0 * currentWeek).coerceAtLeast(0)
    val fat = roundToFive(0.27 * energy / 9.0).coerceAtLeast(0)
    store.saveTargetChange(
        TargetChange(
            effectivePlanDay = currentPlanDay,
            calories = energy,
            protein = protein,
            fat = fat,
        ),
    )
    val changeText = when (calorieDelta) {
        100 -> "+100 kcal"
        -100 -> "−100 kcal"
        else -> "kalori değişikliği yok"
    }
    return ReviewResult("2 haftalık değerlendirme: $changeText · haftalık eğilim %${"%.2f".format(java.util.Locale.US, weeklyRatePercent)}.", true)
}

internal fun sevenDayWeightAverage(weights: List<WeightEntry>, from: LocalDate, to: LocalDate): Double? =
    averageForDates(weights, from, to)

private fun averageForPlanDays(
    weights: List<WeightEntry>,
    startDate: LocalDate,
    firstPlanDay: Int,
    lastPlanDay: Int,
): Double? {
    val from = startDate.plusDays((firstPlanDay - 1).toLong())
    val to = startDate.plusDays((lastPlanDay - 1).toLong())
    return averageForDates(weights, from, to)
}

private fun averageForDates(weights: List<WeightEntry>, from: LocalDate, to: LocalDate): Double? {
    val byDay = weights.mapNotNull { entry ->
        val date = runCatching { LocalDate.parse(entry.day) }.getOrNull() ?: return@mapNotNull null
        if (date.isBefore(from) || date.isAfter(to)) return@mapNotNull null
        date to entry.weight.toDouble()
    }.groupBy({ it.first }, { it.second })
        .mapValues { (_, values) -> values.average() }
    if (byDay.size < 5) return null
    return byDay.values.average()
}

private fun roundToFive(value: Double): Int = (value / 5.0).roundToInt() * 5
