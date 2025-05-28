# Code snippets and samples


## FluxConfigs

- [CreateOrUpdate](#fluxconfigs_createorupdate)
- [Delete](#fluxconfigs_delete)
- [Get](#fluxconfigs_get)
- [List](#fluxconfigs_list)
- [Update](#fluxconfigs_update)

## OperationStatus

- [Get](#operationstatus_get)
### FluxConfigs_CreateOrUpdate

```java
/**
 * Samples for FluxConfigs Delete.
 */
public final class FluxConfigsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/fluxConfigurations/
     * stable/2025-04-01/examples/DeleteFluxConfiguration.json
     */
    /**
     * Sample code: Delete Flux Configuration.
     * 
     * @param manager Entry point to FluxConfigManager.
     */
    public static void deleteFluxConfiguration(
        com.azure.resourcemanager.kubernetesconfiguration.fluxconfigurations.FluxConfigManager manager) {
        manager.fluxConfigs()
            .delete("rg1", "Microsoft.Kubernetes", "connectedClusters", "clusterName1", "srs-fluxconfig", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### FluxConfigs_Delete

```java
import com.azure.resourcemanager.kubernetesconfiguration.fluxconfigurations.models.FluxConfigurationPatch;
import com.azure.resourcemanager.kubernetesconfiguration.fluxconfigurations.models.GitRepositoryPatchDefinition;
import com.azure.resourcemanager.kubernetesconfiguration.fluxconfigurations.models.KustomizationPatchDefinition;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for FluxConfigs Update.
 */
public final class FluxConfigsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/fluxConfigurations/
     * stable/2025-04-01/examples/PatchFluxConfiguration.json
     */
    /**
     * Sample code: Patch Flux Configuration.
     * 
     * @param manager Entry point to FluxConfigManager.
     */
    public static void patchFluxConfiguration(
        com.azure.resourcemanager.kubernetesconfiguration.fluxconfigurations.FluxConfigManager manager) {
        manager.fluxConfigs()
            .update("rg1", "Microsoft.Kubernetes", "connectedClusters", "clusterName1", "srs-fluxconfig",
                new FluxConfigurationPatch().withSuspend(true)
                    .withGitRepository(new GitRepositoryPatchDefinition()
                        .withUrl("https://github.com/jonathan-innis/flux2-kustomize-helm-example.git"))
                    .withKustomizations(mapOf("srs-kustomization1", null, "srs-kustomization2",
                        new KustomizationPatchDefinition().withPath("./test/alt-path").withSyncIntervalInSeconds(300L),
                        "srs-kustomization3", new KustomizationPatchDefinition().withPath("./test/another-path")
                            .withSyncIntervalInSeconds(300L))),
                com.azure.core.util.Context.NONE);
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

### FluxConfigs_Get

```java
import com.azure.resourcemanager.kubernetesconfiguration.fluxconfigurations.fluent.models.FluxConfigurationInner;
import com.azure.resourcemanager.kubernetesconfiguration.fluxconfigurations.models.BucketDefinition;
import com.azure.resourcemanager.kubernetesconfiguration.fluxconfigurations.models.GitRepositoryDefinition;
import com.azure.resourcemanager.kubernetesconfiguration.fluxconfigurations.models.KustomizationDefinition;
import com.azure.resourcemanager.kubernetesconfiguration.fluxconfigurations.models.OciRepositoryDefinition;
import com.azure.resourcemanager.kubernetesconfiguration.fluxconfigurations.models.PostBuildDefinition;
import com.azure.resourcemanager.kubernetesconfiguration.fluxconfigurations.models.ProviderType;
import com.azure.resourcemanager.kubernetesconfiguration.fluxconfigurations.models.RepositoryRefDefinition;
import com.azure.resourcemanager.kubernetesconfiguration.fluxconfigurations.models.ScopeType;
import com.azure.resourcemanager.kubernetesconfiguration.fluxconfigurations.models.SourceKindType;
import com.azure.resourcemanager.kubernetesconfiguration.fluxconfigurations.models.SubstituteFromDefinition;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for FluxConfigs CreateOrUpdate.
 */
public final class FluxConfigsCreateOrUpSamples {
    /*
     * x-ms-original-file:
     * specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/fluxConfigurations/
     * stable/2025-04-01/examples/CreateFluxConfigurationWithOCIRepository.json
     */
    /**
     * Sample code: Create Flux Configuration with OCIRepository Source Kind.
     * 
     * @param manager Entry point to FluxConfigManager.
     */
    public static void createFluxConfigurationWithOCIRepositorySourceKind(
        com.azure.resourcemanager.kubernetesconfiguration.fluxconfigurations.FluxConfigManager manager) {
        manager.fluxConfigs()
            .createOrUpdate("rg1", "Microsoft.Kubernetes", "connectedClusters", "clusterName1", "srs-fluxconfig",
                new FluxConfigurationInner().withScope(ScopeType.CLUSTER)
                    .withNamespace("srs-namespace")
                    .withSourceKind(SourceKindType.OCIREPOSITORY)
                    .withSuspend(false)
                    .withOciRepository(
                        new OciRepositoryDefinition().withUrl("oci://ghcr.io/stefanprodan/manifests/podinfo")
                            .withTimeoutInSeconds(1000L)
                            .withSyncIntervalInSeconds(1000L)
                            .withServiceAccountName("testserviceaccount"))
                    .withKustomizations(mapOf("srs-kustomization1",
                        new KustomizationDefinition().withPath("./test/path")
                            .withDependsOn(Arrays.asList())
                            .withTimeoutInSeconds(600L)
                            .withSyncIntervalInSeconds(600L),
                        "srs-kustomization2",
                        new KustomizationDefinition().withPath("./other/test/path")
                            .withDependsOn(Arrays.asList("srs-kustomization1"))
                            .withTimeoutInSeconds(600L)
                            .withSyncIntervalInSeconds(600L)
                            .withRetryIntervalInSeconds(600L)
                            .withPrune(false))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/fluxConfigurations/
     * stable/2025-04-01/examples/CreateFluxConfiguration.json
     */
    /**
     * Sample code: Create Flux Configuration.
     * 
     * @param manager Entry point to FluxConfigManager.
     */
    public static void createFluxConfiguration(
        com.azure.resourcemanager.kubernetesconfiguration.fluxconfigurations.FluxConfigManager manager) {
        manager.fluxConfigs()
            .createOrUpdate("rg1", "Microsoft.Kubernetes", "connectedClusters", "clusterName1", "srs-fluxconfig",
                new FluxConfigurationInner().withScope(ScopeType.CLUSTER)
                    .withNamespace("srs-namespace")
                    .withSourceKind(SourceKindType.GIT_REPOSITORY)
                    .withSuspend(false)
                    .withGitRepository(new GitRepositoryDefinition().withUrl("https://github.com/Azure/arc-k8s-demo")
                        .withTimeoutInSeconds(600L)
                        .withSyncIntervalInSeconds(600L)
                        .withRepositoryRef(new RepositoryRefDefinition().withBranch("master"))
                        .withHttpsCACert("ZXhhbXBsZWNlcnRpZmljYXRl"))
                    .withKustomizations(mapOf("srs-kustomization1",
                        new KustomizationDefinition().withPath("./test/path")
                            .withDependsOn(Arrays.asList())
                            .withTimeoutInSeconds(600L)
                            .withSyncIntervalInSeconds(600L)
                            .withEnableWait(true)
                            .withPostBuild(new PostBuildDefinition()
                                .withSubstitute(mapOf("cluster_env", "prod", "replica_count", "2"))
                                .withSubstituteFrom(Arrays.asList(new SubstituteFromDefinition().withKind("ConfigMap")
                                    .withName("cluster-test")
                                    .withOptional(true)))),
                        "srs-kustomization2",
                        new KustomizationDefinition().withPath("./other/test/path")
                            .withDependsOn(Arrays.asList("srs-kustomization1"))
                            .withTimeoutInSeconds(600L)
                            .withSyncIntervalInSeconds(600L)
                            .withRetryIntervalInSeconds(600L)
                            .withPrune(false)
                            .withEnableWait(false)
                            .withPostBuild(new PostBuildDefinition().withSubstituteFrom(Arrays.asList(
                                new SubstituteFromDefinition().withKind("ConfigMap")
                                    .withName("cluster-values")
                                    .withOptional(true),
                                new SubstituteFromDefinition().withKind("Secret")
                                    .withName("secret-name")
                                    .withOptional(false))))))
                    .withWaitForReconciliation(true)
                    .withReconciliationWaitDuration("PT30M"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/fluxConfigurations/
     * stable/2025-04-01/examples/CreateFluxConfigurationWithProvider.json
     */
    /**
     * Sample code: Create Flux Configuration with Git Repository Provider.
     * 
     * @param manager Entry point to FluxConfigManager.
     */
    public static void createFluxConfigurationWithGitRepositoryProvider(
        com.azure.resourcemanager.kubernetesconfiguration.fluxconfigurations.FluxConfigManager manager) {
        manager.fluxConfigs()
            .createOrUpdate("rg1", "Microsoft.Kubernetes", "connectedClusters", "clusterName1", "srs-fluxconfig",
                new FluxConfigurationInner().withScope(ScopeType.CLUSTER)
                    .withNamespace("srs-namespace")
                    .withSourceKind(SourceKindType.GIT_REPOSITORY)
                    .withSuspend(false)
                    .withGitRepository(
                        new GitRepositoryDefinition().withUrl("https://dev.azure.com/org/proj/_git/arc-k8s-demo")
                            .withTimeoutInSeconds(600L)
                            .withSyncIntervalInSeconds(600L)
                            .withRepositoryRef(new RepositoryRefDefinition().withBranch("master"))
                            .withHttpsCACert("ZXhhbXBsZWNlcnRpZmljYXRl")
                            .withProvider(ProviderType.AZURE))
                    .withKustomizations(mapOf("srs-kustomization1",
                        new KustomizationDefinition().withPath("./test/path")
                            .withDependsOn(Arrays.asList())
                            .withTimeoutInSeconds(600L)
                            .withSyncIntervalInSeconds(600L)
                            .withEnableWait(true)
                            .withPostBuild(new PostBuildDefinition()
                                .withSubstitute(mapOf("cluster_env", "prod", "replica_count", "2"))
                                .withSubstituteFrom(Arrays.asList(new SubstituteFromDefinition().withKind("ConfigMap")
                                    .withName("cluster-test")
                                    .withOptional(true)))),
                        "srs-kustomization2",
                        new KustomizationDefinition().withPath("./other/test/path")
                            .withDependsOn(Arrays.asList("srs-kustomization1"))
                            .withTimeoutInSeconds(600L)
                            .withSyncIntervalInSeconds(600L)
                            .withRetryIntervalInSeconds(600L)
                            .withPrune(false)
                            .withEnableWait(false)
                            .withPostBuild(new PostBuildDefinition().withSubstituteFrom(Arrays.asList(
                                new SubstituteFromDefinition().withKind("ConfigMap")
                                    .withName("cluster-values")
                                    .withOptional(true),
                                new SubstituteFromDefinition().withKind("Secret")
                                    .withName("secret-name")
                                    .withOptional(false))))))
                    .withWaitForReconciliation(true)
                    .withReconciliationWaitDuration("PT30M"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/fluxConfigurations/
     * stable/2025-04-01/examples/CreateFluxConfigurationWithBucket.json
     */
    /**
     * Sample code: Create Flux Configuration with Bucket Source Kind.
     * 
     * @param manager Entry point to FluxConfigManager.
     */
    public static void createFluxConfigurationWithBucketSourceKind(
        com.azure.resourcemanager.kubernetesconfiguration.fluxconfigurations.FluxConfigManager manager) {
        manager.fluxConfigs()
            .createOrUpdate("rg1", "Microsoft.Kubernetes", "connectedClusters", "clusterName1", "srs-fluxconfig",
                new FluxConfigurationInner().withScope(ScopeType.CLUSTER)
                    .withNamespace("srs-namespace")
                    .withSourceKind(SourceKindType.BUCKET)
                    .withSuspend(false)
                    .withBucket(new BucketDefinition().withUrl("https://fluxminiotest.az.minio.io")
                        .withBucketName("flux")
                        .withTimeoutInSeconds(1000L)
                        .withSyncIntervalInSeconds(1000L)
                        .withAccessKey("fakeTokenPlaceholder"))
                    .withKustomizations(mapOf("srs-kustomization1",
                        new KustomizationDefinition().withPath("./test/path")
                            .withDependsOn(Arrays.asList())
                            .withTimeoutInSeconds(600L)
                            .withSyncIntervalInSeconds(600L),
                        "srs-kustomization2",
                        new KustomizationDefinition().withPath("./other/test/path")
                            .withDependsOn(Arrays.asList("srs-kustomization1"))
                            .withTimeoutInSeconds(600L)
                            .withSyncIntervalInSeconds(600L)
                            .withRetryIntervalInSeconds(600L)
                            .withPrune(false))),
                com.azure.core.util.Context.NONE);
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

### FluxConfigs_List

```java
/**
 * Samples for FluxConfigs List.
 */
public final class FluxConfigsListSamples {
    /*
     * x-ms-original-file:
     * specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/fluxConfigurations/
     * stable/2025-04-01/examples/ListFluxConfigurations.json
     */
    /**
     * Sample code: List Flux Configuration.
     * 
     * @param manager Entry point to FluxConfigManager.
     */
    public static void listFluxConfiguration(
        com.azure.resourcemanager.kubernetesconfiguration.fluxconfigurations.FluxConfigManager manager) {
        manager.fluxConfigs()
            .list("rg1", "Microsoft.Kubernetes", "connectedClusters", "clusterName1", com.azure.core.util.Context.NONE);
    }
}
```

### FluxConfigs_Update

```java
/**
 * Samples for FluxConfigs Get.
 */
public final class FluxConfigsGetSamples {
    /*
     * x-ms-original-file:
     * specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/fluxConfigurations/
     * stable/2025-04-01/examples/GetFluxConfiguration.json
     */
    /**
     * Sample code: Get Flux Configuration.
     * 
     * @param manager Entry point to FluxConfigManager.
     */
    public static void getFluxConfiguration(
        com.azure.resourcemanager.kubernetesconfiguration.fluxconfigurations.FluxConfigManager manager) {
        manager.fluxConfigs()
            .getWithResponse("rg1", "Microsoft.Kubernetes", "connectedClusters", "clusterName1", "srs-fluxconfig",
                com.azure.core.util.Context.NONE);
    }
}
```

### OperationStatus_Get

```java
/**
 * Samples for OperationStatus Get.
 */
public final class OperationStatusGetSamples {
    /*
     * x-ms-original-file:
     * specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/fluxConfigurations/
     * stable/2025-04-01/examples/GetFluxConfigurationAsyncOperationStatus.json
     */
    /**
     * Sample code: FluxConfigurationAsyncOperationStatus Get.
     * 
     * @param manager Entry point to FluxConfigManager.
     */
    public static void fluxConfigurationAsyncOperationStatusGet(
        com.azure.resourcemanager.kubernetesconfiguration.fluxconfigurations.FluxConfigManager manager) {
        manager.operationStatus()
            .getWithResponse("rg1", "Microsoft.Kubernetes", "connectedClusters", "clusterName1", "srs-fluxconfig",
                "99999999-9999-9999-9999-999999999999", com.azure.core.util.Context.NONE);
    }
}
```

