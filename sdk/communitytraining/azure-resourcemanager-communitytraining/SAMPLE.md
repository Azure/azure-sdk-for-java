# Code snippets and samples


## CommunityTrainings

- [Create](#communitytrainings_create)
- [Delete](#communitytrainings_delete)
- [GetByResourceGroup](#communitytrainings_getbyresourcegroup)
- [List](#communitytrainings_list)
- [ListByResourceGroup](#communitytrainings_listbyresourcegroup)
- [Update](#communitytrainings_update)

## Operations

- [List](#operations_list)
### CommunityTrainings_Create

```java
import com.azure.resourcemanager.communitytraining.models.IdentityConfigurationProperties;
import com.azure.resourcemanager.communitytraining.models.Sku;
import com.azure.resourcemanager.communitytraining.models.SkuTier;

/** Samples for CommunityTrainings Create. */
public final class CommunityTrainingsCreateSamples {
    /*
     * x-ms-original-file: specification/communitytraining/resource-manager/Microsoft.Community/stable/2023-11-01/examples/CommunityTrainings_Create.json
     */
    /**
     * Sample code: CreateCommunityTrainings.
     *
     * @param manager Entry point to CommunitytrainingManager.
     */
    public static void createCommunityTrainings(
        com.azure.resourcemanager.communitytraining.CommunitytrainingManager manager) {
        manager
            .communityTrainings()
            .define("ctApplication")
            .withRegion("southeastasia")
            .withExistingResourceGroup("rgCommunityTaining")
            .withSku(new Sku().withName("Commercial").withTier(SkuTier.STANDARD))
            .withPortalName("ctwebsite")
            .withPortalAdminEmailAddress("ctadmin@ct.com")
            .withPortalOwnerOrganizationName("CT Portal Owner Organization")
            .withPortalOwnerEmailAddress("ctcontact@ct.com")
            .withIdentityConfiguration(
                new IdentityConfigurationProperties()
                    .withIdentityType("ADB2C")
                    .withTeamsEnabled(false)
                    .withTenantId("c1ffbb60-88cf-4b83-b54f-c47ae6220c19")
                    .withDomainName("cttenant")
                    .withClientId("8c92390f-2f30-493d-bd13-d3c3eba3709d")
                    .withClientSecret("fakeTokenPlaceholder")
                    .withB2CAuthenticationPolicy("B2C_1_signup_signin")
                    .withB2CPasswordResetPolicy("fakeTokenPlaceholder")
                    .withCustomLoginParameters("custom_hint"))
            .withZoneRedundancyEnabled(true)
            .withDisasterRecoveryEnabled(true)
            .create();
    }
}
```

### CommunityTrainings_Delete

```java
/** Samples for CommunityTrainings Delete. */
public final class CommunityTrainingsDeleteSamples {
    /*
     * x-ms-original-file: specification/communitytraining/resource-manager/Microsoft.Community/stable/2023-11-01/examples/CommunityTrainings_Delete.json
     */
    /**
     * Sample code: DeleteCommunityTrainings.
     *
     * @param manager Entry point to CommunitytrainingManager.
     */
    public static void deleteCommunityTrainings(
        com.azure.resourcemanager.communitytraining.CommunitytrainingManager manager) {
        manager.communityTrainings().delete("rgCommunityTraining", "ctApplication", com.azure.core.util.Context.NONE);
    }
}
```

### CommunityTrainings_GetByResourceGroup

```java
/** Samples for CommunityTrainings GetByResourceGroup. */
public final class CommunityTrainingsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/communitytraining/resource-manager/Microsoft.Community/stable/2023-11-01/examples/CommunityTrainings_Get.json
     */
    /**
     * Sample code: GetCommunityTrainings.
     *
     * @param manager Entry point to CommunitytrainingManager.
     */
    public static void getCommunityTrainings(
        com.azure.resourcemanager.communitytraining.CommunitytrainingManager manager) {
        manager
            .communityTrainings()
            .getByResourceGroupWithResponse("rgCommunityTraining", "ctApplication", com.azure.core.util.Context.NONE);
    }
}
```

### CommunityTrainings_List

```java
/** Samples for CommunityTrainings List. */
public final class CommunityTrainingsListSamples {
    /*
     * x-ms-original-file: specification/communitytraining/resource-manager/Microsoft.Community/stable/2023-11-01/examples/CommunityTrainings_ListBySubscription.json
     */
    /**
     * Sample code: ListBySubscriptionCommunityTrainings.
     *
     * @param manager Entry point to CommunitytrainingManager.
     */
    public static void listBySubscriptionCommunityTrainings(
        com.azure.resourcemanager.communitytraining.CommunitytrainingManager manager) {
        manager.communityTrainings().list(com.azure.core.util.Context.NONE);
    }
}
```

### CommunityTrainings_ListByResourceGroup

```java
/** Samples for CommunityTrainings ListByResourceGroup. */
public final class CommunityTrainingsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/communitytraining/resource-manager/Microsoft.Community/stable/2023-11-01/examples/CommunityTrainings_ListByResourceGroup.json
     */
    /**
     * Sample code: ListByResourceGroupCommunityTrainings.
     *
     * @param manager Entry point to CommunitytrainingManager.
     */
    public static void listByResourceGroupCommunityTrainings(
        com.azure.resourcemanager.communitytraining.CommunitytrainingManager manager) {
        manager.communityTrainings().listByResourceGroup("rgCommunityTraining", com.azure.core.util.Context.NONE);
    }
}
```

### CommunityTrainings_Update

```java
import com.azure.resourcemanager.communitytraining.models.CommunityTraining;
import com.azure.resourcemanager.communitytraining.models.IdentityConfigurationPropertiesUpdate;
import com.azure.resourcemanager.communitytraining.models.Sku;
import com.azure.resourcemanager.communitytraining.models.SkuTier;
import java.util.HashMap;
import java.util.Map;

/** Samples for CommunityTrainings Update. */
public final class CommunityTrainingsUpdateSamples {
    /*
     * x-ms-original-file: specification/communitytraining/resource-manager/Microsoft.Community/stable/2023-11-01/examples/CommunityTrainings_Update.json
     */
    /**
     * Sample code: UpdateCommunityTrainings.
     *
     * @param manager Entry point to CommunitytrainingManager.
     */
    public static void updateCommunityTrainings(
        com.azure.resourcemanager.communitytraining.CommunitytrainingManager manager) {
        CommunityTraining resource =
            manager
                .communityTrainings()
                .getByResourceGroupWithResponse(
                    "rgCommunityTraining", "ctApplication", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf())
            .withSku(new Sku().withName("Commercial").withTier(SkuTier.STANDARD))
            .withIdentityConfiguration(
                new IdentityConfigurationPropertiesUpdate()
                    .withIdentityType("ADB2C")
                    .withTeamsEnabled(false)
                    .withTenantId("c1ffbb60-88cf-4b83-b54f-c47ae6220c19")
                    .withDomainName("cttenant")
                    .withClientId("8c92390f-2f30-493d-bd13-d3c3eba3709d")
                    .withClientSecret("fakeTokenPlaceholder")
                    .withB2CAuthenticationPolicy("B2C_1_signup_signin")
                    .withB2CPasswordResetPolicy("fakeTokenPlaceholder")
                    .withCustomLoginParameters("custom_hint"))
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
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/communitytraining/resource-manager/Microsoft.Community/stable/2023-11-01/examples/Operations_List.json
     */
    /**
     * Sample code: ListOperations.
     *
     * @param manager Entry point to CommunitytrainingManager.
     */
    public static void listOperations(com.azure.resourcemanager.communitytraining.CommunitytrainingManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

