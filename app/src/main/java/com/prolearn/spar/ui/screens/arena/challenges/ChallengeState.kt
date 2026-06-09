package com.prolearn.spar.ui.screens.arena.challenges

import com.prolearn.spar.ui.screens.arena.ArenaQuestion

internal enum class ChallengeType { Pdf, YouTube }

internal data class ChallengeSetup(
    val type: ChallengeType,
    val difficulty: String,
    val subject: String,
    val topic: String,
    val goal: String,
    val contentLength: String,
    val durationSeconds: Int,
    val maxXp: Int = 60
)

internal data class ChallengeResult(
    val type: ChallengeType,
    val setup: ChallengeSetup,
    val title: String,
    val score: Int,
    val total: Int,
    val readingSeconds: Int,
    val answeringSeconds: Int,
    val xpEarned: Int,
    val questions: List<ArenaQuestion>,
    val answers: Map<Int, String>
)

internal data class PdfDemoContent(
    val title: String,
    val pages: List<Pair<String, List<String>>>
)

internal val challengeSubjects = listOf("Physics", "Chemistry", "Maths", "Aptitude")
internal val challengeDifficulties = listOf("Easy", "Medium", "Hard")
internal val challengeGoals = listOf("Quick revision", "Exam practice", "Deep focus")
internal val pdfPageLengthOptions = listOf("1-2 pages", "2-4 pages", "4-6 pages", "6-8 pages")
internal val videoLengthOptions = listOf("5-10 min", "10-20 min", "20-30 min", "30-45 min")

internal fun demoPdfSetup() = ChallengeSetup(
    type = ChallengeType.Pdf,
    difficulty = "Medium",
    subject = "Physics",
    topic = "Electric Current and Ohm's Law",
    goal = "Exam practice",
    contentLength = "2-4 pages",
    durationSeconds = 120
)

internal fun demoVideoSetup() = ChallengeSetup(
    type = ChallengeType.YouTube,
    difficulty = "Medium",
    subject = "Physics",
    topic = "Electric Current and Circuits",
    goal = "Exam practice",
    contentLength = "20-30 min",
    durationSeconds = 1530
)

internal fun defaultTopicFor(type: ChallengeType) =
    if (type == ChallengeType.Pdf) "Electric Current and Ohm's Law" else "Electric Current and Circuits"

internal fun formatSeconds(seconds: Int): String {
    val minutes = seconds / 60
    val remaining = seconds % 60
    return "$minutes:${remaining.toString().padStart(2, '0')}"
}

internal fun calculateChallengeXp(
    questions: List<ArenaQuestion>,
    answers: Map<Int, String>,
    readingSeconds: Int,
    answeringSeconds: Int,
    readingLimitSeconds: Int,
    maxXp: Int
): Int {
    if (questions.isEmpty()) return 0
    val correct = questions.indices.count { answers[it] == questions[it].answer }
    if (correct == 0) return 0

    val accuracyRatio = correct.toFloat() / questions.size
    val accuracyXp = maxXp * 0.72f * accuracyRatio

    val minimumFocus = readingLimitSeconds * 0.25f
    val readingTarget = readingLimitSeconds * 0.72f
    val readingRatio = when {
        readingSeconds < minimumFocus -> (readingSeconds / minimumFocus).coerceIn(0.15f, 0.75f)
        else -> (readingTarget / readingSeconds.coerceAtLeast(1)).coerceIn(0.35f, 1f)
    }
    val readingXp = maxXp * 0.12f * readingRatio * accuracyRatio

    val answerTarget = questions.size * 18f
    val answerRatio = (answerTarget / answeringSeconds.coerceAtLeast(1)).coerceIn(0.25f, 1f)
    val answerXp = maxXp * 0.16f * answerRatio * accuracyRatio

    return (accuracyXp + readingXp + answerXp).toInt().coerceIn(0, maxXp)
}

