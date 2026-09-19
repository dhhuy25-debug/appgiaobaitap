package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun Top3CertificateDialog(
    show: Boolean,
    rank: Int,
    studentName: String,
    className: String,
    score: Double,
    durationSeconds: Int,
    activityTitle: String,
    teacherName: String = "Thầy Huy Dương",
    onDismiss: () -> Unit
) {
    if (!show) return
    val context = LocalContext.current
    val todayStr = SimpleDateFormat("'Ngày' dd 'tháng' MM 'năm' yyyy", Locale("vi", "VN")).format(Date())

    val (medalEmoji, rankTitle, primaryGold) = when (rank) {
        1 -> Triple("🥇", "HẠNG NHẤT (THỦ KHOA)", Color(0xFFD97706))
        2 -> Triple("🥈", "HẠNG NHÌ (Á KHOA 1)", Color(0xFF64748B))
        3 -> Triple("🥉", "HẠNG BA (Á KHOA 2)", Color(0xFFB45309))
        else -> Triple("🎖️", "TOP XUẤT SẮC", Color(0xFF2563EB))
    }

    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("certificate_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF7)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(
                        BorderStroke(
                            3.dp,
                            Brush.linearGradient(listOf(primaryGold, Color(0xFFFDE68A), primaryGold))
                        ),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Embellishment
                    Text(
                        text = "CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Độc lập - Tự do - Hạnh phúc",
                        fontSize = 10.sp,
                        color = Color(0xFF475569),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "★ ★ ★",
                        fontSize = 13.sp,
                        color = primaryGold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "THƯ KHEN THÀNH TÍCH",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryGold,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "HUY DƯƠNG EDU – HỆ THỐNG HỌC TẬP THÔNG MINH",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1D4ED8),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "$medalEmoji $rankTitle $medalEmoji",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryGold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Trân trọng tuyên dương em:",
                        fontSize = 13.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = Color(0xFF334155)
                    )

                    Text(
                        text = studentName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E3A8A),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Học sinh Lớp: $className",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Đã đạt thành tích xuất sắc trong hoạt động học tập:",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF334155)
                    )
                    Text(
                        text = "« $activityTitle »",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Stats row
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "ĐIỂM SỐ", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                            Text(text = "$score/100", fontSize = 15.sp, color = Color(0xFF059669), fontWeight = FontWeight.Bold)
                        }
                        Divider(modifier = Modifier.height(24.dp).width(1.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "THỜI GIAN", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                            Text(text = timeFormatted, fontSize = 15.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                        }
                        Divider(modifier = Modifier.height(24.dp).width(1.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "THỨ HẠNG", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                            Text(text = "Hạng $rank", fontSize = 15.sp, color = primaryGold, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Signature block
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Học sinh", fontSize = 11.sp, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(text = studentName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = todayStr, fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(text = "Giáo viên chủ nhiệm", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = teacherName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D4ED8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "Đã chuẩn bị bản in thư khen thành công!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).testTag("print_certificate_btn")
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("In thư khen", fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                Toast.makeText(context, "Đã lưu tệp thư khen PDF về thiết bị!", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryGold),
                            modifier = Modifier.weight(1f).testTag("download_certificate_btn")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tải xuống", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
