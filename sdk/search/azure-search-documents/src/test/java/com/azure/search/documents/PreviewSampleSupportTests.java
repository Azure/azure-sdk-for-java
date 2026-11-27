// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpMethod;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PreviewSampleSupportTests {
    @Test
    public void failedCreateNeverRegistersPreexistingResourceForCleanup() {
        List<HttpMethod> methods = new ArrayList<>();
        SearchIndexClient client = new SearchIndexClientBuilder().endpoint("https://example.search.windows.net")
            .credential(new AzureKeyCredential("key"))
            .httpClient(request -> {
                methods.add(request.getHttpMethod());
                return Mono.just(new MockHttpResponse(request, 409,
                    "{\"error\":{\"code\":\"AlreadyExists\",\"message\":\"existing\"}}"
                        .getBytes(StandardCharsets.UTF_8)));
            })
            .buildClient();
        assertThrows(HttpResponseException.class, () -> {
            try (PreviewSampleSupport.Resources resources = new PreviewSampleSupport.Resources(client)) {
                resources.createIndex(PreviewSampleSupport.textIndex("existing"));
            }
        });
        assertEquals(Arrays.asList(HttpMethod.POST), methods);
    }

    @Test
    public void cleanupRunsInReverseOrderAndKeepsOriginalFailure() {
        List<String> deleted = new ArrayList<>();
        SearchIndexClient client = new SearchIndexClientBuilder().endpoint("https://example.search.windows.net")
            .credential(new AzureKeyCredential("key"))
            .httpClient(request -> {
                if (request.getHttpMethod() == HttpMethod.DELETE) {
                    deleted.add(request.getUrl().getPath());
                    return Mono.just(new MockHttpResponse(request, 403,
                        "{\"error\":{\"code\":\"Forbidden\",\"message\":\"cleanup failure\"}}"
                            .getBytes(StandardCharsets.UTF_8)));
                }
                return Mono.just(new MockHttpResponse(request, 201, request.getBodyAsBinaryData().toBytes()));
            })
            .buildClient();
        IllegalStateException original = new IllegalStateException("sample failure");
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            try (PreviewSampleSupport.Resources resources = new PreviewSampleSupport.Resources(client)) {
                resources.createIndex(PreviewSampleSupport.textIndex("first"));
                resources.createIndex(PreviewSampleSupport.textIndex("second"));
                throw original;
            }
        });
        assertEquals(original, thrown);
        assertEquals(2, deleted.size());
        assertTrue(deleted.get(0).contains("second"));
        assertTrue(deleted.get(1).contains("first"));
        assertEquals(1, thrown.getSuppressed().length);
        assertEquals(1, thrown.getSuppressed()[0].getSuppressed().length);
    }
}
