// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.models.BlobLayout;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.options.BlobGetLayoutOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlobClientBaseTests extends BlobTestBase {
    private BlobClient bc;

    @BeforeEach
    public void setup() {
        String blobName = generateBlobName();
        bc = cc.getBlobClient(blobName);
        bc.getBlockBlobClient().upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayout() {
        Iterator<BlobLayout> iterator = bc.getLayout(null, Context.NONE).iterator();

        assertTrue(iterator.hasNext());
        BlobLayout layout = iterator.next();
        assertNotNull(layout.getRanges());
        assertFalse(layout.getRanges().isEmpty());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayoutEmptyBlob() {
        BlobClient emptyBlob = cc.getBlobClient(generateBlobName());
        emptyBlob.getBlockBlobClient().commitBlockList(new ArrayList<>());

        assertDoesNotThrow(() -> emptyBlob.getLayout(null, Context.NONE).stream().count());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayoutRange() {
        bc.getBlockBlobClient().upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize(), true);

        assertDoesNotThrow(
            () -> bc.getLayout(new BlobGetLayoutOptions().setRange(new BlobRange(0, (long) Constants.KB)), Context.NONE)
                .stream()
                .count());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayoutPageSize() {
        Iterator<PagedResponse<BlobLayout>> iterator = bc.getLayout(null, Context.NONE).iterableByPage(1).iterator();
        int pageCount = 0;

        while (iterator.hasNext()) {
            PagedResponse<BlobLayout> page = iterator.next();
            assertTrue(page.getValue().size() <= 1);
            pageCount++;
        }

        assertTrue(pageCount > 0);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayoutContinuationToken() {
        Iterator<PagedResponse<BlobLayout>> iterator = bc.getLayout(null, Context.NONE).iterableByPage(1).iterator();
        String token = iterator.next().getContinuationToken();

        assertDoesNotThrow(() -> bc.getLayout(null, Context.NONE).iterableByPage(token).iterator().hasNext());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void getLayoutAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");
        bc.setTags(t);
        match = setupBlobMatchCondition(bc, match);
        leaseID = setupBlobLeaseCondition(bc, leaseID);
        BlobRequestConditions bac = new BlobRequestConditions().setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        assertDoesNotThrow(
            () -> bc.getLayout(new BlobGetLayoutOptions().setRequestConditions(bac), Context.NONE).stream().count());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void getLayoutACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, String tags) {
        BlobRequestConditions bac = new BlobRequestConditions().setLeaseId(setupBlobLeaseCondition(bc, leaseID))
            .setIfMatch(match)
            .setIfNoneMatch(setupBlobMatchCondition(bc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setTagsConditions(tags);

        assertThrows(BlobStorageException.class,
            () -> bc.getLayout(new BlobGetLayoutOptions().setRequestConditions(bac), Context.NONE).stream().count());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayoutError() {
        BlobClient blobClient = cc.getBlobClient(generateBlobName());

        assertThrows(BlobStorageException.class, () -> blobClient.getLayout(null, Context.NONE).stream().count());
    }
}
