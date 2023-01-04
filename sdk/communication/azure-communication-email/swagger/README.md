## Generate autorest code

```yaml
require: https://raw.githubusercontent.com/apattath/azure-rest-api-specs-apattath/main/specification/communication/data-plane/Email/readme.md
output-folder: ../
license-header: MICROSOFT_MIT_SMALL
java: true
regenerate-pom: false
data-plane: true
generate-tests: true
artifact-id: azure-communication-email
generate-samples: true
namespace: com.azure.communication.email
custom-types: EmailMessage,EmailContent,EmailRecipients,EmailAddress,EmailAttachment,SendStatus,SendStatusResult
custom-types-subpackage: models
generate-models: true
service-versions:
- 2023-01-15-preview
```

## Customizations for Email Client Generator

See the [AutoRest samples](https://github.com/Azure/autorest/tree/master/Samples/3b-custom-transformations)
for more about how we're customizing things.

### Remove the LRO property from SEND

```yaml
directive:
  - from: swagger-document
    where: '$.paths["/emails:send"].post'
    transform: >
      $["x-ms-long-running-operation"] = false
```
