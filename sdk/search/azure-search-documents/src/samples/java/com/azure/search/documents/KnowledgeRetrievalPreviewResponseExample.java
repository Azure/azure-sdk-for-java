// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.KnowledgeBase;
import com.azure.search.documents.indexes.models.KnowledgeSourceReference;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClientBuilder;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseMessage;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseMessageContent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseMessageTextContent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseModelAnswerSynthesisActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalOptions;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalResult;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalOutputMode;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalSemanticIntent;
import com.azure.search.documents.knowledgebases.models.PurviewSensitivityLabelInfo;

public class KnowledgeRetrievalPreviewResponseExample {
    private static final String ENDPOINT = System.getenv("SEARCH_ENDPOINT");
    private static final String API_KEY = System.getenv("SEARCH_API_KEY");
    private static final String KB_NAME = "my-knowledge-base";

    public static void main(String[] args) {
        SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .credential(new AzureKeyCredential(API_KEY))
            .endpoint(ENDPOINT)
            .buildClient();
        try {
            KnowledgeSourceReference knowledgeSource = new KnowledgeSourceReference("my-knowledge-source")
                .setEnableImageServing(true)
                .setEnableFreshness(true);

            KnowledgeBase knowledgeBase = new KnowledgeBase(KB_NAME, knowledgeSource);
            searchIndexClient.createOrUpdateKnowledgeBase(knowledgeBase);

            //build retrieval client
            KnowledgeBaseRetrievalClient retrievalClient = new KnowledgeBaseRetrievalClientBuilder()
                .credential(new AzureKeyCredential(API_KEY))
                .endpoint(ENDPOINT)
                .knowledgeBaseName(KB_NAME)
                .buildClient();

            //build retrieval request with preview options
            KnowledgeBaseRetrievalOptions options = new KnowledgeBaseRetrievalOptions()
                .setMaxOutputDocuments(5)
                .setIncludeActivity(true)
                .setOutputMode(KnowledgeRetrievalOutputMode.ANSWER_SYNTHESIS)
                .setIntents(new KnowledgeRetrievalSemanticIntent("What hotels have free wifi?"));

            //send request
            KnowledgeBaseRetrievalResult result = retrievalClient.retrieve(options);

            //parse result

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
                    + " elapsed=" + record.getElapsedMs() + "ms");
                if (record instanceof KnowledgeBaseModelAnswerSynthesisActivityRecord) {
                    KnowledgeBaseModelAnswerSynthesisActivityRecord synthesis =
                        (KnowledgeBaseModelAnswerSynthesisActivityRecord) record;
                    System.out.println("  Model: " + synthesis.getModelName());
                    System.out.println("  Input tokens: " + synthesis.getInputTokens());
                    System.out.println("  Output tokens: " + synthesis.getOutputTokens());
                }
            }

            for (KnowledgeBaseReference ref : result.getReferences()) {
                System.out.println("Reference id=" + ref.getId() + " type=" + ref.getType()
                    + " rerankerScore=" + ref.getRerankerScore());
                if (ref.getSourceData() != null) {
                    System.out.println("  Source data: " + ref.getSourceData().toString());
                }
            }

            PurviewSensitivityLabelInfo labelInfo = result.getResponseSensitivityLabelInfo();
            if (labelInfo != null) {
                System.out.println("Sensitivity Label " + labelInfo.getDisplayName());
                System.out.println("  Label ID: " + labelInfo.getSensitivityLabelId());
                System.out.println("  Priority: " + labelInfo.getPriority());
                System.out.println("  Color: " + labelInfo.getColor());
            }

        } finally {
            searchIndexClient.deleteKnowledgeBase(KB_NAME);
        }
    }

}
