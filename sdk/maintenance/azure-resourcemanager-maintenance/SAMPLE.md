# Code snippets and samples


## ApplyUpdateForResourceGroup

- [ListByResourceGroup](#applyupdateforresourcegroup_listbyresourcegroup)

## ApplyUpdates

- [CreateOrUpdate](#applyupdates_createorupdate)
- [CreateOrUpdateParent](#applyupdates_createorupdateparent)
- [Get](#applyupdates_get)
- [GetParent](#applyupdates_getparent)
- [List](#applyupdates_list)

## ConfigurationAssignments

- [CreateOrUpdate](#configurationassignments_createorupdate)
- [CreateOrUpdateParent](#configurationassignments_createorupdateparent)
- [Delete](#configurationassignments_delete)
- [DeleteParent](#configurationassignments_deleteparent)
- [List](#configurationassignments_list)
- [ListParent](#configurationassignments_listparent)

## MaintenanceConfigurations

- [CreateOrUpdate](#maintenanceconfigurations_createorupdate)
- [Delete](#maintenanceconfigurations_delete)
- [GetByResourceGroup](#maintenanceconfigurations_getbyresourcegroup)
- [List](#maintenanceconfigurations_list)
- [Update](#maintenanceconfigurations_update)

## MaintenanceConfigurationsForResourceGroup

- [ListByResourceGroup](#maintenanceconfigurationsforresourcegroup_listbyresourcegroup)

## Operations

- [List](#operations_list)

## PublicMaintenanceConfigurations

- [Get](#publicmaintenanceconfigurations_get)
- [List](#publicmaintenanceconfigurations_list)

## Updates

- [List](#updates_list)
- [ListParent](#updates_listparent)
### ApplyUpdateForResourceGroup_ListByResourceGroup

```java
/** Samples for ApplyUpdateForResourceGroup ListByResourceGroup. */
public final class ApplyUpdateForResourceGroupListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/maintenance/resource-manager/Microsoft.Maintenance/stable/2021-05-01/examples/ApplyUpdatesResourceGroup_List.json
     */
    /**
     * Sample code: ApplyUpdatesResourceGroup_List.
     *
     * @param manager Entry point to MaintenanceManager.
     */
    public static void applyUpdatesResourceGroupList(com.azure.resourcemanager.maintenance.MaintenanceManager manager) {
        manager.applyUpdateForResourceGroups().listByResourceGroup("examplerg", com.azure.core.util.Context.NONE);
    }
}
```

### ApplyUpdates_CreateOrUpdate

```java
/** Samples for ApplyUpdates CreateOrUpdate. */
public final class ApplyUpdatesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/maintenance/resource-manager/Microsoft.Maintenance/stable/2021-05-01/examples/ApplyUpdates_CreateOrUpdate.json
     */
    /**
     * Sample code: ApplyUpdates_CreateOrUpdate.
     *
     * @param manager Entry point to MaintenanceManager.
     */
    public static void applyUpdatesCreateOrUpdate(com.azure.resourcemanager.maintenance.MaintenanceManager manager) {
        manager
            .applyUpdates()
            .createOrUpdateWithResponse(
                "examplerg",
                "Microsoft.Compute",
                "virtualMachineScaleSets",
                "smdtest1",
                com.azure.core.util.Context.NONE);
    }
}
```

### ApplyUpdates_CreateOrUpdateParent

```java
/** Samples for ApplyUpdates CreateOrUpdateParent. */
public final class ApplyUpdatesCreateOrUpdateParentSamples {
    /*
     * x-ms-original-file: specification/maintenance/resource-manager/Microsoft.Maintenance/stable/2021-05-01/examples/ApplyUpdates_CreateOrUpdateParent.json
     */
    /**
     * Sample code: ApplyUpdates_CreateOrUpdateParent.
     *
     * @param manager Entry point to MaintenanceManager.
     */
    public static void applyUpdatesCreateOrUpdateParent(
        com.azure.resourcemanager.maintenance.MaintenanceManager manager) {
        manager
            .applyUpdates()
            .createOrUpdateParentWithResponse(
                "examplerg",
                "Microsoft.Compute",
                "virtualMachineScaleSets",
                "smdtest1",
                "virtualMachines",
                "smdvm1",
                com.azure.core.util.Context.NONE);
    }
}
```

### ApplyUpdates_Get

```java
/** Samples for ApplyUpdates Get. */
public final class ApplyUpdatesGetSamples {
    /*
     * x-ms-original-file: specification/maintenance/resource-manager/Microsoft.Maintenance/stable/2021-05-01/examples/ApplyUpdates_Get.json
     */
    /**
     * Sample code: ApplyUpdates_Get.
     *
     * @param manager Entry point to MaintenanceManager.
     */
    public static void applyUpdatesGet(com.azure.resourcemanager.maintenance.MaintenanceManager manager) {
        manager
            .applyUpdates()
            .getWithResponse(
                "examplerg",
                "Microsoft.Compute",
                "virtualMachineScaleSets",
                "smdtest1",
                "e9b9685d-78e4-44c4-a81c-64a14f9b87b6",
                com.azure.core.util.Context.NONE);
    }
}
```

### ApplyUpdates_GetParent

```java
/** Samples for ApplyUpdates GetParent. */
public final class ApplyUpdatesGetParentSamples {
    /*
     * x-ms-original-file: specification/maintenance/resource-manager/Microsoft.Maintenance/stable/2021-05-01/examples/ApplyUpdates_GetParent.json
     */
    /**
     * Sample code: ApplyUpdates_GetParent.
     *
     * @param manager Entry point to MaintenanceManager.
     */
    public static void applyUpdatesGetParent(com.azure.resourcemanager.maintenance.MaintenanceManager manager) {
        manager
            .applyUpdates()
            .getParentWithResponse(
                "examplerg",
                "virtualMachineScaleSets",
                "smdtest1",
                "Microsoft.Compute",
                "virtualMachines",
                "smdvm1",
                "e9b9685d-78e4-44c4-a81c-64a14f9b87b6",
                com.azure.core.util.Context.NONE);
    }
}
```

### ApplyUpdates_List

```java
/** Samples for ApplyUpdates List. */
public final class ApplyUpdatesListSamples {
    /*
     * x-ms-original-file: specification/maintenance/resource-manager/Microsoft.Maintenance/stable/2021-05-01/examples/ApplyUpdates_List.json
     */
    /**
     * Sample code: ApplyUpdates_List.
     *
     * @param manager Entry point to MaintenanceManager.
     */
    public static void applyUpdatesList(com.azure.resourcemanager.maintenance.MaintenanceManager manager) {
        manager.applyUpdates().list(com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationAssignments_CreateOrUpdate

```java
import com.azure.resourcemanager.maintenance.fluent.models.ConfigurationAssignmentInner;

/** Samples for ConfigurationAssignments CreateOrUpdate. */
public final class ConfigurationAssignmentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/maintenance/resource-manager/Microsoft.Maintenance/stable/2021-05-01/examples/ConfigurationAssignments_CreateOrUpdate.json
     */
    /**
     * Sample code: ConfigurationAssignments_CreateOrUpdate.
     *
     * @param manager Entry point to MaintenanceManager.
     */
    public static void configurationAssignmentsCreateOrUpdate(
        com.azure.resourcemanager.maintenance.MaintenanceManager manager) {
        manager
            .configurationAssignments()
            .createOrUpdateWithResponse(
                "examplerg",
                "Microsoft.Compute",
                "virtualMachineScaleSets",
                "smdtest1",
                "workervmConfiguration",
                new ConfigurationAssignmentInner()
                    .withMaintenanceConfigurationId(
                        "/subscriptions/5b4b650e-28b9-4790-b3ab-ddbd88d727c4/resourcegroups/examplerg/providers/Microsoft.Maintenance/maintenanceConfigurations/configuration1"),
                com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationAssignments_CreateOrUpdateParent

```java
import com.azure.resourcemanager.maintenance.fluent.models.ConfigurationAssignmentInner;

/** Samples for ConfigurationAssignments CreateOrUpdateParent. */
public final class ConfigurationAssignmentsCreateOrUpdateParentSamples {
    /*
     * x-ms-original-file: specification/maintenance/resource-manager/Microsoft.Maintenance/stable/2021-05-01/examples/ConfigurationAssignments_CreateOrUpdateParent.json
     */
    /**
     * Sample code: ConfigurationAssignments_CreateOrUpdateParent.
     *
     * @param manager Entry point to MaintenanceManager.
     */
    public static void configurationAssignmentsCreateOrUpdateParent(
        com.azure.resourcemanager.maintenance.MaintenanceManager manager) {
        manager
            .configurationAssignments()
            .createOrUpdateParentWithResponse(
                "examplerg",
                "Microsoft.Compute",
                "virtualMachineScaleSets",
                "smdtest1",
                "virtualMachines",
                "smdvm1",
                "workervmPolicy",
                new ConfigurationAssignmentInner()
                    .withMaintenanceConfigurationId(
                        "/subscriptions/5b4b650e-28b9-4790-b3ab-ddbd88d727c4/resourcegroups/examplerg/providers/Microsoft.Maintenance/maintenanceConfigurations/policy1"),
                com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationAssignments_Delete

```java
/** Samples for ConfigurationAssignments Delete. */
public final class ConfigurationAssignmentsDeleteSamples {
    /*
     * x-ms-original-file: specification/maintenance/resource-manager/Microsoft.Maintenance/stable/2021-05-01/examples/ConfigurationAssignments_Delete.json
     */
    /**
     * Sample code: ConfigurationAssignments_Delete.
     *
     * @param manager Entry point to MaintenanceManager.
     */
    public static void configurationAssignmentsDelete(
        com.azure.resourcemanager.maintenance.MaintenanceManager manager) {
        manager
            .configurationAssignments()
            .deleteWithResponse(
                "examplerg",
                "Microsoft.Compute",
                "virtualMachineScaleSets",
                "smdtest1",
                "workervmConfiguration",
                com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationAssignments_DeleteParent

```java
/** Samples for ConfigurationAssignments DeleteParent. */
public final class ConfigurationAssignmentsDeleteParentSamples {
    /*
     * x-ms-original-file: specification/maintenance/resource-manager/Microsoft.Maintenance/stable/2021-05-01/examples/ConfigurationAssignments_DeleteParent.json
     */
    /**
     * Sample code: ConfigurationAssignments_DeleteParent.
     *
     * @param manager Entry point to MaintenanceManager.
     */
    public static void configurationAssignmentsDeleteParent(
        com.azure.resourcemanager.maintenance.MaintenanceManager manager) {
        manager
            .configurationAssignments()
            .deleteParentWithResponse(
                "examplerg",
                "Microsoft.Compute",
                "virtualMachineScaleSets",
                "smdtest1",
                "virtualMachines",
                "smdvm1",
                "workervmConfiguration",
                com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationAssignments_List

```java
/** Samples for ConfigurationAssignments List. */
public final class ConfigurationAssignmentsListSamples {
    /*
     * x-ms-original-file: specification/maintenance/resource-manager/Microsoft.Maintenance/stable/2021-05-01/examples/ConfigurationAssignments_List.json
     */
    /**
     * Sample code: ConfigurationAssignments_List.
     *
     * @param manager Entry point to MaintenanceManager.
     */
    public static void configurationAssignmentsList(com.azure.resourcemanager.maintenance.MaintenanceManager manager) {
        manager
            .configurationAssignments()
            .list(
                "examplerg",
                "Microsoft.Compute",
                "virtualMachineScaleSets",
                "smdtest1",
                com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationAssignments_ListParent

```java
/** Samples for ConfigurationAssignments ListParent. */
public final class ConfigurationAssignmentsListParentSamples {
    /*
     * x-ms-original-file: specification/maintenance/resource-manager/Microsoft.Maintenance/stable/2021-05-01/examples/ConfigurationAssignments_ListParent.json
     */
    /**
     * Sample code: ConfigurationAssignments_ListParent.
     *
     * @param manager Entry point to MaintenanceManager.
     */
    public static void configurationAssignmentsListParent(
        com.azure.resourcemanager.maintenance.MaintenanceManager manager) {
        manager
            .configurationAssignments()
            .listParent(
                "examplerg",
                "Microsoft.Compute",
                "virtualMachineScaleSets",
                "smdtest1",
                "virtualMachines",
                "smdtestvm1",
                com.azure.core.util.Context.NONE);
    }
}
```

### MaintenanceConfigurations_CreateOrUpdate

```java
import com.azure.resourcemanager.maintenance.models.MaintenanceScope;
import com.azure.resourcemanager.maintenance.models.Visibility;

/** Samples for MaintenanceConfigurations CreateOrUpdate. */
public final class MaintenanceConfigurationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/maintenance/resource-manager/Microsoft.Maintenance/stable/2021-05-01/examples/MaintenanceConfigurations_CreateOrUpdateForResource.json
     */
    /**
     * Sample code: MaintenanceConfigurations_CreateOrUpdateForResource.
     *
     * @param manager Entry point to MaintenanceManager.
     */
    public static void maintenanceConfigurationsCreateOrUpdateForResource(
        com.azure.resourcemanager.maintenance.MaintenanceManager manager) {
        manager
            .maintenanceConfigurations()
            .define("configuration1")
            .withExistingResourceGroup("examplerg")
            .withRegion("westus2")
            .withNamespace("Microsoft.Maintenance")
            .withMaintenanceScope(MaintenanceScope.HOST)
            .withVisibility(Visibility.CUSTOM)
            .withStartDateTime("2020-04-30 08:00")
            .withExpirationDateTime("9999-12-31 00:00")
            .withDuration("05:00")
            .withTimeZone("Pacific Standard Time")
            .withRecurEvery("Day")
            .create();
    }
}
```

### MaintenanceConfigurations_Delete

```java
/** Samples for MaintenanceConfigurations Delete. */
public final class MaintenanceConfigurationsDeleteSamples {
    /*
     * x-ms-original-file: specification/maintenance/resource-manager/Microsoft.Maintenance/stable/2021-05-01/examples/MaintenanceConfigurations_DeleteForResource.json
     */
    /**
     * Sample code: MaintenanceConfigurations_DeleteForResource.
     *
     * @param manager Entry point to MaintenanceManager.
     */
    public static void maintenanceConfigurationsDeleteForResource(
        com.azure.resourcemanager.maintenance.MaintenanceManager manager) {
        manager
            .maintenanceConfigurations()
            .deleteByResourceGroupWithResponse("examplerg", "example1", com.azure.core.util.Context.NONE);
    }
}
```

### MaintenanceConfigurations_GetByResourceGroup

```java
/** Samples for MaintenanceConfigurations GetByResourceGroup. */
public final class MaintenanceConfigurationsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/maintenance/resource-manager/Microsoft.Maintenance/stable/2021-05-01/examples/MaintenanceConfigurations_GetForResource.json
     */
    /**
     * Sample code: MaintenanceConfigurations_GetForResource.
     *
     * @param manager Entry point to MaintenanceManager.
     */
    public static void maintenanceConfigurationsGetForResource(
        com.azure.resourcemanager.maintenance.MaintenanceManager manager) {
        manager
            .maintenanceConfigurations()
            .getByResourceGroupWithResponse("examplerg", "configuration1", com.azure.core.util.Context.NONE);
    }
}
```

### MaintenanceConfigurations_List

```java
/** Samples for MaintenanceConfigurations List. */
public final class MaintenanceConfigurationsListSamples {
    /*
     * x-ms-original-file: specification/maintenance/resource-manager/Microsoft.Maintenance/stable/2021-05-01/examples/MaintenanceConfigurations_List.json
     */
    /**
     * Sample code: MaintenanceConfigurations_List.
     *
     * @param manager Entry point to MaintenanceManager.
     */
    public static void maintenanceConfigurationsList(com.azure.resourcemanager.maintenance.MaintenanceManager manager) {
        manager.maintenanceConfigurations().list(com.azure.core.util.Context.NONE);
    }
}
```

### MaintenanceConfigurations_Update

```java
import com.azure.resourcemanager.maintenance.models.MaintenanceConfiguration;
import com.azure.resourcemanager.maintenance.models.MaintenanceScope;
import com.azure.resourcemanager.maintenance.models.Visibility;

/** Samples for MaintenanceConfigurations Update. */
public final class MaintenanceConfigurationsUpdateSamples {
    /*
     * x-ms-original-file: specification/maintenance/resource-manager/Microsoft.Maintenance/stable/2021-05-01/examples/MaintenanceConfigurations_UpdateForResource.json
     */
    /**
     * Sample code: MaintenanceConfigurations_UpdateForResource.
     *
     * @param manager Entry point to MaintenanceManager.
     */
    public static void maintenanceConfigurationsUpdateForResource(
        com.azure.resourcemanager.maintenance.MaintenanceManager manager) {
        MaintenanceConfiguration resource =
            manager
                .maintenanceConfigurations()
                .getByResourceGroupWithResponse("examplerg", "configuration1", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withNamespace("Microsoft.Maintenance")
            .withMaintenanceScope(MaintenanceScope.HOST)
            .withVisibility(Visibility.CUSTOM)
            .withStartDateTime("2020-04-30 08:00")
            .withExpirationDateTime("9999-12-31 00:00")
            .withDuration("05:00")
            .withTimeZone("Pacific Standard Time")
            .withRecurEvery("Month Third Sunday")
            .apply();
    }
}
```

### MaintenanceConfigurationsForResourceGroup_ListByResourceGroup

```java
/** Samples for MaintenanceConfigurationsForResourceGroup ListByResourceGroup. */
public final class MaintenanceConfigurationsForResourceGroupListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/maintenance/resource-manager/Microsoft.Maintenance/stable/2021-05-01/examples/MaintenanceConfigurationsResourceGroup_List.json
     */
    /**
     * Sample code: MaintenanceConfigurationsResourceGroup_List.
     *
     * @param manager Entry point to MaintenanceManager.
     */
    public static void maintenanceConfigurationsResourceGroupList(
        com.azure.resourcemanager.maintenance.MaintenanceManager manager) {
        manager
            .maintenanceConfigurationsForResourceGroups()
            .listByResourceGroup("examplerg", com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/maintenance/resource-manager/Microsoft.Maintenance/stable/2021-05-01/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to MaintenanceManager.
     */
    public static void operationsList(com.azure.resourcemanager.maintenance.MaintenanceManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PublicMaintenanceConfigurations_Get

```java
/** Samples for PublicMaintenanceConfigurations Get. */
public final class PublicMaintenanceConfigurationsGetSamples {
    /*
     * x-ms-original-file: specification/maintenance/resource-manager/Microsoft.Maintenance/stable/2021-05-01/examples/PublicMaintenanceConfigurations_GetForResource.json
     */
    /**
     * Sample code: PublicMaintenanceConfigurations_GetForResource.
     *
     * @param manager Entry point to MaintenanceManager.
     */
    public static void publicMaintenanceConfigurationsGetForResource(
        com.azure.resourcemanager.maintenance.MaintenanceManager manager) {
        manager.publicMaintenanceConfigurations().getWithResponse("configuration1", com.azure.core.util.Context.NONE);
    }
}
```

### PublicMaintenanceConfigurations_List

```java
/** Samples for PublicMaintenanceConfigurations List. */
public final class PublicMaintenanceConfigurationsListSamples {
    /*
     * x-ms-original-file: specification/maintenance/resource-manager/Microsoft.Maintenance/stable/2021-05-01/examples/PublicMaintenanceConfigurations_List.json
     */
    /**
     * Sample code: PublicMaintenanceConfigurations_List.
     *
     * @param manager Entry point to MaintenanceManager.
     */
    public static void publicMaintenanceConfigurationsList(
        com.azure.resourcemanager.maintenance.MaintenanceManager manager) {
        manager.publicMaintenanceConfigurations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Updates_List

```java
/** Samples for Updates List. */
public final class UpdatesListSamples {
    /*
     * x-ms-original-file: specification/maintenance/resource-manager/Microsoft.Maintenance/stable/2021-05-01/examples/Updates_List.json
     */
    /**
     * Sample code: Updates_List.
     *
     * @param manager Entry point to MaintenanceManager.
     */
    public static void updatesList(com.azure.resourcemanager.maintenance.MaintenanceManager manager) {
        manager
            .updates()
            .list(
                "examplerg",
                "Microsoft.Compute",
                "virtualMachineScaleSets",
                "smdtest1",
                com.azure.core.util.Context.NONE);
    }
}
```

### Updates_ListParent

```java
/** Samples for Updates ListParent. */
public final class UpdatesListParentSamples {
    /*
     * x-ms-original-file: specification/maintenance/resource-manager/Microsoft.Maintenance/stable/2021-05-01/examples/Updates_ListParent.json
     */
    /**
     * Sample code: Updates_ListParent.
     *
     * @param manager Entry point to MaintenanceManager.
     */
    public static void updatesListParent(com.azure.resourcemanager.maintenance.MaintenanceManager manager) {
        manager
            .updates()
            .listParent(
                "examplerg",
                "Microsoft.Compute",
                "virtualMachineScaleSets",
                "smdtest1",
                "virtualMachines",
                "1",
                com.azure.core.util.Context.NONE);
    }
}
```

