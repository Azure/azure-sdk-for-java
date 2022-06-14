# Code snippets and samples


## Bots

- [Create](#bots_create)
- [Delete](#bots_delete)
- [GetByResourceGroup](#bots_getbyresourcegroup)
- [List](#bots_list)
- [ListByResourceGroup](#bots_listbyresourcegroup)
- [Update](#bots_update)

## Operations

- [List](#operations_list)
### Bots_Create

```java
import com.azure.resourcemanager.healthbot.models.Identity;
import com.azure.resourcemanager.healthbot.models.ResourceIdentityType;
import com.azure.resourcemanager.healthbot.models.Sku;
import com.azure.resourcemanager.healthbot.models.SkuName;
import com.azure.resourcemanager.healthbot.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/** Samples for Bots Create. */
public final class BotsCreateSamples {
    /*
     * x-ms-original-file: specification/healthbot/resource-manager/Microsoft.HealthBot/stable/2021-06-10/examples/ResourceCreationPut.json
     */
    /**
     * Sample code: BotCreate.
     *
     * @param manager Entry point to HealthbotManager.
     */
    public static void botCreate(com.azure.resourcemanager.healthbot.HealthbotManager manager) {
        manager
            .bots()
            .define("samplebotname")
            .withRegion("East US")
            .withExistingResourceGroup("healthbotClient")
            .withSku(new Sku().withName(SkuName.F0))
            .withIdentity(
                new Identity()
                    .withType(ResourceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/subscription-id/resourcegroups/myrg/providers/microsoft.managedidentity/userassignedidentities/my-mi",
                            new UserAssignedIdentity(),
                            "/subscriptions/subscription-id/resourcegroups/myrg/providers/microsoft.managedidentity/userassignedidentities/my-mi2",
                            new UserAssignedIdentity())))
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

### Bots_Delete

```java
import com.azure.core.util.Context;

/** Samples for Bots Delete. */
public final class BotsDeleteSamples {
    /*
     * x-ms-original-file: specification/healthbot/resource-manager/Microsoft.HealthBot/stable/2021-06-10/examples/ResourceDeletionDelete.json
     */
    /**
     * Sample code: BotDelete.
     *
     * @param manager Entry point to HealthbotManager.
     */
    public static void botDelete(com.azure.resourcemanager.healthbot.HealthbotManager manager) {
        manager.bots().delete("healthbotClient", "samplebotname", Context.NONE);
    }
}
```

### Bots_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Bots GetByResourceGroup. */
public final class BotsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/healthbot/resource-manager/Microsoft.HealthBot/stable/2021-06-10/examples/ResourceInfoGet.json
     */
    /**
     * Sample code: ResourceInfoGet.
     *
     * @param manager Entry point to HealthbotManager.
     */
    public static void resourceInfoGet(com.azure.resourcemanager.healthbot.HealthbotManager manager) {
        manager.bots().getByResourceGroupWithResponse("healthbotClient", "samplebotname", Context.NONE);
    }
}
```

### Bots_List

```java
import com.azure.core.util.Context;

/** Samples for Bots List. */
public final class BotsListSamples {
    /*
     * x-ms-original-file: specification/healthbot/resource-manager/Microsoft.HealthBot/stable/2021-06-10/examples/ListBotsBySubscription.json
     */
    /**
     * Sample code: List Bots by Subscription.
     *
     * @param manager Entry point to HealthbotManager.
     */
    public static void listBotsBySubscription(com.azure.resourcemanager.healthbot.HealthbotManager manager) {
        manager.bots().list(Context.NONE);
    }
}
```

### Bots_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Bots ListByResourceGroup. */
public final class BotsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/healthbot/resource-manager/Microsoft.HealthBot/stable/2021-06-10/examples/ListBotsByResourceGroup.json
     */
    /**
     * Sample code: List Bots by Resource Group.
     *
     * @param manager Entry point to HealthbotManager.
     */
    public static void listBotsByResourceGroup(com.azure.resourcemanager.healthbot.HealthbotManager manager) {
        manager.bots().listByResourceGroup("OneResourceGroupName", Context.NONE);
    }
}
```

### Bots_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.healthbot.models.HealthBot;
import com.azure.resourcemanager.healthbot.models.Sku;
import com.azure.resourcemanager.healthbot.models.SkuName;

/** Samples for Bots Update. */
public final class BotsUpdateSamples {
    /*
     * x-ms-original-file: specification/healthbot/resource-manager/Microsoft.HealthBot/stable/2021-06-10/examples/ResourceUpdatePatch.json
     */
    /**
     * Sample code: BotUpdate.
     *
     * @param manager Entry point to HealthbotManager.
     */
    public static void botUpdate(com.azure.resourcemanager.healthbot.HealthbotManager manager) {
        HealthBot resource =
            manager.bots().getByResourceGroupWithResponse("healthbotClient", "samplebotname", Context.NONE).getValue();
        resource.update().withSku(new Sku().withName(SkuName.F0)).apply();
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/healthbot/resource-manager/Microsoft.HealthBot/stable/2021-06-10/examples/GetOperations.json
     */
    /**
     * Sample code: Get Operations.
     *
     * @param manager Entry point to HealthbotManager.
     */
    public static void getOperations(com.azure.resourcemanager.healthbot.HealthbotManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

