# Code snippets and samples


## FileShareSnapshots

- [CreateOrUpdateFileShareSnapshot](#filesharesnapshots_createorupdatefilesharesnapshot)
- [DeleteFileShareSnapshot](#filesharesnapshots_deletefilesharesnapshot)
- [GetFileShareSnapshot](#filesharesnapshots_getfilesharesnapshot)
- [ListByFileShare](#filesharesnapshots_listbyfileshare)
- [UpdateFileShareSnapshot](#filesharesnapshots_updatefilesharesnapshot)

## FileShares

- [CheckNameAvailability](#fileshares_checknameavailability)
- [CreateOrUpdate](#fileshares_createorupdate)
- [Delete](#fileshares_delete)
- [GetByResourceGroup](#fileshares_getbyresourcegroup)
- [List](#fileshares_list)
- [ListByResourceGroup](#fileshares_listbyresourcegroup)
- [Update](#fileshares_update)

## InformationalOperations

- [GetLimits](#informationaloperations_getlimits)
- [GetProvisioningRecommendation](#informationaloperations_getprovisioningrecommendation)
- [GetUsageData](#informationaloperations_getusagedata)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [Create](#privateendpointconnections_create)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [ListByFileShare](#privateendpointconnections_listbyfileshare)

## PrivateLinkResources

- [Get](#privatelinkresources_get)
- [List](#privatelinkresources_list)
### FileShareSnapshots_CreateOrUpdateFileShareSnapshot

```java
import com.azure.resourcemanager.fileshares.models.FileShareSnapshotProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for FileShareSnapshots CreateOrUpdateFileShareSnapshot.
 */
public final class FileShareSnapshotsCreateOrUpdateFileShareSnapshotSamples {
    /*
     * x-ms-original-file: 2026-06-01/FileShareSnapshot_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileShareSnapshot_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void
        fileShareSnapshotCreateOrUpdateMaximumSet(com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        manager.fileShareSnapshots()
            .define("testfilesharesnapshot")
            .withExistingFileShare("rgfileshares", "fileshare")
            .withProperties(new FileShareSnapshotProperties().withInitiatorId("backup-vault-001")
                .withMetadata(mapOf("key9372", "fakeTokenPlaceholder")))
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

### FileShareSnapshots_DeleteFileShareSnapshot

```java
/**
 * Samples for FileShareSnapshots DeleteFileShareSnapshot.
 */
public final class FileShareSnapshotsDeleteFileShareSnapshotSamples {
    /*
     * x-ms-original-file: 2026-06-01/FileShareSnapshot_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileShareSnapshot_Delete_MaximumSet.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void
        fileShareSnapshotDeleteMaximumSet(com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        manager.fileShareSnapshots()
            .deleteFileShareSnapshot("rgfileshares", "fileshare", "testfilesharesnapshot",
                com.azure.core.util.Context.NONE);
    }
}
```

### FileShareSnapshots_GetFileShareSnapshot

```java
/**
 * Samples for FileShareSnapshots GetFileShareSnapshot.
 */
public final class FileShareSnapshotsGetFileShareSnapshotSamples {
    /*
     * x-ms-original-file: 2026-06-01/FileShareSnapshot_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileShareSnapshot_Get_MaximumSet.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void fileShareSnapshotGetMaximumSet(com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        manager.fileShareSnapshots()
            .getFileShareSnapshotWithResponse("rgfileshares", "fileshare", "testfilesharesnapshot",
                com.azure.core.util.Context.NONE);
    }
}
```

### FileShareSnapshots_ListByFileShare

```java
/**
 * Samples for FileShareSnapshots ListByFileShare.
 */
public final class FileShareSnapshotsListByFileShareSamples {
    /*
     * x-ms-original-file: 2026-06-01/FileShareSnapshot_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: FileShareSnapshot_List_MinimumSet.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void fileShareSnapshotListMinimumSet(com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        manager.fileShareSnapshots().listByFileShare("rgfileshares", "testfileshare", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-06-01/FileShareSnapshot_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileShareSnapshot_List_MaximumSet.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void fileShareSnapshotListMaximumSet(com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        manager.fileShareSnapshots().listByFileShare("rgfileshares", "fileshare", com.azure.core.util.Context.NONE);
    }
}
```

### FileShareSnapshots_UpdateFileShareSnapshot

```java
import com.azure.resourcemanager.fileshares.models.FileShareSnapshot;
import com.azure.resourcemanager.fileshares.models.FileShareSnapshotUpdateProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for FileShareSnapshots UpdateFileShareSnapshot.
 */
public final class FileShareSnapshotsUpdateFileShareSnapshotSamples {
    /*
     * x-ms-original-file: 2026-06-01/FileShareSnapshot_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileShareSnapshot_Update_MaximumSet.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void
        fileShareSnapshotUpdateMaximumSet(com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        FileShareSnapshot resource = manager.fileShareSnapshots()
            .getFileShareSnapshotWithResponse("rgfileshares", "fileshare", "testfilesharesnapshot",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(
                new FileShareSnapshotUpdateProperties().withMetadata(mapOf("key491", "fakeTokenPlaceholder")))
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

### FileShares_CheckNameAvailability

```java
import com.azure.resourcemanager.fileshares.models.CheckNameAvailabilityRequest;

/**
 * Samples for FileShares CheckNameAvailability.
 */
public final class FileSharesCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: 2026-06-01/FileShares_CheckNameAvailability_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileShares_CheckNameAvailability_MaximumSet.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void
        fileSharesCheckNameAvailabilityMaximumSet(com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        manager.fileShares()
            .checkNameAvailabilityWithResponse("westus",
                new CheckNameAvailabilityRequest().withName("fvykqbgmd").withType("Microsoft.FileShares/fileShares"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-06-01/FileShares_CheckNameAvailability_MinimumSet_Gen.json
     */
    /**
     * Sample code: FileShares_CheckNameAvailability_MinimumSet.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void
        fileSharesCheckNameAvailabilityMinimumSet(com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        manager.fileShares()
            .checkNameAvailabilityWithResponse("westus", new CheckNameAvailabilityRequest(),
                com.azure.core.util.Context.NONE);
    }
}
```

### FileShares_CreateOrUpdate

```java
import com.azure.resourcemanager.fileshares.models.EncryptionInTransitRequired;
import com.azure.resourcemanager.fileshares.models.FileShareProperties;
import com.azure.resourcemanager.fileshares.models.MediaTier;
import com.azure.resourcemanager.fileshares.models.NfsProtocolProperties;
import com.azure.resourcemanager.fileshares.models.Protocol;
import com.azure.resourcemanager.fileshares.models.PublicAccessProperties;
import com.azure.resourcemanager.fileshares.models.PublicNetworkAccess;
import com.azure.resourcemanager.fileshares.models.Redundancy;
import com.azure.resourcemanager.fileshares.models.ShareRootSquash;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for FileShares CreateOrUpdate.
 */
public final class FileSharesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-06-01/FileShares_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileShares_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void
        fileSharesCreateOrUpdateMaximumSet(com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        manager.fileShares()
            .define("fileshare")
            .withRegion("westus")
            .withExistingResourceGroup("rgfileshares")
            .withTags(mapOf("key9647", "fakeTokenPlaceholder"))
            .withProperties(new FileShareProperties().withMountName("fileshare")
                .withMediaTier(MediaTier.SSD)
                .withRedundancy(Redundancy.LOCAL)
                .withProtocol(Protocol.NFS)
                .withProvisionedStorageGiB(8)
                .withProvisionedIOPerSec(5)
                .withProvisionedThroughputMiBPerSec(22)
                .withNfsProtocolProperties(new NfsProtocolProperties().withRootSquash(ShareRootSquash.NO_ROOT_SQUASH)
                    .withEncryptionInTransitRequired(EncryptionInTransitRequired.ENABLED))
                .withPublicAccessProperties(new PublicAccessProperties().withAllowedSubnets(Arrays.asList(
                    "/subscriptions/9760acf5-4638-11e7-9bdb-020073ca7778/resourceGroups/myRP/providers/Microsoft.Network/virtualNetworks/testvnet3/subnets/testsubnet3")))
                .withPublicNetworkAccess(PublicNetworkAccess.ENABLED))
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

### FileShares_Delete

```java
/**
 * Samples for FileShares Delete.
 */
public final class FileSharesDeleteSamples {
    /*
     * x-ms-original-file: 2026-06-01/FileShares_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileShares_Delete_MaximumSet.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void fileSharesDeleteMaximumSet(com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        manager.fileShares().delete("rgfileshares", "fileshare", com.azure.core.util.Context.NONE);
    }
}
```

### FileShares_GetByResourceGroup

```java
/**
 * Samples for FileShares GetByResourceGroup.
 */
public final class FileSharesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-06-01/FileShares_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileShares_Get_MaximumSet.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void fileSharesGetMaximumSet(com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        manager.fileShares()
            .getByResourceGroupWithResponse("rgfileshares", "fileshare", com.azure.core.util.Context.NONE);
    }
}
```

### FileShares_List

```java
/**
 * Samples for FileShares List.
 */
public final class FileSharesListSamples {
    /*
     * x-ms-original-file: 2026-06-01/FileShares_ListBySubscription_MinimumSet_Gen.json
     */
    /**
     * Sample code: FileShares_ListBySubscription_MinimumSet.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void
        fileSharesListBySubscriptionMinimumSet(com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        manager.fileShares().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-06-01/FileShares_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileShares_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void
        fileSharesListBySubscriptionMaximumSet(com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        manager.fileShares().list(com.azure.core.util.Context.NONE);
    }
}
```

### FileShares_ListByResourceGroup

```java
/**
 * Samples for FileShares ListByResourceGroup.
 */
public final class FileSharesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-06-01/FileShares_ListByParent_MinimumSet_Gen.json
     */
    /**
     * Sample code: FileShares_ListByParent_MinimumSet.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void
        fileSharesListByParentMinimumSet(com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        manager.fileShares().listByResourceGroup("rgfileshares", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-06-01/FileShares_ListByParent_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileShares_ListByParent_MaximumSet.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void
        fileSharesListByParentMaximumSet(com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        manager.fileShares().listByResourceGroup("rgfileshares", com.azure.core.util.Context.NONE);
    }
}
```

### FileShares_Update

```java
import com.azure.resourcemanager.fileshares.models.EncryptionInTransitRequired;
import com.azure.resourcemanager.fileshares.models.FileShare;
import com.azure.resourcemanager.fileshares.models.FileShareUpdateProperties;
import com.azure.resourcemanager.fileshares.models.NfsProtocolProperties;
import com.azure.resourcemanager.fileshares.models.PublicAccessProperties;
import com.azure.resourcemanager.fileshares.models.PublicNetworkAccess;
import com.azure.resourcemanager.fileshares.models.ShareRootSquash;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for FileShares Update.
 */
public final class FileSharesUpdateSamples {
    /*
     * x-ms-original-file: 2026-06-01/FileShares_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileShares_Update_MaximumSet.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void fileSharesUpdateMaximumSet(com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        FileShare resource = manager.fileShares()
            .getByResourceGroupWithResponse("rgfileshares", "fileshare", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key173", "fakeTokenPlaceholder"))
            .withProperties(new FileShareUpdateProperties().withProvisionedStorageGiB(7)
                .withProvisionedIOPerSec(1)
                .withProvisionedThroughputMiBPerSec(29)
                .withNfsProtocolProperties(new NfsProtocolProperties().withRootSquash(ShareRootSquash.NO_ROOT_SQUASH)
                    .withEncryptionInTransitRequired(EncryptionInTransitRequired.ENABLED))
                .withPublicAccessProperties(new PublicAccessProperties().withAllowedSubnets(Arrays.asList(
                    "/subscriptions/9760acf5-4638-11e7-9bdb-020073ca7778/resourceGroups/myRP/providers/Microsoft.Network/virtualNetworks/testvnet3/subnets/testsubnet3")))
                .withPublicNetworkAccess(PublicNetworkAccess.ENABLED))
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

### InformationalOperations_GetLimits

```java
/**
 * Samples for InformationalOperations GetLimits.
 */
public final class InformationalOperationsGetLimitsSamples {
    /*
     * x-ms-original-file: 2026-06-01/FileShare_GetLimits_MinimumSet_Gen.json
     */
    /**
     * Sample code: FileShare_GetLimits_MinimumSet.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void fileShareGetLimitsMinimumSet(com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        manager.informationalOperations().getLimitsWithResponse("westus", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-06-01/FileShare_GetLimits_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileShare_GetLimits_MaximumSet.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void fileShareGetLimitsMaximumSet(com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        manager.informationalOperations().getLimitsWithResponse("westus", com.azure.core.util.Context.NONE);
    }
}
```

### InformationalOperations_GetProvisioningRecommendation

```java
import com.azure.resourcemanager.fileshares.models.FileShareProvisioningRecommendationInput;
import com.azure.resourcemanager.fileshares.models.FileShareProvisioningRecommendationRequest;

/**
 * Samples for InformationalOperations GetProvisioningRecommendation.
 */
public final class InformationalOperationsGetProvisioningRecommendationSamples {
    /*
     * x-ms-original-file: 2026-06-01/FileShare_GetProvisioningRecommendation_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileShare_GetProvisioningRecommendation_MaximumSet.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void fileShareGetProvisioningRecommendationMaximumSet(
        com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        manager.informationalOperations()
            .getProvisioningRecommendationWithResponse("westus",
                new FileShareProvisioningRecommendationRequest()
                    .withProperties(new FileShareProvisioningRecommendationInput().withProvisionedStorageGiB(7)),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-06-01/FileShare_GetProvisioningRecommendation_MinimumSet_Gen.json
     */
    /**
     * Sample code: FileShare_GetProvisioningRecommendation_MinimumSet.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void fileShareGetProvisioningRecommendationMinimumSet(
        com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        manager.informationalOperations()
            .getProvisioningRecommendationWithResponse("westus",
                new FileShareProvisioningRecommendationRequest()
                    .withProperties(new FileShareProvisioningRecommendationInput().withProvisionedStorageGiB(7)),
                com.azure.core.util.Context.NONE);
    }
}
```

### InformationalOperations_GetUsageData

```java
/**
 * Samples for InformationalOperations GetUsageData.
 */
public final class InformationalOperationsGetUsageDataSamples {
    /*
     * x-ms-original-file: 2026-06-01/FileShare_GetUsageData_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileShare_GetUsageData_MaximumSet.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void fileShareGetUsageDataMaximumSet(com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        manager.informationalOperations().getUsageDataWithResponse("westus", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-06-01/FileShare_GetUsageData_MinimumSet_Gen.json
     */
    /**
     * Sample code: FileShare_GetUsageData_MinimumSet.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void fileShareGetUsageDataMinimumSet(com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        manager.informationalOperations().getUsageDataWithResponse("westus", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-06-01/Operations_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MinimumSet.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void operationsListMinimumSet(com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-06-01/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MaximumSet.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void operationsListMaximumSet(com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Create

```java
import com.azure.resourcemanager.fileshares.models.PrivateEndpointConnectionProperties;
import com.azure.resourcemanager.fileshares.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.fileshares.models.PrivateLinkServiceConnectionState;

/**
 * Samples for PrivateEndpointConnections Create.
 */
public final class PrivateEndpointConnectionsCreateSamples {
    /*
     * x-ms-original-file: 2026-06-01/PrivateEndpointConnections_Create.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Create.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void
        privateEndpointConnectionsCreate(com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        manager.privateEndpointConnections()
            .define("privateEndpointConnection1")
            .withExistingFileShare("rgfileshares", "fileshare")
            .withProperties(new PrivateEndpointConnectionProperties().withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState().withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                    .withDescription("Approved by admin")))
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
     * x-ms-original-file: 2026-06-01/PrivateEndpointConnections_Delete.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Delete.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void
        privateEndpointConnectionsDelete(com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        manager.privateEndpointConnections()
            .delete("rgfileshares", "fileshare", "privateEndpointConnection1", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-06-01/PrivateEndpointConnections_Get.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Get.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void privateEndpointConnectionsGet(com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        manager.privateEndpointConnections()
            .getWithResponse("rgfileshares", "fileshare", "privateEndpointConnection1",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_ListByFileShare

```java
/**
 * Samples for PrivateEndpointConnections ListByFileShare.
 */
public final class PrivateEndpointConnectionsListByFileShareSamples {
    /*
     * x-ms-original-file: 2026-06-01/PrivateEndpointConnections_ListByFileShare.json
     */
    /**
     * Sample code: PrivateEndpointConnections_ListByFileShare.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void
        privateEndpointConnectionsListByFileShare(com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        manager.privateEndpointConnections()
            .listByFileShare("rgfileshares", "fileshare", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
/**
 * Samples for PrivateLinkResources Get.
 */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file: 2026-06-01/PrivateLinkResources_Get.json
     */
    /**
     * Sample code: Get PrivateLinkResource.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void getPrivateLinkResource(com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        manager.privateLinkResources()
            .getWithResponse("res4303", "testfileshare01", "fileshare", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_List

```java
/**
 * Samples for PrivateLinkResources List.
 */
public final class PrivateLinkResourcesListSamples {
    /*
     * x-ms-original-file: 2026-06-01/PrivateLinkResources_ListByFileShare.json
     */
    /**
     * Sample code: List PrivateLinkResources.
     * 
     * @param manager Entry point to FileSharesManager.
     */
    public static void listPrivateLinkResources(com.azure.resourcemanager.fileshares.FileSharesManager manager) {
        manager.privateLinkResources().list("res4303", "testfileshare01", com.azure.core.util.Context.NONE);
    }
}
```

