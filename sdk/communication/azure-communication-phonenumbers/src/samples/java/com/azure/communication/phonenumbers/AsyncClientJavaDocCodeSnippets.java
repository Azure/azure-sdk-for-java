// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.phonenumbers;

import com.azure.communication.phonenumbers.models.PhoneNumberAssignmentType;
import com.azure.communication.phonenumbers.models.PhoneNumberCapabilities;
import com.azure.communication.phonenumbers.models.PhoneNumberCapabilityType;
import com.azure.communication.phonenumbers.models.PhoneNumberOperation;
import com.azure.communication.phonenumbers.models.PhoneNumberSearchOptions;
import com.azure.communication.phonenumbers.models.PhoneNumberSearchResult;
import com.azure.communication.phonenumbers.models.PhoneNumberType;
import com.azure.communication.phonenumbers.models.PurchasePhoneNumbersResult;
import com.azure.communication.phonenumbers.models.PurchasedPhoneNumber;
import com.azure.communication.phonenumbers.models.ReleasePhoneNumberResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;

public class AsyncClientJavaDocCodeSnippets {

    /**
     * Sample code for creating an async Phone Number Client.
     *
     * @return the Phone Number Async Client.
     */
    public PhoneNumbersAsyncClient createPhoneNumberAsyncClient() {
        // You can find your endpoint and access token from your resource in the Azure Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
        AzureKeyCredential keyCredential = new AzureKeyCredential("SECRET");

        // Create an HttpClient builder of your choice and customize it
        HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

        // BEGIN: com.azure.communication.phonenumbers.asyncclient.instantiation
        PhoneNumbersAsyncClient phoneNumberAsyncClient = new PhoneNumbersClientBuilder()
            .endpoint(endpoint)
            .credential(keyCredential)
            .httpClient(httpClient)
            .buildAsyncClient();
        // END: com.azure.communication.phonenumbers.asyncclient.instantiation

        return phoneNumberAsyncClient;
    }

    /**
     * Sample code for getting a purchased phone number.
     *
     * @return the purchased phone number.
     */
    public PurchasedPhoneNumber getPurchasedPhoneNumber() {
        PhoneNumbersAsyncClient phoneNumberAsyncClient = createPhoneNumberAsyncClient();

        // BEGIN: com.azure.communication.phonenumbers.asyncclient.getPurchased
        PurchasedPhoneNumber phoneNumber = phoneNumberAsyncClient.getPurchasedPhoneNumber("+18001234567").block();
        System.out.println("Phone Number Value: " + phoneNumber.getPhoneNumber());
        System.out.println("Phone Number Country Code: " + phoneNumber.getCountryCode());
        // END: com.azure.communication.phonenumbers.asyncclient.getPurchased

        return phoneNumber;
    }

    /**
     * Sample code for getting a purchased phone number with response.
     *
     * @return the purchased phone number.
     */
    public PurchasedPhoneNumber getPurchasedPhoneNumberWithResponse() {
        PhoneNumbersAsyncClient phoneNumberAsyncClient = createPhoneNumberAsyncClient();

        // BEGIN: com.azure.communication.phonenumbers.asyncclient.getPurchasedWithResponse
        Response<PurchasedPhoneNumber> response = phoneNumberAsyncClient
            .getPurchasedPhoneNumberWithResponse("+18001234567").block();
        PurchasedPhoneNumber phoneNumber = response.getValue();
        System.out.println("Phone Number Value: " + phoneNumber.getPhoneNumber());
        System.out.println("Phone Number Country Code: " + phoneNumber.getCountryCode());
        // END: com.azure.communication.phonenumbers.asyncclient.getPurchasedWithResponse

        return phoneNumber;
    }

    /**
     * Sample code for listing all purchased phone numbers.
     */
    public void listPurchasedPhoneNumbers() {
        PhoneNumbersAsyncClient phoneNumberAsyncClient = createPhoneNumberAsyncClient();

        // BEGIN: com.azure.communication.phonenumbers.asyncclient.listPurchased
        PagedFlux<PurchasedPhoneNumber> phoneNumbers = phoneNumberAsyncClient.listPurchasedPhoneNumbers();
        PurchasedPhoneNumber phoneNumber = phoneNumbers.blockFirst();
        System.out.println("Phone Number Value: " + phoneNumber.getPhoneNumber());
        System.out.println("Phone Number Country Code: " + phoneNumber.getCountryCode());
        // END: com.azure.communication.phonenumbers.asyncclient.listPurchased
    }

