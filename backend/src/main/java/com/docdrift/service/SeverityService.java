package com.docdrift.service;

import org.springframework.stereotype.Service;

@Service
public class SeverityService {
    public String assign(String type, boolean route) {
        if("STALE_DOC".equals(type)||"SIGNATURE_MISMATCH".equals(type)) return "HIGH";
        return route ? "MEDIUM" : "LOW";
    }
}
