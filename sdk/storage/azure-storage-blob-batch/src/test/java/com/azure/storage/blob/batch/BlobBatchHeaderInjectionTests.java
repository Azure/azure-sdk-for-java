// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch;

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
 * <p>A caller-controlled {@code x-ms-if-tags} condition supplied through the public
 * {@link BlobRequestConditions#setTagsConditions(String)} API is serialized into the multipart batch body. Carriage
 * return / line feed characters must be rejected so an attacker cannot terminate the intended header and inject a
 * second Azure Storage operation-control header (such as {@code x-ms-delete-snapshots}).</p>
 */
public class BlobBatchHeaderInjectionTests extends BlobBatchTestBase {

    private BlobBatchClient batchClient;

    @Override
    public void beforeTest() {
        super.beforeTest();
        batchClient = new BlobBatchClientBuilder(primaryBlobServiceAsyncClient).buildClient();
    }

    private static String serializeBody(BlobBatch batch) {
        // prepareBlobBatchSubmission builds the batch body without sending a network request, so this exercises the
        // serialization path (including inner-header validation) offline.
        BlobBatchOperationInfo info = batch.prepareBlobBatchSubmission().block();
        StringBuilder sb = new StringBuilder();
        Collection<ByteBuffer> body = info.getBody();
        for (ByteBuffer buffer : body) {
            byte[] bytes = new byte[buffer.remaining()];
            buffer.duplicate().get(bytes);
            sb.append(new String(bytes, StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

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

        long injectedDeleteHeaderCount = 0;
        for (String line : body.split("\r\n")) {
            if ("x-ms-delete-snapshots: include".equalsIgnoreCase(line)) {
                injectedDeleteHeaderCount++;
            }
        }

        assertEquals(0, injectedDeleteHeaderCount);
        assertTrue(body.contains("x-ms-if-tags: \"owner\" = 'owner'"),
            "Clean tag condition header should be preserved intact");
    }
}
