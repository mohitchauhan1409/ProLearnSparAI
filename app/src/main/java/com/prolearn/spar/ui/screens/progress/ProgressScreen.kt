package com.prolearn.spar.ui.screens.progress

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prolearn.spar.constants.Curriculum
import com.prolearn.spar.ui.components.ui.ProLearnCard
import com.prolearn.spar.ui.components.ui.ProLearnProgressBar
import com.prolearn.spar.ui.theme.ProLearnColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProgressScreen(
    viewModel: com.prolearn.spar.ui.screens.home.HomeViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val streak by viewModel.streak.collectAsState()
    val bestStreak by viewModel.bestStreak.collectAsState()
    val last7Days by viewModel.last7Days.collectAsState()

    val days = remember(last7Days) {
        if (last7Days.isEmpty()) List(7) { false }
        else last7Days.split(",").map { it.toBoolean() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProLearnColors.White)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            "Progress",
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = ProLearnColors.Black,
            letterSpacing = (-1).sp
        )
        Spacer(Modifier.height(24.dp))

        // Streak
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${streak}\uD83D\uDD25",
                fontSize = 48.sp,
                fontWeight = FontWeight.SemiBold,
                color = ProLearnColors.Black
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    "day streak",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = ProLearnColors.Black
                )
                Text("Best: ${bestStreak} days", fontSize = 12.sp, color = ProLearnColors.Muted)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            days.forEach { sparred ->
                Box(
                    Modifier
                        .size(8.dp)
                        .background(
                            if (sparred) ProLearnColors.Black else ProLearnColors.Border,
                            CircleShape
                        )
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Subject breakdown
        Text(
            "Subjects",
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = ProLearnColors.Black
        )
        Spacer(Modifier.height(12.dp))

        var expandedSubject by remember { mutableStateOf<String?>(null) }

        Curriculum.subjects.forEach { subject ->
            val isExpanded = expandedSubject == subject
            val rotation by animateFloatAsState(if (isExpanded) 180f else 0f)

            ProLearnCard(modifier = Modifier.padding(vertical = 4.dp)) {
                Column {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { expandedSubject = if (isExpanded) null else subject },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            subject,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = ProLearnColors.Black,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Default.KeyboardArrowDown, null, tint = ProLearnColors.Muted,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(rotation)
                        )
                    }
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            val chapters = Curriculum.getChapters(subject)
                            chapters.forEach { chapter ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        chapter,
                                        fontSize = 13.sp,
                                        color = ProLearnColors.MutedDark,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    ProLearnProgressBar(
                                        progress = (50..95).random() / 100f,
                                        modifier = Modifier.width(60.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Concept heatmap
        Text(
            "Concepts",
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = ProLearnColors.Black
        )
        Spacer(Modifier.height(12.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val sampleConcepts = listOf(
                "Kinematics" to 85, "Newton's Laws" to 60, "Work-Energy" to 45,
                "Electrostatics" to 72, "Bohr Model" to 55, "VSEPR" to 90,
                "Limits" to 78, "Integration" to 35, "Genetics" to 68
            )
            sampleConcepts.forEach { (name, mastery) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(
                            ProLearnColors.Black.copy(
                                alpha = (mastery / 100f).coerceIn(
                                    0.12f,
                                    1f
                                )
                            )
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = ProLearnColors.White
                    )
                }
            }
        }
    }
}
