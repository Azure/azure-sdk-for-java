// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.storage.blob.models.BlobRequestConditions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class EncryptedBlobClientTests {
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
}
