# Release History

## 1.0.0 (2026-03-06)

- Azure Resource Manager ManagementGroups client library for Java. This package contains Microsoft Azure SDK for ManagementGroups Management SDK. The Azure Management Groups API enables consolidation of multiple
subscriptions/resources into an organizational hierarchy and centrally
manage access control, policies, alerting and reporting for those resources. Package api-version 2023-04-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.OperationDisplayProperties` was removed

#### `models.AzureAsyncOperationResults` was removed

#### `models.ListSubscriptionUnderManagementGroup` was removed

#### `models.ManagementGroupListResult` was removed

#### `models.EntityHierarchyItem` was removed

#### `models.EntityListResult` was removed

#### `models.DescendantListResult` was removed

#### `models.OperationListResult` was removed

#### `ManagementGroupsManager` was modified

* `fluent.ManagementGroupsApi serviceClient()` -> `fluent.ManagementGroupsManagementClient serviceClient()`

#### `models.PatchManagementGroupRequest` was modified

* `validate()` was removed

#### `models.ManagementGroupChildInfo` was modified

* `ManagementGroupChildInfo()` was changed to private access
* `withChildren(java.util.List)` was removed
* `withId(java.lang.String)` was removed
* `withType(models.ManagementGroupChildType)` was removed
* `withName(java.lang.String)` was removed
* `withDisplayName(java.lang.String)` was removed
* `validate()` was removed

#### `models.ManagementGroupSubscriptions` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String)` was removed

#### `models.CheckNameAvailabilityRequest` was modified

* `validate()` was removed

#### `models.CreateManagementGroupChildInfo` was modified

* `validate()` was removed

#### `models.HierarchySettingsInfo` was modified

* `HierarchySettingsInfo()` was removed
* `validate()` was removed
* `java.lang.String defaultManagementGroup()` -> `java.lang.String defaultManagementGroup()`
* `withRequireAuthorizationForGroupCreation(java.lang.Boolean)` was removed
* `withTenantId(java.lang.String)` was removed
* `java.lang.String tenantId()` -> `java.lang.String tenantId()`
* `java.lang.Boolean requireAuthorizationForGroupCreation()` -> `java.lang.Boolean requireAuthorizationForGroupCreation()`
* `withDefaultManagementGroup(java.lang.String)` was removed
* `toJson(com.azure.json.JsonWriter)` was removed
* `java.lang.String id()` -> `java.lang.String id()`
* `java.lang.String name()` -> `java.lang.String name()`
* `fromJson(com.azure.json.JsonReader)` was removed
* `java.lang.String type()` -> `java.lang.String type()`

#### `models.ManagementGroupPathElement` was modified

* `ManagementGroupPathElement()` was changed to private access
* `validate()` was removed
* `withDisplayName(java.lang.String)` was removed
* `withName(java.lang.String)` was removed

#### `models.CreateManagementGroupDetails` was modified

* `validate()` was removed

#### `models.DescendantParentGroupInfo` was modified

* `DescendantParentGroupInfo()` was changed to private access
* `validate()` was removed
* `withId(java.lang.String)` was removed

#### `models.ManagementGroupDetails` was modified

* `ManagementGroupDetails()` was changed to private access
* `withUpdatedTime(java.time.OffsetDateTime)` was removed
* `validate()` was removed
* `withPath(java.util.List)` was removed
* `withManagementGroupAncestors(java.util.List)` was removed
* `withParent(models.ParentGroupInfo)` was removed
* `withManagementGroupAncestorsChain(java.util.List)` was removed
* `withUpdatedBy(java.lang.String)` was removed
* `withVersion(java.lang.Integer)` was removed

#### `models.Operation` was modified

* `models.OperationDisplayProperties display()` -> `models.OperationDisplay display()`

#### `models.EntityParentGroupInfo` was modified

* `EntityParentGroupInfo()` was changed to private access
* `withId(java.lang.String)` was removed
* `validate()` was removed

#### `models.ManagementGroups` was modified

* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `deleteByResourceGroup(java.lang.String)` was removed

#### `models.CreateOrUpdateSettingsRequest` was modified

* `validate()` was removed

#### `models.ParentGroupInfo` was modified

* `ParentGroupInfo()` was changed to private access
* `validate()` was removed
* `withDisplayName(java.lang.String)` was removed
* `withId(java.lang.String)` was removed
* `withName(java.lang.String)` was removed

#### `models.CreateParentGroupInfo` was modified

* `validate()` was removed

#### `models.CreateManagementGroupRequest` was modified

* `validate()` was removed

### Features Added

* `models.ActionType` was added

* `models.Origin` was added

* `models.OperationDisplay` was added

#### `models.SubscriptionUnderManagementGroup` was modified

* `systemData()` was added

#### `models.ManagementGroupSubscriptions` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `deleteByResourceGroup(java.lang.String,java.lang.String)` was added

#### `models.HierarchySettingsInfo` was modified

* `innerModel()` was added

#### `models.ManagementGroup` was modified

* `systemData()` was added

#### `models.HierarchySettings` was modified

* `systemData()` was added

#### `models.Operation` was modified

* `origin()` was added
* `isDataAction()` was added
* `actionType()` was added

#### `models.ManagementGroups` was modified

* `deleteByResourceGroup(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `delete(java.lang.String)` was added

## 1.0.0-beta.2 (2024-10-28)

- Azure Resource Manager ManagementGroups client library for Java. This package contains Microsoft Azure SDK for ManagementGroups Management SDK. The Azure Management Groups API enables consolidation of multiple 
subscriptions/resources into an organizational hierarchy and centrally 
manage access control, policies, alerting and reporting for those resources.
. Package tag package-2021-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.PatchManagementGroupRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplayProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EntityHierarchyItem` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `name()` was added
* `type()` was added
* `id()` was added

#### `models.ManagementGroupChildInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EntityListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ManagementGroupDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DescendantListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CheckNameAvailabilityRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CreateManagementGroupChildInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EntityParentGroupInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HierarchySettingsInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CreateOrUpdateSettingsRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ManagementGroupPathElement` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ParentGroupInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CreateManagementGroupDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CreateParentGroupInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ListSubscriptionUnderManagementGroup` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DescendantParentGroupInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CreateManagementGroupRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added
* `type()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ManagementGroupListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.0.0-beta.1 (2023-06-19)

- Azure Resource Manager ManagementGroups client library for Java. This package contains Microsoft Azure SDK for ManagementGroups Management SDK. The Azure Management Groups API enables consolidation of multiple 
subscriptions/resources into an organizational hierarchy and centrally 
manage access control, policies, alerting and reporting for those resources.
. Package tag package-2021-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
