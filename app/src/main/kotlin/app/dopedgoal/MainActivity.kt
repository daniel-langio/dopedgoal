package app.dopedgoal

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.dopedgoal.ui.DopedGoalApp
import app.dopedgoal.ui.theme.DopedGoalTheme

class MainActivity : ComponentActivity() {
    companion object {
        @Volatile
        private var _appContext: Context? = null
        fun getAppContext(): Context? = _appContext
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        _appContext = this
        setContent {
            DopedGoalTheme {
                DopedGoalApp()
            }
        }
    }
}