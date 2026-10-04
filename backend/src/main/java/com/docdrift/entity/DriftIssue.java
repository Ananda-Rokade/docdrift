package com.docdrift.entity;

import jakarta.persistence.*;

@Entity
@Table(name="drift_issues")
public class DriftIssue {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="scan_id") private Scan scan;
    private String issueType, severity, itemName, routeMethod, routePath, filePath, docFile, description;
    @Column(columnDefinition="TEXT") private String aiSuggestion;
    private Integer lineNumber, docLine;
    @Column(length=2000) private String parameters, documentedParameters;
    protected DriftIssue() {}
    public DriftIssue(String type,String severity,String name,String method,String path,String file,Integer line,String docFile,Integer docLine,String description,String ai,String params,String docParams){
        this.issueType=type;this.severity=severity;this.itemName=name;this.routeMethod=method;this.routePath=path;this.filePath=file;this.lineNumber=line;this.docFile=docFile;this.docLine=docLine;this.description=description;this.aiSuggestion=ai;this.parameters=params;this.documentedParameters=docParams;
    }
    void setScan(Scan scan){this.scan=scan;}
}
