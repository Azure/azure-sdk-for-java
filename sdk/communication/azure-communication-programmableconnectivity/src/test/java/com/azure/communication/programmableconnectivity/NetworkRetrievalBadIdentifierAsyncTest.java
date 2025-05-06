// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.programmableconnectivity;

import com.azure.communication.programmableconnectivity.models.NetworkIdentifier;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.test.TestMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

/**
 * Async test for DeviceNetworkAsyncClient with invalid network identifier type.
 */
public final class NetworkRetrievalBadIdentifierAsyncTest extends ProgrammableConnectivityClientTestBase {

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    /**
     * Test retrieving network information with an invalid network identifier type (IPv5) asynchronously.
     * This test verifies that the API correctly returns a 400 Bad Request error.
     */
    @Test
    public void testNetworkRetrievalBadIdentifierAsync() {
        System.out.println("Starting Network Retrieval Bad Identifier Async test...");

        String gatewayId
            = "/subscriptions/28269522-1d13-498d-92e9-23c999c3c997/resourceGroups/gteixeira-orange-testing2/providers/Private.programmableconnectivity/gateways/gateway-uksouth-2505201109";

        // Create an invalid network identifier
        NetworkIdentifier networkId = new NetworkIdentifier("IPv5", "127.0.0.1");

        System.out.println("Request parameters:");
        System.out.println("- Gateway ID: " + gatewayId);
        System.out.println("- Network Type (invalid): " + networkId.getIdentifierType());
        System.out.println("- Network Value: " + networkId.getIdentifier());

        StepVerifier.create(deviceNetworkAsyncClient.retrieve(gatewayId, networkId)).expectErrorSatisfies(error -> {
            System.out.println("Caught expected exception: " + error.getMessage());
            System.out.println("Exception type: " + error.getClass().getName());

            Assertions.assertTrue(error instanceof HttpResponseException,
                "Expected HttpResponseException but got " + error.getClass().getName());

            HttpResponseException ex = (HttpResponseException) error;
            System.out.println("Status code: " + ex.getResponse().getStatusCode());

            Assertions.assertEquals(400, ex.getResponse().getStatusCode(),
                "Expected status code 400 for invalid network identifier type");

            String errorMessage = ex.getMessage();
            System.out.println("Error message: " + errorMessage);

            Assertions.assertTrue(errorMessage.contains("IPv5")
                || errorMessage.contains("identifier")
                || errorMessage.contains("validation"), "Error message should mention the invalid identifier type");
        }).verify();

        if (getTestMode() == TestMode.RECORD) {
            String recordingFilePath = "src/test/resources/session-records/" + this.getClass().getSimpleName() + "."
                + Thread.currentThread().getStackTrace()[1].getMethodName() + ".json";
        }

        System.out.println("Test completed successfully.");
    }
}
