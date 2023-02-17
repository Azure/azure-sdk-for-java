# Code snippets and samples


## BackupEngines

- [Get](#backupengines_get)
- [List](#backupengines_list)

## BackupJobs

- [List](#backupjobs_list)

## BackupOperationResults

- [Get](#backupoperationresults_get)

## BackupOperationStatuses

- [Get](#backupoperationstatuses_get)

## BackupPolicies

- [List](#backuppolicies_list)

## BackupProtectableItems

- [List](#backupprotectableitems_list)

## BackupProtectedItems

- [List](#backupprotecteditems_list)

## BackupProtectionContainers

- [List](#backupprotectioncontainers_list)

## BackupProtectionIntent

- [List](#backupprotectionintent_list)

## BackupResourceEncryptionConfigs

- [Get](#backupresourceencryptionconfigs_get)
- [Update](#backupresourceencryptionconfigs_update)

## BackupResourceStorageConfigsNonCrr

- [Get](#backupresourcestorageconfigsnoncrr_get)
- [Patch](#backupresourcestorageconfigsnoncrr_patch)
- [Update](#backupresourcestorageconfigsnoncrr_update)

## BackupResourceVaultConfigs

- [Get](#backupresourcevaultconfigs_get)
- [Put](#backupresourcevaultconfigs_put)
- [Update](#backupresourcevaultconfigs_update)

## BackupStatus

- [Get](#backupstatus_get)

## BackupUsageSummaries

- [List](#backupusagesummaries_list)

## BackupWorkloadItems

- [List](#backupworkloaditems_list)

## Backups

- [Trigger](#backups_trigger)

## BmsPrepareDataMoveOperationResult

- [Get](#bmspreparedatamoveoperationresult_get)

## DeletedProtectionContainers

- [List](#deletedprotectioncontainers_list)

## ExportJobsOperationResults

- [Get](#exportjobsoperationresults_get)

## FeatureSupport

- [Validate](#featuresupport_validate)

## ItemLevelRecoveryConnections

- [Provision](#itemlevelrecoveryconnections_provision)
- [Revoke](#itemlevelrecoveryconnections_revoke)

## JobCancellations

- [Trigger](#jobcancellations_trigger)

## JobDetails

- [Get](#jobdetails_get)

## JobOperationResults

- [Get](#joboperationresults_get)

## Jobs

- [Export](#jobs_export)

## OperationOperation

- [Validate](#operationoperation_validate)

## Operations

- [List](#operations_list)

## PrivateEndpoint

- [GetOperationStatus](#privateendpoint_getoperationstatus)

## PrivateEndpointConnection

- [Delete](#privateendpointconnection_delete)
- [Get](#privateendpointconnection_get)
- [Put](#privateendpointconnection_put)

## ProtectableContainers

- [List](#protectablecontainers_list)

## ProtectedItemOperationResults

- [Get](#protecteditemoperationresults_get)

## ProtectedItemOperationStatuses

- [Get](#protecteditemoperationstatuses_get)

## ProtectedItems

- [CreateOrUpdate](#protecteditems_createorupdate)
- [Delete](#protecteditems_delete)
- [Get](#protecteditems_get)

## ProtectionContainerOperationResults

- [Get](#protectioncontaineroperationresults_get)

## ProtectionContainerRefreshOperationResults

- [Get](#protectioncontainerrefreshoperationresults_get)

## ProtectionContainers

- [Get](#protectioncontainers_get)
- [Inquire](#protectioncontainers_inquire)
- [Refresh](#protectioncontainers_refresh)
- [Register](#protectioncontainers_register)
- [Unregister](#protectioncontainers_unregister)

## ProtectionIntent

- [CreateOrUpdate](#protectionintent_createorupdate)
- [Delete](#protectionintent_delete)
- [Get](#protectionintent_get)
- [Validate](#protectionintent_validate)

## ProtectionPolicies

- [CreateOrUpdate](#protectionpolicies_createorupdate)
- [Delete](#protectionpolicies_delete)
- [Get](#protectionpolicies_get)

## ProtectionPolicyOperationResults

- [Get](#protectionpolicyoperationresults_get)

## ProtectionPolicyOperationStatuses

- [Get](#protectionpolicyoperationstatuses_get)

## RecoveryPoints

- [Get](#recoverypoints_get)
- [List](#recoverypoints_list)

## RecoveryPointsRecommendedForMove

- [List](#recoverypointsrecommendedformove_list)

## ResourceGuardProxies

- [Get](#resourceguardproxies_get)

## ResourceGuardProxyOperation

- [Delete](#resourceguardproxyoperation_delete)
- [Get](#resourceguardproxyoperation_get)
- [Put](#resourceguardproxyoperation_put)
- [UnlockDelete](#resourceguardproxyoperation_unlockdelete)

## ResourceProvider

- [BmsPrepareDataMove](#resourceprovider_bmspreparedatamove)
- [BmsTriggerDataMove](#resourceprovider_bmstriggerdatamove)
- [GetOperationStatus](#resourceprovider_getoperationstatus)
- [MoveRecoveryPoint](#resourceprovider_moverecoverypoint)

## Restores

- [Trigger](#restores_trigger)

## SecurityPINs

- [Get](#securitypins_get)

## ValidateOperation

- [Trigger](#validateoperation_trigger)

## ValidateOperationResults

- [Get](#validateoperationresults_get)

## ValidateOperationStatuses

- [Get](#validateoperationstatuses_get)
### BackupEngines_Get

```java
/** Samples for BackupEngines Get. */
public final class BackupEnginesGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/Dpm/BackupEngines_Get.json
     */
    /**
     * Sample code: Get Dpm/AzureBackupServer/Lajolla Backup Engine Details.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getDpmAzureBackupServerLajollaBackupEngineDetails(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .backupEngines()
            .getWithResponse("testVault", "testRG", "testServer", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### BackupEngines_List

```java
/** Samples for BackupEngines List. */
public final class BackupEnginesListSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/Dpm/BackupEngines_List.json
     */
    /**
     * Sample code: List Dpm/AzureBackupServer/Lajolla Backup Engines.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void listDpmAzureBackupServerLajollaBackupEngines(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager.backupEngines().list("testVault", "testRG", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### BackupJobs_List

```java
/** Samples for BackupJobs List. */
public final class BackupJobsListSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/Common/ListJobs.json
     */
    /**
     * Sample code: List All Jobs.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void listAllJobs(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager.backupJobs().list("NetSDKTestRsVault", "SwaggerTestRg", null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/Common/ListJobsWithAllSupportedFilters.json
     */
    /**
     * Sample code: List Jobs With Filters.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void listJobsWithFilters(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .backupJobs()
            .list(
                "NetSDKTestRsVault",
                "SwaggerTestRg",
                "startTime eq '2016-01-01 00:00:00 AM' and endTime eq '2017-11-29 00:00:00 AM' and operation eq"
                    + " 'Backup' and backupManagementType eq 'AzureIaasVM' and status eq 'InProgress'",
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/Common/ListJobsWithStartTimeAndEndTimeFilters.json
     */
    /**
     * Sample code: List Jobs With Time Filter.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void listJobsWithTimeFilter(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .backupJobs()
            .list(
                "NetSDKTestRsVault",
                "SwaggerTestRg",
                "startTime eq '2016-01-01 00:00:00 AM' and endTime eq '2017-11-29 00:00:00 AM'",
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### BackupOperationResults_Get

```java
/** Samples for BackupOperationResults Get. */
public final class BackupOperationResultsGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/Common/ProtectedItem_Delete_OperationResult.json
     */
    /**
     * Sample code: Get Result for Protected Item Delete Operation.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getResultForProtectedItemDeleteOperation(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .backupOperationResults()
            .getWithResponse(
                "PySDKBackupTestRsVault",
                "PythonSDKBackupTestRg",
                "00000000-0000-0000-0000-000000000000",
                com.azure.core.util.Context.NONE);
    }
}
```

### BackupOperationStatuses_Get

```java
/** Samples for BackupOperationStatuses Get. */
public final class BackupOperationStatusesGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/Common/ProtectedItem_Delete_OperationStatus.json
     */
    /**
     * Sample code: Get Protected Item Delete Operation Status.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getProtectedItemDeleteOperationStatus(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .backupOperationStatuses()
            .getWithResponse(
                "PySDKBackupTestRsVault",
                "PythonSDKBackupTestRg",
                "00000000-0000-0000-0000-000000000000",
                com.azure.core.util.Context.NONE);
    }
}
```

### BackupPolicies_List

```java
/** Samples for BackupPolicies List. */
public final class BackupPoliciesListSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureWorkload/BackupPolicies_List.json
     */
    /**
     * Sample code: List protection policies with backupManagementType filter as AzureWorkload.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void listProtectionPoliciesWithBackupManagementTypeFilterAsAzureWorkload(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .backupPolicies()
            .list(
                "NetSDKTestRsVault",
                "SwaggerTestRg",
                "backupManagementType eq 'AzureWorkload'",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/V2Policy/v2-List-Policies.json
     */
    /**
     * Sample code: List protection policies with backupManagementType filter as AzureIaasVm with both V1 and V2
     * policies.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void listProtectionPoliciesWithBackupManagementTypeFilterAsAzureIaasVmWithBothV1AndV2Policies(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .backupPolicies()
            .list(
                "NetSDKTestRsVault",
                "SwaggerTestRg",
                "backupManagementType eq 'AzureIaasVM'",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/BackupPolicies_List.json
     */
    /**
     * Sample code: List protection policies with backupManagementType filter as AzureIaasVm.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void listProtectionPoliciesWithBackupManagementTypeFilterAsAzureIaasVm(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .backupPolicies()
            .list(
                "NetSDKTestRsVault",
                "SwaggerTestRg",
                "backupManagementType eq 'AzureIaasVM'",
                com.azure.core.util.Context.NONE);
    }
}
```

### BackupProtectableItems_List

```java
/** Samples for BackupProtectableItems List. */
public final class BackupProtectableItemsListSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/BackupProtectableItems_List.json
     */
    /**
     * Sample code: List protectable items with backupManagementType filter as AzureIaasVm.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void listProtectableItemsWithBackupManagementTypeFilterAsAzureIaasVm(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .backupProtectableItems()
            .list(
                "NetSDKTestRsVault",
                "SwaggerTestRg",
                "backupManagementType eq 'AzureIaasVM'",
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### BackupProtectedItems_List

```java
/** Samples for BackupProtectedItems List. */
public final class BackupProtectedItemsListSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/BackupProtectedItems_List.json
     */
    /**
     * Sample code: List protected items with backupManagementType filter as AzureIaasVm.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void listProtectedItemsWithBackupManagementTypeFilterAsAzureIaasVm(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .backupProtectedItems()
            .list(
                "NetSDKTestRsVault",
                "SwaggerTestRg",
                "backupManagementType eq 'AzureIaasVM' and itemType eq 'VM'",
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### BackupProtectionContainers_List

```java
/** Samples for BackupProtectionContainers List. */
public final class BackupProtectionContainersListSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureStorage/ProtectionContainers_List.json
     */
    /**
     * Sample code: List Backup Protection Containers.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void listBackupProtectionContainers(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .backupProtectionContainers()
            .list("testVault", "testRg", "backupManagementType eq 'AzureWorkload'", com.azure.core.util.Context.NONE);
    }
}
```

### BackupProtectionIntent_List

```java
/** Samples for BackupProtectionIntent List. */
public final class BackupProtectionIntentListSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureWorkload/BackupProtectionIntent_List.json
     */
    /**
     * Sample code: List protection intent with backupManagementType filter.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void listProtectionIntentWithBackupManagementTypeFilter(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager.backupProtectionIntents().list("myVault", "myRG", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### BackupResourceEncryptionConfigs_Get

```java
/** Samples for BackupResourceEncryptionConfigs Get. */
public final class BackupResourceEncryptionConfigsGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/BackupResourceEncryptionConfig_Get.json
     */
    /**
     * Sample code: Get Vault Encryption Configuration.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getVaultEncryptionConfiguration(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .backupResourceEncryptionConfigs()
            .getWithResponse("rishTestVault", "rishgrp", com.azure.core.util.Context.NONE);
    }
}
```

### BackupResourceEncryptionConfigs_Update

```java
import com.azure.resourcemanager.recoveryservicesbackup.models.BackupResourceEncryptionConfig;
import com.azure.resourcemanager.recoveryservicesbackup.models.BackupResourceEncryptionConfigResource;
import com.azure.resourcemanager.recoveryservicesbackup.models.EncryptionAtRestType;
import com.azure.resourcemanager.recoveryservicesbackup.models.InfrastructureEncryptionState;

/** Samples for BackupResourceEncryptionConfigs Update. */
public final class BackupResourceEncryptionConfigsUpdateSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/BackupResourceEncryptionConfig_Put.json
     */
    /**
     * Sample code: Update Vault Encryption Configuration.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void updateVaultEncryptionConfiguration(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .backupResourceEncryptionConfigs()
            .updateWithResponse(
                "source-rsv",
                "test-rg",
                new BackupResourceEncryptionConfigResource()
                    .withProperties(
                        new BackupResourceEncryptionConfig()
                            .withEncryptionAtRestType(EncryptionAtRestType.CUSTOMER_MANAGED)
                            .withKeyUri("fakeTokenPlaceholder")
                            .withSubscriptionId("1a2311d9-66f5-47d3-a9fb-7a37da63934b")
                            .withInfrastructureEncryptionState(InfrastructureEncryptionState.fromString("true"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### BackupResourceStorageConfigsNonCrr_Get

```java
/** Samples for BackupResourceStorageConfigsNonCrr Get. */
public final class BackupResourceStorageConfigsNonCrrGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/Common/BackupStorageConfig_Get.json
     */
    /**
     * Sample code: Get Vault Storage Configuration.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getVaultStorageConfiguration(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .backupResourceStorageConfigsNonCrrs()
            .getWithResponse("PySDKBackupTestRsVault", "PythonSDKBackupTestRg", com.azure.core.util.Context.NONE);
    }
}
```

### BackupResourceStorageConfigsNonCrr_Patch

```java
import com.azure.resourcemanager.recoveryservicesbackup.fluent.models.BackupResourceConfigResourceInner;
import com.azure.resourcemanager.recoveryservicesbackup.models.BackupResourceConfig;
import com.azure.resourcemanager.recoveryservicesbackup.models.StorageType;
import com.azure.resourcemanager.recoveryservicesbackup.models.StorageTypeState;

/** Samples for BackupResourceStorageConfigsNonCrr Patch. */
public final class BackupResourceStorageConfigsNonCrrPatchSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/Common/BackupStorageConfig_Patch.json
     */
    /**
     * Sample code: Update Vault Storage Configuration.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void updateVaultStorageConfiguration(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .backupResourceStorageConfigsNonCrrs()
            .patchWithResponse(
                "PySDKBackupTestRsVault",
                "PythonSDKBackupTestRg",
                new BackupResourceConfigResourceInner()
                    .withProperties(
                        new BackupResourceConfig()
                            .withStorageType(StorageType.LOCALLY_REDUNDANT)
                            .withStorageTypeState(StorageTypeState.UNLOCKED)),
                com.azure.core.util.Context.NONE);
    }
}
```

### BackupResourceStorageConfigsNonCrr_Update

```java
import com.azure.resourcemanager.recoveryservicesbackup.fluent.models.BackupResourceConfigResourceInner;
import com.azure.resourcemanager.recoveryservicesbackup.models.BackupResourceConfig;
import com.azure.resourcemanager.recoveryservicesbackup.models.StorageType;
import com.azure.resourcemanager.recoveryservicesbackup.models.StorageTypeState;

/** Samples for BackupResourceStorageConfigsNonCrr Update. */
public final class BackupResourceStorageConfigsNonCrrUpdateSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/Common/BackupStorageConfig_Put.json
     */
    /**
     * Sample code: Update Vault Storage Configuration.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void updateVaultStorageConfiguration(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .backupResourceStorageConfigsNonCrrs()
            .updateWithResponse(
                "PySDKBackupTestRsVault",
                "PythonSDKBackupTestRg",
                new BackupResourceConfigResourceInner()
                    .withProperties(
                        new BackupResourceConfig()
                            .withStorageType(StorageType.LOCALLY_REDUNDANT)
                            .withStorageTypeState(StorageTypeState.UNLOCKED)),
                com.azure.core.util.Context.NONE);
    }
}
```

### BackupResourceVaultConfigs_Get

```java
/** Samples for BackupResourceVaultConfigs Get. */
public final class BackupResourceVaultConfigsGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/Common/BackupResourceVaultConfigs_Get.json
     */
    /**
     * Sample code: Get Vault Security Config.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getVaultSecurityConfig(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .backupResourceVaultConfigs()
            .getWithResponse("SwaggerTest", "SwaggerTestRg", com.azure.core.util.Context.NONE);
    }
}
```

### BackupResourceVaultConfigs_Put

```java
import com.azure.resourcemanager.recoveryservicesbackup.fluent.models.BackupResourceVaultConfigResourceInner;
import com.azure.resourcemanager.recoveryservicesbackup.models.BackupResourceVaultConfig;
import com.azure.resourcemanager.recoveryservicesbackup.models.EnhancedSecurityState;
import com.azure.resourcemanager.recoveryservicesbackup.models.SoftDeleteFeatureState;

/** Samples for BackupResourceVaultConfigs Put. */
public final class BackupResourceVaultConfigsPutSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/Common/BackupResourceVaultConfigs_Put.json
     */
    /**
     * Sample code: Update Vault Security Config.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void updateVaultSecurityConfig(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .backupResourceVaultConfigs()
            .putWithResponse(
                "SwaggerTest",
                "SwaggerTestRg",
                new BackupResourceVaultConfigResourceInner()
                    .withProperties(
                        new BackupResourceVaultConfig()
                            .withEnhancedSecurityState(EnhancedSecurityState.ENABLED)
                            .withSoftDeleteFeatureState(SoftDeleteFeatureState.ENABLED)),
                com.azure.core.util.Context.NONE);
    }
}
```

### BackupResourceVaultConfigs_Update

```java
import com.azure.resourcemanager.recoveryservicesbackup.fluent.models.BackupResourceVaultConfigResourceInner;
import com.azure.resourcemanager.recoveryservicesbackup.models.BackupResourceVaultConfig;
import com.azure.resourcemanager.recoveryservicesbackup.models.EnhancedSecurityState;

/** Samples for BackupResourceVaultConfigs Update. */
public final class BackupResourceVaultConfigsUpdateSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/Common/BackupResourceVaultConfigs_Patch.json
     */
    /**
     * Sample code: Update Vault Security Config.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void updateVaultSecurityConfig(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .backupResourceVaultConfigs()
            .updateWithResponse(
                "SwaggerTest",
                "SwaggerTestRg",
                new BackupResourceVaultConfigResourceInner()
                    .withProperties(
                        new BackupResourceVaultConfig().withEnhancedSecurityState(EnhancedSecurityState.ENABLED)),
                com.azure.core.util.Context.NONE);
    }
}
```

### BackupStatus_Get

```java
import com.azure.resourcemanager.recoveryservicesbackup.models.BackupStatusRequest;
import com.azure.resourcemanager.recoveryservicesbackup.models.DataSourceType;

/** Samples for BackupStatus Get. */
public final class BackupStatusGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/GetBackupStatus.json
     */
    /**
     * Sample code: Get Azure Virtual Machine Backup Status.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getAzureVirtualMachineBackupStatus(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .backupStatus()
            .getWithResponse(
                "southeastasia",
                new BackupStatusRequest()
                    .withResourceType(DataSourceType.VM)
                    .withResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testRg/providers/Microsoft.Compute/VirtualMachines/testVm"),
                com.azure.core.util.Context.NONE);
    }
}
```

### BackupUsageSummaries_List

```java
/** Samples for BackupUsageSummaries List. */
public final class BackupUsageSummariesListSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/Common/BackupProtectedItem_UsageSummary_Get.json
     */
    /**
     * Sample code: Get Protected Items Usages Summary.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getProtectedItemsUsagesSummary(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .backupUsageSummaries()
            .list(
                "testVault",
                "testRG",
                "type eq 'BackupProtectedItemCountSummary'",
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/Common/BackupProtectionContainers_UsageSummary_Get.json
     */
    /**
     * Sample code: Get Protected Containers Usages Summary.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getProtectedContainersUsagesSummary(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .backupUsageSummaries()
            .list(
                "testVault",
                "testRG",
                "type eq 'BackupProtectionContainerCountSummary'",
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### BackupWorkloadItems_List

```java
/** Samples for BackupWorkloadItems List. */
public final class BackupWorkloadItemsListSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureWorkload/BackupWorkloadItems_List.json
     */
    /**
     * Sample code: List Workload Items in Container.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void listWorkloadItemsInContainer(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .backupWorkloadItems()
            .list(
                "suchandr-seacan-rsv",
                "testRg",
                "Azure",
                "VMAppContainer;Compute;bvtdtestag;sqlserver-1",
                "backupManagementType eq 'AzureWorkload'",
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Backups_Trigger

```java
import com.azure.resourcemanager.recoveryservicesbackup.models.BackupRequestResource;
import com.azure.resourcemanager.recoveryservicesbackup.models.IaasVMBackupRequest;

/** Samples for Backups Trigger. */
public final class BackupsTriggerSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/Common/TriggerBackup_Post.json
     */
    /**
     * Sample code: Trigger Backup.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void triggerBackup(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .backups()
            .triggerWithResponse(
                "linuxRsVault",
                "linuxRsVaultRG",
                "Azure",
                "IaasVMContainer;iaasvmcontainerv2;testrg;v1win2012r",
                "VM;iaasvmcontainerv2;testrg;v1win2012r",
                new BackupRequestResource().withProperties(new IaasVMBackupRequest()),
                com.azure.core.util.Context.NONE);
    }
}
```

### BmsPrepareDataMoveOperationResult_Get

```java
/** Samples for BmsPrepareDataMoveOperationResult Get. */
public final class BmsPrepareDataMoveOperationResultGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/BackupDataMove/PrepareDataMoveOperationResult_Get.json
     */
    /**
     * Sample code: Get operation result for PrepareDataMove.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getOperationResultForPrepareDataMove(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .bmsPrepareDataMoveOperationResults()
            .getWithResponse(
                "source-rsv", "sourceRG", "00000000-0000-0000-0000-000000000000", com.azure.core.util.Context.NONE);
    }
}
```

### DeletedProtectionContainers_List

```java
/** Samples for DeletedProtectionContainers List. */
public final class DeletedProtectionContainersListSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureStorage/SoftDeletedContainers_List.json
     */
    /**
     * Sample code: List Backup Protection Containers.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void listBackupProtectionContainers(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .deletedProtectionContainers()
            .list("testRg", "testVault", "backupManagementType eq 'AzureWorkload'", com.azure.core.util.Context.NONE);
    }
}
```

### ExportJobsOperationResults_Get

```java
/** Samples for ExportJobsOperationResults Get. */
public final class ExportJobsOperationResultsGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/Common/ExportJobsOperationResult.json
     */
    /**
     * Sample code: Export Jobs Operation Results.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void exportJobsOperationResults(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .exportJobsOperationResults()
            .getWithResponse(
                "NetSDKTestRsVault",
                "SwaggerTestRg",
                "00000000-0000-0000-0000-000000000000",
                com.azure.core.util.Context.NONE);
    }
}
```

### FeatureSupport_Validate

```java
import com.azure.resourcemanager.recoveryservicesbackup.models.AzureVMResourceFeatureSupportRequest;

/** Samples for FeatureSupport Validate. */
public final class FeatureSupportValidateSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/BackupFeature_Validate.json
     */
    /**
     * Sample code: Check Azure Vm Backup Feature Support.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void checkAzureVmBackupFeatureSupport(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .featureSupports()
            .validateWithResponse(
                "southeastasia",
                new AzureVMResourceFeatureSupportRequest().withVmSize("Basic_A0").withVmSku("Premium"),
                com.azure.core.util.Context.NONE);
    }
}
```

### ItemLevelRecoveryConnections_Provision

```java
import com.azure.resourcemanager.recoveryservicesbackup.models.IaasVmilrRegistrationRequest;
import com.azure.resourcemanager.recoveryservicesbackup.models.IlrRequestResource;

/** Samples for ItemLevelRecoveryConnections Provision. */
public final class ItemLevelRecoveryConnectionsProvisionSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/Provision_Ilr.json
     */
    /**
     * Sample code: Provision Instant Item Level Recovery for Azure Vm.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void provisionInstantItemLevelRecoveryForAzureVm(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .itemLevelRecoveryConnections()
            .provisionWithResponse(
                "PySDKBackupTestRsVault",
                "PythonSDKBackupTestRg",
                "Azure",
                "iaasvmcontainer;iaasvmcontainerv2;pysdktestrg;pysdktestv2vm1",
                "vm;iaasvmcontainerv2;pysdktestrg;pysdktestv2vm1",
                "1",
                new IlrRequestResource()
                    .withProperties(
                        new IaasVmilrRegistrationRequest()
                            .withRecoveryPointId("38823086363464")
                            .withVirtualMachineId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/pysdktestrg/providers/Microsoft.Compute/virtualMachines/pysdktestv2vm1")
                            .withInitiatorName("Hello World")
                            .withRenewExistingRegistration(true)),
                com.azure.core.util.Context.NONE);
    }
}
```

### ItemLevelRecoveryConnections_Revoke

```java
/** Samples for ItemLevelRecoveryConnections Revoke. */
public final class ItemLevelRecoveryConnectionsRevokeSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/Revoke_Ilr.json
     */
    /**
     * Sample code: Revoke Instant Item Level Recovery for Azure Vm.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void revokeInstantItemLevelRecoveryForAzureVm(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .itemLevelRecoveryConnections()
            .revokeWithResponse(
                "PySDKBackupTestRsVault",
                "PythonSDKBackupTestRg",
                "Azure",
                "iaasvmcontainer;iaasvmcontainerv2;pysdktestrg;pysdktestv2vm1",
                "vm;iaasvmcontainerv2;pysdktestrg;pysdktestv2vm1",
                "1",
                com.azure.core.util.Context.NONE);
    }
}
```

### JobCancellations_Trigger

```java
/** Samples for JobCancellations Trigger. */
public final class JobCancellationsTriggerSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/Common/TriggerCancelJob.json
     */
    /**
     * Sample code: Cancel Job.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void cancelJob(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .jobCancellations()
            .triggerWithResponse(
                "NetSDKTestRsVault",
                "SwaggerTestRg",
                "00000000-0000-0000-0000-000000000000",
                com.azure.core.util.Context.NONE);
    }
}
```

### JobDetails_Get

```java
/** Samples for JobDetails Get. */
public final class JobDetailsGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/Common/GetJobDetails.json
     */
    /**
     * Sample code: Get Job Details.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getJobDetails(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .jobDetails()
            .getWithResponse(
                "NetSDKTestRsVault",
                "SwaggerTestRg",
                "00000000-0000-0000-0000-000000000000",
                com.azure.core.util.Context.NONE);
    }
}
```

### JobOperationResults_Get

```java
/** Samples for JobOperationResults Get. */
public final class JobOperationResultsGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/Common/CancelJobOperationResult.json
     */
    /**
     * Sample code: Cancel Job Operation Result.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void cancelJobOperationResult(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .jobOperationResults()
            .getWithResponse(
                "NetSDKTestRsVault",
                "SwaggerTestRg",
                "00000000-0000-0000-0000-000000000000",
                "00000000-0000-0000-0000-000000000000",
                com.azure.core.util.Context.NONE);
    }
}
```

### Jobs_Export

```java
/** Samples for Jobs Export. */
public final class JobsExportSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/Common/TriggerExportJobs.json
     */
    /**
     * Sample code: Export Jobs.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void exportJobs(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager.jobs().exportWithResponse("NetSDKTestRsVault", "SwaggerTestRg", null, com.azure.core.util.Context.NONE);
    }
}
```

### OperationOperation_Validate

```java
import com.azure.resourcemanager.recoveryservicesbackup.models.EncryptionDetails;
import com.azure.resourcemanager.recoveryservicesbackup.models.IaasVMRestoreRequest;
import com.azure.resourcemanager.recoveryservicesbackup.models.IdentityBasedRestoreDetails;
import com.azure.resourcemanager.recoveryservicesbackup.models.IdentityInfo;
import com.azure.resourcemanager.recoveryservicesbackup.models.RecoveryType;
import com.azure.resourcemanager.recoveryservicesbackup.models.ValidateIaasVMRestoreOperationRequest;

/** Samples for OperationOperation Validate. */
public final class OperationOperationValidateSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/ValidateOperation_RestoreDisk.json
     */
    /**
     * Sample code: Validate Operation.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void validateOperation(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .operationOperations()
            .validateWithResponse(
                "testVault",
                "testRG",
                new ValidateIaasVMRestoreOperationRequest()
                    .withRestoreRequest(
                        new IaasVMRestoreRequest()
                            .withRecoveryPointId("348916168024334")
                            .withRecoveryType(RecoveryType.RESTORE_DISKS)
                            .withSourceResourceId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/netsdktestrg/providers/Microsoft.Compute/virtualMachines/netvmtestv2vm1")
                            .withStorageAccountId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testingRg/providers/Microsoft.Storage/storageAccounts/testAccount")
                            .withRegion("southeastasia")
                            .withCreateNewCloudService(true)
                            .withOriginalStorageAccountOption(false)
                            .withEncryptionDetails(new EncryptionDetails().withEncryptionEnabled(false))
                            .withIdentityInfo(
                                new IdentityInfo()
                                    .withIsSystemAssignedIdentity(false)
                                    .withManagedIdentityResourceId(
                                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/asmaskarRG1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/asmaskartestmsi"))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/ValidateOperation_RestoreDisk_IdentityBasedRestoreDetails.json
     */
    /**
     * Sample code: Validate Operation with identityBasedRestoreDetails.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void validateOperationWithIdentityBasedRestoreDetails(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .operationOperations()
            .validateWithResponse(
                "testVault",
                "testRG",
                new ValidateIaasVMRestoreOperationRequest()
                    .withRestoreRequest(
                        new IaasVMRestoreRequest()
                            .withRecoveryPointId("348916168024334")
                            .withRecoveryType(RecoveryType.RESTORE_DISKS)
                            .withSourceResourceId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/netsdktestrg/providers/Microsoft.Compute/virtualMachines/netvmtestv2vm1")
                            .withRegion("southeastasia")
                            .withCreateNewCloudService(true)
                            .withOriginalStorageAccountOption(false)
                            .withEncryptionDetails(new EncryptionDetails().withEncryptionEnabled(false))
                            .withIdentityInfo(
                                new IdentityInfo()
                                    .withIsSystemAssignedIdentity(false)
                                    .withManagedIdentityResourceId(
                                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/asmaskarRG1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/asmaskartestmsi"))
                            .withIdentityBasedRestoreDetails(
                                new IdentityBasedRestoreDetails()
                                    .withTargetStorageAccountId(
                                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testingRg/providers/Microsoft.Storage/storageAccounts/testAccount"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/ListOperations.json
     */
    /**
     * Sample code: ListOperations.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void listOperations(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpoint_GetOperationStatus

```java
/** Samples for PrivateEndpoint GetOperationStatus. */
public final class PrivateEndpointGetOperationStatusSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/PrivateEndpointConnection/GetPrivateEndpointConnectionOperationStatus.json
     */
    /**
     * Sample code: Get OperationStatus.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getOperationStatus(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .privateEndpoints()
            .getOperationStatusWithResponse(
                "gaallavaultbvtd2msi",
                "gaallaRG",
                "gaallatestpe2.5704c932-249a-490b-a142-1396838cd3b",
                "0f48183b-0a44-4dca-aec1-bba5daab888a",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnection_Delete

```java
/** Samples for PrivateEndpointConnection Delete. */
public final class PrivateEndpointConnectionDeleteSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/PrivateEndpointConnection/DeletePrivateEndpointConnection.json
     */
    /**
     * Sample code: Delete PrivateEndpointConnection.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void deletePrivateEndpointConnection(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .privateEndpointConnections()
            .delete(
                "gaallavaultbvtd2msi",
                "gaallaRG",
                "gaallatestpe2.5704c932-249a-490b-a142-1396838cd3b",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnection_Get

```java
/** Samples for PrivateEndpointConnection Get. */
public final class PrivateEndpointConnectionGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/PrivateEndpointConnection/GetPrivateEndpointConnection.json
     */
    /**
     * Sample code: Get PrivateEndpointConnection.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getPrivateEndpointConnection(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse(
                "gaallavaultbvtd2msi",
                "gaallaRG",
                "gaallatestpe2.5704c932-249a-490b-a142-1396838cd3b",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnection_Put

```java
import com.azure.resourcemanager.recoveryservicesbackup.models.PrivateEndpoint;
import com.azure.resourcemanager.recoveryservicesbackup.models.PrivateEndpointConnection;
import com.azure.resourcemanager.recoveryservicesbackup.models.PrivateEndpointConnectionResource;
import com.azure.resourcemanager.recoveryservicesbackup.models.PrivateEndpointConnectionStatus;
import com.azure.resourcemanager.recoveryservicesbackup.models.PrivateLinkServiceConnectionState;
import com.azure.resourcemanager.recoveryservicesbackup.models.ProvisioningState;

/** Samples for PrivateEndpointConnection Put. */
public final class PrivateEndpointConnectionPutSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/PrivateEndpointConnection/PutPrivateEndpointConnection.json
     */
    /**
     * Sample code: Update PrivateEndpointConnection.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void updatePrivateEndpointConnection(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        PrivateEndpointConnectionResource resource =
            manager
                .privateEndpointConnections()
                .getWithResponse(
                    "gaallavaultbvtd2msi",
                    "gaallaRG",
                    "gaallatestpe2.5704c932-249a-490b-a142-1396838cd3b",
                    com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withProperties(
                new PrivateEndpointConnection()
                    .withProvisioningState(ProvisioningState.SUCCEEDED)
                    .withPrivateEndpoint(
                        new PrivateEndpoint()
                            .withId(
                                "/subscriptions/04cf684a-d41f-4550-9f70-7708a3a2283b/resourceGroups/gaallaRG/providers/Microsoft.Network/privateEndpoints/gaallatestpe3"))
                    .withPrivateLinkServiceConnectionState(
                        new PrivateLinkServiceConnectionState()
                            .withStatus(PrivateEndpointConnectionStatus.APPROVED)
                            .withDescription("Approved by johndoe@company.com")))
            .apply();
    }
}
```

### ProtectableContainers_List

```java
/** Samples for ProtectableContainers List. */
public final class ProtectableContainersListSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureStorage/ProtectableContainers_List.json
     */
    /**
     * Sample code: List protectable items with backupManagementType filter as AzureStorage.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void listProtectableItemsWithBackupManagementTypeFilterAsAzureStorage(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectableContainers()
            .list(
                "testvault",
                "testRg",
                "Azure",
                "backupManagementType eq 'AzureStorage' and workloadType eq 'AzureFileShare'",
                com.azure.core.util.Context.NONE);
    }
}
```

### ProtectedItemOperationResults_Get

```java
/** Samples for ProtectedItemOperationResults Get. */
public final class ProtectedItemOperationResultsGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/ProtectedItemOperationResults.json
     */
    /**
     * Sample code: Get Operation Results of Protected Vm.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getOperationResultsOfProtectedVm(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectedItemOperationResults()
            .getWithResponse(
                "NetSDKTestRsVault",
                "SwaggerTestRg",
                "Azure",
                "IaasVMContainer;iaasvmcontainerv2;netsdktestrg;netvmtestv2vm1",
                "VM;iaasvmcontainerv2;netsdktestrg;netvmtestv2vm1",
                "00000000-0000-0000-0000-000000000000",
                com.azure.core.util.Context.NONE);
    }
}
```

### ProtectedItemOperationStatuses_Get

```java
/** Samples for ProtectedItemOperationStatuses Get. */
public final class ProtectedItemOperationStatusesGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/ProtectedItemOperationStatus.json
     */
    /**
     * Sample code: Get Operation Status of Protected Vm.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getOperationStatusOfProtectedVm(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectedItemOperationStatuses()
            .getWithResponse(
                "NetSDKTestRsVault",
                "SwaggerTestRg",
                "Azure",
                "IaasVMContainer;iaasvmcontainerv2;netsdktestrg;netvmtestv2vm1",
                "VM;iaasvmcontainerv2;netsdktestrg;netvmtestv2vm1",
                "00000000-0000-0000-0000-000000000000",
                com.azure.core.util.Context.NONE);
    }
}
```

### ProtectedItems_CreateOrUpdate

```java
import com.azure.resourcemanager.recoveryservicesbackup.models.AzureIaaSComputeVMProtectedItem;
import com.azure.resourcemanager.recoveryservicesbackup.models.ProtectionState;

/** Samples for ProtectedItems CreateOrUpdate. */
public final class ProtectedItemsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/StopProtection.json
     */
    /**
     * Sample code: Stop Protection with retain data on Azure IaasVm.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void stopProtectionWithRetainDataOnAzureIaasVm(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectedItems()
            .define("VM;iaasvmcontainerv2;netsdktestrg;netvmtestv2vm1")
            .withRegion((String) null)
            .withExistingProtectionContainer(
                "NetSDKTestRsVault",
                "SwaggerTestRg",
                "Azure",
                "IaasVMContainer;iaasvmcontainerv2;netsdktestrg;netvmtestv2vm1")
            .withProperties(
                new AzureIaaSComputeVMProtectedItem()
                    .withSourceResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/netsdktestrg/providers/Microsoft.Compute/virtualMachines/netvmtestv2vm1")
                    .withProtectionState(ProtectionState.PROTECTION_STOPPED))
            .create();
    }

    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/ConfigureProtection.json
     */
    /**
     * Sample code: Enable Protection on Azure IaasVm.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void enableProtectionOnAzureIaasVm(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectedItems()
            .define("VM;iaasvmcontainerv2;netsdktestrg;netvmtestv2vm1")
            .withRegion((String) null)
            .withExistingProtectionContainer(
                "NetSDKTestRsVault",
                "SwaggerTestRg",
                "Azure",
                "IaasVMContainer;iaasvmcontainerv2;netsdktestrg;netvmtestv2vm1")
            .withProperties(
                new AzureIaaSComputeVMProtectedItem()
                    .withSourceResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/netsdktestrg/providers/Microsoft.Compute/virtualMachines/netvmtestv2vm1")
                    .withPolicyId(
                        "/Subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/SwaggerTestRg/providers/Microsoft.RecoveryServices/vaults/NetSDKTestRsVault/backupPolicies/DefaultPolicy"))
            .create();
    }
}
```

### ProtectedItems_Delete

```java
/** Samples for ProtectedItems Delete. */
public final class ProtectedItemsDeleteSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/Common/ProtectedItem_Delete.json
     */
    /**
     * Sample code: Delete Protection from Azure Virtual Machine.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void deleteProtectionFromAzureVirtualMachine(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectedItems()
            .deleteWithResponse(
                "PySDKBackupTestRsVault",
                "PythonSDKBackupTestRg",
                "Azure",
                "iaasvmcontainer;iaasvmcontainerv2;pysdktestrg;pysdktestv2vm1",
                "vm;iaasvmcontainerv2;pysdktestrg;pysdktestv2vm1",
                com.azure.core.util.Context.NONE);
    }
}
```

### ProtectedItems_Get

```java
/** Samples for ProtectedItems Get. */
public final class ProtectedItemsGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/ClassicCompute_ProtectedItem_Get.json
     */
    /**
     * Sample code: Get Protected Classic Virtual Machine Details.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getProtectedClassicVirtualMachineDetails(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectedItems()
            .getWithResponse(
                "PySDKBackupTestRsVault",
                "PythonSDKBackupTestRg",
                "Azure",
                "iaasvmcontainer;iaasvmcontainer;iaasvm-rg;iaasvm-1",
                "vm;iaasvmcontainer;iaasvm-rg;iaasvm-1",
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/Compute_ProtectedItem_Get.json
     */
    /**
     * Sample code: Get Protected Virtual Machine Details.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getProtectedVirtualMachineDetails(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectedItems()
            .getWithResponse(
                "PySDKBackupTestRsVault",
                "PythonSDKBackupTestRg",
                "Azure",
                "iaasvmcontainer;iaasvmcontainerv2;iaasvm-rg;iaasvm-1",
                "vm;iaasvmcontainerv2;iaasvm-rg;iaasvm-1",
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### ProtectionContainerOperationResults_Get

```java
/** Samples for ProtectionContainerOperationResults Get. */
public final class ProtectionContainerOperationResultsGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureStorage/ProtectionContainers_Inquire_Result.json
     */
    /**
     * Sample code: Get Azure Storage Protection Container Operation Result.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getAzureStorageProtectionContainerOperationResult(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectionContainerOperationResults()
            .getWithResponse(
                "testvault",
                "test-rg",
                "Azure",
                "VMAppContainer;Compute;testRG;testSQL",
                "00000000-0000-0000-0000-000000000000",
                com.azure.core.util.Context.NONE);
    }
}
```

### ProtectionContainerRefreshOperationResults_Get

```java
/** Samples for ProtectionContainerRefreshOperationResults Get. */
public final class ProtectionContainerRefreshOperationResultsGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/Common/RefreshContainers_OperationResults.json
     */
    /**
     * Sample code: Azure Vm Discovery Operation Result.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void azureVmDiscoveryOperationResult(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectionContainerRefreshOperationResults()
            .getWithResponse(
                "NetSDKTestRsVault",
                "SwaggerTestRg",
                "Azure",
                "00000000-0000-0000-0000-000000000000",
                com.azure.core.util.Context.NONE);
    }
}
```

### ProtectionContainers_Get

```java
/** Samples for ProtectionContainers Get. */
public final class ProtectionContainersGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureWorkload/ProtectionContainers_Get.json
     */
    /**
     * Sample code: Get Protection Container Details.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getProtectionContainerDetails(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectionContainers()
            .getWithResponse(
                "testVault",
                "testRg",
                "Azure",
                "VMAppContainer;Compute;testRG;testSQL",
                com.azure.core.util.Context.NONE);
    }
}
```

### ProtectionContainers_Inquire

```java
/** Samples for ProtectionContainers Inquire. */
public final class ProtectionContainersInquireSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureStorage/ProtectionContainers_Inquire.json
     */
    /**
     * Sample code: Inquire Azure Storage Protection Containers.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void inquireAzureStorageProtectionContainers(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectionContainers()
            .inquireWithResponse(
                "testvault",
                "test-rg",
                "Azure",
                "storagecontainer;Storage;test-rg;teststorage",
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### ProtectionContainers_Refresh

```java
/** Samples for ProtectionContainers Refresh. */
public final class ProtectionContainersRefreshSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/Common/RefreshContainers.json
     */
    /**
     * Sample code: Trigger Azure Vm Discovery.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void triggerAzureVmDiscovery(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectionContainers()
            .refreshWithResponse("NetSDKTestRsVault", "SwaggerTestRg", "Azure", null, com.azure.core.util.Context.NONE);
    }
}
```

### ProtectionContainers_Register

```java
import com.azure.resourcemanager.recoveryservicesbackup.models.AcquireStorageAccountLock;
import com.azure.resourcemanager.recoveryservicesbackup.models.AzureStorageContainer;
import com.azure.resourcemanager.recoveryservicesbackup.models.BackupManagementType;

/** Samples for ProtectionContainers Register. */
public final class ProtectionContainersRegisterSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureStorage/ProtectionContainers_Register.json
     */
    /**
     * Sample code: RegisterAzure Storage ProtectionContainers.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void registerAzureStorageProtectionContainers(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectionContainers()
            .define("StorageContainer;Storage;SwaggerTestRg;swaggertestsa")
            .withRegion((String) null)
            .withExistingBackupFabric("swaggertestvault", "SwaggerTestRg", "Azure")
            .withProperties(
                new AzureStorageContainer()
                    .withFriendlyName("swaggertestsa")
                    .withBackupManagementType(BackupManagementType.AZURE_STORAGE)
                    .withSourceResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/SwaggerTestRg/providers/Microsoft.Storage/storageAccounts/swaggertestsa")
                    .withAcquireStorageAccountLock(AcquireStorageAccountLock.ACQUIRE))
            .create();
    }
}
```

### ProtectionContainers_Unregister

```java
/** Samples for ProtectionContainers Unregister. */
public final class ProtectionContainersUnregisterSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureWorkload/ProtectionContainers_Unregister.json
     */
    /**
     * Sample code: Unregister Protection Container.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void unregisterProtectionContainer(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectionContainers()
            .unregisterWithResponse(
                "testVault",
                "testRg",
                "Azure",
                "storagecontainer;Storage;test-rg;teststorage",
                com.azure.core.util.Context.NONE);
    }
}
```

### ProtectionIntent_CreateOrUpdate

```java
import com.azure.resourcemanager.recoveryservicesbackup.models.AzureResourceProtectionIntent;

/** Samples for ProtectionIntent CreateOrUpdate. */
public final class ProtectionIntentCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/ProtectionIntent_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or Update Azure Vm Protection Intent.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void createOrUpdateAzureVmProtectionIntent(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectionIntents()
            .define("vm;iaasvmcontainerv2;chamsrgtest;chamscandel")
            .withRegion((String) null)
            .withExistingBackupFabric("myVault", "myRG", "Azure")
            .withProperties(
                new AzureResourceProtectionIntent()
                    .withSourceResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/chamsrgtest/providers/Microsoft.Compute/virtualMachines/chamscandel")
                    .withPolicyId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myRG/providers/Microsoft.RecoveryServices/vaults/myVault/backupPolicies/myPolicy"))
            .create();
    }
}
```

### ProtectionIntent_Delete

```java
/** Samples for ProtectionIntent Delete. */
public final class ProtectionIntentDeleteSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureWorkload/BackupProtectionIntent_Delete.json
     */
    /**
     * Sample code: Delete Protection intent from item.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void deleteProtectionIntentFromItem(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectionIntents()
            .deleteWithResponse(
                "myVault", "myRG", "Azure", "249D9B07-D2EF-4202-AA64-65F35418564E", com.azure.core.util.Context.NONE);
    }
}
```

### ProtectionIntent_Get

```java
/** Samples for ProtectionIntent Get. */
public final class ProtectionIntentGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureWorkload/BackupProtectionIntent_Get.json
     */
    /**
     * Sample code: Get ProtectionIntent for an item.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getProtectionIntentForAnItem(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectionIntents()
            .getWithResponse(
                "myVault", "myRG", "Azure", "249D9B07-D2EF-4202-AA64-65F35418564E", com.azure.core.util.Context.NONE);
    }
}
```

### ProtectionIntent_Validate

```java
import com.azure.resourcemanager.recoveryservicesbackup.models.DataSourceType;
import com.azure.resourcemanager.recoveryservicesbackup.models.PreValidateEnableBackupRequest;

/** Samples for ProtectionIntent Validate. */
public final class ProtectionIntentValidateSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/ProtectionIntent_Validate.json
     */
    /**
     * Sample code: Validate Enable Protection on Azure Vm.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void validateEnableProtectionOnAzureVm(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectionIntents()
            .validateWithResponse(
                "southeastasia",
                new PreValidateEnableBackupRequest()
                    .withResourceType(DataSourceType.VM)
                    .withResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/arunaupgrade/providers/Microsoft.Compute/VirtualMachines/upgrade1")
                    .withVaultId(
                        "/Subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myRG/providers/Microsoft.RecoveryServices/Vaults/myVault")
                    .withProperties(""),
                com.azure.core.util.Context.NONE);
    }
}
```

### ProtectionPolicies_CreateOrUpdate

```java
import com.azure.resourcemanager.recoveryservicesbackup.models.AzureFileShareProtectionPolicy;
import com.azure.resourcemanager.recoveryservicesbackup.models.AzureIaaSvmProtectionPolicy;
import com.azure.resourcemanager.recoveryservicesbackup.models.AzureVmWorkloadProtectionPolicy;
import com.azure.resourcemanager.recoveryservicesbackup.models.DailyRetentionSchedule;
import com.azure.resourcemanager.recoveryservicesbackup.models.DailySchedule;
import com.azure.resourcemanager.recoveryservicesbackup.models.DayOfWeek;
import com.azure.resourcemanager.recoveryservicesbackup.models.HourlySchedule;
import com.azure.resourcemanager.recoveryservicesbackup.models.IaasvmPolicyType;
import com.azure.resourcemanager.recoveryservicesbackup.models.LogSchedulePolicy;
import com.azure.resourcemanager.recoveryservicesbackup.models.LongTermRetentionPolicy;
import com.azure.resourcemanager.recoveryservicesbackup.models.MonthOfYear;
import com.azure.resourcemanager.recoveryservicesbackup.models.MonthlyRetentionSchedule;
import com.azure.resourcemanager.recoveryservicesbackup.models.PolicyType;
import com.azure.resourcemanager.recoveryservicesbackup.models.RetentionDuration;
import com.azure.resourcemanager.recoveryservicesbackup.models.RetentionDurationType;
import com.azure.resourcemanager.recoveryservicesbackup.models.RetentionScheduleFormat;
import com.azure.resourcemanager.recoveryservicesbackup.models.ScheduleRunType;
import com.azure.resourcemanager.recoveryservicesbackup.models.Settings;
import com.azure.resourcemanager.recoveryservicesbackup.models.SimpleRetentionPolicy;
import com.azure.resourcemanager.recoveryservicesbackup.models.SimpleSchedulePolicy;
import com.azure.resourcemanager.recoveryservicesbackup.models.SimpleSchedulePolicyV2;
import com.azure.resourcemanager.recoveryservicesbackup.models.SubProtectionPolicy;
import com.azure.resourcemanager.recoveryservicesbackup.models.WeekOfMonth;
import com.azure.resourcemanager.recoveryservicesbackup.models.WeeklyRetentionFormat;
import com.azure.resourcemanager.recoveryservicesbackup.models.WeeklyRetentionSchedule;
import com.azure.resourcemanager.recoveryservicesbackup.models.WorkloadType;
import com.azure.resourcemanager.recoveryservicesbackup.models.YearlyRetentionSchedule;
import java.time.OffsetDateTime;
import java.util.Arrays;

/** Samples for ProtectionPolicies CreateOrUpdate. */
public final class ProtectionPoliciesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureWorkload/ProtectionPolicies_CreateOrUpdate_Complex.json
     */
    /**
     * Sample code: Create or Update Full Azure Workload Protection Policy.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void createOrUpdateFullAzureWorkloadProtectionPolicy(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectionPolicies()
            .define("testPolicy1")
            .withRegion((String) null)
            .withExistingVault("NetSDKTestRsVault", "SwaggerTestRg")
            .withProperties(
                new AzureVmWorkloadProtectionPolicy()
                    .withWorkLoadType(WorkloadType.SQLDATA_BASE)
                    .withSettings(new Settings().withTimeZone("Pacific Standard Time").withIssqlcompression(false))
                    .withSubProtectionPolicy(
                        Arrays
                            .asList(
                                new SubProtectionPolicy()
                                    .withPolicyType(PolicyType.FULL)
                                    .withSchedulePolicy(
                                        new SimpleSchedulePolicy()
                                            .withScheduleRunFrequency(ScheduleRunType.WEEKLY)
                                            .withScheduleRunDays(Arrays.asList(DayOfWeek.SUNDAY, DayOfWeek.TUESDAY))
                                            .withScheduleRunTimes(
                                                Arrays.asList(OffsetDateTime.parse("2018-01-24T10:00:00Z"))))
                                    .withRetentionPolicy(
                                        new LongTermRetentionPolicy()
                                            .withWeeklySchedule(
                                                new WeeklyRetentionSchedule()
                                                    .withDaysOfTheWeek(
                                                        Arrays.asList(DayOfWeek.SUNDAY, DayOfWeek.TUESDAY))
                                                    .withRetentionTimes(
                                                        Arrays.asList(OffsetDateTime.parse("2018-01-24T10:00:00Z")))
                                                    .withRetentionDuration(
                                                        new RetentionDuration()
                                                            .withCount(2)
                                                            .withDurationType(RetentionDurationType.WEEKS)))
                                            .withMonthlySchedule(
                                                new MonthlyRetentionSchedule()
                                                    .withRetentionScheduleFormatType(RetentionScheduleFormat.WEEKLY)
                                                    .withRetentionScheduleWeekly(
                                                        new WeeklyRetentionFormat()
                                                            .withDaysOfTheWeek(Arrays.asList(DayOfWeek.SUNDAY))
                                                            .withWeeksOfTheMonth(Arrays.asList(WeekOfMonth.SECOND)))
                                                    .withRetentionTimes(
                                                        Arrays.asList(OffsetDateTime.parse("2018-01-24T10:00:00Z")))
                                                    .withRetentionDuration(
                                                        new RetentionDuration()
                                                            .withCount(1)
                                                            .withDurationType(RetentionDurationType.MONTHS)))
                                            .withYearlySchedule(
                                                new YearlyRetentionSchedule()
                                                    .withRetentionScheduleFormatType(RetentionScheduleFormat.WEEKLY)
                                                    .withMonthsOfYear(
                                                        Arrays
                                                            .asList(
                                                                MonthOfYear.JANUARY,
                                                                MonthOfYear.JUNE,
                                                                MonthOfYear.DECEMBER))
                                                    .withRetentionScheduleWeekly(
                                                        new WeeklyRetentionFormat()
                                                            .withDaysOfTheWeek(Arrays.asList(DayOfWeek.SUNDAY))
                                                            .withWeeksOfTheMonth(Arrays.asList(WeekOfMonth.LAST)))
                                                    .withRetentionTimes(
                                                        Arrays.asList(OffsetDateTime.parse("2018-01-24T10:00:00Z")))
                                                    .withRetentionDuration(
                                                        new RetentionDuration()
                                                            .withCount(1)
                                                            .withDurationType(RetentionDurationType.YEARS)))),
                                new SubProtectionPolicy()
                                    .withPolicyType(PolicyType.DIFFERENTIAL)
                                    .withSchedulePolicy(
                                        new SimpleSchedulePolicy()
                                            .withScheduleRunFrequency(ScheduleRunType.WEEKLY)
                                            .withScheduleRunDays(Arrays.asList(DayOfWeek.FRIDAY))
                                            .withScheduleRunTimes(
                                                Arrays.asList(OffsetDateTime.parse("2018-01-24T10:00:00Z"))))
                                    .withRetentionPolicy(
                                        new SimpleRetentionPolicy()
                                            .withRetentionDuration(
                                                new RetentionDuration()
                                                    .withCount(8)
                                                    .withDurationType(RetentionDurationType.DAYS))),
                                new SubProtectionPolicy()
                                    .withPolicyType(PolicyType.LOG)
                                    .withSchedulePolicy(new LogSchedulePolicy().withScheduleFrequencyInMins(60))
                                    .withRetentionPolicy(
                                        new SimpleRetentionPolicy()
                                            .withRetentionDuration(
                                                new RetentionDuration()
                                                    .withCount(7)
                                                    .withDurationType(RetentionDurationType.DAYS))))))
            .create();
    }

    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/ProtectionPolicies_CreateOrUpdate_Simple.json
     */
    /**
     * Sample code: Create or Update Simple Azure Vm Protection Policy.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void createOrUpdateSimpleAzureVmProtectionPolicy(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectionPolicies()
            .define("testPolicy1")
            .withRegion((String) null)
            .withExistingVault("NetSDKTestRsVault", "SwaggerTestRg")
            .withProperties(
                new AzureIaaSvmProtectionPolicy()
                    .withSchedulePolicy(
                        new SimpleSchedulePolicy()
                            .withScheduleRunFrequency(ScheduleRunType.DAILY)
                            .withScheduleRunTimes(Arrays.asList(OffsetDateTime.parse("2018-01-24T02:00:00Z"))))
                    .withRetentionPolicy(
                        new LongTermRetentionPolicy()
                            .withDailySchedule(
                                new DailyRetentionSchedule()
                                    .withRetentionTimes(Arrays.asList(OffsetDateTime.parse("2018-01-24T02:00:00Z")))
                                    .withRetentionDuration(
                                        new RetentionDuration()
                                            .withCount(1)
                                            .withDurationType(RetentionDurationType.DAYS))))
                    .withTimeZone("Pacific Standard Time"))
            .create();
    }

    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureStorage/ProtectionPolicies_CreateOrUpdate_Daily.json
     */
    /**
     * Sample code: Create or Update Daily Azure Storage Protection Policy.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void createOrUpdateDailyAzureStorageProtectionPolicy(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectionPolicies()
            .define("dailyPolicy2")
            .withRegion((String) null)
            .withExistingVault("swaggertestvault", "SwaggerTestRg")
            .withProperties(
                new AzureFileShareProtectionPolicy()
                    .withWorkLoadType(WorkloadType.AZURE_FILE_SHARE)
                    .withSchedulePolicy(
                        new SimpleSchedulePolicy()
                            .withScheduleRunFrequency(ScheduleRunType.DAILY)
                            .withScheduleRunTimes(Arrays.asList(OffsetDateTime.parse("2021-09-29T08:00:00.000Z"))))
                    .withRetentionPolicy(
                        new LongTermRetentionPolicy()
                            .withDailySchedule(
                                new DailyRetentionSchedule()
                                    .withRetentionTimes(Arrays.asList(OffsetDateTime.parse("2021-09-29T08:00:00.000Z")))
                                    .withRetentionDuration(
                                        new RetentionDuration()
                                            .withCount(5)
                                            .withDurationType(RetentionDurationType.DAYS)))
                            .withWeeklySchedule(
                                new WeeklyRetentionSchedule()
                                    .withDaysOfTheWeek(Arrays.asList(DayOfWeek.SUNDAY))
                                    .withRetentionTimes(Arrays.asList(OffsetDateTime.parse("2021-09-29T08:00:00.000Z")))
                                    .withRetentionDuration(
                                        new RetentionDuration()
                                            .withCount(12)
                                            .withDurationType(RetentionDurationType.WEEKS)))
                            .withMonthlySchedule(
                                new MonthlyRetentionSchedule()
                                    .withRetentionScheduleFormatType(RetentionScheduleFormat.WEEKLY)
                                    .withRetentionScheduleWeekly(
                                        new WeeklyRetentionFormat()
                                            .withDaysOfTheWeek(Arrays.asList(DayOfWeek.SUNDAY))
                                            .withWeeksOfTheMonth(Arrays.asList(WeekOfMonth.FIRST)))
                                    .withRetentionTimes(Arrays.asList(OffsetDateTime.parse("2021-09-29T08:00:00.000Z")))
                                    .withRetentionDuration(
                                        new RetentionDuration()
                                            .withCount(60)
                                            .withDurationType(RetentionDurationType.MONTHS)))
                            .withYearlySchedule(
                                new YearlyRetentionSchedule()
                                    .withRetentionScheduleFormatType(RetentionScheduleFormat.WEEKLY)
                                    .withMonthsOfYear(Arrays.asList(MonthOfYear.JANUARY))
                                    .withRetentionScheduleWeekly(
                                        new WeeklyRetentionFormat()
                                            .withDaysOfTheWeek(Arrays.asList(DayOfWeek.SUNDAY))
                                            .withWeeksOfTheMonth(Arrays.asList(WeekOfMonth.FIRST)))
                                    .withRetentionTimes(Arrays.asList(OffsetDateTime.parse("2021-09-29T08:00:00.000Z")))
                                    .withRetentionDuration(
                                        new RetentionDuration()
                                            .withCount(10)
                                            .withDurationType(RetentionDurationType.YEARS))))
                    .withTimeZone("UTC"))
            .create();
    }

    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureStorage/ProtectionPolicies_CreateOrUpdate_Hourly.json
     */
    /**
     * Sample code: Create or Update Hourly Azure Storage Protection Policy.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void createOrUpdateHourlyAzureStorageProtectionPolicy(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectionPolicies()
            .define("newPolicy2")
            .withRegion((String) null)
            .withExistingVault("swaggertestvault", "SwaggerTestRg")
            .withProperties(
                new AzureFileShareProtectionPolicy()
                    .withWorkLoadType(WorkloadType.AZURE_FILE_SHARE)
                    .withSchedulePolicy(
                        new SimpleSchedulePolicy()
                            .withScheduleRunFrequency(ScheduleRunType.HOURLY)
                            .withHourlySchedule(
                                new HourlySchedule()
                                    .withInterval(4)
                                    .withScheduleWindowStartTime(OffsetDateTime.parse("2021-09-29T08:00:00.000Z"))
                                    .withScheduleWindowDuration(12)))
                    .withRetentionPolicy(
                        new LongTermRetentionPolicy()
                            .withDailySchedule(
                                new DailyRetentionSchedule()
                                    .withRetentionDuration(
                                        new RetentionDuration()
                                            .withCount(5)
                                            .withDurationType(RetentionDurationType.DAYS)))
                            .withWeeklySchedule(
                                new WeeklyRetentionSchedule()
                                    .withDaysOfTheWeek(Arrays.asList(DayOfWeek.SUNDAY))
                                    .withRetentionDuration(
                                        new RetentionDuration()
                                            .withCount(12)
                                            .withDurationType(RetentionDurationType.WEEKS)))
                            .withMonthlySchedule(
                                new MonthlyRetentionSchedule()
                                    .withRetentionScheduleFormatType(RetentionScheduleFormat.WEEKLY)
                                    .withRetentionScheduleWeekly(
                                        new WeeklyRetentionFormat()
                                            .withDaysOfTheWeek(Arrays.asList(DayOfWeek.SUNDAY))
                                            .withWeeksOfTheMonth(Arrays.asList(WeekOfMonth.FIRST)))
                                    .withRetentionDuration(
                                        new RetentionDuration()
                                            .withCount(60)
                                            .withDurationType(RetentionDurationType.MONTHS)))
                            .withYearlySchedule(
                                new YearlyRetentionSchedule()
                                    .withRetentionScheduleFormatType(RetentionScheduleFormat.WEEKLY)
                                    .withMonthsOfYear(Arrays.asList(MonthOfYear.JANUARY))
                                    .withRetentionScheduleWeekly(
                                        new WeeklyRetentionFormat()
                                            .withDaysOfTheWeek(Arrays.asList(DayOfWeek.SUNDAY))
                                            .withWeeksOfTheMonth(Arrays.asList(WeekOfMonth.FIRST)))
                                    .withRetentionDuration(
                                        new RetentionDuration()
                                            .withCount(10)
                                            .withDurationType(RetentionDurationType.YEARS))))
                    .withTimeZone("UTC"))
            .create();
    }

    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/ProtectionPolicies_CreateOrUpdate_Complex.json
     */
    /**
     * Sample code: Create or Update Full Azure Vm Protection Policy.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void createOrUpdateFullAzureVmProtectionPolicy(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectionPolicies()
            .define("testPolicy1")
            .withRegion((String) null)
            .withExistingVault("NetSDKTestRsVault", "SwaggerTestRg")
            .withProperties(
                new AzureIaaSvmProtectionPolicy()
                    .withSchedulePolicy(
                        new SimpleSchedulePolicy()
                            .withScheduleRunFrequency(ScheduleRunType.WEEKLY)
                            .withScheduleRunDays(
                                Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY))
                            .withScheduleRunTimes(Arrays.asList(OffsetDateTime.parse("2018-01-24T10:00:00Z"))))
                    .withRetentionPolicy(
                        new LongTermRetentionPolicy()
                            .withWeeklySchedule(
                                new WeeklyRetentionSchedule()
                                    .withDaysOfTheWeek(
                                        Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY))
                                    .withRetentionTimes(Arrays.asList(OffsetDateTime.parse("2018-01-24T10:00:00Z")))
                                    .withRetentionDuration(
                                        new RetentionDuration()
                                            .withCount(1)
                                            .withDurationType(RetentionDurationType.WEEKS)))
                            .withMonthlySchedule(
                                new MonthlyRetentionSchedule()
                                    .withRetentionScheduleFormatType(RetentionScheduleFormat.WEEKLY)
                                    .withRetentionScheduleWeekly(
                                        new WeeklyRetentionFormat()
                                            .withDaysOfTheWeek(Arrays.asList(DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY))
                                            .withWeeksOfTheMonth(Arrays.asList(WeekOfMonth.FIRST, WeekOfMonth.THIRD)))
                                    .withRetentionTimes(Arrays.asList(OffsetDateTime.parse("2018-01-24T10:00:00Z")))
                                    .withRetentionDuration(
                                        new RetentionDuration()
                                            .withCount(2)
                                            .withDurationType(RetentionDurationType.MONTHS)))
                            .withYearlySchedule(
                                new YearlyRetentionSchedule()
                                    .withRetentionScheduleFormatType(RetentionScheduleFormat.WEEKLY)
                                    .withMonthsOfYear(Arrays.asList(MonthOfYear.FEBRUARY, MonthOfYear.NOVEMBER))
                                    .withRetentionScheduleWeekly(
                                        new WeeklyRetentionFormat()
                                            .withDaysOfTheWeek(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.THURSDAY))
                                            .withWeeksOfTheMonth(Arrays.asList(WeekOfMonth.FOURTH)))
                                    .withRetentionTimes(Arrays.asList(OffsetDateTime.parse("2018-01-24T10:00:00Z")))
                                    .withRetentionDuration(
                                        new RetentionDuration()
                                            .withCount(4)
                                            .withDurationType(RetentionDurationType.YEARS))))
                    .withTimeZone("Pacific Standard Time"))
            .create();
    }

    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/V2Policy/IaaS_v2_hourly.json
     */
    /**
     * Sample code: Create or Update Enhanced Azure Vm Protection Policy with Hourly backup.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void createOrUpdateEnhancedAzureVmProtectionPolicyWithHourlyBackup(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectionPolicies()
            .define("v2-daily-sample")
            .withRegion((String) null)
            .withExistingVault("NetSDKTestRsVault", "SwaggerTestRg")
            .withProperties(
                new AzureIaaSvmProtectionPolicy()
                    .withSchedulePolicy(
                        new SimpleSchedulePolicyV2()
                            .withScheduleRunFrequency(ScheduleRunType.HOURLY)
                            .withHourlySchedule(
                                new HourlySchedule()
                                    .withInterval(4)
                                    .withScheduleWindowStartTime(OffsetDateTime.parse("2021-12-17T08:00:00Z"))
                                    .withScheduleWindowDuration(16)))
                    .withRetentionPolicy(
                        new LongTermRetentionPolicy()
                            .withDailySchedule(
                                new DailyRetentionSchedule()
                                    .withRetentionTimes(
                                        Arrays.asList(OffsetDateTime.parse("2021-12-17T08:00:00+00:00")))
                                    .withRetentionDuration(
                                        new RetentionDuration()
                                            .withCount(180)
                                            .withDurationType(RetentionDurationType.DAYS)))
                            .withWeeklySchedule(
                                new WeeklyRetentionSchedule()
                                    .withDaysOfTheWeek(Arrays.asList(DayOfWeek.SUNDAY))
                                    .withRetentionTimes(
                                        Arrays.asList(OffsetDateTime.parse("2021-12-17T08:00:00+00:00")))
                                    .withRetentionDuration(
                                        new RetentionDuration()
                                            .withCount(12)
                                            .withDurationType(RetentionDurationType.WEEKS)))
                            .withMonthlySchedule(
                                new MonthlyRetentionSchedule()
                                    .withRetentionScheduleFormatType(RetentionScheduleFormat.WEEKLY)
                                    .withRetentionScheduleWeekly(
                                        new WeeklyRetentionFormat()
                                            .withDaysOfTheWeek(Arrays.asList(DayOfWeek.SUNDAY))
                                            .withWeeksOfTheMonth(Arrays.asList(WeekOfMonth.FIRST)))
                                    .withRetentionTimes(
                                        Arrays.asList(OffsetDateTime.parse("2021-12-17T08:00:00+00:00")))
                                    .withRetentionDuration(
                                        new RetentionDuration()
                                            .withCount(60)
                                            .withDurationType(RetentionDurationType.MONTHS)))
                            .withYearlySchedule(
                                new YearlyRetentionSchedule()
                                    .withRetentionScheduleFormatType(RetentionScheduleFormat.WEEKLY)
                                    .withMonthsOfYear(Arrays.asList(MonthOfYear.JANUARY))
                                    .withRetentionScheduleWeekly(
                                        new WeeklyRetentionFormat()
                                            .withDaysOfTheWeek(Arrays.asList(DayOfWeek.SUNDAY))
                                            .withWeeksOfTheMonth(Arrays.asList(WeekOfMonth.FIRST)))
                                    .withRetentionTimes(
                                        Arrays.asList(OffsetDateTime.parse("2021-12-17T08:00:00+00:00")))
                                    .withRetentionDuration(
                                        new RetentionDuration()
                                            .withCount(10)
                                            .withDurationType(RetentionDurationType.YEARS))))
                    .withInstantRpRetentionRangeInDays(30)
                    .withTimeZone("India Standard Time")
                    .withPolicyType(IaasvmPolicyType.V2))
            .create();
    }

    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/V2Policy/IaaS_v2_daily.json
     */
    /**
     * Sample code: Create or Update Enhanced Azure Vm Protection Policy with daily backup.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void createOrUpdateEnhancedAzureVmProtectionPolicyWithDailyBackup(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectionPolicies()
            .define("v2-daily-sample")
            .withRegion((String) null)
            .withExistingVault("NetSDKTestRsVault", "SwaggerTestRg")
            .withProperties(
                new AzureIaaSvmProtectionPolicy()
                    .withSchedulePolicy(
                        new SimpleSchedulePolicyV2()
                            .withScheduleRunFrequency(ScheduleRunType.DAILY)
                            .withDailySchedule(
                                new DailySchedule()
                                    .withScheduleRunTimes(Arrays.asList(OffsetDateTime.parse("2018-01-24T10:00:00Z")))))
                    .withRetentionPolicy(
                        new LongTermRetentionPolicy()
                            .withDailySchedule(
                                new DailyRetentionSchedule()
                                    .withRetentionTimes(
                                        Arrays.asList(OffsetDateTime.parse("2021-12-17T08:00:00+00:00")))
                                    .withRetentionDuration(
                                        new RetentionDuration()
                                            .withCount(180)
                                            .withDurationType(RetentionDurationType.DAYS)))
                            .withWeeklySchedule(
                                new WeeklyRetentionSchedule()
                                    .withDaysOfTheWeek(Arrays.asList(DayOfWeek.SUNDAY))
                                    .withRetentionTimes(
                                        Arrays.asList(OffsetDateTime.parse("2021-12-17T08:00:00+00:00")))
                                    .withRetentionDuration(
                                        new RetentionDuration()
                                            .withCount(12)
                                            .withDurationType(RetentionDurationType.WEEKS)))
                            .withMonthlySchedule(
                                new MonthlyRetentionSchedule()
                                    .withRetentionScheduleFormatType(RetentionScheduleFormat.WEEKLY)
                                    .withRetentionScheduleWeekly(
                                        new WeeklyRetentionFormat()
                                            .withDaysOfTheWeek(Arrays.asList(DayOfWeek.SUNDAY))
                                            .withWeeksOfTheMonth(Arrays.asList(WeekOfMonth.FIRST)))
                                    .withRetentionTimes(
                                        Arrays.asList(OffsetDateTime.parse("2021-12-17T08:00:00+00:00")))
                                    .withRetentionDuration(
                                        new RetentionDuration()
                                            .withCount(60)
                                            .withDurationType(RetentionDurationType.MONTHS)))
                            .withYearlySchedule(
                                new YearlyRetentionSchedule()
                                    .withRetentionScheduleFormatType(RetentionScheduleFormat.WEEKLY)
                                    .withMonthsOfYear(Arrays.asList(MonthOfYear.JANUARY))
                                    .withRetentionScheduleWeekly(
                                        new WeeklyRetentionFormat()
                                            .withDaysOfTheWeek(Arrays.asList(DayOfWeek.SUNDAY))
                                            .withWeeksOfTheMonth(Arrays.asList(WeekOfMonth.FIRST)))
                                    .withRetentionTimes(
                                        Arrays.asList(OffsetDateTime.parse("2021-12-17T08:00:00+00:00")))
                                    .withRetentionDuration(
                                        new RetentionDuration()
                                            .withCount(10)
                                            .withDurationType(RetentionDurationType.YEARS))))
                    .withInstantRpRetentionRangeInDays(30)
                    .withTimeZone("India Standard Time")
                    .withPolicyType(IaasvmPolicyType.V2))
            .create();
    }
}
```

### ProtectionPolicies_Delete

```java
/** Samples for ProtectionPolicies Delete. */
public final class ProtectionPoliciesDeleteSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/ProtectionPolicies_Delete.json
     */
    /**
     * Sample code: Delete Azure Vm Protection Policy.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void deleteAzureVmProtectionPolicy(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectionPolicies()
            .delete("NetSDKTestRsVault", "SwaggerTestRg", "testPolicy1", com.azure.core.util.Context.NONE);
    }
}
```

### ProtectionPolicies_Get

```java
/** Samples for ProtectionPolicies Get. */
public final class ProtectionPoliciesGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/ProtectionPolicies_Get.json
     */
    /**
     * Sample code: Get Azure IaasVm Protection Policy Details.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getAzureIaasVmProtectionPolicyDetails(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectionPolicies()
            .getWithResponse("NetSDKTestRsVault", "SwaggerTestRg", "testPolicy1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/V2Policy/v2-Get-Policy.json
     */
    /**
     * Sample code: Get Azure IaasVm Enhanced Protection Policy Details.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getAzureIaasVmEnhancedProtectionPolicyDetails(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectionPolicies()
            .getWithResponse("NetSDKTestRsVault", "SwaggerTestRg", "v2-daily-sample", com.azure.core.util.Context.NONE);
    }
}
```

### ProtectionPolicyOperationResults_Get

```java
/** Samples for ProtectionPolicyOperationResults Get. */
public final class ProtectionPolicyOperationResultsGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/ProtectionPolicyOperationResults_Get.json
     */
    /**
     * Sample code: Get Protection Policy Operation Results.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getProtectionPolicyOperationResults(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectionPolicyOperationResults()
            .getWithResponse(
                "NetSDKTestRsVault",
                "SwaggerTestRg",
                "testPolicy1",
                "00000000-0000-0000-0000-000000000000",
                com.azure.core.util.Context.NONE);
    }
}
```

### ProtectionPolicyOperationStatuses_Get

```java
/** Samples for ProtectionPolicyOperationStatuses Get. */
public final class ProtectionPolicyOperationStatusesGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/ProtectionPolicyOperationStatuses_Get.json
     */
    /**
     * Sample code: Get Protection Policy Operation Status.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getProtectionPolicyOperationStatus(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .protectionPolicyOperationStatuses()
            .getWithResponse(
                "NetSDKTestRsVault",
                "SwaggerTestRg",
                "testPolicy1",
                "00000000-0000-0000-0000-000000000000",
                com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryPoints_Get

```java
/** Samples for RecoveryPoints Get. */
public final class RecoveryPointsGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/RecoveryPoints_Get.json
     */
    /**
     * Sample code: Get Azure Vm Recovery Point Details.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getAzureVmRecoveryPointDetails(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .recoveryPoints()
            .getWithResponse(
                "rshvault",
                "rshhtestmdvmrg",
                "Azure",
                "IaasVMContainer;iaasvmcontainerv2;rshhtestmdvmrg;rshmdvmsmall",
                "VM;iaasvmcontainerv2;rshhtestmdvmrg;rshmdvmsmall",
                "26083826328862",
                com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryPoints_List

```java
/** Samples for RecoveryPoints List. */
public final class RecoveryPointsListSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/RecoveryPoints_List.json
     */
    /**
     * Sample code: Get Protected Azure Vm Recovery Points.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getProtectedAzureVmRecoveryPoints(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .recoveryPoints()
            .list(
                "rshvault",
                "rshhtestmdvmrg",
                "Azure",
                "IaasVMContainer;iaasvmcontainerv2;rshhtestmdvmrg;rshmdvmsmall",
                "VM;iaasvmcontainerv2;rshhtestmdvmrg;rshmdvmsmall",
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryPointsRecommendedForMove_List

```java
import com.azure.resourcemanager.recoveryservicesbackup.models.ListRecoveryPointsRecommendedForMoveRequest;
import java.util.Arrays;

/** Samples for RecoveryPointsRecommendedForMove List. */
public final class RecoveryPointsRecommendedForMoveListSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/RecoveryPointsRecommendedForMove_List.json
     */
    /**
     * Sample code: Get Protected Azure Vm Recovery Points Recommended for Move.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getProtectedAzureVmRecoveryPointsRecommendedForMove(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .recoveryPointsRecommendedForMoves()
            .list(
                "rshvault",
                "rshhtestmdvmrg",
                "Azure",
                "IaasVMContainer;iaasvmcontainerv2;rshhtestmdvmrg;rshmdvmsmall",
                "VM;iaasvmcontainerv2;rshhtestmdvmrg;rshmdvmsmall",
                new ListRecoveryPointsRecommendedForMoveRequest()
                    .withObjectType("ListRecoveryPointsRecommendedForMoveRequest")
                    .withExcludedRPList(Arrays.asList("348916168024334", "348916168024335")),
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceGuardProxies_Get

```java
/** Samples for ResourceGuardProxies Get. */
public final class ResourceGuardProxiesGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/ResourceGuardProxyCRUD/ListResourceGuardProxy.json
     */
    /**
     * Sample code: Get VaultGuardProxies.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getVaultGuardProxies(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager.resourceGuardProxies().get("sampleVault", "SampleResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### ResourceGuardProxyOperation_Delete

```java
/** Samples for ResourceGuardProxyOperation Delete. */
public final class ResourceGuardProxyOperationDeleteSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/ResourceGuardProxyCRUD/DeleteResourceGuardProxy.json
     */
    /**
     * Sample code: Delete ResourceGuardProxy.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void deleteResourceGuardProxy(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .resourceGuardProxyOperations()
            .deleteWithResponse(
                "sampleVault", "SampleResourceGroup", "swaggerExample", com.azure.core.util.Context.NONE);
    }
}
```

### ResourceGuardProxyOperation_Get

```java
/** Samples for ResourceGuardProxyOperation Get. */
public final class ResourceGuardProxyOperationGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/ResourceGuardProxyCRUD/GetResourceGuardProxy.json
     */
    /**
     * Sample code: Get ResourceGuardProxy.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getResourceGuardProxy(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .resourceGuardProxyOperations()
            .getWithResponse("sampleVault", "SampleResourceGroup", "swaggerExample", com.azure.core.util.Context.NONE);
    }
}
```

### ResourceGuardProxyOperation_Put

```java
import com.azure.resourcemanager.recoveryservicesbackup.models.ResourceGuardProxyBase;

/** Samples for ResourceGuardProxyOperation Put. */
public final class ResourceGuardProxyOperationPutSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/ResourceGuardProxyCRUD/PutResourceGuardProxy.json
     */
    /**
     * Sample code: Create ResourceGuardProxy.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void createResourceGuardProxy(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .resourceGuardProxyOperations()
            .define("swaggerExample")
            .withRegion((String) null)
            .withExistingVault("sampleVault", "SampleResourceGroup")
            .withProperties(
                new ResourceGuardProxyBase()
                    .withResourceGuardResourceId(
                        "/subscriptions/c999d45b-944f-418c-a0d8-c3fcfd1802c8/resourceGroups/vaultguardRGNew/providers/Microsoft.DataProtection/resourceGuards/VaultGuardTestNew"))
            .create();
    }
}
```

### ResourceGuardProxyOperation_UnlockDelete

```java
import com.azure.resourcemanager.recoveryservicesbackup.models.UnlockDeleteRequest;
import java.util.Arrays;

/** Samples for ResourceGuardProxyOperation UnlockDelete. */
public final class ResourceGuardProxyOperationUnlockDeleteSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/ResourceGuardProxyCRUD/UnlockDeleteResourceGuardProxy.json
     */
    /**
     * Sample code: UnlockDelete ResourceGuardProxy.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void unlockDeleteResourceGuardProxy(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .resourceGuardProxyOperations()
            .unlockDeleteWithResponse(
                "sampleVault",
                "SampleResourceGroup",
                "swaggerExample",
                new UnlockDeleteRequest()
                    .withResourceGuardOperationRequests(
                        Arrays
                            .asList(
                                "/subscriptions/c999d45b-944f-418c-a0d8-c3fcfd1802c8/resourceGroups/vaultguardRGNew/providers/Microsoft.DataProtection/resourceGuards/VaultGuardTestNew/deleteProtectedItemRequests/default"))
                    .withResourceToBeDeleted(
                        "/subscriptions/62b829ee-7936-40c9-a1c9-47a93f9f3965/resourceGroups/gaallarg/providers/Microsoft.RecoveryServices/vaults/MercuryCrrVault/backupFabrics/Azure/protectionContainers/VMAppContainer;compute;crrtestrg;crrtestvm/protectedItems/SQLDataBase;mssqlserver;testdb"),
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_BmsPrepareDataMove

```java
import com.azure.resourcemanager.recoveryservicesbackup.models.DataMoveLevel;
import com.azure.resourcemanager.recoveryservicesbackup.models.PrepareDataMoveRequest;

/** Samples for ResourceProvider BmsPrepareDataMove. */
public final class ResourceProviderBmsPrepareDataMoveSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/BackupDataMove/PrepareDataMove_Post.json
     */
    /**
     * Sample code: Prepare Data Move.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void prepareDataMove(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .resourceProviders()
            .bmsPrepareDataMove(
                "source-rsv",
                "sourceRG",
                new PrepareDataMoveRequest()
                    .withTargetResourceId(
                        "/subscriptions/04cf684a-d41f-4550-9f70-7708a3a2283b/resourceGroups/targetRG/providers/Microsoft.RecoveryServices/vaults/target-rsv")
                    .withTargetRegion("USGov Virginia")
                    .withDataMoveLevel(DataMoveLevel.VAULT),
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_BmsTriggerDataMove

```java
import com.azure.resourcemanager.recoveryservicesbackup.models.DataMoveLevel;
import com.azure.resourcemanager.recoveryservicesbackup.models.TriggerDataMoveRequest;

/** Samples for ResourceProvider BmsTriggerDataMove. */
public final class ResourceProviderBmsTriggerDataMoveSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/BackupDataMove/TriggerDataMove_Post.json
     */
    /**
     * Sample code: Trigger Data Move.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void triggerDataMove(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .resourceProviders()
            .bmsTriggerDataMove(
                "target-rsv",
                "targetRG",
                new TriggerDataMoveRequest()
                    .withSourceResourceId(
                        "/subscriptions/04cf684a-d41f-4550-9f70-7708a3a2283b/resourceGroups/sourceRG/providers/Microsoft.RecoveryServices/vaults/source-rsv")
                    .withSourceRegion("USGov Iowa")
                    .withDataMoveLevel(DataMoveLevel.VAULT)
                    .withCorrelationId("MTg2OTcyMzM4NzYyMjc1NDY3Nzs1YmUzYmVmNi04YjJiLTRhOTItOTllYi01NTM0MDllYjk2NjE="),
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_GetOperationStatus

```java
/** Samples for ResourceProvider GetOperationStatus. */
public final class ResourceProviderGetOperationStatusSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/BackupDataMove/BackupDataMoveOperationStatus_Get.json
     */
    /**
     * Sample code: Get OperationStatus.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getOperationStatus(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .resourceProviders()
            .getOperationStatusWithResponse(
                "source-rsv", "sourceRG", "00000000-0000-0000-0000-000000000000", com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_MoveRecoveryPoint

```java
import com.azure.resourcemanager.recoveryservicesbackup.models.MoveRPAcrossTiersRequest;
import com.azure.resourcemanager.recoveryservicesbackup.models.RecoveryPointTierType;

/** Samples for ResourceProvider MoveRecoveryPoint. */
public final class ResourceProviderMoveRecoveryPointSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/TriggerRecoveryPointMove_Post.json
     */
    /**
     * Sample code: Trigger RP Move Operation.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void triggerRPMoveOperation(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .resourceProviders()
            .moveRecoveryPoint(
                "testVault",
                "netsdktestrg",
                "Azure",
                "IaasVMContainer;iaasvmcontainerv2;netsdktestrg;netvmtestv2vm1",
                "VM;iaasvmcontainerv2;netsdktestrg;netvmtestv2vm1",
                "348916168024334",
                new MoveRPAcrossTiersRequest()
                    .withObjectType("MoveRPAcrossTiersRequest")
                    .withSourceTierType(RecoveryPointTierType.HARDENED_RP)
                    .withTargetTierType(RecoveryPointTierType.ARCHIVED_RP),
                com.azure.core.util.Context.NONE);
    }
}
```

### Restores_Trigger

```java
import com.azure.resourcemanager.recoveryservicesbackup.models.EncryptionDetails;
import com.azure.resourcemanager.recoveryservicesbackup.models.IaasVMRestoreRequest;
import com.azure.resourcemanager.recoveryservicesbackup.models.IaasVMRestoreWithRehydrationRequest;
import com.azure.resourcemanager.recoveryservicesbackup.models.IdentityBasedRestoreDetails;
import com.azure.resourcemanager.recoveryservicesbackup.models.IdentityInfo;
import com.azure.resourcemanager.recoveryservicesbackup.models.RecoveryPointRehydrationInfo;
import com.azure.resourcemanager.recoveryservicesbackup.models.RecoveryType;
import com.azure.resourcemanager.recoveryservicesbackup.models.RehydrationPriority;
import com.azure.resourcemanager.recoveryservicesbackup.models.RestoreRequestResource;

/** Samples for Restores Trigger. */
public final class RestoresTriggerSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/TriggerRestore_ALR_IaasVMRestoreWithRehydrationRequest.json
     */
    /**
     * Sample code: Restore to New Azure IaasVm with IaasVMRestoreWithRehydrationRequest.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void restoreToNewAzureIaasVmWithIaasVMRestoreWithRehydrationRequest(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .restores()
            .trigger(
                "testVault",
                "netsdktestrg",
                "Azure",
                "IaasVMContainer;iaasvmcontainerv2;netsdktestrg;netvmtestv2vm1",
                "VM;iaasvmcontainerv2;netsdktestrg;netvmtestv2vm1",
                "348916168024334",
                new RestoreRequestResource()
                    .withProperties(
                        new IaasVMRestoreWithRehydrationRequest()
                            .withRecoveryPointId("348916168024334")
                            .withRecoveryType(RecoveryType.ALTERNATE_LOCATION)
                            .withSourceResourceId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/netsdktestrg/providers/Microsoft.Compute/virtualMachines/netvmtestv2vm1")
                            .withTargetVirtualMachineId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/netsdktestrg2/providers/Microsoft.Compute/virtualmachines/RSMDALRVM981435")
                            .withTargetResourceGroupId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/netsdktestrg2")
                            .withStorageAccountId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testRg/providers/Microsoft.Storage/storageAccounts/testingAccount")
                            .withVirtualNetworkId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testRg/providers/Microsoft.Network/virtualNetworks/testNet")
                            .withSubnetId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testRg/providers/Microsoft.Network/virtualNetworks/testNet/subnets/default")
                            .withRegion("southeastasia")
                            .withCreateNewCloudService(false)
                            .withOriginalStorageAccountOption(false)
                            .withEncryptionDetails(new EncryptionDetails().withEncryptionEnabled(false))
                            .withRecoveryPointRehydrationInfo(
                                new RecoveryPointRehydrationInfo()
                                    .withRehydrationRetentionDuration("P7D")
                                    .withRehydrationPriority(RehydrationPriority.HIGH))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/TriggerRestore_ALR_IaasVMRestoreRequest.json
     */
    /**
     * Sample code: Restore to New Azure IaasVm with IaasVMRestoreRequest.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void restoreToNewAzureIaasVmWithIaasVMRestoreRequest(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .restores()
            .trigger(
                "testVault",
                "netsdktestrg",
                "Azure",
                "IaasVMContainer;iaasvmcontainerv2;netsdktestrg;netvmtestv2vm1",
                "VM;iaasvmcontainerv2;netsdktestrg;netvmtestv2vm1",
                "348916168024334",
                new RestoreRequestResource()
                    .withProperties(
                        new IaasVMRestoreRequest()
                            .withRecoveryPointId("348916168024334")
                            .withRecoveryType(RecoveryType.ALTERNATE_LOCATION)
                            .withSourceResourceId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/netsdktestrg/providers/Microsoft.Compute/virtualMachines/netvmtestv2vm1")
                            .withTargetVirtualMachineId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/netsdktestrg2/providers/Microsoft.Compute/virtualmachines/RSMDALRVM981435")
                            .withTargetResourceGroupId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/netsdktestrg2")
                            .withStorageAccountId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testRg/providers/Microsoft.Storage/storageAccounts/testingAccount")
                            .withVirtualNetworkId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testRg/providers/Microsoft.Network/virtualNetworks/testNet")
                            .withSubnetId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testRg/providers/Microsoft.Network/virtualNetworks/testNet/subnets/default")
                            .withRegion("southeastasia")
                            .withCreateNewCloudService(false)
                            .withOriginalStorageAccountOption(false)
                            .withEncryptionDetails(new EncryptionDetails().withEncryptionEnabled(false))
                            .withIdentityInfo(new IdentityInfo().withIsSystemAssignedIdentity(true))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/TriggerRestore_RestoreDisks_IaasVMRestoreWithRehydrationRequest.json
     */
    /**
     * Sample code: Restore Disks with IaasVMRestoreWithRehydrationRequest.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void restoreDisksWithIaasVMRestoreWithRehydrationRequest(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .restores()
            .trigger(
                "testVault",
                "netsdktestrg",
                "Azure",
                "IaasVMContainer;iaasvmcontainerv2;netsdktestrg;netvmtestv2vm1",
                "VM;iaasvmcontainerv2;netsdktestrg;netvmtestv2vm1",
                "348916168024334",
                new RestoreRequestResource()
                    .withProperties(
                        new IaasVMRestoreWithRehydrationRequest()
                            .withRecoveryPointId("348916168024334")
                            .withRecoveryType(RecoveryType.RESTORE_DISKS)
                            .withSourceResourceId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/netsdktestrg/providers/Microsoft.Compute/virtualMachines/netvmtestv2vm1")
                            .withStorageAccountId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testingRg/providers/Microsoft.Storage/storageAccounts/testAccount")
                            .withRegion("southeastasia")
                            .withCreateNewCloudService(true)
                            .withOriginalStorageAccountOption(false)
                            .withEncryptionDetails(new EncryptionDetails().withEncryptionEnabled(false))
                            .withRecoveryPointRehydrationInfo(
                                new RecoveryPointRehydrationInfo()
                                    .withRehydrationRetentionDuration("P7D")
                                    .withRehydrationPriority(RehydrationPriority.STANDARD))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/TriggerRestore_ALR_IaasVMRestoreRequest_IdentityBasedRestoreDetails.json
     */
    /**
     * Sample code: Restore to New Azure IaasVm with IaasVMRestoreRequest with identityBasedRestoreDetails.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void restoreToNewAzureIaasVmWithIaasVMRestoreRequestWithIdentityBasedRestoreDetails(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .restores()
            .trigger(
                "testVault",
                "netsdktestrg",
                "Azure",
                "IaasVMContainer;iaasvmcontainerv2;netsdktestrg;netvmtestv2vm1",
                "VM;iaasvmcontainerv2;netsdktestrg;netvmtestv2vm1",
                "348916168024334",
                new RestoreRequestResource()
                    .withProperties(
                        new IaasVMRestoreRequest()
                            .withRecoveryPointId("348916168024334")
                            .withRecoveryType(RecoveryType.ALTERNATE_LOCATION)
                            .withSourceResourceId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/netsdktestrg/providers/Microsoft.Compute/virtualMachines/netvmtestv2vm1")
                            .withTargetVirtualMachineId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/netsdktestrg2/providers/Microsoft.Compute/virtualmachines/RSMDALRVM981435")
                            .withTargetResourceGroupId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/netsdktestrg2")
                            .withVirtualNetworkId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testRg/providers/Microsoft.Network/virtualNetworks/testNet")
                            .withSubnetId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testRg/providers/Microsoft.Network/virtualNetworks/testNet/subnets/default")
                            .withRegion("southeastasia")
                            .withCreateNewCloudService(false)
                            .withOriginalStorageAccountOption(false)
                            .withEncryptionDetails(new EncryptionDetails().withEncryptionEnabled(false))
                            .withIdentityInfo(new IdentityInfo().withIsSystemAssignedIdentity(true))
                            .withIdentityBasedRestoreDetails(
                                new IdentityBasedRestoreDetails()
                                    .withTargetStorageAccountId(
                                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testRg/providers/Microsoft.Storage/storageAccounts/testingAccount"))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/TriggerRestore_RestoreDisks_IaasVMRestoreRequest.json
     */
    /**
     * Sample code: Restore Disks with IaasVMRestoreRequest.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void restoreDisksWithIaasVMRestoreRequest(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .restores()
            .trigger(
                "testVault",
                "netsdktestrg",
                "Azure",
                "IaasVMContainer;iaasvmcontainerv2;netsdktestrg;netvmtestv2vm1",
                "VM;iaasvmcontainerv2;netsdktestrg;netvmtestv2vm1",
                "348916168024334",
                new RestoreRequestResource()
                    .withProperties(
                        new IaasVMRestoreRequest()
                            .withRecoveryPointId("348916168024334")
                            .withRecoveryType(RecoveryType.RESTORE_DISKS)
                            .withSourceResourceId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/netsdktestrg/providers/Microsoft.Compute/virtualMachines/netvmtestv2vm1")
                            .withStorageAccountId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testingRg/providers/Microsoft.Storage/storageAccounts/testAccount")
                            .withRegion("southeastasia")
                            .withCreateNewCloudService(true)
                            .withOriginalStorageAccountOption(false)
                            .withEncryptionDetails(new EncryptionDetails().withEncryptionEnabled(false))
                            .withIdentityInfo(
                                new IdentityInfo()
                                    .withIsSystemAssignedIdentity(false)
                                    .withManagedIdentityResourceId(
                                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/asmaskarRG1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/asmaskartestmsi"))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/TriggerRestore_RestoreDisks_IaasVMRestoreRequest_IdentityBasedRestoreDetails.json
     */
    /**
     * Sample code: Restore Disks with IaasVMRestoreRequest with IdentityBasedRestoreDetails.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void restoreDisksWithIaasVMRestoreRequestWithIdentityBasedRestoreDetails(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .restores()
            .trigger(
                "testVault",
                "netsdktestrg",
                "Azure",
                "IaasVMContainer;iaasvmcontainerv2;netsdktestrg;netvmtestv2vm1",
                "VM;iaasvmcontainerv2;netsdktestrg;netvmtestv2vm1",
                "348916168024334",
                new RestoreRequestResource()
                    .withProperties(
                        new IaasVMRestoreRequest()
                            .withRecoveryPointId("348916168024334")
                            .withRecoveryType(RecoveryType.RESTORE_DISKS)
                            .withSourceResourceId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/netsdktestrg/providers/Microsoft.Compute/virtualMachines/netvmtestv2vm1")
                            .withRegion("southeastasia")
                            .withCreateNewCloudService(true)
                            .withOriginalStorageAccountOption(false)
                            .withEncryptionDetails(new EncryptionDetails().withEncryptionEnabled(false))
                            .withIdentityInfo(
                                new IdentityInfo()
                                    .withIsSystemAssignedIdentity(false)
                                    .withManagedIdentityResourceId(
                                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/asmaskarRG1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/asmaskartestmsi"))
                            .withIdentityBasedRestoreDetails(
                                new IdentityBasedRestoreDetails()
                                    .withTargetStorageAccountId(
                                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testingRg/providers/Microsoft.Storage/storageAccounts/testAccount"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### SecurityPINs_Get

```java
/** Samples for SecurityPINs Get. */
public final class SecurityPINsGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/Common/BackupSecurityPin_Get.json
     */
    /**
     * Sample code: Get Vault Security Pin.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getVaultSecurityPin(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager.securityPINs().getWithResponse("SwaggerTest", "SwaggerTestRg", null, com.azure.core.util.Context.NONE);
    }
}
```

### ValidateOperation_Trigger

```java
import com.azure.resourcemanager.recoveryservicesbackup.models.EncryptionDetails;
import com.azure.resourcemanager.recoveryservicesbackup.models.IaasVMRestoreRequest;
import com.azure.resourcemanager.recoveryservicesbackup.models.IdentityInfo;
import com.azure.resourcemanager.recoveryservicesbackup.models.RecoveryType;
import com.azure.resourcemanager.recoveryservicesbackup.models.ValidateIaasVMRestoreOperationRequest;

/** Samples for ValidateOperation Trigger. */
public final class ValidateOperationTriggerSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/TriggerValidateOperation_RestoreDisk.json
     */
    /**
     * Sample code: Trigger Validate Operation.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void triggerValidateOperation(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .validateOperations()
            .trigger(
                "testVault",
                "testRG",
                new ValidateIaasVMRestoreOperationRequest()
                    .withRestoreRequest(
                        new IaasVMRestoreRequest()
                            .withRecoveryPointId("348916168024334")
                            .withRecoveryType(RecoveryType.RESTORE_DISKS)
                            .withSourceResourceId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/netsdktestrg/providers/Microsoft.Compute/virtualMachines/netvmtestv2vm1")
                            .withStorageAccountId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testingRg/providers/Microsoft.Storage/storageAccounts/testAccount")
                            .withRegion("southeastasia")
                            .withCreateNewCloudService(true)
                            .withOriginalStorageAccountOption(false)
                            .withEncryptionDetails(new EncryptionDetails().withEncryptionEnabled(false))
                            .withIdentityInfo(
                                new IdentityInfo()
                                    .withIsSystemAssignedIdentity(false)
                                    .withManagedIdentityResourceId(
                                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/asmaskarRG1/providers/Microsoft.ManagedIdentity/userAssignedIdentities/asmaskartestmsi"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### ValidateOperationResults_Get

```java
/** Samples for ValidateOperationResults Get. */
public final class ValidateOperationResultsGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/ValidateOperationResults.json
     */
    /**
     * Sample code: Get Operation Results of Validate Operation.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getOperationResultsOfValidateOperation(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .validateOperationResults()
            .getWithResponse(
                "NetSDKTestRsVault",
                "SwaggerTestRg",
                "00000000-0000-0000-0000-000000000000",
                com.azure.core.util.Context.NONE);
    }
}
```

### ValidateOperationStatuses_Get

```java
/** Samples for ValidateOperationStatuses Get. */
public final class ValidateOperationStatusesGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesbackup/resource-manager/Microsoft.RecoveryServices/stable/2023-01-01/examples/AzureIaasVm/ValidateOperationStatus.json
     */
    /**
     * Sample code: Get Operation Status of Validate Operation.
     *
     * @param manager Entry point to RecoveryServicesBackupManager.
     */
    public static void getOperationStatusOfValidateOperation(
        com.azure.resourcemanager.recoveryservicesbackup.RecoveryServicesBackupManager manager) {
        manager
            .validateOperationStatuses()
            .getWithResponse(
                "NetSDKTestRsVault",
                "SwaggerTestRg",
                "00000000-0000-0000-0000-000000000000",
                com.azure.core.util.Context.NONE);
    }
}
```

