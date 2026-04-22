package com.assistant.service;

import org.springframework.stereotype.Service;

/**
 * ══════════════════════════════════════════════════════════
 *  TEXT-TO-SPEECH SERVICE
 * ══════════════════════════════════════════════════════════
 *
 * Uses the OS built-in TTS — zero dependencies needed!
 *
 *  Windows → PowerShell SpeechSynthesizer (high quality, SAPI voices)
 *  macOS   → say command
 *  Linux   → espeak (install: sudo apt install espeak)
 *
 * All three methods work without any Java library.
 * The response is also printed to console as fallback.
 * ══════════════════════════════════════════════════════════
 */
@Service
public class TextToSpeechService {

    private final String os = System.getProperty("os.name").toLowerCase();

    /**
     * Speaks the given text using the OS voice engine.
     * Non-blocking: speech runs in a background thread.
     */
    public void speak(String text) {
        if (text == null || text.isBlank()) return;

        System.out.println("🔊 Speaking: " + text);

        // Run TTS in background so the HTTP response isn't delayed
        Thread ttsThread = new Thread(() -> {
            try {
                if (os.contains("win")) {
                    speakWindows(text);
                } else if (os.contains("mac")) {
                    speakMac(text);
                } else {
                    speakLinux(text);
                }
            } catch (Exception e) {
                System.err.println("TTS error: " + e.getMessage());
            }
        });
        ttsThread.setDaemon(true);
        ttsThread.start();
    }

    // ── Windows: PowerShell built-in SAPI voice ────────────
    private void speakWindows(String text) throws Exception {
        // Sanitize: remove single quotes to avoid script injection
        String safe = text.replace("'", "").replace("\"", "");

        String script = String.format(
            "Add-Type -AssemblyName System.Speech; " +
            "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
            "$s.Rate = 0; " +   // Rate: -10 (slowest) to 10 (fastest), 0 = normal
            "$s.Volume = 100; " +
            "$s.Speak('%s');", safe);

        new ProcessBuilder("powershell", "-Command", script)
            .redirectErrorStream(true)
            .start()
            .waitFor();
    }

    // ── macOS: say command ─────────────────────────────────
    private void speakMac(String text) throws Exception {
        new ProcessBuilder("say", "-r", "175", text)  // 175 words per minute
            .redirectErrorStream(true)
            .start()
            .waitFor();
    }

    // ── Linux: espeak ──────────────────────────────────────
    private void speakLinux(String text) throws Exception {
        // espeak: -s 150 = 150 words/min, -p 50 = pitch 50
        new ProcessBuilder("espeak", "-s", "150", "-p", "50", text)
            .redirectErrorStream(true)
            .start()
            .waitFor();
    }

    /**
     * Blocking version — waits for speech to finish before returning.
     * Use this when you need speech to complete before the next action.
     */
    public void speakAndWait(String text) {
        if (text == null || text.isBlank()) return;
        System.out.println("🔊 Speaking (blocking): " + text);
        try {
            if (os.contains("win")) {
                speakWindows(text);
            } else if (os.contains("mac")) {
                speakMac(text);
            } else {
                speakLinux(text);
            }
        } catch (Exception e) {
            System.err.println("TTS error: " + e.getMessage());
        }
    }
}
