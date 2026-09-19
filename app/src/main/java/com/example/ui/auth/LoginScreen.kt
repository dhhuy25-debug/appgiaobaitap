package com.example.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.AppDao
import com.example.data.models.StudentEntity
import com.example.data.models.UserEntity
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    dao: AppDao,
    onTeacherLoggedIn: () -> Unit,
    onStudentLoggedIn: (StudentEntity) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedRole by remember { mutableStateOf("TEACHER") } // TEACHER or STUDENT
    var username by remember { mutableStateOf("huyduong") }
    var password by remember { mutableStateOf("123") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1E3A8A), Color(0xFF1D4ED8), Color(0xFFF8FAFC))
                )
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo & Branding Header
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "HUY DƯƠNG EDU",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFFDE68A),
            letterSpacing = 1.sp
        )
        Text(
            text = "HỆ THỐNG QUẢN LÝ HỌC SINH VÀ GIAO BÀI TẬP",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Người quản trị: Thầy Huy Dương",
            fontSize = 12.sp,
            color = Color(0xFFDBEAFE)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Main Login Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth().testTag("login_card")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Role Selector Tabs
                TabRow(
                    selectedTabIndex = if (selectedRole == "TEACHER") 0 else 1,
                    containerColor = Color(0xFFF1F5F9),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedRole == "TEACHER",
                        onClick = {
                            selectedRole = "TEACHER"
                            username = "huyduong"
                            password = "123"
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Giáo viên", fontWeight = FontWeight.Bold)
                            }
                        },
                        modifier = Modifier.testTag("tab_role_teacher")
                    )
                    Tab(
                        selected = selectedRole == "STUDENT",
                        onClick = {
                            selectedRole = "STUDENT"
                            username = "hs001"
                            password = "123"
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Học sinh", fontWeight = FontWeight.Bold)
                            }
                        },
                        modifier = Modifier.testTag("tab_role_student")
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(if (selectedRole == "TEACHER") "Tên đăng nhập giáo viên" else "Mã học sinh / Tên đăng nhập") },
                    leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_username")
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mật khẩu") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_password")
                )

                Button(
                    onClick = {
                        isLoading = true
                        coroutineScope.launch {
                            val user = dao.getUserByUsername(username.trim())
                            if (user == null) {
                                Toast.makeText(context, "Tài khoản không tồn tại. Vui lòng kiểm tra lại!", Toast.LENGTH_SHORT).show()
                                isLoading = false
                                return@launch
                            }
                            if (user.password != password.trim()) {
                                Toast.makeText(context, "Mật khẩu không chính xác!", Toast.LENGTH_SHORT).show()
                                isLoading = false
                                return@launch
                            }

                            if (user.role == "TEACHER") {
                                onTeacherLoggedIn()
                            } else {
                                val student = dao.getStudentByCode(user.studentCode ?: username.trim().uppercase())
                                    ?: dao.getAllStudents().firstOrNull()
                                if (student != null) {
                                    onStudentLoggedIn(student)
                                } else {
                                    Toast.makeText(context, "Không tìm thấy hồ sơ học sinh!", Toast.LENGTH_SHORT).show()
                                }
                            }
                            isLoading = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_login_submit")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Text("Đăng nhập", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Divider()

                // Quick Demo Access Buttons
                Text("Truy cập nhanh để thử nghiệm:", fontSize = 12.sp, color = Color(0xFF64748B))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            username = "huyduong"
                            password = "123"
                            onTeacherLoggedIn()
                        },
                        modifier = Modifier.weight(1f).testTag("quick_login_teacher")
                    ) {
                        Text("Thầy Huy Dương", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                val firstStudent = dao.getAllStudents().firstOrNull()
                                if (firstStudent != null) {
                                    onStudentLoggedIn(firstStudent)
                                } else {
                                    Toast.makeText(context, "Chưa có dữ liệu học sinh", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("quick_login_student")
                    ) {
                        Text("HS Nguyễn Văn An", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
