// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.rest.PagedResponse;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.models.BlobLayoutInfo;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.BlobType;
import com.azure.storage.blob.models.LeaseStateType;
import com.azure.storage.blob.models.LeaseStatusType;
import com.azure.storage.blob.options.BlobGetLayoutOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlobClientBaseGetLayoutAsyncApiTests extends BlobTestBase {
    private BlobAsyncClient bc;

    @BeforeEach
    public void setup() {
        String blobName = generateBlobName();
        bc = ccAsync.getBlobAsyncClient(blobName);
        bc.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayout() {
        StepVerifier.create(bc.getLayoutWithResponse(null).collectList()).assertNext(r -> {
            assertFalse(r.isEmpty());
            BlobLayoutInfo info = r.get(0);
            assertNotNull(info.getETag());
            assertFalse(info.getETag().isEmpty());
            assertEquals(DATA.getDefaultDataSize(), info.getBlobContentLength());
            assertEquals(BlobType.BLOCK_BLOB, info.getBlobType());
            assertNotNull(info.getLastModified());
            assertNotNull(info.getCreatedOn());
            assertTrue(info.isServerEncrypted());
            assertEquals(LeaseStatusType.UNLOCKED, info.getLeaseStatus());
            assertEquals(LeaseStateType.AVAILABLE, info.getLeaseState());
        }).verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayoutEmptyBlob() {
        BlobAsyncClient emptyBlob = ccAsync.getBlobAsyncClient(generateBlobName());

        StepVerifier.create(emptyBlob.getBlockBlobAsyncClient()
            .commitBlockList(new ArrayList<>())
            .thenMany(emptyBlob.getLayoutWithResponse(null))
            .then()).verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayoutRange() {
        StepVerifier.create(bc.getBlockBlobAsyncClient()
            .upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), true)
            .thenMany(bc.getLayoutWithResponse(new BlobGetLayoutOptions().setRange(new BlobRange(0, (long) Constants.KB))))
            .then()).verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayoutPageSize() {
        StepVerifier.create(bc.getLayoutWithResponse(null).byPage(1).collectList()).assertNext(r -> {
            assertFalse(r.isEmpty());
            r.forEach(page -> assertTrue(page.getValue().size() <= 1));
        }).verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayoutContinuationToken() {
        Flux<PagedResponse<BlobLayoutInfo>> response
            = bc.getLayoutWithResponse(null).byPage(1).next().flatMapMany(r -> bc.getLayoutWithResponse(null).byPage(r.getContinuationToken()));

        StepVerifier.create(response.then()).verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsSupplier")
    public void getLayoutAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, String tags) {
        Map<String, String> t = new HashMap<>();
        t.put("foo", "bar");

        Flux<BlobLayoutInfo> response = bc.setTags(t)
            .then(Mono.zip(setupBlobLeaseCondition(bc, leaseID), setupBlobMatchCondition(bc, match),
                BlobTestBase::convertNulls))
            .flatMapMany(conditions -> {
                BlobRequestConditions bac = new BlobRequestConditions().setLeaseId(conditions.get(0))
                    .setIfMatch(conditions.get(1))
                    .setIfNoneMatch(noneMatch)
                    .setIfModifiedSince(modified)
                    .setIfUnmodifiedSince(unmodified)
                    .setTagsConditions(tags);

                return bc.getLayoutWithResponse(new BlobGetLayoutOptions().setRequestConditions(bac));
            });

        StepVerifier.create(response.then()).verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @ParameterizedTest
    @MethodSource("com.azure.storage.blob.BlobTestBase#allConditionsFailSupplier")
    public void getLayoutACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID, String tags) {
        Mono<Long> response
            = Mono
                .zip(setupBlobLeaseCondition(bc, leaseID), setupBlobMatchCondition(bc, noneMatch),
                    BlobTestBase::convertNulls)
                .flatMap(conditions -> {
                    BlobRequestConditions bac = new BlobRequestConditions().setLeaseId(conditions.get(0))
                        .setIfMatch(match)
                        .setIfNoneMatch(conditions.get(1))
                        .setIfModifiedSince(modified)
                        .setIfUnmodifiedSince(unmodified)
                        .setTagsConditions(tags);

                    return bc.getLayoutWithResponse(new BlobGetLayoutOptions().setRequestConditions(bac)).count();
                });

        StepVerifier.create(response).verifyError(BlobStorageException.class);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2027-03-07")
    @Test
    public void getLayoutError() {
        BlobAsyncClient blobClient = ccAsync.getBlobAsyncClient(generateBlobName());

        StepVerifier.create(blobClient.getLayoutWithResponse(null)).verifyError(BlobStorageException.class);
    }
}
