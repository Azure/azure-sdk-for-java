# Code snippets and samples


## AIManagerNamespaces

- [CreateOrUpdate](#aimanagernamespaces_createorupdate)
- [Delete](#aimanagernamespaces_delete)
- [Get](#aimanagernamespaces_get)
- [ListAccessKeys](#aimanagernamespaces_listaccesskeys)
- [ListByAIManager](#aimanagernamespaces_listbyaimanager)
- [ListCredential](#aimanagernamespaces_listcredential)
- [RotateKeys](#aimanagernamespaces_rotatekeys)

## AIManagers

- [CreateOrUpdate](#aimanagers_createorupdate)
- [Delete](#aimanagers_delete)
- [GetByResourceGroup](#aimanagers_getbyresourcegroup)
- [List](#aimanagers_list)
- [ListByResourceGroup](#aimanagers_listbyresourcegroup)
- [ListCredential](#aimanagers_listcredential)
- [Update](#aimanagers_update)

## AIModels

- [CalculateCost](#aimodels_calculatecost)
- [Get](#aimodels_get)
- [List](#aimodels_list)

## ModelDeployments

- [CreateOrUpdate](#modeldeployments_createorupdate)
- [Delete](#modeldeployments_delete)
- [Get](#modeldeployments_get)
- [ListByAIManagerNamespace](#modeldeployments_listbyaimanagernamespace)

## ModelSources

- [CreateOrUpdate](#modelsources_createorupdate)
- [Delete](#modelsources_delete)
- [Get](#modelsources_get)
- [List](#modelsources_list)

## Operations

- [List](#operations_list)
### AIManagerNamespaces_CreateOrUpdate

```java
import com.azure.resourcemanager.containerserviceaimanager.models.AIManagerNamespaceProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AIManagerNamespaces CreateOrUpdate.
 */
public final class AIManagerNamespacesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-05-02-preview/AIManagerNamespaces_CreateOrUpdate.json
     */
    /**
     * Sample code: Creates or updates an AI Manager namespace resource.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void createsOrUpdatesAnAIManagerNamespaceResource(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.aIManagerNamespaces()
            .define("namespace1")
            .withExistingAiManager("rg1", "aimanager1")
            .withProperties(new AIManagerNamespaceProperties().withLabels(mapOf("app", "myapp"))
                .withAnnotations(mapOf("note", "example")))
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

### AIManagerNamespaces_Delete

```java
/**
 * Samples for AIManagerNamespaces Delete.
 */
public final class AIManagerNamespacesDeleteSamples {
    /*
     * x-ms-original-file: 2026-05-02-preview/AIManagerNamespaces_Delete.json
     */
    /**
     * Sample code: Deletes an AI Manager namespace resource.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void deletesAnAIManagerNamespaceResource(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.aIManagerNamespaces().delete("rg1", "aimanager1", "namespace1", null, com.azure.core.util.Context.NONE);
    }
}
```

### AIManagerNamespaces_Get

```java
/**
 * Samples for AIManagerNamespaces Get.
 */
public final class AIManagerNamespacesGetSamples {
    /*
     * x-ms-original-file: 2026-05-02-preview/AIManagerNamespaces_Get.json
     */
    /**
     * Sample code: Gets an AI Manager namespace resource.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void getsAnAIManagerNamespaceResource(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.aIManagerNamespaces()
            .getWithResponse("rg1", "aimanager1", "namespace1", com.azure.core.util.Context.NONE);
    }
}
```

### AIManagerNamespaces_ListAccessKeys

```java
/**
 * Samples for AIManagerNamespaces ListAccessKeys.
 */
public final class AIManagerNamespacesListAccessKeysSamples {
    /*
     * x-ms-original-file: 2026-05-02-preview/AIManagerNamespaces_ListAccessKeys.json
     */
    /**
     * Sample code: AIManagerNamespaces_ListAccessKeys_MaximumSet.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void aIManagerNamespacesListAccessKeysMaximumSet(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.aIManagerNamespaces()
            .listAccessKeysWithResponse("rgaimanagers", "aimanager1", "namespace-1", com.azure.core.util.Context.NONE);
    }
}
```

### AIManagerNamespaces_ListByAIManager

```java
/**
 * Samples for AIManagerNamespaces ListByAIManager.
 */
public final class AIManagerNamespacesListByAIManagerSamples {
    /*
     * x-ms-original-file: 2026-05-02-preview/AIManagerNamespaces_ListByAIManager.json
     */
    /**
     * Sample code: Lists AI Manager namespace resources by AI Manager.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void listsAIManagerNamespaceResourcesByAIManager(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.aIManagerNamespaces().listByAIManager("rg1", "aimanager1", com.azure.core.util.Context.NONE);
    }
}
```

### AIManagerNamespaces_ListCredential

```java
/**
 * Samples for AIManagerNamespaces ListCredential.
 */
public final class AIManagerNamespacesListCredentialSamples {
    /*
     * x-ms-original-file: 2026-05-02-preview/AIManagerNamespaces_ListCredential.json
     */
    /**
     * Sample code: Lists the credentials of an AI Manager namespace.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void listsTheCredentialsOfAnAIManagerNamespace(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.aIManagerNamespaces()
            .listCredentialWithResponse("rg1", "aimanager1", "namespace1", com.azure.core.util.Context.NONE);
    }
}
```

### AIManagerNamespaces_RotateKeys

```java
/**
 * Samples for AIManagerNamespaces RotateKeys.
 */
public final class AIManagerNamespacesRotateKeysSamples {
    /*
     * x-ms-original-file: 2026-05-02-preview/AIManagerNamespaces_RotateKeys.json
     */
    /**
     * Sample code: AIManagerNamespaces_RotateKeys.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void aIManagerNamespacesRotateKeys(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.aIManagerNamespaces()
            .rotateKeysWithResponse("rgaimanagers", "aimanager1", "namespace-1", com.azure.core.util.Context.NONE);
    }
}
```

### AIManagers_CreateOrUpdate

```java
import com.azure.resourcemanager.containerserviceaimanager.models.AIManagerProperties;
import com.azure.resourcemanager.containerserviceaimanager.models.DeletePolicy;
import com.azure.resourcemanager.containerserviceaimanager.models.ManagedServiceIdentity;
import com.azure.resourcemanager.containerserviceaimanager.models.ManagedServiceIdentityType;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AIManagers CreateOrUpdate.
 */
public final class AIManagersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-05-02-preview/AIManagers_CreateOrUpdate.json
     */
    /**
     * Sample code: Creates or updates an AI Manager resource.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void createsOrUpdatesAnAIManagerResource(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.aIManagers()
            .define("aimanager1")
            .withRegion("eastus")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
            .withProperties(new AIManagerProperties().withDeletePolicy(DeletePolicy.KEEP))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
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

### AIManagers_Delete

```java
/**
 * Samples for AIManagers Delete.
 */
public final class AIManagersDeleteSamples {
    /*
     * x-ms-original-file: 2026-05-02-preview/AIManagers_Delete.json
     */
    /**
     * Sample code: Deletes an AI Manager resource.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void deletesAnAIManagerResource(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.aIManagers().delete("rg1", "aimanager1", null, com.azure.core.util.Context.NONE);
    }
}
```

### AIManagers_GetByResourceGroup

```java
/**
 * Samples for AIManagers GetByResourceGroup.
 */
public final class AIManagersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-05-02-preview/AIManagers_Get.json
     */
    /**
     * Sample code: Gets an AI Manager resource.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void getsAnAIManagerResource(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.aIManagers().getByResourceGroupWithResponse("rg1", "aimanager1", com.azure.core.util.Context.NONE);
    }
}
```

### AIManagers_List

```java
/**
 * Samples for AIManagers List.
 */
public final class AIManagersListSamples {
    /*
     * x-ms-original-file: 2026-05-02-preview/AIManagers_ListBySubscription.json
     */
    /**
     * Sample code: Lists AI Manager resources by subscription.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void listsAIManagerResourcesBySubscription(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.aIManagers().list(com.azure.core.util.Context.NONE);
    }
}
```

### AIManagers_ListByResourceGroup

```java
/**
 * Samples for AIManagers ListByResourceGroup.
 */
public final class AIManagersListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-05-02-preview/AIManagers_ListByResourceGroup.json
     */
    /**
     * Sample code: Lists AI Manager resources by resource group.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void listsAIManagerResourcesByResourceGroup(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.aIManagers().listByResourceGroup("rg1", com.azure.core.util.Context.NONE);
    }
}
```

### AIManagers_ListCredential

```java
/**
 * Samples for AIManagers ListCredential.
 */
public final class AIManagersListCredentialSamples {
    /*
     * x-ms-original-file: 2026-05-02-preview/AIManagers_ListCredential.json
     */
    /**
     * Sample code: Lists the credentials of an AI Manager.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void listsTheCredentialsOfAnAIManager(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.aIManagers().listCredentialWithResponse("rg1", "aimanager1", com.azure.core.util.Context.NONE);
    }
}
```

### AIManagers_Update

```java
import com.azure.resourcemanager.containerserviceaimanager.models.AIManager;
import com.azure.resourcemanager.containerserviceaimanager.models.ManagedServiceIdentity;
import com.azure.resourcemanager.containerserviceaimanager.models.ManagedServiceIdentityType;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for AIManagers Update.
 */
public final class AIManagersUpdateSamples {
    /*
     * x-ms-original-file: 2026-05-02-preview/AIManagers_Update.json
     */
    /**
     * Sample code: Updates an AI Manager resource.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void updatesAnAIManagerResource(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        AIManager resource = manager.aIManagers()
            .getByResourceGroupWithResponse("rg1", "aimanager1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key1", "fakeTokenPlaceholder", "key2", "fakeTokenPlaceholder"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
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

### AIModels_CalculateCost

```java
import com.azure.resourcemanager.containerserviceaimanager.models.CalculateCostRequest;

/**
 * Samples for AIModels CalculateCost.
 */
public final class AIModelsCalculateCostSamples {
    /*
     * x-ms-original-file: 2026-05-02-preview/AIModels_CalculateCost.json
     */
    /**
     * Sample code: AIModels_CalculateCost_MaximumSet.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void aIModelsCalculateCostMaximumSet(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.aIModels()
            .calculateCostWithResponse("eastus", "9806f0c862fdd920", new CalculateCostRequest(),
                com.azure.core.util.Context.NONE);
    }
}
```

### AIModels_Get

```java
/**
 * Samples for AIModels Get.
 */
public final class AIModelsGetSamples {
    /*
     * x-ms-original-file: 2026-05-02-preview/AIModels_Get.json
     */
    /**
     * Sample code: AIModels_Get_MaximumSet.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void aIModelsGetMaximumSet(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.aIModels().getWithResponse("eastus", "9806f0c862fdd920", com.azure.core.util.Context.NONE);
    }
}
```

### AIModels_List

```java
/**
 * Samples for AIModels List.
 */
public final class AIModelsListSamples {
    /*
     * x-ms-original-file: 2026-05-02-preview/AIModels_List.json
     */
    /**
     * Sample code: AIModels_List_MaximumSet.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void aIModelsListMaximumSet(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.aIModels().list("eastus", com.azure.core.util.Context.NONE);
    }
}
```

### ModelDeployments_CreateOrUpdate

```java
import com.azure.resourcemanager.containerserviceaimanager.models.AutoscaleProfile;
import com.azure.resourcemanager.containerserviceaimanager.models.ModelDeploymentPerformanceMode;
import com.azure.resourcemanager.containerserviceaimanager.models.ModelDeploymentProperties;
import com.azure.resourcemanager.containerserviceaimanager.models.ScalingProfile;

/**
 * Samples for ModelDeployments CreateOrUpdate.
 */
public final class ModelDeploymentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-05-02-preview/ModelDeployments_CreateOrUpdate.json
     */
    /**
     * Sample code: ModelDeployments_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void modelDeploymentsCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.modelDeployments()
            .define("deployment-1")
            .withExistingNamespace("rgaimanagers", "aimanager1", "namespace-1")
            .withProperties(new ModelDeploymentProperties().withModelResourceId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rgaimanagers/providers/Microsoft.ContainerService/aiModels/9806f0c862fdd920")
                .withModelSourceResourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rgaimanagers/providers/Microsoft.ContainerService/aiManagers/aimanager1/modelSources/huggingface")
                .withPerformanceMode(ModelDeploymentPerformanceMode.BALANCED)
                .withVmSize("Standard_NC96ads_A100_v4")
                .withScale(
                    new ScalingProfile().withAutoscale(new AutoscaleProfile().withMinReplicas(2).withMaxReplicas(8))))
            .withIfMatch("\"abc123def456\"")
            .withIfNoneMatch("*")
            .create();
    }
}
```

### ModelDeployments_Delete

```java
/**
 * Samples for ModelDeployments Delete.
 */
public final class ModelDeploymentsDeleteSamples {
    /*
     * x-ms-original-file: 2026-05-02-preview/ModelDeployments_Delete.json
     */
    /**
     * Sample code: ModelDeployments_Delete_MaximumSet.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void modelDeploymentsDeleteMaximumSet(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.modelDeployments()
            .delete("rgaimanagers", "aimanager1", "namespace-1", "deployment-1", "\"abc123def456\"",
                com.azure.core.util.Context.NONE);
    }
}
```

### ModelDeployments_Get

```java
/**
 * Samples for ModelDeployments Get.
 */
public final class ModelDeploymentsGetSamples {
    /*
     * x-ms-original-file: 2026-05-02-preview/ModelDeployments_Get.json
     */
    /**
     * Sample code: ModelDeployments_Get_MaximumSet.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void modelDeploymentsGetMaximumSet(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.modelDeployments()
            .getWithResponse("rgaimanagers", "aimanager1", "namespace-1", "deployment-1",
                com.azure.core.util.Context.NONE);
    }
}
```

### ModelDeployments_ListByAIManagerNamespace

```java
/**
 * Samples for ModelDeployments ListByAIManagerNamespace.
 */
public final class ModelDeploymentsListByAIManagerNamespaceSamples {
    /*
     * x-ms-original-file: 2026-05-02-preview/ModelDeployments_ListByAIManagerNamespace.json
     */
    /**
     * Sample code: ModelDeployments_ListByAIManagerNamespace_MaximumSet.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void modelDeploymentsListByAIManagerNamespaceMaximumSet(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.modelDeployments()
            .listByAIManagerNamespace("rgaimanagers", "aimanager1", "namespace-1", com.azure.core.util.Context.NONE);
    }
}
```

### ModelSources_CreateOrUpdate

```java
import com.azure.resourcemanager.containerserviceaimanager.models.CredentialValue;
import com.azure.resourcemanager.containerserviceaimanager.models.InlineCredential;
import com.azure.resourcemanager.containerserviceaimanager.models.ModelSourceProperties;
import com.azure.resourcemanager.containerserviceaimanager.models.ModelSourceType;

/**
 * Samples for ModelSources CreateOrUpdate.
 */
public final class ModelSourcesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-05-02-preview/ModelSources_CreateOrUpdate.json
     */
    /**
     * Sample code: ModelSources_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void modelSourcesCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.modelSources()
            .define("huggingface")
            .withExistingAiManager("rgaimanagers", "aimanager1")
            .withProperties(new ModelSourceProperties().withSourceType(ModelSourceType.HUGGING_FACE)
                .withDescription("Hugging Face model source")
                .withCredential(new CredentialValue()
                    .withInline(new InlineCredential().withValue("hf_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"))))
            .withIfMatch("\"00000000-0000-0000-0000-000000000000\"")
            .withIfNoneMatch("*")
            .create();
    }
}
```

### ModelSources_Delete

```java
/**
 * Samples for ModelSources Delete.
 */
public final class ModelSourcesDeleteSamples {
    /*
     * x-ms-original-file: 2026-05-02-preview/ModelSources_Delete.json
     */
    /**
     * Sample code: ModelSources_Delete_MaximumSet.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void modelSourcesDeleteMaximumSet(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.modelSources()
            .delete("rgaimanagers", "aimanager1", "huggingface", "\"abc123def456\"", com.azure.core.util.Context.NONE);
    }
}
```

### ModelSources_Get

```java
/**
 * Samples for ModelSources Get.
 */
public final class ModelSourcesGetSamples {
    /*
     * x-ms-original-file: 2026-05-02-preview/ModelSources_Get.json
     */
    /**
     * Sample code: ModelSources_Get_MaximumSet.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void modelSourcesGetMaximumSet(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.modelSources()
            .getWithResponse("rgaimanagers", "aimanager1", "huggingface", com.azure.core.util.Context.NONE);
    }
}
```

### ModelSources_List

```java
/**
 * Samples for ModelSources List.
 */
public final class ModelSourcesListSamples {
    /*
     * x-ms-original-file: 2026-05-02-preview/ModelSources_List.json
     */
    /**
     * Sample code: ModelSources_List_MaximumSet.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void modelSourcesListMaximumSet(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.modelSources().list("rgaimanagers", "aimanager1", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-05-02-preview/Operations_List.json
     */
    /**
     * Sample code: List the operations for the provider.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void listTheOperationsForTheProvider(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

