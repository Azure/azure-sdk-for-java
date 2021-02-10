# Azure Communication Service Common library for Java

> see https://aka.ms/autorest
## Getting Started

To build the classes in Common, simply Install AutoRest and in this folder, run:

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

There is one swagger for Common APIs. 

```ps
cd <swagger-folder>
autorest README.md --java --v4 --use=@autorest/java@4.0.2
```

## Update generated files for chat service
To update generated files for chat service, run the following command

> autorest README.md --java --v4 --use=@autorest/java@4.0.2

### Code generation settings
``` yaml
input-file: .\common.json
java: true
output-folder: ..\
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.communication.common
generate-client-as-impl: true
models-subpackage: implementation
generate-client-interfaces: false
generate-sync-async-clients: false
sync-methods: all
add-context-parameter: true
context-client-method-parameter: true
enable-xml: false
required-parameter-client-methods: true
```
