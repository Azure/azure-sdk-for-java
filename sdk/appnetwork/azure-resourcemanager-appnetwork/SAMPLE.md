# Code snippets and samples


## AppLinkMembers

- [CreateOrUpdate](#applinkmembers_createorupdate)
- [Delete](#applinkmembers_delete)
- [Get](#applinkmembers_get)
- [ListByAppLink](#applinkmembers_listbyapplink)
- [Update](#applinkmembers_update)

## AppLinks

- [CreateOrUpdate](#applinks_createorupdate)
- [Delete](#applinks_delete)
- [GetByResourceGroup](#applinks_getbyresourcegroup)
- [List](#applinks_list)
- [ListByResourceGroup](#applinks_listbyresourcegroup)
- [Update](#applinks_update)

## AvailableVersions

- [ListByLocation](#availableversions_listbylocation)

## Operations

- [List](#operations_list)

## UpgradeHistories

- [ListByAppLinkMember](#upgradehistories_listbyapplinkmember)
### AppLinkMembers_CreateOrUpdate

```java
import com.azure.resourcemanager.appnetwork.models.AppLinkMemberProperties;
import com.azure.resourcemanager.appnetwork.models.ClusterType;
import com.azure.resourcemanager.appnetwork.models.ConnectivityProfile;
import com.azure.resourcemanager.appnetwork.models.EastWestGatewayProfile;
import com.azure.resourcemanager.appnetwork.models.EastWestGatewayVisibility;
import com.azure.resourcemanager.appnetwork.models.FullyManagedUpgradeProfile;
import com.azure.resourcemanager.appnetwork.models.Metadata;
import com.azure.resourcemanager.appnetwork.models.PrivateConnectProfile;
import com.azure.resourcemanager.appnetwork.models.UpgradeMode;
import com.azure.resourcemanager.appnetwork.models.UpgradeProfile;
import com.azure.resourcemanager.appnetwork.models.UpgradeReleaseChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AppLinkMembers CreateOrUpdate.
 */
public final class AppLinkMembersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-08-01-preview/AppLinkMembers_CreateOrUpdate.json
     */
    /**
     * Sample code: AppLinkMembers_CreateOrUpdate.
     * 
     * @param manager Entry point to AppnetworkManager.
     */
    public static void appLinkMembersCreateOrUpdate(com.azure.resourcemanager.appnetwork.AppnetworkManager manager) {
        manager.appLinkMembers()
            .define("member-01")
            .withRegion("westus2")
            .withExistingAppLink("test_rg", "applink-test-01")
            .withTags(mapOf("key2913", "fakeTokenPlaceholder"))
            .withProperties(new AppLinkMemberProperties().withClusterType(ClusterType.AKS)
                .withMetadata(new Metadata().withResourceId(
                    "/subscriptions/bc7e0da9-5e4c-4a91-9252-9658837006cf/resourcegroups/applink-rg/providers/Microsoft.ContainerService/managedClusters/applink-member1"))
                .withUpgradeProfile(new UpgradeProfile().withMode(UpgradeMode.FULLY_MANAGED)
                    .withFullyManagedUpgradeProfile(
                        new FullyManagedUpgradeProfile().withReleaseChannel(UpgradeReleaseChannel.STABLE)))
                .withConnectivityProfile(new ConnectivityProfile()
                    .withEastWestGateway(
                        new EastWestGatewayProfile().withVisibility(EastWestGatewayVisibility.INTERNAL))
                    .withPrivateConnect(new PrivateConnectProfile().withSubnetResourceId(
                        "/subscriptions/bc7e0da9-5e4c-4a91-9252-9658837006cf/resourceGroups/applink-vnet-rg/providers/Microsoft.Network/virtualNetworks/vnet1/subnets/subnet1"))))
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

### AppLinkMembers_Delete

```java
/**
 * Samples for AppLinkMembers Delete.
 */
public final class AppLinkMembersDeleteSamples {
    /*
     * x-ms-original-file: 2025-08-01-preview/AppLinkMembers_Delete.json
     */
    /**
     * Sample code: AppLinkMembers_Delete.
     * 
     * @param manager Entry point to AppnetworkManager.
     */
    public static void appLinkMembersDelete(com.azure.resourcemanager.appnetwork.AppnetworkManager manager) {
        manager.appLinkMembers().delete("test_rg", "applink-test-01", "member-01", com.azure.core.util.Context.NONE);
    }
}
```

### AppLinkMembers_Get

```java
/**
 * Samples for AppLinkMembers Get.
 */
public final class AppLinkMembersGetSamples {
    /*
     * x-ms-original-file: 2025-08-01-preview/AppLinkMembers_Get.json
     */
    /**
     * Sample code: AppLinkMembers_Get.
     * 
     * @param manager Entry point to AppnetworkManager.
     */
    public static void appLinkMembersGet(com.azure.resourcemanager.appnetwork.AppnetworkManager manager) {
        manager.appLinkMembers()
            .getWithResponse("test_rg", "applink-test-01", "member-01", com.azure.core.util.Context.NONE);
    }
}
```

### AppLinkMembers_ListByAppLink

```java
/**
 * Samples for AppLinkMembers ListByAppLink.
 */
public final class AppLinkMembersListByAppLinkSamples {
    /*
     * x-ms-original-file: 2025-08-01-preview/AppLinkMembers_ListByAppLink.json
     */
    /**
     * Sample code: AppLinkMembers_ListByAppLink.
     * 
     * @param manager Entry point to AppnetworkManager.
     */
    public static void appLinkMembersListByAppLink(com.azure.resourcemanager.appnetwork.AppnetworkManager manager) {
        manager.appLinkMembers().listByAppLink("test_rg", "applink-test-01", com.azure.core.util.Context.NONE);
    }
}
```

### AppLinkMembers_Update

```java
import com.azure.resourcemanager.appnetwork.models.AppLinkMember;
import com.azure.resourcemanager.appnetwork.models.AppLinkMemberUpdateProperties;
import com.azure.resourcemanager.appnetwork.models.ConnectivityProfile;
import com.azure.resourcemanager.appnetwork.models.EastWestGatewayProfile;
import com.azure.resourcemanager.appnetwork.models.EastWestGatewayVisibility;
import com.azure.resourcemanager.appnetwork.models.SelfManagedUpgradeProfile;
import com.azure.resourcemanager.appnetwork.models.UpgradeMode;
import com.azure.resourcemanager.appnetwork.models.UpgradeProfile;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AppLinkMembers Update.
 */
public final class AppLinkMembersUpdateSamples {
    /*
     * x-ms-original-file: 2025-08-01-preview/AppLinkMembers_Update.json
     */
    /**
     * Sample code: AppLinkMembers_Update.
     * 
     * @param manager Entry point to AppnetworkManager.
     */
    public static void appLinkMembersUpdate(com.azure.resourcemanager.appnetwork.AppnetworkManager manager) {
        AppLinkMember resource = manager.appLinkMembers()
            .getWithResponse("test_rg", "applink-test-01", "member-01", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key7952", "fakeTokenPlaceholder"))
            .withProperties(new AppLinkMemberUpdateProperties()
                .withUpgradeProfile(new UpgradeProfile().withMode(UpgradeMode.SELF_MANAGED)
                    .withSelfManagedUpgradeProfile(new SelfManagedUpgradeProfile().withVersion("1.26")))
                .withConnectivityProfile(new ConnectivityProfile().withEastWestGateway(
                    new EastWestGatewayProfile().withVisibility(EastWestGatewayVisibility.INTERNAL))))
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

### AppLinks_CreateOrUpdate

```java
import com.azure.resourcemanager.appnetwork.models.AppLinkProperties;
import com.azure.resourcemanager.appnetwork.models.ManagedServiceIdentity;
import com.azure.resourcemanager.appnetwork.models.ManagedServiceIdentityType;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AppLinks CreateOrUpdate.
 */
public final class AppLinksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-08-01-preview/AppLinks_CreateOrUpdate.json
     */
    /**
     * Sample code: AppLinks_CreateOrUpdate.
     * 
     * @param manager Entry point to AppnetworkManager.
     */
    public static void appLinksCreateOrUpdate(com.azure.resourcemanager.appnetwork.AppnetworkManager manager) {
        manager.appLinks()
            .define("applink-test-01")
            .withRegion("westus2")
            .withExistingResourceGroup("test_rg")
            .withTags(mapOf("key2913", "fakeTokenPlaceholder"))
            .withProperties(new AppLinkProperties())
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
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

### AppLinks_Delete

```java
/**
 * Samples for AppLinks Delete.
 */
public final class AppLinksDeleteSamples {
    /*
     * x-ms-original-file: 2025-08-01-preview/AppLinks_Delete.json
     */
    /**
     * Sample code: AppLinks_Delete.
     * 
     * @param manager Entry point to AppnetworkManager.
     */
    public static void appLinksDelete(com.azure.resourcemanager.appnetwork.AppnetworkManager manager) {
        manager.appLinks().delete("test_rg", "applink-test-01", com.azure.core.util.Context.NONE);
    }
}
```

### AppLinks_GetByResourceGroup

```java
/**
 * Samples for AppLinks GetByResourceGroup.
 */
public final class AppLinksGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-08-01-preview/AppLinks_Get.json
     */
    /**
     * Sample code: AppLinks_Get.
     * 
     * @param manager Entry point to AppnetworkManager.
     */
    public static void appLinksGet(com.azure.resourcemanager.appnetwork.AppnetworkManager manager) {
        manager.appLinks()
            .getByResourceGroupWithResponse("test_rg", "applink-test-01", com.azure.core.util.Context.NONE);
    }
}
```

### AppLinks_List

```java
/**
 * Samples for AppLinks List.
 */
public final class AppLinksListSamples {
    /*
     * x-ms-original-file: 2025-08-01-preview/AppLinks_ListBySubscription.json
     */
    /**
     * Sample code: AppLinks_ListBySubscription.
     * 
     * @param manager Entry point to AppnetworkManager.
     */
    public static void appLinksListBySubscription(com.azure.resourcemanager.appnetwork.AppnetworkManager manager) {
        manager.appLinks().list(com.azure.core.util.Context.NONE);
    }
}
```

### AppLinks_ListByResourceGroup

```java
/**
 * Samples for AppLinks ListByResourceGroup.
 */
public final class AppLinksListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-08-01-preview/AppLinks_ListByResourceGroup.json
     */
    /**
     * Sample code: AppLinks_ListByResourceGroup.
     * 
     * @param manager Entry point to AppnetworkManager.
     */
    public static void appLinksListByResourceGroup(com.azure.resourcemanager.appnetwork.AppnetworkManager manager) {
        manager.appLinks().listByResourceGroup("test_rg", com.azure.core.util.Context.NONE);
    }
}
```

### AppLinks_Update

```java
import com.azure.resourcemanager.appnetwork.models.AppLink;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AppLinks Update.
 */
public final class AppLinksUpdateSamples {
    /*
     * x-ms-original-file: 2025-08-01-preview/AppLinks_Update.json
     */
    /**
     * Sample code: AppLinks_Update.
     * 
     * @param manager Entry point to AppnetworkManager.
     */
    public static void appLinksUpdate(com.azure.resourcemanager.appnetwork.AppnetworkManager manager) {
        AppLink resource = manager.appLinks()
            .getByResourceGroupWithResponse("test_rg", "applink-test-01", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("environment", "production", "cost-center", "platform")).apply();
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

### AvailableVersions_ListByLocation

```java
/**
 * Samples for AvailableVersions ListByLocation.
 */
public final class AvailableVersionsListByLocationSamples {
    /*
     * x-ms-original-file: 2025-08-01-preview/AvailableVersions_ListByLocationWithFilter.json
     */
    /**
     * Sample code: AvailableVersions_ListByLocationWithFilter.
     * 
     * @param manager Entry point to AppnetworkManager.
     */
    public static void
        availableVersionsListByLocationWithFilter(com.azure.resourcemanager.appnetwork.AppnetworkManager manager) {
        manager.availableVersions().listByLocation("westus2", "1.28", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-08-01-preview/AvailableVersions_ListByLocation.json
     */
    /**
     * Sample code: AvailableVersions_ListByLocation.
     * 
     * @param manager Entry point to AppnetworkManager.
     */
    public static void availableVersionsListByLocation(com.azure.resourcemanager.appnetwork.AppnetworkManager manager) {
        manager.availableVersions().listByLocation("westus2", null, com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-08-01-preview/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to AppnetworkManager.
     */
    public static void operationsList(com.azure.resourcemanager.appnetwork.AppnetworkManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### UpgradeHistories_ListByAppLinkMember

```java
/**
 * Samples for UpgradeHistories ListByAppLinkMember.
 */
public final class UpgradeHistoriesListByAppLinkMemberSamples {
    /*
     * x-ms-original-file: 2025-08-01-preview/UpgradeHistories_ListByAppLinkMember.json
     */
    /**
     * Sample code: UpgradeHistories_ListByAppLinkMember.
     * 
     * @param manager Entry point to AppnetworkManager.
     */
    public static void
        upgradeHistoriesListByAppLinkMember(com.azure.resourcemanager.appnetwork.AppnetworkManager manager) {
        manager.upgradeHistories()
            .listByAppLinkMember("test_rg", "applink-test-01", "member-01", com.azure.core.util.Context.NONE);
    }
}
```

