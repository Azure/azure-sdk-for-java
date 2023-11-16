// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.Response;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobAccessPolicy;
import com.azure.storage.blob.models.BlobContainerAccessPolicies;
import com.azure.storage.blob.models.BlobContainerProperties;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobSignedIdentifier;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.BlobType;
import com.azure.storage.blob.models.LeaseStateType;
import com.azure.storage.blob.models.LeaseStatusType;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.options.BlobContainerCreateOptions;
import com.azure.storage.blob.specialized.AppendBlobAsyncClient;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
            .assertNext(r -> assertEquals(r.getValue().getMetadata(), metadata))
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
            .assertNext(r -> assertEquals(r.getValue().getMetadata(), metadata))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("publicAccessSupplier")
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
            .assertNext(r -> assertNotNull(r))
            .verifyComplete();
    }

    @Test
    public void getPropertiesLease() {
    //todo isbr
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
        //todo isbr:

        leaseID = setupContainerLeaseCondition(cc, leaseID);
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
    public void setAccessPolicy(PublicAccessType publicAccess) {
        StepVerifier.create(ccAsync.setAccessPolicyWithResponse(publicAccess, null, null))
            .assertNext(r -> assertTrue(validateBasicHeaders(r.getHeaders())))
            .verifyComplete();

        StepVerifier.create(ccAsync.getProperties())
            .assertNext(r -> assertEquals(r.getBlobPublicAccess(), publicAccess))
            .verifyComplete();
    }

    @Test
    public void setAccessPolicyMinAccess() {
        ccAsync.setAccessPolicy(PublicAccessType.CONTAINER, null).block();
        StepVerifier.create(ccAsync.getProperties())
            .assertNext(r -> assertEquals(r.getBlobPublicAccess(), PublicAccessType.CONTAINER))
            .verifyComplete();
    }

    @Test
    public void setAccessPolicyMinIds() {
        BlobSignedIdentifier identifier = new BlobSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new BlobAccessPolicy()
                .setStartsOn(testResourceNamer.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime())
                .setExpiresOn(testResourceNamer.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime()
                    .plusDays(1))
                .setPermissions("r"));

        List<BlobSignedIdentifier> ids = Collections.singletonList(identifier);

        ccAsync.setAccessPolicy(null, ids).block();

        StepVerifier.create(ccAsync.getAccessPolicy())
            .assertNext(r -> assertEquals(r.getIdentifiers().get(0).getId(), "0000"))
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
       //todo isbr:
        leaseID = setupContainerLeaseCondition(cc, leaseID);
        BlobRequestConditions cac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(ccAsync.setAccessPolicyWithResponse(null, null, cac), 200);
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
            .verifyError(BlobStorageException.class);
    }

    private static Stream<Arguments> setAccessPolicyACIllegalSupplier() {
        return Stream.of(
            Arguments.of(RECEIVED_ETAG, null),
            Arguments.of(null, GARBAGE_ETAG));
    }

    @Test
    public void setAccessPolicyError() {
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());

        StepVerifier.create(ccAsync.setAccessPolicy(null, null))
            .verifyError(BlobStorageException.class);
    }

    @Test
    public void getAccessPolicy() {
        BlobSignedIdentifier identifier = new BlobSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new BlobAccessPolicy()
                .setStartsOn(testResourceNamer.now())
                .setExpiresOn(testResourceNamer.now().plusDays(1))
                .setPermissions("r"));
        List<BlobSignedIdentifier> ids = Collections.singletonList(identifier);
        ccAsync.setAccessPolicy(PublicAccessType.BLOB, ids).block();

        StepVerifier.create(ccAsync.getAccessPolicyWithResponse(null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 200);
                assertEquals(r.getValue().getBlobAccessType(), PublicAccessType.BLOB);
                assertTrue(validateBasicHeaders(r.getHeaders()));
                assertEquals(r.getValue().getIdentifiers().get(0).getAccessPolicy().getExpiresOn(),
                    identifier.getAccessPolicy().getExpiresOn());
                assertEquals(r.getValue().getIdentifiers().get(0).getAccessPolicy().getStartsOn(),
                    identifier.getAccessPolicy().getStartsOn());
                assertEquals(r.getValue().getIdentifiers().get(0).getAccessPolicy().getPermissions(),
                    identifier.getAccessPolicy().getPermissions());
            })
            .verifyComplete();
    }

    @Test
    public void getAccessPolicyLease() {
        //todo isbr
        String leaseID = setupContainerLeaseCondition(cc, RECEIVED_LEASE_ID);
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
        //todo isbr
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
        //todo isbr
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
    @EnabledIf("com.azure.storage.blob.BlobTestBase#isPlaybackMode")
    @Test
    public void deleteIfExistsContainerThatWasAlreadyDeleted() {
        //todo isbr
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

    @DisabledIf("com.azure.storage.blob.BlobTestBase#olderThan20191212ServiceVersion")
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
}
