// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.planetarycomputer;

import com.azure.analytics.planetarycomputer.models.SharedAccessSignatureSignedLink;
import com.azure.analytics.planetarycomputer.models.SharedAccessSignatureToken;
import com.azure.analytics.planetarycomputer.models.StacCollection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Shared Access Signature operations (Group 03).
 * Covers: getToken, getUrl (sign URL), revokeToken.
 * Python equivalent: planetary_computer_03_shared_access_signature.py
 * JS equivalent: 03_sharedAccessSignature.spec.ts
 */
@Tag("SAS")
public class TestPlanetaryComputer03SharedAccessSignatureTests extends PlanetaryComputerTestBase {

    /**
     * Test getting a SAS token with default duration.
     * Python equivalent: generate_sas_token
     * JS equivalent: should get token with default duration
     */
    @Test
    @Tag("GetToken")
    public void test03_01_GetTokenWithDefaultDuration() {
        SharedAccessSignatureClient sasClient = getSasClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing getToken for collection: " + collectionId);

        SharedAccessSignatureToken tokenResponse = sasClient.getToken(collectionId);

        assertNotNull(tokenResponse, "Token response should not be null");
        assertNotNull(tokenResponse.getToken(), "Token should not be null");
        assertTrue(tokenResponse.getToken().length() > 0, "Token should not be empty");
        assertNotNull(tokenResponse.getExpiresOn(), "ExpiresOn should not be null");

        System.out.println("Token length: " + tokenResponse.getToken().length());
        System.out.println("Expires on: " + tokenResponse.getExpiresOn());
        System.out.println("SAS token retrieved successfully");
    }

    /**
     * Test getting a SAS token with custom duration.
     * JS equivalent: should get token with custom duration (60 minutes)
     */
    @Test
    @Tag("GetToken")
    public void test03_02_GetTokenWithCustomDuration() {
        SharedAccessSignatureClient sasClient = getSasClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing getToken with 60-minute duration for collection: " + collectionId);

        SharedAccessSignatureToken tokenResponse = sasClient.getToken(collectionId, 60);

        assertNotNull(tokenResponse, "Token response should not be null");
        assertNotNull(tokenResponse.getToken(), "Token should not be null");
        assertTrue(tokenResponse.getToken().length() > 0, "Token should not be empty");
        assertNotNull(tokenResponse.getExpiresOn(), "ExpiresOn should not be null");

        System.out.println("Token length: " + tokenResponse.getToken().length());
        System.out.println("Expires on: " + tokenResponse.getExpiresOn());
        System.out.println("SAS token with custom duration retrieved successfully");
    }

    /**
     * Test signing a URL with SAS.
     * Python equivalent: sign_asset_href
     * JS equivalent: should sign a URL with SAS
     */
    @Test
    @Tag("SignUrl")
    public void test03_03_SignUrl() {
        SharedAccessSignatureClient sasClient = getSasClient();
        StacClient stacClient = getStacClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing getUrl (sign URL) for collection: " + collectionId);

        // Get a collection to find an asset href to sign
        StacCollection collection = stacClient.getCollection(collectionId);
        assertNotNull(collection, "Collection should not be null");

        // Find a suitable href from collection assets
        String originalHref = null;
        if (collection.getAssets() != null) {
            for (java.util.Map.Entry<String, com.azure.analytics.planetarycomputer.models.StacAsset> entry : collection
                .getAssets()
                .entrySet()) {
                if (entry.getValue().getHref() != null && !entry.getValue().getHref().isEmpty()) {
                    originalHref = entry.getValue().getHref();
                    System.out.println("Using asset '" + entry.getKey() + "' href: " + originalHref);
                    break;
                }
            }
        }

        if (originalHref == null) {
            System.out.println("No asset href found on collection, skipping URL signing test");
            return;
        }

        // Sign the URL
        SharedAccessSignatureSignedLink signedLink = sasClient.getUrl(originalHref);

        assertNotNull(signedLink, "Signed link response should not be null");
        assertNotNull(signedLink.getHref(), "Signed href should not be null");
        assertNotEquals(originalHref, signedLink.getHref(), "Signed URL should differ from original");
        assertTrue(signedLink.getHref().contains("?") || signedLink.getHref().contains("sig="),
            "Signed URL should contain query parameters or sig");

        System.out.println("Original href: " + originalHref);
        System.out.println("Signed href: " + signedLink.getHref());
        System.out.println("URL signed successfully");
    }

    /**
     * Test revoking a SAS token.
     * Python equivalent: revoke_token
     * JS equivalent: should revoke token without error
     */
    @Test
    @Tag("RevokeToken")
    public void test03_04_RevokeToken() {
        SharedAccessSignatureClient sasClient = getSasClient();
        String collectionId = testEnvironment.getCollectionId();

        System.out.println("Testing revokeToken");

        // First get a token to ensure there's something to revoke
        SharedAccessSignatureToken tokenResponse = sasClient.getToken(collectionId);
        assertNotNull(tokenResponse, "Should be able to get a token before revoking");

        // Revoke the token - should complete without error
        sasClient.revokeToken();

        System.out.println("Token revoked successfully");
    }
}
