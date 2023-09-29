package com.lexfury.droidcon

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@Composable
fun <T : Number> LabeledRangeSlider(
	selectedLowerBound: T,
	selectedUpperBound: T,
	steps: List<T>,
	onRangeChanged: (lower: T, upper: T) -> Unit,
	modifier: Modifier = Modifier,
	barHeight: Dp = 12.dp,
	barRadius: Dp = 6.dp,
	barBackgroundColor: Color = Color.LightGray,
	barSelectedColor: Color = Color.Cyan,
	touchCircleRadius: Dp = 16.dp,
	touchCircleColor: Color = Color.White,
	labelSize: TextUnit = 16.sp,
	labelOffset: Dp = 8.dp,
	stepMarkerColor: Color = Color.DarkGray,
) {
	require(steps.size > 2) { "List of steps has to be at least of size 2" }
	require(steps.contains(selectedLowerBound)) { "selectedLowerBound has to be part of the provided steps" }
	require(steps.contains(selectedUpperBound)) { "selectedUpperBound has to be part of the provided steps" }

	var moveLeft by remember { mutableStateOf(false) }
	var moveRight by remember { mutableStateOf(false) }

	var composableSize by remember { mutableStateOf(IntSize(0, 0)) }

	val currentDensity = LocalDensity.current
	val sizeAndDensity = composableSize to currentDensity
	val textMeasurer = rememberTextMeasurer()

	val height = remember(touchCircleRadius, labelSize, labelOffset) { touchCircleRadius * 2 + labelSize.value.dp + labelOffset }
	val barYCenter = sizeAndDensity.derive { composableSize.height - touchCircleRadius.toPx() }
	val barHeightPx = sizeAndDensity.derive { barHeight.toPx() }
	val labelSizePx = sizeAndDensity.derive { labelSize.toPx() }
	val stepMarkerRadius = sizeAndDensity.derive { (barHeight / 4).toPx() }
	val barXStart = sizeAndDensity.derive { touchCircleRadius.toPx() - stepMarkerRadius }
	val barYStart = sizeAndDensity.derive { barYCenter - barHeightPx / 2 }
	val barWidth = sizeAndDensity.derive { composableSize.width - 2 * barXStart }
	val barCornerRadius = sizeAndDensity.derive { CornerRadius(barRadius.toPx(), barRadius.toPx()) }
	val touchCircleRadiusPx = sizeAndDensity.derive { touchCircleRadius.toPx() }


	val (stepXCoordinates, stepSpacing) = sizeAndDensity.derive(steps) {
		calculateStepCoordinatesAndSpacing(
			numberOfSteps = steps.size,
			barXStart = barXStart,
			barWidth = barWidth,
			stepMarkerRadius = stepMarkerRadius
		)
	}

	var leftCirclePosition by remember(key1 = composableSize) {
		val lowerBoundIdx = steps.indexOf(selectedLowerBound)
		mutableStateOf(Offset(stepXCoordinates[lowerBoundIdx], barYCenter))
	}
	var rightCirclePosition by remember(key1 = composableSize) {
		val upperBoundIdx = steps.indexOf(selectedUpperBound)
		mutableStateOf(Offset(stepXCoordinates[upperBoundIdx], barYCenter))
	}

	var touchInteractionState by remember { mutableStateOf<TouchInteraction>(TouchInteraction.NoInteraction) }

	Canvas(
		modifier = modifier
			.height(height)
			.onSizeChanged {
				composableSize = it
			}
			.touchInteraction(remember { MutableInteractionSource() }) {
				touchInteractionState = it
			}
	) {

		drawRoundRect(
			color = barBackgroundColor,
			topLeft = Offset(barXStart, barYStart),
			size = Size(barWidth, barHeightPx),
			cornerRadius = barCornerRadius
		)

		drawRect(
			color = barSelectedColor,
			topLeft = Offset(leftCirclePosition.x, barYStart),
			size = Size(rightCirclePosition.x - leftCirclePosition.x, barHeightPx)
		)

		drawStepMarkersAndLabels(
			steps = steps,
			stepXCoordinates = stepXCoordinates,
			leftCirclePosition = leftCirclePosition,
			rightCirclePosition = rightCirclePosition,
			barYCenter = barYCenter,
			stepMarkerRadius = stepMarkerRadius,
			stepMarkerColor = stepMarkerColor,
			labelSize = labelSizePx,
			textMeasurer = textMeasurer
		)

		drawCircleWithShadow(
			leftCirclePosition,
			moveLeft,
			touchCircleRadiusPx,
			touchCircleColor,
		)

		drawCircleWithShadow(
			rightCirclePosition,
			moveRight,
			touchCircleRadiusPx,
			touchCircleColor,
		)
	}

	handleTouch(
		leftCirclePosition = leftCirclePosition,
		rightCirclePosition = rightCirclePosition,
		moveLeft = moveLeft,
		moveRight = moveRight,
		stepXCoordinates = stepXCoordinates,
		stepSpacing = stepSpacing,
		touchInteraction = touchInteractionState,
		updateLeft = { position, move ->
			leftCirclePosition = position
			moveLeft = move
		},
		updateRight = { position, move ->
			rightCirclePosition = position
			moveRight = move
		},
		onTouchInteractionChanged = { touchInteractionState = it },
		onRangeIdxChanged = { lowerBoundIdx, upperBoundIdx -> onRangeChanged(steps[lowerBoundIdx], steps[upperBoundIdx]) }
	)
}

