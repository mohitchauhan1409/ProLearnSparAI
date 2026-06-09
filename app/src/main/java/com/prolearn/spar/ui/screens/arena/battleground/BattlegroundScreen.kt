package com.prolearn.spar.ui.screens.arena.battleground

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.prolearn.spar.ui.screens.arena.ArenaHeader
import com.prolearn.spar.ui.screens.arena.ArenaList
import com.prolearn.spar.ui.screens.arena.ArenaPlayer
import com.prolearn.spar.ui.screens.arena.BattleArena
import com.prolearn.spar.ui.screens.arena.battleArenas

@Composable
fun BattlegroundScreen(
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var view by rememberSaveable { mutableStateOf(BattlegroundView.Home) }
    var kind by rememberSaveable { mutableStateOf(BattleKind.Erangel) }
    var arenaId by rememberSaveable { mutableStateOf(battleArenas.first().id) }
    val arena = battleArenas.first { it.id == arenaId }

    var lobbyCountdown by rememberSaveable(arena.id, kind) { mutableIntStateOf(LobbySecondsTotal) }
    var questionIndex by rememberSaveable(arena.id, kind) { mutableIntStateOf(0) }
    var readSeconds by rememberSaveable(arena.id, kind) { mutableIntStateOf(ReadSecondsTotal) }
    var answerSeconds by rememberSaveable(arena.id, kind) { mutableIntStateOf(AnswerSecondsTotal) }
    var selected by rememberSaveable(arena.id, kind) { mutableStateOf<String?>(null) }
    var score by rememberSaveable(arena.id, kind) { mutableIntStateOf(0) }
    var xp by rememberSaveable(arena.id, kind) { mutableIntStateOf(0) }
    var youHealth by rememberSaveable(arena.id, kind) { mutableIntStateOf(100) }
    var rivalHealth by rememberSaveable(arena.id, kind) { mutableIntStateOf(100) }
    val players = remember(arena.id, kind) {
        mutableStateListOf<ArenaPlayer>().apply { addAll(startingPlayers(kind)) }
    }

    fun resetBattle() {
        players.clear()
        players.addAll(startingPlayers(kind))
        lobbyCountdown = LobbySecondsTotal
        questionIndex = 0
        readSeconds = ReadSecondsTotal
        answerSeconds = AnswerSecondsTotal
        selected = null
        score = 0
        xp = 0
        youHealth = 100
        rivalHealth = 100
    }

    BackHandler {
        when (view) {
            BattlegroundView.Home -> onBack()
            BattlegroundView.ArenaPicker -> view = BattlegroundView.Home
            BattlegroundView.Playbook -> view = BattlegroundView.ArenaPicker
            BattlegroundView.Matchmaking,
            BattlegroundView.Lobby,
            BattlegroundView.Quiz,
            BattlegroundView.Results -> view = BattlegroundView.ArenaPicker
        }
    }

    AnimatedContent(
        targetState = view,
        transitionSpec = {
            val direction = if (targetState.ordinal >= initialState.ordinal) {
                AnimatedContentTransitionScope.SlideDirection.Left
            } else {
                AnimatedContentTransitionScope.SlideDirection.Right
            }
            (slideIntoContainer(direction, animationSpec = tween(340, easing = FastOutSlowInEasing)) + fadeIn(tween(180))) togetherWith
                (slideOutOfContainer(direction, animationSpec = tween(340, easing = FastOutSlowInEasing)) + fadeOut(tween(180)))
        },
        label = "battlegroundScreenTransition"
    ) { targetView ->
        when (targetView) {
            BattlegroundView.Home -> BattlegroundHomeScreen(
                snackbarHostState = snackbarHostState,
                onBack = onBack,
                onSelectKind = {
                    kind = it
                    resetBattle()
                    view = BattlegroundView.ArenaPicker
                }
            )

            BattlegroundView.ArenaPicker -> ArenaPickerScreen(
                kind = kind,
                selectedArena = arena,
                onPlaybook = {
                    arenaId = it.id
                    view = BattlegroundView.Playbook
                },
                onStart = {
                    arenaId = it.id
                    resetBattle()
                    view = BattlegroundView.Matchmaking
                }
            )

            BattlegroundView.Playbook -> BattlePlaybookScreen(
                kind = kind,
                arena = arena,
                onBack = { view = BattlegroundView.ArenaPicker },
                onStart = {
                    resetBattle()
                    view = BattlegroundView.Matchmaking
                }
            )

            BattlegroundView.Matchmaking -> BattlegroundStageList(kind, arena, onExit = { view = BattlegroundView.ArenaPicker }) {
                StudentMatchingScreen(
                    kind = kind,
                    arena = arena,
                    players = players,
                    onComplete = { view = BattlegroundView.Lobby }
                )
            }

            BattlegroundView.Lobby -> BattlegroundStageList(kind, arena, onExit = { view = BattlegroundView.ArenaPicker }) {
                BattleStartScreen(
                    kind = kind,
                    arena = arena,
                    countdown = lobbyCountdown,
                    players = players,
                    onTick = { lobbyCountdown = it },
                    onComplete = { view = BattlegroundView.Quiz }
                )
            }

            BattlegroundView.Quiz -> BattlegroundStageList(kind, arena, onExit = { view = BattlegroundView.ArenaPicker }) {
                LiveBattleScreen(
                    kind = kind,
                    arena = arena,
                    questionIndex = questionIndex,
                    readSeconds = readSeconds,
                    answerSeconds = answerSeconds,
                    selected = selected,
                    score = score,
                    xp = xp,
                    youHealth = youHealth,
                    rivalHealth = rivalHealth,
                    players = players,
                    onReadTick = { readSeconds = it },
                    onAnswerTick = { answerSeconds = it },
                    onSelect = { selected = it },
                    onApplyResult = { nextScore, nextXp, nextYouHealth, nextRivalHealth ->
                        score = nextScore
                        xp = nextXp
                        youHealth = nextYouHealth
                        rivalHealth = nextRivalHealth
                    },
                    onNextQuestion = {
                        if (questionIndex == arena.questions.lastIndex || youHealth <= 0 || rivalHealth <= 0) {
                            view = BattlegroundView.Results
                        } else {
                            questionIndex += 1
                            readSeconds = ReadSecondsTotal
                            answerSeconds = AnswerSecondsTotal
                            selected = null
                        }
                    }
                )
            }

            BattlegroundView.Results -> BattlegroundStageList(kind, arena, onExit = { view = BattlegroundView.ArenaPicker }) {
                BattleSummaryScreen(
                    kind = kind,
                    arena = arena,
                    players = players,
                    score = score,
                    baseXp = xp,
                    onPlayAgain = {
                        resetBattle()
                        view = BattlegroundView.Matchmaking
                    },
                    onChangeArena = { view = BattlegroundView.ArenaPicker }
                )
            }
        }
    }
}

@Composable
private fun BattlegroundStageList(
    kind: BattleKind,
    arena: BattleArena,
    onExit: () -> Unit,
    content: @Composable () -> Unit
) {
    ArenaList {
        item {
            ArenaHeader(
                title = arena.subject,
                subtitle = if (kind == BattleKind.Erangel) "10-player ranked battle" else "Rank-matched 1v1 duel",
                action = "Exit",
                onAction = onExit
            )
        }
        item { content() }
    }
}
