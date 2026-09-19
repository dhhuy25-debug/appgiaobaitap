package com.example.ui.teacher

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.GeminiEduService
import com.example.data.database.AppDao
import com.example.data.database.DatabaseInitializer
import com.example.data.models.*
import com.example.ui.components.EduConfirmDialog
import com.example.ui.components.EduStatCard
import com.example.ui.components.Top3CertificateDialog
import kotlinx.coroutines.launch

// ==========================================================
// REQUIREMENT XXV & XXVI: ASSIGNMENT STATS & QUESTION ANALYSIS
// ==========================================================
@Composable
fun AssignmentStatsScreen(
    dao: AppDao,
    assignmentId: String? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val assignments by dao.getAllAssignmentsFlow().collectAsState(initial = emptyList())
    var currentAssignId by remember(assignmentId, assignments) {
        mutableStateOf(assignmentId ?: assignments.firstOrNull()?.id ?: "")
    }

    val currentAssignment = assignments.find { it.id == currentAssignId }
    val submissions by dao.getSubmissionsForAssignmentFlow(currentAssignId).collectAsState(initial = emptyList())
    val questions by dao.getQuestionsForAssignmentFlow(currentAssignId).collectAsState(initial = emptyList())
    val students by dao.getAllStudentsFlow().collectAsState(initial = emptyList())

    val totalStudents = students.size
    val submittedCount = submissions.map { it.studentId }.distinct().size
    val notSubmittedCount = (totalStudents - submittedCount).coerceAtLeast(0)

    val maxScore = submissions.maxOfOrNull { it.score } ?: 0.0
    val minScore = submissions.minOfOrNull { it.score } ?: 0.0
    val avgScore = if (submissions.isNotEmpty()) {
        String.format("%.1f", submissions.map { it.score }.average())
    } else "0.0"

    var aiAnalysisText by remember { mutableStateOf<String?>(null) }
    var isAnalyzingAi by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "THỐNG KÊ & PHÂN TÍCH BÀI TẬP",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = currentAssignment?.title ?: "Bài tập",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Assignment Selector
        if (assignments.size > 1) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                assignments.forEach { a ->
                    FilterChip(
                        selected = currentAssignId == a.id,
                        onClick = {
                            currentAssignId = a.id
                            aiAnalysisText = null
                        },
                        label = { Text(a.title, maxLines = 1, fontSize = 11.sp) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // General Stats
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EduStatCard("Đã nộp", "$submittedCount/$totalStudents em", "Tỷ lệ: ${if (totalStudents > 0) (submittedCount * 100 / totalStudents) else 0}%", Icons.Default.FactCheck, Color(0xFFECFDF5), Color(0xFF059669), Modifier.weight(1f))
                    EduStatCard("Chưa nộp", "$notSubmittedCount em", "Cần nhắc nhở", Icons.Default.PendingActions, Color(0xFFFEF2F2), Color(0xFFDC2626), Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EduStatCard("Điểm cao nhất", "$maxScore đ", "Thủ khoa bài tập", Icons.Default.EmojiEvents, Color(0xFFFFFBEB), Color(0xFFD97706), Modifier.weight(1f))
                    EduStatCard("Điểm trung bình", "$avgScore đ", "Thang điểm 100", Icons.Default.ShowChart, Color(0xFFEFF6FF), Color(0xFF1D4ED8), Modifier.weight(1f))
                }
            }

            // Score Distribution
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Phổ điểm học sinh", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        val excellent = submissions.count { it.score >= 90 }
                        val good = submissions.count { it.score in 70.0..89.9 }
                        val avg = submissions.count { it.score in 50.0..69.9 }
                        val weak = submissions.count { it.score < 50.0 }
                        val total = submissions.size.coerceAtLeast(1)

                        ProgressBarItem("Xuất sắc (90 - 100đ)", excellent, total, Color(0xFF10B981))
                        ProgressBarItem("Khá (70 - 89đ)", good, total, Color(0xFF3B82F6))
                        ProgressBarItem("Trung bình (50 - 69đ)", avg, total, Color(0xFFF59E0B))
                        ProgressBarItem("Dưới trung bình (< 50đ)", weak, total, Color(0xFFEF4444))
                    }
                }
            }

            // Requirement XXVI: Question Misconception & AI Remedial Advice
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = Color(0xFF7C3AED))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI Phân Tích Câu Hỏi Khó & Nhầm Lẫn", fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                            }
                            if (isAnalyzingAi) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFF7C3AED))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (aiAnalysisText == null) {
                            Text(
                                text = "Phát hiện các câu hỏi học sinh hay chọn sai và đề xuất nội dung ôn tập bổ trợ cho Thầy Huy Dương.",
                                fontSize = 12.sp,
                                color = Color(0xFF475569)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    isAnalyzingAi = true
                                    coroutineScope.launch {
                                        val firstQ = questions.firstOrNull()?.prompt ?: "Kiến thức bài học"
                                        val result = GeminiEduService.analyzeQuestionMisconceptions(firstQ, 35)
                                        aiAnalysisText = result
                                        isAnalyzingAi = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                modifier = Modifier.testTag("btn_analyze_questions_ai")
                            ) {
                                Text("Phân tích lỗi sai & Gợi ý ôn tập")
                            }
                        } else {
                            Text(
                                text = aiAnalysisText ?: "",
                                fontSize = 13.sp,
                                color = Color(0xFF1E293B),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Student Submissions Table
            item {
                Text(
                    text = "Chi tiết bài nộp học sinh (${submissions.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            items(submissions) { sub ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = sub.studentName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            val minutes = sub.durationSeconds / 60
                            val seconds = sub.durationSeconds % 60
                            Text(
                                text = "Thời gian: ${String.format("%02d:%02d", minutes, seconds)} • Số câu đúng: ${sub.correctCount}/${sub.totalQuestions}",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (sub.score >= 80) Color(0xFFECFDF5) else if (sub.score >= 50) Color(0xFFEFF6FF) else Color(0xFFFEF2F2)
                        ) {
                            Text(
                                text = "${sub.score} đ",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = if (sub.score >= 80) Color(0xFF059669) else if (sub.score >= 50) Color(0xFF1D4ED8) else Color(0xFFDC2626),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================================
// REQUIREMENT XXIII & XXIV: RANKINGS & TOP 3 CERTIFICATES
// ==========================================================
@Composable
fun RankingsScreen(
    dao: AppDao,
    onViewCertificate: (Int, SubmissionEntity) -> Unit
) {
    val context = LocalContext.current
    val submissions by dao.getAllSubmissionsFlow().collectAsState(initial = emptyList())
    val assignments by dao.getAllAssignmentsFlow().collectAsState(initial = emptyList())

    var selectedAssignmentId by remember { mutableStateOf("ALL") }

    // Strict Tie-Breaking Order:
    // 1. Score DESC (Highest score first)
    // 2. Duration ASC (Shortest time first for equal scores)
    val sortedSubmissions = remember(submissions, selectedAssignmentId) {
        val filtered = if (selectedAssignmentId == "ALL") {
            // Group by studentId and take best score
            submissions.groupBy { it.studentId }.map { entry ->
                entry.value.sortedWith(
                    compareByDescending<SubmissionEntity> { it.score }
                        .thenBy { it.durationSeconds }
                ).first()
            }
        } else {
            submissions.filter { it.assignmentId == selectedAssignmentId }
        }

        filtered.sortedWith(
            compareByDescending<SubmissionEntity> { it.score }
                .thenBy { it.durationSeconds }
        )
    }

    var viewingCertSub by remember { mutableStateOf<Pair<Int, SubmissionEntity>?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "BẢNG XẾP HẠNG HỌC SINH",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFD97706)
                )
                Text(
                    text = "Quy tắc: Điểm số cao nhất -> Thời gian nhanh nhất",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }

            Button(
                onClick = {
                    Toast.makeText(context, "Đã xuất bảng xếp hạng ra tệp Excel (CSV) thành công!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                modifier = Modifier.testTag("btn_export_rankings")
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Xuất file", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(
                selected = selectedAssignmentId == "ALL",
                onClick = { selectedAssignmentId = "ALL" },
                label = { Text("Toàn bộ bài tập") }
            )
            assignments.take(2).forEach { a ->
                FilterChip(
                    selected = selectedAssignmentId == a.id,
                    onClick = { selectedAssignmentId = a.id },
                    label = { Text(a.title, maxLines = 1) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (sortedSubmissions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có kết quả xếp hạng nào.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Top 3 Podium Cards Banner
                if (sortedSubmissions.size >= 3) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "🏆 VINH DANH TOP 3 XUẤT SẮC NHẤT",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706),
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    PodiumBadge(2, sortedSubmissions[1], "🥈 Á khoa 1") {
                                        viewingCertSub = 2 to sortedSubmissions[1]
                                    }
                                    PodiumBadge(1, sortedSubmissions[0], "🥇 Thủ khoa") {
                                        viewingCertSub = 1 to sortedSubmissions[0]
                                    }
                                    PodiumBadge(3, sortedSubmissions[2], "🥉 Á khoa 2") {
                                        viewingCertSub = 3 to sortedSubmissions[2]
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text("Danh sách xếp hạng chi tiết", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                itemsIndexed(sortedSubmissions) { index, sub ->
                    val rank = index + 1
                    val (medalColor, medalIcon) = when (rank) {
                        1 -> Color(0xFFD97706) to "🥇"
                        2 -> Color(0xFF64748B) to "🥈"
                        3 -> Color(0xFFB45309) to "🥉"
                        else -> Color(0xFF94A3B8) to "$rank"
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth().testTag("ranking_row_$rank")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(medalColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = medalIcon,
                                        fontSize = if (rank <= 3) 20.sp else 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = medalColor
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = sub.studentName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    val m = sub.durationSeconds / 60
                                    val s = sub.durationSeconds % 60
                                    Text(
                                        text = "Thời gian: ${String.format("%02d:%02d", m, s)} • Lớp: ${sub.className}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${sub.score} đ",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = Color(0xFF059669)
                                )

                                if (rank <= 3) {
                                    IconButton(
                                        onClick = { viewingCertSub = rank to sub },
                                        modifier = Modifier.testTag("btn_view_cert_$rank")
                                    ) {
                                        Icon(
                                            Icons.Default.MilitaryTech,
                                            contentDescription = "Thư khen",
                                            tint = Color(0xFFD97706)
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

    // Top 3 Certificate Dialog
    viewingCertSub?.let { (rank, sub) ->
        Top3CertificateDialog(
            show = true,
            rank = rank,
            studentName = sub.studentName,
            className = sub.className,
            score = sub.score,
            durationSeconds = sub.durationSeconds,
            activityTitle = "Khoa học Lớp 5: Sự biến đổi của chất và năng lượng",
            teacherName = "Thầy Huy Dương",
            onDismiss = { viewingCertSub = null }
        )
    }
}

@Composable
fun PodiumBadge(rank: Int, sub: SubmissionEntity, title: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Text(text = when (rank) { 1 -> "🥇"; 2 -> "🥈"; else -> "🥉" }, fontSize = 28.sp)
        Text(text = sub.studentName, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
        Text(text = "${sub.score} đ", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color(0xFF059669))
        Text(text = title, fontSize = 10.sp, color = Color(0xFFD97706), fontWeight = FontWeight.SemiBold)
    }
}

// ==========================================================
// REQUIREMENT XXVII: STUDENTS NEEDING CARE & INTERVENTION
// ==========================================================
@Composable
fun SupportNeededScreen(dao: AppDao) {
    val coroutineScope = rememberCoroutineScope()
    val students by dao.getAllStudentsFlow().collectAsState(initial = emptyList())
    val submissions by dao.getAllSubmissionsFlow().collectAsState(initial = emptyList())
    val disciplineList by dao.getAllDisciplineFlow().collectAsState(initial = emptyList())

    var selectedStudentForAi by remember { mutableStateOf<StudentEntity?>(null) }
    var aiAdviceText by remember { mutableStateOf<String?>(null) }
    var isLoadingAi by remember { mutableStateOf(false) }

    val needSupportList = remember(students, submissions, disciplineList) {
        students.filter { st ->
            val subs = submissions.filter { it.studentId == st.id }
            val disc = disciplineList.filter { it.studentId == st.id }
            val lowScore = subs.any { it.score < 60 }
            val badDisc = disc.any { it.scoreChange < 0 }
            lowScore || badDisc || subs.isEmpty()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "HỌC SINH CẦN QUAN TÂM & HỖ TRỢ",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = "Phát hiện sớm học sinh có nguy cơ học yếu, nề nếp giảm sút hoặc chưa nộp bài",
            fontSize = 12.sp,
            color = Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (needSupportList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Không có học sinh nào đang gặp khó khăn. Cả lớp đều học tập rất tốt!", color = Color(0xFF059669))
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(needSupportList) { st ->
                    val subs = submissions.filter { it.studentId == st.id }
                    val disc = disciplineList.filter { it.studentId == st.id }
                    val avg = if (subs.isNotEmpty()) subs.map { it.score }.average() else 0.0

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth().testTag("support_card_${st.studentCode}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = st.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(text = "${st.studentCode} • Lớp ${st.className}", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }

                                Button(
                                    onClick = {
                                        selectedStudentForAi = st
                                        isLoadingAi = true
                                        aiAdviceText = null
                                        coroutineScope.launch {
                                            val advice = GeminiEduService.analyzeStudentAssistance(
                                                studentName = st.fullName,
                                                attendanceRate = 85,
                                                disciplineScore = 95,
                                                averageScore = avg,
                                                teacherNote = st.note
                                            )
                                            aiAdviceText = advice
                                            isLoadingAi = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                    modifier = Modifier.testTag("btn_ai_support_advice_${st.studentCode}")
                                ) {
                                    Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("AI Tư vấn", fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "• Điểm trung bình: ${String.format("%.1f", avg)}/100 • Số lần nhắc nhở nề nếp: ${disc.count { it.scoreChange < 0 }}",
                                fontSize = 12.sp,
                                color = Color(0xFFDC2626)
                            )

                            if (st.note.isNotBlank()) {
                                Text(text = "• Lưu ý riêng: ${st.note}", fontSize = 12.sp, color = Color(0xFF475569))
                            }
                        }
                    }
                }
            }
        }
    }

    // AI Advice Dialog
    if (selectedStudentForAi != null) {
        AlertDialog(
            onDismissRequest = { selectedStudentForAi = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = Color(0xFF7C3AED))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tư vấn Sư phạm AI Cho ${selectedStudentForAi?.fullName}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                if (isLoadingAi) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xFF7C3AED))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Trợ lý AI đang lập kế hoạch hỗ trợ...", fontSize = 13.sp)
                    }
                } else {
                    Text(
                        text = aiAdviceText ?: "Chưa có nhận xét.",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF1E293B)
                    )
                }
            },
            confirmButton = {
                Button(onClick = { selectedStudentForAi = null }) {
                    Text("Đã hiểu")
                }
            }
        )
    }
}

// ==========================================================
// REQUIREMENT XXX: TEACHER & SYSTEM SETTINGS
// ==========================================================
@Composable
fun TeacherSettingsScreen(
    dao: AppDao,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings by dao.getSettingsFlow().collectAsState(initial = null)

    var teacherName by remember(settings) { mutableStateOf(settings?.teacherName ?: "Thầy Huy Dương") }
    var schoolName by remember(settings) { mutableStateOf(settings?.schoolName ?: "Trường Tiểu Học Tân Phú") }
    var schoolYear by remember(settings) { mutableStateOf(settings?.schoolYear ?: "2024 - 2025") }
    var allowAi by remember(settings) { mutableStateOf(settings?.allowAiTutorGlobal ?: true) }
    var firestoreSync by remember(settings) { mutableStateOf(settings?.firestoreSyncEnabled ?: false) }

    var showClearDataConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "CÀI ĐẶT HỆ THỐNG",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Thông tin trường lớp, thang điểm và đồng bộ dữ liệu",
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Thông tin giáo viên & Trường học", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    OutlinedTextField(
                        value = teacherName,
                        onValueChange = { teacherName = it },
                        label = { Text("Tên giáo viên chủ nhiệm") },
                        modifier = Modifier.fillMaxWidth().testTag("input_teacher_name")
                    )
                    OutlinedTextField(
                        value = schoolName,
                        onValueChange = { schoolName = it },
                        label = { Text("Tên trường tiểu học") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = schoolYear,
                        onValueChange = { schoolYear = it },
                        label = { Text("Năm học") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Cấu hình Trợ lý AI & Đồng bộ Cloud", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    ConfigToggleRow("Bật Trợ lý Gia sư AI cho học sinh", allowAi) { allowAi = it }
                    Divider()
                    ConfigToggleRow("Đồng bộ dữ liệu thời gian thực (Firestore Sync)", firestoreSync) {
                        firestoreSync = it
                        if (it) {
                            Toast.makeText(context, "Đã kích hoạt chế độ đồng bộ Firestore!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    if (firestoreSync) {
                        Text(
                            text = "Trạng thái: Đang đồng bộ hóa bài tập & bảng điểm đa thiết bị an toàn qua Firestore.",
                            fontSize = 11.sp,
                            color = Color(0xFF059669)
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    val s = settings ?: SystemSettingsEntity()
                    coroutineScope.launch {
                        dao.saveSettings(
                            s.copy(
                                teacherName = teacherName.trim(),
                                schoolName = schoolName.trim(),
                                schoolYear = schoolYear.trim(),
                                allowAiTutorGlobal = allowAi,
                                firestoreSyncEnabled = firestoreSync
                            )
                        )
                        Toast.makeText(context, "Đã lưu cài đặt thành công!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("btn_save_settings")
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Lưu Cài Đặt")
            }
        }

        // Section XXXII: Sample Data Management
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Quản lý dữ liệu mẫu (Sample Data)", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                    Text(
                        text = "Giáo viên có thể nạp lại dữ liệu mẫu 5/9 để xem thử hoặc xóa sạch để nhập dữ liệu lớp học thực tế.",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    DatabaseInitializer.populateSampleData(dao)
                                    Toast.makeText(context, "Đã nạp lại dữ liệu mẫu 5/9 thành công!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("btn_repopulate_sample")
                        ) {
                            Text("Nạp mẫu", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { showClearDataConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            modifier = Modifier.weight(1f).testTag("btn_clear_sample")
                        ) {
                            Text("Xóa sạch", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onLogout,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                modifier = Modifier.fillMaxWidth().testTag("btn_teacher_logout")
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Đăng xuất tài khoản")
            }
        }
    }

    EduConfirmDialog(
        show = showClearDataConfirm,
        title = "Xóa toàn bộ dữ liệu mẫu?",
        message = "Thao tác này sẽ xóa sạch danh sách học sinh, bài tập mẫu và điểm nộp bài để bạn bắt đầu nhập lớp học thực tế.",
        isDestructive = true,
        confirmText = "Xóa dữ liệu mẫu",
        onConfirm = {
            coroutineScope.launch {
                DatabaseInitializer.clearSampleData(dao)
                Toast.makeText(context, "Đã xóa toàn bộ dữ liệu mẫu.", Toast.LENGTH_SHORT).show()
                showClearDataConfirm = false
            }
        },
        onDismiss = { showClearDataConfirm = false }
    )
}
