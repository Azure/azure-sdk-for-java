# Code snippets and samples


## BlockchainMemberOperationResults

- [Get](#blockchainmemberoperationresults_get)

## BlockchainMembers

- [Create](#blockchainmembers_create)
- [Delete](#blockchainmembers_delete)
- [GetByResourceGroup](#blockchainmembers_getbyresourcegroup)
- [List](#blockchainmembers_list)
- [ListApiKeys](#blockchainmembers_listapikeys)
- [ListByResourceGroup](#blockchainmembers_listbyresourcegroup)
- [ListConsortiumMembers](#blockchainmembers_listconsortiummembers)
- [ListRegenerateApiKeys](#blockchainmembers_listregenerateapikeys)
- [Update](#blockchainmembers_update)

## Locations

- [CheckNameAvailability](#locations_checknameavailability)
- [ListConsortiums](#locations_listconsortiums)

## Operations

- [List](#operations_list)

## Skus

- [List](#skus_list)

## TransactionNodes

- [Create](#transactionnodes_create)
- [Delete](#transactionnodes_delete)
- [Get](#transactionnodes_get)
- [List](#transactionnodes_list)
- [ListApiKeys](#transactionnodes_listapikeys)
- [ListRegenerateApiKeys](#transactionnodes_listregenerateapikeys)
- [Update](#transactionnodes_update)
### BlockchainMemberOperationResults_Get

```java
import com.azure.core.util.Context;

/** Samples for BlockchainMemberOperationResults Get. */
public final class BlockchainMemberOperationResultsGetSamples {
    /*
     * x-ms-original-file: specification/blockchain/resource-manager/Microsoft.Blockchain/preview/2018-06-01-preview/examples/BlockchainMemberOperationResults_Get.json
     */
    /**
     * Sample code: BlockchainMemberOperationResults_Get.
     *
     * @param manager Entry point to BlockchainManager.
     */
    public static void blockchainMemberOperationResultsGet(
        com.azure.resourcemanager.blockchain.BlockchainManager manager) {
        manager
            .blockchainMemberOperationResults()
            .getWithResponse("southeastasia", "12f4b309-01e3-4fcf-bc0b-1cc034ca03f8", Context.NONE);
    }
}
```

### BlockchainMembers_Create

```java
import com.azure.resourcemanager.blockchain.models.BlockchainMemberNodesSku;
import com.azure.resourcemanager.blockchain.models.BlockchainProtocol;

/** Samples for BlockchainMembers Create. */
public final class BlockchainMembersCreateSamples {
    /*
     * x-ms-original-file: specification/blockchain/resource-manager/Microsoft.Blockchain/preview/2018-06-01-preview/examples/BlockchainMembers_Create.json
     */
    /**
     * Sample code: BlockchainMembers_Create.
     *
     * @param manager Entry point to BlockchainManager.
     */
    public static void blockchainMembersCreate(com.azure.resourcemanager.blockchain.BlockchainManager manager) {
        manager
            .blockchainMembers()
            .define("contosemember1")
            .withRegion("southeastasia")
            .withExistingResourceGroup("mygroup")
            .withProtocol(BlockchainProtocol.QUORUM)
            .withValidatorNodesSku(new BlockchainMemberNodesSku().withCapacity(2))
            .withPassword("<password>")
            .withConsortium("ContoseConsortium")
            .withConsortiumManagementAccountPassword("<consortiumManagementAccountPassword>")
            .create();
    }
}
```

### BlockchainMembers_Delete

```java
import com.azure.core.util.Context;

/** Samples for BlockchainMembers Delete. */
public final class BlockchainMembersDeleteSamples {
    /*
     * x-ms-original-file: specification/blockchain/resource-manager/Microsoft.Blockchain/preview/2018-06-01-preview/examples/BlockchainMembers_Delete.json
     */
    /**
     * Sample code: BlockchainMembers_Delete.
     *
     * @param manager Entry point to BlockchainManager.
     */
    public static void blockchainMembersDelete(com.azure.resourcemanager.blockchain.BlockchainManager manager) {
        manager.blockchainMembers().delete("mygroup", "contosemember1", Context.NONE);
    }
}
```

### BlockchainMembers_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for BlockchainMembers GetByResourceGroup. */
public final class BlockchainMembersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/blockchain/resource-manager/Microsoft.Blockchain/preview/2018-06-01-preview/examples/BlockchainMembers_Get.json
     */
    /**
     * Sample code: BlockchainMembers_Get.
     *
     * @param manager Entry point to BlockchainManager.
     */
    public static void blockchainMembersGet(com.azure.resourcemanager.blockchain.BlockchainManager manager) {
        manager.blockchainMembers().getByResourceGroupWithResponse("mygroup", "contosemember1", Context.NONE);
    }
}
```

### BlockchainMembers_List

```java
import com.azure.core.util.Context;

/** Samples for BlockchainMembers List. */
public final class BlockchainMembersListSamples {
    /*
     * x-ms-original-file: specification/blockchain/resource-manager/Microsoft.Blockchain/preview/2018-06-01-preview/examples/BlockchainMembers_ListAll.json
     */
    /**
     * Sample code: BlockchainMembers_ListAll.
     *
     * @param manager Entry point to BlockchainManager.
     */
    public static void blockchainMembersListAll(com.azure.resourcemanager.blockchain.BlockchainManager manager) {
        manager.blockchainMembers().list(Context.NONE);
    }
}
```

### BlockchainMembers_ListApiKeys

```java
import com.azure.core.util.Context;

/** Samples for BlockchainMembers ListApiKeys. */
public final class BlockchainMembersListApiKeysSamples {
    /*
     * x-ms-original-file: specification/blockchain/resource-manager/Microsoft.Blockchain/preview/2018-06-01-preview/examples/BlockchainMembers_ListApiKeys.json
     */
    /**
     * Sample code: BlockchainMembers_ListApiKeys.
     *
     * @param manager Entry point to BlockchainManager.
     */
    public static void blockchainMembersListApiKeys(com.azure.resourcemanager.blockchain.BlockchainManager manager) {
        manager.blockchainMembers().listApiKeysWithResponse("contosemember1", "mygroup", Context.NONE);
    }
}
```

### BlockchainMembers_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for BlockchainMembers ListByResourceGroup. */
public final class BlockchainMembersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/blockchain/resource-manager/Microsoft.Blockchain/preview/2018-06-01-preview/examples/BlockchainMembers_List.json
     */
    /**
     * Sample code: BlockchainMembers_List.
     *
     * @param manager Entry point to BlockchainManager.
     */
    public static void blockchainMembersList(com.azure.resourcemanager.blockchain.BlockchainManager manager) {
        manager.blockchainMembers().listByResourceGroup("mygroup", Context.NONE);
    }
}
```

### BlockchainMembers_ListConsortiumMembers

```java
import com.azure.core.util.Context;

/** Samples for BlockchainMembers ListConsortiumMembers. */
public final class BlockchainMembersListConsortiumMembersSamples {
    /*
     * x-ms-original-file: specification/blockchain/resource-manager/Microsoft.Blockchain/preview/2018-06-01-preview/examples/BlockchainMembers_ListConsortiumMembers.json
     */
    /**
     * Sample code: BlockchainMembers_ListConsortiumMembers.
     *
     * @param manager Entry point to BlockchainManager.
     */
    public static void blockchainMembersListConsortiumMembers(
        com.azure.resourcemanager.blockchain.BlockchainManager manager) {
        manager.blockchainMembers().listConsortiumMembers("contosemember1", "mygroup", Context.NONE);
    }
}
```

### BlockchainMembers_ListRegenerateApiKeys

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.blockchain.models.ApiKey;

/** Samples for BlockchainMembers ListRegenerateApiKeys. */
public final class BlockchainMembersListRegenerateApiKeysSamples {
    /*
     * x-ms-original-file: specification/blockchain/resource-manager/Microsoft.Blockchain/preview/2018-06-01-preview/examples/BlockchainMembers_ListRegenerateApiKeys.json
     */
    /**
     * Sample code: BlockchainMembers_ListRegenerateApiKeys.
     *
     * @param manager Entry point to BlockchainManager.
     */
    public static void blockchainMembersListRegenerateApiKeys(
        com.azure.resourcemanager.blockchain.BlockchainManager manager) {
        manager
            .blockchainMembers()
            .listRegenerateApiKeysWithResponse(
                "contosemember1", "mygroup", new ApiKey().withKeyName("key1"), Context.NONE);
    }
}
```

### BlockchainMembers_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.blockchain.models.BlockchainMember;

/** Samples for BlockchainMembers Update. */
public final class BlockchainMembersUpdateSamples {
    /*
     * x-ms-original-file: specification/blockchain/resource-manager/Microsoft.Blockchain/preview/2018-06-01-preview/examples/BlockchainMembers_Update.json
     */
    /**
     * Sample code: BlockchainMembers_Update.
     *
     * @param manager Entry point to BlockchainManager.
     */
    public static void blockchainMembersUpdate(com.azure.resourcemanager.blockchain.BlockchainManager manager) {
        BlockchainMember resource =
            manager
                .blockchainMembers()
                .getByResourceGroupWithResponse("mygroup", "ContoseMember1", Context.NONE)
                .getValue();
        resource
            .update()
            .withConsortiumManagementAccountPassword("<consortiumManagementAccountPassword>")
            .withPassword("<password>")
            .apply();
    }
}
```

### Locations_CheckNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.blockchain.models.NameAvailabilityRequest;

/** Samples for Locations CheckNameAvailability. */
public final class LocationsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/blockchain/resource-manager/Microsoft.Blockchain/preview/2018-06-01-preview/examples/Locations_CheckNameAvailability.json
     */
    /**
     * Sample code: Locations_CheckNameAvailability.
     *
     * @param manager Entry point to BlockchainManager.
     */
    public static void locationsCheckNameAvailability(com.azure.resourcemanager.blockchain.BlockchainManager manager) {
        manager
            .locations()
            .checkNameAvailabilityWithResponse(
                "southeastasia",
                new NameAvailabilityRequest()
                    .withName("contosemember1")
                    .withType("Microsoft.Blockchain/blockchainMembers"),
                Context.NONE);
    }
}
```

### Locations_ListConsortiums

```java
import com.azure.core.util.Context;

/** Samples for Locations ListConsortiums. */
public final class LocationsListConsortiumsSamples {
    /*
     * x-ms-original-file: specification/blockchain/resource-manager/Microsoft.Blockchain/preview/2018-06-01-preview/examples/Locations_ListConsortiums.json
     */
    /**
     * Sample code: Locations_ListConsortiums.
     *
     * @param manager Entry point to BlockchainManager.
     */
    public static void locationsListConsortiums(com.azure.resourcemanager.blockchain.BlockchainManager manager) {
        manager.locations().listConsortiumsWithResponse("southeastasia", Context.NONE);
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/blockchain/resource-manager/Microsoft.Blockchain/preview/2018-06-01-preview/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to BlockchainManager.
     */
    public static void operationsList(com.azure.resourcemanager.blockchain.BlockchainManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### Skus_List

```java
import com.azure.core.util.Context;

/** Samples for Skus List. */
public final class SkusListSamples {
    /*
     * x-ms-original-file: specification/blockchain/resource-manager/Microsoft.Blockchain/preview/2018-06-01-preview/examples/Skus_List.json
     */
    /**
     * Sample code: Skus_List.
     *
     * @param manager Entry point to BlockchainManager.
     */
    public static void skusList(com.azure.resourcemanager.blockchain.BlockchainManager manager) {
        manager.skus().listWithResponse(Context.NONE);
    }
}
```

### TransactionNodes_Create

```java
/** Samples for TransactionNodes Create. */
public final class TransactionNodesCreateSamples {
    /*
     * x-ms-original-file: specification/blockchain/resource-manager/Microsoft.Blockchain/preview/2018-06-01-preview/examples/TransactionNodes_Create.json
     */
    /**
     * Sample code: TransactionNodes_Create.
     *
     * @param manager Entry point to BlockchainManager.
     */
    public static void transactionNodesCreate(com.azure.resourcemanager.blockchain.BlockchainManager manager) {
        manager
            .transactionNodes()
            .define("txnode2")
            .withExistingBlockchainMember("contosemember1", "mygroup")
            .withRegion("southeastasia")
            .withPassword("<password>")
            .create();
    }
}
```

### TransactionNodes_Delete

```java
import com.azure.core.util.Context;

/** Samples for TransactionNodes Delete. */
public final class TransactionNodesDeleteSamples {
    /*
     * x-ms-original-file: specification/blockchain/resource-manager/Microsoft.Blockchain/preview/2018-06-01-preview/examples/TransactionNodes_Delete.json
     */
    /**
     * Sample code: TransactionNodes_Delete.
     *
     * @param manager Entry point to BlockchainManager.
     */
    public static void transactionNodesDelete(com.azure.resourcemanager.blockchain.BlockchainManager manager) {
        manager.transactionNodes().delete("contosemember1", "txNode2", "mygroup", Context.NONE);
    }
}
```

### TransactionNodes_Get

```java
import com.azure.core.util.Context;

/** Samples for TransactionNodes Get. */
public final class TransactionNodesGetSamples {
    /*
     * x-ms-original-file: specification/blockchain/resource-manager/Microsoft.Blockchain/preview/2018-06-01-preview/examples/TransactionNodes_Get.json
     */
    /**
     * Sample code: TransactionNodes_Get.
     *
     * @param manager Entry point to BlockchainManager.
     */
    public static void transactionNodesGet(com.azure.resourcemanager.blockchain.BlockchainManager manager) {
        manager.transactionNodes().getWithResponse("contosemember1", "txnode2", "mygroup", Context.NONE);
    }
}
```

### TransactionNodes_List

```java
import com.azure.core.util.Context;

/** Samples for TransactionNodes List. */
public final class TransactionNodesListSamples {
    /*
     * x-ms-original-file: specification/blockchain/resource-manager/Microsoft.Blockchain/preview/2018-06-01-preview/examples/TransactionNodes_List.json
     */
    /**
     * Sample code: TransactionNodes_List.
     *
     * @param manager Entry point to BlockchainManager.
     */
    public static void transactionNodesList(com.azure.resourcemanager.blockchain.BlockchainManager manager) {
        manager.transactionNodes().list("contosemember1", "mygroup", Context.NONE);
    }
}
```

### TransactionNodes_ListApiKeys

```java
import com.azure.core.util.Context;

/** Samples for TransactionNodes ListApiKeys. */
public final class TransactionNodesListApiKeysSamples {
    /*
     * x-ms-original-file: specification/blockchain/resource-manager/Microsoft.Blockchain/preview/2018-06-01-preview/examples/TransactionNodes_ListApiKeys.json
     */
    /**
     * Sample code: TransactionNodes_ListApiKeys.
     *
     * @param manager Entry point to BlockchainManager.
     */
    public static void transactionNodesListApiKeys(com.azure.resourcemanager.blockchain.BlockchainManager manager) {
        manager.transactionNodes().listApiKeysWithResponse("contosemember1", "txnode2", "mygroup", Context.NONE);
    }
}
```

### TransactionNodes_ListRegenerateApiKeys

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.blockchain.models.ApiKey;

/** Samples for TransactionNodes ListRegenerateApiKeys. */
public final class TransactionNodesListRegenerateApiKeysSamples {
    /*
     * x-ms-original-file: specification/blockchain/resource-manager/Microsoft.Blockchain/preview/2018-06-01-preview/examples/TransactionNodes_ListRegenerateApiKeys.json
     */
    /**
     * Sample code: TransactionNodes_ListRegenerateApiKeys.
     *
     * @param manager Entry point to BlockchainManager.
     */
    public static void transactionNodesListRegenerateApiKeys(
        com.azure.resourcemanager.blockchain.BlockchainManager manager) {
        manager
            .transactionNodes()
            .listRegenerateApiKeysWithResponse(
                "contosemember1", "txnode2", "mygroup", new ApiKey().withKeyName("key1"), Context.NONE);
    }
}
```

### TransactionNodes_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.blockchain.models.TransactionNode;

/** Samples for TransactionNodes Update. */
public final class TransactionNodesUpdateSamples {
    /*
     * x-ms-original-file: specification/blockchain/resource-manager/Microsoft.Blockchain/preview/2018-06-01-preview/examples/TransactionNodes_Update.json
     */
    /**
     * Sample code: TransactionNodes_Update.
     *
     * @param manager Entry point to BlockchainManager.
     */
    public static void transactionNodesUpdate(com.azure.resourcemanager.blockchain.BlockchainManager manager) {
        TransactionNode resource =
            manager.transactionNodes().getWithResponse("contosemember1", "txnode2", "mygroup", Context.NONE).getValue();
        resource.update().withPassword("<password>").apply();
    }
}
```

