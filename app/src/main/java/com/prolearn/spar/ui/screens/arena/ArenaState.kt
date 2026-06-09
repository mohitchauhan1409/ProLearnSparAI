package com.prolearn.spar.ui.screens.arena

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.prolearn.spar.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

object ArenaXpStore {
    private val _totalXp = MutableStateFlow(420)
    val totalXp: StateFlow<Int> = _totalXp

    fun addXp(amount: Int) {
        if (amount > 0) _totalXp.update { it + amount }
    }

    fun spendXp(amount: Int): Boolean {
        if (amount <= 0) return true
        val current = _totalXp.value
        if (current < amount) return false
        _totalXp.value = current - amount
        return true
    }

    fun rankFor(xp: Int): String = when {
        xp >= 1200 -> "Legend"
        xp >= 800 -> "Diamond"
        xp >= 500 -> "Gold"
        xp >= 250 -> "Silver"
        else -> "Bronze"
    }
}

data class BattleArena(
    val id: String,
    val subject: String,
    val subtitle: String,
    val exam: String,
    val difficulty: String,
    val players: Int,
    val maxXp: Int,
    val accent: Color,
    @DrawableRes val imageRes: Int,
    val questionPool: String,
    val description: String,
    val questions: List<ArenaQuestion>
)

data class ArenaPlayer(
    val name: String,
    val grade: String,
    val avatar: String,
    @DrawableRes val avatarRes: Int,
    val score: Int = 0,
    val xp: Int = 0,
    val answeredIn: Int? = null,
    val streak: Int = 0,
    val health: Int = 100
)

data class ArenaQuestion(
    val prompt: String,
    val options: List<String>,
    val answer: String,
    val tag: String = "Curated",
    val chapter: String = "Core Concept",
    val explanation: String = "Review the key idea and try the next question with the same method."
)

val battlePlayers = listOf(
    ArenaPlayer("You", "JEE Main · Gold", "YO", R.drawable.arena_avatar_student_01),
    ArenaPlayer("Aarav", "JEE Main · Gold", "AA", R.drawable.arena_avatar_student_02),
    ArenaPlayer("Mira", "JEE Advanced · Gold", "MI", R.drawable.arena_avatar_student_03),
    ArenaPlayer("Kabir", "JEE Main · Silver", "KA", R.drawable.arena_avatar_student_04),
    ArenaPlayer("Isha", "JEE Main · Gold", "IS", R.drawable.arena_avatar_student_05),
    ArenaPlayer("Riya", "JEE Main · Silver", "RI", R.drawable.arena_avatar_student_06),
    ArenaPlayer("Naina", "JEE Advanced · Gold", "NA", R.drawable.arena_avatar_student_07),
    ArenaPlayer("Dev", "JEE Main · Gold", "DE", R.drawable.arena_avatar_student_08),
    ArenaPlayer("Tara", "JEE Main · Silver", "TA", R.drawable.arena_avatar_student_09),
    ArenaPlayer("Vivaan", "JEE Advanced · Gold", "VI", R.drawable.arena_avatar_student_10)
)

val duelPlayers = listOf(
    battlePlayers.first(),
    ArenaPlayer("Naina", "JEE Advanced · Gold", "NA", R.drawable.arena_avatar_student_07)
)

