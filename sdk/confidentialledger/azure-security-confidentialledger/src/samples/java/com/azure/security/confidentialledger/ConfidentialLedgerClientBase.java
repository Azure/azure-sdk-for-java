// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.confidentialledger;

import com.azure.identity.DefaultAzureCredentialBuilder;

public class ConfidentialLedgerClientBase {
    public ConfidentialLedgerClientBase() {
        // BEGIN:readme-sample-createClient
        ConfidentialLedgerClient confidentialLedgerClient =
                new ConfidentialLedgerClientBuilder()
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .ledgerUri("https://my-ledger.confidential-ledger.azure.com")
                        .buildClient();
        // END:readme-sample-createClient
    }
}
