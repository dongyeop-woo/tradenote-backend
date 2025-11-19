package com.share.share.web;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
            "message", "Share API Server",
            "version", "0.0.1",
            "endpoints", Map.of(
                "auth", "/api/auth",
                "community", "/api/community",
                "trades", "/api/trades"
            )
        );
    }
}

