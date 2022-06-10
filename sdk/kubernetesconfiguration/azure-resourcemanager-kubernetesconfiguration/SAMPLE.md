# Code snippets and samples


## Extensions

- [Create](#extensions_create)
- [Delete](#extensions_delete)
- [Get](#extensions_get)
- [List](#extensions_list)
- [Update](#extensions_update)

## FluxConfigOperationStatus

- [Get](#fluxconfigoperationstatus_get)

## FluxConfigurations

- [CreateOrUpdate](#fluxconfigurations_createorupdate)
- [Delete](#fluxconfigurations_delete)
- [Get](#fluxconfigurations_get)
- [List](#fluxconfigurations_list)
- [Update](#fluxconfigurations_update)

## OperationStatus

- [Get](#operationstatus_get)
- [List](#operationstatus_list)

## Operations

- [List](#operations_list)

## SourceControlConfigurations

- [CreateOrUpdate](#sourcecontrolconfigurations_createorupdate)
- [Delete](#sourcecontrolconfigurations_delete)
- [Get](#sourcecontrolconfigurations_get)
- [List](#sourcecontrolconfigurations_list)
### Extensions_Create

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kubernetesconfiguration.fluent.models.ExtensionInner;
import com.azure.resourcemanager.kubernetesconfiguration.models.Scope;
import com.azure.resourcemanager.kubernetesconfiguration.models.ScopeCluster;
import java.util.HashMap;
import java.util.Map;

/** Samples for Extensions Create. */
public final class ExtensionsCreateSamples {
    /*
     * x-ms-original-file: specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/stable/2022-03-01/examples/CreateExtension.json
     */
    /**
     * Sample code: Create Extension.
     *
     * @param manager Entry point to SourceControlConfigurationManager.
     */
    public static void createExtension(
        com.azure.resourcemanager.kubernetesconfiguration.SourceControlConfigurationManager manager) {
        manager
            .extensions()
            .create(
                "rg1",
                "Microsoft.Kubernetes",
                "connectedClusters",
                "clusterName1",
                "ClusterMonitor",
                new ExtensionInner()
                    .withExtensionType("azuremonitor-containers")
                    .withAutoUpgradeMinorVersion(true)
                    .withReleaseTrain("Preview")
                    .withScope(new Scope().withCluster(new ScopeCluster().withReleaseNamespace("kube-system")))
                    .withConfigurationSettings(
                        mapOf(
                            "omsagent.env.clusterName",
                            "clusterName1",
                            "omsagent.secret.wsid",
                            "a38cef99-5a89-52ed-b6db-22095c23664b"))
                    .withConfigurationProtectedSettings(mapOf("omsagent.secret.key", "secretKeyValue01")),
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

### Extensions_Delete

```java
import com.azure.core.util.Context;

/** Samples for Extensions Delete. */
public final class ExtensionsDeleteSamples {
    /*
     * x-ms-original-file: specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/stable/2022-03-01/examples/DeleteExtension.json
     */
    /**
     * Sample code: Delete Extension.
     *
     * @param manager Entry point to SourceControlConfigurationManager.
     */
    public static void deleteExtension(
        com.azure.resourcemanager.kubernetesconfiguration.SourceControlConfigurationManager manager) {
        manager
            .extensions()
            .delete(
                "rg1",
                "Microsoft.Kubernetes",
                "connectedClusters",
                "clusterName1",
                "ClusterMonitor",
                null,
                Context.NONE);
    }
}
```

### Extensions_Get

```java
import com.azure.core.util.Context;

/** Samples for Extensions Get. */
public final class ExtensionsGetSamples {
    /*
     * x-ms-original-file: specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/stable/2022-03-01/examples/GetExtension.json
     */
    /**
     * Sample code: Get Extension.
     *
     * @param manager Entry point to SourceControlConfigurationManager.
     */
    public static void getExtension(
        com.azure.resourcemanager.kubernetesconfiguration.SourceControlConfigurationManager manager) {
        manager
            .extensions()
            .getWithResponse(
                "rg1", "Microsoft.Kubernetes", "connectedClusters", "clusterName1", "ClusterMonitor", Context.NONE);
    }
}
```

### Extensions_List

```java
import com.azure.core.util.Context;

/** Samples for Extensions List. */
public final class ExtensionsListSamples {
    /*
     * x-ms-original-file: specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/stable/2022-03-01/examples/ListExtensions.json
     */
    /**
     * Sample code: List Extensions.
     *
     * @param manager Entry point to SourceControlConfigurationManager.
     */
    public static void listExtensions(
        com.azure.resourcemanager.kubernetesconfiguration.SourceControlConfigurationManager manager) {
        manager.extensions().list("rg1", "Microsoft.Kubernetes", "connectedClusters", "clusterName1", Context.NONE);
    }
}
```

### Extensions_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kubernetesconfiguration.models.PatchExtension;
import java.util.HashMap;
import java.util.Map;

/** Samples for Extensions Update. */
public final class ExtensionsUpdateSamples {
    /*
     * x-ms-original-file: specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/stable/2022-03-01/examples/PatchExtension.json
     */
    /**
     * Sample code: Update Extension.
     *
     * @param manager Entry point to SourceControlConfigurationManager.
     */
    public static void updateExtension(
        com.azure.resourcemanager.kubernetesconfiguration.SourceControlConfigurationManager manager) {
        manager
            .extensions()
            .update(
                "rg1",
                "Microsoft.Kubernetes",
                "connectedClusters",
                "clusterName1",
                "ClusterMonitor",
                new PatchExtension()
                    .withAutoUpgradeMinorVersion(true)
                    .withReleaseTrain("Preview")
                    .withConfigurationSettings(
                        mapOf(
                            "omsagent.env.clusterName",
                            "clusterName1",
                            "omsagent.secret.wsid",
                            "a38cef99-5a89-52ed-b6db-22095c23664b"))
                    .withConfigurationProtectedSettings(mapOf("omsagent.secret.key", "secretKeyValue01")),
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

### FluxConfigOperationStatus_Get

```java
import com.azure.core.util.Context;

/** Samples for FluxConfigOperationStatus Get. */
public final class FluxConfigOperationStatusGetSamples {
    /*
     * x-ms-original-file: specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/stable/2022-03-01/examples/GetFluxConfigurationAsyncOperationStatus.json
     */
    /**
     * Sample code: FluxConfigurationAsyncOperationStatus Get.
     *
     * @param manager Entry point to SourceControlConfigurationManager.
     */
    public static void fluxConfigurationAsyncOperationStatusGet(
        com.azure.resourcemanager.kubernetesconfiguration.SourceControlConfigurationManager manager) {
        manager
            .fluxConfigOperationStatus()
            .getWithResponse(
                "rg1",
                "Microsoft.Kubernetes",
                "connectedClusters",
                "clusterName1",
                "srs-fluxconfig",
                "99999999-9999-9999-9999-999999999999",
                Context.NONE);
    }
}
```

### FluxConfigurations_CreateOrUpdate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kubernetesconfiguration.fluent.models.FluxConfigurationInner;
import com.azure.resourcemanager.kubernetesconfiguration.models.BucketDefinition;
import com.azure.resourcemanager.kubernetesconfiguration.models.GitRepositoryDefinition;
import com.azure.resourcemanager.kubernetesconfiguration.models.KustomizationDefinition;
import com.azure.resourcemanager.kubernetesconfiguration.models.RepositoryRefDefinition;
import com.azure.resourcemanager.kubernetesconfiguration.models.ScopeType;
import com.azure.resourcemanager.kubernetesconfiguration.models.SourceKindType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for FluxConfigurations CreateOrUpdate. */
public final class FluxConfigurationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/stable/2022-03-01/examples/CreateFluxConfiguration.json
     */
    /**
     * Sample code: Create Flux Configuration.
     *
     * @param manager Entry point to SourceControlConfigurationManager.
     */
    public static void createFluxConfiguration(
        com.azure.resourcemanager.kubernetesconfiguration.SourceControlConfigurationManager manager) {
        manager
            .fluxConfigurations()
            .createOrUpdate(
                "rg1",
                "Microsoft.Kubernetes",
                "connectedClusters",
                "clusterName1",
                "srs-fluxconfig",
                new FluxConfigurationInner()
                    .withScope(ScopeType.CLUSTER)
                    .withNamespace("srs-namespace")
                    .withSourceKind(SourceKindType.GIT_REPOSITORY)
                    .withSuspend(false)
                    .withGitRepository(
                        new GitRepositoryDefinition()
                            .withUrl("https://github.com/Azure/arc-k8s-demo")
                            .withTimeoutInSeconds(600L)
                            .withSyncIntervalInSeconds(600L)
                            .withRepositoryRef(new RepositoryRefDefinition().withBranch("master"))
                            .withHttpsCACert("ZXhhbXBsZWNlcnRpZmljYXRl"))
                    .withKustomizations(
                        mapOf(
                            "srs-kustomization1",
                            new KustomizationDefinition()
                                .withPath("./test/path")
                                .withDependsOn(Arrays.asList())
                                .withTimeoutInSeconds(600L)
                                .withSyncIntervalInSeconds(600L),
                            "srs-kustomization2",
                            new KustomizationDefinition()
                                .withPath("./other/test/path")
                                .withDependsOn(Arrays.asList("srs-kustomization1"))
                                .withTimeoutInSeconds(600L)
                                .withSyncIntervalInSeconds(600L)
                                .withRetryIntervalInSeconds(600L)
                                .withPrune(false))),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/stable/2022-03-01/examples/CreateFluxConfigurationWithBucket.json
     */
    /**
     * Sample code: Create Flux Configuration with Bucket Source Kind.
     *
     * @param manager Entry point to SourceControlConfigurationManager.
     */
    public static void createFluxConfigurationWithBucketSourceKind(
        com.azure.resourcemanager.kubernetesconfiguration.SourceControlConfigurationManager manager) {
        manager
            .fluxConfigurations()
            .createOrUpdate(
                "rg1",
                "Microsoft.Kubernetes",
                "connectedClusters",
                "clusterName1",
                "srs-fluxconfig",
                new FluxConfigurationInner()
                    .withScope(ScopeType.CLUSTER)
                    .withNamespace("srs-namespace")
                    .withSourceKind(SourceKindType.BUCKET)
                    .withSuspend(false)
                    .withBucket(
                        new BucketDefinition()
                            .withUrl("https://fluxminiotest.az.minio.io")
                            .withBucketName("flux")
                            .withTimeoutInSeconds(1000L)
                            .withSyncIntervalInSeconds(1000L)
                            .withAccessKey("fluxminiotest"))
                    .withKustomizations(
                        mapOf(
                            "srs-kustomization1",
                            new KustomizationDefinition()
                                .withPath("./test/path")
                                .withDependsOn(Arrays.asList())
                                .withTimeoutInSeconds(600L)
                                .withSyncIntervalInSeconds(600L),
                            "srs-kustomization2",
                            new KustomizationDefinition()
                                .withPath("./other/test/path")
                                .withDependsOn(Arrays.asList("srs-kustomization1"))
                                .withTimeoutInSeconds(600L)
                                .withSyncIntervalInSeconds(600L)
                                .withRetryIntervalInSeconds(600L)
                                .withPrune(false))),
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

### FluxConfigurations_Delete

```java
import com.azure.core.util.Context;

/** Samples for FluxConfigurations Delete. */
public final class FluxConfigurationsDeleteSamples {
    /*
     * x-ms-original-file: specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/stable/2022-03-01/examples/DeleteFluxConfiguration.json
     */
    /**
     * Sample code: Delete Flux Configuration.
     *
     * @param manager Entry point to SourceControlConfigurationManager.
     */
    public static void deleteFluxConfiguration(
        com.azure.resourcemanager.kubernetesconfiguration.SourceControlConfigurationManager manager) {
        manager
            .fluxConfigurations()
            .delete(
                "rg1",
                "Microsoft.Kubernetes",
                "connectedClusters",
                "clusterName1",
                "srs-fluxconfig",
                null,
                Context.NONE);
    }
}
```

### FluxConfigurations_Get

```java
import com.azure.core.util.Context;

/** Samples for FluxConfigurations Get. */
public final class FluxConfigurationsGetSamples {
    /*
     * x-ms-original-file: specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/stable/2022-03-01/examples/GetFluxConfiguration.json
     */
    /**
     * Sample code: Get Flux Configuration.
     *
     * @param manager Entry point to SourceControlConfigurationManager.
     */
    public static void getFluxConfiguration(
        com.azure.resourcemanager.kubernetesconfiguration.SourceControlConfigurationManager manager) {
        manager
            .fluxConfigurations()
            .getWithResponse(
                "rg1", "Microsoft.Kubernetes", "connectedClusters", "clusterName1", "srs-fluxconfig", Context.NONE);
    }
}
```

### FluxConfigurations_List

```java
import com.azure.core.util.Context;

/** Samples for FluxConfigurations List. */
public final class FluxConfigurationsListSamples {
    /*
     * x-ms-original-file: specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/stable/2022-03-01/examples/ListFluxConfigurations.json
     */
    /**
     * Sample code: List Flux Configuration.
     *
     * @param manager Entry point to SourceControlConfigurationManager.
     */
    public static void listFluxConfiguration(
        com.azure.resourcemanager.kubernetesconfiguration.SourceControlConfigurationManager manager) {
        manager
            .fluxConfigurations()
            .list("rg1", "Microsoft.Kubernetes", "connectedClusters", "clusterName1", Context.NONE);
    }
}
```

### FluxConfigurations_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kubernetesconfiguration.models.FluxConfigurationPatch;
import com.azure.resourcemanager.kubernetesconfiguration.models.GitRepositoryPatchDefinition;
import com.azure.resourcemanager.kubernetesconfiguration.models.KustomizationPatchDefinition;
import java.util.HashMap;
import java.util.Map;

/** Samples for FluxConfigurations Update. */
public final class FluxConfigurationsUpdateSamples {
    /*
     * x-ms-original-file: specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/stable/2022-03-01/examples/PatchFluxConfiguration.json
     */
    /**
     * Sample code: Patch Flux Configuration.
     *
     * @param manager Entry point to SourceControlConfigurationManager.
     */
    public static void patchFluxConfiguration(
        com.azure.resourcemanager.kubernetesconfiguration.SourceControlConfigurationManager manager) {
        manager
            .fluxConfigurations()
            .update(
                "rg1",
                "Microsoft.Kubernetes",
                "connectedClusters",
                "clusterName1",
                "srs-fluxconfig",
                new FluxConfigurationPatch()
                    .withSuspend(true)
                    .withGitRepository(
                        new GitRepositoryPatchDefinition()
                            .withUrl("https://github.com/jonathan-innis/flux2-kustomize-helm-example.git"))
                    .withKustomizations(
                        mapOf(
                            "srs-kustomization1",
                            null,
                            "srs-kustomization2",
                            new KustomizationPatchDefinition()
                                .withPath("./test/alt-path")
                                .withSyncIntervalInSeconds(300L),
                            "srs-kustomization3",
                            new KustomizationPatchDefinition()
                                .withPath("./test/another-path")
                                .withSyncIntervalInSeconds(300L))),
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

### OperationStatus_Get

```java
import com.azure.core.util.Context;

/** Samples for OperationStatus Get. */
public final class OperationStatusGetSamples {
    /*
     * x-ms-original-file: specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/stable/2022-03-01/examples/GetExtensionAsyncOperationStatus.json
     */
    /**
     * Sample code: ExtensionAsyncOperationStatus Get.
     *
     * @param manager Entry point to SourceControlConfigurationManager.
     */
    public static void extensionAsyncOperationStatusGet(
        com.azure.resourcemanager.kubernetesconfiguration.SourceControlConfigurationManager manager) {
        manager
            .operationStatus()
            .getWithResponse(
                "rg1",
                "Microsoft.Kubernetes",
                "connectedClusters",
                "clusterName1",
                "ClusterMonitor",
                "99999999-9999-9999-9999-999999999999",
                Context.NONE);
    }
}
```

### OperationStatus_List

```java
import com.azure.core.util.Context;

/** Samples for OperationStatus List. */
public final class OperationStatusListSamples {
    /*
     * x-ms-original-file: specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/stable/2022-03-01/examples/ListAsyncOperationStatus.json
     */
    /**
     * Sample code: AsyncOperationStatus List.
     *
     * @param manager Entry point to SourceControlConfigurationManager.
     */
    public static void asyncOperationStatusList(
        com.azure.resourcemanager.kubernetesconfiguration.SourceControlConfigurationManager manager) {
        manager
            .operationStatus()
            .list("rg1", "Microsoft.Kubernetes", "connectedClusters", "clusterName1", Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/stable/2022-03-01/examples/OperationsList.json
     */
    /**
     * Sample code: BatchAccountDelete.
     *
     * @param manager Entry point to SourceControlConfigurationManager.
     */
    public static void batchAccountDelete(
        com.azure.resourcemanager.kubernetesconfiguration.SourceControlConfigurationManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### SourceControlConfigurations_CreateOrUpdate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kubernetesconfiguration.fluent.models.SourceControlConfigurationInner;
import com.azure.resourcemanager.kubernetesconfiguration.models.HelmOperatorProperties;
import com.azure.resourcemanager.kubernetesconfiguration.models.OperatorScopeType;
import com.azure.resourcemanager.kubernetesconfiguration.models.OperatorType;
import java.util.HashMap;
import java.util.Map;

/** Samples for SourceControlConfigurations CreateOrUpdate. */
public final class SourceControlConfigurationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/stable/2022-03-01/examples/CreateSourceControlConfiguration.json
     */
    /**
     * Sample code: Create Source Control Configuration.
     *
     * @param manager Entry point to SourceControlConfigurationManager.
     */
    public static void createSourceControlConfiguration(
        com.azure.resourcemanager.kubernetesconfiguration.SourceControlConfigurationManager manager) {
        manager
            .sourceControlConfigurations()
            .createOrUpdateWithResponse(
                "rg1",
                "Microsoft.Kubernetes",
                "connectedClusters",
                "clusterName1",
                "SRS_GitHubConfig",
                new SourceControlConfigurationInner()
                    .withRepositoryUrl("git@github.com:k8sdeveloper425/flux-get-started")
                    .withOperatorNamespace("SRS_Namespace")
                    .withOperatorInstanceName("SRSGitHubFluxOp-01")
                    .withOperatorType(OperatorType.FLUX)
                    .withOperatorParams("--git-email=xyzgituser@users.srs.github.com")
                    .withConfigurationProtectedSettings(mapOf("protectedSetting1Key", "protectedSetting1Value"))
                    .withOperatorScope(OperatorScopeType.NAMESPACE)
                    .withSshKnownHostsContents(
                        "c3NoLmRldi5henVyZS5jb20gc3NoLXJzYSBBQUFBQjNOemFDMXljMkVBQUFBREFRQUJBQUFCQVFDN0hyMW9UV3FOcU9sekdKT2ZHSjROYWtWeUl6ZjFyWFlkNGQ3d282akJsa0x2Q0E0b2RCbEwwbURVeVowL1FVZlRUcWV1K3RtMjJnT3N2K1ZyVlRNazZ2d1JVNzVnWS95OXV0NU1iM2JSNUJWNThkS1h5cTlBOVVlQjVDYWtlaG41WmdtNngxbUtvVnlmK0ZGbjI2aVlxWEpSZ3pJWlpjWjVWNmhyRTBRZzM5a1ptNGF6NDhvMEFVYmY2U3A0U0xkdm51TWEyc1ZOd0hCYm9TN0VKa201N1hRUFZVMy9RcHlOTEhiV0Rkend0cmxTK2V6MzBTM0FkWWhMS0VPeEFHOHdlT255cnRMSkFVZW45bVRrb2w4b0lJMWVkZjdtV1diV1ZmMG5CbWx5MjErblpjbUNUSVNRQnRkY3lQYUVubzdmRlFNREQyNi9zMGxmS29iNEt3OEg=")
                    .withEnableHelmOperator(true)
                    .withHelmOperatorProperties(
                        new HelmOperatorProperties()
                            .withChartVersion("0.3.0")
                            .withChartValues(
                                "--set git.ssh.secretName=flux-git-deploy --set tillerNamespace=kube-system")),
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

### SourceControlConfigurations_Delete

```java
import com.azure.core.util.Context;

/** Samples for SourceControlConfigurations Delete. */
public final class SourceControlConfigurationsDeleteSamples {
    /*
     * x-ms-original-file: specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/stable/2022-03-01/examples/DeleteSourceControlConfiguration.json
     */
    /**
     * Sample code: Delete Source Control Configuration.
     *
     * @param manager Entry point to SourceControlConfigurationManager.
     */
    public static void deleteSourceControlConfiguration(
        com.azure.resourcemanager.kubernetesconfiguration.SourceControlConfigurationManager manager) {
        manager
            .sourceControlConfigurations()
            .delete(
                "rg1", "Microsoft.Kubernetes", "connectedClusters", "clusterName1", "SRS_GitHubConfig", Context.NONE);
    }
}
```

### SourceControlConfigurations_Get

```java
import com.azure.core.util.Context;

/** Samples for SourceControlConfigurations Get. */
public final class SourceControlConfigurationsGetSamples {
    /*
     * x-ms-original-file: specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/stable/2022-03-01/examples/GetSourceControlConfiguration.json
     */
    /**
     * Sample code: Get Source Control Configuration.
     *
     * @param manager Entry point to SourceControlConfigurationManager.
     */
    public static void getSourceControlConfiguration(
        com.azure.resourcemanager.kubernetesconfiguration.SourceControlConfigurationManager manager) {
        manager
            .sourceControlConfigurations()
            .getWithResponse(
                "rg1", "Microsoft.Kubernetes", "connectedClusters", "clusterName1", "SRS_GitHubConfig", Context.NONE);
    }
}
```

### SourceControlConfigurations_List

```java
import com.azure.core.util.Context;

/** Samples for SourceControlConfigurations List. */
public final class SourceControlConfigurationsListSamples {
    /*
     * x-ms-original-file: specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/stable/2022-03-01/examples/ListSourceControlConfiguration.json
     */
    /**
     * Sample code: List Source Control Configuration.
     *
     * @param manager Entry point to SourceControlConfigurationManager.
     */
    public static void listSourceControlConfiguration(
        com.azure.resourcemanager.kubernetesconfiguration.SourceControlConfigurationManager manager) {
        manager
            .sourceControlConfigurations()
            .list("rg1", "Microsoft.Kubernetes", "connectedClusters", "clusterName1", Context.NONE);
    }
}
```