private fun DrawScope.drawCircleWithShadow(
	position: Offset,
	touched: Boolean,
	touchCircleRadius: Float,
	touchCircleColor: Color
) {
	val touchAddition = if (touched) {
		1.dp.toPx()
	} else {
		0f
	}

	drawIntoCanvas {
		val paint = Paint()
		val frameworkPaint = paint.asFrameworkPaint()
		frameworkPaint.color = touchCircleColor.toArgb()
		frameworkPaint.setShadowLayer(
			2.dp.toPx() + touchAddition,
			0f,
			0f,
			Color.DarkGray.toArgb()
		)
		it.drawCircle(
			position,
			touchCircleRadius,
			paint
		)
	}
}

private fun <T> DrawScope.drawStepMarkersAndLabels(
	steps: List<T>,
	stepXCoordinates: FloatArray,
	leftCirclePosition: Offset,
	rightCirclePosition: Offset,
	barYCenter: Float,
	stepMarkerRadius: Float,
	stepMarkerColor: Color,
	labelSize: Float,
	textMeasurer: TextMeasurer,
) {
	require(steps.size == stepXCoordinates.size) { "Step value size and step coordinate size do not match. Value size: ${steps.size}, Coordinate size: ${stepXCoordinates.size}" }

	steps.forEachIndexed { index, step ->
		val stepMarkerCenter = Offset(stepXCoordinates[index], barYCenter)

		val isCurrentlySelectedByLeftCircle =
			(leftCirclePosition.x > (stepMarkerCenter.x - stepMarkerRadius / 2)) &&
				(leftCirclePosition.x < (stepMarkerCenter.x + stepMarkerRadius / 2))
		val isCurrentlySelectedByRightCircle =
			(rightCirclePosition.x > (stepMarkerCenter.x - stepMarkerRadius / 2)) &&
				(rightCirclePosition.x < (stepMarkerCenter.x + stepMarkerRadius / 2))

		val textStyle = when {
			isCurrentlySelectedByLeftCircle || isCurrentlySelectedByRightCircle -> TextStyle.Default.copy(color = Color.Black, fontWeight = FontWeight.Bold)
			stepMarkerCenter.x < leftCirclePosition.x || stepMarkerCenter.x > rightCirclePosition.x -> TextStyle.Default.copy(color = Color.LightGray)
			else -> TextStyle.Default.copy(color = Color.Black)
		}

		drawCircle(
			color = stepMarkerColor,
			radius = stepMarkerRadius,
			alpha = .1f,
			center = stepMarkerCenter
		)

		val stepText = step.toString().let { text ->
			if (text.length > 3) {
				text.substring(0, 2)
			} else {
				text
			}
		}

		drawText(
			textMeasurer = textMeasurer,
			text = stepText,
			style = textStyle,
			topLeft = Offset(stepMarkerCenter.x - (stepText.length * labelSize) / 3, 0f),
		)

	}
}

private fun calculateStepCoordinatesAndSpacing(
	numberOfSteps: Int,
	barXStart: Float,
	barWidth: Float,
	stepMarkerRadius: Float,
): Pair<FloatArray, Float> {
	val stepOffset = barXStart + stepMarkerRadius
	val stepSpacing = (barWidth - 2 * stepMarkerRadius) / (numberOfSteps - 1)

	val stepXCoordinates = generateSequence(stepOffset) { it + stepSpacing }
		.take(numberOfSteps)
		.toList()

	return stepXCoordinates.toFloatArray() to stepSpacing
}

@Composable
private fun <T> Pair<IntSize, Density>.derive(additionalKey: Any? = null, block: Density.() -> T): T =
	remember(key1 = first, key2 = additionalKey) {
		second.block()
	}

