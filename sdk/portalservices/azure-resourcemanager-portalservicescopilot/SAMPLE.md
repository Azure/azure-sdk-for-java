# Code snippets and samples


## CopilotSettings

- [CreateOrUpdate](#copilotsettings_createorupdate)
- [Delete](#copilotsettings_delete)
- [Get](#copilotsettings_get)
- [Update](#copilotsettings_update)

## Operations

- [List](#operations_list)
### CopilotSettings_CreateOrUpdate

```java
import com.azure.resourcemanager.portalservicescopilot.fluent.models.CopilotSettingsResourceInner;
import com.azure.resourcemanager.portalservicescopilot.models.CopilotSettingsProperties;

/**
 * Samples for CopilotSettings CreateOrUpdate.
 */
public final class CopilotSettingsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/CopilotSettings_CreateOrUpdate.json
     */
    /**
     * Sample code: Create a new Copilot settings or update an existing one.
     * 
     * @param manager Entry point to PortalServicesCopilotManager.
     */
    public static void createANewCopilotSettingsOrUpdateAnExistingOne(
        com.azure.resourcemanager.portalservicescopilot.PortalServicesCopilotManager manager) {
        manager.copilotSettings()
            .createOrUpdateWithResponse(new CopilotSettingsResourceInner().withProperties(
                new CopilotSettingsProperties().withAccessControlEnabled(true)), com.azure.core.util.Context.NONE);
    }
}
```

### CopilotSettings_Delete

```java
/**
 * Samples for CopilotSettings Delete.
 */
public final class CopilotSettingsDeleteSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/CopilotSettings_Delete.json
     */
    /**
     * Sample code: Delete Copilot Settings.
     * 
     * @param manager Entry point to PortalServicesCopilotManager.
     */
    public static void
        deleteCopilotSettings(com.azure.resourcemanager.portalservicescopilot.PortalServicesCopilotManager manager) {
        manager.copilotSettings().deleteWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### CopilotSettings_Get

```java
/**
 * Samples for CopilotSettings Get.
 */
public final class CopilotSettingsGetSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/CopilotSettings_Get.json
     */
    /**
     * Sample code: Get Copilot Settings.
     * 
     * @param manager Entry point to PortalServicesCopilotManager.
     */
    public static void
        getCopilotSettings(com.azure.resourcemanager.portalservicescopilot.PortalServicesCopilotManager manager) {
        manager.copilotSettings().getWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### CopilotSettings_Update

```java
import com.azure.resourcemanager.portalservicescopilot.models.CopilotSettingsResourceUpdate;
import com.azure.resourcemanager.portalservicescopilot.models.CopilotSettingsResourceUpdateProperties;

/**
 * Samples for CopilotSettings Update.
 */
public final class CopilotSettingsUpdateSamples {
    /*
     * x-ms-original-file: 2024-04-01-preview/CopilotSettings_Update.json
     */
    /**
     * Sample code: Update Copilot Settings.
     * 
     * @param manager Entry point to PortalServicesCopilotManager.
     */
    public static void
        updateCopilotSettings(com.azure.resourcemanager.portalservicescopilot.PortalServicesCopilotManager manager) {
        manager.copilotSettings()
            .updateWithResponse(
                new CopilotSettingsResourceUpdate()
                    .withProperties(new CopilotSettingsResourceUpdateProperties().withAccessControlEnabled(true)),
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2024-04-01-preview/Operations_List.json
     */
    /**
     * Sample code: List the operations for the Microsoft.PortalServices provider.
     * 
     * @param manager Entry point to PortalServicesCopilotManager.
     */
    public static void listTheOperationsForTheMicrosoftPortalServicesProvider(
        com.azure.resourcemanager.portalservicescopilot.PortalServicesCopilotManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

