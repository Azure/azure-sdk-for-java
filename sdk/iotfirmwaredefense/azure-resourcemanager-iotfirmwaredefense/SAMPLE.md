# Code snippets and samples


## Firmware

- [Create](#firmware_create)
- [Delete](#firmware_delete)
- [GenerateBinaryHardeningDetails](#firmware_generatebinaryhardeningdetails)
- [GenerateBinaryHardeningSummary](#firmware_generatebinaryhardeningsummary)
- [GenerateComponentDetails](#firmware_generatecomponentdetails)
- [GenerateCryptoCertificateSummary](#firmware_generatecryptocertificatesummary)
- [GenerateCryptoKeySummary](#firmware_generatecryptokeysummary)
- [GenerateCveSummary](#firmware_generatecvesummary)
- [GenerateDownloadUrl](#firmware_generatedownloadurl)
- [GenerateFilesystemDownloadUrl](#firmware_generatefilesystemdownloadurl)
- [GenerateSummary](#firmware_generatesummary)
- [Get](#firmware_get)
- [ListByWorkspace](#firmware_listbyworkspace)
- [ListGenerateBinaryHardeningList](#firmware_listgeneratebinaryhardeninglist)
- [ListGenerateComponentList](#firmware_listgeneratecomponentlist)
- [ListGenerateCryptoCertificateList](#firmware_listgeneratecryptocertificatelist)
- [ListGenerateCryptoKeyList](#firmware_listgeneratecryptokeylist)
- [ListGenerateCveList](#firmware_listgeneratecvelist)
- [ListGeneratePasswordHashList](#firmware_listgeneratepasswordhashlist)
- [Update](#firmware_update)

## Operations

- [List](#operations_list)

## Workspaces

- [Create](#workspaces_create)
- [Delete](#workspaces_delete)
- [GenerateUploadUrl](#workspaces_generateuploadurl)
- [GetByResourceGroup](#workspaces_getbyresourcegroup)
- [List](#workspaces_list)
- [ListByResourceGroup](#workspaces_listbyresourcegroup)
- [Update](#workspaces_update)
### Firmware_Create

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.iotfirmwaredefense.models.Status;
import java.io.IOException;
import java.util.Arrays;

/** Samples for Firmware Create. */
public final class FirmwareCreateSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_Create_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firmware_Create_MinimumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareCreateMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.firmwares().define("umrkdttp").withExistingWorkspace("rgworkspaces-firmwares", "A7").create();
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firmware_Create_MaximumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareCreateMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) throws IOException {
        manager
            .firmwares()
            .define("umrkdttp")
            .withExistingWorkspace("rgworkspaces-firmwares", "A7")
            .withFileName("wresexxulcdsdd")
            .withVendor("vycmdhgtmepcptyoubztiuudpkcpd")
            .withModel("f")
            .withVersion("s")
            .withDescription("uz")
            .withFileSize(17L)
            .withStatus(Status.PENDING)
            .withStatusMessages(
                Arrays
                    .asList(
                        SerializerFactory
                            .createDefaultManagementSerializerAdapter()
                            .deserialize(
                                "{\"message\":\"ulvhmhokezathzzauiitu\"}", Object.class, SerializerEncoding.JSON)))
            .create();
    }
}
```

### Firmware_Delete

```java
/** Samples for Firmware Delete. */
public final class FirmwareDeleteSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firmware_Delete_MinimumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareDeleteMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .deleteWithResponse("rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firmware_Delete_MaximumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareDeleteMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .deleteWithResponse("rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE);
    }
}
```

### Firmware_GenerateBinaryHardeningDetails

```java
/** Samples for Firmware GenerateBinaryHardeningDetails. */
public final class FirmwareGenerateBinaryHardeningDetailsSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_GenerateBinaryHardeningDetails_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firmware_GenerateBinaryHardeningDetails_MaximumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareGenerateBinaryHardeningDetailsMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .generateBinaryHardeningDetailsWithResponse(
                "rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_GenerateBinaryHardeningDetails_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firmware_GenerateBinaryHardeningDetails_MinimumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareGenerateBinaryHardeningDetailsMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .generateBinaryHardeningDetailsWithResponse(
                "rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE);
    }
}
```

### Firmware_GenerateBinaryHardeningSummary

```java
/** Samples for Firmware GenerateBinaryHardeningSummary. */
public final class FirmwareGenerateBinaryHardeningSummarySamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_GenerateBinaryHardeningSummary_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firmware_GenerateBinaryHardeningSummary_MaximumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareGenerateBinaryHardeningSummaryMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .generateBinaryHardeningSummaryWithResponse(
                "rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_GenerateBinaryHardeningSummary_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firmware_GenerateBinaryHardeningSummary_MinimumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareGenerateBinaryHardeningSummaryMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .generateBinaryHardeningSummaryWithResponse(
                "rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE);
    }
}
```

### Firmware_GenerateComponentDetails

```java
/** Samples for Firmware GenerateComponentDetails. */
public final class FirmwareGenerateComponentDetailsSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_GenerateComponentDetails_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firmware_GenerateComponentDetails_MinimumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareGenerateComponentDetailsMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .generateComponentDetailsWithResponse(
                "rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_GenerateComponentDetails_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firmware_GenerateComponentDetails_MaximumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareGenerateComponentDetailsMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .generateComponentDetailsWithResponse(
                "rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE);
    }
}
```

### Firmware_GenerateCryptoCertificateSummary

```java
/** Samples for Firmware GenerateCryptoCertificateSummary. */
public final class FirmwareGenerateCryptoCertificateSummarySamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_GenerateCryptoCertificateSummary_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firmware_GenerateCryptoCertificateSummary_MaximumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareGenerateCryptoCertificateSummaryMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .generateCryptoCertificateSummaryWithResponse(
                "FirmwareAnalysisRG",
                "default",
                "DECAFBAD-0000-0000-0000-BADBADBADBAD",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_GenerateCryptoCertificateSummary_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firmware_GenerateCryptoCertificateSummary_MinimumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareGenerateCryptoCertificateSummaryMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .generateCryptoCertificateSummaryWithResponse(
                "rgworkspaces-firmwares", "j5QE_", "wujtpcgypfpqseyrsebolarkspy", com.azure.core.util.Context.NONE);
    }
}
```

### Firmware_GenerateCryptoKeySummary

```java
/** Samples for Firmware GenerateCryptoKeySummary. */
public final class FirmwareGenerateCryptoKeySummarySamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_GenerateCryptoKeySummary_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firmware_GenerateCryptoKeySummary_MaximumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareGenerateCryptoKeySummaryMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .generateCryptoKeySummaryWithResponse(
                "FirmwareAnalysisRG",
                "default",
                "DECAFBAD-0000-0000-0000-BADBADBADBAD",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_GenerateCryptoKeySummary_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firmware_GenerateCryptoKeySummary_MinimumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareGenerateCryptoKeySummaryMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .generateCryptoKeySummaryWithResponse(
                "rgworkspaces-firmwares", "j5QE_", "wujtpcgypfpqseyrsebolarkspy", com.azure.core.util.Context.NONE);
    }
}
```

### Firmware_GenerateCveSummary

```java
/** Samples for Firmware GenerateCveSummary. */
public final class FirmwareGenerateCveSummarySamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_GenerateCveSummary_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firmware_GenerateCveSummary_MinimumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareGenerateCveSummaryMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .generateCveSummaryWithResponse(
                "rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_GenerateCveSummary_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firmware_GenerateCveSummary_MaximumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareGenerateCveSummaryMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .generateCveSummaryWithResponse(
                "rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE);
    }
}
```

### Firmware_GenerateDownloadUrl

```java
/** Samples for Firmware GenerateDownloadUrl. */
public final class FirmwareGenerateDownloadUrlSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_GenerateDownloadUrl_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firmware_GenerateDownloadUrl_MaximumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareGenerateDownloadUrlMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .generateDownloadUrlWithResponse(
                "rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_GenerateDownloadUrl_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firmware_GenerateDownloadUrl_MinimumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareGenerateDownloadUrlMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .generateDownloadUrlWithResponse(
                "rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE);
    }
}
```

### Firmware_GenerateFilesystemDownloadUrl

```java
/** Samples for Firmware GenerateFilesystemDownloadUrl. */
public final class FirmwareGenerateFilesystemDownloadUrlSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_GenerateFilesystemDownloadUrl_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firmware_GenerateFilesystemDownloadUrl_MaximumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareGenerateFilesystemDownloadUrlMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .generateFilesystemDownloadUrlWithResponse(
                "rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_GenerateFilesystemDownloadUrl_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firmware_GenerateFilesystemDownloadUrl_MinimumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareGenerateFilesystemDownloadUrlMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .generateFilesystemDownloadUrlWithResponse(
                "rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE);
    }
}
```

### Firmware_GenerateSummary

```java
/** Samples for Firmware GenerateSummary. */
public final class FirmwareGenerateSummarySamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_GenerateSummary_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firmware_GenerateSummary_MinimumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareGenerateSummaryMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .generateSummaryWithResponse("rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_GenerateSummary_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firmware_GenerateSummary_MaximumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareGenerateSummaryMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .generateSummaryWithResponse("rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE);
    }
}
```

### Firmware_Get

```java
/** Samples for Firmware Get. */
public final class FirmwareGetSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firmware_Get_MinimumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareGetMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .getWithResponse("rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firmware_Get_MaximumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareGetMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .getWithResponse("rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE);
    }
}
```

### Firmware_ListByWorkspace

```java
/** Samples for Firmware ListByWorkspace. */
public final class FirmwareListByWorkspaceSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_ListByWorkspace_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firmware_ListByWorkspace_MaximumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareListByWorkspaceMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.firmwares().listByWorkspace("rgworkspaces-firmwares", "A7", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_ListByWorkspace_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firmware_ListByWorkspace_MinimumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareListByWorkspaceMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.firmwares().listByWorkspace("rgworkspaces-firmwares", "A7", com.azure.core.util.Context.NONE);
    }
}
```

### Firmware_ListGenerateBinaryHardeningList

```java
/** Samples for Firmware ListGenerateBinaryHardeningList. */
public final class FirmwareListGenerateBinaryHardeningListSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_ListGenerateBinaryHardeningList_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firmware_ListGenerateBinaryHardeningList_MinimumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareListGenerateBinaryHardeningListMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .listGenerateBinaryHardeningList(
                "rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_ListGenerateBinaryHardeningList_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firmware_ListGenerateBinaryHardeningList_MaximumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareListGenerateBinaryHardeningListMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .listGenerateBinaryHardeningList(
                "rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE);
    }
}
```

### Firmware_ListGenerateComponentList

```java
/** Samples for Firmware ListGenerateComponentList. */
public final class FirmwareListGenerateComponentListSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_ListGenerateComponentList_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firmware_ListGenerateComponentList_MaximumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareListGenerateComponentListMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .listGenerateComponentList("rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_ListGenerateComponentList_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firmware_ListGenerateComponentList_MinimumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareListGenerateComponentListMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .listGenerateComponentList("rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE);
    }
}
```

### Firmware_ListGenerateCryptoCertificateList

```java
/** Samples for Firmware ListGenerateCryptoCertificateList. */
public final class FirmwareListGenerateCryptoCertificateListSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_ListGenerateCryptoCertificateList_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firmware_ListGenerateCryptoCertificateList_MaximumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareListGenerateCryptoCertificateListMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .listGenerateCryptoCertificateList(
                "FirmwareAnalysisRG",
                "default",
                "DECAFBAD-0000-0000-0000-BADBADBADBAD",
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_ListGenerateCryptoCertificateList_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firmware_ListGenerateCryptoCertificateList_MinimumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareListGenerateCryptoCertificateListMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .listGenerateCryptoCertificateList(
                "rgworkspaces-firmwares", "j5QE_", "wujtpcgypfpqseyrsebolarkspy", com.azure.core.util.Context.NONE);
    }
}
```

### Firmware_ListGenerateCryptoKeyList

```java
/** Samples for Firmware ListGenerateCryptoKeyList. */
public final class FirmwareListGenerateCryptoKeyListSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_ListGenerateCryptoKeyList_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firmware_ListGenerateCryptoKeyList_MinimumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareListGenerateCryptoKeyListMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .listGenerateCryptoKeyList(
                "rgworkspaces-firmwares", "j5QE_", "wujtpcgypfpqseyrsebolarkspy", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_ListGenerateCryptoKeyList_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firmware_ListGenerateCryptoKeyList_MaximumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareListGenerateCryptoKeyListMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .listGenerateCryptoKeyList(
                "FirmwareAnalysisRG",
                "default",
                "DECAFBAD-0000-0000-0000-BADBADBADBAD",
                com.azure.core.util.Context.NONE);
    }
}
```

### Firmware_ListGenerateCveList

```java
/** Samples for Firmware ListGenerateCveList. */
public final class FirmwareListGenerateCveListSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_ListGenerateCveList_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firmware_ListGenerateCveList_MaximumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareListGenerateCveListMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .listGenerateCveList("rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_ListGenerateCveList_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firmware_ListGenerateCveList_MinimumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareListGenerateCveListMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .listGenerateCveList("rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE);
    }
}
```

### Firmware_ListGeneratePasswordHashList

```java
/** Samples for Firmware ListGeneratePasswordHashList. */
public final class FirmwareListGeneratePasswordHashListSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_ListGeneratePasswordHashList_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firmware_ListGeneratePasswordHashList_MinimumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareListGeneratePasswordHashListMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .listGeneratePasswordHashList("rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_ListGeneratePasswordHashList_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firmware_ListGeneratePasswordHashList_MaximumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareListGeneratePasswordHashListMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .firmwares()
            .listGeneratePasswordHashList("rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE);
    }
}
```

### Firmware_Update

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.iotfirmwaredefense.models.Firmware;
import com.azure.resourcemanager.iotfirmwaredefense.models.Status;
import java.io.IOException;
import java.util.Arrays;

/** Samples for Firmware Update. */
public final class FirmwareUpdateSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Firmware_Update_MaximumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareUpdateMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) throws IOException {
        Firmware resource =
            manager
                .firmwares()
                .getWithResponse("rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withFileName("wresexxulcdsdd")
            .withVendor("vycmdhgtmepcptyoubztiuudpkcpd")
            .withModel("f")
            .withVersion("s")
            .withDescription("uz")
            .withFileSize(17L)
            .withStatus(Status.PENDING)
            .withStatusMessages(
                Arrays
                    .asList(
                        SerializerFactory
                            .createDefaultManagementSerializerAdapter()
                            .deserialize(
                                "{\"message\":\"ulvhmhokezathzzauiitu\"}", Object.class, SerializerEncoding.JSON)))
            .apply();
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Firmware_Update_MinimumSet_Gen.json
     */
    /**
     * Sample code: Firmware_Update_MinimumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void firmwareUpdateMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        Firmware resource =
            manager
                .firmwares()
                .getWithResponse("rgworkspaces-firmwares", "A7", "umrkdttp", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().apply();
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Operations_List_MinimumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MinimumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void operationsListMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Operations_List_MaximumSet_Gen.json
     */
    /**
     * Sample code: Operations_List_MaximumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void operationsListMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_Create

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for Workspaces Create. */
public final class WorkspacesCreateSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Workspaces_Create_MinimumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_Create_MinimumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void workspacesCreateMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .workspaces()
            .define("E___-3")
            .withRegion("jjwbseilitjgdrhbvvkwviqj")
            .withExistingResourceGroup("rgworkspaces")
            .create();
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Workspaces_Create_MaximumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_Create_MaximumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void workspacesCreateMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .workspaces()
            .define("E___-3")
            .withRegion("jjwbseilitjgdrhbvvkwviqj")
            .withExistingResourceGroup("rgworkspaces")
            .withTags(mapOf("key450", "fakeTokenPlaceholder"))
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

### Workspaces_Delete

```java
/** Samples for Workspaces Delete. */
public final class WorkspacesDeleteSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Workspaces_Delete_MinimumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_Delete_MinimumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void workspacesDeleteMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .workspaces()
            .deleteByResourceGroupWithResponse("rgworkspaces", "E___-3", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Workspaces_Delete_MaximumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_Delete_MaximumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void workspacesDeleteMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .workspaces()
            .deleteByResourceGroupWithResponse("rgworkspaces", "E___-3", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_GenerateUploadUrl

```java
import com.azure.resourcemanager.iotfirmwaredefense.models.GenerateUploadUrlRequest;

/** Samples for Workspaces GenerateUploadUrl. */
public final class WorkspacesGenerateUploadUrlSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Workspaces_GenerateUploadUrl_MinimumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_GenerateUploadUrl_MinimumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void workspacesGenerateUploadUrlMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .workspaces()
            .generateUploadUrlWithResponse(
                "rgworkspaces", "E___-3", new GenerateUploadUrlRequest(), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Workspaces_GenerateUploadUrl_MaximumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_GenerateUploadUrl_MaximumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void workspacesGenerateUploadUrlMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager
            .workspaces()
            .generateUploadUrlWithResponse(
                "rgworkspaces",
                "E___-3",
                new GenerateUploadUrlRequest().withFirmwareId("ytsfprbywi"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_GetByResourceGroup

```java
/** Samples for Workspaces GetByResourceGroup. */
public final class WorkspacesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Workspaces_Get_MaximumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_Get_MaximumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void workspacesGetMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.workspaces().getByResourceGroupWithResponse("rgworkspaces", "E_US", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Workspaces_Get_MinimumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_Get_MinimumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void workspacesGetMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        manager.workspaces().getByResourceGroupWithResponse("rgworkspaces", "E_US", com.azure.core.util.Context.NONE);
    }
}
```

### Workspaces_List

```java
/** Samples for Workspaces List. */
public final class WorkspacesListSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Workspaces_ListBySubscription_MaximumSet_Gen.json
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
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Workspaces_ListBySubscription_MinimumSet_Gen.json
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
/** Samples for Workspaces ListByResourceGroup. */
public final class WorkspacesListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Workspaces_ListByResourceGroup_MaximumSet_Gen.json
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
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Workspaces_ListByResourceGroup_MinimumSet_Gen.json
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

/** Samples for Workspaces Update. */
public final class WorkspacesUpdateSamples {
    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Workspaces_Update_MaximumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_Update_MaximumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void workspacesUpdateMaximumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        Workspace resource =
            manager
                .workspaces()
                .getByResourceGroupWithResponse("rgworkspaces", "E___-3", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().apply();
    }

    /*
     * x-ms-original-file: specification/fist/resource-manager/Microsoft.IoTFirmwareDefense/preview/2023-02-08-preview/examples/Workspaces_Update_MinimumSet_Gen.json
     */
    /**
     * Sample code: Workspaces_Update_MinimumSet_Gen.
     *
     * @param manager Entry point to IoTFirmwareDefenseManager.
     */
    public static void workspacesUpdateMinimumSetGen(
        com.azure.resourcemanager.iotfirmwaredefense.IoTFirmwareDefenseManager manager) {
        Workspace resource =
            manager
                .workspaces()
                .getByResourceGroupWithResponse("rgworkspaces", "E___-3", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().apply();
    }
}
```

