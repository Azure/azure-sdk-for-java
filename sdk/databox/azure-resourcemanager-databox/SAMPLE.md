# Code snippets and samples


## Jobs

- [BookShipmentPickUp](#jobs_bookshipmentpickup)
- [Cancel](#jobs_cancel)
- [Create](#jobs_create)
- [Delete](#jobs_delete)
- [GetByResourceGroup](#jobs_getbyresourcegroup)
- [List](#jobs_list)
- [ListByResourceGroup](#jobs_listbyresourcegroup)
- [ListCredentials](#jobs_listcredentials)
- [MarkDevicesShipped](#jobs_markdevicesshipped)
- [Update](#jobs_update)

## Operations

- [List](#operations_list)

## ResourceProvider

- [Mitigate](#resourceprovider_mitigate)

## Service

- [ListAvailableSkusByResourceGroup](#service_listavailableskusbyresourcegroup)
- [RegionConfiguration](#service_regionconfiguration)
- [RegionConfigurationByResourceGroup](#service_regionconfigurationbyresourcegroup)
- [ValidateAddress](#service_validateaddress)
- [ValidateInputs](#service_validateinputs)
- [ValidateInputsByResourceGroup](#service_validateinputsbyresourcegroup)
### Jobs_BookShipmentPickUp

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.databox.models.ShipmentPickUpRequest;
import java.time.OffsetDateTime;

/** Samples for Jobs BookShipmentPickUp. */
public final class JobsBookShipmentPickUpSamples {
    /*
     * x-ms-original-file: specification/databox/resource-manager/Microsoft.DataBox/stable/2022-02-01/examples/BookShipmentPickupPost.json
     */
    /**
     * Sample code: BookShipmentPickupPost.
     *
     * @param manager Entry point to DataBoxManager.
     */
    public static void bookShipmentPickupPost(com.azure.resourcemanager.databox.DataBoxManager manager) {
        manager
            .jobs()
            .bookShipmentPickUpWithResponse(
                "bvttoolrg6",
                "TJ-636646322037905056",
                new ShipmentPickUpRequest()
                    .withStartTime(OffsetDateTime.parse("2019-09-20T18:30:00Z"))
                    .withEndTime(OffsetDateTime.parse("2019-09-22T18:30:00Z"))
                    .withShipmentLocation("Front desk"),
                Context.NONE);
    }
}
```

### Jobs_Cancel

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.databox.models.CancellationReason;

/** Samples for Jobs Cancel. */
public final class JobsCancelSamples {
    /*
     * x-ms-original-file: specification/databox/resource-manager/Microsoft.DataBox/stable/2022-02-01/examples/JobsCancelPost.json
     */
    /**
     * Sample code: JobsCancelPost.
     *
     * @param manager Entry point to DataBoxManager.
     */
    public static void jobsCancelPost(com.azure.resourcemanager.databox.DataBoxManager manager) {
        manager
            .jobs()
            .cancelWithResponse(
                "SdkRg5154", "SdkJob952", new CancellationReason().withReason("CancelTest"), Context.NONE);
    }
}
```

### Jobs_Create

```java
import com.azure.resourcemanager.databox.models.AddressType;
import com.azure.resourcemanager.databox.models.ContactDetails;
import com.azure.resourcemanager.databox.models.DataAccountType;
import com.azure.resourcemanager.databox.models.DataBoxJobDetails;
import com.azure.resourcemanager.databox.models.DataExportDetails;
import com.azure.resourcemanager.databox.models.DataImportDetails;
import com.azure.resourcemanager.databox.models.DoubleEncryption;
import com.azure.resourcemanager.databox.models.EncryptionPreferences;
import com.azure.resourcemanager.databox.models.Preferences;
import com.azure.resourcemanager.databox.models.ResourceIdentity;
import com.azure.resourcemanager.databox.models.ShippingAddress;
import com.azure.resourcemanager.databox.models.Sku;
import com.azure.resourcemanager.databox.models.SkuName;
import com.azure.resourcemanager.databox.models.StorageAccountDetails;
import com.azure.resourcemanager.databox.models.TransferAllDetails;
import com.azure.resourcemanager.databox.models.TransferConfiguration;
import com.azure.resourcemanager.databox.models.TransferConfigurationTransferAllDetails;
import com.azure.resourcemanager.databox.models.TransferConfigurationType;
import com.azure.resourcemanager.databox.models.TransferType;
import com.azure.resourcemanager.databox.models.UserAssignedIdentity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Jobs Create. */
public final class JobsCreateSamples {
    /*
     * x-ms-original-file: specification/databox/resource-manager/Microsoft.DataBox/stable/2022-02-01/examples/JobsCreateDevicePassword.json
     */
    /**
     * Sample code: JobsCreateDevicePassword.
     *
     * @param manager Entry point to DataBoxManager.
     */
    public static void jobsCreateDevicePassword(com.azure.resourcemanager.databox.DataBoxManager manager) {
        manager
            .jobs()
            .define("SdkJob9640")
            .withRegion("westus")
            .withExistingResourceGroup("SdkRg7478")
            .withSku(new Sku().withName(SkuName.DATA_BOX))
            .withTransferType(TransferType.IMPORT_TO_AZURE)
            .withDetails(
                new DataBoxJobDetails()
                    .withContactDetails(
                        new ContactDetails()
                            .withContactName("Public SDK Test")
                            .withPhone("1234567890")
                            .withPhoneExtension("1234")
                            .withEmailList(Arrays.asList("testing@microsoft.com")))
                    .withShippingAddress(
                        new ShippingAddress()
                            .withStreetAddress1("16 TOWNSEND ST")
                            .withStreetAddress2("Unit 1")
                            .withCity("San Francisco")
                            .withStateOrProvince("CA")
                            .withCountry("US")
                            .withPostalCode("94107")
                            .withCompanyName("Microsoft")
                            .withAddressType(AddressType.COMMERCIAL))
                    .withDataImportDetails(
                        Arrays
                            .asList(
                                new DataImportDetails()
                                    .withAccountDetails(
                                        new StorageAccountDetails()
                                            .withSharePassword("<sharePassword>")
                                            .withStorageAccountId(
                                                "/subscriptions/fa68082f-8ff7-4a25-95c7-ce9da541242f/resourceGroups/databoxbvt1/providers/Microsoft.Storage/storageAccounts/databoxbvttestaccount2"))))
                    .withDevicePassword("<devicePassword>"))
            .create();
    }

    /*
     * x-ms-original-file: specification/databox/resource-manager/Microsoft.DataBox/stable/2022-02-01/examples/JobsCreate.json
     */
    /**
     * Sample code: JobsCreate.
     *
     * @param manager Entry point to DataBoxManager.
     */
    public static void jobsCreate(com.azure.resourcemanager.databox.DataBoxManager manager) {
        manager
            .jobs()
            .define("SdkJob952")
            .withRegion("westus")
            .withExistingResourceGroup("SdkRg5154")
            .withSku(new Sku().withName(SkuName.DATA_BOX))
            .withTransferType(TransferType.IMPORT_TO_AZURE)
            .withDetails(
                new DataBoxJobDetails()
                    .withContactDetails(
                        new ContactDetails()
                            .withContactName("Public SDK Test")
                            .withPhone("1234567890")
                            .withPhoneExtension("1234")
                            .withEmailList(Arrays.asList("testing@microsoft.com")))
                    .withShippingAddress(
                        new ShippingAddress()
                            .withStreetAddress1("16 TOWNSEND ST")
                            .withStreetAddress2("Unit 1")
                            .withCity("San Francisco")
                            .withStateOrProvince("CA")
                            .withCountry("US")
                            .withPostalCode("94107")
                            .withCompanyName("Microsoft")
                            .withAddressType(AddressType.COMMERCIAL))
                    .withDataImportDetails(
                        Arrays
                            .asList(
                                new DataImportDetails()
                                    .withAccountDetails(
                                        new StorageAccountDetails()
                                            .withStorageAccountId(
                                                "/subscriptions/fa68082f-8ff7-4a25-95c7-ce9da541242f/resourcegroups/databoxbvt/providers/Microsoft.Storage/storageAccounts/databoxbvttestaccount")))))
            .create();
    }

    /*
     * x-ms-original-file: specification/databox/resource-manager/Microsoft.DataBox/stable/2022-02-01/examples/JobsCreateDoubleEncryption.json
     */
    /**
     * Sample code: JobsCreateDoubleEncryption.
     *
     * @param manager Entry point to DataBoxManager.
     */
    public static void jobsCreateDoubleEncryption(com.azure.resourcemanager.databox.DataBoxManager manager) {
        manager
            .jobs()
            .define("SdkJob6599")
            .withRegion("westus")
            .withExistingResourceGroup("SdkRg608")
            .withSku(new Sku().withName(SkuName.DATA_BOX))
            .withTransferType(TransferType.IMPORT_TO_AZURE)
            .withDetails(
                new DataBoxJobDetails()
                    .withContactDetails(
                        new ContactDetails()
                            .withContactName("Public SDK Test")
                            .withPhone("1234567890")
                            .withPhoneExtension("1234")
                            .withEmailList(Arrays.asList("testing@microsoft.com")))
                    .withShippingAddress(
                        new ShippingAddress()
                            .withStreetAddress1("16 TOWNSEND ST")
                            .withStreetAddress2("Unit 1")
                            .withCity("San Francisco")
                            .withStateOrProvince("CA")
                            .withCountry("US")
                            .withPostalCode("94107")
                            .withCompanyName("Microsoft")
                            .withAddressType(AddressType.COMMERCIAL))
                    .withDataImportDetails(
                        Arrays
                            .asList(
                                new DataImportDetails()
                                    .withAccountDetails(
                                        new StorageAccountDetails()
                                            .withStorageAccountId(
                                                "/subscriptions/fa68082f-8ff7-4a25-95c7-ce9da541242f/resourcegroups/databoxbvt/providers/Microsoft.Storage/storageAccounts/databoxbvttestaccount"))))
                    .withPreferences(
                        new Preferences()
                            .withEncryptionPreferences(
                                new EncryptionPreferences().withDoubleEncryption(DoubleEncryption.ENABLED))))
            .create();
    }

    /*
     * x-ms-original-file: specification/databox/resource-manager/Microsoft.DataBox/stable/2022-02-01/examples/JobsCreateWithUserAssignedIdentity.json
     */
    /**
     * Sample code: JobsCreateWithUserAssignedIdentity.
     *
     * @param manager Entry point to DataBoxManager.
     */
    public static void jobsCreateWithUserAssignedIdentity(com.azure.resourcemanager.databox.DataBoxManager manager) {
        manager
            .jobs()
            .define("SdkJob5337")
            .withRegion("westus")
            .withExistingResourceGroup("SdkRg7552")
            .withSku(new Sku().withName(SkuName.DATA_BOX))
            .withTransferType(TransferType.IMPORT_TO_AZURE)
            .withIdentity(
                new ResourceIdentity()
                    .withType("UserAssigned")
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/fa68082f-8ff7-4a25-95c7-ce9da541242f/resourceGroups/akvenkat/providers/Microsoft.ManagedIdentity/userAssignedIdentities/sdkIdentity",
                            new UserAssignedIdentity())))
            .withDetails(
                new DataBoxJobDetails()
                    .withContactDetails(
                        new ContactDetails()
                            .withContactName("Public SDK Test")
                            .withPhone("1234567890")
                            .withPhoneExtension("1234")
                            .withEmailList(Arrays.asList("testing@microsoft.com")))
                    .withShippingAddress(
                        new ShippingAddress()
                            .withStreetAddress1("16 TOWNSEND ST")
                            .withStreetAddress2("Unit 1")
                            .withCity("San Francisco")
                            .withStateOrProvince("CA")
                            .withCountry("US")
                            .withPostalCode("94107")
                            .withCompanyName("Microsoft")
                            .withAddressType(AddressType.COMMERCIAL))
                    .withDataImportDetails(
                        Arrays
                            .asList(
                                new DataImportDetails()
                                    .withAccountDetails(
                                        new StorageAccountDetails()
                                            .withStorageAccountId(
                                                "/subscriptions/fa68082f-8ff7-4a25-95c7-ce9da541242f/resourceGroups/databoxbvt1/providers/Microsoft.Storage/storageAccounts/databoxbvttestaccount2")))))
            .create();
    }

    /*
     * x-ms-original-file: specification/databox/resource-manager/Microsoft.DataBox/stable/2022-02-01/examples/JobsCreateExport.json
     */
    /**
     * Sample code: JobsCreateExport.
     *
     * @param manager Entry point to DataBoxManager.
     */
    public static void jobsCreateExport(com.azure.resourcemanager.databox.DataBoxManager manager) {
        manager
            .jobs()
            .define("SdkJob6429")
            .withRegion("westus")
            .withExistingResourceGroup("SdkRg8091")
            .withSku(new Sku().withName(SkuName.DATA_BOX))
            .withTransferType(TransferType.EXPORT_FROM_AZURE)
            .withDetails(
                new DataBoxJobDetails()
                    .withContactDetails(
                        new ContactDetails()
                            .withContactName("Public SDK Test")
                            .withPhone("1234567890")
                            .withPhoneExtension("1234")
                            .withEmailList(Arrays.asList("testing@microsoft.com")))
                    .withShippingAddress(
                        new ShippingAddress()
                            .withStreetAddress1("16 TOWNSEND ST")
                            .withStreetAddress2("Unit 1")
                            .withCity("San Francisco")
                            .withStateOrProvince("CA")
                            .withCountry("US")
                            .withPostalCode("94107")
                            .withCompanyName("Microsoft")
                            .withAddressType(AddressType.COMMERCIAL))
                    .withDataExportDetails(
                        Arrays
                            .asList(
                                new DataExportDetails()
                                    .withTransferConfiguration(
                                        new TransferConfiguration()
                                            .withTransferConfigurationType(TransferConfigurationType.TRANSFER_ALL)
                                            .withTransferAllDetails(
                                                new TransferConfigurationTransferAllDetails()
                                                    .withInclude(
                                                        new TransferAllDetails()
                                                            .withDataAccountType(DataAccountType.STORAGE_ACCOUNT)
                                                            .withTransferAllBlobs(true)
                                                            .withTransferAllFiles(true))))
                                    .withAccountDetails(
                                        new StorageAccountDetails()
                                            .withStorageAccountId(
                                                "/subscriptions/fa68082f-8ff7-4a25-95c7-ce9da541242f/resourceGroups/akvenkat/providers/Microsoft.Storage/storageAccounts/aaaaaa2")))))
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

### Jobs_Delete

```java
import com.azure.core.util.Context;

/** Samples for Jobs Delete. */
public final class JobsDeleteSamples {
    /*
     * x-ms-original-file: specification/databox/resource-manager/Microsoft.DataBox/stable/2022-02-01/examples/JobsDelete.json
     */
    /**
     * Sample code: JobsDelete.
     *
     * @param manager Entry point to DataBoxManager.
     */
    public static void jobsDelete(com.azure.resourcemanager.databox.DataBoxManager manager) {
        manager.jobs().delete("SdkRg5154", "SdkJob952", Context.NONE);
    }
}
```

### Jobs_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Jobs GetByResourceGroup. */
public final class JobsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/databox/resource-manager/Microsoft.DataBox/stable/2022-02-01/examples/JobsGet.json
     */
    /**
     * Sample code: JobsGet.
     *
     * @param manager Entry point to DataBoxManager.
     */
    public static void jobsGet(com.azure.resourcemanager.databox.DataBoxManager manager) {
        manager.jobs().getByResourceGroupWithResponse("SdkRg5154", "SdkJob952", "details", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/databox/resource-manager/Microsoft.DataBox/stable/2022-02-01/examples/JobsGetCmk.json
     */
    /**
     * Sample code: JobsGetCmk.
     *
     * @param manager Entry point to DataBoxManager.
     */
    public static void jobsGetCmk(com.azure.resourcemanager.databox.DataBoxManager manager) {
        manager.jobs().getByResourceGroupWithResponse("SdkRg7937", "SdkJob1735", "details", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/databox/resource-manager/Microsoft.DataBox/stable/2022-02-01/examples/JobsGetExport.json
     */
    /**
     * Sample code: JobsGetExport.
     *
     * @param manager Entry point to DataBoxManager.
     */
    public static void jobsGetExport(com.azure.resourcemanager.databox.DataBoxManager manager) {
        manager.jobs().getByResourceGroupWithResponse("SdkRg8091", "SdkJob6429", "details", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/databox/resource-manager/Microsoft.DataBox/stable/2022-02-01/examples/JobsGetCopyStuck.json
     */
    /**
     * Sample code: JobsGetCopyStuck.
     *
     * @param manager Entry point to DataBoxManager.
     */
    public static void jobsGetCopyStuck(com.azure.resourcemanager.databox.DataBoxManager manager) {
        manager
            .jobs()
            .getByResourceGroupWithResponse("dmstestresource", "TJx-637505258985313014", "details", Context.NONE);
    }

    /*
     * x-ms-original-file: specification/databox/resource-manager/Microsoft.DataBox/stable/2022-02-01/examples/JobsGetWaitingForAction.json
     */
    /**
     * Sample code: JobsGetWaitingForAction.
     *
     * @param manager Entry point to DataBoxManager.
     */
    public static void jobsGetWaitingForAction(com.azure.resourcemanager.databox.DataBoxManager manager) {
        manager
            .jobs()
            .getByResourceGroupWithResponse("dmstestresource", "TJx-637505258985313014", "details", Context.NONE);
    }
}
```

### Jobs_List

```java
import com.azure.core.util.Context;

/** Samples for Jobs List. */
public final class JobsListSamples {
    /*
     * x-ms-original-file: specification/databox/resource-manager/Microsoft.DataBox/stable/2022-02-01/examples/JobsList.json
     */
    /**
     * Sample code: JobsList.
     *
     * @param manager Entry point to DataBoxManager.
     */
    public static void jobsList(com.azure.resourcemanager.databox.DataBoxManager manager) {
        manager.jobs().list(null, Context.NONE);
    }
}
```

### Jobs_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Jobs ListByResourceGroup. */
public final class JobsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/databox/resource-manager/Microsoft.DataBox/stable/2022-02-01/examples/JobsListByResourceGroup.json
     */
    /**
     * Sample code: JobsListByResourceGroup.
     *
     * @param manager Entry point to DataBoxManager.
     */
    public static void jobsListByResourceGroup(com.azure.resourcemanager.databox.DataBoxManager manager) {
        manager.jobs().listByResourceGroup("SdkRg5154", null, Context.NONE);
    }
}
```

### Jobs_ListCredentials

```java
import com.azure.core.util.Context;

/** Samples for Jobs ListCredentials. */
public final class JobsListCredentialsSamples {
    /*
     * x-ms-original-file: specification/databox/resource-manager/Microsoft.DataBox/stable/2022-02-01/examples/JobsListCredentials.json
     */
    /**
     * Sample code: JobsListCredentials.
     *
     * @param manager Entry point to DataBoxManager.
     */
    public static void jobsListCredentials(com.azure.resourcemanager.databox.DataBoxManager manager) {
        manager.jobs().listCredentials("bvttoolrg6", "TJ-636646322037905056", Context.NONE);
    }
}
```

### Jobs_MarkDevicesShipped

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.databox.models.MarkDevicesShippedRequest;
import com.azure.resourcemanager.databox.models.PackageCarrierInfo;

/** Samples for Jobs MarkDevicesShipped. */
public final class JobsMarkDevicesShippedSamples {
    /*
     * x-ms-original-file: specification/databox/resource-manager/Microsoft.DataBox/stable/2022-02-01/examples/MarkDevicesShipped.json
     */
    /**
     * Sample code: MarkDevicesShipped.
     *
     * @param manager Entry point to DataBoxManager.
     */
    public static void markDevicesShipped(com.azure.resourcemanager.databox.DataBoxManager manager) {
        manager
            .jobs()
            .markDevicesShippedWithResponse(
                "SdkJob8367",
                "SdkRg9836",
                new MarkDevicesShippedRequest()
                    .withDeliverToDcPackageDetails(
                        new PackageCarrierInfo().withCarrierName("DHL").withTrackingId("123456")),
                Context.NONE);
    }
}
```

### Jobs_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.databox.models.AddressType;
import com.azure.resourcemanager.databox.models.ContactDetails;
import com.azure.resourcemanager.databox.models.IdentityProperties;
import com.azure.resourcemanager.databox.models.JobResource;
import com.azure.resourcemanager.databox.models.KekType;
import com.azure.resourcemanager.databox.models.KeyEncryptionKey;
import com.azure.resourcemanager.databox.models.ResourceIdentity;
import com.azure.resourcemanager.databox.models.ShippingAddress;
import com.azure.resourcemanager.databox.models.UpdateJobDetails;
import com.azure.resourcemanager.databox.models.UserAssignedIdentity;
import com.azure.resourcemanager.databox.models.UserAssignedProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for Jobs Update. */
public final class JobsUpdateSamples {
    /*
     * x-ms-original-file: specification/databox/resource-manager/Microsoft.DataBox/stable/2022-02-01/examples/JobsPatchCmk.json
     */
    /**
     * Sample code: JobsPatchCmk.
     *
     * @param manager Entry point to DataBoxManager.
     */
    public static void jobsPatchCmk(com.azure.resourcemanager.databox.DataBoxManager manager) {
        JobResource resource =
            manager.jobs().getByResourceGroupWithResponse("SdkRg7937", "SdkJob1735", null, Context.NONE).getValue();
        resource
            .update()
            .withDetails(
                new UpdateJobDetails()
                    .withKeyEncryptionKey(
                        new KeyEncryptionKey()
                            .withKekType(KekType.CUSTOMER_MANAGED)
                            .withKekUrl("https://sdkkeyvault.vault.azure.net/keys/SSDKEY/")
                            .withKekVaultResourceId(
                                "/subscriptions/fa68082f-8ff7-4a25-95c7-ce9da541242f/resourceGroups/akvenkat/providers/Microsoft.KeyVault/vaults/SDKKeyVault")))
            .apply();
    }

    /*
     * x-ms-original-file: specification/databox/resource-manager/Microsoft.DataBox/stable/2022-02-01/examples/JobsPatchSystemAssignedToUserAssigned.json
     */
    /**
     * Sample code: JobsPatchSystemAssignedToUserAssigned.
     *
     * @param manager Entry point to DataBoxManager.
     */
    public static void jobsPatchSystemAssignedToUserAssigned(com.azure.resourcemanager.databox.DataBoxManager manager) {
        JobResource resource =
            manager.jobs().getByResourceGroupWithResponse("SdkRg9765", "SdkJob2965", null, Context.NONE).getValue();
        resource
            .update()
            .withIdentity(
                new ResourceIdentity()
                    .withType("SystemAssigned,UserAssigned")
                    .withUserAssignedIdentities(
                        mapOf(
                            "/subscriptions/fa68082f-8ff7-4a25-95c7-ce9da541242f/resourceGroups/akvenkat/providers/Microsoft.ManagedIdentity/userAssignedIdentities/sdkIdentity",
                            new UserAssignedIdentity())))
            .withDetails(
                new UpdateJobDetails()
                    .withKeyEncryptionKey(
                        new KeyEncryptionKey()
                            .withKekType(KekType.CUSTOMER_MANAGED)
                            .withIdentityProperties(
                                new IdentityProperties()
                                    .withType("UserAssigned")
                                    .withUserAssigned(
                                        new UserAssignedProperties()
                                            .withResourceId(
                                                "/subscriptions/fa68082f-8ff7-4a25-95c7-ce9da541242f/resourceGroups/akvenkat/providers/Microsoft.ManagedIdentity/userAssignedIdentities/sdkIdentity")))
                            .withKekUrl("https://sdkkeyvault.vault.azure.net/keys/SSDKEY/")
                            .withKekVaultResourceId(
                                "/subscriptions/fa68082f-8ff7-4a25-95c7-ce9da541242f/resourceGroups/akvenkat/providers/Microsoft.KeyVault/vaults/SDKKeyVault")))
            .apply();
    }

    /*
     * x-ms-original-file: specification/databox/resource-manager/Microsoft.DataBox/stable/2022-02-01/examples/JobsPatch.json
     */
    /**
     * Sample code: JobsPatch.
     *
     * @param manager Entry point to DataBoxManager.
     */
    public static void jobsPatch(com.azure.resourcemanager.databox.DataBoxManager manager) {
        JobResource resource =
            manager.jobs().getByResourceGroupWithResponse("SdkRg5154", "SdkJob952", "details", Context.NONE).getValue();
        resource
            .update()
            .withDetails(
                new UpdateJobDetails()
                    .withContactDetails(
                        new ContactDetails()
                            .withContactName("Update Job")
                            .withPhone("1234567890")
                            .withPhoneExtension("1234")
                            .withEmailList(Arrays.asList("testing@microsoft.com")))
                    .withShippingAddress(
                        new ShippingAddress()
                            .withStreetAddress1("16 TOWNSEND ST")
                            .withStreetAddress2("Unit 1")
                            .withCity("San Francisco")
                            .withStateOrProvince("CA")
                            .withCountry("US")
                            .withPostalCode("94107")
                            .withCompanyName("Microsoft")
                            .withAddressType(AddressType.COMMERCIAL)))
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

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/databox/resource-manager/Microsoft.DataBox/stable/2022-02-01/examples/OperationsGet.json
     */
    /**
     * Sample code: OperationsGet.
     *
     * @param manager Entry point to DataBoxManager.
     */
    public static void operationsGet(com.azure.resourcemanager.databox.DataBoxManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### ResourceProvider_Mitigate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.databox.models.CustomerResolutionCode;
import com.azure.resourcemanager.databox.models.MitigateJobRequest;

/** Samples for ResourceProvider Mitigate. */
public final class ResourceProviderMitigateSamples {
    /*
     * x-ms-original-file: specification/databox/resource-manager/Microsoft.DataBox/stable/2022-02-01/examples/JobMitigate.json
     */
    /**
     * Sample code: Mitigate.
     *
     * @param manager Entry point to DataBoxManager.
     */
    public static void mitigate(com.azure.resourcemanager.databox.DataBoxManager manager) {
        manager
            .resourceProviders()
            .mitigateWithResponse(
                "SdkJob8367",
                "SdkRg9836",
                new MitigateJobRequest().withCustomerResolutionCode(CustomerResolutionCode.MOVE_TO_CLEAN_UP_DEVICE),
                Context.NONE);
    }
}
```

### Service_ListAvailableSkusByResourceGroup

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.databox.models.AvailableSkuRequest;
import com.azure.resourcemanager.databox.models.TransferType;

/** Samples for Service ListAvailableSkusByResourceGroup. */
public final class ServiceListAvailableSkusByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/databox/resource-manager/Microsoft.DataBox/stable/2022-02-01/examples/AvailableSkusPost.json
     */
    /**
     * Sample code: AvailableSkusPost.
     *
     * @param manager Entry point to DataBoxManager.
     */
    public static void availableSkusPost(com.azure.resourcemanager.databox.DataBoxManager manager) {
        manager
            .services()
            .listAvailableSkusByResourceGroup(
                "bvttoolrg6",
                "westus",
                new AvailableSkuRequest()
                    .withTransferType(TransferType.IMPORT_TO_AZURE)
                    .withCountry("US")
                    .withLocation("westus"),
                Context.NONE);
    }
}
```

### Service_RegionConfiguration

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.databox.models.DataBoxScheduleAvailabilityRequest;
import com.azure.resourcemanager.databox.models.RegionConfigurationRequest;

/** Samples for Service RegionConfiguration. */
public final class ServiceRegionConfigurationSamples {
    /*
     * x-ms-original-file: specification/databox/resource-manager/Microsoft.DataBox/stable/2022-02-01/examples/RegionConfiguration.json
     */
    /**
     * Sample code: RegionConfiguration.
     *
     * @param manager Entry point to DataBoxManager.
     */
    public static void regionConfiguration(com.azure.resourcemanager.databox.DataBoxManager manager) {
        manager
            .services()
            .regionConfigurationWithResponse(
                "westus",
                new RegionConfigurationRequest()
                    .withScheduleAvailabilityRequest(
                        new DataBoxScheduleAvailabilityRequest().withStorageLocation("westus")),
                Context.NONE);
    }
}
```

### Service_RegionConfigurationByResourceGroup

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.databox.models.DataBoxScheduleAvailabilityRequest;
import com.azure.resourcemanager.databox.models.RegionConfigurationRequest;

/** Samples for Service RegionConfigurationByResourceGroup. */
public final class ServiceRegionConfigurationByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/databox/resource-manager/Microsoft.DataBox/stable/2022-02-01/examples/RegionConfigurationByResourceGroup.json
     */
    /**
     * Sample code: RegionConfigurationByResourceGroup.
     *
     * @param manager Entry point to DataBoxManager.
     */
    public static void regionConfigurationByResourceGroup(com.azure.resourcemanager.databox.DataBoxManager manager) {
        manager
            .services()
            .regionConfigurationByResourceGroupWithResponse(
                "SdkRg4981",
                "westus",
                new RegionConfigurationRequest()
                    .withScheduleAvailabilityRequest(
                        new DataBoxScheduleAvailabilityRequest().withStorageLocation("westus")),
                Context.NONE);
    }
}
```

### Service_ValidateAddress

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.databox.models.AddressType;
import com.azure.resourcemanager.databox.models.ShippingAddress;
import com.azure.resourcemanager.databox.models.SkuName;
import com.azure.resourcemanager.databox.models.ValidateAddress;

/** Samples for Service ValidateAddress. */
public final class ServiceValidateAddressSamples {
    /*
     * x-ms-original-file: specification/databox/resource-manager/Microsoft.DataBox/stable/2022-02-01/examples/ValidateAddressPost.json
     */
    /**
     * Sample code: ValidateAddressPost.
     *
     * @param manager Entry point to DataBoxManager.
     */
    public static void validateAddressPost(com.azure.resourcemanager.databox.DataBoxManager manager) {
        manager
            .services()
            .validateAddressWithResponse(
                "westus",
                new ValidateAddress()
                    .withShippingAddress(
                        new ShippingAddress()
                            .withStreetAddress1("16 TOWNSEND ST")
                            .withStreetAddress2("Unit 1")
                            .withCity("San Francisco")
                            .withStateOrProvince("CA")
                            .withCountry("US")
                            .withPostalCode("94107")
                            .withCompanyName("Microsoft")
                            .withAddressType(AddressType.COMMERCIAL))
                    .withDeviceType(SkuName.DATA_BOX),
                Context.NONE);
    }
}
```

### Service_ValidateInputs

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.databox.models.AddressType;
import com.azure.resourcemanager.databox.models.CreateJobValidations;
import com.azure.resourcemanager.databox.models.CreateOrderLimitForSubscriptionValidationRequest;
import com.azure.resourcemanager.databox.models.DataImportDetails;
import com.azure.resourcemanager.databox.models.DataTransferDetailsValidationRequest;
import com.azure.resourcemanager.databox.models.Preferences;
import com.azure.resourcemanager.databox.models.PreferencesValidationRequest;
import com.azure.resourcemanager.databox.models.ShippingAddress;
import com.azure.resourcemanager.databox.models.SkuAvailabilityValidationRequest;
import com.azure.resourcemanager.databox.models.SkuName;
import com.azure.resourcemanager.databox.models.StorageAccountDetails;
import com.azure.resourcemanager.databox.models.SubscriptionIsAllowedToCreateJobValidationRequest;
import com.azure.resourcemanager.databox.models.TransferType;
import com.azure.resourcemanager.databox.models.TransportPreferences;
import com.azure.resourcemanager.databox.models.TransportShipmentTypes;
import com.azure.resourcemanager.databox.models.ValidateAddress;
import java.util.Arrays;

/** Samples for Service ValidateInputs. */
public final class ServiceValidateInputsSamples {
    /*
     * x-ms-original-file: specification/databox/resource-manager/Microsoft.DataBox/stable/2022-02-01/examples/ValidateInputs.json
     */
    /**
     * Sample code: ValidateInputs.
     *
     * @param manager Entry point to DataBoxManager.
     */
    public static void validateInputs(com.azure.resourcemanager.databox.DataBoxManager manager) {
        manager
            .services()
            .validateInputsWithResponse(
                "westus",
                new CreateJobValidations()
                    .withIndividualRequestDetails(
                        Arrays
                            .asList(
                                new DataTransferDetailsValidationRequest()
                                    .withDataImportDetails(
                                        Arrays
                                            .asList(
                                                new DataImportDetails()
                                                    .withAccountDetails(
                                                        new StorageAccountDetails()
                                                            .withStorageAccountId(
                                                                "/subscriptions/fa68082f-8ff7-4a25-95c7-ce9da541242f/resourcegroups/databoxbvt/providers/Microsoft.Storage/storageAccounts/databoxbvttestaccount"))))
                                    .withDeviceType(SkuName.DATA_BOX)
                                    .withTransferType(TransferType.IMPORT_TO_AZURE),
                                new ValidateAddress()
                                    .withShippingAddress(
                                        new ShippingAddress()
                                            .withStreetAddress1("16 TOWNSEND ST")
                                            .withStreetAddress2("Unit 1")
                                            .withCity("San Francisco")
                                            .withStateOrProvince("CA")
                                            .withCountry("US")
                                            .withPostalCode("94107")
                                            .withCompanyName("Microsoft")
                                            .withAddressType(AddressType.COMMERCIAL))
                                    .withDeviceType(SkuName.DATA_BOX)
                                    .withTransportPreferences(
                                        new TransportPreferences()
                                            .withPreferredShipmentType(TransportShipmentTypes.MICROSOFT_MANAGED)),
                                new SubscriptionIsAllowedToCreateJobValidationRequest(),
                                new SkuAvailabilityValidationRequest()
                                    .withDeviceType(SkuName.DATA_BOX)
                                    .withTransferType(TransferType.IMPORT_TO_AZURE)
                                    .withCountry("US")
                                    .withLocation("westus"),
                                new CreateOrderLimitForSubscriptionValidationRequest().withDeviceType(SkuName.DATA_BOX),
                                new PreferencesValidationRequest()
                                    .withPreference(
                                        new Preferences()
                                            .withTransportPreferences(
                                                new TransportPreferences()
                                                    .withPreferredShipmentType(
                                                        TransportShipmentTypes.MICROSOFT_MANAGED)))
                                    .withDeviceType(SkuName.DATA_BOX))),
                Context.NONE);
    }
}
```

### Service_ValidateInputsByResourceGroup

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.databox.models.AddressType;
import com.azure.resourcemanager.databox.models.CreateJobValidations;
import com.azure.resourcemanager.databox.models.CreateOrderLimitForSubscriptionValidationRequest;
import com.azure.resourcemanager.databox.models.DataImportDetails;
import com.azure.resourcemanager.databox.models.DataTransferDetailsValidationRequest;
import com.azure.resourcemanager.databox.models.Preferences;
import com.azure.resourcemanager.databox.models.PreferencesValidationRequest;
import com.azure.resourcemanager.databox.models.ShippingAddress;
import com.azure.resourcemanager.databox.models.SkuAvailabilityValidationRequest;
import com.azure.resourcemanager.databox.models.SkuName;
import com.azure.resourcemanager.databox.models.StorageAccountDetails;
import com.azure.resourcemanager.databox.models.SubscriptionIsAllowedToCreateJobValidationRequest;
import com.azure.resourcemanager.databox.models.TransferType;
import com.azure.resourcemanager.databox.models.TransportPreferences;
import com.azure.resourcemanager.databox.models.TransportShipmentTypes;
import com.azure.resourcemanager.databox.models.ValidateAddress;
import java.util.Arrays;

/** Samples for Service ValidateInputsByResourceGroup. */
public final class ServiceValidateInputsByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/databox/resource-manager/Microsoft.DataBox/stable/2022-02-01/examples/ValidateInputsByResourceGroup.json
     */
    /**
     * Sample code: ValidateInputsByResourceGroup.
     *
     * @param manager Entry point to DataBoxManager.
     */
    public static void validateInputsByResourceGroup(com.azure.resourcemanager.databox.DataBoxManager manager) {
        manager
            .services()
            .validateInputsByResourceGroupWithResponse(
                "SdkRg6861",
                "westus",
                new CreateJobValidations()
                    .withIndividualRequestDetails(
                        Arrays
                            .asList(
                                new DataTransferDetailsValidationRequest()
                                    .withDataImportDetails(
                                        Arrays
                                            .asList(
                                                new DataImportDetails()
                                                    .withAccountDetails(
                                                        new StorageAccountDetails()
                                                            .withStorageAccountId(
                                                                "/subscriptions/fa68082f-8ff7-4a25-95c7-ce9da541242f/resourcegroups/databoxbvt/providers/Microsoft.Storage/storageAccounts/databoxbvttestaccount"))))
                                    .withDeviceType(SkuName.DATA_BOX)
                                    .withTransferType(TransferType.IMPORT_TO_AZURE),
                                new ValidateAddress()
                                    .withShippingAddress(
                                        new ShippingAddress()
                                            .withStreetAddress1("16 TOWNSEND ST")
                                            .withStreetAddress2("Unit 1")
                                            .withCity("San Francisco")
                                            .withStateOrProvince("CA")
                                            .withCountry("US")
                                            .withPostalCode("94107")
                                            .withCompanyName("Microsoft")
                                            .withAddressType(AddressType.COMMERCIAL))
                                    .withDeviceType(SkuName.DATA_BOX)
                                    .withTransportPreferences(
                                        new TransportPreferences()
                                            .withPreferredShipmentType(TransportShipmentTypes.MICROSOFT_MANAGED)),
                                new SubscriptionIsAllowedToCreateJobValidationRequest(),
                                new SkuAvailabilityValidationRequest()
                                    .withDeviceType(SkuName.DATA_BOX)
                                    .withTransferType(TransferType.IMPORT_TO_AZURE)
                                    .withCountry("US")
                                    .withLocation("westus"),
                                new CreateOrderLimitForSubscriptionValidationRequest().withDeviceType(SkuName.DATA_BOX),
                                new PreferencesValidationRequest()
                                    .withPreference(
                                        new Preferences()
                                            .withTransportPreferences(
                                                new TransportPreferences()
                                                    .withPreferredShipmentType(
                                                        TransportShipmentTypes.MICROSOFT_MANAGED)))
                                    .withDeviceType(SkuName.DATA_BOX))),
                Context.NONE);
    }
}
```

