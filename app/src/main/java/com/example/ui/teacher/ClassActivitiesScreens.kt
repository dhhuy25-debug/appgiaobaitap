package com.example.ui.teacher

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.database.AppDao
import com.example.data.models.*
import com.example.ui.components.EduBadgeChip
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ==========================================
// REQUIREMENT VI: CHUYÊN CẦN (ATTENDANCE)
// ==========================================
@Composable
fun AttendanceScreen(dao: AppDao) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }
    val classes by dao.getAllClassesFlow().collectAsState(initial = emptyList())
    var selectedClassId by remember { mutableStateOf("class_5_9") }

    val students by dao.getStudentsByClassFlow(selectedClassId).collectAsState(initial = emptyList())
    val attendanceList by dao.getAttendanceFlow(selectedClassId, selectedDate).collectAsState(initial = emptyList())

    val attendanceMap = remember(attendanceList) {
        attendanceList.associateBy { it.studentId }
    }

    val presentCount = students.count { (attendanceMap[it.id]?.status ?: "PRESENT") == "PRESENT" }
    val excusedCount = students.count { attendanceMap[it.id]?.status == "EXCUSED_ABSENCE" }
    val unexcusedCount = students.count { attendanceMap[it.id]?.status == "UNEXCUSED_ABSENCE" }
    val total = students.size

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
                    text = "ĐIỂM DANH CHUYÊN CẦN",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Ngày: $selectedDate",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
            }
            Button(
                onClick = {
                    coroutineScope.launch {
                        val allPresent = students.map { st ->
                            AttendanceEntity(
                                id = "att_${st.id}_$selectedDate",
                                studentId = st.id,
                                classId = st.classId,
                                date = selectedDate,
                                status = "PRESENT",
                                note = ""
                            )
                        }
                        dao.insertAttendanceList(allPresent)
                        Toast.makeText(context, "Đã điểm danh cả lớp có mặt!", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                modifier = Modifier.testTag("btn_mark_all_present")
            ) {
                Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tất cả có mặt", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Summary Bar
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Có mặt", fontSize = 12.sp, color = Color(0xFF059669), fontWeight = FontWeight.Bold)
                    Text("$presentCount/$total", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF059669))
                }
                Divider(modifier = Modifier.height(24.dp).width(1.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Có phép", fontSize = 12.sp, color = Color(0xFFD97706), fontWeight = FontWeight.Bold)
                    Text("$excusedCount", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD97706))
                }
                Divider(modifier = Modifier.height(24.dp).width(1.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Không phép", fontSize = 12.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                    Text("$unexcusedCount", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFDC2626))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(students) { st ->
                val currentRecord = attendanceMap[st.id]
                val currentStatus = currentRecord?.status ?: "PRESENT"

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().testTag("attendance_row_${st.studentCode}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(st.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${st.studentCode} • Lớp ${st.className}", fontSize = 12.sp, color = Color(0xFF64748B))
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AttendanceStatusButton("Có mặt", currentStatus == "PRESENT", Color(0xFF059669)) {
                                coroutineScope.launch {
                                    dao.insertAttendance(
                                        AttendanceEntity("att_${st.id}_$selectedDate", st.id, st.classId, selectedDate, "PRESENT", "")
                                    )
                                }
                            }
                            AttendanceStatusButton("Phép", currentStatus == "EXCUSED_ABSENCE", Color(0xFFD97706)) {
                                coroutineScope.launch {
                                    dao.insertAttendance(
                                        AttendanceEntity("att_${st.id}_$selectedDate", st.id, st.classId, selectedDate, "EXCUSED_ABSENCE", "Có phép")
                                    )
                                }
                            }
                            AttendanceStatusButton("Vắng", currentStatus == "UNEXCUSED_ABSENCE", Color(0xFFDC2626)) {
                                coroutineScope.launch {
                                    dao.insertAttendance(
                                        AttendanceEntity("att_${st.id}_$selectedDate", st.id, st.classId, selectedDate, "UNEXCUSED_ABSENCE", "Không phép")
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

@Composable
fun AttendanceStatusButton(label: String, selected: Boolean, color: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onClick() },
        color = if (selected) color else color.copy(alpha = 0.1f),
        contentColor = if (selected) Color.White else color
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

// ==========================================
// REQUIREMENT VII: NỀ NẾP (DISCIPLINE)
// ==========================================
@Composable
fun DisciplineScreen(dao: AppDao) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    val students by dao.getAllStudentsFlow().collectAsState(initial = emptyList())
    val disciplineList by dao.getAllDisciplineFlow().collectAsState(initial = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedStudentId by remember { mutableStateOf(if (students.isNotEmpty()) students.first().id else "") }
    var selectedCriteria by remember { mutableStateOf("Đi học đúng giờ (+1đ)") }
    var note by remember { mutableStateOf("") }

    val criteriaOptions = listOf(
        "Đi học đúng giờ (+1đ)" to 1,
        "Làm bài tập đầy đủ (+2đ)" to 2,
        "Giữ trật tự tốt (+1đ)" to 1,
        "Phát biểu xây dựng bài (+2đ)" to 2,
        "Giúp đỡ bạn bè (+2đ)" to 2,
        "Nói chuyện riêng (-1đ)" to -1,
        "Đi học muộn (-1đ)" to -1,
        "Quên làm bài tập (-2đ)" to -2,
        "Mất trật tự trong lớp (-2đ)" to -2,
        "Không mang sách vở (-1đ)" to -1
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("fab_add_discipline")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm ghi nhận")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "THEO DÕI NỀ NẾP LỚP HỌC",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "10 tiêu chí chấm điểm nề nếp & rèn luyện của học sinh",
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (disciplineList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chưa có ghi nhận nề nếp nào.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(disciplineList) { disc ->
                        val student = students.find { it.id == disc.studentId }
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (disc.scoreChange > 0) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (disc.scoreChange > 0) "+${disc.scoreChange}" else "${disc.scoreChange}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (disc.scoreChange > 0) Color(0xFF059669) else Color(0xFFDC2626)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = student?.fullName ?: "Học sinh",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "${disc.criteria} • ${disc.date}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )
                                        if (disc.reason.isNotBlank()) {
                                            Text(text = disc.reason, fontSize = 11.sp, color = Color(0xFF2563EB))
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            dao.deleteDiscipline(disc)
                                            Toast.makeText(context, "Đã xóa ghi nhận", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Xóa", tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Ghi nhận nề nếp học sinh", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        Text("Chọn học sinh:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        var expanded by remember { mutableStateOf(false) }
                        val currentStudent = students.find { it.id == selectedStudentId }
                        Box {
                            OutlinedButton(
                                onClick = { expanded = true },
                                modifier = Modifier.fillMaxWidth().testTag("select_student_disc_btn")
                            ) {
                                Text(currentStudent?.fullName ?: "Chọn học sinh")
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                students.forEach { st ->
                                    DropdownMenuItem(
                                        text = { Text("${st.studentCode} - ${st.fullName}") },
                                        onClick = {
                                            selectedStudentId = st.id
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text("Tiêu chí rèn luyện:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        criteriaOptions.forEach { (label, _) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedCriteria = label }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedCriteria == label,
                                    onClick = { selectedCriteria = label }
                                )
                                Text(label, fontSize = 13.sp)
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("Lý do / Chi tiết cụ thể") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val student = students.find { it.id == selectedStudentId } ?: students.firstOrNull()
                        if (student == null) return@Button
                        val scoreChange = criteriaOptions.find { it.first == selectedCriteria }?.second ?: 1

                        val disc = DisciplineEntity(
                            id = "disc_${System.currentTimeMillis()}",
                            studentId = student.id,
                            classId = student.classId,
                            date = today,
                            criteria = selectedCriteria.substringBefore(" ("),
                            scoreChange = scoreChange,
                            reason = note.trim(),
                            note = ""
                        )
                        coroutineScope.launch {
                            dao.insertDiscipline(disc)
                            Toast.makeText(context, "Đã ghi nhận nề nếp thành công!", Toast.LENGTH_SHORT).show()
                            showAddDialog = false
                            note = ""
                        }
                    }
                ) {
                    Text("Lưu ghi nhận")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

// ==========================================
// REQUIREMENT VIII: KHEN THƯỞNG (REWARDS)
// ==========================================
@Composable
fun RewardsScreen(dao: AppDao) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    val students by dao.getAllStudentsFlow().collectAsState(initial = emptyList())
    val rewards by dao.getAllRewardsFlow().collectAsState(initial = emptyList())

    var showRewardDialog by remember { mutableStateOf(false) }
    var selectedStudentId by remember { mutableStateOf(if (students.isNotEmpty()) students.first().id else "") }
    var selectedBadge by remember { mutableStateOf("DILIGENT") }
    var reason by remember { mutableStateOf("") }
    var bonusPoints by remember { mutableStateOf("10") }

    val badges = listOf(
        "DILIGENT" to ("⭐ Chăm chỉ" to Color(0xFFF59E0B)),
        "ACHIEVEMENT" to ("🏆 Thành tích tốt" to Color(0xFF3B82F6)),
        "PROGRESS" to ("🌟 Tiến bộ" to Color(0xFF10B981)),
        "CREATIVE" to ("💡 Sáng tạo" to Color(0xFF8B5CF6)),
        "COOPERATION" to ("🤝 Hợp tác" to Color(0xFFEC4899))
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showRewardDialog = true },
                containerColor = Color(0xFFD97706),
                modifier = Modifier.testTag("fab_award_badge")
            ) {
                Icon(Icons.Default.MilitaryTech, contentDescription = "Tặng huy hiệu", tint = Color.White)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "KHEN THƯỞNG & HUY HIỆU",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFD97706)
            )
            Text(
                text = "Tuyên dương, tặng sao và huy hiệu khích lệ học sinh",
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (rewards.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chưa có lượt khen thưởng nào.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(rewards) { rew ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth().testTag("reward_card_${rew.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    EduBadgeChip(rew.badgeType)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = rew.studentName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(text = rew.rewardName, fontSize = 13.sp, color = Color(0xFFD97706), fontWeight = FontWeight.SemiBold)
                                        if (rew.reason.isNotBlank()) {
                                            Text(text = "Lý do: ${rew.reason}", fontSize = 11.sp, color = Color(0xFF475569))
                                        }
                                        Text(text = "Ngày: ${rew.date} • Điểm cộng: +${rew.points}đ", fontSize = 11.sp, color = Color(0xFF059669))
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            dao.deleteReward(rew)
                                            Toast.makeText(context, "Đã thu hồi khen thưởng", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Xóa", tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRewardDialog) {
        AlertDialog(
            onDismissRequest = { showRewardDialog = false },
            title = { Text("Tặng Huy Hiệu & Khen Thưởng", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        Text("Chọn học sinh:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        var expanded by remember { mutableStateOf(false) }
                        val currentStudent = students.find { it.id == selectedStudentId }
                        Box {
                            OutlinedButton(
                                onClick = { expanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(currentStudent?.fullName ?: "Chọn học sinh")
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                students.forEach { st ->
                                    DropdownMenuItem(
                                        text = { Text("${st.studentCode} - ${st.fullName}") },
                                        onClick = {
                                            selectedStudentId = st.id
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text("Chọn huy hiệu:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        badges.forEach { (type, pair) ->
                            val (label, _) = pair
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedBadge = type }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = selectedBadge == type, onClick = { selectedBadge = type })
                                Text(label, fontSize = 14.sp)
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = reason,
                            onValueChange = { reason = it },
                            label = { Text("Lý do khen thưởng") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = bonusPoints,
                            onValueChange = { bonusPoints = it },
                            label = { Text("Điểm thưởng cộng thêm") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val student = students.find { it.id == selectedStudentId } ?: students.firstOrNull()
                        if (student == null) return@Button
                        val pts = bonusPoints.toIntOrNull() ?: 10
                        val badgeTitle = badges.find { it.first == selectedBadge }?.second?.first ?: "Huy hiệu"

                        val rew = RewardEntity(
                            id = "rew_${System.currentTimeMillis()}",
                            studentId = student.id,
                            studentName = student.fullName,
                            classId = student.classId,
                            date = today,
                            rewardName = badgeTitle,
                            reason = reason.trim(),
                            points = pts,
                            badgeType = selectedBadge
                        )
                        coroutineScope.launch {
                            dao.insertReward(rew)
                            Toast.makeText(context, "Đã trao huy hiệu thành công!", Toast.LENGTH_SHORT).show()
                            showRewardDialog = false
                            reason = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                ) {
                    Text("Trao huy hiệu")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRewardDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

// ==========================================
// REQUIREMENT IX: HỒ SƠ LƯU Ý (STUDENT PROFILE NOTES)
// ==========================================
@Composable
fun StudentProfileNotesScreen(dao: AppDao) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val students by dao.getAllStudentsFlow().collectAsState(initial = emptyList())

    var selectedStudent by remember { mutableStateOf<StudentEntity?>(null) }
    var currentNote by remember { mutableStateOf<StudentNoteEntity?>(null) }

    LaunchedEffect(students) {
        if (selectedStudent == null && students.isNotEmpty()) {
            selectedStudent = students.first()
        }
    }

    LaunchedEffect(selectedStudent) {
        selectedStudent?.let { st ->
            currentNote = dao.getStudentNote(st.id) ?: StudentNoteEntity(
                id = "note_${st.id}",
                studentId = st.id
            )
        }
    }

    var academic by remember(currentNote) { mutableStateOf(currentNote?.academicStatus ?: "") }
    var discipline by remember(currentNote) { mutableStateOf(currentNote?.disciplineStatus ?: "") }
    var attendance by remember(currentNote) { mutableStateOf(currentNote?.attendanceStatus ?: "") }
    var circumstances by remember(currentNote) { mutableStateOf(currentNote?.specialCircumstances ?: "") }
    var peerRelations by remember(currentNote) { mutableStateOf(currentNote?.peerRelations ?: "") }
    var behavior by remember(currentNote) { mutableStateOf(currentNote?.psychologyBehavior ?: "") }
    var parentCoordination by remember(currentNote) { mutableStateOf(currentNote?.parentCoordination ?: "") }
    var teacherComments by remember(currentNote) { mutableStateOf(currentNote?.teacherComments ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "HỒ SƠ THEO DÕI & LƯU Ý HỌC SINH",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Ghi chú bảo mật của giáo viên chủ nhiệm về từng học sinh",
            fontSize = 13.sp,
            color = Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Student selector chips
        LazyColumn(
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            // horizontal scroll row
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    students.forEach { st ->
                        FilterChip(
                            selected = selectedStudent?.id == st.id,
                            onClick = { selectedStudent = st },
                            label = { Text("${st.studentCode} - ${st.fullName}") }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                OutlinedTextField(
                    value = academic,
                    onValueChange = { academic = it },
                    label = { Text("1. Tình hình học tập") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = discipline,
                    onValueChange = { discipline = it },
                    label = { Text("2. Nề nếp, kỷ luật trên lớp") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = attendance,
                    onValueChange = { attendance = it },
                    label = { Text("3. Chuyên cần, đi học đúng giờ") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = circumstances,
                    onValueChange = { circumstances = it },
                    label = { Text("4. Hoàn cảnh đặc biệt (gia đình, kinh tế...)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = peerRelations,
                    onValueChange = { peerRelations = it },
                    label = { Text("5. Mối quan hệ với bạn bè") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = behavior,
                    onValueChange = { behavior = it },
                    label = { Text("6. Tâm lý, tính cách, hành vi") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = parentCoordination,
                    onValueChange = { parentCoordination = it },
                    label = { Text("7. Tình hình phối hợp cùng phụ huynh") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = teacherComments,
                    onValueChange = { teacherComments = it },
                    label = { Text("8. Nhận xét chung của Thầy Huy Dương") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Button(
                    onClick = {
                        val st = selectedStudent ?: return@Button
                        val noteEntity = StudentNoteEntity(
                            id = "note_${st.id}",
                            studentId = st.id,
                            academicStatus = academic,
                            disciplineStatus = discipline,
                            attendanceStatus = attendance,
                            specialCircumstances = circumstances,
                            peerRelations = peerRelations,
                            psychologyBehavior = behavior,
                            parentCoordination = parentCoordination,
                            teacherComments = teacherComments,
                            updatedAt = System.currentTimeMillis()
                        )
                        coroutineScope.launch {
                            dao.insertStudentNote(noteEntity)
                            Toast.makeText(context, "Đã lưu hồ sơ học sinh ${st.fullName}!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("save_student_notes_btn")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Lưu Hồ Sơ Lưu Ý")
                }
            }
        }
    }
}
