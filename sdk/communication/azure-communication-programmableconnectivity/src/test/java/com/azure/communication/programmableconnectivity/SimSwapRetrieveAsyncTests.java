// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.programmableconnectivity;

import com.azure.communication.programmableconnectivity.models.NetworkIdentifier;
import com.azure.communication.programmableconnectivity.models.SimSwapRetrievalContent;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public final class SimSwapRetrieveAsyncTests extends ProgrammableConnectivityClientTestBase {

    @Override
    protected void beforeTest() {
        super.beforeTest();

        if (!interceptorManager.isLiveMode()) {
            interceptorManager.addSanitizers(Arrays.asList(
                new TestProxySanitizer("10000100", "sanitized-phone-number", TestProxySanitizerType.BODY_REGEX)));
        }
    }

    /**
     * This test verifies that the API returns a valid date for a SIM swap.
     */
    @Test
    public void testSimSwapRetrieveAsync() {
        System.out.println("Starting SIM swap async retrieval test...");

        String gatewayId
            = "/subscriptions/28269522-1d13-498d-92e9-23c999c3c997/resourceGroups/gteixeira-orange-testing2/providers/Private.programmableconnectivity/gateways/gateway-uksouth-2505201109";
        NetworkIdentifier networkId = new NetworkIdentifier("NetworkCode", "E2E_Test_Operator_Contoso");
        String phoneNumber = "10000100";

        SimSwapRetrievalContent content = new SimSwapRetrievalContent(networkId).setPhoneNumber(phoneNumber);

        StepVerifier.create(simSwapAsyncClient.retrieve(gatewayId, content)).assertNext(response -> {
            Assertions.assertNotNull(response, "Response should not be null");
            OffsetDateTime swapDate = response.getDate();
            System.out.println("\nResponse details:");

            if (swapDate != null) {
                String formattedDate = swapDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                System.out.println("- SIM swap date: " + formattedDate);

                OffsetDateTime now = OffsetDateTime.now();
                long daysSinceSwap = java.time.Duration.between(swapDate, now).toDays();
                System.out.println("- Days since swap: " + daysSinceSwap);

                Assertions.assertTrue(swapDate.isBefore(OffsetDateTime.now()), "SIM swap date should be in the past");
            } else {
                System.out.println("- No SIM swap date available");
            }
        }).verifyComplete();

        System.out.println("Async test completed successfully.");
    }
}
