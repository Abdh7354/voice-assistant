package com.assistant.service;

import com.assistant.model.ParsedCommand;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * ══════════════════════════════════════════════════════════
 *  NLP COMMAND PARSER
 * ══════════════════════════════════════════════════════════
 *
 * HOW IT WORKS (Rule-Based NLP):
 *
 *  Step 1 → Normalize  : lowercase, trim, remove punctuation
 *  Step 2 → Tokenize   : split into words
 *  Step 3 → Intent     : match trigger words to an intent
 *  Step 4 → Entity     : extract what comes after the trigger
 *  Step 5 → Fuzzy fix  : typo correction with Levenshtein distance
 *
 * INTENTS SUPPORTED:
 *   OPEN_APP    → "open chrome", "launch whatsapp", "start vlc"
 *   WEB_SEARCH  → "search cats", "google python", "look up java"
 *   TELL_TIME   → "what time is it", "tell me the time"
 *   TELL_DATE   → "what is today's date", "tell me the date"
 *   PLAY_MUSIC  → "play music", "play a song"
 *   OPEN_FILE   → "open my documents", "open downloads folder"
 *   SYSTEM_CMD  → "take screenshot", "lock screen", "shutdown pc"
 *   GREET       → "hello", "hi", "hey assistant"
 *   JOKE        → "tell me a joke", "say something funny"
 *   WEATHER     → "what is the weather", "weather today"
 *   HELP        → "what can you do", "help me"
 *   EXIT        → "exit", "bye", "shutdown assistant"
 *   UNKNOWN     → anything else
 * ══════════════════════════════════════════════════════════
 */
@Service
public class CommandParserService {

    // ── Intent trigger keyword groups ─────────────────────
    private static final List<String> OPEN_TRIGGERS   =
        Arrays.asList("open", "launch", "start", "run", "execute");

    private static final List<String> SEARCH_TRIGGERS =
        Arrays.asList("search", "google", "look up", "find", "lookup", "browse");

    private static final List<String> TIME_TRIGGERS   =
        Arrays.asList("time", "clock", "what time");

    private static final List<String> DATE_TRIGGERS   =
        Arrays.asList("date", "today", "day", "what day");

    private static final List<String> MUSIC_TRIGGERS  =
        Arrays.asList("play music", "play song", "play a song", "music", "play");

    private static final List<String> FILE_TRIGGERS   =
        Arrays.asList("open file", "open folder", "show folder",
                      "open documents", "open downloads", "open desktop");

    private static final List<String> SYSTEM_TRIGGERS =
        Arrays.asList("screenshot", "take screenshot", "lock", "lock screen",
                      "shutdown pc", "restart", "volume up", "volume down",
                      "mute", "unmute");

    private static final List<String> GREET_TRIGGERS  =
        Arrays.asList("hello", "hi", "hey", "good morning",
                      "good afternoon", "good evening", "what's up");

    private static final List<String> JOKE_TRIGGERS   =
        Arrays.asList("joke", "funny", "laugh", "humor");

    private static final List<String> WEATHER_TRIGGERS =
        Arrays.asList("weather", "temperature", "forecast");

    private static final List<String> HELP_TRIGGERS   =
        Arrays.asList("help", "what can you do", "commands",
                      "capabilities", "features");

    private static final List<String> EXIT_TRIGGERS   =
        Arrays.asList("exit", "bye", "goodbye", "quit",
                      "shutdown assistant", "stop");

    // ── Fuzzy matching (typo correction) ──────────────────
    private static final LevenshteinDistance LEVENSHTEIN =
        LevenshteinDistance.getDefaultInstance();

    // ── All known trigger words for fuzzy correction ──────
    private static final List<String> ALL_TRIGGERS = new ArrayList<>();
    static {
        ALL_TRIGGERS.addAll(OPEN_TRIGGERS);
        ALL_TRIGGERS.addAll(SEARCH_TRIGGERS);
        ALL_TRIGGERS.addAll(TIME_TRIGGERS);
        ALL_TRIGGERS.addAll(DATE_TRIGGERS);
        ALL_TRIGGERS.addAll(MUSIC_TRIGGERS);
    }

    // ════════════════════════════════════════════════════
    //  MAIN PARSE METHOD
    // ════════════════════════════════════════════════════
    public ParsedCommand parse(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return new ParsedCommand("UNKNOWN", "", "");
        }

