# Code snippets and samples


## Agreements

- [CreateOrUpdate](#agreements_createorupdate)
- [Delete](#agreements_delete)
- [Get](#agreements_get)
- [List](#agreements_list)

## CloudResources

- [CreateOrUpdate](#cloudresources_createorupdate)
- [Delete](#cloudresources_delete)
- [Get](#cloudresources_get)
- [ListByResourceGroup](#cloudresources_listbyresourcegroup)
- [Update](#cloudresources_update)

## Clouds

- [CreateOrUpdate](#clouds_createorupdate)
- [Delete](#clouds_delete)
- [GetByResourceGroup](#clouds_getbyresourcegroup)
- [List](#clouds_list)
- [ListByResourceGroup](#clouds_listbyresourcegroup)
- [Update](#clouds_update)

## ComputeConfigs

- [CreateOrUpdate](#computeconfigs_createorupdate)
- [Delete](#computeconfigs_delete)
- [Get](#computeconfigs_get)
- [ListByResourceGroup](#computeconfigs_listbyresourcegroup)
- [Update](#computeconfigs_update)

## ContainerImages

- [CreateOrUpdate](#containerimages_createorupdate)
- [Delete](#containerimages_delete)
- [Get](#containerimages_get)
- [ListByResourceGroup](#containerimages_listbyresourcegroup)
- [Update](#containerimages_update)

## Jobs

- [CreateOrUpdate](#jobs_createorupdate)
- [Delete](#jobs_delete)
- [Get](#jobs_get)
- [ListByResourceGroup](#jobs_listbyresourcegroup)
- [Update](#jobs_update)

## OperationStatuses

- [Get](#operationstatuses_get)

## Operations

- [List](#operations_list)

## Projects

- [CreateOrUpdate](#projects_createorupdate)
- [Delete](#projects_delete)
- [Get](#projects_get)
- [ListByResourceGroup](#projects_listbyresourcegroup)

## Services

- [CreateOrUpdate](#services_createorupdate)
- [Delete](#services_delete)
- [Get](#services_get)
- [ListByResourceGroup](#services_listbyresourcegroup)
- [Update](#services_update)

## Workspaces

- [CreateOrUpdate](#workspaces_createorupdate)
- [Delete](#workspaces_delete)
- [Get](#workspaces_get)
- [ListByResourceGroup](#workspaces_listbyresourcegroup)
- [Update](#workspaces_update)
### Agreements_CreateOrUpdate

```java
import com.azure.resourcemanager.anyscale.models.AgreementProperties;

/**
 * Samples for Agreements CreateOrUpdate.
 */
public final class AgreementsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01/Agreements_CreateOrUpdate.json
     */
    /**
     * Sample code: Sign an agreement via PUT.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void signAnAgreementViaPUT(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.agreements().define("default").withProperties(new AgreementProperties()).create();
    }
}
```

### Agreements_Delete

```java
/**
 * Samples for Agreements Delete.
 */
public final class AgreementsDeleteSamples {
    /*
     * x-ms-original-file: 2026-09-01/Agreements_Delete.json
     */
    /**
     * Sample code: Revoke acceptance of an agreement.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void revokeAcceptanceOfAnAgreement(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.agreements().deleteWithResponse("default", com.azure.core.util.Context.NONE);
    }
}
```

### Agreements_Get

```java
/**
 * Samples for Agreements Get.
 */
public final class AgreementsGetSamples {
    /*
     * x-ms-original-file: 2026-09-01/Agreements_Get.json
     */
    /**
     * Sample code: Get an agreement.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void getAnAgreement(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.agreements().getWithResponse("default", com.azure.core.util.Context.NONE);
    }
}
```

### Agreements_List

```java
/**
 * Samples for Agreements List.
 */
public final class AgreementsListSamples {
    /*
     * x-ms-original-file: 2026-09-01/Agreements_ListBySubscription.json
     */
    /**
     * Sample code: List the agreements in a subscription.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void listTheAgreementsInASubscription(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.agreements().list(com.azure.core.util.Context.NONE);
    }
}
```

### CloudResources_CreateOrUpdate

```java
import com.azure.resourcemanager.anyscale.models.CloudResourceProperties;
import com.azure.resourcemanager.anyscale.models.ComputeStack;
import com.azure.resourcemanager.anyscale.models.Provider;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for CloudResources CreateOrUpdate.
 */
public final class CloudResourcesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01/CloudResources_CreateOrUpdate.json
     */
    /**
     * Sample code: CloudResources_CreateOrUpdate.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void cloudResourcesCreateOrUpdate(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.cloudResources()
            .define("AnyscaleCloudResource")
            .withRegion("eastus")
            .withExistingCloud("rganyscale", "AnyscaleCloud")
            .withTags(mapOf("environment", "production", "team", "ml-platform"))
            .withProperties(new CloudResourceProperties().withProvider(Provider.AWS)
                .withComputeStack(ComputeStack.K8S)
                .withCloudStorageBucketName("s3://anyscale-storage")
                .withCloudStorageBucketEndpoint("https://anyscale-storage.s3.us-west-2.amazonaws.com")
                .withRegion("us-west-2")
                .withCloudStorageBucketRegion("us-west-2")
                .withAnyscaleOperatorIamIdentity("arn:aws:iam::123456789012:role/anyscale-operator"))
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

### CloudResources_Delete

```java
/**
 * Samples for CloudResources Delete.
 */
public final class CloudResourcesDeleteSamples {
    /*
     * x-ms-original-file: 2026-09-01/CloudResources_Delete.json
     */
    /**
     * Sample code: CloudResources_Delete.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void cloudResourcesDelete(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.cloudResources()
            .delete("rganyscale", "AnyscaleCloud", "AnyscaleCloudResource", com.azure.core.util.Context.NONE);
    }
}
```

### CloudResources_Get

```java
/**
 * Samples for CloudResources Get.
 */
public final class CloudResourcesGetSamples {
    /*
     * x-ms-original-file: 2026-09-01/CloudResources_Get.json
     */
    /**
     * Sample code: CloudResources_Get.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void cloudResourcesGet(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.cloudResources()
            .getWithResponse("rganyscale", "AnyscaleCloud", "AnyscaleCloudResource", com.azure.core.util.Context.NONE);
    }
}
```

### CloudResources_ListByResourceGroup

```java
/**
 * Samples for CloudResources ListByResourceGroup.
 */
public final class CloudResourcesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-09-01/CloudResources_ListByResourceGroup.json
     */
    /**
     * Sample code: CloudResources_ListByResourceGroup.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void cloudResourcesListByResourceGroup(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.cloudResources().listByResourceGroup("rganyscale", "AnyscaleCloud", com.azure.core.util.Context.NONE);
    }
}
```

### CloudResources_Update

```java
import com.azure.resourcemanager.anyscale.models.CloudResource;
import com.azure.resourcemanager.anyscale.models.CloudResourceUpdateProperties;
import com.azure.resourcemanager.anyscale.models.ComputeStack;
import com.azure.resourcemanager.anyscale.models.Provider;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for CloudResources Update.
 */
public final class CloudResourcesUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01/CloudResources_Update.json
     */
    /**
     * Sample code: CloudResources_Update.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void cloudResourcesUpdate(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        CloudResource resource = manager.cloudResources()
            .getWithResponse("rganyscale", "AnyscaleCloud", "AnyscaleCloudResource", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("environment", "staging", "team", "ml-platform"))
            .withProperties(new CloudResourceUpdateProperties().withProvider(Provider.AWS)
                .withComputeStack(ComputeStack.K8S)
                .withCloudStorageBucketName("s3://anyscale-storage")
                .withCloudStorageBucketEndpoint("https://anyscale-storage.s3.us-west-2.amazonaws.com")
                .withAnyscaleOperatorIamIdentity("arn:aws:iam::123456789012:role/anyscale-operator"))
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

### Clouds_CreateOrUpdate

```java
import com.azure.resourcemanager.anyscale.models.CloudProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Clouds CreateOrUpdate.
 */
public final class CloudsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01/Clouds_CreateOrUpdate.json
     */
    /**
     * Sample code: Clouds_CreateOrUpdate.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void cloudsCreateOrUpdate(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.clouds()
            .define("testcloud")
            .withRegion("eastus")
            .withExistingResourceGroup("rganyscale")
            .withTags(mapOf("environment", "production", "team", "ml-platform"))
            .withProperties(new CloudProperties().withAcrResourceId(
                "/subscriptions/14AB8053-986C-414E-96B3-11D82B8B478D/resourceGroups/rganyscale/providers/Microsoft.ContainerRegistry/registries/anyscaleacr"))
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

### Clouds_Delete

```java
/**
 * Samples for Clouds Delete.
 */
public final class CloudsDeleteSamples {
    /*
     * x-ms-original-file: 2026-09-01/Clouds_Delete.json
     */
    /**
     * Sample code: Clouds_Delete.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void cloudsDelete(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.clouds().delete("rganyscale", "testcloud", com.azure.core.util.Context.NONE);
    }
}
```

### Clouds_GetByResourceGroup

```java
/**
 * Samples for Clouds GetByResourceGroup.
 */
public final class CloudsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-09-01/Clouds_Get.json
     */
    /**
     * Sample code: Clouds_Get.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void cloudsGet(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.clouds().getByResourceGroupWithResponse("rganyscale", "testcloud", com.azure.core.util.Context.NONE);
    }
}
```

### Clouds_List

```java
/**
 * Samples for Clouds List.
 */
public final class CloudsListSamples {
    /*
     * x-ms-original-file: 2026-09-01/Clouds_ListBySubscription.json
     */
    /**
     * Sample code: Clouds_ListBySubscription.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void cloudsListBySubscription(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.clouds().list(com.azure.core.util.Context.NONE);
    }
}
```

### Clouds_ListByResourceGroup

```java
/**
 * Samples for Clouds ListByResourceGroup.
 */
public final class CloudsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-09-01/Clouds_ListByResourceGroup.json
     */
    /**
     * Sample code: Clouds_ListByResourceGroup.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void cloudsListByResourceGroup(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.clouds().listByResourceGroup("rganyscale", com.azure.core.util.Context.NONE);
    }
}
```

### Clouds_Update

```java
import com.azure.resourcemanager.anyscale.models.Cloud;
import com.azure.resourcemanager.anyscale.models.CloudUpdateProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Clouds Update.
 */
public final class CloudsUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01/Clouds_Update.json
     */
    /**
     * Sample code: Clouds_Update.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void cloudsUpdate(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        Cloud resource = manager.clouds()
            .getByResourceGroupWithResponse("rganyscale", "testcloud", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("environment", "production", "team", "ml-platform"))
            .withProperties(new CloudUpdateProperties().withAcrResourceId(
                "/subscriptions/14AB8053-986C-414E-96B3-11D82B8B478D/resourceGroups/rganyscale/providers/Microsoft.ContainerRegistry/registries/anyscaleacr"))
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

### ComputeConfigs_CreateOrUpdate

```java
import com.azure.resourcemanager.anyscale.models.ComputeConfigProperties;
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpec;
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpecOption;
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpecOptionFlags;
import com.azure.resourcemanager.anyscale.models.CpuArchitecture;
import com.azure.resourcemanager.anyscale.models.LogicalResources;
import com.azure.resourcemanager.anyscale.models.LogicalResourcesCustomResources;
import com.azure.resourcemanager.anyscale.models.MarketType;
import com.azure.resourcemanager.anyscale.models.NodeType;
import com.azure.resourcemanager.anyscale.models.NodeTypeFlags;
import com.azure.resourcemanager.anyscale.models.NodeTypeLabels;
import com.azure.resourcemanager.anyscale.models.NodeTypeRequiredLabels;
import com.azure.resourcemanager.anyscale.models.PhysicalResources;
import com.azure.resourcemanager.anyscale.models.WorkerNode;
import java.util.Arrays;

/**
 * Samples for ComputeConfigs CreateOrUpdate.
 */
public final class ComputeConfigsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01/ComputeConfigs_CreateOrUpdate.json
     */
    /**
     * Sample code: ComputeConfigs_CreateOrUpdate.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void computeConfigsCreateOrUpdate(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.computeConfigs()
            .define("testcomputeconfig")
            .withExistingCloud("rganyscale", "testcloud")
            .withProperties(new ComputeConfigProperties().withSpec(new ComputeConfigSpec()
                .withOptions(Arrays.asList(new ComputeConfigSpecOption().withCloudResourceNames(Arrays.asList(
                    "/subscriptions/14AB8053-986C-414E-96B3-11D82B8B478D/resourceGroups/rganyscale/providers/Anyscale.Platform/clouds/testcloud/cloudResources/eastus-cloud-resource"))
                    .withHeadNode(new NodeType().withName("head-node")
                        .withInstanceType("Standard_D8s_v5")
                        .withResources(new LogicalResources().withCpu(8L)
                            .withGpu(0L)
                            .withMemory(32L)
                            .withObjectStoreMemory(8L)
                            .withCustomResources(new LogicalResourcesCustomResources()))
                        .withRequiredResources(new PhysicalResources().withCpu(8L)
                            .withGpu(0L)
                            .withMemory(32L)
                            .withTpu(0L)
                            .withAccelerator("")
                            .withCpuArchitecture(CpuArchitecture.X86_64))
                        .withLabels(new NodeTypeLabels())
                        .withRequiredLabels(new NodeTypeRequiredLabels())
                        .withAdvancedInstanceConfig("{}")
                        .withFlags(new NodeTypeFlags()))
                    .withWorkerNodes(Arrays.asList(new WorkerNode().withName("gpu-worker")
                        .withInstanceType("Standard_NC24ads_A100_v4")
                        .withResources(new LogicalResources().withCpu(24L)
                            .withGpu(1L)
                            .withMemory(220L)
                            .withObjectStoreMemory(50L)
                            .withCustomResources(new LogicalResourcesCustomResources()))
                        .withRequiredResources(new PhysicalResources().withCpu(24L)
                            .withGpu(1L)
                            .withMemory(220L)
                            .withTpu(0L)
                            .withAccelerator("A100")
                            .withCpuArchitecture(CpuArchitecture.X86_64))
                        .withLabels(new NodeTypeLabels())
                        .withRequiredLabels(new NodeTypeRequiredLabels())
                        .withAdvancedInstanceConfig("{}")
                        .withFlags(new NodeTypeFlags())
                        .withMinNodes(1)
                        .withMaxNodes(10)
                        .withMarketType(MarketType.ON_DEMAND)))
                    .withZones(Arrays.asList("eastus-1"))
                    .withAdvancedInstanceConfig("{}")
                    .withFlags(new ComputeConfigSpecOptionFlags())))))
            .create();
    }
}
```

### ComputeConfigs_Delete

```java
/**
 * Samples for ComputeConfigs Delete.
 */
public final class ComputeConfigsDeleteSamples {
    /*
     * x-ms-original-file: 2026-09-01/ComputeConfigs_Delete.json
     */
    /**
     * Sample code: ComputeConfigs_Delete.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void computeConfigsDelete(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.computeConfigs()
            .delete("rganyscale", "testcloud", "testcomputeconfig", com.azure.core.util.Context.NONE);
    }
}
```

### ComputeConfigs_Get

```java
/**
 * Samples for ComputeConfigs Get.
 */
public final class ComputeConfigsGetSamples {
    /*
     * x-ms-original-file: 2026-09-01/ComputeConfigs_Get.json
     */
    /**
     * Sample code: ComputeConfigs_Get.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void computeConfigsGet(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.computeConfigs()
            .getWithResponse("rganyscale", "testcloud", "testcomputeconfig", com.azure.core.util.Context.NONE);
    }
}
```

### ComputeConfigs_ListByResourceGroup

```java
/**
 * Samples for ComputeConfigs ListByResourceGroup.
 */
public final class ComputeConfigsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-09-01/ComputeConfigs_ListByResourceGroup.json
     */
    /**
     * Sample code: ComputeConfigs_ListByResourceGroup.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void computeConfigsListByResourceGroup(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.computeConfigs().listByResourceGroup("rganyscale", "testcloud", com.azure.core.util.Context.NONE);
    }
}
```

### ComputeConfigs_Update

```java
import com.azure.resourcemanager.anyscale.models.ComputeConfig;
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpec;
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpecOption;
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpecOptionFlags;
import com.azure.resourcemanager.anyscale.models.ComputeConfigUpdateProperties;
import com.azure.resourcemanager.anyscale.models.CpuArchitecture;
import com.azure.resourcemanager.anyscale.models.LogicalResources;
import com.azure.resourcemanager.anyscale.models.LogicalResourcesCustomResources;
import com.azure.resourcemanager.anyscale.models.MarketType;
import com.azure.resourcemanager.anyscale.models.NodeType;
import com.azure.resourcemanager.anyscale.models.NodeTypeFlags;
import com.azure.resourcemanager.anyscale.models.NodeTypeLabels;
import com.azure.resourcemanager.anyscale.models.NodeTypeRequiredLabels;
import com.azure.resourcemanager.anyscale.models.PhysicalResources;
import com.azure.resourcemanager.anyscale.models.WorkerNode;
import java.util.Arrays;

/**
 * Samples for ComputeConfigs Update.
 */
public final class ComputeConfigsUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01/ComputeConfigs_Update.json
     */
    /**
     * Sample code: ComputeConfigs_Update.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void computeConfigsUpdate(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        ComputeConfig resource = manager.computeConfigs()
            .getWithResponse("rganyscale", "testcloud", "testcomputeconfig", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new ComputeConfigUpdateProperties().withSpec(new ComputeConfigSpec()
                .withOptions(Arrays.asList(new ComputeConfigSpecOption().withCloudResourceNames(Arrays.asList(
                    "/subscriptions/14AB8053-986C-414E-96B3-11D82B8B478D/resourceGroups/rganyscale/providers/Anyscale.Platform/clouds/testcloud/cloudResources/eastus-cloud-resource"))
                    .withHeadNode(new NodeType().withName("head-node")
                        .withInstanceType("Standard_D8s_v5")
                        .withResources(new LogicalResources().withCpu(8L)
                            .withGpu(0L)
                            .withMemory(32L)
                            .withObjectStoreMemory(8L)
                            .withCustomResources(new LogicalResourcesCustomResources()))
                        .withRequiredResources(new PhysicalResources().withCpu(8L)
                            .withGpu(0L)
                            .withMemory(32L)
                            .withTpu(0L)
                            .withAccelerator("")
                            .withCpuArchitecture(CpuArchitecture.X86_64))
                        .withLabels(new NodeTypeLabels())
                        .withRequiredLabels(new NodeTypeRequiredLabels())
                        .withAdvancedInstanceConfig("{}")
                        .withFlags(new NodeTypeFlags()))
                    .withWorkerNodes(Arrays.asList(new WorkerNode().withName("gpu-worker")
                        .withInstanceType("Standard_NC24ads_A100_v4")
                        .withResources(new LogicalResources().withCpu(24L)
                            .withGpu(1L)
                            .withMemory(220L)
                            .withObjectStoreMemory(50L)
                            .withCustomResources(new LogicalResourcesCustomResources()))
                        .withRequiredResources(new PhysicalResources().withCpu(24L)
                            .withGpu(1L)
                            .withMemory(220L)
                            .withTpu(0L)
                            .withAccelerator("A100")
                            .withCpuArchitecture(CpuArchitecture.X86_64))
                        .withLabels(new NodeTypeLabels())
                        .withRequiredLabels(new NodeTypeRequiredLabels())
                        .withAdvancedInstanceConfig("{}")
                        .withFlags(new NodeTypeFlags())
                        .withMinNodes(1)
                        .withMaxNodes(10)
                        .withMarketType(MarketType.ON_DEMAND)))
                    .withZones(Arrays.asList("eastus-1"))
                    .withAdvancedInstanceConfig("{}")
                    .withFlags(new ComputeConfigSpecOptionFlags())))))
            .apply();
    }
}
```

### ContainerImages_CreateOrUpdate

```java
import com.azure.resourcemanager.anyscale.models.ContainerImageProperties;

/**
 * Samples for ContainerImages CreateOrUpdate.
 */
public final class ContainerImagesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01/ContainerImages_CreateOrUpdate.json
     */
    /**
     * Sample code: ContainerImages_CreateOrUpdate.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void containerImagesCreateOrUpdate(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.containerImages()
            .define("testcontainerimage")
            .withExistingCloud("rganyscale", "testcloud")
            .withProperties(
                new ContainerImageProperties().withContainerImageUri("myacr.azurecr.io/anyscale/my-ray-image:latest"))
            .create();
    }
}
```

### ContainerImages_Delete

```java
/**
 * Samples for ContainerImages Delete.
 */
public final class ContainerImagesDeleteSamples {
    /*
     * x-ms-original-file: 2026-09-01/ContainerImages_Delete.json
     */
    /**
     * Sample code: ContainerImages_Delete.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void containerImagesDelete(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.containerImages()
            .delete("rganyscale", "testcloud", "testcontainerimage", com.azure.core.util.Context.NONE);
    }
}
```

### ContainerImages_Get

```java
/**
 * Samples for ContainerImages Get.
 */
public final class ContainerImagesGetSamples {
    /*
     * x-ms-original-file: 2026-09-01/ContainerImages_Get.json
     */
    /**
     * Sample code: ContainerImages_Get.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void containerImagesGet(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.containerImages()
            .getWithResponse("rganyscale", "testcloud", "testcontainerimage", com.azure.core.util.Context.NONE);
    }
}
```

### ContainerImages_ListByResourceGroup

```java
/**
 * Samples for ContainerImages ListByResourceGroup.
 */
public final class ContainerImagesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-09-01/ContainerImages_ListByResourceGroup.json
     */
    /**
     * Sample code: ContainerImages_ListByResourceGroup.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void containerImagesListByResourceGroup(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.containerImages().listByResourceGroup("rganyscale", "testcloud", com.azure.core.util.Context.NONE);
    }
}
```

### ContainerImages_Update

```java
import com.azure.resourcemanager.anyscale.models.ContainerImage;
import com.azure.resourcemanager.anyscale.models.ContainerImageUpdateProperties;

/**
 * Samples for ContainerImages Update.
 */
public final class ContainerImagesUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01/ContainerImages_Update.json
     */
    /**
     * Sample code: ContainerImages_Update.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void containerImagesUpdate(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        ContainerImage resource = manager.containerImages()
            .getWithResponse("rganyscale", "testcloud", "testcontainerimage", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new ContainerImageUpdateProperties()
                .withContainerfile(
                    "FROM anyscale/ray:2.44.0-py312-cu124\nRUN pip install torch==2.1.0 transformers==4.36.0")
                .withContainerImageUri("myacr.azurecr.io/anyscale/my-ray-image:latest"))
            .apply();
    }
}
```

### Jobs_CreateOrUpdate

```java
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpec;
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpecOption;
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpecOptionFlags;
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpecOrReference;
import com.azure.resourcemanager.anyscale.models.CpuArchitecture;
import com.azure.resourcemanager.anyscale.models.JobConfig;
import com.azure.resourcemanager.anyscale.models.JobConfigEnvVars;
import com.azure.resourcemanager.anyscale.models.JobProperties;
import com.azure.resourcemanager.anyscale.models.LogicalResources;
import com.azure.resourcemanager.anyscale.models.LogicalResourcesCustomResources;
import com.azure.resourcemanager.anyscale.models.MarketType;
import com.azure.resourcemanager.anyscale.models.NodeType;
import com.azure.resourcemanager.anyscale.models.NodeTypeFlags;
import com.azure.resourcemanager.anyscale.models.NodeTypeLabels;
import com.azure.resourcemanager.anyscale.models.NodeTypeRequiredLabels;
import com.azure.resourcemanager.anyscale.models.PhysicalResources;
import com.azure.resourcemanager.anyscale.models.WorkerNode;
import java.time.Duration;
import java.util.Arrays;

/**
 * Samples for Jobs CreateOrUpdate.
 */
public final class JobsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01/Jobs_CreateOrUpdate.json
     */
    /**
     * Sample code: Jobs_CreateOrUpdate.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void jobsCreateOrUpdate(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.jobs()
            .define("testjob")
            .withExistingProject("rganyscale", "testcloud", "testproject")
            .withProperties(new JobProperties().withConfig(new JobConfig()
                .withImageUri("anyscale/ray:2.44.0-py312-cu124")
                .withComputeConfig(new ComputeConfigSpecOrReference().withSpec(new ComputeConfigSpec()
                    .withOptions(Arrays.asList(new ComputeConfigSpecOption().withCloudResourceNames(Arrays.asList(
                        "/subscriptions/14AB8053-986C-414E-96B3-11D82B8B478D/resourceGroups/rganyscale/providers/Anyscale.Platform/clouds/testcloud/cloudResources/eastus-cloud-resource"))
                        .withHeadNode(new NodeType().withName("head-node")
                            .withInstanceType("Standard_D8s_v5")
                            .withResources(new LogicalResources().withCpu(8L)
                                .withGpu(0L)
                                .withMemory(32L)
                                .withObjectStoreMemory(8L)
                                .withCustomResources(new LogicalResourcesCustomResources()))
                            .withRequiredResources(new PhysicalResources().withCpu(8L)
                                .withGpu(0L)
                                .withMemory(32L)
                                .withTpu(0L)
                                .withAccelerator("")
                                .withCpuArchitecture(CpuArchitecture.X86_64))
                            .withLabels(new NodeTypeLabels())
                            .withRequiredLabels(new NodeTypeRequiredLabels())
                            .withAdvancedInstanceConfig("{}")
                            .withFlags(new NodeTypeFlags()))
                        .withWorkerNodes(Arrays.asList(new WorkerNode().withName("gpu-worker")
                            .withInstanceType("Standard_NC24ads_A100_v4")
                            .withResources(new LogicalResources().withCpu(24L)
                                .withGpu(1L)
                                .withMemory(220L)
                                .withObjectStoreMemory(50L)
                                .withCustomResources(new LogicalResourcesCustomResources()))
                            .withRequiredResources(new PhysicalResources().withCpu(24L)
                                .withGpu(1L)
                                .withMemory(220L)
                                .withTpu(0L)
                                .withAccelerator("A100")
                                .withCpuArchitecture(CpuArchitecture.X86_64))
                            .withLabels(new NodeTypeLabels())
                            .withRequiredLabels(new NodeTypeRequiredLabels())
                            .withAdvancedInstanceConfig("{}")
                            .withFlags(new NodeTypeFlags())
                            .withMinNodes(1)
                            .withMaxNodes(10)
                            .withMarketType(MarketType.ON_DEMAND)))
                        .withZones(Arrays.asList("eastus-1"))
                        .withAdvancedInstanceConfig("{}")
                        .withFlags(new ComputeConfigSpecOptionFlags()))))
                    .withComputeConfigName(
                        "/subscriptions/14AB8053-986C-414E-96B3-11D82B8B478D/resourceGroups/rganyscale/providers/Anyscale.Platform/clouds/testcloud/computeConfigs/my-compute-config"))
                .withEntrypoint("python train.py --epochs 10 --batch-size 32")
                .withWorkingDir("https://myaccount.blob.core.windows.net/my-container/my-project")
                .withPyModules(Arrays
                    .asList("https://myaccount.blob.core.windows.net/my-container/my_module-0.1.0-py3-none-any.whl"))
                .withPipRequirements(Arrays.asList("torch==2.1.0", "transformers==4.36.0"))
                .withEnvVars(new JobConfigEnvVars())
                .withMaxRetries(3)
                .withTimeout(Duration.parse("PT23M"))))
            .create();
    }
}
```

### Jobs_Delete

```java
/**
 * Samples for Jobs Delete.
 */
public final class JobsDeleteSamples {
    /*
     * x-ms-original-file: 2026-09-01/Jobs_Delete.json
     */
    /**
     * Sample code: Jobs_Delete.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void jobsDelete(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.jobs().delete("rganyscale", "testcloud", "testproject", "testjob", com.azure.core.util.Context.NONE);
    }
}
```

### Jobs_Get

```java
/**
 * Samples for Jobs Get.
 */
public final class JobsGetSamples {
    /*
     * x-ms-original-file: 2026-09-01/Jobs_Get.json
     */
    /**
     * Sample code: Jobs_Get.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void jobsGet(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.jobs()
            .getWithResponse("rganyscale", "testcloud", "testproject", "testjob", com.azure.core.util.Context.NONE);
    }
}
```

### Jobs_ListByResourceGroup

```java
/**
 * Samples for Jobs ListByResourceGroup.
 */
public final class JobsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-09-01/Jobs_ListByResourceGroup.json
     */
    /**
     * Sample code: Jobs_ListByResourceGroup.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void jobsListByResourceGroup(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.jobs().listByResourceGroup("rganyscale", "testcloud", "testproject", com.azure.core.util.Context.NONE);
    }
}
```

### Jobs_Update

```java
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpec;
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpecOption;
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpecOptionFlags;
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpecOrReference;
import com.azure.resourcemanager.anyscale.models.CpuArchitecture;
import com.azure.resourcemanager.anyscale.models.Job;
import com.azure.resourcemanager.anyscale.models.JobConfig;
import com.azure.resourcemanager.anyscale.models.JobConfigEnvVars;
import com.azure.resourcemanager.anyscale.models.JobUpdateProperties;
import com.azure.resourcemanager.anyscale.models.LogicalResources;
import com.azure.resourcemanager.anyscale.models.LogicalResourcesCustomResources;
import com.azure.resourcemanager.anyscale.models.MarketType;
import com.azure.resourcemanager.anyscale.models.NodeType;
import com.azure.resourcemanager.anyscale.models.NodeTypeFlags;
import com.azure.resourcemanager.anyscale.models.NodeTypeLabels;
import com.azure.resourcemanager.anyscale.models.NodeTypeRequiredLabels;
import com.azure.resourcemanager.anyscale.models.PhysicalResources;
import com.azure.resourcemanager.anyscale.models.WorkerNode;
import java.time.Duration;
import java.util.Arrays;

/**
 * Samples for Jobs Update.
 */
public final class JobsUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01/Jobs_Update.json
     */
    /**
     * Sample code: Jobs_Update.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void jobsUpdate(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        Job resource = manager.jobs()
            .getWithResponse("rganyscale", "testcloud", "testproject", "testjob", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new JobUpdateProperties().withConfig(new JobConfig()
                .withImageUri("anyscale/ray:2.44.0-py312-cu124")
                .withComputeConfig(new ComputeConfigSpecOrReference().withSpec(new ComputeConfigSpec()
                    .withOptions(Arrays.asList(new ComputeConfigSpecOption().withCloudResourceNames(Arrays.asList(
                        "/subscriptions/14AB8053-986C-414E-96B3-11D82B8B478D/resourceGroups/rganyscale/providers/Anyscale.Platform/clouds/testcloud/cloudResources/eastus-cloud-resource"))
                        .withHeadNode(new NodeType().withName("head-node")
                            .withInstanceType("Standard_D8s_v5")
                            .withResources(new LogicalResources().withCpu(8L)
                                .withGpu(0L)
                                .withMemory(32L)
                                .withObjectStoreMemory(8L)
                                .withCustomResources(new LogicalResourcesCustomResources()))
                            .withRequiredResources(new PhysicalResources().withCpu(8L)
                                .withGpu(0L)
                                .withMemory(32L)
                                .withTpu(0L)
                                .withAccelerator("")
                                .withCpuArchitecture(CpuArchitecture.X86_64))
                            .withLabels(new NodeTypeLabels())
                            .withRequiredLabels(new NodeTypeRequiredLabels())
                            .withAdvancedInstanceConfig("{}")
                            .withFlags(new NodeTypeFlags()))
                        .withWorkerNodes(Arrays.asList(new WorkerNode().withName("gpu-worker")
                            .withInstanceType("Standard_NC24ads_A100_v4")
                            .withResources(new LogicalResources().withCpu(24L)
                                .withGpu(1L)
                                .withMemory(220L)
                                .withObjectStoreMemory(50L)
                                .withCustomResources(new LogicalResourcesCustomResources()))
                            .withRequiredResources(new PhysicalResources().withCpu(24L)
                                .withGpu(1L)
                                .withMemory(220L)
                                .withTpu(0L)
                                .withAccelerator("A100")
                                .withCpuArchitecture(CpuArchitecture.X86_64))
                            .withLabels(new NodeTypeLabels())
                            .withRequiredLabels(new NodeTypeRequiredLabels())
                            .withAdvancedInstanceConfig("{}")
                            .withFlags(new NodeTypeFlags())
                            .withMinNodes(1)
                            .withMaxNodes(10)
                            .withMarketType(MarketType.ON_DEMAND)))
                        .withZones(Arrays.asList("eastus-1"))
                        .withAdvancedInstanceConfig("{}")
                        .withFlags(new ComputeConfigSpecOptionFlags()))))
                    .withComputeConfigName(
                        "/subscriptions/14AB8053-986C-414E-96B3-11D82B8B478D/resourceGroups/rganyscale/providers/Anyscale.Platform/clouds/testcloud/computeConfigs/my-compute-config"))
                .withEntrypoint("python train.py --epochs 10 --batch-size 32")
                .withWorkingDir("https://myaccount.blob.core.windows.net/my-container/my-project")
                .withPyModules(Arrays
                    .asList("https://myaccount.blob.core.windows.net/my-container/my_module-0.1.0-py3-none-any.whl"))
                .withPipRequirements(Arrays.asList("torch==2.1.0", "transformers==4.36.0"))
                .withEnvVars(new JobConfigEnvVars())
                .withMaxRetries(3)
                .withTimeout(Duration.parse("PT23M"))))
            .apply();
    }
}
```

### OperationStatuses_Get

```java
/**
 * Samples for OperationStatuses Get.
 */
public final class OperationStatusesGetSamples {
    /*
     * x-ms-original-file: 2026-09-01/OperationStatuses_Get.json
     */
    /**
     * Sample code: OperationStatuses_Get.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void operationStatusesGet(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.operationStatuses()
            .getWithResponse("eastus", "9e4d5a19-61f0-4c75-8a34-2e16a915b062", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-09-01/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void operationsList(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Projects_CreateOrUpdate

```java
import com.azure.resourcemanager.anyscale.models.ProjectProperties;

/**
 * Samples for Projects CreateOrUpdate.
 */
public final class ProjectsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01/Projects_CreateOrUpdate.json
     */
    /**
     * Sample code: Projects_CreateOrUpdate.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void projectsCreateOrUpdate(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.projects()
            .define("testproject")
            .withExistingCloud("rganyscale", "testcloud")
            .withProperties(new ProjectProperties())
            .create();
    }
}
```

### Projects_Delete

```java
/**
 * Samples for Projects Delete.
 */
public final class ProjectsDeleteSamples {
    /*
     * x-ms-original-file: 2026-09-01/Projects_Delete.json
     */
    /**
     * Sample code: Projects_Delete.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void projectsDelete(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.projects().delete("rganyscale", "testcloud", "testproject", com.azure.core.util.Context.NONE);
    }
}
```

### Projects_Get

```java
/**
 * Samples for Projects Get.
 */
public final class ProjectsGetSamples {
    /*
     * x-ms-original-file: 2026-09-01/Projects_Get.json
     */
    /**
     * Sample code: Projects_Get.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void projectsGet(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.projects().getWithResponse("rganyscale", "testcloud", "testproject", com.azure.core.util.Context.NONE);
    }
}
```

### Projects_ListByResourceGroup

```java
/**
 * Samples for Projects ListByResourceGroup.
 */
public final class ProjectsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-09-01/Projects_ListByResourceGroup.json
     */
    /**
     * Sample code: Projects_ListByResourceGroup.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void projectsListByResourceGroup(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.projects().listByResourceGroup("rganyscale", "testcloud", com.azure.core.util.Context.NONE);
    }
}
```

### Services_CreateOrUpdate

```java
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpec;
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpecOption;
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpecOptionFlags;
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpecOrReference;
import com.azure.resourcemanager.anyscale.models.CpuArchitecture;
import com.azure.resourcemanager.anyscale.models.LogicalResources;
import com.azure.resourcemanager.anyscale.models.LogicalResourcesCustomResources;
import com.azure.resourcemanager.anyscale.models.MarketType;
import com.azure.resourcemanager.anyscale.models.NodeType;
import com.azure.resourcemanager.anyscale.models.NodeTypeFlags;
import com.azure.resourcemanager.anyscale.models.NodeTypeLabels;
import com.azure.resourcemanager.anyscale.models.NodeTypeRequiredLabels;
import com.azure.resourcemanager.anyscale.models.PhysicalResources;
import com.azure.resourcemanager.anyscale.models.RayGcsExternalStorageConfig;
import com.azure.resourcemanager.anyscale.models.RolloutStrategy;
import com.azure.resourcemanager.anyscale.models.ServiceDeploymentConfig;
import com.azure.resourcemanager.anyscale.models.ServiceDeploymentConfigApplication;
import com.azure.resourcemanager.anyscale.models.ServiceDeploymentConfigEnvVars;
import com.azure.resourcemanager.anyscale.models.ServiceDeploymentConfigGrpcConfig;
import com.azure.resourcemanager.anyscale.models.ServiceDeploymentConfigHttpConfig;
import com.azure.resourcemanager.anyscale.models.ServiceProperties;
import com.azure.resourcemanager.anyscale.models.TracingConfig;
import com.azure.resourcemanager.anyscale.models.WorkerNode;
import java.util.Arrays;

/**
 * Samples for Services CreateOrUpdate.
 */
public final class ServicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01/Services_CreateOrUpdate.json
     */
    /**
     * Sample code: Services_CreateOrUpdate.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void servicesCreateOrUpdate(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.services()
            .define("testservice")
            .withExistingProject("rganyscale", "testcloud", "testproject")
            .withProperties(new ServiceProperties().withDeploymentConfig(new ServiceDeploymentConfig()
                .withDescription("Production model serving endpoint")
                .withComputeConfig(new ComputeConfigSpecOrReference().withSpec(new ComputeConfigSpec()
                    .withOptions(Arrays.asList(new ComputeConfigSpecOption().withCloudResourceNames(Arrays.asList(
                        "/subscriptions/14AB8053-986C-414E-96B3-11D82B8B478D/resourceGroups/rganyscale/providers/Anyscale.Platform/clouds/testcloud/cloudResources/eastus-cloud-resource"))
                        .withHeadNode(new NodeType().withName("head-node")
                            .withInstanceType("Standard_D8s_v5")
                            .withResources(new LogicalResources().withCpu(8L)
                                .withGpu(0L)
                                .withMemory(32L)
                                .withObjectStoreMemory(8L)
                                .withCustomResources(new LogicalResourcesCustomResources()))
                            .withRequiredResources(new PhysicalResources().withCpu(8L)
                                .withGpu(0L)
                                .withMemory(32L)
                                .withTpu(0L)
                                .withAccelerator("")
                                .withCpuArchitecture(CpuArchitecture.X86_64))
                            .withLabels(new NodeTypeLabels())
                            .withRequiredLabels(new NodeTypeRequiredLabels())
                            .withAdvancedInstanceConfig("{}")
                            .withFlags(new NodeTypeFlags()))
                        .withWorkerNodes(Arrays.asList(new WorkerNode().withName("gpu-worker")
                            .withInstanceType("Standard_NC24ads_A100_v4")
                            .withResources(new LogicalResources().withCpu(24L)
                                .withGpu(1L)
                                .withMemory(220L)
                                .withObjectStoreMemory(50L)
                                .withCustomResources(new LogicalResourcesCustomResources()))
                            .withRequiredResources(new PhysicalResources().withCpu(24L)
                                .withGpu(1L)
                                .withMemory(220L)
                                .withTpu(0L)
                                .withAccelerator("A100")
                                .withCpuArchitecture(CpuArchitecture.X86_64))
                            .withLabels(new NodeTypeLabels())
                            .withRequiredLabels(new NodeTypeRequiredLabels())
                            .withAdvancedInstanceConfig("{}")
                            .withFlags(new NodeTypeFlags())
                            .withMinNodes(1)
                            .withMaxNodes(10)
                            .withMarketType(MarketType.ON_DEMAND)))
                        .withZones(Arrays.asList("eastus-1"))
                        .withAdvancedInstanceConfig("{}")
                        .withFlags(new ComputeConfigSpecOptionFlags()))))
                    .withComputeConfigName(
                        "/subscriptions/14AB8053-986C-414E-96B3-11D82B8B478D/resourceGroups/rganyscale/providers/Anyscale.Platform/clouds/testcloud/computeConfigs/my-compute-config"))
                .withImageUri("anyscale/ray:2.44.0-py312-cu124")
                .withApplications(Arrays.asList(new ServiceDeploymentConfigApplication()))
                .withRolloutStrategy(RolloutStrategy.ROLLOUT)
                .withVersion("v1")
                .withCanaryPercent(0)
                .withMaxSurgePercent(25)
                .withAutoCompleteRollout(true)
                .withTrafficPercent(100)
                .withHttpConfig(new ServiceDeploymentConfigHttpConfig())
                .withGrpcConfig(new ServiceDeploymentConfigGrpcConfig())
                .withMaxUptimeTimeoutSec(0)
                .withEnvVars(new ServiceDeploymentConfigEnvVars())
                .withTracingConfig(new TracingConfig().withEnabled(true).withSamplingRatio(1.0D))
                .withRayGcsExternalStorageConfig(
                    new RayGcsExternalStorageConfig().withAddress("redis://my-redis-host:6379"))
                .withConnectionIds(Arrays.asList("connection-01"))))
            .create();
    }
}
```

### Services_Delete

```java
/**
 * Samples for Services Delete.
 */
public final class ServicesDeleteSamples {
    /*
     * x-ms-original-file: 2026-09-01/Services_Delete.json
     */
    /**
     * Sample code: Services_Delete.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void servicesDelete(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.services()
            .delete("rganyscale", "testcloud", "testproject", "testservice", com.azure.core.util.Context.NONE);
    }
}
```

### Services_Get

```java
/**
 * Samples for Services Get.
 */
public final class ServicesGetSamples {
    /*
     * x-ms-original-file: 2026-09-01/Services_Get.json
     */
    /**
     * Sample code: Services_Get.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void servicesGet(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.services()
            .getWithResponse("rganyscale", "testcloud", "testproject", "testservice", com.azure.core.util.Context.NONE);
    }
}
```

### Services_ListByResourceGroup

```java
/**
 * Samples for Services ListByResourceGroup.
 */
public final class ServicesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-09-01/Services_ListByResourceGroup.json
     */
    /**
     * Sample code: Services_ListByResourceGroup.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void servicesListByResourceGroup(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.services()
            .listByResourceGroup("rganyscale", "testcloud", "testproject", com.azure.core.util.Context.NONE);
    }
}
```

### Services_Update

```java
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpec;
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpecOption;
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpecOptionFlags;
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpecOrReference;
import com.azure.resourcemanager.anyscale.models.CpuArchitecture;
import com.azure.resourcemanager.anyscale.models.LogicalResources;
import com.azure.resourcemanager.anyscale.models.LogicalResourcesCustomResources;
import com.azure.resourcemanager.anyscale.models.MarketType;
import com.azure.resourcemanager.anyscale.models.NodeType;
import com.azure.resourcemanager.anyscale.models.NodeTypeFlags;
import com.azure.resourcemanager.anyscale.models.NodeTypeLabels;
import com.azure.resourcemanager.anyscale.models.NodeTypeRequiredLabels;
import com.azure.resourcemanager.anyscale.models.PhysicalResources;
import com.azure.resourcemanager.anyscale.models.RayGcsExternalStorageConfig;
import com.azure.resourcemanager.anyscale.models.RolloutStrategy;
import com.azure.resourcemanager.anyscale.models.Service;
import com.azure.resourcemanager.anyscale.models.ServiceDeploymentConfig;
import com.azure.resourcemanager.anyscale.models.ServiceDeploymentConfigApplication;
import com.azure.resourcemanager.anyscale.models.ServiceDeploymentConfigEnvVars;
import com.azure.resourcemanager.anyscale.models.ServiceDeploymentConfigGrpcConfig;
import com.azure.resourcemanager.anyscale.models.ServiceDeploymentConfigHttpConfig;
import com.azure.resourcemanager.anyscale.models.ServiceUpdateProperties;
import com.azure.resourcemanager.anyscale.models.TracingConfig;
import com.azure.resourcemanager.anyscale.models.WorkerNode;
import java.util.Arrays;

/**
 * Samples for Services Update.
 */
public final class ServicesUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01/Services_Update.json
     */
    /**
     * Sample code: Services_Update.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void servicesUpdate(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        Service resource = manager.services()
            .getWithResponse("rganyscale", "testcloud", "testproject", "testservice", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new ServiceUpdateProperties().withDeploymentConfig(new ServiceDeploymentConfig()
                .withDescription("Production model serving endpoint")
                .withComputeConfig(new ComputeConfigSpecOrReference().withSpec(new ComputeConfigSpec()
                    .withOptions(Arrays.asList(new ComputeConfigSpecOption().withCloudResourceNames(Arrays.asList(
                        "/subscriptions/14AB8053-986C-414E-96B3-11D82B8B478D/resourceGroups/rganyscale/providers/Anyscale.Platform/clouds/testcloud/cloudResources/eastus-cloud-resource"))
                        .withHeadNode(new NodeType().withName("head-node")
                            .withInstanceType("Standard_D8s_v5")
                            .withResources(new LogicalResources().withCpu(8L)
                                .withGpu(0L)
                                .withMemory(32L)
                                .withObjectStoreMemory(8L)
                                .withCustomResources(new LogicalResourcesCustomResources()))
                            .withRequiredResources(new PhysicalResources().withCpu(8L)
                                .withGpu(0L)
                                .withMemory(32L)
                                .withTpu(0L)
                                .withAccelerator("")
                                .withCpuArchitecture(CpuArchitecture.X86_64))
                            .withLabels(new NodeTypeLabels())
                            .withRequiredLabels(new NodeTypeRequiredLabels())
                            .withAdvancedInstanceConfig("{}")
                            .withFlags(new NodeTypeFlags()))
                        .withWorkerNodes(Arrays.asList(new WorkerNode().withName("gpu-worker")
                            .withInstanceType("Standard_NC24ads_A100_v4")
                            .withResources(new LogicalResources().withCpu(24L)
                                .withGpu(1L)
                                .withMemory(220L)
                                .withObjectStoreMemory(50L)
                                .withCustomResources(new LogicalResourcesCustomResources()))
                            .withRequiredResources(new PhysicalResources().withCpu(24L)
                                .withGpu(1L)
                                .withMemory(220L)
                                .withTpu(0L)
                                .withAccelerator("A100")
                                .withCpuArchitecture(CpuArchitecture.X86_64))
                            .withLabels(new NodeTypeLabels())
                            .withRequiredLabels(new NodeTypeRequiredLabels())
                            .withAdvancedInstanceConfig("{}")
                            .withFlags(new NodeTypeFlags())
                            .withMinNodes(1)
                            .withMaxNodes(10)
                            .withMarketType(MarketType.ON_DEMAND)))
                        .withZones(Arrays.asList("eastus-1"))
                        .withAdvancedInstanceConfig("{}")
                        .withFlags(new ComputeConfigSpecOptionFlags()))))
                    .withComputeConfigName(
                        "/subscriptions/14AB8053-986C-414E-96B3-11D82B8B478D/resourceGroups/rganyscale/providers/Anyscale.Platform/clouds/testcloud/computeConfigs/my-compute-config"))
                .withImageUri("anyscale/ray:2.44.0-py312-cu124")
                .withApplications(Arrays.asList(new ServiceDeploymentConfigApplication()))
                .withRolloutStrategy(RolloutStrategy.ROLLOUT)
                .withVersion("v1")
                .withCanaryPercent(0)
                .withMaxSurgePercent(25)
                .withAutoCompleteRollout(true)
                .withTrafficPercent(100)
                .withHttpConfig(new ServiceDeploymentConfigHttpConfig())
                .withGrpcConfig(new ServiceDeploymentConfigGrpcConfig())
                .withMaxUptimeTimeoutSec(0)
                .withEnvVars(new ServiceDeploymentConfigEnvVars())
                .withTracingConfig(new TracingConfig().withEnabled(true).withSamplingRatio(1.0D))
                .withRayGcsExternalStorageConfig(
                    new RayGcsExternalStorageConfig().withAddress("redis://my-redis-host:6379"))
                .withConnectionIds(Arrays.asList("connection-01"))))
            .apply();
    }
}
```

### Workspaces_CreateOrUpdate

```java
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpec;
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpecOption;
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpecOptionFlags;
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpecOrReference;
import com.azure.resourcemanager.anyscale.models.CpuArchitecture;
import com.azure.resourcemanager.anyscale.models.LogicalResources;
import com.azure.resourcemanager.anyscale.models.LogicalResourcesCustomResources;
import com.azure.resourcemanager.anyscale.models.MarketType;
import com.azure.resourcemanager.anyscale.models.NodeType;
import com.azure.resourcemanager.anyscale.models.NodeTypeFlags;
import com.azure.resourcemanager.anyscale.models.NodeTypeLabels;
import com.azure.resourcemanager.anyscale.models.NodeTypeRequiredLabels;
import com.azure.resourcemanager.anyscale.models.PhysicalResources;
import com.azure.resourcemanager.anyscale.models.WorkerNode;
import com.azure.resourcemanager.anyscale.models.WorkspaceProperties;
import java.util.Arrays;

/**
 * Samples for Workspaces CreateOrUpdate.
 */
public final class WorkspacesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01/Workspaces_CreateOrUpdate.json
     */
    /**
     * Sample code: Workspaces_CreateOrUpdate.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void workspacesCreateOrUpdate(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.workspaces()
            .define("testworkspace")
            .withExistingProject("rganyscale", "testcloud", "testproject")
            .withProperties(new WorkspaceProperties().withComputeConfig(new ComputeConfigSpecOrReference()
                .withSpec(new ComputeConfigSpec().withOptions(Arrays.asList(new ComputeConfigSpecOption()
                    .withCloudResourceNames(Arrays.asList(
                        "/subscriptions/14AB8053-986C-414E-96B3-11D82B8B478D/resourceGroups/rganyscale/providers/Anyscale.Platform/clouds/testcloud/cloudResources/eastus-cloud-resource"))
                    .withHeadNode(new NodeType().withName("head-node")
                        .withInstanceType("Standard_D8s_v5")
                        .withResources(new LogicalResources().withCpu(8L)
                            .withGpu(0L)
                            .withMemory(32L)
                            .withObjectStoreMemory(8L)
                            .withCustomResources(new LogicalResourcesCustomResources()))
                        .withRequiredResources(new PhysicalResources().withCpu(8L)
                            .withGpu(0L)
                            .withMemory(32L)
                            .withTpu(0L)
                            .withAccelerator("")
                            .withCpuArchitecture(CpuArchitecture.X86_64))
                        .withLabels(new NodeTypeLabels())
                        .withRequiredLabels(new NodeTypeRequiredLabels())
                        .withAdvancedInstanceConfig("{}")
                        .withFlags(new NodeTypeFlags()))
                    .withWorkerNodes(Arrays.asList(new WorkerNode().withName("gpu-worker")
                        .withInstanceType("Standard_NC24ads_A100_v4")
                        .withResources(new LogicalResources().withCpu(24L)
                            .withGpu(1L)
                            .withMemory(220L)
                            .withObjectStoreMemory(50L)
                            .withCustomResources(new LogicalResourcesCustomResources()))
                        .withRequiredResources(new PhysicalResources().withCpu(24L)
                            .withGpu(1L)
                            .withMemory(220L)
                            .withTpu(0L)
                            .withAccelerator("A100")
                            .withCpuArchitecture(CpuArchitecture.X86_64))
                        .withLabels(new NodeTypeLabels())
                        .withRequiredLabels(new NodeTypeRequiredLabels())
                        .withAdvancedInstanceConfig("{}")
                        .withFlags(new NodeTypeFlags())
                        .withMinNodes(1)
                        .withMaxNodes(10)
                        .withMarketType(MarketType.ON_DEMAND)))
                    .withZones(Arrays.asList("eastus-1"))
                    .withAdvancedInstanceConfig("{}")
                    .withFlags(new ComputeConfigSpecOptionFlags()))))
                .withComputeConfigName(
                    "/subscriptions/14AB8053-986C-414E-96B3-11D82B8B478D/resourceGroups/rganyscale/providers/Anyscale.Platform/clouds/testcloud/computeConfigs/my-compute-config"))
                .withImageUri("anyscale/ray:2.44.0-py312-cu124")
                .withIdleTerminationMinutes(30))
            .create();
    }
}
```

### Workspaces_Delete

```java
/**
 * Samples for Workspaces Delete.
 */
public final class WorkspacesDeleteSamples {
    /*
     * x-ms-original-file: 2026-09-01/Workspaces_Delete.json
     */
    /**
     * Sample code: Workspaces_Delete.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void workspacesDelete(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.workspaces()
            .delete("rganyscale", "testcloud", "testproject", "testworkspace", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_Get

```java
/**
 * Samples for Workspaces Get.
 */
public final class WorkspacesGetSamples {
    /*
     * x-ms-original-file: 2026-09-01/Workspaces_Get.json
     */
    /**
     * Sample code: Workspaces_Get.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void workspacesGet(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.workspaces()
            .getWithResponse("rganyscale", "testcloud", "testproject", "testworkspace",
                com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_ListByResourceGroup

```java
/**
 * Samples for Workspaces ListByResourceGroup.
 */
public final class WorkspacesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-09-01/Workspaces_ListByResourceGroup.json
     */
    /**
     * Sample code: Workspaces_ListByResourceGroup.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void workspacesListByResourceGroup(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        manager.workspaces()
            .listByResourceGroup("rganyscale", "testcloud", "testproject", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_Update

```java
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpec;
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpecOption;
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpecOptionFlags;
import com.azure.resourcemanager.anyscale.models.ComputeConfigSpecOrReference;
import com.azure.resourcemanager.anyscale.models.CpuArchitecture;
import com.azure.resourcemanager.anyscale.models.LogicalResources;
import com.azure.resourcemanager.anyscale.models.LogicalResourcesCustomResources;
import com.azure.resourcemanager.anyscale.models.MarketType;
import com.azure.resourcemanager.anyscale.models.NodeType;
import com.azure.resourcemanager.anyscale.models.NodeTypeFlags;
import com.azure.resourcemanager.anyscale.models.NodeTypeLabels;
import com.azure.resourcemanager.anyscale.models.NodeTypeRequiredLabels;
import com.azure.resourcemanager.anyscale.models.PhysicalResources;
import com.azure.resourcemanager.anyscale.models.WorkerNode;
import com.azure.resourcemanager.anyscale.models.Workspace;
import com.azure.resourcemanager.anyscale.models.WorkspaceUpdateProperties;
import java.util.Arrays;

/**
 * Samples for Workspaces Update.
 */
public final class WorkspacesUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-01/Workspaces_Update.json
     */
    /**
     * Sample code: Workspaces_Update.
     * 
     * @param manager Entry point to AnyscaleManager.
     */
    public static void workspacesUpdate(com.azure.resourcemanager.anyscale.AnyscaleManager manager) {
        Workspace resource = manager.workspaces()
            .getWithResponse("rganyscale", "testcloud", "testproject", "testworkspace",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new WorkspaceUpdateProperties().withComputeConfig(new ComputeConfigSpecOrReference()
                .withSpec(new ComputeConfigSpec().withOptions(Arrays.asList(new ComputeConfigSpecOption()
                    .withCloudResourceNames(Arrays.asList(
                        "/subscriptions/14AB8053-986C-414E-96B3-11D82B8B478D/resourceGroups/rganyscale/providers/Anyscale.Platform/clouds/testcloud/cloudResources/eastus-cloud-resource"))
                    .withHeadNode(new NodeType().withName("head-node")
                        .withInstanceType("Standard_D8s_v5")
                        .withResources(new LogicalResources().withCpu(8L)
                            .withGpu(0L)
                            .withMemory(32L)
                            .withObjectStoreMemory(8L)
                            .withCustomResources(new LogicalResourcesCustomResources()))
                        .withRequiredResources(new PhysicalResources().withCpu(8L)
                            .withGpu(0L)
                            .withMemory(32L)
                            .withTpu(0L)
                            .withAccelerator("")
                            .withCpuArchitecture(CpuArchitecture.X86_64))
                        .withLabels(new NodeTypeLabels())
                        .withRequiredLabels(new NodeTypeRequiredLabels())
                        .withAdvancedInstanceConfig("{}")
                        .withFlags(new NodeTypeFlags()))
                    .withWorkerNodes(Arrays.asList(new WorkerNode().withName("gpu-worker")
                        .withInstanceType("Standard_NC24ads_A100_v4")
                        .withResources(new LogicalResources().withCpu(24L)
                            .withGpu(1L)
                            .withMemory(220L)
                            .withObjectStoreMemory(50L)
                            .withCustomResources(new LogicalResourcesCustomResources()))
                        .withRequiredResources(new PhysicalResources().withCpu(24L)
                            .withGpu(1L)
                            .withMemory(220L)
                            .withTpu(0L)
                            .withAccelerator("A100")
                            .withCpuArchitecture(CpuArchitecture.X86_64))
                        .withLabels(new NodeTypeLabels())
                        .withRequiredLabels(new NodeTypeRequiredLabels())
                        .withAdvancedInstanceConfig("{}")
                        .withFlags(new NodeTypeFlags())
                        .withMinNodes(1)
                        .withMaxNodes(10)
                        .withMarketType(MarketType.ON_DEMAND)))
                    .withZones(Arrays.asList("eastus-1"))
                    .withAdvancedInstanceConfig("{}")
                    .withFlags(new ComputeConfigSpecOptionFlags()))))
                .withComputeConfigName(
                    "/subscriptions/14AB8053-986C-414E-96B3-11D82B8B478D/resourceGroups/rganyscale/providers/Anyscale.Platform/clouds/testcloud/computeConfigs/my-compute-config"))
                .withImageUri("anyscale/ray:2.44.0-py312-cu124")
                .withIdleTerminationMinutes(30))
            .apply();
    }
}
```

