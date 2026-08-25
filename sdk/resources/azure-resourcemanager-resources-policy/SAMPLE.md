# Code snippets and samples


## DataPolicyManifests

- [GetByPolicyMode](#datapolicymanifests_getbypolicymode)
- [List](#datapolicymanifests_list)

## PolicyAssignments

- [Create](#policyassignments_create)
- [Delete](#policyassignments_delete)
- [Get](#policyassignments_get)
- [List](#policyassignments_list)
- [ListByResourceGroup](#policyassignments_listbyresourcegroup)
- [ListForManagementGroup](#policyassignments_listformanagementgroup)
- [ListForResource](#policyassignments_listforresource)
- [Update](#policyassignments_update)

## PolicyDefinitionVersions

- [CreateOrUpdate](#policydefinitionversions_createorupdate)
- [CreateOrUpdateAtManagementGroup](#policydefinitionversions_createorupdateatmanagementgroup)
- [Delete](#policydefinitionversions_delete)
- [DeleteAtManagementGroup](#policydefinitionversions_deleteatmanagementgroup)
- [Get](#policydefinitionversions_get)
- [GetAtManagementGroup](#policydefinitionversions_getatmanagementgroup)
- [GetBuiltIn](#policydefinitionversions_getbuiltin)
- [List](#policydefinitionversions_list)
- [ListAll](#policydefinitionversions_listall)
- [ListAllAtManagementGroup](#policydefinitionversions_listallatmanagementgroup)
- [ListAllBuiltins](#policydefinitionversions_listallbuiltins)
- [ListBuiltIn](#policydefinitionversions_listbuiltin)
- [ListByManagementGroup](#policydefinitionversions_listbymanagementgroup)

## PolicyDefinitions

- [CreateOrUpdate](#policydefinitions_createorupdate)
- [CreateOrUpdateAtManagementGroup](#policydefinitions_createorupdateatmanagementgroup)
- [Delete](#policydefinitions_delete)
- [DeleteAtManagementGroup](#policydefinitions_deleteatmanagementgroup)
- [Get](#policydefinitions_get)
- [GetAtManagementGroup](#policydefinitions_getatmanagementgroup)
- [GetBuiltIn](#policydefinitions_getbuiltin)
- [List](#policydefinitions_list)
- [ListBuiltIn](#policydefinitions_listbuiltin)
- [ListByManagementGroup](#policydefinitions_listbymanagementgroup)

## PolicyEnrollments

- [CreateOrUpdate](#policyenrollments_createorupdate)
- [Delete](#policyenrollments_delete)
- [Get](#policyenrollments_get)
- [List](#policyenrollments_list)
- [ListByResourceGroup](#policyenrollments_listbyresourcegroup)
- [ListForManagementGroup](#policyenrollments_listformanagementgroup)
- [ListForResource](#policyenrollments_listforresource)
- [Update](#policyenrollments_update)

## PolicyExemptions

- [CreateOrUpdate](#policyexemptions_createorupdate)
- [Delete](#policyexemptions_delete)
- [Get](#policyexemptions_get)
- [List](#policyexemptions_list)
- [ListByResourceGroup](#policyexemptions_listbyresourcegroup)
- [ListForManagementGroup](#policyexemptions_listformanagementgroup)
- [ListForResource](#policyexemptions_listforresource)
- [Update](#policyexemptions_update)

## PolicySetDefinitionVersions

- [CreateOrUpdate](#policysetdefinitionversions_createorupdate)
- [CreateOrUpdateAtManagementGroup](#policysetdefinitionversions_createorupdateatmanagementgroup)
- [Delete](#policysetdefinitionversions_delete)
- [DeleteAtManagementGroup](#policysetdefinitionversions_deleteatmanagementgroup)
- [Get](#policysetdefinitionversions_get)
- [GetAtManagementGroup](#policysetdefinitionversions_getatmanagementgroup)
- [GetBuiltIn](#policysetdefinitionversions_getbuiltin)
- [List](#policysetdefinitionversions_list)
- [ListAll](#policysetdefinitionversions_listall)
- [ListAllAtManagementGroup](#policysetdefinitionversions_listallatmanagementgroup)
- [ListAllBuiltins](#policysetdefinitionversions_listallbuiltins)
- [ListBuiltIn](#policysetdefinitionversions_listbuiltin)
- [ListByManagementGroup](#policysetdefinitionversions_listbymanagementgroup)

## PolicySetDefinitions

- [CreateOrUpdate](#policysetdefinitions_createorupdate)
- [CreateOrUpdateAtManagementGroup](#policysetdefinitions_createorupdateatmanagementgroup)
- [Delete](#policysetdefinitions_delete)
- [DeleteAtManagementGroup](#policysetdefinitions_deleteatmanagementgroup)
- [Get](#policysetdefinitions_get)
- [GetAtManagementGroup](#policysetdefinitions_getatmanagementgroup)
- [GetBuiltIn](#policysetdefinitions_getbuiltin)
- [List](#policysetdefinitions_list)
- [ListBuiltIn](#policysetdefinitions_listbuiltin)
- [ListByManagementGroup](#policysetdefinitions_listbymanagementgroup)

## PolicyTokens

- [Acquire](#policytokens_acquire)
- [AcquireAtManagementGroup](#policytokens_acquireatmanagementgroup)

## VariableValues

- [CreateOrUpdate](#variablevalues_createorupdate)
- [CreateOrUpdateAtManagementGroup](#variablevalues_createorupdateatmanagementgroup)
- [Delete](#variablevalues_delete)
- [DeleteAtManagementGroup](#variablevalues_deleteatmanagementgroup)
- [Get](#variablevalues_get)
- [GetAtManagementGroup](#variablevalues_getatmanagementgroup)
- [List](#variablevalues_list)
- [ListForManagementGroup](#variablevalues_listformanagementgroup)

## Variables

- [CreateOrUpdate](#variables_createorupdate)
- [CreateOrUpdateAtManagementGroup](#variables_createorupdateatmanagementgroup)
- [Delete](#variables_delete)
- [DeleteAtManagementGroup](#variables_deleteatmanagementgroup)
- [Get](#variables_get)
- [GetAtManagementGroup](#variables_getatmanagementgroup)
- [List](#variables_list)
- [ListForManagementGroup](#variables_listformanagementgroup)
### DataPolicyManifests_GetByPolicyMode

```java
/**
 * Samples for DataPolicyManifests GetByPolicyMode.
 */
public final class DataPolicyManifestsGetByPolicyModeSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/getDataPolicyManifest.json
     */
    /**
     * Sample code: Retrieve a data policy manifest by policy mode.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        retrieveADataPolicyManifestByPolicyMode(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.dataPolicyManifests()
            .getByPolicyModeWithResponse("Microsoft.KeyVault.Data", com.azure.core.util.Context.NONE);
    }
}
```

### DataPolicyManifests_List

```java
/**
 * Samples for DataPolicyManifests List.
 */
public final class DataPolicyManifestsListSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listDataPolicyManifests.json
     */
    /**
     * Sample code: List data policy manifests.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void listDataPolicyManifests(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.dataPolicyManifests().list(null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-01-01-preview/listDataPolicyManifestsNamespaceFilter.json
     */
    /**
     * Sample code: List data policy manifests with namespace filter.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        listDataPolicyManifestsWithNamespaceFilter(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.dataPolicyManifests().list("namespace eq 'Microsoft.KeyVault'", com.azure.core.util.Context.NONE);
    }
}
```

### PolicyAssignments_Create

```java
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.resources.policy.models.EnforcementMode;
import com.azure.resourcemanager.resources.policy.models.Identity;
import com.azure.resourcemanager.resources.policy.models.NonComplianceMessage;
import com.azure.resourcemanager.resources.policy.models.OverrideKind;
import com.azure.resourcemanager.resources.policy.models.OverrideModel;
import com.azure.resourcemanager.resources.policy.models.ParameterValuesValue;
import com.azure.resourcemanager.resources.policy.models.ResourceIdentityType;
import com.azure.resourcemanager.resources.policy.models.ResourceSelector;
import com.azure.resourcemanager.resources.policy.models.Selector;
import com.azure.resourcemanager.resources.policy.models.SelectorKind;
import com.azure.resourcemanager.resources.policy.models.SelfServeExemptionSettings;
import com.azure.resourcemanager.resources.policy.models.UserAssignedIdentitiesValue;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for PolicyAssignments Create.
 */
public final class PolicyAssignmentsCreateSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/createPolicyAssignmentWithResourceSelectors.json
     */
    /**
     * Sample code: Create or update a policy assignment with resource selectors.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void createOrUpdateAPolicyAssignmentWithResourceSelectors(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyAssignments()
            .define("CostManagement")
            .withExistingScope("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2")
            .withDisplayName("Limit the resource location and resource SKU")
            .withPolicyDefinitionId(
                "/subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/providers/Microsoft.Authorization/policySetDefinitions/CostManagement")
            .withDescription("Limit the resource location and resource SKU")
            .withMetadata(BinaryData.fromBytes("{assignedBy=Special Someone}".getBytes(StandardCharsets.UTF_8)))
            .withResourceSelectors(Arrays.asList(new ResourceSelector().withName("SDPRegions")
                .withSelectors(Arrays.asList(new Selector().withKind(SelectorKind.RESOURCE_LOCATION)
                    .withIn(Arrays.asList("eastus2euap", "centraluseuap"))))))
            .create();
    }

    /*
     * x-ms-original-file: 2026-01-01-preview/createPolicyAssignmentWithEnrollEnforcement.json
     */
    /**
     * Sample code: Create or update a policy assignment to enforce policy effect only on enrolled resources during
     * resource creation or update.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        createOrUpdateAPolicyAssignmentToEnforcePolicyEffectOnlyOnEnrolledResourcesDuringResourceCreationOrUpdate(
            com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyAssignments()
            .define("EnforceNamingEnroll")
            .withExistingScope("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2")
            .withDisplayName("Enforce resource naming rules")
            .withPolicyDefinitionId(
                "/subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/providers/Microsoft.Authorization/policyDefinitions/ResourceNaming")
            .withParameters(
                mapOf("prefix",
                    new ParameterValuesValue()
                        .withValue(BinaryData.fromBytes("DeptA".getBytes(StandardCharsets.UTF_8))),
                    "suffix",
                    new ParameterValuesValue().withValue(BinaryData.fromBytes("-LC".getBytes(StandardCharsets.UTF_8)))))
            .withDescription("Force resource names to begin with given DeptA and end with -LC")
            .withMetadata(BinaryData.fromBytes("{assignedBy=Special Someone}".getBytes(StandardCharsets.UTF_8)))
            .withEnforcementMode(EnforcementMode.ENROLL)
            .create();
    }

    /*
     * x-ms-original-file: 2026-01-01-preview/createPolicyAssignment.json
     */
    /**
     * Sample code: Create or update a policy assignment.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        createOrUpdateAPolicyAssignment(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyAssignments()
            .define("EnforceNaming")
            .withExistingScope("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2")
            .withDisplayName("Enforce resource naming rules")
            .withPolicyDefinitionId(
                "/subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/providers/Microsoft.Authorization/policyDefinitions/ResourceNaming")
            .withParameters(
                mapOf("prefix",
                    new ParameterValuesValue()
                        .withValue(BinaryData.fromBytes("DeptA".getBytes(StandardCharsets.UTF_8))),
                    "suffix",
                    new ParameterValuesValue().withValue(BinaryData.fromBytes("-LC".getBytes(StandardCharsets.UTF_8)))))
            .withDescription("Force resource names to begin with given DeptA and end with -LC")
            .withMetadata(BinaryData.fromBytes("{assignedBy=Special Someone}".getBytes(StandardCharsets.UTF_8)))
            .withNonComplianceMessages(Arrays.asList(
                new NonComplianceMessage().withMessage("Resource names must start with 'DeptA' and end with '-LC'.")))
            .create();
    }

    /*
     * x-ms-original-file: 2026-01-01-preview/createPolicyAssignmentWithIdentity.json
     */
    /**
     * Sample code: Create or update a policy assignment with a system assigned identity.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void createOrUpdateAPolicyAssignmentWithASystemAssignedIdentity(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyAssignments()
            .define("EnforceNaming")
            .withExistingScope("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2")
            .withRegion("eastus")
            .withIdentity(new Identity().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
            .withDisplayName("Enforce resource naming rules")
            .withPolicyDefinitionId(
                "/subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/providers/Microsoft.Authorization/policyDefinitions/ResourceNaming")
            .withParameters(
                mapOf("prefix",
                    new ParameterValuesValue()
                        .withValue(BinaryData.fromBytes("DeptA".getBytes(StandardCharsets.UTF_8))),
                    "suffix",
                    new ParameterValuesValue().withValue(BinaryData.fromBytes("-LC".getBytes(StandardCharsets.UTF_8)))))
            .withDescription("Force resource names to begin with given DeptA and end with -LC")
            .withMetadata(BinaryData.fromBytes("{assignedBy=Foo Bar}".getBytes(StandardCharsets.UTF_8)))
            .withEnforcementMode(EnforcementMode.DEFAULT)
            .create();
    }

    /*
     * x-ms-original-file: 2026-01-01-preview/createPolicyAssignmentWithSelfserveExemptionSettings.json
     */
    /**
     * Sample code: Create or update a policy assignment with self-serve exemption settings.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void createOrUpdateAPolicyAssignmentWithSelfServeExemptionSettings(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyAssignments()
            .define("CostManagement")
            .withExistingScope("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2")
            .withRegion("eastus")
            .withDisplayName("Limit the resource location and resource SKU")
            .withPolicyDefinitionId(
                "/subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/providers/Microsoft.Authorization/policySetDefinitions/CostManagement")
            .withDefinitionVersion("1.*.*")
            .withDescription("Limit the resource location and resource SKU")
            .withMetadata(BinaryData.fromBytes("{assignedBy=Foo Bar}".getBytes(StandardCharsets.UTF_8)))
            .withEnforcementMode(EnforcementMode.DEFAULT)
            .withSelfServeExemptionSettings(new SelfServeExemptionSettings().withEnabled(true)
                .withPolicyDefinitionReferenceIds(Arrays.asList("Limit_Skus")))
            .create();
    }

    /*
     * x-ms-original-file: 2026-01-01-preview/createPolicyAssignmentNonComplianceMessages.json
     */
    /**
     * Sample code: Create or update a policy assignment with multiple non-compliance messages.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void createOrUpdateAPolicyAssignmentWithMultipleNonComplianceMessages(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyAssignments()
            .define("securityInitAssignment")
            .withExistingScope("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2")
            .withDisplayName("Enforce security policies")
            .withPolicyDefinitionId(
                "/subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/providers/Microsoft.Authorization/policySetDefinitions/securityInitiative")
            .withNonComplianceMessages(Arrays.asList(new NonComplianceMessage().withMessage(
                "Resources must comply with all internal security policies. See <internal site URL> for more info."),
                new NonComplianceMessage().withMessage("Resource names must start with 'DeptA' and end with '-LC'.")
                    .withPolicyDefinitionReferenceId("10420126870854049575"),
                new NonComplianceMessage().withMessage("Storage accounts must have firewall rules configured.")
                    .withPolicyDefinitionReferenceId("8572513655450389710")))
            .create();
    }

    /*
     * x-ms-original-file: 2026-01-01-preview/createPolicyAssignmentWithUserAssignedIdentity.json
     */
    /**
     * Sample code: Create or update a policy assignment with a user assigned identity.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void createOrUpdateAPolicyAssignmentWithAUserAssignedIdentity(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyAssignments()
            .define("EnforceNaming")
            .withExistingScope("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2")
            .withRegion("eastus")
            .withIdentity(new Identity().withType(ResourceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/resourceGroups/testResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-identity",
                    new UserAssignedIdentitiesValue())))
            .withDisplayName("Enforce resource naming rules")
            .withPolicyDefinitionId(
                "/subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/providers/Microsoft.Authorization/policyDefinitions/ResourceNaming")
            .withParameters(
                mapOf("prefix",
                    new ParameterValuesValue()
                        .withValue(BinaryData.fromBytes("DeptA".getBytes(StandardCharsets.UTF_8))),
                    "suffix",
                    new ParameterValuesValue().withValue(BinaryData.fromBytes("-LC".getBytes(StandardCharsets.UTF_8)))))
            .withDescription("Force resource names to begin with given DeptA and end with -LC")
            .withMetadata(BinaryData.fromBytes("{assignedBy=Foo Bar}".getBytes(StandardCharsets.UTF_8)))
            .withEnforcementMode(EnforcementMode.DEFAULT)
            .create();
    }

    /*
     * x-ms-original-file: 2026-01-01-preview/createPolicyAssignmentWithOverrides.json
     */
    /**
     * Sample code: Create or update a policy assignment with overrides.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        createOrUpdateAPolicyAssignmentWithOverrides(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyAssignments()
            .define("CostManagement")
            .withExistingScope("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2")
            .withDisplayName("Limit the resource location and resource SKU")
            .withPolicyDefinitionId(
                "/subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/providers/Microsoft.Authorization/policySetDefinitions/CostManagement")
            .withDefinitionVersion("1.*.*")
            .withDescription("Limit the resource location and resource SKU")
            .withMetadata(BinaryData.fromBytes("{assignedBy=Special Someone}".getBytes(StandardCharsets.UTF_8)))
            .withOverrides(Arrays.asList(
                new OverrideModel().withKind(OverrideKind.POLICY_EFFECT)
                    .withValue("Audit")
                    .withSelectors(Arrays.asList(new Selector().withKind(SelectorKind.POLICY_DEFINITION_REFERENCE_ID)
                        .withIn(Arrays.asList("Limit_Skus", "Limit_Locations")))),
                new OverrideModel().withKind(OverrideKind.DEFINITION_VERSION)
                    .withValue("2.*.*")
                    .withSelectors(Arrays.asList(new Selector().withKind(SelectorKind.RESOURCE_LOCATION)
                        .withIn(Arrays.asList("eastUSEuap", "centralUSEuap"))))))
            .create();
    }

    /*
     * x-ms-original-file: 2026-01-01-preview/createPolicyAssignmentWithoutEnforcement.json
     */
    /**
     * Sample code: Create or update a policy assignment without enforcing policy effect during resource creation or
     * update.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void createOrUpdateAPolicyAssignmentWithoutEnforcingPolicyEffectDuringResourceCreationOrUpdate(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyAssignments()
            .define("EnforceNaming")
            .withExistingScope("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2")
            .withDisplayName("Enforce resource naming rules")
            .withPolicyDefinitionId(
                "/subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/providers/Microsoft.Authorization/policyDefinitions/ResourceNaming")
            .withParameters(
                mapOf("prefix",
                    new ParameterValuesValue()
                        .withValue(BinaryData.fromBytes("DeptA".getBytes(StandardCharsets.UTF_8))),
                    "suffix",
                    new ParameterValuesValue().withValue(BinaryData.fromBytes("-LC".getBytes(StandardCharsets.UTF_8)))))
            .withDescription("Force resource names to begin with given DeptA and end with -LC")
            .withMetadata(BinaryData.fromBytes("{assignedBy=Special Someone}".getBytes(StandardCharsets.UTF_8)))
            .withEnforcementMode(EnforcementMode.DO_NOT_ENFORCE)
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

### PolicyAssignments_Delete

```java
/**
 * Samples for PolicyAssignments Delete.
 */
public final class PolicyAssignmentsDeleteSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/deletePolicyAssignment.json
     */
    /**
     * Sample code: Delete a policy assignment.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void deleteAPolicyAssignment(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyAssignments()
            .deleteByResourceGroupWithResponse("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2", "EnforceNaming",
                com.azure.core.util.Context.NONE);
    }
}
```

### PolicyAssignments_Get

```java
/**
 * Samples for PolicyAssignments Get.
 */
public final class PolicyAssignmentsGetSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/getPolicyAssignmentWithOverrides.json
     */
    /**
     * Sample code: Retrieve a policy assignment with overrides.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        retrieveAPolicyAssignmentWithOverrides(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyAssignments()
            .getWithResponse("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2", "CostManagement", null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-01-01-preview/getPolicyAssignmentWithResourceSelectors.json
     */
    /**
     * Sample code: Retrieve a policy assignment with resource selectors.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void retrieveAPolicyAssignmentWithResourceSelectors(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyAssignments()
            .getWithResponse("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2", "CostManagement", null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-01-01-preview/getPolicyAssignmentWithUserAssignedIdentity.json
     */
    /**
     * Sample code: Retrieve a policy assignment with a user assigned identity.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void retrieveAPolicyAssignmentWithAUserAssignedIdentity(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyAssignments()
            .getWithResponse("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2", "EnforceNaming", null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-01-01-preview/getPolicyAssignment.json
     */
    /**
     * Sample code: Retrieve a policy assignment.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void retrieveAPolicyAssignment(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyAssignments()
            .getWithResponse("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2", "EnforceNaming", null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-01-01-preview/getPolicyAssignmentWithIdentity.json
     */
    /**
     * Sample code: Retrieve a policy assignment with a system assigned identity.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void retrieveAPolicyAssignmentWithASystemAssignedIdentity(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyAssignments()
            .getWithResponse("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2", "EnforceNaming", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### PolicyAssignments_List

```java
/**
 * Samples for PolicyAssignments List.
 */
public final class PolicyAssignmentsListSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listPolicyAssignments.json
     */
    /**
     * Sample code: List policy assignments that apply to a subscription.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void listPolicyAssignmentsThatApplyToASubscription(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyAssignments()
            .list("atScope()", "LatestDefinitionVersion, EffectiveDefinitionVersion", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### PolicyAssignments_ListByResourceGroup

```java
/**
 * Samples for PolicyAssignments ListByResourceGroup.
 */
public final class PolicyAssignmentsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listPolicyAssignmentsForResourceGroup.json
     */
    /**
     * Sample code: List policy assignments that apply to a resource group.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void listPolicyAssignmentsThatApplyToAResourceGroup(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyAssignments()
            .listByResourceGroup("TestResourceGroup", "atScope()",
                "LatestDefinitionVersion, EffectiveDefinitionVersion", null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicyAssignments_ListForManagementGroup

```java
/**
 * Samples for PolicyAssignments ListForManagementGroup.
 */
public final class PolicyAssignmentsListForManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listPolicyAssignmentsForManagementGroup.json
     */
    /**
     * Sample code: List policy assignments that apply to a management group.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void listPolicyAssignmentsThatApplyToAManagementGroup(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyAssignments()
            .listForManagementGroup("TestManagementGroup", "atScope()",
                "LatestDefinitionVersion, EffectiveDefinitionVersion", null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicyAssignments_ListForResource

```java
/**
 * Samples for PolicyAssignments ListForResource.
 */
public final class PolicyAssignmentsListForResourceSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listPolicyAssignmentsForResource.json
     */
    /**
     * Sample code: List policy assignments that apply to a resource group.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void listPolicyAssignmentsThatApplyToAResourceGroup(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyAssignments()
            .listForResource("TestResourceGroup", "Microsoft.Compute", "virtualMachines/MyTestVm", "domainNames",
                "MyTestComputer.cloudapp.net", null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicyAssignments_Update

```java
import com.azure.resourcemanager.resources.policy.models.Identity;
import com.azure.resourcemanager.resources.policy.models.OverrideKind;
import com.azure.resourcemanager.resources.policy.models.OverrideModel;
import com.azure.resourcemanager.resources.policy.models.PolicyAssignment;
import com.azure.resourcemanager.resources.policy.models.ResourceIdentityType;
import com.azure.resourcemanager.resources.policy.models.ResourceSelector;
import com.azure.resourcemanager.resources.policy.models.Selector;
import com.azure.resourcemanager.resources.policy.models.SelectorKind;
import com.azure.resourcemanager.resources.policy.models.SelfServeExemptionSettings;
import com.azure.resourcemanager.resources.policy.models.UserAssignedIdentitiesValue;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for PolicyAssignments Update.
 */
public final class PolicyAssignmentsUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/updatePolicyAssignmentWithResourceSelectors.json
     */
    /**
     * Sample code: Update a policy assignment with resource selectors.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        updateAPolicyAssignmentWithResourceSelectors(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        PolicyAssignment resource = manager.policyAssignments()
            .getWithResponse("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2", "CostManagement", null,
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withResourceSelectors(Arrays.asList(new ResourceSelector().withName("SDPRegions")
                .withSelectors(Arrays.asList(new Selector().withKind(SelectorKind.RESOURCE_LOCATION)
                    .withIn(Arrays.asList("eastus2euap", "centraluseuap"))))))
            .apply();
    }

    /*
     * x-ms-original-file: 2026-01-01-preview/updatePolicyAssignmentWithUserAssignedIdentity.json
     */
    /**
     * Sample code: Update a policy assignment with a user assigned identity.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void updateAPolicyAssignmentWithAUserAssignedIdentity(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        PolicyAssignment resource = manager.policyAssignments()
            .getWithResponse("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2", "EnforceNaming", null,
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withIdentity(new Identity().withType(ResourceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/resourceGroups/testResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/test-identity",
                    new UserAssignedIdentitiesValue())))
            .apply();
    }

    /*
     * x-ms-original-file: 2026-01-01-preview/updatePolicyAssignmentWithOverrides.json
     */
    /**
     * Sample code: Update a policy assignment with overrides.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        updateAPolicyAssignmentWithOverrides(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        PolicyAssignment resource = manager.policyAssignments()
            .getWithResponse("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2", "CostManagement", null,
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withOverrides(Arrays.asList(new OverrideModel().withKind(OverrideKind.POLICY_EFFECT)
                .withValue("Audit")
                .withSelectors(Arrays.asList(new Selector().withKind(SelectorKind.POLICY_DEFINITION_REFERENCE_ID)
                    .withIn(Arrays.asList("Limit_Skus", "Limit_Locations"))))))
            .apply();
    }

    /*
     * x-ms-original-file: 2026-01-01-preview/updatePolicyAssignmentWithSelfserveExemptionSettings.json
     */
    /**
     * Sample code: Update a policy assignment with self-serve exemption settings.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void updateAPolicyAssignmentWithSelfServeExemptionSettings(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        PolicyAssignment resource = manager.policyAssignments()
            .getWithResponse("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2", "CostManagement", null,
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withSelfServeExemptionSettings(new SelfServeExemptionSettings().withEnabled(true)
                .withPolicyDefinitionReferenceIds(Arrays.asList("Limit_Skus")))
            .apply();
    }

    /*
     * x-ms-original-file: 2026-01-01-preview/updatePolicyAssignmentWithIdentity.json
     */
    /**
     * Sample code: Update a policy assignment with a system assigned identity.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void updateAPolicyAssignmentWithASystemAssignedIdentity(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        PolicyAssignment resource = manager.policyAssignments()
            .getWithResponse("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2", "EnforceNaming", null,
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withIdentity(new Identity().withType(ResourceIdentityType.SYSTEM_ASSIGNED)).apply();
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

### PolicyDefinitionVersions_CreateOrUpdate

```java
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.resources.policy.models.ParameterDefinitionsValue;
import com.azure.resourcemanager.resources.policy.models.ParameterDefinitionsValueMetadata;
import com.azure.resourcemanager.resources.policy.models.ParameterType;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for PolicyDefinitionVersions CreateOrUpdate.
 */
public final class PolicyDefinitionVersionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/createOrUpdatePolicyDefinitionVersion.json
     */
    /**
     * Sample code: Create or update a policy definition version.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        createOrUpdateAPolicyDefinitionVersion(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyDefinitionVersions()
            .define("1.2.1")
            .withExistingPolicyDefinition("ResourceNaming")
            .withMode("All")
            .withDisplayName("Enforce resource naming convention")
            .withDescription("Force resource names to begin with given 'prefix' and/or end with given 'suffix'")
            .withPolicyRule(BinaryData.fromBytes(
                "{if={not={field=name, like=[concat(parameters('prefix'), '*', parameters('suffix'))]}}, then={effect=deny}}"
                    .getBytes(StandardCharsets.UTF_8)))
            .withMetadata(BinaryData.fromBytes("{category=Naming}".getBytes(StandardCharsets.UTF_8)))
            .withParameters(mapOf("prefix",
                new ParameterDefinitionsValue().withType(ParameterType.STRING)
                    .withMetadata(new ParameterDefinitionsValueMetadata().withDisplayName("Prefix")
                        .withDescription("Resource name prefix")
                        .withAdditionalProperties(mapOf())),
                "suffix",
                new ParameterDefinitionsValue().withType(ParameterType.STRING)
                    .withMetadata(new ParameterDefinitionsValueMetadata().withDisplayName("Suffix")
                        .withDescription("Resource name suffix")
                        .withAdditionalProperties(mapOf()))))
            .withVersion("1.2.1")
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

### PolicyDefinitionVersions_CreateOrUpdateAtManagementGroup

```java
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.resources.policy.fluent.models.PolicyDefinitionVersionInner;
import com.azure.resourcemanager.resources.policy.models.ParameterDefinitionsValue;
import com.azure.resourcemanager.resources.policy.models.ParameterDefinitionsValueMetadata;
import com.azure.resourcemanager.resources.policy.models.ParameterType;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for PolicyDefinitionVersions CreateOrUpdateAtManagementGroup.
 */
public final class PolicyDefinitionVersionsCreateOrUpdateAtManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/createOrUpdatePolicyDefinitionVersionAtManagementGroup.json
     */
    /**
     * Sample code: Create or update a policy definition version at management group level.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void createOrUpdateAPolicyDefinitionVersionAtManagementGroupLevel(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyDefinitionVersions()
            .createOrUpdateAtManagementGroupWithResponse("MyManagementGroup", "ResourceNaming", "1.2.1",
                new PolicyDefinitionVersionInner().withMode("All")
                    .withDisplayName("Enforce resource naming convention")
                    .withDescription("Force resource names to begin with given 'prefix' and/or end with given 'suffix'")
                    .withPolicyRule(BinaryData.fromBytes(
                        "{if={not={field=name, like=[concat(parameters('prefix'), '*', parameters('suffix'))]}}, then={effect=deny}}"
                            .getBytes(StandardCharsets.UTF_8)))
                    .withMetadata(BinaryData.fromBytes("{category=Naming}".getBytes(StandardCharsets.UTF_8)))
                    .withParameters(mapOf("prefix",
                        new ParameterDefinitionsValue().withType(ParameterType.STRING)
                            .withMetadata(new ParameterDefinitionsValueMetadata().withDisplayName("Prefix")
                                .withDescription("Resource name prefix")
                                .withAdditionalProperties(mapOf())),
                        "suffix",
                        new ParameterDefinitionsValue().withType(ParameterType.STRING)
                            .withMetadata(new ParameterDefinitionsValueMetadata().withDisplayName("Suffix")
                                .withDescription("Resource name suffix")
                                .withAdditionalProperties(mapOf()))))
                    .withVersion("1.2.1"),
                com.azure.core.util.Context.NONE);
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

### PolicyDefinitionVersions_Delete

```java
/**
 * Samples for PolicyDefinitionVersions Delete.
 */
public final class PolicyDefinitionVersionsDeleteSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/deletePolicyDefinitionVersion.json
     */
    /**
     * Sample code: Delete a policy definition version.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        deleteAPolicyDefinitionVersion(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyDefinitionVersions()
            .deleteByResourceGroupWithResponse("ResourceNaming", "1.2.1", com.azure.core.util.Context.NONE);
    }
}
```

### PolicyDefinitionVersions_DeleteAtManagementGroup

```java
/**
 * Samples for PolicyDefinitionVersions DeleteAtManagementGroup.
 */
public final class PolicyDefinitionVersionsDeleteAtManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/deletePolicyDefinitionVersionAtManagementGroup.json
     */
    /**
     * Sample code: Delete a policy definition version at management group level.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void deleteAPolicyDefinitionVersionAtManagementGroupLevel(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyDefinitionVersions()
            .deleteAtManagementGroupWithResponse("MyManagementGroup", "ResourceNaming", "1.2.1",
                com.azure.core.util.Context.NONE);
    }
}
```

### PolicyDefinitionVersions_Get

```java
/**
 * Samples for PolicyDefinitionVersions Get.
 */
public final class PolicyDefinitionVersionsGetSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/getPolicyDefinitionVersion.json
     */
    /**
     * Sample code: Retrieve a policy definition version.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        retrieveAPolicyDefinitionVersion(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyDefinitionVersions().getWithResponse("ResourceNaming", "1.2.1", com.azure.core.util.Context.NONE);
    }
}
```

### PolicyDefinitionVersions_GetAtManagementGroup

```java
/**
 * Samples for PolicyDefinitionVersions GetAtManagementGroup.
 */
public final class PolicyDefinitionVersionsGetAtManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/getPolicyDefinitionVersionAtManagementGroup.json
     */
    /**
     * Sample code: Retrieve a policy definition version at management group level.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void retrieveAPolicyDefinitionVersionAtManagementGroupLevel(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyDefinitionVersions()
            .getAtManagementGroupWithResponse("MyManagementGroup", "ResourceNaming", "1.2.1",
                com.azure.core.util.Context.NONE);
    }
}
```

### PolicyDefinitionVersions_GetBuiltIn

```java
/**
 * Samples for PolicyDefinitionVersions GetBuiltIn.
 */
public final class PolicyDefinitionVersionsGetBuiltInSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/getBuiltinPolicyDefinitionVersion.json
     */
    /**
     * Sample code: Retrieve a built-in policy definition version.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        retrieveABuiltInPolicyDefinitionVersion(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyDefinitionVersions()
            .getBuiltInWithResponse("7433c107-6db4-4ad1-b57a-a76dce0154a1", "1.2.1", com.azure.core.util.Context.NONE);
    }
}
```

### PolicyDefinitionVersions_List

```java
/**
 * Samples for PolicyDefinitionVersions List.
 */
public final class PolicyDefinitionVersionsListSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listPolicyDefinitionVersions.json
     */
    /**
     * Sample code: List policy definition versions by subscription.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        listPolicyDefinitionVersionsBySubscription(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyDefinitionVersions().list("ResourceNaming", null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicyDefinitionVersions_ListAll

```java
/**
 * Samples for PolicyDefinitionVersions ListAll.
 */
public final class PolicyDefinitionVersionsListAllSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listAllPolicyDefinitionVersions.json
     */
    /**
     * Sample code: List all policy definition versions at subscription.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void listAllPolicyDefinitionVersionsAtSubscription(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyDefinitionVersions().listAllWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### PolicyDefinitionVersions_ListAllAtManagementGroup

```java
/**
 * Samples for PolicyDefinitionVersions ListAllAtManagementGroup.
 */
public final class PolicyDefinitionVersionsListAllAtManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listAllPolicyDefinitionVersionsByManagementGroup.json
     */
    /**
     * Sample code: List all policy definition versions at management group.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void listAllPolicyDefinitionVersionsAtManagementGroup(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyDefinitionVersions()
            .listAllAtManagementGroupWithResponse("MyManagementGroup", com.azure.core.util.Context.NONE);
    }
}
```

### PolicyDefinitionVersions_ListAllBuiltins

```java
/**
 * Samples for PolicyDefinitionVersions ListAllBuiltins.
 */
public final class PolicyDefinitionVersionsListAllBuiltinsSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listAllBuiltInPolicyDefinitionVersions.json
     */
    /**
     * Sample code: List all built-in policy definition versions.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        listAllBuiltInPolicyDefinitionVersions(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyDefinitionVersions().listAllBuiltinsWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### PolicyDefinitionVersions_ListBuiltIn

```java
/**
 * Samples for PolicyDefinitionVersions ListBuiltIn.
 */
public final class PolicyDefinitionVersionsListBuiltInSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listBuiltInPolicyDefinitionVersions.json
     */
    /**
     * Sample code: List built-in policy definition versions.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        listBuiltInPolicyDefinitionVersions(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyDefinitionVersions()
            .listBuiltIn("06a78e20-9358-41c9-923c-fb736d382a12", null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicyDefinitionVersions_ListByManagementGroup

```java
/**
 * Samples for PolicyDefinitionVersions ListByManagementGroup.
 */
public final class PolicyDefinitionVersionsListByManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listPolicyDefinitionVersionsByManagementGroup.json
     */
    /**
     * Sample code: List policy definition versions by management group.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void listPolicyDefinitionVersionsByManagementGroup(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyDefinitionVersions()
            .listByManagementGroup("MyManagementGroup", "ResourceNaming", null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicyDefinitions_CreateOrUpdate

```java
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.resources.policy.models.ExternalEvaluationEndpointSettings;
import com.azure.resourcemanager.resources.policy.models.ExternalEvaluationEnforcementSettings;
import com.azure.resourcemanager.resources.policy.models.ParameterDefinitionsValue;
import com.azure.resourcemanager.resources.policy.models.ParameterDefinitionsValueMetadata;
import com.azure.resourcemanager.resources.policy.models.ParameterType;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for PolicyDefinitions CreateOrUpdate.
 */
public final class PolicyDefinitionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/createOrUpdatePolicyDefinitionAdvancedParams.json
     */
    /**
     * Sample code: Create or update a policy definition with advanced parameters.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void createOrUpdateAPolicyDefinitionWithAdvancedParameters(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyDefinitions()
            .define("EventHubDiagnosticLogs")
            .withMode("Indexed")
            .withDisplayName("Event Hubs should have diagnostic logging enabled")
            .withDescription(
                "Audit enabling of logs and retain them up to a year. This enables recreation of activity trails for investigation purposes when a security incident occurs or your network is compromised")
            .withPolicyRule(BinaryData.fromBytes(
                "{if={equals=Microsoft.EventHub/namespaces, field=type}, then={effect=AuditIfNotExists, details={type=Microsoft.Insights/diagnosticSettings, existenceCondition={allOf=[{equals=true, field=Microsoft.Insights/diagnosticSettings/logs[*].retentionPolicy.enabled}, {equals=[parameters('requiredRetentionDays')], field=Microsoft.Insights/diagnosticSettings/logs[*].retentionPolicy.days}]}}}}"
                    .getBytes(StandardCharsets.UTF_8)))
            .withMetadata(BinaryData.fromBytes("{category=Event Hub}".getBytes(StandardCharsets.UTF_8)))
            .withParameters(mapOf("requiredRetentionDays",
                new ParameterDefinitionsValue().withType(ParameterType.INTEGER)
                    .withAllowedValues(Arrays.asList(BinaryData.fromBytes("0".getBytes(StandardCharsets.UTF_8)),
                        BinaryData.fromBytes("30".getBytes(StandardCharsets.UTF_8)),
                        BinaryData.fromBytes("90".getBytes(StandardCharsets.UTF_8)),
                        BinaryData.fromBytes("180".getBytes(StandardCharsets.UTF_8)),
                        BinaryData.fromBytes("365".getBytes(StandardCharsets.UTF_8))))
                    .withDefaultValue(BinaryData.fromBytes("365".getBytes(StandardCharsets.UTF_8)))
                    .withMetadata(new ParameterDefinitionsValueMetadata().withDisplayName("Required retention (days)")
                        .withDescription("The required diagnostic logs retention in days")
                        .withAdditionalProperties(mapOf()))))
            .create();
    }

    /*
     * x-ms-original-file: 2026-01-01-preview/createOrUpdatePolicyDefinitionExternalEvaluationEnforcementSettings.json
     */
    /**
     * Sample code: Create or update a policy definition with external evaluation enforcement settings.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void createOrUpdateAPolicyDefinitionWithExternalEvaluationEnforcementSettings(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyDefinitions()
            .define("RandomizeVMAllocation")
            .withMode("Indexed")
            .withDisplayName("Randomize VM Allocation")
            .withDescription(
                "Randomly disable VM allocation in eastus by having policy rule reference the outcome of invoking an external endpoint using the CoinFlip endpoint that returns random values.")
            .withPolicyRule(BinaryData.fromBytes(
                "{if={allOf=[{equals=Microsoft.Compute/virtualMachines, field=type}, {equals=eastus, field=location}, {equals=false, value=[claims().isValid]}]}, then={effect=deny}}"
                    .getBytes(StandardCharsets.UTF_8)))
            .withMetadata(BinaryData.fromBytes("{category=VM}".getBytes(StandardCharsets.UTF_8)))
            .withExternalEvaluationEnforcementSettings(new ExternalEvaluationEnforcementSettings()
                .withMissingTokenAction("fakeTokenPlaceholder")
                .withEndpointSettings(new ExternalEvaluationEndpointSettings().withKind("CoinFlip")
                    .withDetails(BinaryData.fromBytes("{successProbability=0.5}".getBytes(StandardCharsets.UTF_8))))
                .withRoleDefinitionIds(Arrays.asList(
                    "subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/providers/Microsoft.Authorization/roleDefinitions/f0cc2aea-b517-48f6-8f9e-0c01c687907b")))
            .create();
    }

    /*
     * x-ms-original-file: 2026-01-01-preview/createOrUpdatePolicyDefinition.json
     */
    /**
     * Sample code: Create or update a policy definition.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        createOrUpdateAPolicyDefinition(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyDefinitions()
            .define("ResourceNaming")
            .withMode("All")
            .withDisplayName("Enforce resource naming convention")
            .withDescription("Force resource names to begin with given 'prefix' and/or end with given 'suffix'")
            .withPolicyRule(BinaryData.fromBytes(
                "{if={not={field=name, like=[concat(parameters('prefix'), '*', parameters('suffix'))]}}, then={effect=deny}}"
                    .getBytes(StandardCharsets.UTF_8)))
            .withMetadata(BinaryData.fromBytes("{category=Naming}".getBytes(StandardCharsets.UTF_8)))
            .withParameters(mapOf("prefix",
                new ParameterDefinitionsValue().withType(ParameterType.STRING)
                    .withMetadata(new ParameterDefinitionsValueMetadata().withDisplayName("Prefix")
                        .withDescription("Resource name prefix")
                        .withAdditionalProperties(mapOf())),
                "suffix",
                new ParameterDefinitionsValue().withType(ParameterType.STRING)
                    .withMetadata(new ParameterDefinitionsValueMetadata().withDisplayName("Suffix")
                        .withDescription("Resource name suffix")
                        .withAdditionalProperties(mapOf()))))
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

### PolicyDefinitions_CreateOrUpdateAtManagementGroup

```java
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.resources.policy.fluent.models.PolicyDefinitionInner;
import com.azure.resourcemanager.resources.policy.models.ParameterDefinitionsValue;
import com.azure.resourcemanager.resources.policy.models.ParameterDefinitionsValueMetadata;
import com.azure.resourcemanager.resources.policy.models.ParameterType;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for PolicyDefinitions CreateOrUpdateAtManagementGroup.
 */
public final class PolicyDefinitionsCreateOrUpdateAtManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/createOrUpdatePolicyDefinitionAtManagementGroup.json
     */
    /**
     * Sample code: Create or update a policy definition at management group level.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void createOrUpdateAPolicyDefinitionAtManagementGroupLevel(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyDefinitions()
            .createOrUpdateAtManagementGroupWithResponse("MyManagementGroup", "ResourceNaming",
                new PolicyDefinitionInner().withMode("All")
                    .withDisplayName("Enforce resource naming convention")
                    .withDescription("Force resource names to begin with given 'prefix' and/or end with given 'suffix'")
                    .withPolicyRule(BinaryData.fromBytes(
                        "{if={not={field=name, like=[concat(parameters('prefix'), '*', parameters('suffix'))]}}, then={effect=deny}}"
                            .getBytes(StandardCharsets.UTF_8)))
                    .withMetadata(BinaryData.fromBytes("{category=Naming}".getBytes(StandardCharsets.UTF_8)))
                    .withParameters(mapOf("prefix",
                        new ParameterDefinitionsValue().withType(ParameterType.STRING)
                            .withMetadata(new ParameterDefinitionsValueMetadata().withDisplayName("Prefix")
                                .withDescription("Resource name prefix")
                                .withAdditionalProperties(mapOf())),
                        "suffix",
                        new ParameterDefinitionsValue().withType(ParameterType.STRING)
                            .withMetadata(new ParameterDefinitionsValueMetadata().withDisplayName("Suffix")
                                .withDescription("Resource name suffix")
                                .withAdditionalProperties(mapOf())))),
                com.azure.core.util.Context.NONE);
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

### PolicyDefinitions_Delete

```java
/**
 * Samples for PolicyDefinitions Delete.
 */
public final class PolicyDefinitionsDeleteSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/deletePolicyDefinition.json
     */
    /**
     * Sample code: Delete a policy definition.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void deleteAPolicyDefinition(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyDefinitions().deleteWithResponse("ResourceNaming", com.azure.core.util.Context.NONE);
    }
}
```

### PolicyDefinitions_DeleteAtManagementGroup

```java
/**
 * Samples for PolicyDefinitions DeleteAtManagementGroup.
 */
public final class PolicyDefinitionsDeleteAtManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/deletePolicyDefinitionAtManagementGroup.json
     */
    /**
     * Sample code: Delete a policy definition at management group level.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void deleteAPolicyDefinitionAtManagementGroupLevel(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyDefinitions()
            .deleteAtManagementGroupWithResponse("MyManagementGroup", "ResourceNaming",
                com.azure.core.util.Context.NONE);
    }
}
```

### PolicyDefinitions_Get

```java
/**
 * Samples for PolicyDefinitions Get.
 */
public final class PolicyDefinitionsGetSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/getPolicyDefinition.json
     */
    /**
     * Sample code: Retrieve a policy definition.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void retrieveAPolicyDefinition(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyDefinitions().getWithResponse("ResourceNaming", com.azure.core.util.Context.NONE);
    }
}
```

### PolicyDefinitions_GetAtManagementGroup

```java
/**
 * Samples for PolicyDefinitions GetAtManagementGroup.
 */
public final class PolicyDefinitionsGetAtManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/getPolicyDefinitionAtManagementGroup.json
     */
    /**
     * Sample code: Retrieve a policy definition at management group level.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void retrieveAPolicyDefinitionAtManagementGroupLevel(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyDefinitions()
            .getAtManagementGroupWithResponse("MyManagementGroup", "ResourceNaming", com.azure.core.util.Context.NONE);
    }
}
```

### PolicyDefinitions_GetBuiltIn

```java
/**
 * Samples for PolicyDefinitions GetBuiltIn.
 */
public final class PolicyDefinitionsGetBuiltInSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/getBuiltinPolicyDefinition.json
     */
    /**
     * Sample code: Retrieve a built-in policy definition.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        retrieveABuiltInPolicyDefinition(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyDefinitions()
            .getBuiltInWithResponse("7433c107-6db4-4ad1-b57a-a76dce0154a1", com.azure.core.util.Context.NONE);
    }
}
```

### PolicyDefinitions_List

```java
/**
 * Samples for PolicyDefinitions List.
 */
public final class PolicyDefinitionsListSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listPolicyDefinitions.json
     */
    /**
     * Sample code: List policy definitions by subscription.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        listPolicyDefinitionsBySubscription(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyDefinitions().list(null, null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicyDefinitions_ListBuiltIn

```java
/**
 * Samples for PolicyDefinitions ListBuiltIn.
 */
public final class PolicyDefinitionsListBuiltInSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listBuiltInPolicyDefinitions.json
     */
    /**
     * Sample code: List built-in policy definitions.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void listBuiltInPolicyDefinitions(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyDefinitions().listBuiltIn(null, null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicyDefinitions_ListByManagementGroup

```java
/**
 * Samples for PolicyDefinitions ListByManagementGroup.
 */
public final class PolicyDefinitionsListByManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listPolicyDefinitionsByManagementGroup.json
     */
    /**
     * Sample code: List policy definitions by management group.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        listPolicyDefinitionsByManagementGroup(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyDefinitions()
            .listByManagementGroup("MyManagementGroup", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicyEnrollments_CreateOrUpdate

```java
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.resources.policy.models.AssignmentScopeValidation;
import com.azure.resourcemanager.resources.policy.models.ResourceSelector;
import com.azure.resourcemanager.resources.policy.models.Selector;
import com.azure.resourcemanager.resources.policy.models.SelectorKind;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Samples for PolicyEnrollments CreateOrUpdate.
 */
public final class PolicyEnrollmentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/createOrUpdatePolicyEnrollmentWithResourceSelectors.json
     */
    /**
     * Sample code: Create or update a policy enrollment with resource selectors.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void createOrUpdateAPolicyEnrollmentWithResourceSelectors(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyEnrollments()
            .define("DemoExpensiveVM")
            .withExistingScope("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/resourceGroups/demoCluster")
            .withPolicyAssignmentId(
                "/subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/providers/Microsoft.Authorization/policyAssignments/CostManagement")
            .withPolicyDefinitionReferenceIds(Arrays.asList("Limit_Skus"))
            .withDisplayName("Enroll demo cluster")
            .withDescription("Enroll demo cluster from limit sku")
            .withMetadata(
                BinaryData.fromBytes("{reason=Enrollment for a expensive VM demo}".getBytes(StandardCharsets.UTF_8)))
            .withAssignmentScopeValidation(AssignmentScopeValidation.DEFAULT)
            .withResourceSelectors(Arrays.asList(new ResourceSelector().withName("SDPRegions")
                .withSelectors(Arrays.asList(new Selector().withKind(SelectorKind.RESOURCE_LOCATION)
                    .withIn(Arrays.asList("eastus2euap", "centraluseuap"))))))
            .create();
    }

    /*
     * x-ms-original-file: 2026-01-01-preview/createOrUpdatePolicyEnrollment.json
     */
    /**
     * Sample code: Create or update a policy enrollment.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        createOrUpdateAPolicyEnrollment(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyEnrollments()
            .define("DemoExpensiveVM")
            .withExistingScope("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/resourceGroups/demoCluster")
            .withPolicyAssignmentId(
                "/subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/providers/Microsoft.Authorization/policyAssignments/CostManagement")
            .withPolicyDefinitionReferenceIds(Arrays.asList("Limit_Skus"))
            .withDisplayName("Enroll demo cluster")
            .withDescription("Enroll demo cluster from limit sku")
            .withMetadata(
                BinaryData.fromBytes("{reason=Enrollment for a expensive VM demo}".getBytes(StandardCharsets.UTF_8)))
            .create();
    }
}
```

### PolicyEnrollments_Delete

```java
/**
 * Samples for PolicyEnrollments Delete.
 */
public final class PolicyEnrollmentsDeleteSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/deletePolicyEnrollment.json
     */
    /**
     * Sample code: Delete a policy enrollment.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void deleteAPolicyEnrollment(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyEnrollments()
            .deleteByResourceGroupWithResponse(
                "subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/resourceGroups/demoCluster", "DemoExpensiveVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### PolicyEnrollments_Get

```java
/**
 * Samples for PolicyEnrollments Get.
 */
public final class PolicyEnrollmentsGetSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/getPolicyEnrollmentWithResourceSelectors.json
     */
    /**
     * Sample code: Retrieve a policy enrollment with resource selectors.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void retrieveAPolicyEnrollmentWithResourceSelectors(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyEnrollments()
            .getWithResponse("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/resourceGroups/demoCluster",
                "DemoExpensiveVM", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-01-01-preview/getPolicyEnrollment.json
     */
    /**
     * Sample code: Retrieve a policy enrollment.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void retrieveAPolicyEnrollment(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyEnrollments()
            .getWithResponse("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/resourceGroups/demoCluster",
                "DemoExpensiveVM", com.azure.core.util.Context.NONE);
    }
}
```

### PolicyEnrollments_List

```java
/**
 * Samples for PolicyEnrollments List.
 */
public final class PolicyEnrollmentsListSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listPolicyEnrollmentsForSubscription.json
     */
    /**
     * Sample code: List policy enrollments for subscription.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        listPolicyEnrollmentsForSubscription(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyEnrollments().list("atScope()", com.azure.core.util.Context.NONE);
    }
}
```

### PolicyEnrollments_ListByResourceGroup

```java
/**
 * Samples for PolicyEnrollments ListByResourceGroup.
 */
public final class PolicyEnrollmentsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listPolicyEnrollmentsForResourceGroup.json
     */
    /**
     * Sample code: List policy enrollments for resource group.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        listPolicyEnrollmentsForResourceGroup(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyEnrollments()
            .listByResourceGroup("TestResourceGroup", "atScope()", com.azure.core.util.Context.NONE);
    }
}
```

### PolicyEnrollments_ListForManagementGroup

```java
/**
 * Samples for PolicyEnrollments ListForManagementGroup.
 */
public final class PolicyEnrollmentsListForManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listPolicyEnrollmentsForManagementGroup.json
     */
    /**
     * Sample code: List policy enrollments for management group.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        listPolicyEnrollmentsForManagementGroup(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyEnrollments().listForManagementGroup("DevOrg", "atScope()", com.azure.core.util.Context.NONE);
    }
}
```

### PolicyEnrollments_ListForResource

```java
/**
 * Samples for PolicyEnrollments ListForResource.
 */
public final class PolicyEnrollmentsListForResourceSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listPolicyEnrollmentsForResource.json
     */
    /**
     * Sample code: List policy enrollments for resource.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        listPolicyEnrollmentsForResource(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyEnrollments()
            .listForResource("TestResourceGroup", "Microsoft.Compute", "virtualMachines/MyTestVm", "domainNames",
                "MyTestComputer.cloudapp.net", null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicyEnrollments_Update

```java
import com.azure.resourcemanager.resources.policy.models.AssignmentScopeValidation;
import com.azure.resourcemanager.resources.policy.models.PolicyEnrollment;
import com.azure.resourcemanager.resources.policy.models.ResourceSelector;
import com.azure.resourcemanager.resources.policy.models.Selector;
import com.azure.resourcemanager.resources.policy.models.SelectorKind;
import java.util.Arrays;

/**
 * Samples for PolicyEnrollments Update.
 */
public final class PolicyEnrollmentsUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/updatePolicyEnrollmentWithResourceSelectors.json
     */
    /**
     * Sample code: Update a policy enrollment with resource selectors.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        updateAPolicyEnrollmentWithResourceSelectors(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        PolicyEnrollment resource = manager.policyEnrollments()
            .getWithResponse("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/resourceGroups/demoCluster",
                "DemoExpensiveVM", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withAssignmentScopeValidation(AssignmentScopeValidation.DEFAULT)
            .withResourceSelectors(Arrays.asList(new ResourceSelector().withName("SDPRegions")
                .withSelectors(Arrays.asList(new Selector().withKind(SelectorKind.RESOURCE_LOCATION)
                    .withIn(Arrays.asList("eastus2euap", "centraluseuap"))))))
            .apply();
    }
}
```

### PolicyExemptions_CreateOrUpdate

```java
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.resources.policy.models.AssignmentScopeValidation;
import com.azure.resourcemanager.resources.policy.models.ExemptionCategory;
import com.azure.resourcemanager.resources.policy.models.ResourceSelector;
import com.azure.resourcemanager.resources.policy.models.Selector;
import com.azure.resourcemanager.resources.policy.models.SelectorKind;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Samples for PolicyExemptions CreateOrUpdate.
 */
public final class PolicyExemptionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/createOrUpdatePolicyExemption.json
     */
    /**
     * Sample code: Create or update a policy exemption.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        createOrUpdateAPolicyExemption(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyExemptions()
            .define("DemoExpensiveVM")
            .withExistingScope("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/resourceGroups/demoCluster")
            .withPolicyAssignmentId(
                "/subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/providers/Microsoft.Authorization/policyAssignments/CostManagement")
            .withPolicyDefinitionReferenceIds(Arrays.asList("Limit_Skus"))
            .withExemptionCategory(ExemptionCategory.WAIVER)
            .withDisplayName("Exempt demo cluster")
            .withDescription("Exempt demo cluster from limit sku")
            .withMetadata(BinaryData
                .fromBytes("{reason=Temporary exemption for a expensive VM demo}".getBytes(StandardCharsets.UTF_8)))
            .create();
    }

    /*
     * x-ms-original-file: 2026-01-01-preview/createOrUpdatePolicyExemptionWithResourceSelectors.json
     */
    /**
     * Sample code: Create or update a policy exemption with resource selectors.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void createOrUpdateAPolicyExemptionWithResourceSelectors(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyExemptions()
            .define("DemoExpensiveVM")
            .withExistingScope("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/resourceGroups/demoCluster")
            .withPolicyAssignmentId(
                "/subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/providers/Microsoft.Authorization/policyAssignments/CostManagement")
            .withPolicyDefinitionReferenceIds(Arrays.asList("Limit_Skus"))
            .withExemptionCategory(ExemptionCategory.WAIVER)
            .withDisplayName("Exempt demo cluster")
            .withDescription("Exempt demo cluster from limit sku")
            .withMetadata(BinaryData
                .fromBytes("{reason=Temporary exemption for a expensive VM demo}".getBytes(StandardCharsets.UTF_8)))
            .withResourceSelectors(Arrays.asList(new ResourceSelector().withName("SDPRegions")
                .withSelectors(Arrays.asList(new Selector().withKind(SelectorKind.RESOURCE_LOCATION)
                    .withIn(Arrays.asList("eastus2euap", "centraluseuap"))))))
            .withAssignmentScopeValidation(AssignmentScopeValidation.DEFAULT)
            .create();
    }
}
```

### PolicyExemptions_Delete

```java
/**
 * Samples for PolicyExemptions Delete.
 */
public final class PolicyExemptionsDeleteSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/deletePolicyExemption.json
     */
    /**
     * Sample code: Delete a policy exemption.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void deleteAPolicyExemption(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyExemptions()
            .deleteByResourceGroupWithResponse(
                "subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/resourceGroups/demoCluster", "DemoExpensiveVM",
                com.azure.core.util.Context.NONE);
    }
}
```

### PolicyExemptions_Get

```java
/**
 * Samples for PolicyExemptions Get.
 */
public final class PolicyExemptionsGetSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/getPolicyExemptionWithResourceSelectors.json
     */
    /**
     * Sample code: Retrieve a policy exemption with resource selectors.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void retrieveAPolicyExemptionWithResourceSelectors(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyExemptions()
            .getWithResponse("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/resourceGroups/demoCluster",
                "DemoExpensiveVM", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-01-01-preview/getPolicyExemption.json
     */
    /**
     * Sample code: Retrieve a policy exemption.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void retrieveAPolicyExemption(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyExemptions()
            .getWithResponse("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/resourceGroups/demoCluster",
                "DemoExpensiveVM", com.azure.core.util.Context.NONE);
    }
}
```

### PolicyExemptions_List

```java
/**
 * Samples for PolicyExemptions List.
 */
public final class PolicyExemptionsListSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listPolicyExemptionsForSubscription.json
     */
    /**
     * Sample code: List policy exemptions that apply to a subscription.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        listPolicyExemptionsThatApplyToASubscription(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyExemptions().list("atScope()", com.azure.core.util.Context.NONE);
    }
}
```

### PolicyExemptions_ListByResourceGroup

```java
/**
 * Samples for PolicyExemptions ListByResourceGroup.
 */
public final class PolicyExemptionsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listPolicyExemptionsForResourceGroup.json
     */
    /**
     * Sample code: List policy exemptions that apply to a resource group.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void listPolicyExemptionsThatApplyToAResourceGroup(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyExemptions()
            .listByResourceGroup("TestResourceGroup", "atScope()", com.azure.core.util.Context.NONE);
    }
}
```

### PolicyExemptions_ListForManagementGroup

```java
/**
 * Samples for PolicyExemptions ListForManagementGroup.
 */
public final class PolicyExemptionsListForManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listPolicyExemptionsForManagementGroup.json
     */
    /**
     * Sample code: List policy exemptions that apply to a management group.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void listPolicyExemptionsThatApplyToAManagementGroup(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyExemptions().listForManagementGroup("DevOrg", "atScope()", com.azure.core.util.Context.NONE);
    }
}
```

### PolicyExemptions_ListForResource

```java
/**
 * Samples for PolicyExemptions ListForResource.
 */
public final class PolicyExemptionsListForResourceSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listPolicyExemptionsForResource.json
     */
    /**
     * Sample code: List all policy exemptions that apply to a resource.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        listAllPolicyExemptionsThatApplyToAResource(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyExemptions()
            .listForResource("TestResourceGroup", "Microsoft.Compute", "virtualMachines/MyTestVm", "domainNames",
                "MyTestComputer.cloudapp.net", null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicyExemptions_Update

```java
import com.azure.resourcemanager.resources.policy.models.AssignmentScopeValidation;
import com.azure.resourcemanager.resources.policy.models.PolicyExemption;
import com.azure.resourcemanager.resources.policy.models.ResourceSelector;
import com.azure.resourcemanager.resources.policy.models.Selector;
import com.azure.resourcemanager.resources.policy.models.SelectorKind;
import java.util.Arrays;

/**
 * Samples for PolicyExemptions Update.
 */
public final class PolicyExemptionsUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/updatePolicyExemptionWithResourceSelectors.json
     */
    /**
     * Sample code: Update a policy exemption with resource selectors.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        updateAPolicyExemptionWithResourceSelectors(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        PolicyExemption resource = manager.policyExemptions()
            .getWithResponse("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/resourceGroups/demoCluster",
                "DemoExpensiveVM", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withResourceSelectors(Arrays.asList(new ResourceSelector().withName("SDPRegions")
                .withSelectors(Arrays.asList(new Selector().withKind(SelectorKind.RESOURCE_LOCATION)
                    .withIn(Arrays.asList("eastus2euap", "centraluseuap"))))))
            .withAssignmentScopeValidation(AssignmentScopeValidation.DEFAULT)
            .apply();
    }
}
```

### PolicySetDefinitionVersions_CreateOrUpdate

```java
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.resources.policy.models.ParameterDefinitionsValue;
import com.azure.resourcemanager.resources.policy.models.ParameterDefinitionsValueMetadata;
import com.azure.resourcemanager.resources.policy.models.ParameterType;
import com.azure.resourcemanager.resources.policy.models.ParameterValuesValue;
import com.azure.resourcemanager.resources.policy.models.PolicyDefinitionReference;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for PolicySetDefinitionVersions CreateOrUpdate.
 */
public final class PolicySetDefinitionVersionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/createOrUpdatePolicySetDefinitionVersion.json
     */
    /**
     * Sample code: Create or update a policy set definition version.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        createOrUpdateAPolicySetDefinitionVersion(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policySetDefinitionVersions()
            .define("1.2.1")
            .withExistingPolicySetDefinition("CostManagement")
            .withDisplayName("Cost Management")
            .withDescription("Policies to enforce low cost storage SKUs")
            .withMetadata(BinaryData.fromBytes("{category=Cost Management}".getBytes(StandardCharsets.UTF_8)))
            .withParameters(
                mapOf("namePrefix",
                    new ParameterDefinitionsValue().withType(ParameterType.STRING)
                        .withDefaultValue(BinaryData.fromBytes("myPrefix".getBytes(StandardCharsets.UTF_8)))
                        .withMetadata(new ParameterDefinitionsValueMetadata()
                            .withDisplayName("Prefix to enforce on resource names")
                            .withAdditionalProperties(mapOf()))))
            .withPolicyDefinitions(Arrays.asList(new PolicyDefinitionReference().withPolicyDefinitionId(
                "/subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/providers/Microsoft.Authorization/policyDefinitions/7433c107-6db4-4ad1-b57a-a76dce0154a1")
                .withParameters(mapOf("listOfAllowedSKUs",
                    new ParameterValuesValue().withValue(
                        BinaryData.fromBytes("[Standard_GRS, Standard_LRS]".getBytes(StandardCharsets.UTF_8)))))
                .withPolicyDefinitionReferenceId("Limit_Skus"),
                new PolicyDefinitionReference().withPolicyDefinitionId(
                    "/subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/providers/Microsoft.Authorization/policyDefinitions/ResourceNaming")
                    .withParameters(mapOf("prefix",
                        new ParameterValuesValue().withValue(
                            BinaryData.fromBytes("[parameters('namePrefix')]".getBytes(StandardCharsets.UTF_8))),
                        "suffix",
                        new ParameterValuesValue()
                            .withValue(BinaryData.fromBytes("-LC".getBytes(StandardCharsets.UTF_8)))))
                    .withPolicyDefinitionReferenceId("Resource_Naming")))
            .withVersion("1.2.1")
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

### PolicySetDefinitionVersions_CreateOrUpdateAtManagementGroup

```java
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.resources.policy.fluent.models.PolicySetDefinitionVersionInner;
import com.azure.resourcemanager.resources.policy.models.ParameterValuesValue;
import com.azure.resourcemanager.resources.policy.models.PolicyDefinitionReference;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for PolicySetDefinitionVersions CreateOrUpdateAtManagementGroup.
 */
public final class PolicySetDefinitionVersionsCreateOrUpdateAtManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/createOrUpdatePolicySetDefinitionVersionAtManagementGroup.json
     */
    /**
     * Sample code: Create or update a policy set definition version at management group level.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void createOrUpdateAPolicySetDefinitionVersionAtManagementGroupLevel(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policySetDefinitionVersions()
            .createOrUpdateAtManagementGroupWithResponse("MyManagementGroup", "CostManagement", "1.2.1",
                new PolicySetDefinitionVersionInner().withDisplayName("Cost Management")
                    .withDescription("Policies to enforce low cost storage SKUs")
                    .withMetadata(BinaryData.fromBytes("{category=Cost Management}".getBytes(StandardCharsets.UTF_8)))
                    .withPolicyDefinitions(Arrays.asList(new PolicyDefinitionReference().withPolicyDefinitionId(
                        "/providers/Microsoft.Management/managementGroups/MyManagementGroup/providers/Microsoft.Authorization/policyDefinitions/7433c107-6db4-4ad1-b57a-a76dce0154a1")
                        .withParameters(mapOf("listOfAllowedSKUs",
                            new ParameterValuesValue().withValue(
                                BinaryData.fromBytes("[Standard_GRS, Standard_LRS]".getBytes(StandardCharsets.UTF_8)))))
                        .withPolicyDefinitionReferenceId("Limit_Skus"),
                        new PolicyDefinitionReference().withPolicyDefinitionId(
                            "/providers/Microsoft.Management/managementGroups/MyManagementGroup/providers/Microsoft.Authorization/policyDefinitions/ResourceNaming")
                            .withParameters(mapOf("prefix",
                                new ParameterValuesValue()
                                    .withValue(BinaryData.fromBytes("DeptA".getBytes(StandardCharsets.UTF_8))),
                                "suffix",
                                new ParameterValuesValue()
                                    .withValue(BinaryData.fromBytes("-LC".getBytes(StandardCharsets.UTF_8)))))
                            .withPolicyDefinitionReferenceId("Resource_Naming")))
                    .withVersion("1.2.1"),
                com.azure.core.util.Context.NONE);
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

### PolicySetDefinitionVersions_Delete

```java
/**
 * Samples for PolicySetDefinitionVersions Delete.
 */
public final class PolicySetDefinitionVersionsDeleteSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/deletePolicySetDefinitionVersion.json
     */
    /**
     * Sample code: Delete a policy set definition version.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        deleteAPolicySetDefinitionVersion(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policySetDefinitionVersions()
            .deleteByResourceGroupWithResponse("CostManagement", "1.2.1", com.azure.core.util.Context.NONE);
    }
}
```

### PolicySetDefinitionVersions_DeleteAtManagementGroup

```java
/**
 * Samples for PolicySetDefinitionVersions DeleteAtManagementGroup.
 */
public final class PolicySetDefinitionVersionsDeleteAtManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/deletePolicySetDefinitionVersionAtManagementGroup.json
     */
    /**
     * Sample code: Delete a policy set definition version at management group level.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void deleteAPolicySetDefinitionVersionAtManagementGroupLevel(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policySetDefinitionVersions()
            .deleteAtManagementGroupWithResponse("MyManagementGroup", "CostManagement", "1.2.1",
                com.azure.core.util.Context.NONE);
    }
}
```

### PolicySetDefinitionVersions_Get

```java
/**
 * Samples for PolicySetDefinitionVersions Get.
 */
public final class PolicySetDefinitionVersionsGetSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/getPolicySetDefinitionVersion.json
     */
    /**
     * Sample code: Retrieve a policy set definition version.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        retrieveAPolicySetDefinitionVersion(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policySetDefinitionVersions()
            .getWithResponse("CostManagement", "1.2.1", null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicySetDefinitionVersions_GetAtManagementGroup

```java
/**
 * Samples for PolicySetDefinitionVersions GetAtManagementGroup.
 */
public final class PolicySetDefinitionVersionsGetAtManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/getPolicySetDefinitionVersionAtManagementGroup.json
     */
    /**
     * Sample code: Retrieve a policy set definition version at management group level.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void retrieveAPolicySetDefinitionVersionAtManagementGroupLevel(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policySetDefinitionVersions()
            .getAtManagementGroupWithResponse("MyManagementGroup", "CostManagement", "1.2.1", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### PolicySetDefinitionVersions_GetBuiltIn

```java
/**
 * Samples for PolicySetDefinitionVersions GetBuiltIn.
 */
public final class PolicySetDefinitionVersionsGetBuiltInSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/getBuiltInPolicySetDefinitionVersion.json
     */
    /**
     * Sample code: Retrieve a built-in policy set definition version.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        retrieveABuiltInPolicySetDefinitionVersion(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policySetDefinitionVersions()
            .getBuiltInWithResponse("1f3afdf9-d0c9-4c3d-847f-89da613e70a8", "1.2.1", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### PolicySetDefinitionVersions_List

```java
/**
 * Samples for PolicySetDefinitionVersions List.
 */
public final class PolicySetDefinitionVersionsListSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listPolicySetDefinitionVersions.json
     */
    /**
     * Sample code: List policy set definitions.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void listPolicySetDefinitions(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policySetDefinitionVersions().list("CostManagement", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicySetDefinitionVersions_ListAll

```java
/**
 * Samples for PolicySetDefinitionVersions ListAll.
 */
public final class PolicySetDefinitionVersionsListAllSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listAllPolicySetDefinitionVersions.json
     */
    /**
     * Sample code: List all policy definition versions at subscription.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void listAllPolicyDefinitionVersionsAtSubscription(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policySetDefinitionVersions().listAllWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### PolicySetDefinitionVersions_ListAllAtManagementGroup

```java
/**
 * Samples for PolicySetDefinitionVersions ListAllAtManagementGroup.
 */
public final class PolicySetDefinitionVersionsListAllAtManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listAllPolicySetDefinitionVersionsByManagementGroup.json
     */
    /**
     * Sample code: List all policy definition versions at management group.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void listAllPolicyDefinitionVersionsAtManagementGroup(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policySetDefinitionVersions()
            .listAllAtManagementGroupWithResponse("MyManagementGroup", com.azure.core.util.Context.NONE);
    }
}
```

### PolicySetDefinitionVersions_ListAllBuiltins

```java
/**
 * Samples for PolicySetDefinitionVersions ListAllBuiltins.
 */
public final class PolicySetDefinitionVersionsListAllBuiltinsSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listAllBuiltInPolicySetDefinitionVersions.json
     */
    /**
     * Sample code: List all built-in policy definition versions.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        listAllBuiltInPolicyDefinitionVersions(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policySetDefinitionVersions().listAllBuiltinsWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### PolicySetDefinitionVersions_ListBuiltIn

```java
/**
 * Samples for PolicySetDefinitionVersions ListBuiltIn.
 */
public final class PolicySetDefinitionVersionsListBuiltInSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listBuiltInPolicySetDefinitionVersions.json
     */
    /**
     * Sample code: List built-in policy set definitions.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        listBuiltInPolicySetDefinitions(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policySetDefinitionVersions()
            .listBuiltIn("1f3afdf9-d0c9-4c3d-847f-89da613e70a8", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicySetDefinitionVersions_ListByManagementGroup

```java
/**
 * Samples for PolicySetDefinitionVersions ListByManagementGroup.
 */
public final class PolicySetDefinitionVersionsListByManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listPolicySetDefinitionVersionsByManagementGroup.json
     */
    /**
     * Sample code: List policy set definitions at management group level.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void listPolicySetDefinitionsAtManagementGroupLevel(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policySetDefinitionVersions()
            .listByManagementGroup("MyManagementGroup", "CostManagement", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicySetDefinitions_CreateOrUpdate

```java
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.resources.policy.models.ParameterDefinitionsValue;
import com.azure.resourcemanager.resources.policy.models.ParameterDefinitionsValueMetadata;
import com.azure.resourcemanager.resources.policy.models.ParameterType;
import com.azure.resourcemanager.resources.policy.models.ParameterValuesValue;
import com.azure.resourcemanager.resources.policy.models.PolicyDefinitionGroup;
import com.azure.resourcemanager.resources.policy.models.PolicyDefinitionReference;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for PolicySetDefinitions CreateOrUpdate.
 */
public final class PolicySetDefinitionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/createOrUpdatePolicySetDefinitionWithGroups.json
     */
    /**
     * Sample code: Create or update a policy set definition with groups.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        createOrUpdateAPolicySetDefinitionWithGroups(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policySetDefinitions()
            .define("CostManagement")
            .withDisplayName("Cost Management")
            .withDescription("Policies to enforce low cost storage SKUs")
            .withMetadata(BinaryData.fromBytes("{category=Cost Management}".getBytes(StandardCharsets.UTF_8)))
            .withPolicyDefinitions(Arrays.asList(new PolicyDefinitionReference().withPolicyDefinitionId(
                "/subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/providers/Microsoft.Authorization/policyDefinitions/7433c107-6db4-4ad1-b57a-a76dce0154a1")
                .withDefinitionVersion("1.*.*")
                .withParameters(mapOf("listOfAllowedSKUs",
                    new ParameterValuesValue().withValue(
                        BinaryData.fromBytes("[Standard_GRS, Standard_LRS]".getBytes(StandardCharsets.UTF_8)))))
                .withPolicyDefinitionReferenceId("Limit_Skus")
                .withGroupNames(Arrays.asList("CostSaving")),
                new PolicyDefinitionReference().withPolicyDefinitionId(
                    "/subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/providers/Microsoft.Authorization/policyDefinitions/ResourceNaming")
                    .withDefinitionVersion("1.*.*")
                    .withParameters(mapOf("prefix",
                        new ParameterValuesValue()
                            .withValue(BinaryData.fromBytes("DeptA".getBytes(StandardCharsets.UTF_8))),
                        "suffix",
                        new ParameterValuesValue()
                            .withValue(BinaryData.fromBytes("-LC".getBytes(StandardCharsets.UTF_8)))))
                    .withPolicyDefinitionReferenceId("Resource_Naming")
                    .withGroupNames(Arrays.asList("Organizational"))))
            .withPolicyDefinitionGroups(Arrays.asList(
                new PolicyDefinitionGroup().withName("CostSaving")
                    .withDisplayName("Cost Management Policies")
                    .withDescription("Policies designed to control spend within a subscription."),
                new PolicyDefinitionGroup().withName("Organizational")
                    .withDisplayName("Organizational Policies")
                    .withDescription(
                        "Policies that help enforce resource organization standards within a subscription.")))
            .create();
    }

    /*
     * x-ms-original-file: 2026-01-01-preview/createOrUpdatePolicySetDefinition.json
     */
    /**
     * Sample code: Create or update a policy set definition.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        createOrUpdateAPolicySetDefinition(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policySetDefinitions()
            .define("CostManagement")
            .withDisplayName("Cost Management")
            .withDescription("Policies to enforce low cost storage SKUs")
            .withMetadata(BinaryData.fromBytes("{category=Cost Management}".getBytes(StandardCharsets.UTF_8)))
            .withParameters(
                mapOf("namePrefix",
                    new ParameterDefinitionsValue().withType(ParameterType.STRING)
                        .withDefaultValue(BinaryData.fromBytes("myPrefix".getBytes(StandardCharsets.UTF_8)))
                        .withMetadata(new ParameterDefinitionsValueMetadata()
                            .withDisplayName("Prefix to enforce on resource names")
                            .withAdditionalProperties(mapOf()))))
            .withPolicyDefinitions(Arrays.asList(new PolicyDefinitionReference().withPolicyDefinitionId(
                "/subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/providers/Microsoft.Authorization/policyDefinitions/7433c107-6db4-4ad1-b57a-a76dce0154a1")
                .withParameters(mapOf("listOfAllowedSKUs",
                    new ParameterValuesValue().withValue(
                        BinaryData.fromBytes("[Standard_GRS, Standard_LRS]".getBytes(StandardCharsets.UTF_8)))))
                .withPolicyDefinitionReferenceId("Limit_Skus"),
                new PolicyDefinitionReference().withPolicyDefinitionId(
                    "/subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/providers/Microsoft.Authorization/policyDefinitions/ResourceNaming")
                    .withParameters(mapOf("prefix",
                        new ParameterValuesValue().withValue(
                            BinaryData.fromBytes("[parameters('namePrefix')]".getBytes(StandardCharsets.UTF_8))),
                        "suffix",
                        new ParameterValuesValue()
                            .withValue(BinaryData.fromBytes("-LC".getBytes(StandardCharsets.UTF_8)))))
                    .withPolicyDefinitionReferenceId("Resource_Naming")))
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

### PolicySetDefinitions_CreateOrUpdateAtManagementGroup

```java
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.resources.policy.fluent.models.PolicySetDefinitionInner;
import com.azure.resourcemanager.resources.policy.models.ParameterValuesValue;
import com.azure.resourcemanager.resources.policy.models.PolicyDefinitionGroup;
import com.azure.resourcemanager.resources.policy.models.PolicyDefinitionReference;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for PolicySetDefinitions CreateOrUpdateAtManagementGroup.
 */
public final class PolicySetDefinitionsCreateOrUpdateAtManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/createOrUpdatePolicySetDefinitionWithGroupsAtManagementGroup.json
     */
    /**
     * Sample code: Create or update a policy set definition with groups at management group level.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void createOrUpdateAPolicySetDefinitionWithGroupsAtManagementGroupLevel(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policySetDefinitions()
            .createOrUpdateAtManagementGroupWithResponse("MyManagementGroup", "CostManagement",
                new PolicySetDefinitionInner().withDisplayName("Cost Management")
                    .withDescription("Policies to enforce low cost storage SKUs")
                    .withMetadata(BinaryData.fromBytes("{category=Cost Management}".getBytes(StandardCharsets.UTF_8)))
                    .withPolicyDefinitions(Arrays.asList(new PolicyDefinitionReference().withPolicyDefinitionId(
                        "/subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/providers/Microsoft.Authorization/policyDefinitions/7433c107-6db4-4ad1-b57a-a76dce0154a1")
                        .withParameters(mapOf("listOfAllowedSKUs",
                            new ParameterValuesValue().withValue(
                                BinaryData.fromBytes("[Standard_GRS, Standard_LRS]".getBytes(StandardCharsets.UTF_8)))))
                        .withPolicyDefinitionReferenceId("Limit_Skus")
                        .withGroupNames(Arrays.asList("CostSaving")),
                        new PolicyDefinitionReference().withPolicyDefinitionId(
                            "/subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/providers/Microsoft.Authorization/policyDefinitions/ResourceNaming")
                            .withParameters(mapOf("prefix",
                                new ParameterValuesValue()
                                    .withValue(BinaryData.fromBytes("DeptA".getBytes(StandardCharsets.UTF_8))),
                                "suffix",
                                new ParameterValuesValue()
                                    .withValue(BinaryData.fromBytes("-LC".getBytes(StandardCharsets.UTF_8)))))
                            .withPolicyDefinitionReferenceId("Resource_Naming")
                            .withGroupNames(Arrays.asList("Organizational"))))
                    .withPolicyDefinitionGroups(Arrays.asList(
                        new PolicyDefinitionGroup().withName("CostSaving")
                            .withDisplayName("Cost Management Policies")
                            .withDescription("Policies designed to control spend within a subscription."),
                        new PolicyDefinitionGroup().withName("Organizational")
                            .withDisplayName("Organizational Policies")
                            .withDescription(
                                "Policies that help enforce resource organization standards within a subscription."))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-01-01-preview/createOrUpdatePolicySetDefinitionAtManagementGroup.json
     */
    /**
     * Sample code: Create or update a policy set definition at management group level.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void createOrUpdateAPolicySetDefinitionAtManagementGroupLevel(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policySetDefinitions()
            .createOrUpdateAtManagementGroupWithResponse("MyManagementGroup", "CostManagement",
                new PolicySetDefinitionInner().withDisplayName("Cost Management")
                    .withDescription("Policies to enforce low cost storage SKUs")
                    .withMetadata(BinaryData.fromBytes("{category=Cost Management}".getBytes(StandardCharsets.UTF_8)))
                    .withPolicyDefinitions(Arrays.asList(new PolicyDefinitionReference().withPolicyDefinitionId(
                        "/providers/Microsoft.Management/managementGroups/MyManagementGroup/providers/Microsoft.Authorization/policyDefinitions/7433c107-6db4-4ad1-b57a-a76dce0154a1")
                        .withParameters(mapOf("listOfAllowedSKUs",
                            new ParameterValuesValue().withValue(
                                BinaryData.fromBytes("[Standard_GRS, Standard_LRS]".getBytes(StandardCharsets.UTF_8)))))
                        .withPolicyDefinitionReferenceId("Limit_Skus"),
                        new PolicyDefinitionReference().withPolicyDefinitionId(
                            "/providers/Microsoft.Management/managementGroups/MyManagementGroup/providers/Microsoft.Authorization/policyDefinitions/ResourceNaming")
                            .withParameters(mapOf("prefix",
                                new ParameterValuesValue()
                                    .withValue(BinaryData.fromBytes("DeptA".getBytes(StandardCharsets.UTF_8))),
                                "suffix",
                                new ParameterValuesValue()
                                    .withValue(BinaryData.fromBytes("-LC".getBytes(StandardCharsets.UTF_8)))))
                            .withPolicyDefinitionReferenceId("Resource_Naming"))),
                com.azure.core.util.Context.NONE);
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

### PolicySetDefinitions_Delete

```java
/**
 * Samples for PolicySetDefinitions Delete.
 */
public final class PolicySetDefinitionsDeleteSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/deletePolicySetDefinition.json
     */
    /**
     * Sample code: Delete a policy set definition.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void deleteAPolicySetDefinition(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policySetDefinitions().deleteWithResponse("CostManagement", com.azure.core.util.Context.NONE);
    }
}
```

### PolicySetDefinitions_DeleteAtManagementGroup

```java
/**
 * Samples for PolicySetDefinitions DeleteAtManagementGroup.
 */
public final class PolicySetDefinitionsDeleteAtManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/deletePolicySetDefinitionAtManagementGroup.json
     */
    /**
     * Sample code: Delete a policy set definition at management group level.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void deleteAPolicySetDefinitionAtManagementGroupLevel(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policySetDefinitions()
            .deleteAtManagementGroupWithResponse("MyManagementGroup", "CostManagement",
                com.azure.core.util.Context.NONE);
    }
}
```

### PolicySetDefinitions_Get

```java
/**
 * Samples for PolicySetDefinitions Get.
 */
public final class PolicySetDefinitionsGetSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/getPolicySetDefinition.json
     */
    /**
     * Sample code: Retrieve a policy set definition.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void retrieveAPolicySetDefinition(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policySetDefinitions().getWithResponse("CostManagement", null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicySetDefinitions_GetAtManagementGroup

```java
/**
 * Samples for PolicySetDefinitions GetAtManagementGroup.
 */
public final class PolicySetDefinitionsGetAtManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/getPolicySetDefinitionAtManagementGroup.json
     */
    /**
     * Sample code: Retrieve a policy set definition at management group level.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void retrieveAPolicySetDefinitionAtManagementGroupLevel(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policySetDefinitions()
            .getAtManagementGroupWithResponse("MyManagementGroup", "CostManagement", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### PolicySetDefinitions_GetBuiltIn

```java
/**
 * Samples for PolicySetDefinitions GetBuiltIn.
 */
public final class PolicySetDefinitionsGetBuiltInSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/getBuiltInPolicySetDefinition.json
     */
    /**
     * Sample code: Retrieve a built-in policy set definition.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        retrieveABuiltInPolicySetDefinition(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policySetDefinitions()
            .getBuiltInWithResponse("1f3afdf9-d0c9-4c3d-847f-89da613e70a8", null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicySetDefinitions_List

```java
/**
 * Samples for PolicySetDefinitions List.
 */
public final class PolicySetDefinitionsListSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listPolicySetDefinitions.json
     */
    /**
     * Sample code: List policy set definitions.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void listPolicySetDefinitions(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policySetDefinitions().list(null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicySetDefinitions_ListBuiltIn

```java
/**
 * Samples for PolicySetDefinitions ListBuiltIn.
 */
public final class PolicySetDefinitionsListBuiltInSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listBuiltInPolicySetDefinitions.json
     */
    /**
     * Sample code: List built-in policy set definitions.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        listBuiltInPolicySetDefinitions(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policySetDefinitions().listBuiltIn(null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicySetDefinitions_ListByManagementGroup

```java
/**
 * Samples for PolicySetDefinitions ListByManagementGroup.
 */
public final class PolicySetDefinitionsListByManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listPolicySetDefinitionsByManagementGroup.json
     */
    /**
     * Sample code: List policy set definitions at management group level.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void listPolicySetDefinitionsAtManagementGroupLevel(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policySetDefinitions()
            .listByManagementGroup("MyManagementGroup", null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### PolicyTokens_Acquire

```java
import com.azure.resourcemanager.resources.policy.models.PolicyTokenOperation;
import com.azure.resourcemanager.resources.policy.models.PolicyTokenRequest;

/**
 * Samples for PolicyTokens Acquire.
 */
public final class PolicyTokensAcquireSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/acquirePolicyToken.json
     */
    /**
     * Sample code: Acquire a policy token.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void acquireAPolicyToken(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyTokens()
            .acquireWithResponse(new PolicyTokenRequest().withOperation(new PolicyTokenOperation().withUri(
                "https://management.azure.com/subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/resourceGroups/testRG/providers/Microsoft.Compute/virtualMachines/testVM?api-version=2024-01-01")
                .withHttpMethod("delete")), com.azure.core.util.Context.NONE);
    }
}
```

### PolicyTokens_AcquireAtManagementGroup

```java
import com.azure.resourcemanager.resources.policy.models.PolicyTokenOperation;
import com.azure.resourcemanager.resources.policy.models.PolicyTokenRequest;

/**
 * Samples for PolicyTokens AcquireAtManagementGroup.
 */
public final class PolicyTokensAcquireAtManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/acquirePolicyTokenAtManagementGroup.json
     */
    /**
     * Sample code: Acquire a policy token at management group level.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        acquireAPolicyTokenAtManagementGroupLevel(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyTokens()
            .acquireAtManagementGroupWithResponse("MyManagementGroup",
                new PolicyTokenRequest().withOperation(new PolicyTokenOperation().withUri(
                    "https://management.azure.com/providers/Microsoft.Management/managementGroups/MyManagementGroup/providers/Microsoft.Authorization/roleAssignments/00000000-0000-0000-0000-000000000000?api-version=2022-04-01")
                    .withHttpMethod("delete")),
                com.azure.core.util.Context.NONE);
    }
}
```

### VariableValues_CreateOrUpdate

```java
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.resources.policy.models.PolicyVariableValueColumnValue;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Samples for VariableValues CreateOrUpdate.
 */
public final class VariableValuesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/createOrUpdateVariableValue.json
     */
    /**
     * Sample code: Create or update a variable value.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void createOrUpdateAVariableValue(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.variableValues()
            .define("TestValue")
            .withExistingVariable("DemoTestVariable")
            .withValues(Arrays.asList(
                new PolicyVariableValueColumnValue().withColumnName("StringColumn")
                    .withColumnValue(BinaryData.fromBytes("SampleValue".getBytes(StandardCharsets.UTF_8))),
                new PolicyVariableValueColumnValue().withColumnName("IntegerColumn")
                    .withColumnValue(BinaryData.fromBytes("10".getBytes(StandardCharsets.UTF_8)))))
            .create();
    }
}
```

### VariableValues_CreateOrUpdateAtManagementGroup

```java
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.resources.policy.fluent.models.VariableValueInner;
import com.azure.resourcemanager.resources.policy.models.PolicyVariableValueColumnValue;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Samples for VariableValues CreateOrUpdateAtManagementGroup.
 */
public final class VariableValuesCreateOrUpdateAtManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/createOrUpdateVariableValueAtManagementGroup.json
     */
    /**
     * Sample code: Create or update a variable value at management group.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void createOrUpdateAVariableValueAtManagementGroup(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.variableValues()
            .createOrUpdateAtManagementGroupWithResponse("DevOrg", "DemoTestVariable", "TestValue",
                new VariableValueInner().withValues(Arrays.asList(
                    new PolicyVariableValueColumnValue().withColumnName("StringColumn")
                        .withColumnValue(BinaryData.fromBytes("SampleValue".getBytes(StandardCharsets.UTF_8))),
                    new PolicyVariableValueColumnValue().withColumnName("IntegerColumn")
                        .withColumnValue(BinaryData.fromBytes("10".getBytes(StandardCharsets.UTF_8))))),
                com.azure.core.util.Context.NONE);
    }
}
```

### VariableValues_Delete

```java
/**
 * Samples for VariableValues Delete.
 */
public final class VariableValuesDeleteSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/deleteVariableValue.json
     */
    /**
     * Sample code: Delete a variable value.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void deleteAVariableValue(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.variableValues()
            .deleteByResourceGroupWithResponse("DemoTestVariable", "TestValue", com.azure.core.util.Context.NONE);
    }
}
```

### VariableValues_DeleteAtManagementGroup

```java
/**
 * Samples for VariableValues DeleteAtManagementGroup.
 */
public final class VariableValuesDeleteAtManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/deleteVariableValueAtManagementGroup.json
     */
    /**
     * Sample code: Delete a variable value at management group.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        deleteAVariableValueAtManagementGroup(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.variableValues()
            .deleteAtManagementGroupWithResponse("DevOrg", "DemoTestVariable", "TestValue",
                com.azure.core.util.Context.NONE);
    }
}
```

### VariableValues_Get

```java
/**
 * Samples for VariableValues Get.
 */
public final class VariableValuesGetSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/getVariableValue.json
     */
    /**
     * Sample code: Retrieve a variable value.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void retrieveAVariableValue(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.variableValues().getWithResponse("DemoTestVariable", "TestValue", com.azure.core.util.Context.NONE);
    }
}
```

### VariableValues_GetAtManagementGroup

```java
/**
 * Samples for VariableValues GetAtManagementGroup.
 */
public final class VariableValuesGetAtManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/getVariableValueAtManagementGroup.json
     */
    /**
     * Sample code: Retrieve a variable value at management group.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        retrieveAVariableValueAtManagementGroup(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.variableValues()
            .getAtManagementGroupWithResponse("DevOrg", "DemoTestVariable", "TestValue",
                com.azure.core.util.Context.NONE);
    }
}
```

### VariableValues_List

```java
/**
 * Samples for VariableValues List.
 */
public final class VariableValuesListSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listVariableValuesForSubscription.json
     */
    /**
     * Sample code: List variable values that apply to a variable at subscription level.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void listVariableValuesThatApplyToAVariableAtSubscriptionLevel(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.variableValues().list("DemoTestVariable", com.azure.core.util.Context.NONE);
    }
}
```

### VariableValues_ListForManagementGroup

```java
/**
 * Samples for VariableValues ListForManagementGroup.
 */
public final class VariableValuesListForManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listVariableValuesForManagementGroup.json
     */
    /**
     * Sample code: List variable values at a management group scope.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        listVariableValuesAtAManagementGroupScope(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.variableValues().listForManagementGroup("DevOrg", "DemoTestVariable", com.azure.core.util.Context.NONE);
    }
}
```

### Variables_CreateOrUpdate

```java
import com.azure.resourcemanager.resources.policy.models.PolicyVariableColumn;
import java.util.Arrays;

/**
 * Samples for Variables CreateOrUpdate.
 */
public final class VariablesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/createOrUpdateVariable.json
     */
    /**
     * Sample code: Create or update a variable.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void createOrUpdateAVariable(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.variables()
            .define("DemoTestVariable")
            .withColumns(Arrays.asList(new PolicyVariableColumn().withColumnName("TestColumn")))
            .create();
    }
}
```

### Variables_CreateOrUpdateAtManagementGroup

```java
import com.azure.resourcemanager.resources.policy.fluent.models.VariableInner;
import com.azure.resourcemanager.resources.policy.models.PolicyVariableColumn;
import java.util.Arrays;

/**
 * Samples for Variables CreateOrUpdateAtManagementGroup.
 */
public final class VariablesCreateOrUpdateAtManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/createOrUpdateVariableAtManagementGroup.json
     */
    /**
     * Sample code: Create or update a variable at management group.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        createOrUpdateAVariableAtManagementGroup(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.variables()
            .createOrUpdateAtManagementGroupWithResponse("DevOrg", "DemoTestVariable",
                new VariableInner().withColumns(Arrays.asList(new PolicyVariableColumn().withColumnName("TestColumn"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Variables_Delete

```java
/**
 * Samples for Variables Delete.
 */
public final class VariablesDeleteSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/deleteVariable.json
     */
    /**
     * Sample code: Delete a variable.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void deleteAVariable(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.variables().deleteWithResponse("DemoTestVariable", com.azure.core.util.Context.NONE);
    }
}
```

### Variables_DeleteAtManagementGroup

```java
/**
 * Samples for Variables DeleteAtManagementGroup.
 */
public final class VariablesDeleteAtManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/deleteVariableAtManagementGroup.json
     */
    /**
     * Sample code: Delete a variable at management group.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        deleteAVariableAtManagementGroup(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.variables()
            .deleteAtManagementGroupWithResponse("DevOrg", "DemoTestVariable", com.azure.core.util.Context.NONE);
    }
}
```

### Variables_Get

```java
/**
 * Samples for Variables Get.
 */
public final class VariablesGetSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/getVariable.json
     */
    /**
     * Sample code: Retrieve a variable.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void retrieveAVariable(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.variables().getWithResponse("DemoTestVariable", com.azure.core.util.Context.NONE);
    }
}
```

### Variables_GetAtManagementGroup

```java
/**
 * Samples for Variables GetAtManagementGroup.
 */
public final class VariablesGetAtManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/getVariableAtManagementGroup.json
     */
    /**
     * Sample code: Retrieve a variable at management group.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        retrieveAVariableAtManagementGroup(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.variables()
            .getAtManagementGroupWithResponse("DevOrg", "DemoTestVariable", com.azure.core.util.Context.NONE);
    }
}
```

### Variables_List

```java
/**
 * Samples for Variables List.
 */
public final class VariablesListSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listVariablesForSubscription.json
     */
    /**
     * Sample code: List variables that apply to a subscription.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        listVariablesThatApplyToASubscription(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.variables().list(com.azure.core.util.Context.NONE);
    }
}
```

### Variables_ListForManagementGroup

```java
/**
 * Samples for Variables ListForManagementGroup.
 */
public final class VariablesListForManagementGroupSamples {
    /*
     * x-ms-original-file: 2026-01-01-preview/listVariablesForManagementGroup.json
     */
    /**
     * Sample code: List variables that apply to a management group.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        listVariablesThatApplyToAManagementGroup(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.variables().listForManagementGroup("DevOrg", com.azure.core.util.Context.NONE);
    }
}
```

