package com.prolearn.spar.ui.screens.arena

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

object ArenaXpStore {
    private val _totalXp = MutableStateFlow(420)
    val totalXp: StateFlow<Int> = _totalXp

    fun addXp(amount: Int) {
        if (amount > 0) _totalXp.update { it + amount }
    }

    fun rankFor(xp: Int): String = when {
        xp >= 1200 -> "Legend"
        xp >= 800 -> "Diamond"
        xp >= 500 -> "Gold"
        xp >= 250 -> "Silver"
        else -> "Bronze"
    }
}

data class ArenaPlayer(
    val name: String,
    val grade: String,
    val avatar: String,
    val score: Int = 0,
    val health: Int = 100
)

data class ArenaQuestion(
    val prompt: String,
    val options: List<String>,
    val answer: String
)

val arenaQuestions = listOf(
    ArenaQuestion(
        "Which organelle is called the powerhouse of the cell?",
        listOf("Nucleus", "Mitochondria", "Ribosome", "Vacuole"),
        "Mitochondria"
    ),
    ArenaQuestion(
        "What is the SI unit of force?",
        listOf("Joule", "Pascal", "Newton", "Watt"),
        "Newton"
    ),
    ArenaQuestion(
        "Photosynthesis mainly produces which gas?",
        listOf("Nitrogen", "Oxygen", "Carbon dioxide", "Hydrogen"),
        "Oxygen"
    ),
    ArenaQuestion(
        "What is 3x + 2 when x = 4?",
        listOf("10", "12", "14", "16"),
        "14"
    ),
    ArenaQuestion(
        "Which particle has a negative charge?",
        listOf("Proton", "Neutron", "Electron", "Photon"),
        "Electron"
    )
)

val arenaPlayers = listOf(
    ArenaPlayer("Aarav", "Grade 10", "AA", 260),
    ArenaPlayer("Mira", "Grade 9", "MI", 240),
    ArenaPlayer("Kabir", "Grade 10", "KA", 220),
    ArenaPlayer("Isha", "Grade 11", "IS", 210),
    ArenaPlayer("Riya", "Grade 10", "RI", 190),
    ArenaPlayer("You", "Grade 10", "YO", 0)
)
