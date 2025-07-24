package com.azure.security.confidentialledger;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.confidentialledger.ConfidentialLedgerClient;
import com.azure.security.confidentialledger.ConfidentialLedgerClientBuilder;

public class CreateLedgerEntrySample {
    public static void main(String[] args) {
        ConfidentialLedgerClient confidentialLedgerClient =
            new ConfidentialLedgerClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .ledgerEndpoint(Configuration.getGlobalConfiguration().get("LEDGERENDPOINT"))
                .buildClient();

        BinaryData entry = BinaryData.fromString("{\"contents\":\"Basic ledger entry contents.\"}");
        Response<BinaryData> response = confidentialLedgerClient.createLedgerEntryWithResponse(entry, new RequestOptions());
    }
}
