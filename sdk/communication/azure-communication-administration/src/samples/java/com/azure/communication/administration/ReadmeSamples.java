// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.administration;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.azure.communication.administration.models.*;
import com.azure.communication.common.CommunicationUser;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.polling.SyncPoller;

public class ReadmeSamples {
    /**
     * Sample code for creating a sync Communication Identity Client.
     *
     * @return the Communication Identity Client.
     */
    public CommunicationIdentityClient createCommunicationIdentityClient() {
        // You can find your endpoint and access key from your resource in the Azure Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
        String accessKey = "SECRET";

        // Create an HttpClient builder of your choice and customize it
        HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

        CommunicationIdentityClient communicationIdentityClient = new CommunicationIdentityClientBuilder()
            .endpoint(endpoint)
            .accessKey(accessKey)
            .httpClient(httpClient)
            .buildClient();

        return communicationIdentityClient;
    }

    /**
     * Sample code for creating a sync Communication Identity Client using connection string.
     *
     * @return the Communication Identity Client.
     */
    public CommunicationIdentityClient createCommunicationIdentityClientWithConnectionString() {
        // Create an HttpClient builder of your choice and customize it
        HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

        // Your can find your connection string from your resource in the Azure Portal
        String connectionString = "<connection_string>";

        CommunicationIdentityClient communicationIdentityClient = new CommunicationIdentityClientBuilder()
            .connectionString(connectionString)
            .httpClient(httpClient)
            .buildClient();

        return communicationIdentityClient;
    }

    /**
     * Sample code for creating a user
     *
     * @return the created user
     */
    public CommunicationUser createNewUser() {
        CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();
        CommunicationUser user = communicationIdentityClient.createUser();
        System.out.println("User id: " + user.getId());
        return user;
    }

    /**
     * Sample code for issuing a user token
     *
     * @return the issued user token
     */
    public CommunicationUserToken issueUserToken() {
        CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();
        CommunicationUser user = communicationIdentityClient.createUser();
        List<String> scopes = new ArrayList<>(Arrays.asList("chat"));
        CommunicationUserToken userToken = communicationIdentityClient.issueToken(user, scopes);
        System.out.println("Token: " + userToken.getToken());
        System.out.println("Expires On: " + userToken.getExpiresOn());
        return userToken;
    }

     /**
      * Sample code for revoking user token
      */
    public void revokeUserToken() {
        CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();
        CommunicationUser user = createNewUser();
        List<String> scopes = new ArrayList<>(Arrays.asList("chat"));
        communicationIdentityClient.issueToken(user, scopes);
        // revoke tokens issued for the user prior to now
        communicationIdentityClient.revokeTokens(user, OffsetDateTime.now());
    }

    /**
     * Sample code for deleting user
     */
    public void deleteUser() {
        CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();
        CommunicationUser user = communicationIdentityClient.createUser();
        // delete a previously created user
        communicationIdentityClient.deleteUser(user);
    }

    /**
     * Sample code for creating a sync PhoneNumber Client.
     *
     * @return the Phone Number Client.
     */
    public PhoneNumberClient createPhoneNumberClient() {
        // You can find your endpoint and access key from your resource in the Azure Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
        String accessKey = "SECRET";

        // Create an HttpClient builder of your choice and customize it
        HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

        PhoneNumberClient phoneNumberClient = new PhoneNumberClientBuilder()
            .endpoint(endpoint)
            .accessKey(accessKey)
            .httpClient(httpClient)
            .buildClient();

        return phoneNumberClient;
    }

    /**
     * Sample code to search geographic phone numbers
     *
     * @return Id for the search
     */
    public String beginSearchGeographicPhoneNumbers() {
        String countryCode = "US";

        SearchCapabilities searchCapabilities = new SearchCapabilities()
            .setSms(CapabilityValue.OUTBOUND)
            .setCalling(CapabilityValue.INBOUND_OUTBOUND);

        SearchRequest searchRequest = new SearchRequest()
            .setNumberType(PhoneNumberType.GEOGRAPHIC)
            .setAssignmentType(AssignmentType.APPLICATION)
            .setAreaCode("425")
            .setCapabilities(searchCapabilities)
            .setQuantity(2);

        PhoneNumberClient phoneNumberClient = createPhoneNumberClient();
        SyncPoller<Operation,SearchResult> syncPoller = phoneNumberClient
            .beginSearchPhoneNumber(countryCode, searchRequest);

        syncPoller.waitForCompletion();
        SearchResult searchResult = syncPoller.getFinalResult();

        System.out.println("SearchId: " + searchResult.getId());
        System.out.println("Monthly Rate: " + searchResult.getMonthlyRate().getValue());
        for (String phoneNumber : searchResult.getPhoneNumbers()) {
            System.out.println("Available Phone Number: " + phoneNumber);
        }

        return searchResult.getId();
    }

