# Code snippets and samples


## Operations

- [List](#operations_list)

## Organizations

- [CreateOrUpdate](#organizations_createorupdate)
- [Delete](#organizations_delete)
- [GetAllServerlessRuntimes](#organizations_getallserverlessruntimes)
- [GetByResourceGroup](#organizations_getbyresourcegroup)
- [GetServerlessMetadata](#organizations_getserverlessmetadata)
- [List](#organizations_list)
- [ListByResourceGroup](#organizations_listbyresourcegroup)
- [Update](#organizations_update)

## ServerlessRuntimes

- [CheckDependencies](#serverlessruntimes_checkdependencies)
- [CreateOrUpdate](#serverlessruntimes_createorupdate)
- [Delete](#serverlessruntimes_delete)
- [Get](#serverlessruntimes_get)
- [ListByInformaticaOrganizationResource](#serverlessruntimes_listbyinformaticaorganizationresource)
- [ServerlessResourceById](#serverlessruntimes_serverlessresourcebyid)
- [StartFailedServerlessRuntime](#serverlessruntimes_startfailedserverlessruntime)
- [Update](#serverlessruntimes_update)
### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2025-11-27/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void
        operationsList(com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-11-27/Operations_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_Min.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void operationsListMin(
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Organizations_CreateOrUpdate

```java
import com.azure.resourcemanager.informaticadatamanagement.models.CompanyDetails;
import com.azure.resourcemanager.informaticadatamanagement.models.InformaticaProperties;
import com.azure.resourcemanager.informaticadatamanagement.models.LinkOrganization;
import com.azure.resourcemanager.informaticadatamanagement.models.MarketplaceDetails;
import com.azure.resourcemanager.informaticadatamanagement.models.OfferDetails;
import com.azure.resourcemanager.informaticadatamanagement.models.OrganizationProperties;
import com.azure.resourcemanager.informaticadatamanagement.models.UserDetails;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Organizations CreateOrUpdate.
 */
public final class OrganizationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-11-27/Organizations_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_CreateOrUpdate.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void organizationsCreateOrUpdate(
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        manager.organizations()
            .define("myOrganization")
            .withRegion("westus")
            .withExistingResourceGroup("rg-example")
            .withTags(mapOf("environment", "production"))
            .withProperties(new OrganizationProperties()
                .withInformaticaProperties(new InformaticaProperties().withOrganizationId("org123")
                    .withOrganizationName("MyInformaticaOrg")
                    .withInformaticaRegion("westus")
                    .withSingleSignOnUrl("https://sso.informatica.com/myorg"))
                .withMarketplaceDetails(new MarketplaceDetails().withMarketplaceSubscriptionId("mktplace-sub-123")
                    .withOfferDetails(new OfferDetails().withPublisherId("informatica")
                        .withOfferId("informatica-cloud")
                        .withPlanId("enterprise-plan")
                        .withPlanName("Enterprise Plan")
                        .withTermUnit("P1M")
                        .withTermId("term-001")))
                .withUserDetails(new UserDetails().withFirstName("John")
                    .withLastName("Doe")
                    .withEmailAddress("john.doe@example.com")
                    .withUpn("john.doe@example.com")
                    .withPhoneNumber("+1-555-0100"))
                .withCompanyDetails(new CompanyDetails().withCompanyName("Contoso Ltd")
                    .withOfficeAddress("123 Main Street, Seattle, WA 98101")
                    .withCountry("USA")
                    .withDomain("contoso.com")
                    .withBusiness("Technology")
                    .withNumberOfEmployees(500))
                .withLinkOrganization(new LinkOrganization().withToken("fakeTokenPlaceholder")))
            .create();
    }

    /*
     * x-ms-original-file: 2025-11-27/Organizations_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: Organizations_CreateOrUpdate_Min.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void organizationsCreateOrUpdateMin(
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        manager.organizations()
            .define("myOrganization")
            .withRegion("westus")
            .withExistingResourceGroup("rg-example")
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
     * x-ms-original-file: 2025-11-27/Organizations_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_Delete.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void organizationsDelete(
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        manager.organizations().delete("rg-example", "myOrganization", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-11-27/Organizations_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: Organizations_Delete_Min.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void organizationsDeleteMin(
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        manager.organizations().delete("rg-example", "myOrganization", com.azure.core.util.Context.NONE);
    }
}
```

### Organizations_GetAllServerlessRuntimes

```java
/**
 * Samples for Organizations GetAllServerlessRuntimes.
 */
public final class OrganizationsGetAllServerlessRuntimesSamples {
    /*
     * x-ms-original-file: 2025-11-27/Organizations_GetAllServerlessRuntimes_MinimumSet_Gen.json
     */
    /**
     * Sample code: Organizations_GetAllServerlessRuntimes_Min.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void organizationsGetAllServerlessRuntimesMin(
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        manager.organizations()
            .getAllServerlessRuntimesWithResponse("rg-example", "myOrganization", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-11-27/Organizations_GetAllServerlessRuntimes_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_GetAllServerlessRuntimes.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void organizationsGetAllServerlessRuntimes(
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        manager.organizations()
            .getAllServerlessRuntimesWithResponse("rg-example", "myOrganization", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-11-27/Organizations_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: Organizations_Get_Min.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void organizationsGetMin(
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        manager.organizations()
            .getByResourceGroupWithResponse("rg-example", "myOrganization", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-11-27/Organizations_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_Get.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void
        organizationsGet(com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        manager.organizations()
            .getByResourceGroupWithResponse("rg-example", "myOrganization", com.azure.core.util.Context.NONE);
    }
}
```

### Organizations_GetServerlessMetadata

```java
/**
 * Samples for Organizations GetServerlessMetadata.
 */
public final class OrganizationsGetServerlessMetadataSamples {
    /*
     * x-ms-original-file: 2025-11-27/Organizations_GetServerlessMetadata_MinimumSet_Gen.json
     */
    /**
     * Sample code: Organizations_GetServerlessMetadata_Min.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void organizationsGetServerlessMetadataMin(
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        manager.organizations()
            .getServerlessMetadataWithResponse("rg-example", "myOrganization", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-11-27/Organizations_GetServerlessMetadata_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_GetServerlessMetadata.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void organizationsGetServerlessMetadata(
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        manager.organizations()
            .getServerlessMetadataWithResponse("rg-example", "myOrganization", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-11-27/Organizations_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_ListBySubscription.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void organizationsListBySubscription(
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        manager.organizations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-11-27/Organizations_ListBySubscription_MinimumSet_Gen.json
     */
    /**
     * Sample code: Organizations_ListBySubscription_Min.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void organizationsListBySubscriptionMin(
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
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
     * x-ms-original-file: 2025-11-27/Organizations_ListByResourceGroup_MinimumSet_Gen.json
     */
    /**
     * Sample code: Organizations_ListByResourceGroup_Min.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void organizationsListByResourceGroupMin(
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        manager.organizations().listByResourceGroup("rg-example", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-11-27/Organizations_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_ListByResourceGroup.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void organizationsListByResourceGroup(
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        manager.organizations().listByResourceGroup("rg-example", com.azure.core.util.Context.NONE);
    }
}
```

### Organizations_Update

```java
import com.azure.resourcemanager.informaticadatamanagement.models.CompanyDetailsUpdate;
import com.azure.resourcemanager.informaticadatamanagement.models.InformaticaOrganizationResource;
import com.azure.resourcemanager.informaticadatamanagement.models.InformaticaOrganizationResourceUpdate;
import com.azure.resourcemanager.informaticadatamanagement.models.MarketplaceDetailsUpdate;
import com.azure.resourcemanager.informaticadatamanagement.models.OfferDetailsUpdate;
import com.azure.resourcemanager.informaticadatamanagement.models.OrganizationPropertiesCustomUpdate;
import com.azure.resourcemanager.informaticadatamanagement.models.UserDetailsUpdate;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Organizations Update.
 */
public final class OrganizationsUpdateSamples {
    /*
     * x-ms-original-file: 2025-11-27/Organizations_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Organizations_Update.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void organizationsUpdate(
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        InformaticaOrganizationResource resource = manager.organizations()
            .getByResourceGroupWithResponse("rg-example", "myOrganization", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("environment", "production", "team", "platform"))
            .withProperties(new OrganizationPropertiesCustomUpdate()
                .withInformaticaOrganizationProperties(
                    new InformaticaOrganizationResourceUpdate().withTags(mapOf("env", "production")))
                .withMarketplaceDetails(new MarketplaceDetailsUpdate().withMarketplaceSubscriptionId("mktplace-sub-123")
                    .withOfferDetails(new OfferDetailsUpdate().withPublisherId("informatica")
                        .withOfferId("informatica-cloud")
                        .withPlanId("enterprise-plan")
                        .withPlanName("Enterprise Plan")
                        .withTermUnit("P1M")
                        .withTermId("term-001")))
                .withUserDetails(new UserDetailsUpdate().withFirstName("John")
                    .withLastName("Doe")
                    .withEmailAddress("john.doe@example.com")
                    .withUpn("john.doe@example.com")
                    .withPhoneNumber("+1-555-0100"))
                .withCompanyDetails(new CompanyDetailsUpdate().withCompanyName("Contoso Ltd")
                    .withOfficeAddress("123 Main Street, Seattle, WA 98101")
                    .withCountry("USA")
                    .withDomain("contoso.com")
                    .withBusiness("Technology")
                    .withNumberOfEmployees(500))
                .withExistingResourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg-example/providers/Informatica.DataManagement/organizations/existingOrg"))
            .apply();
    }

    /*
     * x-ms-original-file: 2025-11-27/Organizations_Update_MinimumSet_Gen.json
     */
    /**
     * Sample code: Organizations_Update_Min.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void organizationsUpdateMin(
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        InformaticaOrganizationResource resource = manager.organizations()
            .getByResourceGroupWithResponse("rg-example", "myOrganization", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
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

### ServerlessRuntimes_CheckDependencies

```java
/**
 * Samples for ServerlessRuntimes CheckDependencies.
 */
public final class ServerlessRuntimesCheckDependenciesSamples {
    /*
     * x-ms-original-file: 2025-11-27/ServerlessRuntimes_CheckDependencies_MinimumSet_Gen.json
     */
    /**
     * Sample code: ServerlessRuntimes_CheckDependencies_Min.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void serverlessRuntimesCheckDependenciesMin(
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        manager.serverlessRuntimes()
            .checkDependenciesWithResponse("rg-example", "myOrganization", "myServerlessRuntime",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-11-27/ServerlessRuntimes_CheckDependencies_MaximumSet_Gen.json
     */
    /**
     * Sample code: ServerlessRuntimes_CheckDependencies.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void serverlessRuntimesCheckDependencies(
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        manager.serverlessRuntimes()
            .checkDependenciesWithResponse("rg-example", "myOrganization", "myServerlessRuntime",
                com.azure.core.util.Context.NONE);
    }
}
```

### ServerlessRuntimes_CreateOrUpdate

```java
import com.azure.resourcemanager.informaticadatamanagement.models.ApplicationConfigs;
import com.azure.resourcemanager.informaticadatamanagement.models.ApplicationType;
import com.azure.resourcemanager.informaticadatamanagement.models.CdiConfigProps;
import com.azure.resourcemanager.informaticadatamanagement.models.InformaticaServerlessRuntimeProperties;
import com.azure.resourcemanager.informaticadatamanagement.models.NetworkInterfaceConfiguration;
import com.azure.resourcemanager.informaticadatamanagement.models.PlatformType;
import com.azure.resourcemanager.informaticadatamanagement.models.ServerlessRuntimeConfigProperties;
import com.azure.resourcemanager.informaticadatamanagement.models.ServerlessRuntimeDataDisk;
import com.azure.resourcemanager.informaticadatamanagement.models.ServerlessRuntimeNetworkProfile;
import com.azure.resourcemanager.informaticadatamanagement.models.ServerlessRuntimeTag;
import com.azure.resourcemanager.informaticadatamanagement.models.ServerlessRuntimeUserContextProperties;
import java.util.Arrays;

/**
 * Samples for ServerlessRuntimes CreateOrUpdate.
 */
public final class ServerlessRuntimesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2025-11-27/ServerlessRuntimes_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: ServerlessRuntimes_CreateOrUpdate.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void serverlessRuntimesCreateOrUpdate(
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        manager.serverlessRuntimes()
            .define("myServerlessRuntime")
            .withExistingOrganization("rg-example", "myOrganization")
            .withProperties(new InformaticaServerlessRuntimeProperties()
                .withDescription("Production serverless runtime")
                .withPlatform(PlatformType.AZURE)
                .withApplicationType(ApplicationType.CDI)
                .withComputeUnits("4")
                .withExecutionTimeout("3600")
                .withServerlessAccountLocation("westus")
                .withServerlessRuntimeNetworkProfile(new ServerlessRuntimeNetworkProfile()
                    .withNetworkInterfaceConfiguration(new NetworkInterfaceConfiguration().withVnetId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg-example/providers/Microsoft.Network/virtualNetworks/myVnet")
                        .withSubnetId(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg-example/providers/Microsoft.Network/virtualNetworks/myVnet/subnets/default")
                        .withVnetResourceGuid("vnet-guid-001")))
                .withAdvancedCustomProperties(Arrays.asList())
                .withSupplementaryFileLocation("/files/supplementary")
                .withServerlessRuntimeDataDisks(Arrays.asList(new ServerlessRuntimeDataDisk().withType("NFS")
                    .withServerHostOrIpAddress("10.0.0.5")
                    .withSourceMount("/source/data")
                    .withTargetMount("/target/data")
                    .withMountOptions("rw,sync")))
                .withServerlessRuntimeConfig(new ServerlessRuntimeConfigProperties()
                    .withCdiConfigProps(Arrays.asList(new CdiConfigProps().withEngineName("CDI Engine")
                        .withEngineVersion("1.0")
                        .withApplicationConfigs(Arrays.asList(new ApplicationConfigs().withType("string")
                            .withName("configName")
                            .withValue("configValue")
                            .withPlatform("AZURE")
                            .withCustomized("false")
                            .withDefaultValue("default")))))
                    .withCdieConfigProps(Arrays.asList(new CdiConfigProps().withEngineName("CDIE Engine")
                        .withEngineVersion("1.0")
                        .withApplicationConfigs(Arrays.asList(new ApplicationConfigs().withType("string")
                            .withName("configName")
                            .withValue("configValue")
                            .withPlatform("AZURE")
                            .withCustomized("false")
                            .withDefaultValue("default"))))))
                .withServerlessRuntimeTags(
                    Arrays.asList(new ServerlessRuntimeTag().withName("environment").withValue("production")))
                .withServerlessRuntimeUserContextProperties(
                    new ServerlessRuntimeUserContextProperties().withUserContextToken("fakeTokenPlaceholder")))
            .create();
    }

    /*
     * x-ms-original-file: 2025-11-27/ServerlessRuntimes_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: ServerlessRuntimes_CreateOrUpdate_Min.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void serverlessRuntimesCreateOrUpdateMin(
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        manager.serverlessRuntimes()
            .define("myServerlessRuntime")
            .withExistingOrganization("rg-example", "myOrganization")
            .withProperties(new InformaticaServerlessRuntimeProperties().withServerlessAccountLocation("westus"))
            .create();
    }
}
```

### ServerlessRuntimes_Delete

```java
/**
 * Samples for ServerlessRuntimes Delete.
 */
public final class ServerlessRuntimesDeleteSamples {
    /*
     * x-ms-original-file: 2025-11-27/ServerlessRuntimes_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: ServerlessRuntimes_Delete.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void serverlessRuntimesDelete(
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        manager.serverlessRuntimes()
            .delete("rg-example", "myOrganization", "myServerlessRuntime", com.azure.core.util.Context.NONE);
    }
}
```

### ServerlessRuntimes_Get

```java
/**
 * Samples for ServerlessRuntimes Get.
 */
public final class ServerlessRuntimesGetSamples {
    /*
     * x-ms-original-file: 2025-11-27/ServerlessRuntimes_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: ServerlessRuntimes_Get.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void serverlessRuntimesGet(
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        manager.serverlessRuntimes()
            .getWithResponse("rg-example", "myOrganization", "myServerlessRuntime", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-11-27/ServerlessRuntimes_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: ServerlessRuntimes_Get_Min.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void serverlessRuntimesGetMin(
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        manager.serverlessRuntimes()
            .getWithResponse("rg-example", "myOrganization", "myServerlessRuntime", com.azure.core.util.Context.NONE);
    }
}
```

### ServerlessRuntimes_ListByInformaticaOrganizationResource

```java
/**
 * Samples for ServerlessRuntimes ListByInformaticaOrganizationResource.
 */
public final class ServerlessRuntimesListByInformaticaOrganizationResourSamples {
    /*
     * x-ms-original-file: 2025-11-27/ServerlessRuntimes_ListByInformaticaOrganizationResource_MaximumSet_Gen.json
     */
    /**
     * Sample code: ServerlessRuntimes_ListByInformaticaOrganizationResource.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void serverlessRuntimesListByInformaticaOrganizationResource(
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        manager.serverlessRuntimes()
            .listByInformaticaOrganizationResource("rg-example", "myOrganization", com.azure.core.util.Context.NONE);
    }
}
```

### ServerlessRuntimes_ServerlessResourceById

```java
/**
 * Samples for ServerlessRuntimes ServerlessResourceById.
 */
public final class ServerlessRuntimesServerlessResourceByIdSamples {
    /*
     * x-ms-original-file: 2025-11-27/ServerlessRuntimes_ServerlessResourceById_MaximumSet_Gen.json
     */
    /**
     * Sample code: ServerlessRuntimes_ServerlessResourceById.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void serverlessRuntimesServerlessResourceById(
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        manager.serverlessRuntimes()
            .serverlessResourceByIdWithResponse("rg-example", "myOrganization", "myServerlessRuntime",
                com.azure.core.util.Context.NONE);
    }
}
```

### ServerlessRuntimes_StartFailedServerlessRuntime

```java
/**
 * Samples for ServerlessRuntimes StartFailedServerlessRuntime.
 */
public final class ServerlessRuntimesStartFailedServerlessRuntimeSamples {
    /*
     * x-ms-original-file: 2025-11-27/ServerlessRuntimes_StartFailedServerlessRuntime_MaximumSet_Gen.json
     */
    /**
     * Sample code: ServerlessRuntimes_StartFailedServerlessRuntime.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void serverlessRuntimesStartFailedServerlessRuntime(
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        manager.serverlessRuntimes()
            .startFailedServerlessRuntimeWithResponse("rg-example", "myOrganization", "myServerlessRuntime",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2025-11-27/ServerlessRuntimes_StartFailedServerlessRuntime_MinimumSet_Gen.json
     */
    /**
     * Sample code: ServerlessRuntimes_StartFailedServerlessRuntime_Min.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void serverlessRuntimesStartFailedServerlessRuntimeMin(
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        manager.serverlessRuntimes()
            .startFailedServerlessRuntimeWithResponse("rg-example", "myOrganization", "myServerlessRuntime",
                com.azure.core.util.Context.NONE);
    }
}
```

### ServerlessRuntimes_Update

```java
import com.azure.resourcemanager.informaticadatamanagement.models.ApplicationConfigs;
import com.azure.resourcemanager.informaticadatamanagement.models.ApplicationType;
import com.azure.resourcemanager.informaticadatamanagement.models.CdiConfigProps;
import com.azure.resourcemanager.informaticadatamanagement.models.InformaticaServerlessRuntimeResource;
import com.azure.resourcemanager.informaticadatamanagement.models.NetworkInterfaceConfigurationUpdate;
import com.azure.resourcemanager.informaticadatamanagement.models.PlatformType;
import com.azure.resourcemanager.informaticadatamanagement.models.ServerlessRuntimeConfigPropertiesUpdate;
import com.azure.resourcemanager.informaticadatamanagement.models.ServerlessRuntimeDataDisk;
import com.azure.resourcemanager.informaticadatamanagement.models.ServerlessRuntimeNetworkProfileUpdate;
import com.azure.resourcemanager.informaticadatamanagement.models.ServerlessRuntimePropertiesCustomUpdate;
import com.azure.resourcemanager.informaticadatamanagement.models.ServerlessRuntimeTag;
import com.azure.resourcemanager.informaticadatamanagement.models.ServerlessRuntimeUserContextPropertiesUpdate;
import java.util.Arrays;

/**
 * Samples for ServerlessRuntimes Update.
 */
public final class ServerlessRuntimesUpdateSamples {
    /*
     * x-ms-original-file: 2025-11-27/ServerlessRuntimes_Update_MinimumSet_Gen.json
     */
    /**
     * Sample code: ServerlessRuntimes_Update_Min.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void serverlessRuntimesUpdateMin(
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        InformaticaServerlessRuntimeResource resource = manager.serverlessRuntimes()
            .getWithResponse("rg-example", "myOrganization", "myServerlessRuntime", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
    }

    /*
     * x-ms-original-file: 2025-11-27/ServerlessRuntimes_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: ServerlessRuntimes_Update.
     * 
     * @param manager Entry point to InformaticaDataManagementManager.
     */
    public static void serverlessRuntimesUpdate(
        com.azure.resourcemanager.informaticadatamanagement.InformaticaDataManagementManager manager) {
        InformaticaServerlessRuntimeResource resource = manager.serverlessRuntimes()
            .getWithResponse("rg-example", "myOrganization", "myServerlessRuntime", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withProperties(new ServerlessRuntimePropertiesCustomUpdate()
                .withDescription("Updated production serverless runtime")
                .withPlatform(PlatformType.AZURE)
                .withApplicationType(ApplicationType.CDI)
                .withComputeUnits("8")
                .withExecutionTimeout("7200")
                .withServerlessAccountLocation("westus")
                .withServerlessRuntimeNetworkProfile(new ServerlessRuntimeNetworkProfileUpdate()
                    .withNetworkInterfaceConfiguration(new NetworkInterfaceConfigurationUpdate().withVnetId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg-example/providers/Microsoft.Network/virtualNetworks/myVnet")
                        .withSubnetId(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg-example/providers/Microsoft.Network/virtualNetworks/myVnet/subnets/default")
                        .withVnetResourceGuid("vnet-guid-001")))
                .withAdvancedCustomProperties(Arrays.asList())
                .withSupplementaryFileLocation("/files/supplementary")
                .withServerlessRuntimeDataDisks(Arrays.asList(new ServerlessRuntimeDataDisk().withType("NFS")
                    .withServerHostOrIpAddress("10.0.0.10")
                    .withSourceMount("/source/data-updated")
                    .withTargetMount("/target/data-updated")
                    .withMountOptions("rw,sync")))
                .withServerlessRuntimeConfig(new ServerlessRuntimeConfigPropertiesUpdate()
                    .withCdiConfigProps(Arrays.asList(new CdiConfigProps().withEngineName("CDI Engine")
                        .withEngineVersion("2.0")
                        .withApplicationConfigs(Arrays.asList(new ApplicationConfigs().withType("string")
                            .withName("updatedConfig")
                            .withValue("updatedValue")
                            .withPlatform("AZURE")
                            .withCustomized("true")
                            .withDefaultValue("default")))))
                    .withCdieConfigProps(Arrays.asList(new CdiConfigProps().withEngineName("CDIE Engine")
                        .withEngineVersion("2.0")
                        .withApplicationConfigs(Arrays.asList(new ApplicationConfigs().withType("string")
                            .withName("updatedConfig")
                            .withValue("updatedValue")
                            .withPlatform("AZURE")
                            .withCustomized("true")
                            .withDefaultValue("default"))))))
                .withServerlessRuntimeTags(
                    Arrays.asList(new ServerlessRuntimeTag().withName("environment").withValue("staging")))
                .withServerlessRuntimeUserContextProperties(
                    new ServerlessRuntimeUserContextPropertiesUpdate().withUserContextToken("fakeTokenPlaceholder")))
            .apply();
    }
}
```

