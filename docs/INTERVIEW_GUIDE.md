# DocDrift interview guide

## 30-second explanation

DocDrift is a React and Spring Boot application that checks whether a repository's API documentation matches its source code. It reads routes from supported Java or Express source files and Markdown, detects undocumented endpoints, stale entries, and parameter mismatches, assigns severity using fixed rules, and optionally asks Gemini to suggest a fix. Scan reports are stored with Spring Data JPA in H2 for the demo or MySQL for a persistent setup.

## One-minute request flow

The React dashboard posts a repository URL to a Spring REST controller. A service clones the public repository with JGit and walks its files. JavaParser reads Spring MVC route annotations; a small recognizer handles common Express route registrations. A Markdown extractor reads documented method/path entries and nearby parameters. The drift service matches normalized method/path pairs and compares parameters. Separate severity rules classify findings. Gemini only summarizes confirmed findings, so AI availability cannot change detection results. JPA stores the scan and findings, and the controller returns the report for the dashboard.

## Common questions

### Why Spring Boot?

It gives the project a straightforward Java REST API, dependency injection, configuration, and database integration without adding a complex architecture.

### Why MySQL and JPA?

A scan has multiple findings, which maps naturally to a one-to-many relationship. JPA maps the Java entities to relational tables, and Hibernate implements that mapping. H2 makes the local demo easy to start; MySQL provides persistent storage.

### Why JGit and JavaParser?

JGit clones repositories from Java without shelling out to Git. JavaParser builds a syntax tree so route annotations and method parameters can be inspected more reliably than with regular expressions.

### How are the three drift types detected?

- **Undocumented:** a supported source route has no documentation match.
- **Stale documentation:** a documented route has no source match.
- **Signature mismatch:** method and normalized path match, but parameter names differ.

### Why keep AI out of detection?

Detection and severity remain repeatable. Gemini receives only confirmed findings and writes optional explanatory/fix text. A missing key or failed Gemini request does not prevent the scan from being returned or saved.

### What are the main limitations?

The current source extractors support Spring MVC routes and common Express patterns, and the Markdown parser expects explicit method/path text. It is not a complete JavaScript/TypeScript parser, general Markdown AST, or OpenAPI implementation.
