// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.confidentialledger;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class GetLedgerEntrySample {
    public static void main(String[] args) {
        ConfidentialLedgerClient confidentialLedgerClient =
                new ConfidentialLedgerClientBuilder()
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .ledgerEndpoint("https://my-ledger.confidential-ledger.azure.com")
                        .buildClient();
        RequestOptions requestOptions = new RequestOptions();
        // the transactionId can be retrieved after posting to a ledger (see PostLedgerEntry.java)
        Response<BinaryData> response = confidentialLedgerClient.getLedgerEntryWithResponse("3.14", requestOptions);
    }
}
