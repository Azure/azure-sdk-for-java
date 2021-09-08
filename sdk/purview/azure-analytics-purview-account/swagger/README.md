## Generate autorest code
``` yaml
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/purview/data-plane/Azure.Analytics.Purview.Account/preview/2019-11-01-preview/account.json
java: true
output-folder: ../
namespace: com.azure.analytics.purview.account
generate-client-interfaces: false
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
low-level-client: true
credential-types: tokencredential
credential-scopes: https://purview.azure.net/.default
title: PurviewAccountClient
service-name: PurviewAccount
service-versions:
  - 2019-11-01-preview
generate-client-as-impl: true
add-context-parameter: true
context-client-method-parameter: true
generate-sync-async-clients: true
artifact-id: azure-analytics-purview-account
llc-properties:
    version: 1.0.0-beta.1
    group-id: com.azure
    rp-name: purview
```
