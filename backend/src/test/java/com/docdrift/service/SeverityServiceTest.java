package com.docdrift.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SeverityServiceTest {
    private final SeverityService service=new SeverityService();
    @Test void severityRulesAreDeterministic(){assertEquals("HIGH",service.assign("STALE_DOC",true));assertEquals("HIGH",service.assign("SIGNATURE_MISMATCH",true));assertEquals("MEDIUM",service.assign("UNDOCUMENTED",true));assertEquals("LOW",service.assign("UNDOCUMENTED",false));}
}
