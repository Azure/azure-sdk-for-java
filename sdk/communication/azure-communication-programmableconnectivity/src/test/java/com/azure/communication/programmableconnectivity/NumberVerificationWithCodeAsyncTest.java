// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.programmableconnectivity;

import com.azure.communication.programmableconnectivity.models.NetworkIdentifier;
import com.azure.communication.programmableconnectivity.models.NumberVerificationWithCodeContent;
import com.azure.communication.programmableconnectivity.models.NumberVerificationWithoutCodeContent;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.test.TestMode;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Async tests for the full number verification flow.
 * This represents a complete verification workflow:
 * 1. Initial request without code (redirects to operator auth) - Using SDK async API
 * 2. User authentication at operator endpoint (simulated)
 * 3. Auth callback with operator code (redirects to developer backend) - Using direct HTTP call
 * 4. Final verification with APC code - Using SDK async API
 */
public final class NumberVerificationWithCodeAsyncTest extends ProgrammableConnectivityClientTestBase {

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    /**
     * Tests the complete number verification flow asynchronously, including:
     * 1. Initial request (verifyWithoutCode) - Using SDK async API
     * 2. Processing the redirect to operator auth
     * 3. Handling the auth callback with operator code - Using direct HTTP call
     * 4. Final verification with APC code (verifyWithCode) - Using SDK async API
     */
    @Test
    public void testFullNumberVerificationFlowAsync() throws IOException {
        System.out.println("Starting Full Number Verification Flow Async test...");

        String gatewayId
            = "/subscriptions/28269522-1d13-498d-92e9-23c999c3c997/resourceGroups/gteixeira-orange-testing2/providers/Private.programmableconnectivity/gateways/gateway-uksouth-2505201109";
        NetworkIdentifier networkId = new NetworkIdentifier("NetworkCode", "E2E_Test_Operator_Contoso");
        String phoneNumber = "10000100";

        // Developer backend redirect URI - where user should be redirected after auth
        String redirectUri = "https://contoso.invalid/";

        // STEP 1: Initial request to verify without code 
        System.out.println("\nSTEP 1: Making initial verification request (verifyWithoutCode)");

        NumberVerificationWithoutCodeContent initialContent
            = new NumberVerificationWithoutCodeContent(networkId, redirectUri).setPhoneNumber(phoneNumber);

        RequestOptions requestOptions = new RequestOptions();

        Mono<Response<Void>> initialResponseMono = numberVerificationAsyncClient
            .verifyWithoutCodeWithResponse(gatewayId, BinaryData.fromObject(initialContent), requestOptions);

        final String[] redirectToOperatorAuth = { null }; // Use array to capture value in lambda
        final String[] state = { null };
        final String[] redirectUriFromUrl = { null };

        StepVerifier.create(initialResponseMono).assertNext(initialResponse -> {
            System.out.println("Initial response status code: " + initialResponse.getStatusCode());
            Assertions.assertEquals(302, initialResponse.getStatusCode(),
                "Expected a 302 redirect status code for initial verification");

            // Extract and validate the location header (redirect to operator auth)
            redirectToOperatorAuth[0] = initialResponse.getHeaders().getValue(HttpHeaderName.LOCATION);
            System.out.println("Redirect to operator auth: " + redirectToOperatorAuth[0]);

            Assertions.assertNotNull(redirectToOperatorAuth[0], "Redirect URL should not be null");
            Assertions.assertFalse(redirectToOperatorAuth[0].isEmpty(), "Redirect URL should not be empty");

            // STEP 2: Extract parameters from the redirect URL
            System.out.println("\nSTEP 2: Extracting parameters from redirect URL");

            state[0] = extractParameterFromUrl(redirectToOperatorAuth[0], "state");
            redirectUriFromUrl[0] = extractParameterFromUrl(redirectToOperatorAuth[0], "redirect_uri");

            System.out.println("Extracted parameters:");
            System.out.println("- state: " + state[0]);
            System.out.println("- redirect_uri: " + redirectUriFromUrl[0]);
        }).verifyComplete();

        // STEP 3: In a real scenario, the user would authenticate with the operator
        // and the operator would redirect back to the APC callback URL with an operator code
        System.out.println("\nSTEP 3: Simulating operator authentication and calling authcallback");

        // Operator code expected by the mock operator (hardcoded for test purposes)
        String operatorCode = "hardcodedOperatorCode";

        // Extract the base URL for the authcallback
        // In a real scenario, you would use the redirectUriFromUrl as the base
        // For this test, we'll use a constructed URL based on the endpoint
        String baseEndpoint = extractBaseEndpoint(numberVerificationClient);
        String authCallbackUrl = baseEndpoint + "/authcallback";

        // Make direct HTTP request to the authcallback endpoint
        String apcCode = null;

        if (getTestMode() == TestMode.PLAYBACK) {
            apcCode = "apc_cfd3dc6a5bd441cb90cf399c3f228655";
            System.out.println("In playback mode, using predefined APC code: " + apcCode);
        } else {
            // In RECORD or LIVE mode, make an actual HTTP request to the authcallback endpoint
            try {
                String callbackResponse = callAuthCallback(authCallbackUrl, operatorCode, state[0]);
                System.out.println("Auth callback response: " + callbackResponse);

                if (callbackResponse != null) {
                    // The callback should redirect to the developer backend with the APC code in the query string
                    // Example: https://contoso.invalid/?apcCode=abc123&correlationId=xyz
                    apcCode = extractParameterFromUrl(callbackResponse, "apcCode");
                    System.out.println("Extracted APC code from callback response: " + apcCode);

                    if (apcCode == null || apcCode.isEmpty()) {
                        throw new IllegalStateException(
                            "Failed to extract valid APC code from callback response: " + callbackResponse);
                    }
                } else {
                    throw new IllegalStateException(
                        "Auth callback response was null - unable to proceed with verification");
                }
            } catch (Exception ex) {
                System.out.println("Error calling auth callback: " + ex.getMessage());
            }
        }

        System.out.println("Using APC code: " + apcCode);

        // STEP 4: Final verification with the APC code - Using SDK async API
        System.out.println("\nSTEP 4: Making final verification request with APC code (verifyWithCode)");

        final String finalApcCode = apcCode;
        NumberVerificationWithCodeContent finalContent = new NumberVerificationWithCodeContent(finalApcCode);

        StepVerifier.create(numberVerificationAsyncClient.verifyWithCode(gatewayId, finalContent))
            .assertNext(result -> {
                System.out.println("Final verification result: " + result.isVerificationResult());
                Assertions.assertTrue(result.isVerificationResult(), "Expected a successful verification result");
            })
            .verifyComplete();

        if (getTestMode() == TestMode.RECORD) {
            String recordingFilePath = "src/test/resources/session-records/" + this.getClass().getSimpleName() + "."
                + Thread.currentThread().getStackTrace()[1].getMethodName() + ".json";
        }

        System.out.println("Number verification flow async test completed successfully.");
    }

