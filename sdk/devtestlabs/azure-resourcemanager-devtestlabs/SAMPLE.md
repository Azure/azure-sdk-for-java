# Code snippets and samples


## ArmTemplates

- [Get](#armtemplates_get)
- [List](#armtemplates_list)

## ArtifactSources

- [CreateOrUpdate](#artifactsources_createorupdate)
- [Delete](#artifactsources_delete)
- [Get](#artifactsources_get)
- [List](#artifactsources_list)
- [Update](#artifactsources_update)

## Artifacts

- [GenerateArmTemplate](#artifacts_generatearmtemplate)
- [Get](#artifacts_get)
- [List](#artifacts_list)

## Costs

- [CreateOrUpdate](#costs_createorupdate)
- [Get](#costs_get)

## CustomImages

- [CreateOrUpdate](#customimages_createorupdate)
- [Delete](#customimages_delete)
- [Get](#customimages_get)
- [List](#customimages_list)
- [Update](#customimages_update)

## Disks

- [Attach](#disks_attach)
- [CreateOrUpdate](#disks_createorupdate)
- [Delete](#disks_delete)
- [Detach](#disks_detach)
- [Get](#disks_get)
- [List](#disks_list)
- [Update](#disks_update)

## Environments

- [CreateOrUpdate](#environments_createorupdate)
- [Delete](#environments_delete)
- [Get](#environments_get)
- [List](#environments_list)
- [Update](#environments_update)

## Formulas

- [CreateOrUpdate](#formulas_createorupdate)
- [Delete](#formulas_delete)
- [Get](#formulas_get)
- [List](#formulas_list)
- [Update](#formulas_update)

## GalleryImages

- [List](#galleryimages_list)

## GlobalSchedules

- [CreateOrUpdate](#globalschedules_createorupdate)
- [Delete](#globalschedules_delete)
- [Execute](#globalschedules_execute)
- [GetByResourceGroup](#globalschedules_getbyresourcegroup)
- [List](#globalschedules_list)
- [ListByResourceGroup](#globalschedules_listbyresourcegroup)
- [Retarget](#globalschedules_retarget)
- [Update](#globalschedules_update)

## Labs

- [ClaimAnyVm](#labs_claimanyvm)
- [CreateEnvironment](#labs_createenvironment)
- [CreateOrUpdate](#labs_createorupdate)
- [Delete](#labs_delete)
- [ExportResourceUsage](#labs_exportresourceusage)
- [GenerateUploadUri](#labs_generateuploaduri)
- [GetByResourceGroup](#labs_getbyresourcegroup)
- [ImportVirtualMachine](#labs_importvirtualmachine)
- [List](#labs_list)
- [ListByResourceGroup](#labs_listbyresourcegroup)
- [ListVhds](#labs_listvhds)
- [Update](#labs_update)

## NotificationChannels

- [CreateOrUpdate](#notificationchannels_createorupdate)
- [Delete](#notificationchannels_delete)
- [Get](#notificationchannels_get)
- [List](#notificationchannels_list)
- [Notify](#notificationchannels_notify)
- [Update](#notificationchannels_update)

## Operations

- [Get](#operations_get)

## Policies

- [CreateOrUpdate](#policies_createorupdate)
- [Delete](#policies_delete)
- [Get](#policies_get)
- [List](#policies_list)
- [Update](#policies_update)

## PolicySets

- [EvaluatePolicies](#policysets_evaluatepolicies)

## ProviderOperations

- [List](#provideroperations_list)

## Schedules

- [CreateOrUpdate](#schedules_createorupdate)
- [Delete](#schedules_delete)
- [Execute](#schedules_execute)
- [Get](#schedules_get)
- [List](#schedules_list)
- [ListApplicable](#schedules_listapplicable)
- [Update](#schedules_update)

## Secrets

- [CreateOrUpdate](#secrets_createorupdate)
- [Delete](#secrets_delete)
- [Get](#secrets_get)
- [List](#secrets_list)
- [Update](#secrets_update)

## ServiceFabricSchedules

- [CreateOrUpdate](#servicefabricschedules_createorupdate)
- [Delete](#servicefabricschedules_delete)
- [Execute](#servicefabricschedules_execute)
- [Get](#servicefabricschedules_get)
- [List](#servicefabricschedules_list)
- [Update](#servicefabricschedules_update)

## ServiceFabrics

- [CreateOrUpdate](#servicefabrics_createorupdate)
- [Delete](#servicefabrics_delete)
- [Get](#servicefabrics_get)
- [List](#servicefabrics_list)
- [ListApplicableSchedules](#servicefabrics_listapplicableschedules)
- [Start](#servicefabrics_start)
- [Stop](#servicefabrics_stop)
- [Update](#servicefabrics_update)

## ServiceRunners

- [CreateOrUpdate](#servicerunners_createorupdate)
- [Delete](#servicerunners_delete)
- [Get](#servicerunners_get)

## Users

- [CreateOrUpdate](#users_createorupdate)
- [Delete](#users_delete)
- [Get](#users_get)
- [List](#users_list)
- [Update](#users_update)

## VirtualMachineSchedules

- [CreateOrUpdate](#virtualmachineschedules_createorupdate)
- [Delete](#virtualmachineschedules_delete)
- [Execute](#virtualmachineschedules_execute)
- [Get](#virtualmachineschedules_get)
- [List](#virtualmachineschedules_list)
- [Update](#virtualmachineschedules_update)

## VirtualMachines

- [AddDataDisk](#virtualmachines_adddatadisk)
- [ApplyArtifacts](#virtualmachines_applyartifacts)
- [Claim](#virtualmachines_claim)
- [CreateOrUpdate](#virtualmachines_createorupdate)
- [Delete](#virtualmachines_delete)
- [DetachDataDisk](#virtualmachines_detachdatadisk)
- [Get](#virtualmachines_get)
- [GetRdpFileContents](#virtualmachines_getrdpfilecontents)
- [List](#virtualmachines_list)
- [ListApplicableSchedules](#virtualmachines_listapplicableschedules)
- [Redeploy](#virtualmachines_redeploy)
- [Resize](#virtualmachines_resize)
- [Restart](#virtualmachines_restart)
- [Start](#virtualmachines_start)
- [Stop](#virtualmachines_stop)
- [TransferDisks](#virtualmachines_transferdisks)
- [UnClaim](#virtualmachines_unclaim)
- [Update](#virtualmachines_update)

## VirtualNetworks

- [CreateOrUpdate](#virtualnetworks_createorupdate)
- [Delete](#virtualnetworks_delete)
- [Get](#virtualnetworks_get)
- [List](#virtualnetworks_list)
- [Update](#virtualnetworks_update)
### ArmTemplates_Get

```java
import com.azure.core.util.Context;

/** Samples for ArmTemplates Get. */
public final class ArmTemplatesGetSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/ArmTemplates_Get.json
     */
    /**
     * Sample code: ArmTemplates_Get.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void armTemplatesGet(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .armTemplates()
            .getWithResponse(
                "resourceGroupName", "{labName}", "{artifactSourceName}", "{armTemplateName}", null, Context.NONE);
    }
}
```

### ArmTemplates_List

```java
import com.azure.core.util.Context;

/** Samples for ArmTemplates List. */
public final class ArmTemplatesListSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/ArmTemplates_List.json
     */
    /**
     * Sample code: ArmTemplates_List.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void armTemplatesList(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .armTemplates()
            .list("resourceGroupName", "{labName}", "{artifactSourceName}", null, null, null, null, Context.NONE);
    }
}
```

### ArtifactSources_CreateOrUpdate

```java
import com.azure.resourcemanager.devtestlabs.models.EnableStatus;
import com.azure.resourcemanager.devtestlabs.models.SourceControlType;
import java.util.HashMap;
import java.util.Map;

/** Samples for ArtifactSources CreateOrUpdate. */
public final class ArtifactSourcesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/ArtifactSources_CreateOrUpdate.json
     */
    /**
     * Sample code: ArtifactSources_CreateOrUpdate.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void artifactSourcesCreateOrUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .artifactSources()
            .define("{artifactSourceName}")
            .withRegion((String) null)
            .withExistingLab("resourceGroupName", "{labName}")
            .withTags(mapOf("tagName1", "tagValue1"))
            .withDisplayName("{displayName}")
            .withUri("{artifactSourceUri}")
            .withSourceType(SourceControlType.fromString("{VsoGit|GitHub|StorageAccount}"))
            .withFolderPath("{folderPath}")
            .withArmTemplateFolderPath("{armTemplateFolderPath}")
            .withBranchRef("{branchRef}")
            .withSecurityToken("{securityToken}")
            .withStatus(EnableStatus.fromString("{Enabled|Disabled}"))
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

### ArtifactSources_Delete

```java
import com.azure.core.util.Context;

/** Samples for ArtifactSources Delete. */
public final class ArtifactSourcesDeleteSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/ArtifactSources_Delete.json
     */
    /**
     * Sample code: ArtifactSources_Delete.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void artifactSourcesDelete(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .artifactSources()
            .deleteWithResponse("resourceGroupName", "{labName}", "{artifactSourceName}", Context.NONE);
    }
}
```

### ArtifactSources_Get

```java
import com.azure.core.util.Context;

/** Samples for ArtifactSources Get. */
public final class ArtifactSourcesGetSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/ArtifactSources_Get.json
     */
    /**
     * Sample code: ArtifactSources_Get.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void artifactSourcesGet(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .artifactSources()
            .getWithResponse("resourceGroupName", "{labName}", "{artifactSourceName}", null, Context.NONE);
    }
}
```

### ArtifactSources_List

```java
import com.azure.core.util.Context;

/** Samples for ArtifactSources List. */
public final class ArtifactSourcesListSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/ArtifactSources_List.json
     */
    /**
     * Sample code: ArtifactSources_List.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void artifactSourcesList(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.artifactSources().list("resourceGroupName", "{labName}", null, null, null, null, Context.NONE);
    }
}
```

### ArtifactSources_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.ArtifactSource;
import java.util.HashMap;
import java.util.Map;

/** Samples for ArtifactSources Update. */
public final class ArtifactSourcesUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/ArtifactSources_Update.json
     */
    /**
     * Sample code: ArtifactSources_Update.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void artifactSourcesUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        ArtifactSource resource =
            manager
                .artifactSources()
                .getWithResponse("resourceGroupName", "{labName}", "{artifactSourceName}", null, Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tagName1", "tagValue1")).apply();
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

### Artifacts_GenerateArmTemplate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.FileUploadOptions;
import com.azure.resourcemanager.devtestlabs.models.GenerateArmTemplateRequest;

/** Samples for Artifacts GenerateArmTemplate. */
public final class ArtifactsGenerateArmTemplateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Artifacts_GenerateArmTemplate.json
     */
    /**
     * Sample code: Artifacts_GenerateArmTemplate.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void artifactsGenerateArmTemplate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .artifacts()
            .generateArmTemplateWithResponse(
                "resourceGroupName",
                "{labName}",
                "{artifactSourceName}",
                "{artifactName}",
                new GenerateArmTemplateRequest()
                    .withVirtualMachineName("{vmName}")
                    .withLocation("{location}")
                    .withFileUploadOptions(FileUploadOptions.NONE),
                Context.NONE);
    }
}
```

### Artifacts_Get

```java
import com.azure.core.util.Context;

/** Samples for Artifacts Get. */
public final class ArtifactsGetSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Artifacts_Get.json
     */
    /**
     * Sample code: Artifacts_Get.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void artifactsGet(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .artifacts()
            .getWithResponse(
                "resourceGroupName", "{labName}", "{artifactSourceName}", "{artifactName}", null, Context.NONE);
    }
}
```

### Artifacts_List

```java
import com.azure.core.util.Context;

/** Samples for Artifacts List. */
public final class ArtifactsListSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Artifacts_List.json
     */
    /**
     * Sample code: Artifacts_List.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void artifactsList(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .artifacts()
            .list("resourceGroupName", "{labName}", "{artifactSourceName}", null, null, null, null, Context.NONE);
    }
}
```

### Costs_CreateOrUpdate

```java
import com.azure.resourcemanager.devtestlabs.models.CostThresholdProperties;
import com.azure.resourcemanager.devtestlabs.models.CostThresholdStatus;
import com.azure.resourcemanager.devtestlabs.models.PercentageCostThresholdProperties;
import com.azure.resourcemanager.devtestlabs.models.ReportingCycleType;
import com.azure.resourcemanager.devtestlabs.models.TargetCostProperties;
import com.azure.resourcemanager.devtestlabs.models.TargetCostStatus;
import java.time.OffsetDateTime;
import java.util.Arrays;

/** Samples for Costs CreateOrUpdate. */
public final class CostsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Costs_CreateOrUpdate.json
     */
    /**
     * Sample code: Costs_CreateOrUpdate.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void costsCreateOrUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .costs()
            .define("targetCost")
            .withRegion((String) null)
            .withExistingLab("resourceGroupName", "{labName}")
            .withTargetCost(
                new TargetCostProperties()
                    .withStatus(TargetCostStatus.ENABLED)
                    .withTarget(100)
                    .withCostThresholds(
                        Arrays
                            .asList(
                                new CostThresholdProperties()
                                    .withThresholdId("00000000-0000-0000-0000-000000000001")
                                    .withPercentageThreshold(
                                        new PercentageCostThresholdProperties().withThresholdValue(25.0))
                                    .withDisplayOnChart(CostThresholdStatus.DISABLED)
                                    .withSendNotificationWhenExceeded(CostThresholdStatus.DISABLED),
                                new CostThresholdProperties()
                                    .withThresholdId("00000000-0000-0000-0000-000000000002")
                                    .withPercentageThreshold(
                                        new PercentageCostThresholdProperties().withThresholdValue(50.0))
                                    .withDisplayOnChart(CostThresholdStatus.ENABLED)
                                    .withSendNotificationWhenExceeded(CostThresholdStatus.ENABLED),
                                new CostThresholdProperties()
                                    .withThresholdId("00000000-0000-0000-0000-000000000003")
                                    .withPercentageThreshold(
                                        new PercentageCostThresholdProperties().withThresholdValue(75.0))
                                    .withDisplayOnChart(CostThresholdStatus.DISABLED)
                                    .withSendNotificationWhenExceeded(CostThresholdStatus.DISABLED),
                                new CostThresholdProperties()
                                    .withThresholdId("00000000-0000-0000-0000-000000000004")
                                    .withPercentageThreshold(
                                        new PercentageCostThresholdProperties().withThresholdValue(100.0))
                                    .withDisplayOnChart(CostThresholdStatus.DISABLED)
                                    .withSendNotificationWhenExceeded(CostThresholdStatus.DISABLED),
                                new CostThresholdProperties()
                                    .withThresholdId("00000000-0000-0000-0000-000000000005")
                                    .withPercentageThreshold(
                                        new PercentageCostThresholdProperties().withThresholdValue(125.0))
                                    .withDisplayOnChart(CostThresholdStatus.DISABLED)
                                    .withSendNotificationWhenExceeded(CostThresholdStatus.DISABLED)))
                    .withCycleStartDateTime(OffsetDateTime.parse("2020-12-01T00:00:00.000Z"))
                    .withCycleEndDateTime(OffsetDateTime.parse("2020-12-31T00:00:00.000Z"))
                    .withCycleType(ReportingCycleType.CALENDAR_MONTH))
            .withCurrencyCode("USD")
            .withStartDateTime(OffsetDateTime.parse("2020-12-01T00:00:00Z"))
            .withEndDateTime(OffsetDateTime.parse("2020-12-31T23:59:59Z"))
            .create();
    }
}
```

### Costs_Get

```java
import com.azure.core.util.Context;

/** Samples for Costs Get. */
public final class CostsGetSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Costs_Get.json
     */
    /**
     * Sample code: Costs_Get.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void costsGet(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.costs().getWithResponse("resourceGroupName", "{labName}", "targetCost", null, Context.NONE);
    }
}
```

### CustomImages_CreateOrUpdate

```java
import com.azure.resourcemanager.devtestlabs.models.CustomImagePropertiesFromVm;
import com.azure.resourcemanager.devtestlabs.models.LinuxOsInfo;
import com.azure.resourcemanager.devtestlabs.models.LinuxOsState;
import java.util.HashMap;
import java.util.Map;

/** Samples for CustomImages CreateOrUpdate. */
public final class CustomImagesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/CustomImages_CreateOrUpdate.json
     */
    /**
     * Sample code: CustomImages_CreateOrUpdate.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void customImagesCreateOrUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .customImages()
            .define("{customImageName}")
            .withRegion((String) null)
            .withExistingLab("resourceGroupName", "{labName}")
            .withTags(mapOf("tagName1", "tagValue1"))
            .withVm(
                new CustomImagePropertiesFromVm()
                    .withSourceVmId(
                        "/subscriptions/{subscriptionId}/resourcegroups/resourceGroupName/providers/microsoft.devtestlab/labs/{labName}/virtualmachines/{vmName}")
                    .withLinuxOsInfo(new LinuxOsInfo().withLinuxOsState(LinuxOsState.NON_DEPROVISIONED)))
            .withDescription("My Custom Image")
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

### CustomImages_Delete

```java
import com.azure.core.util.Context;

/** Samples for CustomImages Delete. */
public final class CustomImagesDeleteSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/CustomImages_Delete.json
     */
    /**
     * Sample code: CustomImages_Delete.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void customImagesDelete(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.customImages().delete("resourceGroupName", "{labName}", "{customImageName}", Context.NONE);
    }
}
```

### CustomImages_Get

```java
import com.azure.core.util.Context;

/** Samples for CustomImages Get. */
public final class CustomImagesGetSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/CustomImages_Get.json
     */
    /**
     * Sample code: CustomImages_Get.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void customImagesGet(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .customImages()
            .getWithResponse("resourceGroupName", "{labName}", "{customImageName}", null, Context.NONE);
    }
}
```

### CustomImages_List

```java
import com.azure.core.util.Context;

/** Samples for CustomImages List. */
public final class CustomImagesListSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/CustomImages_List.json
     */
    /**
     * Sample code: CustomImages_List.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void customImagesList(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.customImages().list("resourceGroupName", "{labName}", null, null, null, null, Context.NONE);
    }
}
```

### CustomImages_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.CustomImage;
import java.util.HashMap;
import java.util.Map;

/** Samples for CustomImages Update. */
public final class CustomImagesUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/CustomImages_Update.json
     */
    /**
     * Sample code: CustomImages_Update.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void customImagesUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        CustomImage resource =
            manager
                .customImages()
                .getWithResponse("resourceGroupName", "{labName}", "{customImageName}", null, Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tagName1", "tagValue2")).apply();
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

### Disks_Attach

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.AttachDiskProperties;

/** Samples for Disks Attach. */
public final class DisksAttachSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Disks_Attach.json
     */
    /**
     * Sample code: Disks_Attach.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void disksAttach(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .disks()
            .attach(
                "resourceGroupName",
                "{labName}",
                "{userId}",
                "{diskName}",
                new AttachDiskProperties()
                    .withLeasedByLabVmId(
                        "/subscriptions/{subscriptionId}/resourcegroups/resourceGroupName/providers/microsoft.devtestlab/labs/{labName}/virtualmachines/{vmName}"),
                Context.NONE);
    }
}
```

### Disks_CreateOrUpdate

```java
import com.azure.resourcemanager.devtestlabs.models.StorageType;

/** Samples for Disks CreateOrUpdate. */
public final class DisksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Disks_CreateOrUpdate.json
     */
    /**
     * Sample code: Disks_CreateOrUpdate.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void disksCreateOrUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .disks()
            .define("{diskName}")
            .withRegion((String) null)
            .withExistingUser("resourceGroupName", "{labName}", "{userId}")
            .withDiskType(StorageType.STANDARD)
            .withDiskSizeGiB(1023)
            .withLeasedByLabVmId(
                "/subscriptions/{subscriptionId}/resourcegroups/resourceGroupName/providers/microsoft.devtestlab/labs/{labName}/virtualmachines/vmName")
            .create();
    }
}
```

### Disks_Delete

```java
import com.azure.core.util.Context;

/** Samples for Disks Delete. */
public final class DisksDeleteSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Disks_Delete.json
     */
    /**
     * Sample code: Disks_Delete.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void disksDelete(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.disks().delete("resourceGroupName", "{labName}", "{userId}", "{diskName}", Context.NONE);
    }
}
```

### Disks_Detach

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.DetachDiskProperties;

/** Samples for Disks Detach. */
public final class DisksDetachSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Disks_Detach.json
     */
    /**
     * Sample code: Disks_Detach.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void disksDetach(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .disks()
            .detach(
                "resourceGroupName",
                "{labName}",
                "{userId}",
                "{diskName}",
                new DetachDiskProperties()
                    .withLeasedByLabVmId(
                        "/subscriptions/{subscriptionId}/resourcegroups/myResourceGroup/providers/microsoft.devtestlab/labs/{labName}/virtualmachines/{vmName}"),
                Context.NONE);
    }
}
```

### Disks_Get

```java
import com.azure.core.util.Context;

/** Samples for Disks Get. */
public final class DisksGetSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Disks_Get.json
     */
    /**
     * Sample code: Disks_Get.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void disksGet(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.disks().getWithResponse("resourceGroupName", "{labName}", "@me", "{diskName}", null, Context.NONE);
    }
}
```

### Disks_List

```java
import com.azure.core.util.Context;

/** Samples for Disks List. */
public final class DisksListSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Disks_List.json
     */
    /**
     * Sample code: Disks_List.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void disksList(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.disks().list("resourceGroupName", "{labName}", "@me", null, null, null, null, Context.NONE);
    }
}
```

### Disks_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.Disk;
import java.util.HashMap;
import java.util.Map;

/** Samples for Disks Update. */
public final class DisksUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Disks_Update.json
     */
    /**
     * Sample code: Disks_Update.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void disksUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        Disk resource =
            manager
                .disks()
                .getWithResponse("resourceGroupName", "{labName}", "@me", "diskName", null, Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tagName1", "tagValue1")).apply();
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

### Environments_CreateOrUpdate

```java
import com.azure.resourcemanager.devtestlabs.models.EnvironmentDeploymentProperties;
import java.util.Arrays;

/** Samples for Environments CreateOrUpdate. */
public final class EnvironmentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Environments_CreateOrUpdate.json
     */
    /**
     * Sample code: Environments_CreateOrUpdate.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void environmentsCreateOrUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .environments()
            .define("{environmentName}")
            .withRegion((String) null)
            .withExistingUser("resourceGroupName", "{labName}", "@me")
            .withDeploymentProperties(
                new EnvironmentDeploymentProperties()
                    .withArmTemplateId(
                        "/subscriptions/{subscriptionId}/resourceGroups/resourceGroupName/providers/Microsoft.DevTestLab/labs/{labName}/artifactSources/{artifactSourceName}/armTemplates/{armTemplateName}")
                    .withParameters(Arrays.asList()))
            .create();
    }
}
```

### Environments_Delete

```java
import com.azure.core.util.Context;

/** Samples for Environments Delete. */
public final class EnvironmentsDeleteSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Environments_Delete.json
     */
    /**
     * Sample code: Environments_Delete.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void environmentsDelete(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.environments().delete("resourceGroupName", "{labName}", "@me", "{environmentName}", Context.NONE);
    }
}
```

### Environments_Get

```java
import com.azure.core.util.Context;

/** Samples for Environments Get. */
public final class EnvironmentsGetSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Environments_Get.json
     */
    /**
     * Sample code: Environments_Get.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void environmentsGet(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .environments()
            .getWithResponse("resourceGroupName", "{labName}", "@me", "{environmentName}", null, Context.NONE);
    }
}
```

### Environments_List

```java
import com.azure.core.util.Context;

/** Samples for Environments List. */
public final class EnvironmentsListSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Environments_List.json
     */
    /**
     * Sample code: Environments_List.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void environmentsList(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.environments().list("resourceGroupName", "{labName}", "@me", null, null, null, null, Context.NONE);
    }
}
```

### Environments_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.DtlEnvironment;
import java.util.HashMap;
import java.util.Map;

/** Samples for Environments Update. */
public final class EnvironmentsUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Environments_Update.json
     */
    /**
     * Sample code: Environments_Update.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void environmentsUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        DtlEnvironment resource =
            manager
                .environments()
                .getWithResponse("resourceGroupName", "{labName}", "@me", "{environmentName}", null, Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tagName1", "tagValue1")).apply();
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

### Formulas_CreateOrUpdate

```java
import com.azure.resourcemanager.devtestlabs.models.ArtifactInstallProperties;
import com.azure.resourcemanager.devtestlabs.models.GalleryImageReference;
import com.azure.resourcemanager.devtestlabs.models.InboundNatRule;
import com.azure.resourcemanager.devtestlabs.models.LabVirtualMachineCreationParameter;
import com.azure.resourcemanager.devtestlabs.models.NetworkInterfaceProperties;
import com.azure.resourcemanager.devtestlabs.models.SharedPublicIpAddressConfiguration;
import com.azure.resourcemanager.devtestlabs.models.TransportProtocol;
import java.util.Arrays;

/** Samples for Formulas CreateOrUpdate. */
public final class FormulasCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Formulas_CreateOrUpdate.json
     */
    /**
     * Sample code: Formulas_CreateOrUpdate.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void formulasCreateOrUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .formulas()
            .define("{formulaName}")
            .withRegion("{location}")
            .withExistingLab("resourceGroupName", "{labName}")
            .withDescription("Formula using a Linux base")
            .withFormulaContent(
                new LabVirtualMachineCreationParameter()
                    .withLocation("{location}")
                    .withNotes("Ubuntu Server 20.10")
                    .withSize("Standard_B1ms")
                    .withUsername("user")
                    .withIsAuthenticationWithSshKey(false)
                    .withLabSubnetName("Dtl{labName}Subnet")
                    .withLabVirtualNetworkId("/virtualnetworks/dtl{labName}")
                    .withDisallowPublicIpAddress(true)
                    .withArtifacts(
                        Arrays
                            .asList(
                                new ArtifactInstallProperties()
                                    .withArtifactId(
                                        "/artifactsources/{artifactSourceName}/artifacts/linux-install-nodejs")
                                    .withParameters(Arrays.asList())))
                    .withGalleryImageReference(
                        new GalleryImageReference()
                            .withOffer("0001-com-ubuntu-server-groovy")
                            .withPublisher("canonical")
                            .withSku("20_10")
                            .withOsType("Linux")
                            .withVersion("latest"))
                    .withNetworkInterface(
                        new NetworkInterfaceProperties()
                            .withSharedPublicIpAddressConfiguration(
                                new SharedPublicIpAddressConfiguration()
                                    .withInboundNatRules(
                                        Arrays
                                            .asList(
                                                new InboundNatRule()
                                                    .withTransportProtocol(TransportProtocol.TCP)
                                                    .withBackendPort(22)))))
                    .withAllowClaim(false)
                    .withStorageType("Standard"))
            .create();
    }
}
```

### Formulas_Delete

```java
import com.azure.core.util.Context;

/** Samples for Formulas Delete. */
public final class FormulasDeleteSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Formulas_Delete.json
     */
    /**
     * Sample code: Formulas_Delete.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void formulasDelete(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.formulas().deleteWithResponse("resourceGroupName", "{labName}", "{formulaName}", Context.NONE);
    }
}
```

### Formulas_Get

```java
import com.azure.core.util.Context;

/** Samples for Formulas Get. */
public final class FormulasGetSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Formulas_Get.json
     */
    /**
     * Sample code: Formulas_Get.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void formulasGet(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.formulas().getWithResponse("resourceGroupName", "{labName}", "{formulaName}", null, Context.NONE);
    }
}
```

### Formulas_List

```java
import com.azure.core.util.Context;

/** Samples for Formulas List. */
public final class FormulasListSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Formulas_List.json
     */
    /**
     * Sample code: Formulas_List.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void formulasList(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.formulas().list("resourceGroupName", "{labName}", null, null, null, null, Context.NONE);
    }
}
```

### Formulas_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.Formula;
import java.util.HashMap;
import java.util.Map;

/** Samples for Formulas Update. */
public final class FormulasUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Formulas_Update.json
     */
    /**
     * Sample code: Formulas_Update.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void formulasUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        Formula resource =
            manager
                .formulas()
                .getWithResponse("resourceGroupName", "{labName}", "{formulaName}", null, Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tagName1", "tagValue1")).apply();
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

### GalleryImages_List

```java
import com.azure.core.util.Context;

/** Samples for GalleryImages List. */
public final class GalleryImagesListSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/GalleryImages_List.json
     */
    /**
     * Sample code: GalleryImages_List.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void galleryImagesList(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.galleryImages().list("resourceGroupName", "{labName}", null, null, null, null, Context.NONE);
    }
}
```

### GlobalSchedules_CreateOrUpdate

```java
import com.azure.resourcemanager.devtestlabs.models.EnableStatus;
import com.azure.resourcemanager.devtestlabs.models.WeekDetails;
import java.util.Arrays;

/** Samples for GlobalSchedules CreateOrUpdate. */
public final class GlobalSchedulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/GlobalSchedules_CreateOrUpdate.json
     */
    /**
     * Sample code: GlobalSchedules_CreateOrUpdate.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void globalSchedulesCreateOrUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .globalSchedules()
            .define("labvmautostart")
            .withRegion((String) null)
            .withExistingResourceGroup("resourceGroupName")
            .withStatus(EnableStatus.ENABLED)
            .withTaskType("LabVmsStartupTask")
            .withWeeklyRecurrence(
                new WeekDetails()
                    .withWeekdays(Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"))
                    .withTime("0700"))
            .withTimeZoneId("Hawaiian Standard Time")
            .create();
    }
}
```

### GlobalSchedules_Delete

```java
import com.azure.core.util.Context;

/** Samples for GlobalSchedules Delete. */
public final class GlobalSchedulesDeleteSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/GlobalSchedules_Delete.json
     */
    /**
     * Sample code: GlobalSchedules_Delete.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void globalSchedulesDelete(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.globalSchedules().deleteWithResponse("resourceGroupName", "labvmautostart", Context.NONE);
    }
}
```

### GlobalSchedules_Execute

```java
import com.azure.core.util.Context;

/** Samples for GlobalSchedules Execute. */
public final class GlobalSchedulesExecuteSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/GlobalSchedules_Execute.json
     */
    /**
     * Sample code: GlobalSchedules_Execute.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void globalSchedulesExecute(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.globalSchedules().execute("resourceGroupName", "labvmautostart", Context.NONE);
    }
}
```

### GlobalSchedules_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for GlobalSchedules GetByResourceGroup. */
public final class GlobalSchedulesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/GlobalSchedules_Get.json
     */
    /**
     * Sample code: GlobalSchedules_Get.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void globalSchedulesGet(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .globalSchedules()
            .getByResourceGroupWithResponse("resourceGroupName", "labvmautostart", null, Context.NONE);
    }
}
```

### GlobalSchedules_List

```java
import com.azure.core.util.Context;

/** Samples for GlobalSchedules List. */
public final class GlobalSchedulesListSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/GlobalSchedules_ListBySubscription.json
     */
    /**
     * Sample code: GlobalSchedules_ListBySubscription.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void globalSchedulesListBySubscription(
        com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.globalSchedules().list(null, null, null, null, Context.NONE);
    }
}
```

### GlobalSchedules_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for GlobalSchedules ListByResourceGroup. */
public final class GlobalSchedulesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/GlobalSchedules_ListByResourceGroup.json
     */
    /**
     * Sample code: GlobalSchedules_ListByResourceGroup.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void globalSchedulesListByResourceGroup(
        com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.globalSchedules().listByResourceGroup("resourceGroupName", null, null, null, null, Context.NONE);
    }
}
```

### GlobalSchedules_Retarget

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.RetargetScheduleProperties;

/** Samples for GlobalSchedules Retarget. */
public final class GlobalSchedulesRetargetSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/GlobalSchedules_Retarget.json
     */
    /**
     * Sample code: GlobalSchedules_Retarget.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void globalSchedulesRetarget(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .globalSchedules()
            .retarget(
                "resourceGroupName",
                "{scheduleName}",
                new RetargetScheduleProperties()
                    .withCurrentResourceId(
                        "/subscriptions/{subscriptionId}/resourcegroups/resourceGroupName/providers/microsoft.devtestlab/labs/{targetLab}")
                    .withTargetResourceId(
                        "/subscriptions/{subscriptionId}/resourcegroups/resourceGroupName/providers/microsoft.devtestlab/labs/{currentLab}"),
                Context.NONE);
    }
}
```

### GlobalSchedules_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.Schedule;
import java.util.HashMap;
import java.util.Map;

/** Samples for GlobalSchedules Update. */
public final class GlobalSchedulesUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/GlobalSchedules_Update.json
     */
    /**
     * Sample code: GlobalSchedules_Update.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void globalSchedulesUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        Schedule resource =
            manager
                .globalSchedules()
                .getByResourceGroupWithResponse("resourceGroupName", "labvmautostart", null, Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tagName1", "tagValue1")).apply();
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

### Labs_ClaimAnyVm

```java
import com.azure.core.util.Context;

/** Samples for Labs ClaimAnyVm. */
public final class LabsClaimAnyVmSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Labs_ClaimAnyVm.json
     */
    /**
     * Sample code: Labs_ClaimAnyVm.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void labsClaimAnyVm(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.labs().claimAnyVm("resourceGroupName", "{labName}", Context.NONE);
    }
}
```

### Labs_CreateEnvironment

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.GalleryImageReference;
import com.azure.resourcemanager.devtestlabs.models.LabVirtualMachineCreationParameter;
import java.util.HashMap;
import java.util.Map;

/** Samples for Labs CreateEnvironment. */
public final class LabsCreateEnvironmentSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Labs_CreateEnvironment.json
     */
    /**
     * Sample code: Labs_CreateEnvironment.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void labsCreateEnvironment(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .labs()
            .createEnvironment(
                "resourceGroupName",
                "{labName}",
                new LabVirtualMachineCreationParameter()
                    .withName("{vmName}")
                    .withLocation("{location}")
                    .withTags(mapOf("tagName1", "tagValue1"))
                    .withSize("Standard_A2_v2")
                    .withUsername("{userName}")
                    .withPassword("{userPassword}")
                    .withLabSubnetName("{virtualnetwork-subnet-name}")
                    .withLabVirtualNetworkId(
                        "/subscriptions/{subscriptionId}/resourcegroups/resourceGroupName/providers/microsoft.devtestlab/labs/{labName}/virtualnetworks/{virtualNetworkName}")
                    .withDisallowPublicIpAddress(true)
                    .withGalleryImageReference(
                        new GalleryImageReference()
                            .withOffer("UbuntuServer")
                            .withPublisher("Canonical")
                            .withSku("16.04-LTS")
                            .withOsType("Linux")
                            .withVersion("Latest"))
                    .withAllowClaim(true)
                    .withStorageType("Standard"),
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

### Labs_CreateOrUpdate

```java
import com.azure.resourcemanager.devtestlabs.models.StorageType;
import java.util.HashMap;
import java.util.Map;

/** Samples for Labs CreateOrUpdate. */
public final class LabsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Labs_CreateOrUpdate.json
     */
    /**
     * Sample code: Labs_CreateOrUpdate.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void labsCreateOrUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .labs()
            .define("{labName}")
            .withRegion("{location}")
            .withExistingResourceGroup("resourceGroupName")
            .withTags(mapOf("tagName1", "tagValue1"))
            .withLabStorageType(StorageType.fromString("{Standard|Premium}"))
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

### Labs_Delete

```java
import com.azure.core.util.Context;

/** Samples for Labs Delete. */
public final class LabsDeleteSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Labs_Delete.json
     */
    /**
     * Sample code: Labs_Delete.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void labsDelete(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.labs().delete("resourceGroupName", "{labName}", Context.NONE);
    }
}
```

### Labs_ExportResourceUsage

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.ExportResourceUsageParameters;
import java.time.OffsetDateTime;

/** Samples for Labs ExportResourceUsage. */
public final class LabsExportResourceUsageSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Labs_ExportResourceUsage.json
     */
    /**
     * Sample code: Labs_ExportResourceUsage.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void labsExportResourceUsage(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .labs()
            .exportResourceUsage(
                "resourceGroupName",
                "{labName}",
                new ExportResourceUsageParameters()
                    .withBlobStorageAbsoluteSasUri(
                        "https://invalid.blob.core.windows.net/export.blob?sv=2015-07-08&sig={sas}&sp=rcw")
                    .withUsageStartDate(OffsetDateTime.parse("2020-12-01T00:00:00Z")),
                Context.NONE);
    }
}
```

### Labs_GenerateUploadUri

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.GenerateUploadUriParameter;

/** Samples for Labs GenerateUploadUri. */
public final class LabsGenerateUploadUriSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Labs_GenerateUploadUri.json
     */
    /**
     * Sample code: Labs_GenerateUploadUri.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void labsGenerateUploadUri(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .labs()
            .generateUploadUriWithResponse(
                "resourceGroupName",
                "{labName}",
                new GenerateUploadUriParameter().withBlobName("{blob-name}"),
                Context.NONE);
    }
}
```

### Labs_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Labs GetByResourceGroup. */
public final class LabsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Labs_Get.json
     */
    /**
     * Sample code: Labs_Get.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void labsGet(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.labs().getByResourceGroupWithResponse("resourceGroupName", "{labName}", null, Context.NONE);
    }
}
```

### Labs_ImportVirtualMachine

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.ImportLabVirtualMachineRequest;

/** Samples for Labs ImportVirtualMachine. */
public final class LabsImportVirtualMachineSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Labs_ImportVirtualMachine.json
     */
    /**
     * Sample code: Labs_ImportVirtualMachine.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void labsImportVirtualMachine(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .labs()
            .importVirtualMachine(
                "resourceGroupName",
                "{labName}",
                new ImportLabVirtualMachineRequest()
                    .withSourceVirtualMachineResourceId(
                        "/subscriptions/{subscriptionId}/resourceGroups/{otherResourceGroupName}/providers/Microsoft.Compute/virtualMachines/{vmName}")
                    .withDestinationVirtualMachineName("{vmName}"),
                Context.NONE);
    }
}
```

### Labs_List

```java
import com.azure.core.util.Context;

/** Samples for Labs List. */
public final class LabsListSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Labs_ListBySubscription.json
     */
    /**
     * Sample code: Labs_ListBySubscription.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void labsListBySubscription(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.labs().list(null, null, null, null, Context.NONE);
    }
}
```

### Labs_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Labs ListByResourceGroup. */
public final class LabsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Labs_ListByResourceGroup.json
     */
    /**
     * Sample code: Labs_ListByResourceGroup.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void labsListByResourceGroup(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.labs().listByResourceGroup("resourceGroupName", null, null, null, null, Context.NONE);
    }
}
```

### Labs_ListVhds

```java
import com.azure.core.util.Context;

/** Samples for Labs ListVhds. */
public final class LabsListVhdsSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Labs_ListVhds.json
     */
    /**
     * Sample code: Labs_ListVhds.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void labsListVhds(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.labs().listVhds("resourceGroupName", "{labName}", Context.NONE);
    }
}
```

### Labs_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.Lab;

/** Samples for Labs Update. */
public final class LabsUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Labs_Update.json
     */
    /**
     * Sample code: Labs_Update.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void labsUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        Lab resource =
            manager
                .labs()
                .getByResourceGroupWithResponse("resourceGroupName", "{labName}", null, Context.NONE)
                .getValue();
        resource.update().apply();
    }
}
```

### NotificationChannels_CreateOrUpdate

```java
import com.azure.resourcemanager.devtestlabs.models.Event;
import com.azure.resourcemanager.devtestlabs.models.NotificationChannelEventType;
import java.util.Arrays;

/** Samples for NotificationChannels CreateOrUpdate. */
public final class NotificationChannelsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/NotificationChannels_CreateOrUpdate.json
     */
    /**
     * Sample code: NotificationChannels_CreateOrUpdate.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void notificationChannelsCreateOrUpdate(
        com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .notificationChannels()
            .define("{notificationChannelName}")
            .withRegion((String) null)
            .withExistingLab("resourceGroupName", "{labName}")
            .withWebhookUrl("{webhookUrl}")
            .withEmailRecipient("{email}")
            .withNotificationLocale("en")
            .withDescription("Integration configured for auto-shutdown")
            .withEvents(Arrays.asList(new Event().withEventName(NotificationChannelEventType.AUTO_SHUTDOWN)))
            .create();
    }
}
```

### NotificationChannels_Delete

```java
import com.azure.core.util.Context;

/** Samples for NotificationChannels Delete. */
public final class NotificationChannelsDeleteSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/NotificationChannels_Delete.json
     */
    /**
     * Sample code: NotificationChannels_Delete.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void notificationChannelsDelete(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .notificationChannels()
            .deleteWithResponse("resourceGroupName", "{labName}", "{notificationChannelName}", Context.NONE);
    }
}
```

### NotificationChannels_Get

```java
import com.azure.core.util.Context;

/** Samples for NotificationChannels Get. */
public final class NotificationChannelsGetSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/NotificationChannels_Get.json
     */
    /**
     * Sample code: NotificationChannels_Get.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void notificationChannelsGet(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .notificationChannels()
            .getWithResponse("resourceGroupName", "{labName}", "{notificationChannelName}", null, Context.NONE);
    }
}
```

### NotificationChannels_List

```java
import com.azure.core.util.Context;

/** Samples for NotificationChannels List. */
public final class NotificationChannelsListSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/NotificationChannels_List.json
     */
    /**
     * Sample code: NotificationChannels_List.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void notificationChannelsList(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.notificationChannels().list("resourceGroupName", "{labName}", null, null, null, null, Context.NONE);
    }
}
```

### NotificationChannels_Notify

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.NotificationChannelEventType;
import com.azure.resourcemanager.devtestlabs.models.NotifyParameters;

/** Samples for NotificationChannels Notify. */
public final class NotificationChannelsNotifySamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/NotificationChannels_Notify.json
     */
    /**
     * Sample code: NotificationChannels_Notify.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void notificationChannelsNotify(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .notificationChannels()
            .notifyWithResponse(
                "resourceGroupName",
                "{labName}",
                "{notificationChannelName}",
                new NotifyParameters()
                    .withEventName(NotificationChannelEventType.AUTO_SHUTDOWN)
                    .withJsonPayload(
                        "{\"eventType\":\"AutoShutdown\",\"subscriptionId\":\"{subscriptionId}\",\"resourceGroupName\":\"resourceGroupName\",\"labName\":\"{labName}\"}"),
                Context.NONE);
    }
}
```

### NotificationChannels_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.NotificationChannel;

/** Samples for NotificationChannels Update. */
public final class NotificationChannelsUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/NotificationChannels_Update.json
     */
    /**
     * Sample code: NotificationChannels_Update.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void notificationChannelsUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        NotificationChannel resource =
            manager
                .notificationChannels()
                .getWithResponse("resourceGroupName", "{labName}", "{notificationChannelName}", null, Context.NONE)
                .getValue();
        resource.update().apply();
    }
}
```

### Operations_Get

```java
import com.azure.core.util.Context;

/** Samples for Operations Get. */
public final class OperationsGetSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Operations_Get.json
     */
    /**
     * Sample code: Operations_Get.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void operationsGet(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.operations().getWithResponse("{locationName}", "{operationName}", Context.NONE);
    }
}
```

### Policies_CreateOrUpdate

```java
import com.azure.resourcemanager.devtestlabs.models.PolicyEvaluatorType;
import com.azure.resourcemanager.devtestlabs.models.PolicyFactName;
import com.azure.resourcemanager.devtestlabs.models.PolicyStatus;
import java.util.HashMap;
import java.util.Map;

/** Samples for Policies CreateOrUpdate. */
public final class PoliciesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Policies_CreateOrUpdate.json
     */
    /**
     * Sample code: Policies_CreateOrUpdate.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void policiesCreateOrUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .policies()
            .define("{policyName}")
            .withRegion("{location}")
            .withExistingPolicyset("resourceGroupName", "{labName}", "{policySetName}")
            .withTags(mapOf("tagName1", "tagValue1"))
            .withDescription("{policyDescription}")
            .withStatus(PolicyStatus.fromString("{policyStatus}"))
            .withFactName(PolicyFactName.fromString("{policyFactName}"))
            .withFactData("{policyFactData}")
            .withThreshold("{policyThreshold}")
            .withEvaluatorType(PolicyEvaluatorType.fromString("{policyEvaluatorType}"))
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

### Policies_Delete

```java
import com.azure.core.util.Context;

/** Samples for Policies Delete. */
public final class PoliciesDeleteSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Policies_Delete.json
     */
    /**
     * Sample code: Policies_Delete.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void policiesDelete(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .policies()
            .deleteWithResponse("resourceGroupName", "{labName}", "{policySetName}", "{policyName}", Context.NONE);
    }
}
```

### Policies_Get

```java
import com.azure.core.util.Context;

/** Samples for Policies Get. */
public final class PoliciesGetSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Policies_Get.json
     */
    /**
     * Sample code: Policies_Get.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void policiesGet(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .policies()
            .getWithResponse("resourceGroupName", "{labName}", "{policySetName}", "{policyName}", null, Context.NONE);
    }
}
```

### Policies_List

```java
import com.azure.core.util.Context;

/** Samples for Policies List. */
public final class PoliciesListSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Policies_List.json
     */
    /**
     * Sample code: Policies_List.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void policiesList(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .policies()
            .list("resourceGroupName", "{labName}", "{policySetName}", null, null, null, null, Context.NONE);
    }
}
```

### Policies_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.Policy;
import java.util.HashMap;
import java.util.Map;

/** Samples for Policies Update. */
public final class PoliciesUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Policies_Update.json
     */
    /**
     * Sample code: Policies_Update.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void policiesUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        Policy resource =
            manager
                .policies()
                .getWithResponse(
                    "resourceGroupName", "{labName}", "{policySetName}", "{policyName}", null, Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tagName1", "tagValue1")).apply();
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

### PolicySets_EvaluatePolicies

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.EvaluatePoliciesProperties;
import com.azure.resourcemanager.devtestlabs.models.EvaluatePoliciesRequest;
import java.util.Arrays;

/** Samples for PolicySets EvaluatePolicies. */
public final class PolicySetsEvaluatePoliciesSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/PolicySets_EvaluatePolicies.json
     */
    /**
     * Sample code: PolicySets_EvaluatePolicies.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void policySetsEvaluatePolicies(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .policySets()
            .evaluatePoliciesWithResponse(
                "resourceGroupName",
                "{labName}",
                "{policySetName}",
                new EvaluatePoliciesRequest()
                    .withPolicies(
                        Arrays
                            .asList(new EvaluatePoliciesProperties().withFactName("LabVmCount").withValueOffset("1"))),
                Context.NONE);
    }
}
```

### ProviderOperations_List

```java
import com.azure.core.util.Context;

/** Samples for ProviderOperations List. */
public final class ProviderOperationsListSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/ProviderOperations_List.json
     */
    /**
     * Sample code: ProviderOperations_List.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void providerOperationsList(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.providerOperations().list(Context.NONE);
    }
}
```

### Schedules_CreateOrUpdate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.fluent.models.ScheduleInner;
import com.azure.resourcemanager.devtestlabs.models.DayDetails;
import com.azure.resourcemanager.devtestlabs.models.EnableStatus;
import com.azure.resourcemanager.devtestlabs.models.HourDetails;
import com.azure.resourcemanager.devtestlabs.models.NotificationSettings;
import com.azure.resourcemanager.devtestlabs.models.WeekDetails;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Schedules CreateOrUpdate. */
public final class SchedulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Schedules_CreateOrUpdate.json
     */
    /**
     * Sample code: Schedules_CreateOrUpdate.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void schedulesCreateOrUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .schedules()
            .createOrUpdateWithResponse(
                "resourceGroupName",
                "{labName}",
                "{scheduleName}",
                new ScheduleInner()
                    .withLocation("{location}")
                    .withTags(mapOf("tagName1", "tagValue1"))
                    .withStatus(EnableStatus.fromString("{Enabled|Disabled}"))
                    .withTaskType("{myLabVmTaskType}")
                    .withWeeklyRecurrence(
                        new WeekDetails()
                            .withWeekdays(Arrays.asList("Monday", "Wednesday", "Friday"))
                            .withTime("{timeOfTheDayTheScheduleWillOccurOnThoseDays}"))
                    .withDailyRecurrence(new DayDetails().withTime("{timeOfTheDayTheScheduleWillOccurEveryDay}"))
                    .withHourlyRecurrence(new HourDetails().withMinute(30))
                    .withTimeZoneId("Pacific Standard Time")
                    .withNotificationSettings(
                        new NotificationSettings()
                            .withStatus(EnableStatus.fromString("{Enabled|Disabled}"))
                            .withTimeInMinutes(15)
                            .withWebhookUrl("{webhookUrl}")
                            .withEmailRecipient("{email}")
                            .withNotificationLocale("EN"))
                    .withTargetResourceId(
                        "/subscriptions/{subscriptionId}/resourcegroups/resourceGroupName/providers/microsoft.devtestlab/labs/{labName}"),
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

### Schedules_Delete

```java
import com.azure.core.util.Context;

/** Samples for Schedules Delete. */
public final class SchedulesDeleteSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Schedules_Delete.json
     */
    /**
     * Sample code: Schedules_Delete.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void schedulesDelete(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.schedules().deleteWithResponse("resourceGroupName", "{labName}", "{scheduleName}", Context.NONE);
    }
}
```

### Schedules_Execute

```java
import com.azure.core.util.Context;

/** Samples for Schedules Execute. */
public final class SchedulesExecuteSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Schedules_Execute.json
     */
    /**
     * Sample code: Schedules_Execute.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void schedulesExecute(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.schedules().execute("resourceGroupName", "{labName}", "{scheduleName}", Context.NONE);
    }
}
```

### Schedules_Get

```java
import com.azure.core.util.Context;

/** Samples for Schedules Get. */
public final class SchedulesGetSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Schedules_Get.json
     */
    /**
     * Sample code: Schedules_Get.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void schedulesGet(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.schedules().getWithResponse("resourceGroupName", "{labName}", "{scheduleName}", null, Context.NONE);
    }
}
```

### Schedules_List

```java
import com.azure.core.util.Context;

/** Samples for Schedules List. */
public final class SchedulesListSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Schedules_List.json
     */
    /**
     * Sample code: Schedules_List.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void schedulesList(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.schedules().list("resourceGroupName", "{labName}", null, null, null, null, Context.NONE);
    }
}
```

### Schedules_ListApplicable

```java
import com.azure.core.util.Context;

/** Samples for Schedules ListApplicable. */
public final class SchedulesListApplicableSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Schedules_ListApplicable.json
     */
    /**
     * Sample code: Schedules_ListApplicable.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void schedulesListApplicable(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.schedules().listApplicable("resourceGroupName", "{labName}", "{scheduleName}", Context.NONE);
    }
}
```

### Schedules_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.ScheduleFragment;
import java.util.HashMap;
import java.util.Map;

/** Samples for Schedules Update. */
public final class SchedulesUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Schedules_Update.json
     */
    /**
     * Sample code: Schedules_Update.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void schedulesUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .schedules()
            .updateWithResponse(
                "resourceGroupName",
                "{labName}",
                "{scheduleName}",
                new ScheduleFragment().withTags(mapOf("tagName1", "tagValue1")),
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

### Secrets_CreateOrUpdate

```java
/** Samples for Secrets CreateOrUpdate. */
public final class SecretsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Secrets_CreateOrUpdate.json
     */
    /**
     * Sample code: Secrets_CreateOrUpdate.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void secretsCreateOrUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .secrets()
            .define("{secretName}")
            .withRegion((String) null)
            .withExistingUser("resourceGroupName", "{labName}", "{userName}")
            .withValue("{secret}")
            .create();
    }
}
```

### Secrets_Delete

```java
import com.azure.core.util.Context;

/** Samples for Secrets Delete. */
public final class SecretsDeleteSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Secrets_Delete.json
     */
    /**
     * Sample code: Secrets_Delete.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void secretsDelete(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .secrets()
            .deleteWithResponse("resourceGroupName", "{labName}", "{userName}", "{secretName}", Context.NONE);
    }
}
```

### Secrets_Get

```java
import com.azure.core.util.Context;

/** Samples for Secrets Get. */
public final class SecretsGetSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Secrets_Get.json
     */
    /**
     * Sample code: Secrets_Get.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void secretsGet(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .secrets()
            .getWithResponse("resourceGroupName", "{labName}", "{userName}", "{secretName}", null, Context.NONE);
    }
}
```

### Secrets_List

```java
import com.azure.core.util.Context;

/** Samples for Secrets List. */
public final class SecretsListSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Secrets_List.json
     */
    /**
     * Sample code: Secrets_List.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void secretsList(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.secrets().list("resourceGroupName", "{labName}", "{userName}", null, null, null, null, Context.NONE);
    }
}
```

### Secrets_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.Secret;
import java.util.HashMap;
import java.util.Map;

/** Samples for Secrets Update. */
public final class SecretsUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Secrets_Update.json
     */
    /**
     * Sample code: Secrets_Update.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void secretsUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        Secret resource =
            manager
                .secrets()
                .getWithResponse("resourceGroupName", "{labName}", "{userName}", "{secretName}", null, Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tagName1", "tagValue1")).apply();
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

### ServiceFabricSchedules_CreateOrUpdate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.fluent.models.ScheduleInner;
import com.azure.resourcemanager.devtestlabs.models.DayDetails;
import com.azure.resourcemanager.devtestlabs.models.EnableStatus;
import com.azure.resourcemanager.devtestlabs.models.HourDetails;
import com.azure.resourcemanager.devtestlabs.models.NotificationSettings;
import com.azure.resourcemanager.devtestlabs.models.WeekDetails;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for ServiceFabricSchedules CreateOrUpdate. */
public final class ServiceFabricSchedulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/ServiceFabricSchedules_CreateOrUpdate.json
     */
    /**
     * Sample code: ServiceFabricSchedules_CreateOrUpdate.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void serviceFabricSchedulesCreateOrUpdate(
        com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .serviceFabricSchedules()
            .createOrUpdateWithResponse(
                "resourceGroupName",
                "{labName}",
                "@me",
                "{serviceFrabicName}",
                "{scheduleName}",
                new ScheduleInner()
                    .withLocation("{location}")
                    .withTags(mapOf("tagName1", "tagValue1"))
                    .withStatus(EnableStatus.fromString("{Enabled|Disabled}"))
                    .withTaskType(
                        "{Unknown|LabVmsShutdownTask|LabVmsStartupTask|LabVmReclamationTask|ComputeVmShutdownTask}")
                    .withWeeklyRecurrence(
                        new WeekDetails()
                            .withWeekdays(
                                Arrays
                                    .asList(
                                        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"))
                            .withTime("19:00"))
                    .withDailyRecurrence(new DayDetails().withTime("19:00"))
                    .withHourlyRecurrence(new HourDetails().withMinute(0))
                    .withTimeZoneId("Pacific Standard Time")
                    .withNotificationSettings(
                        new NotificationSettings()
                            .withStatus(EnableStatus.fromString("{Enabled|Disabled}"))
                            .withTimeInMinutes(15)
                            .withWebhookUrl("{webhoolUrl}")
                            .withEmailRecipient("{email}")
                            .withNotificationLocale("EN"))
                    .withTargetResourceId(
                        "/subscriptions/{subscriptionId}/resourceGroups/resourceGroupName/providers/Microsoft.DevTestLab/labs/{labName}/users/{uniqueIdentifier}/servicefabrics/{serviceFrabicName}"),
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

### ServiceFabricSchedules_Delete

```java
import com.azure.core.util.Context;

/** Samples for ServiceFabricSchedules Delete. */
public final class ServiceFabricSchedulesDeleteSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/ServiceFabricSchedules_Delete.json
     */
    /**
     * Sample code: ServiceFabricSchedules_Delete.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void serviceFabricSchedulesDelete(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .serviceFabricSchedules()
            .deleteWithResponse(
                "resourceGroupName", "{labName}", "@me", "{serviceFrabicName}", "{scheduleName}", Context.NONE);
    }
}
```

### ServiceFabricSchedules_Execute

```java
import com.azure.core.util.Context;

/** Samples for ServiceFabricSchedules Execute. */
public final class ServiceFabricSchedulesExecuteSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/ServiceFabricSchedules_Execute.json
     */
    /**
     * Sample code: ServiceFabricSchedules_Execute.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void serviceFabricSchedulesExecute(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .serviceFabricSchedules()
            .execute("resourceGroupName", "{labName}", "@me", "{serviceFrabicName}", "{scheduleName}", Context.NONE);
    }
}
```

### ServiceFabricSchedules_Get

```java
import com.azure.core.util.Context;

/** Samples for ServiceFabricSchedules Get. */
public final class ServiceFabricSchedulesGetSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/ServiceFabricSchedules_Get.json
     */
    /**
     * Sample code: ServiceFabricSchedules_Get.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void serviceFabricSchedulesGet(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .serviceFabricSchedules()
            .getWithResponse(
                "resourceGroupName", "{labName}", "@me", "{serviceFrabicName}", "{scheduleName}", null, Context.NONE);
    }
}
```

### ServiceFabricSchedules_List

```java
import com.azure.core.util.Context;

/** Samples for ServiceFabricSchedules List. */
public final class ServiceFabricSchedulesListSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/ServiceFabricSchedules_List.json
     */
    /**
     * Sample code: ServiceFabricSchedules_List.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void serviceFabricSchedulesList(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .serviceFabricSchedules()
            .list("resourceGroupName", "{labName}", "@me", "{serviceFrabicName}", null, null, null, null, Context.NONE);
    }
}
```

### ServiceFabricSchedules_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.ScheduleFragment;
import java.util.HashMap;
import java.util.Map;

/** Samples for ServiceFabricSchedules Update. */
public final class ServiceFabricSchedulesUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/ServiceFabricSchedules_Update.json
     */
    /**
     * Sample code: ServiceFabricSchedules_Update.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void serviceFabricSchedulesUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .serviceFabricSchedules()
            .updateWithResponse(
                "resourceGroupName",
                "{labName}",
                "@me",
                "{serviceFrabicName}",
                "{scheduleName}",
                new ScheduleFragment().withTags(mapOf("tagName1", "tagValue1")),
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

### ServiceFabrics_CreateOrUpdate

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for ServiceFabrics CreateOrUpdate. */
public final class ServiceFabricsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/ServiceFabrics_CreateOrUpdate.json
     */
    /**
     * Sample code: ServiceFabrics_CreateOrUpdate.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void serviceFabricsCreateOrUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .serviceFabrics()
            .define("{serviceFabricName}")
            .withRegion("{location}")
            .withExistingUser("resourceGroupName", "{labName}", "{userName}")
            .withTags(mapOf("tagName1", "tagValue1"))
            .withExternalServiceFabricId("{serviceFabricId}")
            .withEnvironmentId("{environmentId}")
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

### ServiceFabrics_Delete

```java
import com.azure.core.util.Context;

/** Samples for ServiceFabrics Delete. */
public final class ServiceFabricsDeleteSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/ServiceFabrics_Delete.json
     */
    /**
     * Sample code: ServiceFabrics_Delete.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void serviceFabricsDelete(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .serviceFabrics()
            .delete("resourceGroupName", "{labName}", "{userName}", "{serviceFabricName}", Context.NONE);
    }
}
```

### ServiceFabrics_Get

```java
import com.azure.core.util.Context;

/** Samples for ServiceFabrics Get. */
public final class ServiceFabricsGetSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/ServiceFabrics_Get.json
     */
    /**
     * Sample code: ServiceFabrics_Get.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void serviceFabricsGet(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .serviceFabrics()
            .getWithResponse("resourceGroupName", "{labName}", "{userName}", "{serviceFabricName}", null, Context.NONE);
    }
}
```

### ServiceFabrics_List

```java
import com.azure.core.util.Context;

/** Samples for ServiceFabrics List. */
public final class ServiceFabricsListSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/ServiceFabrics_List.json
     */
    /**
     * Sample code: ServiceFabrics_List.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void serviceFabricsList(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .serviceFabrics()
            .list("resourceGroupName", "{labName}", "{userName}", null, null, null, null, Context.NONE);
    }
}
```

### ServiceFabrics_ListApplicableSchedules

```java
import com.azure.core.util.Context;

/** Samples for ServiceFabrics ListApplicableSchedules. */
public final class ServiceFabricsListApplicableSchedulesSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/ServiceFabrics_ListApplicableSchedules.json
     */
    /**
     * Sample code: ServiceFabrics_ListApplicableSchedules.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void serviceFabricsListApplicableSchedules(
        com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .serviceFabrics()
            .listApplicableSchedulesWithResponse(
                "resourceGroupName", "{labName}", "{userName}", "{serviceFabricName}", Context.NONE);
    }
}
```

### ServiceFabrics_Start

```java
import com.azure.core.util.Context;

/** Samples for ServiceFabrics Start. */
public final class ServiceFabricsStartSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/ServiceFabrics_Start.json
     */
    /**
     * Sample code: ServiceFabrics_Start.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void serviceFabricsStart(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .serviceFabrics()
            .start("resourceGroupName", "{labName}", "{userName}", "{serviceFabricName}", Context.NONE);
    }
}
```

### ServiceFabrics_Stop

```java
import com.azure.core.util.Context;

/** Samples for ServiceFabrics Stop. */
public final class ServiceFabricsStopSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/ServiceFabrics_Stop.json
     */
    /**
     * Sample code: ServiceFabrics_Stop.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void serviceFabricsStop(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .serviceFabrics()
            .stop("resourceGroupName", "{labName}", "{userName}", "{serviceFabricName}", Context.NONE);
    }
}
```

### ServiceFabrics_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.ServiceFabric;
import java.util.HashMap;
import java.util.Map;

/** Samples for ServiceFabrics Update. */
public final class ServiceFabricsUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/ServiceFabrics_Update.json
     */
    /**
     * Sample code: ServiceFabrics_Update.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void serviceFabricsUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        ServiceFabric resource =
            manager
                .serviceFabrics()
                .getWithResponse(
                    "resourceGroupName", "{labName}", "{userName}", "{serviceFabricName}", null, Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tagName1", "tagValue1")).apply();
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

### ServiceRunners_CreateOrUpdate

```java
import com.azure.resourcemanager.devtestlabs.models.IdentityProperties;
import com.azure.resourcemanager.devtestlabs.models.ManagedIdentityType;
import java.util.HashMap;
import java.util.Map;

/** Samples for ServiceRunners CreateOrUpdate. */
public final class ServiceRunnersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/ServiceRunners_CreateOrUpdate.json
     */
    /**
     * Sample code: ServiceRunners_CreateOrUpdate.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void serviceRunnersCreateOrUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .serviceRunners()
            .define("{servicerunnerName}")
            .withRegion("{location}")
            .withExistingLab("resourceGroupName", "{devtestlabName}")
            .withTags(mapOf("tagName1", "tagValue1"))
            .withIdentity(
                new IdentityProperties()
                    .withType(ManagedIdentityType.fromString("{identityType}"))
                    .withPrincipalId("{identityPrincipalId}")
                    .withTenantId("{identityTenantId}")
                    .withClientSecretUrl("{identityClientSecretUrl}"))
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

### ServiceRunners_Delete

```java
import com.azure.core.util.Context;

/** Samples for ServiceRunners Delete. */
public final class ServiceRunnersDeleteSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/ServiceRunners_Delete.json
     */
    /**
     * Sample code: ServiceRunners_Delete.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void serviceRunnersDelete(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .serviceRunners()
            .deleteWithResponse("resourceGroupName", "{devtestlabName}", "{servicerunnerName}", Context.NONE);
    }
}
```

### ServiceRunners_Get

```java
import com.azure.core.util.Context;

/** Samples for ServiceRunners Get. */
public final class ServiceRunnersGetSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/ServiceRunners_Get.json
     */
    /**
     * Sample code: ServiceRunners_Get.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void serviceRunnersGet(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .serviceRunners()
            .getWithResponse("resourceGroupName", "{devtestlabName}", "{servicerunnerName}", Context.NONE);
    }
}
```

### Users_CreateOrUpdate

```java
import com.azure.resourcemanager.devtestlabs.models.UserIdentity;
import com.azure.resourcemanager.devtestlabs.models.UserSecretStore;
import java.util.HashMap;
import java.util.Map;

/** Samples for Users CreateOrUpdate. */
public final class UsersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Users_CreateOrUpdate.json
     */
    /**
     * Sample code: Users_CreateOrUpdate.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void usersCreateOrUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .users()
            .define("{userName}")
            .withRegion("{location}")
            .withExistingLab("resourceGroupName", "{devtestlabName}")
            .withTags(mapOf("tagName1", "tagValue1"))
            .withIdentity(
                new UserIdentity()
                    .withPrincipalName("{principalName}")
                    .withPrincipalId("{principalId}")
                    .withTenantId("{tenantId}")
                    .withObjectId("{objectId}")
                    .withAppId("{appId}"))
            .withSecretStore(new UserSecretStore().withKeyVaultUri("{keyVaultUri}").withKeyVaultId("{keyVaultId}"))
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

### Users_Delete

```java
import com.azure.core.util.Context;

/** Samples for Users Delete. */
public final class UsersDeleteSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Users_Delete.json
     */
    /**
     * Sample code: Users_Delete.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void usersDelete(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.users().delete("resourceGroupName", "{devtestlabName}", "{userName}", Context.NONE);
    }
}
```

### Users_Get

```java
import com.azure.core.util.Context;

/** Samples for Users Get. */
public final class UsersGetSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Users_Get.json
     */
    /**
     * Sample code: Users_Get.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void usersGet(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.users().getWithResponse("resourceGroupName", "{devtestlabName}", "{userName}", null, Context.NONE);
    }
}
```

### Users_List

```java
import com.azure.core.util.Context;

/** Samples for Users List. */
public final class UsersListSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Users_List.json
     */
    /**
     * Sample code: Users_List.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void usersList(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.users().list("resourceGroupName", "{devtestlabName}", null, null, null, null, Context.NONE);
    }
}
```

### Users_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.User;
import java.util.HashMap;
import java.util.Map;

/** Samples for Users Update. */
public final class UsersUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/Users_Update.json
     */
    /**
     * Sample code: Users_Update.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void usersUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        User resource =
            manager
                .users()
                .getWithResponse("resourceGroupName", "{devtestlabName}", "{userName}", null, Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tagName1", "tagValue1")).apply();
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

### VirtualMachineSchedules_CreateOrUpdate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.fluent.models.ScheduleInner;
import com.azure.resourcemanager.devtestlabs.models.DayDetails;
import com.azure.resourcemanager.devtestlabs.models.EnableStatus;
import com.azure.resourcemanager.devtestlabs.models.HourDetails;
import com.azure.resourcemanager.devtestlabs.models.NotificationSettings;
import com.azure.resourcemanager.devtestlabs.models.WeekDetails;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for VirtualMachineSchedules CreateOrUpdate. */
public final class VirtualMachineSchedulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/VirtualMachineSchedules_CreateOrUpdate.json
     */
    /**
     * Sample code: VirtualMachineSchedules_CreateOrUpdate.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void virtualMachineSchedulesCreateOrUpdate(
        com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .virtualMachineSchedules()
            .createOrUpdateWithResponse(
                "resourceGroupName",
                "{labName}",
                "{vmName}",
                "LabVmsShutdown",
                new ScheduleInner()
                    .withLocation("{location}")
                    .withTags(mapOf("tagName1", "tagValue1"))
                    .withStatus(EnableStatus.ENABLED)
                    .withTaskType("LabVmsShutdownTask")
                    .withWeeklyRecurrence(
                        new WeekDetails().withWeekdays(Arrays.asList("Friday", "Saturday", "Sunday")).withTime("1700"))
                    .withDailyRecurrence(new DayDetails().withTime("1900"))
                    .withHourlyRecurrence(new HourDetails().withMinute(30))
                    .withTimeZoneId("Pacific Standard Time")
                    .withNotificationSettings(
                        new NotificationSettings()
                            .withStatus(EnableStatus.ENABLED)
                            .withTimeInMinutes(30)
                            .withWebhookUrl("{webhookUrl}")
                            .withEmailRecipient("{email}")
                            .withNotificationLocale("EN"))
                    .withTargetResourceId(
                        "/subscriptions/{subscriptionId}/resourcegroups/resourceGroupName/providers/microsoft.devtestlab/labs/{labName}/virtualMachines/{vmName}"),
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

### VirtualMachineSchedules_Delete

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachineSchedules Delete. */
public final class VirtualMachineSchedulesDeleteSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/VirtualMachineSchedules_Delete.json
     */
    /**
     * Sample code: VirtualMachineSchedules_Delete.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void virtualMachineSchedulesDelete(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .virtualMachineSchedules()
            .deleteWithResponse("resourceGroupName", "{labName}", "{vmName}", "LabVmsShutdown", Context.NONE);
    }
}
```

### VirtualMachineSchedules_Execute

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachineSchedules Execute. */
public final class VirtualMachineSchedulesExecuteSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/VirtualMachineSchedules_Execute.json
     */
    /**
     * Sample code: VirtualMachineSchedules_Execute.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void virtualMachineSchedulesExecute(
        com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .virtualMachineSchedules()
            .execute("resourceGroupName", "{labName}", "{vmName}", "LabVmsShutdown", Context.NONE);
    }
}
```

### VirtualMachineSchedules_Get

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachineSchedules Get. */
public final class VirtualMachineSchedulesGetSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/VirtualMachineSchedules_Get.json
     */
    /**
     * Sample code: VirtualMachineSchedules_Get.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void virtualMachineSchedulesGet(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .virtualMachineSchedules()
            .getWithResponse("resourceGroupName", "{labName}", "{vmName}", "LabVmsShutdown", null, Context.NONE);
    }
}
```

### VirtualMachineSchedules_List

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachineSchedules List. */
public final class VirtualMachineSchedulesListSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/VirtualMachineSchedules_List.json
     */
    /**
     * Sample code: VirtualMachineSchedules_List.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void virtualMachineSchedulesList(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .virtualMachineSchedules()
            .list("resourceGroupName", "{labName}", "{vmName}", null, null, null, null, Context.NONE);
    }
}
```

### VirtualMachineSchedules_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.ScheduleFragment;
import java.util.HashMap;
import java.util.Map;

/** Samples for VirtualMachineSchedules Update. */
public final class VirtualMachineSchedulesUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/VirtualMachineSchedules_Update.json
     */
    /**
     * Sample code: VirtualMachineSchedules_Update.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void virtualMachineSchedulesUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .virtualMachineSchedules()
            .updateWithResponse(
                "resourceGroupName",
                "{labName}",
                "{vmName}",
                "LabVmsShutdown",
                new ScheduleFragment().withTags(mapOf("tagName1", "tagValue1")),
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

### VirtualMachines_AddDataDisk

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.AttachNewDataDiskOptions;
import com.azure.resourcemanager.devtestlabs.models.DataDiskProperties;
import com.azure.resourcemanager.devtestlabs.models.StorageType;

/** Samples for VirtualMachines AddDataDisk. */
public final class VirtualMachinesAddDataDiskSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/VirtualMachines_AddDataDisk.json
     */
    /**
     * Sample code: VirtualMachines_AddDataDisk.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void virtualMachinesAddDataDisk(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .virtualMachines()
            .addDataDisk(
                "resourceGroupName",
                "{labName}",
                "{virtualMachineName}",
                new DataDiskProperties()
                    .withAttachNewDataDiskOptions(
                        new AttachNewDataDiskOptions()
                            .withDiskSizeGiB(127)
                            .withDiskName("{diskName}")
                            .withDiskType(StorageType.fromString("{diskType}"))),
                Context.NONE);
    }
}
```

### VirtualMachines_ApplyArtifacts

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.ApplyArtifactsRequest;
import com.azure.resourcemanager.devtestlabs.models.ArtifactInstallProperties;
import java.util.Arrays;

/** Samples for VirtualMachines ApplyArtifacts. */
public final class VirtualMachinesApplyArtifactsSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/VirtualMachines_ApplyArtifacts.json
     */
    /**
     * Sample code: VirtualMachines_ApplyArtifacts.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void virtualMachinesApplyArtifacts(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .virtualMachines()
            .applyArtifacts(
                "resourceGroupName",
                "{labName}",
                "{vmName}",
                new ApplyArtifactsRequest()
                    .withArtifacts(
                        Arrays
                            .asList(
                                new ArtifactInstallProperties()
                                    .withArtifactId(
                                        "/subscriptions/{subscriptionId}/resourceGroups/resourceGroupName/providers/Microsoft.DevTestLab/labs/{labName}/artifactSources/public"
                                            + " repo/artifacts/windows-restart"))),
                Context.NONE);
    }
}
```

### VirtualMachines_Claim

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines Claim. */
public final class VirtualMachinesClaimSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/VirtualMachines_Claim.json
     */
    /**
     * Sample code: VirtualMachines_Claim.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void virtualMachinesClaim(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.virtualMachines().claim("resourceGroupName", "{labName}", "{vmName}", Context.NONE);
    }
}
```

### VirtualMachines_CreateOrUpdate

```java
import com.azure.resourcemanager.devtestlabs.models.GalleryImageReference;
import java.util.HashMap;
import java.util.Map;

/** Samples for VirtualMachines CreateOrUpdate. */
public final class VirtualMachinesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/VirtualMachines_CreateOrUpdate.json
     */
    /**
     * Sample code: VirtualMachines_CreateOrUpdate.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void virtualMachinesCreateOrUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .virtualMachines()
            .define("{vmName}")
            .withRegion("{location}")
            .withExistingLab("resourceGroupName", "{labName}")
            .withTags(mapOf("tagName1", "tagValue1"))
            .withSize("Standard_A2_v2")
            .withUsername("{userName}")
            .withPassword("{userPassword}")
            .withLabSubnetName("{virtualNetworkName}Subnet")
            .withLabVirtualNetworkId(
                "/subscriptions/{subscriptionId}/resourcegroups/resourceGroupName/providers/microsoft.devtestlab/labs/{labName}/virtualnetworks/{virtualNetworkName}")
            .withDisallowPublicIpAddress(true)
            .withGalleryImageReference(
                new GalleryImageReference()
                    .withOffer("UbuntuServer")
                    .withPublisher("Canonical")
                    .withSku("16.04-LTS")
                    .withOsType("Linux")
                    .withVersion("Latest"))
            .withAllowClaim(true)
            .withStorageType("Standard")
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

### VirtualMachines_Delete

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines Delete. */
public final class VirtualMachinesDeleteSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/VirtualMachines_Delete.json
     */
    /**
     * Sample code: VirtualMachines_Delete.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void virtualMachinesDelete(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.virtualMachines().delete("resourceGroupName", "{labName}", "{vmName}", Context.NONE);
    }
}
```

### VirtualMachines_DetachDataDisk

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.DetachDataDiskProperties;

/** Samples for VirtualMachines DetachDataDisk. */
public final class VirtualMachinesDetachDataDiskSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/VirtualMachines_DetachDataDisk.json
     */
    /**
     * Sample code: VirtualMachines_DetachDataDisk.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void virtualMachinesDetachDataDisk(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .virtualMachines()
            .detachDataDisk(
                "resourceGroupName",
                "{labName}",
                "{virtualMachineName}",
                new DetachDataDiskProperties()
                    .withExistingLabDiskId(
                        "/subscriptions/{subscriptionId}/resourcegroups/resourceGroupName/providers/microsoft.devtestlab/labs/{labName}/virtualmachines/{virtualMachineName}"),
                Context.NONE);
    }
}
```

### VirtualMachines_Get

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines Get. */
public final class VirtualMachinesGetSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/VirtualMachines_Get.json
     */
    /**
     * Sample code: VirtualMachines_Get.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void virtualMachinesGet(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.virtualMachines().getWithResponse("resourceGroupName", "{labName}", "{vmName}", null, Context.NONE);
    }
}
```

### VirtualMachines_GetRdpFileContents

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines GetRdpFileContents. */
public final class VirtualMachinesGetRdpFileContentsSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/VirtualMachines_GetRdpFileContents.json
     */
    /**
     * Sample code: VirtualMachines_GetRdpFileContents.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void virtualMachinesGetRdpFileContents(
        com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .virtualMachines()
            .getRdpFileContentsWithResponse("resourceGroupName", "{labName}", "{vmName}", Context.NONE);
    }
}
```

### VirtualMachines_List

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines List. */
public final class VirtualMachinesListSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/VirtualMachines_List.json
     */
    /**
     * Sample code: VirtualMachines_List.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void virtualMachinesList(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.virtualMachines().list("resourceGroupName", "{labName}", null, null, null, null, Context.NONE);
    }
}
```

### VirtualMachines_ListApplicableSchedules

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines ListApplicableSchedules. */
public final class VirtualMachinesListApplicableSchedulesSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/VirtualMachines_ListApplicableSchedules.json
     */
    /**
     * Sample code: VirtualMachines_ListApplicableSchedules.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void virtualMachinesListApplicableSchedules(
        com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .virtualMachines()
            .listApplicableSchedulesWithResponse("resourceGroupName", "{labName}", "{vmName}", Context.NONE);
    }
}
```

### VirtualMachines_Redeploy

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines Redeploy. */
public final class VirtualMachinesRedeploySamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/VirtualMachines_Redeploy.json
     */
    /**
     * Sample code: VirtualMachines_Redeploy.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void virtualMachinesRedeploy(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.virtualMachines().redeploy("resourceGroupName", "{labName}", "{vmName}", Context.NONE);
    }
}
```

### VirtualMachines_Resize

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.ResizeLabVirtualMachineProperties;

/** Samples for VirtualMachines Resize. */
public final class VirtualMachinesResizeSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/VirtualMachines_Resize.json
     */
    /**
     * Sample code: VirtualMachines_Resize.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void virtualMachinesResize(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .virtualMachines()
            .resize(
                "resourceGroupName",
                "{labName}",
                "{vmName}",
                new ResizeLabVirtualMachineProperties().withSize("Standard_A4_v2"),
                Context.NONE);
    }
}
```

### VirtualMachines_Restart

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines Restart. */
public final class VirtualMachinesRestartSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/VirtualMachines_Restart.json
     */
    /**
     * Sample code: VirtualMachines_Restart.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void virtualMachinesRestart(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.virtualMachines().restart("resourceGroupName", "{labName}", "{vmName}", Context.NONE);
    }
}
```

### VirtualMachines_Start

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines Start. */
public final class VirtualMachinesStartSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/VirtualMachines_Start.json
     */
    /**
     * Sample code: VirtualMachines_Start.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void virtualMachinesStart(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.virtualMachines().start("resourceGroupName", "{labName}", "{vmName}", Context.NONE);
    }
}
```

### VirtualMachines_Stop

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines Stop. */
public final class VirtualMachinesStopSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/VirtualMachines_Stop.json
     */
    /**
     * Sample code: VirtualMachines_Stop.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void virtualMachinesStop(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.virtualMachines().stop("resourceGroupName", "{labName}", "{vmName}", Context.NONE);
    }
}
```

### VirtualMachines_TransferDisks

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines TransferDisks. */
public final class VirtualMachinesTransferDisksSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/VirtualMachines_TransferDisks.json
     */
    /**
     * Sample code: VirtualMachines_TransferDisks.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void virtualMachinesTransferDisks(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.virtualMachines().transferDisks("resourceGroupName", "{labName}", "{virtualmachineName}", Context.NONE);
    }
}
```

### VirtualMachines_UnClaim

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines UnClaim. */
public final class VirtualMachinesUnClaimSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/VirtualMachines_UnClaim.json
     */
    /**
     * Sample code: VirtualMachines_UnClaim.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void virtualMachinesUnClaim(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.virtualMachines().unClaim("resourceGroupName", "{labName}", "{vmName}", Context.NONE);
    }
}
```

### VirtualMachines_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.LabVirtualMachine;

/** Samples for VirtualMachines Update. */
public final class VirtualMachinesUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/VirtualMachines_Update.json
     */
    /**
     * Sample code: VirtualMachines_Update.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void virtualMachinesUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        LabVirtualMachine resource =
            manager
                .virtualMachines()
                .getWithResponse("resourceGroupName", "{labName}", "{vmName}", null, Context.NONE)
                .getValue();
        resource.update().apply();
    }
}
```

### VirtualNetworks_CreateOrUpdate

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for VirtualNetworks CreateOrUpdate. */
public final class VirtualNetworksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/VirtualNetworks_CreateOrUpdate.json
     */
    /**
     * Sample code: VirtualNetworks_CreateOrUpdate.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void virtualNetworksCreateOrUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .virtualNetworks()
            .define("{virtualNetworkName}")
            .withRegion("{location}")
            .withExistingLab("resourceGroupName", "{labName}")
            .withTags(mapOf("tagName1", "tagValue1"))
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

### VirtualNetworks_Delete

```java
import com.azure.core.util.Context;

/** Samples for VirtualNetworks Delete. */
public final class VirtualNetworksDeleteSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/VirtualNetworks_Delete.json
     */
    /**
     * Sample code: VirtualNetworks_Delete.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void virtualNetworksDelete(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.virtualNetworks().delete("resourceGroupName", "{labName}", "{virtualNetworkName}", Context.NONE);
    }
}
```

### VirtualNetworks_Get

```java
import com.azure.core.util.Context;

/** Samples for VirtualNetworks Get. */
public final class VirtualNetworksGetSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/VirtualNetworks_Get.json
     */
    /**
     * Sample code: VirtualNetworks_Get.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void virtualNetworksGet(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager
            .virtualNetworks()
            .getWithResponse("resourceGroupName", "{labName}", "{virtualNetworkName}", null, Context.NONE);
    }
}
```

### VirtualNetworks_List

```java
import com.azure.core.util.Context;

/** Samples for VirtualNetworks List. */
public final class VirtualNetworksListSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/VirtualNetworks_List.json
     */
    /**
     * Sample code: VirtualNetworks_List.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void virtualNetworksList(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.virtualNetworks().list("resourceGroupName", "{labName}", null, null, null, null, Context.NONE);
    }
}
```

### VirtualNetworks_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devtestlabs.models.VirtualNetwork;
import java.util.HashMap;
import java.util.Map;

/** Samples for VirtualNetworks Update. */
public final class VirtualNetworksUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/VirtualNetworks_Update.json
     */
    /**
     * Sample code: VirtualNetworks_Update.
     *
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void virtualNetworksUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        VirtualNetwork resource =
            manager
                .virtualNetworks()
                .getWithResponse("resourceGroupName", "{labName}", "{virtualNetworkName}", null, Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tagName1", "tagValue1")).apply();
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

