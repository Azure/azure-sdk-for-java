# Release History

## 1.0.0-beta.2 (2021-01-14)

- Azure Resource Manager MySql client library for Java. This package contains Microsoft Azure SDK for MySql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure MySQL resources including servers, databases, firewall rules, VNET rules, log files and configurations with new business model. Package tag package-2020-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Change

#### `models.Server$DefinitionStages` was modified

* `withProperties(models.ServerPropertiesForCreate)` was removed in stage 3
* Stage 4 was added

#### `models.Server$Definition` was modified

* `withSku(models.Sku)` was removed

#### `models.ServerForCreate` was modified

* `withSku(models.Sku)` was removed
* `sku()` was removed

### New Feature

* `models.ServerSecurityAlertPolicyListResult` was added

#### `models.ServerSecurityAlertPolicies` was modified

* `listByServer(java.lang.String,java.lang.String)` was added
* `listByServer(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Server$Definition` was modified

* `withSkus(models.Sku)` was added

#### `models.ServerForCreate` was modified

* `skus()` was added
* `withSkus(models.Sku)` was added

## 1.0.0-beta.1 (2020-12-16)

- Azure Resource Manager MySql client library for Java. This package contains Microsoft Azure SDK for MySql Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure MySQL resources including servers, databases, firewall rules, VNET rules, log files and configurations with new business model. Package tag package-2020-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).