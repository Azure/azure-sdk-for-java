// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.models.AzureOpenAIModelName;
import com.azure.search.documents.indexes.models.AzureOpenAIVectorizer;
import com.azure.search.documents.indexes.models.AzureOpenAIVectorizerParameters;
import com.azure.search.documents.indexes.models.HnswAlgorithmConfiguration;
import com.azure.search.documents.indexes.models.HnswParameters;
import com.azure.search.documents.indexes.models.KnowledgeBase;
import com.azure.search.documents.indexes.models.KnowledgeSourceReference;
import com.azure.search.documents.indexes.models.KnowledgeSourceResultsProcessing;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexFieldReference;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSource;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.VectorSearch;
import com.azure.search.documents.indexes.models.VectorSearchAlgorithmMetric;
import com.azure.search.documents.indexes.models.VectorSearchProfile;
import com.azure.search.documents.indexes.models.VectorSearchTopicGraphProfile;
import com.azure.search.documents.knowledgebases.KnowledgeBaseRetrievalClient;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalOptions;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalResult;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalMinimalReasoningEffort;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalOutputMode;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalSemanticIntent;
import com.azure.search.documents.knowledgebases.models.SearchIndexKnowledgeSourceParams;
import com.azure.search.documents.models.BalancedVectorQueryTopicGraph;
import com.azure.search.documents.models.DiverseVectorQueryTopicGraph;
import com.azure.search.documents.models.NoneVectorQueryTopicGraph;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResult;
import com.azure.search.documents.models.VectorQueryTopicGraph;
import com.azure.search.documents.models.VectorSearchTopicGraphKind;
import com.azure.search.documents.models.VectorizableTextQuery;
import com.azure.search.documents.models.VectorizedQuery;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builds a GraphRAG Zero corpus and topic sidecar and compares search and knowledge-base retrieval modes.
 *
 * <p>Set SEARCH_ENDPOINT, SEARCH_API_KEY, AZURE_OPENAI_ENDPOINT, AZURE_OPENAI_API_KEY,
 * AZURE_OPENAI_EMBEDDING_DEPLOYMENT and AZURE_OPENAI_EMBEDDING_MODEL. Use text-embedding-3-small
 * (1536 dimensions) or text-embedding-3-large (3072); AZURE_OPENAI_EMBEDDING_DIMENSIONS optionally
 * reduces the dimension count for both stored embeddings and the query vectorizer. The caller and Search
 * must reach the deployment. Embedding/vectorization calls incur usage. Search must support the
 * 2026-11-01-preview Topic Graph API and semantic search.</p>
 *
 * <p>Nine synthetic hotels and three sampled document vectors exercise the API, not retrieval quality.
 * The samples are mock topic representatives, not computed cluster centroids. Unique resources are
 * created without overwriting existing resources and cleaned up on exit. A pool size of 1000 is the
 * default, not a maximum; the supported range is 250 through 10000.</p>
 */
public class TopicGraphPreviewExample {
    private static final String QUERY
        = "Which hotels offer relaxation, interesting dining, and outdoor adventures?";

