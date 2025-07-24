package com.azure.security.confidentialledger;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class PostEntryWithCollectionAndTagSample {
    public static void main(String[] args) {
        ConfidentialLedgerClient client = new ConfidentialLedgerClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .ledgerEndpoint(System.getenv("LEDGERENDPOINT"))
            .buildClient();

        BinaryData entry = BinaryData.fromString("{\"contents\":\"Ledger entry with collection and tags.\"}");
        RequestOptions options = new RequestOptions()
            .addQueryParam("collectionId", "Collection1")
            .addQueryParam("tags", "tag1,tag2,tag3");

        client.createLedgerEntry(entry, options);
    }
}
