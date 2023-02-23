## Generate autorest code

```yaml
input-file:
- https://github.com/Azure/azure-rest-api-specs/blob/12cbd66750c4dfe302e9eca42b1fc727f8c02700/specification/purview/data-plane/Azure.Analytics.Purview.Share/preview/2023-02-15-preview/share.json
output-folder: ../
java: true
regenerate-pom: false
data-plane: true
generate-tests: true
artifact-id: azure-analytics-purview-sharing
generate-samples: true
namespace: com.azure.analytics.purview.sharing
service-versions:
- 2023-02-15-preview
```
