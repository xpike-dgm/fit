package com.xpike.fit.ui

import android.app.Activity
import android.graphics.Color as AndroidColor
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class MacroTotals(
    val calories: Int = 0,
    val protein: Int = 0,
    val carbs: Double = 0.0,
    val fat: Int = 0,
)

private enum class AppTab(val label: String) {
    Home("Ana sayfa"),
    Log("Günlük"),
    Progress("İlerleme"),
    Profile("Profil"),
}

@Composable
fun FitApp() {
    val context = LocalContext.current
    val activity = context as? Activity
    val store = remember(context) { FitLocalStore(context) }
    var todayDate by remember { mutableStateOf(LocalDate.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            val currentDate = LocalDate.now()
            if (currentDate != todayDate) todayDate = currentDate
        }
    }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedDateText by rememberSaveable { mutableStateOf(todayDate.toString()) }
    LaunchedEffect(todayDate, selectedTab) {
        if (selectedTab != 1) selectedDateText = todayDate.toString()
    }
    val selectedDate = remember(selectedDateText) { runCatching { LocalDate.parse(selectedDateText) }.getOrDefault(todayDate) }
    val dateKey = selectedDate.toString()
    val todayKey = todayDate.toString()
    var planStartDate by remember(store) { mutableStateOf(store.loadOrCreatePlanStartDate(todayDate)) }
    val planDay = planDayFor(todayDate, planStartDate)
    var darkTheme by rememberSaveable { mutableStateOf(store.loadDarkTheme()) }
    var waterMl by rememberSaveable(dateKey) { mutableIntStateOf(store.loadWater(dateKey)) }
    var workoutDone by rememberSaveable(dateKey) { mutableStateOf(store.loadWorkout(dateKey)) }
    var dayComplete by rememberSaveable(dateKey) { mutableStateOf(store.loadDayComplete(dateKey)) }
    var weightDisturbance by rememberSaveable(dateKey) { mutableStateOf(store.loadWeightDisturbance(dateKey)) }
    var showMealDialog by rememberSaveable { mutableStateOf(false) }
    var editingMeal by remember { mutableStateOf<MealEntry?>(null) }
    var showWeightDialog by rememberSaveable { mutableStateOf(false) }
    val meals = remember(dateKey) { mutableStateListOf<MealEntry>().apply { addAll(store.loadMeals(dateKey)) } }
    val weights = remember {
        mutableStateListOf<WeightEntry>().apply {
            addAll(store.loadWeights().ifEmpty { listOf(WeightEntry("Başlangıç", 61f)) })
        }
    }
    val weeklyWeightAverage = sevenDayWeightAverage(weights, todayDate.minusDays(6), todayDate)
    var goalThresholdDateText by rememberSaveable { mutableStateOf(store.loadGoalThresholdDate()?.toString()) }
    LaunchedEffect(weeklyWeightAverage, todayDate) {
        if (weeklyWeightAverage != null && weeklyWeightAverage >= 73.0 && goalThresholdDateText == null) {
            store.saveGoalThresholdDate(todayDate)
            goalThresholdDateText = todayDate.toString()
        }
    }
    val goalThresholdDate = goalThresholdDateText?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        ?: if (weeklyWeightAverage != null && weeklyWeightAverage >= 73.0) todayDate else null
    val confirmationAverage = goalThresholdDate?.let { thresholdDate ->
        if (todayDate.isBefore(thresholdDate.plusDays(7))) null
        else sevenDayWeightAverage(weights, thresholdDate.plusDays(1), thresholdDate.plusDays(7))
    }
    val goalPlanStopped = goalThresholdDate != null
    val goalStatusMessage = when {
        !goalPlanStopped -> null
        confirmationAverage == null -> "73,0 kg eşiği görüldü · sonraki 7 günlük ortalama ile teyit bekleniyor."
        confirmationAverage >= 73.0 -> "73,0 kg hedefi sonraki tam haftanın ortalamasıyla teyit edildi."
        else -> "Hedef eşiği görüldü; teyit haftasının ortalaması 73,0 kg altında kaldı."
    }
    val reviewResult = remember(planDay, weights.size, meals.size, dayComplete, weeklyWeightAverage, goalThresholdDateText) {
        evaluateTargetReview(planDay, planStartDate, weights.toList(), store)
    }
    val targets = dailyTargetsFor(todayDate, planStartDate, store).let { current ->
        if (goalPlanStopped) {
            current.copy(
                gainPlanStopped = true,
                status = "Artış hedefi durduruldu · idame hedefi ayrıca belirlenmeli.",
            )
        } else current
    }
    var currentWeight by rememberSaveable { mutableStateOf(weights.lastOrNull()?.weight ?: 61f) }
    val photos = remember { mutableStateListOf<Uri>().apply { addAll(store.loadPhotos()) } }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) store.copyPhotoToPrivateStorage(uri)?.let { photos.add(0, it) }
    }

    val totals = remember(meals.size, meals.map { it.id }) {
        MacroTotals(
            calories = meals.sumOf { it.calories },
            protein = meals.sumOf { it.protein },
            carbs = meals.sumOf { it.carbs },
            fat = meals.sumOf { it.fat },
        )
    }
    val today = remember(todayDate) { todayDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.forLanguageTag("tr-TR"))) }

    FitTheme(darkTheme = darkTheme) {
        SideEffect {
            activity?.window?.let { window ->
                window.statusBarColor = if (darkTheme) AndroidColor.rgb(16, 18, 15) else AndroidColor.rgb(244, 246, 241)
                window.navigationBarColor = if (darkTheme) AndroidColor.rgb(16, 18, 15) else AndroidColor.rgb(244, 246, 241)
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                ) {
                    AppTab.entries.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = {
                                selectedTab = index
                                if (index != 1) selectedDateText = todayDate.toString()
                            },
                            icon = {
                                Icon(
                                    imageVector = when (tab) {
                                        AppTab.Home -> Icons.Outlined.Home
                                        AppTab.Log -> Icons.Outlined.Restaurant
                                        AppTab.Progress -> Icons.Outlined.ShowChart
                                        AppTab.Profile -> Icons.Outlined.Person
                                    },
                                    contentDescription = tab.label,
                                )
                            },
                            label = { Text(tab.label, fontSize = 11.sp) },
                            alwaysShowLabel = true,
                        )
                    }
                }
            },
        ) { innerPadding ->
            val contentModifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)

            when (AppTab.entries[selectedTab]) {
                AppTab.Home -> HomeScreen(
                    modifier = contentModifier,
                    dateLabel = today,
                    currentWeight = currentWeight,
                    weeklyWeightAverage = weeklyWeightAverage,
                    planDay = planDay,
                    goalStatusMessage = goalStatusMessage,
                    totals = totals,
                    targets = targets,
                    reviewMessage = reviewResult?.message,
                    mealCount = meals.size,
                    waterMl = waterMl,
                    workoutDone = workoutDone,
                    onAddMeal = { selectedDateText = todayDate.toString(); selectedTab = 1; showMealDialog = true },
                    onAddWater = { waterMl += 250; store.saveWater(todayKey, waterMl) },
                    onWorkoutToggle = { workoutDone = !workoutDone; store.saveWorkout(todayKey, workoutDone) },
                    onAddWeight = { showWeightDialog = true; selectedTab = 2 },
                    onOpenProgress = { selectedTab = 2 },
                )
                AppTab.Log -> LogScreen(
                    modifier = contentModifier,
                    selectedDate = selectedDate,
                    todayDate = todayDate,
                    earliestDate = planStartDate,
                    totals = totals,
                    targets = if (selectedDate == todayDate) targets else dailyTargetsFor(selectedDate, planStartDate, store),
                    reviewMessage = if (selectedDate == todayDate) reviewResult?.message else null,
                    meals = meals,
                    dayComplete = dayComplete,
                    weightDisturbance = weightDisturbance,
                    onDateChange = { date -> selectedDateText = date.toString() },
                    onAddMeal = { editingMeal = null; showMealDialog = true },
                    onEditMeal = { meal -> editingMeal = meal; showMealDialog = true },
                    onDeleteMeal = { entry ->
                        meals.remove(entry)
                        dayComplete = false
                        store.saveDayComplete(dateKey, false)
                        store.saveMeals(dateKey, meals.toList())
                    },
                    onDayCompleteChange = { completed ->
                        dayComplete = completed
                        store.saveDayComplete(dateKey, completed)
                    },
                    onWeightDisturbanceChange = { present ->
                        weightDisturbance = present
                        store.saveWeightDisturbance(dateKey, present)
                    },
                )
                AppTab.Progress -> ProgressScreen(
                    modifier = contentModifier,
                    currentWeight = currentWeight,
                    weeklyWeightAverage = weeklyWeightAverage,
                    goalStatusMessage = goalStatusMessage,
                    weights = weights,
                    photos = photos,
                    onAddWeight = { showWeightDialog = true },
                    onAddPhoto = {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    onRemovePhoto = { uri -> store.removePhoto(uri); photos.remove(uri) },
                )
                AppTab.Profile -> ProfileScreen(
                    modifier = contentModifier,
                    darkTheme = darkTheme,
                    onThemeChange = { darkTheme = it; store.saveDarkTheme(it) },
                    planDay = planDay,
                    reviewMessage = reviewResult?.message,
                    targets = targets,
                    planStartDate = planStartDate,
                    onPlanStartDateChange = { date ->
                        planStartDate = date
                        store.savePlanStartDate(date)
                    },
                )
            }
        }
    }

    if (showMealDialog) {
        MealEntryDialog(
            initialEntry = editingMeal,
            onDismiss = { showMealDialog = false; editingMeal = null },
            onSave = { type, calories, protein, carbs, fat ->
                val editing = editingMeal
                val updated = MealEntry(
                    id = editing?.id ?: System.nanoTime(),
                    type = type,
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fat = fat,
                )
                if (editing == null) meals.add(updated) else {
                    val index = meals.indexOfFirst { it.id == editing.id }
                    if (index >= 0) meals[index] = updated
                }
                dayComplete = false
                store.saveDayComplete(dateKey, false)
                store.saveMeals(dateKey, meals.toList())
                editingMeal = null
                showMealDialog = false
            },
        )
    }

    if (showWeightDialog) {
        WeightEntryDialog(
            initialValue = currentWeight,
            onDismiss = { showWeightDialog = false },
            onSave = { value ->
                currentWeight = value
                weights.add(WeightEntry(todayKey, value))
                store.saveWeights(weights.toList())
                showWeightDialog = false
            },
        )
    }
}

