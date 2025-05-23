// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.programmableconnectivity;

import com.azure.communication.programmableconnectivity.models.NetworkIdentifier;
import com.azure.core.test.TestMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

public final class NetworkRetrievalAsyncTest extends ProgrammableConnectivityClientTestBase {

    @Override
    protected void beforeTest() {
        super.beforeTest();

    }

    /**
     * This test verifies that the API correctly returns network details for the IP address.
     */
    @Test
    public void testNetworkRetrievalAsync() {
        System.out.println("Starting Network Retrieval Async test...");

        String gatewayId
            = "/subscriptions/28269522-1d13-498d-92e9-23c999c3c997/resourceGroups/gteixeira-orange-testing2/providers/Private.programmableconnectivity/gateways/gateway-uksouth-2505201109";

        NetworkIdentifier networkId = new NetworkIdentifier("IPv4", "10.0.0.1");

        System.out.println("Request parameters:");
        System.out.println("- Gateway ID: " + gatewayId);
        System.out.println("- Network Type: " + networkId.getIdentifierType());
        System.out.println("- Network Value: " + networkId.getIdentifier());

        StepVerifier.create(deviceNetworkAsyncClient.retrieve(gatewayId, networkId)).assertNext(result -> {
            System.out.println("Network code: " + result.getNetworkCode());

            Assertions.assertNotNull(result, "Network retrieval result should not be null");
            Assertions.assertNotNull(result.getNetworkCode(), "Network code should not be null");

            if (interceptorManager.isPlaybackMode()) {
                Assertions.assertEquals("E2E_Test_Operator_Contoso", result.getNetworkCode(),
                    "Expected network code to be E2E_Test_Operator_Contoso in playback mode");
            } else {
                System.out.println("Retrieved network code: " + result.getNetworkCode());
            }
        }).verifyComplete();

        if (getTestMode() == TestMode.RECORD) {
            String recordingFilePath = "src/test/resources/session-records/" + this.getClass().getSimpleName() + "."
                + Thread.currentThread().getStackTrace()[1].getMethodName() + ".json";
        }

        System.out.println("Test completed successfully.");
    }
}
