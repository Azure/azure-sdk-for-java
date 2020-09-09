// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.credential.TokenRequestContext;
import com.azure.messaging.eventhubs.implementation.EventHubSharedKeyCredential;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EventHubSharedKeyCredentialTest {
    private static final String KEY_NAME = "some-key-name";
    private static final String KEY_VALUE = "ctzMq410TV3wS7upTBcunJTDLEJwMAZuFPfr0mrrA08=";
    private static final Duration TOKEN_DURATION = Duration.ofMinutes(10);

    @Test
    public void constructorNullDuration() {
        assertThrows(NullPointerException.class, () -> new EventHubSharedKeyCredential(KEY_NAME, KEY_VALUE, null));
    }

    @Test
    public void constructorNullKey() {
        assertThrows(NullPointerException.class, () -> new EventHubSharedKeyCredential(null, KEY_VALUE, TOKEN_DURATION));
    }

    @Test
    public void constructorEmptyKey() {
        assertThrows(IllegalArgumentException.class, () -> new EventHubSharedKeyCredential("", KEY_VALUE, TOKEN_DURATION));

    }

    @Test
    public void constructorNullValue() {
        assertThrows(NullPointerException.class, () -> new EventHubSharedKeyCredential(KEY_NAME, null, TOKEN_DURATION));
    }

    @Test
    public void constructorEmptyValue() {
        assertThrows(IllegalArgumentException.class, () -> new EventHubSharedKeyCredential(KEY_NAME, "", TOKEN_DURATION));
    }

    @Test
    public void constructsToken() throws UnsupportedEncodingException {
        // Arrange
        final String signatureExpires = "se";
        final EventHubSharedKeyCredential credential =
            new EventHubSharedKeyCredential(KEY_NAME, KEY_VALUE, TOKEN_DURATION);
        final String resource = "some resource name";
        final String resourceUriEncode = URLEncoder.encode(resource, StandardCharsets.UTF_8.toString());
        final Map<String, String> expected = new HashMap<>();
        expected.put("sr", resourceUriEncode);
        expected.put("sig", null);
        expected.put(signatureExpires, null);
        expected.put("skn", KEY_NAME);

        // Act & Assert
        StepVerifier.create(credential.getToken(new TokenRequestContext().addScopes(resource)))
            .assertNext(accessToken -> {
                assertNotNull(accessToken);

                assertFalse(accessToken.isExpired());
                assertTrue(accessToken.getExpiresAt().isAfter(OffsetDateTime.now(ZoneOffset.UTC)));

                final String[] split = accessToken.getToken().split(" ");
                assertEquals(2, split.length);
                assertEquals("SharedAccessSignature", split[0].trim());

                final String[] components = split[1].split("&");
                for (String component : components) {
                    final String[] pair = component.split("=");
                    final String key = pair[0];
                    final String value = pair[1];
                    final String expectedValue = expected.get(key);

                    assertTrue(expected.containsKey(key));

                    // These are the values that are random, but we expect the expiration to be after this date.
                    if (signatureExpires.equals(key)) {
                        final Instant instant = Instant.ofEpochSecond(Long.parseLong(value));
                        assertTrue(instant.isAfter(Instant.now()));
                    } else if (expectedValue == null) {
                        assertNotNull(value);
                    } else {
                        assertEquals(expectedValue, value);
                    }
                }
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("getSas")
    public void testSharedAccessSignatureCredential(String sas, OffsetDateTime expectedExpirationTime) {
        EventHubSharedKeyCredential eventHubSharedKeyCredential = new EventHubSharedKeyCredential(sas);
        StepVerifier.create(eventHubSharedKeyCredential.getToken(new TokenRequestContext().addScopes("sb://test"
            + "-entity.servicebus.windows.net/.default")))
            .assertNext(token -> {
                assertNotNull(token.getToken());
                assertEquals(sas, token.getToken());
                assertEquals(expectedExpirationTime, token.getExpiresAt());
            })
            .verifyComplete();
    }

    private static Stream<Arguments> getSas() {
        String validSas = "SharedAccessSignature "
            + "sr=https%3A%2F%2Fentity-name.servicebus.windows.net%2F"
            + "&sig=encodedsignature%3D"
            + "&se=1599537084"
            + "&skn=test-sas-key";
        String validSasWithNoExpirationTime = "SharedAccessSignature "
            + "sr=https%3A%2F%2Fentity-name.servicebus.windows.net%2F"
            + "&sig=encodedsignature%3D"
            + "&skn=test-sas-key";
        String validSasInvalidExpirationTimeFormat = "SharedAccessSignature "
            + "sr=https%3A%2F%2Fentity-name.servicebus.windows.net%2F"
            + "&sig=encodedsignature%3D"
            + "&se=se=2020-12-31T13:37:45Z"
            + "&skn=test-sas-key";

        return Stream.of(
            Arguments.of(validSas, OffsetDateTime.parse("2020-09-08T03:51:24Z")),
            Arguments.of(validSasWithNoExpirationTime, OffsetDateTime.MAX),
            Arguments.of(validSasInvalidExpirationTimeFormat, OffsetDateTime.MAX)
        );
    }

}