        // Normalize: lowercase, trim, collapse spaces, remove punctuation
        String text = rawText.toLowerCase()
                             .trim()
                             .replaceAll("[^a-z0-9 ]", "")
                             .replaceAll("\\s+", " ");

        System.out.println("🧠 Parsing: '" + text + "'");

        // ── Priority checks (most specific first) ──────────

        if (containsAny(text, HELP_TRIGGERS))    return parse("HELP",       "",              rawText);
        if (containsAny(text, EXIT_TRIGGERS))    return parse("EXIT",       "",              rawText);
        if (containsAny(text, GREET_TRIGGERS))   return parse("GREET",      "",              rawText);
        if (containsAny(text, JOKE_TRIGGERS))    return parse("JOKE",       "",              rawText);
        if (containsAny(text, WEATHER_TRIGGERS)) return parse("WEATHER",    "",              rawText);
        if (containsAny(text, TIME_TRIGGERS))    return parse("TELL_TIME",  "",              rawText);
        if (containsAny(text, DATE_TRIGGERS))    return parse("TELL_DATE",  "",              rawText);
        if (containsAny(text, FILE_TRIGGERS))    return parse("OPEN_FILE",  extractFile(text), rawText);
        if (containsAny(text, SYSTEM_TRIGGERS))  return parse("SYSTEM_CMD", text,            rawText);
        if (containsAny(text, MUSIC_TRIGGERS))   return parse("PLAY_MUSIC", extractAfter(text, MUSIC_TRIGGERS), rawText);
        if (containsAny(text, SEARCH_TRIGGERS))  return parse("WEB_SEARCH", extractAfter(text, SEARCH_TRIGGERS), rawText);
        if (containsAny(text, OPEN_TRIGGERS))    return parse("OPEN_APP",   extractAfter(text, OPEN_TRIGGERS), rawText);

        // ── Fuzzy fallback: try correcting the first word ──
        String[] words = text.split(" ");
        String   first = words[0];
        String   best  = bestMatch(first, OPEN_TRIGGERS, 2);

        if (best != null) {
            String entity = text.substring(first.length()).trim();
            System.out.println("🔀 Fuzzy matched '" + first + "' → '" + best + "'");
            return new ParsedCommand("OPEN_APP", entity, rawText);
        }

        return new ParsedCommand("UNKNOWN", text, rawText);
    }

    // ════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════

    /** Convenience constructor wrapper */
    private ParsedCommand parse(String intent, String entity, String raw) {
        return new ParsedCommand(intent, entity.trim(), raw);
    }

    /** Check if text contains any of the trigger phrases */
    private boolean containsAny(String text, List<String> triggers) {
        for (String t : triggers) {
            if (text.contains(t)) return true;
        }
        return false;
    }

    /**
     * Extract entity after the FIRST matching trigger.
     * "open chrome please" + ["open"] → "chrome please"
     */
    private String extractAfter(String text, List<String> triggers) {
        // Sort by length desc so "look up" matches before "look"
        List<String> sorted = new ArrayList<>(triggers);
        sorted.sort((a, b) -> b.length() - a.length());

        for (String trigger : sorted) {
            int idx = text.indexOf(trigger);
            if (idx != -1) {
                String after = text.substring(idx + trigger.length()).trim();
                // Remove filler words
                after = after.replaceAll("^(for|me|the|a|an) ", "");
                return after;
            }
        }
        return text;
    }

    /** Extract a file/folder keyword from text */
    private String extractFile(String text) {
        if (text.contains("documents")) return "documents";
        if (text.contains("downloads")) return "downloads";
        if (text.contains("desktop"))   return "desktop";
        if (text.contains("music"))     return "music";
        if (text.contains("pictures"))  return "pictures";
        return "documents"; // default
    }

    /**
     * Fuzzy match: returns best matching trigger if Levenshtein distance ≤ maxDist.
     * Handles typos like "opan" → "open", "serach" → "search"
     */
    private String bestMatch(String word, List<String> candidates, int maxDist) {
        String bestWord = null;
        int    bestDist  = Integer.MAX_VALUE;

        for (String candidate : candidates) {
            int dist = LEVENSHTEIN.apply(word, candidate);
            if (dist < bestDist && dist <= maxDist) {
                bestDist = dist;
                bestWord = candidate;
            }
        }
        return bestWord;
    }
}
