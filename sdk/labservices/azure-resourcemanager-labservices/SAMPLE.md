# Code snippets and samples


## Images

- [CreateOrUpdate](#images_createorupdate)
- [Get](#images_get)
- [ListByLabPlan](#images_listbylabplan)
- [Update](#images_update)

## LabPlans

- [CreateOrUpdate](#labplans_createorupdate)
- [Delete](#labplans_delete)
- [GetByResourceGroup](#labplans_getbyresourcegroup)
- [List](#labplans_list)
- [ListByResourceGroup](#labplans_listbyresourcegroup)
- [SaveImage](#labplans_saveimage)
- [Update](#labplans_update)

## Labs

- [CreateOrUpdate](#labs_createorupdate)
- [Delete](#labs_delete)
- [GetByResourceGroup](#labs_getbyresourcegroup)
- [List](#labs_list)
- [ListByResourceGroup](#labs_listbyresourcegroup)
- [Publish](#labs_publish)
- [SyncGroup](#labs_syncgroup)
- [Update](#labs_update)

## OperationResults

- [Get](#operationresults_get)

## Operations

- [List](#operations_list)

## Schedules

- [CreateOrUpdate](#schedules_createorupdate)
- [Delete](#schedules_delete)
- [Get](#schedules_get)
- [ListByLab](#schedules_listbylab)
- [Update](#schedules_update)

## Skus

- [List](#skus_list)

## Usages

- [ListByLocation](#usages_listbylocation)

## Users

- [CreateOrUpdate](#users_createorupdate)
- [Delete](#users_delete)
- [Get](#users_get)
- [Invite](#users_invite)
- [ListByLab](#users_listbylab)
- [Update](#users_update)

## VirtualMachines

- [Get](#virtualmachines_get)
- [ListByLab](#virtualmachines_listbylab)
- [Redeploy](#virtualmachines_redeploy)
- [Reimage](#virtualmachines_reimage)
- [ResetPassword](#virtualmachines_resetpassword)
- [Start](#virtualmachines_start)
- [Stop](#virtualmachines_stop)
### Images_CreateOrUpdate

```java
import com.azure.resourcemanager.labservices.models.EnableState;

/** Samples for Images CreateOrUpdate. */
public final class ImagesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/Images/putImage.json
     */
    /**
     * Sample code: putImage.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void putImage(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager
            .images()
            .define("image1")
            .withExistingLabPlan("testrg123", "testlabplan")
            .withEnabledState(EnableState.ENABLED)
            .create();
    }
}
```

### Images_Get

```java
import com.azure.core.util.Context;

/** Samples for Images Get. */
public final class ImagesGetSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/Images/getImage.json
     */
    /**
     * Sample code: getImage.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void getImage(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager.images().getWithResponse("testrg123", "testlabplan", "image1", Context.NONE);
    }
}
```

### Images_ListByLabPlan

```java
import com.azure.core.util.Context;

/** Samples for Images ListByLabPlan. */
public final class ImagesListByLabPlanSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/Images/listImages.json
     */
    /**
     * Sample code: listImages.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void listImages(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager.images().listByLabPlan("testrg123", "testlabplan", null, Context.NONE);
    }
}
```

### Images_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.labservices.models.EnableState;
import com.azure.resourcemanager.labservices.models.Image;

/** Samples for Images Update. */
public final class ImagesUpdateSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/Images/patchImage.json
     */
    /**
     * Sample code: patchImage.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void patchImage(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        Image resource =
            manager.images().getWithResponse("testrg123", "testlabplan", "image1", Context.NONE).getValue();
        resource.update().withEnabledState(EnableState.ENABLED).apply();
    }
}
```

### LabPlans_CreateOrUpdate

```java
import com.azure.resourcemanager.labservices.models.AutoShutdownProfile;
import com.azure.resourcemanager.labservices.models.ConnectionProfile;
import com.azure.resourcemanager.labservices.models.ConnectionType;
import com.azure.resourcemanager.labservices.models.EnableState;
import com.azure.resourcemanager.labservices.models.LabPlanNetworkProfile;
import com.azure.resourcemanager.labservices.models.ShutdownOnIdleMode;
import com.azure.resourcemanager.labservices.models.SupportInfo;
import java.time.Duration;

/** Samples for LabPlans CreateOrUpdate. */
public final class LabPlansCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/LabPlans/putLabPlan.json
     */
    /**
     * Sample code: putLabPlan.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void putLabPlan(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager
            .labPlans()
            .define("testlabplan")
            .withRegion("westus")
            .withExistingResourceGroup("testrg123")
            .withDefaultConnectionProfile(
                new ConnectionProfile()
                    .withWebSshAccess(ConnectionType.NONE)
                    .withWebRdpAccess(ConnectionType.NONE)
                    .withClientSshAccess(ConnectionType.PUBLIC)
                    .withClientRdpAccess(ConnectionType.PUBLIC))
            .withDefaultAutoShutdownProfile(
                new AutoShutdownProfile()
                    .withShutdownOnDisconnect(EnableState.ENABLED)
                    .withShutdownWhenNotConnected(EnableState.ENABLED)
                    .withShutdownOnIdle(ShutdownOnIdleMode.USER_ABSENCE)
                    .withDisconnectDelay(Duration.parse("PT5M"))
                    .withNoConnectDelay(Duration.parse("PT5M"))
                    .withIdleDelay(Duration.parse("PT5M")))
            .withDefaultNetworkProfile(
                new LabPlanNetworkProfile()
                    .withSubnetId(
                        "/subscriptions/34adfa4f-cedf-4dc0-ba29-b6d1a69ab345/resourceGroups/testrg123/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/default"))
            .withSharedGalleryId(
                "/subscriptions/34adfa4f-cedf-4dc0-ba29-b6d1a69ab345/resourceGroups/testrg123/providers/Microsoft.Compute/galleries/testsig")
            .withSupportInfo(
                new SupportInfo()
                    .withUrl("help.contoso.com")
                    .withEmail("help@contoso.com")
                    .withPhone("+1-202-555-0123")
                    .withInstructions("Contact support for help."))
            .create();
    }
}
```

### LabPlans_Delete

```java
import com.azure.core.util.Context;

/** Samples for LabPlans Delete. */
public final class LabPlansDeleteSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/LabPlans/deleteLabPlan.json
     */
    /**
     * Sample code: deleteLabPlan.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void deleteLabPlan(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager.labPlans().delete("testrg123", "testlabplan", Context.NONE);
    }
}
```

### LabPlans_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for LabPlans GetByResourceGroup. */
public final class LabPlansGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/LabPlans/getLabPlan.json
     */
    /**
     * Sample code: getLabPlan.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void getLabPlan(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager.labPlans().getByResourceGroupWithResponse("testrg123", "testlabplan", Context.NONE);
    }
}
```

### LabPlans_List

```java
import com.azure.core.util.Context;

/** Samples for LabPlans List. */
public final class LabPlansListSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/LabPlans/listLabPlans.json
     */
    /**
     * Sample code: listLabPlans.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void listLabPlans(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager.labPlans().list(null, Context.NONE);
    }
}
```

### LabPlans_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for LabPlans ListByResourceGroup. */
public final class LabPlansListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/LabPlans/listResourceGroupLabPlans.json
     */
    /**
     * Sample code: listResourceGroupLabPlans.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void listResourceGroupLabPlans(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager.labPlans().listByResourceGroup("testrg123", Context.NONE);
    }
}
```

### LabPlans_SaveImage

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.labservices.models.SaveImageBody;

/** Samples for LabPlans SaveImage. */
public final class LabPlansSaveImageSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/LabPlans/saveImageVirtualMachine.json
     */
    /**
     * Sample code: saveImageVirtualMachine.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void saveImageVirtualMachine(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager
            .labPlans()
            .saveImage(
                "testrg123",
                "testlabplan",
                new SaveImageBody()
                    .withName("Test Image")
                    .withLabVirtualMachineId(
                        "/subscriptions/34adfa4f-cedf-4dc0-ba29-b6d1a69ab345/resourceGroups/testrg123/providers/Microsoft.LabServices/labs/testlab/virtualMachines/template"),
                Context.NONE);
    }
}
```

### LabPlans_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.labservices.models.ConnectionProfile;
import com.azure.resourcemanager.labservices.models.ConnectionType;
import com.azure.resourcemanager.labservices.models.LabPlan;

/** Samples for LabPlans Update. */
public final class LabPlansUpdateSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/LabPlans/patchLabPlan.json
     */
    /**
     * Sample code: patchLabPlan.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void patchLabPlan(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        LabPlan resource =
            manager.labPlans().getByResourceGroupWithResponse("testrg123", "testlabplan", Context.NONE).getValue();
        resource
            .update()
            .withDefaultConnectionProfile(
                new ConnectionProfile()
                    .withWebSshAccess(ConnectionType.NONE)
                    .withWebRdpAccess(ConnectionType.NONE)
                    .withClientSshAccess(ConnectionType.PUBLIC)
                    .withClientRdpAccess(ConnectionType.PUBLIC))
            .apply();
    }
}
```

### Labs_CreateOrUpdate

```java
import com.azure.resourcemanager.labservices.models.AutoShutdownProfile;
import com.azure.resourcemanager.labservices.models.ConnectionProfile;
import com.azure.resourcemanager.labservices.models.ConnectionType;
import com.azure.resourcemanager.labservices.models.CreateOption;
import com.azure.resourcemanager.labservices.models.Credentials;
import com.azure.resourcemanager.labservices.models.EnableState;
import com.azure.resourcemanager.labservices.models.ImageReference;
import com.azure.resourcemanager.labservices.models.LabNetworkProfile;
import com.azure.resourcemanager.labservices.models.SecurityProfile;
import com.azure.resourcemanager.labservices.models.ShutdownOnIdleMode;
import com.azure.resourcemanager.labservices.models.Sku;
import com.azure.resourcemanager.labservices.models.VirtualMachineAdditionalCapabilities;
import com.azure.resourcemanager.labservices.models.VirtualMachineProfile;
import java.time.Duration;

/** Samples for Labs CreateOrUpdate. */
public final class LabsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/Labs/putLab.json
     */
    /**
     * Sample code: putLab.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void putLab(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager
            .labs()
            .define("testlab")
            .withRegion("westus")
            .withExistingResourceGroup("testrg123")
            .withNetworkProfile(
                new LabNetworkProfile()
                    .withSubnetId(
                        "/subscriptions/34adfa4f-cedf-4dc0-ba29-b6d1a69ab345/resourceGroups/testrg123/providers/Microsoft.Network/virtualNetworks/test-vnet/subnets/default"))
            .withAutoShutdownProfile(
                new AutoShutdownProfile()
                    .withShutdownOnDisconnect(EnableState.ENABLED)
                    .withShutdownWhenNotConnected(EnableState.ENABLED)
                    .withShutdownOnIdle(ShutdownOnIdleMode.USER_ABSENCE)
                    .withDisconnectDelay(Duration.parse("PT5M"))
                    .withNoConnectDelay(Duration.parse("PT5M"))
                    .withIdleDelay(Duration.parse("PT5M")))
            .withConnectionProfile(
                new ConnectionProfile()
                    .withWebSshAccess(ConnectionType.NONE)
                    .withWebRdpAccess(ConnectionType.NONE)
                    .withClientSshAccess(ConnectionType.PUBLIC)
                    .withClientRdpAccess(ConnectionType.PUBLIC))
            .withVirtualMachineProfile(
                new VirtualMachineProfile()
                    .withCreateOption(CreateOption.TEMPLATE_VM)
                    .withImageReference(
                        new ImageReference()
                            .withOffer("WindowsServer")
                            .withPublisher("Microsoft")
                            .withSku("2019-Datacenter")
                            .withVersion("2019.0.20190410"))
                    .withSku(new Sku().withName("Medium"))
                    .withAdditionalCapabilities(
                        new VirtualMachineAdditionalCapabilities().withInstallGpuDrivers(EnableState.DISABLED))
                    .withUsageQuota(Duration.parse("PT10H"))
                    .withUseSharedPassword(EnableState.DISABLED)
                    .withAdminUser(new Credentials().withUsername("test-user")))
            .withSecurityProfile(new SecurityProfile().withOpenAccess(EnableState.DISABLED))
            .withLabPlanId(
                "/subscriptions/34adfa4f-cedf-4dc0-ba29-b6d1a69ab345/resourceGroups/testrg123/providers/Microsoft.LabServices/labPlans/testlabplan")
            .withTitle("Test Lab")
            .withDescription("This is a test lab.")
            .create();
    }
}
```

### Labs_Delete

```java
import com.azure.core.util.Context;

/** Samples for Labs Delete. */
public final class LabsDeleteSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/Labs/deleteLab.json
     */
    /**
     * Sample code: deleteLab.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void deleteLab(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager.labs().delete("testrg123", "testlab", Context.NONE);
    }
}
```

### Labs_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Labs GetByResourceGroup. */
public final class LabsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/Labs/getLab.json
     */
    /**
     * Sample code: getLab.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void getLab(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager.labs().getByResourceGroupWithResponse("testrg123", "testlab", Context.NONE);
    }
}
```

### Labs_List

```java
import com.azure.core.util.Context;

/** Samples for Labs List. */
public final class LabsListSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/Labs/listLabs.json
     */
    /**
     * Sample code: listLabs.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void listLabs(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager.labs().list(null, Context.NONE);
    }
}
```

### Labs_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Labs ListByResourceGroup. */
public final class LabsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/Labs/listResourceGroupLabs.json
     */
    /**
     * Sample code: listResourceGroupLabs.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void listResourceGroupLabs(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager.labs().listByResourceGroup("testrg123", Context.NONE);
    }
}
```

### Labs_Publish

```java
import com.azure.core.util.Context;

/** Samples for Labs Publish. */
public final class LabsPublishSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/Labs/publishLab.json
     */
    /**
     * Sample code: publishLab.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void publishLab(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager.labs().publish("testrg123", "testlab", Context.NONE);
    }
}
```

### Labs_SyncGroup

```java
import com.azure.core.util.Context;

/** Samples for Labs SyncGroup. */
public final class LabsSyncGroupSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/Labs/syncLab.json
     */
    /**
     * Sample code: syncLab.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void syncLab(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager.labs().syncGroup("testrg123", "testlab", Context.NONE);
    }
}
```

### Labs_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.labservices.models.EnableState;
import com.azure.resourcemanager.labservices.models.Lab;
import com.azure.resourcemanager.labservices.models.SecurityProfile;

/** Samples for Labs Update. */
public final class LabsUpdateSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/Labs/patchLab.json
     */
    /**
     * Sample code: patchLab.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void patchLab(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        Lab resource = manager.labs().getByResourceGroupWithResponse("testrg123", "testlab", Context.NONE).getValue();
        resource.update().withSecurityProfile(new SecurityProfile().withOpenAccess(EnableState.ENABLED)).apply();
    }
}
```

### OperationResults_Get

```java
import com.azure.core.util.Context;

/** Samples for OperationResults Get. */
public final class OperationResultsGetSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/OperationResults/getOperationResult.json
     */
    /**
     * Sample code: getOperationResult.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void getOperationResult(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager.operationResults().getWithResponse("a64149d8-84cb-4566-ab8e-b4ee1a074174", Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/LabServices/listOperations.json
     */
    /**
     * Sample code: listOperations.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void listOperations(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### Schedules_CreateOrUpdate

```java
import com.azure.resourcemanager.labservices.models.RecurrenceFrequency;
import com.azure.resourcemanager.labservices.models.RecurrencePattern;
import java.time.OffsetDateTime;

/** Samples for Schedules CreateOrUpdate. */
public final class SchedulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/Schedules/putSchedule.json
     */
    /**
     * Sample code: putSchedule.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void putSchedule(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager
            .schedules()
            .define("schedule1")
            .withExistingLab("testrg123", "testlab")
            .withStartAt(OffsetDateTime.parse("2020-05-26T12:00:00Z"))
            .withStopAt(OffsetDateTime.parse("2020-05-26T18:00:00Z"))
            .withRecurrencePattern(
                new RecurrencePattern()
                    .withFrequency(RecurrenceFrequency.DAILY)
                    .withInterval(2)
                    .withExpirationDate(OffsetDateTime.parse("2020-08-14T23:59:59Z")))
            .withTimeZoneId("America/Los_Angeles")
            .withNotes("Schedule 1 for students")
            .create();
    }
}
```

### Schedules_Delete

```java
import com.azure.core.util.Context;

/** Samples for Schedules Delete. */
public final class SchedulesDeleteSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/Schedules/deleteSchedule.json
     */
    /**
     * Sample code: deleteSchedule.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void deleteSchedule(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager.schedules().delete("testrg123", "testlab", "schedule1", Context.NONE);
    }
}
```

### Schedules_Get

```java
import com.azure.core.util.Context;

/** Samples for Schedules Get. */
public final class SchedulesGetSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/Schedules/getSchedule.json
     */
    /**
     * Sample code: getSchedule.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void getSchedule(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager.schedules().getWithResponse("testrg123", "testlab", "schedule1", Context.NONE);
    }
}
```

### Schedules_ListByLab

```java
import com.azure.core.util.Context;

/** Samples for Schedules ListByLab. */
public final class SchedulesListByLabSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/Schedules/listSchedule.json
     */
    /**
     * Sample code: getListSchedule.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void getListSchedule(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager.schedules().listByLab("testrg123", "testlab", null, Context.NONE);
    }
}
```

### Schedules_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.labservices.models.RecurrenceFrequency;
import com.azure.resourcemanager.labservices.models.RecurrencePattern;
import com.azure.resourcemanager.labservices.models.Schedule;
import java.time.OffsetDateTime;

/** Samples for Schedules Update. */
public final class SchedulesUpdateSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/Schedules/patchSchedule.json
     */
    /**
     * Sample code: patchSchedule.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void patchSchedule(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        Schedule resource =
            manager.schedules().getWithResponse("testrg123", "testlab", "schedule1", Context.NONE).getValue();
        resource
            .update()
            .withRecurrencePattern(
                new RecurrencePattern()
                    .withFrequency(RecurrenceFrequency.DAILY)
                    .withInterval(2)
                    .withExpirationDate(OffsetDateTime.parse("2020-08-14T23:59:59Z")))
            .apply();
    }
}
```

### Skus_List

```java
import com.azure.core.util.Context;

/** Samples for Skus List. */
public final class SkusListSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/Skus/listSkus.json
     */
    /**
     * Sample code: listSkus.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void listSkus(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager.skus().list(null, Context.NONE);
    }
}
```

### Usages_ListByLocation

```java
import com.azure.core.util.Context;

/** Samples for Usages ListByLocation. */
public final class UsagesListByLocationSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/Usages/getUsages.json
     */
    /**
     * Sample code: listUsages.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void listUsages(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager.usages().listByLocation("eastus2", null, Context.NONE);
    }
}
```

### Users_CreateOrUpdate

```java
import java.time.Duration;

/** Samples for Users CreateOrUpdate. */
public final class UsersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/Users/putUser.json
     */
    /**
     * Sample code: putUser.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void putUser(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager
            .users()
            .define("testuser")
            .withExistingLab("testrg123", "testlab")
            .withEmail("testuser@contoso.com")
            .withAdditionalUsageQuota(Duration.parse("PT10H"))
            .create();
    }
}
```

### Users_Delete

```java
import com.azure.core.util.Context;

/** Samples for Users Delete. */
public final class UsersDeleteSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/Users/deleteUser.json
     */
    /**
     * Sample code: deleteUser.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void deleteUser(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager.users().delete("testrg123", "testlab", "testuser", Context.NONE);
    }
}
```

### Users_Get

```java
import com.azure.core.util.Context;

/** Samples for Users Get. */
public final class UsersGetSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/Users/getUser.json
     */
    /**
     * Sample code: getUser.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void getUser(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager.users().getWithResponse("testrg123", "testlab", "testuser", Context.NONE);
    }
}
```

### Users_Invite

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.labservices.models.InviteBody;

/** Samples for Users Invite. */
public final class UsersInviteSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/Users/inviteUser.json
     */
    /**
     * Sample code: inviteUser.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void inviteUser(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager
            .users()
            .invite(
                "testrg123",
                "testlab",
                "testuser",
                new InviteBody().withText("Invitation to lab testlab"),
                Context.NONE);
    }
}
```

### Users_ListByLab

```java
import com.azure.core.util.Context;

/** Samples for Users ListByLab. */
public final class UsersListByLabSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/Users/listUser.json
     */
    /**
     * Sample code: listUser.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void listUser(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager.users().listByLab("testrg123", "testlab", null, Context.NONE);
    }
}
```

### Users_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.labservices.models.User;
import java.time.Duration;

/** Samples for Users Update. */
public final class UsersUpdateSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/Users/patchUser.json
     */
    /**
     * Sample code: patchUser.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void patchUser(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        User resource = manager.users().getWithResponse("testrg123", "testlab", "testuser", Context.NONE).getValue();
        resource.update().withAdditionalUsageQuota(Duration.parse("PT10H")).apply();
    }
}
```

### VirtualMachines_Get

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines Get. */
public final class VirtualMachinesGetSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/VirtualMachines/getVirtualMachine.json
     */
    /**
     * Sample code: getVirtualMachine.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void getVirtualMachine(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager.virtualMachines().getWithResponse("testrg123", "testlab", "template", Context.NONE);
    }
}
```

### VirtualMachines_ListByLab

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines ListByLab. */
public final class VirtualMachinesListByLabSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/VirtualMachines/listVirtualMachine.json
     */
    /**
     * Sample code: listVirtualMachine.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void listVirtualMachine(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager.virtualMachines().listByLab("testrg123", "testlab", null, Context.NONE);
    }
}
```

### VirtualMachines_Redeploy

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines Redeploy. */
public final class VirtualMachinesRedeploySamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/VirtualMachines/redeployVirtualMachine.json
     */
    /**
     * Sample code: redeployVirtualMachine.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void redeployVirtualMachine(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager.virtualMachines().redeploy("testrg123", "testlab", "template", Context.NONE);
    }
}
```

### VirtualMachines_Reimage

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines Reimage. */
public final class VirtualMachinesReimageSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/VirtualMachines/reimageVirtualMachine.json
     */
    /**
     * Sample code: reimageVirtualMachine.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void reimageVirtualMachine(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager.virtualMachines().reimage("testrg123", "testlab", "template", Context.NONE);
    }
}
```

### VirtualMachines_ResetPassword

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.labservices.models.ResetPasswordBody;

/** Samples for VirtualMachines ResetPassword. */
public final class VirtualMachinesResetPasswordSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/VirtualMachines/resetPasswordVirtualMachine.json
     */
    /**
     * Sample code: resetPasswordVirtualMachine.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void resetPasswordVirtualMachine(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager
            .virtualMachines()
            .resetPassword(
                "testrg123",
                "testlab",
                "template",
                new ResetPasswordBody().withUsername("example-username").withPassword("example-password"),
                Context.NONE);
    }
}
```

### VirtualMachines_Start

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines Start. */
public final class VirtualMachinesStartSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/VirtualMachines/startVirtualMachine.json
     */
    /**
     * Sample code: startVirtualMachine.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void startVirtualMachine(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager.virtualMachines().start("testrg123", "testlab", "template", Context.NONE);
    }
}
```

### VirtualMachines_Stop

```java
import com.azure.core.util.Context;

/** Samples for VirtualMachines Stop. */
public final class VirtualMachinesStopSamples {
    /*
     * x-ms-original-file: specification/labservices/resource-manager/Microsoft.LabServices/stable/2022-08-01/examples/VirtualMachines/stopVirtualMachine.json
     */
    /**
     * Sample code: stopVirtualMachine.
     *
     * @param manager Entry point to LabServicesManager.
     */
    public static void stopVirtualMachine(com.azure.resourcemanager.labservices.LabServicesManager manager) {
        manager.virtualMachines().stop("testrg123", "testlab", "template", Context.NONE);
    }
}
```

