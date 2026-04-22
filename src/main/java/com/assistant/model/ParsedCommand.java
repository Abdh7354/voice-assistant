package com.assistant.model;

/**
 * Holds the result of NLP parsing.
 *
 * Example:
 *   Input text : "open chrome"
 *   intent     : "OPEN_APP"
 *   entity     : "chrome"
 *   rawText    : "open chrome"
 */
public class ParsedCommand {

    private String intent;    // What the user wants to do
    private String entity;    // What the action is about
    private String rawText;   // Original recognized text

    // ── Constructors ──────────────────────────────────────
    public ParsedCommand() {}

    public ParsedCommand(String intent, String entity, String rawText) {
        this.intent  = intent;
        this.entity  = entity;
        this.rawText = rawText;
    }

    // ── Getters & Setters ──────────────────────────────────
    public String getIntent()             { return intent; }
    public void   setIntent(String i)     { this.intent = i; }

    public String getEntity()             { return entity; }
    public void   setEntity(String e)     { this.entity = e; }

    public String getRawText()            { return rawText; }
    public void   setRawText(String r)    { this.rawText = r; }

    @Override
    public String toString() {
        return "ParsedCommand{intent='" + intent
             + "', entity='" + entity
             + "', raw='" + rawText + "'}";
    }
}
