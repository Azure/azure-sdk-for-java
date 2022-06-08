# Code snippets and samples


## B2CTenants

- [CheckNameAvailability](#b2ctenants_checknameavailability)
- [Create](#b2ctenants_create)
- [Delete](#b2ctenants_delete)
- [GetByResourceGroup](#b2ctenants_getbyresourcegroup)
- [List](#b2ctenants_list)
- [ListByResourceGroup](#b2ctenants_listbyresourcegroup)
- [Update](#b2ctenants_update)

## GuestUsages

- [Create](#guestusages_create)
- [Delete](#guestusages_delete)
- [GetByResourceGroup](#guestusages_getbyresourcegroup)
- [List](#guestusages_list)
- [ListByResourceGroup](#guestusages_listbyresourcegroup)
- [Update](#guestusages_update)

## Operations

- [List](#operations_list)
### B2CTenants_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.azureadexternalidentities.models.CheckNameAvailabilityRequestBody;

/** Samples for B2CTenants CheckNameAvailability. */
public final class B2CTenantsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/cpim/resource-manager/Microsoft.AzureActiveDirectory/stable/2021-04-01/examples/checkNameAvailability-taken.json
     */
    /**
     * Sample code: Check name availability - taken.
     *
     * @param manager Entry point to ExternalIdentitiesConfigurationManager.
     */
    public static void checkNameAvailabilityTaken(
        com.azure.resourcemanager.azureadexternalidentities.ExternalIdentitiesConfigurationManager manager) {
        manager
            .b2CTenants()
            .checkNameAvailabilityWithResponse(
                new CheckNameAvailabilityRequestBody().withName("constoso.onmicrosoft.com").withCountryCode("US"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/cpim/resource-manager/Microsoft.AzureActiveDirectory/stable/2021-04-01/examples/checkNameAvailability-available.json
     */
    /**
     * Sample code: Check name availability - available.
     *
     * @param manager Entry point to ExternalIdentitiesConfigurationManager.
     */
    public static void checkNameAvailabilityAvailable(
        com.azure.resourcemanager.azureadexternalidentities.ExternalIdentitiesConfigurationManager manager) {
        manager
            .b2CTenants()
            .checkNameAvailabilityWithResponse(
                new CheckNameAvailabilityRequestBody().withName("constoso.onmicrosoft.com").withCountryCode("US"),
                Context.NONE);
    }
}
```

### B2CTenants_Create

```java
import com.azure.resourcemanager.azureadexternalidentities.models.B2CResourceSku;
import com.azure.resourcemanager.azureadexternalidentities.models.B2CResourceSkuName;
import com.azure.resourcemanager.azureadexternalidentities.models.B2CResourceSkuTier;

/** Samples for B2CTenants Create. */
public final class B2CTenantsCreateSamples {
    /*
     * x-ms-original-file: specification/cpim/resource-manager/Microsoft.AzureActiveDirectory/stable/2021-04-01/examples/createTenant.json
     */
    /**
     * Sample code: Create tenant.
     *
     * @param manager Entry point to ExternalIdentitiesConfigurationManager.
     */
    public static void createTenant(
        com.azure.resourcemanager.azureadexternalidentities.ExternalIdentitiesConfigurationManager manager) {
        manager
            .b2CTenants()
            .define("contoso.onmicrosoft.com")
            .withRegion("United States")
            .withExistingResourceGroup("contosoResourceGroup")
            .withSku(new B2CResourceSku().withName(B2CResourceSkuName.STANDARD).withTier(B2CResourceSkuTier.A0))
            .withDisplayName("Contoso")
            .withCountryCode("US")
            .create();
    }
}
```

### B2CTenants_Delete

```java
import com.azure.core.util.Context;

/** Samples for B2CTenants Delete. */
public final class B2CTenantsDeleteSamples {
    /*
     * x-ms-original-file: specification/cpim/resource-manager/Microsoft.AzureActiveDirectory/stable/2021-04-01/examples/deleteTenant.json
     */
    /**
     * Sample code: Delete tenant.
     *
     * @param manager Entry point to ExternalIdentitiesConfigurationManager.
     */
    public static void deleteTenant(
        com.azure.resourcemanager.azureadexternalidentities.ExternalIdentitiesConfigurationManager manager) {
        manager.b2CTenants().delete("rg1", "contoso.onmicrosoft.com", Context.NONE);
    }
}
```

### B2CTenants_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for B2CTenants GetByResourceGroup. */
public final class B2CTenantsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/cpim/resource-manager/Microsoft.AzureActiveDirectory/stable/2021-04-01/examples/getTenant.json
     */
    /**
     * Sample code: Get tenant.
     *
     * @param manager Entry point to ExternalIdentitiesConfigurationManager.
     */
    public static void getTenant(
        com.azure.resourcemanager.azureadexternalidentities.ExternalIdentitiesConfigurationManager manager) {
        manager
            .b2CTenants()
            .getByResourceGroupWithResponse("contosoResourceGroup", "contoso.onmicrosoft.com", Context.NONE);
    }
}
```

### B2CTenants_List

```java
import com.azure.core.util.Context;

/** Samples for B2CTenants List. */
public final class B2CTenantsListSamples {
    /*
     * x-ms-original-file: specification/cpim/resource-manager/Microsoft.AzureActiveDirectory/stable/2021-04-01/examples/listTenantsBySubscription.json
     */
    /**
     * Sample code: B2CTenants_ListBySubscription.
     *
     * @param manager Entry point to ExternalIdentitiesConfigurationManager.
     */
    public static void b2CTenantsListBySubscription(
        com.azure.resourcemanager.azureadexternalidentities.ExternalIdentitiesConfigurationManager manager) {
        manager.b2CTenants().list(Context.NONE);
    }
}
```

### B2CTenants_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for B2CTenants ListByResourceGroup. */
public final class B2CTenantsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/cpim/resource-manager/Microsoft.AzureActiveDirectory/stable/2021-04-01/examples/listTenantsByResourceGroup.json
     */
    /**
     * Sample code: B2CTenants_ListByResourceGroup.
     *
     * @param manager Entry point to ExternalIdentitiesConfigurationManager.
     */
    public static void b2CTenantsListByResourceGroup(
        com.azure.resourcemanager.azureadexternalidentities.ExternalIdentitiesConfigurationManager manager) {
        manager.b2CTenants().listByResourceGroup("contosoResourceGroup", Context.NONE);
    }
}
```

### B2CTenants_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.azureadexternalidentities.models.B2CResourceSku;
import com.azure.resourcemanager.azureadexternalidentities.models.B2CResourceSkuName;
import com.azure.resourcemanager.azureadexternalidentities.models.B2CTenantResource;
import com.azure.resourcemanager.azureadexternalidentities.models.B2CTenantResourcePropertiesBillingConfig;
import com.azure.resourcemanager.azureadexternalidentities.models.BillingType;
import java.util.HashMap;
import java.util.Map;

/** Samples for B2CTenants Update. */
public final class B2CTenantsUpdateSamples {
    /*
     * x-ms-original-file: specification/cpim/resource-manager/Microsoft.AzureActiveDirectory/stable/2021-04-01/examples/updateTenant.json
     */
    /**
     * Sample code: Update tenant.
     *
     * @param manager Entry point to ExternalIdentitiesConfigurationManager.
     */
    public static void updateTenant(
        com.azure.resourcemanager.azureadexternalidentities.ExternalIdentitiesConfigurationManager manager) {
        B2CTenantResource resource =
            manager
                .b2CTenants()
                .getByResourceGroupWithResponse("contosoResourceGroup", "contoso.onmicrosoft.com", Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("key", "value"))
            .withSku(new B2CResourceSku().withName(B2CResourceSkuName.PREMIUM_P1))
            .withBillingConfig(new B2CTenantResourcePropertiesBillingConfig().withBillingType(BillingType.MAU))
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

### GuestUsages_Create

```java
/** Samples for GuestUsages Create. */
public final class GuestUsagesCreateSamples {
    /*
     * x-ms-original-file: specification/cpim/resource-manager/Microsoft.AzureActiveDirectory/stable/2021-04-01/examples/GuestUsagesCreate.json
     */
    /**
     * Sample code: GuestUsages_Create.
     *
     * @param manager Entry point to ExternalIdentitiesConfigurationManager.
     */
    public static void guestUsagesCreate(
        com.azure.resourcemanager.azureadexternalidentities.ExternalIdentitiesConfigurationManager manager) {
        manager
            .guestUsages()
            .define("contoso.onmicrosoft.com")
            .withRegion("United States")
            .withExistingResourceGroup("contosoResourceGroup")
            .withTenantId("c963dd1a-9174-4c23-8bae-733d7f955492")
            .create();
    }
}
```

### GuestUsages_Delete

```java
import com.azure.core.util.Context;

/** Samples for GuestUsages Delete. */
public final class GuestUsagesDeleteSamples {
    /*
     * x-ms-original-file: specification/cpim/resource-manager/Microsoft.AzureActiveDirectory/stable/2021-04-01/examples/GuestUsagesDelete.json
     */
    /**
     * Sample code: GuestUsages_Delete.
     *
     * @param manager Entry point to ExternalIdentitiesConfigurationManager.
     */
    public static void guestUsagesDelete(
        com.azure.resourcemanager.azureadexternalidentities.ExternalIdentitiesConfigurationManager manager) {
        manager.guestUsages().deleteWithResponse("contosoResourceGroup", "contoso.onmicrosoft.com", Context.NONE);
    }
}
```

### GuestUsages_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for GuestUsages GetByResourceGroup. */
public final class GuestUsagesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/cpim/resource-manager/Microsoft.AzureActiveDirectory/stable/2021-04-01/examples/GuestUsagesGet.json
     */
    /**
     * Sample code: GuestUsages_Get.
     *
     * @param manager Entry point to ExternalIdentitiesConfigurationManager.
     */
    public static void guestUsagesGet(
        com.azure.resourcemanager.azureadexternalidentities.ExternalIdentitiesConfigurationManager manager) {
        manager
            .guestUsages()
            .getByResourceGroupWithResponse("contosoResourceGroup", "contoso.onmicrosoft.com", Context.NONE);
    }
}
```

### GuestUsages_List

```java
import com.azure.core.util.Context;

/** Samples for GuestUsages List. */
public final class GuestUsagesListSamples {
    /*
     * x-ms-original-file: specification/cpim/resource-manager/Microsoft.AzureActiveDirectory/stable/2021-04-01/examples/GuestUsagesSubscriptionGet.json
     */
    /**
     * Sample code: GuestUsagesSubscription_List.
     *
     * @param manager Entry point to ExternalIdentitiesConfigurationManager.
     */
    public static void guestUsagesSubscriptionList(
        com.azure.resourcemanager.azureadexternalidentities.ExternalIdentitiesConfigurationManager manager) {
        manager.guestUsages().list(Context.NONE);
    }
}
```

### GuestUsages_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for GuestUsages ListByResourceGroup. */
public final class GuestUsagesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/cpim/resource-manager/Microsoft.AzureActiveDirectory/stable/2021-04-01/examples/GuestUsagesResourceGroupGet.json
     */
    /**
     * Sample code: GuestUsagesResourceGroup_List.
     *
     * @param manager Entry point to ExternalIdentitiesConfigurationManager.
     */
    public static void guestUsagesResourceGroupList(
        com.azure.resourcemanager.azureadexternalidentities.ExternalIdentitiesConfigurationManager manager) {
        manager.guestUsages().listByResourceGroup("contosoResourceGroup", Context.NONE);
    }
}
```

### GuestUsages_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.azureadexternalidentities.models.GuestUsagesResource;

/** Samples for GuestUsages Update. */
public final class GuestUsagesUpdateSamples {
    /*
     * x-ms-original-file: specification/cpim/resource-manager/Microsoft.AzureActiveDirectory/stable/2021-04-01/examples/GuestUsagesUpdate.json
     */
    /**
     * Sample code: GuestUsages_Update.
     *
     * @param manager Entry point to ExternalIdentitiesConfigurationManager.
     */
    public static void guestUsagesUpdate(
        com.azure.resourcemanager.azureadexternalidentities.ExternalIdentitiesConfigurationManager manager) {
        GuestUsagesResource resource =
            manager
                .guestUsages()
                .getByResourceGroupWithResponse("contosoResourceGroup", "contoso.onmicrosoft.com", Context.NONE)
                .getValue();
        resource.update().apply();
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/cpim/resource-manager/Microsoft.AzureActiveDirectory/stable/2021-04-01/examples/OperationsList.json
     */
    /**
     * Sample code: operations_list.
     *
     * @param manager Entry point to ExternalIdentitiesConfigurationManager.
     */
    public static void operationsList(
        com.azure.resourcemanager.azureadexternalidentities.ExternalIdentitiesConfigurationManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

