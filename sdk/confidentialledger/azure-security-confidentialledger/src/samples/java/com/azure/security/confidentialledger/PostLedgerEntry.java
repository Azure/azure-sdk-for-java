// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.confidentialledger;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class PostLedgerEntry {
    public static void main(String[] args) {
        ConfidentialLedgerClient confidentialLedgerClient =
                new ConfidentialLedgerClientBuilder()
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .ledgerUri("https://my-ledger.confidential-ledger.azure.com")
                        .buildClient();
        BinaryData entry = BinaryData.fromString("{\"contents\":\"New ledger entry contents.\"}");

        // optionally, you can define a collection id (here, the collectionId is 2):
        // requestOptions = new RequestOptions().addQueryParam("collectionId", "" + 2);
        RequestOptions requestOptions = new RequestOptions();
        Response<BinaryData> response = confidentialLedgerClient.postLedgerEntryWithResponse(entry, requestOptions);

        // you can use the transaction id to get the transaction status
        String transactionId = response.getHeaders().get("x-ms-ccf-transaction-id").getValue();
    }
}
