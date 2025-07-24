package com.azure.security.confidentialledger;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.confidentialledger.ConfidentialLedgerClient;
import com.azure.security.confidentialledger.ConfidentialLedgerClientBuilder;

/**
 * Sample for listing ledger entries with collectionId and tags filters.
 */
public class ListLedgerEntriesWithCollectionIdAndTagsSample {
    public static void main(String[] args) {
        ConfidentialLedgerClient confidentialLedgerClient =
            new ConfidentialLedgerClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .ledgerEndpoint(Configuration.getGlobalConfiguration().get("LEDGERENDPOINT"))
                .buildClient();

        RequestOptions requestOptions = new RequestOptions()
            .addQueryParam("collectionId", "Collection2")
            .addQueryParam("tag", "tagA");
        PagedIterable<BinaryData> response = confidentialLedgerClient.listLedgerEntries(requestOptions);
    }
}
