# Azure Device Update for IoT Hub for Java

> see https://aka.ms/autorest

### Setup
```ps
Fork and clone https://github.com/Azure/autorest.java 
git checkout v4
git submodule update --init --recursive
mvn package -Dlocal
npm install
npm install -g autorest
```

### Generation
```ps
cd <swagger-folder>
autorest --java --use=C:/work/autorest.java
```

### Code generation settings
``` yaml
use: '@autorest/java@4.0.16'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/deviceupdate/data-plane/Microsoft.DeviceUpdate/preview/2020-09-01/deviceupdate.json
java: true
output-folder: ../
namespace: com.azure.iot.deviceupdate
generate-client-interfaces: false
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
generate-sync-async-clients: true
context-client-method-parameter: true
```