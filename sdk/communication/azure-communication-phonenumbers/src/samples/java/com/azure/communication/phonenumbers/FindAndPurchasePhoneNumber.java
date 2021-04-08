// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.phonenumbers;

import com.azure.communication.phonenumbers.models.PhoneNumberCapabilities;
import com.azure.communication.phonenumbers.models.PhoneNumberCapabilityType;
import com.azure.communication.phonenumbers.models.PhoneNumberSearchOptions;
import com.azure.communication.phonenumbers.models.PhoneNumberSearchResult;
import com.azure.communication.phonenumbers.models.PhoneNumberType;
import com.azure.communication.phonenumbers.models.PhoneNumberAssignmentType;
import com.azure.communication.phonenumbers.models.PhoneNumberOperation;
import com.azure.core.util.Context;
import com.azure.core.util.polling.PollResponse;

public class FindAndPurchasePhoneNumber {
    public static void main(String[] args) {
        String connectionString = System.getenv("COMMUNICATION_CONNECTION_STRING");
        String areaCode = System.getenv("AZURE_COMMUNICATION_AREA_CODE");

        PhoneNumbersClient phoneNumberClient = new PhoneNumbersClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities()
            .setCalling(PhoneNumberCapabilityType.INBOUND)
            .setSms(PhoneNumberCapabilityType.INBOUND_OUTBOUND);
        PhoneNumberSearchOptions searchOptions = new PhoneNumberSearchOptions().setAreaCode(areaCode).setQuantity(1);

        PhoneNumberSearchResult searchResult = phoneNumberClient
            .beginSearchAvailablePhoneNumbers("US", PhoneNumberType.TOLL_FREE, PhoneNumberAssignmentType.APPLICATION, capabilities, searchOptions, Context.NONE)
            .getFinalResult();

        System.out.println("Searched phone numbers: " + searchResult.getPhoneNumbers());
        System.out.println("Search expires by: " + searchResult.getSearchExpiresBy());
        System.out.println("Phone number costs:" + searchResult.getCost().getAmount());

        PollResponse<PhoneNumberOperation> purchaseResponse =
            phoneNumberClient.beginPurchasePhoneNumbers(searchResult.getSearchId(), Context.NONE).waitForCompletion();
        System.out.println("Purchase phone numbers is complete: " + purchaseResponse.getStatus());
    }
}
