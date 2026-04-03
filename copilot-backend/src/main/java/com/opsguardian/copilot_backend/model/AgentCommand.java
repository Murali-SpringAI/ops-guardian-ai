package com.opsguardian.copilot_backend.model;

public class AgentCommand {

    private String action;
    private String target;

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
}
