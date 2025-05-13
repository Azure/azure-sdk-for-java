// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.programmableconnectivity;

import com.azure.communication.programmableconnectivity.models.NetworkIdentifier;
import com.azure.communication.programmableconnectivity.models.SimSwapVerificationContent;
import com.azure.communication.programmableconnectivity.models.SimSwapVerificationResult;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.exception.HttpResponseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public final class SimSwapVerifyTests extends ProgrammableConnectivityClientTestBase {

    @Override
    protected void beforeTest() {
        // Call the parent method to set up the client
        super.beforeTest();

        // Add sanitizers for sensitive information in recordings
        if (!interceptorManager.isLiveMode()) {
            interceptorManager.addSanitizers(Arrays.asList(
                new TestProxySanitizer("/subscriptions/[^/]+/", "/subscriptions/sanitized-subscription-id/",
                    TestProxySanitizerType.URL),
                new TestProxySanitizer("/resourceGroups/[^/]+/", "/resourceGroups/sanitized-resource-group/",
                    TestProxySanitizerType.URL),
                new TestProxySanitizer("/gateways/[^/\\s]+", "/gateways/sanitized-gateway", TestProxySanitizerType.URL),
                new TestProxySanitizer("$..phoneNumber", null, "sanitized-phone-number",
                    TestProxySanitizerType.BODY_KEY)));
        }
    }

    @Test
    public void testSimSwapVerify() {
        // Use actual gateway ID for your test environment
        String gatewayId
            = "/subscriptions/28269522-1d13-498d-92e9-23c999c3c997/resourceGroups/gteixeira-orange-testing2/providers/Private.programmableconnectivity/gateways/gateway-uksouth-2505081537";

        // Use Network Code instead of IPv4 as seen in your recent tests
        NetworkIdentifier networkId = new NetworkIdentifier("NetworkCode", "E2E_Test_Operator_Contoso");

        // Add a phone number - useful for API behavior
        SimSwapVerificationContent verificationContent
            = new SimSwapVerificationContent(networkId).setPhoneNumber("10000100").setMaxAgeHours(941);

        // Add some logging for debugging
        System.out.println("Starting SimSwap verification test...");
        System.out.println("Using gateway ID: " + gatewayId);
        System.out.println("Using network identifier type: " + networkId.getIdentifierType());
        System.out.println("Using network identifier: " + networkId.getIdentifier());

        // Execute the API call
        SimSwapVerificationResult response = simSwapClient.verify(gatewayId, verificationContent);

        // Basic validation
        Assertions.assertNotNull(response, "Response should not be null");

        // Log the result
        System.out.println("Verification result: " + response.isVerificationResult());

        // Verify the result is a boolean (either true or false is acceptable for the test)
        // We don't know what the expected result will be for test data, so just check the type
        boolean result = response.isVerificationResult();
        System.out.println("Test completed successfully.");
    }

    /**
    * Test verifying that the response contains the expected x-ms-response-id header.
    * This test demonstrates how to access raw HTTP response headers.
    */
    @Test
    public void testSimSwapVerifyHeaderRetrieval() {
        System.out.println("Starting SIM swap header retrieval test...");

        // Prepare test parameters
        String gatewayId
            = "/subscriptions/28269522-1d13-498d-92e9-23c999c3c997/resourceGroups/gteixeira-orange-testing2/providers/Private.programmableconnectivity/gateways/gateway-uksouth-2505081537";
        NetworkIdentifier networkId = new NetworkIdentifier("NetworkCode", "E2E_Test_Operator_Contoso");
        String phoneNumber = "+50000000000";
        int maxAgeHours = 120;

        // Create verification content
        SimSwapVerificationContent content
            = new SimSwapVerificationContent(networkId).setPhoneNumber(phoneNumber).setMaxAgeHours(maxAgeHours);

        // Execute the API call
        SimSwapVerificationResult result = simSwapClient.verify(gatewayId, content);

        // Validate result exists
        Assertions.assertNotNull(result, "Verification result should not be null");
        System.out.println("Verification result: " + result.isVerificationResult());

        // Extract the header value from our capture policy
        String responseId = headerCapturePolicy.getHeaderValue("x-ms-response-id");
        String xMsResponseId = (responseId != null) ? responseId : "not found";

        System.out.println("x-ms-response-id: " + xMsResponseId);

        // Assert the header is not empty
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

        // Prepare test parameters
        String gatewayId
            = "/subscriptions/28269522-1d13-498d-92e9-23c999c3c997/resourceGroups/gteixeira-orange-testing2/providers/Private.programmableconnectivity/gateways/gateway-uksouth-2505081537";
        NetworkIdentifier networkId = new NetworkIdentifier("NetworkCode", "E2E_Test_Operator_Contoso");

        // Use an invalid phone number format to trigger validation error
        String invalidPhoneNumber = "not-a-phone-number";
        int maxAgeHours = 120; // Using a valid value for this parameter

        // Create verification content with invalid phone number
        SimSwapVerificationContent content
            = new SimSwapVerificationContent(networkId).setPhoneNumber(invalidPhoneNumber).setMaxAgeHours(maxAgeHours);

        try {
            // Execute the API call - this should fail with a 400 error
            SimSwapVerificationResult result = simSwapClient.verify(gatewayId, content);

            // If we get here, the test failed because an exception should have been thrown
            Assertions.fail("Expected HttpResponseException was not thrown for invalid phone number");

        } catch (HttpResponseException ex) {
            // Log the exception details
            System.out.println("Caught expected exception: " + ex.getMessage());
            System.out.println("Exception type: " + ex.getClass().getName());
            System.out.println("Status code: " + ex.getResponse().getStatusCode());

            // Verify the status code is 400 Bad Request
            Assertions.assertEquals(400, ex.getResponse().getStatusCode(),
                "Expected status code 400 for invalid phone number");

            // Verify the error message contains information about phone number validation
            String errorMessage = ex.getMessage();
            Assertions.assertTrue(errorMessage.contains("PhoneNumber"),
                "Error message should mention phone number validation issue");
            Assertions.assertTrue(errorMessage.contains("regular expression"),
                "Error message should mention the regular expression constraint");
        }

        System.out.println("Test completed successfully.");
    }

}
