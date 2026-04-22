package com.assistant.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads all configurable values from application.properties.
 * App paths are stored in a Map so executors can look them up by name.
 */
@Configuration
public class AppConfig {

    // ── Inject paths from application.properties ──
    @Value("${app.path.chrome}")    private String chromePath;
    @Value("${app.path.whatsapp}")  private String whatsappPath;
    @Value("${app.path.notepad}")   private String notepadPath;
    @Value("${app.path.calculator}")private String calculatorPath;
    @Value("${app.path.vlc}")       private String vlcPath;
    @Value("${app.path.spotify}")   private String spotifyPath;
    @Value("${app.path.vscode}")    private String vscodePath;
    @Value("${app.path.explorer}")  private String explorerPath;
    @Value("${app.path.word}")      private String wordPath;
    @Value("${app.path.excel}")     private String excelPath;

    @Value("${music.folder.path}")  private String musicFolderPath;
    @Value("${vosk.model.path}")    private String voskModelPath;
    @Value("${voice.listen.duration.ms:6000}") private int listenDurationMs;

    /**
     * Returns a name → executable-path map.
     * The NLP engine looks up app names in this map.
     * To add a new app: add a line in application.properties and here.
     */
    @Bean
    public Map<String, String> appPathMap() {
        Map<String, String> map = new HashMap<>();

        // Browsers
        map.put("chrome",      chromePath);
        map.put("google",      chromePath);   // "open google" → chrome
        map.put("browser",     chromePath);

        // Communication
        map.put("whatsapp",    whatsappPath);

        // Productivity
        map.put("notepad",     notepadPath);
        map.put("calculator",  calculatorPath);
        map.put("calc",        calculatorPath);
        map.put("word",        wordPath);
        map.put("excel",       excelPath);
        map.put("vscode",      vscodePath);
        map.put("code",        vscodePath);
        map.put("vs code",     vscodePath);

        // Media
        map.put("vlc",         vlcPath);
        map.put("spotify",     spotifyPath);

        // File manager
        map.put("explorer",    explorerPath);
        map.put("files",       explorerPath);
        map.put("file manager",explorerPath);

        return map;
    }

    // ── Getters ──────────────────────────────────────────
    public String getMusicFolderPath()  { return musicFolderPath; }
    public String getVoskModelPath()    { return voskModelPath; }
    public int    getListenDurationMs() { return listenDurationMs; }
}
