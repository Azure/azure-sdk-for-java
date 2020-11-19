// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.administration;

import com.azure.communication.administration.models.AcquiredPhoneNumber;
import com.azure.communication.administration.models.AreaCodes;
import com.azure.communication.administration.models.Capability;
import com.azure.communication.administration.models.CreateReservationOptions;
import com.azure.communication.administration.models.LocationOptionsQuery;
import com.azure.communication.administration.models.LocationOptionsResponse;
import com.azure.communication.administration.models.NumberConfigurationResponse;
import com.azure.communication.administration.models.NumberUpdateCapabilities;
import com.azure.communication.administration.models.PhoneNumberCountry;
import com.azure.communication.administration.models.PhoneNumberEntity;
import com.azure.communication.administration.models.PhoneNumberRelease;
import com.azure.communication.administration.models.PhoneNumberReservation;
import com.azure.communication.administration.models.PhonePlan;
import com.azure.communication.administration.models.PhonePlanGroup;
import com.azure.communication.administration.models.PstnConfiguration;
import com.azure.communication.administration.models.ReleaseStatus;
import com.azure.communication.administration.models.UpdateNumberCapabilitiesResponse;
import com.azure.communication.administration.models.UpdatePhoneNumberCapabilitiesResponse;
import com.azure.communication.common.PhoneNumber;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PhoneNumberClientIntegrationTest extends PhoneNumberIntegrationTestBase {
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listAllPhoneNumbers(HttpClient httpClient) {
        PagedIterable<AcquiredPhoneNumber> pagedIterable = this.getClient(httpClient).listAllPhoneNumbers(LOCALE);
        assertNotNull(pagedIterable.iterator().next().getPhoneNumber());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listPhonePlanGroups(HttpClient httpClient) {
        PagedIterable<PhonePlanGroup> pagedIterable =
            this.getClient(httpClient).listPhonePlanGroups(COUNTRY_CODE, LOCALE, true);

        assertNotNull(pagedIterable.iterator().next().getPhonePlanGroupId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listPhonePlans(HttpClient httpClient) {
        PagedIterable<PhonePlanGroup> phonePlanGroupsPagedIterable =
            this.getClient(httpClient).listPhonePlanGroups(COUNTRY_CODE, LOCALE, true);
        PagedIterable<PhonePlan> phonePlanPagedIterable =
            this.getClient(httpClient).listPhonePlans(COUNTRY_CODE, phonePlanGroupsPagedIterable.iterator().next().getPhonePlanGroupId(), LOCALE);

        assertNotNull(phonePlanPagedIterable.iterator().next().getPhonePlanId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listAllReleases(HttpClient httpClient) {
        PagedIterable<PhoneNumberEntity> pagedIterable = this.getClient(httpClient).listAllReleases();
        assertNotNull(pagedIterable.iterator().next().getId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listAllReservations(HttpClient httpClient) {
        PagedIterable<PhoneNumberEntity> pagedIterable = this.getClient(httpClient).listAllReservations();
        assertNotNull(pagedIterable.iterator().next().getId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listAllSupportedCountries(HttpClient httpClient) {
        PagedIterable<PhoneNumberCountry> pagedIterable = this.getClient(httpClient).listAllSupportedCountries(LOCALE);
        assertNotNull(pagedIterable.iterator().next().getCountryCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPhonePlanLocationOptions(HttpClient httpClient) {
        PagedIterable<PhonePlanGroup> phonePlanGroupsPagedIterable =
            this.getClient(httpClient).listPhonePlanGroups(COUNTRY_CODE, LOCALE, true);
        String phonePlanGroupId = phonePlanGroupsPagedIterable.iterator().next().getPhonePlanGroupId();
        PagedIterable<PhonePlan> phonePlanPagedIterable =
            this.getClient(httpClient).listPhonePlans(COUNTRY_CODE, phonePlanGroupId, LOCALE);
        LocationOptionsResponse response =
            this.getClient(httpClient).getPhonePlanLocationOptions(COUNTRY_CODE, phonePlanGroupId, phonePlanPagedIterable.iterator().next().getPhonePlanId(), LOCALE);
        assertNotNull(response.getLocationOptions().getLabelId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getAllAreaCodes(HttpClient httpClient) {
        PagedIterable<PhonePlanGroup> phonePlanGroupsPagedIterable =
            this.getClient(httpClient).listPhonePlanGroups(COUNTRY_CODE, LOCALE, true);
        String phonePlanGroupId = phonePlanGroupsPagedIterable.iterator().next().getPhonePlanGroupId();
        PagedIterable<PhonePlan> phonePlanPagedIterable =
            this.getClient(httpClient).listPhonePlans(COUNTRY_CODE, phonePlanGroupId, LOCALE);

        List<LocationOptionsQuery> locationOptions = new ArrayList<>();
        LocationOptionsQuery query = new LocationOptionsQuery();
        query.setLabelId("state");
        query.setOptionsValue(LOCATION_OPTION_STATE);
        locationOptions.add(query);

        query = new LocationOptionsQuery();
        query.setLabelId("city");
        query.setOptionsValue(LOCATION_OPTION_CITY);
        locationOptions.add(query);

        AreaCodes areaCodes =
            this.getClient(httpClient).getAllAreaCodes("selection", COUNTRY_CODE, phonePlanPagedIterable.iterator().next().getPhonePlanId(), locationOptions);

        assertTrue(areaCodes.getPrimaryAreaCodes().size() > 0);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getAllAreaCodesWithResponse(HttpClient httpClient) {
        PagedIterable<PhonePlanGroup> phonePlanGroupsPagedIterable =
            this.getClient(httpClient).listPhonePlanGroups(COUNTRY_CODE, LOCALE, true);
        String phonePlanGroupId = phonePlanGroupsPagedIterable.iterator().next().getPhonePlanGroupId();
        PagedIterable<PhonePlan> phonePlanPagedIterable =
            this.getClient(httpClient).listPhonePlans(COUNTRY_CODE, phonePlanGroupId, LOCALE);

        List<LocationOptionsQuery> locationOptions = new ArrayList<>();
        LocationOptionsQuery query = new LocationOptionsQuery();
        query.setLabelId("state");
        query.setOptionsValue(LOCATION_OPTION_STATE);
        locationOptions.add(query);

        query = new LocationOptionsQuery();
        query.setLabelId("city");
        query.setOptionsValue(LOCATION_OPTION_CITY);
        locationOptions.add(query);

        Response<AreaCodes> areaCodesResponse = this.getClient(httpClient).getAllAreaCodesWithResponse(
            "selection", COUNTRY_CODE, phonePlanPagedIterable.iterator().next().getPhonePlanId(), locationOptions, Context.NONE);

        assertEquals(200, areaCodesResponse.getStatusCode());
        assertTrue(areaCodesResponse.getValue().getPrimaryAreaCodes().size() > 0);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void beginCreateReservationGetReservationByIdCancelReservationSync(HttpClient httpClient) {
         // Setting up for phone number reservation creation
        PhoneNumberClient client = this.getClient(httpClient);
        PagedIterable<PhonePlanGroup> phonePlanGroupsPagedIterable =
            client.listPhonePlanGroups(COUNTRY_CODE, LOCALE, true);
        String phonePlanGroupId = phonePlanGroupsPagedIterable.iterator().next().getPhonePlanGroupId();
        PagedIterable<PhonePlan> phonePlanPagedIterable =
            client.listPhonePlans(COUNTRY_CODE, phonePlanGroupId, LOCALE);
        
        // Create reservation
        PhoneNumberReservation reservation = beginCreateReservation(httpClient, phonePlanPagedIterable.iterator().next()).getFinalResult();
        String reservationId = reservation.getReservationId();
        assertEquals(reservation.getPhoneNumbers().size(), 1);
        assertNotNull(reservationId);

        // Get reservation By Id
        PhoneNumberReservation search = client.getReservationById(reservationId);
        assertEquals(reservationId, search.getReservationId());

        // Cancel reservation
        client.cancelReservation(reservationId);
    }
    
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void beginCreateReservationGetReservationByIdCancelReservationWithResponseSync(HttpClient httpClient) {
         // Setting up for phone number reservation creation
        PhoneNumberClient client = this.getClient(httpClient);
        PagedIterable<PhonePlanGroup> phonePlanGroupsPagedIterable =
            client.listPhonePlanGroups(COUNTRY_CODE, LOCALE, true);
        String phonePlanGroupId = phonePlanGroupsPagedIterable.iterator().next().getPhonePlanGroupId();
        PagedIterable<PhonePlan> phonePlanPagedIterable =
            client.listPhonePlans(COUNTRY_CODE, phonePlanGroupId, LOCALE);
        
        // Create Reservation
        PhoneNumberReservation reservation = beginCreateReservation(httpClient, phonePlanPagedIterable.iterator().next()).getFinalResult();
        String reservationId = reservation.getReservationId();
        assertEquals(reservation.getPhoneNumbers().size(), 1);
        assertNotNull(reservationId);

        // Get reservation By Id
        Response<PhoneNumberReservation> search = client.getReservationByIdWithResponse(reservationId, Context.NONE);
        assertEquals(200, search.getStatusCode());
        assertEquals(reservationId, search.getValue().getReservationId());

        // Cancel reservation
        Response<Void> cancelResponse = client.cancelReservationWithResponse(reservationId, Context.NONE);
        assertEquals(202, cancelResponse.getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void beginCreateReservationBeginPurchaseReservationTestCapabilitiesWithResponseBeginReleasePhoneNumberSync(HttpClient httpClient) {
         // Setting up for phone number reservation creation
        PhoneNumberClient client = this.getClient(httpClient);
        PagedIterable<PhonePlanGroup> phonePlanGroupsPagedIterable =
            client.listPhonePlanGroups(COUNTRY_CODE, LOCALE, true);
        String phonePlanGroupId = phonePlanGroupsPagedIterable.iterator().next().getPhonePlanGroupId();
        PagedIterable<PhonePlan> phonePlanPagedIterable =
            client.listPhonePlans(COUNTRY_CODE, phonePlanGroupId, LOCALE);
        
        // Create reservation
        PhoneNumberReservation reservation = beginCreateReservation(httpClient, phonePlanPagedIterable.iterator().next()).getFinalResult();
        String reservationId = reservation.getReservationId();
        List<String> phoneNumbers = reservation.getPhoneNumbers();
        assertEquals(phoneNumbers.size(), 1);

        String phoneNumber = phoneNumbers.get(0);
        assertNotNull(reservationId);

        // Purchase reservation
        beginPurchaseReservation(httpClient, reservationId).getFinalResult();

        // Update capabilities with response
        List<Capability> capabilitiesToAdd = new ArrayList<>();
        capabilitiesToAdd.add(Capability.INBOUND_CALLING);

        NumberUpdateCapabilities update = new NumberUpdateCapabilities();
        update.setAdd(capabilitiesToAdd);

        Map<PhoneNumber, NumberUpdateCapabilities> updateMap = new HashMap<>();
        updateMap.put(new PhoneNumber(phoneNumber), update);

        Response<UpdateNumberCapabilitiesResponse> updateResponse =
            this.getClient(httpClient).updateCapabilitiesWithResponse(updateMap, Context.NONE);
        String capabilitiesUpdateId = updateResponse.getValue().getCapabilitiesUpdateId();
        assertEquals(200, updateResponse.getStatusCode());
        assertNotNull(capabilitiesUpdateId);

        // Get capabilities update
        Response<UpdatePhoneNumberCapabilitiesResponse> getResponse =
            this.getClient(httpClient).getCapabilitiesUpdateWithResponse(capabilitiesUpdateId, Context.NONE);
        assertEquals(200, getResponse.getStatusCode());
        assertEquals(capabilitiesUpdateId, getResponse.getValue().getCapabilitiesUpdateId());

        // Release phone number
        PhoneNumberRelease phoneNumberRelease = beginReleasePhoneNumbers(httpClient, phoneNumber).getFinalResult();
        assertEquals(ReleaseStatus.COMPLETE, phoneNumberRelease.getStatus());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void beginCreateReservationBeginPurchaseReservationTestConfigurationWithResponseBeginReleasePhoneNumberSync(HttpClient httpClient) {
         // Setting up for phone number reservation creation
        PhoneNumberClient client = this.getClient(httpClient);
        PagedIterable<PhonePlanGroup> phonePlanGroupsPagedIterable =
            client.listPhonePlanGroups(COUNTRY_CODE, LOCALE, true);
        String phonePlanGroupId = phonePlanGroupsPagedIterable.iterator().next().getPhonePlanGroupId();
        PagedIterable<PhonePlan> phonePlanPagedIterable =
            client.listPhonePlans(COUNTRY_CODE, phonePlanGroupId, LOCALE);
        
        // Create reservation
        PhoneNumberReservation reservation = beginCreateReservation(httpClient, phonePlanPagedIterable.iterator().next()).getFinalResult();
        String reservationId = reservation.getReservationId();
        List<String> phoneNumbers = reservation.getPhoneNumbers();
        assertEquals(phoneNumbers.size(), 1);

        String purchasedPhoneNumber = phoneNumbers.get(0);
        assertNotNull(reservationId);

        // Purchase reservation
        beginPurchaseReservation(httpClient, reservationId).getFinalResult();

        // Configure number with response
        PhoneNumber number = new PhoneNumber(purchasedPhoneNumber);
        PstnConfiguration pstnConfiguration = new PstnConfiguration();
        pstnConfiguration.setApplicationId("ApplicationId");
        pstnConfiguration.setCallbackUrl("https://callbackurl");
        Response<Void> configResponse = this.getClient(httpClient).configureNumberWithResponse(number, pstnConfiguration, Context.NONE);

        assertEquals(200, configResponse.getStatusCode());

        // Get number configuration with response
        Response<NumberConfigurationResponse> getResponse =
            this.getClient(httpClient).getNumberConfigurationWithResponse(number, Context.NONE);

        assertEquals(200, getResponse.getStatusCode());
        assertNotNull(getResponse.getValue().getPstnConfiguration().getApplicationId());
        assertNotNull(getResponse.getValue().getPstnConfiguration().getCallbackUrl());


        // Unconfigure number with response
        Response<Void> unconfigureResponse = this.getClient(httpClient).unconfigureNumberWithResponse(number, Context.NONE);
        assertEquals(200, unconfigureResponse.getStatusCode());

        // Release phone number
        PhoneNumberRelease phoneNumberRelease = beginReleasePhoneNumbers(httpClient, purchasedPhoneNumber).getFinalResult();
        assertEquals(ReleaseStatus.COMPLETE, phoneNumberRelease.getStatus());
    }

    private SyncPoller<PhoneNumberReservation, PhoneNumberReservation> beginCreateReservation(HttpClient httpClient, PhonePlan phonePlan) {
        List<String> phonePlanIds = new ArrayList<>();
        phonePlanIds.add(phonePlan.getPhonePlanId());

        CreateReservationOptions createReservationOptions = new CreateReservationOptions();
        createReservationOptions
            .setAreaCode("213")
            .setDescription(RESERVATION_OPTIONS_DESCRIPTION)
            .setDisplayName(RESERVATION_OPTIONS_NAME)
            .setPhonePlanIds(phonePlanIds)
            .setQuantity(1);

        Duration duration = Duration.ofSeconds(1);
        return this.getClient(httpClient).beginCreateReservation(createReservationOptions, duration);
    }

    private SyncPoller<Void, Void> beginPurchaseReservation(HttpClient httpClient, String reservationId) {
        Duration pollInterval = Duration.ofSeconds(1);
        return this.getClient(httpClient).beginPurchaseReservation(reservationId, pollInterval);
    }

    private SyncPoller<PhoneNumberRelease, PhoneNumberRelease> beginReleasePhoneNumbers(HttpClient httpClient, String phoneNumber) {
        PhoneNumber releasedPhoneNumber = new PhoneNumber(phoneNumber);
        List<PhoneNumber> phoneNumbers = new ArrayList<>();
        phoneNumbers.add(releasedPhoneNumber);
        Duration pollInterval = Duration.ofSeconds(1);
        return this.getClient(httpClient).beginReleasePhoneNumbers(phoneNumbers, pollInterval);
    }

    private PhoneNumberClient getClient(HttpClient httpClient) {
        return super.getClientBuilderWithConnectionString(httpClient).buildClient();
    }
}
