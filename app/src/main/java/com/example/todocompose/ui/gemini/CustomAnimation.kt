package com.example.todocompose.ui.gemini

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.todocompose.R
import com.example.todocompose.ui.theme.TodoComposeTheme
import kotlinx.coroutines.delay

@Preview(showBackground = false)
@Composable
fun AnimatedSpeakerPreview() = MaterialTheme {
    AnimatedSpeaker()
}

@Composable
fun AnimatedSpeaker(
    circleColor: Color = MaterialTheme.colorScheme.onSecondary,
    animationDelay: Int = 1200
) {

// 3 circles
    val circles = List(3) {
        remember {
            Animatable(initialValue = 0f)
        }
    }

    circles.forEachIndexed { index, animatable ->
        LaunchedEffect(Unit) {
            // Use coroutine delay to sync animations
            // divide the animation delay by number of circles
            delay(timeMillis = (animationDelay / 3L) * (index + 1))
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = animationDelay,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size = 90.dp)
            .background(color = Color.Transparent)
    ) {
        Icon(
            modifier = Modifier.size(36.dp),
            painter = painterResource(id = R.drawable.ic_speaker_phone),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondary
        )
        circles.forEachIndexed { index, animatable ->
            Box(
                modifier = Modifier
                    .scale(scale = animatable.value)
                    .size(size = 90.dp)
                    .clip(shape = CircleShape)
                    .background(
                        color = circleColor
                            .copy(alpha = (1 - animatable.value))
                    )
            )
        }
    }
}

@Composable
fun AnimatedListening(cycleDuration: Int = 1200) {
    // Create an infinite transition
    val leftArcTransition = rememberInfiniteTransition(label = "LeftArcTransition")
    val rightArcTransition = rememberInfiniteTransition(label = "RightArcTransition")

    // Define the animated value for the number of visible dots
    val leftArcCount = leftArcTransition.animateValue(
        initialValue = 0,
        targetValue = 4,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = cycleDuration,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "LeftArcCount"
    )

    val rightArcCount = rightArcTransition.animateValue(
        initialValue = 0,
        targetValue = 4,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = cycleDuration,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "RightArcCount"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(leftArcCount.value) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .wrapContentWidth()
                    .height(60.dp)
                    .background(color = Color.Transparent)
            ) {
                DrawArc(
                    startAngle = -90f,
                    sweepAngle = -180f,
                    color = Color.White
                )
            }
        }
        Icon(
            modifier = Modifier.size(36.dp),
            painter = painterResource(id = R.drawable.ic_mic),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondary
        )
        repeat(rightArcCount.value) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .wrapContentWidth()
                    .height(60.dp)
                    .background(color = Color.Transparent)
            ) {
                DrawArc(
                    startAngle = 90f,
                    sweepAngle = -180f,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
}

@Composable
fun DrawArc(
    radius: Dp = 50.dp,
    strokeWidth: Dp = 2.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    startAngle: Float = 90f,
    sweepAngle: Float = -180f,
) {
    Canvas(
        modifier = Modifier
            .height(radius)
            .width(radius / 4)
    ) {
        val arcBounds = this.size.copy(height = radius.toPx())
        drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            size = arcBounds,
            style = Stroke(strokeWidth.toPx())
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AnimatedListeningPreview() {
    TodoComposeTheme {
        AnimatedListening()
    }
}