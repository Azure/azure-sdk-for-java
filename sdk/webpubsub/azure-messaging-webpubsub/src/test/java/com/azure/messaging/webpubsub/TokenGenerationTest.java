// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import com.azure.messaging.webpubsub.models.GetAuthenticationTokenOptions;
import com.azure.messaging.webpubsub.models.WebPubSubAuthenticationToken;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link WebPubSubAsyncServiceClient#getAuthenticationToken(GetAuthenticationTokenOptions)
 * getAuthenticationToken} method.
 */
public class TokenGenerationTest {

    @ParameterizedTest
    @MethodSource("getTokenOptions")
    public void testTokenGeneration(GetAuthenticationTokenOptions tokenOptions) {
        WebPubSubServiceClient client = new WebPubSubClientBuilder()
            .hub("test")
            // this connection string has a dummy access key for testing purposes
            .connectionString("Endpoint=https://testendpoint.webpubsubdev.azure.com;"
                + "AccessKey=xJItsTUmJB1m+98rVG8YepBvx5BaMnUtGtbGa/oDM+mGyZ=;Version=1.0;")
            .buildClient();
        WebPubSubAuthenticationToken authenticationToken = client.getAuthenticationToken(tokenOptions);

        assertNotNull(authenticationToken.getAuthToken());
        assertTrue(authenticationToken.getUrl().startsWith("ws://testendpoint.webpubsubdev.azure.com"));
        assertTrue(authenticationToken.getUrl().endsWith("access_token=" + authenticationToken.getAuthToken()));
    }

    /**
     * Generates various option types for testing getAuthenticationToken() method.
     * @return A stream of options to parameterized test.
     */
    private static Stream<Arguments> getTokenOptions() {
        return Stream.of(
            Arguments.of(
                new GetAuthenticationTokenOptions()),

            Arguments.of(
                new GetAuthenticationTokenOptions()
                    .setUserId("foo")),

            Arguments.of(
                new GetAuthenticationTokenOptions()
                    .setUserId("foo")
                    .addRole("admin")),

            Arguments.of(
                new GetAuthenticationTokenOptions()
                    .setUserId("foo")
                    .setExpiresAfter(Duration.ofDays(1))),

            Arguments.of(
                new GetAuthenticationTokenOptions()
                    .setUserId("foo")
                    .setExpiresAfter(Duration.ofDays(1))
                    .addRole("admin")),

            Arguments.of(
                new GetAuthenticationTokenOptions()
                    .setUserId("foo")
                    .setExpiresAfter(Duration.ofDays(1))
                    .setRoles(Arrays.asList("admin", "owner")))
        );
    }
}
