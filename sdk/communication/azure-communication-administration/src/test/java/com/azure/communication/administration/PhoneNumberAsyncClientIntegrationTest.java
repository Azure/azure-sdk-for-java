// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.administration;

import com.azure.communication.administration.models.AcquiredPhoneNumber;
import com.azure.communication.administration.models.AreaCodes;
import com.azure.communication.administration.models.Capability;
import com.azure.communication.administration.models.CreateReservationOptions;
import com.azure.communication.administration.models.LocationOptionsQuery;
import com.azure.communication.administration.models.LocationOptionsResponse;
import com.azure.communication.administration.models.LocationType;
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

public class PhoneNumberAsyncClientIntegrationTest extends PhoneNumberIntegrationTestBase {

    private PhoneNumberAsyncClient getClientWithConnectionString(HttpClient httpClient, String testName) {
        PhoneNumberClientBuilder builder = super.getClientBuilderWithConnectionString(httpClient);
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createAsyncPhoneNumberClientWithConnectionString(HttpClient httpClient) {
        PhoneNumberAsyncClient phoneNumberAsyncClient = this.getClientWithConnectionString(httpClient, "createAsyncClient");
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
    public void createAsyncPhoneNumberClientWithManagedIdentity(HttpClient httpClient) {
        PhoneNumberAsyncClient phoneNumberAsyncClient = getClientBuilderUsingManagedIdentity(httpClient).buildAsyncClient();
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
        PagedFlux<AcquiredPhoneNumber> pagedFlux = this.getClientWithConnectionString(httpClient, "listAllPhoneNumbers").listAllPhoneNumbers(LOCALE);

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
            this.getClientWithConnectionString(httpClient, "listPhonePlanGroups").listPhonePlanGroups(COUNTRY_CODE, LOCALE, true);

        StepVerifier.create(pagedFlux.next())
            .assertNext(item -> {
                assertNotNull(item.getPhonePlanGroupId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listPhonePlans(HttpClient httpClient) {
        StepVerifier.create(
            this.getClientWithConnectionString(httpClient, "listPhonePlans_listPlanGroups").listPhonePlanGroups(COUNTRY_CODE, LOCALE, true).next()
                .flatMap((PhonePlanGroup phonePlanGroup) -> {
                    return this.getClientWithConnectionString(httpClient, "listPhonePlans").listPhonePlans(COUNTRY_CODE, phonePlanGroup.getPhonePlanGroupId(), LOCALE).next();
                }))
            .assertNext((PhonePlan phonePlan) -> {
                assertNotNull(phonePlan.getPhonePlanId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listAllReleases(HttpClient httpClient) {
        PagedFlux<PhoneNumberEntity> pagedFlux = this.getClientWithConnectionString(httpClient, "listAllReleases").listAllReleases();

        StepVerifier.create(pagedFlux.next())
            .assertNext(item -> {
                assertNotNull(item.getId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listAllReservations(HttpClient httpClient) {
        PagedFlux<PhoneNumberEntity> pagedFlux = this.getClientWithConnectionString(httpClient, "listAllReservations").listAllReservations();

        StepVerifier.create(pagedFlux.next())
            .assertNext(item -> {
                assertNotNull(item.getId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listAllSupportedCountries(HttpClient httpClient) {
        PagedFlux<PhoneNumberCountry> pagedFlux = this.getClientWithConnectionString(httpClient, "listAllSupportedCountries").listAllSupportedCountries(LOCALE);

        StepVerifier.create(pagedFlux.next())
            .assertNext(item -> {
                assertNotNull(item.getCountryCode());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPhonePlanLocationOptions(HttpClient httpClient) {
        StepVerifier.create(
            this.getClientWithConnectionString(httpClient, "getPhonePlanLocationOptions_listPlanGroups").listPhonePlanGroups(COUNTRY_CODE, LOCALE, true).next()
                .flatMap((PhonePlanGroup phonePlanGroup) -> {
                    return this.getClientWithConnectionString(httpClient, "getPhonePlanLocationOptions_listPlans").listPhonePlans(COUNTRY_CODE, phonePlanGroup.getPhonePlanGroupId(), LOCALE).next()
                    .flatMap((PhonePlan phonePlan) -> {
                        return this.getClientWithConnectionString(httpClient, "getPhonePlanLocationOptions").getPhonePlanLocationOptions(COUNTRY_CODE, phonePlanGroup.getPhonePlanGroupId(), phonePlan.getPhonePlanId(), LOCALE);
                    });
                }))
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

        StepVerifier.create(
            this.getClientWithConnectionString(httpClient, "getAllAreaCodes_listPlanGroups").listPhonePlanGroups(COUNTRY_CODE, LOCALE, true).next()
                .flatMap(phonePlanGroups -> {
                    return this.getClientWithConnectionString(httpClient, "getAllAreaCodes_listPlans").listPhonePlans(COUNTRY_CODE, phonePlanGroups.getPhonePlanGroupId(), LOCALE).next()
                    .flatMap(phonePlans -> {
                        return this.getClientWithConnectionString(httpClient, "getAllAreaCodes").getAllAreaCodes(LocationType.SELECTION.toString(), COUNTRY_CODE, phonePlans.getPhonePlanId(), locationOptions);
                    });
                }))
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

        StepVerifier.create(
            this.getClientWithConnectionString(httpClient, "getAllAreaCodesWithResponse_listPlanGroups")
            .listPhonePlanGroups(COUNTRY_CODE, LOCALE, true).next()
                .flatMap(phonePlanGroups -> {
                    return this.getClientWithConnectionString(httpClient, "getAllAreaCodesWithResponse_listPlans").listPhonePlans(COUNTRY_CODE, phonePlanGroups.getPhonePlanGroupId(), LOCALE).next()
                    .flatMap(phonePlans -> {
                        return this.getClientWithConnectionString(httpClient, "getAllAreaCodesWithResponse").getAllAreaCodesWithResponse(LocationType.SELECTION.toString(), COUNTRY_CODE, phonePlans.getPhonePlanId(), locationOptions, Context.NONE);
                    });
                }))
            .assertNext(item -> {
                assertEquals(200, item.getStatusCode());
                assertTrue(item.getValue().getPrimaryAreaCodes().size() > 0);
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void beginCreateReservationGetReservationByIdCancelReservation(HttpClient httpClient) {
        StepVerifier.create(
            // Setting up for phone number reservation creation
            this.getClientWithConnectionString(httpClient, "reservationTests_listPlanGroups").listPhonePlanGroups(COUNTRY_CODE, LOCALE, true).next()
                .flatMap((PhonePlanGroup phonePlanGroup) -> {
                    return this.getClientWithConnectionString(httpClient, "reservationTests_listPlans").listPhonePlans(COUNTRY_CODE, phonePlanGroup.getPhonePlanGroupId(), LOCALE).next()
                    .flatMap((PhonePlan phonePlan) -> {
                        // Create Reservation
                        return beginCreateReservation(httpClient, phonePlan, "reservationTests_beginCreateReservation").last()
                        .flatMap((AsyncPollResponse<PhoneNumberReservation, PhoneNumberReservation> createdRes) -> {
                            assertEquals(createdRes.getValue().getPhoneNumbers().size(), 1);
                            assertNotNull(createdRes.getValue().getReservationId());
                            // Get Reservation by id
                            return this.getClientWithConnectionString(httpClient, "reservationTests_getReservationById").getReservationById(createdRes.getValue().getReservationId()).
                            flatMap(reservation -> {
                                assertEquals(createdRes.getValue().getReservationId(), reservation.getReservationId());
                                // Cancel Reservation
                                return this.getClientWithConnectionString(httpClient, "reservationTests_cancelReservation").cancelReservation(reservation.getReservationId());
                            });
                        });
                    });
                }))
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void beginCreateReservationGetReservationByIdCancelReservationWithResponse(HttpClient httpClient) {
        StepVerifier.create(
            // Setting up for phone number reservation creation
            this.getClientWithConnectionString(httpClient, "reservationWithResponseTests_listPlanGroups").listPhonePlanGroups(COUNTRY_CODE, LOCALE, true).next()
                .flatMap((PhonePlanGroup phonePlanGroup) -> {
                    return this.getClientWithConnectionString(httpClient, "reservationWithResponseTests_listPlans").listPhonePlans(COUNTRY_CODE, phonePlanGroup.getPhonePlanGroupId(), LOCALE).next()
                    .flatMap((PhonePlan phonePlan) -> {
                        // Create Reservation
                        return beginCreateReservation(httpClient, phonePlan, "reservationWithResponseTests_beginCreateReservation").last()
                        .flatMap((AsyncPollResponse<PhoneNumberReservation, PhoneNumberReservation> createdRes) -> {
                            assertEquals(createdRes.getValue().getPhoneNumbers().size(), 1);
                            assertNotNull(createdRes.getValue().getReservationId());
                            // Get Reservation by id with response
                            return this.getClientWithConnectionString(httpClient, "reservationWithResponseTests_getResponseById").getReservationByIdWithResponse(createdRes.getValue().getReservationId())
                            .flatMap((Response<PhoneNumberReservation> reservationResponse) -> {
                                assertEquals(200, reservationResponse.getStatusCode());
                                assertEquals(createdRes.getValue().getReservationId(), reservationResponse.getValue().getReservationId());
                                // Cancel Reservation with response
                                return this.getClientWithConnectionString(httpClient, "reservationWithResponseTests_cancelReservation").cancelReservationWithResponse(reservationResponse.getValue().getReservationId());
                            });
                        });
                    });
                }))
                .assertNext(cancelReservationResponse -> {
                    assertEquals(202, cancelReservationResponse.getStatusCode());
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)")
    public void purchaseReservationBeginReleasePhoneNumber(HttpClient httpClient) {
        StepVerifier.create(
            // Setting up for phone number reservation creation
            this.getClientWithConnectionString(httpClient, "purchaseReleaseNumberTests_listPlanGroups").listPhonePlanGroups(COUNTRY_CODE, LOCALE, true).next()
                .flatMap((PhonePlanGroup phonePlanGroup) -> {
                    return this.getClientWithConnectionString(httpClient, "purchaseReleaseNumberTests_listPlans").listPhonePlans(COUNTRY_CODE, phonePlanGroup.getPhonePlanGroupId(), LOCALE).next()
                    .flatMap((PhonePlan phonePlan) -> {
                        // Create Reservation
                        return beginCreateReservation(httpClient, phonePlan, "purchaseReleaseNumberTests_beginCreateReservation").last()
                        .flatMap((AsyncPollResponse<PhoneNumberReservation, PhoneNumberReservation> createdRes) -> {
                            assertEquals(createdRes.getValue().getPhoneNumbers().size(), 1);
                            String purchasedNumber = createdRes.getValue().getPhoneNumbers().get(0);
                            // Purchase Reservation
                            return beginPurchaseReservation(httpClient, createdRes.getValue().getReservationId(), "purchaseReleaseNumberTests_beginPurchaseReservation").last()
                            .flatMap((AsyncPollResponse<Void, Void> response) -> {
                                assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED.toString(), response.getStatus().toString());
                                return beginReleasePhoneNumbers(httpClient, purchasedNumber, "purchaseReleaseNumberTests_beginReleasePhoneNumbers").last();
                            });
                        });
                    });
                }))
                .assertNext((AsyncPollResponse<PhoneNumberRelease, PhoneNumberRelease> releaseNumberResponse) -> {
                    assertEquals(ReleaseStatus.COMPLETE, releaseNumberResponse.getValue().getStatus());
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void configureNumberGetNumberConfigurationUnconfigureNumberWithResponse(HttpClient httpClient) {          
        // Configuring purchased number
        PhoneNumberIdentifier number = new PhoneNumberIdentifier(PHONE_NUMBER);
        PstnConfiguration pstnConfiguration = new PstnConfiguration();
        pstnConfiguration.setApplicationId("ApplicationId");
        pstnConfiguration.setCallbackUrl("https://callbackurl");
        StepVerifier.create(
            this.getClientWithConnectionString(httpClient, "configureNumberWithResponseTests_configureNumber").configureNumberWithResponse(number, pstnConfiguration)
                .flatMap((Response<Void> configResponse) -> {
                    assertEquals(200, configResponse.getStatusCode());
                    // Get configurations of purchased number
                    return this.getClientWithConnectionString(httpClient, "configureNumberWithResponseTests_getNumberConfig").getNumberConfigurationWithResponse(number)
                    .flatMap((Response<NumberConfigurationResponse> getConfigResponse) -> {
                        assertEquals(200, getConfigResponse.getStatusCode());
                        assertNotNull(getConfigResponse.getValue().getPstnConfiguration().getApplicationId());
                        assertNotNull(getConfigResponse.getValue().getPstnConfiguration().getCallbackUrl());
                        // Unconfigure the purchased number
                        return this.getClientWithConnectionString(httpClient, "configureNumberWithResponseTests_unconfigureNumber").unconfigureNumberWithResponse(number);
                    });
                }))
                .assertNext((Response<Void> unconfigureResponse) -> {
                    assertEquals(200, unconfigureResponse.getStatusCode());
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void configureNumberGetNumberConfigurationUnconfigureNumber(HttpClient httpClient) {          
        // Configuring purchased number
        PhoneNumberIdentifier number = new PhoneNumberIdentifier(PHONE_NUMBER);
        PstnConfiguration pstnConfiguration = new PstnConfiguration();
        pstnConfiguration.setApplicationId("ApplicationId");
        pstnConfiguration.setCallbackUrl("https://callbackurl");
        StepVerifier.create(
            this.getClientWithConnectionString(httpClient, "configureNumberTests_configureNumber").configureNumber(number, pstnConfiguration)
                .flatMap(response -> {
                    // Get configurations of purchased number
                    return this.getClientWithConnectionString(httpClient, "configureNumberTests_getNumberConfig").getNumberConfiguration(number)
                    .flatMap((NumberConfigurationResponse configResponse) -> {
                        assertNotNull(configResponse.getPstnConfiguration().getApplicationId());
                        assertNotNull(configResponse.getPstnConfiguration().getCallbackUrl());
                        // Unconfigure the purchased number
                        return this.getClientWithConnectionString(httpClient, "configureNumberTests_unconfigureNumber").unconfigureNumber(number);
                    });
                }))
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateCapabilitiesGetCapabilitiesUpdateWithResponse(HttpClient httpClient) {
        List<Capability> capabilitiesToAdd = new ArrayList<>();
        capabilitiesToAdd.add(Capability.INBOUND_CALLING);
        NumberUpdateCapabilities update = new NumberUpdateCapabilities();
        update.setAdd(capabilitiesToAdd);
        Map<PhoneNumberIdentifier, NumberUpdateCapabilities> updateMap = new HashMap<>();
        updateMap.put(new PhoneNumberIdentifier(PHONE_NUMBER), update);

        StepVerifier.create(
            this.getClientWithConnectionString(httpClient, "capabilitiesWithResponseTests_updateCapabilities").updateCapabilitiesWithResponse(updateMap)
                .flatMap((Response<UpdateNumberCapabilitiesResponse> updateResponse) -> {
                    assertEquals(200, updateResponse.getStatusCode());
                    // Get capabilities update
                    String capabilitiesUpdateId = updateResponse.getValue().getCapabilitiesUpdateId();
                    assertNotNull(capabilitiesUpdateId);
                    return this.getClientWithConnectionString(httpClient, "capabilitiesWithResponseTests_getCapabilitiesUpdate").getCapabilitiesUpdateWithResponse(capabilitiesUpdateId);
                }))
                .assertNext((Response<UpdatePhoneNumberCapabilitiesResponse> retrievedUpdateResponse) -> {
                    assertEquals(200, retrievedUpdateResponse.getStatusCode());
                    assertNotNull(retrievedUpdateResponse.getValue().getCapabilitiesUpdateId());
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateCapabilitiesGetCapabilitiesUpdate(HttpClient httpClient) {
        List<Capability> capabilitiesToAdd = new ArrayList<>();
        capabilitiesToAdd.add(Capability.INBOUND_CALLING);
        NumberUpdateCapabilities update = new NumberUpdateCapabilities();
        update.setAdd(capabilitiesToAdd);
        Map<PhoneNumberIdentifier, NumberUpdateCapabilities> updateMap = new HashMap<>();
        updateMap.put(new PhoneNumberIdentifier(PHONE_NUMBER), update);

        StepVerifier.create(
            this.getClientWithConnectionString(httpClient, "capabilitiesTests_updateCapabilities").updateCapabilities(updateMap)
                .flatMap((UpdateNumberCapabilitiesResponse updateResponse) -> {
                    // Get capabilities update
                    String capabilitiesUpdateId = updateResponse.getCapabilitiesUpdateId();
                    assertNotNull(capabilitiesUpdateId);
                    return this.getClientWithConnectionString(httpClient, "capabilitiesTests_getCapabilitiesUpdate").getCapabilitiesUpdate(capabilitiesUpdateId);
                }))
                .assertNext((UpdatePhoneNumberCapabilitiesResponse retrievedUpdateResponse) -> {
                    assertNotNull(retrievedUpdateResponse.getCapabilitiesUpdateId());
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listPhonePlansNullCountryCode(HttpClient httpClient) {
        PagedFlux<PhonePlan> pagedFlux = this.getClientWithConnectionString(httpClient, "listPhonePlansNullCountryCode")
            .listPhonePlans(null, "PHONE_PLAN_GROUP_ID", LOCALE);

        StepVerifier.create(pagedFlux.next())
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listPhonePlansNullPhonePlanGroupId(HttpClient httpClient) {
        PagedFlux<PhonePlan> phonePlans =
            this.getClientWithConnectionString(httpClient, "listPhonePlansNullPhonePlanGroupId").listPhonePlans(COUNTRY_CODE, null, LOCALE);

        StepVerifier.create(phonePlans)
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPhonePlanLocationOptionsWithResponseNullCountryCode(HttpClient httpClient) {
        Mono<LocationOptionsResponse> mono =
            this.getClientWithConnectionString(httpClient, "getPhonePlanLocationOptionsWithResponseNullCountryCode")
                .getPhonePlanLocationOptions(null, "PHONE_PLAN_GROUP_ID", "PHONE_PLAN_ID", LOCALE);

        StepVerifier.create(mono)
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPhonePlanLocationOptionsWithResponseNullPhonePlanGroupId(HttpClient httpClient) {
        Mono<LocationOptionsResponse> mono =
            this.getClientWithConnectionString(httpClient, "getPhonePlanLocationOptionsWithResponseNullPhonePlanGroupId")
                .getPhonePlanLocationOptions(COUNTRY_CODE, null, "PHONE_PLAN_ID", LOCALE);

        StepVerifier.create(mono)
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPhonePlanLocationOptionsWithResponseNullPhonePlanId(HttpClient httpClient) {
        Mono<LocationOptionsResponse> mono =
            this.getClientWithConnectionString(httpClient, "getPhonePlanLocationOptionsWithResponseNullPhonePlanId")
                .getPhonePlanLocationOptions(COUNTRY_CODE, "PHONE_PLAN_GROUP_ID", null, LOCALE);

        StepVerifier.create(mono)
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getAllAreaCodesWithResponseNullLocationType(HttpClient httpClient) {
        List<LocationOptionsQuery> locationOptions = new ArrayList<>();
        Mono<Response<AreaCodes>> mono = this.getClientWithConnectionString(httpClient, "getAllAreaCodesWithResponseNullLocationType")
            .getAllAreaCodesWithResponse(null, COUNTRY_CODE, "PHONE_PLAN_ID", locationOptions, Context.NONE);

        StepVerifier.create(mono)
            .verifyError(java.lang.RuntimeException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getAllAreaCodesWithResponseNullCountryCode(HttpClient httpClient) {
        List<LocationOptionsQuery> locationOptions = new ArrayList<>();
        Mono<Response<AreaCodes>> mono = this.getClientWithConnectionString(httpClient, "getAllAreaCodesWithResponseNullCountryCode")
            .getAllAreaCodesWithResponse("selection", null, "PHONE_PLAN_ID", locationOptions, Context.NONE);

        StepVerifier.create(mono)
            .verifyError(java.lang.RuntimeException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getAllAreaCodesWithResponseNullPhonePlanId(HttpClient httpClient) {
        List<LocationOptionsQuery> locationOptions = new ArrayList<>();
        Mono<Response<AreaCodes>> mono = this.getClientWithConnectionString(httpClient, "getAllAreaCodesWithResponseNullPhonePlanId")
            .getAllAreaCodesWithResponse("selection", COUNTRY_CODE, null, locationOptions, Context.NONE);

        StepVerifier.create(mono)
            .verifyError(java.lang.RuntimeException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateCapabilitiesWithResponseNullPhoneNumberCapabilitiesUpdate(HttpClient httpClient) {
        Mono<Response<UpdateNumberCapabilitiesResponse>> mono =
            this.getClientWithConnectionString(httpClient, "updateCapabilitiesWithResponseNullPhoneNumberCapabilitiesUpdate")
                .updateCapabilitiesWithResponse(null, Context.NONE);

        StepVerifier.create(mono)
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getReservationByIdWithResponseNullSearchId(HttpClient httpClient) {
        Mono<Response<PhoneNumberReservation>> mono = this.getClientWithConnectionString(httpClient, "getReservationByIdWithResponseNullSearchId")
            .getReservationByIdWithResponse(null, Context.NONE);

        StepVerifier.create(mono)
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cancelReservationWithResponseNullReservationId(HttpClient httpClient) {
        Mono<Response<Void>> mono = this.getClientWithConnectionString(httpClient, "cancelReservationWithResponseNullReservationId")
            .cancelReservationWithResponse(null, Context.NONE);

        StepVerifier.create(mono)
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void configureNumberWithResponseNullPhoneNumber(HttpClient httpClient) {
        PstnConfiguration pstnConfiguration = new PstnConfiguration();
        pstnConfiguration.setApplicationId("ApplicationId");
        pstnConfiguration.setCallbackUrl("https://callbackurl");

        Mono<Response<Void>> mono = this.getClientWithConnectionString(httpClient, "configureNumberWithResponseNullPhoneNumber")
            .configureNumberWithResponse(null, pstnConfiguration, Context.NONE);

        StepVerifier.create(mono)
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void configureNumberWithResponseNullPstnConfig(HttpClient httpClient) {
        PhoneNumberIdentifier number = new PhoneNumberIdentifier("PHONENUMBER_TO_CONFIGURE");
        Mono<Response<Void>> mono = this.getClientWithConnectionString(httpClient, "configureNumberWithResponseNullPstnConfig")
            .configureNumberWithResponse(number, null, Context.NONE);

        StepVerifier.create(mono)
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getNumberConfigurationWithResponseNullPhoneNumber(HttpClient httpClient) {
        Mono<Response<NumberConfigurationResponse>> mono =
            this.getClientWithConnectionString(httpClient, "getNumberConfigurationWithResponseNullPhoneNumber")
                .getNumberConfigurationWithResponse(null, Context.NONE);

        StepVerifier.create(mono)
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getCapabilitiesUpdateWithResponseNullCapabilitiesId(HttpClient httpClient) {
        Mono<Response<UpdatePhoneNumberCapabilitiesResponse>> mono = this.getClientWithConnectionString(httpClient, "getCapabilitiesUpdateWithResponseNullCapabilitiesId")
            .getCapabilitiesUpdateWithResponse(null, Context.NONE);

        StepVerifier.create(mono)
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void unconfigureNumberWithResponseNullPhoneNumber(HttpClient httpClient) {
        Mono<Response<Void>> mono = this.getClientWithConnectionString(httpClient, "unconfigureNumberWithResponseNullPhoneNumber")
            .unconfigureNumberWithResponse(null, Context.NONE);

        StepVerifier.create(mono)
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listPhonePlanGroupsNullCountryCode(HttpClient httpClient) {
        PagedFlux<PhonePlanGroup> phonePlanGroups = this.getClientWithConnectionString(httpClient, "listPhonePlanGroupsNullCountryCode")
            .listPhonePlanGroups(null, LOCALE, true, Context.NONE);

        StepVerifier.create(phonePlanGroups)
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getReleaseByIdWithResponseNullReleaseId(HttpClient httpClient) {
        Mono<Response<PhoneNumberRelease>> mono = this.getClientWithConnectionString(httpClient, "getReleaseByIdWithResponseNullReleaseId")
            .getReleaseByIdWithResponse(null, Context.NONE);

        StepVerifier.create(mono)
            .verifyError(NullPointerException.class);
    }


    private PollerFlux<PhoneNumberRelease, PhoneNumberRelease> beginReleasePhoneNumbers(HttpClient httpClient, String phoneNumber, String testName) {
        PhoneNumberIdentifier releasedPhoneNumber = new PhoneNumberIdentifier(phoneNumber);
        List<PhoneNumberIdentifier> phoneNumbers = new ArrayList<>();
        phoneNumbers.add(releasedPhoneNumber);
        Duration pollInterval = Duration.ofSeconds(1);
        return this.getClientWithConnectionString(httpClient, testName).beginReleasePhoneNumbers(phoneNumbers, pollInterval);
    }

    private PollerFlux<PhoneNumberReservation, PhoneNumberReservation> beginCreateReservation(HttpClient httpClient, PhonePlan phonePlan, String testName) {
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

    private  PollerFlux<Void, Void> beginPurchaseReservation(HttpClient httpClient, String reservationId, String testName) {
        Duration pollInterval = Duration.ofSeconds(1);
        return this.getClientWithConnectionString(httpClient, testName)
            .beginPurchaseReservation(reservationId, pollInterval);
    }
}
