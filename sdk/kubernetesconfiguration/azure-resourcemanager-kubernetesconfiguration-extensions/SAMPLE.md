# Code snippets and samples


## Extensions

- [Create](#extensions_create)
- [Delete](#extensions_delete)
- [Get](#extensions_get)
- [List](#extensions_list)
- [Update](#extensions_update)

## OperationStatus

- [Get](#operationstatus_get)
### Extensions_Create

```java
/**
 * Samples for Extensions Get.
 */
public final class ExtensionsGetSamples {
    /*
     * x-ms-original-file:
     * specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/extensions/stable/2024-
     * 11-01/examples/GetExtensionWithPlan.json
     */
    /**
     * Sample code: Get Extension with Plan.
     * 
     * @param manager Entry point to ExtensionsManager.
     */
    public static void
        getExtensionWithPlan(com.azure.resourcemanager.kubernetesconfiguration.extensions.ExtensionsManager manager) {
        manager.extensions()
            .getWithResponse("rg1", "Microsoft.Kubernetes", "connectedClusters", "clusterName1", "azureVote",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/extensions/stable/2024-
     * 11-01/examples/GetExtension.json
     */
    /**
     * Sample code: Get Extension.
     * 
     * @param manager Entry point to ExtensionsManager.
     */
    public static void
        getExtension(com.azure.resourcemanager.kubernetesconfiguration.extensions.ExtensionsManager manager) {
        manager.extensions()
            .getWithResponse("rg1", "Microsoft.Kubernetes", "connectedClusters", "clusterName1", "ClusterMonitor",
                com.azure.core.util.Context.NONE);
    }
}
```

### Extensions_Delete

```java
import com.azure.resourcemanager.kubernetesconfiguration.extensions.models.PatchExtension;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Extensions Update.
 */
public final class ExtensionsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/extensions/stable/2024-
     * 11-01/examples/PatchExtension.json
     */
    /**
     * Sample code: Update Extension.
     * 
     * @param manager Entry point to ExtensionsManager.
     */
    public static void
        updateExtension(com.azure.resourcemanager.kubernetesconfiguration.extensions.ExtensionsManager manager) {
        manager.extensions()
            .update("rg1", "Microsoft.Kubernetes", "connectedClusters", "clusterName1", "ClusterMonitor",
                new PatchExtension().withAutoUpgradeMinorVersion(true)
                    .withReleaseTrain("Preview")
                    .withConfigurationSettings(mapOf("omsagent.env.clusterName", "clusterName1", "omsagent.secret.wsid",
                        "fakeTokenPlaceholder"))
                    .withConfigurationProtectedSettings(mapOf("omsagent.secret.key", "fakeTokenPlaceholder")),
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

### Extensions_Get

```java
import com.azure.resourcemanager.kubernetesconfiguration.extensions.fluent.models.ExtensionInner;
import com.azure.resourcemanager.kubernetesconfiguration.extensions.models.Plan;
import com.azure.resourcemanager.kubernetesconfiguration.extensions.models.Scope;
import com.azure.resourcemanager.kubernetesconfiguration.extensions.models.ScopeCluster;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Extensions Create.
 */
public final class ExtensionsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/extensions/stable/2024-
     * 11-01/examples/CreateExtension.json
     */
    /**
     * Sample code: Create Extension.
     * 
     * @param manager Entry point to ExtensionsManager.
     */
    public static void
        createExtension(com.azure.resourcemanager.kubernetesconfiguration.extensions.ExtensionsManager manager) {
        manager.extensions()
            .create("rg1", "Microsoft.Kubernetes", "connectedClusters", "clusterName1", "ClusterMonitor",
                new ExtensionInner().withExtensionType("azuremonitor-containers")
                    .withAutoUpgradeMinorVersion(true)
                    .withReleaseTrain("Preview")
                    .withScope(new Scope().withCluster(new ScopeCluster().withReleaseNamespace("kube-system")))
                    .withConfigurationSettings(mapOf("omsagent.env.clusterName", "clusterName1", "omsagent.secret.wsid",
                        "fakeTokenPlaceholder"))
                    .withConfigurationProtectedSettings(mapOf("omsagent.secret.key", "fakeTokenPlaceholder")),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/extensions/stable/2024-
     * 11-01/examples/CreateExtensionWithPlan.json
     */
    /**
     * Sample code: Create Extension with Plan.
     * 
     * @param manager Entry point to ExtensionsManager.
     */
    public static void createExtensionWithPlan(
        com.azure.resourcemanager.kubernetesconfiguration.extensions.ExtensionsManager manager) {
        manager.extensions()
            .create("rg1", "Microsoft.Kubernetes", "connectedClusters", "clusterName1", "azureVote",
                new ExtensionInner()
                    .withPlan(new Plan().withName("azure-vote-standard")
                        .withPublisher("Microsoft")
                        .withProduct("azure-vote-standard-offer-id"))
                    .withExtensionType("azure-vote")
                    .withAutoUpgradeMinorVersion(true)
                    .withReleaseTrain("Preview"),
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

### Extensions_List

```java
/**
 * Samples for Extensions List.
 */
public final class ExtensionsListSamples {
    /*
     * x-ms-original-file:
     * specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/extensions/stable/2024-
     * 11-01/examples/ListExtensions.json
     */
    /**
     * Sample code: List Extensions.
     * 
     * @param manager Entry point to ExtensionsManager.
     */
    public static void
        listExtensions(com.azure.resourcemanager.kubernetesconfiguration.extensions.ExtensionsManager manager) {
        manager.extensions()
            .list("rg1", "Microsoft.Kubernetes", "connectedClusters", "clusterName1", com.azure.core.util.Context.NONE);
    }
}
```

### Extensions_Update

```java
/**
 * Samples for Extensions Delete.
 */
public final class ExtensionsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/extensions/stable/2024-
     * 11-01/examples/DeleteExtension.json
     */
    /**
     * Sample code: Delete Extension.
     * 
     * @param manager Entry point to ExtensionsManager.
     */
    public static void
        deleteExtension(com.azure.resourcemanager.kubernetesconfiguration.extensions.ExtensionsManager manager) {
        manager.extensions()
            .delete("rg1", "Microsoft.Kubernetes", "connectedClusters", "clusterName1", "ClusterMonitor", null,
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
     * specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/extensions/stable/2024-
     * 11-01/examples/GetExtensionAsyncOperationStatus.json
     */
    /**
     * Sample code: ExtensionAsyncOperationStatus Get.
     * 
     * @param manager Entry point to ExtensionsManager.
     */
    public static void extensionAsyncOperationStatusGet(
        com.azure.resourcemanager.kubernetesconfiguration.extensions.ExtensionsManager manager) {
        manager.operationStatus()
            .getWithResponse("rg1", "Microsoft.Kubernetes", "connectedClusters", "clusterName1", "ClusterMonitor",
                "99999999-9999-9999-9999-999999999999", com.azure.core.util.Context.NONE);
    }
}
```

