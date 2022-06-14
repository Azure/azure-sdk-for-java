# Code snippets and samples


## Application

- [Create](#application_create)
- [Delete](#application_delete)
- [GetByResourceGroup](#application_getbyresourcegroup)
- [List](#application_list)
- [ListByResourceGroup](#application_listbyresourcegroup)

## CodePackage

- [GetContainerLogs](#codepackage_getcontainerlogs)

## Gateway

- [Create](#gateway_create)
- [Delete](#gateway_delete)
- [GetByResourceGroup](#gateway_getbyresourcegroup)
- [List](#gateway_list)
- [ListByResourceGroup](#gateway_listbyresourcegroup)

## Network

- [Create](#network_create)
- [Delete](#network_delete)
- [GetByResourceGroup](#network_getbyresourcegroup)
- [List](#network_list)
- [ListByResourceGroup](#network_listbyresourcegroup)

## Secret

- [Create](#secret_create)
- [Delete](#secret_delete)
- [GetByResourceGroup](#secret_getbyresourcegroup)
- [List](#secret_list)
- [ListByResourceGroup](#secret_listbyresourcegroup)

## SecretValue

- [Create](#secretvalue_create)
- [Delete](#secretvalue_delete)
- [Get](#secretvalue_get)
- [List](#secretvalue_list)
- [ListValue](#secretvalue_listvalue)

## Service

- [Get](#service_get)
- [List](#service_list)

## ServiceReplica

- [Get](#servicereplica_get)
- [List](#servicereplica_list)

## Volume

- [Create](#volume_create)
- [Delete](#volume_delete)
- [GetByResourceGroup](#volume_getbyresourcegroup)
- [List](#volume_list)
- [ListByResourceGroup](#volume_listbyresourcegroup)
### Application_Create

```java
import com.azure.resourcemanager.servicefabricmesh.fluent.models.ServiceResourceDescriptionInner;
import com.azure.resourcemanager.servicefabricmesh.models.ContainerCodePackageProperties;
import com.azure.resourcemanager.servicefabricmesh.models.EndpointProperties;
import com.azure.resourcemanager.servicefabricmesh.models.EndpointRef;
import com.azure.resourcemanager.servicefabricmesh.models.NetworkRef;
import com.azure.resourcemanager.servicefabricmesh.models.OperatingSystemType;
import com.azure.resourcemanager.servicefabricmesh.models.ResourceRequests;
import com.azure.resourcemanager.servicefabricmesh.models.ResourceRequirements;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Application Create. */
public final class ApplicationCreateSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/applications/create_update.json
     */
    /**
     * Sample code: CreateOrUpdateApplication.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void createOrUpdateApplication(
        com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager
            .applications()
            .define("sampleApplication")
            .withRegion("EastUS")
            .withExistingResourceGroup("sbz_demo")
            .withTags(mapOf())
            .withDescription("Service Fabric Mesh sample application.")
            .withServices(
                Arrays
                    .asList(
                        new ServiceResourceDescriptionInner()
                            .withName("helloWorldService")
                            .withOsType(OperatingSystemType.LINUX)
                            .withCodePackages(
                                Arrays
                                    .asList(
                                        new ContainerCodePackageProperties()
                                            .withName("helloWorldCode")
                                            .withImage("seabreeze/sbz-helloworld:1.0-alpine")
                                            .withEndpoints(
                                                Arrays
                                                    .asList(
                                                        new EndpointProperties()
                                                            .withName("helloWorldListener")
                                                            .withPort(80)))
                                            .withResources(
                                                new ResourceRequirements()
                                                    .withRequests(
                                                        new ResourceRequests().withMemoryInGB(1.0).withCpu(1.0)))))
                            .withNetworkRefs(
                                Arrays
                                    .asList(
                                        new NetworkRef()
                                            .withName(
                                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/sbz_demo/providers/Microsoft.ServiceFabricMesh/networks/sampleNetwork")
                                            .withEndpointRefs(
                                                Arrays.asList(new EndpointRef().withName("helloWorldListener")))))
                            .withDescription("SeaBreeze Hello World Service.")
                            .withReplicaCount(1)))
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

### Application_Delete

```java
import com.azure.core.util.Context;

/** Samples for Application Delete. */
public final class ApplicationDeleteSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/applications/delete.json
     */
    /**
     * Sample code: DeleteApplication.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void deleteApplication(com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager.applications().deleteWithResponse("sbz_demo", "sampleApplication", Context.NONE);
    }
}
```

### Application_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Application GetByResourceGroup. */
public final class ApplicationGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/applications/get.json
     */
    /**
     * Sample code: GetApplication.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void getApplication(com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager.applications().getByResourceGroupWithResponse("sbz_demo", "sampleApplication", Context.NONE);
    }
}
```

### Application_List

```java
import com.azure.core.util.Context;

/** Samples for Application List. */
public final class ApplicationListSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/applications/list_bySubscriptionId.json
     */
    /**
     * Sample code: ListApplicationsBySubscriptionId.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void listApplicationsBySubscriptionId(
        com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager.applications().list(Context.NONE);
    }
}
```

### Application_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Application ListByResourceGroup. */
public final class ApplicationListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/applications/list_byResourceGroup.json
     */
    /**
     * Sample code: ListApplicationsByResourceGroup.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void listApplicationsByResourceGroup(
        com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager.applications().listByResourceGroup("sbz_demo", Context.NONE);
    }
}
```

### CodePackage_GetContainerLogs

```java
import com.azure.core.util.Context;

/** Samples for CodePackage GetContainerLogs. */
public final class CodePackageGetContainerLogsSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/applications/services/replicas/codepackages/get_logs.json
     */
    /**
     * Sample code: GetContainerLogs.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void getContainerLogs(com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager
            .codePackages()
            .getContainerLogsWithResponse(
                "sbz_demo", "sbzDocApp", "sbzDocService", "0", "sbzDocCode", null, Context.NONE);
    }
}
```

### Gateway_Create

```java
import com.azure.resourcemanager.servicefabricmesh.models.GatewayDestination;
import com.azure.resourcemanager.servicefabricmesh.models.HeaderMatchType;
import com.azure.resourcemanager.servicefabricmesh.models.HttpConfig;
import com.azure.resourcemanager.servicefabricmesh.models.HttpHostConfig;
import com.azure.resourcemanager.servicefabricmesh.models.HttpRouteConfig;
import com.azure.resourcemanager.servicefabricmesh.models.HttpRouteMatchHeader;
import com.azure.resourcemanager.servicefabricmesh.models.HttpRouteMatchPath;
import com.azure.resourcemanager.servicefabricmesh.models.HttpRouteMatchRule;
import com.azure.resourcemanager.servicefabricmesh.models.NetworkRef;
import com.azure.resourcemanager.servicefabricmesh.models.PathMatchType;
import com.azure.resourcemanager.servicefabricmesh.models.TcpConfig;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Gateway Create. */
public final class GatewayCreateSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/gateways/create_update.json
     */
    /**
     * Sample code: CreateOrUpdateGateway.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void createOrUpdateGateway(
        com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager
            .gateways()
            .define("sampleGateway")
            .withRegion("EastUS")
            .withExistingResourceGroup("sbz_demo")
            .withSourceNetwork(new NetworkRef().withName("Open"))
            .withDestinationNetwork(new NetworkRef().withName("helloWorldNetwork"))
            .withTags(mapOf())
            .withDescription("Service Fabric Mesh sample gateway.")
            .withTcp(
                Arrays
                    .asList(
                        new TcpConfig()
                            .withName("web")
                            .withPort(80)
                            .withDestination(
                                new GatewayDestination()
                                    .withApplicationName("helloWorldApp")
                                    .withServiceName("helloWorldService")
                                    .withEndpointName("helloWorldListener"))))
            .withHttp(
                Arrays
                    .asList(
                        new HttpConfig()
                            .withName("contosoWebsite")
                            .withPort(8081)
                            .withHosts(
                                Arrays
                                    .asList(
                                        new HttpHostConfig()
                                            .withName("contoso.com")
                                            .withRoutes(
                                                Arrays
                                                    .asList(
                                                        new HttpRouteConfig()
                                                            .withName("index")
                                                            .withMatch(
                                                                new HttpRouteMatchRule()
                                                                    .withPath(
                                                                        new HttpRouteMatchPath()
                                                                            .withValue("/index")
                                                                            .withRewrite("/")
                                                                            .withType(PathMatchType.PREFIX))
                                                                    .withHeaders(
                                                                        Arrays
                                                                            .asList(
                                                                                new HttpRouteMatchHeader()
                                                                                    .withName("accept")
                                                                                    .withValue("application/json")
                                                                                    .withType(HeaderMatchType.EXACT))))
                                                            .withDestination(
                                                                new GatewayDestination()
                                                                    .withApplicationName("httpHelloWorldApp")
                                                                    .withServiceName("indexService")
                                                                    .withEndpointName("indexHttpEndpoint"))))))))
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

### Gateway_Delete

```java
import com.azure.core.util.Context;

/** Samples for Gateway Delete. */
public final class GatewayDeleteSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/gateways/delete.json
     */
    /**
     * Sample code: DeleteGateway.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void deleteGateway(com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager.gateways().deleteWithResponse("sbz_demo", "sampleGateway", Context.NONE);
    }
}
```

### Gateway_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Gateway GetByResourceGroup. */
public final class GatewayGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/gateways/get.json
     */
    /**
     * Sample code: GetGateway.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void getGateway(com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager.gateways().getByResourceGroupWithResponse("sbz_demo", "sampleGateway", Context.NONE);
    }
}
```

### Gateway_List

```java
import com.azure.core.util.Context;

/** Samples for Gateway List. */
public final class GatewayListSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/gateways/list_bySubscriptionId.json
     */
    /**
     * Sample code: ListGatewaysBySubscriptionId.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void listGatewaysBySubscriptionId(
        com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager.gateways().list(Context.NONE);
    }
}
```

### Gateway_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Gateway ListByResourceGroup. */
public final class GatewayListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/gateways/list_byResourceGroup.json
     */
    /**
     * Sample code: ListGatewaysByResourceGroup.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void listGatewaysByResourceGroup(
        com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager.gateways().listByResourceGroup("sbz_demo", Context.NONE);
    }
}
```

### Network_Create

```java
import com.azure.resourcemanager.servicefabricmesh.models.LocalNetworkResourceProperties;
import java.util.HashMap;
import java.util.Map;

/** Samples for Network Create. */
public final class NetworkCreateSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/networks/create_update.json
     */
    /**
     * Sample code: CreateOrUpdateNetwork.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void createOrUpdateNetwork(
        com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager
            .networks()
            .define("sampleNetwork")
            .withRegion("EastUS")
            .withExistingResourceGroup("sbz_demo")
            .withProperties(
                new LocalNetworkResourceProperties()
                    .withDescription("Service Fabric Mesh sample network.")
                    .withNetworkAddressPrefix("2.0.0.0/16"))
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

### Network_Delete

```java
import com.azure.core.util.Context;

/** Samples for Network Delete. */
public final class NetworkDeleteSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/networks/delete.json
     */
    /**
     * Sample code: DeleteNetwork.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void deleteNetwork(com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager.networks().deleteWithResponse("sbz_demo", "sampleNetwork", Context.NONE);
    }
}
```

### Network_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Network GetByResourceGroup. */
public final class NetworkGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/networks/get.json
     */
    /**
     * Sample code: GetNetwork.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void getNetwork(com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager.networks().getByResourceGroupWithResponse("sbz_demo", "sampleNetwork", Context.NONE);
    }
}
```

### Network_List

```java
import com.azure.core.util.Context;

/** Samples for Network List. */
public final class NetworkListSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/networks/list_bySubscriptionId.json
     */
    /**
     * Sample code: ListNetworksBySubscriptionId.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void listNetworksBySubscriptionId(
        com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager.networks().list(Context.NONE);
    }
}
```

### Network_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Network ListByResourceGroup. */
public final class NetworkListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/networks/list_byResourceGroup.json
     */
    /**
     * Sample code: ListNetworksByResourceGroup.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void listNetworksByResourceGroup(
        com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager.networks().listByResourceGroup("sbz_demo", Context.NONE);
    }
}
```

### Secret_Create

```java
import com.azure.resourcemanager.servicefabricmesh.models.InlinedValueSecretResourceProperties;
import java.util.HashMap;
import java.util.Map;

/** Samples for Secret Create. */
public final class SecretCreateSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/secrets/create_update.json
     */
    /**
     * Sample code: CreateOrUpdateSecret.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void createOrUpdateSecret(
        com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager
            .secrets()
            .define("dbConnectionString")
            .withRegion("EastUS")
            .withExistingResourceGroup("sbz_demo")
            .withProperties(
                new InlinedValueSecretResourceProperties()
                    .withDescription("Mongo DB connection string for backend database!")
                    .withContentType("text/plain"))
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

### Secret_Delete

```java
import com.azure.core.util.Context;

/** Samples for Secret Delete. */
public final class SecretDeleteSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/secrets/delete.json
     */
    /**
     * Sample code: DeleteSecret.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void deleteSecret(com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager.secrets().deleteWithResponse("sbz_demo", "dbConnectionString", Context.NONE);
    }
}
```

### Secret_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Secret GetByResourceGroup. */
public final class SecretGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/secrets/get.json
     */
    /**
     * Sample code: GetSecret.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void getSecret(com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager.secrets().getByResourceGroupWithResponse("sbz_demo", "dbConnectionString", Context.NONE);
    }
}
```

### Secret_List

```java
import com.azure.core.util.Context;

/** Samples for Secret List. */
public final class SecretListSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/secrets/list_bySubscriptionId.json
     */
    /**
     * Sample code: ListSecretsBySubscriptionId.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void listSecretsBySubscriptionId(
        com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager.secrets().list(Context.NONE);
    }
}
```

### Secret_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Secret ListByResourceGroup. */
public final class SecretListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/secrets/list_byResourceGroup.json
     */
    /**
     * Sample code: ListSecretsByResourceGroup.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void listSecretsByResourceGroup(
        com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager.secrets().listByResourceGroup("sbz_demo", Context.NONE);
    }
}
```

### SecretValue_Create

```java
/** Samples for SecretValue Create. */
public final class SecretValueCreateSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/secrets/values/create.json
     */
    /**
     * Sample code: CreateSecretValue.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void createSecretValue(com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager
            .secretValues()
            .define("v1")
            .withRegion("West US")
            .withExistingSecret("sbz_demo", "dbConnectionString")
            .withValue(
                "mongodb://contoso123:0Fc3IolnL12312asdfawejunASDF@asdfYXX2t8a97kghVcUzcDv98hawelufhawefafnoQRGwNj2nMPL1Y9qsIr9Srdw==@contoso123.documents.azure.com:10255/mydatabase?ssl=true")
            .create();
    }
}
```

### SecretValue_Delete

```java
import com.azure.core.util.Context;

/** Samples for SecretValue Delete. */
public final class SecretValueDeleteSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/secrets/values/delete.json
     */
    /**
     * Sample code: DeleteSecretValue.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void deleteSecretValue(com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager.secretValues().deleteWithResponse("sbz_demo", "dbConnectionString", "v1", Context.NONE);
    }
}
```

### SecretValue_Get

```java
import com.azure.core.util.Context;

/** Samples for SecretValue Get. */
public final class SecretValueGetSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/secrets/values/get.json
     */
    /**
     * Sample code: GetSecretValue.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void getSecretValue(com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager.secretValues().getWithResponse("sbz_demo", "dbConnectionString", "v1", Context.NONE);
    }
}
```

### SecretValue_List

```java
import com.azure.core.util.Context;

/** Samples for SecretValue List. */
public final class SecretValueListSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/secrets/values/list.json
     */
    /**
     * Sample code: ListSecretValues.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void listSecretValues(com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager.secretValues().list("sbz_demo", "dbConnectionString", Context.NONE);
    }
}
```

### SecretValue_ListValue

```java
import com.azure.core.util.Context;

/** Samples for SecretValue ListValue. */
public final class SecretValueListValueSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/secrets/values/list_value.json
     */
    /**
     * Sample code: ListSecretValue.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void listSecretValue(com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager.secretValues().listValueWithResponse("sbz_demo", "dbConnectionString", "v1", Context.NONE);
    }
}
```

### Service_Get

```java
import com.azure.core.util.Context;

/** Samples for Service Get. */
public final class ServiceGetSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/applications/services/get.json
     */
    /**
     * Sample code: GetService.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void getService(com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager.services().getWithResponse("sbz_demo", "sampleApplication", "helloWorldService", Context.NONE);
    }
}
```

### Service_List

```java
import com.azure.core.util.Context;

/** Samples for Service List. */
public final class ServiceListSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/applications/services/list.json
     */
    /**
     * Sample code: ListServices.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void listServices(com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager.services().list("sbz_demo", "sampleApplication", Context.NONE);
    }
}
```

### ServiceReplica_Get

```java
import com.azure.core.util.Context;

/** Samples for ServiceReplica Get. */
public final class ServiceReplicaGetSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/applications/services/replicas/get.json
     */
    /**
     * Sample code: ReplicaGet.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void replicaGet(com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager.serviceReplicas().getWithResponse("sbz_demo", "helloWorldApp", "helloWorldService", "0", Context.NONE);
    }
}
```

### ServiceReplica_List

```java
import com.azure.core.util.Context;

/** Samples for ServiceReplica List. */
public final class ServiceReplicaListSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/applications/services/replicas/list.json
     */
    /**
     * Sample code: ReplicasGetAll.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void replicasGetAll(com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager.serviceReplicas().list("sbz_demo", "sampleApplication", "helloWorldService", Context.NONE);
    }
}
```

### Volume_Create

```java
import com.azure.resourcemanager.servicefabricmesh.models.VolumeProvider;
import com.azure.resourcemanager.servicefabricmesh.models.VolumeProviderParametersAzureFile;
import java.util.HashMap;
import java.util.Map;

/** Samples for Volume Create. */
public final class VolumeCreateSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/volumes/create_update.json
     */
    /**
     * Sample code: CreateOrUpdateVolume.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void createOrUpdateVolume(
        com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager
            .volumes()
            .define("sampleVolume")
            .withRegion("EastUS")
            .withExistingResourceGroup("sbz_demo")
            .withProvider(VolumeProvider.SFAZURE_FILE)
            .withTags(mapOf())
            .withDescription("Service Fabric Mesh sample volume.")
            .withAzureFileParameters(
                new VolumeProviderParametersAzureFile()
                    .withAccountName("sbzdemoaccount")
                    .withAccountKey("provide-account-key-here")
                    .withShareName("sharel"))
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

### Volume_Delete

```java
import com.azure.core.util.Context;

/** Samples for Volume Delete. */
public final class VolumeDeleteSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/volumes/delete.json
     */
    /**
     * Sample code: DeleteVolume.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void deleteVolume(com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager.volumes().deleteWithResponse("sbz_demo", "sampleVolume", Context.NONE);
    }
}
```

### Volume_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Volume GetByResourceGroup. */
public final class VolumeGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/volumes/get.json
     */
    /**
     * Sample code: GetVolume.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void getVolume(com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager.volumes().getByResourceGroupWithResponse("sbz_demo", "sampleVolume", Context.NONE);
    }
}
```

### Volume_List

```java
import com.azure.core.util.Context;

/** Samples for Volume List. */
public final class VolumeListSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/volumes/list_bySubscriptionId.json
     */
    /**
     * Sample code: ListVolumesBySubscriptionId.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void listVolumesBySubscriptionId(
        com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager.volumes().list(Context.NONE);
    }
}
```

### Volume_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Volume ListByResourceGroup. */
public final class VolumeListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/servicefabricmesh/resource-manager/Microsoft.ServiceFabricMesh/preview/2018-09-01-preview/examples/volumes/list_byResourceGroup.json
     */
    /**
     * Sample code: ListVolumesByResourceGroup.
     *
     * @param manager Entry point to ServiceFabricMeshManager.
     */
    public static void listVolumesByResourceGroup(
        com.azure.resourcemanager.servicefabricmesh.ServiceFabricMeshManager manager) {
        manager.volumes().listByResourceGroup("sbz_demo", Context.NONE);
    }
}
```

