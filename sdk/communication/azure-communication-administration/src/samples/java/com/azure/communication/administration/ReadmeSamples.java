// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.administration;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.azure.communication.administration.models.AcquiredPhoneNumber;
import com.azure.communication.administration.models.AreaCodes;
import com.azure.communication.administration.models.LocationOptions;
import com.azure.communication.administration.models.LocationOptionsDetails;
import com.azure.communication.administration.models.LocationOptionsQuery;
import com.azure.communication.administration.models.PhoneNumberCountry;
import com.azure.communication.administration.models.PhonePlan;
import com.azure.communication.administration.models.PhonePlanGroup;
import com.azure.communication.common.CommunicationClientCredential;
import com.azure.communication.common.CommunicationUser;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.rest.PagedIterable;

public class ReadmeSamples {

    /**
     * Sample code for creating a sync Communication Identity Client.
     *
     * @return the Communication Identity Client.
     * @throws NoSuchAlgorithmException if Communication Client Credential HMAC not available
     * @throws InvalidKeyException if Communication Client Credential access key is not valid
     */
    public CommunicationIdentityClient createCommunicationIdentityClient()
            throws InvalidKeyException, NoSuchAlgorithmException {
        // You can find your endpoint and access token from your resource in the Azure Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
        String accessToken = "SECRET";

        // Create an HttpClient builder of your choice and customize it
        HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

        CommunicationIdentityClient communicationIdentityClient = new CommunicationIdentityClientBuilder()
            .endpoint(endpoint)
            .credential(new CommunicationClientCredential(accessToken))
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
        try {
            CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();
            CommunicationUser user = communicationIdentityClient.createUser();
            System.out.println("User id: " + user.getId());
            return user;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Sample code for issuing a user token
     *
     * @return the issued user token
     */
    public CommunicationUserToken issueUserToken() {
        try {
            CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();
            CommunicationUser user = communicationIdentityClient.createUser();
            List<String> scopes = new ArrayList<>(Arrays.asList("chat"));
            CommunicationUserToken userToken = communicationIdentityClient.issueToken(user, scopes);
            System.out.println("Token: " + userToken.getToken());
            System.out.println("Expires On: " + userToken.getExpiresOn());
            return userToken;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

     /**
      * Sample code for revoking user token
      */
    public void revokeUserToken() {
        try {
            CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();
            CommunicationUser user = createNewUser();
            List<String> scopes = new ArrayList<>(Arrays.asList("chat"));
            communicationIdentityClient.issueToken(user, scopes);
            // revoke tokens issued for the user prior to now
            communicationIdentityClient.revokeTokens(user, OffsetDateTime.now());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sample code for deleting user
     */
    public void deleteUser() {
        try {
            CommunicationIdentityClient communicationIdentityClient = createCommunicationIdentityClient();
            CommunicationUser user = communicationIdentityClient.createUser();
            // delete a previously created user
            communicationIdentityClient.deleteUser(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sample code for creating a sync Phone Number Client.
     *
     * @return the Phone Number Client.
     * @throws NoSuchAlgorithmException if Communication Client Credential HMAC not available
     * @throws InvalidKeyException if Communication Client Credential access key is not valid
     */
    public PhoneNumberClient createPhoneNumberClient()
        throws NoSuchAlgorithmException, InvalidKeyException {
        // You can find your endpoint and access token from your resource in the Azure Portal
        String endpoint = "https://<RESOURCE_NAME>.communication.azure.com";
        String accessToken = "SECRET";

        // Create an HttpClient builder of your choice and customize it
        HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();

        PhoneNumberClient phoneNumberClient = new PhoneNumberClientBuilder()
            .endpoint(endpoint)
            .credential(new CommunicationClientCredential(accessToken))
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

        try {
            PhoneNumberClient phoneNumberClient = createPhoneNumberClient();

            PagedIterable<PhoneNumberCountry> phoneNumberCountries = phoneNumberClient
                .listAllSupportedCountries(locale);

            for (PhoneNumberCountry phoneNumberCountry
                : phoneNumberCountries) {
                System.out.println("Phone Number Country Code: " + phoneNumberCountry.getCountryCode());
                System.out.println("Phone Number Country Name: " + phoneNumberCountry.getLocalizedName());
            }

            return phoneNumberCountries;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Sample code to get a list of all acquired phone numbers
     *
     * @return the acquired phone numbers
     */
    public PagedIterable<AcquiredPhoneNumber> getAcquiredPhoneNumbers() {
        String locale = "en-us";

        try {
            PhoneNumberClient phoneNumberClient = createPhoneNumberClient();

            PagedIterable<AcquiredPhoneNumber> acquiredPhoneNumbers = phoneNumberClient
                .listAllPhoneNumbers(locale);

            for (AcquiredPhoneNumber acquiredPhoneNumber
                : acquiredPhoneNumbers) {
                System.out.println("Acquired Phone Number: " + acquiredPhoneNumber.getPhoneNumber());
            }

            return acquiredPhoneNumbers;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Sample code to get a list of all phone plan groups
     *
     * @return phone plans groups
     */
    public PagedIterable<PhonePlanGroup> getPhonePlanGroups() {
        String countryCode = "US";
        String locale = "en-us";

        try {
            PhoneNumberClient phoneNumberClient = createPhoneNumberClient();

            PagedIterable<PhonePlanGroup> phonePlanGroups = phoneNumberClient
                .listPhonePlanGroups(countryCode, locale, true);

            for (PhonePlanGroup phonePlanGroup
                : phonePlanGroups) {
                System.out.println("Phone Plan GroupId: " + phonePlanGroup.getPhonePlanGroupId());
                System.out.println("Phone Plan NumberType: " + phonePlanGroup.getPhoneNumberType());
            }

            return phonePlanGroups;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Sample code to get the area codes for a location
     *
     * @return Area Codes for a location
     */
    public AreaCodes getAreaCodes() {
        String countryCode = "US";
        String phonePlanId = "phone-plan-id-1";

        List<LocationOptionsQuery> locationOptions = new ArrayList<>();
        LocationOptionsQuery query = new LocationOptionsQuery();
        query.setLabelId("state");
        query.setOptionsValue("LOCATION_OPTION_STATE");
        locationOptions.add(query);

        query = new LocationOptionsQuery();
        query.setLabelId("city");
        query.setOptionsValue("LOCATION_OPTION_CITY");
        locationOptions.add(query);

        try {
            PhoneNumberClient phoneNumberClient = createPhoneNumberClient();

            AreaCodes areaCodes = phoneNumberClient
                .getAllAreaCodes("selection", countryCode, phonePlanId, locationOptions);

            for (String areaCode
                : areaCodes.getPrimaryAreaCodes()) {
                System.out.println(areaCode);
            }

            return areaCodes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
