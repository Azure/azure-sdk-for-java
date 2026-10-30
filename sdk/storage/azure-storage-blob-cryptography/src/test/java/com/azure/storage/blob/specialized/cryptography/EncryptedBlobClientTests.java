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
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.json.JsonProviders;
import com.azure.json.JsonWriter;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
// TODO: Re-enable when getLayout is made public.
// import com.azure.storage.blob.models.BlobLayoutRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.options.BlobGetLayoutOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
// TODO: Re-enable when getLayout is made public.
// import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EncryptedBlobClientTests {
    private static final String ENDPOINT = "https://account.blob.core.windows.net";
    private static final String ETAG = "0x8DABC";
    private static final byte[] LAYOUT_BODY = ("<?xml version=\"1.0\" encoding=\"utf-8\"?>"
        + "<BlobLayout><Ranges><Range Start=\"0\" End=\"99\" EndpointIndex=\"0\" /></Ranges>"
        + "<Endpoints><Endpoint Index=\"0\" Value=\"https://host-a:443\" /></Endpoints></BlobLayout>")
            .getBytes(StandardCharsets.UTF_8);

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

    @Test
    void encryptedDownloadFromCopiedBlobClientStillDecrypts() throws IOException {
        byte[] plaintext = "encrypted download".getBytes(StandardCharsets.UTF_8);
        FakeKey key = new FakeKey("keyId", new byte[256]);
        EncryptedBlob encryptedBlob
            = Objects.requireNonNull(createBuilder(new NoOpHttpClient(), key).buildEncryptedBlobAsyncClient()
                .encryptBlob(Flux.just(ByteBuffer.wrap(plaintext)))
                .block());
        byte[] ciphertext = Objects
            .requireNonNull(FluxUtil.collectBytesInByteBufferStream(encryptedBlob.getCiphertextFlux()).block());

        BlobClient blobClient = new BlobClientBuilder().endpoint(ENDPOINT)
            .containerName("container")
            .blobName("blob")
            .httpClient(new EncryptedDownloadHttpClient(ciphertext, serialize(encryptedBlob.getEncryptionData())))
            .buildClient();
        EncryptedBlobClient encryptedBlobClient
            = new EncryptedBlobClientBuilder(EncryptionVersion.V2).blobClient(blobClient)
                .key(key, "keyWrapAlgorithm")
                .buildEncryptedBlobClient();

        assertArrayEquals(plaintext, encryptedBlobClient.downloadContent().toBytes());
    }

    // TODO: Delete when getLayout is made public; replaced by encryptedClientGetLayoutBypassesDecryption below.
    @Test
    void encryptedClientLayoutRequestBypassesDecryption() {
        EncryptedLayoutHttpClient httpClient = new EncryptedLayoutHttpClient();
        EncryptedBlobClient encryptedBlobClient = createBuilder(httpClient).buildEncryptedBlobClient();

        try (HttpResponse response = encryptedBlobClient.getHttpPipeline()
            .sendSync(new HttpRequest(HttpMethod.GET, encryptedBlobClient.getBlobUrl() + "?comp=layout"),
                Context.NONE)) {
            assertArrayEquals(LAYOUT_BODY, response.getBodyAsByteArray().block());
        }
        assertTrue(httpClient.requestUrl.get().contains("comp=layout"));
    }

    // TODO: Delete when getLayout is made public; replaced by encryptedAsyncClientGetLayoutBypassesDecryption below.
    @Test
    void encryptedAsyncClientLayoutRequestBypassesDecryption() {
        EncryptedLayoutHttpClient httpClient = new EncryptedLayoutHttpClient();
        EncryptedBlobAsyncClient encryptedBlobClient = createBuilder(httpClient).buildEncryptedBlobAsyncClient();

        StepVerifier
            .create(encryptedBlobClient.getHttpPipeline()
                .send(new HttpRequest(HttpMethod.GET, encryptedBlobClient.getBlobUrl() + "?comp=layout"))
                .flatMap(response -> response.getBodyAsByteArray().doFinally(signal -> response.close())))
            .assertNext(body -> assertArrayEquals(LAYOUT_BODY, body))
            .verifyComplete();

        assertTrue(httpClient.requestUrl.get().contains("comp=layout"));
    }

    // TODO: Delete when getLayout is made public.
    @ParameterizedTest
    @ValueSource(classes = { EncryptedBlobClient.class, EncryptedBlobAsyncClient.class })
    void encryptedClientsDoNotExposeGetLayout(Class<?> clientClass) {
        assertThrows(NoSuchMethodException.class, () -> clientClass.getMethod("getLayout", BlobGetLayoutOptions.class));
        assertThrows(NoSuchMethodException.class,
            () -> clientClass.getMethod("getLayout", BlobGetLayoutOptions.class, Context.class));
    }

    // TODO: Re-enable when getLayout is made public.
    // @Test
    // void encryptedClientGetLayoutBypassesDecryption() {
    //     EncryptedLayoutHttpClient httpClient = new EncryptedLayoutHttpClient();
    //     EncryptedBlobClient encryptedBlobClient = createBuilder(httpClient).buildEncryptedBlobClient();
    //
    //     BlobLayoutRange layoutRange
    //         = encryptedBlobClient.getLayout(null).stream().findFirst().orElseThrow(AssertionError::new);
    //
    //     assertEquals(URI.create("https://host-a:443"), layoutRange.getEndpoint());
    //     assertTrue(httpClient.requestUrl.get().contains("comp=layout"));
    // }

    // TODO: Re-enable when getLayout is made public.
    // @Test
    // void encryptedAsyncClientGetLayoutBypassesDecryption() {
    //     EncryptedLayoutHttpClient httpClient = new EncryptedLayoutHttpClient();
    //     EncryptedBlobAsyncClient encryptedBlobClient = createBuilder(httpClient).buildEncryptedBlobAsyncClient();
    //
    //     StepVerifier.create(encryptedBlobClient.getLayout(null).next())
    //         .assertNext(layoutRange -> assertEquals(URI.create("https://host-a:443"), layoutRange.getEndpoint()))
    //         .verifyComplete();
    //
    //     assertTrue(httpClient.requestUrl.get().contains("comp=layout"));
    // }

    private static EncryptedBlobClientBuilder createBuilder(HttpClient httpClient) {
        return createBuilder(httpClient, new FakeKey("keyId", new byte[256]));
    }

    private static EncryptedBlobClientBuilder createBuilder(HttpClient httpClient, FakeKey key) {
        return new EncryptedBlobClientBuilder(EncryptionVersion.V2).endpoint(ENDPOINT)
            .containerName("container")
            .blobName("blob")
            .key(key, "keyWrapAlgorithm")
            .httpClient(httpClient);
    }

    private static String serialize(EncryptionData encryptionData) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
            jsonWriter.writeJson(encryptionData).flush();
            return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        }
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

    private static final class EncryptedDownloadHttpClient implements HttpClient {
        private final byte[] ciphertext;
        private final String encryptionData;

        private EncryptedDownloadHttpClient(byte[] ciphertext, String encryptionData) {
            this.ciphertext = ciphertext;
            this.encryptionData = encryptionData;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.ETAG, "\"" + ETAG + "\"")
                .set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(ciphertext.length))
                .set(HttpHeaderName.fromString("x-ms-blob-type"), "BlockBlob")
                .set(CryptographyConstants.ENCRYPTION_METADATA_HEADER, encryptionData);
            byte[] body = request.getHttpMethod() == HttpMethod.GET ? ciphertext : new byte[0];
            return Mono.just(new MockHttpResponse(request, 200, headers, body));
        }
    }

    private static final class EncryptedLayoutHttpClient implements HttpClient {
        private final AtomicReference<String> requestUrl = new AtomicReference<>();

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            requestUrl.set(request.getUrl().toString());
            HttpHeaders headers
                = new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(LAYOUT_BODY.length))
                    .set(HttpHeaderName.CONTENT_TYPE, "application/xml")
                    .set(CryptographyConstants.ENCRYPTION_METADATA_HEADER, "encryption metadata");
            return Mono.just(new MockHttpResponse(request, 200, headers, LAYOUT_BODY));
        }
    }
}
