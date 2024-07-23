// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.models;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Context;
import com.azure.search.documents.SearchAsyncClient;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.SearchRequestUrlRewriterPolicy;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.SearchIndexerAsyncClient;
import com.azure.search.documents.indexes.SearchIndexerClient;
import com.azure.search.documents.indexes.SearchIndexerClientBuilder;
import com.azure.search.documents.indexes.models.IndexDocumentsBatch;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexer;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceConnection;
import com.azure.search.documents.indexes.models.SearchIndexerSkillset;
import com.azure.search.documents.indexes.models.SynonymMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
public class SearchRequestUrlRewriterPolicyTests {
    @ParameterizedTest
    @MethodSource("correctUrlRewriteSupplier")
    public void correctUrlRewrite(Callable<?> apiCall, String expectedUrl) {
        try {
            apiCall.call();
        } catch (Exception ex) {
            UrlRewriteException urlRewriteException = Assertions.assertInstanceOf(UrlRewriteException.class, ex);
            assertTrue(urlRewriteException.rewrittenUrl.startsWith(expectedUrl),
                () -> "Expected URL to start with " + expectedUrl + " but was " + urlRewriteException.rewrittenUrl);
        }
    }

    public static Stream<Arguments> correctUrlRewriteSupplier() {
        HttpClient urlRewriteHttpClient =
            request -> Mono.error(new UrlRewriteException("Url rewritten", request.getUrl().toString()));

        SearchClientBuilder searchClientBuilder = new SearchClientBuilder()
            .indexName("test")
            .endpoint("https://test.search.windows.net")
            .credential(new MockTokenCredential())
            .retryOptions(new RetryOptions(new FixedDelayOptions(0, Duration.ofMillis(1))))
            .addPolicy(new SearchRequestUrlRewriterPolicy())
            .httpClient(urlRewriteHttpClient);
        SearchClient searchClient = searchClientBuilder.buildClient();
        SearchAsyncClient searchAsyncClient = searchClientBuilder.buildAsyncClient();

        SearchIndexClientBuilder searchIndexClientBuilder = new SearchIndexClientBuilder()
            .endpoint("https://test.search.windows.net")
            .credential(new MockTokenCredential())
            .retryOptions(new RetryOptions(new FixedDelayOptions(0, Duration.ofMillis(1))))
            .addPolicy(new SearchRequestUrlRewriterPolicy())
            .httpClient(urlRewriteHttpClient);
        SearchIndexClient searchIndexClient = searchIndexClientBuilder.buildClient();
        SearchIndexAsyncClient searchIndexAsyncClient = searchIndexClientBuilder.buildAsyncClient();

        SearchIndexerClientBuilder searchIndexerClientBuilder = new SearchIndexerClientBuilder()
            .endpoint("https://test.search.windows.net")
            .credential(new MockTokenCredential())
            .retryOptions(new RetryOptions(new FixedDelayOptions(0, Duration.ofMillis(1))))
            .addPolicy(new SearchRequestUrlRewriterPolicy())
            .httpClient(urlRewriteHttpClient);
        SearchIndexerClient searchIndexerClient = searchIndexerClientBuilder.buildClient();
        SearchIndexerAsyncClient searchIndexerAsyncClient = searchIndexerClientBuilder.buildAsyncClient();

        String docsUrl = "https://test.search.windows.net/indexes/test/docs";

        SearchIndex index = new SearchIndex("index");
        String indexUrl = "https://test.search.windows.net/indexes/index";

        SynonymMap synonymMap = new SynonymMap("synonym");
        String synonymMapUrl = "https://test.search.windows.net/synonymmaps/synonym";

        SearchIndexerDataSourceConnection dataSource = new SearchIndexerDataSourceConnection("datasource");
        String dataSourceUrl = "https://test.search.windows.net/datasources/datasource";

        SearchIndexer indexer = new SearchIndexer("indexer");
        String indexerUrl = "https://test.search.windows.net/indexers/indexer";

        SearchIndexerSkillset skillset = new SearchIndexerSkillset("skillset");
        String skillsetUrl = "https://test.search.windows.net/skillsets/skillset";

        return Stream.of(
            Arguments.of(toCallable(() -> searchClient.indexDocumentsWithResponse(new IndexDocumentsBatch<>(), null,
                Context.NONE)), docsUrl + "/search.index"),
            Arguments.of(toCallable(() -> searchClient.getDocumentWithResponse("test", SearchDocument.class, null,
                Context.NONE)), docsUrl + "/test"),
            Arguments.of(toCallable(() -> searchClient.getDocumentCountWithResponse(Context.NONE)),
                docsUrl + "/$count"),
            Arguments.of(toCallable(() -> searchClient.search("search", null, Context.NONE).iterator().hasNext()),
                docsUrl + "/search.post.search"),
            Arguments.of(toCallable(() -> searchClient.suggest("suggest", "suggester", null, Context.NONE)
                .iterator().hasNext()), docsUrl + "/seach.post.suggest"),
            Arguments.of(toCallable(() -> searchClient.autocomplete("autocomplete", "suggester", null, Context.NONE)
                .iterator().hasNext()), docsUrl + "/search.post.autocomplete"),

            Arguments.of(toCallable(searchAsyncClient.indexDocumentsWithResponse(new IndexDocumentsBatch<>(), null)),
                docsUrl + "/search.index"),
            Arguments.of(toCallable(searchAsyncClient.getDocumentWithResponse("test", SearchDocument.class, null)),
                docsUrl + "/test"),
            Arguments.of(toCallable(searchAsyncClient.getDocumentCountWithResponse()), docsUrl + "/$count"),
            Arguments.of(toCallable(searchAsyncClient.search("search", null)), docsUrl + "/search.post.search"),
            Arguments.of(toCallable(searchAsyncClient.suggest("suggest", "suggester", null)),
                docsUrl + "/search.post.suggest"),
            Arguments.of(toCallable(searchAsyncClient.autocomplete("autocomplete", "suggester", null)),
                docsUrl + "/search.post.autocomplete"),

            Arguments.of(toCallable(() -> searchIndexClient.createIndexWithResponse(index, Context.NONE)),
                "https://test.search.windows.net/indexes"),
            Arguments.of(toCallable(() -> searchIndexClient.getIndexWithResponse("index", Context.NONE)), indexUrl),
            Arguments.of(toCallable(() -> searchIndexClient.getIndexStatisticsWithResponse("index", Context.NONE)),
                indexUrl + "/search.stats"),
            Arguments.of(toCallable(() -> searchIndexClient.listIndexes(Context.NONE).iterator().hasNext()),
                "https://test.search.windows.net/indexes"),
            Arguments.of(toCallable(() -> searchIndexClient.listIndexNames(Context.NONE).iterator().hasNext()),
                "https://test.search.windows.net/indexes"),
            Arguments.of(toCallable(() -> searchIndexClient.createOrUpdateIndexWithResponse(index, false, false,
                Context.NONE)), indexUrl),
            Arguments.of(toCallable(() -> searchIndexClient.deleteIndexWithResponse(index, true, Context.NONE)),
                indexUrl),
            Arguments.of(toCallable(() -> searchIndexClient.analyzeText("index", null, Context.NONE)),
                indexUrl + "/search.analyze"),
            Arguments.of(toCallable(() -> searchIndexClient.createSynonymMapWithResponse(synonymMap, Context.NONE)),
                "https://test.search.windows.net/synonymmaps"),
            Arguments.of(toCallable(() -> searchIndexClient.getSynonymMapWithResponse("synonym", Context.NONE)),
                synonymMapUrl),
            Arguments.of(toCallable(() -> searchIndexClient.listSynonymMaps(Context.NONE).iterator().hasNext()),
                "https://test.search.windows.net/synonymmaps"),
            Arguments.of(toCallable(() -> searchIndexClient.listSynonymMapNames(Context.NONE).iterator().hasNext()),
                "https://test.search.windows.net/synonymmaps"),
            Arguments.of(toCallable(() -> searchIndexClient.createOrUpdateSynonymMapWithResponse(synonymMap, false,
                Context.NONE)), synonymMapUrl),
            Arguments.of(toCallable(() -> searchIndexClient.deleteSynonymMapWithResponse(synonymMap, true,
                Context.NONE)), synonymMapUrl),
            Arguments.of(toCallable(() -> searchIndexClient.getServiceStatisticsWithResponse(Context.NONE)),
                "https://test.search.windows.net/servicestats"),

            Arguments.of(toCallable(searchIndexAsyncClient.createIndexWithResponse(index)),
                "https://test.search.windows.net/indexes"),
            Arguments.of(toCallable(searchIndexAsyncClient.getIndexWithResponse("index")), indexUrl),
            Arguments.of(toCallable(searchIndexAsyncClient.getIndexStatisticsWithResponse("index")),
                indexUrl + "/search.stats"),
            Arguments.of(toCallable(searchIndexAsyncClient.listIndexes()), "https://test.search.windows.net/indexes"),
            Arguments.of(toCallable(searchIndexAsyncClient.listIndexNames()),
                "https://test.search.windows.net/indexes"),
            Arguments.of(toCallable(searchIndexAsyncClient.createOrUpdateIndexWithResponse(index, false, false)),
                indexUrl),
            Arguments.of(toCallable(searchIndexAsyncClient.deleteIndexWithResponse(index, true)), indexUrl),
            Arguments.of(toCallable(searchIndexAsyncClient.analyzeText("index", null)), indexUrl + "/search.analyze"),
            Arguments.of(toCallable(searchIndexAsyncClient.createSynonymMapWithResponse(synonymMap)),
                "https://test.search.windows.net/synonymmaps"),
            Arguments.of(toCallable(searchIndexAsyncClient.getSynonymMapWithResponse("synonym")), synonymMapUrl),
            Arguments.of(toCallable(searchIndexAsyncClient.listSynonymMaps()),
                "https://test.search.windows.net/synonymmaps"),
            Arguments.of(toCallable(searchIndexAsyncClient.listSynonymMapNames()),
                "https://test.search.windows.net/synonymmaps"),
            Arguments.of(toCallable(searchIndexAsyncClient.createOrUpdateSynonymMapWithResponse(synonymMap, false)),
                synonymMapUrl),
            Arguments.of(toCallable(searchIndexAsyncClient.deleteSynonymMapWithResponse(synonymMap, true)),
                synonymMapUrl),
            Arguments.of(toCallable(searchIndexAsyncClient.getServiceStatisticsWithResponse()),
                "https://test.search.windows.net/servicestats"),
            Arguments.of(toCallable(() -> searchIndexerClient.createOrUpdateDataSourceConnectionWithResponse(dataSource,
                true, Context.NONE)), dataSourceUrl),
            Arguments.of(toCallable(() -> searchIndexerClient.createDataSourceConnectionWithResponse(dataSource,
                Context.NONE)), "https://test.search.windows.net/datasources"),
            Arguments.of(toCallable(() -> searchIndexerClient.getDataSourceConnectionWithResponse("datasource",
                Context.NONE)), dataSourceUrl),
            Arguments.of(toCallable(() -> searchIndexerClient.listDataSourceConnections(Context.NONE).iterator()
                .hasNext()), "https://test.search.windows.net/datasources"),
            Arguments.of(toCallable(() -> searchIndexerClient.listDataSourceConnectionNames(Context.NONE).iterator()
                .hasNext()), "https://test.search.windows.net/datasources"),
            Arguments.of(toCallable(() -> searchIndexerClient.deleteDataSourceConnectionWithResponse(dataSource, true,
                Context.NONE)), dataSourceUrl),
            Arguments.of(toCallable(() -> searchIndexerClient.createIndexerWithResponse(indexer, Context.NONE)),
                "https://test.search.windows.net/indexers"),
            Arguments.of(toCallable(() -> searchIndexerClient.createOrUpdateIndexerWithResponse(indexer, false,
                Context.NONE)), indexerUrl),
            Arguments.of(toCallable(() -> searchIndexerClient.listIndexers(Context.NONE).iterator().hasNext()),
                "https://test.search.windows.net/indexers"),
            Arguments.of(toCallable(() -> searchIndexerClient.listIndexerNames(Context.NONE).iterator().hasNext()),
                "https://test.search.windows.net/indexers"),
            Arguments.of(toCallable(() -> searchIndexerClient.getIndexerWithResponse("indexer", Context.NONE)),
                indexerUrl),
            Arguments.of(toCallable(() -> searchIndexerClient.deleteIndexerWithResponse(indexer, true, Context.NONE)),
                indexerUrl),
            Arguments.of(toCallable(() -> searchIndexerClient.resetIndexerWithResponse("indexer", Context.NONE)),
                indexerUrl + "/search.reset"),
            Arguments.of(toCallable(() -> searchIndexerClient.runIndexerWithResponse("indexer", Context.NONE)),
                indexerUrl + "/search.run"),
            Arguments.of(toCallable(() -> searchIndexerClient.getIndexerStatusWithResponse("indexer", Context.NONE)),
                indexerUrl + "/search.status"),
            Arguments.of(toCallable(() -> searchIndexerClient.createSkillsetWithResponse(skillset, Context.NONE)),
                "https://test.search.windows.net/skillsets"),
            Arguments.of(toCallable(() -> searchIndexerClient.getSkillsetWithResponse("skillset", Context.NONE)),
                skillsetUrl),
            Arguments.of(toCallable(() -> searchIndexerClient.listSkillsets(Context.NONE).iterator().hasNext()),
                "https://test.search.windows.net/skillsets"),
            Arguments.of(toCallable(() -> searchIndexerClient.listSkillsetNames(Context.NONE).iterator().hasNext()),
                "https://test.search.windows.net/skillsets"),
            Arguments.of(toCallable(() -> searchIndexerClient.createOrUpdateSkillsetWithResponse(skillset, false,
                Context.NONE)), skillsetUrl),
            Arguments.of(toCallable(() -> searchIndexerClient.deleteSkillsetWithResponse(skillset, true, Context.NONE)),
                skillsetUrl),

            Arguments.of(toCallable(searchIndexerAsyncClient.createOrUpdateDataSourceConnectionWithResponse(dataSource,
                true)), dataSourceUrl),
            Arguments.of(toCallable(searchIndexerAsyncClient.createDataSourceConnectionWithResponse(dataSource)),
                "https://test.search.windows.net/datasources"),
            Arguments.of(toCallable(searchIndexerAsyncClient.getDataSourceConnectionWithResponse("datasource")),
                dataSourceUrl),
            Arguments.of(toCallable(searchIndexerAsyncClient.listDataSourceConnections()),
                "https://test.search.windows.net/datasources"),
            Arguments.of(toCallable(searchIndexerAsyncClient.listDataSourceConnectionNames()),
                "https://test.search.windows.net/datasources"),
            Arguments.of(toCallable(searchIndexerAsyncClient.deleteDataSourceConnectionWithResponse(dataSource, true)),
                dataSourceUrl),
            Arguments.of(toCallable(searchIndexerAsyncClient.createIndexerWithResponse(indexer)),
                "https://test.search.windows.net/indexers"),
            Arguments.of(toCallable(searchIndexerAsyncClient.createOrUpdateIndexerWithResponse(indexer, false)),
                indexerUrl),
            Arguments.of(toCallable(searchIndexerAsyncClient.listIndexers()),
                "https://test.search.windows.net/indexers"),
            Arguments.of(toCallable(searchIndexerAsyncClient.listIndexerNames()),
                "https://test.search.windows.net/indexers"),
            Arguments.of(toCallable(searchIndexerAsyncClient.getIndexerWithResponse("indexer")), indexerUrl),
            Arguments.of(toCallable(searchIndexerAsyncClient.deleteIndexerWithResponse(indexer, true)), indexerUrl),
            Arguments.of(toCallable(searchIndexerAsyncClient.resetIndexerWithResponse("indexer")),
                indexerUrl + "/search.reset"),
            Arguments.of(toCallable(searchIndexerAsyncClient.runIndexerWithResponse("indexer")),
                indexerUrl + "/search.run"),
            Arguments.of(toCallable(searchIndexerAsyncClient.getIndexerStatusWithResponse("indexer")),
                indexerUrl + "/search.status"),
            Arguments.of(toCallable(searchIndexerAsyncClient.createSkillsetWithResponse(skillset)),
                "https://test.search.windows.net/skillsets"),
            Arguments.of(toCallable(searchIndexerAsyncClient.getSkillsetWithResponse("skillset")), skillsetUrl),
            Arguments.of(toCallable(searchIndexerAsyncClient.listSkillsets()),
                "https://test.search.windows.net/skillsets"),
            Arguments.of(toCallable(searchIndexerAsyncClient.listSkillsetNames()),
                "https://test.search.windows.net/skillsets"),
            Arguments.of(toCallable(searchIndexerAsyncClient.createOrUpdateSkillsetWithResponse(skillset, false)),
                skillsetUrl),
            Arguments.of(toCallable(searchIndexerAsyncClient.deleteSkillsetWithResponse(skillset, true)), skillsetUrl),
            Arguments.of(toCallable(searchIndexerAsyncClient.deleteSkillsetWithResponse(skillset, true)), skillsetUrl)
        );
    }

    private static Callable<?> toCallable(Supplier<?> apiCall) {
        return () -> apiCall;
    }

    private static Callable<?> toCallable(Mono<?> apiCall) {
        return apiCall::block;
    }

    private static Callable<?> toCallable(Flux<?> apiCall) {
        return apiCall::blockFirst;
    }

    private static final class UrlRewriteException extends RuntimeException {
        private final String rewrittenUrl;
        UrlRewriteException(String message, String rewrittenUrl) {
            super(message);

            this.rewrittenUrl = rewrittenUrl;
        }
    }
}
