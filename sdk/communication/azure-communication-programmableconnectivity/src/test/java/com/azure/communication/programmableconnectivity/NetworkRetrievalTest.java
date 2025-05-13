// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.programmableconnectivity;

import com.azure.communication.programmableconnectivity.models.NetworkIdentifier;
import com.azure.communication.programmableconnectivity.models.NetworkRetrievalResult;
import com.azure.core.test.TestMode;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * Test for DeviceNetworkClient to retrieve network information.
 */
public final class NetworkRetrievalTest extends ProgrammableConnectivityClientTestBase {

    @Override
    protected void beforeTest() {
        super.beforeTest();

    }

    /**
     * Test retrieving network information based on an IPv4 address.
     * This test verifies that the API correctly returns network details for the IP address.
     */
    @Test
    public void testNetworkRetrieval() {
        System.out.println("Starting Network Retrieval test...");

        String gatewayId
            = "/subscriptions/28269522-1d13-498d-92e9-23c999c3c997/resourceGroups/gteixeira-orange-testing2/providers/Private.programmableconnectivity/gateways/gateway-uksouth-2505091000";

        NetworkIdentifier networkId = new NetworkIdentifier("IPv4", "10.0.0.1");

        System.out.println("Request parameters:");
        System.out.println("- Gateway ID: " + gatewayId);
        System.out.println("- Network Type: " + networkId.getIdentifierType());
        System.out.println("- Network Value: " + networkId.getIdentifier());

        // Execute the API call
        NetworkRetrievalResult result = deviceNetworkClient.retrieve(gatewayId, networkId);

        // Validate response
        System.out.println("Network code: " + result.getNetworkCode());

        // Assert the network code matches expected value
        Assertions.assertNotNull(result, "Network retrieval result should not be null");
        Assertions.assertNotNull(result.getNetworkCode(), "Network code should not be null");

        if (interceptorManager.isPlaybackMode()) {
            // In playback mode, the recorded response should match the expected value
            Assertions.assertEquals("E2E_Test_Operator_Contoso", result.getNetworkCode(),
                "Expected network code to be E2E_Test_Operator_Contoso in playback mode");
        } else {
            // In live or record mode, just log the actual value
            System.out.println("Retrieved network code: " + result.getNetworkCode());
        }

        // Sanitize the recording file after test execution (only in RECORD mode)
        if (getTestMode() == TestMode.RECORD) {
            // The recording file path is based on test class and method name
            String recordingFilePath = "src/test/resources/session-records/" + this.getClass().getSimpleName() + "."
                + Thread.currentThread().getStackTrace()[1].getMethodName() + ".json";
            TestRecordingSanitizer.sanitizeRecording(recordingFilePath);
        }

        System.out.println("Test completed successfully.");
    }
}
