# Code snippets and samples


## Certificates

- [CreateOrUpdate](#certificates_createorupdate)
- [Delete](#certificates_delete)
- [Get](#certificates_get)
- [List](#certificates_list)

## Configurations

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
/** Samples for Certificates CreateOrUpdate. */
public final class CertificatesCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/stable/2022-08-01/examples/Certificates_CreateOrUpdate.json
     */
    /**
     * Sample code: Certificates_CreateOrUpdate.
     *
     * @param manager Entry point to NginxManager.
     */
    public static void certificatesCreateOrUpdate(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager
            .certificates()
            .define("default")
            .withRegion((String) null)
            .withExistingNginxDeployment("myResourceGroup", "myDeployment")
            .create();
    }
}
```

### Certificates_Delete

```java
import com.azure.core.util.Context;

/** Samples for Certificates Delete. */
public final class CertificatesDeleteSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/stable/2022-08-01/examples/Certificates_Delete.json
     */
    /**
     * Sample code: Certificates_Delete.
     *
     * @param manager Entry point to NginxManager.
     */
    public static void certificatesDelete(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager.certificates().delete("myResourceGroup", "myDeployment", "default", Context.NONE);
    }
}
```

### Certificates_Get

```java
import com.azure.core.util.Context;

/** Samples for Certificates Get. */
public final class CertificatesGetSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/stable/2022-08-01/examples/Certificates_Get.json
     */
    /**
     * Sample code: Certificates_Get.
     *
     * @param manager Entry point to NginxManager.
     */
    public static void certificatesGet(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager.certificates().getWithResponse("myResourceGroup", "myDeployment", "default", Context.NONE);
    }
}
```

### Certificates_List

```java
import com.azure.core.util.Context;

/** Samples for Certificates List. */
public final class CertificatesListSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/stable/2022-08-01/examples/Certificates_List.json
     */
    /**
     * Sample code: Certificates_List.
     *
     * @param manager Entry point to NginxManager.
     */
    public static void certificatesList(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager.certificates().list("myResourceGroup", "myDeployment", Context.NONE);
    }
}
```

### Configurations_CreateOrUpdate

```java
/** Samples for Configurations CreateOrUpdate. */
public final class ConfigurationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/stable/2022-08-01/examples/Configurations_CreateOrUpdate.json
     */
    /**
     * Sample code: Configurations_CreateOrUpdate.
     *
     * @param manager Entry point to NginxManager.
     */
    public static void configurationsCreateOrUpdate(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager
            .configurations()
            .define("default")
            .withRegion((String) null)
            .withExistingNginxDeployment("myResourceGroup", "myDeployment")
            .create();
    }
}
```

### Configurations_Delete

```java
import com.azure.core.util.Context;

/** Samples for Configurations Delete. */
public final class ConfigurationsDeleteSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/stable/2022-08-01/examples/Configurations_Delete.json
     */
    /**
     * Sample code: Configurations_Delete.
     *
     * @param manager Entry point to NginxManager.
     */
    public static void configurationsDelete(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager.configurations().delete("myResourceGroup", "myDeployment", "default", Context.NONE);
    }
}
```

### Configurations_Get

```java
import com.azure.core.util.Context;

/** Samples for Configurations Get. */
public final class ConfigurationsGetSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/stable/2022-08-01/examples/Configurations_Get.json
     */
    /**
     * Sample code: Configurations_Get.
     *
     * @param manager Entry point to NginxManager.
     */
    public static void configurationsGet(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager.configurations().getWithResponse("myResourceGroup", "myDeployment", "default", Context.NONE);
    }
}
```

### Configurations_List

```java
import com.azure.core.util.Context;

/** Samples for Configurations List. */
public final class ConfigurationsListSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/stable/2022-08-01/examples/Configurations_List.json
     */
    /**
     * Sample code: Configurations_List.
     *
     * @param manager Entry point to NginxManager.
     */
    public static void configurationsList(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager.configurations().list("myResourceGroup", "myDeployment", Context.NONE);
    }
}
```

### Deployments_CreateOrUpdate

```java
/** Samples for Deployments CreateOrUpdate. */
public final class DeploymentsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/stable/2022-08-01/examples/Deployments_Create.json
     */
    /**
     * Sample code: Deployments_Create.
     *
     * @param manager Entry point to NginxManager.
     */
    public static void deploymentsCreate(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager
            .deployments()
            .define("myDeployment")
            .withRegion((String) null)
            .withExistingResourceGroup("myResourceGroup")
            .create();
    }
}
```

### Deployments_Delete

```java
import com.azure.core.util.Context;

/** Samples for Deployments Delete. */
public final class DeploymentsDeleteSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/stable/2022-08-01/examples/Deployments_Delete.json
     */
    /**
     * Sample code: Deployments_Delete.
     *
     * @param manager Entry point to NginxManager.
     */
    public static void deploymentsDelete(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager.deployments().delete("myResourceGroup", "myDeployment", Context.NONE);
    }
}
```

### Deployments_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Deployments GetByResourceGroup. */
public final class DeploymentsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/stable/2022-08-01/examples/Deployments_Get.json
     */
    /**
     * Sample code: Deployments_Get.
     *
     * @param manager Entry point to NginxManager.
     */
    public static void deploymentsGet(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager.deployments().getByResourceGroupWithResponse("myResourceGroup", "myDeployment", Context.NONE);
    }
}
```

### Deployments_List

```java
import com.azure.core.util.Context;

/** Samples for Deployments List. */
public final class DeploymentsListSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/stable/2022-08-01/examples/Deployments_List.json
     */
    /**
     * Sample code: Deployments_List.
     *
     * @param manager Entry point to NginxManager.
     */
    public static void deploymentsList(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager.deployments().list(Context.NONE);
    }
}
```

### Deployments_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Deployments ListByResourceGroup. */
public final class DeploymentsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/stable/2022-08-01/examples/Deployments_ListByResourceGroup.json
     */
    /**
     * Sample code: Deployments_ListByResourceGroup.
     *
     * @param manager Entry point to NginxManager.
     */
    public static void deploymentsListByResourceGroup(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager.deployments().listByResourceGroup("myResourceGroup", Context.NONE);
    }
}
```

### Deployments_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.nginx.models.NginxDeployment;

/** Samples for Deployments Update. */
public final class DeploymentsUpdateSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/stable/2022-08-01/examples/Deployments_Update.json
     */
    /**
     * Sample code: Deployments_Update.
     *
     * @param manager Entry point to NginxManager.
     */
    public static void deploymentsUpdate(com.azure.resourcemanager.nginx.NginxManager manager) {
        NginxDeployment resource =
            manager
                .deployments()
                .getByResourceGroupWithResponse("myResourceGroup", "myDeployment", Context.NONE)
                .getValue();
        resource.update().apply();
    }
}
```

### Operations_List

```java
import com.azure.core.util.Context;

/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/nginx/resource-manager/NGINX.NGINXPLUS/stable/2022-08-01/examples/Operations_List.json
     */
    /**
     * Sample code: Operations_List.
     *
     * @param manager Entry point to NginxManager.
     */
    public static void operationsList(com.azure.resourcemanager.nginx.NginxManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

