## Generate autorest code

``` yaml
use: '@autorest/java@4.0.24'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/webpubsub/data-plane/WebPubSub/preview/2021-08-01-preview/webpubsub.json
java: true
output-folder: ..
namespace: com.azure.messaging.webpubsub.llc
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
low-level-client: true
generate-sync-async-clients: true
service-name: WebPubSubService
```
