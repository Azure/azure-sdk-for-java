// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer.async;

import com.azure.analytics.planetarycomputer.PlanetaryComputerTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Async tests for Shared Access Signature operations (Group 03).
 * Mirrors sync tests in TestPlanetaryComputer03SharedAccessSignatureTests.
 * Covers: getToken, getUrl (sign URL), revokeToken.
 */
@Tag("SAS")
@Tag("Async")
public class TestPlanetaryComputer03AsyncSasTests extends PlanetaryComputerTestBase {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    /**
     * Async test getting a SAS token with default duration.
     * Mirrors: test03_01_GetTokenWithDefaultDuration
     */
    @Test
    @Tag("GetToken")
    public void test03_01_GetTokenWithDefaultDurationAsync() {
        String collectionId = testEnvironment.getCollectionId();

        StepVerifier.create(sharedAccessSignatureAsyncClient.getToken(collectionId)).assertNext(tokenResponse -> {
            assertNotNull(tokenResponse, "Token response should not be null");
            assertNotNull(tokenResponse.getToken(), "Token should not be null");
            assertTrue(tokenResponse.getToken().length() > 0, "Token should not be empty");
            assertNotNull(tokenResponse.getExpiresOn(), "ExpiresOn should not be null");
        }).verifyComplete();
    }

    /**
     * Async test getting a SAS token with custom duration (60 minutes).
     * Mirrors: test03_02_GetTokenWithCustomDuration
     */
    @Test
    @Tag("GetToken")
    public void test03_02_GetTokenWithCustomDurationAsync() {
        String collectionId = testEnvironment.getCollectionId();

        StepVerifier.create(sharedAccessSignatureAsyncClient.getToken(collectionId, 60)).assertNext(tokenResponse -> {
            assertNotNull(tokenResponse, "Token response should not be null");
            assertNotNull(tokenResponse.getToken(), "Token should not be null");
            assertTrue(tokenResponse.getToken().length() > 0, "Token should not be empty");
            assertNotNull(tokenResponse.getExpiresOn(), "ExpiresOn should not be null");
        }).verifyComplete();
    }

    /**
     * Async test signing a URL with SAS.
     * Mirrors: test03_03_SignUrl
     */
    @Test
    @Tag("SignUrl")
    public void test03_03_SignUrlAsync() {
        String collectionId = testEnvironment.getCollectionId();

        // First get a collection to find an asset href to sign
        StepVerifier.create(stacAsyncClient.getCollection(collectionId).flatMap(collection -> {
            assertNotNull(collection, "Collection should not be null");

            // Find a suitable href from collection assets
            String originalHref = null;
            if (collection.getAssets() != null) {
                for (java.util.Map.Entry<String, com.azure.analytics.planetarycomputer.models.StacAsset> entry : collection
                    .getAssets()
                    .entrySet()) {
                    if (entry.getValue().getHref() != null && !entry.getValue().getHref().isEmpty()) {
                        originalHref = entry.getValue().getHref();
                        break;
                    }
                }
            }

            if (originalHref == null) {
                return reactor.core.publisher.Mono.empty();
            }

            final String href = originalHref;
            return sharedAccessSignatureAsyncClient.getUrl(href)
                .map(signedLink -> new String[] { href, signedLink.getHref() });
        })).assertNext(hrefs -> {
            String originalHref = hrefs[0];
            String signedHref = hrefs[1];
            assertNotNull(signedHref, "Signed href should not be null");
            assertNotEquals(originalHref, signedHref, "Signed URL should differ from original");
            assertTrue(signedHref.contains("?") || signedHref.contains("sig="),
                "Signed URL should contain query parameters or sig");
        }).verifyComplete();
    }

    /**
     * Async test revoking a SAS token.
     * Mirrors: test03_04_RevokeToken
     */
    @Test
    @Tag("RevokeToken")
    public void test03_04_RevokeTokenAsync() {
        String collectionId = testEnvironment.getCollectionId();

        // First get a token to ensure there's something to revoke, then revoke
        StepVerifier.create(sharedAccessSignatureAsyncClient.getToken(collectionId).flatMap(tokenResponse -> {
            assertNotNull(tokenResponse, "Should be able to get a token before revoking");
            return sharedAccessSignatureAsyncClient.revokeToken();
        })).verifyComplete();
    }
}
