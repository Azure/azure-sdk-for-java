# Code snippets and samples


## CloudManifestFile

- [Get](#cloudmanifestfile_get)
- [List](#cloudmanifestfile_list)

## CustomerSubscriptions

- [Create](#customersubscriptions_create)
- [Delete](#customersubscriptions_delete)
- [Get](#customersubscriptions_get)
- [List](#customersubscriptions_list)

## DeploymentLicense

- [Create](#deploymentlicense_create)

## Operations

- [List](#operations_list)

## Products

- [Get](#products_get)
- [GetProduct](#products_getproduct)
- [GetProducts](#products_getproducts)
- [List](#products_list)
- [ListDetails](#products_listdetails)
- [ListProducts](#products_listproducts)
- [UploadLog](#products_uploadlog)

## Registrations

- [CreateOrUpdate](#registrations_createorupdate)
- [Delete](#registrations_delete)
- [EnableRemoteManagement](#registrations_enableremotemanagement)
- [GetActivationKey](#registrations_getactivationkey)
- [GetByResourceGroup](#registrations_getbyresourcegroup)
- [List](#registrations_list)
- [ListByResourceGroup](#registrations_listbyresourcegroup)
- [Update](#registrations_update)
### CloudManifestFile_Get

```java
/** Samples for CloudManifestFile Get. */
public final class CloudManifestFileGetSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/stable/2022-06-01/examples/CloudManifestFile/Get.json
     */
    /**
     * Sample code: Returns the properties of a cloud specific manifest file.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsThePropertiesOfACloudSpecificManifestFile(
        com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager.cloudManifestFiles().getWithResponse("latest", null, com.azure.core.util.Context.NONE);
    }
}
```

### CloudManifestFile_List

```java
/** Samples for CloudManifestFile List. */
public final class CloudManifestFileListSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/stable/2022-06-01/examples/CloudManifestFile/List.json
     */
    /**
     * Sample code: Returns the properties of a cloud specific manifest file with latest version.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsThePropertiesOfACloudSpecificManifestFileWithLatestVersion(
        com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager.cloudManifestFiles().listWithResponse(com.azure.core.util.Context.NONE);
    }
}
```

### CustomerSubscriptions_Create

```java
/** Samples for CustomerSubscriptions Create. */
public final class CustomerSubscriptionsCreateSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/stable/2022-06-01/examples/CustomerSubscription/Put.json
     */
    /**
     * Sample code: Creates a new customer subscription under a registration.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void createsANewCustomerSubscriptionUnderARegistration(
        com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager
            .customerSubscriptions()
            .define("E09A4E93-29A7-4EBA-A6D4-76202383F07F")
            .withExistingRegistration("azurestack", "testregistration")
            .withTenantId("dbab3982-796f-4d03-9908-044c08aef8a2")
            .create();
    }
}
```

### CustomerSubscriptions_Delete

```java
/** Samples for CustomerSubscriptions Delete. */
public final class CustomerSubscriptionsDeleteSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/stable/2022-06-01/examples/CustomerSubscription/Delete.json
     */
    /**
     * Sample code: Deletes a customer subscription under a registration.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void deletesACustomerSubscriptionUnderARegistration(
        com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager
            .customerSubscriptions()
            .deleteWithResponse(
                "azurestack",
                "testregistration",
                "E09A4E93-29A7-4EBA-A6D4-76202383F07F",
                com.azure.core.util.Context.NONE);
    }
}
```

### CustomerSubscriptions_Get

```java
/** Samples for CustomerSubscriptions Get. */
public final class CustomerSubscriptionsGetSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/stable/2022-06-01/examples/CustomerSubscription/Get.json
     */
    /**
     * Sample code: Returns the specified product.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsTheSpecifiedProduct(com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager
            .customerSubscriptions()
            .getWithResponse(
                "azurestack",
                "testregistration",
                "E09A4E93-29A7-4EBA-A6D4-76202383F07F",
                com.azure.core.util.Context.NONE);
    }
}
```

### CustomerSubscriptions_List

```java
/** Samples for CustomerSubscriptions List. */
public final class CustomerSubscriptionsListSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/stable/2022-06-01/examples/CustomerSubscription/List.json
     */
    /**
     * Sample code: Returns a list of products.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsAListOfProducts(com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager.customerSubscriptions().list("azurestack", "testregistration", com.azure.core.util.Context.NONE);
    }
}
```

### DeploymentLicense_Create

```java
import com.azure.resourcemanager.azurestack.models.DeploymentLicenseRequest;

/** Samples for DeploymentLicense Create. */
public final class DeploymentLicenseCreateSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/stable/2022-06-01/examples/DeploymentLicense/Post.json
     */
    /**
     * Sample code: Creates a license that can be used to deploy an Azure Stack device.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void createsALicenseThatCanBeUsedToDeployAnAzureStackDevice(
        com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager
            .deploymentLicenses()
            .createWithResponse(
                new DeploymentLicenseRequest().withVerificationVersion("1"), com.azure.core.util.Context.NONE);
    }
}
```

### Operations_List

```java
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/stable/2022-06-01/examples/Operation/List.json
     */
    /**
     * Sample code: Returns the list of supported REST operations.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsTheListOfSupportedRESTOperations(
        com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Products_Get

```java
/** Samples for Products Get. */
public final class ProductsGetSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/stable/2022-06-01/examples/Product/Get.json
     */
    /**
     * Sample code: Returns the specified product.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsTheSpecifiedProduct(com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager
            .products()
            .getWithResponse(
                "azurestack",
                "testregistration",
                "Microsoft.OSTCExtensions.VMAccessForLinux.1.4.7.1",
                com.azure.core.util.Context.NONE);
    }
}
```

### Products_GetProduct

```java
/** Samples for Products GetProduct. */
public final class ProductsGetProductSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/stable/2022-06-01/examples/Product/GetPost.json
     */
    /**
     * Sample code: Returns the specified product.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsTheSpecifiedProduct(com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager
            .products()
            .getProductWithResponse(
                "azurestack",
                "testregistration",
                "Microsoft.OSTCExtensions.VMAccessForLinux.1.4.7.1",
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Products_GetProducts

```java
/** Samples for Products GetProducts. */
public final class ProductsGetProductsSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/stable/2022-06-01/examples/Product/ListPost.json
     */
    /**
     * Sample code: Returns a list of products.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsAListOfProducts(com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager
            .products()
            .getProductsWithResponse("azurestack", "testregistration", "_all", null, com.azure.core.util.Context.NONE);
    }
}
```

### Products_List

```java
/** Samples for Products List. */
public final class ProductsListSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/stable/2022-06-01/examples/Product/List.json
     */
    /**
     * Sample code: Returns a list of products.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsAListOfProducts(com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager.products().list("azurestack", "testregistration", com.azure.core.util.Context.NONE);
    }
}
```

### Products_ListDetails

```java
/** Samples for Products ListDetails. */
public final class ProductsListDetailsSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/stable/2022-06-01/examples/Product/Post.json
     */
    /**
     * Sample code: Returns the extended properties of a product.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsTheExtendedPropertiesOfAProduct(
        com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager
            .products()
            .listDetailsWithResponse(
                "azurestack",
                "testregistration",
                "Microsoft.OSTCExtensions.VMAccessForLinux.1.4.7.1",
                com.azure.core.util.Context.NONE);
    }
}
```

### Products_ListProducts

```java
/** Samples for Products ListProducts. */
public final class ProductsListProductsSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/stable/2022-06-01/examples/Product/ListPost.json
     */
    /**
     * Sample code: Returns a list of products.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsAListOfProducts(com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager
            .products()
            .listProductsWithResponse("azurestack", "testregistration", "_all", null, com.azure.core.util.Context.NONE);
    }
}
```

### Products_UploadLog

```java
/** Samples for Products UploadLog. */
public final class ProductsUploadLogSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/stable/2022-06-01/examples/Product/UploadLog.json
     */
    /**
     * Sample code: Returns the specified product.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsTheSpecifiedProduct(com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager
            .products()
            .uploadLogWithResponse(
                "azurestack",
                "testregistration",
                "Microsoft.OSTCExtensions.VMAccessForLinux.1.4.7.1",
                null,
                com.azure.core.util.Context.NONE);
    }
}
```

### Registrations_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestack.models.Location;

/** Samples for Registrations CreateOrUpdate. */
public final class RegistrationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/stable/2022-06-01/examples/Registration/Put.json
     */
    /**
     * Sample code: Create or update an Azure Stack registration.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void createOrUpdateAnAzureStackRegistration(
        com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager
            .registrations()
            .define("testregistration")
            .withLocation(Location.GLOBAL)
            .withExistingResourceGroup("azurestack")
            .withRegistrationToken(
                "EyjIAWXSAw5nTw9KZWWiOiJeZxZlbg9wBwvUdCiSIM9iaMVjdeLkijoinwIzyJa2Ytgtowm2yy00OdG4lTlLyJmtztHjZGfJZTC0NZK1iIWiY2XvdWRJzCi6iJy5nDy0oDk1LTNHmWeTnDUwyS05oDI0LTrINzYwoGq5mjAzziIsim1HCmtldHBsYwnLu3LuZGljYXrpB25FBmfIbgVkIJp0CNvLLCJOYXJkd2FYzuLUZM8iOlt7IM51bunvcMVZiJoYlCjcaw9ZiJPBIjNkzDJHmda3yte5ndqZMdq4YmZkZmi5oDM3OTY3ZwNMIL0SIM5PyYI6WyJLZTy0ztJJMwZKy2m0OWNLODDLMwm2zTm0ymzKyjmWySisiJA3njlHmtdlY2q4NjRjnwFIZtC1YZi5ZGyZodM3Y2vjIl0siMnwDsi6wyi2oDUZoTbiY2RhNDa0ymrKoWe4YtK5otblzWrJzGyzNCISIjmYnzC4M2vmnZdIoDRKM2i5ytfkmJlhnDc1zdhLzWm1il0sim5HBwuiOijIqzF1MTvhmDIXmIIsimrpc2SiolsioWNlZjVhnZM1otQ0nDu3NmjlN2M3zmfjzmyZMTJhZtiiLcjLZjLmmZJhmWVhytG0NTu0OTqZNWu1Mda0MZbIYtfjyijdLCj1DWlKijoinwM5Mwu3NjytMju5Os00oTIwlWi0OdmTnGzHotiWm2RjyTCxIIwiBWvTb3J5ijPbijAYZDA3M2fjNzu0YTRMZTfhodkxzDnkogY5ZtAWzdyXIiwINZcWzThLnDQ4otrJndAzZGI5MGzlYtY1ZJA5ZdfiNMQIXX1DlcJpC3n1zxiiOijZb21lB25LIIWIdmVyC2LVbiI6IJeuMcJ9")
            .create();
    }
}
```

### Registrations_Delete

```java
/** Samples for Registrations Delete. */
public final class RegistrationsDeleteSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/stable/2022-06-01/examples/Registration/Delete.json
     */
    /**
     * Sample code: Delete the requested Azure Stack registration.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void deleteTheRequestedAzureStackRegistration(
        com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager
            .registrations()
            .deleteByResourceGroupWithResponse("azurestack", "testregistration", com.azure.core.util.Context.NONE);
    }
}
```

### Registrations_EnableRemoteManagement

```java
/** Samples for Registrations EnableRemoteManagement. */
public final class RegistrationsEnableRemoteManagementSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/stable/2022-06-01/examples/RemoteManagement/Post.json
     */
    /**
     * Sample code: Returns empty response for successful action..
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsEmptyResponseForSuccessfulAction(
        com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager
            .registrations()
            .enableRemoteManagementWithResponse("azurestack", "testregistration", com.azure.core.util.Context.NONE);
    }
}
```

### Registrations_GetActivationKey

```java
/** Samples for Registrations GetActivationKey. */
public final class RegistrationsGetActivationKeySamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/stable/2022-06-01/examples/Registration/Post.json
     */
    /**
     * Sample code: Returns Azure Stack Activation Key.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsAzureStackActivationKey(com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager
            .registrations()
            .getActivationKeyWithResponse("azurestack", "testregistration", com.azure.core.util.Context.NONE);
    }
}
```

### Registrations_GetByResourceGroup

```java
/** Samples for Registrations GetByResourceGroup. */
public final class RegistrationsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/stable/2022-06-01/examples/Registration/Get.json
     */
    /**
     * Sample code: Returns the properties of an Azure Stack registration.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsThePropertiesOfAnAzureStackRegistration(
        com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager
            .registrations()
            .getByResourceGroupWithResponse("azurestack", "testregistration", com.azure.core.util.Context.NONE);
    }
}
```

### Registrations_List

```java
/** Samples for Registrations List. */
public final class RegistrationsListSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/stable/2022-06-01/examples/Registration/ListBySubscription.json
     */
    /**
     * Sample code: Returns a list of all registrations under current subscription.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsAListOfAllRegistrationsUnderCurrentSubscription(
        com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager.registrations().list(com.azure.core.util.Context.NONE);
    }
}
```

### Registrations_ListByResourceGroup

```java
/** Samples for Registrations ListByResourceGroup. */
public final class RegistrationsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/stable/2022-06-01/examples/Registration/List.json
     */
    /**
     * Sample code: Returns a list of all registrations.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsAListOfAllRegistrations(com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager.registrations().listByResourceGroup("azurestack", com.azure.core.util.Context.NONE);
    }
}
```

### Registrations_Update

```java
import com.azure.resourcemanager.azurestack.models.Registration;

/** Samples for Registrations Update. */
public final class RegistrationsUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/stable/2022-06-01/examples/Registration/Patch.json
     */
    /**
     * Sample code: Patch an Azure Stack registration.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void patchAnAzureStackRegistration(com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        Registration resource =
            manager
                .registrations()
                .getByResourceGroupWithResponse("azurestack", "testregistration", com.azure.core.util.Context.NONE)
                .getValue();
        resource
            .update()
            .withRegistrationToken(
                "EyjIAWXSAw5nTw9KZWWiOiJeZxZlbg9wBwvUdCiSIM9iaMVjdeLkijoinwIzyJa2Ytgtowm2yy00OdG4lTlLyJmtztHjZGfJZTC0NZK1iIWiY2XvdWRJzCi6iJy5nDy0oDk1LTNHmWeTnDUwyS05oDI0LTrINzYwoGq5mjAzziIsim1HCmtldHBsYwnLu3LuZGljYXrpB25FBmfIbgVkIJp0CNvLLCJOYXJkd2FYzuLUZM8iOlt7IM51bunvcMVZiJoYlCjcaw9ZiJPBIjNkzDJHmda3yte5ndqZMdq4YmZkZmi5oDM3OTY3ZwNMIL0SIM5PyYI6WyJLZTy0ztJJMwZKy2m0OWNLODDLMwm2zTm0ymzKyjmWySisiJA3njlHmtdlY2q4NjRjnwFIZtC1YZi5ZGyZodM3Y2vjIl0siMnwDsi6wyi2oDUZoTbiY2RhNDa0ymrKoWe4YtK5otblzWrJzGyzNCISIjmYnzC4M2vmnZdIoDRKM2i5ytfkmJlhnDc1zdhLzWm1il0sim5HBwuiOijIqzF1MTvhmDIXmIIsimrpc2SiolsioWNlZjVhnZM1otQ0nDu3NmjlN2M3zmfjzmyZMTJhZtiiLcjLZjLmmZJhmWVhytG0NTu0OTqZNWu1Mda0MZbIYtfjyijdLCj1DWlKijoinwM5Mwu3NjytMju5Os00oTIwlWi0OdmTnGzHotiWm2RjyTCxIIwiBWvTb3J5ijPbijAYZDA3M2fjNzu0YTRMZTfhodkxzDnkogY5ZtAWzdyXIiwINZcWzThLnDQ4otrJndAzZGI5MGzlYtY1ZJA5ZdfiNMQIXX1DlcJpC3n1zxiiOijZb21lB25LIIWIdmVyC2LVbiI6IJeuMcJ9")
            .apply();
    }
}
```

