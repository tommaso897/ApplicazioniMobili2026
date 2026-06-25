package com.example.project2026.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project2026.viewmodel.AuthState
import com.example.project2026.viewmodel.UtenteViewModel

@Composable
fun SchermataLogin(
    utenteViewModel: UtenteViewModel,
    onLoginSuccess: () -> Unit
) {
    val authState by utenteViewModel.authState.collectAsState()
    var mostraRegistrazione by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
            utenteViewModel.resetState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0A0A0F), Color(0xFF10151C), Color(0xFF0A0A0F))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo / Icona
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF1C2A3A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "PARK MATE",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                letterSpacing = 4.sp
            )
            Text(
                text = "La tua sosta, sotto controllo",
                color = Color.Gray,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 40.dp)
            )

            AnimatedVisibility(
                visible = !mostraRegistrazione,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                FormLogin(
                    authState = authState,
                    onLogin = { user, pass -> utenteViewModel.login(user, pass) },
                    onVaiRegistrazione = {
                        utenteViewModel.resetState()
                        mostraRegistrazione = true
                    }
                )
            }

            AnimatedVisibility(
                visible = mostraRegistrazione,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                FormRegistrazione(
                    authState = authState,
                    onRegistra = { user, pass, conferma -> utenteViewModel.registra(user, pass, conferma) },
                    onVaiLogin = {
                        utenteViewModel.resetState()
                        mostraRegistrazione = false
                    }
                )
            }
        }
    }
}

@Composable
private fun FormLogin(
    authState: AuthState,
    onLogin: (String, String) -> Unit,
    onVaiRegistrazione: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisibile by remember { mutableStateOf(false) }

    val errore = (authState as? AuthState.Error)?.message
    val caricamento = authState is AuthState.Loading

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CampoTesto(
            valore = username,
            onCambio = { username = it },
            etichetta = "Username",
            icona = { Icon(Icons.Default.Person, null, tint = Color.Gray) },
            azioneIme = ImeAction.Next
        )

        CampoTesto(
            valore = password,
            onCambio = { password = it },
            etichetta = "Password",
            icona = { Icon(Icons.Default.Lock, null, tint = Color.Gray) },
            nascosta = !passwordVisibile,
            trailingIcon = {
                IconButton(onClick = { passwordVisibile = !passwordVisibile }) {
                    Icon(
                        imageVector = if (passwordVisibile) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            },
            azioneIme = ImeAction.Done,
            onFatto = { onLogin(username, password) }
        )

        AnimatedVisibility(visible = errore != null) {
            Text(
                text = errore ?: "",
                color = Color(0xFFFF453A),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = { onLogin(username, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
            shape = RoundedCornerShape(14.dp),
            enabled = !caricamento
        ) {
            if (caricamento) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text("ACCEDI", fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 2.sp)
            }
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Non hai un account? ", color = Color.Gray, fontSize = 13.sp)
            Text(
                text = "Registrati",
                color = Color(0xFF3B82F6),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onVaiRegistrazione() }
            )
        }
    }
}

@Composable
private fun FormRegistrazione(
    authState: AuthState,
    onRegistra: (String, String, String) -> Unit,
    onVaiLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confermaPassword by remember { mutableStateOf("") }
    var passwordVisibile by remember { mutableStateOf(false) }

    val errore = (authState as? AuthState.Error)?.message
    val caricamento = authState is AuthState.Loading

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Crea account",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        )

        CampoTesto(
            valore = username,
            onCambio = { username = it },
            etichetta = "Username",
            icona = { Icon(Icons.Default.Person, null, tint = Color.Gray) },
            azioneIme = ImeAction.Next
        )

        CampoTesto(
            valore = password,
            onCambio = { password = it },
            etichetta = "Password",
            icona = { Icon(Icons.Default.Lock, null, tint = Color.Gray) },
            nascosta = !passwordVisibile,
            trailingIcon = {
                IconButton(onClick = { passwordVisibile = !passwordVisibile }) {
                    Icon(
                        imageVector = if (passwordVisibile) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            },
            azioneIme = ImeAction.Next
        )

        CampoTesto(
            valore = confermaPassword,
            onCambio = { confermaPassword = it },
            etichetta = "Conferma Password",
            icona = { Icon(Icons.Default.Lock, null, tint = Color.Gray) },
            nascosta = !passwordVisibile,
            azioneIme = ImeAction.Done,
            onFatto = { onRegistra(username, password, confermaPassword) }
        )

        AnimatedVisibility(visible = errore != null) {
            Text(
                text = errore ?: "",
                color = Color(0xFFFF453A),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = { onRegistra(username, password, confermaPassword) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
            shape = RoundedCornerShape(14.dp),
            enabled = !caricamento
        ) {
            if (caricamento) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text("REGISTRATI", fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 2.sp)
            }
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Hai già un account? ", color = Color.Gray, fontSize = 13.sp)
            Text(
                text = "Accedi",
                color = Color(0xFF3B82F6),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onVaiLogin() }
            )
        }
    }
}

@Composable
private fun CampoTesto(
    valore: String,
    onCambio: (String) -> Unit,
    etichetta: String,
    icona: @Composable () -> Unit,
    nascosta: Boolean = false,
    trailingIcon: (@Composable () -> Unit)? = null,
    azioneIme: ImeAction = ImeAction.Default,
    onFatto: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = valore,
        onValueChange = onCambio,
        label = { Text(etichetta) },
        leadingIcon = icona,
        trailingIcon = trailingIcon,
        visualTransformation = if (nascosta) PasswordVisualTransformation() else VisualTransformation.None,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = if (nascosta) KeyboardType.Password else KeyboardType.Text,
            imeAction = azioneIme
        ),
        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
            onDone = { onFatto?.invoke() }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1C1C1E),
            unfocusedContainerColor = Color(0xFF1C1C1E),
            focusedBorderColor = Color(0xFF3B82F6),
            unfocusedBorderColor = Color(0xFF2C2C2E),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = Color(0xFF3B82F6),
            unfocusedLabelColor = Color.Gray,
            cursorColor = Color(0xFF3B82F6)
        )
    )
}
