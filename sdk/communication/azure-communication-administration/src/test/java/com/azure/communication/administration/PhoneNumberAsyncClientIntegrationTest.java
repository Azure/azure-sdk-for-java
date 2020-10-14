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
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
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
public class PhoneNumberAsyncClientIntegrationTest extends PhoneNumberIntegrationTestBase {
    @Test()
    public void createAsyncPhoneNumberClientWithConnectionString() {
        PhoneNumberAsyncClient phoneNumberAsyncClient = getClientBuilderWithConnectionString().buildAsyncClient();
        assertNotNull(phoneNumberAsyncClient);

        // Smoke test using phoneNumberAsyncClient to list all phone numbers
        PagedFlux<AcquiredPhoneNumber> pagedFlux = phoneNumberAsyncClient.listAllPhoneNumbers(LOCALE);
        StepVerifier.create(pagedFlux.next())
            .assertNext(item -> {
                assertNotNull(item.getPhoneNumber());
            })
            .verifyComplete();
    }
    
    @Test()
    public void listAllPhoneNumbers() {
        PagedFlux<AcquiredPhoneNumber> pagedFlux = this.getClient().listAllPhoneNumbers(LOCALE);

        StepVerifier.create(pagedFlux.next())
            .assertNext(item -> {
                assertNotNull(item.getPhoneNumber());
            })
            .verifyComplete();
    }

    @Test()
    public void listPhonePlanGroups() {
        PagedFlux<PhonePlanGroup> pagedFlux =
            this.getClient().listPhonePlanGroups(COUNTRY_CODE, LOCALE, true);

        StepVerifier.create(pagedFlux.next())
            .assertNext(item -> {
                assertNotNull(item.getPhonePlanGroupId());
            })
            .verifyComplete();
    }

    @Test()
    public void listPhonePlans() {
        PagedFlux<PhonePlan> pagedFlux =
            this.getClient().listPhonePlans(COUNTRY_CODE, PHONE_PLAN_GROUP_ID, LOCALE);

        StepVerifier.create(pagedFlux.next())
            .assertNext(item -> {
                assertNotNull(item.getPhonePlanId());
            })
            .verifyComplete();
    }

    @Test()
    public void listAllReleases() {
        PagedFlux<PhoneNumberEntity> pagedFlux = this.getClient().listAllReleases();

        StepVerifier.create(pagedFlux.next())
            .assertNext(item -> {
                assertNotNull(item.getId());
            })
            .verifyComplete();
    }

    @Test()
    public void listAllSearches() {
        PagedFlux<PhoneNumberEntity> pagedFlux = this.getClient().listAllSearches();

        StepVerifier.create(pagedFlux.next())
            .assertNext(item -> {
                assertNotNull(item.getId());
            })
            .verifyComplete();
    }

    @Test()
    public void listAllSupportedCountries() {
        PagedFlux<PhoneNumberCountry> pagedFlux = this.getClient().listAllSupportedCountries(LOCALE);

        StepVerifier.create(pagedFlux.next())
            .assertNext(item -> {
                assertNotNull(item.getCountryCode());
            })
            .verifyComplete();
    }

