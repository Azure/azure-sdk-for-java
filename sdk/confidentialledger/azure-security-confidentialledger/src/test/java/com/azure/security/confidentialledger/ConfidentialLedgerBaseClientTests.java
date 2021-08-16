// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.confidentialledger;

import com.azure.core.experimental.http.DynamicResponse;
import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfidentialLedgerBaseClientTests extends ConfidentialLedgerClientTestBase {
    private ConfidentialLedgerBaseClient client;

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> {
            try {
                return new ConfidentialLedgerClientBuilder()
                    .ledgerUri(new URL(getConfidentialLedgerUrl()))
                    .identityServiceUri(new URL(getConfidentialIdentityUrl()))
                    .pipeline(httpPipeline)
                    .buildConfidentialLedgerBaseClient();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void getLedgerEntries()
    {
        DynamicResponse response = client.getLedgerEntries().send();

        assertEquals(200, response.getStatusCode());

        JsonReader jsonReader = Json.createReader(new StringReader(response.getBody().toString()));
        JsonObject result = jsonReader.readObject();
        int count = 0;
        if (result.containsKey("entries")) {
            JsonArray value = result.getJsonArray("entries");
            count += value.size();
        }
        if (result.containsKey("@nextLink")) {
            response = client.getLedgerEntriesNext(result.getString("@nextLink").replaceAll("^/", "")).send();
            jsonReader = Json.createReader(new StringReader(response.getBody().toString()));
            result = jsonReader.readObject();
            count += result.getJsonArray("entries").size();
        }
        assertTrue(count > 0);
    }
}
