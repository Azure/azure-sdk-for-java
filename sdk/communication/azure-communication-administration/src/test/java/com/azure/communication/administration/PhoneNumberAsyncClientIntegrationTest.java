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
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.Context;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createAsyncPhoneNumberClientWithConnectionString(HttpClient httpClient) {
        PhoneNumberAsyncClient phoneNumberAsyncClient = getClientBuilderWithConnectionString(httpClient).buildAsyncClient();
        assertNotNull(phoneNumberAsyncClient);

        // Smoke test using phoneNumberAsyncClient to list all phone numbers
        PagedFlux<AcquiredPhoneNumber> pagedFlux = phoneNumberAsyncClient.listAllPhoneNumbers(LOCALE);
        StepVerifier.create(pagedFlux.next())
            .assertNext(item -> {
                assertNotNull(item.getPhoneNumber());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listAllPhoneNumbers(HttpClient httpClient) {
        PagedFlux<AcquiredPhoneNumber> pagedFlux = this.getClient(httpClient).listAllPhoneNumbers(LOCALE);

        StepVerifier.create(pagedFlux.next())
            .assertNext(item -> {
                assertNotNull(item.getPhoneNumber());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listPhonePlanGroups(HttpClient httpClient) {
        PagedFlux<PhonePlanGroup> pagedFlux =
            this.getClient(httpClient).listPhonePlanGroups(COUNTRY_CODE, LOCALE, true);

        StepVerifier.create(pagedFlux.next())
            .assertNext(item -> {
                assertNotNull(item.getPhonePlanGroupId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listPhonePlans(HttpClient httpClient) {
        PagedFlux<PhonePlan> pagedFlux =
            this.getClient(httpClient).listPhonePlans(COUNTRY_CODE, PHONE_PLAN_GROUP_ID, LOCALE);

        StepVerifier.create(pagedFlux.next())
            .assertNext(item -> {
                assertNotNull(item.getPhonePlanId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listAllReleases(HttpClient httpClient) {
        PagedFlux<PhoneNumberEntity> pagedFlux = this.getClient(httpClient).listAllReleases();

        StepVerifier.create(pagedFlux.next())
            .assertNext(item -> {
                assertNotNull(item.getId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listAllReservations(HttpClient httpClient) {
        PagedFlux<PhoneNumberEntity> pagedFlux = this.getClient(httpClient).listAllReservations();

        StepVerifier.create(pagedFlux.next())
            .assertNext(item -> {
                assertNotNull(item.getId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listAllSupportedCountries(HttpClient httpClient) {
        PagedFlux<PhoneNumberCountry> pagedFlux = this.getClient(httpClient).listAllSupportedCountries(LOCALE);

        StepVerifier.create(pagedFlux.next())
            .assertNext(item -> {
                assertNotNull(item.getCountryCode());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPhonePlanLocationOptions(HttpClient httpClient) {
        Mono<LocationOptionsResponse> mono =
            this.getClient(httpClient).getPhonePlanLocationOptions(COUNTRY_CODE, PHONE_PLAN_GROUP_ID, PHONE_PLAN_ID, LOCALE);

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertNotNull(item.getLocationOptions().getLabelId());
            })
            .verifyComplete();
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

        Mono<AreaCodes> mono =
            this.getClient(httpClient).getAllAreaCodes("selection", COUNTRY_CODE, PHONE_PLAN_ID, locationOptions);

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertTrue(item.getPrimaryAreaCodes().size() > 0);
            })
            .verifyComplete();
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

        Mono<Response<AreaCodes>> mono = this.getClient(httpClient).getAllAreaCodesWithResponse(
            "selection", COUNTRY_CODE, PHONE_PLAN_ID, locationOptions, Context.NONE);

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertEquals(200, item.getStatusCode());
                assertTrue(item.getValue().getPrimaryAreaCodes().size() > 0);
            })
            .verifyComplete();
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

        Mono<UpdateNumberCapabilitiesResponse> mono = this.getClient(httpClient).updateCapabilities(updateMap);

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertNotNull(item.getCapabilitiesUpdateId());
            })
            .verifyComplete();
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

        Mono<Response<UpdateNumberCapabilitiesResponse>> mono =
            this.getClient(httpClient).updateCapabilitiesWithResponse(updateMap, Context.NONE);

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertEquals(200, item.getStatusCode());
                assertNotNull(item.getValue().getCapabilitiesUpdateId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getCapabilitiesUpdate(HttpClient httpClient) {
        Mono<UpdatePhoneNumberCapabilitiesResponse> mono =
            this.getClient(httpClient).getCapabilitiesUpdate(CAPABILITIES_ID);

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertNotNull(item.getCapabilitiesUpdateId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getCapabilitiesUpdateWithResponse(HttpClient httpClient) {
        Mono<Response<UpdatePhoneNumberCapabilitiesResponse>> mono =
            this.getClient(httpClient).getCapabilitiesUpdateWithResponse(CAPABILITIES_ID, Context.NONE);

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertEquals(200, item.getStatusCode());
                assertNotNull(item.getValue().getCapabilitiesUpdateId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createReservationWithResponse(HttpClient httpClient) {
        List<String> phonePlanIds = new ArrayList<>();
        phonePlanIds.add(PHONE_PLAN_ID);

        CreateReservationOptions createReservationOptions = new CreateReservationOptions();
        createReservationOptions
            .setAreaCode(AREA_CODE_FOR_SEARCH)
            .setDescription("testreservation20200014")
            .setDisplayName("testreservation20200014")
            .setPhonePlanIds(phonePlanIds)
            .setQuantity(1);

        Mono<Response<CreateReservationResponse>> mono =
            this.getClient(httpClient).createReservationWithResponse(createReservationOptions, Context.NONE);

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertEquals(201, item.getStatusCode());
                assertNotNull(item.getValue().getReservationId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getReservationById(HttpClient httpClient) {
        Mono<PhoneNumberReservation> mono = this.getClient(httpClient).getReservationById(RESERVATION_ID);

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertEquals(RESERVATION_ID, item.getReservationId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getReservationByIdWithResponse(HttpClient httpClient) {
        Mono<Response<PhoneNumberReservation>> mono = this.getClient(httpClient).getReservationByIdWithResponse(RESERVATION_ID, Context.NONE);

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertEquals(200, item.getStatusCode());
                assertEquals(RESERVATION_ID, item.getValue().getReservationId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cancelReservation(HttpClient httpClient) {
        Mono<Void> mono = this.getClient(httpClient).cancelReservation(RESERVATION_ID_TO_CANCEL);

        StepVerifier.create(mono).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cancelReservationWithResponse(HttpClient httpClient) {
        Mono<Response<Void>> mono = this.getClient(httpClient).cancelReservationWithResponse(RESERVATION_ID_TO_CANCEL, Context.NONE);

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertEquals(202, item.getStatusCode());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void configureNumber(HttpClient httpClient) {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_CONFIGURE);
        PstnConfiguration pstnConfiguration = new PstnConfiguration();
        pstnConfiguration.setApplicationId("ApplicationId");
        pstnConfiguration.setCallbackUrl("https://callbackurl");

        Mono<Void> mono = this.getClient(httpClient).configureNumber(number, pstnConfiguration);

        StepVerifier.create(mono).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void configureNumberWithResponse(HttpClient httpClient) {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_CONFIGURE);
        PstnConfiguration pstnConfiguration = new PstnConfiguration();
        pstnConfiguration.setApplicationId("ApplicationId");
        pstnConfiguration.setCallbackUrl("https://callbackurl");

        Mono<Response<Void>> mono = this.getClient(httpClient).configureNumberWithResponse(number, pstnConfiguration, Context.NONE);

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertEquals(200, item.getStatusCode());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getNumberConfiguration(HttpClient httpClient) {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_GET_CONFIG);

        Mono<NumberConfigurationResponse> mono = this.getClient(httpClient).getNumberConfiguration(number);

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertEquals("ApplicationId", item.getPstnConfiguration().getApplicationId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getNumberConfigurationWithResponse(HttpClient httpClient) {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_GET_CONFIG);

        Mono<Response<NumberConfigurationResponse>> mono =
            this.getClient(httpClient).getNumberConfigurationWithResponse(number, Context.NONE);

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertEquals(200, item.getStatusCode());
                assertEquals("ApplicationId", item.getValue().getPstnConfiguration().getApplicationId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void unconfigureNumber(HttpClient httpClient) {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_UNCONFIGURE);

        Mono<Void> mono = this.getClient(httpClient).unconfigureNumber(number);

        StepVerifier.create(mono).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void unconfigureNumberWithResponse(HttpClient httpClient) {
        PhoneNumber number = new PhoneNumber(PHONENUMBER_TO_UNCONFIGURE);

        Mono<Response<Void>> mono = this.getClient(httpClient).unconfigureNumberWithResponse(number, Context.NONE);

        StepVerifier.create(mono)
            .assertNext(item -> {
                assertEquals(200, item.getStatusCode());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void beginCreateReservation(HttpClient httpClient) {
        List<String> phonePlanIds = new ArrayList<>();
        phonePlanIds.add(PHONE_PLAN_ID);

        CreateReservationOptions createReservationOptions = new CreateReservationOptions();
        createReservationOptions
            .setAreaCode(AREA_CODE_FOR_SEARCH)
            .setDescription(RESERVATION_OPTIONS_DESCRIPTION)
            .setDisplayName(RESERVATION_OPTIONS_NAME)
            .setPhonePlanIds(phonePlanIds)
            .setQuantity(2);

        Duration duration = Duration.ofSeconds(1);
        PhoneNumberAsyncClient client = this.getClient(httpClient);
        PollerFlux<PhoneNumberReservation, PhoneNumberReservation> poller =
            client.beginCreateReservation(createReservationOptions, duration);
        Mono<AsyncPollResponse<PhoneNumberReservation, PhoneNumberReservation>> asyncRes = poller.last();
        StepVerifier.create(asyncRes)
            .assertNext(item -> {
                assertEquals(item.getValue().getPhoneNumbers().size(), 2);
                assertNotNull(item.getValue().getReservationId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void beginPurchaseReservation(HttpClient httpClient) {
        Duration pollInterval = Duration.ofSeconds(1);
        PhoneNumberAsyncClient client = this.getClient(httpClient);
        PollerFlux<Void, Void> poller =
            client.beginPurchaseReservation(RESERVATION_ID, pollInterval);
        poller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .blockLast();
        Mono<PhoneNumberReservation> testResult = client.getReservationById(RESERVATION_ID);
        StepVerifier.create(testResult)
            .assertNext(item -> {
                assertEquals(ReservationStatus.SUCCESS,
                    ReservationStatus.fromString(item.getStatus().toString()));
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void beginReleasePhoneNumbers(HttpClient httpClient) {
        PhoneNumber phoneNumber = new PhoneNumber(PHONENUMBER_TO_RELEASE);
        List<PhoneNumber> phoneNumbers = new ArrayList<>();
        phoneNumbers.add(phoneNumber);
        Duration pollInterval = Duration.ofSeconds(1);
        PollerFlux<PhoneNumberRelease, PhoneNumberRelease> poller =
            this.getClient(httpClient).beginReleasePhoneNumbers(phoneNumbers, pollInterval);
        Mono<AsyncPollResponse<PhoneNumberRelease, PhoneNumberRelease>> asyncRes = poller.last();
        StepVerifier.create(asyncRes)
            .assertNext(item -> {
                assertEquals(ReleaseStatus.COMPLETE,
                    item.getValue().getStatus());
            })
            .verifyComplete();
    }

    private PhoneNumberAsyncClient getClient(HttpClient httpClient) {
        return super.getClientBuilder(httpClient).buildAsyncClient();
    }
}
