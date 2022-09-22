## Generate autorest code

```yaml
input-file:
- https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/purview/data-plane/Azure.Analytics.Purview.Share/preview/2021-09-01-preview/share.json
output-folder: ../
java: true
regenerate-pom: false
data-plane: true
generate-tests: true
artifact-id: azure-analytics-purview-share
generate-samples: true
namespace: com.azure.analytics.purview.share
service-versions:
- 2021-09-01-preview
```
