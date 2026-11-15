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
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSource;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.SemanticConfiguration;
import com.azure.search.documents.indexes.models.SemanticField;
import com.azure.search.documents.indexes.models.SemanticPrioritizedFields;
import com.azure.search.documents.indexes.models.SemanticSearch;
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
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexDocumentsBatch;
import com.azure.search.documents.models.IndexDocumentsResult;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Demonstrates knowledge-base and request-level retrieval configuration precedence.
 *
 * <p>Set {@code SEARCH_ENDPOINT}, {@code SEARCH_API_KEY}, {@code SEARCH_OPENAI_ENDPOINT},
 * {@code SEARCH_OPENAI_API_KEY}, {@code SEARCH_OPENAI_DEPLOYMENT_NAME}, and {@code SEARCH_OPENAI_MODEL_NAME}. The
 * deployed model must support automatic reasoning.</p>
 */
public class KnowledgeBasePreviewConfigurationExample {
    private static final String SEMANTIC_CONFIGURATION_NAME = "sample-semantic-config";

    public static void main(String[] args) {
        String endpoint = System.getenv("SEARCH_ENDPOINT");
        AzureKeyCredential credential = new AzureKeyCredential(System.getenv("SEARCH_API_KEY"));
        SearchIndexClient searchIndexClient
            = new SearchIndexClientBuilder().credential(credential).endpoint(endpoint).buildClient();

        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String indexName = "kb-config-index-" + suffix;
        String knowledgeSourceName = "kb-config-source-" + suffix;
        String knowledgeBaseName = "kb-config-" + suffix;
        boolean indexCreated = false;
        boolean knowledgeSourceCreated = false;
        boolean knowledgeBaseCreated = false;

        try {
            searchIndexClient.createIndex(createSampleIndex(indexName));
            indexCreated = true;
            uploadSampleDocument(searchIndexClient.getSearchClient(indexName));

            SearchIndexKnowledgeSource knowledgeSource = new SearchIndexKnowledgeSource(knowledgeSourceName,
                new SearchIndexKnowledgeSourceParameters(indexName)
                    .setSemanticConfigurationName(SEMANTIC_CONFIGURATION_NAME));
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
            if (indexCreated) {
                searchIndexClient.deleteIndex(indexName);
            }
        }
    }

    private static SearchIndex createSampleIndex(String indexName) {
        return new SearchIndex(indexName,
            Arrays.asList(new SearchField("id", SearchFieldDataType.STRING).setKey(true),
                new SearchField("title", SearchFieldDataType.STRING).setSearchable(true),
                new SearchField("content", SearchFieldDataType.STRING).setSearchable(true),
                new SearchField("category", SearchFieldDataType.STRING).setSearchable(true).setFilterable(true)))
                    .setSemanticSearch(new SemanticSearch().setConfigurations(new SemanticConfiguration(
                        SEMANTIC_CONFIGURATION_NAME,
                        new SemanticPrioritizedFields().setTitleField(new SemanticField("title"))
                            .setContentFields(new SemanticField("content"))
                            .setKeywordsFields(new SemanticField("category")))));
    }

    private static void uploadSampleDocument(SearchClient searchClient) {
        Map<String, Object> document = new LinkedHashMap<>();
        document.put("id", "1");
        document.put("title", "August product update");
        document.put("content", "The latest product update adds knowledge base reasoning improvements.");
        document.put("category", "Product update");
        IndexDocumentsResult result = searchClient.indexDocuments(new IndexDocumentsBatch(
            new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(document)));
        if (result.getResults().size() != 1 || !result.getResults().get(0).isSucceeded()) {
            throw new IllegalStateException("The sample document wasn't indexed successfully.");
        }

        for (int attempt = 0; attempt < 30; attempt++) {
            if (searchClient.getDocumentCount() > 0) {
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for the sample document.", ex);
            }
        }
        throw new IllegalStateException("The sample document wasn't available for retrieval.");
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
