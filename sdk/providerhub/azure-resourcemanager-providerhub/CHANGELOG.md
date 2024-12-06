# Release History

## 1.2.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0 (2024-12-06)

- Azure Resource Manager ProviderHub client library for Java. This package contains Microsoft Azure SDK for ProviderHub Management SDK. Microsoft ProviderHub. Package tag package-2020-11-20. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

### Features Added

#### `models.DefaultRolloutSpecificationProviderRegistration` was modified

* `id()` was added
* `type()` was added
* `name()` was added

#### `models.ErrorInnerError` was modified

* `innerError()` was added
* `code()` was added

#### `models.CustomRolloutSpecificationProviderRegistration` was modified

* `id()` was added
* `type()` was added
* `name()` was added

## 1.0.0 (2023-02-20)

- Azure Resource Manager ProviderHub client library for Java. This package contains Microsoft Azure SDK for ProviderHub Management SDK. Microsoft ProviderHub. Package tag package-2020-11-20. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.1 (2023-02-07)

- Azure Resource Manager ProviderHub client library for Java. This package contains Microsoft Azure SDK for ProviderHub Management SDK. Microsoft ProviderHub. Package tag package-2020-11-20. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
