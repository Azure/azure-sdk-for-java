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
import com.azure.resourcemanager.qumulo.models.StorageSku;
import com.azure.resourcemanager.qumulo.models.UserAssignedIdentity;
import com.azure.resourcemanager.qumulo.models.UserDetails;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for FileSystems CreateOrUpdate. */
public final class FileSystemsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/liftrqumulo/resource-manager/Qumulo.Storage/preview/2022-10-12-preview/examples/FileSystems_CreateOrUpdate_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_CreateOrUpdate_MaximumSet_Gen.
     *
     * @param manager Entry point to QumuloManager.
     */
    public static void fileSystemsCreateOrUpdateMaximumSetGen(com.azure.resourcemanager.qumulo.QumuloManager manager) {
        manager
            .fileSystems()
            .define("nauwwbfoqehgbhdsmkewoboyxeqg")
            .withRegion("przdlsmlzsszphnixq")
            .withExistingResourceGroup("rgQumulo")
            .withMarketplaceDetails(
                new MarketplaceDetails()
                    .withMarketplaceSubscriptionId("ujrcqvxfnhxxheoth")
                    .withPlanId("x")
                    .withOfferId("eiyhbmpwgezcmzrrfoiskuxlcvwojf")
                    .withPublisherId("wfmokfdjbwpjhz"))
            .withStorageSku(StorageSku.STANDARD)
            .withUserDetails(new UserDetails().withEmail("viptslwulnpaupfljvnjeq"))
            .withDelegatedSubnetId("neqctctqdmjezfgt")
            .withAdminPassword("ekceujoecaashtjlsgcymnrdozk")
            .withInitialCapacity(9)
            .withTags(mapOf("key6565", "cgdhmupta"))
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.NONE)
                    .withUserAssignedIdentities(mapOf("key4522", new UserAssignedIdentity())))
            .withClusterLoginUrl("jjqhgevy")
            .withPrivateIPs(Arrays.asList("kslguxrwbwkrj"))
            .withAvailabilityZone("maseyqhlnhoiwbabcqabtedbjpip")
            .create();
    }

    /*
     * x-ms-original-file: specification/liftrqumulo/resource-manager/Qumulo.Storage/preview/2022-10-12-preview/examples/FileSystems_CreateOrUpdate_MinimumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_CreateOrUpdate_MinimumSet_Gen.
     *
     * @param manager Entry point to QumuloManager.
     */
    public static void fileSystemsCreateOrUpdateMinimumSetGen(com.azure.resourcemanager.qumulo.QumuloManager manager) {
        manager
            .fileSystems()
            .define("aaaaaaaa")
            .withRegion("aaaaaaaaaaaaaaaaaaaaaaaaa")
            .withExistingResourceGroup("rgopenapi")
            .withMarketplaceDetails(
                new MarketplaceDetails()
                    .withMarketplaceSubscriptionId("aaaaaaaaaaaaa")
                    .withPlanId("aaaaaa")
                    .withOfferId("aaaaaaaaaaaaaaaaaaaaaaaaa")
                    .withPublisherId("aa"))
            .withStorageSku(StorageSku.STANDARD)
            .withUserDetails(new UserDetails().withEmail("aaaaaaaaaaaaaaaaaaaaaaa"))
            .withDelegatedSubnetId("aaaaaaaaaa")
            .withAdminPassword("ekceujoecaashtjlsgcymnrdozk")
            .withInitialCapacity(9)
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

### FileSystems_Delete

```java
/** Samples for FileSystems Delete. */
public final class FileSystemsDeleteSamples {
    /*
     * x-ms-original-file: specification/liftrqumulo/resource-manager/Qumulo.Storage/preview/2022-10-12-preview/examples/FileSystems_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_Delete_MaximumSet_Gen.
     *
     * @param manager Entry point to QumuloManager.
     */
    public static void fileSystemsDeleteMaximumSetGen(com.azure.resourcemanager.qumulo.QumuloManager manager) {
        manager.fileSystems().delete("rgQumulo", "nauwwbfoqehgbhdsmkewoboyxeqg", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/liftrqumulo/resource-manager/Qumulo.Storage/preview/2022-10-12-preview/examples/FileSystems_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_Delete_MinimumSet_Gen.
     *
     * @param manager Entry point to QumuloManager.
     */
    public static void fileSystemsDeleteMinimumSetGen(com.azure.resourcemanager.qumulo.QumuloManager manager) {
        manager.fileSystems().delete("rgQumulo", "nauwwbfoqehgbhdsmkewoboyxeqg", com.azure.core.util.Context.NONE);
    }
}
```

### FileSystems_GetByResourceGroup

```java
/** Samples for FileSystems GetByResourceGroup. */
public final class FileSystemsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/liftrqumulo/resource-manager/Qumulo.Storage/preview/2022-10-12-preview/examples/FileSystems_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_Get_MaximumSet_Gen.
     *
     * @param manager Entry point to QumuloManager.
     */
    public static void fileSystemsGetMaximumSetGen(com.azure.resourcemanager.qumulo.QumuloManager manager) {
        manager
            .fileSystems()
            .getByResourceGroupWithResponse(
                "rgQumulo", "nauwwbfoqehgbhdsmkewoboyxeqg", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/liftrqumulo/resource-manager/Qumulo.Storage/preview/2022-10-12-preview/examples/FileSystems_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_Get_MinimumSet_Gen.
     *
     * @param manager Entry point to QumuloManager.
     */
    public static void fileSystemsGetMinimumSetGen(com.azure.resourcemanager.qumulo.QumuloManager manager) {
        manager
            .fileSystems()
            .getByResourceGroupWithResponse("rgQumulo", "aaaaaaaaaaaaaaaaa", com.azure.core.util.Context.NONE);
    }
}
```

### FileSystems_List

```java
/** Samples for FileSystems List. */
public final class FileSystemsListSamples {
    /*
     * x-ms-original-file: specification/liftrqumulo/resource-manager/Qumulo.Storage/preview/2022-10-12-preview/examples/FileSystems_ListBySubscription_MinimumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_ListBySubscription_MinimumSet_Gen.
     *
     * @param manager Entry point to QumuloManager.
     */
    public static void fileSystemsListBySubscriptionMinimumSetGen(
        com.azure.resourcemanager.qumulo.QumuloManager manager) {
        manager.fileSystems().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/liftrqumulo/resource-manager/Qumulo.Storage/preview/2022-10-12-preview/examples/FileSystems_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_ListBySubscription_MaximumSet_Gen.
     *
     * @param manager Entry point to QumuloManager.
     */
    public static void fileSystemsListBySubscriptionMaximumSetGen(
        com.azure.resourcemanager.qumulo.QumuloManager manager) {
        manager.fileSystems().list(com.azure.core.util.Context.NONE);
    }
}
```

### FileSystems_ListByResourceGroup

```java
/** Samples for FileSystems ListByResourceGroup. */
public final class FileSystemsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/liftrqumulo/resource-manager/Qumulo.Storage/preview/2022-10-12-preview/examples/FileSystems_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_ListByResourceGroup_MaximumSet_Gen.
     *
     * @param manager Entry point to QumuloManager.
     */
    public static void fileSystemsListByResourceGroupMaximumSetGen(
        com.azure.resourcemanager.qumulo.QumuloManager manager) {
        manager.fileSystems().listByResourceGroup("rgQumulo", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/liftrqumulo/resource-manager/Qumulo.Storage/preview/2022-10-12-preview/examples/FileSystems_ListByResourceGroup_MinimumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_ListByResourceGroup_MinimumSet_Gen.
     *
     * @param manager Entry point to QumuloManager.
     */
    public static void fileSystemsListByResourceGroupMinimumSetGen(
        com.azure.resourcemanager.qumulo.QumuloManager manager) {
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for FileSystems Update. */
public final class FileSystemsUpdateSamples {
    /*
     * x-ms-original-file: specification/liftrqumulo/resource-manager/Qumulo.Storage/preview/2022-10-12-preview/examples/FileSystems_Update_MinimumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_Update_MinimumSet_Gen.
     *
     * @param manager Entry point to QumuloManager.
     */
    public static void fileSystemsUpdateMinimumSetGen(com.azure.resourcemanager.qumulo.QumuloManager manager) {
        FileSystemResource resource =
            manager
                .fileSystems()
                .getByResourceGroupWithResponse("rgQumulo", "aaaaaaaaaaaaaaaaa", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().apply();
    }

    /*
     * x-ms-original-file: specification/liftrqumulo/resource-manager/Qumulo.Storage/preview/2022-10-12-preview/examples/FileSystems_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: FileSystems_Update_MaximumSet_Gen.
     *
     * @param manager Entry point to QumuloManager.
     */
    public static void fileSystemsUpdateMaximumSetGen(com.azure.resourcemanager.qumulo.QumuloManager manager) {
        FileSystemResource resource =
            manager
                .fileSystems()
                .getByResourceGroupWithResponse(
                    "rgQumulo", "nauwwbfoqehgbhdsmkewoboyxeqg", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("key7534", "jsgqvqbagquvxowbrkanyhzvo"))
            .withIdentity(
                new ManagedServiceIdentity()
                    .withType(ManagedServiceIdentityType.NONE)
                    .withUserAssignedIdentities(mapOf("key4522", new UserAssignedIdentity())))
            .withProperties(
                new FileSystemResourceUpdateProperties()
                    .withMarketplaceDetails(
                        new MarketplaceDetails()
                            .withMarketplaceSubscriptionId("ujrcqvxfnhxxheoth")
                            .withPlanId("x")
                            .withOfferId("eiyhbmpwgezcmzrrfoiskuxlcvwojf")
                            .withPublisherId("wfmokfdjbwpjhz"))
                    .withUserDetails(new UserDetails().withEmail("viptslwulnpaupfljvnjeq"))
                    .withDelegatedSubnetId("vjfirtaljehawmflyfianw")
                    .withClusterLoginUrl("adabmuthwrbjshzfbo")
                    .withPrivateIPs(Arrays.asList("eugjqbaoucgjsopzfrq")))
            .apply();
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

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/liftrqumulo/resource-manager/Qumulo.Storage/preview/2022-10-12-preview/examples/Operations_List_MinimumSet_Gen.json
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
     * x-ms-original-file: specification/liftrqumulo/resource-manager/Qumulo.Storage/preview/2022-10-12-preview/examples/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MaximumSet_Gen.
     *
     * @param manager Entry point to QumuloManager.
     */
    public static void operationsListMaximumSetGen(com.azure.resourcemanager.qumulo.QumuloManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

