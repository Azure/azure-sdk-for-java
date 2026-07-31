# Code snippets and samples


## Artifacts

- [Get](#artifacts_get)
- [ListByParent](#artifacts_listbyparent)
- [ListDownloadUri](#artifacts_listdownloaduri)

## DisconnectedOperations

- [CreateOrUpdate](#disconnectedoperations_createorupdate)
- [Delete](#disconnectedoperations_delete)
- [GetByResourceGroup](#disconnectedoperations_getbyresourcegroup)
- [List](#disconnectedoperations_list)
- [ListByResourceGroup](#disconnectedoperations_listbyresourcegroup)
- [ListDeploymentManifest](#disconnectedoperations_listdeploymentmanifest)
- [Update](#disconnectedoperations_update)

## HardwareSettings

- [CreateOrUpdate](#hardwaresettings_createorupdate)
- [Delete](#hardwaresettings_delete)
- [Get](#hardwaresettings_get)
- [ListByParent](#hardwaresettings_listbyparent)

## Images

- [Get](#images_get)
- [ListByDisconnectedOperation](#images_listbydisconnectedoperation)
- [ListDownloadUri](#images_listdownloaduri)
### Artifacts_Get

```java
/**
 * Samples for Artifacts Get.
 */
public final class ArtifactsGetSamples {
    /*
     * x-ms-original-file: 2026-03-15/Artifacts_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Artifacts_Get.
     * 
     * @param manager Entry point to DisconnectedOperationsManager.
     */
    public static void
        artifactsGet(com.azure.resourcemanager.disconnectedoperations.DisconnectedOperationsManager manager) {
        manager.artifacts()
            .getWithResponse("rgdisconnectedoperations", "J_3-_S--_-UM_-_7w11", "PMY-", "-8Y-Us1BNNG6-H5w6-2--RP",
                com.azure.core.util.Context.NONE);
    }
}
```

### Artifacts_ListByParent

```java
/**
 * Samples for Artifacts ListByParent.
 */
public final class ArtifactsListByParentSamples {
    /*
     * x-ms-original-file: 2026-03-15/Artifact_ListByParent_MaximumSet_Gen.json
     */
    /**
     * Sample code: Artifacts_ListByParent.
     * 
     * @param manager Entry point to DisconnectedOperationsManager.
     */
    public static void
        artifactsListByParent(com.azure.resourcemanager.disconnectedoperations.DisconnectedOperationsManager manager) {
        manager.artifacts()
            .listByParent("rgdisconnectedoperations", "XOn_Y-7_M-46E-Y", "2v5Q3mNihPV88C882LnbQO8",
                com.azure.core.util.Context.NONE);
    }
}
```

### Artifacts_ListDownloadUri

```java
/**
 * Samples for Artifacts ListDownloadUri.
 */
public final class ArtifactsListDownloadUriSamples {
    /*
     * x-ms-original-file: 2026-03-15/Artifact_ListDownloadUri_MaximumSet_Gen.json
     */
    /**
     * Sample code: Artifacts_ListDownloadUri.
     * 
     * @param manager Entry point to DisconnectedOperationsManager.
     */
    public static void artifactsListDownloadUri(
        com.azure.resourcemanager.disconnectedoperations.DisconnectedOperationsManager manager) {
        manager.artifacts()
            .listDownloadUriWithResponse("rgdisconnectedoperations", "L4z_-S", "B-Ra--W0", "artifact1",
                com.azure.core.util.Context.NONE);
    }
}
```

### DisconnectedOperations_CreateOrUpdate

```java
import com.azure.resourcemanager.disconnectedoperations.models.AutoRenew;
import com.azure.resourcemanager.disconnectedoperations.models.BenefitPlanStatus;
import com.azure.resourcemanager.disconnectedoperations.models.BenefitPlans;
import com.azure.resourcemanager.disconnectedoperations.models.BillingConfiguration;
import com.azure.resourcemanager.disconnectedoperations.models.BillingPeriod;
import com.azure.resourcemanager.disconnectedoperations.models.ConnectionIntent;
import com.azure.resourcemanager.disconnectedoperations.models.DisconnectedOperationProperties;
import com.azure.resourcemanager.disconnectedoperations.models.PricingModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DisconnectedOperations CreateOrUpdate.
 */
public final class DisconnectedOperationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-03-15/DisconnectedOperations_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: DisconnectedOperations_CreateOrUpdate.
     * 
     * @param manager Entry point to DisconnectedOperationsManager.
     */
    public static void disconnectedOperationsCreateOrUpdate(
        com.azure.resourcemanager.disconnectedoperations.DisconnectedOperationsManager manager) {
        manager.disconnectedOperations()
            .define("demo-resource")
            .withRegion("eastus")
            .withExistingResourceGroup("rgdisconnectedOperations")
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
            .withProperties(new DisconnectedOperationProperties().withConnectionIntent(ConnectionIntent.DISCONNECTED)
                .withBillingConfiguration(new BillingConfiguration().withAutoRenew(AutoRenew.ENABLED)
                    .withCurrent(new BillingPeriod().withCores(12).withPricingModel(PricingModel.TRIAL)))
                .withBenefitPlans(new BenefitPlans().withAzureHybridWindowsServerBenefit(BenefitPlanStatus.ENABLED)
                    .withWindowsServerVmCount(5)))
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

### DisconnectedOperations_Delete

```java
/**
 * Samples for DisconnectedOperations Delete.
 */
public final class DisconnectedOperationsDeleteSamples {
    /*
     * x-ms-original-file: 2026-03-15/DisconnectedOperations_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: DisconnectedOperations_Delete.
     * 
     * @param manager Entry point to DisconnectedOperationsManager.
     */
    public static void disconnectedOperationsDelete(
        com.azure.resourcemanager.disconnectedoperations.DisconnectedOperationsManager manager) {
        manager.disconnectedOperations()
            .delete("rgdisconnectedoperations", "demo-resource", com.azure.core.util.Context.NONE);
    }
}
```

### DisconnectedOperations_GetByResourceGroup

```java
/**
 * Samples for DisconnectedOperations GetByResourceGroup.
 */
public final class DisconnectedOperationsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-03-15/DisconnectedOperations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: DisconnectedOperations_Get.
     * 
     * @param manager Entry point to DisconnectedOperationsManager.
     */
    public static void disconnectedOperationsGet(
        com.azure.resourcemanager.disconnectedoperations.DisconnectedOperationsManager manager) {
        manager.disconnectedOperations()
            .getByResourceGroupWithResponse("rgdisconnectedoperations", "demo-resource",
                com.azure.core.util.Context.NONE);
    }
}
```

### DisconnectedOperations_List

```java
/**
 * Samples for DisconnectedOperations List.
 */
public final class DisconnectedOperationsListSamples {
    /*
     * x-ms-original-file: 2026-03-15/DisconnectedOperations_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: DisconnectedOperations_ListBySubscription.
     * 
     * @param manager Entry point to DisconnectedOperationsManager.
     */
    public static void disconnectedOperationsListBySubscription(
        com.azure.resourcemanager.disconnectedoperations.DisconnectedOperationsManager manager) {
        manager.disconnectedOperations().list(com.azure.core.util.Context.NONE);
    }
}
```

### DisconnectedOperations_ListByResourceGroup

```java
/**
 * Samples for DisconnectedOperations ListByResourceGroup.
 */
public final class DisconnectedOperationsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-03-15/DisconnectedOperations_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: DisconnectedOperations_ListByResourceGroup.
     * 
     * @param manager Entry point to DisconnectedOperationsManager.
     */
    public static void disconnectedOperationsListByResourceGroup(
        com.azure.resourcemanager.disconnectedoperations.DisconnectedOperationsManager manager) {
        manager.disconnectedOperations()
            .listByResourceGroup("rgdisconnectedoperations", com.azure.core.util.Context.NONE);
    }
}
```

### DisconnectedOperations_ListDeploymentManifest

```java
/**
 * Samples for DisconnectedOperations ListDeploymentManifest.
 */
public final class DisconnectedOperationsListDeploymentManifestSamples {
    /*
     * x-ms-original-file: 2026-03-15/DisconnectedOperations_ListDeploymentManifest_MaximumSet_Gen.json
     */
    /**
     * Sample code: DisconnectedOperations_ListDeploymentManifest.
     * 
     * @param manager Entry point to DisconnectedOperationsManager.
     */
    public static void disconnectedOperationsListDeploymentManifest(
        com.azure.resourcemanager.disconnectedoperations.DisconnectedOperationsManager manager) {
        manager.disconnectedOperations()
            .listDeploymentManifestWithResponse("rgdisconnectedoperations", "demo-resource",
                com.azure.core.util.Context.NONE);
    }
}
```

### DisconnectedOperations_Update

```java
import com.azure.resourcemanager.disconnectedoperations.models.ConnectionIntent;
import com.azure.resourcemanager.disconnectedoperations.models.DisconnectedOperation;
import com.azure.resourcemanager.disconnectedoperations.models.DisconnectedOperationUpdateProperties;
import com.azure.resourcemanager.disconnectedoperations.models.RegistrationStatus;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DisconnectedOperations Update.
 */
public final class DisconnectedOperationsUpdateSamples {
    /*
     * x-ms-original-file: 2026-03-15/DisconnectedOperations_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: DisconnectedOperations_Update.
     * 
     * @param manager Entry point to DisconnectedOperationsManager.
     */
    public static void disconnectedOperationsUpdate(
        com.azure.resourcemanager.disconnectedoperations.DisconnectedOperationsManager manager) {
        DisconnectedOperation resource = manager.disconnectedOperations()
            .getByResourceGroupWithResponse("rgdisconnectedoperations", "demo-resource",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key2", "fakeTokenPlaceholder"))
            .withProperties(new DisconnectedOperationUpdateProperties().withConnectionIntent(ConnectionIntent.CONNECTED)
                .withRegistrationStatus(RegistrationStatus.REGISTERED)
                .withDeviceVersion("2.0.0"))
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

### HardwareSettings_CreateOrUpdate

```java
import com.azure.resourcemanager.disconnectedoperations.models.HardwareSettingProperties;

/**
 * Samples for HardwareSettings CreateOrUpdate.
 */
public final class HardwareSettingsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-03-15/HardwareSettings_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: HardwareSettings_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to DisconnectedOperationsManager.
     */
    public static void hardwareSettingsCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.disconnectedoperations.DisconnectedOperationsManager manager) {
        manager.hardwareSettings()
            .define("default")
            .withExistingDisconnectedOperation("rgdisconnectedOperations", "demo-resource")
            .withProperties(new HardwareSettingProperties().withTotalCores(200)
                .withDiskSpaceInGb(1024)
                .withMemoryInGb(64)
                .withOem("Contoso")
                .withHardwareSku("MC-760")
                .withNodes(3)
                .withVersionAtRegistration("2411.2")
                .withSolutionBuilderExtension("xyz")
                .withDeviceId("663ee8a3-4ea8-48ec-8810-b1f8b86cb270"))
            .create();
    }
}
```

### HardwareSettings_Delete

```java
/**
 * Samples for HardwareSettings Delete.
 */
public final class HardwareSettingsDeleteSamples {
    /*
     * x-ms-original-file: 2026-03-15/HardwareSettings_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: HardwareSettings_Delete_MaximumSet.
     * 
     * @param manager Entry point to DisconnectedOperationsManager.
     */
    public static void hardwareSettingsDeleteMaximumSet(
        com.azure.resourcemanager.disconnectedoperations.DisconnectedOperationsManager manager) {
        manager.hardwareSettings()
            .delete("rgdisconnectedOperations", "demo-resource", "default", com.azure.core.util.Context.NONE);
    }
}
```

### HardwareSettings_Get

```java
/**
 * Samples for HardwareSettings Get.
 */
public final class HardwareSettingsGetSamples {
    /*
     * x-ms-original-file: 2026-03-15/HardwareSettings_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: HardwareSettings_Get_MaximumSet.
     * 
     * @param manager Entry point to DisconnectedOperationsManager.
     */
    public static void hardwareSettingsGetMaximumSet(
        com.azure.resourcemanager.disconnectedoperations.DisconnectedOperationsManager manager) {
        manager.hardwareSettings()
            .getWithResponse("rgdisconnectedOperations", "demo-resource", "default", com.azure.core.util.Context.NONE);
    }
}
```

### HardwareSettings_ListByParent

```java
/**
 * Samples for HardwareSettings ListByParent.
 */
public final class HardwareSettingsListByParentSamples {
    /*
     * x-ms-original-file: 2026-03-15/HardwareSettings_ListByParent_MaximumSet_Gen.json
     */
    /**
     * Sample code: HardwareSettings_ListByParent_MaximumSet.
     * 
     * @param manager Entry point to DisconnectedOperationsManager.
     */
    public static void hardwareSettingsListByParentMaximumSet(
        com.azure.resourcemanager.disconnectedoperations.DisconnectedOperationsManager manager) {
        manager.hardwareSettings()
            .listByParent("rgdisconnectedOperations", "demo-resource", com.azure.core.util.Context.NONE);
    }
}
```

### Images_Get

```java
/**
 * Samples for Images Get.
 */
public final class ImagesGetSamples {
    /*
     * x-ms-original-file: 2026-03-15/Images_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Images_Get.
     * 
     * @param manager Entry point to DisconnectedOperationsManager.
     */
    public static void
        imagesGet(com.azure.resourcemanager.disconnectedoperations.DisconnectedOperationsManager manager) {
        manager.images()
            .getWithResponse("rgdisconnectedoperations", "bT62l-KS7g1-uh", "2P6", com.azure.core.util.Context.NONE);
    }
}
```

### Images_ListByDisconnectedOperation

```java
/**
 * Samples for Images ListByDisconnectedOperation.
 */
public final class ImagesListByDisconnectedOperationSamples {
    /*
     * x-ms-original-file: 2026-03-15/Images_ListByDisconnectedOperation_MaximumSet_Gen.json
     */
    /**
     * Sample code: Images_ListByDisconnectedOperation.
     * 
     * @param manager Entry point to DisconnectedOperationsManager.
     */
    public static void imagesListByDisconnectedOperation(
        com.azure.resourcemanager.disconnectedoperations.DisconnectedOperationsManager manager) {
        manager.images()
            .listByDisconnectedOperation("rgdisconnectedoperations", "w_-EG-3-euL7K3-E", "toynendoobwkrcwmfdfup", 20, 3,
                com.azure.core.util.Context.NONE);
    }
}
```

### Images_ListDownloadUri

```java
/**
 * Samples for Images ListDownloadUri.
 */
public final class ImagesListDownloadUriSamples {
    /*
     * x-ms-original-file: 2026-03-15/Images_ListDownloadUri_MaximumSet_Gen.json
     */
    /**
     * Sample code: Images_ListDownloadUri.
     * 
     * @param manager Entry point to DisconnectedOperationsManager.
     */
    public static void
        imagesListDownloadUri(com.azure.resourcemanager.disconnectedoperations.DisconnectedOperationsManager manager) {
        manager.images()
            .listDownloadUriWithResponse("rgdisconnectedOperations", "g_-5-160", "1Q6lGV4V65j-1",
                com.azure.core.util.Context.NONE);
    }
}
```

