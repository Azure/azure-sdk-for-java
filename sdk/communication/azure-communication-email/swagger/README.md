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
model-override-setter-from-superclass: true
generate-client-interfaces: false
required-fields-as-ctor-args: true
generate-client-as-impl: true
url-as-string: true
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