    /**
     * Runs the topic graph walkthrough.
     * @param args unused.
     * @throws IOException if the embedding HTTP response cannot be read.
     */
    public static void main(String[] args) throws IOException {
        Embeddings embeddings = new Embeddings();
        SearchIndexClient indexes = PreviewSampleSupport.indexClient();
        List<Map<String, Object>> documents = documents();
        List<String> texts = documents.stream().map(document -> (String) document.get("content"))
            .collect(Collectors.toList());
        texts.add(QUERY);
        List<List<Float>> vectors = embeddings.embed(texts);
        for (int i = 0; i < documents.size(); i++) {
            documents.get(i).put("content_vector", vectors.get(i));
        }
        String corpusName = PreviewSampleSupport.name("hotels-grz");
        String sidecarName = corpusName + "-topicgraph";
        String sourceName = PreviewSampleSupport.name("hotels-grz-source");
        String baseName = PreviewSampleSupport.name("hotels-grz-base");
        System.out.println("New sample resources: corpus=" + corpusName + ", sidecar=" + sidecarName
            + ", source=" + sourceName + ", KB=" + baseName);
        try (PreviewSampleSupport.Resources resources = new PreviewSampleSupport.Resources(indexes)) {
            resources.createIndex(sidecar(sidecarName, embeddings.dimensions));
            resources.createIndex(corpus(corpusName, embeddings.dimensions, embeddings.parameters()));
            linkOwnedIndexes(indexes, corpusName, sidecarName);
            SearchClient search = indexes.getSearchClient(corpusName);
            PreviewSampleSupport.upload(search, documents);
            List<Map<String, Object>> representatives = new ArrayList<>();
            for (int i : new int[] {0, 3, 6}) {
                Map<String, Object> representative = new LinkedHashMap<>();
                representative.put("id", "mock-" + documents.get(i).get("topic"));
                representative.put("subject_id", documents.get(i).get("topic"));
                representative.put("object_vector", vectors.get(i));
                representatives.add(representative);
            }
            PreviewSampleSupport.upload(indexes.getSearchClient(sidecarName), representatives);
            System.out.println("Corpus: " + corpusName + "; linked topic sidecar: " + sidecarName);
            compareSearch(search, vectors.get(vectors.size() - 1));
            resources.createKnowledgeSource(knowledgeSource(sourceName, corpusName));
            resources.createKnowledgeBase(new KnowledgeBase(baseName, new KnowledgeSourceReference(sourceName))
                .setOutputMode(KnowledgeRetrievalOutputMode.EXTRACTIVE_DATA)
                .setRetrievalReasoningEffort(new KnowledgeRetrievalMinimalReasoningEffort()));
            compareRetrieval(PreviewSampleSupport.retrievalClient(baseName), sourceName);
        }
    }

    static SearchIndex corpus(String name, int dimensions, AzureOpenAIVectorizerParameters parameters) {
        SearchIndex index = PreviewSampleSupport.textIndex(name);
        List<SearchField> fields = new ArrayList<>(index.getFields());
        fields.add(new SearchField("topic", SearchFieldDataType.STRING).setFilterable(true).setFacetable(true));
        fields.add(vectorField("content_vector", dimensions, "vectors").setHidden(true));
        return new SearchIndex(name, fields).setSemanticSearch(index.getSemanticSearch())
            .setVectorSearch(vectorSearch("vectors")
                .setProfiles(new VectorSearchProfile("vectors", "cosine").setVectorizerName("embeddings"))
                .setVectorizers(new AzureOpenAIVectorizer("embeddings").setParameters(parameters)));
    }

    static SearchIndex sidecar(String name, int dimensions) {
        if (!name.endsWith("-topicgraph")) {
            throw new IllegalArgumentException("The new topic sidecar name must end in -topicgraph.");
        }
        return new SearchIndex(name, Arrays.asList(
            new SearchField("id", SearchFieldDataType.STRING).setKey(true),
            new SearchField("subject_id", SearchFieldDataType.STRING).setHidden(false),
            vectorField("object_vector", dimensions, "centroids").setHidden(false)))
            .setVectorSearch(vectorSearch("centroids"));
    }

    private static SearchField vectorField(String name, int dimensions, String profile) {
        return new SearchField(name, SearchFieldDataType.collection(SearchFieldDataType.SINGLE))
            .setSearchable(true).setStored(true).setVectorSearchDimensions(dimensions)
            .setVectorSearchProfileName(profile);
    }

    private static VectorSearch vectorSearch(String profile) {
        return new VectorSearch().setAlgorithms(new HnswAlgorithmConfiguration("cosine")
            .setParameters(new HnswParameters().setMetric(VectorSearchAlgorithmMetric.COSINE)))
            .setProfiles(new VectorSearchProfile(profile, "cosine"));
    }

