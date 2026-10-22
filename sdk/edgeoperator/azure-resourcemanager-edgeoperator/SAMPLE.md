# Code snippets and samples


## Operations

- [List](#operations_list)

## SystemReadinessOperations

- [Get](#systemreadinessoperations_get)
### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2026-06-01-preview/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to EdgeOperatorManager.
     */
    public static void operationsList(com.azure.resourcemanager.edgeoperator.EdgeOperatorManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### SystemReadinessOperations_Get

```java
/**
 * Samples for SystemReadinessOperations Get.
 */
public final class SystemReadinessOperationsGetSamples {
    /*
     * x-ms-original-file: 2026-06-01-preview/SystemReadinessOperations_Get.json
     */
    /**
     * Sample code: SystemReadinessOperations_Get.
     * 
     * @param manager Entry point to EdgeOperatorManager.
     */
    public static void
        systemReadinessOperationsGet(com.azure.resourcemanager.edgeoperator.EdgeOperatorManager manager) {
        manager.systemReadinessOperations().getWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

