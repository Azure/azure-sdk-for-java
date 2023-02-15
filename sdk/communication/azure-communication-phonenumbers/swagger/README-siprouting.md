# Azure Communication Phone Numbers SIP Routing library for Java

> see https://aka.ms/autorest
## Getting Started

To build the SDK for Communication Phone Numbers SIP Routing library, simply Install AutoRest and in this folder, run:

### Setup
```ps
Fork and clone https://github.com/Azure/autorest.java
git checkout main
git submodule update --init --recursive
mvn package -Dlocal
npm install
npm install -g autorest
```

### Generation
```ps
cd <swagger-folder>
mvn install
autorest README-siprouting.md --java --v4 --use=@autorest/java@4.1.3
```

## Update generated files for Sip Routing service
To update generated files for Sip Routing service, run the following command

> autorest README-siprouting.md --java --v4 --use=@autorest/java@4.0.40

### Code generation settings
```yaml
title: Azure Communication Phone Numbers SIP Routing Service
tag: package-2021-05-01-preview
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/3b84377154da23b8b5a011a7c0344f36f56ced16/specification/communication/data-plane/SipRouting/preview/2021-05-01-preview/communicationservicessiprouting.json
override-client-name: SipRoutingAdminClient
custom-types-subpackage: models
models-subpackage: implementation.models
java: true
output-folder: ..\
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.communication.phonenumbers.siprouting
generate-client-as-impl: true
sync-methods: all
context-client-method-parameter: true
add-context-parameter: true
service-interface-as-public: true
customization-class: src/main/java/SipRoutingCustomizations.java
```

### Use SipConfiguration instead of SipConfigurationPatch
```yaml
directive:
    - from: swagger-document
      where: $.paths["/sip"].patch.parameters[1].schema
      transform: >
          $.$ref = "#/definitions/SipConfiguration";
```

### Delete SipConfigurationPatch and TrunkPatch
```yaml
directive:
    - from: swagger-document
      where: $.definitions
      transform: >
          delete $.SipConfigurationPatch;
          delete $.TrunkPatch;
```

### Directive renaming "Trunk" model to "SipTrunk"
```yaml
directive:
    - from: swagger-document
      where: "$.definitions.Trunk" 
      transform: >
          $["x-ms-client-name"] = "SipTrunk";
```

### Directive renaming "TrunkRoute" model to "SipTrunkRoute"
```yaml
directive:
    - from: swagger-document
      where: "$.definitions.TrunkRoute" 
      transform: >
          $["x-ms-client-name"] = "SipTrunkRoute";
```
