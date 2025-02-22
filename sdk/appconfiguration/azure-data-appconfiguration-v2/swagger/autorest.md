``` yaml
use: '@autorest/java@4.1.39'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/c1af3ab8e803da2f40fc90217a6d023bc13b677f/specification/appconfiguration/data-plane/Microsoft.AppConfiguration/stable/2023-11-01/appconfiguration.json
java: true
output-folder: ../
namespace: com.azure.v2.data.appconfiguration
generate-client-interfaces: false
license-header: MICROSOFT_MIT_SMALL
sync-methods: sync-only
context-client-method-parameter: true
service-interface-as-public: true
enable-sync-stack: true
generic-response-type: true
stream-style-serialization: true
generate-sync-async-clients: true
flavor: azurev2
disable-typed-headers-methods: true
```
