package com.example.ui.teacher

import android.widget.Toast
import androidx.compose.foundation.clickable
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
import com.example.data.models.AssignmentEntity
import com.example.ui.components.EduConfirmDialog
import kotlinx.coroutines.launch

@Composable
fun AssignmentListScreen(
    dao: AppDao,
    onCreateNew: () -> Unit,
    onViewStats: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val assignments by dao.getAllAssignmentsFlow().collectAsState(initial = emptyList())
    val submissions by dao.getAllSubmissionsFlow().collectAsState(initial = emptyList())

    var assignmentToDelete by remember { mutableStateOf<AssignmentEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateNew,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("fab_create_assignment")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tạo bài tập mới")
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
                        text = "QUẢN LÝ BÀI TẬP",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Danh sách bài ôn tập, kiểm tra và phiếu học tập",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                }
                Badge(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text("${assignments.size} Bài", modifier = Modifier.padding(6.dp), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (assignments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chưa có bài tập nào. Nhấn dấu + để tạo bài tập mới.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(assignments) { assign ->
                        val subCount = submissions.count { it.assignmentId == assign.id }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onViewStats(assign.id) }
                                .testTag("assignment_item_${assign.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (assign.status == "PUBLISHED") Color(0xFFECFDF5) else Color(0xFFFEF3C7)
                                    ) {
                                        Text(
                                            text = if (assign.status == "PUBLISHED") "ĐÃ XUẤT BẢN" else "BẢN NHÁP",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (assign.status == "PUBLISHED") Color(0xFF059669) else Color(0xFFD97706),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Text(
                                        text = "${assign.subject} • Lớp ${assign.grade}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = assign.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )

                                if (assign.content.isNotBlank()) {
                                    Text(
                                        text = assign.content,
                                        fontSize = 13.sp,
                                        color = Color(0xFF475569),
                                        maxLines = 2
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text(text = "⏱️ ${assign.durationMinutes} phút", fontSize = 12.sp, color = Color(0xFF64748B))
                                    Text(text = "📝 Đã nộp: $subCount lượt", fontSize = 12.sp, color = Color(0xFF059669), fontWeight = FontWeight.Bold)
                                    if (assign.allowAiTutor) {
                                        Text(text = "🤖 Có Trợ lý AI", fontSize = 12.sp, color = Color(0xFF7C3AED))
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(onClick = { onViewStats(assign.id) }) {
                                        Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Xem thống kê & bài nộp")
                                    }
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                val newStatus = if (assign.status == "PUBLISHED") "DRAFT" else "PUBLISHED"
                                                dao.updateAssignment(assign.copy(status = newStatus))
                                                Toast.makeText(context, "Đã đổi trạng thái sang $newStatus", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    ) {
                                        Icon(
                                            if (assign.status == "PUBLISHED") Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Ẩn/Hiện",
                                            tint = Color(0xFF64748B)
                                        )
                                    }
                                    IconButton(onClick = { assignmentToDelete = assign }) {
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

    EduConfirmDialog(
        show = assignmentToDelete != null,
        title = "Xác nhận xóa bài tập?",
        message = "Bạn có chắc muốn xóa bài '${assignmentToDelete?.title}'? Toàn bộ câu hỏi và kết quả nộp bài của học sinh sẽ bị xóa.",
        isDestructive = true,
        confirmText = "Xóa bài tập",
        onConfirm = {
            assignmentToDelete?.let { assign ->
                coroutineScope.launch {
                    dao.deleteAssignment(assign)
                    dao.deleteQuestionsByAssignment(assign.id)
                    Toast.makeText(context, "Đã xóa bài tập", Toast.LENGTH_SHORT).show()
                    assignmentToDelete = null
                }
            }
        },
        onDismiss = { assignmentToDelete = null }
    )
}
