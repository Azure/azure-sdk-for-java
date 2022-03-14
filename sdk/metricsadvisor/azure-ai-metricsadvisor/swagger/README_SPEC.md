## Generate autorest code

```yaml
input-file:
- https://github.com/weidongxu-microsoft/azure-sdk-for-java/blob/dpg-customizations/sdk/metricsadvisor/azure-ai-metricsadvisor/swagger/MetricsAdvisor.json
output-folder: ../
java: true
regenerate-pom: false
partial-update: true
model-override-setter-from-superclass: true
use-default-http-status-code-to-exception-type-mapping: true
generate-sync-async-clients: true
generate-client-as-impl: true
models-subpackage: implementation.models
generate-client-interfaces: false
generate-builder-per-client: true
add-context-parameter: true
generate-tests: true
artifact-id: azure-ai-metricsadvisor
low-level-client: true
sync-methods: all
generate-samples: true
license-header: MICROSOFT_MIT_SMALL
client-logger: true
namespace: com.azure.ai.metricsadvisor
context-client-method-parameter: true
azure-arm: false
service-versions:
- '1.0'

security: AADToken
security-scopes: https://cognitiveservices.azure.com/.default
```
