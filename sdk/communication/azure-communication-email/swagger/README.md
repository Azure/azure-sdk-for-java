## Generate autorest code

```yaml
require: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/communication/data-plane/Email/readme.md
output-folder: ../
license-header: MICROSOFT_MIT_SMALL
java: true
regenerate-pom: false
data-plane: true
generate-tests: true
artifact-id: azure-communication-email
generate-samples: true
namespace: com.azure.communication.email
custom-types: EmailMessage,EmailCustomHeader,EmailContent,EmailImportance,EmailRecipients,EmailAddress,EmailAttachment,EmailAttachmentType,SendStatus,SendStatusResult
custom-types-subpackage: models
generate-models: true
service-versions:
- 2021-10-01-preview
```

## Customizations for Email Client Generator

See the [AutoRest samples](https://github.com/Azure/autorest/tree/master/Samples/3b-custom-transformations)
for more about how we're customizing things.

### Change the bCC property to bcc

```yaml
directive:
  - from: swagger-document
    where: $.definitions.EmailRecipients.properties.bCC
    transform: >
      $["x-ms-client-name"] = "bcc"
```