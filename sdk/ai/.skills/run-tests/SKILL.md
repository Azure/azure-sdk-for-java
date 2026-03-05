---
name: run-tests
description: Run project tests using Maven (mvn). Use when the user asks to run tests.
---

# Run Tests (Maven)

Use Maven (`mvn`) to run tests. Confirm you are in a directory with a `pom.xml` (project root or module root).

## Common commands

### All tests (default)
```bash
mvn test
```

### Specific module (multi-module)
```bash
mvn -pl <module> -am test
```

### Specific test class or method (Surefire)
```bash
mvn -Dtest=MyTest test
mvn -Dtest=MyTest#myMethod test
```

## Test modes (AZURE_TEST_MODE)
If the user asks for live/record/playback, set the env var for the command:
```powershell
$env:AZURE_TEST_MODE = "LIVE"
mvn test
```

## Running tests with secrets

Tests often require environment variables with connection strings, keys, or other secrets.

- If `wr-load` is available, use it to load env vars from KeyVault before running tests:
  ```bash
  wr-load -Resource <resource>; AZURE_TEST_MODE=LIVE mvn "-Dtest=<TestClass>" test
  ```
  **Minimize `wr-load` calls** — it fetches secrets over the network. Run it once, capture the values, and reuse them.

- If `wr-load` is **not** available, ask the user to set the required environment variables manually before running tests. Suggest they check the project's `test-resources.json` or README for the expected variable names.

## Steps
1. Ensure you're in the correct Maven project directory (contains `pom.xml`). If not, ask for the correct path.
2. Start simple: `mvn "-Dtest=<TestClass>" test`. Only add flags if something fails.
3. If the user provides a test name, use `-Dtest=<pattern>`.
4. If the user specifies a module, use `-pl <module> -am test`.
5. If the user specifies a test mode (LIVE/RECORD/PLAYBACK), set the env var.
6. If tests require secrets, use `wr-load` if available; otherwise ask the user to set the required environment variables manually.
7. If the command fails, report the error output and ask how they want to proceed.

## Troubleshooting (only add flags when needed)

Apply these **only** if the simple `mvn test` command fails with the specific error described:

- **Samples fail to compile on Java 8 base-testCompile**: add `-Dbuildhelper.addtestsource.skip=true -Dbuildhelper.addtestresource.skip=true`
- **JPMS/module-path errors** (e.g., `okio` module issues): add `-Dsurefire.useModulePath=false`
- **Build plugins block the run**: add skip flags as needed, e.g. `-Denforcer.skip=true -Dcodesnippet.skip=true -Dcheckstyle.skip=true`
- **Reactor blocking errors in async tests** (Netty thread): add `$env:AZURE_TEST_HTTP_CLIENTS = "okhttp"`
- **SSL handshake / PKIX path building failed** (`SSLHandshakeException`, `unable to find valid certification path to requested target`): the JVM's trust store is missing the corporate root CA. Add `-Djavax.net.ssl.trustStoreType=WINDOWS-ROOT` to use the Windows certificate store instead. This is common on corporate networks with proxy/firewall TLS interception.

Do NOT preemptively add all these flags. Start simple and escalate only on failure.
