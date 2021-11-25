# Code snippets and samples


## Operations

- [List](#operations_list)

## SqlServerRegistrations

- [CreateOrUpdate](#sqlserverregistrations_createorupdate)
- [Delete](#sqlserverregistrations_delete)
- [GetByResourceGroup](#sqlserverregistrations_getbyresourcegroup)
- [List](#sqlserverregistrations_list)
- [ListByResourceGroup](#sqlserverregistrations_listbyresourcegroup)
- [Update](#sqlserverregistrations_update)

## SqlServers

- [CreateOrUpdate](#sqlservers_createorupdate)
- [Delete](#sqlservers_delete)
- [Get](#sqlservers_get)
- [ListByResourceGroup](#sqlservers_listbyresourcegroup)
### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/azuredata/resource-manager/Microsoft.AzureData/preview/2019-07-24-preview/examples/ListOperation.json
     */
    /**
     * Sample code: Lists all of the available SQL Server Registration API operations.
     *
     * @param manager Entry point to AzureDataManager.
     */
    public static void listsAllOfTheAvailableSQLServerRegistrationAPIOperations(
        com.azure.resourcemanager.azuredata.AzureDataManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### SqlServerRegistrations_CreateOrUpdate

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for SqlServerRegistrations CreateOrUpdate. */
public final class SqlServerRegistrationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azuredata/resource-manager/Microsoft.AzureData/preview/2019-07-24-preview/examples/CreateOrUpdateSqlServerRegistration.json
     */
    /**
     * Sample code: Creates or updates a SQL Server registration.
     *
     * @param manager Entry point to AzureDataManager.
     */
    public static void createsOrUpdatesASQLServerRegistration(
        com.azure.resourcemanager.azuredata.AzureDataManager manager) {
        manager
            .sqlServerRegistrations()
            .define("testsqlregistration")
            .withRegion("northeurope")
            .withExistingResourceGroup("testrg")
            .withTags(mapOf("mytag", "myval"))
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

### SqlServerRegistrations_Delete

```java
import com.azure.core.util.Context;

/** Samples for SqlServerRegistrations Delete. */
public final class SqlServerRegistrationsDeleteSamples {
    /*
     * x-ms-original-file: specification/azuredata/resource-manager/Microsoft.AzureData/preview/2019-07-24-preview/examples/DeleteSqlServerRegistration.json
     */
    /**
     * Sample code: Deletes a SQL Server registration.
     *
     * @param manager Entry point to AzureDataManager.
     */
    public static void deletesASQLServerRegistration(com.azure.resourcemanager.azuredata.AzureDataManager manager) {
        manager.sqlServerRegistrations().deleteWithResponse("testrg", "testsqlregistration", Context.NONE);
    }
}
```

### SqlServerRegistrations_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for SqlServerRegistrations GetByResourceGroup. */
public final class SqlServerRegistrationsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azuredata/resource-manager/Microsoft.AzureData/preview/2019-07-24-preview/examples/GetSqlServerRegistration.json
     */
    /**
     * Sample code: Gets a SQL Server registration.
     *
     * @param manager Entry point to AzureDataManager.
     */
    public static void getsASQLServerRegistration(com.azure.resourcemanager.azuredata.AzureDataManager manager) {
        manager.sqlServerRegistrations().getByResourceGroupWithResponse("testrg", "testsqlregistration", Context.NONE);
    }
}
```

### SqlServerRegistrations_List

```java
import com.azure.core.util.Context;

/** Samples for SqlServerRegistrations List. */
public final class SqlServerRegistrationsListSamples {
    /*
     * x-ms-original-file: specification/azuredata/resource-manager/Microsoft.AzureData/preview/2019-07-24-preview/examples/ListSubscriptionSqlServerRegistration.json
     */
    /**
     * Sample code: Gets all SQL Server registrations in a subscription.
     *
     * @param manager Entry point to AzureDataManager.
     */
    public static void getsAllSQLServerRegistrationsInASubscription(
        com.azure.resourcemanager.azuredata.AzureDataManager manager) {
        manager.sqlServerRegistrations().list(Context.NONE);
    }
}
```

### SqlServerRegistrations_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for SqlServerRegistrations ListByResourceGroup. */
public final class SqlServerRegistrationsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azuredata/resource-manager/Microsoft.AzureData/preview/2019-07-24-preview/examples/ListByResourceGroupSqlServerRegistration.json
     */
    /**
     * Sample code: Gets all SQL Server registrations in a resource group.
     *
     * @param manager Entry point to AzureDataManager.
     */
    public static void getsAllSQLServerRegistrationsInAResourceGroup(
        com.azure.resourcemanager.azuredata.AzureDataManager manager) {
        manager.sqlServerRegistrations().listByResourceGroup("testrg", Context.NONE);
    }
}
```

### SqlServerRegistrations_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.azuredata.models.SqlServerRegistration;
import java.util.HashMap;
import java.util.Map;

/** Samples for SqlServerRegistrations Update. */
public final class SqlServerRegistrationsUpdateSamples {
    /*
     * x-ms-original-file: specification/azuredata/resource-manager/Microsoft.AzureData/preview/2019-07-24-preview/examples/UpdateSqlServerRegistration.json
     */
    /**
     * Sample code: Updates a SQL Server Registration tags.
     *
     * @param manager Entry point to AzureDataManager.
     */
    public static void updatesASQLServerRegistrationTags(com.azure.resourcemanager.azuredata.AzureDataManager manager) {
        SqlServerRegistration resource =
            manager
                .sqlServerRegistrations()
                .getByResourceGroupWithResponse("testrg", "testsqlregistration", Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("mytag", "myval")).apply();
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

### SqlServers_CreateOrUpdate

```java
/** Samples for SqlServers CreateOrUpdate. */
public final class SqlServersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azuredata/resource-manager/Microsoft.AzureData/preview/2019-07-24-preview/examples/CreateOrUpdateSqlServerWithRegistrationGroup.json
     */
    /**
     * Sample code: Creates or updates a SQL Server in a Registration group.
     *
     * @param manager Entry point to AzureDataManager.
     */
    public static void createsOrUpdatesASQLServerInARegistrationGroup(
        com.azure.resourcemanager.azuredata.AzureDataManager manager) {
        manager
            .sqlServers()
            .define("testsqlserver")
            .withExistingSqlServerRegistration("testrg", "testsqlregistration")
            .withVersion("2008")
            .withEdition("Latin")
            .withRegistrationId(
                "/subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/testrg/providers/Microsoft.AzureData/SqlServerRegistrations/testsqlregistration")
            .withPropertyBag("")
            .create();
    }
}
```

### SqlServers_Delete

```java
import com.azure.core.util.Context;

/** Samples for SqlServers Delete. */
public final class SqlServersDeleteSamples {
    /*
     * x-ms-original-file: specification/azuredata/resource-manager/Microsoft.AzureData/preview/2019-07-24-preview/examples/DeleteSqlServer.json
     */
    /**
     * Sample code: Deletes a SQL Server.
     *
     * @param manager Entry point to AzureDataManager.
     */
    public static void deletesASQLServer(com.azure.resourcemanager.azuredata.AzureDataManager manager) {
        manager.sqlServers().deleteWithResponse("testrg", "testsqlregistration", "testsqlserver", Context.NONE);
    }
}
```

### SqlServers_Get

```java
import com.azure.core.util.Context;

/** Samples for SqlServers Get. */
public final class SqlServersGetSamples {
    /*
     * x-ms-original-file: specification/azuredata/resource-manager/Microsoft.AzureData/preview/2019-07-24-preview/examples/GetSqlServer.json
     */
    /**
     * Sample code: Gets a SQL Server.
     *
     * @param manager Entry point to AzureDataManager.
     */
    public static void getsASQLServer(com.azure.resourcemanager.azuredata.AzureDataManager manager) {
        manager.sqlServers().getWithResponse("testrg", "testsqlregistration", "testsqlserver", null, Context.NONE);
    }
}
```

### SqlServers_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for SqlServers ListByResourceGroup. */
public final class SqlServersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azuredata/resource-manager/Microsoft.AzureData/preview/2019-07-24-preview/examples/ListByResourceGroupSqlServer.json
     */
    /**
     * Sample code: Gets all SQL Servers in a SQL Server Registration.
     *
     * @param manager Entry point to AzureDataManager.
     */
    public static void getsAllSQLServersInASQLServerRegistration(
        com.azure.resourcemanager.azuredata.AzureDataManager manager) {
        manager.sqlServers().listByResourceGroup("testrg", "testsqlregistration", null, Context.NONE);
    }
}
```

