// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.programmableconnectivity;

import com.azure.communication.programmableconnectivity.models.NetworkIdentifier;
import com.azure.communication.programmableconnectivity.models.SimSwapVerificationContent;
import com.azure.communication.programmableconnectivity.models.SimSwapVerificationResult;
import com.azure.core.exception.HttpResponseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.azure.core.test.TestMode;

public final class SimSwapVerifyTests extends ProgrammableConnectivityClientTestBase {

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    @Test
    public void testSimSwapVerify() {
        String gatewayId;
        if (getTestMode() == TestMode.PLAYBACK) {
            gatewayId = "sanitized-gateway-id";
        } else {
            gatewayId
                = "/subscriptions/28269522-1d13-498d-92e9-23c999c3c997/resourceGroups/gteixeira-orange-testing2/providers/Private.programmableconnectivity/gateways/gateway-uksouth-2505201109";
        }

        NetworkIdentifier networkId = new NetworkIdentifier("NetworkCode", "E2E_Test_Operator_Contoso");

        SimSwapVerificationContent verificationContent
            = new SimSwapVerificationContent(networkId).setPhoneNumber("10000100").setMaxAgeHours(941);

        System.out.println("Starting SimSwap verification test...");
        System.out.println("Using gateway ID: " + gatewayId);
        System.out.println("Using network identifier type: " + networkId.getIdentifierType());
        System.out.println("Using network identifier: " + networkId.getIdentifier());

        SimSwapVerificationResult response = simSwapClient.verify(gatewayId, verificationContent);

        Assertions.assertNotNull(response, "Response should not be null");

        System.out.println("Verification result: " + response.isVerificationResult());

        boolean result = response.isVerificationResult();
        System.out.println("Test completed successfully.");
    }

    /**
    * Test verifying that the response contains the expected x-ms-response-id header.
    */
    @Test
    public void testSimSwapVerifyHeaderRetrieval() {
        System.out.println("Starting SIM swap header retrieval test...");

        String gatewayId
            = "/subscriptions/28269522-1d13-498d-92e9-23c999c3c997/resourceGroups/gteixeira-orange-testing2/providers/Private.programmableconnectivity/gateways/gateway-uksouth-2505201109";
        NetworkIdentifier networkId = new NetworkIdentifier("NetworkCode", "E2E_Test_Operator_Contoso");
        String phoneNumber = "+50000000000";
        int maxAgeHours = 120;

        SimSwapVerificationContent content
            = new SimSwapVerificationContent(networkId).setPhoneNumber(phoneNumber).setMaxAgeHours(maxAgeHours);

        SimSwapVerificationResult result = simSwapClient.verify(gatewayId, content);

        Assertions.assertNotNull(result, "Verification result should not be null");
        System.out.println("Verification result: " + result.isVerificationResult());

        String responseId = headerCapturePolicy.getHeaderValue("x-ms-response-id");
        String xMsResponseId = (responseId != null) ? responseId : "not found";

        System.out.println("x-ms-response-id: " + xMsResponseId);

        Assertions.assertNotNull(responseId, "x-ms-response-id header should be present");
        Assertions.assertFalse(responseId.isEmpty(), "x-ms-response-id header should not be empty");

        System.out.println("Test completed successfully.");
    }

    /**
     * Test verifying that the API correctly handles invalid input parameters.
     * Specifically, this tests that an invalid phone number format will trigger
     * a 400 Bad Request error with appropriate validation details.
     */
    @Test
    public void testSimSwapVerifyBadResponse() {
        System.out.println("Starting SimSwap bad response test...");

        String gatewayId
            = "/subscriptions/28269522-1d13-498d-92e9-23c999c3c997/resourceGroups/gteixeira-orange-testing2/providers/Private.programmableconnectivity/gateways/gateway-uksouth-2505201109";
        NetworkIdentifier networkId = new NetworkIdentifier("NetworkCode", "E2E_Test_Operator_Contoso");

        String invalidPhoneNumber = "not-a-phone-number";
        int maxAgeHours = 120;

        SimSwapVerificationContent content
            = new SimSwapVerificationContent(networkId).setPhoneNumber(invalidPhoneNumber).setMaxAgeHours(maxAgeHours);

        try {
            // Execute the API call - this should fail with a 400 error
            SimSwapVerificationResult result = simSwapClient.verify(gatewayId, content);

            Assertions.fail("Expected HttpResponseException was not thrown for invalid phone number");

        } catch (HttpResponseException ex) {
            System.out.println("Caught expected exception: " + ex.getMessage());
            System.out.println("Exception type: " + ex.getClass().getName());
            System.out.println("Status code: " + ex.getResponse().getStatusCode());

            Assertions.assertEquals(400, ex.getResponse().getStatusCode(),
                "Expected status code 400 for invalid phone number");

            String errorMessage = ex.getMessage();
            Assertions.assertTrue(errorMessage.contains("PhoneNumber"),
                "Error message should mention phone number validation issue");
            Assertions.assertTrue(errorMessage.contains("regular expression"),
                "Error message should mention the regular expression constraint");
        }

        System.out.println("Test completed successfully.");
    }

}
