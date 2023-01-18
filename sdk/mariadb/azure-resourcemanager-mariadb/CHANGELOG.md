# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2023-01-18)

- Azure Resource Manager MariaDB client library for Java. This package contains Microsoft Azure SDK for MariaDB Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure MariaDB resources including servers, databases, firewall rules, VNET rules, log files and configurations with new business model. Package tag package-2020-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.Database` was modified

* `resourceGroupName()` was added

#### `models.PrivateEndpointConnection` was modified

* `resourceGroupName()` was added

#### `models.FirewallRule` was modified

* `resourceGroupName()` was added

#### `MariaDBManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.Configuration` was modified

* `resourceGroupName()` was added

#### `MariaDBManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added
* `withScope(java.lang.String)` was added

#### `models.VirtualNetworkRule` was modified

* `resourceGroupName()` was added

#### `models.Server` was modified

* `stop(com.azure.core.util.Context)` was added
* `stop()` was added
* `resourceGroupName()` was added
* `start(com.azure.core.util.Context)` was added
* `restart(com.azure.core.util.Context)` was added
* `start()` was added
* `restart()` was added

#### `models.ServerSecurityAlertPolicy` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.1 (2021-04-16)

- Azure Resource Manager MariaDB client library for Java. This package contains Microsoft Azure SDK for MariaDB Management SDK. The Microsoft Azure management API provides create, read, update, and delete functionality for Azure MariaDB resources including servers, databases, firewall rules, VNET rules, log files and configurations with new business model. Package tag package-2020-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
