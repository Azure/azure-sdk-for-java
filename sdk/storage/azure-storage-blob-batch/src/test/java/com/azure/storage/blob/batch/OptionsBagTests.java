// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch;

import com.azure.storage.blob.batch.options.BlobBatchSetBlobAccessTierOptions;
import com.azure.storage.blob.models.AccessTier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OptionsBagTests {
    @Test
    public void setAccessTierConstructor() {
        String url = "https://account.blob.core.windows.net/container/blob";
        String container = "container";
        String blob = "blob";

        BlobBatchSetBlobAccessTierOptions options1 = new BlobBatchSetBlobAccessTierOptions(url, AccessTier.ARCHIVE);
        BlobBatchSetBlobAccessTierOptions options2
            = new BlobBatchSetBlobAccessTierOptions(container, blob, AccessTier.ARCHIVE);

        assertEquals(url, options1.getBlobUrl());
        assertEquals(url, options2.getBlobUrl());
        assertEquals(blob, options1.getBlobName());
        assertEquals(blob, options2.getBlobName());
        assertEquals(container, options1.getBlobContainerName());
        assertEquals(container, options2.getBlobContainerName());
        assertEquals("container/blob", options1.getBlobIdentifier());
        assertEquals("container/blob", options2.getBlobIdentifier());
    }

    @Test
    public void setAccessTierSnapshot() {
        String url = "https://account.blob.core.windows.net/container/blob?snapshot=snapshot";
        String container = "container";
        String blob = "blob";
        String snapshot = "snapshot";

        BlobBatchSetBlobAccessTierOptions options1 = new BlobBatchSetBlobAccessTierOptions(url, AccessTier.ARCHIVE);
        BlobBatchSetBlobAccessTierOptions options2
            = new BlobBatchSetBlobAccessTierOptions(container, blob, AccessTier.ARCHIVE).setSnapshot(snapshot);

        assertEquals(url, options1.getBlobUrl());
        assertEquals(url, options2.getBlobUrl());
        assertEquals(blob, options1.getBlobName());
        assertEquals(blob, options2.getBlobName());
        assertEquals(container, options1.getBlobContainerName());
        assertEquals(container, options2.getBlobContainerName());
        assertEquals(snapshot, options1.getSnapshot());
        assertEquals(snapshot, options2.getSnapshot());
        assertEquals("container/blob?snapshot=snapshot", options1.getBlobIdentifier());
        assertEquals("container/blob?snapshot=snapshot", options2.getBlobIdentifier());
    }

    @Test
    public void setAccessTierVersion() {
        String url = "https://account.blob.core.windows.net/container/blob?versionid=version";
        String container = "container";
        String blob = "blob";
        String version = "version";

        BlobBatchSetBlobAccessTierOptions options1 = new BlobBatchSetBlobAccessTierOptions(url, AccessTier.ARCHIVE);
        BlobBatchSetBlobAccessTierOptions options2
            = new BlobBatchSetBlobAccessTierOptions(container, blob, AccessTier.ARCHIVE).setVersionId(version);

        assertEquals(url, options1.getBlobUrl());
        assertEquals(url, options2.getBlobUrl());
        assertEquals(blob, options1.getBlobName());
        assertEquals(blob, options2.getBlobName());
        assertEquals(container, options1.getBlobContainerName());
        assertEquals(container, options2.getBlobContainerName());
        assertEquals(version, options1.getVersionId());
        assertEquals(version, options2.getVersionId());
        assertEquals("container/blob?versionid=version", options1.getBlobIdentifier());
        assertEquals("container/blob?versionid=version", options2.getBlobIdentifier());
    }

    @Test
    public void setAccessTierVersionSnapshotError() {
        BlobBatchSetBlobAccessTierOptions options1 = new BlobBatchSetBlobAccessTierOptions(
            "https://account.blob.core.windows.net/container/blob?versionid=version&snapshot=snapshot",
            AccessTier.ARCHIVE);
        BlobBatchSetBlobAccessTierOptions options2
            = new BlobBatchSetBlobAccessTierOptions("container", "blob", AccessTier.ARCHIVE).setVersionId("version")
                .setSnapshot("snapshot");

        assertThrows(IllegalArgumentException.class, options1::getBlobIdentifier);
        assertThrows(IllegalArgumentException.class, options2::getBlobIdentifier);
    }
}
