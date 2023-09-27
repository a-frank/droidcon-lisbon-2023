package com.lexfury.droidcon

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun CanvasScreen(modifier: Modifier = Modifier) {
	val textMeasurer = rememberTextMeasurer()
	Canvas(modifier = modifier) {
		drawText(
			text = "Hello droidcon Lisbon!",
			textMeasurer = textMeasurer,
			style = TextStyle.Default.copy(color = Color.LightGray)
		)
	}
}

@Preview
@Composable
private fun CanvasScreenPreview() {
	CanvasScreen(modifier = Modifier.fillMaxSize())
}