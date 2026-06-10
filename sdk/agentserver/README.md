# Java Agent Server Adapter

A Java adapter for building agent servers that implement the OpenAI Responses API. The project provides a framework
for creating agents that can be deployed as containers and interact with Azure AI Foundry.

## Project Structure

| Module                                 | Description                                                                                                                     |
|----------------------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| `java-agent-server-api`             | Core API — defines `ResponseHandler`, `ResponseContext`, streaming primitives, and the OpenAI Responses model types.            |
| `java-agent-server-jersey`          | Jersey/Grizzly HTTP server adapter that exposes a `AgentServerResponsesApi` as a REST endpoint on port **8088**.             |
| `java-agent-server-langchain4j`     | Integration layer for [LangChain4j](https://docs.langchain4j.dev/) agents, mapping them into the Agent Server Responses API. |
| `java-agent-server-langchain4j-bom` | Bill-of-Materials (BOM) for the LangChain4j integration.                                                                        |
| `agent-servers-samples/`            | Sample agents (see [Samples](#samples) below).                                                                                  |

## Prerequisites

* **Java 21+** (the project targets Java 21)
* **Docker** & **Docker Compose** (for containerized runs)
* No local Maven installation is needed — the included **Maven Wrapper** (`./mvnw`) downloads the correct version
  automatically.

## Building

From the `java_adapter/` directory, build all modules:

```bash
./mvnw clean package -DskipTests
```

To run the unit tests:

```bash
./mvnw clean verify
```

## Samples

### Echo Sample

A minimal agent that echoes the user's input back. It does not call any LLM and requires no API keys — useful for
verifying the adapter wiring and HTTP/SSE lifecycle.

**Run with the provided script:**

```bash
cd agent-servers-samples/echo-sample
chmod +x run-sample.sh
./run-sample.sh
```

The script will:

1. Build the entire project with `../../mvnw clean package -DskipTests`.
2. Build and start the Docker container (port **8088**).
3. Send a test request and print the JSON response.
4. Tear down the container.

**Or run each step manually:**

```bash
# 1. Build
../../mvnw clean package -DskipTests

# 2. Start the container
docker-compose up -d --build

# 3. Test — synchronous JSON response
curl -X POST http://localhost:8088/responses \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -d '{
    "input": "Echo this back to me.",
    "model": "gpt-4o"
  }' | json_pp

# 4. Test — streaming SSE response
curl -X POST http://localhost:8088/responses \
  -H "Accept: text/event-stream" \
  -H "Content-Type: application/json" \
  -d '{
    "input": "Echo this back to me.",
    "model": "gpt-4o"
  }' --no-buffer

# 5. Stop
docker-compose down
```

---

### LangChain4j Sample (`agent-servers-langchain4j-sample`)

An agent powered by LangChain4j and Azure OpenAI that solves simple maths queries using tool-calling (add, subtract,
multiply, hash).

#### Configure environment variables

Create or edit the `.env` file at `src/adapter/java/.env` (three levels above this directory):

```dotenv
USE_AZURE_CLIENT=true

AZURE_DEPLOYMENT_NAME=gpt-4o
AZURE_ENDPOINT=https://<your-resource>.openai.azure.com/
AZURE_API_KEY=<your-api-key>
```

#### Run with the provided script

```bash
cd agent-servers-samples/agent-servers-langchain4j-sample
chmod +x run-sample.sh
./run-sample.sh
```

The script will:

1. Build the entire project with `../../mvnw package -DskipTests`.
2. Load environment variables from `../../../.env`.
3. Build and start the Docker container (port **8088**), injecting the Azure OpenAI configuration.
4. Send a test request (`"What is 12 + 13"`) and print the JSON response.
5. Tear down the container.

**Or run each step manually:**

```bash
# 1. Build
../../mvnw package -DskipTests

# 2. Export env vars
set -a && source ../../../.env && set +a

# 3. Start the container
docker-compose up -d --build

# 4. Wait for startup
sleep 10

# 5. Test
curl -X POST http://localhost:8088/responses \
  -H "Accept: application/json" \
  -H "Content-Type: application/json" \
  -d '{
    "input": "What is 12 + 13",
    "model": "gpt-4o"
  }' | json_pp

# 6. Stop
docker-compose down
```

## Running without Docker

You can also run the fat-JAR directly after building:

```bash
# Echo sample
java -jar agent-servers-samples/echo-sample/target/echo-sample-1.0.0-SNAPSHOT-jar-with-dependencies.jar

# LangChain4j sample (requires env vars to be set)
export AZURE_DEPLOYMENT_NAME=gpt-4o
export AZURE_API_KEY=<your-api-key>
export AZURE_ENDPOINT=https://<your-resource>.openai.azure.com/
java -jar agent-servers-samples/agent-servers-langchain4j-sample/target/agent-servers-langchain4j-sample-1.0.0-SNAPSHOT-jar-with-dependencies.jar
```

The server starts on `http://localhost:8088`.

## Deploying to Azure AI Foundry

Refer to the `deploy.sh` script in the echo-sample for an example of registering a agent server with Azure AI Foundry
using `az rest`:

```bash
cd agent-servers-samples/echo-sample
# Edit deploy.sh to set your FOUNDRY_ACCOUNT, PROJECT, IMAGE, etc.
./deploy.sh
```

## ADC Egress Proxy CA Certificate (hosted runtime)

When an agent runs inside the Azure AI Foundry vNext ("ADC") hosting environment, traffic to
the Foundry **project endpoint** (`https://<project>.services.ai.azure.com/...` — which fronts
Azure OpenAI, the Responses storage API, MSI token exchange, etc.) is routed through a
TLS-terminating egress proxy whose CA certificate is mounted into the container at
`/etc/ssl/certs/adc-egress-proxy-ca.crt`. The JVM does not trust this CA by default, so calls
fail with a `PKIX path building failed` / TLS handshake error.

The proxy is **selective**, it only intercepts `*.services.ai.azure.com`.

### Recommended setup

Bootstrap from your application's `main()` with the zero-argument
`installAdcEgressProxyCertificate()`:

```java
import com.microsoft.agentserver.api.TrustStoreInstaller;

public final class Main {
    public static void main(String[] args) throws Exception {
        // Installs the ADC egress proxy CA and scopes its trust to *.services.ai.azure.com only.
        // Safe to call unconditionally — no-op when the mount path is absent (local dev).
        TrustStoreInstaller.installAdcEgressProxyCertificate();

        // ... rest of startup ...
    }
}
```

This applies a full install with the default host-scoping predicate
(`*.services.ai.azure.com`) and logs through the class-static SLF4J logger.

If you want the install/audit lines to flow through your application's own logger (so they
appear in the same log stream as the rest of your output, e.g. Application Insights), use
the one-arg overload:

```java
TrustStoreInstaller.installAdcEgressProxyCertificate(LOGGER);
```

### Important constraints

* **Call from `main()`, not a static initializer block.** Logging frameworks (e.g. the
  Application Insights Java agent) attach their appenders during early JVM startup; messages
  emitted from a static block can be dropped before the appender is wired up, hiding the
  install confirmation lines.
* **Call before any HTTPS client is created.** Once `SSLContext.getDefault()` has been
  initialized, the system-property switch has no effect for the current JVM.
* **Requires `keytool` / a full JDK image at runtime.** Sample Dockerfiles use
  `eclipse-temurin:25-jdk` (not `-jre`) for this reason.
* **For application-logger output, use the one-arg overload.** By default the install
  audit lines go through the class-static SLF4J logger (`TrustStoreInstaller`); pass your
  own logger to route them into your application's log stream (e.g. Application Insights).
* For an arbitrary (non-ADC) proxy CA, use
  `TrustStoreInstaller.installProxyCertificate(logger, hostPredicate, certPath, aliasPrefix)`.
  Pass `null` for `hostPredicate` to skip the host-scoping layer.

### Alternative: container `run.sh` entrypoint

If you prefer not to modify your application's `main()`, the same effect can be achieved
externally by having the container entrypoint import the cert via `keytool` before launching
the JVM. Each sample ships a `run.sh` of this shape (copied into the image and used as the
Dockerfile `ENTRYPOINT`):

```sh
#!/bin/sh
ADC_CERT="/etc/ssl/certs/adc-egress-proxy-ca.crt"
if [ -f "$ADC_CERT" ]; then
  echo "Importing ADC egress proxy CA cert into Java truststore..."
  cat "$ADC_CERT" >> /etc/ssl/certs/ca-certificates.crt 2>/dev/null || true
  JAVA_CACERTS="$JAVA_HOME/lib/security/cacerts"
  if [ -f "$JAVA_CACERTS" ]; then
    keytool -importcert -noprompt -trustcacerts \
      -alias adc-egress-proxy \
      -file "$ADC_CERT" \
      -keystore "$JAVA_CACERTS" \
      -storepass changeit 2>/dev/null || true
    echo "ADC CA cert imported into Java truststore."
  else
    echo "WARNING: Java cacerts not found at $JAVA_CACERTS"
  fi
else
  echo "No ADC egress proxy CA cert found (not running in vNext)."
fi

exec java \
  ${APPLICATIONINSIGHTS_CONNECTION_STRING:+-javaagent:/app/applicationinsights-agent.jar} \
  -jar /app/app.jar
```

Wire it into the Dockerfile:

```dockerfile
FROM eclipse-temurin:25-jdk
COPY target/app.jar /app/app.jar
COPY run.sh /app/run.sh
RUN chmod +x /app/run.sh
ENTRYPOINT ["/app/run.sh"]
```

Trade-offs vs. the in-process `TrustStoreInstaller` call:

| Aspect                                   | `run.sh` (keytool)              | `TrustStoreInstaller` (Java)            |
|------------------------------------------|---------------------------------|-----------------------------------------|
| Mutates JDK `cacerts` in the image layer | Yes (in-place)                  | No (writes a temp keystore)             |
| Works on JRE-only images                 | No — needs `keytool` (full JDK) | No — also needs `keytool`-capable JDK   |
| Visible in application logs              | Only via stdout (`echo`)        | Via supplied SLF4J logger + AppInsights |
| Local-dev no-op when cert is absent      | Yes                             | Yes                                     |
| Requires touching `main()`               | No                              | Yes (one line)                          |
| Survives container restart (same image)  | Yes                             | Re-runs each JVM start                  |

Pick **one** approach, not both. The Java call is preferred when you want the install
confirmation to flow through your structured logger (and thence to Application Insights /
log aggregation); the shell script is preferred when you want zero application-code changes
or need the cert trusted by non-Java tools in the same container.

## API Endpoints

| Method | Path         | Accept Header       | Description                       |
|--------|--------------|---------------------|-----------------------------------|
| `POST` | `/responses` | `application/json`  | Returns a complete JSON response. |
| `POST` | `/responses` | `text/event-stream` | Returns a streaming SSE response. |

