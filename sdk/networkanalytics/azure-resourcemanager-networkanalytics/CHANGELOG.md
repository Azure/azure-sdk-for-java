# Release History

## 1.1.0 (2024-12-13)

- Azure Resource Manager Network Analytics client library for Java. This package contains Microsoft Azure SDK for Network Analytics Management SDK.  Package tag package-2023-11-15. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

## 1.0.0 (2024-01-24)

- Azure Resource Manager Network Analytics client library for Java. This package contains Microsoft Azure SDK for Network Analytics Management SDK.  Package tag package-2023-11-15. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.1 (2023-11-28)

- Azure Resource Manager Network Analytics client library for Java. This package contains Microsoft Azure SDK for Network Analytics Management SDK.  Package tag package-2023-11-15. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
