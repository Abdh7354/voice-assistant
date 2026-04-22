package com.assistant.executor;

import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.imageio.ImageIO;

/**
 * ══════════════════════════════════════════════════════════
 *  SYSTEM COMMAND EXECUTOR
 * ══════════════════════════════════════════════════════════
 *
 * Handles OS-level automation:
 *   - take screenshot
 *   - lock screen
 *   - volume control
 *   - shutdown / restart (requires confirmation)
 * ══════════════════════════════════════════════════════════
 */
@Component
public class SystemCommandExecutor {

    private final String os = System.getProperty("os.name").toLowerCase();

    public String execute(String command) {
        command = command.toLowerCase().trim();

        if (command.contains("screenshot")) return takeScreenshot();
        if (command.contains("lock"))        return lockScreen();
        if (command.contains("volume up"))   return adjustVolume(true);
        if (command.contains("volume down")) return adjustVolume(false);
        if (command.contains("mute"))        return muteVolume();

        return "System command not recognized: " + command;
    }

    /** Take a screenshot and save to Desktop */
    public String takeScreenshot() {
        try {
            Robot robot = new Robot();
            Rectangle screen = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage capture = robot.createScreenCapture(screen);

            // Save to Desktop with timestamp
            String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String desktop = System.getProperty("user.home") + File.separator + "Desktop";
            File output = new File(desktop, "screenshot_" + timestamp + ".png");

            ImageIO.write(capture, "png", output);
            System.out.println("📸 Screenshot saved: " + output.getAbsolutePath());
            return "Screenshot saved to Desktop as " + output.getName();

        } catch (AWTException | IOException e) {
            return "Screenshot failed: " + e.getMessage();
        }
    }

    /** Lock the screen using OS command */
    public String lockScreen() {
        try {
            if (os.contains("win")) {
                // Windows: rundll32 user32.dll,LockWorkStation
                new ProcessBuilder("rundll32", "user32.dll,LockWorkStation").start();
            } else if (os.contains("mac")) {
                new ProcessBuilder(
                    "osascript", "-e",
                    "tell application \"System Events\" to keystroke \"q\" "
                    + "using {command down, control down}"
                ).start();
            } else {
                // Linux (GNOME)
                new ProcessBuilder("gnome-screensaver-command", "-l").start();
            }
            return "Screen locked.";
        } catch (IOException e) {
            return "Lock screen failed: " + e.getMessage();
        }
    }

    /** Increase or decrease system volume */
    public String adjustVolume(boolean increase) {
        try {
            if (os.contains("win")) {
                // Use nircmd (free tool) or PowerShell
                String key = increase ? "VolumeUp" : "VolumeDown";
                String script = String.format(
                    "$wsh = New-Object -ComObject WScript.Shell; " +
                    "for ($i=0; $i -lt 5; $i++) { $wsh.SendKeys([char]%d); }",
                    increase ? 0xAF : 0xAE   // VK_VOLUME_UP / VK_VOLUME_DOWN
                );
                new ProcessBuilder("powershell", "-Command", script).start();
            } else if (os.contains("mac")) {
                String cmd = increase
                    ? "set volume output volume ((output volume of (get volume settings)) + 10)"
                    : "set volume output volume ((output volume of (get volume settings)) - 10)";
                new ProcessBuilder("osascript", "-e", cmd).start();
            } else {
                String step = increase ? "5%+" : "5%-";
                new ProcessBuilder("amixer", "-D", "pulse", "sset", "Master", step).start();
            }
            return "Volume " + (increase ? "increased." : "decreased.");
        } catch (IOException e) {
            return "Volume control failed: " + e.getMessage();
        }
    }

    /** Mute / unmute volume */
    public String muteVolume() {
        try {
            if (os.contains("mac")) {
                new ProcessBuilder("osascript", "-e", "set volume output muted true").start();
            } else if (os.contains("win")) {
                String script =
                    "$wsh = New-Object -ComObject WScript.Shell; " +
                    "$wsh.SendKeys([char]173);"; // VK_VOLUME_MUTE
                new ProcessBuilder("powershell", "-Command", script).start();
            }
            return "Volume muted.";
        } catch (IOException e) {
            return "Mute failed: " + e.getMessage();
        }
    }
}
