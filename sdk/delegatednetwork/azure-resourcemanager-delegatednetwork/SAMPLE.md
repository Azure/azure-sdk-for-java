# Code snippets and samples


## Controller

- [Create](#controller_create)
- [Delete](#controller_delete)
- [GetByResourceGroup](#controller_getbyresourcegroup)
- [Patch](#controller_patch)

## DelegatedNetwork

- [List](#delegatednetwork_list)
- [ListByResourceGroup](#delegatednetwork_listbyresourcegroup)

## DelegatedSubnetService

- [Delete](#delegatedsubnetservice_delete)
- [GetByResourceGroup](#delegatedsubnetservice_getbyresourcegroup)
- [List](#delegatedsubnetservice_list)
- [ListByResourceGroup](#delegatedsubnetservice_listbyresourcegroup)
- [PatchDetails](#delegatedsubnetservice_patchdetails)
- [PutDetails](#delegatedsubnetservice_putdetails)

## Operations

- [List](#operations_list)

## OrchestratorInstanceService

- [Create](#orchestratorinstanceservice_create)
- [Delete](#orchestratorinstanceservice_delete)
- [GetByResourceGroup](#orchestratorinstanceservice_getbyresourcegroup)
- [List](#orchestratorinstanceservice_list)
- [ListByResourceGroup](#orchestratorinstanceservice_listbyresourcegroup)
- [Patch](#orchestratorinstanceservice_patch)
### Controller_Create

```java
/** Samples for Controller Create. */
public final class ControllerCreateSamples {
    /*
     * x-ms-original-file: specification/dnc/resource-manager/Microsoft.DelegatedNetwork/stable/2021-03-15/examples/putController.json
     */
    /**
     * Sample code: Create controller.
     *
     * @param manager Entry point to DelegatedNetworkManager.
     */
    public static void createController(com.azure.resourcemanager.delegatednetwork.DelegatedNetworkManager manager) {
        manager
            .controllers()
            .define("testcontroller")
            .withRegion("West US")
            .withExistingResourceGroup("TestRG")
            .create();
    }
}
```

### Controller_Delete

```java
import com.azure.core.util.Context;

/** Samples for Controller Delete. */
public final class ControllerDeleteSamples {
    /*
     * x-ms-original-file: specification/dnc/resource-manager/Microsoft.DelegatedNetwork/stable/2021-03-15/examples/deleteController.json
     */
    /**
     * Sample code: Delete controller.
     *
     * @param manager Entry point to DelegatedNetworkManager.
     */
    public static void deleteController(com.azure.resourcemanager.delegatednetwork.DelegatedNetworkManager manager) {
        manager.controllers().delete("TestRG", "testcontroller", Context.NONE);
    }
}
```

### Controller_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Controller GetByResourceGroup. */
public final class ControllerGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/dnc/resource-manager/Microsoft.DelegatedNetwork/stable/2021-03-15/examples/getController.json
     */
    /**
     * Sample code: Get details of a controller.
     *
     * @param manager Entry point to DelegatedNetworkManager.
     */
    public static void getDetailsOfAController(
        com.azure.resourcemanager.delegatednetwork.DelegatedNetworkManager manager) {
        manager.controllers().getByResourceGroupWithResponse("TestRG", "testcontroller", Context.NONE);
    }
}
```

### Controller_Patch

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.delegatednetwork.models.DelegatedController;
import java.util.HashMap;
import java.util.Map;

/** Samples for Controller Patch. */
public final class ControllerPatchSamples {
    /*
     * x-ms-original-file: specification/dnc/resource-manager/Microsoft.DelegatedNetwork/stable/2021-03-15/examples/patchController.json
     */
    /**
     * Sample code: update controller.
     *
     * @param manager Entry point to DelegatedNetworkManager.
     */
    public static void updateController(com.azure.resourcemanager.delegatednetwork.DelegatedNetworkManager manager) {
        DelegatedController resource =
            manager.controllers().getByResourceGroupWithResponse("TestRG", "testcontroller", Context.NONE).getValue();
        resource.update().withTags(mapOf("key", "value")).apply();
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

### DelegatedNetwork_List

```java
import com.azure.core.util.Context;

/** Samples for DelegatedNetwork List. */
public final class DelegatedNetworkListSamples {
    /*
     * x-ms-original-file: specification/dnc/resource-manager/Microsoft.DelegatedNetwork/stable/2021-03-15/examples/controllerListBySub.json
     */
    /**
     * Sample code: Get DelegatedController resources by subscription.
     *
     * @param manager Entry point to DelegatedNetworkManager.
     */
    public static void getDelegatedControllerResourcesBySubscription(
        com.azure.resourcemanager.delegatednetwork.DelegatedNetworkManager manager) {
        manager.delegatedNetworks().list(Context.NONE);
    }
}
```

### DelegatedNetwork_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for DelegatedNetwork ListByResourceGroup. */
public final class DelegatedNetworkListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/dnc/resource-manager/Microsoft.DelegatedNetwork/stable/2021-03-15/examples/controllerListByRG.json
     */
    /**
     * Sample code: Get DelegatedNetwork resources by resource group.
     *
     * @param manager Entry point to DelegatedNetworkManager.
     */
    public static void getDelegatedNetworkResourcesByResourceGroup(
        com.azure.resourcemanager.delegatednetwork.DelegatedNetworkManager manager) {
        manager.delegatedNetworks().listByResourceGroup("testRG", Context.NONE);
    }
}
```

### DelegatedSubnetService_Delete

```java
import com.azure.core.util.Context;

/** Samples for DelegatedSubnetService Delete. */
public final class DelegatedSubnetServiceDeleteSamples {
    /*
     * x-ms-original-file: specification/dnc/resource-manager/Microsoft.DelegatedNetwork/stable/2021-03-15/examples/deleteDelegatedSubnet.json
     */
    /**
     * Sample code: delete delegated subnet.
     *
     * @param manager Entry point to DelegatedNetworkManager.
     */
    public static void deleteDelegatedSubnet(
        com.azure.resourcemanager.delegatednetwork.DelegatedNetworkManager manager) {
        manager.delegatedSubnetServices().delete("TestRG", "delegated1", null, Context.NONE);
    }
}
```

### DelegatedSubnetService_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for DelegatedSubnetService GetByResourceGroup. */
public final class DelegatedSubnetServiceGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/dnc/resource-manager/Microsoft.DelegatedNetwork/stable/2021-03-15/examples/getDelegatedSubnet.json
     */
    /**
     * Sample code: Get details of a delegated subnet.
     *
     * @param manager Entry point to DelegatedNetworkManager.
     */
    public static void getDetailsOfADelegatedSubnet(
        com.azure.resourcemanager.delegatednetwork.DelegatedNetworkManager manager) {
        manager.delegatedSubnetServices().getByResourceGroupWithResponse("TestRG", "delegated1", Context.NONE);
    }
}
```

### DelegatedSubnetService_List

```java
import com.azure.core.util.Context;

/** Samples for DelegatedSubnetService List. */
public final class DelegatedSubnetServiceListSamples {
    /*
     * x-ms-original-file: specification/dnc/resource-manager/Microsoft.DelegatedNetwork/stable/2021-03-15/examples/delegatedSubnetListBySub.json
     */
    /**
     * Sample code: Get DelegatedSubnets resources by subscription.
     *
     * @param manager Entry point to DelegatedNetworkManager.
     */
    public static void getDelegatedSubnetsResourcesBySubscription(
        com.azure.resourcemanager.delegatednetwork.DelegatedNetworkManager manager) {
        manager.delegatedSubnetServices().list(Context.NONE);
    }
}
```

### DelegatedSubnetService_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for DelegatedSubnetService ListByResourceGroup. */
public final class DelegatedSubnetServiceListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/dnc/resource-manager/Microsoft.DelegatedNetwork/stable/2021-03-15/examples/delegatedSubnetListByRG.json
     */
    /**
     * Sample code: Get DelegatedSubnets resources by resource group.
     *
     * @param manager Entry point to DelegatedNetworkManager.
     */
    public static void getDelegatedSubnetsResourcesByResourceGroup(
        com.azure.resourcemanager.delegatednetwork.DelegatedNetworkManager manager) {
        manager.delegatedSubnetServices().listByResourceGroup("testRG", Context.NONE);
    }
}
```

### DelegatedSubnetService_PatchDetails

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.delegatednetwork.models.DelegatedSubnet;
import java.util.HashMap;
import java.util.Map;

/** Samples for DelegatedSubnetService PatchDetails. */
public final class DelegatedSubnetServicePatchDetailsSamples {
    /*
     * x-ms-original-file: specification/dnc/resource-manager/Microsoft.DelegatedNetwork/stable/2021-03-15/examples/patchDelegatedSubnet.json
     */
    /**
     * Sample code: patch delegated subnet.
     *
     * @param manager Entry point to DelegatedNetworkManager.
     */
    public static void patchDelegatedSubnet(
        com.azure.resourcemanager.delegatednetwork.DelegatedNetworkManager manager) {
        DelegatedSubnet resource =
            manager
                .delegatedSubnetServices()
                .getByResourceGroupWithResponse("TestRG", "delegated1", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("key", "value")).apply();
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

### DelegatedSubnetService_PutDetails

```java
import com.azure.resourcemanager.delegatednetwork.models.ControllerDetails;
import com.azure.resourcemanager.delegatednetwork.models.SubnetDetails;

/** Samples for DelegatedSubnetService PutDetails. */
public final class DelegatedSubnetServicePutDetailsSamples {
    /*
     * x-ms-original-file: specification/dnc/resource-manager/Microsoft.DelegatedNetwork/stable/2021-03-15/examples/putDelegatedSubnet.json
     */
    /**
     * Sample code: put delegated subnet.
     *
     * @param manager Entry point to DelegatedNetworkManager.
     */
    public static void putDelegatedSubnet(com.azure.resourcemanager.delegatednetwork.DelegatedNetworkManager manager) {
        manager
            .delegatedSubnetServices()
            .define("delegated1")
            .withRegion("West US")
            .withExistingResourceGroup("TestRG")
            .withSubnetDetails(
                new SubnetDetails()
                    .withId(
                        "/subscriptions/613192d7-503f-477a-9cfe-4efc3ee2bd60/resourceGroups/TestRG/providers/Microsoft.Network/virtualNetworks/testvnet/subnets/testsubnet"))
            .withControllerDetails(
                new ControllerDetails()
                    .withId(
                        "/subscriptions/613192d7-503f-477a-9cfe-4efc3ee2bd60/resourceGroups/TestRG/providers/Microsoft.DelegatedNetwork/controller/dnctestcontroller"))
            .create();
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/dnc/resource-manager/Microsoft.DelegatedNetwork/stable/2021-03-15/examples/delegatedNetworkOperationsList.json
     */
    /**
     * Sample code: Get available operations.
     *
     * @param manager Entry point to DelegatedNetworkManager.
     */
    public static void getAvailableOperations(
        com.azure.resourcemanager.delegatednetwork.DelegatedNetworkManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### OrchestratorInstanceService_Create

```java
import com.azure.resourcemanager.delegatednetwork.models.ControllerDetails;
import com.azure.resourcemanager.delegatednetwork.models.OrchestratorIdentity;
import com.azure.resourcemanager.delegatednetwork.models.OrchestratorKind;
import com.azure.resourcemanager.delegatednetwork.models.OrchestratorResourceProperties;
import com.azure.resourcemanager.delegatednetwork.models.ResourceIdentityType;

/** Samples for OrchestratorInstanceService Create. */
public final class OrchestratorInstanceServiceCreateSamples {
    /*
     * x-ms-original-file: specification/dnc/resource-manager/Microsoft.DelegatedNetwork/stable/2021-03-15/examples/putOrchestrator.json
     */
    /**
     * Sample code: Create orchestrator instance.
     *
     * @param manager Entry point to DelegatedNetworkManager.
     */
    public static void createOrchestratorInstance(
        com.azure.resourcemanager.delegatednetwork.DelegatedNetworkManager manager) {
        manager
            .orchestratorInstanceServices()
            .define("testk8s1")
            .withRegion("West US")
            .withExistingResourceGroup("TestRG")
            .withKind(OrchestratorKind.KUBERNETES)
            .withIdentity(new OrchestratorIdentity().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
            .withProperties(
                new OrchestratorResourceProperties()
                    .withOrchestratorAppId("546192d7-503f-477a-9cfe-4efc3ee2b6e1")
                    .withOrchestratorTenantId("da6192d7-503f-477a-9cfe-4efc3ee2b6c3")
                    .withClusterRootCA("ddsadsad344mfdsfdl")
                    .withApiServerEndpoint("https://testk8s.cloudapp.net")
                    .withPrivateLinkResourceId(
                        "/subscriptions/613192d7-503f-477a-9cfe-4efc3ee2bd60/resourceGroups/TestRG/providers/Microsoft.Network/privateLinkServices/plresource1")
                    .withControllerDetails(
                        new ControllerDetails()
                            .withId(
                                "/subscriptions/613192d7-503f-477a-9cfe-4efc3ee2bd60/resourceGroups/TestRG/providers/Microsoft.DelegatedNetwork/controller/testcontroller")))
            .create();
    }
}
```

### OrchestratorInstanceService_Delete

```java
import com.azure.core.util.Context;

/** Samples for OrchestratorInstanceService Delete. */
public final class OrchestratorInstanceServiceDeleteSamples {
    /*
     * x-ms-original-file: specification/dnc/resource-manager/Microsoft.DelegatedNetwork/stable/2021-03-15/examples/deleteOrchestrator.json
     */
    /**
     * Sample code: Delete Orchestrator Instance.
     *
     * @param manager Entry point to DelegatedNetworkManager.
     */
    public static void deleteOrchestratorInstance(
        com.azure.resourcemanager.delegatednetwork.DelegatedNetworkManager manager) {
        manager.orchestratorInstanceServices().delete("TestRG", "k8stest1", null, Context.NONE);
    }
}
```

### OrchestratorInstanceService_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for OrchestratorInstanceService GetByResourceGroup. */
public final class OrchestratorInstanceServiceGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/dnc/resource-manager/Microsoft.DelegatedNetwork/stable/2021-03-15/examples/getOrchestrator.json
     */
    /**
     * Sample code: Get details of a orchestratorInstance.
     *
     * @param manager Entry point to DelegatedNetworkManager.
     */
    public static void getDetailsOfAOrchestratorInstance(
        com.azure.resourcemanager.delegatednetwork.DelegatedNetworkManager manager) {
        manager.orchestratorInstanceServices().getByResourceGroupWithResponse("TestRG", "testk8s1", Context.NONE);
    }
}
```

### OrchestratorInstanceService_List

```java
import com.azure.core.util.Context;

/** Samples for OrchestratorInstanceService List. */
public final class OrchestratorInstanceServiceListSamples {
    /*
     * x-ms-original-file: specification/dnc/resource-manager/Microsoft.DelegatedNetwork/stable/2021-03-15/examples/orchestratorInstanceListBySub.json
     */
    /**
     * Sample code: Get orchestratorInstance resources by subscription.
     *
     * @param manager Entry point to DelegatedNetworkManager.
     */
    public static void getOrchestratorInstanceResourcesBySubscription(
        com.azure.resourcemanager.delegatednetwork.DelegatedNetworkManager manager) {
        manager.orchestratorInstanceServices().list(Context.NONE);
    }
}
```

### OrchestratorInstanceService_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for OrchestratorInstanceService ListByResourceGroup. */
public final class OrchestratorInstanceServiceListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/dnc/resource-manager/Microsoft.DelegatedNetwork/stable/2021-03-15/examples/orchestratorInstanceListByRG.json
     */
    /**
     * Sample code: Get OrchestratorInstance resources by resource group.
     *
     * @param manager Entry point to DelegatedNetworkManager.
     */
    public static void getOrchestratorInstanceResourcesByResourceGroup(
        com.azure.resourcemanager.delegatednetwork.DelegatedNetworkManager manager) {
        manager.orchestratorInstanceServices().listByResourceGroup("testRG", Context.NONE);
    }
}
```

### OrchestratorInstanceService_Patch

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.delegatednetwork.models.Orchestrator;
import java.util.HashMap;
import java.util.Map;

/** Samples for OrchestratorInstanceService Patch. */
public final class OrchestratorInstanceServicePatchSamples {
    /*
     * x-ms-original-file: specification/dnc/resource-manager/Microsoft.DelegatedNetwork/stable/2021-03-15/examples/patchOrchestrator.json
     */
    /**
     * Sample code: update Orchestrator Instance.
     *
     * @param manager Entry point to DelegatedNetworkManager.
     */
    public static void updateOrchestratorInstance(
        com.azure.resourcemanager.delegatednetwork.DelegatedNetworkManager manager) {
        Orchestrator resource =
            manager
                .orchestratorInstanceServices()
                .getByResourceGroupWithResponse("TestRG", "testk8s1", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("key", "value")).apply();
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

