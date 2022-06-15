# Code snippets and samples


## AnalysisResults

- [Get](#analysisresults_get)
- [List](#analysisresults_list)

## AvailableOS

- [Get](#availableos_get)
- [List](#availableos_list)

## BillingHubService

- [GetFreeHourBalance](#billinghubservice_getfreehourbalance)
- [GetUsage](#billinghubservice_getusage)

## CustomerEvents

- [Create](#customerevents_create)
- [Delete](#customerevents_delete)
- [Get](#customerevents_get)
- [ListByTestBaseAccount](#customerevents_listbytestbaseaccount)

## EmailEvents

- [Get](#emailevents_get)
- [List](#emailevents_list)

## FavoriteProcesses

- [Create](#favoriteprocesses_create)
- [Delete](#favoriteprocesses_delete)
- [Get](#favoriteprocesses_get)
- [List](#favoriteprocesses_list)

## FlightingRings

- [Get](#flightingrings_get)
- [List](#flightingrings_list)

## OSUpdates

- [Get](#osupdates_get)
- [List](#osupdates_list)

## Operations

- [List](#operations_list)

## Packages

- [Create](#packages_create)
- [Delete](#packages_delete)
- [Get](#packages_get)
- [GetDownloadUrl](#packages_getdownloadurl)
- [HardDelete](#packages_harddelete)
- [ListByTestBaseAccount](#packages_listbytestbaseaccount)
- [RunTest](#packages_runtest)
- [Update](#packages_update)

## Skus

- [List](#skus_list)

## TestBaseAccounts

- [CheckPackageNameAvailability](#testbaseaccounts_checkpackagenameavailability)
- [Create](#testbaseaccounts_create)
- [Delete](#testbaseaccounts_delete)
- [GetByResourceGroup](#testbaseaccounts_getbyresourcegroup)
- [GetFileUploadUrl](#testbaseaccounts_getfileuploadurl)
- [List](#testbaseaccounts_list)
- [ListByResourceGroup](#testbaseaccounts_listbyresourcegroup)
- [Offboard](#testbaseaccounts_offboard)
- [Update](#testbaseaccounts_update)

## TestResults

- [Get](#testresults_get)
- [GetConsoleLogDownloadUrl](#testresults_getconsolelogdownloadurl)
- [GetDownloadUrl](#testresults_getdownloadurl)
- [GetVideoDownloadUrl](#testresults_getvideodownloadurl)
- [List](#testresults_list)

## TestSummaries

- [Get](#testsummaries_get)
- [List](#testsummaries_list)

## TestTypes

- [Get](#testtypes_get)
- [List](#testtypes_list)

## Usage

- [List](#usage_list)
### AnalysisResults_Get

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.testbase.models.AnalysisResultName;

/** Samples for AnalysisResults Get. */
public final class AnalysisResultsGetSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/MemoryUtilizationAnalysisResultGet.json
     */
    /**
     * Sample code: MemoryUtilizationAnalysisResultGet.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void memoryUtilizationAnalysisResultGet(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .analysisResults()
            .getWithResponse(
                "contoso-rg1",
                "contoso-testBaseAccount1",
                "contoso-package2",
                "Windows-10-1909-Test-Id",
                AnalysisResultName.MEMORY_UTILIZATION,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/CPURegressionAnalysisResultGet.json
     */
    /**
     * Sample code: CPURegressionAnalysisResultGet.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void cPURegressionAnalysisResultGet(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .analysisResults()
            .getWithResponse(
                "contoso-rg1",
                "contoso-testBaseAccount1",
                "contoso-package2",
                "Windows-10-1909-Test-Id",
                AnalysisResultName.CPU_REGRESSION,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/ScriptExecutionAnalysisResultGet.json
     */
    /**
     * Sample code: ScriptExecutionAnalysisResultGet.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void scriptExecutionAnalysisResultGet(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .analysisResults()
            .getWithResponse(
                "contoso-rg1",
                "contoso-testBaseAccount1",
                "contoso-package2",
                "Windows-10-1909-Test-Id",
                AnalysisResultName.SCRIPT_EXECUTION,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/CPUUtilizationAnalysisResultGet.json
     */
    /**
     * Sample code: CPUUtilizationAnalysisResultGet.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void cPUUtilizationAnalysisResultGet(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .analysisResults()
            .getWithResponse(
                "contoso-rg1",
                "contoso-testBaseAccount1",
                "contoso-package2",
                "Windows-10-1909-Test-Id",
                AnalysisResultName.CPU_UTILIZATION,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/MemoryRegressionAnalysisResultGet.json
     */
    /**
     * Sample code: MemoryRegressionAnalysisResultGet.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void memoryRegressionAnalysisResultGet(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .analysisResults()
            .getWithResponse(
                "contoso-rg1",
                "contoso-testBaseAccount1",
                "contoso-package2",
                "Windows-10-1909-Test-Id",
                AnalysisResultName.MEMORY_REGRESSION,
                Context.NONE);
    }
}
```

### AnalysisResults_List

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.testbase.models.AnalysisResultType;

/** Samples for AnalysisResults List. */
public final class AnalysisResultsListSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/MemoryRegressionAnalysisResultsList.json
     */
    /**
     * Sample code: MemoryRegressionAnalysisResultsList.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void memoryRegressionAnalysisResultsList(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .analysisResults()
            .list(
                "contoso-rg1",
                "contoso-testBaseAccount1",
                "contoso-package2",
                "Windows-10-1909-Test-Id",
                AnalysisResultType.MEMORY_REGRESSION,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/ScriptExecutionAnalysisResultsList.json
     */
    /**
     * Sample code: ScriptExecutionAnalysisResultsList.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void scriptExecutionAnalysisResultsList(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .analysisResults()
            .list(
                "contoso-rg1",
                "contoso-testBaseAccount1",
                "contoso-package2",
                "Windows-10-1909-Test-Id",
                AnalysisResultType.SCRIPT_EXECUTION,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/CPUUtilizationAnalysisResultsList.json
     */
    /**
     * Sample code: CPUUtilizationAnalysisResultsList.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void cPUUtilizationAnalysisResultsList(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .analysisResults()
            .list(
                "contoso-rg1",
                "contoso-testBaseAccount1",
                "contoso-package2",
                "Windows-10-1909-Test-Id",
                AnalysisResultType.CPUUTILIZATION,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/MemoryUtilizationAnalysisResultsList.json
     */
    /**
     * Sample code: MemoryUtilizationAnalysisResultsList.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void memoryUtilizationAnalysisResultsList(
        com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .analysisResults()
            .list(
                "contoso-rg1",
                "contoso-testBaseAccount1",
                "contoso-package2",
                "Windows-10-1909-Test-Id",
                AnalysisResultType.MEMORY_UTILIZATION,
                Context.NONE);
    }

    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/CPURegressionAnalysisResultsList.json
     */
    /**
     * Sample code: CPURegressionAnalysisResultsList.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void cPURegressionAnalysisResultsList(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .analysisResults()
            .list(
                "contoso-rg1",
                "contoso-testBaseAccount1",
                "contoso-package2",
                "Windows-10-1909-Test-Id",
                AnalysisResultType.CPUREGRESSION,
                Context.NONE);
    }
}
```

### AvailableOS_Get

```java
import com.azure.core.util.Context;

/** Samples for AvailableOS Get. */
public final class AvailableOSGetSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/AvailableOSGet.json
     */
    /**
     * Sample code: AvailableOSGet.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void availableOSGet(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager.availableOS().getWithResponse("contoso-rg", "contoso-testBaseAccount", "Windows-10-2004", Context.NONE);
    }
}
```

### AvailableOS_List

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.testbase.models.OsUpdateType;

/** Samples for AvailableOS List. */
public final class AvailableOSListSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/AvailableOSsList.json
     */
    /**
     * Sample code: AvailableOSsList.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void availableOSsList(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager.availableOS().list("contoso-rg", "contoso-testBaseAccount", OsUpdateType.SECURITY_UPDATE, Context.NONE);
    }
}
```

### BillingHubService_GetFreeHourBalance

```java
import com.azure.core.util.Context;

/** Samples for BillingHubService GetFreeHourBalance. */
public final class BillingHubServiceGetFreeHourBalanceSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/BillingHubGetFreeHourBalance.json
     */
    /**
     * Sample code: BillingHubGetFreeHourBalance.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void billingHubGetFreeHourBalance(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .billingHubServices()
            .getFreeHourBalanceWithResponse("contoso-rg1", "contoso-testBaseAccount1", Context.NONE);
    }
}
```

### BillingHubService_GetUsage

```java
import com.azure.core.util.Context;

/** Samples for BillingHubService GetUsage. */
public final class BillingHubServiceGetUsageSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/BillingHubGetUsage.json
     */
    /**
     * Sample code: BillingHubGetUsage.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void billingHubGetUsage(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .billingHubServices()
            .getUsageWithResponse("contoso-rg1", "contoso-testBaseAccount1", null, Context.NONE);
    }
}
```

### CustomerEvents_Create

```java
import com.azure.resourcemanager.testbase.models.DistributionGroupListReceiverValue;
import com.azure.resourcemanager.testbase.models.NotificationEventReceiver;
import com.azure.resourcemanager.testbase.models.NotificationReceiverValue;
import com.azure.resourcemanager.testbase.models.UserObjectReceiverValue;
import java.util.Arrays;

/** Samples for CustomerEvents Create. */
public final class CustomerEventsCreateSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/CustomerEventCreate.json
     */
    /**
     * Sample code: CustomerEventCreate.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void customerEventCreate(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .customerEvents()
            .define("WeeklySummary")
            .withExistingTestBaseAccount("contoso-rg1", "contoso-testBaseAccount1")
            .withEventName("WeeklySummary")
            .withReceivers(
                Arrays
                    .asList(
                        new NotificationEventReceiver()
                            .withReceiverType("UserObjects")
                            .withReceiverValue(
                                new NotificationReceiverValue()
                                    .withUserObjectReceiverValue(
                                        new UserObjectReceiverValue()
                                            .withUserObjectIds(Arrays.asList("245245245245325", "365365365363565")))),
                        new NotificationEventReceiver()
                            .withReceiverType("DistributionGroup")
                            .withReceiverValue(
                                new NotificationReceiverValue()
                                    .withDistributionGroupListReceiverValue(
                                        new DistributionGroupListReceiverValue()
                                            .withDistributionGroups(Arrays.asList("test@microsoft.com"))))))
            .create();
    }
}
```

### CustomerEvents_Delete

```java
import com.azure.core.util.Context;

/** Samples for CustomerEvents Delete. */
public final class CustomerEventsDeleteSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/CustomerEventDelete.json
     */
    /**
     * Sample code: CustomerEventDelete.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void customerEventDelete(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager.customerEvents().delete("contoso-rg1", "contoso-testBaseAccount1", "WeeklySummary", Context.NONE);
    }
}
```

### CustomerEvents_Get

```java
import com.azure.core.util.Context;

/** Samples for CustomerEvents Get. */
public final class CustomerEventsGetSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/CustomerEventGet.json
     */
    /**
     * Sample code: CustomerEventGet.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void customerEventGet(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .customerEvents()
            .getWithResponse("contoso-rg1", "contoso-testBaseAccount1", "WeeklySummary", Context.NONE);
    }
}
```

### CustomerEvents_ListByTestBaseAccount

```java
import com.azure.core.util.Context;

/** Samples for CustomerEvents ListByTestBaseAccount. */
public final class CustomerEventsListByTestBaseAccountSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/CustomerEventsList.json
     */
    /**
     * Sample code: CustomerEventsList.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void customerEventsList(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager.customerEvents().listByTestBaseAccount("contoso-rg1", "contoso-testBaseAccount1", Context.NONE);
    }
}
```

### EmailEvents_Get

```java
import com.azure.core.util.Context;

/** Samples for EmailEvents Get. */
public final class EmailEventsGetSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/EmailEventGet.json
     */
    /**
     * Sample code: EmailEventGet.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void emailEventGet(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager.emailEvents().getWithResponse("contoso-rg", "contoso-testBaseAccount", "weekly-summary", Context.NONE);
    }
}
```

### EmailEvents_List

```java
import com.azure.core.util.Context;

/** Samples for EmailEvents List. */
public final class EmailEventsListSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/EmailEventsList.json
     */
    /**
     * Sample code: EmailEventsList.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void emailEventsList(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager.emailEvents().list("contoso-rg", "contoso-testBaseAccount", Context.NONE);
    }
}
```

### FavoriteProcesses_Create

```java
/** Samples for FavoriteProcesses Create. */
public final class FavoriteProcessesCreateSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/FavoriteProcessCreate.json
     */
    /**
     * Sample code: FavoriteProcessCreate.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void favoriteProcessCreate(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .favoriteProcesses()
            .define("testAppProcess")
            .withExistingPackage("contoso-rg1", "contoso-testBaseAccount1", "contoso-package2")
            .withActualProcessName("testApp&.exe")
            .create();
    }
}
```

### FavoriteProcesses_Delete

```java
import com.azure.core.util.Context;

/** Samples for FavoriteProcesses Delete. */
public final class FavoriteProcessesDeleteSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/FavoriteProcessDelete.json
     */
    /**
     * Sample code: FavoriteProcessDelete.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void favoriteProcessDelete(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .favoriteProcesses()
            .deleteWithResponse(
                "contoso-rg1", "contoso-testBaseAccount1", "contoso-package2", "testAppProcess", Context.NONE);
    }
}
```

### FavoriteProcesses_Get

```java
import com.azure.core.util.Context;

/** Samples for FavoriteProcesses Get. */
public final class FavoriteProcessesGetSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/FavoriteProcessGet.json
     */
    /**
     * Sample code: FavoriteProcessGet.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void favoriteProcessGet(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .favoriteProcesses()
            .getWithResponse(
                "contoso-rg1", "contoso-testBaseAccount1", "contoso-package2", "testAppProcess", Context.NONE);
    }
}
```

### FavoriteProcesses_List

```java
import com.azure.core.util.Context;

/** Samples for FavoriteProcesses List. */
public final class FavoriteProcessesListSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/FavoriteProcessesList.json
     */
    /**
     * Sample code: FavoriteProcessesList.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void favoriteProcessesList(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager.favoriteProcesses().list("contoso-rg1", "contoso-testBaseAccount1", "contoso-package2", Context.NONE);
    }
}
```

### FlightingRings_Get

```java
import com.azure.core.util.Context;

/** Samples for FlightingRings Get. */
public final class FlightingRingsGetSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/FlightingRingGet.json
     */
    /**
     * Sample code: FlightingRingGet.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void flightingRingGet(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .flightingRings()
            .getWithResponse("contoso-rg", "contoso-testBaseAccount", "Insider-Beta-Channel", Context.NONE);
    }
}
```

### FlightingRings_List

```java
import com.azure.core.util.Context;

/** Samples for FlightingRings List. */
public final class FlightingRingsListSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/FlightingRingsList.json
     */
    /**
     * Sample code: FlightingRingsList.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void flightingRingsList(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager.flightingRings().list("contoso-rg", "contoso-testBaseAccount", Context.NONE);
    }
}
```

### OSUpdates_Get

```java
import com.azure.core.util.Context;

/** Samples for OSUpdates Get. */
public final class OSUpdatesGetSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/OSUpdateGet.json
     */
    /**
     * Sample code: OSUpdateGet.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void oSUpdateGet(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .oSUpdates()
            .getWithResponse(
                "contoso-rg1",
                "contoso-testBaseAccount1",
                "contoso-package2",
                "Windows-10-2004-2020-12-B-505",
                Context.NONE);
    }
}
```

### OSUpdates_List

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.testbase.models.OsUpdateType;

/** Samples for OSUpdates List. */
public final class OSUpdatesListSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/OSUpdatesList.json
     */
    /**
     * Sample code: OSUpdatesList.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void oSUpdatesList(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .oSUpdates()
            .list(
                "contoso-rg1",
                "contoso-testBaseAccount1",
                "contoso-package2",
                OsUpdateType.SECURITY_UPDATE,
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
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/OperationsList.json
     */
    /**
     * Sample code: OperationsList.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void operationsList(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### Packages_Create

```java
import com.azure.resourcemanager.testbase.models.Action;
import com.azure.resourcemanager.testbase.models.Command;
import com.azure.resourcemanager.testbase.models.ContentType;
import com.azure.resourcemanager.testbase.models.TargetOSInfo;
import com.azure.resourcemanager.testbase.models.Test;
import com.azure.resourcemanager.testbase.models.TestType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Packages Create. */
public final class PackagesCreateSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/PackageCreate.json
     */
    /**
     * Sample code: PackageCreate.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void packageCreate(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .packages()
            .define("contoso-package2")
            .withRegion("westus")
            .withExistingTestBaseAccount("contoso-rg1", "contoso-testBaseAccount1")
            .withTags(mapOf())
            .withApplicationName("contoso-package2")
            .withVersion("1.0.0")
            .withTargetOSList(
                Arrays
                    .asList(
                        new TargetOSInfo()
                            .withOsUpdateType("Security updates")
                            .withTargetOSs(Arrays.asList("Windows 10 2004", "Windows 10 1903"))))
            .withFlightingRing("Insider Beta Channel")
            .withBlobPath("storageAccountPath/package.zip")
            .withTests(
                Arrays
                    .asList(
                        new Test()
                            .withTestType(TestType.OUT_OF_BOX_TEST)
                            .withIsActive(true)
                            .withCommands(
                                Arrays
                                    .asList(
                                        new Command()
                                            .withName("Install")
                                            .withAction(Action.INSTALL)
                                            .withContentType(ContentType.PATH)
                                            .withContent("app/scripts/install/job.ps1")
                                            .withRunElevated(true)
                                            .withRestartAfter(true)
                                            .withMaxRunTime(1800)
                                            .withRunAsInteractive(true)
                                            .withAlwaysRun(true)
                                            .withApplyUpdateBefore(false),
                                        new Command()
                                            .withName("Launch")
                                            .withAction(Action.LAUNCH)
                                            .withContentType(ContentType.PATH)
                                            .withContent("app/scripts/launch/job.ps1")
                                            .withRunElevated(true)
                                            .withRestartAfter(false)
                                            .withMaxRunTime(1800)
                                            .withRunAsInteractive(true)
                                            .withAlwaysRun(false)
                                            .withApplyUpdateBefore(true),
                                        new Command()
                                            .withName("Close")
                                            .withAction(Action.CLOSE)
                                            .withContentType(ContentType.PATH)
                                            .withContent("app/scripts/close/job.ps1")
                                            .withRunElevated(true)
                                            .withRestartAfter(false)
                                            .withMaxRunTime(1800)
                                            .withRunAsInteractive(true)
                                            .withAlwaysRun(false)
                                            .withApplyUpdateBefore(false),
                                        new Command()
                                            .withName("Uninstall")
                                            .withAction(Action.UNINSTALL)
                                            .withContentType(ContentType.PATH)
                                            .withContent("app/scripts/uninstall/job.ps1")
                                            .withRunElevated(true)
                                            .withRestartAfter(false)
                                            .withMaxRunTime(1800)
                                            .withRunAsInteractive(true)
                                            .withAlwaysRun(true)
                                            .withApplyUpdateBefore(false)))))
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

### Packages_Delete

```java
import com.azure.core.util.Context;

/** Samples for Packages Delete. */
public final class PackagesDeleteSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/PackageDelete.json
     */
    /**
     * Sample code: PackageDelete.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void packageDelete(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager.packages().delete("contoso-rg1", "contoso-testBaseAccount1", "contoso-package2", Context.NONE);
    }
}
```

### Packages_Get

```java
import com.azure.core.util.Context;

/** Samples for Packages Get. */
public final class PackagesGetSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/PackageGet.json
     */
    /**
     * Sample code: PackageGet.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void packageGet(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager.packages().getWithResponse("contoso-rg1", "contoso-testBaseAccount1", "contoso-package2", Context.NONE);
    }
}
```

### Packages_GetDownloadUrl

```java
import com.azure.core.util.Context;

/** Samples for Packages GetDownloadUrl. */
public final class PackagesGetDownloadUrlSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/PackageGetDownloadURL.json
     */
    /**
     * Sample code: PackageGetDownloadURL.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void packageGetDownloadURL(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .packages()
            .getDownloadUrlWithResponse("contoso-rg1", "contoso-testBaseAccount1", "contoso-package2", Context.NONE);
    }
}
```

### Packages_HardDelete

```java
import com.azure.core.util.Context;

/** Samples for Packages HardDelete. */
public final class PackagesHardDeleteSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/PackageHardDelete.json
     */
    /**
     * Sample code: PackageHardDelete.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void packageHardDelete(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager.packages().hardDelete("contoso-rg1", "contoso-testBaseAccount1", "contoso-package2", Context.NONE);
    }
}
```

### Packages_ListByTestBaseAccount

```java
import com.azure.core.util.Context;

/** Samples for Packages ListByTestBaseAccount. */
public final class PackagesListByTestBaseAccountSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/PackagesList.json
     */
    /**
     * Sample code: PackagesList.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void packagesList(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager.packages().listByTestBaseAccount("contoso-rg1", "contoso-testBaseAccount1", Context.NONE);
    }
}
```

### Packages_RunTest

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.testbase.models.OsUpdateType;
import com.azure.resourcemanager.testbase.models.PackageRunTestParameters;
import com.azure.resourcemanager.testbase.models.TestType;

/** Samples for Packages RunTest. */
public final class PackagesRunTestSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/PackageRunTest.json
     */
    /**
     * Sample code: PackageRunTest.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void packageRunTest(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .packages()
            .runTestWithResponse(
                "contoso-rg1",
                "contoso-testBaseAccount1",
                "contoso-package2",
                new PackageRunTestParameters()
                    .withTestType(TestType.OUT_OF_BOX_TEST)
                    .withOsUpdateType(OsUpdateType.SECURITY_UPDATE)
                    .withOsName("Windows 10 21H1")
                    .withReleaseName("2021.05 B"),
                Context.NONE);
    }
}
```

### Packages_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.testbase.models.Action;
import com.azure.resourcemanager.testbase.models.Command;
import com.azure.resourcemanager.testbase.models.ContentType;
import com.azure.resourcemanager.testbase.models.PackageResource;
import com.azure.resourcemanager.testbase.models.TargetOSInfo;
import com.azure.resourcemanager.testbase.models.Test;
import com.azure.resourcemanager.testbase.models.TestType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Packages Update. */
public final class PackagesUpdateSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/PackageUpdate.json
     */
    /**
     * Sample code: PackageUpdate.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void packageUpdate(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        PackageResource resource =
            manager
                .packages()
                .getWithResponse("contoso-rg1", "contoso-testBaseAccount1", "contoso-package2", Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf())
            .withTargetOSList(
                Arrays
                    .asList(
                        new TargetOSInfo()
                            .withOsUpdateType("Security updates")
                            .withTargetOSs(Arrays.asList("Windows 10 2004", "Windows 10 1903"))))
            .withFlightingRing("Insider Beta Channel")
            .withIsEnabled(false)
            .withBlobPath("storageAccountPath/package.zip")
            .withTests(
                Arrays
                    .asList(
                        new Test()
                            .withTestType(TestType.OUT_OF_BOX_TEST)
                            .withIsActive(true)
                            .withCommands(
                                Arrays
                                    .asList(
                                        new Command()
                                            .withName("Install")
                                            .withAction(Action.INSTALL)
                                            .withContentType(ContentType.PATH)
                                            .withContent("app/scripts/install/job.ps1")
                                            .withRunElevated(true)
                                            .withRestartAfter(true)
                                            .withMaxRunTime(1800)
                                            .withRunAsInteractive(true)
                                            .withAlwaysRun(true)
                                            .withApplyUpdateBefore(false),
                                        new Command()
                                            .withName("Launch")
                                            .withAction(Action.LAUNCH)
                                            .withContentType(ContentType.PATH)
                                            .withContent("app/scripts/launch/job.ps1")
                                            .withRunElevated(true)
                                            .withRestartAfter(false)
                                            .withMaxRunTime(1800)
                                            .withRunAsInteractive(true)
                                            .withAlwaysRun(false)
                                            .withApplyUpdateBefore(true),
                                        new Command()
                                            .withName("Close")
                                            .withAction(Action.CLOSE)
                                            .withContentType(ContentType.PATH)
                                            .withContent("app/scripts/close/job.ps1")
                                            .withRunElevated(true)
                                            .withRestartAfter(false)
                                            .withMaxRunTime(1800)
                                            .withRunAsInteractive(true)
                                            .withAlwaysRun(false)
                                            .withApplyUpdateBefore(false),
                                        new Command()
                                            .withName("Uninstall")
                                            .withAction(Action.UNINSTALL)
                                            .withContentType(ContentType.PATH)
                                            .withContent("app/scripts/uninstall/job.ps1")
                                            .withRunElevated(true)
                                            .withRestartAfter(false)
                                            .withMaxRunTime(1800)
                                            .withRunAsInteractive(true)
                                            .withAlwaysRun(true)
                                            .withApplyUpdateBefore(false)))))
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

### Skus_List

```java
import com.azure.core.util.Context;

/** Samples for Skus List. */
public final class SkusListSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/TestBaseAccountSKUsList.json
     */
    /**
     * Sample code: TestBaseAccountSKUsList.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void testBaseAccountSKUsList(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager.skus().list(Context.NONE);
    }
}
```

### TestBaseAccounts_CheckPackageNameAvailability

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.testbase.models.PackageCheckNameAvailabilityParameters;

/** Samples for TestBaseAccounts CheckPackageNameAvailability. */
public final class TestBaseAccountsCheckPackageNameAvailabilitySamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/CheckPackageNameAvailability.json
     */
    /**
     * Sample code: CheckPackageNameAvailability.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void checkPackageNameAvailability(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .testBaseAccounts()
            .checkPackageNameAvailabilityWithResponse(
                "contoso-rg1",
                "contoso-testBaseAccount1",
                new PackageCheckNameAvailabilityParameters()
                    .withName("testApp")
                    .withApplicationName("testApp")
                    .withVersion("1.0.0")
                    .withType("Microsoft.TestBase/testBaseAccounts/packages"),
                Context.NONE);
    }
}
```

### TestBaseAccounts_Create

```java
import com.azure.resourcemanager.testbase.fluent.models.TestBaseAccountSkuInner;
import com.azure.resourcemanager.testbase.models.Tier;

/** Samples for TestBaseAccounts Create. */
public final class TestBaseAccountsCreateSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/TestBaseAccountCreate.json
     */
    /**
     * Sample code: TestBaseAccountCreate.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void testBaseAccountCreate(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .testBaseAccounts()
            .define("contoso-testBaseAccount1")
            .withRegion("westus")
            .withExistingResourceGroup("contoso-rg1")
            .withSku(new TestBaseAccountSkuInner().withName("S0").withTier(Tier.STANDARD))
            .create();
    }
}
```

### TestBaseAccounts_Delete

```java
import com.azure.core.util.Context;

/** Samples for TestBaseAccounts Delete. */
public final class TestBaseAccountsDeleteSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/TestBaseAccountDelete.json
     */
    /**
     * Sample code: TestBaseAccountDelete.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void testBaseAccountDelete(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager.testBaseAccounts().delete("contoso-rg1", "contoso-testBaseAccount1", Context.NONE);
    }
}
```

### TestBaseAccounts_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for TestBaseAccounts GetByResourceGroup. */
public final class TestBaseAccountsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/TestBaseAccountGet.json
     */
    /**
     * Sample code: TestBaseAccountGet.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void testBaseAccountGet(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .testBaseAccounts()
            .getByResourceGroupWithResponse("contoso-rg1", "contoso-testBaseAccount1", Context.NONE);
    }
}
```

### TestBaseAccounts_GetFileUploadUrl

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.testbase.models.GetFileUploadUrlParameters;

/** Samples for TestBaseAccounts GetFileUploadUrl. */
public final class TestBaseAccountsGetFileUploadUrlSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/TestBaseAccountGetFileUploadUrl.json
     */
    /**
     * Sample code: TestBaseAccountGetFileUploadUrl.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void testBaseAccountGetFileUploadUrl(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .testBaseAccounts()
            .getFileUploadUrlWithResponse(
                "contoso-rg1",
                "contoso-testBaseAccount1",
                new GetFileUploadUrlParameters().withBlobName("package.zip"),
                Context.NONE);
    }
}
```

### TestBaseAccounts_List

```java
import com.azure.core.util.Context;

/** Samples for TestBaseAccounts List. */
public final class TestBaseAccountsListSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/TestBaseAccountsListBySubscription.json
     */
    /**
     * Sample code: TestBaseAccountsListBySubscription.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void testBaseAccountsListBySubscription(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager.testBaseAccounts().list(null, Context.NONE);
    }
}
```

### TestBaseAccounts_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for TestBaseAccounts ListByResourceGroup. */
public final class TestBaseAccountsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/TestBaseAccountsList.json
     */
    /**
     * Sample code: TestBaseAccountsList.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void testBaseAccountsList(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager.testBaseAccounts().listByResourceGroup("contoso-rg1", null, Context.NONE);
    }
}
```

### TestBaseAccounts_Offboard

```java
import com.azure.core.util.Context;

/** Samples for TestBaseAccounts Offboard. */
public final class TestBaseAccountsOffboardSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/TestBaseAccountOffboard.json
     */
    /**
     * Sample code: TestBaseAccountOffboard.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void testBaseAccountOffboard(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager.testBaseAccounts().offboard("contoso-rg1", "contoso-testBaseAccount1", Context.NONE);
    }
}
```

### TestBaseAccounts_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.testbase.fluent.models.TestBaseAccountSkuInner;
import com.azure.resourcemanager.testbase.models.TestBaseAccountResource;
import com.azure.resourcemanager.testbase.models.Tier;

/** Samples for TestBaseAccounts Update. */
public final class TestBaseAccountsUpdateSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/TestBaseAccountUpdate.json
     */
    /**
     * Sample code: TestBaseAccountUpdate.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void testBaseAccountUpdate(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        TestBaseAccountResource resource =
            manager
                .testBaseAccounts()
                .getByResourceGroupWithResponse("contoso-rg1", "contoso-testBaseAccount1", Context.NONE)
                .getValue();
        resource.update().withSku(new TestBaseAccountSkuInner().withName("S0").withTier(Tier.STANDARD)).apply();
    }
}
```

### TestResults_Get

```java
import com.azure.core.util.Context;

/** Samples for TestResults Get. */
public final class TestResultsGetSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/TestResultGet.json
     */
    /**
     * Sample code: TestResultGet.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void testResultGet(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .testResults()
            .getWithResponse(
                "contoso-rg1",
                "contoso-testBaseAccount1",
                "contoso-package2",
                "Windows-10-1909-99b1f80d-03a9-4148-997f-806ba5bac8e0",
                Context.NONE);
    }
}
```

### TestResults_GetConsoleLogDownloadUrl

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.testbase.models.TestResultConsoleLogDownloadUrlParameters;

/** Samples for TestResults GetConsoleLogDownloadUrl. */
public final class TestResultsGetConsoleLogDownloadUrlSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/TestResultGetConsoleLogDownloadURL.json
     */
    /**
     * Sample code: TestResultGetConsoleLogDownloadURL.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void testResultGetConsoleLogDownloadURL(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .testResults()
            .getConsoleLogDownloadUrlWithResponse(
                "contoso-rg1",
                "contoso-testBaseAccount1",
                "contoso-package2",
                "Windows-10-1909-99b1f80d-03a9-4148-997f-806ba5bac8e0",
                new TestResultConsoleLogDownloadUrlParameters().withLogFileName("launch-stderr-1234.log"),
                Context.NONE);
    }
}
```

### TestResults_GetDownloadUrl

```java
import com.azure.core.util.Context;

/** Samples for TestResults GetDownloadUrl. */
public final class TestResultsGetDownloadUrlSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/TestResultGetDownloadURL.json
     */
    /**
     * Sample code: TestResultGetDownloadURL.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void testResultGetDownloadURL(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .testResults()
            .getDownloadUrlWithResponse(
                "contoso-rg1",
                "contoso-testBaseAccount1",
                "contoso-package2",
                "Windows-10-1909-99b1f80d-03a9-4148-997f-806ba5bac8e0",
                Context.NONE);
    }
}
```

### TestResults_GetVideoDownloadUrl

```java
import com.azure.core.util.Context;

/** Samples for TestResults GetVideoDownloadUrl. */
public final class TestResultsGetVideoDownloadUrlSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/TestResultGetVideoDownloadURL.json
     */
    /**
     * Sample code: TestResultGetVideoDownloadURL.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void testResultGetVideoDownloadURL(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .testResults()
            .getVideoDownloadUrlWithResponse(
                "contoso-rg1",
                "contoso-testBaseAccount1",
                "contoso-package2",
                "Windows-10-1909-99b1f80d-03a9-4148-997f-806ba5bac8e0",
                Context.NONE);
    }
}
```

### TestResults_List

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.testbase.models.OsUpdateType;

/** Samples for TestResults List. */
public final class TestResultsListSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/TestResultsList.json
     */
    /**
     * Sample code: TestResultsList.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void testResultsList(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .testResults()
            .list(
                "contoso-rg1",
                "contoso-testBaseAccount1",
                "contoso-package2",
                OsUpdateType.SECURITY_UPDATE,
                "osName eq 'Windows 10 2004' and releaseName eq '2020.11B'",
                Context.NONE);
    }
}
```

### TestSummaries_Get

```java
import com.azure.core.util.Context;

/** Samples for TestSummaries Get. */
public final class TestSummariesGetSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/TestSummaryGet.json
     */
    /**
     * Sample code: TestSummaryGet.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void testSummaryGet(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager
            .testSummaries()
            .getWithResponse(
                "contoso-rg1",
                "contoso-testBaseAccount1",
                "contoso-package2-096bffb5-5d3d-4305-a66a-953372ed6e88",
                Context.NONE);
    }
}
```

### TestSummaries_List

```java
import com.azure.core.util.Context;

/** Samples for TestSummaries List. */
public final class TestSummariesListSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/TestSummariesList.json
     */
    /**
     * Sample code: TestSummariesList.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void testSummariesList(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager.testSummaries().list("contoso-rg1", "contoso-testBaseAccount1", Context.NONE);
    }
}
```

### TestTypes_Get

```java
import com.azure.core.util.Context;

/** Samples for TestTypes Get. */
public final class TestTypesGetSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/TestTypeGet.json
     */
    /**
     * Sample code: TestTypeGet.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void testTypeGet(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager.testTypes().getWithResponse("contoso-rg", "contoso-testBaseAccount", "Functional-Test", Context.NONE);
    }
}
```

### TestTypes_List

```java
import com.azure.core.util.Context;

/** Samples for TestTypes List. */
public final class TestTypesListSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/TestTypesList.json
     */
    /**
     * Sample code: TestTypesList.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void testTypesList(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager.testTypes().list("contoso-rg", "contoso-testBaseAccount", Context.NONE);
    }
}
```

### Usage_List

```java
import com.azure.core.util.Context;

/** Samples for Usage List. */
public final class UsageListSamples {
    /*
     * x-ms-original-file: specification/testbase/resource-manager/Microsoft.TestBase/preview/2022-04-01-preview/examples/TestBaseAccountUsagesList.json
     */
    /**
     * Sample code: TestBaseAccountUsagesList.
     *
     * @param manager Entry point to TestBaseManager.
     */
    public static void testBaseAccountUsagesList(com.azure.resourcemanager.testbase.TestBaseManager manager) {
        manager.usages().list("contoso-rg1", "contoso-testBaseAccount1", null, Context.NONE);
    }
}
```

