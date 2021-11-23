# Code snippets and samples


## Accounts

- [CheckNameAvailability](#accounts_checknameavailability)
- [CreateOrUpdate](#accounts_createorupdate)
- [Delete](#accounts_delete)
- [GetByResourceGroup](#accounts_getbyresourcegroup)
- [List](#accounts_list)
- [ListByResourceGroup](#accounts_listbyresourcegroup)
- [Update](#accounts_update)

## DataPools

- [CreateOrUpdate](#datapools_createorupdate)
- [Delete](#datapools_delete)
- [Get](#datapools_get)
- [List](#datapools_list)
- [Update](#datapools_update)

## Operations

- [List](#operations_list)
### Accounts_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.adp.models.AccountCheckNameAvailabilityParameters;
import com.azure.resourcemanager.adp.models.Type;

/** Samples for Accounts CheckNameAvailability. */
public final class AccountsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/adp/resource-manager/Microsoft.AutonomousDevelopmentPlatform/preview/2021-11-01-preview/examples/AdpAccountCheckNameAvailability.json
     */
    /**
     * Sample code: AccountCheckNameAvailability.
     *
     * @param manager Entry point to AdpManager.
     */
    public static void accountCheckNameAvailability(com.azure.resourcemanager.adp.AdpManager manager) {
        manager
            .accounts()
            .checkNameAvailabilityWithResponse(
                new AccountCheckNameAvailabilityParameters()
                    .withName("adp1")
                    .withType(Type.MICROSOFT_AUTONOMOUS_DEVELOPMENT_PLATFORM_ACCOUNTS),
                Context.NONE);
    }
}
```

### Accounts_CreateOrUpdate

```java
/** Samples for Accounts CreateOrUpdate. */
public final class AccountsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/adp/resource-manager/Microsoft.AutonomousDevelopmentPlatform/preview/2021-11-01-preview/examples/AdpAccountPut.json
     */
    /**
     * Sample code: Put account.
     *
     * @param manager Entry point to AdpManager.
     */
    public static void putAccount(com.azure.resourcemanager.adp.AdpManager manager) {
        manager.accounts().define("sampleacct").withRegion("Global").withExistingResourceGroup("adpClient").create();
    }
}
```

### Accounts_Delete

```java
import com.azure.core.util.Context;

/** Samples for Accounts Delete. */
public final class AccountsDeleteSamples {
    /*
     * x-ms-original-file: specification/adp/resource-manager/Microsoft.AutonomousDevelopmentPlatform/preview/2021-11-01-preview/examples/AdpAccountDelete.json
     */
    /**
     * Sample code: Delete account.
     *
     * @param manager Entry point to AdpManager.
     */
    public static void deleteAccount(com.azure.resourcemanager.adp.AdpManager manager) {
        manager.accounts().delete("adpClient", "sampleacct", Context.NONE);
    }
}
```

### Accounts_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Accounts GetByResourceGroup. */
public final class AccountsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/adp/resource-manager/Microsoft.AutonomousDevelopmentPlatform/preview/2021-11-01-preview/examples/AdpAccountGet.json
     */
    /**
     * Sample code: Get account.
     *
     * @param manager Entry point to AdpManager.
     */
    public static void getAccount(com.azure.resourcemanager.adp.AdpManager manager) {
        manager.accounts().getByResourceGroupWithResponse("adpClient", "sampleacct", Context.NONE);
    }
}
```

### Accounts_List

```java
import com.azure.core.util.Context;

/** Samples for Accounts List. */
public final class AccountsListSamples {
    /*
     * x-ms-original-file: specification/adp/resource-manager/Microsoft.AutonomousDevelopmentPlatform/preview/2021-11-01-preview/examples/AdpAccountsList.json
     */
    /**
     * Sample code: List accounts.
     *
     * @param manager Entry point to AdpManager.
     */
    public static void listAccounts(com.azure.resourcemanager.adp.AdpManager manager) {
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
     * x-ms-original-file: specification/adp/resource-manager/Microsoft.AutonomousDevelopmentPlatform/preview/2021-11-01-preview/examples/AdpAccountsListByResourceGroup.json
     */
    /**
     * Sample code: List accounts by resource group.
     *
     * @param manager Entry point to AdpManager.
     */
    public static void listAccountsByResourceGroup(com.azure.resourcemanager.adp.AdpManager manager) {
        manager.accounts().listByResourceGroup("adpClient", Context.NONE);
    }
}
```

### Accounts_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.adp.models.Account;

/** Samples for Accounts Update. */
public final class AccountsUpdateSamples {
    /*
     * x-ms-original-file: specification/adp/resource-manager/Microsoft.AutonomousDevelopmentPlatform/preview/2021-11-01-preview/examples/AdpAccountPatch.json
     */
    /**
     * Sample code: Patch account.
     *
     * @param manager Entry point to AdpManager.
     */
    public static void patchAccount(com.azure.resourcemanager.adp.AdpManager manager) {
        Account resource =
            manager.accounts().getByResourceGroupWithResponse("adpClient", "sampleacct", Context.NONE).getValue();
        resource.update().apply();
    }
}
```

### DataPools_CreateOrUpdate

```java
/** Samples for DataPools CreateOrUpdate. */
public final class DataPoolsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/adp/resource-manager/Microsoft.AutonomousDevelopmentPlatform/preview/2021-11-01-preview/examples/AdpDataPoolPut.json
     */
    /**
     * Sample code: Put Data Pool.
     *
     * @param manager Entry point to AdpManager.
     */
    public static void putDataPool(com.azure.resourcemanager.adp.AdpManager manager) {
        manager.dataPools().define("sampledp").withExistingAccount("adpClient", "sampleacct").create();
    }
}
```

### DataPools_Delete

```java
import com.azure.core.util.Context;

/** Samples for DataPools Delete. */
public final class DataPoolsDeleteSamples {
    /*
     * x-ms-original-file: specification/adp/resource-manager/Microsoft.AutonomousDevelopmentPlatform/preview/2021-11-01-preview/examples/AdpDataPoolDelete.json
     */
    /**
     * Sample code: Delete Data Pool.
     *
     * @param manager Entry point to AdpManager.
     */
    public static void deleteDataPool(com.azure.resourcemanager.adp.AdpManager manager) {
        manager.dataPools().delete("adpClient", "sampleacct", "sampledp", Context.NONE);
    }
}
```

### DataPools_Get

```java
import com.azure.core.util.Context;

/** Samples for DataPools Get. */
public final class DataPoolsGetSamples {
    /*
     * x-ms-original-file: specification/adp/resource-manager/Microsoft.AutonomousDevelopmentPlatform/preview/2021-11-01-preview/examples/AdpDataPoolGet.json
     */
    /**
     * Sample code: Get Data Pool.
     *
     * @param manager Entry point to AdpManager.
     */
    public static void getDataPool(com.azure.resourcemanager.adp.AdpManager manager) {
        manager.dataPools().getWithResponse("adpClient", "sampleacct", "sampledp", Context.NONE);
    }
}
```

### DataPools_List

```java
import com.azure.core.util.Context;

/** Samples for DataPools List. */
public final class DataPoolsListSamples {
    /*
     * x-ms-original-file: specification/adp/resource-manager/Microsoft.AutonomousDevelopmentPlatform/preview/2021-11-01-preview/examples/AdpDataPoolsList.json
     */
    /**
     * Sample code: List Data Pools.
     *
     * @param manager Entry point to AdpManager.
     */
    public static void listDataPools(com.azure.resourcemanager.adp.AdpManager manager) {
        manager.dataPools().list("adpClient", "sampleacct", Context.NONE);
    }
}
```

### DataPools_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.adp.models.DataPool;
import com.azure.resourcemanager.adp.models.DataPoolLocation;
import java.util.Arrays;

/** Samples for DataPools Update. */
public final class DataPoolsUpdateSamples {
    /*
     * x-ms-original-file: specification/adp/resource-manager/Microsoft.AutonomousDevelopmentPlatform/preview/2021-11-01-preview/examples/AdpDataPoolPatch.json
     */
    /**
     * Sample code: Patch Data Pool.
     *
     * @param manager Entry point to AdpManager.
     */
    public static void patchDataPool(com.azure.resourcemanager.adp.AdpManager manager) {
        DataPool resource =
            manager.dataPools().getWithResponse("adpClient", "sampleacct", "sampledp", Context.NONE).getValue();
        resource.update().withLocations(Arrays.asList(new DataPoolLocation().withName("westus"))).apply();
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/adp/resource-manager/Microsoft.AutonomousDevelopmentPlatform/preview/2021-11-01-preview/examples/AdpOperationsList.json
     */
    /**
     * Sample code: List operations.
     *
     * @param manager Entry point to AdpManager.
     */
    public static void listOperations(com.azure.resourcemanager.adp.AdpManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

