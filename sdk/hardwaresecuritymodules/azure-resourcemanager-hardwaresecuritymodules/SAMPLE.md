# Code snippets and samples


## CloudHsmClusterBackupStatus

- [Get](#cloudhsmclusterbackupstatus_get)

## CloudHsmClusterPrivateEndpointConnections

- [Create](#cloudhsmclusterprivateendpointconnections_create)
- [Delete](#cloudhsmclusterprivateendpointconnections_delete)
- [Get](#cloudhsmclusterprivateendpointconnections_get)

## CloudHsmClusterPrivateLinkResources

- [ListByCloudHsmCluster](#cloudhsmclusterprivatelinkresources_listbycloudhsmcluster)

## CloudHsmClusterRestoreStatus

- [Get](#cloudhsmclusterrestorestatus_get)

## CloudHsmClusters

- [Backup](#cloudhsmclusters_backup)
- [CreateOrUpdate](#cloudhsmclusters_createorupdate)
- [Delete](#cloudhsmclusters_delete)
- [GetByResourceGroup](#cloudhsmclusters_getbyresourcegroup)
- [List](#cloudhsmclusters_list)
- [ListByResourceGroup](#cloudhsmclusters_listbyresourcegroup)
- [Restore](#cloudhsmclusters_restore)
- [Update](#cloudhsmclusters_update)
- [ValidateBackupProperties](#cloudhsmclusters_validatebackupproperties)
- [ValidateRestoreProperties](#cloudhsmclusters_validaterestoreproperties)

## DedicatedHsm

- [CreateOrUpdate](#dedicatedhsm_createorupdate)
- [Delete](#dedicatedhsm_delete)
- [GetByResourceGroup](#dedicatedhsm_getbyresourcegroup)
- [List](#dedicatedhsm_list)
- [ListByResourceGroup](#dedicatedhsm_listbyresourcegroup)
- [ListOutboundNetworkDependenciesEndpoints](#dedicatedhsm_listoutboundnetworkdependenciesendpoints)
- [Update](#dedicatedhsm_update)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [ListByCloudHsmCluster](#privateendpointconnections_listbycloudhsmcluster)
### CloudHsmClusterBackupStatus_Get

```java
/**
 * Samples for CloudHsmClusterBackupStatus Get.
 */
public final class CloudHsmClusterBackupStatusGetSamples {
    /*
     * x-ms-original-file: 2025-03-31/CloudHsmCluster_Backup_Pending_MaximumSet_Gen.json
     */
    /**
     * Sample code: CloudHsmCluster_Get_Backup_Status_MaximumSet_Gen.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void cloudHsmClusterGetBackupStatusMaximumSetGen(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.cloudHsmClusterBackupStatus()
            .getWithResponse("rgcloudhsm", "chsm1", "572a45927fc240e1ac075de27371680b",
                com.azure.core.util.Context.NONE);
    }
}
```

### CloudHsmClusterPrivateEndpointConnections_Create

```java
import com.azure.resourcemanager.hardwaresecuritymodules.models.PrivateEndpointConnectionProperties;
import com.azure.resourcemanager.hardwaresecuritymodules.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.hardwaresecuritymodules.models.PrivateLinkServiceConnectionState;

/**
 * Samples for CloudHsmClusterPrivateEndpointConnections Create.
 */
public final class CloudHsmClusterPrivateEndpointConnectionsCreateSamples {
    /*
     * x-ms-original-file: 2025-03-31/CloudHsmClusterPrivateEndpointConnection_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: CloudHsmClusterPrivateEndpointConnection_Create_MaximumSet_Gen.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void cloudHsmClusterPrivateEndpointConnectionCreateMaximumSetGen(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.cloudHsmClusterPrivateEndpointConnections()
            .define("sample-pec")
            .withExistingCloudHsmCluster("rgcloudhsm", "chsm1")
            .withProperties(new PrivateEndpointConnectionProperties().withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState().withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                    .withDescription("My name is Joe and I'm approving this.")))
            .create();
    }
}
```

### CloudHsmClusterPrivateEndpointConnections_Delete

```java
/**
 * Samples for CloudHsmClusterPrivateEndpointConnections Delete.
 */
public final class CloudHsmClusterPrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: 2025-03-31/CloudHsmClusterPrivateEndpointConnection_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: CloudHsmClusterPrivateEndpointConnection_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void cloudHsmClusterPrivateEndpointConnectionDeleteMaximumSetGen(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.cloudHsmClusterPrivateEndpointConnections()
            .delete("rgcloudhsm", "chsm1", "sample-pec", com.azure.core.util.Context.NONE);
    }
}
```

### CloudHsmClusterPrivateEndpointConnections_Get

```java
/**
 * Samples for CloudHsmClusterPrivateEndpointConnections Get.
 */
public final class CloudHsmClusterPrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: 2025-03-31/CloudHsmClusterPrivateEndpointConnection_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: CloudHsmClusterPrivateEndpointConnection_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void cloudHsmClusterPrivateEndpointConnectionGetMaximumSetGen(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.cloudHsmClusterPrivateEndpointConnections()
            .getWithResponse("rgcloudhsm", "chsm1", "sample-pec", com.azure.core.util.Context.NONE);
    }
}
```

### CloudHsmClusterPrivateLinkResources_ListByCloudHsmCluster

```java
/**
 * Samples for CloudHsmClusterPrivateLinkResources ListByCloudHsmCluster.
 */
public final class CloudHsmClusterPrivateLinkResourcesListByCloudHsmClusterSamples {
    /*
     * x-ms-original-file: 2025-03-31/CloudHsmClusterPrivateLinkResource_ListByCloudHsmCluster_MaximumSet_Gen.json
     */
    /**
     * Sample code: CloudHsmClusterPrivateLinkResources_ListByResource_MaximumSet_Gen.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void cloudHsmClusterPrivateLinkResourcesListByResourceMaximumSetGen(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.cloudHsmClusterPrivateLinkResources()
            .listByCloudHsmCluster("rgcloudhsm", "chsm1", com.azure.core.util.Context.NONE);
    }
}
```

### CloudHsmClusterRestoreStatus_Get

```java
/**
 * Samples for CloudHsmClusterRestoreStatus Get.
 */
public final class CloudHsmClusterRestoreStatusGetSamples {
    /*
     * x-ms-original-file: 2025-03-31/CloudHsmCluster_Restore_Pending_MaximumSet_Gen.json
     */
    /**
     * Sample code: CloudHsmCluster_Get_Restore_Status_MaximumSet_Gen.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void cloudHsmClusterGetRestoreStatusMaximumSetGen(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.cloudHsmClusterRestoreStatus()
            .getWithResponse("rgcloudhsm", "chsm1", "572a45927fc240e1ac075de27371680b",
                com.azure.core.util.Context.NONE);
    }
}
```

### CloudHsmClusters_Backup

```java
import com.azure.resourcemanager.hardwaresecuritymodules.models.BackupRequestProperties;

/**
 * Samples for CloudHsmClusters Backup.
 */
public final class CloudHsmClustersBackupSamples {
    /*
     * x-ms-original-file: 2025-03-31/CloudHsmCluster_CreateOrValidate_Backup_MaximumSet_Gen.json
     */
    /**
     * Sample code: CloudHsmCluster_Create_Backup_MaximumSet_Gen.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void cloudHsmClusterCreateBackupMaximumSetGen(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.cloudHsmClusters()
            .backup("rgcloudhsm", "chsm1",
                new BackupRequestProperties()
                    .withAzureStorageBlobContainerUri(
                        "https://myaccount.blob.core.windows.net/sascontainer/sasContainer")
                    .withToken("fakeTokenPlaceholder"),
                com.azure.core.util.Context.NONE);
    }
}
```

### CloudHsmClusters_CreateOrUpdate

```java
import com.azure.resourcemanager.hardwaresecuritymodules.models.CloudHsmClusterProperties;
import com.azure.resourcemanager.hardwaresecuritymodules.models.CloudHsmClusterSku;
import com.azure.resourcemanager.hardwaresecuritymodules.models.CloudHsmClusterSkuFamily;
import com.azure.resourcemanager.hardwaresecuritymodules.models.CloudHsmClusterSkuName;
import com.azure.resourcemanager.hardwaresecuritymodules.models.ManagedServiceIdentity;
import com.azure.resourcemanager.hardwaresecuritymodules.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.hardwaresecuritymodules.models.PublicNetworkAccess;
import com.azure.resourcemanager.hardwaresecuritymodules.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for CloudHsmClusters CreateOrUpdate.
 */
public final class CloudHsmClustersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-03-31/CloudHsmCluster_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: CloudHsmCluster_CreateOrUpdate_MaximumSet_Gen.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void cloudHsmClusterCreateOrUpdateMaximumSetGen(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.cloudHsmClusters()
            .define("chsm1")
            .withRegion("eastus2")
            .withExistingResourceGroup("rgcloudhsm")
            .withTags(mapOf("Dept", "hsm", "Environment", "dogfood"))
            .withProperties(new CloudHsmClusterProperties().withPublicNetworkAccess(PublicNetworkAccess.DISABLED))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/contoso-resources/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identity-1",
                    new UserAssignedIdentity())))
            .withSku(new CloudHsmClusterSku().withFamily(CloudHsmClusterSkuFamily.B)
                .withName(CloudHsmClusterSkuName.STANDARD_B1))
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

### CloudHsmClusters_Delete

```java
/**
 * Samples for CloudHsmClusters Delete.
 */
public final class CloudHsmClustersDeleteSamples {
    /*
     * x-ms-original-file: 2025-03-31/CloudHsmCluster_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: CloudHsmCluster_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void cloudHsmClusterDeleteMaximumSetGen(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.cloudHsmClusters().delete("rgcloudhsm", "chsm1", com.azure.core.util.Context.NONE);
    }
}
```

### CloudHsmClusters_GetByResourceGroup

```java
/**
 * Samples for CloudHsmClusters GetByResourceGroup.
 */
public final class CloudHsmClustersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-03-31/CloudHsmCluster_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: CloudHsmCluster_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void cloudHsmClusterGetMaximumSetGen(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.cloudHsmClusters()
            .getByResourceGroupWithResponse("rgcloudhsm", "chsm1", com.azure.core.util.Context.NONE);
    }
}
```

### CloudHsmClusters_List

```java
/**
 * Samples for CloudHsmClusters List.
 */
public final class CloudHsmClustersListSamples {
    /*
     * x-ms-original-file: 2025-03-31/CloudHsmCluster_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: CloudHsmCluster_ListBySubscription_MaximumSet_Gen.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void cloudHsmClusterListBySubscriptionMaximumSetGen(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.cloudHsmClusters().list(null, com.azure.core.util.Context.NONE);
    }
}
```

### CloudHsmClusters_ListByResourceGroup

```java
/**
 * Samples for CloudHsmClusters ListByResourceGroup.
 */
public final class CloudHsmClustersListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-03-31/CloudHsmCluster_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: CloudHsmCluster_ListByResourceGroup_MaximumSet_Gen.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void cloudHsmClusterListByResourceGroupMaximumSetGen(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.cloudHsmClusters().listByResourceGroup("rgcloudhsm", null, com.azure.core.util.Context.NONE);
    }
}
```

### CloudHsmClusters_Restore

```java
import com.azure.resourcemanager.hardwaresecuritymodules.models.RestoreRequestProperties;

/**
 * Samples for CloudHsmClusters Restore.
 */
public final class CloudHsmClustersRestoreSamples {
    /*
     * x-ms-original-file: 2025-03-31/CloudHsmCluster_RequestOrValidate_Restore_MaximumSet_Gen.json
     */
    /**
     * Sample code: CloudHsmCluster_Restore_MaximumSet_Gen.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void cloudHsmClusterRestoreMaximumSetGen(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.cloudHsmClusters()
            .restore("rgcloudhsm", "chsm1",
                new RestoreRequestProperties()
                    .withAzureStorageBlobContainerUri(
                        "https://myaccount.blob.core.windows.net/sascontainer/sasContainer")
                    .withBackupId("backupId"),
                com.azure.core.util.Context.NONE);
    }
}
```

### CloudHsmClusters_Update

```java
import com.azure.resourcemanager.hardwaresecuritymodules.models.CloudHsmCluster;
import com.azure.resourcemanager.hardwaresecuritymodules.models.ManagedServiceIdentity;
import com.azure.resourcemanager.hardwaresecuritymodules.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.hardwaresecuritymodules.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for CloudHsmClusters Update.
 */
public final class CloudHsmClustersUpdateSamples {
    /*
     * x-ms-original-file: 2025-03-31/CloudHsmCluster_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: CloudHsmCluster_Update_MaximumSet_Gen.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void cloudHsmClusterUpdateMaximumSetGen(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        CloudHsmCluster resource = manager.cloudHsmClusters()
            .getByResourceGroupWithResponse("rgcloudhsm", "chsm1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("Dept", "hsm", "Environment", "dogfood", "Slice", "A"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/contoso-resources/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identity-1",
                    new UserAssignedIdentity())))
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

### CloudHsmClusters_ValidateBackupProperties

```java
import com.azure.resourcemanager.hardwaresecuritymodules.models.BackupRequestProperties;

/**
 * Samples for CloudHsmClusters ValidateBackupProperties.
 */
public final class CloudHsmClustersValidateBackupPropertiesSamples {
    /*
     * x-ms-original-file: 2025-03-31/CloudHsmCluster_Create_Backup_MaximumSet_Gen_ValidateBackupProperties.json
     */
    /**
     * Sample code: CloudHsmCluster_ValidateBackup_Validation_MaximumSet_Gen.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void cloudHsmClusterValidateBackupValidationMaximumSetGen(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.cloudHsmClusters()
            .validateBackupProperties("rgcloudhsm", "chsm1",
                new BackupRequestProperties()
                    .withAzureStorageBlobContainerUri(
                        "https://myaccount.blob.core.windows.net/sascontainer/sasContainer")
                    .withToken("fakeTokenPlaceholder"),
                com.azure.core.util.Context.NONE);
    }
}
```

### CloudHsmClusters_ValidateRestoreProperties

```java
import com.azure.resourcemanager.hardwaresecuritymodules.models.RestoreRequestProperties;

/**
 * Samples for CloudHsmClusters ValidateRestoreProperties.
 */
public final class CloudHsmClustersValidateRestorePropertiesSamples {
    /*
     * x-ms-original-file:
     * 2025-03-31/CloudHsmCluster_RequestOrValidate_Restore_MaximumSet_Gen_ValidateRestoreProperties.json
     */
    /**
     * Sample code: CloudHsmCluster_ValidateRestore_MaximumSet_Gen.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void cloudHsmClusterValidateRestoreMaximumSetGen(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.cloudHsmClusters()
            .validateRestoreProperties("rgcloudhsm", "chsm1",
                new RestoreRequestProperties()
                    .withAzureStorageBlobContainerUri(
                        "https://myaccount.blob.core.windows.net/sascontainer/sasContainer")
                    .withBackupId("backupId"),
                com.azure.core.util.Context.NONE);
    }
}
```

### DedicatedHsm_CreateOrUpdate

```java
import com.azure.resourcemanager.hardwaresecuritymodules.models.ApiEntityReference;
import com.azure.resourcemanager.hardwaresecuritymodules.models.DedicatedHsmProperties;
import com.azure.resourcemanager.hardwaresecuritymodules.models.NetworkInterface;
import com.azure.resourcemanager.hardwaresecuritymodules.models.NetworkProfile;
import com.azure.resourcemanager.hardwaresecuritymodules.models.Sku;
import com.azure.resourcemanager.hardwaresecuritymodules.models.SkuName;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DedicatedHsm CreateOrUpdate.
 */
public final class DedicatedHsmCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-03-31/PaymentHsm_CreateOrUpdate_WithManagementProfile.json
     */
    /**
     * Sample code: Create a new or update an existing payment HSM with management profile.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void createANewOrUpdateAnExistingPaymentHSMWithManagementProfile(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.dedicatedHsms()
            .define("hsm1")
            .withRegion("westus")
            .withExistingResourceGroup("hsm-group")
            .withProperties(new DedicatedHsmProperties()
                .withNetworkProfile(new NetworkProfile().withSubnet(new ApiEntityReference().withResourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/hsm-group/providers/Microsoft.Network/virtualNetworks/stamp01/subnets/stamp01"))
                    .withNetworkInterfaces(Arrays.asList(new NetworkInterface().withPrivateIpAddress("1.0.0.1"))))
                .withManagementNetworkProfile(new NetworkProfile().withSubnet(new ApiEntityReference().withResourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/hsm-group/providers/Microsoft.Network/virtualNetworks/stamp01/subnets/stamp01"))
                    .withNetworkInterfaces(Arrays.asList(new NetworkInterface().withPrivateIpAddress("1.0.0.2"))))
                .withStampId("stamp01"))
            .withSku(new Sku().withName(SkuName.PAY_SHIELD10K_LMK1_CPS60))
            .withTags(mapOf("Dept", "hsm", "Environment", "dogfood"))
            .create();
    }

    /*
     * x-ms-original-file: 2025-03-31/DedicatedHsm_CreateOrUpdate.json
     */
    /**
     * Sample code: Create a new or update an existing dedicated HSM.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void createANewOrUpdateAnExistingDedicatedHSM(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.dedicatedHsms()
            .define("hsm1")
            .withRegion("westus")
            .withExistingResourceGroup("hsm-group")
            .withProperties(new DedicatedHsmProperties()
                .withNetworkProfile(new NetworkProfile().withSubnet(new ApiEntityReference().withResourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/hsm-group/providers/Microsoft.Network/virtualNetworks/stamp01/subnets/stamp01"))
                    .withNetworkInterfaces(Arrays.asList(new NetworkInterface().withPrivateIpAddress("1.0.0.1"))))
                .withStampId("stamp01"))
            .withSku(new Sku().withName(SkuName.SAFE_NET_LUNA_NETWORK_HSM_A790))
            .withTags(mapOf("Dept", "hsm", "Environment", "dogfood"))
            .create();
    }

    /*
     * x-ms-original-file: 2025-03-31/PaymentHsm_CreateOrUpdate.json
     */
    /**
     * Sample code: Create a new or update an existing payment HSM.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void createANewOrUpdateAnExistingPaymentHSM(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.dedicatedHsms()
            .define("hsm1")
            .withRegion("westus")
            .withExistingResourceGroup("hsm-group")
            .withProperties(new DedicatedHsmProperties()
                .withNetworkProfile(new NetworkProfile().withSubnet(new ApiEntityReference().withResourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/hsm-group/providers/Microsoft.Network/virtualNetworks/stamp01/subnets/stamp01"))
                    .withNetworkInterfaces(Arrays.asList(new NetworkInterface().withPrivateIpAddress("1.0.0.1"))))
                .withStampId("stamp01"))
            .withSku(new Sku().withName(SkuName.PAY_SHIELD10K_LMK1_CPS60))
            .withTags(mapOf("Dept", "hsm", "Environment", "dogfood"))
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

### DedicatedHsm_Delete

```java
/**
 * Samples for DedicatedHsm Delete.
 */
public final class DedicatedHsmDeleteSamples {
    /*
     * x-ms-original-file: 2025-03-31/DedicatedHsm_Delete.json
     */
    /**
     * Sample code: Delete a dedicated HSM.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void
        deleteADedicatedHSM(com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.dedicatedHsms().delete("hsm-group", "hsm1", com.azure.core.util.Context.NONE);
    }
}
```

### DedicatedHsm_GetByResourceGroup

```java
/**
 * Samples for DedicatedHsm GetByResourceGroup.
 */
public final class DedicatedHsmGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-03-31/PaymentHsm_Get.json
     */
    /**
     * Sample code: Get a payment HSM.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void
        getAPaymentHSM(com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.dedicatedHsms().getByResourceGroupWithResponse("hsm-group", "hsm1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-03-31/PaymentHsm_Get_With_2018-10-31Preview_Version.json
     */
    /**
     * Sample code: Get a payment HSM with 2018-10-31Preview api version.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void getAPaymentHSMWith20181031PreviewApiVersion(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.dedicatedHsms().getByResourceGroupWithResponse("hsm-group", "hsm1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-03-31/DedicatedHsm_Get.json
     */
    /**
     * Sample code: Get a dedicated HSM.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void
        getADedicatedHSM(com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.dedicatedHsms().getByResourceGroupWithResponse("hsm-group", "hsm1", com.azure.core.util.Context.NONE);
    }
}
```

### DedicatedHsm_List

```java
/**
 * Samples for DedicatedHsm List.
 */
public final class DedicatedHsmListSamples {
    /*
     * x-ms-original-file: 2025-03-31/PaymentHsm_ListBySubscription.json
     */
    /**
     * Sample code: List dedicated HSM devices in a subscription including payment HSM.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void listDedicatedHSMDevicesInASubscriptionIncludingPaymentHSM(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.dedicatedHsms().list(null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-03-31/DedicatedHsm_ListBySubscription.json
     */
    /**
     * Sample code: List dedicated HSM devices in a subscription.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void listDedicatedHSMDevicesInASubscription(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.dedicatedHsms().list(null, com.azure.core.util.Context.NONE);
    }
}
```

### DedicatedHsm_ListByResourceGroup

```java
/**
 * Samples for DedicatedHsm ListByResourceGroup.
 */
public final class DedicatedHsmListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-03-31/DedicatedHsm_ListByResourceGroup.json
     */
    /**
     * Sample code: List dedicated HSM devices in a resource group.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void listDedicatedHSMDevicesInAResourceGroup(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.dedicatedHsms().listByResourceGroup("hsm-group", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-03-31/PaymentHsm_ListByResourceGroup.json
     */
    /**
     * Sample code: List dedicated HSM devices in a resource group including payment HSM.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void listDedicatedHSMDevicesInAResourceGroupIncludingPaymentHSM(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.dedicatedHsms().listByResourceGroup("hsm-group", null, com.azure.core.util.Context.NONE);
    }
}
```

### DedicatedHsm_ListOutboundNetworkDependenciesEndpoints

```java
/**
 * Samples for DedicatedHsm ListOutboundNetworkDependenciesEndpoints.
 */
public final class DedicatedHsmListOutboundNetworkDependenciesEndpointsSamples {
    /*
     * x-ms-original-file: 2025-03-31/GetOutboundNetworkDependenciesEndpointsList.json
     */
    /**
     * Sample code: List OutboundNetworkDependenciesEndpoints by Managed Cluster.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void listOutboundNetworkDependenciesEndpointsByManagedCluster(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.dedicatedHsms()
            .listOutboundNetworkDependenciesEndpoints("hsm-group", "hsm1", com.azure.core.util.Context.NONE);
    }
}
```

### DedicatedHsm_Update

```java
import com.azure.resourcemanager.hardwaresecuritymodules.models.DedicatedHsm;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for DedicatedHsm Update.
 */
public final class DedicatedHsmUpdateSamples {
    /*
     * x-ms-original-file: 2025-03-31/DedicatedHsm_Update.json
     */
    /**
     * Sample code: Update an existing dedicated HSM.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void updateAnExistingDedicatedHSM(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        DedicatedHsm resource = manager.dedicatedHsms()
            .getByResourceGroupWithResponse("hsm-group", "hsm1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("Dept", "hsm", "Environment", "dogfood", "Slice", "A")).apply();
    }

    /*
     * x-ms-original-file: 2025-03-31/PaymentHsm_Update.json
     */
    /**
     * Sample code: Update an existing payment HSM.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void updateAnExistingPaymentHSM(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        DedicatedHsm resource = manager.dedicatedHsms()
            .getByResourceGroupWithResponse("hsm-group", "hsm1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("Dept", "hsm", "Environment", "dogfood", "Slice", "A")).apply();
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

### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2025-03-31/OperationsList.json
     */
    /**
     * Sample code: Get a list of Payment HSM operations.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void getAListOfPaymentHSMOperations(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_ListByCloudHsmCluster

```java
/**
 * Samples for PrivateEndpointConnections ListByCloudHsmCluster.
 */
public final class PrivateEndpointConnectionsListByCloudHsmClusterSamples {
    /*
     * x-ms-original-file: 2025-03-31/CloudHsmClusterPrivateEndpointConnection_ListByCloudHsmCluster_MaximumSet_Gen.json
     */
    /**
     * Sample code: CloudHsmClusterPrivateEndpointConnection_ListByResource_MaximumSet_Gen.
     * 
     * @param manager Entry point to HardwareSecurityModulesManager.
     */
    public static void cloudHsmClusterPrivateEndpointConnectionListByResourceMaximumSetGen(
        com.azure.resourcemanager.hardwaresecuritymodules.HardwareSecurityModulesManager manager) {
        manager.privateEndpointConnections()
            .listByCloudHsmCluster("rgcloudhsm", "chsm1", com.azure.core.util.Context.NONE);
    }
}
```

