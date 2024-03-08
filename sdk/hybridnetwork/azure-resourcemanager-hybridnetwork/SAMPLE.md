# Code snippets and samples


## ArtifactManifests

- [CreateOrUpdate](#artifactmanifests_createorupdate)
- [Delete](#artifactmanifests_delete)
- [Get](#artifactmanifests_get)
- [ListByArtifactStore](#artifactmanifests_listbyartifactstore)
- [ListCredential](#artifactmanifests_listcredential)
- [Update](#artifactmanifests_update)
- [UpdateState](#artifactmanifests_updatestate)

## ArtifactStores

- [CreateOrUpdate](#artifactstores_createorupdate)
- [Delete](#artifactstores_delete)
- [Get](#artifactstores_get)
- [ListByPublisher](#artifactstores_listbypublisher)
- [Update](#artifactstores_update)

## Components

- [Get](#components_get)
- [ListByNetworkFunction](#components_listbynetworkfunction)

## ConfigurationGroupSchemas

- [CreateOrUpdate](#configurationgroupschemas_createorupdate)
- [Delete](#configurationgroupschemas_delete)
- [Get](#configurationgroupschemas_get)
- [ListByPublisher](#configurationgroupschemas_listbypublisher)
- [Update](#configurationgroupschemas_update)
- [UpdateState](#configurationgroupschemas_updatestate)

## ConfigurationGroupValues

- [CreateOrUpdate](#configurationgroupvalues_createorupdate)
- [Delete](#configurationgroupvalues_delete)
- [GetByResourceGroup](#configurationgroupvalues_getbyresourcegroup)
- [List](#configurationgroupvalues_list)
- [ListByResourceGroup](#configurationgroupvalues_listbyresourcegroup)
- [UpdateTags](#configurationgroupvalues_updatetags)

## NetworkFunctionDefinitionGroups

- [CreateOrUpdate](#networkfunctiondefinitiongroups_createorupdate)
- [Delete](#networkfunctiondefinitiongroups_delete)
- [Get](#networkfunctiondefinitiongroups_get)
- [ListByPublisher](#networkfunctiondefinitiongroups_listbypublisher)
- [Update](#networkfunctiondefinitiongroups_update)

## NetworkFunctionDefinitionVersions

- [CreateOrUpdate](#networkfunctiondefinitionversions_createorupdate)
- [Delete](#networkfunctiondefinitionversions_delete)
- [Get](#networkfunctiondefinitionversions_get)
- [ListByNetworkFunctionDefinitionGroup](#networkfunctiondefinitionversions_listbynetworkfunctiondefinitiongroup)
- [Update](#networkfunctiondefinitionversions_update)
- [UpdateState](#networkfunctiondefinitionversions_updatestate)

## NetworkFunctions

- [CreateOrUpdate](#networkfunctions_createorupdate)
- [Delete](#networkfunctions_delete)
- [ExecuteRequest](#networkfunctions_executerequest)
- [GetByResourceGroup](#networkfunctions_getbyresourcegroup)
- [List](#networkfunctions_list)
- [ListByResourceGroup](#networkfunctions_listbyresourcegroup)
- [UpdateTags](#networkfunctions_updatetags)

## NetworkServiceDesignGroups

- [CreateOrUpdate](#networkservicedesigngroups_createorupdate)
- [Delete](#networkservicedesigngroups_delete)
- [Get](#networkservicedesigngroups_get)
- [ListByPublisher](#networkservicedesigngroups_listbypublisher)
- [Update](#networkservicedesigngroups_update)

## NetworkServiceDesignVersions

- [CreateOrUpdate](#networkservicedesignversions_createorupdate)
- [Delete](#networkservicedesignversions_delete)
- [Get](#networkservicedesignversions_get)
- [ListByNetworkServiceDesignGroup](#networkservicedesignversions_listbynetworkservicedesigngroup)
- [Update](#networkservicedesignversions_update)
- [UpdateState](#networkservicedesignversions_updatestate)

## Operations

- [List](#operations_list)

## ProxyArtifact

- [Get](#proxyartifact_get)
- [List](#proxyartifact_list)
- [UpdateState](#proxyartifact_updatestate)

## Publishers

- [CreateOrUpdate](#publishers_createorupdate)
- [Delete](#publishers_delete)
- [GetByResourceGroup](#publishers_getbyresourcegroup)
- [List](#publishers_list)
- [ListByResourceGroup](#publishers_listbyresourcegroup)
- [Update](#publishers_update)

## SiteNetworkServices

- [CreateOrUpdate](#sitenetworkservices_createorupdate)
- [Delete](#sitenetworkservices_delete)
- [GetByResourceGroup](#sitenetworkservices_getbyresourcegroup)
- [List](#sitenetworkservices_list)
- [ListByResourceGroup](#sitenetworkservices_listbyresourcegroup)
- [UpdateTags](#sitenetworkservices_updatetags)

## Sites

- [CreateOrUpdate](#sites_createorupdate)
- [Delete](#sites_delete)
- [GetByResourceGroup](#sites_getbyresourcegroup)
- [List](#sites_list)
- [ListByResourceGroup](#sites_listbyresourcegroup)
- [UpdateTags](#sites_updatetags)
### ArtifactManifests_CreateOrUpdate

```java
import com.azure.resourcemanager.hybridnetwork.models.ArtifactManifestPropertiesFormat;
import com.azure.resourcemanager.hybridnetwork.models.ArtifactType;
import com.azure.resourcemanager.hybridnetwork.models.ManifestArtifactFormat;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Samples for ArtifactManifests CreateOrUpdate.
 */
public final class ArtifactManifestsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/ArtifactManifestCreate.json
     */
    /**
     * Sample code: Create or update the artifact manifest resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createOrUpdateTheArtifactManifestResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.artifactManifests().define("TestManifest").withRegion("eastus").withExistingArtifactStore("rg", "TestPublisher", "TestArtifactStore").withProperties(new ArtifactManifestPropertiesFormat().withArtifacts(Arrays.asList(new ManifestArtifactFormat().withArtifactName("fed-rbac").withArtifactType(ArtifactType.OCIARTIFACT).withArtifactVersion("1.0.0"), new ManifestArtifactFormat().withArtifactName("nginx").withArtifactType(ArtifactType.OCIARTIFACT).withArtifactVersion("v1")))).create();
    }
}
```

### ArtifactManifests_Delete

```java
/**
 * Samples for ArtifactManifests Delete.
 */
public final class ArtifactManifestsDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/ArtifactManifestDelete.json
     */
    /**
     * Sample code: Delete a artifact manifest resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void deleteAArtifactManifestResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.artifactManifests().delete("rg", "TestPublisher", "TestArtifactStore", "TestManifest", com.azure.core.util.Context.NONE);
    }
}
```

### ArtifactManifests_Get

```java
/**
 * Samples for ArtifactManifests Get.
 */
public final class ArtifactManifestsGetSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/ArtifactManifestGet.json
     */
    /**
     * Sample code: Get a artifact manifest resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getAArtifactManifestResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.artifactManifests().getWithResponse("rg", "TestPublisher", "TestArtifactStore", "TestManifest", com.azure.core.util.Context.NONE);
    }
}
```

### ArtifactManifests_ListByArtifactStore

```java
/**
 * Samples for ArtifactManifests ListByArtifactStore.
 */
public final class ArtifactManifestsListByArtifactStoreSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/ArtifactManifestListByArtifactStore.json
     */
    /**
     * Sample code: Get artifact manifest list resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getArtifactManifestListResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.artifactManifests().listByArtifactStore("rg", "TestPublisher", "TestArtifactStore", com.azure.core.util.Context.NONE);
    }
}
```

### ArtifactManifests_ListCredential

```java
/**
 * Samples for ArtifactManifests ListCredential.
 */
public final class ArtifactManifestsListCredentialSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/ArtifactManifestListCredential.json
     */
    /**
     * Sample code: List a credential for artifact manifest.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void listACredentialForArtifactManifest(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.artifactManifests().listCredentialWithResponse("rg", "TestPublisher", "TestArtifactStore", "TestArtifactManifestName", com.azure.core.util.Context.NONE);
    }
}
```

### ArtifactManifests_Update

```java
import com.azure.resourcemanager.hybridnetwork.models.ArtifactManifest;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ArtifactManifests Update.
 */
public final class ArtifactManifestsUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/ArtifactManifestUpdateTags.json
     */
    /**
     * Sample code: Update a artifact manifest resource tags.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void updateAArtifactManifestResourceTags(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        ArtifactManifest resource = manager.artifactManifests().getWithResponse("rg", "TestPublisher", "TestArtifactStore", "TestManifest", com.azure.core.util.Context.NONE).getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### ArtifactManifests_UpdateState

```java
import com.azure.resourcemanager.hybridnetwork.fluent.models.ArtifactManifestUpdateStateInner;
import com.azure.resourcemanager.hybridnetwork.models.ArtifactManifestState;
import java.util.stream.Collectors;

/**
 * Samples for ArtifactManifests UpdateState.
 */
public final class ArtifactManifestsUpdateStateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/ArtifactManifestUpdateState.json
     */
    /**
     * Sample code: Update artifact manifest state.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void updateArtifactManifestState(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.artifactManifests().updateState("rg", "TestPublisher", "TestArtifactStore", "TestArtifactManifestName", new ArtifactManifestUpdateStateInner().withArtifactManifestState(ArtifactManifestState.UPLOADED), com.azure.core.util.Context.NONE);
    }
}
```

### ArtifactStores_CreateOrUpdate

```java
import com.azure.resourcemanager.hybridnetwork.models.ArtifactReplicationStrategy;
import com.azure.resourcemanager.hybridnetwork.models.ArtifactStorePropertiesFormat;
import com.azure.resourcemanager.hybridnetwork.models.ArtifactStorePropertiesFormatManagedResourceGroupConfiguration;
import com.azure.resourcemanager.hybridnetwork.models.ArtifactStoreType;
import java.util.stream.Collectors;

/**
 * Samples for ArtifactStores CreateOrUpdate.
 */
public final class ArtifactStoresCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/ArtifactStoreCreate.json
     */
    /**
     * Sample code: Create or update an artifact store of publisher resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createOrUpdateAnArtifactStoreOfPublisherResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.artifactStores().define("TestArtifactStore").withRegion("eastus").withExistingPublisher("rg", "TestPublisher").withProperties(new ArtifactStorePropertiesFormat().withStoreType(ArtifactStoreType.AZURE_CONTAINER_REGISTRY).withReplicationStrategy(ArtifactReplicationStrategy.SINGLE_REPLICATION).withManagedResourceGroupConfiguration(new ArtifactStorePropertiesFormatManagedResourceGroupConfiguration().withName("testRg").withLocation("eastus"))).create();
    }
}
```

### ArtifactStores_Delete

```java
/**
 * Samples for ArtifactStores Delete.
 */
public final class ArtifactStoresDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/ArtifactStoreDelete.json
     */
    /**
     * Sample code: Delete a artifact store of publisher resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void deleteAArtifactStoreOfPublisherResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.artifactStores().delete("rg", "TestPublisher", "TestArtifactStore", com.azure.core.util.Context.NONE);
    }
}
```

### ArtifactStores_Get

```java
/**
 * Samples for ArtifactStores Get.
 */
public final class ArtifactStoresGetSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/ArtifactStoreGet.json
     */
    /**
     * Sample code: Get a artifact store resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getAArtifactStoreResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.artifactStores().getWithResponse("rg", "TestPublisher", "TestArtifactStoreName", com.azure.core.util.Context.NONE);
    }
}
```

### ArtifactStores_ListByPublisher

```java
/**
 * Samples for ArtifactStores ListByPublisher.
 */
public final class ArtifactStoresListByPublisherSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/ArtifactStoresListByPublisherName.json
     */
    /**
     * Sample code: Get application groups under a publisher resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getApplicationGroupsUnderAPublisherResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.artifactStores().listByPublisher("rg", "TestPublisher", com.azure.core.util.Context.NONE);
    }
}
```

### ArtifactStores_Update

```java
import com.azure.resourcemanager.hybridnetwork.models.ArtifactStore;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ArtifactStores Update.
 */
public final class ArtifactStoresUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/ArtifactStoreUpdateTags.json
     */
    /**
     * Sample code: Update artifact store resource tags.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void updateArtifactStoreResourceTags(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        ArtifactStore resource = manager.artifactStores().getWithResponse("rg", "TestPublisher", "TestArtifactStore", com.azure.core.util.Context.NONE).getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### Components_Get

```java
/**
 * Samples for Components Get.
 */
public final class ComponentsGetSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/ComponentGet.json
     */
    /**
     * Sample code: Get component resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getComponentResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.components().getWithResponse("rg", "testNf", "testComponent", com.azure.core.util.Context.NONE);
    }
}
```

### Components_ListByNetworkFunction

```java
/**
 * Samples for Components ListByNetworkFunction.
 */
public final class ComponentsListByNetworkFunctionSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/ComponentListByNetworkFunction.json
     */
    /**
     * Sample code: List components in network function.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void listComponentsInNetworkFunction(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.components().listByNetworkFunction("rg", "testNf", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationGroupSchemas_CreateOrUpdate

```java
import com.azure.resourcemanager.hybridnetwork.models.ConfigurationGroupSchemaPropertiesFormat;

/**
 * Samples for ConfigurationGroupSchemas CreateOrUpdate.
 */
public final class ConfigurationGroupSchemasCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/ConfigurationGroupSchemaCreate.json
     */
    /**
     * Sample code: Create or update the network function definition group.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createOrUpdateTheNetworkFunctionDefinitionGroup(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.configurationGroupSchemas().define("testConfigurationGroupSchema").withRegion("westUs2").withExistingPublisher("rg1", "testPublisher").withProperties(new ConfigurationGroupSchemaPropertiesFormat().withDescription("Schema with no secrets").withSchemaDefinition("{\"type\":\"object\",\"properties\":{\"interconnect-groups\":{\"type\":\"object\",\"properties\":{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"},\"international-interconnects\":{\"type\":\"array\",\"item\":{\"type\":\"string\"}},\"domestic-interconnects\":{\"type\":\"array\",\"item\":{\"type\":\"string\"}}}}},\"interconnect-group-assignments\":{\"type\":\"object\",\"properties\":{\"type\":\"object\",\"properties\":{\"ssc\":{\"type\":\"string\"},\"interconnects-interconnects\":{\"type\":\"string\"}}}}},\"required\":[\"interconnect-groups\",\"interconnect-group-assignments\"]}")).create();
    }
}
```

### ConfigurationGroupSchemas_Delete

```java
/**
 * Samples for ConfigurationGroupSchemas Delete.
 */
public final class ConfigurationGroupSchemasDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/ConfigurationGroupSchemaDelete.json
     */
    /**
     * Sample code: Delete a network function group resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void deleteANetworkFunctionGroupResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.configurationGroupSchemas().delete("rg1", "testPublisher", "testConfigurationGroupSchema", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationGroupSchemas_Get

```java
/**
 * Samples for ConfigurationGroupSchemas Get.
 */
public final class ConfigurationGroupSchemasGetSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/ConfigurationGroupSchemaGet.json
     */
    /**
     * Sample code: Get a networkFunctionDefinition group resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getANetworkFunctionDefinitionGroupResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.configurationGroupSchemas().getWithResponse("rg1", "testPublisher", "testConfigurationGroupSchema", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationGroupSchemas_ListByPublisher

```java
/**
 * Samples for ConfigurationGroupSchemas ListByPublisher.
 */
public final class ConfigurationGroupSchemasListByPublisherSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/ConfigurationGroupSchemaListByPublisherName.json
     */
    /**
     * Sample code: Get networkFunctionDefinition groups under publisher resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getNetworkFunctionDefinitionGroupsUnderPublisherResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.configurationGroupSchemas().listByPublisher("rg1", "testPublisher", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationGroupSchemas_Update

```java
import com.azure.resourcemanager.hybridnetwork.models.ConfigurationGroupSchema;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ConfigurationGroupSchemas Update.
 */
public final class ConfigurationGroupSchemasUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/ConfigurationGroupSchemaUpdateTags.json
     */
    /**
     * Sample code: Create or update the configuration group schema resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createOrUpdateTheConfigurationGroupSchemaResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        ConfigurationGroupSchema resource = manager.configurationGroupSchemas().getWithResponse("rg1", "testPublisher", "testConfigurationGroupSchema", com.azure.core.util.Context.NONE).getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### ConfigurationGroupSchemas_UpdateState

```java
import com.azure.resourcemanager.hybridnetwork.fluent.models.ConfigurationGroupSchemaVersionUpdateStateInner;
import com.azure.resourcemanager.hybridnetwork.models.VersionState;
import java.util.stream.Collectors;

/**
 * Samples for ConfigurationGroupSchemas UpdateState.
 */
public final class ConfigurationGroupSchemasUpdateStateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/ConfigurationGroupSchemaVersionUpdateState.json
     */
    /**
     * Sample code: Update network service design version state.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void updateNetworkServiceDesignVersionState(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.configurationGroupSchemas().updateState("rg1", "testPublisher", "testConfigurationGroupSchema", new ConfigurationGroupSchemaVersionUpdateStateInner().withVersionState(VersionState.ACTIVE), com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationGroupValues_CreateOrUpdate

```java
import com.azure.resourcemanager.hybridnetwork.models.ConfigurationValueWithoutSecrets;
import com.azure.resourcemanager.hybridnetwork.models.ConfigurationValueWithSecrets;
import com.azure.resourcemanager.hybridnetwork.models.OpenDeploymentResourceReference;
import com.azure.resourcemanager.hybridnetwork.models.SecretDeploymentResourceReference;

/**
 * Samples for ConfigurationGroupValues CreateOrUpdate.
 */
public final class ConfigurationGroupValuesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/ConfigurationGroupValueCreate.json
     */
    /**
     * Sample code: Create or update configuration group value.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createOrUpdateConfigurationGroupValue(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.configurationGroupValues().define("testConfigurationGroupValue").withRegion("eastus").withExistingResourceGroup("rg1").withProperties(new ConfigurationValueWithoutSecrets().withConfigurationGroupSchemaResourceReference(new OpenDeploymentResourceReference().withId("/subscriptions/subid/resourcegroups/testRG/providers/microsoft.hybridnetwork/publishers/testPublisher/configurationGroupSchemas/testConfigurationGroupSchemaName")).withConfigurationValue("{\"interconnect-groups\":{\"stripe-one\":{\"name\":\"Stripe one\",\"international-interconnects\":[\"france\",\"germany\"],\"domestic-interconnects\":[\"birmingham\",\"edinburgh\"]},\"stripe-two\":{\"name\":\"Stripe two\",\"international-interconnects\":[\"germany\",\"italy\"],\"domestic-interconnects\":[\"edinburgh\",\"london\"]}},\"interconnect-group-assignments\":{\"ssc-one\":{\"ssc\":\"SSC 1\",\"interconnects\":\"stripe-one\"},\"ssc-two\":{\"ssc\":\"SSC 2\",\"interconnects\":\"stripe-two\"}}}")).create();
    }

    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/ConfigurationGroupValueFirstPartyCreate.json
     */
    /**
     * Sample code: Create or update first party configuration group value.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createOrUpdateFirstPartyConfigurationGroupValue(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.configurationGroupValues().define("testConfigurationGroupValue").withRegion("eastus").withExistingResourceGroup("rg1").withProperties(new ConfigurationValueWithoutSecrets().withConfigurationGroupSchemaResourceReference(new SecretDeploymentResourceReference().withId("/subscriptions/subid/resourcegroups/testRG/providers/microsoft.hybridnetwork/publishers/testPublisher/configurationGroupSchemas/testConfigurationGroupSchemaName")).withConfigurationValue("{\"interconnect-groups\":{\"stripe-one\":{\"name\":\"Stripe one\",\"international-interconnects\":[\"france\",\"germany\"],\"domestic-interconnects\":[\"birmingham\",\"edinburgh\"]},\"stripe-two\":{\"name\":\"Stripe two\",\"international-interconnects\":[\"germany\",\"italy\"],\"domestic-interconnects\":[\"edinburgh\",\"london\"]}},\"interconnect-group-assignments\":{\"ssc-one\":{\"ssc\":\"SSC 1\",\"interconnects\":\"stripe-one\"},\"ssc-two\":{\"ssc\":\"SSC 2\",\"interconnects\":\"stripe-two\"}}}")).create();
    }

    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/ConfigurationGroupValueCreateSecret.json
     */
    /**
     * Sample code: Create or update configuration group value with secrets.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createOrUpdateConfigurationGroupValueWithSecrets(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.configurationGroupValues().define("testConfigurationGroupValue").withRegion("eastus").withExistingResourceGroup("rg1").withProperties(new ConfigurationValueWithSecrets().withConfigurationGroupSchemaResourceReference(new OpenDeploymentResourceReference().withId("/subscriptions/subid/resourcegroups/testRG/providers/microsoft.hybridnetwork/publishers/testPublisher/configurationGroupSchemas/testConfigurationGroupSchemaName")).withSecretConfigurationValue("fakeTokenPlaceholder")).create();
    }
}
```

### ConfigurationGroupValues_Delete

```java
/**
 * Samples for ConfigurationGroupValues Delete.
 */
public final class ConfigurationGroupValuesDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/ConfigurationGroupValueDelete.json
     */
    /**
     * Sample code: Delete hybrid configuration group resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void deleteHybridConfigurationGroupResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.configurationGroupValues().delete("rg1", "testConfigurationGroupValue", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationGroupValues_GetByResourceGroup

```java
/**
 * Samples for ConfigurationGroupValues GetByResourceGroup.
 */
public final class ConfigurationGroupValuesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/ConfigurationGroupValueGet.json
     */
    /**
     * Sample code: Get hybrid configuration group.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getHybridConfigurationGroup(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.configurationGroupValues().getByResourceGroupWithResponse("rg1", "testConfigurationGroupValue", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationGroupValues_List

```java
/**
 * Samples for ConfigurationGroupValues List.
 */
public final class ConfigurationGroupValuesListSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/ConfigurationGroupValueListBySubscription.json
     */
    /**
     * Sample code: List all hybrid network sites in a subscription.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void listAllHybridNetworkSitesInASubscription(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.configurationGroupValues().list(com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationGroupValues_ListByResourceGroup

```java
/**
 * Samples for ConfigurationGroupValues ListByResourceGroup.
 */
public final class ConfigurationGroupValuesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/ConfigurationGroupValueListByResourceGroup.json
     */
    /**
     * Sample code: List all hybrid network configurationGroupValues in a subscription.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void listAllHybridNetworkConfigurationGroupValuesInASubscription(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.configurationGroupValues().listByResourceGroup("rg1", com.azure.core.util.Context.NONE);
    }
}
```

### ConfigurationGroupValues_UpdateTags

```java
import com.azure.resourcemanager.hybridnetwork.models.ConfigurationGroupValue;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ConfigurationGroupValues UpdateTags.
 */
public final class ConfigurationGroupValuesUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/ConfigurationGroupValueUpdateTags.json
     */
    /**
     * Sample code: Update hybrid configuration group tags.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void updateHybridConfigurationGroupTags(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        ConfigurationGroupValue resource = manager.configurationGroupValues().getByResourceGroupWithResponse("rg1", "testConfigurationGroupValue", com.azure.core.util.Context.NONE).getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### NetworkFunctionDefinitionGroups_CreateOrUpdate

```java
/**
 * Samples for NetworkFunctionDefinitionGroups CreateOrUpdate.
 */
public final class NetworkFunctionDefinitionGroupsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkFunctionDefinitionGroupCreate.json
     */
    /**
     * Sample code: Create or update the network function definition group.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createOrUpdateTheNetworkFunctionDefinitionGroup(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctionDefinitionGroups().define("TestNetworkFunctionDefinitionGroupName").withRegion("eastus").withExistingPublisher("rg", "TestPublisher").create();
    }
}
```

### NetworkFunctionDefinitionGroups_Delete

```java
/**
 * Samples for NetworkFunctionDefinitionGroups Delete.
 */
public final class NetworkFunctionDefinitionGroupsDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkFunctionDefinitionGroupDelete.json
     */
    /**
     * Sample code: Delete a network function group resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void deleteANetworkFunctionGroupResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctionDefinitionGroups().delete("rg", "TestPublisher", "TestNetworkFunctionDefinitionGroupName", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkFunctionDefinitionGroups_Get

```java
/**
 * Samples for NetworkFunctionDefinitionGroups Get.
 */
public final class NetworkFunctionDefinitionGroupsGetSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkFunctionDefinitionGroupGet.json
     */
    /**
     * Sample code: Get a networkFunctionDefinition group resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getANetworkFunctionDefinitionGroupResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctionDefinitionGroups().getWithResponse("rg", "TestPublisher", "TestNetworkFunctionDefinitionGroupName", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkFunctionDefinitionGroups_ListByPublisher

```java
/**
 * Samples for NetworkFunctionDefinitionGroups ListByPublisher.
 */
public final class NetworkFunctionDefinitionGroupsListByPublisherSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkFunctionDefinitionGroupsListByPublisherName.json
     */
    /**
     * Sample code: Get networkFunctionDefinition groups under publisher resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getNetworkFunctionDefinitionGroupsUnderPublisherResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctionDefinitionGroups().listByPublisher("rg", "TestPublisher", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkFunctionDefinitionGroups_Update

```java
import com.azure.resourcemanager.hybridnetwork.models.NetworkFunctionDefinitionGroup;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for NetworkFunctionDefinitionGroups Update.
 */
public final class NetworkFunctionDefinitionGroupsUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkFunctionDefinitionGroupUpdateTags.json
     */
    /**
     * Sample code: Create or update the network function definition group resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createOrUpdateTheNetworkFunctionDefinitionGroupResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        NetworkFunctionDefinitionGroup resource = manager.networkFunctionDefinitionGroups().getWithResponse("rg", "TestPublisher", "TestNetworkFunctionDefinitionGroupName", com.azure.core.util.Context.NONE).getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### NetworkFunctionDefinitionVersions_CreateOrUpdate

```java
import com.azure.resourcemanager.hybridnetwork.models.ApplicationEnablement;
import com.azure.resourcemanager.hybridnetwork.models.ArmTemplateArtifactProfile;
import com.azure.resourcemanager.hybridnetwork.models.ArmTemplateMappingRuleProfile;
import com.azure.resourcemanager.hybridnetwork.models.AzureArcKubernetesArtifactProfile;
import com.azure.resourcemanager.hybridnetwork.models.AzureArcKubernetesDeployMappingRuleProfile;
import com.azure.resourcemanager.hybridnetwork.models.AzureArcKubernetesHelmApplication;
import com.azure.resourcemanager.hybridnetwork.models.AzureArcKubernetesNetworkFunctionTemplate;
import com.azure.resourcemanager.hybridnetwork.models.AzureCoreArmTemplateArtifactProfile;
import com.azure.resourcemanager.hybridnetwork.models.AzureCoreArmTemplateDeployMappingRuleProfile;
import com.azure.resourcemanager.hybridnetwork.models.AzureCoreNetworkFunctionArmTemplateApplication;
import com.azure.resourcemanager.hybridnetwork.models.AzureCoreNetworkFunctionTemplate;
import com.azure.resourcemanager.hybridnetwork.models.AzureCoreNetworkFunctionVhdApplication;
import com.azure.resourcemanager.hybridnetwork.models.AzureCoreVhdImageArtifactProfile;
import com.azure.resourcemanager.hybridnetwork.models.AzureCoreVhdImageDeployMappingRuleProfile;
import com.azure.resourcemanager.hybridnetwork.models.AzureOperatorNexusArmTemplateArtifactProfile;
import com.azure.resourcemanager.hybridnetwork.models.AzureOperatorNexusArmTemplateDeployMappingRuleProfile;
import com.azure.resourcemanager.hybridnetwork.models.AzureOperatorNexusImageArtifactProfile;
import com.azure.resourcemanager.hybridnetwork.models.AzureOperatorNexusImageDeployMappingRuleProfile;
import com.azure.resourcemanager.hybridnetwork.models.AzureOperatorNexusNetworkFunctionArmTemplateApplication;
import com.azure.resourcemanager.hybridnetwork.models.AzureOperatorNexusNetworkFunctionImageApplication;
import com.azure.resourcemanager.hybridnetwork.models.AzureOperatorNexusNetworkFunctionTemplate;
import com.azure.resourcemanager.hybridnetwork.models.ContainerizedNetworkFunctionDefinitionVersion;
import com.azure.resourcemanager.hybridnetwork.models.DependsOnProfile;
import com.azure.resourcemanager.hybridnetwork.models.HelmArtifactProfile;
import com.azure.resourcemanager.hybridnetwork.models.HelmInstallOptions;
import com.azure.resourcemanager.hybridnetwork.models.HelmMappingRuleProfile;
import com.azure.resourcemanager.hybridnetwork.models.HelmMappingRuleProfileOptions;
import com.azure.resourcemanager.hybridnetwork.models.HelmUpgradeOptions;
import com.azure.resourcemanager.hybridnetwork.models.ImageArtifactProfile;
import com.azure.resourcemanager.hybridnetwork.models.ImageMappingRuleProfile;
import com.azure.resourcemanager.hybridnetwork.models.ReferencedResource;
import com.azure.resourcemanager.hybridnetwork.models.VhdImageArtifactProfile;
import com.azure.resourcemanager.hybridnetwork.models.VhdImageMappingRuleProfile;
import com.azure.resourcemanager.hybridnetwork.models.VirtualNetworkFunctionDefinitionVersion;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Samples for NetworkFunctionDefinitionVersions CreateOrUpdate.
 */
public final class NetworkFunctionDefinitionVersionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/AzureCore/VirtualNetworkFunctionDefinitionVersionCreate.json
     */
    /**
     * Sample code: Create or update a network function definition version resource for AzureCore VNF.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createOrUpdateANetworkFunctionDefinitionVersionResourceForAzureCoreVNF(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctionDefinitionVersions().define("1.0.0").withRegion("eastus").withExistingNetworkFunctionDefinitionGroup("rg", "TestPublisher", "TestNetworkFunctionDefinitionGroupName").withProperties(new VirtualNetworkFunctionDefinitionVersion().withDescription("test NFDV for AzureCore").withDeployParameters("{\"virtualMachineName\":{\"type\":\"string\"},\"cpuCores\":{\"type\":\"int\"},\"memorySizeGB\":{\"type\":\"int\"},\"cloudServicesNetworkAttachment\":{\"type\":\"object\",\"properties\":{\"networkAttachmentName\":{\"type\":\"string\"},\"attachedNetworkId\":{\"type\":\"string\"},\"ipAllocationMethod\":{\"type\":\"string\"},\"ipv4Address\":{\"type\":\"string\"},\"ipv6Address\":{\"type\":\"string\"},\"defaultGateway\":{\"type\":\"string\"}},\"required\":[\"attachedNetworkId\",\"ipAllocationMethod\"]},\"networkAttachments\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"networkAttachmentName\":{\"type\":\"string\"},\"attachedNetworkId\":{\"type\":\"string\"},\"ipAllocationMethod\":{\"type\":\"string\"},\"ipv4Address\":{\"type\":\"string\"},\"ipv6Address\":{\"type\":\"string\"},\"defaultGateway\":{\"type\":\"string\"}},\"required\":[\"attachedNetworkId\",\"ipAllocationMethod\"]}},\"storageProfile\":{\"type\":\"object\",\"properties\":{\"osDisk\":{\"type\":\"object\",\"properties\":{\"createOption\":{\"type\":\"string\"},\"deleteOption\":{\"type\":\"string\"},\"diskSizeGB\":{\"type\":\"integer\"}},\"required\":[\"diskSizeGB\"]}},\"required\":[\"osDisk\"]},\"sshPublicKeys\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"keyData\":{\"type\":\"string\"}},\"required\":[\"keyData\"]}},\"userData\":{\"type\":\"string\"},\"adminUsername\":{\"type\":\"string\"},\"bootMethod\":{\"type\":\"string\",\"default\":\"UEFI\",\"enum\":[\"UEFI\",\"BIOS\"]},\"isolateEmulatorThread\":{\"type\":\"string\"},\"virtioInterface\":{\"type\":\"string\"},\"placementHints\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"hintType\":{\"type\":\"string\",\"enum\":[\"Affinity\",\"AntiAffinity\"]},\"resourceId\":{\"type\":\"string\"},\"schedulingExecution\":{\"type\":\"string\",\"enum\":[\"Soft\",\"Hard\"]},\"scope\":{\"type\":\"string\"}},\"required\":[\"hintType\",\"schedulingExecution\",\"resourceId\",\"scope\"]}}}").withNetworkFunctionTemplate(new AzureCoreNetworkFunctionTemplate().withNetworkFunctionApplications(Arrays.asList(new AzureCoreNetworkFunctionVhdApplication().withName("testImageRole").withDependsOnProfile(new DependsOnProfile().withInstallDependsOn(Arrays.asList()).withUninstallDependsOn(Arrays.asList()).withUpdateDependsOn(Arrays.asList())).withArtifactProfile(new AzureCoreVhdImageArtifactProfile().withArtifactStore(new ReferencedResource().withId("/subscriptions/subid/resourceGroups/rg/providers/microsoft.hybridnetwork/publishers/TestPublisher/artifactStores/TestArtifactStore")).withVhdArtifactProfile(new VhdImageArtifactProfile().withVhdName("test-image").withVhdVersion("1-0-0"))).withDeployParametersMappingRuleProfile(new AzureCoreVhdImageDeployMappingRuleProfile().withApplicationEnablement(ApplicationEnablement.UNKNOWN).withVhdImageMappingRuleProfile(new VhdImageMappingRuleProfile().withUserConfiguration(""))), new AzureCoreNetworkFunctionArmTemplateApplication().withName("testTemplateRole").withDependsOnProfile(new DependsOnProfile().withInstallDependsOn(Arrays.asList("testImageRole")).withUninstallDependsOn(Arrays.asList("testImageRole")).withUpdateDependsOn(Arrays.asList("testImageRole"))).withArtifactProfile(new AzureCoreArmTemplateArtifactProfile().withArtifactStore(new ReferencedResource().withId("/subscriptions/subid/resourceGroups/rg/providers/microsoft.hybridnetwork/publishers/TestPublisher/artifactStores/TestArtifactStore")).withTemplateArtifactProfile(new ArmTemplateArtifactProfile().withTemplateName("test-template").withTemplateVersion("1.0.0"))).withDeployParametersMappingRuleProfile(new AzureCoreArmTemplateDeployMappingRuleProfile().withApplicationEnablement(ApplicationEnablement.UNKNOWN).withTemplateMappingRuleProfile(new ArmTemplateMappingRuleProfile().withTemplateParameters("{\"virtualMachineName\":\"{deployParameters.virtualMachineName}\",\"cpuCores\":\"{deployParameters.cpuCores}\",\"memorySizeGB\":\"{deployParameters.memorySizeGB}\",\"cloudServicesNetworkAttachment\":\"{deployParameters.cloudServicesNetworkAttachment}\",\"networkAttachments\":\"{deployParameters.networkAttachments}\",\"sshPublicKeys\":\"{deployParameters.sshPublicKeys}\",\"storageProfile\":\"{deployParameters.storageProfile}\",\"isolateEmulatorThread\":\"{deployParameters.isolateEmulatorThread}\",\"virtioInterface\":\"{deployParameters.virtioInterface}\",\"userData\":\"{deployParameters.userData}\",\"adminUsername\":\"{deployParameters.adminUsername}\",\"bootMethod\":\"{deployParameters.bootMethod}\",\"placementHints\":\"{deployParameters.placementHints}\"}"))))))).create();
    }

    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/AzureOperatorNexus/VirtualNetworkFunctionDefinitionVersionCreate.json
     */
    /**
     * Sample code: Create or update a network function definition version resource for AzureOperatorNexus VNF.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createOrUpdateANetworkFunctionDefinitionVersionResourceForAzureOperatorNexusVNF(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctionDefinitionVersions().define("1.0.0").withRegion("eastus").withExistingNetworkFunctionDefinitionGroup("rg", "TestPublisher", "TestNetworkFunctionDefinitionGroupName").withProperties(new VirtualNetworkFunctionDefinitionVersion().withDescription("test NFDV for AzureOperatorNexus").withDeployParameters("{\"virtualMachineName\":{\"type\":\"string\"},\"extendedLocationName\":{\"type\":\"string\"},\"cpuCores\":{\"type\":\"int\"},\"memorySizeGB\":{\"type\":\"int\"},\"cloudServicesNetworkAttachment\":{\"type\":\"object\",\"properties\":{\"networkAttachmentName\":{\"type\":\"string\"},\"attachedNetworkId\":{\"type\":\"string\"},\"ipAllocationMethod\":{\"type\":\"string\"},\"ipv4Address\":{\"type\":\"string\"},\"ipv6Address\":{\"type\":\"string\"},\"defaultGateway\":{\"type\":\"string\"}},\"required\":[\"attachedNetworkId\",\"ipAllocationMethod\"]},\"networkAttachments\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"networkAttachmentName\":{\"type\":\"string\"},\"attachedNetworkId\":{\"type\":\"string\"},\"ipAllocationMethod\":{\"type\":\"string\"},\"ipv4Address\":{\"type\":\"string\"},\"ipv6Address\":{\"type\":\"string\"},\"defaultGateway\":{\"type\":\"string\"}},\"required\":[\"attachedNetworkId\",\"ipAllocationMethod\"]}},\"storageProfile\":{\"type\":\"object\",\"properties\":{\"osDisk\":{\"type\":\"object\",\"properties\":{\"createOption\":{\"type\":\"string\"},\"deleteOption\":{\"type\":\"string\"},\"diskSizeGB\":{\"type\":\"integer\"}},\"required\":[\"diskSizeGB\"]}},\"required\":[\"osDisk\"]},\"sshPublicKeys\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"keyData\":{\"type\":\"string\"}},\"required\":[\"keyData\"]}},\"userData\":{\"type\":\"string\"},\"adminUsername\":{\"type\":\"string\"},\"bootMethod\":{\"type\":\"string\",\"default\":\"UEFI\",\"enum\":[\"UEFI\",\"BIOS\"]},\"isolateEmulatorThread\":{\"type\":\"string\"},\"virtioInterface\":{\"type\":\"string\"},\"placementHints\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"hintType\":{\"type\":\"string\",\"enum\":[\"Affinity\",\"AntiAffinity\"]},\"resourceId\":{\"type\":\"string\"},\"schedulingExecution\":{\"type\":\"string\",\"enum\":[\"Soft\",\"Hard\"]},\"scope\":{\"type\":\"string\"}},\"required\":[\"hintType\",\"schedulingExecution\",\"resourceId\",\"scope\"]}}}").withNetworkFunctionTemplate(new AzureOperatorNexusNetworkFunctionTemplate().withNetworkFunctionApplications(Arrays.asList(new AzureOperatorNexusNetworkFunctionImageApplication().withName("testImageRole").withDependsOnProfile(new DependsOnProfile().withInstallDependsOn(Arrays.asList()).withUninstallDependsOn(Arrays.asList()).withUpdateDependsOn(Arrays.asList())).withArtifactProfile(new AzureOperatorNexusImageArtifactProfile().withArtifactStore(new ReferencedResource().withId("/subscriptions/subid/resourceGroups/rg/providers/microsoft.hybridnetwork/publishers/TestPublisher/artifactStores/TestArtifactStore")).withImageArtifactProfile(new ImageArtifactProfile().withImageName("test-image").withImageVersion("1.0.0"))).withDeployParametersMappingRuleProfile(new AzureOperatorNexusImageDeployMappingRuleProfile().withApplicationEnablement(ApplicationEnablement.UNKNOWN).withImageMappingRuleProfile(new ImageMappingRuleProfile().withUserConfiguration(""))), new AzureOperatorNexusNetworkFunctionArmTemplateApplication().withName("testTemplateRole").withDependsOnProfile(new DependsOnProfile().withInstallDependsOn(Arrays.asList("testImageRole")).withUninstallDependsOn(Arrays.asList("testImageRole")).withUpdateDependsOn(Arrays.asList("testImageRole"))).withArtifactProfile(new AzureOperatorNexusArmTemplateArtifactProfile().withArtifactStore(new ReferencedResource().withId("/subscriptions/subid/resourceGroups/rg/providers/microsoft.hybridnetwork/publishers/TestPublisher/artifactStores/TestArtifactStore")).withTemplateArtifactProfile(new ArmTemplateArtifactProfile().withTemplateName("test-template").withTemplateVersion("1.0.0"))).withDeployParametersMappingRuleProfile(new AzureOperatorNexusArmTemplateDeployMappingRuleProfile().withApplicationEnablement(ApplicationEnablement.UNKNOWN).withTemplateMappingRuleProfile(new ArmTemplateMappingRuleProfile().withTemplateParameters("{\"virtualMachineName\":\"{deployParameters.virtualMachineName}\",\"extendedLocationName\":\"{deployParameters.extendedLocationName}\",\"cpuCores\":\"{deployParameters.cpuCores}\",\"memorySizeGB\":\"{deployParameters.memorySizeGB}\",\"cloudServicesNetworkAttachment\":\"{deployParameters.cloudServicesNetworkAttachment}\",\"networkAttachments\":\"{deployParameters.networkAttachments}\",\"sshPublicKeys\":\"{deployParameters.sshPublicKeys}\",\"storageProfile\":\"{deployParameters.storageProfile}\",\"isolateEmulatorThread\":\"{deployParameters.isolateEmulatorThread}\",\"virtioInterface\":\"{deployParameters.virtioInterface}\",\"userData\":\"{deployParameters.userData}\",\"adminUsername\":\"{deployParameters.adminUsername}\",\"bootMethod\":\"{deployParameters.bootMethod}\",\"placementHints\":\"{deployParameters.placementHints}\"}"))))))).create();
    }

    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkFunctionDefinitionVersionCreate.json
     */
    /**
     * Sample code: Create or update a network function definition version resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createOrUpdateANetworkFunctionDefinitionVersionResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctionDefinitionVersions().define("1.0.0").withRegion("eastus").withExistingNetworkFunctionDefinitionGroup("rg", "TestPublisher", "TestNetworkFunctionDefinitionGroupName").withProperties(new ContainerizedNetworkFunctionDefinitionVersion().withDeployParameters("{\"type\":\"object\",\"properties\":{\"releaseName\":{\"type\":\"string\"},\"namespace\":{\"type\":\"string\"}}}").withNetworkFunctionTemplate(new AzureArcKubernetesNetworkFunctionTemplate().withNetworkFunctionApplications(Arrays.asList(new AzureArcKubernetesHelmApplication().withName("fedrbac").withDependsOnProfile(new DependsOnProfile().withInstallDependsOn(Arrays.asList()).withUninstallDependsOn(Arrays.asList()).withUpdateDependsOn(Arrays.asList())).withArtifactProfile(new AzureArcKubernetesArtifactProfile().withArtifactStore(new ReferencedResource().withId("/subscriptions/subid/resourcegroups/rg/providers/microsoft.hybridnetwork/publishers/TestPublisher/artifactStores/testArtifactStore")).withHelmArtifactProfile(new HelmArtifactProfile().withHelmPackageName("fed-rbac").withHelmPackageVersionRange("~2.1.3").withRegistryValuesPaths(Arrays.asList("global.registry.docker.repoPath")).withImagePullSecretsValuesPaths(Arrays.asList("global.imagePullSecrets")))).withDeployParametersMappingRuleProfile(new AzureArcKubernetesDeployMappingRuleProfile().withApplicationEnablement(ApplicationEnablement.ENABLED).withHelmMappingRuleProfile(new HelmMappingRuleProfile().withReleaseNamespace("{deployParameters.namesapce}").withReleaseName("{deployParameters.releaseName}").withHelmPackageVersion("2.1.3").withValues("").withOptions(new HelmMappingRuleProfileOptions().withInstallOptions(new HelmInstallOptions().withAtomic("true").withWaitOption("waitValue").withTimeout("30")).withUpgradeOptions(new HelmUpgradeOptions().withAtomic("true").withWaitOption("waitValue").withTimeout("30"))))))))).create();
    }
}
```

### NetworkFunctionDefinitionVersions_Delete

```java
/**
 * Samples for NetworkFunctionDefinitionVersions Delete.
 */
public final class NetworkFunctionDefinitionVersionsDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkFunctionDefinitionVersionDelete.json
     */
    /**
     * Sample code: Delete a network function definition version.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void deleteANetworkFunctionDefinitionVersion(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctionDefinitionVersions().delete("rg", "TestPublisher", "TestNetworkFunctionDefinitionGroupName", "1.0.0", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/AzureOperatorNexus/VirtualNetworkFunctionDefinitionVersionDelete.json
     */
    /**
     * Sample code: Delete a network function definition version for AzureOperatorNexus VNF.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void deleteANetworkFunctionDefinitionVersionForAzureOperatorNexusVNF(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctionDefinitionVersions().delete("rg", "TestPublisher", "TestNetworkFunctionDefinitionGroupName", "1.0.0", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/AzureCore/VirtualNetworkFunctionDefinitionVersionDelete.json
     */
    /**
     * Sample code: Delete a network function definition version for AzureCore VNF.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void deleteANetworkFunctionDefinitionVersionForAzureCoreVNF(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctionDefinitionVersions().delete("rg", "TestPublisher", "TestNetworkFunctionDefinitionGroupName", "1.0.0", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkFunctionDefinitionVersions_Get

```java
/**
 * Samples for NetworkFunctionDefinitionVersions Get.
 */
public final class NetworkFunctionDefinitionVersionsGetSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkFunctionDefinitionVersionGet.json
     */
    /**
     * Sample code: Get a network function definition version resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getANetworkFunctionDefinitionVersionResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctionDefinitionVersions().getWithResponse("rg", "TestPublisher", "TestNetworkFunctionDefinitionGroupName", "1.0.0", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/AzureCore/VirtualNetworkFunctionDefinitionVersionGet.json
     */
    /**
     * Sample code: Get network function definition version resource for AzureCore VNF.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getNetworkFunctionDefinitionVersionResourceForAzureCoreVNF(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctionDefinitionVersions().getWithResponse("rg", "TestPublisher", "TestNetworkFunctionDefinitionGroupName", "1.0.0", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/AzureOperatorNexus/VirtualNetworkFunctionDefinitionVersionGet.json
     */
    /**
     * Sample code: Get network function definition version resource for AzureOperatorNexus VNF.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getNetworkFunctionDefinitionVersionResourceForAzureOperatorNexusVNF(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctionDefinitionVersions().getWithResponse("rg", "TestPublisher", "TestNetworkFunctionDefinitionGroupName", "1.0.0", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkFunctionDefinitionVersions_ListByNetworkFunctionDefinitionGroup

```java
/**
 * Samples for NetworkFunctionDefinitionVersions ListByNetworkFunctionDefinitionGroup.
 */
public final class NetworkFunctionDefinitionVersionsListByNetworkFunctionDefinitionGroupSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkFunctionDefinitionVersionListByNetworkFunctionDefinitionGroup.json
     */
    /**
     * Sample code: Get Publisher resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getPublisherResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctionDefinitionVersions().listByNetworkFunctionDefinitionGroup("rg", "TestPublisher", "TestNetworkFunctionDefinitionGroupNameName", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkFunctionDefinitionVersions_Update

```java
import com.azure.resourcemanager.hybridnetwork.models.NetworkFunctionDefinitionVersion;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for NetworkFunctionDefinitionVersions Update.
 */
public final class NetworkFunctionDefinitionVersionsUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkFunctionDefinitionVersionUpdateTags.json
     */
    /**
     * Sample code: Update the network function definition version tags.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void updateTheNetworkFunctionDefinitionVersionTags(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        NetworkFunctionDefinitionVersion resource = manager.networkFunctionDefinitionVersions().getWithResponse("rg", "TestPublisher", "TestNetworkFunctionDefinitionGroupName", "1.0.0", com.azure.core.util.Context.NONE).getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### NetworkFunctionDefinitionVersions_UpdateState

```java
import com.azure.resourcemanager.hybridnetwork.fluent.models.NetworkFunctionDefinitionVersionUpdateStateInner;
import com.azure.resourcemanager.hybridnetwork.models.VersionState;
import java.util.stream.Collectors;

/**
 * Samples for NetworkFunctionDefinitionVersions UpdateState.
 */
public final class NetworkFunctionDefinitionVersionsUpdateStateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkFunctionDefinitionVersionUpdateState.json
     */
    /**
     * Sample code: Update network function definition version state.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void updateNetworkFunctionDefinitionVersionState(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctionDefinitionVersions().updateState("rg", "TestPublisher", "TestSkuGroup", "1.0.0", new NetworkFunctionDefinitionVersionUpdateStateInner().withVersionState(VersionState.ACTIVE), com.azure.core.util.Context.NONE);
    }
}
```

### NetworkFunctions_CreateOrUpdate

```java
import com.azure.resourcemanager.hybridnetwork.models.NetworkFunctionValueWithoutSecrets;
import com.azure.resourcemanager.hybridnetwork.models.NetworkFunctionValueWithSecrets;
import com.azure.resourcemanager.hybridnetwork.models.NfviType;
import com.azure.resourcemanager.hybridnetwork.models.OpenDeploymentResourceReference;
import com.azure.resourcemanager.hybridnetwork.models.SecretDeploymentResourceReference;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Samples for NetworkFunctions CreateOrUpdate.
 */
public final class NetworkFunctionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/AzureCore/VirtualNetworkFunctionCreate.json
     */
    /**
     * Sample code: Create virtual network function resource on AzureCore.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createVirtualNetworkFunctionResourceOnAzureCore(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctions().define("testNf").withRegion("eastus").withExistingResourceGroup("rg").withProperties(new NetworkFunctionValueWithoutSecrets().withNetworkFunctionDefinitionVersionResourceReference(new OpenDeploymentResourceReference().withId("/subscriptions/subid/resourcegroups/rg/providers/Microsoft.HybridNetwork/publishers/testVendor/networkFunctionDefinitionGroups/testnetworkFunctionDefinitionGroupName/networkFunctionDefinitionVersions/1.0.1")).withNfviType(NfviType.AZURE_CORE).withNfviId("/subscriptions/subid/resourceGroups/testResourceGroup").withAllowSoftwareUpdate(false).withDeploymentValues("{\"virtualMachineName\":\"test-VM\",\"cpuCores\":4,\"memorySizeGB\":8,\"cloudServicesNetworkAttachment\":{\"attachedNetworkId\":\"test-csnet\",\"ipAllocationMethod\":\"Dynamic\",\"networkAttachmentName\":\"test-cs-vlan\"},\"networkAttachments\":[{\"attachedNetworkId\":\"test-l3vlan\",\"defaultGateway\":\"True\",\"ipAllocationMethod\":\"Dynamic\",\"networkAttachmentName\":\"test-vlan\"}],\"sshPublicKeys\":[{\"keyData\":\"ssh-rsa CMIIIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA0TqlveKKlc2MFvEmuXJiLGBsY1t4ML4uiRADGSZlnc+7Ugv3h+MCjkkwOKiOdsNo8k4KSBIG5GcQfKYOOd17AJvqCL6cGQbaLuqv0a64jeDm8oO8/xN/IM0oKw7rMr/2oAJOgIsfeXPkRxWWic9AVIS++H5Qi2r7bUFX+cqFsyUCAwEBBQ==\"}],\"storageProfile\":{\"osDisk\":{\"createOption\":\"Ephemeral\",\"deleteOption\":\"Delete\",\"diskSizeGB\":10}},\"userData\":\"testUserData\",\"adminUsername\":\"testUser\",\"virtioInterface\":\"Transitional\",\"isolateEmulatorThread\":\"False\",\"bootMethod\":\"BIOS\",\"placementHints\":[]}")).create();
    }

    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkFunctionFirstPartyCreate.json
     */
    /**
     * Sample code: Create first party network function resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createFirstPartyNetworkFunctionResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctions().define("testNf").withRegion("eastus").withExistingResourceGroup("rg").withProperties(new NetworkFunctionValueWithoutSecrets().withNetworkFunctionDefinitionVersionResourceReference(new SecretDeploymentResourceReference().withId("/subscriptions/subid/resourcegroups/rg/providers/Microsoft.HybridNetwork/publishers/testVendor/networkFunctionDefinitionGroups/testnetworkFunctionDefinitionGroupName/networkFunctionDefinitionVersions/1.0.1")).withNfviType(NfviType.AZURE_ARC_KUBERNETES).withNfviId("/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testResourceGroup/providers/Microsoft.ExtendedLocation/customLocations/testCustomLocation").withAllowSoftwareUpdate(false).withRoleOverrideValues(Arrays.asList("{\"name\":\"testRoleOne\",\"deployParametersMappingRuleProfile\":{\"helmMappingRuleProfile\":{\"helmPackageVersion\":\"2.1.3\",\"values\":\"{\\\"roleOneParam\\\":\\\"roleOneOverrideValue\\\"}\"}}}", "{\"name\":\"testRoleTwo\",\"deployParametersMappingRuleProfile\":{\"helmMappingRuleProfile\":{\"releaseName\":\"overrideReleaseName\",\"releaseNamespace\":\"overrideNamespace\",\"values\":\"{\\\"roleTwoParam\\\":\\\"roleTwoOverrideValue\\\"}\"}}}")).withDeploymentValues("{\"releaseName\":\"testReleaseName\",\"namespace\":\"testNamespace\"}")).create();
    }

    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkFunctionCreate.json
     */
    /**
     * Sample code: Create network function resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createNetworkFunctionResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctions().define("testNf").withRegion("eastus").withExistingResourceGroup("rg").withProperties(new NetworkFunctionValueWithoutSecrets().withNetworkFunctionDefinitionVersionResourceReference(new OpenDeploymentResourceReference().withId("/subscriptions/subid/resourcegroups/rg/providers/Microsoft.HybridNetwork/publishers/testVendor/networkFunctionDefinitionGroups/testnetworkFunctionDefinitionGroupName/networkFunctionDefinitionVersions/1.0.1")).withNfviType(NfviType.AZURE_ARC_KUBERNETES).withNfviId("/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testResourceGroup/providers/Microsoft.ExtendedLocation/customLocations/testCustomLocation").withAllowSoftwareUpdate(false).withRoleOverrideValues(Arrays.asList("{\"name\":\"testRoleOne\",\"deployParametersMappingRuleProfile\":{\"helmMappingRuleProfile\":{\"helmPackageVersion\":\"2.1.3\",\"values\":\"{\\\"roleOneParam\\\":\\\"roleOneOverrideValue\\\"}\"}}}", "{\"name\":\"testRoleTwo\",\"deployParametersMappingRuleProfile\":{\"helmMappingRuleProfile\":{\"releaseName\":\"overrideReleaseName\",\"releaseNamespace\":\"overrideNamespace\",\"values\":\"{\\\"roleTwoParam\\\":\\\"roleTwoOverrideValue\\\"}\"}}}")).withDeploymentValues("{\"releaseName\":\"testReleaseName\",\"namespace\":\"testNamespace\"}")).create();
    }

    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkFunctionCreateSecret.json
     */
    /**
     * Sample code: Create network function resource with secrets.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createNetworkFunctionResourceWithSecrets(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctions().define("testNf").withRegion("eastus").withExistingResourceGroup("rg").withProperties(new NetworkFunctionValueWithSecrets().withNetworkFunctionDefinitionVersionResourceReference(new OpenDeploymentResourceReference().withId("/subscriptions/subid/resourcegroups/rg/providers/Microsoft.HybridNetwork/publishers/testVendor/networkFunctionDefinitionGroups/testnetworkFunctionDefinitionGroupName/networkFunctionDefinitionVersions/1.0.1")).withNfviType(NfviType.AZURE_ARC_KUBERNETES).withNfviId("/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testResourceGroup/providers/Microsoft.ExtendedLocation/customLocations/testCustomLocation").withAllowSoftwareUpdate(false).withRoleOverrideValues(Arrays.asList("{\"name\":\"testRoleOne\",\"deployParametersMappingRuleProfile\":{\"helmMappingRuleProfile\":{\"helmPackageVersion\":\"2.1.3\",\"values\":\"{\\\"roleOneParam\\\":\\\"roleOneOverrideValue\\\"}\"}}}", "{\"name\":\"testRoleTwo\",\"deployParametersMappingRuleProfile\":{\"helmMappingRuleProfile\":{\"releaseName\":\"overrideReleaseName\",\"releaseNamespace\":\"overrideNamespace\",\"values\":\"{\\\"roleTwoParam\\\":\\\"roleTwoOverrideValue\\\"}\"}}}")).withSecretDeploymentValues("fakeTokenPlaceholder")).create();
    }

    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/AzureOperatorNexus/VirtualNetworkFunctionCreate.json
     */
    /**
     * Sample code: Create virtual network function resource on AzureOperatorNexus.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createVirtualNetworkFunctionResourceOnAzureOperatorNexus(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctions().define("testNf").withRegion("eastus").withExistingResourceGroup("rg").withProperties(new NetworkFunctionValueWithoutSecrets().withNetworkFunctionDefinitionVersionResourceReference(new OpenDeploymentResourceReference().withId("/subscriptions/subid/resourcegroups/rg/providers/Microsoft.HybridNetwork/publishers/testVendor/networkFunctionDefinitionGroups/testnetworkFunctionDefinitionGroupName/networkFunctionDefinitionVersions/1.0.1")).withNfviType(NfviType.AZURE_OPERATOR_NEXUS).withNfviId("/subscriptions/subid/resourceGroups/testResourceGroup/providers/Microsoft.ExtendedLocation/customLocations/testCustomLocation").withAllowSoftwareUpdate(false).withDeploymentValues("{\"virtualMachineName\":\"test-VM\",\"extendedLocationName\":\"test-cluster\",\"cpuCores\":4,\"memorySizeGB\":8,\"cloudServicesNetworkAttachment\":{\"attachedNetworkId\":\"test-csnet\",\"ipAllocationMethod\":\"Dynamic\",\"networkAttachmentName\":\"test-cs-vlan\"},\"networkAttachments\":[{\"attachedNetworkId\":\"test-l3vlan\",\"defaultGateway\":\"True\",\"ipAllocationMethod\":\"Dynamic\",\"networkAttachmentName\":\"test-vlan\"}],\"sshPublicKeys\":[{\"keyData\":\"ssh-rsa CMIIIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA0TqlveKKlc2MFvEmuXJiLGBsY1t4ML4uiRADGSZlnc+7Ugv3h+MCjkkwOKiOdsNo8k4KSBIG5GcQfKYOOd17AJvqCL6cGQbaLuqv0a64jeDm8oO8/xN/IM0oKw7rMr/2oAJOgIsfeXPkRxWWic9AVIS++H5Qi2r7bUFX+cqFsyUCAwEBBQ==\"}],\"storageProfile\":{\"osDisk\":{\"createOption\":\"Ephemeral\",\"deleteOption\":\"Delete\",\"diskSizeGB\":10}},\"userData\":\"testUserData\",\"adminUsername\":\"testUser\",\"virtioInterface\":\"Transitional\",\"isolateEmulatorThread\":\"False\",\"bootMethod\":\"BIOS\",\"placementHints\":[]}")).create();
    }
}
```

### NetworkFunctions_Delete

```java
/**
 * Samples for NetworkFunctions Delete.
 */
public final class NetworkFunctionsDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkFunctionDelete.json
     */
    /**
     * Sample code: Delete network function resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void deleteNetworkFunctionResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctions().delete("rg", "testNf", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/AzureCore/VirtualNetworkFunctionDelete.json
     */
    /**
     * Sample code: Delete virtual network function resource on AzureCore.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void deleteVirtualNetworkFunctionResourceOnAzureCore(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctions().delete("rg", "testNf", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/AzureOperatorNexus/VirtualNetworkFunctionDelete.json
     */
    /**
     * Sample code: Delete virtual network function resource on AzureOperatorNexus.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void deleteVirtualNetworkFunctionResourceOnAzureOperatorNexus(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctions().delete("rg", "testNf", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkFunctions_ExecuteRequest

```java
import com.azure.resourcemanager.hybridnetwork.models.ExecuteRequestParameters;
import com.azure.resourcemanager.hybridnetwork.models.HttpMethod;
import com.azure.resourcemanager.hybridnetwork.models.RequestMetadata;
import java.util.stream.Collectors;

/**
 * Samples for NetworkFunctions ExecuteRequest.
 */
public final class NetworkFunctionsExecuteRequestSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkFunctionsExecuteRequest.json
     */
    /**
     * Sample code: Send request to  network function services.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void sendRequestToNetworkFunctionServices(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctions().executeRequest("rg", "testNetworkfunction", new ExecuteRequestParameters().withServiceEndpoint("serviceEndpoint").withRequestMetadata(new RequestMetadata().withRelativePath("/simProfiles/testSimProfile").withHttpMethod(HttpMethod.POST).withSerializedBody("{\"subscriptionProfile\":\"ChantestSubscription15\",\"permanentKey\":\"00112233445566778899AABBCCDDEEFF\",\"opcOperatorCode\":\"63bfa50ee6523365ff14c1f45f88737d\",\"staticIpAddresses\":{\"internet\":{\"ipv4Addr\":\"198.51.100.1\",\"ipv6Prefix\":\"2001:db8:abcd:12::0/64\"},\"another_network\":{\"ipv6Prefix\":\"2001:111:cdef:22::0/64\"}}}").withApiVersion("apiVersionQueryString")), com.azure.core.util.Context.NONE);
    }
}
```

### NetworkFunctions_GetByResourceGroup

```java
/**
 * Samples for NetworkFunctions GetByResourceGroup.
 */
public final class NetworkFunctionsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/AzureOperatorNexus/VirtualNetworkFunctionGet.json
     */
    /**
     * Sample code: Get virtual network function resource on AzureOperatorNexus.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getVirtualNetworkFunctionResourceOnAzureOperatorNexus(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctions().getByResourceGroupWithResponse("rg", "testNf", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkFunctionGet.json
     */
    /**
     * Sample code: Get network function resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getNetworkFunctionResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctions().getByResourceGroupWithResponse("rg", "testNf", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/AzureCore/VirtualNetworkFunctionGet.json
     */
    /**
     * Sample code: Get virtual network function resource on AzureCore.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getVirtualNetworkFunctionResourceOnAzureCore(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctions().getByResourceGroupWithResponse("rg", "testNf", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkFunctions_List

```java
/**
 * Samples for NetworkFunctions List.
 */
public final class NetworkFunctionsListSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkFunctionListBySubscription.json
     */
    /**
     * Sample code: List all network function resources in subscription.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void listAllNetworkFunctionResourcesInSubscription(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctions().list(com.azure.core.util.Context.NONE);
    }
}
```

### NetworkFunctions_ListByResourceGroup

```java
/**
 * Samples for NetworkFunctions ListByResourceGroup.
 */
public final class NetworkFunctionsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkFunctionListByResourceGroup.json
     */
    /**
     * Sample code: List network function in resource group.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void listNetworkFunctionInResourceGroup(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkFunctions().listByResourceGroup("rg", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkFunctions_UpdateTags

```java
import com.azure.resourcemanager.hybridnetwork.models.NetworkFunction;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for NetworkFunctions UpdateTags.
 */
public final class NetworkFunctionsUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkFunctionUpdateTags.json
     */
    /**
     * Sample code: Update tags for network function resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void updateTagsForNetworkFunctionResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        NetworkFunction resource = manager.networkFunctions().getByResourceGroupWithResponse("rg", "testNf", com.azure.core.util.Context.NONE).getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### NetworkServiceDesignGroups_CreateOrUpdate

```java
/**
 * Samples for NetworkServiceDesignGroups CreateOrUpdate.
 */
public final class NetworkServiceDesignGroupsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkServiceDesignGroupCreate.json
     */
    /**
     * Sample code: Create or update the network service design group.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createOrUpdateTheNetworkServiceDesignGroup(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkServiceDesignGroups().define("TestNetworkServiceDesignGroupName").withRegion("eastus").withExistingPublisher("rg", "TestPublisher").create();
    }
}
```

### NetworkServiceDesignGroups_Delete

```java
/**
 * Samples for NetworkServiceDesignGroups Delete.
 */
public final class NetworkServiceDesignGroupsDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkServiceDesignGroupDelete.json
     */
    /**
     * Sample code: Delete a network function group resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void deleteANetworkFunctionGroupResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkServiceDesignGroups().delete("rg", "TestPublisher", "TestNetworkServiceDesignGroupName", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkServiceDesignGroups_Get

```java
/**
 * Samples for NetworkServiceDesignGroups Get.
 */
public final class NetworkServiceDesignGroupsGetSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkServiceDesignGroupGet.json
     */
    /**
     * Sample code: Get a networkServiceDesign group resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getANetworkServiceDesignGroupResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkServiceDesignGroups().getWithResponse("rg", "TestPublisher", "TestNetworkServiceDesignGroupName", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkServiceDesignGroups_ListByPublisher

```java
/**
 * Samples for NetworkServiceDesignGroups ListByPublisher.
 */
public final class NetworkServiceDesignGroupsListByPublisherSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkServiceDesignGroupsListByPublisherName.json
     */
    /**
     * Sample code: Get networkServiceDesign groups under publisher resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getNetworkServiceDesignGroupsUnderPublisherResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkServiceDesignGroups().listByPublisher("rg", "TestPublisher", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkServiceDesignGroups_Update

```java
import com.azure.resourcemanager.hybridnetwork.models.NetworkServiceDesignGroup;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for NetworkServiceDesignGroups Update.
 */
public final class NetworkServiceDesignGroupsUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkServiceDesignGroupUpdateTags.json
     */
    /**
     * Sample code: Create or update the network service design group resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createOrUpdateTheNetworkServiceDesignGroupResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        NetworkServiceDesignGroup resource = manager.networkServiceDesignGroups().getWithResponse("rg", "TestPublisher", "TestNetworkServiceDesignGroupName", com.azure.core.util.Context.NONE).getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### NetworkServiceDesignVersions_CreateOrUpdate

```java
import com.azure.resourcemanager.hybridnetwork.models.ArmResourceDefinitionResourceElementTemplate;
import com.azure.resourcemanager.hybridnetwork.models.ArmResourceDefinitionResourceElementTemplateDetails;
import com.azure.resourcemanager.hybridnetwork.models.DependsOnProfile;
import com.azure.resourcemanager.hybridnetwork.models.NetworkServiceDesignVersionPropertiesFormat;
import com.azure.resourcemanager.hybridnetwork.models.NsdArtifactProfile;
import com.azure.resourcemanager.hybridnetwork.models.ReferencedResource;
import com.azure.resourcemanager.hybridnetwork.models.TemplateType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Samples for NetworkServiceDesignVersions CreateOrUpdate.
 */
public final class NetworkServiceDesignVersionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkServiceDesignVersionCreate.json
     */
    /**
     * Sample code: Create or update a network service design version resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createOrUpdateANetworkServiceDesignVersionResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkServiceDesignVersions().define("1.0.0").withRegion("eastus").withExistingNetworkServiceDesignGroup("rg", "TestPublisher", "TestNetworkServiceDesignGroupName").withProperties(new NetworkServiceDesignVersionPropertiesFormat().withConfigurationGroupSchemaReferences(mapOf("MyVM_Configuration", new ReferencedResource().withId("/subscriptions/subid/resourcegroups/contosorg1/providers/microsoft.hybridnetwork/publishers/contosoGroup/networkServiceDesignGroups/NSD_contoso/configurationGroupSchemas/MyVM_Configuration_Schema"))).withResourceElementTemplates(Arrays.asList(new ArmResourceDefinitionResourceElementTemplateDetails().withName("MyVM").withDependsOnProfile(new DependsOnProfile().withInstallDependsOn(Arrays.asList())).withConfiguration(new ArmResourceDefinitionResourceElementTemplate().withTemplateType(TemplateType.ARM_TEMPLATE).withParameterValues("{\"publisherName\":\"{configurationparameters('MyVM_Configuration').publisherName}\",\"skuGroupName\":\"{configurationparameters('MyVM_Configuration').skuGroupName}\",\"skuVersion\":\"{configurationparameters('MyVM_Configuration').skuVersion}\",\"skuOfferingLocation\":\"{configurationparameters('MyVM_Configuration').skuOfferingLocation}\",\"nfviType\":\"{nfvis().nfvisFromSitePerNfviType.AzureCore.nfviAlias1.nfviType}\",\"nfviId\":\"{nfvis().nfvisFromSitePerNfviType.AzureCore.nfviAlias1.nfviId}\",\"allowSoftwareUpdates\":\"{configurationparameters('MyVM_Configuration').allowSoftwareUpdates}\",\"virtualNetworkName\":\"{configurationparameters('MyVM_Configuration').vnetName}\",\"subnetName\":\"{configurationparameters('MyVM_Configuration').subnetName}\",\"subnetAddressPrefix\":\"{configurationparameters('MyVM_Configuration').subnetAddressPrefix}\",\"managedResourceGroup\":\"{configurationparameters('SNSSelf').managedResourceGroupName}\",\"adminPassword\":\"{secretparameters('MyVM_Configuration').adminPassword}\"}").withArtifactProfile(new NsdArtifactProfile().withArtifactStoreReference(new ReferencedResource().withId("/subscriptions/subid/providers/Microsoft.HybridNetwork/publishers/contosoGroup/artifactStoreReference/store1")).withArtifactName("MyVMArmTemplate").withArtifactVersion("1.0.0")))))).create();
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

### NetworkServiceDesignVersions_Delete

```java
/**
 * Samples for NetworkServiceDesignVersions Delete.
 */
public final class NetworkServiceDesignVersionsDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkServiceDesignVersionDelete.json
     */
    /**
     * Sample code: Delete a network service design version.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void deleteANetworkServiceDesignVersion(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkServiceDesignVersions().delete("rg", "TestPublisher", "TestNetworkServiceDesignGroupName", "1.0.0", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkServiceDesignVersions_Get

```java
/**
 * Samples for NetworkServiceDesignVersions Get.
 */
public final class NetworkServiceDesignVersionsGetSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkServiceDesignVersionGet.json
     */
    /**
     * Sample code: Get a network service design version resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getANetworkServiceDesignVersionResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkServiceDesignVersions().getWithResponse("rg", "TestPublisher", "TestNetworkServiceDesignGroupName", "1.0.0", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkServiceDesignVersions_ListByNetworkServiceDesignGroup

```java
/**
 * Samples for NetworkServiceDesignVersions ListByNetworkServiceDesignGroup.
 */
public final class NetworkServiceDesignVersionsListByNetworkServiceDesignGroupSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkServiceDesignVersionListByNetworkServiceDesignGroup.json
     */
    /**
     * Sample code: Get Publisher resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getPublisherResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkServiceDesignVersions().listByNetworkServiceDesignGroup("rg", "TestPublisher", "TestNetworkServiceDesignGroupName", com.azure.core.util.Context.NONE);
    }
}
```

### NetworkServiceDesignVersions_Update

```java
import com.azure.resourcemanager.hybridnetwork.models.NetworkServiceDesignVersion;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for NetworkServiceDesignVersions Update.
 */
public final class NetworkServiceDesignVersionsUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkServiceDesignVersionUpdateTags.json
     */
    /**
     * Sample code: Update the network service design version tags.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void updateTheNetworkServiceDesignVersionTags(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        NetworkServiceDesignVersion resource = manager.networkServiceDesignVersions().getWithResponse("rg", "TestPublisher", "TestNetworkServiceDesignGroupName", "1.0.0", com.azure.core.util.Context.NONE).getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### NetworkServiceDesignVersions_UpdateState

```java
import com.azure.resourcemanager.hybridnetwork.fluent.models.NetworkServiceDesignVersionUpdateStateInner;
import com.azure.resourcemanager.hybridnetwork.models.VersionState;
import java.util.stream.Collectors;

/**
 * Samples for NetworkServiceDesignVersions UpdateState.
 */
public final class NetworkServiceDesignVersionsUpdateStateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/NetworkServiceDesignVersionUpdateState.json
     */
    /**
     * Sample code: Update network service design version state.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void updateNetworkServiceDesignVersionState(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.networkServiceDesignVersions().updateState("rg", "TestPublisher", "TestNetworkServiceDesignGroupName", "1.0.0", new NetworkServiceDesignVersionUpdateStateInner().withVersionState(VersionState.ACTIVE), com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/GetOperations.json
     */
    /**
     * Sample code: Get Registration Operations.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getRegistrationOperations(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### ProxyArtifact_Get

```java
/**
 * Samples for ProxyArtifact Get.
 */
public final class ProxyArtifactGetSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/PureProxyArtifact/ArtifactGet.json
     */
    /**
     * Sample code: Get an artifact overview.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getAnArtifactOverview(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.proxyArtifacts().get("TestResourceGroup", "TestPublisher", "TestArtifactStoreName", "fedrbac", com.azure.core.util.Context.NONE);
    }
}
```

### ProxyArtifact_List

```java
/**
 * Samples for ProxyArtifact List.
 */
public final class ProxyArtifactListSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/PureProxyArtifact/ArtifactList.json
     */
    /**
     * Sample code: List artifacts under an artifact store.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void listArtifactsUnderAnArtifactStore(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.proxyArtifacts().list("TestResourceGroup", "TestPublisher", "TestArtifactStoreName", com.azure.core.util.Context.NONE);
    }
}
```

### ProxyArtifact_UpdateState

```java
import com.azure.resourcemanager.hybridnetwork.models.ArtifactChangeState;
import com.azure.resourcemanager.hybridnetwork.models.ArtifactChangeStateProperties;
import com.azure.resourcemanager.hybridnetwork.models.ArtifactState;
import java.util.stream.Collectors;

/**
 * Samples for ProxyArtifact UpdateState.
 */
public final class ProxyArtifactUpdateStateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/PureProxyArtifact/ArtifactChangeState.json
     */
    /**
     * Sample code: Update an artifact state.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void updateAnArtifactState(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.proxyArtifacts().updateState("TestResourceGroup", "TestPublisher", "TestArtifactStoreName", "fedrbac", "1.0.0", new ArtifactChangeState().withProperties(new ArtifactChangeStateProperties().withArtifactState(ArtifactState.DEPRECATED)), com.azure.core.util.Context.NONE);
    }
}
```

### Publishers_CreateOrUpdate

```java
import com.azure.resourcemanager.hybridnetwork.models.PublisherPropertiesFormat;
import com.azure.resourcemanager.hybridnetwork.models.PublisherScope;
import java.util.stream.Collectors;

/**
 * Samples for Publishers CreateOrUpdate.
 */
public final class PublishersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/PublisherCreate.json
     */
    /**
     * Sample code: Create or update a publisher resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createOrUpdateAPublisherResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.publishers().define("TestPublisher").withRegion("eastus").withExistingResourceGroup("rg").withProperties(new PublisherPropertiesFormat().withScope(PublisherScope.fromString("Public"))).create();
    }
}
```

### Publishers_Delete

```java
/**
 * Samples for Publishers Delete.
 */
public final class PublishersDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/PublisherDelete.json
     */
    /**
     * Sample code: Delete a publisher resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void deleteAPublisherResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.publishers().delete("rg", "TestPublisher", com.azure.core.util.Context.NONE);
    }
}
```

### Publishers_GetByResourceGroup

```java
/**
 * Samples for Publishers GetByResourceGroup.
 */
public final class PublishersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/PublisherGet.json
     */
    /**
     * Sample code: Get a publisher resource.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getAPublisherResource(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.publishers().getByResourceGroupWithResponse("rg", "TestPublisher", com.azure.core.util.Context.NONE);
    }
}
```

### Publishers_List

```java
/**
 * Samples for Publishers List.
 */
public final class PublishersListSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/PublisherListBySubscription.json
     */
    /**
     * Sample code: List all publisher resources in a subscription.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void listAllPublisherResourcesInASubscription(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.publishers().list(com.azure.core.util.Context.NONE);
    }
}
```

### Publishers_ListByResourceGroup

```java
/**
 * Samples for Publishers ListByResourceGroup.
 */
public final class PublishersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/PublisherListByResourceGroup.json
     */
    /**
     * Sample code: List all publisher resources in a resource group.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void listAllPublisherResourcesInAResourceGroup(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.publishers().listByResourceGroup("rg", com.azure.core.util.Context.NONE);
    }
}
```

### Publishers_Update

```java
import com.azure.resourcemanager.hybridnetwork.models.Publisher;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Publishers Update.
 */
public final class PublishersUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/PublisherUpdateTags.json
     */
    /**
     * Sample code: Update a publisher tags.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void updateAPublisherTags(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        Publisher resource = manager.publishers().getByResourceGroupWithResponse("rg", "TestPublisher", com.azure.core.util.Context.NONE).getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### SiteNetworkServices_CreateOrUpdate

```java
import com.azure.resourcemanager.hybridnetwork.models.OpenDeploymentResourceReference;
import com.azure.resourcemanager.hybridnetwork.models.ReferencedResource;
import com.azure.resourcemanager.hybridnetwork.models.SecretDeploymentResourceReference;
import com.azure.resourcemanager.hybridnetwork.models.SiteNetworkServicePropertiesFormat;
import com.azure.resourcemanager.hybridnetwork.models.Sku;
import com.azure.resourcemanager.hybridnetwork.models.SkuName;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Samples for SiteNetworkServices CreateOrUpdate.
 */
public final class SiteNetworkServicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/SiteNetworkServiceCreate.json
     */
    /**
     * Sample code: Create site network service.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createSiteNetworkService(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.siteNetworkServices().define("testSiteNetworkServiceName").withRegion("westUs2").withExistingResourceGroup("rg1").withProperties(new SiteNetworkServicePropertiesFormat().withSiteReference(new ReferencedResource().withId("/subscriptions/subid/resourcegroups/contosorg1/providers/microsoft.hybridnetwork/sites/testSite")).withNetworkServiceDesignVersionResourceReference(new OpenDeploymentResourceReference().withId("/subscriptions/subid/resourcegroups/rg/providers/Microsoft.HybridNetwork/publishers/TestPublisher/networkServiceDesignGroups/TestNetworkServiceDesignGroupName/networkServiceDesignVersions/1.0.0")).withDesiredStateConfigurationGroupValueReferences(mapOf("MyVM_Configuration", new ReferencedResource().withId("/subscriptions/subid/resourcegroups/contosorg1/providers/microsoft.hybridnetwork/configurationgroupvalues/MyVM_Configuration1")))).withSku(new Sku().withName(SkuName.STANDARD)).create();
    }

    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/SiteNetworkServiceFirstPartyCreate.json
     */
    /**
     * Sample code: Create first party site network service.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createFirstPartySiteNetworkService(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.siteNetworkServices().define("testSiteNetworkServiceName").withRegion("westUs2").withExistingResourceGroup("rg1").withProperties(new SiteNetworkServicePropertiesFormat().withSiteReference(new ReferencedResource().withId("/subscriptions/subid/resourcegroups/contosorg1/providers/microsoft.hybridnetwork/sites/testSite")).withNetworkServiceDesignVersionResourceReference(new SecretDeploymentResourceReference().withId("/subscriptions/subid/resourcegroups/rg/providers/Microsoft.HybridNetwork/publishers/TestPublisher/networkServiceDesignGroups/TestNetworkServiceDesignGroupName/networkServiceDesignVersions/1.0.0")).withDesiredStateConfigurationGroupValueReferences(mapOf("MyVM_Configuration", new ReferencedResource().withId("/subscriptions/subid/resourcegroups/contosorg1/providers/microsoft.hybridnetwork/configurationgroupvalues/MyVM_Configuration1")))).withSku(new Sku().withName(SkuName.STANDARD)).create();
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

### SiteNetworkServices_Delete

```java
/**
 * Samples for SiteNetworkServices Delete.
 */
public final class SiteNetworkServicesDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/SiteNetworkServiceDelete.json
     */
    /**
     * Sample code: Delete network site.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void deleteNetworkSite(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.siteNetworkServices().delete("rg1", "testSiteNetworkServiceName", com.azure.core.util.Context.NONE);
    }
}
```

### SiteNetworkServices_GetByResourceGroup

```java
/**
 * Samples for SiteNetworkServices GetByResourceGroup.
 */
public final class SiteNetworkServicesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/SiteNetworkServiceGet.json
     */
    /**
     * Sample code: Get network site.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getNetworkSite(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.siteNetworkServices().getByResourceGroupWithResponse("rg1", "testSiteNetworkServiceName", com.azure.core.util.Context.NONE);
    }
}
```

### SiteNetworkServices_List

```java
/**
 * Samples for SiteNetworkServices List.
 */
public final class SiteNetworkServicesListSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/SiteNetworkServiceListBySubscription.json
     */
    /**
     * Sample code: List all hybrid network sites in a subscription.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void listAllHybridNetworkSitesInASubscription(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.siteNetworkServices().list(com.azure.core.util.Context.NONE);
    }
}
```

### SiteNetworkServices_ListByResourceGroup

```java
/**
 * Samples for SiteNetworkServices ListByResourceGroup.
 */
public final class SiteNetworkServicesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/SiteNetworkServiceListByResourceGroup.json
     */
    /**
     * Sample code: List all network sites.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void listAllNetworkSites(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.siteNetworkServices().listByResourceGroup("rg1", com.azure.core.util.Context.NONE);
    }
}
```

### SiteNetworkServices_UpdateTags

```java
import com.azure.resourcemanager.hybridnetwork.models.SiteNetworkService;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for SiteNetworkServices UpdateTags.
 */
public final class SiteNetworkServicesUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/SiteNetworkServiceUpdateTags.json
     */
    /**
     * Sample code: Update network site tags.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void updateNetworkSiteTags(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        SiteNetworkService resource = manager.siteNetworkServices().getByResourceGroupWithResponse("rg1", "testSiteNetworkServiceName", com.azure.core.util.Context.NONE).getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### Sites_CreateOrUpdate

```java
import com.azure.resourcemanager.hybridnetwork.models.AzureArcK8SClusterNfviDetails;
import com.azure.resourcemanager.hybridnetwork.models.AzureCoreNfviDetails;
import com.azure.resourcemanager.hybridnetwork.models.AzureOperatorNexusClusterNfviDetails;
import com.azure.resourcemanager.hybridnetwork.models.ReferencedResource;
import com.azure.resourcemanager.hybridnetwork.models.SitePropertiesFormat;
import java.util.Arrays;

/**
 * Samples for Sites CreateOrUpdate.
 */
public final class SitesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/SiteCreate.json
     */
    /**
     * Sample code: Create network site.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void createNetworkSite(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.sites().define("testSite").withRegion("westUs2").withExistingResourceGroup("rg1").withProperties(new SitePropertiesFormat().withNfvis(Arrays.asList(new AzureCoreNfviDetails().withName("nfvi1").withLocation("westUs2"), new AzureArcK8SClusterNfviDetails().withName("nfvi2").withCustomLocationReference(new ReferencedResource().withId("/subscriptions/subid/resourceGroups/testResourceGroup/providers/Microsoft.ExtendedLocation/customLocations/testCustomLocation1")), new AzureOperatorNexusClusterNfviDetails().withName("nfvi3").withCustomLocationReference(new ReferencedResource().withId("/subscriptions/subid/resourceGroups/testResourceGroup/providers/Microsoft.ExtendedLocation/customLocations/testCustomLocation2"))))).create();
    }
}
```

### Sites_Delete

```java
/**
 * Samples for Sites Delete.
 */
public final class SitesDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/SiteDelete.json
     */
    /**
     * Sample code: Delete network site.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void deleteNetworkSite(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.sites().delete("rg1", "testSite", com.azure.core.util.Context.NONE);
    }
}
```

### Sites_GetByResourceGroup

```java
/**
 * Samples for Sites GetByResourceGroup.
 */
public final class SitesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/SiteGet.json
     */
    /**
     * Sample code: Get network site.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void getNetworkSite(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.sites().getByResourceGroupWithResponse("rg1", "testSite", com.azure.core.util.Context.NONE);
    }
}
```

### Sites_List

```java
/**
 * Samples for Sites List.
 */
public final class SitesListSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/SiteListBySubscription.json
     */
    /**
     * Sample code: List all hybrid network sites in a subscription.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void listAllHybridNetworkSitesInASubscription(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.sites().list(com.azure.core.util.Context.NONE);
    }
}
```

### Sites_ListByResourceGroup

```java
/**
 * Samples for Sites ListByResourceGroup.
 */
public final class SitesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/SiteListByResourceGroup.json
     */
    /**
     * Sample code: List all network sites.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void listAllNetworkSites(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        manager.sites().listByResourceGroup("rg1", com.azure.core.util.Context.NONE);
    }
}
```

### Sites_UpdateTags

```java
import com.azure.resourcemanager.hybridnetwork.models.Site;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Sites UpdateTags.
 */
public final class SitesUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/hybridnetwork/resource-manager/Microsoft.HybridNetwork/stable/2023-09-01/examples/SiteUpdateTags.json
     */
    /**
     * Sample code: Update network site tags.
     * 
     * @param manager Entry point to HybridNetworkManager.
     */
    public static void updateNetworkSiteTags(com.azure.resourcemanager.hybridnetwork.HybridNetworkManager manager) {
        Site resource = manager.sites().getByResourceGroupWithResponse("rg1", "testSite", com.azure.core.util.Context.NONE).getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

