package com.prolearn.spar.ui.screens.arena.challenges

import android.webkit.WebChromeClient
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prolearn.spar.ui.screens.arena.ArenaList
import com.prolearn.spar.ui.screens.arena.ArenaQuestion
import com.prolearn.spar.ui.screens.arena.Blue
import com.prolearn.spar.ui.screens.arena.Coral
import com.prolearn.spar.ui.screens.arena.GlassStroke
import com.prolearn.spar.ui.screens.arena.Gold
import com.prolearn.spar.ui.screens.arena.IconBadge
import com.prolearn.spar.ui.screens.arena.Ink
import com.prolearn.spar.ui.screens.arena.Moss
import com.prolearn.spar.ui.screens.arena.PageBg
import com.prolearn.spar.ui.screens.arena.Paper
import com.prolearn.spar.ui.screens.arena.PremiumButton
import com.prolearn.spar.ui.screens.arena.PremiumPanel
import com.prolearn.spar.ui.screens.arena.Sky
import com.prolearn.spar.ui.screens.arena.SoftBorder
import com.prolearn.spar.ui.theme.ProLearnColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChallengeSetupSheet(
    type: ChallengeType,
    onDismiss: () -> Unit,
    onStart: (ChallengeSetup) -> Unit,
    sheetState: SheetState
) {
    var difficulty by rememberSaveable(type) { mutableStateOf("Medium") }
    var subject by rememberSaveable(type) { mutableStateOf("Physics") }
    var topic by rememberSaveable(type) { mutableStateOf(defaultTopicFor(type)) }
    var goal by rememberSaveable(type) { mutableStateOf("Exam practice") }
    var contentLength by rememberSaveable(type) {
        mutableStateOf(if (type == ChallengeType.Pdf) "2-4 pages" else "20-30 min")
    }
    val duration = if (type == ChallengeType.Pdf) 120 else 1530
    val accent = if (type == ChallengeType.Pdf) Blue else Coral
    val title = if (type == ChallengeType.Pdf) "Set up PDF Challenge" else "Set up YouTube Challenge"
    val lengthTitle = if (type == ChallengeType.Pdf) "PDF page length" else "Video length"
    val lengthOptions = if (type == ChallengeType.Pdf) pdfPageLengthOptions else videoLengthOptions

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PageBg,
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 10.dp, bottom = 2.dp)
                    .size(width = 42.dp, height = 5.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(SoftBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp)
                .padding(bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ChallengeSetupHeader(type, title, accent)

            SetupSectionCard("Difficulty", Icons.Default.AutoAwesome, accent) {
                SegmentedChoice(challengeDifficulties, difficulty, accent) { difficulty = it }
            }

            SetupSectionCard("Subject", Icons.Default.School, accent) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(challengeSubjects) { option ->
                        ChoiceChip(option, subject == option, accent) { subject = option }
                    }
                }
            }

            SetupSectionCard("Topic", Icons.AutoMirrored.Filled.MenuBook, accent) {
                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Write a topic, e.g. Ohm's Law") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Demo content is hardcoded for Physics · ${defaultTopicFor(type)} while the flow is ready for generated content later.",
                    color = ProLearnColors.MutedDark,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            SetupSectionCard("Learning goal", Icons.Default.CheckCircle, accent) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(challengeGoals) { option ->
                        ChoiceChip(option, goal == option, accent) { goal = option }
                    }
                }
            }

            SetupSectionCard(lengthTitle, Icons.Default.FormatListNumbered, accent) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(lengthOptions) { option ->
                        ChoiceChip(option, contentLength == option, accent) { contentLength = option }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SetupStat("Time", "${duration / 60}m ${duration % 60}s", Icons.Default.Timer, accent, Modifier.weight(1f))
                SetupStat("Up to", "60 XP", Icons.Default.EmojiEvents, Gold, Modifier.weight(1f))
            }

            PremiumButton("Start challenge", Icons.Default.PlayArrow) {
                onStart(
                    ChallengeSetup(
                        type = type,
                        difficulty = difficulty,
                        subject = subject,
                        topic = topic.ifBlank { defaultTopicFor(type) },
                        goal = goal,
                        contentLength = contentLength,
                        durationSeconds = duration
                    )
                )
            }
        }
    }
}

