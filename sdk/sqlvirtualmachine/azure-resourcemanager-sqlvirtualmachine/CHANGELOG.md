# Release History

## 1.0.0-beta.4 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.3 (2022-09-20)

- Azure Resource Manager SqlVirtualMachine client library for Java. This package contains Microsoft Azure SDK for SqlVirtualMachine Management SDK. The SQL virtual machine management API provides a RESTful set of web APIs that interact with Azure Compute, Network & Storage services to manage your SQL Server virtual machine. The API enables users to create, delete and retrieve a SQL virtual machine, SQL virtual machine group or availability group listener. Package tag package-preview-2022-07. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.DaysOfWeek` was removed

#### `models.Schedule` was modified

* `withDayOfWeek(models.DayOfWeek)` was removed
* `models.DayOfWeek dayOfWeek()` -> `models.AssessmentDayOfWeek dayOfWeek()`

### Features Added

* `models.AutoBackupDaysOfWeek` was added

* `models.MultiSubnetIpConfiguration` was added

* `models.LeastPrivilegeMode` was added

* `models.AssessmentDayOfWeek` was added

* `models.ClusterSubnetType` was added

#### `models.Schedule` was modified

* `withDayOfWeek(models.AssessmentDayOfWeek)` was added

#### `models.SqlVirtualMachine$Definition` was modified

* `withEnableAutomaticUpgrade(java.lang.Boolean)` was added
* `withWsfcStaticIp(java.lang.String)` was added
* `withLeastPrivilegeMode(models.LeastPrivilegeMode)` was added

#### `models.SqlTempDbSettings` was modified

* `persistFolder()` was added
* `withPersistFolderPath(java.lang.String)` was added
* `persistFolderPath()` was added
* `withPersistFolder(java.lang.Boolean)` was added

#### `models.AvailabilityGroupListener` was modified

* `resourceGroupName()` was added
* `multiSubnetIpConfigurations()` was added

#### `SqlVirtualMachineManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.AgConfiguration` was modified

* `withReplicas(java.util.List)` was added

#### `models.WsfcDomainProfile` was modified

* `clusterSubnetType()` was added
* `withClusterSubnetType(models.ClusterSubnetType)` was added

#### `SqlVirtualMachineManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.SqlInstanceSettings` was modified

* `withIsIfiEnabled(java.lang.Boolean)` was added
* `withIsLpimEnabled(java.lang.Boolean)` was added
* `isIfiEnabled()` was added
* `isLpimEnabled()` was added

#### `models.SqlVirtualMachineGroup` was modified

* `resourceGroupName()` was added

#### `models.AvailabilityGroupListener$Update` was modified

* `withMultiSubnetIpConfigurations(java.util.List)` was added

#### `models.AvailabilityGroupListener$Definition` was modified

* `withMultiSubnetIpConfigurations(java.util.List)` was added

#### `models.SqlVirtualMachine` was modified

* `leastPrivilegeMode()` was added
* `resourceGroupName()` was added
* `wsfcStaticIp()` was added
* `enableAutomaticUpgrade()` was added

## 1.0.0-beta.2 (2022-02-24)

- Azure Resource Manager SqlVirtualMachine client library for Java. This package contains Microsoft Azure SDK for SqlVirtualMachine Management SDK. The SQL virtual machine management API provides a RESTful set of web APIs that interact with Azure Compute, Network & Storage services to manage your SQL Server virtual machine. The API enables users to create, delete and retrieve a SQL virtual machine, SQL virtual machine group or availability group listener. Package tag package-preview-2021-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.StorageConfigurationSettings` was modified

* `models.SqlStorageSettings sqlTempDbSettings()` -> `models.SqlTempDbSettings sqlTempDbSettings()`
* `withSqlTempDbSettings(models.SqlStorageSettings)` was removed

#### `models.SqlVirtualMachine$Definition` was modified

* `withSqlVirtualMachineGroupResourceId(java.lang.String)` was removed

#### `models.AvailabilityGroupListeners` was modified

* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.Role` was added

* `models.Commit` was added

* `models.Schedule` was added

* `models.ReadableSecondary` was added

* `models.SqlTempDbSettings` was added

* `models.Failover` was added

* `models.AssessmentSettings` was added

* `models.AgConfiguration` was added

* `models.SqlInstanceSettings` was added

* `models.DaysOfWeek` was added

* `models.AgReplica` was added

#### `models.StorageConfigurationSettings` was modified

* `withSqlTempDbSettings(models.SqlTempDbSettings)` was added
* `sqlSystemDbOnDataDisk()` was added
* `withSqlSystemDbOnDataDisk(java.lang.Boolean)` was added

#### `models.SqlVirtualMachines` was modified

* `startAssessment(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `redeploy(java.lang.String,java.lang.String)` was added
* `redeploy(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `startAssessment(java.lang.String,java.lang.String)` was added

#### `models.SqlVirtualMachine$Definition` was modified

* `withAssessmentSettings(models.AssessmentSettings)` was added

#### `models.AvailabilityGroupListener` was modified

* `availabilityGroupConfiguration()` was added
* `systemData()` was added

#### `models.ServerConfigurationsManagementSettings` was modified

* `withSqlInstanceSettings(models.SqlInstanceSettings)` was added
* `sqlInstanceSettings()` was added

#### `models.AutoBackupSettings` was modified

* `daysOfWeek()` was added
* `storageContainerName()` was added
* `withDaysOfWeek(java.util.List)` was added
* `withStorageContainerName(java.lang.String)` was added

#### `SqlVirtualMachineManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.AvailabilityGroupListeners` was modified

* `getByIdWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.SqlVirtualMachineGroup` was modified

* `systemData()` was added

#### `models.AvailabilityGroupListener$Update` was modified

* `withAvailabilityGroupConfiguration(models.AgConfiguration)` was added

#### `models.AvailabilityGroupListener$Definition` was modified

* `withAvailabilityGroupConfiguration(models.AgConfiguration)` was added

#### `models.SqlVirtualMachine` was modified

* `redeploy()` was added
* `assessmentSettings()` was added
* `startAssessment()` was added
* `systemData()` was added
* `redeploy(com.azure.core.util.Context)` was added
* `startAssessment(com.azure.core.util.Context)` was added

## 1.0.0-beta.1 (2020-12-18)

- Azure Resource Manager SqlVirtualMachine client library for Java. This package contains Microsoft Azure SDK for SqlVirtualMachine Management SDK. The SQL virtual machine management API provides a RESTful set of web APIs that interact with Azure Compute, Network & Storage services to manage your SQL Server virtual machine. The API enables users to create, delete and retrieve a SQL virtual machine, SQL virtual machine group or availability group listener. Package tag package-2017-03-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
