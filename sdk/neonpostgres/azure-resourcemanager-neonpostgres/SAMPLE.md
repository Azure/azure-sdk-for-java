# Code snippets and samples


## Branches

- [CreateOrUpdate](#branches_createorupdate)
- [Delete](#branches_delete)
- [Get](#branches_get)
- [List](#branches_list)
- [Preflight](#branches_preflight)

## Computes

- [List](#computes_list)

## Endpoints

- [CreateOrUpdate](#endpoints_createorupdate)
- [Delete](#endpoints_delete)
- [List](#endpoints_list)

## NeonDatabases

- [CreateOrUpdate](#neondatabases_createorupdate)
- [Delete](#neondatabases_delete)
- [List](#neondatabases_list)

## NeonRoles

- [CreateOrUpdate](#neonroles_createorupdate)
- [Delete](#neonroles_delete)
- [List](#neonroles_list)

## Operations

- [List](#operations_list)

## Organizations

- [CreateOrUpdate](#organizations_createorupdate)
- [Delete](#organizations_delete)
- [GetByResourceGroup](#organizations_getbyresourcegroup)
- [GetPostgresVersions](#organizations_getpostgresversions)
- [List](#organizations_list)
- [ListByResourceGroup](#organizations_listbyresourcegroup)
- [Update](#organizations_update)

## Projects

- [CreateOrUpdate](#projects_createorupdate)
- [Delete](#projects_delete)
- [Get](#projects_get)
- [GetConnectionUri](#projects_getconnectionuri)
- [List](#projects_list)
### Branches_CreateOrUpdate

```java
import com.azure.resourcemanager.neonpostgres.models.Attributes;
import com.azure.resourcemanager.neonpostgres.models.AutoscalingSize;
import com.azure.resourcemanager.neonpostgres.models.BranchProperties;
import com.azure.resourcemanager.neonpostgres.models.EndpointProperties;
import com.azure.resourcemanager.neonpostgres.models.EndpointType;
import com.azure.resourcemanager.neonpostgres.models.NeonDatabaseProperties;
import com.azure.resourcemanager.neonpostgres.models.NeonRoleProperties;
import java.util.Arrays;

/**
 * Samples for Branches CreateOrUpdate.
 */
public final class BranchesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-23-preview/Branches_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Branches_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void
        branchesCreateOrUpdateMaximumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.branches()
            .define("feature")
            .withExistingProject("rgneon", "myOrganization", "myProject")
            .withProperties(new BranchProperties().withEntityName("FeatureBranch")
                .withAttributes(Arrays.asList(new Attributes().withName("on").withValue("qzp")))
                .withProjectId("cxhihpayn")
                .withParentId("parent-123abc")
                .withRoleName("lwlafskrxvggwnfu")
                .withDatabaseName("zxqetv")
                .withRoles(Arrays.asList(new NeonRoleProperties().withEntityName("AdminRole")
                    .withAttributes(Arrays.asList(new Attributes().withName("on").withValue("qzp")))
                    .withBranchId("tnmwjbftrvfpepgeytoeqsyhyz")
                    .withPermissions(Arrays.asList("cgubrzxkomlxoqdua"))
                    .withIsSuperUser(true)))
                .withDatabases(Arrays.asList(new NeonDatabaseProperties().withEntityName("MainDatabase")
                    .withAttributes(Arrays.asList(new Attributes().withName("on").withValue("qzp")))
                    .withBranchId("sllrohrmwkgzre")
                    .withOwnerName("rjpysakvuicrlwvzcbmp")))
                .withEndpoints(Arrays.asList(new EndpointProperties().withEntityName("PrimaryEndpoint")
                    .withAttributes(Arrays.asList(new Attributes().withName("on").withValue("qzp")))
                    .withProjectId("vwwhykqyr")
                    .withBranchId("blclbeuzvywzagbuvdo")
                    .withEndpointType(EndpointType.READ_ONLY)
                    .withSize(new AutoscalingSize().withAutoscalingLimitMinCu(3.0).withAutoscalingLimitMaxCu(14.0)))))
            .create();
    }
}
```

### Branches_Delete

```java
/**
 * Samples for Branches Delete.
 */
public final class BranchesDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-23-preview/Branches_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Branches_Delete_MaximumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void branchesDeleteMaximumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.branches()
            .deleteWithResponse("rgneon", "myOrganization", "myProject", "feature", com.azure.core.util.Context.NONE);
    }
}
```

### Branches_Get

```java
/**
 * Samples for Branches Get.
 */
public final class BranchesGetSamples {
    /*
     * x-ms-original-file: 2025-06-23-preview/Branches_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Branches_Get_MaximumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void branchesGetMaximumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.branches()
            .getWithResponse("rgneon", "myOrganization", "myProject", "feature", com.azure.core.util.Context.NONE);
    }
}
```

### Branches_List

```java
/**
 * Samples for Branches List.
 */
public final class BranchesListSamples {
    /*
     * x-ms-original-file: 2025-06-23-preview/Branches_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Branches_List_MaximumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void branchesListMaximumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.branches().list("rgneon", "myOrganization", "myProject", com.azure.core.util.Context.NONE);
    }
}
```

### Branches_Preflight

```java
import com.azure.resourcemanager.neonpostgres.models.BranchProperties;
import com.azure.resourcemanager.neonpostgres.models.EndpointProperties;
import com.azure.resourcemanager.neonpostgres.models.EndpointType;
import com.azure.resourcemanager.neonpostgres.models.EntityType;
import com.azure.resourcemanager.neonpostgres.models.NeonDatabaseProperties;
import com.azure.resourcemanager.neonpostgres.models.PreflightCheckParameters;

/**
 * Samples for Branches Preflight.
 */
public final class BranchesPreflightSamples {
    /*
     * x-ms-original-file: 2025-06-23-preview/Branches_Preflight_MaximumSet_Gen.json
     */
    /**
     * Sample code: Branches_Preflight_MaximumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void branchesPreflightMaximumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.branches()
            .preflightWithResponse("rgneon", "myOrganization", "myProject", "myBranch",
                new PreflightCheckParameters().withProjectId("project-123")
                    .withBranchId("branch-123")
                    .withEntityType(EntityType.BRANCH)
                    .withBranchProperties(new BranchProperties().withProjectId("project-123")
                        .withRoleName("admin")
                        .withDatabaseName("application")
                        .withBranchId("branch-123")
                        .withBranch("myBranch")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-06-23-preview/Branches_Preflight_Endpoint_Gen.json
     */
    /**
     * Sample code: Branches_Preflight_Endpoint_Gen.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void
        branchesPreflightEndpointGen(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.branches()
            .preflightWithResponse("rgneon", "myOrganization", "myProject", "myBranch",
                new PreflightCheckParameters().withProjectId("project-123")
                    .withBranchId("branch-123")
                    .withEntityType(EntityType.ENDPOINT)
                    .withEndpointProperties(new EndpointProperties().withEndpointType(EndpointType.READ_WRITE)
                        .withEndpointId("endpoint-456")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-06-23-preview/Branches_Preflight_Database_Gen.json
     */
    /**
     * Sample code: Branches_Preflight_Database_Gen.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void
        branchesPreflightDatabaseGen(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.branches()
            .preflightWithResponse("rgneon", "myOrganization", "myProject", "myBranch",
                new PreflightCheckParameters().withProjectId("project-123")
                    .withBranchId("branch-123")
                    .withEntityType(EntityType.fromString("database"))
                    .withDatabaseProperties(new NeonDatabaseProperties().withDatabaseName("analytics")),
                com.azure.core.util.Context.NONE);
    }
}
```

### Computes_List

```java
/**
 * Samples for Computes List.
 */
public final class ComputesListSamples {
    /*
     * x-ms-original-file: 2025-06-23-preview/Computes_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Computes_List_MaximumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void computesListMaximumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.computes().list("rgneon", "myOrganization", "myProject", "feature", com.azure.core.util.Context.NONE);
    }
}
```

### Endpoints_CreateOrUpdate

```java
import com.azure.resourcemanager.neonpostgres.models.Attributes;
import com.azure.resourcemanager.neonpostgres.models.AutoscalingSize;
import com.azure.resourcemanager.neonpostgres.models.EndpointProperties;
import com.azure.resourcemanager.neonpostgres.models.EndpointType;
import java.util.Arrays;

/**
 * Samples for Endpoints CreateOrUpdate.
 */
public final class EndpointsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-23-preview/Endpoints_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Endpoints_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void
        endpointsCreateOrUpdateMaximumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.endpoints()
            .define("primary-endpoint")
            .withExistingBranche("rgneon", "myOrganization", "myProject", "feature")
            .withProperties(new EndpointProperties().withEntityName("PrimaryEndpoint")
                .withAttributes(Arrays.asList(new Attributes().withName("on").withValue("qzp")))
                .withProjectId("vwwhykqyr")
                .withBranchId("blclbeuzvywzagbuvdo")
                .withEndpointType(EndpointType.READ_ONLY)
                .withSize(new AutoscalingSize().withAutoscalingLimitMinCu(3.0).withAutoscalingLimitMaxCu(14.0)))
            .create();
    }
}
```

### Endpoints_Delete

```java
/**
 * Samples for Endpoints Delete.
 */
public final class EndpointsDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-23-preview/Endpoints_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Endpoints_Delete_MaximumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void endpointsDeleteMaximumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.endpoints()
            .deleteWithResponse("rgneon", "myNeonOrg", "myProject", "main", "myEndpoint",
                com.azure.core.util.Context.NONE);
    }
}
```

### Endpoints_List

```java
/**
 * Samples for Endpoints List.
 */
public final class EndpointsListSamples {
    /*
     * x-ms-original-file: 2025-06-23-preview/Endpoints_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Endpoints_List_MaximumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void endpointsListMaximumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.endpoints().list("rgneon", "myOrganization", "myProject", "feature", com.azure.core.util.Context.NONE);
    }
}
```

### NeonDatabases_CreateOrUpdate

```java
import com.azure.resourcemanager.neonpostgres.models.Attributes;
import com.azure.resourcemanager.neonpostgres.models.NeonDatabaseProperties;
import java.util.Arrays;

/**
 * Samples for NeonDatabases CreateOrUpdate.
 */
public final class NeonDatabasesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-23-preview/NeonDatabases_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: NeonDatabases_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void
        neonDatabasesCreateOrUpdateMaximumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.neonDatabases()
            .define("postgres_main_db")
            .withExistingBranche("rgneon", "myOrganization", "myProject", "feature")
            .withProperties(new NeonDatabaseProperties().withEntityName("MainDatabase")
                .withAttributes(Arrays.asList(new Attributes().withName("on").withValue("qzp")))
                .withBranchId("sllrohrmwkgzre")
                .withOwnerName("rjpysakvuicrlwvzcbmp"))
            .create();
    }
}
```

### NeonDatabases_Delete

```java
/**
 * Samples for NeonDatabases Delete.
 */
public final class NeonDatabasesDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-23-preview/NeonDatabases_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: NeonDatabases_Delete_MaximumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void
        neonDatabasesDeleteMaximumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.neonDatabases()
            .deleteWithResponse("rgneon", "myOrganization", "myProject", "feature", "postgres_main_db",
                com.azure.core.util.Context.NONE);
    }
}
```

### NeonDatabases_List

```java
/**
 * Samples for NeonDatabases List.
 */
public final class NeonDatabasesListSamples {
    /*
     * x-ms-original-file: 2025-06-23-preview/NeonDatabases_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: NeonDatabases_List_MaximumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void neonDatabasesListMaximumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.neonDatabases().list("rgneon", "myOrganization", "myProject", "main", com.azure.core.util.Context.NONE);
    }
}
```

### NeonRoles_CreateOrUpdate

```java
import com.azure.resourcemanager.neonpostgres.models.Attributes;
import com.azure.resourcemanager.neonpostgres.models.NeonRoleProperties;
import java.util.Arrays;

/**
 * Samples for NeonRoles CreateOrUpdate.
 */
public final class NeonRolesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-23-preview/NeonRoles_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: NeonRoles_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void
        neonRolesCreateOrUpdateMaximumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.neonRoles()
            .define("read_only_role")
            .withExistingBranche("rgneon", "myOrganization", "myProject", "feature")
            .withProperties(new NeonRoleProperties().withEntityName("PostgresReadOnlyRole")
                .withAttributes(Arrays.asList(new Attributes().withName("on").withValue("qzp")))
                .withBranchId("tnmwjbftrvfpepgeytoeqsyhyz")
                .withPermissions(Arrays.asList("cgubrzxkomlxoqdua"))
                .withIsSuperUser(true))
            .create();
    }
}
```

### NeonRoles_Delete

```java
/**
 * Samples for NeonRoles Delete.
 */
public final class NeonRolesDeleteSamples {
    /*
     * x-ms-original-file: 2025-06-23-preview/NeonRoles_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: NeonRoles_Delete_MaximumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void neonRolesDeleteMaximumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.neonRoles()
            .deleteWithResponse("rgneon", "myOrganization", "myProject", "feature", "read_only_role",
                com.azure.core.util.Context.NONE);
    }
}
```

### NeonRoles_List

```java
/**
 * Samples for NeonRoles List.
 */
public final class NeonRolesListSamples {
    /*
     * x-ms-original-file: 2025-06-23-preview/NeonRoles_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: NeonRoles_List_MaximumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void neonRolesListMaximumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.neonRoles().list("rgneon", "myOrganization", "myProject", "feature", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-06-23-preview/Operations_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MinimumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void operationsListMinimumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-06-23-preview/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MaximumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void operationsListMaximumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Organizations_CreateOrUpdate

```java
import com.azure.resourcemanager.neonpostgres.models.Attributes;
import com.azure.resourcemanager.neonpostgres.models.AutoscalingSize;
import com.azure.resourcemanager.neonpostgres.models.BranchProperties;
import com.azure.resourcemanager.neonpostgres.models.CompanyDetails;
import com.azure.resourcemanager.neonpostgres.models.DefaultEndpointSettings;
import com.azure.resourcemanager.neonpostgres.models.EndpointProperties;
import com.azure.resourcemanager.neonpostgres.models.EndpointType;
import com.azure.resourcemanager.neonpostgres.models.MarketplaceDetails;
import com.azure.resourcemanager.neonpostgres.models.MarketplaceSubscriptionStatus;
import com.azure.resourcemanager.neonpostgres.models.NeonDatabaseProperties;
import com.azure.resourcemanager.neonpostgres.models.NeonRoleProperties;
import com.azure.resourcemanager.neonpostgres.models.OfferDetails;
import com.azure.resourcemanager.neonpostgres.models.OrganizationProperties;
import com.azure.resourcemanager.neonpostgres.models.PartnerOrganizationProperties;
import com.azure.resourcemanager.neonpostgres.models.ProjectProperties;
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
     * x-ms-original-file: 2025-06-23-preview/Organizations_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void
        organizationsCreateOrUpdateMaximumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.organizations()
            .define("myOrganization")
            .withRegion("westus")
            .withExistingResourceGroup("rgneon")
            .withTags(mapOf("environment", "development"))
            .withProperties(
                new OrganizationProperties()
                    .withMarketplaceDetails(
                        new MarketplaceDetails().withSubscriptionId("DFF26289-4E9C-46D0-890E-F8BE27BDA8C2")
                            .withSubscriptionStatus(MarketplaceSubscriptionStatus.PENDING_FULFILLMENT_START)
                            .withOfferDetails(new OfferDetails().withPublisherId("neon-tech")
                                .withOfferId("neon-postgres")
                                .withPlanId("standard")
                                .withPlanName("Standard")
                                .withTermUnit("Monthly")
                                .withTermId("hjk5")))
                    .withUserDetails(new UserDetails().withFirstName("John")
                        .withLastName("Doe")
                        .withEmailAddress("john.doe@example.com")
                        .withUpn("john.doe@example.com")
                        .withPhoneNumber("555-123-4567"))
                    .withCompanyDetails(new CompanyDetails().withCompanyName("Contoso Ltd")
                        .withCountry("United States")
                        .withOfficeAddress("123 Main Street, Seattle, WA 98101")
                        .withBusinessPhone("555-987-6543")
                        .withDomain("contoso.com")
                        .withNumberOfEmployees(30L))
                    .withPartnerOrganizationProperties(
                        new PartnerOrganizationProperties().withOrganizationId("12a34b56-7c89-0d12-e34f-g56h7i8j9k0l")
                            .withOrganizationName("Contoso")
                            .withSingleSignOnProperties(new SingleSignOnProperties()
                                .withSingleSignOnState(SingleSignOnStates.INITIAL)
                                .withEnterpriseAppId("98f76e54-3d21-0c9b-a87f-6e5d4c3b2a10")
                                .withSingleSignOnUrl("https://login.microsoftonline.com/")
                                .withAadDomains(Arrays.asList("contoso.com"))))
                    .withProjectProperties(new ProjectProperties().withEntityName("myProject")
                        .withAttributes(Arrays.asList(new Attributes().withName("environment").withValue("dev")))
                        .withRegionId("westus")
                        .withStorage(22L)
                        .withPgVersion(14)
                        .withHistoryRetention(3)
                        .withDefaultEndpointSettings(new DefaultEndpointSettings().withAutoscalingLimitMinCu(3.0)
                            .withAutoscalingLimitMaxCu(28.0))
                        .withBranch(new BranchProperties().withEntityName("feature")
                            .withAttributes(Arrays.asList(new Attributes().withName("environment").withValue("dev")))
                            .withProjectId("project-123")
                            .withParentId("main-branch")
                            .withRoles(Arrays.asList(new NeonRoleProperties().withEntityName("read_only_role")
                                .withAttributes(
                                    Arrays.asList(new Attributes().withName("environment").withValue("dev")))
                                .withBranchId("branch-123")
                                .withPermissions(Arrays.asList("SELECT"))
                                .withIsSuperUser(true)))
                            .withDatabases(Arrays.asList(new NeonDatabaseProperties().withEntityName("appdb")
                                .withAttributes(
                                    Arrays.asList(new Attributes().withName("environment").withValue("dev")))
                                .withBranchId("branch-123")
                                .withOwnerName("postgres")))
                            .withEndpoints(Arrays.asList(new EndpointProperties().withEntityName("read-endpoint")
                                .withAttributes(
                                    Arrays.asList(new Attributes().withName("environment").withValue("dev")))
                                .withProjectId("project-123")
                                .withBranchId("branch-123")
                                .withEndpointType(EndpointType.READ_ONLY)
                                .withSize(new AutoscalingSize().withAutoscalingLimitMinCu(3.0)
                                    .withAutoscalingLimitMaxCu(14.0)))))
                        .withRoles(Arrays.asList(new NeonRoleProperties().withEntityName("admin_role")
                            .withAttributes(Arrays.asList(new Attributes().withName("environment").withValue("dev")))
                            .withBranchId("branch-123")
                            .withPermissions(Arrays.asList("ALL"))
                            .withIsSuperUser(true)))
                        .withDatabases(Arrays.asList(new NeonDatabaseProperties().withEntityName("maindb")
                            .withAttributes(Arrays.asList(new Attributes().withName("environment").withValue("dev")))
                            .withBranchId("branch-123")
                            .withOwnerName("postgres")))
                        .withEndpoints(Arrays.asList(new EndpointProperties().withEntityName("primary-endpoint")
                            .withAttributes(Arrays.asList(new Attributes().withName("environment").withValue("dev")))
                            .withProjectId("project-123")
                            .withBranchId("branch-123")
                            .withEndpointType(EndpointType.READ_ONLY)
                            .withSize(new AutoscalingSize().withAutoscalingLimitMinCu(3.0)
                                .withAutoscalingLimitMaxCu(14.0))))))
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
     * x-ms-original-file: 2025-06-23-preview/Organizations_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_Delete_MaximumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void
        organizationsDeleteMaximumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.organizations().delete("rgneon", "myOrganization", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-06-23-preview/Organizations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_Get_MaximumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void organizationsGetMaximumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.organizations()
            .getByResourceGroupWithResponse("rgneon", "myOrganization", com.azure.core.util.Context.NONE);
    }
}
```

### Organizations_GetPostgresVersions

```java
import com.azure.resourcemanager.neonpostgres.models.PgVersion;

/**
 * Samples for Organizations GetPostgresVersions.
 */
public final class OrganizationsGetPostgresVersionsSamples {
    /*
     * x-ms-original-file: 2025-06-23-preview/Organizations_GetPostgresVersions_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_GetPostgresVersions_MaximumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void
        organizationsGetPostgresVersionsMaximumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.organizations()
            .getPostgresVersionsWithResponse("rgneon", new PgVersion().withVersion(25),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-06-23-preview/Organizations_GetPostgresVersions_MinimumSet_Gen.json
     */
    /**
     * Sample code: Organizations_GetPostgresVersions_MinimumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void
        organizationsGetPostgresVersionsMinimumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.organizations().getPostgresVersionsWithResponse("rgneon", null, com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-06-23-preview/Organizations_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_ListBySubscription_MaximumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void
        organizationsListBySubscriptionMaximumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
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
     * x-ms-original-file: 2025-06-23-preview/Organizations_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_ListByResourceGroup_MaximumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void
        organizationsListByResourceGroupMaximumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.organizations().listByResourceGroup("rgneon", com.azure.core.util.Context.NONE);
    }
}
```

### Organizations_Update

```java
import com.azure.resourcemanager.neonpostgres.models.Attributes;
import com.azure.resourcemanager.neonpostgres.models.AutoscalingSize;
import com.azure.resourcemanager.neonpostgres.models.BranchProperties;
import com.azure.resourcemanager.neonpostgres.models.CompanyDetails;
import com.azure.resourcemanager.neonpostgres.models.DefaultEndpointSettings;
import com.azure.resourcemanager.neonpostgres.models.EndpointProperties;
import com.azure.resourcemanager.neonpostgres.models.EndpointType;
import com.azure.resourcemanager.neonpostgres.models.MarketplaceDetails;
import com.azure.resourcemanager.neonpostgres.models.MarketplaceSubscriptionStatus;
import com.azure.resourcemanager.neonpostgres.models.NeonDatabaseProperties;
import com.azure.resourcemanager.neonpostgres.models.NeonRoleProperties;
import com.azure.resourcemanager.neonpostgres.models.OfferDetails;
import com.azure.resourcemanager.neonpostgres.models.OrganizationResource;
import com.azure.resourcemanager.neonpostgres.models.OrganizationResourceUpdateProperties;
import com.azure.resourcemanager.neonpostgres.models.PartnerOrganizationProperties;
import com.azure.resourcemanager.neonpostgres.models.ProjectProperties;
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
     * x-ms-original-file: 2025-06-23-preview/Organizations_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_Update_MaximumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void
        organizationsUpdateMaximumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        OrganizationResource resource = manager.organizations()
            .getByResourceGroupWithResponse("rgneon", "myOrganization", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key2979", "fakeTokenPlaceholder"))
            .withProperties(new OrganizationResourceUpdateProperties()
                .withMarketplaceDetails(
                    new MarketplaceDetails().withSubscriptionId("11111111-2222-3333-4444-555555555555")
                        .withSubscriptionStatus(MarketplaceSubscriptionStatus.fromString("Fulfilled"))
                        .withOfferDetails(new OfferDetails().withPublisherId("neon")
                            .withOfferId("neon-postgres")
                            .withPlanId("standard")
                            .withPlanName("Standard Plan")
                            .withTermUnit("P1M")
                            .withTermId("hjk5-pou9-mnb8")))
                .withUserDetails(new UserDetails().withFirstName("John")
                    .withLastName("Doe")
                    .withEmailAddress("john.doe@example.com")
                    .withUpn("johndoe")
                    .withPhoneNumber("555-123-4567"))
                .withCompanyDetails(new CompanyDetails().withCompanyName("Contoso Ltd.")
                    .withCountry("United States")
                    .withOfficeAddress("123 Main Street, Seattle, WA 98101")
                    .withBusinessPhone("555-987-6543")
                    .withDomain("contoso.com")
                    .withNumberOfEmployees(250L))
                .withPartnerOrganizationProperties(
                    new PartnerOrganizationProperties().withOrganizationId("org-123456")
                        .withOrganizationName("myOrganization")
                        .withSingleSignOnProperties(new SingleSignOnProperties()
                            .withSingleSignOnState(SingleSignOnStates.fromString("Configured"))
                            .withEnterpriseAppId("12345678-abcd-1234-efgh-123456789012")
                            .withSingleSignOnUrl("https://login.microsoftonline.com/contoso.com")
                            .withAadDomains(Arrays.asList("contoso.com"))))
                .withProjectProperties(new ProjectProperties().withEntityName("myProject")
                    .withAttributes(Arrays.asList(new Attributes().withName("environment").withValue("development")))
                    .withRegionId("westus")
                    .withStorage(22L)
                    .withPgVersion(23)
                    .withHistoryRetention(16)
                    .withDefaultEndpointSettings(
                        new DefaultEndpointSettings().withAutoscalingLimitMinCu(11.0).withAutoscalingLimitMaxCu(11.0))
                    .withBranch(new BranchProperties().withEntityName("main")
                        .withAttributes(
                            Arrays.asList(new Attributes().withName("environment").withValue("development")))
                        .withProjectId("project-123")
                        .withParentId("main-branch")
                        .withRoles(Arrays.asList(new NeonRoleProperties().withEntityName("read_only_role")
                            .withAttributes(
                                Arrays.asList(new Attributes().withName("environment").withValue("development")))
                            .withBranchId("branch-123")
                            .withPermissions(Arrays.asList("SELECT"))
                            .withIsSuperUser(true)))
                        .withDatabases(Arrays.asList(new NeonDatabaseProperties().withEntityName("appdb")
                            .withAttributes(
                                Arrays.asList(new Attributes().withName("environment").withValue("development")))
                            .withBranchId("branch-123")
                            .withOwnerName("postgres")))
                        .withEndpoints(
                            Arrays.asList(new EndpointProperties().withEntityName("primary-endpoint")
                                .withAttributes(
                                    Arrays.asList(new Attributes().withName("environment").withValue("development")))
                                .withProjectId("project-123")
                                .withBranchId("branch-123")
                                .withEndpointType(EndpointType.READ_WRITE)
                                .withSize(new AutoscalingSize().withAutoscalingLimitMinCu(1.0)
                                    .withAutoscalingLimitMaxCu(4.0)))))
                    .withRoles(Arrays.asList(new NeonRoleProperties().withEntityName("admin_role")
                        .withAttributes(
                            Arrays.asList(new Attributes().withName("environment").withValue("development")))
                        .withBranchId("branch-123")
                        .withPermissions(Arrays.asList("ALL"))
                        .withIsSuperUser(true)))
                    .withDatabases(Arrays.asList(new NeonDatabaseProperties().withEntityName("postgres")
                        .withAttributes(
                            Arrays.asList(new Attributes().withName("environment").withValue("development")))
                        .withBranchId("branch-123")
                        .withOwnerName("postgres")))
                    .withEndpoints(
                        Arrays
                            .asList(new EndpointProperties().withEntityName("readonly-endpoint")
                                .withAttributes(
                                    Arrays.asList(new Attributes().withName("environment").withValue("development")))
                                .withProjectId("project-123")
                                .withBranchId("branch-123")
                                .withEndpointType(EndpointType.READ_ONLY)
                                .withSize(new AutoscalingSize().withAutoscalingLimitMinCu(1.0)
                                    .withAutoscalingLimitMaxCu(2.0))))))
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
import com.azure.resourcemanager.neonpostgres.models.Attributes;
import com.azure.resourcemanager.neonpostgres.models.AutoscalingSize;
import com.azure.resourcemanager.neonpostgres.models.BranchProperties;
import com.azure.resourcemanager.neonpostgres.models.DefaultEndpointSettings;
import com.azure.resourcemanager.neonpostgres.models.EndpointProperties;
import com.azure.resourcemanager.neonpostgres.models.EndpointType;
import com.azure.resourcemanager.neonpostgres.models.NeonDatabaseProperties;
import com.azure.resourcemanager.neonpostgres.models.NeonRoleProperties;
import com.azure.resourcemanager.neonpostgres.models.ProjectProperties;
import java.util.Arrays;

/**
 * Samples for Projects CreateOrUpdate.
 */
public final class ProjectsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-06-23-preview/Projects_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Projects_CreateOrUpdate_MaximumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void
        projectsCreateOrUpdateMaximumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.projects()
            .define("myProject")
            .withExistingOrganization("rgneon", "myOrganization")
            .withProperties(new ProjectProperties().withEntityName("myProject")
                .withAttributes(Arrays.asList(new Attributes().withName("environment").withValue("development")))
                .withRegionId("westus")
                .withStorage(22L)
                .withPgVersion(14)
                .withHistoryRetention(3)
                .withDefaultEndpointSettings(
                    new DefaultEndpointSettings().withAutoscalingLimitMinCu(3.0).withAutoscalingLimitMaxCu(28.0))
                .withBranch(new BranchProperties().withEntityName("main")
                    .withAttributes(Arrays.asList(new Attributes().withName("environment").withValue("development")))
                    .withProjectId("project-123")
                    .withParentId("main-branch")
                    .withRoles(Arrays.asList(new NeonRoleProperties().withEntityName("read_only_role")
                        .withAttributes(
                            Arrays.asList(new Attributes().withName("environment").withValue("development")))
                        .withBranchId("branch-123")
                        .withPermissions(Arrays.asList("SELECT"))
                        .withIsSuperUser(true)))
                    .withDatabases(Arrays.asList(new NeonDatabaseProperties().withEntityName("appdb")
                        .withAttributes(
                            Arrays.asList(new Attributes().withName("environment").withValue("development")))
                        .withBranchId("branch-123")
                        .withOwnerName("postgres")))
                    .withEndpoints(
                        Arrays
                            .asList(new EndpointProperties().withEntityName("primary-endpoint")
                                .withAttributes(
                                    Arrays.asList(new Attributes().withName("environment").withValue("development")))
                                .withProjectId("project-123")
                                .withBranchId("branch-123")
                                .withEndpointType(EndpointType.READ_WRITE)
                                .withSize(new AutoscalingSize().withAutoscalingLimitMinCu(1.0)
                                    .withAutoscalingLimitMaxCu(4.0)))))
                .withRoles(Arrays.asList(new NeonRoleProperties().withEntityName("admin_role")
                    .withAttributes(Arrays.asList(new Attributes().withName("environment").withValue("development")))
                    .withBranchId("branch-123")
                    .withPermissions(Arrays.asList("ALL"))
                    .withIsSuperUser(true)))
                .withDatabases(Arrays.asList(new NeonDatabaseProperties().withEntityName("postgres")
                    .withAttributes(Arrays.asList(new Attributes().withName("environment").withValue("development")))
                    .withBranchId("branch-123")
                    .withOwnerName("postgres")))
                .withEndpoints(Arrays.asList(new EndpointProperties().withEntityName("readonly-endpoint")
                    .withAttributes(Arrays.asList(new Attributes().withName("environment").withValue("development")))
                    .withProjectId("project-123")
                    .withBranchId("branch-123")
                    .withEndpointType(EndpointType.READ_ONLY)
                    .withSize(new AutoscalingSize().withAutoscalingLimitMinCu(1.0).withAutoscalingLimitMaxCu(2.0)))))
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
     * x-ms-original-file: 2025-06-23-preview/Projects_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Projects_Delete_MaximumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void projectsDeleteMaximumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.projects()
            .deleteWithResponse("rgneon", "myOrganization", "myProject", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-06-23-preview/Projects_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Projects_Get_MaximumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void projectsGetMaximumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.projects().getWithResponse("rgneon", "myOrganization", "myProject", com.azure.core.util.Context.NONE);
    }
}
```

### Projects_GetConnectionUri

```java
import com.azure.resourcemanager.neonpostgres.fluent.models.ConnectionUriPropertiesInner;

/**
 * Samples for Projects GetConnectionUri.
 */
public final class ProjectsGetConnectionUriSamples {
    /*
     * x-ms-original-file: 2025-06-23-preview/Projects_GetConnectionUri_MaximumSet_Gen.json
     */
    /**
     * Sample code: Projects_GetConnectionUri_MaximumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void
        projectsGetConnectionUriMaximumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.projects()
            .getConnectionUriWithResponse("rgneon", "myOrganization", "myProject",
                new ConnectionUriPropertiesInner().withProjectId("project-123")
                    .withBranchId("branch-123")
                    .withDatabaseName("application")
                    .withRoleName("admin")
                    .withEndpointId("endpoint-123")
                    .withIsPooled(true),
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-06-23-preview/Projects_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Projects_List_MaximumSet.
     * 
     * @param manager Entry point to NeonPostgresManager.
     */
    public static void projectsListMaximumSet(com.azure.resourcemanager.neonpostgres.NeonPostgresManager manager) {
        manager.projects().list("rgneon", "myOrganization", com.azure.core.util.Context.NONE);
    }
}
```

