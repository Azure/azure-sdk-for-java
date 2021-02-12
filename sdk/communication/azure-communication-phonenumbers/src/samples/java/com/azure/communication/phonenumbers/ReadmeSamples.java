// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.phonenumbers;

import com.azure.communication.phonenumbers.models.AcquiredPhoneNumber;
import com.azure.communication.phonenumbers.models.PhoneNumberAssignmentType;
import com.azure.communication.phonenumbers.models.PhoneNumberCapabilities;
import com.azure.communication.phonenumbers.models.PhoneNumberCapabilitiesRequest;
import com.azure.communication.phonenumbers.models.PhoneNumberCapabilityValue;
import com.azure.communication.phonenumbers.models.PhoneNumberOperation;
import com.azure.communication.phonenumbers.models.PhoneNumberSearchRequest;
import com.azure.communication.phonenumbers.models.PhoneNumberSearchResult;
import com.azure.communication.phonenumbers.models.PhoneNumberType;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.core.util.polling.PollResponse;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class ReadmeSamples {

    /**
     * Sample code for creating a sync Phone Number Client.
     *
     * @return the Phone Number Client.
     */
    public PhoneNumbersClient createPhoneNumberClient() {
        // You can find your endpoint and access token from your resource in the Azure Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
        String accessKey = "SECRET";

        // Create an HttpClient builder of your choice and customize it
        HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

        PhoneNumbersClient phoneNumberClient = new PhoneNumbersClientBuilder()
            .endpoint(endpoint)
            .accessKey(accessKey)
            .httpClient(httpClient)
            .buildClient();

        return phoneNumberClient;
    }

    /**
     * Sample code for creating a sync Communication Identity Client using AAD authentication.
     *
     * @return the Phone Number Client.
     */
    public PhoneNumbersClient createPhoneNumberClientWithAAD() {
        // You can find your endpoint and access key from your resource in the Azure Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";

        // Create an HttpClient builder of your choice and customize it
        HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

        PhoneNumbersClient phoneNumberClient = new PhoneNumbersClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpClient(httpClient)
            .buildClient();

        return phoneNumberClient;
    }

    /**
     * Sample code for getting an acquired phone number.
     *
     * @return the acquired phone number.
     */
    public AcquiredPhoneNumber getPhoneNumber() {
        PhoneNumbersClient phoneNumberClient = createPhoneNumberClient();
        AcquiredPhoneNumber phoneNumber = phoneNumberClient.getPhoneNumber("+18001234567");
        System.out.println("Phone Number Value: " + phoneNumber.getPhoneNumber());
        System.out.println("Phone Number Country Code: " + phoneNumber.getCountryCode());
        return phoneNumber;
    }

    /**
     * Sample code for listing all acquired phone numbers.
     *
     * @return all acquired phone number.
     */
    public PagedIterable<AcquiredPhoneNumber> listPhoneNumbers() {
        PagedIterable<AcquiredPhoneNumber> phoneNumbers = createPhoneNumberClient().listPhoneNumbers(Context.NONE);
        AcquiredPhoneNumber phoneNumber = phoneNumbers.iterator().next();
        System.out.println("Phone Number Value: " + phoneNumber.getPhoneNumber());
        System.out.println("Phone Number Country Code: " + phoneNumber.getCountryCode());
        return phoneNumbers;
    }

    /**
     * Search for available phone numbers and purchase phone numbers
     */
    public void searchAvailablePhoneNumbersandPurchasePhoneNumbers() {
        PhoneNumbersClient phoneNumberClient = createPhoneNumberClient();
        PhoneNumberSearchRequest searchRequest = new PhoneNumberSearchRequest();
        searchRequest
            .setAreaCode("800") // Area code is optional for toll free numbers
            .setAssignmentType(PhoneNumberAssignmentType.USER)
            .setCapabilities(new PhoneNumberCapabilities()
                .setCalling(PhoneNumberCapabilityValue.INBOUND)
                .setSms(PhoneNumberCapabilityValue.INBOUND_OUTBOUND))
            .setPhoneNumberType(PhoneNumberType.GEOGRAPHIC)
            .setQuantity(1); // Quantity is optional, default is 1

        PhoneNumberSearchResult searchResult = phoneNumberClient
            .beginSearchAvailablePhoneNumbers("US", searchRequest, Context.NONE)
            .getFinalResult();

        System.out.println("Searched phone numbers: " + searchResult.getPhoneNumbers());
        System.out.println("Search expires by: " + searchResult.getSearchExpiresBy());
        System.out.println("Phone number costs:" + searchResult.getCost().getAmount());

        PollResponse<PhoneNumberOperation> purchaseResponse = 
            phoneNumberClient.beginPurchasePhoneNumbers(searchResult.getSearchId(), Context.NONE).waitForCompletion();
        System.out.println("Purchase phone numbers is complete: " + purchaseResponse.getStatus());
    }

    /**
     * Release acquired phone number
     */
    public void releasePhoneNumber() {
        PhoneNumbersClient phoneNumberClient = createPhoneNumberClient();
        PollResponse<PhoneNumberOperation> releaseResponse = 
            phoneNumberClient.beginReleasePhoneNumber("+18001234567", Context.NONE).waitForCompletion();
        System.out.println("Release phone number is complete: " + releaseResponse.getStatus());
    }

    /**
     * Update phone number capabilities
     *
     * @return the updated acquired phone number
     */
    public AcquiredPhoneNumber updatePhoneNumberCapabilities() {
        PhoneNumbersClient phoneNumberClient = createPhoneNumberClient();
        PhoneNumberCapabilitiesRequest capabilitiesRequest = new PhoneNumberCapabilitiesRequest();
        capabilitiesRequest
            .setCalling(PhoneNumberCapabilityValue.INBOUND)
            .setSms(PhoneNumberCapabilityValue.INBOUND_OUTBOUND);
        AcquiredPhoneNumber phoneNumber = phoneNumberClient.beginUpdatePhoneNumberCapabilities("+18001234567", capabilitiesRequest, Context.NONE).getFinalResult();
        
        System.out.println("Phone Number Calling capabilities: " + phoneNumber.getCapabilities().getCalling()); //Phone Number Calling capabilities: inbound
        System.out.println("Phone Number SMS capabilities: " + phoneNumber.getCapabilities().getSms()); //Phone Number SMS capabilities: inbound+outbound
        return phoneNumber;
    }
}
