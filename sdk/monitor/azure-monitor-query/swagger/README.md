## Generate autorest code
## input-file: https://dev.loganalytics.io/swagger/api.loganalytics.io/v1/swagger.json

## Log Query 
These settings apply only when `--tag=package-log` is specified on the command line.

``` yaml $(tag) == 'package-log'
use: '@autorest/java@4.0.22'
input-file: log_query_swagger.json
java: true
output-folder: ../
namespace: com.azure.monitor.query.log
generate-client-interfaces: false
sync-methods: all
client-side-validations: true
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
context-client-method-parameter: true
required-parameter-client-methods: false 
generate-client-as-impl: true
models-subpackage: implementation.models
required-fields-as-ctor-args: true
model-override-setter-from-superclass: true
credential-types: tokencredential
```

## Metrics Query
These settings apply only when `--tag=package-metrics` is specified on the command line.

``` yaml $(tag) == 'package-metrics'
use: '@autorest/java@4.0.22'
input-file: metrics_swagger.json
java: true
output-folder: ../
namespace: com.azure.monitor.query.metric
generate-client-interfaces: false
sync-methods: all
client-side-validations: true
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
context-client-method-parameter: true
required-parameter-client-methods: false 
generate-client-as-impl: true
models-subpackage: implementation.models
required-fields-as-ctor-args: true
model-override-setter-from-superclass: true
credential-types: tokencredential
directive:
    - rename-model:
        from: Response
        to: MetricsResponse
```
