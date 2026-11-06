// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.config;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class PaginationPagePolicy implements HttpPipelinePolicy {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private final List<Page> pages = new CopyOnWriteArrayList<>();

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        String query = context.getHttpRequest().getUrl().getQuery();
        boolean paginationRequest = "/kv".equals(context.getHttpRequest().getUrl().getPath())
            && query != null
            && URLDecoder.decode(query, StandardCharsets.UTF_8).contains("key=" + PaginationTestData.KEY_PREFIX + "*");

        return next.process().flatMap(response -> {
            if (!paginationRequest || response.getStatusCode() != 200) {
                return Mono.just(response);
            }
            // Buffer once so observation does not consume the SDK's response body.
            HttpResponse buffered = response.buffer();
            return buffered.getBodyAsByteArray().map(body -> {
                JsonNode items = MAPPER.readTree(body).get("items");
                if (items == null || !items.isArray()) {
                    throw new IllegalStateException("Pagination response must contain an items array.");
                }
                pages.add(new Page(items.size(), buffered.getHeaderValue(HttpHeaderName.LINK) != null));
                return buffered;
            });
        });
    }

    @Override
    public HttpPipelinePosition getPipelinePosition() {
        return HttpPipelinePosition.PER_CALL;
    }

    List<Page> getPages() {
        return List.copyOf(pages);
    }

    record Page(int size, boolean hasContinuation) {
    }
}
