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
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.util.Context;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class PhoneNumbersClientIntegrationTest extends PhoneNumbersIntegrationTestBase {

    @Override
    protected void beforeTest() {
        super.beforeTest();
        assumeTrue(shouldEnablePhoneNumbersTests());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPurchasedPhoneNumber(HttpClient httpClient) {
        String phoneNumber = getTestPhoneNumber(PHONE_NUMBER);
        PurchasedPhoneNumber number = this.getClientWithConnectionString(httpClient, "getPurchasedPhoneNumberSync").getPurchasedPhoneNumber(phoneNumber);
        assertEquals(phoneNumber, number.getPhoneNumber());
        assertEquals(COUNTRY_CODE, number.getCountryCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPurchasedPhoneNumberWithAAD(HttpClient httpClient) {
        String phoneNumber = getTestPhoneNumber(PHONE_NUMBER);
        PurchasedPhoneNumber number = this.getClientWithManagedIdentity(httpClient, "getPurchasedPhoneNumberWithAADSync").getPurchasedPhoneNumber(phoneNumber);
        assertEquals(phoneNumber, number.getPhoneNumber());
        assertEquals(COUNTRY_CODE, number.getCountryCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPurchasedPhoneNumberWithResponse(HttpClient httpClient) {
        String phoneNumber = getTestPhoneNumber(PHONE_NUMBER);
        Response<PurchasedPhoneNumber> response = this.getClientWithConnectionString(httpClient, "getPurchasedPhoneNumberWithResponseSync")
            .getPurchasedPhoneNumberWithResponse(phoneNumber, Context.NONE);
        PurchasedPhoneNumber number = response.getValue();
        assertEquals(200, response.getStatusCode());
        assertEquals(phoneNumber, number.getPhoneNumber());
        assertEquals(COUNTRY_CODE, number.getCountryCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listPurchasedPhoneNumbers(HttpClient httpClient) {
        PagedIterable<PurchasedPhoneNumber> numbers = this.getClientWithConnectionString(httpClient, "listPurchasedPhoneNumbersSync").listPurchasedPhoneNumbers(Context.NONE);
        PurchasedPhoneNumber number = numbers.iterator().next();
        assertNotNull(number.getPhoneNumber());
        assertEquals(COUNTRY_CODE, number.getCountryCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listPurchasedPhoneNumbersWithoutContext(HttpClient httpClient) {
        PagedIterable<PurchasedPhoneNumber> numbers = this.getClientWithConnectionString(httpClient, "listPurchasedPhoneNumbersSync").listPurchasedPhoneNumbers();
        PurchasedPhoneNumber number = numbers.iterator().next();
        assertNotNull(number.getPhoneNumber());
        assertEquals(COUNTRY_CODE, number.getCountryCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)")
    public void beginSearchAvailablePhoneNumbers(HttpClient httpClient) {
        PhoneNumberSearchResult searchResult = beginSearchAvailablePhoneNumbersHelper(httpClient, "beginSearchAvailablePhoneNumbersSync", true).getFinalResult();
        assertEquals(searchResult.getPhoneNumbers().size(), 1);
        assertNotNull(searchResult.getSearchId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)")
    public void beginPurchaseandReleasePhoneNumbersWithoutContext(HttpClient httpClient) {
        PhoneNumberSearchResult searchResult = beginSearchAvailablePhoneNumbersHelper(httpClient, "beginPurchaseandReleasePhoneNumbers_beginSearchAvailablePhoneNumbersWithoutContextSync", false).getFinalResult();
        String phoneNumber = getTestPhoneNumber(searchResult.getPhoneNumbers().get(0));
        PollResponse<PhoneNumberOperation> purchaseOperationResponse = beginPurchasePhoneNumbersHelper(httpClient, searchResult.getSearchId(), "beginPurchasePhoneNumbersWithoutContextSync", false).waitForCompletion();
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, purchaseOperationResponse.getStatus());
        PollResponse<PhoneNumberOperation> releaseOperationResponse = beginReleasePhoneNumberHelper(httpClient, phoneNumber, "beginReleasePhoneNumberWithoutContextSync", false).waitForCompletion();
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, releaseOperationResponse.getStatus());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)")
    public void beginPurchaseandReleasePhoneNumbers(HttpClient httpClient) {
        PhoneNumberSearchResult searchResult = beginSearchAvailablePhoneNumbersHelper(httpClient, "beginPurchaseandReleasePhoneNumbers_beginSearchAvailablePhoneNumbersSync", true).getFinalResult();
        String phoneNumber = getTestPhoneNumber(searchResult.getPhoneNumbers().get(0));
        PollResponse<PhoneNumberOperation> purchaseOperationResponse = beginPurchasePhoneNumbersHelper(httpClient, searchResult.getSearchId(), "beginPurchasePhoneNumbersSync", true).waitForCompletion();
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, purchaseOperationResponse.getStatus());
        PollResponse<PhoneNumberOperation> releaseOperationResponse = beginReleasePhoneNumberHelper(httpClient, phoneNumber, "beginReleasePhoneNumberSync", true).waitForCompletion();
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, releaseOperationResponse.getStatus());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void beginUpdatePhoneNumberCapabilitiesWithoutContext(HttpClient httpClient) {
        String phoneNumber = getTestPhoneNumber(PHONE_NUMBER);
        PurchasedPhoneNumber acquiredPhoneNumber = beginUpdatePhoneNumberCapabilitiesHelper(httpClient, phoneNumber, "beginUpdatePhoneNumberCapabilitiesWithoutContextSync", false).getFinalResult();
        assertEquals(PhoneNumberCapabilityType.INBOUND_OUTBOUND, acquiredPhoneNumber.getCapabilities().getSms());
        assertEquals(PhoneNumberCapabilityType.INBOUND, acquiredPhoneNumber.getCapabilities().getCalling());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void beginUpdatePhoneNumberCapabilities(HttpClient httpClient) {
        String phoneNumber = getTestPhoneNumber(PHONE_NUMBER);
        PurchasedPhoneNumber acquiredPhoneNumber = beginUpdatePhoneNumberCapabilitiesHelper(httpClient, phoneNumber, "beginUpdatePhoneNumberCapabilitiesSync", true).getFinalResult();
        assertEquals(PhoneNumberCapabilityType.INBOUND_OUTBOUND, acquiredPhoneNumber.getCapabilities().getSms());
        assertEquals(PhoneNumberCapabilityType.INBOUND, acquiredPhoneNumber.getCapabilities().getCalling());
    }

    private SyncPoller<PhoneNumberOperation, PhoneNumberSearchResult> beginSearchAvailablePhoneNumbersHelper(HttpClient httpClient, String testName, boolean withContext) {
        PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities();
        capabilities.setCalling(PhoneNumberCapabilityType.INBOUND);
        capabilities.setSms(PhoneNumberCapabilityType.INBOUND_OUTBOUND);
        PhoneNumberSearchOptions searchOptions = new PhoneNumberSearchOptions().setQuantity(1);

        if (withContext) {
            return setPollInterval(getClientWithConnectionString(httpClient, testName).beginSearchAvailablePhoneNumbers(
                COUNTRY_CODE,
                PhoneNumberType.TOLL_FREE,
                PhoneNumberAssignmentType.APPLICATION,
                capabilities,
                searchOptions,
                Context.NONE));
        }
        return setPollInterval(getClientWithConnectionString(httpClient, testName).beginSearchAvailablePhoneNumbers(
            COUNTRY_CODE,
            PhoneNumberType.TOLL_FREE,
            PhoneNumberAssignmentType.APPLICATION,
            capabilities));
    }

    private SyncPoller<PhoneNumberOperation, PurchasePhoneNumbersResult> beginPurchasePhoneNumbersHelper(HttpClient httpClient, String searchId, String testName, boolean withContext) {
        if (withContext) {
            return setPollInterval(this.getClientWithConnectionString(httpClient, testName)
            .beginPurchasePhoneNumbers(searchId, Context.NONE));
        }
        return setPollInterval(this.getClientWithConnectionString(httpClient, testName)
        .beginPurchasePhoneNumbers(searchId));
    }

    private SyncPoller<PhoneNumberOperation, ReleasePhoneNumberResult> beginReleasePhoneNumberHelper(HttpClient httpClient, String phoneNumber, String testName, boolean withContext) {
        if (getTestMode() == TestMode.PLAYBACK) {
            phoneNumber = "+REDACTED";
        }
        if (withContext) {
            return setPollInterval(this.getClientWithConnectionString(httpClient, testName)
            .beginReleasePhoneNumber(phoneNumber, Context.NONE));
        }
        return setPollInterval(this.getClientWithConnectionString(httpClient, testName)
        .beginReleasePhoneNumber(phoneNumber));
    }

    private SyncPoller<PhoneNumberOperation, PurchasedPhoneNumber>
        beginUpdatePhoneNumberCapabilitiesHelper(HttpClient httpClient, String phoneNumber, String testName, boolean withContext) {
        PhoneNumberCapabilities capabilities = new PhoneNumberCapabilities();
        capabilities.setCalling(PhoneNumberCapabilityType.INBOUND);
        capabilities.setSms(PhoneNumberCapabilityType.INBOUND_OUTBOUND);
        if (withContext) {
            return setPollInterval(this.getClientWithConnectionString(httpClient, testName)
            .beginUpdatePhoneNumberCapabilities(phoneNumber, capabilities, Context.NONE));
        }
        return setPollInterval(this.getClientWithConnectionString(httpClient, testName)
            .beginUpdatePhoneNumberCapabilities(phoneNumber, capabilities));
    }

    private <T, U> SyncPoller<T, U> setPollInterval(SyncPoller<T, U> syncPoller) {
        return interceptorManager.isPlaybackMode()
            ? syncPoller.setPollInterval(Duration.ofMillis(1))
            : syncPoller.setPollInterval(Duration.ofSeconds(1));
    }

    private PhoneNumbersClient getClientWithConnectionString(HttpClient httpClient, String testName) {
        PhoneNumbersClientBuilder builder = super.getClientBuilderWithConnectionString(httpClient);
        return addLoggingPolicy(builder, testName).buildClient();
    }

    private PhoneNumbersClient getClientWithManagedIdentity(HttpClient httpClient, String testName) {
        PhoneNumbersClientBuilder builder = super.getClientBuilderUsingManagedIdentity(httpClient);
        return addLoggingPolicy(builder, testName).buildClient();
    }

    private String getTestPhoneNumber(String phoneNumber) {
        if (getTestMode() == TestMode.PLAYBACK) {
            phoneNumber = "+REDACTED";
        }
        return phoneNumber;
    }
}
