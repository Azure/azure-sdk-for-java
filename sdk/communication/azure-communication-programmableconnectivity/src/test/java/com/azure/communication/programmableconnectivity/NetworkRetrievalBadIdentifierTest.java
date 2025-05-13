// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.programmableconnectivity;

import com.azure.communication.programmableconnectivity.models.NetworkIdentifier;
import com.azure.communication.programmableconnectivity.models.NetworkRetrievalResult;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * Test for DeviceNetworkClient with invalid network identifier type.
 */
public final class NetworkRetrievalBadIdentifierTest extends ProgrammableConnectivityClientTestBase {

    @Override
    protected void beforeTest() {
        // Call the parent method to set up the client
        super.beforeTest();

        // Add sanitizers for sensitive information in recordings
        if (!interceptorManager.isLiveMode()) {
            interceptorManager.addSanitizers(Arrays.asList(
                new TestProxySanitizer("/subscriptions/[a-zA-Z0-9-]+/", "/subscriptions/sanitized-subscription-id/",
                    TestProxySanitizerType.URL),
                new TestProxySanitizer("/resourceGroups/[a-zA-Z0-9-]+/", "/resourceGroups/sanitized-resource-group/",
                    TestProxySanitizerType.URL),
                new TestProxySanitizer("/gateways/[a-zA-Z0-9-]+", "/gateways/sanitized-gateway",
                    TestProxySanitizerType.URL)));
        }
    }

    /**
     * Test retrieving network information with an invalid network identifier type (IPv5).
     * This test verifies that the API correctly returns a 400 Bad Request error.
     */
    @Test
    public void testNetworkRetrievalBadIdentifier() {
        System.out.println("Starting Network Retrieval Bad Identifier test...");

        // Prepare test parameters
        String gatewayId
            = "/subscriptions/28269522-1d13-498d-92e9-23c999c3c997/resourceGroups/gteixeira-orange-testing2/providers/Private.programmableconnectivity/gateways/gateway-uksouth-2505131009";

        // Create an invalid network identifier (IPv5 doesn't exist)
        NetworkIdentifier networkId = new NetworkIdentifier("IPv5", "127.0.0.1");

        System.out.println("Request parameters:");
        System.out.println("- Gateway ID: " + gatewayId);
        System.out.println("- Network Type (invalid): " + networkId.getIdentifierType());
        System.out.println("- Network Value: " + networkId.getIdentifier());

        try {
            // Execute the API call - this should fail with a 400 error
            NetworkRetrievalResult result = deviceNetworkClient.retrieve(gatewayId, networkId);

            // If we reach here, the test failed because an exception should have been thrown
            Assertions.fail("Expected HttpResponseException was not thrown for invalid network identifier type");

        } catch (HttpResponseException ex) {
            // Log the exception details for debugging
            System.out.println("Caught expected exception: " + ex.getMessage());
            System.out.println("Exception type: " + ex.getClass().getName());
            System.out.println("Status code: " + ex.getResponse().getStatusCode());

            // Verify the status code is 400 Bad Request
            Assertions.assertEquals(400, ex.getResponse().getStatusCode(),
                "Expected status code 400 for invalid network identifier type");

            // Additional assertions can be added to check specific error details
            String errorMessage = ex.getMessage();
            System.out.println("Error message: " + errorMessage);

            // Check if the error message contains relevant information about the invalid identifier
            Assertions.assertTrue(errorMessage.contains("IPv5")
                || errorMessage.contains("identifier")
                || errorMessage.contains("validation"), "Error message should mention the invalid identifier type");
        }

        System.out.println("Test completed successfully.");
    }
}