    // Called only after this run successfully created both indexes.
    static void linkOwnedIndexes(SearchIndexClient indexes, String corpusName, String sidecarName) {
        indexes.getIndex(sidecarName);
        SearchIndex corpus = indexes.getIndex(corpusName);
        if (corpus.getVectorSearch() == null || corpus.getETag() == null) {
            throw new IllegalStateException("The newly created corpus must have vector configuration and an ETag.");
        }
        corpus.getVectorSearch().setTopicGraphProfiles(Collections.singletonList(new VectorSearchTopicGraphProfile(
            "hotel-topics", sidecarName, VectorSearchTopicGraphKind.BALANCED).setDefaultMinDiversityPoolSize(1000)));
        corpus.getFields().stream().filter(field -> "content_vector".equals(field.getName())).findFirst()
            .orElseThrow(() -> new IllegalStateException("Corpus vector field was not returned."))
            .setTopicGraphProfile("hotel-topics");
        indexes.createOrUpdateIndexWithResponse(corpus,
            new RequestOptions().setHeader(HttpHeaderName.IF_MATCH, corpus.getETag()));
        SearchIndex linked = indexes.getIndex(corpusName);
        if (linked.getVectorSearch() == null || linked.getVectorSearch().getTopicGraphProfiles() == null
            || linked.getVectorSearch().getTopicGraphProfiles().stream()
                .noneMatch(profile -> "hotel-topics".equals(profile.getName())
                    && sidecarName.equals(profile.getIndexName()))
            || linked.getFields().stream().noneMatch(field -> "content_vector".equals(field.getName())
                && "hotel-topics".equals(field.getTopicGraphProfile()))) {
            throw new IllegalStateException("The persisted corpus/sidecar topic link was not returned.");
        }
    }

    static SearchIndexKnowledgeSource knowledgeSource(String name, String indexName) {
        return new SearchIndexKnowledgeSource(name, new SearchIndexKnowledgeSourceParameters(indexName)
            .setSemanticConfigurationName("default")
            .setSourceDataFields(new SearchIndexFieldReference("id"), new SearchIndexFieldReference("title"),
                new SearchIndexFieldReference("content"), new SearchIndexFieldReference("topic"))
            .setSearchFields(new SearchIndexFieldReference("title"), new SearchIndexFieldReference("content"),
                new SearchIndexFieldReference("content_vector"))
            .setTopicGraph(new DiverseVectorQueryTopicGraph().setDiversityFactor(0.2F).setMinDiversityPoolSize(500)))
            .setResultsProcessing(KnowledgeSourceResultsProcessing.NONE);
    }

    static void compareSearch(SearchClient client, List<Float> queryVector) {
        List<VectorQueryTopicGraph> modes = Arrays.asList(new NoneVectorQueryTopicGraph(),
            new DiverseVectorQueryTopicGraph().setDiversityFactor(0.2F).setMinDiversityPoolSize(500),
            new BalancedVectorQueryTopicGraph().setMinDiversityPoolSize(500));
        for (VectorQueryTopicGraph mode : modes) {
            printSearch(mode.getKind().toString(), client.search(new SearchOptions()
                .setVectorQueries(new VectorizedQuery(queryVector).setFields("content_vector")
                    .setKNearestNeighbors(6).setTopicGraph(mode))
                .setSelect("id", "title", "topic", "content").setTop(6)).stream().collect(Collectors.toList()));
        }
        printSearch("Text vectorization with inherited balanced profile", client.search(new SearchOptions()
            .setVectorQueries(new VectorizableTextQuery(QUERY).setFields("content_vector").setKNearestNeighbors(6))
            .setSelect("id", "title", "topic", "content").setTop(6)).stream().collect(Collectors.toList()));
    }

    private static void printSearch(String label, List<SearchResult> results) {
        if (results.size() != 6 || results.stream().map(result -> result.getAdditionalProperties().get("id"))
            .distinct().count() != 6) {
            throw new IllegalStateException(label + " must return six distinct synthetic hotels.");
        }
        System.out.println(label + ": " + results.stream().map(SearchResult::getAdditionalProperties)
            .collect(Collectors.toList()));
    }