    /**
     * Sample code to search toll-free phone numbers
     *
     * @return Id for the search
     */
    public String beginSearchTollFreePhoneNumbers() {
        String countryCode = "US";

        SearchCapabilities searchCapabilities = new SearchCapabilities()
            .setSms(CapabilityValue.NONE)
            .setCalling(CapabilityValue.INBOUND);

        SearchRequest searchRequest = new SearchRequest()
            .setNumberType(PhoneNumberType.TOLL_FREE)
            .setAssignmentType(AssignmentType.APPLICATION)
            .setAreaCode("800")
            .setCapabilities(searchCapabilities)
            .setQuantity(1);

        PhoneNumberClient phoneNumberClient = createPhoneNumberClient();
        SyncPoller<Operation,SearchResult> syncPoller = phoneNumberClient
            .beginSearchPhoneNumber(countryCode, searchRequest);

        syncPoller.waitForCompletion();
        SearchResult searchResult = syncPoller.getFinalResult();

        System.out.println("SearchId: " + searchResult.getId());
        System.out.println("Monthly Rate: " + searchResult.getMonthlyRate().getValue());
        for (String phoneNumber : searchResult.getPhoneNumbers()) {
            System.out.println("Available Phone Number: " + phoneNumber);
        }

        return searchResult.getId();
    }

    /**
     * Sample code to purchase a phone number search
     */
    public void beginPurchasePhoneNumber() {
        String searchId = "SEARCH_ID";

        PhoneNumberClient phoneNumberClient = createPhoneNumberClient();
        SyncPoller<Operation, SearchResult> syncPoller = phoneNumberClient.beginPurchasePhoneNumber(searchId);
        syncPoller.waitForCompletion();

        System.out.println("Purchase Completed for the Search: " + searchId);
    }

    /**
     * Sample code to get the details of a search
     */
    public void getSearchResult() {
        String searchId = "SEARCH_ID";

        PhoneNumberClient phoneNumberClient = createPhoneNumberClient();
        SearchResult searchResult = phoneNumberClient.getSearchResult(searchId);

        System.out.println("SearchId: " + searchResult.getId());
        System.out.println("Search is for Assignment Type: " + searchResult.getAssignmentType());
        System.out.println("Search is for Number Type: " + searchResult.getNumberType());
        System.out.println("Search will expire at: " + searchResult.getSearchExpiresBy().toString());
    }

    /**
     * Sample code to list down all the acquired phone numbers
     */
    public void listAcquiredPhoneNumbers() {
        PhoneNumberClient phoneNumberClient = createPhoneNumberClient();
        PagedIterable<AcquiredPhoneNumber> acquiredPhoneNumbers = phoneNumberClient.listAcquiredPhoneNumbers();

        System.out.println("Displaying all acquired phone numbers");
        for (AcquiredPhoneNumber acquiredPhoneNumber : acquiredPhoneNumbers) {
            System.out.println(acquiredPhoneNumber.getPhoneNumber());
        }
    }

    /**
     * Sample code to get the details of an acquired phone number
     */
    public void getPhoneNumber() {
        String phoneNumber = "PHONE_NUMBER";

        PhoneNumberClient phoneNumberClient = createPhoneNumberClient();
        AcquiredPhoneNumber acquiredPhoneNumber = phoneNumberClient.getPhoneNumber(phoneNumber);

        System.out.println("Assignment Type: " + acquiredPhoneNumber.getAssignmentType());
        System.out.println("Number Type: " + acquiredPhoneNumber.getNumberType());
        System.out.println("SMS Capabilities: " + acquiredPhoneNumber.getCapabilities().getSms().toString());
        System.out.println("Calling Capabilities: " + acquiredPhoneNumber.getCapabilities().getCalling().toString());
        System.out.println("Purchased On Date: " + acquiredPhoneNumber.getPurchaseDate());
    }

    /**
     * Sample code to update a phone number
     */
    public void beginUpdatePhoneNumber() {
        String phoneNumber = "PHONE_NUMBER";

        Capabilities capabilities = new Capabilities()
            .setSms(CapabilityValue.INBOUND_OUTBOUND)
            .setCalling(CapabilityValue.INBOUND_OUTBOUND);

        AcquiredPhoneNumberUpdate acquiredPhoneNumberUpdate = new AcquiredPhoneNumberUpdate()
            .setCapabilities(capabilities);

        PhoneNumberClient phoneNumberClient = createPhoneNumberClient();
        SyncPoller<Operation, AcquiredPhoneNumber> syncPoller = phoneNumberClient
            .beginUpdatePhoneNumber(phoneNumber, acquiredPhoneNumberUpdate);

        AcquiredPhoneNumber acquiredPhoneNumber = syncPoller.getFinalResult();
        System.out.println("Updated SMS: " + acquiredPhoneNumber.getCapabilities().getSms().toString());
        System.out.println("Updated Calling: " + acquiredPhoneNumber.getCapabilities().getCalling().toString());
    }

    /**
     * Sample code to release a phone number
     */
    public void beginReleasePhoneNumber() {
        String phoneNumber = "PHONE_NUMBER";

        PhoneNumberClient phoneNumberClient = createPhoneNumberClient();
        SyncPoller<Operation, Void> syncPoller = phoneNumberClient.beginReleasePhoneNumber(phoneNumber);
        syncPoller.waitForCompletion();

        System.out.println("Released Phone Number: " + phoneNumber);
    }
}
