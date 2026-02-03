// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.confidentialledger;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * Sample for listing all ledger entries.
 */
public class ListLedgerEntriesSample {
    public static void main(String[] args) {
        ConfidentialLedgerClient confidentialLedgerClient =
            new ConfidentialLedgerClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .ledgerEndpoint(Configuration.getGlobalConfiguration().get("LEDGERENDPOINT"))
                .buildClient();

        RequestOptions requestOptions = new RequestOptions();
        PagedIterable<BinaryData> response = confidentialLedgerClient.listLedgerEntries(requestOptions);
    }
}
