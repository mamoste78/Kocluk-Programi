package com.example

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.student.StudentDashboardScreen
import com.example.ui.screens.teacher.TeacherDashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.UserRole

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: MainViewModel = viewModel()) {
    val currentRole by viewModel.currentRole.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    when (currentRole) {
        UserRole.NONE -> {
            // Giriş ekranında geri tuşuna basılırsa uygulamadan çıkar
            BackHandler {
                activity?.finish()
            }
            AuthScreen(viewModel = viewModel)
        }
        UserRole.OGRENCI -> {
            // Öğrenci panelinde geri tuşuna basıldığında oturumu KAPATMAZ, uygulamayı arka plana alır
            BackHandler {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastBackPressTime < 2000) {
                    activity?.moveTaskToBack(true)
                } else {
                    lastBackPressTime = currentTime
                    Toast.makeText(context, "Uygulamadan çıkmak için tekrar basın", Toast.LENGTH_SHORT).show()
                }
            }
            StudentDashboardScreen(viewModel = viewModel)
        }
        UserRole.OGRETMEN -> {
            // Öğretmen panelinde geri tuşuna basıldığında oturumu KAPATMAZ, uygulamayı arka plana alır
            BackHandler {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastBackPressTime < 2000) {
                    activity?.moveTaskToBack(true)
                } else {
                    lastBackPressTime = currentTime
                    Toast.makeText(context, "Uygulamadan çıkmak için tekrar basın", Toast.LENGTH_SHORT).show()
                }
            }
            TeacherDashboardScreen(viewModel = viewModel)
        }
    }
}