    static KnowledgeBaseRetrievalOptions retrievalOptions(String sourceName, VectorQueryTopicGraph override) {
        return new KnowledgeBaseRetrievalOptions().setIntents(new KnowledgeRetrievalSemanticIntent(QUERY))
            .setRetrievalReasoningEffort(new KnowledgeRetrievalMinimalReasoningEffort())
            .setOutputMode(KnowledgeRetrievalOutputMode.EXTRACTIVE_DATA).setIncludeActivity(true)
            .setMaxRuntimeInSeconds(60).setMaxOutputDocuments(6)
            .setKnowledgeSourceParams(Collections.singletonList(new SearchIndexKnowledgeSourceParams(sourceName)
                .setAlwaysQuerySource(true).setFailOnError(true).setIncludeReferences(true)
                .setIncludeReferenceSourceData(true).setResultsProcessing(KnowledgeSourceResultsProcessing.NONE)
                .setTopicGraph(override)));
    }

    private static void compareRetrieval(KnowledgeBaseRetrievalClient client, String sourceName) {
        List<VectorQueryTopicGraph> overrides = Arrays.asList(null,
            new DiverseVectorQueryTopicGraph().setDiversityFactor(0.25F).setMinDiversityPoolSize(2000),
            new BalancedVectorQueryTopicGraph(), new NoneVectorQueryTopicGraph());
        for (VectorQueryTopicGraph override : overrides) {
            KnowledgeBaseRetrievalResult result = client.retrieve(retrievalOptions(sourceName, override));
            if (result.getReferences() == null || result.getReferences().isEmpty()
                || result.getActivity() == null || result.getActivity().isEmpty()
                || result.getActivity().stream().anyMatch(activity -> activity.getError() != null)) {
                throw new IllegalStateException(
                    "Topic-aware retrieval must return references and successful activity.");
            }
            System.out.println((override == null ? "Stored diverse source default" : "Runtime " + override.getKind())
                + "; activities returned: " + result.getActivity().size()
                + "; source references: " + result.getReferences().size());
            result.getReferences().forEach(reference -> System.out.println("Reference " + reference.getId()
                + " (activity " + reference.getActivitySource() + "): " + reference.getSourceData()));
        }
    }

    static List<Map<String, Object>> documents() {
        String[][] hotels = {
            {"wellness", "Harbor Spa Hotel",
                "Relax at a waterfront hotel with a heated pool, spa treatments, and quiet rooms."},
            {"wellness", "Garden Retreat",
                "Enjoy a peaceful hotel garden, morning yoga, and a full-service wellness spa."},
            {"wellness", "Lakeside Sanctuary",
                "Recharge with lakeside walks, a sauna, and relaxing hotel massage treatments."},
            {"dining", "Market Square Hotel",
                "Taste regional dishes at the hotel's restaurant near the lively food market."},
            {"dining", "Rooftop Dining Hotel",
                "Enjoy rooftop dining, a chef's tasting menu, and local wine at this city hotel."},
            {"dining", "Harbor Kitchen Inn",
                "Explore seafood restaurants and cooking classes at a hotel beside the harbor."},
            {"adventure", "Mountain Trail Lodge",
                "Explore hiking trails and mountain biking routes from this outdoor adventure hotel."},
            {"adventure", "River Adventure Inn",
                "Try guided kayaking, rafting, and riverside hiking from this active holiday hotel."},
            {"adventure", "Forest Explorer Hotel",
                "Discover forest trails, climbing excursions, and wildlife tours near this hotel."}
        };
        List<Map<String, Object>> documents = new ArrayList<>();
        for (int i = 0; i < hotels.length; i++) {
            Map<String, Object> document = new LinkedHashMap<>();
            document.put("id", hotels[i][0] + "-" + (i % 3 + 1));
            document.put("topic", hotels[i][0]);
            document.put("title", hotels[i][1]);
            document.put("content", hotels[i][2]);
            documents.add(document);
        }
        return documents;
    }

