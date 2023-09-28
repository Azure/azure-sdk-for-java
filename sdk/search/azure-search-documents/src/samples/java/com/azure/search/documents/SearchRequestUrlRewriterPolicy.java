// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.UrlBuilder;
import reactor.core.publisher.Mono;

/**
 * This an example {@link HttpPipelinePolicy} that can rewrite request URLs to replace the OData URL syntax
 * ({@code /docs('key')}) with standard URL syntax ({@code /docs/key}).
 */
public final class SearchRequestUrlRewriterPolicy implements HttpPipelinePolicy {

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        context.setHttpRequest(rewriteUrl(context.getHttpRequest()));
        return next.process();
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        context.setHttpRequest(rewriteUrl(context.getHttpRequest()));
        return next.processSync();
    }

    private static HttpRequest rewriteUrl(HttpRequest request) {
        UrlBuilder urlBuilder = UrlBuilder.parse(request.getUrl());
        String path = urlBuilder.getPath();

        if (path.startsWith("/aliases('")) {
            urlBuilder.setPath(createNewPath(path, "/aliases/", 10));
        } else if (path.startsWith("/datasources('")) {
            urlBuilder.setPath(createNewPath(path, "/datasources/", 14));
        } else if (path.startsWith("/indexers('")) {
            urlBuilder.setPath(createNewPath(path, "/indexers/", 11));
        } else if (path.startsWith("/indexes('")) {
            // Indexes is special as it can be used for either the management-style APIs managing the index or with
            // document retrieval.
            //
            // So it needs to replace the OData URL syntax for the index name and also check if it contains the
            // document retrieval path.
            int documentRetrievalIndex = path.indexOf("/docs('");
            if (documentRetrievalIndex != -1) {
                int odataUrlClose = path.indexOf("')", 10);
                StringBuilder newPath = new StringBuilder(path.length())
                    .append("/indexes/")
                    .append(path, 10, odataUrlClose)
                    .append(path, odataUrlClose + 2, documentRetrievalIndex)
                    .append("/docs/");

                odataUrlClose = path.indexOf("')", documentRetrievalIndex + 7);
                newPath.append(path, documentRetrievalIndex + 7, odataUrlClose);

                if (odataUrlClose < path.length() - 2) {
                    newPath.append(path, odataUrlClose + 2, path.length());
                }

                urlBuilder.setPath(newPath.toString());
            } else {
                urlBuilder.setPath(createNewPath(path, "/indexes/", 10));
            }
        } else if (path.startsWith("/skillsets('")) {
            urlBuilder.setPath(createNewPath(path, "/skillsets/", 12));
        } else if (path.startsWith("/synonymmaps('")) {
            urlBuilder.setPath(createNewPath(path, "/synonymmaps/", 14));
        } else {
            return request;
        }

        return request.setUrl(urlBuilder.toString());
    }

    private static String createNewPath(String path, String pathSegment, int startIndex) {
        int odataUrlClose = path.indexOf("')", startIndex);
        StringBuilder newPath =
            new StringBuilder(path.length()).append(pathSegment).append(path, startIndex, odataUrlClose);

        if (odataUrlClose < path.length() - 2) {
            newPath.append(path, odataUrlClose + 2, path.length());
        }

        return newPath.toString();
    }
}
