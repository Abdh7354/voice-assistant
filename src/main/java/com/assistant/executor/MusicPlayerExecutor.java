package com.assistant.executor;

import com.assistant.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * ══════════════════════════════════════════════════════════
 *  MUSIC PLAYER EXECUTOR
 * ══════════════════════════════════════════════════════════
 *
 * Handles: "play music", "play a song"
 *
 * Strategy:
 *  1. Open music folder in file manager (user picks the song)
 *  2. If specific song name given, try to find & play it
 *  3. Fallback: open the system's default music player
 * ══════════════════════════════════════════════════════════
 */
@Component
public class MusicPlayerExecutor {

    @Autowired
    private AppConfig config;

    private final String os = System.getProperty("os.name").toLowerCase();

    // Supported audio extensions
    private static final List<String> AUDIO_EXTENSIONS =
        Arrays.asList(".mp3", ".wav", ".flac", ".aac", ".ogg", ".m4a");

    public String play(String songName) {

        // If a specific song is requested, try to find it
        if (songName != null && !songName.isBlank()
                && !songName.equalsIgnoreCase("music")
                && !songName.equalsIgnoreCase("song")) {
            return playSpecificSong(songName.trim());
        }

        // Otherwise, open the Music folder
        return openMusicFolder();
    }

    /** Try to find a song file by name in the music folder */
    private String playSpecificSong(String name) {
        String folderPath = config.getMusicFolderPath()
            .replace("%USERNAME%", System.getProperty("user.name"));

        File folder = new File(folderPath);

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File f : files) {
                    String fname = f.getName().toLowerCase();
                    if (fname.contains(name.toLowerCase()) && isAudioFile(f)) {
                        return openFile(f);
                    }
                }
            }
        }

        // Specific song not found — open folder instead
        System.out.println("🎵 Song '" + name + "' not found. Opening music folder.");
        return openMusicFolder();
    }

    /** Open the configured music folder in the file manager */
    private String openMusicFolder() {
        String path = config.getMusicFolderPath()
            .replace("%USERNAME%", System.getProperty("user.name"));

        File folder = new File(path);

        // If configured path doesn't exist, try OS default music folder
        if (!folder.exists()) {
            folder = new File(System.getProperty("user.home"), "Music");
        }

        if (folder.exists()) {
            return openFile(folder);
        }

        return "Music folder not found. Please set 'music.folder.path' in application.properties.";
    }

    /** Open a file or folder using the OS default handler */
    private String openFile(File file) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
                return "Opening " + file.getName() + ". Enjoy the music! 🎵";
            }

            // Fallback using OS commands
            if (os.contains("win")) {
                new ProcessBuilder("explorer", file.getAbsolutePath()).start();
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", file.getAbsolutePath()).start();
            } else {
                new ProcessBuilder("xdg-open", file.getAbsolutePath()).start();
            }

            return "Opening music: " + file.getName();

        } catch (Exception e) {
            return "Could not open music: " + e.getMessage();
        }
    }

    private boolean isAudioFile(File f) {
        String name = f.getName().toLowerCase();
        return AUDIO_EXTENSIONS.stream().anyMatch(name::endsWith);
    }
}
