package com.prolearn.spar.ui.screens.profile

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.prolearn.spar.ui.components.ui.Avatar
import com.prolearn.spar.ui.screens.auth.AuthViewModel
import com.prolearn.spar.ui.screens.home.HomeViewModel
import com.prolearn.spar.ui.theme.BricolageGrotesqueFamily
import com.prolearn.spar.ui.theme.ProLearnColors
import kotlinx.coroutines.delay

private val PageBg = Color(0xFFF8FAF7)
private val Ink = Color(0xFF151616)
private val Moss = Color(0xFF4E7D68)
private val LimeMist = Color(0xFFEAF6D8)
private val SkyMist = Color(0xFFEAF3FF)
private val BlushMist = Color(0xFFFFEFF3)
private val SoftBorder = Color(0xFFDDE5DC)
private val GlassStroke = Color(0x88FFFFFF)

private data class FaqItem(val question: String, val answer: String)

@OptIn(ExperimentalLayoutApi::class)
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
    val streakReminders by homeViewModel.streakRemindersEnabled.collectAsState()
    var showSignOutDialog by remember { mutableStateOf(false) }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = {
                Text(
                    "Sign out?",
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    fontFamily = BricolageGrotesqueFamily
                )
            },
            text = {
                Text(
                    "You will need to sign in again to continue your study sessions.",
                    color = ProLearnColors.MutedDark,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontFamily = BricolageGrotesqueFamily
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        authViewModel.logout { onLogout() }
                    }
                ) {
                    Text("Sign out", color = ProLearnColors.Error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel", color = ProLearnColors.MutedDark, fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(PageBg, Color(0xFFF2F7F4), Color(0xFFFFFBF5)),
                    start = Offset.Zero,
                    end = Offset(900f, 1500f)
                )
            )
    ) {
        ProfileAmbient()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedProfileBlock(index = 0) {
                ProfileHero(
                    initials = currentUser?.avatarInitials ?: "?",
                    name = currentUser?.name ?: "Learner",
                    email = currentUser?.email.orEmpty(),
                    examTarget = currentUser?.examTarget ?: "JEE Advanced",
                    streak = streak,
                    sessions = totalSessions
                )
            }

            AnimatedProfileBlock(index = 1) {
                SettingsPanel(
                    title = "Study settings",
                    icon = Icons.Default.School
                ) {
                    Text(
                        "Exam target",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        fontFamily = BricolageGrotesqueFamily
                    )
                    Spacer(Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("JEE Mains", "JEE Advanced", "NEET", "CUET").forEach { exam ->
                            ExamChip(
                                text = exam,
                                selected = currentUser?.examTarget == exam,
                                onClick = { homeViewModel.updateExamTarget(exam) }
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    ReminderToggle(
                        checked = streakReminders,
                        onCheckedChange = homeViewModel::setStreakRemindersEnabled
                    )
                }
            }

            AnimatedProfileBlock(index = 2) {
                SettingsPanel(
                    title = "FAQs",
                    icon = Icons.Default.AutoAwesome
                ) {
                    val faqs = remember {
                        listOf(
                            FaqItem(
                                "What is a study session?",
                                "A study session is a voice-first round where the AI asks, listens, challenges your reasoning, and helps you correct weak steps."
                            ),
                            FaqItem(
                                "How does exam target affect my learning?",
                                "Your target shapes the tone, examples, and question pressure so sessions feel closer to the exam you are preparing for."
                            ),
                            FaqItem(
                                "Do streak reminders change my sessions?",
                                "No. They only control reminder nudges. Your study data and session difficulty stay the same."
                            ),
                            FaqItem(
                                "Can I change this later?",
                                "Yes. You can update your exam target here anytime, and new sessions will use the latest target."
                            )
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        faqs.forEach { item ->
                            FaqCard(item)
                        }
                    }
                }
            }

            AnimatedProfileBlock(index = 3) {
                SignOutButton(onClick = { showSignOutDialog = true })
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ProfileHero(
    initials: String,
    name: String,
    email: String,
    examTarget: String,
    streak: Int,
    sessions: Int
) {
    SettingsPanel {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .shadow(18.dp, CircleShape, ambientColor = Moss.copy(alpha = 0.18f), spotColor = Moss.copy(alpha = 0.18f))
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, GlassStroke, CircleShape)
                    .padding(3.dp)
            ) {
                Avatar(initials = initials, size = 68.dp, fontSize = 22)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                name,
                fontSize = 26.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Ink,
                textAlign = TextAlign.Center,
                fontFamily = BricolageGrotesqueFamily
            )
            Text(
                email,
                fontSize = 13.sp,
                color = ProLearnColors.Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = BricolageGrotesqueFamily
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MiniStat("$streak", "day streak", Modifier.weight(1f))
                MiniStat("$sessions", "sessions", Modifier.weight(1f))
                MiniStat(examTarget, "target", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SettingsPanel(
    title: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(18.dp, RoundedCornerShape(28.dp), ambientColor = Moss.copy(alpha = 0.1f), spotColor = Moss.copy(alpha = 0.1f))
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White.copy(alpha = 0.76f))
            .border(1.dp, GlassStroke, RoundedCornerShape(28.dp))
            .padding(14.dp)
    ) {
        if (title != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Box(
                        Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(LimeMist.copy(alpha = 0.72f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null, tint = Moss, modifier = Modifier.size(17.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                }
                Text(
                    title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    fontFamily = BricolageGrotesqueFamily
                )
            }
            Spacer(Modifier.height(14.dp))
        }
        content()
    }
}

@Composable
private fun MiniStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(LimeMist.copy(alpha = 0.48f))
            .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontFamily = BricolageGrotesqueFamily
        )
        Text(
            label,
            fontSize = 10.sp,
            color = ProLearnColors.MutedDark,
            maxLines = 1,
            fontFamily = BricolageGrotesqueFamily
        )
    }
}

@Composable
private fun ExamChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val fill by animateColorAsState(if (selected) Moss.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.74f), label = "examFill")
    val border by animateColorAsState(if (selected) Moss else SoftBorder, label = "examBorder")
    val scale by animateFloatAsState(if (selected) 1.03f else 1f, spring(dampingRatio = 0.72f), label = "examScale")

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(100.dp))
            .background(fill)
            .border(if (selected) 1.7.dp else 1.dp, border, RoundedCornerShape(100.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 13.dp, vertical = 10.dp)
    ) {
        Text(
            text,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            color = Ink,
            fontFamily = BricolageGrotesqueFamily
        )
    }
}

@Composable
private fun ReminderToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SkyMist.copy(alpha = 0.48f))
            .border(1.dp, Color.White.copy(alpha = 0.78f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.78f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Notifications, null, tint = Moss, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Streak reminders", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text(
                if (checked) "Daily nudge is active" else "Daily nudge is paused",
                fontSize = 12.sp,
                color = ProLearnColors.MutedDark
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Moss,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = SoftBorder
            )
        )
    }
}

