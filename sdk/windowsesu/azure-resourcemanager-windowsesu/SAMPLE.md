# Code snippets and samples


## MultipleActivationKeys

- [Create](#multipleactivationkeys_create)
- [Delete](#multipleactivationkeys_delete)
- [GetByResourceGroup](#multipleactivationkeys_getbyresourcegroup)
- [List](#multipleactivationkeys_list)
- [ListByResourceGroup](#multipleactivationkeys_listbyresourcegroup)
- [Update](#multipleactivationkeys_update)

## Operations

- [List](#operations_list)
### MultipleActivationKeys_Create

```java
import com.azure.resourcemanager.windowsesu.models.OsType;
import com.azure.resourcemanager.windowsesu.models.SupportType;

/** Samples for MultipleActivationKeys Create. */
public final class MultipleActivationKeysCreateSamples {
    /*
     * x-ms-original-file: specification/windowsesu/resource-manager/Microsoft.WindowsESU/preview/2019-09-16-preview/examples/CreateMultipleActivationKey.json
     */
    /**
     * Sample code: CreateMultipleActivationKey.
     *
     * @param manager Entry point to WindowsesuManager.
     */
    public static void createMultipleActivationKey(com.azure.resourcemanager.windowsesu.WindowsesuManager manager) {
        manager
            .multipleActivationKeys()
            .define("server08-key-2019")
            .withRegion("East US")
            .withExistingResourceGroup("testgr1")
            .withOsType(OsType.WINDOWS_SERVER2008)
            .withSupportType(SupportType.SUPPLEMENTAL_SERVICING)
            .withInstalledServerNumber(100)
            .withAgreementNumber("1a2b45ag")
            .withIsEligible(true)
            .create();
    }
}
```

### MultipleActivationKeys_Delete

```java
import com.azure.core.util.Context;

/** Samples for MultipleActivationKeys Delete. */
public final class MultipleActivationKeysDeleteSamples {
    /*
     * x-ms-original-file: specification/windowsesu/resource-manager/Microsoft.WindowsESU/preview/2019-09-16-preview/examples/DeleteMultipleActivationKey.json
     */
    /**
     * Sample code: DeleteMultipleActivationKey.
     *
     * @param manager Entry point to WindowsesuManager.
     */
    public static void deleteMultipleActivationKey(com.azure.resourcemanager.windowsesu.WindowsesuManager manager) {
        manager.multipleActivationKeys().deleteWithResponse("testgr1", "server08-key-2019", Context.NONE);
    }
}
```

### MultipleActivationKeys_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for MultipleActivationKeys GetByResourceGroup. */
public final class MultipleActivationKeysGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/windowsesu/resource-manager/Microsoft.WindowsESU/preview/2019-09-16-preview/examples/GetMultipleActivationKey.json
     */
    /**
     * Sample code: GetMultipleActivationKey.
     *
     * @param manager Entry point to WindowsesuManager.
     */
    public static void getMultipleActivationKey(com.azure.resourcemanager.windowsesu.WindowsesuManager manager) {
        manager.multipleActivationKeys().getByResourceGroupWithResponse("testgr1", "server08-key-2019", Context.NONE);
    }
}
```

### MultipleActivationKeys_List

```java
import com.azure.core.util.Context;

/** Samples for MultipleActivationKeys List. */
public final class MultipleActivationKeysListSamples {
    /*
     * x-ms-original-file: specification/windowsesu/resource-manager/Microsoft.WindowsESU/preview/2019-09-16-preview/examples/ListMultipleActivationKeys.json
     */
    /**
     * Sample code: ListMultipleActivationKeys.
     *
     * @param manager Entry point to WindowsesuManager.
     */
    public static void listMultipleActivationKeys(com.azure.resourcemanager.windowsesu.WindowsesuManager manager) {
        manager.multipleActivationKeys().list(Context.NONE);
    }
}
```

### MultipleActivationKeys_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for MultipleActivationKeys ListByResourceGroup. */
public final class MultipleActivationKeysListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/windowsesu/resource-manager/Microsoft.WindowsESU/preview/2019-09-16-preview/examples/ListMultipleActivationKeysByResourceGroup.json
     */
    /**
     * Sample code: ListMultipleActivationKeys.
     *
     * @param manager Entry point to WindowsesuManager.
     */
    public static void listMultipleActivationKeys(com.azure.resourcemanager.windowsesu.WindowsesuManager manager) {
        manager.multipleActivationKeys().listByResourceGroup("testrg1", Context.NONE);
    }
}
```

### MultipleActivationKeys_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.windowsesu.models.MultipleActivationKey;
import java.util.HashMap;
import java.util.Map;

/** Samples for MultipleActivationKeys Update. */
public final class MultipleActivationKeysUpdateSamples {
    /*
     * x-ms-original-file: specification/windowsesu/resource-manager/Microsoft.WindowsESU/preview/2019-09-16-preview/examples/UpdateMultipleActivationKey.json
     */
    /**
     * Sample code: UpdateMultipleActivationKey.
     *
     * @param manager Entry point to WindowsesuManager.
     */
    public static void updateMultipleActivationKey(com.azure.resourcemanager.windowsesu.WindowsesuManager manager) {
        MultipleActivationKey resource =
            manager
                .multipleActivationKeys()
                .getByResourceGroupWithResponse("testgr1", "server08-key-2019", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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
     * x-ms-original-file: specification/windowsesu/resource-manager/Microsoft.WindowsESU/preview/2019-09-16-preview/examples/ListOperations.json
     */
    /**
     * Sample code: ListOperations.
     *
     * @param manager Entry point to WindowsesuManager.
     */
    public static void listOperations(com.azure.resourcemanager.windowsesu.WindowsesuManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