    private static final class Embeddings {
        private final String endpoint = PreviewSampleSupport.required("AZURE_OPENAI_ENDPOINT");
        private final String deployment = PreviewSampleSupport.required("AZURE_OPENAI_EMBEDDING_DEPLOYMENT");
        private final String model = PreviewSampleSupport.required("AZURE_OPENAI_EMBEDDING_MODEL");
        private final String key = PreviewSampleSupport.required("AZURE_OPENAI_API_KEY");
        private final int dimensions;

        private Embeddings() {
            int fullDimensions;
            if ("text-embedding-3-small".equals(model)) {
                fullDimensions = 1536;
            } else if ("text-embedding-3-large".equals(model)) {
                fullDimensions = 3072;
            } else {
                throw new IllegalArgumentException("Use text-embedding-3-small or text-embedding-3-large.");
            }
            dimensions = Integer.parseInt(PreviewSampleSupport.optional("AZURE_OPENAI_EMBEDDING_DIMENSIONS",
                Integer.toString(fullDimensions)));
            if (dimensions <= 0 || dimensions > fullDimensions || !"https".equals(URI.create(endpoint).getScheme())) {
                throw new IllegalArgumentException("Use an HTTPS embedding endpoint and valid model dimensions.");
            }
        }

        private AzureOpenAIVectorizerParameters parameters() {
            return new AzureOpenAIVectorizerParameters().setResourceUrl(endpoint).setDeploymentName(deployment)
                .setModelName(AzureOpenAIModelName.fromString(model)).setApiKey(key);
        }

        private List<List<Float>> embed(List<String> texts) throws IOException {
            HttpRequest request = embeddingRequest(endpoint, deployment, key, texts, dimensions);
            try (HttpResponse response = HttpClient.createDefault().sendSync(request, Context.NONE)) {
                if (response.getStatusCode() != 200) {
                    throw new IllegalStateException("Embedding request failed with HTTP " + response.getStatusCode()
                        + ". Check deployment access and model configuration.");
                }
                return readEmbeddings(response.getBodyAsBinaryData(), texts.size(), dimensions);
            }
        }
    }

    static HttpRequest embeddingRequest(String endpoint, String deployment, String key, List<String> texts,
        int dimensions) throws IOException {
        String url = endpoint.replaceAll("/+$", "") + "/openai/deployments/"
            + URLEncoder.encode(deployment, StandardCharsets.UTF_8.name()) + "/embeddings?api-version=2024-02-01";
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("input", texts);
        body.put("dimensions", dimensions);
        body.put("encoding_format", "float");
        return new HttpRequest(HttpMethod.POST, url)
            .setHeader(HttpHeaderName.CONTENT_TYPE, "application/json").setHeader("api-key", key)
            .setBody(BinaryData.fromObject(body));
    }

    static List<List<Float>> readEmbeddings(BinaryData response, int count, int dimensions) {
        List<List<Float>> vectors = new ArrayList<>(Collections.nCopies(count, null));
        Object data = response.toObject(Map.class).get("data");
        if (!(data instanceof List) || ((List<?>) data).size() != count) {
            throw new IllegalStateException("Embedding response count does not match the synthetic fixture.");
        }
        for (Object item : (List<?>) data) {
            Map<?, ?> entry = (Map<?, ?>) item;
            int index = ((Number) entry.get("index")).intValue();
            List<?> values = (List<?>) entry.get("embedding");
            if (index < 0 || index >= count || vectors.get(index) != null || values.size() != dimensions) {
                throw new IllegalStateException("Embedding response indices or vector dimensions do not match.");
            }
            List<Float> vector = values.stream().map(value -> ((Number) value).floatValue())
                .collect(Collectors.toList());
            if (vector.stream().anyMatch(value -> !Float.isFinite(value))) {
                throw new IllegalStateException("Embedding response contains a nonfinite value.");
            }
            vectors.set(index, vector);
        }
        return vectors;
    }
}
