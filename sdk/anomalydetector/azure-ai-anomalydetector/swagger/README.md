## Generate autorest code
``` yaml
input-file:
- https://raw.githubusercontent.com/conhua/azure-rest-api-specs-pr/conhua/anomaly-detector-multivarite/specification/cognitiveservices/data-plane/AnomalyDetector/preview/v1.1-preview/AnomalyDetector.json?token=AARWQPJZG3BAE354CNKBHUTAMZRVS
- https://raw.githubusercontent.com/conhua/azure-rest-api-specs-pr/conhua/anomaly-detector-multivarite/specification/cognitiveservices/data-plane/AnomalyDetector/preview/v1.1-preview/MultivariateAnomalyDetector.json?token=AARWQPJS3LI6HHHX5MTIKPDAMZR2O
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
