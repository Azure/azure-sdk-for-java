// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.test.http.MockHttpResponse;
import com.microsoft.aad.msal4j.MsalServiceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Stream;

public class IdentityRetryStrategyTest {

    @Test
    public void testIdentityRetryDelayCalculation() {
        IdentityRetryStrategy retryStrategy = new IdentityRetryStrategy();
        int retry = 0;
        Queue<Long> expectedEntries = new LinkedList<>();
        expectedEntries.addAll(Arrays.asList(800L, 1600L, 3200L));

        while (retry < retryStrategy.getMaxRetries()) {
            long timeout = retryStrategy.calculateRetryDelay(retry).toMillis();
            if (expectedEntries.contains(timeout)) {
                expectedEntries.remove(timeout);
            } else {
                Assertions.fail("Unexpected timeout: " + timeout);
            }
            retry++;
        }
    }

    @Test
    public void testCustomMaxRetries() {
        IdentityRetryStrategy retryStrategy = new IdentityRetryStrategy(5);
        Assertions.assertEquals(5, retryStrategy.getMaxRetries());
    }

    @Test
    public void testCustomBaseDelay() {
        IdentityRetryStrategy retryStrategy = new IdentityRetryStrategy(3, Duration.ofMillis(1000));
        Assertions.assertEquals(1000L, retryStrategy.calculateRetryDelay(0).toMillis());
        Assertions.assertEquals(2000L, retryStrategy.calculateRetryDelay(1).toMillis());
        Assertions.assertEquals(4000L, retryStrategy.calculateRetryDelay(2).toMillis());
    }

    @ParameterizedTest
    @MethodSource("shouldRetryOnHttpStatusCode")
    public void testShouldRetryOnHttpResponse(int statusCode, boolean expectedRetry, String description) {
        IdentityRetryStrategy retryStrategy = new IdentityRetryStrategy();
        MockHttpResponse httpResponse = new MockHttpResponse(null, statusCode);

        Assertions.assertEquals(expectedRetry, retryStrategy.shouldRetry(httpResponse), description);
    }

    private static Stream<Arguments> shouldRetryOnHttpStatusCode() {
        return Stream.of(Arguments.of(400, false, "Should not retry on 400 Bad Request"),
            Arguments.of(401, true, "Should retry on 401 Unauthorized"),
            Arguments.of(403, false, "Should not retry on 403 Forbidden"),
            Arguments.of(429, true, "Should retry on 429 Too Many Requests"),
            Arguments.of(500, true, "Should retry on 500 Internal Server Error"),
            Arguments.of(503, true, "Should retry on 503 Service Unavailable"),
            Arguments.of(599, true, "Should retry on 599 status code"),
            Arguments.of(404, false, "Should not retry on 404 Not Found"),
            Arguments.of(200, false, "Should not retry on 200 OK"));
    }

    @Test
    public void testShouldNotRetryOnNonRetryableMsalException() {
        IdentityRetryStrategy retryStrategy = new IdentityRetryStrategy();

        // Test AADSTS700016 - Application not found in directory
        MsalServiceException exception = new MsalServiceException(
            "AADSTS700016: Application with identifier '12345678-1234-1234-1234-123456789012' was not found in the directory 'rpdmdev-aad'.",
            "AADSTS700016");

        Assertions.assertFalse(retryStrategy.shouldRetryException(exception),
            "Should not retry on AADSTS700016 - Application not found");
    }

    @Test
    public void testShouldNotRetryOnInvalidClientSecret() {
        IdentityRetryStrategy retryStrategy = new IdentityRetryStrategy();

        // Test AADSTS7000215 - Invalid client secret
        MsalServiceException exception
            = new MsalServiceException("AADSTS7000215: Invalid client secret is provided.", "AADSTS7000215");

        Assertions.assertFalse(retryStrategy.shouldRetryException(exception),
            "Should not retry on AADSTS7000215 - Invalid client secret");
    }

    @Test
    public void testShouldNotRetryOnExpiredClientSecret() {
        IdentityRetryStrategy retryStrategy = new IdentityRetryStrategy();

        // Test AADSTS7000222 - Expired client secret
        MsalServiceException exception
            = new MsalServiceException("AADSTS7000222: Invalid client secret is provided (expired).", "AADSTS7000222");

        Assertions.assertFalse(retryStrategy.shouldRetryException(exception),
            "Should not retry on AADSTS7000222 - Expired client secret");
    }

    @Test
    public void testShouldNotRetryOnInvalidUsername() {
        IdentityRetryStrategy retryStrategy = new IdentityRetryStrategy();

        // Test AADSTS50126 - Invalid username or password
        MsalServiceException exception
            = new MsalServiceException("AADSTS50126: Invalid username or password.", "AADSTS50126");

        Assertions.assertFalse(retryStrategy.shouldRetryException(exception),
            "Should not retry on AADSTS50126 - Invalid credentials");
    }

    @Test
    public void testShouldNotRetryOnUserNotFound() {
        IdentityRetryStrategy retryStrategy = new IdentityRetryStrategy();

        // Test AADSTS50034 - User account not found
        MsalServiceException exception
            = new MsalServiceException("AADSTS50034: User account not found in directory.", "AADSTS50034");

        Assertions.assertFalse(retryStrategy.shouldRetryException(exception),
            "Should not retry on AADSTS50034 - User not found");
    }

