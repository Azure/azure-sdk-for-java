// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.KnowledgeBase;
import com.azure.search.documents.indexes.models.KnowledgeSource;
import com.azure.search.documents.indexes.models.KnowledgeSourceReference;
import com.azure.search.documents.indexes.models.McpServerKnowledgeSource;
import com.azure.search.documents.indexes.models.McpServerKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.McpServerTool;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClientBuilder;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalOptions;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalResult;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalSemanticIntent;

import java.util.Arrays;

/**
 * Demonstrates creating and using an MCP Server knowledge source in the preview API.
 */
public class KnowledgeSourceMcpServerPreviewExample {

    private static final String KB_NAME = "mcp-server-kind-sample-kb";
    private static final String KS_NAME = "mcp-server-kind-sample-ks";

    public static void main(String[] args) {
        String endpoint = System.getenv("SEARCH_ENDPOINT");
        String apiKey = System.getenv("SEARCH_API_KEY");

        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildClient();

        KnowledgeBaseRetrievalClient retrievalClient = new KnowledgeBaseRetrievalClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .knowledgeBaseName(KB_NAME)
            .buildClient();

        try {
            // Create KS — MCP Server requires a server URL and a list of tools
            McpServerKnowledgeSource knowledgeSource = new McpServerKnowledgeSource(
                KS_NAME,
                new McpServerKnowledgeSourceParameters(
                    "https://my-mcp-server.example.com/sse",
                    Arrays.asList(
                        new McpServerTool().setName("search-tool").setMaxOutputTokens(2000),
                        new McpServerTool().setName("lookup-tool").setMaxOutputTokens(1000)
                    )
                )
            );
            searchIndexClient.createOrUpdateKnowledgeSource(knowledgeSource);

            // Verify KS kind
            KnowledgeSource retrieved = searchIndexClient.getKnowledgeSource(KS_NAME);
            System.out.println("KnowledgeSource kind = " + retrieved.getKind());

            // Hook up KS to a KB
            KnowledgeSourceReference ref = new KnowledgeSourceReference(KS_NAME);
            KnowledgeBase knowledgeBase = new KnowledgeBase(KB_NAME, ref);
            searchIndexClient.createOrUpdateKnowledgeBase(knowledgeBase);
            System.out.println("Created KnowledgeBase " + KB_NAME + " referencing " + KS_NAME);

            // Issue retrieval request to verify everything is wired up end-to-end
            KnowledgeBaseRetrievalOptions options = new KnowledgeBaseRetrievalOptions()
                .setIntents(new KnowledgeRetrievalSemanticIntent("What tools are available?"));
            KnowledgeBaseRetrievalResult result = retrievalClient.retrieve(options);
            System.out.println("Response messages: " + result.getResponse().size());

        } finally {
            searchIndexClient.deleteKnowledgeBase(KB_NAME);
            searchIndexClient.deleteKnowledgeSource(KS_NAME);
        }
    }
}
