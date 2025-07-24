# Azure Monitor OpenTelemetry Exporter for Java

> see https://aka.ms/autorest

This is the AutoRest configuration file for Monitor OpenTelemetry Exporter.

---
## Getting Started
To build the SDK for Monitor OpenTelemetry Exporter, simply [Install AutoRest](https://aka.ms/autorest) and
in this folder, run:

> `autorest`

To see additional help and options, run:

> `autorest --help`

### Setup
```ps
npm install -g autorest
```

### Generation
```ps
cd <swagger-folder>
autorest
```

## Generate autorest code
``` yaml
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/applicationinsights/data-plane/Monitor.Exporters/preview/v2.1/swagger.json
java: true
use: '@autorest/java@4.1.52'
output-folder: ../
models-subpackage: implementation.models
namespace: com.azure.monitor.opentelemetry.exporter
license-header: MICROSOFT_MIT_SMALL
generate-client-as-impl: true
artifact-id: azure-monitor-opentelemetry-exporter
customization-class: src/main/java/MonitorOpenTelemetryExporterCustomizations.java
directive:
    - rename-model:
        from: TrackResponse
        to: ExportResult
```
