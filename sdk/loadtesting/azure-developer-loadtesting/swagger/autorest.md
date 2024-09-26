# Azure Load Test Service for Java

> see https://aka.ms/autorest

This is the AutoRest configuration file for Load Test Service.

---
## Getting Started
To build the SDKs for Load Test Service, simply [Install Autorest](https://aka.ms/autorest) and in this folder, run:

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

### Configuration

```yaml
use: '@autorest/java@4.1.26'
output-folder: ../
java: true
input-file: https://github.com/Azure/azure-rest-api-specs/blob/3e27c70e7c02c07b458bc0e94716c3d82d3fdd19/specification/loadtestservice/data-plane/Microsoft.LoadTestService/stable/2022-11-01/loadtestservice.json
title: LoadTestingClient
namespace: com.azure.developer.loadtesting
artifact-id: azure-developer-loadtesting
enable-sync-stack: true
generate-builder-per-client: true
data-plane: true
security: AADToken
security-scopes: https://cnt-prod.loadtesting.azure.com/.default
partial-update: true
stream-style-serialization: true
service-versions:
- '2022-11-01'
directive:
- rename-operation:
    from: LoadTestRun_ListMetricNamespaces
    to: LoadTestRun_GetMetricNamespaces
- rename-operation:
    from: LoadTestRun_ListMetricDefinitions
    to: LoadTestRun_GetMetricDefinitions
- where-operation: LoadTestRun_ListMetrics
  transform: $['parameters'][3]['x-ms-client-name'] = 'metricName'
- where-operation: LoadTestRun_ListMetricDimensionValues
  transform: $['parameters'][3]['x-ms-client-name'] = 'metricName'
```
