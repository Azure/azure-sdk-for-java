# Code snippets and samples


## Clusters

- [CreateOrUpdate](#clusters_createorupdate)
- [Delete](#clusters_delete)
- [Get](#clusters_get)
- [List](#clusters_list)

## Operations

- [List](#operations_list)

## Organizations

- [CreateOrUpdate](#organizations_createorupdate)
- [Delete](#organizations_delete)
- [GetByResourceGroup](#organizations_getbyresourcegroup)
- [List](#organizations_list)
- [ListByResourceGroup](#organizations_listbyresourcegroup)
- [Update](#organizations_update)

## Projects

- [CreateOrUpdate](#projects_createorupdate)
- [Delete](#projects_delete)
- [Get](#projects_get)
- [List](#projects_list)
- [ListClusterTierRegions](#projects_listclustertierregions)
- [TierLimitReached](#projects_tierlimitreached)
### Clusters_CreateOrUpdate

```java
import com.azure.resourcemanager.mongodbatlas.models.ClusterProperties;
import com.azure.resourcemanager.mongodbatlas.models.ClusterTier;

/**
 * Samples for Clusters CreateOrUpdate.
 */
public final class ClustersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/Clusters_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Clusters_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to MongoDBAtlasManager.
     */
    public static void
        clustersCreateOrUpdateMaximumSet(com.azure.resourcemanager.mongodbatlas.MongoDBAtlasManager manager) {
        manager.clusters()
            .define("myCluster")
            .withExistingProject("rgopenapi", "myOrganization", "myProject")
            .withProperties(new ClusterProperties().withClusterTier(ClusterTier.FREE).withRegionName("eastus"))
            .create();
    }
}
```

### Clusters_Delete

```java
/**
 * Samples for Clusters Delete.
 */
public final class ClustersDeleteSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/Clusters_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Clusters_Delete_MaximumSet.
     * 
     * @param manager Entry point to MongoDBAtlasManager.
     */
    public static void clustersDeleteMaximumSet(com.azure.resourcemanager.mongodbatlas.MongoDBAtlasManager manager) {
        manager.clusters()
            .delete("rgopenapi", "myOrganization", "myProject", "myCluster", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_Get

```java
/**
 * Samples for Clusters Get.
 */
public final class ClustersGetSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/Clusters_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Clusters_Get_MaximumSet.
     * 
     * @param manager Entry point to MongoDBAtlasManager.
     */
    public static void clustersGetMaximumSet(com.azure.resourcemanager.mongodbatlas.MongoDBAtlasManager manager) {
        manager.clusters()
            .getWithResponse("rgopenapi", "myOrganization", "myProject", "myCluster", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_List

```java
/**
 * Samples for Clusters List.
 */
public final class ClustersListSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/Clusters_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Clusters_List_MaximumSet.
     * 
     * @param manager Entry point to MongoDBAtlasManager.
     */
    public static void clustersListMaximumSet(com.azure.resourcemanager.mongodbatlas.MongoDBAtlasManager manager) {
        manager.clusters().list("rgopenapi", "myOrganization", "myProject", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-03-01-preview/Operations_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MinimumSet.
     * 
     * @param manager Entry point to MongoDBAtlasManager.
     */
    public static void operationsListMinimumSet(com.azure.resourcemanager.mongodbatlas.MongoDBAtlasManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-03-01-preview/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MaximumSet.
     * 
     * @param manager Entry point to MongoDBAtlasManager.
     */
    public static void operationsListMaximumSet(com.azure.resourcemanager.mongodbatlas.MongoDBAtlasManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Organizations_CreateOrUpdate

```java
import com.azure.resourcemanager.mongodbatlas.models.ManagedServiceIdentity;
import com.azure.resourcemanager.mongodbatlas.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.mongodbatlas.models.MarketplaceDetails;
import com.azure.resourcemanager.mongodbatlas.models.OfferDetails;
import com.azure.resourcemanager.mongodbatlas.models.OrganizationProperties;
import com.azure.resourcemanager.mongodbatlas.models.PartnerProperties;
import com.azure.resourcemanager.mongodbatlas.models.UserDetails;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Organizations CreateOrUpdate.
 */
public final class OrganizationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/Organizations_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to MongoDBAtlasManager.
     */
    public static void
        organizationsCreateOrUpdateMaximumSet(com.azure.resourcemanager.mongodbatlas.MongoDBAtlasManager manager) {
        manager.organizations()
            .define("U.1-:7")
            .withRegion("wobqn")
            .withExistingResourceGroup("rgopenapi")
            .withTags(mapOf())
            .withProperties(new OrganizationProperties()
                .withMarketplace(new MarketplaceDetails().withSubscriptionId("o")
                    .withOfferDetails(new OfferDetails().withPublisherId("rxglearenxsgpwzlsxmiicynks")
                        .withOfferId("ohnquleylybvjrtnpjupvwlk")
                        .withPlanId("obhxnhvrtbcnoovgofbs")
                        .withPlanName("lkwdzpfhvjezjusrqzyftcikxdt")
                        .withTermUnit("omkxrnburbnruglwqgjlahvjmbfcse")
                        .withTermId("bqmmltwmtpdcdeszbka")))
                .withUser(new UserDetails().withFirstName("aslybvdwwddqxwazxvxhjrs")
                    .withLastName("cnuitqoqpcyvmuqowgnxpwxjcveyr")
                    .withEmailAddress(".K_@e7N-g1.xjqnbPs")
                    .withUpn("howdzmfy")
                    .withPhoneNumber("ilypntsrbmbbbexbasuu")
                    .withCompanyName("oxdcwwl"))
                .withPartnerProperties(new PartnerProperties().withOrganizationId("lyombjlhvwxithkiy")
                    .withRedirectUrl("cbxwtehraetlluocdihfgchvjzockn")
                    .withOrganizationName("U.1-:7")))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.NONE)
                .withUserAssignedIdentities(mapOf()))
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
     * x-ms-original-file: 2026-03-01-preview/Organizations_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_Delete_MaximumSet.
     * 
     * @param manager Entry point to MongoDBAtlasManager.
     */
    public static void
        organizationsDeleteMaximumSet(com.azure.resourcemanager.mongodbatlas.MongoDBAtlasManager manager) {
        manager.organizations().delete("rgopenapi", "U.1-:7", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-03-01-preview/Organizations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_Get_MaximumSet.
     * 
     * @param manager Entry point to MongoDBAtlasManager.
     */
    public static void organizationsGetMaximumSet(com.azure.resourcemanager.mongodbatlas.MongoDBAtlasManager manager) {
        manager.organizations().getByResourceGroupWithResponse("rgopenapi", "U.1-:7", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-03-01-preview/Organizations_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to MongoDBAtlasManager.
     */
    public static void
        organizationsListBySubscriptionMaximumSet(com.azure.resourcemanager.mongodbatlas.MongoDBAtlasManager manager) {
        manager.organizations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-03-01-preview/Organizations_ListBySubscription_MinimumSet_Gen.json
     */
    /**
     * Sample code: Organizations_ListBySubscription_MaximumSet - generated by [MinimumSet] rule.
     * 
     * @param manager Entry point to MongoDBAtlasManager.
     */
    public static void organizationsListBySubscriptionMaximumSetGeneratedByMinimumSetRule(
        com.azure.resourcemanager.mongodbatlas.MongoDBAtlasManager manager) {
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
     * x-ms-original-file: 2026-03-01-preview/Organizations_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to MongoDBAtlasManager.
     */
    public static void
        organizationsListByResourceGroupMaximumSet(com.azure.resourcemanager.mongodbatlas.MongoDBAtlasManager manager) {
        manager.organizations().listByResourceGroup("rgopenapi", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-03-01-preview/Organizations_ListByResourceGroup_MinimumSet_Gen.json
     */
    /**
     * Sample code: Organizations_ListByResourceGroup_MaximumSet - generated by [MinimumSet] rule.
     * 
     * @param manager Entry point to MongoDBAtlasManager.
     */
    public static void organizationsListByResourceGroupMaximumSetGeneratedByMinimumSetRule(
        com.azure.resourcemanager.mongodbatlas.MongoDBAtlasManager manager) {
        manager.organizations().listByResourceGroup("rgopenapi", com.azure.core.util.Context.NONE);
    }
}
```

### Organizations_Update

```java
import com.azure.resourcemanager.mongodbatlas.models.ManagedServiceIdentity;
import com.azure.resourcemanager.mongodbatlas.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.mongodbatlas.models.OrganizationResource;
import com.azure.resourcemanager.mongodbatlas.models.OrganizationResourceUpdateProperties;
import com.azure.resourcemanager.mongodbatlas.models.PartnerProperties;
import com.azure.resourcemanager.mongodbatlas.models.UserDetails;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Organizations Update.
 */
public final class OrganizationsUpdateSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/Organizations_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_Update_MaximumSet.
     * 
     * @param manager Entry point to MongoDBAtlasManager.
     */
    public static void
        organizationsUpdateMaximumSet(com.azure.resourcemanager.mongodbatlas.MongoDBAtlasManager manager) {
        OrganizationResource resource = manager.organizations()
            .getByResourceGroupWithResponse("rgopenapi", "U.1-:7", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf())
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.NONE)
                .withUserAssignedIdentities(mapOf()))
            .withProperties(new OrganizationResourceUpdateProperties()
                .withUser(new UserDetails().withFirstName("btyhwmlbzzihjfimviefebg")
                    .withLastName("xx")
                    .withEmailAddress(".K_@e7N-g1.xjqnbPs")
                    .withUpn("mxtbogd")
                    .withPhoneNumber("isvc")
                    .withCompanyName("oztteysco"))
                .withPartnerProperties(new PartnerProperties().withOrganizationId("vugtqrobendjkinziswxlqueouo")
                    .withRedirectUrl("cbxwtehraetlluocdihfgchvjzockn")
                    .withOrganizationName("U.1-:7")))
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

### Projects_CreateOrUpdate

```java
import com.azure.resourcemanager.mongodbatlas.models.ProjectProperties;

/**
 * Samples for Projects CreateOrUpdate.
 */
public final class ProjectsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/Projects_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Projects_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to MongoDBAtlasManager.
     */
    public static void
        projectsCreateOrUpdateMaximumSet(com.azure.resourcemanager.mongodbatlas.MongoDBAtlasManager manager) {
        manager.projects()
            .define("myProject")
            .withExistingOrganization("rgopenapi", "myOrganization")
            .withProperties(new ProjectProperties())
            .create();
    }
}
```

### Projects_Delete

```java
/**
 * Samples for Projects Delete.
 */
public final class ProjectsDeleteSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/Projects_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Projects_Delete_MaximumSet.
     * 
     * @param manager Entry point to MongoDBAtlasManager.
     */
    public static void projectsDeleteMaximumSet(com.azure.resourcemanager.mongodbatlas.MongoDBAtlasManager manager) {
        manager.projects().delete("rgopenapi", "myOrganization", "myProject", com.azure.core.util.Context.NONE);
    }
}
```

### Projects_Get

```java
/**
 * Samples for Projects Get.
 */
public final class ProjectsGetSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/Projects_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Projects_Get_MaximumSet.
     * 
     * @param manager Entry point to MongoDBAtlasManager.
     */
    public static void projectsGetMaximumSet(com.azure.resourcemanager.mongodbatlas.MongoDBAtlasManager manager) {
        manager.projects()
            .getWithResponse("rgopenapi", "myOrganization", "myProject", com.azure.core.util.Context.NONE);
    }
}
```

### Projects_List

```java
/**
 * Samples for Projects List.
 */
public final class ProjectsListSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/Projects_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Projects_List_MaximumSet.
     * 
     * @param manager Entry point to MongoDBAtlasManager.
     */
    public static void projectsListMaximumSet(com.azure.resourcemanager.mongodbatlas.MongoDBAtlasManager manager) {
        manager.projects().list("rgopenapi", "myOrganization", com.azure.core.util.Context.NONE);
    }
}
```

### Projects_ListClusterTierRegions

```java
/**
 * Samples for Projects ListClusterTierRegions.
 */
public final class ProjectsListClusterTierRegionsSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/Projects_ListClusterTierRegions_MaximumSet_Gen.json
     */
    /**
     * Sample code: Projects_ListClusterTierRegions_MaximumSet.
     * 
     * @param manager Entry point to MongoDBAtlasManager.
     */
    public static void
        projectsListClusterTierRegionsMaximumSet(com.azure.resourcemanager.mongodbatlas.MongoDBAtlasManager manager) {
        manager.projects()
            .listClusterTierRegionsWithResponse("rgopenapi", "myOrganization", "myProject",
                com.azure.core.util.Context.NONE);
    }
}
```

### Projects_TierLimitReached

```java
/**
 * Samples for Projects TierLimitReached.
 */
public final class ProjectsTierLimitReachedSamples {
    /*
     * x-ms-original-file: 2026-03-01-preview/Projects_TierLimitReached_MaximumSet_Gen.json
     */
    /**
     * Sample code: Projects_TierLimitReached_MaximumSet.
     * 
     * @param manager Entry point to MongoDBAtlasManager.
     */
    public static void
        projectsTierLimitReachedMaximumSet(com.azure.resourcemanager.mongodbatlas.MongoDBAtlasManager manager) {
        manager.projects()
            .tierLimitReachedWithResponse("rgopenapi", "myOrganization", "myProject", com.azure.core.util.Context.NONE);
    }
}
```

