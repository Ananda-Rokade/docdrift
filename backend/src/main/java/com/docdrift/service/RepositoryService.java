package com.docdrift.service;

import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class RepositoryService {
    private static final Logger logger=LoggerFactory.getLogger(RepositoryService.class);
    private static final Pattern GITHUB=Pattern.compile("^https://github\\.com/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+(?:\\.git)?/?$");
    private final Path tempRoot=Path.of(System.getProperty("java.io.tmpdir"),"docdrift-repos");
    public Path cloneRepository(String url) throws Exception {
        if(url==null||!GITHUB.matcher(url.trim()).matches())throw new IllegalArgumentException("Enter a public GitHub repository URL, for example https://github.com/owner/repository");
        Files.createDirectories(tempRoot); Path destination=tempRoot.resolve(UUID.randomUUID().toString());
        try { Git.cloneRepository().setURI(url.trim()).setDirectory(destination.toFile()).setCloneAllBranches(false).call().close(); return destination; }
        catch(Exception e){delete(destination);logger.warn("JGit clone failed for {}: {}",url,e.toString());throw new IllegalStateException("Unable to clone repository. Check the URL and confirm the repository is public.",e);}
    }
    public void delete(Path folder){ if(folder==null||!folder.normalize().startsWith(tempRoot.normalize()))return; try(Stream<Path> paths=Files.walk(folder)){paths.sorted((a,b)->b.getNameCount()-a.getNameCount()).forEach(p->{try{Files.deleteIfExists(p);}catch(IOException ignored){}});}catch(IOException ignored){} }
}
