# Code snippets and samples


## Endpoints

- [CreateOrUpdate](#endpoints_createorupdate)
- [Delete](#endpoints_delete)
- [Get](#endpoints_get)
- [List](#endpoints_list)
- [ListCredentials](#endpoints_listcredentials)
- [Update](#endpoints_update)

## Operations

- [List](#operations_list)
### Endpoints_CreateOrUpdate

```java
import com.azure.resourcemanager.hybridconnectivity.models.Type;

/** Samples for Endpoints CreateOrUpdate. */
public final class EndpointsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridconnectivity/resource-manager/Microsoft.HybridConnectivity/preview/2021-10-06-preview/examples/EndpointsPutCustom.json
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
            .withTypePropertiesType(Type.CUSTOM)
            .withResourceId(
                "/subscriptions/f5bcc1d9-23af-4ae9-aca1-041d0f593a63/resourceGroups/hybridRG/providers/Microsoft.Relay/namespaces/custom-relay-namespace")
            .create();
    }

    /*
     * x-ms-original-file: specification/hybridconnectivity/resource-manager/Microsoft.HybridConnectivity/preview/2021-10-06-preview/examples/EndpointsPutDefault.json
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
            .withTypePropertiesType(Type.DEFAULT)
            .create();
    }
}
```

### Endpoints_Delete

```java
import com.azure.core.util.Context;

/** Samples for Endpoints Delete. */
public final class EndpointsDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridconnectivity/resource-manager/Microsoft.HybridConnectivity/preview/2021-10-06-preview/examples/EndpointsDeleteDefault.json
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
            .deleteWithResponse(
                "subscriptions/f5bcc1d9-23af-4ae9-aca1-041d0f593a63/resourceGroups/hybridRG/providers/Microsoft.HybridCompute/machines/testMachine",
                "default",
                Context.NONE);
    }
}
```

### Endpoints_Get

```java
import com.azure.core.util.Context;

/** Samples for Endpoints Get. */
public final class EndpointsGetSamples {
    /*
     * x-ms-original-file: specification/hybridconnectivity/resource-manager/Microsoft.HybridConnectivity/preview/2021-10-06-preview/examples/EndpointsGetDefault.json
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
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/hybridconnectivity/resource-manager/Microsoft.HybridConnectivity/preview/2021-10-06-preview/examples/EndpointsGetCustom.json
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
                Context.NONE);
    }
}
```

### Endpoints_List

```java
import com.azure.core.util.Context;

/** Samples for Endpoints List. */
public final class EndpointsListSamples {
    /*
     * x-ms-original-file: specification/hybridconnectivity/resource-manager/Microsoft.HybridConnectivity/preview/2021-10-06-preview/examples/EndpointsList.json
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
                Context.NONE);
    }
}
```

### Endpoints_ListCredentials

```java
import com.azure.core.util.Context;

/** Samples for Endpoints ListCredentials. */
public final class EndpointsListCredentialsSamples {
    /*
     * x-ms-original-file: specification/hybridconnectivity/resource-manager/Microsoft.HybridConnectivity/preview/2021-10-06-preview/examples/EndpointsPostListCredentials.json
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
                Context.NONE);
    }
}
```

### Endpoints_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.hybridconnectivity.models.EndpointResource;
import com.azure.resourcemanager.hybridconnectivity.models.Type;

/** Samples for Endpoints Update. */
public final class EndpointsUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridconnectivity/resource-manager/Microsoft.HybridConnectivity/preview/2021-10-06-preview/examples/EndpointsPatchDefault.json
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
                    Context.NONE)
                .getValue();
        resource.update().withTypePropertiesType(Type.DEFAULT).apply();
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/hybridconnectivity/resource-manager/Microsoft.HybridConnectivity/preview/2021-10-06-preview/examples/OperationsList.json
     */
    /**
     * Sample code: HybridConnectivityOperationsList.
     *
     * @param manager Entry point to HybridConnectivityManager.
     */
    public static void hybridConnectivityOperationsList(
        com.azure.resourcemanager.hybridconnectivity.HybridConnectivityManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

