# Azure Cognitive Service - Text Analytics for Java

> see https://aka.ms/autorest

### Setup
```ps
Fork and clone https://github.com/Azure/autorest.java 
Add a system envirement variable, `AUTOREST_HOME=C:\work\autorestHome`, which contains all autorest related pacakges.
such as `@autorest_core`, `@autorest_java`, `@autorest_modelerfour`. 

git checkout main
git submodule update --init --recursive
mvn package -Dlocal
npm install
npm install -g autorest
```

### Generation
```ps
cd <swagger-folder>
autorest
```

Alternately, for using the specific autorest java version, use `--use:@autorest/java@{specific-version}`. The version 
will be downloaded and compiled locally in `AUTOREST_HOME`.

```ps
cd <swagger-folder>
autorest --java --use:@autorest/java@4.1.9 README.md
```

### Code generation settings
``` yaml
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/527f6d35fb0d85c48210ca0f6f6f42814d63bd33/specification/cognitiveservices/data-plane/Language/preview/2022-10-01-preview/analyzetext.json
java: true
output-folder: ..\
generate-client-as-impl: true
namespace: com.azure.ai.textanalytics
generate-client-interfaces: false
enable-sync-stack: true
license-header: MICROSOFT_MIT_SMALL
add-context-parameter: true
models-subpackage: implementation.models
custom-types-subpackage: models
context-client-method-parameter: true
service-interface-as-public: true
generic-response-type: true
```
