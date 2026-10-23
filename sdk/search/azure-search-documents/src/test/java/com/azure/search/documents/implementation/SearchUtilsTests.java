// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.models.HybridSearch;
import com.azure.search.documents.models.AgenticSearchOptions;
import com.azure.search.documents.models.QueryLanguage;
import com.azure.search.documents.models.QueryRewritesType;
import com.azure.search.documents.models.QuerySpellerType;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchRequest;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class SearchUtilsTests {
    @Test
    public void transfersPreviewOptionsToSearchRequest() {
        HybridSearch hybridSearch = new HybridSearch().setMaxTextRecallSize(100);
        AgenticSearchOptions agenticOptions = new AgenticSearchOptions();
        SearchOptions options = new SearchOptions().setQueryLanguage(QueryLanguage.EN_US)
            .setQuerySpeller(QuerySpellerType.LEXICON)
            .setQueryRewrites(QueryRewritesType.GENERATIVE)
            .setSemanticFields(Arrays.asList("title", "content"))
            .setHybridSearch(hybridSearch)
            .setMoreLikeThis("document-id")
            .setAgenticOptions(agenticOptions);

        SearchRequest request = SearchUtils.fromSearchOptions(options);

        assertEquals(options.getQueryLanguage(), request.getQueryLanguage());
        assertEquals(options.getQuerySpeller(), request.getQuerySpeller());
        assertEquals(options.getQueryRewrites(), request.getQueryRewrites());
        assertEquals(options.getSemanticFields(), request.getSemanticFields());
        assertSame(hybridSearch, request.getHybridSearch());
        assertEquals("document-id", request.getMoreLikeThis());
        assertSame(agenticOptions, request.getAgenticOptions());
        assertNull(SearchUtils.fromSearchOptions(null));
    }

    @Test
    public void preservesAuthorizationAcrossSearchPages() {
        AtomicInteger requests = new AtomicInteger();
        SearchClientBuilder builder = new SearchClientBuilder().endpoint("https://example.search.windows.net")
            .indexName("documents")
            .credential(new AzureKeyCredential("key"))
            .httpClient(request -> {
                assertEquals("source-token",
                    request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-query-source-authorization")));
                String json = requests.incrementAndGet() % 2 != 0
                    ? "{\"value\":[{\"@search.score\":1,\"id\":\"1\"}],"
                        + "\"@search.nextPageParameters\":{\"search\":\"*\"}}"
                    : "{\"value\":[{\"@search.score\":1,\"id\":\"2\"}]}";
                return Mono.just(new MockHttpResponse(request, 200, json.getBytes(StandardCharsets.UTF_8)));
            });
        SearchOptions options = new SearchOptions().setQuerySourceAuthorization("source-token");
        assertEquals(2, builder.buildClient().search(options).stream().count());
        assertEquals(2, builder.buildAsyncClient().search(options).collectList().block().size());
        assertEquals(4, requests.get());
    }

    @Test
    public void forwardsAuthorizationHeadersFromSearchOptions() {
        SearchClient client = new SearchClientBuilder().endpoint("https://example.search.windows.net")
            .indexName("documents")
            .credential(new AzureKeyCredential("key"))
            .httpClient(request -> {
                assertEquals("source-token",
                    request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-query-source-authorization")));
                assertEquals("false",
                    request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-enable-elevated-read")));
                return Mono.just(new MockHttpResponse(request, 200, "{\"value\":[]}".getBytes(StandardCharsets.UTF_8)));
            })
            .buildClient();

        assertEquals(0,
            client.search(new SearchOptions().setQuerySourceAuthorization("source-token").setEnableElevatedRead(false))
                .stream()
                .count());
    }
}
