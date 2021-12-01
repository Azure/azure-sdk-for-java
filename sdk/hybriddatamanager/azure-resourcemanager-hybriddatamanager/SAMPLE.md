# Code snippets and samples


## DataManagers

- [Create](#datamanagers_create)
- [Delete](#datamanagers_delete)
- [GetByResourceGroup](#datamanagers_getbyresourcegroup)
- [List](#datamanagers_list)
- [ListByResourceGroup](#datamanagers_listbyresourcegroup)
- [Update](#datamanagers_update)

## DataServices

- [Get](#dataservices_get)
- [ListByDataManager](#dataservices_listbydatamanager)

## DataStoreTypes

- [Get](#datastoretypes_get)
- [ListByDataManager](#datastoretypes_listbydatamanager)

## DataStores

- [CreateOrUpdate](#datastores_createorupdate)
- [Delete](#datastores_delete)
- [Get](#datastores_get)
- [ListByDataManager](#datastores_listbydatamanager)

## JobDefinitions

- [CreateOrUpdate](#jobdefinitions_createorupdate)
- [Delete](#jobdefinitions_delete)
- [Get](#jobdefinitions_get)
- [ListByDataManager](#jobdefinitions_listbydatamanager)
- [ListByDataService](#jobdefinitions_listbydataservice)
- [Run](#jobdefinitions_run)

## Jobs

- [Cancel](#jobs_cancel)
- [Get](#jobs_get)
- [ListByDataManager](#jobs_listbydatamanager)
- [ListByDataService](#jobs_listbydataservice)
- [ListByJobDefinition](#jobs_listbyjobdefinition)
- [Resume](#jobs_resume)

## Operations

- [List](#operations_list)

## PublicKeys

- [Get](#publickeys_get)
- [ListByDataManager](#publickeys_listbydatamanager)
### DataManagers_Create

```java
/** Samples for DataManagers Create. */
public final class DataManagersCreateSamples {
    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/DataManagers_Create-PUT-example-41.json
     */
    /**
     * Sample code: DataManagers_CreatePUT41.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void dataManagersCreatePUT41(com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) {
        manager
            .dataManagers()
            .define("TestAzureSDKOperations")
            .withRegion("westus")
            .withExistingResourceGroup("ResourceGroupForSDKTest")
            .create();
    }
}
```

### DataManagers_Delete

```java
import com.azure.core.util.Context;

/** Samples for DataManagers Delete. */
public final class DataManagersDeleteSamples {
    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/DataManagers_Delete-DELETE-example-41.json
     */
    /**
     * Sample code: DataManagers_DeleteDELETE41.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void dataManagersDeleteDELETE41(
        com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) {
        manager.dataManagers().delete("ResourceGroupForSDKTest", "TestAzureSDKOperations", Context.NONE);
    }
}
```

### DataManagers_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for DataManagers GetByResourceGroup. */
public final class DataManagersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/DataManagers_Get-GET-example-41.json
     */
    /**
     * Sample code: DataManagers_GetGET41.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void dataManagersGetGET41(com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) {
        manager
            .dataManagers()
            .getByResourceGroupWithResponse("ResourceGroupForSDKTest", "TestAzureSDKOperations", Context.NONE);
    }
}
```

### DataManagers_List

```java
import com.azure.core.util.Context;

/** Samples for DataManagers List. */
public final class DataManagersListSamples {
    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/DataManagers_List-GET-example-21.json
     */
    /**
     * Sample code: DataManagers_ListGET21.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void dataManagersListGET21(com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) {
        manager.dataManagers().list(Context.NONE);
    }
}
```

### DataManagers_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for DataManagers ListByResourceGroup. */
public final class DataManagersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/DataManagers_ListByResourceGroup-GET-example-31.json
     */
    /**
     * Sample code: DataManagers_ListByResourceGroupGET31.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void dataManagersListByResourceGroupGET31(
        com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) {
        manager.dataManagers().listByResourceGroup("ResourceGroupForSDKTest", Context.NONE);
    }
}
```

### DataManagers_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.hybriddatamanager.models.DataManager;
import com.azure.resourcemanager.hybriddatamanager.models.Sku;
import java.util.HashMap;
import java.util.Map;

/** Samples for DataManagers Update. */
public final class DataManagersUpdateSamples {
    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/DataManagers_Update-PATCH-example-43.json
     */
    /**
     * Sample code: DataManagers_UpdatePATCH43.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void dataManagersUpdatePATCH43(
        com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) {
        DataManager resource =
            manager
                .dataManagers()
                .getByResourceGroupWithResponse("ResourceGroupForSDKTest", "TestAzureSDKOperations", Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("UpdateDateTime", "05-Feb-20 2:17:22 PM"))
            .withSku(new Sku().withName("DS0").withTier("Standard"))
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

### DataServices_Get

```java
import com.azure.core.util.Context;

/** Samples for DataServices Get. */
public final class DataServicesGetSamples {
    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/DataServices_Get-GET-example-62.json
     */
    /**
     * Sample code: DataServices_GetGET62.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void dataServicesGetGET62(com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) {
        manager
            .dataServices()
            .getWithResponse("DataTransformation", "ResourceGroupForSDKTest", "TestAzureSDKOperations", Context.NONE);
    }
}
```

### DataServices_ListByDataManager

```java
import com.azure.core.util.Context;

/** Samples for DataServices ListByDataManager. */
public final class DataServicesListByDataManagerSamples {
    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/DataServices_ListByDataManager-GET-example-51.json
     */
    /**
     * Sample code: DataServices_ListByDataManagerGET51.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void dataServicesListByDataManagerGET51(
        com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) {
        manager.dataServices().listByDataManager("ResourceGroupForSDKTest", "TestAzureSDKOperations", Context.NONE);
    }
}
```

### DataStoreTypes_Get

```java
import com.azure.core.util.Context;

/** Samples for DataStoreTypes Get. */
public final class DataStoreTypesGetSamples {
    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/DataStoreTypes_Get-GET-example-183.json
     */
    /**
     * Sample code: DataStoreTypes_GetGET183.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void dataStoreTypesGetGET183(com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) {
        manager
            .dataStoreTypes()
            .getWithResponse("AzureStorageAccount", "ResourceGroupForSDKTest", "TestAzureSDKOperations", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/DataStoreTypes_Get-GET-example-182.json
     */
    /**
     * Sample code: DataStoreTypes_GetGET182.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void dataStoreTypesGetGET182(com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) {
        manager
            .dataStoreTypes()
            .getWithResponse("StorSimple8000Series", "ResourceGroupForSDKTest", "TestAzureSDKOperations", Context.NONE);
    }
}
```

### DataStoreTypes_ListByDataManager

```java
import com.azure.core.util.Context;

/** Samples for DataStoreTypes ListByDataManager. */
public final class DataStoreTypesListByDataManagerSamples {
    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/DataStoreTypes_ListByDataManager-GET-example-171.json
     */
    /**
     * Sample code: DataStoreTypes_ListByDataManagerGET171.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void dataStoreTypesListByDataManagerGET171(
        com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) {
        manager.dataStoreTypes().listByDataManager("ResourceGroupForSDKTest", "TestAzureSDKOperations", Context.NONE);
    }
}
```

### DataStores_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.hybriddatamanager.models.CustomerSecret;
import com.azure.resourcemanager.hybriddatamanager.models.State;
import com.azure.resourcemanager.hybriddatamanager.models.SupportedAlgorithm;
import java.io.IOException;
import java.util.Arrays;

/** Samples for DataStores CreateOrUpdate. */
public final class DataStoresCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/DataStores_CreateOrUpdate_DataSource-PUT-example-162.json
     */
    /**
     * Sample code: DataStores_CreateOrUpdate_DataSourcePUT162.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void dataStoresCreateOrUpdateDataSourcePUT162(
        com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) throws IOException {
        manager
            .dataStores()
            .define("TestStorSimpleSource1")
            .withExistingDataManager("ResourceGroupForSDKTest", "TestAzureSDKOperations")
            .withState(State.ENABLED)
            .withDataStoreTypeId(
                "/subscriptions/6e0219f5-327a-4365-904f-05eed4227ad7/resourceGroups/ResourceGroupForSDKTest/providers/Microsoft.HybridData/dataManagers/TestAzureSDKOperations/dataStoreTypes/StorSimple8000Series")
            .withRepositoryId(
                "/subscriptions/c5fc377d-0085-41b9-86b7-cc96dc56d1e9/resourceGroups/ForDMS/providers/Microsoft.StorSimple/managers/BLR8600")
            .withExtendedProperties(
                SerializerFactory
                    .createDefaultManagementSerializerAdapter()
                    .deserialize(
                        "{\"extendedSaKey\":null,\"resourceId\":\"/subscriptions/c5fc377d-0085-41b9-86b7-cc96dc56d1e9/resourceGroups/ForDMS/providers/Microsoft.StorSimple/managers/BLR8600\"}",
                        Object.class,
                        SerializerEncoding.JSON))
            .withCustomerSecrets(
                Arrays
                    .asList(
                        new CustomerSecret()
                            .withKeyIdentifier("ServiceEncryptionKey")
                            .withKeyValue(
                                "EVuEBV40qv23xDRL4NZBuMms4e3So6ikHjrQYRvG9NloqxdgPOg+ZYzpho5lytI4fmv0ANmRIvDiDboRXcUVSjbB9T2gm19fMIuwZa4FK2+LYEgMqKK1GaLkk7xC8f5IeFUXLo6KyBBpaAiayTnWDcHuYEpMiGrV7trDDcbhMRefO3CHecmH3Z7ye8L0RQ/e7WW8GlCKZj3m0BaG7OrJgjai8gyDfMfGAG5rTqEhDVh2hLQ+TjvUjcOFwHvJusqKTENtbJTNQYmL9wZXsnwBvUwxqrGieILNB7V3GD1Ow9OiV0UCDW1e9LnMueukg+l7YJCU9FUhIPh/nSif6p32zw==:jCfio+pDtY3BSPZDpDJ0L6QdXLYMeOmxaFWtYTOZkNqNTgT8Loc/KSQRjtWS5K4N4btbznuSJ/dzg0aZEzlXgKDSuZgMfd4Ch92ZwAC/BkeCmVrTjiKJsoQXO1IICCUf7GHGBbYnnpsNJcEn4vyc9NXyKwOBjeU+I9AyK7PtYiC03RLpTS6xttFCICteBV0uoBHAiV0chZ5VIIUUMjO9u8EhHqRY7NNcGbWdVJeAb6J3vH4E/DHkQj+DXlpjcLvmK/uqBwxfNju30RJhR04Nmz6zcv/zTcvS0uN5hEPQoHLyv84hjnc4omg/gmNjo2cDW64QxA3RTJ5Sl///4xTBkg==")
                            .withAlgorithm(SupportedAlgorithm.RSA1_5)))
            .create();
    }

    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/DataStores_CreateOrUpdate_DataSink-PUT-example-162.json
     */
    /**
     * Sample code: DataStores_CreateOrUpdate_DataSinkPUT162.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void dataStoresCreateOrUpdateDataSinkPUT162(
        com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) throws IOException {
        manager
            .dataStores()
            .define("TestAzureStorage1")
            .withExistingDataManager("ResourceGroupForSDKTest", "TestAzureSDKOperations")
            .withState(State.ENABLED)
            .withDataStoreTypeId(
                "/subscriptions/6e0219f5-327a-4365-904f-05eed4227ad7/resourceGroups/ResourceGroupForSDKTest/providers/Microsoft.HybridData/dataManagers/TestAzureSDKOperations/dataStoreTypes/AzureStorageAccount")
            .withRepositoryId(
                "/subscriptions/6e0219f5-327a-4365-904f-05eed4227ad7/resourceGroups/ResourceGroupForSDKTest/providers/Microsoft.Storage/storageAccounts/dmsdatasink")
            .withExtendedProperties(
                SerializerFactory
                    .createDefaultManagementSerializerAdapter()
                    .deserialize(
                        "{\"extendedSaKey\":null,\"extendedSaName\":\"/subscriptions/6e0219f5-327a-4365-904f-05eed4227ad7/resourceGroups/ResourceGroupForSDKTest/providers/Microsoft.Storage/storageAccounts/dmsdatasink\",\"storageAccountNameForQueue\":\"/subscriptions/6e0219f5-327a-4365-904f-05eed4227ad7/resourceGroups/ResourceGroupForSDKTest/providers/Microsoft.Storage/storageAccounts/dmsdatasink\"}",
                        Object.class,
                        SerializerEncoding.JSON))
            .withCustomerSecrets(
                Arrays
                    .asList(
                        new CustomerSecret()
                            .withKeyIdentifier("StorageAccountAccessKey")
                            .withKeyValue(
                                "Of4H9eF03G8QuxvkZQEbFWv3YdN3U//WugzuqReQekbXXQyg+QSicVKrwSOOKVi1zWMYGbKg7d5/ES2gdz+O5ZEw89bvE4mJD/wQmkIsqhPnbN0gyVK6nZePXVUU1A+UzjLfvhSA6KyUQfzNAZ5/TLt6fo1JyQrKTtkvnkLFyfv1AqBZ+dw8JK3RZi/rEN8HD3R3qsBwUYfyEuGLGiujy2CGrr/1uPiUVMR6QuFWRsjm39eMSHa4maLg4tQ0IY/jIy8rMlx3KjF3CcCbPzAqEq5vXy37wkjZbus771te1gLSrzcpVKIMg4DrmgaoJ02jAu+izBjNgLXAFPSUneQ8yw==:ezMkh4PMhCnjJtYkpTaP0SdblP5VAeRe4glW2PgIzICHw3T8ZyGDoaTrCv4/m5wtcEhWdtxhta+j1MQWlK5MIA+hvf8QjIDIjQv696ov5y+pcFe/upd2ekGOei7FCwB2u7I8WnkAtIKTUkf6eDQBZXm26DjfG1Dlc+Mjjq+AerukEa6WpOyqrD7Qub26Pgmj4AsuBx19X1EAmTZacubkoiNagXM8V+IDanHOhLMvfgQ7rw8oZhWfofxi4m+eJqjOXXaqSyorNK8UEcqP6P9pDP8AN8ulXEx6rZy2B5Oi0vSV+wlRLbUuQslga4ItOGxctW/ZX8uWozt+5A3k4URt6A==")
                            .withAlgorithm(SupportedAlgorithm.RSA1_5),
                        new CustomerSecret()
                            .withKeyIdentifier("StorageAccountAccessKeyForQueue")
                            .withKeyValue(
                                "Of4H9eF03G8QuxvkZQEbFWv3YdN3U//WugzuqReQekbXXQyg+QSicVKrwSOOKVi1zWMYGbKg7d5/ES2gdz+O5ZEw89bvE4mJD/wQmkIsqhPnbN0gyVK6nZePXVUU1A+UzjLfvhSA6KyUQfzNAZ5/TLt6fo1JyQrKTtkvnkLFyfv1AqBZ+dw8JK3RZi/rEN8HD3R3qsBwUYfyEuGLGiujy2CGrr/1uPiUVMR6QuFWRsjm39eMSHa4maLg4tQ0IY/jIy8rMlx3KjF3CcCbPzAqEq5vXy37wkjZbus771te1gLSrzcpVKIMg4DrmgaoJ02jAu+izBjNgLXAFPSUneQ8yw==:ezMkh4PMhCnjJtYkpTaP0SdblP5VAeRe4glW2PgIzICHw3T8ZyGDoaTrCv4/m5wtcEhWdtxhta+j1MQWlK5MIA+hvf8QjIDIjQv696ov5y+pcFe/upd2ekGOei7FCwB2u7I8WnkAtIKTUkf6eDQBZXm26DjfG1Dlc+Mjjq+AerukEa6WpOyqrD7Qub26Pgmj4AsuBx19X1EAmTZacubkoiNagXM8V+IDanHOhLMvfgQ7rw8oZhWfofxi4m+eJqjOXXaqSyorNK8UEcqP6P9pDP8AN8ulXEx6rZy2B5Oi0vSV+wlRLbUuQslga4ItOGxctW/ZX8uWozt+5A3k4URt6A==")
                            .withAlgorithm(SupportedAlgorithm.RSA1_5)))
            .create();
    }
}
```

### DataStores_Delete

```java
import com.azure.core.util.Context;

/** Samples for DataStores Delete. */
public final class DataStoresDeleteSamples {
    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/DataStores_Delete_DataSource-DELETE-example-161.json
     */
    /**
     * Sample code: DataStores_Delete_DataSourceDELETE161.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void dataStoresDeleteDataSourceDELETE161(
        com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) {
        manager
            .dataStores()
            .delete("TestStorSimpleSource1", "ResourceGroupForSDKTest", "TestAzureSDKOperations", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/DataStores_Delete_DataSink-DELETE-example-161.json
     */
    /**
     * Sample code: DataStores_Delete_DataSinkDELETE161.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void dataStoresDeleteDataSinkDELETE161(
        com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) {
        manager
            .dataStores()
            .delete("TestAzureStorage1", "ResourceGroupForSDKTest", "TestAzureSDKOperations", Context.NONE);
    }
}
```

### DataStores_Get

```java
import com.azure.core.util.Context;

/** Samples for DataStores Get. */
public final class DataStoresGetSamples {
    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/DataStores_Get-GET-example-161.json
     */
    /**
     * Sample code: DataStores_GetGET161.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void dataStoresGetGET161(com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) {
        manager
            .dataStores()
            .getWithResponse(
                "TestStorSimpleSource1", "ResourceGroupForSDKTest", "TestAzureSDKOperations", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/DataStores_Get-GET-example-162.json
     */
    /**
     * Sample code: DataStores_GetGET162.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void dataStoresGetGET162(com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) {
        manager
            .dataStores()
            .getWithResponse("TestAzureStorage1", "ResourceGroupForSDKTest", "TestAzureSDKOperations", Context.NONE);
    }
}
```

### DataStores_ListByDataManager

```java
import com.azure.core.util.Context;

/** Samples for DataStores ListByDataManager. */
public final class DataStoresListByDataManagerSamples {
    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/DataStores_ListByDataManager-GET-example-151.json
     */
    /**
     * Sample code: DataStores_ListByDataManagerGET151.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void dataStoresListByDataManagerGET151(
        com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) {
        manager.dataStores().listByDataManager("ResourceGroupForSDKTest", "TestAzureSDKOperations", null, Context.NONE);
    }
}
```

### JobDefinitions_CreateOrUpdate

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.hybriddatamanager.models.RunLocation;
import com.azure.resourcemanager.hybriddatamanager.models.State;
import com.azure.resourcemanager.hybriddatamanager.models.UserConfirmation;
import java.io.IOException;

/** Samples for JobDefinitions CreateOrUpdate. */
public final class JobDefinitionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/JobDefinitions_CreateOrUpdate-PUT-example-83.json
     */
    /**
     * Sample code: JobDefinitions_CreateOrUpdatePUT83.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void jobDefinitionsCreateOrUpdatePUT83(
        com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) throws IOException {
        manager
            .jobDefinitions()
            .define("jobdeffromtestcode1")
            .withExistingDataService("DataTransformation", "ResourceGroupForSDKTest", "TestAzureSDKOperations")
            .withDataSourceId(
                "/subscriptions/6e0219f5-327a-4365-904f-05eed4227ad7/resourceGroups/ResourceGroupForSDKTest/providers/Microsoft.HybridData/dataManagers/TestAzureSDKOperations/dataStores/TestStorSimpleSource1")
            .withDataSinkId(
                "/subscriptions/6e0219f5-327a-4365-904f-05eed4227ad7/resourceGroups/ResourceGroupForSDKTest/providers/Microsoft.HybridData/dataManagers/TestAzureSDKOperations/dataStores/TestAzureStorage1")
            .withState(State.ENABLED)
            .withRunLocation(RunLocation.WESTUS)
            .withUserConfirmation(UserConfirmation.REQUIRED)
            .withDataServiceInput(
                SerializerFactory
                    .createDefaultManagementSerializerAdapter()
                    .deserialize(
                        "{\"AzureStorageType\":\"Blob\",\"BackupChoice\":\"UseExistingLatest\",\"ContainerName\":\"containerfromtest\",\"DeviceName\":\"8600-SHG0997877L71FC\",\"FileNameFilter\":\"*\",\"IsDirectoryMode\":false,\"RootDirectories\":[\"\\\\\"],\"VolumeNames\":[\"TestAutomation\"]}",
                        Object.class,
                        SerializerEncoding.JSON))
            .create();
    }
}
```

### JobDefinitions_Delete

```java
import com.azure.core.util.Context;

/** Samples for JobDefinitions Delete. */
public final class JobDefinitionsDeleteSamples {
    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/JobDefinitions_Delete-DELETE-example-81.json
     */
    /**
     * Sample code: JobDefinitions_DeleteDELETE81.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void jobDefinitionsDeleteDELETE81(
        com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) {
        manager
            .jobDefinitions()
            .delete(
                "DataTransformation",
                "jobdeffromtestcode1",
                "ResourceGroupForSDKTest",
                "TestAzureSDKOperations",
                Context.NONE);
    }
}
```

### JobDefinitions_Get

```java
import com.azure.core.util.Context;

/** Samples for JobDefinitions Get. */
public final class JobDefinitionsGetSamples {
    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/JobDefinitions_Get-GET-example-81.json
     */
    /**
     * Sample code: JobDefinitions_GetGET81.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void jobDefinitionsGetGET81(com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) {
        manager
            .jobDefinitions()
            .getWithResponse(
                "DataTransformation",
                "jobdeffromtestcode1",
                "ResourceGroupForSDKTest",
                "TestAzureSDKOperations",
                Context.NONE);
    }
}
```

### JobDefinitions_ListByDataManager

```java
import com.azure.core.util.Context;

/** Samples for JobDefinitions ListByDataManager. */
public final class JobDefinitionsListByDataManagerSamples {
    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/JobDefinitions_ListByDataManager-GET-example-191.json
     */
    /**
     * Sample code: JobDefinitions_ListByDataManagerGET191.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void jobDefinitionsListByDataManagerGET191(
        com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) {
        manager
            .jobDefinitions()
            .listByDataManager("ResourceGroupForSDKTest", "TestAzureSDKOperations", null, Context.NONE);
    }
}
```

### JobDefinitions_ListByDataService

```java
import com.azure.core.util.Context;

/** Samples for JobDefinitions ListByDataService. */
public final class JobDefinitionsListByDataServiceSamples {
    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/JobDefinitions_ListByDataService-GET-example-71.json
     */
    /**
     * Sample code: JobDefinitions_ListByDataServiceGET71.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void jobDefinitionsListByDataServiceGET71(
        com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) {
        manager
            .jobDefinitions()
            .listByDataService(
                "DataTransformation", "ResourceGroupForSDKTest", "TestAzureSDKOperations", null, Context.NONE);
    }
}
```

### JobDefinitions_Run

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.hybriddatamanager.models.RunParameters;
import com.azure.resourcemanager.hybriddatamanager.models.UserConfirmation;
import java.io.IOException;
import java.util.Arrays;

/** Samples for JobDefinitions Run. */
public final class JobDefinitionsRunSamples {
    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/JobDefinitions_Run-POST-example-132.json
     */
    /**
     * Sample code: JobDefinitions_RunPOST132.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void jobDefinitionsRunPOST132(com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager)
        throws IOException {
        manager
            .jobDefinitions()
            .run(
                "DataTransformation",
                "jobdeffromtestcode1",
                "ResourceGroupForSDKTest",
                "TestAzureSDKOperations",
                new RunParameters()
                    .withUserConfirmation(UserConfirmation.NOT_REQUIRED)
                    .withDataServiceInput(
                        SerializerFactory
                            .createDefaultManagementSerializerAdapter()
                            .deserialize(
                                "{\"AzureStorageType\":\"Blob\",\"BackupChoice\":\"UseExistingLatest\",\"ContainerName\":\"containerfromtest\",\"DeviceName\":\"8600-SHG0997877L71FC\",\"FileNameFilter\":\"*\",\"IsDirectoryMode\":false,\"RootDirectories\":[\"\\\\\"],\"VolumeNames\":[\"TestAutomation\"]}",
                                Object.class,
                                SerializerEncoding.JSON))
                    .withCustomerSecrets(Arrays.asList()),
                Context.NONE);
    }
}
```

### Jobs_Cancel

```java
import com.azure.core.util.Context;

/** Samples for Jobs Cancel. */
public final class JobsCancelSamples {
    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/Jobs_Cancel-POST-example-111.json
     */
    /**
     * Sample code: Jobs_CancelPOST111.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void jobsCancelPOST111(com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) {
        manager
            .jobs()
            .cancel(
                "DataTransformation",
                "jobdeffromtestcode1",
                "6eca9b3d-5ffe-4b44-9607-1ba838371ff7",
                "ResourceGroupForSDKTest",
                "TestAzureSDKOperations",
                Context.NONE);
    }
}
```

### Jobs_Get

```java
import com.azure.core.util.Context;

/** Samples for Jobs Get. */
public final class JobsGetSamples {
    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/Jobs_Get-GET-example-101.json
     */
    /**
     * Sample code: Jobs_GetGET101.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void jobsGetGET101(com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) {
        manager
            .jobs()
            .getWithResponse(
                "DataTransformation",
                "jobdeffromtestcode1",
                "99ef93fe-36be-43e4-bebf-de6746730601",
                "ResourceGroupForSDKTest",
                "TestAzureSDKOperations",
                null,
                Context.NONE);
    }
}
```

### Jobs_ListByDataManager

```java
import com.azure.core.util.Context;

/** Samples for Jobs ListByDataManager. */
public final class JobsListByDataManagerSamples {
    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/Jobs_ListByDataManager-GET-example-201.json
     */
    /**
     * Sample code: Jobs_ListByDataManagerGET201.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void jobsListByDataManagerGET201(
        com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) {
        manager.jobs().listByDataManager("ResourceGroupForSDKTest", "TestAzureSDKOperations", null, Context.NONE);
    }
}
```

### Jobs_ListByDataService

```java
import com.azure.core.util.Context;

/** Samples for Jobs ListByDataService. */
public final class JobsListByDataServiceSamples {
    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/Jobs_ListByDataService-GET-example-141.json
     */
    /**
     * Sample code: Jobs_ListByDataServiceGET141.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void jobsListByDataServiceGET141(
        com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) {
        manager
            .jobs()
            .listByDataService(
                "DataTransformation", "ResourceGroupForSDKTest", "TestAzureSDKOperations", null, Context.NONE);
    }
}
```

### Jobs_ListByJobDefinition

```java
import com.azure.core.util.Context;

/** Samples for Jobs ListByJobDefinition. */
public final class JobsListByJobDefinitionSamples {
    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/Jobs_ListByJobDefinition-GET-example-91.json
     */
    /**
     * Sample code: Jobs_ListByJobDefinitionGET91.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void jobsListByJobDefinitionGET91(
        com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) {
        manager
            .jobs()
            .listByJobDefinition(
                "DataTransformation",
                "jobdeffromtestcode1",
                "ResourceGroupForSDKTest",
                "TestAzureSDKOperations",
                null,
                Context.NONE);
    }
}
```

### Jobs_Resume

```java
import com.azure.core.util.Context;

/** Samples for Jobs Resume. */
public final class JobsResumeSamples {
    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/Jobs_Resume-POST-example-121.json
     */
    /**
     * Sample code: Jobs_ResumePOST121.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void jobsResumePOST121(com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) {
        manager
            .jobs()
            .resume(
                "DataTransformation",
                "jobdeffromtestcode1",
                "99ef93fe-36be-43e4-bebf-de6746730601",
                "ResourceGroupForSDKTest",
                "TestAzureSDKOperations",
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
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/Operations_List-GET-example-11.json
     */
    /**
     * Sample code: Operations_ListGET11.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void operationsListGET11(com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### PublicKeys_Get

```java
import com.azure.core.util.Context;

/** Samples for PublicKeys Get. */
public final class PublicKeysGetSamples {
    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/PublicKeys_Get-GET-example-222.json
     */
    /**
     * Sample code: PublicKeys_GetGET222.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void publicKeysGetGET222(com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) {
        manager
            .publicKeys()
            .getWithResponse("default", "ResourceGroupForSDKTest", "TestAzureSDKOperations", Context.NONE);
    }
}
```

### PublicKeys_ListByDataManager

```java
import com.azure.core.util.Context;

/** Samples for PublicKeys ListByDataManager. */
public final class PublicKeysListByDataManagerSamples {
    /*
     * x-ms-original-file: specification/hybriddatamanager/resource-manager/Microsoft.HybridData/stable/2019-06-01/examples/PublicKeys_ListByDataManager-GET-example-211.json
     */
    /**
     * Sample code: PublicKeys_ListByDataManagerGET211.
     *
     * @param manager Entry point to HybridDataManager.
     */
    public static void publicKeysListByDataManagerGET211(
        com.azure.resourcemanager.hybriddatamanager.HybridDataManager manager) {
        manager.publicKeys().listByDataManager("ResourceGroupForSDKTest", "TestAzureSDKOperations", Context.NONE);
    }
}
```

