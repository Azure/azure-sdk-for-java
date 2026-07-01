// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.confidentialledger;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.json.models.JsonObject;
import com.azure.json.models.JsonString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Base64;

public final class UserTests extends ConfidentialLedgerClientTestBase {
    @Test
    public void testGetCurrentUserTests() {
        // Get the current user's OID from the access token
        AccessToken token = new DefaultAzureCredentialBuilder().build()
            .getToken(new TokenRequestContext().addScopes("https://confidential-ledger.azure.com/.default"))
            .block();
        Assertions.assertNotNull(token, "Should be able to get an access token");

        // Decode JWT payload to extract oid claim
        String[] parts = token.getToken().split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
        JsonObject claims = BinaryData.fromString(payload).toObject(JsonObject.class);
        String oid = ((JsonString) claims.getProperty("oid")).getValue();
        Assertions.assertNotNull(oid, "Token should contain an oid claim");

        // Verify the current user exists in the ledger
        Response<BinaryData> response = confidentialLedgerClient.getLedgerUserWithResponse(oid, new RequestOptions());

        Assertions.assertEquals(200, response.getStatusCode());

        JsonObject jsonObject = response.getValue().toObject(JsonObject.class);
        Assertions.assertNotNull(jsonObject.getProperty("assignedRoles"));
    }
}
