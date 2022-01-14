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
import com.azure.core.util.Context;
import com.azure.resourcemanager.edgeorder.models.CancellationReason;

/** Samples for ResourceProvider CancelOrderItem. */
public final class ResourceProviderCancelOrderItemSamples {
    /*
     * x-ms-original-file: specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/CancelOrderItem.json
     */
    /**
     * Sample code: CancelOrderItem.
     *
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void cancelOrderItem(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager
            .resourceProviders()
            .cancelOrderItemWithResponse(
                "TestOrderItemName1", "TestRG", new CancellationReason().withReason("Order cancelled"), Context.NONE);
    }
}
```

### ResourceProvider_CreateAddress

```java
import com.azure.resourcemanager.edgeorder.models.AddressType;
import com.azure.resourcemanager.edgeorder.models.ContactDetails;
import com.azure.resourcemanager.edgeorder.models.ShippingAddress;
import java.util.Arrays;

/** Samples for ResourceProvider CreateAddress. */
public final class ResourceProviderCreateAddressSamples {
    /*
     * x-ms-original-file: specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/CreateAddress.json
     */
    /**
     * Sample code: CreateAddress.
     *
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void createAddress(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager
            .resourceProviders()
            .defineAddress("TestMSAddressName")
            .withRegion("westus")
            .withExistingResourceGroup("TestRG")
            .withContactDetails(
                new ContactDetails()
                    .withContactName("Petr Cech")
                    .withPhone("1234567890")
                    .withPhoneExtension("")
                    .withEmailList(Arrays.asList("testemail@microsoft.com")))
            .withShippingAddress(
                new ShippingAddress()
                    .withStreetAddress1("16 TOWNSEND ST")
                    .withStreetAddress2("UNIT 1")
                    .withCity("San Francisco")
                    .withStateOrProvince("CA")
                    .withCountry("US")
                    .withPostalCode("94107")
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
import java.util.HashMap;
import java.util.Map;

/** Samples for ResourceProvider CreateOrderItem. */
public final class ResourceProviderCreateOrderItemSamples {
    /*
     * x-ms-original-file: specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/CreateOrderItem.json
     */
    /**
     * Sample code: CreateOrderItem.
     *
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void createOrderItem(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager
            .resourceProviders()
            .defineOrderItem("TestOrderItemName01")
            .withRegion("westus")
            .withExistingResourceGroup("TestRG")
            .withOrderItemDetails(
                new OrderItemDetails()
                    .withProductDetails(
                        new ProductDetails()
                            .withHierarchyInformation(
                                new HierarchyInformation()
                                    .withProductFamilyName("AzureStackEdge")
                                    .withProductLineName("AzureStackEdge")
                                    .withProductName("AzureStackEdgeGPU")
                                    .withConfigurationName("AzureStackEdgeGPU")))
                    .withOrderItemType(OrderItemType.PURCHASE)
                    .withPreferences(
                        new Preferences()
                            .withTransportPreferences(
                                new TransportPreferences()
                                    .withPreferredShipmentType(TransportShipmentTypes.MICROSOFT_MANAGED))))
            .withAddressDetails(
                new AddressDetails()
                    .withForwardAddress(
                        new AddressProperties()
                            .withShippingAddress(
                                new ShippingAddress()
                                    .withStreetAddress1("16 TOWNSEND ST")
                                    .withStreetAddress2("UNIT 1")
                                    .withCity("San Francisco")
                                    .withStateOrProvince("CA")
                                    .withCountry("US")
                                    .withPostalCode("94107")
                                    .withZipExtendedCode("1")
                                    .withCompanyName("Microsoft")
                                    .withAddressType(AddressType.RESIDENTIAL))
                            .withContactDetails(
                                new ContactDetails()
                                    .withContactName("164 TOWNSEND ST")
                                    .withPhone("3213131190")
                                    .withEmailList(
                                        Arrays.asList("ssemmail@microsoft.com", "vishwamdir@microsoft.com")))))
            .withOrderId(
                "/subscriptions/fa68082f-8ff7-4a25-95c7-ce9da541242f/resourceGroups/TestRG/providers/Microsoft.EdgeOrder/locations/westus/orders/TestOrderItemName01")
            .withTags(mapOf("carrot", "vegetable", "mango", "fruit"))
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

### ResourceProvider_Delete

```java
import com.azure.core.util.Context;

/** Samples for ResourceProvider Delete. */
public final class ResourceProviderDeleteSamples {
    /*
     * x-ms-original-file: specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/DeleteAddressByName.json
     */
    /**
     * Sample code: DeleteAddressByName.
     *
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void deleteAddressByName(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders().delete("TestRG", "TestAddressName1", Context.NONE);
    }
}
```

### ResourceProvider_DeleteOrderItemByName

```java
import com.azure.core.util.Context;

/** Samples for ResourceProvider DeleteOrderItemByName. */
public final class ResourceProviderDeleteOrderItemByNameSamples {
    /*
     * x-ms-original-file: specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/DeleteOrderItemByName.json
     */
    /**
     * Sample code: DeleteOrderItemByName.
     *
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void deleteOrderItemByName(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders().deleteOrderItemByName("TestOrderItemName01", "TestRG", Context.NONE);
    }
}
```

### ResourceProvider_GetByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ResourceProvider GetByResourceGroup. */
public final class ResourceProviderGetByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/GetAddressByName.json
     */
    /**
     * Sample code: GetAddressByName.
     *
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void getAddressByName(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders().getByResourceGroupWithResponse("TestRG", "TestMSAddressName", Context.NONE);
    }
}
```

### ResourceProvider_GetOrderByName

```java
import com.azure.core.util.Context;

/** Samples for ResourceProvider GetOrderByName. */
public final class ResourceProviderGetOrderByNameSamples {
    /*
     * x-ms-original-file: specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/GetOrderByName.json
     */
    /**
     * Sample code: GetOrderByName.
     *
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void getOrderByName(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager
            .resourceProviders()
            .getOrderByNameWithResponse("TestOrderItemName901", "TestRG", "%7B%7B%7Blocation%7D%7D", Context.NONE);
    }
}
```

### ResourceProvider_GetOrderItemByName

```java
import com.azure.core.util.Context;

/** Samples for ResourceProvider GetOrderItemByName. */
public final class ResourceProviderGetOrderItemByNameSamples {
    /*
     * x-ms-original-file: specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/GetOrderItemByName.json
     */
    /**
     * Sample code: GetOrderItemByName.
     *
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void getOrderItemByName(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders().getOrderItemByNameWithResponse("TestOrderItemName01", "TestRG", null, Context.NONE);
    }
}
```

### ResourceProvider_List

```java
import com.azure.core.util.Context;

/** Samples for ResourceProvider List. */
public final class ResourceProviderListSamples {
    /*
     * x-ms-original-file: specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/ListAddressesAtSubscriptionLevel.json
     */
    /**
     * Sample code: ListAddressesAtSubscriptionLevel.
     *
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void listAddressesAtSubscriptionLevel(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders().list(null, null, Context.NONE);
    }
}
```

### ResourceProvider_ListByResourceGroup

```java
import com.azure.core.util.Context;

/** Samples for ResourceProvider ListByResourceGroup. */
public final class ResourceProviderListByResourceGroupSamples {
    /*
     * x-ms-original-file: specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/ListAddressesAtResourceGroupLevel.json
     */
    /**
     * Sample code: ListAddressesAtResourceGroupLevel.
     *
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void listAddressesAtResourceGroupLevel(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders().listByResourceGroup("TestRG", null, null, Context.NONE);
    }
}
```

### ResourceProvider_ListConfigurations

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.edgeorder.models.ConfigurationFilters;
import com.azure.resourcemanager.edgeorder.models.ConfigurationsRequest;
import com.azure.resourcemanager.edgeorder.models.FilterableProperty;
import com.azure.resourcemanager.edgeorder.models.HierarchyInformation;
import com.azure.resourcemanager.edgeorder.models.SupportedFilterTypes;
import java.util.Arrays;

/** Samples for ResourceProvider ListConfigurations. */
public final class ResourceProviderListConfigurationsSamples {
    /*
     * x-ms-original-file: specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/ListConfigurations.json
     */
    /**
     * Sample code: ListConfigurations.
     *
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void listConfigurations(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager
            .resourceProviders()
            .listConfigurations(
                new ConfigurationsRequest()
                    .withConfigurationFilters(
                        Arrays
                            .asList(
                                new ConfigurationFilters()
                                    .withHierarchyInformation(
                                        new HierarchyInformation()
                                            .withProductFamilyName("AzureStackEdge")
                                            .withProductLineName("AzureStackEdge")
                                            .withProductName("AzureStackEdgeGPU"))
                                    .withFilterableProperty(
                                        Arrays
                                            .asList(
                                                new FilterableProperty()
                                                    .withType(SupportedFilterTypes.SHIP_TO_COUNTRIES)
                                                    .withSupportedValues(Arrays.asList("US")))))),
                null,
                Context.NONE);
    }
}
```

### ResourceProvider_ListOperations

```java
import com.azure.core.util.Context;

/** Samples for ResourceProvider ListOperations. */
public final class ResourceProviderListOperationsSamples {
    /*
     * x-ms-original-file: specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/ListOperations.json
     */
    /**
     * Sample code: ListOperations.
     *
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void listOperations(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders().listOperations(Context.NONE);
    }
}
```

### ResourceProvider_ListOrderAtResourceGroupLevel

```java
import com.azure.core.util.Context;

/** Samples for ResourceProvider ListOrderAtResourceGroupLevel. */
public final class ResourceProviderListOrderAtResourceGroupLevelSamples {
    /*
     * x-ms-original-file: specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/ListOrderAtResourceGroupLevel.json
     */
    /**
     * Sample code: ListOrderAtResourceGroupLevel.
     *
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void listOrderAtResourceGroupLevel(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders().listOrderAtResourceGroupLevel("TestRG", null, Context.NONE);
    }
}
```

### ResourceProvider_ListOrderAtSubscriptionLevel

```java
import com.azure.core.util.Context;

/** Samples for ResourceProvider ListOrderAtSubscriptionLevel. */
public final class ResourceProviderListOrderAtSubscriptionLevelSamples {
    /*
     * x-ms-original-file: specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/ListOrderAtSubscriptionLevel.json
     */
    /**
     * Sample code: ListOrderAtSubscriptionLevel.
     *
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void listOrderAtSubscriptionLevel(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders().listOrderAtSubscriptionLevel(null, Context.NONE);
    }
}
```

### ResourceProvider_ListOrderItemsAtResourceGroupLevel

```java
import com.azure.core.util.Context;

/** Samples for ResourceProvider ListOrderItemsAtResourceGroupLevel. */
public final class ResourceProviderListOrderItemsAtResourceGroupLevelSamples {
    /*
     * x-ms-original-file: specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/ListOrderItemsAtResourceGroupLevel.json
     */
    /**
     * Sample code: ListOrderItemsAtResourceGroupLevel.
     *
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void listOrderItemsAtResourceGroupLevel(
        com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders().listOrderItemsAtResourceGroupLevel("TestRG", null, null, null, Context.NONE);
    }
}
```

### ResourceProvider_ListOrderItemsAtSubscriptionLevel

```java
import com.azure.core.util.Context;

/** Samples for ResourceProvider ListOrderItemsAtSubscriptionLevel. */
public final class ResourceProviderListOrderItemsAtSubscriptionLevelSamples {
    /*
     * x-ms-original-file: specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/ListOrderItemsAtSubscriptionLevel.json
     */
    /**
     * Sample code: ListOrderItemsAtSubscriptionLevel.
     *
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void listOrderItemsAtSubscriptionLevel(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders().listOrderItemsAtSubscriptionLevel(null, null, null, Context.NONE);
    }
}
```

### ResourceProvider_ListProductFamilies

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.edgeorder.models.FilterableProperty;
import com.azure.resourcemanager.edgeorder.models.ProductFamiliesRequest;
import com.azure.resourcemanager.edgeorder.models.SupportedFilterTypes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for ResourceProvider ListProductFamilies. */
public final class ResourceProviderListProductFamiliesSamples {
    /*
     * x-ms-original-file: specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/ListProductFamilies.json
     */
    /**
     * Sample code: ListProductFamilies.
     *
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void listProductFamilies(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager
            .resourceProviders()
            .listProductFamilies(
                new ProductFamiliesRequest()
                    .withFilterableProperties(
                        mapOf(
                            "azurestackedge",
                            Arrays
                                .asList(
                                    new FilterableProperty()
                                        .withType(SupportedFilterTypes.SHIP_TO_COUNTRIES)
                                        .withSupportedValues(Arrays.asList("US"))))),
                null,
                null,
                Context.NONE);
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

### ResourceProvider_ListProductFamiliesMetadata

```java
import com.azure.core.util.Context;

/** Samples for ResourceProvider ListProductFamiliesMetadata. */
public final class ResourceProviderListProductFamiliesMetadataSamples {
    /*
     * x-ms-original-file: specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/ListProductFamiliesMetadata.json
     */
    /**
     * Sample code: ListProductFamiliesMetadata.
     *
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void listProductFamiliesMetadata(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager.resourceProviders().listProductFamiliesMetadata(null, Context.NONE);
    }
}
```

### ResourceProvider_ReturnOrderItem

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.edgeorder.models.ReturnOrderItemDetails;

/** Samples for ResourceProvider ReturnOrderItem. */
public final class ResourceProviderReturnOrderItemSamples {
    /*
     * x-ms-original-file: specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/ReturnOrderItem.json
     */
    /**
     * Sample code: ReturnOrderItem.
     *
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void returnOrderItem(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        manager
            .resourceProviders()
            .returnOrderItem(
                "TestOrderName1",
                "TestRG",
                new ReturnOrderItemDetails().withReturnReason("Order returned"),
                Context.NONE);
    }
}
```

### ResourceProvider_UpdateAddress

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.edgeorder.models.AddressResource;
import com.azure.resourcemanager.edgeorder.models.AddressType;
import com.azure.resourcemanager.edgeorder.models.ContactDetails;
import com.azure.resourcemanager.edgeorder.models.ShippingAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for ResourceProvider UpdateAddress. */
public final class ResourceProviderUpdateAddressSamples {
    /*
     * x-ms-original-file: specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/UpdateAddress.json
     */
    /**
     * Sample code: UpdateAddress.
     *
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void updateAddress(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        AddressResource resource =
            manager
                .resourceProviders()
                .getByResourceGroupWithResponse("TestRG", "TestAddressName2", Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(
                mapOf(
                    "Hobby",
                    "Web Series Added",
                    "Name",
                    "Smile-Updated",
                    "WhatElse",
                    "Web Series Added",
                    "Work",
                    "Engineering"))
            .withShippingAddress(
                new ShippingAddress()
                    .withStreetAddress1("16 TOWNSEND STT")
                    .withStreetAddress2("UNIT 1")
                    .withCity("San Francisco")
                    .withStateOrProvince("CA")
                    .withCountry("US")
                    .withPostalCode("94107")
                    .withCompanyName("Microsoft")
                    .withAddressType(AddressType.NONE))
            .withContactDetails(
                new ContactDetails()
                    .withContactName("Petr Cech")
                    .withPhone("1234567890")
                    .withPhoneExtension("")
                    .withEmailList(Arrays.asList("ssemcr@microsoft.com")))
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

### ResourceProvider_UpdateOrderItem

```java
import com.azure.core.util.Context;
import com.azure.resourcemanager.edgeorder.fluent.models.AddressProperties;
import com.azure.resourcemanager.edgeorder.models.ContactDetails;
import com.azure.resourcemanager.edgeorder.models.OrderItemResource;
import com.azure.resourcemanager.edgeorder.models.Preferences;
import com.azure.resourcemanager.edgeorder.models.TransportPreferences;
import com.azure.resourcemanager.edgeorder.models.TransportShipmentTypes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for ResourceProvider UpdateOrderItem. */
public final class ResourceProviderUpdateOrderItemSamples {
    /*
     * x-ms-original-file: specification/edgeorder/resource-manager/Microsoft.EdgeOrder/stable/2021-12-01/examples/UpdateOrderItem.json
     */
    /**
     * Sample code: UpdateOrderItem.
     *
     * @param manager Entry point to EdgeOrderManager.
     */
    public static void updateOrderItem(com.azure.resourcemanager.edgeorder.EdgeOrderManager manager) {
        OrderItemResource resource =
            manager
                .resourceProviders()
                .getOrderItemByNameWithResponse("TestOrderItemName01", "TestRG", null, Context.NONE)
                .getValue();
        resource
            .update()
            .withTags(mapOf("ant", "insect", "pigeon", "bird", "tiger", "animal"))
            .withForwardAddress(
                new AddressProperties()
                    .withContactDetails(
                        new ContactDetails()
                            .withContactName("Updated contact name")
                            .withPhone("2222200000")
                            .withEmailList(Arrays.asList("testemail@microsoft.com"))))
            .withPreferences(
                new Preferences()
                    .withTransportPreferences(
                        new TransportPreferences().withPreferredShipmentType(TransportShipmentTypes.CUSTOMER_MANAGED)))
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

