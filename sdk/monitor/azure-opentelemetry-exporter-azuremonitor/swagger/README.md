## Generate autorest code
``` yaml
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/applicationinsights/data-plane/Monitor.Exporters/preview/2020-09-15_Preview/swagger.json
java: true
output-folder: ../
namespace: com.azure.opentelemetry.exporter.azuremonitor
generate-client-interfaces: false
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
context-client-method-parameter: true
generate-client-as-impl: true
artifact-id: azure-opentelemetry-exporter-azuremonitor
directive:
    - rename-model:
        from: TrackResponse
        to: ExportResult
```
