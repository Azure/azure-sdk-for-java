// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.programmableconnectivity;

import com.azure.communication.programmableconnectivity.models.NetworkIdentifier;
import com.azure.communication.programmableconnectivity.models.NumberVerificationResult;
import com.azure.communication.programmableconnectivity.models.NumberVerificationWithCodeContent;
import com.azure.communication.programmableconnectivity.models.NumberVerificationWithoutCodeContent;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.test.TestMode;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tests the full number verification flow using both verifyWithoutCode and verifyWithCode APIs.
 * This represents a complete verification workflow:
 * 1. Initial request without code (redirects to operator auth) - Using SDK
 * 2. User authentication at operator endpoint (simulated)
 * 3. Auth callback with operator code (redirects to developer backend) - Using direct HTTP call
 * 4. Final verification with APC code - Using SDK
 */
public final class NumberVerificationWithCodeTest extends ProgrammableConnectivityClientTestBase {

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
     * Tests the complete number verification flow, including:
     * 1. Initial request (verifyWithoutCode) - Using SDK
     * 2. Processing the redirect to operator auth
     * 3. Handling the auth callback with operator code - Using direct HTTP call
     * 4. Final verification with APC code (verifyWithCode) - Using SDK
     */
    @Test
    public void testFullNumberVerificationFlow() throws IOException {
        System.out.println("Starting Full Number Verification Flow test...");

        String gatewayId
            = "/subscriptions/28269522-1d13-498d-92e9-23c999c3c997/resourceGroups/gteixeira-orange-testing2/providers/Private.programmableconnectivity/gateways/gateway-uksouth-2505131009";
        NetworkIdentifier networkId = new NetworkIdentifier("NetworkCode", "E2E_Test_Operator_Contoso");
        String phoneNumber = "10000100";

        // Developer backend redirect URI - where user should be redirected after auth
        String redirectUri = "https://contoso.invalid/";

        // STEP 1: Initial request to verify without code 
        System.out.println("\nSTEP 1: Making initial verification request (verifyWithoutCode)");

        // Create verification content for the initial request
        NumberVerificationWithoutCodeContent initialContent
            = new NumberVerificationWithoutCodeContent(networkId, redirectUri).setPhoneNumber(phoneNumber);

        // Create request options that don't automatically follow redirects
        RequestOptions requestOptions = new RequestOptions();

        // Execute the initial API call
        Response<Void> initialResponse = numberVerificationClient.verifyWithoutCodeWithResponse(gatewayId,
            BinaryData.fromObject(initialContent), requestOptions);

        // Validate initial response
        System.out.println("Initial response status code: " + initialResponse.getStatusCode());
        Assertions.assertEquals(302, initialResponse.getStatusCode(),
            "Expected a 302 redirect status code for initial verification");

        // Extract and validate the location header (redirect to operator auth)
        String redirectToOperatorAuth = initialResponse.getHeaders().getValue(HttpHeaderName.LOCATION);
        System.out.println("Redirect to operator auth: " + redirectToOperatorAuth);

        Assertions.assertNotNull(redirectToOperatorAuth, "Redirect URL should not be null");
        Assertions.assertFalse(redirectToOperatorAuth.isEmpty(), "Redirect URL should not be empty");

        // STEP 2: Extract parameters from the redirect URL
        System.out.println("\nSTEP 2: Extracting parameters from redirect URL");

        // Extract state parameter from the redirect URL
        String state = extractParameterFromUrl(redirectToOperatorAuth, "state");
        String redirectUriFromUrl = extractParameterFromUrl(redirectToOperatorAuth, "redirect_uri");

        System.out.println("Extracted parameters:");
        System.out.println("- state: " + state);
        System.out.println("- redirect_uri: " + redirectUriFromUrl);

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
            // Have not figure out if PLAYBACK is possible
            apcCode = "apc_1a1550d9383b49d7b592a4670aab0d9c";
            System.out.println("In playback mode, using predefined APC code: " + apcCode);
        } else {
            // In RECORD or LIVE mode, make an actual HTTP request to the authcallback endpoint
            try {
                String callbackResponse = callAuthCallback(authCallbackUrl, operatorCode, state);
                System.out.println("Auth callback response: " + callbackResponse);

                // Extract the APC code from the callback response 
                if (callbackResponse != null) {
                    // The callback should redirect to the developer backend with the APC code in the query string
                    // Example: https://contoso.invalid/?apcCode=abc123&correlationId=xyz
                    apcCode = extractParameterFromUrl(callbackResponse, "apcCode");
                    System.out.println("Extracted APC code from callback response: " + apcCode);

                    // If we couldn't extract a valid code, fail the test
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

        // STEP 4: Final verification with the APC code - Using SDK
        System.out.println("\nSTEP 4: Making final verification request with APC code (verifyWithCode)");

        // Create verification content with the APC code
        NumberVerificationWithCodeContent finalContent = new NumberVerificationWithCodeContent(apcCode);

        try {
            // Execute the final API call with the APC code
            NumberVerificationResult result = numberVerificationClient.verifyWithCode(gatewayId, finalContent);

            // Log and validate the verification result
            System.out.println("Final verification result: " + result.isVerificationResult());

            // The test expects a successful verification
            Assertions.assertTrue(result.isVerificationResult(), "Expected a successful verification result");

        } catch (Exception ex) {
            System.out.println("Error in final verification: " + ex.getMessage());

            if (getTestMode() == TestMode.PLAYBACK) {
                // In playback mode, this failure is unexpected
                throw ex;
            } else {
                // In LIVE/RECORD mode, this might happen with test codes
                System.out.println("Error during verification with code: " + ex.getMessage());
                // We can still fail the test here if we want to enforce success
                throw new IllegalStateException("Verification with APC code failed: " + ex.getMessage(), ex);
            }
        }

        System.out.println("Number verification flow test completed successfully.");

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

        // Configure connection to not follow redirects
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("GET");

        // Get the response code
        int responseCode = connection.getResponseCode();
        System.out.println("Auth callback response code: " + responseCode);

        // Get the redirect location if available
        String location = connection.getHeaderField("Location");
        System.out.println("Auth callback redirect location: " + location);

        // Close the connection
        connection.disconnect();

        // Return the location header which should contain the redirect to the developer backend with the APC code
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
