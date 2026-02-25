# Code snippets and samples


## Operations

- [List](#operations_list)

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
     * x-ms-original-file: 2025-06-05/Operations_List_MinimumSet_Gen.json
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
     * x-ms-original-file: 2025-06-05/Operations_List_MaximumSet_Gen.json
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

### SpotPlacementScores_Get

```java
/**
 * Samples for SpotPlacementScores Get.
 */
public final class SpotPlacementScoresGetSamples {
    /*
     * x-ms-original-file: 2025-06-05/GetSpotPlacementScores.json
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
     * x-ms-original-file: 2025-06-05/GenerateSpotPlacementScores.json
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

