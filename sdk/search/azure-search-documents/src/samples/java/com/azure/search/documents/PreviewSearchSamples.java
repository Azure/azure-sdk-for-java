// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.BinaryData;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.models.RandomScoringFunction;
import com.azure.search.documents.indexes.models.RandomScoringParameters;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceFilterHint;
import com.azure.search.documents.indexes.models.SearchIndexKnowledgeSourceFilterHintAutoConfiguration;
import com.azure.search.documents.models.BalancedVectorQueryTopicGraph;
import com.azure.search.documents.models.GraphOptions;
import com.azure.search.documents.models.GraphQueryExecutionMode;
import com.azure.search.documents.models.GraphQueryExplainStage;
import com.azure.search.documents.models.GraphQueryResult;
import com.azure.search.documents.models.GraphQueryError;
import com.azure.search.documents.models.VectorizedQuery;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

/**
 * Examples using existing indexes with the November 2026 preview API.
 */
public final class PreviewSearchSamples {
    /**
     * Reads optional service-supplied listing totals without enumerating every page.
     * A missing count remains null; this API does not request counts through an undocumented query parameter.
     *
     * @param client the index management client.
     */
    public void listIndexesWithMetadata(SearchIndexClient client) {
        client.listIndexesWithMetadata(null).streamByPage().limit(1).forEach(page -> {
            System.out.printf("Service-supplied total: %s%n", page.getCount());
            page.getValue().forEach(index -> System.out.println(index.getName()));
        });
    }

    /**
     * Parses typed graph diagnostics without replacing the client's existing exception handling.
     *
     * @param exception the exception returned by a graph request.
     * @return the standard error and optional graph diagnostics.
     */
    public Mono<GraphQueryError> graphErrorDetails(HttpResponseException exception) {
        return exception.getResponse().getBodyAsString()
            .map(body -> BinaryData.fromString(body).toObject(GraphQueryError.class));
    }

    /**
     * Plans a graph query without executing backend searches.
     *
     * @param client a client for an existing graph index.
     * @return the plan and diagnostics.
     */
    public GraphQueryResult planGraph(SearchClient client) {
        return client.graph(new GraphOptions("MATCH (n) RETURN n")
            .setExecutionMode(GraphQueryExecutionMode.PLAN_ONLY)
            .setExplain(Collections.singletonList(GraphQueryExplainStage.LOGICAL_PLAN))
            .setIncludeDiagnostics(true));
    }

    /**
     * Executes a graph query asynchronously.
     *
     * @param client a client for an existing graph index.
     * @return the projected rows and execution statistics.
     */
    public Mono<GraphQueryResult> queryGraph(SearchAsyncClient client) {
        return client.graph(new GraphOptions("MATCH (n) RETURN n").setIncludeStatistics(true));
    }

    /**
     * Creates a balanced topic graph vector query.
     *
     * @param vector the query vector matching the index dimensions.
     * @return the query for a topic-graph-enabled vector field.
     */
    public VectorizedQuery balancedVector(List<Float> vector) {
        return new VectorizedQuery(vector).setFields("contentVector")
            .setTopicGraph(new BalancedVectorQueryTopicGraph().setMinDiversityPoolSize(500));
    }

    /**
     * Grounds a filter hint using cached facet values.
     *
     * @return the knowledge source's category filter hint.
     */
    public SearchIndexKnowledgeSourceFilterHint automaticFilterHint() {
        return new SearchIndexKnowledgeSourceFilterHint("category").setAutoConfiguration(
            new SearchIndexKnowledgeSourceFilterHintAutoConfiguration().setCacheTtl(900).setMaxFacetValueCount(32));
    }

    /**
     * Creates seeded random scoring for a scoring profile.
     *
     * @return the scoring function. Supply the seed through the named query scoring parameter.
     */
    public RandomScoringFunction seededRandomScoring() {
        return new RandomScoringFunction().setParameters(new RandomScoringParameters("seed"));
    }
}
