package com.docdrift.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="scans")
public class Scan {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private String repositoryUrl;
    private Instant scanDate = Instant.now();
    private int totalCodeItems, totalDocItems, totalIssues;
    @OneToMany(mappedBy="scan", cascade=CascadeType.ALL, orphanRemoval=true) private List<DriftIssue> issues = new ArrayList<>();
    protected Scan() {}
    public Scan(String url, int codeCount, int docCount) { repositoryUrl=url; totalCodeItems=codeCount; totalDocItems=docCount; }
    public void addIssue(DriftIssue issue) { issues.add(issue); issue.setScan(this); totalIssues=issues.size(); }
    public Long getId(){return id;} public String getRepositoryUrl(){return repositoryUrl;} public Instant getScanDate(){return scanDate;}
    public int getTotalCodeItems(){return totalCodeItems;} public int getTotalDocItems(){return totalDocItems;} public int getTotalIssues(){return totalIssues;} public List<DriftIssue> getIssues(){return issues;}
}
