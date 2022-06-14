# Code snippets and samples


## Configurations

- [CreateInResourceGroup](#configurations_createinresourcegroup)
- [CreateInSubscription](#configurations_createinsubscription)
- [List](#configurations_list)
- [ListByResourceGroup](#configurations_listbyresourcegroup)

## RecommendationMetadata

- [Get](#recommendationmetadata_get)
- [List](#recommendationmetadata_list)

## Recommendations

- [Generate](#recommendations_generate)
- [Get](#recommendations_get)
- [GetGenerateStatus](#recommendations_getgeneratestatus)
- [List](#recommendations_list)

## Suppressions

- [Create](#suppressions_create)
- [Delete](#suppressions_delete)
- [Get](#suppressions_get)
- [List](#suppressions_list)
### Configurations_CreateInResourceGroup

```java
import com.azure.resourcemanager.advisor.models.Category;
import com.azure.resourcemanager.advisor.models.ConfigurationName;
import com.azure.resourcemanager.advisor.models.CpuThreshold;
import com.azure.resourcemanager.advisor.models.DigestConfig;
import com.azure.resourcemanager.advisor.models.DigestConfigState;
import java.util.Arrays;

/** Samples for Configurations CreateInResourceGroup. */
public final class ConfigurationsCreateInResourceGroupSamples {
    /*
     * x-ms-original-file: specification/advisor/resource-manager/Microsoft.Advisor/stable/2020-01-01/examples/CreateConfiguration.json
     */
    /**
     * Sample code: PutConfigurations.
     *
     * @param manager Entry point to AdvisorManager.
     */
    public static void putConfigurations(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager
            .configurations()
            .define(ConfigurationName.DEFAULT)
            .withExistingResourceGroup("resourceGroup")
            .withExclude(true)
            .withLowCpuThreshold(CpuThreshold.FIVE)
            .withDigests(
                Arrays
                    .asList(
                        new DigestConfig()
                            .withName("digestConfigName")
                            .withActionGroupResourceId(
                                "/subscriptions/subscriptionId/resourceGroups/resourceGroup/providers/microsoft.insights/actionGroups/actionGroupName")
                            .withFrequency(30)
                            .withCategories(
                                Arrays
                                    .asList(
                                        Category.HIGH_AVAILABILITY,
                                        Category.SECURITY,
                                        Category.PERFORMANCE,
                                        Category.COST,
                                        Category.OPERATIONAL_EXCELLENCE))
                            .withLanguage("en")
                            .withState(DigestConfigState.ACTIVE)))
            .create();
    }
}
```

### Configurations_CreateInSubscription

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.advisor.fluent.models.ConfigDataInner;
import com.azure.resourcemanager.advisor.models.Category;
import com.azure.resourcemanager.advisor.models.ConfigurationName;
import com.azure.resourcemanager.advisor.models.CpuThreshold;
import com.azure.resourcemanager.advisor.models.DigestConfig;
import com.azure.resourcemanager.advisor.models.DigestConfigState;
import java.util.Arrays;

/** Samples for Configurations CreateInSubscription. */
public final class ConfigurationsCreateInSubscriptionSamples {
    /*
     * x-ms-original-file: specification/advisor/resource-manager/Microsoft.Advisor/stable/2020-01-01/examples/CreateConfiguration.json
     */
    /**
     * Sample code: PutConfigurations.
     *
     * @param manager Entry point to AdvisorManager.
     */
    public static void putConfigurations(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager
            .configurations()
            .createInSubscriptionWithResponse(
                ConfigurationName.DEFAULT,
                new ConfigDataInner()
                    .withExclude(true)
                    .withLowCpuThreshold(CpuThreshold.FIVE)
                    .withDigests(
                        Arrays
                            .asList(
                                new DigestConfig()
                                    .withName("digestConfigName")
                                    .withActionGroupResourceId(
                                        "/subscriptions/subscriptionId/resourceGroups/resourceGroup/providers/microsoft.insights/actionGroups/actionGroupName")
                                    .withFrequency(30)
                                    .withCategories(
                                        Arrays
                                            .asList(
                                                Category.HIGH_AVAILABILITY,
                                                Category.SECURITY,
                                                Category.PERFORMANCE,
                                                Category.COST,
                                                Category.OPERATIONAL_EXCELLENCE))
                                    .withLanguage("en")
                                    .withState(DigestConfigState.ACTIVE))),
                Context.NONE);
    }
}
```

### Configurations_List

```java
import com.azure.core.util.Context;

/** Samples for Configurations List. */
public final class ConfigurationsListSamples {
    /*
     * x-ms-original-file: specification/advisor/resource-manager/Microsoft.Advisor/stable/2020-01-01/examples/ListConfigurations.json
     */
    /**
     * Sample code: GetConfigurations.
     *
     * @param manager Entry point to AdvisorManager.
     */
    public static void getConfigurations(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.configurations().list(Context.NONE);
    }
}
```

### Configurations_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Configurations ListByResourceGroup. */
public final class ConfigurationsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/advisor/resource-manager/Microsoft.Advisor/stable/2020-01-01/examples/ListConfigurations.json
     */
    /**
     * Sample code: GetConfigurations.
     *
     * @param manager Entry point to AdvisorManager.
     */
    public static void getConfigurations(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.configurations().listByResourceGroup("resourceGroup", Context.NONE);
    }
}
```

### RecommendationMetadata_Get

```java
import com.azure.core.util.Context;

/** Samples for RecommendationMetadata Get. */
public final class RecommendationMetadataGetSamples {
    /*
     * x-ms-original-file: specification/advisor/resource-manager/Microsoft.Advisor/stable/2020-01-01/examples/GetRecommendationMetadataEntity.json
     */
    /**
     * Sample code: GetMetadata.
     *
     * @param manager Entry point to AdvisorManager.
     */
    public static void getMetadata(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.recommendationMetadatas().getWithResponse("types", Context.NONE);
    }
}
```

### RecommendationMetadata_List

```java
import com.azure.core.util.Context;

/** Samples for RecommendationMetadata List. */
public final class RecommendationMetadataListSamples {
    /*
     * x-ms-original-file: specification/advisor/resource-manager/Microsoft.Advisor/stable/2020-01-01/examples/ListRecommendationMetadata.json
     */
    /**
     * Sample code: GetMetadata.
     *
     * @param manager Entry point to AdvisorManager.
     */
    public static void getMetadata(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.recommendationMetadatas().list(Context.NONE);
    }
}
```

### Recommendations_Generate

```java
import com.azure.core.util.Context;

/** Samples for Recommendations Generate. */
public final class RecommendationsGenerateSamples {
    /*
     * x-ms-original-file: specification/advisor/resource-manager/Microsoft.Advisor/stable/2020-01-01/examples/GenerateRecommendations.json
     */
    /**
     * Sample code: GenerateRecommendations.
     *
     * @param manager Entry point to AdvisorManager.
     */
    public static void generateRecommendations(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.recommendations().generateWithResponse(Context.NONE);
    }
}
```

### Recommendations_Get

```java
import com.azure.core.util.Context;

/** Samples for Recommendations Get. */
public final class RecommendationsGetSamples {
    /*
     * x-ms-original-file: specification/advisor/resource-manager/Microsoft.Advisor/stable/2020-01-01/examples/GetRecommendationDetail.json
     */
    /**
     * Sample code: GetRecommendationDetail.
     *
     * @param manager Entry point to AdvisorManager.
     */
    public static void getRecommendationDetail(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.recommendations().getWithResponse("resourceUri", "recommendationId", Context.NONE);
    }
}
```

### Recommendations_GetGenerateStatus

```java
import com.azure.core.util.Context;
import java.util.UUID;

/** Samples for Recommendations GetGenerateStatus. */
public final class RecommendationsGetGenerateStatusSamples {
    /*
     * x-ms-original-file: specification/advisor/resource-manager/Microsoft.Advisor/stable/2020-01-01/examples/EmptyResponse.json
     */
    /**
     * Sample code: GetGenerateStatus.
     *
     * @param manager Entry point to AdvisorManager.
     */
    public static void getGenerateStatus(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.recommendations().getGenerateStatusWithResponse(UUID.fromString("operationGUID"), Context.NONE);
    }
}
```

### Recommendations_List

```java
import com.azure.core.util.Context;

/** Samples for Recommendations List. */
public final class RecommendationsListSamples {
    /*
     * x-ms-original-file: specification/advisor/resource-manager/Microsoft.Advisor/stable/2020-01-01/examples/ListRecommendations.json
     */
    /**
     * Sample code: ListRecommendations.
     *
     * @param manager Entry point to AdvisorManager.
     */
    public static void listRecommendations(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.recommendations().list(null, 10, null, Context.NONE);
    }
}
```

### Suppressions_Create

```java
/** Samples for Suppressions Create. */
public final class SuppressionsCreateSamples {
    /*
     * x-ms-original-file: specification/advisor/resource-manager/Microsoft.Advisor/stable/2020-01-01/examples/CreateSuppression.json
     */
    /**
     * Sample code: CreateSuppression.
     *
     * @param manager Entry point to AdvisorManager.
     */
    public static void createSuppression(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager
            .suppressions()
            .define("suppressionName1")
            .withExistingRecommendation("resourceUri", "recommendationId")
            .withTtl("07:00:00:00")
            .create();
    }
}
```

### Suppressions_Delete

```java
import com.azure.core.util.Context;

/** Samples for Suppressions Delete. */
public final class SuppressionsDeleteSamples {
    /*
     * x-ms-original-file: specification/advisor/resource-manager/Microsoft.Advisor/stable/2020-01-01/examples/DeleteSuppression.json
     */
    /**
     * Sample code: DeleteSuppression.
     *
     * @param manager Entry point to AdvisorManager.
     */
    public static void deleteSuppression(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.suppressions().deleteWithResponse("resourceUri", "recommendationId", "suppressionName1", Context.NONE);
    }
}
```

### Suppressions_Get

```java
import com.azure.core.util.Context;

/** Samples for Suppressions Get. */
public final class SuppressionsGetSamples {
    /*
     * x-ms-original-file: specification/advisor/resource-manager/Microsoft.Advisor/stable/2020-01-01/examples/GetSuppressionDetail.json
     */
    /**
     * Sample code: GetSuppressionDetail.
     *
     * @param manager Entry point to AdvisorManager.
     */
    public static void getSuppressionDetail(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.suppressions().getWithResponse("resourceUri", "recommendationId", "suppressionName1", Context.NONE);
    }
}
```

### Suppressions_List

```java
import com.azure.core.util.Context;

/** Samples for Suppressions List. */
public final class SuppressionsListSamples {
    /*
     * x-ms-original-file: specification/advisor/resource-manager/Microsoft.Advisor/stable/2020-01-01/examples/ListSuppressions.json
     */
    /**
     * Sample code: ListSuppressions.
     *
     * @param manager Entry point to AdvisorManager.
     */
    public static void listSuppressions(com.azure.resourcemanager.advisor.AdvisorManager manager) {
        manager.suppressions().list(null, null, Context.NONE);
    }
}
```

