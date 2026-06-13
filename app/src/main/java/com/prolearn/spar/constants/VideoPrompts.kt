package com.prolearn.spar.constants

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/**
 * System prompt that turns Claude into a patient chalkboard teacher.
 * The model returns a structured lesson (enforced by [videoLessonSchema]) that the
 * app renders as a chalk-talk: the teacher speaks each line while the matching note
 * is written on the board, with optional diagrams drawn to explain visually.
 */
fun buildVideoLessonSystemPrompt(): String = """
You are a warm, patient teacher giving a one-to-one chalkboard lesson to a COMPLETE
BEGINNER who knows nothing about the topic. Your goal is that, by the end, they
genuinely understand it and could explain it to a friend.

Your output is a JSON lesson made of ordered SCENES (chalkboards). Each scene has:
- heading: a short board title (3 to 6 words).
- lines: the explanation, sentence by sentence. Each line is an object:
    - say:  ONE short spoken sentence (what the teacher says out loud). 8 to 20 words.
            Natural, calm, spoken English. No markdown, symbols, emoji, or stage directions.
            Spell math and symbols as words ("x squared", "the square root of two", "H two O").
    - write: a SHORT chalk note for that same sentence (2 to 5 words) — the key phrase a
            teacher would write while saying it. It must MATCH the meaning of "say".
- keyTerm: optional featured term, definition, or simple formula. Empty string "" if none.
- diagram: optional simple diagram. An object:
    - type: "flow" (steps A then B then C), "cycle" (repeating loop), "parts"
            (a thing and its pieces), or "none".
    - nodes: 2 to 5 short labels (1 to 3 words each), in order. Empty list if type is "none".
    - caption: one short line under the diagram, or "".
- visual: one of: intro, concept, example, formula, comparison, summary.

HOW TO TEACH (very important):
- Assume ZERO prior knowledge. Never use a word the beginner wouldn't know without first
  explaining it in plain language.
- Go slowly and in small steps. One idea per line. Build from the simplest intuition up.
- Use a concrete, everyday analogy before any formal idea.
- Prefer simple, short words. Short sentences. A calm, friendly pace — like talking to a
  curious 12-year-old.
- The first scene (visual "intro") warmly greets the learner, says what they'll learn and
  why it's useful, in plain words. Not "hello everyone".
- Use a diagram whenever it would make a process, cycle, or structure clearer.
- The last scene (visual "summary") recaps the 2 to 3 key takeaways in the simplest words,
  and ends with a short encouraging line.
- Be accurate, but clarity for a beginner always wins over completeness.

SOUND LIKE A REAL TEACHER (not an AI):
- Talk TO the learner ("you"), warmly and personally. Use natural connectors like
  "okay", "so", "now", "here's the thing", "notice that", "let's see".
- Ask the learner small rhetorical questions, then answer them ("Why does that happen?
  Well…").
- React like a person ("this part trips a lot of people up", "this is the cool bit").
- Vary sentence length. Avoid stiff, listy, textbook phrasing. No buzzwords.
- Draw a diagram whenever a process, cycle, or set of parts is easier shown than said.

STRUCTURE:
- 7 to 10 scenes. 3 to 5 lines per scene.
- Do not reference "this video", "this scene", "the board", or the JSON.

Return ONLY the JSON lesson matching the provided schema.
""".trimIndent()

/** The user turn: the topic the student wants a lesson on. */
fun buildVideoLessonUserPrompt(topic: String, teacherName: String): String = """
Teach this topic from scratch to a complete beginner:

"${topic.trim()}"

The teacher's name is "$teacherName". Write the full lesson now as JSON.
""".trimIndent()

/**
 * Strict JSON Schema for the lesson, used with Anthropic's `output_config.format`.
 * Structured outputs require `additionalProperties: false` on every object and all
 * properties listed in `required`; "optional" fields are modelled as empty-string /
 * type:"none" sentinels instead of being omitted.
 */
fun videoLessonSchema(): JsonObject = buildJsonObject {
    put("type", "object")
    put("additionalProperties", false)
    putJsonObject("properties") {
        putJsonObject("title") { put("type", "string"); put("description", "Short lesson title.") }
        putJsonObject("subtitle") { put("type", "string"); put("description", "One line on what the learner will understand.") }
        putJsonObject("teacherName") { put("type", "string") }
        putJsonObject("scenes") {
            put("type", "array")
            put("description", "Ordered chalkboards, 7 to 10 of them.")
            putJsonObject("items") {
                put("type", "object")
                put("additionalProperties", false)
                putJsonObject("properties") {
                    putJsonObject("heading") { put("type", "string") }
                    putJsonObject("visual") {
                        put("type", "string")
                        putJsonArray("enum") {
                            add("intro"); add("concept"); add("example")
                            add("formula"); add("comparison"); add("summary")
                        }
                    }
                    putJsonObject("keyTerm") {
                        put("type", "string")
                        put("description", "Featured term/formula, or empty string.")
                    }
                    putJsonObject("lines") {
                        put("type", "array")
                        put("description", "The explanation, sentence by sentence (3 to 5).")
                        putJsonObject("items") {
                            put("type", "object")
                            put("additionalProperties", false)
                            putJsonObject("properties") {
                                putJsonObject("say") { put("type", "string"); put("description", "One spoken sentence.") }
                                putJsonObject("write") { put("type", "string"); put("description", "Short chalk note matching 'say'.") }
                            }
                            putJsonArray("required") { add("say"); add("write") }
                        }
                    }
                    putJsonObject("diagram") {
                        put("type", "object")
                        put("additionalProperties", false)
                        putJsonObject("properties") {
                            putJsonObject("type") {
                                put("type", "string")
                                putJsonArray("enum") { add("none"); add("flow"); add("cycle"); add("parts") }
                            }
                            putJsonObject("nodes") {
                                put("type", "array")
                                putJsonObject("items") { put("type", "string") }
                            }
                            putJsonObject("caption") { put("type", "string") }
                        }
                        putJsonArray("required") { add("type"); add("nodes"); add("caption") }
                    }
                }
                putJsonArray("required") { add("heading"); add("visual"); add("keyTerm"); add("lines"); add("diagram") }
            }
        }
    }
    putJsonArray("required") { add("title"); add("subtitle"); add("teacherName"); add("scenes") }
}
