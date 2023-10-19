``` yaml
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/applicationinsights/data-plane/Monitor.Exporters/preview/v2.1/swagger.json
java: true
output-folder: ../
namespace: com.azure.sdk.build.tool.implementation
generate-client-interfaces: false
service-interface-as-public: true
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
context-client-method-parameter: true
directive:
    - rename-model:
        from: TrackResponse
        to: ExportResult
```
