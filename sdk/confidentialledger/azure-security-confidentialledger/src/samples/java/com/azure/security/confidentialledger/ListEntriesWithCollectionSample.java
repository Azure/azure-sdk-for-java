package com.azure.security.confidentialledger;

import com.azure.core.http.rest.RequestOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class ListEntriesWithCollectionSample {
    public static void main(String[] args) {
        ConfidentialLedgerClient client = new ConfidentialLedgerClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .ledgerEndpoint(System.getenv("LEDGERENDPOINT"))
            .buildClient();

        RequestOptions options = new RequestOptions()
            .addQueryParam("collectionId", "Collection1");

        client.listLedgerEntries(options).forEach(entry -> {
            System.out.println(entry.toString());
        });
    }
}
