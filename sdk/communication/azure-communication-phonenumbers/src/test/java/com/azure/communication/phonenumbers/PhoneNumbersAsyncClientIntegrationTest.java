// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.phonenumbers;

import com.azure.communication.phonenumbers.models.PhoneNumberAssignmentType;
import com.azure.communication.phonenumbers.models.PhoneNumberCapabilities;
import com.azure.communication.phonenumbers.models.PhoneNumberCapabilityType;
import com.azure.communication.phonenumbers.models.PhoneNumberOperation;
import com.azure.communication.phonenumbers.models.PhoneNumberSearchOptions;
import com.azure.communication.phonenumbers.models.PhoneNumberSearchResult;
import com.azure.communication.phonenumbers.models.PhoneNumberType;
import com.azure.communication.phonenumbers.models.PurchasePhoneNumbersResult;
import com.azure.communication.phonenumbers.models.PurchasedPhoneNumber;
import com.azure.communication.phonenumbers.models.ReleasePhoneNumberResult;
import com.azure.communication.phonenumbers.models.PhoneNumberOperationStatus;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
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
        String phoneNumber = getTestPhoneNumber(PHONE_NUMBER);
        StepVerifier.create(
            this.getClientWithConnectionString(httpClient, "getPurchasedPhoneNumber").getPurchasedPhoneNumber(phoneNumber)
            )
            .assertNext((PurchasedPhoneNumber number) -> {
                assertEquals(phoneNumber, number.getPhoneNumber());
                assertEquals(COUNTRY_CODE, number.getCountryCode());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPurchasedPhoneNumberWithAAD(HttpClient httpClient) {
        String phoneNumber = getTestPhoneNumber(PHONE_NUMBER);
        StepVerifier.create(
            this.getClientWithManagedIdentity(httpClient, "getPurchasedPhoneNumberWithAAD").getPurchasedPhoneNumber(phoneNumber)
            )
            .assertNext((PurchasedPhoneNumber number) -> {
                assertEquals(phoneNumber, number.getPhoneNumber());
                assertEquals(COUNTRY_CODE, number.getCountryCode());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPurchasedPhoneNumberWithResponse(HttpClient httpClient) {
        String phoneNumber = getTestPhoneNumber(PHONE_NUMBER);
        StepVerifier.create(
            this.getClientWithConnectionString(httpClient, "getPurchasedPhoneNumberWithResponse").getPurchasedPhoneNumberWithResponse(phoneNumber)
        )
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
            this.getClientWithConnectionString(httpClient, "listPurchasedPhoneNumbers").listPurchasedPhoneNumbers().next()
        )
        .assertNext((PurchasedPhoneNumber number) -> {
            assertNotNull(number.getPhoneNumber());
            assertEquals(COUNTRY_CODE, number.getCountryCode());
        })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "COMMUNICATION_SKIP_INT_PHONENUMBERS_TEST",
        matches = "(?i)(true)")
    public void beginSearchAvailablePhoneNumbers(HttpClient httpClient) {
        StepVerifier.create(
            beginSearchAvailablePhoneNumbersHelper(httpClient, "beginSearchAvailablePhoneNumbers", true).last()
            .flatMap((AsyncPollResponse<PhoneNumberOperation, PhoneNumberSearchResult> result) -> {
                return result.getFinalResult();
            })
        ).assertNext((PhoneNumberSearchResult searchResult) -> {
            assertEquals(searchResult.getPhoneNumbers().size(), 1);
            assertNotNull(searchResult.getSearchId());
        })
        .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "COMMUNICATION_SKIP_INT_PHONENUMBERS_TEST",
        matches = "(?i)(true)")
    public void beginSearchAvailablePhoneNumbersWithoutOptions(HttpClient httpClient) {
        StepVerifier.create(
            beginSearchAvailablePhoneNumbersHelper(httpClient, "beginSearchAvailablePhoneNumbersWithoutOptions", false).last()
            .flatMap((AsyncPollResponse<PhoneNumberOperation, PhoneNumberSearchResult> result) -> {
                return result.getFinalResult();
            })
        ).assertNext((PhoneNumberSearchResult searchResult) -> {
            assertEquals(searchResult.getPhoneNumbers().size(), 1);
            assertNotNull(searchResult.getSearchId());
        })
        .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)")
    public void beginPurchaseandReleasePhoneNumbers(HttpClient httpClient) {
        StepVerifier.create(
            beginSearchAvailablePhoneNumbersHelper(httpClient, "beginSearchAvailablePhoneNumbers", true).last()
            .flatMap((AsyncPollResponse<PhoneNumberOperation, PhoneNumberSearchResult> result) -> {
                return result.getFinalResult()
                .flatMap((PhoneNumberSearchResult searchResult) -> {
                    String phoneNumber = getTestPhoneNumber(searchResult.getPhoneNumbers().get(0));
                    return beginPurchasePhoneNumbersHelper(httpClient, searchResult.getSearchId(), "beginPurchasePhoneNumbers").last()
                    .flatMap((AsyncPollResponse<PhoneNumberOperation, PurchasePhoneNumbersResult> purchaseResult)  -> {
                        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, purchaseResult.getStatus());
                        return beginReleasePhoneNumberHelper(httpClient, phoneNumber, "beginReleasePhoneNumber").last();
                    });
                });
            })
        ).assertNext((AsyncPollResponse<PhoneNumberOperation, ReleasePhoneNumberResult> releaseResult)  -> {
            assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, releaseResult.getStatus());

        })
        .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "COMMUNICATION_SKIP_INT_PHONENUMBERS_TEST",
        matches = "(?i)(true)")
    public void beginUpdatePhoneNumberCapabilities(HttpClient httpClient) {
        String phoneNumber = getTestPhoneNumber(PHONE_NUMBER);

        StepVerifier.create(
            this.getClientWithConnectionString(httpClient, "getPurchasedPhoneNumberForCapabilities").getPurchasedPhoneNumberWithResponse(phoneNumber)
                .flatMap(responseAcquiredPhone -> {
                    PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities();
                    capabilities.setCalling(responseAcquiredPhone.getValue().getCapabilities().getCalling() == PhoneNumberCapabilityType.INBOUND ? PhoneNumberCapabilityType.OUTBOUND : PhoneNumberCapabilityType.INBOUND);
                    capabilities.setSms(responseAcquiredPhone.getValue().getCapabilities().getSms() == PhoneNumberCapabilityType.INBOUND_OUTBOUND ? PhoneNumberCapabilityType.OUTBOUND : PhoneNumberCapabilityType.INBOUND_OUTBOUND);
                    return beginUpdatePhoneNumberCapabilitiesHelper(httpClient, phoneNumber, "beginUpdatePhoneNumberCapabilities", capabilities)
                        .last()
                        .flatMap((AsyncPollResponse<PhoneNumberOperation, PurchasedPhoneNumber> result) -> {
                            assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, result.getStatus());
                            assertEquals(PhoneNumberOperationStatus.SUCCEEDED, result.getValue().getStatus());
                            return result.getFinalResult();
                        });

                })
        ).assertNext((PurchasedPhoneNumber acquiredPhoneNumber) -> {
            assertNotNull(acquiredPhoneNumber);
        })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPurchasedPhoneNumberNullNumber(HttpClient httpClient) {
        StepVerifier.create(
            this.getClientWithConnectionString(httpClient, "getPurchasedPhoneNumberNullNumber").getPurchasedPhoneNumber(null)
            )
            .verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPurchasedPhoneNumberWithResponseNullNumber(HttpClient httpClient) {
        StepVerifier.create(
            this.getClientWithConnectionString(httpClient, "getPurchasedPhoneNumberWithResponseNullNumber").getPurchasedPhoneNumberWithResponse(null)
            )
            .verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void beginSearchAvailablePhoneNumbersNullCountryCode(HttpClient httpClient) {
        StepVerifier.create(
            this.getClientWithConnectionString(httpClient, "beginSearchAvailablePhoneNumbersNullCountryCode")
                .beginSearchAvailablePhoneNumbers(null, PhoneNumberType.TOLL_FREE, PhoneNumberAssignmentType.APPLICATION, null, null)
            )
            .verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void beginUpdatePhoneNumberCapabilitiesNullPhoneNumber(HttpClient httpClient) {
        StepVerifier.create(
            this.getClientWithConnectionString(httpClient, "beginUpdatePhoneNumberCapabilitiesNullPhoneNumber")
                .beginUpdatePhoneNumberCapabilities(null, new PhoneNumberCapabilities())
            )
            .verifyError();
    }

    private PollerFlux<PhoneNumberOperation, PhoneNumberSearchResult> beginSearchAvailablePhoneNumbersHelper(HttpClient httpClient, String testName, boolean withOptions) {
        PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities();
        capabilities.setCalling(PhoneNumberCapabilityType.INBOUND);
        capabilities.setSms(PhoneNumberCapabilityType.INBOUND_OUTBOUND);
        PhoneNumberSearchOptions searchOptions = new PhoneNumberSearchOptions().setQuantity(1);

        if (withOptions) {
            return setPollInterval(this.getClientWithConnectionString(httpClient, testName)
            .beginSearchAvailablePhoneNumbers(
                COUNTRY_CODE,
                PhoneNumberType.TOLL_FREE,
                PhoneNumberAssignmentType.APPLICATION,
                capabilities,
                searchOptions
                ));
        }
        return setPollInterval(this.getClientWithConnectionString(httpClient, testName)
            .beginSearchAvailablePhoneNumbers(
                COUNTRY_CODE,
                PhoneNumberType.TOLL_FREE,
                PhoneNumberAssignmentType.APPLICATION,
                capabilities
                ));
    }

    private PollerFlux<PhoneNumberOperation, PurchasePhoneNumbersResult> beginPurchasePhoneNumbersHelper(HttpClient httpClient, String searchId, String testName) {
        return setPollInterval(this.getClientWithConnectionString(httpClient, testName)
            .beginPurchasePhoneNumbers(searchId));
    }

    private PollerFlux<PhoneNumberOperation, ReleasePhoneNumberResult> beginReleasePhoneNumberHelper(HttpClient httpClient, String phoneNumber, String testName) {
        if (getTestMode() == TestMode.PLAYBACK) {
            phoneNumber = "+REDACTED";
        }
        return setPollInterval(this.getClientWithConnectionString(httpClient, testName)
            .beginReleasePhoneNumber(phoneNumber));
    }

    private PollerFlux<PhoneNumberOperation, PurchasedPhoneNumber> beginUpdatePhoneNumberCapabilitiesHelper(HttpClient httpClient, String phoneNumber, String testName, PhoneNumberCapabilities capabilities) {

        return setPollInterval(this.getClientWithConnectionString(httpClient, testName)
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

    private String getTestPhoneNumber(String phoneNumber) {
        if (getTestMode() == TestMode.PLAYBACK) {
            phoneNumber = "+REDACTED";
        }
        return phoneNumber;
    }
}
