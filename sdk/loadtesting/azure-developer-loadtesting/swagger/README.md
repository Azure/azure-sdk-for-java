## Generate autorest code

```yaml
require: https://github.com/Azure/azure-rest-api-specs/blob/3e27c70e7c02c07b458bc0e94716c3d82d3fdd19/specification/loadtestservice/data-plane/readme.md
java: true
data-plane: true
title: LoadTestingClient
package-version: 1.0.0-beta.2
security: AADToken
security-scopes: https://cnt-prod.loadtesting.azure.com/.default
artifact-id: azure-developer-loadtesting
namespace: com.azure.developer.loadtesting
generate-builder-per-client: false
partial-update: true
output-folder: $(azure-sdk-for-java-folder)/sdk/loadtesting/azure-developer-loadtesting
service-versions:
- '2022-11-01'
```
