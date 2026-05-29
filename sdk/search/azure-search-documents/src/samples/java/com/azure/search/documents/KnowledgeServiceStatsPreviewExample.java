// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.ResourceCounter;
import com.azure.search.documents.indexes.models.SearchServiceCounters;
import com.azure.search.documents.indexes.models.SearchServiceStatistics;

/**
 * Demonstrates retrieving service-level knowledge base and knowledge source counters
 * introduced in the preview API.
 *
 * These counters provide visibility into Knowledge Retrieval resource usage and quotas
 * on your Azure AI Search service.
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

        // New preview counters for Knowledge Retrieval objects
        ResourceCounter kbCounter = counters.getKnowledgeBaseCounter();
        ResourceCounter ksCounter = counters.getKnowledgeSourceCounter();

        System.out.println("Knowledge Bases  — usage: " + kbCounter.getUsage() + ", quota: " + kbCounter.getQuota());
        System.out.println("Knowledge Sources — usage: " + ksCounter.getUsage() + ", quota: " + ksCounter.getQuota());
    }
}
