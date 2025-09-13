# Release History

## 1.1.0-beta.1 (2025-09-10)

- Azure Resource Manager Neon Postgres client library for Java. This package contains Microsoft Azure SDK for Neon Postgres Management SDK.  Package api-version 2025-06-23-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Compute$DefinitionStages` was removed

#### `models.Compute$Definition` was removed

#### `models.Compute$Update` was removed

#### `models.Compute$UpdateStages` was removed

#### `models.NeonRoles` was modified

* `get(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was removed
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `getById(java.lang.String)` was removed
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.NeonDatabases` was modified

* `get(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was removed
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `getById(java.lang.String)` was removed

#### `models.Computes` was modified

* `define(java.lang.String)` was removed
* `get(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was removed
* `getById(java.lang.String)` was removed
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was removed
* `deleteById(java.lang.String)` was removed
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `deleteWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Endpoint` was modified

* `refresh(com.azure.core.util.Context)` was removed
* `refresh()` was removed

#### `models.OrganizationResource$Update` was modified

* `withProperties(models.OrganizationProperties)` was removed

#### `models.NeonDatabase` was modified

* `refresh()` was removed
* `refresh(com.azure.core.util.Context)` was removed

#### `models.NeonRole` was modified

* `refresh(com.azure.core.util.Context)` was removed
* `refresh()` was removed

#### `models.Endpoints` was modified

* `get(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was removed
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `getById(java.lang.String)` was removed

#### `models.Compute` was modified

* `refresh(com.azure.core.util.Context)` was removed
* `refresh()` was removed
* `resourceGroupName()` was removed
* `update()` was removed

#### `models.ComputeProperties` was modified

* `withStatus(java.lang.String)` was removed
* `withEntityName(java.lang.String)` was removed
* `withMemory(java.lang.Integer)` was removed
* `withRegion(java.lang.String)` was removed
* `withAttributes(java.util.List)` was removed
* `withCpuCores(java.lang.Integer)` was removed

### Features Added

* `models.EntityType` was added

* `models.AutoscalingSize` was added

* `models.EndpointStatus` was added

* `models.PreflightCheckParameters` was added

* `models.OrganizationResourceUpdateProperties` was added

* `models.OrganizationResourceUpdate` was added

* `models.PreflightCheckResult` was added

#### `models.Branches` was modified

* `preflightWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.PreflightCheckParameters,com.azure.core.util.Context)` was added
* `preflight(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.PreflightCheckParameters)` was added

#### `models.NeonDatabaseProperties` was modified

* `withDatabaseName(java.lang.String)` was added
* `lastUpdated()` was added
* `databaseName()` was added

#### `models.EndpointProperties` was modified

* `computeName()` was added
* `withComputeName(java.lang.String)` was added
* `status()` was added
* `size()` was added
* `withSize(models.AutoscalingSize)` was added
* `withEndpointId(java.lang.String)` was added
* `endpointId()` was added
* `lastActive()` was added

#### `models.OrganizationResource$Update` was modified

* `withProperties(models.OrganizationResourceUpdateProperties)` was added

#### `models.NeonRoleProperties` was modified

* `withRoleName(java.lang.String)` was added
* `lastUpdated()` was added
* `roleName()` was added
* `owns()` was added

#### `models.Branch` was modified

* `preflight(models.PreflightCheckParameters)` was added
* `preflightWithResponse(models.PreflightCheckParameters,com.azure.core.util.Context)` was added

#### `models.BranchProperties` was modified

* `dataSize()` was added
* `branchId()` was added
* `withBranch(java.lang.String)` was added
* `computeHours()` was added
* `withBranchId(java.lang.String)` was added
* `protectedProperty()` was added
* `lastActive()` was added
* `isDefault()` was added
* `branch()` was added

## 1.0.0 (2025-04-21)

- Azure Resource Manager Neon Postgres client library for Java. This package contains Microsoft Azure SDK for Neon Postgres Management SDK. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.NeonDatabase$Definition` was added

* `models.NeonRole$DefinitionStages` was added

* `models.EndpointType` was added

* `models.NeonRoles` was added

* `models.Project$DefinitionStages` was added

* `models.NeonDatabases` was added

* `implementation.models.EndpointListResult` was added

* `implementation.models.ProjectListResult` was added

* `implementation.models.NeonRoleListResult` was added

* `models.ConnectionUriProperties` was added

* `models.PgVersion` was added

* `models.Project$Update` was added

* `models.Compute$DefinitionStages` was added

* `models.Branches` was added

* `models.Computes` was added

* `models.NeonDatabaseProperties` was added

* `models.Endpoint` was added

* `models.Endpoint$DefinitionStages` was added

* `models.Endpoint$Definition` was added

* `models.EndpointProperties` was added

* `models.ProjectProperties` was added

* `models.NeonRoleProperties` was added

* `models.Project$Definition` was added

* `models.Endpoint$Update` was added

* `models.NeonDatabase` was added

* `models.NeonDatabase$Update` was added

* `implementation.models.NeonDatabaseListResult` was added

* `implementation.models.ComputeListResult` was added

* `models.Compute$Definition` was added

* `models.Compute$Update` was added

* `models.NeonRole$Definition` was added

* `implementation.models.BranchListResult` was added

* `models.Branch` was added

* `models.NeonRole` was added

* `models.PgVersionsResult` was added

* `models.Branch$UpdateStages` was added

* `models.NeonDatabase$UpdateStages` was added

* `models.NeonRole$Update` was added

* `models.Endpoints` was added

* `models.NeonDatabase$DefinitionStages` was added

* `models.Project` was added

* `models.Compute$UpdateStages` was added

* `models.DefaultEndpointSettings` was added

* `models.Compute` was added

* `models.Branch$DefinitionStages` was added

* `models.Endpoint$UpdateStages` was added

* `models.Attributes` was added

* `models.Project$UpdateStages` was added

* `models.BranchProperties` was added

* `models.Branch$Definition` was added

* `models.Branch$Update` was added

* `models.Projects` was added

* `models.ComputeProperties` was added

* `models.NeonRole$UpdateStages` was added

#### `models.Organizations` was modified

* `getPostgresVersionsWithResponse(java.lang.String,models.PgVersion,com.azure.core.util.Context)` was added
* `getPostgresVersions(java.lang.String)` was added

#### `models.OrganizationProperties` was modified

* `withProjectProperties(models.ProjectProperties)` was added
* `projectProperties()` was added

#### `NeonPostgresManager` was modified

* `computes()` was added
* `neonDatabases()` was added
* `branches()` was added
* `projects()` was added
* `neonRoles()` was added
* `endpoints()` was added

## 1.0.0-beta.1 (2024-12-03)

- Azure Resource Manager Neon Postgres client library for Java. This package contains Microsoft Azure SDK for Neon Postgres Management SDK. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

- Initial release for the azure-resourcemanager-neonpostgres Java SDK.
