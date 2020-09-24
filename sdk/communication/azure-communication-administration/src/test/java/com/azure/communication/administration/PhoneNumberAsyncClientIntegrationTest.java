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
import com.azure.communication.administration.models.PhoneNumberSearch;
import com.azure.communication.administration.models.PhonePlan;
import com.azure.communication.administration.models.PhonePlanGroup;
import com.azure.communication.administration.models.PstnConfiguration;
import com.azure.communication.administration.models.ReleaseResponse;
import com.azure.communication.administration.models.UpdateNumberCapabilitiesResponse;
import com.azure.communication.administration.models.UpdatePhoneNumberCapabilitiesResponse;
import com.azure.communication.common.PhoneNumber;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PhoneNumberAsyncClientIntegrationTest extends PhoneNumberIntegrationTestBase {
    @Test()
    public void listAllPhoneNumbers() {
        PagedFlux<AcquiredPhoneNumber> pagedFlux = this.getClient().listAllPhoneNumbers(LOCALE);

        assertNotNull(pagedFlux.blockFirst().getPhoneNumber());
    }

    @Test()
    public void listPhonePlanGroups() {
        PagedFlux<PhonePlanGroup> pagedFlux =
            this.getClient().listPhonePlanGroups(COUNTRY_CODE, LOCALE, true);

        assertNotNull(pagedFlux.blockFirst().getPhonePlanGroupId());
    }

    @Test()
    public void listPhonePlans() {
        PagedFlux<PhonePlan> pagedFlux =
            this.getClient().listPhonePlans(COUNTRY_CODE, PHONE_PLAN_GROUP_ID, LOCALE);

        assertNotNull(pagedFlux.blockFirst().getPhonePlanId());
    }

    @Test()
    public void listAllReleases() {
        PagedFlux<PhoneNumberEntity> pagedFlux = this.getClient().listAllReleases();

        assertNotNull(pagedFlux.blockFirst().getId());
    }

    @Test()
    public void listAllSearches() {
        PagedFlux<PhoneNumberEntity> pagedFlux = this.getClient().listAllSearches();

        assertNotNull(pagedFlux.blockFirst().getId());
    }

    @Test()
    public void listAllSupportedCountries() {
        PagedFlux<PhoneNumberCountry> pagedFlux = this.getClient().listAllSupportedCountries(LOCALE);

        assertNotNull(pagedFlux.blockFirst().getCountryCode());
    }

    @Test()
    public void getPhonePlanLocationOptions() {
        Mono<LocationOptionsResponse> mono =
            this.getClient().getPhonePlanLocationOptions(COUNTRY_CODE, PHONE_PLAN_GROUP_ID, PHONE_PLAN_ID, LOCALE);

        assertNotNull(mono.block().getLocationOptions().getLabelId());
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

        Mono<AreaCodes> mono =
            this.getClient().getAllAreaCodes("selection", COUNTRY_CODE, PHONE_PLAN_ID, locationOptions);

        assertTrue(mono.block().getPrimaryAreaCodes().size() > 0);
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

        Mono<Response<AreaCodes>> mono = this.getClient().getAllAreaCodesWithResponse(
            "selection", COUNTRY_CODE, PHONE_PLAN_ID, locationOptions, Context.NONE);

        Response<AreaCodes> response = mono.block();
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getValue().getPrimaryAreaCodes().size() > 0);
    }

    @Test()
    public void updateCapabilities() {
        List<Capability> capabilitiesToAdd = new ArrayList<>();
        capabilitiesToAdd.add(Capability.INBOUND_CALLING);

        NumberUpdateCapabilities update = new NumberUpdateCapabilities();
        update.setAdd(capabilitiesToAdd);

        Map<PhoneNumber, NumberUpdateCapabilities> updateMap = new HashMap<>();
        updateMap.put(new PhoneNumber(PHONENUMBER_FOR_CAPABILITIES), update);

        Mono<UpdateNumberCapabilitiesResponse> mono = this.getClient().updateCapabilities(updateMap);

        assertNotNull(mono.block().getCapabilitiesUpdateId());
    }

    @Test()
    public void updateCapabilitiesWithResponse() {
        List<Capability> capabilitiesToAdd = new ArrayList<>();
        capabilitiesToAdd.add(Capability.INBOUND_CALLING);

        NumberUpdateCapabilities update = new NumberUpdateCapabilities();
        update.setAdd(capabilitiesToAdd);

        Map<PhoneNumber, NumberUpdateCapabilities> updateMap = new HashMap<>();
        updateMap.put(new PhoneNumber(PHONENUMBER_FOR_CAPABILITIES), update);

        Mono<Response<UpdateNumberCapabilitiesResponse>> mono =
            this.getClient().updateCapabilitiesWithResponse(updateMap, Context.NONE);

        Response<UpdateNumberCapabilitiesResponse> response = mono.block();
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getValue().getCapabilitiesUpdateId());
    }

    @Test()
    public void getCapabilitiesUpdate() {
        Mono<UpdatePhoneNumberCapabilitiesResponse> mono =
            this.getClient().getCapabilitiesUpdate(CAPABILITIES_ID);
        assertNotNull(mono.block().getCapabilitiesUpdateId());
    }

    @Test()
    public void getCapabilitiesUpdateWithResponse() {
        Mono<Response<UpdatePhoneNumberCapabilitiesResponse>> mono =
            this.getClient().getCapabilitiesUpdateWithResponse(CAPABILITIES_ID, Context.NONE);

        Response<UpdatePhoneNumberCapabilitiesResponse> response = mono.block();
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
            .setDescription("testsearch20200014")
            .setDisplayName("testsearch20200014")
            .setPhonePlanIds(phonePlanIds)
            .setQuantity(1);

        Mono<CreateSearchResponse> mono = this.getClient().createSearch(createSearchOptions);

        assertNotNull(mono.block().getSearchId());
    }

    @Test()
    public void createSearchWithResponse() {
        List<String> phonePlanIds = new ArrayList<>();
        phonePlanIds.add(PHONE_PLAN_ID);

        CreateSearchOptions createSearchOptions = new CreateSearchOptions();
        createSearchOptions
            .setAreaCode(AREA_CODE_FOR_SEARCH)
            .setDescription("testsearch20200014")
            .setDisplayName("testsearch20200014")
            .setPhonePlanIds(phonePlanIds)
            .setQuantity(1);

        Mono<Response<CreateSearchResponse>> mono =
            this.getClient().createSearchWithResponse(createSearchOptions, Context.NONE);

        Response<CreateSearchResponse> response = mono.block();

        assertEquals(201, response.getStatusCode());
        assertNotNull(response.getValue().getSearchId());
    }

    @Test()
    public void getSearchById() {
        Mono<PhoneNumberSearch> mono = this.getClient().getSearchById(SEARCH_ID);

        assertEquals(SEARCH_ID, mono.block().getSearchId());
    }

    @Test()
    public void getSearchByIdWithResponse() {
        Mono<Response<PhoneNumberSearch>> mono = this.getClient().getSearchByIdWithResponse(SEARCH_ID, Context.NONE);

        Response<PhoneNumberSearch> response = mono.block();
        assertEquals(200, response.getStatusCode());
        assertEquals(SEARCH_ID, response.getValue().getSearchId());
    }

    @Test()
    public void purchaseSearch() {
        Mono<Void> mono = this.getClient().purchaseSearch(SEARCH_ID_TO_PURCHASE);
        mono.block();
    }

    @Test()
    public void purchaseSearchWithResponse() {
        Mono<Response<Void>> mono = this.getClient().purchaseSearchWithResponse(SEARCH_ID_TO_PURCHASE, Context.NONE);

        Response<Void> response = mono.block();
        assertEquals(202, response.getStatusCode());
    }

    @Test()
    public void cancelSearch() {
        Mono<Void> mono = this.getClient().cancelSearch(SEARCH_ID_TO_CANCEL);
        mono.block();
    }

    @Test()
    public void cancelSearchWithResponse() {
        Mono<Response<Void>> mono = this.getClient().cancelSearchWithResponse(SEARCH_ID_TO_CANCEL, Context.NONE);

        Response<Void> response = mono.block();
        assertEquals(202, response.getStatusCode());
    }

    @Test()
    public void configureNumber() {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_CONFIGURE);
        PstnConfiguration pstnConfiguration = new PstnConfiguration();
        pstnConfiguration.setApplicationId("ApplicationId");
        pstnConfiguration.setAzurePstnTargetId("AzurePstnTargetId");
        pstnConfiguration.setCallbackUrl("https://callbackurl");

        Mono<Void> mono = this.getClient().configureNumber(number, pstnConfiguration);

        mono.block();
    }

    @Test()
    public void configureNumberWithResponse() {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_CONFIGURE);
        PstnConfiguration pstnConfiguration = new PstnConfiguration();
        pstnConfiguration.setApplicationId("ApplicationId");
        pstnConfiguration.setAzurePstnTargetId("AzurePstnTargetId");
        pstnConfiguration.setCallbackUrl("https://callbackurl");

        Mono<Response<Void>> mono = this.getClient().configureNumberWithResponse(number, pstnConfiguration, Context.NONE);

        Response<Void> response = mono.block();
        assertEquals(202, response.getStatusCode());
    }

    @Test()
    public void getNumberConfiguration() {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_GET_CONFIG);

        Mono<NumberConfigurationResponse> mono = this.getClient().getNumberConfiguration(number);

        assertEquals("ApplicationId", mono.block().getPstnConfiguration().getApplicationId());
    }

    @Test()
    public void getNumberConfigurationWithResponse() {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_GET_CONFIG);

        Mono<Response<NumberConfigurationResponse>> mono =
            this.getClient().getNumberConfigurationWithResponse(number, Context.NONE);

        Response<NumberConfigurationResponse> response = mono.block();
        assertEquals(200, response.getStatusCode());
        assertEquals("ApplicationId", response.getValue().getPstnConfiguration().getApplicationId());
    }

    @Test()
    public void unconfigureNumber() {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_UNCONFIGURE);

        Mono<Void> mono = this.getClient().unconfigureNumber(number);

        mono.block();
    }

    @Test()
    public void unconfigureNumberWithResponse() {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_UNCONFIGURE);

        Mono<Response<Void>> mono = this.getClient().unconfigureNumberWithResponse(number, Context.NONE);

        Response<Void> response = mono.block();
        assertEquals(202, response.getStatusCode());
    }

    @Test()
    public void releasePhoneNumbers() {
        List<PhoneNumber> phoneNumbers = new ArrayList<>();
        phoneNumbers.add(new PhoneNumber(PHONENUMBER_TO_RELEASE));

        Mono<ReleaseResponse> mono = this.getClient().releasePhoneNumbers(phoneNumbers);

        assertNotNull(mono.block().getReleaseId());
    }

    @Test()
    public void releasePhoneNumbersWithResponse() {
        List<PhoneNumber> phoneNumbers = new ArrayList<>();
        phoneNumbers.add(new PhoneNumber(PHONENUMBER_TO_RELEASE));

        Mono<Response<ReleaseResponse>> mono =
            this.getClient().releasePhoneNumbersWithResponse(phoneNumbers, Context.NONE);

        Response<ReleaseResponse> response = mono.block();
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getValue().getReleaseId());
    }

    private PhoneNumberAsyncClient getClient() {
        return super.getClientBuilder().buildAsyncClient();
    }
}