@Composable
private fun HomeScreen(
    modifier: Modifier,
    dateLabel: String,
    currentWeight: Float,
    weeklyWeightAverage: Double?,
    planDay: Int,
    goalStatusMessage: String?,
    totals: MacroTotals,
    targets: DailyTargets,
    reviewMessage: String?,
    mealCount: Int,
    waterMl: Int,
    workoutDone: Boolean,
    onAddMeal: () -> Unit,
    onAddWater: () -> Unit,
    onWorkoutToggle: () -> Unit,
    onAddWeight: () -> Unit,
    onOpenProgress: () -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            HomeHeader(dateLabel = dateLabel)
        }
        item {
            GoalHeroCard(
                currentWeight = currentWeight,
                weeklyWeightAverage = weeklyWeightAverage,
                planDay = planDay,
                goalStatusMessage = goalStatusMessage,
                onClick = onOpenProgress,
            )
        }
        item {
            DailyNutritionCard(totals = totals, targets = targets, status = reviewMessage ?: targets.status, onAddMeal = onAddMeal)
        }
        item {
            SectionHeading(title = "Bugünkü alışkanlıklar", trailing = "90 günlük plan")
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                WaterCard(
                    modifier = Modifier.weight(1f),
                    waterMl = waterMl,
                    onAdd = onAddWater,
                )
                WorkoutCard(
                    modifier = Modifier.weight(1f),
                    workoutDone = workoutDone,
                    onToggle = onWorkoutToggle,
                )
            }
        }
        item {
            SectionHeading(title = "İlerleme", trailing = "Gör")
        }
        item {
            WeightMiniCard(currentWeight = currentWeight, onAddWeight = onAddWeight)
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onAddMeal)
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                        Text("Öğün kayıtları", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (mealCount == 0) "Bugün henüz kayıt yok" else "$mealCount öğün kaydedildi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(Icons.Outlined.ChevronRight, contentDescription = "Öğünlere git", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(dateLabel: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("FIT", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = MaterialTheme.colorScheme.secondary)
            Text(
                dateLabel.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.forLanguageTag("tr-TR")) else it.toString() },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
            Text("Bugün nasıl gidiyor?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center,
        ) {
            Text("73", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun GoalHeroCard(
    currentWeight: Float,
    weeklyWeightAverage: Double?,
    planDay: Int,
    goalStatusMessage: String?,
    onClick: () -> Unit,
) {
    val trackedWeight = weeklyWeightAverage?.toFloat() ?: currentWeight
    val progress = ((trackedWeight - 61f) / (73f - 61f)).coerceIn(0f, 1f)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.ShowChart, contentDescription = null, modifier = Modifier.size(17.dp), tint = MaterialTheme.colorScheme.onPrimary)
                    Text("  KİLO HEDEFİ", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onPrimary)
                }
                Text("73,0 kg", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(top = 8.dp))
                Text(
                    weeklyWeightAverage?.let { "7 günlük ortalama ${it.formatGrams()} kg" } ?: "${currentWeight.oneDecimal()} kg  →  73,0 kg",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.76f),
                )
                Text(
                    goalStatusMessage ?: "Başlangıç 61 kg · gün ${planDay.coerceAtMost(90)} / 90 · takip gerekirse sürer",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(82.dp)) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f),
                    strokeWidth = 7.dp,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${(progress * 100).toInt()}%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    Text("tamamlandı", fontSize = 9.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f))
                }
            }
        }
    }
}

