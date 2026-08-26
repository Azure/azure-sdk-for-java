// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.SearchIndexerClient;
import com.azure.search.documents.indexes.SearchIndexerClientBuilder;
import com.azure.search.documents.indexes.models.AzureBlobKnowledgeSource;
import com.azure.search.documents.indexes.models.AzureBlobKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.AzureOpenAIModelName;
import com.azure.search.documents.indexes.models.AzureOpenAIVectorizerParameters;
import com.azure.search.documents.indexes.models.CreatedResources;
import com.azure.search.documents.indexes.models.KnowledgeSourceContentExtractionMode;
import com.azure.search.documents.indexes.models.LexicalAnalyzerName;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSource;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceFieldValueBoost;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceFilterHint;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceQueryHints;
import com.azure.search.documents.indexes.models.SearchIndexerStatus;
import com.azure.search.documents.knowledgebases.models.AiServices;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceAzureOpenAIVectorizer;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceIngestionParameters;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceNetworkAccessMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Demonstrates preview Knowledge Source configuration and generated-resource inspection.
 *
 * <p>The supported-language Blob folder should contain English content; the fallback folder should contain content in
 * a language without a dedicated Microsoft analyzer.</p>
 *
 * <p>Set {@code SEARCH_ENDPOINT}, {@code SEARCH_API_KEY}, {@code SEARCH_STORAGE_RESOURCE_ID},
 * {@code SEARCH_STORAGE_CONTAINER_NAME},
 * {@code SEARCH_SUPPORTED_LANGUAGE_FOLDER_PATH}, {@code SEARCH_UNSUPPORTED_LANGUAGE_FOLDER_PATH},
 * {@code SEARCH_AI_SERVICES_ENDPOINT}, {@code SEARCH_AI_SERVICES_API_KEY}, {@code SEARCH_OPENAI_ENDPOINT},
 * {@code SEARCH_OPENAI_API_KEY}, {@code SEARCH_OPENAI_EMBEDDING_DEPLOYMENT_NAME}, and
 * {@code SEARCH_OPENAI_EMBEDDING_MODEL_NAME} before running this sample.</p>
 */
