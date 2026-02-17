// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.models;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Context;
import com.azure.search.documents.SearchAsyncClient;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchRequestUrlRewriterPolicy;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.SearchIndexerAsyncClient;
import com.azure.search.documents.indexes.SearchIndexerClient;
import com.azure.search.documents.indexes.SearchIndexerClientBuilder;
import com.azure.search.documents.indexes.models.AnalyzeTextOptions;
import com.azure.search.documents.indexes.models.DataSourceCredentials;
import com.azure.search.documents.indexes.models.SearchAlias;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexer;
import com.azure.search.documents.indexes.models.SearchIndexerDataContainer;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceConnection;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceType;
import com.azure.search.documents.indexes.models.SearchIndexerSkillset;
import com.azure.search.documents.indexes.models.SkillNames;
import com.azure.search.documents.indexes.models.SynonymMap;
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

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
public class SearchRequestUrlRewriterPolicyTests {
    @ParameterizedTest
    @MethodSource("correctUrlRewriteSupplier")
    public void correctUrlRewrite(Callable<?> apiCall, String expectedUrl) {
        UrlRewriteException urlRewriteException = assertThrows(UrlRewriteException.class, apiCall::call);
        assertTrue(urlRewriteException.rewrittenUrl.startsWith(expectedUrl),
            () -> "Expected URL to start with " + expectedUrl + " but was " + urlRewriteException.rewrittenUrl);
    }

    public static Stream<Arguments> correctUrlRewriteSupplier() {
        String endpoint = "https://test.search.windows.net";
        HttpClient urlRewriteHttpClient = new HttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return Mono.error(new UrlRewriteException("Url rewritten", request.getUrl().toString()));
            }

