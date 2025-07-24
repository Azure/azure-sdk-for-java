package com.azure.security.confidentialledger;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class PostEntryWithCollectionSample {
    public static void main(String[] args) {
        ConfidentialLedgerClient client = new ConfidentialLedgerClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .ledgerEndpoint(System.getenv("LEDGERENDPOINT"))
            .buildClient();

        BinaryData entry = BinaryData.fromString("{\"contents\":\"Ledger entry with collection.\"}");
        RequestOptions options = new RequestOptions()
            .addQueryParam("collectionId", "Collection1");

        client.createLedgerEntry(entry, options);
    }
}
