package com.example.data.database

import com.example.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

object DatabaseInitializer {

    suspend fun initializeIfNeeded(dao: AppDao) = withContext(Dispatchers.IO) {
        val teacher = dao.getUserByUsername("huyduong")
        if (teacher == null) {
            populateSampleData(dao)
        }
    }

    suspend fun populateSampleData(dao: AppDao) = withContext(Dispatchers.IO) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // 1. Settings
        val settings = SystemSettingsEntity(
            id = "main_settings",
            teacherName = "Thầy Huy Dương",
            schoolName = "Trường Tiểu Học Tân Phú",
            schoolYear = "2024 - 2025",
            defaultClass = "5/9",
            defaultGrade = 5,
            passScore = 50.0,
            rankingTieBreaker = "TIME_FIRST",
            allowAiTutorGlobal = true
        )
        dao.saveSettings(settings)

        // 2. Teacher account
        val teacherUser = UserEntity(
            id = "teacher_huyduong",
            role = "TEACHER",
            username = "huyduong",
            password = "123",
            fullName = "Thầy Huy Dương",
            avatar = "teacher"
        )
        dao.insertUser(teacherUser)

        // 3. Classes
        val class59 = ClassRoomEntity(
            id = "class_5_9",
            name = "5/9",
            grade = 5,
            schoolYear = "2024-2025",
            totalStudents = 10,
            note = "Lớp chủ nhiệm Thầy Huy Dương"
        )
        val class51 = ClassRoomEntity(
            id = "class_5_1",
            name = "5/1",
            grade = 5,
            schoolYear = "2024-2025",
            totalStudents = 0,
            note = "Khối 5"
        )
        val class42 = ClassRoomEntity(
            id = "class_4_2",
            name = "4/2",
            grade = 4,
            schoolYear = "2024-2025",
            totalStudents = 0,
            note = "Khối 4"
        )
        dao.insertClass(class59)
        dao.insertClass(class51)
        dao.insertClass(class42)

        // 4. 10 Sample Students for 5/9
        val studentsList = listOf(
            StudentEntity("std_001", "HS001", "Nguyễn Văn An", "2014-02-12", "Nam", "class_5_9", "5/9", "0912345601", "Lớp trưởng, gương mẫu", "hs001", "123"),
            StudentEntity("std_002", "HS002", "Trần Thị Mai", "2014-06-20", "Nữ", "class_5_9", "5/9", "0912345602", "Lớp phó học tập", "hs002", "123"),
            StudentEntity("std_003", "HS003", "Lê Gia Bảo", "2014-04-05", "Nam", "class_5_9", "5/9", "0912345603", "Toán tốt, nhiệt tình", "hs003", "123"),
            StudentEntity("std_004", "HS004", "Phạm Quỳnh Chi", "2014-08-18", "Nữ", "class_5_9", "5/9", "0912345604", "Viết chữ đẹp", "hs004", "123"),
            StudentEntity("std_005", "HS005", "Hoàng Minh Đức", "2014-11-23", "Nam", "class_5_9", "5/9", "0912345605", "Hơi hiếu động", "hs005", "123"),
            StudentEntity("std_006", "HS006", "Vũ Thùy Linh", "2014-01-30", "Nữ", "class_5_9", "5/9", "0912345606", "Cần luyện đọc diễn cảm", "hs006", "123"),
            StudentEntity("std_007", "HS007", "Đặng Tuấn Kiệt", "2014-09-14", "Nam", "class_5_9", "5/9", "0912345607", "Có tiến bộ môn Khoa học", "hs007", "123"),
            StudentEntity("std_008", "HS008", "Bùi Ngọc Hân", "2014-07-08", "Nữ", "class_5_9", "5/9", "0912345608", "Chăm chỉ làm bài tập", "hs008", "123"),
            StudentEntity("std_009", "HS009", "Ngô Hoàng Nam", "2014-12-01", "Nam", "class_5_9", "5/9", "0912345609", "Cần nhắc nhở trật tự", "hs009", "123"),
            StudentEntity("std_010", "HS010", "Đỗ Thanh Trúc", "2014-03-25", "Nữ", "class_5_9", "5/9", "0912345610", "Khéo tay, vẽ tranh đẹp", "hs010", "123")
        )
        dao.insertStudents(studentsList)

