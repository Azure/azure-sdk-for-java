# Code snippets and samples


## Addresses

- [Create](#addresses_create)
- [Delete](#addresses_delete)
- [GetByResourceGroup](#addresses_getbyresourcegroup)
- [List](#addresses_list)
- [ListByResourceGroup](#addresses_listbyresourcegroup)
- [Update](#addresses_update)

## Operations

- [List](#operations_list)

## OrderItems

- [Cancel](#orderitems_cancel)
- [Create](#orderitems_create)
- [Delete](#orderitems_delete)
- [GetByResourceGroup](#orderitems_getbyresourcegroup)
- [List](#orderitems_list)
- [ListByResourceGroup](#orderitems_listbyresourcegroup)
- [ReturnMethod](#orderitems_returnmethod)
- [Update](#orderitems_update)

## Orders

- [Get](#orders_get)
- [List](#orders_list)
- [ListByResourceGroup](#orders_listbyresourcegroup)

## ProductsAndConfigurations

- [ListConfigurations](#productsandconfigurations_listconfigurations)
- [ListProductFamilies](#productsandconfigurations_listproductfamilies)
- [ListProductFamiliesMetadata](#productsandconfigurations_listproductfamiliesmetadata)
### Addresses_Create

```java
import com.azure.resourcemanager.edgeorder.models.AddressClassification;
import com.azure.resourcemanager.edgeorder.models.AddressType;
import com.azure.resourcemanager.edgeorder.models.ContactDetails;
import com.azure.resourcemanager.edgeorder.models.ShippingAddress;
import java.util.Arrays;

/**
 * Samples for Addresses Create.
 */
public final class AddressesCreateSamples {
    /*
     * x-ms-original-file: 2024-02-01/CreateAddress.json
     */
    /**
     * Sample code: CreateAddress.
     * 
     * @param manager Entry point to EdgeorderManager.
     */
    public static void createAddress(com.azure.resourcemanager.edgeorder.EdgeorderManager manager) {
        manager.addresses()
            .define("TestAddressName2")
            .withRegion("eastus")
            .withExistingResourceGroup("YourResourceGroupName")
            .withAddressClassification(AddressClassification.SHIPPING)
            .withShippingAddress(new ShippingAddress().withStreetAddress1("16 TOWNSEND ST")
                .withStreetAddress2("UNIT 1")
                .withCity("San Francisco")
                .withStateOrProvince("CA")
                .withCountry("US")
                .withPostalCode("fakeTokenPlaceholder")
                .withCompanyName("Microsoft")
                .withAddressType(AddressType.NONE))
            .withContactDetails(new ContactDetails().withContactName("XXXX XXXX")
                .withPhone("0000000000")
                .withPhoneExtension("")
                .withEmailList(Arrays.asList("xxxx@xxxx.xxx")))
            .create();
    }
}
```

### Addresses_Delete

```java
/**
 * Samples for Addresses Delete.
 */
public final class AddressesDeleteSamples {
    /*
     * x-ms-original-file: 2024-02-01/DeleteAddressByName.json
     */
    /**
     * Sample code: DeleteAddressByName.
     * 
     * @param manager Entry point to EdgeorderManager.
     */
    public static void deleteAddressByName(com.azure.resourcemanager.edgeorder.EdgeorderManager manager) {
        manager.addresses().delete("YourResourceGroupName", "TestAddressName1", com.azure.core.util.Context.NONE);
    }
}
```

### Addresses_GetByResourceGroup

```java
/**
 * Samples for Addresses GetByResourceGroup.
 */
public final class AddressesGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-02-01/GetAddressByName.json
     */
    /**
     * Sample code: GetAddressByName.
     * 
     * @param manager Entry point to EdgeorderManager.
     */
    public static void getAddressByName(com.azure.resourcemanager.edgeorder.EdgeorderManager manager) {
        manager.addresses()
            .getByResourceGroupWithResponse("YourResourceGroupName", "TestAddressName1",
                com.azure.core.util.Context.NONE);
    }
}
```

### Addresses_List

```java
/**
 * Samples for Addresses List.
 */
public final class AddressesListSamples {
    /*
     * x-ms-original-file: 2024-02-01/ListAddressesAtSubscriptionLevel.json
     */
    /**
     * Sample code: ListAddressesAtSubscriptionLevel.
     * 
     * @param manager Entry point to EdgeorderManager.
     */
    public static void listAddressesAtSubscriptionLevel(com.azure.resourcemanager.edgeorder.EdgeorderManager manager) {
        manager.addresses().list(null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### Addresses_ListByResourceGroup

```java
/**
 * Samples for Addresses ListByResourceGroup.
 */
public final class AddressesListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-02-01/ListAddressesAtResourceGroupLevel.json
     */
    /**
     * Sample code: ListAddressesAtResourceGroupLevel.
     * 
     * @param manager Entry point to EdgeorderManager.
     */
    public static void listAddressesAtResourceGroupLevel(com.azure.resourcemanager.edgeorder.EdgeorderManager manager) {
        manager.addresses()
            .listByResourceGroup("YourResourceGroupName", null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### Addresses_Update

```java
import com.azure.resourcemanager.edgeorder.models.AddressResource;
import com.azure.resourcemanager.edgeorder.models.AddressType;
import com.azure.resourcemanager.edgeorder.models.ContactDetails;
import com.azure.resourcemanager.edgeorder.models.ShippingAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Addresses Update.
 */
public final class AddressesUpdateSamples {
    /*
     * x-ms-original-file: 2024-02-01/UpdateAddress.json
     */
    /**
     * Sample code: UpdateAddress.
     * 
     * @param manager Entry point to EdgeorderManager.
     */
    public static void updateAddress(com.azure.resourcemanager.edgeorder.EdgeorderManager manager) {
        AddressResource resource = manager.addresses()
            .getByResourceGroupWithResponse("YourResourceGroupName", "TestAddressName2",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("tag1", "value1", "tag2", "value2"))
            .withShippingAddress(new ShippingAddress().withStreetAddress1("16 TOWNSEND ST")
                .withStreetAddress2("UNIT 1")
                .withCity("San Francisco")
                .withStateOrProvince("CA")
                .withCountry("US")
                .withPostalCode("fakeTokenPlaceholder")
                .withCompanyName("Microsoft")
                .withAddressType(AddressType.NONE))
            .withContactDetails(new ContactDetails().withContactName("YYYY YYYY")
                .withPhone("0000000000")
                .withPhoneExtension("")
                .withEmailList(Arrays.asList("xxxx@xxxx.xxx")))
            .apply();
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
     * x-ms-original-file: 2024-02-01/ListOperations.json
     */
    /**
     * Sample code: ListOperations.
     * 
     * @param manager Entry point to EdgeorderManager.
     */
    public static void listOperations(com.azure.resourcemanager.edgeorder.EdgeorderManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### OrderItems_Cancel

```java
import com.azure.resourcemanager.edgeorder.models.CancellationReason;

/**
 * Samples for OrderItems Cancel.
 */
public final class OrderItemsCancelSamples {
    /*
     * x-ms-original-file: 2024-02-01/CancelOrderItem.json
     */
    /**
     * Sample code: CancelOrderItem.
     * 
     * @param manager Entry point to EdgeorderManager.
     */
    public static void cancelOrderItem(com.azure.resourcemanager.edgeorder.EdgeorderManager manager) {
        manager.orderItems()
            .cancelWithResponse("YourResourceGroupName", "TestOrderItemName3",
                new CancellationReason().withReason("Order cancelled"), com.azure.core.util.Context.NONE);
    }
}
```

### OrderItems_Create

```java
import com.azure.resourcemanager.edgeorder.fluent.models.AddressProperties;
import com.azure.resourcemanager.edgeorder.models.AddressDetails;
import com.azure.resourcemanager.edgeorder.models.AddressType;
import com.azure.resourcemanager.edgeorder.models.ContactDetails;
import com.azure.resourcemanager.edgeorder.models.HierarchyInformation;
import com.azure.resourcemanager.edgeorder.models.OrderItemDetails;
import com.azure.resourcemanager.edgeorder.models.OrderItemType;
import com.azure.resourcemanager.edgeorder.models.Preferences;
import com.azure.resourcemanager.edgeorder.models.ProductDetails;
import com.azure.resourcemanager.edgeorder.models.ShippingAddress;
import com.azure.resourcemanager.edgeorder.models.TransportPreferences;
import com.azure.resourcemanager.edgeorder.models.TransportShipmentTypes;
import java.util.Arrays;

/**
 * Samples for OrderItems Create.
 */
public final class OrderItemsCreateSamples {
    /*
     * x-ms-original-file: 2024-02-01/CreateOrderItem.json
     */
    /**
     * Sample code: CreateOrderItem.
     * 
     * @param manager Entry point to EdgeorderManager.
     */
    public static void createOrderItem(com.azure.resourcemanager.edgeorder.EdgeorderManager manager) {
        manager.orderItems()
            .define("TestOrderItemName2")
            .withRegion("eastus")
            .withExistingResourceGroup("YourResourceGroupName")
            .withOrderItemDetails(new OrderItemDetails()
                .withProductDetails(new ProductDetails()
                    .withHierarchyInformation(new HierarchyInformation().withProductFamilyName("azurestackedge")
                        .withProductLineName("azurestackedge")
                        .withProductName("azurestackedgegpu")
                        .withConfigurationName("edgep_base")))
                .withOrderItemType(OrderItemType.PURCHASE)
                .withPreferences(new Preferences().withTransportPreferences(
                    new TransportPreferences().withPreferredShipmentType(TransportShipmentTypes.MICROSOFT_MANAGED))))
            .withOrderId(
                "/subscriptions/eb5dc900-6186-49d8-b7d7-febd866fdc1d/resourceGroups/YourResourceGroupName/providers/Microsoft.EdgeOrder/locations/eastus/orders/TestOrderName2")
            .withAddressDetails(new AddressDetails().withForwardAddress(new AddressProperties()
                .withShippingAddress(new ShippingAddress().withStreetAddress1("16 TOWNSEND ST")
                    .withStreetAddress2("UNIT 1")
                    .withCity("San Francisco")
                    .withStateOrProvince("CA")
                    .withCountry("US")
                    .withPostalCode("fakeTokenPlaceholder")
                    .withCompanyName("Microsoft")
                    .withAddressType(AddressType.NONE))
                .withContactDetails(new ContactDetails().withContactName("XXXX XXXX")
                    .withPhone("0000000000")
                    .withPhoneExtension("")
                    .withEmailList(Arrays.asList("xxxx@xxxx.xxx")))))
            .create();
    }
}
```

### OrderItems_Delete

```java
/**
 * Samples for OrderItems Delete.
 */
public final class OrderItemsDeleteSamples {
    /*
     * x-ms-original-file: 2024-02-01/DeleteOrderItemByName.json
     */
    /**
     * Sample code: DeleteOrderItemByName.
     * 
     * @param manager Entry point to EdgeorderManager.
     */
    public static void deleteOrderItemByName(com.azure.resourcemanager.edgeorder.EdgeorderManager manager) {
        manager.orderItems().delete("YourResourceGroupName", "TestOrderItemName3", com.azure.core.util.Context.NONE);
    }
}
```

### OrderItems_GetByResourceGroup

```java
/**
 * Samples for OrderItems GetByResourceGroup.
 */
public final class OrderItemsGetByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-02-01/GetOrderItemByName.json
     */
    /**
     * Sample code: GetOrderItemByName.
     * 
     * @param manager Entry point to EdgeorderManager.
     */
    public static void getOrderItemByName(com.azure.resourcemanager.edgeorder.EdgeorderManager manager) {
        manager.orderItems()
            .getByResourceGroupWithResponse("YourResourceGroupName", "TestOrderItemName1", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### OrderItems_List

```java
/**
 * Samples for OrderItems List.
 */
public final class OrderItemsListSamples {
    /*
     * x-ms-original-file: 2024-02-01/ListOrderItemsAtSubscriptionLevel.json
     */
    /**
     * Sample code: ListOrderItemsAtSubscriptionLevel.
     * 
     * @param manager Entry point to EdgeorderManager.
     */
    public static void listOrderItemsAtSubscriptionLevel(com.azure.resourcemanager.edgeorder.EdgeorderManager manager) {
        manager.orderItems().list(null, null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### OrderItems_ListByResourceGroup

```java
/**
 * Samples for OrderItems ListByResourceGroup.
 */
public final class OrderItemsListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-02-01/ListOrderItemsAtResourceGroupLevel.json
     */
    /**
     * Sample code: ListOrderItemsAtResourceGroupLevel.
     * 
     * @param manager Entry point to EdgeorderManager.
     */
    public static void
        listOrderItemsAtResourceGroupLevel(com.azure.resourcemanager.edgeorder.EdgeorderManager manager) {
        manager.orderItems()
            .listByResourceGroup("YourResourceGroupName", null, null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### OrderItems_ReturnMethod

```java
import com.azure.resourcemanager.edgeorder.models.ReturnOrderItemDetails;

/**
 * Samples for OrderItems ReturnMethod.
 */
public final class OrderItemsReturnMethodSamples {
    /*
     * x-ms-original-file: 2024-02-01/ReturnOrderItem.json
     */
    /**
     * Sample code: ReturnOrderItem.
     * 
     * @param manager Entry point to EdgeorderManager.
     */
    public static void returnOrderItem(com.azure.resourcemanager.edgeorder.EdgeorderManager manager) {
        manager.orderItems()
            .returnMethod("YourResourceGroupName", "TestOrderName4",
                new ReturnOrderItemDetails().withReturnReason("Order returned"), com.azure.core.util.Context.NONE);
    }
}
```

### OrderItems_Update

```java
import com.azure.resourcemanager.edgeorder.models.OrderItemResource;
import com.azure.resourcemanager.edgeorder.models.Preferences;
import com.azure.resourcemanager.edgeorder.models.TransportPreferences;
import com.azure.resourcemanager.edgeorder.models.TransportShipmentTypes;

/**
 * Samples for OrderItems Update.
 */
public final class OrderItemsUpdateSamples {
    /*
     * x-ms-original-file: 2024-02-01/UpdateOrderItem.json
     */
    /**
     * Sample code: UpdateOrderItem.
     * 
     * @param manager Entry point to EdgeorderManager.
     */
    public static void updateOrderItem(com.azure.resourcemanager.edgeorder.EdgeorderManager manager) {
        OrderItemResource resource = manager.orderItems()
            .getByResourceGroupWithResponse("YourResourceGroupName", "TestOrderItemName3", null,
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withPreferences(new Preferences().withTransportPreferences(
                new TransportPreferences().withPreferredShipmentType(TransportShipmentTypes.CUSTOMER_MANAGED)))
            .apply();
    }
}
```

### Orders_Get

```java
/**
 * Samples for Orders Get.
 */
public final class OrdersGetSamples {
    /*
     * x-ms-original-file: 2024-02-01/GetOrderByName.json
     */
    /**
     * Sample code: GetOrderByName.
     * 
     * @param manager Entry point to EdgeorderManager.
     */
    public static void getOrderByName(com.azure.resourcemanager.edgeorder.EdgeorderManager manager) {
        manager.orders()
            .getWithResponse("YourResourceGroupName", "eastus", "TestOrderName3", com.azure.core.util.Context.NONE);
    }
}
```

### Orders_List

```java
/**
 * Samples for Orders List.
 */
public final class OrdersListSamples {
    /*
     * x-ms-original-file: 2024-02-01/ListOrderAtSubscriptionLevel.json
     */
    /**
     * Sample code: ListOrderAtSubscriptionLevel.
     * 
     * @param manager Entry point to EdgeorderManager.
     */
    public static void listOrderAtSubscriptionLevel(com.azure.resourcemanager.edgeorder.EdgeorderManager manager) {
        manager.orders().list(null, null, com.azure.core.util.Context.NONE);
    }
}
```

### Orders_ListByResourceGroup

```java
/**
 * Samples for Orders ListByResourceGroup.
 */
public final class OrdersListByResourceGroupSamples {
    /*
     * x-ms-original-file: 2024-02-01/ListOrderAtResourceGroupLevel.json
     */
    /**
     * Sample code: ListOrderAtResourceGroupLevel.
     * 
     * @param manager Entry point to EdgeorderManager.
     */
    public static void listOrderAtResourceGroupLevel(com.azure.resourcemanager.edgeorder.EdgeorderManager manager) {
        manager.orders().listByResourceGroup("YourResourceGroupName", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### ProductsAndConfigurations_ListConfigurations

```java
import com.azure.resourcemanager.edgeorder.models.ConfigurationFilter;
import com.azure.resourcemanager.edgeorder.models.ConfigurationsRequest;
import com.azure.resourcemanager.edgeorder.models.FilterableProperty;
import com.azure.resourcemanager.edgeorder.models.HierarchyInformation;
import com.azure.resourcemanager.edgeorder.models.SupportedFilterTypes;
import java.util.Arrays;

/**
 * Samples for ProductsAndConfigurations ListConfigurations.
 */
public final class ProductsAndConfigurationsListConfigurationsSamples {
    /*
     * x-ms-original-file: 2024-02-01/ListConfigurations.json
     */
    /**
     * Sample code: ListConfigurations.
     * 
     * @param manager Entry point to EdgeorderManager.
     */
    public static void listConfigurations(com.azure.resourcemanager.edgeorder.EdgeorderManager manager) {
        manager.productsAndConfigurations()
            .listConfigurations(new ConfigurationsRequest().withConfigurationFilter(new ConfigurationFilter()
                .withHierarchyInformation(new HierarchyInformation().withProductFamilyName("azurestackedge")
                    .withProductLineName("azurestackedge")
                    .withProductName("azurestackedgegpu"))
                .withFilterableProperty(
                    Arrays.asList(new FilterableProperty().withType(SupportedFilterTypes.SHIP_TO_COUNTRIES)
                        .withSupportedValues(Arrays.asList("US"))))),
                null, com.azure.core.util.Context.NONE);
    }
}
```

### ProductsAndConfigurations_ListProductFamilies

```java
import com.azure.resourcemanager.edgeorder.models.FilterableProperty;
import com.azure.resourcemanager.edgeorder.models.ProductFamiliesRequest;
import com.azure.resourcemanager.edgeorder.models.SupportedFilterTypes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ProductsAndConfigurations ListProductFamilies.
 */
public final class ProductsAndConfigurationsListProductFamiliesSamples {
    /*
     * x-ms-original-file: 2024-02-01/ListProductFamilies.json
     */
    /**
     * Sample code: ListProductFamilies.
     * 
     * @param manager Entry point to EdgeorderManager.
     */
    public static void listProductFamilies(com.azure.resourcemanager.edgeorder.EdgeorderManager manager) {
        manager.productsAndConfigurations()
            .listProductFamilies(
                new ProductFamiliesRequest().withFilterableProperties(mapOf("azurestackedge",
                    Arrays.asList(new FilterableProperty().withType(SupportedFilterTypes.SHIP_TO_COUNTRIES)
                        .withSupportedValues(Arrays.asList("US"))))),
                "configurations", null, com.azure.core.util.Context.NONE);
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

### ProductsAndConfigurations_ListProductFamiliesMetadata

```java
/**
 * Samples for ProductsAndConfigurations ListProductFamiliesMetadata.
 */
public final class ProductsAndConfigurationsListProductFamiliesMetadataSamples {
    /*
     * x-ms-original-file: 2024-02-01/ListProductFamiliesMetadata.json
     */
    /**
     * Sample code: ListProductFamiliesMetadata.
     * 
     * @param manager Entry point to EdgeorderManager.
     */
    public static void listProductFamiliesMetadata(com.azure.resourcemanager.edgeorder.EdgeorderManager manager) {
        manager.productsAndConfigurations().listProductFamiliesMetadata(null, com.azure.core.util.Context.NONE);
    }
}
```

