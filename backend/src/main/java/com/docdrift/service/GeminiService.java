package com.docdrift.service;

import com.docdrift.model.DriftIssueResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.*;

@Service
public class GeminiService {
    private static final Logger logger=LoggerFactory.getLogger(GeminiService.class);
    @Value("${app.gemini.api-key:}") private String apiKey;
    @Value("${app.gemini.model:gemini-3.6-flash}") private String model;
    private final RestClient http=RestClient.builder().requestFactory(requestFactory()).build();
    private JdkClientHttpRequestFactory requestFactory(){JdkClientHttpRequestFactory factory=new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());factory.setReadTimeout(Duration.ofSeconds(20));return factory;}
    public List<String> summarizeTopIssues(List<DriftIssueResponse> issues){
        List<String> results=new ArrayList<>();
        if(issues.isEmpty())return results;
        String unavailable="**Gemini summary unavailable:** add `GEMINI_AUTH_KEY` to the DocDrift Backend IntelliJ run configuration, then restart the backend.";
        if(apiKey==null||apiKey.isBlank()){for(int i=0;i<issues.size();i++)results.add(unavailable);return results;}
        StringBuilder prompt=new StringBuilder("Summarize each confirmed documentation drift below for a developer. Use only supplied facts. For each, include Summary, Impact, and Suggested fix. Return only JSON in this shape: {\"summaries\":[{\"index\":1,\"summary\":\"Markdown text\"}]}. Include one entry per finding and preserve its index.\n");
        for(int i=0;i<issues.size();i++){DriftIssueResponse issue=issues.get(i);prompt.append("\nFinding ").append(i+1).append(": type=").append(issue.type).append("; route=").append(issue.routeMethod).append(" ").append(issue.routePath).append("; actual parameters=").append(issue.parameters).append("; documented parameters=").append(issue.documentedParameters).append("; code location=").append(issue.file).append(":").append(issue.line).append("; documentation location=").append(issue.docFile).append(":").append(issue.docLine).append("; details=").append(issue.description).append(".\n");}
        try { Map<String,Object> body=Map.of("contents",List.of(Map.of("parts",List.of(Map.of("text",prompt.toString())))),"generationConfig",Map.of("responseMimeType","application/json"));
            Map<?,?> response=http.post().uri("https://generativelanguage.googleapis.com/v1beta/models/"+model+":generateContent").header("x-goog-api-key",apiKey).body(body).retrieve().body(Map.class);
            String text=responseText(response);Map<?,?> json=new ObjectMapper().readValue(text,Map.class);Object values=json.get("summaries");
            if(values instanceof List<?> list){for(int i=0;i<issues.size();i++)results.add(null);for(Object value:list)if(value instanceof Map<?,?> item&&item.get("index") instanceof Number index&&item.get("summary")!=null){int position=index.intValue()-1;if(position>=0&&position<results.size())results.set(position,item.get("summary").toString());}
                if(results.stream().allMatch(Objects::nonNull))return results;
            }
        } catch(Exception error) { logger.warn("Gemini summary request failed: {}",error.toString()); /* AI is optional: a provider failure never cancels the deterministic scan. */ }
        for(int i=0;i<issues.size();i++)if(i>=results.size()||results.get(i)==null) {if(i>=results.size())results.add("**Gemini summary unavailable:** check the API key, model setting, or network. The deterministic finding is still available.");else results.set(i,"**Gemini summary unavailable:** check the API key, model setting, or network. The deterministic finding is still available.");}
        return results;
    }
    private String responseText(Map<?,?> response){Object candidates=response==null?null:response.get("candidates");if(candidates instanceof List<?> list&&!list.isEmpty()&&list.get(0) instanceof Map<?,?> candidate){Object content=candidate.get("content");if(content instanceof Map<?,?> c&&c.get("parts") instanceof List<?> parts&&!parts.isEmpty()&&parts.get(0) instanceof Map<?,?> p&&p.get("text")!=null)return p.get("text").toString();}throw new IllegalStateException("Gemini returned no summary text.");}
}
