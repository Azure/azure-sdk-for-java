# Code snippets and samples


## CheckNameAvailability

- [Post](#checknameavailability_post)

## Diagnostics

- [Create](#diagnostics_create)
- [Get](#diagnostics_get)

## DiscoverySolution

- [List](#discoverysolution_list)

## Operations

- [List](#operations_list)

## SolutionOperation

- [Create](#solutionoperation_create)
- [Get](#solutionoperation_get)
- [Update](#solutionoperation_update)

## Troubleshooters

- [ContinueMethod](#troubleshooters_continuemethod)
- [Create](#troubleshooters_create)
- [End](#troubleshooters_end)
- [Get](#troubleshooters_get)
- [Restart](#troubleshooters_restart)
### CheckNameAvailability_Post

```java
import com.azure.resourcemanager.selfhelp.models.CheckNameAvailabilityRequest;

/** Samples for CheckNameAvailability Post. */
public final class CheckNameAvailabilityPostSamples {
    /*
     * x-ms-original-file: specification/help/resource-manager/Microsoft.Help/preview/2023-09-01-preview/examples/CheckNameAvailabilityForDiagnosticWhenNameIsNotAvailable.json
     */
    /**
     * Sample code: Example when name is not available for a Diagnostic resource.
     *
     * @param manager Entry point to SelfHelpManager.
     */
    public static void exampleWhenNameIsNotAvailableForADiagnosticResource(
        com.azure.resourcemanager.selfhelp.SelfHelpManager manager) {
        manager
            .checkNameAvailabilities()
            .postWithResponse(
                "subscriptions/0d0fcd2e-c4fd-4349-8497-200edb3923c6",
                new CheckNameAvailabilityRequest().withName("sampleName").withType("Microsoft.Help/diagnostics"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/help/resource-manager/Microsoft.Help/preview/2023-09-01-preview/examples/CheckNameAvailabilityForDiagnosticWhenNameIsAvailable.json
     */
    /**
     * Sample code: Example when name is available for a Diagnostic resource.
     *
     * @param manager Entry point to SelfHelpManager.
     */
    public static void exampleWhenNameIsAvailableForADiagnosticResource(
        com.azure.resourcemanager.selfhelp.SelfHelpManager manager) {
        manager
            .checkNameAvailabilities()
            .postWithResponse(
                "subscriptions/0d0fcd2e-c4fd-4349-8497-200edb3923c6",
                new CheckNameAvailabilityRequest().withName("sampleName").withType("Microsoft.Help/diagnostics"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Diagnostics_Create

```java
/** Samples for Diagnostics Create. */
public final class DiagnosticsCreateSamples {
    /*
     * x-ms-original-file: specification/help/resource-manager/Microsoft.Help/preview/2023-09-01-preview/examples/CreateDiagnosticForKeyVaultResource.json
     */
    /**
     * Sample code: Creates a Diagnostic for a KeyVault resource.
     *
     * @param manager Entry point to SelfHelpManager.
     */
    public static void createsADiagnosticForAKeyVaultResource(
        com.azure.resourcemanager.selfhelp.SelfHelpManager manager) {
        manager
            .diagnostics()
            .define("VMNotWorkingInsight")
            .withExistingScope(
                "subscriptions/0d0fcd2e-c4fd-4349-8497-200edb3923c6/resourceGroups/myresourceGroup/providers/Microsoft.KeyVault/vaults/test-keyvault-non-read")
            .create();
    }
}
```

### Diagnostics_Get

```java
/** Samples for Diagnostics Get. */
public final class DiagnosticsGetSamples {
    /*
     * x-ms-original-file: specification/help/resource-manager/Microsoft.Help/preview/2023-09-01-preview/examples/GetDiagnosticForKeyVaultResource.json
     */
    /**
     * Sample code: Gets a Diagnostic for a KeyVault resource.
     *
     * @param manager Entry point to SelfHelpManager.
     */
    public static void getsADiagnosticForAKeyVaultResource(com.azure.resourcemanager.selfhelp.SelfHelpManager manager) {
        manager
            .diagnostics()
            .getWithResponse(
                "subscriptions/0d0fcd2e-c4fd-4349-8497-200edb3923c6/resourceGroups/myresourceGroup/providers/Microsoft.KeyVault/vaults/test-keyvault-non-read",
                "VMNotWorkingInsight",
                com.azure.core.util.Context.NONE);
    }
}
```

### DiscoverySolution_List

```java
/** Samples for DiscoverySolution List. */
public final class DiscoverySolutionListSamples {
    /*
     * x-ms-original-file: specification/help/resource-manager/Microsoft.Help/preview/2023-09-01-preview/examples/ListDiscoverySolutionsAtResourceScope.json
     */
    /**
     * Sample code: List DiscoverySolutions at resource scope.
     *
     * @param manager Entry point to SelfHelpManager.
     */
    public static void listDiscoverySolutionsAtResourceScope(
        com.azure.resourcemanager.selfhelp.SelfHelpManager manager) {
        manager
            .discoverySolutions()
            .list(
                "subscriptions/0d0fcd2e-c4fd-4349-8497-200edb3923c6/resourcegroups/myresourceGroup/providers/Microsoft.KeyVault/vaults/test-keyvault-non-read",
                "ProblemClassificationId eq 'SampleProblemClassificationId1'",
                null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/help/resource-manager/Microsoft.Help/preview/2023-09-01-preview/examples/ListDiscoverySolutionsAtSubscriptionScope.json
     */
    /**
     * Sample code: List DiscoverySolutions at subscription scope.
     *
     * @param manager Entry point to SelfHelpManager.
     */
    public static void listDiscoverySolutionsAtSubscriptionScope(
        com.azure.resourcemanager.selfhelp.SelfHelpManager manager) {
        manager
            .discoverySolutions()
            .list(
                "subscriptions/0d0fcd2e-c4fd-4349-8497-200edb3923c6",
                "ProblemClassificationId eq 'SampleProblemClassificationId1'",
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/help/resource-manager/Microsoft.Help/preview/2023-09-01-preview/examples/ListOperations.json
     */
    /**
     * Sample code: List All Operations.
     *
     * @param manager Entry point to SelfHelpManager.
     */
    public static void listAllOperations(com.azure.resourcemanager.selfhelp.SelfHelpManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### SolutionOperation_Create

```java
import com.azure.resourcemanager.selfhelp.models.Name;
import com.azure.resourcemanager.selfhelp.models.SolutionResourceProperties;
import com.azure.resourcemanager.selfhelp.models.TriggerCriterion;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for SolutionOperation Create. */
public final class SolutionOperationCreateSamples {
    /*
     * x-ms-original-file: specification/help/resource-manager/Microsoft.Help/preview/2023-09-01-preview/examples/Solution_Create.json
     */
    /**
     * Sample code: Solution_Create.
     *
     * @param manager Entry point to SelfHelpManager.
     */
    public static void solutionCreate(com.azure.resourcemanager.selfhelp.SelfHelpManager manager) {
        manager
            .solutionOperations()
            .define("SolutionResourceName1")
            .withExistingScope(
                "subscriptions/mySubscription/resourcegroups/myresourceGroup/providers/Microsoft.KeyVault/vaults/test-keyvault-rp")
            .withProperties(
                new SolutionResourceProperties()
                    .withTriggerCriteria(
                        Arrays.asList(new TriggerCriterion().withName(Name.SOLUTION_ID).withValue("SolutionId1")))
                    .withParameters(
                        mapOf(
                            "resourceUri",
                            "subscriptions/mySubscription/resourcegroups/myresourceGroup/providers/Microsoft.KeyVault/vaults/test-keyvault-rp")))
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

### SolutionOperation_Get

```java
/** Samples for SolutionOperation Get. */
public final class SolutionOperationGetSamples {
    /*
     * x-ms-original-file: specification/help/resource-manager/Microsoft.Help/preview/2023-09-01-preview/examples/Solution_Get.json
     */
    /**
     * Sample code: Solution_Get.
     *
     * @param manager Entry point to SelfHelpManager.
     */
    public static void solutionGet(com.azure.resourcemanager.selfhelp.SelfHelpManager manager) {
        manager
            .solutionOperations()
            .getWithResponse(
                "subscriptions/mySubscription/resourcegroups/myresourceGroup/providers/Microsoft.KeyVault/vaults/test-keyvault-rp",
                "SolutionResource1",
                com.azure.core.util.Context.NONE);
    }
}
```

### SolutionOperation_Update

```java
import com.azure.resourcemanager.selfhelp.models.SolutionResource;

/** Samples for SolutionOperation Update. */
public final class SolutionOperationUpdateSamples {
    /*
     * x-ms-original-file: specification/help/resource-manager/Microsoft.Help/preview/2023-09-01-preview/examples/Solution_Update.json
     */
    /**
     * Sample code: Solution_Update.
     *
     * @param manager Entry point to SelfHelpManager.
     */
    public static void solutionUpdate(com.azure.resourcemanager.selfhelp.SelfHelpManager manager) {
        SolutionResource resource =
            manager
                .solutionOperations()
                .getWithResponse(
                    "subscriptions/mySubscription/resourcegroups/myresourceGroup/providers/Microsoft.KeyVault/vaults/test-keyvault-rp",
                    "SolutionResourceName1",
                    com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().apply();
    }
}
```

### Troubleshooters_ContinueMethod

```java
import com.azure.resourcemanager.selfhelp.models.ContinueRequestBody;
import com.azure.resourcemanager.selfhelp.models.QuestionType;
import com.azure.resourcemanager.selfhelp.models.TroubleshooterResponse;
import java.util.Arrays;

/** Samples for Troubleshooters ContinueMethod. */
public final class TroubleshootersContinueMethodSamples {
    /*
     * x-ms-original-file: specification/help/resource-manager/Microsoft.Help/preview/2023-09-01-preview/examples/Troubleshooter_Continue.json
     */
    /**
     * Sample code: Troubleshooter_Continue.
     *
     * @param manager Entry point to SelfHelpManager.
     */
    public static void troubleshooterContinue(com.azure.resourcemanager.selfhelp.SelfHelpManager manager) {
        manager
            .troubleshooters()
            .continueMethodWithResponse(
                "subscriptions/mySubscription/resourcegroups/myresourceGroup/providers/Microsoft.KeyVault/vaults/test-keyvault-rp",
                "abf168ed-1b54-454a-86f6-e4b62253d3b1",
                new ContinueRequestBody()
                    .withStepId("SampleStepId")
                    .withResponses(
                        Arrays
                            .asList(
                                new TroubleshooterResponse()
                                    .withQuestionId("SampleQuestionId")
                                    .withQuestionType(QuestionType.fromString("Text"))
                                    .withResponse("Connection exception"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Troubleshooters_Create

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for Troubleshooters Create. */
public final class TroubleshootersCreateSamples {
    /*
     * x-ms-original-file: specification/help/resource-manager/Microsoft.Help/preview/2023-09-01-preview/examples/Troubleshooter_Create.json
     */
    /**
     * Sample code: Troubleshooters_Create.
     *
     * @param manager Entry point to SelfHelpManager.
     */
    public static void troubleshootersCreate(com.azure.resourcemanager.selfhelp.SelfHelpManager manager) {
        manager
            .troubleshooters()
            .define("abf168ed-1b54-454a-86f6-e4b62253d3b1")
            .withExistingScope(
                "subscriptions/mySubscription/resourcegroups/myresourceGroup/providers/Microsoft.KeyVault/vaults/test-keyvault-rp")
            .withSolutionId("SampleTroubleshooterSolutionId")
            .withParameters(
                mapOf(
                    "ResourceURI",
                    "subscriptions/mySubscription/resourcegroups/myresourceGroup/providers/Microsoft.KeyVault/vaults/test-keyvault-rp"))
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

### Troubleshooters_End

```java
/** Samples for Troubleshooters End. */
public final class TroubleshootersEndSamples {
    /*
     * x-ms-original-file: specification/help/resource-manager/Microsoft.Help/preview/2023-09-01-preview/examples/Troubleshooter_End.json
     */
    /**
     * Sample code: Troubleshooters_End.
     *
     * @param manager Entry point to SelfHelpManager.
     */
    public static void troubleshootersEnd(com.azure.resourcemanager.selfhelp.SelfHelpManager manager) {
        manager
            .troubleshooters()
            .endWithResponse(
                "subscriptions/mySubscription/resourcegroups/myresourceGroup/providers/Microsoft.KeyVault/vaults/test-keyvault-rp",
                "abf168ed-1b54-454a-86f6-e4b62253d3b1",
                com.azure.core.util.Context.NONE);
    }
}
```

### Troubleshooters_Get

```java
/** Samples for Troubleshooters Get. */
public final class TroubleshootersGetSamples {
    /*
     * x-ms-original-file: specification/help/resource-manager/Microsoft.Help/preview/2023-09-01-preview/examples/Troubleshooter_Get.json
     */
    /**
     * Sample code: Troubleshooters_Get.
     *
     * @param manager Entry point to SelfHelpManager.
     */
    public static void troubleshootersGet(com.azure.resourcemanager.selfhelp.SelfHelpManager manager) {
        manager
            .troubleshooters()
            .getWithResponse(
                "subscriptions/mySubscription/resourcegroups/myresourceGroup/providers/Microsoft.KeyVault/vaults/test-keyvault-rp",
                "abf168ed-1b54-454a-86f6-e4b62253d3b1",
                com.azure.core.util.Context.NONE);
    }
}
```

### Troubleshooters_Restart

```java
/** Samples for Troubleshooters Restart. */
public final class TroubleshootersRestartSamples {
    /*
     * x-ms-original-file: specification/help/resource-manager/Microsoft.Help/preview/2023-09-01-preview/examples/Troubleshooter_Restart.json
     */
    /**
     * Sample code: Troubleshooters_Restart.
     *
     * @param manager Entry point to SelfHelpManager.
     */
    public static void troubleshootersRestart(com.azure.resourcemanager.selfhelp.SelfHelpManager manager) {
        manager
            .troubleshooters()
            .restartWithResponse(
                "subscriptions/mySubscription/resourcegroups/myresourceGroup/providers/Microsoft.KeyVault/vaults/test-keyvault-rp",
                "abf168ed-1b54-454a-86f6-e4b62253d3b1",
                com.azure.core.util.Context.NONE);
    }
}
```

