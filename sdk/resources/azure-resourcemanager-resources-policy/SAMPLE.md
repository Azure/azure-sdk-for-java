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
- [AcquireAtResourceGroup](#policytokens_acquireatresourcegroup)
### DataPolicyManifests_GetByPolicyMode

```java
/**
 * Samples for DataPolicyManifests GetByPolicyMode.
 */
public final class DataPolicyManifestsGetByPolicyModeSamples {
    /*
     * x-ms-original-file: 2026-07-01/getDataPolicyManifest.json
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
     * x-ms-original-file: 2026-07-01/listDataPolicyManifests.json
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
     * x-ms-original-file: 2026-07-01/listDataPolicyManifestsNamespaceFilter.json
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
     * x-ms-original-file: 2026-07-01/createPolicyAssignmentWithResourceSelectors.json
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
     * x-ms-original-file: 2026-07-01/createPolicyAssignmentWithEnrollEnforcement.json
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
     * x-ms-original-file: 2026-07-01/createPolicyAssignment.json
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
     * x-ms-original-file: 2026-07-01/createPolicyAssignmentWithIdentity.json
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
     * x-ms-original-file: 2026-07-01/createPolicyAssignmentWithResourcePercentageSelector.json
     */
    /**
     * Sample code: Create or update a policy assignment with a resource percentage selector.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void createOrUpdateAPolicyAssignmentWithAResourcePercentageSelector(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyAssignments()
            .define("CostManagement")
            .withExistingScope("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2")
            .withDisplayName("Limit resources by rollout percentage")
            .withPolicyDefinitionId(
                "/subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/providers/Microsoft.Authorization/policySetDefinitions/CostManagement")
            .withDescription("Limit resources by rollout percentage")
            .withMetadata(BinaryData.fromBytes("{assignedBy=Special Someone}".getBytes(StandardCharsets.UTF_8)))
            .withResourceSelectors(Arrays.asList(new ResourceSelector().withName("SDPRollout")
                .withSelectors(Arrays
                    .asList(new Selector().withKind(SelectorKind.fromString("resourcePercentage")).withProgress(80)))))
            .create();
    }

    /*
     * x-ms-original-file: 2026-07-01/createPolicyAssignmentWithSelfserveExemptionSettings.json
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
     * x-ms-original-file: 2026-07-01/createPolicyAssignmentNonComplianceMessages.json
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
     * x-ms-original-file: 2026-07-01/createPolicyAssignmentWithUserAssignedIdentity.json
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
     * x-ms-original-file: 2026-07-01/createPolicyAssignmentWithOverrides.json
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
     * x-ms-original-file: 2026-07-01/createPolicyAssignmentWithoutEnforcement.json
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
     * x-ms-original-file: 2026-07-01/deletePolicyAssignment.json
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
     * x-ms-original-file: 2026-07-01/getPolicyAssignmentWithOverrides.json
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
     * x-ms-original-file: 2026-07-01/getPolicyAssignmentWithResourcePercentageSelector.json
     */
    /**
     * Sample code: Retrieve a policy assignment with a resource percentage selector.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void retrieveAPolicyAssignmentWithAResourcePercentageSelector(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyAssignments()
            .getWithResponse("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2", "CostManagement", null,
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-07-01/getPolicyAssignmentWithResourceSelectors.json
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
     * x-ms-original-file: 2026-07-01/getPolicyAssignmentWithUserAssignedIdentity.json
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
     * x-ms-original-file: 2026-07-01/getPolicyAssignment.json
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
     * x-ms-original-file: 2026-07-01/getPolicyAssignmentWithIdentity.json
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
     * x-ms-original-file: 2026-07-01/listPolicyAssignments.json
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
     * x-ms-original-file: 2026-07-01/listPolicyAssignmentsForResourceGroup.json
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
     * x-ms-original-file: 2026-07-01/listPolicyAssignmentsForManagementGroup.json
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
     * x-ms-original-file: 2026-07-01/listPolicyAssignmentsForResource.json
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
     * x-ms-original-file: 2026-07-01/updatePolicyAssignmentWithResourceSelectors.json
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
     * x-ms-original-file: 2026-07-01/updatePolicyAssignmentWithResourcePercentageSelector.json
     */
    /**
     * Sample code: Update a policy assignment with a resource percentage selector.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void updateAPolicyAssignmentWithAResourcePercentageSelector(
        com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        PolicyAssignment resource = manager.policyAssignments()
            .getWithResponse("subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2", "CostManagement", null,
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withResourceSelectors(Arrays.asList(new ResourceSelector().withName("SDPRollout")
                .withSelectors(Arrays
                    .asList(new Selector().withKind(SelectorKind.fromString("resourcePercentage")).withProgress(80)))))
            .apply();
    }

    /*
     * x-ms-original-file: 2026-07-01/updatePolicyAssignmentWithUserAssignedIdentity.json
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
     * x-ms-original-file: 2026-07-01/updatePolicyAssignmentWithOverrides.json
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
     * x-ms-original-file: 2026-07-01/updatePolicyAssignmentWithSelfserveExemptionSettings.json
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
     * x-ms-original-file: 2026-07-01/updatePolicyAssignmentWithIdentity.json
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
     * x-ms-original-file: 2026-07-01/createOrUpdatePolicyDefinitionVersion.json
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
     * x-ms-original-file: 2026-07-01/createOrUpdatePolicyDefinitionVersionAtManagementGroup.json
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
     * x-ms-original-file: 2026-07-01/deletePolicyDefinitionVersion.json
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
     * x-ms-original-file: 2026-07-01/deletePolicyDefinitionVersionAtManagementGroup.json
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
     * x-ms-original-file: 2026-07-01/getPolicyDefinitionVersion.json
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
     * x-ms-original-file: 2026-07-01/getPolicyDefinitionVersionAtManagementGroup.json
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
     * x-ms-original-file: 2026-07-01/getBuiltinPolicyDefinitionVersion.json
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
     * x-ms-original-file: 2026-07-01/listPolicyDefinitionVersions.json
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
     * x-ms-original-file: 2026-07-01/listAllPolicyDefinitionVersions.json
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
     * x-ms-original-file: 2026-07-01/listAllPolicyDefinitionVersionsByManagementGroup.json
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
     * x-ms-original-file: 2026-07-01/listAllBuiltInPolicyDefinitionVersions.json
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
     * x-ms-original-file: 2026-07-01/listBuiltInPolicyDefinitionVersions.json
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
     * x-ms-original-file: 2026-07-01/listPolicyDefinitionVersionsByManagementGroup.json
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
     * x-ms-original-file: 2026-07-01/createOrUpdatePolicyDefinitionAdvancedParams.json
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
     * x-ms-original-file: 2026-07-01/createOrUpdatePolicyDefinitionExternalEvaluationEnforcementSettings.json
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
     * x-ms-original-file: 2026-07-01/createOrUpdatePolicyDefinition.json
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
     * x-ms-original-file: 2026-07-01/createOrUpdatePolicyDefinitionAtManagementGroup.json
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
     * x-ms-original-file: 2026-07-01/deletePolicyDefinition.json
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
     * x-ms-original-file: 2026-07-01/deletePolicyDefinitionAtManagementGroup.json
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
     * x-ms-original-file: 2026-07-01/getPolicyDefinition.json
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
     * x-ms-original-file: 2026-07-01/getPolicyDefinitionAtManagementGroup.json
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
     * x-ms-original-file: 2026-07-01/getBuiltinPolicyDefinition.json
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
     * x-ms-original-file: 2026-07-01/listPolicyDefinitions.json
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
     * x-ms-original-file: 2026-07-01/listBuiltInPolicyDefinitions.json
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
     * x-ms-original-file: 2026-07-01/listPolicyDefinitionsByManagementGroup.json
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
     * x-ms-original-file: 2026-07-01/createOrUpdatePolicySetDefinitionVersion.json
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
     * x-ms-original-file: 2026-07-01/createOrUpdatePolicySetDefinitionVersionAtManagementGroup.json
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
                        "/providers/Microsoft.Management/managementgroups/MyManagementGroup/providers/Microsoft.Authorization/policyDefinitions/7433c107-6db4-4ad1-b57a-a76dce0154a1")
                        .withParameters(mapOf("listOfAllowedSKUs",
                            new ParameterValuesValue().withValue(
                                BinaryData.fromBytes("[Standard_GRS, Standard_LRS]".getBytes(StandardCharsets.UTF_8)))))
                        .withPolicyDefinitionReferenceId("Limit_Skus"),
                        new PolicyDefinitionReference().withPolicyDefinitionId(
                            "/providers/Microsoft.Management/managementgroups/MyManagementGroup/providers/Microsoft.Authorization/policyDefinitions/ResourceNaming")
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
     * x-ms-original-file: 2026-07-01/deletePolicySetDefinitionVersion.json
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
     * x-ms-original-file: 2026-07-01/deletePolicySetDefinitionVersionAtManagementGroup.json
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
     * x-ms-original-file: 2026-07-01/getPolicySetDefinitionVersion.json
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
     * x-ms-original-file: 2026-07-01/getPolicySetDefinitionVersionAtManagementGroup.json
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
     * x-ms-original-file: 2026-07-01/getBuiltInPolicySetDefinitionVersion.json
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
     * x-ms-original-file: 2026-07-01/listPolicySetDefinitionVersions.json
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
     * x-ms-original-file: 2026-07-01/listAllPolicySetDefinitionVersions.json
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
     * x-ms-original-file: 2026-07-01/listAllPolicySetDefinitionVersionsByManagementGroup.json
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
     * x-ms-original-file: 2026-07-01/listAllBuiltInPolicySetDefinitionVersions.json
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
     * x-ms-original-file: 2026-07-01/listBuiltInPolicySetDefinitionVersions.json
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
     * x-ms-original-file: 2026-07-01/listPolicySetDefinitionVersionsByManagementGroup.json
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
     * x-ms-original-file: 2026-07-01/createOrUpdatePolicySetDefinitionWithGroups.json
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
     * x-ms-original-file: 2026-07-01/createOrUpdatePolicySetDefinition.json
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
     * x-ms-original-file: 2026-07-01/createOrUpdatePolicySetDefinitionWithGroupsAtManagementGroup.json
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
     * x-ms-original-file: 2026-07-01/createOrUpdatePolicySetDefinitionAtManagementGroup.json
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
                        "/providers/Microsoft.Management/managementgroups/MyManagementGroup/providers/Microsoft.Authorization/policyDefinitions/7433c107-6db4-4ad1-b57a-a76dce0154a1")
                        .withParameters(mapOf("listOfAllowedSKUs",
                            new ParameterValuesValue().withValue(
                                BinaryData.fromBytes("[Standard_GRS, Standard_LRS]".getBytes(StandardCharsets.UTF_8)))))
                        .withPolicyDefinitionReferenceId("Limit_Skus"),
                        new PolicyDefinitionReference().withPolicyDefinitionId(
                            "/providers/Microsoft.Management/managementgroups/MyManagementGroup/providers/Microsoft.Authorization/policyDefinitions/ResourceNaming")
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
     * x-ms-original-file: 2026-07-01/deletePolicySetDefinition.json
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
     * x-ms-original-file: 2026-07-01/deletePolicySetDefinitionAtManagementGroup.json
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
     * x-ms-original-file: 2026-07-01/getPolicySetDefinition.json
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
     * x-ms-original-file: 2026-07-01/getPolicySetDefinitionAtManagementGroup.json
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
     * x-ms-original-file: 2026-07-01/getBuiltInPolicySetDefinition.json
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
     * x-ms-original-file: 2026-07-01/listPolicySetDefinitions.json
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
     * x-ms-original-file: 2026-07-01/listBuiltInPolicySetDefinitions.json
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
     * x-ms-original-file: 2026-07-01/listPolicySetDefinitionsByManagementGroup.json
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
     * x-ms-original-file: 2026-07-01/acquirePolicyToken.json
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
     * x-ms-original-file: 2026-07-01/acquirePolicyTokenAtManagementGroup.json
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

### PolicyTokens_AcquireAtResourceGroup

```java
import com.azure.resourcemanager.resources.policy.models.PolicyTokenOperation;
import com.azure.resourcemanager.resources.policy.models.PolicyTokenRequest;

/**
 * Samples for PolicyTokens AcquireAtResourceGroup.
 */
public final class PolicyTokensAcquireAtResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-07-01/acquirePolicyTokenAtResourceGroup.json
     */
    /**
     * Sample code: Acquire a policy token at resource group level.
     * 
     * @param manager Entry point to PolicyManager.
     */
    public static void
        acquireAPolicyTokenAtResourceGroupLevel(com.azure.resourcemanager.resources.policy.PolicyManager manager) {
        manager.policyTokens()
            .acquireAtResourceGroupWithResponse("testRG",
                new PolicyTokenRequest().withOperation(new PolicyTokenOperation().withUri(
                    "https://management.azure.com/subscriptions/ae640e6b-ba3e-4256-9d62-2993eecfa6f2/resourceGroups/testRG/providers/Microsoft.Compute/virtualMachines/testVM?api-version=2024-01-01")
                    .withHttpMethod("delete")),
                com.azure.core.util.Context.NONE);
    }
}
```

