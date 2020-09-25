# Azure Communication Administration library for Java

> see https://aka.ms/autorest
## Getting Started

To build the SDK for CommunicationIdentity Client, simply Install AutoRest and in this folder, run:

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

There is one swagger for Administration management APIs.

```ps
cd <swagger-folder>
autorest --use=@autorest/java@4.0.1
```

## Update generated files for Administration service
To update generated files for Administration service, run the following command

> autorest --use=@autorest/java@4.0.1

### Code generation settings
``` yaml
input-file: swagger.json
java: true
output-folder: ..\
sync-methods: all
license-header: MICROSOFT_MIT_SMALL	
namespace: com.azure.communication.administration	
generate-client-as-impl: true	
custom-types-subpackage: models
add-context-parameter: true
context-client-method-parameter: true
```