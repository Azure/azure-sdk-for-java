// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.confidentialledger;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.json.models.JsonObject;
import com.azure.json.models.JsonString;

public class GetUserSample {
    public static void main(String[] args) {
        ConfidentialLedgerClient confidentialLedgerClient =
                new ConfidentialLedgerClientBuilder()
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .ledgerEndpoint("https://my-ledger.confidential-ledger.azure.com")
                        .buildClient();
        RequestOptions requestOptions = new RequestOptions();

        String aadObjectId = "AAD OBJECT ID";
        Response<BinaryData> response = confidentialLedgerClient.getUserWithResponse(aadObjectId, requestOptions);

        BinaryData parsedResponse = response.getValue();

        JsonObject responseBodyJson = parsedResponse.toObject(JsonObject.class);
        System.out.println("Assigned role for user is "
            + ((JsonString) responseBodyJson.getProperty("assignedRole")).getValue());
    }
}
