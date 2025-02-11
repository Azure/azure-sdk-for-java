# Code snippets and samples


## ResourceProvider

- [CancelOrderItem](#resourceprovider_cancelorderitem)
- [CreateAddress](#resourceprovider_createaddress)
- [CreateOrderItem](#resourceprovider_createorderitem)
- [Delete](#resourceprovider_delete)
- [DeleteOrderItemByName](#resourceprovider_deleteorderitembyname)
- [GetByResourceGroup](#resourceprovider_getbyresourcegroup)
- [GetOrderByName](#resourceprovider_getorderbyname)
- [GetOrderItemByName](#resourceprovider_getorderitembyname)
- [List](#resourceprovider_list)
- [ListByResourceGroup](#resourceprovider_listbyresourcegroup)
- [ListConfigurations](#resourceprovider_listconfigurations)
- [ListOperations](#resourceprovider_listoperations)
- [ListOrderAtResourceGroupLevel](#resourceprovider_listorderatresourcegrouplevel)
- [ListOrderAtSubscriptionLevel](#resourceprovider_listorderatsubscriptionlevel)
- [ListOrderItemsAtResourceGroupLevel](#resourceprovider_listorderitemsatresourcegrouplevel)
- [ListOrderItemsAtSubscriptionLevel](#resourceprovider_listorderitemsatsubscriptionlevel)
- [ListProductFamilies](#resourceprovider_listproductfamilies)
- [ListProductFamiliesMetadata](#resourceprovider_listproductfamiliesmetadata)
- [ReturnOrderItem](#resourceprovider_returnorderitem)
- [UpdateAddress](#resourceprovider_updateaddress)
- [UpdateOrderItem](#resourceprovider_updateorderitem)
### ResourceProvider_CancelOrderItem

```java
import com.azure.resourcemanager.edgeorder.models.CancellationReason;

/**
 * Samples for ResourceProvider CancelOrderItem.
 */
public final class ResourceProviderCancelOrderItemSamples {
    /*
     * x-ms-original-file:
     * specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/CancelOrderItem.json
     */
    /**
     * Sample code: CancelOrderItem.
     * 
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void cancelOrderItem(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders()
            .cancelOrderItemWithResponse("TestOrderItemName3", "YourResourceGroupName",
                new CancellationReason().withReason("Order cancelled"), com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_CreateAddress

```java
import com.azure.resourcemanager.edgeorder.models.AddressType;
import com.azure.resourcemanager.edgeorder.models.ContactDetails;
import com.azure.resourcemanager.edgeorder.models.ShippingAddress;
import java.util.Arrays;

/**
 * Samples for ResourceProvider CreateAddress.
 */
public final class ResourceProviderCreateAddressSamples {
    /*
     * x-ms-original-file:
     * specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/CreateAddress.json
     */
    /**
     * Sample code: CreateAddress.
     * 
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void createAddress(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders()
            .defineAddress("TestAddressName2")
            .withRegion("eastus")
            .withExistingResourceGroup("YourResourceGroupName")
            .withContactDetails(new ContactDetails().withContactName("XXXX XXXX")
                .withPhone("0000000000")
                .withPhoneExtension("")
                .withEmailList(Arrays.asList("xxxx@xxxx.xxx")))
            .withShippingAddress(new ShippingAddress().withStreetAddress1("16 TOWNSEND ST")
                .withStreetAddress2("UNIT 1")
                .withCity("San Francisco")
                .withStateOrProvince("CA")
                .withCountry("US")
                .withPostalCode("fakeTokenPlaceholder")
                .withCompanyName("Microsoft")
                .withAddressType(AddressType.NONE))
            .create();
    }
}
```

### ResourceProvider_CreateOrderItem

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
 * Samples for ResourceProvider CreateOrderItem.
 */
public final class ResourceProviderCreateOrderItemSamples {
    /*
     * x-ms-original-file:
     * specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/CreateOrderItem.json
     */
    /**
     * Sample code: CreateOrderItem.
     * 
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void createOrderItem(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders()
            .defineOrderItem("TestOrderItemName2")
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
            .withOrderId(
                "/subscriptions/YourSubscriptionId/resourceGroups/YourResourceGroupName/providers/Microsoft.EdgeOrder/locations/eastus/orders/TestOrderName2")
            .create();
    }
}
```

### ResourceProvider_Delete

```java
/**
 * Samples for ResourceProvider Delete.
 */
public final class ResourceProviderDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/DeleteAddressByName.json
     */
    /**
     * Sample code: DeleteAddressByName.
     * 
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void deleteAddressByName(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders()
            .delete("YourResourceGroupName", "TestAddressName1", com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_DeleteOrderItemByName

```java
/**
 * Samples for ResourceProvider DeleteOrderItemByName.
 */
public final class ResourceProviderDeleteOrderItemByNameSamples {
    /*
     * x-ms-original-file:
     * specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/DeleteOrderItemByName.
     * json
     */
    /**
     * Sample code: DeleteOrderItemByName.
     * 
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void deleteOrderItemByName(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders()
            .deleteOrderItemByName("TestOrderItemName3", "YourResourceGroupName", com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_GetByResourceGroup

```java
/**
 * Samples for ResourceProvider GetByResourceGroup.
 */
public final class ResourceProviderGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/GetAddressByName.json
     */
    /**
     * Sample code: GetAddressByName.
     * 
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void getAddressByName(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders()
            .getByResourceGroupWithResponse("YourResourceGroupName", "TestAddressName1",
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_GetOrderByName

```java
/**
 * Samples for ResourceProvider GetOrderByName.
 */
public final class ResourceProviderGetOrderByNameSamples {
    /*
     * x-ms-original-file:
     * specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/GetOrderByName.json
     */
    /**
     * Sample code: GetOrderByName.
     * 
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void getOrderByName(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders()
            .getOrderByNameWithResponse("TestOrderName3", "YourResourceGroupName", "eastus",
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_GetOrderItemByName

```java
/**
 * Samples for ResourceProvider GetOrderItemByName.
 */
public final class ResourceProviderGetOrderItemByNameSamples {
    /*
     * x-ms-original-file:
     * specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/GetOrderItemByName.json
     */
    /**
     * Sample code: GetOrderItemByName.
     * 
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void getOrderItemByName(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders()
            .getOrderItemByNameWithResponse("TestOrderItemName1", "YourResourceGroupName", null,
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_List

```java
/**
 * Samples for ResourceProvider List.
 */
public final class ResourceProviderListSamples {
    /*
     * x-ms-original-file: specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/
     * ListAddressesAtSubscriptionLevel.json
     */
    /**
     * Sample code: ListAddressesAtSubscriptionLevel.
     * 
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void listAddressesAtSubscriptionLevel(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders().list(null, null, com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_ListByResourceGroup

```java
/**
 * Samples for ResourceProvider ListByResourceGroup.
 */
public final class ResourceProviderListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/
     * ListAddressesAtResourceGroupLevel.json
     */
    /**
     * Sample code: ListAddressesAtResourceGroupLevel.
     * 
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void listAddressesAtResourceGroupLevel(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders()
            .listByResourceGroup("YourResourceGroupName", null, null, com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_ListConfigurations

```java
import com.azure.resourcemanager.edgeorder.models.ConfigurationFilters;
import com.azure.resourcemanager.edgeorder.models.ConfigurationsRequest;
import com.azure.resourcemanager.edgeorder.models.FilterableProperty;
import com.azure.resourcemanager.edgeorder.models.HierarchyInformation;
import com.azure.resourcemanager.edgeorder.models.SupportedFilterTypes;
import java.util.Arrays;

/**
 * Samples for ResourceProvider ListConfigurations.
 */
public final class ResourceProviderListConfigurationsSamples {
    /*
     * x-ms-original-file:
     * specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/ListConfigurations.json
     */
    /**
     * Sample code: ListConfigurations.
     * 
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void listConfigurations(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders()
            .listConfigurations(
                new ConfigurationsRequest().withConfigurationFilters(Arrays.asList(new ConfigurationFilters()
                    .withHierarchyInformation(new HierarchyInformation().withProductFamilyName("azurestackedge")
                        .withProductLineName("azurestackedge")
                        .withProductName("azurestackedgegpu"))
                    .withFilterableProperty(
                        Arrays.asList(new FilterableProperty().withType(SupportedFilterTypes.SHIP_TO_COUNTRIES)
                            .withSupportedValues(Arrays.asList("US")))))),
                null, com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_ListOperations

```java
/**
 * Samples for ResourceProvider ListOperations.
 */
public final class ResourceProviderListOperationsSamples {
    /*
     * x-ms-original-file:
     * specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/ListOperations.json
     */
    /**
     * Sample code: ListOperations.
     * 
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void listOperations(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders().listOperations(com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_ListOrderAtResourceGroupLevel

```java
/**
 * Samples for ResourceProvider ListOrderAtResourceGroupLevel.
 */
public final class ResourceProviderListOrderAtResourceGroupLevelSamples {
    /*
     * x-ms-original-file: specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/
     * ListOrderAtResourceGroupLevel.json
     */
    /**
     * Sample code: ListOrderAtResourceGroupLevel.
     * 
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void listOrderAtResourceGroupLevel(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders()
            .listOrderAtResourceGroupLevel("YourResourceGroupName", null, com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_ListOrderAtSubscriptionLevel

```java
/**
 * Samples for ResourceProvider ListOrderAtSubscriptionLevel.
 */
public final class ResourceProviderListOrderAtSubscriptionLevelSamples {
    /*
     * x-ms-original-file: specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/
     * ListOrderAtSubscriptionLevel.json
     */
    /**
     * Sample code: ListOrderAtSubscriptionLevel.
     * 
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void listOrderAtSubscriptionLevel(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders().listOrderAtSubscriptionLevel(null, com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_ListOrderItemsAtResourceGroupLevel

```java
/**
 * Samples for ResourceProvider ListOrderItemsAtResourceGroupLevel.
 */
public final class ResourceProviderListOrderItemsAtResourceGroupLevelSamples {
    /*
     * x-ms-original-file: specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/
     * ListOrderItemsAtResourceGroupLevel.json
     */
    /**
     * Sample code: ListOrderItemsAtResourceGroupLevel.
     * 
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void
        listOrderItemsAtResourceGroupLevel(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders()
            .listOrderItemsAtResourceGroupLevel("YourResourceGroupName", null, null, null,
                com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_ListOrderItemsAtSubscriptionLevel

```java
/**
 * Samples for ResourceProvider ListOrderItemsAtSubscriptionLevel.
 */
public final class ResourceProviderListOrderItemsAtSubscriptionLevelSamples {
    /*
     * x-ms-original-file: specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/
     * ListOrderItemsAtSubscriptionLevel.json
     */
    /**
     * Sample code: ListOrderItemsAtSubscriptionLevel.
     * 
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void listOrderItemsAtSubscriptionLevel(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders()
            .listOrderItemsAtSubscriptionLevel(null, null, null, com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_ListProductFamilies

```java
import com.azure.resourcemanager.edgeorder.models.FilterableProperty;
import com.azure.resourcemanager.edgeorder.models.ProductFamiliesRequest;
import com.azure.resourcemanager.edgeorder.models.SupportedFilterTypes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ResourceProvider ListProductFamilies.
 */
public final class ResourceProviderListProductFamiliesSamples {
    /*
     * x-ms-original-file:
     * specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/ListProductFamilies.json
     */
    /**
     * Sample code: ListProductFamilies.
     * 
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void listProductFamilies(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders()
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

### ResourceProvider_ListProductFamiliesMetadata

```java
/**
 * Samples for ResourceProvider ListProductFamiliesMetadata.
 */
public final class ResourceProviderListProductFamiliesMetadataSamples {
    /*
     * x-ms-original-file: specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/
     * ListProductFamiliesMetadata.json
     */
    /**
     * Sample code: ListProductFamiliesMetadata.
     * 
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void listProductFamiliesMetadata(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders().listProductFamiliesMetadata(null, com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_ReturnOrderItem

```java
import com.azure.resourcemanager.edgeorder.models.ReturnOrderItemDetails;

/**
 * Samples for ResourceProvider ReturnOrderItem.
 */
public final class ResourceProviderReturnOrderItemSamples {
    /*
     * x-ms-original-file:
     * specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/ReturnOrderItem.json
     */
    /**
     * Sample code: ReturnOrderItem.
     * 
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void returnOrderItem(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders()
            .returnOrderItem("TestOrderName4", "YourResourceGroupName",
                new ReturnOrderItemDetails().withReturnReason("Order returned"), com.azure.core.util.Context.NONE);
    }
}
```

### ResourceProvider_UpdateAddress

```java
import com.azure.resourcemanager.edgeorder.models.AddressResource;
import com.azure.resourcemanager.edgeorder.models.AddressType;
import com.azure.resourcemanager.edgeorder.models.ContactDetails;
import com.azure.resourcemanager.edgeorder.models.ShippingAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ResourceProvider UpdateAddress.
 */
public final class ResourceProviderUpdateAddressSamples {
    /*
     * x-ms-original-file:
     * specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/UpdateAddress.json
     */
    /**
     * Sample code: UpdateAddress.
     * 
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void updateAddress(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        AddressResource resource = manager.resourceProviders()
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

### ResourceProvider_UpdateOrderItem

```java
import com.azure.resourcemanager.edgeorder.models.OrderItemResource;
import com.azure.resourcemanager.edgeorder.models.Preferences;
import com.azure.resourcemanager.edgeorder.models.TransportPreferences;
import com.azure.resourcemanager.edgeorder.models.TransportShipmentTypes;

/**
 * Samples for ResourceProvider UpdateOrderItem.
 */
public final class ResourceProviderUpdateOrderItemSamples {
    /*
     * x-ms-original-file:
     * specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/UpdateOrderItem.json
     */
    /**
     * Sample code: UpdateOrderItem.
     * 
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void updateOrderItem(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        OrderItemResource resource = manager.resourceProviders()
            .getOrderItemByNameWithResponse("TestOrderItemName3", "YourResourceGroupName", null,
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withPreferences(new Preferences().withTransportPreferences(
                new TransportPreferences().withPreferredShipmentType(TransportShipmentTypes.CUSTOMER_MANAGED)))
            .apply();
    }
}
```

