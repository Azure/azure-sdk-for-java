# Azure Monitor Ingestion for Java

> see https://aka.ms/autorest

This is the AutoRest configuration file for Monitor Ingestion.

---
## Getting Started
To build the SDK for Monitor Ingestion, simply [Install AutoRest](https://aka.ms/autorest) and
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

```yaml
java: true
use: '@autorest/java@4.1.42'
output-folder: ../
license-header: MICROSOFT_MIT_SMALL
input-file: https://github.com/Azure/azure-rest-api-specs/blob/main/specification/monitor/data-plane/ingestion/stable/2023-01-01/DataCollectionRules.json
namespace: com.azure.monitor.ingestion.implementation
implementation-subpackage: ""
sync-methods: all
required-fields-as-ctor-args: true
credential-types: tokencredential
credential-scopes: https://monitor.azure.com//.default
client-side-validations: true
artifact-id: azure-monitor-ingestion
data-plane: true
enable-sync-stack: true
customization-class: src/main/java/MonitorIngestionCustomizations.java
```
