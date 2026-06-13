package com.prolearn.spar.domain.model

import kotlinx.serialization.Serializable

/**
 * A fully scripted AI explainer-video lesson produced by the LLM (Claude),
 * rendered as a chalk-talk: a teacher explaining on a chalkboard.
 */
@Serializable
data class VideoLesson(
    val title: String,
    val subtitle: String,
    val teacherName: String,
    val scenes: List<VideoScene>
)

/**
 * One scene / board of the lesson.
 *
 * @param heading  Short on-screen heading written at the top of the board.
 * @param visual   Layout/colour hint. One of [SceneVisual] values.
 * @param lines    The spoken explanation, sentence by sentence. Each [SceneLine]
 *                 pairs what the teacher *says* with the concise note *written* on
 *                 the board, so the writing always matches the narration.
 * @param keyTerm  Optional featured term/definition/formula (empty string = none).
 * @param diagram  Optional chalk diagram to draw (type "none" = none).
 */
@Serializable
data class VideoScene(
    val heading: String,
    val visual: String = SceneVisual.CONCEPT,
    val lines: List<SceneLine> = emptyList(),
    val keyTerm: String? = null,
    val diagram: SceneDiagram? = null
) {
    /** Full spoken narration for this scene (used for TTS). */
    val narration: String get() = lines.joinToString(" ") { it.say.trim() }.trim()
}

/** One beat of the explanation: what is said aloud, and the short note written for it. */
@Serializable
data class SceneLine(
    val say: String,   // full spoken sentence, TTS-friendly
    val write: String  // concise chalk note shown on the board (2–5 words)
)

/**
 * A simple chalk diagram the renderer can draw from labelled nodes.
 *
 * @param type   "flow" (A → B → C), "cycle" (A → B → C → back), "parts"
 *               (a concept and its components), or "none".
 * @param nodes  The labels, in order.
 * @param caption Optional one-line caption under the diagram.
 */
@Serializable
data class SceneDiagram(
    val type: String = DiagramType.NONE,
    val nodes: List<String> = emptyList(),
    val caption: String? = null
) {
    val isPresent: Boolean
        get() = DiagramType.normalize(type) != DiagramType.NONE && nodes.isNotEmpty()
}

object DiagramType {
    const val NONE = "none"
    const val FLOW = "flow"
    const val CYCLE = "cycle"
    const val PARTS = "parts"

    fun normalize(raw: String?): String = when (raw?.trim()?.lowercase()) {
        FLOW -> FLOW
        CYCLE -> CYCLE
        PARTS -> PARTS
        else -> NONE
    }
}

/** Canonical visual-layout hints the LLM may emit for a scene. */
object SceneVisual {
    const val INTRO = "intro"
    const val CONCEPT = "concept"
    const val EXAMPLE = "example"
    const val FORMULA = "formula"
    const val COMPARISON = "comparison"
    const val SUMMARY = "summary"

    fun normalize(raw: String?): String = when (raw?.trim()?.lowercase()) {
        INTRO -> INTRO
        EXAMPLE -> EXAMPLE
        FORMULA -> FORMULA
        COMPARISON -> COMPARISON
        SUMMARY -> SUMMARY
        else -> CONCEPT
    }
}

/**
 * A scene paired with its synthesized narration audio, ready for playback.
 * [audio] is the MP3 byte stream returned by the TTS provider.
 */
data class PlayableScene(
    val index: Int,
    val scene: VideoScene,
    val audio: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlayableScene) return false
        return index == other.index && scene == other.scene && audio.contentEquals(other.audio)
    }

    override fun hashCode(): Int {
        var result = index
        result = 31 * result + scene.hashCode()
        result = 31 * result + audio.contentHashCode()
        return result
    }
}
