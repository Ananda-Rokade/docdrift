# DocDrift

DocDrift checks whether a GitHub repository's API documentation matches its source code. It reports undocumented endpoints, stale documentation, and signature mismatches, then assigns deterministic severity and can ask Gemini for a suggested fix.

## Features

- Clone and scan a public GitHub repository, or use the included local demo fixtures.
- Extract Spring MVC and common Express routes from source code.
- Compare routes and parameters with Markdown documentation.
- Detect `UNDOCUMENTED`, `STALE_DOC`, and `SIGNATURE_MISMATCH` findings.
- Optionally generate AI summaries for the two highest-severity findings with Gemini.
- Save scan reports and findings with Spring Data JPA. The IntelliJ demo profile uses H2; the default profile uses MySQL.

## Architecture

```text
React + Axios → Spring Boot REST API → JGit / source & Markdown extractors
              → drift and severity services → Gemini (optional)
              → Spring Data JPA → H2 demo or MySQL
```

The backend follows a simple Controller → Service → Repository → Entity structure. See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the processing flow and [docs/INTERVIEW_GUIDE.md](docs/INTERVIEW_GUIDE.md) for an interview explanation.

## Run locally

### Requirements

- JDK 17+
- IntelliJ IDEA (or Maven 3.8+)
- Node.js 20+ and npm
- MySQL 8+ only when using the MySQL profile

### IntelliJ IDEA (quick demo)

1. Open this project folder in IntelliJ IDEA and set the Project SDK and Maven runner to JDK 17+.
2. Import `backend/pom.xml` as a Maven project and allow dependencies to load.
3. Run the shared **DocDrift Backend** configuration. It uses the `demo` profile and in-memory H2, so no MySQL setup is needed.
4. Run **DocDrift Frontend**. If dependencies are missing, run `npm install` in `frontend` first.
5. Open <http://localhost:5173> and choose **With drift** or **No drift**, or submit a public GitHub repository URL.

See [docs/RUNNING.md](docs/RUNNING.md) for MySQL, Gemini, and command-line setup.

## Project layout

```text
backend/       Spring Boot REST API, analysis services, JPA entities
frontend/      React dashboard (Vite)
test-fixtures/ Local repositories for drift and clean demonstrations
docs/          Architecture, setup, and interview notes
```

## API

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/repo/analyze` | Clone and analyze a public GitHub repository |
| `POST` | `/api/repo/analyze-fixture/drift` | Analyze the drift demo fixture |
| `POST` | `/api/repo/analyze-fixture/clean` | Analyze the clean demo fixture |
| `POST` | `/api/repo/scan` | Extract source items for troubleshooting |
| `POST` | `/api/repo/connect` | Check that a public repository can be cloned |

Analyze request body:

```json
{"repositoryUrl":"https://github.com/owner/repository"}
```

## Detection rules

- Source route with no matching documentation: `UNDOCUMENTED`.
- Documented route missing from source: `STALE_DOC`.
- Matching method and normalized route with different parameters: `SIGNATURE_MISMATCH`.
- Stale documentation and signature mismatches are `HIGH`; undocumented routes are `MEDIUM`.
- Gemini only suggests wording for already-confirmed findings; it does not detect drift or assign severity.

## Checks

```powershell
cd backend
mvn test

cd ..\frontend
npm run build
```

## Limitations

DocDrift currently supports public GitHub HTTPS repositories, Spring MVC route annotations, common Express route registrations, and Markdown entries with explicit HTTP method/path text. It is a focused demo, not a full language server or general Markdown/OpenAPI parser.
