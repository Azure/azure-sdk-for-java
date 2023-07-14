## Generate autorest code
## input-file: https://dev.loganalytics.io/swagger/api.loganalytics.io/v1/swagger.json

## Shared Configurations

```yaml
use: '@autorest/java@4.1.17'
java: true
output-folder: ../
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
client-side-validations: true
artifact-id: azure-monitor-query
disable-client-builder: true
enable-sync-stack: true
```

## Log Query 
These settings apply only when `--tag=package-log` is specified on the command line.

``` yaml $(tag) == 'package-log'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/605407bc0c1a133018285f550d01175469cb3c3a/specification/operationalinsights/data-plane/Microsoft.OperationalInsights/stable/2022-10-27/OperationalInsights.json
namespace: com.azure.monitor.query.implementation.logs
```

## Metrics Query
These settings apply only when `--tag=package-metrics` is specified on the command line.

``` yaml $(tag) == 'package-metrics'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/dba6ed1f03bda88ac6884c0a883246446cc72495/specification/monitor/resource-manager/Microsoft.Insights/stable/2018-01-01/metrics_API.json
namespace: com.azure.monitor.query.implementation.metrics
directive:
    - rename-model:
        from: Response
        to: MetricsResponse
```

## Metrics Namespaces Query
These settings apply only when `--tag=package-metrics-namespaces` is specified on the command line.

``` yaml $(tag) == 'package-metrics-namespaces'
use: '@autorest/java@4.1.17'
service-name: MetricsNamespaces
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/dba6ed1f03bda88ac6884c0a883246446cc72495/specification/monitor/resource-manager/Microsoft.Insights/preview/2017-12-01-preview/metricNamespaces_API.json
namespace: com.azure.monitor.query.implementation.metricsnamespaces
```

``` yaml $(tag) == 'package-metrics-namespaces'
directive:
- from: swagger-document
  where: $.info
  transform: >
    $.title = "MetricsNamespacesClient";
```

## Metrics Definitions Query
These settings apply only when `--tag=package-metrics-definitions` is specified on the command line.

``` yaml $(tag) == 'package-metrics-definitions'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/dba6ed1f03bda88ac6884c0a883246446cc72495/specification/monitor/resource-manager/Microsoft.Insights/stable/2018-01-01/metricDefinitions_API.json
service-name: MetricsDefinitions
namespace: com.azure.monitor.query.implementation.metricsdefinitions
```

``` yaml $(tag) == 'package-metrics-definitions'
directive:
- from: swagger-document
  where: $.info
  transform: >
    $.title = "MetricsDefinitionsClient";
```
