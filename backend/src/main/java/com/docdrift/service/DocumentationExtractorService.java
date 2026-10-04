package com.docdrift.service;

import com.docdrift.model.DocItem;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Stream;

@Service
public class DocumentationExtractorService {
    private static final Pattern ROUTE=Pattern.compile("\\b(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS)\\s+(/[/\\w:{}.$-]*)",Pattern.CASE_INSENSITIVE);
    private static final Pattern PARAMETER=Pattern.compile("(?:^|[,`*\\-\\s])([A-Za-z_$][\\w$]*)(?=[`*,\\s:]|$)");

    public List<DocItem> extract(Path root) throws IOException {
        List<DocItem> found=new ArrayList<>();
        try(Stream<Path> paths=Files.walk(root)) {
            Iterator<Path> files=paths.filter(Files::isRegularFile)
                    .filter(p->p.getFileName().toString().toLowerCase().endsWith(".md"))
                    .filter(p->!p.toString().replace('\\','/').contains("/.git/")).limit(1000).iterator();
            while(files.hasNext()) {
                Path file=files.next(); List<String> lines=Files.readAllLines(file);
                for(int line=0;line<lines.size();line++) {
                    Matcher route=ROUTE.matcher(lines.get(line));
                    if(route.find()) found.add(readRoute(root,file,lines,line,route));
                    else addDocumentedFunction(root,file,lines.get(line),line,found);
                }
            }
        }
        return found;
    }

    private DocItem readRoute(Path root,Path file,List<String> lines,int line,Matcher route) {
        DocItem item=new DocItem(); item.method=route.group(1).toUpperCase(); item.path=route.group(2); item.name=item.path;
        item.file=root.relativize(file).toString().replace('\\','/'); item.line=line+1;
        for(int next=line+1;next<Math.min(lines.size(),line+14);next++) {
            String text=lines.get(next); if(text.matches("^#{1,6}\\s+.*")) break;
            if(!(text.toLowerCase().contains("param")||text.stripLeading().startsWith("-")||text.stripLeading().startsWith("*")||text.contains("`")))continue;
            Matcher params=PARAMETER.matcher(text);
            while(params.find()) { String name=params.group(1); if(!Set.of("Parameters","Parameter","Body","Query","Path","Request","required","optional").contains(name)&&!item.parameters.contains(name))item.parameters.add(name); }
        }
        return item;
    }

    private void addDocumentedFunction(Path root,Path file,String text,int line,List<DocItem> found) {
        Matcher function=Pattern.compile("`([A-Za-z_$][\\w$]*)`\\s*\\(").matcher(text);
        if(function.find()) { DocItem item=new DocItem();item.type="FUNCTION";item.name=function.group(1);item.file=root.relativize(file).toString().replace('\\','/');item.line=line+1;found.add(item); }
    }
}
