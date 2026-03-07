# Code snippets and samples


## AkriConnector

- [CreateOrUpdate](#akriconnector_createorupdate)
- [Delete](#akriconnector_delete)
- [Get](#akriconnector_get)
- [ListByTemplate](#akriconnector_listbytemplate)

## AkriConnectorTemplate

- [CreateOrUpdate](#akriconnectortemplate_createorupdate)
- [Delete](#akriconnectortemplate_delete)
- [Get](#akriconnectortemplate_get)
- [ListByInstanceResource](#akriconnectortemplate_listbyinstanceresource)

## Broker

- [CreateOrUpdate](#broker_createorupdate)
- [Delete](#broker_delete)
- [Get](#broker_get)
- [ListByResourceGroup](#broker_listbyresourcegroup)

## BrokerAuthentication

- [CreateOrUpdate](#brokerauthentication_createorupdate)
- [Delete](#brokerauthentication_delete)
- [Get](#brokerauthentication_get)
- [ListByResourceGroup](#brokerauthentication_listbyresourcegroup)

## BrokerAuthorization

- [CreateOrUpdate](#brokerauthorization_createorupdate)
- [Delete](#brokerauthorization_delete)
- [Get](#brokerauthorization_get)
- [ListByResourceGroup](#brokerauthorization_listbyresourcegroup)

## BrokerListener

- [CreateOrUpdate](#brokerlistener_createorupdate)
- [Delete](#brokerlistener_delete)
- [Get](#brokerlistener_get)
- [ListByResourceGroup](#brokerlistener_listbyresourcegroup)

## Dataflow

- [CreateOrUpdate](#dataflow_createorupdate)
- [Delete](#dataflow_delete)
- [Get](#dataflow_get)
- [ListByResourceGroup](#dataflow_listbyresourcegroup)

## DataflowEndpoint

- [CreateOrUpdate](#dataflowendpoint_createorupdate)
- [Delete](#dataflowendpoint_delete)
- [Get](#dataflowendpoint_get)
- [ListByResourceGroup](#dataflowendpoint_listbyresourcegroup)

## DataflowGraph

- [CreateOrUpdate](#dataflowgraph_createorupdate)
- [Delete](#dataflowgraph_delete)
- [Get](#dataflowgraph_get)
- [ListByDataflowProfile](#dataflowgraph_listbydataflowprofile)

## DataflowProfile

- [CreateOrUpdate](#dataflowprofile_createorupdate)
- [Delete](#dataflowprofile_delete)
- [Get](#dataflowprofile_get)
- [ListByResourceGroup](#dataflowprofile_listbyresourcegroup)

## Instance

- [CreateOrUpdate](#instance_createorupdate)
- [Delete](#instance_delete)
- [GetByResourceGroup](#instance_getbyresourcegroup)
- [List](#instance_list)
- [ListByResourceGroup](#instance_listbyresourcegroup)
- [Update](#instance_update)

## Operations

- [List](#operations_list)

## RegistryEndpoint

- [CreateOrUpdate](#registryendpoint_createorupdate)
- [Delete](#registryendpoint_delete)
- [Get](#registryendpoint_get)
- [ListByInstanceResource](#registryendpoint_listbyinstanceresource)
### AkriConnector_CreateOrUpdate

```java
import com.azure.resourcemanager.iotoperations.models.AkriConnectorProperties;
import com.azure.resourcemanager.iotoperations.models.ExtendedLocation;
import com.azure.resourcemanager.iotoperations.models.ExtendedLocationType;

/**
 * Samples for AkriConnector CreateOrUpdate.
 */
public final class AkriConnectorCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-01/AkriConnector_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: AkriConnector_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        akriConnectorCreateOrUpdateMaximumSet(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.akriConnectors()
            .define("resource-name123")
            .withExistingAkriConnectorTemplate("rgiotoperations", "resource-name123", "resource-name123")
            .withProperties(new AkriConnectorProperties())
            .withExtendedLocation(new ExtendedLocation().withName(
                "subscriptions/0000000-0000-0000-0000-000000000000/resourceGroups/resourceGroup123/providers/Microsoft.ExtendedLocation/customLocations/resource-name123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }
}
```

### AkriConnector_Delete

```java
/**
 * Samples for AkriConnector Delete.
 */
public final class AkriConnectorDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-01/AkriConnector_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: AkriConnector_Delete_MaximumSet.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        akriConnectorDeleteMaximumSet(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.akriConnectors()
            .delete("rgiotoperations", "resource-name123", "resource-name123", "resource-name123",
                com.azure.core.util.Context.NONE);
    }
}
```

### AkriConnector_Get

```java
/**
 * Samples for AkriConnector Get.
 */
public final class AkriConnectorGetSamples {
    /*
     * x-ms-original-file: 2025-10-01/AkriConnector_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: AkriConnector_Get_MaximumSet.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        akriConnectorGetMaximumSet(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.akriConnectors()
            .getWithResponse("rgiotoperations", "resource-name123", "resource-name123", "resource-name123",
                com.azure.core.util.Context.NONE);
    }
}
```

### AkriConnector_ListByTemplate

```java
/**
 * Samples for AkriConnector ListByTemplate.
 */
public final class AkriConnectorListByTemplateSamples {
    /*
     * x-ms-original-file: 2025-10-01/AkriConnector_ListByTemplate_MaximumSet_Gen.json
     */
    /**
     * Sample code: AkriConnector_ListByTemplate_MaximumSet.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        akriConnectorListByTemplateMaximumSet(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.akriConnectors()
            .listByTemplate("rgiotoperations", "resource-name123", "resource-name123",
                com.azure.core.util.Context.NONE);
    }
}
```

### AkriConnectorTemplate_CreateOrUpdate

```java
import com.azure.resourcemanager.iotoperations.models.AkriConnectorTemplateAioMetadata;
import com.azure.resourcemanager.iotoperations.models.AkriConnectorTemplateDeviceInboundEndpointType;
import com.azure.resourcemanager.iotoperations.models.AkriConnectorTemplateDiagnostics;
import com.azure.resourcemanager.iotoperations.models.AkriConnectorTemplateManagedConfiguration;
import com.azure.resourcemanager.iotoperations.models.AkriConnectorTemplateProperties;
import com.azure.resourcemanager.iotoperations.models.AkriConnectorTemplateRuntimeImageConfiguration;
import com.azure.resourcemanager.iotoperations.models.AkriConnectorTemplateRuntimeImageConfigurationSettings;
import com.azure.resourcemanager.iotoperations.models.AkriConnectorsContainerRegistry;
import com.azure.resourcemanager.iotoperations.models.AkriConnectorsContainerRegistrySettings;
import com.azure.resourcemanager.iotoperations.models.AkriConnectorsDiagnosticsLogs;
import com.azure.resourcemanager.iotoperations.models.AkriConnectorsMqttConnectionConfiguration;
import com.azure.resourcemanager.iotoperations.models.AkriConnectorsMqttProtocolType;
import com.azure.resourcemanager.iotoperations.models.AkriConnectorsServiceAccountAuthentication;
import com.azure.resourcemanager.iotoperations.models.AkriConnectorsServiceAccountTokenSettings;
import com.azure.resourcemanager.iotoperations.models.AkriConnectorsTag;
import com.azure.resourcemanager.iotoperations.models.ExtendedLocation;
import com.azure.resourcemanager.iotoperations.models.ExtendedLocationType;
import com.azure.resourcemanager.iotoperations.models.OperationalMode;
import com.azure.resourcemanager.iotoperations.models.TlsProperties;
import java.util.Arrays;

/**
 * Samples for AkriConnectorTemplate CreateOrUpdate.
 */
public final class AkriConnectorTemplateCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-01/AkriConnectorTemplate_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: AkriConnectorTemplate_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void akriConnectorTemplateCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.akriConnectorTemplates()
            .define("resource-name123")
            .withExistingInstance("rgiotoperations", "resource-name123")
            .withProperties(new AkriConnectorTemplateProperties()
                .withAioMetadata(
                    new AkriConnectorTemplateAioMetadata().withAioMinVersion("1.2.0").withAioMaxVersion("1.4.0"))
                .withRuntimeConfiguration(new AkriConnectorTemplateManagedConfiguration()
                    .withManagedConfigurationSettings(new AkriConnectorTemplateRuntimeImageConfiguration()
                        .withImageConfigurationSettings(new AkriConnectorTemplateRuntimeImageConfigurationSettings()
                            .withImageName("akri-connectors/rest")
                            .withRegistrySettings(new AkriConnectorsContainerRegistry().withContainerRegistrySettings(
                                new AkriConnectorsContainerRegistrySettings().withRegistry("akribuilds.azurecr.io")))
                            .withTagDigestSettings(new AkriConnectorsTag().withTag("0.5.0-20250825.4")))))
                .withDiagnostics(new AkriConnectorTemplateDiagnostics()
                    .withLogs(new AkriConnectorsDiagnosticsLogs().withLevel("info")))
                .withDeviceInboundEndpointTypes(Arrays
                    .asList(new AkriConnectorTemplateDeviceInboundEndpointType().withEndpointType("Microsoft.Rest")
                        .withVersion("0.0.1")))
                .withMqttConnectionConfiguration(new AkriConnectorsMqttConnectionConfiguration()
                    .withAuthentication(
                        new AkriConnectorsServiceAccountAuthentication().withServiceAccountTokenSettings(
                            new AkriConnectorsServiceAccountTokenSettings().withAudience("MQ-SAT")))
                    .withHost("aio-broker:18883")
                    .withProtocol(AkriConnectorsMqttProtocolType.MQTT)
                    .withKeepAliveSeconds(10)
                    .withMaxInflightMessages(10)
                    .withSessionExpirySeconds(60)
                    .withTls(new TlsProperties().withMode(OperationalMode.ENABLED)
                        .withTrustedCaCertificateConfigMapRef("azure-iot-operations-aio-ca-trust-bundle"))))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }
}
```

### AkriConnectorTemplate_Delete

```java
/**
 * Samples for AkriConnectorTemplate Delete.
 */
public final class AkriConnectorTemplateDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-01/AkriConnectorTemplate_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: AkriConnectorTemplate_Delete_MaximumSet.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        akriConnectorTemplateDeleteMaximumSet(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.akriConnectorTemplates()
            .delete("rgiotoperations", "resource-name123", "resource-name123", com.azure.core.util.Context.NONE);
    }
}
```

### AkriConnectorTemplate_Get

```java
/**
 * Samples for AkriConnectorTemplate Get.
 */
public final class AkriConnectorTemplateGetSamples {
    /*
     * x-ms-original-file: 2025-10-01/AkriConnectorTemplate_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: AkriConnectorTemplate_Get_MaximumSet.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        akriConnectorTemplateGetMaximumSet(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.akriConnectorTemplates()
            .getWithResponse("rgiotoperations", "resource-name123", "resource-name123",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-10-01/AkriConnectorTemplate_Get_Managed_Rest.json
     */
    /**
     * Sample code: AkriConnectorTemplate_Get_Managed_Rest.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        akriConnectorTemplateGetManagedRest(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.akriConnectorTemplates()
            .getWithResponse("rgiotoperations", "resource-name123", "resource-name123",
                com.azure.core.util.Context.NONE);
    }
}
```

### AkriConnectorTemplate_ListByInstanceResource

```java
/**
 * Samples for AkriConnectorTemplate ListByInstanceResource.
 */
public final class AkriConnectorTemplateListByInstanceResourceSamples {
    /*
     * x-ms-original-file: 2025-10-01/AkriConnectorTemplate_ListByInstanceResource_MaximumSet_Gen.json
     */
    /**
     * Sample code: AkriConnectorTemplate_ListByInstanceResource_MaximumSet.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void akriConnectorTemplateListByInstanceResourceMaximumSet(
        com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.akriConnectorTemplates()
            .listByInstanceResource("rgiotoperations", "resource-name123", com.azure.core.util.Context.NONE);
    }
}
```

### Broker_CreateOrUpdate

```java
import com.azure.resourcemanager.iotoperations.models.AdvancedSettings;
import com.azure.resourcemanager.iotoperations.models.BackendChain;
import com.azure.resourcemanager.iotoperations.models.BrokerDiagnostics;
import com.azure.resourcemanager.iotoperations.models.BrokerMemoryProfile;
import com.azure.resourcemanager.iotoperations.models.BrokerProperties;
import com.azure.resourcemanager.iotoperations.models.Cardinality;
import com.azure.resourcemanager.iotoperations.models.CertManagerCertOptions;
import com.azure.resourcemanager.iotoperations.models.CertManagerPrivateKey;
import com.azure.resourcemanager.iotoperations.models.ClientConfig;
import com.azure.resourcemanager.iotoperations.models.DiagnosticsLogs;
import com.azure.resourcemanager.iotoperations.models.DiskBackedMessageBuffer;
import com.azure.resourcemanager.iotoperations.models.ExtendedLocation;
import com.azure.resourcemanager.iotoperations.models.ExtendedLocationType;
import com.azure.resourcemanager.iotoperations.models.Frontend;
import com.azure.resourcemanager.iotoperations.models.GenerateResourceLimits;
import com.azure.resourcemanager.iotoperations.models.KubernetesReference;
import com.azure.resourcemanager.iotoperations.models.LocalKubernetesReference;
import com.azure.resourcemanager.iotoperations.models.Metrics;
import com.azure.resourcemanager.iotoperations.models.OperationalMode;
import com.azure.resourcemanager.iotoperations.models.OperatorValues;
import com.azure.resourcemanager.iotoperations.models.PrivateKeyAlgorithm;
import com.azure.resourcemanager.iotoperations.models.PrivateKeyRotationPolicy;
import com.azure.resourcemanager.iotoperations.models.SelfCheck;
import com.azure.resourcemanager.iotoperations.models.SelfTracing;
import com.azure.resourcemanager.iotoperations.models.SubscriberMessageDropStrategy;
import com.azure.resourcemanager.iotoperations.models.SubscriberQueueLimit;
import com.azure.resourcemanager.iotoperations.models.Traces;
import com.azure.resourcemanager.iotoperations.models.VolumeClaimResourceRequirements;
import com.azure.resourcemanager.iotoperations.models.VolumeClaimSpec;
import com.azure.resourcemanager.iotoperations.models.VolumeClaimSpecSelector;
import com.azure.resourcemanager.iotoperations.models.VolumeClaimSpecSelectorMatchExpressions;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Broker CreateOrUpdate.
 */
public final class BrokerCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-01/Broker_CreateOrUpdate_Minimal.json
     */
    /**
     * Sample code: Broker_CreateOrUpdate_Minimal.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        brokerCreateOrUpdateMinimal(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.brokers()
            .define("resource-name123")
            .withExistingInstance("rgiotoperations", "resource-name123")
            .withProperties(new BrokerProperties().withMemoryProfile(BrokerMemoryProfile.TINY))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-01/Broker_CreateOrUpdate_Complex.json
     */
    /**
     * Sample code: Broker_CreateOrUpdate_Complex.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        brokerCreateOrUpdateComplex(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.brokers()
            .define("resource-name123")
            .withExistingInstance("rgiotoperations", "resource-name123")
            .withProperties(new BrokerProperties()
                .withCardinality(new Cardinality()
                    .withBackendChain(new BackendChain().withPartitions(2).withRedundancyFactor(2).withWorkers(2))
                    .withFrontend(new Frontend().withReplicas(2).withWorkers(2)))
                .withDiskBackedMessageBuffer(new DiskBackedMessageBuffer().withMaxSize("50M"))
                .withGenerateResourceLimits(new GenerateResourceLimits().withCpu(OperationalMode.ENABLED))
                .withMemoryProfile(BrokerMemoryProfile.MEDIUM))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-01/Broker_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Broker_CreateOrUpdate.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void brokerCreateOrUpdate(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.brokers()
            .define("resource-name123")
            .withExistingInstance("rgiotoperations", "resource-name123")
            .withProperties(
                new BrokerProperties()
                    .withAdvanced(new AdvancedSettings()
                        .withClients(new ClientConfig().withMaxSessionExpirySeconds(3859)
                            .withMaxMessageExpirySeconds(3263)
                            .withMaxPacketSizeBytes(3029)
                            .withSubscriberQueueLimit(new SubscriberQueueLimit().withLength(6L)
                                .withStrategy(SubscriberMessageDropStrategy.NONE))
                            .withMaxReceiveMaximum(2365)
                            .withMaxKeepAliveSeconds(3744))
                        .withEncryptInternalTraffic(OperationalMode.ENABLED)
                        .withInternalCerts(new CertManagerCertOptions().withDuration("bchrc")
                            .withRenewBefore("xkafmpgjfifkwwrhkswtopdnne")
                            .withPrivateKey(new CertManagerPrivateKey().withAlgorithm(PrivateKeyAlgorithm.EC256)
                                .withRotationPolicy(PrivateKeyRotationPolicy.ALWAYS))))
                    .withCardinality(new Cardinality()
                        .withBackendChain(new BackendChain().withPartitions(11).withRedundancyFactor(5).withWorkers(15))
                        .withFrontend(new Frontend().withReplicas(2).withWorkers(6)))
                    .withDiagnostics(new BrokerDiagnostics()
                        .withLogs(new DiagnosticsLogs().withLevel("rnmwokumdmebpmfxxxzvvjfdywotav"))
                        .withMetrics(new Metrics().withPrometheusPort(7581))
                        .withSelfCheck(new SelfCheck().withMode(OperationalMode.ENABLED)
                            .withIntervalSeconds(158)
                            .withTimeoutSeconds(14))
                        .withTraces(new Traces().withMode(OperationalMode.ENABLED)
                            .withCacheSizeMegabytes(28)
                            .withSelfTracing(
                                new SelfTracing().withMode(OperationalMode.ENABLED).withIntervalSeconds(22))
                            .withSpanChannelCapacity(1000)))
                    .withDiskBackedMessageBuffer(new DiskBackedMessageBuffer().withMaxSize("500M")
                        .withEphemeralVolumeClaimSpec(new VolumeClaimSpec().withVolumeName("c")
                            .withVolumeMode("rxvpksjuuugqnqzeiprocknbn")
                            .withStorageClassName("sseyhrjptkhrqvpdpjmornkqvon")
                            .withAccessModes(Arrays.asList("nuluhigrbb"))
                            .withDataSource(new LocalKubernetesReference().withApiGroup("npqapyksvvpkohujx")
                                .withKind("wazgyb")
                                .withName("cwhsgxxcxsyppoefm"))
                            .withDataSourceRef(new KubernetesReference().withApiGroup("mnfnykznjjsoqpfsgdqioupt")
                                .withKind("odynqzekfzsnawrctaxg")
                                .withName("envszivbbmixbyddzg")
                                .withNamespace("etcfzvxqd"))
                            .withResources(new VolumeClaimResourceRequirements()
                                .withLimits(mapOf("key2719", "fakeTokenPlaceholder"))
                                .withRequests(mapOf("key2909", "fakeTokenPlaceholder")))
                            .withSelector(new VolumeClaimSpecSelector()
                                .withMatchExpressions(Arrays.asList(
                                    new VolumeClaimSpecSelectorMatchExpressions().withKey("fakeTokenPlaceholder")
                                        .withOperator(OperatorValues.IN)
                                        .withValues(Arrays.asList("slmpajlywqvuyknipgztsonqyybt"))))
                                .withMatchLabels(mapOf("key6673", "fakeTokenPlaceholder"))))
                        .withPersistentVolumeClaimSpec(new VolumeClaimSpec().withVolumeName("c")
                            .withVolumeMode("rxvpksjuuugqnqzeiprocknbn")
                            .withStorageClassName("sseyhrjptkhrqvpdpjmornkqvon")
                            .withAccessModes(Arrays.asList("nuluhigrbb"))
                            .withDataSource(new LocalKubernetesReference().withApiGroup("npqapyksvvpkohujx")
                                .withKind("wazgyb")
                                .withName("cwhsgxxcxsyppoefm"))
                            .withDataSourceRef(new KubernetesReference().withApiGroup("mnfnykznjjsoqpfsgdqioupt")
                                .withKind("odynqzekfzsnawrctaxg")
                                .withName("envszivbbmixbyddzg")
                                .withNamespace("etcfzvxqd"))
                            .withResources(new VolumeClaimResourceRequirements()
                                .withLimits(mapOf("key2719", "fakeTokenPlaceholder"))
                                .withRequests(mapOf("key2909", "fakeTokenPlaceholder")))
                            .withSelector(new VolumeClaimSpecSelector()
                                .withMatchExpressions(Arrays.asList(
                                    new VolumeClaimSpecSelectorMatchExpressions().withKey("fakeTokenPlaceholder")
                                        .withOperator(OperatorValues.IN)
                                        .withValues(Arrays.asList("slmpajlywqvuyknipgztsonqyybt"))))
                                .withMatchLabels(mapOf("key6673", "fakeTokenPlaceholder")))))
                    .withGenerateResourceLimits(new GenerateResourceLimits().withCpu(OperationalMode.ENABLED))
                    .withMemoryProfile(BrokerMemoryProfile.TINY))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-01/Broker_CreateOrUpdate_Simple.json
     */
    /**
     * Sample code: Broker_CreateOrUpdate_Simple.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        brokerCreateOrUpdateSimple(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.brokers()
            .define("resource-name123")
            .withExistingInstance("rgiotoperations", "resource-name123")
            .withProperties(new BrokerProperties()
                .withCardinality(new Cardinality()
                    .withBackendChain(new BackendChain().withPartitions(2).withRedundancyFactor(2).withWorkers(2))
                    .withFrontend(new Frontend().withReplicas(2).withWorkers(2)))
                .withGenerateResourceLimits(new GenerateResourceLimits().withCpu(OperationalMode.ENABLED))
                .withMemoryProfile(BrokerMemoryProfile.LOW))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
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

### Broker_Delete

```java
/**
 * Samples for Broker Delete.
 */
public final class BrokerDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-01/Broker_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Broker_Delete.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void brokerDelete(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.brokers()
            .delete("rgiotoperations", "resource-name123", "resource-name123", com.azure.core.util.Context.NONE);
    }
}
```

### Broker_Get

```java
/**
 * Samples for Broker Get.
 */
public final class BrokerGetSamples {
    /*
     * x-ms-original-file: 2025-10-01/Broker_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Broker_Get.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void brokerGet(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.brokers()
            .getWithResponse("rgiotoperations", "resource-name123", "resource-name123",
                com.azure.core.util.Context.NONE);
    }
}
```

### Broker_ListByResourceGroup

```java
/**
 * Samples for Broker ListByResourceGroup.
 */
public final class BrokerListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-10-01/Broker_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: Broker_ListByResourceGroup.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void brokerListByResourceGroup(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.brokers().listByResourceGroup("rgiotoperations", "resource-name123", com.azure.core.util.Context.NONE);
    }
}
```

### BrokerAuthentication_CreateOrUpdate

```java
import com.azure.resourcemanager.iotoperations.models.BrokerAuthenticationMethod;
import com.azure.resourcemanager.iotoperations.models.BrokerAuthenticationProperties;
import com.azure.resourcemanager.iotoperations.models.BrokerAuthenticatorCustomAuth;
import com.azure.resourcemanager.iotoperations.models.BrokerAuthenticatorMethodCustom;
import com.azure.resourcemanager.iotoperations.models.BrokerAuthenticatorMethodSat;
import com.azure.resourcemanager.iotoperations.models.BrokerAuthenticatorMethodX509;
import com.azure.resourcemanager.iotoperations.models.BrokerAuthenticatorMethodX509Attributes;
import com.azure.resourcemanager.iotoperations.models.BrokerAuthenticatorMethods;
import com.azure.resourcemanager.iotoperations.models.ExtendedLocation;
import com.azure.resourcemanager.iotoperations.models.ExtendedLocationType;
import com.azure.resourcemanager.iotoperations.models.X509ManualCertificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for BrokerAuthentication CreateOrUpdate.
 */
public final class BrokerAuthenticationCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-01/BrokerAuthentication_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: BrokerAuthentication_CreateOrUpdate.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        brokerAuthenticationCreateOrUpdate(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.brokerAuthentications()
            .define("resource-name123")
            .withExistingBroker("rgiotoperations", "resource-name123", "resource-name123")
            .withProperties(
                new BrokerAuthenticationProperties()
                    .withAuthenticationMethods(
                        Arrays
                            .asList(new BrokerAuthenticatorMethods().withMethod(BrokerAuthenticationMethod.CUSTOM)
                                .withCustomSettings(new BrokerAuthenticatorMethodCustom()
                                    .withAuth(new BrokerAuthenticatorCustomAuth()
                                        .withX509(new X509ManualCertificate().withSecretRef("fakeTokenPlaceholder")))
                                    .withCaCertConfigMap("pdecudefqyolvncbus")
                                    .withEndpoint("https://www.example.com")
                                    .withHeaders(mapOf("key8518", "fakeTokenPlaceholder")))
                                .withServiceAccountTokenSettings(
                                    new BrokerAuthenticatorMethodSat().withAudiences(Arrays.asList("jqyhyqatuydg")))
                                .withX509Settings(new BrokerAuthenticatorMethodX509()
                                    .withAuthorizationAttributes(mapOf("key3384",
                                        new BrokerAuthenticatorMethodX509Attributes()
                                            .withAttributes(mapOf("key186", "fakeTokenPlaceholder"))
                                            .withSubject("jpgwctfeixitptfgfnqhua")))
                                    .withTrustedClientCaCert("vlctsqddl")))))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-01/BrokerAuthentication_CreateOrUpdate_Complex.json
     */
    /**
     * Sample code: BrokerAuthentication_CreateOrUpdate_Complex.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void brokerAuthenticationCreateOrUpdateComplex(
        com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.brokerAuthentications()
            .define("resource-name123")
            .withExistingBroker("rgiotoperations", "resource-name123", "resource-name123")
            .withProperties(new BrokerAuthenticationProperties().withAuthenticationMethods(Arrays.asList(
                new BrokerAuthenticatorMethods().withMethod(BrokerAuthenticationMethod.SERVICE_ACCOUNT_TOKEN)
                    .withServiceAccountTokenSettings(
                        new BrokerAuthenticatorMethodSat().withAudiences(Arrays.asList("aio-internal"))),
                new BrokerAuthenticatorMethods().withMethod(BrokerAuthenticationMethod.X509)
                    .withX509Settings(new BrokerAuthenticatorMethodX509().withAuthorizationAttributes(mapOf("root",
                        new BrokerAuthenticatorMethodX509Attributes().withAttributes(mapOf("organization", "contoso"))
                            .withSubject("CN = Contoso Root CA Cert, OU = Engineering, C = US"),
                        "intermediate",
                        new BrokerAuthenticatorMethodX509Attributes()
                            .withAttributes(mapOf("city", "seattle", "foo", "bar"))
                            .withSubject("CN = Contoso Intermediate CA"),
                        "smart-fan",
                        new BrokerAuthenticatorMethodX509Attributes().withAttributes(mapOf("building", "17"))
                            .withSubject("CN = smart-fan")))
                        .withTrustedClientCaCert("my-ca")))))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
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

### BrokerAuthentication_Delete

```java
/**
 * Samples for BrokerAuthentication Delete.
 */
public final class BrokerAuthenticationDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-01/BrokerAuthentication_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: BrokerAuthentication_Delete.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        brokerAuthenticationDelete(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.brokerAuthentications()
            .delete("rgiotoperations", "resource-name123", "resource-name123", "resource-name123",
                com.azure.core.util.Context.NONE);
    }
}
```

### BrokerAuthentication_Get

```java
/**
 * Samples for BrokerAuthentication Get.
 */
public final class BrokerAuthenticationGetSamples {
    /*
     * x-ms-original-file: 2025-10-01/BrokerAuthentication_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: BrokerAuthentication_Get.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void brokerAuthenticationGet(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.brokerAuthentications()
            .getWithResponse("rgiotoperations", "resource-name123", "resource-name123", "resource-name123",
                com.azure.core.util.Context.NONE);
    }
}
```

### BrokerAuthentication_ListByResourceGroup

```java
/**
 * Samples for BrokerAuthentication ListByResourceGroup.
 */
public final class BrokerAuthenticationListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-10-01/BrokerAuthentication_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: BrokerAuthentication_ListByResourceGroup.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        brokerAuthenticationListByResourceGroup(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.brokerAuthentications()
            .listByResourceGroup("rgiotoperations", "resource-name123", "resource-name123",
                com.azure.core.util.Context.NONE);
    }
}
```

### BrokerAuthorization_CreateOrUpdate

```java
import com.azure.resourcemanager.iotoperations.models.AuthorizationConfig;
import com.azure.resourcemanager.iotoperations.models.AuthorizationRule;
import com.azure.resourcemanager.iotoperations.models.BrokerAuthorizationProperties;
import com.azure.resourcemanager.iotoperations.models.BrokerResourceDefinitionMethods;
import com.azure.resourcemanager.iotoperations.models.BrokerResourceRule;
import com.azure.resourcemanager.iotoperations.models.ExtendedLocation;
import com.azure.resourcemanager.iotoperations.models.ExtendedLocationType;
import com.azure.resourcemanager.iotoperations.models.OperationalMode;
import com.azure.resourcemanager.iotoperations.models.PrincipalDefinition;
import com.azure.resourcemanager.iotoperations.models.StateStoreResourceDefinitionMethods;
import com.azure.resourcemanager.iotoperations.models.StateStoreResourceKeyTypes;
import com.azure.resourcemanager.iotoperations.models.StateStoreResourceRule;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for BrokerAuthorization CreateOrUpdate.
 */
public final class BrokerAuthorizationCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-01/BrokerAuthorization_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: BrokerAuthorization_CreateOrUpdate.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        brokerAuthorizationCreateOrUpdate(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.brokerAuthorizations()
            .define("resource-name123")
            .withExistingBroker("rgiotoperations", "resource-name123", "resource-name123")
            .withProperties(new BrokerAuthorizationProperties()
                .withAuthorizationPolicies(new AuthorizationConfig().withCache(OperationalMode.ENABLED)
                    .withRules(Arrays.asList(new AuthorizationRule()
                        .withBrokerResources(
                            Arrays.asList(new BrokerResourceRule().withMethod(BrokerResourceDefinitionMethods.CONNECT)
                                .withClientIds(Arrays.asList("nlc"))
                                .withTopics(Arrays.asList("wvuca"))))
                        .withPrincipals(new PrincipalDefinition()
                            .withAttributes(Arrays.asList(mapOf("key5526", "fakeTokenPlaceholder")))
                            .withClientIds(Arrays.asList("smopeaeddsygz"))
                            .withUsernames(Arrays.asList("iozngyqndrteikszkbasinzdjtm")))
                        .withStateStoreResources(
                            Arrays.asList(new StateStoreResourceRule().withKeyType(StateStoreResourceKeyTypes.PATTERN)
                                .withKeys(Arrays.asList("tkounsqtwvzyaklxjqoerpu"))
                                .withMethod(StateStoreResourceDefinitionMethods.READ)))))))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-01/BrokerAuthorization_CreateOrUpdate_Simple.json
     */
    /**
     * Sample code: BrokerAuthorization_CreateOrUpdate_Simple.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        brokerAuthorizationCreateOrUpdateSimple(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.brokerAuthorizations()
            .define("resource-name123")
            .withExistingBroker("rgiotoperations", "resource-name123", "resource-name123")
            .withProperties(new BrokerAuthorizationProperties()
                .withAuthorizationPolicies(new AuthorizationConfig().withCache(OperationalMode.ENABLED)
                    .withRules(Arrays.asList(new AuthorizationRule()
                        .withBrokerResources(
                            Arrays.asList(new BrokerResourceRule().withMethod(BrokerResourceDefinitionMethods.CONNECT),
                                new BrokerResourceRule().withMethod(BrokerResourceDefinitionMethods.SUBSCRIBE)
                                    .withTopics(Arrays.asList("topic", "topic/with/wildcard/#"))))
                        .withPrincipals(new PrincipalDefinition()
                            .withAttributes(Arrays.asList(mapOf("floor", "floor1", "site", "site1")))
                            .withClientIds(Arrays.asList("my-client-id")))
                        .withStateStoreResources(
                            Arrays.asList(new StateStoreResourceRule().withKeyType(StateStoreResourceKeyTypes.PATTERN)
                                .withKeys(Arrays.asList("*"))
                                .withMethod(StateStoreResourceDefinitionMethods.READ_WRITE)))))))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-01/BrokerAuthorization_CreateOrUpdate_Complex.json
     */
    /**
     * Sample code: BrokerAuthorization_CreateOrUpdate_Complex.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        brokerAuthorizationCreateOrUpdateComplex(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.brokerAuthorizations()
            .define("resource-name123")
            .withExistingBroker("rgiotoperations", "resource-name123", "resource-name123")
            .withProperties(new BrokerAuthorizationProperties()
                .withAuthorizationPolicies(new AuthorizationConfig().withCache(OperationalMode.ENABLED)
                    .withRules(Arrays.asList(new AuthorizationRule()
                        .withBrokerResources(Arrays.asList(
                            new BrokerResourceRule().withMethod(BrokerResourceDefinitionMethods.CONNECT)
                                .withClientIds(Arrays.asList("{principal.attributes.building}*")),
                            new BrokerResourceRule().withMethod(BrokerResourceDefinitionMethods.PUBLISH)
                                .withTopics(Arrays.asList(
                                    "sensors/{principal.attributes.building}/{principal.clientId}/telemetry/*")),
                            new BrokerResourceRule().withMethod(BrokerResourceDefinitionMethods.SUBSCRIBE)
                                .withTopics(Arrays.asList("commands/{principal.attributes.organization}"))))
                        .withPrincipals(new PrincipalDefinition()
                            .withAttributes(Arrays.asList(mapOf("building", "17", "organization", "contoso")))
                            .withUsernames(Arrays.asList("temperature-sensor", "humidity-sensor")))
                        .withStateStoreResources(Arrays.asList(
                            new StateStoreResourceRule().withKeyType(StateStoreResourceKeyTypes.PATTERN)
                                .withKeys(Arrays.asList("myreadkey", "myotherkey?", "mynumerickeysuffix[0-9]",
                                    "clients:{principal.clientId}:*"))
                                .withMethod(StateStoreResourceDefinitionMethods.READ),
                            new StateStoreResourceRule().withKeyType(StateStoreResourceKeyTypes.BINARY)
                                .withKeys(Arrays.asList("MTE2IDEwMSAxMTUgMTE2"))
                                .withMethod(StateStoreResourceDefinitionMethods.READ_WRITE)))))))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
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

### BrokerAuthorization_Delete

```java
/**
 * Samples for BrokerAuthorization Delete.
 */
public final class BrokerAuthorizationDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-01/BrokerAuthorization_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: BrokerAuthorization_Delete.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void brokerAuthorizationDelete(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.brokerAuthorizations()
            .delete("rgiotoperations", "resource-name123", "resource-name123", "resource-name123",
                com.azure.core.util.Context.NONE);
    }
}
```

### BrokerAuthorization_Get

```java
/**
 * Samples for BrokerAuthorization Get.
 */
public final class BrokerAuthorizationGetSamples {
    /*
     * x-ms-original-file: 2025-10-01/BrokerAuthorization_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: BrokerAuthorization_Get.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void brokerAuthorizationGet(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.brokerAuthorizations()
            .getWithResponse("rgiotoperations", "resource-name123", "resource-name123", "resource-name123",
                com.azure.core.util.Context.NONE);
    }
}
```

### BrokerAuthorization_ListByResourceGroup

```java
/**
 * Samples for BrokerAuthorization ListByResourceGroup.
 */
public final class BrokerAuthorizationListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-10-01/BrokerAuthorization_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: BrokerAuthorization_ListByResourceGroup.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        brokerAuthorizationListByResourceGroup(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.brokerAuthorizations()
            .listByResourceGroup("rgiotoperations", "resource-name123", "resource-name123",
                com.azure.core.util.Context.NONE);
    }
}
```

### BrokerListener_CreateOrUpdate

```java
import com.azure.resourcemanager.iotoperations.models.BrokerListenerProperties;
import com.azure.resourcemanager.iotoperations.models.BrokerProtocolType;
import com.azure.resourcemanager.iotoperations.models.CertManagerCertificateSpec;
import com.azure.resourcemanager.iotoperations.models.CertManagerIssuerKind;
import com.azure.resourcemanager.iotoperations.models.CertManagerIssuerRef;
import com.azure.resourcemanager.iotoperations.models.CertManagerPrivateKey;
import com.azure.resourcemanager.iotoperations.models.ExtendedLocation;
import com.azure.resourcemanager.iotoperations.models.ExtendedLocationType;
import com.azure.resourcemanager.iotoperations.models.ListenerPort;
import com.azure.resourcemanager.iotoperations.models.PrivateKeyAlgorithm;
import com.azure.resourcemanager.iotoperations.models.PrivateKeyRotationPolicy;
import com.azure.resourcemanager.iotoperations.models.SanForCert;
import com.azure.resourcemanager.iotoperations.models.ServiceType;
import com.azure.resourcemanager.iotoperations.models.TlsCertMethod;
import com.azure.resourcemanager.iotoperations.models.TlsCertMethodMode;
import com.azure.resourcemanager.iotoperations.models.X509ManualCertificate;
import java.util.Arrays;

/**
 * Samples for BrokerListener CreateOrUpdate.
 */
public final class BrokerListenerCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-01/BrokerListener_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: BrokerListener_CreateOrUpdate.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        brokerListenerCreateOrUpdate(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.brokerListeners()
            .define("resource-name123")
            .withExistingBroker("rgiotoperations", "resource-name123", "resource-name123")
            .withProperties(new BrokerListenerProperties().withServiceName("tpfiszlapdpxktx")
                .withPorts(Arrays.asList(new ListenerPort().withAuthenticationRef("tjvdroaqqy")
                    .withAuthorizationRef("fakeTokenPlaceholder")
                    .withNodePort(7281)
                    .withPort(1268)
                    .withProtocol(BrokerProtocolType.MQTT)
                    .withTls(new TlsCertMethod().withMode(TlsCertMethodMode.AUTOMATIC)
                        .withCertManagerCertificateSpec(new CertManagerCertificateSpec().withDuration("qmpeffoksron")
                            .withSecretName("fakeTokenPlaceholder")
                            .withRenewBefore("hutno")
                            .withIssuerRef(new CertManagerIssuerRef().withGroup("jtmuladdkpasfpoyvewekmiy")
                                .withKind(CertManagerIssuerKind.ISSUER)
                                .withName("ocwoqpgucvjrsuudtjhb"))
                            .withPrivateKey(new CertManagerPrivateKey().withAlgorithm(PrivateKeyAlgorithm.EC256)
                                .withRotationPolicy(PrivateKeyRotationPolicy.ALWAYS))
                            .withSan(new SanForCert().withDns(Arrays.asList("xhvmhrrhgfsapocjeebqtnzarlj"))
                                .withIp(Arrays.asList("zbgugfzcgsmegevzktsnibyuyp"))))
                        .withManual(new X509ManualCertificate().withSecretRef("fakeTokenPlaceholder")))))
                .withServiceType(ServiceType.CLUSTER_IP))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-01/BrokerListener_CreateOrUpdate_Simple.json
     */
    /**
     * Sample code: BrokerListener_CreateOrUpdate_Simple.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        brokerListenerCreateOrUpdateSimple(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.brokerListeners()
            .define("resource-name123")
            .withExistingBroker("rgiotoperations", "resource-name123", "resource-name123")
            .withProperties(new BrokerListenerProperties().withPorts(Arrays.asList(new ListenerPort().withPort(1883))))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-01/BrokerListener_CreateOrUpdate_Complex.json
     */
    /**
     * Sample code: BrokerListener_CreateOrUpdate_Complex.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        brokerListenerCreateOrUpdateComplex(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.brokerListeners()
            .define("resource-name123")
            .withExistingBroker("rgiotoperations", "resource-name123", "resource-name123")
            .withProperties(new BrokerListenerProperties()
                .withPorts(Arrays.asList(
                    new ListenerPort().withAuthenticationRef("example-authentication")
                        .withPort(8080)
                        .withProtocol(BrokerProtocolType.WEB_SOCKETS),
                    new ListenerPort().withAuthenticationRef("example-authentication")
                        .withPort(8443)
                        .withProtocol(BrokerProtocolType.WEB_SOCKETS)
                        .withTls(new TlsCertMethod().withMode(TlsCertMethodMode.AUTOMATIC)
                            .withCertManagerCertificateSpec(new CertManagerCertificateSpec()
                                .withIssuerRef(new CertManagerIssuerRef().withGroup("jtmuladdkpasfpoyvewekmiy")
                                    .withKind(CertManagerIssuerKind.ISSUER)
                                    .withName("example-issuer")))),
                    new ListenerPort().withAuthenticationRef("example-authentication").withPort(1883),
                    new ListenerPort().withAuthenticationRef("example-authentication")
                        .withPort(8883)
                        .withTls(new TlsCertMethod().withMode(TlsCertMethodMode.MANUAL)
                            .withManual(new X509ManualCertificate().withSecretRef("fakeTokenPlaceholder")))))
                .withServiceType(ServiceType.LOAD_BALANCER))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }
}
```

### BrokerListener_Delete

```java
/**
 * Samples for BrokerListener Delete.
 */
public final class BrokerListenerDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-01/BrokerListener_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: BrokerListener_Delete.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void brokerListenerDelete(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.brokerListeners()
            .delete("rgiotoperations", "resource-name123", "resource-name123", "resource-name123",
                com.azure.core.util.Context.NONE);
    }
}
```

### BrokerListener_Get

```java
/**
 * Samples for BrokerListener Get.
 */
public final class BrokerListenerGetSamples {
    /*
     * x-ms-original-file: 2025-10-01/BrokerListener_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: BrokerListener_Get.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void brokerListenerGet(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.brokerListeners()
            .getWithResponse("rgiotoperations", "resource-name123", "resource-name123", "resource-name123",
                com.azure.core.util.Context.NONE);
    }
}
```

### BrokerListener_ListByResourceGroup

```java
/**
 * Samples for BrokerListener ListByResourceGroup.
 */
public final class BrokerListenerListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-10-01/BrokerListener_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: BrokerListener_ListByResourceGroup.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        brokerListenerListByResourceGroup(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.brokerListeners()
            .listByResourceGroup("rgiotoperations", "resource-name123", "resource-name123",
                com.azure.core.util.Context.NONE);
    }
}
```

### Dataflow_CreateOrUpdate

```java
import com.azure.resourcemanager.iotoperations.models.DataflowBuiltInTransformationDataset;
import com.azure.resourcemanager.iotoperations.models.DataflowBuiltInTransformationFilter;
import com.azure.resourcemanager.iotoperations.models.DataflowBuiltInTransformationMap;
import com.azure.resourcemanager.iotoperations.models.DataflowBuiltInTransformationSettings;
import com.azure.resourcemanager.iotoperations.models.DataflowDestinationOperationSettings;
import com.azure.resourcemanager.iotoperations.models.DataflowMappingType;
import com.azure.resourcemanager.iotoperations.models.DataflowOperation;
import com.azure.resourcemanager.iotoperations.models.DataflowProperties;
import com.azure.resourcemanager.iotoperations.models.DataflowSourceOperationSettings;
import com.azure.resourcemanager.iotoperations.models.ExtendedLocation;
import com.azure.resourcemanager.iotoperations.models.ExtendedLocationType;
import com.azure.resourcemanager.iotoperations.models.FilterType;
import com.azure.resourcemanager.iotoperations.models.OperationType;
import com.azure.resourcemanager.iotoperations.models.OperationalMode;
import com.azure.resourcemanager.iotoperations.models.SourceSerializationFormat;
import com.azure.resourcemanager.iotoperations.models.TransformationSerializationFormat;
import java.util.Arrays;

/**
 * Samples for Dataflow CreateOrUpdate.
 */
public final class DataflowCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-01/Dataflow_CreateOrUpdate_FilterToTopic.json
     */
    /**
     * Sample code: Dataflow_CreateOrUpdate_FilterToTopic.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        dataflowCreateOrUpdateFilterToTopic(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflows()
            .define("mqtt-filter-to-topic")
            .withExistingDataflowProfile("rgiotoperations", "resource-name123", "resource-name123")
            .withProperties(
                new DataflowProperties().withMode(OperationalMode.ENABLED)
                    .withOperations(
                        Arrays.asList(
                            new DataflowOperation().withOperationType(OperationType.SOURCE)
                                .withName("source1")
                                .withSourceSettings(new DataflowSourceOperationSettings()
                                    .withEndpointRef("aio-builtin-broker-endpoint")
                                    .withDataSources(Arrays.asList("azure-iot-operations/data/thermostat"))),
                            new DataflowOperation().withOperationType(OperationType.BUILT_IN_TRANSFORMATION)
                                .withName("transformation1")
                                .withBuiltInTransformationSettings(new DataflowBuiltInTransformationSettings()
                                    .withFilter(Arrays
                                        .asList(new DataflowBuiltInTransformationFilter().withType(FilterType.FILTER)
                                            .withDescription("filter-datapoint")
                                            .withInputs(Arrays.asList("temperature.Value", "\"Tag 10\".Value"))
                                            .withExpression("$1 > 9000 && $2 >= 8000")))
                                    .withMap(Arrays.asList(new DataflowBuiltInTransformationMap()
                                        .withType(DataflowMappingType.PASS_THROUGH)
                                        .withInputs(Arrays.asList("*"))
                                        .withOutput("*")))),
                            new DataflowOperation().withOperationType(OperationType.DESTINATION)
                                .withName("destination1")
                                .withDestinationSettings(new DataflowDestinationOperationSettings()
                                    .withEndpointRef("aio-builtin-broker-endpoint")
                                    .withDataDestination("data/filtered/thermostat")))))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-01/Dataflow_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Dataflow_CreateOrUpdate.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void dataflowCreateOrUpdate(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflows()
            .define("resource-name123")
            .withExistingDataflowProfile("rgiotoperations", "resource-name123", "resource-name123")
            .withProperties(
                new DataflowProperties().withMode(OperationalMode.ENABLED)
                    .withRequestDiskPersistence(OperationalMode.DISABLED)
                    .withOperations(Arrays.asList(new DataflowOperation().withOperationType(OperationType.SOURCE)
                        .withName("knnafvkwoeakm")
                        .withSourceSettings(new DataflowSourceOperationSettings()
                            .withEndpointRef("iixotodhvhkkfcfyrkoveslqig")
                            .withAssetRef("zayyykwmckaocywdkohmu")
                            .withSerializationFormat(SourceSerializationFormat.JSON)
                            .withSchemaRef("pknmdzqll")
                            .withDataSources(Arrays.asList("chkkpymxhp")))
                        .withBuiltInTransformationSettings(new DataflowBuiltInTransformationSettings()
                            .withSerializationFormat(TransformationSerializationFormat.DELTA)
                            .withSchemaRef("mcdc")
                            .withDatasets(
                                Arrays.asList(new DataflowBuiltInTransformationDataset().withKey("fakeTokenPlaceholder")
                                    .withDescription("Lorem ipsum odor amet, consectetuer adipiscing elit.")
                                    .withSchemaRef("n")
                                    .withInputs(Arrays.asList("mosffpsslifkq"))
                                    .withExpression("aatbwomvflemsxialv")))
                            .withFilter(
                                Arrays.asList(new DataflowBuiltInTransformationFilter().withType(FilterType.FILTER)
                                    .withDescription("Lorem ipsum odor amet, consectetuer adipiscing elit.")
                                    .withInputs(Arrays.asList("sxmjkbntgb"))
                                    .withExpression("n")))
                            .withMap(Arrays.asList(
                                new DataflowBuiltInTransformationMap().withType(DataflowMappingType.NEW_PROPERTIES)
                                    .withDescription("Lorem ipsum odor amet, consectetuer adipiscing elit.")
                                    .withInputs(Arrays.asList("xsbxuk"))
                                    .withExpression("txoiltogsarwkzalsphvlmt")
                                    .withOutput("nvgtmkfl"))))
                        .withDestinationSettings(
                            new DataflowDestinationOperationSettings().withEndpointRef("kybkchnzimerguekuvqlqiqdvvrt")
                                .withDataDestination("cbrh")))))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-01/Dataflow_CreateOrUpdate_ComplexContextualization.json
     */
    /**
     * Sample code: Dataflow_CreateOrUpdate_ComplexContextualization.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void dataflowCreateOrUpdateComplexContextualization(
        com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflows()
            .define("aio-to-adx-contexualized")
            .withExistingDataflowProfile("rgiotoperations", "resource-name123", "resource-name123")
            .withProperties(
                new DataflowProperties().withMode(OperationalMode.ENABLED)
                    .withOperations(Arrays.asList(
                        new DataflowOperation().withOperationType(OperationType.SOURCE)
                            .withName("source1")
                            .withSourceSettings(new DataflowSourceOperationSettings()
                                .withEndpointRef("aio-builtin-broker-endpoint")
                                .withDataSources(Arrays.asList("azure-iot-operations/data/thermostat"))),
                        new DataflowOperation().withOperationType(OperationType.BUILT_IN_TRANSFORMATION)
                            .withName("transformation1")
                            .withBuiltInTransformationSettings(new DataflowBuiltInTransformationSettings()
                                .withDatasets(Arrays
                                    .asList(new DataflowBuiltInTransformationDataset().withKey("fakeTokenPlaceholder")
                                        .withInputs(Arrays.asList("$source.country", "$context.country"))
                                        .withExpression("$1 == $2")))
                                .withMap(Arrays.asList(
                                    new DataflowBuiltInTransformationMap().withInputs(Arrays.asList("*"))
                                        .withOutput("*"),
                                    new DataflowBuiltInTransformationMap()
                                        .withInputs(Arrays.asList("$context(quality).*"))
                                        .withOutput("enriched.*")))),
                        new DataflowOperation().withOperationType(OperationType.DESTINATION)
                            .withName("destination1")
                            .withDestinationSettings(
                                new DataflowDestinationOperationSettings().withEndpointRef("adx-endpoint")
                                    .withDataDestination("mytable")))))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-01/Dataflow_CreateOrUpdate_ComplexEventHub.json
     */
    /**
     * Sample code: Dataflow_CreateOrUpdate_ComplexEventHub.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        dataflowCreateOrUpdateComplexEventHub(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflows()
            .define("aio-to-event-hub-transformed")
            .withExistingDataflowProfile("rgiotoperations", "resource-name123", "resource-name123")
            .withProperties(
                new DataflowProperties().withMode(OperationalMode.ENABLED)
                    .withOperations(
                        Arrays
                            .asList(
                                new DataflowOperation().withOperationType(OperationType.SOURCE)
                                    .withName("source1")
                                    .withSourceSettings(new DataflowSourceOperationSettings()
                                        .withEndpointRef("aio-builtin-broker-endpoint")
                                        .withDataSources(Arrays.asList("azure-iot-operations/data/thermostat"))),
                                new DataflowOperation().withOperationType(OperationType.BUILT_IN_TRANSFORMATION)
                                    .withBuiltInTransformationSettings(new DataflowBuiltInTransformationSettings()
                                        .withFilter(Arrays.asList(new DataflowBuiltInTransformationFilter()
                                            .withInputs(Arrays.asList("temperature.Value", "\"Tag 10\".Value"))
                                            .withExpression("$1 > 9000 && $2 >= 8000")))
                                        .withMap(Arrays.asList(
                                            new DataflowBuiltInTransformationMap().withInputs(Arrays.asList("*"))
                                                .withOutput("*"),
                                            new DataflowBuiltInTransformationMap()
                                                .withInputs(Arrays.asList("temperature.Value", "\"Tag 10\".Value"))
                                                .withExpression("($1+$2)/2")
                                                .withOutput("AvgTemp.Value"),
                                            new DataflowBuiltInTransformationMap().withInputs(Arrays.asList())
                                                .withExpression("true")
                                                .withOutput("dataflow-processed"),
                                            new DataflowBuiltInTransformationMap()
                                                .withInputs(Arrays.asList("temperature.SourceTimestamp"))
                                                .withExpression("")
                                                .withOutput(""),
                                            new DataflowBuiltInTransformationMap()
                                                .withInputs(Arrays.asList("\"Tag 10\""))
                                                .withExpression("")
                                                .withOutput("pressure"),
                                            new DataflowBuiltInTransformationMap()
                                                .withInputs(Arrays.asList("temperature.Value"))
                                                .withExpression("cToF($1)")
                                                .withOutput("temperatureF.Value"),
                                            new DataflowBuiltInTransformationMap()
                                                .withInputs(Arrays.asList("\"Tag 10\".Value"))
                                                .withExpression("scale ($1,0,10,0,100)")
                                                .withOutput("\"Scale Tag 10\".Value")))),
                                new DataflowOperation().withOperationType(OperationType.DESTINATION)
                                    .withName("destination1")
                                    .withDestinationSettings(
                                        new DataflowDestinationOperationSettings().withEndpointRef("event-hub-endpoint")
                                            .withDataDestination("myuniqueeventhub")))))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-01/Dataflow_CreateOrUpdate_SimpleFabric.json
     */
    /**
     * Sample code: Dataflow_CreateOrUpdate_SimpleFabric.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        dataflowCreateOrUpdateSimpleFabric(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflows()
            .define("aio-to-fabric")
            .withExistingDataflowProfile("rgiotoperations", "resource-name123", "resource-name123")
            .withProperties(
                new DataflowProperties().withMode(OperationalMode.ENABLED)
                    .withOperations(
                        Arrays
                            .asList(
                                new DataflowOperation().withOperationType(OperationType.SOURCE)
                                    .withName("source1")
                                    .withSourceSettings(new DataflowSourceOperationSettings()
                                        .withEndpointRef("aio-builtin-broker-endpoint")
                                        .withDataSources(Arrays.asList("azure-iot-operations/data/thermostat"))),
                                new DataflowOperation().withOperationType(OperationType.BUILT_IN_TRANSFORMATION)
                                    .withBuiltInTransformationSettings(new DataflowBuiltInTransformationSettings()
                                        .withSerializationFormat(TransformationSerializationFormat.PARQUET)
                                        .withSchemaRef("aio-sr://exampleNamespace/exmapleParquetSchema:1.0.0")),
                                new DataflowOperation().withOperationType(OperationType.DESTINATION)
                                    .withName("destination1")
                                    .withDestinationSettings(
                                        new DataflowDestinationOperationSettings().withEndpointRef("fabric-endpoint")
                                            .withDataDestination("telemetryTable")))))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-01/Dataflow_CreateOrUpdate_SimpleEventGrid.json
     */
    /**
     * Sample code: Dataflow_CreateOrUpdate_SimpleEventGrid.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        dataflowCreateOrUpdateSimpleEventGrid(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflows()
            .define("aio-to-event-grid")
            .withExistingDataflowProfile("rgiotoperations", "resource-name123", "resource-name123")
            .withProperties(new DataflowProperties().withMode(OperationalMode.ENABLED)
                .withOperations(Arrays.asList(
                    new DataflowOperation().withOperationType(OperationType.SOURCE)
                        .withName("source1")
                        .withSourceSettings(
                            new DataflowSourceOperationSettings().withEndpointRef("aio-builtin-broker-endpoint")
                                .withDataSources(Arrays.asList("thermostats/+/telemetry/temperature/#"))),
                    new DataflowOperation().withOperationType(OperationType.DESTINATION)
                        .withName("destination1")
                        .withDestinationSettings(
                            new DataflowDestinationOperationSettings().withEndpointRef("event-grid-endpoint")
                                .withDataDestination("factory/telemetry")))))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }
}
```

### Dataflow_Delete

```java
/**
 * Samples for Dataflow Delete.
 */
public final class DataflowDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-01/Dataflow_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Dataflow_Delete.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void dataflowDelete(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflows()
            .delete("rgiotoperations", "resource-name123", "resource-name123", "resource-name123",
                com.azure.core.util.Context.NONE);
    }
}
```

### Dataflow_Get

```java
/**
 * Samples for Dataflow Get.
 */
public final class DataflowGetSamples {
    /*
     * x-ms-original-file: 2025-10-01/Dataflow_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Dataflow_Get.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void dataflowGet(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflows()
            .getWithResponse("rgiotoperations", "resource-name123", "resource-name123", "resource-name123",
                com.azure.core.util.Context.NONE);
    }
}
```

### Dataflow_ListByResourceGroup

```java
/**
 * Samples for Dataflow ListByResourceGroup.
 */
public final class DataflowListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-10-01/Dataflow_ListByProfileResource_MaximumSet_Gen.json
     */
    /**
     * Sample code: Dataflow_ListByProfileResource.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        dataflowListByProfileResource(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflows()
            .listByResourceGroup("rgiotoperations", "resource-name123", "resource-name123",
                com.azure.core.util.Context.NONE);
    }
}
```

### DataflowEndpoint_CreateOrUpdate

```java
import com.azure.resourcemanager.iotoperations.models.BatchingConfiguration;
import com.azure.resourcemanager.iotoperations.models.BrokerProtocolType;
import com.azure.resourcemanager.iotoperations.models.CloudEventAttributeType;
import com.azure.resourcemanager.iotoperations.models.DataExplorerAuthMethod;
import com.azure.resourcemanager.iotoperations.models.DataLakeStorageAuthMethod;
import com.azure.resourcemanager.iotoperations.models.DataflowEndpointAuthenticationAccessToken;
import com.azure.resourcemanager.iotoperations.models.DataflowEndpointAuthenticationSasl;
import com.azure.resourcemanager.iotoperations.models.DataflowEndpointAuthenticationSaslType;
import com.azure.resourcemanager.iotoperations.models.DataflowEndpointAuthenticationServiceAccountToken;
import com.azure.resourcemanager.iotoperations.models.DataflowEndpointAuthenticationSystemAssignedManagedIdentity;
import com.azure.resourcemanager.iotoperations.models.DataflowEndpointAuthenticationUserAssignedManagedIdentity;
import com.azure.resourcemanager.iotoperations.models.DataflowEndpointAuthenticationX509;
import com.azure.resourcemanager.iotoperations.models.DataflowEndpointDataExplorer;
import com.azure.resourcemanager.iotoperations.models.DataflowEndpointDataExplorerAuthentication;
import com.azure.resourcemanager.iotoperations.models.DataflowEndpointDataLakeStorage;
import com.azure.resourcemanager.iotoperations.models.DataflowEndpointDataLakeStorageAuthentication;
import com.azure.resourcemanager.iotoperations.models.DataflowEndpointFabricOneLake;
import com.azure.resourcemanager.iotoperations.models.DataflowEndpointFabricOneLakeAuthentication;
import com.azure.resourcemanager.iotoperations.models.DataflowEndpointFabricOneLakeNames;
import com.azure.resourcemanager.iotoperations.models.DataflowEndpointFabricPathType;
import com.azure.resourcemanager.iotoperations.models.DataflowEndpointKafka;
import com.azure.resourcemanager.iotoperations.models.DataflowEndpointKafkaAcks;
import com.azure.resourcemanager.iotoperations.models.DataflowEndpointKafkaAuthentication;
import com.azure.resourcemanager.iotoperations.models.DataflowEndpointKafkaBatching;
import com.azure.resourcemanager.iotoperations.models.DataflowEndpointKafkaCompression;
import com.azure.resourcemanager.iotoperations.models.DataflowEndpointKafkaPartitionStrategy;
import com.azure.resourcemanager.iotoperations.models.DataflowEndpointLocalStorage;
import com.azure.resourcemanager.iotoperations.models.DataflowEndpointMqtt;
import com.azure.resourcemanager.iotoperations.models.DataflowEndpointMqttAuthentication;
import com.azure.resourcemanager.iotoperations.models.DataflowEndpointProperties;
import com.azure.resourcemanager.iotoperations.models.EndpointType;
import com.azure.resourcemanager.iotoperations.models.ExtendedLocation;
import com.azure.resourcemanager.iotoperations.models.ExtendedLocationType;
import com.azure.resourcemanager.iotoperations.models.FabricOneLakeAuthMethod;
import com.azure.resourcemanager.iotoperations.models.KafkaAuthMethod;
import com.azure.resourcemanager.iotoperations.models.MqttAuthMethod;
import com.azure.resourcemanager.iotoperations.models.MqttRetainType;
import com.azure.resourcemanager.iotoperations.models.OperationalMode;
import com.azure.resourcemanager.iotoperations.models.TlsProperties;

/**
 * Samples for DataflowEndpoint CreateOrUpdate.
 */
public final class DataflowEndpointCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-01/DataflowEndpoint_CreateOrUpdate_EventGrid.json
     */
    /**
     * Sample code: DataflowEndpoint_CreateOrUpdate_EventGrid.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        dataflowEndpointCreateOrUpdateEventGrid(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflowEndpoints()
            .define("event-grid-endpoint")
            .withExistingInstance("rgiotoperations", "resource-name123")
            .withProperties(new DataflowEndpointProperties().withEndpointType(EndpointType.MQTT)
                .withMqttSettings(new DataflowEndpointMqtt()
                    .withAuthentication(new DataflowEndpointMqttAuthentication()
                        .withMethod(MqttAuthMethod.SYSTEM_ASSIGNED_MANAGED_IDENTITY)
                        .withSystemAssignedManagedIdentitySettings(
                            new DataflowEndpointAuthenticationSystemAssignedManagedIdentity()))
                    .withHost("example.westeurope-1.ts.eventgrid.azure.net:8883")
                    .withTls(new TlsProperties().withMode(OperationalMode.ENABLED))))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-01/DataflowEndpoint_CreateOrUpdate_ADLSv2.json
     */
    /**
     * Sample code: DataflowEndpoint_CreateOrUpdate_ADLSv2.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        dataflowEndpointCreateOrUpdateADLSv2(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflowEndpoints()
            .define("adlsv2-endpoint")
            .withExistingInstance("rgiotoperations", "resource-name123")
            .withProperties(new DataflowEndpointProperties().withEndpointType(EndpointType.DATA_LAKE_STORAGE)
                .withDataLakeStorageSettings(
                    new DataflowEndpointDataLakeStorage()
                        .withAuthentication(
                            new DataflowEndpointDataLakeStorageAuthentication()
                                .withMethod(DataLakeStorageAuthMethod.ACCESS_TOKEN)
                                .withAccessTokenSettings(new DataflowEndpointAuthenticationAccessToken()
                                    .withSecretRef("fakeTokenPlaceholder")))
                        .withHost("example.blob.core.windows.net")))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-01/DataflowEndpoint_CreateOrUpdate_EventHub.json
     */
    /**
     * Sample code: DataflowEndpoint_CreateOrUpdate_EventHub.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        dataflowEndpointCreateOrUpdateEventHub(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflowEndpoints()
            .define("event-hub-endpoint")
            .withExistingInstance("rgiotoperations", "resource-name123")
            .withProperties(new DataflowEndpointProperties().withEndpointType(EndpointType.KAFKA)
                .withKafkaSettings(new DataflowEndpointKafka()
                    .withAuthentication(new DataflowEndpointKafkaAuthentication()
                        .withMethod(KafkaAuthMethod.SYSTEM_ASSIGNED_MANAGED_IDENTITY)
                        .withSystemAssignedManagedIdentitySettings(
                            new DataflowEndpointAuthenticationSystemAssignedManagedIdentity()))
                    .withConsumerGroupId("aiodataflows")
                    .withHost("example.servicebus.windows.net:9093")
                    .withTls(new TlsProperties().withMode(OperationalMode.ENABLED))))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-01/DataflowEndpoint_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: DataflowEndpoint_CreateOrUpdate.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        dataflowEndpointCreateOrUpdate(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflowEndpoints()
            .define("resource-name123")
            .withExistingInstance("rgiotoperations", "resource-name123")
            .withProperties(
                new DataflowEndpointProperties().withEndpointType(EndpointType.DATA_EXPLORER)
                    .withDataExplorerSettings(
                        new DataflowEndpointDataExplorer()
                            .withAuthentication(
                                new DataflowEndpointDataExplorerAuthentication()
                                    .withMethod(DataExplorerAuthMethod.SYSTEM_ASSIGNED_MANAGED_IDENTITY)
                                    .withSystemAssignedManagedIdentitySettings(
                                        new DataflowEndpointAuthenticationSystemAssignedManagedIdentity()
                                            .withAudience("psxomrfbhoflycm"))
                                    .withUserAssignedManagedIdentitySettings(
                                        new DataflowEndpointAuthenticationUserAssignedManagedIdentity()
                                            .withClientId("fb90f267-8872-431a-a76a-a1cec5d3c4d2")
                                            .withScope("zop")
                                            .withTenantId("ed060aa2-71ff-4d3f-99c4-a9138356fdec")))
                            .withDatabase("yqcdpjsifm")
                            .withHost("<cluster>.<region>.kusto.windows.net")
                            .withBatching(new BatchingConfiguration().withLatencySeconds(9312).withMaxMessages(9028)))
                    .withDataLakeStorageSettings(
                        new DataflowEndpointDataLakeStorage()
                            .withAuthentication(new DataflowEndpointDataLakeStorageAuthentication()
                                .withMethod(DataLakeStorageAuthMethod.SYSTEM_ASSIGNED_MANAGED_IDENTITY)
                                .withAccessTokenSettings(new DataflowEndpointAuthenticationAccessToken()
                                    .withSecretRef("fakeTokenPlaceholder"))
                                .withSystemAssignedManagedIdentitySettings(
                                    new DataflowEndpointAuthenticationSystemAssignedManagedIdentity()
                                        .withAudience("psxomrfbhoflycm"))
                                .withUserAssignedManagedIdentitySettings(
                                    new DataflowEndpointAuthenticationUserAssignedManagedIdentity()
                                        .withClientId("fb90f267-8872-431a-a76a-a1cec5d3c4d2")
                                        .withScope("zop")
                                        .withTenantId("ed060aa2-71ff-4d3f-99c4-a9138356fdec")))
                            .withHost("<account>.blob.core.windows.net")
                            .withBatching(new BatchingConfiguration().withLatencySeconds(9312).withMaxMessages(9028)))
                    .withFabricOneLakeSettings(new DataflowEndpointFabricOneLake()
                        .withAuthentication(new DataflowEndpointFabricOneLakeAuthentication()
                            .withMethod(FabricOneLakeAuthMethod.SYSTEM_ASSIGNED_MANAGED_IDENTITY)
                            .withSystemAssignedManagedIdentitySettings(
                                new DataflowEndpointAuthenticationSystemAssignedManagedIdentity()
                                    .withAudience("psxomrfbhoflycm"))
                            .withUserAssignedManagedIdentitySettings(
                                new DataflowEndpointAuthenticationUserAssignedManagedIdentity()
                                    .withClientId("fb90f267-8872-431a-a76a-a1cec5d3c4d2")
                                    .withScope("zop")
                                    .withTenantId("ed060aa2-71ff-4d3f-99c4-a9138356fdec")))
                        .withNames(new DataflowEndpointFabricOneLakeNames().withLakehouseName("wpeathi")
                            .withWorkspaceName("nwgmitkbljztgms"))
                        .withOneLakePathType(DataflowEndpointFabricPathType.FILES)
                        .withHost("https://<host>.fabric.microsoft.com")
                        .withBatching(new BatchingConfiguration().withLatencySeconds(9312).withMaxMessages(9028)))
                    .withKafkaSettings(new DataflowEndpointKafka()
                        .withAuthentication(new DataflowEndpointKafkaAuthentication()
                            .withMethod(KafkaAuthMethod.SYSTEM_ASSIGNED_MANAGED_IDENTITY)
                            .withSystemAssignedManagedIdentitySettings(
                                new DataflowEndpointAuthenticationSystemAssignedManagedIdentity()
                                    .withAudience("psxomrfbhoflycm"))
                            .withUserAssignedManagedIdentitySettings(
                                new DataflowEndpointAuthenticationUserAssignedManagedIdentity()
                                    .withClientId("fb90f267-8872-431a-a76a-a1cec5d3c4d2")
                                    .withScope("zop")
                                    .withTenantId("ed060aa2-71ff-4d3f-99c4-a9138356fdec"))
                            .withSaslSettings(new DataflowEndpointAuthenticationSasl()
                                .withSaslType(DataflowEndpointAuthenticationSaslType.PLAIN)
                                .withSecretRef("fakeTokenPlaceholder"))
                            .withX509CertificateSettings(
                                new DataflowEndpointAuthenticationX509().withSecretRef("fakeTokenPlaceholder")))
                        .withConsumerGroupId("ukkzcjiyenhxokat")
                        .withHost("pwcqfiqclcgneolpewnyavoulbip")
                        .withBatching(new DataflowEndpointKafkaBatching().withMode(OperationalMode.ENABLED)
                            .withLatencyMs(3679)
                            .withMaxBytes(8887)
                            .withMaxMessages(2174))
                        .withCopyMqttProperties(OperationalMode.ENABLED)
                        .withCompression(DataflowEndpointKafkaCompression.NONE)
                        .withKafkaAcks(DataflowEndpointKafkaAcks.ZERO)
                        .withPartitionStrategy(DataflowEndpointKafkaPartitionStrategy.DEFAULT)
                        .withTls(new TlsProperties().withMode(OperationalMode.ENABLED)
                            .withTrustedCaCertificateConfigMapRef("tectjjvukvelsreihwadh"))
                        .withCloudEventAttributes(CloudEventAttributeType.fromString("PassThrough")))
                    .withLocalStorageSettings(
                        new DataflowEndpointLocalStorage().withPersistentVolumeClaimRef("jjwqwvd"))
                    .withMqttSettings(new DataflowEndpointMqtt()
                        .withAuthentication(new DataflowEndpointMqttAuthentication()
                            .withMethod(MqttAuthMethod.SYSTEM_ASSIGNED_MANAGED_IDENTITY)
                            .withSystemAssignedManagedIdentitySettings(
                                new DataflowEndpointAuthenticationSystemAssignedManagedIdentity()
                                    .withAudience("psxomrfbhoflycm"))
                            .withUserAssignedManagedIdentitySettings(
                                new DataflowEndpointAuthenticationUserAssignedManagedIdentity()
                                    .withClientId("fb90f267-8872-431a-a76a-a1cec5d3c4d2")
                                    .withScope("zop")
                                    .withTenantId("ed060aa2-71ff-4d3f-99c4-a9138356fdec"))
                            .withServiceAccountTokenSettings(new DataflowEndpointAuthenticationServiceAccountToken()
                                .withAudience("ejbklrbxgjaqleoycgpje"))
                            .withX509CertificateSettings(
                                new DataflowEndpointAuthenticationX509().withSecretRef("fakeTokenPlaceholder")))
                        .withClientIdPrefix("kkljsdxdirfhwxtkavldekeqhv")
                        .withHost("nyhnxqnbspstctl")
                        .withProtocol(BrokerProtocolType.MQTT)
                        .withKeepAliveSeconds(0)
                        .withRetain(MqttRetainType.KEEP)
                        .withMaxInflightMessages(0)
                        .withQos(1)
                        .withSessionExpirySeconds(0)
                        .withTls(new TlsProperties().withMode(OperationalMode.ENABLED)
                            .withTrustedCaCertificateConfigMapRef("tectjjvukvelsreihwadh"))
                        .withCloudEventAttributes(CloudEventAttributeType.fromString("PassThrough"))))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-01/DataflowEndpoint_CreateOrUpdate_ADX.json
     */
    /**
     * Sample code: DataflowEndpoint_CreateOrUpdate_ADX.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        dataflowEndpointCreateOrUpdateADX(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflowEndpoints()
            .define("adx-endpoint")
            .withExistingInstance("rgiotoperations", "resource-name123")
            .withProperties(new DataflowEndpointProperties().withEndpointType(EndpointType.DATA_EXPLORER)
                .withDataExplorerSettings(new DataflowEndpointDataExplorer()
                    .withAuthentication(new DataflowEndpointDataExplorerAuthentication()
                        .withMethod(DataExplorerAuthMethod.SYSTEM_ASSIGNED_MANAGED_IDENTITY)
                        .withSystemAssignedManagedIdentitySettings(
                            new DataflowEndpointAuthenticationSystemAssignedManagedIdentity()))
                    .withDatabase("example-database")
                    .withHost("example.westeurope.kusto.windows.net")
                    .withBatching(new BatchingConfiguration().withLatencySeconds(9312).withMaxMessages(9028))))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-01/DataflowEndpoint_CreateOrUpdate_Fabric.json
     */
    /**
     * Sample code: DataflowEndpoint_CreateOrUpdate_Fabric.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        dataflowEndpointCreateOrUpdateFabric(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflowEndpoints()
            .define("fabric-endpoint")
            .withExistingInstance("rgiotoperations", "resource-name123")
            .withProperties(new DataflowEndpointProperties().withEndpointType(EndpointType.FABRIC_ONE_LAKE)
                .withFabricOneLakeSettings(new DataflowEndpointFabricOneLake()
                    .withAuthentication(new DataflowEndpointFabricOneLakeAuthentication()
                        .withMethod(FabricOneLakeAuthMethod.SYSTEM_ASSIGNED_MANAGED_IDENTITY)
                        .withSystemAssignedManagedIdentitySettings(
                            new DataflowEndpointAuthenticationSystemAssignedManagedIdentity()))
                    .withNames(new DataflowEndpointFabricOneLakeNames().withLakehouseName("example-lakehouse")
                        .withWorkspaceName("example-workspace"))
                    .withOneLakePathType(DataflowEndpointFabricPathType.TABLES)
                    .withHost("onelake.dfs.fabric.microsoft.com")))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-01/DataflowEndpoint_CreateOrUpdate_LocalStorage.json
     */
    /**
     * Sample code: DataflowEndpoint_CreateOrUpdate_LocalStorage.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void dataflowEndpointCreateOrUpdateLocalStorage(
        com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflowEndpoints()
            .define("local-storage-endpoint")
            .withExistingInstance("rgiotoperations", "resource-name123")
            .withProperties(new DataflowEndpointProperties().withEndpointType(EndpointType.LOCAL_STORAGE)
                .withLocalStorageSettings(
                    new DataflowEndpointLocalStorage().withPersistentVolumeClaimRef("example-pvc")))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-01/DataflowEndpoint_CreateOrUpdate_AIO.json
     */
    /**
     * Sample code: DataflowEndpoint_CreateOrUpdate_AIO.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        dataflowEndpointCreateOrUpdateAIO(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflowEndpoints()
            .define("aio-builtin-broker-endpoint")
            .withExistingInstance("rgiotoperations", "resource-name123")
            .withProperties(
                new DataflowEndpointProperties().withEndpointType(EndpointType.MQTT)
                    .withMqttSettings(new DataflowEndpointMqtt()
                        .withAuthentication(
                            new DataflowEndpointMqttAuthentication().withMethod(MqttAuthMethod.fromString("Kubernetes"))
                                .withServiceAccountTokenSettings(new DataflowEndpointAuthenticationServiceAccountToken()
                                    .withAudience("aio-internal")))
                        .withHost("aio-broker:18883")
                        .withTls(new TlsProperties().withMode(OperationalMode.ENABLED)
                            .withTrustedCaCertificateConfigMapRef("aio-ca-trust-bundle-test-only"))))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-01/DataflowEndpoint_CreateOrUpdate_MQTT.json
     */
    /**
     * Sample code: DataflowEndpoint_CreateOrUpdate_MQTT.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        dataflowEndpointCreateOrUpdateMQTT(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflowEndpoints()
            .define("generic-mqtt-broker-endpoint")
            .withExistingInstance("rgiotoperations", "resource-name123")
            .withProperties(new DataflowEndpointProperties().withEndpointType(EndpointType.MQTT)
                .withMqttSettings(new DataflowEndpointMqtt()
                    .withAuthentication(
                        new DataflowEndpointMqttAuthentication().withMethod(MqttAuthMethod.X509CERTIFICATE)
                            .withX509CertificateSettings(
                                new DataflowEndpointAuthenticationX509().withSecretRef("fakeTokenPlaceholder")))
                    .withClientIdPrefix("factory-gateway")
                    .withHost("example.broker.local:1883")
                    .withProtocol(BrokerProtocolType.WEB_SOCKETS)
                    .withKeepAliveSeconds(60)
                    .withRetain(MqttRetainType.KEEP)
                    .withMaxInflightMessages(100)
                    .withQos(1)
                    .withSessionExpirySeconds(3600)
                    .withTls(new TlsProperties().withMode(OperationalMode.DISABLED))))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-01/DataflowEndpoint_CreateOrUpdate_Kafka.json
     */
    /**
     * Sample code: DataflowEndpoint_CreateOrUpdate_Kafka.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        dataflowEndpointCreateOrUpdateKafka(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflowEndpoints()
            .define("generic-kafka-endpoint")
            .withExistingInstance("rgiotoperations", "resource-name123")
            .withProperties(new DataflowEndpointProperties().withEndpointType(EndpointType.KAFKA)
                .withKafkaSettings(new DataflowEndpointKafka()
                    .withAuthentication(new DataflowEndpointKafkaAuthentication().withMethod(KafkaAuthMethod.SASL)
                        .withSaslSettings(new DataflowEndpointAuthenticationSasl()
                            .withSaslType(DataflowEndpointAuthenticationSaslType.PLAIN)
                            .withSecretRef("fakeTokenPlaceholder")))
                    .withConsumerGroupId("dataflows")
                    .withHost("example.kafka.local:9093")
                    .withBatching(new DataflowEndpointKafkaBatching().withMode(OperationalMode.ENABLED)
                        .withLatencyMs(5)
                        .withMaxBytes(1000000)
                        .withMaxMessages(100000))
                    .withCopyMqttProperties(OperationalMode.ENABLED)
                    .withCompression(DataflowEndpointKafkaCompression.GZIP)
                    .withKafkaAcks(DataflowEndpointKafkaAcks.ALL)
                    .withPartitionStrategy(DataflowEndpointKafkaPartitionStrategy.DEFAULT)
                    .withTls(new TlsProperties().withMode(OperationalMode.ENABLED)
                        .withTrustedCaCertificateConfigMapRef("ca-certificates"))
                    .withCloudEventAttributes(CloudEventAttributeType.PROPAGATE)))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }
}
```

### DataflowEndpoint_Delete

```java
/**
 * Samples for DataflowEndpoint Delete.
 */
public final class DataflowEndpointDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-01/DataflowEndpoint_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: DataflowEndpoint_Delete.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void dataflowEndpointDelete(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflowEndpoints()
            .delete("rgiotoperations", "resource-name123", "resource-name123", com.azure.core.util.Context.NONE);
    }
}
```

### DataflowEndpoint_Get

```java
/**
 * Samples for DataflowEndpoint Get.
 */
public final class DataflowEndpointGetSamples {
    /*
     * x-ms-original-file: 2025-10-01/DataflowEndpoint_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: DataflowEndpoint_Get.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void dataflowEndpointGet(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflowEndpoints()
            .getWithResponse("rgiotoperations", "resource-name123", "resource-name123",
                com.azure.core.util.Context.NONE);
    }
}
```

### DataflowEndpoint_ListByResourceGroup

```java
/**
 * Samples for DataflowEndpoint ListByResourceGroup.
 */
public final class DataflowEndpointListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-10-01/DataflowEndpoint_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: DataflowEndpoint_ListByResourceGroup.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        dataflowEndpointListByResourceGroup(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflowEndpoints()
            .listByResourceGroup("rgiotoperations", "resource-name123", com.azure.core.util.Context.NONE);
    }
}
```

### DataflowGraph_CreateOrUpdate

```java
import com.azure.resourcemanager.iotoperations.models.DataflowGraphConnectionInput;
import com.azure.resourcemanager.iotoperations.models.DataflowGraphConnectionOutput;
import com.azure.resourcemanager.iotoperations.models.DataflowGraphConnectionSchemaSerializationFormat;
import com.azure.resourcemanager.iotoperations.models.DataflowGraphConnectionSchemaSettings;
import com.azure.resourcemanager.iotoperations.models.DataflowGraphDestinationNode;
import com.azure.resourcemanager.iotoperations.models.DataflowGraphDestinationNodeSettings;
import com.azure.resourcemanager.iotoperations.models.DataflowGraphGraphNode;
import com.azure.resourcemanager.iotoperations.models.DataflowGraphGraphNodeConfiguration;
import com.azure.resourcemanager.iotoperations.models.DataflowGraphNodeConnection;
import com.azure.resourcemanager.iotoperations.models.DataflowGraphNodeGraphSettings;
import com.azure.resourcemanager.iotoperations.models.DataflowGraphProperties;
import com.azure.resourcemanager.iotoperations.models.DataflowGraphSourceNode;
import com.azure.resourcemanager.iotoperations.models.DataflowGraphSourceSettings;
import com.azure.resourcemanager.iotoperations.models.ExtendedLocation;
import com.azure.resourcemanager.iotoperations.models.ExtendedLocationType;
import com.azure.resourcemanager.iotoperations.models.OperationalMode;
import java.util.Arrays;

/**
 * Samples for DataflowGraph CreateOrUpdate.
 */
public final class DataflowGraphCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-01/DataflowGraph_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: DataflowGraph_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        dataflowGraphCreateOrUpdateMaximumSet(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflowGraphs()
            .define("resource-123")
            .withExistingDataflowProfile("rgiotoperations", "resource-123", "resource-123")
            .withProperties(
                new DataflowGraphProperties().withMode(OperationalMode.ENABLED)
                    .withRequestDiskPersistence(OperationalMode.ENABLED)
                    .withNodes(
                        Arrays.asList(new DataflowGraphSourceNode().withName("temperature")
                            .withSourceSettings(new DataflowGraphSourceSettings().withEndpointRef("default")
                                .withDataSources(Arrays.asList("telemetry/temperature"))),
                            new DataflowGraphGraphNode().withName("my-graph")
                                .withGraphSettings(new DataflowGraphNodeGraphSettings().withRegistryEndpointRef(
                                    "my-registry-endpoint")
                                    .withArtifact("my-wasm-module:1.4.3")
                                    .withConfiguration(Arrays.asList(
                                        new DataflowGraphGraphNodeConfiguration().withKey("fakeTokenPlaceholder")
                                            .withValue("value1"),
                                        new DataflowGraphGraphNodeConfiguration().withKey("fakeTokenPlaceholder")
                                            .withValue("value2")))),
                            new DataflowGraphDestinationNode().withName("alert")
                                .withDestinationSettings(
                                    new DataflowGraphDestinationNodeSettings().withEndpointRef("default")
                                        .withDataDestination("telemetry/temperature/alert")),
                            new DataflowGraphDestinationNode().withName("fabric")
                                .withDestinationSettings(
                                    new DataflowGraphDestinationNodeSettings().withEndpointRef("fabric")
                                        .withDataDestination("my-table"))))
                    .withNodeConnections(
                        Arrays
                            .asList(
                                new DataflowGraphNodeConnection()
                                    .withFrom(
                                        new DataflowGraphConnectionInput().withName("temperature")
                                            .withSchema(new DataflowGraphConnectionSchemaSettings()
                                                .withSerializationFormat(
                                                    DataflowGraphConnectionSchemaSerializationFormat.AVRO)
                                                .withSchemaRef("aio-sr://namespace/temperature:1")))
                                    .withTo(new DataflowGraphConnectionOutput().withName("my-graph")),
                                new DataflowGraphNodeConnection()
                                    .withFrom(
                                        new DataflowGraphConnectionInput().withName("my-graph.alert-output")
                                            .withSchema(new DataflowGraphConnectionSchemaSettings()
                                                .withSerializationFormat(
                                                    DataflowGraphConnectionSchemaSerializationFormat.AVRO)
                                                .withSchemaRef("aio-sr://namespace/alert:1")))
                                    .withTo(new DataflowGraphConnectionOutput().withName("fabric")),
                                new DataflowGraphNodeConnection()
                                    .withFrom(new DataflowGraphConnectionInput().withName("my-graph.normal-output")
                                        .withSchema(new DataflowGraphConnectionSchemaSettings()
                                            .withSerializationFormat(
                                                DataflowGraphConnectionSchemaSerializationFormat.AVRO)
                                            .withSchemaRef("aio-sr://namespace/alert:1")))
                                    .withTo(new DataflowGraphConnectionOutput().withName("fabric")))))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }
}
```

### DataflowGraph_Delete

```java
/**
 * Samples for DataflowGraph Delete.
 */
public final class DataflowGraphDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-01/DataflowGraph_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: DataflowGraph_Delete_MaximumSet.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        dataflowGraphDeleteMaximumSet(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflowGraphs()
            .delete("rgiotoperations", "resource-123", "resource-123", "resource-123",
                com.azure.core.util.Context.NONE);
    }
}
```

### DataflowGraph_Get

```java
/**
 * Samples for DataflowGraph Get.
 */
public final class DataflowGraphGetSamples {
    /*
     * x-ms-original-file: 2025-10-01/DataflowGraph_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: DataflowGraph_Get_MaximumSet.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        dataflowGraphGetMaximumSet(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflowGraphs()
            .getWithResponse("rgiotoperations", "resource-123", "resource-123", "resource-123",
                com.azure.core.util.Context.NONE);
    }
}
```

### DataflowGraph_ListByDataflowProfile

```java
/**
 * Samples for DataflowGraph ListByDataflowProfile.
 */
public final class DataflowGraphListByDataflowProfileSamples {
    /*
     * x-ms-original-file: 2025-10-01/DataflowGraph_ListByDataflowProfile_MaximumSet_Gen.json
     */
    /**
     * Sample code: DataflowGraph_ListByDataflowProfile_MaximumSet.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void dataflowGraphListByDataflowProfileMaximumSet(
        com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflowGraphs()
            .listByDataflowProfile("rgiotoperations", "resource-123", "resource-123", com.azure.core.util.Context.NONE);
    }
}
```

### DataflowProfile_CreateOrUpdate

```java
import com.azure.resourcemanager.iotoperations.models.DataflowProfileProperties;
import com.azure.resourcemanager.iotoperations.models.DiagnosticsLogs;
import com.azure.resourcemanager.iotoperations.models.ExtendedLocation;
import com.azure.resourcemanager.iotoperations.models.ExtendedLocationType;
import com.azure.resourcemanager.iotoperations.models.Metrics;
import com.azure.resourcemanager.iotoperations.models.ProfileDiagnostics;

/**
 * Samples for DataflowProfile CreateOrUpdate.
 */
public final class DataflowProfileCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-01/DataflowProfile_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: DataflowProfile_CreateOrUpdate.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        dataflowProfileCreateOrUpdate(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflowProfiles()
            .define("resource-name123")
            .withExistingInstance("rgiotoperations", "resource-name123")
            .withProperties(new DataflowProfileProperties().withDiagnostics(
                new ProfileDiagnostics().withLogs(new DiagnosticsLogs().withLevel("rnmwokumdmebpmfxxxzvvjfdywotav"))
                    .withMetrics(new Metrics().withPrometheusPort(7581)))
                .withInstanceCount(14))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-01/DataflowProfile_CreateOrUpdate_Minimal.json
     */
    /**
     * Sample code: DataflowProfile_CreateOrUpdate_Minimal.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        dataflowProfileCreateOrUpdateMinimal(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflowProfiles()
            .define("aio-dataflowprofile")
            .withExistingInstance("rgiotoperations", "resource-name123")
            .withProperties(new DataflowProfileProperties().withInstanceCount(1))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }

    /*
     * x-ms-original-file: 2025-10-01/DataflowProfile_CreateOrUpdate_Multi.json
     */
    /**
     * Sample code: DataflowProfile_CreateOrUpdate_Multi.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        dataflowProfileCreateOrUpdateMulti(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflowProfiles()
            .define("aio-dataflowprofile")
            .withExistingInstance("rgiotoperations", "resource-name123")
            .withProperties(new DataflowProfileProperties().withInstanceCount(3))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }
}
```

### DataflowProfile_Delete

```java
/**
 * Samples for DataflowProfile Delete.
 */
public final class DataflowProfileDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-01/DataflowProfile_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: DataflowProfile_Delete.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void dataflowProfileDelete(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflowProfiles()
            .delete("rgiotoperations", "resource-name123", "resource-name123", com.azure.core.util.Context.NONE);
    }
}
```

### DataflowProfile_Get

```java
/**
 * Samples for DataflowProfile Get.
 */
public final class DataflowProfileGetSamples {
    /*
     * x-ms-original-file: 2025-10-01/DataflowProfile_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: DataflowProfile_Get.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void dataflowProfileGet(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflowProfiles()
            .getWithResponse("rgiotoperations", "resource-name123", "resource-name123",
                com.azure.core.util.Context.NONE);
    }
}
```

### DataflowProfile_ListByResourceGroup

```java
/**
 * Samples for DataflowProfile ListByResourceGroup.
 */
public final class DataflowProfileListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-10-01/DataflowProfile_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: DataflowProfile_ListByResourceGroup.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        dataflowProfileListByResourceGroup(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.dataflowProfiles()
            .listByResourceGroup("rgiotoperations", "resource-name123", com.azure.core.util.Context.NONE);
    }
}
```

### Instance_CreateOrUpdate

```java
import com.azure.resourcemanager.iotoperations.models.ExtendedLocation;
import com.azure.resourcemanager.iotoperations.models.ExtendedLocationType;
import com.azure.resourcemanager.iotoperations.models.InstanceProperties;
import com.azure.resourcemanager.iotoperations.models.ManagedServiceIdentity;
import com.azure.resourcemanager.iotoperations.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.iotoperations.models.SchemaRegistryRef;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Instance CreateOrUpdate.
 */
public final class InstanceCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-01/Instance_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Instance_CreateOrUpdate.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void instanceCreateOrUpdate(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.instances()
            .define("aio-instance")
            .withRegion("eastus2")
            .withExistingResourceGroup("rgiotoperations")
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .withTags(mapOf())
            .withProperties(new InstanceProperties().withDescription("kpqtgocs")
                .withSchemaRegistryRef(new SchemaRegistryRef().withResourceId(
                    "/subscriptions/0000000-0000-0000-0000-000000000000/resourceGroups/resourceGroup123/providers/Microsoft.DeviceRegistry/schemaRegistries/resource-name123")))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.NONE)
                .withUserAssignedIdentities(mapOf()))
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

### Instance_Delete

```java
/**
 * Samples for Instance Delete.
 */
public final class InstanceDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-01/Instance_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Instance_Delete.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void instanceDelete(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.instances().delete("rgiotoperations", "aio-instance", com.azure.core.util.Context.NONE);
    }
}
```

### Instance_GetByResourceGroup

```java
/**
 * Samples for Instance GetByResourceGroup.
 */
public final class InstanceGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-10-01/Instance_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Instance_Get.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void instanceGet(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.instances()
            .getByResourceGroupWithResponse("rgiotoperations", "aio-instance", com.azure.core.util.Context.NONE);
    }
}
```

### Instance_List

```java
/**
 * Samples for Instance List.
 */
public final class InstanceListSamples {
    /*
     * x-ms-original-file: 2025-10-01/Instance_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: Instance_ListBySubscription.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        instanceListBySubscription(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.instances().list(com.azure.core.util.Context.NONE);
    }
}
```

### Instance_ListByResourceGroup

```java
/**
 * Samples for Instance ListByResourceGroup.
 */
public final class InstanceListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-10-01/Instance_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: Instance_ListByResourceGroup.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        instanceListByResourceGroup(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.instances().listByResourceGroup("rgiotoperations", com.azure.core.util.Context.NONE);
    }
}
```

### Instance_Update

```java
import com.azure.resourcemanager.iotoperations.models.InstanceResource;
import com.azure.resourcemanager.iotoperations.models.ManagedServiceIdentity;
import com.azure.resourcemanager.iotoperations.models.ManagedServiceIdentityType;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Instance Update.
 */
public final class InstanceUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-01/Instance_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Instance_Update.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void instanceUpdate(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        InstanceResource resource = manager.instances()
            .getByResourceGroupWithResponse("rgiotoperations", "aio-instance", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf())
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.NONE)
                .withUserAssignedIdentities(mapOf()))
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

### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2025-10-01/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void operationsList(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### RegistryEndpoint_CreateOrUpdate

```java
import com.azure.resourcemanager.iotoperations.models.ExtendedLocation;
import com.azure.resourcemanager.iotoperations.models.ExtendedLocationType;
import com.azure.resourcemanager.iotoperations.models.RegistryEndpointAnonymousAuthentication;
import com.azure.resourcemanager.iotoperations.models.RegistryEndpointAnonymousSettings;
import com.azure.resourcemanager.iotoperations.models.RegistryEndpointProperties;
import com.azure.resourcemanager.iotoperations.models.RegistryEndpointTrustedSigningKeyConfigMap;
import com.azure.resourcemanager.iotoperations.models.RegistryEndpointTrustedSigningKeySecret;
import java.util.Arrays;

/**
 * Samples for RegistryEndpoint CreateOrUpdate.
 */
public final class RegistryEndpointCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-10-01/RegistryEndpoint_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: RegistryEndpoint_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        registryEndpointCreateOrUpdateMaximumSet(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.registryEndpoints()
            .define("resource-123")
            .withExistingInstance("rgiotoperations", "resource-123")
            .withProperties(new RegistryEndpointProperties().withHost("contoso.azurecr.io")
                .withAuthentication(new RegistryEndpointAnonymousAuthentication()
                    .withAnonymousSettings(new RegistryEndpointAnonymousSettings()))
                .withCodeSigningCas(
                    Arrays.asList(new RegistryEndpointTrustedSigningKeySecret().withSecretRef("fakeTokenPlaceholder"),
                        new RegistryEndpointTrustedSigningKeyConfigMap().withConfigMapRef("my-configmap"))))
            .withExtendedLocation(new ExtendedLocation().withName(
                "/subscriptions/F8C729F9-DF9C-4743-848F-96EE433D8E53/resourceGroups/rgiotoperations/providers/Microsoft.ExtendedLocation/customLocations/resource-123")
                .withType(ExtendedLocationType.CUSTOM_LOCATION))
            .create();
    }
}
```

### RegistryEndpoint_Delete

```java
/**
 * Samples for RegistryEndpoint Delete.
 */
public final class RegistryEndpointDeleteSamples {
    /*
     * x-ms-original-file: 2025-10-01/RegistryEndpoint_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: RegistryEndpoint_Delete_MaximumSet.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        registryEndpointDeleteMaximumSet(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.registryEndpoints()
            .delete("rgiotoperations", "resource-123", "resource-123", com.azure.core.util.Context.NONE);
    }
}
```

### RegistryEndpoint_Get

```java
/**
 * Samples for RegistryEndpoint Get.
 */
public final class RegistryEndpointGetSamples {
    /*
     * x-ms-original-file: 2025-10-01/RegistryEndpoint_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: RegistryEndpoint_Get_MaximumSet.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void
        registryEndpointGetMaximumSet(com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.registryEndpoints()
            .getWithResponse("rgiotoperations", "resource-123", "resource-123", com.azure.core.util.Context.NONE);
    }
}
```

### RegistryEndpoint_ListByInstanceResource

```java
/**
 * Samples for RegistryEndpoint ListByInstanceResource.
 */
public final class RegistryEndpointListByInstanceResourceSamples {
    /*
     * x-ms-original-file: 2025-10-01/RegistryEndpoint_ListByInstanceResource_MaximumSet_Gen.json
     */
    /**
     * Sample code: RegistryEndpoint_ListByInstanceResource_MaximumSet.
     * 
     * @param manager Entry point to IoTOperationsManager.
     */
    public static void registryEndpointListByInstanceResourceMaximumSet(
        com.azure.resourcemanager.iotoperations.IoTOperationsManager manager) {
        manager.registryEndpoints()
            .listByInstanceResource("rgiotoperations", "resource-123", com.azure.core.util.Context.NONE);
    }
}
```

