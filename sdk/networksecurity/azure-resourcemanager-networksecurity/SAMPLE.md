# Code snippets and samples


## NeuroShieldFindings

- [Get](#neuroshieldfindings_get)
- [List](#neuroshieldfindings_list)
- [ListByNeuroShield](#neuroshieldfindings_listbyneuroshield)
- [Mitigate](#neuroshieldfindings_mitigate)
- [Rollback](#neuroshieldfindings_rollback)

## NeuroShieldMitigations

- [Execute](#neuroshieldmitigations_execute)
- [Get](#neuroshieldmitigations_get)
- [List](#neuroshieldmitigations_list)
- [ListByNeuroShield](#neuroshieldmitigations_listbyneuroshield)
- [Rollback](#neuroshieldmitigations_rollback)

## NeuroShieldOperationStatuses

- [Get](#neuroshieldoperationstatuses_get)

## NeuroShieldProtectedResources

- [CreateOrUpdate](#neuroshieldprotectedresources_createorupdate)
- [Delete](#neuroshieldprotectedresources_delete)
- [Get](#neuroshieldprotectedresources_get)
- [List](#neuroshieldprotectedresources_list)
- [TriggerInventoryUpload](#neuroshieldprotectedresources_triggerinventoryupload)

## NeuroShieldSummaries

- [Get](#neuroshieldsummaries_get)
- [List](#neuroshieldsummaries_list)

## NeuroShields

- [CreateOrUpdate](#neuroshields_createorupdate)
- [Delete](#neuroshields_delete)
- [GetByResourceGroup](#neuroshields_getbyresourcegroup)
- [List](#neuroshields_list)
- [ListByResourceGroup](#neuroshields_listbyresourcegroup)
- [Update](#neuroshields_update)

## Operations

- [List](#operations_list)
### NeuroShieldFindings_Get

```java
/**
 * Samples for NeuroShieldFindings Get.
 */
public final class NeuroShieldFindingsGetSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShieldFindings_Get.json
     */
    /**
     * Sample code: Get finding.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void getFinding(com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShieldFindings()
            .getWithResponse("myResourceGroup", "myNeuroShield", "vm-01", "CVE-2026-12345-vm-01",
                com.azure.core.util.Context.NONE);
    }
}
```

### NeuroShieldFindings_List

```java
/**
 * Samples for NeuroShieldFindings List.
 */
public final class NeuroShieldFindingsListSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShieldFindings_List.json
     */
    /**
     * Sample code: List findings.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void listFindings(com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShieldFindings()
            .list("myResourceGroup", "myNeuroShield", "vm-01", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### NeuroShieldFindings_ListByNeuroShield

```java
/**
 * Samples for NeuroShieldFindings ListByNeuroShield.
 */
public final class NeuroShieldFindingsListByNeuroShieldSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShieldFindings_ListByNeuroShield.json
     */
    /**
     * Sample code: List findings by NeuroShield.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void
        listFindingsByNeuroShield(com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShieldFindings()
            .listByNeuroShield("myResourceGroup", "myNeuroShield", 50, null, com.azure.core.util.Context.NONE);
    }
}
```

### NeuroShieldFindings_Mitigate

```java
/**
 * Samples for NeuroShieldFindings Mitigate.
 */
public final class NeuroShieldFindingsMitigateSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShieldFindings_Mitigate.json
     */
    /**
     * Sample code: Mitigate finding.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void mitigateFinding(com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShieldFindings()
            .mitigateWithResponse("myResourceGroup", "myNeuroShield", "vm-01", "CVE-2026-12345-vm-01",
                com.azure.core.util.Context.NONE);
    }
}
```

### NeuroShieldFindings_Rollback

```java
/**
 * Samples for NeuroShieldFindings Rollback.
 */
public final class NeuroShieldFindingsRollbackSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShieldFindings_Rollback.json
     */
    /**
     * Sample code: Rollback finding mitigations.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void
        rollbackFindingMitigations(com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShieldFindings()
            .rollbackWithResponse("myResourceGroup", "myNeuroShield", "vm-01", "CVE-2026-12345-vm-01",
                com.azure.core.util.Context.NONE);
    }
}
```

### NeuroShieldMitigations_Execute

```java
/**
 * Samples for NeuroShieldMitigations Execute.
 */
public final class NeuroShieldMitigationsExecuteSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShieldMitigations_Execute.json
     */
    /**
     * Sample code: Execute mitigation.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void executeMitigation(com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShieldMitigations()
            .executeWithResponse("myResourceGroup", "myNeuroShield", "vm-01", "mitigation-01",
                com.azure.core.util.Context.NONE);
    }
}
```

### NeuroShieldMitigations_Get

```java
/**
 * Samples for NeuroShieldMitigations Get.
 */
public final class NeuroShieldMitigationsGetSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShieldMitigations_Get_Failed.json
     */
    /**
     * Sample code: Get mitigation (Failed status).
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void
        getMitigationFailedStatus(com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShieldMitigations()
            .getWithResponse("myResourceGroup", "myNeuroShield", "vm-01", "mitigation-01",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShieldMitigations_Get_Succeeded.json
     */
    /**
     * Sample code: Get mitigation (Succeeded status).
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void
        getMitigationSucceededStatus(com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShieldMitigations()
            .getWithResponse("myResourceGroup", "myNeuroShield", "vm-01", "mitigation-01",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShieldMitigations_Get.json
     */
    /**
     * Sample code: Get mitigation.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void getMitigation(com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShieldMitigations()
            .getWithResponse("myResourceGroup", "myNeuroShield", "vm-01", "mitigation-01",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShieldMitigations_Get_InProgress.json
     */
    /**
     * Sample code: Get mitigation (InProgress status).
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void
        getMitigationInProgressStatus(com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShieldMitigations()
            .getWithResponse("myResourceGroup", "myNeuroShield", "vm-01", "mitigation-01",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShieldMitigations_Get_Queued.json
     */
    /**
     * Sample code: Get mitigation (Queued status).
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void
        getMitigationQueuedStatus(com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShieldMitigations()
            .getWithResponse("myResourceGroup", "myNeuroShield", "vm-01", "mitigation-01",
                com.azure.core.util.Context.NONE);
    }
}
```

### NeuroShieldMitigations_List

```java
/**
 * Samples for NeuroShieldMitigations List.
 */
public final class NeuroShieldMitigationsListSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShieldMitigations_List.json
     */
    /**
     * Sample code: List mitigations.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void listMitigations(com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShieldMitigations()
            .list("myResourceGroup", "myNeuroShield", "vm-01", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### NeuroShieldMitigations_ListByNeuroShield

```java
/**
 * Samples for NeuroShieldMitigations ListByNeuroShield.
 */
public final class NeuroShieldMitigationsListByNeuroShieldSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShieldMitigations_ListByNeuroShield.json
     */
    /**
     * Sample code: List mitigations by NeuroShield.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void
        listMitigationsByNeuroShield(com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShieldMitigations()
            .listByNeuroShield("myResourceGroup", "myNeuroShield", 50, null, com.azure.core.util.Context.NONE);
    }
}
```

### NeuroShieldMitigations_Rollback

```java
/**
 * Samples for NeuroShieldMitigations Rollback.
 */
public final class NeuroShieldMitigationsRollbackSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShieldMitigations_Rollback.json
     */
    /**
     * Sample code: Rollback mitigation.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void rollbackMitigation(com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShieldMitigations()
            .rollbackWithResponse("myResourceGroup", "myNeuroShield", "vm-01", "mitigation-01",
                com.azure.core.util.Context.NONE);
    }
}
```

### NeuroShieldOperationStatuses_Get

```java
/**
 * Samples for NeuroShieldOperationStatuses Get.
 */
public final class NeuroShieldOperationStatusesGetSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShieldOperationStatuses_Get.json
     */
    /**
     * Sample code: NeuroShieldOperationStatuses_Get.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void
        neuroShieldOperationStatusesGet(com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShieldOperationStatuses()
            .getWithResponse("eastus2", "00000000-0000-0000-0000-000000000001", com.azure.core.util.Context.NONE);
    }
}
```

### NeuroShieldProtectedResources_CreateOrUpdate

```java
import com.azure.resourcemanager.networksecurity.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.networksecurity.models.NeuroShieldProtectedResourceIdentity;
import com.azure.resourcemanager.networksecurity.models.NeuroShieldProtectedResourceIdentityUserAssignedIdentities;
import com.azure.resourcemanager.networksecurity.models.NeuroShieldProtectedResourceProperties;
import com.azure.resourcemanager.networksecurity.models.NeuroShieldProtectedResourceUserAssignedIdentity;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for NeuroShieldProtectedResources CreateOrUpdate.
 */
public final class NeuroShieldProtectedResourcesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShieldProtectedResources_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or update protected resource with a user-assigned target identity.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void createOrUpdateProtectedResourceWithAUserAssignedTargetIdentity(
        com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShieldProtectedResources()
            .define("vm-01")
            .withExistingNeuroShield("myResourceGroup", "myNeuroShield")
            .withProperties(new NeuroShieldProtectedResourceProperties().withTargetResourceId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myComputeRg/providers/Microsoft.Compute/virtualMachines/vm-01")
                .withTargetIdentity(new NeuroShieldProtectedResourceIdentity()
                    .withType(ManagedServiceIdentityType.USER_ASSIGNED)
                    .withUserAssignedIdentities(
                        new NeuroShieldProtectedResourceIdentityUserAssignedIdentities().withAdditionalProperties(mapOf(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myIdentityRg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/neuronIdentity",
                            new NeuroShieldProtectedResourceUserAssignedIdentity()
                                .withPrincipalId("11111111-1111-1111-1111-111111111111")
                                .withClientId("22222222-2222-2222-2222-222222222222"))))))
            .create();
    }

    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShieldProtectedResources_CreateOrUpdate_WithoutIdentity.json
     */
    /**
     * Sample code: Create or update protected resource without an identity.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void createOrUpdateProtectedResourceWithoutAnIdentity(
        com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShieldProtectedResources()
            .define("vm-01")
            .withExistingNeuroShield("myResourceGroup", "myNeuroShield")
            .withProperties(new NeuroShieldProtectedResourceProperties().withTargetResourceId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myComputeRg/providers/Microsoft.Compute/virtualMachines/vm-01"))
            .create();
    }

    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShieldProtectedResources_CreateOrUpdate_NoneIdentity.json
     */
    /**
     * Sample code: Create or update protected resource with explicit None identity.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void createOrUpdateProtectedResourceWithExplicitNoneIdentity(
        com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShieldProtectedResources()
            .define("vm-01")
            .withExistingNeuroShield("myResourceGroup", "myNeuroShield")
            .withProperties(new NeuroShieldProtectedResourceProperties().withTargetResourceId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myComputeRg/providers/Microsoft.Compute/virtualMachines/vm-01")
                .withTargetIdentity(
                    new NeuroShieldProtectedResourceIdentity().withType(ManagedServiceIdentityType.NONE)))
            .create();
    }

    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShieldProtectedResources_CreateOrUpdate_SystemAssigned.json
     */
    /**
     * Sample code: Create or update protected resource with a system-assigned target identity.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void createOrUpdateProtectedResourceWithASystemAssignedTargetIdentity(
        com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShieldProtectedResources()
            .define("vm-01")
            .withExistingNeuroShield("myResourceGroup", "myNeuroShield")
            .withProperties(new NeuroShieldProtectedResourceProperties().withTargetResourceId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myComputeRg/providers/Microsoft.Compute/virtualMachines/vm-01")
                .withTargetIdentity(
                    new NeuroShieldProtectedResourceIdentity().withPrincipalId("11111111-1111-1111-1111-111111111111")
                        .withTenantId("22222222-2222-2222-2222-222222222222")
                        .withType(ManagedServiceIdentityType.SYSTEM_ASSIGNED)))
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

### NeuroShieldProtectedResources_Delete

```java
/**
 * Samples for NeuroShieldProtectedResources Delete.
 */
public final class NeuroShieldProtectedResourcesDeleteSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShieldProtectedResources_Delete.json
     */
    /**
     * Sample code: Delete protected resource.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void
        deleteProtectedResource(com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShieldProtectedResources()
            .delete("myResourceGroup", "myNeuroShield", "vm-01", null, com.azure.core.util.Context.NONE);
    }
}
```

### NeuroShieldProtectedResources_Get

```java
/**
 * Samples for NeuroShieldProtectedResources Get.
 */
public final class NeuroShieldProtectedResourcesGetSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShieldProtectedResources_Get.json
     */
    /**
     * Sample code: Get protected resource.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void getProtectedResource(com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShieldProtectedResources()
            .getWithResponse("myResourceGroup", "myNeuroShield", "vm-01", com.azure.core.util.Context.NONE);
    }
}
```

### NeuroShieldProtectedResources_List

```java
/**
 * Samples for NeuroShieldProtectedResources List.
 */
public final class NeuroShieldProtectedResourcesListSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShieldProtectedResources_List.json
     */
    /**
     * Sample code: List protected resources.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void
        listProtectedResources(com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShieldProtectedResources()
            .list("myResourceGroup", "myNeuroShield", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### NeuroShieldProtectedResources_TriggerInventoryUpload

```java
/**
 * Samples for NeuroShieldProtectedResources TriggerInventoryUpload.
 */
public final class NeuroShieldProtectedResourcesTriggerInventoryUploadSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShieldProtectedResources_TriggerInventoryUpload.json
     */
    /**
     * Sample code: Trigger an inventory upload.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void
        triggerAnInventoryUpload(com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShieldProtectedResources()
            .triggerInventoryUploadWithResponse("myResourceGroup", "myNeuroShield", "vm-01",
                com.azure.core.util.Context.NONE);
    }
}
```

### NeuroShieldSummaries_Get

```java
/**
 * Samples for NeuroShieldSummaries Get.
 */
public final class NeuroShieldSummariesGetSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShieldSummaries_Get.json
     */
    /**
     * Sample code: Get a NeuroShield fleet summary.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void
        getANeuroShieldFleetSummary(com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShieldSummaries()
            .getWithResponse("myResourceGroup", "myNeuroShield", com.azure.core.util.Context.NONE);
    }
}
```

### NeuroShieldSummaries_List

```java
/**
 * Samples for NeuroShieldSummaries List.
 */
public final class NeuroShieldSummariesListSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShieldSummaries_List.json
     */
    /**
     * Sample code: List NeuroShield fleet summaries.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void
        listNeuroShieldFleetSummaries(com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShieldSummaries().list("myResourceGroup", "myNeuroShield", null, com.azure.core.util.Context.NONE);
    }
}
```

### NeuroShields_CreateOrUpdate

```java
import com.azure.resourcemanager.networksecurity.models.NeuroShieldProperties;
import com.azure.resourcemanager.networksecurity.models.NeuroShieldResponseMode;
import com.azure.resourcemanager.networksecurity.models.NeuroShieldSeverityThreshold;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for NeuroShields CreateOrUpdate.
 */
public final class NeuroShieldsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShields_CreateOrUpdate_OmittedOptionalProperties.json
     */
    /**
     * Sample code: Create or update NeuroShield with omitted optional properties.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void createOrUpdateNeuroShieldWithOmittedOptionalProperties(
        com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShields()
            .define("myNeuroShield")
            .withRegion("eastus2")
            .withExistingResourceGroup("myResourceGroup")
            .withProperties(new NeuroShieldProperties().withResponseMode(NeuroShieldResponseMode.MANUAL))
            .withIfMatch("\"neuroshield-etag\"")
            .create();
    }

    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShields_CreateOrUpdate.json
     */
    /**
     * Sample code: Create or update NeuroShield resource.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void
        createOrUpdateNeuroShieldResource(com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShields()
            .define("myNeuroShield")
            .withRegion("eastus2")
            .withExistingResourceGroup("myResourceGroup")
            .withProperties(new NeuroShieldProperties().withResponseMode(NeuroShieldResponseMode.MANUAL)
                .withSeverityThreshold(NeuroShieldSeverityThreshold.MEDIUM)
                .withDescription("Test NeuroShield resource"))
            .withTags(mapOf("env", "test"))
            .withIfMatch("\"neuroshield-etag\"")
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

### NeuroShields_Delete

```java
/**
 * Samples for NeuroShields Delete.
 */
public final class NeuroShieldsDeleteSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShields_Delete.json
     */
    /**
     * Sample code: Delete NeuroShield resource.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void
        deleteNeuroShieldResource(com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShields()
            .deleteByResourceGroupWithResponse("myResourceGroup", "myNeuroShield", com.azure.core.util.Context.NONE);
    }
}
```

### NeuroShields_GetByResourceGroup

```java
/**
 * Samples for NeuroShields GetByResourceGroup.
 */
public final class NeuroShieldsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShields_Get.json
     */
    /**
     * Sample code: Get NeuroShield resource.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void
        getNeuroShieldResource(com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShields()
            .getByResourceGroupWithResponse("myResourceGroup", "myNeuroShield", com.azure.core.util.Context.NONE);
    }
}
```

### NeuroShields_List

```java
/**
 * Samples for NeuroShields List.
 */
public final class NeuroShieldsListSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShields_ListBySubscription.json
     */
    /**
     * Sample code: List NeuroShield resources by subscription.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void listNeuroShieldResourcesBySubscription(
        com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShields().list(null, null, com.azure.core.util.Context.NONE);
    }
}
```

### NeuroShields_ListByResourceGroup

```java
/**
 * Samples for NeuroShields ListByResourceGroup.
 */
public final class NeuroShieldsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShields_ListByResourceGroup.json
     */
    /**
     * Sample code: List NeuroShield resources by resource group.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void listNeuroShieldResourcesByResourceGroup(
        com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.neuroShields().listByResourceGroup("myResourceGroup", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### NeuroShields_Update

```java
import com.azure.resourcemanager.networksecurity.models.NeuroShieldPatchProperties;
import com.azure.resourcemanager.networksecurity.models.NeuroShieldPatchTags;
import com.azure.resourcemanager.networksecurity.models.NeuroShieldResource;
import com.azure.resourcemanager.networksecurity.models.NeuroShieldResponseMode;
import com.azure.resourcemanager.networksecurity.models.NeuroShieldSeverityThreshold;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for NeuroShields Update.
 */
public final class NeuroShieldsUpdateSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShields_Update_OmittedProperties.json
     */
    /**
     * Sample code: Update tags while preserving omitted PATCH properties.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void updateTagsWhilePreservingOmittedPATCHProperties(
        com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        NeuroShieldResource resource = manager.neuroShields()
            .getByResourceGroupWithResponse("myResourceGroup", "myNeuroShield", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(new NeuroShieldPatchTags().withAdditionalProperties(mapOf()))
            .withIfMatch("\"neuroshield-etag\"")
            .apply();
    }

    /*
     * x-ms-original-file: 2026-10-01-preview/NeuroShields_Update.json
     */
    /**
     * Sample code: Update NeuroShield resource.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void
        updateNeuroShieldResource(com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        NeuroShieldResource resource = manager.neuroShields()
            .getByResourceGroupWithResponse("myResourceGroup", "myNeuroShield", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(new NeuroShieldPatchTags().withAdditionalProperties(mapOf("env", "production")))
            .withProperties(new NeuroShieldPatchProperties().withResponseMode(NeuroShieldResponseMode.AUTO)
                .withSeverityThreshold(NeuroShieldSeverityThreshold.HIGH)
                .withDescription("Updated NeuroShield resource"))
            .withIfMatch("\"neuroshield-etag\"")
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

### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2026-10-01-preview/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void operationsList(com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: 2026-10-01-preview/Operations_List_Public.json
     */
    /**
     * Sample code: List provider operations.
     * 
     * @param manager Entry point to NetworkSecurityManager.
     */
    public static void
        listProviderOperations(com.azure.resourcemanager.networksecurity.NetworkSecurityManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

