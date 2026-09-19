package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val role: String, // "TEACHER" or "STUDENT"
    val username: String,
    val password: String,
    val fullName: String,
    val studentCode: String? = null,
    val classId: String? = null,
    val className: String? = null,
    val avatar: String = "default"
)

@Entity(tableName = "classes")
data class ClassRoomEntity(
    @PrimaryKey val id: String,
    val name: String, // e.g. "5/9"
    val grade: Int, // 1 to 5
    val schoolYear: String = "2024-2025",
    val totalStudents: Int = 0,
    val note: String = ""
)

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val id: String,
    val studentCode: String, // e.g. "HS001"
    val fullName: String,
    val birthDate: String = "2014-05-15",
    val gender: String = "Nam",
    val classId: String,
    val className: String,
    val parentPhone: String = "",
    val note: String = "",
    val username: String,
    val password: String = "123456"
)

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val classId: String,
    val date: String, // yyyy-MM-dd
    val status: String, // "PRESENT", "EXCUSED_ABSENCE", "UNEXCUSED_ABSENCE"
    val note: String = ""
)

@Entity(tableName = "discipline")
data class DisciplineEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val classId: String,
    val date: String,
    val criteria: String, // "Đi học đúng giờ", "Làm bài tập", etc.
    val scoreChange: Int, // e.g. +2, -1
    val reason: String = "",
    val note: String = ""
)

@Entity(tableName = "rewards")
data class RewardEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val studentName: String,
    val classId: String,
    val date: String,
    val rewardName: String,
    val reason: String,
    val points: Int = 10,
    val badgeType: String = "DILIGENT" // "DILIGENT", "ACHIEVEMENT", "PROGRESS", "CREATIVE", "COOPERATION"
)

@Entity(tableName = "student_notes")
data class StudentNoteEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val academicStatus: String = "Tiếp thu tốt, chăm chú nghe giảng",
    val disciplineStatus: String = "Chấp hành tốt nội quy lớp học",
    val attendanceStatus: String = "Chuyên cần cao",
    val specialCircumstances: String = "",
    val peerRelations: String = "Thân thiện, hòa đồng với bạn bè",
    val psychologyBehavior: String = "Tự tin, hoạt bát",
    val parentCoordination: String = "Phụ huynh nhiệt tình phối hợp",
    val teacherComments: String = "Có năng khiếu, cần phát huy",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "assignments")
data class AssignmentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val grade: Int = 5,
    val topic: String = "",
    val content: String = "",
    val objectives: String = "",
    val durationMinutes: Int = 15,
    val deadline: String = "2025-12-31 23:59",
    val maxAttempts: Int = 2, // 0 = unlimited
    val maxScore: Int = 100,
    val shuffleQuestions: Boolean = true,
    val shuffleAnswers: Boolean = true,
    val showAnswersAfterSubmit: Boolean = true,
    val allowAiTutor: Boolean = true,
    val releaseResultsImmediately: Boolean = true,
    val scoringRule: String = "HIGHEST", // "HIGHEST", "LATEST", "AVERAGE"
    val status: String = "PUBLISHED", // "DRAFT", "PUBLISHED"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: String,
    val assignmentId: String,
    val orderIndex: Int,
    val type: String, // "SINGLE_CHOICE", "MULTI_CHOICE", "TRUE_FALSE", "FILL_BLANK", "MATCHING", "ORDERING", "SHORT_ANSWER"
    val prompt: String,
    val optionsJson: String = "[]", // serialized JSON array
    val correctAnswer: String,
    val explanation: String = "",
    val points: Int = 10,
    val difficulty: String = "COMPREHENSION" // "RECOGNITION", "COMPREHENSION", "APPLICATION", "HIGH_APPLICATION"
)

@Entity(tableName = "submissions")
data class SubmissionEntity(
    @PrimaryKey val id: String,
    val assignmentId: String,
    val studentId: String,
    val studentName: String,
    val className: String,
    val attemptNumber: Int = 1,
    val score: Double,
    val totalQuestions: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val durationSeconds: Int,
    val submittedAt: Long = System.currentTimeMillis(),
    val aiFeedbackSummary: String = "",
    val teacherReviewed: Boolean = true
)

@Entity(tableName = "submission_answers")
data class SubmissionAnswerEntity(
    @PrimaryKey val id: String,
    val submissionId: String,
    val questionId: String,
    val studentAnswer: String,
    val isCorrect: Boolean,
    val pointsAwarded: Double,
    val feedback: String = ""
)

@Entity(tableName = "system_settings")
data class SystemSettingsEntity(
    @PrimaryKey val id: String = "main_settings",
    val teacherName: String = "Thầy Huy Dương",
    val schoolName: String = "Trường Tiểu Học Tân Phú",
    val schoolYear: String = "2024 - 2025",
    val defaultClass: String = "5/9",
    val defaultGrade: Int = 5,
    val passScore: Double = 50.0,
    val rankingTieBreaker: String = "TIME_FIRST", // "TIME_FIRST", "DISCIPLINE_FIRST"
    val allowAiTutorGlobal: Boolean = true,
    val firestoreSyncEnabled: Boolean = false,
    val firebaseConfigJson: String = ""
)
