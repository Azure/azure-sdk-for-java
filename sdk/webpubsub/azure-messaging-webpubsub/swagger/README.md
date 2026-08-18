# Azure Web PubSub for Java

> see https://aka.ms/autorest

This is the AutoRest configuration file for Web PubSub.
---
## Getting Started

To build the SDK for Web PubSub, simply [Install AutoRest](https://aka.ms/autorest) and in this folder, run:

> `autorest`

To see additional help and options, run:

> `autorest --help`

### Setup
```ps
npm install -g autorest
```

### Generation

```ps
cd <swagger-folder>
autorest
```

## Generate autorest code

``` yaml
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/webpubsub/data-plane/WebPubSub/stable/2024-12-01/webpubsub.json
java: true
use: '@autorest/java@4.1.52'
output-folder: ..
namespace: com.azure.messaging.webpubsub
sync-methods: all
client-side-validations: true
license-header: MICROSOFT_MIT_SMALL
generate-client-as-impl: true
enable-page-size: true
data-plane: true
generate-sync-async-clients: true
service-name: WebPubSubService
generate-builder-per-client: false
partial-update: true
disable-client-builder: true
service-versions:
  - '2021-10-01'
  - '2022-11-01'
  - '2024-01-01'
  - '2024-12-01'
```
