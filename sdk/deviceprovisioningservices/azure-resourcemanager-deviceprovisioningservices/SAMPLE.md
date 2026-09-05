# Code snippets and samples


## DpsCertificate

- [CreateOrUpdate](#dpscertificate_createorupdate)
- [Delete](#dpscertificate_delete)
- [GenerateVerificationCode](#dpscertificate_generateverificationcode)
- [Get](#dpscertificate_get)
- [List](#dpscertificate_list)
- [VerifyCertificate](#dpscertificate_verifycertificate)

## IotDpsResource

- [CheckProvisioningServiceNameAvailability](#iotdpsresource_checkprovisioningservicenameavailability)
- [CreateOrUpdate](#iotdpsresource_createorupdate)
- [CreateOrUpdatePrivateEndpointConnection](#iotdpsresource_createorupdateprivateendpointconnection)
- [Delete](#iotdpsresource_delete)
- [DeletePrivateEndpointConnection](#iotdpsresource_deleteprivateendpointconnection)
- [GetByResourceGroup](#iotdpsresource_getbyresourcegroup)
- [GetOperationResult](#iotdpsresource_getoperationresult)
- [GetPrivateEndpointConnection](#iotdpsresource_getprivateendpointconnection)
- [GetPrivateLinkResources](#iotdpsresource_getprivatelinkresources)
- [List](#iotdpsresource_list)
- [ListByResourceGroup](#iotdpsresource_listbyresourcegroup)
- [ListKeys](#iotdpsresource_listkeys)
- [ListKeysForKeyName](#iotdpsresource_listkeysforkeyname)
- [ListPrivateEndpointConnections](#iotdpsresource_listprivateendpointconnections)
- [ListPrivateLinkResources](#iotdpsresource_listprivatelinkresources)
- [ListValidSkus](#iotdpsresource_listvalidskus)
- [Update](#iotdpsresource_update)

## Operations

- [List](#operations_list)
### DpsCertificate_CreateOrUpdate

```java
import com.azure.resourcemanager.deviceprovisioningservices.models.CertificateProperties;

/**
 * Samples for DpsCertificate CreateOrUpdate.
 */
public final class DpsCertificateCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-08-31/DPSCertificateCreateOrUpdate.json
     */
    /**
     * Sample code: DPSCreateOrUpdateCertificate.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void
        dPSCreateOrUpdateCertificate(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.dpsCertificates()
            .define("cert")
            .withExistingProvisioningService("myResourceGroup", "myFirstProvisioningService")
            .withProperties(new CertificateProperties().withCertificate("MA==".getBytes()))
            .create();
    }
}
```

### DpsCertificate_Delete

```java

/**
 * Samples for DpsCertificate Delete.
 */
public final class DpsCertificateDeleteSamples {
    /*
     * x-ms-original-file: 2026-08-31/DPSDeleteCertificate.json
     */
    /**
     * Sample code: DPSDeleteCertificate.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void
        dPSDeleteCertificate(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.dpsCertificates()
            .deleteWithResponse("myResourceGroup", "AAAAAAAADGk=", "myFirstProvisioningService", "cert", null, null,
                null, null, null, null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### DpsCertificate_GenerateVerificationCode

```java

/**
 * Samples for DpsCertificate GenerateVerificationCode.
 */
public final class DpsCertificateGenerateVerificationCodeSamples {
    /*
     * x-ms-original-file: 2026-08-31/DPSGenerateVerificationCode.json
     */
    /**
     * Sample code: DPSGenerateVerificationCode.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void
        dPSGenerateVerificationCode(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.dpsCertificates()
            .generateVerificationCodeWithResponse("cert", "AAAAAAAADGk=", "myResourceGroup",
                "myFirstProvisioningService", null, null, null, null, null, null, null, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### DpsCertificate_Get

```java
/**
 * Samples for DpsCertificate Get.
 */
public final class DpsCertificateGetSamples {
    /*
     * x-ms-original-file: 2026-08-31/DPSGetCertificate.json
     */
    /**
     * Sample code: DPSGetCertificate.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSGetCertificate(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.dpsCertificates()
            .getWithResponse("cert", "myResourceGroup", "myFirstProvisioningService", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### DpsCertificate_List

```java
/**
 * Samples for DpsCertificate List.
 */
public final class DpsCertificateListSamples {
    /*
     * x-ms-original-file: 2026-08-31/DPSGetCertificates.json
     */
    /**
     * Sample code: DPSGetCertificates.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSGetCertificates(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.dpsCertificates()
            .list("myResourceGroup", "myFirstProvisioningService", com.azure.core.util.Context.NONE);
    }
}
```

### DpsCertificate_VerifyCertificate

```java
import com.azure.resourcemanager.deviceprovisioningservices.models.VerificationCodeRequest;

/**
 * Samples for DpsCertificate VerifyCertificate.
 */
public final class DpsCertificateVerifyCertificateSamples {
    /*
     * x-ms-original-file: 2026-08-31/DPSVerifyCertificate.json
     */
    /**
     * Sample code: DPSVerifyCertificate.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void
        dPSVerifyCertificate(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.dpsCertificates()
            .verifyCertificateWithResponse("cert", "AAAAAAAADGk=", "myResourceGroup", "myFirstProvisioningService",
                new VerificationCodeRequest().withCertificate("#####################################"), null, null,
                null, null, null, null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### IotDpsResource_CheckProvisioningServiceNameAvailability

```java
import com.azure.resourcemanager.deviceprovisioningservices.models.OperationInputs;

/**
 * Samples for IotDpsResource CheckProvisioningServiceNameAvailability.
 */
public final class IotDpsResourceCheckProvisioningServiceNameAvailabiSamples {
    /*
     * x-ms-original-file: 2026-08-31/DPSCheckNameAvailability.json
     */
    /**
     * Sample code: DPSCheckName.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSCheckName(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.iotDpsResources()
            .checkProvisioningServiceNameAvailabilityWithResponse(new OperationInputs().withName("test213123"),
                com.azure.core.util.Context.NONE);
    }
}
```

### IotDpsResource_CreateOrUpdate

```java
import com.azure.resourcemanager.deviceprovisioningservices.models.IotDpsPropertiesDescription;
import com.azure.resourcemanager.deviceprovisioningservices.models.IotDpsSku;
import com.azure.resourcemanager.deviceprovisioningservices.models.IotDpsSkuInfo;
import com.azure.resourcemanager.deviceprovisioningservices.models.IotHubAuthenticationType;
import com.azure.resourcemanager.deviceprovisioningservices.models.IotHubDefinitionDescription;
import com.azure.resourcemanager.deviceprovisioningservices.models.ManagedServiceIdentity;
import com.azure.resourcemanager.deviceprovisioningservices.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.deviceprovisioningservices.models.PublicNetworkAccess;
import com.azure.resourcemanager.deviceprovisioningservices.models.UserAssignedIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for IotDpsResource CreateOrUpdate.
 */
public final class IotDpsResourceCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-08-31/DPSCreate_DisableLocalAuthTrue.json
     */
    /**
     * Sample code: DPSCreate_DisableLocalAuthTrue.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void
        dPSCreateDisableLocalAuthTrue(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.iotDpsResources()
            .define("myFirstProvisioningService")
            .withRegion("East US")
            .withExistingResourceGroup("myResourceGroup")
            .withProperties(new IotDpsPropertiesDescription().withPublicNetworkAccess(PublicNetworkAccess.ENABLED)
                .withDisableLocalAuth(true))
            .withSku(new IotDpsSkuInfo().withName(IotDpsSku.S1).withCapacity(1L))
            .withTags(mapOf("key1", "fakeTokenPlaceholder", "key2", "fakeTokenPlaceholder"))
            .create();
    }

    /*
     * x-ms-original-file: 2026-08-31/DPSUpdate_DisableLocalAuth.json
     */
    /**
     * Sample code: DPSUpdate_DisableLocalAuth.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void
        dPSUpdateDisableLocalAuth(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.iotDpsResources()
            .define("myFirstProvisioningService")
            .withRegion("East US")
            .withExistingResourceGroup("myResourceGroup")
            .withProperties(new IotDpsPropertiesDescription().withDisableLocalAuth(true))
            .withSku(new IotDpsSkuInfo().withName(IotDpsSku.S1).withCapacity(1L))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: 2026-08-31/DPSUpdate.json
     */
    /**
     * Sample code: DPSUpdate.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSUpdate(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.iotDpsResources()
            .define("myFirstProvisioningService")
            .withRegion("East US")
            .withExistingResourceGroup("myResourceGroup")
            .withProperties(new IotDpsPropertiesDescription().withEnableDataResidency(false))
            .withSku(new IotDpsSkuInfo().withName(IotDpsSku.S1).withCapacity(1L))
            .withTags(mapOf())
            .withIdentity(new ManagedServiceIdentity()
                .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/91d12660-3dec-467a-be2a-213b5544ddc0/resourcegroups/testrg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/testidentity",
                    new UserAssignedIdentity())))
            .create();
    }

    /*
     * x-ms-original-file: 2026-08-31/DPSCreate.json
     */
    /**
     * Sample code: DPSCreate.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSCreate(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.iotDpsResources()
            .define("myFirstProvisioningService")
            .withRegion("East US")
            .withExistingResourceGroup("myResourceGroup")
            .withProperties(new IotDpsPropertiesDescription().withEnableDataResidency(false))
            .withSku(new IotDpsSkuInfo().withName(IotDpsSku.S1).withCapacity(1L))
            .withTags(mapOf())
            .create();
    }

    /*
     * x-ms-original-file: 2026-08-31/DPSCreateWithIotHub.json
     */
    /**
     * Sample code: DPSCreateWithIotHub.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSCreateWithIotHub(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.iotDpsResources()
            .define("myFirstProvisioningService")
            .withRegion("East US")
            .withExistingResourceGroup("myResourceGroup")
            .withProperties(new IotDpsPropertiesDescription()
                .withIotHubs(Arrays.asList(new IotHubDefinitionDescription().withApplyAllocationPolicy(true)
                    .withAllocationWeight(1)
                    .withHostName("myFirstIoTHub.azure-devices.net")
                    .withAuthenticationType(IotHubAuthenticationType.USER_ASSIGNED)
                    .withSelectedUserAssignedIdentityResourceId(
                        "/subscriptions/abcf2d55-764f-419f-974d-f77a55c2ce55/resourcegroups/my-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/my-mi-1")
                    .withLocation("eastus")))
                .withEnableDataResidency(false))
            .withSku(new IotDpsSkuInfo().withName(IotDpsSku.S1).withCapacity(1L))
            .withTags(mapOf())
            .withIdentity(new ManagedServiceIdentity()
                .withType(ManagedServiceIdentityType.fromString("SystemAssigned, UserAssigned"))
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/abcf2d55-764f-419f-974d-f77a55c2ce55/resourcegroups/my-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/my-mi-1",
                    new UserAssignedIdentity())))
            .create();
    }

    /*
     * x-ms-original-file: 2026-08-31/DPSCreate_DisableLocalAuthFalse.json
     */
    /**
     * Sample code: DPSCreate_DisableLocalAuthFalse.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void
        dPSCreateDisableLocalAuthFalse(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.iotDpsResources()
            .define("myFirstProvisioningService")
            .withRegion("East US")
            .withExistingResourceGroup("myResourceGroup")
            .withProperties(new IotDpsPropertiesDescription().withPublicNetworkAccess(PublicNetworkAccess.ENABLED)
                .withDisableLocalAuth(false))
            .withSku(new IotDpsSkuInfo().withName(IotDpsSku.S1).withCapacity(1L))
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
            .create();
    }

    /*
     * x-ms-original-file: 2026-08-31/DPSCreateWithNamespace.json
     */
    /**
     * Sample code: DPSCreateWithNamespace.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void
        dPSCreateWithNamespace(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.iotDpsResources()
            .define("myFirstProvisioningService")
            .withRegion("East US")
            .withExistingResourceGroup("myResourceGroup")
            .withProperties(new IotDpsPropertiesDescription().withEnableDataResidency(false))
            .withSku(new IotDpsSkuInfo().withName(IotDpsSku.S1).withCapacity(1L))
            .withTags(mapOf())
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

### IotDpsResource_CreateOrUpdatePrivateEndpointConnection

```java
import com.azure.resourcemanager.deviceprovisioningservices.models.PrivateEndpointConnectionProperties;
import com.azure.resourcemanager.deviceprovisioningservices.models.PrivateLinkServiceConnectionState;
import com.azure.resourcemanager.deviceprovisioningservices.models.PrivateLinkServiceConnectionStatus;

/**
 * Samples for IotDpsResource CreateOrUpdatePrivateEndpointConnection.
 */
public final class IotDpsResourceCreateOrUpdatePrivateEndpointConnectSamples {
    /*
     * x-ms-original-file: 2026-08-31/DPSCreateOrUpdatePrivateEndpointConnection.json
     */
    /**
     * Sample code: PrivateEndpointConnection_CreateOrUpdate.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void privateEndpointConnectionCreateOrUpdate(
        com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.iotDpsResources()
            .definePrivateEndpointConnection("myPrivateEndpointConnection")
            .withExistingProvisioningService("myResourceGroup", "myFirstProvisioningService")
            .withProperties(new PrivateEndpointConnectionProperties().withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState().withStatus(PrivateLinkServiceConnectionStatus.APPROVED)
                    .withDescription("Approved by johndoe@contoso.com")))
            .create();
    }
}
```

### IotDpsResource_Delete

```java
/**
 * Samples for IotDpsResource Delete.
 */
public final class IotDpsResourceDeleteSamples {
    /*
     * x-ms-original-file: 2026-08-31/DPSDelete.json
     */
    /**
     * Sample code: DPSDelete.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSDelete(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.iotDpsResources()
            .delete("myResourceGroup", "myFirstProvisioningService", com.azure.core.util.Context.NONE);
    }
}
```

### IotDpsResource_DeletePrivateEndpointConnection

```java
/**
 * Samples for IotDpsResource DeletePrivateEndpointConnection.
 */
public final class IotDpsResourceDeletePrivateEndpointConnectionSamples {
    /*
     * x-ms-original-file: 2026-08-31/DPSDeletePrivateEndpointConnection.json
     */
    /**
     * Sample code: PrivateEndpointConnection_Delete.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void
        privateEndpointConnectionDelete(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.iotDpsResources()
            .deletePrivateEndpointConnection("myResourceGroup", "myFirstProvisioningService",
                "myPrivateEndpointConnection", com.azure.core.util.Context.NONE);
    }
}
```

### IotDpsResource_GetByResourceGroup

```java
/**
 * Samples for IotDpsResource GetByResourceGroup.
 */
public final class IotDpsResourceGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-08-31/DPSGet_DisableLocalAuth.json
     */
    /**
     * Sample code: DPSGet_DisableLocalAuth.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void
        dPSGetDisableLocalAuth(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.iotDpsResources()
            .getByResourceGroupWithResponse("myResourceGroup", "myFirstProvisioningService",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-08-31/DPSGet.json
     */
    /**
     * Sample code: DPSGet.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSGet(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.iotDpsResources()
            .getByResourceGroupWithResponse("myResourceGroup", "myFirstProvisioningService",
                com.azure.core.util.Context.NONE);
    }
}
```

### IotDpsResource_GetOperationResult

```java
/**
 * Samples for IotDpsResource GetOperationResult.
 */
public final class IotDpsResourceGetOperationResultSamples {
    /*
     * x-ms-original-file: 2026-08-31/DPSGetOperationResult.json
     */
    /**
     * Sample code: DPSGetOperationResult.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void
        dPSGetOperationResult(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.iotDpsResources()
            .getOperationResultWithResponse("MTY5OTNmZDctODI5Yy00N2E2LTkxNDQtMDU1NGIyYzY1ZjRl", "myResourceGroup",
                "myFirstProvisioningService", "1508265712453", com.azure.core.util.Context.NONE);
    }
}
```

### IotDpsResource_GetPrivateEndpointConnection

```java
/**
 * Samples for IotDpsResource GetPrivateEndpointConnection.
 */
public final class IotDpsResourceGetPrivateEndpointConnectionSamples {
    /*
     * x-ms-original-file: 2026-08-31/DPSGetPrivateEndpointConnection.json
     */
    /**
     * Sample code: PrivateEndpointConnection_Get.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void
        privateEndpointConnectionGet(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.iotDpsResources()
            .getPrivateEndpointConnectionWithResponse("myResourceGroup", "myFirstProvisioningService",
                "myPrivateEndpointConnection", com.azure.core.util.Context.NONE);
    }
}
```

### IotDpsResource_GetPrivateLinkResources

```java
/**
 * Samples for IotDpsResource GetPrivateLinkResources.
 */
public final class IotDpsResourceGetPrivateLinkResourcesSamples {
    /*
     * x-ms-original-file: 2026-08-31/DPSGetPrivateLinkResources.json
     */
    /**
     * Sample code: PrivateLinkResources_List.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void
        privateLinkResourcesList(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.iotDpsResources()
            .getPrivateLinkResourcesWithResponse("myResourceGroup", "myFirstProvisioningService", "iotDps",
                com.azure.core.util.Context.NONE);
    }
}
```

### IotDpsResource_List

```java
/**
 * Samples for IotDpsResource List.
 */
public final class IotDpsResourceListSamples {
    /*
     * x-ms-original-file: 2026-08-31/DPSListBySubscription.json
     */
    /**
     * Sample code: DPSListBySubscription.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void
        dPSListBySubscription(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.iotDpsResources().list(com.azure.core.util.Context.NONE);
    }
}
```

### IotDpsResource_ListByResourceGroup

```java
/**
 * Samples for IotDpsResource ListByResourceGroup.
 */
public final class IotDpsResourceListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-08-31/DPSListByResourceGroup.json
     */
    /**
     * Sample code: DPSListByResourceGroup.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void
        dPSListByResourceGroup(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.iotDpsResources().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### IotDpsResource_ListKeys

```java
/**
 * Samples for IotDpsResource ListKeys.
 */
public final class IotDpsResourceListKeysSamples {
    /*
     * x-ms-original-file: 2026-08-31/DPSListKeys.json
     */
    /**
     * Sample code: DPSListKeys.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSListKeys(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.iotDpsResources()
            .listKeys("myFirstProvisioningService", "myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### IotDpsResource_ListKeysForKeyName

```java
/**
 * Samples for IotDpsResource ListKeysForKeyName.
 */
public final class IotDpsResourceListKeysForKeyNameSamples {
    /*
     * x-ms-original-file: 2026-08-31/DPSGetKey.json
     */
    /**
     * Sample code: DPSGetKey.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSGetKey(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.iotDpsResources()
            .listKeysForKeyNameWithResponse("myFirstProvisioningService", "testKey", "myResourceGroup",
                com.azure.core.util.Context.NONE);
    }
}
```

### IotDpsResource_ListPrivateEndpointConnections

```java
/**
 * Samples for IotDpsResource ListPrivateEndpointConnections.
 */
public final class IotDpsResourceListPrivateEndpointConnectionsSamples {
    /*
     * x-ms-original-file: 2026-08-31/DPSListPrivateEndpointConnections.json
     */
    /**
     * Sample code: PrivateEndpointConnections_List.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void
        privateEndpointConnectionsList(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.iotDpsResources()
            .listPrivateEndpointConnectionsWithResponse("myResourceGroup", "myFirstProvisioningService",
                com.azure.core.util.Context.NONE);
    }
}
```

### IotDpsResource_ListPrivateLinkResources

```java
/**
 * Samples for IotDpsResource ListPrivateLinkResources.
 */
public final class IotDpsResourceListPrivateLinkResourcesSamples {
    /*
     * x-ms-original-file: 2026-08-31/DPSListPrivateLinkResources.json
     */
    /**
     * Sample code: PrivateLinkResources_List.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void
        privateLinkResourcesList(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.iotDpsResources()
            .listPrivateLinkResources("myResourceGroup", "myFirstProvisioningService",
                com.azure.core.util.Context.NONE);
    }
}
```

### IotDpsResource_ListValidSkus

```java
/**
 * Samples for IotDpsResource ListValidSkus.
 */
public final class IotDpsResourceListValidSkusSamples {
    /*
     * x-ms-original-file: 2026-08-31/DPSGetValidSku.json
     */
    /**
     * Sample code: DPSGetValidSku.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSGetValidSku(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.iotDpsResources()
            .listValidSkus("myFirstProvisioningService", "myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### IotDpsResource_Update

```java
import com.azure.resourcemanager.deviceprovisioningservices.models.ProvisioningServiceDescription;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for IotDpsResource Update.
 */
public final class IotDpsResourceUpdateSamples {
    /*
     * x-ms-original-file: 2026-08-31/DPSPatch_DisableLocalAuth.json
     */
    /**
     * Sample code: DPSPatch_DisableLocalAuth.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void
        dPSPatchDisableLocalAuth(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        ProvisioningServiceDescription resource = manager.iotDpsResources()
            .getByResourceGroupWithResponse("myResourceGroup", "myFirstProvisioningService",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("foo", "bar")).apply();
    }

    /*
     * x-ms-original-file: 2026-08-31/DPSPatch.json
     */
    /**
     * Sample code: DPSPatch.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSPatch(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        ProvisioningServiceDescription resource = manager.iotDpsResources()
            .getByResourceGroupWithResponse("myResourceGroup", "myFirstProvisioningService",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("foo", "bar")).apply();
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
     * x-ms-original-file: 2026-08-31/DPSOperations.json
     */
    /**
     * Sample code: DPSOperations.
     * 
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSOperations(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

