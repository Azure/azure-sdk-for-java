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
import com.azure.communication.phonenumbers.models.PurchasedPhoneNumber;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;

public class SyncClientJavaDocCodeSnippets {

    /**
     * Sample code for creating a sync Phone Number Client.
     *
     * @return the Phone Number Client.
     */
    public PhoneNumbersClient createPhoneNumberClient() {
        // You can find your endpoint and access token from your resource in the Azure Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
        AzureKeyCredential keyCredential = new AzureKeyCredential("SECRET");

        // Create an HttpClient builder of your choice and customize it
        HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

        // BEGIN: com.azure.communication.phonenumbers.client.instantiation
        PhoneNumbersClient phoneNumberClient = new PhoneNumbersClientBuilder()
            .endpoint(endpoint)
            .credential(keyCredential)
            .httpClient(httpClient)
            .buildClient();
        // END: com.azure.communication.phonenumbers.client.instantiation

        return phoneNumberClient;
    }

    /**
     * Sample code for getting a purchased phone number.
     *
     * @return the purchased phone number.
     */
    public PurchasedPhoneNumber getPurchasedPhoneNumber() {
        PhoneNumbersClient phoneNumberClient = createPhoneNumberClient();

        // BEGIN: com.azure.communication.phonenumbers.client.getPurchased
        PurchasedPhoneNumber phoneNumber = phoneNumberClient.getPurchasedPhoneNumber("+18001234567");
        System.out.println("Phone Number Value: " + phoneNumber.getPhoneNumber());
        System.out.println("Phone Number Country Code: " + phoneNumber.getCountryCode());
        // END: com.azure.communication.phonenumbers.client.getPurchased

        return phoneNumber;
    }

    /**
     * Sample code for getting a purchased phone number with response.
     *
     * @return the purchased phone number.
     */
    public PurchasedPhoneNumber getPurchasedPhoneNumberWithResponse() {
        PhoneNumbersClient phoneNumberClient = createPhoneNumberClient();

        // BEGIN: com.azure.communication.phonenumbers.client.getPurchasedWithResponse
        Response<PurchasedPhoneNumber> response = phoneNumberClient
            .getPurchasedPhoneNumberWithResponse("+18001234567", Context.NONE);
        PurchasedPhoneNumber phoneNumber = response.getValue();
        System.out.println("Phone Number Value: " + phoneNumber.getPhoneNumber());
        System.out.println("Phone Number Country Code: " + phoneNumber.getCountryCode());
        // END: com.azure.communication.phonenumbers.client.getPurchasedWithResponse

        return phoneNumber;
    }

    /**
     * Sample code for listing all purchased phone numbers.
     *
     * @return all purchased phone number.
     */
    public PagedIterable<PurchasedPhoneNumber> listPurchasedPhoneNumbers() {
        PhoneNumbersClient phoneNumberClient = createPhoneNumberClient();

        // BEGIN: com.azure.communication.phonenumbers.client.listPurchased
        PagedIterable<PurchasedPhoneNumber> phoneNumbers = phoneNumberClient.listPurchasedPhoneNumbers();
        PurchasedPhoneNumber phoneNumber = phoneNumbers.iterator().next();
        System.out.println("Phone Number Value: " + phoneNumber.getPhoneNumber());
        System.out.println("Phone Number Country Code: " + phoneNumber.getCountryCode());
        // END: com.azure.communication.phonenumbers.client.listPurchased

        return phoneNumbers;
    }

    /**
     * Sample code for listing all purchased phone numbers with context.
     *
     * @return all purchased phone number.
     */
    public PagedIterable<PurchasedPhoneNumber> listPurchasedPhoneNumbersWithContext() {
        PhoneNumbersClient phoneNumberClient = createPhoneNumberClient();

        // BEGIN: com.azure.communication.phonenumbers.client.listPurchasedWithContext
        PagedIterable<PurchasedPhoneNumber> phoneNumbers = phoneNumberClient.listPurchasedPhoneNumbers(Context.NONE);
        PurchasedPhoneNumber phoneNumber = phoneNumbers.iterator().next();
        System.out.println("Phone Number Value: " + phoneNumber.getPhoneNumber());
        System.out.println("Phone Number Country Code: " + phoneNumber.getCountryCode());
        // END: com.azure.communication.phonenumbers.client.listPurchasedWithContext

        return phoneNumbers;
    }

