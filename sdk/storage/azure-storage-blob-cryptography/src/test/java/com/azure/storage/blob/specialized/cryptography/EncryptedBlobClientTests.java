// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.storage.blob.models.BlobRequestConditions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class EncryptedBlobClientTests {
    private static final String ENDPOINT = "https://account.blob.core.windows.net";
    private static final String ETAG = "0x8DABC";

    @Test
    void applyETagLockCreatesConditionsAndQuotesETag() {
        BlobRequestConditions conditions = EncryptedBlobClient.applyETagLock(null, "0x8DABC");

        assertEquals("\"0x8DABC\"", conditions.getIfMatch());
    }

    @Test
    void applyETagLockReusesConditionsAndPreservesOtherValues() {
        BlobRequestConditions conditions = new BlobRequestConditions().setLeaseId("lease-id");

        BlobRequestConditions result = EncryptedBlobClient.applyETagLock(conditions, "\"0x8DABC\"");

        assertSame(conditions, result);
        assertEquals("\"0x8DABC\"", result.getIfMatch());
        assertEquals("lease-id", result.getLeaseId());
    }

    @Test
    void syncDownloadSendsQuotedETagLock() {
        ETagCapturingHttpClient httpClient = new ETagCapturingHttpClient();
        EncryptedBlobClient client = createBuilder(httpClient).buildEncryptedBlobClient();

        client.downloadContent();

        assertEquals("\"" + ETAG + "\"", httpClient.downloadIfMatch.get());
    }

    @Test
    void asyncDownloadSendsQuotedETagLockWhenConditionsAreOmitted() {
        ETagCapturingHttpClient httpClient = new ETagCapturingHttpClient();
        EncryptedBlobAsyncClient client = createBuilder(httpClient).buildEncryptedBlobAsyncClient();

        client.downloadContent().block();

        assertEquals("\"" + ETAG + "\"", httpClient.downloadIfMatch.get());
    }

    private static EncryptedBlobClientBuilder createBuilder(HttpClient httpClient) {
        return new EncryptedBlobClientBuilder().endpoint(ENDPOINT)
            .containerName("container")
            .blobName("blob")
            .key(new FakeKey("keyId", new byte[256]), "keyWrapAlgorithm")
            .httpClient(httpClient);
    }

    private static final class ETagCapturingHttpClient implements HttpClient {
        private final AtomicReference<String> downloadIfMatch = new AtomicReference<>();

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.ETAG, "\"" + ETAG + "\"")
                .set(HttpHeaderName.CONTENT_LENGTH, "0")
                .set(HttpHeaderName.fromString("x-ms-blob-type"), "BlockBlob");

            if (request.getHttpMethod() == HttpMethod.GET) {
                downloadIfMatch.set(request.getHeaders().getValue(HttpHeaderName.IF_MATCH));
            }

            return Mono.just(new MockHttpResponse(request, 200, headers, new byte[0]));
        }
    }
}
