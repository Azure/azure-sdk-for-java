# Code snippets and samples


## LandingZoneAccountOperations

- [Create](#landingzoneaccountoperations_create)
- [Delete](#landingzoneaccountoperations_delete)
- [GetByResourceGroup](#landingzoneaccountoperations_getbyresourcegroup)
- [List](#landingzoneaccountoperations_list)
- [ListByResourceGroup](#landingzoneaccountoperations_listbyresourcegroup)
- [Update](#landingzoneaccountoperations_update)

## LandingZoneConfigurationOperations

- [Create](#landingzoneconfigurationoperations_create)
- [CreateCopy](#landingzoneconfigurationoperations_createcopy)
- [Delete](#landingzoneconfigurationoperations_delete)
- [GenerateLandingZone](#landingzoneconfigurationoperations_generatelandingzone)
- [Get](#landingzoneconfigurationoperations_get)
- [ListByResourceGroup](#landingzoneconfigurationoperations_listbyresourcegroup)
- [ListBySubscription](#landingzoneconfigurationoperations_listbysubscription)
- [Update](#landingzoneconfigurationoperations_update)
- [UpdateAuthoringStatus](#landingzoneconfigurationoperations_updateauthoringstatus)

## LandingZoneRegistrationOperations

- [Create](#landingzoneregistrationoperations_create)
- [Delete](#landingzoneregistrationoperations_delete)
- [Get](#landingzoneregistrationoperations_get)
- [ListByResourceGroup](#landingzoneregistrationoperations_listbyresourcegroup)
- [ListBySubscription](#landingzoneregistrationoperations_listbysubscription)
- [Update](#landingzoneregistrationoperations_update)

## Operations

- [List](#operations_list)
### LandingZoneAccountOperations_Create

```java
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.LandingZoneAccountResourceProperties;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.ManagedServiceIdentity;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for LandingZoneAccountOperations Create.
 */
public final class LandingZoneAccountOperationsCreateSamples {
    /*
     * x-ms-original-file: 2025-02-27-preview/LandingZoneAccountOperations_Create.json
     */
    /**
     * Sample code: LandingZoneAccountOperations_Create.
     * 
     * @param manager Entry point to RegulatedEnvironmentManagementManager.
     */
    public static void landingZoneAccountOperationsCreate(
        com.azure.resourcemanager.regulatedenvironmentmanagement.RegulatedEnvironmentManagementManager manager) {
        manager.landingZoneAccountOperations()
            .define("ExampleLZA")
            .withRegion("northeurope")
            .withExistingResourceGroup("SampleResourceGroup")
            .withTags(mapOf("tag1", "MCFS"))
            .withProperties(new LandingZoneAccountResourceProperties().withStorageAccount(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg-examplegroup/providers/Microsoft.Storage/storageAccounts/saexample"))
            .withIdentity(new ManagedServiceIdentity()
                .withType(ManagedServiceIdentityType.fromString("SystemAssigned, UserAssigned"))
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg-examplegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/mi-exampleidentity",
                    new UserAssignedIdentity())))
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

### LandingZoneAccountOperations_Delete

```java
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.CustomNamingConvention;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.DecommissionedManagementGroupProperties;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.FirewallCreationOptions;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.LandingZoneConfigurationResourceProperties;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.LandingZoneManagementGroupProperties;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.ManagedIdentityProperties;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.ManagedIdentityResourceType;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.ManagementGroupProperties;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.PlatformManagementGroupProperties;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.PolicyInitiativeAssignmentProperties;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.ResourceCreationOptions;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.ResourceType;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.SandboxManagementGroupProperties;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.Tags;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for LandingZoneConfigurationOperations Create.
 */
public final class LandingZoneConfigurationOperationsCreateS {
    /*
     * x-ms-original-file: 2025-02-27-preview/LandingZoneConfigurationOperations_Create.json
     */
    /**
     * Sample code: LandingZoneConfigurationOperations_Create.
     * 
     * @param manager Entry point to RegulatedEnvironmentManagementManager.
     */
    public static void landingZoneConfigurationOperationsCreate(
        com.azure.resourcemanager.regulatedenvironmentmanagement.RegulatedEnvironmentManagementManager manager) {
        manager.landingZoneConfigurationOperations()
            .define("ExampleLZC")
            .withExistingLandingZoneAccount("rg-examplegroup", "ExampleLZA")
            .withProperties(new LandingZoneConfigurationResourceProperties()
                .withDdosProtectionCreationOption(ResourceCreationOptions.USE_EXISTING)
                .withExistingDdosProtectionId(
                    "/subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/rg-examplegroup/providers/Microsoft.Network/ddosProtectionPlans/ddos-example")
                .withLogAnalyticsWorkspaceCreationOption(ResourceCreationOptions.USE_EXISTING)
                .withExistingLogAnalyticsWorkspaceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/rg-examplegroup/providers/Microsoft.OperationalInsights/workspaces/log-example")
                .withTags(Arrays.asList(new Tags().withName("tag1").withValue("Lorem"),
                    new Tags().withName("tag2").withValue("Ipsum")))
                .withFirewallCreationOption(FirewallCreationOptions.PREMIUM)
                .withFirewallSubnetCidrBlock("10.20.254.0/24")
                .withGatewaySubnetCidrBlock("10.20.252.0/24")
                .withLogRetentionInDays(540L)
                .withHubNetworkCidrBlock("10.20.0.0/16")
                .withAzureBastionCreationOption(ResourceCreationOptions.USE_EXISTING)
                .withExistingAzureBastionId(
                    "/subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/rg-examplegroup/providers/Microsoft.Network/bastionHosts/bas-example")
                .withLandingZonesMgChildren(Arrays.asList(
                    new LandingZoneManagementGroupProperties()
                        .withPolicyInitiativesAssignmentProperties(Arrays.asList())
                        .withName("Corp"),
                    new LandingZoneManagementGroupProperties()
                        .withPolicyInitiativesAssignmentProperties(Arrays.asList())
                        .withName("Online"),
                    new LandingZoneManagementGroupProperties().withPolicyInitiativesAssignmentProperties(
                        Arrays.asList(new PolicyInitiativeAssignmentProperties().withPolicyInitiativeId(
                            "/providers/Microsoft.Authorization/policySetDefinitions/03de05a4-c324-4ccd-882f-a814ea8ab9ea")
                            .withAssignmentParameters(mapOf())))
                        .withName("Confidential Corp"),
                    new LandingZoneManagementGroupProperties().withPolicyInitiativesAssignmentProperties(
                        Arrays.asList(new PolicyInitiativeAssignmentProperties().withPolicyInitiativeId(
                            "/providers/Microsoft.Authorization/policySetDefinitions/03de05a4-c324-4ccd-882f-a814ea8ab9ea")
                            .withAssignmentParameters(mapOf())))
                        .withName("Confidential Online")))
                .withTopLevelMgMetadata(new ManagementGroupProperties().withPolicyInitiativesAssignmentProperties(
                    Arrays.asList(new PolicyInitiativeAssignmentProperties().withPolicyInitiativeId(
                        "/providers/Microsoft.Authorization/policySetDefinitions/c1cbff38-87c0-4b9f-9f70-035c7a3b5523")
                        .withAssignmentParameters(mapOf("listOfAllowedLocations",
                            BinaryData
                                .fromBytes("[swedencentral, eastus2, uksouth]".getBytes(StandardCharsets.UTF_8)))))))
                .withLandingZonesMgMetadata(new ManagementGroupProperties().withPolicyInitiativesAssignmentProperties(
                    Arrays.asList(new PolicyInitiativeAssignmentProperties().withPolicyInitiativeId(
                        "/providers/Microsoft.Authorization/policySetDefinitions/c1cbff38-87c0-4b9f-9f70-035c7a3b5523")
                        .withAssignmentParameters(mapOf("listOfAllowedLocations",
                            BinaryData.fromBytes("[swedencentral]".getBytes(StandardCharsets.UTF_8)))))))
                .withPlatformMgMetadata(new ManagementGroupProperties().withPolicyInitiativesAssignmentProperties(
                    Arrays.asList(new PolicyInitiativeAssignmentProperties().withPolicyInitiativeId(
                        "/providers/Microsoft.Authorization/policySetDefinitions/c1cbff38-87c0-4b9f-9f70-035c7a3b5523")
                        .withAssignmentParameters(mapOf("listOfAllowedLocations",
                            BinaryData.fromBytes("[swedencentral]".getBytes(StandardCharsets.UTF_8)))))))
                .withPlatformManagementMgMetadata(new ManagementGroupProperties()
                    .withPolicyInitiativesAssignmentProperties(Arrays.asList(new PolicyInitiativeAssignmentProperties()
                        .withPolicyInitiativeId(
                            "/providers/Microsoft.Authorization/policySetDefinitions/c1cbff38-87c0-4b9f-9f70-035c7a3b5523")
                        .withAssignmentParameters(mapOf("listOfAllowedLocations",
                            BinaryData.fromBytes("[swedencentral]".getBytes(StandardCharsets.UTF_8)))))))
                .withPlatformConnectivityMgMetadata(new ManagementGroupProperties()
                    .withPolicyInitiativesAssignmentProperties(Arrays.asList(new PolicyInitiativeAssignmentProperties()
                        .withPolicyInitiativeId(
                            "/providers/Microsoft.Authorization/policySetDefinitions/c1cbff38-87c0-4b9f-9f70-035c7a3b5523")
                        .withAssignmentParameters(mapOf("listOfAllowedLocations",
                            BinaryData.fromBytes("[swedencentral]".getBytes(StandardCharsets.UTF_8)))))))
                .withPlatformIdentityMgMetadata(new ManagementGroupProperties()
                    .withPolicyInitiativesAssignmentProperties(Arrays.asList(new PolicyInitiativeAssignmentProperties()
                        .withPolicyInitiativeId(
                            "/providers/Microsoft.Authorization/policySetDefinitions/c1cbff38-87c0-4b9f-9f70-035c7a3b5523")
                        .withAssignmentParameters(mapOf("listOfAllowedLocations",
                            BinaryData.fromBytes("[swedencentral]".getBytes(StandardCharsets.UTF_8)))))))
                .withDecommissionedMgMetadata(new DecommissionedManagementGroupProperties()
                    .withPolicyInitiativesAssignmentProperties(Arrays.asList(new PolicyInitiativeAssignmentProperties()
                        .withPolicyInitiativeId(
                            "/providers/Microsoft.Authorization/policySetDefinitions/c1cbff38-87c0-4b9f-9f70-035c7a3b5523")
                        .withAssignmentParameters(mapOf("listOfAllowedLocations",
                            BinaryData.fromBytes("[swedencentral]".getBytes(StandardCharsets.UTF_8))))))
                    .withCreate(true))
                .withSandboxMgMetadata(new SandboxManagementGroupProperties().withPolicyInitiativesAssignmentProperties(
                    Arrays.asList(new PolicyInitiativeAssignmentProperties().withPolicyInitiativeId(
                        "/providers/Microsoft.Authorization/policySetDefinitions/c1cbff38-87c0-4b9f-9f70-035c7a3b5523")
                        .withAssignmentParameters(mapOf("listOfAllowedLocations",
                            BinaryData.fromBytes("[swedencentral]".getBytes(StandardCharsets.UTF_8))))))
                    .withCreate(true))
                .withManagedIdentity(new ManagedIdentityProperties().withType(ManagedIdentityResourceType.USER_ASSIGNED)
                    .withUserAssignedIdentityResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/rg-examplegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/mi-example"))
                .withPlatformMgChildren(Arrays.asList(new PlatformManagementGroupProperties()
                    .withPolicyInitiativesAssignmentProperties(Arrays.asList(new PolicyInitiativeAssignmentProperties()
                        .withPolicyInitiativeId(
                            "/providers/Microsoft.Authorization/policySetDefinitions/c1cbff38-87c0-4b9f-9f70-035c7a3b5523")
                        .withAssignmentParameters(mapOf("listOfAllowedLocations",
                            BinaryData.fromBytes("[swedencentral]".getBytes(StandardCharsets.UTF_8))))))
                    .withName("Telemetry"),
                    new PlatformManagementGroupProperties().withPolicyInitiativesAssignmentProperties(
                        Arrays.asList(new PolicyInitiativeAssignmentProperties().withPolicyInitiativeId(
                            "/providers/Microsoft.Authorization/policySetDefinitions/c1cbff38-87c0-4b9f-9f70-035c7a3b5523")
                            .withAssignmentParameters(mapOf("listOfAllowedLocations",
                                BinaryData.fromBytes("[swedencentral]".getBytes(StandardCharsets.UTF_8))))))
                        .withName("Security")))
                .withNamingConventionFormula(
                    "{ResourceTypeAbbreviation}-{DeploymentPrefix}-Contoso-{DeploymentSuffix}-{Environment}")
                .withCustomNamingConvention(
                    Arrays.asList(new CustomNamingConvention().withResourceType(ResourceType.DDOS_PROTECTION_PLANS)
                        .withFormula("{ResourceTypeAbbreviation}-{DeploymentPrefix}-Contoso-{DeploymentSuffix}"))))
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

### LandingZoneAccountOperations_GetByResourceGroup

```java
/**
 * Samples for LandingZoneConfigurationOperations Delete.
 */
public final class LandingZoneConfigurationOperationsDeleteS {
    /*
     * x-ms-original-file: 2025-02-27-preview/LandingZoneConfigurationOperations_Delete.json
     */
    /**
     * Sample code: LandingZoneConfigurationOperations_Delete.
     * 
     * @param manager Entry point to RegulatedEnvironmentManagementManager.
     */
    public static void landingZoneConfigurationOperationsDelete(
        com.azure.resourcemanager.regulatedenvironmentmanagement.RegulatedEnvironmentManagementManager manager) {
        manager.landingZoneConfigurationOperations()
            .delete("ExampleResourceGroup", "SampleLZA", "ExampleLZC", com.azure.core.util.Context.NONE);
    }
}
```

### LandingZoneAccountOperations_List

```java
import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.CustomNamingConvention;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.DecommissionedManagementGroupProperties;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.FirewallCreationOptions;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.LandingZoneConfigurationResource;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.LandingZoneConfigurationResourceProperties;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.LandingZoneManagementGroupProperties;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.ManagedIdentityProperties;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.ManagedIdentityResourceType;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.ManagementGroupProperties;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.PlatformManagementGroupProperties;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.PolicyInitiativeAssignmentProperties;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.ResourceCreationOptions;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.ResourceType;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.SandboxManagementGroupProperties;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.Tags;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for LandingZoneConfigurationOperations Update.
 */
public final class LandingZoneConfigurationOperationsUpdateS {
    /*
     * x-ms-original-file: 2025-02-27-preview/LandingZoneConfigurationOperations_Update.json
     */
    /**
     * Sample code: LandingZoneConfigurationOperations_Update.
     * 
     * @param manager Entry point to RegulatedEnvironmentManagementManager.
     */
    public static void landingZoneConfigurationOperationsUpdate(
        com.azure.resourcemanager.regulatedenvironmentmanagement.RegulatedEnvironmentManagementManager manager) {
        LandingZoneConfigurationResource resource = manager.landingZoneConfigurationOperations()
            .getWithResponse("ExampleResourceGroup", "ExampleLZA", "ExampleLZC", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new LandingZoneConfigurationResourceProperties()
                .withDdosProtectionCreationOption(ResourceCreationOptions.YES)
                .withLogAnalyticsWorkspaceCreationOption(ResourceCreationOptions.NO)
                .withTags(Arrays.asList(new Tags().withName("tag1").withValue("do"),
                    new Tags().withName("tag2").withValue("do")))
                .withFirewallCreationOption(FirewallCreationOptions.STANDARD)
                .withFirewallSubnetCidrBlock("10.20.255.0/24")
                .withGatewaySubnetCidrBlock("10.20.255.0/24")
                .withLogRetentionInDays(540L)
                .withHubNetworkCidrBlock("10.20.255.0/24")
                .withAzureBastionCreationOption(ResourceCreationOptions.YES)
                .withAzureBastionSubnetCidrBlock("10.20.255.0/24")
                .withLandingZonesMgChildren(Arrays.asList(new LandingZoneManagementGroupProperties()
                    .withPolicyInitiativesAssignmentProperties(Arrays.asList())
                    .withName("Corp")))
                .withTopLevelMgMetadata(new ManagementGroupProperties().withPolicyInitiativesAssignmentProperties(
                    Arrays.asList(new PolicyInitiativeAssignmentProperties().withPolicyInitiativeId(
                        "/providers/Microsoft.Authorization/policySetDefinitions/0a2ebd47-3fb9-4735-a006-b7f31ddadd9f")
                        .withAssignmentParameters(
                            mapOf("Effect", BinaryData.fromBytes("Audit".getBytes(StandardCharsets.UTF_8)))))))
                .withLandingZonesMgMetadata(
                    new ManagementGroupProperties().withPolicyInitiativesAssignmentProperties(Arrays.asList()))
                .withPlatformMgMetadata(
                    new ManagementGroupProperties().withPolicyInitiativesAssignmentProperties(Arrays.asList()))
                .withPlatformManagementMgMetadata(
                    new ManagementGroupProperties().withPolicyInitiativesAssignmentProperties(Arrays.asList()))
                .withPlatformConnectivityMgMetadata(
                    new ManagementGroupProperties().withPolicyInitiativesAssignmentProperties(Arrays.asList()))
                .withPlatformIdentityMgMetadata(
                    new ManagementGroupProperties().withPolicyInitiativesAssignmentProperties(Arrays.asList()))
                .withDecommissionedMgMetadata(new DecommissionedManagementGroupProperties()
                    .withPolicyInitiativesAssignmentProperties(Arrays.asList())
                    .withCreate(false))
                .withSandboxMgMetadata(
                    new SandboxManagementGroupProperties().withPolicyInitiativesAssignmentProperties(Arrays.asList())
                        .withCreate(true))
                .withManagedIdentity(
                    new ManagedIdentityProperties().withType(ManagedIdentityResourceType.SYSTEM_ASSIGNED))
                .withPlatformMgChildren(Arrays.asList(
                    new PlatformManagementGroupProperties().withPolicyInitiativesAssignmentProperties(Arrays.asList())
                        .withName("childmg1"),
                    new PlatformManagementGroupProperties().withPolicyInitiativesAssignmentProperties(Arrays.asList())
                        .withName("childmg2")))
                .withNamingConventionFormula("{ResourceTypeAbbreviation}")
                .withCustomNamingConvention(
                    Arrays.asList(new CustomNamingConvention().withResourceType(ResourceType.AZURE_FIREWALLS)
                        .withFormula("{DeploymentSuffix}"))))
            .apply();
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

### LandingZoneAccountOperations_ListByResourceGroup

```java
/**
 * Samples for LandingZoneAccountOperations GetByResourceGroup.
 */
public final class LandingZoneAccountOperationsGetByResource {
    /*
     * x-ms-original-file: 2025-02-27-preview/LandingZoneAccountOperations_Get.json
     */
    /**
     * Sample code: LandingZoneAccountOperations_Get.
     * 
     * @param manager Entry point to RegulatedEnvironmentManagementManager.
     */
    public static void landingZoneAccountOperationsGet(
        com.azure.resourcemanager.regulatedenvironmentmanagement.RegulatedEnvironmentManagementManager manager) {
        manager.landingZoneAccountOperations()
            .getByResourceGroupWithResponse("SampleResourceGroup", "SampleLZA", com.azure.core.util.Context.NONE);
    }
}
```

### LandingZoneAccountOperations_Update

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2025-02-27-preview/Operations_List.json
     */
    /**
     * Sample code: Operations_List_MaximumSet_Gen - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to RegulatedEnvironmentManagementManager.
     */
    public static void operationsListMaximumSetGenGeneratedByMaximumSetRule(
        com.azure.resourcemanager.regulatedenvironmentmanagement.RegulatedEnvironmentManagementManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### LandingZoneConfigurationOperations_Create

```java
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.GenerateLandingZoneRequest;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.InfrastructureAsCodeOutputOptions;

/**
 * Samples for LandingZoneConfigurationOperations GenerateLandingZone.
 */
public final class LandingZoneConfigurationOperationsGenerat {
    /*
     * x-ms-original-file: 2025-02-27-preview/LandingZoneConfigurationOperations_GenerateLandingZone.json
     */
    /**
     * Sample code: LandingZoneConfigurationOperations_GenerateLandingZone.
     * 
     * @param manager Entry point to RegulatedEnvironmentManagementManager.
     */
    public static void landingZoneConfigurationOperationsGenerateLandingZone(
        com.azure.resourcemanager.regulatedenvironmentmanagement.RegulatedEnvironmentManagementManager manager) {
        manager.landingZoneConfigurationOperations()
            .generateLandingZone("ExampleResourceGroup", "SampleLZA", "ExampleLZC",
                new GenerateLandingZoneRequest()
                    .withInfrastructureAsCodeOutputOptions(InfrastructureAsCodeOutputOptions.BICEP)
                    .withExistingManagementSubscriptionId("/subscriptions/00000000-0000-0000-0000-000000000001")
                    .withExistingIdentitySubscriptionId("/subscriptions/00000000-0000-0000-0000-000000000002")
                    .withExistingConnectivitySubscriptionId("/subscriptions/00000000-0000-0000-0000-000000000003")
                    .withDeploymentPrefix("mcfs")
                    .withDeploymentSuffix("test")
                    .withTopLevelMgDisplayName("TestMG")
                    .withDeploymentLocation("eastus")
                    .withOrganization("test")
                    .withEnvironment("QA"),
                com.azure.core.util.Context.NONE);
    }
}
```

### LandingZoneConfigurationOperations_CreateCopy

```java
/**
 * Samples for LandingZoneAccountOperations List.
 */
public final class LandingZoneAccountOperationsListSamples {
    /*
     * x-ms-original-file: 2025-02-27-preview/LandingZoneAccountOperations_ListBySubscription.json
     */
    /**
     * Sample code: LandingZoneAccountOperations_ListBySubscription.
     * 
     * @param manager Entry point to RegulatedEnvironmentManagementManager.
     */
    public static void landingZoneAccountOperationsListBySubscription(
        com.azure.resourcemanager.regulatedenvironmentmanagement.RegulatedEnvironmentManagementManager manager) {
        manager.landingZoneAccountOperations().list(com.azure.core.util.Context.NONE);
    }
}
```

### LandingZoneConfigurationOperations_Delete

```java
/**
 * Samples for LandingZoneConfigurationOperations ListBySubscription.
 */
public final class LandingZoneConfigurationOperationsListByS {
    /*
     * x-ms-original-file: 2025-02-27-preview/LandingZoneConfigurationOperations_ListBySubscription.json
     */
    /**
     * Sample code: LandingZoneConfigurationOperations_ListBySubscription.
     * 
     * @param manager Entry point to RegulatedEnvironmentManagementManager.
     */
    public static void landingZoneConfigurationOperationsListBySubscription(
        com.azure.resourcemanager.regulatedenvironmentmanagement.RegulatedEnvironmentManagementManager manager) {
        manager.landingZoneConfigurationOperations().listBySubscription("ExampleLZA", com.azure.core.util.Context.NONE);
    }
}
```

### LandingZoneConfigurationOperations_GenerateLandingZone

```java
/**
 * Samples for LandingZoneConfigurationOperations Get.
 */
public final class LandingZoneConfigurationOperationsGetSamp {
    /*
     * x-ms-original-file: 2025-02-27-preview/LandingZoneConfigurationOperations_Get.json
     */
    /**
     * Sample code: LandingZoneConfigurationOperations_Get.
     * 
     * @param manager Entry point to RegulatedEnvironmentManagementManager.
     */
    public static void landingZoneConfigurationOperationsGet(
        com.azure.resourcemanager.regulatedenvironmentmanagement.RegulatedEnvironmentManagementManager manager) {
        manager.landingZoneConfigurationOperations()
            .getWithResponse("rgsovereign", "SampleLZA", "SampleLZC", com.azure.core.util.Context.NONE);
    }
}
```

### LandingZoneConfigurationOperations_Get

```java
/**
 * Samples for LandingZoneAccountOperations ListByResourceGroup.
 */
public final class LandingZoneAccountOperationsListByResourc {
    /*
     * x-ms-original-file: 2025-02-27-preview/LandingZoneAccountOperations_ListByResourceGroup.json
     */
    /**
     * Sample code: LandingZoneAccountOperations_ListByResourceGroup_MaximumSet_Gen - generated by [MaximumSet] rule.
     * 
     * @param manager Entry point to RegulatedEnvironmentManagementManager.
     */
    public static void landingZoneAccountOperationsListByResourceGroupMaximumSetGenGeneratedByMaximumSetRule(
        com.azure.resourcemanager.regulatedenvironmentmanagement.RegulatedEnvironmentManagementManager manager) {
        manager.landingZoneAccountOperations()
            .listByResourceGroup("SampleResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### LandingZoneConfigurationOperations_ListByResourceGroup

```java
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.CreateLandingZoneConfigurationCopyRequest;

/**
 * Samples for LandingZoneConfigurationOperations CreateCopy.
 */
public final class LandingZoneConfigurationOperationsCreateC {
    /*
     * x-ms-original-file: 2025-02-27-preview/LandingZoneConfigurationOperations_CreateCopy.json
     */
    /**
     * Sample code: LandingZoneConfigurationOperations_CreateCopy.
     * 
     * @param manager Entry point to RegulatedEnvironmentManagementManager.
     */
    public static void landingZoneConfigurationOperationsCreateCopy(
        com.azure.resourcemanager.regulatedenvironmentmanagement.RegulatedEnvironmentManagementManager manager) {
        manager.landingZoneConfigurationOperations()
            .createCopy("ExampleResourceGroup", "SampleLZA", "ExampleLZC",
                new CreateLandingZoneConfigurationCopyRequest().withName("LandingZoneConfiguration"),
                com.azure.core.util.Context.NONE);
    }
}
```

### LandingZoneConfigurationOperations_ListBySubscription

```java
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.LandingZoneAccountResource;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.LandingZoneAccountResourceProperties;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.ManagedServiceIdentity;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.ManagedServiceIdentityType;

/**
 * Samples for LandingZoneAccountOperations Update.
 */
public final class LandingZoneAccountOperationsUpdateSamples {
    /*
     * x-ms-original-file: 2025-02-27-preview/LandingZoneAccountOperations_Update.json
     */
    /**
     * Sample code: LandingZoneAccountOperations_Update.
     * 
     * @param manager Entry point to RegulatedEnvironmentManagementManager.
     */
    public static void landingZoneAccountOperationsUpdate(
        com.azure.resourcemanager.regulatedenvironmentmanagement.RegulatedEnvironmentManagementManager manager) {
        LandingZoneAccountResource resource = manager.landingZoneAccountOperations()
            .getByResourceGroupWithResponse("ExampleResourceGroup", "ExampleLZA", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new LandingZoneAccountResourceProperties().withStorageAccount(
                "/subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/TestStorageAccount/providers/Microsoft.Storage/storageAccounts/teststcontainer"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED))
            .apply();
    }
}
```

### LandingZoneConfigurationOperations_Update

```java
/**
 * Samples for LandingZoneRegistrationOperations ListBySubscription.
 */
public final class LandingZoneRegistrationOperationsListBySu {
    /*
     * x-ms-original-file: 2025-02-27-preview/LandingZoneRegistrationOperations_ListBySubscription.json
     */
    /**
     * Sample code: LandingZoneRegistrationOperations_ListBySubscription.
     * 
     * @param manager Entry point to RegulatedEnvironmentManagementManager.
     */
    public static void landingZoneRegistrationOperationsListBySubscription(
        com.azure.resourcemanager.regulatedenvironmentmanagement.RegulatedEnvironmentManagementManager manager) {
        manager.landingZoneRegistrationOperations().listBySubscription("ExampleLZA", com.azure.core.util.Context.NONE);
    }
}
```

### LandingZoneConfigurationOperations_UpdateAuthoringStatus

```java
/**
 * Samples for LandingZoneAccountOperations Delete.
 */
public final class LandingZoneAccountOperationsDeleteSamples {
    /*
     * x-ms-original-file: 2025-02-27-preview/LandingZoneAccountOperations_Delete.json
     */
    /**
     * Sample code: LandingZoneAccountOperations_Delete.
     * 
     * @param manager Entry point to RegulatedEnvironmentManagementManager.
     */
    public static void landingZoneAccountOperationsDelete(
        com.azure.resourcemanager.regulatedenvironmentmanagement.RegulatedEnvironmentManagementManager manager) {
        manager.landingZoneAccountOperations()
            .delete("SampleResourceGroup", "SampleLZA", com.azure.core.util.Context.NONE);
    }
}
```

### LandingZoneRegistrationOperations_Create

```java
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.LandingZoneRegistrationResource;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.LandingZoneRegistrationResourceProperties;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.ManagedIdentityProperties;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.ManagedIdentityResourceType;

/**
 * Samples for LandingZoneRegistrationOperations Update.
 */
public final class LandingZoneRegistrationOperationsUpdateSa {
    /*
     * x-ms-original-file: 2025-02-27-preview/LandingZoneRegistrationOperations_Update.json
     */
    /**
     * Sample code: LandingZoneRegistrationOperations_Update.
     * 
     * @param manager Entry point to RegulatedEnvironmentManagementManager.
     */
    public static void landingZoneRegistrationOperationsUpdate(
        com.azure.resourcemanager.regulatedenvironmentmanagement.RegulatedEnvironmentManagementManager manager) {
        LandingZoneRegistrationResource resource = manager.landingZoneRegistrationOperations()
            .getWithResponse("ExampleResourceGroup", "ExampleLZA", "ExampleLZR", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new LandingZoneRegistrationResourceProperties()
                .withExistingTopLevelMgId("/providers/Microsoft.Management/managementGroups/mcfs")
                .withExistingLandingZoneConfigurationId(
                    "/subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/TestResourceGroup/providers/Microsoft.Sovereign/landingZoneAccounts/ExampleLZA/landingZoneConfigurations/ExampleLZC")
                .withManagedIdentity(
                    new ManagedIdentityProperties().withType(ManagedIdentityResourceType.SYSTEM_ASSIGNED)))
            .apply();
    }
}
```

### LandingZoneRegistrationOperations_Delete

```java
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.LandingZoneRegistrationResourceProperties;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.ManagedIdentityProperties;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.ManagedIdentityResourceType;

/**
 * Samples for LandingZoneRegistrationOperations Create.
 */
public final class LandingZoneRegistrationOperationsCreateSa {
    /*
     * x-ms-original-file: 2025-02-27-preview/LandingZoneRegistrationOperations_Create.json
     */
    /**
     * Sample code: LandingZoneRegistrationOperations_Create.
     * 
     * @param manager Entry point to RegulatedEnvironmentManagementManager.
     */
    public static void landingZoneRegistrationOperationsCreate(
        com.azure.resourcemanager.regulatedenvironmentmanagement.RegulatedEnvironmentManagementManager manager) {
        manager.landingZoneRegistrationOperations()
            .define("ExampleLZR")
            .withExistingLandingZoneAccount("ExampleResourceGroup", "ExampleLZA")
            .withProperties(new LandingZoneRegistrationResourceProperties()
                .withExistingTopLevelMgId("/providers/Microsoft.Management/managementGroups/mg-example")
                .withExistingLandingZoneConfigurationId(
                    "/subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/rg-examplegroup/providers/Microsoft.Sovereign/landingZoneAccounts/ExampleLZA/landingZoneConfigurations/ExampleLZC")
                .withManagedIdentity(new ManagedIdentityProperties().withType(ManagedIdentityResourceType.USER_ASSIGNED)
                    .withUserAssignedIdentityResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000001/resourceGroups/rg-examplegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/mi-example")))
            .create();
    }
}
```

### LandingZoneRegistrationOperations_Get

```java
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.AuthoringStatus;
import com.azure.resourcemanager.regulatedenvironmentmanagement.models.UpdateAuthoringStatusRequest;

/**
 * Samples for LandingZoneConfigurationOperations UpdateAuthoringStatus.
 */
public final class LandingZoneConfigurationOperationsUpdateA {
    /*
     * x-ms-original-file: 2025-02-27-preview/LandingZoneConfigurationOperations_UpdateAuthoringStatus.json
     */
    /**
     * Sample code: LandingZoneConfigurationOperations_UpdateAuthoringStatus.
     * 
     * @param manager Entry point to RegulatedEnvironmentManagementManager.
     */
    public static void landingZoneConfigurationOperationsUpdateAuthoringStatus(
        com.azure.resourcemanager.regulatedenvironmentmanagement.RegulatedEnvironmentManagementManager manager) {
        manager.landingZoneConfigurationOperations()
            .updateAuthoringStatus("ExampleResourceGroup", "ExampleLZA", "ExampleLZC",
                new UpdateAuthoringStatusRequest().withAuthoringStatus(AuthoringStatus.AUTHORING),
                com.azure.core.util.Context.NONE);
    }
}
```

### LandingZoneRegistrationOperations_ListByResourceGroup

```java
/**
 * Samples for LandingZoneRegistrationOperations ListByResourceGroup.
 */
public final class LandingZoneRegistrationOperationsListByRe {
    /*
     * x-ms-original-file: 2025-02-27-preview/LandingZoneRegistrationOperations_ListByResourceGroup.json
     */
    /**
     * Sample code: LandingZoneRegistrationOperations_ListByResourceGroup.
     * 
     * @param manager Entry point to RegulatedEnvironmentManagementManager.
     */
    public static void landingZoneRegistrationOperationsListByResourceGroup(
        com.azure.resourcemanager.regulatedenvironmentmanagement.RegulatedEnvironmentManagementManager manager) {
        manager.landingZoneRegistrationOperations()
            .listByResourceGroup("ExampleResourceGroup", "ExampleLZA", com.azure.core.util.Context.NONE);
    }
}
```

### LandingZoneRegistrationOperations_ListBySubscription

```java
/**
 * Samples for LandingZoneRegistrationOperations Delete.
 */
public final class LandingZoneRegistrationOperationsDeleteSa {
    /*
     * x-ms-original-file: 2025-02-27-preview/LandingZoneRegistrationOperations_Delete.json
     */
    /**
     * Sample code: LandingZoneRegistrationOperations_Delete.
     * 
     * @param manager Entry point to RegulatedEnvironmentManagementManager.
     */
    public static void landingZoneRegistrationOperationsDelete(
        com.azure.resourcemanager.regulatedenvironmentmanagement.RegulatedEnvironmentManagementManager manager) {
        manager.landingZoneRegistrationOperations()
            .deleteWithResponse("ExampleResourceGroup", "ExampleLZA", "ExampleLZR", com.azure.core.util.Context.NONE);
    }
}
```

### LandingZoneRegistrationOperations_Update

```java
/**
 * Samples for LandingZoneRegistrationOperations Get.
 */
public final class LandingZoneRegistrationOperationsGetSampl {
    /*
     * x-ms-original-file: 2025-02-27-preview/LandingZoneRegistrationOperations_Get.json
     */
    /**
     * Sample code: LandingZoneRegistrationOperations_Get.
     * 
     * @param manager Entry point to RegulatedEnvironmentManagementManager.
     */
    public static void landingZoneRegistrationOperationsGet(
        com.azure.resourcemanager.regulatedenvironmentmanagement.RegulatedEnvironmentManagementManager manager) {
        manager.landingZoneRegistrationOperations()
            .getWithResponse("ExampleResourceGroup", "ExampleLZA", "ExampleLZR", com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/**
 * Samples for LandingZoneConfigurationOperations ListByResourceGroup.
 */
public final class LandingZoneConfigurationOperationsListByR {
    /*
     * x-ms-original-file: 2025-02-27-preview/LandingZoneConfigurationOperations_ListByResourceGroup.json
     */
    /**
     * Sample code: LandingZoneConfigurationOperations_ListByResourceGroup.
     * 
     * @param manager Entry point to RegulatedEnvironmentManagementManager.
     */
    public static void landingZoneConfigurationOperationsListByResourceGroup(
        com.azure.resourcemanager.regulatedenvironmentmanagement.RegulatedEnvironmentManagementManager manager) {
        manager.landingZoneConfigurationOperations()
            .listByResourceGroup("ExampleResourceGroup", "ExampleLZA", com.azure.core.util.Context.NONE);
    }
}
```

