// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.models.GraphFieldMappings;
import com.azure.search.documents.indexes.models.GraphProfile;
import com.azure.search.documents.indexes.models.GraphProfileKind;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.models.GraphOptions;
import com.azure.search.documents.models.GraphQueryExecutionMode;
import com.azure.search.documents.models.GraphQueryExplainStage;
import com.azure.search.documents.models.GraphQueryProvenanceHydrateDocumentsParameters;
import com.azure.search.documents.models.GraphQueryProvenanceKind;
import com.azure.search.documents.models.GraphQueryProvenanceParameters;
import com.azure.search.documents.models.GraphQueryResult;
import com.azure.search.documents.models.GraphQueryRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates and populates an edge-list graph and its provenance index, then runs typed GQL queries.
 *
 * <p>Set SEARCH_ENDPOINT and SEARCH_API_KEY for a service supporting 2026-11-01-preview graph APIs.
 * The sample uses only synthetic hotel assertions. Unique indexes are created without overwriting existing
 * resources and cleaned up on exit. Set SEARCH_GRAPH_HYDRATE=true only when source hydration is already
 * enabled and authorized; this sample does not change service flags or permissions.</p>
 */
public class GraphIndexPreviewExample {
    private static final String QUERY = "MATCH (h:Hotel)-[r:operatedBy]->(o:Organization) "
        + "WHERE h.id = 'hotel:harbor' "
        + "RETURN h.name AS hotel, o.name AS organization, r.predicate AS relationship";

    /**
     * Runs the direct graph walkthrough.
     * @param args unused.
     */
    public static void main(String[] args) {
        SearchIndexClient indexes = PreviewSampleSupport.indexClient();
        String graphIndex = PreviewSampleSupport.name("hotels-ekg");
        String sourceIndex = PreviewSampleSupport.name("hotels-ekg-source");
        String graphName = "hotels";
        try (PreviewSampleSupport.Resources resources = new PreviewSampleSupport.Resources(indexes)) {
            resources.createIndex(provenanceIndex(sourceIndex));
            resources.createIndex(graphIndex(graphIndex, sourceIndex));
            PreviewSampleSupport.upload(indexes.getSearchClient(sourceIndex), sourceDocuments());
            SearchClient graph = indexes.getSearchClient(graphIndex);
            PreviewSampleSupport.upload(graph, assertionDocuments(graphName));
            System.out.println("Graph index: " + graphIndex + "; provenance index: " + sourceIndex);
            executeQueries(graph, graphName,
                Boolean.parseBoolean(PreviewSampleSupport.optional("SEARCH_GRAPH_HYDRATE", "false")));
        }
    }

    static SearchIndex provenanceIndex(String name) {
        return new SearchIndex(name, Arrays.asList(
            new SearchField("uid", SearchFieldDataType.STRING).setKey(true).setFilterable(true),
            new SearchField("snippet", SearchFieldDataType.STRING).setSearchable(true),
            new SearchField("snippet_parent_id", SearchFieldDataType.STRING).setFilterable(true)));
    }

