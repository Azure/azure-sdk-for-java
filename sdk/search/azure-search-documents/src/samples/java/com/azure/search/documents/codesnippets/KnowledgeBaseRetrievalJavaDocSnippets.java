// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.codesnippets;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClientBuilder;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseMessage;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseMessageTextContent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalRequest;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalResponse;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalSemanticIntent;
import com.azure.search.documents.knowledgebases.models.SearchIndexKnowledgeSourceParams;

import java.util.Arrays;

@SuppressWarnings("unused")
public class KnowledgeBaseRetrievalJavaDocSnippets {

    private static KnowledgeBaseRetrievalClient retrievalClient;

    /**
     * Code snippet for creating a {@link KnowledgeBaseRetrievalClient}.
     */
    private static KnowledgeBaseRetrievalClient createRetrievalClient() {
        // BEGIN: com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient.instantiation
        KnowledgeBaseRetrievalClient retrievalClient = new KnowledgeBaseRetrievalClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
        // END: com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient.instantiation
        return retrievalClient;
    }

    /**
     * Code snippet for a simple retrieval using a user message.
     */
    public static void retrieve() {
        retrievalClient = createRetrievalClient();
        // BEGIN: com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient.retrieve#String-KnowledgeBaseRetrievalRequest
        KnowledgeBaseRetrievalRequest request = new KnowledgeBaseRetrievalRequest()
            .setMessages(new KnowledgeBaseMessage(
                new KnowledgeBaseMessageTextContent("What hotels are near the ocean?")));

        KnowledgeBaseRetrievalResponse response = retrievalClient.retrieve("my-knowledge-base", request);

        response.getResponse().forEach(message ->
            message.getContent().forEach(content -> {
                if (content instanceof KnowledgeBaseMessageTextContent) {
                    System.out.println(((KnowledgeBaseMessageTextContent) content).getText());
                }
            }));
        // END: com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient.retrieve#String-KnowledgeBaseRetrievalRequest
    }

    /**
     * Code snippet for retrieval using an explicit semantic intent (bypasses model query planning).
     */
    public static void retrieveWithIntent() {
        retrievalClient = createRetrievalClient();
        // BEGIN: com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient.retrieve.withIntent
        KnowledgeBaseRetrievalRequest request = new KnowledgeBaseRetrievalRequest()
            .setIntents(new KnowledgeRetrievalSemanticIntent("hotels near the ocean with free parking"));

        KnowledgeBaseRetrievalResponse response = retrievalClient.retrieve("my-knowledge-base", request);

        response.getResponse().forEach(message ->
            message.getContent().forEach(content -> {
                if (content instanceof KnowledgeBaseMessageTextContent) {
                    System.out.println(((KnowledgeBaseMessageTextContent) content).getText());
                }
            }));
        // END: com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient.retrieve.withIntent
    }

    /**
     * Code snippet for retrieval with runtime knowledge source params and references.
     */
    public static void retrieveWithSourceParamsAndReferences() {
        retrievalClient = createRetrievalClient();
        // BEGIN: com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient.retrieve.withSourceParams
        KnowledgeBaseRetrievalRequest request = new KnowledgeBaseRetrievalRequest()
            .setMessages(new KnowledgeBaseMessage(
                new KnowledgeBaseMessageTextContent("What hotels are available in Virginia?")))
            .setKnowledgeSourceParams(Arrays.asList(
                new SearchIndexKnowledgeSourceParams("my-knowledge-source")
                    .setFilterAddOn("Address/StateProvince eq 'VA'")
                    .setIncludeReferences(true)
                    .setIncludeReferenceSourceData(true)))
            .setIncludeActivity(true);

        KnowledgeBaseRetrievalResponse response = retrievalClient.retrieve("my-knowledge-base", request);

        // Print the assistant response
        response.getResponse().forEach(message ->
            message.getContent().forEach(content -> {
                if (content instanceof KnowledgeBaseMessageTextContent) {
                    System.out.println(((KnowledgeBaseMessageTextContent) content).getText());
                }
            }));

        // Print the source references
        for (KnowledgeBaseReference reference : response.getReferences()) {
            System.out.println("Reference [" + reference.getId() + "] score: " + reference.getRerankerScore());
        }
        // END: com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient.retrieve.withSourceParams
    }
}

