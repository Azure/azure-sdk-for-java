// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.models.AzureBlobKnowledgeSource;
import com.azure.search.documents.indexes.models.AzureBlobKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.AzureOpenAIModelName;
import com.azure.search.documents.indexes.models.ExhaustiveKnnAlgorithmConfiguration;
import com.azure.search.documents.indexes.models.KnowledgeSourceContentExtractionMode;
import com.azure.search.documents.indexes.models.KnowledgeSourceSynchronizationStatus;
import com.azure.search.documents.indexes.models.OpenAICompatibleVectorizer;
import com.azure.search.documents.indexes.models.OpenAICompatibleVectorizerParameters;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.VectorSearch;
import com.azure.search.documents.indexes.models.VectorSearchProfile;
import com.azure.search.documents.knowledgebases.models.CompletedSynchronizationState;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceIngestionParameters;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceOpenAICompatibleVectorizer;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceStatus;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResult;
import com.azure.search.documents.models.VectorizableTextQuery;
import com.azure.search.documents.models.VectorizedQuery;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generates hotel embeddings through an OpenAI-compatible provider/APIM gateway, uploads them to a new index,
 * and compares stored-vector search with Search's compatible query-time text vectorization.
 *
 * <p>Set {@code SEARCH_ENDPOINT}, {@code SEARCH_API_KEY}, {@code AZURE_SEARCH_COMPATIBLE_EMBEDDING_URI}
 * (the complete HTTPS embeddings POST URL including path/query),
 * {@code AZURE_SEARCH_COMPATIBLE_EMBEDDING_MODEL}, and {@code AZURE_SEARCH_COMPATIBLE_EMBEDDING_DIMENSIONS}.
 * For example, an actual {@code text-embedding-ada-002} deployment uses 1536 dimensions. The gateway must route
 * to that same deployed model; neither this sample nor Search sends a model property. Set exactly one of
 * {@code AZURE_SEARCH_COMPATIBLE_EMBEDDING_HEADERS_JSON} or
 * {@code AZURE_SEARCH_COMPATIBLE_EMBEDDING_MI_AUDIENCE}, as described in
 * {@link OpenAICompatibleRetrievalPreviewExample}. The MI option additionally requires the local
 * {@code DefaultAzureCredential} identity to access the provider using the audience GUID's
 * {@code /.default} scope, for document-vector generation; Search uses its own managed identity for queries.
 * The provider must accept batched inputs and return indexed float embeddings. Redirects are not followed.</p>
 *
 * <p>This default flow demonstrates client-side document embedding plus Search query-time vectorization, not
 * ingestion-time skill execution. Requires compatible-vectorizer preview support and one free index slot.
 * Only resources created by this run are deleted.</p>
 *
 * <p>Optional managed-ingestion flow: pass {@code blob} as the sole argument, and also set
 * {@code AZURE_SEARCH_COMPATIBLE_BLOB_CONNECTION_STRING} and {@code AZURE_SEARCH_COMPATIBLE_BLOB_CONTAINER}.
 * Also set {@code AZURE_SEARCH_COMPATIBLE_BLOB_FOLDER} to a dedicated nonempty folder of supported text documents,
 * accessible through that connection string (or Search's storage identity). Confirm the folder contains only
 * approved sample data, then set {@code AZURE_SEARCH_COMPATIBLE_BLOB_SOURCE_APPROVED=true}. Container-root reads
 * are not permitted. The sample does not create or modify blobs.
 * This creates a managed Azure Blob knowledge source with a compatible ingestion embedding
 * model, waits for a successful nonempty synchronization, then deletes the knowledge source and its managed resources.
 * It does not merely serialize a model configuration or claim that a skillset has run.</p>
 */
public final class OpenAICompatibleEmbeddingsPreviewExample {
    private OpenAICompatibleEmbeddingsPreviewExample() {
    }

    /**
     * Runs vector indexing/querying, or managed Blob ingestion when passed {@code blob}.
     *
     * @param args empty for index/query, or a single {@code blob} argument.
     */
    public static void main(String[] args) {
        if (args.length > 1 || (args.length == 1 && !"blob".equals(args[0]))) {
            throw new IllegalArgumentException("Use no arguments for index/query, or 'blob' for managed ingestion.");
        }
        OpenAICompatibleVectorizerParameters provider = provider(
            PreviewSampleSupport.required("AZURE_SEARCH_COMPATIBLE_EMBEDDING_URI"),
            PreviewSampleSupport.required("AZURE_SEARCH_COMPATIBLE_EMBEDDING_MODEL"),
            Integer.parseInt(PreviewSampleSupport.required("AZURE_SEARCH_COMPATIBLE_EMBEDDING_DIMENSIONS")),
            System.getenv("AZURE_SEARCH_COMPATIBLE_EMBEDDING_HEADERS_JSON"),
            System.getenv("AZURE_SEARCH_COMPATIBLE_EMBEDDING_MI_AUDIENCE"));
        SearchIndexClient indexes = PreviewSampleSupport.indexClient();
        if (args.length == 1) {
            runManagedBlob(indexes, provider,
                PreviewSampleSupport.required("AZURE_SEARCH_COMPATIBLE_BLOB_CONNECTION_STRING"),
                PreviewSampleSupport.required("AZURE_SEARCH_COMPATIBLE_BLOB_CONTAINER"),
                PreviewSampleSupport.required("AZURE_SEARCH_COMPATIBLE_BLOB_FOLDER"),
                PreviewSampleSupport.optional("AZURE_SEARCH_COMPATIBLE_BLOB_SOURCE_APPROVED", "false"));
        } else {
            TokenCredential identity = provider.getManagedIdentityAudience() == null
                ? null : new DefaultAzureCredentialBuilder().build();
            run(indexes, HttpClient.createDefault(), provider, identity);
        }
    }

    static OpenAICompatibleVectorizerParameters provider(String uri, String model, int dimensions, String headers,
        String audience) {
        if (model == null || !model.startsWith("text-embedding-") || !model.equals(model.trim()) || dimensions <= 0) {
            throw new IllegalArgumentException(
                "Specify the deployed text-embedding model and its positive dimensions.");
        }
        if ("text-embedding-ada-002".equals(model) && dimensions != 1536) {
            throw new IllegalArgumentException("text-embedding-ada-002 requires 1536 dimensions.");
        }
        OpenAICompatibleRetrievalPreviewExample.CompatibleAuth auth
            = OpenAICompatibleRetrievalPreviewExample.CompatibleAuth.parse(headers, audience);
        return new OpenAICompatibleVectorizerParameters(OpenAICompatibleRetrievalPreviewExample.fullUri(uri),
            AzureOpenAIModelName.fromString(model)).setDimensions(dimensions)
                .setHeaders(auth.headers).setManagedIdentityAudience(auth.audience);
    }

    static void run(SearchIndexClient indexes, HttpClient providerClient, OpenAICompatibleVectorizerParameters provider,
        TokenCredential identity) {
        List<Map<String, Object>> documents = PreviewSampleSupport.hotelDocuments();
        documents.get(0).put("title", "Harbor Spa Hotel");
        documents.get(0).put("content",
            "Relax at a waterfront hotel with a heated pool, spa treatments, and quiet rooms.");
        documents.get(1).put("content", "A business hotel near offices with meeting rooms and high-speed internet.");
        documents.get(2).put("content", "An adventure lodge near mountain hiking trails and rock climbing.");
        List<String> input = documents.stream().map(document -> (String) document.get("content"))
            .collect(Collectors.toList());
        List<List<Float>> vectors = embed(providerClient, provider, identity, input);
        for (int i = 0; i < documents.size(); i++) {
            documents.get(i).put("contentVector", vectors.get(i));
        }
        String indexName = PreviewSampleSupport.name("compatible-vectors");
        try (PreviewSampleSupport.Resources resources = new PreviewSampleSupport.Resources(indexes)) {
            resources.createIndex(vectorIndex(indexName, provider));
            SearchClient search = indexes.getSearchClient(indexName);
            PreviewSampleSupport.upload(search, documents);
            SearchOptions control = new SearchOptions().setSelect("id", "title").setTop(3)
                .setVectorQueries(new VectorizedQuery(vectors.get(0)).setFields("contentVector")
                    .setKNearestNeighbors(3).setExhaustive(true));
            SearchOptions generated = new SearchOptions().setSelect("id", "title").setTop(3)
                .setVectorQueries(new VectorizableTextQuery("Relaxing hotel with a pool and spa treatments.")
                    .setFields("contentVector").setKNearestNeighbors(3).setExhaustive(true));
            verifyFirst(search.search(control), "hotel-ca-1");
            verifyFirst(search.search(generated), "hotel-ca-1");
            System.out.println("Stored-vector and provider-vectorized text queries both ranked the spa hotel first.");
        }
    }

    static SearchIndex vectorIndex(String name, OpenAICompatibleVectorizerParameters provider) {
        SearchIndex index = PreviewSampleSupport.textIndex(name);
        List<SearchField> fields = new ArrayList<>(index.getFields());
        fields.add(new SearchField("contentVector", SearchFieldDataType.collection(SearchFieldDataType.SINGLE))
            .setSearchable(true).setVectorSearchDimensions(provider.getDimensions())
            .setVectorSearchProfileName("compatible-profile"));
        return new SearchIndex(name, fields).setSemanticSearch(index.getSemanticSearch())
            .setVectorSearch(new VectorSearch().setAlgorithms(new ExhaustiveKnnAlgorithmConfiguration("exact"))
                .setProfiles(new VectorSearchProfile("compatible-profile", "exact").setVectorizerName("compatible"))
                .setVectorizers(new OpenAICompatibleVectorizer("compatible", provider)));
    }

    static List<List<Float>> embed(HttpClient client, OpenAICompatibleVectorizerParameters provider,
        TokenCredential identity, List<String> input) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("input", input);
        if (!"text-embedding-ada-002".equals(provider.getModelName().toString())) {
            payload.put("dimensions", provider.getDimensions());
        }
        HttpRequest request = new HttpRequest(HttpMethod.POST, provider.getUri())
            .setHeader("Content-Type", "application/json")
            .setBody(BinaryData.fromObject(payload));
        try {
            if (provider.getHeaders() != null) {
                provider.getHeaders().forEach(request::setHeader);
            } else {
                if (identity == null) {
                    throw new IllegalArgumentException("A local provider credential is required for MI embedding.");
                }
                AccessToken token = identity.getToken(new TokenRequestContext()
                    .addScopes(provider.getManagedIdentityAudience() + "/.default")).block(Duration.ofSeconds(60));
                if (token == null) {
                    throw new IllegalStateException("The local provider credential returned no access token.");
                }
                request.setHeader("Authorization", "Bearer " + token.getToken());
            }
            try (HttpResponse response = client.send(request).block(Duration.ofSeconds(60))) {
                if (response == null || response.getStatusCode() != 200) {
                    throw new IllegalStateException("The embedding provider must return HTTP 200 without redirects.");
                }
                return parseEmbeddings(response.getBodyAsBinaryData(), input.size(), provider.getDimensions());
            }
        } catch (RuntimeException ex) {
            // Transport and provider errors can contain credential-bearing headers, URIs, or response bodies.
            throw new IllegalStateException(
                "Embedding failed. Check provider access, routing, and embedding dimensions.");
        }
    }

    static List<List<Float>> parseEmbeddings(BinaryData body, int count, int dimensions) {
        try (JsonReader reader = JsonProviders.createReader(body.toBytes())) {
            if (reader.nextToken() != JsonToken.START_OBJECT) {
                throw new IllegalArgumentException();
            }
            Map<String, Object> response = reader.readMap(JsonReader::readUntyped);
            if (response == null || !(response.get("data") instanceof List<?>)) {
                throw new IllegalArgumentException();
            }
            List<?> data = (List<?>) response.get("data");
            if (data.size() != count) {
                throw new IllegalArgumentException();
            }
            List<List<Float>> vectors = new ArrayList<>(Collections.nCopies(count, null));
            for (Object value : data) {
                if (!(value instanceof Map<?, ?>)) {
                    throw new IllegalArgumentException();
                }
                Map<?, ?> item = (Map<?, ?>) value;
                if (!(item.get("index") instanceof Number) || !(item.get("embedding") instanceof List<?>)) {
                    throw new IllegalArgumentException();
                }
                Number position = (Number) item.get("index");
                int index = position.intValue();
                List<?> embedding = (List<?>) item.get("embedding");
                if (index < 0 || index >= count || position.doubleValue() != index
                    || vectors.get(index) != null || embedding.size() != dimensions) {
                    throw new IllegalArgumentException();
                }
                List<Float> vector = new ArrayList<>(dimensions);
                boolean nonzero = false;
                for (Object number : embedding) {
                    if (!(number instanceof Number)) {
                        throw new IllegalArgumentException();
                    }
                    float component = ((Number) number).floatValue();
                    if (!Float.isFinite(component)) {
                        throw new IllegalArgumentException();
                    }
                    nonzero |= component != 0;
                    vector.add(component);
                }
                if (!nonzero) {
                    throw new IllegalArgumentException();
                }
                vectors.set(index, vector);
            }
            if (reader.nextToken() != JsonToken.END_DOCUMENT) {
                throw new IllegalArgumentException();
            }
            return vectors;
        } catch (IOException | RuntimeException ex) {
            throw new IllegalArgumentException(
                "Provider embeddings must have unique indexes and matching finite float vectors.");
        }
    }

    private static void verifyFirst(Iterable<SearchResult> results, String expected) {
        java.util.Iterator<SearchResult> iterator = results.iterator();
        if (!iterator.hasNext() || !expected.equals(iterator.next().getAdditionalProperties().get("id"))) {
            throw new IllegalStateException("The query did not rank the expected spa hotel first.");
        }
    }

    static AzureBlobKnowledgeSource managedBlobSource(String name, OpenAICompatibleVectorizerParameters provider,
        String connectionString, String container, String folder) {
        return new AzureBlobKnowledgeSource(name,
            new AzureBlobKnowledgeSourceParameters(connectionString, container).setFolderPath(folder)
                .setIngestionParameters(new KnowledgeSourceIngestionParameters()
                    .setContentExtractionMode(KnowledgeSourceContentExtractionMode.MINIMAL)
                    .setDisableImageVerbalization(true)
                    .setEmbeddingModel(new KnowledgeSourceOpenAICompatibleVectorizer(provider))));
    }

    static String approvedFolder(String folder, String approved) {
        String value = folder == null ? "" : folder.trim();
        if (!"true".equalsIgnoreCase(approved) || value.isEmpty() || value.startsWith("/") || value.startsWith("\\")
            || java.util.Arrays.stream(value.replace('\\', '/').split("/"))
                .anyMatch(segment -> ".".equals(segment) || "..".equals(segment))) {
            throw new IllegalArgumentException(
                "Set AZURE_SEARCH_COMPATIBLE_BLOB_FOLDER to a dedicated approved nonempty folder, then set "
                    + "AZURE_SEARCH_COMPATIBLE_BLOB_SOURCE_APPROVED=true. Container-root ingestion is not permitted.");
        }
        return value;
    }

    static void runManagedBlob(SearchIndexClient indexes, OpenAICompatibleVectorizerParameters provider,
        String connectionString, String container, String folder, String approved) {
        String approvedFolder = approvedFolder(folder, approved);
        String sourceName = PreviewSampleSupport.name("compatible-blob");
        try (PreviewSampleSupport.Resources resources = new PreviewSampleSupport.Resources(indexes)) {
            resources.createKnowledgeSource(
                managedBlobSource(sourceName, provider, connectionString, container, approvedFolder));
            for (int attempt = 0; attempt < 120; attempt++) {
                KnowledgeSourceStatus status = indexes.getKnowledgeSourceStatus(sourceName);
                if (status.getCurrentSynchronizationState() != null
                    && status.getCurrentSynchronizationState().getItemsUpdatesFailed() > 0) {
                    throw new IllegalStateException("Managed compatible ingestion reported failed document updates.");
                }
                CompletedSynchronizationState completed = status.getLastSynchronizationState();
                if (completed != null) {
                    if (completed.getItemsUpdatesFailed() != 0 || completed.getItemsUpdatesProcessed() <= 0) {
                        throw new IllegalStateException(
                            "Managed compatible ingestion did not process nonempty data successfully.");
                    }
                    if (KnowledgeSourceSynchronizationStatus.ACTIVE.equals(status.getSynchronizationStatus())) {
                        System.out.println(
                            "Managed Blob knowledge source successfully ingested data using compatible embeddings.");
                        return;
                    }
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while waiting for compatible Blob ingestion.", ex);
                }
            }
            throw new IllegalStateException("Managed compatible ingestion did not finish within ten minutes.");
        }
    }
}
