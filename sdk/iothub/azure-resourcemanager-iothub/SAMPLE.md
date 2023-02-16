# Code snippets and samples


## Certificates

- [CreateOrUpdate](#certificates_createorupdate)
- [Delete](#certificates_delete)
- [GenerateVerificationCode](#certificates_generateverificationcode)
- [Get](#certificates_get)
- [ListByIotHub](#certificates_listbyiothub)
- [Verify](#certificates_verify)

## IotHub

- [ManualFailover](#iothub_manualfailover)

## IotHubResource

- [CheckNameAvailability](#iothubresource_checknameavailability)
- [CreateEventHubConsumerGroup](#iothubresource_createeventhubconsumergroup)
- [CreateOrUpdate](#iothubresource_createorupdate)
- [Delete](#iothubresource_delete)
- [DeleteEventHubConsumerGroup](#iothubresource_deleteeventhubconsumergroup)
- [ExportDevices](#iothubresource_exportdevices)
- [GetByResourceGroup](#iothubresource_getbyresourcegroup)
- [GetEndpointHealth](#iothubresource_getendpointhealth)
- [GetEventHubConsumerGroup](#iothubresource_geteventhubconsumergroup)
- [GetJob](#iothubresource_getjob)
- [GetKeysForKeyName](#iothubresource_getkeysforkeyname)
- [GetQuotaMetrics](#iothubresource_getquotametrics)
- [GetStats](#iothubresource_getstats)
- [GetValidSkus](#iothubresource_getvalidskus)
- [ImportDevices](#iothubresource_importdevices)
- [List](#iothubresource_list)
- [ListByResourceGroup](#iothubresource_listbyresourcegroup)
- [ListEventHubConsumerGroups](#iothubresource_listeventhubconsumergroups)
- [ListJobs](#iothubresource_listjobs)
- [ListKeys](#iothubresource_listkeys)
- [TestAllRoutes](#iothubresource_testallroutes)
- [TestRoute](#iothubresource_testroute)
- [Update](#iothubresource_update)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [List](#privateendpointconnections_list)
- [Update](#privateendpointconnections_update)

## PrivateLinkResourcesOperation

- [Get](#privatelinkresourcesoperation_get)
- [List](#privatelinkresourcesoperation_list)

## ResourceProviderCommon

- [GetSubscriptionQuota](#resourceprovidercommon_getsubscriptionquota)
### Certificates_CreateOrUpdate

```java
import com.azure.resourcemanager.iothub.models.CertificateProperties;

/** Samples for Certificates CreateOrUpdate. */
public final class CertificatesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_certificatescreateorupdate.json
     */
    /**
     * Sample code: Certificates_CreateOrUpdate.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void certificatesCreateOrUpdate(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager
            .certificates()
            .define("cert")
            .withExistingIotHub("myResourceGroup", "iothub")
            .withProperties(new CertificateProperties().withCertificate("############################################"))
            .create();
    }
}
```

### Certificates_Delete

```java
import com.azure.core.util.Context;

/** Samples for Certificates Delete. */
public final class CertificatesDeleteSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_certificatesdelete.json
     */
    /**
     * Sample code: Certificates_Delete.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void certificatesDelete(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager.certificates().deleteWithResponse("myResourceGroup", "myhub", "cert", "AAAAAAAADGk=", Context.NONE);
    }
}
```

### Certificates_GenerateVerificationCode

```java
import com.azure.core.util.Context;

/** Samples for Certificates GenerateVerificationCode. */
public final class CertificatesGenerateVerificationCodeSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_generateverificationcode.json
     */
    /**
     * Sample code: Certificates_GenerateVerificationCode.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void certificatesGenerateVerificationCode(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager
            .certificates()
            .generateVerificationCodeWithResponse("myResourceGroup", "testHub", "cert", "AAAAAAAADGk=", Context.NONE);
    }
}
```

### Certificates_Get

```java
import com.azure.core.util.Context;

/** Samples for Certificates Get. */
public final class CertificatesGetSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_getcertificate.json
     */
    /**
     * Sample code: Certificates_Get.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void certificatesGet(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager.certificates().getWithResponse("myResourceGroup", "testhub", "cert", Context.NONE);
    }
}
```

### Certificates_ListByIotHub

```java
import com.azure.core.util.Context;

/** Samples for Certificates ListByIotHub. */
public final class CertificatesListByIotHubSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_listcertificates.json
     */
    /**
     * Sample code: Certificates_ListByIotHub.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void certificatesListByIotHub(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager.certificates().listByIotHubWithResponse("myResourceGroup", "testhub", Context.NONE);
    }
}
```

### Certificates_Verify

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.iothub.models.CertificateVerificationDescription;

/** Samples for Certificates Verify. */
public final class CertificatesVerifySamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_certverify.json
     */
    /**
     * Sample code: Certificates_Verify.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void certificatesVerify(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager
            .certificates()
            .verifyWithResponse(
                "myResourceGroup",
                "myFirstProvisioningService",
                "cert",
                "AAAAAAAADGk=",
                new CertificateVerificationDescription().withCertificate("#####################################"),
                Context.NONE);
    }
}
```

### IotHub_ManualFailover

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.iothub.models.FailoverInput;

/** Samples for IotHub ManualFailover. */
public final class IotHubManualFailoverSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/IotHub_ManualFailover.json
     */
    /**
     * Sample code: IotHub_ManualFailover.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void iotHubManualFailover(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager
            .iotHubs()
            .manualFailover(
                "testHub", "myResourceGroup", new FailoverInput().withFailoverRegion("testHub"), Context.NONE);
    }
}
```

### IotHubResource_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.iothub.models.OperationInputs;

/** Samples for IotHubResource CheckNameAvailability. */
public final class IotHubResourceCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/checkNameAvailability.json
     */
    /**
     * Sample code: IotHubResource_CheckNameAvailability.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void iotHubResourceCheckNameAvailability(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager
            .iotHubResources()
            .checkNameAvailabilityWithResponse(new OperationInputs().withName("test-request"), Context.NONE);
    }
}
```

### IotHubResource_CreateEventHubConsumerGroup

```java
import com.azure.resourcemanager.iothub.models.EventHubConsumerGroupName;

/** Samples for IotHubResource CreateEventHubConsumerGroup. */
public final class IotHubResourceCreateEventHubConsumerGroupSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_createconsumergroup.json
     */
    /**
     * Sample code: IotHubResource_CreateEventHubConsumerGroup.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void iotHubResourceCreateEventHubConsumerGroup(
        com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager
            .iotHubResources()
            .defineEventHubConsumerGroup("test")
            .withExistingEventHubEndpoint("myResourceGroup", "testHub", "events")
            .withProperties(new EventHubConsumerGroupName().withName("test"))
            .create();
    }
}
```

### IotHubResource_CreateOrUpdate

```java
import com.azure.resourcemanager.iothub.models.Capabilities;
import com.azure.resourcemanager.iothub.models.CloudToDeviceProperties;
import com.azure.resourcemanager.iothub.models.DefaultAction;
import com.azure.resourcemanager.iothub.models.EventHubProperties;
import com.azure.resourcemanager.iothub.models.FallbackRouteProperties;
import com.azure.resourcemanager.iothub.models.FeedbackProperties;
import com.azure.resourcemanager.iothub.models.IotHubProperties;
import com.azure.resourcemanager.iothub.models.IotHubSku;
import com.azure.resourcemanager.iothub.models.IotHubSkuInfo;
import com.azure.resourcemanager.iothub.models.MessagingEndpointProperties;
import com.azure.resourcemanager.iothub.models.NetworkRuleIpAction;
import com.azure.resourcemanager.iothub.models.NetworkRuleSetIpRule;
import com.azure.resourcemanager.iothub.models.NetworkRuleSetProperties;
import com.azure.resourcemanager.iothub.models.RootCertificateProperties;
import com.azure.resourcemanager.iothub.models.RoutingEndpoints;
import com.azure.resourcemanager.iothub.models.RoutingProperties;
import com.azure.resourcemanager.iothub.models.RoutingSource;
import com.azure.resourcemanager.iothub.models.StorageEndpointProperties;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for IotHubResource CreateOrUpdate. */
public final class IotHubResourceCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_createOrUpdate.json
     */
    /**
     * Sample code: IotHubResource_CreateOrUpdate.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void iotHubResourceCreateOrUpdate(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager
            .iotHubResources()
            .define("testHub")
            .withRegion("centraluseuap")
            .withExistingResourceGroup("myResourceGroup")
            .withSku(new IotHubSkuInfo().withName(IotHubSku.S1).withCapacity(1L))
            .withTags(mapOf())
            .withEtag("AAAAAAFD6M4=")
            .withProperties(
                new IotHubProperties()
                    .withIpFilterRules(Arrays.asList())
                    .withNetworkRuleSets(
                        new NetworkRuleSetProperties()
                            .withDefaultAction(DefaultAction.DENY)
                            .withApplyToBuiltInEventHubEndpoint(true)
                            .withIpRules(
                                Arrays
                                    .asList(
                                        new NetworkRuleSetIpRule()
                                            .withFilterName("rule1")
                                            .withAction(NetworkRuleIpAction.ALLOW)
                                            .withIpMask("131.117.159.53"),
                                        new NetworkRuleSetIpRule()
                                            .withFilterName("rule2")
                                            .withAction(NetworkRuleIpAction.ALLOW)
                                            .withIpMask("157.55.59.128/25"))))
                    .withMinTlsVersion("1.2")
                    .withEventHubEndpoints(
                        mapOf("events", new EventHubProperties().withRetentionTimeInDays(1L).withPartitionCount(2)))
                    .withRouting(
                        new RoutingProperties()
                            .withEndpoints(
                                new RoutingEndpoints()
                                    .withServiceBusQueues(Arrays.asList())
                                    .withServiceBusTopics(Arrays.asList())
                                    .withEventHubs(Arrays.asList())
                                    .withStorageContainers(Arrays.asList()))
                            .withRoutes(Arrays.asList())
                            .withFallbackRoute(
                                new FallbackRouteProperties()
                                    .withName("$fallback")
                                    .withSource(RoutingSource.DEVICE_MESSAGES)
                                    .withCondition("true")
                                    .withEndpointNames(Arrays.asList("events"))
                                    .withIsEnabled(true)))
                    .withStorageEndpoints(
                        mapOf(
                            "$default",
                            new StorageEndpointProperties()
                                .withSasTtlAsIso8601(Duration.parse("PT1H"))
                                .withConnectionString("")
                                .withContainerName("")))
                    .withMessagingEndpoints(
                        mapOf(
                            "fileNotifications",
                            new MessagingEndpointProperties()
                                .withLockDurationAsIso8601(Duration.parse("PT1M"))
                                .withTtlAsIso8601(Duration.parse("PT1H"))
                                .withMaxDeliveryCount(10)))
                    .withEnableFileUploadNotifications(false)
                    .withCloudToDevice(
                        new CloudToDeviceProperties()
                            .withMaxDeliveryCount(10)
                            .withDefaultTtlAsIso8601(Duration.parse("PT1H"))
                            .withFeedback(
                                new FeedbackProperties()
                                    .withLockDurationAsIso8601(Duration.parse("PT1M"))
                                    .withTtlAsIso8601(Duration.parse("PT1H"))
                                    .withMaxDeliveryCount(10)))
                    .withFeatures(Capabilities.NONE)
                    .withEnableDataResidency(true)
                    .withRootCertificate(new RootCertificateProperties().withEnableRootCertificateV2(true)))
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

### IotHubResource_Delete

```java
import com.azure.core.util.Context;

/** Samples for IotHubResource Delete. */
public final class IotHubResourceDeleteSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_delete.json
     */
    /**
     * Sample code: IotHubResource_Delete.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void iotHubResourceDelete(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager.iotHubResources().delete("myResourceGroup", "testHub", Context.NONE);
    }
}
```

### IotHubResource_DeleteEventHubConsumerGroup

```java
import com.azure.core.util.Context;

/** Samples for IotHubResource DeleteEventHubConsumerGroup. */
public final class IotHubResourceDeleteEventHubConsumerGroupSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_deleteconsumergroup.json
     */
    /**
     * Sample code: IotHubResource_DeleteEventHubConsumerGroup.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void iotHubResourceDeleteEventHubConsumerGroup(
        com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager
            .iotHubResources()
            .deleteEventHubConsumerGroupWithResponse("myResourceGroup", "testHub", "events", "test", Context.NONE);
    }
}
```

### IotHubResource_ExportDevices

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.iothub.models.AuthenticationType;
import com.azure.resourcemanager.iothub.models.ExportDevicesRequest;
import com.azure.resourcemanager.iothub.models.ManagedIdentity;

/** Samples for IotHubResource ExportDevices. */
public final class IotHubResourceExportDevicesSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_exportdevices.json
     */
    /**
     * Sample code: IotHubResource_ExportDevices.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void iotHubResourceExportDevices(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager
            .iotHubResources()
            .exportDevicesWithResponse(
                "myResourceGroup",
                "testHub",
                new ExportDevicesRequest()
                    .withExportBlobContainerUri("testBlob")
                    .withExcludeKeys(true)
                    .withAuthenticationType(AuthenticationType.IDENTITY_BASED)
                    .withIdentity(
                        new ManagedIdentity()
                            .withUserAssignedIdentity(
                                "/subscriptions/91d12660-3dec-467a-be2a-213b5544ddc0/resourceGroups/myResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1")),
                Context.NONE);
    }
}
```

### IotHubResource_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for IotHubResource GetByResourceGroup. */
public final class IotHubResourceGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_get.json
     */
    /**
     * Sample code: IotHubResource_Get.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void iotHubResourceGet(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager.iotHubResources().getByResourceGroupWithResponse("myResourceGroup", "testHub", Context.NONE);
    }
}
```

### IotHubResource_GetEndpointHealth

```java
import com.azure.core.util.Context;

/** Samples for IotHubResource GetEndpointHealth. */
public final class IotHubResourceGetEndpointHealthSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_routingendpointhealth.json
     */
    /**
     * Sample code: IotHubResource_GetEndpointHealth.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void iotHubResourceGetEndpointHealth(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager.iotHubResources().getEndpointHealth("myResourceGroup", "testHub", Context.NONE);
    }
}
```

### IotHubResource_GetEventHubConsumerGroup

```java
import com.azure.core.util.Context;

/** Samples for IotHubResource GetEventHubConsumerGroup. */
public final class IotHubResourceGetEventHubConsumerGroupSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_getconsumergroup.json
     */
    /**
     * Sample code: IotHubResource_ListEventHubConsumerGroups.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void iotHubResourceListEventHubConsumerGroups(
        com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager
            .iotHubResources()
            .getEventHubConsumerGroupWithResponse("myResourceGroup", "testHub", "events", "test", Context.NONE);
    }
}
```

### IotHubResource_GetJob

```java
import com.azure.core.util.Context;

/** Samples for IotHubResource GetJob. */
public final class IotHubResourceGetJobSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_getjob.json
     */
    /**
     * Sample code: IotHubResource_GetJob.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void iotHubResourceGetJob(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager.iotHubResources().getJobWithResponse("myResourceGroup", "testHub", "test", Context.NONE);
    }
}
```

### IotHubResource_GetKeysForKeyName

```java
import com.azure.core.util.Context;

/** Samples for IotHubResource GetKeysForKeyName. */
public final class IotHubResourceGetKeysForKeyNameSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_getkey.json
     */
    /**
     * Sample code: IotHubResource_GetKeysForKeyName.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void iotHubResourceGetKeysForKeyName(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager
            .iotHubResources()
            .getKeysForKeyNameWithResponse("myResourceGroup", "testHub", "iothubowner", Context.NONE);
    }
}
```

### IotHubResource_GetQuotaMetrics

```java
import com.azure.core.util.Context;

/** Samples for IotHubResource GetQuotaMetrics. */
public final class IotHubResourceGetQuotaMetricsSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_quotametrics.json
     */
    /**
     * Sample code: IotHubResource_GetQuotaMetrics.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void iotHubResourceGetQuotaMetrics(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager.iotHubResources().getQuotaMetrics("myResourceGroup", "testHub", Context.NONE);
    }
}
```

### IotHubResource_GetStats

```java
import com.azure.core.util.Context;

/** Samples for IotHubResource GetStats. */
public final class IotHubResourceGetStatsSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_stats.json
     */
    /**
     * Sample code: IotHubResource_GetStats.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void iotHubResourceGetStats(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager.iotHubResources().getStatsWithResponse("myResourceGroup", "testHub", Context.NONE);
    }
}
```

### IotHubResource_GetValidSkus

```java
import com.azure.core.util.Context;

/** Samples for IotHubResource GetValidSkus. */
public final class IotHubResourceGetValidSkusSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_getskus.json
     */
    /**
     * Sample code: IotHubResource_GetValidSkus.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void iotHubResourceGetValidSkus(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager.iotHubResources().getValidSkus("myResourceGroup", "testHub", Context.NONE);
    }
}
```

### IotHubResource_ImportDevices

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.iothub.models.ImportDevicesRequest;

/** Samples for IotHubResource ImportDevices. */
public final class IotHubResourceImportDevicesSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_importdevices.json
     */
    /**
     * Sample code: IotHubResource_ImportDevices.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void iotHubResourceImportDevices(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager
            .iotHubResources()
            .importDevicesWithResponse(
                "myResourceGroup",
                "testHub",
                new ImportDevicesRequest().withInputBlobContainerUri("testBlob").withOutputBlobContainerUri("testBlob"),
                Context.NONE);
    }
}
```

### IotHubResource_List

```java
import com.azure.core.util.Context;

/** Samples for IotHubResource List. */
public final class IotHubResourceListSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_listbysubscription.json
     */
    /**
     * Sample code: IotHubResource_ListBySubscription.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void iotHubResourceListBySubscription(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager.iotHubResources().list(Context.NONE);
    }
}
```

### IotHubResource_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for IotHubResource ListByResourceGroup. */
public final class IotHubResourceListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_listbyrg.json
     */
    /**
     * Sample code: IotHubResource_ListByResourceGroup.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void iotHubResourceListByResourceGroup(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager.iotHubResources().listByResourceGroup("myResourceGroup", Context.NONE);
    }
}
```

### IotHubResource_ListEventHubConsumerGroups

```java
import com.azure.core.util.Context;

/** Samples for IotHubResource ListEventHubConsumerGroups. */
public final class IotHubResourceListEventHubConsumerGroupsSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_listehgroups.json
     */
    /**
     * Sample code: IotHubResource_ListEventHubConsumerGroups.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void iotHubResourceListEventHubConsumerGroups(
        com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager.iotHubResources().listEventHubConsumerGroups("myResourceGroup", "testHub", "events", Context.NONE);
    }
}
```

### IotHubResource_ListJobs

```java
import com.azure.core.util.Context;

/** Samples for IotHubResource ListJobs. */
public final class IotHubResourceListJobsSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_listjobs.json
     */
    /**
     * Sample code: IotHubResource_ListJobs.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void iotHubResourceListJobs(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager.iotHubResources().listJobs("myResourceGroup", "testHub", Context.NONE);
    }
}
```

### IotHubResource_ListKeys

```java
import com.azure.core.util.Context;

/** Samples for IotHubResource ListKeys. */
public final class IotHubResourceListKeysSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_listkeys.json
     */
    /**
     * Sample code: IotHubResource_ListKeys.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void iotHubResourceListKeys(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager.iotHubResources().listKeys("myResourceGroup", "testHub", Context.NONE);
    }
}
```

### IotHubResource_TestAllRoutes

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.iothub.models.RoutingMessage;
import com.azure.resourcemanager.iothub.models.RoutingSource;
import com.azure.resourcemanager.iothub.models.TestAllRoutesInput;
import java.util.HashMap;
import java.util.Map;

/** Samples for IotHubResource TestAllRoutes. */
public final class IotHubResourceTestAllRoutesSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_testallroutes.json
     */
    /**
     * Sample code: IotHubResource_TestAllRoutes.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void iotHubResourceTestAllRoutes(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager
            .iotHubResources()
            .testAllRoutesWithResponse(
                "testHub",
                "myResourceGroup",
                new TestAllRoutesInput()
                    .withRoutingSource(RoutingSource.DEVICE_MESSAGES)
                    .withMessage(
                        new RoutingMessage()
                            .withBody("Body of message")
                            .withAppProperties(mapOf("key1", "value1"))
                            .withSystemProperties(mapOf("key1", "value1"))),
                Context.NONE);
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

### IotHubResource_TestRoute

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.iothub.models.RouteProperties;
import com.azure.resourcemanager.iothub.models.RoutingMessage;
import com.azure.resourcemanager.iothub.models.RoutingSource;
import com.azure.resourcemanager.iothub.models.TestRouteInput;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for IotHubResource TestRoute. */
public final class IotHubResourceTestRouteSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_testnewroute.json
     */
    /**
     * Sample code: IotHubResource_TestRoute.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void iotHubResourceTestRoute(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager
            .iotHubResources()
            .testRouteWithResponse(
                "testHub",
                "myResourceGroup",
                new TestRouteInput()
                    .withMessage(
                        new RoutingMessage()
                            .withBody("Body of message")
                            .withAppProperties(mapOf("key1", "value1"))
                            .withSystemProperties(mapOf("key1", "value1")))
                    .withRoute(
                        new RouteProperties()
                            .withName("Routeid")
                            .withSource(RoutingSource.DEVICE_MESSAGES)
                            .withEndpointNames(Arrays.asList("id1"))
                            .withIsEnabled(true)),
                Context.NONE);
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

### IotHubResource_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.iothub.models.IotHubDescription;
import java.util.HashMap;
import java.util.Map;

/** Samples for IotHubResource Update. */
public final class IotHubResourceUpdateSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_patch.json
     */
    /**
     * Sample code: IotHubResource_Update.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void iotHubResourceUpdate(com.azure.resourcemanager.iothub.IotHubManager manager) {
        IotHubDescription resource =
            manager
                .iotHubResources()
                .getByResourceGroupWithResponse("myResourceGroup", "myHub", Context.NONE)
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
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_operations.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void operationsList(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### PrivateEndpointConnections_Delete

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Delete. */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_deleteprivateendpointconnection.json
     */
    /**
     * Sample code: PrivateEndpointConnection_Delete.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void privateEndpointConnectionDelete(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager
            .privateEndpointConnections()
            .delete("myResourceGroup", "testHub", "myPrivateEndpointConnection", Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_getprivateendpointconnection.json
     */
    /**
     * Sample code: PrivateEndpointConnection_Get.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void privateEndpointConnectionGet(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse("myResourceGroup", "testHub", "myPrivateEndpointConnection", Context.NONE);
    }
}
```

### PrivateEndpointConnections_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections List. */
public final class PrivateEndpointConnectionsListSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_listprivateendpointconnections.json
     */
    /**
     * Sample code: PrivateEndpointConnections_List.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void privateEndpointConnectionsList(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager.privateEndpointConnections().listWithResponse("myResourceGroup", "testHub", Context.NONE);
    }
}
```

### PrivateEndpointConnections_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.iothub.fluent.models.PrivateEndpointConnectionInner;
import com.azure.resourcemanager.iothub.models.PrivateEndpointConnectionProperties;
import com.azure.resourcemanager.iothub.models.PrivateLinkServiceConnectionState;
import com.azure.resourcemanager.iothub.models.PrivateLinkServiceConnectionStatus;

/** Samples for PrivateEndpointConnections Update. */
public final class PrivateEndpointConnectionsUpdateSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_updateprivateendpointconnection.json
     */
    /**
     * Sample code: PrivateEndpointConnection_Update.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void privateEndpointConnectionUpdate(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager
            .privateEndpointConnections()
            .update(
                "myResourceGroup",
                "testHub",
                "myPrivateEndpointConnection",
                new PrivateEndpointConnectionInner()
                    .withProperties(
                        new PrivateEndpointConnectionProperties()
                            .withPrivateLinkServiceConnectionState(
                                new PrivateLinkServiceConnectionState()
                                    .withStatus(PrivateLinkServiceConnectionStatus.APPROVED)
                                    .withDescription("Approved by johndoe@contoso.com"))),
                Context.NONE);
    }
}
```

### PrivateLinkResourcesOperation_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResourcesOperation Get. */
public final class PrivateLinkResourcesOperationGetSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_getprivatelinkresources.json
     */
    /**
     * Sample code: PrivateLinkResources_List.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void privateLinkResourcesList(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager.privateLinkResourcesOperations().getWithResponse("myResourceGroup", "testHub", "iotHub", Context.NONE);
    }
}
```

### PrivateLinkResourcesOperation_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinkResourcesOperation List. */
public final class PrivateLinkResourcesOperationListSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_listprivatelinkresources.json
     */
    /**
     * Sample code: PrivateLinkResources_List.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void privateLinkResourcesList(com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager.privateLinkResourcesOperations().listWithResponse("myResourceGroup", "testHub", Context.NONE);
    }
}
```

### ResourceProviderCommon_GetSubscriptionQuota

```java
import com.azure.core.util.Context;

/** Samples for ResourceProviderCommon GetSubscriptionQuota. */
public final class ResourceProviderCommonGetSubscriptionQuotaSamples {
    /*
     * x-ms-original-file: specification/iothub/resource-manager/Microsoft.Devices/preview/2022-04-30-preview/examples/iothub_usages.json
     */
    /**
     * Sample code: ResourceProviderCommon_GetSubscriptionQuota.
     *
     * @param manager Entry point to IotHubManager.
     */
    public static void resourceProviderCommonGetSubscriptionQuota(
        com.azure.resourcemanager.iothub.IotHubManager manager) {
        manager.resourceProviderCommons().getSubscriptionQuotaWithResponse(Context.NONE);
    }
}
```

