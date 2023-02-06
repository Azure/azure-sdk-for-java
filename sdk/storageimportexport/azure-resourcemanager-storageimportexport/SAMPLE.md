# Code snippets and samples


## BitLockerKeys

- [List](#bitlockerkeys_list)

## Jobs

- [Create](#jobs_create)
- [Delete](#jobs_delete)
- [GetByResourceGroup](#jobs_getbyresourcegroup)
- [List](#jobs_list)
- [ListByResourceGroup](#jobs_listbyresourcegroup)
- [Update](#jobs_update)

## Locations

- [Get](#locations_get)
- [List](#locations_list)

## Operations

- [List](#operations_list)
### BitLockerKeys_List

```java
/** Samples for BitLockerKeys List. */
public final class BitLockerKeysListSamples {
    /*
     * x-ms-original-file: specification/storageimportexport/resource-manager/Microsoft.ImportExport/preview/2021-01-01/examples/ListBitLockerKeys.json
     */
    /**
     * Sample code: List BitLocker Keys for drives in a job.
     *
     * @param manager Entry point to StorageImportExportManager.
     */
    public static void listBitLockerKeysForDrivesInAJob(
        com.azure.resourcemanager.storageimportexport.StorageImportExportManager manager) {
        manager.bitLockerKeys().list("myJob", "myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### Jobs_Create

```java
import com.azure.resourcemanager.storageimportexport.models.DriveStatus;
import com.azure.resourcemanager.storageimportexport.models.Export;
import com.azure.resourcemanager.storageimportexport.models.JobDetails;
import com.azure.resourcemanager.storageimportexport.models.ReturnAddress;
import com.azure.resourcemanager.storageimportexport.models.ReturnShipping;
import java.util.Arrays;

/** Samples for Jobs Create. */
public final class JobsCreateSamples {
    /*
     * x-ms-original-file: specification/storageimportexport/resource-manager/Microsoft.ImportExport/preview/2021-01-01/examples/CreateJob.json
     */
    /**
     * Sample code: Create import job.
     *
     * @param manager Entry point to StorageImportExportManager.
     */
    public static void createImportJob(
        com.azure.resourcemanager.storageimportexport.StorageImportExportManager manager) {
        manager
            .jobs()
            .define("myJob")
            .withExistingResourceGroup("myResourceGroup")
            .withRegion("West US")
            .withProperties(
                new JobDetails()
                    .withStorageAccountId(
                        "/subscriptions/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx/resourceGroups/myResourceGroup/providers/Microsoft.ClassicStorage/storageAccounts/test")
                    .withJobType("Import")
                    .withReturnAddress(
                        new ReturnAddress()
                            .withRecipientName("Test")
                            .withStreetAddress1("Street1")
                            .withStreetAddress2("street2")
                            .withCity("Redmond")
                            .withStateOrProvince("wa")
                            .withPostalCode("fakeTokenPlaceholder")
                            .withCountryOrRegion("USA")
                            .withPhone("4250000000")
                            .withEmail("Test@contoso.com"))
                    .withReturnShipping(
                        new ReturnShipping().withCarrierName("FedEx").withCarrierAccountNumber("989ffff"))
                    .withDiagnosticsPath("waimportexport")
                    .withLogLevel("Verbose")
                    .withBackupDriveManifest(true)
                    .withDriveList(
                        Arrays
                            .asList(
                                new DriveStatus()
                                    .withDriveId("9CA995BB")
                                    .withBitLockerKey("fakeTokenPlaceholder")
                                    .withManifestFile("\\8a0c23f7-14b7-470a-9633-fcd46590a1bc.manifest")
                                    .withManifestHash(
                                        "4228EC5D8E048CB9B515338C789314BE8D0B2FDBC7C7A0308E1C826242CDE74E")
                                    .withDriveHeaderHash(
                                        "0:1048576:FB6B6ED500D49DA6E0D723C98D42C657F2881CC13357C28DCECA6A524F1292501571A321238540E621AB5BD9C9A32637615919A75593E6CB5C1515DAE341CABF;135266304:143360:C957A189AFC38C4E80731252301EB91427CE55E61448FA3C73C6FDDE70ABBC197947EC8D0249A2C639BB10B95957D5820A4BE8DFBBF76FFFA688AE5CE0D42EC3"))))
            .create();
    }

    /*
     * x-ms-original-file: specification/storageimportexport/resource-manager/Microsoft.ImportExport/preview/2021-01-01/examples/CreateExportJob.json
     */
    /**
     * Sample code: Create export job.
     *
     * @param manager Entry point to StorageImportExportManager.
     */
    public static void createExportJob(
        com.azure.resourcemanager.storageimportexport.StorageImportExportManager manager) {
        manager
            .jobs()
            .define("myExportJob")
            .withExistingResourceGroup("myResourceGroup")
            .withRegion("West US")
            .withProperties(
                new JobDetails()
                    .withStorageAccountId(
                        "/subscriptions/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx/resourceGroups/myResourceGroup/providers/Microsoft.ClassicStorage/storageAccounts/test")
                    .withJobType("Export")
                    .withReturnAddress(
                        new ReturnAddress()
                            .withRecipientName("Test")
                            .withStreetAddress1("Street1")
                            .withStreetAddress2("street2")
                            .withCity("Redmond")
                            .withStateOrProvince("wa")
                            .withPostalCode("fakeTokenPlaceholder")
                            .withCountryOrRegion("USA")
                            .withPhone("4250000000")
                            .withEmail("Test@contoso.com"))
                    .withReturnShipping(
                        new ReturnShipping().withCarrierName("FedEx").withCarrierAccountNumber("989ffff"))
                    .withDiagnosticsPath("waimportexport")
                    .withLogLevel("Verbose")
                    .withBackupDriveManifest(true)
                    .withExport(new Export().withBlobPathPrefix(Arrays.asList("/"))))
            .create();
    }
}
```

### Jobs_Delete

```java
/** Samples for Jobs Delete. */
public final class JobsDeleteSamples {
    /*
     * x-ms-original-file: specification/storageimportexport/resource-manager/Microsoft.ImportExport/preview/2021-01-01/examples/DeleteJob.json
     */
    /**
     * Sample code: Delete job.
     *
     * @param manager Entry point to StorageImportExportManager.
     */
    public static void deleteJob(com.azure.resourcemanager.storageimportexport.StorageImportExportManager manager) {
        manager.jobs().deleteByResourceGroupWithResponse("myResourceGroup", "myJob", com.azure.core.util.Context.NONE);
    }
}
```

### Jobs_GetByResourceGroup

```java
/** Samples for Jobs GetByResourceGroup. */
public final class JobsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/storageimportexport/resource-manager/Microsoft.ImportExport/preview/2021-01-01/examples/GetExportJob.json
     */
    /**
     * Sample code: Get export job.
     *
     * @param manager Entry point to StorageImportExportManager.
     */
    public static void getExportJob(com.azure.resourcemanager.storageimportexport.StorageImportExportManager manager) {
        manager.jobs().getByResourceGroupWithResponse("myResourceGroup", "myJob", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/storageimportexport/resource-manager/Microsoft.ImportExport/preview/2021-01-01/examples/GetJob.json
     */
    /**
     * Sample code: Get import job.
     *
     * @param manager Entry point to StorageImportExportManager.
     */
    public static void getImportJob(com.azure.resourcemanager.storageimportexport.StorageImportExportManager manager) {
        manager.jobs().getByResourceGroupWithResponse("myResourceGroup", "myJob", com.azure.core.util.Context.NONE);
    }
}
```

### Jobs_List

```java
/** Samples for Jobs List. */
public final class JobsListSamples {
    /*
     * x-ms-original-file: specification/storageimportexport/resource-manager/Microsoft.ImportExport/preview/2021-01-01/examples/ListJobsInSubscription.json
     */
    /**
     * Sample code: List jobs in a subscription.
     *
     * @param manager Entry point to StorageImportExportManager.
     */
    public static void listJobsInASubscription(
        com.azure.resourcemanager.storageimportexport.StorageImportExportManager manager) {
        manager.jobs().list(null, null, com.azure.core.util.Context.NONE);
    }
}
```

### Jobs_ListByResourceGroup

```java
/** Samples for Jobs ListByResourceGroup. */
public final class JobsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/storageimportexport/resource-manager/Microsoft.ImportExport/preview/2021-01-01/examples/ListJobsInResourceGroup.json
     */
    /**
     * Sample code: List jobs in a resource group.
     *
     * @param manager Entry point to StorageImportExportManager.
     */
    public static void listJobsInAResourceGroup(
        com.azure.resourcemanager.storageimportexport.StorageImportExportManager manager) {
        manager.jobs().listByResourceGroup("myResourceGroup", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### Jobs_Update

```java
import com.azure.resourcemanager.storageimportexport.models.JobResponse;

/** Samples for Jobs Update. */
public final class JobsUpdateSamples {
    /*
     * x-ms-original-file: specification/storageimportexport/resource-manager/Microsoft.ImportExport/preview/2021-01-01/examples/UpdateExportJob.json
     */
    /**
     * Sample code: Update export job.
     *
     * @param manager Entry point to StorageImportExportManager.
     */
    public static void updateExportJob(
        com.azure.resourcemanager.storageimportexport.StorageImportExportManager manager) {
        JobResponse resource =
            manager
                .jobs()
                .getByResourceGroupWithResponse("myResourceGroup", "myExportJob", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withState("").withLogLevel("Verbose").withBackupDriveManifest(true).apply();
    }

    /*
     * x-ms-original-file: specification/storageimportexport/resource-manager/Microsoft.ImportExport/preview/2021-01-01/examples/UpdateJob.json
     */
    /**
     * Sample code: Update import job.
     *
     * @param manager Entry point to StorageImportExportManager.
     */
    public static void updateImportJob(
        com.azure.resourcemanager.storageimportexport.StorageImportExportManager manager) {
        JobResponse resource =
            manager
                .jobs()
                .getByResourceGroupWithResponse("myResourceGroup", "myJob", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withState("").withLogLevel("Verbose").withBackupDriveManifest(true).apply();
    }
}
```

### Locations_Get

```java
/** Samples for Locations Get. */
public final class LocationsGetSamples {
    /*
     * x-ms-original-file: specification/storageimportexport/resource-manager/Microsoft.ImportExport/preview/2021-01-01/examples/GetLocation.json
     */
    /**
     * Sample code: Get locations.
     *
     * @param manager Entry point to StorageImportExportManager.
     */
    public static void getLocations(com.azure.resourcemanager.storageimportexport.StorageImportExportManager manager) {
        manager.locations().getWithResponse("West US", com.azure.core.util.Context.NONE);
    }
}
```

### Locations_List

```java
/** Samples for Locations List. */
public final class LocationsListSamples {
    /*
     * x-ms-original-file: specification/storageimportexport/resource-manager/Microsoft.ImportExport/preview/2021-01-01/examples/ListLocations.json
     */
    /**
     * Sample code: List locations.
     *
     * @param manager Entry point to StorageImportExportManager.
     */
    public static void listLocations(com.azure.resourcemanager.storageimportexport.StorageImportExportManager manager) {
        manager.locations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/storageimportexport/resource-manager/Microsoft.ImportExport/preview/2021-01-01/examples/ListOperations.json
     */
    /**
     * Sample code: List available operations.
     *
     * @param manager Entry point to StorageImportExportManager.
     */
    public static void listAvailableOperations(
        com.azure.resourcemanager.storageimportexport.StorageImportExportManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

