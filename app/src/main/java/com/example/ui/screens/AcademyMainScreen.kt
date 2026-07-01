package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ForumPostEntity
import com.example.data.database.StudyGroupEntity
import com.example.data.database.StudyReminderEntity
import com.example.data.repository.CodingChallenge
import com.example.data.repository.JobListing
import com.example.data.repository.Lesson
import com.example.data.repository.Simulation
import com.example.data.repository.Reply
import com.example.ui.theme.*
import com.example.ui.viewmodel.AcademyScreen
import com.example.ui.viewmodel.AcademyViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademyMainScreen(
    viewModel: AcademyViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val progress by viewModel.userProgress.collectAsState()

    val completedLessons = remember(progress) {
        progress?.completedLessonIds?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()
    }
    val completedChallenges = remember(progress) {
        progress?.completedChallengeIds?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()
    }
    val completedSimulations = remember(progress) {
        progress?.completedSimulationIds?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Shield logo",
                            tint = CyberTeal,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = "CYBER ACADEMY",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Interactive Defense Lab",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CyberTeal,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                },
                actions = {
                    // Profile XP Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .background(SlateCard, RoundedCornerShape(20.dp))
                            .border(1.dp, SlateBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "XP Icon",
                            tint = CyberOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Lvl ${progress?.level ?: 1}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "${progress?.xp ?: 0} XP",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = CyberTeal
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SlateDark,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            // Standard Navigation Bar (M3 pill style)
            NavigationBar(
                containerColor = SlateDark,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                val screens = listOf(
                    Triple(AcademyScreen.Home, "Learn", Icons.Default.Home),
                    Triple(AcademyScreen.ThreatSim, "Threats", Icons.Default.Warning),
                    Triple(AcademyScreen.Coding, "Coding", Icons.Default.Build),
                    Triple(AcademyScreen.Interview, "Interview", Icons.Default.AccountCircle),
                    Triple(AcademyScreen.Forum, "Mentor", Icons.Default.Share),
                    Triple(AcademyScreen.Reminders, "Schedule", Icons.Default.Notifications),
                    Triple(AcademyScreen.Jobs, "Careers", Icons.Default.Email)
                )

                screens.forEach { (screen, label, icon) ->
                    NavigationBarItem(
                        selected = currentScreen == screen,
                        onClick = { viewModel.setScreen(screen) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (currentScreen == screen) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 10.sp
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SlateDark,
                            selectedTextColor = CyberTeal,
                            indicatorColor = CyberTeal,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_${screen.name.lowercase()}")
                    )
                }
            }
        },
        containerColor = SlateDark,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(SlateDark)
        ) {
            when (currentScreen) {
                AcademyScreen.Home -> HomeScreenContent(
                    viewModel = viewModel,
                    completedLessonIds = completedLessons
                )
                AcademyScreen.ThreatSim -> ThreatSimScreenContent(
                    viewModel = viewModel,
                    completedSimulations = completedSimulations
                )
                AcademyScreen.Coding -> CodingScreenContent(
                    viewModel = viewModel,
                    completedChallengeIds = completedChallenges
                )
                AcademyScreen.Forum -> ForumScreenContent(
                    viewModel = viewModel
                )
                AcademyScreen.Interview -> InterviewScreenContent(
                    viewModel = viewModel
                )
                AcademyScreen.Reminders -> RemindersScreenContent(
                    viewModel = viewModel
                )
                AcademyScreen.Jobs -> JobsScreenContent(
                    viewModel = viewModel
                )
            }
        }
    }
}

// ==========================================
// 1. HOME / LESSONS SCREEN
// ==========================================
@Composable
fun HomeScreenContent(
    viewModel: AcademyViewModel,
    completedLessonIds: Set<String>
) {
    val selectedPath by viewModel.selectedPath.collectAsState()
    val selectedLesson by viewModel.selectedLesson.collectAsState()

    val paths = viewModel.repository.certificationPaths
    val lessons = viewModel.getLessonsForSelectedPath()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (selectedLesson == null) {
            // --- HEADER INFO ---
            Text(
                text = "Structured Certification Paths",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            // --- PATH SELECTION TABS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                paths.forEach { path ->
                    val isSelected = path == selectedPath
                    Button(
                        onClick = { viewModel.selectPath(path) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) CyberTeal else SlateCard,
                            contentColor = if (isSelected) SlateDark else TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = path.replace("CompTIA ", ""),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // --- LESSON LIST ---
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(lessons) { lesson ->
                    val isCompleted = completedLessonIds.contains(lesson.id)
                    Card(
                        onClick = { viewModel.selectLesson(lesson) },
                        colors = CardDefaults.cardColors(containerColor = SlateCard),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (isCompleted) CyberGreen else SlateBorder, RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isCompleted) CyberGreen.copy(0.15f) else CyberBlue.copy(0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                                    contentDescription = "Status icon",
                                    tint = if (isCompleted) CyberGreen else CyberBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = lesson.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = lesson.description,
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                    maxLines = 2
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "+${lesson.points}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CyberOrange
                                    )
                                )
                                Text(
                                    text = "XP",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // --- DRILLDOWN DETAIL VIEW ---
            val lesson = selectedLesson!!
            val quizResult by viewModel.lessonQuizResult.collectAsState()
            val quizExplanation by viewModel.lessonExplanation.collectAsState()
            var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { viewModel.selectLesson(null) },
                    modifier = Modifier
                        .background(SlateCard, CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back to lessons",
                        tint = TextPrimary
                    )
                }
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp),
                    maxLines = 1
                )
                Box(modifier = Modifier.size(36.dp)) // spacer
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Lesson material text block
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateCard),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "CORE CONCEPTS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTeal
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = lesson.content,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 22.sp,
                                    color = TextPrimary
                                )
                            )
                        }
                    }
                }

                item {
                    // Interactive Gamified Quiz Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateDark),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CyberTeal.copy(0.4f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "KNOWLEDGE CHECK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTeal
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = lesson.quizQuestion,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Options list
                            lesson.quizOptions.forEachIndexed { idx, option ->
                                val isSelected = selectedOptionIndex == idx
                                val isQuizSubmitted = quizResult != null

                                val cardBg = when {
                                    isQuizSubmitted && idx == lesson.quizAnswerIndex -> CyberGreen.copy(0.15f)
                                    isQuizSubmitted && isSelected && idx != lesson.quizAnswerIndex -> CyberRed.copy(0.15f)
                                    isSelected -> CyberTeal.copy(0.15f)
                                    else -> SlateCard
                                }

                                val borderClr = when {
                                    isQuizSubmitted && idx == lesson.quizAnswerIndex -> CyberGreen
                                    isQuizSubmitted && isSelected && idx != lesson.quizAnswerIndex -> CyberRed
                                    isSelected -> CyberTeal
                                    else -> SlateBorder
                                }

                                Card(
                                    onClick = { if (quizResult == null) selectedOptionIndex = idx },
                                    colors = CardDefaults.cardColors(containerColor = cardBg),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .border(1.dp, borderClr, RoundedCornerShape(12.dp))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = option,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = TextPrimary,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (quizResult == null) {
                                Button(
                                    onClick = { selectedOptionIndex?.let { viewModel.submitLessonQuiz(it) } },
                                    enabled = selectedOptionIndex != null,
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberTeal, contentColor = SlateDark),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Text("Submit Answer", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                // Result feedback banner
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (quizResult == true) CyberGreen.copy(0.1f) else CyberRed.copy(0.1f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (quizResult == true) Icons.Default.CheckCircle else Icons.Default.Close,
                                            contentDescription = "Status",
                                            tint = if (quizResult == true) CyberGreen else CyberRed
                                        )
                                        Text(
                                            text = if (quizResult == true) "CORRECT! +${lesson.points} XP" else "INCORRECT ANSWER",
                                            fontWeight = FontWeight.Bold,
                                            color = if (quizResult == true) CyberGreen else CyberRed
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = quizExplanation,
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. REAL-TIME THREAT SIMULATION
// ==========================================
@Composable
fun ThreatSimScreenContent(
    viewModel: AcademyViewModel,
    completedSimulations: Set<String>
) {
    val selectedSim by viewModel.selectedSimulation.collectAsState()
    val simulations = viewModel.getSimulations()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (selectedSim == null) {
            Text(
                text = "Live Threat Mitigation Laboratory",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(simulations) { sim ->
                    val isDone = completedSimulations.contains(sim.id)
                    Card(
                        onClick = { viewModel.selectSimulation(sim) },
                        colors = CardDefaults.cardColors(containerColor = SlateCard),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (isDone) CyberGreen else SlateBorder, RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (isDone) CyberGreen.copy(0.15f) else CyberRed.copy(0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = "Threat Icon",
                                    tint = if (isDone) CyberGreen else CyberRed,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sim.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = sim.description,
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                    maxLines = 2
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "+${sim.points}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CyberOrange
                                    )
                                )
                                Text(
                                    text = "XP",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Interactive Terminal Console View
            val sim = selectedSim!!
            val logs by viewModel.consoleLogs.collectAsState()
            val completed by viewModel.simulationCompleted.collectAsState()
            var commandInput by remember { mutableStateOf("") }
            val focusManager = LocalFocusManager.current

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { viewModel.selectSimulation(null) },
                    modifier = Modifier
                        .background(SlateCard, CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Text(
                    text = "LAB: ${sim.title}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Box(modifier = Modifier.size(36.dp))
            }

            // Scenario card
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "INCIDENT BRIEFING",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CyberRed
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = sim.scenario,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                    )
                }
            }

            // Simulated Terminal View
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
                    .border(1.dp, CyberTeal.copy(0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                // Console Logs stream
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    reverseLayout = false,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(logs) { log ->
                        val color = when {
                            log.startsWith(">") -> CyberTeal
                            log.contains("VICTORY") || log.contains("SUCCESS") -> CyberGreen
                            log.contains("WARNING") || log.contains("ALERT") || log.contains("SYSTEM_ALERT") -> CyberOrange
                            log.contains("ERROR") -> CyberRed
                            else -> TextSecondary
                        }
                        Text(
                            text = log,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = color
                        )
                    }
                }

                Divider(color = SlateBorder, modifier = Modifier.padding(vertical = 8.dp))

                // Input shell row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "root@cybershield:~#",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = CyberTeal,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = commandInput,
                        onValueChange = { if (!completed) commandInput = it },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color.White
                        ),
                        placeholder = {
                            Text(
                                "Enter shell command...",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (commandInput.isNotBlank() && !completed) {
                                    viewModel.executeSimCommand(commandInput)
                                    commandInput = ""
                                    focusManager.clearFocus()
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("terminal_input")
                    )
                    IconButton(
                        onClick = {
                            if (commandInput.isNotBlank() && !completed) {
                                viewModel.executeSimCommand(commandInput)
                                commandInput = ""
                                focusManager.clearFocus()
                            }
                        },
                        enabled = !completed
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send command",
                            tint = if (completed) TextSecondary else CyberTeal
                        )
                    }
                }
            }

            if (completed) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberGreen.copy(0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = CyberGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = "Mitigation Complete!",
                                fontWeight = FontWeight.Bold,
                                color = CyberGreen
                            )
                            Text(
                                text = "You have successfully defended the platform. +${sim.points} XP earned.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. INTERACTIVE CODING CHALLENGES
// ==========================================
@Composable
fun CodingScreenContent(
    viewModel: AcademyViewModel,
    completedChallengeIds: Set<String>
) {
    val selectedChallenge by viewModel.selectedChallenge.collectAsState()
    val challenges = viewModel.getCodingChallenges()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (selectedChallenge == null) {
            Text(
                text = "Interactive Code Defense Suite",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(challenges) { challenge ->
                    val isCompleted = completedChallengeIds.contains(challenge.id)
                    Card(
                        onClick = { viewModel.selectChallenge(challenge) },
                        colors = CardDefaults.cardColors(containerColor = SlateCard),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (isCompleted) CyberGreen else SlateBorder, RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isCompleted) CyberGreen.copy(0.15f) else CyberBlue.copy(0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Edit,
                                    contentDescription = "Status",
                                    tint = if (isCompleted) CyberGreen else CyberBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = challenge.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = challenge.description,
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                    maxLines = 2
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "+${challenge.points}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CyberOrange
                                    )
                                )
                                Text(
                                    text = "XP",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            val challenge = selectedChallenge!!
            val challengeSubmitted by viewModel.challengeSubmitted.collectAsState()
            val challengeCorrect by viewModel.challengeCorrect.collectAsState()
            val challengeOutputMessage by viewModel.challengeOutputMessage.collectAsState()
            var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { viewModel.selectChallenge(null) },
                    modifier = Modifier
                        .background(SlateCard, CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Text(
                    text = "CHALLENGE: ${challenge.title}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Box(modifier = Modifier.size(36.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Task instruction card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateCard),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "INSTRUCTIONS",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CyberTeal
                                    )
                                )
                                Badge(containerColor = CyberBlue) {
                                    Text(
                                        challenge.language,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = challenge.instructions,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                            )
                        }
                    }
                }

                item {
                    // Code Block Editor
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0B0F19))
                            .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "vulnerable_code.${if (challenge.language == "Python") "py" else "kt"}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(
                            text = challenge.initialCode,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }

                challenge.multipleChoiceOptions?.let { options ->
                    item {
                        Column {
                            Text(
                                text = "SELECT THE CORRECT PATCH:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTeal
                                ),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            options.forEachIndexed { idx, option ->
                                val isSelected = selectedOptionIndex == idx
                                val borderClr = if (isSelected) CyberTeal else SlateBorder
                                val cardBg = if (isSelected) CyberTeal.copy(0.15f) else SlateCard

                                Card(
                                    onClick = { if (!challengeSubmitted) selectedOptionIndex = idx },
                                    colors = CardDefaults.cardColors(containerColor = cardBg),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .border(1.dp, borderClr, RoundedCornerShape(10.dp))
                                ) {
                                    Text(
                                        text = option,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    if (!challengeSubmitted) {
                        Button(
                            onClick = { selectedOptionIndex?.let { viewModel.submitChallengeAnswer(it) } },
                            enabled = selectedOptionIndex != null,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberTeal, contentColor = SlateDark),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Deploy Secure Patch", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Patch Output Console
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (challengeCorrect) CyberGreen.copy(0.1f) else CyberRed.copy(0.1f))
                                .border(1.dp, if (challengeCorrect) CyberGreen else CyberRed, RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (challengeCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                                    contentDescription = "Status",
                                    tint = if (challengeCorrect) CyberGreen else CyberRed
                                )
                                Text(
                                    text = if (challengeCorrect) "SECURE AUDIT SUCCESS" else "SECURE AUDIT FAILURE",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = if (challengeCorrect) CyberGreen else CyberRed
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = challengeOutputMessage,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. COMMUNITY FORUM & MENTORSHIP
// ==========================================
@Composable
fun ForumScreenContent(
    viewModel: AcademyViewModel
) {
    val scope = rememberCoroutineScope()
    val posts by viewModel.forumPosts.collectAsState()
    var showNewPostDialog by remember { mutableStateOf(false) }
    var selectedPostForReplies by remember { mutableStateOf<ForumPostEntity?>(null) }

    // State for creating post
    var postTitle by remember { mutableStateOf("") }
    var postContent by remember { mutableStateOf("") }
    var postTag by remember { mutableStateOf("CompTIA Security+") }

    // State for replies
    var replyText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (selectedPostForReplies == null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Expert Mentorship Forum",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Collaborate with industry gurus",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                FloatingActionButton(
                    onClick = { showNewPostDialog = true },
                    containerColor = CyberTeal,
                    contentColor = SlateDark,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add post")
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(posts) { post ->
                    Card(
                        onClick = { selectedPostForReplies = post },
                        colors = CardDefaults.cardColors(containerColor = SlateCard),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(CyberTeal.copy(0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            post.author.first().toString().uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = CyberTeal,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Text(
                                        text = "${post.author} (${post.role})",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = CyberTeal,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Badge(containerColor = SlateBorder) {
                                    Text(
                                        post.tag,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextSecondary
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = post.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = post.content,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Replies icon",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Inspect answers / collaborate",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = CyberBlue,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Detailed discussion Q&A view
            val post = selectedPostForReplies!!

            // Parse replies safely
            val repliesList = remember(post.repliesJson) {
                viewModel.parseReplies(post.repliesJson)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { selectedPostForReplies = null },
                    modifier = Modifier
                        .background(SlateCard, CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Text(
                    text = "Discussion Lounge",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Box(modifier = Modifier.size(36.dp))
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // Original Post Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateCard),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${post.author} (${post.role})",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = CyberTeal,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Badge(containerColor = SlateDark) {
                                    Text(
                                        post.tag,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = post.title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = post.content,
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "ANSWERS & MENTORSHIP (${repliesList.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CyberTeal
                        ),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(repliesList) { reply ->
                    val isMentor = reply.role == "Mentor" || reply.role == "Expert"
                    val borderClr = if (isMentor) CyberTeal else SlateBorder
                    val backgroundClr = if (isMentor) CyberTeal.copy(0.05f) else SlateCard

                    Card(
                        colors = CardDefaults.cardColors(containerColor = backgroundClr),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, borderClr, RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${reply.author} (${reply.role})",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isMentor) CyberTeal else TextSecondary
                                    )
                                )
                                if (isMentor) {
                                    Badge(containerColor = CyberTeal) {
                                        Text(
                                            "MENTOR REAFFIRMED",
                                            color = SlateDark,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = reply.content,
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                            )
                        }
                    }
                }
            }

            // Quick reply input box
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    placeholder = { Text("Contribute your solution...", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SlateCard,
                        unfocusedContainerColor = SlateCard,
                        focusedBorderColor = CyberTeal,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                IconButton(
                    onClick = {
                        if (replyText.isNotBlank()) {
                            viewModel.addReply(post.id, replyText)
                            replyText = ""
                            // Update our local detail view state with the newly loaded db data
                            scope.launch {
                                val updatedPost = viewModel.repository.forumPosts.first().find { it.id == post.id }
                                if (updatedPost != null) {
                                    selectedPostForReplies = updatedPost
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .background(CyberTeal, CircleShape)
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Reply",
                        tint = SlateDark
                    )
                }
            }
        }
    }

    // New Post Dialog
    if (showNewPostDialog) {
        AlertDialog(
            onDismissRequest = { showNewPostDialog = false },
            title = { Text("Post a Mentorship Query", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = postTitle,
                        onValueChange = { postTitle = it },
                        label = { Text("Topic Title") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = postContent,
                        onValueChange = { postContent = it },
                        label = { Text("Detailed Question") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Tag choice
                    Text("Select Certification Tag:", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("CompTIA Security+", "CISSP", "Networking").forEach { tag ->
                            val isSelected = tag == postTag
                            Button(
                                onClick = { postTag = tag },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) CyberTeal else SlateCard,
                                    contentColor = if (isSelected) SlateDark else TextPrimary
                                )
                            ) {
                                Text(tag.replace("CompTIA ", ""), fontSize = 10.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (postTitle.isNotBlank() && postContent.isNotBlank()) {
                            viewModel.createPost(postTitle, postContent, postTag)
                            postTitle = ""
                            postContent = ""
                            showNewPostDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberTeal, contentColor = SlateDark)
                ) {
                    Text("Publish Thread", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewPostDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SlateDark
        )
    }
}

// ==========================================
// 5. AI-POWERED MOCK INTERVIEW SIMULATOR
// ==========================================
@Composable
fun InterviewScreenContent(
    viewModel: AcademyViewModel
) {
    val isRunning by viewModel.isInterviewRunning.collectAsState()
    val type by viewModel.interviewType.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val currentQuestion by viewModel.currentQuestion.collectAsState()
    val feedback by viewModel.interviewFeedback.collectAsState()
    val lastScore by viewModel.lastScore.collectAsState()
    val finished by viewModel.interviewFinished.collectAsState()
    val pastSessions by viewModel.interviewSessions.collectAsState()

    var answerText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!isRunning) {
            // Main menu selection / previous stats
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "AI Interview Simulator",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "Realistic technical & behavioral drilling using Gemini AI",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    onClick = { viewModel.startInterview("Technical") },
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, CyberTeal.copy(0.4f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Technical",
                            tint = CyberTeal,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Technical Drill",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "NIST, firewalls, threat analysis, cryptosystems",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            ),
                            fontSize = 11.sp
                        )
                    }
                }

                Card(
                    onClick = { viewModel.startInterview("Behavioral") },
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, CyberBlue.copy(0.4f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Behavioral",
                            tint = CyberBlue,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Behavioral Drill",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Incident management, teams, ethics, compromises",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            ),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Text(
                text = "PAST SESSION HISTORY",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = CyberTeal
                ),
                modifier = Modifier.padding(top = 12.dp)
            )

            if (pastSessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(SlateCard, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No interview history yet. Complete your first mock drill above!",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(pastSessions) { session ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SlateCard),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        "${session.type} Mock Interview",
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = session.feedback,
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                        maxLines = 1
                                    )
                                }
                                Badge(containerColor = if (session.score >= 80) CyberGreen else CyberOrange) {
                                    Text(
                                        "${session.score}/100",
                                        fontWeight = FontWeight.Bold,
                                        color = SlateDark,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Live active chat interface
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { viewModel.cancelInterview() },
                    modifier = Modifier
                        .background(SlateCard, CircleShape)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = TextPrimary
                    )
                }
                Text(
                    text = "AI Interrogator ($type)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Box(modifier = Modifier.size(36.dp))
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Show feedback from previous answer if present
                if (feedback.isNotBlank()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SlateCard),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CyberTeal.copy(0.4f), RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "PREVIOUS RESPONSE FEEDBACK",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = CyberTeal
                                        )
                                    )
                                    lastScore?.let { score ->
                                        Badge(containerColor = if (score >= 80) CyberGreen else CyberOrange) {
                                            Text(
                                                "Score: $score/100",
                                                fontWeight = FontWeight.Bold,
                                                color = SlateDark,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = feedback,
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                                )
                            }
                        }
                    }
                }

                // AI Active Question Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateDark),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CyberBlue, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "INTERVIEWER QUESTION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CyberBlue
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (isGenerating && currentQuestion.isBlank()) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CyberBlue)
                                    Text("Generating secure prompt questions...", color = TextSecondary, fontSize = 12.sp)
                                }
                            } else {
                                Text(
                                    text = currentQuestion,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 22.sp,
                                        color = TextPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (finished) {
                Button(
                    onClick = { viewModel.saveAndFinishInterview() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = SlateDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Complete Mock Session & Review Results", fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = answerText,
                        onValueChange = { answerText = it },
                        placeholder = { Text("Formulate your security response...", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SlateCard,
                            unfocusedContainerColor = SlateCard,
                            focusedBorderColor = CyberTeal,
                            unfocusedBorderColor = SlateBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isGenerating
                    )

                    IconButton(
                        onClick = {
                            if (answerText.isNotBlank()) {
                                viewModel.submitInterviewAnswer(answerText)
                                answerText = ""
                            }
                        },
                        enabled = !isGenerating && answerText.isNotBlank(),
                        modifier = Modifier
                            .background(if (isGenerating || answerText.isBlank()) SlateCard else CyberTeal, CircleShape)
                            .size(48.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = CyberTeal)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Submit Answer",
                                tint = if (answerText.isBlank()) TextSecondary else SlateDark
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. STUDY REMINDERS & GROUP RECOMMENDATIONS
// ==========================================
@Composable
fun RemindersScreenContent(
    viewModel: AcademyViewModel
) {
    val reminders by viewModel.studyReminders.collectAsState()
    val groups by viewModel.studyGroups.collectAsState()

    var reminderTitle by remember { mutableStateOf("") }
    var reminderTimeText by remember { mutableStateOf("18:00") }
    var reminderDaysText by remember { mutableStateOf("Mon, Wed, Fri") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Study Optimization & Schedules",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = "Set daily triggers & join peer collaboration circles",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
        }

        // --- SCHEDULER CONTROLS ---
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CREATE REMINDER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = CyberTeal
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = reminderTitle,
                        onValueChange = { reminderTitle = it },
                        placeholder = { Text("e.g. CompTIA Syllabus Review", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (reminderTitle.isNotBlank()) {
                                viewModel.addReminder(reminderTitle, reminderTimeText, reminderDaysText)
                                reminderTitle = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberTeal, contentColor = SlateDark)
                    ) {
                        Text("Add", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = reminderTimeText,
                        onValueChange = { reminderTimeText = it },
                        label = { Text("Time (24h)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = reminderDaysText,
                        onValueChange = { reminderDaysText = it },
                        label = { Text("Days") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1.5f),
                        singleLine = true
                    )
                }
            }
        }

        // Reminders list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "ACTIVE STUDY REMINDERS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = CyberTeal
                    )
                )
            }

            if (reminders.isEmpty()) {
                item {
                    Text(
                        "No reminders set. Add one above to structure your cyber schedules!",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(reminders) { reminder ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = reminder.title,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${reminder.daysText} at ${reminder.timeText}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                            IconButton(onClick = { viewModel.deleteReminder(reminder.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = CyberRed
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "RECOMMENDED STUDY GROUPS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = CyberTeal
                    ),
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(groups) { group ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = group.name,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = group.description,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${group.memberCount} members active",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = CyberTeal,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Button(
                            onClick = { viewModel.toggleStudyGroupJoin(group) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (group.isJoined) CyberGreen else CyberTeal,
                                contentColor = SlateDark
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (group.isJoined) "Joined" else "Join Room",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. JOB BOARD SCREEN
// ==========================================
@Composable
fun JobsScreenContent(
    viewModel: AcademyViewModel
) {
    val jobs by viewModel.dbJobListings.collectAsState()
    val candidates by viewModel.dbCandidates.collectAsState()
    val userProfile by viewModel.currentUserCandidate.collectAsState()
    val allApplications by viewModel.allJobApplications.collectAsState()

    var activeSubTab by remember { mutableStateOf("FindJobs") } // "FindJobs", "MyProfile", "RecruiterPortal"

    // Search & filter states
    var jobQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("All") }
    var selectedJobDetail by remember { mutableStateOf<com.example.data.database.JobListingEntity?>(null) }
    
    var candidateQuery by remember { mutableStateOf("") }

    // Recruiter form states
    var isPostingJob by remember { mutableStateOf(false) }
    var postTitle by remember { mutableStateOf("") }
    var postCompany by remember { mutableStateOf("") }
    var postLocation by remember { mutableStateOf("") }
    var postType by remember { mutableStateOf("Full-time") }
    var postSalary by remember { mutableStateOf("") }
    var postRecruiter by remember { mutableStateOf("") }
    var postDescription by remember { mutableStateOf("") }
    var postRequirements by remember { mutableStateOf("") }
    var showJobPostedSuccess by remember { mutableStateOf(false) }

    // User profile states (initialized/synced from DB state)
    var profileName by remember { mutableStateOf("") }
    var profileTitle by remember { mutableStateOf("") }
    var profileSkills by remember { mutableStateOf("") }
    var profileCertifications by remember { mutableStateOf("") }
    var profileExperience by remember { mutableStateOf("") }
    var profileEmail by remember { mutableStateOf("") }
    var showProfileSavedSuccess by remember { mutableStateOf(false) }

    var selectedPostedJobForApplicants by remember { mutableStateOf<com.example.data.database.JobListingEntity?>(null) }

    // Sync profile states on first load or when DB entity updates
    LaunchedEffect(userProfile) {
        userProfile?.let {
            profileName = it.name
            profileTitle = it.title
            profileSkills = it.skills
            profileCertifications = it.certifications
            profileExperience = it.experienceYears.toString()
            profileEmail = it.email
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- CAREERS NAVIGATION HEADER ---
        if (selectedJobDetail == null && selectedPostedJobForApplicants == null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlateCard, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabOptions = listOf(
                    Triple("FindJobs", "Find Jobs", Icons.Default.Search),
                    Triple("MyProfile", "My Profile", Icons.Default.Person),
                    Triple("RecruiterPortal", "Recruiter Desk", Icons.Default.AccountCircle)
                )
                tabOptions.forEach { (tabId, label, icon) ->
                    val isSelected = activeSubTab == tabId
                    Button(
                        onClick = { activeSubTab = tabId },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) CyberTeal else Color.Transparent,
                            contentColor = if (isSelected) SlateDark else TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        when {
            // --- DRILLDOWN: JOB DETAIL VIEW ---
            selectedJobDetail != null -> {
                val job = selectedJobDetail!!
                val hasApplied = allApplications.any { 
                    it.jobId == job.id && it.applicantEmail == (userProfile?.email ?: "thawfiks@gmail.com") 
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { selectedJobDetail = null },
                        modifier = Modifier
                            .background(SlateCard, CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Text(
                        text = "POSITION SPECIFICATIONS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Box(modifier = Modifier.size(36.dp))
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SlateCard),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = job.company,
                                    color = CyberTeal,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = job.title,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${job.location} | ${job.type} | ${job.salary}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "INCIDENT COMMANDER SUMMARY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTeal
                                )
                            )
                            Text(
                                text = job.description,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 20.sp,
                                    color = TextPrimary
                                )
                            )
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "TACTICAL CAPABILITIES REQUIRED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTeal
                                )
                            )
                            job.requirements.split(",").map { it.trim() }.forEach { req ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Requirement bullet",
                                        tint = CyberTeal,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = req,
                                        style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SlateDark),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(CyberBlue.copy(0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Recruiter profile",
                                        tint = CyberBlue
                                    )
                                }
                                Column {
                                    Text(
                                        text = job.recruiterName,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Company Security Recruiter. Ready to evaluate your verified skills and certifications.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                if (!hasApplied) {
                                    val appName = userProfile?.name ?: "Cyber Cadet"
                                    val appEmail = userProfile?.email ?: "thawfiks@gmail.com"
                                    val appSkills = userProfile?.skills ?: "Log Correlation, TCP/IP basics"
                                    val appCerts = userProfile?.certifications ?: "Learning Cyber Defender"
                                    viewModel.applyForJob(
                                        jobId = job.id,
                                        applicantName = appName,
                                        applicantEmail = appEmail,
                                        applicantSkills = appSkills,
                                        applicantCertifications = appCerts
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasApplied) CyberGreen else CyberTeal,
                                contentColor = SlateDark
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = if (hasApplied) "APPLICATION SECURED (In Review)" else "Apply with Academy Profile",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            // --- DRILLDOWN: POSTED JOB APPLICANTS VIEW ---
            selectedPostedJobForApplicants != null -> {
                val job = selectedPostedJobForApplicants!!
                val applicants = allApplications.filter { it.jobId == job.id }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { selectedPostedJobForApplicants = null },
                        modifier = Modifier
                            .background(SlateCard, CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Text(
                        text = "APPLICANTS PROTOCOL",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Box(modifier = Modifier.size(36.dp))
                }

                Text(
                    text = "Applicants for: ${job.title} (${job.company})",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = CyberTeal
                    )
                )

                if (applicants.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "No applicants",
                                tint = TextSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "No applications received yet.",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(applicants) { app ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SlateCard),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = app.applicantName,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                        )
                                        Text(
                                            text = "Email: ${app.applicantEmail}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = CyberTeal)
                                        )
                                    }
                                    
                                    Divider(color = SlateBorder)
                                    
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = "SKILLS:",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = CyberOrange
                                                )
                                            )
                                            Text(
                                                text = app.applicantSkills,
                                                style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                                            )
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = "CERTS:",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = CyberTeal
                                                )
                                            )
                                            Text(
                                                text = app.applicantCertifications,
                                                style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- TAB 1: FIND JOBS ---
            activeSubTab == "FindJobs" -> {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Cybersecurity Recruitment Corridor",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Connect directly with leading agencies and defense contractors",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                // Search field
                OutlinedTextField(
                    value = jobQuery,
                    onValueChange = { jobQuery = it },
                    placeholder = { Text("Search by title, company, requirements...", color = TextSecondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("job_search_input"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search icon",
                            tint = TextSecondary
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SlateCard,
                        unfocusedContainerColor = SlateCard,
                        focusedBorderColor = CyberTeal,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Filter Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("All", "Full-time", "Contract", "Remote")
                    filters.forEach { filter ->
                        val isSelected = selectedTypeFilter == filter
                        Button(
                            onClick = { selectedTypeFilter = filter },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) CyberTeal.copy(0.15f) else SlateCard,
                                contentColor = if (isSelected) CyberTeal else TextSecondary
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .border(1.dp, if (isSelected) CyberTeal else SlateBorder, RoundedCornerShape(16.dp)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = filter,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                // Filter jobs
                val filteredJobs = jobs.filter { job ->
                    val queryMatch = job.title.contains(jobQuery, ignoreCase = true) ||
                            job.company.contains(jobQuery, ignoreCase = true) ||
                            job.requirements.contains(jobQuery, ignoreCase = true) ||
                            job.description.contains(jobQuery, ignoreCase = true)
                    val filterMatch = selectedTypeFilter == "All" || job.type.equals(selectedTypeFilter, ignoreCase = true)
                    queryMatch && filterMatch
                }

                if (filteredJobs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Empty",
                                tint = TextSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                "No cybersecurity openings match your query.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        items(filteredJobs) { job ->
                            val hasApplied = allApplications.any { 
                                it.jobId == job.id && it.applicantEmail == (userProfile?.email ?: "thawfiks@gmail.com") 
                            }
                            Card(
                                onClick = { selectedJobDetail = job },
                                colors = CardDefaults.cardColors(containerColor = SlateCard),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, if (hasApplied) CyberGreen else SlateBorder, RoundedCornerShape(16.dp))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = job.company,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = CyberTeal,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Badge(containerColor = SlateDark) {
                                            Text(
                                                text = job.type,
                                                color = TextSecondary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = job.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${job.location} | ${job.salary}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (hasApplied) Icons.Default.CheckCircle else Icons.Default.Email,
                                            contentDescription = "Applied Icon",
                                            tint = if (hasApplied) CyberGreen else CyberBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = if (hasApplied) "Application Sent to Recruiter" else "Click for details and apply",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = if (hasApplied) CyberGreen else CyberBlue,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- TAB 2: MY PROFILE ---
            activeSubTab == "MyProfile" -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Your Secure Candidate Profile",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Publishing your credentials makes you searchable by active cybersecurity recruiters",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }
                    }

                    // Profile Summary Card Preview
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SlateDark),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CyberTeal.copy(0.4f), RoundedCornerShape(16.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(CyberTeal.copy(0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Cadet avatar",
                                        tint = CyberTeal,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (profileName.isBlank()) "Cadet Profile Pending" else profileName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    )
                                    Text(
                                        text = if (profileTitle.isBlank()) "No title set yet" else profileTitle,
                                        style = MaterialTheme.typography.bodySmall.copy(color = CyberTeal)
                                    )
                                    Text(
                                        text = "Email: ${if (profileEmail.isBlank()) "None set" else profileEmail}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${profileExperience.toIntOrNull() ?: 0} Yrs",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = CyberOrange
                                        )
                                    )
                                    Text(
                                        text = "EXP",
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                                    )
                                }
                            }
                        }
                    }

                    // Form inputs
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Name
                            OutlinedTextField(
                                value = profileName,
                                onValueChange = { profileName = it },
                                label = { Text("Full Name", color = TextSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = SlateCard,
                                    unfocusedContainerColor = SlateCard,
                                    focusedBorderColor = CyberTeal,
                                    unfocusedBorderColor = SlateBorder
                                ),
                                singleLine = true
                            )

                            // Title
                            OutlinedTextField(
                                value = profileTitle,
                                onValueChange = { profileTitle = it },
                                label = { Text("Target Role (e.g. SOC Analyst, Security Engineer)", color = TextSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = SlateCard,
                                    unfocusedContainerColor = SlateCard,
                                    focusedBorderColor = CyberTeal,
                                    unfocusedBorderColor = SlateBorder
                                ),
                                singleLine = true
                            )

                            // Skills
                            OutlinedTextField(
                                value = profileSkills,
                                onValueChange = { profileSkills = it },
                                label = { Text("Technical Skills (comma separated)", color = TextSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = SlateCard,
                                    unfocusedContainerColor = SlateCard,
                                    focusedBorderColor = CyberTeal,
                                    unfocusedBorderColor = SlateBorder
                                )
                            )

                            // Certifications
                            OutlinedTextField(
                                value = profileCertifications,
                                onValueChange = { profileCertifications = it },
                                label = { Text("Certifications (comma separated)", color = TextSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = SlateCard,
                                    unfocusedContainerColor = SlateCard,
                                    focusedBorderColor = CyberTeal,
                                    unfocusedBorderColor = SlateBorder
                                )
                            )

                            // Experience years & email
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = profileExperience,
                                    onValueChange = { profileExperience = it },
                                    label = { Text("Experience (Yrs)", color = TextSecondary) },
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedContainerColor = SlateCard,
                                        unfocusedContainerColor = SlateCard,
                                        focusedBorderColor = CyberTeal,
                                        unfocusedBorderColor = SlateBorder
                                    ),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = profileEmail,
                                    onValueChange = { profileEmail = it },
                                    label = { Text("Contact Email", color = TextSecondary) },
                                    modifier = Modifier.weight(2f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedContainerColor = SlateCard,
                                        unfocusedContainerColor = SlateCard,
                                        focusedBorderColor = CyberTeal,
                                        unfocusedBorderColor = SlateBorder
                                    ),
                                    singleLine = true
                                )
                            }
                        }
                    }

                    if (showProfileSavedSuccess) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberGreen.copy(0.1f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Success",
                                        tint = CyberGreen
                                    )
                                    Text(
                                        text = "Profile Published on Secure Registry! +10 XP",
                                        fontWeight = FontWeight.Bold,
                                        color = CyberGreen,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                if (profileName.isNotBlank() && profileEmail.isNotBlank()) {
                                    viewModel.updateCandidateProfile(
                                        name = profileName,
                                        title = profileTitle,
                                        skills = profileSkills,
                                        certifications = profileCertifications,
                                        experienceYears = profileExperience.toIntOrNull() ?: 0,
                                        email = profileEmail,
                                        avatarSeed = profileName.take(3)
                                    )
                                    showProfileSavedSuccess = true
                                }
                            },
                            enabled = profileName.isNotBlank() && profileEmail.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberTeal, contentColor = SlateDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Publish and Save Profile", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // --- TAB 3: RECRUITER PORTAL ---
            activeSubTab == "RecruiterPortal" -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Cyber Command Recruiter Bureau",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Query global candidate rosters and launch targeted security roles",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }
                    }

                    // Form toggle button or Form itself
                    item {
                        if (!isPostingJob) {
                            Button(
                                onClick = { isPostingJob = true },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberTeal, contentColor = SlateDark),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                                    Text("Post a New Cybersecurity Opening", fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SlateCard),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, CyberTeal.copy(0.4f), RoundedCornerShape(16.dp))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "NEW RECRUITMENT PROTOCOL",
                                        fontWeight = FontWeight.Bold,
                                        color = CyberTeal,
                                        style = MaterialTheme.typography.labelSmall
                                    )

                                    OutlinedTextField(
                                        value = postTitle,
                                        onValueChange = { postTitle = it },
                                        label = { Text("Job Title (e.g., SOC Incident Responder)", color = TextSecondary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = CyberTeal,
                                            unfocusedBorderColor = SlateBorder
                                        ),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = postCompany,
                                        onValueChange = { postCompany = it },
                                        label = { Text("Company Name", color = TextSecondary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = CyberTeal,
                                            unfocusedBorderColor = SlateBorder
                                        ),
                                        singleLine = true
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        OutlinedTextField(
                                            value = postLocation,
                                            onValueChange = { postLocation = it },
                                            label = { Text("Location (e.g. Remote)", color = TextSecondary) },
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary,
                                                focusedBorderColor = CyberTeal,
                                                unfocusedBorderColor = SlateBorder
                                            ),
                                            singleLine = true
                                        )

                                        OutlinedTextField(
                                            value = postSalary,
                                            onValueChange = { postSalary = it },
                                            label = { Text("Salary Range", color = TextSecondary) },
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary,
                                                focusedBorderColor = CyberTeal,
                                                unfocusedBorderColor = SlateBorder
                                            ),
                                            singleLine = true
                                        )
                                    }

                                    OutlinedTextField(
                                        value = postRecruiter,
                                        onValueChange = { postRecruiter = it },
                                        label = { Text("Recruiter Contact Name", color = TextSecondary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = CyberTeal,
                                            unfocusedBorderColor = SlateBorder
                                        ),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = postRequirements,
                                        onValueChange = { postRequirements = it },
                                        label = { Text("Mandatory Skills & Certs (comma-separated)", color = TextSecondary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = CyberTeal,
                                            unfocusedBorderColor = SlateBorder
                                        )
                                    )

                                    OutlinedTextField(
                                        value = postDescription,
                                        onValueChange = { postDescription = it },
                                        label = { Text("Job Description & Mission Parameters", color = TextSecondary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = CyberTeal,
                                            unfocusedBorderColor = SlateBorder
                                        )
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Button(
                                            onClick = { isPostingJob = false },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = TextSecondary),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Cancel")
                                        }

                                        Button(
                                            onClick = {
                                                if (postTitle.isNotBlank() && postCompany.isNotBlank()) {
                                                    viewModel.postJobListing(
                                                        title = postTitle,
                                                        company = postCompany,
                                                        location = postLocation,
                                                        type = postType,
                                                        salary = postSalary,
                                                        recruiterName = postRecruiter,
                                                        description = postDescription,
                                                        requirements = postRequirements
                                                    )
                                                    showJobPostedSuccess = true
                                                    isPostingJob = false
                                                    // Reset fields
                                                    postTitle = ""
                                                    postCompany = ""
                                                    postLocation = ""
                                                    postSalary = ""
                                                    postRecruiter = ""
                                                    postRequirements = ""
                                                    postDescription = ""
                                                }
                                            },
                                            enabled = postTitle.isNotBlank() && postCompany.isNotBlank(),
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberTeal, contentColor = SlateDark),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Publish Job", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (showJobPostedSuccess) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberGreen.copy(0.1f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Success",
                                        tint = CyberGreen
                                    )
                                    Text(
                                        text = "Tactical Role Published successfully!",
                                        fontWeight = FontWeight.Bold,
                                        color = CyberGreen,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    // --- RECRUITER SUB-SECTION: CANDIDATE SEARCH ---
                    item {
                        Divider(color = SlateBorder)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tactical Candidate Search Protocol",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = candidateQuery,
                            onValueChange = { candidateQuery = it },
                            placeholder = { Text("Filter candidates by skills/certs...", color = TextSecondary) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("candidate_search_input"),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search icon",
                                    tint = TextSecondary
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = SlateCard,
                                unfocusedContainerColor = SlateCard,
                                focusedBorderColor = CyberTeal,
                                unfocusedBorderColor = SlateBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    val filteredCandidates = candidates.filter { cand ->
                        candidateQuery.isBlank() || 
                                cand.skills.contains(candidateQuery, ignoreCase = true) || 
                                cand.certifications.contains(candidateQuery, ignoreCase = true) || 
                                cand.title.contains(candidateQuery, ignoreCase = true) || 
                                cand.name.contains(candidateQuery, ignoreCase = true)
                    }

                    if (filteredCandidates.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No candidates match your query parameters.", color = TextSecondary)
                            }
                        }
                    } else {
                        items(filteredCandidates) { cand ->
                            var contactSentByRecruiter by remember { mutableStateOf(false) }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SlateCard),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = cand.name,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                            )
                                            Text(
                                                text = cand.title,
                                                style = MaterialTheme.typography.bodySmall.copy(color = CyberTeal)
                                            )
                                        }
                                        Badge(containerColor = SlateDark) {
                                            Text(
                                                text = "${cand.experienceYears} Yrs Exp",
                                                color = CyberOrange,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = "Skills:",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextSecondary
                                                )
                                            )
                                            Text(
                                                text = cand.skills,
                                                style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                                            )
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = "Certs:",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextSecondary
                                                )
                                            )
                                            Text(
                                                text = cand.certifications,
                                                style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                                            )
                                        }
                                    }

                                    Divider(color = SlateBorder)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = cand.email,
                                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                        )

                                        Button(
                                            onClick = { contactSentByRecruiter = true },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (contactSentByRecruiter) CyberGreen.copy(0.15f) else CyberBlue,
                                                contentColor = if (contactSentByRecruiter) CyberGreen else SlateDark
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(32.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp)
                                        ) {
                                            Text(
                                                text = if (contactSentByRecruiter) "Inquiry Dispatched" else "Acquire Contact",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- RECRUITER SUB-SECTION: MANAGE POSTED JOBS & APPLICANTS ---
                    item {
                        Divider(color = SlateBorder)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Verify Applications For Your Roles",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (jobs.isEmpty()) {
                        item {
                            Text(
                                "No active roles published on board.",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    } else {
                        items(jobs) { job ->
                            val jobApps = allApplications.filter { it.jobId == job.id }
                            Card(
                                onClick = { selectedPostedJobForApplicants = job },
                                colors = CardDefaults.cardColors(containerColor = SlateCard),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = job.title,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "${job.company} | ${job.location}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                        )
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .background(CyberTeal.copy(0.15f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "${jobApps.size} Applicants",
                                            fontWeight = FontWeight.Bold,
                                            color = CyberTeal,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
