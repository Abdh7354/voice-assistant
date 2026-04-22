package com.assistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║        VOICE CONTROLLED DESKTOP ASSISTANT                   ║
 * ║        Built with Java 17 + Spring Boot 3.2                 ║
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * HOW TO RUN:
 *   mvn spring-boot:run
 *   Then open: http://localhost:8080
 *
 * HOW TO BUILD JAR:
 *   mvn clean package
 *   java -jar target/voice-assistant-1.0.0.jar
 */
@SpringBootApplication
public class VoiceAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(VoiceAssistantApplication.class, args);

        System.out.println("\n");
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║   🎤  Voice Assistant is RUNNING!               ║");
        System.out.println("║   👉  Open: http://localhost:8080               ║");
        System.out.println("║   📝  Try typing: 'open chrome'                 ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();
    }
}
