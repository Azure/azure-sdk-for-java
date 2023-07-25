// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.phonenumbers;

import com.azure.communication.phonenumbers.models.PhoneNumberAreaCode;
import com.azure.communication.phonenumbers.models.PhoneNumberAssignmentType;
import com.azure.communication.phonenumbers.models.PhoneNumberCapabilities;
import com.azure.communication.phonenumbers.models.PhoneNumberCapabilityType;
import com.azure.communication.phonenumbers.models.PhoneNumberCountry;
import com.azure.communication.phonenumbers.models.PhoneNumberLocality;
import com.azure.communication.phonenumbers.models.PhoneNumberOffering;
import com.azure.communication.phonenumbers.models.PhoneNumberOperation;
import com.azure.communication.phonenumbers.models.PhoneNumberOperationStatus;
import com.azure.communication.phonenumbers.models.PhoneNumberSearchOptions;
import com.azure.communication.phonenumbers.models.PhoneNumberSearchResult;
import com.azure.communication.phonenumbers.models.PhoneNumberType;
import com.azure.communication.phonenumbers.models.PurchasePhoneNumbersResult;
import com.azure.communication.phonenumbers.models.PurchasedPhoneNumber;
import com.azure.communication.phonenumbers.models.ReleasePhoneNumberResult;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PhoneNumbersAsyncClientIntegrationTest extends PhoneNumbersIntegrationTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPurchasedPhoneNumber(HttpClient httpClient) {
        String phoneNumber = redactIfPlaybackMode(getTestPhoneNumber());
        StepVerifier.create(
                this.getClientWithConnectionString(httpClient, "getPurchasedPhoneNumber")
                        .getPurchasedPhoneNumber(phoneNumber))
                .assertNext((PurchasedPhoneNumber number) -> {
                    assertEquals(phoneNumber, number.getPhoneNumber());
                    assertEquals(COUNTRY_CODE, number.getCountryCode());
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPurchasedPhoneNumberWithAAD(HttpClient httpClient) {
        String phoneNumber = redactIfPlaybackMode(getTestPhoneNumber());
        StepVerifier.create(
                this.getClientWithManagedIdentity(httpClient, "getPurchasedPhoneNumberWithAAD")
                        .getPurchasedPhoneNumber(phoneNumber))
                .assertNext((PurchasedPhoneNumber number) -> {
                    assertEquals(phoneNumber, number.getPhoneNumber());
                    assertEquals(COUNTRY_CODE, number.getCountryCode());
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPurchasedPhoneNumberWithResponse(HttpClient httpClient) {
        String phoneNumber = redactIfPlaybackMode(getTestPhoneNumber());
        StepVerifier.create(
                this.getClientWithConnectionString(httpClient, "getPurchasedPhoneNumberWithResponse")
                        .getPurchasedPhoneNumberWithResponse(phoneNumber))
                .assertNext((Response<PurchasedPhoneNumber> response) -> {
                    assertEquals(200, response.getStatusCode());
                    assertEquals(phoneNumber, response.getValue().getPhoneNumber());
                    assertEquals(COUNTRY_CODE, response.getValue().getCountryCode());
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listPurchasedPhoneNumbers(HttpClient httpClient) {
        StepVerifier.create(
                this.getClientWithConnectionString(httpClient, "listPurchasedPhoneNumbers").listPurchasedPhoneNumbers()
                        .next())
                .assertNext((PurchasedPhoneNumber number) -> {
                    assertNotNull(number.getPhoneNumber());
                    assertEquals(COUNTRY_CODE, number.getCountryCode());
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(named = "COMMUNICATION_SKIP_INT_PHONENUMBERS_TEST", matches = "(?i)(true)")
    public void beginSearchAvailablePhoneNumbers(HttpClient httpClient) {
        PhoneNumbersAsyncClient client =
            this.getClientWithConnectionString(httpClient, "beginSearchAvailablePhoneNumbers");
        StepVerifier.create(
            beginSearchAvailablePhoneNumbersHelper(client, true).last()
                .flatMap((AsyncPollResponse<PhoneNumberOperation, PhoneNumberSearchResult> result) -> {
                    return result.getFinalResult();
                }))
            .assertNext((PhoneNumberSearchResult searchResult) -> {
                assertEquals(searchResult.getPhoneNumbers().size(), 1);
                assertNotNull(searchResult.getSearchId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(named = "COMMUNICATION_SKIP_INT_PHONENUMBERS_TEST", matches = "(?i)(true)")
    public void beginSearchAvailablePhoneNumbersWithoutOptions(HttpClient httpClient) {
        PhoneNumbersAsyncClient client =
            this.getClientWithConnectionString(httpClient, "getPurchasedPhoneNumberForCapabilities");
        StepVerifier.create(
            beginSearchAvailablePhoneNumbersHelper(client, false).last()
                .flatMap((AsyncPollResponse<PhoneNumberOperation, PhoneNumberSearchResult> result) -> {
                    return result.getFinalResult();
                }))
        .assertNext((PhoneNumberSearchResult searchResult) -> {
            assertEquals(searchResult.getPhoneNumbers().size(), 1);
            assertNotNull(searchResult.getSearchId());
        })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(named = "SKIP_LIVE_TEST", matches = "(?i)(true)")
    public void beginPurchaseandReleasePhoneNumbers(HttpClient httpClient) {
        PhoneNumbersAsyncClient client =
            this.getClientWithConnectionString(httpClient, "getPurchasedPhoneNumberForCapabilities");
        StepVerifier.create(
            beginSearchAvailablePhoneNumbersHelper(client, true).last()
                .flatMap((AsyncPollResponse<PhoneNumberOperation, PhoneNumberSearchResult> result) -> {
                    return result.getFinalResult()
                        .flatMap((PhoneNumberSearchResult searchResult) -> {
                            String phoneNumber = searchResult.getPhoneNumbers().get(0);
                            return beginPurchasePhoneNumbersHelper(client, searchResult.getSearchId()
                            ).last()
                                .flatMap((
                                    AsyncPollResponse<PhoneNumberOperation, PurchasePhoneNumbersResult> purchaseResult) -> {
                                    assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                                        purchaseResult.getStatus());
                                    return beginReleasePhoneNumberHelper(client, phoneNumber
                                    ).last();
                                });
                        });
                }))
        .assertNext((AsyncPollResponse<PhoneNumberOperation, ReleasePhoneNumberResult> releaseResult) -> {
            assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, releaseResult.getStatus());

        })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(named = "COMMUNICATION_SKIP_INT_PHONENUMBERS_TEST", matches = "(?i)(true)")
    @DisabledIfEnvironmentVariable(named = "SKIP_UPDATE_CAPABILITIES_LIVE_TESTS", matches = "(?i)(true)")
    public void beginUpdatePhoneNumberCapabilities(HttpClient httpClient) {
        String phoneNumber = getTestPhoneNumber();
        PhoneNumbersAsyncClient client =
            this.getClientWithConnectionString(httpClient, "getPurchasedPhoneNumberForCapabilities");
        StepVerifier.create(
            client.getPurchasedPhoneNumberWithResponse(phoneNumber)
                .flatMap(responseAcquiredPhone -> {
                    PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities();
                    capabilities.setCalling(responseAcquiredPhone.getValue().getCapabilities()
                        .getCalling() == PhoneNumberCapabilityType.INBOUND
                        ? PhoneNumberCapabilityType.OUTBOUND
                        : PhoneNumberCapabilityType.INBOUND);
                    capabilities.setSms(responseAcquiredPhone.getValue().getCapabilities()
                        .getSms() == PhoneNumberCapabilityType.INBOUND_OUTBOUND
                        ? PhoneNumberCapabilityType.OUTBOUND
                        : PhoneNumberCapabilityType.INBOUND_OUTBOUND);
                    return beginUpdatePhoneNumberCapabilitiesHelper(client, phoneNumber, capabilities)
                        .last()
                        .flatMap((AsyncPollResponse<PhoneNumberOperation, PurchasedPhoneNumber> result) -> {
                            assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                                result.getStatus());
                            assertEquals(PhoneNumberOperationStatus.SUCCEEDED,
                                result.getValue().getStatus());
                            return result.getFinalResult();
                        });
                }))
        .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPurchasedPhoneNumberNullNumber(HttpClient httpClient) {
        StepVerifier.create(
                this.getClientWithConnectionString(httpClient, "getPurchasedPhoneNumberNullNumber")
                        .getPurchasedPhoneNumber(null))
                .verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPurchasedPhoneNumberWithResponseNullNumber(HttpClient httpClient) {
        StepVerifier.create(
                this.getClientWithConnectionString(httpClient, "getPurchasedPhoneNumberWithResponseNullNumber")
                        .getPurchasedPhoneNumberWithResponse(null))
                .verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void beginSearchAvailablePhoneNumbersNullCountryCode(HttpClient httpClient) {
        StepVerifier.create(
                this.getClientWithConnectionString(httpClient, "beginSearchAvailablePhoneNumbersNullCountryCode")
                        .beginSearchAvailablePhoneNumbers(null, PhoneNumberType.TOLL_FREE,
                                PhoneNumberAssignmentType.APPLICATION, null, null))
                .verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void beginUpdatePhoneNumberCapabilitiesNullPhoneNumber(HttpClient httpClient) {
        StepVerifier.create(
                this.getClientWithConnectionString(httpClient, "beginUpdatePhoneNumberCapabilitiesNullPhoneNumber")
                        .beginUpdatePhoneNumberCapabilities(null, new PhoneNumberCapabilities()))
                .verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void beginUpdatePhoneNumberCapabilitiesUnauthorizedPhoneNumber(HttpClient httpClient) {
        PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities();
        capabilities.setCalling(PhoneNumberCapabilityType.INBOUND);
        StepVerifier.create(
            this.getClientWithConnectionString(httpClient, "beginUpdatePhoneNumberCapabilitiesUnauthorizedPhoneNumber")
                .beginUpdatePhoneNumberCapabilities("+14255555111", capabilities)
            )
            .verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void beginUpdatePhoneNumberCapabilitiesInvalidPhoneNumber(HttpClient httpClient) {
        PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities();
        capabilities.setCalling(PhoneNumberCapabilityType.INBOUND);
        StepVerifier.create(
            this.getClientWithConnectionString(httpClient, "beginUpdatePhoneNumberCapabilitiesInvalidPhoneNumber")
                .beginUpdatePhoneNumberCapabilities("invalid-phone-number", capabilities)
            )
            .verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTollFreeAreaCodesWithAAD(HttpClient httpClient) {
        StepVerifier.create(
                this.getClientWithManagedIdentity(httpClient, "listAvailableTollFreeAreaCodes")
                        .listAvailableTollFreeAreaCodes("US", PhoneNumberAssignmentType.APPLICATION).next())
                .assertNext((PhoneNumberAreaCode areaCodes) -> {
                    assertNotNull(areaCodes.getAreaCode());
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getGeographicAreaCodesWithAAD(HttpClient httpClient) {
        PhoneNumberLocality locality = this.getClientWithConnectionString(httpClient, "listAvailableLocalities")
                .listAvailableLocalities("US", null).blockFirst();
        StepVerifier.create(
                this.getClientWithManagedIdentity(httpClient, "listAvailableGeographicAreaCodes")
                        .listAvailableGeographicAreaCodes("US", PhoneNumberAssignmentType.PERSON,
                                locality.getLocalizedName(), locality.getAdministrativeDivision().getAbbreviatedName())
                        .next())
                .assertNext((PhoneNumberAreaCode areaCodes) -> {
                    assertNotNull(areaCodes);
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getCountriesWithAAD(HttpClient httpClient) {
        StepVerifier.create(
                this.getClientWithManagedIdentity(httpClient, "listAvailableCountries").listAvailableCountries().next())
                .assertNext((PhoneNumberCountry country) -> {
                    assertNotNull(country);
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getLocalitiesWithAAD(HttpClient httpClient) {
        StepVerifier.create(
                this.getClientWithManagedIdentity(httpClient, "listAvailableLocalities")
                        .listAvailableLocalities("US", null).next())
                .assertNext((PhoneNumberLocality locality) -> {
                    assertNotNull(locality);
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getLocalitiesAdministrativeDivisionWithAAD(HttpClient httpClient) {
        PhoneNumberLocality locality = this.getClientWithConnectionString(httpClient, "listAvailableLocalities")
                .listAvailableLocalities("US", null).blockFirst();
        StepVerifier.create(
                this.getClientWithManagedIdentity(httpClient, "listAvailableLocalities")
                        .listAvailableLocalities("US", locality.getAdministrativeDivision().getAbbreviatedName()).next())
                .assertNext((PhoneNumberLocality localityWithAD) -> {
                    assertNotNull(localityWithAD);
                    assertEquals(localityWithAD.getAdministrativeDivision().getAbbreviatedName(), locality.getAdministrativeDivision().getAbbreviatedName());
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getOfferingsWithAAD(HttpClient httpClient) {
        StepVerifier.create(
                this.getClientWithManagedIdentity(httpClient, "listAvailableOfferings")
                        .listAvailableOfferings("US", null, null).next())
                .assertNext((PhoneNumberOffering offering) -> {
                    assertNotNull(offering);
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getTollFreeAreaCodes(HttpClient httpClient) {
        StepVerifier.create(
                this.getClientWithConnectionString(httpClient, "listAvailableTollFreeAreaCodes")
                        .listAvailableTollFreeAreaCodes("US", PhoneNumberAssignmentType.APPLICATION).next())
                .assertNext((PhoneNumberAreaCode areaCodes) -> {
                    assertNotNull(areaCodes.getAreaCode());
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getGeographicAreaCodes(HttpClient httpClient) {
        PhoneNumberLocality locality = this.getClientWithConnectionString(httpClient, "listAvailableLocalities")
                .listAvailableLocalities("US", null).blockFirst();
        StepVerifier.create(
                this.getClientWithConnectionString(httpClient, "listAvailableGeographicAreaCodes")
                        .listAvailableGeographicAreaCodes("US", PhoneNumberAssignmentType.PERSON,
                                locality.getLocalizedName(), locality.getAdministrativeDivision().getAbbreviatedName())
                        .next())
                .assertNext((PhoneNumberAreaCode areaCodes) -> {
                    assertNotNull(areaCodes);
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getCountries(HttpClient httpClient) {
        StepVerifier.create(
                this.getClientWithConnectionString(httpClient, "listAvailableCountries").listAvailableCountries()
                        .next())
                .assertNext((PhoneNumberCountry country) -> {
                    assertNotNull(country);
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getLocalities(HttpClient httpClient) {
        StepVerifier.create(
                this.getClientWithConnectionString(httpClient, "listAvailableLocalities")
                        .listAvailableLocalities("US", null).next())
                .assertNext((PhoneNumberLocality locality) -> {
                    assertNotNull(locality);
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getLocalitiesAdministrativeDivision(HttpClient httpClient) {
        String localityAdministraiveDivision = this.getClientWithConnectionString(httpClient, "listAvailableLocalities")
                .listAvailableLocalities("US", null).blockFirst().getAdministrativeDivision().getAbbreviatedName();
        StepVerifier.create(
                this.getClientWithConnectionString(httpClient, "listAvailableLocalities")
                        .listAvailableLocalities("US", localityAdministraiveDivision).next())
                .assertNext((PhoneNumberLocality locality) -> {
                    assertNotNull(locality);
                    assertEquals(locality.getAdministrativeDivision().getAbbreviatedName(),
                            localityAdministraiveDivision);
                })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getOfferings(HttpClient httpClient) {
        StepVerifier.create(
                this.getClientWithConnectionString(httpClient, "listAvailableOfferings")
                        .listAvailableOfferings("US", null, null).next())
                .assertNext((PhoneNumberOffering offering) -> {
                    assertNotNull(offering);
                })
                .verifyComplete();
    }

    private PollerFlux<PhoneNumberOperation, PhoneNumberSearchResult> beginSearchAvailablePhoneNumbersHelper(
        PhoneNumbersAsyncClient client, boolean withOptions) {
        PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities();
        capabilities.setCalling(PhoneNumberCapabilityType.INBOUND);
        capabilities.setSms(PhoneNumberCapabilityType.INBOUND_OUTBOUND);
        PhoneNumberSearchOptions searchOptions = new PhoneNumberSearchOptions().setQuantity(1);

        if (withOptions) {
            return setPollInterval(client
                    .beginSearchAvailablePhoneNumbers(
                            COUNTRY_CODE,
                            PhoneNumberType.TOLL_FREE,
                            PhoneNumberAssignmentType.APPLICATION,
                            capabilities,
                            searchOptions));
        }
        return setPollInterval(client
                .beginSearchAvailablePhoneNumbers(
                        COUNTRY_CODE,
                        PhoneNumberType.TOLL_FREE,
                        PhoneNumberAssignmentType.APPLICATION,
                        capabilities));
    }

    private PollerFlux<PhoneNumberOperation, PurchasePhoneNumbersResult> beginPurchasePhoneNumbersHelper(
        PhoneNumbersAsyncClient client, String searchId) {
        return setPollInterval(client
                .beginPurchasePhoneNumbers(searchId));
    }

    private PollerFlux<PhoneNumberOperation, ReleasePhoneNumberResult> beginReleasePhoneNumberHelper(
        PhoneNumbersAsyncClient client, String phoneNumber) {
        return setPollInterval(client
                .beginReleasePhoneNumber(phoneNumber));
    }

    private PollerFlux<PhoneNumberOperation, PurchasedPhoneNumber> beginUpdatePhoneNumberCapabilitiesHelper(
        PhoneNumbersAsyncClient client, String phoneNumber, PhoneNumberCapabilities capabilities) {

        return setPollInterval(client
                .beginUpdatePhoneNumberCapabilities(phoneNumber, capabilities));
    }

    private <T, U> PollerFlux<T, U> setPollInterval(PollerFlux<T, U> pollerFlux) {
        return interceptorManager.isPlaybackMode()
                ? pollerFlux.setPollInterval(Duration.ofMillis(1))
                : pollerFlux.setPollInterval(Duration.ofSeconds(1));
    }

    private PhoneNumbersAsyncClient getClientWithConnectionString(HttpClient httpClient, String testName) {
        PhoneNumbersClientBuilder builder = super.getClientBuilderWithConnectionString(httpClient);
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }

    private PhoneNumbersAsyncClient getClientWithManagedIdentity(HttpClient httpClient, String testName) {
        PhoneNumbersClientBuilder builder = super.getClientBuilderUsingManagedIdentity(httpClient);
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }
}
