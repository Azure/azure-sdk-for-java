// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.storage.common.StorageSharedKeyCredential;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

final class RenameDestinationTestHelper {
    static final String DESTINATION = "dest dir/dest file.txt";
    private static final HttpHeaderName RENAME_SOURCE = HttpHeaderName.fromString("x-ms-file-rename-source");

    private final List<HttpRequest> requests = new ArrayList<>();

    ShareFileClientBuilder createBuilder() {
        HttpClient httpClient = request -> {
            requests.add(request);
            HttpMethod method = request.getHttpMethod();
            if (method == HttpMethod.HEAD) {
                return Mono.just(new MockHttpResponse(request, 200, FileIdTestHelper.fileHeaders("file.txt")));
            } else if (method == HttpMethod.GET) {
                return Mono
                    .just(new MockHttpResponse(request, 200, FileIdTestHelper.fileHeaders("file.txt"), new byte[1024]));
            } else if (method == HttpMethod.DELETE) {
                return Mono.just(new MockHttpResponse(request, 202, new HttpHeaders()));
            }
            int statusCode = request.getHeaders().getValue(RENAME_SOURCE) != null ? 200 : 201;
            return Mono.just(new MockHttpResponse(request, statusCode, new HttpHeaders()));
        };
        return new ShareFileClientBuilder().endpoint(FileIdTestHelper.ENDPOINT)
            .shareName(FileIdTestHelper.SHARE_NAME)
            .credential(new StorageSharedKeyCredential("account", "a2V5"))
            .httpClient(httpClient);
    }

    // The URL a client built directly for the destination path sends; the renamed client must match it.
    String expectedFileDeleteUrl() {
        createBuilder().resourcePath(DESTINATION).buildFileClient().delete();
        return lastRequestUrl();
    }

    String expectedDirectoryDeleteUrl() {
        createBuilder().resourcePath(DESTINATION).buildDirectoryClient().delete();
        return lastRequestUrl();
    }

    String lastRequestUrl() {
        return requests.get(requests.size() - 1).getUrl().toString();
    }

    int requestCount() {
        return requests.size();
    }

    List<String> requestLines(int from) {
        List<String> lines = new ArrayList<>();
        for (HttpRequest request : requests.subList(from, requests.size())) {
            String query = request.getUrl().getQuery();
            lines.add(request.getHttpMethod() + " " + request.getUrl().getPath() + (query == null ? "" : "?" + query));
        }
        return lines;
    }
}
