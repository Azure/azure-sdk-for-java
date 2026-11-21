// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.FabricDataAgentKnowledgeSource;
import com.azure.search.documents.indexes.models.FabricDataAgentKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.KnowledgeBase;
import com.azure.search.documents.indexes.models.KnowledgeSource;
import com.azure.search.documents.indexes.models.KnowledgeSourceReference;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClientBuilder;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalOptions;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalResult;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalSemanticIntent;

/**
 * Demonstrates creating and using a Fabric Data Agent knowledge source in the preview API.
 */
public class KnowledgeSourceFabricDataAgentPreviewExample {

    public static void main(String[] args) {
        String knowledgeBaseName = PreviewSampleSupport.name("java-dp11-fabric-agent-kb");
        String knowledgeSourceName = PreviewSampleSupport.name("java-dp11-fabric-agent-ks");
        String endpoint = System.getenv("SEARCH_ENDPOINT");
        String apiKey = System.getenv("SEARCH_API_KEY");
        String workspaceId = PreviewSampleSupport.required("FABRIC_WORKSPACE_ID");
        String dataAgentId = PreviewSampleSupport.required("FABRIC_DATA_AGENT_ID");

        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildClient();

        KnowledgeBaseRetrievalClient retrievalClient = new KnowledgeBaseRetrievalClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .knowledgeBaseName(knowledgeBaseName)
            .buildClient();

        try (PreviewSampleSupport.Resources resources = new PreviewSampleSupport.Resources(searchIndexClient)) {
            // Create KS — Fabric Data Agent requires a workspace ID and data agent ID
            FabricDataAgentKnowledgeSource knowledgeSource = new FabricDataAgentKnowledgeSource(
                knowledgeSourceName,
                new FabricDataAgentKnowledgeSourceParameters(workspaceId, dataAgentId)
            );
            resources.createKnowledgeSource(knowledgeSource);

            // Verify KS kind
            KnowledgeSource retrieved = searchIndexClient.getKnowledgeSource(knowledgeSourceName);
            System.out.println("KnowledgeSource kind = " + retrieved.getKind());

            // Hook up KS to a KB
            KnowledgeSourceReference ref = new KnowledgeSourceReference(knowledgeSourceName);
            KnowledgeBase knowledgeBase = new KnowledgeBase(knowledgeBaseName, ref);
            resources.createKnowledgeBase(knowledgeBase);
            System.out.println("Created KnowledgeBase " + knowledgeBaseName + " referencing " + knowledgeSourceName);

            // Issue retrieval request to verify everything is wired up end-to-end
            KnowledgeBaseRetrievalOptions options = new KnowledgeBaseRetrievalOptions()
                .setIntents(new KnowledgeRetrievalSemanticIntent("What data is the agent managing?"));
            KnowledgeBaseRetrievalResult result = retrievalClient.retrieve(options);
            System.out.println("Response messages: " + result.getResponse().size());

        }
    }
}
