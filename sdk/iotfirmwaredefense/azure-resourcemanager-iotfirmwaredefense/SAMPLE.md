# Code snippets and samples


## BinaryHardening

- [ListByFirmware](#binaryhardening_listbyfirmware)

## CryptoCertificates

- [ListByFirmware](#cryptocertificates_listbyfirmware)

## CryptoKeys

- [ListByFirmware](#cryptokeys_listbyfirmware)

## Cves

- [ListByFirmware](#cves_listbyfirmware)

## Firmwares

- [Create](#firmwares_create)
- [Delete](#firmwares_delete)
- [GenerateDownloadUrl](#firmwares_generatedownloadurl)
- [GenerateFilesystemDownloadUrl](#firmwares_generatefilesystemdownloadurl)
- [Get](#firmwares_get)
- [ListByWorkspace](#firmwares_listbyworkspace)
- [Update](#firmwares_update)

## Operations

- [List](#operations_list)

## PasswordHashes

- [ListByFirmware](#passwordhashes_listbyfirmware)

## SbomComponents

- [ListByFirmware](#sbomcomponents_listbyfirmware)

## Summaries

- [Get](#summaries_get)
- [ListByFirmware](#summaries_listbyfirmware)

## Workspaces

- [Create](#workspaces_create)
- [Delete](#workspaces_delete)
- [GenerateUploadUrl](#workspaces_generateuploadurl)
- [GetByResourceGroup](#workspaces_getbyresourcegroup)
- [List](#workspaces_list)
- [ListByResourceGroup](#workspaces_listbyresourcegroup)
- [Update](#workspaces_update)
### BinaryHardening_ListByFirmware

```java
/**
 * Samples for BinaryHardening ListByFirmware.
 */
public final class BinaryHardeningListByFirmwareSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * BinaryHardening_ListByFirmware_MinimumSet_Gen.json
     */
    /**
     * Sample code: BinaryHardening_ListByFirmware_MinimumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void binaryHardeningListByFirmwareMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.binaryHardenings().listByFirmware("FirmwareAnalysisRG", "default",
            "109a9886-50bf-85a8-9d75-000000000000", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * BinaryHardening_ListByFirmware_MaximumSet_Gen.json
     */
    /**
     * Sample code: BinaryHardening_ListByFirmware_MaximumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void binaryHardeningListByFirmwareMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.binaryHardenings().listByFirmware("FirmwareAnalysisRG", "default",
            "109a9886-50bf-85a8-9d75-000000000000", com.azure.core.util.Context.NONE);
    }
}
```

### CryptoCertificates_ListByFirmware

```java
/**
 * Samples for CryptoCertificates ListByFirmware.
 */
public final class CryptoCertificatesListByFirmwareSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * CryptoCertificates_ListByFirmware_MaximumSet_Gen.json
     */
    /**
     * Sample code: CryptoCertificates_ListByFirmware_MaximumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void cryptoCertificatesListByFirmwareMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.cryptoCertificates().listByFirmware("FirmwareAnalysisRG", "default",
            "109a9886-50bf-85a8-9d75-000000000000", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * CryptoCertificates_ListByFirmware_MinimumSet_Gen.json
     */
    /**
     * Sample code: CryptoCertificates_ListByFirmware_MinimumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void cryptoCertificatesListByFirmwareMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.cryptoCertificates().listByFirmware("FirmwareAnalysisRG", "default",
            "109a9886-50bf-85a8-9d75-000000000000", com.azure.core.util.Context.NONE);
    }
}
```

### CryptoKeys_ListByFirmware

```java
/**
 * Samples for CryptoKeys ListByFirmware.
 */
public final class CryptoKeysListByFirmwareSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * CryptoKeys_ListByFirmware_MinimumSet_Gen.json
     */
    /**
     * Sample code: CryptoKeys_ListByFirmware_MinimumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void cryptoKeysListByFirmwareMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.cryptoKeys().listByFirmware("FirmwareAnalysisRG", "default", "109a9886-50bf-85a8-9d75-000000000000",
            com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * CryptoKeys_ListByFirmware_MaximumSet_Gen.json
     */
    /**
     * Sample code: CryptoKeys_ListByFirmware_MaximumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void cryptoKeysListByFirmwareMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.cryptoKeys().listByFirmware("FirmwareAnalysisRG", "default", "109a9886-50bf-85a8-9d75-000000000000",
            com.azure.core.util.Context.NONE);
    }
}
```

### Cves_ListByFirmware

```java
/**
 * Samples for Cves ListByFirmware.
 */
public final class CvesListByFirmwareSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Cves_ListByFirmware_MaximumSet_Gen.json
     */
    /**
     * Sample code: Cves_ListByFirmware_MaximumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void cvesListByFirmwareMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.cves().listByFirmware("FirmwareAnalysisRG", "default", "109a9886-50bf-85a8-9d75-000000000000",
            com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Cves_ListByFirmware_MinimumSet_Gen.json
     */
    /**
     * Sample code: Cves_ListByFirmware_MinimumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void cvesListByFirmwareMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.cves().listByFirmware("FirmwareAnalysisRG", "default", "109a9886-50bf-85a8-9d75-000000000000",
            com.azure.core.util.Context.NONE);
    }
}
```

### Firmwares_Create

```java
import com.azure.resourcemanager.iotfirmwaredefense.models.Status;
import com.azure.resourcemanager.iotfirmwaredefense.models.StatusMessage;
import java.util.Arrays;

/**
 * Samples for Firmwares Create.
 */
public final class FirmwaresCreateSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Firmwares_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firmwares_Create_MaximumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void
        firmwaresCreateMaximumSetGen(com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.firmwares().define("umrkdttp").withExistingWorkspace("rgworkspaces-firmwares", "A7")
            .withFileName("wresexxulcdsdd").withVendor("vycmdhgtmepcptyoubztiuudpkcpd").withModel("f").withVersion("s")
            .withDescription("uz").withFileSize(17L).withStatus(Status.PENDING)
            .withStatusMessages(Arrays.asList(new StatusMessage().withMessage("ulvhmhokezathzzauiitu"))).create();
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Firmwares_Create_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firmwares_Create_MinimumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void
        firmwaresCreateMinimumSetGen(com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.firmwares().define("umrkdttp").withExistingWorkspace("rgworkspaces-firmwares", "A7").create();
    }
}
```

### Firmwares_Delete

```java
/**
 * Samples for Firmwares Delete.
 */
public final class FirmwaresDeleteSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Firmwares_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firmwares_Delete_MinimumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void
        firmwaresDeleteMinimumSetGen(com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.firmwares().deleteWithResponse("rgworkspaces-firmwares", "A7", "umrkdttp",
            com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Firmwares_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firmwares_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void
        firmwaresDeleteMaximumSetGen(com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.firmwares().deleteWithResponse("rgworkspaces-firmwares", "A7", "umrkdttp",
            com.azure.core.util.Context.NONE);
    }
}
```

### Firmwares_GenerateDownloadUrl

```java
/**
 * Samples for Firmwares GenerateDownloadUrl.
 */
public final class FirmwaresGenerateDownloadUrlSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Firmwares_GenerateDownloadUrl_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firmwares_GenerateDownloadUrl_MaximumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwaresGenerateDownloadUrlMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.firmwares().generateDownloadUrlWithResponse("rgworkspaces-firmwares", "A7", "umrkdttp",
            com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Firmwares_GenerateDownloadUrl_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firmwares_GenerateDownloadUrl_MinimumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwaresGenerateDownloadUrlMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.firmwares().generateDownloadUrlWithResponse("rgworkspaces-firmwares", "A7", "umrkdttp",
            com.azure.core.util.Context.NONE);
    }
}
```

### Firmwares_GenerateFilesystemDownloadUrl

```java
/**
 * Samples for Firmwares GenerateFilesystemDownloadUrl.
 */
public final class FirmwaresGenerateFilesystemDownloadUrlSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Firmwares_GenerateFilesystemDownloadUrl_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firmwares_GenerateFilesystemDownloadUrl_MaximumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwaresGenerateFilesystemDownloadUrlMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.firmwares().generateFilesystemDownloadUrlWithResponse("rgworkspaces-firmwares", "A7", "umrkdttp",
            com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Firmwares_GenerateFilesystemDownloadUrl_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firmwares_GenerateFilesystemDownloadUrl_MinimumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwaresGenerateFilesystemDownloadUrlMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.firmwares().generateFilesystemDownloadUrlWithResponse("rgworkspaces-firmwares", "A7", "umrkdttp",
            com.azure.core.util.Context.NONE);
    }
}
```

### Firmwares_Get

```java
/**
 * Samples for Firmwares Get.
 */
public final class FirmwaresGetSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Firmwares_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firmwares_Get_MinimumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void
        firmwaresGetMinimumSetGen(com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.firmwares().getWithResponse("rgworkspaces-firmwares", "A7", "umrkdttp",
            com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Firmwares_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firmwares_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void
        firmwaresGetMaximumSetGen(com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.firmwares().getWithResponse("rgworkspaces-firmwares", "A7", "umrkdttp",
            com.azure.core.util.Context.NONE);
    }
}
```

### Firmwares_ListByWorkspace

```java
/**
 * Samples for Firmwares ListByWorkspace.
 */
public final class FirmwaresListByWorkspaceSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Firmwares_ListByWorkspace_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firmwares_ListByWorkspace_MaximumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwaresListByWorkspaceMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.firmwares().listByWorkspace("rgworkspaces-firmwares", "A7", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Firmwares_ListByWorkspace_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firmwares_ListByWorkspace_MinimumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwaresListByWorkspaceMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.firmwares().listByWorkspace("rgworkspaces-firmwares", "A7", com.azure.core.util.Context.NONE);
    }
}
```

### Firmwares_Update

```java
import com.azure.resourcemanager.iotfirmwaredefense.models.Firmware;
import com.azure.resourcemanager.iotfirmwaredefense.models.Status;
import com.azure.resourcemanager.iotfirmwaredefense.models.StatusMessage;
import java.util.Arrays;

/**
 * Samples for Firmwares Update.
 */
public final class FirmwaresUpdateSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Firmwares_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firmwares_Update_MaximumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void
        firmwaresUpdateMaximumSetGen(com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        Firmware resource = manager.firmwares()
            .getWithResponse("rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE).getValue();
        resource.update().withFileName("wresexxulcdsdd").withVendor("vycmdhgtmepcptyoubztiuudpkcpd").withModel("f")
            .withVersion("s").withDescription("uz").withFileSize(17L).withStatus(Status.PENDING)
            .withStatusMessages(Arrays.asList(new StatusMessage().withMessage("ulvhmhokezathzzauiitu"))).apply();
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Firmwares_Update_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firmwares_Update_MinimumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void
        firmwaresUpdateMinimumSetGen(com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        Firmware resource = manager.firmwares()
            .getWithResponse("rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE).getValue();
        resource.update().apply();
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
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Operations_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MinimumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void
        operationsListMinimumSetGen(com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MaximumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void
        operationsListMaximumSetGen(com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PasswordHashes_ListByFirmware

```java
/**
 * Samples for PasswordHashes ListByFirmware.
 */
public final class PasswordHashesListByFirmwareSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * PasswordHashes_ListByFirmware_MaximumSet_Gen.json
     */
    /**
     * Sample code: PasswordHashes_ListByFirmware_MaximumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void passwordHashesListByFirmwareMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.passwordHashes().listByFirmware("FirmwareAnalysisRG", "default", "109a9886-50bf-85a8-9d75-000000000000",
            com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * PasswordHashes_ListByFirmware_MinimumSet_Gen.json
     */
    /**
     * Sample code: PasswordHashes_ListByFirmware_MinimumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void passwordHashesListByFirmwareMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.passwordHashes().listByFirmware("FirmwareAnalysisRG", "default", "109a9886-50bf-85a8-9d75-000000000000",
            com.azure.core.util.Context.NONE);
    }
}
```

### SbomComponents_ListByFirmware

```java
/**
 * Samples for SbomComponents ListByFirmware.
 */
public final class SbomComponentsListByFirmwareSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * SbomComponents_ListByFirmware_MinimumSet_Gen.json
     */
    /**
     * Sample code: SbomComponents_ListByFirmware_MinimumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void sbomComponentsListByFirmwareMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.sbomComponents().listByFirmware("FirmwareAnalysisRG", "default", "109a9886-50bf-85a8-9d75-000000000000",
            com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * SbomComponents_ListByFirmware_MaximumSet_Gen.json
     */
    /**
     * Sample code: SbomComponents_ListByFirmware_MaximumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void sbomComponentsListByFirmwareMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.sbomComponents().listByFirmware("FirmwareAnalysisRG", "default", "109a9886-50bf-85a8-9d75-000000000000",
            com.azure.core.util.Context.NONE);
    }
}
```

### Summaries_Get

```java
import com.azure.resourcemanager.iotfirmwaredefense.models.SummaryName;

/**
 * Samples for Summaries Get.
 */
public final class SummariesGetSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Summaries_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Summaries_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void
        summariesGetMaximumSetGen(com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.summaries().getWithResponse("FirmwareAnalysisRG", "default", "109a9886-50bf-85a8-9d75-000000000000",
            SummaryName.FIRMWARE, com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Summaries_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: Summaries_Get_MinimumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void
        summariesGetMinimumSetGen(com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.summaries().getWithResponse("FirmwareAnalysisRG", "default", "109a9886-50bf-85a8-9d75-000000000000",
            SummaryName.FIRMWARE, com.azure.core.util.Context.NONE);
    }
}
```

### Summaries_ListByFirmware

```java
/**
 * Samples for Summaries ListByFirmware.
 */
public final class SummariesListByFirmwareSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Summaries_ListByFirmware_MinimumSet_Gen.json
     */
    /**
     * Sample code: Summaries_ListByFirmware_MinimumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void summariesListByFirmwareMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.summaries().listByFirmware("FirmwareAnalysisRG", "default", "109a9886-50bf-85a8-9d75-000000000000",
            com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Summaries_ListByFirmware_MaximumSet_Gen.json
     */
    /**
     * Sample code: Summaries_ListByFirmware_MaximumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void summariesListByFirmwareMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.summaries().listByFirmware("FirmwareAnalysisRG", "default", "109a9886-50bf-85a8-9d75-000000000000",
            com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_Create

```java
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Workspaces Create.
 */
public final class WorkspacesCreateSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Workspaces_Create_MinimumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_Create_MinimumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void
        workspacesCreateMinimumSetGen(com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.workspaces().define("E___-3").withRegion("jjwbseilitjgdrhbvvkwviqj")
            .withExistingResourceGroup("rgworkspaces").create();
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Workspaces_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_Create_MaximumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void
        workspacesCreateMaximumSetGen(com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.workspaces().define("E___-3").withRegion("jjwbseilitjgdrhbvvkwviqj")
            .withExistingResourceGroup("rgworkspaces").withTags(mapOf("key450", "fakeTokenPlaceholder")).create();
    }

    // Use "Map.of" if available
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

### Workspaces_Delete

```java
/**
 * Samples for Workspaces Delete.
 */
public final class WorkspacesDeleteSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Workspaces_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_Delete_MinimumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void
        workspacesDeleteMinimumSetGen(com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.workspaces().deleteByResourceGroupWithResponse("rgworkspaces", "E___-3",
            com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Workspaces_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_Delete_MaximumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void
        workspacesDeleteMaximumSetGen(com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.workspaces().deleteByResourceGroupWithResponse("rgworkspaces", "E___-3",
            com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_GenerateUploadUrl

```java
import com.azure.resourcemanager.iotfirmwaredefense.models.GenerateUploadUrlRequest;

/**
 * Samples for Workspaces GenerateUploadUrl.
 */
public final class WorkspacesGenerateUploadUrlSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Workspaces_GenerateUploadUrl_MinimumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_GenerateUploadUrl_MinimumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void workspacesGenerateUploadUrlMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.workspaces().generateUploadUrlWithResponse("rgworkspaces", "E___-3", new GenerateUploadUrlRequest(),
            com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Workspaces_GenerateUploadUrl_MaximumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_GenerateUploadUrl_MaximumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void workspacesGenerateUploadUrlMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.workspaces().generateUploadUrlWithResponse("rgworkspaces", "E___-3",
            new GenerateUploadUrlRequest().withFirmwareId("ytsfprbywi"), com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_GetByResourceGroup

```java
/**
 * Samples for Workspaces GetByResourceGroup.
 */
public final class WorkspacesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Workspaces_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_Get_MaximumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void
        workspacesGetMaximumSetGen(com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.workspaces().getByResourceGroupWithResponse("rgworkspaces", "E_US", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Workspaces_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_Get_MinimumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void
        workspacesGetMinimumSetGen(com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.workspaces().getByResourceGroupWithResponse("rgworkspaces", "E_US", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_List

```java
/**
 * Samples for Workspaces List.
 */
public final class WorkspacesListSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Workspaces_ListBySubscription_MaximumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_ListBySubscription_MaximumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void workspacesListBySubscriptionMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.workspaces().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Workspaces_ListBySubscription_MinimumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_ListBySubscription_MinimumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void workspacesListBySubscriptionMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.workspaces().list(com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_ListByResourceGroup

```java
/**
 * Samples for Workspaces ListByResourceGroup.
 */
public final class WorkspacesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Workspaces_ListByResourceGroup_MaximumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_ListByResourceGroup_MaximumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void workspacesListByResourceGroupMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.workspaces().listByResourceGroup("rgworkspaces", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Workspaces_ListByResourceGroup_MinimumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_ListByResourceGroup_MinimumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void workspacesListByResourceGroupMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.workspaces().listByResourceGroup("rgworkspaces", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_Update

```java
import com.azure.resourcemanager.iotfirmwaredefense.models.Workspace;

/**
 * Samples for Workspaces Update.
 */
public final class WorkspacesUpdateSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Workspaces_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_Update_MaximumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void
        workspacesUpdateMaximumSetGen(com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        Workspace resource = manager.workspaces()
            .getByResourceGroupWithResponse("rgworkspaces", "E___-3", com.azure.core.util.Context.NONE).getValue();
        resource.update().apply();
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/stable/2024-01-10/examples/
     * Workspaces_Update_MinimumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_Update_MinimumSet_Gen.
     * 
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void
        workspacesUpdateMinimumSetGen(com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        Workspace resource = manager.workspaces()
            .getByResourceGroupWithResponse("rgworkspaces", "E___-3", com.azure.core.util.Context.NONE).getValue();
        resource.update().apply();
    }
}
```