@Composable
private fun DailyNutritionCard(totals: MacroTotals, targets: DailyTargets, status: String?, onAddMeal: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Günlük beslenme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        if (targets.gainPlanStopped) "Artış dönemi durdu · idame hedefi belirlenmeli"
                        else "Günlük hedef · ${targets.calories.formatCalories()} kcal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
                TextButton(onClick = onAddMeal) {
                    Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Öğün")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(totals.calories.formatCalories(), fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold)
                if (targets.gainPlanStopped) {
                    Text(" kcal kaydedildi", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 6.dp, bottom = 5.dp))
                } else {
                    Text(" / ${targets.calories.formatCalories()} kcal", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 6.dp, bottom = 5.dp))
                }
            }
            if (!targets.gainPlanStopped) {
                ProgressTrack(
                    progress = (totals.calories.toFloat() / targets.calories.coerceAtLeast(1)).coerceIn(0f, 1f),
                    modifier = Modifier.padding(top = 8.dp).height(7.dp),
                    color = MaterialTheme.colorScheme.secondary,
                )
                val calorieDifference = targets.calories - totals.calories
                Text(
                    if (calorieDifference >= 0) "Kalan ${calorieDifference.formatCalories()} kcal" else "Hedefin ${(-calorieDifference).formatCalories()} kcal üzeri",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MacroValue(modifier = Modifier.weight(1f), label = "Protein", value = totals.protein.toDouble(), target = targets.protein.toDouble().takeUnless { targets.gainPlanStopped }, tint = Color(0xFF579A63))
                MacroValue(modifier = Modifier.weight(1f), label = "Karbonhidrat", value = totals.carbs, target = targets.carbs.takeUnless { targets.gainPlanStopped }, tint = Color(0xFFD5983E))
                MacroValue(modifier = Modifier.weight(1f), label = "Yağ", value = totals.fat.toDouble(), target = targets.fat.toDouble().takeUnless { targets.gainPlanStopped }, tint = Color(0xFF7779CE))
            }
            if (!status.isNullOrBlank()) {
                Text(status, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 12.dp))
            }
        }
    }
}