    /**
     * Search for available phone numbers and purchase phone numbers
     */
    public void searchAvailablePhoneNumbersAndPurchasePhoneNumbers() {
        PhoneNumbersClient phoneNumberClient = createPhoneNumberClient();

        // BEGIN: com.azure.communication.phonenumbers.client.beginSearchAvailable
        PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities()
            .setCalling(PhoneNumberCapabilityType.INBOUND)
            .setSms(PhoneNumberCapabilityType.INBOUND_OUTBOUND);

        SyncPoller<PhoneNumberOperation, PhoneNumberSearchResult> poller = phoneNumberClient
            .beginSearchAvailablePhoneNumbers("US", PhoneNumberType.TOLL_FREE,
                PhoneNumberAssignmentType.APPLICATION, capabilities);
        PollResponse<PhoneNumberOperation> response = poller.waitForCompletion();
        String searchId = "";

        if (LongRunningOperationStatus.SUCCESSFULLY_COMPLETED == response.getStatus()) {
            PhoneNumberSearchResult searchResult = poller.getFinalResult();
            searchId = searchResult.getSearchId();
            System.out.println("Searched phone numbers: " + searchResult.getPhoneNumbers());
            System.out.println("Search expires by: " + searchResult.getSearchExpiresBy());
            System.out.println("Phone number costs:" + searchResult.getCost().getAmount());
        }
        // END: com.azure.communication.phonenumbers.client.beginSearchAvailable

        // BEGIN: com.azure.communication.phonenumbers.client.beginPurchase
        PollResponse<PhoneNumberOperation> purchaseResponse =
            phoneNumberClient.beginPurchasePhoneNumbers(searchId).waitForCompletion();
        System.out.println("Purchase phone numbers is complete: " + purchaseResponse.getStatus());
        // END: com.azure.communication.phonenumbers.client.beginPurchase
    }

    /**
     * Search for available phone numbers with search options
     */
    public void searchAvailablePhoneNumbersWithSearchOptionsAndPurchasePhoneNumbers() {
        PhoneNumbersClient phoneNumberClient = createPhoneNumberClient();

        // BEGIN: com.azure.communication.phonenumbers.client.beginSearchAvailableWithOptions
        PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities()
            .setCalling(PhoneNumberCapabilityType.INBOUND)
            .setSms(PhoneNumberCapabilityType.INBOUND_OUTBOUND);
        PhoneNumberSearchOptions searchOptions = new PhoneNumberSearchOptions().setAreaCode("800").setQuantity(1);

        SyncPoller<PhoneNumberOperation, PhoneNumberSearchResult> poller = phoneNumberClient
            .beginSearchAvailablePhoneNumbers("US", PhoneNumberType.TOLL_FREE,
                PhoneNumberAssignmentType.APPLICATION, capabilities, searchOptions, Context.NONE);
        PollResponse<PhoneNumberOperation> response = poller.waitForCompletion();
        String searchId = "";

        if (LongRunningOperationStatus.SUCCESSFULLY_COMPLETED == response.getStatus()) {
            PhoneNumberSearchResult searchResult = poller.getFinalResult();
            searchId = searchResult.getSearchId();
            System.out.println("Searched phone numbers: " + searchResult.getPhoneNumbers());
            System.out.println("Search expires by: " + searchResult.getSearchExpiresBy());
            System.out.println("Phone number costs:" + searchResult.getCost().getAmount());
        }
        // END: com.azure.communication.phonenumbers.client.beginSearchAvailableWithOptions

        // BEGIN: com.azure.communication.phonenumbers.client.beginPurchaseWithContext
        PollResponse<PhoneNumberOperation> purchaseResponse =
            phoneNumberClient.beginPurchasePhoneNumbers(searchId, Context.NONE).waitForCompletion();
        System.out.println("Purchase phone numbers is complete: " + purchaseResponse.getStatus());
        // END: com.azure.communication.phonenumbers.client.beginPurchaseWithContext
    }

