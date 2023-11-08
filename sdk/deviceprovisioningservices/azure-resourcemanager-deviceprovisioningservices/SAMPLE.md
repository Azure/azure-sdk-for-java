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

/** Samples for DpsCertificate CreateOrUpdate. */
public final class DpsCertificateCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/deviceprovisioningservices/resource-manager/Microsoft.Devices/stable/2022-02-05/examples/DPSCertificateCreateOrUpdate.json
     */
    /**
     * Sample code: DPSCreateOrUpdateCertificate.
     *
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSCreateOrUpdateCertificate(
        com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager
            .dpsCertificates()
            .define("cert")
            .withExistingProvisioningService("myResourceGroup", "myFirstProvisioningService")
            .withProperties(
                new CertificateProperties().withCertificate("############################################".getBytes()))
            .create();
    }
}
```

### DpsCertificate_Delete

```java
import com.azure.core.util.Context;

/** Samples for DpsCertificate Delete. */
public final class DpsCertificateDeleteSamples {
    /*
     * x-ms-original-file: specification/deviceprovisioningservices/resource-manager/Microsoft.Devices/stable/2022-02-05/examples/DPSDeleteCertificate.json
     */
    /**
     * Sample code: DPSDeleteCertificate.
     *
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSDeleteCertificate(
        com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager
            .dpsCertificates()
            .deleteWithResponse(
                "myResourceGroup",
                "AAAAAAAADGk=",
                "myFirstProvisioningService",
                "cert",
                null,
                new byte[0],
                null,
                null,
                null,
                null,
                null,
                null,
                Context.NONE);
    }
}
```

### DpsCertificate_GenerateVerificationCode

```java
import com.azure.core.util.Context;

/** Samples for DpsCertificate GenerateVerificationCode. */
public final class DpsCertificateGenerateVerificationCodeSamples {
    /*
     * x-ms-original-file: specification/deviceprovisioningservices/resource-manager/Microsoft.Devices/stable/2022-02-05/examples/DPSGenerateVerificationCode.json
     */
    /**
     * Sample code: DPSGenerateVerificationCode.
     *
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSGenerateVerificationCode(
        com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager
            .dpsCertificates()
            .generateVerificationCodeWithResponse(
                "cert",
                "AAAAAAAADGk=",
                "myResourceGroup",
                "myFirstProvisioningService",
                null,
                new byte[0],
                null,
                null,
                null,
                null,
                null,
                null,
                Context.NONE);
    }
}
```

### DpsCertificate_Get

```java
import com.azure.core.util.Context;

/** Samples for DpsCertificate Get. */
public final class DpsCertificateGetSamples {
    /*
     * x-ms-original-file: specification/deviceprovisioningservices/resource-manager/Microsoft.Devices/stable/2022-02-05/examples/DPSGetCertificate.json
     */
    /**
     * Sample code: DPSGetCertificate.
     *
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSGetCertificate(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager
            .dpsCertificates()
            .getWithResponse("cert", "myResourceGroup", "myFirstProvisioningService", null, Context.NONE);
    }
}
```

### DpsCertificate_List

```java
import com.azure.core.util.Context;

/** Samples for DpsCertificate List. */
public final class DpsCertificateListSamples {
    /*
     * x-ms-original-file: specification/deviceprovisioningservices/resource-manager/Microsoft.Devices/stable/2022-02-05/examples/DPSGetCertificates.json
     */
    /**
     * Sample code: DPSGetCertificates.
     *
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSGetCertificates(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.dpsCertificates().listWithResponse("myResourceGroup", "myFirstProvisioningService", Context.NONE);
    }
}
```

### DpsCertificate_VerifyCertificate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.deviceprovisioningservices.models.VerificationCodeRequest;

/** Samples for DpsCertificate VerifyCertificate. */
public final class DpsCertificateVerifyCertificateSamples {
    /*
     * x-ms-original-file: specification/deviceprovisioningservices/resource-manager/Microsoft.Devices/stable/2022-02-05/examples/DPSVerifyCertificate.json
     */
    /**
     * Sample code: DPSVerifyCertificate.
     *
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSVerifyCertificate(
        com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager
            .dpsCertificates()
            .verifyCertificateWithResponse(
                "cert",
                "AAAAAAAADGk=",
                "myResourceGroup",
                "myFirstProvisioningService",
                new VerificationCodeRequest().withCertificate("#####################################"),
                null,
                new byte[0],
                null,
                null,
                null,
                null,
                null,
                null,
                Context.NONE);
    }
}
```

### IotDpsResource_CheckProvisioningServiceNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.deviceprovisioningservices.models.OperationInputs;

/** Samples for IotDpsResource CheckProvisioningServiceNameAvailability. */
public final class IotDpsResourceCheckProvisioningServiceNameAvailabilit {
    /*
     * x-ms-original-file: specification/deviceprovisioningservices/resource-manager/Microsoft.Devices/stable/2022-02-05/examples/DPSCheckNameAvailability.json
     */
    /**
     * Sample code: DPSCheckName.
     *
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSCheckName(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager
            .iotDpsResources()
            .checkProvisioningServiceNameAvailabilityWithResponse(
                new OperationInputs().withName("test213123"), Context.NONE);
    }
}
```

### IotDpsResource_CreateOrUpdate

```java
import com.azure.resourcemanager.deviceprovisioningservices.models.IotDpsPropertiesDescription;
import com.azure.resourcemanager.deviceprovisioningservices.models.IotDpsSku;
import com.azure.resourcemanager.deviceprovisioningservices.models.IotDpsSkuInfo;
import java.util.HashMap;
import java.util.Map;

/** Samples for IotDpsResource CreateOrUpdate. */
public final class IotDpsResourceCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/deviceprovisioningservices/resource-manager/Microsoft.Devices/stable/2022-02-05/examples/DPSCreate.json
     */
    /**
     * Sample code: DPSCreate.
     *
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSCreate(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager
            .iotDpsResources()
            .define("myFirstProvisioningService")
            .withRegion("East US")
            .withExistingResourceGroup("myResourceGroup")
            .withProperties(new IotDpsPropertiesDescription().withEnableDataResidency(false))
            .withSku(new IotDpsSkuInfo().withName(IotDpsSku.S1).withCapacity(1L))
            .withTags(mapOf())
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

### IotDpsResource_CreateOrUpdatePrivateEndpointConnection

```java
import com.azure.resourcemanager.deviceprovisioningservices.models.PrivateEndpointConnectionProperties;
import com.azure.resourcemanager.deviceprovisioningservices.models.PrivateLinkServiceConnectionState;
import com.azure.resourcemanager.deviceprovisioningservices.models.PrivateLinkServiceConnectionStatus;

/** Samples for IotDpsResource CreateOrUpdatePrivateEndpointConnection. */
public final class IotDpsResourceCreateOrUpdatePrivateEndpointConnection {
    /*
     * x-ms-original-file: specification/deviceprovisioningservices/resource-manager/Microsoft.Devices/stable/2022-02-05/examples/DPSCreateOrUpdatePrivateEndpointConnection.json
     */
    /**
     * Sample code: PrivateEndpointConnection_CreateOrUpdate.
     *
     * @param manager Entry point to IotDpsManager.
     */
    public static void privateEndpointConnectionCreateOrUpdate(
        com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager
            .iotDpsResources()
            .definePrivateEndpointConnection("myPrivateEndpointConnection")
            .withExistingProvisioningService("myResourceGroup", "myFirstProvisioningService")
            .withProperties(
                new PrivateEndpointConnectionProperties()
                    .withPrivateLinkServiceConnectionState(
                        new PrivateLinkServiceConnectionState()
                            .withStatus(PrivateLinkServiceConnectionStatus.APPROVED)
                            .withDescription("Approved by johndoe@contoso.com")))
            .create();
    }
}
```

### IotDpsResource_Delete

```java
import com.azure.core.util.Context;

/** Samples for IotDpsResource Delete. */
public final class IotDpsResourceDeleteSamples {
    /*
     * x-ms-original-file: specification/deviceprovisioningservices/resource-manager/Microsoft.Devices/stable/2022-02-05/examples/DPSDelete.json
     */
    /**
     * Sample code: DPSDelete.
     *
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSDelete(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.iotDpsResources().delete("myResourceGroup", "myFirstProvisioningService", Context.NONE);
    }
}
```

### IotDpsResource_DeletePrivateEndpointConnection

```java
import com.azure.core.util.Context;

/** Samples for IotDpsResource DeletePrivateEndpointConnection. */
public final class IotDpsResourceDeletePrivateEndpointConnectionSamples {
    /*
     * x-ms-original-file: specification/deviceprovisioningservices/resource-manager/Microsoft.Devices/stable/2022-02-05/examples/DPSDeletePrivateEndpointConnection.json
     */
    /**
     * Sample code: PrivateEndpointConnection_Delete.
     *
     * @param manager Entry point to IotDpsManager.
     */
    public static void privateEndpointConnectionDelete(
        com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager
            .iotDpsResources()
            .deletePrivateEndpointConnection(
                "myResourceGroup", "myFirstProvisioningService", "myPrivateEndpointConnection", Context.NONE);
    }
}
```

### IotDpsResource_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for IotDpsResource GetByResourceGroup. */
public final class IotDpsResourceGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/deviceprovisioningservices/resource-manager/Microsoft.Devices/stable/2022-02-05/examples/DPSGet.json
     */
    /**
     * Sample code: DPSGet.
     *
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSGet(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager
            .iotDpsResources()
            .getByResourceGroupWithResponse("myResourceGroup", "myFirstProvisioningService", Context.NONE);
    }
}
```

### IotDpsResource_GetOperationResult

```java
import com.azure.core.util.Context;

/** Samples for IotDpsResource GetOperationResult. */
public final class IotDpsResourceGetOperationResultSamples {
    /*
     * x-ms-original-file: specification/deviceprovisioningservices/resource-manager/Microsoft.Devices/stable/2022-02-05/examples/DPSGetOperationResult.json
     */
    /**
     * Sample code: DPSGetOperationResult.
     *
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSGetOperationResult(
        com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager
            .iotDpsResources()
            .getOperationResultWithResponse(
                "MTY5OTNmZDctODI5Yy00N2E2LTkxNDQtMDU1NGIyYzY1ZjRl",
                "myResourceGroup",
                "myFirstProvisioningService",
                "1508265712453",
                Context.NONE);
    }
}
```

### IotDpsResource_GetPrivateEndpointConnection

```java
import com.azure.core.util.Context;

/** Samples for IotDpsResource GetPrivateEndpointConnection. */
public final class IotDpsResourceGetPrivateEndpointConnectionSamples {
    /*
     * x-ms-original-file: specification/deviceprovisioningservices/resource-manager/Microsoft.Devices/stable/2022-02-05/examples/DPSGetPrivateEndpointConnection.json
     */
    /**
     * Sample code: PrivateEndpointConnection_Get.
     *
     * @param manager Entry point to IotDpsManager.
     */
    public static void privateEndpointConnectionGet(
        com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager
            .iotDpsResources()
            .getPrivateEndpointConnectionWithResponse(
                "myResourceGroup", "myFirstProvisioningService", "myPrivateEndpointConnection", Context.NONE);
    }
}
```

### IotDpsResource_GetPrivateLinkResources

```java
import com.azure.core.util.Context;

/** Samples for IotDpsResource GetPrivateLinkResources. */
public final class IotDpsResourceGetPrivateLinkResourcesSamples {
    /*
     * x-ms-original-file: specification/deviceprovisioningservices/resource-manager/Microsoft.Devices/stable/2022-02-05/examples/DPSGetPrivateLinkResources.json
     */
    /**
     * Sample code: PrivateLinkResources_List.
     *
     * @param manager Entry point to IotDpsManager.
     */
    public static void privateLinkResourcesList(
        com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager
            .iotDpsResources()
            .getPrivateLinkResourcesWithResponse(
                "myResourceGroup", "myFirstProvisioningService", "iotDps", Context.NONE);
    }
}
```

### IotDpsResource_List

```java
import com.azure.core.util.Context;

/** Samples for IotDpsResource List. */
public final class IotDpsResourceListSamples {
    /*
     * x-ms-original-file: specification/deviceprovisioningservices/resource-manager/Microsoft.Devices/stable/2022-02-05/examples/DPSListBySubscription.json
     */
    /**
     * Sample code: DPSListBySubscription.
     *
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSListBySubscription(
        com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.iotDpsResources().list(Context.NONE);
    }
}
```

### IotDpsResource_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for IotDpsResource ListByResourceGroup. */
public final class IotDpsResourceListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/deviceprovisioningservices/resource-manager/Microsoft.Devices/stable/2022-02-05/examples/DPSListByResourceGroup.json
     */
    /**
     * Sample code: DPSListByResourceGroup.
     *
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSListByResourceGroup(
        com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.iotDpsResources().listByResourceGroup("myResourceGroup", Context.NONE);
    }
}
```

### IotDpsResource_ListKeys

```java
import com.azure.core.util.Context;

/** Samples for IotDpsResource ListKeys. */
public final class IotDpsResourceListKeysSamples {
    /*
     * x-ms-original-file: specification/deviceprovisioningservices/resource-manager/Microsoft.Devices/stable/2022-02-05/examples/DPSListKeys.json
     */
    /**
     * Sample code: DPSListKeys.
     *
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSListKeys(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.iotDpsResources().listKeys("myFirstProvisioningService", "myResourceGroup", Context.NONE);
    }
}
```

### IotDpsResource_ListKeysForKeyName

```java
import com.azure.core.util.Context;

/** Samples for IotDpsResource ListKeysForKeyName. */
public final class IotDpsResourceListKeysForKeyNameSamples {
    /*
     * x-ms-original-file: specification/deviceprovisioningservices/resource-manager/Microsoft.Devices/stable/2022-02-05/examples/DPSGetKey.json
     */
    /**
     * Sample code: DPSGetKey.
     *
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSGetKey(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager
            .iotDpsResources()
            .listKeysForKeyNameWithResponse("myFirstProvisioningService", "testKey", "myResourceGroup", Context.NONE);
    }
}
```

### IotDpsResource_ListPrivateEndpointConnections

```java
import com.azure.core.util.Context;

/** Samples for IotDpsResource ListPrivateEndpointConnections. */
public final class IotDpsResourceListPrivateEndpointConnectionsSamples {
    /*
     * x-ms-original-file: specification/deviceprovisioningservices/resource-manager/Microsoft.Devices/stable/2022-02-05/examples/DPSListPrivateEndpointConnections.json
     */
    /**
     * Sample code: PrivateEndpointConnections_List.
     *
     * @param manager Entry point to IotDpsManager.
     */
    public static void privateEndpointConnectionsList(
        com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager
            .iotDpsResources()
            .listPrivateEndpointConnectionsWithResponse("myResourceGroup", "myFirstProvisioningService", Context.NONE);
    }
}
```

### IotDpsResource_ListPrivateLinkResources

```java
import com.azure.core.util.Context;

/** Samples for IotDpsResource ListPrivateLinkResources. */
public final class IotDpsResourceListPrivateLinkResourcesSamples {
    /*
     * x-ms-original-file: specification/deviceprovisioningservices/resource-manager/Microsoft.Devices/stable/2022-02-05/examples/DPSListPrivateLinkResources.json
     */
    /**
     * Sample code: PrivateLinkResources_List.
     *
     * @param manager Entry point to IotDpsManager.
     */
    public static void privateLinkResourcesList(
        com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager
            .iotDpsResources()
            .listPrivateLinkResourcesWithResponse("myResourceGroup", "myFirstProvisioningService", Context.NONE);
    }
}
```

### IotDpsResource_ListValidSkus

```java
import com.azure.core.util.Context;

/** Samples for IotDpsResource ListValidSkus. */
public final class IotDpsResourceListValidSkusSamples {
    /*
     * x-ms-original-file: specification/deviceprovisioningservices/resource-manager/Microsoft.Devices/stable/2022-02-05/examples/DPSGetValidSku.json
     */
    /**
     * Sample code: DPSGetValidSku.
     *
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSGetValidSku(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.iotDpsResources().listValidSkus("myFirstProvisioningService", "myResourceGroup", Context.NONE);
    }
}
```

### IotDpsResource_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.deviceprovisioningservices.models.ProvisioningServiceDescription;
import java.util.HashMap;
import java.util.Map;

/** Samples for IotDpsResource Update. */
public final class IotDpsResourceUpdateSamples {
    /*
     * x-ms-original-file: specification/deviceprovisioningservices/resource-manager/Microsoft.Devices/stable/2022-02-05/examples/DPSPatch.json
     */
    /**
     * Sample code: DPSPatch.
     *
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSPatch(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        ProvisioningServiceDescription resource =
            manager
                .iotDpsResources()
                .getByResourceGroupWithResponse("myResourceGroup", "myFirstProvisioningService", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("foo", "bar")).apply();
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

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/deviceprovisioningservices/resource-manager/Microsoft.Devices/stable/2022-02-05/examples/DPSOperations.json
     */
    /**
     * Sample code: DPSOperations.
     *
     * @param manager Entry point to IotDpsManager.
     */
    public static void dPSOperations(com.azure.resourcemanager.deviceprovisioningservices.IotDpsManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

