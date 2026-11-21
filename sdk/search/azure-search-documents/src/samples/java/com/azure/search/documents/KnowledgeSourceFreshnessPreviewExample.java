// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.AzureOpenAIModelName;
import com.azure.search.documents.indexes.models.AzureOpenAIVectorizerParameters;
import com.azure.search.documents.indexes.models.KnowledgeBase;
import com.azure.search.documents.indexes.models.KnowledgeBaseAzureOpenAIModel;
import com.azure.search.documents.indexes.models.KnowledgeSourceReference;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSource;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceParameters;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClientBuilder;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalOptions;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalResult;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalMinimalReasoningEffort;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalOutputMode;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalSemanticIntent;

/**
 * Demonstrates how persisted retrieval defaults and freshness-aware behavior work together
 * for indexed knowledge sources in the preview API.
 *
 * <p>Key concepts shown:</p>
 * <ul>
 *   <li>Setting enableFreshness on a KnowledgeSourceReference so retrieval prefers recent documents</li>
 *   <li>Persisting retrieval defaults (outputMode, retrievalReasoningEffort, retrievalInstructions) on the KB</li>
 *   <li>Issuing a retrieve call that inherits those defaults without re-specifying them</li>
 * </ul>
 */
public class KnowledgeSourceFreshnessPreviewExample {

    public static void main(String[] args) {
        String knowledgeBaseName = PreviewSampleSupport.name("java-dp11-freshness-kb");
        String knowledgeSourceName = PreviewSampleSupport.name("java-dp11-freshness-ks");
        String endpoint = System.getenv("SEARCH_ENDPOINT");
        String apiKey = System.getenv("SEARCH_API_KEY");
        String indexName = PreviewSampleSupport.required("SEARCH_INDEX_NAME");
        String aoaiEndpoint = System.getenv("SEARCH_OPENAI_ENDPOINT");
        String aoaiDeployment = System.getenv("SEARCH_OPENAI_DEPLOYMENT_NAME");

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
            // Step 1: Create an indexed knowledge source backed by a search index.
            SearchIndexKnowledgeSource knowledgeSource = new SearchIndexKnowledgeSource(
                knowledgeSourceName,
                new SearchIndexKnowledgeSourceParameters(indexName)
                    .setSemanticConfigurationName(
                        PreviewSampleSupport.optional("SEARCH_SEMANTIC_CONFIGURATION", "default"))
            );
            resources.createKnowledgeSource(knowledgeSource);

            // Step 2: Reference the source with enableFreshness = true.
            // This tells the service to prefer recently-updated documents during retrieval.
            KnowledgeSourceReference sourceRef = new KnowledgeSourceReference(knowledgeSourceName)
                .setEnableFreshness(true);

            // Step 3: Create the knowledge base with persisted retrieval defaults.
            // These defaults apply to every retrieve call unless overridden at query time.
            KnowledgeBase knowledgeBase = new KnowledgeBase(knowledgeBaseName, sourceRef)
                .setModels(new KnowledgeBaseAzureOpenAIModel(
                    new AzureOpenAIVectorizerParameters()
                        .setResourceUrl(aoaiEndpoint)
                        .setDeploymentName(aoaiDeployment)
                        .setApiKey(System.getenv("SEARCH_OPENAI_API_KEY"))
                        .setModelName(AzureOpenAIModelName.fromString(
                            PreviewSampleSupport.required("SEARCH_OPENAI_MODEL_NAME")))))
                .setOutputMode(KnowledgeRetrievalOutputMode.EXTRACTIVE_DATA)
                .setRetrievalReasoningEffort(new KnowledgeRetrievalMinimalReasoningEffort())
                .setRetrievalInstructions("Prefer documents updated in the last 7 days.");

            resources.createKnowledgeBase(knowledgeBase);
            System.out.println("Created KB with freshness enabled and persisted defaults.");

            // Step 4: Retrieve — pass only the query. Defaults from the KB are applied automatically.
            KnowledgeBaseRetrievalOptions options = new KnowledgeBaseRetrievalOptions()
                .setIntents(new KnowledgeRetrievalSemanticIntent("What are the latest updates?"));

            KnowledgeBaseRetrievalResult result = retrievalClient.retrieve(options);
            System.out.println("Response messages: " + result.getResponse().size());
            System.out.println("Activity records: "
                + (result.getActivity() != null ? result.getActivity().size() : 0));


        }
    }
}
