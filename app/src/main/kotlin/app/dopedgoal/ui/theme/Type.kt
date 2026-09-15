package app.dopedgoal.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

/**
 * Humanist sans for interface text; a compact monospace face for counters and
 * material metadata. No serif body face — it turns dense at Android body sizes.
 */
private val Interface = FontFamily.SansSerif
private val Meta = FontFamily.Monospace

val DopedGoalTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = Interface,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Interface,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Interface,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Interface,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Meta,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.6.sp,
    ),
)

/** Counters such as `4 / 12`, and material metadata on brick detail. */
val MetaTextStyle = TextStyle(
    fontFamily = Meta,
    fontWeight = FontWeight.Medium,
    fontSize = 13.sp,
    textAlign = TextAlign.End,
)
