// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClientBuilder;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseMessageTextContent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseMessage;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseMessageContent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseMessageImageContent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseImageContent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalOptions;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalResult;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalSemanticIntent;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalLowReasoningEffort;
import com.azure.search.documents.knowledgebases.models.SearchIndexKnowledgeSourceParams;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceParams;
import com.azure.search.documents.knowledgebases.models.FileKnowledgeSourceParams;
import com.azure.search.documents.knowledgebases.models.IndexedSqlKnowledgeSourceParams;
import com.azure.search.documents.knowledgebases.models.IndexedSharePointKnowledgeSourceParams;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * This example demonstrates end-to-end knowledge base retrieval using the KnowledgeBaseRetrievalClient.
 * <p>
 * It demonstrates:
 * <ul>
 *     <li>Simple retrieval with a semantic intent</li>
 *     <li>Retrieval with explicit semantic intent (bypasses model query planning)</li>
 *     <li>Retrieval with runtime knowledge source params, filters, and references</li>
 * </ul>
 * <p>
 * Set the following environment variables before running this sample:
 * <ul>
 *     <li>SEARCH_ENDPOINT - the endpoint of your Azure AI Search service</li>
 *     <li>SEARCH_API_KEY - the admin key of your Azure AI Search service</li>
 *     <li>KNOWLEDGE_BASE_NAME - the name of an existing knowledge base</li>
 *     <li>KNOWLEDGE_SOURCE_NAME - the name of its populated source when passing file, sql, or sharepoint</li>
 *     <li>SEARCH_IMAGE_URL - optional image URL for a knowledge base with an image-capable model</li>
 * </ul>
 */
public class KnowledgeRetrievalExample {

    private static final String ENDPOINT = System.getenv("SEARCH_ENDPOINT");
    private static final String API_KEY = System.getenv("SEARCH_API_KEY");
    private static final String KB_NAME = System.getenv("KNOWLEDGE_BASE_NAME");

    public static void main(String[] args) {
        KnowledgeBaseRetrievalClient retrievalClient = new KnowledgeBaseRetrievalClientBuilder()
            .credential(new AzureKeyCredential(API_KEY))
            .endpoint(ENDPOINT)
            .knowledgeBaseName(KB_NAME)
            .buildClient();

        if (args.length > 0) {
            KnowledgeSourceParams source = sourceParameters(args[0], System.getenv("KNOWLEDGE_SOURCE_NAME"));
            retrievalClient.retrieve(new KnowledgeBaseRetrievalOptions()
                .setIntents(new KnowledgeRetrievalSemanticIntent("Summarize the latest changes."))
                .setKnowledgeSourceParams(Arrays.asList(source.setIncludeReferences(true).setIncludeReferenceSourceData(true)))
                .setIncludeActivity(true)).getReferences()
                .forEach(reference -> System.out.println("Source reference: " + reference.getId()));
            return;
        }

        // Simple retrieval with a semantic intent
        System.out.println("=== Simple Retrieval ===");
        simpleRetrieval(retrievalClient);

        // Retrieval with explicit semantic intent
        System.out.println("\n=== Retrieval with Explicit Intent ===");
        retrievalWithIntent(retrievalClient);

        // Retrieval with source params, filters, and references
        System.out.println("\n=== Retrieval with Source Params and References ===");
        retrievalWithSourceParams(retrievalClient);

        System.out.println("\n=== Retrieval with Conversation History ===");
        retrievalClient.retrieve(conversationRequest(System.getenv("SEARCH_IMAGE_URL"))).getResponse().forEach(message ->
            message.getContent().forEach(content -> {
                if (content instanceof KnowledgeBaseMessageTextContent) {
                    System.out.println(((KnowledgeBaseMessageTextContent) content).getText());
                }
            }));
    }

    private static void simpleRetrieval(KnowledgeBaseRetrievalClient retrievalClient) {
        KnowledgeBaseRetrievalOptions request = new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("What hotels are near the ocean?"));

