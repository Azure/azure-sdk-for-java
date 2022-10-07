# Code snippets and samples


## DocumentProcessor

- [Create](#documentprocessor_create)
- [Delete](#documentprocessor_delete)
- [GetByResourceGroup](#documentprocessor_getbyresourcegroup)
- [List](#documentprocessor_list)
- [ListByResourceGroup](#documentprocessor_listbyresourcegroup)
- [Update](#documentprocessor_update)

## Operations

- [List](#operations_list)
### DocumentProcessor_Create

```java
import com.azure.resourcemanager.syntex.models.DocumentProcessorProperties;
import java.util.HashMap;
import java.util.Map;

/** Samples for DocumentProcessor Create. */
public final class DocumentProcessorCreateSamples {
    /*
     * x-ms-original-file: specification/syntex/resource-manager/Microsoft.Syntex/preview/2022-09-15-preview/examples/DocumentProcessor_Create.json
     */
    /**
     * Sample code: DocumentProcessor_Create.
     *
     * @param manager Entry point to SyntexManager.
     */
    public static void documentProcessorCreate(com.azure.resourcemanager.syntex.SyntexManager manager) {
        manager
            .documentProcessors()
            .define("myprocessor")
            .withRegion("westus")
            .withExistingResourceGroup("mygroup")
            .withTags(mapOf("additionalProp1", "string1", "additionalProp2", "string2", "additionalProp3", "string3"))
            .withProperties(
                new DocumentProcessorProperties()
                    .withSpoTenantUrl("https://test123.sharepoint.com")
                    .withSpoTenantId("e9bb744b-9558-4dc6-9e50-a3297e3332fa"))
            .create();
    }

    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
```

### DocumentProcessor_Delete

```java
import com.azure.core.util.Context;

/** Samples for DocumentProcessor Delete. */
public final class DocumentProcessorDeleteSamples {
    /*
     * x-ms-original-file: specification/syntex/resource-manager/Microsoft.Syntex/preview/2022-09-15-preview/examples/DocumentProcessor_Delete.json
     */
    /**
     * Sample code: DocumentProcessor_Delete.
     *
     * @param manager Entry point to SyntexManager.
     */
    public static void documentProcessorDelete(com.azure.resourcemanager.syntex.SyntexManager manager) {
        manager.documentProcessors().deleteByResourceGroupWithResponse("mygroup", "myprocessor", Context.NONE);
    }
}
```

### DocumentProcessor_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for DocumentProcessor GetByResourceGroup. */
public final class DocumentProcessorGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/syntex/resource-manager/Microsoft.Syntex/preview/2022-09-15-preview/examples/DocumentProcessor_Get.json
     */
    /**
     * Sample code: DocumentProcessor_Get.
     *
     * @param manager Entry point to SyntexManager.
     */
    public static void documentProcessorGet(com.azure.resourcemanager.syntex.SyntexManager manager) {
        manager.documentProcessors().getByResourceGroupWithResponse("mygroup", "myprocessor", Context.NONE);
    }
}
```

### DocumentProcessor_List

```java
import com.azure.core.util.Context;

/** Samples for DocumentProcessor List. */
public final class DocumentProcessorListSamples {
    /*
     * x-ms-original-file: specification/syntex/resource-manager/Microsoft.Syntex/preview/2022-09-15-preview/examples/DocumentProcessor_List.json
     */
    /**
     * Sample code: DocumentProcessor_List.
     *
     * @param manager Entry point to SyntexManager.
     */
    public static void documentProcessorList(com.azure.resourcemanager.syntex.SyntexManager manager) {
        manager.documentProcessors().list(Context.NONE);
    }
}
```

### DocumentProcessor_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for DocumentProcessor ListByResourceGroup. */
public final class DocumentProcessorListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/syntex/resource-manager/Microsoft.Syntex/preview/2022-09-15-preview/examples/DocumentProcessor_ListByResourceGroup.json
     */
    /**
     * Sample code: DocumentProcessor_ListByResourceGroup.
     *
     * @param manager Entry point to SyntexManager.
     */
    public static void documentProcessorListByResourceGroup(com.azure.resourcemanager.syntex.SyntexManager manager) {
        manager.documentProcessors().listByResourceGroup("mygroup", Context.NONE);
    }
}
```

### DocumentProcessor_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.syntex.models.DocumentProcessorResource;
import java.util.HashMap;
import java.util.Map;

/** Samples for DocumentProcessor Update. */
public final class DocumentProcessorUpdateSamples {
    /*
     * x-ms-original-file: specification/syntex/resource-manager/Microsoft.Syntex/preview/2022-09-15-preview/examples/DocumentProcessor_Update.json
     */
    /**
     * Sample code: DocumentProcessor_Update.
     *
     * @param manager Entry point to SyntexManager.
     */
    public static void documentProcessorUpdate(com.azure.resourcemanager.syntex.SyntexManager manager) {
        DocumentProcessorResource resource =
            manager
                .documentProcessors()
                .getByResourceGroupWithResponse("mygroup", "myprocessor", Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("additionalProp1", "string4", "additionalProp2", "string5", "additionalProp3", "string6"))
            .withSpoTenantUrl("https://test123.sharepoint.com")
            .withSpoTenantId("e9bb744b-9558-4dc6-9e50-a3297e3332fa")
            .apply();
    }

    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/syntex/resource-manager/Microsoft.Syntex/preview/2022-09-15-preview/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to SyntexManager.
     */
    public static void operationsList(com.azure.resourcemanager.syntex.SyntexManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