@Composable
private fun ChallengeSetupHeader(type: ChallengeType, title: String, accent: Color) {
    PremiumPanel(accent = accent, bgColor = Color.White.copy(alpha = 0.74f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(if (type == ChallengeType.Pdf) Icons.Default.PictureAsPdf else Icons.Default.PlayCircle, accent)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
                Text(
                    if (type == ChallengeType.Pdf) "Read a compact demo PDF, then answer focused MCQs."
                    else "Watch a focused demo lesson, then answer checkpoint MCQs.",
                    fontSize = 12.sp,
                    color = ProLearnColors.MutedDark,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun SetupSectionCard(
    title: String,
    icon: ImageVector,
    accent: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.72f))
            .border(1.dp, GlassStroke, RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(8.dp))
            Text(title, color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
        }
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun SegmentedChoice(options: List<String>, selected: String, accent: Color, onSelect: (String) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(100.dp))
            .background(Sky.copy(alpha = 0.72f))
            .padding(4.dp)
    ) {
        options.forEach { option ->
            val active = option == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (active) Color.White else Color.Transparent)
                    .border(
                        if (active) 1.dp else 0.dp,
                        if (active) accent.copy(alpha = 0.28f) else Color.Transparent,
                        RoundedCornerShape(100.dp)
                    )
                    .clickable(
                        indication = null,
                        interactionSource = MutableInteractionSource(),
                        onClick = { onSelect(option) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    option,
                    color = if (active) Ink else ProLearnColors.MutedDark,
                    fontSize = 13.sp,
                    fontWeight = if (active) FontWeight.ExtraBold else FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ChoiceChip(text: String, selected: Boolean, accent: Color, onClick: () -> Unit) {
    val fill by animateColorAsState(
        if (selected) accent.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.78f),
        label = "challengeChipFill"
    )
    val border by animateColorAsState(if (selected) accent else SoftBorder, label = "challengeChipBorder")
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(fill)
            .border(if (selected) 1.6.dp else 1.dp, border, RoundedCornerShape(100.dp))
            .clickable(
                indication = null,
                interactionSource = MutableInteractionSource(),
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text,
            color = Ink,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
        )
        if (selected) {
            Spacer(Modifier.width(7.dp))
            Icon(Icons.Default.CheckCircle, null, tint = accent, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun SetupStat(label: String, value: String, icon: ImageVector, accent: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(62.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.76f))
            .border(1.dp, GlassStroke, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(icon, accent, 34)
        Spacer(Modifier.width(9.dp))
        Column {
            Text(label, color = ProLearnColors.MutedDark, fontSize = 11.sp)
            Text(value, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
internal fun StickyChallengeScaffold(
    buttonText: String,
    buttonIcon: ImageVector,
    onButton: () -> Unit,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        ArenaList(content)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, PageBg.copy(alpha = 0.96f), PageBg)
                    )
                )
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            PremiumButton(buttonText, buttonIcon, onButton)
        }
    }
}

@Composable
internal fun ChallengeProgressHeader(
    icon: ImageVector,
    accent: Color,
    title: String,
    subtitle: String,
    timer: Int,
    progress: Float
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.84f))
            .border(1.dp, GlassStroke, RoundedCornerShape(22.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon, accent, 38)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    subtitle,
                    color = ProLearnColors.MutedDark,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(accent.copy(alpha = 0.12f))
                    .border(1.dp, accent.copy(alpha = 0.22f), RoundedCornerShape(100.dp))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Timer, null, tint = accent, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(5.dp))
                Text(formatSeconds(timer), color = Ink, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(100.dp)),
            color = accent,
            trackColor = Paper
        )
    }
}

@Composable
internal fun PdfDocumentPreview(content: PdfDemoContent) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFFE9EBEF))
            .border(1.dp, SoftBorder, RoundedCornerShape(22.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        content.pages.forEachIndexed { index, page ->
            PdfPage(index + 1, page.first, page.second)
        }
    }
}

@Composable
private fun PdfPage(page: Int, title: String, paragraphs: List<String>) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE5E1D8), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("SPAR PDF", color = Blue, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.weight(1f))
            Text("Page $page", color = ProLearnColors.MutedDark, fontSize = 10.sp)
        }
        Spacer(Modifier.height(10.dp))
        Text(title, color = Ink, fontSize = 20.sp, lineHeight = 23.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(10.dp))
        paragraphs.forEach { paragraph ->
            Text(paragraph, color = Ink, fontSize = 13.sp, lineHeight = 19.sp)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
internal fun VideoLessonCard() {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.86f))
            .border(1.dp, GlassStroke, RoundedCornerShape(22.dp))
            .padding(14.dp)
    ) {
        Text("Video topic", color = Coral, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
        Text("Electric Current and Circuits", color = Ink, fontSize = 20.sp, lineHeight = 23.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(8.dp))
        Text(
            "This demo lesson focuses on current flow, closed circuits, resistance, and the V-I graph for an ohmic conductor.",
            color = ProLearnColors.MutedDark,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VideoMetaPill("Current")
            VideoMetaPill("Resistance")
            VideoMetaPill("V-I graph")
        }
    }
}

