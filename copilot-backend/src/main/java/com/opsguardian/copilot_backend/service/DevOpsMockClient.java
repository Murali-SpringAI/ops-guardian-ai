package com.opsguardian.copilot_backend.service;

import org.springframework.stereotype.Component;

@Component
public class DevOpsMockClient {

    public String restartService(String service, String token) {
        // Demo: log token usage
        System.out.println("[Mock] Restart called on " + service + " with token=" + token);
        return "Service restarted: " + service;
    }

    public String scaleService(String service, String token) {
        System.out.println("[Mock] Scale called on " + service + " with token=" + token);
        return "Service scaled: " + service;
    }

    public String fetchLogs(String service, String token) {
        System.out.println("[Mock] Fetch logs for " + service + " with token=" + token);
        return "Logs for: " + service + " [demo log data]";
    }

    public String fetchMetrics(String service, String token) {
        System.out.println("[Mock] Fetch metrics for " + service + " with token=" + token);
        return "Metrics for: " + service + " [demo metrics data]";
    }
}
