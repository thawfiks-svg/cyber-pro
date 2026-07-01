package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Database
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// --- Entities ---

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val id: Int = 1,
    val xp: Int = 0,
    val level: Int = 1,
    val completedLessonIds: String = "", // comma-separated
    val completedChallengeIds: String = "", // comma-separated
    val completedSimulationIds: String = "" // comma-separated
)

@Entity(tableName = "forum_posts")
data class ForumPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val author: String,
    val role: String, // "Mentor", "Student", "Expert"
    val timestamp: Long,
    val tag: String,
    val repliesJson: String = "[]" // JSON representation of replies
)

@Entity(tableName = "study_reminders")
data class StudyReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val timeText: String,
    val daysText: String, // e.g. "Mon, Wed, Fri"
    val isEnabled: Boolean = true
)

@Entity(tableName = "study_groups")
data class StudyGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val certificationPath: String, // "CompTIA Security+", "CISSP", "Networking"
    val memberCount: Int,
    val isJoined: Boolean = false
)

@Entity(tableName = "interview_sessions")
data class InterviewSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "Technical" or "Behavioral"
    val timestamp: Long,
    val feedback: String = "",
    val score: Int = 0,
    val transcriptJson: String = "[]"
)

// --- DAOs ---

@Dao
interface AcademyDao {
    // User Progress
    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    fun getUserProgress(): Flow<UserProgressEntity?>

    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    suspend fun getUserProgressOnce(): UserProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProgress(progress: UserProgressEntity)

    // Forum Posts
    @Query("SELECT * FROM forum_posts ORDER BY timestamp DESC")
    fun getAllForumPosts(): Flow<List<ForumPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForumPost(post: ForumPostEntity)

    @Query("SELECT * FROM forum_posts WHERE id = :postId")
    suspend fun getForumPostById(postId: Int): ForumPostEntity?

    // Study Reminders
    @Query("SELECT * FROM study_reminders ORDER BY id DESC")
    fun getAllReminders(): Flow<List<StudyReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: StudyReminderEntity)

    @Query("DELETE FROM study_reminders WHERE id = :reminderId")
    suspend fun deleteReminderById(reminderId: Int)

    // Study Groups
    @Query("SELECT * FROM study_groups")
    fun getAllStudyGroups(): Flow<List<StudyGroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyGroup(group: StudyGroupEntity)

    @Query("UPDATE study_groups SET isJoined = :isJoined, memberCount = memberCount + :memberDelta WHERE id = :groupId")
    suspend fun updateGroupJoinState(groupId: Int, isJoined: Boolean, memberDelta: Int)

    // Interview Sessions
    @Query("SELECT * FROM interview_sessions ORDER BY timestamp DESC")
    fun getAllInterviewSessions(): Flow<List<InterviewSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterviewSession(session: InterviewSessionEntity)

    // Job Listings
    @Query("SELECT * FROM job_listings ORDER BY timestamp DESC")
    fun getAllJobListingsFlow(): Flow<List<JobListingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobListing(job: JobListingEntity)

    // Candidates
    @Query("SELECT * FROM candidate_profiles ORDER BY id DESC")
    fun getAllCandidatesFlow(): Flow<List<CandidateProfileEntity>>

    @Query("SELECT * FROM candidate_profiles WHERE isCurrentUser = 1 LIMIT 1")
    fun getCurrentUserCandidateFlow(): Flow<CandidateProfileEntity?>

    @Query("SELECT * FROM candidate_profiles WHERE isCurrentUser = 1 LIMIT 1")
    suspend fun getCurrentUserCandidateOnce(): CandidateProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandidate(candidate: CandidateProfileEntity)

    // Job Applications
    @Query("SELECT * FROM job_applications WHERE jobId = :jobId ORDER BY timestamp DESC")
    fun getApplicationsForJobFlow(jobId: Int): Flow<List<JobApplicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobApplication(app: JobApplicationEntity)

    @Query("SELECT * FROM job_applications ORDER BY timestamp DESC")
    fun getAllJobApplicationsFlow(): Flow<List<JobApplicationEntity>>
}

// --- Additional Entities for Job Board ---

@Entity(tableName = "job_listings")
data class JobListingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val company: String,
    val location: String,
    val type: String, // "Full-time", "Contract", "Remote"
    val salary: String,
    val recruiterName: String,
    val description: String,
    val requirements: String, // comma-separated
    val timestamp: Long
)

@Entity(tableName = "candidate_profiles")
data class CandidateProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val title: String,
    val skills: String, // comma-separated
    val certifications: String, // comma-separated
    val experienceYears: Int,
    val email: String,
    val avatarSeed: String,
    val isCurrentUser: Boolean = false
)

@Entity(tableName = "job_applications")
data class JobApplicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val jobId: Int,
    val applicantName: String,
    val applicantEmail: String,
    val applicantSkills: String, // comma-separated
    val applicantCertifications: String, // comma-separated
    val timestamp: Long
)

// --- Database ---

@Database(
    entities = [
        UserProgressEntity::class,
        ForumPostEntity::class,
        StudyReminderEntity::class,
        StudyGroupEntity::class,
        InterviewSessionEntity::class,
        JobListingEntity::class,
        CandidateProfileEntity::class,
        JobApplicationEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun academyDao(): AcademyDao
}
