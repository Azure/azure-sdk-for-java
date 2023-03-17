# Code snippets and samples


## HealthReportOperation

- [Get](#healthreportoperation_get)

## HealthReports

- [List](#healthreports_list)

## SqlVulnerabilityAssessmentBaselineRules

- [Add](#sqlvulnerabilityassessmentbaselinerules_add)
- [CreateOrUpdate](#sqlvulnerabilityassessmentbaselinerules_createorupdate)
- [Delete](#sqlvulnerabilityassessmentbaselinerules_delete)
- [Get](#sqlvulnerabilityassessmentbaselinerules_get)
- [List](#sqlvulnerabilityassessmentbaselinerules_list)

## SqlVulnerabilityAssessmentScanResults

- [Get](#sqlvulnerabilityassessmentscanresults_get)
- [List](#sqlvulnerabilityassessmentscanresults_list)

## SqlVulnerabilityAssessmentScans

- [Get](#sqlvulnerabilityassessmentscans_get)
- [List](#sqlvulnerabilityassessmentscans_list)
### HealthReportOperation_Get

```java
/** Samples for HealthReportOperation Get. */
public final class HealthReportOperationGetSamples {
    /*
     * x-ms-original-file: specification/security/resource-manager/Microsoft.Security/preview/2023-02-01-preview/examples/HealthReports/GetHealthReport_example.json
     */
    /**
     * Sample code: Get health report of resource.
     *
     * @param manager Entry point to SecurityManager.
     */
    public static void getHealthReportOfResource(com.azure.resourcemanager.security.SecurityManager manager) {
        manager
            .healthReportOperations()
            .getWithResponse(
                "subscriptions/a1efb6ca-fbc5-4782-9aaa-5c7daded1ce2/resourcegroups/E2E-IBB0WX/providers/Microsoft.Security/securityconnectors/AwsConnectorAllOfferings",
                "909c629a-bf39-4521-8e4f-10b443a0bc02",
                com.azure.core.util.Context.NONE);
    }
}
```

### HealthReports_List

```java
/** Samples for HealthReports List. */
public final class HealthReportsListSamples {
    /*
     * x-ms-original-file: specification/security/resource-manager/Microsoft.Security/preview/2023-02-01-preview/examples/HealthReports/ListHealthReports_example.json
     */
    /**
     * Sample code: List health reports.
     *
     * @param manager Entry point to SecurityManager.
     */
    public static void listHealthReports(com.azure.resourcemanager.security.SecurityManager manager) {
        manager
            .healthReports()
            .list("subscriptions/a1efb6ca-fbc5-4782-9aaa-5c7daded1ce2", com.azure.core.util.Context.NONE);
    }
}
```

### SqlVulnerabilityAssessmentBaselineRules_Add

```java
import com.azure.resourcemanager.security.models.RulesResultsInput;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for SqlVulnerabilityAssessmentBaselineRules Add. */
public final class SqlVulnerabilityAssessmentBaselineRulesAddSamples {
    /*
     * x-ms-original-file: specification/security/resource-manager/Microsoft.Security/preview/2023-02-01-preview/examples/sqlVulnerabilityAssessmentsBaselineRuleOperations/ArcMachineBaselineRules_Add.json
     */
    /**
     * Sample code: Create a baseline for all rules.
     *
     * @param manager Entry point to SecurityManager.
     */
    public static void createABaselineForAllRules(com.azure.resourcemanager.security.SecurityManager manager) {
        manager
            .sqlVulnerabilityAssessmentBaselineRules()
            .addWithResponse(
                "55555555-6666-7777-8888-999999999999",
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/Rg/providers/Microsoft.HybridCompute/machines/MyMachine/sqlServers/server1/databases/master",
                new RulesResultsInput()
                    .withLatestScan(false)
                    .withResults(
                        mapOf(
                            "VA1234",
                            Arrays.asList(Arrays.asList("userA", "SELECT"), Arrays.asList("userB", "SELECT")),
                            "VA5678",
                            Arrays.asList(Arrays.asList("Test", "0.0.0.0", "125.125.125.125")))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/security/resource-manager/Microsoft.Security/preview/2023-02-01-preview/examples/sqlVulnerabilityAssessmentsBaselineRuleOperations/ArcMachineBaselineRules_AddLatest.json
     */
    /**
     * Sample code: Create a baseline for all rules using the latest scan results.
     *
     * @param manager Entry point to SecurityManager.
     */
    public static void createABaselineForAllRulesUsingTheLatestScanResults(
        com.azure.resourcemanager.security.SecurityManager manager) {
        manager
            .sqlVulnerabilityAssessmentBaselineRules()
            .addWithResponse(
                "55555555-6666-7777-8888-999999999999",
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/Rg/providers/Microsoft.HybridCompute/machines/MyMachine/sqlServers/server1/databases/master",
                new RulesResultsInput().withLatestScan(true).withResults(mapOf()),
                com.azure.core.util.Context.NONE);
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

### SqlVulnerabilityAssessmentBaselineRules_CreateOrUpdate

```java
import java.util.Arrays;

/** Samples for SqlVulnerabilityAssessmentBaselineRules CreateOrUpdate. */
public final class SqlVulnerabilityAssessmentBaselineRulesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/security/resource-manager/Microsoft.Security/preview/2023-02-01-preview/examples/sqlVulnerabilityAssessmentsBaselineRuleOperations/ArcMachineBaselineRules_PutLatest.json
     */
    /**
     * Sample code: Create a baseline using the latest scan results.
     *
     * @param manager Entry point to SecurityManager.
     */
    public static void createABaselineUsingTheLatestScanResults(
        com.azure.resourcemanager.security.SecurityManager manager) {
        manager
            .sqlVulnerabilityAssessmentBaselineRules()
            .define("VA1234")
            .withExistingResourceId(
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/Rg/providers/Microsoft.HybridCompute/machines/MyMachine/sqlServers/server1/databases/master")
            .withLatestScan(true)
            .withResults(Arrays.asList())
            .withWorkspaceId("55555555-6666-7777-8888-999999999999")
            .create();
    }

    /*
     * x-ms-original-file: specification/security/resource-manager/Microsoft.Security/preview/2023-02-01-preview/examples/sqlVulnerabilityAssessmentsBaselineRuleOperations/ArcMachineBaselineRules_Put.json
     */
    /**
     * Sample code: Create a baseline.
     *
     * @param manager Entry point to SecurityManager.
     */
    public static void createABaseline(com.azure.resourcemanager.security.SecurityManager manager) {
        manager
            .sqlVulnerabilityAssessmentBaselineRules()
            .define("VA1234")
            .withExistingResourceId(
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/Rg/providers/Microsoft.HybridCompute/machines/MyMachine/sqlServers/server1/databases/master")
            .withLatestScan(false)
            .withResults(Arrays.asList(Arrays.asList("userA", "SELECT"), Arrays.asList("userB", "SELECT")))
            .withWorkspaceId("55555555-6666-7777-8888-999999999999")
            .create();
    }
}
```

### SqlVulnerabilityAssessmentBaselineRules_Delete

```java
/** Samples for SqlVulnerabilityAssessmentBaselineRules Delete. */
public final class SqlVulnerabilityAssessmentBaselineRulesDeleteSamples {
    /*
     * x-ms-original-file: specification/security/resource-manager/Microsoft.Security/preview/2023-02-01-preview/examples/sqlVulnerabilityAssessmentsBaselineRuleOperations/ArcMachineBaselineRules_Delete.json
     */
    /**
     * Sample code: Delete the baseline.
     *
     * @param manager Entry point to SecurityManager.
     */
    public static void deleteTheBaseline(com.azure.resourcemanager.security.SecurityManager manager) {
        manager
            .sqlVulnerabilityAssessmentBaselineRules()
            .deleteWithResponse(
                "VA1234",
                "55555555-6666-7777-8888-999999999999",
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/Rg/providers/Microsoft.HybridCompute/machines/MyMachine/sqlServers/server1/databases/master",
                com.azure.core.util.Context.NONE);
    }
}
```

### SqlVulnerabilityAssessmentBaselineRules_Get

```java
/** Samples for SqlVulnerabilityAssessmentBaselineRules Get. */
public final class SqlVulnerabilityAssessmentBaselineRulesGetSamples {
    /*
     * x-ms-original-file: specification/security/resource-manager/Microsoft.Security/preview/2023-02-01-preview/examples/sqlVulnerabilityAssessmentsBaselineRuleOperations/ArcMachineBaselineRules_Get.json
     */
    /**
     * Sample code: Get the baseline.
     *
     * @param manager Entry point to SecurityManager.
     */
    public static void getTheBaseline(com.azure.resourcemanager.security.SecurityManager manager) {
        manager
            .sqlVulnerabilityAssessmentBaselineRules()
            .getWithResponse(
                "VA1234",
                "55555555-6666-7777-8888-999999999999",
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/Rg/providers/Microsoft.HybridCompute/machines/MyMachine/sqlServers/server1/databases/master",
                com.azure.core.util.Context.NONE);
    }
}
```

### SqlVulnerabilityAssessmentBaselineRules_List

```java
/** Samples for SqlVulnerabilityAssessmentBaselineRules List. */
public final class SqlVulnerabilityAssessmentBaselineRulesListSamples {
    /*
     * x-ms-original-file: specification/security/resource-manager/Microsoft.Security/preview/2023-02-01-preview/examples/sqlVulnerabilityAssessmentsBaselineRuleOperations/ArcMachineBaselineRules_List.json
     */
    /**
     * Sample code: List baseline for all rules.
     *
     * @param manager Entry point to SecurityManager.
     */
    public static void listBaselineForAllRules(com.azure.resourcemanager.security.SecurityManager manager) {
        manager
            .sqlVulnerabilityAssessmentBaselineRules()
            .listWithResponse(
                "55555555-6666-7777-8888-999999999999",
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/Rg/providers/Microsoft.HybridCompute/machines/MyMachine/sqlServers/server1/databases/master",
                com.azure.core.util.Context.NONE);
    }
}
```

### SqlVulnerabilityAssessmentScanResults_Get

```java
/** Samples for SqlVulnerabilityAssessmentScanResults Get. */
public final class SqlVulnerabilityAssessmentScanResultsGetSamples {
    /*
     * x-ms-original-file: specification/security/resource-manager/Microsoft.Security/preview/2023-02-01-preview/examples/sqlVulnerabilityAssessmentsScanResultsOperations/ArcMachineScanResults_Get.json
     */
    /**
     * Sample code: Get scan details of a scan record.
     *
     * @param manager Entry point to SecurityManager.
     */
    public static void getScanDetailsOfAScanRecord(com.azure.resourcemanager.security.SecurityManager manager) {
        manager
            .sqlVulnerabilityAssessmentScanResults()
            .getWithResponse(
                "Scheduled-20200623",
                "VA2063",
                "55555555-6666-7777-8888-999999999999",
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/Rg/providers/Microsoft.HybridCompute/machines/MyMachine/sqlServers/server1/databases/master",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/security/resource-manager/Microsoft.Security/preview/2023-02-01-preview/examples/sqlVulnerabilityAssessmentsScanResultsOperations/ArcMachineScanResults_GetLatest.json
     */
    /**
     * Sample code: Get scan details of the latest scan record.
     *
     * @param manager Entry point to SecurityManager.
     */
    public static void getScanDetailsOfTheLatestScanRecord(com.azure.resourcemanager.security.SecurityManager manager) {
        manager
            .sqlVulnerabilityAssessmentScanResults()
            .getWithResponse(
                "latest",
                "VA2063",
                "55555555-6666-7777-8888-999999999999",
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/Rg/providers/Microsoft.HybridCompute/machines/MyMachine/sqlServers/server1/databases/master",
                com.azure.core.util.Context.NONE);
    }
}
```

### SqlVulnerabilityAssessmentScanResults_List

```java
/** Samples for SqlVulnerabilityAssessmentScanResults List. */
public final class SqlVulnerabilityAssessmentScanResultsListSamples {
    /*
     * x-ms-original-file: specification/security/resource-manager/Microsoft.Security/preview/2023-02-01-preview/examples/sqlVulnerabilityAssessmentsScanResultsOperations/ArcMachineScanResults_ListLatest.json
     */
    /**
     * Sample code: List scan results of the latest scan.
     *
     * @param manager Entry point to SecurityManager.
     */
    public static void listScanResultsOfTheLatestScan(com.azure.resourcemanager.security.SecurityManager manager) {
        manager
            .sqlVulnerabilityAssessmentScanResults()
            .listWithResponse(
                "latest",
                "55555555-6666-7777-8888-999999999999",
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/Rg/providers/Microsoft.HybridCompute/machines/MyMachine/sqlServers/server1/databases/master",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/security/resource-manager/Microsoft.Security/preview/2023-02-01-preview/examples/sqlVulnerabilityAssessmentsScanResultsOperations/ArcMachineScanResults_List.json
     */
    /**
     * Sample code: List scan results.
     *
     * @param manager Entry point to SecurityManager.
     */
    public static void listScanResults(com.azure.resourcemanager.security.SecurityManager manager) {
        manager
            .sqlVulnerabilityAssessmentScanResults()
            .listWithResponse(
                "Scheduled-20200623",
                "55555555-6666-7777-8888-999999999999",
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/Rg/providers/Microsoft.HybridCompute/machines/MyMachine/sqlServers/server1/databases/master",
                com.azure.core.util.Context.NONE);
    }
}
```

### SqlVulnerabilityAssessmentScans_Get

```java
/** Samples for SqlVulnerabilityAssessmentScans Get. */
public final class SqlVulnerabilityAssessmentScansGetSamples {
    /*
     * x-ms-original-file: specification/security/resource-manager/Microsoft.Security/preview/2023-02-01-preview/examples/sqlVulnerabilityAssessmentScanOperations/ArcMachineScans_Get.json
     */
    /**
     * Sample code: Get scan details of a scan record.
     *
     * @param manager Entry point to SecurityManager.
     */
    public static void getScanDetailsOfAScanRecord(com.azure.resourcemanager.security.SecurityManager manager) {
        manager
            .sqlVulnerabilityAssessmentScans()
            .getWithResponse(
                "Scheduled-20200623",
                "55555555-6666-7777-8888-999999999999",
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/Rg/providers/Microsoft.HybridCompute/machines/MyMachine/sqlServers/server1/databases/master",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/security/resource-manager/Microsoft.Security/preview/2023-02-01-preview/examples/sqlVulnerabilityAssessmentScanOperations/ArcMachineScans_GetLatest.json
     */
    /**
     * Sample code: Get scan details of the latest scan record.
     *
     * @param manager Entry point to SecurityManager.
     */
    public static void getScanDetailsOfTheLatestScanRecord(com.azure.resourcemanager.security.SecurityManager manager) {
        manager
            .sqlVulnerabilityAssessmentScans()
            .getWithResponse(
                "latest",
                "55555555-6666-7777-8888-999999999999",
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/Rg/providers/Microsoft.HybridCompute/machines/MyMachine/sqlServers/server1/databases/master",
                com.azure.core.util.Context.NONE);
    }
}
```

### SqlVulnerabilityAssessmentScans_List

```java
/** Samples for SqlVulnerabilityAssessmentScans List. */
public final class SqlVulnerabilityAssessmentScansListSamples {
    /*
     * x-ms-original-file: specification/security/resource-manager/Microsoft.Security/preview/2023-02-01-preview/examples/sqlVulnerabilityAssessmentScanOperations/ArcMachineScans_List.json
     */
    /**
     * Sample code: List scan details.
     *
     * @param manager Entry point to SecurityManager.
     */
    public static void listScanDetails(com.azure.resourcemanager.security.SecurityManager manager) {
        manager
            .sqlVulnerabilityAssessmentScans()
            .listWithResponse(
                "55555555-6666-7777-8888-999999999999",
                "subscriptions/00000000-1111-2222-3333-444444444444/resourceGroups/Rg/providers/Microsoft.HybridCompute/machines/MyMachine/sqlServers/server1/databases/master",
                com.azure.core.util.Context.NONE);
    }
}
```

