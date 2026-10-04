package com.docdrift.service;

import com.docdrift.model.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class DriftDetectionService {
    private final SeverityService severity;
    public DriftDetectionService(SeverityService severity){this.severity=severity;}
    public List<DriftIssueResponse> detect(List<CodeItem> code,List<DocItem> docs){
        List<DriftIssueResponse> issues=new ArrayList<>(); Map<String,DocItem> docByKey=new LinkedHashMap<>();Map<String,CodeItem> codeByKey=new LinkedHashMap<>();
        for(DocItem d:docs)docByKey.putIfAbsent(d.key(),d); for(CodeItem c:code)codeByKey.putIfAbsent(c.key(),c);
        for(CodeItem c:code){DocItem d=docByKey.get(c.key());if(d==null)issues.add(fromCode("UNDOCUMENTED",c,null,"Code item is not described in the Markdown documentation.")); else if(!same(c.parameters,d.parameters))issues.add(fromCode("SIGNATURE_MISMATCH",c,d,"Documented parameters differ from the parameters extracted from source code."));}
        for(DocItem d:docs)if(!codeByKey.containsKey(d.key()))issues.add(fromDoc(d,"Documentation describes an endpoint that was not found in source code."));
        return issues;
    }
    private boolean same(List<String> a,List<String> b){return new HashSet<>(a).equals(new HashSet<>(b));}
    private DriftIssueResponse fromCode(String type,CodeItem c,DocItem d,String description){DriftIssueResponse i=new DriftIssueResponse();i.type=type;i.severity=severity.assign(type,c.method!=null);i.itemName=c.name;i.routeMethod=c.method;i.routePath=c.path;i.file=c.file;i.line=c.line;i.parameters=c.parameters;i.description=description;if(d!=null){i.docFile=d.file;i.docLine=d.line;i.documentedParameters=d.parameters;}return i;}
    private DriftIssueResponse fromDoc(DocItem d,String description){DriftIssueResponse i=new DriftIssueResponse();i.type="STALE_DOC";i.severity=severity.assign(i.type,d.method!=null);i.itemName=d.name;i.routeMethod=d.method;i.routePath=d.path;i.docFile=d.file;i.docLine=d.line;i.documentedParameters=d.parameters;i.description=description;return i;}
}
