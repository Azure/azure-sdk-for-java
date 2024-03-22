## Generate autorest code

``` yaml
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/webpubsub/data-plane/WebPubSub/stable/2022-11-01/webpubsub.json
java: true
output-folder: ..
namespace: com.azure.messaging.webpubsub
generate-client-interfaces: false
sync-methods: all
client-side-validations: true
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
context-client-method-parameter: true
required-parameter-client-methods: false 
generate-client-as-impl: true
service-interface-as-public: true
use-iterable: true
data-plane: true
generate-sync-async-clients: true
service-name: WebPubSubService
generate-builder-per-client: false
service-versions:
  - '2021-10-01'
  - '2022-11-01'
```
