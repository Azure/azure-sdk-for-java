# Code snippets and samples


## Apps

- [CheckNameAvailability](#apps_checknameavailability)
- [CheckSubdomainAvailability](#apps_checksubdomainavailability)
- [CreateOrUpdate](#apps_createorupdate)
- [Delete](#apps_delete)
- [GetByResourceGroup](#apps_getbyresourcegroup)
- [List](#apps_list)
- [ListByResourceGroup](#apps_listbyresourcegroup)
- [ListTemplates](#apps_listtemplates)
- [Update](#apps_update)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [Create](#privateendpointconnections_create)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [List](#privateendpointconnections_list)

## PrivateLinks

- [Get](#privatelinks_get)
- [List](#privatelinks_list)
### Apps_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.iotcentral.models.OperationInputs;

/** Samples for Apps CheckNameAvailability. */
public final class AppsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/iotcentral/resource-manager/Microsoft.IoTCentral/preview/2021-11-01-preview/examples/Apps_CheckNameAvailability.json
     */
    /**
     * Sample code: Apps_CheckNameAvailability.
     *
     * @param manager Entry point to IotCentralManager.
     */
    public static void appsCheckNameAvailability(com.azure.resourcemanager.iotcentral.IotCentralManager manager) {
        manager
            .apps()
            .checkNameAvailabilityWithResponse(
                new OperationInputs().withName("myiotcentralapp").withType("IoTApps"), Context.NONE);
    }
}
```

### Apps_CheckSubdomainAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.iotcentral.models.OperationInputs;

/** Samples for Apps CheckSubdomainAvailability. */
public final class AppsCheckSubdomainAvailabilitySamples {
    /*
     * x-ms-original-file: specification/iotcentral/resource-manager/Microsoft.IoTCentral/preview/2021-11-01-preview/examples/Apps_CheckSubdomainAvailability.json
     */
    /**
     * Sample code: Apps_SubdomainAvailability.
     *
     * @param manager Entry point to IotCentralManager.
     */
    public static void appsSubdomainAvailability(com.azure.resourcemanager.iotcentral.IotCentralManager manager) {
        manager
            .apps()
            .checkSubdomainAvailabilityWithResponse(
                new OperationInputs().withName("myiotcentralapp").withType("IoTApps"), Context.NONE);
    }
}
```

### Apps_CreateOrUpdate

```java
import com.azure.resourcemanager.iotcentral.models.AppSku;
import com.azure.resourcemanager.iotcentral.models.AppSkuInfo;
import com.azure.resourcemanager.iotcentral.models.SystemAssignedServiceIdentity;
import com.azure.resourcemanager.iotcentral.models.SystemAssignedServiceIdentityType;

/** Samples for Apps CreateOrUpdate. */
public final class AppsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/iotcentral/resource-manager/Microsoft.IoTCentral/preview/2021-11-01-preview/examples/Apps_CreateOrUpdate.json
     */
    /**
     * Sample code: Apps_CreateOrUpdate.
     *
     * @param manager Entry point to IotCentralManager.
     */
    public static void appsCreateOrUpdate(com.azure.resourcemanager.iotcentral.IotCentralManager manager) {
        manager
            .apps()
            .define("myIoTCentralApp")
            .withRegion("westus")
            .withExistingResourceGroup("resRg")
            .withSku(new AppSkuInfo().withName(AppSku.ST2))
            .withIdentity(
                new SystemAssignedServiceIdentity().withType(SystemAssignedServiceIdentityType.SYSTEM_ASSIGNED))
            .withDisplayName("My IoT Central App")
            .withSubdomain("my-iot-central-app")
            .withTemplate("iotc-pnp-preview@1.0.0")
            .create();
    }
}
```

### Apps_Delete

```java
import com.azure.core.util.Context;

/** Samples for Apps Delete. */
public final class AppsDeleteSamples {
    /*
     * x-ms-original-file: specification/iotcentral/resource-manager/Microsoft.IoTCentral/preview/2021-11-01-preview/examples/Apps_Delete.json
     */
    /**
     * Sample code: Apps_Delete.
     *
     * @param manager Entry point to IotCentralManager.
     */
    public static void appsDelete(com.azure.resourcemanager.iotcentral.IotCentralManager manager) {
        manager.apps().delete("resRg", "myIoTCentralApp", Context.NONE);
    }
}
```

### Apps_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Apps GetByResourceGroup. */
public final class AppsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/iotcentral/resource-manager/Microsoft.IoTCentral/preview/2021-11-01-preview/examples/Apps_Get.json
     */
    /**
     * Sample code: Apps_Get.
     *
     * @param manager Entry point to IotCentralManager.
     */
    public static void appsGet(com.azure.resourcemanager.iotcentral.IotCentralManager manager) {
        manager.apps().getByResourceGroupWithResponse("resRg", "myIoTCentralApp", Context.NONE);
    }
}
```

### Apps_List

```java
import com.azure.core.util.Context;

/** Samples for Apps List. */
public final class AppsListSamples {
    /*
     * x-ms-original-file: specification/iotcentral/resource-manager/Microsoft.IoTCentral/preview/2021-11-01-preview/examples/Apps_ListBySubscription.json
     */
    /**
     * Sample code: Apps_ListBySubscription.
     *
     * @param manager Entry point to IotCentralManager.
     */
    public static void appsListBySubscription(com.azure.resourcemanager.iotcentral.IotCentralManager manager) {
        manager.apps().list(Context.NONE);
    }
}
```

### Apps_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Apps ListByResourceGroup. */
public final class AppsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/iotcentral/resource-manager/Microsoft.IoTCentral/preview/2021-11-01-preview/examples/Apps_ListByResourceGroup.json
     */
    /**
     * Sample code: Apps_ListByResourceGroup.
     *
     * @param manager Entry point to IotCentralManager.
     */
    public static void appsListByResourceGroup(com.azure.resourcemanager.iotcentral.IotCentralManager manager) {
        manager.apps().listByResourceGroup("resRg", Context.NONE);
    }
}
```

### Apps_ListTemplates

```java
import com.azure.core.util.Context;

/** Samples for Apps ListTemplates. */
public final class AppsListTemplatesSamples {
    /*
     * x-ms-original-file: specification/iotcentral/resource-manager/Microsoft.IoTCentral/preview/2021-11-01-preview/examples/Apps_Templates.json
     */
    /**
     * Sample code: Apps_ListTemplates.
     *
     * @param manager Entry point to IotCentralManager.
     */
    public static void appsListTemplates(com.azure.resourcemanager.iotcentral.IotCentralManager manager) {
        manager.apps().listTemplates(Context.NONE);
    }
}
```

### Apps_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.iotcentral.models.AppPatch;
import com.azure.resourcemanager.iotcentral.models.SystemAssignedServiceIdentity;
import com.azure.resourcemanager.iotcentral.models.SystemAssignedServiceIdentityType;

/** Samples for Apps Update. */
public final class AppsUpdateSamples {
    /*
     * x-ms-original-file: specification/iotcentral/resource-manager/Microsoft.IoTCentral/preview/2021-11-01-preview/examples/Apps_Update.json
     */
    /**
     * Sample code: Apps_Update.
     *
     * @param manager Entry point to IotCentralManager.
     */
    public static void appsUpdate(com.azure.resourcemanager.iotcentral.IotCentralManager manager) {
        manager
            .apps()
            .update(
                "resRg",
                "myIoTCentralApp",
                new AppPatch()
                    .withIdentity(
                        new SystemAssignedServiceIdentity().withType(SystemAssignedServiceIdentityType.SYSTEM_ASSIGNED))
                    .withDisplayName("My IoT Central App 2"),
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
     * x-ms-original-file: specification/iotcentral/resource-manager/Microsoft.IoTCentral/preview/2021-11-01-preview/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to IotCentralManager.
     */
    public static void operationsList(com.azure.resourcemanager.iotcentral.IotCentralManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### PrivateEndpointConnections_Create

```java
import com.azure.resourcemanager.iotcentral.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.iotcentral.models.PrivateLinkServiceConnectionState;

/** Samples for PrivateEndpointConnections Create. */
public final class PrivateEndpointConnectionsCreateSamples {
    /*
     * x-ms-original-file: specification/iotcentral/resource-manager/Microsoft.IoTCentral/preview/2021-11-01-preview/examples/PrivateEndpointConnections_Update.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Update.
     *
     * @param manager Entry point to IotCentralManager.
     */
    public static void privateEndpointConnectionsUpdate(
        com.azure.resourcemanager.iotcentral.IotCentralManager manager) {
        manager
            .privateEndpointConnections()
            .define("myIoTCentralAppEndpoint")
            .withExistingIotApp("resRg", "myIoTCentralApp")
            .withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState()
                    .withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)
                    .withDescription("Auto-approved")
                    .withActionsRequired("None"))
            .create();
    }
}
```

### PrivateEndpointConnections_Delete

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Delete. */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file: specification/iotcentral/resource-manager/Microsoft.IoTCentral/preview/2021-11-01-preview/examples/PrivateEndpointConnections_Delete.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Delete.
     *
     * @param manager Entry point to IotCentralManager.
     */
    public static void privateEndpointConnectionsDelete(
        com.azure.resourcemanager.iotcentral.IotCentralManager manager) {
        manager
            .privateEndpointConnections()
            .delete("resRg", "myIoTCentralApp", "myIoTCentralAppEndpoint", Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections Get. */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file: specification/iotcentral/resource-manager/Microsoft.IoTCentral/preview/2021-11-01-preview/examples/PrivateEndpointConnections_Get.json
     */
    /**
     * Sample code: PrivateEndpointConnections_Get.
     *
     * @param manager Entry point to IotCentralManager.
     */
    public static void privateEndpointConnectionsGet(com.azure.resourcemanager.iotcentral.IotCentralManager manager) {
        manager
            .privateEndpointConnections()
            .getWithResponse("resRg", "myIoTCentralApp", "myIoTCentralAppEndpoint", Context.NONE);
    }
}
```

### PrivateEndpointConnections_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateEndpointConnections List. */
public final class PrivateEndpointConnectionsListSamples {
    /*
     * x-ms-original-file: specification/iotcentral/resource-manager/Microsoft.IoTCentral/preview/2021-11-01-preview/examples/PrivateEndpointConnections_List.json
     */
    /**
     * Sample code: PrivateEndpointConnections_List.
     *
     * @param manager Entry point to IotCentralManager.
     */
    public static void privateEndpointConnectionsList(com.azure.resourcemanager.iotcentral.IotCentralManager manager) {
        manager.privateEndpointConnections().list("resRg", "myIoTCentralApp", Context.NONE);
    }
}
```

### PrivateLinks_Get

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinks Get. */
public final class PrivateLinksGetSamples {
    /*
     * x-ms-original-file: specification/iotcentral/resource-manager/Microsoft.IoTCentral/preview/2021-11-01-preview/examples/PrivateLinks_Get.json
     */
    /**
     * Sample code: PrivateLinks_Get.
     *
     * @param manager Entry point to IotCentralManager.
     */
    public static void privateLinksGet(com.azure.resourcemanager.iotcentral.IotCentralManager manager) {
        manager.privateLinks().getWithResponse("resRg", "myIoTCentralApp", "iotApp", Context.NONE);
    }
}
```

### PrivateLinks_List

```java
import com.azure.core.util.Context;

/** Samples for PrivateLinks List. */
public final class PrivateLinksListSamples {
    /*
     * x-ms-original-file: specification/iotcentral/resource-manager/Microsoft.IoTCentral/preview/2021-11-01-preview/examples/PrivateLinks_List.json
     */
    /**
     * Sample code: PrivateLinks_List.
     *
     * @param manager Entry point to IotCentralManager.
     */
    public static void privateLinksList(com.azure.resourcemanager.iotcentral.IotCentralManager manager) {
        manager.privateLinks().list("resRg", "myIoTCentralApp", Context.NONE);
    }
}
```

