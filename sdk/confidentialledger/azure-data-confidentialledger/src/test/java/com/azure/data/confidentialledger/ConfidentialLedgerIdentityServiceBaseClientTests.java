// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.confidentialledger;

import java.net.MalformedURLException;
import java.net.URL;

public class ConfidentialLedgerIdentityServiceBaseClientTests extends ConfidentialLedgerClientTestBase {
    private ConfidentialLedgerIdentityServiceBaseClient client;

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> {
            try {
                return new ConfidentialLedgerClientBuilder()
                    .ledgerUri(new URL(getConfidentialLedgerUrl()))
                    .identityServiceUri(new URL(getConfidentialIdentityUrl()))
                    .pipeline(httpPipeline)
                    .buildConfidentialLedgerIdentityServiceBaseClient();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        });
    }
//
//    @Test
//    public void getLedgerIdentity() {
//        DynamicResponse response = client.getLedgerIdentity()
//    }
//
//    @Test
//    public void getUser()
//    {
//        String objId = Configuration.getGlobalConfiguration().get("CONFIDENTIALLEDGER_CLIENT_OBJECTID");
//        DynamicResponse response = client.getUser(objId).send();
//
//        assertEquals(200, response.getStatusCode());
//
//        JsonReader jsonReader = Json.createReader(new StringReader(response.getBody().toString()));
//        JsonObject result = jsonReader.readObject();
//        assertTrue(result.containsKey("assignedRole"));
//        assertEquals(objId, result.getString("userId"));
//    }
//
//    @Test
//    public void getLedgerEntries()
//    {
//        DynamicResponse response = client.getLedgerEntries().send();
//
//        assertEquals(200, response.getStatusCode());
//
//        JsonReader jsonReader = Json.createReader(new StringReader(response.getBody().toString()));
//        JsonObject result = jsonReader.readObject();
//        assertTrue(result.containsKey("entries"));
//
//        JsonArray value = result.getJsonArray("entries");
//        assertTrue(value.size() > 0);
//    }
}
