package com.xpike.fit.ui

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate

internal data class MealEntry(
    val id: Long,
    val type: String,
    val calories: Int,
    val protein: Int,
    val carbs: Double,
    val fat: Int,
)

internal data class WeightEntry(
    val day: String,
    val weight: Float,
    val id: Long = System.currentTimeMillis(),
)

internal data class TargetChange(
    val effectivePlanDay: Int,
    val calories: Int,
    val protein: Int,
    val fat: Int,
)

internal class FitLocalStore(context: Context) {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences("fit_local_data", Context.MODE_PRIVATE)

    fun loadDarkTheme(): Boolean = preferences.getBoolean("dark_theme", false)

    fun saveDarkTheme(enabled: Boolean) {
        preferences.edit().putBoolean("dark_theme", enabled).apply()
    }

    fun loadWater(day: String): Int = preferences.getInt("water_$day", 0)

    fun saveWater(day: String, millilitres: Int) {
        preferences.edit().putInt("water_$day", millilitres.coerceAtLeast(0)).apply()
    }

    fun loadWorkout(day: String): Boolean = preferences.getBoolean("workout_$day", false)

    fun saveWorkout(day: String, completed: Boolean) {
        preferences.edit().putBoolean("workout_$day", completed).apply()
    }

    fun loadDayComplete(day: String): Boolean = preferences.getBoolean("day_complete_$day", false)

    fun saveDayComplete(day: String, completed: Boolean) {
        preferences.edit().putBoolean("day_complete_$day", completed).apply()
    }

    fun loadWeightDisturbance(day: String): Boolean = preferences.getBoolean("weight_disturbance_$day", false)

    fun saveWeightDisturbance(day: String, present: Boolean) {
        preferences.edit().putBoolean("weight_disturbance_$day", present).apply()
    }

    fun loadOrCreatePlanStartDate(today: LocalDate): LocalDate {
        val saved = preferences.getString("plan_start_date", null)?.let {
            runCatching { LocalDate.parse(it) }.getOrNull()
        }
        if (saved != null) return saved
        preferences.edit().putString("plan_start_date", today.toString()).apply()
        return today
    }

    fun savePlanStartDate(date: LocalDate) {
        preferences.edit().putString("plan_start_date", date.toString()).apply()
    }

    fun loadTargetChanges(): List<TargetChange> {
        val raw = preferences.getString("target_changes", null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                TargetChange(
                    effectivePlanDay = item.optInt("effectivePlanDay", 0),
                    calories = item.optInt("calories", 3000),
                    protein = item.optInt("protein", 120),
                    fat = item.optInt("fat", 90),
                )
            }.sortedBy { it.effectivePlanDay }
        }.getOrDefault(emptyList())
    }

    fun saveTargetChange(change: TargetChange) {
        val changes = loadTargetChanges().filterNot { it.effectivePlanDay == change.effectivePlanDay } + change
        val array = JSONArray()
        changes.sortedBy { it.effectivePlanDay }.forEach { item ->
            array.put(
                JSONObject()
                    .put("effectivePlanDay", item.effectivePlanDay)
                    .put("calories", item.calories)
                    .put("protein", item.protein)
                    .put("fat", item.fat),
            )
        }
        preferences.edit().putString("target_changes", array.toString()).apply()
    }

    fun loadGoalThresholdDate(): LocalDate? = preferences.getString("goal_threshold_date", null)?.let {
        runCatching { LocalDate.parse(it) }.getOrNull()
    }

    fun saveGoalThresholdDate(date: LocalDate) {
        if (loadGoalThresholdDate() == null) {
            preferences.edit().putString("goal_threshold_date", date.toString()).apply()
        }
    }

    fun loadMeals(day: String): List<MealEntry> {
        val raw = preferences.getString("meals_$day", null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                MealEntry(
                    id = item.optLong("id"),
                    type = item.optString("type", "Öğün"),
                    calories = item.optInt("calories"),
                    protein = item.optInt("protein"),
                    carbs = item.optDouble("carbs"),
                    fat = item.optInt("fat"),
                )
            }
        }.getOrDefault(emptyList())
    }

    fun saveMeals(day: String, meals: List<MealEntry>) {
        val array = JSONArray()
        meals.forEach { meal ->
            array.put(
                JSONObject()
                    .put("id", meal.id)
                    .put("type", meal.type)
                    .put("calories", meal.calories)
                    .put("protein", meal.protein)
                    .put("carbs", meal.carbs)
                    .put("fat", meal.fat),
            )
        }
        preferences.edit().putString("meals_$day", array.toString()).apply()
    }

    fun loadWeights(): List<WeightEntry> {
        val raw = preferences.getString("weights", null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                WeightEntry(
                    id = item.optLong("id", index.toLong() + 1L),
                    day = item.optString("day", "Kayıt"),
                    weight = item.optDouble("weight", 61.0).toFloat(),
                )
            }
        }.getOrDefault(emptyList())
    }

    fun saveWeights(weights: List<WeightEntry>) {
        val array = JSONArray()
        weights.forEach { entry ->
            array.put(JSONObject().put("id", entry.id).put("day", entry.day).put("weight", entry.weight.toDouble()))
        }
        preferences.edit().putString("weights", array.toString()).apply()
    }

    fun loadPhotos(): List<Uri> = loadPhotoPaths().map { Uri.fromFile(File(it)) }

    fun copyPhotoToPrivateStorage(source: Uri): Uri? {
        return runCatching {
            val directory = File(appContext.filesDir, "progress_photos").apply { mkdirs() }
            val destination = File(directory, "progress_${System.currentTimeMillis()}.img")
            val input = appContext.contentResolver.openInputStream(source) ?: return null
            input.use { stream -> destination.outputStream().use(stream::copyTo) }
            val paths = loadPhotoPaths().toMutableList().apply { add(0, destination.absolutePath) }
            savePhotoPaths(paths)
            Uri.fromFile(destination)
        }.getOrNull()
    }

    fun removePhoto(uri: Uri) {
        val path = uri.path ?: return
        val directory = File(appContext.filesDir, "progress_photos").canonicalFile
        val file = File(path).canonicalFile
        if (file.path.startsWith(directory.path + File.separator)) file.delete()
        savePhotoPaths(loadPhotoPaths().filterNot { it == file.path })
    }

    private fun loadPhotoPaths(): List<String> {
        val raw = preferences.getString("progress_photo_paths", null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            List(array.length()) { index -> array.getString(index) }
        }.getOrDefault(emptyList())
    }

    private fun savePhotoPaths(paths: List<String>) {
        val array = JSONArray()
        paths.forEach(array::put)
        preferences.edit().putString("progress_photo_paths", array.toString()).apply()
    }
}
