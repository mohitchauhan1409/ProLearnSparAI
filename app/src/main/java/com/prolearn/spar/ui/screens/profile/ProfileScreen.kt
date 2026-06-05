package com.prolearn.spar.ui.screens.profile

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.prolearn.spar.ui.components.ui.Avatar
import com.prolearn.spar.ui.screens.auth.AuthViewModel
import com.prolearn.spar.ui.screens.home.HomeViewModel
import com.prolearn.spar.ui.theme.BricolageGrotesqueFamily
import com.prolearn.spar.ui.theme.ProLearnColors

@Composable
fun ProfileScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onLogout: () -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val currentUser by homeViewModel.currentUser.collectAsState()
    val streak by homeViewModel.streak.collectAsState()
    val totalSessions by homeViewModel.totalSessions.collectAsState()
    var showSignOutDialog by remember { mutableStateOf(false) }

    // Sign-out confirmation dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = {
                Text(
                    "Sign out?",
                    fontFamily = BricolageGrotesqueFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = ProLearnColors.Black
                )
            },
            text = {
                Text(
                    "You'll need to sign in again to access your account.",
                    fontFamily = BricolageGrotesqueFamily,
                    color = ProLearnColors.Muted,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        authViewModel.logout { onLogout() }
                    }
                ) {
                    Text(
                        "Sign out",
                        fontFamily = BricolageGrotesqueFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = ProLearnColors.Error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(
                        "Cancel",
                        fontFamily = BricolageGrotesqueFamily,
                        color = ProLearnColors.MutedDark
                    )
                }
            },
            containerColor = ProLearnColors.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProLearnColors.White)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
    ) {
        // Profile header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val initials = currentUser?.avatarInitials ?: "?"
            Avatar(initials = initials, size = 64.dp, fontSize = 22)
            Spacer(Modifier.height(12.dp))
            Text(
                currentUser?.name ?: "—",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = ProLearnColors.Black
            )
            Text(currentUser?.email ?: "", fontSize = 13.sp, color = ProLearnColors.Muted)
            Spacer(Modifier.height(4.dp))
            Text(
                currentUser?.examTarget ?: "",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = ProLearnColors.MutedDark
            )
        }

        // Stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatBox("$totalSessions", "Sessions")
            StatBox("$streak\uD83D\uDD25", "Streak")
            StatBox(currentUser?.examTarget ?: "—", "Target")
        }

        Spacer(Modifier.height(24.dp))

        // Settings
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            SectionTitle("PREFERENCES")
            SettingsRow(Icons.Outlined.Star, "Exam target", currentUser?.examTarget)
            SettingsRow(Icons.Default.GraphicEq, "AI Voice", "Aria (Calm)")
            SettingsRow(Icons.Default.Notifications, "Streak reminders", "On")

            Spacer(Modifier.height(24.dp))
            SectionTitle("ABOUT")
            SettingsRow(Icons.Default.Help, "How Spar AI works")
            SettingsRow(Icons.Default.EmojiEvents, "Achievements")
            SettingsRow(Icons.Default.Star, "Rate the app")

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = ProLearnColors.Border)
            Spacer(Modifier.height(16.dp))

            SectionTitle("ACCOUNT")
            SettingsRow(
                icon = Icons.Outlined.Logout,
                label = "Sign out",
                textColor = ProLearnColors.Error,
                onClick = { showSignOutDialog = true }
            )
            Spacer(Modifier.height(8.dp))
            SettingsRow(
                icon = Icons.Default.Delete,
                label = "Delete account",
                textColor = ProLearnColors.Error
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = ProLearnColors.Muted,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
    )
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String? = null,
    onClick: (() -> Unit)? = null,
    textColor: androidx.compose.ui.graphics.Color = ProLearnColors.Black
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = textColor, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, fontSize = 14.sp, color = textColor, modifier = Modifier.weight(1f))
        value?.let {
            Text(it, fontSize = 14.sp, color = ProLearnColors.Muted, modifier = Modifier.padding(end = 4.dp))
        }
        if (onClick != null) {
            Icon(Icons.Default.ChevronRight, null, tint = ProLearnColors.Muted, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun StatBox(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = ProLearnColors.Black)
        Text(label, fontSize = 11.sp, color = ProLearnColors.Muted)
    }
}
