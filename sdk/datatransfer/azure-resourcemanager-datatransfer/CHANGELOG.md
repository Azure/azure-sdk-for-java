# Release History

## 1.0.0-beta.2 (2026-11-06)

- Azure Resource Manager Data Transfer client library for Java. This package contains Microsoft Azure SDK for Data Transfer Management SDK.  Package api-version 2026-06-29-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.MessagingTransferFlow$DefinitionStages` was added

* `models.MessagingTransferFlow$Update` was added

* `models.LocationMapping` was added

* `models.MessagingTransferFlow` was added

* `models.TransferFlowIdBody` was added

* `models.StorageTransferFlow` was added

* `models.StorageTransferFlowsPatch` was added

* `models.TransferFlowSummary` was added

* `models.ListTransferFlowsRequestBody` was added

* `models.MessagingTransferFlow$UpdateStages` was added

* `models.TransferFlowRequestBody` was added

* `models.StorageTransferFlows` was added

* `models.StorageTransferFlow$Definition` was added

* `models.StorageTransferFlow$Update` was added

* `models.CommonTransferFlowStatus` was added

* `models.StorageTransferFlow$DefinitionStages` was added

* `models.StorageTransferFlowProperties` was added

* `models.MessagingTransferFlow$Definition` was added

* `models.StorageTransferFlow$UpdateStages` was added

* `models.MessagingTransferFlowProperties` was added

* `models.TransferFlowResponseBody` was added

* `models.ListTransferFlowsByPipelineRequestBody` was added

* `models.TransferRole` was added

* `models.MessagingTransferFlowsPatch` was added

* `models.MessagingTransferFlows` was added

#### `models.DirectionSettings` was modified

* `messagingLocationMappings()` was added
* `storageLocationMappings()` was added

#### `models.TargetType` was modified

* `STORAGE_TRANSFER_FLOW` was added
* `MESSAGING_TRANSFER_FLOW` was added

#### `models.Pipeline` was modified

* `approveTransferFlow(models.TransferFlowRequestBody,com.azure.core.util.Context)` was added
* `rejectTransferFlow(models.TransferFlowRequestBody,com.azure.core.util.Context)` was added
* `listTransferFlows()` was added
* `rejectTransferFlow(models.TransferFlowRequestBody)` was added
* `listTransferFlows(models.ListTransferFlowsByPipelineRequestBody,com.azure.core.util.Context)` was added
* `approveTransferFlow(models.TransferFlowRequestBody)` was added

#### `DataTransferManager` was modified

* `messagingTransferFlows()` was added
* `storageTransferFlows()` was added

#### `models.Pipelines` was modified

* `rejectTransferFlow(java.lang.String,java.lang.String,models.TransferFlowRequestBody,com.azure.core.util.Context)` was added
* `listTransferFlows(java.lang.String,java.lang.String)` was added
* `approveTransferFlow(java.lang.String,java.lang.String,models.TransferFlowRequestBody,com.azure.core.util.Context)` was added
* `rejectTransferFlow(java.lang.String,java.lang.String,models.TransferFlowRequestBody)` was added
* `approveTransferFlow(java.lang.String,java.lang.String,models.TransferFlowRequestBody)` was added
* `listTransferFlows(java.lang.String,java.lang.String,models.ListTransferFlowsByPipelineRequestBody,com.azure.core.util.Context)` was added

## 1.0.0-beta.1 (2026-10-23)

- Azure Resource Manager Data Transfer client library for Java. This package contains Microsoft Azure SDK for Data Transfer Management SDK.  Package api-version 2026-02-06-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-datatransfer Java SDK.

