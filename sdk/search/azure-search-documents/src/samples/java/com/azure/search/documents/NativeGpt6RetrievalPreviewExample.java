// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.models.AzureOpenAIModelName;
import com.azure.search.documents.indexes.models.AzureOpenAIVectorizerParameters;
import com.azure.search.documents.indexes.models.KnowledgeBase;
import com.azure.search.documents.indexes.models.KnowledgeBaseAzureOpenAIModel;
import com.azure.search.documents.indexes.models.KnowledgeSourceReference;
import com.azure.search.documents.indexes.models.SearchIndexFieldReference;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSource;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceParameters;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalResult;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseSearchIndexReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalLowReasoningEffort;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalOutputMode;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Retrieves seeded hotel evidence using a native Azure OpenAI GPT-6 model with the November 2026 preview.
 *
 * <p>Set {@code SEARCH_ENDPOINT}, {@code SEARCH_API_KEY}, {@code SEARCH_OPENAI_ENDPOINT} (the Azure OpenAI
 * resource root, not a Responses URL), and {@code SEARCH_GPT6_DEPLOYMENT_NAME} to an existing deployment.
 * {@code SEARCH_GPT6_MODEL_NAME} defaults to {@code gpt-6-sol}; the other pinned supported profiles are
 * {@code gpt-6-astra}, {@code gpt-6-luna}, and {@code gpt-6.1-sol}. The profile must match the actual deployment.
 * Set {@code SEARCH_OPENAI_API_KEY}, or grant Search's system-assigned identity Cognitive Services OpenAI User
 * on that resource before running. Compatible-provider URI, headers, and audience variables are not used.</p>
 *
 * <p>GPT-6 selects Responses rather than Chat Completions. This sample checks Search-side activity, reasoning usage
 * when supplied, and references against the uploaded corpus. Reasoning-token telemetry is optional in the contract.
 * It cannot prove the downstream Responses wire protocol or stateless
 * tool continuation without appropriately redacted provider traces.</p>
 *
 * <p>Requires a reachable GPT-6 deployment, preview availability, semantic ranking already enabled, and a free index
 * slot. Creates a small index, knowledge source, and knowledge base, then deletes only successfully created resources
 * in reverse order. Does not create deployments, assign roles, enable paid features, or print response content.</p>
 */
public final class NativeGpt6RetrievalPreviewExample {
    private NativeGpt6RetrievalPreviewExample() {
    }

    /**
     * Runs native GPT-6 retrieval over a private-to-this-run synthetic corpus.
     *
     * @param args unused.
     */
    public static void main(String[] args) {
        AzureOpenAIVectorizerParameters model = model(
            PreviewSampleSupport.required("SEARCH_OPENAI_ENDPOINT"),
            PreviewSampleSupport.required("SEARCH_GPT6_DEPLOYMENT_NAME"),
            PreviewSampleSupport.optional("SEARCH_GPT6_MODEL_NAME", "gpt-6-sol"),
            PreviewSampleSupport.optional("SEARCH_OPENAI_API_KEY", null));
        run(PreviewSampleSupport.indexClient(), PreviewSampleSupport::retrievalClient, model);
        System.out.println("Retrieved seeded evidence with native GPT-6."
            + " Verify Responses transport in provider traces.");
    }

    static AzureOpenAIVectorizerParameters model(String endpoint, String deployment, String profile, String key) {
        if (!Arrays.asList("gpt-6-astra", "gpt-6-sol", "gpt-6-luna", "gpt-6.1-sol").contains(profile)) {
            throw new IllegalArgumentException("Select a supported GPT-6 profile matching the existing deployment.");
        }
        if (deployment == null || !deployment.matches("[A-Za-z0-9][A-Za-z0-9._-]*")) {
            throw new IllegalArgumentException("Set SEARCH_GPT6_DEPLOYMENT_NAME to an existing deployment name.");
        }
        if (endpoint == null) {
            throw new IllegalArgumentException("Set SEARCH_OPENAI_ENDPOINT to the HTTPS Azure OpenAI resource root.");
        }
        try {
            URI uri = URI.create(endpoint);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                || !uri.getHost().endsWith(".openai.azure.com") || uri.getRawUserInfo() != null
                || uri.getRawQuery() != null || uri.getRawFragment() != null
                || !(uri.getRawPath().isEmpty() || "/".equals(uri.getRawPath()))) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Set SEARCH_OPENAI_ENDPOINT to the HTTPS Azure OpenAI resource root.");
        }
        if (key != null && key.trim().isEmpty()) {
            throw new IllegalArgumentException("Omit SEARCH_OPENAI_API_KEY for identity authentication.");
        }
        return new AzureOpenAIVectorizerParameters().setResourceUrl(endpoint).setDeploymentName(deployment)
            .setModelName(AzureOpenAIModelName.fromString(profile)).setApiKey(key);
    }

    static void run(SearchIndexClient indexes, Function<String, KnowledgeBaseRetrievalClient> retrievalFactory,
        AzureOpenAIVectorizerParameters model) {
        String indexName = PreviewSampleSupport.name("native-gpt6-hotels");
        String sourceName = indexName + "-source";
        String baseName = indexName + "-kb";
        List<Map<String, Object>> corpus = PreviewSampleSupport.hotelDocuments();
        try (PreviewSampleSupport.Resources resources = new PreviewSampleSupport.Resources(indexes)) {
            resources.createIndex(PreviewSampleSupport.textIndex(indexName));
            PreviewSampleSupport.upload(indexes.getSearchClient(indexName), corpus);
            resources.createKnowledgeSource(new SearchIndexKnowledgeSource(sourceName,
                new SearchIndexKnowledgeSourceParameters(indexName).setSemanticConfigurationName("default")
                    .setSourceDataFields(new SearchIndexFieldReference("id"), new SearchIndexFieldReference("title"),
                        new SearchIndexFieldReference("content"), new SearchIndexFieldReference("country"))));
            OpenAICompatibleRetrievalPreviewExample.waitForSource(indexes, sourceName);
            resources.createKnowledgeBase(new KnowledgeBase(baseName, new KnowledgeSourceReference(sourceName))
                .setModels(new KnowledgeBaseAzureOpenAIModel(model))
                .setRetrievalReasoningEffort(new KnowledgeRetrievalLowReasoningEffort())
                .setOutputMode(KnowledgeRetrievalOutputMode.EXTRACTIVE_DATA));
            KnowledgeBaseRetrievalResult result = retrievalFactory.apply(baseName)
                .retrieve(OpenAICompatibleRetrievalPreviewExample.request(sourceName));
            verifyResult(result, sourceName, corpus);
        }
    }

    static void verifyResult(KnowledgeBaseRetrievalResult result, String sourceName, List<Map<String, Object>> corpus) {
        Map<String, Object> expectedContent = new LinkedHashMap<>();
        for (Map<String, Object> document : corpus) {
            expectedContent.put((String) document.get("id"), document.get("content"));
        }
        OpenAICompatibleRetrievalPreviewExample.verifyResult(result, sourceName, expectedContent.keySet(), false);
        for (KnowledgeBaseReference reference : result.getReferences()) {
            String key = ((KnowledgeBaseSearchIndexReference) reference).getDocKey();
            if (!expectedContent.get(key).equals(reference.getSourceData().get("content"))) {
                throw new IllegalStateException(
                    "Native GPT-6 references must contain the actual seeded document text.");
            }
        }
    }
}
