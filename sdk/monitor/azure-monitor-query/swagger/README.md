## Generate autorest code
## input-file: https://dev.loganalytics.io/swagger/api.loganalytics.io/v1/swagger.json

## Log Query 
These settings apply only when `--tag=package-log` is specified on the command line.

``` yaml $(tag) == 'package-log'
use: '@autorest/java@4.0.22'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/operationalinsights/data-plane/Microsoft.OperationalInsights/preview/2021-05-19_Preview/OperationalInsights.json
java: true
output-folder: ../
namespace: com.azure.monitor.query.log
generate-client-interfaces: false
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
context-client-method-parameter: true
required-parameter-client-methods: false 
generate-client-as-impl: true
models-subpackage: implementation.models
required-fields-as-ctor-args: true
model-override-setter-from-superclass: true
credential-types: tokencredential
client-side-validations: true
artifact-id: azure-monitor-query
```

## Metrics Query
These settings apply only when `--tag=package-metrics` is specified on the command line.

``` yaml $(tag) == 'package-metrics'
use: '@autorest/java@4.0.22'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/monitor/resource-manager/Microsoft.Insights/stable/2018-01-01/metrics_API.json
java: true
output-folder: ../
namespace: com.azure.monitor.query.metrics
generate-client-interfaces: false
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
context-client-method-parameter: true
required-parameter-client-methods: false 
generate-client-as-impl: true
models-subpackage: implementation.models
required-fields-as-ctor-args: true
model-override-setter-from-superclass: true
credential-types: tokencredential
client-side-validations: true
artifact-id: azure-monitor-query
directive:
    - rename-model:
        from: Response
        to: MetricsResponse
```

## Metrics Namespaces Query
These settings apply only when `--tag=package-metrics-namespaces` is specified on the command line.

``` yaml $(tag) == 'package-metrics-namespaces'
use: '@autorest/java@4.0.22'
service-name: MetricsNamespaces
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/monitor/resource-manager/Microsoft.Insights/preview/2017-12-01-preview/metricNamespaces_API.json
java: true
output-folder: ../
namespace: com.azure.monitor.query.metricsnamespaces
generate-client-interfaces: false
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
context-client-method-parameter: true
required-parameter-client-methods: false 
generate-client-as-impl: true
models-subpackage: implementation.models
required-fields-as-ctor-args: true
model-override-setter-from-superclass: true
credential-types: tokencredential
client-side-validations: true
artifact-id: azure-monitor-query
```

## Metrics Definitions Query
These settings apply only when `--tag=package-metrics-definitions` is specified on the command line.

``` yaml $(tag) == 'package-metrics-definitions'
use: '@autorest/java@4.0.22'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/monitor/resource-manager/Microsoft.Insights/stable/2018-01-01/metricDefinitions_API.json
service-name: MetricsDefinitions
java: true
output-folder: ../
namespace: com.azure.monitor.query.metricsdefinitions
generate-client-interfaces: false
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
context-client-method-parameter: true
required-parameter-client-methods: false 
generate-client-as-impl: true
models-subpackage: implementation.models
required-fields-as-ctor-args: true
model-override-setter-from-superclass: true
credential-types: tokencredential
client-side-validations: true
artifact-id: azure-monitor-query
```
