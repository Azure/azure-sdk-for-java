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
import com.azure.search.documents.indexes.models.KnowledgeBaseRetrieveDefaults;
import com.azure.search.documents.indexes.models.KnowledgeSourceReference;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSource;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceParameters;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClientBuilder;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseAgenticReasoningActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalOptions;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalResult;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalAutoReasoningEffort;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalLowReasoningEffort;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalOutputMode;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalReasoningEffortKind;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalSemanticIntent;

import java.util.UUID;

/**
 * Demonstrates knowledge-base and request-level retrieval configuration precedence.
 *
 * <p>Set {@code SEARCH_ENDPOINT}, {@code SEARCH_API_KEY}, {@code SEARCH_INDEX_NAME},
 * {@code SEARCH_SEMANTIC_CONFIGURATION_NAME}, {@code SEARCH_OPENAI_ENDPOINT},
 * {@code SEARCH_OPENAI_API_KEY}, {@code SEARCH_OPENAI_DEPLOYMENT_NAME}, and
 * {@code SEARCH_OPENAI_MODEL_NAME}. The deployed model must support automatic reasoning.</p>
 */
public class KnowledgeBasePreviewConfigurationExample {
    public static void main(String[] args) {
        String endpoint = System.getenv("SEARCH_ENDPOINT");
        AzureKeyCredential credential = new AzureKeyCredential(System.getenv("SEARCH_API_KEY"));
        SearchIndexClient searchIndexClient
            = new SearchIndexClientBuilder().credential(credential).endpoint(endpoint).buildClient();

        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String knowledgeSourceName = "kb-config-source-" + suffix;
        String knowledgeBaseName = "kb-config-" + suffix;
        boolean knowledgeSourceCreated = false;
        boolean knowledgeBaseCreated = false;

        try {
            SearchIndexKnowledgeSource knowledgeSource = new SearchIndexKnowledgeSource(knowledgeSourceName,
                new SearchIndexKnowledgeSourceParameters(System.getenv("SEARCH_INDEX_NAME"))
                    .setSemanticConfigurationName(System.getenv("SEARCH_SEMANTIC_CONFIGURATION_NAME")));
            searchIndexClient.createKnowledgeSource(knowledgeSource);
            knowledgeSourceCreated = true;

            KnowledgeBase knowledgeBase
                = new KnowledgeBase(knowledgeBaseName, new KnowledgeSourceReference(knowledgeSourceName))
                    .setModels(new KnowledgeBaseAzureOpenAIModel(new AzureOpenAIVectorizerParameters()
                        .setResourceUrl(System.getenv("SEARCH_OPENAI_ENDPOINT"))
                        .setApiKey(System.getenv("SEARCH_OPENAI_API_KEY"))
                        .setDeploymentName(System.getenv("SEARCH_OPENAI_DEPLOYMENT_NAME"))
                        .setModelName(AzureOpenAIModelName.fromString(System.getenv("SEARCH_OPENAI_MODEL_NAME")))))
                    .setOutputMode(KnowledgeRetrievalOutputMode.ANSWER_SYNTHESIS);
            searchIndexClient.createKnowledgeBase(knowledgeBase);
            knowledgeBaseCreated = true;

            KnowledgeBaseRetrievalClient retrievalClient = new KnowledgeBaseRetrievalClientBuilder().endpoint(endpoint)
                .credential(credential)
                .knowledgeBaseName(knowledgeBaseName)
                .buildClient();

            // With neither a request nor KB reasoning setting, the service default is low.
            verifyReasoningEffort(retrievalClient.retrieve(createRequest()), KnowledgeRetrievalReasoningEffortKind.LOW);

            KnowledgeBaseRetrieveDefaults retrieveDefaults = new KnowledgeBaseRetrieveDefaults()
                .setMaxRuntimeInSeconds(30)
                .setMaxOutputDocuments(5)
                .setMaxOutputSizeInTokens(6000);
            knowledgeBase.setRetrievalReasoningEffort(new KnowledgeRetrievalAutoReasoningEffort())
                .setRetrieveDefaults(retrieveDefaults);
            searchIndexClient.createOrUpdateKnowledgeBase(knowledgeBase);

            KnowledgeBase persistedKnowledgeBase = searchIndexClient.getKnowledgeBase(knowledgeBaseName);
            if (!(persistedKnowledgeBase.getRetrievalReasoningEffort() instanceof KnowledgeRetrievalAutoReasoningEffort)
                || persistedKnowledgeBase.getRetrieveDefaults() == null
                || !Integer.valueOf(6000)
                    .equals(persistedKnowledgeBase.getRetrieveDefaults().getMaxOutputSizeInTokens())) {
                throw new IllegalStateException("The KB reasoning effort or retrieve defaults weren't persisted.");
            }

            // When the request omits these values, KB auto reasoning and retrieveDefaults apply. Auto chooses the
            // effective billing effort reported by the activity record.
            KnowledgeBaseAgenticReasoningActivityRecord automaticReasoning
                = findReasoningActivity(retrievalClient.retrieve(createRequest()));
            if (automaticReasoning.getRetrievalReasoningEffort() == null) {
                throw new IllegalStateException("Automatic reasoning didn't report an effective reasoning effort.");
            }

            // Request values override the KB values. Note the intentionally different property names:
            // retrieveDefaults.maxOutputSizeInTokens versus request.maxOutputSize.
            KnowledgeBaseRetrievalOptions requestOverride = createRequest()
                .setRetrievalReasoningEffort(new KnowledgeRetrievalLowReasoningEffort())
                .setMaxOutputDocuments(2)
                .setMaxOutputSize(5000);
            verifyReasoningEffort(retrievalClient.retrieve(requestOverride), KnowledgeRetrievalReasoningEffortKind.LOW);

            KnowledgeBase unchangedKnowledgeBase = searchIndexClient.getKnowledgeBase(knowledgeBaseName);
            if (!(unchangedKnowledgeBase.getRetrievalReasoningEffort()
                instanceof KnowledgeRetrievalAutoReasoningEffort)
                || !Integer.valueOf(6000)
                    .equals(unchangedKnowledgeBase.getRetrieveDefaults().getMaxOutputSizeInTokens())) {
                throw new IllegalStateException("Request overrides must not modify persisted KB configuration.");
            }
            System.out.println("Verified request > knowledge base > service-default precedence.");
        } finally {
            if (knowledgeBaseCreated) {
                searchIndexClient.deleteKnowledgeBase(knowledgeBaseName);
            }
            if (knowledgeSourceCreated) {
                searchIndexClient.deleteKnowledgeSource(knowledgeSourceName);
            }
        }
    }

