// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.confidentialledger;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.json.models.JsonObject;
import com.azure.json.models.JsonString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Base64;

public final class UserTests extends ConfidentialLedgerClientTestBase {

    // Sanitized OID used in playback recordings
    private static final String SANITIZED_USER_OID = "sanitized-user-oid";

    @Test
    public void testGetCurrentUserTests() {
        String oid;
        if (getTestMode() == TestMode.PLAYBACK) {
            // In playback mode, use the sanitized OID that matches the recording
            oid = SANITIZED_USER_OID;
        } else {
            // In live/record mode, get the current user's OID from the access token
            AccessToken token = new DefaultAzureCredentialBuilder().build()
                .getToken(new TokenRequestContext().addScopes("https://confidential-ledger.azure.com/.default"))
                .block();
            Assertions.assertNotNull(token, "Should be able to get an access token");

            // Decode JWT payload to extract oid claim
            String[] parts = token.getToken().split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            JsonObject claims = BinaryData.fromString(payload).toObject(JsonObject.class);
            oid = ((JsonString) claims.getProperty("oid")).getValue();
            Assertions.assertNotNull(oid, "Token should contain an oid claim");
        }

        // Verify the user exists in the ledger
        Response<BinaryData> response = confidentialLedgerClient.getLedgerUserWithResponse(oid, new RequestOptions());

        Assertions.assertEquals(200, response.getStatusCode());

        JsonObject jsonObject = response.getValue().toObject(JsonObject.class);
        Assertions.assertNotNull(jsonObject.getProperty("assignedRoles"));
    }
}
