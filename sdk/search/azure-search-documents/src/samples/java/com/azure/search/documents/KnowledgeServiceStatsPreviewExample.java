// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.ResourceCounter;
import com.azure.search.documents.indexes.models.SearchServiceCounters;
import com.azure.search.documents.indexes.models.SearchServiceLimits;
import com.azure.search.documents.indexes.models.SearchServiceStatistics;

/**
 * Demonstrates retrieving service-level knowledge resource counters and the per-index vector-size limit introduced in
 * the preview API.
 *
 * The counters describe service usage and quotas. {@code maxVectorIndexSizePerIndexInBytes} is a separate limit that
 * applies to each index; it isn't current vector usage or a per-partition quota.
 */
public class KnowledgeServiceStatsPreviewExample {

    public static void main(String[] args) {
        SearchIndexClient client = new SearchIndexClientBuilder()
            .endpoint(System.getenv("SEARCH_ENDPOINT"))
            .credential(new AzureKeyCredential(System.getenv("SEARCH_API_KEY")))
            .buildClient();

        // Retrieve service statistics
        SearchServiceStatistics stats = client.getServiceStatistics();
        SearchServiceCounters counters = stats.getCounters();
        SearchServiceLimits limits = stats.getLimits();

        // New preview counters for Knowledge Retrieval objects
        ResourceCounter kbCounter = counters.getKnowledgeBaseCounter();
        ResourceCounter ksCounter = counters.getKnowledgeSourceCounter();

        System.out.println("Knowledge Bases - usage: " + kbCounter.getUsage() + ", quota: " + kbCounter.getQuota());
        System.out.println("Knowledge Sources - usage: " + ksCounter.getUsage() + ", quota: " + ksCounter.getQuota());

        Long maxVectorIndexSize = limits == null ? null : limits.getMaxVectorIndexSizePerIndexInBytes();
        if (maxVectorIndexSize == null) {
            System.out.println("The service didn't report a per-index vector-size limit.");
        } else {
            System.out.println("Maximum vector index size per index: " + maxVectorIndexSize + " bytes");
        }
    }
}
