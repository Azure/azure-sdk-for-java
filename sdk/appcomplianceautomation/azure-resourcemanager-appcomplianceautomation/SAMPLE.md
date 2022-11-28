# Code snippets and samples


## Operations

- [List](#operations_list)

## ReportOperation

- [CreateOrUpdate](#reportoperation_createorupdate)
- [Delete](#reportoperation_delete)
- [Get](#reportoperation_get)
- [Update](#reportoperation_update)

## Reports

- [List](#reports_list)

## SnapshotOperation

- [Download](#snapshotoperation_download)
- [Get](#snapshotoperation_get)

## Snapshots

- [List](#snapshots_list)
### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/preview/2022-11-16-preview/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void operationsList(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### ReportOperation_CreateOrUpdate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.appcomplianceautomation.fluent.models.ReportResourceInner;
import com.azure.resourcemanager.appcomplianceautomation.models.ReportProperties;
import com.azure.resourcemanager.appcomplianceautomation.models.ResourceMetadata;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for ReportOperation CreateOrUpdate. */
public final class ReportOperationCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/preview/2022-11-16-preview/examples/Report_CreateOrUpdate.json
     */
    /**
     * Sample code: Report_CreateOrUpdate.
     *
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void reportCreateOrUpdate(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager
            .reportOperations()
            .createOrUpdate(
                "testReportName",
                new ReportResourceInner()
                    .withProperties(
                        new ReportProperties()
                            .withOfferGuid("0000")
                            .withTimeZone("GMT Standard Time")
                            .withTriggerTime(OffsetDateTime.parse("2022-03-04T05:11:56.197Z"))
                            .withResources(
                                Arrays
                                    .asList(
                                        new ResourceMetadata()
                                            .withResourceId(
                                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/Microsoft.Network/privateEndpoints/myPrivateEndpoint")
                                            .withTags(mapOf("key1", "value1"))))),
                Context.NONE);
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

### ReportOperation_Delete

```java
import com.azure.core.util.Context;

/** Samples for ReportOperation Delete. */
public final class ReportOperationDeleteSamples {
    /*
     * x-ms-original-file: specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/preview/2022-11-16-preview/examples/Report_Delete.json
     */
    /**
     * Sample code: Report_Delete.
     *
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void reportDelete(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.reportOperations().delete("testReportName", Context.NONE);
    }
}
```

### ReportOperation_Get

```java
import com.azure.core.util.Context;

/** Samples for ReportOperation Get. */
public final class ReportOperationGetSamples {
    /*
     * x-ms-original-file: specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/preview/2022-11-16-preview/examples/Report_Get.json
     */
    /**
     * Sample code: Report_Get.
     *
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void reportGet(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.reportOperations().getWithResponse("testReport", Context.NONE);
    }
}
```

### ReportOperation_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.appcomplianceautomation.models.ReportProperties;
import com.azure.resourcemanager.appcomplianceautomation.models.ReportResourcePatch;
import com.azure.resourcemanager.appcomplianceautomation.models.ResourceMetadata;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for ReportOperation Update. */
public final class ReportOperationUpdateSamples {
    /*
     * x-ms-original-file: specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/preview/2022-11-16-preview/examples/Report_Update.json
     */
    /**
     * Sample code: Report_Update.
     *
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void reportUpdate(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager
            .reportOperations()
            .update(
                "testReportName",
                new ReportResourcePatch()
                    .withProperties(
                        new ReportProperties()
                            .withOfferGuid("0000")
                            .withTimeZone("GMT Standard Time")
                            .withTriggerTime(OffsetDateTime.parse("2022-03-04T05:11:56.197Z"))
                            .withResources(
                                Arrays
                                    .asList(
                                        new ResourceMetadata()
                                            .withResourceId(
                                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/myResourceGroup/providers/Microsoft.Network/privateEndpoints/myPrivateEndpoint")
                                            .withTags(mapOf("key1", "value1"))))),
                Context.NONE);
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

### Reports_List

```java
import com.azure.core.util.Context;

/** Samples for Reports List. */
public final class ReportsListSamples {
    /*
     * x-ms-original-file: specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/preview/2022-11-16-preview/examples/Reports_List.json
     */
    /**
     * Sample code: Reports_List.
     *
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void reportsList(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager
            .reports()
            .list(
                "1",
                100,
                null,
                "00000000-0000-0000-0000-000000000000",
                "00000000-0000-0000-0000-000000000000",
                Context.NONE);
    }
}
```

### SnapshotOperation_Download

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.appcomplianceautomation.models.DownloadType;
import com.azure.resourcemanager.appcomplianceautomation.models.SnapshotDownloadRequest;

/** Samples for SnapshotOperation Download. */
public final class SnapshotOperationDownloadSamples {
    /*
     * x-ms-original-file: specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/preview/2022-11-16-preview/examples/Snapshot_ComplianceReport_Download.json
     */
    /**
     * Sample code: Snapshot_Download_ComplianceReport.
     *
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void snapshotDownloadComplianceReport(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager
            .snapshotOperations()
            .download(
                "testReportName",
                "testSnapshotName",
                new SnapshotDownloadRequest()
                    .withReportCreatorTenantId("00000000-0000-0000-0000-000000000000")
                    .withDownloadType(DownloadType.COMPLIANCE_REPORT)
                    .withOfferGuid("00000000-0000-0000-0000-000000000000"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/preview/2022-11-16-preview/examples/Snapshot_ResourceList_Download.json
     */
    /**
     * Sample code: Snapshot_Download_ResourceList.
     *
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void snapshotDownloadResourceList(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager
            .snapshotOperations()
            .download(
                "testReportName",
                "testSnapshotName",
                new SnapshotDownloadRequest()
                    .withReportCreatorTenantId("00000000-0000-0000-0000-000000000000")
                    .withDownloadType(DownloadType.RESOURCE_LIST)
                    .withOfferGuid("00000000-0000-0000-0000-000000000000"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/preview/2022-11-16-preview/examples/Snapshot_ComplianceDetailedPdfReport_Download.json
     */
    /**
     * Sample code: Snapshot_Download_ComplianceDetailedPdfReport.
     *
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void snapshotDownloadComplianceDetailedPdfReport(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager
            .snapshotOperations()
            .download(
                "testReportName",
                "testSnapshotName",
                new SnapshotDownloadRequest()
                    .withReportCreatorTenantId("00000000-0000-0000-0000-000000000000")
                    .withDownloadType(DownloadType.COMPLIANCE_DETAILED_PDF_REPORT)
                    .withOfferGuid("00000000-0000-0000-0000-000000000000"),
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/preview/2022-11-16-preview/examples/Snapshot_CompliancePdfReport_Download.json
     */
    /**
     * Sample code: Snapshot_Download_CompliancePdfReport.
     *
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void snapshotDownloadCompliancePdfReport(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager
            .snapshotOperations()
            .download(
                "testReportName",
                "testSnapshotName",
                new SnapshotDownloadRequest()
                    .withReportCreatorTenantId("00000000-0000-0000-0000-000000000000")
                    .withDownloadType(DownloadType.COMPLIANCE_PDF_REPORT)
                    .withOfferGuid("00000000-0000-0000-0000-000000000000"),
                Context.NONE);
    }
}
```

### SnapshotOperation_Get

```java
import com.azure.core.util.Context;

/** Samples for SnapshotOperation Get. */
public final class SnapshotOperationGetSamples {
    /*
     * x-ms-original-file: specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/preview/2022-11-16-preview/examples/Snapshot_Get.json
     */
    /**
     * Sample code: Snapshot_Get.
     *
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void snapshotGet(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager.snapshotOperations().getWithResponse("testReportName", "testSnapshot", Context.NONE);
    }
}
```

### Snapshots_List

```java
import com.azure.core.util.Context;

/** Samples for Snapshots List. */
public final class SnapshotsListSamples {
    /*
     * x-ms-original-file: specification/appcomplianceautomation/resource-manager/Microsoft.AppComplianceAutomation/preview/2022-11-16-preview/examples/Snapshots_List.json
     */
    /**
     * Sample code: Snapshots_List.
     *
     * @param manager Entry point to AppComplianceAutomationManager.
     */
    public static void snapshotsList(
        com.azure.resourcemanager.appcomplianceautomation.AppComplianceAutomationManager manager) {
        manager
            .snapshots()
            .list(
                "testReportName",
                "1",
                100,
                null,
                "00000000-0000-0000-0000-000000000000",
                "00000000-0000-0000-0000-000000000000",
                Context.NONE);
    }
}
```

