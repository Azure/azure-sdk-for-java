// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.rest.Response;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.AppendBlobItem;
import com.azure.storage.blob.models.BlobCopyInfo;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.models.PageBlobItem;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.BlobAsyncClientBase;
import com.azure.storage.common.sas.AccountSasPermission;
import com.azure.storage.common.sas.AccountSasResourceType;
import com.azure.storage.common.sas.AccountSasService;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuple4;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VersioningAsyncTests extends BlobTestBase {

    private BlobContainerAsyncClient blobContainerClient;
    private BlobAsyncClient blobClient;
    private final String contentV1 = "contentV1";
    private final String contentV2 = "contentV2";

    @BeforeEach
    public void setup() {
        String blobName = generateBlobName();
        String containerName = generateContainerName();
        blobContainerClient = versionedBlobServiceAsyncClient.createBlobContainer(containerName).block();
        blobClient = blobContainerClient.getBlobAsyncClient(blobName);
    }

    @AfterEach
    public void cleanup() {
        blobContainerClient.delete().block();
    }

    @Test
    public void createBlockBlobWithVersion() {
        Mono<Tuple2<BlockBlobItem, BlockBlobItem>> response = blobClient.getBlockBlobAsyncClient()
            .upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize())
            .flatMap(r -> Mono.zip(Mono.just(r), blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(),
                DATA.getDefaultDataSize(), true)));

        StepVerifier.create(response)
            .assertNext(r -> {
                assertNotNull(r.getT1().getVersionId());
                assertNotNull(r.getT2().getVersionId());
                assertFalse(StringUtils.equals(r.getT1().getVersionId(), r.getT2().getVersionId()));
            })
            .verifyComplete();
    }

    @Test
    public void createPageBlobWithVersion() {
        Mono<Tuple2<PageBlobItem, PageBlobItem>> response = blobClient.getPageBlobAsyncClient().create(512)
            .flatMap(r -> Mono.zip(Mono.just(r), blobClient.getPageBlobAsyncClient().create(512, true)));

        StepVerifier.create(response)
            .assertNext(r -> {
                assertNotNull(r.getT1().getVersionId());
                assertNotNull(r.getT2().getVersionId());
                assertFalse(StringUtils.equals(r.getT1().getVersionId(), r.getT2().getVersionId()));
            })
            .verifyComplete();
    }

    @Test
    public void createAppendBlobWithVersion() {
        Mono<Tuple2<AppendBlobItem, AppendBlobItem>> response = blobClient.getAppendBlobAsyncClient().create()
            .flatMap(r -> Mono.zip(Mono.just(r), blobClient.getAppendBlobAsyncClient().create(true)));

        StepVerifier.create(response)
            .assertNext(r -> {
                assertNotNull(r.getT1().getVersionId());
                assertNotNull(r.getT2().getVersionId());
                assertFalse(StringUtils.equals(r.getT1().getVersionId(), r.getT2().getVersionId()));
            })
            .verifyComplete();
    }

    @Test
    public void downloadBlobByVersion() {
        Flux<ByteBuffer> inputV1 = Flux.just(ByteBuffer.wrap(contentV1.getBytes(StandardCharsets.UTF_8)));
        Flux<ByteBuffer> inputV2 = Flux.just(ByteBuffer.wrap(contentV2.getBytes(StandardCharsets.UTF_8)));

        Mono<byte[]> response1 = blobClient.getBlockBlobAsyncClient().upload(inputV1, contentV1.length())
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(blobClient
                .getVersionClient(r.getVersionId()).download()));

        Mono<byte[]> response2 = blobClient.getBlockBlobAsyncClient().upload(inputV2, contentV2.length(), true)
                .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(blobClient
                    .getVersionClient(r.getVersionId()).download()));

        StepVerifier.create(response1)
            .assertNext(r -> TestUtils.assertArraysEqual(r, contentV1.getBytes(StandardCharsets.UTF_8)))
            .verifyComplete();

        StepVerifier.create(response2)
            .assertNext(r -> TestUtils.assertArraysEqual(r, contentV2.getBytes(StandardCharsets.UTF_8)))
            .verifyComplete();
    }

    @Test
    public void downloadBlobByVersionStreaming() {
        Flux<ByteBuffer> inputV1 = Flux.just(ByteBuffer.wrap(contentV1.getBytes(StandardCharsets.UTF_8)));
        Flux<ByteBuffer> inputV2 = Flux.just(ByteBuffer.wrap(contentV2.getBytes(StandardCharsets.UTF_8)));

        Mono<byte[]> response1 = blobClient.getBlockBlobAsyncClient().upload(inputV1, contentV1.length())
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(blobClient
                .getVersionClient(r.getVersionId()).downloadStream()));

        Mono<byte[]> response2 = blobClient.getBlockBlobAsyncClient().upload(inputV2, contentV2.length(), true)
            .flatMap(r -> FluxUtil.collectBytesInByteBufferStream(blobClient
                .getVersionClient(r.getVersionId()).downloadStream()));

        StepVerifier.create(response1)
            .assertNext(r -> TestUtils.assertArraysEqual(r, contentV1.getBytes(StandardCharsets.UTF_8)))
            .verifyComplete();

        StepVerifier.create(response2)
            .assertNext(r -> TestUtils.assertArraysEqual(r, contentV2.getBytes(StandardCharsets.UTF_8)))
            .verifyComplete();
    }

    @Test
    public void downloadBlobByVersionBinaryData() {
        Flux<ByteBuffer> inputV1 = Flux.just(ByteBuffer.wrap(contentV1.getBytes(StandardCharsets.UTF_8)));
        Flux<ByteBuffer> inputV2 = Flux.just(ByteBuffer.wrap(contentV2.getBytes(StandardCharsets.UTF_8)));

        Mono<BinaryData> response1 = blobClient.getBlockBlobAsyncClient().upload(inputV1, contentV1.length())
            .flatMap(r -> blobClient.getVersionClient(r.getVersionId()).downloadContent());

        Mono<BinaryData> response2 = blobClient.getBlockBlobAsyncClient().upload(inputV2, contentV2.length(), true)
            .flatMap(r -> blobClient.getVersionClient(r.getVersionId()).downloadContent());

        StepVerifier.create(response1)
            .assertNext(r -> assertEquals(r.toString(), contentV1))
            .verifyComplete();

        StepVerifier.create(response2)
            .assertNext(r -> assertEquals(r.toString(), contentV2))
            .verifyComplete();
    }

    @Test
    public void deleteBlobByVersion() {
        Flux<ByteBuffer> inputV1 = Flux.just(ByteBuffer.wrap(contentV1.getBytes(StandardCharsets.UTF_8)));
        Flux<ByteBuffer> inputV2 = Flux.just(ByteBuffer.wrap(contentV2.getBytes(StandardCharsets.UTF_8)));

        Mono<Tuple2<Boolean, Boolean>> response = blobClient.getBlockBlobAsyncClient().upload(inputV1, contentV1.length())
            .flatMap(r -> Mono.zip(Mono.just(r), blobClient.getBlockBlobAsyncClient().upload(inputV2, contentV2.length(), true)))
            .flatMap(tuple -> blobClient.getVersionClient(tuple.getT1().getVersionId()).delete()
                .then(Mono.zip(Mono.just(tuple.getT1()), Mono.just(tuple.getT2()))))
            .flatMap(r -> Mono.zip(blobClient.getVersionClient(r.getT1().getVersionId()).exists(),
                blobClient.getVersionClient(r.getT2().getVersionId()).exists()));

       StepVerifier.create(response)
           .assertNext(r -> {
               assertFalse(r.getT1());
               assertTrue(r.getT2());
           })
           .verifyComplete();
    }

    @Test
    public void deleteBlobByVersionUsingSASToken() {
        Flux<ByteBuffer> inputV1 = Flux.just(ByteBuffer.wrap(contentV1.getBytes(StandardCharsets.UTF_8)));
        Flux<ByteBuffer> inputV2 = Flux.just(ByteBuffer.wrap(contentV2.getBytes(StandardCharsets.UTF_8)));

        Mono<Tuple2<Boolean, Boolean>> response = blobClient.getBlockBlobAsyncClient().upload(inputV1, contentV1.length())
            .flatMap(r -> Mono.zip(Mono.just(r), blobClient.getBlockBlobAsyncClient().upload(inputV2, contentV2.length(), true)))
            .flatMap(r -> {
                BlobSasPermission permission = new BlobSasPermission()
                    .setDeleteVersionPermission(true);
                String sasToken = blobClient.getVersionClient(r.getT1().getVersionId())
                    .generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1), permission));

                BlobAsyncClient sasClient = getBlobAsyncClient(blobClient.getVersionClient(r.getT1().getVersionId()).getBlobUrl(),
                    sasToken);
                return sasClient.delete().then(Mono.zip(Mono.just(r.getT1()), Mono.just(r.getT2())));
            }).flatMap(r -> Mono.zip(blobClient.getVersionClient(r.getT1().getVersionId()).exists(), blobClient.getVersionClient(r.getT2().getVersionId()).exists()));

        StepVerifier.create(response)
            .assertNext(r -> {
                assertFalse(r.getT1());
                assertTrue(r.getT2());
            })
            .verifyComplete();
    }

    @Test
    public void deleteBlobByVersionUsingContainerSASToken() {
        Flux<ByteBuffer> inputV1 = Flux.just(ByteBuffer.wrap(contentV1.getBytes(StandardCharsets.UTF_8)));
        Flux<ByteBuffer> inputV2 = Flux.just(ByteBuffer.wrap(contentV2.getBytes(StandardCharsets.UTF_8)));

        Mono<Tuple2<Boolean, Boolean>> response = blobClient.getBlockBlobAsyncClient().upload(inputV1, contentV1.length())
            .flatMap(r -> Mono.zip(Mono.just(r), blobClient.getBlockBlobAsyncClient().upload(inputV2, contentV2.length(), true)))
            .flatMap(r -> {
                BlobSasPermission permission = new BlobSasPermission()
                    .setDeleteVersionPermission(true);
                String sasToken = blobContainerClient.generateSas(
                    new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1), permission));

                BlobAsyncClient sasClient = getBlobAsyncClient(blobClient.getVersionClient(r.getT1().getVersionId()).getBlobUrl(),
                    sasToken);
                return sasClient.delete().then(Mono.zip(Mono.just(r.getT1()), Mono.just(r.getT2())));
            }).flatMap(r -> Mono.zip(blobClient.getVersionClient(r.getT1().getVersionId()).exists(), blobClient.getVersionClient(r.getT2().getVersionId()).exists()));

        StepVerifier.create(response)
            .assertNext(r -> {
                assertFalse(r.getT1());
                assertTrue(r.getT2());
            })
            .verifyComplete();
    }

    @Test
    public void deleteBlobByVersionUsingAccountSASToken() {
        Flux<ByteBuffer> inputV1 = Flux.just(ByteBuffer.wrap(contentV1.getBytes(StandardCharsets.UTF_8)));
        Flux<ByteBuffer> inputV2 = Flux.just(ByteBuffer.wrap(contentV2.getBytes(StandardCharsets.UTF_8)));

        Mono<Tuple2<Boolean, Boolean>> response = blobClient.getBlockBlobAsyncClient().upload(inputV1, contentV1.length())
            .flatMap(r -> Mono.zip(Mono.just(r), blobClient.getBlockBlobAsyncClient().upload(inputV2, contentV2.length(), true)))
            .flatMap(r -> {
                AccountSasPermission permission = new AccountSasPermission()
                    .setDeleteVersionPermission(true);
                String sasToken = versionedBlobServiceAsyncClient.generateAccountSas(new AccountSasSignatureValues(
                    testResourceNamer.now().plusDays(1), permission, new AccountSasService().setBlobAccess(true),
                    new AccountSasResourceType().setObject(true)));

                BlobAsyncClient sasClient = getBlobAsyncClient(blobClient.getVersionClient(r.getT1().getVersionId()).getBlobUrl(),
                    sasToken);
                return sasClient.delete().then(Mono.zip(Mono.just(r.getT1()), Mono.just(r.getT2())));
            }).flatMap(r -> Mono.zip(blobClient.getVersionClient(r.getT1().getVersionId()).exists(), blobClient.getVersionClient(r.getT2().getVersionId()).exists()));

        StepVerifier.create(response)
            .assertNext(r -> {
                assertFalse(r.getT1());
                assertTrue(r.getT2());
            })
            .verifyComplete();
    }

    @Test
    public void getBlobPropertiesByVersion() {
        String key = "key";
        String valV2 = "val2";
        String valV3 = "val3";

        Mono<Tuple3<BlobProperties, BlobProperties, BlobProperties>> response = blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(),
            DATA.getDefaultDataSize())
            .flatMap(r -> Mono.zip(Mono.just(r), blobClient.getBlockBlobAsyncClient()
                .setMetadataWithResponse(Collections.singletonMap(key, valV2), null), blobClient.getBlockBlobAsyncClient()
                .setMetadataWithResponse(Collections.singletonMap(key, valV3), null)))
            .flatMap(r -> {
                String versionId1 = r.getT1().getVersionId();
                String versionId2 = r.getT2().getHeaders().getValue(X_MS_VERSION_ID);
                String versionId3 = r.getT3().getHeaders().getValue(X_MS_VERSION_ID);
                return Mono.zip(blobClient.getVersionClient(versionId1).getProperties(),
                    blobClient.getVersionClient(versionId2).getProperties(),
                    blobClient.getVersionClient(versionId3).getProperties());
            });

        StepVerifier.create(response)
            .assertNext(r -> {
                assertNull(r.getT1().getMetadata().get(key));
                assertEquals(valV2, r.getT2().getMetadata().get(key));
                assertEquals(valV3, r.getT3().getMetadata().get(key));
            })
            .verifyComplete();
    }

    @Test
    public void listBlobsWithVersion() {
        Mono<Tuple4<BlockBlobItem, BlockBlobItem, BlockBlobItem, List<BlobItem>>> response =
            blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize())
            .flatMap(blobItemV1 ->
                blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), true)
                .flatMap(blobItemV2 ->
                    blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), true)
                    .flatMap(blobItemV3 ->
                        blobContainerClient.listBlobs(
                            new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveVersions(true)),
                            null)
                            .collectList()
                            .flatMap(list -> Mono.zip(Mono.just(blobItemV1), Mono.just(blobItemV2),
                            Mono.just(blobItemV3), Mono.just(list)))
                    )
                )
            );

        StepVerifier.create(response)
            .assertNext(tuple -> {
                BlockBlobItem blobItemV1 = tuple.getT1();
                BlockBlobItem blobItemV2 = tuple.getT2();
                BlockBlobItem blobItemV3 = tuple.getT3();
                List<BlobItem> blobItems = tuple.getT4();

                assertEquals(blobItemV1.getVersionId(), blobItems.get(0).getVersionId());
                assertNull(blobItems.get(0).isCurrentVersion());

                assertEquals(blobItemV2.getVersionId(), blobItems.get(1).getVersionId());
                assertNull(blobItems.get(1).isCurrentVersion());

                assertEquals(blobItemV3.getVersionId(), blobItems.get(2).getVersionId());
                assertTrue(blobItems.get(2).isCurrentVersion());
            })
            .verifyComplete();
    }

    @Test
    public void listBlobsWithoutVersion() {
        Flux<Tuple2<BlobItem, BlockBlobItem>> response = blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize())
            .then(blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), true))
            .then(blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), true))
            .flatMapMany(r -> Flux.zip(blobContainerClient.listBlobs(
                new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveVersions(false)), null), Flux.just(r)));

        StepVerifier.create(response)
            .assertNext(r -> assertEquals(r.getT2().getVersionId(), r.getT1().getVersionId()))
            .verifyComplete();
    }

    @Test
    public void beginCopyBlobsWithVersion() {
        BlobAsyncClient sourceBlob = blobContainerClient.getBlobAsyncClient(generateBlobName());

        Flux<Tuple2<AsyncPollResponse<BlobCopyInfo, Void>, BlockBlobItem>> response = sourceBlob.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize())
            .then(blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(),
                DATA.getDefaultDataSize()))
            .flatMapMany(r -> {
                PollerFlux<BlobCopyInfo, Void> poller = setPlaybackPollerFluxPollInterval(blobClient.beginCopy(
                    sourceBlob.getBlobUrl(), null));
                return Flux.zip(poller, Flux.just(r));
            });

        StepVerifier.create(response)
            .assertNext(r -> {
                assertNotNull(r.getT1().getValue().getVersionId());
                assertNotEquals(r.getT2().getVersionId(), r.getT1().getValue().getVersionId());
            })
            .verifyComplete();
    }

    @Test
    public void copyFromUrlBlobsWithVersion() {
        BlobAsyncClient sourceBlob = blobContainerClient.getBlobAsyncClient(generateBlobName());

        Mono<Tuple2<Response<String>, BlockBlobItem>> response = sourceBlob.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize())
            .then(blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(),
                DATA.getDefaultDataSize()))
            .flatMap(r -> {
                String sas = sourceBlob.generateSas(new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1),
                    new BlobSasPermission().setTagsPermission(true).setReadPermission(true)));
                return Mono.zip(blobClient.copyFromUrlWithResponse(sourceBlob.getBlobUrl() + "?" + sas,
                    null, null, null, null), Mono.just(r));
            });

        StepVerifier.create(response)
            .assertNext(r -> {
                assertNotNull(r.getT1().getHeaders().getValue(X_MS_VERSION_ID));
                assertNotEquals(r.getT2().getVersionId(), r.getT1().getHeaders().getValue(X_MS_VERSION_ID));
            })
            .verifyComplete();
    }

    @Test
    public void setTierWithVersion() {
        Flux<ByteBuffer> inputV1 = Flux.just(ByteBuffer.wrap(contentV1.getBytes(StandardCharsets.UTF_8)));
        Flux<ByteBuffer> inputV2 = Flux.just(ByteBuffer.wrap(contentV2.getBytes(StandardCharsets.UTF_8)));

        Mono<Tuple2<BlobProperties, BlobProperties>> response = blobClient.getBlockBlobAsyncClient().upload(inputV1, contentV1.length())
            .flatMap(r -> Mono.zip(Mono.just(r), blobClient.getBlockBlobAsyncClient().upload(inputV2, contentV2.length(), true)))
            .flatMap(r -> blobClient.getVersionClient(r.getT1().getVersionId()).setAccessTier(AccessTier.COOL)
                .then(Mono.zip(blobClient.getVersionClient(r.getT1().getVersionId()).getProperties(), blobClient.getProperties())));

        StepVerifier.create(response)
            .assertNext(r -> {
                assertEquals(AccessTier.COOL, r.getT1().getAccessTier());
                assertNotEquals(AccessTier.COOL, r.getT2().getAccessTier());
            })
            .verifyComplete();
    }

    @Test
    public void setTierWithVersionError() {
        Flux<ByteBuffer> inputV1 = Flux.just(ByteBuffer.wrap(contentV1.getBytes(StandardCharsets.UTF_8)));
        blobClient.getBlockBlobAsyncClient().upload(inputV1, contentV1.length());
        String fakeVersion = "2020-04-17T20:37:16.5129130Z";

        StepVerifier.create(blobClient.getVersionClient(fakeVersion).setAccessTier(AccessTier.COOL))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void blobPropertiesShouldContainVersionInformation() {
        Mono<Tuple4<BlobProperties, BlobProperties, BlockBlobItem, BlockBlobItem>> response = blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(),
            DATA.getDefaultDataSize())
            .flatMap(r -> Mono.zip(Mono.just(r), blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(),
                DATA.getDefaultDataSize(), true)))
            .flatMap(tuple -> Mono.zip(blobClient.getVersionClient(tuple.getT1().getVersionId()).getProperties(),
                blobClient.getVersionClient(tuple.getT2().getVersionId()).getProperties(), Mono.just(tuple.getT1()), Mono.just(tuple.getT2())));

        StepVerifier.create(response)
            .assertNext(r -> {
                assertEquals(r.getT1().getVersionId(), r.getT3().getVersionId());
                assertNull(r.getT1().isCurrentVersion());
                assertEquals(r.getT2().getVersionId(), r.getT4().getVersionId());
                assertTrue(r.getT2().isCurrentVersion());
            })
            .verifyComplete();
    }

    @Test
    public void doNotLookForSnapshotOfVersion() {
        StepVerifier.create(blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();

        assertThrows(IllegalArgumentException.class, () -> blobClient.getVersionClient("a").getSnapshotClient("b"));
    }

    @Test
    public void doNotLookForVersionOfSnapshot() {
        StepVerifier.create(blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()))
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();

        assertThrows(IllegalArgumentException.class, () -> blobClient.getSnapshotClient("a").getVersionClient("b"));
    }

    @Test
    public void snapshotCreatesNewVersion() {
        Mono<Tuple2<BlockBlobItem, Response<BlobAsyncClientBase>>> response = blobClient.getBlockBlobAsyncClient()
            .upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize())
            .flatMap(r -> Mono.zip(Mono.just(r), blobClient.createSnapshotWithResponse(null, null)));

        StepVerifier.create(response)
            .assertNext(r -> {
                assertNotNull(r.getT2().getHeaders().getValue(X_MS_VERSION_ID));
                assertNotEquals(r.getT1().getVersionId(), r.getT2().getHeaders().getValue(X_MS_VERSION_ID));
            })
            .verifyComplete();
    }

    @Test
    public void versionedBlobURLContainsVersion() {
        Mono<Tuple2<String, BlockBlobItem>> response = blobClient.getBlockBlobAsyncClient().upload(DATA.getDefaultFlux(),
            DATA.getDefaultDataSize())
            .flatMap(r -> Mono.zip(Mono.just(blobClient.getVersionClient(r.getVersionId()).getBlobUrl()), Mono.just(r)));

        StepVerifier.create(response)
            .assertNext(r -> assertTrue(r.getT1().contains(r.getT2().getVersionId())))
            .verifyComplete();
    }
}
