package com.opsguardian.copilot_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class DevOpsService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String BASE_URL="http://localhost:8081/api";

    public Map getMetrics(){
        return restTemplate.getForObject(BASE_URL + "/metrics", Map.class);
    }

    public String scaleService() {
        Map response = restTemplate.postForObject(BASE_URL + "/scale", null, Map.class);
        return response.get("message").toString();
    }

    public String restartService() {
        Map response = restTemplate.postForObject(BASE_URL + "/restart", null, Map.class);
        return response.get("message").toString();
    }

}
