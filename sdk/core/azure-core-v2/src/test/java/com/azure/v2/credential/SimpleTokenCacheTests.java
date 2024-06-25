// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.credential;

import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests {@link SimpleTokenCache}.
 */
public class SimpleTokenCacheTests {
    @Test
    public void wipResetsOnCancel() {
        SimpleTokenCache simpleTokenCache
            = new SimpleTokenCache(() -> new AccessToken("test", OffsetDateTime.now().plusMinutes(5)))
                .delayElement(Duration.ofMinutes(1)));

        StepVerifier
            .create(simpleTokenCache.getToken().doOnRequest(ignored -> assertNotNull(simpleTokenCache.getWipValue())))
            .expectSubscription()
            .expectNoEvent(Duration.ofSeconds(2))
            .thenCancel()
            .verify();

        assertNull(simpleTokenCache.getWipValue());
    }
}
