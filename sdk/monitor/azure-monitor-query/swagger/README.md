# Azure Monitor Query for Java

> see https://aka.ms/autorest

This is the Autorest configuration file for Monitor Query.

---
## Getting Started
To build the SDK for Monitor Query, simply [Install Autorest](https://aka.ms/autorest) and
in this folder, run:

> `autorest --tag={swagger specification}`

To see additional help and options, run:

> `autorest --help`

### Setup
```ps
npm install -g autorest
```

### Generation

There are five swagger specifications for Monitor Query: `log`, `metrics`, `metrics-namespaces`, `metrics-definitions`,
and `metrics-batch`.
They use the following tags respectively: `--tag=log`, `--tag=metrics`, `--tag=metrics-namespaces`,
`--tag=metrics-definitions`, `--tag=metrics-batch`.

```ps
cd <swagger-folder>
autorest --tag={swagger specification}
```

e.g.
```ps
cd <swagger-folder>
autorest --tag=log
autorest --tag=metrics
autorest --tag=metrics-namespaces
autorest --tag=metrics-definitions
autorest --tag=metrics-batch
```

## input-file: https://dev.loganalytics.io/swagger/api.loganalytics.io/v1/swagger.json

## Log Query
These settings apply only when `--tag=log` is specified on the command line.

```yaml $(tag) == 'log'
use: '@autorest/java@4.1.39'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/21f5332f2dc7437d1446edf240e9a3d4c90c6431/specification/operationalinsights/data-plane/Microsoft.OperationalInsights/stable/2022-10-27/OperationalInsights.json
java: true
output-folder: ../
namespace: com.azure.monitor.query.implementation.logs
generate-client-interfaces: false
service-interface-as-public: true
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
context-client-method-parameter: true
required-parameter-client-methods: false
required-fields-as-ctor-args: true
model-override-setter-from-superclass: true
credential-types: tokencredential
artifact-id: azure-monitor-query
customization-class: src/main/java/LogsCustomization.java
enable-sync-stack: true
stream-style-serialization: true
```

## Metrics Query
These settings apply only when `--tag=metrics` is specified on the command line.

```yaml $(tag) == 'metrics'
use: '@autorest/java@4.1.39'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/0b64ca7cbe3af8cd13228dfb783a16b8272b8be2/specification/monitor/resource-manager/Microsoft.Insights/stable/2024-02-01/metrics_API.json
java: true
output-folder: ../
namespace: com.azure.monitor.query.implementation.metrics
generate-client-interfaces: false
service-interface-as-public: true
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
context-client-method-parameter: true
required-parameter-client-methods: false 
required-fields-as-ctor-args: true
model-override-setter-from-superclass: true
credential-types: tokencredential
artifact-id: azure-monitor-query
customization-class: src/main/java/MetricsCustomization.java
enable-sync-stack: true
stream-style-serialization: true
directive:
    - rename-model:
        from: Response
        to: MetricsResponse
```

### Change Interval to type 'Duration'

```yaml $(tag) == 'metrics'
directive:
- from: swagger-document
  where: $.definitions.MetricsResponse.properties.interval
  transform: >
    $["format"] = "duration";
```

```yaml $(tag) == 'metrics'
directive:
- from: swagger-document
  where: $.parameters.IntervalParameter
  transform: >
    $["format"] = "duration";
```

## Metrics Namespaces Query
These settings apply only when `--tag=metrics-namespaces` is specified on the command line.

```yaml $(tag) == 'metrics-namespaces'
use: '@autorest/java@4.1.39'
service-name: MetricsNamespaces
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/0b64ca7cbe3af8cd13228dfb783a16b8272b8be2/specification/monitor/resource-manager/Microsoft.Insights/stable/2024-02-01/metricNamespaces_API.json
java: true
output-folder: ../
namespace: com.azure.monitor.query.implementation.metricsnamespaces
generate-client-interfaces: false
service-interface-as-public: true
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
context-client-method-parameter: true
required-parameter-client-methods: false 
required-fields-as-ctor-args: true
model-override-setter-from-superclass: true
credential-types: tokencredential
artifact-id: azure-monitor-query
customization-class: src/main/java/MetricsNamespacesCustomization.java
enable-sync-stack: true
stream-style-serialization: true
```

## Metrics Definitions Query
These settings apply only when `--tag=metrics-definitions` is specified on the command line.

```yaml $(tag) == 'metrics-definitions'
use: '@autorest/java@4.1.39'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/0b64ca7cbe3af8cd13228dfb783a16b8272b8be2/specification/monitor/resource-manager/Microsoft.Insights/stable/2024-02-01/metricDefinitions_API.json
service-name: MetricsDefinitions
java: true
output-folder: ../
namespace: com.azure.monitor.query.implementation.metricsdefinitions
generate-client-interfaces: false
service-interface-as-public: true
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
context-client-method-parameter: true
required-parameter-client-methods: false 
required-fields-as-ctor-args: true
model-override-setter-from-superclass: true
credential-types: tokencredential
artifact-id: azure-monitor-query
customization-class: src/main/java/MetricsDefinitionsCustomization.java
enable-sync-stack: true
stream-style-serialization: true
```

## Metrics Batch Query
These settings apply only when `--tag=metrics-batch` is specified on the command line.

```yaml $(tag) == 'metrics-batch'
use: '@autorest/java@4.1.39'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/0550754fb421cd3a5859abf6713a542b682f626c/specification/monitor/data-plane/Microsoft.Insights/stable/2024-02-01/metricBatch.json
service-name: MetricsBatch
java: true
output-folder: ../
namespace: com.azure.monitor.query.implementation.metricsbatch
generate-client-interfaces: false
service-interface-as-public: true
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
context-client-method-parameter: true
required-parameter-client-methods: false
required-fields-as-ctor-args: true
model-override-setter-from-superclass: true
credential-types: tokencredential
artifact-id: azure-monitor-query
enable-sync-stack: true
stream-style-serialization: true
customization-class: src/main/java/MetricsClientCustomization.java
```

### Change Interval to type 'Duration'

```yaml $(tag) == 'metrics-batch' 
directive:
    - from: swagger-document
      where: $.parameters.IntervalParameter
      transform: >
          $["format"] = "duration";
```

### Change subscriptionId to type 'String'
```yaml $(tag) == 'metrics-batch' 
directive:
    - from: swagger-document
      where: $.parameters.SubscriptionIdParameter
      transform: >
          $["format"] = "";
```
