package com.assistant.executor;

import org.springframework.stereotype.Component;

import java.awt.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * ══════════════════════════════════════════════════════════
 *  WEB SEARCH EXECUTOR
 * ══════════════════════════════════════════════════════════
 *
 * Handles: "search python tutorials", "google best laptops 2024"
 *
 * Opens the default system browser with a Google search URL.
 * Uses java.awt.Desktop (built into JDK — no extra dependencies).
 * ══════════════════════════════════════════════════════════
 */
@Component
public class WebSearchExecutor {

    private final String os = System.getProperty("os.name").toLowerCase();

    /**
     * Open default browser with Google search for the given query.
     */
    public String search(String query) {
        if (query == null || query.isBlank()) {
            return "Please tell me what to search for.";
        }

        query = query.trim();
        System.out.println("🔍 Searching Google for: '" + query + "'");

        try {
            // URL-encode the query to handle spaces and special chars
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url     = "https://www.google.com/search?q=" + encoded;

            openUrl(url);
            return "Searching Google for: " + query;

        } catch (Exception e) {
            return "Search failed: " + e.getMessage();
        }
    }

    /**
     * Opens any URL in the default browser.
     * Also used by other executors (weather, YouTube, etc.)
     */
    public String openUrl(String url) throws Exception {

        // Method 1: java.awt.Desktop (cleanest, cross-platform)
        if (Desktop.isDesktopSupported()
                && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(url));
            return "Opened: " + url;
        }

        // Method 2: OS command fallback
        if (os.contains("win")) {
            new ProcessBuilder("cmd", "/c", "start", url)
                .redirectErrorStream(true).start();
        } else if (os.contains("mac")) {
            new ProcessBuilder("open", url)
                .redirectErrorStream(true).start();
        } else {
            new ProcessBuilder("xdg-open", url)
                .redirectErrorStream(true).start();
        }

        return "Opened: " + url;
    }

    /** Open YouTube search */
    public String searchYoutube(String query) throws Exception {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        openUrl("https://www.youtube.com/results?search_query=" + encoded);
        return "Searching YouTube for: " + query;
    }

    /** Open Wikipedia */
    public String searchWikipedia(String query) throws Exception {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        openUrl("https://en.wikipedia.org/wiki/Special:Search?search=" + encoded);
        return "Opening Wikipedia for: " + query;
    }
}
