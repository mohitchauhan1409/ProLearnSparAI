package com.prolearn.spar.ui.screens.setup

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prolearn.spar.R
import com.prolearn.spar.domain.model.SparConfig
import com.prolearn.spar.ui.components.ui.ProLearnButton
import com.prolearn.spar.ui.components.ui.ProLearnTextField
import com.prolearn.spar.ui.theme.ProLearnColors

private val PageBg = Color(0xFFF8FAF7)
private val Ink = Color(0xFF151616)
private val Moss = Color(0xFF4E7D68)
private val LimeMist = Color(0xFFEAF6D8)
private val SkyMist = Color(0xFFEAF3FF)
private val BlushMist = Color(0xFFFFEFF3)
private val GlassStroke = Color(0x66FFFFFF)
private val SoftBorder = Color(0xFFDDE5DC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SparSetupScreen(
    onNavigateBack: () -> Unit,
    onStartSpar: (SparConfig) -> Unit,
    viewModel: SetupViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val examTarget by viewModel.examTarget.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val selectedChapter by viewModel.selectedChapter.collectAsState()
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsState()
    val selectedVoice by viewModel.selectedVoice.collectAsState()
    val selectedVoiceName by viewModel.selectedVoiceName.collectAsState()
    val studentFirstName by viewModel.studentFirstName.collectAsState()
    val previewingVoiceId by viewModel.previewingVoiceId.collectAsState()

    var selectedSessionType by remember { mutableStateOf("Learning") }
    var showSubjectSheet by remember { mutableStateOf(false) }
    var showChapterSheet by remember { mutableStateOf(false) }
    var subjectSearch by remember { mutableStateOf("") }
    var chapterSearch by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(PageBg, Color(0xFFF2F7F4), Color(0xFFFFFBF5)),
                    start = Offset.Zero,
                    end = Offset(900f, 1400f)
                )
            )
    ) {
        Box(
            Modifier
                .size(240.dp)
                .offset(x = (-88).dp, y = 70.dp)
                .clip(CircleShape)
                .background(LimeMist.copy(alpha = 0.58f))
        )
        Box(
            Modifier
                .size(210.dp)
                .align(Alignment.TopEnd)
                .offset(x = 70.dp, y = 10.dp)
                .clip(CircleShape)
                .background(SkyMist.copy(alpha = 0.7f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 112.dp)
        ) {
            TopBar(onNavigateBack)

            Column(
                modifier = Modifier.padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                HeaderCard()

                AnimatedSetupSection(index = 0, title = "Session Type") {
                    ChipRow(
                        options = listOf("Learning", "Doubt", "Practice"),
                        selected = selectedSessionType,
                        onSelect = { selectedSessionType = it }
                    )
                }

                AnimatedSetupSection(index = 1, title = "Difficulty Level") {
                    DifficultySelector(
                        options = listOf("Easy", "Medium", "Hard"),
                        selected = selectedDifficulty,
                        onSelect = viewModel::selectDifficulty
                    )
                }

                AnimatedSetupSection(index = 2, title = "Subject") {
                    SelectorField(
                        value = selectedSubject ?: "Choose subject",
                        supporting = "Curated for $examTarget",
                        icon = Icons.Default.School,
                        selected = selectedSubject != null,
                        onClick = { showSubjectSheet = true }
                    )
                }

                AnimatedSetupSection(index = 3, title = "Chapter") {
                    SelectorField(
                        value = selectedChapter ?: "Choose chapter",
                        supporting = if (selectedSubject == null) "Pick a subject first" else "Generic is available for open sessions",
                        icon = Icons.Default.MenuBook,
                        selected = selectedChapter != null,
                        enabled = selectedSubject != null,
                        onClick = { showChapterSheet = selectedSubject != null }
                    )
                }

                AnimatedSetupSection(index = 4, title = "Select AI Teacher") {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 6.dp)
                    ) {
                        items(Teachers.options) { teacher ->
                            TeacherCard(
                                teacher = teacher,
                                isSelected = selectedVoice == teacher.voiceId,
                                isPreviewing = previewingVoiceId == teacher.voiceId,
                                onSelect = {
                                    viewModel.selectVoice(
                                        teacher.voiceId,
                                        teacher.name
                                    )
                                },
                                onPreview = { viewModel.previewTeacher(teacher) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, PageBg.copy(alpha = 0.94f), PageBg)
                    )
                )
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            ProLearnButton(
                text = "Start session",
                enabled = viewModel.isValid,
                onClick = {
                    val config = SparConfig(
                        subject = selectedSubject ?: return@ProLearnButton,
                        chapter = selectedChapter ?: return@ProLearnButton,
                        sessionType = selectedSessionType,
                        concepts = viewModel.getConcepts(),
                        difficulty = selectedDifficulty,
                        examTarget = examTarget,
                        voiceId = selectedVoice,
                        voiceName = selectedVoiceName,
                        studentName = studentFirstName,
                        questionCount = 12,
                        durationMinutes = 10,
                        isGhostMode = false
                    )
                    onStartSpar(config)
                },
                backgroundColor = Ink
            )
        }
    }

    if (showSubjectSheet) {
        SelectionSheet(
            title = "Subject",
            search = subjectSearch,
            onSearchChange = { subjectSearch = it },
            placeholder = "Search subjects...",
            items = viewModel.subjects.filter {
                subjectSearch.isBlank() || it.contains(subjectSearch, ignoreCase = true)
            },
            selected = selectedSubject,
            onSelect = {
                viewModel.selectSubject(it)
                showSubjectSheet = false
                subjectSearch = ""
            },
            onDismiss = { showSubjectSheet = false }
        )
    }

    if (showChapterSheet) {
        SelectionSheet(
            title = "Chapter",
            search = chapterSearch,
            onSearchChange = { chapterSearch = it },
            placeholder = "Search chapters...",
            items = viewModel.getChapters().filter {
                chapterSearch.isBlank() || it.contains(chapterSearch, ignoreCase = true)
            },
            selected = selectedChapter,
            onSelect = {
                viewModel.selectChapter(it)
                showChapterSheet = false
                chapterSearch = ""
            },
            onDismiss = { showChapterSheet = false }
        )
    }
}

@Composable
private fun TopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Default.ArrowBack, "Back", tint = Ink)
        }
        Text(
            "New study session",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = Ink
        )
    }
}

