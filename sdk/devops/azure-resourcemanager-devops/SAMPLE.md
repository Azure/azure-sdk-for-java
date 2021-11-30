# Code snippets and samples


## Operations

- [List](#operations_list)

## PipelineTemplateDefinitions

- [List](#pipelinetemplatedefinitions_list)

## Pipelines

- [CreateOrUpdate](#pipelines_createorupdate)
- [Delete](#pipelines_delete)
- [GetByResourceGroup](#pipelines_getbyresourcegroup)
- [List](#pipelines_list)
- [ListByResourceGroup](#pipelines_listbyresourcegroup)
- [Update](#pipelines_update)
### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/devops/resource-manager/Microsoft.DevOps/preview/2019-07-01-preview/examples/ListOperations.json
     */
    /**
     * Sample code: Get a list of operations supported by Microsoft.DevOps resource provider.
     *
     * @param manager Entry point to DevopsManager.
     */
    public static void getAListOfOperationsSupportedByMicrosoftDevOpsResourceProvider(
        com.azure.resourcemanager.devops.DevopsManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### PipelineTemplateDefinitions_List

```java
import com.azure.core.util.Context;

/** Samples for PipelineTemplateDefinitions List. */
public final class PipelineTemplateDefinitionsListSamples {
    /*
     * x-ms-original-file: specification/devops/resource-manager/Microsoft.DevOps/preview/2019-07-01-preview/examples/ListPipelineTemplateDefinitions.json
     */
    /**
     * Sample code: Get the list of pipeline template definitions.
     *
     * @param manager Entry point to DevopsManager.
     */
    public static void getTheListOfPipelineTemplateDefinitions(com.azure.resourcemanager.devops.DevopsManager manager) {
        manager.pipelineTemplateDefinitions().list(Context.NONE);
    }
}
```

### Pipelines_CreateOrUpdate

```java
import com.azure.resourcemanager.devops.models.BootstrapConfiguration;
import com.azure.resourcemanager.devops.models.OrganizationReference;
import com.azure.resourcemanager.devops.models.PipelineTemplate;
import com.azure.resourcemanager.devops.models.ProjectReference;
import java.util.HashMap;
import java.util.Map;

/** Samples for Pipelines CreateOrUpdate. */
public final class PipelinesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devops/resource-manager/Microsoft.DevOps/preview/2019-07-01-preview/examples/CreateAzurePipeline-Sample-AspNet-WindowsWebApp.json
     */
    /**
     * Sample code: Create an Azure pipeline to deploy a sample ASP.Net application to Azure web-app.
     *
     * @param manager Entry point to DevopsManager.
     */
    public static void createAnAzurePipelineToDeployASampleASPNetApplicationToAzureWebApp(
        com.azure.resourcemanager.devops.DevopsManager manager) {
        manager
            .pipelines()
            .define("myAspNetWebAppPipeline")
            .withRegion("South India")
            .withExistingResourceGroup("myAspNetWebAppPipeline-rg")
            .withOrganization(new OrganizationReference().withName("myAspNetWebAppPipeline-org"))
            .withProject(new ProjectReference().withName("myAspNetWebAppPipeline-project"))
            .withBootstrapConfiguration(
                new BootstrapConfiguration()
                    .withTemplate(
                        new PipelineTemplate()
                            .withId("ms.vss-continuous-delivery-pipeline-templates.aspnet-windowswebapp")
                            .withParameters(
                                mapOf(
                                    "appInsightLocation",
                                    "South India",
                                    "appServicePlan",
                                    "S1 Standard",
                                    "azureAuth",
                                    "{\"scheme\":\"ServicePrincipal\",\"parameters\":{\"tenantid\":\"{subscriptionTenantId}\",\"objectid\":\"{appObjectId}\",\"serviceprincipalid\":\"{appId}\",\"serviceprincipalkey\":\"{appSecret}\"}}",
                                    "location",
                                    "South India",
                                    "resourceGroup",
                                    "myAspNetWebAppPipeline-rg",
                                    "subscriptionId",
                                    "{subscriptionId}",
                                    "webAppName",
                                    "myAspNetWebApp"))))
            .withTags(mapOf())
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

### Pipelines_Delete

```java
import com.azure.core.util.Context;

/** Samples for Pipelines Delete. */
public final class PipelinesDeleteSamples {
    /*
     * x-ms-original-file: specification/devops/resource-manager/Microsoft.DevOps/preview/2019-07-01-preview/examples/DeleteAzurePipeline.json
     */
    /**
     * Sample code: Get an existing Azure pipeline.
     *
     * @param manager Entry point to DevopsManager.
     */
    public static void getAnExistingAzurePipeline(com.azure.resourcemanager.devops.DevopsManager manager) {
        manager.pipelines().deleteWithResponse("myAspNetWebAppPipeline-rg", "myAspNetWebAppPipeline", Context.NONE);
    }
}
```

### Pipelines_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Pipelines GetByResourceGroup. */
public final class PipelinesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/devops/resource-manager/Microsoft.DevOps/preview/2019-07-01-preview/examples/GetAzurePipeline.json
     */
    /**
     * Sample code: Get an existing Azure pipeline.
     *
     * @param manager Entry point to DevopsManager.
     */
    public static void getAnExistingAzurePipeline(com.azure.resourcemanager.devops.DevopsManager manager) {
        manager
            .pipelines()
            .getByResourceGroupWithResponse("myAspNetWebAppPipeline-rg", "myAspNetWebAppPipeline", Context.NONE);
    }
}
```

### Pipelines_List

```java
import com.azure.core.util.Context;

/** Samples for Pipelines List. */
public final class PipelinesListSamples {
    /*
     * x-ms-original-file: specification/devops/resource-manager/Microsoft.DevOps/preview/2019-07-01-preview/examples/ListAzurePipelinesBySubscription.json
     */
    /**
     * Sample code: List all Azure pipelines under the specified subscription.
     *
     * @param manager Entry point to DevopsManager.
     */
    public static void listAllAzurePipelinesUnderTheSpecifiedSubscription(
        com.azure.resourcemanager.devops.DevopsManager manager) {
        manager.pipelines().list(Context.NONE);
    }
}
```

### Pipelines_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Pipelines ListByResourceGroup. */
public final class PipelinesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/devops/resource-manager/Microsoft.DevOps/preview/2019-07-01-preview/examples/ListAzurePipelinesByResourceGroup.json
     */
    /**
     * Sample code: List all Azure Pipelines under the specified resource group.
     *
     * @param manager Entry point to DevopsManager.
     */
    public static void listAllAzurePipelinesUnderTheSpecifiedResourceGroup(
        com.azure.resourcemanager.devops.DevopsManager manager) {
        manager.pipelines().listByResourceGroup("myAspNetWebAppPipeline-rg", Context.NONE);
    }
}
```

### Pipelines_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.devops.models.Pipeline;
import java.util.HashMap;
import java.util.Map;

/** Samples for Pipelines Update. */
public final class PipelinesUpdateSamples {
    /*
     * x-ms-original-file: specification/devops/resource-manager/Microsoft.DevOps/preview/2019-07-01-preview/examples/UpdateAzurePipeline.json
     */
    /**
     * Sample code: Get an existing Azure pipeline.
     *
     * @param manager Entry point to DevopsManager.
     */
    public static void getAnExistingAzurePipeline(com.azure.resourcemanager.devops.DevopsManager manager) {
        Pipeline resource =
            manager
                .pipelines()
                .getByResourceGroupWithResponse("myAspNetWebAppPipeline-rg", "myAspNetWebAppPipeline", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tagKey", "tagvalue")).apply();
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

