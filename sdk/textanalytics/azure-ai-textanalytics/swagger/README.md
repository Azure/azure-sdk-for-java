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
autorest --java --use=C:\Users\shafang\work\autorest.java
```

### Code generation settings
``` yaml
input-file: C:\Users\shafang\work\azure-rest-api-specs\specification\cognitiveservices\data-plane\TextAnalytics\preview\v3.0-preview.1\TextAnalytics.json
java: true
output-folder: C:\Users\shafang\work\azure-sdk-for-java\sdk\cognitiveservices\azure-cs-textanalytics\
namespace: com.azure.ai.textanalytics
enable-xml: true
generate-client-interfaces: false
sync-methods: none
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
models-subpackage: implementation.models
custom-types: DetectedLanguage
custom-types-subpackage: models
```
