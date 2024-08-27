// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Context;
import com.azure.core.util.polling.PollerFlux;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobAccessPolicy;
import com.azure.storage.blob.models.BlobAudience;
import com.azure.storage.blob.models.BlobCopyInfo;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobSignedIdentifier;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.BlobType;
import com.azure.storage.blob.models.CopyStatusType;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.blob.models.LeaseStateType;
import com.azure.storage.blob.models.LeaseStatusType;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.models.ObjectReplicationPolicy;
import com.azure.storage.blob.models.ObjectReplicationStatus;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.models.RehydratePriority;
import com.azure.storage.blob.models.TaggedBlobItem;
import com.azure.storage.blob.options.BlobContainerCreateOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.options.BlobSetAccessTierOptions;
import com.azure.storage.blob.options.FindBlobsOptions;
import com.azure.storage.blob.options.PageBlobCreateOptions;
import com.azure.storage.blob.specialized.AppendBlobAsyncClient;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.PageBlobAsyncClient;
import com.azure.storage.common.test.shared.TestHttpClientType;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.extensions.PlaybackOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.test.StepVerifier;

import java.net.URL;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContainerAsyncApiTests extends BlobTestBase {
    private String tagKey;
    private String tagValue;

    @BeforeEach
    public void setup() {
        tagKey = testResourceNamer.randomName(prefix, 20);
        tagValue = testResourceNamer.randomName(prefix, 20);
    }

    @Test
    public void blobNameNull() {
        assertThrows(NullPointerException.class, () -> cc.getBlobClient(null));
    }

    @Test
    public void createAllNull() {
        // Overwrite the existing cc, which has already been created
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());

        StepVerifier.create(ccAsync.createWithResponse(null, null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                assertTrue(validateBasicHeaders(r.getHeaders()));
            })
            .verifyComplete();
    }

    @Test
    public void createMin() {
        BlobContainerAsyncClient cc = primaryBlobServiceAsyncClient.createBlobContainer(generateContainerName()).block();
        StepVerifier.create(cc.exists())
            .expectNext(true)
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("createMetadataSupplier")
    public void createMetadata(String key1, String value1, String key2, String value2) {
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }

        ccAsync.createWithResponse(metadata, null).block();
        StepVerifier.create(ccAsync.getPropertiesWithResponse(null))
            .assertNext(r -> {
                if (ENVIRONMENT.getHttpClientType() == TestHttpClientType.JDK_HTTP) {
                    // JDK HttpClient returns headers with names lowercased.
                    Map<String, String> lowercasedMetadata = metadata.entrySet().stream()
                        .collect(Collectors.toMap(e -> e.getKey().toLowerCase(), Map.Entry::getValue));
                    assertEquals(lowercasedMetadata, r.getValue().getMetadata());
                } else {
                    assertEquals(metadata, r.getValue().getMetadata());
                }
            })
            .verifyComplete();
    }

    private static Stream<Arguments> createMetadataSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of("foo", "bar", "fizz", "buzz"),
            Arguments.of("testFoo", "testBar", "testFizz", "testBuzz")
        );
    }

    @ParameterizedTest
    @MethodSource("publicAccessSupplier")
    @PlaybackOnly
    public void createPublicAccess(PublicAccessType publicAccess) {
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());
        ccAsync.createWithResponse(null, publicAccess).block();
        StepVerifier.create(ccAsync.getProperties())
            .assertNext(r ->  assertEquals(r.getBlobPublicAccess(), publicAccess))
            .verifyComplete();
    }

    private static Stream<Arguments> publicAccessSupplier() {
        return Stream.of(
            Arguments.of(PublicAccessType.BLOB),
            Arguments.of(PublicAccessType.CONTAINER),
            Arguments.of((PublicAccessType) null));
    }

    @Test
    public void createError() {
        StepVerifier.create(ccAsync.create())
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertExceptionStatusCodeAndMessage(e, 409, BlobErrorCode.CONTAINER_ALREADY_EXISTS);
                assertTrue(e.getServiceMessage().contains("The specified container already exists."));
            });
    }

    @Test
    public void createIfNotExistsAllNull() {
        // Overwrite the existing cc, which has already been created
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());

        StepVerifier.create(ccAsync.createIfNotExistsWithResponse(null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 201);
                assertTrue(validateBasicHeaders(r.getHeaders()));
            })
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsMin() {
        BlobContainerAsyncClient cc = primaryBlobServiceAsyncClient.createBlobContainerIfNotExists(generateContainerName()).block();

        StepVerifier.create(cc.exists())
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsMinContainer() {
        BlobContainerAsyncClient cc = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());

        StepVerifier.create(cc.createIfNotExists())
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsWithResponse() {
        assertAsyncResponseStatusCode(primaryBlobServiceAsyncClient
            .createBlobContainerIfNotExistsWithResponse(generateContainerName(), null), 201);
    }

    @Test
    public void createIfNotExistsBlobServiceThatAlreadyExists() {
        String containerName = generateContainerName();

        assertAsyncResponseStatusCode(primaryBlobServiceAsyncClient.createBlobContainerIfNotExistsWithResponse(
            containerName, null), 201);
        assertAsyncResponseStatusCode(primaryBlobServiceAsyncClient.createBlobContainerIfNotExistsWithResponse(
            containerName, null), 409);
    }

    @ParameterizedTest
    @MethodSource("createMetadataSupplier")
    public void createIfNotExistsMetadataIfNotExists(String key1, String value1, String key2, String value2) {
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }
        BlobContainerCreateOptions options = new BlobContainerCreateOptions().setMetadata(metadata);

        StepVerifier.create(ccAsync.createIfNotExistsWithResponse(options))
            .assertNext(r -> assertTrue(r.getValue()))
            .verifyComplete();

        StepVerifier.create(ccAsync.getPropertiesWithResponse(null))
            .assertNext(r -> {
                if (ENVIRONMENT.getHttpClientType() == TestHttpClientType.JDK_HTTP) {
                    // JDK HttpClient returns headers with names lowercased.
                    Map<String, String> lowercasedMetadata = metadata.entrySet().stream()
                        .collect(Collectors.toMap(e -> e.getKey().toLowerCase(), Map.Entry::getValue));
                    assertEquals(lowercasedMetadata, r.getValue().getMetadata());
                } else {
                    assertEquals(metadata, r.getValue().getMetadata());
                }
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("publicAccessSupplier")
    @PlaybackOnly
    public void createIfNotExistsPublicAccess(PublicAccessType publicAccess) {
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());

        StepVerifier.create(ccAsync.createIfNotExistsWithResponse(new BlobContainerCreateOptions()
            .setPublicAccessType(publicAccess)))
            .assertNext(r -> assertTrue(r.getValue()))
            .verifyComplete();

        StepVerifier.create(ccAsync.getProperties())
            .assertNext(r -> assertEquals(r.getBlobPublicAccess(), publicAccess))
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsOnContainerThatAlreadyExists() {
        StepVerifier.create(ccAsync.createIfNotExists())
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsOnAContainerThatAlreadyExistsWithResponse() {
        BlobContainerAsyncClient cc = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());

        StepVerifier.create(cc.createIfNotExistsWithResponse(null))
            .assertNext(r -> {
                assertEquals(r.getStatusCode(), 201);
                assertTrue(r.getValue());
            })
            .verifyComplete();

        StepVerifier.create(cc.createIfNotExistsWithResponse(null))
            .assertNext(r -> {
                assertEquals(r.getStatusCode(), 409);
                assertFalse(r.getValue());
            })
            .verifyComplete();
    }

    @Test
    public void getPropertiesNull() {
        StepVerifier.create(ccAsync.getPropertiesWithResponse(null))
            .assertNext(r -> {
                assertTrue(validateBasicHeaders(r.getHeaders()));
                assertNull(r.getValue().getBlobPublicAccess());
                assertFalse(r.getValue().hasImmutabilityPolicy());
                assertFalse(r.getValue().hasLegalHold());
                assertNull(r.getValue().getLeaseDuration());
                assertEquals(r.getValue().getLeaseState(), LeaseStateType.AVAILABLE);
                assertEquals(r.getValue().getLeaseStatus(), LeaseStatusType.UNLOCKED);
                assertEquals(0, r.getValue().getMetadata().size());
                assertFalse(r.getValue().isEncryptionScopeOverridePrevented());
                assertNotNull(r.getValue().getDefaultEncryptionScope());
            })
            .verifyComplete();
    }

    @Test
    public void getPropertiesMin() {
        StepVerifier.create(ccAsync.getProperties())
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    @Test
    public void getPropertiesLease() {
        String leaseID = setupContainerAsyncLeaseCondition(ccAsync, RECEIVED_LEASE_ID);

        assertAsyncResponseStatusCode(ccAsync.getPropertiesWithResponse(leaseID), 200);
    }

    @Test
    public void getPropertiesLeaseFail() {
        StepVerifier.create(ccAsync.getPropertiesWithResponse("garbage"))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void getPropertiesError() {
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());
        StepVerifier.create(ccAsync.getProperties())
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void setMetadata() {
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());
        Map<String, String> metadata = new HashMap<>();
        metadata.put("key", "value");
        ccAsync.createWithResponse(metadata, null).block();

        StepVerifier.create(ccAsync.setMetadataWithResponse(null, null))
            .assertNext(r -> {
                assertEquals(r.getStatusCode(), 200);
                assertTrue(validateBasicHeaders(r.getHeaders()));
            })
            .verifyComplete();

        StepVerifier.create(ccAsync.getPropertiesWithResponse(null))
            .assertNext(r -> assertEquals(r.getValue().getMetadata().size(), 0))
            .verifyComplete();
    }

    @Test
    public void setMetadataMin() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("foo", "bar");

        ccAsync.setMetadata(metadata).block();

        StepVerifier.create(ccAsync.getPropertiesWithResponse(null))
            .assertNext(r -> assertEquals(r.getValue().getMetadata(), metadata))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("setMetadataMetadataSupplier")
    public void setMetadataMetadata(String key1, String value1, String key2, String value2) {
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }

        assertAsyncResponseStatusCode(ccAsync.setMetadataWithResponse(metadata, null), 200);

        StepVerifier.create(ccAsync.getPropertiesWithResponse(null))
            .assertNext(r -> assertEquals(r.getValue().getMetadata(), metadata))
            .verifyComplete();    }

    private static Stream<Arguments> setMetadataMetadataSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of("foo", "bar", "fizz", "buzz")
        );
    }

    @ParameterizedTest
    @MethodSource("setMetadataACSupplier")
    public void setMetadataAC(OffsetDateTime modified, String leaseID) {
        leaseID = setupContainerAsyncLeaseCondition(ccAsync, leaseID);
        BlobRequestConditions cac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified);

        assertAsyncResponseStatusCode(ccAsync.setMetadataWithResponse(null, cac), 200);
    }

    private static Stream<Arguments> setMetadataACSupplier() {
        return Stream.of(
            Arguments.of(null, null),
            Arguments.of(OLD_DATE, null),
            Arguments.of(null, RECEIVED_LEASE_ID));
    }

    @ParameterizedTest
    @MethodSource("setMetadataACFailSupplier")
    public void setMetadataACFail(OffsetDateTime modified, String leaseID) {
        BlobRequestConditions cac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified);

        StepVerifier.create(ccAsync.setMetadataWithResponse(null, cac))
            .verifyError(BlobStorageException.class);
    }

    private static Stream<Arguments> setMetadataACFailSupplier() {
        return Stream.of(
            Arguments.of(NEW_DATE, null),
            Arguments.of(null, GARBAGE_LEASE_ID));
    }

    @ParameterizedTest
    @MethodSource("setMetadataACIllegalSupplier")
    public void setMetadataACIllegal(OffsetDateTime unmodified, String match, String noneMatch) {
        BlobRequestConditions mac = new BlobRequestConditions()
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        StepVerifier.create(ccAsync.setMetadataWithResponse(null, mac))
            .verifyError(UnsupportedOperationException.class);
    }

    private static Stream<Arguments> setMetadataACIllegalSupplier() {
        return Stream.of(
            Arguments.of(NEW_DATE, null, null),
            Arguments.of(null, RECEIVED_ETAG, null),
            Arguments.of(null, null, GARBAGE_ETAG));
    }

    @Test
    public void setMetadataError() {
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());
        StepVerifier.create(ccAsync.setMetadata(null))
            .verifyError(BlobStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("publicAccessSupplier")
    @PlaybackOnly
    public void setAccessPolicy(PublicAccessType publicAccess) {
        StepVerifier.create(ccAsync.setAccessPolicyWithResponse(publicAccess, null, null))
            .assertNext(r -> assertTrue(validateBasicHeaders(r.getHeaders())))
            .verifyComplete();

        StepVerifier.create(ccAsync.getProperties())
            .assertNext(r -> assertEquals(r.getBlobPublicAccess(), publicAccess))
            .verifyComplete();
    }

    @Test
    public void setAccessPolicyIds() {
        BlobSignedIdentifier identifier = new BlobSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new BlobAccessPolicy()
                .setStartsOn(testResourceNamer.now())
                .setExpiresOn(testResourceNamer.now().plusDays(1))
                .setPermissions("r"));
        BlobSignedIdentifier identifier2 = new BlobSignedIdentifier()
            .setId("0001")
            .setAccessPolicy(new BlobAccessPolicy()
                .setStartsOn(testResourceNamer.now())
                .setExpiresOn(testResourceNamer.now().plusDays(2))
                .setPermissions("w"));
        List<BlobSignedIdentifier> ids = Arrays.asList(identifier, identifier2);

        StepVerifier.create(ccAsync.setAccessPolicyWithResponse(null, ids, null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 200);
                assertTrue(validateBasicHeaders(r.getHeaders()));
            })
            .verifyComplete();

        StepVerifier.create(ccAsync.getAccessPolicyWithResponse(null))
            .assertNext(r -> {
                List<BlobSignedIdentifier> receivedIdentifiers = r.getValue().getIdentifiers();
                assertEquals(receivedIdentifiers.get(0).getAccessPolicy().getExpiresOn(),
                    identifier.getAccessPolicy().getExpiresOn());
                assertEquals(receivedIdentifiers.get(0).getAccessPolicy().getStartsOn(),
                    identifier.getAccessPolicy().getStartsOn());
                assertEquals(receivedIdentifiers.get(0).getAccessPolicy().getPermissions(),
                    identifier.getAccessPolicy().getPermissions());
                assertEquals(receivedIdentifiers.get(1).getAccessPolicy().getExpiresOn(),
                    identifier2.getAccessPolicy().getExpiresOn());
                assertEquals(receivedIdentifiers.get(1).getAccessPolicy().getStartsOn(),
                    identifier2.getAccessPolicy().getStartsOn());
                assertEquals(receivedIdentifiers.get(1).getAccessPolicy().getPermissions(),
                    identifier2.getAccessPolicy().getPermissions());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("setAccessPolicyACSupplier")
    public void setAccessPolicyAC(OffsetDateTime modified, OffsetDateTime unmodified, String leaseID) {
        leaseID = setupContainerAsyncLeaseCondition(ccAsync, leaseID);
        BlobRequestConditions cac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(ccAsync.setAccessPolicyWithResponse(null, null, cac),
            200);
    }

    private static Stream<Arguments> setAccessPolicyACSupplier() {
        return Stream.of(
            Arguments.of(null, null, null),
            Arguments.of(OLD_DATE, null, null),
            Arguments.of(null, NEW_DATE, null),
            Arguments.of(null, null, RECEIVED_LEASE_ID));
    }

    @ParameterizedTest
    @MethodSource("setAccessPolicyACFailSupplier")
    public void setAccessPolicyACFail(OffsetDateTime modified, OffsetDateTime unmodified, String leaseID) {
        BlobRequestConditions cac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(ccAsync.setAccessPolicyWithResponse(null, null, cac))
            .verifyError(BlobStorageException.class);
    }

    private static Stream<Arguments> setAccessPolicyACFailSupplier() {
        return Stream.of(
            Arguments.of(NEW_DATE, null, null),
            Arguments.of(null, OLD_DATE, null),
            Arguments.of(null, null, GARBAGE_LEASE_ID));
    }

    @ParameterizedTest
    @MethodSource("setAccessPolicyACIllegalSupplier")
    public void setAccessPolicyACIllegal(String match, String noneMatch) {
        BlobRequestConditions mac = new BlobRequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch);

        StepVerifier.create(ccAsync.setAccessPolicyWithResponse(null, null, mac))
            .verifyError(UnsupportedOperationException.class);
    }

    private static Stream<Arguments> setAccessPolicyACIllegalSupplier() {
        return Stream.of(
            Arguments.of(RECEIVED_ETAG, null),
            Arguments.of(null, GARBAGE_ETAG));
    }

    @Test
    public void getAccessPolicyLease() {
        String leaseID = setupContainerAsyncLeaseCondition(ccAsync, RECEIVED_LEASE_ID);
        assertAsyncResponseStatusCode(ccAsync.getAccessPolicyWithResponse(leaseID), 200);
    }

    @Test
    public void getAccessPolicyLeaseFail() {
        StepVerifier.create(ccAsync.getAccessPolicyWithResponse(GARBAGE_LEASE_ID))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void getAccessPolicyError() {
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());
        StepVerifier.create(ccAsync.getAccessPolicy())
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void delete() {
        StepVerifier.create(ccAsync.deleteWithResponse(null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 202);
                assertNotNull(r.getHeaders().getValue(X_MS_REQUEST_ID));
                assertNotNull(r.getHeaders().getValue(X_MS_VERSION));
                assertNotNull(r.getHeaders().getValue(HttpHeaderName.DATE));
            })
            .verifyComplete();
    }

    @Test
    public void deleteMin() {
        ccAsync.delete().block();
        StepVerifier.create(ccAsync.exists())
            .expectNext(false)
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("setAccessPolicyACSupplier")
    public void deleteAC(OffsetDateTime modified, OffsetDateTime unmodified, String leaseID) {
        leaseID = setupContainerAsyncLeaseCondition(ccAsync, leaseID);
        BlobRequestConditions cac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(ccAsync.deleteWithResponse(cac), 202);
    }

    @ParameterizedTest
    @MethodSource("setAccessPolicyACFailSupplier")
    public void deleteACFail(OffsetDateTime modified, OffsetDateTime unmodified, String leaseID) {
        BlobRequestConditions cac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(ccAsync.deleteWithResponse(cac))
            .verifyError(BlobStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("setAccessPolicyACIllegalSupplier")
    public void deleteACIllegal(String match, String noneMatch) {
        BlobRequestConditions mac = new BlobRequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch);

        StepVerifier.create(ccAsync.deleteWithResponse(mac))
            .verifyError(UnsupportedOperationException.class);
    }

    @Test
    public void deleteError() {
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());
        StepVerifier.create(ccAsync.delete())
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void deleteIfExists() {
        StepVerifier.create(ccAsync.deleteIfExistsWithResponse(null))
            .assertNext(r -> {
                assertTrue(r.getValue());
                assertResponseStatusCode(r, 202);
                assertNotNull(r.getHeaders().getValue(X_MS_REQUEST_ID));
                assertNotNull(r.getHeaders().getValue(X_MS_VERSION));
                assertNotNull(r.getHeaders().getValue(HttpHeaderName.DATE));
            })
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsMin() {
        StepVerifier.create(ccAsync.deleteIfExists())
            .expectNext(true)
            .verifyComplete();

        StepVerifier.create(ccAsync.exists())
            .expectNext(false)
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("setAccessPolicyACSupplier")
    public void deleteIfExistsAC(OffsetDateTime modified, OffsetDateTime unmodified, String leaseID) {
        leaseID = setupContainerAsyncLeaseCondition(ccAsync, leaseID);
        BlobRequestConditions cac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(ccAsync.deleteIfExistsWithResponse(cac), 202);
    }

    @ParameterizedTest
    @MethodSource("setAccessPolicyACFailSupplier")
    public void deleteIfExistsACFail(OffsetDateTime modified, OffsetDateTime unmodified, String leaseID) {
        BlobRequestConditions cac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(ccAsync.deleteIfExistsWithResponse(cac))
            .verifyError(BlobStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("setAccessPolicyACIllegalSupplier")
    public void deleteIfExistsACIllegal(String match, String noneMatch) {
        BlobRequestConditions mac = new BlobRequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch);

        StepVerifier.create(ccAsync.deleteIfExistsWithResponse(mac))
            .verifyError(UnsupportedOperationException.class);
    }

    @Test
    public void deleteIfExistsOnAContainerThatDoesNotExist() {
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());

        StepVerifier.create(ccAsync.deleteIfExistsWithResponse(new BlobRequestConditions()))
            .assertNext(r -> {
                assertFalse(r.getValue());
                assertResponseStatusCode(r, 404);
            })
            .verifyComplete();
    }

    // We can't guarantee that the requests will always happen before the container is garbage collected
    @PlaybackOnly
    @Test
    public void deleteIfExistsContainerThatWasAlreadyDeleted() {
        StepVerifier.create(ccAsync.deleteIfExists())
            .expectNext(true)
            .verifyComplete();

        // Confirming the behavior of the api when the container is in the deleting state.
        // After deletehas been called once but before it has been garbage collected
        StepVerifier.create(ccAsync.deleteIfExists())
            .expectNext(true)
            .verifyComplete();

        StepVerifier.create(ccAsync.exists())
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    public void listBlockBlobsFlat() {
        String name = generateBlobName();
        BlockBlobAsyncClient bu = ccAsync.getBlobAsyncClient(name).getBlockBlobAsyncClient();
        bu.upload(DATA.getDefaultFlux(), 7).block();

        StepVerifier.create(ccAsync.listBlobs(new ListBlobsOptions().setPrefix(prefix)))
            .assertNext(r -> {
                assertEquals(name, r.getName());
                assertEquals(BlobType.BLOCK_BLOB, r.getProperties().getBlobType());
                assertNull(r.getProperties().getCopyCompletionTime());
                assertNull(r.getProperties().getCopyStatusDescription());
                assertNull(r.getProperties().getCopyId());
                assertNull(r.getProperties().getCopyProgress());
                assertNull(r.getProperties().getCopySource());
                assertNull(r.getProperties().getCopyStatus());
                assertNull(r.getProperties().isIncrementalCopy());
                assertNull(r.getProperties().getDestinationSnapshot());
                assertNull(r.getProperties().getLeaseDuration());
                assertEquals(LeaseStateType.AVAILABLE, r.getProperties().getLeaseState());
                assertEquals(LeaseStatusType.UNLOCKED, r.getProperties().getLeaseStatus());
                assertNotNull(r.getProperties().getContentLength());
                assertNotNull(r.getProperties().getContentType());
                assertNotNull(r.getProperties().getContentMd5());
                assertNull(r.getProperties().getContentEncoding());
                assertNull(r.getProperties().getContentDisposition());
                assertNull(r.getProperties().getContentLanguage());
                assertNull(r.getProperties().getCacheControl());
                assertNull(r.getProperties().getBlobSequenceNumber());
                assertTrue(r.getProperties().isServerEncrypted());
                assertTrue(r.getProperties().isAccessTierInferred());
                assertEquals(AccessTier.HOT, r.getProperties().getAccessTier());
                assertNull(r.getProperties().getArchiveStatus());
                assertNotNull(r.getProperties().getCreationTime());
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void listAppendBlobsFlat() {
        String name = generateBlobName();
        AppendBlobAsyncClient bu = ccAsync.getBlobAsyncClient(name).getAppendBlobAsyncClient();
        bu.create().block();
        bu.seal().block();

        StepVerifier.create(ccAsync.listBlobs(new ListBlobsOptions().setPrefix(prefix)))
            .assertNext(r -> {
                assertEquals(name, r.getName());
                assertEquals(BlobType.APPEND_BLOB, r.getProperties().getBlobType());
                assertNull(r.getProperties().getCopyCompletionTime());
                assertNull(r.getProperties().getCopyStatusDescription());
                assertNull(r.getProperties().getCopyId());
                assertNull(r.getProperties().getCopyProgress());
                assertNull(r.getProperties().getCopySource());
                assertNull(r.getProperties().getCopyStatus());
                assertNull(r.getProperties().isIncrementalCopy());
                assertNull(r.getProperties().getDestinationSnapshot());
                assertNull(r.getProperties().getLeaseDuration());
                assertEquals(LeaseStateType.AVAILABLE, r.getProperties().getLeaseState());
                assertEquals(LeaseStatusType.UNLOCKED, r.getProperties().getLeaseStatus());
                assertNotNull(r.getProperties().getContentLength());
                assertNotNull(r.getProperties().getContentType());
                assertNull(r.getProperties().getContentMd5());
                assertNull(r.getProperties().getContentEncoding());
                assertNull(r.getProperties().getContentDisposition());
                assertNull(r.getProperties().getContentLanguage());
                assertNull(r.getProperties().getCacheControl());
                assertNull(r.getProperties().getBlobSequenceNumber());
                assertTrue(r.getProperties().isServerEncrypted());
                assertNull(r.getProperties().isAccessTierInferred());
                assertNull(r.getProperties().getAccessTier());
                assertNull(r.getProperties().getArchiveStatus());
                assertNotNull(r.getProperties().getCreationTime());
                assertTrue(r.getProperties().isSealed());
            })
            .verifyComplete();
    }

    @Test
    public void listPageBlobsFlat() {
        ccAsync = premiumBlobServiceAsyncClient.getBlobContainerAsyncClient(containerName);
        ccAsync.createIfNotExists().block();
        String name = generateBlobName();
        PageBlobAsyncClient bu = ccAsync.getBlobAsyncClient(name).getPageBlobAsyncClient();
        bu.create(512).block();

        StepVerifier.create(ccAsync.listBlobs(new ListBlobsOptions().setPrefix(prefix), null))
            .assertNext(r -> {
                assertEquals(name, r.getName());
                assertEquals(BlobType.PAGE_BLOB, r.getProperties().getBlobType());
                assertNull(r.getProperties().getCopyCompletionTime());
                assertNull(r.getProperties().getCopyStatusDescription());
                assertNull(r.getProperties().getCopyId());
                assertNull(r.getProperties().getCopyProgress());
                assertNull(r.getProperties().getCopySource());
                assertNull(r.getProperties().getCopyStatus());
                assertNull(r.getProperties().isIncrementalCopy());
                assertNull(r.getProperties().getDestinationSnapshot());
                assertNull(r.getProperties().getLeaseDuration());
                assertEquals(LeaseStateType.AVAILABLE, r.getProperties().getLeaseState());
                assertEquals(LeaseStatusType.UNLOCKED, r.getProperties().getLeaseStatus());
                assertNotNull(r.getProperties().getContentLength());
                assertNotNull(r.getProperties().getContentType());
                assertNull(r.getProperties().getContentMd5());
                assertNull(r.getProperties().getContentEncoding());
                assertNull(r.getProperties().getContentDisposition());
                assertNull(r.getProperties().getContentLanguage());
                assertNull(r.getProperties().getCacheControl());
                assertEquals(0, r.getProperties().getBlobSequenceNumber());
                assertTrue(r.getProperties().isServerEncrypted());
                assertTrue(r.getProperties().isAccessTierInferred());
                assertEquals(AccessTier.P10, r.getProperties().getAccessTier());
                assertNull(r.getProperties().getArchiveStatus());
                assertNotNull(r.getProperties().getCreationTime());
            })
            .verifyComplete();

        // cleanup:
        ccAsync.delete().block();
    }

    @Test
    public void listBlobsFlatMin() {
        StepVerifier.create(ccAsync.listBlobs())
            .verifyComplete();
    }

    private String setupListBlobsTest(String normalName, String copyName, String metadataName, String tagsName,
                                      String uncommittedName) {
        PageBlobAsyncClient normal = ccAsync.getBlobAsyncClient(normalName).getPageBlobAsyncClient();
        normal.create(512).block();

        PageBlobAsyncClient copyBlob = ccAsync.getBlobAsyncClient(copyName).getPageBlobAsyncClient();
        PollerFlux<BlobCopyInfo, Void> poller = setPlaybackPollerFluxPollInterval(copyBlob.beginCopy(normal.getBlobUrl(),
            null));
        poller.blockLast();

        PageBlobAsyncClient metadataBlob = ccAsync.getBlobAsyncClient(metadataName).getPageBlobAsyncClient();
        Map<String, String> metadata = new HashMap<>();
        metadata.put("foo", "bar");
        metadataBlob.createWithResponse(512, null, null, metadata, null).block();

        PageBlobAsyncClient tagsBlob = ccAsync.getBlobAsyncClient(tagsName).getPageBlobAsyncClient();
        Map<String, String> tags = new HashMap<>();
        tags.put(tagKey, tagValue);
        tagsBlob.createWithResponse(new PageBlobCreateOptions(512).setTags(tags)).block();

        BlockBlobAsyncClient uncommittedBlob = ccAsync.getBlobAsyncClient(uncommittedName).getBlockBlobAsyncClient();
        uncommittedBlob.stageBlock(getBlockID(), DATA.getDefaultFlux(), DATA.getDefaultData().remaining()).block();

        return normal.createSnapshot().block().getSnapshotId();
    }

    @Test
    public void listBlobsFlatOptionsCopy() {
        ListBlobsOptions options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveCopy(true));
        String normalName = "a" + generateBlobName();
        String copyName = "c" + generateBlobName();
        String metadataName = "m" + generateBlobName();
        String tagsName = "t" + generateBlobName();
        String uncommittedName = "u" + generateBlobName();
        setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName);

        StepVerifier.create(ccAsync.listBlobs(options))
            .assertNext(r -> assertEquals(normalName, r.getName()))
            .assertNext(r -> {
                assertEquals(copyName, r.getName());
                assertNotNull(r.getProperties().getCopyId());
                // Comparing the urls isn't reliable because the service may use https.
                assertTrue(r.getProperties().getCopySource().contains(normalName));
                // We waited for the copy to complete.
                assertEquals(CopyStatusType.SUCCESS, r.getProperties().getCopyStatus());
                assertNotNull(r.getProperties().getCopyProgress());
                assertNotNull(r.getProperties().getCopyCompletionTime());
            })
            .expectNextCount(2) //  metadata, tags
            .verifyComplete();
    }

    @Test
    public void listBlobsFlatOptionsMetadata() {
        ListBlobsOptions options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveMetadata(true));
        String normalName = "a" + generateBlobName();
        String copyName = "c" + generateBlobName();
        String metadataName = "m" + generateBlobName();
        String tagsName = "t" + generateBlobName();
        String uncommittedName = "u" + generateBlobName();
        setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName);


        StepVerifier.create(ccAsync.listBlobs(options))
            .assertNext(r -> assertEquals(normalName, r.getName()))
            .assertNext(r -> {
                assertEquals(copyName, r.getName());
                assertNull(r.getProperties().getCopyCompletionTime());
            })
            .assertNext(r -> {
                assertEquals(metadataName, r.getName());
                assertEquals("bar", r.getMetadata().get("foo"));
            })
            .expectNextCount(1) //  tags
            .verifyComplete();
    }

    @PlaybackOnly
    @Test
    public void listBlobsFlatOptionsLastAccessTime() {
        BlockBlobAsyncClient b = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
        b.upload(DATA.getDefaultFlux(), DATA.getDefaultData().remaining()).block();

        StepVerifier.create(ccAsync.listBlobs())
            .assertNext(r -> assertNotNull(r.getProperties().getLastAccessedTime()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void listBlobsFlatOptionsTags() {
        ListBlobsOptions options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveTags(true));
        String normalName = "a" + generateBlobName();
        String copyName = "c" + generateBlobName();
        String metadataName = "m" + generateBlobName();
        String tagsName = "t" + generateBlobName();
        String uncommittedName = "u" + generateBlobName();
        setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName);

        StepVerifier.create(ccAsync.listBlobs(options))
            .assertNext(r -> assertEquals(normalName, r.getName()))
            .assertNext(r -> {
                assertEquals(copyName, r.getName());
                assertNull(r.getProperties().getCopyCompletionTime());
            })
            .assertNext(r -> {
                assertEquals(metadataName, r.getName());
                assertNull(r.getMetadata());
            })
            .assertNext(r -> {
                assertEquals(tagValue, r.getTags().get(tagKey));
                assertEquals(1, r.getProperties().getTagCount());
            })
            .verifyComplete();
    }

    @Test
    public void listBlobsFlatOptionsSnapshots() {
        ListBlobsOptions options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveSnapshots(true));
        String normalName = "a" + generateBlobName();
        String copyName = "c" + generateBlobName();
        String metadataName = "m" + generateBlobName();
        String tagsName = "t" + generateBlobName();
        String uncommittedName = "u" + generateBlobName();
        String snapshotTime = setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName);

        StepVerifier.create(ccAsync.listBlobs(options))
            .assertNext(r -> {
                assertEquals(normalName, r.getName());
                assertEquals(snapshotTime, r.getSnapshot());
            })
            .assertNext(r -> assertEquals(normalName, r.getName()))
            .expectNextCount(3) //copy, metadata, tags
            .verifyComplete();
    }

    @Test
    public void listBlobsFlatOptionsUncommitted() {
        ListBlobsOptions options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveUncommittedBlobs(true));
        String normalName = "a" + generateBlobName();
        String copyName = "c" + generateBlobName();
        String metadataName = "m" + generateBlobName();
        String tagsName = "t" + generateBlobName();
        String uncommittedName = "u" + generateBlobName();
        setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName);

        StepVerifier.create(ccAsync.listBlobs(options))
            .assertNext(r -> assertEquals(normalName, r.getName()))
            .expectNextCount(3)
            .assertNext(r -> assertEquals(uncommittedName, r.getName()))
            .verifyComplete();
    }

    @Test
    public void listBlobsFlatOptionsPrefix() {
        ListBlobsOptions options = new ListBlobsOptions().setPrefix("a");
        String normalName = "a" + generateBlobName();
        String copyName = "c" + generateBlobName();
        String metadataName = "m" + generateBlobName();
        String tagsName = "t" + generateBlobName();
        String uncommittedName = "u" + generateBlobName();
        setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName);


        StepVerifier.create(ccAsync.listBlobs(options))
            .assertNext(r -> assertEquals(normalName, r.getName()))
            .verifyComplete();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void listBlobsFlatOptionsMaxResults() {
        int pageSize = 2;
        ListBlobsOptions options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveCopy(true)
            .setRetrieveSnapshots(true).setRetrieveUncommittedBlobs(true)).setMaxResultsPerPage(pageSize);
        String normalName = "a" + generateBlobName();
        String copyName = "c" + generateBlobName();
        String metadataName = "m" + generateBlobName();
        String tagsName = "t" + generateBlobName();
        String uncommittedName = "u" + generateBlobName();
        setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName);

        // expect: "Get first page of blob listings"
        StepVerifier.create(ccAsync.listBlobs(options).byPage().limitRequest(1))
            .assertNext(it -> assertEquals(pageSize, it.getValue().size()))
            .verifyComplete();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void listBlobsFlatOptionsMaxResultsByPage() {
        int pageSize = 2;
        ListBlobsOptions options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveCopy(true)
            .setRetrieveSnapshots(true).setRetrieveUncommittedBlobs(true));
        String normalName = "a" + generateBlobName();
        String copyName = "c" + generateBlobName();
        String metadataName = "m" + generateBlobName();
        String tagsName = "t" + generateBlobName();
        String uncommittedName = "u" + generateBlobName();
        setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName);

        // expect: "Get first page of blob listings

        StepVerifier.create(ccAsync.listBlobs(options).byPage(pageSize).limitRequest(1))
            .assertNext(it -> assertEquals(pageSize, it.getValue().size()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void listBlobsFlatOptionsDeletedWithVersions() {
        BlobContainerAsyncClient versionedCC = versionedBlobServiceAsyncClient.getBlobContainerAsyncClient(containerName);
        versionedCC.createIfNotExists().block();
        String blobName = generateBlobName();
        AppendBlobAsyncClient blob = versionedCC.getBlobAsyncClient(blobName).getAppendBlobAsyncClient();
        blob.create().block();
        Map<String, String> metadata = new HashMap<>();
        metadata.put("foo", "bar");
        blob.setMetadata(metadata).block();
        blob.delete().block();
        ListBlobsOptions options = new ListBlobsOptions().setPrefix(blobName).setDetails(new BlobListDetails()
            .setRetrieveDeletedBlobsWithVersions(true));

        StepVerifier.create(versionedCC.listBlobs(options))
            .assertNext(r -> {
                assertEquals(blobName, r.getName());
                assertTrue(r.hasVersionsOnly());
            })
            .verifyComplete();

        // cleanup:
        versionedCC.delete().block();
    }

    @Test
    public void listBlobsPrefixWithComma() {
        String prefix = generateBlobName() + ", " + generateBlobName();
        BlockBlobAsyncClient b = ccAsync.getBlobAsyncClient(prefix).getBlockBlobAsyncClient();
        b.upload(DATA.getDefaultFlux(), DATA.getDefaultData().remaining()).block();

        ListBlobsOptions options = new ListBlobsOptions().setPrefix(prefix);
        StepVerifier.create(ccAsync.listBlobs(options))
            .assertNext(r -> assertEquals(prefix, r.getName()))
            .verifyComplete();
    }

    @Test
    public void listBlobsFlatMarker() {
        int numBlobs = 10;
        int pageSize = 6;
        for (int i = 0; i < numBlobs; i++) {
            PageBlobAsyncClient bc = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
            bc.create(512).block();
        }
        // when: "listBlobs with async client"
        PagedFlux<BlobItem> pagedFlux = ccAsync.listBlobs(new ListBlobsOptions().setMaxResultsPerPage(pageSize));
        PagedResponse<BlobItem> pagedResponse1 = pagedFlux.byPage().blockFirst();
        assertNotNull(pagedResponse1);
        PagedResponse<BlobItem> pagedResponse2 = pagedFlux.byPage(pagedResponse1.getContinuationToken()).blockFirst();

        assertEquals(pageSize, pagedResponse1.getValue().size());
        assertNotNull(pagedResponse2);
        assertEquals(numBlobs - pageSize, pagedResponse2.getValue().size());
        assertNull(pagedResponse2.getContinuationToken());
    }

    @Test
    public void listBlobsFlatMarkerOverload() {
        int numBlobs = 10;
        int pageSize = 6;
        for (int i = 0; i < numBlobs; i++) {
            PageBlobAsyncClient bc = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
            bc.create(512).block();
        }
        // when: "listBlobs with async client"
        PagedFlux<BlobItem> pagedFlux = ccAsync.listBlobs(new ListBlobsOptions().setMaxResultsPerPage(pageSize));
        PagedResponse<BlobItem> pagedResponse1 = pagedFlux.byPage().blockFirst();
        assertNotNull(pagedResponse1);
        pagedFlux = ccAsync.listBlobs(new ListBlobsOptions().setMaxResultsPerPage(pageSize),
            pagedResponse1.getContinuationToken());
        PagedResponse<BlobItem> pagedResponse2 = pagedFlux.byPage().blockFirst();

        assertEquals(pageSize, pagedResponse1.getValue().size());
        assertNotNull(pagedResponse2);
        assertEquals(numBlobs - pageSize, pagedResponse2.getValue().size());
        assertNull(pagedResponse2.getContinuationToken());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("listBlobsFlatRehydratePrioritySupplier")
    public void listBlobsFlatRehydratePriority(RehydratePriority rehydratePriority) {
        String name = generateBlobName();
        BlockBlobAsyncClient bc = ccAsync.getBlobAsyncClient(name).getBlockBlobAsyncClient();
        bc.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();

        if (rehydratePriority != null) {
            bc.setAccessTier(AccessTier.ARCHIVE).block();
            bc.setAccessTierWithResponse(new BlobSetAccessTierOptions(AccessTier.HOT)
                .setPriority(rehydratePriority)).block();
        }
        StepVerifier.create(ccAsync.listBlobs())
            .assertNext(r -> assertEquals(rehydratePriority, r.getProperties().getRehydratePriority()))
            .verifyComplete();
    }

    private static Stream<Arguments> listBlobsFlatRehydratePrioritySupplier() {
        return Stream.of(
            Arguments.of((RehydratePriority) null),
            Arguments.of(RehydratePriority.STANDARD),
            Arguments.of(RehydratePriority.HIGH));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-02-12")
    @Test
    public void listBlobsFlatInvalidXml() {
        String blobName = "dir1/dir2/file\uFFFE.blob";
        ccAsync.getBlobAsyncClient(blobName).getAppendBlobAsyncClient().create().block();

        StepVerifier.create(ccAsync.listBlobs())
            .assertNext(r -> assertEquals(blobName, r.getName()))
            .verifyComplete();
    }

    @Test
    public void listBlobsFlatError() {
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());
        StepVerifier.create(ccAsync.listBlobs())
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void listBlobsFlatWithTimeoutStillBackedByPagedFlux() {
        int numBlobs = 5;
        int pageResults = 3;

        for (int i = 0; i < numBlobs; i++) {
            BlockBlobAsyncClient blob = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
            blob.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();
        }

        // when: "Consume results by page, then still have paging functionality"
        StepVerifier.create(ccAsync.listBlobs(new ListBlobsOptions().setMaxResultsPerPage(pageResults)).byPage())
            .expectNextCount(2)
            .verifyComplete();
    }

    @Test
    public void listBlobsHierWithTimeoutStillBackedByPagedFlux() {
        int numBlobs = 5;
        int pageResults = 3;

        for (int i = 0; i < numBlobs; i++) {
            BlockBlobAsyncClient blob = ccAsync.getBlobAsyncClient(generateBlobName()).getBlockBlobAsyncClient();
            blob.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();
        }

        // when: "Consume results by page, then still have paging functionality"
        StepVerifier.create(ccAsync.listBlobsByHierarchy("/", new ListBlobsOptions()
            .setMaxResultsPerPage(pageResults)).byPage())
            .expectNextCount(2)
            .verifyComplete();
    }

    /*
    This test requires two accounts that are configured in a very specific way. It is not feasible to setup that
    relationship programmatically, so we have recorded a successful interaction and only test recordings.
    */
    //@PlaybackOnly
    @Disabled("Need to re-record once account is setup properly.")
    @Test
    public void listBlobsFlatORS() {
        BlobContainerAsyncClient sourceContainer = primaryBlobServiceAsyncClient
            .getBlobContainerAsyncClient("test1");
        BlobContainerAsyncClient destContainer = alternateBlobServiceAsyncClient
            .getBlobContainerAsyncClient("test2");

        List<BlobItem> sourceBlobs = sourceContainer.listBlobs().collect(Collectors.toList()).block();
        List<BlobItem> destBlobs = destContainer.listBlobs().collect(Collectors.toList()).block();

        int i = 0;
        for (BlobItem blob : sourceBlobs) {
            if (i == 1) {
                assertNull(blob.getObjectReplicationSourcePolicies());
            } else {
                assertTrue(validateOR(
                    blob.getObjectReplicationSourcePolicies()
                ));
            }
            i++;
        }

        /* Service specifies no ors metadata on the dest blobs. */
        for (BlobItem blob : destBlobs) {
            assertNull(blob.getObjectReplicationSourcePolicies());
        }
    }

    private boolean validateOR(List<ObjectReplicationPolicy> policies) {
        return policies.stream()
            .filter(policy -> "fd2da1b9-56f5-45ff-9eb6-310e6dfc2c80".equals(policy.getPolicyId()))
            .findFirst()
            .get()
            .getRules()
            .stream()
            .filter(rule -> "105f9aad-f39b-4064-8e47-ccd7937295ca".equals(rule.getRuleId()))
            .findFirst()
            .get()
            .getStatus() == ObjectReplicationStatus.COMPLETE;
    }

    @Test
    public void listBlobsHierarchy() {
        String name = generateBlobName();
        PageBlobAsyncClient bu = ccAsync.getBlobAsyncClient(name).getPageBlobAsyncClient();
        bu.create(512).block();

        StepVerifier.create(ccAsync.listBlobsByHierarchy(null))
            .assertNext(r -> assertEquals(name, r.getName()))
            .verifyComplete();
    }

    @Test
    public void listBlobsHierarchyMin() {
        StepVerifier.create(ccAsync.listBlobsByHierarchy("/"))
            .verifyComplete();
    }

    @Test
    public void listBlobsHierOptionsCopy() {
        ListBlobsOptions options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveCopy(true));
        String normalName = "a" + generateBlobName();
        String copyName = "c" + generateBlobName();
        String metadataName = "m" + generateBlobName();
        String tagsName = "t" + generateBlobName();
        String uncommittedName = "u" + generateBlobName();
        setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName);

        StepVerifier.create(ccAsync.listBlobsByHierarchy("", options))
            .assertNext(r -> assertEquals(normalName, r.getName()))
            .assertNext(r -> {
                assertEquals(copyName, r.getName());
                assertNotNull(r.getProperties().getCopyId());
                // Comparing the urls isn't reliable because the service may use https.
                assertTrue(r.getProperties().getCopySource().contains(normalName));
                assertEquals(CopyStatusType.SUCCESS, r.getProperties().getCopyStatus()); // We waited for the copy to complete.
                assertNotNull(r.getProperties().getCopyProgress());
                assertNotNull(r.getProperties().getCopyCompletionTime());
            })
            .expectNextCount(2) //metadata, tags
            .verifyComplete();
    }

    @Test
    public void listBlobsHierOptionsMetadata() {
        ListBlobsOptions options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveMetadata(true));
        String normalName = "a" + generateBlobName();
        String copyName = "c" + generateBlobName();
        String metadataName = "m" + generateBlobName();
        String tagsName = "t" + generateBlobName();
        String uncommittedName = "u" + generateBlobName();
        setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName);

        StepVerifier.create(ccAsync.listBlobsByHierarchy("", options))
            .assertNext(r -> assertEquals(normalName, r.getName()))
            .assertNext(r -> {
                assertEquals(copyName, r.getName());
                assertNull(r.getProperties().getCopyCompletionTime());
            })
            .assertNext(r -> {
                assertEquals(metadataName, r.getName());
                assertEquals("bar", r.getMetadata().get("foo"));
            })
            .expectNextCount(1)
            .verifyComplete(); //tags
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void listBlobsHierOptionsTags() {
        ListBlobsOptions options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveTags(true));
        String normalName = "a" + generateBlobName();
        String copyName = "c" + generateBlobName();
        String metadataName = "m" + generateBlobName();
        String tagsName = "t" + generateBlobName();
        String uncommittedName = "u" + generateBlobName();
        setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName);

        StepVerifier.create(ccAsync.listBlobsByHierarchy("", options))
            .assertNext(r -> assertEquals(normalName, r.getName()))
            .assertNext(r -> {
                assertEquals(copyName, r.getName());
                assertNull(r.getProperties().getCopyCompletionTime());
            })
            .assertNext(r -> {
                assertEquals(metadataName, r.getName());
                assertNull(r.getMetadata());
            })
            .assertNext(r -> {
                assertEquals(tagValue, r.getTags().get(tagKey));
                assertEquals(1, r.getProperties().getTagCount());
            })
            .verifyComplete();
    }

    @Test
    public void listBlobsHierOptionsUncommitted() {
        ListBlobsOptions options = new ListBlobsOptions().setDetails(
            new BlobListDetails().setRetrieveUncommittedBlobs(true));
        String normalName = "a" + generateBlobName();
        String copyName = "c" + generateBlobName();
        String metadataName = "m" + generateBlobName();
        String tagsName = "t" + generateBlobName();
        String uncommittedName = "u" + generateBlobName();
        setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName);

        StepVerifier.create(ccAsync.listBlobsByHierarchy("", options))
            .assertNext(r -> assertEquals(normalName, r.getName()))
            .expectNextCount(3)
            .assertNext(r -> assertEquals(uncommittedName, r.getName()))
            .verifyComplete();
    }

    @Test
    public void listBlobsHierOptionsprefix() {
        ListBlobsOptions options = new ListBlobsOptions().setPrefix("a");
        String normalName = "a" + generateBlobName();
        String copyName = "c" + generateBlobName();
        String metadataName = "m" + generateBlobName();
        String tagsName = "t" + generateBlobName();
        String uncommittedName = "u" + generateBlobName();
        setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName);

        StepVerifier.create(ccAsync.listBlobsByHierarchy("", options))
            .assertNext(r -> assertEquals(normalName, r.getName()))
            .verifyComplete();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void listBlobsHierOptionsmaxResults() {
        ListBlobsOptions options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveCopy(true)
            .setRetrieveUncommittedBlobs(true)).setMaxResultsPerPage(1);
        String normalName = "a" + generateBlobName();
        String copyName = "c" + generateBlobName();
        String metadataName = "m" + generateBlobName();
        String tagsName = "t" + generateBlobName();
        String uncommittedName = "u" + generateBlobName();
        setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName);

        StepVerifier.create(ccAsync.listBlobsByHierarchy("", options).byPage().limitRequest(1))
            .assertNext(it -> assertEquals(1, it.getValue().size()))
            .verifyComplete();
    }

    @Test
    public void listBlobsHierOptionsMaxResultsByPage() {
        ListBlobsOptions options = new ListBlobsOptions().setDetails(new BlobListDetails().setRetrieveCopy(true)
            .setRetrieveUncommittedBlobs(true));
        String normalName = "a" + generateBlobName();
        String copyName = "c" + generateBlobName();
        String metadataName = "m" + generateBlobName();
        String tagsName = "t" + generateBlobName();
        String uncommittedName = "u" + generateBlobName();
        setupListBlobsTest(normalName, copyName, metadataName, tagsName, uncommittedName);

        StepVerifier.create(ccAsync.listBlobsByHierarchy("", options).byPage(1))
            .thenConsumeWhile(r -> {
                assertEquals(1, r.getValue().size());
                return true;
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void listBlobsHierOptionsDeletedWithVersions() {
        BlobContainerAsyncClient versionedCC = versionedBlobServiceAsyncClient.getBlobContainerAsyncClient(containerName);
        versionedCC.createIfNotExists().block();
        String blobName = generateBlobName();
        AppendBlobAsyncClient blob = versionedCC.getBlobAsyncClient(blobName).getAppendBlobAsyncClient();
        blob.create().block();
        Map<String, String> metadata = new HashMap<>();
        metadata.put("foo", "bar");
        blob.setMetadata(metadata).block();
        blob.delete().block();
        ListBlobsOptions options = new ListBlobsOptions().setPrefix(blobName)
            .setDetails(new BlobListDetails().setRetrieveDeletedBlobsWithVersions(true));

        StepVerifier.create(versionedCC.listBlobsByHierarchy("", options))
            .assertNext(r -> {
                assertEquals(blobName, r.getName());
                assertTrue(r.hasVersionsOnly());
            })
            .verifyComplete();

        // cleanup:
        versionedCC.delete().block();
    }

    @Test
    public void listBlobsHierOptionsFail() {
        ListBlobsOptions options = new ListBlobsOptions()
            .setDetails(new BlobListDetails().setRetrieveSnapshots(true))
            .setMaxResultsPerPage(5);

        StepVerifier.create(ccAsync.listBlobsByHierarchy(null, options))
            .verifyError(UnsupportedOperationException.class);
    }

    @Test
    public void listBlobsHierDelim() {
        List<String> blobNames = Arrays.asList("a", "b/a", "c", "d/a", "e", "f", "g/a");
        for (String blobName : blobNames) {
            AppendBlobAsyncClient bu = ccAsync.getBlobAsyncClient(blobName).getAppendBlobAsyncClient();
            bu.create().block();
        }

        Set<String> foundBlobs = new HashSet<>();
        Set<String> foundPrefixes = new HashSet<>();
        ccAsync.listBlobsByHierarchy(null).collect(Collectors.toList()).block()
            .forEach(blobItem -> {
                if (blobItem.isPrefix()) {
                    foundPrefixes.add(blobItem.getName());
                } else {
                    foundBlobs.add(blobItem.getName());
                }
            });

        List<String> expectedBlobs = Arrays.asList("a", "c", "e", "f");
        List<String> expectedPrefixes = Arrays.asList("b/", "d/", "g/");

        for (String blobName : expectedBlobs) {
            assertTrue(foundBlobs.contains(blobName));
        }

        for (String prefix : expectedPrefixes) {
            assertTrue(foundPrefixes.contains(prefix));
        }
    }

    @Test
    public void listBlobsHierMarker() {
        int numBlobs = 10;
        int pageSize = 6;
        for (int i = 0; i < numBlobs; i++) {
            PageBlobAsyncClient bc = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
            bc.create(512).block();
        }

        StepVerifier.create(ccAsync.listBlobsByHierarchy("/", new ListBlobsOptions()
                .setMaxResultsPerPage(pageSize)).byPage())
            .assertNext(r -> {
                assertEquals(pageSize, r.getValue().size());
                assertNotNull(r.getContinuationToken());
            })
            .assertNext(r -> {
                assertEquals(r.getValue().size(), numBlobs - pageSize);
                assertNull(r.getContinuationToken());
            })
            .verifyComplete();
    }

    /*
    This test requires two accounts that are configured in a very specific way. It is not feasible to setup that
    relationship programmatically, so we have recorded a successful interaction and only test recordings.
    */
    @PlaybackOnly
    @Test
    public void listBlobsHierORS() {
        BlobContainerAsyncClient sourceContainer = primaryBlobServiceAsyncClient
            .getBlobContainerAsyncClient("test1");
        BlobContainerAsyncClient destContainer = alternateBlobServiceAsyncClient
            .getBlobContainerAsyncClient("test2");

        List<BlobItem> sourceBlobs = sourceContainer.listBlobsByHierarchy("/").collect(Collectors.toList()).block();
        List<BlobItem> destBlobs = destContainer.listBlobsByHierarchy("/").collect(Collectors.toList()).block();

        int i = 0;
        for (BlobItem blob : sourceBlobs) {
            if (i == 1) {
                assertNull(blob.getObjectReplicationSourcePolicies());
            } else {
                assertTrue(validateOR(blob.getObjectReplicationSourcePolicies()
                ));
            }
            i++;
        }

        /* Service specifies no ors metadata on the dest blobs. */
        for (BlobItem blob : destBlobs) {
            assertNull(blob.getObjectReplicationSourcePolicies());
        }
    }

    @Test
    public void listBlobsFlatSimple() {
        // setup: "Create 10 page blobs in the container"
        int numBlobs = 10;
        int pageSize = 3;
        for (int i = 0; i < numBlobs; i++) {
            PageBlobAsyncClient bc = ccAsync.getBlobAsyncClient(generateBlobName()).getPageBlobAsyncClient();
            bc.create(512).block();
        }

        // expect: "listing operation will fetch all 10 blobs, despite page size being smaller than 10"
        StepVerifier.create(ccAsync.listBlobs(new ListBlobsOptions().setMaxResultsPerPage(pageSize)))
            .expectNextCount(numBlobs)
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("listBlobsFlatRehydratePrioritySupplier")
    public void listBlobsHierRehydratePriority(RehydratePriority rehydratePriority) {
        String name = generateBlobName();
        BlockBlobAsyncClient bc = ccAsync.getBlobAsyncClient(name).getBlockBlobAsyncClient();
        bc.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();

        if (rehydratePriority != null) {
            bc.setAccessTier(AccessTier.ARCHIVE).block();
            bc.setAccessTierWithResponse(new BlobSetAccessTierOptions(AccessTier.HOT)
                .setPriority(rehydratePriority)).block();
        }

        StepVerifier.create(ccAsync.listBlobsByHierarchy(null))
            .assertNext(r -> assertEquals(rehydratePriority, r.getProperties().getRehydratePriority()))
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void listAppendBlobsHier() {
        String name = generateBlobName();
        AppendBlobAsyncClient bu = ccAsync.getBlobAsyncClient(name).getAppendBlobAsyncClient();
        bu.create().block();
        bu.seal().block();

        StepVerifier.create(ccAsync.listBlobsByHierarchy(null, new ListBlobsOptions().setPrefix(prefix)))
            .assertNext(r -> {
                assertEquals(name, r.getName());
                assertEquals(BlobType.APPEND_BLOB, r.getProperties().getBlobType());
                assertNull(r.getProperties().getCopyCompletionTime());
                assertNull(r.getProperties().getCopyStatusDescription());
                assertNull(r.getProperties().getCopyId());
                assertNull(r.getProperties().getCopyProgress());
                assertNull(r.getProperties().getCopySource());
                assertNull(r.getProperties().getCopyStatus());
                assertNull(r.getProperties().isIncrementalCopy());
                assertNull(r.getProperties().getDestinationSnapshot());
                assertNull(r.getProperties().getLeaseDuration());
                assertEquals(LeaseStateType.AVAILABLE, r.getProperties().getLeaseState());
                assertEquals(LeaseStatusType.UNLOCKED, r.getProperties().getLeaseStatus());
                assertNotNull(r.getProperties().getContentLength());
                assertNotNull(r.getProperties().getContentType());
                assertNull(r.getProperties().getContentMd5());
                assertNull(r.getProperties().getContentEncoding());
                assertNull(r.getProperties().getContentDisposition());
                assertNull(r.getProperties().getContentLanguage());
                assertNull(r.getProperties().getCacheControl());
                assertNull(r.getProperties().getBlobSequenceNumber());
                assertTrue(r.getProperties().isServerEncrypted());
                assertNull(r.getProperties().isAccessTierInferred());
                assertNull(r.getProperties().getAccessTier());
                assertNull(r.getProperties().getArchiveStatus());
                assertNotNull(r.getProperties().getCreationTime());
                assertTrue(r.getProperties().isSealed());
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-02-12")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void listBlobsHierInvalidXml(boolean delimiter) {
        String blobName = "dir1/dir2/file\uFFFE.blob";
        ccAsync.getBlobAsyncClient(blobName).getAppendBlobAsyncClient().create().block();

        if (!delimiter) {
            StepVerifier.create(ccAsync.listBlobsByHierarchy("", null))
                .assertNext(r -> {
                    assertEquals(r.getName(), blobName);
                    assertEquals(false, r.isPrefix());
                })
                .verifyComplete();
        } else {
            StepVerifier.create(ccAsync.listBlobsByHierarchy(".b", null))
                .assertNext(r -> {
                    assertEquals(r.getName(), "dir1/dir2/file\uFFFE.b");
                    assertEquals(true, r.isPrefix());
                })
                .verifyComplete();
        }
    }

    @Test
    public void listBlobsHierError() {
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());
        StepVerifier.create(ccAsync.listBlobsByHierarchy("."))
            .verifyError(BlobStorageException.class);
    }

    private void setupContainerForListing(BlobContainerAsyncClient containerClient) {
        List<String> blobNames = Arrays.asList("foo", "bar", "baz", "foo/foo", "foo/bar", "baz/foo", "baz/foo/bar",
            "baz/bar/foo");

        for (String blob : blobNames) {
            BlockBlobAsyncClient blockBlobClient = containerClient.getBlobAsyncClient(blob).getBlockBlobAsyncClient();
            blockBlobClient.upload(DATA.getDefaultFlux(), DATA.getDefaultDataSize()).block();
        }
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-12-02")
    @Test
    public void listBlobsHierSegmentWithVersionPrefixAndDelimiter() {
        BlobContainerAsyncClient versionedCC = versionedBlobServiceAsyncClient.getBlobContainerAsyncClient(containerName);
        versionedCC.createIfNotExists().block();
        ListBlobsOptions options = new ListBlobsOptions()
            .setDetails(new BlobListDetails().setRetrieveVersions(true))
            .setPrefix("baz");

        setupContainerForListing(versionedCC);

        Set<BlobItem> foundBlobs = new HashSet<>();
        Set<BlobItem> foundPrefixes = new HashSet<>();

        versionedCC.listBlobsByHierarchy("/", options).collect(Collectors.toList()).block()
            .forEach(blobItem -> {
                if (blobItem.isPrefix()) {
                    foundPrefixes.add(blobItem);
                } else {
                    foundBlobs.add(blobItem);
                }
            });

        assertEquals(1, foundBlobs.size());
        assertEquals(1, foundPrefixes.size());
        BlobItem first = foundBlobs.iterator().next();
        assertEquals("baz", first.getName());
        assertNotNull(first.getVersionId());
        assertEquals("baz/", foundPrefixes.iterator().next().getName());

        // cleanup:
        versionedCC.delete().block();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-04-10")
    @Test
    public void findBlobsMin() {
        StepVerifier.create(ccAsync.findBlobsByTags("\"key\"='value'"))
            .verifyComplete();
    }

    @SuppressWarnings("deprecation")
    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-04-10")
    @Test
    public void findBlobsQuery() {
        BlobAsyncClient blobClient = ccAsync.getBlobAsyncClient(generateBlobName());
        blobClient.uploadWithResponse(new BlobParallelUploadOptions(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize()).setTags(Collections.singletonMap("key", "value"))).block();
        blobClient = ccAsync.getBlobAsyncClient(generateBlobName());
        blobClient.uploadWithResponse(new BlobParallelUploadOptions(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize()).setTags(Collections.singletonMap("bar", "foo"))).block();
        blobClient = ccAsync.getBlobAsyncClient(generateBlobName());
        blobClient.upload(DATA.getDefaultFlux(), null).block();

        sleepIfRunningAgainstService(10 * 1000); // To allow tags to index

        String query = "\"bar\"='foo'";
        StepVerifier.create(ccAsync.findBlobsByTags(String.format(query, ccAsync.getBlobContainerName())))
            .assertNext(r -> {
                assertEquals(1, r.getTags().size());
                assertEquals("foo", r.getTags().get("bar"));
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-04-10")
    @Test
    public void findBlobsMarker() {
        Map<String, String> tags = Collections.singletonMap(tagKey, tagValue);
        for (int i = 0; i < 10; i++) {
            ccAsync.getBlobAsyncClient(generateBlobName()).uploadWithResponse(
                new BlobParallelUploadOptions(DATA.getDefaultInputStream()).setTags(tags)).block();
        }

        sleepIfRunningAgainstService(10 * 1000); // To allow tags to index

        PagedResponse<TaggedBlobItem> firstPage = ccAsync.findBlobsByTags(
            new FindBlobsOptions(String.format("\"%s\"='%s'", tagKey, tagValue)).setMaxResultsPerPage(5), null,
            Context.NONE).byPage().blockFirst();
        String marker = firstPage.getContinuationToken();
        String firstBlobName = firstPage.getValue().iterator().next().getName();

        PagedResponse<TaggedBlobItem> secondPage = ccAsync.findBlobsByTags(
            new FindBlobsOptions(String.format("\"%s\"='%s'", tagKey, tagValue)).setMaxResultsPerPage(5), null,
            Context.NONE).byPage(marker).blockLast();

        // Assert that the second segment is indeed after the first alphabetically
        assertTrue(firstBlobName.compareTo(secondPage.getValue().iterator().next().getName()) < 0);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-04-10")
    @Test
    public void findBlobsMaxResults() {
        int numBlobs = 7;
        int pageResults = 3;
        Map<String, String> tags = Collections.singletonMap(tagKey, tagValue);

        for (int i = 0; i < numBlobs; i++) {
            ccAsync.getBlobAsyncClient(generateBlobName()).uploadWithResponse(
                new BlobParallelUploadOptions(DATA.getDefaultInputStream()).setTags(tags)).block();
        }

        StepVerifier.create(ccAsync.findBlobsByTags(new FindBlobsOptions(String.format("\"%s\"='%s'", tagKey, tagValue))
            .setMaxResultsPerPage(pageResults), null, Context.NONE).byPage())
            .thenConsumeWhile(r -> {
                assertTrue(r.getValue().size() <= pageResults);
                return true;
            })
            .verifyComplete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-04-10")
    @Test
    public void findBlobsMaxResultsByPage() {
        int numBlobs = 7;
        int pageResults = 3;
        Map<String, String> tags = Collections.singletonMap(tagKey, tagValue);

        for (int i = 0; i < numBlobs; i++) {
            ccAsync.getBlobAsyncClient(generateBlobName()).uploadWithResponse(
                new BlobParallelUploadOptions(DATA.getDefaultInputStream()).setTags(tags)).block();
        }

        StepVerifier.create(ccAsync.findBlobsByTags(new FindBlobsOptions(String.format("\"%s\"='%s'",
            tagKey, tagValue))).byPage(pageResults))
            .thenConsumeWhile(r -> {
                assertTrue(r.getValue().size() <= pageResults);
                return true;
            })
            .verifyComplete();
    }

    @Test
    public void findBlobsError() {
        StepVerifier.create(ccAsync.findBlobsByTags("garbageTag").byPage())
            .verifyError(BlobStorageException.class);
    }

    @SuppressWarnings("deprecation")
    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-04-10")
    @Test
    public void findBlobsWithTimeoutStillBackedByPagedFlux() {
        int numBlobs = 5;
        int pageResults = 3;
        Map<String, String> tags = Collections.singletonMap(tagKey, tagValue);

        for (int i = 0; i < numBlobs; i++) {
            ccAsync.getBlobAsyncClient(generateBlobName()).uploadWithResponse(
                new BlobParallelUploadOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSize()).setTags(tags)).block();
        }

        // when: "Consume results by page, still have paging functionality"
        StepVerifier.create(ccAsync.findBlobsByTags(new FindBlobsOptions(String.format("\"%s\"='%s'", tagKey, tagValue))
            .setMaxResultsPerPage(pageResults), Duration.ofSeconds(10), Context.NONE).byPage().count())
            .expectNextCount(1)
            .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(strings = {"中文",
        "az[]",
        "hello world",
        "hello/world",
        "hello&world",
        "!*'();:@&=+/$,/?#[]"
    })
    public void createURLSpecialChars(String name) {
        // This test checks that we encode special characters in blob names correctly.
        AppendBlobAsyncClient bu2 = ccAsync.getBlobAsyncClient(name).getAppendBlobAsyncClient();
        PageBlobAsyncClient bu3 = ccAsync.getBlobAsyncClient(name + "2").getPageBlobAsyncClient();
        BlockBlobAsyncClient bu4 = ccAsync.getBlobAsyncClient(name + "3").getBlockBlobAsyncClient();
        BlockBlobAsyncClient bu5 = ccAsync.getBlobAsyncClient(name).getBlockBlobAsyncClient();

        assertAsyncResponseStatusCode(bu2.createWithResponse(null, null, null),
            201);
        assertAsyncResponseStatusCode(bu5.getPropertiesWithResponse(null), 200);
        assertAsyncResponseStatusCode(bu3.createWithResponse(512, null, null, null,
            null), 201);
        assertAsyncResponseStatusCode(bu4.uploadWithResponse(DATA.getDefaultFlux(), DATA.getDefaultDataSize(),
            null, null, null, null, null), 201);

        StepVerifier.create(ccAsync.listBlobs())
            .assertNext(r -> assertEquals(name, r.getName()))
            .assertNext(r -> assertEquals(name + "2", r.getName()))
            .assertNext(r -> assertEquals(name + "3", r.getName()))
            .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "%E4%B8%AD%E6%96%87",
        "az%5B%5D",
        "hello%20world",
        "hello%2Fworld",
        "hello%26world",
        "%21%2A%27%28%29%3B%3A%40%26%3D%2B%24%2C%2F%3F%23%5B%5D"
    })
    public void createURLSpecialCharsEncoded(String name) {
        // This test checks that we handle blob names with encoded special characters correctly.
        AppendBlobAsyncClient bu2 = ccAsync.getBlobAsyncClient(name).getAppendBlobAsyncClient();
        PageBlobAsyncClient bu3 = ccAsync.getBlobAsyncClient(name + "2").getPageBlobAsyncClient();
        BlockBlobAsyncClient bu4 = ccAsync.getBlobAsyncClient(name + "3").getBlockBlobAsyncClient();
        BlockBlobAsyncClient bu5 = ccAsync.getBlobAsyncClient(name).getBlockBlobAsyncClient();

        assertAsyncResponseStatusCode(bu2.createWithResponse(null, null, null),
            201);
        assertAsyncResponseStatusCode(bu5.getPropertiesWithResponse(null), 200);
        assertAsyncResponseStatusCode(bu3.createWithResponse(512, null, null, null,
            null), 201);
        assertAsyncResponseStatusCode(bu4.uploadWithResponse(DATA.getDefaultFlux(), DATA.getDefaultDataSize(),
            null, null, null, null, null), 201);

        StepVerifier.create(ccAsync.listBlobs())
            .assertNext(r -> assertEquals(name, r.getName()))
            .assertNext(r -> assertEquals(name + "2", r.getName()))
            .assertNext(r -> assertEquals(name + "3", r.getName()))
            .verifyComplete();
    }

    @Test
    public void rootExplicit() {
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(BlobContainerClient.ROOT_CONTAINER_NAME);
        // create root container if not exist.
        if (!ccAsync.exists().block()) {
            ccAsync.create().block();
        }
        AppendBlobAsyncClient bu = ccAsync.getBlobAsyncClient("rootblob").getAppendBlobAsyncClient();
        assertAsyncResponseStatusCode(bu.createWithResponse(null, null, null),
            201);
    }

    @Test
    public void rootExplicitInEndpoint() {
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(BlobContainerClient.ROOT_CONTAINER_NAME);
        // create root container if not exist.
        if (!ccAsync.exists().block()) {
            ccAsync.create().block();
        }
        AppendBlobAsyncClient bu = ccAsync.getBlobAsyncClient("rootblob").getAppendBlobAsyncClient();

        assertAsyncResponseStatusCode(bu.createWithResponse(null, null, null),
            201);
        StepVerifier.create(bu.getPropertiesWithResponse(null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 200);
                assertEquals(BlobType.APPEND_BLOB, r.getValue().getBlobType());
            })
            .verifyComplete();
    }

    @Test
    public void blobClientBuilderRootImplicit() {
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(BlobContainerClient.ROOT_CONTAINER_NAME);
        // createroot container if not exist.
        if (!ccAsync.exists().block()) {
            ccAsync.create().block();
        }

        AppendBlobAsyncClient bc = instrument(new BlobClientBuilder()
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential())
            .endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint())
            .blobName("rootblob"))
            .buildAsyncClient().getAppendBlobAsyncClient();

        assertAsyncResponseStatusCode(bc.createWithResponse(null, null, null),
            201);

        StepVerifier.create(bc.getPropertiesWithResponse(null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 200);
                assertEquals(BlobType.APPEND_BLOB, r.getValue().getBlobType());
            })
            .verifyComplete();
    }

    @Test
    public void containerClientBuilderRootImplicit() {
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(BlobContainerClient.ROOT_CONTAINER_NAME);
        // create root container if not exist.
        if (!ccAsync.exists().block()) {
            ccAsync.create().block();
        }

        ccAsync = instrument(new BlobContainerClientBuilder()
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential())
            .endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint())
            .containerName(null))
            .buildAsyncClient();

        StepVerifier.create(ccAsync.getProperties())
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();

        assertEquals(BlobContainerAsyncClient.ROOT_CONTAINER_NAME, ccAsync.getBlobContainerName());

        AppendBlobAsyncClient bc = ccAsync.getBlobAsyncClient("rootblob").getAppendBlobAsyncClient();
        bc.create(true).block();

        StepVerifier.create(bc.exists())
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void serviceClientImplicitRoot() {
        assertEquals(primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(null).getBlobContainerName(),
            BlobContainerAsyncClient.ROOT_CONTAINER_NAME);
        assertEquals(primaryBlobServiceAsyncClient.getBlobContainerAsyncClient("").getBlobContainerName(),
            BlobContainerAsyncClient.ROOT_CONTAINER_NAME);
    }

    @Test
    public void webContainer() {
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(BlobContainerClient.STATIC_WEBSITE_CONTAINER_NAME);
        // createroot container if not exist.
        try {
            ccAsync.create().block();
        } catch (BlobStorageException se) {
            if (se.getErrorCode() != BlobErrorCode.CONTAINER_ALREADY_EXISTS) {
                throw se;
            }
        }

        BlobContainerAsyncClient webContainer = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(
            BlobContainerClient.STATIC_WEBSITE_CONTAINER_NAME);

        // Validate some basic operation.
        StepVerifier.create(webContainer.setAccessPolicy(null, null))
            .verifyComplete();
    }

    @Test
    public void getAccountInfo() {
        StepVerifier.create(premiumBlobServiceAsyncClient.getAccountInfoWithResponse())
            .assertNext(r -> {
                assertNotNull(r.getHeaders().getValue(HttpHeaderName.DATE));
                assertNotNull(r.getHeaders().getValue(X_MS_VERSION));
                assertNotNull(r.getHeaders().getValue(X_MS_REQUEST_ID));
                assertNotNull(r.getValue().getAccountKind());
                assertNotNull(r.getValue().getSkuName());
            })
            .verifyComplete();
    }

    @Test
    public void getAccountInfoMin() {
        assertAsyncResponseStatusCode(primaryBlobServiceAsyncClient.getAccountInfoWithResponse(), 200);
    }

    @Test
    public void getAccountInfoBase() {
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());

        StepVerifier.create(ccAsync.getAccountInfo())
            .assertNext(r -> {
                assertNotNull(r.getAccountKind());
                assertNotNull(r.getSkuName());
                assertFalse(r.isHierarchicalNamespaceEnabled());
            })
            .verifyComplete();
    }

    @Test
    public void getAccountInfoBaseFail() {
        BlobServiceAsyncClient serviceClient = instrument(new BlobServiceClientBuilder()
            .endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint())
            .credential(new MockTokenCredential()))
            .buildAsyncClient();

        BlobContainerAsyncClient containerClient = serviceClient.getBlobContainerAsyncClient(generateContainerName());

        StepVerifier.create(containerClient.getAccountInfo())
            .verifyErrorSatisfies(r -> {
                BlobStorageException e = assertInstanceOf(BlobStorageException.class, r);
                assertEquals(BlobErrorCode.INVALID_AUTHENTICATION_INFO, e.getErrorCode());
            });
    }

    @Test
    public void getContainerName() {
        String containerName = generateContainerName();
        BlobContainerAsyncClient newcc = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(containerName);
        assertEquals(containerName, newcc.getBlobContainerName());
    }

    @Test
    public void builderCpkValidation() {
        URL endpoint = BlobUrlParts.parse(ccAsync.getBlobContainerUrl()).setScheme("http").toUrl();
        BlobContainerClientBuilder builder = new BlobContainerClientBuilder()
            .customerProvidedKey(new CustomerProvidedKey(Base64.getEncoder().encodeToString(getRandomByteArray(256))))
            .endpoint(endpoint.toString());

        assertThrows(IllegalArgumentException.class, builder::buildAsyncClient);
    }

    @Test
    public void builderBearerTokenValidation() {
        URL endpoint = BlobUrlParts.parse(ccAsync.getBlobContainerUrl()).setScheme("http").toUrl();
        BlobContainerClientBuilder builder = new BlobContainerClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint.toString());

        assertThrows(IllegalArgumentException.class, builder::buildAsyncClient);
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    @Test
    public void perCallPolicy() {
        BlobContainerAsyncClient containerClient = getContainerClientBuilder(ccAsync.getBlobContainerUrl())
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential())
            .addPolicy(getPerCallVersionPolicy())
            .buildAsyncClient();

        StepVerifier.create(containerClient.getPropertiesWithResponse(null))
            .assertNext(r -> assertEquals("2017-11-09", r.getHeaders().getValue(X_MS_VERSION)))
            .verifyComplete();
    }

    @Test
    public void defaultAudience() {
        BlobContainerAsyncClient aadContainer = getContainerClientBuilderWithTokenCredential(ccAsync.getBlobContainerUrl())
            .audience(null)
            .buildAsyncClient();

        StepVerifier.create(aadContainer.exists())
            .expectNext(true);
    }

    @Test
    public void storageAccountAudience() {
        BlobContainerAsyncClient aadContainer = getContainerClientBuilderWithTokenCredential(ccAsync.getBlobContainerUrl())
            .audience(BlobAudience.createBlobServiceAccountAudience(ccAsync.getAccountName()))
            .buildAsyncClient();

        StepVerifier.create(aadContainer.exists())
            .expectNext(true);
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2024-08-04")
    @LiveOnly
    @Test
    /* This test tests if the bearer challenge is working properly. A bad audience is passed in, the service returns
    the default audience, and the request gets retried with this default audience, making the call function as expected.
     */
    public void audienceErrorBearerChallengeRetry() {
        BlobContainerAsyncClient aadContainer = getContainerClientBuilderWithTokenCredential(ccAsync.getBlobContainerUrl())
                .audience(BlobAudience.createBlobServiceAccountAudience("badAudience"))
                .buildAsyncClient();

        StepVerifier.create(aadContainer.exists())
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    @Test
    public void audienceFromString() {
        String url = String.format("https://%s.blob.core.windows.net/", ccAsync.getAccountName());
        BlobAudience audience = BlobAudience.fromString(url);

        BlobContainerAsyncClient aadContainer = getContainerClientBuilderWithTokenCredential(ccAsync.getBlobContainerUrl())
            .audience(audience)
            .buildAsyncClient();

        StepVerifier.create(aadContainer.exists())
            .expectNext(true);
    }


}


