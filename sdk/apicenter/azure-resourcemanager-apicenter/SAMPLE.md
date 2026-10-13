# Code snippets and samples


## ApiDefinitions

- [CreateOrUpdate](#apidefinitions_createorupdate)
- [Delete](#apidefinitions_delete)
- [ExportSpecification](#apidefinitions_exportspecification)
- [Get](#apidefinitions_get)
- [Head](#apidefinitions_head)
- [ImportSpecification](#apidefinitions_importspecification)
- [List](#apidefinitions_list)

## ApiSources

- [CreateOrUpdate](#apisources_createorupdate)
- [Delete](#apisources_delete)
- [Get](#apisources_get)
- [Head](#apisources_head)
- [List](#apisources_list)

## ApiVersions

- [CreateOrUpdate](#apiversions_createorupdate)
- [Delete](#apiversions_delete)
- [Get](#apiversions_get)
- [Head](#apiversions_head)
- [List](#apiversions_list)

## Apis

- [CreateOrUpdate](#apis_createorupdate)
- [Delete](#apis_delete)
- [Get](#apis_get)
- [Head](#apis_head)
- [List](#apis_list)

## DeletedServices

- [Delete](#deletedservices_delete)
- [GetByResourceGroup](#deletedservices_getbyresourcegroup)
- [List](#deletedservices_list)
- [ListByResourceGroup](#deletedservices_listbyresourcegroup)

## Deployments

- [CreateOrUpdate](#deployments_createorupdate)
- [Delete](#deployments_delete)
- [Get](#deployments_get)
- [Head](#deployments_head)
- [List](#deployments_list)

## Environments

- [CreateOrUpdate](#environments_createorupdate)
- [Delete](#environments_delete)
- [Get](#environments_get)
- [Head](#environments_head)
- [List](#environments_list)

## MetadataSchemas

- [CreateOrUpdate](#metadataschemas_createorupdate)
- [Delete](#metadataschemas_delete)
- [Get](#metadataschemas_get)
- [Head](#metadataschemas_head)
- [List](#metadataschemas_list)

## Operations

- [List](#operations_list)

## Services

- [CreateOrUpdate](#services_createorupdate)
- [Delete](#services_delete)
- [ExportMetadataSchema](#services_exportmetadataschema)
- [GetByResourceGroup](#services_getbyresourcegroup)
- [List](#services_list)
- [ListByResourceGroup](#services_listbyresourcegroup)
- [Update](#services_update)

## Workspaces

- [CreateOrUpdate](#workspaces_createorupdate)
- [Delete](#workspaces_delete)
- [Get](#workspaces_get)
- [Head](#workspaces_head)
- [List](#workspaces_list)
### ApiDefinitions_CreateOrUpdate

```java
import com.azure.resourcemanager.apicenter.models.ApiDefinitionProperties;

/**
 * Samples for ApiDefinitions CreateOrUpdate.
 */
public final class ApiDefinitionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/ApiDefinitions_CreateOrUpdate.json
     */
    /**
     * Sample code: ApiDefinitions_CreateOrUpdate.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void apiDefinitionsCreateOrUpdate(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.apiDefinitions()
            .define("openapi")
            .withExistingVersion("contoso-resources", "contoso", "default", "openapi", "2023-01-01")
            .withProperties(new ApiDefinitionProperties().withTitle("OpenAPI").withDescription("Default spec"))
            .create();
    }
}
```

### ApiDefinitions_Delete

```java
/**
 * Samples for ApiDefinitions Delete.
 */
public final class ApiDefinitionsDeleteSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/ApiDefinitions_Delete.json
     */
    /**
     * Sample code: ApiDefinitions_Delete.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void apiDefinitionsDelete(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.apiDefinitions()
            .deleteWithResponse("contoso-resources", "contoso", "default", "echo-api", "2023-01-01", "openapi",
                com.azure.core.util.Context.NONE);
    }
}
```

### ApiDefinitions_ExportSpecification

```java
/**
 * Samples for ApiDefinitions ExportSpecification.
 */
public final class ApiDefinitionsExportSpecificationSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/ApiDefinitions_ExportSpecification.json
     */
    /**
     * Sample code: ApiDefinitions_ExportSpecification.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void apiDefinitionsExportSpecification(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.apiDefinitions()
            .exportSpecification("contoso-resources", "contoso", "default", "echo-api", "2023-01-01", "openapi",
                com.azure.core.util.Context.NONE);
    }
}
```

### ApiDefinitions_Get

```java
/**
 * Samples for ApiDefinitions Get.
 */
public final class ApiDefinitionsGetSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/ApiDefinitions_Get.json
     */
    /**
     * Sample code: ApiDefinitions_Get.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void apiDefinitionsGet(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.apiDefinitions()
            .getWithResponse("contoso-resources", "contoso", "default", "echo-api", "2023-01-01", "openapi",
                com.azure.core.util.Context.NONE);
    }
}
```

### ApiDefinitions_Head

```java
/**
 * Samples for ApiDefinitions Head.
 */
public final class ApiDefinitionsHeadSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/ApiDefinitions_Head.json
     */
    /**
     * Sample code: ApiDefinitions_Head.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void apiDefinitionsHead(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.apiDefinitions()
            .headWithResponse("contoso-resources", "contoso", "default", "echo-api", "2023-01-01", "openapi",
                com.azure.core.util.Context.NONE);
    }
}
```

### ApiDefinitions_ImportSpecification

```java

/**
 * Samples for ApiDefinitions ImportSpecification.
 */
public final class ApiDefinitionsImportSpecificationSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/ApiDefinitions_ImportSpecification.json
     */
    /**
     * Sample code: ApiDefinitions_ImportSpecification.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void apiDefinitionsImportSpecification(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.apiDefinitions()
            .importSpecification("contoso-resources", "contoso", "default", "echo-api", "2023-01-01", "openapi", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### ApiDefinitions_List

```java
/**
 * Samples for ApiDefinitions List.
 */
public final class ApiDefinitionsListSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/ApiDefinitions_List.json
     */
    /**
     * Sample code: ApiDefinitions_ListByApiVersion.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void apiDefinitionsListByApiVersion(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.apiDefinitions()
            .list("contoso-resources", "contoso", "default", "echo-api", "2023-01-01", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### ApiSources_CreateOrUpdate

```java
import com.azure.resourcemanager.apicenter.models.ApiSourceProperties;
import com.azure.resourcemanager.apicenter.models.AzureApiManagementSource;
import com.azure.resourcemanager.apicenter.models.ImportSpecificationOptions;
import com.azure.resourcemanager.apicenter.models.LifecycleStage;

/**
 * Samples for ApiSources CreateOrUpdate.
 */
public final class ApiSourcesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/ApiSources_CreateOrUpdate.json
     */
    /**
     * Sample code: ApiSources_CreateOrUpdate.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void apiSourcesCreateOrUpdate(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.apiSources()
            .define("contoso-api-management")
            .withExistingWorkspace("contoso-resources", "contoso", "default")
            .withProperties(new ApiSourceProperties().withImportSpecification(ImportSpecificationOptions.ON_DEMAND)
                .withAzureApiManagementSource(new AzureApiManagementSource().withResourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/contoso-resources/providers/Microsoft.ApiManagement/service/contoso")
                    .withMsiResourceId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/contoso-resources/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-identity"))
                .withTargetEnvironmentId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/contoso-resources/providers/Microsoft.ApiCenter/services/contoso/workspaces/default/environments/azure-api-management")
                .withTargetLifecycleStage(LifecycleStage.DESIGN))
            .create();
    }
}
```

### ApiSources_Delete

```java
/**
 * Samples for ApiSources Delete.
 */
public final class ApiSourcesDeleteSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/ApiSources_Delete.json
     */
    /**
     * Sample code: ApiSources_Delete.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void apiSourcesDelete(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.apiSources()
            .deleteWithResponse("contoso-resources", "contoso", "default", "contoso-api-management",
                com.azure.core.util.Context.NONE);
    }
}
```

### ApiSources_Get

```java
/**
 * Samples for ApiSources Get.
 */
public final class ApiSourcesGetSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/ApiSources_Get.json
     */
    /**
     * Sample code: ApiSources_Get.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void apiSourcesGet(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.apiSources()
            .getWithResponse("contoso-resources", "contoso", "default", "contoso-api-management",
                com.azure.core.util.Context.NONE);
    }
}
```

### ApiSources_Head

```java
/**
 * Samples for ApiSources Head.
 */
public final class ApiSourcesHeadSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/ApiSources_Head.json
     */
    /**
     * Sample code: ApiSources_Head.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void apiSourcesHead(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.apiSources()
            .headWithResponse("contoso-resources", "contoso", "default", "contoso-api-management",
                com.azure.core.util.Context.NONE);
    }
}
```

### ApiSources_List

```java
/**
 * Samples for ApiSources List.
 */
public final class ApiSourcesListSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/ApiSources_List.json
     */
    /**
     * Sample code: ApiSources_ListByWorkspace.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void apiSourcesListByWorkspace(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.apiSources().list("contoso-resources", "contoso", "default", null, com.azure.core.util.Context.NONE);
    }
}
```

### ApiVersions_CreateOrUpdate

```java
import com.azure.resourcemanager.apicenter.models.ApiVersionProperties;
import com.azure.resourcemanager.apicenter.models.LifecycleStage;

/**
 * Samples for ApiVersions CreateOrUpdate.
 */
public final class ApiVersionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/ApiVersions_CreateOrUpdate.json
     */
    /**
     * Sample code: ApiVersions_CreateOrUpdate.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void apiVersionsCreateOrUpdate(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.apiVersions()
            .define("2023-01-01")
            .withExistingApi("contoso-resources", "contoso", "default", "echo-api")
            .withProperties(
                new ApiVersionProperties().withTitle("2023-01-01").withLifecycleStage(LifecycleStage.PRODUCTION))
            .create();
    }
}
```

### ApiVersions_Delete

```java
/**
 * Samples for ApiVersions Delete.
 */
public final class ApiVersionsDeleteSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/ApiVersions_Delete.json
     */
    /**
     * Sample code: ApiVersions_Delete.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void apiVersionsDelete(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.apiVersions()
            .deleteWithResponse("contoso-resources", "contoso", "default", "echo-api", "2023-01-01",
                com.azure.core.util.Context.NONE);
    }
}
```

### ApiVersions_Get

```java
/**
 * Samples for ApiVersions Get.
 */
public final class ApiVersionsGetSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/ApiVersions_Get.json
     */
    /**
     * Sample code: ApiVersions_Get.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void apiVersionsGet(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.apiVersions()
            .getWithResponse("contoso-resources", "contoso", "default", "echo-api", "2023-01-01",
                com.azure.core.util.Context.NONE);
    }
}
```

### ApiVersions_Head

```java
/**
 * Samples for ApiVersions Head.
 */
public final class ApiVersionsHeadSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/ApiVersions_Head.json
     */
    /**
     * Sample code: ApiVersions_Head.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void apiVersionsHead(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.apiVersions()
            .headWithResponse("contoso-resources", "contoso", "default", "echo-api", "2023-01-01",
                com.azure.core.util.Context.NONE);
    }
}
```

### ApiVersions_List

```java
/**
 * Samples for ApiVersions List.
 */
public final class ApiVersionsListSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/ApiVersions_List.json
     */
    /**
     * Sample code: ApiVersions_ListByApi.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void apiVersionsListByApi(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.apiVersions()
            .list("contoso-resources", "contoso", "default", "echo-api", null, com.azure.core.util.Context.NONE);
    }
}
```

### Apis_CreateOrUpdate

```java
import com.azure.resourcemanager.apicenter.models.ApiKind;
import com.azure.resourcemanager.apicenter.models.ApiProperties;
import com.azure.resourcemanager.apicenter.models.CustomProperties;
import com.azure.resourcemanager.apicenter.models.ExternalDocumentation;
import com.azure.resourcemanager.apicenter.models.License;
import com.azure.resourcemanager.apicenter.models.TermsOfService;
import java.util.Arrays;

/**
 * Samples for Apis CreateOrUpdate.
 */
public final class ApisCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/Apis_CreateOrUpdate.json
     */
    /**
     * Sample code: Apis_CreateOrUpdate.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void apisCreateOrUpdate(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.apis()
            .define("echo-api")
            .withExistingWorkspace("contoso-resources", "contoso", "default")
            .withProperties(new ApiProperties().withTitle("Echo API")
                .withKind(ApiKind.REST)
                .withDescription("A simple HTTP request/response service.")
                .withTermsOfService(new TermsOfService().withUrl("https://contoso.com/terms-of-service"))
                .withExternalDocumentation(Arrays.asList(
                    new ExternalDocumentation().withTitle("Onboarding docs").withUrl("https://docs.contoso.com")))
                .withLicense(new License().withUrl("https://contoso.com/license"))
                .withCustomProperties(new CustomProperties()))
            .create();
    }
}
```

### Apis_Delete

```java
/**
 * Samples for Apis Delete.
 */
public final class ApisDeleteSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/Apis_Delete.json
     */
    /**
     * Sample code: Apis_Delete.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void apisDelete(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.apis()
            .deleteWithResponse("contoso-resources", "contoso", "default", "echo-api",
                com.azure.core.util.Context.NONE);
    }
}
```

### Apis_Get

```java
/**
 * Samples for Apis Get.
 */
public final class ApisGetSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/Apis_Get.json
     */
    /**
     * Sample code: Apis_Get.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void apisGet(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.apis()
            .getWithResponse("contoso-resources", "contoso", "default", "echo-api", com.azure.core.util.Context.NONE);
    }
}
```

### Apis_Head

```java
/**
 * Samples for Apis Head.
 */
public final class ApisHeadSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/Apis_Head.json
     */
    /**
     * Sample code: Apis_Head.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void apisHead(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.apis()
            .headWithResponse("contoso-resources", "contoso", "default", "echo-api", com.azure.core.util.Context.NONE);
    }
}
```

### Apis_List

```java
/**
 * Samples for Apis List.
 */
public final class ApisListSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/Apis_List.json
     */
    /**
     * Sample code: Apis_ListByWorkspace.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void apisListByWorkspace(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.apis().list("contoso-resources", "contoso", "default", null, com.azure.core.util.Context.NONE);
    }
}
```

### DeletedServices_Delete

```java
/**
 * Samples for DeletedServices Delete.
 */
public final class DeletedServicesDeleteSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/DeletedServices_Delete.json
     */
    /**
     * Sample code: DeletedServices_Delete.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void deletedServicesDelete(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.deletedServices()
            .deleteByResourceGroupWithResponse("contoso-resources", "contoso", com.azure.core.util.Context.NONE);
    }
}
```

### DeletedServices_GetByResourceGroup

```java
/**
 * Samples for DeletedServices GetByResourceGroup.
 */
public final class DeletedServicesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/DeletedServices_Get.json
     */
    /**
     * Sample code: DeletedServices_Get.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void deletedServicesGet(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.deletedServices()
            .getByResourceGroupWithResponse("contoso-resources", "contoso", com.azure.core.util.Context.NONE);
    }
}
```

### DeletedServices_List

```java
/**
 * Samples for DeletedServices List.
 */
public final class DeletedServicesListSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/DeletedServices_List.json
     */
    /**
     * Sample code: DeletedServices_ListBySubscription.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void deletedServicesListBySubscription(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.deletedServices().list(com.azure.core.util.Context.NONE);
    }
}
```

### DeletedServices_ListByResourceGroup

```java
/**
 * Samples for DeletedServices ListByResourceGroup.
 */
public final class DeletedServicesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/DeletedServices_ListBySubscription.json
     */
    /**
     * Sample code: DeletedServices_List.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void deletedServicesList(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.deletedServices().listByResourceGroup("contoso-resources", null, com.azure.core.util.Context.NONE);
    }
}
```

### Deployments_CreateOrUpdate

```java
import com.azure.resourcemanager.apicenter.models.DeploymentProperties;
import com.azure.resourcemanager.apicenter.models.DeploymentServer;
import com.azure.resourcemanager.apicenter.models.DeploymentState;
import java.util.Arrays;

/**
 * Samples for Deployments CreateOrUpdate.
 */
public final class DeploymentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/Deployments_CreateOrUpdate.json
     */
    /**
     * Sample code: Deployments_CreateOrUpdate.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void deploymentsCreateOrUpdate(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.deployments()
            .define("production")
            .withExistingApi("contoso-resources", "contoso", "default", "echo-api")
            .withProperties(new DeploymentProperties().withTitle("Production deployment")
                .withDescription("Public cloud production deployment.")
                .withEnvironmentId("/workspaces/default/environments/production")
                .withDefinitionId("/workspaces/default/apis/echo-api/versions/2023-01-01/definitions/openapi")
                .withState(DeploymentState.ACTIVE)
                .withServer(new DeploymentServer().withRuntimeUri(Arrays.asList("https://api.contoso.com"))))
            .create();
    }
}
```

### Deployments_Delete

```java
/**
 * Samples for Deployments Delete.
 */
public final class DeploymentsDeleteSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/Deployments_Delete.json
     */
    /**
     * Sample code: Deployments_Delete.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void deploymentsDelete(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.deployments()
            .deleteWithResponse("contoso-resources", "contoso", "default", "echo-api", "production",
                com.azure.core.util.Context.NONE);
    }
}
```

### Deployments_Get

```java
/**
 * Samples for Deployments Get.
 */
public final class DeploymentsGetSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/Deployments_Get.json
     */
    /**
     * Sample code: Deployments_Get.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void deploymentsGet(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.deployments()
            .getWithResponse("contoso-resources", "contoso", "default", "echo-api", "production",
                com.azure.core.util.Context.NONE);
    }
}
```

### Deployments_Head

```java
/**
 * Samples for Deployments Head.
 */
public final class DeploymentsHeadSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/Deployments_Head.json
     */
    /**
     * Sample code: Deployments_Head.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void deploymentsHead(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.deployments()
            .headWithResponse("contoso-resources", "contoso", "default", "echo-api", "production",
                com.azure.core.util.Context.NONE);
    }
}
```

### Deployments_List

```java
/**
 * Samples for Deployments List.
 */
public final class DeploymentsListSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/Deployments_List.json
     */
    /**
     * Sample code: Deployments_ListByApi.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void deploymentsListByApi(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.deployments()
            .list("contoso-resources", "contoso", "default", "echo-api", null, com.azure.core.util.Context.NONE);
    }
}
```

### Environments_CreateOrUpdate

```java
import com.azure.resourcemanager.apicenter.models.EnvironmentKind;
import com.azure.resourcemanager.apicenter.models.EnvironmentProperties;
import com.azure.resourcemanager.apicenter.models.EnvironmentServer;
import com.azure.resourcemanager.apicenter.models.EnvironmentServerType;
import com.azure.resourcemanager.apicenter.models.Onboarding;
import java.util.Arrays;

/**
 * Samples for Environments CreateOrUpdate.
 */
public final class EnvironmentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/Environments_CreateOrUpdate.json
     */
    /**
     * Sample code: Environments_CreateOrUpdate.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void environmentsCreateOrUpdate(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.environments()
            .define("public")
            .withExistingWorkspace("contoso-resources", "contoso", "default")
            .withProperties(new EnvironmentProperties().withTitle("Contoso Europe Azure API Management")
                .withDescription("The primary Azure API Management service for the European division of Contoso.")
                .withKind(EnvironmentKind.PRODUCTION)
                .withServer(new EnvironmentServer().withType(EnvironmentServerType.AZURE_API_MANAGEMENT)
                    .withManagementPortalUri(Arrays.asList(
                        "https://management.azure.com/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/contoso-resources/providers/Microsoft.ApiManagement/service/contoso")))
                .withOnboarding(new Onboarding().withInstructions(
                    "Sign in or sign up in the specified developer portal to request API access. You must complete the internal privacy training for your account to be approved.")
                    .withDeveloperPortalUri(Arrays.asList("https://developer.contoso.com"))))
            .create();
    }
}
```

### Environments_Delete

```java
/**
 * Samples for Environments Delete.
 */
public final class EnvironmentsDeleteSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/Environments_Delete.json
     */
    /**
     * Sample code: Environments_Delete.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void environmentsDelete(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.environments()
            .deleteWithResponse("contoso-resources", "contoso", "default", "public", com.azure.core.util.Context.NONE);
    }
}
```

### Environments_Get

```java
/**
 * Samples for Environments Get.
 */
public final class EnvironmentsGetSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/Environments_Get.json
     */
    /**
     * Sample code: Environments_Get.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void environmentsGet(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.environments()
            .getWithResponse("contoso-resources", "contoso", "default", "public", com.azure.core.util.Context.NONE);
    }
}
```

### Environments_Head

```java
/**
 * Samples for Environments Head.
 */
public final class EnvironmentsHeadSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/Environments_Head.json
     */
    /**
     * Sample code: Environments_Head.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void environmentsHead(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.environments()
            .headWithResponse("contoso-resources", "contoso", "default", "public", com.azure.core.util.Context.NONE);
    }
}
```

### Environments_List

```java
/**
 * Samples for Environments List.
 */
public final class EnvironmentsListSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/Environments_List.json
     */
    /**
     * Sample code: Environments_ListByWorkspace.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void environmentsListByWorkspace(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.environments().list("contoso-resources", "contoso", "default", null, com.azure.core.util.Context.NONE);
    }
}
```

### MetadataSchemas_CreateOrUpdate

```java
import com.azure.resourcemanager.apicenter.models.MetadataAssignment;
import com.azure.resourcemanager.apicenter.models.MetadataAssignmentEntity;
import com.azure.resourcemanager.apicenter.models.MetadataSchemaProperties;
import java.util.Arrays;

/**
 * Samples for MetadataSchemas CreateOrUpdate.
 */
public final class MetadataSchemasCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/MetadataSchemas_CreateOrUpdate.json
     */
    /**
     * Sample code: MetadataSchemas_CreateOrUpdate.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void metadataSchemasCreateOrUpdate(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.metadataSchemas()
            .define("author")
            .withExistingService("contoso-resources", "contoso")
            .withProperties(new MetadataSchemaProperties()
                .withSchema("{\"type\":\"string\", \"title\":\"Author\", pattern: \"^[a-zA-Z]+$\"}")
                .withAssignedTo(Arrays
                    .asList(new MetadataAssignment().withEntity(MetadataAssignmentEntity.API).withDeprecated(true))))
            .create();
    }
}
```

### MetadataSchemas_Delete

```java
/**
 * Samples for MetadataSchemas Delete.
 */
public final class MetadataSchemasDeleteSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/MetadataSchemas_Delete.json
     */
    /**
     * Sample code: MetadataSchemas_Delete.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void metadataSchemasDelete(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.metadataSchemas()
            .deleteWithResponse("contoso-resources", "contoso", "author", com.azure.core.util.Context.NONE);
    }
}
```

### MetadataSchemas_Get

```java
/**
 * Samples for MetadataSchemas Get.
 */
public final class MetadataSchemasGetSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/MetadataSchemas_Get.json
     */
    /**
     * Sample code: MetadataSchemas_Get.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void metadataSchemasGet(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.metadataSchemas()
            .getWithResponse("contoso-resources", "contoso", "lastName", com.azure.core.util.Context.NONE);
    }
}
```

### MetadataSchemas_Head

```java
/**
 * Samples for MetadataSchemas Head.
 */
public final class MetadataSchemasHeadSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/MetadataSchemas_Head.json
     */
    /**
     * Sample code: MetadataSchemas_Head.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void metadataSchemasHead(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.metadataSchemas()
            .headWithResponse("contoso-resources", "contoso", "author", com.azure.core.util.Context.NONE);
    }
}
```

### MetadataSchemas_List

```java
/**
 * Samples for MetadataSchemas List.
 */
public final class MetadataSchemasListSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/MetadataSchemas_List.json
     */
    /**
     * Sample code: MetadataSchemas_ListByService.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void metadataSchemasListByService(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.metadataSchemas().list("contoso-resources", "contoso", null, com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2024-06-01-preview/Operations_List.json
     */
    /**
     * Sample code: List Provider Operations.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void listProviderOperations(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Services_CreateOrUpdate

```java
import com.azure.resourcemanager.apicenter.models.ManagedServiceIdentity;
import com.azure.resourcemanager.apicenter.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.apicenter.models.ServiceProperties;
import com.azure.resourcemanager.apicenter.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Services CreateOrUpdate.
 */
public final class ServicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/Services_CreateOrUpdate.json
     */
    /**
     * Sample code: Services_CreateOrUpdate.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void servicesCreateOrUpdate(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.services()
            .define("contoso")
            .withRegion("East US")
            .withExistingResourceGroup("contoso-resources")
            .withTags(mapOf())
            .withProperties(new ServiceProperties())
            .withIdentity(new ManagedServiceIdentity()
                .withType(ManagedServiceIdentityType.fromString("SystemAssigned, UserAssigned"))
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/contoso-resources/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-identity",
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

### Services_Delete

```java
/**
 * Samples for Services Delete.
 */
public final class ServicesDeleteSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/Services_Delete.json
     */
    /**
     * Sample code: Services_Delete.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void servicesDelete(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.services()
            .deleteByResourceGroupWithResponse("contoso-resources", "contoso", com.azure.core.util.Context.NONE);
    }
}
```

### Services_ExportMetadataSchema

```java
import com.azure.resourcemanager.apicenter.models.MetadataAssignmentEntity;
import com.azure.resourcemanager.apicenter.models.MetadataSchemaExportRequest;

/**
 * Samples for Services ExportMetadataSchema.
 */
public final class ServicesExportMetadataSchemaSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/Services_ExportMetadataSchema.json
     */
    /**
     * Sample code: Services_ExportMetadataSchema.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void servicesExportMetadataSchema(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.services()
            .exportMetadataSchema("contoso-resources", "contoso",
                new MetadataSchemaExportRequest().withAssignedTo(MetadataAssignmentEntity.API),
                com.azure.core.util.Context.NONE);
    }
}
```

### Services_GetByResourceGroup

```java
/**
 * Samples for Services GetByResourceGroup.
 */
public final class ServicesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/Services_Get.json
     */
    /**
     * Sample code: Services_Get.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void servicesGet(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.services()
            .getByResourceGroupWithResponse("contoso-resources", "contoso", com.azure.core.util.Context.NONE);
    }
}
```

### Services_List

```java
/**
 * Samples for Services List.
 */
public final class ServicesListSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/Services_ListBySubscription.json
     */
    /**
     * Sample code: Services_ListBySubscription.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void servicesListBySubscription(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.services().list(com.azure.core.util.Context.NONE);
    }
}
```

### Services_ListByResourceGroup

```java
/**
 * Samples for Services ListByResourceGroup.
 */
public final class ServicesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/Services_ListByResourceGroup.json
     */
    /**
     * Sample code: Services_ListByResourceGroup.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void servicesListByResourceGroup(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.services().listByResourceGroup("contoso-resources", com.azure.core.util.Context.NONE);
    }
}
```

### Services_Update

```java
import com.azure.resourcemanager.apicenter.models.ManagedServiceIdentity;
import com.azure.resourcemanager.apicenter.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.apicenter.models.Service;
import com.azure.resourcemanager.apicenter.models.UserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Services Update.
 */
public final class ServicesUpdateSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/Services_Update.json
     */
    /**
     * Sample code: Services_Update.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void servicesUpdate(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        Service resource = manager.services()
            .getByResourceGroupWithResponse("contoso-resources", "contoso", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf())
            .withIdentity(new ManagedServiceIdentity()
                .withType(ManagedServiceIdentityType.fromString("SystemAssigned, UserAssigned"))
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/contoso-resources/providers/Microsoft.ManagedIdentity/userAssignedIdentities/contoso-identity",
                    new UserAssignedIdentity())))
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

### Workspaces_CreateOrUpdate

```java
import com.azure.resourcemanager.apicenter.models.WorkspaceProperties;

/**
 * Samples for Workspaces CreateOrUpdate.
 */
public final class WorkspacesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/Workspaces_CreateOrUpdate.json
     */
    /**
     * Sample code: Workspaces_CreateOrUpdate.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void workspacesCreateOrUpdate(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.workspaces()
            .define("default")
            .withExistingService("contoso-resources", "contoso")
            .withProperties(new WorkspaceProperties().withTitle("default"))
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
     * x-ms-original-file: 2024-06-01-preview/Workspaces_Delete.json
     */
    /**
     * Sample code: Workspaces_Delete.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void workspacesDelete(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.workspaces()
            .deleteWithResponse("contoso-resources", "contoso", "default", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2024-06-01-preview/Workspaces_Get.json
     */
    /**
     * Sample code: Workspaces_Get.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void workspacesGet(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.workspaces()
            .getWithResponse("contoso-resources", "contoso", "default", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_Head

```java
/**
 * Samples for Workspaces Head.
 */
public final class WorkspacesHeadSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/Workspaces_Head.json
     */
    /**
     * Sample code: Workspaces_Head.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void workspacesHead(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.workspaces()
            .headWithResponse("contoso-resources", "contoso", "default", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_List

```java
/**
 * Samples for Workspaces List.
 */
public final class WorkspacesListSamples {
    /*
     * x-ms-original-file: 2024-06-01-preview/Workspaces_List.json
     */
    /**
     * Sample code: Workspaces_ListByService.
     * 
     * @param manager Entry point to ApiCenterManager.
     */
    public static void workspacesListByService(com.azure.resourcemanager.apicenter.ApiCenterManager manager) {
        manager.workspaces().list("contoso-resources", "contoso", null, com.azure.core.util.Context.NONE);
    }
}
```

