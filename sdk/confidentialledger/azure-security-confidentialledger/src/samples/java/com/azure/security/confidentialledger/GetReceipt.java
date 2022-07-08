// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.confidentialledger;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class GetReceipt {
    public static void main(String[] args) {
        ConfidentialLedgerClient confidentialLedgerClient =
                new ConfidentialLedgerClientBuilder()
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .ledgerUri("https://my-ledger.confidential-ledger.azure.com")
                        .buildClient();
        // BEGIN:com.azure.security.confidentialledger.generated.getreceipt.getreceipt
        RequestOptions requestOptions = new RequestOptions();
        // the transactionId can be retrieved after posting to a ledger (see PostLedgerEntry.java)
        Response<BinaryData> response = confidentialLedgerClient.getReceiptWithResponse("3.14", requestOptions);
        // END:com.azure.security.confidentialledger.generated.getreceipt.getreceipt
    }
}
