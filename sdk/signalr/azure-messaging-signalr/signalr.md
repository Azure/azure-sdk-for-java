## Generate autorest code

``` yaml
use: '@autorest/java@4.0.4'
input-file: https://raw.githubusercontent.com/Azure/azure-signalr-vnext-features/master/serverless-websocket/specs/ws.swagger.json
java: true
output-folder: src/main/java
namespace: com.azure.messaging.signalr.implementation.client
generate-client-interfaces: false
sync-methods: none
client-side-validations: true
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
generate-sync-async-clients: true
context-client-method-parameter: true
required-parameter-client-methods: false
```