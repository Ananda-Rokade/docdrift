package com.docdrift.model;

import java.util.ArrayList;
import java.util.List;

public class DocItem {
    public String type = "ROUTE";
    public String name;
    public String method;
    public String path;
    public List<String> parameters = new ArrayList<>();
    public String file;
    public int line;
    public String key() { return method != null ? method.toUpperCase() + " " + CodeItem.normalize(path) : "FUNCTION " + name; }
}
