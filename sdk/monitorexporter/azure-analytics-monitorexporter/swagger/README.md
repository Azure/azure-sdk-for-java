## Generate autorest code
``` yaml
input-file: https://raw.githubusercontent.com/srnagar/azure-rest-api-specs/master/specification/applicationinsights/data-plane/Monitor.Exporters/preview/2020-09-15/swagger.json
java: true
output-folder: ../
namespace: com.azure.analytics.monitorexporter
generate-client-interfaces: false
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
context-client-method-parameter: true
generate-client-as-impl: true
artifact-id: azure-analytics-monitorexporter
```
