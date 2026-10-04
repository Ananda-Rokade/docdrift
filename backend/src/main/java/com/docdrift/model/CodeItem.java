package com.docdrift.model;

import java.util.ArrayList;
import java.util.List;

public class CodeItem {
    public String type = "ROUTE";
    public String name;
    public String method;
    public String path;
    public List<String> parameters = new ArrayList<>();
    public String file;
    public int line;
    public String key() { return method != null ? method.toUpperCase() + " " + normalize(path) : "FUNCTION " + name; }
    static String normalize(String value) { return value == null ? "" : value.replaceAll(":([A-Za-z_$][\\w$]*)", "{$1}").replaceAll("\\s+", "").trim(); }
}
