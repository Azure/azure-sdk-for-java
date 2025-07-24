package com.azure.security.confidentialledger;

import com.azure.identity.DefaultAzureCredentialBuilder;

public class ListEntriesSample {
    public static void main(String[] args) {
        ConfidentialLedgerClient client = new ConfidentialLedgerClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .ledgerEndpoint(System.getenv("LEDGERENDPOINT"))
            .buildClient();

        client.listLedgerEntries().forEach(entry -> {
            System.out.println(entry.toString());
        });
    }
}
