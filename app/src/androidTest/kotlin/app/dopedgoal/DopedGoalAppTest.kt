package app.dopedgoal

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DopedGoalAppTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launchesOnToday() {
        rule.onNodeWithText("Today").assertIsDisplayed()
        rule.onNodeWithText("Your first brick is waiting.").assertIsDisplayed()
    }

    @Test
    fun bottomNavigationReachesArchive() {
        rule.onNodeWithText("Archive").performClick()
        rule.onNodeWithText("Finished walls are kept here, exactly as you built them.").assertIsDisplayed()
    }
}
