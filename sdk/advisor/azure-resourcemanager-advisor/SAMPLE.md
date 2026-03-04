# Code snippets and samples


## AdvisorScores

- [Get](#advisorscores_get)
- [List](#advisorscores_list)

## AssessmentTypes

- [List](#assessmenttypes_list)

## Assessments

- [Delete](#assessments_delete)
- [Get](#assessments_get)
- [List](#assessments_list)
- [Put](#assessments_put)

## Configurations

- [CreateInResourceGroup](#configurations_createinresourcegroup)
- [CreateInSubscription](#configurations_createinsubscription)
- [List](#configurations_list)
- [ListByResourceGroup](#configurations_listbyresourcegroup)

## Operations

- [List](#operations_list)

## RecommendationMetadata

- [Get](#recommendationmetadata_get)
- [List](#recommendationmetadata_list)

## Recommendations

- [Generate](#recommendations_generate)
- [Get](#recommendations_get)
- [GetGenerateStatus](#recommendations_getgeneratestatus)
- [List](#recommendations_list)
- [ListByTenant](#recommendations_listbytenant)
- [Patch](#recommendations_patch)

## ResiliencyReviews

- [Get](#resiliencyreviews_get)
- [List](#resiliencyreviews_list)

## ResourceProvider

- [Predict](#resourceprovider_predict)

## Suppressions

- [Create](#suppressions_create)
- [Delete](#suppressions_delete)
- [Get](#suppressions_get)
- [List](#suppressions_list)

## TriageRecommendations

- [ApproveTriageRecommendation](#triagerecommendations_approvetriagerecommendation)
- [Get](#triagerecommendations_get)
- [List](#triagerecommendations_list)
- [RejectTriageRecommendation](#triagerecommendations_rejecttriagerecommendation)
- [ResetTriageRecommendation](#triagerecommendations_resettriagerecommendation)

## TriageResources

- [Get](#triageresources_get)
- [List](#triageresources_list)

## Workloads

- [List](#workloads_list)
### AdvisorScores_Get

```java
/**
 * Samples for AdvisorScores Get.
 */
public final class AdvisorScoresGetSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/GetAdvisorScoreDetail.json
     */
    /**
     * Sample code: GetAdvisorScoreDetail.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void getAdvisorScoreDetail(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.advisorScores().getWithResponse("Cost", com.azure.core.util.Context.NONE);
    }
}
```

### AdvisorScores_List

```java
/**
 * Samples for AdvisorScores List.
 */
public final class AdvisorScoresListSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/ListAdvisorScore.json
     */
    /**
     * Sample code: ListAdvisorScore.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void listAdvisorScore(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.advisorScores().list(com.azure.core.util.Context.NONE);
    }
}
```

### AssessmentTypes_List

```java
/**
 * Samples for AssessmentTypes List.
 */
public final class AssessmentTypesListSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/ListAssessmentTypes.json
     */
    /**
     * Sample code: GetAssessmentTypes.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void getAssessmentTypes(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.assessmentTypes().list(com.azure.core.util.Context.NONE);
    }
}
```

### Assessments_Delete

```java
/**
 * Samples for Assessments Delete.
 */
public final class AssessmentsDeleteSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/DeleteAssessment.json
     */
    /**
     * Sample code: DeleteAssessment.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void deleteAssessment(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.assessments().deleteWithResponse("assessment1", com.azure.core.util.Context.NONE);
    }
}
```

### Assessments_Get

```java
/**
 * Samples for Assessments Get.
 */
public final class AssessmentsGetSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/GetAssessment.json
     */
    /**
     * Sample code: GetAssessment.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void getAssessment(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.assessments().getWithResponse("assessment1", com.azure.core.util.Context.NONE);
    }
}
```

### Assessments_List

```java
/**
 * Samples for Assessments List.
 */
public final class AssessmentsListSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/ListAssessments.json
     */
    /**
     * Sample code: ListAssessments.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void listAssessments(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.assessments().list(null, null, com.azure.core.util.Context.NONE);
    }
}
```

### Assessments_Put

```java
/**
 * Samples for Assessments Put.
 */
public final class AssessmentsPutSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/PutAssessment.json
     */
    /**
     * Sample code: PutAssessment.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void putAssessment(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.assessments()
            .define("assessment1")
            .withWorkloadId("f72b7134-800f-4f1b-a5bd-691e2140c7d5")
            .withTypeId("23513bdb-e8a2-4f0b-8b6b-191ee1f52d34")
            .withLocale("en-us")
            .create();
    }
}
```

### Configurations_CreateInResourceGroup

```java
import com.azure.resourcemanager.advisor.models.Category;
import com.azure.resourcemanager.advisor.models.ConfigurationName;
import com.azure.resourcemanager.advisor.models.CpuThreshold;
import com.azure.resourcemanager.advisor.models.DigestConfig;
import com.azure.resourcemanager.advisor.models.DigestConfigState;
import com.azure.resourcemanager.advisor.models.DurationModel;
import java.util.Arrays;

/**
 * Samples for Configurations CreateInResourceGroup.
 */
public final class ConfigurationsCreateInResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/CreateConfiguration.json
     */
    /**
     * Sample code: PutConfigurations.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void putConfigurations(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.configurations()
            .define(ConfigurationName.DEFAULT)
            .withExistingResourceGroup("resourceGroup")
            .withExclude(true)
            .withLowCpuThreshold(CpuThreshold.FIVE)
            .withDuration(DurationModel.SEVEN)
            .withDigests(Arrays.asList(new DigestConfig().withName("digestConfigName")
                .withActionGroupResourceId(
                    "/subscriptions/58c3f667-7a62-4bfd-a658-846493e9a493/resourceGroups/resourceGroup/providers/microsoft.insights/actionGroups/actionGroupName")
                .withFrequency(30)
                .withCategories(Arrays.asList(Category.HIGH_AVAILABILITY, Category.SECURITY, Category.PERFORMANCE,
                    Category.COST, Category.OPERATIONAL_EXCELLENCE))
                .withLanguage("en")
                .withState(DigestConfigState.ACTIVE)))
            .create();
    }
}
```

### Configurations_CreateInSubscription

```java
import com.azure.resourcemanager.advisor.fluent.models.ConfigDataInner;
import com.azure.resourcemanager.advisor.models.Category;
import com.azure.resourcemanager.advisor.models.ConfigurationName;
import com.azure.resourcemanager.advisor.models.CpuThreshold;
import com.azure.resourcemanager.advisor.models.DigestConfig;
import com.azure.resourcemanager.advisor.models.DigestConfigState;
import com.azure.resourcemanager.advisor.models.DurationModel;
import java.util.Arrays;

/**
 * Samples for Configurations CreateInSubscription.
 */
public final class ConfigurationsCreateInSubscriptionSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/CreateConfiguration_CreateInSubscription.json
     */
    /**
     * Sample code: PutConfigurations.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void putConfigurations(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.configurations()
            .createInSubscriptionWithResponse(ConfigurationName.DEFAULT, new ConfigDataInner().withExclude(true)
                .withLowCpuThreshold(CpuThreshold.FIVE)
                .withDuration(DurationModel.SEVEN)
                .withDigests(Arrays.asList(new DigestConfig().withName("digestConfigName")
                    .withActionGroupResourceId(
                        "/subscriptions/58c3f667-7a62-4bfd-a658-846493e9a493/resourceGroups/resourceGroup/providers/microsoft.insights/actionGroups/actionGroupName")
                    .withFrequency(30)
                    .withCategories(Arrays.asList(Category.HIGH_AVAILABILITY, Category.SECURITY, Category.PERFORMANCE,
                        Category.COST, Category.OPERATIONAL_EXCELLENCE))
                    .withLanguage("en")
                    .withState(DigestConfigState.ACTIVE))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Configurations_List

```java
/**
 * Samples for Configurations List.
 */
public final class ConfigurationsListSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/ListConfigurations_ListBySubscription.json
     */
    /**
     * Sample code: GetConfigurations.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void getConfigurations(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.configurations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Configurations_ListByResourceGroup

```java
/**
 * Samples for Configurations ListByResourceGroup.
 */
public final class ConfigurationsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/ListConfigurations.json
     */
    /**
     * Sample code: GetConfigurations.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void getConfigurations(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.configurations().listByResourceGroup("resourceGroup", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-05-01-preview/OperationsList.json
     */
    /**
     * Sample code: OperationsList.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void operationsList(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### RecommendationMetadata_Get

```java
/**
 * Samples for RecommendationMetadata Get.
 */
public final class RecommendationMetadataGetSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/GetRecommendationMetadataEntity.json
     */
    /**
     * Sample code: GetMetadata.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void getMetadata(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.recommendationMetadatas().getWithResponse("types", com.azure.core.util.Context.NONE);
    }
}
```

### RecommendationMetadata_List

```java
/**
 * Samples for RecommendationMetadata List.
 */
public final class RecommendationMetadataListSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/ListRecommendationMetadata.json
     */
    /**
     * Sample code: GetMetadata.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void getMetadata(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.recommendationMetadatas().list(com.azure.core.util.Context.NONE);
    }
}
```

### Recommendations_Generate

```java
/**
 * Samples for Recommendations Generate.
 */
public final class RecommendationsGenerateSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/GenerateRecommendations.json
     */
    /**
     * Sample code: GenerateRecommendations.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void generateRecommendations(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.recommendations().generateWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### Recommendations_Get

```java
/**
 * Samples for Recommendations Get.
 */
public final class RecommendationsGetSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/GetRecommendationDetailServiceGroupResourceUri.json
     */
    /**
     * Sample code: GetRecommendationDetailServiceGroupResourceUri.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void
        getRecommendationDetailServiceGroupResourceUri(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.recommendations()
            .getWithResponse("providers/microsoft.management/serviceGroup/serviceGroupXYZ",
                "37c93209-4bfb-4f3b-8874-ccc718f7a467", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-05-01-preview/GetRecommendationDetailSubscriptionResourceUri.json
     */
    /**
     * Sample code: GetRecommendationDetailSubscriptionResourceUri.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void
        getRecommendationDetailSubscriptionResourceUri(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.recommendations()
            .getWithResponse(
                "subscriptions/a5481ee1-95df-47d0-85d4-dd3f0dfa19bc/resourceGroups/resourceGroup/providers/Microsoft.Compute/availabilitysets/armavset",
                "bd27ddc6-1312-4067-b4af-cbb45e32cfd7", com.azure.core.util.Context.NONE);
    }
}
```

### Recommendations_GetGenerateStatus

```java
/**
 * Samples for Recommendations GetGenerateStatus.
 */
public final class RecommendationsGetGenerateStatusSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/EmptyResponse.json
     */
    /**
     * Sample code: GetGenerateStatus.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void getGenerateStatus(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.recommendations()
            .getGenerateStatusWithResponse("123e4567-e89b-12d3-a456-426614174000", com.azure.core.util.Context.NONE);
    }
}
```

### Recommendations_List

```java
/**
 * Samples for Recommendations List.
 */
public final class RecommendationsListSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/ListRecommendationsSubscriptionResourceUri.json
     */
    /**
     * Sample code: ListRecommendationsSubscriptionResourceUri.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void
        listRecommendationsSubscriptionResourceUri(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.recommendations().list(null, 10, null, com.azure.core.util.Context.NONE);
    }
}
```

### Recommendations_ListByTenant

```java
/**
 * Samples for Recommendations ListByTenant.
 */
public final class RecommendationsListByTenantSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/ListRecommendationsServiceGroupResourceUri.json
     */
    /**
     * Sample code: ListRecommendationsServiceGroupResourceUri.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void
        listRecommendationsServiceGroupResourceUri(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.recommendations()
            .listByTenant("providers/microsoft.management/serviceGroups/serviceGroupXYZ", null, 10, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Recommendations_Patch

```java
import com.azure.resourcemanager.advisor.models.State;
import com.azure.resourcemanager.advisor.models.TrackedRecommendationProperties;
import com.azure.resourcemanager.advisor.models.TrackedRecommendationPropertiesPayload;
import com.azure.resourcemanager.advisor.models.TrackedRecommendationPropertiesPayloadProperties;
import java.time.OffsetDateTime;

/**
 * Samples for Recommendations Patch.
 */
public final class RecommendationsPatchSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/UpdateTrackedRecommendation.json
     */
    /**
     * Sample code: UpdateTrackedRecommendation.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void updateTrackedRecommendation(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.recommendations()
            .patchWithResponse(
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/resourceGroupName/providers/Microsoft.Compute/virtualMachines/xyz",
                "c5532a76-8605-4328-ad27-f37ae87c086c",
                new TrackedRecommendationPropertiesPayload()
                    .withProperties(new TrackedRecommendationPropertiesPayloadProperties()
                        .withTrackedProperties(new TrackedRecommendationProperties().withState(State.POSTPONED)
                            .withPostponedTime(OffsetDateTime.parse("2023-10-01T00:00:00Z")))),
                com.azure.core.util.Context.NONE);
    }
}
```

### ResiliencyReviews_Get

```java
/**
 * Samples for ResiliencyReviews Get.
 */
public final class ResiliencyReviewsGetSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/ResiliencyReviewsGet.json
     */
    /**
     * Sample code: GetResiliencyReview.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void getResiliencyReview(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.resiliencyReviews()
            .getWithResponse("11111111-1111-2222-3333-444444444444", com.azure.core.util.Context.NONE);
    }
}
```

### ResiliencyReviews_List

```java
/**
 * Samples for ResiliencyReviews List.
 */
public final class ResiliencyReviewsListSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/ResiliencyReviewsList.json
     */
    /**
     * Sample code: ListResiliencyReviews.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void listResiliencyReviews(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.resiliencyReviews().list(null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_Predict

```java
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.advisor.models.PredictionRequest;
import com.azure.resourcemanager.advisor.models.PredictionType;
import java.nio.charset.StandardCharsets;

/**
 * Samples for ResourceProvider Predict.
 */
public final class ResourceProviderPredictSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Predict.json
     */
    /**
     * Sample code: Predict.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void predict(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.resourceProviders()
            .predictWithResponse(new PredictionRequest().withPredictionType(PredictionType.PREDICTIVE_RIGHTSIZING)
                .withExtendedProperties(BinaryData.fromBytes(
                    "{region=CentralUS, deploymentType=Linux_IaaS_Software_Store, sku=Standard_Dv4, type=iaas, numberOfInstances=10}"
                        .getBytes(StandardCharsets.UTF_8))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Suppressions_Create

```java
/**
 * Samples for Suppressions Create.
 */
public final class SuppressionsCreateSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/CreateSuppression.json
     */
    /**
     * Sample code: CreateSuppression.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void createSuppression(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.suppressions()
            .define("suppressionName1")
            .withExistingRecommendation("resourceUri", "recommendationId")
            .withTtl("07:00:00:00")
            .create();
    }
}
```

### Suppressions_Delete

```java
/**
 * Samples for Suppressions Delete.
 */
public final class SuppressionsDeleteSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/DeleteSuppression.json
     */
    /**
     * Sample code: DeleteSuppression.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void deleteSuppression(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.suppressions()
            .deleteWithResponse("resourceUri", "recommendationId", "suppressionName1",
                com.azure.core.util.Context.NONE);
    }
}
```

### Suppressions_Get

```java
/**
 * Samples for Suppressions Get.
 */
public final class SuppressionsGetSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/GetSuppressionDetail.json
     */
    /**
     * Sample code: GetSuppressionDetail.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void getSuppressionDetail(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.suppressions()
            .getWithResponse("resourceUri", "recommendationId", "suppressionName1", com.azure.core.util.Context.NONE);
    }
}
```

### Suppressions_List

```java
/**
 * Samples for Suppressions List.
 */
public final class SuppressionsListSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/ListSuppressions.json
     */
    /**
     * Sample code: ListSuppressions.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void listSuppressions(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.suppressions().list(null, null, com.azure.core.util.Context.NONE);
    }
}
```

### TriageRecommendations_ApproveTriageRecommendation

```java
/**
 * Samples for TriageRecommendations ApproveTriageRecommendation.
 */
public final class TriageRecommendationsApproveTriageRecommendationSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/TriageRecommendationsApprove.json
     */
    /**
     * Sample code: Approve a triage recommendation.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void approveATriageRecommendation(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.triageRecommendations()
            .approveTriageRecommendationWithResponse("11111111-1111-2222-3333-444444444444",
                "22222222-1111-2222-3333-444444444444", com.azure.core.util.Context.NONE);
    }
}
```

### TriageRecommendations_Get

```java
/**
 * Samples for TriageRecommendations Get.
 */
public final class TriageRecommendationsGetSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/TriageRecommendationsGet.json
     */
    /**
     * Sample code: GetTriageRecommendation.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void getTriageRecommendation(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.triageRecommendations()
            .getWithResponse("11111111-1111-2222-3333-444444444444", "22222222-1111-2222-3333-444444444444",
                com.azure.core.util.Context.NONE);
    }
}
```

### TriageRecommendations_List

```java
/**
 * Samples for TriageRecommendations List.
 */
public final class TriageRecommendationsListSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/TriageRecommendationsList.json
     */
    /**
     * Sample code: ListTriageRecommendations.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void listTriageRecommendations(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.triageRecommendations()
            .list("11111111-1111-2222-3333-444444444444", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### TriageRecommendations_RejectTriageRecommendation

```java
import com.azure.resourcemanager.advisor.models.ReasonForRejectionName;
import com.azure.resourcemanager.advisor.models.RecommendationRejectBody;

/**
 * Samples for TriageRecommendations RejectTriageRecommendation.
 */
public final class TriageRecommendationsRejectTriageRecommendationSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/TriageRecommendationsReject.json
     */
    /**
     * Sample code: Reject a triage recommendation.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void rejectATriageRecommendation(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.triageRecommendations()
            .rejectTriageRecommendationWithResponse("11111111-1111-2222-3333-444444444444",
                "22222222-1111-2222-3333-444444444444",
                new RecommendationRejectBody().withReasonForRejection(ReasonForRejectionName.NOT_ARISK),
                com.azure.core.util.Context.NONE);
    }
}
```

### TriageRecommendations_ResetTriageRecommendation

```java
/**
 * Samples for TriageRecommendations ResetTriageRecommendation.
 */
public final class TriageRecommendationsResetTriageRecommendationSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/TriageRecommendationsReset.json
     */
    /**
     * Sample code: Reset a triage recommendation.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void resetATriageRecommendation(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.triageRecommendations()
            .resetTriageRecommendationWithResponse("11111111-1111-2222-3333-444444444444",
                "22222222-1111-2222-3333-444444444444", com.azure.core.util.Context.NONE);
    }
}
```

### TriageResources_Get

```java
/**
 * Samples for TriageResources Get.
 */
public final class TriageResourcesGetSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/TriageResourcesGet.json
     */
    /**
     * Sample code: GetTriageResource.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void getTriageResource(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.triageResources()
            .getWithResponse("11111111-1111-2222-3333-444444444444", "22222222-1111-2222-3333-444444444444",
                "33333333-1111-2222-3333-444444444444", com.azure.core.util.Context.NONE);
    }
}
```

### TriageResources_List

```java
/**
 * Samples for TriageResources List.
 */
public final class TriageResourcesListSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/TriageResourcesList.json
     */
    /**
     * Sample code: ListTriageResources.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void listTriageResources(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.triageResources()
            .list("11111111-1111-2222-3333-444444444445", "22222222-1111-2222-3333-444444444444",
                com.azure.core.util.Context.NONE);
    }
}
```

### Workloads_List

```java
/**
 * Samples for Workloads List.
 */
public final class WorkloadsListSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/ListWorkloads.json
     */
    /**
     * Sample code: GetWorkloads.
     * 
     * @param manager Entry point to AdvisorManager.
     */
    public static void getWorkloads(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.workloads().list(com.azure.core.util.Context.NONE);
    }
}
```

