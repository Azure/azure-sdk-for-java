# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2021-11-03)

- Azure Resource Manager NotificationHubs client library for Java. This package contains Microsoft Azure SDK for NotificationHubs Management SDK. Azure NotificationHub client. Package tag package-2017-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.SharedAccessAuthorizationRuleProperties` was removed

#### `models.SharedAccessAuthorizationRuleResource$DefinitionStages` was modified

* `withProperties(models.SharedAccessAuthorizationRuleProperties)` was removed in stage 2

#### `models.SharedAccessAuthorizationRuleResource$Definition` was modified

* `withProperties(models.SharedAccessAuthorizationRuleProperties)` was removed

#### `models.SharedAccessAuthorizationRuleResource$Update` was modified

* `withProperties(models.SharedAccessAuthorizationRuleProperties)` was removed

#### `models.SharedAccessAuthorizationRuleCreateOrUpdateParameters` was modified

* `withProperties(models.SharedAccessAuthorizationRuleProperties)` was removed
* `models.SharedAccessAuthorizationRuleProperties properties()` -> `fluent.models.SharedAccessAuthorizationRuleProperties properties()`

### Features Added

#### `models.SharedAccessAuthorizationRuleResource$Definition` was modified

* `withProperties(fluent.models.SharedAccessAuthorizationRuleProperties)` was added

#### `models.SharedAccessAuthorizationRuleResource$Update` was modified

* `withProperties(fluent.models.SharedAccessAuthorizationRuleProperties)` was added

#### `NotificationHubsManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.SharedAccessAuthorizationRuleCreateOrUpdateParameters` was modified

* `withProperties(fluent.models.SharedAccessAuthorizationRuleProperties)` was added

## 1.0.0-beta.1 (2021-04-20)

- Azure Resource Manager NotificationHubs client library for Java. This package contains Microsoft Azure SDK for NotificationHubs Management SDK. Azure NotificationHub client. Package tag package-2017-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