    static SearchIndex graphIndex(String name, String provenanceName) {
        if (name.equals(provenanceName)) {
            throw new IllegalArgumentException("Graph and provenance indexes must be distinct.");
        }
        Set<String> facetable = new HashSet<>(Arrays.asList("subject", "predicate", "object", "object_string",
            "object_double", "subject_type", "subject_id", "object_type", "object_id", "graph_name"));
        Set<String> sortable = new HashSet<>(Arrays.asList("id", "subject", "object", "object_string", "object_double",
            "subject_id", "object_id", "graph_name"));
        List<SearchField> fields = new ArrayList<>();
        for (String field : Arrays.asList("id", "edge_hash", "subject", "predicate", "object", "object_string",
            "object_double", "object_bool", "object_datetime", "subject_type", "subject_id", "object_type", "object_id",
            "graph_name", "edge_confidence", "s_confidence", "p_confidence", "o_confidence", "chunk_id", "parent_id",
            "subject_id_display_name", "object_id_display_name")) {
            SearchFieldDataType type = SearchFieldDataType.STRING;
            if ("object_double".equals(field) || field.endsWith("confidence")) {
                type = SearchFieldDataType.DOUBLE;
            } else if ("object_bool".equals(field)) {
                type = SearchFieldDataType.BOOLEAN;
            } else if ("object_datetime".equals(field)) {
                type = SearchFieldDataType.DATE_TIME_OFFSET;
            }
            fields.add(new SearchField(field, type).setKey("id".equals(field)).setFilterable(true)
                .setFacetable(facetable.contains(field)).setSortable(sortable.contains(field)).setHidden(false));
        }
        GraphFieldMappings mappings = new GraphFieldMappings("id", "edge_hash", "subject", "predicate", "object",
            "object_string", "object_double", "object_bool", "object_datetime", "subject_type", "subject_id",
            "object_type", "object_id", "edge_confidence", "s_confidence", "p_confidence", "o_confidence", "chunk_id",
            "parent_id", "graph_name", "subject_id_display_name", "object_id_display_name");
        return new SearchIndex(name, fields).setGraphProfile(
            new GraphProfile(GraphProfileKind.EDGE_LIST, mappings).setProvenanceIndexName(provenanceName));
    }

    static List<Map<String, Object>> sourceDocuments() {
        List<Map<String, Object>> documents = new ArrayList<>();
        documents.add(sourceDocument("harbor-operator",
            "Historic Harbor Hotel is operated by Contoso Hospitality."));
        documents.add(sourceDocument("harbor-location", "Historic Harbor Hotel is located in Seattle."));
        return documents;
    }

    private static Map<String, Object> sourceDocument(String id, String text) {
        Map<String, Object> document = new LinkedHashMap<>();
        document.put("uid", id);
        document.put("snippet", text);
        document.put("snippet_parent_id", "harbor-brochure");
        return document;
    }

    static List<Map<String, Object>> assertionDocuments(String graphName) {
        List<Map<String, Object>> documents = new ArrayList<>();
        String[][] relationships = {
            {"operatedBy", "Contoso Hospitality", "Organization", "organization:contoso", "harbor-operator"},
            {"locatedIn", "Seattle", "Place", "place:seattle", "harbor-location"}
        };
        for (int i = 0; i < relationships.length; i++) {
            String[] relationship = relationships[i];
            Map<String, Object> assertion = new LinkedHashMap<>();
            assertion.put("id", "assertion-" + (i + 1));
            assertion.put("edge_hash", "fact-" + (i + 1));
            assertion.put("subject", "Historic Harbor Hotel");
            assertion.put("subject_type", "Hotel");
            assertion.put("subject_id", "hotel:harbor");
            assertion.put("predicate", relationship[0]);
            assertion.put("object", relationship[1]);
            assertion.put("object_string", relationship[1]);
            assertion.put("object_type", relationship[2]);
            assertion.put("object_id", relationship[3]);
            assertion.put("graph_name", graphName);
            for (String confidence : Arrays.asList("edge_confidence", "s_confidence", "p_confidence", "o_confidence")) {
                assertion.put(confidence, 1.0);
            }
            assertion.put("chunk_id", relationship[4]);
            assertion.put("parent_id", "harbor-brochure");
            assertion.put("subject_id_display_name", "Historic Harbor Hotel");
            assertion.put("object_id_display_name", relationship[1]);
            documents.add(assertion);
        }
        return documents;
    }

