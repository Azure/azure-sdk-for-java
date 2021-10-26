# Code snippets and samples


## ConnectedCluster

- [Create](#connectedcluster_create)
- [Delete](#connectedcluster_delete)
- [GetByResourceGroup](#connectedcluster_getbyresourcegroup)
- [List](#connectedcluster_list)
- [ListByResourceGroup](#connectedcluster_listbyresourcegroup)
- [ListClusterUserCredential](#connectedcluster_listclusterusercredential)
- [Update](#connectedcluster_update)

## Operations

- [Get](#operations_get)
### ConnectedCluster_Create

```java
import com.azure.resourcemanager.hybridkubernetes.models.ConnectedClusterIdentity;
import com.azure.resourcemanager.hybridkubernetes.models.ResourceIdentityType;
import java.util.HashMap;
import java.util.Map;

/** Samples for ConnectedCluster Create. */
public final class ConnectedClusterCreateSamples {
    /*
     * x-ms-original-file: specification/hybridkubernetes/resource-manager/Microsoft.Kubernetes/stable/2021-10-01/examples/CreateClusterExample.json
     */
    /**
     * Sample code: CreateClusterExample.
     *
     * @param manager Entry point to HybridKubernetesManager.
     */
    public static void createClusterExample(
        com.azure.resourcemanager.hybridkubernetes.HybridKubernetesManager manager) {
        manager
            .connectedClusters()
            .define("testCluster")
            .withRegion("East US")
            .withExistingResourceGroup("k8sc-rg")
            .withIdentity(new ConnectedClusterIdentity().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
            .withAgentPublicKeyCertificate(
                "MIICYzCCAcygAwIBAgIBADANBgkqhkiG9w0BAQUFADAuMQswCQYDVQQGEwJVUzEMMAoGA1UEChMDSUJNMREwDwYDVQQLEwhMb2NhbCBDQTAeFw05OTEyMjIwNTAwMDBaFw0wMDEyMjMwNDU5NTlaMC4xCzAJBgNVBAYTAlVTMQwwCgYDVQQKEwNJQk0xETAPBgNVBAsTCExvY2FsIENBMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQD2bZEo7xGaX2/0GHkrNFZvlxBou9v1Jmt/PDiTMPve8r9FeJAQ0QdvFST/0JPQYD20rH0bimdDLgNdNynmyRoS2S/IInfpmf69iyc2G0TPyRvmHIiOZbdCd+YBHQi1adkj17NDcWj6S14tVurFX73zx0sNoMS79q3tuXKrDsxeuwIDAQABo4GQMIGNMEsGCVUdDwGG+EIBDQQ+EzxHZW5lcmF0ZWQgYnkgdGhlIFNlY3VyZVdheSBTZWN1cml0eSBTZXJ2ZXIgZm9yIE9TLzM5MCAoUkFDRikwDgYDVR0PAQH/BAQDAgAGMA8GA1UdEwEB/wQFMAMBAf8wHQYDVR0OBBYEFJ3+ocRyCTJw067dLSwr/nalx6YMMA0GCSqGSIb3DQEBBQUAA4GBAMaQzt+zaj1GU77yzlr8iiMBXgdQrwsZZWJo5exnAucJAEYQZmOfyLiM"
                    + " D6oYq+ZnfvM0n8G/Y79q8nhwvuxpYOnRSAXFp6xSkrIOeZtJMY1h00LKp/JX3Ng1svZ2agE126JHsQ0bhzN5TKsYfbwfTwfjdWAGy6Vf1nYi/rO+ryMO")
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

### ConnectedCluster_Delete

```java
import com.azure.core.util.Context;

/** Samples for ConnectedCluster Delete. */
public final class ConnectedClusterDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridkubernetes/resource-manager/Microsoft.Kubernetes/stable/2021-10-01/examples/DeleteClusterExample.json
     */
    /**
     * Sample code: DeleteClusterExample.
     *
     * @param manager Entry point to HybridKubernetesManager.
     */
    public static void deleteClusterExample(
        com.azure.resourcemanager.hybridkubernetes.HybridKubernetesManager manager) {
        manager.connectedClusters().delete("k8sc-rg", "testCluster", Context.NONE);
    }
}
```

### ConnectedCluster_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ConnectedCluster GetByResourceGroup. */
public final class ConnectedClusterGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridkubernetes/resource-manager/Microsoft.Kubernetes/stable/2021-10-01/examples/GetClusterExample.json
     */
    /**
     * Sample code: GetClusterExample.
     *
     * @param manager Entry point to HybridKubernetesManager.
     */
    public static void getClusterExample(com.azure.resourcemanager.hybridkubernetes.HybridKubernetesManager manager) {
        manager.connectedClusters().getByResourceGroupWithResponse("k8sc-rg", "testCluster", Context.NONE);
    }
}
```

### ConnectedCluster_List

```java
import com.azure.core.util.Context;

/** Samples for ConnectedCluster List. */
public final class ConnectedClusterListSamples {
    /*
     * x-ms-original-file: specification/hybridkubernetes/resource-manager/Microsoft.Kubernetes/stable/2021-10-01/examples/GetClustersBySubscriptionExample.json
     */
    /**
     * Sample code: GetClustersExample.
     *
     * @param manager Entry point to HybridKubernetesManager.
     */
    public static void getClustersExample(com.azure.resourcemanager.hybridkubernetes.HybridKubernetesManager manager) {
        manager.connectedClusters().list(Context.NONE);
    }
}
```

### ConnectedCluster_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ConnectedCluster ListByResourceGroup. */
public final class ConnectedClusterListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridkubernetes/resource-manager/Microsoft.Kubernetes/stable/2021-10-01/examples/GetClustersByResourceGroupExample.json
     */
    /**
     * Sample code: GetClustersExample.
     *
     * @param manager Entry point to HybridKubernetesManager.
     */
    public static void getClustersExample(com.azure.resourcemanager.hybridkubernetes.HybridKubernetesManager manager) {
        manager.connectedClusters().listByResourceGroup("k8sc-rg", Context.NONE);
    }
}
```

### ConnectedCluster_ListClusterUserCredential

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.hybridkubernetes.models.AuthenticationMethod;
import com.azure.resourcemanager.hybridkubernetes.models.ListClusterUserCredentialProperties;

/** Samples for ConnectedCluster ListClusterUserCredential. */
public final class ConnectedClusterListClusterUserCredentialSamples {
    /*
     * x-ms-original-file: specification/hybridkubernetes/resource-manager/Microsoft.Kubernetes/stable/2021-10-01/examples/ConnectedClustersListClusterCredentialResultCSPAAD.json
     */
    /**
     * Sample code: ListClusterUserCredentialExample.
     *
     * @param manager Entry point to HybridKubernetesManager.
     */
    public static void listClusterUserCredentialExample(
        com.azure.resourcemanager.hybridkubernetes.HybridKubernetesManager manager) {
        manager
            .connectedClusters()
            .listClusterUserCredentialWithResponse(
                "k8sc-rg",
                "testCluster",
                new ListClusterUserCredentialProperties()
                    .withAuthenticationMethod(AuthenticationMethod.AAD)
                    .withClientProxy(true),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/hybridkubernetes/resource-manager/Microsoft.Kubernetes/stable/2021-10-01/examples/ConnectedClustersListClusterCredentialResultHPAAD.json
     */
    /**
     * Sample code: ListClusterUserCredentialCSPExample.
     *
     * @param manager Entry point to HybridKubernetesManager.
     */
    public static void listClusterUserCredentialCSPExample(
        com.azure.resourcemanager.hybridkubernetes.HybridKubernetesManager manager) {
        manager
            .connectedClusters()
            .listClusterUserCredentialWithResponse(
                "k8sc-rg",
                "testCluster",
                new ListClusterUserCredentialProperties()
                    .withAuthenticationMethod(AuthenticationMethod.AAD)
                    .withClientProxy(false),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/hybridkubernetes/resource-manager/Microsoft.Kubernetes/stable/2021-10-01/examples/ConnectedClustersListClusterCredentialResultCSPToken.json
     */
    /**
     * Sample code: ListClusterUserCredentialNonAadExample.
     *
     * @param manager Entry point to HybridKubernetesManager.
     */
    public static void listClusterUserCredentialNonAadExample(
        com.azure.resourcemanager.hybridkubernetes.HybridKubernetesManager manager) {
        manager
            .connectedClusters()
            .listClusterUserCredentialWithResponse(
                "k8sc-rg",
                "testCluster",
                new ListClusterUserCredentialProperties()
                    .withAuthenticationMethod(AuthenticationMethod.TOKEN)
                    .withClientProxy(true),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/hybridkubernetes/resource-manager/Microsoft.Kubernetes/stable/2021-10-01/examples/ConnectedClustersListClusterCredentialResultHPToken.json
     */
    /**
     * Sample code: ListClusterUserCredentialNonAadCSPExample.
     *
     * @param manager Entry point to HybridKubernetesManager.
     */
    public static void listClusterUserCredentialNonAadCSPExample(
        com.azure.resourcemanager.hybridkubernetes.HybridKubernetesManager manager) {
        manager
            .connectedClusters()
            .listClusterUserCredentialWithResponse(
                "k8sc-rg",
                "testCluster",
                new ListClusterUserCredentialProperties()
                    .withAuthenticationMethod(AuthenticationMethod.TOKEN)
                    .withClientProxy(false),
                Context.NONE);
    }
}
```

### ConnectedCluster_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.hybridkubernetes.models.ConnectedCluster;
import java.util.HashMap;
import java.util.Map;

/** Samples for ConnectedCluster Update. */
public final class ConnectedClusterUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridkubernetes/resource-manager/Microsoft.Kubernetes/stable/2021-10-01/examples/UpdateClusterExample.json
     */
    /**
     * Sample code: UpdateClusterExample.
     *
     * @param manager Entry point to HybridKubernetesManager.
     */
    public static void updateClusterExample(
        com.azure.resourcemanager.hybridkubernetes.HybridKubernetesManager manager) {
        ConnectedCluster resource =
            manager
                .connectedClusters()
                .getByResourceGroupWithResponse("k8sc-rg", "testCluster", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
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

### Operations_Get

```java
import com.azure.core.util.Context;

/** Samples for Operations Get. */
public final class OperationsGetSamples {
    /*
     * x-ms-original-file: specification/hybridkubernetes/resource-manager/Microsoft.Kubernetes/stable/2021-10-01/examples/ListConnectedClusterOperationsExample.json
     */
    /**
     * Sample code: ListConnectedClusterOperationsExample.
     *
     * @param manager Entry point to HybridKubernetesManager.
     */
    public static void listConnectedClusterOperationsExample(
        com.azure.resourcemanager.hybridkubernetes.HybridKubernetesManager manager) {
        manager.operations().get(Context.NONE);
    }
}
```

