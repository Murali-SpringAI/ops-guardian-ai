package com.opsguardian.copilot_backend.service;

import org.springframework.stereotype.Service;

@Service
public class AuditService {

    public void log(String userId, String action, String target) {
        System.out.println(
                "AUDIT → user=" + userId +
                        " action=" + action +
                        " target=" + target +
                        " time=" + System.currentTimeMillis()
        );
    }
}
