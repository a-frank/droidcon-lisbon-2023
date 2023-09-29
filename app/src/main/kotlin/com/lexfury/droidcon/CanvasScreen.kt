package com.lexfury.droidcon

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun CanvasScreen(modifier: Modifier = Modifier) {
	var rotate by remember {
		mutableStateOf(false)
	}
	LaunchedEffect(key1 = Unit) {
		delay(1_000)
		rotate = true
	}

	val rotation by animateFloatAsState(targetValue = if (!rotate) 0f else 180f, label = "rotation", animationSpec = infiniteRepeatable(tween(2_000)))

	LazyColumn(
		modifier = modifier
			.padding(16.dp)
	) {
		item(key = "Text") {
			val textMeasurer = rememberTextMeasurer()
			SizedCanvas {
				drawText(
					text = "Hello droidcon Lisbon!",
					textMeasurer = textMeasurer,
					style = TextStyle.Default.copy(color = Color.LightGray, fontSize = 32.sp),
				)
			}
		}
		item(key = "Simple circle") {
			SizedCanvas {
				drawCircle(color = Color.Green)
			}
		}
		item(key = "Sized circle") {
			SizedCanvas {
				drawCircle(color = Color.Green, radius = 32.dp.toPx())
			}
		}
		item(key = "Simple rect") {
			SizedCanvas {
				drawRect(
					color = Color.Green,
					topLeft = Offset(it.quarterWidth, it.quarterHeight),
					size = Size(it.halfWidth, it.halfHeight)
				)
			}
		}
		item(key = "Rect with a gradient brush coloration") {
			SizedCanvas {
				drawRect(
					brush = Brush.horizontalGradient(listOf(Color.Blue, Color.Green, Color.Blue)),
					topLeft = Offset(it.quarterWidth, it.quarterHeight),
					size = Size(it.halfWidth, it.halfHeight)
				)
			}
		}
		item(key = "Rect with rounded corners") {
			SizedCanvas {
				drawRoundRect(
					brush = Brush.horizontalGradient(listOf(Color.Blue, Color.Green, Color.Blue)),
					topLeft = Offset(it.quarterWidth, it.quarterHeight),
					size = Size(it.halfWidth, it.halfHeight),
					cornerRadius = CornerRadius(8.dp.toPx())
				)
			}
		}
		item(key = "Using Path to create a rect with partial rounded corners") {
			SizedCanvas {
				val cornerRadiusLeft = CornerRadius(8.dp.toPx())

				val backgroundPath = Path().apply {
					addRoundRect(
						RoundRect(
							rect = Rect(
								offset = Offset(it.quarterWidth, it.quarterHeight),
								size = Size(it.halfWidth, it.halfHeight),
							),
							topLeft = cornerRadiusLeft,
							bottomLeft = cornerRadiusLeft,
						),
					)
				}
				drawPath(
					path = backgroundPath,
					color = Color.Green,
				)
			}
		}
		item(key = "Draw an hour glass using a Path") {
			SizedCanvas {
				val hourGlass = Path().apply {
					moveTo(it.quarterWidth, it.quarterHeight)
					lineTo(it.width - it.quarterWidth, it.quarterHeight)
					quadraticBezierTo(it.halfWidth, it.halfHeight, it.width - it.quarterWidth, it.height - it.quarterHeight)
					lineTo(it.quarterWidth, it.height - it.quarterHeight)
					quadraticBezierTo(it.halfWidth, it.halfHeight, it.quarterWidth, it.quarterHeight)
					close()
				}
				drawPath(
					path = hourGlass,
					color = Color.Green,
					style = Stroke(width = 2.dp.toPx())
				)
			}
		}
		item(key = "Transformation operations on a drawn object") {
			SizedCanvas {
				val hourGlass = Path().apply {
					moveTo(it.quarterWidth, it.quarterHeight)
					lineTo(it.width - it.quarterWidth, it.quarterHeight)
					quadraticBezierTo(it.halfWidth, it.halfHeight, it.width - it.quarterWidth, it.height - it.quarterHeight)
					lineTo(it.quarterWidth, it.height - it.quarterHeight)
					quadraticBezierTo(it.halfWidth, it.halfHeight, it.quarterWidth, it.quarterHeight)
					close()
				}
				scale(.5f) {
					rotate(rotation) {
						drawPath(
							path = hourGlass,
							color = Color.Green,
							style = Stroke(width = 2.dp.toPx())
						)
					}
				}
			}
		}
	}
}

@Composable
private fun SizedCanvas(modifier: Modifier = Modifier, content: DrawScope.(CanvasSizes) -> Unit) {
	Canvas(
		modifier = modifier
			.fillMaxWidth()
			.height(300.dp)
	) {
		val canvasWidth = size.width
		val halfCanvasWidth = center.x
		val quarterCanvasWidth = canvasWidth / 4

		val canvasHeight = size.height
		val halfCanvasHeight = center.y
		val quarterCanvasHeight = canvasHeight / 4

		val canvasSizes = CanvasSizes(
			width = canvasWidth,
			halfWidth = halfCanvasWidth,
			quarterWidth = quarterCanvasWidth,
			height = canvasHeight,
			halfHeight = halfCanvasHeight,
			quarterHeight = quarterCanvasHeight
		)
		content(canvasSizes)
	}
}

private data class CanvasSizes(
	val width: Float,
	val halfWidth: Float,
	val quarterWidth: Float,
	val height: Float,
	val halfHeight: Float,
	val quarterHeight: Float
)

@Preview
@Composable
private fun CanvasScreenPreview() {
	CanvasScreen(modifier = Modifier.fillMaxSize())
}