        KnowledgeBaseRetrievalResult response = retrievalClient.retrieve(request);

        response.getResponse().forEach(message ->
            message.getContent().forEach(content -> {
                if (content instanceof KnowledgeBaseMessageTextContent) {
                    System.out.println(((KnowledgeBaseMessageTextContent) content).getText());
                }
            }));
    }

    private static void retrievalWithIntent(KnowledgeBaseRetrievalClient retrievalClient) {
        KnowledgeBaseRetrievalOptions request = new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("hotels near the ocean with free parking"));

        KnowledgeBaseRetrievalResult response = retrievalClient.retrieve(request);

        response.getResponse().forEach(message ->
            message.getContent().forEach(content -> {
                if (content instanceof KnowledgeBaseMessageTextContent) {
                    System.out.println(((KnowledgeBaseMessageTextContent) content).getText());
                }
            }));
    }

    private static void retrievalWithSourceParams(KnowledgeBaseRetrievalClient retrievalClient) {
        KnowledgeBaseRetrievalOptions request = new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("What hotels are available in Virginia?"))
            .setKnowledgeSourceParams(Arrays.asList(
                new SearchIndexKnowledgeSourceParams("my-knowledge-source")
                    .setFilterAddOn("Address/StateProvince eq 'VA'")
                    .setIncludeReferences(true)
                    .setIncludeReferenceSourceData(true)
                    .setAlwaysQuerySource(true)
                    .setFailOnError(true)
                    .setMaxOutputDocuments(3)))
            .setIncludeActivity(true);

        KnowledgeBaseRetrievalResult response = retrievalClient.retrieve(request);

        // Print the assistant response
        response.getResponse().forEach(message ->
            message.getContent().forEach(content -> {
                if (content instanceof KnowledgeBaseMessageTextContent) {
                    System.out.println(((KnowledgeBaseMessageTextContent) content).getText());
                }
            }));

        // Print the source references
        System.out.println("\nReferences:");
        for (KnowledgeBaseReference reference : response.getReferences()) {
            System.out.println("  Reference [" + reference.getId() + "] score: " + reference.getRerankerScore());
        }
    }

    static KnowledgeBaseRetrievalOptions conversationRequest(String imageUrl) {
        List<KnowledgeBaseMessageContent> followUp = new ArrayList<>();
        followUp.add(new KnowledgeBaseMessageTextContent("Yes, and free wifi."));
        if (imageUrl != null) {
            followUp.add(new KnowledgeBaseMessageImageContent(new KnowledgeBaseImageContent(imageUrl)));
        }
        return new KnowledgeBaseRetrievalOptions()
            .setMessages(Arrays.asList(
                new KnowledgeBaseMessage(new KnowledgeBaseMessageTextContent("Find hotels near the ocean."))
                    .setRole("user"),
                new KnowledgeBaseMessage(new KnowledgeBaseMessageTextContent("Do you need parking?"))
                    .setRole("assistant"),
                new KnowledgeBaseMessage(followUp).setRole("user")))
            .setRetrievalReasoningEffort(new KnowledgeRetrievalLowReasoningEffort())
            .setMaxOutputDocuments(5)
            .setIncludeActivity(true);
    }

    static KnowledgeSourceParams sourceParameters(String kind, String name) {
        switch (kind) {
            case "file":
                return new FileKnowledgeSourceParams(name).setAlwaysQuerySource(true).setMaxOutputDocuments(3);
            case "sql":
                return new IndexedSqlKnowledgeSourceParams(name).setAlwaysQuerySource(true).setMaxOutputDocuments(3);
            case "sharepoint":
                return new IndexedSharePointKnowledgeSourceParams(name).setAlwaysQuerySource(true).setMaxOutputDocuments(3);
            default:
                throw new IllegalArgumentException("Expected file, sql, sharepoint, or no arguments.");
        }
    }
}
