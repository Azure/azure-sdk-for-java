# Code snippets and samples


## Dra

- [Create](#dra_create)
- [Delete](#dra_delete)
- [Get](#dra_get)
- [List](#dra_list)

## DraOperationStatus

- [Get](#draoperationstatus_get)

## EmailConfiguration

- [Create](#emailconfiguration_create)
- [Get](#emailconfiguration_get)
- [List](#emailconfiguration_list)

## Event

- [Get](#event_get)
- [List](#event_list)

## Fabric

- [Create](#fabric_create)
- [Delete](#fabric_delete)
- [GetByResourceGroup](#fabric_getbyresourcegroup)
- [List](#fabric_list)
- [ListByResourceGroup](#fabric_listbyresourcegroup)
- [Update](#fabric_update)

## FabricOperationsStatus

- [Get](#fabricoperationsstatus_get)

## Operations

- [List](#operations_list)

## Policy

- [Create](#policy_create)
- [Delete](#policy_delete)
- [Get](#policy_get)
- [List](#policy_list)

## PolicyOperationStatus

- [Get](#policyoperationstatus_get)

## ProtectedItem

- [Create](#protecteditem_create)
- [Delete](#protecteditem_delete)
- [Get](#protecteditem_get)
- [List](#protecteditem_list)
- [PlannedFailover](#protecteditem_plannedfailover)

## ProtectedItemOperationStatus

- [Get](#protecteditemoperationstatus_get)

## RecoveryPoints

- [Get](#recoverypoints_get)
- [List](#recoverypoints_list)

## ReplicationExtension

- [Create](#replicationextension_create)
- [Delete](#replicationextension_delete)
- [Get](#replicationextension_get)
- [List](#replicationextension_list)

## ResourceProvider

- [CheckNameAvailability](#resourceprovider_checknameavailability)
- [DeploymentPreflight](#resourceprovider_deploymentpreflight)

## Vault

- [Create](#vault_create)
- [Delete](#vault_delete)
- [GetByResourceGroup](#vault_getbyresourcegroup)
- [List](#vault_list)
- [ListByResourceGroup](#vault_listbyresourcegroup)
- [Update](#vault_update)

## VaultOperationStatus

- [Get](#vaultoperationstatus_get)

## Workflow

- [Get](#workflow_get)
- [List](#workflow_list)

## WorkflowOperationStatus

- [Get](#workflowoperationstatus_get)
### Dra_Create

```java
import com.azure.resourcemanager.recoveryservicesdatareplication.models.DraModelCustomProperties;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.DraModelProperties;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.IdentityModel;

/** Samples for Dra Create. */
public final class DraCreateSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/Dra_Create.json
     */
    /**
     * Sample code: Dra_Create.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void draCreate(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .dras()
            .define("M")
            .withExistingReplicationFabric("rgrecoveryservicesdatareplication", "wPR")
            .withProperties(
                new DraModelProperties()
                    .withMachineId("envzcoijbqhtrpncbjbhk")
                    .withMachineName("y")
                    .withAuthenticationIdentity(
                        new IdentityModel()
                            .withTenantId("joclkkdovixwapephhxaqtefubhhmq")
                            .withApplicationId("cwktzrwajuvfyyymfstpey")
                            .withObjectId("khsiaqfbpuhp")
                            .withAudience("dkjobanyqgzenivyxhvavottpc")
                            .withAadAuthority("bubwwbowfhdmujrt"))
                    .withResourceAccessIdentity(
                        new IdentityModel()
                            .withTenantId("joclkkdovixwapephhxaqtefubhhmq")
                            .withApplicationId("cwktzrwajuvfyyymfstpey")
                            .withObjectId("khsiaqfbpuhp")
                            .withAudience("dkjobanyqgzenivyxhvavottpc")
                            .withAadAuthority("bubwwbowfhdmujrt"))
                    .withCustomProperties(new DraModelCustomProperties()))
            .create();
    }
}
```

### Dra_Delete

```java
/** Samples for Dra Delete. */
public final class DraDeleteSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/Dra_Delete.json
     */
    /**
     * Sample code: Dra_Delete.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void draDelete(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.dras().delete("rgrecoveryservicesdatareplication", "wPR", "M", com.azure.core.util.Context.NONE);
    }
}
```

### Dra_Get

```java
/** Samples for Dra Get. */
public final class DraGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/Dra_Get.json
     */
    /**
     * Sample code: Dra_Get.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void draGet(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .dras()
            .getWithResponse("rgrecoveryservicesdatareplication", "wPR", "M", com.azure.core.util.Context.NONE);
    }
}
```

### Dra_List

```java
/** Samples for Dra List. */
public final class DraListSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/Dra_List.json
     */
    /**
     * Sample code: Dra_List.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void draList(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.dras().list("rgrecoveryservicesdatareplication", "wPR", com.azure.core.util.Context.NONE);
    }
}
```

### DraOperationStatus_Get

```java
/** Samples for DraOperationStatus Get. */
public final class DraOperationStatusGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/DraOperationStatus_Get.json
     */
    /**
     * Sample code: DraOperationStatus_Get.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void draOperationStatusGet(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .draOperationStatus()
            .getWithResponse(
                "rgrecoveryservicesdatareplication", "wPR", "M", "dadsqwcq", com.azure.core.util.Context.NONE);
    }
}
```

### EmailConfiguration_Create

```java
import com.azure.resourcemanager.recoveryservicesdatareplication.models.EmailConfigurationModelProperties;
import java.util.Arrays;

/** Samples for EmailConfiguration Create. */
public final class EmailConfigurationCreateSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/EmailConfiguration_Create.json
     */
    /**
     * Sample code: EmailConfiguration_Create.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void emailConfigurationCreate(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .emailConfigurations()
            .define("0")
            .withExistingReplicationVault("rgrecoveryservicesdatareplication", "4")
            .withProperties(
                new EmailConfigurationModelProperties()
                    .withSendToOwners(true)
                    .withCustomEmailAddresses(Arrays.asList("ketvbducyailcny"))
                    .withLocale("vpnjxjvdqtebnucyxiyrjiko"))
            .create();
    }
}
```

### EmailConfiguration_Get

```java
/** Samples for EmailConfiguration Get. */
public final class EmailConfigurationGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/EmailConfiguration_Get.json
     */
    /**
     * Sample code: EmailConfiguration_Get.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void emailConfigurationGet(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .emailConfigurations()
            .getWithResponse("rgrecoveryservicesdatareplication", "4", "0", com.azure.core.util.Context.NONE);
    }
}
```

### EmailConfiguration_List

```java
/** Samples for EmailConfiguration List. */
public final class EmailConfigurationListSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/EmailConfiguration_List.json
     */
    /**
     * Sample code: EmailConfiguration_List.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void emailConfigurationList(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.emailConfigurations().list("rgrecoveryservicesdatareplication", "4", com.azure.core.util.Context.NONE);
    }
}
```

### Event_Get

```java
/** Samples for Event Get. */
public final class EventGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/Event_Get.json
     */
    /**
     * Sample code: Event_Get.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void eventGet(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .events()
            .getWithResponse("rgrecoveryservicesdatareplication", "4", "231CIG", com.azure.core.util.Context.NONE);
    }
}
```

### Event_List

```java
/** Samples for Event List. */
public final class EventListSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/Event_List.json
     */
    /**
     * Sample code: Event_List.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void eventList(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .events()
            .list(
                "rgrecoveryservicesdatareplication",
                "4",
                "wbglupjzvkirtgnnyasxom",
                "cxtufi",
                com.azure.core.util.Context.NONE);
    }
}
```

### Fabric_Create

```java
import com.azure.resourcemanager.recoveryservicesdatareplication.models.FabricModelCustomProperties;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.FabricModelProperties;
import java.util.HashMap;
import java.util.Map;

/** Samples for Fabric Create. */
public final class FabricCreateSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/Fabric_Create.json
     */
    /**
     * Sample code: Fabric_Create.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void fabricCreate(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .fabrics()
            .define("wPR")
            .withRegion("tqygutlpob")
            .withExistingResourceGroup("rgrecoveryservicesdatareplication")
            .withProperties(new FabricModelProperties().withCustomProperties(new FabricModelCustomProperties()))
            .withTags(mapOf("key3917", "fakeTokenPlaceholder"))
            .create();
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

### Fabric_Delete

```java
/** Samples for Fabric Delete. */
public final class FabricDeleteSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/Fabric_Delete.json
     */
    /**
     * Sample code: Fabric_Delete.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void fabricDelete(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.fabrics().delete("rgrecoveryservicesdatareplication", "wPR", com.azure.core.util.Context.NONE);
    }
}
```

### Fabric_GetByResourceGroup

```java
/** Samples for Fabric GetByResourceGroup. */
public final class FabricGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/Fabric_Get.json
     */
    /**
     * Sample code: Fabric_Get.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void fabricGet(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .fabrics()
            .getByResourceGroupWithResponse(
                "rgrecoveryservicesdatareplication", "wPR", com.azure.core.util.Context.NONE);
    }
}
```

### Fabric_List

```java
/** Samples for Fabric List. */
public final class FabricListSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/Fabric_ListBySubscription.json
     */
    /**
     * Sample code: Fabric_ListBySubscription.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void fabricListBySubscription(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.fabrics().list("rmgqrpzucsizbyjscxzockbiyg", com.azure.core.util.Context.NONE);
    }
}
```

### Fabric_ListByResourceGroup

```java
/** Samples for Fabric ListByResourceGroup. */
public final class FabricListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/Fabric_List.json
     */
    /**
     * Sample code: Fabric_List.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void fabricList(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .fabrics()
            .listByResourceGroup("rgrecoveryservicesdatareplication", "mjzsxwwmtvd", com.azure.core.util.Context.NONE);
    }
}
```

### Fabric_Update

```java
import com.azure.resourcemanager.recoveryservicesdatareplication.models.FabricModel;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.FabricModelCustomProperties;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.FabricModelProperties;
import java.util.HashMap;
import java.util.Map;

/** Samples for Fabric Update. */
public final class FabricUpdateSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/Fabric_Update.json
     */
    /**
     * Sample code: Fabric_Update.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void fabricUpdate(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        FabricModel resource =
            manager
                .fabrics()
                .getByResourceGroupWithResponse(
                    "rgrecoveryservicesdatareplication", "wPR", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("key6664", "fakeTokenPlaceholder"))
            .withProperties(new FabricModelProperties().withCustomProperties(new FabricModelCustomProperties()))
            .apply();
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

### FabricOperationsStatus_Get

```java
/** Samples for FabricOperationsStatus Get. */
public final class FabricOperationsStatusGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/FabricOperationsStatus_Get.json
     */
    /**
     * Sample code: FabricOperationsStatus_Get.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void fabricOperationsStatusGet(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .fabricOperationsStatus()
            .getWithResponse("rgrecoveryservicesdatareplication", "wPR", "dvfwerv", com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void operationsList(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Policy_Create

```java
import com.azure.resourcemanager.recoveryservicesdatareplication.models.PolicyModelCustomProperties;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.PolicyModelProperties;

/** Samples for Policy Create. */
public final class PolicyCreateSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/Policy_Create.json
     */
    /**
     * Sample code: Policy_Create.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void policyCreate(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .policies()
            .define("fafqwc")
            .withExistingReplicationVault("rgrecoveryservicesdatareplication", "4")
            .withProperties(new PolicyModelProperties().withCustomProperties(new PolicyModelCustomProperties()))
            .create();
    }
}
```

### Policy_Delete

```java
/** Samples for Policy Delete. */
public final class PolicyDeleteSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/Policy_Delete.json
     */
    /**
     * Sample code: Policy_Delete.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void policyDelete(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .policies()
            .delete("rgrecoveryservicesdatareplication", "4", "wqfscsdv", com.azure.core.util.Context.NONE);
    }
}
```

### Policy_Get

```java
/** Samples for Policy Get. */
public final class PolicyGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/Policy_Get.json
     */
    /**
     * Sample code: Policy_Get.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void policyGet(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .policies()
            .getWithResponse("rgrecoveryservicesdatareplication", "4", "wdqsacasc", com.azure.core.util.Context.NONE);
    }
}
```

### Policy_List

```java
/** Samples for Policy List. */
public final class PolicyListSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/Policy_List.json
     */
    /**
     * Sample code: Policy_List.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void policyList(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.policies().list("rgrecoveryservicesdatareplication", "4", com.azure.core.util.Context.NONE);
    }
}
```

### PolicyOperationStatus_Get

```java
/** Samples for PolicyOperationStatus Get. */
public final class PolicyOperationStatusGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/PolicyOperationStatus_Get.json
     */
    /**
     * Sample code: PolicyOperationStatus_Get.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void policyOperationStatusGet(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .policyOperationStatus()
            .getWithResponse(
                "rgrecoveryservicesdatareplication", "4", "xczxcwec", "wdqfsdxv", com.azure.core.util.Context.NONE);
    }
}
```

### ProtectedItem_Create

```java
import com.azure.resourcemanager.recoveryservicesdatareplication.models.ProtectedItemModelCustomProperties;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.ProtectedItemModelProperties;

/** Samples for ProtectedItem Create. */
public final class ProtectedItemCreateSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/ProtectedItem_Create.json
     */
    /**
     * Sample code: ProtectedItem_Create.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void protectedItemCreate(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .protectedItems()
            .define("d")
            .withExistingReplicationVault("rgrecoveryservicesdatareplication", "4")
            .withProperties(
                new ProtectedItemModelProperties()
                    .withPolicyName("tjoeiynplt")
                    .withReplicationExtensionName("jwxdo")
                    .withCustomProperties(new ProtectedItemModelCustomProperties()))
            .create();
    }
}
```

### ProtectedItem_Delete

```java
/** Samples for ProtectedItem Delete. */
public final class ProtectedItemDeleteSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/ProtectedItem_Delete.json
     */
    /**
     * Sample code: ProtectedItem_Delete.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void protectedItemDelete(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .protectedItems()
            .delete("rgrecoveryservicesdatareplication", "4", "d", true, com.azure.core.util.Context.NONE);
    }
}
```

### ProtectedItem_Get

```java
/** Samples for ProtectedItem Get. */
public final class ProtectedItemGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/ProtectedItem_Get.json
     */
    /**
     * Sample code: ProtectedItem_Get.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void protectedItemGet(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .protectedItems()
            .getWithResponse("rgrecoveryservicesdatareplication", "4", "d", com.azure.core.util.Context.NONE);
    }
}
```

### ProtectedItem_List

```java
/** Samples for ProtectedItem List. */
public final class ProtectedItemListSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/ProtectedItem_List.json
     */
    /**
     * Sample code: ProtectedItem_List.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void protectedItemList(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.protectedItems().list("rgrecoveryservicesdatareplication", "4", com.azure.core.util.Context.NONE);
    }
}
```

### ProtectedItem_PlannedFailover

```java
import com.azure.resourcemanager.recoveryservicesdatareplication.fluent.models.PlannedFailoverModelInner;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.PlannedFailoverModelCustomProperties;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.PlannedFailoverModelProperties;

/** Samples for ProtectedItem PlannedFailover. */
public final class ProtectedItemPlannedFailoverSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/ProtectedItem_PlannedFailover.json
     */
    /**
     * Sample code: ProtectedItem_PlannedFailover.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void protectedItemPlannedFailover(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .protectedItems()
            .plannedFailover(
                "rgrecoveryservicesdatareplication",
                "4",
                "d",
                new PlannedFailoverModelInner()
                    .withProperties(
                        new PlannedFailoverModelProperties()
                            .withCustomProperties(new PlannedFailoverModelCustomProperties())),
                com.azure.core.util.Context.NONE);
    }
}
```

### ProtectedItemOperationStatus_Get

```java
/** Samples for ProtectedItemOperationStatus Get. */
public final class ProtectedItemOperationStatusGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/ProtectedItemOperationStatus_Get.json
     */
    /**
     * Sample code: ProtectedItemOperationStatus_Get.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void protectedItemOperationStatusGet(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .protectedItemOperationStatus()
            .getWithResponse(
                "rgrecoveryservicesdatareplication", "4", "d", "wdqacsc", com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryPoints_Get

```java
/** Samples for RecoveryPoints Get. */
public final class RecoveryPointsGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/RecoveryPoints_Get.json
     */
    /**
     * Sample code: RecoveryPoints_Get.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void recoveryPointsGet(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .recoveryPoints()
            .getWithResponse("rgrecoveryservicesdatareplication", "4", "d", "1X", com.azure.core.util.Context.NONE);
    }
}
```

### RecoveryPoints_List

```java
/** Samples for RecoveryPoints List. */
public final class RecoveryPointsListSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/RecoveryPoints_List.json
     */
    /**
     * Sample code: RecoveryPoints_List.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void recoveryPointsList(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.recoveryPoints().list("rgrecoveryservicesdatareplication", "4", "d", com.azure.core.util.Context.NONE);
    }
}
```

### ReplicationExtension_Create

```java
import com.azure.resourcemanager.recoveryservicesdatareplication.models.ReplicationExtensionModelCustomProperties;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.ReplicationExtensionModelProperties;

/** Samples for ReplicationExtension Create. */
public final class ReplicationExtensionCreateSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/ReplicationExtension_Create.json
     */
    /**
     * Sample code: ReplicationExtension_Create.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void replicationExtensionCreate(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .replicationExtensions()
            .define("g16yjJ")
            .withExistingReplicationVault("rgrecoveryservicesdatareplication", "4")
            .withProperties(
                new ReplicationExtensionModelProperties()
                    .withCustomProperties(new ReplicationExtensionModelCustomProperties()))
            .create();
    }
}
```

### ReplicationExtension_Delete

```java
/** Samples for ReplicationExtension Delete. */
public final class ReplicationExtensionDeleteSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/ReplicationExtension_Delete.json
     */
    /**
     * Sample code: ReplicationExtension_Delete.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void replicationExtensionDelete(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .replicationExtensions()
            .delete("rgrecoveryservicesdatareplication", "4", "g16yjJ", com.azure.core.util.Context.NONE);
    }
}
```

### ReplicationExtension_Get

```java
/** Samples for ReplicationExtension Get. */
public final class ReplicationExtensionGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/ReplicationExtension_Get.json
     */
    /**
     * Sample code: ReplicationExtension_Get.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void replicationExtensionGet(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .replicationExtensions()
            .getWithResponse("rgrecoveryservicesdatareplication", "4", "g16yjJ", com.azure.core.util.Context.NONE);
    }
}
```

### ReplicationExtension_List

```java
/** Samples for ReplicationExtension List. */
public final class ReplicationExtensionListSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/ReplicationExtension_List.json
     */
    /**
     * Sample code: ReplicationExtension_List.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void replicationExtensionList(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .replicationExtensions()
            .list("rgrecoveryservicesdatareplication", "4", com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_CheckNameAvailability

```java
import com.azure.resourcemanager.recoveryservicesdatareplication.models.CheckNameAvailabilityModel;

/** Samples for ResourceProvider CheckNameAvailability. */
public final class ResourceProviderCheckNameAvailabilityS {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/CheckNameAvailability.json
     */
    /**
     * Sample code: CheckNameAvailability.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void checkNameAvailability(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .resourceProviders()
            .checkNameAvailabilityWithResponse(
                "trfqtbtmusswpibw",
                new CheckNameAvailabilityModel().withName("updkdcixs").withType("gngmcancdauwhdixjjvqnfkvqc"),
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_DeploymentPreflight

```java
import com.azure.resourcemanager.recoveryservicesdatareplication.fluent.models.DeploymentPreflightModelInner;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.DeploymentPreflightResource;
import java.util.Arrays;

/** Samples for ResourceProvider DeploymentPreflight. */
public final class ResourceProviderDeploymentPreflightSam {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/DeploymentPreflight.json
     */
    /**
     * Sample code: DeploymentPreflight.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void deploymentPreflight(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .resourceProviders()
            .deploymentPreflightWithResponse(
                "rgrecoveryservicesdatareplication",
                "kjoiahxljomjcmvabaobumg",
                new DeploymentPreflightModelInner()
                    .withResources(
                        Arrays
                            .asList(
                                new DeploymentPreflightResource()
                                    .withName("xtgugoflfc")
                                    .withType("nsnaptduolqcxsikrewvgjbxqpt")
                                    .withLocation("cbsgtxkjdzwbyp")
                                    .withApiVersion("otihymhvzblycdoxo"))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Vault_Create

```java
import com.azure.resourcemanager.recoveryservicesdatareplication.models.ReplicationVaultType;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.VaultModelProperties;
import java.util.HashMap;
import java.util.Map;

/** Samples for Vault Create. */
public final class VaultCreateSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/Vault_Create.json
     */
    /**
     * Sample code: Vault_Create.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void vaultCreate(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .vaults()
            .define("4")
            .withRegion("eck")
            .withExistingResourceGroup("rgrecoveryservicesdatareplication")
            .withTags(mapOf("key5359", "fakeTokenPlaceholder"))
            .withProperties(new VaultModelProperties().withVaultType(ReplicationVaultType.DISASTER_RECOVERY))
            .create();
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

### Vault_Delete

```java
/** Samples for Vault Delete. */
public final class VaultDeleteSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/Vault_Delete.json
     */
    /**
     * Sample code: Vault_Delete.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void vaultDelete(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.vaults().delete("rgrecoveryservicesdatareplication", "4", com.azure.core.util.Context.NONE);
    }
}
```

### Vault_GetByResourceGroup

```java
/** Samples for Vault GetByResourceGroup. */
public final class VaultGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/Vault_Get.json
     */
    /**
     * Sample code: Vault_Get.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void vaultGet(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .vaults()
            .getByResourceGroupWithResponse("rgrecoveryservicesdatareplication", "4", com.azure.core.util.Context.NONE);
    }
}
```

### Vault_List

```java
/** Samples for Vault List. */
public final class VaultListSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/Vault_ListBySubscription.json
     */
    /**
     * Sample code: Vault_ListBySubscription.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void vaultListBySubscription(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager.vaults().list("dqsjhseyugyexxrlrln", com.azure.core.util.Context.NONE);
    }
}
```

### Vault_ListByResourceGroup

```java
/** Samples for Vault ListByResourceGroup. */
public final class VaultListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/Vault_List.json
     */
    /**
     * Sample code: Vault_List.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void vaultList(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .vaults()
            .listByResourceGroup("rgrecoveryservicesdatareplication", "mwculdaqndp", com.azure.core.util.Context.NONE);
    }
}
```

### Vault_Update

```java
import com.azure.resourcemanager.recoveryservicesdatareplication.models.ReplicationVaultType;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.VaultModel;
import com.azure.resourcemanager.recoveryservicesdatareplication.models.VaultModelProperties;
import java.util.HashMap;
import java.util.Map;

/** Samples for Vault Update. */
public final class VaultUpdateSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/Vault_Update.json
     */
    /**
     * Sample code: Vault_Update.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void vaultUpdate(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        VaultModel resource =
            manager
                .vaults()
                .getByResourceGroupWithResponse(
                    "rgrecoveryservicesdatareplication", "4", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("key8872", "fakeTokenPlaceholder"))
            .withProperties(new VaultModelProperties().withVaultType(ReplicationVaultType.DISASTER_RECOVERY))
            .apply();
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

### VaultOperationStatus_Get

```java
/** Samples for VaultOperationStatus Get. */
public final class VaultOperationStatusGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/VaultOperationStatus_Get.json
     */
    /**
     * Sample code: VaultOperationStatus_Get.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void vaultOperationStatusGet(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .vaultOperationStatus()
            .getWithResponse("rgrecoveryservicesdatareplication", "4", "vsdvwe", com.azure.core.util.Context.NONE);
    }
}
```

### Workflow_Get

```java
/** Samples for Workflow Get. */
public final class WorkflowGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/Workflow_Get.json
     */
    /**
     * Sample code: Workflow_Get.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void workflowGet(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .workflows()
            .getWithResponse("rgrecoveryservicesdatareplication", "4", "ZGH4y", com.azure.core.util.Context.NONE);
    }
}
```

### Workflow_List

```java
/** Samples for Workflow List. */
public final class WorkflowListSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/Workflow_List.json
     */
    /**
     * Sample code: Workflow_List.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void workflowList(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .workflows()
            .list(
                "rgrecoveryservicesdatareplication",
                "4",
                "mnebpgmjcitjleipnttx",
                "rdavrzbethhslmkqgajontnxsue",
                com.azure.core.util.Context.NONE);
    }
}
```

### WorkflowOperationStatus_Get

```java
/** Samples for WorkflowOperationStatus Get. */
public final class WorkflowOperationStatusGetSamples {
    /*
     * x-ms-original-file: specification/recoveryservicesdatareplication/resource-manager/Microsoft.DataReplication/preview/2021-02-16-preview/examples/WorkflowOperationStatus_Get.json
     */
    /**
     * Sample code: WorkflowOperationStatus_Get.
     *
     * @param manager Entry point to RecoveryServicesDataReplicationManager.
     */
    public static void workflowOperationStatusGet(
        com.azure.resourcemanager.recoveryservicesdatareplication.RecoveryServicesDataReplicationManager manager) {
        manager
            .workflowOperationStatus()
            .getWithResponse(
                "rgrecoveryservicesdatareplication", "4", "ZGH4y", "wdqcxc", com.azure.core.util.Context.NONE);
    }
}
```

