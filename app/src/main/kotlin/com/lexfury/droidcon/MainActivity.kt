package com.lexfury.droidcon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.lexfury.droidcon.ui.theme.DroidconLisbonTheme

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			DroidconLisbonTheme {
				Surface {
					CanvasScreen(modifier = Modifier.fillMaxSize())
				}
			}
		}
	}
}
