// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.confidentialledger;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class GetLedgerEntries {
    public static void main(String[] args) {
        ConfidentialLedgerClient confidentialLedgerClient =
                new ConfidentialLedgerClientBuilder()
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .ledgerUri("https://my-ledger.confidential-ledger.azure.com")
                        .buildClient();
        // BEGIN:com.azure.security.confidentialledger.generated.listledgerentries.getledgerentries

        // If you can't find a transaction, make sure you are adding a query parameter for the collection the transaction
        // will be a part of:
        // requestOptions = new RequestOptions().addQueryParam("collectionId", "" + 2);
        RequestOptions requestOptions =
                new RequestOptions()
                        .addQueryParam("fromTransactionId", "3.14")
                        .addQueryParam("toTransactionId", "3.42");
        PagedIterable<BinaryData> response = confidentialLedgerClient.listLedgerEntries(requestOptions);
        // END:com.azure.security.confidentialledger.generated.listledgerentries.getledgerentries
    }
}
