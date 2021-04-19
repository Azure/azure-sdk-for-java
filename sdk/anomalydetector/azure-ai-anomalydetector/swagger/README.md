## Generate autorest code
``` yaml
input-file:
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/cognitiveservices/data-plane/AnomalyDetector/preview/v1.1-preview/AnomalyDetector.json
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/cognitiveservices/data-plane/AnomalyDetector/preview/v1.1-preview/MultivariateAnomalyDetector.json
java: true
output-folder: ../
namespace: com.azure.ai.anomalydetector
generate-client-interfaces: false
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
generate-sync-async-clients: true
context-client-method-parameter: true
directive:
    - rename-model:
        from: APIError
        to: ApiError
    - rename-model:
        from: APIErrorException
        to: ApiErrorException
```
