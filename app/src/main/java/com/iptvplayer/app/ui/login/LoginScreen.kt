package com.iptvplayer.app.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.iptvplayer.app.R

private val BrandTeal = Color(0xFF22D3EE)
private val BrandViolet = Color(0xFF8B5CF6)
private val BrandPink = Color(0xFFEC4899)

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    var profileName by remember { mutableStateOf("") }
    var serverUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var epgUrl by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }
    var passwordVisible by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val isM3uMode by viewModel.isM3uMode.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            onLoginSuccess()
        }
    }

    val fieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.White.copy(alpha = 0.08f),
        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
        disabledContainerColor = Color.White.copy(alpha = 0.05f),
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        cursorColor = BrandTeal
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF05060F),
                        Color(0xFF0B0F24),
                        Color(0xFF05060F)
                    )
                )
            )
    ) {
        // Lueur décorative derrière le logo
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
                .size(220.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(BrandViolet.copy(alpha = 0.35f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Image(
                painter = painterResource(R.drawable.logo_infinity),
                contentDescription = "Infinity Player",
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.login_app_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isM3uMode) stringResource(R.string.login_subtitle_m3u) else stringResource(R.string.login_subtitle_xtream),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Sélecteur à deux segments
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(4.dp)
            ) {
                SegmentOption(
                    label = "Xtream",
                    selected = !isM3uMode,
                    onClick = { if (isM3uMode) viewModel.toggleMode() },
                    modifier = Modifier.weight(1f)
                )
                SegmentOption(
                    label = "Lien M3U",
                    selected = isM3uMode,
                    onClick = { if (!isM3uMode) viewModel.toggleMode() },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextField(
                value = profileName,
                onValueChange = { profileName = it },
                label = { Text(stringResource(R.string.login_name_label)) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = {
                    Text(
                        if (isM3uMode) stringResource(R.string.login_url_label)
                        else stringResource(R.string.login_server_url_label)
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            if (!isM3uMode) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text(stringResource(R.string.login_username_label)) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors,
                        modifier = Modifier.weight(1f)
                    )
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.login_password_label)) },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                value = epgUrl,
                onValueChange = { epgUrl = it },
                label = { Text(stringResource(R.string.login_epg_url_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(checkedColor = BrandTeal)
                )
                Text(stringResource(R.string.login_remember), color = Color.White.copy(alpha = 0.85f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState is LoginUiState.Error) {
                val code = (uiState as LoginUiState.Error).message
                val message = when (code) {
                    "EMPTY_URL" -> stringResource(R.string.login_error_empty_url)
                    "INVALID_URL" -> stringResource(R.string.login_error_invalid_url)
                    "MISSING_CREDENTIALS" -> stringResource(R.string.login_error_missing_credentials)
                    "INVALID_M3U_LINK" -> stringResource(R.string.login_error_invalid_m3u_link)
                    "NETWORK_ERROR" -> stringResource(R.string.login_error_network)
                    else -> code
                }
                Text(
                    text = message,
                    color = Color(0xFFFF6B6B),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (uiState is LoginUiState.Loading) {
                Text(
                    text = stringResource(R.string.login_loading),
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = BrandTeal,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            GradientButton(
                text = stringResource(R.string.login_button),
                enabled = uiState !is LoginUiState.Loading,
                onClick = {
                    viewModel.connect(profileName, serverUrl, username, password, epgUrl, rememberMe)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SegmentOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (selected) {
                    Brush.horizontalGradient(listOf(BrandTeal, BrandViolet))
                } else {
                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                }
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.5f),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun GradientButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (enabled) {
                    Brush.horizontalGradient(listOf(BrandTeal, BrandViolet, BrandPink))
                } else {
                    Brush.horizontalGradient(listOf(Color.Gray, Color.Gray))
                }
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
