package com.example.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.AppDao
import com.example.data.models.*
import com.example.ui.components.EduStatCard
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TeacherDashboardScreen(
    dao: AppDao,
    onNavigate: (String) -> Unit
) {
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val displayDate = remember { SimpleDateFormat("EEEE, dd/MM/yyyy", Locale("vi", "VN")).format(Date()) }

    val students by dao.getAllStudentsFlow().collectAsState(initial = emptyList())
    val assignments by dao.getAllAssignmentsFlow().collectAsState(initial = emptyList())
    val submissions by dao.getAllSubmissionsFlow().collectAsState(initial = emptyList())
    val rewards by dao.getAllRewardsFlow().collectAsState(initial = emptyList())
    val attendanceList by dao.getAttendanceFlow("class_5_9", todayStr).collectAsState(initial = emptyList())

    val totalStudents = students.size
    val presentCount = attendanceList.count { it.status == "PRESENT" }
    val excusedCount = attendanceList.count { it.status == "EXCUSED_ABSENCE" }
    val unexcusedCount = attendanceList.count { it.status == "UNEXCUSED_ABSENCE" }

    val activeAssignmentsCount = assignments.count { it.status == "PUBLISHED" }
    val completedSubmissionsCount = submissions.size
    val avgScore = if (submissions.isNotEmpty()) {
        String.format("%.1f", submissions.map { it.score }.average())
    } else "0.0"

    val needSupportStudents = students.filter { st ->
        val studentSubs = submissions.filter { it.studentId == st.id }
        val lowScore = studentSubs.any { it.score < 50 }
        val absent = attendanceList.any { it.studentId == st.id && it.status == "UNEXCUSED_ABSENCE" }
        lowScore || absent
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Teacher Welcome Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("teacher_welcome_banner"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF1E3A8A), Color(0xFF2563EB), Color(0xFF0284C7))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "HUY DƯƠNG EDU",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFDE68A),
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Xin chào, Thầy Huy Dương!",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.School,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Hôm nay: $displayDate • Lớp chủ nhiệm: 5/9",
                            fontSize = 13.sp,
                            color = Color(0xFFDBEAFE)
                        )
                    }
                }
            }
        }

        // Quick Actions Row
        item {
            Text(
                text = "Thao tác nhanh",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onNavigate("create_assignment") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                    modifier = Modifier.weight(1f).testTag("quick_create_assignment_btn")
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Giao bài tập", fontSize = 13.sp)
                }
                Button(
                    onClick = { onNavigate("attendance") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    modifier = Modifier.weight(1f).testTag("quick_attendance_btn")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Điểm danh", fontSize = 13.sp)
                }
                Button(
                    onClick = { onNavigate("rewards") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                    modifier = Modifier.weight(1f).testTag("quick_reward_btn")
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Khen thưởng", fontSize = 13.sp)
                }
            }
        }

        // Section V: Key Statistical Cards
        item {
            Text(
                text = "Thống kê tổng quan lớp học",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EduStatCard(
                    title = "Tổng số học sinh",
                    value = "$totalStudents em",
                    subtitle = "Lớp 5/9",
                    icon = Icons.Default.Groups,
                    containerColor = Color(0xFFEFF6FF),
                    contentColor = Color(0xFF1D4ED8),
                    modifier = Modifier.weight(1f).testTag("stat_total_students")
                )
                EduStatCard(
                    title = "Có mặt hôm nay",
                    value = "$presentCount/$totalStudents",
                    subtitle = "Tỷ lệ: ${if (totalStudents > 0) (presentCount * 100 / totalStudents) else 0}%",
                    icon = Icons.Default.CheckCircleOutline,
                    containerColor = Color(0xFFECFDF5),
                    contentColor = Color(0xFF059669),
                    modifier = Modifier.weight(1f).testTag("stat_present_today")
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EduStatCard(
                    title = "Vắng có phép",
                    value = "$excusedCount em",
                    subtitle = "Có đơn xin phép",
                    icon = Icons.Default.EventBusy,
                    containerColor = Color(0xFFFFFBEB),
                    contentColor = Color(0xFFD97706),
                    modifier = Modifier.weight(1f).testTag("stat_excused")
                )
                EduStatCard(
                    title = "Vắng không phép",
                    value = "$unexcusedCount em",
                    subtitle = "Cần liên hệ gia đình",
                    icon = Icons.Default.WarningAmber,
                    containerColor = Color(0xFFFEF2F2),
                    contentColor = Color(0xFFDC2626),
                    modifier = Modifier.weight(1f).testTag("stat_unexcused")
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EduStatCard(
                    title = "Bài tập đang giao",
                    value = "$activeAssignmentsCount bài",
                    subtitle = "Đang nhận nộp bài",
                    icon = Icons.Default.Assignment,
                    containerColor = Color(0xFFF5F3FF),
                    contentColor = Color(0xFF7C3AED),
                    modifier = Modifier.weight(1f).testTag("stat_active_assignments")
                )
                EduStatCard(
                    title = "Lượt làm bài",
                    value = "$completedSubmissionsCount lượt",
                    subtitle = "Đã nộp bài chấm điểm",
                    icon = Icons.Default.FactCheck,
                    containerColor = Color(0xFFF0FDF4),
                    contentColor = Color(0xFF16A34A),
                    modifier = Modifier.weight(1f).testTag("stat_completed_submissions")
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EduStatCard(
                    title = "Điểm trung bình",
                    value = "$avgScore/100",
                    subtitle = "Toàn bộ bài tập",
                    icon = Icons.Default.TrendingUp,
                    containerColor = Color(0xFFFDF4FF),
                    contentColor = Color(0xFFC026D3),
                    modifier = Modifier.weight(1f).testTag("stat_avg_score")
                )
                EduStatCard(
                    title = "Khen thưởng",
                    value = "${rewards.size} lượt",
                    subtitle = "Huy hiệu & Sao chăm chỉ",
                    icon = Icons.Default.EmojiEvents,
                    containerColor = Color(0xFFFFF7ED),
                    contentColor = Color(0xFFEA580C),
                    modifier = Modifier.weight(1f).testTag("stat_rewards")
                )
            }
        }

        // Section V: "VIỆC CẦN LÀM" (To-Do List for Teacher)
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("todo_section_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFE11D48))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VIỆC CẦN LÀM HÔM NAY",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE11D48)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    val unsubmittedCount = (totalStudents - submissions.map { it.studentId }.distinct().size).coerceAtLeast(0)
                    TodoItem(
                        icon = Icons.Default.AssignmentLate,
                        color = Color(0xFFD97706),
                        title = "$unsubmittedCount học sinh chưa nộp bài tập mới nhất",
                        actionText = "Nhắc nhở",
                        onClick = { onNavigate("assignments") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TodoItem(
                        icon = Icons.Default.Support,
                        color = Color(0xFFDC2626),
                        title = "${needSupportStudents.size} học sinh cần được hỗ trợ & lưu ý nề nếp",
                        actionText = "Xem ngay",
                        onClick = { onNavigate("support") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TodoItem(
                        icon = Icons.Default.PhoneCallback,
                        color = Color(0xFF2563EB),
                        title = "$unexcusedCount học sinh vắng không phép cần gọi điện phụ huynh",
                        actionText = "Điểm danh",
                        onClick = { onNavigate("attendance") }
                    )
                }
            }
        }

        // Mini charts/visualizers
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("charts_overview_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Phân bố kết quả học tập",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val excellent = submissions.count { it.score >= 90 }
                    val good = submissions.count { it.score in 70.0..89.9 }
                    val average = submissions.count { it.score in 50.0..69.9 }
                    val weak = submissions.count { it.score < 50.0 }
                    val total = submissions.size.coerceAtLeast(1)

                    ProgressBarItem("Xuất sắc (90 - 100đ)", excellent, total, Color(0xFF10B981))
                    ProgressBarItem("Khá (70 - 89đ)", good, total, Color(0xFF3B82F6))
                    ProgressBarItem("Trung bình (50 - 69đ)", average, total, Color(0xFFF59E0B))
                    ProgressBarItem("Cần cố gắng (< 50đ)", weak, total, Color(0xFFEF4444))
                }
            }
        }
    }
}

@Composable
fun TodoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    title: String,
    actionText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.08f))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
        }
        Text(
            text = actionText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun ProgressBarItem(label: String, count: Int, total: Int, color: Color) {
    val fraction = (count.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 12.sp, color = Color(0xFF475569))
            Text(text = "$count em (${(fraction * 100).toInt()}%)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}
