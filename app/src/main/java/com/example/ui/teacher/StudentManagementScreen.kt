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
import androidx.compose.ui.window.Dialog
import com.example.data.database.AppDao
import com.example.data.models.*
import com.example.ui.components.EduBadgeChip
import com.example.ui.components.EduConfirmDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentManagementScreen(
    dao: AppDao,
    initialClassId: String? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val students by dao.getAllStudentsFlow().collectAsState(initial = emptyList())
    val classes by dao.getAllClassesFlow().collectAsState(initial = emptyList())
    val allSubmissions by dao.getAllSubmissionsFlow().collectAsState(initial = emptyList())
    val allRewards by dao.getAllRewardsFlow().collectAsState(initial = emptyList())
    val allDiscipline by dao.getAllDisciplineFlow().collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var selectedClassId by remember { mutableStateOf(initialClassId ?: "ALL") }

    var showAddDialog by remember { mutableStateOf(false) }
    var showBulkDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<StudentEntity?>(null) }
    var studentToDelete by remember { mutableStateOf<StudentEntity?>(null) }
    var viewingStudentDetail by remember { mutableStateOf<StudentEntity?>(null) }

    // Student form state
    var fullName by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("2014-05-15") }
    var gender by remember { mutableStateOf("Nam") }
    var classId by remember { mutableStateOf(if (classes.isNotEmpty()) classes.first().id else "class_5_9") }
    var studentCode by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("123") }
    var parentPhone by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    // Bulk text
    var bulkText by remember { mutableStateOf("") }

    val filteredStudents = students.filter { st ->
        val matchClass = (selectedClassId == "ALL" || st.classId == selectedClassId)
        val matchSearch = st.fullName.contains(searchQuery, ignoreCase = true) ||
                st.studentCode.contains(searchQuery, ignoreCase = true) ||
                st.username.contains(searchQuery, ignoreCase = true)
        matchClass && matchSearch
    }

    Scaffold(
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallFloatingActionButton(
                    onClick = { showBulkDialog = true },
                    containerColor = Color(0xFF059669),
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_bulk_student")
                ) {
                    Icon(Icons.Default.GroupAdd, contentDescription = "Nhập nhiều")
                }
                FloatingActionButton(
                    onClick = {
                        editingStudent = null
                        fullName = ""
                        birthDate = "2014-05-15"
                        gender = "Nam"
                        classId = if (classes.isNotEmpty()) classes.first().id else "class_5_9"
                        val nextNumber = (students.size + 1)
                        studentCode = String.format("HS%03d", nextNumber)
                        username = studentCode.lowercase()
                        password = "123"
                        parentPhone = ""
                        note = ""
                        showAddDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("fab_add_student")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Thêm học sinh")
                }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "QUẢN LÝ HỌC SINH",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Danh sách học sinh, tài khoản và hồ sơ học tập",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                }
                Badge(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text("${filteredStudents.size}/${students.size} HS", modifier = Modifier.padding(6.dp), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search and Class Filter
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Tìm kiếm họ tên, mã HS...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth().testTag("search_student_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedClassId == "ALL",
                    onClick = { selectedClassId = "ALL" },
                    label = { Text("Tất cả lớp") }
                )
                classes.forEach { cl ->
                    FilterChip(
                        selected = selectedClassId == cl.id,
                        onClick = { selectedClassId = cl.id },
                        label = { Text("Lớp ${cl.name}") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredStudents.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Không tìm thấy học sinh nào.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(filteredStudents) { st ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewingStudentDetail = st }
                                .testTag("student_card_${st.studentCode}")
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
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (st.gender == "Nam") Color(0xFFDBEAFE) else Color(0xFFFCE7F3)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (st.gender == "Nam") "👦" else "👧",
                                            fontSize = 20.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = st.fullName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFFF1F5F9)
                                            ) {
                                                Text(
                                                    text = st.studentCode,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF2563EB),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Lớp ${st.className} • TK: ${st.username} • MK: ${st.password}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )
                                        if (st.parentPhone.isNotBlank()) {
                                            Text(
                                                text = "SĐT PH: ${st.parentPhone}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF059669)
                                            )
                                        }
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            editingStudent = st
                                            fullName = st.fullName
                                            birthDate = st.birthDate
                                            gender = st.gender
                                            classId = st.classId
                                            studentCode = st.studentCode
                                            username = st.username
                                            password = st.password
                                            parentPhone = st.parentPhone
                                            note = st.note
                                            showAddDialog = true
                                        }
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = Color(0xFF64748B))
                                    }
                                    IconButton(
                                        onClick = { studentToDelete = st }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Student Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = if (editingStudent == null) "+ THÊM HỌC SINH" else "SỬA THÔNG TIN HỌC SINH",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    item {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Họ và tên học sinh *") },
                            modifier = Modifier.fillMaxWidth().testTag("input_student_name")
                        )
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = studentCode,
                                onValueChange = { studentCode = it },
                                label = { Text("Mã học sinh *") },
                                modifier = Modifier.weight(1f).testTag("input_student_code")
                            )
                            OutlinedTextField(
                                value = birthDate,
                                onValueChange = { birthDate = it },
                                label = { Text("Ngày sinh (yyyy-MM-dd)") },
                                modifier = Modifier.weight(1.2f)
                            )
                        }
                    }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Giới tính: ", fontSize = 14.sp)
                            RadioButton(selected = gender == "Nam", onClick = { gender = "Nam" })
                            Text("Nam", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            RadioButton(selected = gender == "Nữ", onClick = { gender = "Nữ" })
                            Text("Nữ", fontSize = 14.sp)
                        }
                    }
                    item {
                        Text("Chọn lớp:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            classes.forEach { cl ->
                                FilterChip(
                                    selected = classId == cl.id,
                                    onClick = { classId = cl.id },
                                    label = { Text(cl.name) }
                                )
                            }
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("Tên đăng nhập *") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Mật khẩu *") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = parentPhone,
                            onValueChange = { parentPhone = it },
                            label = { Text("Số điện thoại phụ huynh") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("Ghi chú riêng của giáo viên") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (fullName.isBlank() || studentCode.isBlank() || username.isBlank()) {
                            Toast.makeText(context, "Vui lòng điền đầy đủ các thông tin bắt buộc (*)", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val selectedClass = classes.find { it.id == classId }
                        val classNameVal = selectedClass?.name ?: "5/9"
                        val studentId = editingStudent?.id ?: "std_${System.currentTimeMillis()}"

                        val entity = StudentEntity(
                            id = studentId,
                            studentCode = studentCode.trim().uppercase(),
                            fullName = fullName.trim(),
                            birthDate = birthDate.trim(),
                            gender = gender,
                            classId = classId,
                            className = classNameVal,
                            parentPhone = parentPhone.trim(),
                            note = note.trim(),
                            username = username.trim().lowercase(),
                            password = password.trim()
                        )
                        coroutineScope.launch {
                            dao.insertStudent(entity)
                            dao.insertUser(
                                UserEntity(
                                    id = "user_$studentId",
                                    role = "STUDENT",
                                    username = entity.username,
                                    password = entity.password,
                                    fullName = entity.fullName,
                                    studentCode = entity.studentCode,
                                    classId = entity.classId,
                                    className = entity.className
                                )
                            )
                            Toast.makeText(context, "Đã lưu học sinh thành công!", Toast.LENGTH_SHORT).show()
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("dialog_save_student_btn")
                ) {
                    Text("Lưu lại")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Bulk Add Dialog
    if (showBulkDialog) {
        AlertDialog(
            onDismissRequest = { showBulkDialog = false },
            title = { Text("Nhập Nhiều Học Sinh Cùng Lúc", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Mỗi dòng 1 học sinh: Họ và tên, Giới tính, SĐT phụ huynh (nếu có)",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bulkText,
                        onValueChange = { bulkText = it },
                        placeholder = { Text("Ví dụ:\nNguyễn Văn Tuấn, Nam, 0912345678\nLê Thị Thảo, Nữ, 0987654321") },
                        modifier = Modifier.fillMaxWidth().height(180.dp).testTag("bulk_student_textarea")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (bulkText.isBlank()) return@Button
                        val lines = bulkText.lines().filter { it.isNotBlank() }
                        coroutineScope.launch {
                            var currentCount = students.size
                            val selectedClass = classes.find { it.id == selectedClassId && it.id != "ALL" } ?: classes.firstOrNull()
                            val cId = selectedClass?.id ?: "class_5_9"
                            val cName = selectedClass?.name ?: "5/9"

                            for (line in lines) {
                                currentCount++
                                val parts = line.split(",").map { it.trim() }
                                val name = parts.getOrNull(0) ?: "Học sinh $currentCount"
                                val gen = if (parts.getOrNull(1)?.contains("Nữ", ignoreCase = true) == true) "Nữ" else "Nam"
                                val phone = parts.getOrNull(2) ?: ""
                                val code = String.format("HS%03d", currentCount)
                                val sid = "std_${System.currentTimeMillis()}_$currentCount"

                                val student = StudentEntity(
                                    id = sid,
                                    studentCode = code,
                                    fullName = name,
                                    birthDate = "2014-05-15",
                                    gender = gen,
                                    classId = cId,
                                    className = cName,
                                    parentPhone = phone,
                                    note = "",
                                    username = code.lowercase(),
                                    password = "123"
                                )
                                dao.insertStudent(student)
                                dao.insertUser(
                                    UserEntity(
                                        id = "user_$sid",
                                        role = "STUDENT",
                                        username = student.username,
                                        password = student.password,
                                        fullName = student.fullName,
                                        studentCode = student.studentCode,
                                        classId = student.classId,
                                        className = student.className
                                    )
                                )
                            }
                            Toast.makeText(context, "Đã thêm thành công ${lines.size} học sinh!", Toast.LENGTH_SHORT).show()
                            showBulkDialog = false
                            bulkText = ""
                        }
                    }
                ) {
                    Text("Bắt đầu nhập")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showBulkDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Section XXVIII: 360-degree Student Profile Dialog
    viewingStudentDetail?.let { st ->
        Dialog(onDismissRequest = { viewingStudentDetail = null }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(4.dp).testTag("student_detail_dialog"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFDBEAFE)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(if (st.gender == "Nam") "👦" else "👧", fontSize = 24.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = st.fullName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${st.studentCode} • Lớp ${st.className} • ${st.gender}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                            IconButton(onClick = { viewingStudentDetail = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Đóng")
                            }
                        }
                    }

                    // Holistic overview cards
                    item {
                        val studentSubs = allSubmissions.filter { it.studentId == st.id }
                        val studentRewards = allRewards.filter { it.studentId == st.id }
                        val studentDisc = allDiscipline.filter { it.studentId == st.id }
                        val discScore = 100 + studentDisc.sumOf { it.scoreChange }
                        val avgScore = if (studentSubs.isNotEmpty()) {
                            String.format("%.1f", studentSubs.map { it.score }.average())
                        } else "--"

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProfileStatItem("Điểm TB", "$avgScore/100", Color(0xFF059669), Modifier.weight(1f))
                            ProfileStatItem("Nề nếp", "$discScore đ", Color(0xFF2563EB), Modifier.weight(1f))
                            ProfileStatItem("Khen thưởng", "${studentRewards.size} lượt", Color(0xFFD97706), Modifier.weight(1f))
                        }
                    }

                    item {
                        Divider()
                        Text("Thông tin liên hệ & Đăng nhập", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("• Tên đăng nhập: ${st.username}", fontSize = 13.sp)
                        Text("• Mật khẩu: ${st.password}", fontSize = 13.sp)
                        Text("• Ngày sinh: ${st.birthDate}", fontSize = 13.sp)
                        Text("• Số điện thoại phụ huynh: ${if (st.parentPhone.isNotBlank()) st.parentPhone else "Chưa cập nhật"}", fontSize = 13.sp)
                        if (st.note.isNotBlank()) {
                            Text("• Ghi chú riêng: ${st.note}", fontSize = 13.sp, color = Color(0xFF059669))
                        }
                    }

                    item {
                        Divider()
                        Text("Huy hiệu & Khen thưởng đạt được", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        val studentRewards = allRewards.filter { it.studentId == st.id }
                        if (studentRewards.isEmpty()) {
                            Text("Chưa có khen thưởng nào.", fontSize = 12.sp, color = Color.Gray)
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                studentRewards.take(3).forEach { rew ->
                                    EduBadgeChip(rew.badgeType)
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = { viewingStudentDetail = null },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Đóng hồ sơ")
                        }
                    }
                }
            }
        }
    }

    // Confirm Delete Dialog
    EduConfirmDialog(
        show = studentToDelete != null,
        title = "Xác nhận xóa học sinh?",
        message = "Bạn có chắc muốn xóa học sinh ${studentToDelete?.fullName} (${studentToDelete?.studentCode})? Dữ liệu điểm và bài làm của học sinh sẽ bị xóa.",
        isDestructive = true,
        confirmText = "Xóa học sinh",
        onConfirm = {
            studentToDelete?.let { st ->
                coroutineScope.launch {
                    dao.deleteStudent(st)
                    Toast.makeText(context, "Đã xóa học sinh ${st.fullName}", Toast.LENGTH_SHORT).show()
                    studentToDelete = null
                }
            }
        },
        onDismiss = { studentToDelete = null }
    )
}

@Composable
fun ProfileStatItem(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 11.sp, color = Color(0xFF64748B))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
