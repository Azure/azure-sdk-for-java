# Code snippets and samples


## BackupInstances

- [AdhocBackup](#backupinstances_adhocbackup)
- [CreateOrUpdate](#backupinstances_createorupdate)
- [Delete](#backupinstances_delete)
- [Get](#backupinstances_get)
- [List](#backupinstances_list)
- [TriggerRehydrate](#backupinstances_triggerrehydrate)
- [TriggerRestore](#backupinstances_triggerrestore)
- [ValidateForBackup](#backupinstances_validateforbackup)
- [ValidateForRestore](#backupinstances_validateforrestore)

## BackupPolicies

- [CreateOrUpdate](#backuppolicies_createorupdate)
- [Delete](#backuppolicies_delete)
- [Get](#backuppolicies_get)
- [List](#backuppolicies_list)

## BackupVaultOperationResults

- [Get](#backupvaultoperationresults_get)

## BackupVaults

- [CheckNameAvailability](#backupvaults_checknameavailability)
- [CreateOrUpdate](#backupvaults_createorupdate)
- [Delete](#backupvaults_delete)
- [GetByResourceGroup](#backupvaults_getbyresourcegroup)
- [List](#backupvaults_list)
- [ListByResourceGroup](#backupvaults_listbyresourcegroup)
- [Update](#backupvaults_update)

## DataProtection

- [CheckFeatureSupport](#dataprotection_checkfeaturesupport)

## DataProtectionOperations

- [List](#dataprotectionoperations_list)

## ExportJobs

- [Trigger](#exportjobs_trigger)

## ExportJobsOperationResult

- [Get](#exportjobsoperationresult_get)

## Jobs

- [Get](#jobs_get)
- [List](#jobs_list)

## OperationResult

- [Get](#operationresult_get)

## OperationStatus

- [Get](#operationstatus_get)

## RecoveryPoints

- [Get](#recoverypoints_get)
- [List](#recoverypoints_list)

## ResourceGuards

- [Delete](#resourceguards_delete)
- [GetBackupSecurityPinRequestsObjects](#resourceguards_getbackupsecuritypinrequestsobjects)
- [GetByResourceGroup](#resourceguards_getbyresourcegroup)
- [GetDefaultBackupSecurityPinRequestsObject](#resourceguards_getdefaultbackupsecuritypinrequestsobject)
- [GetDefaultDeleteProtectedItemRequestsObject](#resourceguards_getdefaultdeleteprotecteditemrequestsobject)
- [GetDefaultDeleteResourceGuardProxyRequestsObject](#resourceguards_getdefaultdeleteresourceguardproxyrequestsobject)
- [GetDefaultDisableSoftDeleteRequestsObject](#resourceguards_getdefaultdisablesoftdeleterequestsobject)
- [GetDefaultUpdateProtectedItemRequestsObject](#resourceguards_getdefaultupdateprotecteditemrequestsobject)
- [GetDefaultUpdateProtectionPolicyRequestsObject](#resourceguards_getdefaultupdateprotectionpolicyrequestsobject)
- [GetDeleteProtectedItemRequestsObjects](#resourceguards_getdeleteprotecteditemrequestsobjects)
- [GetDeleteResourceGuardProxyRequestsObjects](#resourceguards_getdeleteresourceguardproxyrequestsobjects)
- [GetDisableSoftDeleteRequestsObjects](#resourceguards_getdisablesoftdeleterequestsobjects)
- [GetUpdateProtectedItemRequestsObjects](#resourceguards_getupdateprotecteditemrequestsobjects)
- [GetUpdateProtectionPolicyRequestsObjects](#resourceguards_getupdateprotectionpolicyrequestsobjects)
- [List](#resourceguards_list)
- [ListByResourceGroup](#resourceguards_listbyresourcegroup)
- [Patch](#resourceguards_patch)
- [Put](#resourceguards_put)

## RestorableTimeRanges

- [Find](#restorabletimeranges_find)
### BackupInstances_AdhocBackup

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.dataprotection.models.AdHocBackupRuleOptions;
import com.azure.resourcemanager.dataprotection.models.AdhocBackupTriggerOption;
import com.azure.resourcemanager.dataprotection.models.TriggerBackupRequest;

/** Samples for BackupInstances AdhocBackup. */
public final class BackupInstancesAdhocBackupSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/BackupInstanceOperations/TriggerBackup.json
     */
    /**
     * Sample code: Trigger Adhoc Backup.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void triggerAdhocBackup(com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .backupInstances()
            .adhocBackup(
                "PratikPrivatePreviewVault1",
                "000pikumar",
                "testInstance1",
                new TriggerBackupRequest()
                    .withBackupRuleOptions(
                        new AdHocBackupRuleOptions()
                            .withRuleName("BackupWeekly")
                            .withTriggerOption(new AdhocBackupTriggerOption().withRetentionTagOverride("yearly"))),
                Context.NONE);
    }
}
```

### BackupInstances_CreateOrUpdate

```java
import com.azure.resourcemanager.dataprotection.models.AzureOperationalStoreParameters;
import com.azure.resourcemanager.dataprotection.models.BackupInstance;
import com.azure.resourcemanager.dataprotection.models.DataStoreTypes;
import com.azure.resourcemanager.dataprotection.models.Datasource;
import com.azure.resourcemanager.dataprotection.models.DatasourceSet;
import com.azure.resourcemanager.dataprotection.models.PolicyInfo;
import com.azure.resourcemanager.dataprotection.models.PolicyParameters;
import com.azure.resourcemanager.dataprotection.models.SecretStoreBasedAuthCredentials;
import com.azure.resourcemanager.dataprotection.models.SecretStoreResource;
import com.azure.resourcemanager.dataprotection.models.SecretStoreType;
import java.util.Arrays;

/** Samples for BackupInstances CreateOrUpdate. */
public final class BackupInstancesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/BackupInstanceOperations/PutBackupInstance.json
     */
    /**
     * Sample code: Create BackupInstance.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void createBackupInstance(com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .backupInstances()
            .define("testInstance1")
            .withExistingBackupVault("PratikPrivatePreviewVault1", "000pikumar")
            .withProperties(
                new BackupInstance()
                    .withFriendlyName("harshitbi2")
                    .withDataSourceInfo(
                        new Datasource()
                            .withDatasourceType("OssDB")
                            .withObjectType("Datasource")
                            .withResourceId(
                                "/subscriptions/f75d8d8b-6735-4697-82e1-1a7a3ff0d5d4/resourceGroups/viveksipgtest/providers/Microsoft.DBforPostgreSQL/servers/viveksipgtest/databases/testdb")
                            .withResourceLocation("")
                            .withResourceName("testdb")
                            .withResourceType("Microsoft.DBforPostgreSQL/servers/databases")
                            .withResourceUri(""))
                    .withDataSourceSetInfo(
                        new DatasourceSet()
                            .withDatasourceType("OssDB")
                            .withObjectType("DatasourceSet")
                            .withResourceId(
                                "/subscriptions/f75d8d8b-6735-4697-82e1-1a7a3ff0d5d4/resourceGroups/viveksipgtest/providers/Microsoft.DBforPostgreSQL/servers/viveksipgtest")
                            .withResourceLocation("")
                            .withResourceName("viveksipgtest")
                            .withResourceType("Microsoft.DBforPostgreSQL/servers")
                            .withResourceUri(""))
                    .withPolicyInfo(
                        new PolicyInfo()
                            .withPolicyId(
                                "/subscriptions/04cf684a-d41f-4550-9f70-7708a3a2283b/resourceGroups/000pikumar/providers/Microsoft.DataProtection/Backupvaults/PratikPrivatePreviewVault1/backupPolicies/PratikPolicy1")
                            .withPolicyParameters(
                                new PolicyParameters()
                                    .withDataStoreParametersList(
                                        Arrays
                                            .asList(
                                                new AzureOperationalStoreParameters()
                                                    .withDataStoreType(DataStoreTypes.OPERATIONAL_STORE)
                                                    .withResourceGroupId(
                                                        "/subscriptions/f75d8d8b-6735-4697-82e1-1a7a3ff0d5d4/resourceGroups/viveksipgtest")))))
                    .withDatasourceAuthCredentials(
                        new SecretStoreBasedAuthCredentials()
                            .withSecretStoreResource(
                                new SecretStoreResource()
                                    .withUri("https://samplevault.vault.azure.net/secrets/credentials")
                                    .withSecretStoreType(SecretStoreType.AZURE_KEY_VAULT)))
                    .withObjectType("BackupInstance"))
            .create();
    }
}
```

### BackupInstances_Delete

```java
import com.azure.core.util.Context;

/** Samples for BackupInstances Delete. */
public final class BackupInstancesDeleteSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/BackupInstanceOperations/DeleteBackupInstance.json
     */
    /**
     * Sample code: Delete BackupInstance.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void deleteBackupInstance(com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager.backupInstances().delete("PratikPrivatePreviewVault1", "000pikumar", "testInstance1", Context.NONE);
    }
}
```

### BackupInstances_Get

```java
import com.azure.core.util.Context;

/** Samples for BackupInstances Get. */
public final class BackupInstancesGetSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/BackupInstanceOperations/GetBackupInstance.json
     */
    /**
     * Sample code: Get BackupInstance.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void getBackupInstance(com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .backupInstances()
            .getWithResponse("PratikPrivatePreviewVault1", "000pikumar", "testInstance1", Context.NONE);
    }
}
```

### BackupInstances_List

```java
import com.azure.core.util.Context;

/** Samples for BackupInstances List. */
public final class BackupInstancesListSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/BackupInstanceOperations/ListBackupInstances.json
     */
    /**
     * Sample code: List BackupInstances in a Vault.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void listBackupInstancesInAVault(
        com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager.backupInstances().list("PratikPrivatePreviewVault1", "000pikumar", Context.NONE);
    }
}
```

### BackupInstances_TriggerRehydrate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.dataprotection.models.AzureBackupRehydrationRequest;
import com.azure.resourcemanager.dataprotection.models.RehydrationPriority;

/** Samples for BackupInstances TriggerRehydrate. */
public final class BackupInstancesTriggerRehydrateSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/BackupInstanceOperations/TriggerRehydrate.json
     */
    /**
     * Sample code: Trigger Rehydrate.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void triggerRehydrate(com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .backupInstances()
            .triggerRehydrate(
                "000pikumar",
                "PratikPrivatePreviewVault1",
                "testInstance1",
                new AzureBackupRehydrationRequest()
                    .withRecoveryPointId("hardcodedRP")
                    .withRehydrationPriority(RehydrationPriority.HIGH)
                    .withRehydrationRetentionDuration("7D"),
                Context.NONE);
    }
}
```

### BackupInstances_TriggerRestore

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.dataprotection.models.AzureBackupRecoveryPointBasedRestoreRequest;
import com.azure.resourcemanager.dataprotection.models.AzureBackupRestoreRequest;
import com.azure.resourcemanager.dataprotection.models.Datasource;
import com.azure.resourcemanager.dataprotection.models.DatasourceSet;
import com.azure.resourcemanager.dataprotection.models.RecoveryOption;
import com.azure.resourcemanager.dataprotection.models.RestoreFilesTargetInfo;
import com.azure.resourcemanager.dataprotection.models.RestoreTargetInfo;
import com.azure.resourcemanager.dataprotection.models.RestoreTargetLocationType;
import com.azure.resourcemanager.dataprotection.models.SecretStoreBasedAuthCredentials;
import com.azure.resourcemanager.dataprotection.models.SecretStoreResource;
import com.azure.resourcemanager.dataprotection.models.SecretStoreType;
import com.azure.resourcemanager.dataprotection.models.SourceDataStoreType;
import com.azure.resourcemanager.dataprotection.models.TargetDetails;

/** Samples for BackupInstances TriggerRestore. */
public final class BackupInstancesTriggerRestoreSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/BackupInstanceOperations/TriggerRestoreAsFiles.json
     */
    /**
     * Sample code: Trigger Restore As Files.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void triggerRestoreAsFiles(com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .backupInstances()
            .triggerRestore(
                "PrivatePreviewVault1",
                "000pikumar",
                "testInstance1",
                new AzureBackupRecoveryPointBasedRestoreRequest()
                    .withRestoreTargetInfo(
                        new RestoreFilesTargetInfo()
                            .withRecoveryOption(RecoveryOption.FAIL_IF_EXISTS)
                            .withRestoreLocation("southeastasia")
                            .withTargetDetails(
                                new TargetDetails()
                                    .withFilePrefix("restoredblob")
                                    .withRestoreTargetLocationType(RestoreTargetLocationType.AZURE_BLOBS)
                                    .withUrl("https://teststorage.blob.core.windows.net/restoretest")))
                    .withSourceDataStoreType(SourceDataStoreType.VAULT_STORE)
                    .withRecoveryPointId("hardcodedRP"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/BackupInstanceOperations/TriggerRestoreWithRehydration.json
     */
    /**
     * Sample code: Trigger Restore With Rehydration.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void triggerRestoreWithRehydration(
        com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .backupInstances()
            .triggerRestore(
                "PratikPrivatePreviewVault1",
                "000pikumar",
                "testInstance1",
                new AzureBackupRestoreRequest()
                    .withRestoreTargetInfo(
                        new RestoreTargetInfo()
                            .withRecoveryOption(RecoveryOption.FAIL_IF_EXISTS)
                            .withRestoreLocation("southeastasia")
                            .withDatasourceInfo(
                                new Datasource()
                                    .withDatasourceType("OssDB")
                                    .withObjectType("Datasource")
                                    .withResourceId(
                                        "/subscriptions/f75d8d8b-6735-4697-82e1-1a7a3ff0d5d4/resourceGroups/viveksipgtest/providers/Microsoft.DBforPostgreSQL/servers/viveksipgtest/databases/testdb")
                                    .withResourceLocation("")
                                    .withResourceName("testdb")
                                    .withResourceType("Microsoft.DBforPostgreSQL/servers/databases")
                                    .withResourceUri(""))
                            .withDatasourceSetInfo(
                                new DatasourceSet()
                                    .withDatasourceType("OssDB")
                                    .withObjectType("DatasourceSet")
                                    .withResourceId(
                                        "/subscriptions/f75d8d8b-6735-4697-82e1-1a7a3ff0d5d4/resourceGroups/viveksipgtest/providers/Microsoft.DBforPostgreSQL/servers/viveksipgtest")
                                    .withResourceLocation("")
                                    .withResourceName("viveksipgtest")
                                    .withResourceType("Microsoft.DBforPostgreSQL/servers")
                                    .withResourceUri("")))
                    .withSourceDataStoreType(SourceDataStoreType.VAULT_STORE),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/BackupInstanceOperations/TriggerRestore.json
     */
    /**
     * Sample code: Trigger Restore.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void triggerRestore(com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .backupInstances()
            .triggerRestore(
                "PratikPrivatePreviewVault1",
                "000pikumar",
                "testInstance1",
                new AzureBackupRecoveryPointBasedRestoreRequest()
                    .withRestoreTargetInfo(
                        new RestoreTargetInfo()
                            .withRecoveryOption(RecoveryOption.FAIL_IF_EXISTS)
                            .withRestoreLocation("southeastasia")
                            .withDatasourceInfo(
                                new Datasource()
                                    .withDatasourceType("OssDB")
                                    .withObjectType("Datasource")
                                    .withResourceId(
                                        "/subscriptions/f75d8d8b-6735-4697-82e1-1a7a3ff0d5d4/resourceGroups/viveksipgtest/providers/Microsoft.DBforPostgreSQL/servers/viveksipgtest/databases/testdb")
                                    .withResourceLocation("")
                                    .withResourceName("testdb")
                                    .withResourceType("Microsoft.DBforPostgreSQL/servers/databases")
                                    .withResourceUri(""))
                            .withDatasourceSetInfo(
                                new DatasourceSet()
                                    .withDatasourceType("OssDB")
                                    .withObjectType("DatasourceSet")
                                    .withResourceId(
                                        "/subscriptions/f75d8d8b-6735-4697-82e1-1a7a3ff0d5d4/resourceGroups/viveksipgtest/providers/Microsoft.DBforPostgreSQL/servers/viveksipgtest")
                                    .withResourceLocation("")
                                    .withResourceName("viveksipgtest")
                                    .withResourceType("Microsoft.DBforPostgreSQL/servers")
                                    .withResourceUri(""))
                            .withDatasourceAuthCredentials(
                                new SecretStoreBasedAuthCredentials()
                                    .withSecretStoreResource(
                                        new SecretStoreResource()
                                            .withUri("https://samplevault.vault.azure.net/secrets/credentials")
                                            .withSecretStoreType(SecretStoreType.AZURE_KEY_VAULT))))
                    .withSourceDataStoreType(SourceDataStoreType.VAULT_STORE)
                    .withRecoveryPointId("hardcodedRP"),
                Context.NONE);
    }
}
```

### BackupInstances_ValidateForBackup

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.dataprotection.models.BackupInstance;
import com.azure.resourcemanager.dataprotection.models.Datasource;
import com.azure.resourcemanager.dataprotection.models.DatasourceSet;
import com.azure.resourcemanager.dataprotection.models.PolicyInfo;
import com.azure.resourcemanager.dataprotection.models.SecretStoreBasedAuthCredentials;
import com.azure.resourcemanager.dataprotection.models.SecretStoreResource;
import com.azure.resourcemanager.dataprotection.models.SecretStoreType;
import com.azure.resourcemanager.dataprotection.models.ValidateForBackupRequest;

/** Samples for BackupInstances ValidateForBackup. */
public final class BackupInstancesValidateForBackupSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/BackupInstanceOperations/ValidateForBackup.json
     */
    /**
     * Sample code: Validate For Backup.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void validateForBackup(com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .backupInstances()
            .validateForBackup(
                "PratikPrivatePreviewVault1",
                "000pikumar",
                new ValidateForBackupRequest()
                    .withBackupInstance(
                        new BackupInstance()
                            .withFriendlyName("harshitbi2")
                            .withDataSourceInfo(
                                new Datasource()
                                    .withDatasourceType("OssDB")
                                    .withObjectType("Datasource")
                                    .withResourceId(
                                        "/subscriptions/f75d8d8b-6735-4697-82e1-1a7a3ff0d5d4/resourceGroups/viveksipgtest/providers/Microsoft.DBforPostgreSQL/servers/viveksipgtest/databases/testdb")
                                    .withResourceLocation("")
                                    .withResourceName("testdb")
                                    .withResourceType("Microsoft.DBforPostgreSQL/servers/databases")
                                    .withResourceUri(""))
                            .withDataSourceSetInfo(
                                new DatasourceSet()
                                    .withDatasourceType("OssDB")
                                    .withObjectType("DatasourceSet")
                                    .withResourceId(
                                        "/subscriptions/f75d8d8b-6735-4697-82e1-1a7a3ff0d5d4/resourceGroups/viveksipgtest/providers/Microsoft.DBforPostgreSQL/servers/viveksipgtest")
                                    .withResourceLocation("")
                                    .withResourceName("viveksipgtest")
                                    .withResourceType("Microsoft.DBforPostgreSQL/servers")
                                    .withResourceUri(""))
                            .withPolicyInfo(
                                new PolicyInfo()
                                    .withPolicyId(
                                        "/subscriptions/04cf684a-d41f-4550-9f70-7708a3a2283b/resourceGroups/000pikumar/providers/Microsoft.DataProtection/Backupvaults/PratikPrivatePreviewVault1/backupPolicies/PratikPolicy1"))
                            .withDatasourceAuthCredentials(
                                new SecretStoreBasedAuthCredentials()
                                    .withSecretStoreResource(
                                        new SecretStoreResource()
                                            .withUri("https://samplevault.vault.azure.net/secrets/credentials")
                                            .withSecretStoreType(SecretStoreType.AZURE_KEY_VAULT)))
                            .withObjectType("BackupInstance")),
                Context.NONE);
    }
}
```

### BackupInstances_ValidateForRestore

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.dataprotection.models.AzureBackupRecoveryPointBasedRestoreRequest;
import com.azure.resourcemanager.dataprotection.models.Datasource;
import com.azure.resourcemanager.dataprotection.models.DatasourceSet;
import com.azure.resourcemanager.dataprotection.models.RecoveryOption;
import com.azure.resourcemanager.dataprotection.models.RestoreTargetInfo;
import com.azure.resourcemanager.dataprotection.models.SecretStoreBasedAuthCredentials;
import com.azure.resourcemanager.dataprotection.models.SecretStoreResource;
import com.azure.resourcemanager.dataprotection.models.SecretStoreType;
import com.azure.resourcemanager.dataprotection.models.SourceDataStoreType;
import com.azure.resourcemanager.dataprotection.models.ValidateRestoreRequestObject;

/** Samples for BackupInstances ValidateForRestore. */
public final class BackupInstancesValidateForRestoreSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/BackupInstanceOperations/ValidateRestore.json
     */
    /**
     * Sample code: Validate Restore.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void validateRestore(com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .backupInstances()
            .validateForRestore(
                "PratikPrivatePreviewVault1",
                "000pikumar",
                "testInstance1",
                new ValidateRestoreRequestObject()
                    .withRestoreRequestObject(
                        new AzureBackupRecoveryPointBasedRestoreRequest()
                            .withRestoreTargetInfo(
                                new RestoreTargetInfo()
                                    .withRecoveryOption(RecoveryOption.FAIL_IF_EXISTS)
                                    .withRestoreLocation("southeastasia")
                                    .withDatasourceInfo(
                                        new Datasource()
                                            .withDatasourceType("OssDB")
                                            .withObjectType("Datasource")
                                            .withResourceId(
                                                "/subscriptions/f75d8d8b-6735-4697-82e1-1a7a3ff0d5d4/resourceGroups/viveksipgtest/providers/Microsoft.DBforPostgreSQL/servers/viveksipgtest/databases/testdb")
                                            .withResourceLocation("")
                                            .withResourceName("testdb")
                                            .withResourceType("Microsoft.DBforPostgreSQL/servers/databases")
                                            .withResourceUri(""))
                                    .withDatasourceSetInfo(
                                        new DatasourceSet()
                                            .withDatasourceType("OssDB")
                                            .withObjectType("DatasourceSet")
                                            .withResourceId(
                                                "/subscriptions/f75d8d8b-6735-4697-82e1-1a7a3ff0d5d4/resourceGroups/viveksipgtest/providers/Microsoft.DBforPostgreSQL/servers/viveksipgtest")
                                            .withResourceLocation("")
                                            .withResourceName("viveksipgtest")
                                            .withResourceType("Microsoft.DBforPostgreSQL/servers")
                                            .withResourceUri(""))
                                    .withDatasourceAuthCredentials(
                                        new SecretStoreBasedAuthCredentials()
                                            .withSecretStoreResource(
                                                new SecretStoreResource()
                                                    .withUri("https://samplevault.vault.azure.net/secrets/credentials")
                                                    .withSecretStoreType(SecretStoreType.AZURE_KEY_VAULT))))
                            .withSourceDataStoreType(SourceDataStoreType.VAULT_STORE)
                            .withRecoveryPointId("hardcodedRP")),
                Context.NONE);
    }
}
```

### BackupPolicies_CreateOrUpdate

```java
import com.azure.resourcemanager.dataprotection.models.AbsoluteDeleteOption;
import com.azure.resourcemanager.dataprotection.models.AzureBackupParams;
import com.azure.resourcemanager.dataprotection.models.AzureBackupRule;
import com.azure.resourcemanager.dataprotection.models.AzureRetentionRule;
import com.azure.resourcemanager.dataprotection.models.BackupPolicy;
import com.azure.resourcemanager.dataprotection.models.BackupSchedule;
import com.azure.resourcemanager.dataprotection.models.DataStoreInfoBase;
import com.azure.resourcemanager.dataprotection.models.DataStoreTypes;
import com.azure.resourcemanager.dataprotection.models.DayOfWeek;
import com.azure.resourcemanager.dataprotection.models.RetentionTag;
import com.azure.resourcemanager.dataprotection.models.ScheduleBasedBackupCriteria;
import com.azure.resourcemanager.dataprotection.models.ScheduleBasedTriggerContext;
import com.azure.resourcemanager.dataprotection.models.SourceLifeCycle;
import com.azure.resourcemanager.dataprotection.models.TaggingCriteria;
import java.time.OffsetDateTime;
import java.util.Arrays;

/** Samples for BackupPolicies CreateOrUpdate. */
public final class BackupPoliciesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/PolicyCRUD/CreateOrUpdateBackupPolicy.json
     */
    /**
     * Sample code: CreateOrUpdate BackupPolicy.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void createOrUpdateBackupPolicy(
        com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .backupPolicies()
            .define("OSSDBPolicy")
            .withExistingBackupVault("PrivatePreviewVault", "000pikumar")
            .withProperties(
                new BackupPolicy()
                    .withDatasourceTypes(Arrays.asList("OssDB"))
                    .withPolicyRules(
                        Arrays
                            .asList(
                                new AzureBackupRule()
                                    .withName("BackupWeekly")
                                    .withBackupParameters(new AzureBackupParams().withBackupType("Full"))
                                    .withDataStore(
                                        new DataStoreInfoBase()
                                            .withDataStoreType(DataStoreTypes.VAULT_STORE)
                                            .withObjectType("DataStoreInfoBase"))
                                    .withTrigger(
                                        new ScheduleBasedTriggerContext()
                                            .withSchedule(
                                                new BackupSchedule()
                                                    .withRepeatingTimeIntervals(
                                                        Arrays.asList("R/2019-11-20T08:00:00-08:00/P1W")))
                                            .withTaggingCriteria(
                                                Arrays
                                                    .asList(
                                                        new TaggingCriteria()
                                                            .withIsDefault(true)
                                                            .withTaggingPriority(99L)
                                                            .withTagInfo(new RetentionTag().withTagName("Default")),
                                                        new TaggingCriteria()
                                                            .withCriteria(
                                                                Arrays
                                                                    .asList(
                                                                        new ScheduleBasedBackupCriteria()
                                                                            .withDaysOfTheWeek(
                                                                                Arrays.asList(DayOfWeek.SUNDAY))
                                                                            .withScheduleTimes(
                                                                                Arrays
                                                                                    .asList(
                                                                                        OffsetDateTime
                                                                                            .parse(
                                                                                                "2019-03-01T13:00:00Z")))))
                                                            .withIsDefault(false)
                                                            .withTaggingPriority(20L)
                                                            .withTagInfo(new RetentionTag().withTagName("Weekly"))))),
                                new AzureRetentionRule()
                                    .withName("Default")
                                    .withIsDefault(true)
                                    .withLifecycles(
                                        Arrays
                                            .asList(
                                                new SourceLifeCycle()
                                                    .withDeleteAfter(new AbsoluteDeleteOption().withDuration("P1W"))
                                                    .withSourceDataStore(
                                                        new DataStoreInfoBase()
                                                            .withDataStoreType(DataStoreTypes.VAULT_STORE)
                                                            .withObjectType("DataStoreInfoBase")))),
                                new AzureRetentionRule()
                                    .withName("Weekly")
                                    .withIsDefault(false)
                                    .withLifecycles(
                                        Arrays
                                            .asList(
                                                new SourceLifeCycle()
                                                    .withDeleteAfter(new AbsoluteDeleteOption().withDuration("P12W"))
                                                    .withSourceDataStore(
                                                        new DataStoreInfoBase()
                                                            .withDataStoreType(DataStoreTypes.VAULT_STORE)
                                                            .withObjectType("DataStoreInfoBase")))))))
            .create();
    }
}
```

### BackupPolicies_Delete

```java
import com.azure.core.util.Context;

/** Samples for BackupPolicies Delete. */
public final class BackupPoliciesDeleteSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/PolicyCRUD/DeleteBackupPolicy.json
     */
    /**
     * Sample code: Delete BackupPolicy.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void deleteBackupPolicy(com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager.backupPolicies().deleteWithResponse("PrivatePreviewVault", "000pikumar", "OSSDBPolicy", Context.NONE);
    }
}
```

### BackupPolicies_Get

```java
import com.azure.core.util.Context;

/** Samples for BackupPolicies Get. */
public final class BackupPoliciesGetSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/PolicyCRUD/GetBackupPolicy.json
     */
    /**
     * Sample code: Get BackupPolicy.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void getBackupPolicy(com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager.backupPolicies().getWithResponse("PrivatePreviewVault", "000pikumar", "OSSDBPolicy", Context.NONE);
    }
}
```

### BackupPolicies_List

```java
import com.azure.core.util.Context;

/** Samples for BackupPolicies List. */
public final class BackupPoliciesListSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/PolicyCRUD/ListBackupPolicy.json
     */
    /**
     * Sample code: List BackupPolicy.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void listBackupPolicy(com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager.backupPolicies().list("PrivatePreviewVault", "000pikumar", Context.NONE);
    }
}
```

### BackupVaultOperationResults_Get

```java
import com.azure.core.util.Context;

/** Samples for BackupVaultOperationResults Get. */
public final class BackupVaultOperationResultsGetSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/VaultCRUD/GetOperationResultPatch.json
     */
    /**
     * Sample code: GetOperationResult Patch.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void getOperationResultPatch(com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .backupVaultOperationResults()
            .getWithResponse(
                "swaggerExample",
                "SampleResourceGroup",
                "YWUzNDFkMzQtZmM5OS00MmUyLWEzNDMtZGJkMDIxZjlmZjgzOzdmYzBiMzhmLTc2NmItNDM5NS05OWQ1LTVmOGEzNzg4MWQzNA==",
                Context.NONE);
    }
}
```

### BackupVaults_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.dataprotection.models.CheckNameAvailabilityRequest;

/** Samples for BackupVaults CheckNameAvailability. */
public final class BackupVaultsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/VaultCRUD/CheckBackupVaultsNameAvailability.json
     */
    /**
     * Sample code: Check BackupVaults name availability.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void checkBackupVaultsNameAvailability(
        com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .backupVaults()
            .checkNameAvailabilityWithResponse(
                "SampleResourceGroup",
                "westus",
                new CheckNameAvailabilityRequest()
                    .withName("swaggerExample")
                    .withType("Microsoft.DataProtection/BackupVaults"),
                Context.NONE);
    }
}
```

### BackupVaults_CreateOrUpdate

```java
import com.azure.resourcemanager.dataprotection.models.BackupVault;
import com.azure.resourcemanager.dataprotection.models.DppIdentityDetails;
import com.azure.resourcemanager.dataprotection.models.StorageSetting;
import com.azure.resourcemanager.dataprotection.models.StorageSettingStoreTypes;
import com.azure.resourcemanager.dataprotection.models.StorageSettingTypes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for BackupVaults CreateOrUpdate. */
public final class BackupVaultsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/VaultCRUD/PutBackupVault.json
     */
    /**
     * Sample code: Create BackupVault.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void createBackupVault(com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .backupVaults()
            .define("swaggerExample")
            .withRegion("WestUS")
            .withExistingResourceGroup("SampleResourceGroup")
            .withProperties(
                new BackupVault()
                    .withStorageSettings(
                        Arrays
                            .asList(
                                new StorageSetting()
                                    .withDatastoreType(StorageSettingStoreTypes.VAULT_STORE)
                                    .withType(StorageSettingTypes.LOCALLY_REDUNDANT))))
            .withTags(mapOf("key1", "val1"))
            .withIdentity(new DppIdentityDetails().withType("None"))
            .create();
    }

    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/VaultCRUD/PutBackupVaultWithMSI.json
     */
    /**
     * Sample code: Create BackupVault With MSI.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void createBackupVaultWithMSI(
        com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .backupVaults()
            .define("swaggerExample")
            .withRegion("WestUS")
            .withExistingResourceGroup("SampleResourceGroup")
            .withProperties(
                new BackupVault()
                    .withStorageSettings(
                        Arrays
                            .asList(
                                new StorageSetting()
                                    .withDatastoreType(StorageSettingStoreTypes.VAULT_STORE)
                                    .withType(StorageSettingTypes.LOCALLY_REDUNDANT))))
            .withTags(mapOf("key1", "val1"))
            .withIdentity(new DppIdentityDetails().withType("systemAssigned"))
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

### BackupVaults_Delete

```java
import com.azure.core.util.Context;

/** Samples for BackupVaults Delete. */
public final class BackupVaultsDeleteSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/VaultCRUD/DeleteBackupVault.json
     */
    /**
     * Sample code: Delete BackupVault.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void deleteBackupVault(com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager.backupVaults().deleteWithResponse("SampleResourceGroup", "swaggerExample", Context.NONE);
    }
}
```

### BackupVaults_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for BackupVaults GetByResourceGroup. */
public final class BackupVaultsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/VaultCRUD/GetBackupVault.json
     */
    /**
     * Sample code: Get BackupVault.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void getBackupVault(com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager.backupVaults().getByResourceGroupWithResponse("SampleResourceGroup", "swaggerExample", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/VaultCRUD/GetBackupVaultWithMSI.json
     */
    /**
     * Sample code: Get BackupVault With MSI.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void getBackupVaultWithMSI(com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager.backupVaults().getByResourceGroupWithResponse("SampleResourceGroup", "swaggerExample", Context.NONE);
    }
}
```

### BackupVaults_List

```java
import com.azure.core.util.Context;

/** Samples for BackupVaults List. */
public final class BackupVaultsListSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/VaultCRUD/GetBackupVaultsInSubscription.json
     */
    /**
     * Sample code: Get BackupVaults in Subscription.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void getBackupVaultsInSubscription(
        com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager.backupVaults().list(Context.NONE);
    }
}
```

### BackupVaults_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for BackupVaults ListByResourceGroup. */
public final class BackupVaultsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/VaultCRUD/GetBackupVaultsInResourceGroup.json
     */
    /**
     * Sample code: Get BackupVaults in ResourceGroup.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void getBackupVaultsInResourceGroup(
        com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager.backupVaults().listByResourceGroup("SampleResourceGroup", Context.NONE);
    }
}
```

### BackupVaults_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.dataprotection.models.BackupVaultResource;
import java.util.HashMap;
import java.util.Map;

/** Samples for BackupVaults Update. */
public final class BackupVaultsUpdateSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/VaultCRUD/PatchBackupVault.json
     */
    /**
     * Sample code: Patch BackupVault.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void patchBackupVault(com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        BackupVaultResource resource =
            manager
                .backupVaults()
                .getByResourceGroupWithResponse("SampleResourceGroup", "swaggerExample", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("newKey", "newVal")).apply();
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

### DataProtection_CheckFeatureSupport

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.dataprotection.models.FeatureType;
import com.azure.resourcemanager.dataprotection.models.FeatureValidationRequest;

/** Samples for DataProtection CheckFeatureSupport. */
public final class DataProtectionCheckFeatureSupportSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/CheckfeatureSupport.json
     */
    /**
     * Sample code: Check Azure Vm Backup Feature Support.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void checkAzureVmBackupFeatureSupport(
        com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .dataProtections()
            .checkFeatureSupportWithResponse(
                "WestUS", new FeatureValidationRequest().withFeatureType(FeatureType.DATA_SOURCE_TYPE), Context.NONE);
    }
}
```

### DataProtectionOperations_List

```java
import com.azure.core.util.Context;

/** Samples for DataProtectionOperations List. */
public final class DataProtectionOperationsListSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/Operations/List.json
     */
    /**
     * Sample code: Returns the list of supported REST operations.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void returnsTheListOfSupportedRESTOperations(
        com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager.dataProtectionOperations().list(Context.NONE);
    }
}
```

### ExportJobs_Trigger

```java
import com.azure.core.util.Context;

/** Samples for ExportJobs Trigger. */
public final class ExportJobsTriggerSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/JobCRUD/TriggerExportJobs.json
     */
    /**
     * Sample code: Trigger Export Jobs.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void triggerExportJobs(com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager.exportJobs().trigger("SwaggerTestRg", "NetSDKTestRsVault", Context.NONE);
    }
}
```

### ExportJobsOperationResult_Get

```java
import com.azure.core.util.Context;

/** Samples for ExportJobsOperationResult Get. */
public final class ExportJobsOperationResultGetSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/JobCRUD/GetExportJobsOperationResult.json
     */
    /**
     * Sample code: Get Export Jobs Operation Result.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void getExportJobsOperationResult(
        com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .exportJobsOperationResults()
            .getWithResponse(
                "SwaggerTestRg", "NetSDKTestRsVault", "00000000-0000-0000-0000-000000000000", Context.NONE);
    }
}
```

### Jobs_Get

```java
import com.azure.core.util.Context;

/** Samples for Jobs Get. */
public final class JobsGetSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/JobCRUD/GetJob.json
     */
    /**
     * Sample code: Get Job.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void getJob(com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .jobs()
            .getWithResponse("BugBash1", "BugBashVaultForCCYv11", "3c60cb49-63e8-4b21-b9bd-26277b3fdfae", Context.NONE);
    }
}
```

### Jobs_List

```java
import com.azure.core.util.Context;

/** Samples for Jobs List. */
public final class JobsListSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/JobCRUD/ListJobs.json
     */
    /**
     * Sample code: Get Jobs.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void getJobs(com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager.jobs().list("BugBash1", "BugBashVaultForCCYv11", Context.NONE);
    }
}
```

### OperationResult_Get

```java
import com.azure.core.util.Context;

/** Samples for OperationResult Get. */
public final class OperationResultGetSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/GetOperationResult.json
     */
    /**
     * Sample code: Get OperationResult.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void getOperationResult(com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .operationResults()
            .getWithResponse(
                "MjkxOTMyODMtYTE3My00YzJjLTg5NjctN2E4MDIxNDA3NjA2OzdjNGE2ZWRjLWJjMmItNDRkYi1hYzMzLWY1YzEwNzk5Y2EyOA==",
                "WestUS",
                Context.NONE);
    }
}
```

### OperationStatus_Get

```java
import com.azure.core.util.Context;

/** Samples for OperationStatus Get. */
public final class OperationStatusGetSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/GetOperationStatus.json
     */
    /**
     * Sample code: Get OperationStatus.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void getOperationStatus(com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .operationStatus()
            .getWithResponse(
                "WestUS",
                "MjkxOTMyODMtYTE3My00YzJjLTg5NjctN2E4MDIxNDA3NjA2OzdjNGE2ZWRjLWJjMmItNDRkYi1hYzMzLWY1YzEwNzk5Y2EyOA==",
                Context.NONE);
    }
}
```

### RecoveryPoints_Get

```java
import com.azure.core.util.Context;

/** Samples for RecoveryPoints Get. */
public final class RecoveryPointsGetSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/BackupInstanceOperations/GetRecoveryPoint.json
     */
    /**
     * Sample code: Get Recovery Point.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void getRecoveryPoint(com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .recoveryPoints()
            .getWithResponse(
                "PratikPrivatePreviewVault1",
                "000pikumar",
                "testInstance1",
                "7fb2cddd-c5b3-44f6-a0d9-db3c4f9d5f25",
                Context.NONE);
    }
}
```

### RecoveryPoints_List

```java
import com.azure.core.util.Context;

/** Samples for RecoveryPoints List. */
public final class RecoveryPointsListSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/BackupInstanceOperations/ListRecoveryPoints.json
     */
    /**
     * Sample code: List Recovery Points in a Vault.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void listRecoveryPointsInAVault(
        com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .recoveryPoints()
            .list("PratikPrivatePreviewVault1", "000pikumar", "testInstance1", null, null, Context.NONE);
    }
}
```

### ResourceGuards_Delete

```java
import com.azure.core.util.Context;

/** Samples for ResourceGuards Delete. */
public final class ResourceGuardsDeleteSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/ResourceGuardCRUD/DeleteResourceGuard.json
     */
    /**
     * Sample code: Delete ResourceGuard.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void deleteResourceGuard(com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager.resourceGuards().deleteWithResponse("SampleResourceGroup", "swaggerExample", Context.NONE);
    }
}
```

### ResourceGuards_GetBackupSecurityPinRequestsObjects

```java
import com.azure.core.util.Context;

/** Samples for ResourceGuards GetBackupSecurityPinRequestsObjects. */
public final class ResourceGuardsGetBackupSecurityPinRequestsObjectsSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/ResourceGuardCRUD/ListBackupSecurityPINRequests.json
     */
    /**
     * Sample code: List OperationsRequestObject.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void listOperationsRequestObject(
        com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .resourceGuards()
            .getBackupSecurityPinRequestsObjects("SampleResourceGroup", "swaggerExample", Context.NONE);
    }
}
```

### ResourceGuards_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ResourceGuards GetByResourceGroup. */
public final class ResourceGuardsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/ResourceGuardCRUD/GetResourceGuard.json
     */
    /**
     * Sample code: Get ResourceGuard.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void getResourceGuard(com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager.resourceGuards().getByResourceGroupWithResponse("SampleResourceGroup", "swaggerExample", Context.NONE);
    }
}
```

### ResourceGuards_GetDefaultBackupSecurityPinRequestsObject

```java
import com.azure.core.util.Context;

/** Samples for ResourceGuards GetDefaultBackupSecurityPinRequestsObject. */
public final class ResourceGuardsGetDefaultBackupSecurityPinRequestsObjectSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/ResourceGuardCRUD/GetDefaultBackupSecurityPINRequests.json
     */
    /**
     * Sample code: Get DefaultOperationsRequestObject.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void getDefaultOperationsRequestObject(
        com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .resourceGuards()
            .getDefaultBackupSecurityPinRequestsObjectWithResponse(
                "SampleResourceGroup", "swaggerExample", "default", Context.NONE);
    }
}
```

### ResourceGuards_GetDefaultDeleteProtectedItemRequestsObject

```java
import com.azure.core.util.Context;

/** Samples for ResourceGuards GetDefaultDeleteProtectedItemRequestsObject. */
public final class ResourceGuardsGetDefaultDeleteProtectedItemRequestsObjectSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/ResourceGuardCRUD/GetDefaultDeleteProtectedItemRequests.json
     */
    /**
     * Sample code: Get DefaultOperationsRequestObject.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void getDefaultOperationsRequestObject(
        com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .resourceGuards()
            .getDefaultDeleteProtectedItemRequestsObjectWithResponse(
                "SampleResourceGroup", "swaggerExample", "default", Context.NONE);
    }
}
```

### ResourceGuards_GetDefaultDeleteResourceGuardProxyRequestsObject

```java
import com.azure.core.util.Context;

/** Samples for ResourceGuards GetDefaultDeleteResourceGuardProxyRequestsObject. */
public final class ResourceGuardsGetDefaultDeleteResourceGuardProxyRequestsObjectSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/ResourceGuardCRUD/GetDefaultDeleteResourceGuardProxyRequests.json
     */
    /**
     * Sample code: Get DefaultOperationsRequestObject.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void getDefaultOperationsRequestObject(
        com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .resourceGuards()
            .getDefaultDeleteResourceGuardProxyRequestsObjectWithResponse(
                "SampleResourceGroup", "swaggerExample", "default", Context.NONE);
    }
}
```

### ResourceGuards_GetDefaultDisableSoftDeleteRequestsObject

```java
import com.azure.core.util.Context;

/** Samples for ResourceGuards GetDefaultDisableSoftDeleteRequestsObject. */
public final class ResourceGuardsGetDefaultDisableSoftDeleteRequestsObjectSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/ResourceGuardCRUD/GetDefaultDisableSoftDeleteRequests.json
     */
    /**
     * Sample code: Get DefaultOperationsRequestObject.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void getDefaultOperationsRequestObject(
        com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .resourceGuards()
            .getDefaultDisableSoftDeleteRequestsObjectWithResponse(
                "SampleResourceGroup", "swaggerExample", "default", Context.NONE);
    }
}
```

### ResourceGuards_GetDefaultUpdateProtectedItemRequestsObject

```java
import com.azure.core.util.Context;

/** Samples for ResourceGuards GetDefaultUpdateProtectedItemRequestsObject. */
public final class ResourceGuardsGetDefaultUpdateProtectedItemRequestsObjectSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/ResourceGuardCRUD/GetDefaultUpdateProtectedItemRequests.json
     */
    /**
     * Sample code: Get DefaultOperationsRequestObject.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void getDefaultOperationsRequestObject(
        com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .resourceGuards()
            .getDefaultUpdateProtectedItemRequestsObjectWithResponse(
                "SampleResourceGroup", "swaggerExample", "default", Context.NONE);
    }
}
```

### ResourceGuards_GetDefaultUpdateProtectionPolicyRequestsObject

```java
import com.azure.core.util.Context;

/** Samples for ResourceGuards GetDefaultUpdateProtectionPolicyRequestsObject. */
public final class ResourceGuardsGetDefaultUpdateProtectionPolicyRequestsObjectSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/ResourceGuardCRUD/GetDefaultUpdateProtectionPolicyRequests.json
     */
    /**
     * Sample code: Get DefaultOperationsRequestObject.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void getDefaultOperationsRequestObject(
        com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .resourceGuards()
            .getDefaultUpdateProtectionPolicyRequestsObjectWithResponse(
                "SampleResourceGroup", "swaggerExample", "default", Context.NONE);
    }
}
```

### ResourceGuards_GetDeleteProtectedItemRequestsObjects

```java
import com.azure.core.util.Context;

/** Samples for ResourceGuards GetDeleteProtectedItemRequestsObjects. */
public final class ResourceGuardsGetDeleteProtectedItemRequestsObjectsSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/ResourceGuardCRUD/ListDeleteProtectedItemRequests.json
     */
    /**
     * Sample code: List OperationsRequestObject.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void listOperationsRequestObject(
        com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .resourceGuards()
            .getDeleteProtectedItemRequestsObjects("SampleResourceGroup", "swaggerExample", Context.NONE);
    }
}
```

### ResourceGuards_GetDeleteResourceGuardProxyRequestsObjects

```java
import com.azure.core.util.Context;

/** Samples for ResourceGuards GetDeleteResourceGuardProxyRequestsObjects. */
public final class ResourceGuardsGetDeleteResourceGuardProxyRequestsObjectsSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/ResourceGuardCRUD/ListDeleteResourceGuardProxyRequests.json
     */
    /**
     * Sample code: List OperationsRequestObject.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void listOperationsRequestObject(
        com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .resourceGuards()
            .getDeleteResourceGuardProxyRequestsObjects("SampleResourceGroup", "swaggerExample", Context.NONE);
    }
}
```

### ResourceGuards_GetDisableSoftDeleteRequestsObjects

```java
import com.azure.core.util.Context;

/** Samples for ResourceGuards GetDisableSoftDeleteRequestsObjects. */
public final class ResourceGuardsGetDisableSoftDeleteRequestsObjectsSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/ResourceGuardCRUD/ListDisableSoftDeleteRequests.json
     */
    /**
     * Sample code: List OperationsRequestObject.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void listOperationsRequestObject(
        com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .resourceGuards()
            .getDisableSoftDeleteRequestsObjects("SampleResourceGroup", "swaggerExample", Context.NONE);
    }
}
```

### ResourceGuards_GetUpdateProtectedItemRequestsObjects

```java
import com.azure.core.util.Context;

/** Samples for ResourceGuards GetUpdateProtectedItemRequestsObjects. */
public final class ResourceGuardsGetUpdateProtectedItemRequestsObjectsSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/ResourceGuardCRUD/ListUpdateProtectedItemRequests.json
     */
    /**
     * Sample code: List OperationsRequestObject.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void listOperationsRequestObject(
        com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .resourceGuards()
            .getUpdateProtectedItemRequestsObjects("SampleResourceGroup", "swaggerExample", Context.NONE);
    }
}
```

### ResourceGuards_GetUpdateProtectionPolicyRequestsObjects

```java
import com.azure.core.util.Context;

/** Samples for ResourceGuards GetUpdateProtectionPolicyRequestsObjects. */
public final class ResourceGuardsGetUpdateProtectionPolicyRequestsObjectsSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/ResourceGuardCRUD/ListUpdateProtectionPolicyRequests.json
     */
    /**
     * Sample code: List OperationsRequestObject.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void listOperationsRequestObject(
        com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .resourceGuards()
            .getUpdateProtectionPolicyRequestsObjects("SampleResourceGroup", "swaggerExample", Context.NONE);
    }
}
```

### ResourceGuards_List

```java
import com.azure.core.util.Context;

/** Samples for ResourceGuards List. */
public final class ResourceGuardsListSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/ResourceGuardCRUD/GetResourceGuardsInSubscription.json
     */
    /**
     * Sample code: Get ResourceGuards in Subscription.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void getResourceGuardsInSubscription(
        com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager.resourceGuards().list(Context.NONE);
    }
}
```

### ResourceGuards_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ResourceGuards ListByResourceGroup. */
public final class ResourceGuardsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/ResourceGuardCRUD/GetResourceGuardsInResourceGroup.json
     */
    /**
     * Sample code: Get ResourceGuards in ResourceGroup.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void getResourceGuardsInResourceGroup(
        com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager.resourceGuards().listByResourceGroup("SampleResourceGroup", Context.NONE);
    }
}
```

### ResourceGuards_Patch

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.dataprotection.models.ResourceGuardResource;
import java.util.HashMap;
import java.util.Map;

/** Samples for ResourceGuards Patch. */
public final class ResourceGuardsPatchSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/ResourceGuardCRUD/PatchResourceGuard.json
     */
    /**
     * Sample code: Patch ResourceGuard.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void patchResourceGuard(com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        ResourceGuardResource resource =
            manager
                .resourceGuards()
                .getByResourceGroupWithResponse("SampleResourceGroup", "swaggerExample", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("newKey", "newVal")).apply();
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

### ResourceGuards_Put

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for ResourceGuards Put. */
public final class ResourceGuardsPutSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/ResourceGuardCRUD/PutResourceGuard.json
     */
    /**
     * Sample code: Create ResourceGuard.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void createResourceGuard(com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .resourceGuards()
            .define("swaggerExample")
            .withRegion("WestUS")
            .withExistingResourceGroup("SampleResourceGroup")
            .withTags(mapOf("key1", "val1"))
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

### RestorableTimeRanges_Find

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.dataprotection.models.AzureBackupFindRestorableTimeRangesRequest;
import com.azure.resourcemanager.dataprotection.models.RestoreSourceDataStoreType;

/** Samples for RestorableTimeRanges Find. */
public final class RestorableTimeRangesFindSamples {
    /*
     * x-ms-original-file: specification/dataprotection/resource-manager/Microsoft.DataProtection/stable/2021-07-01/examples/BackupInstanceOperations/FindRestorableTimeRanges.json
     */
    /**
     * Sample code: Find Restorable Time Ranges.
     *
     * @param manager Entry point to DataProtectionManager.
     */
    public static void findRestorableTimeRanges(
        com.azure.resourcemanager.dataprotection.DataProtectionManager manager) {
        manager
            .restorableTimeRanges()
            .findWithResponse(
                "ZBlobBackupVaultBVTD3",
                "Blob-Backup",
                "zblobbackuptestsa58",
                new AzureBackupFindRestorableTimeRangesRequest()
                    .withSourceDataStoreType(RestoreSourceDataStoreType.OPERATIONAL_STORE)
                    .withStartTime("2020-10-17T23:28:17.6829685Z")
                    .withEndTime("2021-02-24T00:35:17.6829685Z"),
                Context.NONE);
    }
}
```

