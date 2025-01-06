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
     * x-ms-original-file: 2024-08-01-preview/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void operationsList(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Organizations_CreateOrUpdate

```java
import com.azure.resourcemanager.neonpostgres.models.CompanyDetails;
import com.azure.resourcemanager.neonpostgres.models.MarketplaceDetails;
import com.azure.resourcemanager.neonpostgres.models.MarketplaceSubscriptionStatus;
import com.azure.resourcemanager.neonpostgres.models.OfferDetails;
import com.azure.resourcemanager.neonpostgres.models.OrganizationProperties;
import com.azure.resourcemanager.neonpostgres.models.PartnerOrganizationProperties;
import com.azure.resourcemanager.neonpostgres.models.SingleSignOnProperties;
import com.azure.resourcemanager.neonpostgres.models.SingleSignOnStates;
import com.azure.resourcemanager.neonpostgres.models.UserDetails;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Organizations CreateOrUpdate.
 */
public final class OrganizationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-08-01-preview/Organizations_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_CreateOrUpdate.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void organizationsCreateOrUpdate(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.organizations()
            .define("XB-.:")
            .withRegion("upxxgikyqrbnv")
            .withExistingResourceGroup("rgneon")
            .withTags(mapOf("key2099", "fakeTokenPlaceholder"))
            .withProperties(new OrganizationProperties()
                .withMarketplaceDetails(new MarketplaceDetails().withSubscriptionId("yxmkfivp")
                    .withSubscriptionStatus(MarketplaceSubscriptionStatus.PENDING_FULFILLMENT_START)
                    .withOfferDetails(new OfferDetails().withPublisherId("hporaxnopmolttlnkbarw")
                        .withOfferId("bunyeeupoedueofwrzej")
                        .withPlanId("nlbfiwtslenfwek")
                        .withPlanName("ljbmgpkfqklaufacbpml")
                        .withTermUnit("qbcq")
                        .withTermId("aedlchikwqckuploswthvshe")))
                .withUserDetails(new UserDetails().withFirstName("buwwe")
                    .withLastName("escynjpynkoox")
                    .withEmailAddress("3i_%@w8-y.H-p.tvj.dG")
                    .withUpn("fwedjamgwwrotcjaucuzdwycfjdqn")
                    .withPhoneNumber("dlrqoowumy"))
                .withCompanyDetails(new CompanyDetails().withCompanyName("uxn")
                    .withCountry("lpajqzptqchuko")
                    .withOfficeAddress("chpkrlpmfslmawgunjxdllzcrctykq")
                    .withBusinessPhone("hbeb")
                    .withDomain("krjldeakhwiepvs")
                    .withNumberOfEmployees(23L))
                .withPartnerOrganizationProperties(
                    new PartnerOrganizationProperties().withOrganizationId("nrhvoqzulowcunhmvwfgjcaibvwcl")
                        .withOrganizationName("2__.-")
                        .withSingleSignOnProperties(
                            new SingleSignOnProperties().withSingleSignOnState(SingleSignOnStates.INITIAL)
                                .withEnterpriseAppId("fpibacregjfncfdsojs")
                                .withSingleSignOnUrl("tmojh")
                                .withAadDomains(Arrays.asList("kndszgrwzbvvlssvkej")))))
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
     * x-ms-original-file: 2024-08-01-preview/Organizations_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_Delete.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void organizationsDelete(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.organizations().delete("rgneon", "2_3", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2024-08-01-preview/Organizations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_Get.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void organizationsGet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.organizations().getByResourceGroupWithResponse("rgneon", "5", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2024-08-01-preview/Organizations_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_ListBySubscription.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void
        organizationsListBySubscription(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
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
     * x-ms-original-file: 2024-08-01-preview/Organizations_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_ListByResourceGroup.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void
        organizationsListByResourceGroup(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.organizations().listByResourceGroup("rgneon", com.azure.core.util.Context.NONE);
    }
}
```

### Organizations_Update

```java
import com.azure.resourcemanager.neonpostgres.models.CompanyDetails;
import com.azure.resourcemanager.neonpostgres.models.OrganizationProperties;
import com.azure.resourcemanager.neonpostgres.models.OrganizationResource;
import com.azure.resourcemanager.neonpostgres.models.PartnerOrganizationProperties;
import com.azure.resourcemanager.neonpostgres.models.SingleSignOnProperties;
import com.azure.resourcemanager.neonpostgres.models.SingleSignOnStates;
import com.azure.resourcemanager.neonpostgres.models.UserDetails;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Organizations Update.
 */
public final class OrganizationsUpdateSamples {
    /*
     * x-ms-original-file: 2024-08-01-preview/Organizations_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_Update.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void organizationsUpdate(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        OrganizationResource resource = manager.organizations()
            .getByResourceGroupWithResponse("rgneon", "eRY-J_:", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key8990", "fakeTokenPlaceholder"))
            .withProperties(new OrganizationProperties()
                .withUserDetails(new UserDetails().withFirstName("buwwe")
                    .withLastName("escynjpynkoox")
                    .withEmailAddress("3i_%@w8-y.H-p.tvj.dG")
                    .withUpn("fwedjamgwwrotcjaucuzdwycfjdqn")
                    .withPhoneNumber("dlrqoowumy"))
                .withCompanyDetails(new CompanyDetails().withCompanyName("uxn")
                    .withCountry("lpajqzptqchuko")
                    .withOfficeAddress("chpkrlpmfslmawgunjxdllzcrctykq")
                    .withBusinessPhone("hbeb")
                    .withDomain("krjldeakhwiepvs")
                    .withNumberOfEmployees(23L))
                .withPartnerOrganizationProperties(
                    new PartnerOrganizationProperties().withOrganizationId("njyoqflcmfwzfsqe")
                        .withOrganizationName("J:.._3P")
                        .withSingleSignOnProperties(
                            new SingleSignOnProperties().withSingleSignOnState(SingleSignOnStates.INITIAL)
                                .withEnterpriseAppId("fpibacregjfncfdsojs")
                                .withSingleSignOnUrl("tmojh")
                                .withAadDomains(Arrays.asList("kndszgrwzbvvlssvkej")))))
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

