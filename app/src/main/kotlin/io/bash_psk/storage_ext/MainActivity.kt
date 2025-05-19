package io.bash_psk.storage_ext

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.bash_psk.storage_ext.ui.theme.StorageExtTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            StorageExtTheme {

                MainScreen()
            }
        }
    }
}