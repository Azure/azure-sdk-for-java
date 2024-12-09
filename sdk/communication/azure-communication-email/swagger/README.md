# Azure Communication Email Service client library for Java

> see https://aka.ms/autorest

This is the AutoRest configuration file for Communication Email Service.

---
## Getting Started
To build the SDK for Communication Email Service, simply [Install AutoRest](https://aka.ms/autorest) and
in this folder, run:

> `autorest`

To see additional help and options, run:

> `autorest --help`

### Setup
```ps
npm install -g autorest
```

### Generation
```ps
cd <swagger-folder>
autorest
```

## Generate autorest code

```yaml
require: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/e64ad693df24b47d4009eece6663c8d95cf94be6/specification/communication/data-plane/Email/readme.md
output-folder: ../
license-header: MICROSOFT_MIT_SMALL
title: Azure Communication Email Service
java: true
use-extension:
    "@autorest/java": "4.1.42"
artifact-id: azure-communication-email
namespace: com.azure.communication.email
models-subpackage: implementation.models
custom-types: EmailSendStatus,EmailAddress
custom-types-subpackage: models
required-fields-as-ctor-args: true
generate-client-as-impl: true
service-versions:
- 2024-07-01-preview
polling:
  default:
    intermediate-type: EmailSendResult
    final-type: EmailSendResult
```

## Customizations for Email Client Generator

See the [AutoRest samples](https://github.com/Azure/autorest/tree/master/Samples/3b-custom-transformations)
for more about how we're customizing things.

### Remove "To" from the required properties

```yaml
directive:
  - from: swagger-document
    where: $.definitions.EmailRecipients
    transform: >
      $["required"] = []
```
