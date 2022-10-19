# Code snippets and samples


## ArcSettings

- [Create](#arcsettings_create)
- [CreateIdentity](#arcsettings_createidentity)
- [Delete](#arcsettings_delete)
- [GeneratePassword](#arcsettings_generatepassword)
- [Get](#arcsettings_get)
- [ListByCluster](#arcsettings_listbycluster)
- [Update](#arcsettings_update)

## Clusters

- [Create](#clusters_create)
- [CreateIdentity](#clusters_createidentity)
- [Delete](#clusters_delete)
- [GetByResourceGroup](#clusters_getbyresourcegroup)
- [List](#clusters_list)
- [ListByResourceGroup](#clusters_listbyresourcegroup)
- [Update](#clusters_update)
- [UploadCertificate](#clusters_uploadcertificate)

## Extensions

- [Create](#extensions_create)
- [Delete](#extensions_delete)
- [Get](#extensions_get)
- [ListByArcSetting](#extensions_listbyarcsetting)
- [Update](#extensions_update)

## Operations

- [List](#operations_list)
### ArcSettings_Create

```java
/** Samples for ArcSettings Create. */
public final class ArcSettingsCreateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2022-05-01/examples/PutArcSetting.json
     */
    /**
     * Sample code: Create ArcSetting.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void createArcSetting(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.arcSettings().define("default").withExistingCluster("test-rg", "myCluster").create();
    }
}
```

### ArcSettings_CreateIdentity

```java
import com.azure.core.util.Context;

/** Samples for ArcSettings CreateIdentity. */
public final class ArcSettingsCreateIdentitySamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2022-05-01/examples/CreateArcIdentity.json
     */
    /**
     * Sample code: Create Arc Identity.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void createArcIdentity(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.arcSettings().createIdentity("test-rg", "myCluster", "default", Context.NONE);
    }
}
```

### ArcSettings_Delete

```java
import com.azure.core.util.Context;

/** Samples for ArcSettings Delete. */
public final class ArcSettingsDeleteSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2022-05-01/examples/DeleteArcSetting.json
     */
    /**
     * Sample code: Delete ArcSetting.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteArcSetting(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.arcSettings().delete("test-rg", "myCluster", "default", Context.NONE);
    }
}
```

### ArcSettings_GeneratePassword

```java
import com.azure.core.util.Context;

/** Samples for ArcSettings GeneratePassword. */
public final class ArcSettingsGeneratePasswordSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2022-05-01/examples/GeneratePassword.json
     */
    /**
     * Sample code: Generate Password.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void generatePassword(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.arcSettings().generatePasswordWithResponse("test-rg", "myCluster", "default", Context.NONE);
    }
}
```

### ArcSettings_Get

```java
import com.azure.core.util.Context;

/** Samples for ArcSettings Get. */
public final class ArcSettingsGetSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2022-05-01/examples/GetArcSetting.json
     */
    /**
     * Sample code: Get ArcSetting.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getArcSetting(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.arcSettings().getWithResponse("test-rg", "myCluster", "default", Context.NONE);
    }
}
```

### ArcSettings_ListByCluster

```java
import com.azure.core.util.Context;

/** Samples for ArcSettings ListByCluster. */
public final class ArcSettingsListByClusterSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2022-05-01/examples/ListArcSettingsByCluster.json
     */
    /**
     * Sample code: List ArcSetting resources by HCI Cluster.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listArcSettingResourcesByHCICluster(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.arcSettings().listByCluster("test-rg", "myCluster", Context.NONE);
    }
}
```

### ArcSettings_Update

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.azurestackhci.models.ArcSetting;
import java.io.IOException;

/** Samples for ArcSettings Update. */
public final class ArcSettingsUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2022-05-01/examples/PatchArcSetting.json
     */
    /**
     * Sample code: Patch ArcSetting.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void patchArcSetting(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager)
        throws IOException {
        ArcSetting resource =
            manager.arcSettings().getWithResponse("test-rg", "myCluster", "default", Context.NONE).getValue();
        resource
            .update()
            .withConnectivityProperties(
                SerializerFactory
                    .createDefaultManagementSerializerAdapter()
                    .deserialize("{\"enabled\":true}", Object.class, SerializerEncoding.JSON))
            .apply();
    }
}
```

### Clusters_Create

```java
/** Samples for Clusters Create. */
public final class ClustersCreateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2022-05-01/examples/CreateCluster.json
     */
    /**
     * Sample code: Create cluster.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void createCluster(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .clusters()
            .define("myCluster")
            .withRegion("East US")
            .withExistingResourceGroup("test-rg")
            .withCloudManagementEndpoint("https://98294836-31be-4668-aeae-698667faf99b.waconazure.com")
            .withAadClientId("24a6e53d-04e5-44d2-b7cc-1b732a847dfc")
            .withAadTenantId("7e589cc1-a8b6-4dff-91bd-5ec0fa18db94")
            .create();
    }
}
```

### Clusters_CreateIdentity

```java
import com.azure.core.util.Context;

/** Samples for Clusters CreateIdentity. */
public final class ClustersCreateIdentitySamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2022-05-01/examples/CreateClusterIdentity.json
     */
    /**
     * Sample code: Create cluster Identity.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void createClusterIdentity(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.clusters().createIdentity("test-rg", "myCluster", Context.NONE);
    }
}
```

### Clusters_Delete

```java
import com.azure.core.util.Context;

/** Samples for Clusters Delete. */
public final class ClustersDeleteSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2022-05-01/examples/DeleteCluster.json
     */
    /**
     * Sample code: Delete cluster.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteCluster(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.clusters().delete("test-rg", "myCluster", Context.NONE);
    }
}
```

### Clusters_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Clusters GetByResourceGroup. */
public final class ClustersGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2022-05-01/examples/GetCluster.json
     */
    /**
     * Sample code: Get cluster.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getCluster(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.clusters().getByResourceGroupWithResponse("test-rg", "myCluster", Context.NONE);
    }
}
```

### Clusters_List

```java
import com.azure.core.util.Context;

/** Samples for Clusters List. */
public final class ClustersListSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2022-05-01/examples/ListClustersBySubscription.json
     */
    /**
     * Sample code: List clusters in a given subscription.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listClustersInAGivenSubscription(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.clusters().list(Context.NONE);
    }
}
```

### Clusters_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Clusters ListByResourceGroup. */
public final class ClustersListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2022-05-01/examples/ListClustersByResourceGroup.json
     */
    /**
     * Sample code: List clusters in a given resource group.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listClustersInAGivenResourceGroup(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.clusters().listByResourceGroup("test-rg", Context.NONE);
    }
}
```

### Clusters_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.azurestackhci.models.Cluster;
import com.azure.resourcemanager.azurestackhci.models.ClusterDesiredProperties;
import com.azure.resourcemanager.azurestackhci.models.DiagnosticLevel;
import com.azure.resourcemanager.azurestackhci.models.WindowsServerSubscription;
import java.util.HashMap;
import java.util.Map;

/** Samples for Clusters Update. */
public final class ClustersUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2022-05-01/examples/UpdateCluster.json
     */
    /**
     * Sample code: Update cluster.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void updateCluster(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        Cluster resource =
            manager.clusters().getByResourceGroupWithResponse("test-rg", "myCluster", Context.NONE).getValue();
        resource
            .update()
            .withTags(mapOf("tag1", "value1", "tag2", "value2"))
            .withCloudManagementEndpoint("https://98294836-31be-4668-aeae-698667faf99b.waconazure.com")
            .withDesiredProperties(
                new ClusterDesiredProperties()
                    .withWindowsServerSubscription(WindowsServerSubscription.ENABLED)
                    .withDiagnosticLevel(DiagnosticLevel.BASIC))
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

### Clusters_UploadCertificate

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.azurestackhci.models.RawCertificateData;
import com.azure.resourcemanager.azurestackhci.models.UploadCertificateRequest;
import java.util.Arrays;

/** Samples for Clusters UploadCertificate. */
public final class ClustersUploadCertificateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2022-05-01/examples/UploadCertificate.json
     */
    /**
     * Sample code: Upload certificate.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void uploadCertificate(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .clusters()
            .uploadCertificate(
                "test-rg",
                "myCluster",
                new UploadCertificateRequest()
                    .withProperties(
                        new RawCertificateData().withCertificates(Arrays.asList("base64cert", "base64cert"))),
                Context.NONE);
    }
}
```

### Extensions_Create

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import java.io.IOException;

/** Samples for Extensions Create. */
public final class ExtensionsCreateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2022-05-01/examples/PutExtension.json
     */
    /**
     * Sample code: Create Arc Extension.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void createArcExtension(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager)
        throws IOException {
        manager
            .extensions()
            .define("MicrosoftMonitoringAgent")
            .withExistingArcSetting("test-rg", "myCluster", "default")
            .withPublisher("Microsoft.Compute")
            .withTypePropertiesType("MicrosoftMonitoringAgent")
            .withTypeHandlerVersion("1.10")
            .withSettings(
                SerializerFactory
                    .createDefaultManagementSerializerAdapter()
                    .deserialize("{\"workspaceId\":\"xx\"}", Object.class, SerializerEncoding.JSON))
            .withProtectedSettings(
                SerializerFactory
                    .createDefaultManagementSerializerAdapter()
                    .deserialize("{\"workspaceKey\":\"xx\"}", Object.class, SerializerEncoding.JSON))
            .create();
    }
}
```

### Extensions_Delete

```java
import com.azure.core.util.Context;

/** Samples for Extensions Delete. */
public final class ExtensionsDeleteSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2022-05-01/examples/DeleteExtension.json
     */
    /**
     * Sample code: Delete Arc Extension.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void deleteArcExtension(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.extensions().delete("test-rg", "myCluster", "default", "MicrosoftMonitoringAgent", Context.NONE);
    }
}
```

### Extensions_Get

```java
import com.azure.core.util.Context;

/** Samples for Extensions Get. */
public final class ExtensionsGetSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2022-05-01/examples/GetExtension.json
     */
    /**
     * Sample code: Get ArcSettings Extension.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void getArcSettingsExtension(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager
            .extensions()
            .getWithResponse("test-rg", "myCluster", "default", "MicrosoftMonitoringAgent", Context.NONE);
    }
}
```

### Extensions_ListByArcSetting

```java
import com.azure.core.util.Context;

/** Samples for Extensions ListByArcSetting. */
public final class ExtensionsListByArcSettingSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2022-05-01/examples/ListExtensionsByArcSetting.json
     */
    /**
     * Sample code: List Extensions under ArcSetting resource.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void listExtensionsUnderArcSettingResource(
        com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.extensions().listByArcSetting("test-rg", "myCluster", "default", Context.NONE);
    }
}
```

### Extensions_Update

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.azurestackhci.models.Extension;
import java.io.IOException;

/** Samples for Extensions Update. */
public final class ExtensionsUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2022-05-01/examples/PatchExtension.json
     */
    /**
     * Sample code: Update Arc Extension.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void updateArcExtension(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager)
        throws IOException {
        Extension resource =
            manager
                .extensions()
                .getWithResponse("test-rg", "myCluster", "default", "MicrosoftMonitoringAgent", Context.NONE)
                .getValue();
        resource
            .update()
            .withPublisher("Microsoft.Compute")
            .withTypePropertiesType("MicrosoftMonitoringAgent")
            .withTypeHandlerVersion("1.10")
            .withSettings(
                SerializerFactory
                    .createDefaultManagementSerializerAdapter()
                    .deserialize("{\"workspaceId\":\"xx\"}", Object.class, SerializerEncoding.JSON))
            .apply();
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/azurestackhci/resource-manager/Microsoft.AzureStackHCI/stable/2022-05-01/examples/ListOperations.json
     */
    /**
     * Sample code: Create cluster.
     *
     * @param manager Entry point to AzureStackHciManager.
     */
    public static void createCluster(com.azure.resourcemanager.azurestackhci.AzureStackHciManager manager) {
        manager.operations().listWithResponse(Context.NONE);
    }
}
```

