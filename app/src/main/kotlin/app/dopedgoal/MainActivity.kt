package app.dopedgoal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.dopedgoal.ui.DopedGoalApp
import app.dopedgoal.ui.theme.DopedGoalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            DopedGoalTheme {
                DopedGoalApp()
            }
        }
    }
}
