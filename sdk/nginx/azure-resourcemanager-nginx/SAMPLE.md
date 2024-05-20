# Code snippets and samples


## Certificates

- [CreateOrUpdate](#certificates_createorupdate)
- [Delete](#certificates_delete)
- [Get](#certificates_get)
- [List](#certificates_list)

## Configurations

- [Analysis](#configurations_analysis)
- [CreateOrUpdate](#configurations_createorupdate)
- [Delete](#configurations_delete)
- [Get](#configurations_get)
- [List](#configurations_list)

## Deployments

- [CreateOrUpdate](#deployments_createorupdate)
- [Delete](#deployments_delete)
- [GetByResourceGroup](#deployments_getbyresourcegroup)
- [List](#deployments_list)
- [ListByResourceGroup](#deployments_listbyresourcegroup)
- [Update](#deployments_update)

## Operations

- [List](#operations_list)
### Certificates_CreateOrUpdate

```java
import com.azure.resourcemanager.nginx.models.NginxCertificateProperties;

/**
 * Samples for Certificates CreateOrUpdate.
 */
public final class CertificatesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/preview/2024-01-01-preview/examples/Certificates_CreateOrUpdate.json
     */
    /**
     * Sample code: Certificates_CreateOrUpdate.
     * 
     * @param manager Entry point to NginxManager.
     */
    public static void certificatesCreateOrUpdate(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager.certificates()
            .define("default")
            .withExistingNginxDeployment("myResourceGroup", "myDeployment")
            .withProperties(new NginxCertificateProperties().withKeyVirtualPath("fakeTokenPlaceholder")
                .withCertificateVirtualPath("/src/cert/somePath.cert")
                .withKeyVaultSecretId("fakeTokenPlaceholder"))
            .create();
    }
}
```

### Certificates_Delete

```java
/**
 * Samples for Certificates Delete.
 */
public final class CertificatesDeleteSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/preview/2024-01-01-preview/examples/Certificates_Delete.json
     */
    /**
     * Sample code: Certificates_Delete.
     * 
     * @param manager Entry point to NginxManager.
     */
    public static void certificatesDelete(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager.certificates().delete("myResourceGroup", "myDeployment", "default", com.azure.core.util.Context.NONE);
    }
}
```

### Certificates_Get

```java
/**
 * Samples for Certificates Get.
 */
public final class CertificatesGetSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/preview/2024-01-01-preview/examples/Certificates_Get.json
     */
    /**
     * Sample code: Certificates_Get.
     * 
     * @param manager Entry point to NginxManager.
     */
    public static void certificatesGet(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager.certificates()
            .getWithResponse("myResourceGroup", "myDeployment", "default", com.azure.core.util.Context.NONE);
    }
}
```

### Certificates_List

```java
/**
 * Samples for Certificates List.
 */
public final class CertificatesListSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/preview/2024-01-01-preview/examples/Certificates_List.json
     */
    /**
     * Sample code: Certificates_List.
     * 
     * @param manager Entry point to NginxManager.
     */
    public static void certificatesList(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager.certificates().list("myResourceGroup", "myDeployment", com.azure.core.util.Context.NONE);
    }
}
```

### Configurations_Analysis

```java
import com.azure.resourcemanager.nginx.models.AnalysisCreate;
import com.azure.resourcemanager.nginx.models.AnalysisCreateConfig;
import com.azure.resourcemanager.nginx.models.NginxConfigurationFile;
import com.azure.resourcemanager.nginx.models.NginxConfigurationPackage;
import java.util.Arrays;

/**
 * Samples for Configurations Analysis.
 */
public final class ConfigurationsAnalysisSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/preview/2024-01-01-preview/examples/Configurations_Analysis.json
     */
    /**
     * Sample code: Configurations_Analysis.
     * 
     * @param manager Entry point to NginxManager.
     */
    public static void configurationsAnalysis(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager.configurations()
            .analysisWithResponse("myResourceGroup", "myDeployment", "default",
                new AnalysisCreate().withConfig(new AnalysisCreateConfig().withRootFile("/etc/nginx/nginx.conf")
                    .withFiles(Arrays.asList(
                        new NginxConfigurationFile().withContent("ABCDEF==").withVirtualPath("/etc/nginx/nginx.conf")))
                    .withPackageProperty(new NginxConfigurationPackage())),
                com.azure.core.util.Context.NONE);
    }
}
```

### Configurations_CreateOrUpdate

```java
import com.azure.resourcemanager.nginx.models.NginxConfigurationFile;
import com.azure.resourcemanager.nginx.models.NginxConfigurationPackage;
import com.azure.resourcemanager.nginx.models.NginxConfigurationProperties;
import java.util.Arrays;

/**
 * Samples for Configurations CreateOrUpdate.
 */
public final class ConfigurationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/preview/2024-01-01-preview/examples/Configurations_CreateOrUpdate.json
     */
    /**
     * Sample code: Configurations_CreateOrUpdate.
     * 
     * @param manager Entry point to NginxManager.
     */
    public static void configurationsCreateOrUpdate(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager.configurations()
            .define("default")
            .withExistingNginxDeployment("myResourceGroup", "myDeployment")
            .withProperties(new NginxConfigurationProperties()
                .withFiles(Arrays.asList(
                    new NginxConfigurationFile().withContent("ABCDEF==").withVirtualPath("/etc/nginx/nginx.conf")))
                .withPackageProperty(new NginxConfigurationPackage())
                .withRootFile("/etc/nginx/nginx.conf"))
            .create();
    }
}
```

### Configurations_Delete

```java
/**
 * Samples for Configurations Delete.
 */
public final class ConfigurationsDeleteSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/preview/2024-01-01-preview/examples/Configurations_Delete.json
     */
    /**
     * Sample code: Configurations_Delete.
     * 
     * @param manager Entry point to NginxManager.
     */
    public static void configurationsDelete(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager.configurations().delete("myResourceGroup", "myDeployment", "default", com.azure.core.util.Context.NONE);
    }
}
```

### Configurations_Get

```java
/**
 * Samples for Configurations Get.
 */
public final class ConfigurationsGetSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/preview/2024-01-01-preview/examples/Configurations_Get.json
     */
    /**
     * Sample code: Configurations_Get.
     * 
     * @param manager Entry point to NginxManager.
     */
    public static void configurationsGet(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager.configurations()
            .getWithResponse("myResourceGroup", "myDeployment", "default", com.azure.core.util.Context.NONE);
    }
}
```

### Configurations_List

```java
/**
 * Samples for Configurations List.
 */
public final class ConfigurationsListSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/preview/2024-01-01-preview/examples/Configurations_List.json
     */
    /**
     * Sample code: Configurations_List.
     * 
     * @param manager Entry point to NginxManager.
     */
    public static void configurationsList(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager.configurations().list("myResourceGroup", "myDeployment", com.azure.core.util.Context.NONE);
    }
}
```

### Deployments_CreateOrUpdate

```java
import com.azure.resourcemanager.nginx.models.AutoUpgradeProfile;
import com.azure.resourcemanager.nginx.models.NginxDeploymentProperties;
import com.azure.resourcemanager.nginx.models.NginxDeploymentScalingProperties;
import com.azure.resourcemanager.nginx.models.NginxDeploymentUserProfile;
import com.azure.resourcemanager.nginx.models.NginxFrontendIpConfiguration;
import com.azure.resourcemanager.nginx.models.NginxNetworkInterfaceConfiguration;
import com.azure.resourcemanager.nginx.models.NginxNetworkProfile;
import com.azure.resourcemanager.nginx.models.NginxPrivateIpAddress;
import com.azure.resourcemanager.nginx.models.NginxPrivateIpAllocationMethod;
import com.azure.resourcemanager.nginx.models.NginxPublicIpAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Deployments CreateOrUpdate.
 */
public final class DeploymentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/preview/2024-01-01-preview/examples/Deployments_Create.json
     */
    /**
     * Sample code: Deployments_Create.
     * 
     * @param manager Entry point to NginxManager.
     */
    public static void deploymentsCreate(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager.deployments()
            .define("myDeployment")
            .withRegion("West US")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf("Environment", "Dev"))
            .withProperties(new NginxDeploymentProperties().withManagedResourceGroup("myManagedResourceGroup")
                .withNetworkProfile(new NginxNetworkProfile()
                    .withFrontEndIpConfiguration(new NginxFrontendIpConfiguration()
                        .withPublicIpAddresses(Arrays.asList(new NginxPublicIpAddress().withId(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.Network/publicIPAddresses/myPublicIPAddress")))
                        .withPrivateIpAddresses(Arrays.asList(new NginxPrivateIpAddress()
                            .withPrivateIpAddress("1.1.1.1")
                            .withPrivateIpAllocationMethod(NginxPrivateIpAllocationMethod.STATIC)
                            .withSubnetId(
                                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.Network/virtualNetworks/myVnet/subnets/mySubnet"))))
                    .withNetworkInterfaceConfiguration(new NginxNetworkInterfaceConfiguration().withSubnetId(
                        "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/myResourceGroup/providers/Microsoft.Network/virtualNetworks/myVnet/subnets/mySubnet")))
                .withScalingProperties(new NginxDeploymentScalingProperties().withCapacity(10))
                .withAutoUpgradeProfile(new AutoUpgradeProfile().withUpgradeChannel("stable"))
                .withUserProfile(new NginxDeploymentUserProfile().withPreferredEmail("example@example.email")))
            .create();
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

### Deployments_Delete

```java
/**
 * Samples for Deployments Delete.
 */
public final class DeploymentsDeleteSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/preview/2024-01-01-preview/examples/Deployments_Delete.json
     */
    /**
     * Sample code: Deployments_Delete.
     * 
     * @param manager Entry point to NginxManager.
     */
    public static void deploymentsDelete(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager.deployments().delete("myResourceGroup", "myDeployment", com.azure.core.util.Context.NONE);
    }
}
```

### Deployments_GetByResourceGroup

```java
/**
 * Samples for Deployments GetByResourceGroup.
 */
public final class DeploymentsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/preview/2024-01-01-preview/examples/Deployments_Get.json
     */
    /**
     * Sample code: Deployments_Get.
     * 
     * @param manager Entry point to NginxManager.
     */
    public static void deploymentsGet(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager.deployments()
            .getByResourceGroupWithResponse("myResourceGroup", "myDeployment", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/preview/2024-01-01-preview/examples/Deployments_Get_AutoScale.json
     */
    /**
     * Sample code: Deployments_Get_AutoScale.
     * 
     * @param manager Entry point to NginxManager.
     */
    public static void deploymentsGetAutoScale(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager.deployments()
            .getByResourceGroupWithResponse("myResourceGroup", "myDeployment", com.azure.core.util.Context.NONE);
    }
}
```

### Deployments_List

```java
/**
 * Samples for Deployments List.
 */
public final class DeploymentsListSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/preview/2024-01-01-preview/examples/Deployments_List.json
     */
    /**
     * Sample code: Deployments_List.
     * 
     * @param manager Entry point to NginxManager.
     */
    public static void deploymentsList(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager.deployments().list(com.azure.core.util.Context.NONE);
    }
}
```

### Deployments_ListByResourceGroup

```java
/**
 * Samples for Deployments ListByResourceGroup.
 */
public final class DeploymentsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/preview/2024-01-01-preview/examples/Deployments_ListByResourceGroup.json
     */
    /**
     * Sample code: Deployments_ListByResourceGroup.
     * 
     * @param manager Entry point to NginxManager.
     */
    public static void deploymentsListByResourceGroup(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager.deployments().listByResourceGroup("myResourceGroup", com.azure.core.util.Context.NONE);
    }
}
```

### Deployments_Update

```java
import com.azure.resourcemanager.nginx.models.NginxDeployment;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Deployments Update.
 */
public final class DeploymentsUpdateSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/preview/2024-01-01-preview/examples/Deployments_Update.json
     */
    /**
     * Sample code: Deployments_Update.
     * 
     * @param manager Entry point to NginxManager.
     */
    public static void deploymentsUpdate(com.azure.resourcemanager.nginx.NginxManager manager) {
        NginxDeployment resource = manager.deployments()
            .getByResourceGroupWithResponse("myResourceGroup", "myDeployment", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("Environment", "Dev")).apply();
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

### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/preview/2024-01-01-preview/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     * 
     * @param manager Entry point to NginxManager.
     */
    public static void operationsList(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