@Composable
private fun VideoMetaPill(text: String) {
    Text(
        text,
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(Coral.copy(alpha = 0.11f))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        color = Ink,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
internal fun YouTubeEmbed() {
    val context = LocalContext.current
    val videoId = "r-SCyD7f_zI"
    val embedUrl = "https://www.youtube-nocookie.com/embed/$videoId?rel=0&modestbranding=1&playsinline=1&enablejsapi=1&origin=https://www.youtube-nocookie.com"
    val playerHtml = """
        <!doctype html>
        <html>
          <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
            <style>
              html, body {
                margin: 0;
                padding: 0;
                width: 100%;
                height: 100%;
                overflow: hidden;
                background: #111111;
              }
              .player {
                position: fixed;
                inset: 0;
                width: 100%;
                height: 100%;
                background: #111111;
              }
              iframe {
                width: 100%;
                height: 100%;
                border: 0;
                background: #111111;
                display: block;
              }
            </style>
          </head>
          <body>
            <div class="player">
              <iframe
                src="$embedUrl"
                title="YouTube video player"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                referrerpolicy="strict-origin-when-cross-origin"
                allowfullscreen>
              </iframe>
            </div>
          </body>
        </html>
    """.trimIndent()
    AndroidView(
        factory = {
            WebView(context).apply {
                setBackgroundColor(android.graphics.Color.BLACK)
                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                CookieManager.getInstance().setAcceptCookie(true)
                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        Log.d(
                            "ArenaYouTube",
                            "Console ${consoleMessage?.messageLevel()}: ${consoleMessage?.message()}"
                        )
                        return true
                    }
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        Log.d("ArenaYouTube", "Page finished: $url")
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        Log.e(
                            "ArenaYouTube",
                            "Error ${error?.errorCode}: ${error?.description} url=${request?.url}"
                        )
                    }
                }
                settings.javaScriptEnabled = true
                settings.javaScriptCanOpenWindowsAutomatically = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.allowContentAccess = true
                settings.allowFileAccess = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.setSupportMultipleWindows(false)
                settings.userAgentString = "${settings.userAgentString} ProLearnArena/1.0"
                Log.d("ArenaYouTube", "Loading iframe wrapper: $embedUrl")
                loadDataWithBaseURL("https://www.youtube-nocookie.com", playerHtml, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            if (webView.url.isNullOrBlank() || webView.url == "about:blank") {
                Log.d("ArenaYouTube", "Reloading iframe wrapper: $embedUrl")
                webView.loadDataWithBaseURL("https://www.youtube-nocookie.com", playerHtml, "text/html", "UTF-8", null)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16 / 9f)
            .clip(RoundedCornerShape(22.dp))
            .background(Ink)
    )
}

@Composable
internal fun ChallengeMcqStage(
    title: String,
    subtitle: String,
    questions: List<ArenaQuestion>,
    answers: Map<Int, String>,
    accent: Color,
    onAnswer: (Int, String) -> Unit,
    onSubmit: () -> Unit
) {
    PremiumPanel(accent = accent) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Default.CheckCircle, accent)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
                Text(subtitle, color = ProLearnColors.MutedDark, fontSize = 12.sp, lineHeight = 17.sp)
            }
            Text(
                "${answers.size}/${questions.size}",
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(accent.copy(alpha = 0.14f))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                color = Ink,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(Modifier.height(6.dp))
        Text("Up to 60 XP", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(14.dp))
        questions.forEachIndexed { index, question ->
            QuestionCard(index, question, answers[index], accent) { onAnswer(index, it) }
            Spacer(Modifier.height(12.dp))
        }
        PremiumButton("Submit answers", Icons.Default.CheckCircle, onSubmit)
    }
}

@Composable
private fun QuestionCard(
    index: Int,
    question: ArenaQuestion,
    selected: String?,
    accent: Color,
    onAnswer: (String) -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Paper)
            .border(1.dp, SoftBorder, RoundedCornerShape(22.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Q${index + 1}",
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(accent.copy(alpha = 0.14f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                color = Ink,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.width(8.dp))
            Text(question.chapter, color = ProLearnColors.MutedDark, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(10.dp))
        Text(question.prompt, color = Ink, fontSize = 16.sp, lineHeight = 21.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(12.dp))
        question.options.forEach { option ->
            ChallengeAnswerOption(option, selected, question.answer) { onAnswer(option) }
            Spacer(Modifier.height(7.dp))
        }
        if (selected != null) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (selected == question.answer) Moss.copy(alpha = 0.12f) else Coral.copy(alpha = 0.12f))
                    .padding(11.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    if (selected == question.answer) Icons.Default.CheckCircle else Icons.Default.Close,
                    null,
                    tint = if (selected == question.answer) Moss else Coral,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(question.explanation, color = Ink, fontSize = 12.sp, lineHeight = 17.sp)
            }
        }
    }
}

@Composable
private fun ChallengeAnswerOption(option: String, selected: String?, answer: String, onClick: () -> Unit) {
    val isSelected = selected == option
    val isCorrect = selected != null && option == answer
    val color = when {
        isCorrect -> Moss
        isSelected -> Coral
        else -> Color.White
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected == null) Color.White.copy(alpha = 0.78f) else color.copy(alpha = 0.14f))
            .border(1.dp, if (isCorrect || isSelected) color else SoftBorder, RoundedCornerShape(18.dp))
            .clickable(enabled = selected == null, onClick = onClick)
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(option, Modifier.weight(1f), color = Ink, fontWeight = FontWeight.SemiBold, lineHeight = 19.sp)
        if (selected != null && (isCorrect || isSelected)) {
            Icon(
                if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
