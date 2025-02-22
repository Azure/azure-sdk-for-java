// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.edgeorder.generated;

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
