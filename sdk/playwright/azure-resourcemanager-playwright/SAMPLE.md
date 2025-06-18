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
- [Delete](#playwrightworkspaces_delete)
- [Update](#playwrightworkspaces_update)
### Operations_List

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
     * x-ms-original-file: 2025-07-01-preview/PlaywrightWorkspaces_Update.json
     */
    /**
     * Sample code: PlaywrightWorkspaces_Update.
     * 
     * @param manager Entry point to MicrosoftPlaywrightServiceManager.
     */
    public static void
        playwrightWorkspacesUpdate(com.azure.resourcemanager.playwright.MicrosoftPlaywrightServiceManager manager) {
        PlaywrightWorkspace resource = manager.playwrightWorkspaces()
            .getByResourceGroupWithResponse("dummyrg", "myWorkspace", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("Team", "Dev Exp", "Division", "LT"))
            .withProperties(new PlaywrightWorkspaceUpdateProperties().withRegionalAffinity(EnablementStatus.DISABLED))
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

### PlaywrightQuotas_Get

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to MicrosoftPlaywrightServiceManager.
     */
    public static void operationsList(com.azure.resourcemanager.playwright.MicrosoftPlaywrightServiceManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PlaywrightQuotas_ListBySubscription

```java
/**
 * Samples for PlaywrightWorkspaceQuotas ListByPlaywrightWorkspace.
 */
public final class PlaywrightWorkspaceQuotasListByPlaywrightWorkspaceSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/PlaywrightWorkspaceQuotas_ListByPlaywrightWorkspace.json
     */
    /**
     * Sample code: PlaywrightWorkspaceQuotas_ListByPlaywrightWorkspace.
     * 
     * @param manager Entry point to MicrosoftPlaywrightServiceManager.
     */
    public static void playwrightWorkspaceQuotasListByPlaywrightWorkspace(
        com.azure.resourcemanager.playwright.MicrosoftPlaywrightServiceManager manager) {
        manager.playwrightWorkspaceQuotas()
            .listByPlaywrightWorkspace("dummyrg", "myWorkspace", com.azure.core.util.Context.NONE);
    }
}
```

### PlaywrightWorkspaceQuotas_Get

```java
/**
 * Samples for PlaywrightQuotas ListBySubscription.
 */
public final class PlaywrightQuotasListBySubscriptionSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/PlaywrightQuotas_ListBySubscription.json
     */
    /**
     * Sample code: PlaywrightQuotas_ListBySubscription.
     * 
     * @param manager Entry point to MicrosoftPlaywrightServiceManager.
     */
    public static void playwrightQuotasListBySubscription(
        com.azure.resourcemanager.playwright.MicrosoftPlaywrightServiceManager manager) {
        manager.playwrightQuotas().listBySubscription("eastus", com.azure.core.util.Context.NONE);
    }
}
```

### PlaywrightWorkspaceQuotas_ListByPlaywrightWorkspace

```java
/**
 * Samples for PlaywrightWorkspaces Delete.
 */
public final class PlaywrightWorkspacesDeleteSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/PlaywrightWorkspaces_Delete.json
     */
    /**
     * Sample code: PlaywrightWorkspaces_Delete.
     * 
     * @param manager Entry point to MicrosoftPlaywrightServiceManager.
     */
    public static void
        playwrightWorkspacesDelete(com.azure.resourcemanager.playwright.MicrosoftPlaywrightServiceManager manager) {
        manager.playwrightWorkspaces().delete("dummyrg", "myWorkspace", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-07-01-preview/PlaywrightWorkspaces_CheckNameAvailability.json
     */
    /**
     * Sample code: PlaywrightWorkspaces_CheckNameAvailability.
     * 
     * @param manager Entry point to MicrosoftPlaywrightServiceManager.
     */
    public static void playwrightWorkspacesCheckNameAvailability(
        com.azure.resourcemanager.playwright.MicrosoftPlaywrightServiceManager manager) {
        manager.playwrightWorkspaces()
            .checkNameAvailabilityWithResponse(new CheckNameAvailabilityRequest().withName("dummyName")
                .withType("Microsoft.LoadTestService/PlaywrightWorkspaces"), com.azure.core.util.Context.NONE);
    }
}
```

### PlaywrightWorkspaces_Delete

```java
import com.azure.resourcemanager.playwright.models.QuotaName;

/**
 * Samples for PlaywrightQuotas Get.
 */
public final class PlaywrightQuotasGetSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/PlaywrightQuotas_Get.json
     */
    /**
     * Sample code: PlaywrightQuotas_Get.
     * 
     * @param manager Entry point to MicrosoftPlaywrightServiceManager.
     */
    public static void
        playwrightQuotasGet(com.azure.resourcemanager.playwright.MicrosoftPlaywrightServiceManager manager) {
        manager.playwrightQuotas()
            .getWithResponse("eastus", QuotaName.EXECUTION_MINUTES, com.azure.core.util.Context.NONE);
    }
}
```

### PlaywrightWorkspaces_Update

```java
import com.azure.resourcemanager.playwright.models.QuotaName;

/**
 * Samples for PlaywrightWorkspaceQuotas Get.
 */
public final class PlaywrightWorkspaceQuotasGetSamples {
    /*
     * x-ms-original-file: 2025-07-01-preview/PlaywrightWorkspaceQuotas_Get.json
     */
    /**
     * Sample code: PlaywrightWorkspaceQuotas_Get.
     * 
     * @param manager Entry point to MicrosoftPlaywrightServiceManager.
     */
    public static void
        playwrightWorkspaceQuotasGet(com.azure.resourcemanager.playwright.MicrosoftPlaywrightServiceManager manager) {
        manager.playwrightWorkspaceQuotas()
            .getWithResponse("dummyrg", "myWorkspace", QuotaName.EXECUTION_MINUTES, com.azure.core.util.Context.NONE);
    }
}
```

