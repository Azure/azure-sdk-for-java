# Code snippets and samples


## CloudManifestFile

- [Get](#cloudmanifestfile_get)
- [List](#cloudmanifestfile_list)

## CustomerSubscriptions

- [Create](#customersubscriptions_create)
- [Delete](#customersubscriptions_delete)
- [Get](#customersubscriptions_get)
- [List](#customersubscriptions_list)

## LinkedSubscriptions

- [CreateOrUpdate](#linkedsubscriptions_createorupdate)
- [Delete](#linkedsubscriptions_delete)
- [GetByResourceGroup](#linkedsubscriptions_getbyresourcegroup)
- [List](#linkedsubscriptions_list)
- [ListByResourceGroup](#linkedsubscriptions_listbyresourcegroup)
- [Update](#linkedsubscriptions_update)

## Operations

- [List](#operations_list)

## Products

- [Get](#products_get)
- [GetProduct](#products_getproduct)
- [GetProducts](#products_getproducts)
- [List](#products_list)
- [ListDetails](#products_listdetails)
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
import com.azure.core.util.Context;

/** Samples for CloudManifestFile Get. */
public final class CloudManifestFileGetSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/preview/2020-06-01-preview/examples/CloudManifestFile/Get.json
     */
    /**
     * Sample code: Returns the properties of a cloud specific manifest file.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsThePropertiesOfACloudSpecificManifestFile(
        com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager.cloudManifestFiles().getWithResponse("latest", null, Context.NONE);
    }
}
```

### CloudManifestFile_List

```java
import com.azure.core.util.Context;

/** Samples for CloudManifestFile List. */
public final class CloudManifestFileListSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/preview/2020-06-01-preview/examples/CloudManifestFile/List.json
     */
    /**
     * Sample code: Returns the properties of a cloud specific manifest file with latest version.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsThePropertiesOfACloudSpecificManifestFileWithLatestVersion(
        com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager.cloudManifestFiles().listWithResponse(Context.NONE);
    }
}
```

### CustomerSubscriptions_Create

```java
/** Samples for CustomerSubscriptions Create. */
public final class CustomerSubscriptionsCreateSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/preview/2020-06-01-preview/examples/CustomerSubscription/Put.json
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
import com.azure.core.util.Context;

/** Samples for CustomerSubscriptions Delete. */
public final class CustomerSubscriptionsDeleteSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/preview/2020-06-01-preview/examples/CustomerSubscription/Delete.json
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
            .deleteWithResponse("azurestack", "testregistration", "E09A4E93-29A7-4EBA-A6D4-76202383F07F", Context.NONE);
    }
}
```

### CustomerSubscriptions_Get

```java
import com.azure.core.util.Context;

/** Samples for CustomerSubscriptions Get. */
public final class CustomerSubscriptionsGetSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/preview/2020-06-01-preview/examples/CustomerSubscription/Get.json
     */
    /**
     * Sample code: Returns the specified product.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsTheSpecifiedProduct(com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager
            .customerSubscriptions()
            .getWithResponse("azurestack", "testregistration", "E09A4E93-29A7-4EBA-A6D4-76202383F07F", Context.NONE);
    }
}
```

### CustomerSubscriptions_List

```java
import com.azure.core.util.Context;

/** Samples for CustomerSubscriptions List. */
public final class CustomerSubscriptionsListSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/preview/2020-06-01-preview/examples/CustomerSubscription/List.json
     */
    /**
     * Sample code: Returns a list of products.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsAListOfProducts(com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager.customerSubscriptions().list("azurestack", "testregistration", Context.NONE);
    }
}
```

### LinkedSubscriptions_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestack.models.Location;

/** Samples for LinkedSubscriptions CreateOrUpdate. */
public final class LinkedSubscriptionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/preview/2020-06-01-preview/examples/LinkedSubscription/Put.json
     */
    /**
     * Sample code: Create or update a Linked Subscription.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void createOrUpdateALinkedSubscription(
        com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager
            .linkedSubscriptions()
            .define("testLinkedSubscription")
            .withLocation(Location.fromString("eastus"))
            .withExistingResourceGroup("azurestack")
            .withLinkedSubscriptionId("104fbb77-2b0e-476a-83de-65ad8acd1f0b")
            .withRegistrationResourceId(
                "/subscriptions/dd8597b4-8739-4467-8b10-f8679f62bfbf/resourceGroups/azurestack/providers/Microsoft.AzureStack/registrations/testRegistration")
            .create();
    }
}
```

### LinkedSubscriptions_Delete

```java
import com.azure.core.util.Context;

/** Samples for LinkedSubscriptions Delete. */
public final class LinkedSubscriptionsDeleteSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/preview/2020-06-01-preview/examples/LinkedSubscription/Delete.json
     */
    /**
     * Sample code: Delete the requested Linked Subscription.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void deleteTheRequestedLinkedSubscription(
        com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager.linkedSubscriptions().deleteWithResponse("azurestack", "testlinkedsubscription", Context.NONE);
    }
}
```

### LinkedSubscriptions_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for LinkedSubscriptions GetByResourceGroup. */
public final class LinkedSubscriptionsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/preview/2020-06-01-preview/examples/LinkedSubscription/Get.json
     */
    /**
     * Sample code: Returns the properties of a Linked Subscription resource.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsThePropertiesOfALinkedSubscriptionResource(
        com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager
            .linkedSubscriptions()
            .getByResourceGroupWithResponse("azurestack", "testLinkedSubscription", Context.NONE);
    }
}
```

### LinkedSubscriptions_List

```java
import com.azure.core.util.Context;

/** Samples for LinkedSubscriptions List. */
public final class LinkedSubscriptionsListSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/preview/2020-06-01-preview/examples/LinkedSubscription/ListBySubscription.json
     */
    /**
     * Sample code: Returns a list of all linked subscriptions.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsAListOfAllLinkedSubscriptions(
        com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager.linkedSubscriptions().list(Context.NONE);
    }
}
```

### LinkedSubscriptions_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for LinkedSubscriptions ListByResourceGroup. */
public final class LinkedSubscriptionsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/preview/2020-06-01-preview/examples/LinkedSubscription/List.json
     */
    /**
     * Sample code: Returns a list of all linked subscriptions.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsAListOfAllLinkedSubscriptions(
        com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager.linkedSubscriptions().listByResourceGroup("azurestack", Context.NONE);
    }
}
```

### LinkedSubscriptions_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.azurestack.models.LinkedSubscription;

/** Samples for LinkedSubscriptions Update. */
public final class LinkedSubscriptionsUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/preview/2020-06-01-preview/examples/LinkedSubscription/Patch.json
     */
    /**
     * Sample code: Patch a Linked Subscription resource.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void patchALinkedSubscriptionResource(
        com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        LinkedSubscription resource =
            manager
                .linkedSubscriptions()
                .getByResourceGroupWithResponse("azurestack", "testLinkedSubscription", Context.NONE)
                .getValue();
        resource
            .update()
            .withLinkedSubscriptionId("104fbb77-2b0e-476a-83de-65ad8acd1f0b")
            .withRegistrationResourceId(
                "/subscriptions/dd8597b4-8739-4467-8b10-f8679f62bfbf/resourceGroups/azurestack/providers/Microsoft.AzureStack/registrations/testRegistration")
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
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/preview/2020-06-01-preview/examples/Operation/List.json
     */
    /**
     * Sample code: Returns the list of supported REST operations.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsTheListOfSupportedRESTOperations(
        com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager.operations().list(Context.NONE);
    }
}
```

### Products_Get

```java
import com.azure.core.util.Context;

/** Samples for Products Get. */
public final class ProductsGetSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/preview/2020-06-01-preview/examples/Product/Get.json
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
                "azurestack", "testregistration", "Microsoft.OSTCExtensions.VMAccessForLinux.1.4.7.1", Context.NONE);
    }
}
```

### Products_GetProduct

```java
import com.azure.core.util.Context;

/** Samples for Products GetProduct. */
public final class ProductsGetProductSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/preview/2020-06-01-preview/examples/Product/GetPost.json
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
                Context.NONE);
    }
}
```

### Products_GetProducts

```java
import com.azure.core.util.Context;

/** Samples for Products GetProducts. */
public final class ProductsGetProductsSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/preview/2020-06-01-preview/examples/Product/ListPost.json
     */
    /**
     * Sample code: Returns a list of products.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsAListOfProducts(com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager.products().getProductsWithResponse("azurestack", "testregistration", "_all", null, Context.NONE);
    }
}
```

### Products_List

```java
import com.azure.core.util.Context;

/** Samples for Products List. */
public final class ProductsListSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/preview/2020-06-01-preview/examples/Product/List.json
     */
    /**
     * Sample code: Returns a list of products.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsAListOfProducts(com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager.products().list("azurestack", "testregistration", Context.NONE);
    }
}
```

### Products_ListDetails

```java
import com.azure.core.util.Context;

/** Samples for Products ListDetails. */
public final class ProductsListDetailsSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/preview/2020-06-01-preview/examples/Product/Post.json
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
                "azurestack", "testregistration", "Microsoft.OSTCExtensions.VMAccessForLinux.1.4.7.1", Context.NONE);
    }
}
```

### Products_UploadLog

```java
import com.azure.core.util.Context;

/** Samples for Products UploadLog. */
public final class ProductsUploadLogSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/preview/2020-06-01-preview/examples/Product/UploadLog.json
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
                Context.NONE);
    }
}
```

### Registrations_CreateOrUpdate

```java
import com.azure.resourcemanager.azurestack.models.Location;

/** Samples for Registrations CreateOrUpdate. */
public final class RegistrationsCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/preview/2020-06-01-preview/examples/Registration/Put.json
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
import com.azure.core.util.Context;

/** Samples for Registrations Delete. */
public final class RegistrationsDeleteSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/preview/2020-06-01-preview/examples/Registration/Delete.json
     */
    /**
     * Sample code: Delete the requested Azure Stack registration.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void deleteTheRequestedAzureStackRegistration(
        com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager.registrations().deleteWithResponse("azurestack", "testregistration", Context.NONE);
    }
}
```

### Registrations_EnableRemoteManagement

```java
import com.azure.core.util.Context;

/** Samples for Registrations EnableRemoteManagement. */
public final class RegistrationsEnableRemoteManagementSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/preview/2020-06-01-preview/examples/RemoteManagement/Post.json
     */
    /**
     * Sample code: Returns empty response for successful action..
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsEmptyResponseForSuccessfulAction(
        com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager.registrations().enableRemoteManagementWithResponse("azurestack", "testregistration", Context.NONE);
    }
}
```

### Registrations_GetActivationKey

```java
import com.azure.core.util.Context;

/** Samples for Registrations GetActivationKey. */
public final class RegistrationsGetActivationKeySamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/preview/2020-06-01-preview/examples/Registration/Post.json
     */
    /**
     * Sample code: Returns Azure Stack Activation Key.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsAzureStackActivationKey(com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager.registrations().getActivationKeyWithResponse("azurestack", "testregistration", Context.NONE);
    }
}
```

### Registrations_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Registrations GetByResourceGroup. */
public final class RegistrationsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/preview/2020-06-01-preview/examples/Registration/Get.json
     */
    /**
     * Sample code: Returns the properties of an Azure Stack registration.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsThePropertiesOfAnAzureStackRegistration(
        com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager.registrations().getByResourceGroupWithResponse("azurestack", "testregistration", Context.NONE);
    }
}
```

### Registrations_List

```java
import com.azure.core.util.Context;

/** Samples for Registrations List. */
public final class RegistrationsListSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/preview/2020-06-01-preview/examples/Registration/ListBySubscription.json
     */
    /**
     * Sample code: Returns a list of all registrations under current subscription.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsAListOfAllRegistrationsUnderCurrentSubscription(
        com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager.registrations().list(Context.NONE);
    }
}
```

### Registrations_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for Registrations ListByResourceGroup. */
public final class RegistrationsListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/preview/2020-06-01-preview/examples/Registration/List.json
     */
    /**
     * Sample code: Returns a list of all registrations.
     *
     * @param manager Entry point to AzureStackManager.
     */
    public static void returnsAListOfAllRegistrations(com.azure.resourcemanager.azurestack.AzureStackManager manager) {
        manager.registrations().listByResourceGroup("azurestack", Context.NONE);
    }
}
```

### Registrations_Update

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.azurestack.models.Registration;

/** Samples for Registrations Update. */
public final class RegistrationsUpdateSamples {
    /*
     * x-ms-original-file: specification/azurestack/resource-manager/Microsoft.AzureStack/preview/2020-06-01-preview/examples/Registration/Patch.json
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
                .getByResourceGroupWithResponse("azurestack", "testregistration", Context.NONE)
                .getValue();
        resource
            .update()
            .withRegistrationToken(
                "EyjIAWXSAw5nTw9KZWWiOiJeZxZlbg9wBwvUdCiSIM9iaMVjdeLkijoinwIzyJa2Ytgtowm2yy00OdG4lTlLyJmtztHjZGfJZTC0NZK1iIWiY2XvdWRJzCi6iJy5nDy0oDk1LTNHmWeTnDUwyS05oDI0LTrINzYwoGq5mjAzziIsim1HCmtldHBsYwnLu3LuZGljYXrpB25FBmfIbgVkIJp0CNvLLCJOYXJkd2FYzuLUZM8iOlt7IM51bunvcMVZiJoYlCjcaw9ZiJPBIjNkzDJHmda3yte5ndqZMdq4YmZkZmi5oDM3OTY3ZwNMIL0SIM5PyYI6WyJLZTy0ztJJMwZKy2m0OWNLODDLMwm2zTm0ymzKyjmWySisiJA3njlHmtdlY2q4NjRjnwFIZtC1YZi5ZGyZodM3Y2vjIl0siMnwDsi6wyi2oDUZoTbiY2RhNDa0ymrKoWe4YtK5otblzWrJzGyzNCISIjmYnzC4M2vmnZdIoDRKM2i5ytfkmJlhnDc1zdhLzWm1il0sim5HBwuiOijIqzF1MTvhmDIXmIIsimrpc2SiolsioWNlZjVhnZM1otQ0nDu3NmjlN2M3zmfjzmyZMTJhZtiiLcjLZjLmmZJhmWVhytG0NTu0OTqZNWu1Mda0MZbIYtfjyijdLCj1DWlKijoinwM5Mwu3NjytMju5Os00oTIwlWi0OdmTnGzHotiWm2RjyTCxIIwiBWvTb3J5ijPbijAYZDA3M2fjNzu0YTRMZTfhodkxzDnkogY5ZtAWzdyXIiwINZcWzThLnDQ4otrJndAzZGI5MGzlYtY1ZJA5ZdfiNMQIXX1DlcJpC3n1zxiiOijZb21lB25LIIWIdmVyC2LVbiI6IJeuMcJ9")
            .apply();
    }
}
```

