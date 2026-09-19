package com.example.ui.teacher

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.AppDao
import com.example.data.models.ClassRoomEntity
import com.example.ui.components.EduConfirmDialog
import kotlinx.coroutines.launch

@Composable
fun ClassManagementScreen(
    dao: AppDao,
    onNavigateToStudents: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val classes by dao.getAllClassesFlow().collectAsState(initial = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }
    var editingClass by remember { mutableStateOf<ClassRoomEntity?>(null) }
    var classToDelete by remember { mutableStateOf<ClassRoomEntity?>(null) }

    var className by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("5") }
    var schoolYear by remember { mutableStateOf("2024-2025") }
    var totalStudents by remember { mutableStateOf("35") }
    var note by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingClass = null
                    className = ""
                    grade = "5"
                    schoolYear = "2024-2025"
                    totalStudents = "35"
                    note = ""
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("fab_add_class")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm lớp")
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
                        text = "QUẢN LÝ LỚP HỌC",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Danh sách các lớp giảng dạy và sĩ số",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                }
                Badge(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text("${classes.size} Lớp", modifier = Modifier.padding(6.dp), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (classes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chưa có lớp học nào. Nhấn dấu + để thêm lớp mới.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(classes) { cl ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth().testTag("class_card_${cl.name}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        modifier = Modifier.size(50.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = cl.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = "Lớp ${cl.name} (Khối ${cl.grade})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = "Năm học: ${cl.schoolYear} • Sĩ số dự kiến: ${cl.totalStudents}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )
                                        if (cl.note.isNotBlank()) {
                                            Text(
                                                text = cl.note,
                                                fontSize = 11.sp,
                                                color = Color(0xFF059669)
                                            )
                                        }
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = { onNavigateToStudents(cl.id) },
                                        modifier = Modifier.testTag("btn_view_students_${cl.name}")
                                    ) {
                                        Icon(Icons.Default.People, contentDescription = "Xem học sinh", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(
                                        onClick = {
                                            editingClass = cl
                                            className = cl.name
                                            grade = cl.grade.toString()
                                            schoolYear = cl.schoolYear
                                            totalStudents = cl.totalStudents.toString()
                                            note = cl.note
                                            showAddDialog = true
                                        }
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = Color(0xFF64748B))
                                    }
                                    IconButton(
                                        onClick = { classToDelete = cl }
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

    // Add / Edit Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = if (editingClass == null) "Thêm Lớp Học Mới" else "Chỉnh Sửa Lớp ${editingClass?.name}",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = className,
                        onValueChange = { className = it },
                        label = { Text("Tên lớp (ví dụ: 5/9, 4/2...)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_class_name")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = grade,
                            onValueChange = { grade = it },
                            label = { Text("Khối (1-5)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = totalStudents,
                            onValueChange = { totalStudents = it },
                            label = { Text("Sĩ số") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = schoolYear,
                        onValueChange = { schoolYear = it },
                        label = { Text("Năm học") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Ghi chú (chủ nhiệm, phòng học...)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (className.isBlank()) {
                            Toast.makeText(context, "Vui lòng nhập tên lớp", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val gradeInt = grade.toIntOrNull() ?: 5
                        val totalInt = totalStudents.toIntOrNull() ?: 35
                        val entity = ClassRoomEntity(
                            id = editingClass?.id ?: "class_${className.replace("/", "_").lowercase()}_${System.currentTimeMillis() % 10000}",
                            name = className.trim(),
                            grade = gradeInt,
                            schoolYear = schoolYear.trim(),
                            totalStudents = totalInt,
                            note = note.trim()
                        )
                        coroutineScope.launch {
                            if (editingClass == null) {
                                dao.insertClass(entity)
                                Toast.makeText(context, "Đã thêm lớp thành công!", Toast.LENGTH_SHORT).show()
                            } else {
                                dao.updateClass(entity)
                                Toast.makeText(context, "Đã cập nhật lớp thành công!", Toast.LENGTH_SHORT).show()
                            }
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("dialog_save_class_btn")
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

    // Confirm Delete Dialog
    EduConfirmDialog(
        show = classToDelete != null,
        title = "Xác nhận xóa lớp?",
        message = "Bạn có chắc chắn muốn xóa lớp ${classToDelete?.name}? Thao tác này không thể hoàn tác.",
        isDestructive = true,
        confirmText = "Xóa vĩnh viễn",
        onConfirm = {
            classToDelete?.let { cl ->
                coroutineScope.launch {
                    dao.deleteClass(cl)
                    Toast.makeText(context, "Đã xóa lớp ${cl.name}", Toast.LENGTH_SHORT).show()
                    classToDelete = null
                }
            }
        },
        onDismiss = { classToDelete = null }
    )
}