        for (st in studentsList) {
            dao.insertUser(
                UserEntity(
                    id = "user_${st.id}",
                    role = "STUDENT",
                    username = st.username,
                    password = st.password,
                    fullName = st.fullName,
                    studentCode = st.studentCode,
                    classId = st.classId,
                    className = st.className
                )
            )
        }

        // 5. Sample Attendance for Today
        val attendances = studentsList.mapIndexed { index, st ->
            val status = when (index) {
                4 -> "EXCUSED_ABSENCE"
                8 -> "UNEXCUSED_ABSENCE"
                else -> "PRESENT"
            }
            val note = when (index) {
                4 -> "Phụ huynh xin phép bị cảm"
                8 -> "Chưa liên lạc được phụ huynh"
                else -> ""
            }
            AttendanceEntity(
                id = "att_${st.id}_$today",
                studentId = st.id,
                classId = st.classId,
                date = today,
                status = status,
                note = note
            )
        }
        dao.insertAttendanceList(attendances)

        // 6. Discipline Records
        dao.insertDiscipline(DisciplineEntity("disc_01", "std_001", "class_5_9", today, "Giúp đỡ bạn", 2, "Hỗ trợ bạn học tập", "Khen ngợi"))
        dao.insertDiscipline(DisciplineEntity("disc_02", "std_002", "class_5_9", today, "Đi học đúng giờ", 1, "Đến sớm trực nhật", ""))
        dao.insertDiscipline(DisciplineEntity("disc_03", "std_005", "class_5_9", today, "Giữ trật tự", -1, "Nói chuyện riêng trong giờ học", "Đã nhắc nhở"))
        dao.insertDiscipline(DisciplineEntity("disc_04", "std_003", "class_5_9", today, "Làm bài tập", 2, "Hoàn thành bài xuất sắc", ""))

        // 7. Rewards & Badges
        dao.insertReward(RewardEntity("rew_01", "std_001", "Nguyễn Văn An", "class_5_9", today, "Ngôi sao Chăm chỉ", "Lớp trưởng gương mẫu cả tuần", 20, "DILIGENT"))
        dao.insertReward(RewardEntity("rew_02", "std_002", "Trần Thị Mai", "class_5_9", today, "Thủ khoa Vòng 1", "Đạt điểm tuyệt đối bài kiểm tra", 25, "ACHIEVEMENT"))
        dao.insertReward(RewardEntity("rew_03", "std_007", "Đặng Tuấn Kiệt", "class_5_9", today, "Búp sen Tiến bộ", "Tiến bộ vượt bậc môn Khoa học", 15, "PROGRESS"))
        dao.insertReward(RewardEntity("rew_04", "std_010", "Đỗ Thanh Trúc", "class_5_9", today, "Họa sĩ Nhí Sáng tạo", "Sáng tạo sơ đồ tư duy môn học", 15, "CREATIVE"))
        dao.insertReward(RewardEntity("rew_05", "std_003", "Lê Gia Bảo", "class_5_9", today, "Đôi bạn Cùng tiến", "Nhiệt tình hướng dẫn nhóm", 15, "COOPERATION"))

        // 8. Holistic Student Profile Notes
        dao.insertStudentNote(
            StudentNoteEntity(
                id = "note_std_001",
                studentId = "std_001",
                academicStatus = "Xuất sắc toàn diện, năng nổ trong học tập.",
                disciplineStatus = "Chấp hành rất tốt nề nếp nội quy trường lớp.",
                attendanceStatus = "100% chuyên cần đầy đủ, đúng giờ.",
                specialCircumstances = "Gia đình quan tâm chu đáo.",
                peerRelations = "Được tập thể lớp tin tưởng và quý mến.",
                psychologyBehavior = "Chín chắn, trách nhiệm cao.",
                parentCoordination = "Thường xuyên trao đổi tình hình cùng giáo viên.",
                teacherComments = "Tiếp tục phát huy vai trò thủ lĩnh học sinh."
            )
        )
        dao.insertStudentNote(
            StudentNoteEntity(
                id = "note_std_005",
                studentId = "std_005",
                academicStatus = "Lực học trung bình khá, hay lơ đãng khi làm bài.",
                disciplineStatus = "Thỉnh thoảng nói chuyện riêng và quên mang sách.",
                attendanceStatus = "Vắng 1 buổi có phép trong tuần.",
                specialCircumstances = "Bố mẹ đi làm xa, ở với ông bà.",
                peerRelations = "Hòa đồng nhưng dễ bị phân tâm.",
                psychologyBehavior = "Cần động viên, khen ngợi đúng lúc.",
                parentCoordination = "Giáo viên chủ động gọi điện trao đổi hàng tuần.",
                teacherComments = "Cần kèm cặp sát sao bài tập về nhà và nhắc nhở nề nếp."
            )
        )