public class KnowledgeSourceCrudExample {
    public static void main(String[] args) {
        String endpoint = System.getenv("SEARCH_ENDPOINT");
        AzureKeyCredential credential = new AzureKeyCredential(System.getenv("SEARCH_API_KEY"));
        SearchIndexClient searchIndexClient
            = new SearchIndexClientBuilder().credential(credential).endpoint(endpoint).buildClient();
        SearchIndexerClient searchIndexerClient
            = new SearchIndexerClientBuilder().credential(credential).endpoint(endpoint).buildClient();

        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String indexName = "query-hints-index-" + suffix;
        String searchIndexSourceName = "query-hints-" + suffix;
        String privateBlobSourceName = "private-blob-" + suffix;
        String fallbackBlobSourceName = "fallback-blob-" + suffix;
        List<String> createdSources = new ArrayList<>();
        boolean indexCreated = false;

        try {
            searchIndexClient.createIndex(new SearchIndex(indexName,
                Arrays.asList(new SearchField("id", SearchFieldDataType.STRING).setKey(true),
                    new SearchField("Category", SearchFieldDataType.STRING).setSearchable(true).setFilterable(true))));
            indexCreated = true;

            SearchIndexKnowledgeSource queryHintSource = createQueryHintSource(searchIndexSourceName, indexName);
            SearchIndexKnowledgeSource createdQueryHintSource = (SearchIndexKnowledgeSource) searchIndexClient
                .createKnowledgeSource(queryHintSource);
            createdSources.add(searchIndexSourceName);
            verifyQueryHints(createdQueryHintSource);

            AzureBlobKnowledgeSource privateBlobSource = createBlobSource(privateBlobSourceName,
                System.getenv("SEARCH_SUPPORTED_LANGUAGE_FOLDER_PATH"), KnowledgeSourceNetworkAccessMode.PRIVATE);
            AzureBlobKnowledgeSource createdPrivateBlobSource
                = (AzureBlobKnowledgeSource) searchIndexClient.createKnowledgeSource(privateBlobSource);
            createdSources.add(privateBlobSourceName);
            inspectGeneratedResources(searchIndexClient, searchIndexerClient, createdPrivateBlobSource,
                KnowledgeSourceNetworkAccessMode.PRIVATE, "snippet_en", LexicalAnalyzerName.EN_MICROSOFT);

            AzureBlobKnowledgeSource fallbackBlobSource = createBlobSource(fallbackBlobSourceName,
                System.getenv("SEARCH_UNSUPPORTED_LANGUAGE_FOLDER_PATH"), KnowledgeSourceNetworkAccessMode.PUBLIC);
            AzureBlobKnowledgeSource createdFallbackBlobSource
                = (AzureBlobKnowledgeSource) searchIndexClient.createKnowledgeSource(fallbackBlobSource);
            createdSources.add(fallbackBlobSourceName);
            inspectGeneratedResources(searchIndexClient, searchIndexerClient, createdFallbackBlobSource,
                KnowledgeSourceNetworkAccessMode.PUBLIC, "snippet_default", LexicalAnalyzerName.STANDARD_LUCENE);

            createdQueryHintSource.setDescription("Search-index source with persisted query hints.");
            searchIndexClient.createOrUpdateKnowledgeSource(createdQueryHintSource);
            searchIndexClient.listKnowledgeSources()
                .stream()
                .filter(source -> createdSources.contains(source.getName()))
                .forEach(source -> System.out.println("Created " + source.getKind() + " source " + source.getName()));
        } finally {
            Collections.reverse(createdSources);
            createdSources.forEach(searchIndexClient::deleteKnowledgeSource);
            if (indexCreated) {
                searchIndexClient.deleteIndex(indexName);
            }
        }
    }

    private static SearchIndexKnowledgeSource createQueryHintSource(String name, String indexName) {
        SearchIndexKnowledgeSourceFilterHint filterHint
            = new SearchIndexKnowledgeSourceFilterHint("Category", Collections.singletonList("Luxury"))
                .setFilterInstructions("Use Category when the user requests a specific hotel category.");
        SearchIndexKnowledgeSourceFieldValueBoost boost
            = new SearchIndexKnowledgeSourceFieldValueBoost("Category", 2.0)
                .setFieldValues(Collections.singletonList("Luxury"))
                .setBoostInstructions("Prefer luxury hotels when luxury amenities are requested.");
        SearchIndexKnowledgeSourceQueryHints queryHints = new SearchIndexKnowledgeSourceQueryHints()
            .setFilters(Collections.singletonList(filterHint))
            .setBoosts(Collections.singletonList(boost));
        return new SearchIndexKnowledgeSource(name,
            new SearchIndexKnowledgeSourceParameters(indexName).setQueryHints(queryHints));
    }

    private static void verifyQueryHints(SearchIndexKnowledgeSource source) {
        SearchIndexKnowledgeSourceQueryHints queryHints = source.getSearchIndexParameters().getQueryHints();
        if (queryHints == null || queryHints.getFilters() == null || queryHints.getFilters().size() != 1
            || queryHints.getBoosts() == null || queryHints.getBoosts().size() != 1) {
            throw new IllegalStateException("The query hints weren't persisted on the knowledge source.");
        }
    }

