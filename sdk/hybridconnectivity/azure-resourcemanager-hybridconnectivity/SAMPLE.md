# Code snippets and samples


## Endpoints

- [CreateOrUpdate](#endpoints_createorupdate)
- [Delete](#endpoints_delete)
- [Get](#endpoints_get)
- [List](#endpoints_list)
- [ListCredentials](#endpoints_listcredentials)
- [ListIngressGatewayCredentials](#endpoints_listingressgatewaycredentials)
- [ListManagedProxyDetails](#endpoints_listmanagedproxydetails)
- [Update](#endpoints_update)

## Operations

- [List](#operations_list)

## ServiceConfigurations

- [CreateOrupdate](#serviceconfigurations_createorupdate)
- [Delete](#serviceconfigurations_delete)
- [Get](#serviceconfigurations_get)
- [ListByEndpointResource](#serviceconfigurations_listbyendpointresource)
- [Update](#serviceconfigurations_update)
### Endpoints_CreateOrUpdate

```java
import com.azure.resourcemanager.hybridconnectivity.models.EndpointProperties;
import com.azure.resourcemanager.hybridconnectivity.models.Type;

/** Samples for Endpoints CreateOrUpdate. */
public final class EndpointsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridconnectivity/resource-manager/Microsoft.HybridConnectivity/stable/2023-03-15/examples/EndpointsPutCustom.json
     */
    /**
     * Sample code: HybridConnectivityEndpointsPutCustom.
     *
     * @param manager Entry point to HybridConnectivityManager.
     */
    public static void hybridConnectivityEndpointsPutCustom(
        com.azure.resourcemanager.hybridconnectivity.HybridConnectivityManager manager) {
        manager
            .endpoints()
            .define("custom")
            .withExistingResourceUri(
                "subscriptions/f5bcc1d9-23af-4ae9-aca1-041d0f593a63/resourceGroups/hybridRG/providers/Microsoft.HybridCompute/machines/testMachine")
            .withProperties(
                new EndpointProperties()
                    .withType(Type.CUSTOM)
                    .withResourceId(
                        "/subscriptions/f5bcc1d9-23af-4ae9-aca1-041d0f593a63/resourceGroups/hybridRG/providers/Microsoft.Relay/namespaces/custom-relay-namespace"))
            .create();
    }

    /*
     * x-ms-original-file: specification/hybridconnectivity/resource-manager/Microsoft.HybridConnectivity/stable/2023-03-15/examples/EndpointsPutDefault.json
     */
    /**
     * Sample code: HybridConnectivityEndpointsPutDefault.
     *
     * @param manager Entry point to HybridConnectivityManager.
     */
    public static void hybridConnectivityEndpointsPutDefault(
        com.azure.resourcemanager.hybridconnectivity.HybridConnectivityManager manager) {
        manager
            .endpoints()
            .define("default")
            .withExistingResourceUri(
                "subscriptions/f5bcc1d9-23af-4ae9-aca1-041d0f593a63/resourceGroups/hybridRG/providers/Microsoft.HybridCompute/machines/testMachine")
            .withProperties(new EndpointProperties().withType(Type.DEFAULT))
            .create();
    }
}
```

### Endpoints_Delete

```java
/** Samples for Endpoints Delete. */
public final class EndpointsDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridconnectivity/resource-manager/Microsoft.HybridConnectivity/stable/2023-03-15/examples/EndpointsDeleteDefault.json
     */
    /**
     * Sample code: HybridConnectivityEndpointsDeleteDefault.
     *
     * @param manager Entry point to HybridConnectivityManager.
     */
    public static void hybridConnectivityEndpointsDeleteDefault(
        com.azure.resourcemanager.hybridconnectivity.HybridConnectivityManager manager) {
        manager
            .endpoints()
            .deleteByResourceGroupWithResponse(
                "subscriptions/f5bcc1d9-23af-4ae9-aca1-041d0f593a63/resourceGroups/hybridRG/providers/Microsoft.HybridCompute/machines/testMachine",
                "default",
                com.azure.core.util.Context.NONE);
    }
}
```

### Endpoints_Get

```java
/** Samples for Endpoints Get. */
public final class EndpointsGetSamples {
    /*
     * x-ms-original-file: specification/hybridconnectivity/resource-manager/Microsoft.HybridConnectivity/stable/2023-03-15/examples/EndpointsGetDefault.json
     */
    /**
     * Sample code: HybridConnectivityEndpointsGetDefault.
     *
     * @param manager Entry point to HybridConnectivityManager.
     */
    public static void hybridConnectivityEndpointsGetDefault(
        com.azure.resourcemanager.hybridconnectivity.HybridConnectivityManager manager) {
        manager
            .endpoints()
            .getWithResponse(
                "subscriptions/f5bcc1d9-23af-4ae9-aca1-041d0f593a63/resourceGroups/hybridRG/providers/Microsoft.HybridCompute/machines/testMachine",
                "default",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/hybridconnectivity/resource-manager/Microsoft.HybridConnectivity/stable/2023-03-15/examples/EndpointsGetCustom.json
     */
    /**
     * Sample code: HybridConnectivityEndpointsGetCustom.
     *
     * @param manager Entry point to HybridConnectivityManager.
     */
    public static void hybridConnectivityEndpointsGetCustom(
        com.azure.resourcemanager.hybridconnectivity.HybridConnectivityManager manager) {
        manager
            .endpoints()
            .getWithResponse(
                "subscriptions/f5bcc1d9-23af-4ae9-aca1-041d0f593a63/resourceGroups/hybridRG/providers/Microsoft.HybridCompute/machines/testMachine",
                "custom",
                com.azure.core.util.Context.NONE);
    }
}
```

### Endpoints_List

```java
/** Samples for Endpoints List. */
public final class EndpointsListSamples {
    /*
     * x-ms-original-file: specification/hybridconnectivity/resource-manager/Microsoft.HybridConnectivity/stable/2023-03-15/examples/EndpointsList.json
     */
    /**
     * Sample code: HybridConnectivityEndpointsGet.
     *
     * @param manager Entry point to HybridConnectivityManager.
     */
    public static void hybridConnectivityEndpointsGet(
        com.azure.resourcemanager.hybridconnectivity.HybridConnectivityManager manager) {
        manager
            .endpoints()
            .list(
                "subscriptions/f5bcc1d9-23af-4ae9-aca1-041d0f593a63/resourceGroups/hybridRG/providers/Microsoft.HybridCompute/machines/testMachine",
                com.azure.core.util.Context.NONE);
    }
}
```

### Endpoints_ListCredentials

```java
import com.azure.resourcemanager.hybridconnectivity.models.ListCredentialsRequest;
import com.azure.resourcemanager.hybridconnectivity.models.ServiceName;

/** Samples for Endpoints ListCredentials. */
public final class EndpointsListCredentialsSamples {
    /*
     * x-ms-original-file: specification/hybridconnectivity/resource-manager/Microsoft.HybridConnectivity/stable/2023-03-15/examples/EndpointsPostListCredentials.json
     */
    /**
     * Sample code: HybridConnectivityEndpointsPostListCredentials.
     *
     * @param manager Entry point to HybridConnectivityManager.
     */
    public static void hybridConnectivityEndpointsPostListCredentials(
        com.azure.resourcemanager.hybridconnectivity.HybridConnectivityManager manager) {
        manager
            .endpoints()
            .listCredentialsWithResponse(
                "subscriptions/f5bcc1d9-23af-4ae9-aca1-041d0f593a63/resourceGroups/hybridRG/providers/Microsoft.HybridCompute/machines/testMachine",
                "default",
                10800L,
                new ListCredentialsRequest().withServiceName(ServiceName.SSH),
                com.azure.core.util.Context.NONE);
    }
}
```

### Endpoints_ListIngressGatewayCredentials

```java
/** Samples for Endpoints ListIngressGatewayCredentials. */
public final class EndpointsListIngressGatewayCredentialsSamples {
    /*
     * x-ms-original-file: specification/hybridconnectivity/resource-manager/Microsoft.HybridConnectivity/stable/2023-03-15/examples/EndpointsPostListIngressGatewayCredentials.json
     */
    /**
     * Sample code: HybridConnectivityEndpointsPostListIngressGatewayCredentials.
     *
     * @param manager Entry point to HybridConnectivityManager.
     */
    public static void hybridConnectivityEndpointsPostListIngressGatewayCredentials(
        com.azure.resourcemanager.hybridconnectivity.HybridConnectivityManager manager) {
        manager
            .endpoints()
            .listIngressGatewayCredentialsWithResponse(
                "subscriptions/f5bcc1d9-23af-4ae9-aca1-041d0f593a63/resourceGroups/arcGroup/providers/Microsoft.ArcPlaceHolder/ProvisionedClusters/cluster0",
                "default",
                10800L,
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Endpoints_ListManagedProxyDetails

```java
import com.azure.resourcemanager.hybridconnectivity.models.ManagedProxyRequest;
import com.azure.resourcemanager.hybridconnectivity.models.ServiceName;

/** Samples for Endpoints ListManagedProxyDetails. */
public final class EndpointsListManagedProxyDetailsSamples {
    /*
     * x-ms-original-file: specification/hybridconnectivity/resource-manager/Microsoft.HybridConnectivity/stable/2023-03-15/examples/EndpointsPostListManagedProxyDetails.json
     */
    /**
     * Sample code: HybridConnectivityEndpointsPostListManagedProxyDetails.
     *
     * @param manager Entry point to HybridConnectivityManager.
     */
    public static void hybridConnectivityEndpointsPostListManagedProxyDetails(
        com.azure.resourcemanager.hybridconnectivity.HybridConnectivityManager manager) {
        manager
            .endpoints()
            .listManagedProxyDetailsWithResponse(
                "subscriptions/f5bcc1d9-23af-4ae9-aca1-041d0f593a63/resourceGroups/arcGroup/providers/Microsoft.Compute/virtualMachines/vm00006",
                "default",
                new ManagedProxyRequest()
                    .withService("127.0.0.1:65035")
                    .withHostname("r.proxy.arc.com")
                    .withServiceName(ServiceName.WAC),
                com.azure.core.util.Context.NONE);
    }
}
```

### Endpoints_Update

```java
import com.azure.resourcemanager.hybridconnectivity.models.EndpointProperties;
import com.azure.resourcemanager.hybridconnectivity.models.EndpointResource;
import com.azure.resourcemanager.hybridconnectivity.models.Type;

/** Samples for Endpoints Update. */
public final class EndpointsUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridconnectivity/resource-manager/Microsoft.HybridConnectivity/stable/2023-03-15/examples/EndpointsPatchDefault.json
     */
    /**
     * Sample code: HybridConnectivityEndpointsPatchDefault.
     *
     * @param manager Entry point to HybridConnectivityManager.
     */
    public static void hybridConnectivityEndpointsPatchDefault(
        com.azure.resourcemanager.hybridconnectivity.HybridConnectivityManager manager) {
        EndpointResource resource =
            manager
                .endpoints()
                .getWithResponse(
                    "subscriptions/f5bcc1d9-23af-4ae9-aca1-041d0f593a63/resourceGroups/hybridRG/providers/Microsoft.HybridCompute/machines/testMachine",
                    "default",
                    com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withProperties(new EndpointProperties().withType(Type.DEFAULT)).apply();
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/hybridconnectivity/resource-manager/Microsoft.HybridConnectivity/stable/2023-03-15/examples/OperationsList.json
     */
    /**
     * Sample code: HybridConnectivityOperationsList.
     *
     * @param manager Entry point to HybridConnectivityManager.
     */
    public static void hybridConnectivityOperationsList(
        com.azure.resourcemanager.hybridconnectivity.HybridConnectivityManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### ServiceConfigurations_CreateOrupdate

```java
import com.azure.resourcemanager.hybridconnectivity.models.ServiceName;

/** Samples for ServiceConfigurations CreateOrupdate. */
public final class ServiceConfigurationsCreateOrupdateSamples {
    /*
     * x-ms-original-file: specification/hybridconnectivity/resource-manager/Microsoft.HybridConnectivity/stable/2023-03-15/examples/ServiceConfigurationsPutWAC.json
     */
    /**
     * Sample code: ServiceConfigurationsPutWAC.
     *
     * @param manager Entry point to HybridConnectivityManager.
     */
    public static void serviceConfigurationsPutWAC(
        com.azure.resourcemanager.hybridconnectivity.HybridConnectivityManager manager) {
        manager
            .serviceConfigurations()
            .define("WAC")
            .withExistingEndpoint(
                "subscriptions/f5bcc1d9-23af-4ae9-aca1-041d0f593a63/resourceGroups/hybridRG/providers/Microsoft.HybridCompute/machines/testMachine/providers/Microsoft.HybridConnectivity/endpoints/default",
                "default")
            .withServiceName(ServiceName.WAC)
            .withPort(6516L)
            .create();
    }

    /*
     * x-ms-original-file: specification/hybridconnectivity/resource-manager/Microsoft.HybridConnectivity/stable/2023-03-15/examples/ServiceConfigurationsPutSSH.json
     */
    /**
     * Sample code: ServiceConfigurationsPutSSH.
     *
     * @param manager Entry point to HybridConnectivityManager.
     */
    public static void serviceConfigurationsPutSSH(
        com.azure.resourcemanager.hybridconnectivity.HybridConnectivityManager manager) {
        manager
            .serviceConfigurations()
            .define("SSH")
            .withExistingEndpoint(
                "subscriptions/f5bcc1d9-23af-4ae9-aca1-041d0f593a63/resourceGroups/hybridRG/providers/Microsoft.HybridCompute/machines/testMachine/providers/Microsoft.HybridConnectivity/endpoints/default",
                "default")
            .withServiceName(ServiceName.SSH)
            .withPort(22L)
            .create();
    }
}
```

### ServiceConfigurations_Delete

```java
/** Samples for ServiceConfigurations Delete. */
public final class ServiceConfigurationsDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridconnectivity/resource-manager/Microsoft.HybridConnectivity/stable/2023-03-15/examples/ServiceConfigurationsDeleteSSH.json
     */
    /**
     * Sample code: ServiceConfigurationsDeleteSSH.
     *
     * @param manager Entry point to HybridConnectivityManager.
     */
    public static void serviceConfigurationsDeleteSSH(
        com.azure.resourcemanager.hybridconnectivity.HybridConnectivityManager manager) {
        manager
            .serviceConfigurations()
            .deleteWithResponse(
                "subscriptions/f5bcc1d9-23af-4ae9-aca1-041d0f593a63/resourceGroups/hybridRG/providers/Microsoft.HybridCompute/machines/testMachine/providers/Microsoft.HybridConnectivity/endpoints/default",
                "default",
                "SSH",
                com.azure.core.util.Context.NONE);
    }
}
```

### ServiceConfigurations_Get

```java
/** Samples for ServiceConfigurations Get. */
public final class ServiceConfigurationsGetSamples {
    /*
     * x-ms-original-file: specification/hybridconnectivity/resource-manager/Microsoft.HybridConnectivity/stable/2023-03-15/examples/ServiceConfigurationsGetWAC.json
     */
    /**
     * Sample code: HybridConnectivityEndpointsServiceconfigurationsGetWAC.
     *
     * @param manager Entry point to HybridConnectivityManager.
     */
    public static void hybridConnectivityEndpointsServiceconfigurationsGetWAC(
        com.azure.resourcemanager.hybridconnectivity.HybridConnectivityManager manager) {
        manager
            .serviceConfigurations()
            .getWithResponse(
                "subscriptions/f5bcc1d9-23af-4ae9-aca1-041d0f593a63/resourceGroups/hybridRG/providers/Microsoft.HybridCompute/machines/testMachine/providers/Microsoft.HybridConnectivity/endpoints/default",
                "default",
                "WAC",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/hybridconnectivity/resource-manager/Microsoft.HybridConnectivity/stable/2023-03-15/examples/ServiceConfigurationsGetSSH.json
     */
    /**
     * Sample code: HybridConnectivityEndpointsServiceconfigurationsGetSSH.
     *
     * @param manager Entry point to HybridConnectivityManager.
     */
    public static void hybridConnectivityEndpointsServiceconfigurationsGetSSH(
        com.azure.resourcemanager.hybridconnectivity.HybridConnectivityManager manager) {
        manager
            .serviceConfigurations()
            .getWithResponse(
                "subscriptions/f5bcc1d9-23af-4ae9-aca1-041d0f593a63/resourceGroups/hybridRG/providers/Microsoft.HybridCompute/machines/testMachine/providers/Microsoft.HybridConnectivity/endpoints/default",
                "default",
                "SSH",
                com.azure.core.util.Context.NONE);
    }
}
```

### ServiceConfigurations_ListByEndpointResource

```java
/** Samples for ServiceConfigurations ListByEndpointResource. */
public final class ServiceConfigurationsListByEndpointResourceSamples {
    /*
     * x-ms-original-file: specification/hybridconnectivity/resource-manager/Microsoft.HybridConnectivity/stable/2023-03-15/examples/ServiceConfigurationsList.json
     */
    /**
     * Sample code: GetClustersExample.
     *
     * @param manager Entry point to HybridConnectivityManager.
     */
    public static void getClustersExample(
        com.azure.resourcemanager.hybridconnectivity.HybridConnectivityManager manager) {
        manager
            .serviceConfigurations()
            .listByEndpointResource(
                "subscriptions/f5bcc1d9-23af-4ae9-aca1-041d0f593a63/resourceGroups/hybridRG/providers/Microsoft.HybridCompute/machines/testMachine/providers/Microsoft.HybridConnectivity/endpoints/default",
                "default",
                com.azure.core.util.Context.NONE);
    }
}
```

### ServiceConfigurations_Update

```java
import com.azure.resourcemanager.hybridconnectivity.models.ServiceConfigurationResource;

/** Samples for ServiceConfigurations Update. */
public final class ServiceConfigurationsUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridconnectivity/resource-manager/Microsoft.HybridConnectivity/stable/2023-03-15/examples/ServiceConfigurationsPatchSSH.json
     */
    /**
     * Sample code: ServiceConfigurationsPatchSSH.
     *
     * @param manager Entry point to HybridConnectivityManager.
     */
    public static void serviceConfigurationsPatchSSH(
        com.azure.resourcemanager.hybridconnectivity.HybridConnectivityManager manager) {
        ServiceConfigurationResource resource =
            manager
                .serviceConfigurations()
                .getWithResponse(
                    "subscriptions/f5bcc1d9-23af-4ae9-aca1-041d0f593a63/resourceGroups/hybridRG/providers/Microsoft.HybridCompute/machines/testMachine/providers/Microsoft.HybridConnectivity/endpoints/default",
                    "default",
                    "SSH",
                    com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withPort(22L).apply();
    }
}
```

