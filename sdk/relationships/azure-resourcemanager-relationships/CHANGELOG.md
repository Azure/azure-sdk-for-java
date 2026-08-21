# Release History

## 1.0.0-beta.2 (2026-08-13)

- Azure Resource Manager relationships client library for Java. This package contains Microsoft Azure SDK for relationships Management SDK. Microsoft.Relationships Resource Provider management API. Package api-version 2026-03-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ServiceGroupMemberRelationshipProperties` was removed

#### `models.ServiceGroupMemberRelationship$Update` was modified

* `withProperties(models.ServiceGroupMemberRelationshipProperties)` was removed

#### `models.ServiceGroupMemberRelationship$Definition` was modified

* `withProperties(models.ServiceGroupMemberRelationshipProperties)` was removed

#### `models.ServiceGroupMemberRelationship` was modified

* `models.ServiceGroupMemberRelationshipProperties properties()` -> `models.ServiceGroupMemberRelationshipPropertiesV2 properties()`

### Features Added

* `models.ContainsRelationship` was added

* `models.ContainsRelationshipProperties` was added

* `models.ContainsRelationships` was added

* `models.ServiceGroupMemberRelationshipPropertiesV2` was added

#### `models.ServiceGroupMemberRelationship$Update` was modified

* `withProperties(models.ServiceGroupMemberRelationshipPropertiesV2)` was added

#### `models.ServiceGroupMemberRelationships` was modified

* `listByParent(java.lang.String)` was added
* `listByParent(java.lang.String,com.azure.core.util.Context)` was added

#### `models.DependencyOfRelationships` was modified

* `listByParent(java.lang.String)` was added
* `listByParent(java.lang.String,com.azure.core.util.Context)` was added

#### `RelationshipsManager` was modified

* `containsRelationships()` was added

#### `models.ServiceGroupMemberRelationship$Definition` was modified

* `withProperties(models.ServiceGroupMemberRelationshipPropertiesV2)` was added

## 1.0.0-beta.1 (2026-04-03)

- Azure Resource Manager relationships client library for Java. This package contains Microsoft Azure SDK for relationships Management SDK. Microsoft.Relationships Resource Provider management API. Package api-version 2023-09-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-relationships Java SDK.

