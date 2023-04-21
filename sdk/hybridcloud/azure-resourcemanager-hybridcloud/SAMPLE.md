# Code snippets and samples


## CloudConnections

- [CreateOrUpdate](#cloudconnections_createorupdate)
- [Delete](#cloudconnections_delete)
- [GetByResourceGroup](#cloudconnections_getbyresourcegroup)
- [List](#cloudconnections_list)
- [ListByResourceGroup](#cloudconnections_listbyresourcegroup)
- [UpdateTags](#cloudconnections_updatetags)

## CloudConnectors

- [CreateOrUpdate](#cloudconnectors_createorupdate)
- [Delete](#cloudconnectors_delete)
- [DiscoverResources](#cloudconnectors_discoverresources)
- [GetByResourceGroup](#cloudconnectors_getbyresourcegroup)
- [List](#cloudconnectors_list)
- [ListByResourceGroup](#cloudconnectors_listbyresourcegroup)
- [UpdateTags](#cloudconnectors_updatetags)

## HybridCloud

- [ListOperations](#hybridcloud_listoperations)
### CloudConnections_CreateOrUpdate

```java
import com.azure.resourcemanager.hybridcloud.models.ResourceReference;

/** Samples for CloudConnections CreateOrUpdate. */
public final class CloudConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridcloud/resource-manager/Microsoft.HybridCloud/preview/2023-01-01-preview/examples/CloudConnectionCreate.json
     */
    /**
     * Sample code: Create a Cloud Connection.
     *
     * @param manager Entry point to HybridCloudManager.
     */
    public static void createACloudConnection(com.azure.resourcemanager.hybridcloud.HybridCloudManager manager) {
        manager
            .cloudConnections()
            .define("cloudconnection1")
            .withRegion("West US")
            .withExistingResourceGroup("demo-rg")
            .withCloudConnector(
                new ResourceReference()
                    .withId(
                        "/subscriptions/subid/resourceGroups/demo-rg/providers/Microsoft.HybridCloud/cloudConnectors/123456789012"))
            .withRemoteResourceId("arn:aws:ec2:us-east-1:123456789012:VPNGateway/vgw-043da592550819c8a")
            .withVirtualHub(
                new ResourceReference()
                    .withId(
                        "/subscriptions/subid/resourceGroups/demo-rg/providers/Microsoft.Network/VirtualHubs/testHub"))
            .withSharedKey("password123")
            .create();
    }
}
```

### CloudConnections_Delete

```java
/** Samples for CloudConnections Delete. */
public final class CloudConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridcloud/resource-manager/Microsoft.HybridCloud/preview/2023-01-01-preview/examples/CloudConnectionDelete.json
     */
    /**
     * Sample code: Delete Cloud Connection.
     *
     * @param manager Entry point to HybridCloudManager.
     */
    public static void deleteCloudConnection(com.azure.resourcemanager.hybridcloud.HybridCloudManager manager) {
        manager.cloudConnections().delete("demo-rg", "cloudconnection1", com.azure.core.util.Context.NONE);
    }
}
```

### CloudConnections_GetByResourceGroup

```java
/** Samples for CloudConnections GetByResourceGroup. */
public final class CloudConnectionsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridcloud/resource-manager/Microsoft.HybridCloud/preview/2023-01-01-preview/examples/CloudConnectionGet.json
     */
    /**
     * Sample code: Get Cloud Connection.
     *
     * @param manager Entry point to HybridCloudManager.
     */
    public static void getCloudConnection(com.azure.resourcemanager.hybridcloud.HybridCloudManager manager) {
        manager
            .cloudConnections()
            .getByResourceGroupWithResponse("demo-rg", "cloudConnection1", com.azure.core.util.Context.NONE);
    }
}
```

### CloudConnections_List

```java
/** Samples for CloudConnections List. */
public final class CloudConnectionsListSamples {
    /*
     * x-ms-original-file: specification/hybridcloud/resource-manager/Microsoft.HybridCloud/preview/2023-01-01-preview/examples/CloudConnectionsBySubscriptionList.json
     */
    /**
     * Sample code: List of Cloud Connections by Subscription.
     *
     * @param manager Entry point to HybridCloudManager.
     */
    public static void listOfCloudConnectionsBySubscription(
        com.azure.resourcemanager.hybridcloud.HybridCloudManager manager) {
        manager.cloudConnections().list(com.azure.core.util.Context.NONE);
    }
}
```

### CloudConnections_ListByResourceGroup

```java
/** Samples for CloudConnections ListByResourceGroup. */
public final class CloudConnectionsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridcloud/resource-manager/Microsoft.HybridCloud/preview/2023-01-01-preview/examples/CloudConnectionsByResourceGroupList.json
     */
    /**
     * Sample code: List of Cloud Connections by ResourceGroup.
     *
     * @param manager Entry point to HybridCloudManager.
     */
    public static void listOfCloudConnectionsByResourceGroup(
        com.azure.resourcemanager.hybridcloud.HybridCloudManager manager) {
        manager.cloudConnections().listByResourceGroup("demo-rg", com.azure.core.util.Context.NONE);
    }
}
```

### CloudConnections_UpdateTags

```java
import com.azure.resourcemanager.hybridcloud.models.CloudConnection;
import java.util.HashMap;
import java.util.Map;

/** Samples for CloudConnections UpdateTags. */
public final class CloudConnectionsUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/hybridcloud/resource-manager/Microsoft.HybridCloud/preview/2023-01-01-preview/examples/CloudConnectionUpdateTags.json
     */
    /**
     * Sample code: Update Cloud Connections tags.
     *
     * @param manager Entry point to HybridCloudManager.
     */
    public static void updateCloudConnectionsTags(com.azure.resourcemanager.hybridcloud.HybridCloudManager manager) {
        CloudConnection resource =
            manager
                .cloudConnections()
                .getByResourceGroupWithResponse("demo-rg", "cloudConnection1", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("key1", "value1", "key2", "value2")).apply();
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

### CloudConnectors_CreateOrUpdate

```java
import com.azure.resourcemanager.hybridcloud.models.CloudType;

/** Samples for CloudConnectors CreateOrUpdate. */
public final class CloudConnectorsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybridcloud/resource-manager/Microsoft.HybridCloud/preview/2023-01-01-preview/examples/CloudConnectorCreate.json
     */
    /**
     * Sample code: Create a Cloud Connector.
     *
     * @param manager Entry point to HybridCloudManager.
     */
    public static void createACloudConnector(com.azure.resourcemanager.hybridcloud.HybridCloudManager manager) {
        manager
            .cloudConnectors()
            .define("123456789012")
            .withRegion("West US")
            .withExistingResourceGroup("demo-rg")
            .withAccountId("123456789012")
            .withCloudType(CloudType.AWS)
            .create();
    }
}
```

### CloudConnectors_Delete

```java
/** Samples for CloudConnectors Delete. */
public final class CloudConnectorsDeleteSamples {
    /*
     * x-ms-original-file: specification/hybridcloud/resource-manager/Microsoft.HybridCloud/preview/2023-01-01-preview/examples/CloudConnectorDelete.json
     */
    /**
     * Sample code: Delete Cloud Connector.
     *
     * @param manager Entry point to HybridCloudManager.
     */
    public static void deleteCloudConnector(com.azure.resourcemanager.hybridcloud.HybridCloudManager manager) {
        manager.cloudConnectors().delete("demo-rg", "123456789012", com.azure.core.util.Context.NONE);
    }
}
```

### CloudConnectors_DiscoverResources

```java
/** Samples for CloudConnectors DiscoverResources. */
public final class CloudConnectorsDiscoverResourcesSamples {
    /*
     * x-ms-original-file: specification/hybridcloud/resource-manager/Microsoft.HybridCloud/preview/2023-01-01-preview/examples/CloudConnectorDiscoverResources.json
     */
    /**
     * Sample code: Get remote cloud resources by Cloud Connector.
     *
     * @param manager Entry point to HybridCloudManager.
     */
    public static void getRemoteCloudResourcesByCloudConnector(
        com.azure.resourcemanager.hybridcloud.HybridCloudManager manager) {
        manager.cloudConnectors().discoverResources("demo-rg", "123456789012", com.azure.core.util.Context.NONE);
    }
}
```

### CloudConnectors_GetByResourceGroup

```java
/** Samples for CloudConnectors GetByResourceGroup. */
public final class CloudConnectorsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridcloud/resource-manager/Microsoft.HybridCloud/preview/2023-01-01-preview/examples/CloudConnectorGet.json
     */
    /**
     * Sample code: Get Cloud Connector.
     *
     * @param manager Entry point to HybridCloudManager.
     */
    public static void getCloudConnector(com.azure.resourcemanager.hybridcloud.HybridCloudManager manager) {
        manager
            .cloudConnectors()
            .getByResourceGroupWithResponse("demo-rg", "123456789012", com.azure.core.util.Context.NONE);
    }
}
```

### CloudConnectors_List

```java
/** Samples for CloudConnectors List. */
public final class CloudConnectorsListSamples {
    /*
     * x-ms-original-file: specification/hybridcloud/resource-manager/Microsoft.HybridCloud/preview/2023-01-01-preview/examples/CloudConnectorsBySubscriptionList.json
     */
    /**
     * Sample code: List of Cloud Connectors by Subscription.
     *
     * @param manager Entry point to HybridCloudManager.
     */
    public static void listOfCloudConnectorsBySubscription(
        com.azure.resourcemanager.hybridcloud.HybridCloudManager manager) {
        manager.cloudConnectors().list(com.azure.core.util.Context.NONE);
    }
}
```

### CloudConnectors_ListByResourceGroup

```java
/** Samples for CloudConnectors ListByResourceGroup. */
public final class CloudConnectorsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybridcloud/resource-manager/Microsoft.HybridCloud/preview/2023-01-01-preview/examples/CloudConnectorsByResourceGroupList.json
     */
    /**
     * Sample code: List of Cloud Connectors by ResourceGroup.
     *
     * @param manager Entry point to HybridCloudManager.
     */
    public static void listOfCloudConnectorsByResourceGroup(
        com.azure.resourcemanager.hybridcloud.HybridCloudManager manager) {
        manager.cloudConnectors().listByResourceGroup("demo-rg", com.azure.core.util.Context.NONE);
    }
}
```

### CloudConnectors_UpdateTags

```java
import com.azure.resourcemanager.hybridcloud.models.CloudConnector;
import java.util.HashMap;
import java.util.Map;

/** Samples for CloudConnectors UpdateTags. */
public final class CloudConnectorsUpdateTagsSamples {
    /*
     * x-ms-original-file: specification/hybridcloud/resource-manager/Microsoft.HybridCloud/preview/2023-01-01-preview/examples/CloudConnectorUpdateTags.json
     */
    /**
     * Sample code: Update Cloud Connector tags.
     *
     * @param manager Entry point to HybridCloudManager.
     */
    public static void updateCloudConnectorTags(com.azure.resourcemanager.hybridcloud.HybridCloudManager manager) {
        CloudConnector resource =
            manager
                .cloudConnectors()
                .getByResourceGroupWithResponse("demo-rg", "123456789012", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("key1", "value1", "key2", "value2")).apply();
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

### HybridCloud_ListOperations

```java
/** Samples for HybridCloud ListOperations. */
public final class HybridCloudListOperationsSamples {
    /*
     * x-ms-original-file: specification/hybridcloud/resource-manager/Microsoft.HybridCloud/preview/2023-01-01-preview/examples/OperationGroupGet.json
     */
    /**
     * Sample code: OperationsList.
     *
     * @param manager Entry point to HybridCloudManager.
     */
    public static void operationsList(com.azure.resourcemanager.hybridcloud.HybridCloudManager manager) {
        manager.hybridClouds().listOperations(com.azure.core.util.Context.NONE);
    }
}
```

