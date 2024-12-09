``` yaml
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/applicationinsights/data-plane/Monitor.Exporters/preview/v2.1/swagger.json
java: true
output-folder: ../
namespace: com.azure.sdk.build.tool.implementation
license-header: MICROSOFT_MIT_SMALL
directive:
    - rename-model:
        from: TrackResponse
        to: ExportResult
```
