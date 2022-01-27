# Code snippets and samples


## BestPractices

- [Get](#bestpractices_get)
- [ListByTenant](#bestpractices_listbytenant)

## BestPracticesVersions

- [Get](#bestpracticesversions_get)
- [ListByTenant](#bestpracticesversions_listbytenant)

## ConfigurationProfileAssignments

- [CreateOrUpdate](#configurationprofileassignments_createorupdate)
- [Delete](#configurationprofileassignments_delete)
- [Get](#configurationprofileassignments_get)
- [List](#configurationprofileassignments_list)
- [ListByResourceGroup](#configurationprofileassignments_listbyresourcegroup)

## ConfigurationProfiles

- [CreateOrUpdate](#configurationprofiles_createorupdate)
- [Delete](#configurationprofiles_delete)
- [GetByResourceGroup](#configurationprofiles_getbyresourcegroup)
- [List](#configurationprofiles_list)
- [ListByResourceGroup](#configurationprofiles_listbyresourcegroup)
- [Update](#configurationprofiles_update)

## ConfigurationProfilesVersions

- [CreateOrUpdate](#configurationprofilesversions_createorupdate)
- [Delete](#configurationprofilesversions_delete)
- [Get](#configurationprofilesversions_get)
- [ListChildResources](#configurationprofilesversions_listchildresources)
- [Update](#configurationprofilesversions_update)

## Operations

- [List](#operations_list)

## Reports

- [Get](#reports_get)
- [ListByConfigurationProfileAssignments](#reports_listbyconfigurationprofileassignments)
### BestPractices_Get

```java
import com.azure.core.util.Context;

/** Samples for BestPractices Get. */
public final class BestPracticesGetSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/preview/2021-04-30-preview/examples/getBestPractice.json
     */
    /**
     * Sample code: Get an Automanage best practice.
     *
     * @param manager Entry point to AutomanageManager.
     */
    public static void getAnAutomanageBestPractice(com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.bestPractices().getWithResponse("azureBestPracticesProduction", Context.NONE);
    }
}
```

### BestPractices_ListByTenant

```java
import com.azure.core.util.Context;

/** Samples for BestPractices ListByTenant. */
public final class BestPracticesListByTenantSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/preview/2021-04-30-preview/examples/listBestPracticesByTenant.json
     */
    /**
     * Sample code: List Automanage bestPractices.
     *
     * @param manager Entry point to AutomanageManager.
     */
    public static void listAutomanageBestPractices(com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.bestPractices().listByTenant(Context.NONE);
    }
}
```

### BestPracticesVersions_Get

```java
import com.azure.core.util.Context;

/** Samples for BestPracticesVersions Get. */
public final class BestPracticesVersionsGetSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/preview/2021-04-30-preview/examples/getBestPracticeVersion.json
     */
    /**
     * Sample code: Get an Automanage best practice version.
     *
     * @param manager Entry point to AutomanageManager.
     */
    public static void getAnAutomanageBestPracticeVersion(
        com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.bestPracticesVersions().getWithResponse("azureBestPracticesProduction", "version1", Context.NONE);
    }
}
```

### BestPracticesVersions_ListByTenant

```java
import com.azure.core.util.Context;

/** Samples for BestPracticesVersions ListByTenant. */
public final class BestPracticesVersionsListByTenantSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/preview/2021-04-30-preview/examples/listBestPracticesVersionsByTenant.json
     */
    /**
     * Sample code: List Automanage best practices versions.
     *
     * @param manager Entry point to AutomanageManager.
     */
    public static void listAutomanageBestPracticesVersions(
        com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.bestPracticesVersions().listByTenant("azureBestPracticesProduction", Context.NONE);
    }
}
```

### ConfigurationProfileAssignments_CreateOrUpdate

```java
import com.azure.resourcemanager.automanage.models.ConfigurationProfileAssignmentProperties;

/** Samples for ConfigurationProfileAssignments CreateOrUpdate. */
public final class ConfigurationProfileAssignmentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/preview/2021-04-30-preview/examples/createOrUpdateConfigurationProfileAssignment.json
     */
    /**
     * Sample code: Create or update configuration profile assignment.
     *
     * @param manager Entry point to AutomanageManager.
     */
    public static void createOrUpdateConfigurationProfileAssignment(
        com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager
            .configurationProfileAssignments()
            .define("default")
            .withExistingVirtualMachine("myResourceGroupName", "myVMName")
            .withProperties(
                new ConfigurationProfileAssignmentProperties()
                    .withConfigurationProfile(
                        "/providers/Microsoft.Automanage/bestPractices/AzureBestPracticesProduction"))
            .create();
    }
}
```

### ConfigurationProfileAssignments_Delete

```java
import com.azure.core.util.Context;

/** Samples for ConfigurationProfileAssignments Delete. */
public final class ConfigurationProfileAssignmentsDeleteSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/preview/2021-04-30-preview/examples/deleteConfigurationProfileAssignment.json
     */
    /**
     * Sample code: Delete an configuration profile assignment.
     *
     * @param manager Entry point to AutomanageManager.
     */
    public static void deleteAnConfigurationProfileAssignment(
        com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager
            .configurationProfileAssignments()
            .deleteWithResponse("myResourceGroupName", "default", "myVMName", Context.NONE);
    }
}
```

### ConfigurationProfileAssignments_Get

```java
import com.azure.core.util.Context;

/** Samples for ConfigurationProfileAssignments Get. */
public final class ConfigurationProfileAssignmentsGetSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/preview/2021-04-30-preview/examples/getConfigurationProfileAssignment.json
     */
    /**
     * Sample code: Get a configuration profile assignment.
     *
     * @param manager Entry point to AutomanageManager.
     */
    public static void getAConfigurationProfileAssignment(
        com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager
            .configurationProfileAssignments()
            .getWithResponse("myResourceGroupName", "default", "myVMName", Context.NONE);
    }
}
```

### ConfigurationProfileAssignments_List

```java
import com.azure.core.util.Context;

/** Samples for ConfigurationProfileAssignments List. */
public final class ConfigurationProfileAssignmentsListSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/preview/2021-04-30-preview/examples/listConfigurationProfileAssignmentsBySubscription.json
     */
    /**
     * Sample code: List configuration profile assignments by subscription.
     *
     * @param manager Entry point to AutomanageManager.
     */
    public static void listConfigurationProfileAssignmentsBySubscription(
        com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.configurationProfileAssignments().list(Context.NONE);
    }
}
```

### ConfigurationProfileAssignments_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ConfigurationProfileAssignments ListByResourceGroup. */
public final class ConfigurationProfileAssignmentsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/preview/2021-04-30-preview/examples/listConfigurationProfileAssignmentsByResourceGroup.json
     */
    /**
     * Sample code: List configuration profile assignments by resourceGroup.
     *
     * @param manager Entry point to AutomanageManager.
     */
    public static void listConfigurationProfileAssignmentsByResourceGroup(
        com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.configurationProfileAssignments().listByResourceGroup("myResourceGroupName", Context.NONE);
    }
}
```

### ConfigurationProfiles_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.automanage.fluent.models.ConfigurationProfileProperties;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for ConfigurationProfiles CreateOrUpdate. */
public final class ConfigurationProfilesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/preview/2021-04-30-preview/examples/createOrUpdateConfigurationProfile.json
     */
    /**
     * Sample code: Create or update configuration profile.
     *
     * @param manager Entry point to AutomanageManager.
     */
    public static void createOrUpdateConfigurationProfile(
        com.azure.resourcemanager.automanage.AutomanageManager manager) throws IOException {
        manager
            .configurationProfiles()
            .define("customConfigurationProfile")
            .withRegion("East US")
            .withExistingResourceGroup("myResourceGroupName")
            .withTags(mapOf("Organization", "Administration"))
            .withProperties(
                new ConfigurationProfileProperties()
                    .withConfiguration(
                        SerializerFactory
                            .createDefaultManagementSerializerAdapter()
                            .deserialize(
                                "{\"Antimalware/Enable\":false,\"AzureSecurityCenter/Enable\":true,\"Backup/Enable\":false,\"BootDiagnostics/Enable\":true,\"ChangeTrackingAndInventory/Enable\":true,\"GuestConfiguration/Enable\":true,\"LogAnalytics/Enable\":true,\"UpdateManagement/Enable\":true,\"VMInsights/Enable\":true}",
                                Object.class,
                                SerializerEncoding.JSON))
                    .withOverrides(
                        Arrays
                            .asList(
                                SerializerFactory
                                    .createDefaultManagementSerializerAdapter()
                                    .deserialize(
                                        "{\"if\":{\"equals\":\"eastus\",\"field\":\"$.location\"},\"priority\":100,\"then\":{\"LogAnalytics/Enable\":true,\"LogAnalytics/Reprovision\":true,\"LogAnalytics/Workspace\":\"/subscriptions/abc/resourceGroups/xyz/providers/Microsoft.La/Workspaces/eastus\"}}",
                                        Object.class,
                                        SerializerEncoding.JSON),
                                SerializerFactory
                                    .createDefaultManagementSerializerAdapter()
                                    .deserialize(
                                        "{\"if\":{\"equals\":\"centralcanada\",\"field\":\"$.location\"},\"priority\":200,\"then\":{\"LogAnalytics/Enable\":true,\"LogAnalytics/Reprovision\":true,\"LogAnalytics/Workspace\":\"/subscriptions/abc/resourceGroups/xyz/providers/Microsoft.La/Workspaces/centralcanada\"}}",
                                        Object.class,
                                        SerializerEncoding.JSON))))
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

### ConfigurationProfiles_Delete

```java
import com.azure.core.util.Context;

/** Samples for ConfigurationProfiles Delete. */
public final class ConfigurationProfilesDeleteSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/preview/2021-04-30-preview/examples/deleteConfigurationProfile.json
     */
    /**
     * Sample code: Delete a configuration profile.
     *
     * @param manager Entry point to AutomanageManager.
     */
    public static void deleteAConfigurationProfile(com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.configurationProfiles().deleteWithResponse("rg", "customConfigurationProfile", Context.NONE);
    }
}
```

### ConfigurationProfiles_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ConfigurationProfiles GetByResourceGroup. */
public final class ConfigurationProfilesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/preview/2021-04-30-preview/examples/getConfigurationProfile.json
     */
    /**
     * Sample code: Get a configuration profile.
     *
     * @param manager Entry point to AutomanageManager.
     */
    public static void getAConfigurationProfile(com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager
            .configurationProfiles()
            .getByResourceGroupWithResponse("myResourceGroupName", "customConfigurationProfile", Context.NONE);
    }
}
```

### ConfigurationProfiles_List

```java
import com.azure.core.util.Context;

/** Samples for ConfigurationProfiles List. */
public final class ConfigurationProfilesListSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/preview/2021-04-30-preview/examples/listConfigurationProfilesBySubscription.json
     */
    /**
     * Sample code: List configuration profiles by subscription.
     *
     * @param manager Entry point to AutomanageManager.
     */
    public static void listConfigurationProfilesBySubscription(
        com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.configurationProfiles().list(Context.NONE);
    }
}
```

### ConfigurationProfiles_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ConfigurationProfiles ListByResourceGroup. */
public final class ConfigurationProfilesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/preview/2021-04-30-preview/examples/listConfigurationProfilesByResourceGroup.json
     */
    /**
     * Sample code: List configuration profiles by resource group.
     *
     * @param manager Entry point to AutomanageManager.
     */
    public static void listConfigurationProfilesByResourceGroup(
        com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.configurationProfiles().listByResourceGroup("myResourceGroupName", Context.NONE);
    }
}
```

### ConfigurationProfiles_Update

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.automanage.fluent.models.ConfigurationProfileProperties;
import com.azure.resourcemanager.automanage.models.ConfigurationProfile;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Samples for ConfigurationProfiles Update. */
public final class ConfigurationProfilesUpdateSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/preview/2021-04-30-preview/examples/updateConfigurationProfile.json
     */
    /**
     * Sample code: Update configuration profile.
     *
     * @param manager Entry point to AutomanageManager.
     */
    public static void updateConfigurationProfile(com.azure.resourcemanager.automanage.AutomanageManager manager)
        throws IOException {
        ConfigurationProfile resource =
            manager
                .configurationProfiles()
                .getByResourceGroupWithResponse("myResourceGroupName", "customConfigurationProfile", Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("Organization", "Administration"))
            .withProperties(
                new ConfigurationProfileProperties()
                    .withConfiguration(
                        SerializerFactory
                            .createDefaultManagementSerializerAdapter()
                            .deserialize(
                                "{\"Antimalware/Enable\":false,\"AzureSecurityCenter/Enable\":true,\"Backup/Enable\":false,\"BootDiagnostics/Enable\":true,\"ChangeTrackingAndInventory/Enable\":true,\"GuestConfiguration/Enable\":true,\"LogAnalytics/Enable\":true,\"UpdateManagement/Enable\":true,\"VMInsights/Enable\":true}",
                                Object.class,
                                SerializerEncoding.JSON)))
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

### ConfigurationProfilesVersions_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.automanage.fluent.models.ConfigurationProfileInner;
import com.azure.resourcemanager.automanage.fluent.models.ConfigurationProfileProperties;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for ConfigurationProfilesVersions CreateOrUpdate. */
public final class ConfigurationProfilesVersionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/preview/2021-04-30-preview/examples/createOrUpdateConfigurationProfileVersion.json
     */
    /**
     * Sample code: Create or update configuration profile version.
     *
     * @param manager Entry point to AutomanageManager.
     */
    public static void createOrUpdateConfigurationProfileVersion(
        com.azure.resourcemanager.automanage.AutomanageManager manager) throws IOException {
        manager
            .configurationProfilesVersions()
            .createOrUpdateWithResponse(
                "customConfigurationProfile",
                "version1",
                "myResourceGroupName",
                new ConfigurationProfileInner()
                    .withLocation("East US")
                    .withTags(mapOf("Organization", "Administration"))
                    .withProperties(
                        new ConfigurationProfileProperties()
                            .withConfiguration(
                                SerializerFactory
                                    .createDefaultManagementSerializerAdapter()
                                    .deserialize(
                                        "{\"Antimalware/Enable\":false,\"AzureSecurityCenter/Enable\":true,\"Backup/Enable\":false,\"BootDiagnostics/Enable\":true,\"ChangeTrackingAndInventory/Enable\":true,\"GuestConfiguration/Enable\":true,\"LogAnalytics/Enable\":true,\"UpdateManagement/Enable\":true,\"VMInsights/Enable\":true}",
                                        Object.class,
                                        SerializerEncoding.JSON))
                            .withOverrides(
                                Arrays
                                    .asList(
                                        SerializerFactory
                                            .createDefaultManagementSerializerAdapter()
                                            .deserialize(
                                                "{\"if\":{\"equals\":\"eastus\",\"field\":\"$.location\"},\"priority\":100,\"then\":{\"LogAnalytics/Enable\":true,\"LogAnalytics/Reprovision\":true,\"LogAnalytics/Workspace\":\"/subscriptions/abc/resourceGroups/xyz/providers/Microsoft.La/Workspaces/eastus\"}}",
                                                Object.class,
                                                SerializerEncoding.JSON),
                                        SerializerFactory
                                            .createDefaultManagementSerializerAdapter()
                                            .deserialize(
                                                "{\"if\":{\"equals\":\"centralcanada\",\"field\":\"$.location\"},\"priority\":200,\"then\":{\"LogAnalytics/Enable\":true,\"LogAnalytics/Reprovision\":true,\"LogAnalytics/Workspace\":\"/subscriptions/abc/resourceGroups/xyz/providers/Microsoft.La/Workspaces/centralcanada\"}}",
                                                Object.class,
                                                SerializerEncoding.JSON)))),
                Context.NONE);
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

### ConfigurationProfilesVersions_Delete

```java
import com.azure.core.util.Context;

/** Samples for ConfigurationProfilesVersions Delete. */
public final class ConfigurationProfilesVersionsDeleteSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/preview/2021-04-30-preview/examples/deleteConfigurationProfileVersion.json
     */
    /**
     * Sample code: Delete a configuration profile version.
     *
     * @param manager Entry point to AutomanageManager.
     */
    public static void deleteAConfigurationProfileVersion(
        com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager
            .configurationProfilesVersions()
            .deleteWithResponse("rg", "customConfigurationProfile", "version1", Context.NONE);
    }
}
```

### ConfigurationProfilesVersions_Get

```java
import com.azure.core.util.Context;

/** Samples for ConfigurationProfilesVersions Get. */
public final class ConfigurationProfilesVersionsGetSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/preview/2021-04-30-preview/examples/getConfigurationProfileVersion.json
     */
    /**
     * Sample code: Get a configuration profile version.
     *
     * @param manager Entry point to AutomanageManager.
     */
    public static void getAConfigurationProfileVersion(com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager
            .configurationProfilesVersions()
            .getWithResponse("customConfigurationProfile", "version1", "myResourceGroupName", Context.NONE);
    }
}
```

### ConfigurationProfilesVersions_ListChildResources

```java
import com.azure.core.util.Context;

/** Samples for ConfigurationProfilesVersions ListChildResources. */
public final class ConfigurationProfilesVersionsListChildResourcesSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/preview/2021-04-30-preview/examples/listConfigurationProfileVersions.json
     */
    /**
     * Sample code: List configuration profile versions by configuration profile.
     *
     * @param manager Entry point to AutomanageManager.
     */
    public static void listConfigurationProfileVersionsByConfigurationProfile(
        com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager
            .configurationProfilesVersions()
            .listChildResources("customConfigurationProfile", "myResourceGroupName", Context.NONE);
    }
}
```

### ConfigurationProfilesVersions_Update

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.automanage.fluent.models.ConfigurationProfileProperties;
import com.azure.resourcemanager.automanage.models.ConfigurationProfileUpdate;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Samples for ConfigurationProfilesVersions Update. */
public final class ConfigurationProfilesVersionsUpdateSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/preview/2021-04-30-preview/examples/updateConfigurationProfileVersion.json
     */
    /**
     * Sample code: Update configuration profile version.
     *
     * @param manager Entry point to AutomanageManager.
     */
    public static void updateConfigurationProfileVersion(com.azure.resourcemanager.automanage.AutomanageManager manager)
        throws IOException {
        manager
            .configurationProfilesVersions()
            .updateWithResponse(
                "customConfigurationProfile",
                "version1",
                "myResourceGroupName",
                new ConfigurationProfileUpdate()
                    .withTags(mapOf("Organization", "Administration"))
                    .withProperties(
                        new ConfigurationProfileProperties()
                            .withConfiguration(
                                SerializerFactory
                                    .createDefaultManagementSerializerAdapter()
                                    .deserialize(
                                        "{\"Antimalware/Enable\":false,\"AzureSecurityCenter/Enable\":true,\"Backup/Enable\":false,\"BootDiagnostics/Enable\":true,\"ChangeTrackingAndInventory/Enable\":true,\"GuestConfiguration/Enable\":true,\"LogAnalytics/Enable\":true,\"UpdateManagement/Enable\":true,\"VMInsights/Enable\":true}",
                                        Object.class,
                                        SerializerEncoding.JSON))),
                Context.NONE);
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

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/preview/2021-04-30-preview/examples/listOperations.json
     */
    /**
     * Sample code: Lists all of the available Automanage REST API operations.
     *
     * @param manager Entry point to AutomanageManager.
     */
    public static void listsAllOfTheAvailableAutomanageRESTAPIOperations(
        com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### Reports_Get

```java
import com.azure.core.util.Context;

/** Samples for Reports Get. */
public final class ReportsGetSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/preview/2021-04-30-preview/examples/getReport.json
     */
    /**
     * Sample code: Get a report for a configuration profile assignment.
     *
     * @param manager Entry point to AutomanageManager.
     */
    public static void getAReportForAConfigurationProfileAssignment(
        com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager
            .reports()
            .getWithResponse(
                "myResourceGroupName", "default", "b4e9ee6b-1717-4ff0-a8d2-e6d72c33d5f4", "myVMName", Context.NONE);
    }
}
```

### Reports_ListByConfigurationProfileAssignments

```java
import com.azure.core.util.Context;

/** Samples for Reports ListByConfigurationProfileAssignments. */
public final class ReportsListByConfigurationProfileAssignmentsSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/preview/2021-04-30-preview/examples/listReportsByconfigurationProfileAssignment.json
     */
    /**
     * Sample code: List reports by configuration profiles assignment.
     *
     * @param manager Entry point to AutomanageManager.
     */
    public static void listReportsByConfigurationProfilesAssignment(
        com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager
            .reports()
            .listByConfigurationProfileAssignments("myResourceGroupName", "default", "myVMName", Context.NONE);
    }
}
```

