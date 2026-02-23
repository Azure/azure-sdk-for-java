# SimpleWebAppSample — Manual Execution Guide

A long-running web app that generates telemetry and validates customer-facing SDKStats metrics (`Item_Success_Count`, `Item_Dropped_Count`, `Item_Retry_Count`).

---

## Prerequisites

- **Java 21**: `C:\Program Files\Microsoft\jdk-21.0.10.7-hotspot`
- **Maven 3.9.9**: `C:\apache-maven\apache-maven-3.9.9`
- The `azure-monitor-opentelemetry-autoconfigure` module must be compiled (including test sources).

---

## Step-by-Step

### 1. Set up environment variables

Open a PowerShell terminal:

```powershell
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.10.7-hotspot"
$env:PATH = "C:\apache-maven\apache-maven-3.9.9\bin;$env:JAVA_HOME\bin;$env:PATH"
```

### 2. Navigate to the project

```powershell
cd c:\repo\azure-sdk-for-java\sdk\monitor\azure-monitor-opentelemetry-autoconfigure
```

### 3. Compile

```powershell
mvn compile test-compile
```

### 4. Choose a test mode

Set the SDKStats export interval to 60 seconds (instead of the default 900) so results appear sooner:

```powershell
$env:APPLICATIONINSIGHTS_SDKSTATS_EXPORT_INTERVAL = "60"
```

Then pick **one** of the following modes:

#### Success mode (default)

Telemetry is sent to real Azure Monitor. You will see `Item_Success_Count` in the `customMetrics` table.

```powershell
# No TEST_MODE needed — defaults to "success"
```

#### Drop mode

A local mock server on port 9090 returns **400** for all requests. You will see `Item_Dropped_Count` printed in the console.

```powershell
$env:TEST_MODE = "drop"
```

#### Retry mode

A local mock server on port 9090 returns **500** for all requests. You will see `Item_Retry_Count` printed in the console.

```powershell
$env:TEST_MODE = "retry"
```

#### Optional: enable Azure SDK logging

```powershell
$env:AZURE_LOG_LEVEL = "informational"
```

### 5. Run the sample

```powershell
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

Open a **second** PowerShell terminal and hit the endpoints:

```powershell
Invoke-WebRequest http://localhost:8080/
Invoke-WebRequest http://localhost:8080/dependency
Invoke-WebRequest http://localhost:8080/error
Invoke-WebRequest http://localhost:8080/exception
Invoke-WebRequest http://localhost:8080/load
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
