package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.AppDatabase
import com.example.data.database.DatabaseInitializer
import com.example.data.models.StudentEntity
import com.example.ui.auth.LoginScreen
import com.example.ui.student.*
import com.example.ui.teacher.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                EduAppRoot()
            }
        }
    }
}

enum class AuthRole {
    NONE, TEACHER, STUDENT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EduAppRoot() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getInstance(context) }
    val dao = remember { database.appDao() }
    val coroutineScope = rememberCoroutineScope()

    // Initialize database with sample data on first start
    LaunchedEffect(Unit) {
        DatabaseInitializer.initializeIfNeeded(dao)
    }

    var currentRole by remember { mutableStateOf(AuthRole.TEACHER) } // Default to teacher for immediate preview
    var currentStudent by remember { mutableStateOf<StudentEntity?>(null) }

    // Teacher navigation route
    // "dashboard", "students", "classes", "assignments", "create_assignment", "stats", "rankings", "rewards", "attendance", "discipline", "support", "notes", "settings"
    var teacherRoute by remember { mutableStateOf("dashboard") }
    var selectedAssignmentForStats by remember { mutableStateOf<String?>(null) }
    var targetClassForStudents by remember { mutableStateOf<String?>(null) }

    // Student navigation route
    // "home", "quiz", "result", "tutor"
    var studentRoute by remember { mutableStateOf("home") }
    var currentQuizAssignmentId by remember { mutableStateOf("") }
    var currentResultSubmissionId by remember { mutableStateOf("") }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    when (currentRole) {
        AuthRole.NONE -> {
            LoginScreen(
                dao = dao,
                onTeacherLoggedIn = {
                    currentRole = AuthRole.TEACHER
                    teacherRoute = "dashboard"
                },
                onStudentLoggedIn = { st ->
                    currentStudent = st
                    currentRole = AuthRole.STUDENT
                    studentRoute = "home"
                }
            )
        }

        AuthRole.TEACHER -> {
            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = true,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = Modifier.width(310.dp),
                        drawerContainerColor = MaterialTheme.colorScheme.surface
                    ) {
                        // Drawer Header
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Column {
                                Text(
                                    text = "HUY DƯƠNG EDU",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = Color(0xFFFDE68A),
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Thầy Huy Dương (Quản trị)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Lớp chủ nhiệm: 5/9 • Khối 5",
                                    fontSize = 12.sp,
                                    color = Color(0xFFDBEAFE)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            item {
                                DrawerItem("Tổng quan lớp học", Icons.Default.Dashboard, teacherRoute == "dashboard") {
                                    teacherRoute = "dashboard"
                                    coroutineScope.launch { drawerState.close() }
                                }
                            }
                            item {
                                DrawerItem("Quản lý học sinh", Icons.Default.People, teacherRoute == "students") {
                                    targetClassForStudents = null
                                    teacherRoute = "students"
                                    coroutineScope.launch { drawerState.close() }
                                }
                            }
                            item {
                                DrawerItem("Quản lý lớp học", Icons.Default.School, teacherRoute == "classes") {
                                    teacherRoute = "classes"
                                    coroutineScope.launch { drawerState.close() }
                                }
                            }
                            item {
                                DrawerItem("Quản lý bài tập", Icons.Default.Assignment, teacherRoute == "assignments") {
                                    teacherRoute = "assignments"
                                    coroutineScope.launch { drawerState.close() }
                                }
                            }
                            item {
                                DrawerItem("Giao bài tập mới", Icons.Default.AddCircle, teacherRoute == "create_assignment") {
                                    teacherRoute = "create_assignment"
                                    coroutineScope.launch { drawerState.close() }
                                }
                            }
                            item {
                                DrawerItem("Thống kê & Phổ điểm", Icons.Default.BarChart, teacherRoute == "stats") {
                                    teacherRoute = "stats"
                                    coroutineScope.launch { drawerState.close() }
                                }
                            }
                            item {
                                DrawerItem("Bảng xếp hạng học sinh", Icons.Default.EmojiEvents, teacherRoute == "rankings") {
                                    teacherRoute = "rankings"
                                    coroutineScope.launch { drawerState.close() }
                                }
                            }
                            item {
                                DrawerItem("Khen thưởng & Huy hiệu", Icons.Default.Star, teacherRoute == "rewards") {
                                    teacherRoute = "rewards"
                                    coroutineScope.launch { drawerState.close() }
                                }
                            }
                            item {
                                DrawerItem("Điểm danh chuyên cần", Icons.Default.CheckCircle, teacherRoute == "attendance") {
                                    teacherRoute = "attendance"
                                    coroutineScope.launch { drawerState.close() }
                                }
                            }
                            item {
                                DrawerItem("Theo dõi nề nếp", Icons.Default.Rule, teacherRoute == "discipline") {
                                    teacherRoute = "discipline"
                                    coroutineScope.launch { drawerState.close() }
                                }
                            }
                            item {
                                DrawerItem("Học sinh cần quan tâm", Icons.Default.Warning, teacherRoute == "support") {
                                    teacherRoute = "support"
                                    coroutineScope.launch { drawerState.close() }
                                }
                            }
                            item {
                                DrawerItem("Hồ sơ lưu ý giáo viên", Icons.Default.Notes, teacherRoute == "notes") {
                                    teacherRoute = "notes"
                                    coroutineScope.launch { drawerState.close() }
                                }
                            }
                            item {
                                Divider(modifier = Modifier.padding(vertical = 6.dp))
                            }
                            item {
                                DrawerItem("Cài đặt hệ thống", Icons.Default.Settings, teacherRoute == "settings") {
                                    teacherRoute = "settings"
                                    coroutineScope.launch { drawerState.close() }
                                }
                            }
                            item {
                                DrawerItem("Đổi sang vai trò Học sinh", Icons.Default.SwapHoriz, false) {
                                    coroutineScope.launch {
                                        val firstStudent = dao.getAllStudents().firstOrNull()
                                        if (firstStudent != null) {
                                            currentStudent = firstStudent
                                            currentRole = AuthRole.STUDENT
                                            studentRoute = "home"
                                            drawerState.close()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = when (teacherRoute) {
                                            "dashboard" -> "Huy Dương Edu"
                                            "students" -> "Quản lý học sinh"
                                            "classes" -> "Quản lý lớp học"
                                            "assignments" -> "Quản lý bài tập"
                                            "create_assignment" -> "Giao bài tập mới"
                                            "stats" -> "Thống kê kết quả"
                                            "rankings" -> "Bảng xếp hạng"
                                            "rewards" -> "Khen thưởng học sinh"
                                            "attendance" -> "Điểm danh chuyên cần"
                                            "discipline" -> "Theo dõi nề nếp"
                                            "support" -> "Học sinh cần quan tâm"
                                            "notes" -> "Hồ sơ lưu ý"
                                            else -> "Cài đặt hệ thống"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                    Text(
                                        text = "Thầy Huy Dương • Lớp 5/9",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu điều hướng")
                                }
                            },
                            actions = {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color(0xFFEFF6FF),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = "Giáo viên",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1D4ED8),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                        )
                    },
                    bottomBar = {
                        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                            NavigationBarItem(
                                selected = teacherRoute == "dashboard",
                                onClick = { teacherRoute = "dashboard" },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Tổng quan") },
                                label = { Text("Tổng quan", fontSize = 10.sp) }
                            )
                            NavigationBarItem(
                                selected = teacherRoute == "students",
                                onClick = {
                                    targetClassForStudents = null
                                    teacherRoute = "students"
                                },
                                icon = { Icon(Icons.Default.People, contentDescription = "Học sinh") },
                                label = { Text("Học sinh", fontSize = 10.sp) }
                            )
                            NavigationBarItem(
                                selected = teacherRoute == "assignments",
                                onClick = { teacherRoute = "assignments" },
                                icon = { Icon(Icons.Default.Assignment, contentDescription = "Bài tập") },
                                label = { Text("Bài tập", fontSize = 10.sp) }
                            )
                            NavigationBarItem(
                                selected = teacherRoute == "rankings",
                                onClick = { teacherRoute = "rankings" },
                                icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Xếp hạng") },
                                label = { Text("Xếp hạng", fontSize = 10.sp) }
                            )
                            NavigationBarItem(
                                selected = teacherRoute == "settings",
                                onClick = { teacherRoute = "settings" },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Cài đặt") },
                                label = { Text("Cài đặt", fontSize = 10.sp) }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (teacherRoute) {
                            "dashboard" -> TeacherDashboardScreen(dao) { route ->
                                teacherRoute = route
                            }
                            "students" -> StudentManagementScreen(dao, initialClassId = targetClassForStudents)
                            "classes" -> ClassManagementScreen(dao) { classId ->
                                targetClassForStudents = classId
                                teacherRoute = "students"
                            }
                            "assignments" -> AssignmentListScreen(
                                dao = dao,
                                onCreateNew = { teacherRoute = "create_assignment" },
                                onViewStats = { id ->
                                    selectedAssignmentForStats = id
                                    teacherRoute = "stats"
                                }
                            )
                            "create_assignment" -> CreateAssignmentScreen(
                                dao = dao,
                                onFinish = { teacherRoute = "assignments" }
                            )
                            "stats" -> AssignmentStatsScreen(
                                dao = dao,
                                assignmentId = selectedAssignmentForStats,
                                onBack = { teacherRoute = "assignments" }
                            )
                            "rankings" -> RankingsScreen(
                                dao = dao,
                                onViewCertificate = { _, _ -> }
                            )
                            "rewards" -> RewardsScreen(dao)
                            "attendance" -> AttendanceScreen(dao)
                            "discipline" -> DisciplineScreen(dao)
                            "support" -> SupportNeededScreen(dao)
                            "notes" -> StudentProfileNotesScreen(dao)
                            "settings" -> TeacherSettingsScreen(dao) {
                                currentRole = AuthRole.NONE
                            }
                        }
                    }
                }
            }
        }

        AuthRole.STUDENT -> {
            val student = currentStudent
            if (student == null) {
                currentRole = AuthRole.NONE
                return
            }

            when (studentRoute) {
                "home" -> {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Column {
                                        Text("Huy Dương Edu", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                        Text("${student.fullName} • Lớp ${student.className}", fontSize = 11.sp, color = Color(0xFF64748B))
                                    }
                                },
                                actions = {
                                    TextButton(onClick = { currentRole = AuthRole.TEACHER }) {
                                        Text("Vào vai Thầy Huy Dương", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                            )
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            StudentHomeScreen(
                                student = student,
                                dao = dao,
                                onStartAssignment = { assignId ->
                                    currentQuizAssignmentId = assignId
                                    studentRoute = "quiz"
                                },
                                onViewResult = { subId ->
                                    currentResultSubmissionId = subId
                                    studentRoute = "result"
                                },
                                onNavigateToTutor = {
                                    studentRoute = "tutor"
                                },
                                onLogout = { currentRole = AuthRole.NONE }
                            )
                        }
                    }
                }

                "quiz" -> {
                    StudentQuizScreen(
                        student = student,
                        assignmentId = currentQuizAssignmentId,
                        dao = dao,
                        onSubmitted = { subId ->
                            currentResultSubmissionId = subId
                            studentRoute = "result"
                        },
                        onCancel = { studentRoute = "home" }
                    )
                }

                "result" -> {
                    StudentResultScreen(
                        submissionId = currentResultSubmissionId,
                        dao = dao,
                        onBackHome = { studentRoute = "home" }
                    )
                }

                "tutor" -> {
                    StudentAiTutorScreen(
                        student = student,
                        onBack = { studentRoute = "home" }
                    )
                }
            }
        }
    }
}

@Composable
fun DrawerItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp) },
        icon = { Icon(icon, contentDescription = null, tint = if (selected) MaterialTheme.colorScheme.primary else Color(0xFF64748B)) },
        selected = selected,
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            selectedTextColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    )
}