val battleArenas = listOf(
    BattleArena(
        id = "physics",
        subject = "Physics Erangel",
        subtitle = "Mechanics, waves, electricity",
        exam = "JEE Main",
        difficulty = "Rank-matched",
        players = 10,
        maxXp = 95,
        accent = Coral,
        imageRes = R.drawable.arena_subject_physics,
        questionPool = "PYQs 2024-2026 + curated traps",
        description = "Fast conceptual physics where careless unit work gets punished and clean reasoning climbs the board.",
        questions = listOf(
            ArenaQuestion(
                "A particle moves in a straight line with acceleration a = 2t m/s^2. If its initial velocity is 3 m/s, what is its velocity at t = 4 s?",
                listOf("11 m/s", "16 m/s", "19 m/s", "35 m/s"),
                "19 m/s",
                "PYQ-style 2025",
                "Kinematics",
                "Integrate acceleration: v = 3 + integral 2t dt from 0 to 4 = 3 + 16 = 19 m/s."
            ),
            ArenaQuestion(
                "A wire of resistance R is stretched to twice its original length. Its new resistance becomes",
                listOf("R/2", "R", "2R", "4R"),
                "4R",
                "PYQ 2024",
                "Current Electricity",
                "Volume stays constant, so area halves. Resistance rho L/A becomes four times."
            ),
            ArenaQuestion(
                "In a Young's double slit experiment, fringe width is proportional to",
                listOf("d/D", "D/d", "lambda d", "1/lambda"),
                "D/d",
                "Curated",
                "Wave Optics",
                "Fringe width beta = lambda D / d, so it is proportional to D/d."
            )
        )
    ),
    BattleArena(
        id = "chemistry",
        subject = "Chemistry Erangel",
        subtitle = "Physical, organic, inorganic",
        exam = "JEE Main",
        difficulty = "Rank-matched",
        players = 10,
        maxXp = 95,
        accent = Moss,
        imageRes = R.drawable.arena_subject_chemistry,
        questionPool = "PYQs 2024-2026 + curated NCERT lines",
        description = "A tight chemistry arena mixing calculation speed with memory precision and reaction logic.",
        questions = listOf(
            ArenaQuestion(
                "For a first order reaction, the time required for 75% completion is",
                listOf("t1/2", "2t1/2", "3t1/2", "4t1/2"),
                "2t1/2",
                "PYQ 2025",
                "Chemical Kinetics",
                "75% completion leaves 25%, which is two half-lives."
            ),
            ArenaQuestion(
                "Which has the highest first ionization enthalpy?",
                listOf("B", "C", "N", "O"),
                "N",
                "Curated",
                "Periodic Trends",
                "Nitrogen has half-filled p orbitals, making electron removal comparatively harder."
            ),
            ArenaQuestion(
                "The major product of hydration of propene in acidic medium is",
                listOf("Propan-1-ol", "Propan-2-ol", "Propanal", "Acetone"),
                "Propan-2-ol",
                "PYQ-style 2024",
                "Hydrocarbons",
                "Markovnikov addition places OH on the more substituted carbon."
            )
        )
    ),
    BattleArena(
        id = "maths",
        subject = "Maths Erangel",
        subtitle = "Calculus, algebra, coordinate",
        exam = "JEE Main",
        difficulty = "Rank-matched",
        players = 10,
        maxXp = 95,
        accent = Blue,
        imageRes = R.drawable.arena_subject_math,
        questionPool = "PYQs 2024-2026 + speed drills",
        description = "A precision maths battle with short windows, clean options, and instant leaderboard pressure.",
        questions = listOf(
            ArenaQuestion(
                "If f(x) = x^3 - 3x + 2, then f'(1) is",
                listOf("-3", "0", "2", "3"),
                "0",
                "PYQ-style 2025",
                "Differentiation",
                "f'(x) = 3x^2 - 3, so f'(1) = 0."
            ),
            ArenaQuestion(
                "The number of real roots of x^2 + 2x + 5 = 0 is",
                listOf("0", "1", "2", "Infinitely many"),
                "0",
                "Curated",
                "Quadratic Equations",
                "The discriminant is 4 - 20 = -16, so no real roots."
            ),
            ArenaQuestion(
                "The slope of the line 3x - 4y + 8 = 0 is",
                listOf("-3/4", "3/4", "4/3", "-4/3"),
                "3/4",
                "PYQ 2024",
                "Straight Lines",
                "Rewrite as y = 3x/4 + 2, so the slope is 3/4."
            )
        )
    ),
    BattleArena(
        id = "aptitude",
        subject = "Aptitude Erangel",
        subtitle = "Speed maths, logic, patterns",
        exam = "Placement + Olympiad",
        difficulty = "Rank-matched",
        players = 10,
        maxXp = 95,
        accent = Gold,
        imageRes = R.drawable.arena_subject_aptitude,
        questionPool = "Curated reasoning sets",
        description = "A quick-thinking arena for pattern recognition, speed arithmetic, and calm elimination.",
        questions = listOf(
            ArenaQuestion(
                "A train crosses a pole in 12 seconds at 54 km/h. What is the length of the train?",
                listOf("120 m", "150 m", "180 m", "216 m"),
                "180 m",
                "Curated",
                "Time and Speed",
                "54 km/h = 15 m/s. Distance = 15 x 12 = 180 m."
            ),
            ArenaQuestion(
                "Find the next term: 3, 6, 12, 24, ?",
                listOf("30", "36", "42", "48"),
                "48",
                "Curated",
                "Series",
                "Each term doubles."
            ),
            ArenaQuestion(
                "If A is B's brother and C is A's mother, how is C related to B?",
                listOf("Sister", "Mother", "Aunt", "Grandmother"),
                "Mother",
                "Curated",
                "Blood Relations",
                "A and B are siblings, so A's mother is also B's mother."
            )
        )
    )
)

val arenaQuestions = battleArenas
    .flatMap { it.questions }
    .take(6)
