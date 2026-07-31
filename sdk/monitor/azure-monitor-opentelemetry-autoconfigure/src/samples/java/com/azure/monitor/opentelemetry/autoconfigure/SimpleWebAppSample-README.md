# SimpleWebAppSample — Manual Execution Guide

A long-running web app that generates telemetry and validates customer-facing SDKStats metrics (`Item_Success_Count`, `Item_Dropped_Count`, `Item_Retry_Count`).

---

## Prerequisites

- **Java 8+** (JDK 8, 11, 17, or 21)
- **Maven 3.6+**
- The `azure-monitor-opentelemetry-autoconfigure` module must be compiled (including test sources).

---

## Step-by-Step

### 1. Set up environment variables

Ensure `JAVA_HOME` points to your JDK installation and Maven is on your `PATH`.

**Windows (PowerShell):**
```powershell
$env:JAVA_HOME = "<path-to-your-jdk>"
$env:PATH = "<path-to-maven>/bin;$env:JAVA_HOME/bin;$env:PATH"
```

**Linux / macOS:**
```bash
export JAVA_HOME=<path-to-your-jdk>
export PATH=<path-to-maven>/bin:$JAVA_HOME/bin:$PATH
```

### 2. Navigate to the project

```bash
cd sdk/monitor/azure-monitor-opentelemetry-autoconfigure
```

### 3. Compile

```bash
mvn compile test-compile
```

### 4. Choose a test mode

Set the SDKStats export interval to 60 seconds (instead of the default 900) so results appear sooner:

**Windows (PowerShell):**
```powershell
$env:APPLICATIONINSIGHTS_SDKSTATS_EXPORT_INTERVAL = "60"
```

**Linux / macOS:**
```bash
export APPLICATIONINSIGHTS_SDKSTATS_EXPORT_INTERVAL=60
```

Then pick **one** of the following modes:

#### Success mode (default)

Telemetry is sent to real Azure Monitor. You will see `Item_Success_Count` in the `customMetrics` table.
Set `APPLICATIONINSIGHTS_CONNECTION_STRING` to your Application Insights connection string.

```bash
# No TEST_MODE needed — defaults to "success"
```

#### Drop mode

A local mock server on port 9090 returns **400** for all requests. You will see `Item_Dropped_Count` printed in the console.

**Windows:** `$env:TEST_MODE = "drop"` &nbsp;|&nbsp; **Linux/macOS:** `export TEST_MODE=drop`

#### Retry mode

A local mock server on port 9090 returns **500** for all requests. You will see `Item_Retry_Count` printed in the console.

**Windows:** `$env:TEST_MODE = "retry"` &nbsp;|&nbsp; **Linux/macOS:** `export TEST_MODE=retry`

#### Optional: enable Azure SDK logging

**Windows:** `$env:AZURE_LOG_LEVEL = "informational"` &nbsp;|&nbsp; **Linux/macOS:** `export AZURE_LOG_LEVEL=informational`

### 5. Run the sample

```bash
mvn exec:java "-Dexec.mainClass=com.azure.monitor.opentelemetry.autoconfigure.SimpleWebAppSample" "-Dexec.classpathScope=test"
```

You should see:

```
========================================================
 SimpleWebAppSample running on http://localhost:8080
 Mode: SUCCESS — real Azure Monitor → Item_Success_Count
========================================================
```

### 6. Generate telemetry

Open a **second** terminal and hit the endpoints:

```bash
curl http://localhost:8080/
curl http://localhost:8080/dependency
curl http://localhost:8080/error
curl http://localhost:8080/exception
curl http://localhost:8080/load
```

| Endpoint       | What it does                              |
|----------------|-------------------------------------------|
| `GET /`        | Returns "Hello!" (request span)           |
| `GET /dependency` | Makes an outbound HTTP call (dependency span) |
| `GET /error`   | Simulates an error span                   |
| `GET /exception` | Throws & records an exception           |
| `GET /load`    | Fires 20 mixed child spans for volume     |

### 7. Wait ~60 seconds

After the export interval elapses, you will see SDKStats metrics:

- **Success mode**: Query `customMetrics` in Azure Monitor:
  ```kusto
  customMetrics
  | where name startswith "Item_"
  | project timestamp, name, value, customDimensions
  | order by timestamp desc
  ```
- **Drop/Retry mode**: SDKStats payloads appear in the **first terminal's console output**, tagged with `[SDKStats]`:
  ```
  ╔══ MOCK INGESTION #4 [SDKStats] ══════════════════════════════
  ║ POST /v2.1/track → returning 400
  ║ {"name":"Metric","data":{"baseData":{"metrics":[{"name":"Item_Dropped_Count",...}]}}}
  ╚═══════════════════════════════════════════════════════════════
  ```

### 8. Stop the sample

Press `Ctrl+C` in the terminal running the sample.

---

## Notes

- In **drop/retry** modes, both application telemetry and SDKStats metrics go through the same mock pipeline. SDKStats metrics are visible only in the console output, not in Azure Monitor.
- The mock server automatically gunzips incoming payloads and prints each telemetry item on its own line.
- SDKStats dimensions include: `computeType`, `language`, `version`, `telemetry_type`, `telemetry_success`, and mode-specific fields (`drop.code`/`drop.reason` or `retry.code`/`retry.reason`).
