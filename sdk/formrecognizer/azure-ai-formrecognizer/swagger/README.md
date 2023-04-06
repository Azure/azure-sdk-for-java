# Azure Cognitive Service - Form Recognizer for Java

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
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/cognitiveservices/data-plane/FormRecognizer/preview/2023-02-28-preview/FormRecognizer.json
java: true
output-folder: ..\
generate-client-as-impl: true
namespace: com.azure.ai.formrecognizer.documentanalysis
generate-client-interfaces: false
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
models-subpackage: implementation.models
context-client-method-parameter: true
service-interface-as-public: true
custom-strongly-typed-header-deserialization: true
generic-response-type: true
custom-types-subpackage: models
custom-types: DocumentFormulaKind,DocumentAnnotationKind,DocumentPageKind,DocumentBarCodeKind,DocumentPageKind,FontStyle,FontWeight
required-fields-as-ctor-args: true
enable-sync-stack: true
polling: {}
```

### Expose PathOperationId & PathResultId as String
``` yaml $(java)
directive:
  - from: swagger-document
    where: $.parameters
    transform: >
      delete $.PathOperationId["format"];
      delete $.PathResultId["format"];
```

