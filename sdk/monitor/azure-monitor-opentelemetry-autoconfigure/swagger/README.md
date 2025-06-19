# Azure Monitor OpenTelemetry Auto Configure for Java

> see https://aka.ms/autorest

This is the Autorest configuration file for Monitor OpenTelemetry Auto Configure.

---
## Getting Started
To build the SDK for Monitor OpenTelemetry Auto Configure, simply [Install Autorest](https://aka.ms/autorest) and
in this folder, run:

> `autorest --tag={swagger specification}`

To see additional help and options, run:

> `autorest --help`

### Setup
```ps
npm install -g autorest
```

### Generation

There are two swagger specifications for Monitor OpenTelemetry Auto Configure: `exporters` and `livemetrics`.
They use the following tags respectively: `--tag=exporters` and `--tag=livemetrics`.

```ps
cd <swagger-folder>
autorest --tag={swagger specification}
```

e.g.
```ps
cd <swagger-folder>
autorest --tag=exporters
autorest --tag=livemetrics
```

## Exporters
These settings apply only when `--tag=exporters` is specified on the command line.

```yaml $(tag) == 'exporters'
use: '@autorest/java@4.1.52'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/applicationinsights/data-plane/Monitor.Exporters/preview/v2.1/swagger.json
java: true
output-folder: ../
namespace: com.azure.monitor.opentelemetry.autoconfigure
models-subpackage: implementation.models
generate-client-as-impl: true
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
artifact-id: azure-monitor-opentelemetry-autoconfigure
enable-sync-stack: true
customization-class: src/main/java/MonitorOpenTelemetryAutoConfigureCustomizations.java
directive:
    - rename-model:
        from: TrackResponse
        to: ExportResult
```

## Live Metrics
These settings apply only when `--tag=livemetrics` is specified on the command line.

```yaml $(tag) == 'livemetrics'
use: '@autorest/java@4.1.52'
input-file: https://github.com/Azure/azure-rest-api-specs/blob/main/specification/applicationinsights/data-plane/LiveMetrics/preview/2024-04-01-preview/livemetrics.json
java: true
output-folder: ../
namespace: com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
artifact-id: azure-monitor-opentelemetry-autoconfigure
enable-sync-stack: true
```