@Composable
private fun MacroValue(modifier: Modifier = Modifier, label: String, value: Double, target: Double?, tint: Color) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(tint))
            Text(" $label", fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            if (target == null) "${value.formatGrams()}g" else "${value.formatGrams()} / ${target.formatGrams()}g",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 5.dp),
            maxLines = 1,
        )
        if (target != null) {
            ProgressTrack(
                progress = (value / target.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f),
                modifier = Modifier.padding(top = 6.dp).height(5.dp),
                color = tint,
            )
        }
    }
}

@Composable
private fun ProgressTrack(progress: Float, modifier: Modifier = Modifier, color: Color) {
    Box(
        modifier = modifier.fillMaxWidth().clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        if (progress > 0f) {
            Box(
                modifier = Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxSize().clip(CircleShape).background(color),
            )
        }
    }
}

@Composable
private fun SectionHeading(title: String, trailing: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(trailing, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun WaterCard(modifier: Modifier, waterMl: Int, onAdd: () -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Box(Modifier.size(38.dp).clip(CircleShape).background(Color(0xFFE4F4FC)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.WaterDrop, contentDescription = null, tint = Color(0xFF4298C8), modifier = Modifier.size(19.dp))
            }
            Text("Su", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 12.dp))
            Text("${waterMl} ml", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Günlük toplam", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            OutlinedButton(
                onClick = onAdd,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(38.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Text("250 ml", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun WorkoutCard(modifier: Modifier, workoutDone: Boolean, onToggle: () -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Box(Modifier.size(38.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.DirectionsRun, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
            }
            Text("Antrenman", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 12.dp))
            Text(if (workoutDone) "Tamamlandı" else "Bugün kayıt yok", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("Haftalık hedef · 4 gün", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
            OutlinedButton(
                onClick = onToggle,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(38.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(if (workoutDone) Icons.Outlined.Check else Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(if (workoutDone) "İşaretlendi" else "Günü işaretle", fontSize = 12.sp, maxLines = 1)
            }
        }
    }
}

@Composable
private fun WeightMiniCard(currentWeight: Float, onAddWeight: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFFFEEDB)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.MonitorWeight, contentDescription = null, tint = Color(0xFFC58232))
            }
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text("Son tartı", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${currentWeight.oneDecimal()} kg", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(onClick = onAddWeight, shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(17.dp))
                Text("Tartı")
            }
        }
    }
}

@Composable
private fun LogScreen(
    modifier: Modifier,
    selectedDate: LocalDate,
    todayDate: LocalDate,
    earliestDate: LocalDate,
    totals: MacroTotals,
    targets: DailyTargets,
    reviewMessage: String?,
    meals: List<MealEntry>,
    dayComplete: Boolean,
    weightDisturbance: Boolean,
    onDateChange: (LocalDate) -> Unit,
    onAddMeal: () -> Unit,
    onEditMeal: (MealEntry) -> Unit,
    onDeleteMeal: (MealEntry) -> Unit,
    onDayCompleteChange: (Boolean) -> Unit,
    onWeightDisturbanceChange: (Boolean) -> Unit,
) {
    val dateLabel = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.forLanguageTag("tr-TR")))
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ScreenHeader(title = "Günlük kayıt", subtitle = dateLabel)
        }
        item {
            LogDateSelector(
                selectedDate = selectedDate,
                earliestDate = earliestDate,
                todayDate = todayDate,
                onDateChange = onDateChange,
            )
        }
        item {
            DailyNutritionCard(totals = totals, targets = targets, status = reviewMessage ?: targets.status, onAddMeal = onAddMeal)
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(if (dayComplete) "Günlük kayıt tamamlandı" else "Gün tamamlandığında işaretle", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("Eksik kayıtlar otomatik olarak sıfır sayılmaz.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = dayComplete, onCheckedChange = onDayCompleteChange)
                }
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Tartıyı etkileyen durum", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("Kreatin değişimi, hastalık veya belirgin sıvı değişimi", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = weightDisturbance, onCheckedChange = onWeightDisturbanceChange)
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Öğünler", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Değerleri kendin gir, uygulama gün toplamını hesaplasın.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onAddMeal) {
                    Icon(Icons.Outlined.Add, contentDescription = "Öğün ekle")
                }
            }
        }
        if (meals.isEmpty()) {
            item { EmptyMealsCard(onAddMeal = onAddMeal) }
        } else {
            items(meals, key = { it.id }) { entry ->
                MealEntryCard(entry = entry, onEdit = { onEditMeal(entry) }, onDelete = { onDeleteMeal(entry) })
            }
        }
        item {
            Button(
                onClick = onAddMeal,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Text("Öğün kaydı ekle", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun LogDateSelector(
    selectedDate: LocalDate,
    earliestDate: LocalDate,
    todayDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = { onDateChange(selectedDate.minusDays(1)) }, enabled = selectedDate.isAfter(earliestDate)) {
                Text("‹", fontSize = 26.sp, fontWeight = FontWeight.Medium)
            }
            TextButton(onClick = { showPicker = true }) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(selectedDate.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("tr-TR"))), modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.SemiBold)
            }
            TextButton(onClick = { onDateChange(selectedDate.plusDays(1)) }, enabled = selectedDate.isBefore(todayDate)) {
                Text("›", fontSize = 26.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
    if (showPicker) {
        DatePickerPopup(
            initialDate = selectedDate,
            minimumDate = earliestDate,
            maximumDate = todayDate,
            onDismiss = { showPicker = false },
            onSelect = { onDateChange(it); showPicker = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerPopup(
    initialDate: LocalDate,
    minimumDate: LocalDate,
    maximumDate: LocalDate,
    onDismiss: () -> Unit,
    onSelect: (LocalDate) -> Unit,
) {
    val selectableDates = remember(minimumDate, maximumDate) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
                return !date.isBefore(minimumDate) && !date.isAfter(maximumDate)
            }

            override fun isSelectableYear(year: Int): Boolean = year in minimumDate.year..maximumDate.year
        }
    }
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        yearRange = minimumDate.year..maximumDate.year,
        selectableDates = selectableDates,
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { millis ->
                        val picked = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        onSelect(picked.coerceIn(minimumDate, maximumDate))
                    }
                },
                enabled = state.selectedDateMillis != null,
            ) { Text("Seç") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Vazgeç") } },
    ) {
        DatePicker(state = state, showModeToggle = false)
    }
}