    /**
     * Release acquired phone number
     */
    public void releasePhoneNumber() {
        PhoneNumbersClient phoneNumberClient = createPhoneNumberClient();

        // BEGIN: com.azure.communication.phonenumbers.client.beginRelease
        PollResponse<PhoneNumberOperation> releaseResponse =
            phoneNumberClient.beginReleasePhoneNumber("+18001234567").waitForCompletion();
        System.out.println("Release phone number is complete: " + releaseResponse.getStatus());
        // END: com.azure.communication.phonenumbers.client.beginRelease
    }

    /**
     * Release acquired phone number with context
     */
    public void releasePhoneNumberWithContext() {
        PhoneNumbersClient phoneNumberClient = createPhoneNumberClient();

        // BEGIN: com.azure.communication.phonenumbers.client.beginReleaseWithContext
        PollResponse<PhoneNumberOperation> releaseResponse =
            phoneNumberClient.beginReleasePhoneNumber("+18001234567", Context.NONE).waitForCompletion();
        System.out.println("Release phone number is complete: " + releaseResponse.getStatus());
        // END: com.azure.communication.phonenumbers.client.beginReleaseWithContext
    }

    /**
     * Update phone number capabilities
     */
    public void updatePhoneNumberCapabilities() {
        PhoneNumbersClient phoneNumberClient = createPhoneNumberClient();

        // BEGIN: com.azure.communication.phonenumbers.client.beginUpdateCapabilities
        PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities();
        capabilities
            .setCalling(PhoneNumberCapabilityType.INBOUND)
            .setSms(PhoneNumberCapabilityType.INBOUND_OUTBOUND);

        SyncPoller<PhoneNumberOperation, PurchasedPhoneNumber> poller =
            phoneNumberClient.beginUpdatePhoneNumberCapabilities("+18001234567", capabilities);
        PollResponse<PhoneNumberOperation> response = poller.waitForCompletion();

        if (LongRunningOperationStatus.SUCCESSFULLY_COMPLETED == response.getStatus()) {
            PurchasedPhoneNumber phoneNumber = poller.getFinalResult();
            System.out.println("Phone Number Calling capabilities: " + phoneNumber.getCapabilities().getCalling());
            System.out.println("Phone Number SMS capabilities: " + phoneNumber.getCapabilities().getSms());
        }
        // END: com.azure.communication.phonenumbers.client.beginUpdateCapabilities
    }

    /**
     * Update phone number capabilities
     */
    public void updatePhoneNumberCapabilitiesWithContext() {
        PhoneNumbersClient phoneNumberClient = createPhoneNumberClient();

        // BEGIN: com.azure.communication.phonenumbers.client.beginUpdateCapabilitiesWithContext
        PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities();
        capabilities
            .setCalling(PhoneNumberCapabilityType.INBOUND)
            .setSms(PhoneNumberCapabilityType.INBOUND_OUTBOUND);

        SyncPoller<PhoneNumberOperation, PurchasedPhoneNumber> poller =
            phoneNumberClient.beginUpdatePhoneNumberCapabilities("+18001234567", capabilities, Context.NONE);
        PollResponse<PhoneNumberOperation> response = poller.waitForCompletion();

        if (LongRunningOperationStatus.SUCCESSFULLY_COMPLETED == response.getStatus()) {
            PurchasedPhoneNumber phoneNumber = poller.getFinalResult();
            System.out.println("Phone Number Calling capabilities: " + phoneNumber.getCapabilities().getCalling());
            System.out.println("Phone Number SMS capabilities: " + phoneNumber.getCapabilities().getSms());
        }
        // END: com.azure.communication.phonenumbers.client.beginUpdateCapabilitiesWithContext
    }

}
