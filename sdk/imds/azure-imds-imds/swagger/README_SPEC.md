## Generate autorest code

```yaml
input-file:
  - /home/vsts/work/1/s/azure-rest-api-specs/specification/imds/data-plane/Microsoft.InstanceMetadataService/stable/2018-04-02/imds.json
java: true
output-folder: ../
regenerate-pom: false
generate-sync-async-clients: true
generate-client-as-impl: true
generate-client-interfaces: false
add-context-parameter: true
low-level-client: true
sync-methods: all
generate-samples: true
license-header: MICROSOFT_MIT_SMALL
namespace: com.azure.imds.imds
context-client-method-parameter: true
azure-arm: false
credential-types: tokencredential
credential-scopes: https://imds.azure.com/.default
service-versions:
  - '2018-04-02'
```
