// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.confidentialledger.generated;

import com.azure.security.confidentialledger.ConfidentialLedgerClient;
import com.azure.security.confidentialledger.ConfidentialLedgerClientBuilder;

@SuppressWarnings("unused")
public class ReadmeSamples {
    public void createClient() {
        // BEGIN: readme-sample-createClient
        ConfidentialLedgerClient confidentialLedgerClient = new ConfidentialLedgerClientBuilder().buildClient();
        // END: readme-sample-createClient
        
    }
}