    /**
     * Search for available phone numbers and purchase phone numbers
     */
    public void searchAvailablePhoneNumbersAndPurchasePhoneNumbers() {
        PhoneNumbersAsyncClient phoneNumberAsyncClient = createPhoneNumberAsyncClient();

        // BEGIN: com.azure.communication.phonenumbers.asyncclient.beginSearchAvailable
        PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities()
            .setCalling(PhoneNumberCapabilityType.INBOUND)
            .setSms(PhoneNumberCapabilityType.INBOUND_OUTBOUND);

        PollerFlux<PhoneNumberOperation, PhoneNumberSearchResult> poller = phoneNumberAsyncClient
            .beginSearchAvailablePhoneNumbers("US", PhoneNumberType.TOLL_FREE,
                PhoneNumberAssignmentType.APPLICATION, capabilities);
        AsyncPollResponse<PhoneNumberOperation, PhoneNumberSearchResult> response = poller.blockFirst();
        String searchId = "";

        if (LongRunningOperationStatus.SUCCESSFULLY_COMPLETED == response.getStatus()) {
            PhoneNumberSearchResult searchResult = response.getFinalResult().block();
            searchId = searchResult.getSearchId();
            System.out.println("Searched phone numbers: " + searchResult.getPhoneNumbers());
            System.out.println("Search expires by: " + searchResult.getSearchExpiresBy());
            System.out.println("Phone number costs:" + searchResult.getCost().getAmount());
        }
        // END: com.azure.communication.phonenumbers.asyncclient.beginSearchAvailable

        // BEGIN: com.azure.communication.phonenumbers.asyncclient.beginPurchase
        AsyncPollResponse<PhoneNumberOperation, PurchasePhoneNumbersResult> purchaseResponse =
            phoneNumberAsyncClient.beginPurchasePhoneNumbers(searchId).blockFirst();
        System.out.println("Purchase phone numbers is complete: " + purchaseResponse.getStatus());
        // END: com.azure.communication.phonenumbers.asyncclient.beginPurchase
    }

    /**
     * Search for available phone numbers with search options
     */
    public void searchAvailablePhoneNumbersWithSearchOptions() {
        PhoneNumbersAsyncClient phoneNumberAsyncClient = createPhoneNumberAsyncClient();

        // BEGIN: com.azure.communication.phonenumbers.asyncclient.beginSearchAvailableWithOptions
        PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities()
            .setCalling(PhoneNumberCapabilityType.INBOUND)
            .setSms(PhoneNumberCapabilityType.INBOUND_OUTBOUND);
        PhoneNumberSearchOptions searchOptions = new PhoneNumberSearchOptions().setAreaCode("800").setQuantity(1);

        PollerFlux<PhoneNumberOperation, PhoneNumberSearchResult> poller = phoneNumberAsyncClient
            .beginSearchAvailablePhoneNumbers("US", PhoneNumberType.TOLL_FREE,
                PhoneNumberAssignmentType.APPLICATION, capabilities, searchOptions);
        AsyncPollResponse<PhoneNumberOperation, PhoneNumberSearchResult> response = poller.blockFirst();

        if (LongRunningOperationStatus.SUCCESSFULLY_COMPLETED == response.getStatus()) {
            PhoneNumberSearchResult searchResult = response.getFinalResult().block();
            String searchId = searchResult.getSearchId();
            System.out.println("Searched phone numbers: " + searchResult.getPhoneNumbers());
            System.out.println("Search expires by: " + searchResult.getSearchExpiresBy());
            System.out.println("Phone number costs:" + searchResult.getCost().getAmount());
        }
        // END: com.azure.communication.phonenumbers.asyncclient.beginSearchAvailableWithOptions
    }

    /**
     * Release acquired phone number
     */
    public void releasePhoneNumber() {
        PhoneNumbersAsyncClient phoneNumberAsyncClient = createPhoneNumberAsyncClient();

        // BEGIN: com.azure.communication.phonenumbers.asyncclient.beginRelease
        AsyncPollResponse<PhoneNumberOperation, ReleasePhoneNumberResult> releaseResponse =
            phoneNumberAsyncClient.beginReleasePhoneNumber("+18001234567").blockFirst();
        System.out.println("Release phone number is complete: " + releaseResponse.getStatus());
        // END: com.azure.communication.phonenumbers.asyncclient.beginRelease
    }

    /**
     * Update phone number capabilities
     */
    public void updatePhoneNumberCapabilities() {
        PhoneNumbersAsyncClient phoneNumberAsyncClient = createPhoneNumberAsyncClient();

        // BEGIN: com.azure.communication.phonenumbers.asyncclient.beginUpdateCapabilities
        PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities();
        capabilities
            .setCalling(PhoneNumberCapabilityType.INBOUND)
            .setSms(PhoneNumberCapabilityType.INBOUND_OUTBOUND);

        PollerFlux<PhoneNumberOperation, PurchasedPhoneNumber> poller =
            phoneNumberAsyncClient.beginUpdatePhoneNumberCapabilities("+18001234567", capabilities);
        AsyncPollResponse<PhoneNumberOperation, PurchasedPhoneNumber> response = poller.blockFirst();

        if (LongRunningOperationStatus.SUCCESSFULLY_COMPLETED == response.getStatus()) {
            PurchasedPhoneNumber phoneNumber = response.getFinalResult().block();
            System.out.println("Phone Number Calling capabilities: " + phoneNumber.getCapabilities().getCalling());
            System.out.println("Phone Number SMS capabilities: " + phoneNumber.getCapabilities().getSms());
        }
        // END: com.azure.communication.phonenumbers.asyncclient.beginUpdateCapabilities
    }

}
