package com.docdrift.model;

import java.util.ArrayList;
import java.util.List;

public class DriftIssueResponse {
    public String type, severity, itemName, routeMethod, routePath, file, docFile, description, aiSuggestion;
    public Integer line, docLine;
    public List<String> parameters = new ArrayList<>(), documentedParameters = new ArrayList<>();
}
