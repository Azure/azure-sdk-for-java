# Code snippets and samples


## AzureDevOpsConnector

- [CreateOrUpdate](#azuredevopsconnector_createorupdate)
- [Delete](#azuredevopsconnector_delete)
- [GetByResourceGroup](#azuredevopsconnector_getbyresourcegroup)
- [List](#azuredevopsconnector_list)
- [ListByResourceGroup](#azuredevopsconnector_listbyresourcegroup)
- [Update](#azuredevopsconnector_update)

## AzureDevOpsConnectorStatsOperation

- [Get](#azuredevopsconnectorstatsoperation_get)

## AzureDevOpsOrg

- [CreateOrUpdate](#azuredevopsorg_createorupdate)
- [Get](#azuredevopsorg_get)
- [List](#azuredevopsorg_list)
- [Update](#azuredevopsorg_update)

## AzureDevOpsProject

- [CreateOrUpdate](#azuredevopsproject_createorupdate)
- [Get](#azuredevopsproject_get)
- [List](#azuredevopsproject_list)
- [Update](#azuredevopsproject_update)

## AzureDevOpsRepo

- [CreateOrUpdate](#azuredevopsrepo_createorupdate)
- [Get](#azuredevopsrepo_get)
- [List](#azuredevopsrepo_list)
- [ListByConnector](#azuredevopsrepo_listbyconnector)
- [Update](#azuredevopsrepo_update)

## GitHubConnector

- [CreateOrUpdate](#githubconnector_createorupdate)
- [Delete](#githubconnector_delete)
- [GetByResourceGroup](#githubconnector_getbyresourcegroup)
- [List](#githubconnector_list)
- [ListByResourceGroup](#githubconnector_listbyresourcegroup)
- [Update](#githubconnector_update)

## GitHubConnectorStatsOperation

- [Get](#githubconnectorstatsoperation_get)

## GitHubOwner

- [CreateOrUpdate](#githubowner_createorupdate)
- [Get](#githubowner_get)
- [List](#githubowner_list)
- [Update](#githubowner_update)

## GitHubRepo

- [CreateOrUpdate](#githubrepo_createorupdate)
- [Get](#githubrepo_get)
- [List](#githubrepo_list)
- [ListByConnector](#githubrepo_listbyconnector)
- [Update](#githubrepo_update)

## Operations

- [List](#operations_list)
### AzureDevOpsConnector_CreateOrUpdate

```java
import com.azure.resourcemanager.securitydevops.models.AuthorizationInfo;
import com.azure.resourcemanager.securitydevops.models.AzureDevOpsConnectorProperties;
import com.azure.resourcemanager.securitydevops.models.AzureDevOpsOrgMetadata;
import com.azure.resourcemanager.securitydevops.models.AzureDevOpsProjectMetadata;
import java.util.Arrays;

/** Samples for AzureDevOpsConnector CreateOrUpdate. */
public final class AzureDevOpsConnectorCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/AzureDevOpsConnectorCreateOrUpdate.json
     */
    /**
     * Sample code: AzureDevOpsConnector_CreateOrUpdate.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void azureDevOpsConnectorCreateOrUpdate(
        com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager
            .azureDevOpsConnectors()
            .define("testconnector")
            .withRegion("West US")
            .withExistingResourceGroup("westusrg")
            .withProperties(
                new AzureDevOpsConnectorProperties()
                    .withAuthorization(new AuthorizationInfo().withCode("00000000000000000000"))
                    .withOrgs(
                        Arrays
                            .asList(
                                new AzureDevOpsOrgMetadata()
                                    .withName("testOrg")
                                    .withProjects(
                                        Arrays
                                            .asList(
                                                new AzureDevOpsProjectMetadata()
                                                    .withName("testProject")
                                                    .withRepos(Arrays.asList("testRepo")))))))
            .create();
    }
}
```

### AzureDevOpsConnector_Delete

```java
import com.azure.core.util.Context;

/** Samples for AzureDevOpsConnector Delete. */
public final class AzureDevOpsConnectorDeleteSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/AzureDevOpsConnectorDelete.json
     */
    /**
     * Sample code: AzureDevOpsConnector_Delete.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void azureDevOpsConnectorDelete(
        com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager.azureDevOpsConnectors().delete("westusrg", "testconnector", Context.NONE);
    }
}
```

### AzureDevOpsConnector_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for AzureDevOpsConnector GetByResourceGroup. */
public final class AzureDevOpsConnectorGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/AzureDevOpsConnectorGet.json
     */
    /**
     * Sample code: AzureDevOpsConnector_Get.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void azureDevOpsConnectorGet(com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager.azureDevOpsConnectors().getByResourceGroupWithResponse("westusrg", "testconnector", Context.NONE);
    }
}
```

### AzureDevOpsConnector_List

```java
import com.azure.core.util.Context;

/** Samples for AzureDevOpsConnector List. */
public final class AzureDevOpsConnectorListSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/AzureDevOpsConnectorListBySubscription.json
     */
    /**
     * Sample code: AzureDevOpsConnector_ListBySubscription.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void azureDevOpsConnectorListBySubscription(
        com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager.azureDevOpsConnectors().list(Context.NONE);
    }
}
```

### AzureDevOpsConnector_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for AzureDevOpsConnector ListByResourceGroup. */
public final class AzureDevOpsConnectorListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/AzureDevOpsConnectorListByResourceGroup.json
     */
    /**
     * Sample code: AzureDevOpsConnector_ListByResourceGroup.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void azureDevOpsConnectorListByResourceGroup(
        com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager.azureDevOpsConnectors().listByResourceGroup("westusrg", Context.NONE);
    }
}
```

### AzureDevOpsConnector_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.securitydevops.models.AzureDevOpsConnector;
import java.util.HashMap;
import java.util.Map;

/** Samples for AzureDevOpsConnector Update. */
public final class AzureDevOpsConnectorUpdateSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/AzureDevOpsConnectorUpdate.json
     */
    /**
     * Sample code: AzureDevOpsConnector_Update.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void azureDevOpsConnectorUpdate(
        com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        AzureDevOpsConnector resource =
            manager
                .azureDevOpsConnectors()
                .getByResourceGroupWithResponse("westusrg", "testconnector", Context.NONE)
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

### AzureDevOpsConnectorStatsOperation_Get

```java
import com.azure.core.util.Context;

/** Samples for AzureDevOpsConnectorStatsOperation Get. */
public final class AzureDevOpsConnectorStatsOperationGetSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/AzureDevOpsConnectorStatsGet.json
     */
    /**
     * Sample code: AzureDevOpsConnectorStats_Get.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void azureDevOpsConnectorStatsGet(
        com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager.azureDevOpsConnectorStatsOperations().getWithResponse("westusrg", "testconnector", Context.NONE);
    }
}
```

### AzureDevOpsOrg_CreateOrUpdate

```java
import com.azure.resourcemanager.securitydevops.models.AutoDiscovery;
import com.azure.resourcemanager.securitydevops.models.AzureDevOpsOrgProperties;

/** Samples for AzureDevOpsOrg CreateOrUpdate. */
public final class AzureDevOpsOrgCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/AzureDevOpsOrgCreateOrUpdate.json
     */
    /**
     * Sample code: AzureDevOpsOrg_CreateOrUpdate.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void azureDevOpsOrgCreateOrUpdate(
        com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager
            .azureDevOpsOrgs()
            .define("myOrg")
            .withExistingAzureDevOpsConnector("westusrg", "testconnector")
            .withProperties(new AzureDevOpsOrgProperties().withAutoDiscovery(AutoDiscovery.DISABLED))
            .create();
    }
}
```

### AzureDevOpsOrg_Get

```java
import com.azure.core.util.Context;

/** Samples for AzureDevOpsOrg Get. */
public final class AzureDevOpsOrgGetSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/AzureDevOpsOrgGet.json
     */
    /**
     * Sample code: AzureDevOpsOrg_Get.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void azureDevOpsOrgGet(com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager.azureDevOpsOrgs().getWithResponse("westusrg", "testconnector", "myOrg", Context.NONE);
    }
}
```

### AzureDevOpsOrg_List

```java
import com.azure.core.util.Context;

/** Samples for AzureDevOpsOrg List. */
public final class AzureDevOpsOrgListSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/AzureDevOpsOrgList.json
     */
    /**
     * Sample code: AzureDevOpsOrg_List.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void azureDevOpsOrgList(com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager.azureDevOpsOrgs().list("westusrg", "testconnector", Context.NONE);
    }
}
```

### AzureDevOpsOrg_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.securitydevops.models.AutoDiscovery;
import com.azure.resourcemanager.securitydevops.models.AzureDevOpsOrg;
import com.azure.resourcemanager.securitydevops.models.AzureDevOpsOrgProperties;

/** Samples for AzureDevOpsOrg Update. */
public final class AzureDevOpsOrgUpdateSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/AzureDevOpsOrgUpdate.json
     */
    /**
     * Sample code: AzureDevOpsOrg_Update.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void azureDevOpsOrgUpdate(com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        AzureDevOpsOrg resource =
            manager.azureDevOpsOrgs().getWithResponse("westusrg", "testconnector", "myOrg", Context.NONE).getValue();
        resource
            .update()
            .withProperties(new AzureDevOpsOrgProperties().withAutoDiscovery(AutoDiscovery.DISABLED))
            .apply();
    }
}
```

### AzureDevOpsProject_CreateOrUpdate

```java
import com.azure.resourcemanager.securitydevops.models.AutoDiscovery;
import com.azure.resourcemanager.securitydevops.models.AzureDevOpsProjectProperties;

/** Samples for AzureDevOpsProject CreateOrUpdate. */
public final class AzureDevOpsProjectCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/AzureDevOpsProjectCreateOrUpdate.json
     */
    /**
     * Sample code: AzureDevOpsProject_CreateOrUpdate.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void azureDevOpsProjectCreateOrUpdate(
        com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager
            .azureDevOpsProjects()
            .define("myProject")
            .withExistingOrg("westusrg", "testconnector", "myOrg")
            .withProperties(new AzureDevOpsProjectProperties().withAutoDiscovery(AutoDiscovery.DISABLED))
            .create();
    }
}
```

### AzureDevOpsProject_Get

```java
import com.azure.core.util.Context;

/** Samples for AzureDevOpsProject Get. */
public final class AzureDevOpsProjectGetSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/AzureDevOpsProjectGet.json
     */
    /**
     * Sample code: AzureDevOpsProject_Get.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void azureDevOpsProjectGet(com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager.azureDevOpsProjects().getWithResponse("westusrg", "testconnector", "myOrg", "myProject", Context.NONE);
    }
}
```

### AzureDevOpsProject_List

```java
import com.azure.core.util.Context;

/** Samples for AzureDevOpsProject List. */
public final class AzureDevOpsProjectListSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/AzureDevOpsProjectList.json
     */
    /**
     * Sample code: AzureDevOpsProject_List.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void azureDevOpsProjectList(com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager.azureDevOpsProjects().list("westusrg", "testconnector", "myOrg", Context.NONE);
    }
}
```

### AzureDevOpsProject_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.securitydevops.models.AutoDiscovery;
import com.azure.resourcemanager.securitydevops.models.AzureDevOpsProject;
import com.azure.resourcemanager.securitydevops.models.AzureDevOpsProjectProperties;

/** Samples for AzureDevOpsProject Update. */
public final class AzureDevOpsProjectUpdateSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/AzureDevOpsProjectUpdate.json
     */
    /**
     * Sample code: AzureDevOpsProject_Update.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void azureDevOpsProjectUpdate(
        com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        AzureDevOpsProject resource =
            manager
                .azureDevOpsProjects()
                .getWithResponse("westusrg", "testconnector", "myOrg", "myProject", Context.NONE)
                .getValue();
        resource
            .update()
            .withProperties(new AzureDevOpsProjectProperties().withAutoDiscovery(AutoDiscovery.DISABLED))
            .apply();
    }
}
```

### AzureDevOpsRepo_CreateOrUpdate

```java
import com.azure.resourcemanager.securitydevops.models.ActionableRemediation;
import com.azure.resourcemanager.securitydevops.models.ActionableRemediationState;
import com.azure.resourcemanager.securitydevops.models.AzureDevOpsRepoProperties;
import com.azure.resourcemanager.securitydevops.models.RuleCategory;
import com.azure.resourcemanager.securitydevops.models.TargetBranchConfiguration;
import java.util.Arrays;

/** Samples for AzureDevOpsRepo CreateOrUpdate. */
public final class AzureDevOpsRepoCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/AzureDevOpsRepoCreateOrUpdate.json
     */
    /**
     * Sample code: AzureDevOpsRepo_CreateOrUpdate.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void azureDevOpsRepoCreateOrUpdate(
        com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager
            .azureDevOpsRepoes()
            .define("myRepo")
            .withExistingProject("westusrg", "testconnector", "myOrg", "myProject")
            .withProperties(
                new AzureDevOpsRepoProperties()
                    .withRepoId("00000000-0000-0000-0000-000000000000")
                    .withRepoUrl("https://dev.azure.com/myOrg/myProject/_git/myRepo")
                    .withActionableRemediation(
                        new ActionableRemediation()
                            .withState(ActionableRemediationState.ENABLED)
                            .withSeverityLevels(Arrays.asList("High"))
                            .withCategories(Arrays.asList(RuleCategory.SECRETS))
                            .withBranchConfiguration(new TargetBranchConfiguration().withNames(Arrays.asList("main")))))
            .create();
    }
}
```

### AzureDevOpsRepo_Get

```java
import com.azure.core.util.Context;

/** Samples for AzureDevOpsRepo Get. */
public final class AzureDevOpsRepoGetSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/AzureDevOpsRepoGet.json
     */
    /**
     * Sample code: AzureDevOpsRepo_Get.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void azureDevOpsRepoGet(com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager
            .azureDevOpsRepoes()
            .getWithResponse("westusrg", "testconnector", "myOrg", "myProject", "myRepo", Context.NONE);
    }
}
```

### AzureDevOpsRepo_List

```java
import com.azure.core.util.Context;

/** Samples for AzureDevOpsRepo List. */
public final class AzureDevOpsRepoListSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/AzureDevOpsRepoList.json
     */
    /**
     * Sample code: AzureDevOpsRepo_List.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void azureDevOpsRepoList(com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager.azureDevOpsRepoes().list("westusrg", "testconnector", "myOrg", "myProject", Context.NONE);
    }
}
```

### AzureDevOpsRepo_ListByConnector

```java
import com.azure.core.util.Context;

/** Samples for AzureDevOpsRepo ListByConnector. */
public final class AzureDevOpsRepoListByConnectorSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/AzureDevOpsRepoListByConnector.json
     */
    /**
     * Sample code: AzureDevOpsRepo_ListByConnector.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void azureDevOpsRepoListByConnector(
        com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager.azureDevOpsRepoes().listByConnector("westusrg", "testconnector", Context.NONE);
    }
}
```

### AzureDevOpsRepo_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.securitydevops.models.AzureDevOpsRepo;

/** Samples for AzureDevOpsRepo Update. */
public final class AzureDevOpsRepoUpdateSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/AzureDevOpsRepoUpdate.json
     */
    /**
     * Sample code: AzureDevOpsRepo_Update.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void azureDevOpsRepoUpdate(com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        AzureDevOpsRepo resource =
            manager
                .azureDevOpsRepoes()
                .getWithResponse("westusrg", "testconnector", "myOrg", "myProject", "myRepo", Context.NONE)
                .getValue();
        resource.update().apply();
    }
}
```

### GitHubConnector_CreateOrUpdate

```java
import com.azure.resourcemanager.securitydevops.models.GitHubConnectorProperties;

/** Samples for GitHubConnector CreateOrUpdate. */
public final class GitHubConnectorCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/GitHubConnectorCreateOrUpdate.json
     */
    /**
     * Sample code: GitHubConnector_CreateOrUpdate.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void gitHubConnectorCreateOrUpdate(
        com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager
            .gitHubConnectors()
            .define("testconnector")
            .withRegion("West US")
            .withExistingResourceGroup("westusrg")
            .withProperties(new GitHubConnectorProperties().withCode("00000000000000000000"))
            .create();
    }
}
```

### GitHubConnector_Delete

```java
import com.azure.core.util.Context;

/** Samples for GitHubConnector Delete. */
public final class GitHubConnectorDeleteSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/GitHubConnectorDelete.json
     */
    /**
     * Sample code: GitHubConnector_Delete.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void gitHubConnectorDelete(com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager.gitHubConnectors().delete("westusrg", "testconnector", Context.NONE);
    }
}
```

### GitHubConnector_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for GitHubConnector GetByResourceGroup. */
public final class GitHubConnectorGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/GitHubConnectorGet.json
     */
    /**
     * Sample code: GitHubConnector_Get.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void gitHubConnectorGet(com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager.gitHubConnectors().getByResourceGroupWithResponse("westusrg", "testconnector", Context.NONE);
    }
}
```

### GitHubConnector_List

```java
import com.azure.core.util.Context;

/** Samples for GitHubConnector List. */
public final class GitHubConnectorListSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/GitHubConnectorListBySubscription.json
     */
    /**
     * Sample code: GitHubConnector_ListBySubscription.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void gitHubConnectorListBySubscription(
        com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager.gitHubConnectors().list(Context.NONE);
    }
}
```

### GitHubConnector_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for GitHubConnector ListByResourceGroup. */
public final class GitHubConnectorListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/GitHubConnectorListByResourceGroup.json
     */
    /**
     * Sample code: GitHubConnector_ListByResourceGroup.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void gitHubConnectorListByResourceGroup(
        com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager.gitHubConnectors().listByResourceGroup("westusrg", Context.NONE);
    }
}
```

### GitHubConnector_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.securitydevops.models.GitHubConnector;
import java.util.HashMap;
import java.util.Map;

/** Samples for GitHubConnector Update. */
public final class GitHubConnectorUpdateSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/GitHubConnectorUpdate.json
     */
    /**
     * Sample code: GitHubConnector_Update.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void gitHubConnectorUpdate(com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        GitHubConnector resource =
            manager
                .gitHubConnectors()
                .getByResourceGroupWithResponse("westusrg", "testconnector", Context.NONE)
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

### GitHubConnectorStatsOperation_Get

```java
import com.azure.core.util.Context;

/** Samples for GitHubConnectorStatsOperation Get. */
public final class GitHubConnectorStatsOperationGetSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/GitHubConnectorStatsGet.json
     */
    /**
     * Sample code: GitHubConnectorStats_Get.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void gitHubConnectorStatsGet(com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager.gitHubConnectorStatsOperations().getWithResponse("westusrg", "testconnector", Context.NONE);
    }
}
```

### GitHubOwner_CreateOrUpdate

```java
/** Samples for GitHubOwner CreateOrUpdate. */
public final class GitHubOwnerCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/GitHubOwnerCreateOrUpdate.json
     */
    /**
     * Sample code: GitHubOwner_CreateOrUpdate.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void gitHubOwnerCreateOrUpdate(
        com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager.gitHubOwners().define("Azure").withExistingGitHubConnector("westusrg", "testconnector").create();
    }
}
```

### GitHubOwner_Get

```java
import com.azure.core.util.Context;

/** Samples for GitHubOwner Get. */
public final class GitHubOwnerGetSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/GitHubOwnerGet.json
     */
    /**
     * Sample code: GitHubOwner_Get.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void gitHubOwnerGet(com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager.gitHubOwners().getWithResponse("westusrg", "testconnector", "Azure", Context.NONE);
    }
}
```

### GitHubOwner_List

```java
import com.azure.core.util.Context;

/** Samples for GitHubOwner List. */
public final class GitHubOwnerListSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/GitHubOwnerList.json
     */
    /**
     * Sample code: GitHubOwner_List.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void gitHubOwnerList(com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager.gitHubOwners().list("westusrg", "testconnector", Context.NONE);
    }
}
```

### GitHubOwner_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.securitydevops.models.GitHubOwner;

/** Samples for GitHubOwner Update. */
public final class GitHubOwnerUpdateSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/GitHubOwnerUpdate.json
     */
    /**
     * Sample code: GitHubOwner_Update.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void gitHubOwnerUpdate(com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        GitHubOwner resource =
            manager.gitHubOwners().getWithResponse("westusrg", "testconnector", "Azure", Context.NONE).getValue();
        resource.update().apply();
    }
}
```

### GitHubRepo_CreateOrUpdate

```java
/** Samples for GitHubRepo CreateOrUpdate. */
public final class GitHubRepoCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/GitHubRepoCreateOrUpdate.json
     */
    /**
     * Sample code: GitHubRepo_CreateOrUpdate.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void gitHubRepoCreateOrUpdate(
        com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager
            .gitHubRepoes()
            .define("azure-rest-api-specs")
            .withExistingOwner("westusrg", "testconnector", "Azure")
            .create();
    }
}
```

### GitHubRepo_Get

```java
import com.azure.core.util.Context;

/** Samples for GitHubRepo Get. */
public final class GitHubRepoGetSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/GitHubRepoGet.json
     */
    /**
     * Sample code: GitHubRepo_Get.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void gitHubRepoGet(com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager.gitHubRepoes().getWithResponse("westusrg", "testconnector", "Azure", "39093389", Context.NONE);
    }
}
```

### GitHubRepo_List

```java
import com.azure.core.util.Context;

/** Samples for GitHubRepo List. */
public final class GitHubRepoListSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/GitHubRepoList.json
     */
    /**
     * Sample code: GitHubRepo_List.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void gitHubRepoList(com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager.gitHubRepoes().list("westusrg", "testconnector", "Azure", Context.NONE);
    }
}
```

### GitHubRepo_ListByConnector

```java
import com.azure.core.util.Context;

/** Samples for GitHubRepo ListByConnector. */
public final class GitHubRepoListByConnectorSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/GitHubRepoListByConnector.json
     */
    /**
     * Sample code: GitHubRepo_ListByConnector.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void gitHubRepoListByConnector(
        com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager.gitHubRepoes().listByConnector("westusrg", "testconnector", Context.NONE);
    }
}
```

### GitHubRepo_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.securitydevops.models.GitHubRepo;

/** Samples for GitHubRepo Update. */
public final class GitHubRepoUpdateSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/GitHubRepoUpdate.json
     */
    /**
     * Sample code: GitHubRepo_Update.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void gitHubRepoUpdate(com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        GitHubRepo resource =
            manager
                .gitHubRepoes()
                .getWithResponse("westusrg", "testconnector", "Azure", "azure-rest-api-specs", Context.NONE)
                .getValue();
        resource.update().apply();
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/securitydevops/resource-manager/Microsoft.SecurityDevOps/preview/2022-09-01-preview/examples/OperationsList.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to SecuritydevopsManager.
     */
    public static void operationsList(com.azure.resourcemanager.securitydevops.SecuritydevopsManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

