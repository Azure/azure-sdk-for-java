// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.phonenumbers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Duration;

import com.azure.communication.phonenumbers.models.PhoneNumberAssignmentType;
import com.azure.communication.phonenumbers.models.PhoneNumberCapabilities;
import com.azure.communication.phonenumbers.models.PhoneNumberCapabilityValue;
import com.azure.communication.phonenumbers.models.PhoneNumberOperationResult;
import com.azure.communication.phonenumbers.models.PhoneNumberSearchRequest;
import com.azure.communication.phonenumbers.models.PhoneNumberSearchResult;
import com.azure.communication.phonenumbers.models.PhoneNumberType;
import com.azure.core.http.HttpClient;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.PollerFlux;

public class PhoneNumbersAsyncClientIntegrationTest extends PhoneNumbersIntegrationTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")

    public void beginSearchAvailablePhoneNumbers(HttpClient httpClient) {
        StepVerifier.create(
            beginSearchAvailablePhoneNumbersHelper(httpClient, "beginSearchAvailablePhoneNumbers").last()
            .flatMap((AsyncPollResponse<PhoneNumberOperationResult, PhoneNumberSearchResult> result) -> {
                return result.getFinalResult();
            }) 
        ).assertNext((PhoneNumberSearchResult searchResult) -> {
            assertEquals(searchResult.getPhoneNumbers().size(), 1);
            assertNotNull(searchResult.getSearchId());
        })
        .verifyComplete();
    }

    private PollerFlux<PhoneNumberOperationResult, PhoneNumberSearchResult> beginSearchAvailablePhoneNumbersHelper(HttpClient httpClient, String testName) {
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

        Duration duration = Duration.ofSeconds(1);
        return this.getClientWithConnectionString(httpClient, testName)
            .beginSearchAvailablePhoneNumbers(COUNTRY_CODE, phoneNumberSearchRequest, duration);
    }

    private PhoneNumbersAsyncClient getClientWithConnectionString(HttpClient httpClient, String testName) {
        PhoneNumbersClientBuilder builder = super.getClientBuilderWithConnectionString(httpClient);
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }
}
