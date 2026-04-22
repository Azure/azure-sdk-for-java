# Code snippets and samples


## Approval

- [CreateOrUpdate](#approval_createorupdate)
- [Delete](#approval_delete)
- [Get](#approval_get)
- [ListByParent](#approval_listbyparent)
- [NotifyInitiator](#approval_notifyinitiator)
- [Update](#approval_update)

## Community

- [CheckAddressSpaceAvailability](#community_checkaddressspaceavailability)
- [CreateOrUpdate](#community_createorupdate)
- [Delete](#community_delete)
- [GetByResourceGroup](#community_getbyresourcegroup)
- [List](#community_list)
- [ListByResourceGroup](#community_listbyresourcegroup)
- [Update](#community_update)

## CommunityEndpoints

- [CreateOrUpdate](#communityendpoints_createorupdate)
- [Delete](#communityendpoints_delete)
- [Get](#communityendpoints_get)
- [HandleApprovalCreation](#communityendpoints_handleapprovalcreation)
- [HandleApprovalDeletion](#communityendpoints_handleapprovaldeletion)
- [ListByCommunityResource](#communityendpoints_listbycommunityresource)
- [ListBySubscription](#communityendpoints_listbysubscription)
- [Update](#communityendpoints_update)

## EnclaveConnection

- [CreateOrUpdate](#enclaveconnection_createorupdate)
- [Delete](#enclaveconnection_delete)
- [GetByResourceGroup](#enclaveconnection_getbyresourcegroup)
- [HandleApprovalCreation](#enclaveconnection_handleapprovalcreation)
- [HandleApprovalDeletion](#enclaveconnection_handleapprovaldeletion)
- [List](#enclaveconnection_list)
- [ListByResourceGroup](#enclaveconnection_listbyresourcegroup)
- [Update](#enclaveconnection_update)

## EnclaveEndpoints

- [CreateOrUpdate](#enclaveendpoints_createorupdate)
- [Delete](#enclaveendpoints_delete)
- [Get](#enclaveendpoints_get)
- [HandleApprovalCreation](#enclaveendpoints_handleapprovalcreation)
- [HandleApprovalDeletion](#enclaveendpoints_handleapprovaldeletion)
- [ListByEnclaveResource](#enclaveendpoints_listbyenclaveresource)
- [ListBySubscription](#enclaveendpoints_listbysubscription)
- [Update](#enclaveendpoints_update)

## Operations

- [List](#operations_list)

## TransitHub

- [CreateOrUpdate](#transithub_createorupdate)
- [Delete](#transithub_delete)
- [Get](#transithub_get)
- [ListByCommunityResource](#transithub_listbycommunityresource)
- [ListBySubscription](#transithub_listbysubscription)
- [Update](#transithub_update)

## VirtualEnclave

- [CreateOrUpdate](#virtualenclave_createorupdate)
- [Delete](#virtualenclave_delete)
- [GetByResourceGroup](#virtualenclave_getbyresourcegroup)
- [HandleApprovalCreation](#virtualenclave_handleapprovalcreation)
- [HandleApprovalDeletion](#virtualenclave_handleapprovaldeletion)
- [List](#virtualenclave_list)
- [ListByResourceGroup](#virtualenclave_listbyresourcegroup)
- [Update](#virtualenclave_update)

## Workload

- [CreateOrUpdate](#workload_createorupdate)
- [Delete](#workload_delete)
- [Get](#workload_get)
- [ListByEnclaveResource](#workload_listbyenclaveresource)
- [ListBySubscription](#workload_listbysubscription)
- [Update](#workload_update)
### Approval_CreateOrUpdate

```java
import com.azure.resourcemanager.virtualenclaves.models.ActionPerformed;
import com.azure.resourcemanager.virtualenclaves.models.ApprovalProperties;
import com.azure.resourcemanager.virtualenclaves.models.ApprovalStatus;
import com.azure.resourcemanager.virtualenclaves.models.Approver;
import com.azure.resourcemanager.virtualenclaves.models.RequestMetadata;
import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * Samples for Approval CreateOrUpdate.
 */
public final class ApprovalCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Approvals_CreateOrUpdate.json
     */
    /**
     * Sample code: Approval_CreateOrUpdate.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void
        approvalCreateOrUpdate(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.approvals()
            .define("TestApprovals")
            .withExistingResourceUri(
                "subscriptions/c64f6eca-bdc5-4bc2-88d6-f8f1dc23f86c/resourceGroups/TestMyRg/providers/Microsoft.Mission/enclaveconnections/TestMyEnclaveConnection")
            .withProperties(new ApprovalProperties().withParentResourceId(
                "/subscriptions/c64f6eca-bdc5-4bc2-88d6-f8f1dc23f86c/resourceGroups/TestMyRg/providers/microsoft.mission/virtualenclaves/TestMyEnclave")
                .withGrandparentResourceId(
                    "/subscriptions/c64f6eca-bdc5-4bc2-88d6-f8f1dc23f86c/resourceGroups/testrg/providers/Microsoft.Mission/communities/TestMyCommunity")
                .withApprovers(Arrays.asList(new Approver().withApproverEntraId("00000000-0000-0000-0000-000000000000")
                    .withActionPerformed(ActionPerformed.APPROVED)
                    .withLastUpdatedAt(OffsetDateTime.parse("2023-03-17T20:43:17.760Z"))))
                .withTicketId("string")
                .withCreatedAt(OffsetDateTime.parse("2023-03-17T20:43:17.760Z"))
                .withStateChangedAt(OffsetDateTime.parse("2023-03-17T20:43:17.760Z"))
                .withRequestMetadata(new RequestMetadata().withResourceAction("string")
                    .withApprovalCallbackRoute("approvalCallback")
                    .withApprovalCallbackPayload("{\n  \"key1\": \"value1\",\n  \"key2\": \"value2\"\n}")
                    .withApprovalStatus(ApprovalStatus.APPROVED)))
            .create();
    }
}
```

### Approval_Delete

```java
/**
 * Samples for Approval Delete.
 */
public final class ApprovalDeleteSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Approvals_Delete.json
     */
    /**
     * Sample code: Approval_Delete.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void approvalDelete(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.approvals()
            .delete(
                "subscriptions/c64f6eca-bdc5-4bc2-88d6-f8f1dc23f86c/resourceGroups/TestMyRg/providers/Microsoft.Mission/enclaveconnections/TestMyEnclaveConnection",
                "TestApprovals", com.azure.core.util.Context.NONE);
    }
}
```

### Approval_Get

```java
/**
 * Samples for Approval Get.
 */
public final class ApprovalGetSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Approvals_Get.json
     */
    /**
     * Sample code: Approval_Get.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void approvalGet(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.approvals()
            .getWithResponse(
                "subscriptions/c64f6eca-bdc5-4bc2-88d6-f8f1dc23f86c/resourceGroups/TestMyRg/providers/Microsoft.Mission/enclaveconnections/TestMyEnclaveConnection",
                "TestApprovals", com.azure.core.util.Context.NONE);
    }
}
```

### Approval_ListByParent

```java
/**
 * Samples for Approval ListByParent.
 */
public final class ApprovalListByParentSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Approvals_ListByParent.json
     */
    /**
     * Sample code: Approval_ListByParent.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void approvalListByParent(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.approvals()
            .listByParent(
                "subscriptions/c64f6eca-bdc5-4bc2-88d6-f8f1dc23f86c/resourceGroups/TestMyRg/providers/Microsoft.Mission/enclaveconnections/TestMyEnclaveConnection",
                com.azure.core.util.Context.NONE);
    }
}
```

### Approval_NotifyInitiator

```java
import com.azure.resourcemanager.virtualenclaves.models.ApprovalActionRequest;
import com.azure.resourcemanager.virtualenclaves.models.ApprovalActionRequestApprovalStatus;

/**
 * Samples for Approval NotifyInitiator.
 */
public final class ApprovalNotifyInitiatorSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Approvals_NotifyInitiator.json
     */
    /**
     * Sample code: Approval_NotifyInitiator.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void
        approvalNotifyInitiator(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.approvals()
            .notifyInitiator(
                "subscriptions/c64f6eca-bdc5-4bc2-88d6-f8f1dc23f86c/resourceGroups/TestMyRg/providers/Microsoft.Mission/enclaveconnections/TestMyEnclaveConnection",
                "TestApprovals",
                new ApprovalActionRequest().withApprovalStatus(ApprovalActionRequestApprovalStatus.APPROVED),
                com.azure.core.util.Context.NONE);
    }
}
```

### Approval_Update

```java
import com.azure.resourcemanager.virtualenclaves.models.ActionPerformed;
import com.azure.resourcemanager.virtualenclaves.models.ApprovalPatchProperties;
import com.azure.resourcemanager.virtualenclaves.models.ApprovalResource;
import com.azure.resourcemanager.virtualenclaves.models.ApprovalStatus;
import com.azure.resourcemanager.virtualenclaves.models.Approver;
import com.azure.resourcemanager.virtualenclaves.models.RequestMetadataUpdatableProperties;
import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * Samples for Approval Update.
 */
public final class ApprovalUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Approvals_Update.json
     */
    /**
     * Sample code: Approval_Update.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void approvalUpdate(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        ApprovalResource resource = manager.approvals()
            .getWithResponse(
                "subscriptions/c64f6eca-bdc5-4bc2-88d6-f8f1dc23f86c/resourceGroups/TestMyRg/providers/Microsoft.Mission/enclaveconnections/TestMyEnclaveConnection",
                "TestApprovals", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new ApprovalPatchProperties().withParentResourceId(
                "/subscriptions/c64f6eca-bdc5-4bc2-88d6-f8f1dc23f86c/resourceGroups/TestMyRg/providers/microsoft.mission/virtualenclaves/TestMyEnclave")
                .withGrandparentResourceId(
                    "/subscriptions/c64f6eca-bdc5-4bc2-88d6-f8f1dc23f86c/resourceGroups/testrg/providers/Microsoft.Mission/communities/TestMyCommunity")
                .withApprovers(Arrays.asList(new Approver().withApproverEntraId("00000000-0000-0000-0000-000000000000")
                    .withActionPerformed(ActionPerformed.APPROVED)
                    .withLastUpdatedAt(OffsetDateTime.parse("2023-03-17T20:43:17.760Z"))))
                .withTicketId("string")
                .withCreatedAt(OffsetDateTime.parse("2023-03-17T20:43:17.760Z"))
                .withStateChangedAt(OffsetDateTime.parse("2023-03-17T20:43:17.760Z"))
                .withRequestMetadata(new RequestMetadataUpdatableProperties().withResourceAction("string")
                    .withApprovalCallbackRoute("approvalCallback")
                    .withApprovalCallbackPayload("{\n  \"key1\": \"value1\",\n  \"key2\": \"value2\"\n}")
                    .withApprovalStatus(ApprovalStatus.APPROVED)))
            .apply();
    }
}
```

### Community_CheckAddressSpaceAvailability

```java
import com.azure.resourcemanager.virtualenclaves.models.CheckAddressSpaceAvailabilityRequest;
import com.azure.resourcemanager.virtualenclaves.models.EnclaveVirtualNetworkModel;
import com.azure.resourcemanager.virtualenclaves.models.SubnetConfiguration;
import java.util.Arrays;

/**
 * Samples for Community CheckAddressSpaceAvailability.
 */
public final class CommunityCheckAddressSpaceAvailabilitySamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Community_PostCheckAddressSpaceAvailability.json
     */
    /**
     * Sample code: Community_CheckAddressSpaceAvailability.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void communityCheckAddressSpaceAvailability(
        com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.communities()
            .checkAddressSpaceAvailabilityWithResponse("rgopenapi", "TestMyCommunity",
                new CheckAddressSpaceAvailabilityRequest().withCommunityResourceId(
                    "/subscriptions/c64f6eca-bdc5-4bc2-88d6-f8f1dc23f86c/resourceGroups/TestMyRg/providers/Microsoft.Mission/communities/TestMyCommunity")
                    .withEnclaveVirtualNetwork(new EnclaveVirtualNetworkModel().withNetworkSize("small")
                        .withCustomCidrRange("10.0.0.0/24")
                        .withSubnetConfigurations(
                            Arrays.asList(new SubnetConfiguration().withSubnetName("test").withNetworkPrefixSize(26)))
                        .withAllowSubnetCommunication(true)),
                com.azure.core.util.Context.NONE);
    }
}
```

### Community_CreateOrUpdate

```java
import com.azure.resourcemanager.virtualenclaves.models.ApprovalPolicy;
import com.azure.resourcemanager.virtualenclaves.models.ApprovalSettings;
import com.azure.resourcemanager.virtualenclaves.models.CommunityProperties;
import com.azure.resourcemanager.virtualenclaves.models.FirewallSKU;
import com.azure.resourcemanager.virtualenclaves.models.GovernedServiceItem;
import com.azure.resourcemanager.virtualenclaves.models.GovernedServiceItemEnforcement;
import com.azure.resourcemanager.virtualenclaves.models.GovernedServiceItemOption;
import com.azure.resourcemanager.virtualenclaves.models.GovernedServiceItemPolicyAction;
import com.azure.resourcemanager.virtualenclaves.models.MaintenanceModeConfigurationModel;
import com.azure.resourcemanager.virtualenclaves.models.MaintenanceModeConfigurationModelJustification;
import com.azure.resourcemanager.virtualenclaves.models.MaintenanceModeConfigurationModelMode;
import com.azure.resourcemanager.virtualenclaves.models.ManagedServiceIdentity;
import com.azure.resourcemanager.virtualenclaves.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.virtualenclaves.models.MandatoryApprover;
import com.azure.resourcemanager.virtualenclaves.models.Principal;
import com.azure.resourcemanager.virtualenclaves.models.PrincipalType;
import com.azure.resourcemanager.virtualenclaves.models.RoleAssignmentItem;
import com.azure.resourcemanager.virtualenclaves.models.ServiceIdentifier;
import com.azure.resourcemanager.virtualenclaves.models.UserAssignedIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Community CreateOrUpdate.
 */
public final class CommunityCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Community_CreateOrUpdate.json
     */
    /**
     * Sample code: Community_CreateOrUpdate.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void
        communityCreateOrUpdate(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.communities()
            .define("TestMyCommunity")
            .withRegion("westcentralus")
            .withExistingResourceGroup("rgopenapi")
            .withTags(mapOf("sampletag", "samplevalue"))
            .withProperties(new CommunityProperties().withAddressSpace("10.0.0.0/24")
                .withDnsServers(Arrays.asList("azure.net"))
                .withGovernedServiceList(Arrays.asList(
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.AKS)
                        .withOption(GovernedServiceItemOption.ALLOW)
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE),
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.APP_SERVICE)
                        .withOption(GovernedServiceItemOption.ALLOW)
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE),
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.CONTAINER_REGISTRY)
                        .withOption(GovernedServiceItemOption.ALLOW)
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE),
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.COSMOS_DB)
                        .withOption(GovernedServiceItemOption.ALLOW)
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE),
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.KEY_VAULT)
                        .withOption(GovernedServiceItemOption.ALLOW)
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE),
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.MICROSOFT_SQL)
                        .withOption(GovernedServiceItemOption.ALLOW)
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE),
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.MONITORING)
                        .withOption(GovernedServiceItemOption.fromString("Not Applicable"))
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE),
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.POSTGRE_SQL)
                        .withOption(GovernedServiceItemOption.ALLOW)
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE),
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.SERVICE_BUS)
                        .withOption(GovernedServiceItemOption.ALLOW)
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE),
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.STORAGE)
                        .withOption(GovernedServiceItemOption.ALLOW)
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE),
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.AZURE_FIREWALLS)
                        .withOption(GovernedServiceItemOption.ALLOW)
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE),
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.INSIGHTS)
                        .withOption(GovernedServiceItemOption.ALLOW)
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE),
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.LOGIC)
                        .withOption(GovernedServiceItemOption.ALLOW)
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE),
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.PRIVATE_DNSZONES)
                        .withOption(GovernedServiceItemOption.ALLOW)
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE),
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.DATA_CONNECTORS)
                        .withOption(GovernedServiceItemOption.ALLOW)
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE)))
                .withCommunityRoleAssignments(Arrays.asList(new RoleAssignmentItem()
                    .withRoleDefinitionId("b24988ac-6180-42a0-ab88-20f7382dd24c")
                    .withPrincipals(Arrays.asList(
                        new Principal().withId("01234567-89ab-ef01-2345-0123456789ab").withType(PrincipalType.GROUP),
                        new Principal().withId("355a6bb0-abc0-4cba-000d-12a345b678c0").withType(PrincipalType.USER))),
                    new RoleAssignmentItem().withRoleDefinitionId("18d7d88d-d35e-4fb5-a5c3-7773c20a72d9")
                        .withPrincipals(Arrays.asList(new Principal().withId("355a6bb0-abc0-4cba-000d-12a345b678c9")
                            .withType(PrincipalType.USER)))))
                .withFirewallSku(FirewallSKU.STANDARD)
                .withApprovalSettings(new ApprovalSettings().withEndpointCreation(ApprovalPolicy.NOT_REQUIRED)
                    .withEndpointUpdate(ApprovalPolicy.REQUIRED)
                    .withEndpointDeletion(ApprovalPolicy.NOT_REQUIRED)
                    .withConnectionCreation(ApprovalPolicy.REQUIRED)
                    .withConnectionUpdate(ApprovalPolicy.REQUIRED)
                    .withConnectionDeletion(ApprovalPolicy.NOT_REQUIRED)
                    .withEnclaveCreation(ApprovalPolicy.NOT_REQUIRED)
                    .withEnclaveDeletion(ApprovalPolicy.NOT_REQUIRED)
                    .withMaintenanceMode(ApprovalPolicy.NOT_REQUIRED)
                    .withServiceCatalogDeployment(ApprovalPolicy.NOT_REQUIRED)
                    .withNotificationOnApprovalCreation(ApprovalPolicy.NOT_REQUIRED)
                    .withNotificationOnApprovalAction(ApprovalPolicy.NOT_REQUIRED)
                    .withNotificationOnApprovalDeletion(ApprovalPolicy.NOT_REQUIRED)
                    .withMandatoryApprovers(Arrays
                        .asList(new MandatoryApprover().withApproverEntraId("00000000-0000-0000-0000-000000000000")))
                    .withMinimumApproversRequired(0L))
                .withMaintenanceModeConfiguration(new MaintenanceModeConfigurationModel()
                    .withMode(MaintenanceModeConfigurationModelMode.OFF)
                    .withPrincipals(Arrays.asList(
                        new Principal().withId("355a6bb0-abc0-4cba-000d-12a345b678c9").withType(PrincipalType.USER)))
                    .withJustification(MaintenanceModeConfigurationModelJustification.OFF)))
            .withIdentity(new ManagedServiceIdentity()
                .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/subid/resourceGroups/default-azurebatch-japaneast/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1",
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

### Community_Delete

```java
/**
 * Samples for Community Delete.
 */
public final class CommunityDeleteSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Community_Delete.json
     */
    /**
     * Sample code: Community_Delete.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void communityDelete(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.communities().delete("rgopenapi", "TestMyCommunity", com.azure.core.util.Context.NONE);
    }
}
```

### Community_GetByResourceGroup

```java
/**
 * Samples for Community GetByResourceGroup.
 */
public final class CommunityGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Community_Get.json
     */
    /**
     * Sample code: Community_Get.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void communityGet(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.communities()
            .getByResourceGroupWithResponse("rgopenapi", "TestMyCommunity", com.azure.core.util.Context.NONE);
    }
}
```

### Community_List

```java
/**
 * Samples for Community List.
 */
public final class CommunityListSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Community_ListBySubscription.json
     */
    /**
     * Sample code: Community_ListBySubscription.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void
        communityListBySubscription(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.communities().list(com.azure.core.util.Context.NONE);
    }
}
```

### Community_ListByResourceGroup

```java
/**
 * Samples for Community ListByResourceGroup.
 */
public final class CommunityListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Community_ListByResourceGroup.json
     */
    /**
     * Sample code: Community_ListByResourceGroup.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void
        communityListByResourceGroup(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.communities().listByResourceGroup("rgopenapi", com.azure.core.util.Context.NONE);
    }
}
```

### Community_Update

```java
import com.azure.resourcemanager.virtualenclaves.models.CommunityPatchProperties;
import com.azure.resourcemanager.virtualenclaves.models.CommunityResource;
import com.azure.resourcemanager.virtualenclaves.models.GovernedServiceItem;
import com.azure.resourcemanager.virtualenclaves.models.GovernedServiceItemEnforcement;
import com.azure.resourcemanager.virtualenclaves.models.GovernedServiceItemOption;
import com.azure.resourcemanager.virtualenclaves.models.GovernedServiceItemPolicyAction;
import com.azure.resourcemanager.virtualenclaves.models.Principal;
import com.azure.resourcemanager.virtualenclaves.models.PrincipalType;
import com.azure.resourcemanager.virtualenclaves.models.RoleAssignmentItem;
import com.azure.resourcemanager.virtualenclaves.models.ServiceIdentifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Community Update.
 */
public final class CommunityUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Community_Update.json
     */
    /**
     * Sample code: Community_Update.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void communityUpdate(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        CommunityResource resource = manager.communities()
            .getByResourceGroupWithResponse("rgopenapi", "TestMyCommunity", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("sampletag", "samplevalue"))
            .withProperties(new CommunityPatchProperties().withDnsServers(Arrays.asList("azure.net"))
                .withGovernedServiceList(Arrays.asList(
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.AKS)
                        .withOption(GovernedServiceItemOption.ALLOW)
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE),
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.APP_SERVICE)
                        .withOption(GovernedServiceItemOption.ALLOW)
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE),
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.CONTAINER_REGISTRY)
                        .withOption(GovernedServiceItemOption.ALLOW)
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE),
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.COSMOS_DB)
                        .withOption(GovernedServiceItemOption.ALLOW)
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE),
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.KEY_VAULT)
                        .withOption(GovernedServiceItemOption.ALLOW)
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE),
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.MICROSOFT_SQL)
                        .withOption(GovernedServiceItemOption.ALLOW)
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE),
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.MONITORING)
                        .withOption(GovernedServiceItemOption.fromString("Not Applicable"))
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE),
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.POSTGRE_SQL)
                        .withOption(GovernedServiceItemOption.ALLOW)
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE),
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.SERVICE_BUS)
                        .withOption(GovernedServiceItemOption.ALLOW)
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE),
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.STORAGE)
                        .withOption(GovernedServiceItemOption.ALLOW)
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE),
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.AZURE_FIREWALLS)
                        .withOption(GovernedServiceItemOption.ALLOW)
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE),
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.INSIGHTS)
                        .withOption(GovernedServiceItemOption.ALLOW)
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE),
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.LOGIC)
                        .withOption(GovernedServiceItemOption.ALLOW)
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE),
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.PRIVATE_DNSZONES)
                        .withOption(GovernedServiceItemOption.ALLOW)
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE),
                    new GovernedServiceItem().withServiceId(ServiceIdentifier.DATA_CONNECTORS)
                        .withOption(GovernedServiceItemOption.ALLOW)
                        .withEnforcement(GovernedServiceItemEnforcement.ENABLED)
                        .withPolicyAction(GovernedServiceItemPolicyAction.NONE)))
                .withCommunityRoleAssignments(Arrays.asList(new RoleAssignmentItem()
                    .withRoleDefinitionId("b24988ac-6180-42a0-ab88-20f7382dd24c")
                    .withPrincipals(Arrays.asList(
                        new Principal().withId("01234567-89ab-ef01-2345-0123456789ab").withType(PrincipalType.GROUP),
                        new Principal().withId("355a6bb0-abc0-4cba-000d-12a345b678c0").withType(PrincipalType.USER))),
                    new RoleAssignmentItem().withRoleDefinitionId("18d7d88d-d35e-4fb5-a5c3-7773c20a72d9")
                        .withPrincipals(Arrays.asList(new Principal().withId("355a6bb0-abc0-4cba-000d-12a345b678c9")
                            .withType(PrincipalType.USER))))))
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

### CommunityEndpoints_CreateOrUpdate

```java
import com.azure.resourcemanager.virtualenclaves.models.CommunityEndpointDestinationRule;
import com.azure.resourcemanager.virtualenclaves.models.CommunityEndpointProperties;
import com.azure.resourcemanager.virtualenclaves.models.CommunityEndpointProtocol;
import com.azure.resourcemanager.virtualenclaves.models.DestinationType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for CommunityEndpoints CreateOrUpdate.
 */
public final class CommunityEndpointsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/CommunityEndpoints_CreateOrUpdate.json
     */
    /**
     * Sample code: CommunityEndpoints_CreateOrUpdate.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void
        communityEndpointsCreateOrUpdate(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.communityEndpoints()
            .define("TestMyCommunityEndpoint")
            .withRegion("West US")
            .withExistingCommunity("rgopenapi", "TestMyCommunity")
            .withTags(mapOf("sampletag", "samplevalue"))
            .withProperties(new CommunityEndpointProperties().withRuleCollection(
                Arrays.asList(new CommunityEndpointDestinationRule().withDestinationType(DestinationType.FQDNTAG)
                    .withProtocols(Arrays.asList(CommunityEndpointProtocol.TCP))
                    .withTransitHubResourceId(
                        "/subscriptions/c64f6eca-bdc5-4bc2-88d6-f8f1dc23f86c/resourceGroups/testrg/providers/Microsoft.Mission/communities/TestMyCommunity/transitHubs/TestThName")
                    .withDestination("foo.example.com")
                    .withPorts("443"))))
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

### CommunityEndpoints_Delete

```java
/**
 * Samples for CommunityEndpoints Delete.
 */
public final class CommunityEndpointsDeleteSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/CommunityEndpoints_Delete.json
     */
    /**
     * Sample code: CommunityEndpoints_Delete.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void
        communityEndpointsDelete(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.communityEndpoints()
            .delete("rgopenapi", "TestMyCommunity", "TestMyCommunityEndpoint", com.azure.core.util.Context.NONE);
    }
}
```

### CommunityEndpoints_Get

```java
/**
 * Samples for CommunityEndpoints Get.
 */
public final class CommunityEndpointsGetSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/CommunityEndpoints_Get.json
     */
    /**
     * Sample code: CommunityEndpoints_Get.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void communityEndpointsGet(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.communityEndpoints()
            .getWithResponse("rgopenapi", "TestMyCommunity", "TestMyCommunityEndpoint",
                com.azure.core.util.Context.NONE);
    }
}
```

### CommunityEndpoints_HandleApprovalCreation

```java
import com.azure.resourcemanager.virtualenclaves.models.ApprovalCallbackRequest;
import com.azure.resourcemanager.virtualenclaves.models.ApprovalCallbackRequestApprovalStatus;
import com.azure.resourcemanager.virtualenclaves.models.ApprovalCallbackRequestResourceRequestAction;

/**
 * Samples for CommunityEndpoints HandleApprovalCreation.
 */
public final class CommunityEndpointsHandleApprovalCreationSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/CommunityEndpoints_HandleApprovalCreation.json
     */
    /**
     * Sample code: CommunityEndpoints_HandleApprovalCreation.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void communityEndpointsHandleApprovalCreation(
        com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.communityEndpoints()
            .handleApprovalCreation("rgopenapi", "TestMyCommunity", "TestMyCommunityEndpoint",
                new ApprovalCallbackRequest()
                    .withResourceRequestAction(ApprovalCallbackRequestResourceRequestAction.CREATE)
                    .withApprovalStatus(ApprovalCallbackRequestApprovalStatus.APPROVED),
                com.azure.core.util.Context.NONE);
    }
}
```

### CommunityEndpoints_HandleApprovalDeletion

```java
import com.azure.resourcemanager.virtualenclaves.models.ApprovalDeletionCallbackRequest;
import com.azure.resourcemanager.virtualenclaves.models.ApprovalDeletionCallbackRequestResourceRequestAction;

/**
 * Samples for CommunityEndpoints HandleApprovalDeletion.
 */
public final class CommunityEndpointsHandleApprovalDeletionSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/CommunityEndpoints_HandleApprovalDeletion.json
     */
    /**
     * Sample code: CommunityEndpoints_HandleApprovalDeletion.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void communityEndpointsHandleApprovalDeletion(
        com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.communityEndpoints()
            .handleApprovalDeletion(
                "rgopenapi", "TestMyCommunity", "TestMyCommunityEndpoint", new ApprovalDeletionCallbackRequest()
                    .withResourceRequestAction(ApprovalDeletionCallbackRequestResourceRequestAction.CREATE),
                com.azure.core.util.Context.NONE);
    }
}
```

### CommunityEndpoints_ListByCommunityResource

```java
/**
 * Samples for CommunityEndpoints ListByCommunityResource.
 */
public final class CommunityEndpointsListByCommunityResourceSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/CommunityEndpoints_ListByCommunityResource.json
     */
    /**
     * Sample code: CommunityEndpoints_ListByCommunityResource.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void communityEndpointsListByCommunityResource(
        com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.communityEndpoints()
            .listByCommunityResource("rgopenapi", "TestMyCommunity", com.azure.core.util.Context.NONE);
    }
}
```

### CommunityEndpoints_ListBySubscription

```java
/**
 * Samples for CommunityEndpoints ListBySubscription.
 */
public final class CommunityEndpointsListBySubscriptionSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/CommunityEndpoints_ListBySubscription.json
     */
    /**
     * Sample code: CommunityEndpoints_ListBySubscription.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void
        communityEndpointsListBySubscription(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.communityEndpoints().listBySubscription("TestMyCommunity", com.azure.core.util.Context.NONE);
    }
}
```

### CommunityEndpoints_Update

```java
import com.azure.resourcemanager.virtualenclaves.models.CommunityEndpointDestinationRule;
import com.azure.resourcemanager.virtualenclaves.models.CommunityEndpointPatchProperties;
import com.azure.resourcemanager.virtualenclaves.models.CommunityEndpointProtocol;
import com.azure.resourcemanager.virtualenclaves.models.CommunityEndpointResource;
import com.azure.resourcemanager.virtualenclaves.models.DestinationType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for CommunityEndpoints Update.
 */
public final class CommunityEndpointsUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/CommunityEndpoints_Update.json
     */
    /**
     * Sample code: CommunityEndpoints_Update.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void
        communityEndpointsUpdate(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        CommunityEndpointResource resource = manager.communityEndpoints()
            .getWithResponse("rgopenapi", "TestMyCommunity", "TestMyCommunityEndpoint",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("sampletag", "samplevalue"))
            .withProperties(new CommunityEndpointPatchProperties().withRuleCollection(
                Arrays.asList(new CommunityEndpointDestinationRule().withDestinationType(DestinationType.FQDN)
                    .withProtocols(Arrays.asList(CommunityEndpointProtocol.TCP))
                    .withTransitHubResourceId(
                        "/subscriptions/c64f6eca-bdc5-4bc2-88d6-f8f1dc23f86c/resourceGroups/testrg/providers/Microsoft.Mission/communities/TestMyCommunity/transitHubs/TestThName")
                    .withDestination("foo.example.com")
                    .withPorts("443"))))
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

### EnclaveConnection_CreateOrUpdate

```java
import com.azure.resourcemanager.virtualenclaves.models.EnclaveConnectionProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for EnclaveConnection CreateOrUpdate.
 */
public final class EnclaveConnectionCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/EnclaveConnection_CreateOrUpdate.json
     */
    /**
     * Sample code: EnclaveConnection_CreateOrUpdate.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void
        enclaveConnectionCreateOrUpdate(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.enclaveConnections()
            .define("TestMyEnclaveConnection")
            .withRegion("West US")
            .withExistingResourceGroup("rgopenapi")
            .withTags(mapOf("sampletag", "samplevalue"))
            .withProperties(new EnclaveConnectionProperties().withCommunityResourceId(
                "/subscriptions/c64f6eca-bdc5-4bc2-88d6-f8f1dc23f86c/resourceGroups/testrg/providers/Microsoft.Mission/communities/TestMyCommunity")
                .withSourceResourceId(
                    "/subscriptions/c64f6eca-bdc5-4bc2-88d6-f8f1dc23f86c/resourceGroups/TestMyRg/providers/microsoft.mission/virtualenclaves/TestMyEnclave")
                .withSourceCidr("10.0.0.0/24")
                .withDestinationEndpointId(
                    "/subscriptions/c64f6eca-bdc5-4bc2-88d6-f8f1dc23f86c/resourceGroups/TestMyRg/providers/Microsoft.Mission/virtualenclaves/TestMyEnclave/enclaveendpoints/TestMyEnclaveEndpoint"))
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

### EnclaveConnection_Delete

```java
/**
 * Samples for EnclaveConnection Delete.
 */
public final class EnclaveConnectionDeleteSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/EnclaveConnection_Delete.json
     */
    /**
     * Sample code: EnclaveConnection_Delete.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void
        enclaveConnectionDelete(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.enclaveConnections().delete("rgopenapi", "TestMyEnclaveConnection", com.azure.core.util.Context.NONE);
    }
}
```

### EnclaveConnection_GetByResourceGroup

```java
/**
 * Samples for EnclaveConnection GetByResourceGroup.
 */
public final class EnclaveConnectionGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/EnclaveConnection_Get.json
     */
    /**
     * Sample code: EnclaveConnection_Get.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void enclaveConnectionGet(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.enclaveConnections()
            .getByResourceGroupWithResponse("rgopenapi", "TestMyEnclaveConnection", com.azure.core.util.Context.NONE);
    }
}
```

### EnclaveConnection_HandleApprovalCreation

```java
import com.azure.resourcemanager.virtualenclaves.models.ApprovalCallbackRequest;
import com.azure.resourcemanager.virtualenclaves.models.ApprovalCallbackRequestApprovalStatus;
import com.azure.resourcemanager.virtualenclaves.models.ApprovalCallbackRequestResourceRequestAction;

/**
 * Samples for EnclaveConnection HandleApprovalCreation.
 */
public final class EnclaveConnectionHandleApprovalCreationSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/EnclaveConnection_HandleApprovalCreation.json
     */
    /**
     * Sample code: EnclaveConnection_HandleApprovalCreation.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void enclaveConnectionHandleApprovalCreation(
        com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.enclaveConnections()
            .handleApprovalCreation("rgopenapi", "TestMyEnclaveConnection",
                new ApprovalCallbackRequest()
                    .withResourceRequestAction(ApprovalCallbackRequestResourceRequestAction.CREATE)
                    .withApprovalStatus(ApprovalCallbackRequestApprovalStatus.APPROVED)
                    .withApprovalCallbackPayload("{\n  \"key1\": \"value1\",\n  \"key2\": \"value2\"\n}"),
                com.azure.core.util.Context.NONE);
    }
}
```

### EnclaveConnection_HandleApprovalDeletion

```java
import com.azure.resourcemanager.virtualenclaves.models.ApprovalDeletionCallbackRequest;
import com.azure.resourcemanager.virtualenclaves.models.ApprovalDeletionCallbackRequestResourceRequestAction;

/**
 * Samples for EnclaveConnection HandleApprovalDeletion.
 */
public final class EnclaveConnectionHandleApprovalDeletionSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/EnclaveConnection_HandleApprovalDeletion.json
     */
    /**
     * Sample code: EnclaveConnection_HandleApprovalDeletion.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void enclaveConnectionHandleApprovalDeletion(
        com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.enclaveConnections()
            .handleApprovalDeletion("rgopenapi", "TestMyEnclaveConnection", new ApprovalDeletionCallbackRequest()
                .withResourceRequestAction(ApprovalDeletionCallbackRequestResourceRequestAction.CREATE),
                com.azure.core.util.Context.NONE);
    }
}
```

### EnclaveConnection_List

```java
/**
 * Samples for EnclaveConnection List.
 */
public final class EnclaveConnectionListSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/EnclaveConnection_ListBySubscription.json
     */
    /**
     * Sample code: EnclaveConnection_ListBySubscription.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void
        enclaveConnectionListBySubscription(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.enclaveConnections().list(com.azure.core.util.Context.NONE);
    }
}
```

### EnclaveConnection_ListByResourceGroup

```java
/**
 * Samples for EnclaveConnection ListByResourceGroup.
 */
public final class EnclaveConnectionListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/EnclaveConnection_ListByResourceGroup.json
     */
    /**
     * Sample code: EnclaveConnection_ListByResourceGroup.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void
        enclaveConnectionListByResourceGroup(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.enclaveConnections().listByResourceGroup("rgopenapi", com.azure.core.util.Context.NONE);
    }
}
```

### EnclaveConnection_Update

```java
import com.azure.resourcemanager.virtualenclaves.models.EnclaveConnectionPatchProperties;
import com.azure.resourcemanager.virtualenclaves.models.EnclaveConnectionResource;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for EnclaveConnection Update.
 */
public final class EnclaveConnectionUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/EnclaveConnection_Update.json
     */
    /**
     * Sample code: EnclaveConnection_Update.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void
        enclaveConnectionUpdate(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        EnclaveConnectionResource resource = manager.enclaveConnections()
            .getByResourceGroupWithResponse("rgopenapi", "TestMyEnclaveConnection", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("sampletag", "samplevalue"))
            .withProperties(new EnclaveConnectionPatchProperties().withSourceCidr("10.0.0.0/24"))
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

### EnclaveEndpoints_CreateOrUpdate

```java
import com.azure.resourcemanager.virtualenclaves.models.EnclaveEndpointDestinationRule;
import com.azure.resourcemanager.virtualenclaves.models.EnclaveEndpointProperties;
import com.azure.resourcemanager.virtualenclaves.models.EnclaveEndpointProtocol;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for EnclaveEndpoints CreateOrUpdate.
 */
public final class EnclaveEndpointsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/EnclaveEndpoints_CreateOrUpdate.json
     */
    /**
     * Sample code: EnclaveEndpoints_CreateOrUpdate.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void
        enclaveEndpointsCreateOrUpdate(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.enclaveEndpoints()
            .define("TestMyEnclaveEndpoint")
            .withRegion("West US")
            .withExistingVirtualEnclave("rgopenapi", "TestMyEnclave")
            .withTags(mapOf("sampletag", "samplevalue"))
            .withProperties(new EnclaveEndpointProperties().withRuleCollection(Arrays
                .asList(new EnclaveEndpointDestinationRule().withProtocols(Arrays.asList(EnclaveEndpointProtocol.TCP))
                    .withEndpointRuleName("54CEECEF-2C30-488E-946F-D20F414D99BA")
                    .withDestination("10.0.0.0/24")
                    .withPorts("443"))))
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

### EnclaveEndpoints_Delete

```java
/**
 * Samples for EnclaveEndpoints Delete.
 */
public final class EnclaveEndpointsDeleteSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/EnclaveEndpoints_Delete.json
     */
    /**
     * Sample code: EnclaveEndpoints_Delete.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void
        enclaveEndpointsDelete(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.enclaveEndpoints()
            .delete("rgopenapi", "TestMyEnclave", "TestMyEnclaveEndpoint", com.azure.core.util.Context.NONE);
    }
}
```

### EnclaveEndpoints_Get

```java
/**
 * Samples for EnclaveEndpoints Get.
 */
public final class EnclaveEndpointsGetSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/EnclaveEndpoints_Get.json
     */
    /**
     * Sample code: EnclaveEndpoints_Get.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void enclaveEndpointsGet(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.enclaveEndpoints()
            .getWithResponse("rgopenapi", "TestMyEnclave", "TestMyEnclaveEndpoint", com.azure.core.util.Context.NONE);
    }
}
```

### EnclaveEndpoints_HandleApprovalCreation

```java
import com.azure.resourcemanager.virtualenclaves.models.ApprovalCallbackRequest;
import com.azure.resourcemanager.virtualenclaves.models.ApprovalCallbackRequestApprovalStatus;
import com.azure.resourcemanager.virtualenclaves.models.ApprovalCallbackRequestResourceRequestAction;

/**
 * Samples for EnclaveEndpoints HandleApprovalCreation.
 */
public final class EnclaveEndpointsHandleApprovalCreationSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/EnclaveEndpoints_HandleApprovalCreation.json
     */
    /**
     * Sample code: EnclaveEndpoints_HandleApprovalCreation.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void enclaveEndpointsHandleApprovalCreation(
        com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.enclaveEndpoints()
            .handleApprovalCreation("rgopenapi", "TestMyEnclave", "TestMyEnclaveEndpoint",
                new ApprovalCallbackRequest()
                    .withResourceRequestAction(ApprovalCallbackRequestResourceRequestAction.CREATE)
                    .withApprovalStatus(ApprovalCallbackRequestApprovalStatus.APPROVED),
                com.azure.core.util.Context.NONE);
    }
}
```

### EnclaveEndpoints_HandleApprovalDeletion

```java
import com.azure.resourcemanager.virtualenclaves.models.ApprovalDeletionCallbackRequest;
import com.azure.resourcemanager.virtualenclaves.models.ApprovalDeletionCallbackRequestResourceRequestAction;

/**
 * Samples for EnclaveEndpoints HandleApprovalDeletion.
 */
public final class EnclaveEndpointsHandleApprovalDeletionSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/EnclaveEndpoints_HandleApprovalDeletion.json
     */
    /**
     * Sample code: EnclaveEndpoints_HandleApprovalDeletion.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void enclaveEndpointsHandleApprovalDeletion(
        com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.enclaveEndpoints()
            .handleApprovalDeletion(
                "rgopenapi", "TestMyEnclave", "TestMyEnclaveEndpoint", new ApprovalDeletionCallbackRequest()
                    .withResourceRequestAction(ApprovalDeletionCallbackRequestResourceRequestAction.CREATE),
                com.azure.core.util.Context.NONE);
    }
}
```

### EnclaveEndpoints_ListByEnclaveResource

```java
/**
 * Samples for EnclaveEndpoints ListByEnclaveResource.
 */
public final class EnclaveEndpointsListByEnclaveResourceSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/EnclaveEndpoints_ListByEnclaveResource.json
     */
    /**
     * Sample code: EnclaveEndpoints_ListByEnclaveResource.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void enclaveEndpointsListByEnclaveResource(
        com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.enclaveEndpoints()
            .listByEnclaveResource("rgopenapi", "TestMyEnclave", com.azure.core.util.Context.NONE);
    }
}
```

### EnclaveEndpoints_ListBySubscription

```java
/**
 * Samples for EnclaveEndpoints ListBySubscription.
 */
public final class EnclaveEndpointsListBySubscriptionSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/EnclaveEndpoints_ListBySubscription.json
     */
    /**
     * Sample code: EnclaveEndpoints_ListBySubscription.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void
        enclaveEndpointsListBySubscription(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.enclaveEndpoints().listBySubscription("TestMyEnclave", com.azure.core.util.Context.NONE);
    }
}
```

### EnclaveEndpoints_Update

```java
import com.azure.resourcemanager.virtualenclaves.models.EnclaveEndpointDestinationRule;
import com.azure.resourcemanager.virtualenclaves.models.EnclaveEndpointPatchProperties;
import com.azure.resourcemanager.virtualenclaves.models.EnclaveEndpointProtocol;
import com.azure.resourcemanager.virtualenclaves.models.EnclaveEndpointResource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for EnclaveEndpoints Update.
 */
public final class EnclaveEndpointsUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/EnclaveEndpoints_Update.json
     */
    /**
     * Sample code: EnclaveEndpoints_Update.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void
        enclaveEndpointsUpdate(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        EnclaveEndpointResource resource = manager.enclaveEndpoints()
            .getWithResponse("rgopenapi", "TestMyEnclave", "TestMyEnclaveEndpoint", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("sampletag", "samplevalue"))
            .withProperties(new EnclaveEndpointPatchProperties().withRuleCollection(Arrays
                .asList(new EnclaveEndpointDestinationRule().withProtocols(Arrays.asList(EnclaveEndpointProtocol.TCP))
                    .withEndpointRuleName("54CEECEF-2C30-488E-946F-D20F414D99BA")
                    .withDestination("10.0.0.0/24")
                    .withPorts("443"))))
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

### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void operationsList(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### TransitHub_CreateOrUpdate

```java
import com.azure.resourcemanager.virtualenclaves.models.TransitHubProperties;
import com.azure.resourcemanager.virtualenclaves.models.TransitHubState;
import com.azure.resourcemanager.virtualenclaves.models.TransitOption;
import com.azure.resourcemanager.virtualenclaves.models.TransitOptionParams;
import com.azure.resourcemanager.virtualenclaves.models.TransitOptionType;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for TransitHub CreateOrUpdate.
 */
public final class TransitHubCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/TransitHub_CreateOrUpdate.json
     */
    /**
     * Sample code: TransitHub_CreateOrUpdate.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void
        transitHubCreateOrUpdate(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.transitHubs()
            .define("TestThName")
            .withRegion("westcentralus")
            .withExistingCommunity("rgopenapi", "TestMyCommunity")
            .withTags(mapOf("Tag1", "Value1"))
            .withProperties(new TransitHubProperties().withState(TransitHubState.PENDING_APPROVAL)
                .withTransitOption(new TransitOption().withType(TransitOptionType.EXPRESS_ROUTE)
                    .withParams(new TransitOptionParams().withScaleUnits(1L))))
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

### TransitHub_Delete

```java
/**
 * Samples for TransitHub Delete.
 */
public final class TransitHubDeleteSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/TransitHub_Delete.json
     */
    /**
     * Sample code: TransitHub_Delete.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void transitHubDelete(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.transitHubs().delete("rgopenapi", "TestMyCommunity", "TestThName", com.azure.core.util.Context.NONE);
    }
}
```

### TransitHub_Get

```java
/**
 * Samples for TransitHub Get.
 */
public final class TransitHubGetSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/TransitHub_Get.json
     */
    /**
     * Sample code: TransitHub_Get.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void transitHubGet(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.transitHubs()
            .getWithResponse("rgopenapi", "TestMyCommunity", "TestThName", com.azure.core.util.Context.NONE);
    }
}
```

### TransitHub_ListByCommunityResource

```java
/**
 * Samples for TransitHub ListByCommunityResource.
 */
public final class TransitHubListByCommunityResourceSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/TransitHub_ListByCommunityResource.json
     */
    /**
     * Sample code: TransitHub_ListByCommunityResource.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void
        transitHubListByCommunityResource(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.transitHubs().listByCommunityResource("rgopenapi", "TestMyCommunity", com.azure.core.util.Context.NONE);
    }
}
```

### TransitHub_ListBySubscription

```java
/**
 * Samples for TransitHub ListBySubscription.
 */
public final class TransitHubListBySubscriptionSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/TransitHub_ListBySubscription.json
     */
    /**
     * Sample code: TransitHub_ListBySubscription.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void
        transitHubListBySubscription(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.transitHubs().listBySubscription("TestMyCommunity", com.azure.core.util.Context.NONE);
    }
}
```

### TransitHub_Update

```java
import com.azure.resourcemanager.virtualenclaves.models.TransitHubPatchProperties;
import com.azure.resourcemanager.virtualenclaves.models.TransitHubResource;
import com.azure.resourcemanager.virtualenclaves.models.TransitHubState;
import com.azure.resourcemanager.virtualenclaves.models.TransitOption;
import com.azure.resourcemanager.virtualenclaves.models.TransitOptionParams;
import com.azure.resourcemanager.virtualenclaves.models.TransitOptionType;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for TransitHub Update.
 */
public final class TransitHubUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/TransitHub_Update.json
     */
    /**
     * Sample code: TransitHub_Update.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void transitHubUpdate(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        TransitHubResource resource = manager.transitHubs()
            .getWithResponse("rgopenapi", "TestMyCommunity", "TestThName", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key4278", "fakeTokenPlaceholder"))
            .withProperties(new TransitHubPatchProperties().withState(TransitHubState.PENDING_APPROVAL)
                .withTransitOption(new TransitOption().withType(TransitOptionType.EXPRESS_ROUTE)
                    .withParams(new TransitOptionParams().withScaleUnits(1L))))
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

### VirtualEnclave_CreateOrUpdate

```java
import com.azure.resourcemanager.virtualenclaves.models.DiagnosticDestination;
import com.azure.resourcemanager.virtualenclaves.models.EnclaveDefaultSettingsModel;
import com.azure.resourcemanager.virtualenclaves.models.EnclaveVirtualNetworkModel;
import com.azure.resourcemanager.virtualenclaves.models.MaintenanceModeConfigurationModel;
import com.azure.resourcemanager.virtualenclaves.models.MaintenanceModeConfigurationModelJustification;
import com.azure.resourcemanager.virtualenclaves.models.MaintenanceModeConfigurationModelMode;
import com.azure.resourcemanager.virtualenclaves.models.ManagedServiceIdentity;
import com.azure.resourcemanager.virtualenclaves.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.virtualenclaves.models.Principal;
import com.azure.resourcemanager.virtualenclaves.models.PrincipalType;
import com.azure.resourcemanager.virtualenclaves.models.RoleAssignmentItem;
import com.azure.resourcemanager.virtualenclaves.models.SubnetConfiguration;
import com.azure.resourcemanager.virtualenclaves.models.UserAssignedIdentity;
import com.azure.resourcemanager.virtualenclaves.models.VirtualEnclaveProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for VirtualEnclave CreateOrUpdate.
 */
public final class VirtualEnclaveCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/VirtualEnclave_CreateOrUpdate.json
     */
    /**
     * Sample code: VirtualEnclave_CreateOrUpdate.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void
        virtualEnclaveCreateOrUpdate(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.virtualEnclaves()
            .define("TestMyEnclave")
            .withRegion("westcentralus")
            .withExistingResourceGroup("rgopenapi")
            .withTags(mapOf("Tag1", "Value1"))
            .withProperties(new VirtualEnclaveProperties()
                .withEnclaveVirtualNetwork(new EnclaveVirtualNetworkModel().withNetworkSize("small")
                    .withCustomCidrRange("10.0.0.0/24")
                    .withSubnetConfigurations(
                        Arrays.asList(new SubnetConfiguration().withSubnetName("test").withNetworkPrefixSize(26)))
                    .withAllowSubnetCommunication(true))
                .withCommunityResourceId(
                    "/subscriptions/c64f6eca-bdc5-4bc2-88d6-f8f1dc23f86c/resourceGroups/TestMyRg/providers/microsoft.mission/communities/TestMyCommunity")
                .withBastionEnabled(true)
                .withEnclaveRoleAssignments(Arrays.asList(
                    new RoleAssignmentItem().withRoleDefinitionId("b24988ac-6180-42a0-ab88-20f7382dd24c")
                        .withPrincipals(Arrays.asList(
                            new Principal().withId("355a6bb0-abc0-4cba-000d-12a345b678c9").withType(PrincipalType.USER),
                            new Principal().withId("355a6bb0-abc0-4cba-000d-12a345b678c0")
                                .withType(PrincipalType.USER))),
                    new RoleAssignmentItem().withRoleDefinitionId("18d7d88d-d35e-4fb5-a5c3-7773c20a72d9")
                        .withPrincipals(Arrays.asList(new Principal().withId("355a6bb0-abc0-4cba-000d-12a345b678c9")
                            .withType(PrincipalType.USER)))))
                .withWorkloadRoleAssignments(Arrays.asList(
                    new RoleAssignmentItem().withRoleDefinitionId("d73bb868-a0df-4d4d-bd69-98a00b01fccb")
                        .withPrincipals(Arrays.asList(new Principal().withId("01234567-89ab-ef01-2345-0123456789ab")
                            .withType(PrincipalType.GROUP))),
                    new RoleAssignmentItem().withRoleDefinitionId("fb879df8-f326-4884-b1cf-06f3ad86be52")
                        .withPrincipals(Arrays.asList(new Principal().withId("01234567-89ab-ef01-2345-0123456789ab")
                            .withType(PrincipalType.GROUP)))))
                .withEnclaveDefaultSettings(
                    new EnclaveDefaultSettingsModel().withDiagnosticDestination(DiagnosticDestination.BOTH))
                .withMaintenanceModeConfiguration(new MaintenanceModeConfigurationModel()
                    .withMode(MaintenanceModeConfigurationModelMode.OFF)
                    .withPrincipals(Arrays.asList(
                        new Principal().withId("355a6bb0-abc0-4cba-000d-12a345b678c9").withType(PrincipalType.USER)))
                    .withJustification(MaintenanceModeConfigurationModelJustification.OFF)))
            .withIdentity(new ManagedServiceIdentity()
                .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/subid/resourceGroups/default-azurebatch-japaneast/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1",
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

### VirtualEnclave_Delete

```java
/**
 * Samples for VirtualEnclave Delete.
 */
public final class VirtualEnclaveDeleteSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/VirtualEnclave_Delete.json
     */
    /**
     * Sample code: VirtualEnclave_Delete.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void virtualEnclaveDelete(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.virtualEnclaves().delete("rgopenapi", "TestMyEnclave", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualEnclave_GetByResourceGroup

```java
/**
 * Samples for VirtualEnclave GetByResourceGroup.
 */
public final class VirtualEnclaveGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/VirtualEnclave_Get.json
     */
    /**
     * Sample code: VirtualEnclave_Get.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void virtualEnclaveGet(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.virtualEnclaves()
            .getByResourceGroupWithResponse("rgopenapi", "TestMyEnclave", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualEnclave_HandleApprovalCreation

```java
import com.azure.resourcemanager.virtualenclaves.models.ApprovalCallbackRequest;
import com.azure.resourcemanager.virtualenclaves.models.ApprovalCallbackRequestApprovalStatus;
import com.azure.resourcemanager.virtualenclaves.models.ApprovalCallbackRequestResourceRequestAction;

/**
 * Samples for VirtualEnclave HandleApprovalCreation.
 */
public final class VirtualEnclaveHandleApprovalCreationSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/VirtualEnclave_HandleApprovalCreation.json
     */
    /**
     * Sample code: VirtualEnclave_HandleApprovalCreation.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void
        virtualEnclaveHandleApprovalCreation(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.virtualEnclaves()
            .handleApprovalCreation("rgopenapi", "TestMyEnclave",
                new ApprovalCallbackRequest()
                    .withResourceRequestAction(ApprovalCallbackRequestResourceRequestAction.CREATE)
                    .withApprovalStatus(ApprovalCallbackRequestApprovalStatus.APPROVED)
                    .withApprovalCallbackPayload("{\n  \"key1\": \"value1\",\n  \"key2\": \"value2\"\n}"),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualEnclave_HandleApprovalDeletion

```java
import com.azure.resourcemanager.virtualenclaves.models.ApprovalDeletionCallbackRequest;
import com.azure.resourcemanager.virtualenclaves.models.ApprovalDeletionCallbackRequestResourceRequestAction;

/**
 * Samples for VirtualEnclave HandleApprovalDeletion.
 */
public final class VirtualEnclaveHandleApprovalDeletionSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/VirtualEnclave_HandleApprovalDeletion.json
     */
    /**
     * Sample code: VirtualEnclave_HandleApprovalDeletion.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void
        virtualEnclaveHandleApprovalDeletion(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.virtualEnclaves()
            .handleApprovalDeletion("rgopenapi", "TestMyEnclave", new ApprovalDeletionCallbackRequest()
                .withResourceRequestAction(ApprovalDeletionCallbackRequestResourceRequestAction.CREATE),
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualEnclave_List

```java
/**
 * Samples for VirtualEnclave List.
 */
public final class VirtualEnclaveListSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/VirtualEnclave_ListBySubscription.json
     */
    /**
     * Sample code: VirtualEnclave_ListBySubscription.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void
        virtualEnclaveListBySubscription(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.virtualEnclaves().list(com.azure.core.util.Context.NONE);
    }
}
```

### VirtualEnclave_ListByResourceGroup

```java
/**
 * Samples for VirtualEnclave ListByResourceGroup.
 */
public final class VirtualEnclaveListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/VirtualEnclave_ListByResourceGroup.json
     */
    /**
     * Sample code: VirtualEnclave_ListByResourceGroup.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void
        virtualEnclaveListByResourceGroup(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.virtualEnclaves().listByResourceGroup("rgopenapi", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualEnclave_Update

```java
import com.azure.resourcemanager.virtualenclaves.models.DiagnosticDestination;
import com.azure.resourcemanager.virtualenclaves.models.EnclaveDefaultSettingsPatchModel;
import com.azure.resourcemanager.virtualenclaves.models.EnclaveResource;
import com.azure.resourcemanager.virtualenclaves.models.EnclaveVirtualNetworkModel;
import com.azure.resourcemanager.virtualenclaves.models.MaintenanceModeConfigurationModelJustification;
import com.azure.resourcemanager.virtualenclaves.models.MaintenanceModeConfigurationModelMode;
import com.azure.resourcemanager.virtualenclaves.models.MaintenanceModeConfigurationPatchModel;
import com.azure.resourcemanager.virtualenclaves.models.Principal;
import com.azure.resourcemanager.virtualenclaves.models.PrincipalType;
import com.azure.resourcemanager.virtualenclaves.models.RoleAssignmentItem;
import com.azure.resourcemanager.virtualenclaves.models.SubnetConfiguration;
import com.azure.resourcemanager.virtualenclaves.models.VirtualEnclavePatchProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for VirtualEnclave Update.
 */
public final class VirtualEnclaveUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/VirtualEnclave_Update.json
     */
    /**
     * Sample code: VirtualEnclave_Update.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void virtualEnclaveUpdate(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        EnclaveResource resource = manager.virtualEnclaves()
            .getByResourceGroupWithResponse("rgopenapi", "TestMyEnclave", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("Tag1", "Value1"))
            .withProperties(new VirtualEnclavePatchProperties()
                .withEnclaveVirtualNetwork(new EnclaveVirtualNetworkModel().withNetworkSize("small")
                    .withCustomCidrRange("10.0.0.0/24")
                    .withSubnetConfigurations(
                        Arrays.asList(new SubnetConfiguration().withSubnetName("test").withNetworkPrefixSize(26)))
                    .withAllowSubnetCommunication(true))
                .withBastionEnabled(true)
                .withEnclaveRoleAssignments(Arrays.asList(
                    new RoleAssignmentItem().withRoleDefinitionId("b24988ac-6180-42a0-ab88-20f7382dd24c")
                        .withPrincipals(Arrays.asList(
                            new Principal().withId("355a6bb0-abc0-4cba-000d-12a345b678c9").withType(PrincipalType.USER),
                            new Principal().withId("355a6bb0-abc0-4cba-000d-12a345b678c0")
                                .withType(PrincipalType.USER))),
                    new RoleAssignmentItem().withRoleDefinitionId("18d7d88d-d35e-4fb5-a5c3-7773c20a72d9")
                        .withPrincipals(Arrays.asList(new Principal().withId("355a6bb0-abc0-4cba-000d-12a345b678c9")
                            .withType(PrincipalType.USER)))))
                .withWorkloadRoleAssignments(Arrays.asList(
                    new RoleAssignmentItem().withRoleDefinitionId("d73bb868-a0df-4d4d-bd69-98a00b01fccb")
                        .withPrincipals(Arrays.asList(new Principal().withId("01234567-89ab-ef01-2345-0123456789ab")
                            .withType(PrincipalType.GROUP))),
                    new RoleAssignmentItem().withRoleDefinitionId("fb879df8-f326-4884-b1cf-06f3ad86be52")
                        .withPrincipals(Arrays.asList(new Principal().withId("01234567-89ab-ef01-2345-0123456789ab")
                            .withType(PrincipalType.GROUP)))))
                .withEnclaveDefaultSettings(
                    new EnclaveDefaultSettingsPatchModel().withDiagnosticDestination(DiagnosticDestination.BOTH))
                .withMaintenanceModeConfiguration(new MaintenanceModeConfigurationPatchModel()
                    .withMode(MaintenanceModeConfigurationModelMode.OFF)
                    .withPrincipals(Arrays.asList(
                        new Principal().withId("355a6bb0-abc0-4cba-000d-12a345b678c9").withType(PrincipalType.USER)))
                    .withJustification(MaintenanceModeConfigurationModelJustification.OFF)))
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

### Workload_CreateOrUpdate

```java
import com.azure.resourcemanager.virtualenclaves.models.WorkloadProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Workload CreateOrUpdate.
 */
public final class WorkloadCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Workload_CreateOrUpdate.json
     */
    /**
     * Sample code: Workload_CreateOrUpdate.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void
        workloadCreateOrUpdate(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.workloads()
            .define("TestMyWorkload")
            .withRegion("westcentralus")
            .withExistingVirtualEnclave("rgopenapi", "TestMyEnclave")
            .withTags(mapOf("TestKey", "fakeTokenPlaceholder"))
            .withProperties(new WorkloadProperties().withResourceGroupCollection(Arrays.asList()))
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

### Workload_Delete

```java
/**
 * Samples for Workload Delete.
 */
public final class WorkloadDeleteSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Workload_Delete.json
     */
    /**
     * Sample code: Workload_Delete.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void workloadDelete(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.workloads().delete("rgopenapi", "TestMyEnclave", "TestMyWorkload", com.azure.core.util.Context.NONE);
    }
}
```

### Workload_Get

```java
/**
 * Samples for Workload Get.
 */
public final class WorkloadGetSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Workload_Get.json
     */
    /**
     * Sample code: Workload_Get.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void workloadGet(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.workloads()
            .getWithResponse("rgopenapi", "TestMyEnclave", "TestMyWorkload", com.azure.core.util.Context.NONE);
    }
}
```

### Workload_ListByEnclaveResource

```java
/**
 * Samples for Workload ListByEnclaveResource.
 */
public final class WorkloadListByEnclaveResourceSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Workload_ListByEnclaveResource.json
     */
    /**
     * Sample code: Workload_ListByEnclaveResource.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void
        workloadListByEnclaveResource(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.workloads().listByEnclaveResource("rgopenapi", "TestMyEnclave", com.azure.core.util.Context.NONE);
    }
}
```

### Workload_ListBySubscription

```java
/**
 * Samples for Workload ListBySubscription.
 */
public final class WorkloadListBySubscriptionSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Workload_ListBySubscription.json
     */
    /**
     * Sample code: Workload_ListBySubscription.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void
        workloadListBySubscription(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        manager.workloads().listBySubscription("TestMyEnclave", com.azure.core.util.Context.NONE);
    }
}
```

### Workload_Update

```java
import com.azure.resourcemanager.virtualenclaves.models.WorkloadPatchProperties;
import com.azure.resourcemanager.virtualenclaves.models.WorkloadResource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Workload Update.
 */
public final class WorkloadUpdateSamples {
    /*
     * x-ms-original-file: 2025-05-01-preview/Workload_Update.json
     */
    /**
     * Sample code: Workload_Update.
     * 
     * @param manager Entry point to VirtualEnclavesManager.
     */
    public static void workloadUpdate(com.azure.resourcemanager.virtualenclaves.VirtualEnclavesManager manager) {
        WorkloadResource resource = manager.workloads()
            .getWithResponse("rgopenapi", "TestMyEnclave", "TestMyWorkload", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key9465", "fakeTokenPlaceholder"))
            .withProperties(new WorkloadPatchProperties().withResourceGroupCollection(Arrays.asList("g")))
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

