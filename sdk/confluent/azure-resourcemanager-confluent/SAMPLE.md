# Code snippets and samples


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
### MarketplaceAgreements_Create

```java
import com.azure.core.util.Context;

/** Samples for MarketplaceAgreements Create. */
public final class MarketplaceAgreementsCreateSamples {
    /*
     * x-ms-original-file: specification/confluent/resource-manager/Microsoft.Confluent/stable/2021-12-01/examples/MarketplaceAgreements_Create.json
     */
    /**
     * Sample code: MarketplaceAgreements_Create.
     *
     * @param manager Entry point to ConfluentManager.
     */
    public static void marketplaceAgreementsCreate(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.marketplaceAgreements().createWithResponse(null, Context.NONE);
    }
}
```

### MarketplaceAgreements_List

```java
import com.azure.core.util.Context;

/** Samples for MarketplaceAgreements List. */
public final class MarketplaceAgreementsListSamples {
    /*
     * x-ms-original-file: specification/confluent/resource-manager/Microsoft.Confluent/stable/2021-12-01/examples/MarketplaceAgreements_List.json
     */
    /**
     * Sample code: MarketplaceAgreements_List.
     *
     * @param manager Entry point to ConfluentManager.
     */
    public static void marketplaceAgreementsList(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.marketplaceAgreements().list(Context.NONE);
    }
}
```

### Organization_Create

```java
import com.azure.resourcemanager.confluent.models.OfferDetail;
import com.azure.resourcemanager.confluent.models.UserDetail;
import java.util.HashMap;
import java.util.Map;

/** Samples for Organization Create. */
public final class OrganizationCreateSamples {
    /*
     * x-ms-original-file: specification/confluent/resource-manager/Microsoft.Confluent/stable/2021-12-01/examples/Organization_Create.json
     */
    /**
     * Sample code: Organization_Create.
     *
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationCreate(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager
            .organizations()
            .define("myOrganization")
            .withRegion("West US")
            .withExistingResourceGroup("myResourceGroup")
            .withOfferDetail(
                new OfferDetail()
                    .withPublisherId("string")
                    .withId("string")
                    .withPlanId("string")
                    .withPlanName("string")
                    .withTermUnit("string"))
            .withUserDetail(
                new UserDetail()
                    .withFirstName("string")
                    .withLastName("string")
                    .withEmailAddress("contoso@microsoft.com"))
            .withTags(mapOf("Environment", "Dev"))
            .create();
    }

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
import com.azure.core.util.Context;

/** Samples for Organization Delete. */
public final class OrganizationDeleteSamples {
    /*
     * x-ms-original-file: specification/confluent/resource-manager/Microsoft.Confluent/stable/2021-12-01/examples/Organization_Delete.json
     */
    /**
     * Sample code: Confluent_Delete.
     *
     * @param manager Entry point to ConfluentManager.
     */
    public static void confluentDelete(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations().delete("myResourceGroup", "myOrganization", Context.NONE);
    }
}
```

### Organization_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Organization GetByResourceGroup. */
public final class OrganizationGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/confluent/resource-manager/Microsoft.Confluent/stable/2021-12-01/examples/Organization_Get.json
     */
    /**
     * Sample code: Organization_Get.
     *
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationGet(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations().getByResourceGroupWithResponse("myResourceGroup", "myOrganization", Context.NONE);
    }
}
```

### Organization_List

```java
import com.azure.core.util.Context;

/** Samples for Organization List. */
public final class OrganizationListSamples {
    /*
     * x-ms-original-file: specification/confluent/resource-manager/Microsoft.Confluent/stable/2021-12-01/examples/Organization_ListBySubscription.json
     */
    /**
     * Sample code: Organization_ListBySubscription.
     *
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationListBySubscription(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations().list(Context.NONE);
    }
}
```

### Organization_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Organization ListByResourceGroup. */
public final class OrganizationListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/confluent/resource-manager/Microsoft.Confluent/stable/2021-12-01/examples/Organization_ListByResourceGroup.json
     */
    /**
     * Sample code: Organization_ListByResourceGroup.
     *
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationListByResourceGroup(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizations().listByResourceGroup("myResourceGroup", Context.NONE);
    }
}
```

### Organization_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.confluent.models.OrganizationResource;
import java.util.HashMap;
import java.util.Map;

/** Samples for Organization Update. */
public final class OrganizationUpdateSamples {
    /*
     * x-ms-original-file: specification/confluent/resource-manager/Microsoft.Confluent/stable/2021-12-01/examples/Organization_Update.json
     */
    /**
     * Sample code: Confluent_Update.
     *
     * @param manager Entry point to ConfluentManager.
     */
    public static void confluentUpdate(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        OrganizationResource resource =
            manager
                .organizations()
                .getByResourceGroupWithResponse("myResourceGroup", "myOrganization", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("client", "dev-client", "env", "dev")).apply();
    }

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
import com.azure.core.util.Context;

/** Samples for OrganizationOperations List. */
public final class OrganizationOperationsListSamples {
    /*
     * x-ms-original-file: specification/confluent/resource-manager/Microsoft.Confluent/stable/2021-12-01/examples/OrganizationOperations_List.json
     */
    /**
     * Sample code: OrganizationOperations_List.
     *
     * @param manager Entry point to ConfluentManager.
     */
    public static void organizationOperationsList(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager.organizationOperations().list(Context.NONE);
    }
}
```

### Validations_ValidateOrganization

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.confluent.fluent.models.OrganizationResourceInner;
import com.azure.resourcemanager.confluent.models.OfferDetail;
import com.azure.resourcemanager.confluent.models.UserDetail;
import java.util.HashMap;
import java.util.Map;

/** Samples for Validations ValidateOrganization. */
public final class ValidationsValidateOrganizationSamples {
    /*
     * x-ms-original-file: specification/confluent/resource-manager/Microsoft.Confluent/stable/2021-12-01/examples/Validations_ValidateOrganizations.json
     */
    /**
     * Sample code: Validations_ValidateOrganizations.
     *
     * @param manager Entry point to ConfluentManager.
     */
    public static void validationsValidateOrganizations(com.azure.resourcemanager.confluent.ConfluentManager manager) {
        manager
            .validations()
            .validateOrganizationWithResponse(
                "myResourceGroup",
                "myOrganization",
                new OrganizationResourceInner()
                    .withLocation("West US")
                    .withTags(mapOf("Environment", "Dev"))
                    .withOfferDetail(
                        new OfferDetail()
                            .withPublisherId("string")
                            .withId("string")
                            .withPlanId("string")
                            .withPlanName("string")
                            .withTermUnit("string"))
                    .withUserDetail(
                        new UserDetail()
                            .withFirstName("string")
                            .withLastName("string")
                            .withEmailAddress("abc@microsoft.com")),
                Context.NONE);
    }

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

