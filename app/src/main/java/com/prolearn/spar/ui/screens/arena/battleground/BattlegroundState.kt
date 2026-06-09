package com.prolearn.spar.ui.screens.arena.battleground

import com.prolearn.spar.ui.screens.arena.ArenaPlayer
import com.prolearn.spar.ui.screens.arena.battlePlayers
import com.prolearn.spar.ui.screens.arena.duelPlayers

internal enum class BattleKind { Erangel, Duel }

internal enum class BattlegroundView {
    Home,
    ArenaPicker,
    Playbook,
    Matchmaking,
    Lobby,
    Quiz,
    Results
}

internal const val LobbySecondsTotal = 5
internal const val MatchmakingSecondsTotal = 10

internal fun startingPlayers(kind: BattleKind): List<ArenaPlayer> =
    if (kind == BattleKind.Erangel) battlePlayers else duelPlayers
