# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2023-01-18)

- Azure Resource Manager PowerBIDedicated client library for Java. This package contains Microsoft Azure SDK for PowerBIDedicated Management SDK. PowerBI Dedicated Web API provides a RESTful set of web services that enables users to create, retrieve, update, and delete Power BI dedicated capacities. Package tag package-2021-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.DedicatedCapacityProperties` was removed

* `models.DedicatedCapacityMutableProperties` was removed

* `models.AutoScaleVCoreMutableProperties` was removed

* `models.AutoScaleVCoreProperties` was removed

#### `models.AutoScaleVCores` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.MetricSpecificationDimensionsItem` was added

* `models.ServiceSpecification` was added

* `models.LogSpecification` was added

* `models.MetricSpecification` was added

* `models.OperationProperties` was added

#### `PowerBIDedicatedManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.Operation` was modified

* `origin()` was added
* `properties()` was added

#### `models.OperationDisplay` was modified

* `withDescription(java.lang.String)` was added
* `description()` was added

#### `models.SkuDetailsForExistingResource` was modified

* `withResourceType(java.lang.String)` was added
* `resourceType()` was added

#### `models.DedicatedCapacity` was modified

* `resourceGroupName()` was added
* `friendlyName()` was added
* `tenantId()` was added

#### `models.AutoScaleVCore` was modified

* `resourceGroupName()` was added

#### `models.CapacitySku` was modified

* `withCapacity(java.lang.Integer)` was added
* `capacity()` was added

#### `models.AutoScaleVCores` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.DedicatedCapacityUpdateParameters` was modified

* `friendlyName()` was added
* `tenantId()` was added

#### `PowerBIDedicatedManager$Configurable` was modified

* `withScope(java.lang.String)` was added
* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

## 1.0.0-beta.1 (2021-04-20)

- Azure Resource Manager PowerBIDedicated client library for Java. This package contains Microsoft Azure SDK for PowerBIDedicated Management SDK. PowerBI Dedicated Web API provides a RESTful set of web services that enables users to create, retrieve, update, and delete Power BI dedicated capacities. Package tag package-2021-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
