package com.assistant.service;

import com.assistant.config.AppConfig;
import com.assistant.executor.*;
import com.assistant.model.CommandResponse;
import com.assistant.model.ParsedCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * ══════════════════════════════════════════════════════════
 *  COMMAND ROUTER SERVICE
 * ══════════════════════════════════════════════════════════
 *
 * The central hub:
 *   raw text → NLP parse → correct executor → response → TTS
 *
 * All REST controllers call processCommand() with voice/text input.
 * ══════════════════════════════════════════════════════════
 */
@Service
public class CommandRouterService {

    @Autowired private CommandParserService  parser;
    @Autowired private TextToSpeechService   tts;
    @Autowired private AppConfig             appConfig;
    @Autowired private Map<String, String>   appPathMap;

    // ── Executors ──────────────────────────────────────────
    @Autowired private AppLauncherExecutor   appLauncher;
    @Autowired private WebSearchExecutor     webSearch;
    @Autowired private TimeDateExecutor      timeDate;
    @Autowired private MusicPlayerExecutor   musicPlayer;
    @Autowired private FileManagerExecutor   fileManager;
    @Autowired private SystemCommandExecutor systemCmd;

    // ── Jokes bank ────────────────────────────────────────
    private static final List<String> JOKES = List.of(
        "Why do Java developers wear glasses? Because they don't C sharp!",
        "How many programmers does it take to change a light bulb? None, that's a hardware problem.",
        "I would tell you a joke about UDP, but you might not get it.",
        "Why did the developer go broke? Because he used up all his cache!",
        "There are 10 types of people: those who understand binary, and those who don't.",
        "A SQL query walks into a bar, walks up to two tables and asks: Can I join you?"
    );

    private static final List<String> GREETINGS = List.of(
        "Hello! I'm your voice assistant. Ready to help!",
        "Hi there! What can I do for you today?",
        "Hey! I'm listening. What would you like me to do?",
        "Good to hear from you! How can I assist?"
    );

    private final Random random = new Random();

    // ════════════════════════════════════════════════════
    //  MAIN ENTRY POINT
    //  Called by both VoiceController and TextController
    // ════════════════════════════════════════════════════
    public CommandResponse processCommand(String input) {

        System.out.println("\n── New Command ──────────────────────────────");
        System.out.println("📥 Input: " + input);

        if (input == null || input.isBlank()) {
            return respond("", "UNKNOWN", "",
                "I didn't catch that. Please try again.", false);
        }

        // Step 1: Parse intent + entity via NLP
        ParsedCommand cmd = parser.parse(input);
        System.out.println("🧠 Intent: " + cmd.getIntent() + " | Entity: " + cmd.getEntity());

        // Step 2: Route to correct executor
        String responseText = route(cmd);

        // Step 3: Speak the response aloud
        tts.speak(responseText);

        // Step 4: Build and return structured response
        return respond(input, cmd.getIntent(), cmd.getEntity(), responseText, true);
    }

    // ════════════════════════════════════════════════════
    //  ROUTER — maps intent → executor
    // ════════════════════════════════════════════════════
    private String route(ParsedCommand cmd) {
        return switch (cmd.getIntent()) {

            case "OPEN_APP"   -> appLauncher.launch(cmd.getEntity(), appPathMap);

            case "WEB_SEARCH" -> {
                try {
                    yield webSearch.search(cmd.getEntity());
                } catch (Exception e) {
                    yield "Web search failed: " + e.getMessage();
                }
            }

            case "TELL_TIME"  -> timeDate.getTime();

            case "TELL_DATE"  -> timeDate.getDate();

            case "PLAY_MUSIC" -> musicPlayer.play(cmd.getEntity());

            case "OPEN_FILE"  -> fileManager.open(cmd.getEntity());

            case "SYSTEM_CMD" -> systemCmd.execute(cmd.getEntity().isEmpty()
                                    ? cmd.getRawText() : cmd.getEntity());

            case "GREET"      -> GREETINGS.get(random.nextInt(GREETINGS.size()));

            case "JOKE"       -> JOKES.get(random.nextInt(JOKES.size()));

            case "WEATHER"    -> {
                // Open weather in browser (weather API integration point)
                try {
                    webSearch.openUrl("https://www.google.com/search?q=weather+today");
                    yield "Opening weather information for you.";
                } catch (Exception e) {
                    yield "Could not open weather: " + e.getMessage();
                }
            }

            case "HELP"       -> buildHelpText();

            case "EXIT"       -> {
                tts.speakAndWait("Goodbye! Have a great day!");
                System.out.println("👋 Assistant shutdown requested.");
                // Note: actual JVM exit should be triggered by a dedicated endpoint
                yield "Goodbye! The assistant is shutting down.";
            }

            default -> {
                // Fallback: try Google search for anything unrecognized
                String raw = cmd.getRawText();
                System.out.println("❓ Unknown intent — falling back to web search");
                try {
                    webSearch.search(raw);
                    yield "I'm not sure about that, so I searched Google for: " + raw;
                } catch (Exception e) {
                    yield "Sorry, I didn't understand: '" + raw
                        + "'. Try saying 'help' to see what I can do.";
                }
            }
        };
    }

    // ════════════════════════════════════════════════════
    //  HELP TEXT
    // ════════════════════════════════════════════════════
    private String buildHelpText() {
        return "Here's what I can do: "
            + "Open apps like Chrome or WhatsApp. "
            + "Search Google for anything. "
            + "Tell you the time or date. "
            + "Play music. "
            + "Open folders like Documents or Downloads. "
            + "Take a screenshot. "
            + "Tell you a joke. "
            + "Check the weather. "
            + "Just speak or type a command!";
    }

    // ════════════════════════════════════════════════════
    //  BUILD RESPONSE OBJECT
    // ════════════════════════════════════════════════════
    private CommandResponse respond(String heard, String intent,
                                    String entity, String response, boolean success) {
        System.out.println("📤 Response: " + response);
        return new CommandResponse(heard, intent, entity, response, success);
    }
}
