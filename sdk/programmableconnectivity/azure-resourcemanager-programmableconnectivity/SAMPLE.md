# Code snippets and samples


## Gateways

- [CreateOrUpdate](#gateways_createorupdate)
- [Delete](#gateways_delete)
- [GetByResourceGroup](#gateways_getbyresourcegroup)
- [List](#gateways_list)
- [ListByResourceGroup](#gateways_listbyresourcegroup)
- [Update](#gateways_update)

## Operations

- [List](#operations_list)

## OperatorApiConnections

- [Create](#operatorapiconnections_create)
- [Delete](#operatorapiconnections_delete)
- [GetByResourceGroup](#operatorapiconnections_getbyresourcegroup)
- [List](#operatorapiconnections_list)
- [ListByResourceGroup](#operatorapiconnections_listbyresourcegroup)
- [Update](#operatorapiconnections_update)

## OperatorApiPlans

- [Get](#operatorapiplans_get)
- [List](#operatorapiplans_list)
### Gateways_CreateOrUpdate

```java
import com.azure.resourcemanager.programmableconnectivity.models.GatewayProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Gateways CreateOrUpdate.
 */
public final class GatewaysCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-01-15-preview/Gateways_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Gateways_CreateOrUpdate.
     * 
     * @param manager Entry point to ProgrammableConnectivityManager.
     */
    public static void gatewaysCreateOrUpdate(
        com.azure.resourcemanager.programmableconnectivity.ProgrammableConnectivityManager manager) {
        manager.gateways()
            .define("pgzk")
            .withRegion("oryhozfmeohscezl")
            .withExistingResourceGroup("rgopenapi")
            .withTags(mapOf("key2642", "fakeTokenPlaceholder"))
            .withProperties(new GatewayProperties())
            .create();
    }

    // Use "Map.of" if available
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

### Gateways_Delete

```java
/**
 * Samples for Gateways Delete.
 */
public final class GatewaysDeleteSamples {
    /*
     * x-ms-original-file: 2024-01-15-preview/Gateways_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: Gateways_Delete.
     * 
     * @param manager Entry point to ProgrammableConnectivityManager.
     */
    public static void
        gatewaysDelete(com.azure.resourcemanager.programmableconnectivity.ProgrammableConnectivityManager manager) {
        manager.gateways().delete("rgopenapi", "udveaau", com.azure.core.util.Context.NONE);
    }
}
```

### Gateways_GetByResourceGroup

```java
/**
 * Samples for Gateways GetByResourceGroup.
 */
public final class GatewaysGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-01-15-preview/Gateways_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Gateways_Get.
     * 
     * @param manager Entry point to ProgrammableConnectivityManager.
     */
    public static void
        gatewaysGet(com.azure.resourcemanager.programmableconnectivity.ProgrammableConnectivityManager manager) {
        manager.gateways()
            .getByResourceGroupWithResponse("rgopenapi", "kdgpdkrucfphqtgafao", com.azure.core.util.Context.NONE);
    }
}
```

### Gateways_List

```java
/**
 * Samples for Gateways List.
 */
public final class GatewaysListSamples {
    /*
     * x-ms-original-file: 2024-01-15-preview/Gateways_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: Gateways_ListBySubscription.
     * 
     * @param manager Entry point to ProgrammableConnectivityManager.
     */
    public static void gatewaysListBySubscription(
        com.azure.resourcemanager.programmableconnectivity.ProgrammableConnectivityManager manager) {
        manager.gateways().list(com.azure.core.util.Context.NONE);
    }
}
```

### Gateways_ListByResourceGroup

```java
/**
 * Samples for Gateways ListByResourceGroup.
 */
public final class GatewaysListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-01-15-preview/Gateways_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: Gateways_ListByResourceGroup.
     * 
     * @param manager Entry point to ProgrammableConnectivityManager.
     */
    public static void gatewaysListByResourceGroup(
        com.azure.resourcemanager.programmableconnectivity.ProgrammableConnectivityManager manager) {
        manager.gateways().listByResourceGroup("rgopenapi", com.azure.core.util.Context.NONE);
    }
}
```

### Gateways_Update

```java
import com.azure.resourcemanager.programmableconnectivity.models.Gateway;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Gateways Update.
 */
public final class GatewaysUpdateSamples {
    /*
     * x-ms-original-file: 2024-01-15-preview/Gateways_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Gateways_Update.
     * 
     * @param manager Entry point to ProgrammableConnectivityManager.
     */
    public static void
        gatewaysUpdate(com.azure.resourcemanager.programmableconnectivity.ProgrammableConnectivityManager manager) {
        Gateway resource = manager.gateways()
            .getByResourceGroupWithResponse("rgopenapi", "pgzk", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("key2642", "fakeTokenPlaceholder")).apply();
    }

    // Use "Map.of" if available
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
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2024-01-15-preview/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to ProgrammableConnectivityManager.
     */
    public static void
        operationsList(com.azure.resourcemanager.programmableconnectivity.ProgrammableConnectivityManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### OperatorApiConnections_Create

```java
import com.azure.resourcemanager.programmableconnectivity.models.AccountType;
import com.azure.resourcemanager.programmableconnectivity.models.ApplicationProperties;
import com.azure.resourcemanager.programmableconnectivity.models.OperatorApiConnectionProperties;
import com.azure.resourcemanager.programmableconnectivity.models.SaasProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for OperatorApiConnections Create.
 */
public final class OperatorApiConnectionsCreateSamples {
    /*
     * x-ms-original-file: 2024-01-15-preview/OperatorApiConnections_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: OperatorApiConnections_Create.
     * 
     * @param manager Entry point to ProgrammableConnectivityManager.
     */
    public static void operatorApiConnectionsCreate(
        com.azure.resourcemanager.programmableconnectivity.ProgrammableConnectivityManager manager) {
        manager.operatorApiConnections()
            .define("nzsdg")
            .withRegion("dwvzfkjoepbmksygazllqryyinn")
            .withExistingResourceGroup("rgopenapi")
            .withTags(mapOf("key5536", "fakeTokenPlaceholder"))
            .withProperties(new OperatorApiConnectionProperties().withOperatorApiPlanId(
                "/subscriptions/00000000-0000-0000-0000-00000000000/providers/Microsoft.ProgrammableConnectivity/operatorApiPlans/livmzrh")
                .withSaasProperties(
                    new SaasProperties().withSaasSubscriptionId("mgyusmqt").withSaasResourceId("pekejefyvfviabimdrmno"))
                .withConfiguredApplication(new ApplicationProperties().withName("idzqqen")
                    .withApplicationDescription("gjlwegnqvffvsc")
                    .withApplicationType("f")
                    .withLegalName("ar")
                    .withOrganizationDescription("fcueqzlxxr")
                    .withTaxNumber("ngzv")
                    .withPrivacyContactEmailAddress("l"))
                .withAppId("czgrhbvgr")
                .withGatewayId(
                    "/subscriptions/00000000-0000-0000-0000-00000000000/resourceGroups/example-rg/providers/Microsoft.ProgrammableConnectivity/gateways/cdvcixxcdhjqw")
                .withAccountType(AccountType.AZURE_MANAGED)
                .withAppSecret("fakeTokenPlaceholder"))
            .create();
    }

    // Use "Map.of" if available
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

### OperatorApiConnections_Delete

```java
/**
 * Samples for OperatorApiConnections Delete.
 */
public final class OperatorApiConnectionsDeleteSamples {
    /*
     * x-ms-original-file: 2024-01-15-preview/OperatorApiConnections_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: OperatorApiConnections_Delete.
     * 
     * @param manager Entry point to ProgrammableConnectivityManager.
     */
    public static void operatorApiConnectionsDelete(
        com.azure.resourcemanager.programmableconnectivity.ProgrammableConnectivityManager manager) {
        manager.operatorApiConnections().delete("rgopenapi", "dawr", com.azure.core.util.Context.NONE);
    }
}
```

### OperatorApiConnections_GetByResourceGroup

```java
/**
 * Samples for OperatorApiConnections GetByResourceGroup.
 */
public final class OperatorApiConnectionsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-01-15-preview/OperatorApiConnections_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: OperatorApiConnections_Get.
     * 
     * @param manager Entry point to ProgrammableConnectivityManager.
     */
    public static void operatorApiConnectionsGet(
        com.azure.resourcemanager.programmableconnectivity.ProgrammableConnectivityManager manager) {
        manager.operatorApiConnections()
            .getByResourceGroupWithResponse("rgopenapi", "uetzqjrwqtkwgcirdqy", com.azure.core.util.Context.NONE);
    }
}
```

### OperatorApiConnections_List

```java
/**
 * Samples for OperatorApiConnections List.
 */
public final class OperatorApiConnectionsListSamples {
    /*
     * x-ms-original-file: 2024-01-15-preview/OperatorApiConnections_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: OperatorApiConnections_ListBySubscription.
     * 
     * @param manager Entry point to ProgrammableConnectivityManager.
     */
    public static void operatorApiConnectionsListBySubscription(
        com.azure.resourcemanager.programmableconnectivity.ProgrammableConnectivityManager manager) {
        manager.operatorApiConnections().list(com.azure.core.util.Context.NONE);
    }
}
```

### OperatorApiConnections_ListByResourceGroup

```java
/**
 * Samples for OperatorApiConnections ListByResourceGroup.
 */
public final class OperatorApiConnectionsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-01-15-preview/OperatorApiConnections_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: OperatorApiConnections_ListByResourceGroup.
     * 
     * @param manager Entry point to ProgrammableConnectivityManager.
     */
    public static void operatorApiConnectionsListByResourceGroup(
        com.azure.resourcemanager.programmableconnectivity.ProgrammableConnectivityManager manager) {
        manager.operatorApiConnections().listByResourceGroup("rgopenapi", com.azure.core.util.Context.NONE);
    }
}
```

### OperatorApiConnections_Update

```java
import com.azure.resourcemanager.programmableconnectivity.models.ApplicationProperties;
import com.azure.resourcemanager.programmableconnectivity.models.OperatorApiConnection;
import com.azure.resourcemanager.programmableconnectivity.models.OperatorApiConnectionUpdateProperties;
import com.azure.resourcemanager.programmableconnectivity.models.SaasProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for OperatorApiConnections Update.
 */
public final class OperatorApiConnectionsUpdateSamples {
    /*
     * x-ms-original-file: 2024-01-15-preview/OperatorApiConnections_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: OperatorApiConnections_Update.
     * 
     * @param manager Entry point to ProgrammableConnectivityManager.
     */
    public static void operatorApiConnectionsUpdate(
        com.azure.resourcemanager.programmableconnectivity.ProgrammableConnectivityManager manager) {
        OperatorApiConnection resource = manager.operatorApiConnections()
            .getByResourceGroupWithResponse("rgopenapi", "syefewgf", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key3150", "fakeTokenPlaceholder"))
            .withProperties(new OperatorApiConnectionUpdateProperties().withOperatorApiPlanId(
                "/subscriptions/00000000-0000-0000-0000-00000000000/providers/Microsoft.ProgrammableConnectivity/operatorApiPlans/yhlygxdwvrzgazbfzyz")
                .withSaasProperties(
                    new SaasProperties().withSaasSubscriptionId("mgyusmqt").withSaasResourceId("pekejefyvfviabimdrmno"))
                .withConfiguredApplication(new ApplicationProperties().withName("idzqqen")
                    .withApplicationDescription("gjlwegnqvffvsc")
                    .withApplicationType("f")
                    .withLegalName("ar")
                    .withOrganizationDescription("fcueqzlxxr")
                    .withTaxNumber("ngzv")
                    .withPrivacyContactEmailAddress("l"))
                .withAppId("mkfcrn")
                .withAppSecret("fakeTokenPlaceholder"))
            .apply();
    }

    // Use "Map.of" if available
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

### OperatorApiPlans_Get

```java
/**
 * Samples for OperatorApiPlans Get.
 */
public final class OperatorApiPlansGetSamples {
    /*
     * x-ms-original-file: 2024-01-15-preview/OperatorApiPlans_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: OperatorApiPlans_Get.
     * 
     * @param manager Entry point to ProgrammableConnectivityManager.
     */
    public static void operatorApiPlansGet(
        com.azure.resourcemanager.programmableconnectivity.ProgrammableConnectivityManager manager) {
        manager.operatorApiPlans().getWithResponse("etzfxkqegslnxdhdmzvbtzxahnyq", com.azure.core.util.Context.NONE);
    }
}
```

### OperatorApiPlans_List

```java
/**
 * Samples for OperatorApiPlans List.
 */
public final class OperatorApiPlansListSamples {
    /*
     * x-ms-original-file: 2024-01-15-preview/OperatorApiPlans_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: OperatorApiPlans_ListBySubscription.
     * 
     * @param manager Entry point to ProgrammableConnectivityManager.
     */
    public static void operatorApiPlansListBySubscription(
        com.azure.resourcemanager.programmableconnectivity.ProgrammableConnectivityManager manager) {
        manager.operatorApiPlans().list(null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

