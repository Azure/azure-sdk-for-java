# Code snippets and samples


## Dashboards

- [CreateOrUpdate](#dashboards_createorupdate)
- [Delete](#dashboards_delete)
- [GetByResourceGroup](#dashboards_getbyresourcegroup)
- [List](#dashboards_list)
- [ListByResourceGroup](#dashboards_listbyresourcegroup)
- [Update](#dashboards_update)

## ListTenantConfigurationViolations

- [List](#listtenantconfigurationviolations_list)

## Operations

- [List](#operations_list)

## TenantConfigurations

- [Create](#tenantconfigurations_create)
- [Delete](#tenantconfigurations_delete)
- [Get](#tenantconfigurations_get)
- [List](#tenantconfigurations_list)
### Dashboards_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.portal.models.DashboardLens;
import com.azure.resourcemanager.portal.models.DashboardParts;
import com.azure.resourcemanager.portal.models.DashboardPartsPosition;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Dashboards CreateOrUpdate. */
public final class DashboardsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/portal/resource-manager/Microsoft.Portal/preview/2020-09-01-preview/examples/createOrUpdateDashboard.json
     */
    /**
     * Sample code: Create or update a Dashboard.
     *
     * @param manager Entry point to PortalManager.
     */
    public static void createOrUpdateADashboard(com.azure.resourcemanager.portal.PortalManager manager)
        throws IOException {
        manager
            .dashboards()
            .define("testDashboard")
            .withRegion("eastus")
            .withExistingResourceGroup("testRG")
            .withTags(mapOf("aKey", "aValue", "anotherKey", "anotherValue"))
            .withLenses(
                Arrays
                    .asList(
                        new DashboardLens()
                            .withOrder(1)
                            .withParts(
                                Arrays
                                    .asList(
                                        new DashboardParts()
                                            .withPosition(
                                                new DashboardPartsPosition()
                                                    .withX(1)
                                                    .withY(2)
                                                    .withRowSpan(4)
                                                    .withColSpan(3)),
                                        new DashboardParts()
                                            .withPosition(
                                                new DashboardPartsPosition()
                                                    .withX(5)
                                                    .withY(5)
                                                    .withRowSpan(6)
                                                    .withColSpan(6)))),
                        new DashboardLens().withOrder(2).withParts(Arrays.asList())))
            .withMetadata(
                mapOf(
                    "metadata",
                    SerializerFactory
                        .createDefaultManagementSerializerAdapter()
                        .deserialize(
                            "{\"ColSpan\":2,\"RowSpan\":1,\"X\":4,\"Y\":3}", Object.class, SerializerEncoding.JSON)))
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

### Dashboards_Delete

```java
import com.azure.core.util.Context;

/** Samples for Dashboards Delete. */
public final class DashboardsDeleteSamples {
    /*
     * x-ms-original-file: specification/portal/resource-manager/Microsoft.Portal/preview/2020-09-01-preview/examples/deleteDashboard.json
     */
    /**
     * Sample code: Delete a Dashboard.
     *
     * @param manager Entry point to PortalManager.
     */
    public static void deleteADashboard(com.azure.resourcemanager.portal.PortalManager manager) {
        manager.dashboards().deleteWithResponse("testRG", "testDashboard", Context.NONE);
    }
}
```

### Dashboards_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Dashboards GetByResourceGroup. */
public final class DashboardsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/portal/resource-manager/Microsoft.Portal/preview/2020-09-01-preview/examples/getDashboard.json
     */
    /**
     * Sample code: Get a Dashboard.
     *
     * @param manager Entry point to PortalManager.
     */
    public static void getADashboard(com.azure.resourcemanager.portal.PortalManager manager) {
        manager.dashboards().getByResourceGroupWithResponse("testRG", "testDashboard", Context.NONE);
    }
}
```

### Dashboards_List

```java
import com.azure.core.util.Context;

/** Samples for Dashboards List. */
public final class DashboardsListSamples {
    /*
     * x-ms-original-file: specification/portal/resource-manager/Microsoft.Portal/preview/2020-09-01-preview/examples/listDashboardsBySubscription.json
     */
    /**
     * Sample code: List all custom resource providers on the subscription.
     *
     * @param manager Entry point to PortalManager.
     */
    public static void listAllCustomResourceProvidersOnTheSubscription(
        com.azure.resourcemanager.portal.PortalManager manager) {
        manager.dashboards().list(Context.NONE);
    }
}
```

### Dashboards_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Dashboards ListByResourceGroup. */
public final class DashboardsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/portal/resource-manager/Microsoft.Portal/preview/2020-09-01-preview/examples/listDashboardsByResourceGroup.json
     */
    /**
     * Sample code: List all custom resource providers on the resourceGroup.
     *
     * @param manager Entry point to PortalManager.
     */
    public static void listAllCustomResourceProvidersOnTheResourceGroup(
        com.azure.resourcemanager.portal.PortalManager manager) {
        manager.dashboards().listByResourceGroup("testRG", Context.NONE);
    }
}
```

### Dashboards_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.portal.models.Dashboard;
import java.util.HashMap;
import java.util.Map;

/** Samples for Dashboards Update. */
public final class DashboardsUpdateSamples {
    /*
     * x-ms-original-file: specification/portal/resource-manager/Microsoft.Portal/preview/2020-09-01-preview/examples/updateDashboard.json
     */
    /**
     * Sample code: Update a Dashboard.
     *
     * @param manager Entry point to PortalManager.
     */
    public static void updateADashboard(com.azure.resourcemanager.portal.PortalManager manager) {
        Dashboard resource =
            manager.dashboards().getByResourceGroupWithResponse("testRG", "testDashboard", Context.NONE).getValue();
        resource.update().withTags(mapOf("aKey", "bValue", "anotherKey", "anotherValue2")).apply();
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

### ListTenantConfigurationViolations_List

```java
import com.azure.core.util.Context;

/** Samples for ListTenantConfigurationViolations List. */
public final class ListTenantConfigurationViolationsListSamples {
    /*
     * x-ms-original-file: specification/portal/resource-manager/Microsoft.Portal/preview/2020-09-01-preview/examples/TenantConfiguration/GetListOfTenantConfigurationViolations.json
     */
    /**
     * Sample code: Get list of of items that violate tenant's configuration.
     *
     * @param manager Entry point to PortalManager.
     */
    public static void getListOfOfItemsThatViolateTenantSConfiguration(
        com.azure.resourcemanager.portal.PortalManager manager) {
        manager.listTenantConfigurationViolations().list(Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/portal/resource-manager/Microsoft.Portal/preview/2020-09-01-preview/examples/operationsList.json
     */
    /**
     * Sample code: List the portal operations.
     *
     * @param manager Entry point to PortalManager.
     */
    public static void listThePortalOperations(com.azure.resourcemanager.portal.PortalManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### TenantConfigurations_Create

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.portal.fluent.models.ConfigurationInner;
import com.azure.resourcemanager.portal.models.ConfigurationName;

/** Samples for TenantConfigurations Create. */
public final class TenantConfigurationsCreateSamples {
    /*
     * x-ms-original-file: specification/portal/resource-manager/Microsoft.Portal/preview/2020-09-01-preview/examples/TenantConfiguration/CreateOrUpdateTenantConfiguration.json
     */
    /**
     * Sample code: Create or update Tenant configuration.
     *
     * @param manager Entry point to PortalManager.
     */
    public static void createOrUpdateTenantConfiguration(com.azure.resourcemanager.portal.PortalManager manager) {
        manager
            .tenantConfigurations()
            .createWithResponse(
                ConfigurationName.DEFAULT,
                new ConfigurationInner().withEnforcePrivateMarkdownStorage(true),
                Context.NONE);
    }
}
```

### TenantConfigurations_Delete

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.portal.models.ConfigurationName;

/** Samples for TenantConfigurations Delete. */
public final class TenantConfigurationsDeleteSamples {
    /*
     * x-ms-original-file: specification/portal/resource-manager/Microsoft.Portal/preview/2020-09-01-preview/examples/TenantConfiguration/DeleteTenantConfiguration.json
     */
    /**
     * Sample code: Delete Tenant configuration.
     *
     * @param manager Entry point to PortalManager.
     */
    public static void deleteTenantConfiguration(com.azure.resourcemanager.portal.PortalManager manager) {
        manager.tenantConfigurations().deleteWithResponse(ConfigurationName.DEFAULT, Context.NONE);
    }
}
```

### TenantConfigurations_Get

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.portal.models.ConfigurationName;

/** Samples for TenantConfigurations Get. */
public final class TenantConfigurationsGetSamples {
    /*
     * x-ms-original-file: specification/portal/resource-manager/Microsoft.Portal/preview/2020-09-01-preview/examples/TenantConfiguration/GetTenantConfiguration.json
     */
    /**
     * Sample code: Get Tenant configuration.
     *
     * @param manager Entry point to PortalManager.
     */
    public static void getTenantConfiguration(com.azure.resourcemanager.portal.PortalManager manager) {
        manager.tenantConfigurations().getWithResponse(ConfigurationName.DEFAULT, Context.NONE);
    }
}
```

### TenantConfigurations_List

```java
import com.azure.core.util.Context;

/** Samples for TenantConfigurations List. */
public final class TenantConfigurationsListSamples {
    /*
     * x-ms-original-file: specification/portal/resource-manager/Microsoft.Portal/preview/2020-09-01-preview/examples/TenantConfiguration/GetListOfTenantConfigurations.json
     */
    /**
     * Sample code: Get list of Tenant configurations.
     *
     * @param manager Entry point to PortalManager.
     */
    public static void getListOfTenantConfigurations(com.azure.resourcemanager.portal.PortalManager manager) {
        manager.tenantConfigurations().list(Context.NONE);
    }
}
```

