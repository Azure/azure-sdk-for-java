# Code snippets and samples


## Operations

- [List](#operations_list)

## PlaywrightQuotas

- [Get](#playwrightquotas_get)
- [ListBySubscription](#playwrightquotas_listbysubscription)

## PlaywrightWorkspaceQuotas

- [Get](#playwrightworkspacequotas_get)
- [ListByPlaywrightWorkspace](#playwrightworkspacequotas_listbyplaywrightworkspace)

## PlaywrightWorkspaces

- [CheckNameAvailability](#playwrightworkspaces_checknameavailability)
- [CreateOrUpdate](#playwrightworkspaces_createorupdate)
- [Delete](#playwrightworkspaces_delete)
- [GetByResourceGroup](#playwrightworkspaces_getbyresourcegroup)
- [List](#playwrightworkspaces_list)
- [ListByResourceGroup](#playwrightworkspaces_listbyresourcegroup)
- [Update](#playwrightworkspaces_update)
### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to PlaywrightManager.
     */
    public static void operationsList(com.azure.resourcemanager.playwright.PlaywrightManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PlaywrightQuotas_Get

```java
import com.azure.resourcemanager.playwright.models.QuotaName;

/**
 * Samples for PlaywrightQuotas Get.
 */
public final class PlaywrightQuotasGetSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/PlaywrightQuotas_Get.json
     */
    /**
     * Sample code: PlaywrightQuotas_Get.
     * 
     * @param manager Entry point to PlaywrightManager.
     */
    public static void playwrightQuotasGet(com.azure.resourcemanager.playwright.PlaywrightManager manager) {
        manager.playwrightQuotas()
            .getWithResponse("eastus", QuotaName.EXECUTION_MINUTES, com.azure.core.util.Context.NONE);
    }
}
```

### PlaywrightQuotas_ListBySubscription

```java
/**
 * Samples for PlaywrightQuotas ListBySubscription.
 */
public final class PlaywrightQuotasListBySubscriptionSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/PlaywrightQuotas_ListBySubscription.json
     */
    /**
     * Sample code: PlaywrightQuotas_ListBySubscription.
     * 
     * @param manager Entry point to PlaywrightManager.
     */
    public static void
        playwrightQuotasListBySubscription(com.azure.resourcemanager.playwright.PlaywrightManager manager) {
        manager.playwrightQuotas().listBySubscription("eastus", com.azure.core.util.Context.NONE);
    }
}
```

### PlaywrightWorkspaceQuotas_Get

```java
import com.azure.resourcemanager.playwright.models.QuotaName;

/**
 * Samples for PlaywrightWorkspaceQuotas Get.
 */
public final class PlaywrightWorkspaceQuotasGetSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/PlaywrightWorkspaceQuotas_Get.json
     */
    /**
     * Sample code: PlaywrightWorkspaceQuotas_Get.
     * 
     * @param manager Entry point to PlaywrightManager.
     */
    public static void playwrightWorkspaceQuotasGet(com.azure.resourcemanager.playwright.PlaywrightManager manager) {
        manager.playwrightWorkspaceQuotas()
            .getWithResponse("dummyrg", "myWorkspace", QuotaName.EXECUTION_MINUTES, com.azure.core.util.Context.NONE);
    }
}
```

### PlaywrightWorkspaceQuotas_ListByPlaywrightWorkspace

```java
/**
 * Samples for PlaywrightWorkspaceQuotas ListByPlaywrightWorkspace.
 */
public final class PlaywrightWorkspaceQuotasListByPlaywrightWorkspaceSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/PlaywrightWorkspaceQuotas_ListByPlaywrightWorkspace.json
     */
    /**
     * Sample code: PlaywrightWorkspaceQuotas_ListByPlaywrightWorkspace.
     * 
     * @param manager Entry point to PlaywrightManager.
     */
    public static void playwrightWorkspaceQuotasListByPlaywrightWorkspace(
        com.azure.resourcemanager.playwright.PlaywrightManager manager) {
        manager.playwrightWorkspaceQuotas()
            .listByPlaywrightWorkspace("dummyrg", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### PlaywrightWorkspaces_CheckNameAvailability

```java
import com.azure.resourcemanager.playwright.models.CheckNameAvailabilityRequest;

/**
 * Samples for PlaywrightWorkspaces CheckNameAvailability.
 */
public final class PlaywrightWorkspacesCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/PlaywrightWorkspaces_CheckNameAvailability.json
     */
    /**
     * Sample code: PlaywrightWorkspaces_CheckNameAvailability.
     * 
     * @param manager Entry point to PlaywrightManager.
     */
    public static void
        playwrightWorkspacesCheckNameAvailability(com.azure.resourcemanager.playwright.PlaywrightManager manager) {
        manager.playwrightWorkspaces()
            .checkNameAvailabilityWithResponse(new CheckNameAvailabilityRequest().withName("dummyName")
                .withType("Microsoft.LoadTestService/PlaywrightWorkspaces"), com.azure.core.util.Context.NONE);
    }
}
```

### PlaywrightWorkspaces_CreateOrUpdate

```java
import com.azure.resourcemanager.playwright.models.EnablementStatus;
import com.azure.resourcemanager.playwright.models.PlaywrightWorkspaceProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for PlaywrightWorkspaces CreateOrUpdate.
 */
public final class PlaywrightWorkspacesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/PlaywrightWorkspaces_CreateOrUpdate.json
     */
    /**
     * Sample code: PlaywrightWorkspaces_CreateOrUpdate.
     * 
     * @param manager Entry point to PlaywrightManager.
     */
    public static void
        playwrightWorkspacesCreateOrUpdate(com.azure.resourcemanager.playwright.PlaywrightManager manager) {
        manager.playwrightWorkspaces()
            .define("myWorkspace")
            .withRegion("westus3")
            .withExistingResourceGroup("dummyrg")
            .withTags(mapOf("Team", "Dev Exp"))
            .withProperties(new PlaywrightWorkspaceProperties().withRegionalAffinity(EnablementStatus.ENABLED)
                .withLocalAuth(EnablementStatus.ENABLED)
                .withReporting(EnablementStatus.ENABLED)
                .withStorageUri("https://examplestorageaccount.blob.core.windows.net"))
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

### PlaywrightWorkspaces_Delete

```java
/**
 * Samples for PlaywrightWorkspaces Delete.
 */
public final class PlaywrightWorkspacesDeleteSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/PlaywrightWorkspaces_Delete.json
     */
    /**
     * Sample code: PlaywrightWorkspaces_Delete.
     * 
     * @param manager Entry point to PlaywrightManager.
     */
    public static void playwrightWorkspacesDelete(com.azure.resourcemanager.playwright.PlaywrightManager manager) {
        manager.playwrightWorkspaces().delete("dummyrg", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### PlaywrightWorkspaces_GetByResourceGroup

```java
/**
 * Samples for PlaywrightWorkspaces GetByResourceGroup.
 */
public final class PlaywrightWorkspacesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/PlaywrightWorkspaces_Get.json
     */
    /**
     * Sample code: PlaywrightWorkspaces_Get.
     * 
     * @param manager Entry point to PlaywrightManager.
     */
    public static void playwrightWorkspacesGet(com.azure.resourcemanager.playwright.PlaywrightManager manager) {
        manager.playwrightWorkspaces()
            .getByResourceGroupWithResponse("dummyrg", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### PlaywrightWorkspaces_List

```java
/**
 * Samples for PlaywrightWorkspaces List.
 */
public final class PlaywrightWorkspacesListSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/PlaywrightWorkspaces_ListBySubscription.json
     */
    /**
     * Sample code: PlaywrightWorkspaces_ListBySubscription.
     * 
     * @param manager Entry point to PlaywrightManager.
     */
    public static void
        playwrightWorkspacesListBySubscription(com.azure.resourcemanager.playwright.PlaywrightManager manager) {
        manager.playwrightWorkspaces().list(com.azure.core.util.Context.NONE);
    }
}
```

### PlaywrightWorkspaces_ListByResourceGroup

```java
/**
 * Samples for PlaywrightWorkspaces ListByResourceGroup.
 */
public final class PlaywrightWorkspacesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/PlaywrightWorkspaces_ListByResourceGroup.json
     */
    /**
     * Sample code: PlaywrightWorkspaces_ListByResourceGroup.
     * 
     * @param manager Entry point to PlaywrightManager.
     */
    public static void
        playwrightWorkspacesListByResourceGroup(com.azure.resourcemanager.playwright.PlaywrightManager manager) {
        manager.playwrightWorkspaces().listByResourceGroup("dummyrg", com.azure.core.util.Context.NONE);
    }
}
```

### PlaywrightWorkspaces_Update

```java
import com.azure.resourcemanager.playwright.models.EnablementStatus;
import com.azure.resourcemanager.playwright.models.PlaywrightWorkspace;
import com.azure.resourcemanager.playwright.models.PlaywrightWorkspaceUpdateProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for PlaywrightWorkspaces Update.
 */
public final class PlaywrightWorkspacesUpdateSamples {
    /*
     * x-ms-original-file: 2026-02-01-preview/PlaywrightWorkspaces_Update.json
     */
    /**
     * Sample code: PlaywrightWorkspaces_Update.
     * 
     * @param manager Entry point to PlaywrightManager.
     */
    public static void playwrightWorkspacesUpdate(com.azure.resourcemanager.playwright.PlaywrightManager manager) {
        PlaywrightWorkspace resource = manager.playwrightWorkspaces()
            .getByResourceGroupWithResponse("dummyrg", "myWorkspace", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("Team", "Dev Exp", "Division", "LT"))
            .withProperties(new PlaywrightWorkspaceUpdateProperties().withRegionalAffinity(EnablementStatus.DISABLED)
                .withReporting(EnablementStatus.DISABLED))
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

