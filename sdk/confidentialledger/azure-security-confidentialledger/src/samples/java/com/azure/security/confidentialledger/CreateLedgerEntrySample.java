// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.confidentialledger;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class CreateLedgerEntrySample {
    public static void main(String[] args) {
        ConfidentialLedgerClient confidentialLedgerClient =
            new ConfidentialLedgerClientBuilder()
                .addPolicy(new BearerTokenAuthenticationPolicy(new DefaultAzureCredentialBuilder().build(), "https://confidential-ledger.azure.com/.default"))
                .ledgerEndpoint(Configuration.getGlobalConfiguration().get("LEDGERENDPOINT"))
                .buildClient();

        BinaryData entry = BinaryData.fromString("{\"contents\":\"Basic ledger entry contents.\"}");
        Response<BinaryData> response = confidentialLedgerClient.createLedgerEntryWithResponse(entry, new RequestOptions());
    }
}
