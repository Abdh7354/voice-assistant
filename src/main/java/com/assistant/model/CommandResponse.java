package com.assistant.model;

/**
 * Standard JSON response returned by all REST endpoints.
 *
 * Example JSON:
 * {
 *   "heard"    : "open chrome",
 *   "intent"   : "OPEN_APP",
 *   "entity"   : "chrome",
 *   "response" : "Opening Chrome now!",
 *   "success"  : true
 * }
 */
public class CommandResponse {

    private String  heard;      // What the assistant heard / received
    private String  intent;     // Detected intent
    private String  entity;     // Extracted entity
    private String  response;   // Text response (also spoken aloud)
    private boolean success;    // Whether command executed successfully

    // ── Constructors ──────────────────────────────────────
    public CommandResponse() {}

    public CommandResponse(String heard, String intent,
                           String entity, String response, boolean success) {
        this.heard    = heard;
        this.intent   = intent;
        this.entity   = entity;
        this.response = response;
        this.success  = success;
    }

    // ── Getters & Setters ──────────────────────────────────
    public String  getHeard()              { return heard; }
    public void    setHeard(String h)      { this.heard = h; }

    public String  getIntent()             { return intent; }
    public void    setIntent(String i)     { this.intent = i; }

    public String  getEntity()             { return entity; }
    public void    setEntity(String e)     { this.entity = e; }

    public String  getResponse()           { return response; }
    public void    setResponse(String r)   { this.response = r; }

    public boolean isSuccess()             { return success; }
    public void    setSuccess(boolean s)   { this.success = s; }
}
