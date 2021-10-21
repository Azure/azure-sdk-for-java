// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub;

import com.azure.messaging.webpubsub.models.GetClientAccessTokenOptions;
import com.azure.messaging.webpubsub.models.WebPubSubClientAccessToken;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.text.ParseException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link WebPubSubServiceAsyncClient#getClientAccessToken(GetClientAccessTokenOptions)
 * getAuthenticationToken} method.
 */
public class TokenGenerationTest {

    @ParameterizedTest
    @MethodSource("getTokenOptions")
    public void testTokenGeneration(GetClientAccessTokenOptions tokenOptions, String connectionString,
                                    String expectedUrlPrefix, String expectedSubject,
                                    List<String> expectedRoles) throws ParseException {
        WebPubSubServiceClient client = new WebPubSubServiceClientBuilder()
            .hub("test")
            .connectionString(connectionString)
            .buildClient();
        WebPubSubClientAccessToken authenticationToken = client.getClientAccessToken(tokenOptions);

        assertNotNull(authenticationToken.getToken());
        assertTrue(authenticationToken.getUrl().startsWith(expectedUrlPrefix));
        assertTrue(authenticationToken.getUrl().endsWith("access_token=" + authenticationToken.getToken()));
        JWT parse = JWTParser.parse(authenticationToken.getToken());
        JWTClaimsSet jwtClaimsSet = parse.getJWTClaimsSet();
        assertEquals(expectedSubject, jwtClaimsSet.getSubject());
        assertEquals(expectedRoles, jwtClaimsSet.getClaim("role"));
    }

    /**
     * Generates various option types for testing getAuthenticationToken() method.
     * @return A stream of options to parameterized test.
     */
    private static Stream<Arguments> getTokenOptions() {
        // connection strings have dummy access key for testing purposes
        return Stream.of(

            // HTTP
            Arguments.of(
                new GetClientAccessTokenOptions(),
                "Endpoint=http://http.webpubsubdev.azure.com;"
                    + "AccessKey=xJItsTUmJB1m+98rVG8YepBvx5BaMnUtGtbGa/oDM+mGyZ=;Version=1.0;",
                "ws://http.webpubsubdev.azure.com/", null, null),

            // HTTP with port
            Arguments.of(
                new GetClientAccessTokenOptions()
                    .setUserId("foo")
                    .setExpiresAfter(Duration.ofDays(1)),
                "Endpoint=http://testendpoint.webpubsubdev.azure.com;"
                    + "AccessKey=xJItsTUmJB1m+98rVG8YepBvx5BaMnUtGtbGa/oDM+mGyZ=;Version=1.0;Port=8080",
                "ws://testendpoint.webpubsubdev.azure.com:8080/", "foo", null),

            // HTTP with "http" in domain name
            Arguments.of(
                new GetClientAccessTokenOptions()
                    .setUserId("foo"),
                "Endpoint=http://http.webpubsubdev.azure.com;"
                    + "AccessKey=xJItsTUmJB1m+98rVG8YepBvx5BaMnUtGtbGa/oDM+mGyZ=;Version=1.0;",
                "ws://http.webpubsubdev.azure.com/", "foo", null),

            // HTTPS
            Arguments.of(
                new GetClientAccessTokenOptions()
                    .setUserId("foo")
                    .addRole("admin"),
                "Endpoint=https://testendpoint.webpubsubdev.azure.com;"
                    + "AccessKey=xJItsTUmJB1m+98rVG8YepBvx5BaMnUtGtbGa/oDM+mGyZ=;Version=1.0;",
                "wss://testendpoint.webpubsubdev.azure.com/", "foo", Arrays.asList("admin")),

            // HTTPS with port
            Arguments.of(
                new GetClientAccessTokenOptions()
                    .setUserId("foo")
                    .setExpiresAfter(Duration.ofDays(1))
                    .addRole("admin"),
                "Endpoint=https://testendpoint.webpubsubdev.azure.com;Port=8080;"
                    + "AccessKey=xJItsTUmJB1m+98rVG8YepBvx5BaMnUtGtbGa/oDM+mGyZ=;Version=1.0;",
                "wss://testendpoint.webpubsubdev.azure.com:8080/", "foo", Arrays.asList("admin")),

            // HTTPS with "https" in domain name
            Arguments.of(
                new GetClientAccessTokenOptions()
                    .setUserId("foo")
                    .setExpiresAfter(Duration.ofDays(1))
                    .setRoles(Arrays.asList("admin", "owner")),
                "Endpoint=https://https.webpubsubdev.azure.com;"
                    + "AccessKey=xJItsTUmJB1m+98rVG8YepBvx5BaMnUtGtbGa/oDM+mGyZ=;Version=1.0;",
                "wss://https.webpubsubdev.azure.com/", "foo", Arrays.asList("admin", "owner")),

            // Endpoint with path fragments
            Arguments.of(
                new GetClientAccessTokenOptions()
                    .setUserId("foo")
                    .setExpiresAfter(Duration.ofDays(1))
                    .setRoles(Arrays.asList("admin", "owner")),
                "Endpoint=https://testendpoint.webpubsubdev.azure.com/test/path?query_param=value;"
                    + "AccessKey=xJItsTUmJB1m+98rVG8YepBvx5BaMnUtGtbGa/oDM+mGyZ=;Version=1.0;Port=8080",
                "wss://testendpoint.webpubsubdev.azure.com:8080/", "foo", Arrays.asList("admin", "owner"))
        );
    }
}
