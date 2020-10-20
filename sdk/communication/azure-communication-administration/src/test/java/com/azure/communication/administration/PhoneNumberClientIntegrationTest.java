// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.administration;

import com.azure.communication.administration.models.AcquiredPhoneNumber;
import com.azure.communication.administration.models.AreaCodes;
import com.azure.communication.administration.models.Capability;
import com.azure.communication.administration.models.CreateSearchOptions;
import com.azure.communication.administration.models.CreateSearchResponse;
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
import com.azure.communication.administration.models.ReleaseResponse;
import com.azure.communication.administration.models.UpdateNumberCapabilitiesResponse;
import com.azure.communication.administration.models.UpdatePhoneNumberCapabilitiesResponse;
import com.azure.communication.common.PhoneNumber;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

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
    @Test()
    public void listAllPhoneNumbers() {
        PagedIterable<AcquiredPhoneNumber> pagedIterable = this.getClient().listAllPhoneNumbers(LOCALE);

        assertNotNull(pagedIterable.iterator().next().getPhoneNumber());
    }

    @Test()
    public void listPhonePlanGroups() {
        PagedIterable<PhonePlanGroup> pagedIterable =
            this.getClient().listPhonePlanGroups(COUNTRY_CODE, LOCALE, true);

        assertNotNull(pagedIterable.iterator().next().getPhonePlanGroupId());
    }

    @Test()
    public void listPhonePlans() {
        PagedIterable<PhonePlan> pagedIterable =
            this.getClient().listPhonePlans(COUNTRY_CODE, PHONE_PLAN_GROUP_ID, LOCALE);

        assertNotNull(pagedIterable.iterator().next().getPhonePlanId());
    }

    @Test()
    public void listAllReleases() {
        PagedIterable<PhoneNumberEntity> pagedIterable = this.getClient().listAllReleases();

        assertNotNull(pagedIterable.iterator().next().getId());
    }

    @Test()
    public void listAllSearches() {
        PagedIterable<PhoneNumberEntity> pagedIterable = this.getClient().listAllSearches();

        assertNotNull(pagedIterable.iterator().next().getId());
    }

    @Test()
    public void listAllSupportedCountries() {
        PagedIterable<PhoneNumberCountry> pagedIterable = this.getClient().listAllSupportedCountries(LOCALE);

        assertNotNull(pagedIterable.iterator().next().getCountryCode());
    }

    @Test()
    public void getPhonePlanLocationOptions() {
        LocationOptionsResponse response =
            this.getClient().getPhonePlanLocationOptions(COUNTRY_CODE, PHONE_PLAN_GROUP_ID, PHONE_PLAN_ID, LOCALE);

        assertNotNull(response.getLocationOptions().getLabelId());
    }

    @Test()
    public void getAllAreaCodes() {
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
            this.getClient().getAllAreaCodes("selection", COUNTRY_CODE, PHONE_PLAN_ID, locationOptions);

        assertTrue(areaCodes.getPrimaryAreaCodes().size() > 0);
    }

    @Test()
    public void getAllAreaCodesWithResponse() {
        List<LocationOptionsQuery> locationOptions = new ArrayList<>();
        LocationOptionsQuery query = new LocationOptionsQuery();
        query.setLabelId("state");
        query.setOptionsValue(LOCATION_OPTION_STATE);
        locationOptions.add(query);

        query = new LocationOptionsQuery();
        query.setLabelId("city");
        query.setOptionsValue(LOCATION_OPTION_CITY);
        locationOptions.add(query);

        Response<AreaCodes> areaCodesResponse = this.getClient().getAllAreaCodesWithResponse(
            "selection", COUNTRY_CODE, PHONE_PLAN_ID, locationOptions, Context.NONE);

        assertEquals(200, areaCodesResponse.getStatusCode());
        assertTrue(areaCodesResponse.getValue().getPrimaryAreaCodes().size() > 0);
    }

    @Test()
    public void updateCapabilities() {
        List<Capability> capabilitiesToAdd = new ArrayList<>();
        capabilitiesToAdd.add(Capability.INBOUND_CALLING);

        NumberUpdateCapabilities update = new NumberUpdateCapabilities();
        update.setAdd(capabilitiesToAdd);

        Map<PhoneNumber, NumberUpdateCapabilities> updateMap = new HashMap<>();
        updateMap.put(new PhoneNumber(PHONENUMBER_FOR_CAPABILITIES), update);

        UpdateNumberCapabilitiesResponse updateResponse = this.getClient().updateCapabilities(updateMap);

        assertNotNull(updateResponse.getCapabilitiesUpdateId());
    }

    @Test()
    public void updateCapabilitiesWithResponse() {
        List<Capability> capabilitiesToAdd = new ArrayList<>();
        capabilitiesToAdd.add(Capability.INBOUND_CALLING);

        NumberUpdateCapabilities update = new NumberUpdateCapabilities();
        update.setAdd(capabilitiesToAdd);

        Map<PhoneNumber, NumberUpdateCapabilities> updateMap = new HashMap<>();
        updateMap.put(new PhoneNumber(PHONENUMBER_FOR_CAPABILITIES), update);

        Response<UpdateNumberCapabilitiesResponse> response =
            this.getClient().updateCapabilitiesWithResponse(updateMap, Context.NONE);

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getValue().getCapabilitiesUpdateId());
    }

    @Test()
    public void getCapabilitiesUpdate() {
        UpdatePhoneNumberCapabilitiesResponse updateResponse =
            this.getClient().getCapabilitiesUpdate(CAPABILITIES_ID);

        assertNotNull(updateResponse.getCapabilitiesUpdateId());
    }

    @Test()
    public void getCapabilitiesUpdateWithResponse() {
        Response<UpdatePhoneNumberCapabilitiesResponse> response =
            this.getClient().getCapabilitiesUpdateWithResponse(CAPABILITIES_ID, Context.NONE);

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getValue().getCapabilitiesUpdateId());
    }

    @Test()
    public void createSearch() {
        List<String> phonePlanIds = new ArrayList<>();
        phonePlanIds.add(PHONE_PLAN_ID);

        CreateSearchOptions createSearchOptions = new CreateSearchOptions();
        createSearchOptions
            .setAreaCode(AREA_CODE_FOR_SEARCH)
            .setDescription("318362fa-2b19-4062-92af-fa0673914f30")
            .setDisplayName("318362fa-2b19-4062-92af-fa0673914f30")
            .setPhonePlanIds(phonePlanIds)
            .setQuantity(1);

        CreateSearchResponse createSearchResponse = this.getClient().createSearch(createSearchOptions);

        assertNotNull(createSearchResponse.getSearchId());
    }

    @Test()
    public void createSearchWithResponse() {
        List<String> phonePlanIds = new ArrayList<>();
        phonePlanIds.add(PHONE_PLAN_ID);

        CreateSearchOptions createSearchOptions = new CreateSearchOptions();
        createSearchOptions
            .setAreaCode(AREA_CODE_FOR_SEARCH)
            .setDescription("318362fa-2b19-4062-92af-fa0673914f30")
            .setDisplayName("318362fa-2b19-4062-92af-fa0673914f30")
            .setPhonePlanIds(phonePlanIds)
            .setQuantity(1);

        Response<CreateSearchResponse> response =
            this.getClient().createSearchWithResponse(createSearchOptions, Context.NONE);

        assertEquals(201, response.getStatusCode());
        assertNotNull(response.getValue().getSearchId());
    }

    @Test()
    public void getSearchById() {
        PhoneNumberReservation search = this.getClient().getSearchById(SEARCH_ID);

        assertEquals(SEARCH_ID, search.getSearchId());
    }

    @Test()
    public void getSearchByIdWithResponse() {
        Response<PhoneNumberReservation> response = this.getClient().getSearchByIdWithResponse(SEARCH_ID, Context.NONE);

        assertEquals(200, response.getStatusCode());
        assertEquals(SEARCH_ID, response.getValue().getSearchId());
    }

    @Test()
    public void purchaseSearch() {
        this.getClient().purchaseSearch(SEARCH_ID_TO_PURCHASE);
    }

    @Test()
    public void purchaseSearchWithResponse() {
        Response<Void> response = this.getClient().purchaseSearchWithResponse(SEARCH_ID_TO_PURCHASE, Context.NONE);

        assertEquals(202, response.getStatusCode());
    }

    @Test()
    public void cancelSearch() {
        this.getClient().cancelSearch(SEARCH_ID_TO_CANCEL);
    }

    @Test()
    public void cancelSearchWithResponse() {
        Response<Void> response = this.getClient().cancelSearchWithResponse(SEARCH_ID_TO_CANCEL, Context.NONE);

        assertEquals(202, response.getStatusCode());
    }

    @Test()
    public void configureNumber() {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_CONFIGURE);
        PstnConfiguration pstnConfiguration = new PstnConfiguration();
        pstnConfiguration.setApplicationId("ApplicationId");
        pstnConfiguration.setCallbackUrl("https://callbackurl");

        this.getClient().configureNumber(number, pstnConfiguration);
    }

    @Test()
    public void configureNumberWithResponse() {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_CONFIGURE);
        PstnConfiguration pstnConfiguration = new PstnConfiguration();
        pstnConfiguration.setApplicationId("ApplicationId");
        pstnConfiguration.setCallbackUrl("https://callbackurl");

        Response<Void> response = this.getClient().configureNumberWithResponse(number, pstnConfiguration, Context.NONE);

        assertEquals(200, response.getStatusCode());
    }

    @Test()
    public void getNumberConfiguration() {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_GET_CONFIG);

        NumberConfigurationResponse numberConfig = this.getClient().getNumberConfiguration(number);

        assertEquals("ApplicationId", numberConfig.getPstnConfiguration().getApplicationId());
    }

    @Test()
    public void getNumberConfigurationWithResponse() {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_GET_CONFIG);

        Response<NumberConfigurationResponse> response =
            this.getClient().getNumberConfigurationWithResponse(number, Context.NONE);

        assertEquals(200, response.getStatusCode());
        assertEquals("ApplicationId", response.getValue().getPstnConfiguration().getApplicationId());
    }

    @Test()
    public void unconfigureNumber() {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_UNCONFIGURE);
        this.getClient().unconfigureNumber(number);
    }

    @Test()
    public void unconfigureNumberWithResponse() {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_UNCONFIGURE);

        Response<Void> response = this.getClient().unconfigureNumberWithResponse(number, Context.NONE);

        assertEquals(200, response.getStatusCode());
    }

    @Test()
    public void releasePhoneNumbers() {
        List<PhoneNumber> phoneNumbers = new ArrayList<>();
        phoneNumbers.add(new PhoneNumber(PHONENUMBER_TO_RELEASE));

        ReleaseResponse releaseResponse = this.getClient().releasePhoneNumbers(phoneNumbers);

        assertNotNull(releaseResponse.getReleaseId());
    }

    @Test()
    public void releasePhoneNumbersWithResponse() {
        List<PhoneNumber> phoneNumbers = new ArrayList<>();
        phoneNumbers.add(new PhoneNumber(PHONENUMBER_TO_RELEASE));

        Response<ReleaseResponse> response =
            this.getClient().releasePhoneNumbersWithResponse(phoneNumbers, Context.NONE);

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getValue().getReleaseId());
    }

    private PhoneNumberClient getClient() {
        return super.getClientBuilder().buildClient();
    }
}
