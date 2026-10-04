package com.docdrift.service;

import com.docdrift.model.CodeItem;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Stream;

@Service
public class CodeExtractorService {
    private static final Set<String> HTTP = Set.of("GET","POST","PUT","DELETE","PATCH","HEAD","OPTIONS");
    public List<CodeItem> extract(Path root) throws IOException {
        List<CodeItem> found = new ArrayList<>();
        try (Stream<Path> paths=Files.walk(root)) {
            Iterator<Path> it=paths.filter(Files::isRegularFile).filter(this::supported).limit(5000).iterator();
            while(it.hasNext()) { Path file=it.next(); String text=Files.readString(file); String relative=root.relativize(file).toString().replace('\\','/');
                if(file.toString().endsWith(".java")) extractJava(text,relative,found); else extractJavaScript(text,relative,found);
            }
        }
        return found;
    }
    private boolean supported(Path p) {
        String n=p.getFileName().toString(), path=p.toString().replace('\\','/');
        return (n.endsWith(".java")||n.endsWith(".js")||n.endsWith(".jsx")||n.endsWith(".ts")||n.endsWith(".tsx")) && !path.contains("/node_modules/") && !path.contains("/.git/") && !path.contains("/build/") && !path.contains("/target/");
    }
    private void extractJava(String text,String file,List<CodeItem> out) {
        try {
            StaticJavaParser.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
            CompilationUnit unit=StaticJavaParser.parse(text); Map<String,String> classBases=new HashMap<>();
            unit.findAll(com.github.javaparser.ast.body.ClassOrInterfaceDeclaration.class).forEach(c->{for(AnnotationExpr a:c.getAnnotations())if(a.getNameAsString().equals("RequestMapping"))classBases.put(c.getNameAsString(),annotationValue(a));});
            Map<String,List<String>> bodyFields=new HashMap<>();
            unit.findAll(com.github.javaparser.ast.body.RecordDeclaration.class).forEach(r->{List<String> names=new ArrayList<>();r.getParameters().forEach(p->names.add(p.getNameAsString()));bodyFields.put(r.getNameAsString(),names);});
            unit.findAll(com.github.javaparser.ast.body.ClassOrInterfaceDeclaration.class).forEach(c->{List<String> names=new ArrayList<>();c.getFields().forEach(f->f.getVariables().forEach(v->names.add(v.getNameAsString())));if(!names.isEmpty())bodyFields.put(c.getNameAsString(),names);});
            for(MethodDeclaration m:unit.findAll(MethodDeclaration.class)) {
                String method=null,path=null;
                for(AnnotationExpr a:m.getAnnotations()) {
                    String ann=a.getNameAsString();
                    if(ann.equals("RequestMapping")) { method=requestMethod(a); path=annotationValue(a); }
                    else if(ann.matches("(Get|Post|Put|Delete|Patch)Mapping")) { method=ann.replace("Mapping","").toUpperCase(); path=annotationValue(a); }
                }
                String base=m.findAncestor(com.github.javaparser.ast.body.ClassOrInterfaceDeclaration.class).map(c->classBases.getOrDefault(c.getNameAsString(),"")).orElse("");
                if(method!=null) { CodeItem i=new CodeItem(); i.name=m.getNameAsString(); i.method=method; i.path=join(base,path); i.file=file; i.line=m.getBegin().map(p->p.line).orElse(1);
                    for(com.github.javaparser.ast.body.Parameter p:m.getParameters()) for(AnnotationExpr annotation:p.getAnnotations()) if(Set.of("PathVariable","RequestParam","RequestBody","RequestHeader").contains(annotation.getNameAsString())) {
                        if(annotation.getNameAsString().equals("RequestBody")&&bodyFields.containsKey(p.getType().asString())) i.parameters.addAll(bodyFields.get(p.getType().asString())); else i.parameters.add(parameterName(p));
                    }
                    out.add(i);
                } else {
                    CodeItem i=new CodeItem();i.type="FUNCTION";i.name=m.getNameAsString();i.file=file;i.line=m.getBegin().map(p->p.line).orElse(1);
                    m.getParameters().forEach(p->i.parameters.add(p.getNameAsString()));out.add(i);
                }
            }
        } catch(Exception ignored) { /* Skip unsupported source syntax without aborting the repository scan. */ }
    }
    private String parameterName(com.github.javaparser.ast.body.Parameter p) { for(AnnotationExpr a:p.getAnnotations()) if(a instanceof SingleMemberAnnotationExpr s && s.getMemberValue() instanceof StringLiteralExpr v) return v.asString(); return p.getNameAsString(); }
    private String requestMethod(AnnotationExpr a) { if(a instanceof NormalAnnotationExpr n) for(MemberValuePair pair:n.getPairs()) if(pair.getNameAsString().equals("method")) { Matcher m=Pattern.compile("RequestMethod\\.(\\w+)").matcher(pair.getValue().toString()); if(m.find()) return m.group(1); } return "GET"; }
    private String annotationValue(AnnotationExpr a) { if(a instanceof SingleMemberAnnotationExpr s) return stringValue(s.getMemberValue()); if(a instanceof NormalAnnotationExpr n) for(MemberValuePair p:n.getPairs()) if(p.getNameAsString().equals("value")||p.getNameAsString().equals("path")) return stringValue(p.getValue()); return ""; }
    private String stringValue(Expression e) { if(e instanceof StringLiteralExpr s) return s.asString(); if(e instanceof ArrayInitializerExpr a && !a.getValues().isEmpty()) return stringValue(a.getValues().get(0)); return ""; }
    private String join(String a,String b) { return ((a==null?"":a)+"/"+(b==null?"":b)).replaceAll("/+/","/"); }
    private void extractJavaScript(String text,String file,List<CodeItem> out) {
        Pattern route=Pattern.compile("(?:app|router|server)\\.(get|post|put|delete|patch|head|options)\\s*\\(\\s*['\"]([^'\"]+)['\"]\\s*,([^;]*?)\\)",Pattern.CASE_INSENSITIVE|Pattern.DOTALL); Matcher m=route.matcher(text);
        while(m.find()) { CodeItem i=new CodeItem(); i.method=m.group(1).toUpperCase(); i.path=m.group(2); i.name="routeHandler"; i.file=file; i.line=lineAt(text,m.start()); String handler=m.group(3); Matcher param=Pattern.compile("(?:req\\.(?:body|query|params)|request\\.(?:body|query|params))\\.([A-Za-z_$][\\w$]*)").matcher(handler); while(param.find()) if(!i.parameters.contains(param.group(1))) i.parameters.add(param.group(1)); out.add(i); }
        Pattern function=Pattern.compile("(?:function\\s+([A-Za-z_$][\\w$]*)\\s*\\(([^)]*)\\)|(?:const|let|var)\\s+([A-Za-z_$][\\w$]*)\\s*=\\s*(?:async\\s*)?\\(([^)]*)\\)\\s*=>)");Matcher fm=function.matcher(text);
        while(fm.find()){String name=fm.group(1)!=null?fm.group(1):fm.group(3);if(out.stream().anyMatch(i->i.file.equals(file)&&"FUNCTION".equals(i.type)&&name.equals(i.name)))continue;CodeItem i=new CodeItem();i.type="FUNCTION";i.name=name;i.file=file;i.line=lineAt(text,fm.start());String args=fm.group(2)!=null?fm.group(2):fm.group(4);for(String arg:args.split(",")){String clean=arg.trim().replaceAll("=.*$","").trim();if(clean.matches("[A-Za-z_$][\\w$]*"))i.parameters.add(clean);}out.add(i);}
    }
    private int lineAt(String text,int index) { int line=1; for(int i=0;i<index;i++) if(text.charAt(i)=='\n') line++; return line; }
}
