// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.administration;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.azure.communication.administration.models.AcquiredPhoneNumber;
import com.azure.communication.administration.models.AreaCodes;
import com.azure.communication.administration.models.CreateReservationOptions;
import com.azure.communication.administration.models.LocationOptions;
import com.azure.communication.administration.models.LocationOptionsDetails;
import com.azure.communication.administration.models.LocationOptionsQuery;
import com.azure.communication.administration.models.PhoneNumberCountry;
import com.azure.communication.administration.models.PhoneNumberRelease;
import com.azure.communication.administration.models.PhoneNumberReservation;
import com.azure.communication.administration.models.PhonePlan;
import com.azure.communication.administration.models.PhonePlanGroup;
import com.azure.communication.administration.models.PstnConfiguration;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

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
    public CommunicationUserIdentifier createNewUser() {
        CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();
        CommunicationUserIdentifier user = communicationIdentityClient.createUser();
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
        CommunicationUserIdentifier user = communicationIdentityClient.createUser();
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
        CommunicationUserIdentifier user = createNewUser();
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
        CommunicationUserIdentifier user = communicationIdentityClient.createUser();
        // delete a previously created user
        communicationIdentityClient.deleteUser(user);
    }

    /**
     * Sample code for creating a sync Phone Number Client.
     *
     * @return the Phone Number Client.
     */
    public PhoneNumberClient createPhoneNumberClient() {
        // You can find your endpoint and access token from your resource in the Azure Portal
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
     * Sample code to get all supported countries
     *
     * @return supported countries
     */
    public PagedIterable<PhoneNumberCountry> getSupportedCountries() {
        String locale = "en-us";
        PhoneNumberClient phoneNumberClient = createPhoneNumberClient();

        PagedIterable<PhoneNumberCountry> phoneNumberCountries = phoneNumberClient
            .listAllSupportedCountries(locale);

        for (PhoneNumberCountry phoneNumberCountry
            : phoneNumberCountries) {
            System.out.println("Phone Number Country Code: " + phoneNumberCountry.getCountryCode());
            System.out.println("Phone Number Country Name: " + phoneNumberCountry.getLocalizedName());
        }

        return phoneNumberCountries;
    }

    /**
     * Sample code to get a list of all acquired phone numbers
     *
     * @return the acquired phone numbers
     */
    public PagedIterable<AcquiredPhoneNumber> getAcquiredPhoneNumbers() {
        String locale = "en-us";
        PhoneNumberClient phoneNumberClient = createPhoneNumberClient();

        PagedIterable<AcquiredPhoneNumber> acquiredPhoneNumbers = phoneNumberClient
            .listAllPhoneNumbers(locale);

        for (AcquiredPhoneNumber acquiredPhoneNumber
            : acquiredPhoneNumbers) {
            System.out.println("Acquired Phone Number: " + acquiredPhoneNumber.getPhoneNumber());
        }

        return acquiredPhoneNumbers;
    }

    /**
     * Sample code to get a list of all phone plan groups
     *
     * @return phone plans groups
     */
    public PagedIterable<PhonePlanGroup> getPhonePlanGroups() {
        String countryCode = "US";
        String locale = "en-us";
        PhoneNumberClient phoneNumberClient = createPhoneNumberClient();

        PagedIterable<PhonePlanGroup> phonePlanGroups = phoneNumberClient
            .listPhonePlanGroups(countryCode, locale, true);

        for (PhonePlanGroup phonePlanGroup
            : phonePlanGroups) {
            System.out.println("Phone Plan GroupId: " + phonePlanGroup.getPhonePlanGroupId());
            System.out.println("Phone Plan NumberType: " + phonePlanGroup.getPhoneNumberType());
        }

        return phonePlanGroups;
    }

    /**
     * Sample code to get a list of all phone plan instances in a group
     *
     * @return phone plans
     */
    public PagedIterable<PhonePlan> getPhonePlansInGroup() {
        String countryCode = "US";
        String locale = "en-us";
        String phonePlanGroupId = "PHONE_PLAN_GROUP_ID";
        PhoneNumberClient phoneNumberClient = createPhoneNumberClient();

        PagedIterable<PhonePlan> phonePlans = phoneNumberClient
            .listPhonePlans(countryCode, phonePlanGroupId, locale);

        for (PhonePlan phonePlan
            : phonePlans) {
            System.out.println("Phone Plan Id: " + phonePlan.getPhonePlanId());
            System.out.println("Phone Plan Name: " + phonePlan.getLocalizedName());
            System.out.println("Phone Plan Capabilities: " + phonePlan.getCapabilities());
            System.out.println("Phone Plan Area Codes: " + phonePlan.getAreaCodes());
        }

        return phonePlans;
    }

    /**
     * Sample code to get the location options for a phone plan
     *
     * @return Location Options for a phone plan
     */
    public LocationOptions getPhonePlanLocationOptions() {
        String countryCode = "US";
        String locale = "en-us";
        String phonePlanGroupId = "PHONE_PLAN_GROUP_ID";
        String phonePlanId = "PHONE_PLAN_ID";
        PhoneNumberClient phoneNumberClient = createPhoneNumberClient();

        LocationOptions locationOptions = phoneNumberClient
            .getPhonePlanLocationOptions(countryCode, phonePlanGroupId, phonePlanId, locale)
            .getLocationOptions();

        System.out.println("Getting LocationOptions for: " + locationOptions.getLabelId());
        for (LocationOptionsDetails locationOptionsDetails
            : locationOptions.getOptions()) {
            System.out.println(locationOptionsDetails.getValue());
            for (LocationOptions locationOptions1
                : locationOptionsDetails.getLocationOptions()) {
                System.out.println("Getting LocationOptions for: " + locationOptions1.getLabelId());
                for (LocationOptionsDetails locationOptionsDetails1
                    : locationOptions1.getOptions()) {
                    System.out.println(locationOptionsDetails1.getValue());
                }
            }
        }

        return locationOptions;
    }

    /**
     * Sample code to get the area codes for a location
     *
     * @return Area Codes for a location
     */
    public AreaCodes getAreaCodes() {
        String countryCode = "US";
        String phonePlanId = "PHONE_PLAN_ID";

        List<LocationOptionsQuery> locationOptions = new ArrayList<>();
        LocationOptionsQuery query = new LocationOptionsQuery();
        query.setLabelId("state");
        query.setOptionsValue("LOCATION_OPTION_STATE");
        locationOptions.add(query);

        query = new LocationOptionsQuery();
        query.setLabelId("city");
        query.setOptionsValue("LOCATION_OPTION_CITY");
        locationOptions.add(query);
        PhoneNumberClient phoneNumberClient = createPhoneNumberClient();

        AreaCodes areaCodes = phoneNumberClient
            .getAllAreaCodes("selection", countryCode, phonePlanId, locationOptions);

        for (String areaCode
            : areaCodes.getPrimaryAreaCodes()) {
            System.out.println(areaCode);
        }

        return areaCodes;
    }

    /**
     * Sample code to configure a phone number
     */
    public void configurePhoneNumber() {
        PhoneNumberIdentifier phoneNumber = new PhoneNumberIdentifier("PHONENUMBER_TO_CONFIGURE");
        PstnConfiguration pstnConfiguration = new PstnConfiguration();
        pstnConfiguration.setApplicationId("APPLICATION_ID");
        pstnConfiguration.setCallbackUrl("CALLBACK_URL");
        PhoneNumberClient phoneNumberClient = createPhoneNumberClient();
        phoneNumberClient.configureNumber(phoneNumber, pstnConfiguration);
    }

    /**
     * Sample code to create a reservation as a long running operation
     */
    public void beginCreateReservation() {
        String phonePlanId = "PHONE_PLAN_ID";

        List<String> phonePlanIds = new ArrayList<>();
        phonePlanIds.add(phonePlanId);

        CreateReservationOptions createReservationOptions = new CreateReservationOptions();
        createReservationOptions
            .setAreaCode("AREA_CODE_FOR_RESERVATION")
            .setDescription("DESCRIPTION_FOR_RESERVATION")
            .setDisplayName("NAME_FOR_RESERVATION")
            .setPhonePlanIds(phonePlanIds)
            .setQuantity(2);

        Duration duration = Duration.ofSeconds(1);
        PhoneNumberClient phoneNumberClient = createPhoneNumberClient();

        SyncPoller<PhoneNumberReservation, PhoneNumberReservation> res =
            phoneNumberClient.beginCreateReservation(createReservationOptions, duration);
        res.waitForCompletion();
        PhoneNumberReservation result = res.getFinalResult();

        System.out.println("Reservation Id: " + result.getReservationId());
        for (String phoneNumber: result.getPhoneNumbers()) {
            System.out.println("Phone Number: " + phoneNumber);
        }
    }

    /**
     * Sample code to purchase a reservation as a long running operation
     */
    public void beginPurchaseReservation() {
        Duration duration = Duration.ofSeconds(1);
        String phoneNumberReservationId = "RESERVATION_ID_TO_PURCHASE";
        PhoneNumberClient phoneNumberClient = createPhoneNumberClient();

        SyncPoller<Void, Void> res =
            phoneNumberClient.beginPurchaseReservation(phoneNumberReservationId, duration);
        res.waitForCompletion();
    }

    /**
     * Sample code to release a phone number as a long running operation
     */
    public void beginReleasePhoneNumbers() {
        Duration duration = Duration.ofSeconds(1);
        PhoneNumberIdentifier phoneNumber = new PhoneNumberIdentifier("PHONE_NUMBER_TO_RELEASE");
        List<PhoneNumberIdentifier> phoneNumbers = new ArrayList<>();
        phoneNumbers.add(phoneNumber);
        PhoneNumberClient phoneNumberClient = createPhoneNumberClient();

        SyncPoller<PhoneNumberRelease, PhoneNumberRelease> res =
            phoneNumberClient.beginReleasePhoneNumbers(phoneNumbers, duration);
        res.waitForCompletion();
        PhoneNumberRelease result = res.getFinalResult();
        System.out.println("Phone number release status: " + result.getStatus());
    }

    /**
     * Sample code for creating a sync Communication Identity Client using AAD authentication.
     *
     * @return the Communication Identity Client.
     */
    public CommunicationIdentityClient createCommunicationIdentityClientWithAAD() {
        // You can find your endpoint and access key from your resource in the Azure Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";

        // Create an HttpClient builder of your choice and customize it
        HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

        CommunicationIdentityClient communicationIdentityClient = new CommunicationIdentityClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpClient(httpClient)
            .buildClient();

        return communicationIdentityClient;
    }
}