    @Test()
    public void getPhonePlanLocationOptions() {
        Mono<LocationOptionsResponse> mono =
            this.getClient().getPhonePlanLocationOptions(COUNTRY_CODE, PHONE_PLAN_GROUP_ID, PHONE_PLAN_ID, LOCALE);

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertNotNull(item.getLocationOptions().getLabelId());
            })
            .verifyComplete();
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

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertTrue(item.getPrimaryAreaCodes().size() > 0);
            })
            .verifyComplete();
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

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertEquals(200, item.getStatusCode());
                assertTrue(item.getValue().getPrimaryAreaCodes().size() > 0);
            })
            .verifyComplete();
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

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertNotNull(item.getCapabilitiesUpdateId());
            })
            .verifyComplete();
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

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertEquals(200, item.getStatusCode());
                assertNotNull(item.getValue().getCapabilitiesUpdateId());
            })
            .verifyComplete();
    }

    @Test()
    public void getCapabilitiesUpdate() {
        Mono<UpdatePhoneNumberCapabilitiesResponse> mono =
            this.getClient().getCapabilitiesUpdate(CAPABILITIES_ID);

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertNotNull(item.getCapabilitiesUpdateId());
            })
            .verifyComplete();
    }

    @Test()
    public void getCapabilitiesUpdateWithResponse() {
        Mono<Response<UpdatePhoneNumberCapabilitiesResponse>> mono =
            this.getClient().getCapabilitiesUpdateWithResponse(CAPABILITIES_ID, Context.NONE);

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertEquals(200, item.getStatusCode());
                assertNotNull(item.getValue().getCapabilitiesUpdateId());
            })
            .verifyComplete();
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

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertNotNull(item.getSearchId());
            })
            .verifyComplete();
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

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertEquals(201, item.getStatusCode());
                assertNotNull(item.getValue().getSearchId());
            })
            .verifyComplete();
    }

    @Test()
    public void getSearchById() {
        Mono<PhoneNumberSearch> mono = this.getClient().getSearchById(SEARCH_ID);

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertEquals(SEARCH_ID, item.getSearchId());
            })
            .verifyComplete();
    }

    @Test()
    public void getSearchByIdWithResponse() {
        Mono<Response<PhoneNumberSearch>> mono = this.getClient().getSearchByIdWithResponse(SEARCH_ID, Context.NONE);

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertEquals(200, item.getStatusCode());
                assertEquals(SEARCH_ID, item.getValue().getSearchId());
            })
            .verifyComplete();
    }

    @Test()
    public void purchaseSearch() {
        Mono<Void> mono = this.getClient().purchaseSearch(SEARCH_ID_TO_PURCHASE);

        StepVerifier.create(mono).verifyComplete();
    }

    @Test()
    public void purchaseSearchWithResponse() {
        Mono<Response<Void>> mono = this.getClient().purchaseSearchWithResponse(SEARCH_ID_TO_PURCHASE, Context.NONE);

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertEquals(202, item.getStatusCode());
            })
            .verifyComplete();
    }

    @Test()
    public void cancelSearch() {
        Mono<Void> mono = this.getClient().cancelSearch(SEARCH_ID_TO_CANCEL);

        StepVerifier.create(mono).verifyComplete();
    }

    @Test()
    public void cancelSearchWithResponse() {
        Mono<Response<Void>> mono = this.getClient().cancelSearchWithResponse(SEARCH_ID_TO_CANCEL, Context.NONE);

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertEquals(202, item.getStatusCode());
            })
            .verifyComplete();
    }

    @Test()
    public void configureNumber() {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_CONFIGURE);
        PstnConfiguration pstnConfiguration = new PstnConfiguration();
        pstnConfiguration.setApplicationId("ApplicationId");
        pstnConfiguration.setCallbackUrl("https://callbackurl");

        Mono<Void> mono = this.getClient().configureNumber(number, pstnConfiguration);

        StepVerifier.create(mono).verifyComplete();
    }

    @Test()
    public void configureNumberWithResponse() {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_CONFIGURE);
        PstnConfiguration pstnConfiguration = new PstnConfiguration();
        pstnConfiguration.setApplicationId("ApplicationId");
        pstnConfiguration.setCallbackUrl("https://callbackurl");

        Mono<Response<Void>> mono = this.getClient().configureNumberWithResponse(number, pstnConfiguration, Context.NONE);

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertEquals(200, item.getStatusCode());
            })
            .verifyComplete();
    }

    @Test()
    public void getNumberConfiguration() {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_GET_CONFIG);

        Mono<NumberConfigurationResponse> mono = this.getClient().getNumberConfiguration(number);

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertEquals("ApplicationId", item.getPstnConfiguration().getApplicationId());
            })
            .verifyComplete();
    }

    @Test()
    public void getNumberConfigurationWithResponse() {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_GET_CONFIG);

        Mono<Response<NumberConfigurationResponse>> mono =
            this.getClient().getNumberConfigurationWithResponse(number, Context.NONE);

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertEquals(200, item.getStatusCode());
                assertEquals("ApplicationId", item.getValue().getPstnConfiguration().getApplicationId());
            })
            .verifyComplete();
    }

    @Test()
    public void unconfigureNumber() {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_UNCONFIGURE);

        Mono<Void> mono = this.getClient().unconfigureNumber(number);

        StepVerifier.create(mono).verifyComplete();
    }

    @Test()
    public void unconfigureNumberWithResponse() {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_UNCONFIGURE);

        Mono<Response<Void>> mono = this.getClient().unconfigureNumberWithResponse(number, Context.NONE);

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertEquals(200, item.getStatusCode());
            })
            .verifyComplete();
    }

    @Test()
    public void releasePhoneNumbers() {
        List<PhoneNumber> phoneNumbers = new ArrayList<>();
        phoneNumbers.add(new PhoneNumber(PHONENUMBER_TO_RELEASE));

        Mono<ReleaseResponse> mono = this.getClient().releasePhoneNumbers(phoneNumbers);

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertNotNull(item.getReleaseId());
            })
            .verifyComplete();
    }

    @Test()
    public void releasePhoneNumbersWithResponse() {
        List<PhoneNumber> phoneNumbers = new ArrayList<>();
        phoneNumbers.add(new PhoneNumber(PHONENUMBER_TO_RELEASE));

        Mono<Response<ReleaseResponse>> mono =
            this.getClient().releasePhoneNumbersWithResponse(phoneNumbers, Context.NONE);

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertEquals(200, item.getStatusCode());
                assertNotNull(item.getValue().getReleaseId());
            })
            .verifyComplete();
    }

    @Test()
    public void beginCreateSearch() {
        List<String> phonePlanIds = new ArrayList<>();
        phonePlanIds.add(PHONE_PLAN_ID);

        CreateSearchOptions createSearchOptions = new CreateSearchOptions();
        createSearchOptions
            .setAreaCode(AREA_CODE_FOR_SEARCH)
            .setDescription(SEARCH_OPTIONS_DESCRIPTION)
            .setDisplayName(SEARCH_OPTIONS_NAME)
            .setPhonePlanIds(phonePlanIds)
            .setQuantity(2);

        Duration duration = Duration.ofSeconds(1);
        PhoneNumberAsyncClient client = this.getClient();
        PollerFlux<PhoneNumberSearch, PhoneNumberSearch> poller = 
            client.beginCreateSearch(createSearchOptions, duration);
        AsyncPollResponse<PhoneNumberSearch, PhoneNumberSearch> asyncRes = 
            poller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .blockLast();
        PhoneNumberSearch testResult = asyncRes.getValue();
        assertEquals(testResult.getPhoneNumbers().size(), 2);
        assertNotNull(testResult.getSearchId());
    }

    private PhoneNumberAsyncClient getClient() {
        return super.getClientBuilder().buildAsyncClient();
    }
}
