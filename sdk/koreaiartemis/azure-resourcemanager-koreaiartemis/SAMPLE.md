# Code snippets and samples


## Operations

- [List](#operations_list)

## Organizations

- [Create](#organizations_create)
- [Delete](#organizations_delete)
- [GetByResourceGroup](#organizations_getbyresourcegroup)
- [List](#organizations_list)
- [ListByResourceGroup](#organizations_listbyresourcegroup)
- [ListWorkspaces](#organizations_listworkspaces)
- [Update](#organizations_update)

## SaaSOperationGroup

- [ActivateResource](#saasoperationgroup_activateresource)

## Workspaces

- [CreateOrUpdate](#workspaces_createorupdate)
- [Delete](#workspaces_delete)
- [Get](#workspaces_get)
- [GetDetails](#workspaces_getdetails)
- [LinkSaaS](#workspaces_linksaas)
- [ListByOrganization](#workspaces_listbyorganization)
- [ListLinkedSaaS](#workspaces_listlinkedsaas)
### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/Operations_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MinimumSet_Gen.
     * 
     * @param manager Entry point to KoreAIArtemisManager.
     */
    public static void
        operationsListMinimumSetGen(com.azure.resourcemanager.koreaiartemis.KoreAIArtemisManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-07-01-preview/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to KoreAIArtemisManager.
     */
    public static void operationsList(com.azure.resourcemanager.koreaiartemis.KoreAIArtemisManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-07-01-preview/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MaximumSet_Gen.
     * 
     * @param manager Entry point to KoreAIArtemisManager.
     */
    public static void
        operationsListMaximumSetGen(com.azure.resourcemanager.koreaiartemis.KoreAIArtemisManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Organizations_Create

```java
import com.azure.resourcemanager.koreaiartemis.models.CompanyDetails;
import com.azure.resourcemanager.koreaiartemis.models.DefaultWorkspaceProperties;
import com.azure.resourcemanager.koreaiartemis.models.MarketplaceDetails;
import com.azure.resourcemanager.koreaiartemis.models.OfferDetails;
import com.azure.resourcemanager.koreaiartemis.models.OrganizationPartnerProperties;
import com.azure.resourcemanager.koreaiartemis.models.OrganizationProperties;
import com.azure.resourcemanager.koreaiartemis.models.Product;
import com.azure.resourcemanager.koreaiartemis.models.UserDetails;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Organizations Create.
 */
public final class OrganizationsCreateSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/Organizations_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_Create_MaximumSet_Gen.
     * 
     * @param manager Entry point to KoreAIArtemisManager.
     */
    public static void
        organizationsCreateMaximumSetGen(com.azure.resourcemanager.koreaiartemis.KoreAIArtemisManager manager) {
        manager.organizations()
            .define("contoso-org")
            .withRegion("eastus2euap")
            .withExistingResourceGroup("contoso-rg")
            .withTags(mapOf("environment", "production"))
            .withProperties(new OrganizationProperties().withPartnerProperties(new OrganizationPartnerProperties()
                .withOrganizationName("Contoso AI Platform")
                .withBillingEmail("billing@contoso.com")
                .withOwnerEmail("admin@contoso.com")
                .withDefaultWorkspace(new DefaultWorkspaceProperties().withName("my first workspace")
                    .withProduct(Product.AIFORWORK)
                    .withMarketplaceDetails(Arrays.asList(new MarketplaceDetails().withSubscriptionId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/contoso-rg/providers/Microsoft.SaaS/resources/kore-ai-saas")
                        .withOfferDetails(new OfferDetails().withPublisherId("kore-ai")
                            .withOfferId("kore-ai-platform")
                            .withPlanId("business-monthly")
                            .withPlanName("Business Monthly")
                            .withTermUnit("P1M")
                            .withTermId("1m"))))))
                .withUser(new UserDetails().withFirstName("Jane")
                    .withLastName("Doe")
                    .withEmailAddress("jane.doe@contoso.com")
                    .withUpn("jane.doe@contoso.onmicrosoft.com")
                    .withPhoneNumber("+1-425-555-0100")
                    .withAlternateEmail("jane.alternate@contoso.com")
                    .withAddress("1 Microsoft Way, Redmond, WA 98052"))
                .withCompanyDetails(new CompanyDetails().withCompanyName("Contoso Ltd.")
                    .withOfficeAddress("1 Microsoft Way, Redmond, WA 98052")
                    .withCountry("US")
                    .withDomain("contoso.com")
                    .withBusinessType("Technology")
                    .withNumberOfEmployees(500)))
            .create();
    }

    /*
     * x-ms-original-file: 2026-07-01-preview/Organizations_Create.json
     */
    /**
     * Sample code: Organizations_Create.
     * 
     * @param manager Entry point to KoreAIArtemisManager.
     */
    public static void organizationsCreate(com.azure.resourcemanager.koreaiartemis.KoreAIArtemisManager manager) {
        manager.organizations()
            .define("contoso-org")
            .withRegion("eastus2euap")
            .withExistingResourceGroup("contoso-rg")
            .withTags(mapOf("environment", "production"))
            .withProperties(new OrganizationProperties().withPartnerProperties(new OrganizationPartnerProperties()
                .withOrganizationName("Contoso AI Platform")
                .withBillingEmail("billing@contoso.com")
                .withOwnerEmail("admin@contoso.com")
                .withDefaultWorkspace(new DefaultWorkspaceProperties().withName("my first workspace")
                    .withProduct(Product.AIFORWORK)
                    .withMarketplaceDetails(Arrays.asList(new MarketplaceDetails().withSubscriptionId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/contoso-rg/providers/Microsoft.SaaS/resources/kore-ai-saas")
                        .withOfferDetails(new OfferDetails().withPublisherId("kore-ai")
                            .withOfferId("kore-ai-platform")
                            .withPlanId("business-monthly")
                            .withPlanName("Business Monthly")
                            .withTermUnit("P1M")
                            .withTermId("1m"))))))
                .withUser(new UserDetails().withFirstName("Jane")
                    .withLastName("Doe")
                    .withEmailAddress("jane.doe@contoso.com")
                    .withUpn("jane.doe@contoso.onmicrosoft.com")
                    .withPhoneNumber("+1-425-555-0100"))
                .withCompanyDetails(new CompanyDetails().withCompanyName("Contoso Ltd.")
                    .withCountry("US")
                    .withDomain("contoso.com")
                    .withBusinessType("Technology")
                    .withNumberOfEmployees(500)))
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
     * x-ms-original-file: 2026-07-01-preview/Organizations_Delete.json
     */
    /**
     * Sample code: Organizations_Delete.
     * 
     * @param manager Entry point to KoreAIArtemisManager.
     */
    public static void organizationsDelete(com.azure.resourcemanager.koreaiartemis.KoreAIArtemisManager manager) {
        manager.organizations().delete("contoso-rg", "contoso-org", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-07-01-preview/Organizations_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to KoreAIArtemisManager.
     */
    public static void
        organizationsDeleteMaximumSetGen(com.azure.resourcemanager.koreaiartemis.KoreAIArtemisManager manager) {
        manager.organizations().delete("contoso-rg", "contoso-org", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-07-01-preview/Organizations_Get.json
     */
    /**
     * Sample code: Organizations_Get.
     * 
     * @param manager Entry point to KoreAIArtemisManager.
     */
    public static void organizationsGet(com.azure.resourcemanager.koreaiartemis.KoreAIArtemisManager manager) {
        manager.organizations()
            .getByResourceGroupWithResponse("contoso-rg", "contoso-org", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-07-01-preview/Organizations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to KoreAIArtemisManager.
     */
    public static void
        organizationsGetMaximumSetGen(com.azure.resourcemanager.koreaiartemis.KoreAIArtemisManager manager) {
        manager.organizations()
            .getByResourceGroupWithResponse("contoso-rg", "contoso-org", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-07-01-preview/Organizations_ListBySubscription.json
     */
    /**
     * Sample code: Organizations_ListBySubscription.
     * 
     * @param manager Entry point to KoreAIArtemisManager.
     */
    public static void
        organizationsListBySubscription(com.azure.resourcemanager.koreaiartemis.KoreAIArtemisManager manager) {
        manager.organizations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-07-01-preview/Organizations_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_ListBySubscription_MaximumSet_Gen.
     * 
     * @param manager Entry point to KoreAIArtemisManager.
     */
    public static void organizationsListBySubscriptionMaximumSetGen(
        com.azure.resourcemanager.koreaiartemis.KoreAIArtemisManager manager) {
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
     * x-ms-original-file: 2026-07-01-preview/Organizations_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_ListByResourceGroup_MaximumSet_Gen.
     * 
     * @param manager Entry point to KoreAIArtemisManager.
     */
    public static void organizationsListByResourceGroupMaximumSetGen(
        com.azure.resourcemanager.koreaiartemis.KoreAIArtemisManager manager) {
        manager.organizations().listByResourceGroup("contoso-rg", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-07-01-preview/Organizations_ListByResourceGroup.json
     */
    /**
     * Sample code: Organizations_ListByResourceGroup.
     * 
     * @param manager Entry point to KoreAIArtemisManager.
     */
    public static void
        organizationsListByResourceGroup(com.azure.resourcemanager.koreaiartemis.KoreAIArtemisManager manager) {
        manager.organizations().listByResourceGroup("contoso-rg", com.azure.core.util.Context.NONE);
    }
}
```

### Organizations_ListWorkspaces

```java
/**
 * Samples for Organizations ListWorkspaces.
 */
public final class OrganizationsListWorkspacesSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/Organizations_ListWorkspaces.json
     */
    /**
     * Sample code: Organizations_ListWorkspaces.
     * 
     * @param manager Entry point to KoreAIArtemisManager.
     */
    public static void
        organizationsListWorkspaces(com.azure.resourcemanager.koreaiartemis.KoreAIArtemisManager manager) {
        manager.organizations()
            .listWorkspacesWithResponse("contoso-rg", "contoso-org", com.azure.core.util.Context.NONE);
    }
}
```

### Organizations_Update

```java
import com.azure.resourcemanager.koreaiartemis.models.CompanyDetails;
import com.azure.resourcemanager.koreaiartemis.models.OrganizationPartnerPropertiesUpdate;
import com.azure.resourcemanager.koreaiartemis.models.OrganizationPropertiesUpdate;
import com.azure.resourcemanager.koreaiartemis.models.OrganizationResource;
import com.azure.resourcemanager.koreaiartemis.models.UserDetails;

/**
 * Samples for Organizations Update.
 */
public final class OrganizationsUpdateSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/Organizations_Update.json
     */
    /**
     * Sample code: Organizations_Update.
     * 
     * @param manager Entry point to KoreAIArtemisManager.
     */
    public static void organizationsUpdate(com.azure.resourcemanager.koreaiartemis.KoreAIArtemisManager manager) {
        OrganizationResource resource = manager.organizations()
            .getByResourceGroupWithResponse("contoso-rg", "contoso-org", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new OrganizationPropertiesUpdate().withPartnerProperties(
                new OrganizationPartnerPropertiesUpdate().withOrganizationName("Contoso AI Platform (Updated)")
                    .withBillingEmail("new-billing@contoso.com")
                    .withOwnerEmail("admin@contoso.com")))
            .apply();
    }

    /*
     * x-ms-original-file: 2026-07-01-preview/Organizations_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_Update_MaximumSet_Gen.
     * 
     * @param manager Entry point to KoreAIArtemisManager.
     */
    public static void
        organizationsUpdateMaximumSetGen(com.azure.resourcemanager.koreaiartemis.KoreAIArtemisManager manager) {
        OrganizationResource resource = manager.organizations()
            .getByResourceGroupWithResponse("contoso-rg", "contoso-org", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new OrganizationPropertiesUpdate()
                .withPartnerProperties(
                    new OrganizationPartnerPropertiesUpdate().withOrganizationName("Contoso AI Platform (Updated)")
                        .withBillingEmail("new-billing@contoso.com")
                        .withOwnerEmail("admin@contoso.com"))
                .withUser(new UserDetails().withFirstName("Jane")
                    .withLastName("Doe")
                    .withEmailAddress("jane.alternate@contoso.com")
                    .withUpn("jane.doe@contoso.onmicrosoft.com")
                    .withPhoneNumber("+1-425-555-0100")
                    .withAlternateEmail("jane.alternate@contoso.com")
                    .withAddress("1 Microsoft Way, Redmond, WA 98052"))
                .withCompanyDetails(new CompanyDetails().withCompanyName("Contoso Ltd.")
                    .withOfficeAddress("1 Microsoft Way, Redmond, WA 98052")
                    .withCountry("US")
                    .withDomain("contoso.com")
                    .withBusinessType("Technology")
                    .withNumberOfEmployees(28)))
            .apply();
    }
}
```

### SaaSOperationGroup_ActivateResource

```java
import com.azure.resourcemanager.koreaiartemis.models.ActivateSaaSParameterRequest;

/**
 * Samples for SaaSOperationGroup ActivateResource.
 */
public final class SaaSOperationGroupActivateResourceSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/SaaSOperationGroup_ActivateResource.json
     */
    /**
     * Sample code: SaaSOperationGroup_ActivateResource.
     * 
     * @param manager Entry point to KoreAIArtemisManager.
     */
    public static void
        saaSOperationGroupActivateResource(com.azure.resourcemanager.koreaiartemis.KoreAIArtemisManager manager) {
        manager.saaSOperationGroups()
            .activateResource(new ActivateSaaSParameterRequest().withSaasGuid("12345678-1234-1234-1234-123456789012")
                .withPublisherId("kore-ai"), com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_CreateOrUpdate

```java
import com.azure.resourcemanager.koreaiartemis.models.MarketplaceDetails;
import com.azure.resourcemanager.koreaiartemis.models.OfferDetails;
import com.azure.resourcemanager.koreaiartemis.models.Product;
import com.azure.resourcemanager.koreaiartemis.models.WorkspacePartnerProperties;
import com.azure.resourcemanager.koreaiartemis.models.WorkspaceProperties;
import java.util.Arrays;

/**
 * Samples for Workspaces CreateOrUpdate.
 */
public final class WorkspacesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/Workspaces_CreateOrUpdate.json
     */
    /**
     * Sample code: Workspaces_CreateOrUpdate.
     * 
     * @param manager Entry point to KoreAIArtemisManager.
     */
    public static void workspacesCreateOrUpdate(com.azure.resourcemanager.koreaiartemis.KoreAIArtemisManager manager) {
        manager.workspaces()
            .define("contoso-prod")
            .withExistingOrganization("contoso-rg", "contoso-org")
            .withProperties(new WorkspaceProperties().withProduct(Product.AIFORWORK)
                .withMarketplaceDetails(Arrays.asList(new MarketplaceDetails().withSubscriptionId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/contoso-rg/providers/Microsoft.SaaS/resources/kore-ai-saas")
                    .withOfferDetails(new OfferDetails().withPublisherId("kore-ai")
                        .withOfferId("kore-ai-platform")
                        .withPlanId("business-monthly")
                        .withPlanName("Business Monthly")
                        .withTermUnit("P1M")
                        .withTermId("1m"))))
                .withPartnerProperties(new WorkspacePartnerProperties().withName("Contoso Production")
                    .withOwnerEmail("owner@contoso.com")))
            .create();
    }
}
```

### Workspaces_Delete

```java
/**
 * Samples for Workspaces Delete.
 */
public final class WorkspacesDeleteSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/Workspaces_Delete.json
     */
    /**
     * Sample code: Workspaces_Delete.
     * 
     * @param manager Entry point to KoreAIArtemisManager.
     */
    public static void workspacesDelete(com.azure.resourcemanager.koreaiartemis.KoreAIArtemisManager manager) {
        manager.workspaces().delete("contoso-rg", "contoso-org", "contoso-prod", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_Get

```java
/**
 * Samples for Workspaces Get.
 */
public final class WorkspacesGetSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/Workspaces_Get.json
     */
    /**
     * Sample code: Workspaces_Get.
     * 
     * @param manager Entry point to KoreAIArtemisManager.
     */
    public static void workspacesGet(com.azure.resourcemanager.koreaiartemis.KoreAIArtemisManager manager) {
        manager.workspaces()
            .getWithResponse("contoso-rg", "contoso-org", "contoso-prod", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-07-01-preview/Workspaces_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to KoreAIArtemisManager.
     */
    public static void
        workspacesGetMaximumSetGen(com.azure.resourcemanager.koreaiartemis.KoreAIArtemisManager manager) {
        manager.workspaces()
            .getWithResponse("contoso-rg", "contoso-org", "contoso-prod", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_GetDetails

```java
/**
 * Samples for Workspaces GetDetails.
 */
public final class WorkspacesGetDetailsSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/Workspaces_GetDetails.json
     */
    /**
     * Sample code: Workspaces_GetDetails.
     * 
     * @param manager Entry point to KoreAIArtemisManager.
     */
    public static void workspacesGetDetails(com.azure.resourcemanager.koreaiartemis.KoreAIArtemisManager manager) {
        manager.workspaces()
            .getDetailsWithResponse("contoso-rg", "contoso-org", "contoso-prod", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_LinkSaaS

```java
import com.azure.resourcemanager.koreaiartemis.models.WorkspaceSaaSData;
import java.util.Arrays;

/**
 * Samples for Workspaces LinkSaaS.
 */
public final class WorkspacesLinkSaaSSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/Workspaces_LinkSaaS.json
     */
    /**
     * Sample code: Workspaces_LinkSaaS.
     * 
     * @param manager Entry point to KoreAIArtemisManager.
     */
    public static void workspacesLinkSaaS(com.azure.resourcemanager.koreaiartemis.KoreAIArtemisManager manager) {
        manager.workspaces()
            .linkSaaS("contoso-rg", "contoso-org", "contoso-prod",
                new WorkspaceSaaSData().withSaaSResourceIds(Arrays.asList(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/contoso-rg/providers/Microsoft.SaaS/resources/kore-ai-saas-1",
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/contoso-rg/providers/Microsoft.SaaS/resources/kore-ai-saas-2")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_ListByOrganization

```java
/**
 * Samples for Workspaces ListByOrganization.
 */
public final class WorkspacesListByOrganizationSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/Workspaces_ListByOrganization.json
     */
    /**
     * Sample code: Workspaces_ListByOrganization.
     * 
     * @param manager Entry point to KoreAIArtemisManager.
     */
    public static void
        workspacesListByOrganization(com.azure.resourcemanager.koreaiartemis.KoreAIArtemisManager manager) {
        manager.workspaces().listByOrganization("contoso-rg", "contoso-org", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-07-01-preview/Workspaces_ListByOrganization_MaximumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_ListByOrganization_MaximumSet_Gen.
     * 
     * @param manager Entry point to KoreAIArtemisManager.
     */
    public static void workspacesListByOrganizationMaximumSetGen(
        com.azure.resourcemanager.koreaiartemis.KoreAIArtemisManager manager) {
        manager.workspaces().listByOrganization("contoso-rg", "contoso-org", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_ListLinkedSaaS

```java
/**
 * Samples for Workspaces ListLinkedSaaS.
 */
public final class WorkspacesListLinkedSaaSSamples {
    /*
     * x-ms-original-file: 2026-07-01-preview/Workspaces_ListLinkedSaaS.json
     */
    /**
     * Sample code: Workspaces_ListLinkedSaaS.
     * 
     * @param manager Entry point to KoreAIArtemisManager.
     */
    public static void workspacesListLinkedSaaS(com.azure.resourcemanager.koreaiartemis.KoreAIArtemisManager manager) {
        manager.workspaces()
            .listLinkedSaaSWithResponse("contoso-rg", "contoso-org", "contoso-prod", com.azure.core.util.Context.NONE);
    }
}
```

