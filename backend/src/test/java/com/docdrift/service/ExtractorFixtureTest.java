package com.docdrift.service;

import com.docdrift.model.CodeItem;
import com.docdrift.model.DocItem;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ExtractorFixtureTest {
    @Test void extractsJavaRoutesAndMarkdownContractsFromDriftFixture() throws Exception {
        Path root=Path.of("..","test-fixtures","drift").toAbsolutePath().normalize();
        List<CodeItem> code=new CodeExtractorService().extract(root);
        List<DocItem> docs=new DocumentationExtractorService().extract(root);
        assertTrue(code.stream().anyMatch(i->"POST".equals(i.method)&&"/api/payments".equals(i.path)&&i.parameters.containsAll(List.of("amount","userId"))));
        assertTrue(code.stream().anyMatch(i->"PUT".equals(i.method)&&"/api/users/{id}".equals(i.path)&&i.parameters.containsAll(List.of("id","email","role"))));
        assertTrue(docs.stream().anyMatch(i->"PUT".equals(i.method)&&"/api/users/:id".equals(i.path)&&i.parameters.contains("username")));
        assertTrue(docs.stream().anyMatch(i->"DELETE".equals(i.method)&&i.path.equals("/api/users/:id")));
    }

    @Test void ignoresHttpVerbsFollowedByOrdinaryWords(@org.junit.jupiter.api.io.TempDir Path temporaryFolder) throws Exception {
        Files.writeString(temporaryFolder.resolve("README.md"), "The client can GET React data or DELETE any cached entry.\n");
        assertTrue(new DocumentationExtractorService().extract(temporaryFolder).isEmpty());
    }
}
