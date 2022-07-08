// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.confidentialledger;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class GetCurrentLedgerEntry {
    public static void main(String[] args) {
        ConfidentialLedgerClient confidentialLedgerClient =
                new ConfidentialLedgerClientBuilder()
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .ledgerUri("https://my-ledger.confidential-ledger.azure.com")
                        .buildClient();
        // BEGIN:com.azure.security.confidentialledger.generated.getcurrentledgerentry.getcurrentledgerentry
        RequestOptions requestOptions = new RequestOptions();
        Response<BinaryData> response = confidentialLedgerClient.getCurrentLedgerEntryWithResponse(requestOptions);
        // END:com.azure.security.confidentialledger.generated.getcurrentledgerentry.getcurrentledgerentry
    }
}