            @Override
            public HttpResponse sendSync(HttpRequest request, Context context) {
                throw new UrlRewriteException("Url rewritten", request.getUrl().toString());
            }
        };

        SearchIndexClientBuilder indexClientBuilder = new SearchIndexClientBuilder().endpoint(endpoint)
            .credential(new MockTokenCredential())
            .retryOptions(new RetryOptions(new FixedDelayOptions(0, Duration.ofMillis(1))))
            .addPolicy(new SearchRequestUrlRewriterPolicy())
            .httpClient(urlRewriteHttpClient);
        SearchIndexClient indexClient = indexClientBuilder.buildClient();
        SearchIndexAsyncClient indexAsyncClient = indexClientBuilder.buildAsyncClient();

        SearchClient searchClient = indexClient.getSearchClient("test");
        SearchAsyncClient searchAsyncClient = indexAsyncClient.getSearchAsyncClient("test");

        SearchIndexerClientBuilder indexerClientBuilder = new SearchIndexerClientBuilder().endpoint(endpoint)
            .credential(new MockTokenCredential())
            .retryOptions(new RetryOptions(new FixedDelayOptions(0, Duration.ofMillis(1))))
            .addPolicy(new SearchRequestUrlRewriterPolicy())
            .httpClient(urlRewriteHttpClient);
        SearchIndexerClient indexerClient = indexerClientBuilder.buildClient();
        SearchIndexerAsyncClient indexerAsyncClient = indexerClientBuilder.buildAsyncClient();

        String indexesUrl = endpoint + "/indexes";
        String docsUrl = indexesUrl + "/test/docs";
        String indexUrl = indexesUrl + "/index";

        String indexersUrl = endpoint + "/indexers";
        SearchIndex index = new SearchIndex("index");

        String synonymMapsUrl = endpoint + "/synonymmaps";
        SynonymMap synonymMap = new SynonymMap("synonym");
        String synonymUrl = synonymMapsUrl + "/synonym";

        String aliasesUrl = endpoint + "/aliases";
        SearchAlias alias = new SearchAlias("alias", emptyList());
        String aliasUrl = aliasesUrl + "/alias";

        String dataSourcesUrl = endpoint + "/datasources";
        SearchIndexerDataSourceConnection dataSource
            = new SearchIndexerDataSourceConnection("datasource", SearchIndexerDataSourceType.AZURE_BLOB,
                new DataSourceCredentials().setConnectionString("fake"), new SearchIndexerDataContainer("fake"));
        String dataSourceUrl = dataSourcesUrl + "/datasource";

        SearchIndexer indexer = new SearchIndexer("indexer", "dataSourceName", "targetIndexName");
        String indexerUrl = indexersUrl + "/indexer";

        String skillsetsUrl = endpoint + "/skillsets";
        SearchIndexerSkillset skillset = new SearchIndexerSkillset("skillset");
        String skillsetUrl = skillsetsUrl + "/skillset";

        String servicestatsUrl = endpoint + "/servicestats";

        return Stream.of(
            Arguments.of(toCallable(() -> searchClient.indexDocuments(new IndexDocumentsBatch())),
                docsUrl + "/search.index"),
            Arguments.of(toCallable(() -> searchClient.getDocumentWithResponse("test", null)), docsUrl + "/test"),
            Arguments.of(toCallable(() -> searchClient.getDocumentCountWithResponse(null)), docsUrl + "/$count"),
            Arguments.of(toCallable(() -> searchClient.search(null).iterator().hasNext()),
                docsUrl + "/search.post.search"),
            Arguments.of(toCallable(() -> searchClient.suggest(new SuggestOptions("suggest", "suggester"))),
                docsUrl + "/search.post.suggest"),
            Arguments.of(
                toCallable(() -> searchClient.autocomplete(new AutocompleteOptions("autocomplete", "suggester"))),
                docsUrl + "/search.post.autocomplete"),

            Arguments.of(
                toCallable(searchAsyncClient.indexDocumentsWithResponse(new IndexDocumentsBatch(), null, null)),
                docsUrl + "/search.index"),
            Arguments.of(toCallable(searchAsyncClient.getDocumentWithResponse("test", null)), docsUrl + "/test"),
            Arguments.of(toCallable(searchAsyncClient.getDocumentCountWithResponse(null)), docsUrl + "/$count"),
            Arguments.of(toCallable(searchAsyncClient.search(null)), docsUrl + "/search.post.search"),
            Arguments.of(toCallable(searchAsyncClient.suggest(new SuggestOptions("suggest", "suggester"))),
                docsUrl + "/search.post.suggest"),
            Arguments.of(
                toCallable(searchAsyncClient.autocomplete(new AutocompleteOptions("autocomplete", "suggester"))),
                docsUrl + "/search.post.autocomplete"),

            Arguments.of(toCallable(() -> indexClient.createIndexWithResponse(index, null)), indexesUrl),
            Arguments.of(toCallable(() -> indexClient.getIndexWithResponse("index", null)), indexUrl),
            Arguments.of(toCallable(() -> indexClient.getIndexStatisticsWithResponse("index", null)),
                indexUrl + "/search.stats"),
            Arguments.of(toCallable(() -> indexClient.listIndexes().iterator().hasNext()), indexesUrl),
            Arguments.of(toCallable(() -> indexClient.listIndexNames().iterator().hasNext()), indexesUrl),
            Arguments.of(toCallable(() -> indexClient.createOrUpdateIndexWithResponse(index, null)), indexUrl),
            Arguments.of(toCallable(() -> indexClient.deleteIndexWithResponse(index.getName(), null)), indexUrl),
            Arguments.of(toCallable(() -> indexClient.analyzeText("index", new AnalyzeTextOptions("text"))),
                indexUrl + "/search.analyze"),
            Arguments.of(toCallable(() -> indexClient.createSynonymMapWithResponse(synonymMap, null)), synonymMapsUrl),
            Arguments.of(toCallable(() -> indexClient.getSynonymMapWithResponse("synonym", null)), synonymUrl),
            Arguments.of(toCallable(indexClient::listSynonymMaps), synonymMapsUrl),
            Arguments.of(toCallable(indexClient::listSynonymMapNames), synonymMapsUrl),
            Arguments.of(toCallable(() -> indexClient.createOrUpdateSynonymMapWithResponse(synonymMap, null)),
                synonymUrl),
            Arguments.of(toCallable(() -> indexClient.deleteSynonymMapWithResponse(synonymMap.getName(), null)),
                synonymUrl),
            Arguments.of(toCallable(() -> indexClient.getServiceStatisticsWithResponse(null)), servicestatsUrl),

            Arguments.of(toCallable(() -> indexClient.createAliasWithResponse(alias, null)), aliasesUrl),
            Arguments.of(toCallable(() -> indexClient.createOrUpdateAliasWithResponse(alias, null)), aliasUrl),
            Arguments.of(toCallable(() -> indexClient.getAliasWithResponse("alias", null)), aliasUrl),
            Arguments.of(toCallable(() -> indexClient.deleteAliasWithResponse(alias.getName(), null)), aliasUrl),
            Arguments.of(toCallable(() -> indexClient.listAliases(null).iterator().hasNext()), aliasesUrl),

            Arguments.of(toCallable(indexAsyncClient.createIndexWithResponse(index, null)), indexesUrl),
            Arguments.of(toCallable(indexAsyncClient.getIndexWithResponse("index", null)), indexUrl),
            Arguments.of(toCallable(indexAsyncClient.getIndexStatisticsWithResponse("index", null)),
                indexUrl + "/search.stats"),
            Arguments.of(toCallable(indexAsyncClient.listIndexes()), indexesUrl),
            Arguments.of(toCallable(indexAsyncClient.listIndexNames()), indexesUrl),
            Arguments.of(toCallable(indexAsyncClient.createOrUpdateIndexWithResponse(index, null)), indexUrl),
            Arguments.of(toCallable(indexAsyncClient.deleteIndexWithResponse(index.getName(), null)), indexUrl),
            Arguments.of(toCallable(indexAsyncClient.analyzeText("index", new AnalyzeTextOptions("text"))),
                indexUrl + "/search.analyze"),
            Arguments.of(toCallable(indexAsyncClient.createSynonymMapWithResponse(synonymMap, null)), synonymMapsUrl),
            Arguments.of(toCallable(indexAsyncClient.getSynonymMapWithResponse("synonym", null)), synonymUrl),
            Arguments.of(toCallable(indexAsyncClient.listSynonymMaps()), synonymMapsUrl),
            Arguments.of(toCallable(indexAsyncClient.listSynonymMapNames()), synonymMapsUrl),
            Arguments.of(toCallable(indexAsyncClient.createOrUpdateSynonymMapWithResponse(synonymMap, null)),
                synonymUrl),
            Arguments.of(toCallable(indexAsyncClient.deleteSynonymMapWithResponse(synonymMap.getName(), null)),
                synonymUrl),
            Arguments.of(toCallable(indexAsyncClient.getServiceStatisticsWithResponse(null)), servicestatsUrl),
            Arguments.of(toCallable(() -> indexClient.createAliasWithResponse(alias, null)), aliasesUrl),
            Arguments.of(toCallable(() -> indexClient.createOrUpdateAliasWithResponse(alias, null)), aliasUrl),
            Arguments.of(toCallable(() -> indexClient.getAliasWithResponse("alias", null)), aliasUrl),
            Arguments.of(toCallable(() -> indexClient.deleteAliasWithResponse(alias.getName(), null)), aliasUrl),
            Arguments.of(toCallable(() -> indexClient.listAliases(null).iterator().hasNext()), aliasesUrl),
            Arguments.of(
                toCallable(() -> indexerClient.createOrUpdateDataSourceConnectionWithResponse(dataSource, null)),
                dataSourceUrl),
            Arguments.of(toCallable(() -> indexerClient.createDataSourceConnectionWithResponse(dataSource, null)),
                dataSourcesUrl),
            Arguments.of(toCallable(() -> indexerClient.getDataSourceConnectionWithResponse("datasource", null)),
                dataSourceUrl),
            Arguments.of(toCallable(indexerClient::listDataSourceConnections), dataSourcesUrl),
            Arguments.of(toCallable(indexerClient::listDataSourceConnectionNames), dataSourcesUrl),
            Arguments.of(
                toCallable(() -> indexerClient.deleteDataSourceConnectionWithResponse(dataSource.getName(), null)),
                dataSourceUrl),
            Arguments.of(toCallable(() -> indexerClient.createIndexerWithResponse(indexer, null)), indexersUrl),
            Arguments.of(toCallable(() -> indexerClient.createOrUpdateIndexerWithResponse(indexer, null)), indexerUrl),
            Arguments.of(toCallable(indexerClient::listIndexers), indexersUrl),
            Arguments.of(toCallable(indexerClient::listIndexerNames), indexersUrl),
            Arguments.of(toCallable(() -> indexerClient.getIndexerWithResponse("indexer", null)), indexerUrl),
            Arguments.of(toCallable(() -> indexerClient.deleteIndexerWithResponse(indexer.getName(), null)),
                indexerUrl),
            Arguments.of(toCallable(() -> indexerClient.resetIndexerWithResponse("indexer", null)),
                indexerUrl + "/search.reset"),
            Arguments.of(toCallable(() -> indexerClient.runIndexerWithResponse("indexer", null)),
                indexerUrl + "/search.run"),
            Arguments.of(toCallable(() -> indexerClient.getIndexerStatusWithResponse("indexer", null)),
                indexerUrl + "/search.status"),
            Arguments.of(toCallable(() -> indexerClient.resetDocumentsWithResponse(indexer.getName(), null)),
                indexerUrl + "/search.resetdocs"),
            Arguments.of(toCallable(() -> indexerClient.createSkillsetWithResponse(skillset, null)), skillsetsUrl),
            Arguments.of(toCallable(() -> indexerClient.getSkillsetWithResponse("skillset", null)), skillsetUrl),
            Arguments.of(toCallable(indexerClient::listSkillsets), skillsetsUrl),
            Arguments.of(toCallable(indexerClient::listSkillsetNames), skillsetsUrl),
            Arguments.of(toCallable(() -> indexerClient.createOrUpdateSkillsetWithResponse(skillset, null)),
                skillsetUrl),
            Arguments.of(toCallable(() -> indexerClient.deleteSkillsetWithResponse(skillset.getName(), null)),
                skillsetUrl),
            Arguments.of(
                toCallable(() -> indexerClient.resetSkillsWithResponse(skillset.getName(), new SkillNames(), null)),
                skillsetUrl + "/search.resetskills"),

            Arguments.of(
                toCallable(indexerAsyncClient.createOrUpdateDataSourceConnectionWithResponse(dataSource, null)),
                dataSourceUrl),
            Arguments.of(toCallable(indexerAsyncClient.createDataSourceConnectionWithResponse(dataSource, null)),
                dataSourcesUrl),
            Arguments.of(toCallable(indexerAsyncClient.getDataSourceConnectionWithResponse("datasource", null)),
                dataSourceUrl),
            Arguments.of(toCallable(indexerAsyncClient.listDataSourceConnections()), dataSourcesUrl),
            Arguments.of(toCallable(indexerAsyncClient.listDataSourceConnectionNames()), dataSourcesUrl),
            Arguments.of(
                toCallable(indexerAsyncClient.deleteDataSourceConnectionWithResponse(dataSource.getName(), null)),
                dataSourceUrl),
            Arguments.of(toCallable(indexerAsyncClient.createIndexerWithResponse(indexer, null)), indexersUrl),
            Arguments.of(toCallable(indexerAsyncClient.createOrUpdateIndexerWithResponse(indexer, null)), indexerUrl),
            Arguments.of(toCallable(indexerAsyncClient.listIndexers()), indexersUrl),
            Arguments.of(toCallable(indexerAsyncClient.listIndexerNames()), indexersUrl),
            Arguments.of(toCallable(indexerAsyncClient.getIndexerWithResponse("indexer", null)), indexerUrl),
            Arguments.of(toCallable(indexerAsyncClient.deleteIndexerWithResponse(indexer.getName(), null)), indexerUrl),
            Arguments.of(toCallable(indexerAsyncClient.resetIndexerWithResponse("indexer", null)),
                indexerUrl + "/search.reset"),
            Arguments.of(toCallable(indexerAsyncClient.runIndexerWithResponse("indexer", null)),
                indexerUrl + "/search.run"),
            Arguments.of(toCallable(indexerAsyncClient.getIndexerStatusWithResponse("indexer", null)),
                indexerUrl + "/search.status"),
            Arguments.of(toCallable(indexerAsyncClient.resetDocumentsWithResponse(indexer.getName(), null)),
                indexerUrl + "/search.resetdocs"),
            Arguments.of(toCallable(indexerAsyncClient.createSkillsetWithResponse(skillset, null)), skillsetsUrl),
            Arguments.of(toCallable(indexerAsyncClient.getSkillsetWithResponse("skillset", null)), skillsetUrl),
            Arguments.of(toCallable(indexerAsyncClient.listSkillsets()), skillsetsUrl),
            Arguments.of(toCallable(indexerAsyncClient.listSkillsetNames()), skillsetsUrl),
            Arguments.of(toCallable(indexerAsyncClient.createOrUpdateSkillsetWithResponse(skillset, null)),
                skillsetUrl),
            Arguments.of(toCallable(indexerAsyncClient.deleteSkillsetWithResponse(skillset.getName(), null)),
                skillsetUrl),
            Arguments.of(
                toCallable(indexerAsyncClient.resetSkillsWithResponse(skillset.getName(), new SkillNames(), null)),
                skillsetUrl + "/search.resetskills"));
    }

    private static Callable<?> toCallable(Supplier<?> apiCall) {
        return apiCall::get;
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