internal val pdfDemoContent = PdfDemoContent(
    title = "Electric Current and Ohm's Law",
    pages = listOf(
        "Electric Current and Ohm's Law" to listOf(
            "Electric current is the rate at which electric charge flows through a conductor. If Q coulombs of charge pass through a cross-section in t seconds, current I = Q/t. The SI unit of current is ampere.",
            "A potential difference, or voltage, pushes charges through a circuit. Resistance opposes this flow. For many metallic conductors at constant temperature, voltage and current are directly proportional.",
            "Ohm's Law states V = IR, where V is potential difference in volts, I is current in amperes, and R is resistance in ohms."
        ),
        "Worked example" to listOf(
            "Example: A resistor has resistance 4 ohm and carries a current of 2 A. The potential difference across it is V = IR = 2 x 4 = 8 V.",
            "If the voltage is doubled while resistance stays constant, current also doubles. If resistance increases while voltage stays constant, current decreases.",
            "Always check units before substituting values. Current is measured in ampere, voltage in volt, and resistance in ohm."
        ),
        "Reading the V-I graph" to listOf(
            "For an ohmic conductor, the graph between potential difference V and current I is a straight line through the origin. This means current is directly proportional to voltage.",
            "The slope of a V-I graph helps identify resistance. A steeper V versus I graph means a larger resistance because more voltage is needed for the same current.",
            "If the graph bends, the conductor is not obeying Ohm's Law under those conditions, often because temperature has changed."
        ),
        "Common exam traps" to listOf(
            "Do not mix up current and charge. Current is not the amount of charge; it is the rate at which charge flows.",
            "When resistance is fixed, doubling voltage doubles current. When voltage is fixed, increasing resistance reduces current.",
            "In numerical questions, write the formula first, substitute units carefully, and check whether the answer should be in ampere, volt, or ohm."
        )
    )
)

internal val pdfQuestions = listOf(
    ArenaQuestion(
        "Electric current is best defined as...",
        listOf("Energy stored per charge", "Rate of flow of charge", "Resistance per unit length", "Heat produced per second"),
        "Rate of flow of charge",
        "PDF demo",
        "Current",
        "Current is charge flowing per unit time: I = Q/t."
    ),
    ArenaQuestion(
        "Which equation represents Ohm's Law?",
        listOf("P = VI", "V = IR", "I = VR", "R = VI"),
        "V = IR",
        "PDF demo",
        "Ohm's Law",
        "For an ohmic conductor at constant temperature, potential difference equals current multiplied by resistance."
    ),
    ArenaQuestion(
        "A 4 ohm resistor carries 2 A current. What is the voltage?",
        listOf("2 V", "4 V", "8 V", "16 V"),
        "8 V",
        "Worked example",
        "Numericals",
        "Use V = IR. So V = 2 x 4 = 8 V."
    ),
    ArenaQuestion(
        "If voltage doubles and resistance stays constant, current...",
        listOf("Halves", "Doubles", "Becomes zero", "Does not change"),
        "Doubles",
        "PDF demo",
        "Proportionality",
        "From I = V/R, current is directly proportional to voltage when resistance is constant."
    )
)

internal val youtubeQuestions = listOf(
    ArenaQuestion(
        "In a simple circuit, current flows when there is...",
        listOf("Only a resistor", "A closed conducting path and voltage source", "Only an open switch", "No potential difference"),
        "A closed conducting path and voltage source",
        "Video demo",
        "Circuit basics",
        "A closed path plus a source of potential difference allows charge to flow."
    ),
    ArenaQuestion(
        "Resistance mainly acts to...",
        listOf("Oppose current", "Create charge", "Remove voltage units", "Stop all atoms"),
        "Oppose current",
        "Video demo",
        "Resistance",
        "Resistance is the opposition offered to current in a conductor."
    ),
    ArenaQuestion(
        "For an ohmic resistor, the V-I graph is usually...",
        listOf("A circle", "A straight line through the origin", "A random curve", "A horizontal line only"),
        "A straight line through the origin",
        "Video demo",
        "Graph",
        "Ohmic conductors have voltage directly proportional to current, giving a straight line."
    ),
    ArenaQuestion(
        "Which unit belongs to resistance?",
        listOf("Ampere", "Volt", "Ohm", "Coulomb"),
        "Ohm",
        "Video demo",
        "Units",
        "Resistance is measured in ohm."
    )
)
