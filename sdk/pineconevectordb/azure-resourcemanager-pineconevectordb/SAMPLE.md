# Code snippets and samples


## Operations

- [List](#operations_list)

## Organizations

- [CreateOrUpdate](#organizations_createorupdate)
- [Delete](#organizations_delete)
- [GetByResourceGroup](#organizations_getbyresourcegroup)
- [List](#organizations_list)
- [ListByResourceGroup](#organizations_listbyresourcegroup)
- [Update](#organizations_update)
### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2024-10-22-preview/Operations_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MinimumSet.
     * 
     * @param manager Entry point to PineconeVectorDbManager.
     */
    public static void
        operationsListMinimumSet(com.azure.resourcemanager.pineconevectordb.PineconeVectorDbManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2024-10-22-preview/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MaximumSet.
     * 
     * @param manager Entry point to PineconeVectorDbManager.
     */
    public static void
        operationsListMaximumSet(com.azure.resourcemanager.pineconevectordb.PineconeVectorDbManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Organizations_CreateOrUpdate

```java
import com.azure.resourcemanager.pineconevectordb.models.ManagedServiceIdentity;
import com.azure.resourcemanager.pineconevectordb.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.pineconevectordb.models.MarketplaceDetails;
import com.azure.resourcemanager.pineconevectordb.models.OfferDetails;
import com.azure.resourcemanager.pineconevectordb.models.OrganizationProperties;
import com.azure.resourcemanager.pineconevectordb.models.PartnerProperties;
import com.azure.resourcemanager.pineconevectordb.models.SingleSignOnPropertiesV2;
import com.azure.resourcemanager.pineconevectordb.models.SingleSignOnStates;
import com.azure.resourcemanager.pineconevectordb.models.SingleSignOnType;
import com.azure.resourcemanager.pineconevectordb.models.UserAssignedIdentity;
import com.azure.resourcemanager.pineconevectordb.models.UserDetails;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Organizations CreateOrUpdate.
 */
public final class OrganizationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-10-22-preview/Organizations_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to PineconeVectorDbManager.
     */
    public static void organizationsCreateOrUpdateMaximumSet(
        com.azure.resourcemanager.pineconevectordb.PineconeVectorDbManager manager) {
        manager.organizations()
            .define("example-organization-name")
            .withRegion("us-east")
            .withExistingResourceGroup("rgopenapi")
            .withTags(mapOf("my-tag", "tag.value"))
            .withProperties(new OrganizationProperties()
                .withMarketplace(new MarketplaceDetails().withSubscriptionId("76a38ef6-c8c1-4f0d-bfe0-00ec782c8077")
                    .withOfferDetails(new OfferDetails().withPublisherId("4d194daf-fa20-46a8-bfb4-5b7d96cae009")
                        .withOfferId("013124d0-bf05-4eab-a6bb-01fa83870642")
                        .withPlanId("62dda065-5acd-4ac5-b418-8610beed92a2")
                        .withPlanName("Freemium")
                        .withTermUnit("der")
                        .withTermId("a2b7ce01-f06d-4874-9f77-6ea4a4875c16")))
                .withUser(new UserDetails().withFirstName("Jimmy")
                    .withLastName("McExample")
                    .withEmailAddress("example.user@example.com")
                    .withUpn("example.user@example.com")
                    .withPhoneNumber("555-555-5555"))
                .withPartnerProperties(new PartnerProperties().withDisplayName("My Example Organization"))
                .withSingleSignOnProperties(new SingleSignOnPropertiesV2().withType(SingleSignOnType.SAML)
                    .withState(SingleSignOnStates.INITIAL)
                    .withEnterpriseAppId("44d3fb26-d8d5-41ff-9b9a-769737f22f13")
                    .withUrl("https://login.pinecone.io/?sso=true&connection=dfwgsqzkbrjqrglcsa")
                    .withAadDomains(Arrays.asList("exampledomain"))))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.NONE)
                .withUserAssignedIdentities(mapOf("ident904655400", new UserAssignedIdentity())))
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

### Organizations_Delete

```java
/**
 * Samples for Organizations Delete.
 */
public final class OrganizationsDeleteSamples {
    /*
     * x-ms-original-file: 2024-10-22-preview/Organizations_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_Delete_MaximumSet.
     * 
     * @param manager Entry point to PineconeVectorDbManager.
     */
    public static void
        organizationsDeleteMaximumSet(com.azure.resourcemanager.pineconevectordb.PineconeVectorDbManager manager) {
        manager.organizations().delete("rgopenapi", "example-organization-name", com.azure.core.util.Context.NONE);
    }
}
```

### Organizations_GetByResourceGroup

```java
/**
 * Samples for Organizations GetByResourceGroup.
 */
public final class OrganizationsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-10-22-preview/Organizations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_Get_MaximumSet.
     * 
     * @param manager Entry point to PineconeVectorDbManager.
     */
    public static void
        organizationsGetMaximumSet(com.azure.resourcemanager.pineconevectordb.PineconeVectorDbManager manager) {
        manager.organizations()
            .getByResourceGroupWithResponse("rgopenapi", "example-organization-name", com.azure.core.util.Context.NONE);
    }
}
```

### Organizations_List

```java
/**
 * Samples for Organizations List.
 */
public final class OrganizationsListSamples {
    /*
     * x-ms-original-file: 2024-10-22-preview/Organizations_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to PineconeVectorDbManager.
     */
    public static void organizationsListBySubscriptionMaximumSet(
        com.azure.resourcemanager.pineconevectordb.PineconeVectorDbManager manager) {
        manager.organizations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Organizations_ListByResourceGroup

```java
/**
 * Samples for Organizations ListByResourceGroup.
 */
public final class OrganizationsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-10-22-preview/Organizations_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to PineconeVectorDbManager.
     */
    public static void organizationsListByResourceGroupMaximumSet(
        com.azure.resourcemanager.pineconevectordb.PineconeVectorDbManager manager) {
        manager.organizations().listByResourceGroup("rgopenapi", com.azure.core.util.Context.NONE);
    }
}
```

### Organizations_Update

```java
import com.azure.resourcemanager.pineconevectordb.models.ManagedServiceIdentity;
import com.azure.resourcemanager.pineconevectordb.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.pineconevectordb.models.OrganizationResource;
import com.azure.resourcemanager.pineconevectordb.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Organizations Update.
 */
public final class OrganizationsUpdateSamples {
    /*
     * x-ms-original-file: 2024-10-22-preview/Organizations_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_Update_MaximumSet.
     * 
     * @param manager Entry point to PineconeVectorDbManager.
     */
    public static void
        organizationsUpdateMaximumSet(com.azure.resourcemanager.pineconevectordb.PineconeVectorDbManager manager) {
        OrganizationResource resource = manager.organizations()
            .getByResourceGroupWithResponse("rgopenapi", "example-organization-name", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("new-tag", "new.tag.value"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.NONE)
                .withUserAssignedIdentities(mapOf("ident573739201", new UserAssignedIdentity())))
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

