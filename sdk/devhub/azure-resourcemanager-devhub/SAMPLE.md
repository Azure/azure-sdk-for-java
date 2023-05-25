# Code snippets and samples


## Operations

- [List](#operations_list)

## ResourceProvider

- [GeneratePreviewArtifacts](#resourceprovider_generatepreviewartifacts)
- [GitHubOAuth](#resourceprovider_githuboauth)
- [GitHubOAuthCallback](#resourceprovider_githuboauthcallback)
- [ListGitHubOAuth](#resourceprovider_listgithuboauth)

## Workflow

- [CreateOrUpdate](#workflow_createorupdate)
- [Delete](#workflow_delete)
- [GetByResourceGroup](#workflow_getbyresourcegroup)
- [List](#workflow_list)
- [ListByResourceGroup](#workflow_listbyresourcegroup)
- [UpdateTags](#workflow_updatetags)
### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/developerhub/resource-manager/Microsoft.DevHub/preview/2022-10-11-preview/examples/Operation_List.json
     */
    /**
     * Sample code: List available operations for the container service resource provider.
     *
     * @param manager Entry point to DevHubManager.
     */
    public static void listAvailableOperationsForTheContainerServiceResourceProvider(
        com.azure.resourcemanager.devhub.DevHubManager manager) {
        manager.operations().listWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_GeneratePreviewArtifacts

```java
import com.azure.resourcemanager.devhub.fluent.models.ArtifactGenerationProperties;
import com.azure.resourcemanager.devhub.models.DockerfileGenerationMode;
import com.azure.resourcemanager.devhub.models.GenerationLanguage;
import com.azure.resourcemanager.devhub.models.GenerationManifestType;
import com.azure.resourcemanager.devhub.models.ManifestGenerationMode;

/** Samples for ResourceProvider GeneratePreviewArtifacts. */
public final class ResourceProviderGeneratePreviewArtifactsSamples {
    /*
     * x-ms-original-file: specification/developerhub/resource-manager/Microsoft.DevHub/preview/2022-10-11-preview/examples/GeneratePreviewArtifacts.json
     */
    /**
     * Sample code: Artifact Generation Properties.
     *
     * @param manager Entry point to DevHubManager.
     */
    public static void artifactGenerationProperties(com.azure.resourcemanager.devhub.DevHubManager manager) {
        manager
            .resourceProviders()
            .generatePreviewArtifactsWithResponse(
                "location1",
                new ArtifactGenerationProperties()
                    .withGenerationLanguage(GenerationLanguage.JAVASCRIPT)
                    .withLanguageVersion("14")
                    .withPort("80")
                    .withAppName("my-app")
                    .withDockerfileOutputDirectory("./")
                    .withManifestOutputDirectory("./")
                    .withDockerfileGenerationMode(DockerfileGenerationMode.ENABLED)
                    .withManifestGenerationMode(ManifestGenerationMode.ENABLED)
                    .withManifestType(GenerationManifestType.KUBE)
                    .withImageName("myimage")
                    .withNamespace("my-namespace")
                    .withImageTag("latest"),
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_GitHubOAuth

```java
import com.azure.resourcemanager.devhub.models.GitHubOAuthCallRequest;

/** Samples for ResourceProvider GitHubOAuth. */
public final class ResourceProviderGitHubOAuthSamples {
    /*
     * x-ms-original-file: specification/developerhub/resource-manager/Microsoft.DevHub/preview/2022-10-11-preview/examples/GitHubOAuth.json
     */
    /**
     * Sample code: GitHub OAuth.
     *
     * @param manager Entry point to DevHubManager.
     */
    public static void gitHubOAuth(com.azure.resourcemanager.devhub.DevHubManager manager) {
        manager
            .resourceProviders()
            .gitHubOAuthWithResponse(
                "eastus2euap",
                new GitHubOAuthCallRequest().withRedirectUrl("https://ms.portal.azure.com/aks"),
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_GitHubOAuthCallback

```java
/** Samples for ResourceProvider GitHubOAuthCallback. */
public final class ResourceProviderGitHubOAuthCallbackSamples {
    /*
     * x-ms-original-file: specification/developerhub/resource-manager/Microsoft.DevHub/preview/2022-10-11-preview/examples/GitHubOAuthCallback.json
     */
    /**
     * Sample code: GitHub OAuth Callback.
     *
     * @param manager Entry point to DevHubManager.
     */
    public static void gitHubOAuthCallback(com.azure.resourcemanager.devhub.DevHubManager manager) {
        manager
            .resourceProviders()
            .gitHubOAuthCallbackWithResponse(
                "eastus2euap",
                "3584d83530557fdd1f46af8289938c8ef79f9dc5",
                "12345678-3456-7890-5678-012345678901",
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_ListGitHubOAuth

```java
/** Samples for ResourceProvider ListGitHubOAuth. */
public final class ResourceProviderListGitHubOAuthSamples {
    /*
     * x-ms-original-file: specification/developerhub/resource-manager/Microsoft.DevHub/preview/2022-10-11-preview/examples/GitHubOAuth_List.json
     */
    /**
     * Sample code: List GitHub OAuth.
     *
     * @param manager Entry point to DevHubManager.
     */
    public static void listGitHubOAuth(com.azure.resourcemanager.devhub.DevHubManager manager) {
        manager.resourceProviders().listGitHubOAuthWithResponse("eastus2euap", com.azure.core.util.Context.NONE);
    }
}
```

### Workflow_CreateOrUpdate

```java
import com.azure.resourcemanager.devhub.models.Acr;
import com.azure.resourcemanager.devhub.models.DeploymentProperties;
import com.azure.resourcemanager.devhub.models.DockerfileGenerationMode;
import com.azure.resourcemanager.devhub.models.GenerationLanguage;
import com.azure.resourcemanager.devhub.models.GenerationManifestType;
import com.azure.resourcemanager.devhub.models.GitHubWorkflowProfileOidcCredentials;
import com.azure.resourcemanager.devhub.models.ManifestGenerationMode;
import com.azure.resourcemanager.devhub.models.ManifestType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Workflow CreateOrUpdate. */
public final class WorkflowCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/developerhub/resource-manager/Microsoft.DevHub/preview/2022-10-11-preview/examples/Workflow_CreateOrUpdate.json
     */
    /**
     * Sample code: Create Workflow.
     *
     * @param manager Entry point to DevHubManager.
     */
    public static void createWorkflow(com.azure.resourcemanager.devhub.DevHubManager manager) {
        manager
            .workflows()
            .define("workflow1")
            .withRegion("location1")
            .withExistingResourceGroup("resourceGroup1")
            .withTags(mapOf("appname", "testApp"))
            .withRepositoryOwner("owner1")
            .withRepositoryName("repo1")
            .withBranchName("branch1")
            .withDockerfile("repo1/images/Dockerfile")
            .withDockerBuildContext("repo1/src/")
            .withDeploymentProperties(
                new DeploymentProperties()
                    .withManifestType(ManifestType.KUBE)
                    .withKubeManifestLocations(Arrays.asList("/src/manifests/"))
                    .withOverrides(mapOf("key1", "value1")))
            .withNamespace("namespace1")
            .withAcr(
                new Acr()
                    .withAcrSubscriptionId("subscriptionId1")
                    .withAcrResourceGroup("resourceGroup1")
                    .withAcrRegistryName("registry1")
                    .withAcrRepositoryName("repo1"))
            .withOidcCredentials(
                new GitHubWorkflowProfileOidcCredentials()
                    .withAzureClientId("12345678-3456-7890-5678-012345678901")
                    .withAzureTenantId("66666666-3456-7890-5678-012345678901"))
            .withAksResourceId(
                "/subscriptions/subscriptionId1/resourcegroups/resourceGroup1/providers/Microsoft.ContainerService/managedClusters/cluster1")
            .create();
    }

    /*
     * x-ms-original-file: specification/developerhub/resource-manager/Microsoft.DevHub/preview/2022-10-11-preview/examples/Workflow_CreateOrUpdate_WithArtifactGen.json
     */
    /**
     * Sample code: Create Workflow With Artifact Generation.
     *
     * @param manager Entry point to DevHubManager.
     */
    public static void createWorkflowWithArtifactGeneration(com.azure.resourcemanager.devhub.DevHubManager manager) {
        manager
            .workflows()
            .define("workflow1")
            .withRegion("location1")
            .withExistingResourceGroup("resourceGroup1")
            .withTags(mapOf("appname", "testApp"))
            .withRepositoryOwner("owner1")
            .withRepositoryName("repo1")
            .withBranchName("branch1")
            .withDockerfile("repo1/images/Dockerfile")
            .withDockerBuildContext("repo1/src/")
            .withDeploymentProperties(
                new DeploymentProperties()
                    .withManifestType(ManifestType.KUBE)
                    .withKubeManifestLocations(Arrays.asList("/src/manifests/"))
                    .withOverrides(mapOf("key1", "value1")))
            .withAcr(
                new Acr()
                    .withAcrSubscriptionId("subscriptionId1")
                    .withAcrResourceGroup("resourceGroup1")
                    .withAcrRegistryName("registry1")
                    .withAcrRepositoryName("repo1"))
            .withOidcCredentials(
                new GitHubWorkflowProfileOidcCredentials()
                    .withAzureClientId("12345678-3456-7890-5678-012345678901")
                    .withAzureTenantId("66666666-3456-7890-5678-012345678901"))
            .withAksResourceId(
                "/subscriptions/subscriptionId1/resourcegroups/resourceGroup1/providers/Microsoft.ContainerService/managedClusters/cluster1")
            .withGenerationLanguage(GenerationLanguage.JAVASCRIPT)
            .withLanguageVersion("14")
            .withPort("80")
            .withAppName("my-app")
            .withDockerfileOutputDirectory("./")
            .withManifestOutputDirectory("./")
            .withDockerfileGenerationMode(DockerfileGenerationMode.ENABLED)
            .withManifestGenerationMode(ManifestGenerationMode.ENABLED)
            .withManifestType(GenerationManifestType.KUBE)
            .withImageName("myimage")
            .withNamespaceArtifactGenerationPropertiesNamespace("my-namespace")
            .withImageTag("latest")
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

### Workflow_Delete

```java
/** Samples for Workflow Delete. */
public final class WorkflowDeleteSamples {
    /*
     * x-ms-original-file: specification/developerhub/resource-manager/Microsoft.DevHub/preview/2022-10-11-preview/examples/Workflow_Delete.json
     */
    /**
     * Sample code: Delete Workflow.
     *
     * @param manager Entry point to DevHubManager.
     */
    public static void deleteWorkflow(com.azure.resourcemanager.devhub.DevHubManager manager) {
        manager
            .workflows()
            .deleteByResourceGroupWithResponse("resourceGroup1", "workflow1", com.azure.core.util.Context.NONE);
    }
}
```

### Workflow_GetByResourceGroup

```java
/** Samples for Workflow GetByResourceGroup. */
public final class WorkflowGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/developerhub/resource-manager/Microsoft.DevHub/preview/2022-10-11-preview/examples/Workflow_Get.json
     */
    /**
     * Sample code: Get Workflow.
     *
     * @param manager Entry point to DevHubManager.
     */
    public static void getWorkflow(com.azure.resourcemanager.devhub.DevHubManager manager) {
        manager
            .workflows()
            .getByResourceGroupWithResponse("resourceGroup1", "workflow1", com.azure.core.util.Context.NONE);
    }
}
```

### Workflow_List

```java
/** Samples for Workflow List. */
public final class WorkflowListSamples {
    /*
     * x-ms-original-file: specification/developerhub/resource-manager/Microsoft.DevHub/preview/2022-10-11-preview/examples/Workflow_List.json
     */
    /**
     * Sample code: List Workflows.
     *
     * @param manager Entry point to DevHubManager.
     */
    public static void listWorkflows(com.azure.resourcemanager.devhub.DevHubManager manager) {
        manager.workflows().list(com.azure.core.util.Context.NONE);
    }
}
```

### Workflow_ListByResourceGroup

```java
/** Samples for Workflow ListByResourceGroup. */
public final class WorkflowListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/developerhub/resource-manager/Microsoft.DevHub/preview/2022-10-11-preview/examples/Workflow_ListByResourceGroup.json
     */
    /**
     * Sample code: List Workflows.
     *
     * @param manager Entry point to DevHubManager.
     */
    public static void listWorkflows(com.azure.resourcemanager.devhub.DevHubManager manager) {
        manager
            .workflows()
            .listByResourceGroup(
                "resourceGroup1",
                "/subscriptions/subscriptionId1/resourcegroups/resourceGroup1/providers/Microsoft.ContainerService/managedClusters/cluster1",
                com.azure.core.util.Context.NONE);
    }
}
```

### Workflow_UpdateTags

```java
import com.azure.resourcemanager.devhub.models.Workflow;
import java.util.HashMap;
import java.util.Map;

/** Samples for Workflow UpdateTags. */
public final class WorkflowUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/developerhub/resource-manager/Microsoft.DevHub/preview/2022-10-11-preview/examples/Workflow_UpdateTags.json
     */
    /**
     * Sample code: Update Managed Cluster Tags.
     *
     * @param manager Entry point to DevHubManager.
     */
    public static void updateManagedClusterTags(com.azure.resourcemanager.devhub.DevHubManager manager) {
        Workflow resource =
            manager
                .workflows()
                .getByResourceGroupWithResponse("resourceGroup1", "workflow1", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("promote", "false", "resourceEnv", "testing")).apply();
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

