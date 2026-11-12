// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClientBuilder;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseMessage;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseMessageContent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseMessageTextContent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseModelAnswerSynthesisActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseModelQueryPlanningActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseModelWebSummarizationActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityRecordModel;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseSearchIndexActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseSearchIndexReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseAzureBlobReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseIndexedOneLakeReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseIndexedSharePointReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseIndexedSqlReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseFileReference;
import com.azure.search.documents.knowledgebases.models.SearchIndexKnowledgeSourceParams;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalOptions;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalResult;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalOutputMode;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalSemanticIntent;

import java.util.Collections;

/**
 * Demonstrates messages, typed activity, citation URLs, and both retrieval output modes.
 * Set SEARCH_ENDPOINT, SEARCH_API_KEY, KNOWLEDGE_BASE_NAME, and KNOWLEDGE_SOURCE_NAME to existing resources.
 * The knowledge base must have a configured model for answer synthesis. This sample does not modify resources.
 */
public class KnowledgeRetrievalResponseExample {
    private static final String ENDPOINT = System.getenv("SEARCH_ENDPOINT");
    private static final String API_KEY = System.getenv("SEARCH_API_KEY");
    private static final String KB_NAME = System.getenv("KNOWLEDGE_BASE_NAME");

    public static void main(String[] args) {
        KnowledgeBaseRetrievalClient retrievalClient = new KnowledgeBaseRetrievalClientBuilder()
            .credential(new AzureKeyCredential(API_KEY))
            .endpoint(ENDPOINT)
            .knowledgeBaseName(KB_NAME)
            .buildClient();

        KnowledgeBaseRetrievalOptions options = new KnowledgeBaseRetrievalOptions()
            .setMaxOutputDocuments(5)
            .setIncludeActivity(true)
            .setOutputMode(KnowledgeRetrievalOutputMode.ANSWER_SYNTHESIS)
            .setIntents(new KnowledgeRetrievalSemanticIntent("What hotels have free wifi?"));

        inspectResponse(retrievalClient.retrieve(options));
        options.setOutputMode(KnowledgeRetrievalOutputMode.EXTRACTIVE_DATA)
            .setKnowledgeSourceParams(Collections.singletonList(
                new SearchIndexKnowledgeSourceParams(System.getenv("KNOWLEDGE_SOURCE_NAME"))
                    .setIncludeReferences(true).setIncludeReferenceSourceData(true)));
        inspectResponse(retrievalClient.retrieve(options));
    }

    static void inspectResponse(KnowledgeBaseRetrievalResult result) {
        for (KnowledgeBaseMessage message : result.getResponse()) {
            System.out.println("Message role: " + message.getRole());
            for (KnowledgeBaseMessageContent content : message.getContent()) {
                if (content instanceof KnowledgeBaseMessageTextContent) {
                    System.out.println("Text content: " + ((KnowledgeBaseMessageTextContent) content).getText());
                }
            }
        }

        for (KnowledgeBaseActivityRecord record : result.getActivity()) {
            System.out.println("Activity [" + record.getType() + "] id=" + record.getId()
                + " elapsed=" + record.getElapsedMs() + "ms"
                + " started=" + record.getStartedAt() + " completed=" + record.getCompletedAt()
                + " warning=" + record.getWarning());
            if (record instanceof KnowledgeBaseSearchIndexActivityRecord) {
                KnowledgeBaseSearchIndexActivityRecord search = (KnowledgeBaseSearchIndexActivityRecord) record;
                System.out.println("  Source: " + search.getKnowledgeSourceName() + ", documents: " + search.getCount());
            }
            if (record instanceof KnowledgeBaseModelAnswerSynthesisActivityRecord) {
                KnowledgeBaseModelAnswerSynthesisActivityRecord synthesis =
                    (KnowledgeBaseModelAnswerSynthesisActivityRecord) record;
                printModel(synthesis.getModel());
                System.out.println("  Input tokens: " + synthesis.getInputTokens());
                System.out.println("  Output tokens: " + synthesis.getOutputTokens());
            }
            if (record instanceof KnowledgeBaseModelQueryPlanningActivityRecord) {
                KnowledgeBaseModelQueryPlanningActivityRecord planning =
                    (KnowledgeBaseModelQueryPlanningActivityRecord) record;
                printModel(planning.getModel());
                System.out.println("  Planning tokens: " + planning.getInputTokens() + "/" + planning.getOutputTokens());
            }
            if (record instanceof KnowledgeBaseModelWebSummarizationActivityRecord) {
                printModel(((KnowledgeBaseModelWebSummarizationActivityRecord) record).getModel());
            }
        }

        for (KnowledgeBaseReference ref : result.getReferences()) {
            System.out.println("Reference id=" + ref.getId() + " type=" + ref.getType()
                + " rerankerScore=" + ref.getRerankerScore());
            System.out.println("  Search-owned citation URL: " + citationUrl(ref));
            if (ref.getSourceData() != null) {
                System.out.println("  Source data: " + ref.getSourceData().toString());
            }
        }
    }

    private static void printModel(KnowledgeBaseActivityRecordModel model) {
        if (model != null) {
            System.out.println("  Model: " + model.getModelName() + ", deployment: " + model.getDeploymentId());
        }
    }

    static String citationUrl(KnowledgeBaseReference reference) {
        if (reference instanceof KnowledgeBaseSearchIndexReference) {
            return ((KnowledgeBaseSearchIndexReference) reference).getCitationUrl();
        } else if (reference instanceof KnowledgeBaseAzureBlobReference) {
            return ((KnowledgeBaseAzureBlobReference) reference).getCitationUrl();
        } else if (reference instanceof KnowledgeBaseIndexedOneLakeReference) {
            return ((KnowledgeBaseIndexedOneLakeReference) reference).getCitationUrl();
        } else if (reference instanceof KnowledgeBaseIndexedSharePointReference) {
            return ((KnowledgeBaseIndexedSharePointReference) reference).getCitationUrl();
        } else if (reference instanceof KnowledgeBaseIndexedSqlReference) {
            return ((KnowledgeBaseIndexedSqlReference) reference).getCitationUrl();
        } else if (reference instanceof KnowledgeBaseFileReference) {
            return ((KnowledgeBaseFileReference) reference).getCitationUrl();
        }
        return null;
    }
}
