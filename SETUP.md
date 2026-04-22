# Voice Assistant — Setup & Run Guide

## ⚡ Quick Start (5 minutes)

### Prerequisites
- Java 17+ installed (`java -version`)
- Maven installed (`mvn -version`)
- Any IDE (IntelliJ IDEA recommended) OR command line

---

## Step 1 — Clone / Download the project
Place the project folder anywhere, e.g.:
```
C:\Projects\voice-assistant\
```

---

## Step 2 — Update application.properties

Open `src/main/resources/application.properties` and update the app paths to match your machine.

**Windows example:**
```properties
app.path.chrome=C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe
app.path.whatsapp=C:\\Users\\YourName\\AppData\\Local\\WhatsApp\\WhatsApp.exe
```

**macOS example:**
```properties
app.path.chrome=/Applications/Google Chrome.app/Contents/MacOS/Google Chrome
app.path.whatsapp=/Applications/WhatsApp.app/Contents/MacOS/WhatsApp
```

> Tip: Right-click any app → Properties → Location (Windows) to find its path.

---

## Step 3 — Run the project

### Option A: Command Line
```bash
cd voice-assistant
mvn spring-boot:run
```

### Option B: IntelliJ IDEA
1. Open IntelliJ → File → Open → select the `voice-assistant` folder
2. Wait for Maven to download dependencies
3. Open `VoiceAssistantApplication.java`
4. Click the green ▶ Run button

### Option C: Build & run JAR
```bash
mvn clean package
java -jar target/voice-assistant-1.0.0.jar
```

---

## Step 4 — Open the UI
Once the console shows:
```
╔══════════════════════════════════════════════════════╗
║   🎤  Voice Assistant is RUNNING!                   ║
║   👉  Open: http://localhost:8080                   ║
╚══════════════════════════════════════════════════════╝
```

Open your browser → `http://localhost:8080`

---

## Step 5 — Test without microphone first
Type commands in the text box:
- `open chrome`
- `open whatsapp`
- `what time is it`
- `search java spring boot`
- `tell me a joke`
- `help`

All commands work immediately — no Vosk setup needed for text input!

---

## Step 6 — Enable Voice (Vosk setup)

### 6a. Download Vosk model
- Go to: https://alphacephei.com/vosk/models
- Download: `vosk-model-small-en-us-0.15` (~40MB ZIP)
- Extract the folder
- Place it at: `src/main/resources/vosk-model/`

### 6b. Install Vosk Java library
- Download `vosk-java-0.3.45.jar` from:
  https://github.com/alphacep/vosk-api/releases
- Run this command:
```bash
mvn install:install-file \
  -Dfile=vosk-java-0.3.45.jar \
  -DgroupId=net.java.dev.vosk \
  -DartifactId=vosk \
  -Dversion=0.3.45 \
  -Dpackaging=jar
```

### 6c. Uncomment in pom.xml
Uncomment the Vosk dependency block in `pom.xml`.

### 6d. Enable in code
In `SpeechRecognitionService.java`, change:
```java
private static final boolean VOSK_ENABLED = false;
```
to:
```java
private static final boolean VOSK_ENABLED = true;
```

### 6e. Uncomment the Vosk code block
In `SpeechRecognitionService.java`, uncomment the code inside `recognizeWithVosk()`.

---

## Available REST API Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| GET  | /api/voice/health  | Check if server is running |
| POST | /api/voice/command | Send text command |
| POST | /api/voice/listen  | Trigger microphone + STT |
| POST | /api/voice/speak   | Speak any text via TTS |

### Test with curl:
```bash
curl -X POST http://localhost:8080/api/voice/command \
  -H "Content-Type: application/json" \
  -d '{"text":"open chrome"}'
```

---

## Supported Voice Commands

| Say this...                     | Does this...                    |
|---------------------------------|---------------------------------|
| "open chrome"                   | Launches Chrome browser         |
| "open whatsapp"                 | Opens WhatsApp                  |
| "open calculator"               | Opens calculator                |
| "search python tutorials"       | Googles "python tutorials"      |
| "what time is it"               | Speaks the current time         |
| "what is today's date"          | Speaks today's date             |
| "play music"                    | Opens your music folder         |
| "open documents"                | Opens Documents folder          |
| "take screenshot"               | Saves screenshot to Desktop     |
| "tell me a joke"                | Tells a programming joke        |
| "what is the weather"           | Opens Google weather            |
| "hello"                         | Greets you back                 |
| "help"                          | Lists all commands              |
| "bye" / "exit"                  | Shuts down the assistant        |

---

## Adding a New App

1. Open `application.properties`, add:
   ```properties
   app.path.zoom=C:\\Users\\YourName\\AppData\\Roaming\\Zoom\\bin\\Zoom.exe
   ```

2. Open `AppConfig.java`, add to `appPathMap()`:
   ```java
   map.put("zoom", zoomPath);
   ```

3. Add field:
   ```java
   @Value("${app.path.zoom}") private String zoomPath;
   ```

That's it — restart and say "open zoom"!

---

## Project Structure
```
voice-assistant/
├── pom.xml                          ← Maven dependencies
├── SETUP.md                         ← This file
└── src/main/
    ├── java/com/assistant/
    │   ├── VoiceAssistantApplication.java   ← Entry point
    │   ├── config/AppConfig.java            ← App paths config
    │   ├── model/
    │   │   ├── ParsedCommand.java           ← NLP result holder
    │   │   └── CommandResponse.java         ← API response model
    │   ├── controller/
    │   │   └── VoiceController.java         ← REST endpoints
    │   ├── service/
    │   │   ├── SpeechRecognitionService.java ← Vosk / mic
    │   │   ├── CommandParserService.java     ← NLP engine
    │   │   ├── CommandRouterService.java     ← Central router
    │   │   └── TextToSpeechService.java      ← OS TTS
    │   └── executor/
    │       ├── AppLauncherExecutor.java      ← Open apps
    │       ├── WebSearchExecutor.java        ← Google search
    │       ├── TimeDateExecutor.java         ← Time/Date
    │       ├── MusicPlayerExecutor.java      ← Music
    │       ├── FileManagerExecutor.java      ← Folders
    │       └── SystemCommandExecutor.java    ← Screenshot etc.
    └── resources/
        ├── application.properties   ← Config file
        ├── static/index.html        ← Frontend UI
        └── vosk-model/              ← (download separately)
```

---

## Common Issues

**"Port 8080 already in use"**
Change port in `application.properties`: `server.port=8090`

**"App not found" error**
Check the path in `application.properties`. Use absolute paths.

**TTS not working on Linux**
Install espeak: `sudo apt install espeak`

**Microphone not detected**
Grant microphone permissions to Java in OS settings.

---

*Built for B.Tech CSE Major Project Evaluation*
