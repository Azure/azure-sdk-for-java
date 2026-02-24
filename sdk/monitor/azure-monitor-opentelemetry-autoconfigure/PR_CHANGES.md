# PR: Add customer-facing SDKStats metrics (Item_Success_Count, Item_Dropped_Count, Item_Retry_Count)

Implements customer-facing SDKStats per the [customer_facing_sdk_stats.md](customer_facing_sdk_stats.md) spec. The exporter now tracks per-telemetry-type success, drop, and retry counts and periodically exports them as `Metric` TelemetryItems through the existing pipeline to the customer's own Application Insights resource.

---

## New files

### Production

| File | Purpose |
|------|---------|
| `CustomerSdkStats.java` | Thread-safe accumulator (`ConcurrentHashMap<Key, AtomicLong>`) for three counter families: `successCounts`, `droppedCounts`, `retryCounts`. `collectAndReset()` atomically snapshots and clears all counters, returning a list of `TelemetryItem` metrics with the correct dimensions (`computeType`, `language`, `version`, `telemetry_type`, `telemetry_success`, `drop.code`/`drop.reason`, `retry.code`/`retry.reason`). Static factory `create(version)` auto-detects the resource provider. |
| `CustomerSdkStatsTelemetryType.java` | Maps `TelemetryItem.getName()` → spec dimension strings: `Request`→`REQUEST`, `RemoteDependency`→`DEPENDENCY`, `Message`→`TRACE`, `Exception`→`EXCEPTION`, `Metric`→`CUSTOM_METRIC`, `Event`→`CUSTOM_EVENT`, `PageView`→`PAGE_VIEW`, `Availability`→`AVAILABILITY`. Returns `null` for internal items (e.g. `Statsbeat`) to skip counting. |
| `CustomerSdkStatsExceptionCategory.java` | Classifies exceptions into low-cardinality reason strings for `drop.reason`/`retry.reason`: `"Timeout exception"`, `"Network exception"`, `"Storage exception"`, `"Client exception"`. Traverses the cause chain (depth ≤ 10). Also provides `isTimeout()` to choose between `CLIENT_TIMEOUT` and `CLIENT_EXCEPTION` retry codes. |
| `CustomerSdkStatsTelemetryPipelineListener.java` | `TelemetryPipelineListener` that routes pipeline responses to the accumulator: 200→success, retryable (401/403/408/429/500/502/503/504)→retry, redirect (307/308)→skip, all others→drop. `onException` categorizes the throwable and records a retry. Includes `getReasonPhraseForStatusCode()` for common HTTP status codes. |

### Tests

| File | # Tests |
|------|---------|
| `CustomerSdkStatsTest.java` | 10 (accumulation, reset, concurrent increments, telemetry_success split) |
| `CustomerSdkStatsTelemetryTypeTest.java` | 10 (all 8 mappings + Statsbeat→null + unknown→null) |
| `CustomerSdkStatsExceptionCategoryTest.java` | 9 (timeout, network, storage, client, null, wrapped exceptions) |
| `CustomerSdkStatsTelemetryPipelineListenerTest.java` | 9 (success, retry 429/500, drop 402, timeout/network exception, empty skip, redirect skip, reason phrases) |
| **Total** | **38 tests** |

### Sample

| File | Purpose |
|------|---------|
| `SimpleWebAppSample.java` | Long-running web app with 5 endpoints and 3 test modes (`success`/`drop`/`retry`). In drop/retry modes, a built-in mock ingestion server on port 9090 returns the configured error status and prints all gunzipped payloads to the console, tagged `[SDKStats]` when applicable. |
| `SimpleWebAppSample-README.md` | Step-by-step manual execution guide with env var setup, endpoint table, Kusto query, and expected console output. |

---

## Modified files

| File | Change |
|------|--------|
| `TelemetryItemExporter.java` | Added `computeItemCountMetadata()` to compute per-type item counts (with success/failure split for REQUEST/DEPENDENCY) before serialization. Added `sendWithoutTracking()` to send items without triggering recursive customer SDKStats counting. Updated `internalSendByBatch()` to pass item count maps to the pipeline. |
| `TelemetryPipelineRequest.java` | Added `itemCountsByType`, `successItemCountsByType`, `failureItemCountsByType` fields + overloaded public constructor + getters. The original constructor delegates with empty maps. |
| `TelemetryPipelineResponse.java` | Made constructor `public` (was package-private) for test access. |
| `TelemetryPipeline.java` | Added overloaded `send()` accepting item count maps; original `send()` delegates with empty maps. Passes maps into `TelemetryPipelineRequest`. |
| `AzureMonitorHelper.java` | `createTelemetryItemExporter()` now accepts `CustomerSdkStats`, creates a `CustomerSdkStatsTelemetryPipelineListener`, and wires it into the composite listener chain (alongside `DiagnosticTelemetryPipelineListener` and `LocalStorageTelemetryPipelineListener`). |
| `AzureMonitorExporterBuilder.java` | Added `SDKSTATS_DISABLED_ENV_VAR`, `SDKSTATS_EXPORT_INTERVAL_ENV_VAR` constants. Added `createCustomerSdkStats()` and `startCustomerSdkStats()` with a `ScheduledExecutorService` (daemon thread, default 900s interval) that calls `collectAndReset()` and `sendWithoutTracking()`. |

---

## Configuration

| Environment Variable | Default | Description |
|---------------------|---------|-------------|
| `APPLICATIONINSIGHTS_SDKSTATS_DISABLED` | `false` | Set to `true` to disable customer SDKStats entirely. |
| `APPLICATIONINSIGHTS_SDKSTATS_EXPORT_INTERVAL` | `900` (15 min) | Export interval in seconds. |

---

## Metric details

| Metric Name | Dimensions | When counted |
|-------------|------------|--------------|
| `Item_Success_Count` | `computeType`, `language`, `version`, `telemetry_type` | HTTP 200 from ingestion |
| `Item_Dropped_Count` | `computeType`, `language`, `version`, `telemetry_type`, `drop.code`, `drop.reason`, `telemetry_success` | Non-retryable HTTP status (e.g. 400, 402, 404) |
| `Item_Retry_Count` | `computeType`, `language`, `version`, `telemetry_type`, `retry.code`, `retry.reason` | Retryable HTTP status (401, 403, 408, 429, 500-504) or client exception/timeout |

---

## Validation

- **Unit tests**: 38/38 passing
- **Code formatting**: `spotlessApply` clean
- **Compilation**: Module compiles successfully
- **E2E success mode**: `Item_Success_Count` visible in Azure Monitor `customMetrics` table with correct dimensions
- **E2E drop mode**: `Item_Dropped_Count` visible in mock server console with `drop.code=400`, `drop.reason="Bad request"`, per-type/per-success breakdown
- **E2E retry mode**: Mock infrastructure ready (mock server returns 500)
