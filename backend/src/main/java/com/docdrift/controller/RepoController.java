package com.docdrift.controller;

import com.docdrift.entity.*;
import com.docdrift.model.*;
import com.docdrift.repository.ScanRepository;
import com.docdrift.service.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.nio.file.Path;
import java.util.*;

@RestController
@RequestMapping("/api/repo")
public class RepoController {
    private final RepositoryService repositories; private final CodeExtractorService codeExtractor; private final DocumentationExtractorService docsExtractor; private final DriftDetectionService detector; private final GeminiService gemini; private final ScanRepository scans;
    public RepoController(RepositoryService r,CodeExtractorService c,DocumentationExtractorService d,DriftDetectionService detector,GeminiService g,ScanRepository scans){this.repositories=r;this.codeExtractor=c;this.docsExtractor=d;this.detector=detector;this.gemini=g;this.scans=scans;}
    @PostMapping("/connect") public Map<String,Object> connect(@RequestBody AnalyzeRequest request)throws Exception{Path root=repositories.cloneRepository(request.repositoryUrl);repositories.delete(root);return Map.of("connected",true,"message","Repository cloned successfully.");}
    @PostMapping("/scan") public Map<String,Object> scan(@RequestBody AnalyzeRequest request)throws Exception{Path root=repositories.cloneRepository(request.repositoryUrl);try{return Map.of("codeItems",codeExtractor.extract(root));}finally{repositories.delete(root);}}
    @PostMapping("/analyze") public Map<String,Object> analyze(@RequestBody AnalyzeRequest request)throws Exception{
        Path root=repositories.cloneRepository(request.repositoryUrl);
        try { return createReport(request.repositoryUrl,root); } finally {repositories.delete(root);}
    }
    @PostMapping("/analyze-fixture/{name}") public Map<String,Object> analyzeFixture(@PathVariable String name)throws Exception{
        if(!name.equals("drift")&&!name.equals("clean"))throw new IllegalArgumentException("Fixture must be drift or clean.");
        Path root=Path.of("..","test-fixtures",name).toAbsolutePath().normalize();
        if(!java.nio.file.Files.isDirectory(root))throw new IllegalStateException("Fixture folder was not found. Start Spring Boot from the backend folder.");
        return createReport("Local fixture: "+name,root);
    }
    private Map<String,Object> createReport(String url,Path root)throws Exception{
        List<CodeItem> code=codeExtractor.extract(root);List<DocItem> docs=docsExtractor.extract(root);List<DriftIssueResponse> issues=detector.detect(code,docs);
        issues.sort(Comparator.comparingInt(issue->severityOrder(issue.severity)));
        List<DriftIssueResponse> highPriorityIssues=issues.stream().filter(issue->"HIGH".equals(issue.severity)).limit(2).toList();
        List<String> summaries=gemini.summarizeTopIssues(highPriorityIssues);
        for(int i=0;i<highPriorityIssues.size();i++)highPriorityIssues.get(i).aiSuggestion=summaries.get(i);
        Scan scan=new Scan(url,code.size(),docs.size());for(DriftIssueResponse issue:issues)scan.addIssue(toEntity(issue));scans.save(scan);
        Map<String,Long> severity=new LinkedHashMap<>();for(String level:List.of("HIGH","MEDIUM","LOW"))severity.put(level,issues.stream().filter(i->level.equals(i.severity)).count());
        Map<String,Object> result=new LinkedHashMap<>();result.put("scanId",scan.getId());result.put("repositoryUrl",url);result.put("scanDate",scan.getScanDate());result.put("codeItemsScanned",code.size());result.put("docItemsScanned",docs.size());result.put("totalIssues",issues.size());result.put("severityBreakdown",severity);result.put("issues",issues);return result;
    }
    private int severityOrder(String severity){return switch(severity){case "HIGH"->0;case "MEDIUM"->1;default->2;};}
    private DriftIssue toEntity(DriftIssueResponse i){return new DriftIssue(i.type,i.severity,i.itemName,i.routeMethod,i.routePath,i.file,i.line,i.docFile,i.docLine,i.description,i.aiSuggestion,String.join(", ",i.parameters),String.join(", ",i.documentedParameters));}
}
