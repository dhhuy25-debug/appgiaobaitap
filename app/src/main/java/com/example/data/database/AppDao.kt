package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // USERS
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id != 'teacher_huyduong'")
    suspend fun deleteSampleUsers()

    // CLASSES
    @Query("SELECT * FROM classes ORDER BY grade ASC, name ASC")
    fun getAllClassesFlow(): Flow<List<ClassRoomEntity>>

    @Query("SELECT * FROM classes ORDER BY grade ASC, name ASC")
    suspend fun getAllClasses(): List<ClassRoomEntity>

    @Query("SELECT * FROM classes WHERE id = :id LIMIT 1")
    suspend fun getClassById(id: String): ClassRoomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(classRoom: ClassRoomEntity)

    @Update
    suspend fun updateClass(classRoom: ClassRoomEntity)

    @Delete
    suspend fun deleteClass(classRoom: ClassRoomEntity)

    // STUDENTS
    @Query("SELECT * FROM students ORDER BY studentCode ASC")
    fun getAllStudentsFlow(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students ORDER BY studentCode ASC")
    suspend fun getAllStudents(): List<StudentEntity>

    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY studentCode ASC")
    fun getStudentsByClassFlow(classId: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY studentCode ASC")
    suspend fun getStudentsByClass(classId: String): List<StudentEntity>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: String): StudentEntity?

    @Query("SELECT * FROM students WHERE studentCode = :code LIMIT 1")
    suspend fun getStudentByCode(code: String): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    // ATTENDANCE
    @Query("SELECT * FROM attendance WHERE classId = :classId AND date = :date")
    fun getAttendanceFlow(classId: String, date: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE classId = :classId AND date = :date")
    suspend fun getAttendance(classId: String, date: String): List<AttendanceEntity>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId")
    fun getAttendanceForStudentFlow(studentId: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId")
    suspend fun getAttendanceForStudent(studentId: String): List<AttendanceEntity>

    @Query("SELECT * FROM attendance")
    suspend fun getAllAttendance(): List<AttendanceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceList(list: List<AttendanceEntity>)

    // DISCIPLINE
    @Query("SELECT * FROM discipline WHERE studentId = :studentId ORDER BY date DESC")
    fun getDisciplineForStudentFlow(studentId: String): Flow<List<DisciplineEntity>>

    @Query("SELECT * FROM discipline WHERE studentId = :studentId ORDER BY date DESC")
    suspend fun getDisciplineForStudent(studentId: String): List<DisciplineEntity>

    @Query("SELECT * FROM discipline ORDER BY date DESC")
    fun getAllDisciplineFlow(): Flow<List<DisciplineEntity>>

    @Query("SELECT * FROM discipline")
    suspend fun getAllDiscipline(): List<DisciplineEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscipline(discipline: DisciplineEntity)

    @Delete
    suspend fun deleteDiscipline(discipline: DisciplineEntity)

    // REWARDS
    @Query("SELECT * FROM rewards ORDER BY date DESC")
    fun getAllRewardsFlow(): Flow<List<RewardEntity>>

    @Query("SELECT * FROM rewards WHERE studentId = :studentId ORDER BY date DESC")
    fun getRewardsForStudentFlow(studentId: String): Flow<List<RewardEntity>>

    @Query("SELECT * FROM rewards WHERE studentId = :studentId")
    suspend fun getRewardsForStudent(studentId: String): List<RewardEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReward(reward: RewardEntity)

    @Delete
    suspend fun deleteReward(reward: RewardEntity)

    // STUDENT NOTES
    @Query("SELECT * FROM student_notes WHERE studentId = :studentId LIMIT 1")
    fun getStudentNoteFlow(studentId: String): Flow<StudentNoteEntity?>

    @Query("SELECT * FROM student_notes WHERE studentId = :studentId LIMIT 1")
    suspend fun getStudentNote(studentId: String): StudentNoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentNote(note: StudentNoteEntity)

    // ASSIGNMENTS
    @Query("SELECT * FROM assignments ORDER BY createdAt DESC")
    fun getAllAssignmentsFlow(): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE status = 'PUBLISHED' ORDER BY createdAt DESC")
    fun getPublishedAssignmentsFlow(): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE id = :id LIMIT 1")
    suspend fun getAssignmentById(id: String): AssignmentEntity?

    @Query("SELECT * FROM assignments WHERE id = :id LIMIT 1")
    fun getAssignmentByIdFlow(id: String): Flow<AssignmentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: AssignmentEntity)

    @Update
    suspend fun updateAssignment(assignment: AssignmentEntity)

    @Delete
    suspend fun deleteAssignment(assignment: AssignmentEntity)

    // QUESTIONS
    @Query("SELECT * FROM questions WHERE assignmentId = :assignmentId ORDER BY orderIndex ASC")
    fun getQuestionsForAssignmentFlow(assignmentId: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE assignmentId = :assignmentId ORDER BY orderIndex ASC")
    suspend fun getQuestionsForAssignment(assignmentId: String): List<QuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity)

    @Update
    suspend fun updateQuestion(question: QuestionEntity)

    @Delete
    suspend fun deleteQuestion(question: QuestionEntity)

    @Query("DELETE FROM questions WHERE assignmentId = :assignmentId")
    suspend fun deleteQuestionsByAssignment(assignmentId: String)

    // SUBMISSIONS
    @Query("SELECT * FROM submissions ORDER BY submittedAt DESC")
    fun getAllSubmissionsFlow(): Flow<List<SubmissionEntity>>

    @Query("SELECT * FROM submissions WHERE assignmentId = :assignmentId ORDER BY score DESC, durationSeconds ASC")
    fun getSubmissionsForAssignmentFlow(assignmentId: String): Flow<List<SubmissionEntity>>

    @Query("SELECT * FROM submissions WHERE assignmentId = :assignmentId ORDER BY score DESC, durationSeconds ASC")
    suspend fun getSubmissionsForAssignment(assignmentId: String): List<SubmissionEntity>

    @Query("SELECT * FROM submissions WHERE studentId = :studentId ORDER BY submittedAt DESC")
    fun getSubmissionsForStudentFlow(studentId: String): Flow<List<SubmissionEntity>>

    @Query("SELECT * FROM submissions WHERE studentId = :studentId")
    suspend fun getSubmissionsForStudent(studentId: String): List<SubmissionEntity>

    @Query("SELECT * FROM submissions WHERE assignmentId = :assignmentId AND studentId = :studentId ORDER BY attemptNumber DESC")
    suspend fun getSubmissionsForStudentAndAssignment(assignmentId: String, studentId: String): List<SubmissionEntity>

    @Query("SELECT * FROM submissions WHERE id = :id LIMIT 1")
    suspend fun getSubmissionById(id: String): SubmissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: SubmissionEntity)

    // SUBMISSION ANSWERS
    @Query("SELECT * FROM submission_answers WHERE submissionId = :submissionId")
    suspend fun getAnswersForSubmission(submissionId: String): List<SubmissionAnswerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmissionAnswers(answers: List<SubmissionAnswerEntity>)

    // SETTINGS
    @Query("SELECT * FROM system_settings WHERE id = 'main_settings' LIMIT 1")
    fun getSettingsFlow(): Flow<SystemSettingsEntity?>

    @Query("SELECT * FROM system_settings WHERE id = 'main_settings' LIMIT 1")
    suspend fun getSettings(): SystemSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: SystemSettingsEntity)

    // CLEAR SAMPLE DATA
    @Query("DELETE FROM students")
    suspend fun clearAllStudents()

    @Query("DELETE FROM classes")
    suspend fun clearAllClasses()

    @Query("DELETE FROM assignments")
    suspend fun clearAllAssignments()

    @Query("DELETE FROM questions")
    suspend fun clearAllQuestions()

    @Query("DELETE FROM submissions")
    suspend fun clearAllSubmissions()

    @Query("DELETE FROM submission_answers")
    suspend fun clearAllSubmissionAnswers()

    @Query("DELETE FROM attendance")
    suspend fun clearAllAttendance()

    @Query("DELETE FROM discipline")
    suspend fun clearAllDiscipline()

    @Query("DELETE FROM rewards")
    suspend fun clearAllRewards()

    @Query("DELETE FROM student_notes")
    suspend fun clearAllStudentNotes()
}
