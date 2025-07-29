# Code snippets and samples


## Attestations

- [CreateOrUpdateAtResource](#attestations_createorupdateatresource)
- [CreateOrUpdateAtResourceGroup](#attestations_createorupdateatresourcegroup)
- [CreateOrUpdateAtSubscription](#attestations_createorupdateatsubscription)
- [Delete](#attestations_delete)
- [DeleteAtResource](#attestations_deleteatresource)
- [DeleteAtSubscription](#attestations_deleteatsubscription)
- [GetAtResource](#attestations_getatresource)
- [GetAtSubscription](#attestations_getatsubscription)
- [GetByResourceGroup](#attestations_getbyresourcegroup)
- [List](#attestations_list)
- [ListByResourceGroup](#attestations_listbyresourcegroup)
- [ListForResource](#attestations_listforresource)

## ComponentPolicyStates

- [ListQueryResultsForPolicyDefinition](#componentpolicystates_listqueryresultsforpolicydefinition)
- [ListQueryResultsForResource](#componentpolicystates_listqueryresultsforresource)
- [ListQueryResultsForResourceGroup](#componentpolicystates_listqueryresultsforresourcegroup)
- [ListQueryResultsForResourceGroupLevelPolicyAssignment](#componentpolicystates_listqueryresultsforresourcegrouplevelpolicyassignment)
- [ListQueryResultsForSubscription](#componentpolicystates_listqueryresultsforsubscription)
- [ListQueryResultsForSubscriptionLevelPolicyAssignment](#componentpolicystates_listqueryresultsforsubscriptionlevelpolicyassignment)

## Operations

- [List](#operations_list)

## PolicyEvents

- [ListQueryResultsForManagementGroup](#policyevents_listqueryresultsformanagementgroup)
- [ListQueryResultsForPolicyDefinition](#policyevents_listqueryresultsforpolicydefinition)
- [ListQueryResultsForPolicySetDefinition](#policyevents_listqueryresultsforpolicysetdefinition)
- [ListQueryResultsForResource](#policyevents_listqueryresultsforresource)
- [ListQueryResultsForResourceGroup](#policyevents_listqueryresultsforresourcegroup)
- [ListQueryResultsForResourceGroupLevelPolicyAssignment](#policyevents_listqueryresultsforresourcegrouplevelpolicyassignment)
- [ListQueryResultsForSubscription](#policyevents_listqueryresultsforsubscription)
- [ListQueryResultsForSubscriptionLevelPolicyAssignment](#policyevents_listqueryresultsforsubscriptionlevelpolicyassignment)

## PolicyMetadata

- [GetResource](#policymetadata_getresource)
- [List](#policymetadata_list)

## PolicyRestrictions

- [CheckAtManagementGroupScope](#policyrestrictions_checkatmanagementgroupscope)
- [CheckAtResourceGroupScope](#policyrestrictions_checkatresourcegroupscope)
- [CheckAtSubscriptionScope](#policyrestrictions_checkatsubscriptionscope)

## PolicyStates

- [ListQueryResultsForManagementGroup](#policystates_listqueryresultsformanagementgroup)
- [ListQueryResultsForPolicyDefinition](#policystates_listqueryresultsforpolicydefinition)
- [ListQueryResultsForPolicySetDefinition](#policystates_listqueryresultsforpolicysetdefinition)
- [ListQueryResultsForResource](#policystates_listqueryresultsforresource)
- [ListQueryResultsForResourceGroup](#policystates_listqueryresultsforresourcegroup)
- [ListQueryResultsForResourceGroupLevelPolicyAssignment](#policystates_listqueryresultsforresourcegrouplevelpolicyassignment)
- [ListQueryResultsForSubscription](#policystates_listqueryresultsforsubscription)
- [ListQueryResultsForSubscriptionLevelPolicyAssignment](#policystates_listqueryresultsforsubscriptionlevelpolicyassignment)
- [SummarizeForManagementGroup](#policystates_summarizeformanagementgroup)
- [SummarizeForPolicyDefinition](#policystates_summarizeforpolicydefinition)
- [SummarizeForPolicySetDefinition](#policystates_summarizeforpolicysetdefinition)
- [SummarizeForResource](#policystates_summarizeforresource)
- [SummarizeForResourceGroup](#policystates_summarizeforresourcegroup)
- [SummarizeForResourceGroupLevelPolicyAssignment](#policystates_summarizeforresourcegrouplevelpolicyassignment)
- [SummarizeForSubscription](#policystates_summarizeforsubscription)
- [SummarizeForSubscriptionLevelPolicyAssignment](#policystates_summarizeforsubscriptionlevelpolicyassignment)
- [TriggerResourceGroupEvaluation](#policystates_triggerresourcegroupevaluation)
- [TriggerSubscriptionEvaluation](#policystates_triggersubscriptionevaluation)

## PolicyTrackedResources

- [ListQueryResultsForManagementGroup](#policytrackedresources_listqueryresultsformanagementgroup)
- [ListQueryResultsForResource](#policytrackedresources_listqueryresultsforresource)
- [ListQueryResultsForResourceGroup](#policytrackedresources_listqueryresultsforresourcegroup)
- [ListQueryResultsForSubscription](#policytrackedresources_listqueryresultsforsubscription)

## Remediations

- [CancelAtManagementGroup](#remediations_cancelatmanagementgroup)
- [CancelAtResource](#remediations_cancelatresource)
- [CancelAtResourceGroup](#remediations_cancelatresourcegroup)
- [CancelAtSubscription](#remediations_cancelatsubscription)
- [CreateOrUpdateAtManagementGroup](#remediations_createorupdateatmanagementgroup)
- [CreateOrUpdateAtResource](#remediations_createorupdateatresource)
- [CreateOrUpdateAtResourceGroup](#remediations_createorupdateatresourcegroup)
- [CreateOrUpdateAtSubscription](#remediations_createorupdateatsubscription)
- [Delete](#remediations_delete)
- [DeleteAtManagementGroup](#remediations_deleteatmanagementgroup)
- [DeleteAtResource](#remediations_deleteatresource)
- [DeleteAtSubscription](#remediations_deleteatsubscription)
- [GetAtManagementGroup](#remediations_getatmanagementgroup)
- [GetAtResource](#remediations_getatresource)
- [GetAtSubscription](#remediations_getatsubscription)
- [GetByResourceGroup](#remediations_getbyresourcegroup)
- [List](#remediations_list)
- [ListByResourceGroup](#remediations_listbyresourcegroup)
- [ListDeploymentsAtManagementGroup](#remediations_listdeploymentsatmanagementgroup)
- [ListDeploymentsAtResource](#remediations_listdeploymentsatresource)
- [ListDeploymentsAtResourceGroup](#remediations_listdeploymentsatresourcegroup)
- [ListDeploymentsAtSubscription](#remediations_listdeploymentsatsubscription)
- [ListForManagementGroup](#remediations_listformanagementgroup)
- [ListForResource](#remediations_listforresource)
### Attestations_CreateOrUpdateAtResource

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.policyinsights.fluent.models.AttestationInner;
import com.azure.resourcemanager.policyinsights.models.AttestationEvidence;
import com.azure.resourcemanager.policyinsights.models.ComplianceState;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * Samples for Attestations CreateOrUpdateAtResource.
 */
public final class AttestationsCreateOrUpdateAtResourceSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Attestations_CreateResourceScope.json
     */
    /**
     * Sample code: Create attestation at individual resource scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void createAttestationAtIndividualResourceScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) throws IOException {
        manager.attestations()
            .createOrUpdateAtResource(
                "subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/resourcegroups/myrg/providers/microsoft.compute/virtualMachines/devVM",
                "790996e6-9871-4b1f-9cd9-ec42cd6ced1e",
                new AttestationInner().withPolicyAssignmentId(
                    "/subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/providers/microsoft.authorization/policyassignments/b101830944f246d8a14088c5")
                    .withPolicyDefinitionReferenceId("0b158b46-ff42-4799-8e39-08a5c23b4551")
                    .withComplianceState(ComplianceState.COMPLIANT)
                    .withExpiresOn(OffsetDateTime.parse("2021-06-15T00:00:00Z"))
                    .withOwner("55a32e28-3aa5-4eea-9b5a-4cd85153b966")
                    .withComments("This subscription has passed a security audit.")
                    .withEvidence(
                        Arrays.asList(new AttestationEvidence().withDescription("The results of the security audit.")
                            .withSourceUri("https://gist.github.com/contoso/9573e238762c60166c090ae16b814011")))
                    .withAssessmentDate(OffsetDateTime.parse("2021-06-10T00:00:00Z"))
                    .withMetadata(SerializerFactory.createDefaultManagementSerializerAdapter()
                        .deserialize("{\"departmentId\":\"NYC-MARKETING-1\"}", Object.class, SerializerEncoding.JSON)),
                com.azure.core.util.Context.NONE);
    }
}
```

### Attestations_CreateOrUpdateAtResourceGroup

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.policyinsights.models.AttestationEvidence;
import com.azure.resourcemanager.policyinsights.models.ComplianceState;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * Samples for Attestations CreateOrUpdateAtResourceGroup.
 */
public final class AttestationsCreateOrUpdateAtResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Attestations_CreateResourceGroupScope.json
     */
    /**
     * Sample code: Create attestation at resource group scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void createAttestationAtResourceGroupScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) throws IOException {
        manager.attestations()
            .define("790996e6-9871-4b1f-9cd9-ec42cd6ced1e")
            .withExistingResourceGroup("myRg")
            .withPolicyAssignmentId(
                "/subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/providers/microsoft.authorization/policyassignments/b101830944f246d8a14088c5")
            .withPolicyDefinitionReferenceId("0b158b46-ff42-4799-8e39-08a5c23b4551")
            .withComplianceState(ComplianceState.COMPLIANT)
            .withExpiresOn(OffsetDateTime.parse("2021-06-15T00:00:00Z"))
            .withOwner("55a32e28-3aa5-4eea-9b5a-4cd85153b966")
            .withComments("This subscription has passed a security audit.")
            .withEvidence(Arrays.asList(new AttestationEvidence().withDescription("The results of the security audit.")
                .withSourceUri("https://gist.github.com/contoso/9573e238762c60166c090ae16b814011")))
            .withAssessmentDate(OffsetDateTime.parse("2021-06-10T00:00:00Z"))
            .withMetadata(SerializerFactory.createDefaultManagementSerializerAdapter()
                .deserialize("{\"departmentId\":\"NYC-MARKETING-1\"}", Object.class, SerializerEncoding.JSON))
            .create();
    }
}
```

### Attestations_CreateOrUpdateAtSubscription

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.policyinsights.fluent.models.AttestationInner;
import com.azure.resourcemanager.policyinsights.models.AttestationEvidence;
import com.azure.resourcemanager.policyinsights.models.ComplianceState;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * Samples for Attestations CreateOrUpdateAtSubscription.
 */
public final class AttestationsCreateOrUpdateAtSubscriptionSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Attestations_CreateSubscriptionScope.json
     */
    /**
     * Sample code: Create attestation at subscription scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        createAttestationAtSubscriptionScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.attestations()
            .createOrUpdateAtSubscription("790996e6-9871-4b1f-9cd9-ec42cd6ced1e",
                new AttestationInner().withPolicyAssignmentId(
                    "/subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/providers/microsoft.authorization/policyassignments/b101830944f246d8a14088c5")
                    .withComplianceState(ComplianceState.COMPLIANT),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Attestations_CreateSubscriptionScope_AllProperties.json
     */
    /**
     * Sample code: Create attestation at subscription scope with all properties.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void createAttestationAtSubscriptionScopeWithAllProperties(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) throws IOException {
        manager.attestations()
            .createOrUpdateAtSubscription("790996e6-9871-4b1f-9cd9-ec42cd6ced1e",
                new AttestationInner().withPolicyAssignmentId(
                    "/subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/providers/microsoft.authorization/policyassignments/b101830944f246d8a14088c5")
                    .withPolicyDefinitionReferenceId("0b158b46-ff42-4799-8e39-08a5c23b4551")
                    .withComplianceState(ComplianceState.COMPLIANT)
                    .withExpiresOn(OffsetDateTime.parse("2021-06-15T00:00:00Z"))
                    .withOwner("55a32e28-3aa5-4eea-9b5a-4cd85153b966")
                    .withComments("This subscription has passed a security audit.")
                    .withEvidence(
                        Arrays.asList(new AttestationEvidence().withDescription("The results of the security audit.")
                            .withSourceUri("https://gist.github.com/contoso/9573e238762c60166c090ae16b814011")))
                    .withAssessmentDate(OffsetDateTime.parse("2021-06-10T00:00:00Z"))
                    .withMetadata(SerializerFactory.createDefaultManagementSerializerAdapter()
                        .deserialize("{\"departmentId\":\"NYC-MARKETING-1\"}", Object.class, SerializerEncoding.JSON)),
                com.azure.core.util.Context.NONE);
    }
}
```

### Attestations_Delete

```java
/**
 * Samples for Attestations Delete.
 */
public final class AttestationsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Attestations_DeleteResourceGroupScope.json
     */
    /**
     * Sample code: Delete attestation at resource group scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        deleteAttestationAtResourceGroupScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.attestations()
            .deleteByResourceGroupWithResponse("myRg", "790996e6-9871-4b1f-9cd9-ec42cd6ced1e",
                com.azure.core.util.Context.NONE);
    }
}
```

### Attestations_DeleteAtResource

```java
/**
 * Samples for Attestations DeleteAtResource.
 */
public final class AttestationsDeleteAtResourceSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Attestations_DeleteResourceScope.json
     */
    /**
     * Sample code: Delete attestation at individual resource scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void deleteAttestationAtIndividualResourceScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.attestations()
            .deleteAtResourceWithResponse(
                "subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/resourcegroups/myrg/providers/microsoft.compute/virtualMachines/devVM",
                "790996e6-9871-4b1f-9cd9-ec42cd6ced1e", com.azure.core.util.Context.NONE);
    }
}
```

### Attestations_DeleteAtSubscription

```java
/**
 * Samples for Attestations DeleteAtSubscription.
 */
public final class AttestationsDeleteAtSubscriptionSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Attestations_DeleteSubscriptionScope.json
     */
    /**
     * Sample code: Delete attestation at subscription scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        deleteAttestationAtSubscriptionScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.attestations()
            .deleteAtSubscriptionWithResponse("790996e6-9871-4b1f-9cd9-ec42cd6ced1e", com.azure.core.util.Context.NONE);
    }
}
```

### Attestations_GetAtResource

```java
/**
 * Samples for Attestations GetAtResource.
 */
public final class AttestationsGetAtResourceSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Attestations_GetResourceScope.json
     */
    /**
     * Sample code: Get attestation at individual resource scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void getAttestationAtIndividualResourceScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.attestations()
            .getAtResourceWithResponse(
                "subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/resourcegroups/myrg/providers/microsoft.compute/virtualMachines/devVM",
                "790996e6-9871-4b1f-9cd9-ec42cd6ced1e", com.azure.core.util.Context.NONE);
    }
}
```

### Attestations_GetAtSubscription

```java
/**
 * Samples for Attestations GetAtSubscription.
 */
public final class AttestationsGetAtSubscriptionSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Attestations_GetSubscriptionScope.json
     */
    /**
     * Sample code: Get attestation at subscription scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        getAttestationAtSubscriptionScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.attestations()
            .getAtSubscriptionWithResponse("790996e6-9871-4b1f-9cd9-ec42cd6ced1e", com.azure.core.util.Context.NONE);
    }
}
```

### Attestations_GetByResourceGroup

```java
/**
 * Samples for Attestations GetByResourceGroup.
 */
public final class AttestationsGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Attestations_GetResourceGroupScope.json
     */
    /**
     * Sample code: Get attestation at resource group scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        getAttestationAtResourceGroupScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.attestations()
            .getByResourceGroupWithResponse("myRg", "790996e6-9871-4b1f-9cd9-ec42cd6ced1e",
                com.azure.core.util.Context.NONE);
    }
}
```

### Attestations_List

```java
/**
 * Samples for Attestations List.
 */
public final class AttestationsListSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Attestations_ListSubscriptionScope.json
     */
    /**
     * Sample code: List attestations at subscription scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        listAttestationsAtSubscriptionScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.attestations().list(null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Attestations_ListSubscriptionScope_WithQuery.json
     */
    /**
     * Sample code: List attestations at subscription scope with query parameters.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void listAttestationsAtSubscriptionScopeWithQueryParameters(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.attestations()
            .list(1,
                "PolicyAssignmentId eq '/subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/providers/microsoft.authorization/policyassignments/b101830944f246d8a14088c5' AND PolicyDefinitionReferenceId eq '0b158b46-ff42-4799-8e39-08a5c23b4551'",
                com.azure.core.util.Context.NONE);
    }
}
```

### Attestations_ListByResourceGroup

```java
/**
 * Samples for Attestations ListByResourceGroup.
 */
public final class AttestationsListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Attestations_ListResourceGroupScope_WithQuery.json
     */
    /**
     * Sample code: List attestations at resource group scope with query parameters.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void listAttestationsAtResourceGroupScopeWithQueryParameters(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.attestations()
            .listByResourceGroup("myRg", 1,
                "PolicyAssignmentId eq '/subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/providers/microsoft.authorization/policyassignments/b101830944f246d8a14088c5' AND PolicyDefinitionReferenceId eq '0b158b46-ff42-4799-8e39-08a5c23b4551'",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Attestations_ListResourceGroupScope.json
     */
    /**
     * Sample code: List attestations at resource group scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        listAttestationsAtResourceGroupScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.attestations().listByResourceGroup("myRg", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### Attestations_ListForResource

```java
/**
 * Samples for Attestations ListForResource.
 */
public final class AttestationsListForResourceSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Attestations_ListResourceScope_WithQuery.json
     */
    /**
     * Sample code: List attestations at individual resource scope with query parameters.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void listAttestationsAtIndividualResourceScopeWithQueryParameters(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.attestations()
            .listForResource(
                "subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/resourcegroups/myrg/providers/microsoft.compute/virtualMachines/devVM",
                1,
                "PolicyAssignmentId eq '/subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/providers/microsoft.authorization/policyassignments/b101830944f246d8a14088c5' AND PolicyDefinitionReferenceId eq '0b158b46-ff42-4799-8e39-08a5c23b4551'",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Attestations_ListResourceScope.json
     */
    /**
     * Sample code: List attestations at individual resource scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void listAttestationsAtIndividualResourceScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.attestations()
            .listForResource(
                "subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/resourcegroups/myrg/providers/microsoft.compute/virtualMachines/devVM",
                null, null, com.azure.core.util.Context.NONE);
    }
}
```

### ComponentPolicyStates_ListQueryResultsForPolicyDefinition

```java
import com.azure.resourcemanager.policyinsights.models.ComponentPolicyStatesResource;

/**
 * Samples for ComponentPolicyStates ListQueryResultsForPolicyDefinition.
 */
public final class ComponentPolicyStatesListQueryResultsForPolicyDefinitionSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * ComponentPolicyStates_QuerySubscriptionLevelPolicyDefinitionScope.json
     */
    /**
     * Sample code: Query latest component policy states at subscription level policy definition scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryLatestComponentPolicyStatesAtSubscriptionLevelPolicyDefinitionScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.componentPolicyStates()
            .listQueryResultsForPolicyDefinitionWithResponse("fffedd8f-ffff-fffd-fffd-fffed2f84852",
                "24813039-7534-408a-9842-eb99f45721b1", ComponentPolicyStatesResource.LATEST, null, null, null, null,
                null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### ComponentPolicyStates_ListQueryResultsForResource

```java
import com.azure.resourcemanager.policyinsights.models.ComponentPolicyStatesResource;

/**
 * Samples for ComponentPolicyStates ListQueryResultsForResource.
 */
public final class ComponentPolicyStatesListQueryResultsForResourceSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * ComponentPolicyStates_QueryResourceScopeFilterByComponentId.json
     */
    /**
     * Sample code: Query latest component policy compliance state at resource scope filtered by given component id.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryLatestComponentPolicyComplianceStateAtResourceScopeFilteredByGivenComponentId(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.componentPolicyStates()
            .listQueryResultsForResourceWithResponse(
                "subscriptions/fff10b27-fff3-fff5-fff8-fffbe01e86a5/resourceGroups/myResourceGroup/providers/Microsoft.KeyVault/Vaults/myKVName",
                ComponentPolicyStatesResource.LATEST, null, null, null, null, null, "componentId eq cert-RSA-cert-3",
                null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * ComponentPolicyStates_QueryNestedResourceScope.json
     */
    /**
     * Sample code: Query latest component policy states at nested resource scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryLatestComponentPolicyStatesAtNestedResourceScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.componentPolicyStates()
            .listQueryResultsForResourceWithResponse(
                "subscriptions/fff10b27-fff3-fff5-fff8-fffbe01e86a5/resourceGroups/myResourceGroup/providers/Microsoft.KeyVault/vaults/myVault",
                ComponentPolicyStatesResource.LATEST, null, null, null, null, null, null, null, null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * ComponentPolicyStates_QueryResourceScopeGroupByComponentTypeWithAggregate.json
     */
    /**
     * Sample code: Query latest component policy compliance state count grouped by component type at resource scope
     * filtered by given assignment.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        queryLatestComponentPolicyComplianceStateCountGroupedByComponentTypeAtResourceScopeFilteredByGivenAssignment(
            com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.componentPolicyStates()
            .listQueryResultsForResourceWithResponse(
                "subscriptions/e78961ba-36fe-4739-9212-e3031b4c8db7/resourceGroups/myResourceGroup/providers/Microsoft.KeyVault/Vaults/myKVName",
                ComponentPolicyStatesResource.LATEST, null, null, null, null, null,
                "policyAssignmentId eq '/subscriptions/e78961ba-36fe-4739-9212-e3031b4c8db7/providers/microsoft.authorization/policyassignments/560050f83dbb4a24974323f8'",
                "groupby((componentType,complianceState),aggregate($count as count))", null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * ComponentPolicyStates_QueryResourceScope.json
     */
    /**
     * Sample code: Query latest component policy states at resource scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryLatestComponentPolicyStatesAtResourceScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.componentPolicyStates()
            .listQueryResultsForResourceWithResponse(
                "subscriptions/fff10b27-fff3-fff5-fff8-fffbe01e86a5/resourceGroups/myResourceGroup/providers/Microsoft.KeyVault/Vaults/myKVName",
                ComponentPolicyStatesResource.LATEST, null, null, null, null, null, null, null, null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * ComponentPolicyStates_QueryResourceScopeExpandPolicyEvaluationDetails.json
     */
    /**
     * Sample code: Query latest component policy states at resource scope and expand policyEvaluationDetails.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryLatestComponentPolicyStatesAtResourceScopeAndExpandPolicyEvaluationDetails(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.componentPolicyStates()
            .listQueryResultsForResourceWithResponse(
                "subscriptions/fff10b27-fff3-fff5-fff8-fffbe01e86a5/resourceGroups/myResourceGroup/providers/Microsoft.ContainerService/managedClusters/myCluster",
                ComponentPolicyStatesResource.LATEST, null, null, null, null, null,
                "componentType eq 'pod' AND componentId eq 'default/test-pod' AND componentName eq 'test-pod'", null,
                "PolicyEvaluationDetails", com.azure.core.util.Context.NONE);
    }
}
```

### ComponentPolicyStates_ListQueryResultsForResourceGroup

```java
import com.azure.resourcemanager.policyinsights.models.ComponentPolicyStatesResource;

/**
 * Samples for ComponentPolicyStates ListQueryResultsForResourceGroup.
 */
public final class ComponentPolicyStatesListQueryResultsForResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * ComponentPolicyStates_QueryResourceGroupScopeGroupByComponentTypeWithAggregate.json
     */
    /**
     * Sample code: Query latest component policy compliance state count grouped by component type at resource group
     * scope filtered by given assignment.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        queryLatestComponentPolicyComplianceStateCountGroupedByComponentTypeAtResourceGroupScopeFilteredByGivenAssignment(
            com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.componentPolicyStates()
            .listQueryResultsForResourceGroupWithResponse("fffedd8f-ffff-fffd-fffd-fffed2f84852", "myResourceGroup",
                ComponentPolicyStatesResource.LATEST, null, null, null, null, null,
                "policyAssignmentId eq '/subscriptions/fffedd8f-ffff-fffd-fffd-fffed2f84852/providers/microsoft.authorization/policyassignments/560050f83dbb4a24974323f8'",
                "groupby((type,complianceState),aggregate($count as count))", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * ComponentPolicyStates_QueryResourceGroupScope.json
     */
    /**
     * Sample code: Query latest component policy states at resource group scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryLatestComponentPolicyStatesAtResourceGroupScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.componentPolicyStates()
            .listQueryResultsForResourceGroupWithResponse("fffedd8f-ffff-fffd-fffd-fffed2f84852", "myResourceGroup",
                ComponentPolicyStatesResource.LATEST, null, null, null, null, null, null, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### ComponentPolicyStates_ListQueryResultsForResourceGroupLevelPolicyAssignment

```java
import com.azure.resourcemanager.policyinsights.models.ComponentPolicyStatesResource;

/**
 * Samples for ComponentPolicyStates ListQueryResultsForResourceGroupLevelPolicyAssignment.
 */
public final class ComponentPolicyStatesListQueryResultsForResourceGroupLevelPolicyAssignmentSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * ComponentPolicyStates_QueryResourceGroupLevelPolicyAssignmentScope.json
     */
    /**
     * Sample code: Query latest at resource group level policy assignment scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryLatestAtResourceGroupLevelPolicyAssignmentScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.componentPolicyStates()
            .listQueryResultsForResourceGroupLevelPolicyAssignmentWithResponse("fffedd8f-ffff-fffd-fffd-fffed2f84852",
                "myResourceGroup", "myPolicyAssignment", ComponentPolicyStatesResource.LATEST, null, null, null, null,
                null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### ComponentPolicyStates_ListQueryResultsForSubscription

```java
import com.azure.resourcemanager.policyinsights.models.ComponentPolicyStatesResource;

/**
 * Samples for ComponentPolicyStates ListQueryResultsForSubscription.
 */
public final class ComponentPolicyStatesListQueryResultsForSubscriptionSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * ComponentPolicyStates_QuerySubscriptionScopeGroupByComponentTypeWithAggregate.json
     */
    /**
     * Sample code: Query latest component policy compliance state count grouped by component type at subscription scope
     * filtered by given assignment.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        queryLatestComponentPolicyComplianceStateCountGroupedByComponentTypeAtSubscriptionScopeFilteredByGivenAssignment(
            com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.componentPolicyStates()
            .listQueryResultsForSubscriptionWithResponse("e78961ba-36fe-4739-9212-e3031b4c8db7",
                ComponentPolicyStatesResource.LATEST, null, null, null, null, null,
                "policyAssignmentId eq '/subscriptions/e78961ba-36fe-4739-9212-e3031b4c8db7/providers/microsoft.authorization/policyassignments/560050f83dbb4a24974323f8'",
                "groupby((componentType,complianceState),aggregate($count as count))",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * ComponentPolicyStates_QuerySubscriptionScope.json
     */
    /**
     * Sample code: Query latest component policy states at subscription scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryLatestComponentPolicyStatesAtSubscriptionScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.componentPolicyStates()
            .listQueryResultsForSubscriptionWithResponse("fff10b27-fff3-fff5-fff8-fffbe01e86a5",
                ComponentPolicyStatesResource.LATEST, null, null, null, null, null, null, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### ComponentPolicyStates_ListQueryResultsForSubscriptionLevelPolicyAssignment

```java
import com.azure.resourcemanager.policyinsights.models.ComponentPolicyStatesResource;

/**
 * Samples for ComponentPolicyStates ListQueryResultsForSubscriptionLevelPolicyAssignment.
 */
public final class ComponentPolicyStatesListQueryResultsForSubscriptionLevelPolicyAssignmentSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * ComponentPolicyStates_QuerySubscriptionLevelPolicyAssignmentScope.json
     */
    /**
     * Sample code: Query latest at subscription level policy assignment scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryLatestAtSubscriptionLevelPolicyAssignmentScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.componentPolicyStates()
            .listQueryResultsForSubscriptionLevelPolicyAssignmentWithResponse("fffedd8f-ffff-fffd-fffd-fffed2f84852",
                "ec8f9645-8ecb-4abb-9c0b-5292f19d4003", ComponentPolicyStatesResource.LATEST, null, null, null, null,
                null, null, null, com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Operations_ListOperations.json
     */
    /**
     * Sample code: List operations.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void listOperations(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.operations().listWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### PolicyEvents_ListQueryResultsForManagementGroup

```java
import com.azure.resourcemanager.policyinsights.models.PolicyEventsResourceType;

/**
 * Samples for PolicyEvents ListQueryResultsForManagementGroup.
 */
public final class PolicyEventsListQueryResultsForManagementGroupSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyEvents_QueryManagementGroupScopeNextLink.json
     */
    /**
     * Sample code: Query at management group scope with next link.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryAtManagementGroupScopeWithNextLink(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyEvents()
            .listQueryResultsForManagementGroup(PolicyEventsResourceType.DEFAULT, "myManagementGroup", null, null, null,
                null, null, null, null, "WpmWfBSvPhkAK6QD", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyEvents_QueryManagementGroupScope.json
     */
    /**
     * Sample code: Query at management group scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        queryAtManagementGroupScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyEvents()
            .listQueryResultsForManagementGroup(PolicyEventsResourceType.DEFAULT, "myManagementGroup", null, null, null,
                null, null, null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicyEvents_ListQueryResultsForPolicyDefinition

```java
import com.azure.resourcemanager.policyinsights.models.PolicyEventsResourceType;

/**
 * Samples for PolicyEvents ListQueryResultsForPolicyDefinition.
 */
public final class PolicyEventsListQueryResultsForPolicyDefinitionSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyEvents_QuerySubscriptionLevelPolicyDefinitionScope.json
     */
    /**
     * Sample code: Query at subscription level policy definition scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryAtSubscriptionLevelPolicyDefinitionScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyEvents()
            .listQueryResultsForPolicyDefinition(PolicyEventsResourceType.DEFAULT,
                "fffedd8f-ffff-fffd-fffd-fffed2f84852", "24813039-7534-408a-9842-eb99f45721b1", null, null, null, null,
                null, null, null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyEvents_QuerySubscriptionLevelPolicyDefinitionScopeNextLink.json
     */
    /**
     * Sample code: Query at subscription level policy definition scope with next link.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryAtSubscriptionLevelPolicyDefinitionScopeWithNextLink(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyEvents()
            .listQueryResultsForPolicyDefinition(PolicyEventsResourceType.DEFAULT,
                "fffedd8f-ffff-fffd-fffd-fffed2f84852", "24813039-7534-408a-9842-eb99f45721b1", null, null, null, null,
                null, null, null, "WpmWfBSvPhkAK6QD", com.azure.core.util.Context.NONE);
    }
}
```

### PolicyEvents_ListQueryResultsForPolicySetDefinition

```java
import com.azure.resourcemanager.policyinsights.models.PolicyEventsResourceType;

/**
 * Samples for PolicyEvents ListQueryResultsForPolicySetDefinition.
 */
public final class PolicyEventsListQueryResultsForPolicySetDefinitionSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyEvents_QuerySubscriptionLevelPolicySetDefinitionScope.json
     */
    /**
     * Sample code: Query at subscription level policy set definition scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryAtSubscriptionLevelPolicySetDefinitionScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyEvents()
            .listQueryResultsForPolicySetDefinition(PolicyEventsResourceType.DEFAULT,
                "fffedd8f-ffff-fffd-fffd-fffed2f84852", "3e3807c1-65c9-49e0-a406-82d8ae3e338c", null, null, null, null,
                null, null, null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyEvents_QuerySubscriptionLevelPolicySetDefinitionScopeNextLink.json
     */
    /**
     * Sample code: Query at subscription level policy set definition scope with next link.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryAtSubscriptionLevelPolicySetDefinitionScopeWithNextLink(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyEvents()
            .listQueryResultsForPolicySetDefinition(PolicyEventsResourceType.DEFAULT,
                "fffedd8f-ffff-fffd-fffd-fffed2f84852", "3e3807c1-65c9-49e0-a406-82d8ae3e338c", null, null, null, null,
                null, null, null, "WpmWfBSvPhkAK6QD", com.azure.core.util.Context.NONE);
    }
}
```

### PolicyEvents_ListQueryResultsForResource

```java
import com.azure.resourcemanager.policyinsights.models.PolicyEventsResourceType;

/**
 * Samples for PolicyEvents ListQueryResultsForResource.
 */
public final class PolicyEventsListQueryResultsForResourceSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyEvents_QuerySubscriptionLevelResourceScope.json
     */
    /**
     * Sample code: Query at subscription level resource scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        queryAtSubscriptionLevelResourceScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyEvents()
            .listQueryResultsForResource(PolicyEventsResourceType.DEFAULT,
                "subscriptions/fff10b27-fff3-fff5-fff8-fffbe01e86a5/providers/Microsoft.SomeNamespace/someResourceType/someResourceName",
                null, null, null, null, null, null, null, null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyEvents_QueryNestedResourceScope.json
     */
    /**
     * Sample code: Query at nested resource scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        queryAtNestedResourceScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyEvents()
            .listQueryResultsForResource(PolicyEventsResourceType.DEFAULT,
                "subscriptions/fff10b27-fff3-fff5-fff8-fffbe01e86a5/resourceGroups/myResourceGroup/providers/Microsoft.ServiceFabric/clusters/myCluster/applications/myApplication",
                null, null, null, null, null, null, null, null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyEvents_QuerySubscriptionLevelNestedResourceScope.json
     */
    /**
     * Sample code: Query at subscription level nested resource scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryAtSubscriptionLevelNestedResourceScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyEvents()
            .listQueryResultsForResource(PolicyEventsResourceType.DEFAULT,
                "subscriptions/fff10b27-fff3-fff5-fff8-fffbe01e86a5/providers/Microsoft.SomeNamespace/someResourceType/someResource/someNestedResourceType/someNestedResource",
                null, null, null, null, null, null, null, null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyEvents_QueryResourceScope.json
     */
    /**
     * Sample code: Query at resource scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryAtResourceScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyEvents()
            .listQueryResultsForResource(PolicyEventsResourceType.DEFAULT,
                "subscriptions/fff10b27-fff3-fff5-fff8-fffbe01e86a5/resourceGroups/myResourceGroup/providers/Microsoft.ClassicCompute/domainNames/myDomainName",
                null, null, null, null, null, null, null, null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyEvents_QueryResourceScopeExpandComponents.json
     */
    /**
     * Sample code: Query components policy events for resource scope filtered by given assignment.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryComponentsPolicyEventsForResourceScopeFilteredByGivenAssignment(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyEvents()
            .listQueryResultsForResource(PolicyEventsResourceType.DEFAULT,
                "subscriptions/e78961ba-36fe-4739-9212-e3031b4c8db7/resourceGroups/myResourceGroup/providers/Microsoft.KeyVault/Vaults/myKVName",
                null, null, null, null, null,
                "policyAssignmentId eq '/subscriptions/e78961ba-36fe-4739-9212-e3031b4c8db7/providers/microsoft.authorization/policyassignments/560050f83dbb4a24974323f8'",
                null, "components", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyEvents_QueryResourceScopeExpandComponentsGroupByWithAggregate.json
     */
    /**
     * Sample code: Query components policy events count grouped by user and action type for resource scope filtered by
     * given assignment.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        queryComponentsPolicyEventsCountGroupedByUserAndActionTypeForResourceScopeFilteredByGivenAssignment(
            com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyEvents()
            .listQueryResultsForResource(PolicyEventsResourceType.DEFAULT,
                "subscriptions/e78961ba-36fe-4739-9212-e3031b4c8db7/resourceGroups/myResourceGroup/providers/Microsoft.KeyVault/Vaults/myKVName",
                null, null, null, null, null,
                "policyAssignmentId eq '/subscriptions/e78961ba-36fe-4739-9212-e3031b4c8db7/providers/microsoft.authorization/policyassignments/560050f83dbb4a24974323f8'",
                null,
                "components($apply=groupby((tenantId, principalOid, policyDefinitionAction), aggregate($count as totalActions)))",
                null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyEvents_QueryResourceScopeNextLink.json
     */
    /**
     * Sample code: Query at resource scope with next link.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        queryAtResourceScopeWithNextLink(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyEvents()
            .listQueryResultsForResource(PolicyEventsResourceType.DEFAULT,
                "subscriptions/fff10b27-fff3-fff5-fff8-fffbe01e86a5/resourceGroups/myResourceGroup/providers/Microsoft.ClassicCompute/domainNames/myDomainName",
                null, null, null, null, null, null, null, null, "WpmWfBSvPhkAK6QD", com.azure.core.util.Context.NONE);
    }
}
```

### PolicyEvents_ListQueryResultsForResourceGroup

```java
import com.azure.resourcemanager.policyinsights.models.PolicyEventsResourceType;

/**
 * Samples for PolicyEvents ListQueryResultsForResourceGroup.
 */
public final class PolicyEventsListQueryResultsForResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyEvents_QueryResourceGroupScope.json
     */
    /**
     * Sample code: Query at resource group scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        queryAtResourceGroupScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyEvents()
            .listQueryResultsForResourceGroup(PolicyEventsResourceType.DEFAULT, "fffedd8f-ffff-fffd-fffd-fffed2f84852",
                "myResourceGroup", null, null, null, null, null, null, null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyEvents_QueryResourceGroupScopeNextLink.json
     */
    /**
     * Sample code: Query at resource group scope with next link.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        queryAtResourceGroupScopeWithNextLink(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyEvents()
            .listQueryResultsForResourceGroup(PolicyEventsResourceType.DEFAULT, "fffedd8f-ffff-fffd-fffd-fffed2f84852",
                "myResourceGroup", null, null, null, null, null, null, null, "WpmWfBSvPhkAK6QD",
                com.azure.core.util.Context.NONE);
    }
}
```

### PolicyEvents_ListQueryResultsForResourceGroupLevelPolicyAssignment

```java
import com.azure.resourcemanager.policyinsights.models.PolicyEventsResourceType;

/**
 * Samples for PolicyEvents ListQueryResultsForResourceGroupLevelPolicyAssignment.
 */
public final class PolicyEventsListQueryResultsForResourceGroupLevelPolicyAssignmentSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyEvents_QueryResourceGroupLevelPolicyAssignmentScope.json
     */
    /**
     * Sample code: Query at resource group level policy assignment scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryAtResourceGroupLevelPolicyAssignmentScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyEvents()
            .listQueryResultsForResourceGroupLevelPolicyAssignment(PolicyEventsResourceType.DEFAULT,
                "fffedd8f-ffff-fffd-fffd-fffed2f84852", "myResourceGroup", "myPolicyAssignment", null, null, null, null,
                null, null, null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyEvents_QueryResourceGroupLevelPolicyAssignmentScopeNextLink.json
     */
    /**
     * Sample code: Query at resource group level policy assignment scope with next link.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryAtResourceGroupLevelPolicyAssignmentScopeWithNextLink(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyEvents()
            .listQueryResultsForResourceGroupLevelPolicyAssignment(PolicyEventsResourceType.DEFAULT,
                "fffedd8f-ffff-fffd-fffd-fffed2f84852", "myResourceGroup", "myPolicyAssignment", null, null, null, null,
                null, null, null, "WpmWfBSvPhkAK6QD", com.azure.core.util.Context.NONE);
    }
}
```

### PolicyEvents_ListQueryResultsForSubscription

```java
import com.azure.resourcemanager.policyinsights.models.PolicyEventsResourceType;
import java.time.OffsetDateTime;

/**
 * Samples for PolicyEvents ListQueryResultsForSubscription.
 */
public final class PolicyEventsListQueryResultsForSubscriptionSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyEvents_TimeRangeSortSelectTop.json
     */
    /**
     * Sample code: Time range; sort, select and limit.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        timeRangeSortSelectAndLimit(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyEvents()
            .listQueryResultsForSubscription(PolicyEventsResourceType.DEFAULT, "fffedd8f-ffff-fffd-fffd-fffed2f84852",
                2, "Timestamp desc, PolicyAssignmentId asc, SubscriptionId asc, ResourceGroup asc, ResourceId",
                "Timestamp, PolicyAssignmentId, PolicyDefinitionId, SubscriptionId, ResourceGroup, ResourceId",
                OffsetDateTime.parse("2018-02-05T18:00:00Z"), OffsetDateTime.parse("2018-02-06T18:00:00Z"), null, null,
                null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyEvents_FilterAndAggregateOnly.json
     */
    /**
     * Sample code: Filter and aggregate only.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void filterAndAggregateOnly(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyEvents()
            .listQueryResultsForSubscription(PolicyEventsResourceType.DEFAULT, "fffedd8f-ffff-fffd-fffd-fffed2f84852",
                null, null, null, OffsetDateTime.parse("2018-02-05T18:00:00Z"), null,
                "PolicyDefinitionAction eq 'deny'", "aggregate($count as NumDenyEvents)", null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyEvents_FilterAndGroupByWithoutAggregate.json
     */
    /**
     * Sample code: Filter and group without aggregate.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        filterAndGroupWithoutAggregate(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyEvents()
            .listQueryResultsForSubscription(PolicyEventsResourceType.DEFAULT, "fffedd8f-ffff-fffd-fffd-fffed2f84852",
                2, null, null, OffsetDateTime.parse("2018-01-05T18:00:00Z"), null,
                "PolicyDefinitionAction ne 'audit' and PolicyDefinitionAction ne 'append'",
                "groupby((PolicyAssignmentId, PolicyDefinitionId, PolicyDefinitionAction, ResourceId))", null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyEvents_QuerySubscriptionScopeNextLink.json
     */
    /**
     * Sample code: Query at subscription scope with next link.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        queryAtSubscriptionScopeWithNextLink(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyEvents()
            .listQueryResultsForSubscription(PolicyEventsResourceType.DEFAULT, "fffedd8f-ffff-fffd-fffd-fffed2f84852",
                null, null, null, null, null, null, null, "WpmWfBSvPhkAK6QD", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyEvents_FilterAndMultipleGroups.json
     */
    /**
     * Sample code: Filter and multiple groups.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void filterAndMultipleGroups(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyEvents()
            .listQueryResultsForSubscription(PolicyEventsResourceType.DEFAULT, "fffedd8f-ffff-fffd-fffd-fffed2f84852",
                10, "NumDeniedResources desc", null, OffsetDateTime.parse("2018-01-01T00:00:00Z"), null,
                "PolicyDefinitionAction eq 'deny'",
                "groupby((PolicyAssignmentId, PolicyDefinitionId, ResourceId))/groupby((PolicyAssignmentId, PolicyDefinitionId), aggregate($count as NumDeniedResources))",
                null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyEvents_FilterAndGroupByWithAggregate.json
     */
    /**
     * Sample code: Filter and group with aggregate.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        filterAndGroupWithAggregate(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyEvents()
            .listQueryResultsForSubscription(PolicyEventsResourceType.DEFAULT, "fffedd8f-ffff-fffd-fffd-fffed2f84852",
                2, null, null, OffsetDateTime.parse("2018-02-05T18:00:00Z"), null,
                "PolicyDefinitionAction eq 'audit' or PolicyDefinitionAction eq 'deny'",
                "groupby((PolicyAssignmentId, PolicyDefinitionId, PolicyDefinitionAction, ResourceId), aggregate($count as NumEvents))",
                null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyEvents_QuerySubscriptionScope.json
     */
    /**
     * Sample code: Query at subscription scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        queryAtSubscriptionScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyEvents()
            .listQueryResultsForSubscription(PolicyEventsResourceType.DEFAULT, "fffedd8f-ffff-fffd-fffd-fffed2f84852",
                null, null, null, null, null, null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicyEvents_ListQueryResultsForSubscriptionLevelPolicyAssignment

```java
import com.azure.resourcemanager.policyinsights.models.PolicyEventsResourceType;

/**
 * Samples for PolicyEvents ListQueryResultsForSubscriptionLevelPolicyAssignment.
 */
public final class PolicyEventsListQueryResultsForSubscriptionLevelPolicyAssignmentSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyEvents_QuerySubscriptionLevelPolicyAssignmentScope.json
     */
    /**
     * Sample code: Query at subscription level policy assignment scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryAtSubscriptionLevelPolicyAssignmentScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyEvents()
            .listQueryResultsForSubscriptionLevelPolicyAssignment(PolicyEventsResourceType.DEFAULT,
                "fffedd8f-ffff-fffd-fffd-fffed2f84852", "ec8f9645-8ecb-4abb-9c0b-5292f19d4003", null, null, null, null,
                null, null, null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyEvents_QuerySubscriptionLevelPolicyAssignmentScopeNextLink.json
     */
    /**
     * Sample code: Query at subscription level policy assignment scope with next link.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryAtSubscriptionLevelPolicyAssignmentScopeWithNextLink(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyEvents()
            .listQueryResultsForSubscriptionLevelPolicyAssignment(PolicyEventsResourceType.DEFAULT,
                "fffedd8f-ffff-fffd-fffd-fffed2f84852", "ec8f9645-8ecb-4abb-9c0b-5292f19d4003", null, null, null, null,
                null, null, null, "WpmWfBSvPhkAK6QD", com.azure.core.util.Context.NONE);
    }
}
```

### PolicyMetadata_GetResource

```java
/**
 * Samples for PolicyMetadata GetResource.
 */
public final class PolicyMetadataGetResourceSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyMetadata_GetResource.json
     */
    /**
     * Sample code: Get a single policy metadata resource.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        getASinglePolicyMetadataResource(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyMetadatas().getResourceWithResponse("NIST_SP_800-53_R4_AC-2", com.azure.core.util.Context.NONE);
    }
}
```

### PolicyMetadata_List

```java
/**
 * Samples for PolicyMetadata List.
 */
public final class PolicyMetadataListSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyMetadata_List.json
     */
    /**
     * Sample code: Get collection of policy metadata resources.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        getCollectionOfPolicyMetadataResources(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyMetadatas().list(null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyMetadata_List_WithTop.json
     */
    /**
     * Sample code: Get collection of policy metadata resources using top query parameter.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void getCollectionOfPolicyMetadataResourcesUsingTopQueryParameter(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyMetadatas().list(1, com.azure.core.util.Context.NONE);
    }
}
```

### PolicyRestrictions_CheckAtManagementGroupScope

```java
import com.azure.resourcemanager.policyinsights.models.CheckManagementGroupRestrictionsRequest;
import com.azure.resourcemanager.policyinsights.models.PendingField;
import java.util.Arrays;

/**
 * Samples for PolicyRestrictions CheckAtManagementGroupScope.
 */
public final class PolicyRestrictionsCheckAtManagementGroupScopeSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyRestrictions_CheckAtManagementGroupScope.json
     */
    /**
     * Sample code: Check policy restrictions at management group scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void checkPolicyRestrictionsAtManagementGroupScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyRestrictions()
            .checkAtManagementGroupScopeWithResponse("financeMg", new CheckManagementGroupRestrictionsRequest()
                .withPendingFields(Arrays.asList(new PendingField().withField("type"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### PolicyRestrictions_CheckAtResourceGroupScope

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.policyinsights.models.CheckRestrictionsRequest;
import com.azure.resourcemanager.policyinsights.models.CheckRestrictionsResourceDetails;
import com.azure.resourcemanager.policyinsights.models.PendingField;
import java.io.IOException;
import java.util.Arrays;

/**
 * Samples for PolicyRestrictions CheckAtResourceGroupScope.
 */
public final class PolicyRestrictionsCheckAtResourceGroupScopeSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyRestrictions_CheckAtResourceGroupScope.json
     */
    /**
     * Sample code: Check policy restrictions at resource group scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void checkPolicyRestrictionsAtResourceGroupScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) throws IOException {
        manager.policyRestrictions()
            .checkAtResourceGroupScopeWithResponse("vmRg", new CheckRestrictionsRequest()
                .withResourceDetails(new CheckRestrictionsResourceDetails()
                    .withResourceContent(SerializerFactory.createDefaultManagementSerializerAdapter()
                        .deserialize(
                            "{\"type\":\"Microsoft.Compute/virtualMachines\",\"properties\":{\"priority\":\"Spot\"}}",
                            Object.class, SerializerEncoding.JSON))
                    .withApiVersion("2019-12-01"))
                .withPendingFields(
                    Arrays.asList(new PendingField().withField("name").withValues(Arrays.asList("myVMName")),
                        new PendingField().withField("location")
                            .withValues(Arrays.asList("eastus", "westus", "westus2", "westeurope")),
                        new PendingField().withField("tags"))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyRestrictions_CheckAtResourceGroupScopeIncludeAuditEffect.json
     */
    /**
     * Sample code: Check policy restrictions at resource group scope including audit effect.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void checkPolicyRestrictionsAtResourceGroupScopeIncludingAuditEffect(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) throws IOException {
        manager.policyRestrictions()
            .checkAtResourceGroupScopeWithResponse("vmRg", new CheckRestrictionsRequest()
                .withResourceDetails(new CheckRestrictionsResourceDetails()
                    .withResourceContent(SerializerFactory.createDefaultManagementSerializerAdapter()
                        .deserialize(
                            "{\"type\":\"Microsoft.Compute/virtualMachines\",\"properties\":{\"priority\":\"Spot\"}}",
                            Object.class, SerializerEncoding.JSON))
                    .withApiVersion("2019-12-01"))
                .withPendingFields(
                    Arrays.asList(new PendingField().withField("name").withValues(Arrays.asList("myVMName")),
                        new PendingField().withField("location")
                            .withValues(Arrays.asList("eastus", "westus", "westus2", "westeurope")),
                        new PendingField().withField("tags")))
                .withIncludeAuditEffect(true), com.azure.core.util.Context.NONE);
    }
}
```

### PolicyRestrictions_CheckAtSubscriptionScope

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.policyinsights.models.CheckRestrictionsRequest;
import com.azure.resourcemanager.policyinsights.models.CheckRestrictionsResourceDetails;
import com.azure.resourcemanager.policyinsights.models.PendingField;
import java.io.IOException;
import java.util.Arrays;

/**
 * Samples for PolicyRestrictions CheckAtSubscriptionScope.
 */
public final class PolicyRestrictionsCheckAtSubscriptionScopeSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyRestrictions_CheckAtSubscriptionScope.json
     */
    /**
     * Sample code: Check policy restrictions at subscription scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void checkPolicyRestrictionsAtSubscriptionScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) throws IOException {
        manager.policyRestrictions()
            .checkAtSubscriptionScopeWithResponse(new CheckRestrictionsRequest()
                .withResourceDetails(new CheckRestrictionsResourceDetails()
                    .withResourceContent(SerializerFactory.createDefaultManagementSerializerAdapter()
                        .deserialize(
                            "{\"type\":\"Microsoft.Compute/virtualMachines\",\"properties\":{\"priority\":\"Spot\"}}",
                            Object.class, SerializerEncoding.JSON))
                    .withApiVersion("2019-12-01"))
                .withPendingFields(
                    Arrays.asList(new PendingField().withField("name").withValues(Arrays.asList("myVMName")),
                        new PendingField().withField("location")
                            .withValues(Arrays.asList("eastus", "westus", "westus2", "westeurope")),
                        new PendingField().withField("tags"))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyRestrictions_CheckAtSubscriptionScopeIncludeAuditEffect.json
     */
    /**
     * Sample code: Check policy restrictions at subscription scope including audit effect.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void checkPolicyRestrictionsAtSubscriptionScopeIncludingAuditEffect(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) throws IOException {
        manager.policyRestrictions()
            .checkAtSubscriptionScopeWithResponse(new CheckRestrictionsRequest()
                .withResourceDetails(new CheckRestrictionsResourceDetails()
                    .withResourceContent(SerializerFactory.createDefaultManagementSerializerAdapter()
                        .deserialize(
                            "{\"type\":\"Microsoft.Compute/virtualMachines\",\"properties\":{\"priority\":\"Spot\"}}",
                            Object.class, SerializerEncoding.JSON))
                    .withApiVersion("2019-12-01"))
                .withPendingFields(
                    Arrays.asList(new PendingField().withField("name").withValues(Arrays.asList("myVMName")),
                        new PendingField().withField("location")
                            .withValues(Arrays.asList("eastus", "westus", "westus2", "westeurope")),
                        new PendingField().withField("tags")))
                .withIncludeAuditEffect(true), com.azure.core.util.Context.NONE);
    }
}
```

### PolicyStates_ListQueryResultsForManagementGroup

```java
import com.azure.resourcemanager.policyinsights.models.PolicyStatesResource;

/**
 * Samples for PolicyStates ListQueryResultsForManagementGroup.
 */
public final class PolicyStatesListQueryResultsForManagementGroupSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_QueryManagementGroupScope.json
     */
    /**
     * Sample code: Query latest at management group scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        queryLatestAtManagementGroupScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .listQueryResultsForManagementGroup(PolicyStatesResource.LATEST, "myManagementGroup", null, null, null,
                null, null, null, null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_QueryManagementGroupScopeNextLink.json
     */
    /**
     * Sample code: Query latest at management group scope with next link.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryLatestAtManagementGroupScopeWithNextLink(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .listQueryResultsForManagementGroup(PolicyStatesResource.LATEST, "myManagementGroup", null, null, null,
                null, null, null, null, "WpmWfBSvPhkAK6QD", com.azure.core.util.Context.NONE);
    }
}
```

### PolicyStates_ListQueryResultsForPolicyDefinition

```java
import com.azure.resourcemanager.policyinsights.models.PolicyStatesResource;

/**
 * Samples for PolicyStates ListQueryResultsForPolicyDefinition.
 */
public final class PolicyStatesListQueryResultsForPolicyDefinitionSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_QuerySubscriptionLevelPolicyDefinitionScope.json
     */
    /**
     * Sample code: Query latest at subscription level policy definition scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryLatestAtSubscriptionLevelPolicyDefinitionScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .listQueryResultsForPolicyDefinition(PolicyStatesResource.LATEST, "fffedd8f-ffff-fffd-fffd-fffed2f84852",
                "24813039-7534-408a-9842-eb99f45721b1", null, null, null, null, null, null, null, null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_QuerySubscriptionLevelPolicyDefinitionScopeNextLink.json
     */
    /**
     * Sample code: Query latest at subscription level policy definition scope with next link.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryLatestAtSubscriptionLevelPolicyDefinitionScopeWithNextLink(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .listQueryResultsForPolicyDefinition(PolicyStatesResource.LATEST, "fffedd8f-ffff-fffd-fffd-fffed2f84852",
                "24813039-7534-408a-9842-eb99f45721b1", null, null, null, null, null, null, null, "WpmWfBSvPhkAK6QD",
                com.azure.core.util.Context.NONE);
    }
}
```

### PolicyStates_ListQueryResultsForPolicySetDefinition

```java
import com.azure.resourcemanager.policyinsights.models.PolicyStatesResource;

/**
 * Samples for PolicyStates ListQueryResultsForPolicySetDefinition.
 */
public final class PolicyStatesListQueryResultsForPolicySetDefinitionSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_QuerySubscriptionLevelPolicySetDefinitionScopeNextLink.json
     */
    /**
     * Sample code: Query latest at subscription level policy set definition scope with next link.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryLatestAtSubscriptionLevelPolicySetDefinitionScopeWithNextLink(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .listQueryResultsForPolicySetDefinition(PolicyStatesResource.LATEST, "fffedd8f-ffff-fffd-fffd-fffed2f84852",
                "3e3807c1-65c9-49e0-a406-82d8ae3e338c", null, null, null, null, null, null, null, "WpmWfBSvPhkAK6QD",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_QuerySubscriptionLevelPolicySetDefinitionScope.json
     */
    /**
     * Sample code: Query latest at subscription level policy set definition scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryLatestAtSubscriptionLevelPolicySetDefinitionScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .listQueryResultsForPolicySetDefinition(PolicyStatesResource.LATEST, "fffedd8f-ffff-fffd-fffd-fffed2f84852",
                "3e3807c1-65c9-49e0-a406-82d8ae3e338c", null, null, null, null, null, null, null, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### PolicyStates_ListQueryResultsForResource

```java
import com.azure.resourcemanager.policyinsights.models.PolicyStatesResource;

/**
 * Samples for PolicyStates ListQueryResultsForResource.
 */
public final class PolicyStatesListQueryResultsForResourceSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_QueryResourceScopeExpandPolicyEvaluationDetails.json
     */
    /**
     * Sample code: Query all policy states at resource scope and expand policyEvaluationDetails.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryAllPolicyStatesAtResourceScopeAndExpandPolicyEvaluationDetails(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .listQueryResultsForResource(PolicyStatesResource.LATEST,
                "subscriptions/fff10b27-fff3-fff5-fff8-fffbe01e86a5/resourceGroups/myResourceGroup/providers/Microsoft.ClassicCompute/domainNames/myDomainName",
                null, null, null, null, null, null, null, "PolicyEvaluationDetails", null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_QueryResourceScopeNextLink.json
     */
    /**
     * Sample code: Query all policy states at resource scope with next link.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryAllPolicyStatesAtResourceScopeWithNextLink(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .listQueryResultsForResource(PolicyStatesResource.DEFAULT,
                "subscriptions/fff10b27-fff3-fff5-fff8-fffbe01e86a5/resourceGroups/myResourceGroup/providers/Microsoft.ClassicCompute/domainNames/myDomainName",
                null, null, null, null, null, null, null, null, "WpmWfBSvPhkAK6QD", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_QuerySubscriptionLevelNestedResourceScope.json
     */
    /**
     * Sample code: Query all policy states at subscription level nested resource scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryAllPolicyStatesAtSubscriptionLevelNestedResourceScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .listQueryResultsForResource(PolicyStatesResource.DEFAULT,
                "subscriptions/fff10b27-fff3-fff5-fff8-fffbe01e86a5/providers/Microsoft.SomeNamespace/someResourceType/someResource/someNestedResourceType/someNestedResource",
                null, null, null, null, null, null, null, null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_QueryResourceScopeExpandComponents.json
     */
    /**
     * Sample code: Query component policy compliance state at resource scope filtered by given assignment.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryComponentPolicyComplianceStateAtResourceScopeFilteredByGivenAssignment(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .listQueryResultsForResource(PolicyStatesResource.LATEST,
                "subscriptions/e78961ba-36fe-4739-9212-e3031b4c8db7/resourceGroups/myResourceGroup/providers/Microsoft.KeyVault/Vaults/myKVName",
                null, null, null, null, null,
                "policyAssignmentId eq '/subscriptions/e78961ba-36fe-4739-9212-e3031b4c8db7/providers/microsoft.authorization/policyassignments/560050f83dbb4a24974323f8'",
                null, "components($filter=ComplianceState eq 'NonCompliant' or ComplianceState eq 'Compliant')", null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_QueryResourceScopeExpandComponentsGroupByWithAggregate.json
     */
    /**
     * Sample code: Query component policy compliance state count grouped by state type at resource scope filtered by
     * given assignment.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        queryComponentPolicyComplianceStateCountGroupedByStateTypeAtResourceScopeFilteredByGivenAssignment(
            com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .listQueryResultsForResource(PolicyStatesResource.LATEST,
                "subscriptions/e78961ba-36fe-4739-9212-e3031b4c8db7/resourceGroups/myResourceGroup/providers/Microsoft.KeyVault/Vaults/myKVName",
                null, null, null, null, null,
                "policyAssignmentId eq '/subscriptions/e78961ba-36fe-4739-9212-e3031b4c8db7/providers/microsoft.authorization/policyassignments/560050f83dbb4a24974323f8'",
                null,
                "components($filter=ComplianceState eq 'NonCompliant' or ComplianceState eq 'Compliant';$apply=groupby((complianceState),aggregate($count as count)))",
                null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_QuerySubscriptionLevelResourceScope.json
     */
    /**
     * Sample code: Query all policy states at subscription level resource scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryAllPolicyStatesAtSubscriptionLevelResourceScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .listQueryResultsForResource(PolicyStatesResource.DEFAULT,
                "subscriptions/fff10b27-fff3-fff5-fff8-fffbe01e86a5/providers/Microsoft.SomeNamespace/someResourceType/someResourceName",
                null, null, null, null, null, null, null, null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_QueryResourceScope.json
     */
    /**
     * Sample code: Query all policy states at resource scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        queryAllPolicyStatesAtResourceScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .listQueryResultsForResource(PolicyStatesResource.DEFAULT,
                "subscriptions/fff10b27-fff3-fff5-fff8-fffbe01e86a5/resourceGroups/myResourceGroup/providers/Microsoft.ClassicCompute/domainNames/myDomainName",
                null, null, null, null, null, null, null, null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_QueryNestedResourceScope.json
     */
    /**
     * Sample code: Query all policy states at nested resource scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryAllPolicyStatesAtNestedResourceScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .listQueryResultsForResource(PolicyStatesResource.DEFAULT,
                "subscriptions/fff10b27-fff3-fff5-fff8-fffbe01e86a5/resourceGroups/myResourceGroup/providers/Microsoft.ServiceFabric/clusters/myCluster/applications/myApplication",
                null, null, null, null, null, null, null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicyStates_ListQueryResultsForResourceGroup

```java
import com.azure.resourcemanager.policyinsights.models.PolicyStatesResource;

/**
 * Samples for PolicyStates ListQueryResultsForResourceGroup.
 */
public final class PolicyStatesListQueryResultsForResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_QueryResourceGroupScopeNextLink.json
     */
    /**
     * Sample code: Query latest at resource group scope with next link.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryLatestAtResourceGroupScopeWithNextLink(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .listQueryResultsForResourceGroup(PolicyStatesResource.LATEST, "fffedd8f-ffff-fffd-fffd-fffed2f84852",
                "myResourceGroup", null, null, null, null, null, null, null, "WpmWfBSvPhkAK6QD",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_QueryResourceGroupScope.json
     */
    /**
     * Sample code: Query latest at resource group scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        queryLatestAtResourceGroupScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .listQueryResultsForResourceGroup(PolicyStatesResource.LATEST, "fffedd8f-ffff-fffd-fffd-fffed2f84852",
                "myResourceGroup", null, null, null, null, null, null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicyStates_ListQueryResultsForResourceGroupLevelPolicyAssignment

```java
import com.azure.resourcemanager.policyinsights.models.PolicyStatesResource;

/**
 * Samples for PolicyStates ListQueryResultsForResourceGroupLevelPolicyAssignment.
 */
public final class PolicyStatesListQueryResultsForResourceGroupLevelPolicyAssignmentSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_QueryResourceGroupLevelPolicyAssignmentScopeNextLink.json
     */
    /**
     * Sample code: Query latest at resource group level policy assignment scope with next link.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryLatestAtResourceGroupLevelPolicyAssignmentScopeWithNextLink(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .listQueryResultsForResourceGroupLevelPolicyAssignment(PolicyStatesResource.LATEST,
                "fffedd8f-ffff-fffd-fffd-fffed2f84852", "myResourceGroup", "myPolicyAssignment", null, null, null, null,
                null, null, null, "WpmWfBSvPhkAK6QD", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_QueryResourceGroupLevelPolicyAssignmentScope.json
     */
    /**
     * Sample code: Query latest at resource group level policy assignment scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryLatestAtResourceGroupLevelPolicyAssignmentScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .listQueryResultsForResourceGroupLevelPolicyAssignment(PolicyStatesResource.LATEST,
                "fffedd8f-ffff-fffd-fffd-fffed2f84852", "myResourceGroup", "myPolicyAssignment", null, null, null, null,
                null, null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicyStates_ListQueryResultsForSubscription

```java
import com.azure.resourcemanager.policyinsights.models.PolicyStatesResource;
import java.time.OffsetDateTime;

/**
 * Samples for PolicyStates ListQueryResultsForSubscription.
 */
public final class PolicyStatesListQueryResultsForSubscriptionSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_QuerySubscriptionScope.json
     */
    /**
     * Sample code: Query latest at subscription scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        queryLatestAtSubscriptionScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .listQueryResultsForSubscription(PolicyStatesResource.LATEST, "fffedd8f-ffff-fffd-fffd-fffed2f84852", null,
                null, null, null, null, null, null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_QuerySubscriptionScopeNextLink.json
     */
    /**
     * Sample code: Query latest at subscription scope with next link.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryLatestAtSubscriptionScopeWithNextLink(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .listQueryResultsForSubscription(PolicyStatesResource.LATEST, "fffedd8f-ffff-fffd-fffd-fffed2f84852", null,
                null, null, null, null, null, null, "WpmWfBSvPhkAK6QD", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_TimeRangeSortSelectTop.json
     */
    /**
     * Sample code: Time range; sort, select and limit.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        timeRangeSortSelectAndLimit(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .listQueryResultsForSubscription(PolicyStatesResource.LATEST, "fffedd8f-ffff-fffd-fffd-fffed2f84852", 2,
                "Timestamp desc, PolicyAssignmentId asc, SubscriptionId asc, ResourceGroup asc, ResourceId",
                "Timestamp, PolicyAssignmentId, PolicyDefinitionId, SubscriptionId, ResourceGroup, ResourceId, policyDefinitionGroupNames",
                OffsetDateTime.parse("2019-10-05T18:00:00Z"), OffsetDateTime.parse("2019-10-06T18:00:00Z"), null, null,
                null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_FilterAndAggregateOnly.json
     */
    /**
     * Sample code: Filter and aggregate only.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void filterAndAggregateOnly(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .listQueryResultsForSubscription(PolicyStatesResource.LATEST, "fffedd8f-ffff-fffd-fffd-fffed2f84852", null,
                null, null, OffsetDateTime.parse("2019-10-05T18:00:00Z"), null, "PolicyDefinitionAction eq 'deny'",
                "aggregate($count as NumDenyStates)", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_FilterAndGroupByWithoutAggregate.json
     */
    /**
     * Sample code: Filter and group without aggregate.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        filterAndGroupWithoutAggregate(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .listQueryResultsForSubscription(PolicyStatesResource.LATEST, "fffedd8f-ffff-fffd-fffd-fffed2f84852", 2,
                null, null, OffsetDateTime.parse("2019-10-05T18:00:00Z"), null,
                "IsCompliant eq false and (PolicyDefinitionAction ne 'audit' and PolicyDefinitionAction ne 'append')",
                "groupby((PolicyAssignmentId, PolicyDefinitionId, PolicyDefinitionAction, ResourceId))", null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_FilterAndMultipleGroups.json
     */
    /**
     * Sample code: Filter and multiple groups.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void filterAndMultipleGroups(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .listQueryResultsForSubscription(PolicyStatesResource.LATEST, "fffedd8f-ffff-fffd-fffd-fffed2f84852", 10,
                "NumNonCompliantResources desc", null, null, null, "IsCompliant eq false",
                "groupby((PolicyAssignmentId, PolicySetDefinitionId, PolicyDefinitionId, PolicyDefinitionReferenceId, ResourceId))/groupby((PolicyAssignmentId, PolicySetDefinitionId, PolicyDefinitionId, PolicyDefinitionReferenceId), aggregate($count as NumNonCompliantResources))",
                null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_FilterAndGroupByWithAggregate.json
     */
    /**
     * Sample code: Filter and group with aggregate.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        filterAndGroupWithAggregate(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .listQueryResultsForSubscription(PolicyStatesResource.LATEST, "fffedd8f-ffff-fffd-fffd-fffed2f84852", 2,
                "NumAuditDenyNonComplianceRecords desc", null, OffsetDateTime.parse("2019-10-05T18:00:00Z"), null,
                "IsCompliant eq false and (PolicyDefinitionAction eq 'audit' or PolicyDefinitionAction eq 'deny')",
                "groupby((PolicyAssignmentId, PolicyDefinitionId, PolicyDefinitionAction, ResourceId), aggregate($count as NumAuditDenyNonComplianceRecords))",
                null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicyStates_ListQueryResultsForSubscriptionLevelPolicyAssignment

```java
import com.azure.resourcemanager.policyinsights.models.PolicyStatesResource;

/**
 * Samples for PolicyStates ListQueryResultsForSubscriptionLevelPolicyAssignment.
 */
public final class PolicyStatesListQueryResultsForSubscriptionLevelPolicyAssignmentSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_QuerySubscriptionLevelPolicyAssignmentScopeNextLink.json
     */
    /**
     * Sample code: Query latest at subscription level policy assignment scope with next link.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryLatestAtSubscriptionLevelPolicyAssignmentScopeWithNextLink(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .listQueryResultsForSubscriptionLevelPolicyAssignment(PolicyStatesResource.LATEST,
                "fffedd8f-ffff-fffd-fffd-fffed2f84852", "ec8f9645-8ecb-4abb-9c0b-5292f19d4003", null, null, null, null,
                null, null, null, "WpmWfBSvPhkAK6QD", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_QuerySubscriptionLevelPolicyAssignmentScope.json
     */
    /**
     * Sample code: Query latest at subscription level policy assignment scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryLatestAtSubscriptionLevelPolicyAssignmentScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .listQueryResultsForSubscriptionLevelPolicyAssignment(PolicyStatesResource.LATEST,
                "fffedd8f-ffff-fffd-fffd-fffed2f84852", "ec8f9645-8ecb-4abb-9c0b-5292f19d4003", null, null, null, null,
                null, null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicyStates_SummarizeForManagementGroup

```java
import com.azure.resourcemanager.policyinsights.models.PolicyStatesSummaryResourceType;
import java.time.OffsetDateTime;

/**
 * Samples for PolicyStates SummarizeForManagementGroup.
 */
public final class PolicyStatesSummarizeForManagementGroupSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_SummarizeManagementGroupScope.json
     */
    /**
     * Sample code: Summarize at management group scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        summarizeAtManagementGroupScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .summarizeForManagementGroupWithResponse(PolicyStatesSummaryResourceType.LATEST, "myManagementGroup", 0,
                OffsetDateTime.parse("2019-10-05T18:00:00Z"), OffsetDateTime.parse("2019-10-06T18:00:00Z"),
                "PolicyDefinitionAction eq 'deny' or PolicyDefinitionAction eq 'audit'",
                com.azure.core.util.Context.NONE);
    }
}
```

### PolicyStates_SummarizeForPolicyDefinition

```java
import com.azure.resourcemanager.policyinsights.models.PolicyStatesSummaryResourceType;

/**
 * Samples for PolicyStates SummarizeForPolicyDefinition.
 */
public final class PolicyStatesSummarizeForPolicyDefinitionSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_SummarizeSubscriptionLevelPolicyDefinitionScope.json
     */
    /**
     * Sample code: Summarize at policy definition scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        summarizeAtPolicyDefinitionScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .summarizeForPolicyDefinitionWithResponse(PolicyStatesSummaryResourceType.LATEST,
                "fffedd8f-ffff-fffd-fffd-fffed2f84852", "24813039-7534-408a-9842-eb99f45721b1", null, null, null, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### PolicyStates_SummarizeForPolicySetDefinition

```java
import com.azure.resourcemanager.policyinsights.models.PolicyStatesSummaryResourceType;
import java.time.OffsetDateTime;

/**
 * Samples for PolicyStates SummarizeForPolicySetDefinition.
 */
public final class PolicyStatesSummarizeForPolicySetDefinitionSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_SummarizeSubscriptionLevelPolicySetDefinitionScope.json
     */
    /**
     * Sample code: Summarize at policy set definition scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        summarizeAtPolicySetDefinitionScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .summarizeForPolicySetDefinitionWithResponse(PolicyStatesSummaryResourceType.LATEST,
                "fffedd8f-ffff-fffd-fffd-fffed2f84852", "3e3807c1-65c9-49e0-a406-82d8ae3e338c", 1,
                OffsetDateTime.parse("2019-10-05T18:00:00Z"), OffsetDateTime.parse("2019-10-06T18:00:00Z"),
                "PolicyDefinitionAction eq 'deny'", com.azure.core.util.Context.NONE);
    }
}
```

### PolicyStates_SummarizeForResource

```java
import com.azure.resourcemanager.policyinsights.models.PolicyStatesSummaryResourceType;

/**
 * Samples for PolicyStates SummarizeForResource.
 */
public final class PolicyStatesSummarizeForResourceSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_SummarizeResourceScope.json
     */
    /**
     * Sample code: Summarize at resource scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        summarizeAtResourceScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .summarizeForResourceWithResponse(PolicyStatesSummaryResourceType.LATEST,
                "subscriptions/fff10b27-fff3-fff5-fff8-fffbe01e86a5/resourceGroups/myResourceGroup/providers/Microsoft.KeyVault/vaults/my-vault",
                2, null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicyStates_SummarizeForResourceGroup

```java
import com.azure.resourcemanager.policyinsights.models.PolicyStatesSummaryResourceType;

/**
 * Samples for PolicyStates SummarizeForResourceGroup.
 */
public final class PolicyStatesSummarizeForResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_SummarizeResourceGroupScope.json
     */
    /**
     * Sample code: Summarize at resource group scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        summarizeAtResourceGroupScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .summarizeForResourceGroupWithResponse(PolicyStatesSummaryResourceType.LATEST,
                "fffedd8f-ffff-fffd-fffd-fffed2f84852", "myResourceGroup", null, null, null, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### PolicyStates_SummarizeForResourceGroupLevelPolicyAssignment

```java
import com.azure.resourcemanager.policyinsights.models.PolicyStatesSummaryResourceType;

/**
 * Samples for PolicyStates SummarizeForResourceGroupLevelPolicyAssignment.
 */
public final class PolicyStatesSummarizeForResourceGroupLevelPolicyAssignmentSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_SummarizeResourceGroupLevelPolicyAssignmentScope.json
     */
    /**
     * Sample code: Summarize at policy assignment scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        summarizeAtPolicyAssignmentScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .summarizeForResourceGroupLevelPolicyAssignmentWithResponse(PolicyStatesSummaryResourceType.LATEST,
                "fffedd8f-ffff-fffd-fffd-fffed2f84852", "myResourceGroup", "b7a1ca2596524e3ab19597f2", null, null, null,
                null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicyStates_SummarizeForSubscription

```java
import com.azure.resourcemanager.policyinsights.models.PolicyStatesSummaryResourceType;

/**
 * Samples for PolicyStates SummarizeForSubscription.
 */
public final class PolicyStatesSummarizeForSubscriptionSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_SummarizeSubscriptionScopeForPolicyGroup.json
     */
    /**
     * Sample code: Summarize at subscription scope for a policy definition group.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void summarizeAtSubscriptionScopeForAPolicyDefinitionGroup(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .summarizeForSubscriptionWithResponse(PolicyStatesSummaryResourceType.LATEST,
                "fffedd8f-ffff-fffd-fffd-fffed2f84852", 1, null, null, "'group1' IN PolicyDefinitionGroupNames",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_SummarizeSubscriptionScope.json
     */
    /**
     * Sample code: Summarize at subscription scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        summarizeAtSubscriptionScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .summarizeForSubscriptionWithResponse(PolicyStatesSummaryResourceType.LATEST,
                "fffedd8f-ffff-fffd-fffd-fffed2f84852", 5, null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicyStates_SummarizeForSubscriptionLevelPolicyAssignment

```java
import com.azure.resourcemanager.policyinsights.models.PolicyStatesSummaryResourceType;

/**
 * Samples for PolicyStates SummarizeForSubscriptionLevelPolicyAssignment.
 */
public final class PolicyStatesSummarizeForSubscriptionLevelPolicyAssignmentSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_SummarizeSubscriptionLevelPolicyAssignmentScope.json
     */
    /**
     * Sample code: Summarize at policy assignment scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        summarizeAtPolicyAssignmentScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .summarizeForSubscriptionLevelPolicyAssignmentWithResponse(PolicyStatesSummaryResourceType.LATEST,
                "fffedd8f-ffff-fffd-fffd-fffed2f84852", "ec8f9645-8ecb-4abb-9c0b-5292f19d4003", null, null, null, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### PolicyStates_TriggerResourceGroupEvaluation

```java
/**
 * Samples for PolicyStates TriggerResourceGroupEvaluation.
 */
public final class PolicyStatesTriggerResourceGroupEvaluationSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_TriggerResourceGroupEvaluation.json
     */
    /**
     * Sample code: Trigger evaluations for all resources in a resource group.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void triggerEvaluationsForAllResourcesInAResourceGroup(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .triggerResourceGroupEvaluation("fffedd8f-ffff-fffd-fffd-fffed2f84852", "myResourceGroup",
                com.azure.core.util.Context.NONE);
    }
}
```

### PolicyStates_TriggerSubscriptionEvaluation

```java
/**
 * Samples for PolicyStates TriggerSubscriptionEvaluation.
 */
public final class PolicyStatesTriggerSubscriptionEvaluationSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * PolicyStates_TriggerSubscriptionEvaluation.json
     */
    /**
     * Sample code: Trigger evaluations for all resources in a subscription.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void triggerEvaluationsForAllResourcesInASubscription(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyStates()
            .triggerSubscriptionEvaluation("fffedd8f-ffff-fffd-fffd-fffed2f84852", com.azure.core.util.Context.NONE);
    }
}
```

### PolicyTrackedResources_ListQueryResultsForManagementGroup

```java
import com.azure.resourcemanager.policyinsights.models.PolicyTrackedResourcesResourceType;

/**
 * Samples for PolicyTrackedResources ListQueryResultsForManagementGroup.
 */
public final class PolicyTrackedResourcesListQueryResultsForManagementGroupSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/preview/2018-07-01-preview/examples/
     * PolicyTrackedResources_QueryManagementGroupScopeWithFilterAndTop.json
     */
    /**
     * Sample code: Query at management group scope using query parameters.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryAtManagementGroupScopeUsingQueryParameters(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyTrackedResources()
            .listQueryResultsForManagementGroup("myManagementGroup", PolicyTrackedResourcesResourceType.DEFAULT, 1,
                "PolicyAssignmentId eq '/subscriptions/fff8dfdb-fff3-fff0-fff4-fffdcbe6b2ef/resourceGroups/myResourceGroup/providers/Microsoft.Authorization/policyAssignments/myPolicyAssignment' AND TrackedResourceId eq '/subscriptions/fff8dfdb-fff3-fff0-fff4-fffdcbe6b2ef/resourceGroups/myResourceGroup/providers/Microsoft.Example/exampleResourceType/exampleTrackedResourceName'",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/preview/2018-07-01-preview/examples/
     * PolicyTrackedResources_QueryManagementGroupScope.json
     */
    /**
     * Sample code: Query at management group scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        queryAtManagementGroupScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyTrackedResources()
            .listQueryResultsForManagementGroup("myManagementGroup", PolicyTrackedResourcesResourceType.DEFAULT, null,
                null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicyTrackedResources_ListQueryResultsForResource

```java
import com.azure.resourcemanager.policyinsights.models.PolicyTrackedResourcesResourceType;

/**
 * Samples for PolicyTrackedResources ListQueryResultsForResource.
 */
public final class PolicyTrackedResourcesListQueryResultsForResourceSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/preview/2018-07-01-preview/examples/
     * PolicyTrackedResources_QueryResourceScopeWithFilterAndTop.json
     */
    /**
     * Sample code: Query at resource scope using query parameters.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryAtResourceScopeUsingQueryParameters(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyTrackedResources()
            .listQueryResultsForResource(
                "subscriptions/fff8dfdb-fff3-fff0-fff4-fffdcbe6b2ef/resourceGroups/myResourceGroup/providers/Microsoft.Example/exampleResourceType/myResource",
                PolicyTrackedResourcesResourceType.DEFAULT, 1,
                "PolicyAssignmentId eq '/subscriptions/fff8dfdb-fff3-fff0-fff4-fffdcbe6b2ef/resourceGroups/myResourceGroup/providers/Microsoft.Authorization/policyAssignments/myPolicyAssignment' AND TrackedResourceId eq '/subscriptions/fff8dfdb-fff3-fff0-fff4-fffdcbe6b2ef/resourceGroups/myResourceGroup/providers/Microsoft.Example/exampleResourceType/myResource/nestedResourceType/TrackedResource1'",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/preview/2018-07-01-preview/examples/
     * PolicyTrackedResources_QueryResourceScope.json
     */
    /**
     * Sample code: Query at resource scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryAtResourceScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyTrackedResources()
            .listQueryResultsForResource(
                "subscriptions/fff8dfdb-fff3-fff0-fff4-fffdcbe6b2ef/resourceGroups/myResourceGroup/providers/Microsoft.Example/exampleResourceType/myResource",
                PolicyTrackedResourcesResourceType.DEFAULT, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicyTrackedResources_ListQueryResultsForResourceGroup

```java
import com.azure.resourcemanager.policyinsights.models.PolicyTrackedResourcesResourceType;

/**
 * Samples for PolicyTrackedResources ListQueryResultsForResourceGroup.
 */
public final class PolicyTrackedResourcesListQueryResultsForResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/preview/2018-07-01-preview/examples/
     * PolicyTrackedResources_QueryResourceGroupScopeWithFilterAndTop.json
     */
    /**
     * Sample code: Query at resource group scope using query parameters.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryAtResourceGroupScopeUsingQueryParameters(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyTrackedResources()
            .listQueryResultsForResourceGroup("myResourceGroup", PolicyTrackedResourcesResourceType.DEFAULT, 1,
                "PolicyAssignmentId eq '/subscriptions/fff8dfdb-fff3-fff0-fff4-fffdcbe6b2ef/resourceGroups/myResourceGroup/providers/Microsoft.Authorization/policyAssignments/myPolicyAssignment' AND TrackedResourceId eq '/subscriptions/fff8dfdb-fff3-fff0-fff4-fffdcbe6b2ef/resourceGroups/myResourceGroup/providers/Microsoft.Example/exampleResourceType/myResource/nestedResourceType/TrackedResource1'",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/preview/2018-07-01-preview/examples/
     * PolicyTrackedResources_QueryResourceGroupScope.json
     */
    /**
     * Sample code: Query at resource group scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        queryAtResourceGroupScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyTrackedResources()
            .listQueryResultsForResourceGroup("myResourceGroup", PolicyTrackedResourcesResourceType.DEFAULT, null, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### PolicyTrackedResources_ListQueryResultsForSubscription

```java
import com.azure.resourcemanager.policyinsights.models.PolicyTrackedResourcesResourceType;

/**
 * Samples for PolicyTrackedResources ListQueryResultsForSubscription.
 */
public final class PolicyTrackedResourcesListQueryResultsForSubscriptionSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/preview/2018-07-01-preview/examples/
     * PolicyTrackedResources_QuerySubscriptionScopeWithFilterAndTop.json
     */
    /**
     * Sample code: Query at subscription scope using query parameters.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void queryAtSubscriptionScopeUsingQueryParameters(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyTrackedResources()
            .listQueryResultsForSubscription(PolicyTrackedResourcesResourceType.DEFAULT, 1,
                "PolicyAssignmentId eq '/subscriptions/fff8dfdb-fff3-fff0-fff4-fffdcbe6b2ef/resourceGroups/myResourceGroup/providers/Microsoft.Authorization/policyAssignments/myPolicyAssignment' AND TrackedResourceId eq '/subscriptions/fff8dfdb-fff3-fff0-fff4-fffdcbe6b2ef/resourceGroups/myResourceGroup/providers/Microsoft.Example/exampleResourceType/exampleTrackedResourceName'",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/preview/2018-07-01-preview/examples/
     * PolicyTrackedResources_QuerySubscriptionScope.json
     */
    /**
     * Sample code: Query at subscription scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        queryAtSubscriptionScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.policyTrackedResources()
            .listQueryResultsForSubscription(PolicyTrackedResourcesResourceType.DEFAULT, null, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Remediations_CancelAtManagementGroup

```java
/**
 * Samples for Remediations CancelAtManagementGroup.
 */
public final class RemediationsCancelAtManagementGroupSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_CancelManagementGroupScope.json
     */
    /**
     * Sample code: Cancel a remediation at management group scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void cancelARemediationAtManagementGroupScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations()
            .cancelAtManagementGroupWithResponse("financeMg", "myRemediation", com.azure.core.util.Context.NONE);
    }
}
```

### Remediations_CancelAtResource

```java
/**
 * Samples for Remediations CancelAtResource.
 */
public final class RemediationsCancelAtResourceSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_CancelResourceScope.json
     */
    /**
     * Sample code: Cancel a remediation at individual resource scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void cancelARemediationAtIndividualResourceScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations()
            .cancelAtResourceWithResponse(
                "subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/resourcegroups/myResourceGroup/providers/microsoft.storage/storageaccounts/storAc1",
                "myRemediation", com.azure.core.util.Context.NONE);
    }
}
```

### Remediations_CancelAtResourceGroup

```java
/**
 * Samples for Remediations CancelAtResourceGroup.
 */
public final class RemediationsCancelAtResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_CancelResourceGroupScope.json
     */
    /**
     * Sample code: Cancel a remediation at resource group scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        cancelARemediationAtResourceGroupScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations()
            .cancelAtResourceGroupWithResponse("myResourceGroup", "myRemediation", com.azure.core.util.Context.NONE);
    }
}
```

### Remediations_CancelAtSubscription

```java
/**
 * Samples for Remediations CancelAtSubscription.
 */
public final class RemediationsCancelAtSubscriptionSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_CancelSubscriptionScope.json
     */
    /**
     * Sample code: Cancel a remediation at subscription scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        cancelARemediationAtSubscriptionScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations().cancelAtSubscriptionWithResponse("myRemediation", com.azure.core.util.Context.NONE);
    }
}
```

### Remediations_CreateOrUpdateAtManagementGroup

```java
import com.azure.resourcemanager.policyinsights.fluent.models.RemediationInner;

/**
 * Samples for Remediations CreateOrUpdateAtManagementGroup.
 */
public final class RemediationsCreateOrUpdateAtManagementGroupSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_CreateManagementGroupScope.json
     */
    /**
     * Sample code: Create remediation at management group scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void createRemediationAtManagementGroupScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations()
            .createOrUpdateAtManagementGroupWithResponse("financeMg", "storageRemediation",
                new RemediationInner().withPolicyAssignmentId(
                    "/providers/microsoft.management/managementGroups/financeMg/providers/microsoft.authorization/policyassignments/b101830944f246d8a14088c5"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Remediations_CreateOrUpdateAtResource

```java
import com.azure.resourcemanager.policyinsights.fluent.models.RemediationInner;

/**
 * Samples for Remediations CreateOrUpdateAtResource.
 */
public final class RemediationsCreateOrUpdateAtResourceSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_CreateResourceScope.json
     */
    /**
     * Sample code: Create remediation at individual resource scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void createRemediationAtIndividualResourceScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations()
            .createOrUpdateAtResourceWithResponse(
                "subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/resourcegroups/myResourceGroup/providers/microsoft.storage/storageaccounts/storAc1",
                "storageRemediation",
                new RemediationInner().withPolicyAssignmentId(
                    "/subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/resourceGroups/myResourceGroup/providers/microsoft.authorization/policyassignments/b101830944f246d8a14088c5"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Remediations_CreateOrUpdateAtResourceGroup

```java
/**
 * Samples for Remediations CreateOrUpdateAtResourceGroup.
 */
public final class RemediationsCreateOrUpdateAtResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_CreateResourceGroupScope.json
     */
    /**
     * Sample code: Create remediation at resource group scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        createRemediationAtResourceGroupScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations()
            .define("storageRemediation")
            .withExistingResourceGroup("myResourceGroup")
            .withPolicyAssignmentId(
                "/subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/resourceGroups/myResourceGroup/providers/microsoft.authorization/policyassignments/b101830944f246d8a14088c5")
            .create();
    }
}
```

### Remediations_CreateOrUpdateAtSubscription

```java
import com.azure.resourcemanager.policyinsights.fluent.models.RemediationInner;
import com.azure.resourcemanager.policyinsights.models.RemediationFilters;
import com.azure.resourcemanager.policyinsights.models.RemediationPropertiesFailureThreshold;
import com.azure.resourcemanager.policyinsights.models.ResourceDiscoveryMode;
import java.util.Arrays;

/**
 * Samples for Remediations CreateOrUpdateAtSubscription.
 */
public final class RemediationsCreateOrUpdateAtSubscriptionSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_CreateSubscriptionScope.json
     */
    /**
     * Sample code: Create remediation at subscription scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        createRemediationAtSubscriptionScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations()
            .createOrUpdateAtSubscriptionWithResponse("storageRemediation",
                new RemediationInner().withPolicyAssignmentId(
                    "/subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/providers/microsoft.authorization/policyassignments/b101830944f246d8a14088c5"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_CreateSubscriptionScope_AllProperties.json
     */
    /**
     * Sample code: Create remediation at subscription scope with all properties.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void createRemediationAtSubscriptionScopeWithAllProperties(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations()
            .createOrUpdateAtSubscriptionWithResponse("storageRemediation",
                new RemediationInner().withPolicyAssignmentId(
                    "/subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/providers/microsoft.authorization/policyassignments/b101830944f246d8a14088c5")
                    .withPolicyDefinitionReferenceId("8c8fa9e4")
                    .withResourceDiscoveryMode(ResourceDiscoveryMode.RE_EVALUATE_COMPLIANCE)
                    .withFilters(new RemediationFilters().withLocations(Arrays.asList("eastus", "westus")))
                    .withResourceCount(42)
                    .withParallelDeployments(6)
                    .withFailureThreshold(new RemediationPropertiesFailureThreshold().withPercentage(0.1F)),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_CreateSubscriptionScope_ResourceIdsFilter.json
     */
    /**
     * Sample code: Create remediation at subscription scope with resourceIds filter.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void createRemediationAtSubscriptionScopeWithResourceIdsFilter(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations()
            .createOrUpdateAtSubscriptionWithResponse("storageRemediation", new RemediationInner()
                .withPolicyAssignmentId(
                    "/subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/providers/microsoft.authorization/policyassignments/b101830944f246d8a14088c5")
                .withPolicyDefinitionReferenceId("8c8fa9e4")
                .withResourceDiscoveryMode(ResourceDiscoveryMode.EXISTING_NON_COMPLIANT)
                .withFilters(new RemediationFilters().withLocations(Arrays.asList("eastus", "westus"))
                    .withResourceIds(Arrays.asList(
                        "/subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/resourceGroups/res2627/providers/Microsoft.Storage/storageAccounts/sto1125",
                        "/subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/resourceGroups/testcmk3/providers/Microsoft.Storage/storageAccounts/sto3699",
                        "/subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/resourceGroups/res9407/providers/Microsoft.Storage/storageAccounts/sto8596",
                        "/subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/resourceGroups/testcmk3/providers/Microsoft.Storage/storageAccounts/sto6637",
                        "/subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/resourceGroups/res8186/providers/Microsoft.Storage/storageAccounts/sto834",
                        "/subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/resourceGroups/testcmk3/providers/Microsoft.Storage/storageAccounts/sto9174")))
                .withResourceCount(42)
                .withParallelDeployments(6)
                .withFailureThreshold(new RemediationPropertiesFailureThreshold().withPercentage(0.1F)),
                com.azure.core.util.Context.NONE);
    }
}
```

### Remediations_Delete

```java
/**
 * Samples for Remediations Delete.
 */
public final class RemediationsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_DeleteResourceGroupScope.json
     */
    /**
     * Sample code: Delete remediation at resource group scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        deleteRemediationAtResourceGroupScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations()
            .deleteByResourceGroupWithResponse("myResourceGroup", "storageRemediation",
                com.azure.core.util.Context.NONE);
    }
}
```

### Remediations_DeleteAtManagementGroup

```java
/**
 * Samples for Remediations DeleteAtManagementGroup.
 */
public final class RemediationsDeleteAtManagementGroupSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_DeleteManagementGroupScope.json
     */
    /**
     * Sample code: Delete remediation at management group scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void deleteRemediationAtManagementGroupScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations()
            .deleteAtManagementGroupWithResponse("financeMg", "storageRemediation", com.azure.core.util.Context.NONE);
    }
}
```

### Remediations_DeleteAtResource

```java
/**
 * Samples for Remediations DeleteAtResource.
 */
public final class RemediationsDeleteAtResourceSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_DeleteResourceScope.json
     */
    /**
     * Sample code: Delete remediation at individual resource scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void deleteRemediationAtIndividualResourceScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations()
            .deleteAtResourceWithResponse(
                "subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/resourcegroups/myResourceGroup/providers/microsoft.storage/storageaccounts/storAc1",
                "storageRemediation", com.azure.core.util.Context.NONE);
    }
}
```

### Remediations_DeleteAtSubscription

```java
/**
 * Samples for Remediations DeleteAtSubscription.
 */
public final class RemediationsDeleteAtSubscriptionSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_DeleteSubscriptionScope.json
     */
    /**
     * Sample code: Delete remediation at subscription scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        deleteRemediationAtSubscriptionScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations().deleteAtSubscriptionWithResponse("storageRemediation", com.azure.core.util.Context.NONE);
    }
}
```

### Remediations_GetAtManagementGroup

```java
/**
 * Samples for Remediations GetAtManagementGroup.
 */
public final class RemediationsGetAtManagementGroupSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_GetManagementGroupScope.json
     */
    /**
     * Sample code: Get remediation at management group scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        getRemediationAtManagementGroupScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations()
            .getAtManagementGroupWithResponse("financeMg", "storageRemediation", com.azure.core.util.Context.NONE);
    }
}
```

### Remediations_GetAtResource

```java
/**
 * Samples for Remediations GetAtResource.
 */
public final class RemediationsGetAtResourceSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_GetResourceScope.json
     */
    /**
     * Sample code: Get remediation at individual resource scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void getRemediationAtIndividualResourceScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations()
            .getAtResourceWithResponse(
                "subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/resourcegroups/myResourceGroup/providers/microsoft.storage/storageaccounts/storAc1",
                "storageRemediation", com.azure.core.util.Context.NONE);
    }
}
```

### Remediations_GetAtSubscription

```java
/**
 * Samples for Remediations GetAtSubscription.
 */
public final class RemediationsGetAtSubscriptionSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_GetSubscriptionScope.json
     */
    /**
     * Sample code: Get remediation at subscription scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        getRemediationAtSubscriptionScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations().getAtSubscriptionWithResponse("storageRemediation", com.azure.core.util.Context.NONE);
    }
}
```

### Remediations_GetByResourceGroup

```java
/**
 * Samples for Remediations GetByResourceGroup.
 */
public final class RemediationsGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_GetResourceGroupScope.json
     */
    /**
     * Sample code: Get remediation at resource group scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        getRemediationAtResourceGroupScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations()
            .getByResourceGroupWithResponse("myResourceGroup", "storageRemediation", com.azure.core.util.Context.NONE);
    }
}
```

### Remediations_List

```java
/**
 * Samples for Remediations List.
 */
public final class RemediationsListSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_ListSubscriptionScope_WithQuery.json
     */
    /**
     * Sample code: List remediations at subscription scope with query parameters.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void listRemediationsAtSubscriptionScopeWithQueryParameters(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations()
            .list(1,
                "PolicyAssignmentId eq '/subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/providers/microsoft.authorization/policyassignments/b101830944f246d8a14088c5' AND PolicyDefinitionReferenceId eq 'storageSkuDef'",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_ListSubscriptionScope.json
     */
    /**
     * Sample code: List remediations at subscription scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        listRemediationsAtSubscriptionScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations().list(null, null, com.azure.core.util.Context.NONE);
    }
}
```

### Remediations_ListByResourceGroup

```java
/**
 * Samples for Remediations ListByResourceGroup.
 */
public final class RemediationsListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_ListResourceGroupScope_WithQuery.json
     */
    /**
     * Sample code: List remediations at resource group scope with query parameters.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void listRemediationsAtResourceGroupScopeWithQueryParameters(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations()
            .listByResourceGroup("myResourceGroup", 1,
                "PolicyAssignmentId eq '/subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/resourceGroups/myResourceGroup/providers/microsoft.authorization/policyassignments/b101830944f246d8a14088c5'",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_ListResourceGroupScope.json
     */
    /**
     * Sample code: List remediations at resource group scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        listRemediationsAtResourceGroupScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations().listByResourceGroup("myResourceGroup", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### Remediations_ListDeploymentsAtManagementGroup

```java
/**
 * Samples for Remediations ListDeploymentsAtManagementGroup.
 */
public final class RemediationsListDeploymentsAtManagementGroupSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_ListDeploymentsManagementGroupScope.json
     */
    /**
     * Sample code: List deployments for a remediation at management group scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void listDeploymentsForARemediationAtManagementGroupScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations()
            .listDeploymentsAtManagementGroup("financeMg", "myRemediation", null, com.azure.core.util.Context.NONE);
    }
}
```

### Remediations_ListDeploymentsAtResource

```java
/**
 * Samples for Remediations ListDeploymentsAtResource.
 */
public final class RemediationsListDeploymentsAtResourceSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_ListDeploymentsResourceScope.json
     */
    /**
     * Sample code: List deployments for a remediation at individual resource scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void listDeploymentsForARemediationAtIndividualResourceScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations()
            .listDeploymentsAtResource(
                "subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/resourcegroups/myResourceGroup/providers/microsoft.storage/storageaccounts/storAc1",
                "myRemediation", null, com.azure.core.util.Context.NONE);
    }
}
```

### Remediations_ListDeploymentsAtResourceGroup

```java
/**
 * Samples for Remediations ListDeploymentsAtResourceGroup.
 */
public final class RemediationsListDeploymentsAtResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_ListDeploymentsResourceGroupScope.json
     */
    /**
     * Sample code: List deployments for a remediation at resource group scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void listDeploymentsForARemediationAtResourceGroupScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations()
            .listDeploymentsAtResourceGroup("myResourceGroup", "myRemediation", null, com.azure.core.util.Context.NONE);
    }
}
```

### Remediations_ListDeploymentsAtSubscription

```java
/**
 * Samples for Remediations ListDeploymentsAtSubscription.
 */
public final class RemediationsListDeploymentsAtSubscriptionSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_ListDeploymentsSubscriptionScope.json
     */
    /**
     * Sample code: List deployments for a remediation at subscription scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void listDeploymentsForARemediationAtSubscriptionScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations().listDeploymentsAtSubscription("myRemediation", null, com.azure.core.util.Context.NONE);
    }
}
```

### Remediations_ListForManagementGroup

```java
/**
 * Samples for Remediations ListForManagementGroup.
 */
public final class RemediationsListForManagementGroupSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_ListManagementGroupScope.json
     */
    /**
     * Sample code: List remediations at management group scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void
        listRemediationsAtManagementGroupScope(com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations().listForManagementGroup("financeMg", null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_ListManagementGroupScope_WithQuery.json
     */
    /**
     * Sample code: List remediations at management group scope with query parameters.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void listRemediationsAtManagementGroupScopeWithQueryParameters(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations()
            .listForManagementGroup("financeMg", 1,
                "PolicyAssignmentId eq '/providers/microsoft.management/managementGroups/financeMg/providers/microsoft.authorization/policyassignments/b101830944f246d8a14088c5'",
                com.azure.core.util.Context.NONE);
    }
}
```

### Remediations_ListForResource

```java
/**
 * Samples for Remediations ListForResource.
 */
public final class RemediationsListForResourceSamples {
    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_ListResourceScope.json
     */
    /**
     * Sample code: List remediations at individual resource scope.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void listRemediationsAtIndividualResourceScope(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations()
            .listForResource(
                "subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/resourcegroups/myResourceGroup/providers/microsoft.storage/storageaccounts/storAc1",
                null, null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/policyinsights/resource-manager/Microsoft.PolicyInsights/stable/2024-10-01/examples/
     * Remediations_ListResourceScope_WithQuery.json
     */
    /**
     * Sample code: List remediations at individual resource scope with query parameters.
     * 
     * @param manager Entry point to PolicyInsightsManager.
     */
    public static void listRemediationsAtIndividualResourceScopeWithQueryParameters(
        com.azure.resourcemanager.policyinsights.PolicyInsightsManager manager) {
        manager.remediations()
            .listForResource(
                "subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/resourcegroups/myResourceGroup/providers/microsoft.storage/storageaccounts/storAc1",
                1,
                "PolicyAssignmentId eq '/subscriptions/35ee058e-5fa0-414c-8145-3ebb8d09b6e2/providers/microsoft.authorization/policyassignments/b101830944f246d8a14088c5'",
                com.azure.core.util.Context.NONE);
    }
}
```

