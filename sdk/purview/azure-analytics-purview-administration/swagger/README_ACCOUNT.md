## Generate autorest code
``` yaml
input-file:
  - https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/purview/data-plane/Azure.Analytics.Purview.Account/preview/2019-11-01-preview/account.json

java: true
output-folder: ../
namespace: com.azure.analytics.purview.administration
license-header: MICROSOFT_MIT_SMALL
low-level-client: true
credential-types: tokencredential
credential-scopes: https://purview.azure.net/.default
title: PurviewAccountClient
service-name: PurviewAccount
service-versions:
  - 2019-11-01-preview
```
