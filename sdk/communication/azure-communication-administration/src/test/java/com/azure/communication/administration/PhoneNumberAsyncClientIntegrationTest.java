// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.administration;

import com.azure.communication.administration.models.AcquiredPhoneNumber;
import com.azure.communication.administration.models.AreaCodes;
import com.azure.communication.administration.models.Capability;
import com.azure.communication.administration.models.CreateReservationOptions;
import com.azure.communication.administration.models.LocationOptionsQuery;
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
import com.azure.communication.common.PhoneNumber;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.Context;
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
        StepVerifier.create(
            this.getClient(httpClient).listPhonePlanGroups(COUNTRY_CODE, LOCALE, true).next()
                .flatMap((PhonePlanGroup phonePlanGroup) -> {
                    return this.getClient(httpClient).listPhonePlans(COUNTRY_CODE, phonePlanGroup.getPhonePlanGroupId(), LOCALE).next();
                }))
            .assertNext((PhonePlan phonePlan) -> {
                assertNotNull(phonePlan.getPhonePlanId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listPhonePlansNullCountryCode(HttpClient httpClient) {
        StepVerifier.create(
            this.getClient(httpClient).listPhonePlanGroups(COUNTRY_CODE, LOCALE, true).next()
                .flatMap((PhonePlanGroup phonePlanGroup) -> {
                    return this.getClient(httpClient).listPhonePlans(null, phonePlanGroup.getPhonePlanGroupId(), LOCALE).next();
                }))
            .verifyError(NullPointerException.class);
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
        StepVerifier.create(
            this.getClient(httpClient).listPhonePlanGroups(COUNTRY_CODE, LOCALE, true).next()
                .flatMap((PhonePlanGroup phonePlanGroup) -> {
                    return this.getClient(httpClient).listPhonePlans(COUNTRY_CODE, phonePlanGroup.getPhonePlanGroupId(), LOCALE).next()
                    .flatMap((PhonePlan phonePlan) -> {
                        return this.getClient(httpClient).getPhonePlanLocationOptions(COUNTRY_CODE, phonePlanGroup.getPhonePlanGroupId(), phonePlan.getPhonePlanId(), LOCALE);
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
            this.getClient(httpClient).listPhonePlanGroups(COUNTRY_CODE, LOCALE, true).next()
                .flatMap(phonePlanGroups -> {
                    return this.getClient(httpClient).listPhonePlans(COUNTRY_CODE, phonePlanGroups.getPhonePlanGroupId(), LOCALE).next()
                    .flatMap(phonePlans -> {
                        return this.getClient(httpClient).getAllAreaCodes(LocationType.SELECTION.toString(), COUNTRY_CODE, phonePlans.getPhonePlanId(), locationOptions);
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
            this.getClient(httpClient).listPhonePlanGroups(COUNTRY_CODE, LOCALE, true).next()
                .flatMap(phonePlanGroups -> {
                    return this.getClient(httpClient).listPhonePlans(COUNTRY_CODE, phonePlanGroups.getPhonePlanGroupId(), LOCALE).next()
                    .flatMap(phonePlans -> {
                        return this.getClient(httpClient).getAllAreaCodesWithResponse(LocationType.SELECTION.toString(), COUNTRY_CODE, phonePlans.getPhonePlanId(), locationOptions, Context.NONE);
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
    public void E2E_beginCreateReservation_GetReservationById_CancelReservation(HttpClient httpClient) {
        StepVerifier.create(
            // Setting up for phone number reservation creation
            this.getClient(httpClient).listPhonePlanGroups(COUNTRY_CODE, LOCALE, true).next()
                .flatMap((PhonePlanGroup phonePlanGroup) -> {
                    return this.getClient(httpClient).listPhonePlans(COUNTRY_CODE, phonePlanGroup.getPhonePlanGroupId(), LOCALE).next()
                    .flatMap((PhonePlan phonePlan) -> {
                        // Create Reservation
                        return beginCreateReservation(httpClient, phonePlan).last()
                        .flatMap((AsyncPollResponse<PhoneNumberReservation, PhoneNumberReservation> createdRes) -> {
                            assertEquals(createdRes.getValue().getPhoneNumbers().size(), 1);
                            assertNotNull(createdRes.getValue().getReservationId());
                            // Get Reservation by id
                            return this.getClient(httpClient).getReservationById(createdRes.getValue().getReservationId()).
                            flatMap(reservation -> {
                                assertEquals(createdRes.getValue().getReservationId(), reservation.getReservationId());
                                // Cancel Reservation
                                return this.getClient(httpClient).cancelReservation(reservation.getReservationId());
                            });
                        });
                    });
                }))
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void E2E_BeginCreateReservation_GetReservationById_CancelReservation_WithResponse(HttpClient httpClient) {
        StepVerifier.create(
            // Setting up for phone number reservation creation
            this.getClient(httpClient).listPhonePlanGroups(COUNTRY_CODE, LOCALE, true).next()
                .flatMap((PhonePlanGroup phonePlanGroup) -> {
                    return this.getClient(httpClient).listPhonePlans(COUNTRY_CODE, phonePlanGroup.getPhonePlanGroupId(), LOCALE).next()
                    .flatMap((PhonePlan phonePlan) -> {
                        // Create Reservation
                        return beginCreateReservation(httpClient, phonePlan).last()
                        .flatMap((AsyncPollResponse<PhoneNumberReservation, PhoneNumberReservation> createdRes) -> {
                            assertEquals(createdRes.getValue().getPhoneNumbers().size(), 1);
                            assertNotNull(createdRes.getValue().getReservationId());
                            // Get Reservation by id with response
                            return this.getClient(httpClient).getReservationByIdWithResponse(createdRes.getValue().getReservationId())
                            .flatMap((Response<PhoneNumberReservation> reservationResponse) -> {
                                assertEquals(200, reservationResponse.getStatusCode());
                                assertEquals(createdRes.getValue().getReservationId(), reservationResponse.getValue().getReservationId());
                                // Cancel Reservation with response
                                return this.getClient(httpClient).cancelReservationWithResponse(reservationResponse.getValue().getReservationId());
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
    public void E2E_BeginCreateReservation_BeginPurchaseReservation_TestCapabilitiesWithResponse_BeginReleasePhoneNumber(HttpClient httpClient) {
        StepVerifier.create(
            // Setting up for phone number reservation creation
            this.getClient(httpClient).listPhonePlanGroups(COUNTRY_CODE, LOCALE, true).next()
                .flatMap((PhonePlanGroup phonePlanGroup) -> {
                    return this.getClient(httpClient).listPhonePlans(COUNTRY_CODE, phonePlanGroup.getPhonePlanGroupId(), LOCALE).next()
                    .flatMap((PhonePlan phonePlan) -> {
                        // Create Reservation
                        return beginCreateReservation(httpClient, phonePlan).last()
                        .flatMap((AsyncPollResponse<PhoneNumberReservation, PhoneNumberReservation> createdRes) -> {
                            assertEquals(createdRes.getValue().getPhoneNumbers().size(), 1);
                            String purchasedNumber = createdRes.getValue().getPhoneNumbers().get(0);
                            // Purchase Reservation
                            return beginPurchaseReservation(httpClient, createdRes.getValue().getReservationId()).last()
                            .flatMap((AsyncPollResponse<Void, Void> response) -> {
                                assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED.toString(), response.getStatus().toString());
                                // Update capabilities of purchased phone number
                                List<Capability> capabilitiesToAdd = new ArrayList<>();
                                capabilitiesToAdd.add(Capability.INBOUND_CALLING);

                                NumberUpdateCapabilities update = new NumberUpdateCapabilities();
                                update.setAdd(capabilitiesToAdd);
                        
                                Map<PhoneNumber, NumberUpdateCapabilities> updateMap = new HashMap<>();
                                updateMap.put(new PhoneNumber(purchasedNumber), update);
                                return this.getClient(httpClient).updateCapabilitiesWithResponse(updateMap, Context.NONE)
                                .flatMap((Response<UpdateNumberCapabilitiesResponse> updateResponse) -> {
                                    assertEquals(200, updateResponse.getStatusCode());
                                    // Get capabilities update
                                    String capabilitiesUpdateId = updateResponse.getValue().getCapabilitiesUpdateId();
                                    assertNotNull(capabilitiesUpdateId);
                                    return this.getClient(httpClient).getCapabilitiesUpdateWithResponse(capabilitiesUpdateId, Context.NONE)
                                    .flatMap((Response<UpdatePhoneNumberCapabilitiesResponse> retrievedUpdateResponse) -> {
                                        assertEquals(200, retrievedUpdateResponse.getStatusCode());
                                        assertNotNull(retrievedUpdateResponse.getValue().getCapabilitiesUpdateId());
                                        // Release Phone Numbers
                                        return beginReleasePhoneNumbers(httpClient, purchasedNumber).last();
                                    });
                                });
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
    public void E2E_BeginCreateReservation_BeginPurchaseReservation_TestConfigurationWithResponse_BeginReleasePhoneNumber(HttpClient httpClient) {
        StepVerifier.create(
            // Setting up for phone number reservation creation
            this.getClient(httpClient).listPhonePlanGroups(COUNTRY_CODE, LOCALE, true).next()
                .flatMap((PhonePlanGroup phonePlanGroup) -> {
                    return this.getClient(httpClient).listPhonePlans(COUNTRY_CODE, phonePlanGroup.getPhonePlanGroupId(), LOCALE).next()
                    .flatMap((PhonePlan phonePlan) -> {
                        // Create Reservation
                        return beginCreateReservation(httpClient, phonePlan).last()
                        .flatMap((AsyncPollResponse<PhoneNumberReservation, PhoneNumberReservation> createdRes) -> {
                            assertEquals(createdRes.getValue().getPhoneNumbers().size(), 1);
                            String purchasedNumber = createdRes.getValue().getPhoneNumbers().get(0);
                            // Purchase Reservation
                            return beginPurchaseReservation(httpClient, createdRes.getValue().getReservationId()).last()
                            .flatMap((AsyncPollResponse<Void, Void> response) -> {
                                assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED.toString(), response.getStatus().toString());
                                // Configuring purchased number
                                PhoneNumber number = new PhoneNumber(purchasedNumber);
                                PstnConfiguration pstnConfiguration = new PstnConfiguration();
                                pstnConfiguration.setApplicationId("ApplicationId");
                                pstnConfiguration.setCallbackUrl("https://callbackurl");
                                return this.getClient(httpClient).configureNumberWithResponse(number, pstnConfiguration)
                                .flatMap((Response<Void> configResponse) -> {
                                    assertEquals(200, configResponse.getStatusCode());
                                    // Get configurations of purchased number
                                    return this.getClient(httpClient).getNumberConfigurationWithResponse(number, Context.NONE)
                                    .flatMap((Response<NumberConfigurationResponse> getConfigResponse) -> {
                                        assertEquals(200, getConfigResponse.getStatusCode());
                                        assertNotNull(getConfigResponse.getValue().getPstnConfiguration().getApplicationId());
                                        // Unconfigure the purchased number
                                        return this.getClient(httpClient).unconfigureNumberWithResponse(number, Context.NONE)
                                        .flatMap((Response<Void> unconfigureResponse) -> {
                                            assertEquals(200, unconfigureResponse.getStatusCode());
                                            // Release Phone Numbers
                                            return beginReleasePhoneNumbers(httpClient, purchasedNumber).last();
                                        });
                                    });
                                });
                            });
                        });
                    });
                }))
                .assertNext((AsyncPollResponse<PhoneNumberRelease, PhoneNumberRelease> releaseNumberResponse) -> {
                    assertEquals(ReleaseStatus.COMPLETE, releaseNumberResponse.getValue().getStatus());
                })
                .verifyComplete();
    }

    private PollerFlux<PhoneNumberRelease, PhoneNumberRelease> beginReleasePhoneNumbers(HttpClient httpClient, String phoneNumber) {
        PhoneNumber releasedPhoneNumber = new PhoneNumber(phoneNumber);
        List<PhoneNumber> phoneNumbers = new ArrayList<>();
        phoneNumbers.add(releasedPhoneNumber);
        Duration pollInterval = Duration.ofSeconds(1);
        return this.getClient(httpClient).beginReleasePhoneNumbers(phoneNumbers, pollInterval);
    }

    private PollerFlux<PhoneNumberReservation, PhoneNumberReservation> beginCreateReservation(HttpClient httpClient, PhonePlan phonePlan) {
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
 
    private  PollerFlux<Void, Void> beginPurchaseReservation(HttpClient httpClient, String reservationId) {
        Duration pollInterval = Duration.ofSeconds(1);
        return this.getClient(httpClient).beginPurchaseReservation(reservationId, pollInterval);
    }

    private PhoneNumberAsyncClient getClient(HttpClient httpClient) {
        return super.getClientBuilderWithConnectionString(httpClient).buildAsyncClient();
    }
}
