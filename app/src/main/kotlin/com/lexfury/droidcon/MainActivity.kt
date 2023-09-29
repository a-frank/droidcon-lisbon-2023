package com.lexfury.droidcon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.lexfury.droidcon.ui.theme.DroidconLisbonTheme

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {

			var displayedScreen by remember { mutableStateOf(Screen.Draw) }

			DroidconLisbonTheme {
				Surface {
					Scaffold(
						bottomBar = {
							BottomAppBar(
								actions = {
									IconButton(onClick = { displayedScreen = Screen.Draw }) {
										Icon(painter = painterResource(id = R.drawable.draw_abstract_24px), contentDescription = "Drawing")
									}
									IconButton(onClick = { displayedScreen = Screen.Scribble }) {
										Icon(painter = painterResource(id = R.drawable.gesture_24px), contentDescription = "Scribble")
									}
									IconButton(onClick = { displayedScreen = Screen.LabeledRangeSlider }) {
										Icon(painter = painterResource(id = R.drawable.sliders_24px), contentDescription = "Labeled Range Slider")
									}
								}
							)
						}
					) {
						val modifier = Modifier.padding(it)
						when (displayedScreen) {
							Screen.Draw -> CanvasPage(modifier = modifier)
							Screen.Scribble -> ScribblePage(modifier = modifier)
							Screen.LabeledRangeSlider -> LabeledRangeSliderPage(modifier = modifier)
						}
					}

				}
			}
		}
	}

	private enum class Screen {
		Draw, Scribble, LabeledRangeSlider
	}

	@Composable
	private fun CanvasPage(modifier: Modifier = Modifier) {
		CanvasScreen(modifier = modifier.fillMaxSize())
	}

	@Composable
	@OptIn(ExperimentalComposeUiApi::class)
	private fun ScribblePage(modifier: Modifier = Modifier) {
		ScribbleScreen(modifier = modifier.fillMaxSize())
	}

	@Composable
	private fun LabeledRangeSliderPage(modifier: Modifier = Modifier) {
		Column(
			modifier = modifier
				.background(color = Color.White)
				.padding(8.dp)
		) {
			val steps = (0..100).step(10).toList()
			var lowerBound by rememberSaveable { mutableIntStateOf(steps[1]) }
			var upperBound by rememberSaveable { mutableIntStateOf(steps[steps.size - 2]) }

			LabeledRangeSlider(
				selectedLowerBound = lowerBound,
				selectedUpperBound = upperBound,
				steps = steps,
				onRangeChanged = { lower, upper ->
					lowerBound = lower
					upperBound = upper
				},
				modifier = Modifier.fillMaxWidth()
			)

			Spacer(modifier = Modifier.size(16.dp))
			Divider()
			Spacer(modifier = Modifier.size(16.dp))

			Text(
				text = "The selected range is ${lowerBound..upperBound}",
				color = Color.Black,
				modifier = Modifier.align(Alignment.CenterHorizontally)
			)
		}
	}
}