@Composable
private fun EmptyMealsCard(onAddMeal: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 30.dp, horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("İlk öğününü ekle", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
            Text("Kalori ve makroları öğün toplamı olarak gir.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            TextButton(onClick = onAddMeal, modifier = Modifier.padding(top = 8.dp)) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Kayıt oluştur")
            }
        }
    }
}

@Composable
private fun MealEntryCard(entry: MealEntry, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 13.dp, bottom = 13.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(38.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary)
            }
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(entry.type, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("P ${entry.protein} g  ·  K ${entry.carbs.formatGrams()} g  ·  Y ${entry.fat} g", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${entry.calories} kcal", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Row {
                    TextButton(onClick = onEdit, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp), modifier = Modifier.height(30.dp)) {
                        Text("Düzenle", fontSize = 11.sp)
                    }
                    TextButton(onClick = onDelete, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp), modifier = Modifier.height(30.dp)) {
                        Text("Sil", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressScreen(
    modifier: Modifier,
    currentWeight: Float,
    weeklyWeightAverage: Double?,
    goalStatusMessage: String?,
    weights: List<WeightEntry>,
    photos: List<Uri>,
    onAddWeight: () -> Unit,
    onAddPhoto: () -> Unit,
    onRemovePhoto: (Uri) -> Unit,
) {
    var photoToDelete by remember { mutableStateOf<Uri?>(null) }
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { ScreenHeader(title = "İlerleme", subtitle = "Tartı ve fotoğraf kayıtların") }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(26.dp),
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("SON TARTI", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f))
                        Text("${currentWeight.oneDecimal()} kg", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(top = 5.dp))
                        Text(weeklyWeightAverage?.let { "7 günlük ortalama ${it.formatGrams()} kg" } ?: "İlk hafta tartı kayıtları bekleniyor", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                        Text("Hedef 73,0 kg · 90 günlük öncelikli dönem", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), modifier = Modifier.padding(top = 2.dp))
                    }
                    OutlinedButton(
                        onClick = onAddWeight,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary),
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(17.dp))
                        Text("Tartı ekle")
                    }
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Kilo eğilimi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Kayıtların zamanla burada görünür", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Outlined.ShowChart, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    }
                    WeightChart(weights = weights, modifier = Modifier.fillMaxWidth().height(150.dp).padding(top = 14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f), modifier = Modifier.padding(vertical = 12.dp))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Başlangıç  61,0 kg", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Hedef  73,0 kg", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("İlerleme fotoğrafları", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Fotoğraflar cihazından seçilir.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedButton(onClick = onAddPhoto, shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Outlined.CameraAlt, contentDescription = null, modifier = Modifier.size(17.dp))
                    Text(" Ekle")
                }
            }
        }
        if (photos.isEmpty()) {
            item { EmptyPhotoCard(onAddPhoto = onAddPhoto) }
        } else {
            items(photos, key = { it.toString() }) { uri ->
                PhotoItem(uri = uri, onRemove = { photoToDelete = uri })
            }
        }
        item {
            Text(
                when {
                    goalStatusMessage != null -> goalStatusMessage
                    weeklyWeightAverage == null -> "7 günlük ortalama için haftada en az 5 ayrı sabah tartısı kaydet. Günlük değişimler su ve diğer etkenlerden etkilenebilir."
                    else -> "Günlük değişimler su ve diğer etkenlerden etkilenebilir. Kalori değerlendirmesi 14 günlük eğilime ve kayıt yeterliliğine bağlıdır."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 2.dp),
            )
        }
    }
    photoToDelete?.let { uri ->
        AlertDialog(
            onDismissRequest = { photoToDelete = null },
            title = { Text("Fotoğrafı sil?", fontWeight = FontWeight.Bold) },
            text = { Text("Bu fotoğraf cihazdaki uygulama kayıtlarından kaldırılır.") },
            confirmButton = {
                TextButton(onClick = { onRemovePhoto(uri); photoToDelete = null }) {
                    Text("Sil", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { photoToDelete = null }) { Text("Vazgeç") } },
            shape = RoundedCornerShape(24.dp),
        )
    }
}

