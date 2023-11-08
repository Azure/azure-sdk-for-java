# Code snippets and samples


## Operations

- [List](#operations_list)

## Projects

- [CreateOrUpdate](#projects_createorupdate)
- [Delete](#projects_delete)
- [Get](#projects_get)
- [ListByResourceGroup](#projects_listbyresourcegroup)
- [Update](#projects_update)

## ResourceSkus

- [List](#resourceskus_list)

## Services

- [CheckNameAvailability](#services_checknameavailability)
- [CheckStatus](#services_checkstatus)
- [CreateOrUpdate](#services_createorupdate)
- [Delete](#services_delete)
- [GetByResourceGroup](#services_getbyresourcegroup)
- [List](#services_list)
- [ListByResourceGroup](#services_listbyresourcegroup)
- [ListSkus](#services_listskus)
- [NestedCheckNameAvailability](#services_nestedchecknameavailability)
- [Start](#services_start)
- [Stop](#services_stop)
- [Update](#services_update)

## Tasks

- [Cancel](#tasks_cancel)
- [CreateOrUpdate](#tasks_createorupdate)
- [Delete](#tasks_delete)
- [Get](#tasks_get)
- [List](#tasks_list)
- [Update](#tasks_update)

## Usages

- [List](#usages_list)
### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/stable/2018-04-19/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void operationsList(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Projects_CreateOrUpdate

```java
import com.azure.resourcemanager.datamigration.models.ProjectSourcePlatform;
import com.azure.resourcemanager.datamigration.models.ProjectTargetPlatform;

/** Samples for Projects CreateOrUpdate. */
public final class ProjectsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/stable/2018-04-19/examples/Projects_CreateOrUpdate.json
     */
    /**
     * Sample code: Projects_CreateOrUpdate.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void projectsCreateOrUpdate(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .projects()
            .define("DmsSdkProject")
            .withRegion("southcentralus")
            .withExistingService("DmsSdkRg", "DmsSdkService")
            .withSourcePlatform(ProjectSourcePlatform.SQL)
            .withTargetPlatform(ProjectTargetPlatform.SQLDB)
            .create();
    }
}
```

### Projects_Delete

```java
/** Samples for Projects Delete. */
public final class ProjectsDeleteSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/stable/2018-04-19/examples/Projects_Delete.json
     */
    /**
     * Sample code: Projects_Delete.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void projectsDelete(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .projects()
            .deleteWithResponse("DmsSdkRg", "DmsSdkService", "DmsSdkProject", null, com.azure.core.util.Context.NONE);
    }
}
```

### Projects_Get

```java
/** Samples for Projects Get. */
public final class ProjectsGetSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/stable/2018-04-19/examples/Projects_Get.json
     */
    /**
     * Sample code: Projects_Get.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void projectsGet(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .projects()
            .getWithResponse("DmsSdkRg", "DmsSdkService", "DmsSdkProject", com.azure.core.util.Context.NONE);
    }
}
```

### Projects_ListByResourceGroup

```java
/** Samples for Projects ListByResourceGroup. */
public final class ProjectsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/stable/2018-04-19/examples/Projects_List.json
     */
    /**
     * Sample code: Projects_ListByResourceGroup.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void projectsListByResourceGroup(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.projects().listByResourceGroup("DmsSdkRg", "DmsSdkService", com.azure.core.util.Context.NONE);
    }
}
```

### Projects_Update

```java
import com.azure.resourcemanager.datamigration.models.Project;
import com.azure.resourcemanager.datamigration.models.ProjectSourcePlatform;
import com.azure.resourcemanager.datamigration.models.ProjectTargetPlatform;

/** Samples for Projects Update. */
public final class ProjectsUpdateSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/stable/2018-04-19/examples/Projects_Update.json
     */
    /**
     * Sample code: Projects_Update.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void projectsUpdate(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        Project resource =
            manager
                .projects()
                .getWithResponse("DmsSdkRg", "DmsSdkService", "DmsSdkProject", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withSourcePlatform(ProjectSourcePlatform.SQL)
            .withTargetPlatform(ProjectTargetPlatform.SQLDB)
            .apply();
    }
}
```

### ResourceSkus_List

```java
/** Samples for ResourceSkus List. */
public final class ResourceSkusListSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/stable/2018-04-19/examples/ResourceSkus_ListSkus.json
     */
    /**
     * Sample code: ListSkus.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void listSkus(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.resourceSkus().list(com.azure.core.util.Context.NONE);
    }
}
```

### Services_CheckNameAvailability

```java
import com.azure.resourcemanager.datamigration.models.NameAvailabilityRequest;

/** Samples for Services CheckNameAvailability. */
public final class ServicesCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/stable/2018-04-19/examples/Services_CheckNameAvailability.json
     */
    /**
     * Sample code: Services_CheckNameAvailability.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void servicesCheckNameAvailability(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .services()
            .checkNameAvailabilityWithResponse(
                "eastus", new NameAvailabilityRequest(), com.azure.core.util.Context.NONE);
    }
}
```

### Services_CheckStatus

```java
/** Samples for Services CheckStatus. */
public final class ServicesCheckStatusSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/stable/2018-04-19/examples/Services_CheckStatus.json
     */
    /**
     * Sample code: Services_CheckStatus.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void servicesCheckStatus(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.services().checkStatusWithResponse("DmsSdkRg", "DmsSdkService", com.azure.core.util.Context.NONE);
    }
}
```

### Services_CreateOrUpdate

```java
import com.azure.resourcemanager.datamigration.models.ServiceSku;

/** Samples for Services CreateOrUpdate. */
public final class ServicesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/stable/2018-04-19/examples/Services_CreateOrUpdate.json
     */
    /**
     * Sample code: Services_CreateOrUpdate.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void servicesCreateOrUpdate(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .services()
            .define("DmsSdkService")
            .withRegion("southcentralus")
            .withExistingResourceGroup("DmsSdkRg")
            .withSku(new ServiceSku().withName("Basic_1vCore"))
            .withVirtualSubnetId(
                "/subscriptions/fc04246f-04c5-437e-ac5e-206a19e7193f/resourceGroups/DmsSdkTestNetwork/providers/Microsoft.Network/virtualNetworks/DmsSdkTestNetwork/subnets/default")
            .create();
    }
}
```

### Services_Delete

```java
/** Samples for Services Delete. */
public final class ServicesDeleteSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/stable/2018-04-19/examples/Services_Delete.json
     */
    /**
     * Sample code: Services_CreateOrUpdate.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void servicesCreateOrUpdate(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.services().delete("DmsSdkRg", "DmsSdkService", null, com.azure.core.util.Context.NONE);
    }
}
```

### Services_GetByResourceGroup

```java
/** Samples for Services GetByResourceGroup. */
public final class ServicesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/stable/2018-04-19/examples/Services_Get.json
     */
    /**
     * Sample code: Services_CreateOrUpdate.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void servicesCreateOrUpdate(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .services()
            .getByResourceGroupWithResponse("DmsSdkRg", "DmsSdkService", com.azure.core.util.Context.NONE);
    }
}
```

### Services_List

```java
/** Samples for Services List. */
public final class ServicesListSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/stable/2018-04-19/examples/Services_List.json
     */
    /**
     * Sample code: Services_List.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void servicesList(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.services().list(com.azure.core.util.Context.NONE);
    }
}
```

### Services_ListByResourceGroup

```java
/** Samples for Services ListByResourceGroup. */
public final class ServicesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/stable/2018-04-19/examples/Services_ListByResourceGroup.json
     */
    /**
     * Sample code: Services_ListByResourceGroup.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void servicesListByResourceGroup(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.services().listByResourceGroup("DmsSdkRg", com.azure.core.util.Context.NONE);
    }
}
```

### Services_ListSkus

```java
/** Samples for Services ListSkus. */
public final class ServicesListSkusSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/stable/2018-04-19/examples/Services_ListSkus.json
     */
    /**
     * Sample code: Services_ListSkus.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void servicesListSkus(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.services().listSkus("DmsSdkRg", "DmsSdkService", com.azure.core.util.Context.NONE);
    }
}
```

### Services_NestedCheckNameAvailability

```java
import com.azure.resourcemanager.datamigration.models.NameAvailabilityRequest;

/** Samples for Services NestedCheckNameAvailability. */
public final class ServicesNestedCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/stable/2018-04-19/examples/Services_NestedCheckNameAvailability.json
     */
    /**
     * Sample code: Services_NestedCheckNameAvailability.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void servicesNestedCheckNameAvailability(
        com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .services()
            .nestedCheckNameAvailabilityWithResponse(
                "DmsSdkRg", "DmsSdkService", new NameAvailabilityRequest(), com.azure.core.util.Context.NONE);
    }
}
```

### Services_Start

```java
/** Samples for Services Start. */
public final class ServicesStartSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/stable/2018-04-19/examples/Services_Start.json
     */
    /**
     * Sample code: Services_Start.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void servicesStart(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.services().start("DmsSdkRg", "DmsSdkService", com.azure.core.util.Context.NONE);
    }
}
```

### Services_Stop

```java
/** Samples for Services Stop. */
public final class ServicesStopSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/stable/2018-04-19/examples/Services_Stop.json
     */
    /**
     * Sample code: Services_Stop.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void servicesStop(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.services().stop("DmsSdkRg", "DmsSdkService", com.azure.core.util.Context.NONE);
    }
}
```

### Services_Update

```java
import com.azure.resourcemanager.datamigration.models.DataMigrationService;

/** Samples for Services Update. */
public final class ServicesUpdateSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/stable/2018-04-19/examples/Services_Update.json
     */
    /**
     * Sample code: Services_CreateOrUpdate.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void servicesCreateOrUpdate(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        DataMigrationService resource =
            manager
                .services()
                .getByResourceGroupWithResponse("DmsSdkRg", "DmsSdkService", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withVirtualSubnetId(
                "/subscriptions/fc04246f-04c5-437e-ac5e-206a19e7193f/resourceGroups/DmsSdkTestNetwork/providers/Microsoft.Network/virtualNetworks/DmsSdkTestNetwork/subnets/default")
            .apply();
    }
}
```

### Tasks_Cancel

```java
/** Samples for Tasks Cancel. */
public final class TasksCancelSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/stable/2018-04-19/examples/Tasks_Cancel.json
     */
    /**
     * Sample code: Tasks_Cancel.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void tasksCancel(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .tasks()
            .cancelWithResponse(
                "DmsSdkRg", "DmsSdkService", "DmsSdkProject", "DmsSdkTask", com.azure.core.util.Context.NONE);
    }
}
```

### Tasks_CreateOrUpdate

```java
import com.azure.resourcemanager.datamigration.models.AuthenticationType;
import com.azure.resourcemanager.datamigration.models.ConnectToTargetSqlDbTaskInput;
import com.azure.resourcemanager.datamigration.models.ConnectToTargetSqlDbTaskProperties;
import com.azure.resourcemanager.datamigration.models.SqlConnectionInfo;

/** Samples for Tasks CreateOrUpdate. */
public final class TasksCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/stable/2018-04-19/examples/Tasks_CreateOrUpdate.json
     */
    /**
     * Sample code: Tasks_CreateOrUpdate.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void tasksCreateOrUpdate(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .tasks()
            .define("DmsSdkTask")
            .withExistingProject("DmsSdkRg", "DmsSdkService", "DmsSdkProject")
            .withProperties(
                new ConnectToTargetSqlDbTaskProperties()
                    .withInput(
                        new ConnectToTargetSqlDbTaskInput()
                            .withTargetConnectionInfo(
                                new SqlConnectionInfo()
                                    .withUsername("testuser")
                                    .withPassword("fakeTokenPlaceholder")
                                    .withDataSource("ssma-test-server.database.windows.net")
                                    .withAuthentication(AuthenticationType.SQL_AUTHENTICATION)
                                    .withEncryptConnection(true)
                                    .withTrustServerCertificate(true))))
            .create();
    }
}
```

### Tasks_Delete

```java
/** Samples for Tasks Delete. */
public final class TasksDeleteSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/stable/2018-04-19/examples/Tasks_Delete.json
     */
    /**
     * Sample code: Tasks_Delete.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void tasksDelete(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .tasks()
            .deleteWithResponse(
                "DmsSdkRg", "DmsSdkService", "DmsSdkProject", "DmsSdkTask", null, com.azure.core.util.Context.NONE);
    }
}
```

### Tasks_Get

```java
/** Samples for Tasks Get. */
public final class TasksGetSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/stable/2018-04-19/examples/Tasks_Get.json
     */
    /**
     * Sample code: Tasks_Get.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void tasksGet(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager
            .tasks()
            .getWithResponse(
                "DmsSdkRg", "DmsSdkService", "DmsSdkProject", "DmsSdkTask", null, com.azure.core.util.Context.NONE);
    }
}
```

### Tasks_List

```java
/** Samples for Tasks List. */
public final class TasksListSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/stable/2018-04-19/examples/Tasks_List.json
     */
    /**
     * Sample code: Tasks_List.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void tasksList(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.tasks().list("DmsSdkRg", "DmsSdkService", "DmsSdkProject", null, com.azure.core.util.Context.NONE);
    }
}
```

### Tasks_Update

```java
import com.azure.resourcemanager.datamigration.models.AuthenticationType;
import com.azure.resourcemanager.datamigration.models.ConnectToTargetSqlDbTaskInput;
import com.azure.resourcemanager.datamigration.models.ConnectToTargetSqlDbTaskProperties;
import com.azure.resourcemanager.datamigration.models.ProjectTask;
import com.azure.resourcemanager.datamigration.models.SqlConnectionInfo;

/** Samples for Tasks Update. */
public final class TasksUpdateSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/stable/2018-04-19/examples/Tasks_Update.json
     */
    /**
     * Sample code: Tasks_Update.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void tasksUpdate(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        ProjectTask resource =
            manager
                .tasks()
                .getWithResponse(
                    "DmsSdkRg", "DmsSdkService", "DmsSdkProject", "DmsSdkTask", null, com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withProperties(
                new ConnectToTargetSqlDbTaskProperties()
                    .withInput(
                        new ConnectToTargetSqlDbTaskInput()
                            .withTargetConnectionInfo(
                                new SqlConnectionInfo()
                                    .withUsername("testuser")
                                    .withPassword("fakeTokenPlaceholder")
                                    .withDataSource("ssma-test-server.database.windows.net")
                                    .withAuthentication(AuthenticationType.SQL_AUTHENTICATION)
                                    .withEncryptConnection(true)
                                    .withTrustServerCertificate(true))))
            .apply();
    }
}
```

### Usages_List

```java
/** Samples for Usages List. */
public final class UsagesListSamples {
    /*
     * x-ms-original-file: specification/datamigration/resource-manager/Microsoft.DataMigration/stable/2018-04-19/examples/Usages_List.json
     */
    /**
     * Sample code: Services_Usages.
     *
     * @param manager Entry point to DataMigrationManager.
     */
    public static void servicesUsages(com.azure.resourcemanager.datamigration.DataMigrationManager manager) {
        manager.usages().list("westus", com.azure.core.util.Context.NONE);
    }
}
```

