// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.phonenumbers;

import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Duration;

import com.azure.communication.phonenumbers.models.AcquiredPhoneNumber;
import com.azure.communication.phonenumbers.models.PhoneNumberAssignmentType;
import com.azure.communication.phonenumbers.models.PhoneNumberCapabilities;
import com.azure.communication.phonenumbers.models.PhoneNumberCapabilitiesRequest;
import com.azure.communication.phonenumbers.models.PhoneNumberCapabilityValue;
import com.azure.communication.phonenumbers.models.PhoneNumberOperation;
import com.azure.communication.phonenumbers.models.PhoneNumberSearchRequest;
import com.azure.communication.phonenumbers.models.PhoneNumberSearchResult;
import com.azure.communication.phonenumbers.models.PhoneNumberType;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;

public class PhoneNumbersAsyncClientIntegrationTest extends PhoneNumbersIntegrationTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPhoneNumber(HttpClient httpClient) {
        String phoneNumber = getTestPhoneNumber(PHONE_NUMBER);
        StepVerifier.create(
            this.getClientWithConnectionString(httpClient, "getPhoneNumber").getPhoneNumber(phoneNumber)
            )
            .assertNext((AcquiredPhoneNumber number) -> {
                assertEquals(phoneNumber, number.getPhoneNumber());
                assertEquals(COUNTRY_CODE, number.getCountryCode());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPhoneNumberWithAAD(HttpClient httpClient) {
        String phoneNumber = getTestPhoneNumber(PHONE_NUMBER);
        StepVerifier.create(
            this.getClientWithManagedIdentity(httpClient, "getPhoneNumber").getPhoneNumber(phoneNumber)
            )
            .assertNext((AcquiredPhoneNumber number) -> {
                assertEquals(phoneNumber, number.getPhoneNumber());
                assertEquals(COUNTRY_CODE, number.getCountryCode());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPhoneNumberWithResponse(HttpClient httpClient) {
        String phoneNumber = getTestPhoneNumber(PHONE_NUMBER);
        StepVerifier.create(
            this.getClientWithConnectionString(httpClient, "getPhoneNumberWithResponse").getPhoneNumberWithResponse(phoneNumber)
        )
        .assertNext((Response<AcquiredPhoneNumber> response) -> {
            assertEquals(200, response.getStatusCode());
            assertEquals(phoneNumber, response.getValue().getPhoneNumber());
            assertEquals(COUNTRY_CODE, response.getValue().getCountryCode());
        })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listPhoneNumbers(HttpClient httpClient) {
        StepVerifier.create(
            this.getClientWithConnectionString(httpClient, "listPhoneNumbers").listPhoneNumbers().next()
        )
        .assertNext((AcquiredPhoneNumber number) -> {
            assertNotNull(number.getPhoneNumber());
            assertEquals(COUNTRY_CODE, number.getCountryCode());
        })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void beginSearchAvailablePhoneNumbers(HttpClient httpClient) {
        StepVerifier.create(
            beginSearchAvailablePhoneNumbersHelper(httpClient, "beginSearchAvailablePhoneNumbers").last()
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
            beginSearchAvailablePhoneNumbersHelper(httpClient, "beginSearchAvailablePhoneNumbers").last()
            .flatMap((AsyncPollResponse<PhoneNumberOperation, PhoneNumberSearchResult> result) -> {
                return result.getFinalResult()
                .flatMap((PhoneNumberSearchResult searchResult) -> {
                    String phoneNumber = getTestPhoneNumber(searchResult.getPhoneNumbers().get(0));
                    return beginPurchasePhoneNumbersHelper(httpClient, searchResult.getSearchId(), "beginPurchasePhoneNumbers").last()
                    .flatMap((AsyncPollResponse<PhoneNumberOperation, Void> purchaseResult)  -> {
                        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, purchaseResult.getStatus());
                        return beginReleasePhoneNumberHelper(httpClient, phoneNumber, "beginReleasePhoneNumber").last();
                    });
                });
            })
        ).assertNext((AsyncPollResponse<PhoneNumberOperation, Void> releaseResult)  -> {
            assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, releaseResult.getStatus());

        })
        .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void beginUpdatePhoneNumberCapabilities(HttpClient httpClient) {
        String phoneNumber = getTestPhoneNumber(PHONE_NUMBER);
        StepVerifier.create(
            beginUpdatePhoneNumberCapabilitiesHelper(httpClient, phoneNumber, "beginUpdatePhoneNumberCapabilities").last()
            .flatMap((AsyncPollResponse<PhoneNumberOperation, AcquiredPhoneNumber> result) -> {
                assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, result.getStatus());
                return result.getFinalResult();
            }) 
        ).assertNext((AcquiredPhoneNumber acquiredPhoneNumber) -> {
            assertEquals(PhoneNumberCapabilityValue.INBOUND_OUTBOUND, acquiredPhoneNumber.getCapabilities().getSms());
            assertEquals(PhoneNumberCapabilityValue.INBOUND, acquiredPhoneNumber.getCapabilities().getCalling());
        })
        .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPhoneNumberNullNumber(HttpClient httpClient) {
        StepVerifier.create(
            this.getClientWithConnectionString(httpClient, "getPhoneNumberNullNumber").getPhoneNumber(null)
            )
            .verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPhoneNumberWithResponseNullNumber(HttpClient httpClient) {
        StepVerifier.create(
            this.getClientWithConnectionString(httpClient, "getPhoneNumberWithResponseNullNumber").getPhoneNumberWithResponse(null)
            )
            .verifyError();
    }
    
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void beginSearchAvailablePhoneNumbersNullCountryCode(HttpClient httpClient) {
        StepVerifier.create(
            this.getClientWithConnectionString(httpClient, "beginSearchAvailablePhoneNumbersNullCountryCode")
                .beginSearchAvailablePhoneNumbers(null, new PhoneNumberSearchRequest())
            )
            .verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void beginUpdatePhoneNumberCapabilitiesNullPhoneNumber(HttpClient httpClient) {
        StepVerifier.create(
            this.getClientWithConnectionString(httpClient, "beginUpdatePhoneNumberCapabilitiesNullPhoneNumber")
                .beginUpdatePhoneNumberCapabilities(null, new PhoneNumberCapabilitiesRequest())
            )
            .verifyError();
    }

    private PollerFlux<PhoneNumberOperation, PhoneNumberSearchResult> beginSearchAvailablePhoneNumbersHelper(HttpClient httpClient, String testName) {
        PhoneNumberSearchRequest phoneNumberSearchRequest = new PhoneNumberSearchRequest();
        PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities();
        capabilities.setCalling(PhoneNumberCapabilityValue.INBOUND);
        capabilities.setSms(PhoneNumberCapabilityValue.INBOUND_OUTBOUND);
        phoneNumberSearchRequest
            .setAreaCode(AREA_CODE)
            .setAssignmentType(PhoneNumberAssignmentType.APPLICATION)
            .setPhoneNumberType(PhoneNumberType.TOLL_FREE)
            .setCapabilities(capabilities)
            .setQuantity(1);

        return this.getClientWithConnectionString(httpClient, testName)
            .beginSearchAvailablePhoneNumbers(COUNTRY_CODE, phoneNumberSearchRequest).setPollInterval(Duration.ofSeconds(1));
    }

    private PollerFlux<PhoneNumberOperation, Void> beginPurchasePhoneNumbersHelper(HttpClient httpClient, String searchId, String testName) {
        return this.getClientWithConnectionString(httpClient, testName)
            .beginPurchasePhoneNumbers(searchId).setPollInterval(Duration.ofSeconds(1));
    }

    private PollerFlux<PhoneNumberOperation, Void> beginReleasePhoneNumberHelper(HttpClient httpClient, String phoneNumber, String testName) {
        if (getTestMode() == TestMode.PLAYBACK) {
            phoneNumber = "+REDACTED";
        }
        return this.getClientWithConnectionString(httpClient, testName)
            .beginReleasePhoneNumber(phoneNumber).setPollInterval(Duration.ofSeconds(1));
    }

    private PollerFlux<PhoneNumberOperation, AcquiredPhoneNumber> beginUpdatePhoneNumberCapabilitiesHelper(HttpClient httpClient, String phoneNumber, String testName) {
        PhoneNumberCapabilitiesRequest capabilitiesUpdateRequest = new PhoneNumberCapabilitiesRequest();
        capabilitiesUpdateRequest.setCalling(PhoneNumberCapabilityValue.INBOUND);
        capabilitiesUpdateRequest.setSms(PhoneNumberCapabilityValue.INBOUND_OUTBOUND);
        return this.getClientWithConnectionString(httpClient, testName)
            .beginUpdatePhoneNumberCapabilities(phoneNumber, capabilitiesUpdateRequest).setPollInterval(Duration.ofSeconds(1));
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
