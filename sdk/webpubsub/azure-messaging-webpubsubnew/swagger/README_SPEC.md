## Generate autorest code

```yaml
input-file:
  - https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/webpubsub/data-plane/WebPubSub/stable/2021-10-01/webpubsub.json
java: true
output-folder: ../
partial-update: true
regenerate-pom: false
title: WebPubSubClient
generate-sync-async-clients: true
generate-client-as-impl: true
generate-client-interfaces: false
add-context-parameter: true
artifact-id: azure-messaging-webpubsubnew
low-level-client: true
sync-methods: all
generate-samples: true
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.messaging.webpubsubnew
context-client-method-parameter: true
azure-arm: false
credential-types: tokencredential
credential-scopes: https://webpubsub.azure.com/.default
service-versions:
  - '2021-10-01'
```
