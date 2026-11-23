# Code snippets and samples


## Operations

- [List](#operations_list)

## Recommendations

- [RecommendVmSku](#recommendations_recommendvmsku)
### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2026-06-01-preview/Operations_List.json
     */
    /**
     * Sample code: List the operations for the Microsoft.AIInfraOptimization resource provider.
     * 
     * @param manager Entry point to AIInfraOptimizationManager.
     */
    public static void listTheOperationsForTheMicrosoftAIInfraOptimizationResourceProvider(
        com.azure.resourcemanager.aiinfraoptimization.AIInfraOptimizationManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Recommendations_RecommendVmSku

```java
import com.azure.resourcemanager.aiinfraoptimization.models.ProcurementOption;
import com.azure.resourcemanager.aiinfraoptimization.models.RankingPreference;
import com.azure.resourcemanager.aiinfraoptimization.models.RecommendVmSkuDeployment;
import com.azure.resourcemanager.aiinfraoptimization.models.RecommendVmSkuHourlyCost;
import com.azure.resourcemanager.aiinfraoptimization.models.RecommendVmSkuModel;
import com.azure.resourcemanager.aiinfraoptimization.models.RecommendVmSkuOptimization;
import com.azure.resourcemanager.aiinfraoptimization.models.RecommendVmSkuRequest;
import com.azure.resourcemanager.aiinfraoptimization.models.RecommendVmSkuSubscriptionOptions;
import com.azure.resourcemanager.aiinfraoptimization.models.RecommendVmSkuTokenBudget;
import com.azure.resourcemanager.aiinfraoptimization.models.RecommendVmSkuWorkload;
import com.azure.resourcemanager.aiinfraoptimization.models.WeightPrecision;
import java.util.Arrays;

/**
 * Samples for Recommendations RecommendVmSku.
 */
public final class RecommendationsRecommendVmSkuSamples {
    /*
     * x-ms-original-file: 2026-06-01-preview/Recommendations_RecommendVmSku.json
     */
    /**
     * Sample code: Recommend virtual machine sizes for an AI inference workload.
     * 
     * @param manager Entry point to AIInfraOptimizationManager.
     */
    public static void recommendVirtualMachineSizesForAnAIInferenceWorkload(
        com.azure.resourcemanager.aiinfraoptimization.AIInfraOptimizationManager manager) {
        manager.recommendations()
            .recommendVmSkuWithResponse("eastus2", new RecommendVmSkuRequest()
                .withModel(new RecommendVmSkuModel().withHuggingFaceModelId("meta-llama/Llama-3.1-70B-Instruct")
                    .withParameterCount(70000000000L)
                    .withWeightPrecision(WeightPrecision.FP16)
                    .withNumLayers(80)
                    .withNumKeyValueHeads(8)
                    .withHeadDim(128)
                    .withMaxContextLength(131072))
                .withWorkload(new RecommendVmSkuWorkload().withMaxConcurrentRequestsPerReplica(1)
                    .withTokenBudget(new RecommendVmSkuTokenBudget().withPromptTokens(8000).withMaxOutputTokens(1024)))
                .withDeployment(new RecommendVmSkuDeployment().withReplicaCount(1)
                    .withProcurementOption(ProcurementOption.ON_DEMAND))
                .withOptimization(new RecommendVmSkuOptimization().withRankingPreference(RankingPreference.COST)
                    .withMaxDeploymentCostPerHour(
                        new RecommendVmSkuHourlyCost().withAmount(50.0D).withCurrencyCode("fakeTokenPlaceholder")))
                .withTargetVmSizes(Arrays.asList("Standard_NC48ads_A100_v4", "Standard_ND96asr_v4"))
                .withSubscriptionOptions(new RecommendVmSkuSubscriptionOptions().withIncludeQuota(true)
                    .withIncludePlacement(true)
                    .withIncludePricing(true)),
                com.azure.core.util.Context.NONE);
    }
}
```

