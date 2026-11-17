// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.FabricOntologyKnowledgeSource;
import com.azure.search.documents.indexes.models.FabricOntologyKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.KnowledgeBase;
import com.azure.search.documents.indexes.models.KnowledgeSource;
import com.azure.search.documents.indexes.models.KnowledgeSourceReference;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClientBuilder;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalOptions;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalResult;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalSemanticIntent;

/**
 * Demonstrates creating and using a Fabric Ontology knowledge source in the preview API.
 */
public class KnowledgeSourceFabricOntologyPreviewExample {

    public static void main(String[] args) {
        String knowledgeBaseName = PreviewSampleSupport.name("java-dp11-fabric-ontology-kb");
        String knowledgeSourceName = PreviewSampleSupport.name("java-dp11-fabric-ontology-ks");
        String endpoint = System.getenv("SEARCH_ENDPOINT");
        String apiKey = System.getenv("SEARCH_API_KEY");
        String workspaceId = PreviewSampleSupport.required("FABRIC_WORKSPACE_ID");
        String ontologyId = PreviewSampleSupport.required("FABRIC_ONTOLOGY_ID");

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
            // Create KS — Fabric Ontology requires a workspace ID and ontology ID
            FabricOntologyKnowledgeSource knowledgeSource = new FabricOntologyKnowledgeSource(
                knowledgeSourceName,
                new FabricOntologyKnowledgeSourceParameters(workspaceId, ontologyId)
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
                .setIntents(new KnowledgeRetrievalSemanticIntent("What entities are in the ontology?"));
            KnowledgeBaseRetrievalResult result = retrievalClient.retrieve(options);
            System.out.println("Response messages: " + result.getResponse().size());

        }
    }
}
