package com.iptvplayer.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.iptvplayer.app.ui.AppViewModel
import com.iptvplayer.app.ui.SessionState
import com.iptvplayer.app.ui.login.LoginScreen
import com.iptvplayer.app.ui.navigation.MainNavHost
import com.iptvplayer.app.ui.theme.IptvPlayerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IptvPlayerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val appViewModel: AppViewModel = hiltViewModel()
                    val sessionState by appViewModel.sessionState.collectAsState()

                    when (sessionState) {
                        is SessionState.Checking -> {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        is SessionState.LoggedOut -> {
                            LoginScreen(onLoginSuccess = { appViewModel.markLoggedIn() })
                        }
                        is SessionState.LoggedIn -> {
                            MainNavHost(onLoggedOut = { appViewModel.markLoggedOut() })
                        }
                    }
                }
            }
        }
    }
}