        // 9. Sample Assignment: Khoa học Lớp 5
        val assignId = "assign_sample_01"
        val sampleAssignment = AssignmentEntity(
            id = assignId,
            title = "Khoa học Lớp 5: Sự biến đổi của chất và năng lượng",
            subject = "Khoa học",
            grade = 5,
            topic = "Chất và năng lượng",
            content = "Ôn tập kiến thức về sự biến đổi hóa học, năng lượng điện và năng lượng mặt trời.",
            objectives = "Nắm vững hiện tượng biến đổi chất, nhận biết các nguồn năng lượng và cách sử dụng an toàn, tiết kiệm.",
            durationMinutes = 15,
            deadline = "2025-12-31 23:59",
            maxAttempts = 2,
            maxScore = 100,
            shuffleQuestions = false,
            shuffleAnswers = false,
            showAnswersAfterSubmit = true,
            allowAiTutor = true,
            releaseResultsImmediately = true,
            scoringRule = "HIGHEST",
            status = "PUBLISHED",
            createdAt = System.currentTimeMillis() - 86400000L
        )
        dao.insertAssignment(sampleAssignment)

        // 10. Sample Questions for Assignment
        val questions = listOf(
            QuestionEntity(
                id = "q_001",
                assignmentId = assignId,
                orderIndex = 1,
                type = "SINGLE_CHOICE",
                prompt = "Hiện tượng nào dưới đây là sự biến đổi hóa học?",
                optionsJson = "[\"A. Đinh sắt để ngoài không khí ẩm bị gỉ sét\", \"B. Nước sôi bốc hơi thành hơi nước\", \"C. Cắt tờ giấy màu thành nhiều mảnh nhỏ\", \"D. Nước đá tan thành nước lỏng\"]",
                correctAnswer = "A",
                explanation = "Hiện tượng gỉ sét tạo ra chất mới (oxit sắt), đó là sự biến đổi hóa học. Còn bay hơi, xé giấy hay tan chảy chỉ là biến đổi lý học.",
                points = 20,
                difficulty = "RECOGNITION"
            ),
            QuestionEntity(
                id = "q_002",
                assignmentId = assignId,
                orderIndex = 2,
                type = "TRUE_FALSE",
                prompt = "Mặt Trời là nguồn cung cấp ánh sáng và nhiệt năng vô tận cho Trái Đất.",
                optionsJson = "[\"Đúng\", \"Sai\"]",
                correctAnswer = "Đúng",
                explanation = "Mặt Trời là nguồn năng lượng chính chiếu sáng và sưởi ấm cho sự sống trên Trái Đất.",
                points = 20,
                difficulty = "RECOGNITION"
            ),
            QuestionEntity(
                id = "q_003",
                assignmentId = assignId,
                orderIndex = 3,
                type = "FILL_BLANK",
                prompt = "Để tiết kiệm điện, khi rời khỏi phòng học, các em học sinh cần phải ... tất cả các thiết bị điện.",
                optionsJson = "[\"bật\", \"tắt\", \"sửa\", \"giữ\"]",
                correctAnswer = "tắt",
                explanation = "Tắt các thiết bị điện khi không sử dụng là hành động thiết thực để tiết kiệm điện và phòng tránh cháy nổ.",
                points = 20,
                difficulty = "APPLICATION"
            ),
            QuestionEntity(
                id = "q_004",
                assignmentId = assignId,
                orderIndex = 4,
                type = "SHORT_ANSWER",
                prompt = "Nêu 2 việc làm cụ thể em có thể làm ở nhà để tiết kiệm điện năng?",
                optionsJson = "[]",
                correctAnswer = "Tắt quạt và đèn khi ra khỏi phòng, mở cửa sổ đón gió và ánh sáng tự nhiên",
                explanation = "Các việc làm đúng: tắt thiết bị không dùng, tận dụng ánh sáng tự nhiên, không mở tủ lạnh quá lâu.",
                points = 20,
                difficulty = "HIGH_APPLICATION"
            ),
            QuestionEntity(
                id = "q_005",
                assignmentId = assignId,
                orderIndex = 5,
                type = "MULTI_CHOICE",
                prompt = "Những nguồn năng lượng nào dưới đây là năng lượng sạch, tái tạo được? (Chọn nhiều đáp án)",
                optionsJson = "[\"A. Năng lượng gió\", \"B. Năng lượng Mặt Trời\", \"C. Năng lượng từ than đá\", \"D. Năng lượng dòng nước chảy\"]",
                correctAnswer = "A, B, D",
                explanation = "Gió, Mặt Trời và nước chảy là các nguồn năng lượng tái tạo, không gây ô nhiễm môi trường.",
                points = 20,
                difficulty = "COMPREHENSION"
            )
        )
        dao.insertQuestions(questions)

