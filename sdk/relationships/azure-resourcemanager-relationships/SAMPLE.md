# Code snippets and samples


## ContainsRelationships

- [List](#containsrelationships_list)
- [ListByResourceGroup](#containsrelationships_listbyresourcegroup)

## DependencyOfRelationships

- [CreateOrUpdate](#dependencyofrelationships_createorupdate)
- [Delete](#dependencyofrelationships_delete)
- [Get](#dependencyofrelationships_get)
- [ListByParent](#dependencyofrelationships_listbyparent)

## DependencyOfRelationshipsByServiceGroup

- [CreateOrUpdate](#dependencyofrelationshipsbyservicegroup_createorupdate)
- [Delete](#dependencyofrelationshipsbyservicegroup_delete)
- [Get](#dependencyofrelationshipsbyservicegroup_get)
- [List](#dependencyofrelationshipsbyservicegroup_list)

## Operations

- [List](#operations_list)

## ServiceGroupMemberRelationships

- [CreateOrUpdate](#servicegroupmemberrelationships_createorupdate)
- [Delete](#servicegroupmemberrelationships_delete)
- [Get](#servicegroupmemberrelationships_get)
- [ListByParent](#servicegroupmemberrelationships_listbyparent)
### ContainsRelationships_List

```java
/**
 * Samples for ContainsRelationships List.
 */
public final class ContainsRelationshipsListSamples {
    /*
     * x-ms-original-file: 2026-08-01/ContainsRelationships_ListBySubscription.json
     */
    /**
     * Sample code: ContainsRelationships_ListBySubscription.
     * 
     * @param manager Entry point to RelationshipsManager.
     */
    public static void
        containsRelationshipsListBySubscription(com.azure.resourcemanager.relationships.RelationshipsManager manager) {
        manager.containsRelationships().list(null, com.azure.core.util.Context.NONE);
    }
}
```

### ContainsRelationships_ListByResourceGroup

```java
/**
 * Samples for ContainsRelationships ListByResourceGroup.
 */
public final class ContainsRelationshipsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2026-08-01/ContainsRelationships_ListByResourceGroup.json
     */
    /**
     * Sample code: ContainsRelationships_ListByResourceGroup.
     * 
     * @param manager Entry point to RelationshipsManager.
     */
    public static void
        containsRelationshipsListByResourceGroup(com.azure.resourcemanager.relationships.RelationshipsManager manager) {
        manager.containsRelationships().listByResourceGroup("testrg", null, com.azure.core.util.Context.NONE);
    }
}
```

### DependencyOfRelationships_CreateOrUpdate

```java
import com.azure.resourcemanager.relationships.models.DependencyOfRelationshipProperties;

/**
 * Samples for DependencyOfRelationships CreateOrUpdate.
 */
public final class DependencyOfRelationshipsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-08-01/DependencyOfRelationships_CreateOrUpdate.json
     */
    /**
     * Sample code: DependencyOfRelationships_CreateOrUpdate.
     * 
     * @param manager Entry point to RelationshipsManager.
     */
    public static void
        dependencyOfRelationshipsCreateOrUpdate(com.azure.resourcemanager.relationships.RelationshipsManager manager) {
        manager.dependencyOfRelationships()
            .define("relationshipOne")
            .withExistingResourceUri(
                "subscriptions/a925f2f7-5c63-4b7b-8799-25a5f97bc3b2/resourceGroups/testrg/providers/Microsoft.DocumentDb/databaseAccounts/test-db-account")
            .withProperties(new DependencyOfRelationshipProperties().withTargetId(
                "/subscriptions/a925f2f7-5c63-4b7b-8799-25a5f97bc3b2/resourceGroups/testrg123/providers/Microsoft.Web/staticSites/test-site")
                .withTargetTenant("72f988bf-86f1-41af-91ab-2d7cd011db47"))
            .create();
    }
}
```

### DependencyOfRelationships_Delete

```java
/**
 * Samples for DependencyOfRelationships Delete.
 */
public final class DependencyOfRelationshipsDeleteSamples {
    /*
     * x-ms-original-file: 2026-08-01/DependencyOfRelationships_Delete.json
     */
    /**
     * Sample code: DependencyOfRelationships_Delete.
     * 
     * @param manager Entry point to RelationshipsManager.
     */
    public static void
        dependencyOfRelationshipsDelete(com.azure.resourcemanager.relationships.RelationshipsManager manager) {
        manager.dependencyOfRelationships()
            .delete(
                "subscriptions/a925f2f7-5c63-4b7b-8799-25a5f97bc3b2/resourceGroups/testrg/providers/Microsoft.DocumentDb/databaseAccounts/test-db-account",
                "relationshipOne", com.azure.core.util.Context.NONE);
    }
}
```

### DependencyOfRelationships_Get

```java
/**
 * Samples for DependencyOfRelationships Get.
 */
public final class DependencyOfRelationshipsGetSamples {
    /*
     * x-ms-original-file: 2026-08-01/DependencyOfRelationships_Get.json
     */
    /**
     * Sample code: DependencyOfRelationships_Get.
     * 
     * @param manager Entry point to RelationshipsManager.
     */
    public static void
        dependencyOfRelationshipsGet(com.azure.resourcemanager.relationships.RelationshipsManager manager) {
        manager.dependencyOfRelationships()
            .getWithResponse(
                "subscriptions/a925f2f7-5c63-4b7b-8799-25a5f97bc3b2/resourceGroups/testrg/providers/Microsoft.DocumentDb/databaseAccounts/test-db-account",
                "relationshipOne", com.azure.core.util.Context.NONE);
    }
}
```

### DependencyOfRelationships_ListByParent

```java
/**
 * Samples for DependencyOfRelationships ListByParent.
 */
public final class DependencyOfRelationshipsListByParentSamples {
    /*
     * x-ms-original-file: 2026-08-01/DependencyOfRelationships_ListByParent.json
     */
    /**
     * Sample code: DependencyOfRelationships_ListByParent.
     * 
     * @param manager Entry point to RelationshipsManager.
     */
    public static void
        dependencyOfRelationshipsListByParent(com.azure.resourcemanager.relationships.RelationshipsManager manager) {
        manager.dependencyOfRelationships()
            .listByParent(
                "subscriptions/a925f2f7-5c63-4b7b-8799-25a5f97bc3b2/resourceGroups/testrg/providers/Microsoft.DocumentDb/databaseAccounts/test-db-account",
                com.azure.core.util.Context.NONE);
    }
}
```

### DependencyOfRelationshipsByServiceGroup_CreateOrUpdate

```java
import com.azure.resourcemanager.relationships.fluent.models.DependencyOfRelationshipInner;
import com.azure.resourcemanager.relationships.models.DependencyOfRelationshipProperties;

/**
 * Samples for DependencyOfRelationshipsByServiceGroup CreateOrUpdate.
 */
public final class DependencyOfRelationshipsByServiceGroupCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-08-01/DependencyOfRelationshipsByServiceGroup_CreateOrUpdate.json
     */
    /**
     * Sample code: DependencyOfRelationshipsByServiceGroup_CreateOrUpdate.
     * 
     * @param manager Entry point to RelationshipsManager.
     */
    public static void dependencyOfRelationshipsByServiceGroupCreateOrUpdate(
        com.azure.resourcemanager.relationships.RelationshipsManager manager) {
        manager.dependencyOfRelationshipsByServiceGroups()
            .createOrUpdate("myServiceGroup", "relationshipOne", new DependencyOfRelationshipInner()
                .withProperties(new DependencyOfRelationshipProperties().withTargetId(
                    "/subscriptions/a925f2f7-5c63-4b7b-8799-25a5f97bc3b2/resourceGroups/testrg123/providers/Microsoft.Web/staticSites/test-site")
                    .withTargetTenant("72f988bf-86f1-41af-91ab-2d7cd011db47")),
                com.azure.core.util.Context.NONE);
    }
}
```

### DependencyOfRelationshipsByServiceGroup_Delete

```java
/**
 * Samples for DependencyOfRelationshipsByServiceGroup Delete.
 */
public final class DependencyOfRelationshipsByServiceGroupDeleteSamples {
    /*
     * x-ms-original-file: 2026-08-01/DependencyOfRelationshipsByServiceGroup_Delete.json
     */
    /**
     * Sample code: DependencyOfRelationshipsByServiceGroup_Delete.
     * 
     * @param manager Entry point to RelationshipsManager.
     */
    public static void dependencyOfRelationshipsByServiceGroupDelete(
        com.azure.resourcemanager.relationships.RelationshipsManager manager) {
        manager.dependencyOfRelationshipsByServiceGroups()
            .delete("myServiceGroup", "relationshipOne", com.azure.core.util.Context.NONE);
    }
}
```

### DependencyOfRelationshipsByServiceGroup_Get

```java
/**
 * Samples for DependencyOfRelationshipsByServiceGroup Get.
 */
public final class DependencyOfRelationshipsByServiceGroupGetSamples {
    /*
     * x-ms-original-file: 2026-08-01/DependencyOfRelationshipsByServiceGroup_Get.json
     */
    /**
     * Sample code: DependencyOfRelationshipsByServiceGroup_Get.
     * 
     * @param manager Entry point to RelationshipsManager.
     */
    public static void dependencyOfRelationshipsByServiceGroupGet(
        com.azure.resourcemanager.relationships.RelationshipsManager manager) {
        manager.dependencyOfRelationshipsByServiceGroups()
            .getWithResponse("myServiceGroup", "relationshipOne", com.azure.core.util.Context.NONE);
    }
}
```

### DependencyOfRelationshipsByServiceGroup_List

```java
/**
 * Samples for DependencyOfRelationshipsByServiceGroup List.
 */
public final class DependencyOfRelationshipsByServiceGroupListSamples {
    /*
     * x-ms-original-file: 2026-08-01/DependencyOfRelationshipsByServiceGroup_List.json
     */
    /**
     * Sample code: DependencyOfRelationshipsByServiceGroup_List.
     * 
     * @param manager Entry point to RelationshipsManager.
     */
    public static void dependencyOfRelationshipsByServiceGroupList(
        com.azure.resourcemanager.relationships.RelationshipsManager manager) {
        manager.dependencyOfRelationshipsByServiceGroups().list("myServiceGroup", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2026-08-01/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to RelationshipsManager.
     */
    public static void operationsList(com.azure.resourcemanager.relationships.RelationshipsManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### ServiceGroupMemberRelationships_CreateOrUpdate

```java
import com.azure.resourcemanager.relationships.models.ServiceGroupMemberRelationshipPropertiesV2;

/**
 * Samples for ServiceGroupMemberRelationships CreateOrUpdate.
 */
public final class ServiceGroupMemberRelationshipsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2026-08-01/ServiceGroupMemberRelationships_CreateOrUpdate.json
     */
    /**
     * Sample code: ServiceGroupMemberRelationships_CreateOrUpdate.
     * 
     * @param manager Entry point to RelationshipsManager.
     */
    public static void serviceGroupMemberRelationshipsCreateOrUpdate(
        com.azure.resourcemanager.relationships.RelationshipsManager manager) {
        manager.serviceGroupMemberRelationships()
            .define("sg1")
            .withExistingResourceUri(
                "subscriptions/a925f2f7-5c63-4b7b-8799-25a5f97bc3b2/resourceGroups/testrg/providers/Microsoft.DocumentDb/databaseAccounts/test-db-account")
            .withProperties(new ServiceGroupMemberRelationshipPropertiesV2()
                .withSourceId("/providers/Microsoft.Management/serviceGroups/sg1")
                .withSourceTenant("72f988bf-86f1-41af-91ab-2d7cd011db47"))
            .create();
    }
}
```

### ServiceGroupMemberRelationships_Delete

```java
/**
 * Samples for ServiceGroupMemberRelationships Delete.
 */
public final class ServiceGroupMemberRelationshipsDeleteSamples {
    /*
     * x-ms-original-file: 2026-08-01/ServiceGroupMemberRelationships_Delete.json
     */
    /**
     * Sample code: ServiceGroupMemberRelationships_Delete.
     * 
     * @param manager Entry point to RelationshipsManager.
     */
    public static void
        serviceGroupMemberRelationshipsDelete(com.azure.resourcemanager.relationships.RelationshipsManager manager) {
        manager.serviceGroupMemberRelationships()
            .delete(
                "subscriptions/a925f2f7-5c63-4b7b-8799-25a5f97bc3b2/resourceGroups/testrg/providers/Microsoft.DocumentDb/databaseAccounts/test-db-account",
                "sg1", com.azure.core.util.Context.NONE);
    }
}
```

### ServiceGroupMemberRelationships_Get

```java
/**
 * Samples for ServiceGroupMemberRelationships Get.
 */
public final class ServiceGroupMemberRelationshipsGetSamples {
    /*
     * x-ms-original-file: 2026-08-01/ServiceGroupMemberRelationships_Get.json
     */
    /**
     * Sample code: ServiceGroupMemberRelationships_Get.
     * 
     * @param manager Entry point to RelationshipsManager.
     */
    public static void
        serviceGroupMemberRelationshipsGet(com.azure.resourcemanager.relationships.RelationshipsManager manager) {
        manager.serviceGroupMemberRelationships()
            .getWithResponse(
                "subscriptions/a925f2f7-5c63-4b7b-8799-25a5f97bc3b2/resourceGroups/testrg/providers/Microsoft.DocumentDb/databaseAccounts/test-db-account",
                "sg1", com.azure.core.util.Context.NONE);
    }
}
```

### ServiceGroupMemberRelationships_ListByParent

```java
/**
 * Samples for ServiceGroupMemberRelationships ListByParent.
 */
public final class ServiceGroupMemberRelationshipsListByParentSamples {
    /*
     * x-ms-original-file: 2026-08-01/ServiceGroupMemberRelationships_ListByParent.json
     */
    /**
     * Sample code: ServiceGroupMemberRelationships_ListByParent.
     * 
     * @param manager Entry point to RelationshipsManager.
     */
    public static void serviceGroupMemberRelationshipsListByParent(
        com.azure.resourcemanager.relationships.RelationshipsManager manager) {
        manager.serviceGroupMemberRelationships()
            .listByParent(
                "subscriptions/a925f2f7-5c63-4b7b-8799-25a5f97bc3b2/resourceGroups/testrg/providers/Microsoft.DocumentDb/databaseAccounts/test-db-account",
                com.azure.core.util.Context.NONE);
    }
}
```

