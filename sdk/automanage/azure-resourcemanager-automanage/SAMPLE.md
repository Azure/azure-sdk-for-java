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
- [ListByClusterName](#configurationprofileassignments_listbyclustername)
- [ListByMachineName](#configurationprofileassignments_listbymachinename)
- [ListByResourceGroup](#configurationprofileassignments_listbyresourcegroup)
- [ListByVirtualMachines](#configurationprofileassignments_listbyvirtualmachines)

## ConfigurationProfileHciAssignments

- [CreateOrUpdate](#configurationprofilehciassignments_createorupdate)
- [Delete](#configurationprofilehciassignments_delete)
- [Get](#configurationprofilehciassignments_get)

## ConfigurationProfileHcrpAssignments

- [CreateOrUpdate](#configurationprofilehcrpassignments_createorupdate)
- [Delete](#configurationprofilehcrpassignments_delete)
- [Get](#configurationprofilehcrpassignments_get)

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

## HciReports

- [Get](#hcireports_get)
- [ListByConfigurationProfileAssignments](#hcireports_listbyconfigurationprofileassignments)

## HcrpReports

- [Get](#hcrpreports_get)
- [ListByConfigurationProfileAssignments](#hcrpreports_listbyconfigurationprofileassignments)

## Operations

- [List](#operations_list)

## Reports

- [Get](#reports_get)
- [ListByConfigurationProfileAssignments](#reports_listbyconfigurationprofileassignments)

## ServicePrincipals

- [Get](#serviceprincipals_get)
- [List](#serviceprincipals_list)
### BestPractices_Get

```java
/**
 * Samples for BestPractices Get.
 */
public final class BestPracticesGetSamples {
    /*
     * x-ms-original-file:
     * specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/getBestPractice.json
     */
    /**
     * Sample code: Get an Automanage best practice.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void getAnAutomanageBestPractice(com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.bestPractices().getWithResponse("azureBestPracticesProduction", com.azure.core.util.Context.NONE);
    }
}
```

### BestPractices_ListByTenant

```java
/**
 * Samples for BestPractices ListByTenant.
 */
public final class BestPracticesListByTenantSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/
     * listBestPracticesByTenant.json
     */
    /**
     * Sample code: List Automanage bestPractices.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void listAutomanageBestPractices(com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.bestPractices().listByTenant(com.azure.core.util.Context.NONE);
    }
}
```

### BestPracticesVersions_Get

```java
/**
 * Samples for BestPracticesVersions Get.
 */
public final class BestPracticesVersionsGetSamples {
    /*
     * x-ms-original-file:
     * specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/getBestPracticeVersion.
     * json
     */
    /**
     * Sample code: Get an Automanage best practice version.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void
        getAnAutomanageBestPracticeVersion(com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.bestPracticesVersions()
            .getWithResponse("azureBestPracticesProduction", "version1", com.azure.core.util.Context.NONE);
    }
}
```

### BestPracticesVersions_ListByTenant

```java
/**
 * Samples for BestPracticesVersions ListByTenant.
 */
public final class BestPracticesVersionsListByTenantSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/
     * listBestPracticesVersionsByTenant.json
     */
    /**
     * Sample code: List Automanage best practices versions.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void
        listAutomanageBestPracticesVersions(com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.bestPracticesVersions().listByTenant("azureBestPracticesProduction", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationProfileAssignments_CreateOrUpdate

```java
import com.azure.resourcemanager.automanage.models.ConfigurationProfileAssignmentProperties;

/**
 * Samples for ConfigurationProfileAssignments CreateOrUpdate.
 */
public final class ConfigurationProfileAssignmentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/
     * createOrUpdateConfigurationProfileAssignment.json
     */
    /**
     * Sample code: Create or update configuration profile assignment.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void
        createOrUpdateConfigurationProfileAssignment(com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.configurationProfileAssignments()
            .define("default")
            .withExistingVirtualMachine("myResourceGroupName", "myVMName")
            .withProperties(new ConfigurationProfileAssignmentProperties()
                .withConfigurationProfile("/providers/Microsoft.Automanage/bestPractices/AzureBestPracticesProduction"))
            .create();
    }
}
```

### ConfigurationProfileAssignments_Delete

```java
/**
 * Samples for ConfigurationProfileAssignments Delete.
 */
public final class ConfigurationProfileAssignmentsDeleteSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/
     * deleteConfigurationProfileAssignment.json
     */
    /**
     * Sample code: Delete an configuration profile assignment.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void
        deleteAnConfigurationProfileAssignment(com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.configurationProfileAssignments()
            .deleteWithResponse("myResourceGroupName", "default", "myVMName", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationProfileAssignments_Get

```java
/**
 * Samples for ConfigurationProfileAssignments Get.
 */
public final class ConfigurationProfileAssignmentsGetSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/
     * getConfigurationProfileAssignment.json
     */
    /**
     * Sample code: Get a configuration profile assignment.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void
        getAConfigurationProfileAssignment(com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.configurationProfileAssignments()
            .getWithResponse("myResourceGroupName", "default", "myVMName", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationProfileAssignments_List

```java
/**
 * Samples for ConfigurationProfileAssignments List.
 */
public final class ConfigurationProfileAssignmentsListSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/
     * listConfigurationProfileAssignmentsBySubscription.json
     */
    /**
     * Sample code: List configuration profile assignments by subscription.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void listConfigurationProfileAssignmentsBySubscription(
        com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.configurationProfileAssignments().list(com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationProfileAssignments_ListByClusterName

```java
/**
 * Samples for ConfigurationProfileAssignments ListByClusterName.
 */
public final class ConfigurationProfileAssignmentsListByClusterNameSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/
     * listConfigurationProfileAssignmentsByClusterName.json
     */
    /**
     * Sample code: List configuration profile assignments by resourceGroup and cluster.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void listConfigurationProfileAssignmentsByResourceGroupAndCluster(
        com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.configurationProfileAssignments()
            .listByClusterName("myResourceGroupName", "myClusterName", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationProfileAssignments_ListByMachineName

```java
/**
 * Samples for ConfigurationProfileAssignments ListByMachineName.
 */
public final class ConfigurationProfileAssignmentsListByMachineNameSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/
     * listConfigurationProfileAssignmentsByMachineName.json
     */
    /**
     * Sample code: List configuration profile assignments by resourceGroup and machine.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void listConfigurationProfileAssignmentsByResourceGroupAndMachine(
        com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.configurationProfileAssignments()
            .listByMachineName("myResourceGroupName", "myMachineName", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationProfileAssignments_ListByResourceGroup

```java
/**
 * Samples for ConfigurationProfileAssignments ListByResourceGroup.
 */
public final class ConfigurationProfileAssignmentsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/
     * listConfigurationProfileAssignmentsByResourceGroup.json
     */
    /**
     * Sample code: List configuration profile assignments by resourceGroup.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void listConfigurationProfileAssignmentsByResourceGroup(
        com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.configurationProfileAssignments()
            .listByResourceGroup("myResourceGroupName", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationProfileAssignments_ListByVirtualMachines

```java
/**
 * Samples for ConfigurationProfileAssignments ListByVirtualMachines.
 */
public final class ConfigurationProfileAssignmentsListByVirtualMachinesSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/
     * listConfigurationProfileAssignmentsByVirtualMachines.json
     */
    /**
     * Sample code: List configuration profile assignments by resourceGroup and virtual machine.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void listConfigurationProfileAssignmentsByResourceGroupAndVirtualMachine(
        com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.configurationProfileAssignments()
            .listByVirtualMachines("myResourceGroupName", "myVMName", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationProfileHciAssignments_CreateOrUpdate

```java
import com.azure.resourcemanager.automanage.fluent.models.ConfigurationProfileAssignmentInner;
import com.azure.resourcemanager.automanage.models.ConfigurationProfileAssignmentProperties;

/**
 * Samples for ConfigurationProfileHciAssignments CreateOrUpdate.
 */
public final class ConfigurationProfileHciAssignmentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/
     * createOrUpdateConfigurationProfileHCIAssignment.json
     */
    /**
     * Sample code: Create or update a HCI configuration profile assignment.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void createOrUpdateAHCIConfigurationProfileAssignment(
        com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.configurationProfileHciAssignments()
            .createOrUpdateWithResponse("myResourceGroupName", "myClusterName", "default",
                new ConfigurationProfileAssignmentInner()
                    .withProperties(new ConfigurationProfileAssignmentProperties().withConfigurationProfile(
                        "/providers/Microsoft.Automanage/bestPractices/AzureBestPracticesProduction")),
                com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationProfileHciAssignments_Delete

```java
/**
 * Samples for ConfigurationProfileHciAssignments Delete.
 */
public final class ConfigurationProfileHciAssignmentsDeleteSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/
     * deleteConfigurationProfileHCIAssignment.json
     */
    /**
     * Sample code: Delete a HCI configuration profile assignment.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void
        deleteAHCIConfigurationProfileAssignment(com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.configurationProfileHciAssignments()
            .deleteWithResponse("myResourceGroupName", "myClusterName", "default", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationProfileHciAssignments_Get

```java
/**
 * Samples for ConfigurationProfileHciAssignments Get.
 */
public final class ConfigurationProfileHciAssignmentsGetSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/
     * getConfigurationProfileHCIAssignment.json
     */
    /**
     * Sample code: Get a HCI configuration profile assignment.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void
        getAHCIConfigurationProfileAssignment(com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.configurationProfileHciAssignments()
            .getWithResponse("myResourceGroupName", "myClusterName", "default", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationProfileHcrpAssignments_CreateOrUpdate

```java
import com.azure.resourcemanager.automanage.fluent.models.ConfigurationProfileAssignmentInner;
import com.azure.resourcemanager.automanage.models.ConfigurationProfileAssignmentProperties;

/**
 * Samples for ConfigurationProfileHcrpAssignments CreateOrUpdate.
 */
public final class ConfigurationProfileHcrpAssignmentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/
     * createOrUpdateConfigurationProfileHCRPAssignment.json
     */
    /**
     * Sample code: Create or update HCRP configuration profile assignment.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void createOrUpdateHCRPConfigurationProfileAssignment(
        com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.configurationProfileHcrpAssignments()
            .createOrUpdateWithResponse("myResourceGroupName", "myMachineName", "default",
                new ConfigurationProfileAssignmentInner()
                    .withProperties(new ConfigurationProfileAssignmentProperties().withConfigurationProfile(
                        "/providers/Microsoft.Automanage/bestPractices/AzureBestPracticesProduction")),
                com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationProfileHcrpAssignments_Delete

```java
/**
 * Samples for ConfigurationProfileHcrpAssignments Delete.
 */
public final class ConfigurationProfileHcrpAssignmentsDeleteSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/
     * deleteConfigurationProfileHCRPAssignment.json
     */
    /**
     * Sample code: Delete a HCRP configuration profile assignment.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void
        deleteAHCRPConfigurationProfileAssignment(com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.configurationProfileHcrpAssignments()
            .deleteWithResponse("myResourceGroupName", "myMachineName", "default", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationProfileHcrpAssignments_Get

```java
/**
 * Samples for ConfigurationProfileHcrpAssignments Get.
 */
public final class ConfigurationProfileHcrpAssignmentsGetSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/
     * getConfigurationProfileHCRPAssignment.json
     */
    /**
     * Sample code: Get a HCRP configuration profile assignment.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void
        getAHCRPConfigurationProfileAssignment(com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.configurationProfileHcrpAssignments()
            .getWithResponse("myResourceGroupName", "myMachineName", "default", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationProfiles_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.automanage.models.ConfigurationProfileProperties;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ConfigurationProfiles CreateOrUpdate.
 */
public final class ConfigurationProfilesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/
     * createOrUpdateConfigurationProfile.json
     */
    /**
     * Sample code: Create or update configuration profile.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void createOrUpdateConfigurationProfile(
        com.azure.resourcemanager.automanage.AutomanageManager manager) throws IOException {
        manager.configurationProfiles()
            .define("customConfigurationProfile")
            .withRegion("East US")
            .withExistingResourceGroup("myResourceGroupName")
            .withTags(mapOf("Organization", "Administration"))
            .withProperties(new ConfigurationProfileProperties().withConfiguration(SerializerFactory
                .createDefaultManagementSerializerAdapter()
                .deserialize(
                    "{\"Antimalware/Enable\":false,\"AzureSecurityCenter/Enable\":true,\"Backup/Enable\":false,\"BootDiagnostics/Enable\":true,\"ChangeTrackingAndInventory/Enable\":true,\"GuestConfiguration/Enable\":true,\"LogAnalytics/Enable\":true,\"UpdateManagement/Enable\":true,\"VMInsights/Enable\":true}",
                    Object.class, SerializerEncoding.JSON)))
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

### ConfigurationProfiles_Delete

```java
/**
 * Samples for ConfigurationProfiles Delete.
 */
public final class ConfigurationProfilesDeleteSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/
     * deleteConfigurationProfile.json
     */
    /**
     * Sample code: Delete a configuration profile.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void deleteAConfigurationProfile(com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.configurationProfiles()
            .deleteByResourceGroupWithResponse("rg", "customConfigurationProfile", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationProfiles_GetByResourceGroup

```java
/**
 * Samples for ConfigurationProfiles GetByResourceGroup.
 */
public final class ConfigurationProfilesGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/getConfigurationProfile
     * .json
     */
    /**
     * Sample code: Get a configuration profile.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void getAConfigurationProfile(com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.configurationProfiles()
            .getByResourceGroupWithResponse("myResourceGroupName", "customConfigurationProfile",
                com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationProfiles_List

```java
/**
 * Samples for ConfigurationProfiles List.
 */
public final class ConfigurationProfilesListSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/
     * listConfigurationProfilesBySubscription.json
     */
    /**
     * Sample code: List configuration profiles by subscription.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void
        listConfigurationProfilesBySubscription(com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.configurationProfiles().list(com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationProfiles_ListByResourceGroup

```java
/**
 * Samples for ConfigurationProfiles ListByResourceGroup.
 */
public final class ConfigurationProfilesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/
     * listConfigurationProfilesByResourceGroup.json
     */
    /**
     * Sample code: List configuration profiles by resource group.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void
        listConfigurationProfilesByResourceGroup(com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.configurationProfiles().listByResourceGroup("myResourceGroupName", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationProfiles_Update

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.automanage.models.ConfigurationProfile;
import com.azure.resourcemanager.automanage.models.ConfigurationProfileProperties;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ConfigurationProfiles Update.
 */
public final class ConfigurationProfilesUpdateSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/
     * updateConfigurationProfile.json
     */
    /**
     * Sample code: Update configuration profile.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void updateConfigurationProfile(com.azure.resourcemanager.automanage.AutomanageManager manager)
        throws IOException {
        ConfigurationProfile resource = manager.configurationProfiles()
            .getByResourceGroupWithResponse("myResourceGroupName", "customConfigurationProfile",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("Organization", "Administration"))
            .withProperties(new ConfigurationProfileProperties().withConfiguration(SerializerFactory
                .createDefaultManagementSerializerAdapter()
                .deserialize(
                    "{\"Antimalware/Enable\":false,\"AzureSecurityCenter/Enable\":true,\"Backup/Enable\":false,\"BootDiagnostics/Enable\":true,\"ChangeTrackingAndInventory/Enable\":true,\"GuestConfiguration/Enable\":true,\"LogAnalytics/Enable\":true,\"UpdateManagement/Enable\":true,\"VMInsights/Enable\":true}",
                    Object.class, SerializerEncoding.JSON)))
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

### ConfigurationProfilesVersions_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.automanage.fluent.models.ConfigurationProfileInner;
import com.azure.resourcemanager.automanage.models.ConfigurationProfileProperties;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ConfigurationProfilesVersions CreateOrUpdate.
 */
public final class ConfigurationProfilesVersionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/
     * createOrUpdateConfigurationProfileVersion.json
     */
    /**
     * Sample code: Create or update configuration profile version.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void createOrUpdateConfigurationProfileVersion(
        com.azure.resourcemanager.automanage.AutomanageManager manager) throws IOException {
        manager.configurationProfilesVersions()
            .createOrUpdateWithResponse("customConfigurationProfile", "version1", "myResourceGroupName",
                new ConfigurationProfileInner().withLocation("East US")
                    .withTags(mapOf("Organization", "Administration"))
                    .withProperties(new ConfigurationProfileProperties().withConfiguration(SerializerFactory
                        .createDefaultManagementSerializerAdapter()
                        .deserialize(
                            "{\"Antimalware/Enable\":false,\"AzureSecurityCenter/Enable\":true,\"Backup/Enable\":false,\"BootDiagnostics/Enable\":true,\"ChangeTrackingAndInventory/Enable\":true,\"GuestConfiguration/Enable\":true,\"LogAnalytics/Enable\":true,\"UpdateManagement/Enable\":true,\"VMInsights/Enable\":true}",
                            Object.class, SerializerEncoding.JSON))),
                com.azure.core.util.Context.NONE);
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

### ConfigurationProfilesVersions_Delete

```java
/**
 * Samples for ConfigurationProfilesVersions Delete.
 */
public final class ConfigurationProfilesVersionsDeleteSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/
     * deleteConfigurationProfileVersion.json
     */
    /**
     * Sample code: Delete a configuration profile version.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void
        deleteAConfigurationProfileVersion(com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.configurationProfilesVersions()
            .deleteWithResponse("rg", "customConfigurationProfile", "version1", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationProfilesVersions_Get

```java
/**
 * Samples for ConfigurationProfilesVersions Get.
 */
public final class ConfigurationProfilesVersionsGetSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/
     * getConfigurationProfileVersion.json
     */
    /**
     * Sample code: Get a configuration profile version.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void getAConfigurationProfileVersion(com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.configurationProfilesVersions()
            .getWithResponse("customConfigurationProfile", "version1", "myResourceGroupName",
                com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationProfilesVersions_ListChildResources

```java
/**
 * Samples for ConfigurationProfilesVersions ListChildResources.
 */
public final class ConfigurationProfilesVersionsListChildResourcesSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/
     * listConfigurationProfileVersions.json
     */
    /**
     * Sample code: List configuration profile versions by configuration profile.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void listConfigurationProfileVersionsByConfigurationProfile(
        com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.configurationProfilesVersions()
            .listChildResources("customConfigurationProfile", "myResourceGroupName", com.azure.core.util.Context.NONE);
    }
}
```

### HciReports_Get

```java
/**
 * Samples for HciReports Get.
 */
public final class HciReportsGetSamples {
    /*
     * x-ms-original-file:
     * specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/getHCIReport.json
     */
    /**
     * Sample code: Get a report for a HCI configuration profile assignment.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void getAReportForAHCIConfigurationProfileAssignment(
        com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.hciReports()
            .getWithResponse("myResourceGroupName", "myClusterName", "default", "b4e9ee6b-1717-4ff0-a8d2-e6d72c33d5f4",
                com.azure.core.util.Context.NONE);
    }
}
```

### HciReports_ListByConfigurationProfileAssignments

```java
/**
 * Samples for HciReports ListByConfigurationProfileAssignments.
 */
public final class HciReportsListByConfigurationProfileAssignmentsSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/
     * listReportsByconfigurationProfileHCIAssignment.json
     */
    /**
     * Sample code: List reports by HCI configuration profiles assignment.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void listReportsByHCIConfigurationProfilesAssignment(
        com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.hciReports()
            .listByConfigurationProfileAssignments("myResourceGroupName", "myClusterName", "default",
                com.azure.core.util.Context.NONE);
    }
}
```

### HcrpReports_Get

```java
/**
 * Samples for HcrpReports Get.
 */
public final class HcrpReportsGetSamples {
    /*
     * x-ms-original-file:
     * specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/getHCRPReport.json
     */
    /**
     * Sample code: Get a report for a HCRP configuration profile assignment.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void getAReportForAHCRPConfigurationProfileAssignment(
        com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.hcrpReports()
            .getWithResponse("myResourceGroupName", "myMachineName", "default", "b4e9ee6b-1717-4ff0-a8d2-e6d72c33d5f4",
                com.azure.core.util.Context.NONE);
    }
}
```

### HcrpReports_ListByConfigurationProfileAssignments

```java
/**
 * Samples for HcrpReports ListByConfigurationProfileAssignments.
 */
public final class HcrpReportsListByConfigurationProfileAssignmentsSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/
     * listReportsByconfigurationProfileHCRPAssignment.json
     */
    /**
     * Sample code: List reports by HCRP configuration profiles assignment.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void listReportsByHCRPConfigurationProfilesAssignment(
        com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.hcrpReports()
            .listByConfigurationProfileAssignments("myResourceGroupName", "myMachineName", "default",
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
     * x-ms-original-file:
     * specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/listOperations.json
     */
    /**
     * Sample code: Lists all of the available Automanage REST API operations.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void listsAllOfTheAvailableAutomanageRESTAPIOperations(
        com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Reports_Get

```java
/**
 * Samples for Reports Get.
 */
public final class ReportsGetSamples {
    /*
     * x-ms-original-file:
     * specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/getReport.json
     */
    /**
     * Sample code: Get a report for a configuration profile assignment.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void
        getAReportForAConfigurationProfileAssignment(com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.reports()
            .getWithResponse("myResourceGroupName", "default", "b4e9ee6b-1717-4ff0-a8d2-e6d72c33d5f4", "myVMName",
                com.azure.core.util.Context.NONE);
    }
}
```

### Reports_ListByConfigurationProfileAssignments

```java
/**
 * Samples for Reports ListByConfigurationProfileAssignments.
 */
public final class ReportsListByConfigurationProfileAssignmentsSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/
     * listReportsByconfigurationProfileAssignment.json
     */
    /**
     * Sample code: List reports by configuration profiles assignment.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void
        listReportsByConfigurationProfilesAssignment(com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.reports()
            .listByConfigurationProfileAssignments("myResourceGroupName", "default", "myVMName",
                com.azure.core.util.Context.NONE);
    }
}
```

### ServicePrincipals_Get

```java
/**
 * Samples for ServicePrincipals Get.
 */
public final class ServicePrincipalsGetSamples {
    /*
     * x-ms-original-file:
     * specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/getServicePrincipal.
     * json
     */
    /**
     * Sample code: Get service principal.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void getServicePrincipal(com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.servicePrincipals().getWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### ServicePrincipals_List

```java
/**
 * Samples for ServicePrincipals List.
 */
public final class ServicePrincipalsListSamples {
    /*
     * x-ms-original-file: specification/automanage/resource-manager/Microsoft.Automanage/stable/2022-05-04/examples/
     * listServicePrincipalBySubscription.json
     */
    /**
     * Sample code: List service principal by subscription.
     * 
     * @param manager Entry point to AutomanageManager.
     */
    public static void
        listServicePrincipalBySubscription(com.azure.resourcemanager.automanage.AutomanageManager manager) {
        manager.servicePrincipals().list(com.azure.core.util.Context.NONE);
    }
}
```

