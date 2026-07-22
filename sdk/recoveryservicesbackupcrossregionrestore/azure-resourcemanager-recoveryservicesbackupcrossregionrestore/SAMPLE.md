# Code snippets and samples


## AadPropertiesOperation

- [Get](#aadpropertiesoperation_get)

## BackupCrrJobDetails

- [Get](#backupcrrjobdetails_get)

## BackupCrrJobs

- [List](#backupcrrjobs_list)

## BackupProtectedItemsCrr

- [List](#backupprotecteditemscrr_list)

## BackupResourceStorageConfigs

- [Get](#backupresourcestorageconfigs_get)
- [Patch](#backupresourcestorageconfigs_patch)
- [Update](#backupresourcestorageconfigs_update)

## BackupUsageSummariesCRR

- [List](#backupusagesummariescrr_list)

## CrossRegionRestore

- [Trigger](#crossregionrestore_trigger)

## CrrOperationResults

- [Get](#crroperationresults_get)

## CrrOperationStatus

- [Get](#crroperationstatus_get)

## RecoveryPoints

- [GetAccessToken](#recoverypoints_getaccesstoken)

## RecoveryPointsCrr

- [Get](#recoverypointscrr_get)
- [List](#recoverypointscrr_list)
### AadPropertiesOperation_Get

```java
/**
 * Samples for AadPropertiesOperation Get.
 */
public final class AadPropeSamples {
    /*
     * x-ms-original-file: 2026-07-15/AzureIaasVm/BackupAadProperties_Get.json
     */
    /**
     * Sample code: Get AAD Properties for authentication in the third region.
     * 
     * @param manager Entry point to RecoveryServicesBackupCrrManager.
     */
    public static void getAADPropertiesForAuthenticationInTheThirdRegion(
        com.azure.resourcemanager.recoveryservicesbackupcrossregionrestore.RecoveryServicesBackupCrrManager manager) {
        manager.aadPropertiesOperations().getWithResponse("southeastasia", null, com.azure.core.util.Context.NONE);
    }
}
```

### BackupCrrJobDetails_Get

```java
import com.azure.resourcemanager.recoveryservicesbackupcrossregionrestore.models.CrrJobRequest;

/**
 * Samples for BackupCrrJobDetails Get.
 */
public final class BackupCrSamples {
    /*
     * x-ms-original-file: 2026-07-15/AzureIaasVm/GetCrrJob_Post.json
     */
    /**
     * Sample code: Get Cross Region Restore Job Details in the secondary region.
     * 
     * @param manager Entry point to RecoveryServicesBackupCrrManager.
     */
    public static void getCrossRegionRestoreJobDetailsInTheSecondaryRegion(
        com.azure.resourcemanager.recoveryservicesbackupcrossregionrestore.RecoveryServicesBackupCrrManager manager) {
        manager.backupCrrJobDetails()
            .getWithResponse("southeastasia", new CrrJobRequest().withResourceId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testRg/providers/Microsoft.Compute/VirtualMachines/testVm")
                .withJobName("02585cc9-d7f4-4b46-860c-14c048cce178"), com.azure.core.util.Context.NONE);
    }
}
```

### BackupCrrJobs_List

```java
/**
 * Samples for BackupProtectedItemsCrr List.
 */
public final class BackupPrSamples {
    /*
     * x-ms-original-file: 2026-07-15/AzureIaasVm/BackupProtectedItems_List.json
     */
    /**
     * Sample code: List protected items with backupManagementType filter as AzureIaasVm.
     * 
     * @param manager Entry point to RecoveryServicesBackupCrrManager.
     */
    public static void listProtectedItemsWithBackupManagementTypeFilterAsAzureIaasVm(
        com.azure.resourcemanager.recoveryservicesbackupcrossregionrestore.RecoveryServicesBackupCrrManager manager) {
        manager.backupProtectedItemsCrrs()
            .listWithResponse("SwaggerTestRg", "NetSDKTestRsVault",
                "backupManagementType eq 'AzureIaasVM' and itemType eq 'VM'", null, com.azure.core.util.Context.NONE);
    }
}
```

### BackupProtectedItemsCrr_List

```java
/**
 * Samples for BackupResourceStorageConfigs Get.
 */
public final class BackupReSamples {
    /*
     * x-ms-original-file: 2026-07-15/Common/BackupStorageConfig_Get.json
     */
    /**
     * Sample code: Get Vault Storage Configuration.
     * 
     * @param manager Entry point to RecoveryServicesBackupCrrManager.
     */
    public static void getVaultStorageConfiguration(
        com.azure.resourcemanager.recoveryservicesbackupcrossregionrestore.RecoveryServicesBackupCrrManager manager) {
        manager.backupResourceStorageConfigs()
            .getWithResponse("PythonSDKBackupTestRg", "PySDKBackupTestRsVault", com.azure.core.util.Context.NONE);
    }
}
```

### BackupResourceStorageConfigs_Get

```java
/**
 * Samples for BackupUsageSummariesCRR List.
 */
public final class BackupUsSamples {
    /*
     * x-ms-original-file: 2026-07-15/Common/BackupProtectedItem_UsageSummary_Get.json
     */
    /**
     * Sample code: Get Protected Items Usages Summary.
     * 
     * @param manager Entry point to RecoveryServicesBackupCrrManager.
     */
    public static void getProtectedItemsUsagesSummary(
        com.azure.resourcemanager.recoveryservicesbackupcrossregionrestore.RecoveryServicesBackupCrrManager manager) {
        manager.backupUsageSummariesCRRs()
            .listWithResponse("testRG", "testVault", "type eq 'BackupProtectedItemCountSummary'", null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-07-15/Common/BackupProtectionContainers_UsageSummary_Get.json
     */
    /**
     * Sample code: Get Protected Containers Usages Summary.
     * 
     * @param manager Entry point to RecoveryServicesBackupCrrManager.
     */
    public static void getProtectedContainersUsagesSummary(
        com.azure.resourcemanager.recoveryservicesbackupcrossregionrestore.RecoveryServicesBackupCrrManager manager) {
        manager.backupUsageSummariesCRRs()
            .listWithResponse("testRG", "testVault", "type eq 'BackupProtectionContainerCountSummary'", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### BackupResourceStorageConfigs_Patch

```java
import com.azure.resourcemanager.recoveryservicesbackupcrossregionrestore.models.CrossRegionRestoreRequest;
import com.azure.resourcemanager.recoveryservicesbackupcrossregionrestore.models.EncryptionDetails;
import com.azure.resourcemanager.recoveryservicesbackupcrossregionrestore.models.IaasVMRestoreRequest;
import com.azure.resourcemanager.recoveryservicesbackupcrossregionrestore.models.IdentityBasedRestoreDetails;
import com.azure.resourcemanager.recoveryservicesbackupcrossregionrestore.models.IdentityInfo;
import com.azure.resourcemanager.recoveryservicesbackupcrossregionrestore.models.RecoveryType;
import com.azure.resourcemanager.recoveryservicesbackupcrossregionrestore.models.WorkloadCrrAccessToken;
import java.util.Arrays;

/**
 * Samples for CrossRegionRestore Trigger.
 */
public final class CrossRegSamples {
    /*
     * x-ms-original-file: 2026-07-15/AzureIaasVm/TriggerCrossRegionRestore_Post.json
     */
    /**
     * Sample code: Trigger Cross Region Restore.
     * 
     * @param manager Entry point to RecoveryServicesBackupCrrManager.
     */
    public static void triggerCrossRegionRestore(
        com.azure.resourcemanager.recoveryservicesbackupcrossregionrestore.RecoveryServicesBackupCrrManager manager) {
        manager.crossRegionRestores()
            .triggerWithResponse("southeastasia", new CrossRegionRestoreRequest()
                .withCrossRegionRestoreAccessDetails(
                    new WorkloadCrrAccessToken().withAccessTokenString("fakeTokenPlaceholder")
                        .withSubscriptionId("f2edfd5d-5496-4683-b94f-b3588c579009")
                        .withResourceGroupName("srinivasccyrg")
                        .withResourceName("sriniccyvault")
                        .withResourceId("1330837906418138160")
                        .withRecoveryPointId("87178355392716")
                        .withRecoveryPointTime("10/9/2019 6:05:54 PM")
                        .withContainerName("iaasvmcontainerv2;srinivasccyrg;sriniccylinux")
                        .withContainerType("IaasVMContainer")
                        .withBackupManagementType("AzureIaasVM")
                        .withDatasourceType("VM")
                        .withDatasourceName("sriniccylinux")
                        .withDatasourceId("1142937031")
                        .withDatasourceContainerName("iaasvmcontainerv2;srinivasccyrg;sriniccylinux")
                        .withCoordinatorServiceStampUri("https://pod01-coord1.ccy.backup.windowsazure.com")
                        .withProtectionServiceStampId("90d98224-2ac6-4bda-9f35-33fb22841f2a")
                        .withProtectionServiceStampUri("https://pod01-prot1-int.ccy.backup.windowsazure.com")
                        .withTokenExtendedInformation("fakeTokenPlaceholder"))
                .withRestoreRequest(new IaasVMRestoreRequest().withRecoveryPointId("87178355392716")
                    .withRecoveryType(RecoveryType.ALTERNATE_LOCATION)
                    .withSourceResourceId(
                        "/subscriptions/f2edfd5d-5496-4683-b94f-b3588c579009/resourceGroups/srinivasccyrg/providers/Microsoft.Compute/virtualMachines/sriniccylinux")
                    .withTargetVirtualMachineId(
                        "/subscriptions/f2edfd5d-5496-4683-b94f-b3588c579009/resourceGroups/00networkAckl/providers/Microsoft.Compute/virtualMachines/gaallaVM")
                    .withTargetResourceGroupId(
                        "/subscriptions/f2edfd5d-5496-4683-b94f-b3588c579009/resourceGroups/00networkAckl")
                    .withStorageAccountId(
                        "/subscriptions/f2edfd5d-5496-4683-b94f-b3588c579009/resourceGroups/00prjaiTestRg1/providers/Microsoft.Storage/storageAccounts/00prjaitestrg1disks993")
                    .withVirtualNetworkId(
                        "/subscriptions/f2edfd5d-5496-4683-b94f-b3588c579009/resourceGroups/00networkAcklVaultCCY/providers/Microsoft.Network/virtualNetworks/00networkAcklVaultCCY-vnet")
                    .withSubnetId(
                        "/subscriptions/f2edfd5d-5496-4683-b94f-b3588c579009/resourceGroups/00networkAcklVaultCCY/providers/Microsoft.Network/virtualNetworks/00networkAcklVaultCCY-vnet/subnets/default")
                    .withRegion("eastus2euap")
                    .withAffinityGroup("")
                    .withCreateNewCloudService(false)
                    .withOriginalStorageAccountOption(false)
                    .withEncryptionDetails(new EncryptionDetails().withEncryptionEnabled(false))
                    .withZones(Arrays.asList("2"))
                    .withIdentityInfo(new IdentityInfo().withIsSystemAssignedIdentity(false)
                        .withManagedIdentityResourceId(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/asmaskarRG1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/asmaskartestmsi"))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-07-15/AzureIaasVm/TriggerCrossRegionRestore_IdentityBasedRestoreDetails_Post.json
     */
    /**
     * Sample code: Trigger Cross Region Restore with identityBasedRestoreDetails.
     * 
     * @param manager Entry point to RecoveryServicesBackupCrrManager.
     */
    public static void triggerCrossRegionRestoreWithIdentityBasedRestoreDetails(
        com.azure.resourcemanager.recoveryservicesbackupcrossregionrestore.RecoveryServicesBackupCrrManager manager) {
        manager.crossRegionRestores()
            .triggerWithResponse("southeastasia", new CrossRegionRestoreRequest()
                .withCrossRegionRestoreAccessDetails(
                    new WorkloadCrrAccessToken().withAccessTokenString("fakeTokenPlaceholder")
                        .withSubscriptionId("f2edfd5d-5496-4683-b94f-b3588c579009")
                        .withResourceGroupName("srinivasccyrg")
                        .withResourceName("sriniccyvault")
                        .withResourceId("1330837906418138160")
                        .withRecoveryPointId("87178355392716")
                        .withRecoveryPointTime("10/9/2019 6:05:54 PM")
                        .withContainerName("iaasvmcontainerv2;srinivasccyrg;sriniccylinux")
                        .withContainerType("IaasVMContainer")
                        .withBackupManagementType("AzureIaasVM")
                        .withDatasourceType("VM")
                        .withDatasourceName("sriniccylinux")
                        .withDatasourceId("1142937031")
                        .withDatasourceContainerName("iaasvmcontainerv2;srinivasccyrg;sriniccylinux")
                        .withCoordinatorServiceStampUri("https://pod01-coord1.ccy.backup.windowsazure.com")
                        .withProtectionServiceStampId("90d98224-2ac6-4bda-9f35-33fb22841f2a")
                        .withProtectionServiceStampUri("https://pod01-prot1-int.ccy.backup.windowsazure.com")
                        .withTokenExtendedInformation("fakeTokenPlaceholder"))
                .withRestoreRequest(new IaasVMRestoreRequest().withRecoveryPointId("87178355392716")
                    .withRecoveryType(RecoveryType.ALTERNATE_LOCATION)
                    .withSourceResourceId(
                        "/subscriptions/f2edfd5d-5496-4683-b94f-b3588c579009/resourceGroups/srinivasccyrg/providers/Microsoft.Compute/virtualMachines/sriniccylinux")
                    .withTargetVirtualMachineId(
                        "/subscriptions/f2edfd5d-5496-4683-b94f-b3588c579009/resourceGroups/00networkAckl/providers/Microsoft.Compute/virtualMachines/gaallaVM")
                    .withTargetResourceGroupId(
                        "/subscriptions/f2edfd5d-5496-4683-b94f-b3588c579009/resourceGroups/00networkAckl")
                    .withVirtualNetworkId(
                        "/subscriptions/f2edfd5d-5496-4683-b94f-b3588c579009/resourceGroups/00networkAcklVaultCCY/providers/Microsoft.Network/virtualNetworks/00networkAcklVaultCCY-vnet")
                    .withSubnetId(
                        "/subscriptions/f2edfd5d-5496-4683-b94f-b3588c579009/resourceGroups/00networkAcklVaultCCY/providers/Microsoft.Network/virtualNetworks/00networkAcklVaultCCY-vnet/subnets/default")
                    .withRegion("eastus2euap")
                    .withAffinityGroup("")
                    .withCreateNewCloudService(false)
                    .withOriginalStorageAccountOption(false)
                    .withEncryptionDetails(new EncryptionDetails().withEncryptionEnabled(false))
                    .withZones(Arrays.asList("2"))
                    .withIdentityInfo(new IdentityInfo().withIsSystemAssignedIdentity(false)
                        .withManagedIdentityResourceId(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/asmaskarRG1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/asmaskartestmsi"))
                    .withIdentityBasedRestoreDetails(new IdentityBasedRestoreDetails().withTargetStorageAccountId(
                        "/subscriptions/f2edfd5d-5496-4683-b94f-b3588c579009/resourceGroups/00prjaiTestRg1/providers/Microsoft.Storage/storageAccounts/00prjaitestrg1disks993"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### BackupResourceStorageConfigs_Update

```java
/**
 * Samples for CrrOperationResults Get.
 */
public final class CrrOperaSamples {
    /*
     * x-ms-original-file: 2026-07-15/AzureIaasVm/GetCrrOperationResults_Get.json
     */
    /**
     * Sample code: Get Operation Results for Cross Region Restore.
     * 
     * @param manager Entry point to RecoveryServicesBackupCrrManager.
     */
    public static void getOperationResultsForCrossRegionRestore(
        com.azure.resourcemanager.recoveryservicesbackupcrossregionrestore.RecoveryServicesBackupCrrManager manager) {
        manager.crrOperationResults()
            .getWithResponse("southeastasia", "00000000-0000-0000-0000-000000000000", com.azure.core.util.Context.NONE);
    }
}
```

### BackupUsageSummariesCRR_List

```java
import com.azure.resourcemanager.recoveryservicesbackupcrossregionrestore.fluent.models.AADPropertiesResourceInner;
import com.azure.resourcemanager.recoveryservicesbackupcrossregionrestore.models.AADProperties;

/**
 * Samples for RecoveryPoints GetAccessToken.
 */
public final class RecoverySamples {
    /*
     * x-ms-original-file: 2026-07-15/AzureIaasVm/GetAccessToken_Get.json
     */
    /**
     * Sample code: Get Access Token for Crr.
     * 
     * @param manager Entry point to RecoveryServicesBackupCrrManager.
     */
    public static void getAccessTokenForCrr(
        com.azure.resourcemanager.recoveryservicesbackupcrossregionrestore.RecoveryServicesBackupCrrManager manager) {
        manager.recoveryPoints()
            .getAccessTokenWithResponse("rshhtestmdvmrg", "rshvault", "Azure",
                "IaasVMContainer;iaasvmcontainerv2;rshhtestmdvmrg;rshmdvmsmall",
                "VM;iaasvmcontainerv2;rshhtestmdvmrg;rshmdvmsmall", "26083826328862",
                new AADPropertiesResourceInner()
                    .withProperties(new AADProperties().withTenantId("33e01921-4d64-4f8c-a055-5bdaffd5e33d")
                        .withAudience("https://RecoveryServices/IaasCoord/aadmgmt/ase")
                        .withServicePrincipalObjectId("5ecd8123-cf74-4037-83e9-9246b227b351")),
                com.azure.core.util.Context.NONE);
    }
}
```

