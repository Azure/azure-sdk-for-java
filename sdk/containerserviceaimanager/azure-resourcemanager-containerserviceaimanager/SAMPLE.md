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

## CustomAIModels

- [CalculateCost](#customaimodels_calculatecost)
- [CreateOrUpdate](#customaimodels_createorupdate)
- [Delete](#customaimodels_delete)
- [Get](#customaimodels_get)
- [List](#customaimodels_list)

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
     * x-ms-original-file: 2026-09-02-preview/AIManagerNamespaces_CreateOrUpdate.json
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
     * x-ms-original-file: 2026-09-02-preview/AIManagerNamespaces_Delete.json
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
     * x-ms-original-file: 2026-09-02-preview/AIManagerNamespaces_Get.json
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
     * x-ms-original-file: 2026-09-02-preview/AIManagerNamespaces_ListAccessKeys.json
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
     * x-ms-original-file: 2026-09-02-preview/AIManagerNamespaces_ListByAIManager.json
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
     * x-ms-original-file: 2026-09-02-preview/AIManagerNamespaces_ListCredential.json
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
     * x-ms-original-file: 2026-09-02-preview/AIManagerNamespaces_RotateKeys.json
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
     * x-ms-original-file: 2026-09-02-preview/AIManagers_CreateOrUpdate.json
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

    /*
     * x-ms-original-file: 2026-09-02-preview/AIManagers_CreateOrUpdate_BYO.json
     */
    /**
     * Sample code: Creates or updates an AI Manager resource attached to an existing AKS cluster (bring-your-own).
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void createsOrUpdatesAnAIManagerResourceAttachedToAnExistingAKSClusterBringYourOwn(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.aIManagers()
            .define("aimanager1")
            .withRegion("eastus")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
            .withProperties(new AIManagerProperties().withDeletePolicy(DeletePolicy.KEEP)
                .withClusterResourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg1/providers/Microsoft.ContainerService/managedClusters/existing-aks"))
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
     * x-ms-original-file: 2026-09-02-preview/AIManagers_Delete.json
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
     * x-ms-original-file: 2026-09-02-preview/AIManagers_Get_BYO.json
     */
    /**
     * Sample code: Gets an AI Manager resource attached to an existing AKS cluster (bring-your-own).
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void getsAnAIManagerResourceAttachedToAnExistingAKSClusterBringYourOwn(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.aIManagers().getByResourceGroupWithResponse("rg1", "aimanager1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-09-02-preview/AIManagers_Get.json
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
     * x-ms-original-file: 2026-09-02-preview/AIManagers_ListBySubscription.json
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
     * x-ms-original-file: 2026-09-02-preview/AIManagers_ListByResourceGroup.json
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
     * x-ms-original-file: 2026-09-02-preview/AIManagers_ListCredential.json
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
     * x-ms-original-file: 2026-09-02-preview/AIManagers_Update.json
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
/**
 * Samples for AIModels CalculateCost.
 */
public final class AIModelsCalculateCostSamples {
    /*
     * x-ms-original-file: 2026-09-02-preview/AIModels_CalculateCost.json
     */
    /**
     * Sample code: AIModels_CalculateCost_MaximumSet.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void aIModelsCalculateCostMaximumSet(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.aIModels().calculateCostWithResponse("eastus", "9806f0c862fdd920", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-09-02-preview/AIModels_Get.json
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
     * x-ms-original-file: 2026-09-02-preview/AIModels_List.json
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

### CustomAIModels_CalculateCost

```java
/**
 * Samples for CustomAIModels CalculateCost.
 */
public final class CustomAIModelsCalculateCostSamples {
    /*
     * x-ms-original-file: 2026-09-02-preview/CustomAIModels_CalculateCost.json
     */
    /**
     * Sample code: CustomAIModels_CalculateCost.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void customAIModelsCalculateCost(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.customAIModels()
            .calculateCostWithResponse("rg1", "aimanager1", "custom-model1", com.azure.core.util.Context.NONE);
    }
}
```

### CustomAIModels_CreateOrUpdate

```java
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.containerserviceaimanager.models.BaseModelReference;
import com.azure.resourcemanager.containerserviceaimanager.models.CustomAIModelProperties;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for CustomAIModels CreateOrUpdate.
 */
public final class CustomAIModelsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-02-preview/CustomAIModels_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or update a CustomAIModel.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void createOrUpdateACustomAIModel(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.customAIModels()
            .define("custom-model1")
            .withExistingAiManager("rg1", "aimanager1")
            .withProperties(new CustomAIModelProperties().withModelId("Qwen/Qwen36-27B-private")
                .withBaseModel(new BaseModelReference().withId("Qwen/Qwen3.6-27B")
                    .withTotalWeightSizeBytes(64700000000L)
                    .withConfig(mapOf("architectures",
                        BinaryData.fromBytes("[Qwen3_5ForConditionalGeneration]".getBytes(StandardCharsets.UTF_8)),
                        "image_token_id", BinaryData.fromBytes("248056".getBytes(StandardCharsets.UTF_8)),
                        "language_model_only", BinaryData.fromBytes("false".getBytes(StandardCharsets.UTF_8)),
                        "model_type", BinaryData.fromBytes("qwen3_5".getBytes(StandardCharsets.UTF_8)), "text_config",
                        BinaryData.fromBytes(
                            "{attention_bias=false, attention_dropout=0, attn_output_gate=true, bos_token_id=248044, dtype=bfloat16, eos_token_id=248044, full_attention_interval=4, head_dim=256, hidden_act=silu, hidden_size=5120, initializer_range=0.02, intermediate_size=17408, layer_types=[linear_attention, linear_attention, ..., full_attention], linear_conv_kernel_dim=4, linear_key_head_dim=128, linear_num_key_heads=16, linear_num_value_heads=48, linear_value_head_dim=128, mamba_ssm_dtype=float32, max_position_embeddings=262144, model_type=qwen3_5_text, mtp_num_hidden_layers=1, mtp_use_dedicated_embeddings=false, num_attention_heads=24, num_hidden_layers=64, num_key_value_heads=4, output_gate_type=swish, pad_token_id=null, partial_rotary_factor=0.25, rms_norm_eps=1.0E-6, rope_parameters={mrope_interleaved=true, mrope_section=[11, 11, 10], partial_rotary_factor=0.25, rope_theta=10000000, rope_type=default}, tie_word_embeddings=false, use_cache=true, vocab_size=248320}"
                                .getBytes(StandardCharsets.UTF_8)),
                        "tie_word_embeddings", BinaryData.fromBytes("false".getBytes(StandardCharsets.UTF_8)),
                        "transformers_version", BinaryData.fromBytes("4.57.1".getBytes(StandardCharsets.UTF_8)),
                        "video_token_id", BinaryData.fromBytes("248057".getBytes(StandardCharsets.UTF_8)),
                        "vision_config",
                        BinaryData.fromBytes(
                            "{deepstack_visual_indexes=[], depth=27, hidden_act=gelu_pytorch_tanh, hidden_size=1152, in_channels=3, initializer_range=0.02, intermediate_size=4304, model_type=qwen3_5, num_heads=16, num_position_embeddings=2304, out_hidden_size=5120, patch_size=16, spatial_merge_size=2, temporal_patch_size=2}"
                                .getBytes(StandardCharsets.UTF_8)),
                        "vision_end_token_id", BinaryData.fromBytes("248054".getBytes(StandardCharsets.UTF_8)),
                        "vision_start_token_id", BinaryData.fromBytes("248053".getBytes(StandardCharsets.UTF_8)))))
                .withModelSourceResourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg1/providers/Microsoft.ContainerService/aiManagers/aimanager1/modelSources/foundry-private-source")
                .withDescription("Custom Llama 2 7B model for our organization"))
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

### CustomAIModels_Delete

```java
/**
 * Samples for CustomAIModels Delete.
 */
public final class CustomAIModelsDeleteSamples {
    /*
     * x-ms-original-file: 2026-09-02-preview/CustomAIModels_Delete.json
     */
    /**
     * Sample code: Delete a CustomAIModel.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void deleteACustomAIModel(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.customAIModels().delete("rg1", "aimanager1", "custom-model1", null, com.azure.core.util.Context.NONE);
    }
}
```

### CustomAIModels_Get

```java
/**
 * Samples for CustomAIModels Get.
 */
public final class CustomAIModelsGetSamples {
    /*
     * x-ms-original-file: 2026-09-02-preview/CustomAIModels_Get.json
     */
    /**
     * Sample code: Get a CustomAIModel.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void getACustomAIModel(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.customAIModels()
            .getWithResponse("rg1", "aimanager1", "custom-model1", com.azure.core.util.Context.NONE);
    }
}
```

### CustomAIModels_List

```java
/**
 * Samples for CustomAIModels List.
 */
public final class CustomAIModelsListSamples {
    /*
     * x-ms-original-file: 2026-09-02-preview/CustomAIModels_List.json
     */
    /**
     * Sample code: List CustomAIModel resources by AIManager.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void listCustomAIModelResourcesByAIManager(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.customAIModels().list("rg1", "aimanager1", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-09-02-preview/ModelDeployments_CreateOrUpdate.json
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
                "/subscriptions/00000000-0000-0000-0000-000000000000/providers/Microsoft.ContainerService/locations/eastus/aiModels/9806f0c862fdd920")
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
     * x-ms-original-file: 2026-09-02-preview/ModelDeployments_Delete.json
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
     * x-ms-original-file: 2026-09-02-preview/ModelDeployments_Get.json
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
     * x-ms-original-file: 2026-09-02-preview/ModelDeployments_ListByAIManagerNamespace.json
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
import com.azure.resourcemanager.containerserviceaimanager.models.ManagedIdentityCredential;
import com.azure.resourcemanager.containerserviceaimanager.models.MicrosoftFoundrySource;
import com.azure.resourcemanager.containerserviceaimanager.models.ModelSourceProperties;
import com.azure.resourcemanager.containerserviceaimanager.models.ModelSourceType;

/**
 * Samples for ModelSources CreateOrUpdate.
 */
public final class ModelSourcesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-09-02-preview/ModelSources_CreateOrUpdate.json
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

    /*
     * x-ms-original-file: 2026-09-02-preview/ModelSources_CreateOrUpdate_ManagedIdentity.json
     */
    /**
     * Sample code: ModelSources_CreateOrUpdate_ManagedIdentity.
     * 
     * @param manager Entry point to ContainerServiceAIManagerManager.
     */
    public static void modelSourcesCreateOrUpdateManagedIdentity(
        com.azure.resourcemanager.containerserviceaimanager.ContainerServiceAIManagerManager manager) {
        manager.modelSources()
            .define("foundry")
            .withExistingAiManager("rgaimanagers", "aimanager1")
            .withProperties(new ModelSourceProperties().withSourceType(ModelSourceType.MICROSOFT_FOUNDRY)
                .withDescription("Foundry model source")
                .withCredential(
                    new CredentialValue().withManagedIdentity(new ManagedIdentityCredential().withResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testrg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/mytestidentity")))
                .withMicrosoftFoundry(new MicrosoftFoundrySource().withProjectResourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/testrg/providers/Microsoft.CognitiveServices/accounts/test-account/projects/test-model-project")))
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
     * x-ms-original-file: 2026-09-02-preview/ModelSources_Delete.json
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
     * x-ms-original-file: 2026-09-02-preview/ModelSources_Get.json
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
     * x-ms-original-file: 2026-09-02-preview/ModelSources_List.json
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
     * x-ms-original-file: 2026-09-02-preview/Operations_List.json
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

