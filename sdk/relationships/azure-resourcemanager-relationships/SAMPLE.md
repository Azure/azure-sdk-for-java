# Code snippets and samples


## DependencyOfRelationships

- [CreateOrUpdate](#dependencyofrelationships_createorupdate)
- [Delete](#dependencyofrelationships_delete)
- [Get](#dependencyofrelationships_get)

## Operations

- [List](#operations_list)

## ServiceGroupMemberRelationships

- [CreateOrUpdate](#servicegroupmemberrelationships_createorupdate)
- [Delete](#servicegroupmemberrelationships_delete)
- [Get](#servicegroupmemberrelationships_get)
### DependencyOfRelationships_CreateOrUpdate

```java
import com.azure.resourcemanager.relationships.models.DependencyOfRelationshipProperties;

/**
 * Samples for DependencyOfRelationships CreateOrUpdate.
 */
public final class DependencyOfRelationshipsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2023-09-01-preview/DependencyOfRelationships_CreateOrUpdate.json
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
     * x-ms-original-file: 2023-09-01-preview/DependencyOfRelationships_Delete.json
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
     * x-ms-original-file: 2023-09-01-preview/DependencyOfRelationships_Get.json
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

### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: 2023-09-01-preview/Operations_List_MaximumSet_Gen.json
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
import com.azure.resourcemanager.relationships.models.ServiceGroupMemberRelationshipProperties;

/**
 * Samples for ServiceGroupMemberRelationships CreateOrUpdate.
 */
public final class ServiceGroupMemberRelationshipsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: 2023-09-01-preview/ServiceGroupMemberRelationships_CreateOrUpdate.json
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
            .withProperties(new ServiceGroupMemberRelationshipProperties()
                .withTargetId("/providers/Microsoft.Management/serviceGroups/sg1")
                .withTargetTenant("72f988bf-86f1-41af-91ab-2d7cd011db47"))
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
     * x-ms-original-file: 2023-09-01-preview/ServiceGroupMemberRelationships_Delete.json
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
     * x-ms-original-file: 2023-09-01-preview/ServiceGroupMemberRelationships_Get.json
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

