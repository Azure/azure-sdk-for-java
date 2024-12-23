# Release History

## 1.2.0-beta.1 (2024-12-23)

- Azure Resource Manager PaloAlto Networks Ngfw client library for Java. This package contains Microsoft Azure SDK for PaloAlto Networks Ngfw Management SDK.  Package tag package-2023-09-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

#### `models.LocalRulestacks` was modified

* `listAppIdsWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was removed
* `listCountriesWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was removed
* `listPredefinedUrlCategoriesWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was removed
* `listPredefinedUrlCategories(java.lang.String,java.lang.String)` was removed
* `listCountries(java.lang.String,java.lang.String)` was removed
* `listAppIds(java.lang.String,java.lang.String)` was removed

#### `models.PredefinedUrlCategory` was modified

* `withAction(java.lang.String)` was removed
* `java.lang.String name()` -> `java.lang.String name()`
* `validate()` was removed
* `withName(java.lang.String)` was removed
* `java.lang.String action()` -> `java.lang.String action()`

#### `models.LocalRulestackResource` was modified

* `listPredefinedUrlCategories()` was removed
* `listPredefinedUrlCategoriesWithResponse(java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was removed
* `listAppIds()` was removed
* `listCountries()` was removed
* `listAppIdsWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was removed
* `listCountriesWithResponse(java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was removed

#### `models.Country` was modified

* `java.lang.String code()` -> `java.lang.String code()`
* `withDescription(java.lang.String)` was removed
* `withCode(java.lang.String)` was removed
* `validate()` was removed
* `java.lang.String description()` -> `java.lang.String description()`

### Features Added

#### `models.PredefinedUrlCategory` was modified

* `innerModel()` was added

#### `models.Country` was modified

* `innerModel()` was added

## 1.1.0 (2023-11-15)

- Azure Resource Manager PaloAlto Networks Ngfw client library for Java. This package contains Microsoft Azure SDK for PaloAlto Networks Ngfw Management SDK.  Package tag package-2023-09-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.NetworkProfile` was modified

* `trustedRanges()` was added
* `withTrustedRanges(java.util.List)` was added

## 1.0.0 (2023-07-14)

- Azure Resource Manager PaloAlto Networks Ngfw client library for Java. This package contains Microsoft Azure SDK for PaloAlto Networks Ngfw Management SDK.  Package tag package-2022-08-29. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.1 (2023-05-04)

- Azure Resource Manager PaloAlto Networks Ngfw client library for Java. This package contains Microsoft Azure SDK for PaloAlto Networks Ngfw Management SDK.  Package tag package-2022-08-29-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