        // 11. Sample Submissions showcasing the strict ranking tie-breaker:
        // HS001 has 100 points in 501 seconds (Rank 1 🥇)
        // HS002 has 100 points in 555 seconds (Rank 2 🥈 - equal score, longer duration)
        // HS003 has 95 points in 462 seconds (Rank 3 🥉)
        // HS004 has 88 points in 540 seconds (Rank 4)
        // HS007 has 85 points in 620 seconds (Rank 5)
        // HS008 has 75 points in 700 seconds (Rank 6)
        // HS010 has 70 points in 750 seconds (Rank 7)
        val submissions = listOf(
            SubmissionEntity("sub_01", assignId, "std_001", "Nguyễn Văn An", "5/9", 1, 100.0, 5, 5, 0, 501, System.currentTimeMillis() - 7200000),
            SubmissionEntity("sub_02", assignId, "std_002", "Trần Thị Mai", "5/9", 1, 100.0, 5, 5, 0, 555, System.currentTimeMillis() - 7000000),
            SubmissionEntity("sub_03", assignId, "std_003", "Lê Gia Bảo", "5/9", 1, 95.0, 5, 4, 1, 462, System.currentTimeMillis() - 6500000),
            SubmissionEntity("sub_04", assignId, "std_004", "Phạm Quỳnh Chi", "5/9", 1, 88.0, 5, 4, 1, 540, System.currentTimeMillis() - 6000000),
            SubmissionEntity("sub_05", assignId, "std_007", "Đặng Tuấn Kiệt", "5/9", 1, 85.0, 5, 4, 1, 620, System.currentTimeMillis() - 5500000),
            SubmissionEntity("sub_06", assignId, "std_008", "Bùi Ngọc Hân", "5/9", 1, 75.0, 5, 3, 2, 700, System.currentTimeMillis() - 5000000),
            SubmissionEntity("sub_07", assignId, "std_010", "Đỗ Thanh Trúc", "5/9", 1, 70.0, 5, 3, 2, 750, System.currentTimeMillis() - 4500000)
        )
        for (sub in submissions) {
            dao.insertSubmission(sub)
        }
    }

    suspend fun clearSampleData(dao: AppDao) = withContext(Dispatchers.IO) {
        dao.clearAllStudents()
        dao.clearAllClasses()
        dao.clearAllAssignments()
        dao.clearAllQuestions()
        dao.clearAllSubmissions()
        dao.clearAllSubmissionAnswers()
        dao.clearAllAttendance()
        dao.clearAllDiscipline()
        dao.clearAllRewards()
        dao.clearAllStudentNotes()
        dao.deleteSampleUsers()
    }
}