    private static KnowledgeBaseRetrievalOptions createRequest() {
        return new KnowledgeBaseRetrievalOptions()
            .setIntents(new KnowledgeRetrievalSemanticIntent("What are the latest product updates?"))
            .setIncludeActivity(true);
    }

    private static void verifyReasoningEffort(KnowledgeBaseRetrievalResult result,
        KnowledgeRetrievalReasoningEffortKind expectedKind) {
        KnowledgeBaseAgenticReasoningActivityRecord activity = findReasoningActivity(result);
        if (activity.getRetrievalReasoningEffort() == null
            || !expectedKind.equals(activity.getRetrievalReasoningEffort().getKind())) {
            throw new IllegalStateException("The retrieval didn't use the expected reasoning effort.");
        }
    }

    private static KnowledgeBaseAgenticReasoningActivityRecord
        findReasoningActivity(KnowledgeBaseRetrievalResult result) {
        if (result.getActivity() == null) {
            throw new IllegalStateException("The retrieval response didn't contain activity records.");
        }
        return result.getActivity()
            .stream()
            .filter(KnowledgeBaseAgenticReasoningActivityRecord.class::isInstance)
            .map(KnowledgeBaseAgenticReasoningActivityRecord.class::cast)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("The response didn't contain agentic reasoning activity."));
    }
}
