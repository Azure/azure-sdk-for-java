# Azure Document Intelligence for Java

> see https://aka.ms/autorest

This is the Autorest configuration file for Document Intelligence.

---
## Getting Started
To build the SDK for Document Intelligence, simply [Install Autorest](https://aka.ms/autorest) and
in this folder, run:

> `autorest --tag={swagger specification}`

To see additional help and options, run:

> `autorest --help`

### Setup
```ps
npm install -g autorest
```

### Generation

There are two swagger specifications for Document Intelligence: `formrecognizer-v2.1` and `formrecognizer-documentanalysis`.
They use the following tags respectively: `--tag=formrecognizer-v2.1`, `--tag=formrecognizer-documentanalysis`.

```ps
cd <swagger-folder>
autorest --tag={swagger specification}
```

e.g.
```ps
cd <swagger-folder>
autorest --tag=formrecognizer-v2.1
autorest --tag=formrecognizer-documentanalysis
```

## Form Recognizer Service V2.1
### To run, use `autorest --tag:formrecognizer-v2.1 README.md`

``` yaml $(tag) == 'formrecognizer-v2.1'
use: '@autorest/java@4.1.52'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/cognitiveservices/data-plane/FormRecognizer/stable/v2.1/FormRecognizer.json
java: true
output-folder: ..\
generate-client-as-impl: true
namespace: com.azure.ai.formrecognizer
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
models-subpackage: implementation.models
custom-types-subpackage: models
custom-types: LengthUnit,TextStyleName
disable-client-builder: true
```

### Add multiple service API support
This is better to fixed in the swagger, but we are working around now.
``` yaml $(tag) == 'formrecognizer-v2.1'
directive:
- from: swagger-document
  where: $["x-ms-parameterized-host"]
  transform: >
    $.hostTemplate = "{endpoint}/formrecognizer/{ApiVersion}";
    $.parameters.push({
      "name": "ApiVersion",
      "description": "Form Recognizer API version.",
      "x-ms-parameter-location": "client",
      "required": true,
      "type": "string",
      "in": "path",
      "x-ms-skip-url-encoding": true
    });
```

### Rename TextStyle to TextStyleName
``` yaml $(tag) == 'formrecognizer-v2.1'
directive:
- from: swagger-document
  where: $.definitions.Style
  transform: >
    $.properties.name["x-ms-enum"].name = "TextStyleName";
```


## Form Recognizer Service 2023-07-31
### To run, use `autorest --tag:formrecognizer-documentanalysis README.md`
``` yaml $(tag) == 'formrecognizer-documentanalysis'
use: '@autorest/java@4.1.52'
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/cognitiveservices/data-plane/FormRecognizer/stable/2023-07-31/FormRecognizer.json
java: true
output-folder: ..\
generate-client-as-impl: true
namespace: com.azure.ai.formrecognizer.documentanalysis
sync-methods: all
license-header: MICROSOFT_MIT_SMALL
models-subpackage: implementation.models
custom-types-subpackage: models
custom-types: DocumentBarcodeKind,DocumentFormulaKind,DocumentPageKind,FontStyle,FontWeight,ParagraphRole,DocumentAnalysisFeature
customization-class: src/main/java/FormRecognizerDocumentAnalysisCustomization.java
required-fields-as-ctor-args: true
enable-sync-stack: true
polling: {}
disable-client-builder: true
```

### Expose PathOperationId & PathResultId as String
``` yaml $(tag) == 'formrecognizer-documentanalysis'
directive:
  - from: swagger-document
    where: $.parameters
    transform: >
      delete $.PathOperationId["format"];
      delete $.PathResultId["format"];
```
