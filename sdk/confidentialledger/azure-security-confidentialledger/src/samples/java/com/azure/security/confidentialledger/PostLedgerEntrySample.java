// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.confidentialledger;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PostLedgerEntrySample {
    public static void main(String[] args) {
        ConfidentialLedgerClient confidentialLedgerClient =
                new ConfidentialLedgerClientBuilder()
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .ledgerEndpoint("https://my-ledger.confidential-ledger.azure.com")
                        .buildClient();
        BinaryData entry = BinaryData.fromString("{\"contents\":\"New ledger entry contents.\"}");

        // optionally, you can define a collection id (here, the collectionId is 2):
        // requestOptions = new RequestOptions().addQueryParam("collectionId", "" + 2);
        RequestOptions requestOptions = new RequestOptions();
        Response<BinaryData> response = confidentialLedgerClient.createLedgerEntryWithResponse(entry, requestOptions);

        BinaryData parsedResponse = response.getValue();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode responseBodyJson = null;

        try {
            
            responseBodyJson = objectMapper.readTree(parsedResponse.toBytes());
        } catch (IOException e) {
            e.printStackTrace();
            Assertions.assertTrue(false);
        }

        String collectionId = responseBodyJson.get("collectionId").asText();

        // you can use the transaction id to get the transaction status
        String transactionId = response.getHeaders().get("x-ms-ccf-transaction-id").getValue();

        requestOptions = new RequestOptions();
        // the transactionId can be retrieved after posting to a ledger (see PostLedgerEntry.java)
        Response<BinaryData> transactionResponse = confidentialLedgerClient.getTransactionStatusWithResponse(transactionId, requestOptions);
    
        JsonNode transactionResponseBodyJson = null;

        try {
            transactionResponseBodyJson = objectMapper.readTree(transactionResponse.getValue().toBytes());
        } catch (IOException e) {
            e.printStackTrace();
            Assertions.assertTrue(false);
        }

        String responseTransactionId = transactionResponseBodyJson.get("transactionId").asText();
        Integer statusCode = transactionResponse.getStatusCode();
    }
}
