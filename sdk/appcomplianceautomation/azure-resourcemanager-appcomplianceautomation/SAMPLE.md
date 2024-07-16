# Code snippets and samples


## Evidence

- [CreateOrUpdate](#evidence_createorupdate)
- [Delete](#evidence_delete)
- [Download](#evidence_download)
- [Get](#evidence_get)
- [ListByReport](#evidence_listbyreport)

## Operations

- [List](#operations_list)

## ProviderActions

- [CheckNameAvailability](#provideractions_checknameavailability)
- [GetCollectionCount](#provideractions_getcollectioncount)
- [GetOverviewStatus](#provideractions_getoverviewstatus)
- [ListInUseStorageAccounts](#provideractions_listinusestorageaccounts)
- [Onboard](#provideractions_onboard)
- [TriggerEvaluation](#provideractions_triggerevaluation)

## Report

- [CreateOrUpdate](#report_createorupdate)
- [Delete](#report_delete)
- [Fix](#report_fix)
- [Get](#report_get)
- [GetScopingQuestions](#report_getscopingquestions)
- [List](#report_list)
- [NestedResourceCheckNameAvailability](#report_nestedresourcechecknameavailability)
- [SyncCertRecord](#report_synccertrecord)
- [Update](#report_update)
- [Verify](#report_verify)

## ScopingConfiguration

- [CreateOrUpdate](#scopingconfiguration_createorupdate)
- [Delete](#scopingconfiguration_delete)
- [Get](#scopingconfiguration_get)
- [List](#scopingconfiguration_list)

## Snapshot

- [Download](#snapshot_download)
- [Get](#snapshot_get)
- [List](#snapshot_list)

## Webhook

- [CreateOrUpdate](#webhook_createorupdate)
- [Delete](#webhook_delete)
- [Get](#webhook_get)
- [List](#webhook_list)
- [Update](#webhook_update)
### Evidence_CreateOrUpdate

```java

/**
 * Samples for Evidence CreateOrUpdate.
 */
public final class EvidenceCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Evidence_CreateOrUpdate.json
     */
    /**
     * Sample code: Evidence_CreateOrUpdate.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void evidenceCreateOrUpdate(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.evidences()
            .createOrUpdateWithResponse("testReportName", "evidence1", null, null, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Evidence_Delete

```java
/**
 * Samples for Evidence Delete.
 */
public final class EvidenceDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Evidence_Delete.json
     */
    /**
     * Sample code: Evidence_Delete.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void
        evidenceDelete(com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.evidences()
            .deleteByResourceGroupWithResponse("testReportName", "evidence1", com.azure.core.util.Context.NONE);
    }
}
```

### Evidence_Download

```java

/**
 * Samples for Evidence Download.
 */
public final class EvidenceDownloadSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Evidence_Download.json
     */
    /**
     * Sample code: Evidence_Download.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void
        evidenceDownload(com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.evidences().downloadWithResponse("testReportName", "evidence1", null, com.azure.core.util.Context.NONE);
    }
}
```

### Evidence_Get

```java
/**
 * Samples for Evidence Get.
 */
public final class EvidenceGetSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Evidence_Get.json
     */
    /**
     * Sample code: Evidence_Get.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void
        evidenceGet(com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.evidences().getWithResponse("testReportName", "evidence1", com.azure.core.util.Context.NONE);
    }
}
```

### Evidence_ListByReport

```java
/**
 * Samples for Evidence ListByReport.
 */
public final class EvidenceListByReportSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Evidence_ListByReport.json
     */
    /**
     * Sample code: Evidence_ListByReport.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void
        evidenceListByReport(com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.evidences()
            .listByReport("reportName", null, null, null, null, null, null, null, com.azure.core.util.Context.NONE);
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
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void
        operationsList(com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### ProviderActions_CheckNameAvailability

```java

/**
 * Samples for ProviderActions CheckNameAvailability.
 */
public final class ProviderActionsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Report_CheckNameAvailability.json
     */
    /**
     * Sample code: Report_CheckNameAvailability.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void reportCheckNameAvailability(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.providerActions().checkNameAvailabilityWithResponse(null, com.azure.core.util.Context.NONE);
    }
}
```

### ProviderActions_GetCollectionCount

```java

/**
 * Samples for ProviderActions GetCollectionCount.
 */
public final class ProviderActionsGetCollectionCountSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Report_GetCollectionCount.json
     */
    /**
     * Sample code: Report_GetCollectionCount.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void reportGetCollectionCount(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.providerActions().getCollectionCountWithResponse(null, com.azure.core.util.Context.NONE);
    }
}
```

### ProviderActions_GetOverviewStatus

```java

/**
 * Samples for ProviderActions GetOverviewStatus.
 */
public final class ProviderActionsGetOverviewStatusSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Report_GetOverviewStatus.json
     */
    /**
     * Sample code: Report_GetOverviewStatus.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void reportGetOverviewStatus(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.providerActions().getOverviewStatusWithResponse(null, com.azure.core.util.Context.NONE);
    }
}
```

### ProviderActions_ListInUseStorageAccounts

```java

/**
 * Samples for ProviderActions ListInUseStorageAccounts.
 */
public final class ProviderActionsListInUseStorageAccountsSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/ListInUseStorageAccountsWithSubscriptions.json
     */
    /**
     * Sample code: ListInUseStorageAccountsWithSubscriptions.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void listInUseStorageAccountsWithSubscriptions(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.providerActions().listInUseStorageAccountsWithResponse(null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/ListInUseStorageAccountsWithoutSubscriptions.json
     */
    /**
     * Sample code: ListInUseStorageAccountsWithoutSubscriptions.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void listInUseStorageAccountsWithoutSubscriptions(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.providerActions().listInUseStorageAccountsWithResponse(null, com.azure.core.util.Context.NONE);
    }
}
```

### ProviderActions_Onboard

```java

/**
 * Samples for ProviderActions Onboard.
 */
public final class ProviderActionsOnboardSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Onboard.json
     */
    /**
     * Sample code: Onboard.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void
        onboard(com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.providerActions().onboard(null, com.azure.core.util.Context.NONE);
    }
}
```

### ProviderActions_TriggerEvaluation

```java

/**
 * Samples for ProviderActions TriggerEvaluation.
 */
public final class ProviderActionsTriggerEvaluationSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/TriggerEvaluation.json
     */
    /**
     * Sample code: TriggerEvaluation.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void
        triggerEvaluation(com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.providerActions().triggerEvaluation(null, com.azure.core.util.Context.NONE);
    }
}
```

### Report_CreateOrUpdate

```java

/**
 * Samples for Report CreateOrUpdate.
 */
public final class ReportCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Report_CreateOrUpdate.json
     */
    /**
     * Sample code: Report_CreateOrUpdate.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void
        reportCreateOrUpdate(com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.reports().createOrUpdate("testReportName", null, com.azure.core.util.Context.NONE);
    }
}
```

### Report_Delete

```java
/**
 * Samples for Report Delete.
 */
public final class ReportDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Report_Delete.json
     */
    /**
     * Sample code: Report_Delete.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void
        reportDelete(com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.reports().delete("testReportName", com.azure.core.util.Context.NONE);
    }
}
```

### Report_Fix

```java
/**
 * Samples for Report Fix.
 */
public final class ReportFixSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Report_Fix.json
     */
    /**
     * Sample code: Report_Fix.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void
        reportFix(com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.reports().fix("testReport", com.azure.core.util.Context.NONE);
    }
}
```

### Report_Get

```java
/**
 * Samples for Report Get.
 */
public final class ReportGetSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Report_Get.json
     */
    /**
     * Sample code: Report_Get.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void
        reportGet(com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.reports().getWithResponse("testReport", com.azure.core.util.Context.NONE);
    }
}
```

### Report_GetScopingQuestions

```java
/**
 * Samples for Report GetScopingQuestions.
 */
public final class ReportGetScopingQuestionsSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Report_GetScopingQuestions.json
     */
    /**
     * Sample code: Report_GetScopingQuestions.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void reportGetScopingQuestions(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.reports().getScopingQuestionsWithResponse("testReportName", com.azure.core.util.Context.NONE);
    }
}
```

### Report_List

```java
/**
 * Samples for Report List.
 */
public final class ReportListSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Report_List.json
     */
    /**
     * Sample code: Report_List.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void
        reportList(com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.reports()
            .list("1", 100, null, null, null, "00000000-0000-0000-0000-000000000000",
                "00000000-0000-0000-0000-000000000000", com.azure.core.util.Context.NONE);
    }
}
```

### Report_NestedResourceCheckNameAvailability

```java

/**
 * Samples for Report NestedResourceCheckNameAvailability.
 */
public final class ReportNestedResourceCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Report_NestedResourceCheckNameAvailability_Report_Snapshot_Check_Name_Availability.json
     */
    /**
     * Sample code: Report_SnapshotCheckNameAvailability.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void reportSnapshotCheckNameAvailability(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.reports()
            .nestedResourceCheckNameAvailabilityWithResponse("reportABC", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Report_NestedResourceCheckNameAvailability_Report_Evidence_Check_Name_Availability.json
     */
    /**
     * Sample code: Report_EvidenceCheckNameAvailability.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void reportEvidenceCheckNameAvailability(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.reports()
            .nestedResourceCheckNameAvailabilityWithResponse("reportABC", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Report_NestedResourceCheckNameAvailability_Report_Webhook_Check_Name_Availability.json
     */
    /**
     * Sample code: Report_WebhookCheckNameAvailability.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void reportWebhookCheckNameAvailability(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.reports()
            .nestedResourceCheckNameAvailabilityWithResponse("reportABC", null, com.azure.core.util.Context.NONE);
    }
}
```

### Report_SyncCertRecord

```java

/**
 * Samples for Report SyncCertRecord.
 */
public final class ReportSyncCertRecordSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Report_SyncCertRecord.json
     */
    /**
     * Sample code: Report_SyncCertRecord.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void
        reportSyncCertRecord(com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.reports().syncCertRecord("testReportName", null, com.azure.core.util.Context.NONE);
    }
}
```

### Report_Update

```java

/**
 * Samples for Report Update.
 */
public final class ReportUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Report_Update.json
     */
    /**
     * Sample code: Report_Update.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void
        reportUpdate(com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.reports().update("testReportName", null, com.azure.core.util.Context.NONE);
    }
}
```

### Report_Verify

```java
/**
 * Samples for Report Verify.
 */
public final class ReportVerifySamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Report_Verify.json
     */
    /**
     * Sample code: Report_Verify.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void
        reportVerify(com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.reports().verify("testReport", com.azure.core.util.Context.NONE);
    }
}
```

### ScopingConfiguration_CreateOrUpdate

```java

/**
 * Samples for ScopingConfiguration CreateOrUpdate.
 */
public final class ScopingConfigurationCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/ScopingConfiguration_CreateOrUpdate.json
     */
    /**
     * Sample code: ScopingConfiguration_CreateOrUpdate.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void scopingConfigurationCreateOrUpdate(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.scopingConfigurations()
            .createOrUpdateWithResponse("testReportName", "default", null, com.azure.core.util.Context.NONE);
    }
}
```

### ScopingConfiguration_Delete

```java
/**
 * Samples for ScopingConfiguration Delete.
 */
public final class ScopingConfigurationDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/ScopingConfiguration_Delete.json
     */
    /**
     * Sample code: ScopingConfiguration_Delete.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void scopingConfigurationDelete(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.scopingConfigurations()
            .deleteByResourceGroupWithResponse("testReportName", "default", com.azure.core.util.Context.NONE);
    }
}
```

### ScopingConfiguration_Get

```java
/**
 * Samples for ScopingConfiguration Get.
 */
public final class ScopingConfigurationGetSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/ScopingConfiguration_Get.json
     */
    /**
     * Sample code: ScopingConfiguration.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void
        scopingConfiguration(com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.scopingConfigurations().getWithResponse("testReportName", "default", com.azure.core.util.Context.NONE);
    }
}
```

### ScopingConfiguration_List

```java
/**
 * Samples for ScopingConfiguration List.
 */
public final class ScopingConfigurationListSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/ScopingConfiguration_List.json
     */
    /**
     * Sample code: ScopingConfiguration_List.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void scopingConfigurationList(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.scopingConfigurations().list("testReportName", com.azure.core.util.Context.NONE);
    }
}
```

### Snapshot_Download

```java

/**
 * Samples for Snapshot Download.
 */
public final class SnapshotDownloadSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Snapshot_Download_Snapshot_Download_Compliance_Report.json
     */
    /**
     * Sample code: Snapshot_Download_ComplianceReport.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void snapshotDownloadComplianceReport(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.snapshots().download("testReportName", "testSnapshotName", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Snapshot_Download_Snapshot_Download_Resource_List.json
     */
    /**
     * Sample code: Snapshot_Download_ResourceList.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void snapshotDownloadResourceList(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.snapshots().download("testReportName", "testSnapshotName", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Snapshot_Download_Snapshot_Download_Compliance_Detailed_Pdf_Report.json
     */
    /**
     * Sample code: Snapshot_Download_ComplianceDetailedPdfReport.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void snapshotDownloadComplianceDetailedPdfReport(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.snapshots().download("testReportName", "testSnapshotName", null, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Snapshot_Download_Snapshot_Download_Compliance_Pdf_Report.json
     */
    /**
     * Sample code: Snapshot_Download_CompliancePdfReport.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void snapshotDownloadCompliancePdfReport(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.snapshots().download("testReportName", "testSnapshotName", null, com.azure.core.util.Context.NONE);
    }
}
```

### Snapshot_Get

```java
/**
 * Samples for Snapshot Get.
 */
public final class SnapshotGetSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Snapshot_Get.json
     */
    /**
     * Sample code: Snapshot_Get.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void
        snapshotGet(com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.snapshots().getWithResponse("testReportName", "testSnapshot", com.azure.core.util.Context.NONE);
    }
}
```

### Snapshot_List

```java
/**
 * Samples for Snapshot List.
 */
public final class SnapshotListSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Snapshot_List.json
     */
    /**
     * Sample code: Snapshot_List.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void
        snapshotList(com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.snapshots()
            .list("testReportName", "1", 100, null, null, null, "00000000-0000-0000-0000-000000000001",
                "00000000-0000-0000-0000-000000000000", com.azure.core.util.Context.NONE);
    }
}
```

### Webhook_CreateOrUpdate

```java

/**
 * Samples for Webhook CreateOrUpdate.
 */
public final class WebhookCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Webhook_CreateOrUpdate.json
     */
    /**
     * Sample code: Webhook_CreateOrUpdate.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void webhookCreateOrUpdate(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.webhooks()
            .createOrUpdateWithResponse("testReportName", "testWebhookName", null, com.azure.core.util.Context.NONE);
    }
}
```

### Webhook_Delete

```java
/**
 * Samples for Webhook Delete.
 */
public final class WebhookDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Webhook_Delete.json
     */
    /**
     * Sample code: Webhook_Delete.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void
        webhookDelete(com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.webhooks()
            .deleteByResourceGroupWithResponse("testReportName", "testWebhookName", com.azure.core.util.Context.NONE);
    }
}
```

### Webhook_Get

```java
/**
 * Samples for Webhook Get.
 */
public final class WebhookGetSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Webhook_Get.json
     */
    /**
     * Sample code: Webhook_Get.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void
        webhookGet(com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.webhooks().getWithResponse("testReportName", "testWebhookName", com.azure.core.util.Context.NONE);
    }
}
```

### Webhook_List

```java
/**
 * Samples for Webhook List.
 */
public final class WebhookListSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Webhook_List.json
     */
    /**
     * Sample code: Webhook_List.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void
        webhookList(com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.webhooks()
            .list("testReportName", "1", 100, null, null, null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### Webhook_Update

```java

/**
 * Samples for Webhook Update.
 */
public final class WebhookUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/stable/2024-06-27/
     * examples/Webhook_Update.json
     */
    /**
     * Sample code: Webhook_Update.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void
        webhookUpdate(com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.webhooks()
            .updateWithResponse("testReportName", "testWebhookName", null, com.azure.core.util.Context.NONE);
    }
}
```

