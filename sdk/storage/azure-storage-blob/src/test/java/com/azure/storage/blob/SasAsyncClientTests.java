// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.storage.blob.implementation.util.BlobSasImplUtil;
import com.azure.storage.blob.models.BlobContainerProperties;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.TaggedBlobItem;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.AppendBlobAsyncClient;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder;
import com.azure.storage.common.implementation.AccountSasImplUtil;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.SasImplUtils;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.sas.AccountSasPermission;
import com.azure.storage.common.sas.AccountSasResourceType;
import com.azure.storage.common.sas.AccountSasService;
import com.azure.storage.common.sas.AccountSasSignatureValues;
import com.azure.storage.common.sas.CommonSasQueryParameters;
import com.azure.storage.common.sas.SasIpRange;
import com.azure.storage.common.sas.SasProtocol;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SasAsyncClientTests extends BlobTestBase {

    private BlockBlobAsyncClient sasClient;
    private String blobName;

    @BeforeEach
    public void setup() {
        blobName = generateBlobName();
        sasClient = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(), ccAsync.getBlobContainerUrl(), blobName)
            .getBlockBlobAsyncClient();
        sasClient.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();
    }

    @Test
    public void blobSasAllPermissionsSuccess() {
        // FE will reject a permission string it doesn't recognize
        BlobSasPermission allPermissions = new BlobSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
            .setListPermission(true);

        if (Constants.SAS_SERVICE_VERSION.compareTo("2019-12-12") >= 0) {
            allPermissions
                .setMovePermission(true)
                .setExecutePermission(true)
                .setDeleteVersionPermission(true)
                .setTagsPermission(true);
        }
        if (Constants.SAS_SERVICE_VERSION.compareTo("2020-02-10") >= 0) {
            allPermissions.setPermanentDeletePermission(true);
        }

        if (Constants.SAS_SERVICE_VERSION.compareTo("2020-06-12") >= 0) {
            allPermissions.setImmutabilityPolicyPermission(true);
        }

        BlobServiceSasSignatureValues sasValues = generateValues(allPermissions);

        String sas = sasClient.generateSas(sasValues);

        BlockBlobAsyncClient client = getBlobAsyncClient(sas, ccAsync.getBlobContainerUrl(), blobName, null)
            .getBlockBlobAsyncClient();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(client.downloadStream()))
            .assertNext(r -> assertArrayEquals(DATA.getDefaultBytes(), r))
            .verifyComplete();

        StepVerifier.create(client.getProperties())
            .assertNext(r -> assertTrue(validateSasProperties(r)))
            .verifyComplete();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void blobSasReadPermissions() {
        BlobSasPermission permissions = new BlobSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
            .setListPermission(true);
        if (Constants.SAS_SERVICE_VERSION.compareTo("2019-12-12") >= 0) {
            permissions.setMovePermission(true).setExecutePermission(true);
        }

        BlobServiceSasSignatureValues sasValues = generateValues(permissions);

        String sas = sasClient.generateSas(sasValues);

        BlockBlobAsyncClient client = getBlobAsyncClient(sas, ccAsync.getBlobContainerUrl(), blobName, null)
            .getBlockBlobAsyncClient();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(client.download()))
            .assertNext(r -> assertArrayEquals(DATA.getDefaultBytes(), r))
            .verifyComplete();

        StepVerifier.create(client.getProperties())
            .assertNext(r -> assertTrue(validateSasProperties(r)))
            .verifyComplete();
    }

    @Test
    public void canUseConnectionStringWithSasAndQuestionMark() {
        BlobSasPermission permissions = new BlobSasPermission().setReadPermission(true);

        BlobServiceSasSignatureValues sasValues = generateValues(permissions);

        String sas = sasClient.generateSas(sasValues);

        String connectionString = String.format("BlobEndpoint=%s;SharedAccessSignature=%s;",
            ENVIRONMENT.getPrimaryAccount().getBlobEndpoint(), "?" + sas);

        BlobAsyncClient client = instrument(new BlobClientBuilder())
            .connectionString(connectionString)
            .containerName(sasClient.getContainerName())
            .blobName(sasClient.getBlobName())
            .buildAsyncClient();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(client.downloadStream()))
            .assertNext(r -> assertArrayEquals(DATA.getDefaultBytes(), r))
            .verifyComplete();

        StepVerifier.create(client.getProperties())
            .assertNext(r -> assertTrue(validateSasProperties(r)))
            .verifyComplete();
    }

    // RBAC replication lag
    @Test
    public void blobSasUserDelegation() {
        liveTestScenarioWithRetry(() -> {
            BlobSasPermission permissions = new BlobSasPermission()
                .setReadPermission(true)
                .setWritePermission(true)
                .setCreatePermission(true)
                .setDeletePermission(true)
                .setAddPermission(true)
                .setListPermission(true);
            if (Constants.SAS_SERVICE_VERSION.compareTo("2019-12-12") >= 0) {
                permissions.setMovePermission(true).setExecutePermission(true);
            }

            BlobServiceSasSignatureValues sasValues = generateValues(permissions);

            Mono<Tuple2<byte[], BlobProperties>> response = getUserDelegationInfo().flatMap(r -> {
                String sas = sasClient.generateUserDelegationSas(sasValues, r);
                BlockBlobAsyncClient client = getBlobAsyncClient(sas, ccAsync.getBlobContainerUrl(), blobName, null)
                    .getBlockBlobAsyncClient();
                return Mono.zip(FluxUtil.collectBytesInByteBufferStream(client.downloadStream()), client.getProperties());
            });

            StepVerifier.create(response)
                .assertNext(r -> {
                    assertArrayEquals(DATA.getDefaultBytes(), r.getT1());
                    assertTrue(validateSasProperties(r.getT2()));
                })
                .verifyComplete();
        });
    }

    @SuppressWarnings("deprecation")
    @Test
    public void blobSasSnapshot() {
        BlobSasPermission permissions = new BlobSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true);
        BlobServiceSasSignatureValues sasValues = generateValues(permissions);

        Mono<Tuple2<String, String>> snapshotIDAndSasTuple = sasClient.createSnapshot()
            .map(r -> {
                BlockBlobAsyncClient snapshotBlob = new SpecializedBlobClientBuilder()
                    .blobAsyncClient(r).buildBlockBlobAsyncClient();
                String snapshotId = snapshotBlob.getSnapshotId();
                String sas = snapshotBlob.generateSas(sasValues);
                return Tuples.of(snapshotId, sas);
            });

        Flux<ByteBuffer> baseBlobWithSnapshotSasResponse = snapshotIDAndSasTuple.flatMapMany(tuple -> {
            AppendBlobAsyncClient client = getBlobAsyncClient(tuple.getT2(), ccAsync.getBlobContainerUrl(), blobName, null)
                .getAppendBlobAsyncClient();
            return client.download();
        });

        // snapshot-level SAS shouldn't be able to access base blob
        StepVerifier.create(baseBlobWithSnapshotSasResponse)
            .verifyError(BlobStorageException.class);

        Mono<Tuple2<byte[], BlobProperties>> blobSnapshotSasResponse = snapshotIDAndSasTuple.flatMap(tuple -> {
            AppendBlobAsyncClient snapClient = getBlobAsyncClient(tuple.getT2(), ccAsync.getBlobContainerUrl(), blobName, tuple.getT1())
                .getAppendBlobAsyncClient();
            return Mono.zip(FluxUtil.collectBytesInByteBufferStream(snapClient.downloadStream()), snapClient.getProperties());
        });

        StepVerifier.create(blobSnapshotSasResponse)
            .assertNext(r -> {
                assertArrayEquals(DATA.getDefaultBytes(), r.getT1());
                assertTrue(validateSasProperties(r.getT2()));
            })
            .verifyComplete();
    }

    // RBAC replication lag
    @SuppressWarnings("deprecation")
    @Test
    public void blobSasSnapshotUserDelegation() {
        liveTestScenarioWithRetry(() -> {
            BlobSasPermission permissions = new BlobSasPermission()
                .setReadPermission(true)
                .setWritePermission(true)
                .setCreatePermission(true)
                .setDeletePermission(true)
                .setAddPermission(true);
            BlobServiceSasSignatureValues sasValues = generateValues(permissions);

            Mono<Tuple2<String, String>> snapshotIDAndSasTuple = sasClient.createSnapshot()
                .flatMap(r -> {
                    BlockBlobAsyncClient snapshotBlob = new SpecializedBlobClientBuilder()
                        .blobAsyncClient(r).buildBlockBlobAsyncClient();
                    String snapshotId = snapshotBlob.getSnapshotId();
                    return Mono.zip(Mono.just(snapshotId), getUserDelegationInfo().flatMap(info -> Mono.just(snapshotBlob.generateUserDelegationSas(sasValues, info))));
                });

            Flux<ByteBuffer> baseBlobWithSnapshotSasResponse = snapshotIDAndSasTuple.flatMapMany(tuple -> {
                BlockBlobAsyncClient client = getBlobAsyncClient(tuple.getT2(), ccAsync.getBlobContainerUrl(), blobName, null)
                    .getBlockBlobAsyncClient();
                return client.download();
            });

            // snapshot-level SAS shouldn't be able to access base blob
            StepVerifier.create(baseBlobWithSnapshotSasResponse)
                .verifyError(BlobStorageException.class);

            Mono<Tuple2<byte[], BlobProperties>> blobSnapshotSasResponse = snapshotIDAndSasTuple.flatMap(tuple -> {
                BlockBlobAsyncClient snapClient = getBlobAsyncClient(tuple.getT2(), ccAsync.getBlobContainerUrl(), blobName, tuple.getT1())
                    .getBlockBlobAsyncClient();
                return Mono.zip(FluxUtil.collectBytesInByteBufferStream(snapClient.downloadStream()), snapClient.getProperties());
            });

            StepVerifier.create(blobSnapshotSasResponse)
                .assertNext(r -> {
                    assertArrayEquals(DATA.getDefaultBytes(), r.getT1());
                    assertTrue(validateSasProperties(r.getT2()));
                })
                .verifyComplete();
        });
    }

    // RBAC replication lag
    @Test
    public void containerSasUserDelegation() {
        liveTestScenarioWithRetry(() -> {
            BlobContainerSasPermission permissions = new BlobContainerSasPermission()
                .setReadPermission(true)
                .setWritePermission(true)
                .setCreatePermission(true)
                .setDeletePermission(true)
                .setAddPermission(true)
                .setListPermission(true);
            OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);

            BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions);

            Flux<BlobItem> response = getUserDelegationInfo().flatMapMany(r -> {
                String sasWithPermissions = ccAsync.generateUserDelegationSas(sasValues, r);
                BlobContainerAsyncClient client = getContainerAsyncClient(sasWithPermissions, ccAsync.getBlobContainerUrl());
                return client.listBlobs();
            });

            StepVerifier.create(response)
                .expectNextCount(1)
                .verifyComplete();
        });
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void blobSasTags() {
        BlobSasPermission permissions = new BlobSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
            .setTagsPermission(true);

        BlobServiceSasSignatureValues sasValues = generateValues(permissions);
        String sas = sasClient.generateSas(sasValues);
        BlobAsyncClient client = getBlobAsyncClient(sas, ccAsync.getBlobContainerUrl(), blobName, null);

        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");

        Mono<Map<String, String>> response = client.setTags(tags).then(client.getTags());
        StepVerifier.create(response)
            .assertNext(r -> assertEquals(tags, r))
            .verifyComplete();
    }

    @Test
    public void blobSasTagsFail() {
        BlobSasPermission permissions = new BlobSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true);
        /* No tags permission */

        BlobServiceSasSignatureValues sasValues = generateValues(permissions);
        String sas = sasClient.generateSas(sasValues);
        BlobAsyncClient client = getBlobAsyncClient(sas, ccAsync.getBlobContainerUrl(), blobName, null);

        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");
        StepVerifier.create(client.setTags(tags))
            .verifyError(BlobStorageException.class);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void containerSasTags() {
        BlobContainerSasPermission permissions = new BlobContainerSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
            .setListPermission(true)
            .setDeleteVersionPermission(true)
            .setTagsPermission(true);

        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions);
        String sas = ccAsync.generateSas(sasValues);
        BlobAsyncClient client = getBlobAsyncClient(sas, ccAsync.getBlobContainerUrl(), blobName, null);

        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");

        Mono<Map<String, String>> response = client.setTags(tags).then(client.getTags());
        StepVerifier.create(response)
            .assertNext(r -> assertEquals(tags, r))
            .verifyComplete();
    }

    @Test
    public void containerSasTagsFail() {
        BlobContainerSasPermission permissions = new BlobContainerSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true);
        /* No tags permission. */

        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions);
        String sas = sasClient.generateSas(sasValues);
        BlobAsyncClient client = getBlobAsyncClient(sas, ccAsync.getBlobContainerUrl(), blobName, null);

        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");
        StepVerifier.create(client.setTags(tags))
            .verifyError(BlobStorageException.class);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-04-10")
    @Test
    public void containerSasFilterBlobs() {
        BlobContainerSasPermission permissions = new BlobContainerSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
            .setListPermission(true)
            .setDeleteVersionPermission(true)
            .setTagsPermission(true)
            .setFilterPermission(true);

        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions);
        String sas = ccAsync.generateSas(sasValues);
        BlobAsyncClient client = getBlobAsyncClient(sas, ccAsync.getBlobContainerUrl(), blobName, null);

        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");

        Flux<TaggedBlobItem> response = client.setTags(tags).thenMany(ccAsync.findBlobsByTags("\"foo\"='bar'"));
        StepVerifier.create(response)
            .thenConsumeWhile(x -> true)
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-04-10")
    @Test
    public void containerSasFilterBlobsFail() {
        BlobContainerSasPermission permissions = new BlobContainerSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
            .setListPermission(true)
            .setDeleteVersionPermission(true);
        // no filter or tags permission

        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions);
        String sas = ccAsync.generateSas(sasValues);
        BlobAsyncClient client = getBlobAsyncClient(sas, ccAsync.getBlobContainerUrl(), blobName, null);

        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");

        StepVerifier.create(client.setTags(tags))
            .verifyError(BlobStorageException.class);
    }

    // RBAC replication lag
    @Test
    public void blobUserDelegationSaoid() {
        liveTestScenarioWithRetry(() -> {
            BlobSasPermission permissions = new BlobSasPermission()
                .setReadPermission(true);

            OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);
            String saoid = testResourceNamer.randomUuid();
            BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions)
                .setPreauthorizedAgentObjectId(saoid);

            Mono<BlobProperties> response = getOAuthServiceAsyncClient().getUserDelegationKey(null, expiryTime)
                .flatMap(r -> {
                    String keyOid = testResourceNamer.recordValueFromConfig(r.getSignedObjectId());
                    r.setSignedObjectId(keyOid);

                    String keyTid = testResourceNamer.recordValueFromConfig(r.getSignedTenantId());
                    r.setSignedTenantId(keyTid);

                    String sasWithPermissions = sasClient.generateUserDelegationSas(sasValues, r);
                    assertDoesNotThrow(() -> sasWithPermissions.contains("saoid=" + saoid));

                    BlobAsyncClient client = getBlobAsyncClient(sasWithPermissions, ccAsync.getBlobContainerUrl(), blobName,
                        null);
                    return client.getProperties();
                });

            StepVerifier.create(response)
                .expectNextCount(1)
                .verifyComplete();
        });
    }

    // RBAC replication lag
    @Test
    public void containerUserDelegationCorrelationId() {
        liveTestScenarioWithRetry(() -> {
            BlobContainerSasPermission permissions = new BlobContainerSasPermission().setListPermission(true);
            OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);

            String cid = testResourceNamer.randomUuid();
            BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions)
                .setCorrelationId(cid);

            Flux<BlobItem> response = getOAuthServiceAsyncClient().getUserDelegationKey(null, expiryTime)
                .flatMapMany(r -> {
                    String keyOid = testResourceNamer.recordValueFromConfig(r.getSignedObjectId());
                    r.setSignedObjectId(keyOid);

                    String keyTid = testResourceNamer.recordValueFromConfig(r.getSignedTenantId());
                    r.setSignedTenantId(keyTid);

                    String sasWithPermissions = ccAsync.generateUserDelegationSas(sasValues, r);
                    assertDoesNotThrow(() -> sasWithPermissions.contains("scid=" + cid));

                    BlobContainerAsyncClient client = getContainerAsyncClient(sasWithPermissions, ccAsync.getBlobContainerUrl());

                    return client.listBlobs();
                });

            StepVerifier.create(response)
                .expectNextCount(1)
                .verifyComplete();
        });
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-02-10")
    @Test
    public void containerUserDelegationCorrelationIdError() {
        BlobContainerSasPermission permissions = new BlobContainerSasPermission().setListPermission(true);

        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);
        String cid = "invalidcid";
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions)
            .setCorrelationId(cid);

        Flux<BlobItem> response = getOAuthServiceAsyncClient().getUserDelegationKey(null, expiryTime)
            .flatMapMany(r -> {
                String keyOid = testResourceNamer.recordValueFromConfig(r.getSignedObjectId());
                r.setSignedObjectId(keyOid);

                String keyTid = testResourceNamer.recordValueFromConfig(r.getSignedTenantId());
                r.setSignedTenantId(keyTid);

                String sasWithPermissions = ccAsync.generateUserDelegationSas(sasValues, r);

                BlobContainerAsyncClient client = getContainerAsyncClient(sasWithPermissions, ccAsync.getBlobContainerUrl());
                return client.listBlobs();
            });

        StepVerifier.create(response)
            .verifyError(BlobStorageException.class);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-12-06")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void blobSasEncryptionScope(boolean userDelegation) {
        BlobSasPermission permissions = new BlobSasPermission()
            .setReadPermission(true)
            .setWritePermission(true);

        BlobContainerClientBuilder builder = getContainerClientBuilder(ccAsync.getBlobContainerUrl())
            .encryptionScope("testscope1").credential(ENVIRONMENT.getPrimaryAccount().getCredential());

        BlockBlobAsyncClient sharedKeyClient = builder.buildAsyncClient().getBlobAsyncClient(generateBlobName())
            .getBlockBlobAsyncClient();

        // Generate a sas token using a client that has an encryptionScope
        BlobServiceSasSignatureValues sasValues = generateValues(permissions);

        Mono<BlobProperties> response = getUserDelegationInfo()
            .flatMap(r -> {
                String sas;
                if (userDelegation) {
                    sas = sharedKeyClient.generateUserDelegationSas(sasValues, r);
                } else {
                    sas = sharedKeyClient.generateSas(sasValues);
                }

                // Generate a sasClient that does not have an encryptionScope
                sasClient = builder.sasToken(sas).encryptionScope(null).buildAsyncClient()
                    .getBlobAsyncClient(sharedKeyClient.getBlobName()).getBlockBlobAsyncClient();

                // Uploading using the encryption scope sas should force the use of the encryptionScope
                return sasClient.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).then(sasClient.getProperties());
            });

        StepVerifier.create(response)
            .assertNext(r -> assertEquals("testscope1", r.getEncryptionScope()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-12-06")
    @Test
    public void accountSasEncryptionScope() {
        AccountSasService service = new AccountSasService()
            .setBlobAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true);
        AccountSasPermission permissions = new AccountSasPermission()
            .setReadPermission(true)
            .setWritePermission(true);
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);

        AccountSasSignatureValues sasValues = new AccountSasSignatureValues(expiryTime, permissions, service,
            resourceType);
        String sas = getServiceClientBuilder(ENVIRONMENT.getPrimaryAccount().getCredential(),
            ENVIRONMENT.getPrimaryAccount().getBlobEndpoint()).encryptionScope("testscope1").buildClient()
            .generateAccountSas(sasValues);
        BlockBlobAsyncClient client = getBlobAsyncClient(sas, ccAsync.getBlobContainerUrl(), blobName, null)
            .getBlockBlobAsyncClient();

        // Uploading using the encryption scope sas should force the use of the encryptionScope
        Mono<BlobProperties> response = client.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize(), true)
            .then(client.getProperties());

        StepVerifier.create(response)
            .assertNext(r -> assertEquals("testscope1", r.getEncryptionScope()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void accountSasTagsAndFilterTags() {
        AccountSasService service = new AccountSasService()
            .setBlobAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true);
        AccountSasPermission permissions = new AccountSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setDeleteVersionPermission(true)
            .setListPermission(true)
            .setUpdatePermission(true)
            .setProcessMessages(true)
            .setFilterTagsPermission(true)
            .setAddPermission(true)
            .setTagsPermission(true);
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);
        AccountSasSignatureValues sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType);
        String sas = primaryBlobServiceClient.generateAccountSas(sasValues);
        BlockBlobAsyncClient client = getBlobAsyncClient(sas, ccAsync.getBlobContainerUrl(), blobName, null)
            .getBlockBlobAsyncClient();
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");

        StepVerifier.create(client.setTags(tags).then(client.getTags()))
            .assertNext(r -> assertEquals(tags, r))
            .verifyComplete();

        BlobServiceAsyncClient serviceClient = getServiceAsyncClient(sas, primaryBlobServiceAsyncClient.getAccountUrl());

        StepVerifier.create(serviceClient.findBlobsByTags("\"foo\"='bar'"))
            .thenConsumeWhile(x -> true)
            .verifyComplete();
    }

    @Test
    public void accountSasTagsFail() {
        AccountSasService service = new AccountSasService()
            .setBlobAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true);
        AccountSasPermission permissions = new AccountSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setDeleteVersionPermission(true)
            .setListPermission(true)
            .setUpdatePermission(true)
            .setProcessMessages(true)
            .setFilterTagsPermission(true)
            .setAddPermission(true);
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);
        AccountSasSignatureValues sasValues = new AccountSasSignatureValues(expiryTime, permissions, service,
            resourceType);
        String sas = primaryBlobServiceAsyncClient.generateAccountSas(sasValues);
        BlockBlobAsyncClient client = getBlobAsyncClient(sas, ccAsync.getBlobContainerUrl(), blobName, null)
            .getBlockBlobAsyncClient();
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");

        StepVerifier.create(client.setTags(tags))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void accountSasFilterTagsFail() {
        AccountSasService service = new AccountSasService()
            .setBlobAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true);
        AccountSasPermission permissions = new AccountSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setDeleteVersionPermission(true)
            .setListPermission(true)
            .setUpdatePermission(true)
            .setProcessMessages(true)
            .setAddPermission(true);
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);
        AccountSasSignatureValues sasValues = new AccountSasSignatureValues(expiryTime, permissions, service,
            resourceType);
        String sas = primaryBlobServiceAsyncClient.generateAccountSas(sasValues);
        BlobServiceAsyncClient client = getServiceAsyncClient(sas, primaryBlobServiceAsyncClient.getAccountUrl());

        StepVerifier.create(client.findBlobsByTags("\"foo\"='bar'"))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void accountSasBlobRead() {
        AccountSasService service = new AccountSasService()
            .setBlobAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true);
        AccountSasPermission permissions = new AccountSasPermission()
            .setReadPermission(true);
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);

        AccountSasSignatureValues sasValues = new AccountSasSignatureValues(expiryTime, permissions, service,
            resourceType);
        String sas = primaryBlobServiceAsyncClient.generateAccountSas(sasValues);
        BlockBlobAsyncClient client = getBlobAsyncClient(sas, ccAsync.getBlobContainerUrl(), blobName, null)
            .getBlockBlobAsyncClient();
        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(client.downloadStream()))
            .assertNext(r -> assertArrayEquals(DATA.getDefaultBytes(), r))
            .verifyComplete();
    }

    @Test
    public void accountSasBlobDeleteFails() {
        AccountSasService service = new AccountSasService()
            .setBlobAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true);
        AccountSasPermission permissions = new AccountSasPermission()
            .setReadPermission(true);
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);

        AccountSasSignatureValues sasValues = new AccountSasSignatureValues(expiryTime, permissions, service,
            resourceType);
        String sas = primaryBlobServiceAsyncClient.generateAccountSas(sasValues);
        BlockBlobAsyncClient client = getBlobAsyncClient(sas, ccAsync.getBlobContainerUrl(), blobName, null)
            .getBlockBlobAsyncClient();
        StepVerifier.create(client.delete())
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void accountSasCreateContainerFails() {
        AccountSasService service = new AccountSasService()
            .setBlobAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true);
        AccountSasPermission permissions = new AccountSasPermission()
            .setReadPermission(true)
            .setCreatePermission(false);
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);

        AccountSasSignatureValues sasValues = new AccountSasSignatureValues(expiryTime, permissions, service,
            resourceType);
        String sas = primaryBlobServiceAsyncClient.generateAccountSas(sasValues);
        BlobServiceAsyncClient sc = getServiceAsyncClient(sas, primaryBlobServiceAsyncClient.getAccountUrl());
        StepVerifier.create(sc.createBlobContainer(generateContainerName()))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void accountSasCreateContainerSucceeds() {
        AccountSasService service = new AccountSasService()
            .setBlobAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true);
        AccountSasPermission permissions = new AccountSasPermission()
            .setReadPermission(true)
            .setCreatePermission(true);
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);

        AccountSasSignatureValues sasValues = new AccountSasSignatureValues(expiryTime, permissions, service,
            resourceType);
        String sas = primaryBlobServiceAsyncClient.generateAccountSas(sasValues);
        BlobServiceAsyncClient sc = getServiceAsyncClient(sas, primaryBlobServiceAsyncClient.getAccountUrl());
        StepVerifier.create(sc.createBlobContainer(generateContainerName()))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void accountSasOnEndpoint() throws IOException {
        AccountSasService service = new AccountSasService()
            .setBlobAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true);
        AccountSasPermission permissions = new AccountSasPermission()
            .setReadPermission(true)
            .setCreatePermission(true);
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);
        AccountSasSignatureValues sasValues = new AccountSasSignatureValues(expiryTime, permissions, service,
            resourceType);
        String sas = primaryBlobServiceAsyncClient.generateAccountSas(sasValues);

        BlobServiceAsyncClient sc = getServiceAsyncClient(primaryBlobServiceAsyncClient.getAccountUrl() + "?" + sas);
        BlobContainerAsyncClient cc = getContainerClientBuilder(primaryBlobServiceAsyncClient.getAccountUrl()
            + "/" + containerName + "?" + sas).buildAsyncClient();
        Mono<BlobContainerProperties> response = sc.createBlobContainer(generateContainerName())
            .then(cc.getProperties());

        StepVerifier.create(response)
            .expectNextCount(1)
            .verifyComplete();

        BlobAsyncClient bc = getBlobAsyncClient(ENVIRONMENT.getPrimaryAccount().getCredential(),
            primaryBlobServiceAsyncClient.getAccountUrl() + "/" + containerName + "/" + blobName + "?" + sas);
        File file = getRandomFile(256);
        file.deleteOnExit();
        StepVerifier.create(bc.uploadFromFile(file.toPath().toString(), true))
            .verifyComplete();
    }

    @Test
    public void canUseSasToAuthenticate() {
        AccountSasService service = new AccountSasService()
            .setBlobAccess(true);
        AccountSasResourceType resourceType = new AccountSasResourceType()
            .setContainer(true)
            .setService(true)
            .setObject(true);
        AccountSasPermission permissions = new AccountSasPermission()
            .setReadPermission(true);
        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);
        AccountSasSignatureValues sasValues = new AccountSasSignatureValues(expiryTime, permissions, service,
            resourceType);
        String sas = primaryBlobServiceAsyncClient.generateAccountSas(sasValues);

        BlobAsyncClient client = instrument(new BlobClientBuilder()
            .endpoint(ccAsync.getBlobContainerUrl())
            .blobName(blobName)
            .sasToken(sas))
            .buildAsyncClient();
        StepVerifier.create(client.getProperties())
            .expectNextCount(1)
            .verifyComplete();

        client = instrument(new BlobClientBuilder()
            .endpoint(ccAsync.getBlobContainerUrl())
            .blobName(blobName)
            .credential(new AzureSasCredential(sas)))
            .buildAsyncClient();
        StepVerifier.create(client.getProperties())
            .expectNextCount(1)
            .verifyComplete();

        client = instrument(new BlobClientBuilder()
            .endpoint(ccAsync.getBlobContainerUrl() + "?" + sas)
            .blobName(blobName))
            .buildAsyncClient();
        StepVerifier.create(client.getProperties())
            .expectNextCount(1)
            .verifyComplete();

        BlockBlobAsyncClient blockClient = instrument(new SpecializedBlobClientBuilder()
            .endpoint(ccAsync.getBlobContainerUrl())
            .blobName(blobName)
            .sasToken(sas))
            .buildBlockBlobAsyncClient();
        StepVerifier.create(blockClient.getProperties())
            .expectNextCount(1)
            .verifyComplete();

        blockClient = instrument(new SpecializedBlobClientBuilder()
            .endpoint(ccAsync.getBlobContainerUrl())
            .blobName(blobName)
            .credential(new AzureSasCredential(sas)))
            .buildBlockBlobAsyncClient();
        StepVerifier.create(blockClient.getProperties())
            .expectNextCount(1)
            .verifyComplete();

        blockClient = instrument(new SpecializedBlobClientBuilder()
            .endpoint(ccAsync.getBlobContainerUrl() + "?" + sas)
            .blobName(blobName))
            .buildBlockBlobAsyncClient();
        StepVerifier.create(blockClient.getProperties())
            .expectNextCount(1)
            .verifyComplete();

        BlobContainerAsyncClient containerClient = instrument(new BlobContainerClientBuilder()
            .endpoint(ccAsync.getBlobContainerUrl())
            .sasToken(sas))
            .buildAsyncClient();
        StepVerifier.create(containerClient.getProperties())
            .expectNextCount(1)
            .verifyComplete();

        containerClient = instrument(new BlobContainerClientBuilder()
            .endpoint(ccAsync.getBlobContainerUrl())
            .credential(new AzureSasCredential(sas)))
            .buildAsyncClient();
        StepVerifier.create(containerClient.getProperties())
            .expectNextCount(1)
            .verifyComplete();

        containerClient = instrument(new BlobContainerClientBuilder()
            .endpoint(ccAsync.getBlobContainerUrl() + "?" + sas))
            .buildAsyncClient();
        StepVerifier.create(containerClient.getProperties())
            .expectNextCount(1)
            .verifyComplete();

        BlobServiceAsyncClient serviceClient = instrument(new BlobServiceClientBuilder()
            .endpoint(ccAsync.getBlobContainerUrl())
            .sasToken(sas))
            .buildAsyncClient();
        StepVerifier.create(serviceClient.getProperties())
            .expectNextCount(1)
            .verifyComplete();

        serviceClient = instrument(new BlobServiceClientBuilder()
            .endpoint(ccAsync.getBlobContainerUrl())
            .credential(new AzureSasCredential(sas)))
            .buildAsyncClient();
        StepVerifier.create(serviceClient.getProperties())
            .expectNextCount(1)
            .verifyComplete();

        serviceClient = instrument(new BlobServiceClientBuilder()
            .endpoint(ccAsync.getBlobContainerUrl() + "?" + sas))
            .buildAsyncClient();
        StepVerifier.create(serviceClient.getProperties())
            .expectNextCount(1)
            .verifyComplete();
    }

    private BlobServiceSasSignatureValues generateValues(BlobSasPermission permission) {
        return new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1), permission)
            .setStartTime(testResourceNamer.now().minusDays(1))
            .setProtocol(SasProtocol.HTTPS_HTTP)
            .setCacheControl("cache")
            .setContentDisposition("disposition")
            .setContentEncoding("encoding")
            .setContentLanguage("language")
            .setContentType("type");
    }

    private boolean validateSasProperties(BlobProperties properties) {
        boolean ret;
        ret = properties.getCacheControl().equals("cache");
        ret &= properties.getContentDisposition().equals("disposition");
        ret &= properties.getContentEncoding().equals("encoding");
        ret &= properties.getContentLanguage().equals("language");
        return ret;
    }

    private Mono<UserDelegationKey> getUserDelegationInfo() {
        return getOAuthServiceAsyncClient().getUserDelegationKey(testResourceNamer.now().minusDays(1),
            testResourceNamer.now().plusDays(1))
            .flatMap(r -> {
                String keyOid = testResourceNamer.recordValueFromConfig(r.getSignedObjectId());
                r.setSignedObjectId(keyOid);
                String keyTid = testResourceNamer.recordValueFromConfig(r.getSignedTenantId());
                r.setSignedTenantId(keyTid);
                return Mono.just(r);
            });
    }

    /*
     This test will ensure that each field gets placed into the proper location within the string to sign and that null
     values are handled correctly. We will validate the whole SAS with service calls as well as correct serialization of
     individual parts later.
     */
    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-12-06")
    @ParameterizedTest
    @MethodSource("blobSasImplUtilStringToSignSupplier")
    public void blobSasImplUtilStringToSign(OffsetDateTime startTime, String identifier, SasIpRange ipRange,
                                            SasProtocol protocol, String snapId, String cacheControl, String disposition,
                                            String encoding, String language, String type, String versionId,
                                            String encryptionScope, String expectedStringToSign) {
        OffsetDateTime e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0,
            0, ZoneOffset.UTC);
        BlobSasPermission p = new BlobSasPermission();
        p.setReadPermission(true);
        BlobServiceSasSignatureValues v = new BlobServiceSasSignatureValues(e, p);

        String expected = String.format(expectedStringToSign, ENVIRONMENT.getPrimaryAccount().getName());

        v.setStartTime(startTime);

        if (ipRange != null) {
            SasIpRange ipR = new SasIpRange();
            ipR.setIpMin("ip");
            v.setSasIpRange(ipR);
        }

        v.setIdentifier(identifier)
            .setProtocol(protocol)
            .setCacheControl(cacheControl)
            .setContentDisposition(disposition)
            .setContentEncoding(encoding)
            .setContentLanguage(language)
            .setContentType(type);

        BlobSasImplUtil implUtil = new BlobSasImplUtil(v, "containerName", "blobName", snapId,
            versionId, encryptionScope);

        String sasToken = implUtil.generateSas(ENVIRONMENT.getPrimaryAccount().getCredential(), Context.NONE);

        CommonSasQueryParameters token = BlobUrlParts.parse(ccAsync.getBlobContainerUrl() + "?" + sasToken)
            .getCommonSasQueryParameters();

        assertEquals(token.getSignature(), ENVIRONMENT.getPrimaryAccount().getCredential().computeHmac256(expected));
    }

    /*
    We don't test the blob or containerName properties because canonicalized resource is always added as at least
    /blob/accountName. We test canonicalization of resources later. Again, this is not to test a fully functional
    sas but the construction of the string to sign.
    Signed resource is tested elsewhere, as we work some minor magic in choosing which value to use.
    */
    private static Stream<Arguments> blobSasImplUtilStringToSignSupplier() {
        return Stream.of(
            Arguments.of(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), null, null, null, null, null, null, null, null, null, null, null, "r\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, "id", null, null, null, null, null, null, null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\nid\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, null, new SasIpRange(), null, null, null, null, null, null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\nip\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, null, null, SasProtocol.HTTPS_ONLY, null, null, null, null, null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n" + SasProtocol.HTTPS_ONLY + "\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, null, null, null, "snapId", null, null, null, null, null, null, null,  "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nbs\nsnapId\n\n\n\n\n\n"),
            Arguments.of(null, null, null, null, null, "control", null, null, null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\ncontrol\n\n\n\n"),
            Arguments.of(null, null, null, null, null, null, "disposition", null, null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\ndisposition\n\n\n"),
            Arguments.of(null, null, null, null, null, null, null, "encoding", null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\nencoding\n\n"),
            Arguments.of(null, null, null, null, null, null, null, null, "language", null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\nlanguage\n"),
            Arguments.of(null, null, null, null, null, null, null, null, null, "type", null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\ntype"),
            Arguments.of(null, null, null, null, null, null, null, null, null, null, "versionId", null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nbv\nversionId\n\n\n\n\n\n"),
            Arguments.of(null, null, null, null, null, null, null, null, null, null, null, "encryptionScope", "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\nencryptionScope\n\n\n\n\n")
        );
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-12-06")
    @ParameterizedTest
    @MethodSource("blobSasImplUtilStringToSignUserDelegationKeySupplier")
    public void blobSasImplUtilStringToSignUserDelegationKey(OffsetDateTime startTime, String keyOid, String keyTid,
                                                             OffsetDateTime keyStart, OffsetDateTime keyExpiry,
                                                             String keyService, String keyVersion, String keyValue,
                                                             SasIpRange ipRange, SasProtocol protocol, String snapId,
                                                             String cacheControl, String disposition, String encoding,
                                                             String language, String type, String versionId, String saoid,
                                                             String cid, String encryptionScope, String expectedStringToSign) {
        OffsetDateTime e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0,
            0, ZoneOffset.UTC);
        BlobSasPermission p = new BlobSasPermission().setReadPermission(true);
        BlobServiceSasSignatureValues v = new BlobServiceSasSignatureValues(e, p);

        String expected = String.format(expectedStringToSign, ENVIRONMENT.getPrimaryAccount().getName());

        v.setStartTime(startTime);

        if (ipRange != null) {
            SasIpRange ipR = new SasIpRange();
            ipR.setIpMin("ip");
            v.setSasIpRange(ipR);
        }

        v.setProtocol(protocol)
            .setCacheControl(cacheControl)
            .setContentDisposition(disposition)
            .setContentEncoding(encoding)
            .setContentLanguage(language)
            .setContentType(type)
            .setPreauthorizedAgentObjectId(saoid)
            .setCorrelationId(cid);

        UserDelegationKey key = new UserDelegationKey()
            .setSignedObjectId(keyOid)
            .setSignedTenantId(keyTid)
            .setSignedStart(keyStart)
            .setSignedExpiry(keyExpiry)
            .setSignedService(keyService)
            .setSignedVersion(keyVersion)
            .setValue(keyValue);

        BlobSasImplUtil implUtil = new BlobSasImplUtil(v, "containerName", "blobName", snapId,
            versionId, encryptionScope);
        String sasToken = implUtil.generateUserDelegationSas(key, ENVIRONMENT.getPrimaryAccount().getName(),
            Context.NONE);
        CommonSasQueryParameters token = BlobUrlParts.parse(cc.getBlobContainerUrl() + "?" + sasToken)
            .getCommonSasQueryParameters();

        assertEquals(token.getSignature(), StorageImplUtils.computeHMac256(key.getValue(), expected));
    }

    /*
    We test string to sign functionality directly related toUserDelegation sas specific parameters
    */
    private static Stream<Arguments> blobSasImplUtilStringToSignUserDelegationKeySupplier() {
        return Stream.of(
            Arguments.of(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), null, null, null, null, null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null, null, null, null, null, null, null, null, null, null, null, null, "r\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, "11111111-1111-1111-1111-111111111111", null, null, null, null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null, null, null, null, null, null, null, null, null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n11111111-1111-1111-1111-111111111111\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, null, "22222222-2222-2222-2222-222222222222", null, null, null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null, null, null, null, null, null, null, null, null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n22222222-2222-2222-2222-222222222222\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, null, null, OffsetDateTime.of(LocalDateTime.of(2018, 1, 1, 0, 0), ZoneOffset.UTC), null, null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null, null, null, null, null, null, null, null, null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n2018-01-01T00:00:00Z\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, null, null, null, OffsetDateTime.of(LocalDateTime.of(2018, 1, 1, 0, 0), ZoneOffset.UTC), null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null, null, null, null, null, null, null, null, null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n2018-01-01T00:00:00Z\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, null, null, null, null, "b", null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null, null, null, null, null, null, null, null, null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\nb\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, null, null, null, null, null, "2018-06-17", "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null, null, null, null, null, null, null, null, null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n2018-06-17\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, null, null, null, null, null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", new SasIpRange(), null, null, null, null, null, null, null, null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\nip\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, null, null, null, null, null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null, SasProtocol.HTTPS_ONLY, null, null, null, null, null, null, null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n" + SasProtocol.HTTPS_ONLY + "\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, null, null, null, null, null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null, null, "snapId", null, null, null, null, null, null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nbs\nsnapId\n\n\n\n\n\n"),
            Arguments.of(null, null, null, null, null, null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null, null, null, "control", null, null, null, null, null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\ncontrol\n\n\n\n"),
            Arguments.of(null, null, null, null, null, null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null, null, null, null, "disposition", null, null, null, null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\ndisposition\n\n\n"),
            Arguments.of(null, null, null, null, null, null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null, null, null, null, null, "encoding", null, null, null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\nencoding\n\n"),
            Arguments.of(null, null, null, null, null,  null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null, null, null, null, null, null, "language", null, null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\nlanguage\n"),
            Arguments.of(null, null, null, null, null, null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null, null, null, null, null, null, null, "type", null, null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\ntype"),
            Arguments.of(null, null, null, null, null, null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null, null, null, null, null, null, null, null, "versionId", null, null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nbv\nversionId\n\n\n\n\n\n"),
            Arguments.of(null, null, null, null, null, null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null, null, null, null, null, null, null, null, null, "saoid", null, null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\nsaoid\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, null, null, null, null, null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null, null, null, null, null, null, null, null, null, null, "cid", null, "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\ncid\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of(null, null, null, null, null, null, null, "3hd4LRwrARVGbeMRQRfTLIsGMkCPuZJnvxZDU7Gak8c=", null, null, null, null, null, null, null, null, null, null, null, "encryptionScope", "r\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n/blob/%s/containerName/blobName\n\n\n\n\n\n\n\n\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\nencryptionScope\n\n\n\n\n")
        );
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-12-06")
    @ParameterizedTest
    @MethodSource("blobSasImplUtilCanonicalizedResourceSupplier")
    public void blobSasImplUtilCanonicalizedResource(String containerName, String blobName, String snapId,
                                                     OffsetDateTime expiryTime, String expectedResource,
                                                     String expectedStringToSign) {
        BlobServiceSasSignatureValues v = new BlobServiceSasSignatureValues(expiryTime, new BlobSasPermission());
        BlobSasImplUtil implUtil = new BlobSasImplUtil(v, containerName, blobName, snapId, null, null);

        expectedStringToSign = String.format(expectedStringToSign,
            Constants.ISO_8601_UTC_DATE_FORMATTER.format(expiryTime),
            ENVIRONMENT.getPrimaryAccount().getName());

        String token = implUtil.generateSas(ENVIRONMENT.getPrimaryAccount().getCredential(), Context.NONE);

        CommonSasQueryParameters queryParams = new CommonSasQueryParameters(SasImplUtils.parseQueryString(token),
            true);

        assertEquals(queryParams.getSignature(),
            ENVIRONMENT.getPrimaryAccount().getCredential().computeHmac256(expectedStringToSign));
        assertEquals(expectedResource, queryParams.getResource());
    }

    private static Stream<Arguments> blobSasImplUtilCanonicalizedResourceSupplier() {
        return Stream.of(
            Arguments.of("c", "b", "id", OffsetDateTime.now(), "bs", "\n\n%s\n" + "/blob/%s/c/b\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nbs\nid\n\n\n\n\n\n"),
            Arguments.of("c", "b", null, OffsetDateTime.now(), "b", "\n\n%s\n" + "/blob/%s/c/b\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nb\n\n\n\n\n\n\n"),
            Arguments.of("c", null, null, OffsetDateTime.now(), "c", "\n\n%s\n" + "/blob/%s/c\n\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nc\n\n\n\n\n\n\n")
        );
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-12-06")
    @ParameterizedTest
    @MethodSource("accountSasImplUtilStringToSignSupplier")
    public void accountSasImplUtilStringToSign(OffsetDateTime startTime, SasIpRange ipRange, SasProtocol protocol,
                                               String encryptionScope, String expectedStringToSign) {
        AccountSasPermission p = new AccountSasPermission().setReadPermission(true);
        OffsetDateTime e = OffsetDateTime.of(2017, 1, 1, 0, 0, 0,
            0, ZoneOffset.UTC);
        AccountSasService s = new AccountSasService().setBlobAccess(true);
        AccountSasResourceType rt = new AccountSasResourceType().setObject(true);
        AccountSasSignatureValues v = new AccountSasSignatureValues(e, p, s, rt)
            .setStartTime(startTime);
        if (ipRange != null) {
            SasIpRange ipR = new SasIpRange();
            ipR.setIpMin("ip");
            v.setSasIpRange(ipR);
        }

        v.setProtocol(protocol);

        AccountSasImplUtil implUtil = new AccountSasImplUtil(v, encryptionScope);
        String sasToken = implUtil.generateSas(ENVIRONMENT.getPrimaryAccount().getCredential(), Context.NONE);
        CommonSasQueryParameters token = BlobUrlParts.parse(cc.getBlobContainerUrl() + "?" + sasToken)
            .getCommonSasQueryParameters();

        assertEquals(token.getSignature(), ENVIRONMENT.getPrimaryAccount().getCredential()
            .computeHmac256(String.format(expectedStringToSign, ENVIRONMENT.getPrimaryAccount().getName())));
    }

    private static Stream<Arguments> accountSasImplUtilStringToSignSupplier() {
        return Stream.of(
            Arguments.of(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC), null, null, null, "%s" + "\nr\nb\no\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n\n\n" + Constants.SAS_SERVICE_VERSION + "\n\n"),
            Arguments.of(null, new SasIpRange(), null, null, "%s" + "\nr\nb\no\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\nip\n\n" + Constants.SAS_SERVICE_VERSION + "\n\n"),
            Arguments.of(null, null, SasProtocol.HTTPS_ONLY, null, "%s" + "\nr\nb\no\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n\n" + SasProtocol.HTTPS_ONLY + "\n" + Constants.SAS_SERVICE_VERSION + "\n\n"),
            Arguments.of(null, null, null, "encryptionScope", "%s" + "\nr\nb\no\n\n" + Constants.ISO_8601_UTC_DATE_FORMATTER.format(OffsetDateTime.of(2017, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)) + "\n\n\n" + Constants.SAS_SERVICE_VERSION + "\nencryptionScope\n")
        );
    }
}
