# Code snippets and samples


## CloudEndpoints

- [AfsShareMetadataCertificatePublicKeys](#cloudendpoints_afssharemetadatacertificatepublickeys)
- [Create](#cloudendpoints_create)
- [Delete](#cloudendpoints_delete)
- [Get](#cloudendpoints_get)
- [ListBySyncGroup](#cloudendpoints_listbysyncgroup)
- [PostBackup](#cloudendpoints_postbackup)
- [PostRestore](#cloudendpoints_postrestore)
- [PreBackup](#cloudendpoints_prebackup)
- [PreRestore](#cloudendpoints_prerestore)
- [RestoreHeartbeat](#cloudendpoints_restoreheartbeat)
- [TriggerChangeDetection](#cloudendpoints_triggerchangedetection)

## OperationStatusOperation

- [Get](#operationstatusoperation_get)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [Create](#privateendpointconnections_create)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [ListByStorageSyncService](#privateendpointconnections_listbystoragesyncservice)

## PrivateLinkResources

- [ListByStorageSyncService](#privatelinkresources_listbystoragesyncservice)

## RegisteredServers

- [Create](#registeredservers_create)
- [Delete](#registeredservers_delete)
- [Get](#registeredservers_get)
- [ListByStorageSyncService](#registeredservers_listbystoragesyncservice)
- [TriggerRollover](#registeredservers_triggerrollover)
- [Update](#registeredservers_update)

## ResourceProvider

- [LocationOperationStatus](#resourceprovider_locationoperationstatus)

## ServerEndpoints

- [Create](#serverendpoints_create)
- [Delete](#serverendpoints_delete)
- [Get](#serverendpoints_get)
- [ListBySyncGroup](#serverendpoints_listbysyncgroup)
- [RecallAction](#serverendpoints_recallaction)
- [Update](#serverendpoints_update)

## StorageSyncServices

- [CheckNameAvailability](#storagesyncservices_checknameavailability)
- [Create](#storagesyncservices_create)
- [Delete](#storagesyncservices_delete)
- [GetByResourceGroup](#storagesyncservices_getbyresourcegroup)
- [List](#storagesyncservices_list)
- [ListByResourceGroup](#storagesyncservices_listbyresourcegroup)
- [Update](#storagesyncservices_update)

## SyncGroups

- [Create](#syncgroups_create)
- [Delete](#syncgroups_delete)
- [Get](#syncgroups_get)
- [ListByStorageSyncService](#syncgroups_listbystoragesyncservice)

## Workflows

- [Abort](#workflows_abort)
- [Get](#workflows_get)
- [ListByStorageSyncService](#workflows_listbystoragesyncservice)
### CloudEndpoints_AfsShareMetadataCertificatePublicKeys

```java
/**
 * Samples for CloudEndpoints AfsShareMetadataCertificatePublicKeys.
 */
public final class CloudEndpointsAfsShareMetadataCertificatePublicKeysSamples {
    /*
     * x-ms-original-file: 2022-09-01/CloudEndpoints_AfsShareMetadataCertificatePublicKeys.json
     */
    /**
     * Sample code: CloudEndpoints_AfsShareMetadataCertificatePublicKeys.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void cloudEndpointsAfsShareMetadataCertificatePublicKeys(
        com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.cloudEndpoints()
            .afsShareMetadataCertificatePublicKeysWithResponse("SampleResourceGroup_1", "SampleStorageSyncService_1",
                "SampleSyncGroup_1", "SampleCloudEndpoint_1", com.azure.core.util.Context.NONE);
    }
}
```

### CloudEndpoints_Create

```java
/**
 * Samples for CloudEndpoints Create.
 */
public final class CloudEndpointsCreateSamples {
    /*
     * x-ms-original-file: 2022-09-01/CloudEndpoints_Create.json
     */
    /**
     * Sample code: CloudEndpoints_Create.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void cloudEndpointsCreate(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.cloudEndpoints()
            .define("SampleCloudEndpoint_1")
            .withExistingSyncGroup("SampleResourceGroup_1", "SampleStorageSyncService_1", "SampleSyncGroup_1")
            .withStorageAccountResourceId(
                "/subscriptions/744f4d70-6d17-4921-8970-a765d14f763f/resourceGroups/tminienv59svc/providers/Microsoft.Storage/storageAccounts/tminienv59storage")
            .withAzureFileShareName("cvcloud-afscv-0719-058-a94a1354-a1fd-4e9a-9a50-919fad8c4ba4")
            .withStorageAccountTenantId("\"72f988bf-86f1-41af-91ab-2d7cd011db47\"")
            .withFriendlyName("ankushbsubscriptionmgmtmab")
            .create();
    }
}
```

### CloudEndpoints_Delete

```java
/**
 * Samples for CloudEndpoints Delete.
 */
public final class CloudEndpointsDeleteSamples {
    /*
     * x-ms-original-file: 2022-09-01/CloudEndpoints_Delete.json
     */
    /**
     * Sample code: CloudEndpoints_Delete.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void cloudEndpointsDelete(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.cloudEndpoints()
            .delete("SampleResourceGroup_1", "SampleStorageSyncService_1", "SampleSyncGroup_1", "SampleCloudEndpoint_1",
                com.azure.core.util.Context.NONE);
    }
}
```

### CloudEndpoints_Get

```java
/**
 * Samples for CloudEndpoints Get.
 */
public final class CloudEndpointsGetSamples {
    /*
     * x-ms-original-file: 2022-09-01/CloudEndpoints_Get.json
     */
    /**
     * Sample code: CloudEndpoints_Get.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void cloudEndpointsGet(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.cloudEndpoints()
            .getWithResponse("SampleResourceGroup_1", "SampleStorageSyncService_1", "SampleSyncGroup_1",
                "SampleCloudEndpoint_1", com.azure.core.util.Context.NONE);
    }
}
```

### CloudEndpoints_ListBySyncGroup

```java
/**
 * Samples for CloudEndpoints ListBySyncGroup.
 */
public final class CloudEndpointsListBySyncGroupSamples {
    /*
     * x-ms-original-file: 2022-09-01/CloudEndpoints_ListBySyncGroup.json
     */
    /**
     * Sample code: CloudEndpoints_ListBySyncGroup.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void cloudEndpointsListBySyncGroup(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.cloudEndpoints()
            .listBySyncGroup("SampleResourceGroup_1", "SampleStorageSyncService_1", "SampleSyncGroup_1",
                com.azure.core.util.Context.NONE);
    }
}
```

### CloudEndpoints_PostBackup

```java
import com.azure.resourcemanager.storagesync.models.BackupRequest;

/**
 * Samples for CloudEndpoints PostBackup.
 */
public final class CloudEndpointsPostBackupSamples {
    /*
     * x-ms-original-file: 2022-09-01/CloudEndpoints_PostBackup.json
     */
    /**
     * Sample code: CloudEndpoints_PostBackup.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void cloudEndpointsPostBackup(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.cloudEndpoints()
            .postBackup("SampleResourceGroup_1", "SampleStorageSyncService_1", "SampleSyncGroup_1",
                "SampleCloudEndpoint_1",
                new BackupRequest()
                    .withAzureFileShare("https://sampleserver.file.core.test-cint.azure-test.net/sampleFileShare"),
                com.azure.core.util.Context.NONE);
    }
}
```

### CloudEndpoints_PostRestore

```java
import com.azure.resourcemanager.storagesync.models.PostRestoreRequest;
import com.azure.resourcemanager.storagesync.models.RestoreFileSpec;
import java.util.Arrays;

/**
 * Samples for CloudEndpoints PostRestore.
 */
public final class CloudEndpointsPostRestoreSamples {
    /*
     * x-ms-original-file: 2022-09-01/CloudEndpoints_PostRestore.json
     */
    /**
     * Sample code: CloudEndpoints_PostRestore.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void cloudEndpointsPostRestore(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.cloudEndpoints()
            .postRestore("SampleResourceGroup_1", "SampleStorageSyncService_1", "SampleSyncGroup_1",
                "SampleCloudEndpoint_1",
                new PostRestoreRequest()
                    .withAzureFileShareUri(
                        "https://hfsazbackupdevintncus2.file.core.test-cint.azure-test.net/sampleFileShare")
                    .withStatus("Succeeded")
                    .withSourceAzureFileShareUri(
                        "https://hfsazbackupdevintncus2.file.core.test-cint.azure-test.net/sampleFileShare")
                    .withRestoreFileSpec(Arrays.asList(new RestoreFileSpec().withPath("text1.txt").withIsdir(false),
                        new RestoreFileSpec().withPath("MyDir").withIsdir(true),
                        new RestoreFileSpec().withPath("MyDir/SubDir").withIsdir(false),
                        new RestoreFileSpec().withPath("MyDir/SubDir/File1.pdf").withIsdir(false))),
                com.azure.core.util.Context.NONE);
    }
}
```

### CloudEndpoints_PreBackup

```java
import com.azure.resourcemanager.storagesync.models.BackupRequest;

/**
 * Samples for CloudEndpoints PreBackup.
 */
public final class CloudEndpointsPreBackupSamples {
    /*
     * x-ms-original-file: 2022-09-01/CloudEndpoints_PreBackup.json
     */
    /**
     * Sample code: CloudEndpoints_PreBackup.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void cloudEndpointsPreBackup(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.cloudEndpoints()
            .preBackup("SampleResourceGroup_1", "SampleStorageSyncService_1", "SampleSyncGroup_1",
                "SampleCloudEndpoint_1",
                new BackupRequest()
                    .withAzureFileShare("https://sampleserver.file.core.test-cint.azure-test.net/sampleFileShare"),
                com.azure.core.util.Context.NONE);
    }
}
```

### CloudEndpoints_PreRestore

```java
import com.azure.resourcemanager.storagesync.models.PreRestoreRequest;
import com.azure.resourcemanager.storagesync.models.RestoreFileSpec;
import java.util.Arrays;

/**
 * Samples for CloudEndpoints PreRestore.
 */
public final class CloudEndpointsPreRestoreSamples {
    /*
     * x-ms-original-file: 2022-09-01/CloudEndpoints_PreRestore.json
     */
    /**
     * Sample code: CloudEndpoints_PreRestore.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void cloudEndpointsPreRestore(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.cloudEndpoints()
            .preRestore("SampleResourceGroup_1", "SampleStorageSyncService_1", "SampleSyncGroup_1",
                "SampleCloudEndpoint_1",
                new PreRestoreRequest()
                    .withAzureFileShareUri(
                        "https://hfsazbackupdevintncus2.file.core.test-cint.azure-test.net/sampleFileShare")
                    .withRestoreFileSpec(Arrays.asList(new RestoreFileSpec().withPath("text1.txt").withIsdir(false),
                        new RestoreFileSpec().withPath("MyDir").withIsdir(true),
                        new RestoreFileSpec().withPath("MyDir/SubDir").withIsdir(false),
                        new RestoreFileSpec().withPath("MyDir/SubDir/File1.pdf").withIsdir(false))),
                com.azure.core.util.Context.NONE);
    }
}
```

### CloudEndpoints_RestoreHeartbeat

```java
/**
 * Samples for CloudEndpoints RestoreHeartbeat.
 */
public final class CloudEndpointsRestoreHeartbeatSamples {
    /*
     * x-ms-original-file: 2022-09-01/CloudEndpoints_RestoreHeatbeat.json
     */
    /**
     * Sample code: CloudEndpoints_restoreheartbeat.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void
        cloudEndpointsRestoreheartbeat(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.cloudEndpoints()
            .restoreHeartbeatWithResponse("SampleResourceGroup_1", "SampleStorageSyncService_1", "SampleSyncGroup_1",
                "SampleCloudEndpoint_1", com.azure.core.util.Context.NONE);
    }
}
```

### CloudEndpoints_TriggerChangeDetection

```java
import com.azure.resourcemanager.storagesync.models.ChangeDetectionMode;
import com.azure.resourcemanager.storagesync.models.TriggerChangeDetectionParameters;

/**
 * Samples for CloudEndpoints TriggerChangeDetection.
 */
public final class CloudEndpointsTriggerChangeDetectionSamples {
    /*
     * x-ms-original-file: 2022-09-01/CloudEndpoints_TriggerChangeDetection.json
     */
    /**
     * Sample code: CloudEndpoints_TriggerChangeDetection.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void
        cloudEndpointsTriggerChangeDetection(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.cloudEndpoints()
            .triggerChangeDetection("SampleResourceGroup_1", "SampleStorageSyncService_1", "SampleSyncGroup_1",
                "SampleCloudEndpoint_1", new TriggerChangeDetectionParameters().withDirectoryPath("NewDirectory")
                    .withChangeDetectionMode(ChangeDetectionMode.RECURSIVE),
                com.azure.core.util.Context.NONE);
    }
}
```

### OperationStatusOperation_Get

```java
/**
 * Samples for OperationStatusOperation Get.
 */
public final class OperationStatusOperationGetSamples {
    /*
     * x-ms-original-file: 2022-09-01/OperationStatus_Get.json
     */
    /**
     * Sample code: OperationStatus_Get.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void operationStatusGet(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.operationStatusOperations()
            .getWithResponse("SampleResourceGroup_1", "westus", "828219ea-083e-48b5-89ea-8fd9991b2e75",
                "14b50e24-f68d-4b29-a882-38be9dfb8bd1", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2022-09-01/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void operationsList(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Create

```java
import com.azure.resourcemanager.storagesync.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.storagesync.models.PrivateLinkServiceConnectionState;

/**
 * Samples for PrivateEndpointConnections Create.
 */
public final class PrivateEndpointConnectionsCreateSamples {
    /*
     * x-ms-original-file: 2022-09-01/PrivateEndpointConnections_Create.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Create.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void
        privateEndpointConnectionsCreate(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.privateEndpointConnections()
            .define("{privateEndpointConnectionName}")
            .withExistingStorageSyncService("res7687", "sss2527")
            .withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState().withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                    .withDescription("Auto-Approved"))
            .create();
    }
}
```

### PrivateEndpointConnections_Delete

```java
/**
 * Samples for PrivateEndpointConnections Delete.
 */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: 2022-09-01/PrivateEndpointConnections_Delete.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Delete.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void
        privateEndpointConnectionsDelete(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.privateEndpointConnections()
            .delete("res6977", "sss2527", "{privateEndpointConnectionName}", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
/**
 * Samples for PrivateEndpointConnections Get.
 */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: 2022-09-01/PrivateEndpointConnections_Get.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Get.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void privateEndpointConnectionsGet(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.privateEndpointConnections()
            .getWithResponse("res6977", "sss2527", "{privateEndpointConnectionName}", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_ListByStorageSyncService

```java
/**
 * Samples for PrivateEndpointConnections ListByStorageSyncService.
 */
public final class PrivateEndpointConnectionsListByStorageSyncServiceSamples {
    /*
     * x-ms-original-file: 2022-09-01/PrivateEndpointConnections_ListByStorageSyncService.json
     */
    /**
     * Sample code: PrivateEndpointConnections_ListByStorageSyncService.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void privateEndpointConnectionsListByStorageSyncService(
        com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.privateEndpointConnections()
            .listByStorageSyncService("res6977", "sss2527", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_ListByStorageSyncService

```java
/**
 * Samples for PrivateLinkResources ListByStorageSyncService.
 */
public final class PrivateLinkResourcesListByStorageSyncServiceSamples {
    /*
     * x-ms-original-file: 2022-09-01/PrivateLinkResources_List.json
     */
    /**
     * Sample code: PrivateLinkResources_List.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void privateLinkResourcesList(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.privateLinkResources()
            .listByStorageSyncServiceWithResponse("res6977", "sss2527", com.azure.core.util.Context.NONE);
    }
}
```

### RegisteredServers_Create

```java
/**
 * Samples for RegisteredServers Create.
 */
public final class RegisteredServersCreateSamples {
    /*
     * x-ms-original-file: 2022-09-01/RegisteredServers_Create.json
     */
    /**
     * Sample code: RegisteredServers_Create.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void registeredServersCreate(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.registeredServers()
            .define("080d4133-bdb5-40a0-96a0-71a6057bfe9a")
            .withExistingStorageSyncService("SampleResourceGroup_1", "SampleStorageSyncService_1")
            .withServerCertificate(
                "MIIDFjCCAf6gAwIBAgIQQS+DS8uhc4VNzUkTw7wbRjANBgkqhkiG9w0BAQ0FADAzMTEwLwYDVQQDEyhhbmt1c2hiLXByb2QzLnJlZG1vbmQuY29ycC5taWNyb3NvZnQuY29tMB4XDTE3MDgwMzE3MDQyNFoXDTE4MDgwNDE3MDQyNFowMzExMC8GA1UEAxMoYW5rdXNoYi1wcm9kMy5yZWRtb25kLmNvcnAubWljcm9zb2Z0LmNvbTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALDRvV4gmsIy6jGDPiHsXmvgVP749NNP7DopdlbHaNhjFmYINHl0uWylyaZmgJrROt2mnxN/zEyJtGnqYHlzUr4xvGq/qV5pqgdB9tag/sw9i22gfe9PRZ0FmSOZnXMbLYgLiDFqLtut5gHcOuWMj03YnkfoBEKlFBxWbagvW2yxz/Sxi9OVSJOKCaXra0RpcIHrO/KFl6ho2eE1/7Ykmfa8hZvSdoPd5gHdLiQcMB/pxq+mWp1fI6c8vFZoDu7Atn+NXTzYPKUxKzaisF12TsaKpohUsJpbB3Wocb0F5frn614D2pg14ERB5otjAMWw1m65csQWPI6dP8KIYe0+QPkCAwEAAaMmMCQwIgYDVR0lAQH/BBgwFgYIKwYBBQUHAwIGCisGAQQBgjcKAwwwDQYJKoZIhvcNAQENBQADggEBAA4RhVIBkw34M1RwakJgHvtjsOFxF1tVQA941NtLokx1l2Z8+GFQkcG4xpZSt+UN6wLerdCbnNhtkCErWUDeaT0jxk4g71Ofex7iM04crT4iHJr8mi96/XnhnkTUs+GDk12VgdeeNEczMZz+8Mxw9dJ5NCnYgTwO0SzGlclRsDvjzkLo8rh2ZG6n/jKrEyNXXo+hOqhupij0QbRP2Tvexdfw201kgN1jdZify8XzJ8Oi0bTS0KpJf2pNPOlooK2bjMUei9ANtEdXwwfVZGWvVh6tJjdv6k14wWWJ1L7zhA1IIVb1J+sQUzJji5iX0DrezjTz1Fg+gAzITaA/WsuujlM=")
            .withAgentVersion("1.0.277.0")
            .withServerOSVersion("10.0.14393.0")
            .withServerRole("Standalone")
            .withServerId("080d4133-bdb5-40a0-96a0-71a6057bfe9a")
            .withFriendlyName("afscv-2304-139")
            .withApplicationId("00000000-0000-0000-0000-000000000001")
            .withIdentity(true)
            .create();
    }
}
```

### RegisteredServers_Delete

```java
/**
 * Samples for RegisteredServers Delete.
 */
public final class RegisteredServersDeleteSamples {
    /*
     * x-ms-original-file: 2022-09-01/RegisteredServers_Delete.json
     */
    /**
     * Sample code: RegisteredServers_Delete.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void registeredServersDelete(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.registeredServers()
            .delete("SampleResourceGroup_1", "SampleStorageSyncService_1", "41166691-ab03-43e9-ab3e-0330eda162ac",
                com.azure.core.util.Context.NONE);
    }
}
```

### RegisteredServers_Get

```java
/**
 * Samples for RegisteredServers Get.
 */
public final class RegisteredServersGetSamples {
    /*
     * x-ms-original-file: 2022-09-01/RegisteredServers_Get.json
     */
    /**
     * Sample code: RegisteredServers_Get.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void registeredServersGet(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.registeredServers()
            .getWithResponse("SampleResourceGroup_1", "SampleStorageSyncService_1",
                "080d4133-bdb5-40a0-96a0-71a6057bfe9a", com.azure.core.util.Context.NONE);
    }
}
```

### RegisteredServers_ListByStorageSyncService

```java
/**
 * Samples for RegisteredServers ListByStorageSyncService.
 */
public final class RegisteredServersListByStorageSyncServiceSamples {
    /*
     * x-ms-original-file: 2022-09-01/RegisteredServers_ListByStorageSyncService.json
     */
    /**
     * Sample code: RegisteredServers_ListByStorageSyncService.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void
        registeredServersListByStorageSyncService(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.registeredServers()
            .listByStorageSyncService("SampleResourceGroup_1", "SampleStorageSyncService_1",
                com.azure.core.util.Context.NONE);
    }
}
```

### RegisteredServers_TriggerRollover

```java
import com.azure.resourcemanager.storagesync.models.TriggerRolloverRequest;

/**
 * Samples for RegisteredServers TriggerRollover.
 */
public final class RegisteredServersTriggerRolloverSamples {
    /*
     * x-ms-original-file: 2022-09-01/RegisteredServers_TriggerRollover.json
     */
    /**
     * Sample code: RegisteredServers_triggerRollover.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void
        registeredServersTriggerRollover(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.registeredServers()
            .triggerRollover("SampleResourceGroup_1", "SampleStorageSyncService_1",
                "d166ca76-dad2-49df-b409-12345642d730",
                new TriggerRolloverRequest().withServerCertificate(
                    "\"MIIDFjCCAf6gAwIBAgIQQS+DS8uhc4VNzUkTw7wbRjANBgkqhkiG9w0BAQ0FADAzMTEwLwYDVQQDEyhhbmt1c2hiLXByb2QzLnJlZG1vbmQuY29ycC5taWNyb3NvZnQuY29tMB4XDTE3MDgwMzE3MDQyNFoXDTE4MDgwNDE3MDQyNFowMzExMC8GA1UEAxMoYW5rdXNoYi1wcm9kMy5yZWRtb25kLmNvcnAubWljcm9zb2Z0LmNvbTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALDRvV4gmsIy6jGDPiHsXmvgVP749NNP7DopdlbHaNhjFmYINHl0uWylyaZmgJrROt2mnxN/zEyJtGnqYHlzUr4xvGq/qV5pqgdB9tag/sw9i22gfe9PRZ0FmSOZnXMbLYgLiDFqLtut5gHcOuWMj03YnkfoBEKlFBxWbagvW2yxz/Sxi9OVSJOKCaXra0RpcIHrO/KFl6ho2eE1/7Ykmfa8hZvSdoPd5gHdLiQcMB/pxq+mWp1fI6c8vFZoDu7Atn+NXTzYPKUxKzaisF12TsaKpohUsJpbB3Wocb0F5frn614D2pg14ERB5otjAMWw1m65csQWPI6dP8KIYe0+QPkCAwEAAaMmMCQwIgYDVR0lAQH/BBgwFgYIKwYBBQUHAwIGCisGAQQBgjcKAwwwDQYJKoZIhvcNAQENBQADggEBAA4RhVIBkw34M1RwakJgHvtjsOFxF1tVQA941NtLokx1l2Z8+GFQkcG4xpZSt+UN6wLerdCbnNhtkCErWUDeaT0jxk4g71Ofex7iM04crT4iHJr8mi96/XnhnkTUs+GDk12VgdeeNEczMZz+8Mxw9dJ5NCnYgTwO0SzGlclRsDvjzkLo8rh2ZG6n/jKrEyNXXo+hOqhupij0QbRP2Tvexdfw201kgN1jdZify8XzJ8Oi0bTS0KpJf2pNPOlooK2bjMUei9ANtEdXwwfVZGWvVh6tJjdv6k14wWWJ1L7zhA1IIVb1J+sQUzJji5iX0DrezjTz1Fg+gAzITaA/WsuujlM=\""),
                com.azure.core.util.Context.NONE);
    }
}
```

### RegisteredServers_Update

```java
import com.azure.resourcemanager.storagesync.models.RegisteredServer;

/**
 * Samples for RegisteredServers Update.
 */
public final class RegisteredServersUpdateSamples {
    /*
     * x-ms-original-file: 2022-09-01/RegisteredServers_Update.json
     */
    /**
     * Sample code: RegisteredServers_Update.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void registeredServersUpdate(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        RegisteredServer resource = manager.registeredServers()
            .getWithResponse("SampleResourceGroup_1", "SampleStorageSyncService_1",
                "080d4133-bdb5-40a0-96a0-71a6057bfe9a", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withIdentity(true).withApplicationId("120d4132-bcd5-40a0-96a0-71a6057ebf0c").apply();
    }
}
```

### ResourceProvider_LocationOperationStatus

```java
/**
 * Samples for ResourceProvider LocationOperationStatus.
 */
public final class ResourceProviderLocationOperationStatusSamples {
    /*
     * x-ms-original-file: 2022-09-01/LocationOperationStatus_Get.json
     */
    /**
     * Sample code: LocationOperationStatus.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void locationOperationStatus(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.resourceProviders()
            .locationOperationStatusWithResponse("westus",
                "eyJwYXJ0aXRpb25JZCI6ImE1ZDNiMDU4LTYwN2MtNDI0Ny05Y2FmLWJlZmU4NGQ0ZDU0NyIsIndvcmtmbG93SWQiOiJjYzg1MTY2YS0xMjI2LTQ4MGYtYWM5ZC1jMmRhNTVmY2M2ODYiLCJ3b3JrZmxvd09wZXJhdGlvbklkIjoiOTdmODU5ZTAtOGY1MC00ZTg4LWJkZDEtNWZlYzgwYTVlYzM0tui=",
                com.azure.core.util.Context.NONE);
    }
}
```

### ServerEndpoints_Create

```java
import com.azure.resourcemanager.storagesync.models.FeatureStatus;
import com.azure.resourcemanager.storagesync.models.InitialDownloadPolicy;
import com.azure.resourcemanager.storagesync.models.InitialUploadPolicy;
import com.azure.resourcemanager.storagesync.models.LocalCacheMode;

/**
 * Samples for ServerEndpoints Create.
 */
public final class ServerEndpointsCreateSamples {
    /*
     * x-ms-original-file: 2022-09-01/ServerEndpoints_Create.json
     */
    /**
     * Sample code: ServerEndpoints_Create.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void serverEndpointsCreate(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.serverEndpoints()
            .define("SampleServerEndpoint_1")
            .withExistingSyncGroup("SampleResourceGroup_1", "SampleStorageSyncService_1", "SampleSyncGroup_1")
            .withServerLocalPath("D:\\SampleServerEndpoint_1")
            .withCloudTiering(FeatureStatus.OFF)
            .withVolumeFreeSpacePercent(100)
            .withTierFilesOlderThanDays(0)
            .withServerResourceId(
                "/subscriptions/52b8da2f-61e0-4a1f-8dde-336911f367fb/resourceGroups/SampleResourceGroup_1/providers/Microsoft.StorageSync/storageSyncServices/SampleStorageSyncService_1/registeredServers/080d4133-bdb5-40a0-96a0-71a6057bfe9a")
            .withOfflineDataTransfer(FeatureStatus.ON)
            .withOfflineDataTransferShareName("myfileshare")
            .withInitialDownloadPolicy(InitialDownloadPolicy.NAMESPACE_THEN_MODIFIED_FILES)
            .withLocalCacheMode(LocalCacheMode.UPDATE_LOCALLY_CACHED_FILES)
            .withInitialUploadPolicy(InitialUploadPolicy.SERVER_AUTHORITATIVE)
            .create();
    }
}
```

### ServerEndpoints_Delete

```java
/**
 * Samples for ServerEndpoints Delete.
 */
public final class ServerEndpointsDeleteSamples {
    /*
     * x-ms-original-file: 2022-09-01/ServerEndpoints_Delete.json
     */
    /**
     * Sample code: ServerEndpoints_Delete.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void serverEndpointsDelete(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.serverEndpoints()
            .delete("SampleResourceGroup_1", "SampleStorageSyncService_1", "SampleSyncGroup_1",
                "SampleServerEndpoint_1", com.azure.core.util.Context.NONE);
    }
}
```

### ServerEndpoints_Get

```java
/**
 * Samples for ServerEndpoints Get.
 */
public final class ServerEndpointsGetSamples {
    /*
     * x-ms-original-file: 2022-09-01/ServerEndpoints_Get.json
     */
    /**
     * Sample code: ServerEndpoints_Get.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void serverEndpointsGet(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.serverEndpoints()
            .getWithResponse("SampleResourceGroup_1", "SampleStorageSyncService_1", "SampleSyncGroup_1",
                "SampleServerEndpoint_1", com.azure.core.util.Context.NONE);
    }
}
```

### ServerEndpoints_ListBySyncGroup

```java
/**
 * Samples for ServerEndpoints ListBySyncGroup.
 */
public final class ServerEndpointsListBySyncGroupSamples {
    /*
     * x-ms-original-file: 2022-09-01/ServerEndpoints_ListBySyncGroup.json
     */
    /**
     * Sample code: ServerEndpoints_ListBySyncGroup.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void
        serverEndpointsListBySyncGroup(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.serverEndpoints()
            .listBySyncGroup("SampleResourceGroup_1", "SampleStorageSyncService_1", "SampleSyncGroup_1",
                com.azure.core.util.Context.NONE);
    }
}
```

### ServerEndpoints_RecallAction

```java
import com.azure.resourcemanager.storagesync.models.RecallActionParameters;

/**
 * Samples for ServerEndpoints RecallAction.
 */
public final class ServerEndpointsRecallActionSamples {
    /*
     * x-ms-original-file: 2022-09-01/ServerEndpoints_Recall.json
     */
    /**
     * Sample code: ServerEndpoints_recallAction.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void serverEndpointsRecallAction(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.serverEndpoints()
            .recallAction("SampleResourceGroup_1", "SampleStorageSyncService_1", "SampleSyncGroup_1",
                "SampleServerEndpoint_1", new RecallActionParameters().withPattern("").withRecallPath(""),
                com.azure.core.util.Context.NONE);
    }
}
```

### ServerEndpoints_Update

```java
import com.azure.resourcemanager.storagesync.models.FeatureStatus;
import com.azure.resourcemanager.storagesync.models.LocalCacheMode;
import com.azure.resourcemanager.storagesync.models.ServerEndpoint;

/**
 * Samples for ServerEndpoints Update.
 */
public final class ServerEndpointsUpdateSamples {
    /*
     * x-ms-original-file: 2022-09-01/ServerEndpoints_Update.json
     */
    /**
     * Sample code: ServerEndpoints_Update.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void serverEndpointsUpdate(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        ServerEndpoint resource = manager.serverEndpoints()
            .getWithResponse("SampleResourceGroup_1", "SampleStorageSyncService_1", "SampleSyncGroup_1",
                "SampleServerEndpoint_1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withCloudTiering(FeatureStatus.OFF)
            .withVolumeFreeSpacePercent(100)
            .withTierFilesOlderThanDays(0)
            .withOfflineDataTransfer(FeatureStatus.OFF)
            .withLocalCacheMode(LocalCacheMode.UPDATE_LOCALLY_CACHED_FILES)
            .apply();
    }
}
```

### StorageSyncServices_CheckNameAvailability

```java
import com.azure.resourcemanager.storagesync.models.CheckNameAvailabilityParameters;
import com.azure.resourcemanager.storagesync.models.Type;

/**
 * Samples for StorageSyncServices CheckNameAvailability.
 */
public final class StorageSyncServicesCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: 2022-09-01/StorageSyncServiceCheckNameAvailability_Available.json
     */
    /**
     * Sample code: StorageSyncServiceCheckNameAvailability_Available.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void storageSyncServiceCheckNameAvailabilityAvailable(
        com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.storageSyncServices()
            .checkNameAvailabilityWithResponse("westus",
                new CheckNameAvailabilityParameters().withName("newstoragesyncservicename")
                    .withType(Type.MICROSOFT_STORAGE_SYNC_STORAGE_SYNC_SERVICES),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2022-09-01/StorageSyncServiceCheckNameAvailability_AlreadyExists.json
     */
    /**
     * Sample code: StorageSyncServiceCheckNameAvailability_AlreadyExists.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void storageSyncServiceCheckNameAvailabilityAlreadyExists(
        com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.storageSyncServices()
            .checkNameAvailabilityWithResponse("westus",
                new CheckNameAvailabilityParameters().withName("newstoragesyncservicename")
                    .withType(Type.MICROSOFT_STORAGE_SYNC_STORAGE_SYNC_SERVICES),
                com.azure.core.util.Context.NONE);
    }
}
```

### StorageSyncServices_Create

```java
import com.azure.resourcemanager.storagesync.models.IncomingTrafficPolicy;
import com.azure.resourcemanager.storagesync.models.ManagedServiceIdentity;
import com.azure.resourcemanager.storagesync.models.ManagedServiceIdentityType;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for StorageSyncServices Create.
 */
public final class StorageSyncServicesCreateSamples {
    /*
     * x-ms-original-file: 2022-09-01/StorageSyncServices_Create.json
     */
    /**
     * Sample code: StorageSyncServices_Create.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void storageSyncServicesCreate(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.storageSyncServices()
            .define("SampleStorageSyncService_1")
            .withRegion("WestUS")
            .withExistingResourceGroup("SampleResourceGroup_1")
            .withTags(mapOf())
            .withIdentity(new ManagedServiceIdentity()
                .withType(ManagedServiceIdentityType.fromString("SystemAssigned, UserAssigned")))
            .withIncomingTrafficPolicy(IncomingTrafficPolicy.ALLOW_ALL_TRAFFIC)
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

### StorageSyncServices_Delete

```java
/**
 * Samples for StorageSyncServices Delete.
 */
public final class StorageSyncServicesDeleteSamples {
    /*
     * x-ms-original-file: 2022-09-01/StorageSyncServices_Delete.json
     */
    /**
     * Sample code: StorageSyncServices_Delete.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void storageSyncServicesDelete(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.storageSyncServices()
            .delete("SampleResourceGroup_1", "SampleStorageSyncService_1", com.azure.core.util.Context.NONE);
    }
}
```

### StorageSyncServices_GetByResourceGroup

```java
/**
 * Samples for StorageSyncServices GetByResourceGroup.
 */
public final class StorageSyncServicesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2022-09-01/StorageSyncServices_Get.json
     */
    /**
     * Sample code: StorageSyncServices_Get.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void storageSyncServicesGet(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.storageSyncServices()
            .getByResourceGroupWithResponse("SampleResourceGroup_1", "SampleStorageSyncService_1",
                com.azure.core.util.Context.NONE);
    }
}
```

### StorageSyncServices_List

```java
/**
 * Samples for StorageSyncServices List.
 */
public final class StorageSyncServicesListSamples {
    /*
     * x-ms-original-file: 2022-09-01/StorageSyncServices_ListBySubscription.json
     */
    /**
     * Sample code: StorageSyncServices_ListBySubscription.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void
        storageSyncServicesListBySubscription(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.storageSyncServices().list(com.azure.core.util.Context.NONE);
    }
}
```

### StorageSyncServices_ListByResourceGroup

```java
/**
 * Samples for StorageSyncServices ListByResourceGroup.
 */
public final class StorageSyncServicesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2022-09-01/StorageSyncServices_ListByResourceGroup.json
     */
    /**
     * Sample code: StorageSyncServices_ListByResourceGroup.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void
        storageSyncServicesListByResourceGroup(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.storageSyncServices().listByResourceGroup("SampleResourceGroup_1", com.azure.core.util.Context.NONE);
    }
}
```

### StorageSyncServices_Update

```java
import com.azure.resourcemanager.storagesync.models.IncomingTrafficPolicy;
import com.azure.resourcemanager.storagesync.models.StorageSyncService;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for StorageSyncServices Update.
 */
public final class StorageSyncServicesUpdateSamples {
    /*
     * x-ms-original-file: 2022-09-01/StorageSyncServices_Update.json
     */
    /**
     * Sample code: StorageSyncServices_Update.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void storageSyncServicesUpdate(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        StorageSyncService resource = manager.storageSyncServices()
            .getByResourceGroupWithResponse("SampleResourceGroup_1", "SampleStorageSyncService_1",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("Dept", "IT", "Environment", "Test"))
            .withIncomingTrafficPolicy(IncomingTrafficPolicy.ALLOW_ALL_TRAFFIC)
            .withUseIdentity(true)
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

### SyncGroups_Create

```java
import com.azure.core.util.BinaryData;
import java.nio.charset.StandardCharsets;

/**
 * Samples for SyncGroups Create.
 */
public final class SyncGroupsCreateSamples {
    /*
     * x-ms-original-file: 2022-09-01/SyncGroups_Create.json
     */
    /**
     * Sample code: SyncGroups_Create.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void syncGroupsCreate(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.syncGroups()
            .define("SampleSyncGroup_1")
            .withExistingStorageSyncService("SampleResourceGroup_1", "SampleStorageSyncService_1")
            .withProperties(BinaryData.fromBytes("{}".getBytes(StandardCharsets.UTF_8)))
            .create();
    }
}
```

### SyncGroups_Delete

```java
/**
 * Samples for SyncGroups Delete.
 */
public final class SyncGroupsDeleteSamples {
    /*
     * x-ms-original-file: 2022-09-01/SyncGroups_Delete.json
     */
    /**
     * Sample code: SyncGroups_Delete.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void syncGroupsDelete(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.syncGroups()
            .deleteWithResponse("SampleResourceGroup_1", "SampleStorageSyncService_1", "SampleSyncGroup_1",
                com.azure.core.util.Context.NONE);
    }
}
```

### SyncGroups_Get

```java
/**
 * Samples for SyncGroups Get.
 */
public final class SyncGroupsGetSamples {
    /*
     * x-ms-original-file: 2022-09-01/SyncGroups_Get.json
     */
    /**
     * Sample code: SyncGroups_Get.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void syncGroupsGet(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.syncGroups()
            .getWithResponse("SampleResourceGroup_1", "SampleStorageSyncService_1", "SampleSyncGroup_1",
                com.azure.core.util.Context.NONE);
    }
}
```

### SyncGroups_ListByStorageSyncService

```java
/**
 * Samples for SyncGroups ListByStorageSyncService.
 */
public final class SyncGroupsListByStorageSyncServiceSamples {
    /*
     * x-ms-original-file: 2022-09-01/SyncGroups_ListByStorageSyncService.json
     */
    /**
     * Sample code: SyncGroups_ListByStorageSyncService.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void
        syncGroupsListByStorageSyncService(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.syncGroups()
            .listByStorageSyncService("SampleResourceGroup_1", "SampleStorageSyncService_1",
                com.azure.core.util.Context.NONE);
    }
}
```

### Workflows_Abort

```java
/**
 * Samples for Workflows Abort.
 */
public final class WorkflowsAbortSamples {
    /*
     * x-ms-original-file: 2022-09-01/Workflows_Abort.json
     */
    /**
     * Sample code: Workflows_Abort.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void workflowsAbort(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.workflows()
            .abortWithResponse("SampleResourceGroup_1", "SampleStorageSyncService_1",
                "7ffd50b3-5574-478d-9ff2-9371bc42ce68", com.azure.core.util.Context.NONE);
    }
}
```

### Workflows_Get

```java
/**
 * Samples for Workflows Get.
 */
public final class WorkflowsGetSamples {
    /*
     * x-ms-original-file: 2022-09-01/Workflows_Get.json
     */
    /**
     * Sample code: Workflows_Get.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void workflowsGet(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.workflows()
            .getWithResponse("SampleResourceGroup_1", "SampleStorageSyncService_1",
                "828219ea-083e-48b5-89ea-8fd9991b2e75", com.azure.core.util.Context.NONE);
    }
}
```

### Workflows_ListByStorageSyncService

```java
/**
 * Samples for Workflows ListByStorageSyncService.
 */
public final class WorkflowsListByStorageSyncServiceSamples {
    /*
     * x-ms-original-file: 2022-09-01/Workflows_ListByStorageSyncService.json
     */
    /**
     * Sample code: Workflows_ListByStorageSyncService.
     * 
     * @param manager Entry point to StorageSyncManager.
     */
    public static void
        workflowsListByStorageSyncService(com.azure.resourcemanager.storagesync.StorageSyncManager manager) {
        manager.workflows()
            .listByStorageSyncService("SampleResourceGroup_1", "SampleStorageSyncService_1",
                com.azure.core.util.Context.NONE);
    }
}
```

