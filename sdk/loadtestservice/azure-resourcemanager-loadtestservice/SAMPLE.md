# Code snippets and samples


## LoadTests

- [CreateOrUpdate](#loadtests_createorupdate)
- [Delete](#loadtests_delete)
- [GetByResourceGroup](#loadtests_getbyresourcegroup)
- [List](#loadtests_list)
- [ListByResourceGroup](#loadtests_listbyresourcegroup)
- [Update](#loadtests_update)

## Operations

- [List](#operations_list)
### LoadTests_CreateOrUpdate

```java
import com.azure.resourcemanager.loadtestservice.models.EncryptionProperties;
import com.azure.resourcemanager.loadtestservice.models.EncryptionPropertiesIdentity;
import com.azure.resourcemanager.loadtestservice.models.Type;
import java.util.HashMap;
import java.util.Map;

/** Samples for LoadTests CreateOrUpdate. */
public final class LoadTestsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/loadtestservice/resource-manager/Microsoft.LoadTestService/preview/2022-04-15-preview/examples/LoadTests_CreateOrUpdate.json
     */
    /**
     * Sample code: LoadTests_CreateOrUpdate.
     *
     * @param manager Entry point to LoadTestManager.
     */
    public static void loadTestsCreateOrUpdate(com.azure.resourcemanager.loadtestservice.LoadTestManager manager) {
        manager
            .loadTests()
            .define("myLoadTest")
            .withRegion("westus")
            .withExistingResourceGroup("dummyrg")
            .withTags(mapOf("Team", "Dev Exp"))
            .withDescription("This is new load test resource")
            .withEncryption(
                new EncryptionProperties()
                    .withIdentity(
                        new EncryptionPropertiesIdentity()
                            .withType(Type.USER_ASSIGNED)
                            .withResourceId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/dummyrg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1"))
                    .withKeyUrl("https://dummy.vault.azure.net/keys/dummykey1"))
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

### LoadTests_Delete

```java
import com.azure.core.util.Context;

/** Samples for LoadTests Delete. */
public final class LoadTestsDeleteSamples {
    /*
     * x-ms-original-file: specification/loadtestservice/resource-manager/Microsoft.LoadTestService/preview/2022-04-15-preview/examples/LoadTests_Delete.json
     */
    /**
     * Sample code: LoadTests_Delete.
     *
     * @param manager Entry point to LoadTestManager.
     */
    public static void loadTestsDelete(com.azure.resourcemanager.loadtestservice.LoadTestManager manager) {
        manager.loadTests().delete("dummyrg", "myLoadTest", Context.NONE);
    }
}
```

### LoadTests_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for LoadTests GetByResourceGroup. */
public final class LoadTestsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/loadtestservice/resource-manager/Microsoft.LoadTestService/preview/2022-04-15-preview/examples/LoadTests_Get.json
     */
    /**
     * Sample code: LoadTests_Get.
     *
     * @param manager Entry point to LoadTestManager.
     */
    public static void loadTestsGet(com.azure.resourcemanager.loadtestservice.LoadTestManager manager) {
        manager.loadTests().getByResourceGroupWithResponse("dummyrg", "myLoadTest", Context.NONE);
    }
}
```

### LoadTests_List

```java
import com.azure.core.util.Context;

/** Samples for LoadTests List. */
public final class LoadTestsListSamples {
    /*
     * x-ms-original-file: specification/loadtestservice/resource-manager/Microsoft.LoadTestService/preview/2022-04-15-preview/examples/LoadTests_ListBySubscription.json
     */
    /**
     * Sample code: LoadTests_ListBySubscription.
     *
     * @param manager Entry point to LoadTestManager.
     */
    public static void loadTestsListBySubscription(com.azure.resourcemanager.loadtestservice.LoadTestManager manager) {
        manager.loadTests().list(Context.NONE);
    }
}
```

### LoadTests_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for LoadTests ListByResourceGroup. */
public final class LoadTestsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/loadtestservice/resource-manager/Microsoft.LoadTestService/preview/2022-04-15-preview/examples/LoadTests_ListByResourceGroup.json
     */
    /**
     * Sample code: LoadTests_ListByResourceGroup.
     *
     * @param manager Entry point to LoadTestManager.
     */
    public static void loadTestsListByResourceGroup(com.azure.resourcemanager.loadtestservice.LoadTestManager manager) {
        manager.loadTests().listByResourceGroup("dummyrg", Context.NONE);
    }
}
```

### LoadTests_Update

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.loadtestservice.models.EncryptionProperties;
import com.azure.resourcemanager.loadtestservice.models.EncryptionPropertiesIdentity;
import com.azure.resourcemanager.loadtestservice.models.LoadTestResource;
import com.azure.resourcemanager.loadtestservice.models.Type;
import java.io.IOException;

/** Samples for LoadTests Update. */
public final class LoadTestsUpdateSamples {
    /*
     * x-ms-original-file: specification/loadtestservice/resource-manager/Microsoft.LoadTestService/preview/2022-04-15-preview/examples/LoadTests_Update.json
     */
    /**
     * Sample code: LoadTests_Update.
     *
     * @param manager Entry point to LoadTestManager.
     */
    public static void loadTestsUpdate(com.azure.resourcemanager.loadtestservice.LoadTestManager manager)
        throws IOException {
        LoadTestResource resource =
            manager.loadTests().getByResourceGroupWithResponse("dummyrg", "myLoadTest", Context.NONE).getValue();
        resource
            .update()
            .withTags(
                SerializerFactory
                    .createDefaultManagementSerializerAdapter()
                    .deserialize("{\"Division\":\"LT\",\"Team\":\"Dev Exp\"}", Object.class, SerializerEncoding.JSON))
            .withDescription("This is new load test resource")
            .withEncryption(
                new EncryptionProperties()
                    .withIdentity(new EncryptionPropertiesIdentity().withType(Type.SYSTEM_ASSIGNED))
                    .withKeyUrl("https://dummy.vault.azure.net/keys/dummykey1"))
            .apply();
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/loadtestservice/resource-manager/Microsoft.LoadTestService/preview/2022-04-15-preview/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to LoadTestManager.
     */
    public static void operationsList(com.azure.resourcemanager.loadtestservice.LoadTestManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

