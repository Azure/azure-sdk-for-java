// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.administration;

import com.azure.communication.administration.models.AcquiredPhoneNumber;
import com.azure.communication.administration.models.AreaCodes;
import com.azure.communication.administration.models.Capability;
import com.azure.communication.administration.models.CreateReservationOptions;
import com.azure.communication.administration.models.CreateReservationResponse;
import com.azure.communication.administration.models.LocationOptionsQuery;
import com.azure.communication.administration.models.LocationOptionsResponse;
import com.azure.communication.administration.models.NumberConfigurationResponse;
import com.azure.communication.administration.models.NumberUpdateCapabilities;
import com.azure.communication.administration.models.PhoneNumberCountry;
import com.azure.communication.administration.models.PhoneNumberEntity;
import com.azure.communication.administration.models.PhoneNumberReservation;
import com.azure.communication.administration.models.PhonePlan;
import com.azure.communication.administration.models.PhonePlanGroup;
import com.azure.communication.administration.models.PstnConfiguration;
import com.azure.communication.administration.models.UpdateNumberCapabilitiesResponse;
import com.azure.communication.administration.models.UpdatePhoneNumberCapabilitiesResponse;
import com.azure.communication.common.PhoneNumber;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisabledIfEnvironmentVariable(
    named = "SKIP_PHONENUMBER_INTEGRATION_TESTS",
    matches = "(?i)(true)")
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
        PagedIterable<PhonePlan> pagedIterable =
            this.getClient(httpClient).listPhonePlans(COUNTRY_CODE, PHONE_PLAN_GROUP_ID, LOCALE);

        assertNotNull(pagedIterable.iterator().next().getPhonePlanId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listAllReleases(HttpClient httpClient) {
        PagedIterable<PhoneNumberEntity> pagedIterable = this.getClient(httpClient).listAllReleases();

        assertNotNull(pagedIterable.iterator().next().getId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listAllSearches(HttpClient httpClient) {
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
        LocationOptionsResponse response =
            this.getClient(httpClient).getPhonePlanLocationOptions(COUNTRY_CODE, PHONE_PLAN_GROUP_ID, PHONE_PLAN_ID, LOCALE);

        assertNotNull(response.getLocationOptions().getLabelId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getAllAreaCodes(HttpClient httpClient) {
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
            this.getClient(httpClient).getAllAreaCodes("selection", COUNTRY_CODE, PHONE_PLAN_ID, locationOptions);

        assertTrue(areaCodes.getPrimaryAreaCodes().size() > 0);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getAllAreaCodesWithResponse(HttpClient httpClient) {
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
            "selection", COUNTRY_CODE, PHONE_PLAN_ID, locationOptions, Context.NONE);

        assertEquals(200, areaCodesResponse.getStatusCode());
        assertTrue(areaCodesResponse.getValue().getPrimaryAreaCodes().size() > 0);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateCapabilities(HttpClient httpClient) {
        List<Capability> capabilitiesToAdd = new ArrayList<>();
        capabilitiesToAdd.add(Capability.INBOUND_CALLING);

        NumberUpdateCapabilities update = new NumberUpdateCapabilities();
        update.setAdd(capabilitiesToAdd);

        Map<PhoneNumber, NumberUpdateCapabilities> updateMap = new HashMap<>();
        updateMap.put(new PhoneNumber(PHONENUMBER_FOR_CAPABILITIES), update);

        UpdateNumberCapabilitiesResponse updateResponse = this.getClient(httpClient).updateCapabilities(updateMap);

        assertNotNull(updateResponse.getCapabilitiesUpdateId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateCapabilitiesWithResponse(HttpClient httpClient) {
        List<Capability> capabilitiesToAdd = new ArrayList<>();
        capabilitiesToAdd.add(Capability.INBOUND_CALLING);

        NumberUpdateCapabilities update = new NumberUpdateCapabilities();
        update.setAdd(capabilitiesToAdd);

        Map<PhoneNumber, NumberUpdateCapabilities> updateMap = new HashMap<>();
        updateMap.put(new PhoneNumber(PHONENUMBER_FOR_CAPABILITIES), update);

        Response<UpdateNumberCapabilitiesResponse> response =
            this.getClient(httpClient).updateCapabilitiesWithResponse(updateMap, Context.NONE);

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getValue().getCapabilitiesUpdateId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getCapabilitiesUpdate(HttpClient httpClient) {
        UpdatePhoneNumberCapabilitiesResponse updateResponse =
            this.getClient(httpClient).getCapabilitiesUpdate(CAPABILITIES_ID);

        assertNotNull(updateResponse.getCapabilitiesUpdateId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getCapabilitiesUpdateWithResponse(HttpClient httpClient) {
        Response<UpdatePhoneNumberCapabilitiesResponse> response =
            this.getClient(httpClient).getCapabilitiesUpdateWithResponse(CAPABILITIES_ID, Context.NONE);

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getValue().getCapabilitiesUpdateId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createReservation(HttpClient httpClient) {
        List<String> phonePlanIds = new ArrayList<>();
        phonePlanIds.add(PHONE_PLAN_ID);

        CreateReservationOptions createReservationOptions = new CreateReservationOptions();
        createReservationOptions
            .setAreaCode(AREA_CODE_FOR_SEARCH)
            .setDescription("318362fa-2b19-4062-92af-fa0673914f30")
            .setDisplayName("318362fa-2b19-4062-92af-fa0673914f30")
            .setPhonePlanIds(phonePlanIds)
            .setQuantity(1);

        CreateReservationResponse createReservationResponse = this.getClient(httpClient).createReservation(createReservationOptions);

        assertNotNull(createReservationResponse.getReservationId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createReservationWithResponse(HttpClient httpClient) {
        List<String> phonePlanIds = new ArrayList<>();
        phonePlanIds.add(PHONE_PLAN_ID);

        CreateReservationOptions createReservationOptions = new CreateReservationOptions();
        createReservationOptions
            .setAreaCode(AREA_CODE_FOR_SEARCH)
            .setDescription("318362fa-2b19-4062-92af-fa0673914f30")
            .setDisplayName("318362fa-2b19-4062-92af-fa0673914f30")
            .setPhonePlanIds(phonePlanIds)
            .setQuantity(1);

        Response<CreateReservationResponse> response =
            this.getClient(httpClient).createReservationWithResponse(createReservationOptions, Context.NONE);

        assertEquals(201, response.getStatusCode());
        assertNotNull(response.getValue().getReservationId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getReservationById(HttpClient httpClient) {
        PhoneNumberReservation search = this.getClient(httpClient).getReservationById(RESERVATION_ID);

        assertEquals(RESERVATION_ID, search.getReservationId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getReservationByIdWithResponse(HttpClient httpClient) {
        Response<PhoneNumberReservation> response = this.getClient(httpClient).getReservationByIdWithResponse(RESERVATION_ID, Context.NONE);

        assertEquals(200, response.getStatusCode());
        assertEquals(RESERVATION_ID, response.getValue().getReservationId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cancelSearch(HttpClient httpClient) {
        this.getClient(httpClient).cancelReservation(RESERVATION_ID_TO_CANCEL);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cancelSearchWithResponse(HttpClient httpClient) {
        Response<Void> response = this.getClient(httpClient).cancelReservationWithResponse(RESERVATION_ID_TO_CANCEL, Context.NONE);

        assertEquals(202, response.getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void configureNumber(HttpClient httpClient) {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_CONFIGURE);
        PstnConfiguration pstnConfiguration = new PstnConfiguration();
        pstnConfiguration.setApplicationId("ApplicationId");
        pstnConfiguration.setCallbackUrl("https://callbackurl");

        this.getClient(httpClient).configureNumber(number, pstnConfiguration);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void configureNumberWithResponse(HttpClient httpClient) {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_CONFIGURE);
        PstnConfiguration pstnConfiguration = new PstnConfiguration();
        pstnConfiguration.setApplicationId("ApplicationId");
        pstnConfiguration.setCallbackUrl("https://callbackurl");

        Response<Void> response = this.getClient(httpClient).configureNumberWithResponse(number, pstnConfiguration, Context.NONE);

        assertEquals(200, response.getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getNumberConfiguration(HttpClient httpClient) {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_GET_CONFIG);

        NumberConfigurationResponse numberConfig = this.getClient(httpClient).getNumberConfiguration(number);

        assertEquals("ApplicationId", numberConfig.getPstnConfiguration().getApplicationId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getNumberConfigurationWithResponse(HttpClient httpClient) {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_GET_CONFIG);

        Response<NumberConfigurationResponse> response =
            this.getClient(httpClient).getNumberConfigurationWithResponse(number, Context.NONE);

        assertEquals(200, response.getStatusCode());
        assertEquals("ApplicationId", response.getValue().getPstnConfiguration().getApplicationId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void unconfigureNumber(HttpClient httpClient) {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_UNCONFIGURE);
        this.getClient(httpClient).unconfigureNumber(number);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void unconfigureNumberWithResponse(HttpClient httpClient) {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_UNCONFIGURE);

        Response<Void> response = this.getClient(httpClient).unconfigureNumberWithResponse(number, Context.NONE);

        assertEquals(200, response.getStatusCode());
    }

    private PhoneNumberClient getClient(HttpClient httpClient) {
        return super.getClientBuilder(httpClient).buildClient();
    }
}
