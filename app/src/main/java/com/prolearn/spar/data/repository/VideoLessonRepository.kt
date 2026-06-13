package com.prolearn.spar.data.repository

import android.util.Log
import com.prolearn.spar.data.remote.ClaudeApi
import com.prolearn.spar.data.remote.ElevenLabsApi
import com.prolearn.spar.data.remote.VoiceIds
import com.prolearn.spar.domain.model.PlayableScene
import com.prolearn.spar.domain.model.VideoLesson
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "VideoLessonRepo"

/**
 * Orchestrates AI video-lesson generation end to end:
 *  1. Claude scripts the lesson (scenes + narration).
 *  2. ElevenLabs synthesizes a natural teacher voiceover for each scene.
 *
 * The result is a list of [PlayableScene]s the player can play back in sync
 * with the animated on-screen visuals.
 */
@Singleton
class VideoLessonRepository @Inject constructor(
    private val claudeApi: ClaudeApi,
    private val elevenLabsApi: ElevenLabsApi
) {
    /**
     * @param onScriptReady invoked once the lesson script is written, before voiceover.
     * @param onVoiceProgress invoked as each scene's narration is synthesized (done, total).
     */
    suspend fun generate(
        topic: String,
        onScriptReady: (VideoLesson) -> Unit = {},
        onVoiceProgress: (done: Int, total: Int) -> Unit = { _, _ -> }
    ): Result<Pair<VideoLesson, List<PlayableScene>>> = runCatching {
        val lesson = claudeApi.generateLesson(topic).getOrThrow()
        Log.d(TAG, "script ready: '${lesson.title}' with ${lesson.scenes.size} scenes")
        onScriptReady(lesson)

        val total = lesson.scenes.size
        val voiceId = VoiceIds.ARIA
        val playable = lesson.scenes.mapIndexed { index, scene ->
            val audio = elevenLabsApi.synthesizeBytes(
                text = scene.narration,
                voiceId = voiceId,
                previousText = lesson.scenes.getOrNull(index - 1)?.narration,
                nextText = lesson.scenes.getOrNull(index + 1)?.narration,
                speed = 0.9f // calm, beginner-friendly pace
            ).getOrThrow()
            onVoiceProgress(index + 1, total)
            PlayableScene(index = index, scene = scene, audio = audio)
        }

        lesson to playable
    }

    /** Answer a live doubt, grounded in the lesson and the scene the student is on. */
    suspend fun answerDoubt(
        lesson: VideoLesson,
        sceneIndex: Int,
        topic: String,
        question: String
    ): Result<String> {
        val currently = lesson.scenes.getOrNull(sceneIndex)?.let {
            "${it.heading} — ${it.narration}"
        } ?: lesson.title
        return claudeApi.answerDoubt(
            teacherName = lesson.teacherName,
            lessonTitle = lesson.title,
            topic = topic,
            currentlyLearning = currently,
            question = question
        )
    }
}
