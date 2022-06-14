# Code snippets and samples


## MarketplaceRegistrationDefinitions

- [Get](#marketplaceregistrationdefinitions_get)
- [List](#marketplaceregistrationdefinitions_list)

## MarketplaceRegistrationDefinitionsWithoutScope

- [Get](#marketplaceregistrationdefinitionswithoutscope_get)
- [List](#marketplaceregistrationdefinitionswithoutscope_list)

## Operations

- [List](#operations_list)

## OperationsWithScope

- [List](#operationswithscope_list)

## RegistrationAssignments

- [CreateOrUpdate](#registrationassignments_createorupdate)
- [Delete](#registrationassignments_delete)
- [Get](#registrationassignments_get)
- [List](#registrationassignments_list)

## RegistrationDefinitions

- [CreateOrUpdate](#registrationdefinitions_createorupdate)
- [Delete](#registrationdefinitions_delete)
- [Get](#registrationdefinitions_get)
- [List](#registrationdefinitions_list)
### MarketplaceRegistrationDefinitions_Get

```java
import com.azure.core.util.Context;

/** Samples for MarketplaceRegistrationDefinitions Get. */
public final class MarketplaceRegistrationDefinitionsGetSamples {
    /*
     * x-ms-original-file: specification/managedservices/resource-manager/Microsoft.ManagedServices/preview/2022-01-01-preview/examples/GetMarketplaceRegistrationDefinition.json
     */
    /**
     * Sample code: Get Registration Definitions.
     *
     * @param manager Entry point to ManagedServicesManager.
     */
    public static void getRegistrationDefinitions(
        com.azure.resourcemanager.managedservices.ManagedServicesManager manager) {
        manager
            .marketplaceRegistrationDefinitions()
            .getWithResponse(
                "subscription/0afefe50-734e-4610-8a82-a144ahf49dea",
                "publisher.product.planName.version",
                Context.NONE);
    }
}
```

### MarketplaceRegistrationDefinitions_List

```java
import com.azure.core.util.Context;

/** Samples for MarketplaceRegistrationDefinitions List. */
public final class MarketplaceRegistrationDefinitionsListSamples {
    /*
     * x-ms-original-file: specification/managedservices/resource-manager/Microsoft.ManagedServices/preview/2022-01-01-preview/examples/GetMarketplaceRegistrationDefinitions.json
     */
    /**
     * Sample code: Get Registration Definitions.
     *
     * @param manager Entry point to ManagedServicesManager.
     */
    public static void getRegistrationDefinitions(
        com.azure.resourcemanager.managedservices.ManagedServicesManager manager) {
        manager
            .marketplaceRegistrationDefinitions()
            .list(
                "subscription/0afefe50-734e-4610-8a82-a144ahf49dea",
                "planIdentifier eq 'publisher.offerIdentifier.planName.version'",
                Context.NONE);
    }
}
```

### MarketplaceRegistrationDefinitionsWithoutScope_Get

```java
import com.azure.core.util.Context;

/** Samples for MarketplaceRegistrationDefinitionsWithoutScope Get. */
public final class MarketplaceRegistrationDefinitionsWithoutScopeGetSamples {
    /*
     * x-ms-original-file: specification/managedservices/resource-manager/Microsoft.ManagedServices/preview/2022-01-01-preview/examples/GetMarketplaceRegistrationDefinitionAtTenantScope.json
     */
    /**
     * Sample code: Get Marketplace Registration Definition At Tenant Scope.
     *
     * @param manager Entry point to ManagedServicesManager.
     */
    public static void getMarketplaceRegistrationDefinitionAtTenantScope(
        com.azure.resourcemanager.managedservices.ManagedServicesManager manager) {
        manager
            .marketplaceRegistrationDefinitionsWithoutScopes()
            .getWithResponse("publisher.product.planName.version", Context.NONE);
    }
}
```

### MarketplaceRegistrationDefinitionsWithoutScope_List

```java
import com.azure.core.util.Context;

/** Samples for MarketplaceRegistrationDefinitionsWithoutScope List. */
public final class MarketplaceRegistrationDefinitionsWithoutScopeListSamples {
    /*
     * x-ms-original-file: specification/managedservices/resource-manager/Microsoft.ManagedServices/preview/2022-01-01-preview/examples/GetMarketplaceRegistrationDefinitionsAtTenantScope.json
     */
    /**
     * Sample code: Get Marketplace Registration Definitions At Tenant Scope.
     *
     * @param manager Entry point to ManagedServicesManager.
     */
    public static void getMarketplaceRegistrationDefinitionsAtTenantScope(
        com.azure.resourcemanager.managedservices.ManagedServicesManager manager) {
        manager
            .marketplaceRegistrationDefinitionsWithoutScopes()
            .list("planIdentifier eq 'publisher.offerIdentifier.planName.version'", Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/managedservices/resource-manager/Microsoft.ManagedServices/preview/2022-01-01-preview/examples/GetOperations.json
     */
    /**
     * Sample code: Get Registration Operations.
     *
     * @param manager Entry point to ManagedServicesManager.
     */
    public static void getRegistrationOperations(
        com.azure.resourcemanager.managedservices.ManagedServicesManager manager) {
        manager.operations().listWithResponse(Context.NONE);
    }
}
```

### OperationsWithScope_List

```java
import com.azure.core.util.Context;

/** Samples for OperationsWithScope List. */
public final class OperationsWithScopeListSamples {
    /*
     * x-ms-original-file: specification/managedservices/resource-manager/Microsoft.ManagedServices/preview/2022-01-01-preview/examples/GetOperationsWithScope.json
     */
    /**
     * Sample code: Get Registration Operations.
     *
     * @param manager Entry point to ManagedServicesManager.
     */
    public static void getRegistrationOperations(
        com.azure.resourcemanager.managedservices.ManagedServicesManager manager) {
        manager
            .operationsWithScopes()
            .listWithResponse("subscription/0afefe50-734e-4610-8a82-a144ahf49dea", Context.NONE);
    }
}
```

### RegistrationAssignments_CreateOrUpdate

```java
import com.azure.resourcemanager.managedservices.models.RegistrationAssignmentProperties;

/** Samples for RegistrationAssignments CreateOrUpdate. */
public final class RegistrationAssignmentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/managedservices/resource-manager/Microsoft.ManagedServices/preview/2022-01-01-preview/examples/PutRegistrationAssignment.json
     */
    /**
     * Sample code: Put Registration Assignment.
     *
     * @param manager Entry point to ManagedServicesManager.
     */
    public static void putRegistrationAssignment(
        com.azure.resourcemanager.managedservices.ManagedServicesManager manager) {
        manager
            .registrationAssignments()
            .define("26c128c2-fefa-4340-9bb1-6e081c90ada2")
            .withExistingScope("subscription/0afefe50-734e-4610-8a82-a144ahf49dea")
            .withProperties(
                new RegistrationAssignmentProperties()
                    .withRegistrationDefinitionId(
                        "/subscriptions/0afefe50-734e-4610-8a82-a144ahf49dea/providers/Microsoft.ManagedServices/registrationDefinitions/26c128c2-fefa-4340-9bb1-6e081c90ada2"))
            .create();
    }
}
```

### RegistrationAssignments_Delete

```java
import com.azure.core.util.Context;

/** Samples for RegistrationAssignments Delete. */
public final class RegistrationAssignmentsDeleteSamples {
    /*
     * x-ms-original-file: specification/managedservices/resource-manager/Microsoft.ManagedServices/preview/2022-01-01-preview/examples/DeleteRegistrationAssignment.json
     */
    /**
     * Sample code: Delete Registration Assignment.
     *
     * @param manager Entry point to ManagedServicesManager.
     */
    public static void deleteRegistrationAssignment(
        com.azure.resourcemanager.managedservices.ManagedServicesManager manager) {
        manager
            .registrationAssignments()
            .delete(
                "subscription/0afefe50-734e-4610-8a82-a144ahf49dea",
                "26c128c2-fefa-4340-9bb1-6e081c90ada2",
                Context.NONE);
    }
}
```

### RegistrationAssignments_Get

```java
import com.azure.core.util.Context;

/** Samples for RegistrationAssignments Get. */
public final class RegistrationAssignmentsGetSamples {
    /*
     * x-ms-original-file: specification/managedservices/resource-manager/Microsoft.ManagedServices/preview/2022-01-01-preview/examples/GetRegistrationAssignment.json
     */
    /**
     * Sample code: Get Registration Assignment.
     *
     * @param manager Entry point to ManagedServicesManager.
     */
    public static void getRegistrationAssignment(
        com.azure.resourcemanager.managedservices.ManagedServicesManager manager) {
        manager
            .registrationAssignments()
            .getWithResponse(
                "subscription/0afefe50-734e-4610-8a82-a144ahf49dea",
                "26c128c2-fefa-4340-9bb1-6e081c90ada2",
                null,
                Context.NONE);
    }
}
```

### RegistrationAssignments_List

```java
import com.azure.core.util.Context;

/** Samples for RegistrationAssignments List. */
public final class RegistrationAssignmentsListSamples {
    /*
     * x-ms-original-file: specification/managedservices/resource-manager/Microsoft.ManagedServices/preview/2022-01-01-preview/examples/GetRegistrationAssignmentsWithManagedByTenantIdEqFilter.json
     */
    /**
     * Sample code: Get Registration Assignments with ManagedByTenantId eq filter.
     *
     * @param manager Entry point to ManagedServicesManager.
     */
    public static void getRegistrationAssignmentsWithManagedByTenantIdEqFilter(
        com.azure.resourcemanager.managedservices.ManagedServicesManager manager) {
        manager
            .registrationAssignments()
            .list(
                "subscription/0afefe50-734e-4610-8a82-a144ahf49dea",
                null,
                "$filter=managedByTenantId eq '83abe5cd-bcc3-441a-bd86-e6a75360cecc'",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/managedservices/resource-manager/Microsoft.ManagedServices/preview/2022-01-01-preview/examples/GetRegistrationAssignments.json
     */
    /**
     * Sample code: Get Registration Assignments.
     *
     * @param manager Entry point to ManagedServicesManager.
     */
    public static void getRegistrationAssignments(
        com.azure.resourcemanager.managedservices.ManagedServicesManager manager) {
        manager
            .registrationAssignments()
            .list("subscription/0afefe50-734e-4610-8a82-a144ahf49dea", null, null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/managedservices/resource-manager/Microsoft.ManagedServices/preview/2022-01-01-preview/examples/GetRegistrationAssignmentsWithManagedByTenantIdInFilter.json
     */
    /**
     * Sample code: Get Registration Assignments with ManagedByTenantId in filter.
     *
     * @param manager Entry point to ManagedServicesManager.
     */
    public static void getRegistrationAssignmentsWithManagedByTenantIdInFilter(
        com.azure.resourcemanager.managedservices.ManagedServicesManager manager) {
        manager
            .registrationAssignments()
            .list(
                "subscription/0afefe50-734e-4610-8a82-a144ahf49dea",
                null,
                "$filter=managedByTenantId in (83abe5cd-bcc3-441a-bd86-e6a75360cec,"
                    + " de83f4a9-a76a-4025-a91a-91171923eac7)",
                Context.NONE);
    }
}
```

### RegistrationDefinitions_CreateOrUpdate

```java
import com.azure.resourcemanager.managedservices.models.Authorization;
import com.azure.resourcemanager.managedservices.models.EligibleApprover;
import com.azure.resourcemanager.managedservices.models.EligibleAuthorization;
import com.azure.resourcemanager.managedservices.models.JustInTimeAccessPolicy;
import com.azure.resourcemanager.managedservices.models.MultiFactorAuthProvider;
import com.azure.resourcemanager.managedservices.models.Plan;
import com.azure.resourcemanager.managedservices.models.RegistrationDefinitionProperties;
import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

/** Samples for RegistrationDefinitions CreateOrUpdate. */
public final class RegistrationDefinitionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/managedservices/resource-manager/Microsoft.ManagedServices/preview/2022-01-01-preview/examples/PutRegistrationDefinition.json
     */
    /**
     * Sample code: Put Registration Definition.
     *
     * @param manager Entry point to ManagedServicesManager.
     */
    public static void putRegistrationDefinition(
        com.azure.resourcemanager.managedservices.ManagedServicesManager manager) {
        manager
            .registrationDefinitions()
            .define("26c128c2-fefa-4340-9bb1-6e081c90ada2")
            .withExistingScope("subscription/0afefe50-734e-4610-8a82-a144ahf49dea")
            .withProperties(
                new RegistrationDefinitionProperties()
                    .withDescription("Tes1t")
                    .withAuthorizations(
                        Arrays
                            .asList(
                                new Authorization()
                                    .withPrincipalId("f98d86a2-4cc4-4e9d-ad47-b3e80a1bcdfc")
                                    .withPrincipalIdDisplayName("Support User")
                                    .withRoleDefinitionId("acdd72a7-3385-48ef-bd42-f606fba81ae7"),
                                new Authorization()
                                    .withPrincipalId("f98d86a2-4cc4-4e9d-ad47-b3e80a1bcdfc")
                                    .withPrincipalIdDisplayName("User Access Administrator")
                                    .withRoleDefinitionId("18d7d88d-d35e-4fb5-a5c3-7773c20a72d9")
                                    .withDelegatedRoleDefinitionIds(
                                        Arrays.asList(UUID.fromString("b24988ac-6180-42a0-ab88-20f7382dd24c")))))
                    .withEligibleAuthorizations(
                        Arrays
                            .asList(
                                new EligibleAuthorization()
                                    .withPrincipalId("3e0ed8c6-e902-4fc5-863c-e3ddbb2ae2a2")
                                    .withPrincipalIdDisplayName("Support User")
                                    .withRoleDefinitionId("ae349356-3a1b-4a5e-921d-050484c6347e")
                                    .withJustInTimeAccessPolicy(
                                        new JustInTimeAccessPolicy()
                                            .withMultiFactorAuthProvider(MultiFactorAuthProvider.AZURE)
                                            .withMaximumActivationDuration(Duration.parse("PT8H"))
                                            .withManagedByTenantApprovers(
                                                Arrays
                                                    .asList(
                                                        new EligibleApprover()
                                                            .withPrincipalId("d9b22cd6-6407-43cc-8c60-07c56df0b51a")
                                                            .withPrincipalIdDisplayName("Approver Group"))))))
                    .withRegistrationDefinitionName("DefinitionName")
                    .withManagedByTenantId("83abe5cd-bcc3-441a-bd86-e6a75360cecc"))
            .withPlan(
                new Plan()
                    .withName("addesai-plan")
                    .withPublisher("marketplace-test")
                    .withProduct("test")
                    .withVersion("1.0.0"))
            .create();
    }
}
```

### RegistrationDefinitions_Delete

```java
import com.azure.core.util.Context;

/** Samples for RegistrationDefinitions Delete. */
public final class RegistrationDefinitionsDeleteSamples {
    /*
     * x-ms-original-file: specification/managedservices/resource-manager/Microsoft.ManagedServices/preview/2022-01-01-preview/examples/DeleteRegistrationDefinition.json
     */
    /**
     * Sample code: Delete Registration Definition.
     *
     * @param manager Entry point to ManagedServicesManager.
     */
    public static void deleteRegistrationDefinition(
        com.azure.resourcemanager.managedservices.ManagedServicesManager manager) {
        manager
            .registrationDefinitions()
            .deleteWithResponse(
                "26c128c2-fefa-4340-9bb1-6e081c90ada2",
                "subscription/0afefe50-734e-4610-8a82-a144ahf49dea",
                Context.NONE);
    }
}
```

### RegistrationDefinitions_Get

```java
import com.azure.core.util.Context;

/** Samples for RegistrationDefinitions Get. */
public final class RegistrationDefinitionsGetSamples {
    /*
     * x-ms-original-file: specification/managedservices/resource-manager/Microsoft.ManagedServices/preview/2022-01-01-preview/examples/GetRegistrationDefinition.json
     */
    /**
     * Sample code: Get Registration Definition.
     *
     * @param manager Entry point to ManagedServicesManager.
     */
    public static void getRegistrationDefinition(
        com.azure.resourcemanager.managedservices.ManagedServicesManager manager) {
        manager
            .registrationDefinitions()
            .getWithResponse(
                "subscription/0afefe50-734e-4610-8a82-a144ahf49dea",
                "26c128c2-fefa-4340-9bb1-6e081c90ada2",
                Context.NONE);
    }
}
```

### RegistrationDefinitions_List

```java
import com.azure.core.util.Context;

/** Samples for RegistrationDefinitions List. */
public final class RegistrationDefinitionsListSamples {
    /*
     * x-ms-original-file: specification/managedservices/resource-manager/Microsoft.ManagedServices/preview/2022-01-01-preview/examples/GetRegistrationDefinitionsWithManagedByTenantIdEqFilter.json
     */
    /**
     * Sample code: Get Registration Definitions with ManagedByTenantId eq filter.
     *
     * @param manager Entry point to ManagedServicesManager.
     */
    public static void getRegistrationDefinitionsWithManagedByTenantIdEqFilter(
        com.azure.resourcemanager.managedservices.ManagedServicesManager manager) {
        manager
            .registrationDefinitions()
            .list(
                "subscription/0afefe50-734e-4610-8a82-a144ahf49dea",
                "$filter=managedByTenantId eq '83ace5cd-bcc3-441a-hd86-e6a75360cecc'",
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/managedservices/resource-manager/Microsoft.ManagedServices/preview/2022-01-01-preview/examples/GetRegistrationDefinitions.json
     */
    /**
     * Sample code: Get Registration Definitions.
     *
     * @param manager Entry point to ManagedServicesManager.
     */
    public static void getRegistrationDefinitions(
        com.azure.resourcemanager.managedservices.ManagedServicesManager manager) {
        manager.registrationDefinitions().list("subscription/0afefe50-734e-4610-8a82-a144ahf49dea", null, Context.NONE);
    }

    /*
     * x-ms-original-file: specification/managedservices/resource-manager/Microsoft.ManagedServices/preview/2022-01-01-preview/examples/GetRegistrationDefinitionsWithManagedByTenantIdInFilter.json
     */
    /**
     * Sample code: Get Registration Definitions with ManagedByTenantId in filter.
     *
     * @param manager Entry point to ManagedServicesManager.
     */
    public static void getRegistrationDefinitionsWithManagedByTenantIdInFilter(
        com.azure.resourcemanager.managedservices.ManagedServicesManager manager) {
        manager
            .registrationDefinitions()
            .list(
                "subscription/0afefe50-734e-4610-8a82-a144ahf49dea",
                "$filter=managedByTenantId in (83ace5cd-bcc3-441a-hd86-e6a75360cecc,"
                    + " de83f4a9-a76a-4025-a91a-91171923eac7)",
                Context.NONE);
    }
}
```