    static void executeQueries(SearchClient graph, String graphName, boolean hydrate) {
        String filter = "graph_name eq '" + graphName.replace("'", "''") + "'";
        GraphQueryResult plan = graph.graph(new GraphOptions(QUERY).setFilter(filter)
            .setExecutionMode(GraphQueryExecutionMode.PLAN_ONLY)
            .setExplain(Collections.singletonList(GraphQueryExplainStage.PHYSICAL_PLAN))
            .setIncludeStatistics(true).setIncludeDiagnostics(true));
        if ((plan.getValue() != null && !plan.getValue().isEmpty()) || plan.getStatistics() == null
            || !Integer.valueOf(0).equals(plan.getStatistics().getQueryCount()) || plan.getExplain() == null) {
            throw new IllegalStateException("Plan-only graph execution must return a plan without querying data.");
        }
        System.out.println("Physical plan: " + plan.getExplain().getAdditionalProperties());
        GraphQueryResult result = graph.graph(new GraphOptions(QUERY).setFilter(filter)
            .setProvenance(new GraphQueryProvenanceParameters(GraphQueryProvenanceKind.IDS).setMaxEvidencePerRow(5))
            .setIncludeStatistics(true).setIncludeDiagnostics(true));
        if (result.getValue() == null || result.getValue().size() != 1) {
            throw new IllegalStateException("Expected one seeded hotel/operator relationship.");
        }
        GraphQueryRow row = result.getValue().get(0);
        if (!"Contoso Hospitality".equals(row.getAdditionalProperties().get("organization"))
            || row.getProvenance() == null || row.getProvenance().getEvidence() == null
            || row.getProvenance().getEvidence().stream().noneMatch(evidence ->
                "assertion-1".equals(evidence.getEdgeId()) && "harbor-operator".equals(evidence.getChunkId())
                    && "harbor-brochure".equals(evidence.getParentId()))) {
            throw new IllegalStateException("The graph row must include the seeded assertion's source evidence.");
        }
        System.out.println("Relationship: " + row.getAdditionalProperties()
            + "; evidence: " + row.getProvenance().getEvidence().stream()
                .map(evidence -> "edge=" + evidence.getEdgeId() + ", chunk=" + evidence.getChunkId()
                    + ", parent=" + evidence.getParentId())
                .collect(Collectors.joining("; ")));
        if (result.getStatistics() != null) {
            System.out.println("Statistics: queries=" + result.getStatistics().getQueryCount()
                + ", documents=" + result.getStatistics().getDocumentsFetched()
                + ", totalMs=" + result.getStatistics().getTotalMs());
        }
        System.out.println("Diagnostics (code, severity, phase): " + (result.getDiagnostics() == null ? "none"
            : result.getDiagnostics().stream().map(diagnostic -> diagnostic.getCode() + ", "
                + diagnostic.getSeverity() + ", " + diagnostic.getPhase()).collect(Collectors.toList())));
        if (hydrate) {
            GraphQueryResult hydrated = graph.graph(new GraphOptions(QUERY).setFilter(filter)
                .setProvenance(new GraphQueryProvenanceParameters(GraphQueryProvenanceKind.HYDRATE)
                    .setMaxEvidencePerRow(5).setHydrateDocuments(
                        new GraphQueryProvenanceHydrateDocumentsParameters().setSelect("uid,snippet")))
                .setIncludeStatistics(false));
            if (hydrated.getProvenanceDocuments() == null
                || !hydrated.getProvenanceDocuments().getAdditionalProperties().containsKey("harbor-operator")
                || !sourceDocuments().get(0).get("snippet").equals(hydrated.getProvenanceDocuments()
                    .getAdditionalProperties().get("harbor-operator").getAdditionalProperties().get("snippet"))) {
                throw new IllegalStateException("Hydration did not return the supporting synthetic source text.");
            }
            hydrated.getProvenanceDocuments().getAdditionalProperties().forEach((id, document) ->
                System.out.println("Hydrated source " + id + ": "
                    + (document == null ? "not returned" : document.getAdditionalProperties())));
        } else {
            System.out.println("Hydration skipped; it requires existing service support and authorization.");
        }
    }
}
