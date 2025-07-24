package com.azure.security.confidentialledger;

import com.azure.core.http.rest.RequestOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class ListEntriesWithCollectionAndTagSample {
    public static void main(String[] args) {
        ConfidentialLedgerClient client = new ConfidentialLedgerClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .ledgerEndpoint(System.getenv("LEDGERENDPOINT"))
            .buildClient();

        RequestOptions options = new RequestOptions()
            .addQueryParam("collectionId", "Collection1")
            .addQueryParam("tags", "tag1,tag2");
        client.listLedgerEntries(options);
    }
}
