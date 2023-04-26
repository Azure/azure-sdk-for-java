// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch;

import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.batch.options.BlobBatchSetBlobAccessTierOptions;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.RehydratePriority;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.BlobClientBase;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import com.azure.storage.common.sas.AccountSasPermission;
import com.azure.storage.common.sas.AccountSasResourceType;
import com.azure.storage.common.sas.AccountSasService;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import com.azure.storage.common.sas.SasProtocol;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BatchApiTests extends BlobBatchTestBase {
    /*
     * Helper method for tests where some operations fail, but not all fail. This is needed as the underlying request
     * generation is non-deterministic in the ordering of request. This is fine when running against the live service
     * as these requests will be properly associated to the response by their `Content-ID` but this causes issues in
     * playback as we are using a static response that cannot handle changes in operation order.
     */
    static int assertExpectedOrException(Response<?> response, int expectedStatusCode) {
        try {
            assertEquals(expectedStatusCode, response.getStatusCode());
            return 1;
        } catch (Exception exception) {
            assertInstanceOf(BlobStorageException.class, exception);
            return 0;
        }
    }

    private BlobBatchClient batchClient;
    private BlobBatchAsyncClient batchAsyncClient;
    private BlobBatchClient oauthBatchClient;

    @Override
    public void beforeTest() {
        super.beforeTest();
        BlobBatchClientBuilder blobBatchClientBuilder = new BlobBatchClientBuilder(primaryBlobServiceAsyncClient);
        batchClient = blobBatchClientBuilder.buildClient();
        batchAsyncClient = blobBatchClientBuilder.buildAsyncClient();
        oauthBatchClient = new BlobBatchClientBuilder(getOAuthServiceClient()).buildClient();
    }

    @Test
    public void emptyBatch() {
        assertThrows(UnsupportedOperationException.class, () -> batchClient.submitBatch(batchClient.getBlobBatch()));
    }

    @Test
    public void mixedBatch() {
        BlobBatch batch1 = batchClient.getBlobBatch();
        batch1.deleteBlob("container", "blob");
        assertThrows(UnsupportedOperationException.class,
            () -> batch1.setBlobAccessTier("container", "blob2", AccessTier.HOT));

        BlobBatch batch2 = batchClient.getBlobBatch();
        batch2.setBlobAccessTier("container", "blob", AccessTier.HOT);
        assertThrows(UnsupportedOperationException.class, () -> batch2.deleteBlob("container", "blob2"));
    }

    @Test
    public void setTierAllSucceed() {
        String containerName = generateContainerName();
        String blobName1 = generateBlobName();
        String blobName2 = generateBlobName();
        BlobBatch batch = batchClient.getBlobBatch();
        BlobContainerClient containerClient = primaryBlobServiceClient.createBlobContainer(containerName);
        containerClient.getBlobClient(blobName1).getBlockBlobClient().upload(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize());
        containerClient.getBlobClient(blobName2).getBlockBlobClient().upload(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize());

        Response<Void> response1 = batch.setBlobAccessTier(containerName, blobName1, AccessTier.HOT);
        Response<Void> response2 = batch.setBlobAccessTier(containerName, blobName2, AccessTier.COOL);
        batchClient.submitBatch(batch);

        assertEquals(200, response1.getStatusCode());
        assertEquals(200, response2.getStatusCode());
    }

    @DisabledIf("olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("setTierRehydratePrioritySupplier")
    public void setTierRehydratePriority(RehydratePriority rehydratePriority) {
        String containerName = generateContainerName();
        String blobName1 = generateBlobName();
        BlobBatch batch = batchClient.getBlobBatch();
        BlobContainerClient containerClient = primaryBlobServiceClient.createBlobContainer(containerName);
        BlobClient blobClient1 = containerClient.getBlobClient(blobName1);
        blobClient1.getBlockBlobClient().upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        blobClient1.setAccessTier(AccessTier.ARCHIVE);

        Response<Void> response1 = batch.setBlobAccessTier(
            new BlobBatchSetBlobAccessTierOptions(blobClient1.getBlobUrl(), AccessTier.HOT)
                .setPriority(rehydratePriority));
        batchClient.submitBatch(batch);

        assertEquals(202, response1.getStatusCode());
        assertEquals(rehydratePriority, blobClient1.getProperties().getRehydratePriority());
    }

    private static Stream<RehydratePriority> setTierRehydratePrioritySupplier() {
        return Stream.of(RehydratePriority.STANDARD, RehydratePriority.HIGH);
    }

    @DisabledIf("olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("setTierAcSupplier")
    public void setTierAc(String leaseId, String tags) {
        BlobBatch batch = batchClient.getBlobBatch();
        BlobContainerClient containerClient = primaryBlobServiceClient.createBlobContainer(generateContainerName());
        BlobClient blobClient1 = containerClient.getBlobClient(generateBlobName());
        blobClient1.getBlockBlobClient().upload(DATA.getDefaultBinaryData());
        blobClient1.setTags(Collections.singletonMap("foo", "bar"));

        Response<Void> response1 = batch.setBlobAccessTier(new BlobBatchSetBlobAccessTierOptions(
            blobClient1.getBlobUrl(), AccessTier.HOT)
            .setLeaseId(setupBlobLeaseCondition(blobClient1, leaseId)).setTagsConditions(tags));
        batchClient.submitBatch(batch);

        assertEquals(200, response1.getStatusCode());
    }

    private static Stream<Arguments> setTierAcSupplier() {
        return Stream.of(
            Arguments.of(null, null),
            Arguments.of(RECEIVED_LEASE_ID, null),
            Arguments.of(null, "\"foo\" = 'bar'")
        );
    }

    @ParameterizedTest
    @MethodSource("setTierAcFailSupplier")
    public void setTierAcFail(String leaseId, String tags) {
        BlobBatch batch = batchClient.getBlobBatch();
        BlobContainerClient containerClient = primaryBlobServiceClient.createBlobContainer(generateContainerName());
        BlobClient blobClient1 = containerClient.getBlobClient(generateBlobName());
        blobClient1.getBlockBlobClient().upload(DATA.getDefaultBinaryData());

        batch.setBlobAccessTier(new BlobBatchSetBlobAccessTierOptions(blobClient1.getBlobUrl(), AccessTier.HOT)
            .setLeaseId(leaseId).setTagsConditions(tags));

        assertThrows(BlobBatchStorageException.class, () -> batchClient.submitBatch(batch));
    }


    // Ensures errors in the batch using BlobBatchAsyncClient are emitted as onError and are not thrown.
    @ParameterizedTest
    @MethodSource("setTierAcFailSupplier")
    public void setTierAcFailAsync(String leaseId, String tags) {
        BlobBatch batch = batchAsyncClient.getBlobBatch();
        BlobContainerClient containerClient = primaryBlobServiceClient.createBlobContainer(generateContainerName());
        BlobClient blobClient1 = containerClient.getBlobClient(generateBlobName());
        blobClient1.getBlockBlobClient().upload(DATA.getDefaultBinaryData());

        batch.setBlobAccessTier(new BlobBatchSetBlobAccessTierOptions(blobClient1.getBlobUrl(), AccessTier.HOT)
            .setLeaseId(leaseId).setTagsConditions(tags));

        StepVerifier.create(batchAsyncClient.submitBatch(batch))
            .expectError(BlobBatchStorageException.class)
            .verify(Duration.ofSeconds(30));
    }

    private static Stream<Arguments> setTierAcFailSupplier() {
        return Stream.of(Arguments.of(GARBAGE_LEASE_ID, null), Arguments.of(null, "\"notfoo\" = 'notbar'"));
    }

    @Test
    public void setTierSomeSucceedThrowOnAnyError() {
        String containerName = generateContainerName();
        String blobName1 = generateBlobName();
        BlobBatch batch = batchClient.getBlobBatch();
        BlobContainerClient containerClient = primaryBlobServiceClient.createBlobContainer(containerName);
        containerClient.getBlobClient(blobName1).getBlockBlobClient().upload(DATA.getDefaultBinaryData());

        Response<Void> response1 = batch.setBlobAccessTier(containerName, blobName1, AccessTier.HOT);
        Response<Void> response2 = batch.setBlobAccessTier(containerName, generateBlobName(), AccessTier.COOL);

        assertThrows(BlobBatchStorageException.class, () -> batchClient.submitBatch(batch));

        // In PLAYBACK check responses in an order invariant fashion.
        if (getTestMode() == TestMode.PLAYBACK) {
            assertEquals(1, assertExpectedOrException(response1, 200) + assertExpectedOrException(response2, 200));
        } else {
            assertEquals(200, response1.getStatusCode());
            assertThrows(BlobStorageException.class, response2::getStatusCode);
        }
    }

    @Test
    public void setTierSomeSucceedThrowOnAnyErrorAsync() {
        String containerName = generateContainerName();
        String blobName1 = generateBlobName();
        BlobBatch batch = batchAsyncClient.getBlobBatch();
        BlobContainerClient containerClient = primaryBlobServiceClient.createBlobContainer(containerName);
        containerClient.getBlobClient(blobName1).getBlockBlobClient().upload(DATA.getDefaultBinaryData());

        Response<Void> response1 = batch.setBlobAccessTier(containerName, blobName1, AccessTier.HOT);
        Response<Void> response2 = batch.setBlobAccessTier(containerName, generateBlobName(), AccessTier.COOL);

        StepVerifier.create(batchAsyncClient.submitBatch(batch))
            .expectError(BlobBatchStorageException.class)
            .verify(Duration.ofSeconds(30));

        // In PLAYBACK check responses in an order invariant fashion.
        if (getTestMode() == TestMode.PLAYBACK) {
            assertEquals(1, assertExpectedOrException(response1, 200) + assertExpectedOrException(response2, 200));
        } else {
            assertEquals(200, response1.getStatusCode());
            assertThrows(BlobStorageException.class, response2::getStatusCode);
        }
    }

    @Test
    public void setTierSomeSucceedDoNotThrowOnAnyError() {
        String containerName = generateContainerName();
        String blobName1 = generateBlobName();
        BlobBatch batch = batchClient.getBlobBatch();
        BlobContainerClient containerClient = primaryBlobServiceClient.createBlobContainer(containerName);
        containerClient.getBlobClient(blobName1).getBlockBlobClient().upload(DATA.getDefaultBinaryData());

        Response<Void> response1 = batch.setBlobAccessTier(containerName, blobName1, AccessTier.HOT);
        Response<Void> response2 = batch.setBlobAccessTier(containerName, generateBlobName(), AccessTier.COOL);
        batchClient.submitBatchWithResponse(batch, false, null, Context.NONE);

        // In PLAYBACK check responses in an order invariant fashion.
        if (getTestMode() == TestMode.PLAYBACK) {
            assertEquals(1, assertExpectedOrException(response1, 200) + assertExpectedOrException(response2, 200));
        } else {
            assertEquals(200, response1.getStatusCode());
            assertThrows(BlobStorageException.class, response2::getStatusCode);
        }
    }

    @Test
    public void setTierNoneSucceedThrowOnAnyError() {
        String containerName = generateContainerName();
        BlobBatch batch = batchClient.getBlobBatch();
        primaryBlobServiceClient.createBlobContainer(containerName);

        Response<Void> response1 = batch.setBlobAccessTier(containerName, generateBlobName(), AccessTier.HOT);
        Response<Void> response2 = batch.setBlobAccessTier(containerName, generateBlobName(), AccessTier.COOL);

        BlobBatchStorageException ex = assertThrows(BlobBatchStorageException.class,
            () -> batchClient.submitBatch(batch));
        assertEquals(2, getIterableSize(ex.getBatchExceptions()));
        assertThrows(BlobStorageException.class, response1::getStatusCode);
        assertThrows(BlobStorageException.class, response2::getStatusCode);
    }

    @Test
    public void setTierNoneSucceedDoNotThrowOnAnyError() {
        String containerName = generateContainerName();
        BlobBatch batch = batchClient.getBlobBatch();
        primaryBlobServiceClient.createBlobContainer(containerName);

        Response<Void> response1 = batch.setBlobAccessTier(containerName, generateBlobName(), AccessTier.HOT);
        Response<Void> response2 = batch.setBlobAccessTier(containerName, generateBlobName(), AccessTier.COOL);

        assertDoesNotThrow(() -> batchClient.submitBatchWithResponse(batch, false, null, Context.NONE));
        assertThrows(BlobStorageException.class, response1::getStatusCode);
        assertThrows(BlobStorageException.class, response2::getStatusCode);
    }

    @Test
    public void deleteBlobAllSucceed() {
        String containerName = generateContainerName();
        String blobName1 = generateBlobName();
        String blobName2 = generateBlobName();
        BlobBatch batch = batchClient.getBlobBatch();
        BlobContainerClient containerClient = primaryBlobServiceClient.createBlobContainer(containerName);
        containerClient.getBlobClient(blobName1).getPageBlobClient().create(0);
        containerClient.getBlobClient(blobName2).getPageBlobClient().create(0);

        Response<Void> response1 = batch.deleteBlob(containerName, blobName1);
        Response<Void> response2 = batch.deleteBlob(containerName, blobName2);
        batchClient.submitBatch(batch);

        assertEquals(202, response1.getStatusCode());
        assertEquals(202, response2.getStatusCode());
    }

    @Test
    public void deleteBlobSomeSucceedThrowOnAnyError() {
        String containerName = generateContainerName();
        String blobName1 = generateBlobName();
        BlobBatch batch = batchClient.getBlobBatch();
        BlobContainerClient containerClient = primaryBlobServiceClient.createBlobContainer(containerName);
        containerClient.getBlobClient(blobName1).getPageBlobClient().create(0);

        Response<Void> response1 = batch.deleteBlob(containerName, blobName1);
        Response<Void> response2 = batch.deleteBlob(containerName, generateBlobName());

        assertThrows(BlobBatchStorageException.class, () -> batchClient.submitBatch(batch));

        // In PLAYBACK check responses in an order invariant fashion.
        if (getTestMode() == TestMode.PLAYBACK) {
            assertEquals(1, assertExpectedOrException(response1, 202) + assertExpectedOrException(response2, 202));
        } else {
            assertEquals(202, response1.getStatusCode());
            assertThrows(BlobStorageException.class, response2::getStatusCode);
        }
    }

    @Test
    public void deleteBlobSomeSucceedDoNotThrowOnAnyError() {
        String containerName = generateContainerName();
        String blobName1 = generateBlobName();
        BlobBatch batch = batchClient.getBlobBatch();
        BlobContainerClient containerClient = primaryBlobServiceClient.createBlobContainer(containerName);
        containerClient.getBlobClient(blobName1).getPageBlobClient().create(0);

        Response<Void> response1 = batch.deleteBlob(containerName, blobName1);
        Response<Void> response2 = batch.deleteBlob(containerName, generateBlobName());
        batchClient.submitBatchWithResponse(batch, false, null, Context.NONE);

        // In PLAYBACK check responses in an order invariant fashion.
        if (getTestMode() == TestMode.PLAYBACK) {
            assertEquals(1, assertExpectedOrException(response1, 202) + assertExpectedOrException(response2, 202));
        } else {
            assertEquals(202, response1.getStatusCode());
            assertThrows(BlobStorageException.class, response2::getStatusCode);
        }
    }

    @Test
    public void deleteBlobNoneSucceedThrowOnAnyError() {
        String containerName = generateContainerName();
        BlobBatch batch = batchClient.getBlobBatch();
        primaryBlobServiceClient.createBlobContainer(containerName);

        Response<Void> response1 = batch.deleteBlob(containerName, generateBlobName());
        Response<Void> response2 = batch.deleteBlob(containerName, generateBlobName());

        BlobBatchStorageException ex = assertThrows(BlobBatchStorageException.class,
            () -> batchClient.submitBatch(batch));
        assertEquals(2, getIterableSize(ex.getBatchExceptions()));
        assertThrows(BlobStorageException.class, response1::getStatusCode);
        assertThrows(BlobStorageException.class, response2::getStatusCode);
    }

    @Test
    public void deleteBlobNoneSucceedDoNotThrowOnAnyError() {
        String containerName = generateContainerName();
        BlobBatch batch = batchClient.getBlobBatch();
        primaryBlobServiceClient.createBlobContainer(containerName);

        Response<Void> response1 = batch.deleteBlob(containerName, generateBlobName());
        Response<Void> response2 = batch.deleteBlob(containerName, generateBlobName());

        assertDoesNotThrow(() -> batchClient.submitBatchWithResponse(batch, false, null, Context.NONE));
        assertThrows(BlobStorageException.class, response1::getStatusCode);
        assertThrows(BlobStorageException.class, response2::getStatusCode);
    }

    @Test
    public void accessingBatchRequestBeforeSubmissionThrows() {
        BlobBatch batch = batchClient.getBlobBatch();
        Response<Void> batchRequest = batch.deleteBlob("blob", "container");

        assertThrows(UnsupportedOperationException.class, batchRequest::getStatusCode);
    }

    @Test
    public void bulkDeleteBlobs() {
        BlobContainerClient containerClient = primaryBlobServiceClient.createBlobContainer(generateContainerName());
        List<String> blobUrls = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            PageBlobClient pageBlobClient = containerClient.getBlobClient(generateBlobName()).getPageBlobClient();
            pageBlobClient.create(512);
            blobUrls.add(pageBlobClient.getBlobUrl());
        }

        List<Response<Void>> responseList = batchClient.deleteBlobs(blobUrls, DeleteSnapshotsOptionType.INCLUDE)
            .stream().collect(Collectors.toList());

        assertEquals(10, responseList.size());
        for (Response<Void> response : responseList) {
            assertEquals(202, response.getStatusCode());
        }
    }

    @Test
    public void bulkSetAccessTier() {
        BlobContainerClient containerClient = primaryBlobServiceClient.createBlobContainer(generateContainerName());
        List<String> blobUrls = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            BlockBlobClient blockBlobClient = containerClient.getBlobClient(generateBlobName()).getBlockBlobClient();
            blockBlobClient.upload(DATA.getDefaultBinaryData());
            blobUrls.add(blockBlobClient.getBlobUrl());
        }

        List<Response<Void>> responseList = batchClient.setBlobsAccessTier(blobUrls, AccessTier.HOT).stream()
            .collect(Collectors.toList());

        assertEquals(10, responseList.size());
        for (Response<Void> response : responseList) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void bulkSetAccessTierSnapshot() {
        BlobContainerClient containerClient = primaryBlobServiceClient.createBlobContainer(generateContainerName());
        BlockBlobClient blobClient = containerClient.getBlobClient(generateBlobName()).getBlockBlobClient();
        blobClient.upload(DATA.getDefaultBinaryData());
        BlobClientBase snapClient = blobClient.createSnapshot();

        List<String> blobUrls = Collections.singletonList(snapClient.getBlobUrl());
        List<Response<Void>> responseList = batchClient.setBlobsAccessTier(blobUrls, AccessTier.HOT).stream()
            .collect(Collectors.toList());

        assertEquals(1, responseList.size());
        assertEquals(200, responseList.get(0).getStatusCode());
    }

    @Test
    public void bulkSetAccessTierVersion() {
        batchClient = new BlobBatchClientBuilder(versionedBlobServiceClient).buildClient();
        BlobContainerClient containerClient = versionedBlobServiceClient.createBlobContainer(generateContainerName());
        BlobClient blobClient = containerClient.getBlobClient(generateBlobName());
        ByteArrayInputStream inputV1 = new ByteArrayInputStream("contentV1".getBytes(StandardCharsets.UTF_8));
        ByteArrayInputStream inputV2 = new ByteArrayInputStream("contentV2".getBytes(StandardCharsets.UTF_8));
        BlockBlobItem blobItemV1 = blobClient.getBlockBlobClient().upload(inputV1, inputV1.available());
        blobClient.getBlockBlobClient().upload(inputV2, inputV2.available(), true);

        List<String> blobUrls = Collections.singletonList(blobClient.getVersionClient(blobItemV1.getVersionId())
            .getBlobUrl());
        List<Response<Void>> responseList = batchClient.setBlobsAccessTier(blobUrls, AccessTier.HOT).stream()
            .collect(Collectors.toList());

        assertEquals(1, responseList.size());
        assertEquals(200, responseList.get(0).getStatusCode());
    }

    @Test
    public void tooManyOperationsFails() {
        BlobContainerClient containerClient = primaryBlobServiceClient.createBlobContainer(generateContainerName());
        List<String> blobUrls = new ArrayList<>();
        for (int i = 0; i < 257; i++) {
            PageBlobClient pageBlobClient = containerClient.getBlobClient(generateBlobName()).getPageBlobClient();
            blobUrls.add(pageBlobClient.getBlobUrl());
        }

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> batchClient.deleteBlobs(blobUrls, DeleteSnapshotsOptionType.INCLUDE).iterator().next());

        assertTrue(ex instanceof BlobStorageException || ex.getCause() instanceof BlobStorageException);
    }

    @Test
    public void singleOperationExceptionThrowsBlobBatchStorageException() {
        String containerName = generateContainerName();
        BlobBatch batch = batchClient.getBlobBatch();
        primaryBlobServiceClient.createBlobContainer(containerName);

        Response<Void> response1 = batch.deleteBlob(containerName, generateBlobName());

        assertThrows(BlobBatchStorageException.class, () -> batchClient.submitBatch(batch));
        assertThrows(BlobStorageException.class, response1::getStatusCode);
    }

    @RepeatedTest(value = 20)
    public void submittingSameBatchManyTimes() {
        String containerName = generateContainerName();
        String blobName1 = generateBlobName();
        String blobName2 = generateBlobName();
        BlobContainerClient containerClient = primaryBlobServiceClient.createBlobContainer(containerName);
        containerClient.getBlobClient(blobName2).getPageBlobClient().create(0);

        BlobBatch batch = batchClient.getBlobBatch();
        batch.deleteBlob(containerName, blobName1, DeleteSnapshotsOptionType.INCLUDE, null);
        batch.deleteBlob(containerName, blobName2, DeleteSnapshotsOptionType.INCLUDE, null);

        assertThrows(BlobBatchStorageException.class, () -> batchClient.submitBatch(batch));
        assertThrows(UnsupportedOperationException.class, () -> batchClient.submitBatch(batch));
    }

    @Test
    public void submitBatchWithOAuthCredentials() {
        String containerName = generateContainerName();
        String blobName1 = generateBlobName();
        String blobName2 = generateBlobName();
        BlobBatch batch = oauthBatchClient.getBlobBatch();
        BlobContainerClient containerClient = primaryBlobServiceClient.createBlobContainer(containerName);
        containerClient.getBlobClient(blobName1).getPageBlobClient().create(0);
        containerClient.getBlobClient(blobName2).getPageBlobClient().create(0);

        Response<Void> response1 = batch.deleteBlob(containerName, blobName1);
        Response<Void> response2 = batch.deleteBlob(containerName, blobName2);
        oauthBatchClient.submitBatch(batch);

        assertEquals(202, response1.getStatusCode());
        assertEquals(202, response2.getStatusCode());
    }

    @Test
    public void submitBatchWithAccountSasCredentials() {
        String containerName = generateContainerName();
        String blobName1 = generateBlobName();
        String blobName2 = generateBlobName();

        BlobContainerClient containerClient = primaryBlobServiceClient.createBlobContainer(containerName);
        containerClient.getBlobClient(blobName1).getPageBlobClient().create(0);
        containerClient.getBlobClient(blobName2).getPageBlobClient().create(0);

        AccountSasService service = new AccountSasService().setBlobAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true);
        AccountSasPermission permissions = new AccountSasPermission()
            .setReadPermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true);
        AccountSasSignatureValues sasValues = new AccountSasSignatureValues(testResourceNamer.now().plusDays(1),
            permissions, service, resourceType);

        BlobBatchClient batchClient = new BlobBatchClientBuilder(getServiceClient(
            primaryBlobServiceClient.generateAccountSas(sasValues), primaryBlobServiceClient.getAccountUrl()))
            .buildClient();

        BlobBatch batch = batchClient.getBlobBatch();
        Response<Void> response1 = batch.deleteBlob(containerName, blobName1);
        Response<Void> response2 = batch.deleteBlob(containerName, blobName2);
        batchClient.submitBatch(batch);

        assertEquals(202, response1.getStatusCode());
        assertEquals(202, response2.getStatusCode());
    }

    @Test
    public void submitBatchWithAccountSasCredentialsError() {
        String containerName = generateContainerName();
        String blobName1 = generateBlobName();
        String blobName2 = generateBlobName();

        BlobContainerClient containerClient = primaryBlobServiceClient.createBlobContainer(containerName);
        containerClient.getBlobClient(blobName1).getPageBlobClient().create(0);
        containerClient.getBlobClient(blobName2).getPageBlobClient().create(0);

        AccountSasService service = new AccountSasService().setBlobAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true);
        AccountSasPermission permissions = new AccountSasPermission() // No delete permission
            .setReadPermission(true)
            .setCreatePermission(true);
        AccountSasSignatureValues sasValues = new AccountSasSignatureValues(testResourceNamer.now().plusDays(1),
            permissions, service, resourceType);

        BlobBatchClient batchClient = new BlobBatchClientBuilder(getServiceClient(
            primaryBlobServiceClient.generateAccountSas(sasValues), primaryBlobServiceClient.getAccountUrl()))
            .buildClient();

        BlobBatch batch = batchClient.getBlobBatch();
        batch.deleteBlob(containerName, blobName1);
        batch.deleteBlob(containerName, blobName2);

        BlobBatchStorageException ex = assertThrows(BlobBatchStorageException.class,
            () -> batchClient.submitBatch(batch));
        assertEquals(2, getIterableSize(ex.getBatchExceptions()));
    }

    @DisabledIf("olderThan20200612ServiceVersion")
    // Container scoped batch
    @Test
    public void setTierAllSucceedContainerScoped() {
        String containerName = generateContainerName();
        String blobName1 = generateBlobName();
        String blobName2 = generateBlobName();
        BlobContainerClient containerClient = primaryBlobServiceClient.createBlobContainer(containerName);
        containerClient.getBlobClient(blobName1).getBlockBlobClient().upload(DATA.getDefaultBinaryData());
        containerClient.getBlobClient(blobName2).getBlockBlobClient().upload(DATA.getDefaultBinaryData());

        BlobBatchClient batchClient = new BlobBatchClientBuilder(containerClient).buildClient();
        BlobBatch batch = batchClient.getBlobBatch();
        Response<Void> response1 = batch.setBlobAccessTier(containerName, blobName1, AccessTier.HOT);
        Response<Void> response2 = batch.setBlobAccessTier(containerName, blobName2, AccessTier.COOL);
        batchClient.submitBatch(batch);

        assertEquals(200, response1.getStatusCode());
        assertEquals(200, response2.getStatusCode());
    }

    @DisabledIf("olderThan20200612ServiceVersion")
    @Test
    public void deleteBlobAllSucceedContainerScoped() {
        String containerName = generateContainerName();
        String blobName1 = generateBlobName();
        String blobName2 = generateBlobName();
        BlobContainerClient containerClient = primaryBlobServiceClient.createBlobContainer(containerName);
        containerClient.getBlobClient(blobName1).getPageBlobClient().create(0);
        containerClient.getBlobClient(blobName2).getPageBlobClient().create(0);

        BlobBatchClient batchClient = new BlobBatchClientBuilder(containerClient).buildClient();
        BlobBatch batch = batchClient.getBlobBatch();
        Response<Void> response1 = batch.deleteBlob(containerName, blobName1);
        Response<Void> response2 = batch.deleteBlob(containerName, blobName2);
        batchClient.submitBatch(batch);

        assertEquals(202, response1.getStatusCode());
        assertEquals(202, response2.getStatusCode());
    }

    @DisabledIf("olderThan20200612ServiceVersion")
    @Test
    public void bulkDeleteBlobsContainerScoped() {
        String containerName = generateContainerName();
        BlobContainerClient containerClient = primaryBlobServiceClient.createBlobContainer(containerName);
        List<String> blobUrls = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            PageBlobClient pageBlobClient = containerClient.getBlobClient(generateBlobName()).getPageBlobClient();
            pageBlobClient.create(512);
            blobUrls.add(pageBlobClient.getBlobUrl());
        }

        BlobBatchClient batchClient = new BlobBatchClientBuilder(containerClient).buildClient();

        for (Response<Void> response : batchClient.deleteBlobs(blobUrls, DeleteSnapshotsOptionType.INCLUDE)) {
            assertEquals(202, response.getStatusCode());
        }
    }

    @DisabledIf("olderThan20200612ServiceVersion")
    @Test
    public void bulkSetAccessTierContainerScoped() {
        String containerName = generateContainerName();
        BlobContainerClient containerClient = primaryBlobServiceClient.createBlobContainer(containerName);
        List<String> blobUrls = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            BlockBlobClient blockBlobClient = containerClient.getBlobClient(generateBlobName()).getBlockBlobClient();
            blockBlobClient.upload(DATA.getDefaultBinaryData());
            blobUrls.add(blockBlobClient.getBlobUrl());
        }

        BlobBatchClient batchClient = new BlobBatchClientBuilder(containerClient).buildClient();
        for (Response<Void> response : batchClient.setBlobsAccessTier(blobUrls, AccessTier.HOT)) {
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void setTierContainerScopedErrorWrongContainer() {
        String containerName = generateContainerName();
        String blobName1 = generateBlobName();
        String blobName2 = generateBlobName();
        BlobContainerClient containerClient = primaryBlobServiceClient.createBlobContainer(containerName);
        containerClient.getBlobClient(blobName1).getBlockBlobClient().upload(DATA.getDefaultBinaryData());
        containerClient.getBlobClient(blobName2).getBlockBlobClient().upload(DATA.getDefaultBinaryData());

        // Get a batch client associated with a different container.
        containerClient = primaryBlobServiceClient.createBlobContainer(generateContainerName());
        BlobBatchClient batchClient = new BlobBatchClientBuilder(containerClient).buildClient();
        BlobBatch batch = batchClient.getBlobBatch();

        batch.setBlobAccessTier(containerName, blobName1, AccessTier.HOT);
        batch.setBlobAccessTier(containerName, blobName2, AccessTier.COOL);

        assertThrows(BlobStorageException.class, () -> batchClient.submitBatch(batch));
    }

    @Test
    public void deleteBlobContainerScopedErrorWrongContainer() {
        String containerName = generateContainerName();
        String blobName1 = generateBlobName();
        String blobName2 = generateBlobName();
        BlobContainerClient containerClient = primaryBlobServiceClient.createBlobContainer(containerName);
        containerClient.getBlobClient(blobName1).getBlockBlobClient().upload(DATA.getDefaultBinaryData());
        containerClient.getBlobClient(blobName2).getBlockBlobClient().upload(DATA.getDefaultBinaryData());

        // Get a batch client associated with a different container.
        containerClient = primaryBlobServiceClient.createBlobContainer(generateContainerName());
        BlobBatchClient batchClient = new BlobBatchClientBuilder(containerClient).buildClient();
        BlobBatch batch = batchClient.getBlobBatch();

        batch.deleteBlob(containerName, blobName1);
        batch.deleteBlob(containerName, blobName2);

        assertThrows(BlobStorageException.class, () -> batchClient.submitBatch(batch));
    }

    @DisabledIf("olderThan20200612ServiceVersion")
    @Test
    public void submitBatchWithContainerSasCredentials() {
        String containerName = generateContainerName();
        String blobName1 = generateBlobName();
        String blobName2 = generateBlobName();

        BlobContainerClient containerClient = primaryBlobServiceClient.createBlobContainer(containerName);
        containerClient.getBlobClient(blobName1).getPageBlobClient().create(0);
        containerClient.getBlobClient(blobName2).getPageBlobClient().create(0);

        BlobContainerSasPermission permission = new BlobContainerSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
            .setListPermission(true)
            .setMovePermission(true)
            .setExecutePermission(true);
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            permission)
            .setStartTime(testResourceNamer.now().minusDays(1))
            .setProtocol(SasProtocol.HTTPS_HTTP)
            .setCacheControl("cache")
            .setContentDisposition("disposition")
            .setContentEncoding("encoding")
            .setContentLanguage("language")
            .setContentType("type");

        BlobBatchClient batchClient = new BlobBatchClientBuilder(
            getContainerClient(containerClient.generateSas(sasValues), containerClient.getBlobContainerUrl()))
            .buildClient();

        BlobBatch batch = batchClient.getBlobBatch();

        Response<Void> response1 = batch.deleteBlob(containerName, blobName1);
        Response<Void> response2 = batch.deleteBlob(containerName, blobName2);
        batchClient.submitBatch(batch);

        assertEquals(202, response1.getStatusCode());
        assertEquals(202, response2.getStatusCode());
    }

    @DisabledIf("olderThan20200612ServiceVersion")
    @Test
    public void submitBatchWithContainerSasCredentialsError() {
        String containerName = generateContainerName();
        String blobName1 = generateBlobName();
        String blobName2 = generateBlobName();

        BlobContainerClient containerClient = primaryBlobServiceClient.createBlobContainer(containerName);
        containerClient.getBlobClient(blobName1).getPageBlobClient().create(0);
        containerClient.getBlobClient(blobName2).getPageBlobClient().create(0);

        BlobContainerSasPermission permission = new BlobContainerSasPermission() // No delete permission
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true);
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
            permission)
            .setStartTime(testResourceNamer.now().minusDays(1))
            .setProtocol(SasProtocol.HTTPS_HTTP)
            .setCacheControl("cache")
            .setContentDisposition("disposition")
            .setContentEncoding("encoding")
            .setContentLanguage("language")
            .setContentType("type");

        BlobBatchClient batchClient = new BlobBatchClientBuilder(
            getContainerClient(containerClient.generateSas(sasValues), containerClient.getBlobContainerUrl()))
            .buildClient();

        BlobBatch batch = batchClient.getBlobBatch();
        batch.deleteBlob(containerName, blobName1);
        batch.deleteBlob(containerName, blobName2);

        BlobBatchStorageException ex = assertThrows(BlobBatchStorageException.class,
            () -> batchClient.submitBatch(batch));
        assertEquals(2, getIterableSize(ex.getBatchExceptions()));
    }

    private static boolean olderThan20191212ServiceVersion() {
        return olderThan(BlobServiceVersion.V2019_12_12);
    }

    private static boolean olderThan20200612ServiceVersion() {
        return olderThan(BlobServiceVersion.V2020_06_12);
    }

    private static boolean olderThan(BlobServiceVersion targetVersion) {
        String targetServiceVersionFromEnvironment = ENVIRONMENT.getServiceVersion();
        BlobServiceVersion version = (targetServiceVersionFromEnvironment != null)
            ? Enum.valueOf(BlobServiceVersion.class, targetServiceVersionFromEnvironment)
            : BlobServiceVersion.getLatest();

        return version.ordinal() < targetVersion.ordinal();
    }
}
