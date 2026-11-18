// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

// cspell:words rdfs

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexerClient;
import com.azure.search.documents.indexes.SearchIndexerClientBuilder;
import com.azure.search.documents.indexes.models.AzureBlobKnowledgeSource;
import com.azure.search.documents.indexes.models.AzureBlobKnowledgeSourceParameters;
import com.azure.search.documents.indexes.models.AzureOpenAIModelName;
import com.azure.search.documents.indexes.models.AzureOpenAIVectorizerParameters;
import com.azure.search.documents.indexes.models.GraphFieldMappings;
import com.azure.search.documents.indexes.models.GraphProfile;
import com.azure.search.documents.indexes.models.IndexerExecutionResult;
import com.azure.search.documents.indexes.models.IndexerExecutionStatus;
import com.azure.search.documents.indexes.models.KnowledgeBase;
import com.azure.search.documents.indexes.models.KnowledgeBaseAzureOpenAIModel;
import com.azure.search.documents.indexes.models.KnowledgeSourceContentExtractionMode;
import com.azure.search.documents.indexes.models.KnowledgeSourceGraphExtractionParameters;
import com.azure.search.documents.indexes.models.KnowledgeSourceGraphOntology;
import com.azure.search.documents.indexes.models.KnowledgeSourceGraphOntologySourceKind;
import com.azure.search.documents.indexes.models.KnowledgeSourceReference;
import com.azure.search.documents.indexes.models.KnowledgeSourceSynchronizationStatus;
import com.azure.search.documents.knowledgebases.models.AgentRuntimeGraphParams;
import com.azure.search.documents.knowledgebases.models.AgentRuntimeGraphProvenanceParams;
import com.azure.search.documents.knowledgebases.models.AzureBlobKnowledgeSourceParams;
import com.azure.search.documents.knowledgebases.models.CompletedSynchronizationState;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseGraphSearchActivityRecord;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseGraphSearchReference;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseMessage;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseMessageTextContent;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalOptions;
import com.azure.search.documents.knowledgebases.models.KnowledgeBaseRetrievalResult;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalLowReasoningEffort;
import com.azure.search.documents.knowledgebases.models.KnowledgeRetrievalOutputMode;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceAzureOpenAIVectorizer;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceCache;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceGraphProvenanceKind;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceIngestionParameters;
import com.azure.search.documents.knowledgebases.models.KnowledgeSourceStatus;
import com.azure.search.documents.models.GraphOptions;
import com.azure.search.documents.models.GraphQueryProvenanceKind;
import com.azure.search.documents.models.GraphQueryProvenanceParameters;
import com.azure.search.documents.models.GraphQueryResult;
import com.azure.search.documents.models.GraphQueryRow;
import com.azure.search.documents.models.SearchOptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Extracts an EKG from an approved, read-only Blob fixture using the public synthetic hotel ontology and a cache.
 *
 * <p>Before running, upload {@link #SYNTHETIC_HOTELS} as hotel_graph.txt with content type text/plain
 * into a dedicated approved, nonempty Blob folder. Set SEARCH_GRAPH_SOURCE_APPROVED=true only after
 * confirming that folder contains this fixture and no unrelated data. The sample never uploads,
 * modifies or deletes source blobs and requires no Storage SDK dependency.</p>
 *
 * <p>Required environment: SEARCH_ENDPOINT, SEARCH_API_KEY, AZURE_STORAGE_CONNECTION_STRING,
 * AZURE_STORAGE_CONTAINER, AZURE_STORAGE_FOLDER, AZURE_SEARCH_GRAPH_CACHE_CONNECTION_STRING,
 * AZURE_OPENAI_ENDPOINT, AZURE_OPENAI_CHAT_DEPLOYMENT, AZURE_OPENAI_CHAT_MODEL,
 * AZURE_OPENAI_EMBEDDING_DEPLOYMENT and AZURE_OPENAI_EMBEDDING_MODEL. AZURE_OPENAI_API_KEY is
 * optional when Search's managed identity has Cognitive Services OpenAI User on the reachable deployments.
 * The service must support 2026-11-01-preview EKG extraction, cache and graph retrieval.</p>
 *
 * <p>Native Blob soft delete must already be enabled before ingestion. Do not use ADLS Gen2,
 * ACL or sensitivity-label ingestion. The cache requires read/write GPv2 Blob and Table endpoints.
 * ResourceId connections require Storage Blob Data Reader for source reads and Storage Blob Data
 * Contributor plus Storage Table Data Contributor for cache writes. This sample changes no
 * permissions, flags or storage settings. Only newly created KS/KB resources are explicitly cleaned
 * up; inspect printed generated resource names for service-managed artifacts, and manage cache data
 * separately. Cache configuration or unchanged chunks do not prove cache hits: use execution telemetry.</p>
 *
 * <p>Graph parameters customize graph calls but do not select the planner's search tool. The KB's retrieval
 * instructions and relationship-specific question guide graph routing; the sample requires actual graph
 * references and generated GQL rather than accepting an ordinary document-search result.</p>
 */
public class GraphExtractionCachePreviewExample {
    static final String SYNTHETIC_HOTELS
        = "Historic Harbor Hotel is a hotel in Seattle. Contoso Hospitality operates\n"
        + "Historic Harbor Hotel. Historic Harbor Hotel was founded in 1984.\n\n"
        + "Mountain View Inn is a hotel in Denver. Alpine Hospitality operates\n"
        + "Mountain View Inn. Mountain View Inn was founded in 2002.\n\n"
        + "Contoso Hospitality and Alpine Hospitality are organizations, not hotels.\n"
        + "Seattle and Denver are cities. These hotel descriptions are synthetic.\n";

    /**
     * Runs extraction, synchronization checks, direct graph queries and KB graph retrieval.
     * @param args unused.
     */
    public static void main(String[] args) {
        String folder = approvedFolder(PreviewSampleSupport.required("AZURE_STORAGE_FOLDER"),
            PreviewSampleSupport.optional("SEARCH_GRAPH_SOURCE_APPROVED", "false"));
        String connection = PreviewSampleSupport.required("AZURE_STORAGE_CONNECTION_STRING");
        String container = PreviewSampleSupport.required("AZURE_STORAGE_CONTAINER");
        String cacheConnection = PreviewSampleSupport.required("AZURE_SEARCH_GRAPH_CACHE_CONNECTION_STRING");
        String openAiEndpoint = PreviewSampleSupport.required("AZURE_OPENAI_ENDPOINT");
        String chatDeployment = PreviewSampleSupport.required("AZURE_OPENAI_CHAT_DEPLOYMENT");
        String chatModel = PreviewSampleSupport.required("AZURE_OPENAI_CHAT_MODEL");
        String embeddingDeployment = PreviewSampleSupport.required("AZURE_OPENAI_EMBEDDING_DEPLOYMENT");
        String embeddingModel = PreviewSampleSupport.required("AZURE_OPENAI_EMBEDDING_MODEL");
        String key = PreviewSampleSupport.optional("AZURE_OPENAI_API_KEY", null);
        SearchIndexClient indexes = PreviewSampleSupport.indexClient();
        SearchIndexerClient indexers = new SearchIndexerClientBuilder()
            .endpoint(PreviewSampleSupport.required("SEARCH_ENDPOINT"))
            .credential(new AzureKeyCredential(PreviewSampleSupport.required("SEARCH_API_KEY")))
            .serviceVersion(SearchServiceVersion.V2026_11_01_PREVIEW).buildClient();
        String sourceName = PreviewSampleSupport.name("hotels-ekg-cache");
        String baseName = PreviewSampleSupport.name("hotels-ekg-base");
        String graphName = "hotels";
        KnowledgeBaseAzureOpenAIModel chat = new KnowledgeBaseAzureOpenAIModel(
            modelParameters(openAiEndpoint, chatDeployment, chatModel, key));
        KnowledgeSourceIngestionParameters ingestion = new KnowledgeSourceIngestionParameters()
            .setEmbeddingModel(new KnowledgeSourceAzureOpenAIVectorizer().setAzureOpenAIParameters(
                modelParameters(openAiEndpoint, embeddingDeployment, embeddingModel, key)))
            .setChatCompletionModel(chat).setContentExtractionMode(KnowledgeSourceContentExtractionMode.MINIMAL)
            .setDisableImageVerbalization(true)
            .setCache(new KnowledgeSourceCache(cacheConnection).setEnableReprocessing(true));
        AzureBlobKnowledgeSource source
            = knowledgeSource(sourceName, graphName, connection, container, folder, ingestion);
        try (PreviewSampleSupport.Resources resources = new PreviewSampleSupport.Resources(indexes)) {
            resources.createKnowledgeSource(source);
            waitForActive(indexes, sourceName);
            AzureBlobKnowledgeSource stored = (AzureBlobKnowledgeSource) indexes.getKnowledgeSource(sourceName);
            if (stored.getAzureBlobParameters().getCreatedResources() == null) {
                throw new IllegalStateException("The graph knowledge source did not return generated resources.");
            }
            Map<String, String> created
                = stored.getAzureBlobParameters().getCreatedResources().getAdditionalProperties();
            validateGeneratedResources(sourceName, created);
            System.out.println("New knowledge source: " + sourceName + "; generated Search resources: " + created);
            if (!folder.equals(indexers.getDataSourceConnection(created.get("datasource")).getContainer().getQuery())) {
                throw new IllegalStateException("The generated data source lost its approved Blob folder scope.");
            }
            indexers.getIndexer(created.get("indexer"));
            indexers.getSkillset(created.get("skillset"));
            GraphProfile profile = indexes.getIndex(created.get("graphIndex")).getGraphProfile();
            if (profile == null || !created.get("index").equals(profile.getProvenanceIndexName())) {
                throw new IllegalStateException("The managed graph must reference its generated provenance index.");
            }
            waitForSynchronization(indexes, indexers, sourceName, created.get("indexer"));
            SearchClient chunks = indexes.getSearchClient(created.get("index"));
            if (chunks.getDocumentCount() == 0) {
                throw new IllegalStateException("Ingestion completed without searchable source chunks.");
            }
            GraphFieldMappings fields = profile.getFields();
            String filter = fields.getGraphNameField() + " eq '" + graphName.replace("'", "''") + "'";
            SearchClient graph = indexes.getSearchClient(created.get("graphIndex"));
            List<Map<String, Object>> assertions = graph.search(new SearchOptions().setSearchText("*").setFilter(filter)
                .setSelect(fields.getAssertionIdField(), fields.getSubjectField(), fields.getPredicateField(),
                    fields.getObjectField(), fields.getSourceChunkIdField(), fields.getSourceParentIdField())
                .setTop(50)).stream().map(result -> result.getAdditionalProperties()).collect(Collectors.toList());
            if (assertions.isEmpty()) {
                throw new IllegalStateException("Synchronization completed without extracting graph assertions.");
            }
            System.out.println("Extracted assertions: " + assertions);
            GraphQueryResult relationships = graph.graph(new GraphOptions(
                "MATCH (h:Hotel)-[r:operatedBy]->(o:Organization) "
                    + "RETURN h.name AS hotel, o.name AS organization LIMIT 10")
                .setFilter(filter)
                .setProvenance(new GraphQueryProvenanceParameters(GraphQueryProvenanceKind.IDS).setMaxEvidencePerRow(5))
                .setIncludeStatistics(true).setIncludeDiagnostics(true));
            validateRelationships(relationships);
            for (GraphQueryRow row : relationships.getValue()) {
                System.out.println("Extracted hotel/operator relationship: " + row.getAdditionalProperties()
                    + "; evidence: " + (row.getProvenance() == null || row.getProvenance().getEvidence() == null
                        ? "not returned" : row.getProvenance().getEvidence().stream()
                            .map(evidence -> "edge=" + evidence.getEdgeId() + ", chunk=" + evidence.getChunkId()
                                + ", parent=" + evidence.getParentId()).collect(Collectors.joining("; "))));
            }
            System.out.println("Graph diagnostics (code, severity, phase): "
                + (relationships.getDiagnostics() == null ? "none" : relationships.getDiagnostics().stream()
                    .map(diagnostic -> diagnostic.getCode() + ", " + diagnostic.getSeverity()
                        + ", " + diagnostic.getPhase())
                    .collect(Collectors.toList())));
            resources.createKnowledgeBase(knowledgeBase(baseName, sourceName, chat));
            KnowledgeBaseRetrievalResult response = PreviewSampleSupport.retrievalClient(baseName)
                .retrieve(retrievalOptions(sourceName));
            validateGraphRetrieval(response);
            response.getReferences().forEach(reference -> System.out.println("KB graph reference "
                + reference.getId() + " (activity " + reference.getActivitySource() + "): "
                + reference.getSourceData()));
            response.getActivity().stream()
                .filter(activity -> activity instanceof KnowledgeBaseGraphSearchActivityRecord)
                .map(activity -> (KnowledgeBaseGraphSearchActivityRecord) activity)
                .filter(activity -> activity.getGraphSearchArguments() != null)
                .forEach(activity -> System.out.println(
                    "Generated GQL: " + activity.getGraphSearchArguments().getGql()));
            System.out.println("Cache configured; verify cache hits using separate execution/service telemetry.");
        }
    }

    static String approvedFolder(String folder, String approved) {
        String value = folder.trim();
        if (!"true".equalsIgnoreCase(approved) || value.isEmpty() || value.startsWith("/") || value.startsWith("\\")
            || value.replace("/", "").replace("\\", "").isEmpty()) {
            throw new IllegalArgumentException("Before creating Search resources, upload SYNTHETIC_HOTELS as "
                + "text/plain hotel_graph.txt into a dedicated approved nonempty AZURE_STORAGE_FOLDER. "
                + "Confirm it contains no unrelated data, then set SEARCH_GRAPH_SOURCE_APPROVED=true. "
                + "Container-root ingestion is not permitted.");
        }
        return value;
    }

    private static AzureOpenAIVectorizerParameters modelParameters(String endpoint, String deployment,
        String model, String apiKey) {
        return new AzureOpenAIVectorizerParameters().setResourceUrl(endpoint).setDeploymentName(deployment)
            .setModelName(AzureOpenAIModelName.fromString(model)).setApiKey(apiKey);
    }

    static AzureBlobKnowledgeSource knowledgeSource(String name, String graphName, String connection, String container,
        String folder, KnowledgeSourceIngestionParameters ingestion) {
        return new AzureBlobKnowledgeSource(name, new AzureBlobKnowledgeSourceParameters(connection, container)
            .setFolderPath(folder).setAdlsGen2(false).setIngestionParameters(ingestion)
            .setGraphExtraction(new KnowledgeSourceGraphExtractionParameters(graphName).setOntology(
                new KnowledgeSourceGraphOntology(KnowledgeSourceGraphOntologySourceKind.INLINE, ontology())
                    .setName("hotel-relationships"))))
            .setDescription("Synthetic hotel graph extraction using an inline ontology and enrichment cache.");
    }

    static String ontology() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("owl", "http://www.w3.org/2002/07/owl#");
        context.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        context.put("xsd", "http://www.w3.org/2001/XMLSchema#");
        context.put("ekg", "https://schema.search.azure.com/graph-extraction#");
        List<Map<String, Object>> nodes = Arrays.asList(
            ontologyNode("Hotel", "owl:Class", "A hotel property, distinct from the organization operating it."),
            ontologyNode("Organization", "owl:Class", "An organization operating hotel properties."),
            ontologyNode("Place", "owl:Class", "A geographic location."),
            ontologyRelationship("operatedBy", "Organization", "The organization operating the hotel."),
            ontologyRelationship("locatedIn", "Place", "The city where the hotel is located."));
        Map<String, Object> ontology = new LinkedHashMap<>();
        ontology.put("@context", context);
        ontology.put("@graph", nodes);
        return BinaryData.fromObject(ontology).toString();
    }

    private static Map<String, Object> ontologyNode(String name, String type, String comment) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("@id", "ekg:" + name);
        node.put("@type", type);
        node.put("rdfs:comment", comment);
        return node;
    }

    private static Map<String, Object> ontologyRelationship(String name, String range, String comment) {
        Map<String, Object> node = ontologyNode(name, "owl:ObjectProperty", comment);
        node.put("rdfs:domain", Collections.singletonMap("@id", "ekg:Hotel"));
        node.put("rdfs:range", Collections.singletonMap("@id", "ekg:" + range));
        return node;
    }

    static void validateGeneratedResources(String sourceName, Map<String, String> resources) {
        for (String kind : Arrays.asList("datasource", "indexer", "skillset", "index", "graphIndex")) {
            String name = resources.get(kind);
            if (name == null || !name.startsWith(sourceName + "-")) {
                throw new IllegalStateException("Refusing to use generated resources outside this knowledge source.");
            }
        }
    }

    private static void waitForActive(SearchIndexClient indexes, String sourceName) {
        for (int attempt = 0; attempt < 60; attempt++) {
            if (KnowledgeSourceSynchronizationStatus.ACTIVE.equals(
                indexes.getKnowledgeSourceStatus(sourceName).getSynchronizationStatus())) {
                return;
            }
            pause();
        }
        throw new IllegalStateException("The graph knowledge source did not become active within five minutes.");
    }

    static void waitForSynchronization(SearchIndexClient indexes, SearchIndexerClient indexers,
        String sourceName, String indexerName) {
        for (int attempt = 0; attempt < 60; attempt++) {
            KnowledgeSourceStatus status = indexes.getKnowledgeSourceStatus(sourceName);
            CompletedSynchronizationState completed = status.getLastSynchronizationState();
            IndexerExecutionResult execution = indexers.getIndexerStatus(indexerName).getLastResult();
            if (execution != null && (execution.getStatus() == null
                || IndexerExecutionStatus.TRANSIENT_FAILURE.equals(execution.getStatus()))) {
                throw new IllegalStateException(
                    "Graph indexer failed; inspect execution errors and dependency access.");
            }
            if (status.getCurrentSynchronizationState() == null && completed != null
                && completed.getEndTime() != null) {
                if (completed.getItemsUpdatesFailed() != 0) {
                    throw new IllegalStateException("Graph synchronization completed with failed source documents.");
                }
                if (execution != null && IndexerExecutionStatus.SUCCESS.equals(execution.getStatus())) {
                    if (execution.getFailedItemCount() != 0) {
                        throw new IllegalStateException("Graph indexer completed with failed source documents.");
                    }
                    System.out.println("Synchronization completed: " + completed.getEndTime()
                        + "; documents updated: " + completed.getItemsUpdatesProcessed()
                        + "; documents skipped: " + completed.getItemsSkipped()
                        + "; indexer warnings: "
                        + (execution.getWarnings() == null ? 0 : execution.getWarnings().size()));
                    return;
                }
            }
            pause();
        }
        throw new IllegalStateException("No successful graph synchronization completed within five minutes.");
    }

    private static void pause() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for graph ingestion.", ex);
        }
    }

    static void validateRelationships(GraphQueryResult result) {
        if (result.getValue() != null) {
            for (GraphQueryRow row : result.getValue()) {
                Map<String, Object> values = row.getAdditionalProperties();
                boolean expected = ("Historic Harbor Hotel".equals(values.get("hotel"))
                    && "Contoso Hospitality".equals(values.get("organization")))
                    || ("Mountain View Inn".equals(values.get("hotel"))
                    && "Alpine Hospitality".equals(values.get("organization")));
                if (expected && row.getProvenance() != null && row.getProvenance().getEvidence() != null
                    && !row.getProvenance().getEvidence().isEmpty()) {
                    return;
                }
            }
        }
        throw new IllegalStateException("No extracted synthetic hotel/operator relationship has source evidence.");
    }

    static KnowledgeBaseRetrievalOptions retrievalOptions(String sourceName) {
        return new KnowledgeBaseRetrievalOptions().setMessages(Collections.singletonList(new KnowledgeBaseMessage(
            new KnowledgeBaseMessageTextContent("Using the knowledge graph, which organizations operate "
                + "Historic Harbor Hotel and Mountain View Inn? Return each hotel and its operating organization."))
            .setRole("user"))).setIncludeActivity(true).setOutputMode(KnowledgeRetrievalOutputMode.EXTRACTIVE_DATA)
            .setKnowledgeSourceParams(Collections.singletonList(new AzureBlobKnowledgeSourceParams(sourceName)
                .setIncludeReferences(true).setIncludeReferenceSourceData(true)
                .setFailOnError(true).setGraphParameters(new AgentRuntimeGraphParams().setIncludeQuery(true)
                    .setProvenance(new AgentRuntimeGraphProvenanceParams()
                        .setKind(KnowledgeSourceGraphProvenanceKind.NONE)))));
    }

    static KnowledgeBase knowledgeBase(String name, String sourceName, KnowledgeBaseAzureOpenAIModel chat) {
        return new KnowledgeBase(name, new KnowledgeSourceReference(sourceName))
            .setModels(chat).setRetrievalReasoningEffort(new KnowledgeRetrievalLowReasoningEffort())
            .setRetrievalInstructions("For hotel/operator relationship questions, use the knowledge source's "
                + "graph search tool and its operatedBy relationships. Return each hotel's name and operating "
                + "organization. Do not substitute ordinary document search for the graph relationship query.")
            .setOutputMode(KnowledgeRetrievalOutputMode.EXTRACTIVE_DATA);
    }

    static void validateGraphRetrieval(KnowledgeBaseRetrievalResult result) {
        Set<Integer> graphActivityIds = result.getActivity() == null ? Collections.emptySet() : result.getActivity().stream()
            .filter(activity -> activity instanceof KnowledgeBaseGraphSearchActivityRecord)
            .map(activity -> (KnowledgeBaseGraphSearchActivityRecord) activity)
            .filter(activity -> activity.getError() == null && activity.getGraphSearchArguments() != null
                && activity.getGraphSearchArguments().getGql() != null
                && !activity.getGraphSearchArguments().getGql().trim().isEmpty())
            .map(activity -> activity.getId()).collect(Collectors.toSet());
        boolean references = result.getReferences() != null
            && result.getReferences().stream()
                .anyMatch(reference -> reference instanceof KnowledgeBaseGraphSearchReference
                    && graphActivityIds.contains(reference.getActivitySource()));
        boolean gql = !graphActivityIds.isEmpty();
        if (!references || !gql || result.getActivity().stream().anyMatch(activity -> activity.getError() != null)) {
            String activities = result.getActivity() == null ? "none" : result.getActivity().stream()
                .map(activity -> activity.getType() + (activity.getError() == null
                    ? "" : " (" + activity.getError().getCode() + ")"))
                .collect(Collectors.joining(", "));
            throw new IllegalStateException("KB retrieval must return graph references and successful generated GQL."
                + " Graph references: " + references + "; generated GQL: " + gql + "; activities: " + activities);
        }
    }
}
