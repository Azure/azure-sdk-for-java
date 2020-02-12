# Azure Cognitive Service - Text Analytics for Java

> see https://aka.ms/autorest

### Setup
```ps
Fork and clone https://github.com/Azure/autorest.java 
git checkout v3
git submodule update --init --recursive
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
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/master/specification/cognitiveservices/data-plane/FormRecognizer/preview/v2.0/FormRecognizer.json
java: true
output-folder: ..\
namespace: com.azure.ai.formrecognizer
enable-xml: true
generate-client-interfaces: false
sync-methods: none
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
models-subpackage: implementation.models
custom-types-subpackage: models
```