@Composable
private fun FaqCard(item: FaqItem) {
    var expanded by rememberSaveable(item.question) { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.72f))
            .border(1.dp, SoftBorder, RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { expanded = !expanded }
            )
            .padding(13.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                item.question,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Ink,
                modifier = Modifier.weight(1f),
                fontFamily = BricolageGrotesqueFamily
            )
            Icon(
                Icons.Default.ExpandMore,
                null,
                tint = ProLearnColors.MutedDark,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer { rotationZ = if (expanded) 180f else 0f }
            )
        }
        if (expanded) {
            Spacer(Modifier.height(8.dp))
            Text(
                item.answer,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = ProLearnColors.MutedDark,
                fontFamily = BricolageGrotesqueFamily
            )
        }
    }
}

@Composable
private fun SignOutButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.985f else 1f, spring(dampingRatio = 0.72f), label = "signOutScale")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(22.dp))
            .background(ProLearnColors.ErrorSurface.copy(alpha = 0.9f))
            .border(1.dp, ProLearnColors.Error.copy(alpha = 0.18f), RoundedCornerShape(22.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.Logout, null, tint = ProLearnColors.Error, modifier = Modifier.size(19.dp))
        Spacer(Modifier.width(9.dp))
        Text(
            "Sign out",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = ProLearnColors.Error,
            fontFamily = BricolageGrotesqueFamily
        )
    }
}

@Composable
private fun AnimatedProfileBlock(index: Int, content: @Composable () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val y = remember { Animatable(22f) }

    LaunchedEffect(Unit) {
        delay(index * 70L)
        alpha.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
        y.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 180f))
    }

    Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha.value
            translationY = y.value
        }
    ) {
        content()
    }
}

@Composable
private fun BoxScope.ProfileAmbient() {
    val infiniteTransition = rememberInfiniteTransition(label = "profileAmbient")
    val drift by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(2800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "profileDrift"
    )
    Box(
        Modifier
            .size(240.dp)
            .offset(x = (-88).dp, y = (76 + drift).dp)
            .clip(CircleShape)
            .background(LimeMist.copy(alpha = 0.58f))
    )
    Box(
        Modifier
            .size(210.dp)
            .align(Alignment.TopEnd)
            .offset(x = 70.dp, y = (14 - drift).dp)
            .clip(CircleShape)
            .background(SkyMist.copy(alpha = 0.68f))
    )
    Box(
        Modifier
            .size(190.dp)
            .align(Alignment.BottomEnd)
            .offset(x = 54.dp, y = (-90).dp)
            .clip(CircleShape)
            .background(BlushMist.copy(alpha = 0.58f))
    )
}
