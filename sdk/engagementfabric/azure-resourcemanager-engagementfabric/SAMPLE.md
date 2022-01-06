# Code snippets and samples


## Accounts

- [CreateOrUpdate](#accounts_createorupdate)
- [Delete](#accounts_delete)
- [GetByResourceGroup](#accounts_getbyresourcegroup)
- [List](#accounts_list)
- [ListByResourceGroup](#accounts_listbyresourcegroup)
- [ListChannelTypes](#accounts_listchanneltypes)
- [ListKeys](#accounts_listkeys)
- [RegenerateKey](#accounts_regeneratekey)
- [Update](#accounts_update)

## Channels

- [CreateOrUpdate](#channels_createorupdate)
- [Delete](#channels_delete)
- [Get](#channels_get)
- [ListByAccount](#channels_listbyaccount)

## Operations

- [List](#operations_list)

## ResourceProvider

- [CheckNameAvailability](#resourceprovider_checknameavailability)

## SKUs

- [List](#skus_list)
### Accounts_CreateOrUpdate

```java
import com.azure.resourcemanager.engagementfabric.models.Sku;

/** Samples for Accounts CreateOrUpdate. */
public final class AccountsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/engagementfabric/resource-manager/Microsoft.EngagementFabric/preview/2018-09-01/examples/AccountsCreateOrUpdateExample.json
     */
    /**
     * Sample code: AccountsCreateOrUpdateExample.
     *
     * @param manager Entry point to EngagementFabricManager.
     */
    public static void accountsCreateOrUpdateExample(
        com.azure.resourcemanager.engagementfabric.EngagementFabricManager manager) {
        manager
            .accounts()
            .define("ExampleAccount")
            .withRegion("WestUS")
            .withExistingResourceGroup("ExampleRg")
            .withSku(new Sku().withName("B1"))
            .create();
    }
}
```

### Accounts_Delete

```java
import com.azure.core.util.Context;

/** Samples for Accounts Delete. */
public final class AccountsDeleteSamples {
    /*
     * x-ms-original-file: specification/engagementfabric/resource-manager/Microsoft.EngagementFabric/preview/2018-09-01/examples/AccountsDeleteExample.json
     */
    /**
     * Sample code: AccountsDeleteExample.
     *
     * @param manager Entry point to EngagementFabricManager.
     */
    public static void accountsDeleteExample(
        com.azure.resourcemanager.engagementfabric.EngagementFabricManager manager) {
        manager.accounts().deleteWithResponse("ExampleRg", "ExampleAccount", Context.NONE);
    }
}
```

### Accounts_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Accounts GetByResourceGroup. */
public final class AccountsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/engagementfabric/resource-manager/Microsoft.EngagementFabric/preview/2018-09-01/examples/AccountsGetExample.json
     */
    /**
     * Sample code: AccountsGetExample.
     *
     * @param manager Entry point to EngagementFabricManager.
     */
    public static void accountsGetExample(com.azure.resourcemanager.engagementfabric.EngagementFabricManager manager) {
        manager.accounts().getByResourceGroupWithResponse("ExampleRg", "ExampleAccount", Context.NONE);
    }
}
```

### Accounts_List

```java
import com.azure.core.util.Context;

/** Samples for Accounts List. */
public final class AccountsListSamples {
    /*
     * x-ms-original-file: specification/engagementfabric/resource-manager/Microsoft.EngagementFabric/preview/2018-09-01/examples/AccountsListExample.json
     */
    /**
     * Sample code: AccountsListExample.
     *
     * @param manager Entry point to EngagementFabricManager.
     */
    public static void accountsListExample(com.azure.resourcemanager.engagementfabric.EngagementFabricManager manager) {
        manager.accounts().list(Context.NONE);
    }
}
```

### Accounts_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Accounts ListByResourceGroup. */
public final class AccountsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/engagementfabric/resource-manager/Microsoft.EngagementFabric/preview/2018-09-01/examples/AccountsListByResourceGroupExample.json
     */
    /**
     * Sample code: AccountsListByResourceGroupExample.
     *
     * @param manager Entry point to EngagementFabricManager.
     */
    public static void accountsListByResourceGroupExample(
        com.azure.resourcemanager.engagementfabric.EngagementFabricManager manager) {
        manager.accounts().listByResourceGroup("ExampleRg", Context.NONE);
    }
}
```

### Accounts_ListChannelTypes

```java
import com.azure.core.util.Context;

/** Samples for Accounts ListChannelTypes. */
public final class AccountsListChannelTypesSamples {
    /*
     * x-ms-original-file: specification/engagementfabric/resource-manager/Microsoft.EngagementFabric/preview/2018-09-01/examples/AccountsListChannelTypesExample.json
     */
    /**
     * Sample code: AccountsListChannelTypesExample.
     *
     * @param manager Entry point to EngagementFabricManager.
     */
    public static void accountsListChannelTypesExample(
        com.azure.resourcemanager.engagementfabric.EngagementFabricManager manager) {
        manager.accounts().listChannelTypesWithResponse("ExampleRg", "ExampleAccount", Context.NONE);
    }
}
```

### Accounts_ListKeys

```java
import com.azure.core.util.Context;

/** Samples for Accounts ListKeys. */
public final class AccountsListKeysSamples {
    /*
     * x-ms-original-file: specification/engagementfabric/resource-manager/Microsoft.EngagementFabric/preview/2018-09-01/examples/AccountsListKeysExample.json
     */
    /**
     * Sample code: AccountsListKeysExample.
     *
     * @param manager Entry point to EngagementFabricManager.
     */
    public static void accountsListKeysExample(
        com.azure.resourcemanager.engagementfabric.EngagementFabricManager manager) {
        manager.accounts().listKeys("ExampleRg", "ExampleAccount", Context.NONE);
    }
}
```

### Accounts_RegenerateKey

```java
import com.azure.core.util.Context;

/** Samples for Accounts RegenerateKey. */
public final class AccountsRegenerateKeySamples {
    /*
     * x-ms-original-file: specification/engagementfabric/resource-manager/Microsoft.EngagementFabric/preview/2018-09-01/examples/AccountsRegenerateKeyExample.json
     */
    /**
     * Sample code: AccountsRegenerateKeyExample.
     *
     * @param manager Entry point to EngagementFabricManager.
     */
    public static void accountsRegenerateKeyExample(
        com.azure.resourcemanager.engagementfabric.EngagementFabricManager manager) {
        manager.accounts().regenerateKeyWithResponse("ExampleRg", "ExampleAccount", null, Context.NONE);
    }
}
```

### Accounts_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.engagementfabric.models.Account;
import java.util.HashMap;
import java.util.Map;

/** Samples for Accounts Update. */
public final class AccountsUpdateSamples {
    /*
     * x-ms-original-file: specification/engagementfabric/resource-manager/Microsoft.EngagementFabric/preview/2018-09-01/examples/AccountsUpdateExample.json
     */
    /**
     * Sample code: AccountsUpdateExample.
     *
     * @param manager Entry point to EngagementFabricManager.
     */
    public static void accountsUpdateExample(
        com.azure.resourcemanager.engagementfabric.EngagementFabricManager manager) {
        Account resource =
            manager.accounts().getByResourceGroupWithResponse("ExampleRg", "ExampleAccount", Context.NONE).getValue();
        resource.update().withTags(mapOf("tagName", "tagValue")).apply();
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

### Channels_CreateOrUpdate

```java
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Channels CreateOrUpdate. */
public final class ChannelsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/engagementfabric/resource-manager/Microsoft.EngagementFabric/preview/2018-09-01/examples/ChannelsCreateOrUpdateExample.json
     */
    /**
     * Sample code: ChannelsCreateOrUpdateExample.
     *
     * @param manager Entry point to EngagementFabricManager.
     */
    public static void channelsCreateOrUpdateExample(
        com.azure.resourcemanager.engagementfabric.EngagementFabricManager manager) {
        manager
            .channels()
            .define("ExampleChannel")
            .withExistingAccount("ExampleRg", "ExampleAccount")
            .withChannelType("MockChannel")
            .withChannelFunctions(Arrays.asList("MockFunction1", "MockFunction2"))
            .withCredentials(mapOf("AppId", "exampleApp", "AppKey", "exampleAppKey"))
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

### Channels_Delete

```java
import com.azure.core.util.Context;

/** Samples for Channels Delete. */
public final class ChannelsDeleteSamples {
    /*
     * x-ms-original-file: specification/engagementfabric/resource-manager/Microsoft.EngagementFabric/preview/2018-09-01/examples/ChannelsDeleteExample.json
     */
    /**
     * Sample code: ChannelsDeleteExample.
     *
     * @param manager Entry point to EngagementFabricManager.
     */
    public static void channelsDeleteExample(
        com.azure.resourcemanager.engagementfabric.EngagementFabricManager manager) {
        manager.channels().deleteWithResponse("ExampleRg", "ExampleAccount", "ExampleChannel", Context.NONE);
    }
}
```

### Channels_Get

```java
import com.azure.core.util.Context;

/** Samples for Channels Get. */
public final class ChannelsGetSamples {
    /*
     * x-ms-original-file: specification/engagementfabric/resource-manager/Microsoft.EngagementFabric/preview/2018-09-01/examples/ChannelsGetExample.json
     */
    /**
     * Sample code: ChannelsGetExample.
     *
     * @param manager Entry point to EngagementFabricManager.
     */
    public static void channelsGetExample(com.azure.resourcemanager.engagementfabric.EngagementFabricManager manager) {
        manager.channels().getWithResponse("ExampleRg", "ExampleAccount", "ExampleChannel", Context.NONE);
    }
}
```

### Channels_ListByAccount

```java
import com.azure.core.util.Context;

/** Samples for Channels ListByAccount. */
public final class ChannelsListByAccountSamples {
    /*
     * x-ms-original-file: specification/engagementfabric/resource-manager/Microsoft.EngagementFabric/preview/2018-09-01/examples/ChannelsListExample.json
     */
    /**
     * Sample code: ChannelsListExample.
     *
     * @param manager Entry point to EngagementFabricManager.
     */
    public static void channelsListExample(com.azure.resourcemanager.engagementfabric.EngagementFabricManager manager) {
        manager.channels().listByAccount("ExampleRg", "ExampleAccount", Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/engagementfabric/resource-manager/Microsoft.EngagementFabric/preview/2018-09-01/examples/OperationsListExample.json
     */
    /**
     * Sample code: OperationsListExample.
     *
     * @param manager Entry point to EngagementFabricManager.
     */
    public static void operationsListExample(
        com.azure.resourcemanager.engagementfabric.EngagementFabricManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### ResourceProvider_CheckNameAvailability

```java
import com.azure.core.util.Context;

/** Samples for ResourceProvider CheckNameAvailability. */
public final class ResourceProviderCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/engagementfabric/resource-manager/Microsoft.EngagementFabric/preview/2018-09-01/examples/CheckNameAvailabilityExample.json
     */
    /**
     * Sample code: CheckNameAvailabilityExample.
     *
     * @param manager Entry point to EngagementFabricManager.
     */
    public static void checkNameAvailabilityExample(
        com.azure.resourcemanager.engagementfabric.EngagementFabricManager manager) {
        manager.resourceProviders().checkNameAvailabilityWithResponse(null, null, Context.NONE);
    }
}
```

### SKUs_List

```java
import com.azure.core.util.Context;

/** Samples for SKUs List. */
public final class SKUsListSamples {
    /*
     * x-ms-original-file: specification/engagementfabric/resource-manager/Microsoft.EngagementFabric/preview/2018-09-01/examples/SKUsListExample.json
     */
    /**
     * Sample code: SKUsListExample.
     *
     * @param manager Entry point to EngagementFabricManager.
     */
    public static void sKUsListExample(com.azure.resourcemanager.engagementfabric.EngagementFabricManager manager) {
        manager.sKUs().list(Context.NONE);
    }
}
```

