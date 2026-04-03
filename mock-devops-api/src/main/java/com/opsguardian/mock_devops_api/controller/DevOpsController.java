package com.opsguardian.mock_devops_api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DevOpsController {

    @GetMapping("/metrics")
    private Map<String, Object> getMetrics(){

        return Map.of(
              "cpu", 92,
                "memory", 72,
                "Status", "HIGH_CPU"
        );
    }

    @GetMapping("/logs")
    public List<String> getLogs() {
        return List.of(
                "INFO: Service started",
                "WARN: CPU usage high",
                "ERROR: Timeout on dependency"
        );
    }

    @PostMapping("/restart")
    public Map<String, String> restartService() {
        return Map.of("message", "Service restarted successfully");
    }

    @PostMapping("/scale")
    public Map<String, String> scaleService() {
        return Map.of("message", "Service scaled successfully");
    }

}
