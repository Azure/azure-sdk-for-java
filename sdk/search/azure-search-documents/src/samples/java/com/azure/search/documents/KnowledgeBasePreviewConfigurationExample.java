// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.AzureOpenAIModelName;
import com.azure.search.documents.indexes.models.AzureOpenAIVectorizerParameters;
import com.azure.search.documents.indexes.models.CorsOptions;
import com.azure.search.documents.indexes.models.KnowledgeBase;
import com.azure.search.documents.indexes.models.KnowledgeBaseAzureOpenAIModel;
import com.azure.search.documents.indexes.models.KnowledgeSourceReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalMinimalReasoningEffort;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalOutputMode;

/**
 * This example shows how to create and update a knowledge base using preview-only configuration knobs.
 * <p>
 * It demonstrates:
 * <ul>
 *     <li>Setting GPT-5.x model names for query planning</li>
 *     <li>Configuring KB-level retrieval defaults (reasoning effort, output mode, instructions)</li>
 *     <li>CORS options</li>
 *     <li>Encryption key (CMK)</li>
 *     <li>Knowledge source with preview-relevant defaults (image serving, freshness)</li>
 *     <li>Updating an existing knowledge base</li>
 * </ul>
 * <p>
 * Set the following environment variables before running this sample:
 * <ul>
 *     <li>SEARCH_ENDPOINT - the endpoint of your Azure AI Search service</li>
 *     <li>SEARCH_API_KEY - the admin key of your Azure AI Search service</li>
 * </ul>
 */
public class KnowledgeBasePreviewConfigurationExample {

    private static final String ENDPOINT = System.getenv("SEARCH_ENDPOINT");
    private static final String API_KEY = System.getenv("SEARCH_API_KEY");
    private static final String KB_NAME = "my-knowledge-base";

    public static void main(String[] args) {
        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .credential(new AzureKeyCredential(API_KEY))
            .endpoint(ENDPOINT)
            .buildClient();

        try {
            // --- Create a knowledge base with preview configuration knobs ---

            // Knowledge source reference with preview-relevant defaults
            KnowledgeSourceReference knowledgeSource = new KnowledgeSourceReference("my-knowledge-source")
                .setEnableImageServing(true)
                .setEnableFreshness(true);

            KnowledgeBase knowledgeBase = new KnowledgeBase(KB_NAME, knowledgeSource);

            // GPT-5.x model for query planning
            knowledgeBase.setModels(
                new KnowledgeBaseAzureOpenAIModel(
                    new AzureOpenAIVectorizerParameters()
                        .setModelName(AzureOpenAIModelName.GPT54)
                        .setResourceUrl("https://my-openai-resource.openai.azure.com/")
                        .setDeploymentName("my-deployment")
                )
            );

            // KB-level retrieval defaults
            knowledgeBase.setRetrievalReasoningEffort(new KnowledgeRetrievalMinimalReasoningEffort());
            knowledgeBase.setOutputMode(KnowledgeRetrievalOutputMode.ANSWER_SYNTHESIS);
            knowledgeBase.setRetrievalInstructions("Focus on finding hotel listings with amenities and pricing information");
            knowledgeBase.setAnswerInstructions("Provide concise answers in bullet point format");

            // CORS and encryption
            knowledgeBase.setCorsOptions(new CorsOptions("https://my-allowed-origin.com").setMaxAgeInSeconds(3600L));
            // Uncomment below if your service has managed identity configured for Key Vault access:
            // knowledgeBase.setEncryptionKey(new SearchResourceEncryptionKey("my-key", "https://my-key-vault.vault.azure.net"));
            knowledgeBase.setDescription("Knowledge base with custom configuration for retrieval and answer generation");

            searchIndexClient.createOrUpdateKnowledgeBase(knowledgeBase);
            System.out.println("Knowledge base created.");

            // --- Update an existing knowledge base ---

            KnowledgeBase existingKb = searchIndexClient.getKnowledgeBase(KB_NAME);
            existingKb.setRetrievalInstructions("Updated: prioritize results from the last 30 days");
            existingKb.setAnswerInstructions("Updated: answer in full sentences with citations");
            existingKb.setOutputMode(KnowledgeRetrievalOutputMode.EXTRACTIVE_DATA);

            searchIndexClient.createOrUpdateKnowledgeBase(existingKb);
            System.out.println("Knowledge base updated.");
        } finally {
            searchIndexClient.deleteKnowledgeBase(KB_NAME);
        }
    }
}

