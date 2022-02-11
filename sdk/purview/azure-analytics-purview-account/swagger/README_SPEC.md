## Generate autorest code

```yaml
input-files:
- https://github.com/Azure/azure-rest-api-specs/blob/main/specification/purview/data-plane/Azure.Analytics.Purview.Account/preview/2019-11-01-preview/account.json
output-folder: ../
java: true
regenerate-pom: false
partial-update: true
security: AADToken
security-scopes: https://purview.azure.net/.default
generate-sync-async-clients: true
generate-client-as-impl: true
generate-client-interfaces: false
generate-builder-per-client: true
add-context-parameter: true
artifact-id: azure-analytics-purview-account
low-level-client: true
sync-methods: all
generate-samples: true
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.analytics.purview.account
context-client-method-parameter: true
azure-arm: false
service-versions:
- 2019-11-01-preview
```
