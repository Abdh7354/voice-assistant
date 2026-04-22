package com.assistant.executor;
import java.util.Map;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.net.URI;

@Component
public class AppLauncherExecutor {

    public String launch(String appName, Map<String, String> appPathMap) {
        try {
            appName = appName.toLowerCase().trim();

            switch (appName) {
                case "notepad":
                    Runtime.getRuntime().exec("notepad");
                    return "Opening Notepad!";

                case "calculator":
                case "calc":
                    Runtime.getRuntime().exec("calc");
                    return "Opening Calculator!";

                case "paint":
                    Runtime.getRuntime().exec("mspaint");
                    return "Opening Paint!";

                case "chrome":
                    new ProcessBuilder("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe").start();
                    return "Opening Chrome!";
                case "whatsapp":
                    Desktop.getDesktop().browse(new URI("https://web.whatsapp.com"));
                    return "Opening WhatsApp Web!";

                case "youtube":
                    Desktop.getDesktop().browse(new URI("https://www.youtube.com"));
                    return "Opening YouTube!";

                default:
                    new ProcessBuilder("cmd", "/c", "start", "", appName).start();
                    return "Trying to open " + appName + "!";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Could not open " + appName;
        }
    }
}