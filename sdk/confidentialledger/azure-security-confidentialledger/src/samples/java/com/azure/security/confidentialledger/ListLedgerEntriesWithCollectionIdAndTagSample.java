// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.confidentialledger;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * Sample for listing ledger entries with collectionId and tags filters.
 */
public class ListLedgerEntriesWithCollectionIdAndTagSample {
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
