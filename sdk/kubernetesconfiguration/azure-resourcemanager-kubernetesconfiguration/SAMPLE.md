# Code snippets and samples


## Extensions

- [Create](#extensions_create)
- [Delete](#extensions_delete)
- [Get](#extensions_get)
- [List](#extensions_list)
- [Update](#extensions_update)

## OperationStatus

- [Get](#operationstatus_get)
- [List](#operationstatus_list)

## Operations

- [List](#operations_list)
### Extensions_Create

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kubernetesconfiguration.fluent.models.ExtensionInner;
import com.azure.resourcemanager.kubernetesconfiguration.models.ExtensionsClusterResourceName;
import com.azure.resourcemanager.kubernetesconfiguration.models.ExtensionsClusterRp;
import com.azure.resourcemanager.kubernetesconfiguration.models.Scope;
import com.azure.resourcemanager.kubernetesconfiguration.models.ScopeCluster;
import java.util.HashMap;
import java.util.Map;

/** Samples for Extensions Create. */
public final class ExtensionsCreateSamples {
    /*
     * x-ms-original-file: specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/stable/2021-09-01/examples/CreateExtension.json
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
                ExtensionsClusterRp.MICROSOFT_KUBERNETES,
                ExtensionsClusterResourceName.CONNECTED_CLUSTERS,
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
import com.azure.resourcemanager.kubernetesconfiguration.models.ExtensionsClusterResourceName;
import com.azure.resourcemanager.kubernetesconfiguration.models.ExtensionsClusterRp;

/** Samples for Extensions Delete. */
public final class ExtensionsDeleteSamples {
    /*
     * x-ms-original-file: specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/stable/2021-09-01/examples/DeleteExtension.json
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
                ExtensionsClusterRp.MICROSOFT_KUBERNETES,
                ExtensionsClusterResourceName.CONNECTED_CLUSTERS,
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
import com.azure.resourcemanager.kubernetesconfiguration.models.ExtensionsClusterResourceName;
import com.azure.resourcemanager.kubernetesconfiguration.models.ExtensionsClusterRp;

/** Samples for Extensions Get. */
public final class ExtensionsGetSamples {
    /*
     * x-ms-original-file: specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/stable/2021-09-01/examples/GetExtension.json
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
                "rg1",
                ExtensionsClusterRp.MICROSOFT_KUBERNETES,
                ExtensionsClusterResourceName.CONNECTED_CLUSTERS,
                "clusterName1",
                "ClusterMonitor",
                Context.NONE);
    }
}
```

### Extensions_List

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kubernetesconfiguration.models.ExtensionsClusterResourceName;
import com.azure.resourcemanager.kubernetesconfiguration.models.ExtensionsClusterRp;

/** Samples for Extensions List. */
public final class ExtensionsListSamples {
    /*
     * x-ms-original-file: specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/stable/2021-09-01/examples/ListExtensions.json
     */
    /**
     * Sample code: List Extensions.
     *
     * @param manager Entry point to SourceControlConfigurationManager.
     */
    public static void listExtensions(
        com.azure.resourcemanager.kubernetesconfiguration.SourceControlConfigurationManager manager) {
        manager
            .extensions()
            .list(
                "rg1",
                ExtensionsClusterRp.MICROSOFT_KUBERNETES,
                ExtensionsClusterResourceName.CONNECTED_CLUSTERS,
                "clusterName1",
                Context.NONE);
    }
}
```

### Extensions_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kubernetesconfiguration.models.ExtensionsClusterResourceName;
import com.azure.resourcemanager.kubernetesconfiguration.models.ExtensionsClusterRp;
import com.azure.resourcemanager.kubernetesconfiguration.models.PatchExtension;
import java.util.HashMap;
import java.util.Map;

/** Samples for Extensions Update. */
public final class ExtensionsUpdateSamples {
    /*
     * x-ms-original-file: specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/stable/2021-09-01/examples/PatchExtension.json
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
                ExtensionsClusterRp.MICROSOFT_KUBERNETES,
                ExtensionsClusterResourceName.CONNECTED_CLUSTERS,
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

### OperationStatus_Get

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.kubernetesconfiguration.models.ExtensionsClusterResourceName;
import com.azure.resourcemanager.kubernetesconfiguration.models.ExtensionsClusterRp;

/** Samples for OperationStatus Get. */
public final class OperationStatusGetSamples {
    /*
     * x-ms-original-file: specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/stable/2021-09-01/examples/GetAsyncOperationStatus.json
     */
    /**
     * Sample code: AsyncOperationStatus Get.
     *
     * @param manager Entry point to SourceControlConfigurationManager.
     */
    public static void asyncOperationStatusGet(
        com.azure.resourcemanager.kubernetesconfiguration.SourceControlConfigurationManager manager) {
        manager
            .operationStatus()
            .getWithResponse(
                "rg1",
                ExtensionsClusterRp.MICROSOFT_KUBERNETES,
                ExtensionsClusterResourceName.CONNECTED_CLUSTERS,
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
import com.azure.resourcemanager.kubernetesconfiguration.models.ExtensionsClusterResourceName;
import com.azure.resourcemanager.kubernetesconfiguration.models.ExtensionsClusterRp;

/** Samples for OperationStatus List. */
public final class OperationStatusListSamples {
    /*
     * x-ms-original-file: specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/stable/2021-09-01/examples/ListAsyncOperationStatus.json
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
            .list(
                "rg1",
                ExtensionsClusterRp.MICROSOFT_KUBERNETES,
                ExtensionsClusterResourceName.CONNECTED_CLUSTERS,
                "clusterName1",
                Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/kubernetesconfiguration/resource-manager/Microsoft.KubernetesConfiguration/stable/2021-09-01/examples/OperationsList.json
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