    /**
     * Makes a direct HTTP call to the auth callback endpoint.
     * 
     * @param authCallbackUrl The URL of the auth callback endpoint
     * @param operatorCode The operator code to include in the request
     * @param state The state parameter from the initial redirect
     * @return The response from the auth callback endpoint
     * @throws IOException If an I/O error occurs
     */
    private String callAuthCallback(String authCallbackUrl, String operatorCode, String state) throws IOException {
        // Construct the full URL with query parameters
        String encodedCode = URLEncoder.encode(operatorCode, StandardCharsets.UTF_8.toString());
        String encodedState = URLEncoder.encode(state, StandardCharsets.UTF_8.toString());
        String fullUrl = authCallbackUrl + "?code=" + encodedCode + "&state=" + encodedState;

        URL url = new URL(fullUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        System.out.println("Auth callback response code: " + responseCode);

        String location = connection.getHeaderField("Location");
        System.out.println("Auth callback redirect location: " + location);

        connection.disconnect();

        return location;
    }

    /**
     * Extracts a parameter value from a URL.
     * 
     * @param url The URL containing the parameter
     * @param parameterName The name of the parameter to extract
     * @return The value of the parameter, or null if not found
     */
    private String extractParameterFromUrl(String url, String parameterName) {
        Pattern pattern = Pattern.compile(parameterName + "=([^&]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Extracts the base endpoint from a client for constructing the auth callback URL.
     * 
     * @param client The client to extract the endpoint from
     * @return The base endpoint URL
     */
    private String extractBaseEndpoint(Object client) {
        // In a real implementation, you would extract this from the client's configuration
        // For this test, we'll use a hardcoded value that matches your environment
        return "https://uksouth.test.apcgatewayapi.azure.com";
    }
}
