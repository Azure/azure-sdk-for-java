## Generate autorest code

``` yaml
use: '@autorest/java@4.0.4'
input-file: https://raw.githubusercontent.com/Azure/azure-signalr-vnext-features/master/serverless-websocket/specs/ws.swagger.json
java: true
output-folder: ./
namespace: com.azure.messaging.signalr
generate-client-interfaces: false
sync-methods: all
client-side-validations: true
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
context-client-method-parameter: true
required-parameter-client-methods: false 
generate-client-as-impl: true
models-subpackage: implementation.models
service-interface-as-public: true
```
