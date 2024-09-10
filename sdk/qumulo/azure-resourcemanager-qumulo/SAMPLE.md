# Code snippets and samples


## FileSystems

- [CreateOrUpdate](#filesystems_createorupdate)
- [Delete](#filesystems_delete)
- [GetByResourceGroup](#filesystems_getbyresourcegroup)
- [List](#filesystems_list)
- [ListByResourceGroup](#filesystems_listbyresourcegroup)
- [Update](#filesystems_update)

## Operations

- [List](#operations_list)
### FileSystems_CreateOrUpdate

```java
import com.azure.resourcemanager.qumulo.models.ManagedServiceIdentity;
import com.azure.resourcemanager.qumulo.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.qumulo.models.MarketplaceDetails;
import com.azure.resourcemanager.qumulo.models.UserAssignedIdentity;
import com.azure.resourcemanager.qumulo.models.UserDetails;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for FileSystems CreateOrUpdate.
 */
public final class FileSystemsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/liftrqumulo/resource-manager/Qumulo.Storage/stable/2024-06-19/examples/
     * FileSystems_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_CreateOrUpdate.
     * 
     * @param manager Entry point to QumuloManager.
     */
    public static void fileSystemsCreateOrUpdate(com.azure.resourcemanager.qumulo.QumuloManager manager) {
        manager.fileSystems()
            .define("hfcmtgaes")
            .withRegion("pnb")
            .withExistingResourceGroup("rgQumulo")
            .withTags(mapOf("key7090", "fakeTokenPlaceholder"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.NONE)
                .withUserAssignedIdentities(mapOf("key7679", new UserAssignedIdentity())))
            .withMarketplaceDetails(new MarketplaceDetails().withMarketplaceSubscriptionId("xaqtkloiyovmexqhn")
                .withPlanId("fwtpz")
                .withOfferId("s")
                .withPublisherId("czxcfrwodazyaft")
                .withTermUnit("cfwwczmygsimcyvoclcw"))
            .withStorageSku("yhyzby")
            .withUserDetails(new UserDetails().withEmail("aqsnzyroo"))
            .withDelegatedSubnetId("jykmxrf")
            .withClusterLoginUrl("ykaynsjvhihdthkkvvodjrgc")
            .withPrivateIPs(Arrays.asList("gzken"))
            .withAdminPassword("fakeTestSecretPlaceholder")
            .withAvailabilityZone("eqdvbdiuwmhhzqzmksmwllpddqquwt")
            .create();
    }

    /*
     * x-ms-original-file: specification/liftrqumulo/resource-manager/Qumulo.Storage/stable/2024-06-19/examples/
     * FileSystems_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_CreateOrUpdate_MinimumSet_Gen.
     * 
     * @param manager Entry point to QumuloManager.
     */
    public static void fileSystemsCreateOrUpdateMinimumSetGen(com.azure.resourcemanager.qumulo.QumuloManager manager) {
        manager.fileSystems()
            .define("aaaaaaaa")
            .withRegion("aaaaaaaaaaaaaaaaaaaaaaaaa")
            .withExistingResourceGroup("rgopenapi")
            .withMarketplaceDetails(new MarketplaceDetails().withMarketplaceSubscriptionId("aaaaaaaaaaaaa")
                .withPlanId("aaaaaa")
                .withOfferId("aaaaaaaaaaaaaaaaaaaaaaaaa"))
            .withStorageSku("Standard")
            .withUserDetails(new UserDetails().withEmail("viptslwulnpaupfljvnjeq"))
            .withDelegatedSubnetId("aaaaaaaaaa")
            .withAdminPassword("fakeTestSecretPlaceholder")
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

### FileSystems_Delete

```java
/**
 * Samples for FileSystems Delete.
 */
public final class FileSystemsDeleteSamples {
    /*
     * x-ms-original-file: specification/liftrqumulo/resource-manager/Qumulo.Storage/stable/2024-06-19/examples/
     * FileSystems_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_Delete.
     * 
     * @param manager Entry point to QumuloManager.
     */
    public static void fileSystemsDelete(com.azure.resourcemanager.qumulo.QumuloManager manager) {
        manager.fileSystems().delete("rgQumulo", "xoschzkccroahrykedlvbbnsddq", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/liftrqumulo/resource-manager/Qumulo.Storage/stable/2024-06-19/examples/
     * FileSystems_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_Delete_MinimumSet_Gen.
     * 
     * @param manager Entry point to QumuloManager.
     */
    public static void fileSystemsDeleteMinimumSetGen(com.azure.resourcemanager.qumulo.QumuloManager manager) {
        manager.fileSystems().delete("rgQumulo", "jgtskkiplquyrlkaxvhdg", com.azure.core.util.Context.NONE);
    }
}
```

### FileSystems_GetByResourceGroup

```java
/**
 * Samples for FileSystems GetByResourceGroup.
 */
public final class FileSystemsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/liftrqumulo/resource-manager/Qumulo.Storage/stable/2024-06-19/examples/
     * FileSystems_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_Get.
     * 
     * @param manager Entry point to QumuloManager.
     */
    public static void fileSystemsGet(com.azure.resourcemanager.qumulo.QumuloManager manager) {
        manager.fileSystems()
            .getByResourceGroupWithResponse("rgQumulo", "sihbehcisdqtqqyfiewiiaphgh", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/liftrqumulo/resource-manager/Qumulo.Storage/stable/2024-06-19/examples/
     * FileSystems_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_Get_MinimumSet_Gen.
     * 
     * @param manager Entry point to QumuloManager.
     */
    public static void fileSystemsGetMinimumSetGen(com.azure.resourcemanager.qumulo.QumuloManager manager) {
        manager.fileSystems()
            .getByResourceGroupWithResponse("rgQumulo", "aaaaaaaaaaaaaaaaa", com.azure.core.util.Context.NONE);
    }
}
```

### FileSystems_List

```java
/**
 * Samples for FileSystems List.
 */
public final class FileSystemsListSamples {
    /*
     * x-ms-original-file: specification/liftrqumulo/resource-manager/Qumulo.Storage/stable/2024-06-19/examples/
     * FileSystems_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_ListBySubscription.
     * 
     * @param manager Entry point to QumuloManager.
     */
    public static void fileSystemsListBySubscription(com.azure.resourcemanager.qumulo.QumuloManager manager) {
        manager.fileSystems().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/liftrqumulo/resource-manager/Qumulo.Storage/stable/2024-06-19/examples/
     * FileSystems_ListBySubscription_MinimumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_ListBySubscription_MinimumSet_Gen.
     * 
     * @param manager Entry point to QumuloManager.
     */
    public static void
        fileSystemsListBySubscriptionMinimumSetGen(com.azure.resourcemanager.qumulo.QumuloManager manager) {
        manager.fileSystems().list(com.azure.core.util.Context.NONE);
    }
}
```

### FileSystems_ListByResourceGroup

```java
/**
 * Samples for FileSystems ListByResourceGroup.
 */
public final class FileSystemsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/liftrqumulo/resource-manager/Qumulo.Storage/stable/2024-06-19/examples/
     * FileSystems_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_ListByResourceGroup.
     * 
     * @param manager Entry point to QumuloManager.
     */
    public static void fileSystemsListByResourceGroup(com.azure.resourcemanager.qumulo.QumuloManager manager) {
        manager.fileSystems().listByResourceGroup("rgQumulo", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/liftrqumulo/resource-manager/Qumulo.Storage/stable/2024-06-19/examples/
     * FileSystems_ListByResourceGroup_MinimumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_ListByResourceGroup_MinimumSet_Gen.
     * 
     * @param manager Entry point to QumuloManager.
     */
    public static void
        fileSystemsListByResourceGroupMinimumSetGen(com.azure.resourcemanager.qumulo.QumuloManager manager) {
        manager.fileSystems().listByResourceGroup("rgQumulo", com.azure.core.util.Context.NONE);
    }
}
```

### FileSystems_Update

```java
import com.azure.resourcemanager.qumulo.models.FileSystemResource;
import com.azure.resourcemanager.qumulo.models.FileSystemResourceUpdateProperties;
import com.azure.resourcemanager.qumulo.models.ManagedServiceIdentity;
import com.azure.resourcemanager.qumulo.models.ManagedServiceIdentityType;
import com.azure.resourcemanager.qumulo.models.MarketplaceDetails;
import com.azure.resourcemanager.qumulo.models.UserAssignedIdentity;
import com.azure.resourcemanager.qumulo.models.UserDetails;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for FileSystems Update.
 */
public final class FileSystemsUpdateSamples {
    /*
     * x-ms-original-file: specification/liftrqumulo/resource-manager/Qumulo.Storage/stable/2024-06-19/examples/
     * FileSystems_Update_MinimumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_Update_MinimumSet_Gen.
     * 
     * @param manager Entry point to QumuloManager.
     */
    public static void fileSystemsUpdateMinimumSetGen(com.azure.resourcemanager.qumulo.QumuloManager manager) {
        FileSystemResource resource = manager.fileSystems()
            .getByResourceGroupWithResponse("rgQumulo", "aaaaaaaaaaaaaaaaa", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().apply();
    }

    /*
     * x-ms-original-file: specification/liftrqumulo/resource-manager/Qumulo.Storage/stable/2024-06-19/examples/
     * FileSystems_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_Update.
     * 
     * @param manager Entry point to QumuloManager.
     */
    public static void fileSystemsUpdate(com.azure.resourcemanager.qumulo.QumuloManager manager) {
        FileSystemResource resource = manager.fileSystems()
            .getByResourceGroupWithResponse("rgQumulo", "ahpixnvykleksjlr", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key357", "fakeTokenPlaceholder"))
            .withIdentity(new ManagedServiceIdentity().withType(ManagedServiceIdentityType.NONE)
                .withUserAssignedIdentities(mapOf("key7679", new UserAssignedIdentity())))
            .withProperties(new FileSystemResourceUpdateProperties()
                .withMarketplaceDetails(new MarketplaceDetails().withMarketplaceSubscriptionId("xaqtkloiyovmexqhn")
                    .withPlanId("fwtpz")
                    .withOfferId("s")
                    .withPublisherId("czxcfrwodazyaft")
                    .withTermUnit("cfwwczmygsimcyvoclcw"))
                .withUserDetails(new UserDetails().withEmail("aqsnzyroo"))
                .withDelegatedSubnetId("bqaryqsjlackxphpmzffgoqsvm"))
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
     * x-ms-original-file: specification/liftrqumulo/resource-manager/Qumulo.Storage/stable/2024-06-19/examples/
     * Operations_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MinimumSet_Gen.
     * 
     * @param manager Entry point to QumuloManager.
     */
    public static void operationsListMinimumSetGen(com.azure.resourcemanager.qumulo.QumuloManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/liftrqumulo/resource-manager/Qumulo.Storage/stable/2024-06-19/examples/
     * Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to QumuloManager.
     */
    public static void operationsList(com.azure.resourcemanager.qumulo.QumuloManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

