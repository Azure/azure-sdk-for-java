# Code snippets and samples


## Operations

- [List](#operations_list)

## SkuMixPlacementScores

- [Get](#skumixplacementscores_get)
- [Post](#skumixplacementscores_post)

## SpotPlacementScores

- [Get](#spotplacementscores_get)
- [Post](#spotplacementscores_post)
### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2026-09-05-preview/Operations_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MinimumSet_Gen.
     * 
     * @param manager Entry point to ComputeRecommenderManager.
     */
    public static void
        operationsListMinimumSetGen(com.azure.resourcemanager.compute.recommender.ComputeRecommenderManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-09-05-preview/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MaximumSet_Gen.
     * 
     * @param manager Entry point to ComputeRecommenderManager.
     */
    public static void
        operationsListMaximumSetGen(com.azure.resourcemanager.compute.recommender.ComputeRecommenderManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### SkuMixPlacementScores_Get

```java
/**
 * Samples for SkuMixPlacementScores Get.
 */
public final class SkuMixPlacementScoresGetSamples {
    /*
     * x-ms-original-file: 2026-09-05-preview/GetSkuMixPlacementScores.json
     */
    /**
     * Sample code: Gets the metadata of SkuMixPlacement Scores.
     * 
     * @param manager Entry point to ComputeRecommenderManager.
     */
    public static void getsTheMetadataOfSkuMixPlacementScores(
        com.azure.resourcemanager.compute.recommender.ComputeRecommenderManager manager) {
        manager.skuMixPlacementScores().getWithResponse("eastus", com.azure.core.util.Context.NONE);
    }
}
```

### SkuMixPlacementScores_Post

```java
import com.azure.resourcemanager.compute.recommender.models.SkuMixPlacementAllocationStrategy;
import com.azure.resourcemanager.compute.recommender.models.SkuMixPlacementCapacityProfile;
import com.azure.resourcemanager.compute.recommender.models.SkuMixPlacementCapacityType;
import com.azure.resourcemanager.compute.recommender.models.SkuMixPlacementInstanceDescription;
import com.azure.resourcemanager.compute.recommender.models.SkuMixPlacementOSType;
import com.azure.resourcemanager.compute.recommender.models.SkuMixPlacementPriority;
import com.azure.resourcemanager.compute.recommender.models.SkuMixPlacementRequest;
import com.azure.resourcemanager.compute.recommender.models.SkuMixPlacementVMSize;
import com.azure.resourcemanager.compute.recommender.models.SkuMixPlacementZonalDistributionStrategy;
import com.azure.resourcemanager.compute.recommender.models.SkuMixPlacementZoneAllocationPolicy;
import java.util.Arrays;

/**
 * Samples for SkuMixPlacementScores Post.
 */
public final class SkuMixPlacementScoresPostSamples {
    /*
     * x-ms-original-file: 2026-09-05-preview/GenerateSkuMixPlacementScores.json
     */
    /**
     * Sample code: Generates SkuMixPlacement scores for VM SKU mix placement with explicit VM sizes.
     * 
     * @param manager Entry point to ComputeRecommenderManager.
     */
    public static void generatesSkuMixPlacementScoresForVMSKUMixPlacementWithExplicitVMSizes(
        com.azure.resourcemanager.compute.recommender.ComputeRecommenderManager manager) {
        manager.skuMixPlacementScores()
            .postWithResponse("eastus",
                new SkuMixPlacementRequest().withZones(Arrays.asList("1", "2", "3"))
                    .withCapacityProfile(new SkuMixPlacementCapacityProfile().withCapacity(10)
                        .withCapacityType(SkuMixPlacementCapacityType.VM)
                        .withPriority(SkuMixPlacementPriority.REGULAR)
                        .withAllocationStrategy(SkuMixPlacementAllocationStrategy.LOWEST_PRICE)
                        .withOsType(SkuMixPlacementOSType.LINUX)
                        .withZoneAllocationPolicy(new SkuMixPlacementZoneAllocationPolicy()
                            .withDistributionStrategy(SkuMixPlacementZonalDistributionStrategy.BEST_EFFORT_BALANCED)))
                    .withInstanceDescription(new SkuMixPlacementInstanceDescription()
                        .withVmSizes(Arrays.asList(new SkuMixPlacementVMSize().withName("Standard_D2s_v3"),
                            new SkuMixPlacementVMSize().withName("Standard_D4s_v3"),
                            new SkuMixPlacementVMSize().withName("Standard_D8s_v3")))),
                com.azure.core.util.Context.NONE);
    }
}
```

### SpotPlacementScores_Get

```java
/**
 * Samples for SpotPlacementScores Get.
 */
public final class SpotPlacementScoresGetSamples {
    /*
     * x-ms-original-file: 2026-09-05-preview/GetSpotPlacementScores.json
     */
    /**
     * Sample code: Gets the metadata of Spot Placement Scores.
     * 
     * @param manager Entry point to ComputeRecommenderManager.
     */
    public static void getsTheMetadataOfSpotPlacementScores(
        com.azure.resourcemanager.compute.recommender.ComputeRecommenderManager manager) {
        manager.spotPlacementScores().getWithResponse("eastus", com.azure.core.util.Context.NONE);
    }
}
```

### SpotPlacementScores_Post

```java
import com.azure.resourcemanager.compute.recommender.models.ResourceSize;
import com.azure.resourcemanager.compute.recommender.models.SpotPlacementScoresInput;
import java.util.Arrays;

/**
 * Samples for SpotPlacementScores Post.
 */
public final class SpotPlacementScoresPostSamples {
    /*
     * x-ms-original-file: 2026-09-05-preview/GenerateSpotPlacementScores.json
     */
    /**
     * Sample code: Returns spot VM placement scores for given configurations.
     * 
     * @param manager Entry point to ComputeRecommenderManager.
     */
    public static void returnsSpotVMPlacementScoresForGivenConfigurations(
        com.azure.resourcemanager.compute.recommender.ComputeRecommenderManager manager) {
        manager.spotPlacementScores()
            .postWithResponse("eastus",
                new SpotPlacementScoresInput().withDesiredLocations(Arrays.asList("eastus", "eastus2"))
                    .withDesiredSizes(Arrays.asList(new ResourceSize().withSku("Standard_D2_v2")))
                    .withDesiredCount(1)
                    .withAvailabilityZones(true),
                com.azure.core.util.Context.NONE);
    }
}
```