@Composable
private fun HeaderCard() {
    GlassPanel {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(BlushMist),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, null, tint = Moss, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Design your study flow",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Text(
                    "Pick the mode, level, topic, and teacher voice.",
                    fontSize = 13.sp,
                    color = ProLearnColors.MutedDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AnimatedSetupSection(
    index: Int,
    title: String,
    content: @Composable () -> Unit
) {
    val alpha = remember { Animatable(0f) }
    val y = remember { Animatable(24f) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 70L)
        alpha.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
        y.animateTo(0f, spring(dampingRatio = 0.78f, stiffness = 180f))
    }

    Column(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha.value
            translationY = y.value
        }
    ) {
        Text(
            title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Ink,
            modifier = Modifier.padding(start = 2.dp, bottom = 10.dp)
        )
        GlassPanel(content = content)
    }
}

@Composable
private fun GlassPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.68f))
            .border(1.dp, GlassStroke, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        content()
    }
}

@Composable
private fun ChipRow(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(options) { option ->
            GlowChip(
                text = option,
                selected = selected == option,
                onClick = { onSelect(option) }
            )
        }
    }
}

@Composable
private fun GlowChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val fill by animateColorAsState(
        if (selected) Moss.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.7f),
        label = "chipFill"
    )
    val border by animateColorAsState(
        if (selected) Moss else SoftBorder,
        label = "chipBorder"
    )
    val scale by animateFloatAsState(
        if (selected) 1.03f else 1f,
        spring(dampingRatio = 0.62f),
        label = "chipScale"
    )

    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(100.dp))
            .background(fill)
            .border(if (selected) 1.7.dp else 1.dp, border, RoundedCornerShape(100.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = if (selected) Ink else ProLearnColors.MutedDark
        )
    }
}

@Composable
private fun DifficultySelector(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    val selectedIndex = options.indexOf(selected).coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(Color(0xFFEFF4EF))
            .padding(4.dp)
    ) {
        SlidingIndicator(index = selectedIndex, itemCount = options.size)
        Row(Modifier.fillMaxSize()) {
            options.forEach { option ->
                val active = selected == option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(100.dp))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            onSelect(option)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        option,
                        fontSize = 14.sp,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (active) Ink else ProLearnColors.MutedDark
                    )
                }
            }
        }
    }
}

@Composable
private fun SlidingIndicator(index: Int, itemCount: Int) {
    val animatedIndex by animateFloatAsState(
        targetValue = index.toFloat(),
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 220f),
        label = "difficultyIndicator"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth(1f / itemCount)
            .fillMaxSize()
            .graphicsLayer { translationX = size.width * animatedIndex }
            .clip(RoundedCornerShape(100.dp))
            .background(Color.White)
            .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(100.dp))
    )
}

