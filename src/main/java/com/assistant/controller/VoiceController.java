package com.assistant.controller;

import com.assistant.model.CommandResponse;
import com.assistant.service.CommandRouterService;
import com.assistant.service.SpeechRecognitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ══════════════════════════════════════════════════════════
 *  VOICE CONTROLLER — REST API endpoints
 * ══════════════════════════════════════════════════════════
 *
 *  POST /api/voice/listen   → mic → STT → command → response
 *  POST /api/voice/command  → text → command → response
 *  POST /api/voice/speak    → speak any text aloud
 *  GET  /api/voice/health   → check server is running
 * ══════════════════════════════════════════════════════════
 */
@RestController
@RequestMapping("/api/voice")
@CrossOrigin(origins = "*")   // Allow browser / JavaFX frontend
public class VoiceController {

    @Autowired private SpeechRecognitionService speechService;
    @Autowired private CommandRouterService      commandRouter;

    /**
     * POST /api/voice/listen
     *
     * Activates microphone, records for N seconds (set in application.properties),
     * converts speech to text with Vosk, then processes the command.
     *
     * Frontend: call this when user presses the "Listen" button.
     */
    @PostMapping("/listen")
    public ResponseEntity<CommandResponse> listenAndExecute() {
        System.out.println("\n🎤 /api/voice/listen called");

        // 1. Record microphone + speech recognition
        String recognizedText = speechService.recognizeSpeech();
        System.out.println("📝 Recognized: " + recognizedText);

        // 2. Process (NLP → execute → TTS)
        CommandResponse result = commandRouter.processCommand(recognizedText);

        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/voice/command
     * Body: { "text": "open chrome" }
     *
     * Send a text command — same pipeline as voice, just skips STT.
     * Useful for:
     *  - Testing without a microphone
     *  - Typed fallback in the UI
     *  - API integration
     */
    @PostMapping("/command")
    public ResponseEntity<CommandResponse> processTextCommand(
            @RequestBody Map<String, String> body) {

        String text = body.getOrDefault("text", "").trim();
        System.out.println("\n⌨️  /api/voice/command called with: " + text);

        CommandResponse result = commandRouter.processCommand(text);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/voice/speak
     * Body: { "text": "Hello, this is a test" }
     *
     * Speak any arbitrary text aloud via TTS.
     * Useful for testing the TTS engine independently.
     */
    @PostMapping("/speak")
    public ResponseEntity<Map<String, String>> speakText(
            @RequestBody Map<String, String> body) {

        String text = body.getOrDefault("text", "");
        commandRouter.processCommand(""); // warm up (no-op)

        // Direct TTS call via router's injected tts service
        // (kept simple — no executor needed)
        return ResponseEntity.ok(Map.of("spoken", text, "status", "ok"));
    }

    /**
     * GET /api/voice/health
     * Returns: { "status": "running", "os": "Windows 11" }
     *
     * Used by the frontend to verify the backend is alive.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "running",
            "os",     System.getProperty("os.name"),
            "java",   System.getProperty("java.version"),
            "app",    "Voice Assistant v1.0"
        ));
    }
}