@Composable
private fun WeightChart(weights: List<WeightEntry>, modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.secondary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
    Canvas(modifier = modifier) {
        val rows = 4
        for (i in 0 until rows) {
            val y = size.height * i / (rows - 1)
            drawLine(color = gridColor, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }
        if (weights.size >= 2) {
            val values = weights.takeLast(7).map { it.weight }
            val min = (values.minOrNull() ?: 0f) - 0.5f
            val max = (values.maxOrNull() ?: 1f) + 0.5f
            val span = (max - min).coerceAtLeast(1f)
            val points = values.mapIndexed { index, value ->
                Offset(
                    x = size.width * index / (values.lastIndex.coerceAtLeast(1)),
                    y = size.height - ((value - min) / span * size.height),
                )
            }
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(path = path, color = lineColor, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
            points.forEach { drawCircle(color = lineColor, radius = 4.dp.toPx(), center = it) }
        } else {
            val y = size.height * 0.55f
            drawCircle(color = lineColor, radius = 5.dp.toPx(), center = Offset(12.dp.toPx(), y))
        }
    }
}

@Composable
private fun EmptyPhotoCard(onAddPhoto: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onAddPhoto),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(50.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text("İlk fotoğrafını ekle", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("İlerlemeni karşılaştırmak için kaydet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PhotoItem(uri: Uri, onRemove: () -> Unit) {
    val context = LocalContext.current
    val image by produceState<android.graphics.Bitmap?>(initialValue = null, key1 = uri) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
            }.getOrNull()
        }
    }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (image != null) {
                Image(
                    bitmap = image!!.asImageBitmap(),
                    contentDescription = "İlerleme fotoğrafı",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(72.dp).clip(RoundedCornerShape(14.dp)),
                )
            } else {
                Box(Modifier.size(72.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text("İlerleme fotoğrafı", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("Bugün", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onRemove) { Text("Sil", color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun ProfileScreen(
    modifier: Modifier,
    darkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    planDay: Int,
    reviewMessage: String?,
    targets: DailyTargets,
    planStartDate: LocalDate,
    onPlanStartDateChange: (LocalDate) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { ScreenHeader(title = "Profil", subtitle = "Kişisel hedeflerin ve uygulama ayarları") }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    Text("Başlangıç bilgileri", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Tahmini başlangıç değerleri · gerçek ihtiyaç kayıtlarla güncellenir.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 3.dp, bottom = 14.dp))
                    ProfileInfoRow("Yaş", "23")
                    ProfileInfoRow("Boy", "180 cm")
                    ProfileInfoRow("Başlangıç kilosu", "61 kg")
                    ProfileInfoRow("Hedef kilosu", "73,0 kg")
                    ProfileInfoRow("Günlük hareket", "Orta")
                    ProfileInfoRow("Antrenman", "Haftada 4 gün")
                    ProfileInfoRow("Bugünkü enerji hedefi", if (targets.gainPlanStopped) "İdame hedefi gerekli" else "${targets.calories.formatCalories()} kcal")
                    ProfileInfoRow(
                        "Protein / karbonhidrat / yağ",
                        if (targets.gainPlanStopped) "İdame değerlendirmesi gerekli" else "${targets.protein} / ${targets.carbs.formatGrams()} / ${targets.fat} g",
                        last = true,
                    )
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    Text("Görünüm", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(42.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                            Icon(if (darkTheme) Icons.Outlined.DarkMode else Icons.Outlined.LightMode, contentDescription = null)
                        }
                        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(if (darkTheme) "Koyu tema" else "Açık tema", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("Görünümü dilediğin zaman değiştir.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = darkTheme, onCheckedChange = onThemeChange)
                    }
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(24.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                        Text("Plan süresi", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (planDay <= 90) "Öncelikli dönem 90 gün · şu an $planDay. gün" else "90 gün tamamlandı · takip ${planDay}. günde sürüyor",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            var showPlanDatePicker by rememberSaveable { mutableStateOf(false) }
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(22.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                            Text("Plan başlangıç tarihi", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(planStartDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("tr-TR"))), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    OutlinedButton(
                        onClick = { showPlanDatePicker = true },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) { Text("Başlangıç tarihini değiştir") }
                }
            }
            if (showPlanDatePicker) {
                DatePickerPopup(
                    initialDate = planStartDate,
                    minimumDate = LocalDate.of(2000, 1, 1),
                    maximumDate = LocalDate.now(),
                    onDismiss = { showPlanDatePicker = false },
                    onSelect = { onPlanStartDateChange(it); showPlanDatePicker = false },
                )
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(22.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    Text("Hedef güncelleme kuralı", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("Tartı ve tam günlük kayıtlar yeterliyse 14 günde bir en çok ±100 kcal değişir. Eksik kalori ertesi güne borç olmaz; fazla alım aynı hafta en çok −100 kcal/gün dengelenir.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
                    reviewMessage?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 10.dp))
                    }
                    Text("SSN Gainer ve HIQ Creatine değerlerini etikete göre öğün kaydına gir. Ürünler otomatik olarak kalori veya makro eklemez.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String, last: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
    if (!last) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
}

@Composable
private fun ScreenHeader(title: String, subtitle: String) {
    Column {
        Text("FIT", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, letterSpacing = 2.sp, color = MaterialTheme.colorScheme.secondary)
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun MealEntryDialog(
    initialEntry: MealEntry?,
    onDismiss: () -> Unit,
    onSave: (String, Int, Int, Double, Int) -> Unit,
) {
    var selectedType by rememberSaveable(initialEntry?.id) { mutableStateOf(initialEntry?.type ?: "Kahvaltı") }
    var calories by rememberSaveable(initialEntry?.id) { mutableStateOf(initialEntry?.calories?.toString() ?: "") }
    var protein by rememberSaveable(initialEntry?.id) { mutableStateOf(initialEntry?.protein?.toString() ?: "") }
    var carbs by rememberSaveable(initialEntry?.id) { mutableStateOf(initialEntry?.carbs?.formatGrams() ?: "") }
    var fat by rememberSaveable(initialEntry?.id) { mutableStateOf(initialEntry?.fat?.toString() ?: "") }
    val types = listOf("Kahvaltı", "Öğle", "Akşam", "Ara öğün")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialEntry == null) "Öğün değerleri" else "Öğünü düzenle", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 460.dp).verticalScroll(rememberScrollState())) {
                Text("Yiyecek önerisi yok · değerleri sen gir", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    types.forEach { type ->
                        FilterChip(selected = selectedType == type, onClick = { selectedType = type }, label = { Text(type, maxLines = 1) })
                    }
                }
                NumberField(label = "Kalori (kcal)", value = calories, onValueChange = { calories = it }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                    NumberField(label = "Protein (g)", value = protein, onValueChange = { protein = it }, modifier = Modifier.weight(1f))
                NumberField(label = "Karbonhidrat (g)", value = carbs, onValueChange = { carbs = it }, modifier = Modifier.weight(1f), decimal = true)
                }
                NumberField(label = "Yağ (g)", value = fat, onValueChange = { fat = it }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                Text("Boş makro alanları 0 olarak kaydedilir.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        selectedType,
                        calories.toIntOrNull() ?: 0,
                        protein.toIntOrNull() ?: 0,
                        carbs.replace(',', '.').toDoubleOrNull() ?: 0.0,
                        fat.toIntOrNull() ?: 0,
                    )
                },
                enabled = calories.toIntOrNull() != null,
                shape = RoundedCornerShape(14.dp),
            ) { Text("Kaydet") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Vazgeç") } },
        shape = RoundedCornerShape(28.dp),
    )
}

@Composable
private fun WeightEntryDialog(
    initialValue: Float,
    onDismiss: () -> Unit,
    onSave: (Float) -> Unit,
) {
    var value by rememberSaveable { mutableStateOf(initialValue.oneDecimal()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tartı kaydı", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Ölçümünü kilogram olarak gir.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                NumberField(label = "Kilo (kg)", value = value, onValueChange = { value = it }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp), decimal = true)
            }
        },
        confirmButton = {
            Button(
                onClick = { value.replace(',', '.').toFloatOrNull()?.let(onSave) },
                enabled = value.replace(',', '.').toFloatOrNull()?.let { it in 30f..250f } == true,
                shape = RoundedCornerShape(14.dp),
            ) { Text("Kaydet") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Vazgeç") } },
        shape = RoundedCornerShape(28.dp),
    )
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    decimal: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { raw ->
            val cleaned = if (decimal) raw.filter { it.isDigit() || it == ',' || it == '.' } else raw.filter(Char::isDigit)
            onValueChange(cleaned)
        },
        modifier = modifier,
        label = { Text(label, fontSize = 12.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number),
        shape = RoundedCornerShape(14.dp),
    )
}

private fun Int.formatCalories(): String = String.format(Locale.forLanguageTag("tr-TR"), "%,d", this)

private fun Double.formatGrams(): String =
    if (kotlin.math.abs(this - kotlin.math.round(this)) < 0.05) {
        String.format(Locale.forLanguageTag("tr-TR"), "%.0f", this)
    } else {
        String.format(Locale.forLanguageTag("tr-TR"), "%.1f", this)
    }

private fun Float.oneDecimal(): String = String.format(Locale.forLanguageTag("tr-TR"), "%.1f", this)
