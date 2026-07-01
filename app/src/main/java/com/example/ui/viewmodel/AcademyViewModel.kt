package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.CyberAcademyApplication
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiPart
import com.example.data.database.UserProgressEntity
import com.example.data.database.ForumPostEntity
import com.example.data.database.StudyReminderEntity
import com.example.data.database.StudyGroupEntity
import com.example.data.database.InterviewSessionEntity
import com.example.data.repository.AcademyRepository
import com.example.data.repository.Lesson
import com.example.data.repository.Simulation
import com.example.data.repository.CodingChallenge
import com.example.data.repository.JobListing
import com.example.data.repository.Reply
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AcademyScreen {
    Home,
    ThreatSim,
    Coding,
    Forum,
    Interview,
    Reminders,
    Jobs
}

class AcademyViewModel(
    application: Application,
    val repository: AcademyRepository
) : AndroidViewModel(application) {

    // --- Core UI State Flows ---
    private val _currentScreen = MutableStateFlow(AcademyScreen.Home)
    val currentScreen: StateFlow<AcademyScreen> = _currentScreen

    val userProgress: StateFlow<UserProgressEntity?> = repository.userProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProgressEntity())

    val forumPosts: StateFlow<List<ForumPostEntity>> = repository.forumPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studyReminders: StateFlow<List<StudyReminderEntity>> = repository.studyReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studyGroups: StateFlow<List<StudyGroupEntity>> = repository.studyGroups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val interviewSessions: StateFlow<List<InterviewSessionEntity>> = repository.interviewSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dbJobListings: StateFlow<List<com.example.data.database.JobListingEntity>> = repository.jobListings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dbCandidates: StateFlow<List<com.example.data.database.CandidateProfileEntity>> = repository.candidates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUserCandidate: StateFlow<com.example.data.database.CandidateProfileEntity?> = repository.currentUserCandidate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allJobApplications: StateFlow<List<com.example.data.database.JobApplicationEntity>> = repository.allJobApplications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Sub-View Selections ---
    private val _selectedPath = MutableStateFlow("CompTIA Security+")
    val selectedPath: StateFlow<String> = _selectedPath

    private val _selectedLesson = MutableStateFlow<Lesson?>(null)
    val selectedLesson: StateFlow<Lesson?> = _selectedLesson

    private val _lessonQuizResult = MutableStateFlow<Boolean?>(null) // true if correct, false if incorrect, null if unsubmitted
    val lessonQuizResult: StateFlow<Boolean?> = _lessonQuizResult

    private val _lessonExplanation = MutableStateFlow<String>("")
    val lessonExplanation: StateFlow<String> = _lessonExplanation

    // --- Threat Simulation State ---
    private val _selectedSimulation = MutableStateFlow<Simulation?>(null)
    val selectedSimulation: StateFlow<Simulation?> = _selectedSimulation

    private val _consoleLogs = MutableStateFlow<List<String>>(emptyList())
    val consoleLogs: StateFlow<List<String>> = _consoleLogs

    private val _simulationCompleted = MutableStateFlow(false)
    val simulationCompleted: StateFlow<Boolean> = _simulationCompleted

    // --- Coding Challenge State ---
    private val _selectedChallenge = MutableStateFlow<CodingChallenge?>(null)
    val selectedChallenge: StateFlow<CodingChallenge?> = _selectedChallenge

    private val _challengeSubmitted = MutableStateFlow(false)
    val challengeSubmitted: StateFlow<Boolean> = _challengeSubmitted

    private val _challengeCorrect = MutableStateFlow(false)
    val challengeCorrect: StateFlow<Boolean> = _challengeCorrect

    private val _challengeOutputMessage = MutableStateFlow("")
    val challengeOutputMessage: StateFlow<String> = _challengeOutputMessage

    // --- Mock Interview State ---
    private val _isInterviewRunning = MutableStateFlow(false)
    val isInterviewRunning: StateFlow<Boolean> = _isInterviewRunning

    private val _interviewType = MutableStateFlow("Technical")
    val interviewType: StateFlow<String> = _interviewType

    private val _currentQuestion = MutableStateFlow("")
    val currentQuestion: StateFlow<String> = _currentQuestion

    private val _interviewFeedback = MutableStateFlow("")
    val interviewFeedback: StateFlow<String> = _interviewFeedback

    private val _lastScore = MutableStateFlow<Int?>(null)
    val lastScore: StateFlow<Int?> = _lastScore

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    private val _interviewFinished = MutableStateFlow(false)
    val interviewFinished: StateFlow<Boolean> = _interviewFinished

    private val interviewTranscript = mutableListOf<GeminiContent>()

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val repliesAdapter = moshi.adapter<List<Reply>>(
        Types.newParameterizedType(List::class.java, Reply::class.java)
    )

    init {
        viewModelScope.launch {
            repository.populateDefaultsIfNeeded()
        }
    }

    // --- Basic Navigation Actions ---
    fun setScreen(screen: AcademyScreen) {
        _currentScreen.value = screen
        // Reset drilldown states when switching main tabs
        if (screen != AcademyScreen.Home) {
            _selectedLesson.value = null
        }
        if (screen != AcademyScreen.ThreatSim) {
            _selectedSimulation.value = null
        }
        if (screen != AcademyScreen.Coding) {
            _selectedChallenge.value = null
        }
    }

    fun selectPath(path: String) {
        _selectedPath.value = path
        _selectedLesson.value = null
    }

    fun selectLesson(lesson: Lesson?) {
        _selectedLesson.value = lesson
        _lessonQuizResult.value = null
        _lessonExplanation.value = ""
    }

    // --- Gamified Actions ---
    fun submitLessonQuiz(selectedOptionIndex: Int) {
        val lesson = _selectedLesson.value ?: return
        val isCorrect = selectedOptionIndex == lesson.quizAnswerIndex
        _lessonQuizResult.value = isCorrect
        _lessonExplanation.value = lesson.quizExplanation

        if (isCorrect) {
            viewModelScope.launch {
                repository.completeLesson(lesson.id, lesson.points)
            }
        }
    }

    // --- Threat Simulation Action Block ---
    fun selectSimulation(simulation: Simulation?) {
        _selectedSimulation.value = simulation
        _simulationCompleted.value = false
        _consoleLogs.value = simulation?.initialConsoleLogs ?: emptyList()
    }

    fun executeSimCommand(commandString: String) {
        val sim = _selectedSimulation.value ?: return
        val trimmedCmd = commandString.trim()
        val currentLogs = _consoleLogs.value.toMutableList()
        currentLogs.add("> $trimmedCmd")

        if (sim.id == "sim_ddos") {
            when {
                trimmedCmd == "/status" -> {
                    currentLogs.add("CPU LOAD: 94% | MEMORY: 82%")
                    currentLogs.add("MALICIOUS FLOOD ACTIVE: 192.168.4.150 sending 1200 conn/sec.")
                    currentLogs.add("ALERT: Please block the offending IP address with: /block <IP>")
                }
                trimmedCmd == sim.targetSolution -> {
                    currentLogs.add("FIREWALL: Executing block rule for 192.168.4.150...")
                    currentLogs.add("SUCCESS: Trapped 192.168.4.150 connection pool. Dropping packets.")
                    currentLogs.add("SYSTEM: Load stabilizing. CPU: 22%. API Gateway responsive.")
                    currentLogs.add("VICTORY: DDoS Threat successfully mitigated!")
                    _simulationCompleted.value = true
                    viewModelScope.launch {
                        repository.completeSimulation(sim.id, sim.points)
                    }
                }
                trimmedCmd.startsWith("/block") -> {
                    currentLogs.add("FIREWALL ERROR: Unknown or invalid IP specified. Double check log targets!")
                }
                else -> {
                    currentLogs.add("SHELL: Command not recognized. Try `/status` or `/block <IP>`")
                }
            }
        } else if (sim.id == "sim_ransomware") {
            when {
                trimmedCmd == "/ps" -> {
                    currentLogs.add("ACTIVE PROCESSES:")
                    currentLogs.add("PID 102  - system_d (L1)")
                    currentLogs.add("PID 1450 - apache2 (web server)")
                    currentLogs.add("PID 4982 - cryptor.exe (UNAUTHORIZED - ENCRYPTING SHARED FILES)")
                    currentLogs.add("PID 883  - cron_job (scheduler)")
                    currentLogs.add("Type /kill <PID> to terminate the rogue executable.")
                }
                trimmedCmd == sim.targetSolution -> {
                    currentLogs.add("SYSTEM: Sending SIGKILL to PID 4982...")
                    currentLogs.add("SUCCESS: Process 4982 terminated. Encryption stopped.")
                    currentLogs.add("INTEGRITY_CHECK: 12 assets locked, 142 saved successfully.")
                    currentLogs.add("VICTORY: Ransomware contagion isolated and destroyed!")
                    _simulationCompleted.value = true
                    viewModelScope.launch {
                        repository.completeSimulation(sim.id, sim.points)
                    }
                }
                trimmedCmd.startsWith("/kill") -> {
                    currentLogs.add("TERMINATION ERROR: Specified PID is not active or belongs to a core system service.")
                }
                else -> {
                    currentLogs.add("SHELL: Command not recognized. Try `/ps` or `/kill <PID>`")
                }
            }
        }

        _consoleLogs.value = currentLogs
    }

    // --- Coding Challenges Actions ---
    fun selectChallenge(challenge: CodingChallenge?) {
        _selectedChallenge.value = challenge
        _challengeSubmitted.value = false
        _challengeCorrect.value = false
        _challengeOutputMessage.value = ""
    }

    fun submitChallengeAnswer(optionIndex: Int) {
        val challenge = _selectedChallenge.value ?: return
        val isCorrect = optionIndex == challenge.answerIndex
        _challengeSubmitted.value = true
        _challengeCorrect.value = isCorrect

        if (isCorrect) {
            _challengeOutputMessage.value = "TEST CASE SUCCESS: ${challenge.testCasesDescription}\nCompilation 100% Successful. Security vulnerability resolved! +${challenge.points} XP"
            viewModelScope.launch {
                repository.completeChallenge(challenge.id, challenge.points)
            }
        } else {
            _challengeOutputMessage.value = "COMPILATION ERROR: Fix required. The security checker failed the unit test because the vulnerability remains exploit-susceptible."
        }
    }

    // --- Study Reminders & Suggestions Actions ---
    fun addReminder(title: String, time: String, days: String) {
        viewModelScope.launch {
            repository.addReminder(title, time, days)
        }
    }

    fun deleteReminder(id: Int) {
        viewModelScope.launch {
            repository.deleteReminder(id)
        }
    }

    fun toggleStudyGroupJoin(group: StudyGroupEntity) {
        viewModelScope.launch {
            repository.toggleStudyGroupJoin(group.id, group.isJoined)
        }
    }

    // --- Forums Actions ---
    fun createPost(title: String, content: String, tag: String) {
        viewModelScope.launch {
            repository.createForumPost(title, content, "thawfiks", "Student", tag)
        }
    }

    fun addReply(postId: Int, content: String) {
        viewModelScope.launch {
            repository.addPostReply(postId, "thawfiks", "Student", content)
        }
    }

    fun parseReplies(json: String): List<Reply> {
        return repository.parseReplies(json)
    }

    // --- AI-powered Mock Interview Actions ---
    fun startInterview(type: String) {
        _interviewType.value = type
        _isInterviewRunning.value = true
        _interviewFinished.value = false
        _lastScore.value = null
        _interviewFeedback.value = ""
        interviewTranscript.clear()

        _isGenerating.value = true
        viewModelScope.launch {
            val firstResponse = repository.conductInterviewStep(type, emptyList(), "")
            _currentQuestion.value = firstResponse.nextQuestion
            _isGenerating.value = false

            // Track initial turn in transcript
            interviewTranscript.add(GeminiContent(role = "model", parts = listOf(GeminiPart(firstResponse.nextQuestion))))
        }
    }

    fun submitInterviewAnswer(answer: String) {
        if (_isGenerating.value) return
        _isGenerating.value = true

        val type = _interviewType.value
        // Record user answer in transcript
        interviewTranscript.add(GeminiContent(role = "user", parts = listOf(GeminiPart(answer))))

        viewModelScope.launch {
            val response = repository.conductInterviewStep(type, interviewTranscript, answer)
            _lastScore.value = response.score
            _interviewFeedback.value = response.feedback
            _currentQuestion.value = response.nextQuestion
            _isGenerating.value = false

            // Record model follow-up
            interviewTranscript.add(GeminiContent(role = "model", parts = listOf(GeminiPart(response.nextQuestion))))

            // If we have completed 3 rounds of QA, we can let user finish and save results
            // We can determine this by looking at transcript length (user + model turns)
            if (interviewTranscript.size >= 6) {
                _interviewFinished.value = true
            }
        }
    }

    fun saveAndFinishInterview() {
        val finalScore = _lastScore.value ?: 80
        val finalFeedback = _interviewFeedback.value
        val type = _interviewType.value

        viewModelScope.launch {
            // Serialize transcript
            val adapter = moshi.adapter<List<GeminiContent>>(
                Types.newParameterizedType(List::class.java, GeminiContent::class.java)
            )
            val transcriptJson = adapter.toJson(interviewTranscript)
            repository.saveInterviewResult(type, finalScore, finalFeedback, transcriptJson)
            _isInterviewRunning.value = false
            _interviewFinished.value = false
            setScreen(AcademyScreen.Home) // go back home to see XP increase!
        }
    }

    fun cancelInterview() {
        _isInterviewRunning.value = false
        _interviewFinished.value = false
    }

    // --- Static content listings ---
    fun getLessonsForSelectedPath(): List<Lesson> {
        return repository.lessonsList.filter { it.path == _selectedPath.value }
    }

    fun getSimulations(): List<Simulation> {
        return repository.simulationsList
    }

    fun getCodingChallenges(): List<CodingChallenge> {
        return repository.codingChallengesList
    }

    fun getJobListings(): List<JobListing> {
        return repository.jobListingsList
    }

    // --- Job Board Dynamic Actions ---

    fun postJobListing(
        title: String,
        company: String,
        location: String,
        type: String,
        salary: String,
        recruiterName: String,
        description: String,
        requirements: String
    ) {
        viewModelScope.launch {
            repository.postJobListing(
                title = title,
                company = company,
                location = location,
                type = type,
                salary = salary,
                recruiterName = recruiterName,
                description = description,
                requirements = requirements
            )
        }
    }

    fun updateCandidateProfile(
        name: String,
        title: String,
        skills: String,
        certifications: String,
        experienceYears: Int,
        email: String,
        avatarSeed: String
    ) {
        viewModelScope.launch {
            repository.updateCandidateProfile(
                name = name,
                title = title,
                skills = skills,
                certifications = certifications,
                experienceYears = experienceYears,
                email = email,
                avatarSeed = avatarSeed
            )
        }
    }

    fun applyForJob(
        jobId: Int,
        applicantName: String,
        applicantEmail: String,
        applicantSkills: String,
        applicantCertifications: String
    ) {
        viewModelScope.launch {
            repository.applyForJob(
                jobId = jobId,
                applicantName = applicantName,
                applicantEmail = applicantEmail,
                applicantSkills = applicantSkills,
                applicantCertifications = applicantCertifications
            )
            repository.addXp(15) // +15 XP for career progression!
        }
    }

    fun getApplicationsForJob(jobId: Int): kotlinx.coroutines.flow.Flow<List<com.example.data.database.JobApplicationEntity>> {
        return repository.getApplicationsForJob(jobId)
    }
}

// --- Factory Provider ---

class AcademyViewModelFactory(
    private val application: Application,
    private val repository: AcademyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AcademyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AcademyViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
