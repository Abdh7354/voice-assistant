package com.assistant.service;

import com.assistant.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * ══════════════════════════════════════════════════════════
 *  SPEECH RECOGNITION SERVICE
 * ══════════════════════════════════════════════════════════
 *
 * Two modes:
 *
 *  1. VOSK MODE (recommended) — offline, no internet, no API key
 *     Requires: vosk-java.jar + vosk-model folder
 *     Toggle:   set VOSK_ENABLED = true (after adding Vosk dependency)
 *
 *  2. SIMULATION MODE (default, works out of the box)
 *     Records audio, prints confirmation, returns a demo string.
 *     Used so you can test everything without Vosk setup.
 *
 * ══════════════════════════════════════════════════════════
 */
@Service
public class SpeechRecognitionService {

    // ── Switch this to TRUE after adding Vosk JAR + model ──
    private static final boolean VOSK_ENABLED = false;

    @Autowired
    private AppConfig config;

    /**
     * Main entry point — records microphone audio and returns recognized text.
     * Returns lowercase trimmed string like "open chrome".
     */
    public String recognizeSpeech() {
        if (VOSK_ENABLED) {
            return recognizeWithVosk();
        } else {
            return recognizeWithSimulation();
        }
    }

    // ════════════════════════════════════════════════════
    //  VOSK RECOGNITION
    //  Uncomment full body after adding vosk-java.jar
    // ════════════════════════════════════════════════════
    private String recognizeWithVosk() {
        /*
         * STEP-BY-STEP VOSK SETUP:
         * ─────────────────────────
         * 1. Download vosk-java-0.3.45.jar from:
         *    https://github.com/alphacep/vosk-api/releases
         *
         * 2. Install to local Maven repo:
         *    mvn install:install-file -Dfile=vosk-java-0.3.45.jar \
         *      -DgroupId=net.java.dev.vosk -DartifactId=vosk \
         *      -Dversion=0.3.45 -Dpackaging=jar
         *
         * 3. Download vosk-model-small-en-us-0.15 (~40MB) from:
         *    https://alphacephei.com/vosk/models
         *    Extract → src/main/resources/vosk-model/
         *
         * 4. Set VOSK_ENABLED = true above
         *
         * 5. Uncomment the code block below:
         *
         * ─────────────────────────────────────────────────
         *
         * try {
         *     org.vosk.Model model = new org.vosk.Model(config.getVoskModelPath());
         *
         *     AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
         *     DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
         *
         *     if (!AudioSystem.isLineSupported(info)) {
         *         return "error: microphone not supported";
         *     }
         *
         *     TargetDataLine mic = (TargetDataLine) AudioSystem.getLine(info);
         *     mic.open(format);
         *     mic.start();
         *
         *     StringBuilder result = new StringBuilder();
         *
         *     try (org.vosk.Recognizer rec = new org.vosk.Recognizer(model, 16000)) {
         *         byte[] buffer = new byte[4096];
         *         long end = System.currentTimeMillis() + config.getListenDurationMs();
         *
         *         while (System.currentTimeMillis() < end) {
         *             int n = mic.read(buffer, 0, buffer.length);
         *             if (rec.acceptWaveForm(buffer, n)) {
         *                 String json = rec.getResult();
         *                 result.append(extractText(json)).append(" ");
         *             }
         *         }
         *         result.append(extractText(rec.getFinalResult()));
         *     }
         *
         *     mic.stop();
         *     mic.close();
         *     return result.toString().trim().toLowerCase();
         *
         * } catch (Exception e) {
         *     return "error: " + e.getMessage();
         * }
         */
        return "vosk not configured";
    }

    // ════════════════════════════════════════════════════
    //  SIMULATION MODE — records audio but returns demo text
    //  This lets you demo the full system without Vosk
    // ════════════════════════════════════════════════════
    private String recognizeWithSimulation() {
        System.out.println("🎤 [SIMULATION] Microphone active for "
                + config.getListenDurationMs() / 1000 + " seconds...");

        try {
            // Actually open the microphone to show it works
            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (AudioSystem.isLineSupported(info)) {
                TargetDataLine mic = (TargetDataLine) AudioSystem.getLine(info);
                mic.open(format);
                mic.start();

                byte[] buffer = new byte[4096];
                long end = System.currentTimeMillis() + config.getListenDurationMs();
                while (System.currentTimeMillis() < end) {
                    mic.read(buffer, 0, buffer.length);
                }

                mic.stop();
                mic.close();
                System.out.println("🎤 [SIMULATION] Recording complete.");
            } else {
                System.out.println("🎤 [SIMULATION] No microphone found — skipping capture.");
                Thread.sleep(config.getListenDurationMs());
            }

        } catch (Exception e) {
            System.out.println("🎤 [SIMULATION] Mic error: " + e.getMessage());
        }

        // In simulation, return a fixed demo command.
        // The /api/voice/command endpoint is how you actually send text.
        return "simulation mode: use /api/voice/command to send text commands";
    }

    // ════════════════════════════════════════════════════
    //  UTILITY: Extract text from Vosk JSON result
    //  Vosk returns: {"text": "open chrome"}
    // ════════════════════════════════════════════════════
    public String extractText(String json) {
        if (json == null || !json.contains("\"text\"")) return "";
        try {
            int start = json.indexOf("\"text\"") + 9;
            int end   = json.lastIndexOf("\"");
            if (start < end) return json.substring(start, end).trim();
        } catch (Exception ignored) {}
        return "";
    }
}
