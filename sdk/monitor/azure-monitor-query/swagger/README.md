## Generate autorest code
## input-file: https://dev.loganalytics.io/swagger/api.loganalytics.io/v1/swagger.json

## Log Query 
These settings apply only when `--tag=package-log` is specified on the command line.

``` yaml $(tag) == 'package-log'
use: '@autorest/java@4.1.15'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/605407bc0c1a133018285f550d01175469cb3c3a/specification/operationalinsights/data-plane/Microsoft.OperationalInsights/stable/2022-10-27/OperationalInsights.json
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
```

## Metrics Query
These settings apply only when `--tag=package-metrics` is specified on the command line.

``` yaml $(tag) == 'package-metrics'
use: '@autorest/java@4.1.15'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/dba6ed1f03bda88ac6884c0a883246446cc72495/specification/monitor/resource-manager/Microsoft.Insights/stable/2018-01-01/metrics_API.json
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
directive:
    - rename-model:
        from: Response
        to: MetricsResponse
```

## Metrics Namespaces Query
These settings apply only when `--tag=package-metrics-namespaces` is specified on the command line.

``` yaml $(tag) == 'package-metrics-namespaces'
use: '@autorest/java@4.1.15'
service-name: MetricsNamespaces
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/dba6ed1f03bda88ac6884c0a883246446cc72495/specification/monitor/resource-manager/Microsoft.Insights/preview/2017-12-01-preview/metricNamespaces_API.json
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
```

## Metrics Definitions Query
These settings apply only when `--tag=package-metrics-definitions` is specified on the command line.

``` yaml $(tag) == 'package-metrics-definitions'
use: '@autorest/java@4.1.15'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/dba6ed1f03bda88ac6884c0a883246446cc72495/specification/monitor/resource-manager/Microsoft.Insights/stable/2018-01-01/metricDefinitions_API.json
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
```

## Metrics Batch Query
These settings apply only when `--tag=package-metrics-batch` is specified on the command line.

``` yaml $(tag) == 'package-metrics-batch'
use: '@autorest/java@4.1.17'
input-file: https://raw.githubusercontent.com/srnagar/azure-rest-api-specs/fb0e6987b97d7cd6cd13f2f45493f48d3e63e804/specification/monitor/data-plane/Microsoft.Insights/stable/2023-10-01/metricBatch.json
service-name: MetricsDefinitions
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
```
