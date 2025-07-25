// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.confidentialledger;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.json.models.JsonObject;
import com.azure.json.models.JsonString;

public class PostLedgerEntryWithCollectionIdSample {
    public static void main(String[] args) {
        ConfidentialLedgerClient confidentialLedgerClient = new ConfidentialLedgerClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .ledgerEndpoint("https://my-ledger.confidential-ledger.azure.com")
            .buildClient();

        BinaryData entry = BinaryData.fromString("{\"contents\":\"New ledger entry with collectionId.\"}");

        // Specify collectionId in RequestOptions
        RequestOptions requestOptions = new RequestOptions()
            .addQueryParam("collectionId", "2");
        Response<BinaryData> response = confidentialLedgerClient.createLedgerEntryWithResponse(entry, requestOptions);

        BinaryData parsedResponse = response.getValue();
        JsonObject responseBodyJson = parsedResponse.toObject(JsonObject.class);
        String collectionId = ((JsonString) responseBodyJson.getProperty("collectionId")).getValue();

        String transactionId = response.getHeaders()
            .get(HttpHeaderName.fromString("x-ms-ccf-transaction-id"))
            .getValue();

        requestOptions = new RequestOptions();
        Response<BinaryData> transactionResponse = confidentialLedgerClient.getTransactionStatusWithResponse(
            transactionId, requestOptions);

        JsonObject transactionResponseBodyJson = transactionResponse.getValue().toObject(JsonObject.class);
        String responseTransactionId = ((JsonString) transactionResponseBodyJson.getProperty("transactionId"))
            .getValue();
        Integer statusCode = transactionResponse.getStatusCode();
    }
}
