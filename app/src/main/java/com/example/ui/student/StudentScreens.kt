package com.example.ui.student

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ai.GeminiEduService
import com.example.data.database.AppDao
import com.example.data.models.*
import com.example.ui.components.EduBadgeChip
import com.example.ui.components.EduConfirmDialog
import com.example.ui.components.Top3CertificateDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray

// ==========================================================
// REQUIREMENT XV: STUDENT HOME DASHBOARD
// ==========================================================
@Composable
fun StudentHomeScreen(
    student: StudentEntity,
    dao: AppDao,
    onStartAssignment: (String) -> Unit,
    onViewResult: (String) -> Unit,
    onNavigateToTutor: () -> Unit,
    onLogout: () -> Unit
) {
    val assignments by dao.getPublishedAssignmentsFlow().collectAsState(initial = emptyList())
    val mySubmissions by dao.getSubmissionsForStudentFlow(student.id).collectAsState(initial = emptyList())
    val myRewards by dao.getRewardsForStudentFlow(student.id).collectAsState(initial = emptyList())
    val myDiscipline by dao.getDisciplineForStudentFlow(student.id).collectAsState(initial = emptyList())

    val completedAssignIds = remember(mySubmissions) { mySubmissions.map { it.assignmentId }.toSet() }
    val newAssignments = remember(assignments, completedAssignIds) {
        assignments.filter { it.id !in completedAssignIds }
    }
    val completedAssignments = remember(assignments, completedAssignIds) {
        assignments.filter { it.id in completedAssignIds }
    }

    val totalPoints = 100 + myDiscipline.sumOf { it.scoreChange } + myRewards.sumOf { it.points }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Card (Primary School Friendly)
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("student_welcome_banner"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF2563EB), Color(0xFF3B82F6), Color(0xFF60A5FA))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "HUY DƯƠNG EDU",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFDE68A),
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Xin chào, ${student.fullName}! ✨",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Lớp ${student.className} • Mã HS: ${student.studentCode}",
                                    fontSize = 13.sp,
                                    color = Color(0xFFDBEAFE)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (student.gender == "Nam") "👦" else "👧", fontSize = 28.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Points summary chip
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⭐ Điểm rèn luyện tích lũy: ", fontSize = 12.sp, color = Color.White)
                            Text("$totalPoints điểm", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFDE68A))
                        }
                    }
                }
            }
        }

        // Quick AI Tutor Button
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTutor() }
                    .testTag("btn_ask_ai_tutor_home")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF7C3AED)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Gia sư AI Thầy Huy Dương", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF7C3AED))
                            Text("Chưa hiểu bài? Nhấn để hỏi Trợ lý AI ngay nhé!", fontSize = 12.sp, color = Color(0xFF475569))
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF7C3AED))
                }
            }
        }

        // Section: New Assignments
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📚 BÀI TẬP CẦN LÀM (${newAssignments.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D4ED8)
                )
            }
        }

        if (newAssignments.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("🎉 Tuyệt vời! Em đã hoàn thành hết tất cả bài tập.", color = Color(0xFF059669), fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            items(newAssignments) { assign ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("assignment_card_${assign.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFEFF6FF)
                            ) {
                                Text(
                                    text = "${assign.subject} • Khối ${assign.grade}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D4ED8),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Text(text = "⏱️ ${assign.durationMinutes} phút", fontSize = 12.sp, color = Color(0xFF64748B))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = assign.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        if (assign.content.isNotBlank()) {
                            Text(text = assign.content, fontSize = 13.sp, color = Color(0xFF475569), maxLines = 2)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { onStartAssignment(assign.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("btn_start_quiz_${assign.id}")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bắt đầu làm bài", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section: Completed Assignments
        if (completedAssignments.isNotEmpty()) {
            item {
                Text(
                    text = "✅ BÀI TẬP ĐÃ HOÀN THÀNH (${completedAssignments.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF059669)
                )
            }

            items(completedAssignments) { assign ->
                val mySub = mySubmissions.find { it.assignmentId == assign.id }
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().clickable { mySub?.let { onViewResult(it.id) } }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = assign.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Đã nộp • Nhấn để xem lại lời giải", fontSize = 12.sp, color = Color(0xFF64748B))
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFECFDF5)
                        ) {
                            Text(
                                text = "${mySub?.score ?: 0.0} đ",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color(0xFF059669),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section: Badges
        if (myRewards.isNotEmpty()) {
            item {
                Text(
                    text = "🎖️ HUY HIỆU & KHEN THƯỞNG CỦA EM",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD97706)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(myRewards) { rew ->
                        EduBadgeChip(rew.badgeType)
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onLogout,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                modifier = Modifier.fillMaxWidth().testTag("btn_student_logout")
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Đăng xuất tài khoản")
            }
        }
    }
}

// ==========================================================
// REQUIREMENT XVI & XVII & XIX: QUIZ ENGINE WITH AI TUTOR
// ==========================================================
@Composable
fun StudentQuizScreen(
    student: StudentEntity,
    assignmentId: String,
    dao: AppDao,
    onSubmitted: (String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val assignment by dao.getAssignmentByIdFlow(assignmentId).collectAsState(initial = null)
    val questions by dao.getQuestionsForAssignmentFlow(assignmentId).collectAsState(initial = emptyList())

    var currentIndex by remember { mutableStateOf(0) }
    val studentAnswers = remember { mutableStateMapOf<String, String>() }

    // Countdown Timer in seconds
    var remainingSeconds by remember { mutableStateOf(15 * 60) }
    var durationSpentSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(assignment) {
        assignment?.let {
            remainingSeconds = it.durationMinutes * 60
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            if (remainingSeconds > 0) {
                remainingSeconds--
                durationSpentSeconds++
            }
        }
    }

    // AI Elementary Tutor Dialog State
    var showAiTutorModal by remember { mutableStateOf(false) }
    var studentTutorQuery by remember { mutableStateOf("") }
    var aiTutorResponse by remember { mutableStateOf<String?>(null) }
    var isAiTutorLoading by remember { mutableStateOf(false) }

    // Exit and Submit Confirmation Dialogs
    var showExitConfirm by remember { mutableStateOf(false) }
    var showSubmitConfirm by remember { mutableStateOf(false) }

    BackHandler {
        showExitConfirm = true
    }

    val currentQuestion = questions.getOrNull(currentIndex)
    val totalQuestions = questions.size

    val answeredCount = studentAnswers.values.count { it.isNotBlank() }
    val unansweredCount = (totalQuestions - answeredCount).coerceAtLeast(0)

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Quiz Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { showExitConfirm = true }) {
                Icon(Icons.Default.Close, contentDescription = "Thoát")
            }

            // Countdown Timer Pill
            Surface(
                shape = RoundedCornerShape(50),
                color = if (remainingSeconds < 180) Color(0xFFFEE2E2) else Color(0xFFEFF6FF)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        tint = if (remainingSeconds < 180) Color(0xFFDC2626) else Color(0xFF1D4ED8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = timeFormatted,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = if (remainingSeconds < 180) Color(0xFFDC2626) else Color(0xFF1D4ED8)
                    )
                }
            }

            // AI Tutor Button
            Button(
                onClick = {
                    showAiTutorModal = true
                    aiTutorResponse = null
                    studentTutorQuery = ""
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                shape = RoundedCornerShape(50),
                modifier = Modifier.testTag("btn_ask_ai_tutor_quiz")
            ) {
                Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Hỏi AI", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Question Navigation Bubbles
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(questions) { idx, q ->
                val isAnswered = !studentAnswers[q.id].isNullOrBlank()
                val isCurrent = idx == currentIndex
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCurrent -> MaterialTheme.colorScheme.primary
                                isAnswered -> Color(0xFF10B981)
                                else -> Color(0xFFE2E8F0)
                            }
                        )
                        .clickable { currentIndex = idx },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${idx + 1}",
                        color = if (isCurrent || isAnswered) Color.White else Color(0xFF475569),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (currentQuestion != null) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Question Header Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth().testTag("quiz_question_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Câu ${currentIndex + 1} / $totalQuestions",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${currentQuestion.points} điểm",
                                    fontSize = 12.sp,
                                    color = Color(0xFFD97706),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentQuestion.prompt,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A),
                                lineHeight = 24.sp
                            )
                        }
                    }
                }

                // Options / Input depending on Question Type
                item {
                    val currentAnswer = studentAnswers[currentQuestion.id] ?: ""

                    when (currentQuestion.type) {
                        "SINGLE_CHOICE", "TRUE_FALSE" -> {
                            val options = parseOptionsJson(currentQuestion.optionsJson)
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                options.forEach { opt ->
                                    val optKey = if (opt.startsWith("A.") || opt.startsWith("B.") || opt.startsWith("C.") || opt.startsWith("D.")) {
                                        opt.substringBefore(".")
                                    } else opt

                                    val isSelected = currentAnswer == optKey || currentAnswer == opt
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) Color(0xFFEFF6FF) else MaterialTheme.colorScheme.surface
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.5.dp,
                                            if (isSelected) Color(0xFF2563EB) else Color(0xFFE2E8F0)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { studentAnswers[currentQuestion.id] = optKey }
                                            .testTag("option_${optKey}")
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { studentAnswers[currentQuestion.id] = optKey }
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = opt,
                                                fontSize = 15.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color(0xFF1D4ED8) else Color(0xFF1E293B)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        "MULTI_CHOICE" -> {
                            val options = parseOptionsJson(currentQuestion.optionsJson)
                            val selectedList = currentAnswer.split(",").map { it.trim() }.filter { it.isNotBlank() }.toMutableSet()

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                options.forEach { opt ->
                                    val optKey = if (opt.contains(".")) opt.substringBefore(".") else opt
                                    val isSelected = selectedList.contains(optKey)

                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) Color(0xFFEFF6FF) else MaterialTheme.colorScheme.surface
                                        ),
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            if (isSelected) selectedList.remove(optKey) else selectedList.add(optKey)
                                            studentAnswers[currentQuestion.id] = selectedList.sorted().joinToString(", ")
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = { chk ->
                                                    if (chk) selectedList.add(optKey) else selectedList.remove(optKey)
                                                    studentAnswers[currentQuestion.id] = selectedList.sorted().joinToString(", ")
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = opt, fontSize = 15.sp)
                                        }
                                    }
                                }
                            }
                        }

                        "FILL_BLANK", "SHORT_ANSWER" -> {
                            Column {
                                Text("Nhập câu trả lời của em:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = currentAnswer,
                                    onValueChange = { studentAnswers[currentQuestion.id] = it },
                                    placeholder = { Text("Gõ câu trả lời vào đây...") },
                                    modifier = Modifier.fillMaxWidth().height(120.dp).testTag("input_student_quiz_answer")
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Navigation Footer: Previous, Next, and Submit
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { if (currentIndex > 0) currentIndex-- },
                enabled = currentIndex > 0,
                modifier = Modifier.testTag("btn_quiz_prev")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Câu trước")
            }

            if (currentIndex < totalQuestions - 1) {
                Button(
                    onClick = { currentIndex++ },
                    modifier = Modifier.testTag("btn_quiz_next")
                ) {
                    Text("Câu sau")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            } else {
                Button(
                    onClick = { showSubmitConfirm = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    modifier = Modifier.testTag("btn_submit_quiz")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nộp bài thi", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Requirement XIX: AI Elementary Tutor Dialog
    if (showAiTutorModal) {
        Dialog(onDismissRequest = { showAiTutorModal = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("ai_tutor_dialog")
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🤖", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Trợ lý Gia sư AI Thầy Huy Dương", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF7C3AED))
                        }
                        IconButton(onClick = { showAiTutorModal = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Đóng")
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFFBEB)
                    ) {
                        Text(
                            text = "💡 Lưu ý: Trợ lý AI sẽ gợi ý hướng suy nghĩ và phương pháp, không đưa đáp án trực tiếp để em tự rèn luyện nhé!",
                            fontSize = 11.sp,
                            color = Color(0xFFB45309),
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    if (aiTutorResponse != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF5F3FF)
                        ) {
                            Text(
                                text = aiTutorResponse ?: "",
                                fontSize = 14.sp,
                                color = Color(0xFF1E293B),
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = studentTutorQuery,
                        onValueChange = { studentTutorQuery = it },
                        placeholder = { Text("Em chưa hiểu chỗ nào trong câu hỏi này?") },
                        modifier = Modifier.fillMaxWidth().testTag("input_tutor_query")
                    )

                    Button(
                        onClick = {
                            isAiTutorLoading = true
                            coroutineScope.launch {
                                val opts = currentQuestion?.let { parseOptionsJson(it.optionsJson) } ?: emptyList()
                                val response = GeminiEduService.getTutorHint(
                                    questionPrompt = currentQuestion?.prompt ?: "",
                                    options = opts,
                                    studentQuery = studentTutorQuery
                                )
                                aiTutorResponse = response
                                isAiTutorLoading = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        enabled = !isAiTutorLoading,
                        modifier = Modifier.fillMaxWidth().testTag("btn_send_tutor_query")
                    ) {
                        if (isAiTutorLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                        } else {
                            Text("Hỏi Trợ lý AI")
                        }
                    }
                }
            }
        }
    }

    // Submit Confirmation Dialog (Requirement XVIII: Cảnh báo câu chưa làm)
    if (showSubmitConfirm) {
        AlertDialog(
            onDismissRequest = { showSubmitConfirm = false },
            title = { Text("Xác nhận nộp bài thi?", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    if (unansweredCount > 0) {
                        Text(
                            text = "⚠️ Em còn $unansweredCount câu hỏi chưa trả lời!",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text("Em đã trả lời $answeredCount / $totalQuestions câu. Em có chắc chắn muốn nộp bài để Thầy Huy Dương chấm điểm không?")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitConfirm = false
                        submitQuiz(
                            student = student,
                            assignmentId = assignmentId,
                            questions = questions,
                            answersMap = studentAnswers,
                            durationSeconds = durationSpentSeconds,
                            dao = dao,
                            scope = coroutineScope,
                            onSuccess = onSubmitted
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    modifier = Modifier.testTag("btn_dialog_confirm_submit")
                ) {
                    Text("Nộp bài ngay")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSubmitConfirm = false }) {
                    Text("Làm tiếp")
                }
            }
        )
    }

    // Accidental Exit Dialog
    EduConfirmDialog(
        show = showExitConfirm,
        title = "Em có chắc muốn rời khỏi bài thi?",
        message = "Tiến trình làm bài của em đã được tự động lưu lại. Em có thể quay lại làm tiếp bất kỳ lúc nào trước khi hết giờ.",
        confirmText = "Thoát ra",
        cancelText = "Tiếp tục làm",
        onConfirm = {
            showExitConfirm = false
            onCancel()
        },
        onDismiss = { showExitConfirm = false }
    )
}

// ==========================================================
// REQUIREMENT XXI: STUDENT DETAILED RESULT SCREEN
// ==========================================================
@Composable
fun StudentResultScreen(
    submissionId: String,
    dao: AppDao,
    onBackHome: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var submission by remember { mutableStateOf<SubmissionEntity?>(null) }
    var answers by remember { mutableStateOf<List<SubmissionAnswerEntity>>(emptyList()) }
    var questions by remember { mutableStateOf<List<QuestionEntity>>(emptyList()) }

    var selectedQuestionForAiExpl by remember { mutableStateOf<Pair<QuestionEntity, SubmissionAnswerEntity>?>(null) }
    var aiExplanationText by remember { mutableStateOf<String?>(null) }
    var isLoadingExpl by remember { mutableStateOf(false) }

    LaunchedEffect(submissionId) {
        val sub = dao.getSubmissionById(submissionId)
        submission = sub
        if (sub != null) {
            answers = dao.getAnswersForSubmission(sub.id)
            questions = dao.getQuestionsForAssignment(sub.assignmentId)
        }
    }

    val sub = submission

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
            Text(
                text = "KẾT QUẢ BÀI LÀM",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onBackHome) {
                Icon(Icons.Default.Home, contentDescription = "Về trang chủ")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (sub == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val minutes = sub.durationSeconds / 60
            val seconds = sub.durationSeconds % 60

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Score Hero Banner
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (sub.score >= 80) Color(0xFFECFDF5) else if (sub.score >= 50) Color(0xFFEFF6FF) else Color(0xFFFEF2F2)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("result_score_banner")
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (sub.score >= 90) "🎉 Xuất Sắc Lắm!" else if (sub.score >= 70) "👏 Làm Tốt Lắm!" else "💪 Em Hãy Cố Gắng Hơn Nhé!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (sub.score >= 80) Color(0xFF059669) else Color(0xFF1D4ED8)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${sub.score} / 100",
                                fontSize = 38.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (sub.score >= 80) Color(0xFF059669) else Color(0xFF1D4ED8)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "⏱️ Thời gian: ${String.format("%02d:%02d", minutes, seconds)} • Đúng: ${sub.correctCount}/${sub.totalQuestions} câu",
                                fontSize = 13.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Xem lại chi tiết từng câu hỏi & Lời giải",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                // Detailed Review of Each Question
                itemsIndexed(questions) { idx, q ->
                    val ans = answers.find { it.questionId == q.id }
                    val isCorrect = ans?.isCorrect ?: (ans?.studentAnswer == q.correctAnswer)

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Câu ${idx + 1}: ${if (isCorrect) "Đúng (+${q.points}đ)" else "Sai (0đ)"}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isCorrect) Color(0xFF059669) else Color(0xFFDC2626)
                                )

                                // AI Explain Button
                                TextButton(
                                    onClick = {
                                        selectedQuestionForAiExpl = q to (ans ?: SubmissionAnswerEntity("", "", q.id, "", false, 0.0))
                                        isLoadingExpl = true
                                        aiExplanationText = null
                                        coroutineScope.launch {
                                            val expl = GeminiEduService.getPostSubmissionExplanation(
                                                questionPrompt = q.prompt,
                                                correctAnswer = q.correctAnswer,
                                                studentAnswer = ans?.studentAnswer ?: "Chưa làm"
                                            )
                                            aiExplanationText = expl
                                            isLoadingExpl = false
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("AI Giải thích thêm", fontSize = 11.sp)
                                }
                            }

                            Text(text = q.prompt, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "• Em chọn: ${ans?.studentAnswer ?: "Chưa trả lời"}", fontSize = 13.sp, color = if (isCorrect) Color(0xFF059669) else Color(0xFFDC2626))
                            Text(text = "• Đáp án đúng: ${q.correctAnswer}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                            if (q.explanation.isNotBlank()) {
                                Text(text = "💡 Lời giải: ${q.explanation}", fontSize = 12.sp, color = Color(0xFF475569))
                            }
                        }
                    }
                }
            }
        }
    }

    // AI Post-Submission Explanation Dialog
    if (selectedQuestionForAiExpl != null) {
        AlertDialog(
            onDismissRequest = { selectedQuestionForAiExpl = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = Color(0xFF7C3AED))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Trợ lý AI Giải Thích Cặn Kẽ", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                if (isLoadingExpl) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF7C3AED))
                    }
                } else {
                    Text(
                        text = aiExplanationText ?: "Chưa có lời giải thích.",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF1E293B)
                    )
                }
            },
            confirmButton = {
                Button(onClick = { selectedQuestionForAiExpl = null }) {
                    Text("Đã hiểu")
                }
            }
        )
    }
}

// ==========================================================
// DIRECT STUDENT AI TUTOR CHAT
// ==========================================================
@Composable
fun StudentAiTutorScreen(
    student: StudentEntity,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var userQuery by remember { mutableStateOf("") }
    val chatMessages = remember {
        mutableStateListOf(
            "Trợ lý AI" to "Chào em ${student.fullName}! Thầy Huy Dương đã tích hợp Trợ lý Gia sư AI để giúp em giải đáp mọi thắc mắc trong học tập. Em đang gặp khó khăn ở bài học nào nè?"
        )
    }
    var isSending by remember { mutableStateOf(false) }

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
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text("GIA SƯ TIỂU HỌC AI", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF7C3AED))
                Text("Hỗ trợ học tập thông minh 24/7", fontSize = 12.sp, color = Color(0xFF64748B))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chatMessages) { (sender, msg) ->
                val isMe = sender != "Trợ lý AI"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMe) Color(0xFF2563EB) else Color(0xFFF1F5F9)
                        ),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Text(
                            text = msg,
                            fontSize = 14.sp,
                            color = if (isMe) Color.White else Color(0xFF1E293B),
                            modifier = Modifier.padding(12.dp),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userQuery,
                onValueChange = { userQuery = it },
                placeholder = { Text("Nhập câu hỏi bài học...") },
                modifier = Modifier.weight(1f).testTag("input_direct_tutor")
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (userQuery.isNotBlank() && !isSending) {
                        val q = userQuery
                        userQuery = ""
                        chatMessages.add("Em" to q)
                        isSending = true
                        coroutineScope.launch {
                            val reply = GeminiEduService.getTutorHint(
                                questionPrompt = "Học sinh tiểu học hỏi kiến thức",
                                options = emptyList(),
                                studentQuery = q
                            )
                            chatMessages.add("Trợ lý AI" to reply)
                            isSending = false
                        }
                    }
                },
                modifier = Modifier.testTag("btn_send_direct_tutor")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Gửi", tint = Color(0xFF7C3AED))
            }
        }
    }
}

