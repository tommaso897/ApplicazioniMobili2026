package com.example.project2026.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BarraNavigazioneSuperiore(
    onPosizioniSalvateClick: () -> Unit,
    username: String?,
    onLogoutClick: () -> Unit
) {
    var menuAperto by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color(0xFF10151C))
    ) {
        // Icona account a SINISTRA
        Box(modifier = Modifier.align(Alignment.CenterStart)) {
            IconButton(
                onClick = { menuAperto = true },
                modifier = Modifier
                    .padding(start = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Account",
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(26.dp)
                )
            }

            // Dropdown menu account
            DropdownMenu(
                expanded = menuAperto,
                onDismissRequest = { menuAperto = false },
                modifier = Modifier.background(Color(0xFF1C1C1E))
            ) {
                // Header con username
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3B82F6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (username?.firstOrNull()?.uppercaseChar() ?: "?").toString(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Text(
                                text = "  ${username ?: "Utente"}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    },
                    onClick = { /* non fa nulla, è solo info */ },
                    enabled = false
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Logout",
                            color = Color(0xFFFF453A),
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    onClick = {
                        menuAperto = false
                        onLogoutClick()
                    }
                )
            }
        }

        // Titolo centrato
        Text(
            text = "PARK MATE",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.Center)
        )

        // Icona Bookmarks a DESTRA
        IconButton(
            onClick = onPosizioniSalvateClick,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Bookmarks,
                contentDescription = "Posizioni Salvate",
                tint = Color(0xFF4087FA),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