    private static AzureBlobKnowledgeSource createBlobSource(String name, String folderPath,
        KnowledgeSourceNetworkAccessMode networkAccessMode) {
        AzureOpenAIVectorizerParameters vectorizerParameters = new AzureOpenAIVectorizerParameters()
            .setResourceUrl(System.getenv("SEARCH_OPENAI_ENDPOINT"))
            .setApiKey(System.getenv("SEARCH_OPENAI_API_KEY"))
            .setDeploymentName(System.getenv("SEARCH_OPENAI_EMBEDDING_DEPLOYMENT_NAME"))
            .setModelName(AzureOpenAIModelName.fromString(System.getenv("SEARCH_OPENAI_EMBEDDING_MODEL_NAME")));
        KnowledgeSourceIngestionParameters ingestionParameters = new KnowledgeSourceIngestionParameters()
            .setContentExtractionMode(KnowledgeSourceContentExtractionMode.MINIMAL)
            .setEmbeddingModel(
                new KnowledgeSourceAzureOpenAIVectorizer().setAzureOpenAIParameters(vectorizerParameters))
            .setAiServices(new AiServices(System.getenv("SEARCH_AI_SERVICES_ENDPOINT"))
                .setApiKey(System.getenv("SEARCH_AI_SERVICES_API_KEY")))
            .setNetworkAccessMode(networkAccessMode);
        String storageResourceId = System.getenv("SEARCH_STORAGE_RESOURCE_ID");
        String connectionString
            = storageResourceId.startsWith("ResourceId=") ? storageResourceId : "ResourceId=" + storageResourceId;
        AzureBlobKnowledgeSourceParameters blobParameters
            = new AzureBlobKnowledgeSourceParameters(connectionString, System.getenv("SEARCH_STORAGE_CONTAINER_NAME"))
                    .setFolderPath(folderPath)
                    .setIngestionParameters(ingestionParameters);
        return new AzureBlobKnowledgeSource(name, blobParameters);
    }

    private static void inspectGeneratedResources(SearchIndexClient searchIndexClient,
        SearchIndexerClient searchIndexerClient, AzureBlobKnowledgeSource source,
        KnowledgeSourceNetworkAccessMode expectedNetworkAccessMode, String expectedSnippetFieldName,
        LexicalAnalyzerName expectedAnalyzer) {
        AzureBlobKnowledgeSourceParameters parameters = source.getAzureBlobParameters();
        if (!expectedNetworkAccessMode.equals(parameters.getIngestionParameters().getNetworkAccessMode())) {
            throw new IllegalStateException("The knowledge source didn't preserve its create-time network access mode.");
        }

        CreatedResources createdResources = parameters.getCreatedResources();
        if (createdResources == null || createdResources.getAdditionalProperties() == null) {
            throw new IllegalStateException("The service didn't return generated resources for the Blob source.");
        }
        String generatedIndexerName = createdResources.getAdditionalProperties().get("indexer");
        String generatedIndexName = createdResources.getAdditionalProperties().get("index");
        if (generatedIndexerName == null || generatedIndexName == null) {
            throw new IllegalStateException("The generated index or indexer name was missing.");
        }

        SearchIndexerStatus indexerStatus = searchIndexerClient.getIndexerStatus(generatedIndexerName);
        System.out.println("Generated indexer " + generatedIndexerName + " status: " + indexerStatus.getStatus());

        SearchIndex generatedIndex = searchIndexClient.getIndex(generatedIndexName);
        SearchField snippetField = generatedIndex.getFields()
            .stream()
            .filter(field -> expectedSnippetFieldName.equals(field.getName()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "The generated index didn't contain the expected " + expectedSnippetFieldName + " field."));
        LexicalAnalyzerName actualAnalyzer
            = snippetField.getAnalyzerName() != null ? snippetField.getAnalyzerName() : snippetField.getIndexAnalyzerName();
        if (actualAnalyzer == null) {
            actualAnalyzer = LexicalAnalyzerName.STANDARD_LUCENE;
        }
        if (!expectedAnalyzer.equals(actualAnalyzer)) {
            throw new IllegalStateException("Expected generated analyzer " + expectedAnalyzer + " but found "
                + actualAnalyzer + ".");
        }

        if (KnowledgeSourceNetworkAccessMode.PRIVATE.equals(parameters.getIngestionParameters().getNetworkAccessMode())) {
            System.out.println("Approve and verify the Blob and AI Services shared private links through Azure Resource "
                + "Manager; shared-private-link status isn't exposed by the Search data-plane client.");
        }
    }
}
