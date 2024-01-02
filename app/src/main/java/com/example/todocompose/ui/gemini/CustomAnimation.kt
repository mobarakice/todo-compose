package com.example.todocompose.ui.gemini

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.todocompose.R
import com.example.todocompose.ui.theme.TodoComposeTheme
import com.example.todocompose.ui.theme.Typography
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

    val circles = List(3) {
        remember {
            Animatable(initialValue = 0f)
        }
    }

    circles.forEachIndexed { index, value ->
        LaunchedEffect(Unit) {
            // Use coroutine delay to sync animations
            // divide the animation delay by number of circles
            delay(timeMillis = (animationDelay / 3L) * (index + 1))
            value.animateTo(
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
            .size(size = 56.dp)
            .background(color = Color.Transparent)
    ) {
        Icon(
            modifier = Modifier.size(36.dp),
            painter = painterResource(id = R.drawable.ic_speaker_phone),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondary
        )
        circles.forEach {
            Box(
                modifier = Modifier
                    .scale(scale = it.value)
                    .clip(shape = CircleShape)
                    .fillMaxSize()
                    .background(color = circleColor.copy(alpha = (1 - it.value)))
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
                    .wrapContentSize()
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
                    .wrapContentSize()
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
    radius: Dp = 36.dp,
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

@Composable
fun IndeterminateCircularIndicator(size: Dp = 30.dp) {
    Box(
        modifier = Modifier.wrapContentSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(size),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
fun TextWithAnimatedDots(
    @StringRes resInt: Int,
    color: Color = MaterialTheme.colorScheme.onSecondary,
    style: TextStyle = Typography.bodySmall
) {
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(animatedProgress) {

        animatedProgress.animateTo(
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 2000,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Restart
            ), targetValue = 1f
        )
    }

    val ellipsisCount = (animatedProgress.value * 4).toInt() % 4 + 1
    val dots = List(ellipsisCount) { "." }.joinToString("")

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "${stringResource(id = resInt)}$dots",
            style = style,
            color = color
        )
    }
}

@Composable
fun ThinkingLoader(
    modifier: Modifier = Modifier,
    @StringRes resInt: Int,
    color: Color = MaterialTheme.colorScheme.onSecondary,
    style: TextStyle = MaterialTheme.typography.bodyLarge
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        IndeterminateCircularIndicator()
        Spacer(modifier = Modifier.size(8.dp))
        TextWithAnimatedDots(resInt, color, style)
    }
}


@Preview(showBackground = false)
@Composable
fun ThinkingLoaderPreview() {
    TodoComposeTheme {
        ThinkingLoader(resInt = R.string.thinking_loader)
    }
}

@Preview(showBackground = true)
@Composable
fun AnimatedListeningPreview() {
    TodoComposeTheme {
        AnimatedListening()
    }
}