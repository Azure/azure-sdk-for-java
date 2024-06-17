# Code snippets and samples


## Evidence

- [CreateOrUpdate](#evidence_createorupdate)
- [Delete](#evidence_delete)
- [Download](#evidence_download)
- [Get](#evidence_get)
- [ListByReportResource](#evidence_listbyreportresource)

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

- [CheckNameAvailability](#report_checknameavailability)
- [CreateOrUpdate](#report_createorupdate)
- [Delete](#report_delete)
- [Fix](#report_fix)
- [Get](#report_get)
- [GetScopingQuestions](#report_getscopingquestions)
- [ListByTenant](#report_listbytenant)
- [SyncCertRecord](#report_synccertrecord)
- [Update](#report_update)
- [Verify](#report_verify)

## ScopingConfiguration

- [CreateOrUpdate](#scopingconfiguration_createorupdate)
- [Delete](#scopingconfiguration_delete)
- [Get](#scopingconfiguration_get)
- [ListByReportResource](#scopingconfiguration_listbyreportresource)

## Snapshot

- [Download](#snapshot_download)
- [Get](#snapshot_get)
- [ListByReportResource](#snapshot_listbyreportresource)

## Webhook

- [CreateOrUpdate](#webhook_createorupdate)
- [Delete](#webhook_delete)
- [Get](#webhook_get)
- [ListByReportResource](#webhook_listbyreportresource)
- [Update](#webhook_update)
### Evidence_CreateOrUpdate

```java
import com.azure.resourcemanager.appcomplianceautomation.fluent.models.EvidenceResourceInner;
import com.azure.resourcemanager.appcomplianceautomation.models.EvidenceProperties;
import com.azure.resourcemanager.appcomplianceautomation.models.EvidenceType;

/**
 * Samples for Evidence CreateOrUpdate.
 */
public final class EvidenceCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/
     * Evidence_CreateOrUpdate.json
     */
    /**
     * Sample code: Evidence_CreateOrUpdate.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void evidenceCreateOrUpdate(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.evidences()
            .createOrUpdateWithResponse("testReportName", "evidence1",
                new EvidenceResourceInner().withProperties(new EvidenceProperties().withEvidenceType(EvidenceType.FILE)
                    .withFilePath("/test-byos/evidence1.png")
                    .withControlId("Operational_Security_10")
                    .withResponsibilityId("authorized_ip_ranges_should_be_defined_on_kubernetes_services")),
                null, null, com.azure.core.util.Context.NONE);
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
     * specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/Evidence_Delete.json
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
     * specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/Evidence_Download.
     * json
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
     * specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/Evidence_Get.json
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

### Evidence_ListByReportResource

```java
/**
 * Samples for Evidence ListByReportResource.
 */
public final class EvidenceListByReportResourceSamples {
    /*
     * x-ms-original-file: specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/
     * Evidence_ListByReport.json
     */
    /**
     * Sample code: Evidence_ListByReport.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void
        evidenceListByReport(com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.evidences()
            .listByReportResource("reportName", null, null, null, null, null, null, null,
                com.azure.core.util.Context.NONE);
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
     * specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/Operations_List.json
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
     * x-ms-original-file: specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/
     * Report_CheckNameAvailability.json
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
     * x-ms-original-file: specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/
     * Report_GetCollectionCount.json
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
     * x-ms-original-file: specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/
     * Report_GetOverviewStatus.json
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
     * x-ms-original-file: specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/
     * ListInUseStorageAccountsWithoutSubscriptions.json
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
     * specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/Onboard.json
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
     * specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/TriggerEvaluation.
     * json
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

### Report_CheckNameAvailability

```java

/**
 * Samples for Report CheckNameAvailability.
 */
public final class ReportCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/
     * Report_NestedResourceCheckNameAvailability_Report_Evidence_Check_Name_Availability.json
     */
    /**
     * Sample code: Report_EvidenceCheckNameAvailability.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void reportEvidenceCheckNameAvailability(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.reports().checkNameAvailabilityWithResponse("reportABC", null, com.azure.core.util.Context.NONE);
    }
}
```

### Report_CreateOrUpdate

```java
import com.azure.resourcemanager.appcomplianceautomation.fluent.models.ReportResourceInner;
import com.azure.resourcemanager.appcomplianceautomation.models.ReportProperties;
import com.azure.resourcemanager.appcomplianceautomation.models.ResourceMetadata;
import com.azure.resourcemanager.appcomplianceautomation.models.ResourceOrigin;
import com.azure.resourcemanager.appcomplianceautomation.models.StorageInfo;
import java.time.OffsetDateTime;
import java.util.Arrays;

/**
 * Samples for Report CreateOrUpdate.
 */
public final class ReportCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/
     * Report_CreateOrUpdate.json
     */
    /**
     * Sample code: Report_CreateOrUpdate.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void
        reportCreateOrUpdate(com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.reports()
            .createOrUpdate("testReportName", new ReportResourceInner().withProperties(new ReportProperties()
                .withTriggerTime(OffsetDateTime.parse("2022-03-04T05:00:00.000Z"))
                .withTimeZone("GMT Standard Time")
                .withResources(Arrays.asList(new ResourceMetadata().withResourceId(
                    "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/Microsoft.SignalRService/SignalR/mySignalRService")
                    .withResourceType("Microsoft.SignalRService/SignalR")
                    .withResourceOrigin(ResourceOrigin.AZURE)))
                .withOfferGuid("00000000-0000-0000-0000-000000000001,00000000-0000-0000-0000-000000000002")
                .withStorageInfo(new StorageInfo().withSubscriptionId("00000000-0000-0000-0000-000000000000")
                    .withResourceGroup("testResourceGroup")
                    .withAccountName("testStorageAccount")
                    .withLocation("East US"))),
                com.azure.core.util.Context.NONE);
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
     * specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/Report_Delete.json
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
     * specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/Report_Fix.json
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
     * specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/Report_Get.json
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
     * x-ms-original-file: specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/
     * Report_GetScopingQuestions.json
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

### Report_ListByTenant

```java
/**
 * Samples for Report ListByTenant.
 */
public final class ReportListByTenantSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/Report_List.json
     */
    /**
     * Sample code: Report_List.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void
        reportList(com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.reports()
            .listByTenant("1", 100, null, null, null, "00000000-0000-0000-0000-000000000000",
                "00000000-0000-0000-0000-000000000000", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/
     * Report_SyncCertRecord.json
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
     * specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/Report_Update.json
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
     * specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/Report_Verify.json
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
import com.azure.resourcemanager.appcomplianceautomation.fluent.models.ScopingConfigurationResourceInner;
import com.azure.resourcemanager.appcomplianceautomation.models.ScopingAnswer;
import com.azure.resourcemanager.appcomplianceautomation.models.ScopingConfigurationProperties;
import java.util.Arrays;

/**
 * Samples for ScopingConfiguration CreateOrUpdate.
 */
public final class ScopingConfigurationCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/
     * ScopingConfiguration_CreateOrUpdate.json
     */
    /**
     * Sample code: ScopingConfiguration_CreateOrUpdate.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void scopingConfigurationCreateOrUpdate(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.scopingConfigurations()
            .createOrUpdateWithResponse("testReportName", "default", new ScopingConfigurationResourceInner()
                .withProperties(new ScopingConfigurationProperties().withAnswers(Arrays.asList(
                    new ScopingAnswer().withQuestionId("GEN20_hostingEnvironment").withAnswers(Arrays.asList("Azure")),
                    new ScopingAnswer().withQuestionId("DHP_G07_customerDataProcess").withAnswers(Arrays.asList()),
                    new ScopingAnswer().withQuestionId("Tier2InitSub_serviceCommunicate")
                        .withAnswers(Arrays.asList())))),
                com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/
     * ScopingConfiguration_Delete.json
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
     * x-ms-original-file: specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/
     * ScopingConfiguration_Get.json
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

### ScopingConfiguration_ListByReportResource

```java
/**
 * Samples for ScopingConfiguration ListByReportResource.
 */
public final class ScopingConfigurationListByReportResourceSamples {
    /*
     * x-ms-original-file: specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/
     * ScopingConfiguration_List.json
     */
    /**
     * Sample code: ScopingConfiguration_List.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void scopingConfigurationList(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.scopingConfigurations().listByReportResource("testReportName", com.azure.core.util.Context.NONE);
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
     * x-ms-original-file: specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/
     * Snapshot_Download_Snapshot_Download_Compliance_Detailed_Pdf_Report.json
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
     * specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/Snapshot_Get.json
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

### Snapshot_ListByReportResource

```java
/**
 * Samples for Snapshot ListByReportResource.
 */
public final class SnapshotListByReportResourceSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/Snapshot_List.json
     */
    /**
     * Sample code: Snapshot_List.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void
        snapshotList(com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.snapshots()
            .listByReportResource("testReportName", "1", 100, null, null, null, "00000000-0000-0000-0000-000000000001",
                "00000000-0000-0000-0000-000000000000", com.azure.core.util.Context.NONE);
    }
}
```

### Webhook_CreateOrUpdate

```java
import com.azure.resourcemanager.appcomplianceautomation.fluent.models.WebhookResourceInner;
import com.azure.resourcemanager.appcomplianceautomation.models.ContentType;
import com.azure.resourcemanager.appcomplianceautomation.models.EnableSslVerification;
import com.azure.resourcemanager.appcomplianceautomation.models.NotificationEvent;
import com.azure.resourcemanager.appcomplianceautomation.models.SendAllEvents;
import com.azure.resourcemanager.appcomplianceautomation.models.UpdateWebhookKey;
import com.azure.resourcemanager.appcomplianceautomation.models.WebhookProperties;
import com.azure.resourcemanager.appcomplianceautomation.models.WebhookStatus;
import java.util.Arrays;

/**
 * Samples for Webhook CreateOrUpdate.
 */
public final class WebhookCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/
     * Webhook_CreateOrUpdate.json
     */
    /**
     * Sample code: Webhook_CreateOrUpdate.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void webhookCreateOrUpdate(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.webhooks()
            .createOrUpdateWithResponse("testReportName", "testWebhookName",
                new WebhookResourceInner().withProperties(new WebhookProperties().withStatus(WebhookStatus.ENABLED)
                    .withSendAllEvents(SendAllEvents.FALSE)
                    .withEvents(Arrays.asList(NotificationEvent.GENERATE_SNAPSHOT_FAILED))
                    .withPayloadUrl("https://example.com")
                    .withContentType(ContentType.APPLICATION_JSON)
                    .withWebhookKey("fakeTokenPlaceholder")
                    .withUpdateWebhookKey(UpdateWebhookKey.TRUE)
                    .withEnableSslVerification(EnableSslVerification.TRUE)),
                com.azure.core.util.Context.NONE);
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
     * specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/Webhook_Delete.json
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
     * specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/Webhook_Get.json
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

### Webhook_ListByReportResource

```java
/**
 * Samples for Webhook ListByReportResource.
 */
public final class WebhookListByReportResourceSamples {
    /*
     * x-ms-original-file:
     * specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/Webhook_List.json
     */
    /**
     * Sample code: Webhook_List.
     * 
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void
        webhookList(com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.webhooks()
            .listByReportResource("testReportName", "1", 100, null, null, null, null, null,
                com.azure.core.util.Context.NONE);
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
     * specification/appcomplianceautomation/AppComplianceAutomation.Management/examples/2024-06-27/Webhook_Update.json
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

