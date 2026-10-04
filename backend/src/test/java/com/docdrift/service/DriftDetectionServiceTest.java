package com.docdrift.service;

import com.docdrift.model.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class DriftDetectionServiceTest {
    private final DriftDetectionService service=new DriftDetectionService(new SeverityService());
    @Test void detectsUndocumentedRoute(){CodeItem c=code("POST","/payments",List.of("amount"));List<DriftIssueResponse> result=service.detect(List.of(c),List.of());assertEquals("UNDOCUMENTED",result.get(0).type);assertEquals("MEDIUM",result.get(0).severity);}
    @Test void detectsStaleRoute(){DocItem d=doc("DELETE","/users/{id}",List.of("id"));List<DriftIssueResponse> result=service.detect(List.of(),List.of(d));assertEquals("STALE_DOC",result.get(0).type);assertEquals("HIGH",result.get(0).severity);}
    @Test void detectsSignatureMismatch(){List<DriftIssueResponse> result=service.detect(List.of(code("PUT","/users/{id}",List.of("email","role"))),List.of(doc("PUT","/users/:id",List.of("username"))));assertEquals("SIGNATURE_MISMATCH",result.get(0).type);}
    @Test void reportsNoDriftForMatchingRoute(){assertTrue(service.detect(List.of(code("GET","/users",List.of("page"))),List.of(doc("GET","/users",List.of("page")))).isEmpty());}
    private CodeItem code(String method,String path,List<String> params){CodeItem i=new CodeItem();i.method=method;i.path=path;i.name="handler";i.parameters=params;return i;}
    private DocItem doc(String method,String path,List<String> params){DocItem i=new DocItem();i.method=method;i.path=path;i.name=path;i.parameters=params;return i;}
}