// ==========================================================
// SUBMISSION LOGIC
// ==========================================================
private fun submitQuiz(
    student: StudentEntity,
    assignmentId: String,
    questions: List<QuestionEntity>,
    answersMap: Map<String, String>,
    durationSeconds: Int,
    dao: AppDao,
    scope: kotlinx.coroutines.CoroutineScope,
    onSuccess: (String) -> Unit
) {
    scope.launch {
        var totalEarnedPoints = 0.0
        var correctCount = 0
        var incorrectCount = 0
        val submissionId = "sub_${System.currentTimeMillis()}"

        val answerEntities = mutableListOf<SubmissionAnswerEntity>()

        for (q in questions) {
            val studentAns = answersMap[q.id]?.trim() ?: ""
            val isCorrect: Boolean
            val pts: Double

            when (q.type) {
                "SHORT_ANSWER" -> {
                    val gradeResult = GeminiEduService.gradeShortAnswer(
                        questionPrompt = q.prompt,
                        sampleAnswer = q.correctAnswer,
                        studentAnswer = studentAns,
                        maxPoints = q.points.toDouble()
                    )
                    isCorrect = gradeResult.isCorrect
                    pts = gradeResult.scoreAwarded
                }
                else -> {
                    isCorrect = studentAns.equals(q.correctAnswer.trim(), ignoreCase = true)
                    pts = if (isCorrect) q.points.toDouble() else 0.0
                }
            }

            if (isCorrect) correctCount++ else incorrectCount++
            totalEarnedPoints += pts

            answerEntities.add(
                SubmissionAnswerEntity(
                    id = "ans_${submissionId}_${q.id}",
                    submissionId = submissionId,
                    questionId = q.id,
                    studentAnswer = studentAns,
                    isCorrect = isCorrect,
                    pointsAwarded = pts,
                    feedback = ""
                )
            )
        }

        val submissionEntity = SubmissionEntity(
            id = submissionId,
            assignmentId = assignmentId,
            studentId = student.id,
            studentName = student.fullName,
            className = student.className,
            attemptNumber = 1,
            score = totalEarnedPoints,
            totalQuestions = questions.size,
            correctCount = correctCount,
            incorrectCount = incorrectCount,
            durationSeconds = durationSeconds,
            submittedAt = System.currentTimeMillis(),
            aiFeedbackSummary = if (totalEarnedPoints >= 80) "Xuất sắc! Em tiếp thu bài rất tốt." else "Em cần xem lại các câu sai để nắm vững kiến thức nhé."
        )

        dao.insertSubmission(submissionEntity)
        dao.insertSubmissionAnswers(answerEntities)
        onSuccess(submissionId)
    }
}

private fun parseOptionsJson(jsonStr: String): List<String> {
    val list = mutableListOf<String>()
    try {
        val array = JSONArray(jsonStr)
        for (i in 0 until array.length()) {
            list.add(array.getString(i))
        }
    } catch (e: Exception) {
        if (jsonStr.contains(",")) {
            list.addAll(jsonStr.split(",").map { it.trim() })
        } else if (jsonStr.isNotBlank()) {
            list.add(jsonStr)
        }
    }
    return list
}
