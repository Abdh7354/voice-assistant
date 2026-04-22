package com.assistant.executor;

import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.io.File;

/**
 * ══════════════════════════════════════════════════════════
 *  FILE MANAGER EXECUTOR
 * ══════════════════════════════════════════════════════════
 *
 * Handles: "open documents", "open downloads folder",
 *           "open desktop", "open music folder"
 * ══════════════════════════════════════════════════════════
 */
@Component
public class FileManagerExecutor {

    private final String os      = System.getProperty("os.name").toLowerCase();
    private final String homeDir = System.getProperty("user.home");

    public String open(String folderName) {
        File folder = resolveFolder(folderName);

        if (folder == null || !folder.exists()) {
            return "Folder '" + folderName + "' not found on this system.";
        }

        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(folder);
            } else if (os.contains("win")) {
                new ProcessBuilder("explorer", folder.getAbsolutePath()).start();
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", folder.getAbsolutePath()).start();
            } else {
                new ProcessBuilder("xdg-open", folder.getAbsolutePath()).start();
            }
            return "Opening " + folder.getName() + " folder.";
        } catch (Exception e) {
            return "Could not open folder: " + e.getMessage();
        }
    }

    /** Map spoken folder name → File object */
    private File resolveFolder(String name) {
        if (name == null) return new File(homeDir);
        name = name.toLowerCase().trim();

        return switch (name) {
            case "documents", "document", "docs"
                -> new File(homeDir, "Documents");
            case "downloads", "download"
                -> new File(homeDir, "Downloads");
            case "desktop"
                -> new File(homeDir, "Desktop");
            case "music", "songs"
                -> new File(homeDir, "Music");
            case "pictures", "photos", "images"
                -> new File(homeDir, "Pictures");
            case "videos", "movies"
                -> new File(homeDir, "Videos");
            case "home", "user"
                -> new File(homeDir);
            default
                -> new File(homeDir, "Documents"); // fallback
        };
    }
}
