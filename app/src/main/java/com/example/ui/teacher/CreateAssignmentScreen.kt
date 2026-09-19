package com.example.ui.teacher

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.ai.GeneratedQuestionItem
import com.example.data.database.AppDao
import com.example.data.models.AssignmentEntity
import com.example.data.models.QuestionEntity
import kotlinx.coroutines.launch
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAssignmentScreen(
    dao: AppDao,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 0 = General Info, 1 = Question Editor & AI, 2 = Settings & Publish
    var currentStep by remember { mutableStateOf(0) }

    // General Info
    var title by remember { mutableStateOf("Ôn tập kiến thức tuần 12") }
    var subject by remember { mutableStateOf("Khoa học") }
    var grade by remember { mutableStateOf("5") }
    var topic by remember { mutableStateOf("Chất và năng lượng") }
    var contentDesc by remember { mutableStateOf("Ôn tập các dạng năng lượng và sự biến đổi hóa học.") }
    var objectives by remember { mutableStateOf("Học sinh phân biệt được biến đổi lý học và hóa học.") }
    var durationMinutes by remember { mutableStateOf("15") }
    var maxAttempts by remember { mutableStateOf("2") }

    // Settings
    var shuffleQuestions by remember { mutableStateOf(true) }
    var shuffleAnswers by remember { mutableStateOf(true) }
    var showAnswersAfterSubmit by remember { mutableStateOf(true) }
    var allowAiTutor by remember { mutableStateOf(true) }
    var releaseImmediately by remember { mutableStateOf(true) }
    var scoringRule by remember { mutableStateOf("HIGHEST") } // HIGHEST, LATEST, AVERAGE

    // Questions State
    val questionsList = remember {
        mutableStateListOf(
            QuestionEntity(
                id = "q_init_1",
                assignmentId = "",
                orderIndex = 1,
                type = "SINGLE_CHOICE",
                prompt = "Hiện tượng nào sau đây là sự biến đổi hóa học?",
                optionsJson = "[\"A. Đinh sắt bị gỉ sét ngoài không khí\", \"B. Nước lỏng đóng băng\", \"C. Cắt tờ giấy\", \"D. Hòa tan đường\"]",
                correctAnswer = "A",
                explanation = "Gỉ sét tạo thành chất mới (oxit sắt), là biến đổi hóa học.",
                points = 20,
                difficulty = "COMPREHENSION"
            ),
            QuestionEntity(
                id = "q_init_2",
                assignmentId = "",
                orderIndex = 2,
                type = "TRUE_FALSE",
                prompt = "Năng lượng mặt trời là nguồn năng lượng sạch và vô tận.",
                optionsJson = "[\"Đúng\", \"Sai\"]",
                correctAnswer = "Đúng",
                explanation = "Mặt trời cung cấp ánh sáng và nhiệt năng tự nhiên, không gây ô nhiễm.",
                points = 20,
                difficulty = "RECOGNITION"
            )
        )
    }

    // AI Generation State
    var showAiModal by remember { mutableStateOf(false) }
    var showDocModal by remember { mutableStateOf(false) }
    var isGeneratingAi by remember { mutableStateOf(false) }
    var aiQuestionCount by remember { mutableStateOf("5") }
    var aiDifficulty by remember { mutableStateOf("Thông hiểu") }
    var aiAssignType by remember { mutableStateOf("Ôn tập") }
    var documentText by remember { mutableStateOf("") }

    val subjects = listOf("Khoa học", "Toán", "Tiếng Việt", "Lịch sử & Địa lý", "Tin học", "Đạo đức", "Tiếng Anh")
    val questionTypes = listOf(
        "SINGLE_CHOICE" to "Trắc nghiệm 1 đáp án",
        "MULTI_CHOICE" to "Trắc nghiệm nhiều đáp án",
        "TRUE_FALSE" to "Đúng / Sai",
        "FILL_BLANK" to "Điền khuyết",
        "SHORT_ANSWER" to "Trả lời ngắn"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Step indicator header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TẠO BÀI TẬP MỚI",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = when (currentStep) {
                        0 -> "Bước 1: Thông tin cơ bản bài học"
                        1 -> "Bước 2: Soạn câu hỏi & Trợ lý AI (${questionsList.size} câu)"
                        else -> "Bước 3: Cấu hình thi & Xuất bản"
                    },
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StepIndicator(1, currentStep == 0)
                StepIndicator(2, currentStep == 1)
                StepIndicator(3, currentStep == 2)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (currentStep) {
            // STEP 0: GENERAL INFO
            0 -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Tiêu đề bài tập *") },
                            modifier = Modifier.fillMaxWidth().testTag("input_assignment_title")
                        )
                    }
                    item {
                        Text("Môn học:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            subjects.take(4).forEach { sb ->
                                FilterChip(
                                    selected = subject == sb,
                                    onClick = { subject = sb },
                                    label = { Text(sb, fontSize = 12.sp) }
                                )
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            subjects.drop(4).forEach { sb ->
                                FilterChip(
                                    selected = subject == sb,
                                    onClick = { subject = sb },
                                    label = { Text(sb, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = grade,
                                onValueChange = { grade = it },
                                label = { Text("Khối lớp (1-5)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = durationMinutes,
                                onValueChange = { durationMinutes = it },
                                label = { Text("Thời gian (phút)") },
                                modifier = Modifier.weight(1.2f)
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = topic,
                            onValueChange = { topic = it },
                            label = { Text("Bài / Chủ đề bài học") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = contentDesc,
                            onValueChange = { contentDesc = it },
                            label = { Text("Nội dung kiến thức trọng tâm") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = objectives,
                            onValueChange = { objectives = it },
                            label = { Text("Mục tiêu cần đạt cho học sinh") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Button(
                            onClick = {
                                if (title.isBlank()) {
                                    Toast.makeText(context, "Vui lòng nhập tiêu đề bài tập", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                currentStep = 1
                            },
                            modifier = Modifier.fillMaxWidth().testTag("btn_goto_step_2")
                        ) {
                            Text("Tiếp theo: Soạn câu hỏi")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }

            // STEP 1: QUESTIONS & AI GENERATOR
            1 -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // AI Generation Banner & Actions
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF1D4ED8))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Trợ lý AI Soạn Đề", fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8))
                                }
                                Text("Gemini AI", fontSize = 11.sp, color = Color(0xFF1D4ED8), fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { showAiModal = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                    modifier = Modifier.weight(1f).testTag("btn_open_ai_modal")
                                ) {
                                    Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("AI Tạo câu hỏi", fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { showDocModal = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                    modifier = Modifier.weight(1f).testTag("btn_open_doc_modal")
                                ) {
                                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Từ tài liệu", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Danh sách câu hỏi (${questionsList.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        OutlinedButton(
                            onClick = {
                                val nextIndex = questionsList.size + 1
                                questionsList.add(
                                    QuestionEntity(
                                        id = "q_new_${System.currentTimeMillis()}",
                                        assignmentId = "",
                                        orderIndex = nextIndex,
                                        type = "SINGLE_CHOICE",
                                        prompt = "Câu hỏi số $nextIndex: ...",
                                        optionsJson = "[\"A. Lựa chọn 1\", \"B. Lựa chọn 2\", \"C. Lựa chọn 3\", \"D. Lựa chọn 4\"]",
                                        correctAnswer = "A",
                                        explanation = "Giải thích câu hỏi.",
                                        points = 10,
                                        difficulty = "COMPREHENSION"
                                    )
                                )
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Thêm câu thủ công", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(questionsList) { index, q ->
                            QuestionEditCard(
                                index = index + 1,
                                question = q,
                                onUpdate = { updated -> questionsList[index] = updated },
                                onDelete = { questionsList.removeAt(index) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { currentStep = 0 },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Quay lại")
                        }
                        Button(
                            onClick = {
                                if (questionsList.isEmpty()) {
                                    Toast.makeText(context, "Bài tập phải có ít nhất 1 câu hỏi", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                currentStep = 2
                            },
                            modifier = Modifier.weight(1f).testTag("btn_goto_step_3")
                        ) {
                            Text("Cấu hình & Xuất bản")
                        }
                    }
                }
            }

            // STEP 2: SETTINGS & PUBLISH
            2 -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    item {
                        Text(
                            text = "Cấu hình làm bài & Trả kết quả",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ConfigToggleRow("Đảo thứ tự câu hỏi khi học sinh làm bài", shuffleQuestions) { shuffleQuestions = it }
                                ConfigToggleRow("Đảo thứ tự các phương án trả lời", shuffleAnswers) { shuffleAnswers = it }
                                ConfigToggleRow("Hiện đáp án và lời giải sau khi nộp bài", showAnswersAfterSubmit) { showAnswersAfterSubmit = it }
                                ConfigToggleRow("Cho phép Trợ lý AI gợi ý khi học sinh chưa hiểu", allowAiTutor) { allowAiTutor = it }
                                ConfigToggleRow("Trả điểm số và kết quả ngay lập tức", releaseImmediately) { releaseImmediately = it }
                            }
                        }
                    }
                    item {
                        Text("Quy tắc tính điểm:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = scoringRule == "HIGHEST",
                                onClick = { scoringRule = "HIGHEST" },
                                label = { Text("Điểm cao nhất") }
                            )
                            FilterChip(
                                selected = scoringRule == "LATEST",
                                onClick = { scoringRule = "LATEST" },
                                label = { Text("Lần gần nhất") }
                            )
                            FilterChip(
                                selected = scoringRule == "AVERAGE",
                                onClick = { scoringRule = "AVERAGE" },
                                label = { Text("Điểm trung bình") }
                            )
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = maxAttempts,
                                onValueChange = { maxAttempts = it },
                                label = { Text("Số lần làm bài (0 = không giới hạn)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = "2025-12-31 23:59",
                                onValueChange = {},
                                label = { Text("Hạn nộp bài") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = {
                                    saveAssignment(
                                        dao, coroutineScope, title, subject, grade, topic, contentDesc, objectives,
                                        durationMinutes, maxAttempts, shuffleQuestions, shuffleAnswers,
                                        showAnswersAfterSubmit, allowAiTutor, releaseImmediately, scoringRule,
                                        questionsList, "DRAFT"
                                    ) {
                                        Toast.makeText(context, "Đã lưu bản nháp thành công!", Toast.LENGTH_SHORT).show()
                                        onFinish()
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("btn_save_draft")
                            ) {
                                Text("Lưu bản nháp")
                            }
                            Button(
                                onClick = {
                                    saveAssignment(
                                        dao, coroutineScope, title, subject, grade, topic, contentDesc, objectives,
                                        durationMinutes, maxAttempts, shuffleQuestions, shuffleAnswers,
                                        showAnswersAfterSubmit, allowAiTutor, releaseImmediately, scoringRule,
                                        questionsList, "PUBLISHED"
                                    ) {
                                        Toast.makeText(context, "Đã xuất bản bài tập cho học sinh!", Toast.LENGTH_SHORT).show()
                                        onFinish()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                modifier = Modifier.weight(1f).testTag("btn_publish_assignment")
                            ) {
                                Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Xuất bản ngay")
                            }
                        }
                    }
                }
            }
        }
    }

    // AI Generation Dialog
    if (showAiModal) {
        AlertDialog(
            onDismissRequest = { if (!isGeneratingAi) showAiModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF2563EB))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Tạo Bài Tập Tự Động", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Trợ lý AI sẽ tạo câu hỏi bám sát chuẩn kiến thức môn $subject Lớp $grade chủ đề: $topic.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    OutlinedTextField(
                        value = aiQuestionCount,
                        onValueChange = { aiQuestionCount = it },
                        label = { Text("Số câu hỏi cần tạo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Mức độ yêu cầu:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Nhận biết", "Thông hiểu", "Vận dụng", "Vận dụng cao").forEach { dif ->
                            FilterChip(
                                selected = aiDifficulty == dif,
                                onClick = { aiDifficulty = dif },
                                label = { Text(dif, fontSize = 10.sp) }
                            )
                        }
                    }
                    if (isGeneratingAi) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Đang tạo câu hỏi thông minh...", fontSize = 13.sp, color = Color(0xFF2563EB))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isGeneratingAi = true
                        coroutineScope.launch {
                            val count = aiQuestionCount.toIntOrNull() ?: 5
                            val generated = GeminiEduService.generateQuestions(
                                subject = subject,
                                grade = grade.toIntOrNull() ?: 5,
                                topic = topic,
                                knowledgeContent = contentDesc,
                                count = count,
                                difficulty = aiDifficulty,
                                assignmentType = aiAssignType
                            )
                            questionsList.clear()
                            generated.forEachIndexed { idx, item ->
                                val opts = JSONArray().apply {
                                    item.options.forEach { put(it) }
                                }
                                questionsList.add(
                                    QuestionEntity(
                                        id = "q_ai_${System.currentTimeMillis()}_$idx",
                                        assignmentId = "",
                                        orderIndex = idx + 1,
                                        type = item.type,
                                        prompt = item.prompt,
                                        optionsJson = opts.toString(),
                                        correctAnswer = item.correctAnswer,
                                        explanation = item.explanation,
                                        points = item.points,
                                        difficulty = item.difficulty
                                    )
                                )
                            }
                            isGeneratingAi = false
                            showAiModal = false
                            Toast.makeText(context, "Đã tạo thành công ${generated.size} câu hỏi!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isGeneratingAi,
                    modifier = Modifier.testTag("dialog_confirm_ai_btn")
                ) {
                    Text("Bắt đầu tạo")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAiModal = false }, enabled = !isGeneratingAi) {
                    Text("Hủy")
                }
            }
        )
    }

    // Document Extractor Dialog
    if (showDocModal) {
        AlertDialog(
            onDismissRequest = { if (!isGeneratingAi) showDocModal = false },
            title = { Text("Tải / Dán Tài Liệu Bài Học", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Dán văn bản bài học hoặc tài liệu để AI trích xuất câu hỏi:", fontSize = 12.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = documentText,
                        onValueChange = { documentText = it },
                        placeholder = { Text("Ví dụ: Đoạn văn khoa học về chu trình nước trong tự nhiên...") },
                        modifier = Modifier.fillMaxWidth().height(140.dp)
                    )
                    if (isGeneratingAi) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (documentText.isBlank()) return@Button
                        isGeneratingAi = true
                        coroutineScope.launch {
                            val items = GeminiEduService.extractQuestionsFromDocument(documentText, 5)
                            questionsList.clear()
                            items.forEachIndexed { idx, item ->
                                val opts = JSONArray().apply { item.options.forEach { put(it) } }
                                questionsList.add(
                                    QuestionEntity(
                                        id = "q_doc_${System.currentTimeMillis()}_$idx",
                                        assignmentId = "",
                                        orderIndex = idx + 1,
                                        type = item.type,
                                        prompt = item.prompt,
                                        optionsJson = opts.toString(),
                                        correctAnswer = item.correctAnswer,
                                        explanation = item.explanation,
                                        points = item.points,
                                        difficulty = item.difficulty
                                    )
                                )
                            }
                            isGeneratingAi = false
                            showDocModal = false
                            Toast.makeText(context, "Đã trích xuất ${items.size} câu hỏi!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isGeneratingAi
                ) {
                    Text("Phân tích")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDocModal = false }, enabled = !isGeneratingAi) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun QuestionEditCard(
    index: Int,
    question: QuestionEntity,
    onUpdate: (QuestionEntity) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFEFF6FF)
                ) {
                    Text(
                        text = "Câu $index • ${question.type}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D4ED8),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error)
                }
            }

            OutlinedTextField(
                value = question.prompt,
                onValueChange = { onUpdate(question.copy(prompt = it)) },
                label = { Text("Nội dung câu hỏi") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = question.correctAnswer,
                    onValueChange = { onUpdate(question.copy(correctAnswer = it)) },
                    label = { Text("Đáp án đúng") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = question.points.toString(),
                    onValueChange = { onUpdate(question.copy(points = it.toIntOrNull() ?: 10)) },
                    label = { Text("Điểm") },
                    modifier = Modifier.weight(0.6f)
                )
            }

            OutlinedTextField(
                value = question.explanation,
                onValueChange = { onUpdate(question.copy(explanation = it)) },
                label = { Text("Lời giải thích chi tiết") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ConfigToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun StepIndicator(step: Int, active: Boolean) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(if (active) MaterialTheme.colorScheme.primary else Color(0xFFCBD5E1)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$step",
            color = if (active) Color.White else Color(0xFF475569),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun saveAssignment(
    dao: AppDao,
    scope: kotlinx.coroutines.CoroutineScope,
    title: String,
    subject: String,
    gradeStr: String,
    topic: String,
    content: String,
    objectives: String,
    durationStr: String,
    maxAttemptsStr: String,
    shuffleQ: Boolean,
    shuffleA: Boolean,
    showAns: Boolean,
    allowAi: Boolean,
    releaseImm: Boolean,
    scoringRule: String,
    questions: List<QuestionEntity>,
    status: String,
    onSuccess: () -> Unit
) {
    val assignId = "assign_${System.currentTimeMillis()}"
    val grade = gradeStr.toIntOrNull() ?: 5
    val duration = durationStr.toIntOrNull() ?: 15
    val attempts = maxAttemptsStr.toIntOrNull() ?: 2

    val entity = AssignmentEntity(
        id = assignId,
        title = title.trim(),
        subject = subject,
        grade = grade,
        topic = topic.trim(),
        content = content.trim(),
        objectives = objectives.trim(),
        durationMinutes = duration,
        deadline = "2025-12-31 23:59",
        maxAttempts = attempts,
        maxScore = 100,
        shuffleQuestions = shuffleQ,
        shuffleAnswers = shuffleA,
        showAnswersAfterSubmit = showAns,
        allowAiTutor = allowAi,
        releaseResultsImmediately = releaseImm,
        scoringRule = scoringRule,
        status = status,
        createdAt = System.currentTimeMillis()
    )

    val questionsWithId = questions.mapIndexed { idx, q ->
        q.copy(id = "q_${assignId}_$idx", assignmentId = assignId, orderIndex = idx + 1)
    }

    scope.launch {
        dao.insertAssignment(entity)
        dao.insertQuestions(questionsWithId)
        onSuccess()
    }
}