    @Test
    public void testShouldNotRetryWhenErrorCodeInMessage() {
        IdentityRetryStrategy retryStrategy = new IdentityRetryStrategy();

        // Test when error code is in message but not in errorCode field
        MsalServiceException exception = new MsalServiceException(
            "AADSTS700016: Application with identifier 'xyz' was not found in the directory.", null  // errorCode is null, but message contains AADSTS700016
        );

        Assertions.assertFalse(retryStrategy.shouldRetryException(exception),
            "Should not retry when non-retryable error code is in message");
    }

    @Test
    public void testShouldRetryOnRetryableMsalException() {
        IdentityRetryStrategy retryStrategy = new IdentityRetryStrategy();

        // Test a retryable MSAL error (not in the non-retryable list)
        MsalServiceException exception = new MsalServiceException("AADSTS50001: Resource not found.",  // This is not in the non-retryable list
            "AADSTS50001");

        Assertions.assertTrue(retryStrategy.shouldRetryException(exception), "Should retry on retryable MSAL errors");
    }

    @Test
    public void testShouldRetryOnIOException() {
        IdentityRetryStrategy retryStrategy = new IdentityRetryStrategy();

        IOException exception = new IOException("Network error");

        Assertions.assertTrue(retryStrategy.shouldRetryException(exception),
            "Should retry on IOException - network errors");
    }

    @Test
    public void testShouldNotRetryOnGeneralException() {
        IdentityRetryStrategy retryStrategy = new IdentityRetryStrategy();

        Exception exception = new Exception("General error");

        Assertions.assertFalse(retryStrategy.shouldRetryException(exception), "Should not retry on general exceptions");
    }

    @ParameterizedTest
    @MethodSource("nonRetryableErrorCodes")
    public void testAllNonRetryableErrorCodes(String errorCode, String description) {
        IdentityRetryStrategy retryStrategy = new IdentityRetryStrategy();

        MsalServiceException exception = new MsalServiceException(errorCode + ": " + description, errorCode);

        Assertions.assertFalse(retryStrategy.shouldRetryException(exception), "Should not retry on " + errorCode);
    }

    private static Stream<Arguments> nonRetryableErrorCodes() {
        return Stream.of(Arguments.of("AADSTS700016", "Application not found in directory"),
            Arguments.of("AADSTS7000215", "Invalid client secret"),
            Arguments.of("AADSTS7000222", "Expired client secret"),
            Arguments.of("AADSTS50034", "User account not found"),
            Arguments.of("AADSTS50059", "No tenant-identifying information"),
            Arguments.of("AADSTS50076", "Application is disabled"),
            Arguments.of("AADSTS50079", "Strong authentication required"),
            Arguments.of("AADSTS50097", "Device authentication required"),
            Arguments.of("AADSTS50105", "User not assigned to role"),
            Arguments.of("AADSTS50126", "Invalid username or password"),
            Arguments.of("AADSTS50128", "Tenant does not exist"),
            Arguments.of("AADSTS50129", "Device not workplace joined"),
            Arguments.of("AADSTS500011", "Resource principal not found"),
            Arguments.of("AADSTS500208", "Invalid login domain"),
            Arguments.of("AADSTS700027", "Client assertion failed signature validation"),
            Arguments.of("AADSTS650051", "Invalid redirect URI"),
            Arguments.of("AADSTS650052", "Invalid app registration configuration"),
            Arguments.of("AADSTS70001", "Application not found in directory"),
            Arguments.of("AADSTS70002", "Invalid client credentials"),
            Arguments.of("AADSTS90002", "Tenant does not exist"),
            Arguments.of("AADSTS90014", "Required field is missing"), Arguments.of("AADSTS90015", "Invalid parameter"),
            Arguments.of("AADSTS90019", "Tenant-specific endpoint required"),
            Arguments.of("AADSTS90023", "Bad request"), Arguments.of("AADSTS900144", "Request body missing parameter"),
            Arguments.of("AADSTS1002016", "Invalid request format"),
            Arguments.of("AADSTS900382", "Confidential client not supported in cross-cloud"));
    }

    @Test
    public void testRealWorldScenario_ApplicationNotFound() {
        IdentityRetryStrategy retryStrategy = new IdentityRetryStrategy();

        // Simulate the actual error from the user's scenario
        MsalServiceException exception = new MsalServiceException(
            "AADSTS700016: Application with identifier '12345678-1234-1234-1234-123456789012' was not found in the directory 'rpdmdev-aad'. "
                + "This can happen if the application has not been installed by the administrator of the tenant or consented to by any user in the tenant. "
                + "You may have sent your authentication request to the wrong tenant.\r\n"
                + "Trace ID: c326048f-5f74-4d3b-87d3-07e8541f5800\r\n"
                + "Correlation ID: 88a44f83-54a2-4133-aee6-731648210c4d\r\n" + "Timestamp: 2023-06-29 00:42:41Z",
            "AADSTS700016");

        Assertions.assertFalse(retryStrategy.shouldRetryException(exception),
            "Should not retry on application not found - this is a configuration error that won't succeed on retry");
    }
}
