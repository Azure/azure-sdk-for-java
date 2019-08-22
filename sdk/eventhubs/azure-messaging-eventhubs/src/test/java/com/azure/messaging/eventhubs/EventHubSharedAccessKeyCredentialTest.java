// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import org.junit.Assert;
import org.junit.Test;
import reactor.test.StepVerifier;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

public class EventHubSharedAccessKeyCredentialTest {
    private static final String KEY_NAME = "some-key-name";
    private static final String KEY_VALUE = "ctzMq410TV3wS7upTBcunJTDLEJwMAZuFPfr0mrrA08=";
    private static final Duration TOKEN_DURATION = Duration.ofMinutes(10);

    @Test(expected = NullPointerException.class)
    public void constructorNullDuration() throws InvalidKeyException, NoSuchAlgorithmException {
        new EventHubSharedAccessKeyCredential(KEY_NAME, KEY_VALUE, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorNullKey() throws InvalidKeyException, NoSuchAlgorithmException {
        new EventHubSharedAccessKeyCredential(null, KEY_VALUE, TOKEN_DURATION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorNullValue() throws InvalidKeyException, NoSuchAlgorithmException {
        new EventHubSharedAccessKeyCredential(KEY_NAME, null, TOKEN_DURATION);
    }

    @Test
    public void constructsToken() throws InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {
        // Arrange
        final String signatureExpires = "se";
        final EventHubSharedAccessKeyCredential credential =
            new EventHubSharedAccessKeyCredential(KEY_NAME, KEY_VALUE, TOKEN_DURATION);
        final String resource = "some resource name";
        final String resourceUriEncode = URLEncoder.encode(resource, StandardCharsets.UTF_8.toString());
        final Map<String, String> expected = new HashMap<>();
        expected.put("sr", resourceUriEncode);
        expected.put("sig", null);
        expected.put(signatureExpires, null);
        expected.put("skn", KEY_NAME);

        // Act & Assert
        StepVerifier.create(credential.getToken(resource))
            .assertNext(accessToken -> {
                Assert.assertNotNull(accessToken);

                Assert.assertFalse(accessToken.isExpired());
                Assert.assertTrue(accessToken.expiresOn().isAfter(OffsetDateTime.now(ZoneOffset.UTC)));

                final String[] split = accessToken.token().split(" ");
                Assert.assertEquals(2, split.length);
                Assert.assertEquals("SharedAccessSignature", split[0].trim());

                final String[] components = split[1].split("&");
                for (String component : components) {
                    final String[] pair = component.split("=");
                    final String key = pair[0];
                    final String value = pair[1];
                    final String expectedValue = expected.get(key);

                    Assert.assertTrue(expected.containsKey(key));

                    // These are the values that are random, but we expect the expiration to be after this date.
                    if (signatureExpires.equals(key)) {
                        final Instant instant = Instant.ofEpochSecond(Long.valueOf(value));
                        Assert.assertTrue(instant.isAfter(Instant.now()));
                    } else if (expectedValue == null) {
                        Assert.assertNotNull(value);
                    } else {
                        Assert.assertEquals(expectedValue, value);
                    }
                }
            })
            .verifyComplete();
    }
}
