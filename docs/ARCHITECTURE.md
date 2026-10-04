# Architecture and analysis flow

## Request flow

1. React sends a repository URL to `POST /api/repo/analyze` (or requests one of the local fixtures).
2. `RepoController` validates the request and delegates to the service layer.
3. `RepositoryService` clones public GitHub repositories with JGit and removes temporary clone files after analysis.
4. Extractor services read supported source routes and Markdown documentation entries.
5. `DriftDetectionService` compares HTTP method, normalized route, and parameter names. `SeverityService` assigns fixed severity rules.
6. `GeminiService` can request a summary/suggested fix for up to two highest-severity findings. AI output never changes issue type or severity.
7. Spring Data repositories persist the scan and its findings; the API returns the report to the React dashboard.

## Main backend layers

```text
Controller → Service → Repository → Entity
                 ├── source/documentation extraction
                 ├── drift comparison and severity
                 └── optional Gemini request
```

- **Controller:** HTTP endpoints and request/response handling.
- **Service:** repository processing and business rules.
- **Repository:** Spring Data JPA persistence.
- **Entity:** `Scan` and its related `DriftIssue` records.

## Database

`Scan` stores the repository URL, scan time, and summary counts. A scan has many `DriftIssue` records. JPA cascade persistence saves the report and findings together. The `demo` Spring profile uses in-memory H2; the default profile connects to MySQL.

## Boundaries and limitations

Analysis is deterministic and currently focuses on Spring MVC annotations, common Express route registrations, and Markdown text containing explicit method/path entries. It does not fully parse every JavaScript/TypeScript construct or arbitrary Markdown/API specifications. Public repositories only are supported in the current demo.
