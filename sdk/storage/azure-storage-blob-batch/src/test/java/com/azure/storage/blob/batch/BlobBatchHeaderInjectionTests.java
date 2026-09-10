// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.storage.blob.models.BlobRequestConditions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests for Blob Batch inner-header CRLF injection.
 *
 * <p>Inner request header names and values are serialized into the multipart batch body. Carriage return / line feed
 * characters must be rejected on both the name and value so an attacker cannot terminate the intended header and inject
 * a second Azure Storage operation-control header (such as {@code x-ms-delete-snapshots}).</p>
 *
 * <p>The header-<em>value</em> boundary is reachable through the public
 * {@link BlobRequestConditions#setTagsConditions(String)} API. The header-<em>name</em> boundary is reachable by any
 * pipeline policy that calls {@code HttpRequest#setHeader} with an arbitrary name, so it is exercised directly against
 * the serializer.</p>
 */
public class BlobBatchHeaderInjectionTests extends BlobBatchTestBase {

    private static final String BLOB_URL = "https://account.blob.core.windows.net/victim/target";

    private BlobBatchClient batchClient;

    @Override
    public void beforeTest() {
        super.beforeTest();
        batchClient = new BlobBatchClientBuilder(primaryBlobServiceAsyncClient).buildClient();
    }

    private static String serializeBody(BlobBatch batch) {
        // prepareBlobBatchSubmission builds the batch body without sending a network request, so this exercises the
        // serialization path (including inner-header validation) offline.
        return serializeBody(batch.prepareBlobBatchSubmission().block());
    }

    private static String serializeBody(BlobBatchOperationInfo info) {
        StringBuilder sb = new StringBuilder();
        Collection<ByteBuffer> body = info.getBody();
        for (ByteBuffer buffer : body) {
            byte[] bytes = new byte[buffer.remaining()];
            buffer.duplicate().get(bytes);
            sb.append(new String(bytes, StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    private static int countInjectedDeleteHeaders(String body) {
        int count = 0;
        for (String line : body.split("\r\n")) {
            if ("x-ms-delete-snapshots: include".equalsIgnoreCase(line)) {
                count++;
            }
        }
        return count;
    }

    // --- Header value boundary (reachable via the public setTagsConditions API) ---

    @ParameterizedTest
    @ValueSource(
        strings = {
            "\"owner\" = 'attacker'\r\nx-ms-delete-snapshots: include", // CRLF
            "\"owner\" = 'attacker'\rx-ms-delete-snapshots: include",   // lone CR
            "\"owner\" = 'attacker'\nx-ms-delete-snapshots: include"    // lone LF
        })
    public void lineBreakInTagsConditionIsRejected(String maliciousCondition) {
        BlobBatch batch = batchClient.getBlobBatch();
        // deleteOptions is intentionally null - the application did NOT authorize snapshot deletion.
        batch.deleteBlob("victim", "target", null, new BlobRequestConditions().setTagsConditions(maliciousCondition));

        // The line-break-bearing value must be rejected before it is serialized into the batch body.
        assertThrows(IllegalArgumentException.class, () -> batch.prepareBlobBatchSubmission().block());
    }

    @Test
    public void cleanTagsConditionIsPreserved() {
        BlobBatch batch = batchClient.getBlobBatch();
        batch.deleteBlob("victim", "target", null,
            new BlobRequestConditions().setTagsConditions("\"owner\" = 'owner'"));

        String body = serializeBody(batch);

        assertEquals(0, countInjectedDeleteHeaders(body));
        assertTrue(body.contains("x-ms-if-tags: \"owner\" = 'owner'"),
            "Clean tag condition header should be preserved intact");
    }

    // --- Header name boundary (reachable by any policy that sets an arbitrary header name) ---

    @ParameterizedTest
    @ValueSource(
        strings = {
            "x-ms-inject\r\nx-ms-delete-snapshots", // CRLF
            "x-ms-inject\rx-ms-delete-snapshots",   // lone CR
            "x-ms-inject\nx-ms-delete-snapshots"    // lone LF
        })
    public void lineBreakInHeaderNameIsRejected(String maliciousHeaderName) {
        HttpRequest request = new HttpRequest(HttpMethod.DELETE, BLOB_URL);
        request.setHeader(HttpHeaderName.fromString(maliciousHeaderName), "include");

        BlobBatchOperationInfo info = new BlobBatchOperationInfo();
        assertThrows(IllegalArgumentException.class,
            () -> info.addBatchOperation(new BlobBatchOperationResponse<Void>(202), request));
    }

    @Test
    public void cleanHeaderNameIsPreserved() {
        HttpRequest request = new HttpRequest(HttpMethod.DELETE, BLOB_URL);
        request.setHeader(HttpHeaderName.fromString("x-ms-clean-header"), "clean-value");

        BlobBatchOperationInfo info = new BlobBatchOperationInfo();
        info.addBatchOperation(new BlobBatchOperationResponse<Void>(202), request);

        String body = serializeBody(info);

        assertEquals(0, countInjectedDeleteHeaders(body));
        assertTrue(body.contains("x-ms-clean-header: clean-value"),
            "Clean header name and value should be preserved intact");
    }
}
