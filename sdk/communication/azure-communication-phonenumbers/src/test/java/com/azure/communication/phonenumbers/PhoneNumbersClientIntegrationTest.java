// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.phonenumbers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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
import com.azure.communication.phonenumbers.models.PhoneNumberUpdateRequest;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;

public class PhoneNumbersClientIntegrationTest extends PhoneNumbersIntegrationTestBase {
    
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPhoneNumber(HttpClient httpClient) {
        AcquiredPhoneNumber number = this.getClientWithConnectionString(httpClient, "getPhoneNumber").getPhoneNumber(PHONE_NUMBER);
        assertEquals(PHONE_NUMBER, number.getPhoneNumber());
        assertEquals(COUNTRY_CODE, number.getCountryCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getPhoneNumberWithResponse(HttpClient httpClient) {
        Response<AcquiredPhoneNumber> response = this.getClientWithConnectionString(httpClient, "getPhoneNumberWithResponseSync")
            .getPhoneNumberWithResponse(PHONE_NUMBER, Context.NONE);
        AcquiredPhoneNumber number = response.getValue();
        assertEquals(200, response.getStatusCode());
        assertEquals(PHONE_NUMBER, number.getPhoneNumber());
        assertEquals(COUNTRY_CODE, number.getCountryCode());
    }
    
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listPhoneNumbers(HttpClient httpClient) {
        PagedIterable<AcquiredPhoneNumber> numbers = this.getClientWithConnectionString(httpClient, "listPhoneNumbersSync").listPhoneNumbers(Context.NONE);
        AcquiredPhoneNumber number = numbers.iterator().next();
        assertNotNull(number.getPhoneNumber());
        assertEquals(COUNTRY_CODE, number.getCountryCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updatePhoneNumber(HttpClient httpClient) {
        PhoneNumberUpdateRequest request = new PhoneNumberUpdateRequest();
        request.setApplicationId("testApplicationId");
        request.setCallbackUri("testCallbackUri");
        AcquiredPhoneNumber number = this.getClientWithConnectionString(httpClient, "updatePhoneNumberSync").updatePhoneNumber(PHONE_NUMBER, request);
        assertEquals(PHONE_NUMBER, number.getPhoneNumber());
        assertEquals(COUNTRY_CODE, number.getCountryCode());
        assertEquals("testCallbackUri", number.getCallbackUri());
        assertNotNull(number.getApplicationId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updatePhoneNumberWithResponse(HttpClient httpClient) {
        PhoneNumberUpdateRequest request = new PhoneNumberUpdateRequest();
        request.setApplicationId("testApplicationId");
        request.setCallbackUri("testCallbackUri");
        Response<AcquiredPhoneNumber> response = 
            this.getClientWithConnectionString(httpClient, "updatePhoneNumberWithResponseSync").updatePhoneNumberWithResponse(PHONE_NUMBER, request, Context.NONE);
        AcquiredPhoneNumber number = response.getValue();
        assertEquals(200, response.getStatusCode());
        assertEquals(PHONE_NUMBER, number.getPhoneNumber());
        assertEquals(COUNTRY_CODE, number.getCountryCode());
        assertEquals("testCallbackUri", number.getCallbackUri());
        assertNotNull(number.getApplicationId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void beginSearchAvailablePhoneNumbers(HttpClient httpClient) {
        PhoneNumberSearchResult searchResult = beginSearchAvailablePhoneNumbersHelper(httpClient, "beginSearchAvailablePhoneNumbers").getFinalResult();
        assertEquals(searchResult.getPhoneNumbers().size(), 1);
        assertNotNull(searchResult.getSearchId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    // @DisabledIfEnvironmentVariable(
    //     named = "SKIP_LIVE_TEST",
    //     matches = "(?i)(true)")
    public void beginPurchaseandReleasePhoneNumbers(HttpClient httpClient) {
        PhoneNumberSearchResult searchResult = beginSearchAvailablePhoneNumbersHelper(httpClient, "beginPurchaseandReleasePhoneNumbers_beginSearchAvailablePhoneNumbersSync").getFinalResult();
        String phoneNumber = searchResult.getPhoneNumbers().get(0);
        PollResponse<PhoneNumberOperation> purchaseOperationResponse = beginPurchasePhoneNumbersHelper(httpClient, searchResult.getSearchId(), "beginPurchasePhoneNumbersSync").waitForCompletion();
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, purchaseOperationResponse.getStatus());
        // TODO (minnieliu): Check if we can get the number as acquired.
        PollResponse<PhoneNumberOperation> releaseOperationResponse = beginReleasePhoneNumberHelper(httpClient, phoneNumber, "beginReleasePhoneNumberSunc").waitForCompletion();
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, releaseOperationResponse.getStatus());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void beginUpdatePhoneNumberCapabilities(HttpClient httpClient) {
        AcquiredPhoneNumber acquiredPhoneNumber = beginUpdatePhoneNumberCapabilitiesHelper(httpClient, PHONE_NUMBER, "beginUpdatePhoneNumberCapabilitiesSync").getFinalResult();
        assertEquals(PhoneNumberCapabilityValue.INBOUND_OUTBOUND, acquiredPhoneNumber.getCapabilities().getSms());
        assertEquals(PhoneNumberCapabilityValue.INBOUND, acquiredPhoneNumber.getCapabilities().getCalling());
    }
    
    private SyncPoller<PhoneNumberOperation, PhoneNumberSearchResult> beginSearchAvailablePhoneNumbersHelper(HttpClient httpClient, String testName) {
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
        return getClientWithConnectionString(httpClient, testName).beginSearchAvailablePhoneNumbers(COUNTRY_CODE, phoneNumberSearchRequest, Context.NONE);
    }

    private SyncPoller<PhoneNumberOperation, Void> beginPurchasePhoneNumbersHelper(HttpClient httpClient, String searchId, String testName) {
        return this.getClientWithConnectionString(httpClient, testName)
            .beginPurchasePhoneNumbers(searchId, Context.NONE).setPollInterval(Duration.ofSeconds(1));
    }

    private SyncPoller<PhoneNumberOperation, Void> beginReleasePhoneNumberHelper(HttpClient httpClient, String phoneNumber, String testName) {
        return this.getClientWithConnectionString(httpClient, testName)
            .beginReleasePhoneNumber(phoneNumber, Context.NONE).setPollInterval(Duration.ofSeconds(1));
    }

    private SyncPoller<PhoneNumberOperation, AcquiredPhoneNumber> beginUpdatePhoneNumberCapabilitiesHelper(HttpClient httpClient, String phoneNumber, String testName) {
        PhoneNumberCapabilitiesRequest capabilitiesUpdateRequest = new PhoneNumberCapabilitiesRequest();
        capabilitiesUpdateRequest.setCalling(PhoneNumberCapabilityValue.INBOUND);
        capabilitiesUpdateRequest.setSms(PhoneNumberCapabilityValue.INBOUND_OUTBOUND);
        return this.getClientWithConnectionString(httpClient, testName)
            .beginUpdatePhoneNumberCapabilities(phoneNumber, capabilitiesUpdateRequest, Context.NONE).setPollInterval(Duration.ofSeconds(1));
    }
    
    private PhoneNumbersClient getClientWithConnectionString(HttpClient httpClient, String testName) {
        PhoneNumbersClientBuilder builder = super.getClientBuilderWithConnectionString(httpClient);
        return addLoggingPolicy(builder, testName).buildClient();
    }
}
