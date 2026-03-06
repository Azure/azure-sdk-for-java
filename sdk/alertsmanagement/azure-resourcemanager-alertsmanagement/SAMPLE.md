# Code snippets and samples


## Alerts

- [ChangeState](#alerts_changestate)
- [ChangeStateTenant](#alerts_changestatetenant)
- [GetAll](#alerts_getall)
- [GetAllTenant](#alerts_getalltenant)
- [GetById](#alerts_getbyid)
- [GetByIdTenant](#alerts_getbyidtenant)
- [GetEnrichments](#alerts_getenrichments)
- [GetHistory](#alerts_gethistory)
- [GetHistoryTenant](#alerts_gethistorytenant)
- [GetSummary](#alerts_getsummary)
- [MetaData](#alerts_metadata)

## Operations

- [List](#operations_list)
### Alerts_ChangeState

```java
import com.azure.resourcemanager.alertsmanagement.models.AlertState;
import com.azure.resourcemanager.alertsmanagement.models.Comments;

/**
 * Samples for Alerts ChangeState.
 */
public final class AlertsChangeStateSamples {
    /*
     * x-ms-original-file: 2025-05-25-preview/Alerts_ChangeState.json
     */
    /**
     * Sample code: Resolve.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void resolve(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alerts()
            .changeStateWithResponse(
                "subscriptions/3b540246-808d-4331-99aa-917b808a9166/resourcegroups/servicedeskresourcegroup/providers/microsoft.insights/components/servicedeskappinsight",
                "66114d64-d9d9-478b-95c9-b789d6502100", AlertState.ACKNOWLEDGED, new Comments(),
                com.azure.core.util.Context.NONE);
    }
}
```

### Alerts_ChangeStateTenant

```java
import com.azure.resourcemanager.alertsmanagement.models.AlertState;
import com.azure.resourcemanager.alertsmanagement.models.Comments;

/**
 * Samples for Alerts ChangeStateTenant.
 */
public final class AlertsChangeStateTenantSamples {
    /*
     * x-ms-original-file: 2025-05-25-preview/Alerts_ChangeStateTenant.json
     */
    /**
     * Sample code: Resolve.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void resolve(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alerts()
            .changeStateTenantWithResponse("66114d64-d9d9-478b-95c9-b789d6502100", AlertState.ACKNOWLEDGED,
                new Comments(), com.azure.core.util.Context.NONE);
    }
}
```

### Alerts_GetAll

```java

/**
 * Samples for Alerts GetAll.
 */
public final class AlertsGetAllSamples {
    /*
     * x-ms-original-file: 2025-05-25-preview/Alerts_List.json
     */
    /**
     * Sample code: ListAlerts.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void listAlerts(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alerts()
            .getAll("subscriptions/3b540246-808d-4331-99aa-917b808a9166", null, null, null, null, null, null, null,
                null, null, true, null, null, null, null, null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### Alerts_GetAllTenant

```java

/**
 * Samples for Alerts GetAllTenant.
 */
public final class AlertsGetAllTenantSamples {
    /*
     * x-ms-original-file: 2025-05-25-preview/Alerts_List_GetAllTenant.json
     */
    /**
     * Sample code: ListAlerts.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void listAlerts(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alerts()
            .getAllTenant(null, null, null, null, null, null, null, null, null, true, null, null, null, null, null,
                null, null, com.azure.core.util.Context.NONE);
    }
}
```

### Alerts_GetById

```java
/**
 * Samples for Alerts GetById.
 */
public final class AlertsGetByIdSamples {
    /*
     * x-ms-original-file: 2025-05-25-preview/Alerts_GetById.json
     */
    /**
     * Sample code: GetById.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void getById(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alerts()
            .getByIdWithResponse(
                "subscriptions/3b540246-808d-4331-99aa-917b808a9166/resourcegroups/servicedeskresourcegroup/providers/microsoft.insights/components/servicedeskappinsight",
                "66114d64-d9d9-478b-95c9-b789d6502100", com.azure.core.util.Context.NONE);
    }
}
```

### Alerts_GetByIdTenant

```java
/**
 * Samples for Alerts GetByIdTenant.
 */
public final class AlertsGetByIdTenantSamples {
    /*
     * x-ms-original-file: 2025-05-25-preview/Alerts_GetByIdTenant.json
     */
    /**
     * Sample code: GetById.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void getById(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alerts()
            .getByIdTenantWithResponse("66114d64-d9d9-478b-95c9-b789d6502100", com.azure.core.util.Context.NONE);
    }
}
```

### Alerts_GetEnrichments

```java
/**
 * Samples for Alerts GetEnrichments.
 */
public final class AlertsGetEnrichmentsSamples {
    /*
     * x-ms-original-file: 2025-05-25-preview/Alerts_GetEnrichments.json
     */
    /**
     * Sample code: Resolve.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void resolve(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alerts()
            .getEnrichments("subscriptions/72fa99ef-9c84-4a7c-b343-ec62da107d81",
                "66114d64-d9d9-478b-95c9-b789d6502101", com.azure.core.util.Context.NONE);
    }
}
```

### Alerts_GetHistory

```java
/**
 * Samples for Alerts GetHistory.
 */
public final class AlertsGetHistorySamples {
    /*
     * x-ms-original-file: 2025-05-25-preview/Alerts_History.json
     */
    /**
     * Sample code: Resolve.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void resolve(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alerts()
            .getHistoryWithResponse("subscriptions/9e261de7-c804-4b9d-9ebf-6f50fe350a9a",
                "66114d64-d9d9-478b-95c9-b789d6502100", com.azure.core.util.Context.NONE);
    }
}
```

### Alerts_GetHistoryTenant

```java
/**
 * Samples for Alerts GetHistoryTenant.
 */
public final class AlertsGetHistoryTenantSamples {
    /*
     * x-ms-original-file: 2025-05-25-preview/Alerts_GetHistoryTenant.json
     */
    /**
     * Sample code: Resolve.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void resolve(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alerts()
            .getHistoryTenantWithResponse("66114d64-d9d9-478b-95c9-b789d6502100", com.azure.core.util.Context.NONE);
    }
}
```

### Alerts_GetSummary

```java
import com.azure.resourcemanager.alertsmanagement.models.AlertsSummaryGroupByFields;

/**
 * Samples for Alerts GetSummary.
 */
public final class AlertsGetSummarySamples {
    /*
     * x-ms-original-file: 2025-05-25-preview/Alerts_Summary.json
     */
    /**
     * Sample code: Summary.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void summary(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alerts()
            .getSummaryWithResponse("subscriptions/1e3ff1c0-771a-4119-a03b-be82a51e232d",
                AlertsSummaryGroupByFields.fromString("severity,alertState"), null, null, null, null, null, null, null,
                null, null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### Alerts_MetaData

```java
import com.azure.resourcemanager.alertsmanagement.models.Identifier;

/**
 * Samples for Alerts MetaData.
 */
public final class AlertsMetaDataSamples {
    /*
     * x-ms-original-file: 2025-05-25-preview/AlertsMetaData_MonitorService.json
     */
    /**
     * Sample code: MonService.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void monService(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.alerts().metaDataWithResponse(Identifier.MONITOR_SERVICE_LIST, com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: 2025-05-25-preview/Operations_List.json
     */
    /**
     * Sample code: ListOperations.
     * 
     * @param manager Entry point to AlertsManagementManager.
     */
    public static void listOperations(com.azure.resourcemanager.alertsmanagement.AlertsManagementManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

