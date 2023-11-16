# Code snippets and samples


## Access

- [InviteUser](#access_inviteuser)
- [ListClusters](#access_listclusters)
- [ListEnvironments](#access_listenvironments)
- [ListInvitations](#access_listinvitations)
- [ListRoleBindings](#access_listrolebindings)
- [ListServiceAccounts](#access_listserviceaccounts)
- [ListUsers](#access_listusers)

## MarketplaceAgreements

- [Create](#marketplaceagreements_create)
- [List](#marketplaceagreements_list)

## Organization

- [Create](#organization_create)
- [Delete](#organization_delete)
- [GetByResourceGroup](#organization_getbyresourcegroup)
- [List](#organization_list)
- [ListByResourceGroup](#organization_listbyresourcegroup)
- [Update](#organization_update)

## OrganizationOperations

- [List](#organizationoperations_list)

## Validations

- [ValidateOrganization](#validations_validateorganization)
- [ValidateOrganizationV2](#validations_validateorganizationv2)
### Access_InviteUser

```java
import com.azure.resourcemanager.confluent.models.AccessInvitedUserDetails;
import com.azure.resourcemanager.confluent.models.AccessInviteUserAccountModel;

/**
 * Samples for Access InviteUser.
 */
public final class AccessInviteUserSamples {
    /*
     * x-ms-original-file: specification/confluent/resource-manager/Microsoft.Confluent/stable/2023-08-22/examples/Access_InviteUser.json
     */
    /**
     * Sample code: Access_InviteUser.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessInviteUser(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access().inviteUserWithResponse("myResourceGroup", "myOrganization", new AccessInviteUserAccountModel().withInvitedUserDetails(new AccessInvitedUserDetails().withInvitedEmail("user2@onmicrosoft.com").withAuthType("AUTH_TYPE_SSO")), com.azure.core.util.Context.NONE);
    }
}
```

### Access_ListClusters

```java
import com.azure.resourcemanager.confluent.models.ListAccessRequestModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Access ListClusters.
 */
public final class AccessListClustersSamples {
    /*
     * x-ms-original-file: specification/confluent/resource-manager/Microsoft.Confluent/stable/2023-08-22/examples/Access_ClusterList.json
     */
    /**
     * Sample code: Access_ClusterList.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessClusterList(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access().listClustersWithResponse("myResourceGroup", "myOrganization", new ListAccessRequestModel().withSearchFilters(mapOf("pageSize", "10", "pageToken", "fakeTokenPlaceholder")), com.azure.core.util.Context.NONE);
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

### Access_ListEnvironments

```java
import com.azure.resourcemanager.confluent.models.ListAccessRequestModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Access ListEnvironments.
 */
public final class AccessListEnvironmentsSamples {
    /*
     * x-ms-original-file: specification/confluent/resource-manager/Microsoft.Confluent/stable/2023-08-22/examples/Access_EnvironmentList.json
     */
    /**
     * Sample code: Access_EnvironmentList.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessEnvironmentList(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access().listEnvironmentsWithResponse("myResourceGroup", "myOrganization", new ListAccessRequestModel().withSearchFilters(mapOf("pageSize", "10", "pageToken", "fakeTokenPlaceholder")), com.azure.core.util.Context.NONE);
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

### Access_ListInvitations

```java
import com.azure.resourcemanager.confluent.models.ListAccessRequestModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Access ListInvitations.
 */
public final class AccessListInvitationsSamples {
    /*
     * x-ms-original-file: specification/confluent/resource-manager/Microsoft.Confluent/stable/2023-08-22/examples/Access_InvitationsList.json
     */
    /**
     * Sample code: Access_InvitationsList.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessInvitationsList(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access().listInvitationsWithResponse("myResourceGroup", "myOrganization", new ListAccessRequestModel().withSearchFilters(mapOf("pageSize", "10", "pageToken", "fakeTokenPlaceholder", "status", "INVITE_STATUS_SENT")), com.azure.core.util.Context.NONE);
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

### Access_ListRoleBindings

```java
import com.azure.resourcemanager.confluent.models.ListAccessRequestModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Access ListRoleBindings.
 */
public final class AccessListRoleBindingsSamples {
    /*
     * x-ms-original-file: specification/confluent/resource-manager/Microsoft.Confluent/stable/2023-08-22/examples/Access_RoleBindingList.json
     */
    /**
     * Sample code: Access_RoleBindingList.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessRoleBindingList(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access().listRoleBindingsWithResponse("myResourceGroup", "myOrganization", new ListAccessRequestModel().withSearchFilters(mapOf("pageSize", "10", "pageToken", "fakeTokenPlaceholder")), com.azure.core.util.Context.NONE);
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

### Access_ListServiceAccounts

```java
import com.azure.resourcemanager.confluent.models.ListAccessRequestModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Access ListServiceAccounts.
 */
public final class AccessListServiceAccountsSamples {
    /*
     * x-ms-original-file: specification/confluent/resource-manager/Microsoft.Confluent/stable/2023-08-22/examples/Access_ServiceAccountsList.json
     */
    /**
     * Sample code: Access_ServiceAccountsList.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessServiceAccountsList(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access().listServiceAccountsWithResponse("myResourceGroup", "myOrganization", new ListAccessRequestModel().withSearchFilters(mapOf("pageSize", "10", "pageToken", "fakeTokenPlaceholder")), com.azure.core.util.Context.NONE);
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

### Access_ListUsers

```java
import com.azure.resourcemanager.confluent.models.ListAccessRequestModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Access ListUsers.
 */
public final class AccessListUsersSamples {
    /*
     * x-ms-original-file: specification/confluent/resource-manager/Microsoft.Confluent/stable/2023-08-22/examples/Access_UsersList.json
     */
    /**
     * Sample code: Access_UsersList.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void accessUsersList(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.access().listUsersWithResponse("myResourceGroup", "myOrganization", new ListAccessRequestModel().withSearchFilters(mapOf("pageSize", "10", "pageToken", "fakeTokenPlaceholder")), com.azure.core.util.Context.NONE);
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

### MarketplaceAgreements_Create

```java
import com.azure.resourcemanager.confluent.fluent.models.ConfluentAgreementResourceInner;

/**
 * Samples for MarketplaceAgreements Create.
 */
public final class MarketplaceAgreementsCreateSamples {
    /*
     * x-ms-original-file: specification/confluent/resource-manager/Microsoft.Confluent/stable/2023-08-22/examples/MarketplaceAgreements_Create.json
     */
    /**
     * Sample code: MarketplaceAgreements_Create.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void marketplaceAgreementsCreate(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.marketplaceAgreements().createWithResponse(null, com.azure.core.util.Context.NONE);
    }
}
```

### MarketplaceAgreements_List

```java
/**
 * Samples for MarketplaceAgreements List.
 */
public final class MarketplaceAgreementsListSamples {
    /*
     * x-ms-original-file: specification/confluent/resource-manager/Microsoft.Confluent/stable/2023-08-22/examples/MarketplaceAgreements_List.json
     */
    /**
     * Sample code: MarketplaceAgreements_List.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void marketplaceAgreementsList(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.marketplaceAgreements().list(com.azure.core.util.Context.NONE);
    }
}
```

### Organization_Create

```java
import com.azure.resourcemanager.confluent.models.LinkOrganization;
import com.azure.resourcemanager.confluent.models.OfferDetail;
import com.azure.resourcemanager.confluent.models.UserDetail;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Organization Create.
 */
public final class OrganizationCreateSamples {
    /*
     * x-ms-original-file: specification/confluent/resource-manager/Microsoft.Confluent/stable/2023-08-22/examples/Organization_Create.json
     */
    /**
     * Sample code: Organization_Create.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationCreate(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations().define("myOrganization").withRegion("West US").withExistingResourceGroup("myResourceGroup").withOfferDetail(new OfferDetail().withPublisherId("string").withId("string").withPlanId("string").withPlanName("string").withTermUnit("string").withPrivateOfferId("string").withPrivateOfferIds(Arrays.asList("string"))).withUserDetail(new UserDetail().withFirstName("string").withLastName("string").withEmailAddress("contoso@microsoft.com").withUserPrincipalName("contoso@microsoft.com").withAadEmail("contoso@microsoft.com")).withTags(mapOf("Environment", "Dev")).withLinkOrganization(new LinkOrganization().withToken("fakeTokenPlaceholder")).create();
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

### Organization_Delete

```java
/**
 * Samples for Organization Delete.
 */
public final class OrganizationDeleteSamples {
    /*
     * x-ms-original-file: specification/confluent/resource-manager/Microsoft.Confluent/stable/2023-08-22/examples/Organization_Delete.json
     */
    /**
     * Sample code: Confluent_Delete.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void confluentDelete(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations().delete("myResourceGroup", "myOrganization", com.azure.core.util.Context.NONE);
    }
}
```

### Organization_GetByResourceGroup

```java
/**
 * Samples for Organization GetByResourceGroup.
 */
public final class OrganizationGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/confluent/resource-manager/Microsoft.Confluent/stable/2023-08-22/examples/Organization_Get.json
     */
    /**
     * Sample code: Organization_Get.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationGet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations().getByResourceGroupWithResponse("myResourceGroup", "myOrganization", com.azure.core.util.Context.NONE);
    }
}
```

### Organization_List

```java
/**
 * Samples for Organization List.
 */
public final class OrganizationListSamples {
    /*
     * x-ms-original-file: specification/confluent/resource-manager/Microsoft.Confluent/stable/2023-08-22/examples/Organization_ListBySubscription.json
     */
    /**
     * Sample code: Organization_ListBySubscription.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationListBySubscription(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Organization_ListByResourceGroup

```java
/**
 * Samples for Organization ListByResourceGroup.
 */
public final class OrganizationListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/confluent/resource-manager/Microsoft.Confluent/stable/2023-08-22/examples/Organization_ListByResourceGroup.json
     */
    /**
     * Sample code: Organization_ListByResourceGroup.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationListByResourceGroup(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### Organization_Update

```java
import com.azure.resourcemanager.confluent.models.OrganizationResource;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Organization Update.
 */
public final class OrganizationUpdateSamples {
    /*
     * x-ms-original-file: specification/confluent/resource-manager/Microsoft.Confluent/stable/2023-08-22/examples/Organization_Update.json
     */
    /**
     * Sample code: Confluent_Update.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void confluentUpdate(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        OrganizationResource resource = manager.organizations().getByResourceGroupWithResponse("myResourceGroup", "myOrganization", com.azure.core.util.Context.NONE).getValue();
        resource.update().withTags(mapOf("client", "dev-client", "env", "dev")).apply();
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

### OrganizationOperations_List

```java
/**
 * Samples for OrganizationOperations List.
 */
public final class OrganizationOperationsListSamples {
    /*
     * x-ms-original-file: specification/confluent/resource-manager/Microsoft.Confluent/stable/2023-08-22/examples/OrganizationOperations_List.json
     */
    /**
     * Sample code: OrganizationOperations_List.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationOperationsList(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizationOperations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Validations_ValidateOrganization

```java
import com.azure.resourcemanager.confluent.fluent.models.OrganizationResourceInner;
import com.azure.resourcemanager.confluent.models.OfferDetail;
import com.azure.resourcemanager.confluent.models.UserDetail;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Validations ValidateOrganization.
 */
public final class ValidationsValidateOrganizationSamples {
    /*
     * x-ms-original-file: specification/confluent/resource-manager/Microsoft.Confluent/stable/2023-08-22/examples/Validations_ValidateOrganizations.json
     */
    /**
     * Sample code: Validations_ValidateOrganizations.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void validationsValidateOrganizations(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.validations().validateOrganizationWithResponse("myResourceGroup", "myOrganization", new OrganizationResourceInner().withLocation("West US").withTags(mapOf("Environment", "Dev")).withOfferDetail(new OfferDetail().withPublisherId("string").withId("string").withPlanId("string").withPlanName("string").withTermUnit("string").withPrivateOfferId("string").withPrivateOfferIds(Arrays.asList("string"))).withUserDetail(new UserDetail().withFirstName("string").withLastName("string").withEmailAddress("abc@microsoft.com").withUserPrincipalName("abc@microsoft.com").withAadEmail("abc@microsoft.com")), com.azure.core.util.Context.NONE);
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

### Validations_ValidateOrganizationV2

```java
import com.azure.resourcemanager.confluent.fluent.models.OrganizationResourceInner;
import com.azure.resourcemanager.confluent.models.OfferDetail;
import com.azure.resourcemanager.confluent.models.UserDetail;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Validations ValidateOrganizationV2.
 */
public final class ValidationsValidateOrganizationV2Samples {
    /*
     * x-ms-original-file: specification/confluent/resource-manager/Microsoft.Confluent/stable/2023-08-22/examples/Validations_ValidateOrganizationsV2.json
     */
    /**
     * Sample code: Validations_ValidateOrganizations.
     * 
     * @param manager Entry point to ConfluentManager.
     */
    public static void validationsValidateOrganizations(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.validations().validateOrganizationV2WithResponse("myResourceGroup", "myOrganization", new OrganizationResourceInner().withLocation("West US").withTags(mapOf("Environment", "Dev")).withOfferDetail(new OfferDetail().withPublisherId("string").withId("string").withPlanId("string").withPlanName("string").withTermUnit("string").withPrivateOfferId("string").withPrivateOfferIds(Arrays.asList("string"))).withUserDetail(new UserDetail().withFirstName("string").withLastName("string").withEmailAddress("abc@microsoft.com").withUserPrincipalName("abc@microsoft.com").withAadEmail("abc@microsoft.com")), com.azure.core.util.Context.NONE);
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

