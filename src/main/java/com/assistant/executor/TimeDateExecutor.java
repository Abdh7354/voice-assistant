package com.assistant.executor;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * ══════════════════════════════════════════════════════════
 *  TIME & DATE EXECUTOR
 * ══════════════════════════════════════════════════════════
 * Handles: "what time is it", "what is today's date"
 */
@Component
public class TimeDateExecutor {

    public String getTime() {
        // Format: "3:45 PM"
        String time = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("h:mm a"));
        return "The current time is " + time + ".";
    }

    public String getDate() {
        // Format: "Thursday, April 23, 2026"
        String date = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
        return "Today is " + date + ".";
    }

    public String getDateTime() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a"));
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d"));
        return "It is " + time + " on " + date + ".";
    }
}
