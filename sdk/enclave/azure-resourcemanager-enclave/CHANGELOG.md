# Release History

## 1.1.0-beta.1 (2026-11-20)

- Azure Resource Manager Azure Enclave client library for Java. This package contains Microsoft Azure SDK for Azure Enclave Management SDK. Microsoft Mission Resource Provider management API. Package api-version 2026-10-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.EnclaveVirtualNetworkModel` was removed

#### `models.EnclaveAddressSpacesModel` was removed

#### `models.CheckAddressSpaceAvailabilityRequest` was modified

* `withEnclaveVirtualNetwork(models.EnclaveVirtualNetworkModel)` was removed
* `enclaveVirtualNetwork()` was removed

#### `models.VirtualEnclavePatchProperties` was modified

* `withEnclaveVirtualNetwork(models.EnclaveVirtualNetworkModel)` was removed
* `enclaveVirtualNetwork()` was removed

#### `models.VirtualEnclaveProperties` was modified

* `enclaveVirtualNetwork()` was removed
* `withEnclaveVirtualNetwork(models.EnclaveVirtualNetworkModel)` was removed
* `enclaveAddressSpaces()` was removed

### Features Added

* `models.SyncDestinationStatus` was added

* `models.SubnetPurpose` was added

* `models.AddressSpaceOwnership` was added

* `models.RuleDirection` was added

* `models.AddressSpaceEntry` was added

* `models.ComputedAddressSpaceEntry` was added

* `models.SyncStatus` was added

* `models.SubnetJoinPolicyMode` was added

* `models.SyncPerformedBy` was added

#### `models.CommunityProperties` was modified

* `subnetJoinPolicyMode()` was added
* `withSubnetJoinPolicyMode(models.SubnetJoinPolicyMode)` was added

#### `models.CheckAddressSpaceAvailabilityRequest` was modified

* `addressSpace()` was added
* `withAddressSpace(java.lang.String)` was added
* `prefixLength()` was added
* `withPrefixLength(java.lang.Integer)` was added

#### `models.SubnetConfiguration` was modified

* `withSubnetPurpose(models.SubnetPurpose)` was added
* `subnetPurpose()` was added

#### `models.CommunityEndpointPatchProperties` was modified

* `syncTo()` was added
* `withSyncTo(java.util.List)` was added

#### `models.CommunityEndpointResource` was modified

* `sync(com.azure.core.util.Context)` was added
* `sync()` was added

#### `models.DiagnosticDestination` was modified

* `UNSPECIFIED` was added

#### `models.CommunityPatchProperties` was modified

* `subnetJoinPolicyMode()` was added
* `withSubnetJoinPolicyMode(models.SubnetJoinPolicyMode)` was added

#### `models.VirtualEnclavePatchProperties` was modified

* `withSubnetConfigurations(java.util.List)` was added
* `withAddressSpaces(java.util.List)` was added
* `subnetConfigurations()` was added
* `addressSpaces()` was added

#### `models.ServiceIdentifier` was modified

* `AZURE_LOCAL` was added

#### `models.CommunityEndpoints` was modified

* `sync(java.lang.String,java.lang.String,java.lang.String)` was added
* `sync(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.EnclaveConnectionState` was modified

* `DEGRADED` was added
* `RECONCILING` was added

#### `models.VirtualEnclaveProperties` was modified

* `networkName()` was added
* `subnetConfigurations()` was added
* `computedAddressSpaces()` was added
* `withAllowSubnetCommunication(java.lang.Boolean)` was added
* `withAddressSpaces(java.util.List)` was added
* `withSubnetConfigurations(java.util.List)` was added
* `allowSubnetCommunication()` was added
* `addressSpaces()` was added

#### `models.CommunityEndpointDestinationRule` was modified

* `direction()` was added
* `withDirection(models.RuleDirection)` was added

#### `models.CommunityEndpointProperties` was modified

* `withSyncTo(java.util.List)` was added
* `syncPerformedBy()` was added
* `syncDestinationStatuses()` was added
* `syncTo()` was added
* `syncFrom()` was added
* `syncStatus()` was added

## 1.0.0 (2026-08-27)

- Azure Resource Manager Azure Enclave client library for Java. This package contains Microsoft Azure SDK for Azure Enclave Management SDK. Microsoft Mission Resource Provider management API. Package api-version 2026-04-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Other Changes

- First stable (GA) release of the `azure-resourcemanager-enclave` library, targeting API version `2026-04-01`. The API surface is unchanged from `1.0.0-beta.1`; this release promotes the library to a stable, generally available version.

## 1.0.0-beta.1 (2026-07-17)

- Azure Resource Manager Azure Enclave client library for Java. This package contains Microsoft Azure SDK for Azure Enclave Management SDK. Microsoft Mission Resource Provider management API. Package api-version 2026-03-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-enclave Java SDK.
