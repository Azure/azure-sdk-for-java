// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.search.documents.indexes.models.IndexStatisticsSummary;
import com.azure.search.documents.indexes.models.KnowledgeBase;
import com.azure.search.documents.indexes.models.KnowledgeSource;
import com.azure.search.documents.indexes.models.KnowledgeSourceFile;
import com.azure.search.documents.indexes.models.SearchAlias;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexResponse;
import com.azure.search.documents.indexes.models.SearchIndexer;
import com.azure.search.documents.indexes.models.SearchIndexerDataSourceConnection;
import com.azure.search.documents.indexes.models.SearchIndexerSkillset;
import com.azure.search.documents.indexes.models.SynonymMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SearchListingTests {
    private static final String ENDPOINT = "https://example.search.windows.net";
    private static final AzureKeyCredential CREDENTIAL = new AzureKeyCredential("key");
    private static final String NEXT_LINK = ENDPOINT + "/next-page?api-version=2026-10-01&$skiptoken=opaque%2B%2F%3D";

    @ParameterizedTest
    @MethodSource("listings")
    public void listingsAreTypedLazyAndResumable(String path, String properties,
        Function<HttpClient, PagedIterable<String>> sync, Function<HttpClient, PagedFlux<String>> async) {
        AtomicInteger requests = new AtomicInteger();
        HttpClient httpClient = request -> {
            boolean nextPage = request.getUrl().getPath().equals("/next-page");
            if (nextPage) {
                assertEquals(NEXT_LINK, request.getUrl().toExternalForm());
            } else {
                assertEquals("/" + path, request.getUrl().getPath());
                assertTrue(request.getUrl().getQuery().contains("search=prefix"));
                assertTrue(request.getUrl().getQuery().contains("pageSize=1"));
                assertTrue(request.getUrl().getQuery().contains("searchType=prefix"));
            }
            requests.incrementAndGet();
            String name = nextPage ? "second" : "first";
            String body = "{\"value\":[{\"name\":\"" + name + "\",\"fileId\":\"" + name + "\"," + properties + "}]"
                + (nextPage ? "" : ",\"@odata.nextLink\":\"" + NEXT_LINK + "\"") + "}";
            return Mono.just(new MockHttpResponse(request, 200,
                new HttpHeaders().set(HttpHeaderName.fromString("x-ms-request-id"), name),
                body.getBytes(StandardCharsets.UTF_8)));
        };

        PagedIterable<String> iterable = sync.apply(httpClient);
        assertEquals(0, requests.get());
        List<PagedResponse<String>> pages = new ArrayList<>();
        iterable.iterableByPage().forEach(pages::add);
        assertPages(pages);
        assertEquals(2, requests.get());
        assertEquals(Collections.singletonList("second"),
            iterable.iterableByPage(NEXT_LINK).iterator().next().getValue());
        assertEquals(3, requests.get());

        requests.set(0);
        PagedFlux<String> flux = async.apply(httpClient);
        assertEquals(0, requests.get());
        assertPages(flux.byPage().collectList().block());
        assertEquals(2, requests.get());
        PagedResponse<String> resumed = flux.byPage(NEXT_LINK).blockFirst();
        assertNotNull(resumed);
        assertEquals(Collections.singletonList("second"), resumed.getValue());
        assertEquals(3, requests.get());

        requests.set(0);
        assertEquals("first", flux.take(1).blockLast());
        assertEquals(1, requests.get());
    }

    @ParameterizedTest
    @MethodSource("listings")
    public void listingFailuresArePropagated(String path, String properties,
        Function<HttpClient, PagedIterable<String>> sync, Function<HttpClient, PagedFlux<String>> async) {
        HttpClient httpClient = request -> Mono.just(new MockHttpResponse(request, 404, new HttpHeaders(),
            "{\"error\":{\"code\":\"NotFound\",\"message\":\"Missing resource\"}}".getBytes(StandardCharsets.UTF_8)));
        assertThrows(HttpResponseException.class, () -> sync.apply(httpClient).iterator().hasNext());
        assertThrows(HttpResponseException.class, () -> async.apply(httpClient).blockLast());
    }

    private static Stream<Arguments> listings() {
        return Stream.of(
            listing("indexes", "\"fields\":[]",
                http -> indexes(http).buildClient().listIndexes(options()).mapPage(SearchIndex::getName),
                http -> indexes(http).buildAsyncClient().listIndexes(options()).mapPage(SearchIndex::getName)),
            listing("indexes", "\"fields\":[]",
                http -> indexes(http).buildClient()
                    .listIndexesWithSelectedProperties(Collections.singletonList("name"), options())
                    .mapPage(SearchIndexResponse::getName),
                http -> indexes(http).buildAsyncClient()
                    .listIndexesWithSelectedProperties(Collections.singletonList("name"), options())
                    .mapPage(SearchIndexResponse::getName)),
            listing("aliases", "\"indexes\":[\"index\"]",
                http -> indexes(http).buildClient().listAliases(options()).mapPage(SearchAlias::getName),
                http -> indexes(http).buildAsyncClient().listAliases(options()).mapPage(SearchAlias::getName)),
            listing("synonymmaps", "\"format\":\"solr\",\"synonyms\":\"hotel,motel\"",
                http -> indexes(http).buildClient().listSynonymMaps(options()).mapPage(SynonymMap::getName),
                http -> indexes(http).buildAsyncClient().listSynonymMaps(options()).mapPage(SynonymMap::getName)),
            listing("knowledgebases", "\"knowledgeSources\":[{\"name\":\"source\"}]",
                http -> indexes(http).buildClient().listKnowledgeBases(options()).mapPage(KnowledgeBase::getName),
                http -> indexes(http).buildAsyncClient().listKnowledgeBases(options()).mapPage(KnowledgeBase::getName)),
            listing("knowledgesources", "\"kind\":\"file\",\"fileParameters\":{}",
                http -> indexes(http).buildClient().listKnowledgeSources(options()).mapPage(KnowledgeSource::getName),
                http -> indexes(http).buildAsyncClient()
                    .listKnowledgeSources(options())
                    .mapPage(KnowledgeSource::getName)),
            listing("knowledgesources('files')/files", "\"fileName\":\"notes.md\"",
                http -> indexes(http).buildClient()
                    .listKnowledgeSourceFiles("files", options())
                    .mapPage(KnowledgeSourceFile::getFileId),
                http -> indexes(http).buildAsyncClient()
                    .listKnowledgeSourceFiles("files", options())
                    .mapPage(KnowledgeSourceFile::getFileId)),
            listing("indexstats", "\"documentCount\":1,\"storageSize\":2,\"vectorIndexSize\":3",
                http -> indexes(http).buildClient()
                    .listIndexStatsSummary(options())
                    .mapPage(IndexStatisticsSummary::getName),
                http -> indexes(http).buildAsyncClient()
                    .listIndexStatsSummary(options())
                    .mapPage(IndexStatisticsSummary::getName)),
            listing("datasources",
                "\"type\":\"azureblob\",\"credentials\":{\"connectionString\":\"test\"},"
                    + "\"container\":{\"name\":\"container\"}",
                http -> indexers(http).buildClient()
                    .listDataSourceConnections(options())
                    .mapPage(SearchIndexerDataSourceConnection::getName),
                http -> indexers(http).buildAsyncClient()
                    .listDataSourceConnections(options())
                    .mapPage(SearchIndexerDataSourceConnection::getName)),
            listing("indexers", "\"dataSourceName\":\"source\",\"targetIndexName\":\"index\"",
                http -> indexers(http).buildClient().listIndexers(options()).mapPage(SearchIndexer::getName),
                http -> indexers(http).buildAsyncClient().listIndexers(options()).mapPage(SearchIndexer::getName)),
            listing("skillsets", "\"skills\":[]",
                http -> indexers(http).buildClient().listSkillsets(options()).mapPage(SearchIndexerSkillset::getName),
                http -> indexers(http).buildAsyncClient()
                    .listSkillsets(options())
                    .mapPage(SearchIndexerSkillset::getName)));
    }

    @ParameterizedTest
    @MethodSource("nameListings")
    public void nameListingsRequestOnlyNames(Function<HttpClient, PagedIterable<String>> sync,
        Function<HttpClient, PagedFlux<String>> async) {
        AtomicInteger requests = new AtomicInteger();
        HttpClient httpClient = request -> {
            assertTrue(request.getUrl().getQuery().contains("select=name"));
            requests.incrementAndGet();
            return Mono.just(new MockHttpResponse(request, 200, new HttpHeaders(),
                "{\"value\":[{\"name\":\"first\"}]}".getBytes(StandardCharsets.UTF_8)));
        };
        assertEquals(Collections.singletonList("first"),
            sync.apply(httpClient).stream().collect(java.util.stream.Collectors.toList()));
        assertEquals(Collections.singletonList("first"), async.apply(httpClient).collectList().block());
        assertEquals(2, requests.get());
    }

    private static Stream<Arguments> nameListings() {
        return Stream.of(
            names(http -> indexes(http).buildClient().listSynonymMapNames(),
                http -> indexes(http).buildAsyncClient().listSynonymMapNames()),
            names(http -> indexers(http).buildClient().listDataSourceConnectionNames(),
                http -> indexers(http).buildAsyncClient().listDataSourceConnectionNames()),
            names(http -> indexers(http).buildClient().listIndexerNames(),
                http -> indexers(http).buildAsyncClient().listIndexerNames()),
            names(http -> indexers(http).buildClient().listSkillsetNames(),
                http -> indexers(http).buildAsyncClient().listSkillsetNames()));
    }

    @Test
    public void publicListingsHaveTypedSyncAndAsyncCounterparts() throws NoSuchMethodException {
        for (Class<?> client : Arrays.asList(SearchIndexClient.class, SearchIndexerClient.class)) {
            Class<?> asyncClient
                = client == SearchIndexClient.class ? SearchIndexAsyncClient.class : SearchIndexerAsyncClient.class;
            for (Method method : client.getDeclaredMethods()) {
                if (!Modifier.isPublic(method.getModifiers()) || !method.getName().startsWith("list")) {
                    continue;
                }
                assertEquals(PagedIterable.class, method.getReturnType());
                Method counterpart = asyncClient.getMethod(method.getName(), method.getParameterTypes());
                assertEquals(PagedFlux.class, counterpart.getReturnType());
                ParameterizedType type = assertInstanceOf(ParameterizedType.class, method.getGenericReturnType());
                assertFalse(Arrays.asList(type.getActualTypeArguments()).contains(BinaryData.class));
                ParameterizedType asyncType
                    = assertInstanceOf(ParameterizedType.class, counterpart.getGenericReturnType());
                assertEquals(Arrays.asList(type.getActualTypeArguments()),
                    Arrays.asList(asyncType.getActualTypeArguments()));
            }
        }
    }

    private static void assertPages(List<PagedResponse<String>> pages) {
        assertNotNull(pages);
        assertEquals(2, pages.size());
        assertEquals(Collections.singletonList("first"), pages.get(0).getValue());
        assertEquals(Collections.singletonList("second"), pages.get(1).getValue());
        assertEquals(NEXT_LINK, pages.get(0).getContinuationToken());
        assertNull(pages.get(1).getContinuationToken());
        assertEquals(200, pages.get(0).getStatusCode());
        assertEquals("first", pages.get(0).getHeaders().getValue(HttpHeaderName.fromString("x-ms-request-id")));
    }

    private static Arguments listing(String path, String properties, Function<HttpClient, PagedIterable<String>> sync,
        Function<HttpClient, PagedFlux<String>> async) {
        return Arguments.of(path, properties, sync, async);
    }

    private static Arguments names(Function<HttpClient, PagedIterable<String>> sync,
        Function<HttpClient, PagedFlux<String>> async) {
        return Arguments.of(sync, async);
    }

    private static RequestOptions options() {
        return new RequestOptions().addQueryParam("search", "prefix")
            .addQueryParam("pageSize", "1")
            .addQueryParam("searchType", "prefix");
    }

    private static SearchIndexClientBuilder indexes(HttpClient httpClient) {
        return new SearchIndexClientBuilder().endpoint(ENDPOINT).credential(CREDENTIAL).httpClient(httpClient);
    }

    private static SearchIndexerClientBuilder indexers(HttpClient httpClient) {
        return new SearchIndexerClientBuilder().endpoint(ENDPOINT).credential(CREDENTIAL).httpClient(httpClient);
    }
}
