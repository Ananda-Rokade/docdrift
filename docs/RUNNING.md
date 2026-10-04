# Run DocDrift

## IntelliJ IDEA demo

1. Open the repository root in IntelliJ IDEA.
2. Set Project SDK and the Maven runner to JDK 17 or newer.
3. Import `backend/pom.xml` as a Maven project and wait for dependency resolution.
4. Run **DocDrift Backend** from the shared `.run` configurations. It activates the `demo` profile, which uses in-memory H2.
5. From IntelliJ's terminal, run `npm install` in `frontend` once, then start **DocDrift Frontend**.
6. Browse to <http://localhost:5173>.

Run the backend before the frontend. The API is at <http://localhost:8080>.

## MySQL profile

1. Start MySQL and create the database:

   ```sql
   CREATE DATABASE docdrift;
   ```

2. In the **DocDrift Backend** run configuration, remove `--spring.profiles.active=demo` from program arguments.
3. Set `DB_USERNAME` and `DB_PASSWORD` in that configuration's environment variables. Hibernate creates/updates the `scans` and `drift_issues` tables.
4. Start the backend. It uses the default MySQL profile.

Do not commit passwords or API keys. Spring does not read `.env` files automatically; configure environment variables in IntelliJ or the shell.

## Gemini suggestions (optional)

Set `GEMINI_AUTH_KEY` (or `GEMINI_API_KEY`) in the backend run configuration's environment variables. Without a key, analysis still completes and AI suggestions are unavailable. Keep the key out of source control and screenshots.

## Run from PowerShell

Start the backend in one terminal (for an H2 demo):

```powershell
cd backend
mvn spring-boot:run "-Dspring-boot.run.profiles=demo"
```

Start the frontend in another terminal:

```powershell
cd frontend
npm install
npm run dev
```

## Local demonstration

Use the **With drift** and **No drift** buttons on the homepage. The fixture folders are local and do not require a GitHub connection. For a remote scan, enter a public GitHub URL such as `https://github.com/owner/repository`.