@Composable
private fun SelectorField(
    value: String,
    supporting: String,
    icon: ImageVector,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val border by animateColorAsState(if (selected) Moss else SoftBorder, label = "selectorBorder")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = if (enabled) 0.76f else 0.45f))
            .border(if (selected) 1.6.dp else 1.dp, border, RoundedCornerShape(16.dp))
            .clickable(
                enabled = enabled,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            null,
            tint = if (enabled) Moss else ProLearnColors.Muted,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                value,
                fontSize = 15.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (enabled) Ink else ProLearnColors.Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                supporting,
                fontSize = 11.sp,
                color = ProLearnColors.Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            Icons.Default.ExpandMore,
            null,
            tint = ProLearnColors.Muted,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun TeacherCard(
    teacher: TeacherOption,
    isSelected: Boolean,
    isPreviewing: Boolean,
    onSelect: () -> Unit,
    onPreview: () -> Unit
) {
    val elevation by animateDpAsState(
        if (isSelected) 14.dp else 2.dp,
        spring(dampingRatio = 0.7f),
        label = "teacherElevation"
    )
    val lift by animateDpAsState(
        if (isSelected) (-4).dp else 0.dp,
        spring(dampingRatio = 0.7f),
        label = "teacherLift"
    )
    val border by animateColorAsState(if (isSelected) Moss else SoftBorder, label = "teacherBorder")

    Column(
        modifier = Modifier
            .width(178.dp)
            .offset(y = lift)
            .shadow(
                elevation,
                RoundedCornerShape(18.dp),
                ambientColor = Moss.copy(alpha = 0.22f),
                spotColor = Moss.copy(alpha = 0.22f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.82f))
            .border(if (isSelected) 1.8.dp else 1.dp, border, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onSelect
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) LimeMist else Color(0xFFF2F2EF))
            ) {
                Image(
                    painter = painterResource(id = teacherPortrait(teacher.voiceId)),
                    contentDescription = teacher.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.92f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, null, tint = Moss, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Column {
                Text(
                    teacher.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    teacher.specialty,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    color = ProLearnColors.MutedDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            SoftBadge(teacher.style)
            SoftBadge(teacher.language)
        }

        Spacer(Modifier.height(14.dp))

        PreviewButton(
            isPlaying = isPreviewing,
            onClick = onPreview
        )
    }
}

private fun teacherPortrait(voiceId: String): Int = when (voiceId) {
    "LHJy3mhZWsvhUjy0zUM1" -> R.drawable.teacher_pk_anil
    "MF4J4IDTRo0AxOO4dpFR" -> R.drawable.teacher_tripti
    "eUKPwd15VeaPJ9bDZ6iM" -> R.drawable.teacher_manav
    "P7vsEyTOpZ6YUTulin8m" -> R.drawable.teacher_simran
    else -> R.drawable.teacher_pk_anil
}

@Composable
private fun SoftBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(SkyMist.copy(alpha = 0.72f))
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Text(text, fontSize = 11.sp, color = Ink, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PreviewButton(
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "previewPulse")
    val pulse by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(tween(620), RepeatMode.Reverse),
        label = "micPulse"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(if (isPlaying) Moss else Ink)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            if (isPlaying) Icons.Default.Mic else Icons.Default.GraphicEq,
            null,
            tint = Color.White,
            modifier = Modifier
                .size(16.dp)
                .graphicsLayer {
                    if (isPlaying) {
                        scaleX = pulse
                        scaleY = pulse
                    }
                }
        )
        Spacer(Modifier.width(7.dp))
        Text(
            if (isPlaying) "Playing" else "Preview",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionSheet(
    title: String,
    search: String,
    onSearchChange: (String) -> Unit,
    placeholder: String,
    items: List<String>,
    selected: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = PageBg
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, null, tint = Moss, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink)
            }
            Spacer(Modifier.height(14.dp))
            ProLearnTextField(
                value = search,
                onValueChange = onSearchChange,
                placeholder = placeholder
            )
            Spacer(Modifier.height(12.dp))
            LazyColumn(modifier = Modifier.height(340.dp)) {
                items(items) { item ->
                    SheetRow(
                        item = item,
                        selected = selected == item,
                        onClick = { onSelect(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SheetRow(
    item: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) LimeMist.copy(alpha = 0.7f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            item,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = Ink,
            modifier = Modifier.weight(1f)
        )
        if (selected) Icon(Icons.Default.Check, null, tint = Moss, modifier = Modifier.size(19.dp))
    }
}
