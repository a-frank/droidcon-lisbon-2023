package com.lexfury.droidcon

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
@ExperimentalComposeUiApi
fun ScribbleScreen(
	modifier: Modifier = Modifier,
	color: Color = Color.Green,
	lineWidth: Dp = 1.dp,
) {
	var points by remember { mutableStateOf<List<Offset>>(emptyList()) }

	Canvas(modifier = modifier
		.pointerInput(remember { MutableInteractionSource() }) {
			detectDragGestures(
				onDragStart = {
					points = listOf(it)
				},
				onDrag = { change, _ ->
					val pointsFromHistory = change.historical
						.map { it.position }
						.toTypedArray()
					val newPoints = listOf(*pointsFromHistory, change.position)
					points = points + newPoints
				}
			)
		}
	) {
		if (points.size > 1) {
		    val path = Path().apply {
			    val firstPoint = points.first()
			    val rest = points.subList(1, points.size - 1)

			    moveTo(firstPoint.x, firstPoint.y)
			    rest.forEach {
				    lineTo(it.x, it.y)
			    }
		    }

		    drawPath(path, color, style = Stroke(width = lineWidth.toPx()))
		}

	}
}