fun Modifier.touchInteraction(key: Any, block: (TouchInteraction) -> Unit): Modifier =
	pointerInput(key) {
		awaitEachGesture {
			do {
				val event: PointerEvent = awaitPointerEvent()

				event.changes
					.forEach { pointerInputChange: PointerInputChange ->
						if (pointerInputChange.positionChange() != Offset.Zero) pointerInputChange.consume()
					}

				block(TouchInteraction.Move(event.changes.first().position))
			} while (event.changes.any { it.pressed })

			block(TouchInteraction.Up)
		}
	}

sealed class TouchInteraction {
	data object NoInteraction : TouchInteraction()
	data object Up : TouchInteraction()
	data class Move(val position: Offset) : TouchInteraction()
}

private fun handleTouch(
	leftCirclePosition: Offset,
	rightCirclePosition: Offset,
	moveLeft: Boolean,
	moveRight: Boolean,
	stepXCoordinates: FloatArray,
	stepSpacing: Float,
	touchInteraction: TouchInteraction,
	updateLeft: (Offset, Boolean) -> Unit,
	updateRight: (Offset, Boolean) -> Unit,
	onTouchInteractionChanged: (TouchInteraction) -> Unit,
	onRangeIdxChanged: (Int, Int) -> Unit
) {
	when (touchInteraction) {
		is TouchInteraction.Move -> {
			val touchPositionX = touchInteraction.position.x
			if (abs(touchPositionX - leftCirclePosition.x) < abs(touchPositionX - rightCirclePosition.x)) {
				val leftPosition = calculateNewLeftCirclePosition(touchPositionX, leftCirclePosition, rightCirclePosition, stepSpacing, stepXCoordinates.first())
				updateLeft(leftPosition, true)

				if (moveRight) {
					moveToClosestStep(rightCirclePosition, stepXCoordinates) { position, move -> updateRight(position, move) }
				}
			} else {
				val rightPosition = calculateNewRightCirclePosition(touchPositionX, leftCirclePosition, rightCirclePosition, stepSpacing, stepXCoordinates.last())
				updateRight(rightPosition, true)

				if (moveLeft) {
					moveToClosestStep(leftCirclePosition, stepXCoordinates) { position, move -> updateLeft(position, move) }
				}
			}
		}

		is TouchInteraction.Up   -> {
			val (closestLeftValue, closestLeftIndex) = stepXCoordinates.getClosestNumber(leftCirclePosition.x)
			val (closestRightValue, closestRightIndex) = stepXCoordinates.getClosestNumber(rightCirclePosition.x)
			if (moveLeft) {
				val leftPosition = leftCirclePosition.copy(x = closestLeftValue)
				updateLeft(leftPosition, false)
				onRangeIdxChanged(closestLeftIndex, closestRightIndex)
			} else if (moveRight) {
				val rightPosition = rightCirclePosition.copy(x = closestRightValue)
				updateRight(rightPosition, false)
				onRangeIdxChanged(closestLeftIndex, closestRightIndex)
			}

			onTouchInteractionChanged(TouchInteraction.NoInteraction)
		}

		else                     -> {
			// nothing to do
		}
	}
}

private fun calculateNewLeftCirclePosition(
	touchPositionX: Float,
	leftCirclePosition: Offset,
	rightCirclePosition: Offset,
	stepSpacing: Float,
	firstStepXPosition: Float
): Offset = when {
	touchPositionX < firstStepXPosition                    -> leftCirclePosition.copy(x = firstStepXPosition)
	touchPositionX > (rightCirclePosition.x - stepSpacing) -> leftCirclePosition
	else                                                   -> leftCirclePosition.copy(x = touchPositionX)
}

private fun calculateNewRightCirclePosition(
	touchPositionX: Float,
	leftCirclePosition: Offset,
	rightCirclePosition: Offset,
	stepSpacing: Float,
	lastStepXPosition: Float
): Offset = when {
	touchPositionX > lastStepXPosition                    -> rightCirclePosition.copy(x = lastStepXPosition)
	touchPositionX < (leftCirclePosition.x + stepSpacing) -> rightCirclePosition
	else                                                  -> rightCirclePosition.copy(x = touchPositionX)
}

private fun moveToClosestStep(circlePosition: Offset, stepXCoordinates: FloatArray, update: (Offset, Boolean) -> Unit) {
	val (closestRightValue, _) = stepXCoordinates.getClosestNumber(circlePosition.x)
	val updatedPosition = circlePosition.copy(x = closestRightValue)
	update(updatedPosition, false)
}

private fun FloatArray.getClosestNumber(input: Float): Pair<Float, Int> {
	var minElem = this[0]
	var minValue = abs(minElem - input)
	var minIdx = 0
	for (i in 1..lastIndex) {
		val e = this[i]
		val v = abs(e - input)
		if (minValue > v) {
			minElem = e
			minValue = v
			minIdx = i
		}
	}
	return minElem to minIdx
}