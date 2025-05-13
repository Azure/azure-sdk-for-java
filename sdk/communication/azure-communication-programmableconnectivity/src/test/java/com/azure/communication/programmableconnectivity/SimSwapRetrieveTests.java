// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.programmableconnectivity;

import com.azure.communication.programmableconnectivity.models.NetworkIdentifier;
import com.azure.communication.programmableconnectivity.models.SimSwapRetrievalContent;
import com.azure.communication.programmableconnectivity.models.SimSwapRetrievalResult;
import com.azure.communication.programmableconnectivity.models.SimSwapVerificationContent;
import com.azure.communication.programmableconnectivity.models.SimSwapVerificationResult;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.azure.core.util.Configuration;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public final class SimSwapRetrieveTests extends ProgrammableConnectivityClientTestBase {

    @Override
    protected void beforeTest() {
        // Call the parent method to set up the client - this includes base sanitizers
        super.beforeTest();

        if (!interceptorManager.isLiveMode()) {
            interceptorManager.addSanitizers(Arrays.asList(
                // Match the complete JSON property pattern
                new TestProxySanitizer("\"phoneNumber\":\\s*\"[^\"]*\"", "\"phoneNumber\": \"sanitized-phone-number\"",
                    TestProxySanitizerType.BODY_REGEX),
                // For IPv6 test
                new TestProxySanitizer("\"phoneNumber\":\\s*\"\\+[^\"]*\"",
                    "\"phoneNumber\": \"sanitized-phone-number\"", TestProxySanitizerType.BODY_REGEX)));
        }
    }

    /**
     * Test retrieving SIM swap information with standard parameters.
     * This test verifies that the API returns a valid date for a SIM swap.
     */
    @Test
    public void testSimSwapRetrieve() {
        System.out.println("Starting SIM swap retrieval test...");

        // Prepare test parameters
        String gatewayId
            = "/subscriptions/28269522-1d13-498d-92e9-23c999c3c997/resourceGroups/gteixeira-orange-testing2/providers/Private.programmableconnectivity/gateways/gateway-uksouth-2505121452";
        NetworkIdentifier networkId = new NetworkIdentifier("NetworkCode", "E2E_Test_Operator_Contoso");
        String phoneNumber = "10000100";

        // Create the request content
        SimSwapRetrievalContent content = new SimSwapRetrievalContent(networkId).setPhoneNumber(phoneNumber);

        // Execute the API call
        SimSwapRetrievalResult response = simSwapClient.retrieve(gatewayId, content);

        // Validate response
        Assertions.assertNotNull(response, "Response should not be null");

        // Log response details
        OffsetDateTime swapDate = response.getDate();
        System.out.println("\nResponse details:");

        if (swapDate != null) {
            String formattedDate = swapDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            System.out.println("- SIM swap date: " + formattedDate);

            // Calculate time elapsed
            OffsetDateTime now = OffsetDateTime.now();
            long daysSinceSwap = java.time.Duration.between(swapDate, now).toDays();
            System.out.println("- Days since swap: " + daysSinceSwap);

            // Additional assertions based on your specific requirements
            Assertions.assertTrue(swapDate.isBefore(OffsetDateTime.now()), "SIM swap date should be in the past");
        } else {
            System.out.println("- No SIM swap date available");
        }

        System.out.println("Test completed successfully.");
    }

    /**
     * Test retrieving SIM swap information with alternative network identifier.
     * This demonstrates how the API behaves with IPv6 identification.
     */
    @Test
    public void testSimSwapRetrieveWithIpv6() {
        System.out.println("Starting SIM swap retrieval test with IPv6...");

        // Original test parameters from the sample
        String gatewayId
            = "/subscriptions/28269522-1d13-498d-92e9-23c999c3c997/resourceGroups/gteixeira-orange-testing2/providers/Private.programmableconnectivity/gateways/gateway-uksouth-2505121452";
        NetworkIdentifier networkId = new NetworkIdentifier("IPv6", "2001:0db8:85a3:0000:0000:8a2e:0370:7334");
        String phoneNumber = "+61215310263792";

        // Log test parameters
        System.out.println("\nRequest parameters:");
        System.out.println("- Gateway ID: " + gatewayId);
        System.out.println("- Network Type: " + networkId.getIdentifierType());
        System.out.println("- Network Value: " + networkId.getIdentifier());
        System.out.println("- Phone Number: " + phoneNumber);

        // Create the request content
        SimSwapRetrievalContent content = new SimSwapRetrievalContent(networkId).setPhoneNumber(phoneNumber);

        // Execute the API call
        SimSwapRetrievalResult response = simSwapClient.retrieve(gatewayId, content);

        // Validate response
        Assertions.assertNotNull(response, "Response should not be null");

        // Log response details
        OffsetDateTime swapDate = response.getDate();
        if (swapDate != null) {
            System.out.println("\nResponse details:");
            System.out.println("- SIM swap date: " + swapDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        } else {
            System.out.println("\nNo SIM swap date found for this phone number.");
        }

        System.out.println("Test completed successfully.");
    }

}
