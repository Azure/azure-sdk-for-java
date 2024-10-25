// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.authentication;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.AzureKeyCredential;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MixedRealityAccountKeyCredentialTest {
    // NOT REAL: Just a new UUID.
    private final UUID accountId = UUID.fromString("3ff503e0-15ef-4be9-bd99-29e6026d4bf6");

    // NOT REAL: Base64 encoded accountId.
    private final AzureKeyCredential keyCredential =
        new AzureKeyCredential("M2ZmNTAzZTAtMTVlZi00YmU5LWJkOTktMjllNjAyNmQ0YmY2");

    @Test
    public void create() {
        MixedRealityAccountKeyCredential credential = new MixedRealityAccountKeyCredential(accountId, keyCredential);

        assertNotNull(credential);
    }

    @Test
    public void getToken() {
        String expectedAccessTokenValue =
            "3ff503e0-15ef-4be9-bd99-29e6026d4bf6:M2ZmNTAzZTAtMTVlZi00YmU5LWJkOTktMjllNjAyNmQ0YmY2";
        OffsetDateTime expectedExpiration = OffsetDateTime.MAX;
        MixedRealityAccountKeyCredential credential = new MixedRealityAccountKeyCredential(accountId, keyCredential);

        AccessToken token = credential.getToken(null).block();

        assertNotNull(token);
        assertEquals(expectedAccessTokenValue, token.getToken());
        assertEquals(expectedExpiration, token.getExpiresAt());
    }
}
