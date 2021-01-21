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
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;

import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
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
        PagedIterable<AcquiredPhoneNumber> pagedIterable = this.getClientWithConnectionString(httpClient, "listAllPhoneNumbersSync").listAllPhoneNumbers(LOCALE);
        assertNotNull(pagedIterable.iterator().next().getPhoneNumber());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listAllPhoneNumbersWithContext(HttpClient httpClient) {
        PagedIterable<AcquiredPhoneNumber> pagedIterable = this.getClientWithConnectionString(httpClient, "listAllPhoneNumbersWithContextSync").listAllPhoneNumbers(LOCALE, Context.NONE);
        assertNotNull(pagedIterable.iterator().next().getPhoneNumber());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listPhonePlanGroups(HttpClient httpClient) {
        PagedIterable<PhonePlanGroup> pagedIterable =
            this.getClientWithConnectionString(httpClient, "listPhonePlanGroupsSync").listPhonePlanGroups(COUNTRY_CODE, LOCALE, true);

        assertNotNull(pagedIterable.iterator().next().getPhonePlanGroupId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listPhonePlanGroupsWithContext(HttpClient httpClient) {
        PagedIterable<PhonePlanGroup> pagedIterable =
            this.getClientWithConnectionString(httpClient, "listPhonePlanGroupsWithContextSync").listPhonePlanGroups(COUNTRY_CODE, LOCALE, true, Context.NONE);

        assertNotNull(pagedIterable.iterator().next().getPhonePlanGroupId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listPhonePlans(HttpClient httpClient) {
        PagedIterable<PhonePlanGroup> phonePlanGroupsPagedIterable =
            this.getClientWithConnectionString(httpClient, "listPhonePlansSync_listPlanGroups").listPhonePlanGroups(COUNTRY_CODE, LOCALE, true);
        PagedIterable<PhonePlan> phonePlanPagedIterable =
            this.getClientWithConnectionString(httpClient, "listPhonePlansSync").listPhonePlans(COUNTRY_CODE, phonePlanGroupsPagedIterable.iterator().next().getPhonePlanGroupId(), LOCALE);

        assertNotNull(phonePlanPagedIterable.iterator().next().getPhonePlanId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listPhonePlansWithContext(HttpClient httpClient) {
        PagedIterable<PhonePlanGroup> phonePlanGroupsPagedIterable =
            this.getClientWithConnectionString(httpClient, "listPhonePlansWithContextSync_listPlanGroups").listPhonePlanGroups(COUNTRY_CODE, LOCALE, true);
        PagedIterable<PhonePlan> phonePlanPagedIterable =
            this.getClientWithConnectionString(httpClient, "listPhonePlansWithContextSync").listPhonePlans(COUNTRY_CODE, phonePlanGroupsPagedIterable.iterator().next().getPhonePlanGroupId(), LOCALE, Context.NONE);

        assertNotNull(phonePlanPagedIterable.iterator().next().getPhonePlanId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPhonePlanLocationOptionsWithResponse(HttpClient httpClient) {
        PagedIterable<PhonePlanGroup> phonePlanGroupsPagedIterable =
            this.getClientWithConnectionString(httpClient, "getPhonePlanLocationOptionsWithResponseSync_listPlanGroups").listPhonePlanGroups(COUNTRY_CODE, LOCALE, true);
        String planGroupId =  phonePlanGroupsPagedIterable.iterator().next().getPhonePlanGroupId();
        PagedIterable<PhonePlan> phonePlanPagedIterable =
            this.getClientWithConnectionString(httpClient, "getPhonePlanLocationOptionsWithResponseSync_listPlans").listPhonePlans(COUNTRY_CODE, planGroupId, LOCALE, Context.NONE);
        String planId =  phonePlanPagedIterable.iterator().next().getPhonePlanId();
        Response<LocationOptionsResponse> locationOptionsResponse = this.getClientWithConnectionString(httpClient, "getPhonePlanLocationOptionsWithResponseSync").getPhonePlanLocationOptionsWithResponse(COUNTRY_CODE, planGroupId, planId, LOCALE, Context.NONE);
        assertEquals(locationOptionsResponse.getStatusCode(), 200);
        assertNotNull(locationOptionsResponse.getValue().getLocationOptions().getLabelId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listAllReleases(HttpClient httpClient) {
        PagedIterable<PhoneNumberEntity> pagedIterable = this.getClientWithConnectionString(httpClient, "listAllReleasesSync").listAllReleases();
        assertNotNull(pagedIterable.iterator().next().getId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listAllSupportedCountries(HttpClient httpClient) {
        PagedIterable<PhoneNumberCountry> pagedIterable = this.getClientWithConnectionString(httpClient, "listAllSupportedCountriesSync").listAllSupportedCountries(LOCALE);
        assertNotNull(pagedIterable.iterator().next().getCountryCode());
    }
    
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listAllSupportedCountriesWithContext(HttpClient httpClient) {
        PagedIterable<PhoneNumberCountry> pagedIterable = this.getClientWithConnectionString(httpClient, "listAllSupportedCountriesWithContextSync").listAllSupportedCountries(LOCALE, Context.NONE);
        assertNotNull(pagedIterable.iterator().next().getCountryCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listAllReservations(HttpClient httpClient) {
        PagedIterable<PhoneNumberEntity> pagedIterable = this.getClientWithConnectionString(httpClient, "listAllReservationsSync").listAllReservations(Context.NONE);
        assertNotNull(pagedIterable.iterator().next());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPhonePlanLocationOptions(HttpClient httpClient) {
        PagedIterable<PhonePlanGroup> phonePlanGroupsPagedIterable =
            this.getClientWithConnectionString(httpClient, "getPhonePlanLocationOptionsSync_listPlanGroups").listPhonePlanGroups(COUNTRY_CODE, LOCALE, true);
        String phonePlanGroupId = phonePlanGroupsPagedIterable.iterator().next().getPhonePlanGroupId();
        PagedIterable<PhonePlan> phonePlanPagedIterable =
            this.getClientWithConnectionString(httpClient, "getPhonePlanLocationOptionsSync_listPlans").listPhonePlans(COUNTRY_CODE, phonePlanGroupId, LOCALE);
        LocationOptionsResponse response =
            this.getClientWithConnectionString(httpClient, "getPhonePlanLocationOptionsSync").getPhonePlanLocationOptions(COUNTRY_CODE, phonePlanGroupId, phonePlanPagedIterable.iterator().next().getPhonePlanId(), LOCALE);
        assertNotNull(response.getLocationOptions().getLabelId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getAllAreaCodes(HttpClient httpClient) {
        PagedIterable<PhonePlanGroup> phonePlanGroupsPagedIterable =
            this.getClientWithConnectionString(httpClient, "getAllAreaCodesSync_listPlanGroups").listPhonePlanGroups(COUNTRY_CODE, LOCALE, true);
        String phonePlanGroupId = phonePlanGroupsPagedIterable.iterator().next().getPhonePlanGroupId();
        PagedIterable<PhonePlan> phonePlanPagedIterable =
            this.getClientWithConnectionString(httpClient, "getAllAreaCodesSync_listPlans").listPhonePlans(COUNTRY_CODE, phonePlanGroupId, LOCALE);

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
            this.getClientWithConnectionString(httpClient, "getAllAreaCodesSync").getAllAreaCodes("selection", COUNTRY_CODE, phonePlanPagedIterable.iterator().next().getPhonePlanId(), locationOptions);

        assertTrue(areaCodes.getPrimaryAreaCodes().size() > 0);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getAllAreaCodesWithResponse(HttpClient httpClient) {
        PagedIterable<PhonePlanGroup> phonePlanGroupsPagedIterable =
            this.getClientWithConnectionString(httpClient, "getAllAreaCodesWithResponseSync_listPlanGroups").listPhonePlanGroups(COUNTRY_CODE, LOCALE, true);
        String phonePlanGroupId = phonePlanGroupsPagedIterable.iterator().next().getPhonePlanGroupId();
        PagedIterable<PhonePlan> phonePlanPagedIterable =
            this.getClientWithConnectionString(httpClient, "getAllAreaCodesWithResponseSync_listPlans").listPhonePlans(COUNTRY_CODE, phonePlanGroupId, LOCALE);

        List<LocationOptionsQuery> locationOptions = new ArrayList<>();
        LocationOptionsQuery query = new LocationOptionsQuery();
        query.setLabelId("state");
        query.setOptionsValue(LOCATION_OPTION_STATE);
        locationOptions.add(query);

        query = new LocationOptionsQuery();
        query.setLabelId("city");
        query.setOptionsValue(LOCATION_OPTION_CITY);
        locationOptions.add(query);

        Response<AreaCodes> areaCodesResponse = this.getClientWithConnectionString(httpClient, "getAllAreaCodesWithResponseSync").getAllAreaCodesWithResponse(
            "selection", COUNTRY_CODE, phonePlanPagedIterable.iterator().next().getPhonePlanId(), locationOptions, Context.NONE);

        assertEquals(200, areaCodesResponse.getStatusCode());
        assertTrue(areaCodesResponse.getValue().getPrimaryAreaCodes().size() > 0);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void beginCreateReservationGetReservationByIdCancelReservationSync(HttpClient httpClient) {
         // Setting up for phone number reservation creation
        PagedIterable<PhonePlanGroup> phonePlanGroupsPagedIterable =
            this.getClientWithConnectionString(httpClient, "reservationTests_listPlanGroups").listPhonePlanGroups(COUNTRY_CODE, LOCALE, true);
        String phonePlanGroupId = phonePlanGroupsPagedIterable.iterator().next().getPhonePlanGroupId();
        PagedIterable<PhonePlan> phonePlanPagedIterable =
            this.getClientWithConnectionString(httpClient, "reservationTests_listPlans").listPhonePlans(COUNTRY_CODE, phonePlanGroupId, LOCALE);
        
        // Create reservation
        PhoneNumberReservation reservation = beginCreateReservation(httpClient, phonePlanPagedIterable.iterator().next(), "reservationTests_beginCreateReservation").getFinalResult();
        String reservationId = reservation.getReservationId();
        assertEquals(reservation.getPhoneNumbers().size(), 1);
        assertNotNull(reservationId);

        // Get reservation By Id
        PhoneNumberReservation search = this.getClientWithConnectionString(httpClient, "reservationTests_getReservationById").getReservationById(reservationId);
        assertEquals(reservationId, search.getReservationId());

        // Cancel reservation
        this.getClientWithConnectionString(httpClient, "reservationTests_cancelReservation").cancelReservation(reservationId);
    }
    
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void beginCreateReservationGetReservationByIdCancelReservationWithResponseSync(HttpClient httpClient) {
         // Setting up for phone number reservation creation
        PagedIterable<PhonePlanGroup> phonePlanGroupsPagedIterable =
            this.getClientWithConnectionString(httpClient, "reservationWithResponseTestsSync_listPlanGroups").listPhonePlanGroups(COUNTRY_CODE, LOCALE, true);
        String phonePlanGroupId = phonePlanGroupsPagedIterable.iterator().next().getPhonePlanGroupId();
        PagedIterable<PhonePlan> phonePlanPagedIterable =
            this.getClientWithConnectionString(httpClient, "reservationWithResponseTestsSync_listPlans").listPhonePlans(COUNTRY_CODE, phonePlanGroupId, LOCALE);
        
        // Create Reservation
        PhoneNumberReservation reservation = beginCreateReservation(httpClient, phonePlanPagedIterable.iterator().next(), "reservationWithResponseTestsSync_beginCreateReservation").getFinalResult();
        String reservationId = reservation.getReservationId();
        assertEquals(reservation.getPhoneNumbers().size(), 1);
        assertNotNull(reservationId);

        // Get reservation By Id
        Response<PhoneNumberReservation> search = this.getClientWithConnectionString(httpClient, "reservationWithResponseTestsSync_getReservationById").getReservationByIdWithResponse(reservationId, Context.NONE);
        assertEquals(200, search.getStatusCode());
        assertEquals(reservationId, search.getValue().getReservationId());

        // Cancel reservation
        Response<Void> cancelResponse = this.getClientWithConnectionString(httpClient, "reservationWithResponseTestsSync_cancelReservation").cancelReservationWithResponse(reservationId, Context.NONE);
        assertEquals(202, cancelResponse.getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)")
    public void purchaseReservationBeginReleasePhoneNumberSync(HttpClient httpClient) {
         // Setting up for phone number reservation creation
        PagedIterable<PhonePlanGroup> phonePlanGroupsPagedIterable =
            this.getClientWithConnectionString(httpClient, "purchaseReleaseNumberTestsSync_listPlanGroups").listPhonePlanGroups(COUNTRY_CODE, LOCALE, true);
        String phonePlanGroupId = phonePlanGroupsPagedIterable.iterator().next().getPhonePlanGroupId();
        PagedIterable<PhonePlan> phonePlanPagedIterable =
            this.getClientWithConnectionString(httpClient, "purchaseReleaseNumberTestsSync_listPlans").listPhonePlans(COUNTRY_CODE, phonePlanGroupId, LOCALE);
        
        // Create reservation
        PhoneNumberReservation reservation = beginCreateReservation(httpClient, phonePlanPagedIterable.iterator().next(), "purchaseReleaseNumberTestsSync_beginCreateReservation").getFinalResult();
        String reservationId = reservation.getReservationId();
        List<String> phoneNumbers = reservation.getPhoneNumbers();
        assertEquals(phoneNumbers.size(), 1);

        String phoneNumber = phoneNumbers.get(0);
        assertNotNull(reservationId);

        // Purchase reservation
        beginPurchaseReservation(httpClient, reservationId, "purchaseReleaseNumberTestsSync_beginPurchaseReservation").getFinalResult();

        // Release phone number
        PhoneNumberRelease phoneNumberRelease = beginReleasePhoneNumbers(httpClient, phoneNumber, "purchaseReleaseNumberTestsSync_beginReleasePhoneNumbers").getFinalResult();
        assertEquals(ReleaseStatus.COMPLETE, phoneNumberRelease.getStatus());

        // Get release by id
        PhoneNumberRelease getPhoneNumberRelease = this.getClientWithConnectionString(httpClient, "purchaseReleaseNumberTestsSync_getReleaseById").getReleaseById(phoneNumberRelease.getReleaseId());
        assertNotNull(getPhoneNumberRelease);

    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void configureNumberGetNumberConfigurationUnconfigureNumberWithResponse(HttpClient httpClient) {
        // Configure number with response
        PhoneNumberIdentifier number = new PhoneNumberIdentifier(PHONE_NUMBER);
        PstnConfiguration pstnConfiguration = new PstnConfiguration();
        pstnConfiguration.setApplicationId("ApplicationId");
        pstnConfiguration.setCallbackUrl("https://callbackurl");
        Response<Void> configResponse = this.getClientWithConnectionString(httpClient, "configureWithResponseTestsSync_configureNumber").configureNumberWithResponse(number, pstnConfiguration, Context.NONE);

        assertEquals(200, configResponse.getStatusCode());

        // Get number configuration with response
        Response<NumberConfigurationResponse> getResponse =
            this.getClientWithConnectionString(httpClient, "configureWithResponseTestsSync_getNumberConfig").getNumberConfigurationWithResponse(number, Context.NONE);

        assertEquals(200, getResponse.getStatusCode());
        assertNotNull(getResponse.getValue().getPstnConfiguration().getApplicationId());
        assertNotNull(getResponse.getValue().getPstnConfiguration().getCallbackUrl());


        // Unconfigure number with response
        Response<Void> unconfigureResponse = this.getClientWithConnectionString(httpClient, "configureWithResponseTestsSync_unconfigureNumber").unconfigureNumberWithResponse(number, Context.NONE);
        assertEquals(200, unconfigureResponse.getStatusCode());
    }

    
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void configureNumberGetNumberConfigurationUnconfigureNumber(HttpClient httpClient) {
        // Configure number with response
        PhoneNumberIdentifier number = new PhoneNumberIdentifier(PHONE_NUMBER);
        PstnConfiguration pstnConfiguration = new PstnConfiguration();
        pstnConfiguration.setApplicationId("ApplicationId");
        pstnConfiguration.setCallbackUrl("https://callbackurl");
        this.getClientWithConnectionString(httpClient, "configureTestsSync_configureNumber").configureNumber(number, pstnConfiguration);

        // Get number configuration with response
        NumberConfigurationResponse configResponse =
            this.getClientWithConnectionString(httpClient, "configureTestsSync_getNumberConfig").getNumberConfiguration(number);

        assertNotNull(configResponse.getPstnConfiguration().getApplicationId());
        assertNotNull(configResponse.getPstnConfiguration().getCallbackUrl());

        // Unconfigure number with response
        this.getClientWithConnectionString(httpClient, "configureTestsSync_unconfigureNumber").unconfigureNumber(number);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateCapabilitiesGetCapabilitiesUpdateWithResponse(HttpClient httpClient) {
        // Update capabilities with response
        List<Capability> capabilitiesToAdd = new ArrayList<>();
        capabilitiesToAdd.add(Capability.INBOUND_CALLING);

        NumberUpdateCapabilities update = new NumberUpdateCapabilities();
        update.setAdd(capabilitiesToAdd);

        Map<PhoneNumberIdentifier, NumberUpdateCapabilities> updateMap = new HashMap<>();
        updateMap.put(new PhoneNumberIdentifier(PHONE_NUMBER), update);

        Response<UpdateNumberCapabilitiesResponse> updateResponse =
            this.getClientWithConnectionString(httpClient, "capabilitiesWithResponseTestsSync_updateCapabilties").updateCapabilitiesWithResponse(updateMap, Context.NONE);
        String capabilitiesUpdateId = updateResponse.getValue().getCapabilitiesUpdateId();
        assertEquals(200, updateResponse.getStatusCode());
        assertNotNull(capabilitiesUpdateId);

        // Get capabilities update
        Response<UpdatePhoneNumberCapabilitiesResponse> getResponse =
            this.getClientWithConnectionString(httpClient, "capabilitiesWithResponseTestsSync_getCapabilitiesUpdate").getCapabilitiesUpdateWithResponse(capabilitiesUpdateId, Context.NONE);
        assertEquals(200, getResponse.getStatusCode());
        assertEquals(capabilitiesUpdateId, getResponse.getValue().getCapabilitiesUpdateId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateCapabilitiesGetCapabilitiesUpdate(HttpClient httpClient) {
        // Update capabilities with response
        List<Capability> capabilitiesToAdd = new ArrayList<>();
        capabilitiesToAdd.add(Capability.INBOUND_CALLING);

        NumberUpdateCapabilities update = new NumberUpdateCapabilities();
        update.setAdd(capabilitiesToAdd);

        Map<PhoneNumberIdentifier, NumberUpdateCapabilities> updateMap = new HashMap<>();
        updateMap.put(new PhoneNumberIdentifier(PHONE_NUMBER), update);

        UpdateNumberCapabilitiesResponse updateResponse =
            this.getClientWithConnectionString(httpClient, "capabilitiesTestsSync_updateCapabilties").updateCapabilities(updateMap);
        String capabilitiesUpdateId = updateResponse.getCapabilitiesUpdateId();
        assertNotNull(capabilitiesUpdateId);

        // Get capabilities update
        UpdatePhoneNumberCapabilitiesResponse getResponse =
            this.getClientWithConnectionString(httpClient, "capabilitiesTestsSync_getCapabilitiesUpdate").getCapabilitiesUpdate(capabilitiesUpdateId);
        assertEquals(capabilitiesUpdateId, getResponse.getCapabilitiesUpdateId());
    }

    private SyncPoller<PhoneNumberReservation, PhoneNumberReservation> beginCreateReservation(HttpClient httpClient, PhonePlan phonePlan, String testName) {
        List<String> phonePlanIds = new ArrayList<>();
        phonePlanIds.add(phonePlan.getPhonePlanId());

        CreateReservationOptions createReservationOptions = new CreateReservationOptions();
        createReservationOptions
            .setAreaCode(AREA_CODE)
            .setDescription(RESERVATION_OPTIONS_DESCRIPTION)
            .setDisplayName(RESERVATION_OPTIONS_NAME)
            .setPhonePlanIds(phonePlanIds)
            .setQuantity(1);

        Duration duration = Duration.ofSeconds(1);
        return this.getClientWithConnectionString(httpClient, testName).beginCreateReservation(createReservationOptions, duration);
    }

    private SyncPoller<Void, Void> beginPurchaseReservation(HttpClient httpClient, String reservationId, String testName) {
        Duration pollInterval = Duration.ofSeconds(1);
        return this.getClientWithConnectionString(httpClient, testName).beginPurchaseReservation(reservationId, pollInterval);
    }

    private SyncPoller<PhoneNumberRelease, PhoneNumberRelease> beginReleasePhoneNumbers(HttpClient httpClient, String phoneNumber, String testName) {
        PhoneNumberIdentifier releasedPhoneNumber = new PhoneNumberIdentifier(phoneNumber);
        List<PhoneNumberIdentifier> phoneNumbers = new ArrayList<>();
        phoneNumbers.add(releasedPhoneNumber);
        Duration pollInterval = Duration.ofSeconds(1);
        return this.getClientWithConnectionString(httpClient, testName).beginReleasePhoneNumbers(phoneNumbers, pollInterval);
    }

    private PhoneNumberClient getClientWithConnectionString(HttpClient httpClient, String testName) {
        PhoneNumberClientBuilder builder = super.getClientBuilderWithConnectionString(httpClient);
        return addLoggingPolicy(builder, testName).buildClient();
    }
}
