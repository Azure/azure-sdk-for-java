// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.AppendBlobItem;
import com.azure.storage.blob.models.BlobAccessPolicy;
import com.azure.storage.blob.models.BlobAudience;
import com.azure.storage.blob.models.BlobContainerProperties;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.BlobProperties;
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
import com.azure.storage.blob.models.StorageAccountInfo;
import com.azure.storage.blob.models.TaggedBlobItem;
import com.azure.storage.blob.options.BlobContainerCreateOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.options.BlobSetAccessTierOptions;
import com.azure.storage.blob.options.FindBlobsOptions;
import com.azure.storage.blob.options.PageBlobCreateOptions;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.TestHttpClientType;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.extensions.PlaybackOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContainerApiTests extends BlobTestBase {
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
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName());

        Response<Void> response = cc.createWithResponse(null, null, null, null);
        assertResponseStatusCode(response, 201);
        validateBasicHeaders(response.getHeaders());
    }

    @Test
    public void createMin() {
        BlobContainerClient cc = primaryBlobServiceClient.createBlobContainer(generateContainerName());
        assertTrue(cc.exists());
    }

    @ParameterizedTest
    @MethodSource("createMetadataSupplier")
    public void createMetadata(String key1, String value1, String key2, String value2) {
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName());
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }

        cc.createWithResponse(metadata, null, null, null);
        Response<BlobContainerProperties> response = cc.getPropertiesWithResponse(null, null, null);

        if (ENVIRONMENT.getHttpClientType() == TestHttpClientType.JDK_HTTP) {
            // JDK HttpClient returns headers with names lowercased.
            Map<String, String> lowercasedMetadata = metadata.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().toLowerCase(), Map.Entry::getValue));
            assertEquals(lowercasedMetadata, response.getValue().getMetadata());
        } else {
            assertEquals(metadata, response.getValue().getMetadata());
        }
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
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName());
        cc.createWithResponse(null, publicAccess, null, null);
        PublicAccessType access = cc.getProperties().getBlobPublicAccess();
        assertEquals(access, publicAccess);
    }

    private static Stream<Arguments> publicAccessSupplier() {
        return Stream.of(
            Arguments.of(PublicAccessType.BLOB),
            Arguments.of(PublicAccessType.CONTAINER),
            Arguments.of((PublicAccessType) null));
    }

    @Test
    public void createError() {
        BlobStorageException e = assertThrows(BlobStorageException.class, () -> cc.create());
        assertExceptionStatusCodeAndMessage(e, 409, BlobErrorCode.CONTAINER_ALREADY_EXISTS);
        assertTrue(e.getServiceMessage().contains("The specified container already exists."));
    }

    @Test
    public void createIfNotExistsAllNull() {
        // Overwrite the existing cc, which has already been created
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName());

        Response<Boolean> response = cc.createIfNotExistsWithResponse(null, null, null);

        assertResponseStatusCode(response, 201);
        assertTrue(validateBasicHeaders(response.getHeaders()));
    }

    @Test
    public void createIfNotExistsMin() {
        BlobContainerClient cc = primaryBlobServiceClient.createBlobContainerIfNotExists(generateContainerName());

        assertTrue(cc.exists());
    }

    @Test
    public void createIfNotExistsMinContainer() {
        BlobContainerClient cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName());
        boolean result = cc.createIfNotExists();
        assertTrue(result);
    }

    @Test
    public void createIfNotExistsWithResponse() {
        Response<BlobContainerClient> response = primaryBlobServiceClient
            .createBlobContainerIfNotExistsWithResponse(generateContainerName(), null, null);

        assertResponseStatusCode(response, 201);
    }

    @Test
    public void createIfNotExistsBlobServiceThatAlreadyExists() {
        String containerName = generateContainerName();
        Response<BlobContainerClient> response =
            primaryBlobServiceClient.createBlobContainerIfNotExistsWithResponse(containerName, null, null);
        Response<BlobContainerClient> secondResponse =
            primaryBlobServiceClient.createBlobContainerIfNotExistsWithResponse(containerName, null, null);

        assertResponseStatusCode(response, 201);
        assertResponseStatusCode(secondResponse, 409);
    }

    @ParameterizedTest
    @MethodSource("createMetadataSupplier")
    public void createIfNotExistsMetadataIfNotExists(String key1, String value1, String key2, String value2) {
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName());
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }
        BlobContainerCreateOptions options = new BlobContainerCreateOptions().setMetadata(metadata);

        Response<Boolean> result = cc.createIfNotExistsWithResponse(options, null, null);
        Response<BlobContainerProperties> response = cc.getPropertiesWithResponse(null, null, null);

        if (ENVIRONMENT.getHttpClientType() == TestHttpClientType.JDK_HTTP) {
            // JDK HttpClient returns headers with names lowercased.
            Map<String, String> lowercasedMetadata = metadata.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().toLowerCase(), Map.Entry::getValue));
            assertEquals(lowercasedMetadata, response.getValue().getMetadata());
        } else {
            assertEquals(metadata, response.getValue().getMetadata());
        }
        assertTrue(result.getValue());
    }

    @ParameterizedTest
    @MethodSource("publicAccessSupplier")
    @PlaybackOnly
    public void createIfNotExistsPublicAccess(PublicAccessType publicAccess) {
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName());
        Response<Boolean> result = cc.createIfNotExistsWithResponse(new BlobContainerCreateOptions()
            .setPublicAccessType(publicAccess), null, null);
        PublicAccessType access = cc.getProperties().getBlobPublicAccess();
        assertEquals(access, publicAccess);
        assertTrue(result.getValue());
    }

    @Test
    public void createIfNotExistsOnContainerThatAlreadyExists() {
        assertFalse(cc.createIfNotExists());
    }

    @Test
    public void createIfNotExistsOnAContainerThatAlreadyExistsWithResponse() {
        BlobContainerClient cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName());
        Response<Boolean> initialResponse = cc.createIfNotExistsWithResponse(null, null, null);

        Response<Boolean> secondResponse = cc.createIfNotExistsWithResponse(null, null, null);

        assertResponseStatusCode(initialResponse, 201);
        assertTrue(initialResponse.getValue());
        assertResponseStatusCode(secondResponse, 409);
        assertFalse(secondResponse.getValue());
    }

    @Test
    public void getPropertiesNull() {
        Response<BlobContainerProperties> response = cc.getPropertiesWithResponse(null, null, null);

        assertTrue(validateBasicHeaders(response.getHeaders()));
        assertNull(response.getValue().getBlobPublicAccess());
        assertFalse(response.getValue().hasImmutabilityPolicy());
        assertFalse(response.getValue().hasLegalHold());
        assertNull(response.getValue().getLeaseDuration());
        assertEquals(response.getValue().getLeaseState(), LeaseStateType.AVAILABLE);
        assertEquals(response.getValue().getLeaseStatus(), LeaseStatusType.UNLOCKED);
        assertEquals(0, response.getValue().getMetadata().size());
        assertFalse(response.getValue().isEncryptionScopeOverridePrevented());
        assertNotNull(response.getValue().getDefaultEncryptionScope());
    }

    @Test
    public void getPropertiesMin() {
        assertNotNull(cc.getProperties());
    }

    @Test
    public void getPropertiesLease() {
        String leaseID = setupContainerLeaseCondition(cc, RECEIVED_LEASE_ID);

        assertResponseStatusCode(cc.getPropertiesWithResponse(leaseID, null, null), 200);
    }

    @Test
    public void getPropertiesLeaseFail() {
        assertThrows(BlobStorageException.class, () ->
            cc.getPropertiesWithResponse("garbage", null, null));
    }

    @Test
    public void getPropertiesError() {
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName());
        assertThrows(BlobStorageException.class, () -> cc.getProperties());
    }

    @Test
    public void setMetadata() {
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName());
        Map<String, String> metadata = new HashMap<>();
        metadata.put("key", "value");
        cc.createWithResponse(metadata, null, null, null);

        Response<Void> response = cc.setMetadataWithResponse(null, null, null, null);

        assertResponseStatusCode(response, 200);
        assertTrue(validateBasicHeaders(response.getHeaders()));
        assertEquals(cc.getPropertiesWithResponse(null, null, null).getValue().getMetadata().size(), 0);
    }

    @Test
    public void setMetadataMin() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("foo", "bar");

        cc.setMetadata(metadata);

        assertEquals(cc.getPropertiesWithResponse(null, null, null).getValue().getMetadata(), metadata);
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

        assertResponseStatusCode(cc.setMetadataWithResponse(metadata, null, null, null), 200);
        assertEquals(cc.getPropertiesWithResponse(null, null, null).getValue().getMetadata(), metadata);
    }

    private static Stream<Arguments> setMetadataMetadataSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of("foo", "bar", "fizz", "buzz")
        );
    }

    @ParameterizedTest
    @MethodSource("setMetadataACSupplier")
    public void setMetadataAC(OffsetDateTime modified, String leaseID) {
        leaseID = setupContainerLeaseCondition(cc, leaseID);
        BlobRequestConditions cac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified);

        assertResponseStatusCode(cc.setMetadataWithResponse(null, cac, null, null), 200);
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

        assertThrows(BlobStorageException.class, () -> cc.setMetadataWithResponse(null, cac, null, null));
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

        assertThrows(UnsupportedOperationException.class, () -> cc.setMetadataWithResponse(null, mac, null, null));
    }

    private static Stream<Arguments> setMetadataACIllegalSupplier() {
        return Stream.of(
            Arguments.of(NEW_DATE, null, null),
            Arguments.of(null, RECEIVED_ETAG, null),
            Arguments.of(null, null, GARBAGE_ETAG));
    }


    @Test
    public void setMetadataError() {
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName());
        assertThrows(BlobStorageException.class, () -> cc.setMetadata(null));
    }

    @ParameterizedTest
    @MethodSource("publicAccessSupplier")
    @PlaybackOnly
    public void setAccessPolicy(PublicAccessType publicAccess) {
        Response<Void> response = cc.setAccessPolicyWithResponse(publicAccess, null, null, null, null);

        assertTrue(validateBasicHeaders(response.getHeaders()));
        assertEquals(cc.getProperties().getBlobPublicAccess(), publicAccess);
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

        Response<Void> response = cc.setAccessPolicyWithResponse(null, ids, null, null, null);


        List<BlobSignedIdentifier> receivedIdentifiers = cc.getAccessPolicyWithResponse(null, null, null)
            .getValue().getIdentifiers();

        assertResponseStatusCode(response, 200);
        assertTrue(validateBasicHeaders(response.getHeaders()));
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
    }

    @ParameterizedTest
    @MethodSource("setAccessPolicyACSupplier")
    public void setAccessPolicyAC(OffsetDateTime modified, OffsetDateTime unmodified, String leaseID) {
        leaseID = setupContainerLeaseCondition(cc, leaseID);
        BlobRequestConditions cac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertResponseStatusCode(cc.setAccessPolicyWithResponse(null, null, cac, null, null), 200);
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

        assertThrows(BlobStorageException.class, () ->
            cc.setAccessPolicyWithResponse(null, null, cac, null, null));
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

        assertThrows(UnsupportedOperationException.class, () ->
                cc.setAccessPolicyWithResponse(null, null, mac, null, null));
    }

    private static Stream<Arguments> setAccessPolicyACIllegalSupplier() {
        return Stream.of(
            Arguments.of(RECEIVED_ETAG, null),
            Arguments.of(null, GARBAGE_ETAG));
    }

    @Test
    public void getAccessPolicyLease() {
        String leaseID = setupContainerLeaseCondition(cc, RECEIVED_LEASE_ID);
        assertResponseStatusCode(cc.getAccessPolicyWithResponse(leaseID, null, null), 200);
    }

    @Test
    public void getAccessPolicyLeaseFail() {
        assertThrows(BlobStorageException.class, () ->
            cc.getAccessPolicyWithResponse(GARBAGE_LEASE_ID, null, null));
    }

    @Test
    public void getAccessPolicyError() {
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName());
        assertThrows(BlobStorageException.class, () -> cc.getAccessPolicy());
    }

    @Test
    public void delete() {
        Response<Void> response = cc.deleteWithResponse(null, null, null);
        assertResponseStatusCode(response, 202);

        assertNotNull(response.getHeaders().getValue(X_MS_REQUEST_ID));
        assertNotNull(response.getHeaders().getValue(X_MS_VERSION));
        assertNotNull(response.getHeaders().getValue(HttpHeaderName.DATE));
    }

    @Test
    public void deleteMin() {
        cc.delete();
        assertFalse(cc.exists());
    }

    @ParameterizedTest
    @MethodSource("setAccessPolicyACSupplier")
    public void deleteAC(OffsetDateTime modified, OffsetDateTime unmodified, String leaseID) {
        leaseID = setupContainerLeaseCondition(cc, leaseID);
        BlobRequestConditions cac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertResponseStatusCode(cc.deleteWithResponse(cac, null, null), 202);
    }

    @ParameterizedTest
    @MethodSource("setAccessPolicyACFailSupplier")
    public void deleteACFail(OffsetDateTime modified, OffsetDateTime unmodified, String leaseID) {
        BlobRequestConditions cac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(BlobStorageException.class, () -> cc.deleteWithResponse(cac, null, null));
    }

    @ParameterizedTest
    @MethodSource("setAccessPolicyACIllegalSupplier")
    public void deleteACIllegal(String match, String noneMatch) {
        BlobRequestConditions mac = new BlobRequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch);

        assertThrows(UnsupportedOperationException.class, () -> cc.deleteWithResponse(mac, null, null));
    }

    @Test
    public void deleteError() {
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName());
        assertThrows(BlobStorageException.class, () -> cc.delete());
    }

    @Test
    public void deleteIfExists() {
        Response<Boolean> response = cc.deleteIfExistsWithResponse(null, null, null);

        assertTrue(response.getValue());
        assertResponseStatusCode(response, 202);
        assertNotNull(response.getHeaders().getValue(X_MS_REQUEST_ID));
        assertNotNull(response.getHeaders().getValue(X_MS_VERSION));
        assertNotNull(response.getHeaders().getValue(HttpHeaderName.DATE));
    }

    @Test
    public void deleteIfExistsMin() {
        boolean result = cc.deleteIfExists();

        assertTrue(result);
        assertFalse(cc.exists());
    }

    @ParameterizedTest
    @MethodSource("setAccessPolicyACSupplier")
    public void deleteIfExistsAC(OffsetDateTime modified, OffsetDateTime unmodified, String leaseID) {
        leaseID = setupContainerLeaseCondition(cc, leaseID);
        BlobRequestConditions cac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertResponseStatusCode(cc.deleteIfExistsWithResponse(cac, null, null), 202);
    }

    @ParameterizedTest
    @MethodSource("setAccessPolicyACFailSupplier")
    public void deleteIfExistsACFail(OffsetDateTime modified, OffsetDateTime unmodified, String leaseID) {
        BlobRequestConditions cac = new BlobRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(BlobStorageException.class, () -> cc.deleteIfExistsWithResponse(cac, null, null));
    }

    @ParameterizedTest
    @MethodSource("setAccessPolicyACIllegalSupplier")
    public void deleteIfExistsACIllegal(String match, String noneMatch) {
        BlobRequestConditions mac = new BlobRequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch);

        assertThrows(UnsupportedOperationException.class, () -> cc.deleteIfExistsWithResponse(mac, null, null));
    }

    @Test
    public void deleteIfExistsOnAContainerThatDoesNotExist() {
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName());

        Response<Boolean> response = cc.deleteIfExistsWithResponse(new BlobRequestConditions(), null, null);

        assertFalse(response.getValue());
        assertResponseStatusCode(response, 404);
    }

    // We can't guarantee that the requests will always happen before the container is garbage collected
    @PlaybackOnly
    @Test
    public void deleteIfExistsContainerThatWasAlreadyDeleted() {
        boolean result = cc.deleteIfExists();
        boolean result2 = cc.deleteIfExists();

        assertTrue(result);
            // Confirming the behavior of the api when the container is in the deleting state.
            // After deletehas been called once but before it has been garbage collected
        assertTrue(result2);
        assertFalse(cc.exists());
    }

    @Test
    public void listBlockBlobsFlat() {
        String name = generateBlobName();
        BlockBlobClient bu = cc.getBlobClient(name).getBlockBlobClient();
        bu.upload(DATA.getDefaultInputStream(), 7);

        Iterator<BlobItem> blobs = cc.listBlobs(new ListBlobsOptions().setPrefix(prefix), null).iterator();

        BlobItem blob = blobs.next();
        assertFalse(blobs.hasNext());
        assertEquals(name, blob.getName());
        assertEquals(BlobType.BLOCK_BLOB, blob.getProperties().getBlobType());
        assertNull(blob.getProperties().getCopyCompletionTime());
        assertNull(blob.getProperties().getCopyStatusDescription());
        assertNull(blob.getProperties().getCopyId());
        assertNull(blob.getProperties().getCopyProgress());
        assertNull(blob.getProperties().getCopySource());
        assertNull(blob.getProperties().getCopyStatus());
        assertNull(blob.getProperties().isIncrementalCopy());
        assertNull(blob.getProperties().getDestinationSnapshot());
        assertNull(blob.getProperties().getLeaseDuration());
        assertEquals(LeaseStateType.AVAILABLE, blob.getProperties().getLeaseState());
        assertEquals(LeaseStatusType.UNLOCKED, blob.getProperties().getLeaseStatus());
        assertNotNull(blob.getProperties().getContentLength());
        assertNotNull(blob.getProperties().getContentType());
        assertNotNull(blob.getProperties().getContentMd5());
        assertNull(blob.getProperties().getContentEncoding());
        assertNull(blob.getProperties().getContentDisposition());
        assertNull(blob.getProperties().getContentLanguage());
        assertNull(blob.getProperties().getCacheControl());
        assertNull(blob.getProperties().getBlobSequenceNumber());
        assertTrue(blob.getProperties().isServerEncrypted());
        assertTrue(blob.getProperties().isAccessTierInferred());
        assertEquals(AccessTier.HOT, blob.getProperties().getAccessTier());
        assertNull(blob.getProperties().getArchiveStatus());
        assertNotNull(blob.getProperties().getCreationTime());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void listAppendBlobsFlat() {
        String name = generateBlobName();
        AppendBlobClient bu = cc.getBlobClient(name).getAppendBlobClient();
        bu.create();
        bu.seal();

        Iterator<BlobItem> blobs = cc.listBlobs(new ListBlobsOptions().setPrefix(prefix), null).iterator();

        BlobItem blob = blobs.next();
        assertFalse(blobs.hasNext());
        assertEquals(name, blob.getName());
        assertEquals(BlobType.APPEND_BLOB, blob.getProperties().getBlobType());
        assertNull(blob.getProperties().getCopyCompletionTime());
        assertNull(blob.getProperties().getCopyStatusDescription());
        assertNull(blob.getProperties().getCopyId());
        assertNull(blob.getProperties().getCopyProgress());
        assertNull(blob.getProperties().getCopySource());
        assertNull(blob.getProperties().getCopyStatus());
        assertNull(blob.getProperties().isIncrementalCopy());
        assertNull(blob.getProperties().getDestinationSnapshot());
        assertNull(blob.getProperties().getLeaseDuration());
        assertEquals(LeaseStateType.AVAILABLE, blob.getProperties().getLeaseState());
        assertEquals(LeaseStatusType.UNLOCKED, blob.getProperties().getLeaseStatus());
        assertNotNull(blob.getProperties().getContentLength());
        assertNotNull(blob.getProperties().getContentType());
        assertNull(blob.getProperties().getContentMd5());
        assertNull(blob.getProperties().getContentEncoding());
        assertNull(blob.getProperties().getContentDisposition());
        assertNull(blob.getProperties().getContentLanguage());
        assertNull(blob.getProperties().getCacheControl());
        assertNull(blob.getProperties().getBlobSequenceNumber());
        assertTrue(blob.getProperties().isServerEncrypted());
        assertNull(blob.getProperties().isAccessTierInferred());
        assertNull(blob.getProperties().getAccessTier());
        assertNull(blob.getProperties().getArchiveStatus());
        assertNotNull(blob.getProperties().getCreationTime());
        assertTrue(blob.getProperties().isSealed());
    }

    @Test
    public void listPageBlobsFlat() {
        ccPremium = premiumBlobServiceClient.getBlobContainerClient(containerName);
        ccPremium.createIfNotExists();
        String name = generateBlobName();
        PageBlobClient bu = ccPremium.getBlobClient(name).getPageBlobClient();
        bu.create(512);

        Iterator<BlobItem> blobs = ccPremium.listBlobs(new ListBlobsOptions().setPrefix(prefix), null).iterator();
        BlobItem blob = blobs.next();
        assertFalse(blobs.hasNext());
        assertEquals(name, blob.getName());
        assertEquals(BlobType.PAGE_BLOB, blob.getProperties().getBlobType());
        assertNull(blob.getProperties().getCopyCompletionTime());
        assertNull(blob.getProperties().getCopyStatusDescription());
        assertNull(blob.getProperties().getCopyId());
        assertNull(blob.getProperties().getCopyProgress());
        assertNull(blob.getProperties().getCopySource());
        assertNull(blob.getProperties().getCopyStatus());
        assertNull(blob.getProperties().isIncrementalCopy());
        assertNull(blob.getProperties().getDestinationSnapshot());
        assertNull(blob.getProperties().getLeaseDuration());
        assertEquals(LeaseStateType.AVAILABLE, blob.getProperties().getLeaseState());
        assertEquals(LeaseStatusType.UNLOCKED, blob.getProperties().getLeaseStatus());
        assertNotNull(blob.getProperties().getContentLength());
        assertNotNull(blob.getProperties().getContentType());
        assertNull(blob.getProperties().getContentMd5());
        assertNull(blob.getProperties().getContentEncoding());
        assertNull(blob.getProperties().getContentDisposition());
        assertNull(blob.getProperties().getContentLanguage());
        assertNull(blob.getProperties().getCacheControl());
        assertEquals(0, blob.getProperties().getBlobSequenceNumber());
        assertTrue(blob.getProperties().isServerEncrypted());
        assertTrue(blob.getProperties().isAccessTierInferred());
        assertEquals(AccessTier.P10, blob.getProperties().getAccessTier());
        assertNull(blob.getProperties().getArchiveStatus());
        assertNotNull(blob.getProperties().getCreationTime());

        // cleanup:
        ccPremium.delete();
    }

    @Test
    public void listBlobsFlatMin() {
        assertDoesNotThrow(() -> cc.listBlobs().iterator().hasNext());
    }

    private String setupListBlobsTest(String normalName, String copyName, String metadataName, String tagsName,
        String uncommittedName) {
        PageBlobClient normal = cc.getBlobClient(normalName).getPageBlobClient();
        normal.create(512);

        PageBlobClient copyBlob = cc.getBlobClient(copyName).getPageBlobClient();
        setPlaybackSyncPollerPollInterval(copyBlob.beginCopy(normal.getBlobUrl(), null)).waitForCompletion();

        PageBlobClient metadataBlob = cc.getBlobClient(metadataName).getPageBlobClient();
        Map<String, String> metadata = new HashMap<>();
        metadata.put("foo", "bar");
        metadataBlob.createWithResponse(512, null, null, metadata, null, null, null);

        PageBlobClient tagsBlob = cc.getBlobClient(tagsName).getPageBlobClient();
        Map<String, String> tags = new HashMap<>();
        tags.put(tagKey, tagValue);
        tagsBlob.createWithResponse(new PageBlobCreateOptions(512).setTags(tags), null, null);

        BlockBlobClient uncommittedBlob = cc.getBlobClient(uncommittedName).getBlockBlobClient();
        uncommittedBlob.stageBlock(getBlockID(), DATA.getDefaultInputStream(), DATA.getDefaultData().remaining());

        return normal.createSnapshot().getSnapshotId();
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

        List<BlobItem> blobs = cc.listBlobs(options, null).stream().collect(Collectors.toList());

        assertEquals(normalName, blobs.get(0).getName());
        assertEquals(copyName, blobs.get(1).getName());
        assertNotNull(blobs.get(1).getProperties().getCopyId());
        // Comparing the urls isn't reliable because the service may use https.
        assertTrue(blobs.get(1).getProperties().getCopySource().contains(normalName));
        // We waited for the copy to complete.
        assertEquals(CopyStatusType.SUCCESS, blobs.get(1).getProperties().getCopyStatus());
        assertNotNull(blobs.get(1).getProperties().getCopyProgress());
        assertNotNull(blobs.get(1).getProperties().getCopyCompletionTime());
        assertEquals(4, blobs.size()); // Normal, copy, metadata, tags
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

        List<BlobItem> blobs = cc.listBlobs(options, null).stream().collect(Collectors.toList());

        assertEquals(normalName, blobs.get(0).getName());
        assertEquals(copyName, blobs.get(1).getName());
        assertNull(blobs.get(1).getProperties().getCopyCompletionTime());
        assertEquals(metadataName, blobs.get(2).getName());
        assertEquals("bar", blobs.get(2).getMetadata().get("foo"));
        assertEquals(4, blobs.size()); // Normal, copy, metadata, tags
    }

    @PlaybackOnly
    @Test
    public void listBlobsFlatOptionsLastAccessTime() {
        BlockBlobClient b = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        b.upload(DATA.getDefaultInputStream(), DATA.getDefaultData().remaining());
        BlobItem blob = cc.listBlobs().iterator().next();

        assertNotNull(blob.getProperties().getLastAccessedTime());
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

        List<BlobItem> blobs = cc.listBlobs(options, null).stream().collect(Collectors.toList());

        assertEquals(normalName, blobs.get(0).getName());
        assertEquals(copyName, blobs.get(1).getName());
        assertNull(blobs.get(1).getProperties().getCopyCompletionTime());
        assertEquals(metadataName, blobs.get(2).getName());
        assertNull(blobs.get(2).getMetadata());
        assertEquals(tagValue, blobs.get(3).getTags().get(tagKey));
        assertEquals(1, blobs.get(3).getProperties().getTagCount());
        assertEquals(4, blobs.size()); // Normal, copy, metadata, tags
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

        List<BlobItem> blobs = cc.listBlobs(options, null).stream().collect(Collectors.toList());

        assertEquals(normalName, blobs.get(0).getName());
        assertEquals(snapshotTime, blobs.get(0).getSnapshot());
        assertEquals(normalName, blobs.get(1).getName());
        assertEquals(5, blobs.size()); // Normal, snapshot, copy, metadata, tags
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

        List<BlobItem> blobs = cc.listBlobs(options, null).stream().collect(Collectors.toList());

        assertEquals(normalName, blobs.get(0).getName());
        assertEquals(uncommittedName, blobs.get(4).getName());
        assertEquals(5, blobs.size()); // Normal, copy, metadata, tags, uncommitted
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

        Iterator<BlobItem> blobs = cc.listBlobs(options, null).iterator();

        assertEquals(normalName, blobs.next().getName());
        assertFalse(blobs.hasNext()); // Normal
    }

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

        // expect: "Get first page of blob listings (sync and async)"
        assertEquals(pageSize, cc.listBlobs(options, null).iterableByPage().iterator().next().getValue().size());
    }

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

        // expect: "Get first page of blob listings (sync and async)"
        for (PagedResponse<BlobItem> page : cc.listBlobs(options, null).iterableByPage(pageSize)) {
            assertTrue(page.getValue().size() <= pageSize);
        }
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void listBlobsFlatOptionsDeletedWithVersions() {
        BlobContainerClient versionedCC = versionedBlobServiceClient.getBlobContainerClient(containerName);
        versionedCC.createIfNotExists();
        String blobName = generateBlobName();
        AppendBlobClient blob = versionedCC.getBlobClient(blobName).getAppendBlobClient();
        blob.create();
        Map<String, String> metadata = new HashMap<>();
        metadata.put("foo", "bar");
        blob.setMetadata(metadata);
        blob.delete();
        ListBlobsOptions options = new ListBlobsOptions().setPrefix(blobName).setDetails(new BlobListDetails()
            .setRetrieveDeletedBlobsWithVersions(true));

        Iterator<BlobItem> blobs = versionedCC.listBlobs(options, null).iterator();

        BlobItem b = blobs.next();
        assertFalse(blobs.hasNext());
        assertEquals(blobName, b.getName());
        assertTrue(b.hasVersionsOnly());

        // cleanup:
        versionedCC.delete();
    }

    @Test
    public void listBlobsPrefixWithComma() {
        String prefix = generateBlobName() + ", " + generateBlobName();
        BlockBlobClient b = cc.getBlobClient(prefix).getBlockBlobClient();
        b.upload(DATA.getDefaultInputStream(), DATA.getDefaultData().remaining());

        ListBlobsOptions options = new ListBlobsOptions().setPrefix(prefix);
        BlobItem blob = cc.listBlobs(options, null).iterator().next();
        assertEquals(prefix, blob.getName());
    }

    @Test
    public void listBlobsFlatOptionsFail() {
        assertThrows(IllegalArgumentException.class, () -> new ListBlobsOptions().setMaxResultsPerPage(0));
    }

    @Test
    public void listBlobsFlatMarker() {
        int numBlobs = 10;
        int pageSize = 6;
        for (int i = 0; i < numBlobs; i++) {
            PageBlobClient bc = cc.getBlobClient(generateBlobName()).getPageBlobClient();
            bc.create(512);
        }

        // when: "listBlobs with sync client"
        PagedIterable<BlobItem> pagedIterable = cc.listBlobs(new ListBlobsOptions().setMaxResultsPerPage(pageSize), null);
        PagedResponse<BlobItem> pagedSyncResponse1 = pagedIterable.iterableByPage().iterator().next();
        PagedResponse<BlobItem> pagedSyncResponse2 =
            pagedIterable.iterableByPage(pagedSyncResponse1.getContinuationToken()).iterator().next();

        assertEquals(pageSize, pagedSyncResponse1.getValue().size());
        assertEquals(numBlobs - pageSize, pagedSyncResponse2.getValue().size());
        assertNull(pagedSyncResponse2.getContinuationToken());
    }

    @Test
    public void listBlobsFlatMarkerOverload() {
        int numBlobs = 10;
        int pageSize = 6;
        for (int i = 0; i < numBlobs; i++) {
            PageBlobClient bc = cc.getBlobClient(generateBlobName()).getPageBlobClient();
            bc.create(512);
        }

        // when: "listBlobs with sync client"
        PagedIterable<BlobItem> pagedIterable = cc.listBlobs(new ListBlobsOptions().setMaxResultsPerPage(pageSize),
            null);
        PagedResponse<BlobItem> pagedSyncResponse1 = pagedIterable.iterableByPage().iterator().next();

        pagedIterable = cc.listBlobs(new ListBlobsOptions().setMaxResultsPerPage(pageSize),
            pagedSyncResponse1.getContinuationToken(), null);
        PagedResponse<BlobItem> pagedSyncResponse2 = pagedIterable.iterableByPage().iterator().next();

        assertEquals(pageSize, pagedSyncResponse1.getValue().size());
        assertEquals(numBlobs - pageSize, pagedSyncResponse2.getValue().size());
        assertNull(pagedSyncResponse2.getContinuationToken());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("listBlobsFlatRehydratePrioritySupplier")
    public void listBlobsFlatRehydratePriority(RehydratePriority rehydratePriority) {
        String name = generateBlobName();
        BlockBlobClient bc = cc.getBlobClient(name).getBlockBlobClient();
        bc.upload(DATA.getDefaultInputStream(), 7);

        if (rehydratePriority != null) {
            bc.setAccessTier(AccessTier.ARCHIVE);
            bc.setAccessTierWithResponse(new BlobSetAccessTierOptions(AccessTier.HOT)
                .setPriority(rehydratePriority), null, null);
        }
        BlobItem item = cc.listBlobs().iterator().next();
        assertEquals(rehydratePriority, item.getProperties().getRehydratePriority());
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
        cc.getBlobClient(blobName).getAppendBlobClient().create();

        BlobItem blobItem = cc.listBlobs().iterator().next();
        assertEquals(blobName, blobItem.getName());
    }

    @Test
    public void listBlobsFlatError() {
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName());
        assertThrows(BlobStorageException.class, () -> cc.listBlobs().iterator().hasNext());
    }

    @Test
    public void listBlobsFlatWithTimeoutStillBackedByPagedFlux() {
        int numBlobs = 5;
        int pageResults = 3;

        for (int i = 0; i < numBlobs; i++) {
            BlockBlobClient blob = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
            blob.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        }

        // when: "Consume results by page, then still have paging functionality"
        assertDoesNotThrow(() -> cc.listBlobs(new ListBlobsOptions().setMaxResultsPerPage(pageResults),
            Duration.ofSeconds(10)).streamByPage().count());
    }

    @Test
    public void listBlobsHierWithTimeoutStillBackedByPagedFlux() {
        int numBlobs = 5;
        int pageResults = 3;

        for (int i = 0; i < numBlobs; i++) {
            BlockBlobClient blob = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
            blob.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());
        }

        // when: "Consume results by page, then still have paging functionality"
        assertDoesNotThrow(() -> cc.listBlobsByHierarchy("/", new ListBlobsOptions()
            .setMaxResultsPerPage(pageResults), Duration.ofSeconds(10)).streamByPage().count());
    }

    /*
    This test requires two accounts that are configured in a very specific way. It is not feasible to setup that
    relationship programmatically, so we have recorded a successful interaction and only test recordings.
    */

    //@EnabledIf("com.azure.storage.blob.BlobTestBase#isPlaybackMode")
    @Disabled("Need to re-record once account is setup properly.")
    @Test
    public void listBlobsFlatORS() {
        BlobContainerClient sourceContainer = primaryBlobServiceClient.getBlobContainerClient("test1");
        BlobContainerClient destContainer = alternateBlobServiceClient.getBlobContainerClient("test2");

        List<BlobItem> sourceBlobs = sourceContainer.listBlobs().stream().collect(Collectors.toList());
        List<BlobItem> destBlobs = destContainer.listBlobs().stream().collect(Collectors.toList());

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
        PageBlobClient bu = cc.getBlobClient(name).getPageBlobClient();
        bu.create(512);

        Iterator<BlobItem> blobs = cc.listBlobsByHierarchy(null).iterator();
        assertEquals(name, blobs.next().getName());
        assertFalse(blobs.hasNext());
    }

    @Test
    public void listBlobsHierarchyMin() {
        assertDoesNotThrow(() -> cc.listBlobsByHierarchy("/").iterator().hasNext());
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

        List<BlobItem> blobs = cc.listBlobsByHierarchy("", options, null).stream().collect(Collectors.toList());

        assertEquals(normalName, blobs.get(0).getName());
        assertEquals(copyName, blobs.get(1).getName());
        assertNotNull(blobs.get(1).getProperties().getCopyId());
        // Comparing the urls isn't reliable because the service may use https.
        assertTrue(blobs.get(1).getProperties().getCopySource().contains(normalName));
        assertEquals(CopyStatusType.SUCCESS, blobs.get(1).getProperties().getCopyStatus()); // We waited for the copy to complete.
        assertNotNull(blobs.get(1).getProperties().getCopyProgress());
        assertNotNull(blobs.get(1).getProperties().getCopyCompletionTime());
        assertEquals(4, blobs.size()); // Normal, copy, metadata, tags
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

        List<BlobItem>  blobs = cc.listBlobsByHierarchy("", options, null).stream().collect(Collectors.toList());

        assertEquals(normalName, blobs.get(0).getName());
        assertEquals(copyName, blobs.get(1).getName());
        assertNull(blobs.get(1).getProperties().getCopyCompletionTime());
        assertEquals(metadataName, blobs.get(2).getName());
        assertEquals("bar", blobs.get(2).getMetadata().get("foo"));
        assertEquals(4, blobs.size()); // Normal, copy, metadata, tags
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

        List<BlobItem> blobs = cc.listBlobsByHierarchy("", options, null).stream().collect(Collectors.toList());

        assertEquals(normalName, blobs.get(0).getName());
        assertEquals(copyName, blobs.get(1).getName());
        assertNull(blobs.get(1).getProperties().getCopyCompletionTime());
        assertEquals(metadataName, blobs.get(2).getName());
        assertNull(blobs.get(2).getMetadata());
        assertEquals(tagValue, blobs.get(3).getTags().get(tagKey));
        assertEquals(1, blobs.get(3).getProperties().getTagCount());
        assertEquals(4, blobs.size()); // Normal, copy, metadata, tags
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

        List<BlobItem> blobs = cc.listBlobsByHierarchy("", options, null).stream().collect(Collectors.toList());

        assertEquals(normalName, blobs.get(0).getName());
        assertEquals(uncommittedName, blobs.get(4).getName());
        assertEquals(5, blobs.size()); // Normal, copy, metadata, tags, uncommitted
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

        Iterator<BlobItem> blobs = cc.listBlobsByHierarchy("", options, null).iterator();

        assertEquals(normalName, blobs.next().getName());
        assertFalse(blobs.hasNext()); // Normal
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

        PagedIterable<BlobItem> pagedIterable = cc.listBlobsByHierarchy("", options, null);

        Iterable<PagedResponse<BlobItem>> iterableByPage = pagedIterable.iterableByPage(1);
        for (PagedResponse<BlobItem> page : iterableByPage) {
            assertEquals(1, page.getValue().size());
        }
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2020-10-02")
    @Test
    public void listBlobsHierOptionsDeletedWithVersions() {
        BlobContainerClient versionedCC = versionedBlobServiceClient.getBlobContainerClient(containerName);
        versionedCC.createIfNotExists();
        String blobName = generateBlobName();
        AppendBlobClient blob = versionedCC.getBlobClient(blobName).getAppendBlobClient();
        blob.create();
        Map<String, String> metadata = new HashMap<>();
        metadata.put("foo", "bar");
        blob.setMetadata(metadata);
        blob.delete();
        ListBlobsOptions options = new ListBlobsOptions().setPrefix(blobName)
            .setDetails(new BlobListDetails().setRetrieveDeletedBlobsWithVersions(true));

        Iterator<BlobItem> blobs = versionedCC.listBlobsByHierarchy("", options, null).iterator();

        BlobItem b = blobs.next();
        assertFalse(blobs.hasNext());
        assertEquals(blobName, b.getName());
        assertTrue(b.hasVersionsOnly());

        // cleanup:
        versionedCC.delete();
    }

    @ParameterizedTest
    @MethodSource("listBlobsHierOptionsFailSupplier")
    public void listBlobsHierOptionsFail(boolean snapshots, int maxResults, Class<? extends Throwable> exceptionType) {
        assertThrows(exceptionType, () -> {
            ListBlobsOptions options = new ListBlobsOptions()
                .setDetails(new BlobListDetails().setRetrieveSnapshots(snapshots))
                .setMaxResultsPerPage(maxResults);
            cc.listBlobsByHierarchy(null, options, null).iterator().hasNext();
        });
    }

    private static Stream<Arguments> listBlobsHierOptionsFailSupplier() {
        return Stream.of(
            Arguments.of(true, 5, UnsupportedOperationException.class),
            Arguments.of(false, 0, IllegalArgumentException.class));
    }

    @Test
    public void listBlobsHierDelim() {
        List<String> blobNames = Arrays.asList("a", "b/a", "c", "d/a", "e", "f", "g/a");
        for (String blobName : blobNames) {
            AppendBlobClient bu = cc.getBlobClient(blobName).getAppendBlobClient();
            bu.create();
        }

        Set<String> foundBlobs = new HashSet<>();
        Set<String> foundPrefixes = new HashSet<>();
        cc.listBlobsByHierarchy(null).stream().collect(Collectors.toList())
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
            PageBlobClient bc = cc.getBlobClient(generateBlobName()).getPageBlobClient();
            bc.create(512);
        }

        PagedIterable<BlobItem> blobs = cc.listBlobsByHierarchy("/",
            new ListBlobsOptions().setMaxResultsPerPage(pageSize), null);

        PagedResponse<BlobItem> firstPage = blobs.iterableByPage().iterator().next();

        assertEquals(pageSize, firstPage.getValue().size());
        assertNotNull(firstPage.getContinuationToken());

        PagedResponse<BlobItem> secondPage = blobs.iterableByPage(firstPage.getContinuationToken()).iterator().next();

        assertEquals(secondPage.getValue().size(), numBlobs - pageSize);
        assertNull(secondPage.getContinuationToken());
    }

    /*
    This test requires two accounts that are configured in a very specific way. It is not feasible to setup that
    relationship programmatically, so we have recorded a successful interaction and only test recordings.
    */

    @PlaybackOnly
    @Test
    public void listBlobsHierORS() {
        BlobContainerClient sourceContainer = primaryBlobServiceClient.getBlobContainerClient("test1");
        BlobContainerClient destContainer = alternateBlobServiceClient.getBlobContainerClient("test2");

        List<BlobItem> sourceBlobs = sourceContainer.listBlobsByHierarchy("/").stream().collect(Collectors.toList());
        List<BlobItem> destBlobs = destContainer.listBlobsByHierarchy("/").stream().collect(Collectors.toList());

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
            PageBlobClient bc = cc.getBlobClient(generateBlobName()).getPageBlobClient();
            bc.create(512);
        }

        // expect: "listing operation will fetch all 10 blobs, despite page size being smaller than 10"
        assertEquals(numBlobs,
            cc.listBlobs(new ListBlobsOptions().setMaxResultsPerPage(pageSize), null).stream().count());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("listBlobsFlatRehydratePrioritySupplier")
    public void listBlobsHierRehydratePriority(RehydratePriority rehydratePriority) {
        String name = generateBlobName();
        BlockBlobClient bc = cc.getBlobClient(name).getBlockBlobClient();
        bc.upload(DATA.getDefaultInputStream(), 7);

        if (rehydratePriority != null) {
            bc.setAccessTier(AccessTier.ARCHIVE);
            bc.setAccessTierWithResponse(new BlobSetAccessTierOptions(AccessTier.HOT)
                .setPriority(rehydratePriority), null, null);
        }

        BlobItem item = cc.listBlobsByHierarchy(null).iterator().next();
        assertEquals(rehydratePriority, item.getProperties().getRehydratePriority());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2019-12-12")
    @Test
    public void listAppendBlobsHier() {
        String name = generateBlobName();
        AppendBlobClient bu = cc.getBlobClient(name).getAppendBlobClient();
        bu.create();
        bu.seal();

        Iterator<BlobItem> blobs = cc.listBlobsByHierarchy(null, new ListBlobsOptions().setPrefix(prefix), null)
            .iterator();

        BlobItem blob = blobs.next();
        assertFalse(blobs.hasNext());
        assertEquals(name, blob.getName());
        assertEquals(BlobType.APPEND_BLOB, blob.getProperties().getBlobType());
        assertNull(blob.getProperties().getCopyCompletionTime());
        assertNull(blob.getProperties().getCopyStatusDescription());
        assertNull(blob.getProperties().getCopyId());
        assertNull(blob.getProperties().getCopyProgress());
        assertNull(blob.getProperties().getCopySource());
        assertNull(blob.getProperties().getCopyStatus());
        assertNull(blob.getProperties().isIncrementalCopy());
        assertNull(blob.getProperties().getDestinationSnapshot());
        assertNull(blob.getProperties().getLeaseDuration());
        assertEquals(LeaseStateType.AVAILABLE, blob.getProperties().getLeaseState());
        assertEquals(LeaseStatusType.UNLOCKED, blob.getProperties().getLeaseStatus());
        assertNotNull(blob.getProperties().getContentLength());
        assertNotNull(blob.getProperties().getContentType());
        assertNull(blob.getProperties().getContentMd5());
        assertNull(blob.getProperties().getContentEncoding());
        assertNull(blob.getProperties().getContentDisposition());
        assertNull(blob.getProperties().getContentLanguage());
        assertNull(blob.getProperties().getCacheControl());
        assertNull(blob.getProperties().getBlobSequenceNumber());
        assertTrue(blob.getProperties().isServerEncrypted());
        assertNull(blob.getProperties().isAccessTierInferred());
        assertNull(blob.getProperties().getAccessTier());
        assertNull(blob.getProperties().getArchiveStatus());
        assertNotNull(blob.getProperties().getCreationTime());
        assertTrue(blob.getProperties().isSealed());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-02-12")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void listBlobsHierInvalidXml(boolean delimiter) {
        String blobName = "dir1/dir2/file\uFFFE.blob";
        cc.getBlobClient(blobName).getAppendBlobClient().create();

        BlobItem blobItem;
        if (!delimiter) {
            blobItem = cc.listBlobsByHierarchy("", null, null).iterator().next();
        } else {
            blobItem = cc.listBlobsByHierarchy(".b", null, null).iterator().next();
        }

        assertEquals(blobItem.getName(), (delimiter ? "dir1/dir2/file\uFFFE.b" : blobName));
        assertEquals(delimiter, blobItem.isPrefix());
    }

    @Test
    public void listBlobsHierError() {
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName());
        assertThrows(BlobStorageException.class, () -> cc.listBlobsByHierarchy(".").iterator().hasNext());
    }

    private void setupContainerForListing(BlobContainerClient containerClient) {
        List<String> blobNames = Arrays.asList("foo", "bar", "baz", "foo/foo", "foo/bar", "baz/foo", "baz/foo/bar",
            "baz/bar/foo");
        byte[] data = getRandomByteArray(Constants.KB);

        for (String blob : blobNames) {
            BlockBlobClient blockBlobClient = containerClient.getBlobClient(blob).getBlockBlobClient();
            blockBlobClient.upload(new ByteArrayInputStream(data), Constants.KB);
        }
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-12-02")
    @Test
    public void listBlobsHierSegmentWithVersionPrefixAndDelimiter() {
        BlobContainerClient versionedCC = versionedBlobServiceClient.getBlobContainerClient(containerName);
        versionedCC.createIfNotExists();
        ListBlobsOptions options = new ListBlobsOptions()
            .setDetails(new BlobListDetails().setRetrieveVersions(true))
            .setPrefix("baz");

        setupContainerForListing(versionedCC);

        Set<BlobItem> foundBlobs = new HashSet<>();
        Set<BlobItem> foundPrefixes = new HashSet<>();

        versionedCC.listBlobsByHierarchy("/", options, null).stream().collect(Collectors.toList())
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
        versionedCC.delete();
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-04-10")
    @Test
    public void findBlobsMin() {
        assertDoesNotThrow(() -> cc.findBlobsByTags("\"key\"='value'").iterator().hasNext());
    }

    @SuppressWarnings("deprecation")
    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-04-10")
    @Test
    public void findBlobsQuery() {
        BlobClient blobClient = cc.getBlobClient(generateBlobName());
        blobClient.uploadWithResponse(new BlobParallelUploadOptions(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize()).setTags(Collections.singletonMap("key", "value")), null, null);
        blobClient = cc.getBlobClient(generateBlobName());
        blobClient.uploadWithResponse(new BlobParallelUploadOptions(DATA.getDefaultInputStream(),
            DATA.getDefaultDataSize()).setTags(Collections.singletonMap("bar", "foo")), null, null);
        blobClient = cc.getBlobClient(generateBlobName());
        blobClient.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSize());

        sleepIfRunningAgainstService(10 * 1000); // To allow tags to index

        String query = "\"bar\"='foo'";
        PagedIterable<TaggedBlobItem> results = cc.findBlobsByTags(String.format(query, cc.getBlobContainerName()));

        assertEquals(1, results.stream().count());
        TaggedBlobItem tags = results.iterator().next();
        Map<String, String> blobTags = tags.getTags();
        assertEquals(1, blobTags.size());
        assertEquals("foo", blobTags.get("bar"));
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-04-10")
    @Test
    public void findBlobsMarker() {
        Map<String, String> tags = Collections.singletonMap(tagKey, tagValue);
        for (int i = 0; i < 10; i++) {
            cc.getBlobClient(generateBlobName()).uploadWithResponse(
                new BlobParallelUploadOptions(DATA.getDefaultInputStream()).setTags(tags), null, null);
        }

        sleepIfRunningAgainstService(10 * 1000); // To allow tags to index

        PagedResponse<TaggedBlobItem> firstPage = cc.findBlobsByTags(
            new FindBlobsOptions(String.format("\"%s\"='%s'", tagKey, tagValue)).setMaxResultsPerPage(5), null,
            Context.NONE).iterableByPage().iterator().next();
        String marker = firstPage.getContinuationToken();
        String firstBlobName = firstPage.getValue().iterator().next().getName();

        PagedResponse<TaggedBlobItem> secondPage = cc.findBlobsByTags(
            new FindBlobsOptions(String.format("\"%s\"='%s'", tagKey, tagValue)).setMaxResultsPerPage(5), null,
            Context.NONE).iterableByPage(marker).iterator().next();

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
            cc.getBlobClient(generateBlobName()).uploadWithResponse(
                new BlobParallelUploadOptions(DATA.getDefaultInputStream()).setTags(tags), null, null);
        }

        for (PagedResponse<TaggedBlobItem> page : cc.findBlobsByTags(
            new FindBlobsOptions(String.format("\"%s\"='%s'", tagKey, tagValue)).setMaxResultsPerPage(pageResults),
            null, Context.NONE).iterableByPage()) {
            assertTrue(page.getValue().size() <= pageResults);
        }
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-04-10")
    @Test
    public void findBlobsMaxResultsByPage() {
        int numBlobs = 7;
        int pageResults = 3;
        Map<String, String> tags = Collections.singletonMap(tagKey, tagValue);

        for (int i = 0; i < numBlobs; i++) {
            cc.getBlobClient(generateBlobName()).uploadWithResponse(
                new BlobParallelUploadOptions(DATA.getDefaultInputStream()).setTags(tags), null, null);
        }

        for (PagedResponse<TaggedBlobItem> page : cc.findBlobsByTags(new FindBlobsOptions(String.format("\"%s\"='%s'",
            tagKey, tagValue)), null, Context.NONE).iterableByPage(pageResults)) {
            assertTrue(page.getValue().size() <= pageResults);
        }
    }

    @Test
    public void findBlobsError() {
        assertThrows(BlobStorageException.class, () -> cc.findBlobsByTags("garbageTag").streamByPage().count());
    }

    @SuppressWarnings("deprecation")
    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2021-04-10")
    @Test
    public void findBlobsWithTimeoutStillBackedByPagedFlux() {
        int numBlobs = 5;
        int pageResults = 3;
        Map<String, String> tags = Collections.singletonMap(tagKey, tagValue);

        for (int i = 0; i < numBlobs; i++) {
            cc.getBlobClient(generateBlobName()).uploadWithResponse(
                new BlobParallelUploadOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSize()).setTags(tags),
                null, null);
        }

        // when: "Consume results by page, still have paging functionality"
        assertDoesNotThrow(() -> cc.findBlobsByTags(new FindBlobsOptions(String.format("\"%s\"='%s'", tagKey, tagValue))
                .setMaxResultsPerPage(pageResults), Duration.ofSeconds(10), Context.NONE).streamByPage().count());
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
        AppendBlobClient bu2 = cc.getBlobClient(name).getAppendBlobClient();
        PageBlobClient bu3 = cc.getBlobClient(name + "2").getPageBlobClient();
        BlockBlobClient bu4 = cc.getBlobClient(name + "3").getBlockBlobClient();
        BlockBlobClient bu5 = cc.getBlobClient(name).getBlockBlobClient();

        assertResponseStatusCode(bu2.createWithResponse(null, null, null, null, null), 201);
        assertResponseStatusCode(bu5.getPropertiesWithResponse(null, null, null), 200);
        assertResponseStatusCode(bu3.createWithResponse(512, null, null, null,
            null, null, null), 201);
        assertResponseStatusCode(bu4.uploadWithResponse(DATA.getDefaultInputStream(), DATA.getDefaultDataSize(),
            null, null, null, null, null, null, null), 201);

        Iterator<BlobItem> blobs = cc.listBlobs().iterator();

        assertEquals(name, blobs.next().getName());
        assertEquals(name + "2", blobs.next().getName());
        assertEquals(name + "3", blobs.next().getName());
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
        AppendBlobClient bu2 = cc.getBlobClient(name).getAppendBlobClient();
        PageBlobClient bu3 = cc.getBlobClient(name + "2").getPageBlobClient();
        BlockBlobClient bu4 = cc.getBlobClient(name + "3").getBlockBlobClient();
        BlockBlobClient bu5 = cc.getBlobClient(name).getBlockBlobClient();

        assertResponseStatusCode(bu2.createWithResponse(null, null, null, null, null), 201);
        assertResponseStatusCode(bu5.getPropertiesWithResponse(null, null, null), 200);
        assertResponseStatusCode(bu3.createWithResponse(512, null, null, null, null, null, null), 201);
        assertResponseStatusCode(bu4.uploadWithResponse(DATA.getDefaultInputStream(), DATA.getDefaultDataSize(), null,
            null, null, null, null, null, null), 201);

        Iterator<BlobItem> blobs = cc.listBlobs().iterator();

        assertEquals(name, blobs.next().getName());
        assertEquals(name + "2", blobs.next().getName());
        assertEquals(name + "3", blobs.next().getName());
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
    public void createURLSpecialCharsDecoded(String name) {
        // This test checks that we handle blob names with encoded special characters correctly.
        String decodedName = Utility.urlDecode(name);
        AppendBlobClient bu2 = cc.getBlobClient(decodedName).getAppendBlobClient();
        PageBlobClient bu3 = cc.getBlobClient(decodedName + "2").getPageBlobClient();
        BlockBlobClient bu4 = cc.getBlobClient(decodedName + "3").getBlockBlobClient();
        BlockBlobClient bu5 = cc.getBlobClient(decodedName).getBlockBlobClient();

        assertResponseStatusCode(bu2.createWithResponse(null, null, null, null, null), 201);
        assertResponseStatusCode(bu5.getPropertiesWithResponse(null, null, null), 200);
        assertResponseStatusCode(bu3.createWithResponse(512, null, null, null, null, null, null), 201);
        assertResponseStatusCode(bu4.uploadWithResponse(DATA.getDefaultInputStream(), DATA.getDefaultDataSize(), null,
            null, null, null, null, null, null), 201);

        Iterator<BlobItem> blobs = cc.listBlobs().iterator();

        assertEquals(decodedName, blobs.next().getName());
        assertEquals(decodedName + "2", blobs.next().getName());
        assertEquals(decodedName + "3", blobs.next().getName());
    }

    @Test
    public void rootExplicit() {
        cc = primaryBlobServiceClient.getBlobContainerClient(BlobContainerClient.ROOT_CONTAINER_NAME);
        // create root container if not exist.
        if (!cc.exists()) {
            cc.create();
        }
        AppendBlobClient bu = cc.getBlobClient("rootblob").getAppendBlobClient();
        assertResponseStatusCode(bu.createWithResponse(null, null, null, null, null), 201);
    }

    @Test
    public void rootExplicitInEndpoint() {
        cc = primaryBlobServiceClient.getBlobContainerClient(BlobContainerClient.ROOT_CONTAINER_NAME);
        // create root container if not exist.
        if (!cc.exists()) {
            cc.create();
        }
        AppendBlobClient bu = cc.getBlobClient("rootblob").getAppendBlobClient();

        Response<AppendBlobItem> createResponse = bu.createWithResponse(null, null, null, null, null);
        Response<BlobProperties> propsResponse = bu.getPropertiesWithResponse(null, null, null);

        assertResponseStatusCode(createResponse, 201);
        assertResponseStatusCode(propsResponse, 200);
        assertEquals(BlobType.APPEND_BLOB, propsResponse.getValue().getBlobType());
    }

    @Test
    public void blobClientBuilderRootImplicit() {
        cc = primaryBlobServiceClient.getBlobContainerClient(BlobContainerClient.ROOT_CONTAINER_NAME);
        // createroot container if not exist.
        if (!cc.exists()) {
            cc.create();
        }

        AppendBlobClient bc = instrument(new BlobClientBuilder()
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential())
            .endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint())
            .blobName("rootblob"))
            .buildClient().getAppendBlobClient();

        Response<AppendBlobItem> createResponse = bc.createWithResponse(null, null, null, null, null);

        Response<BlobProperties> propsResponse = bc.getPropertiesWithResponse(null, null, null);

        assertResponseStatusCode(createResponse, 201);
        assertResponseStatusCode(propsResponse, 200);
        assertEquals(BlobType.APPEND_BLOB, propsResponse.getValue().getBlobType());
    }

    @Test
    public void containerClientBuilderRootImplicit() {
        cc = primaryBlobServiceClient.getBlobContainerClient(BlobContainerClient.ROOT_CONTAINER_NAME);
        // create root container if not exist.
        if (!cc.exists()) {
            cc.create();
        }

        cc = instrument(new BlobContainerClientBuilder()
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential())
            .endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint())
            .containerName(null))
            .buildClient();

        assertNotNull(cc.getProperties());
        assertEquals(BlobContainerAsyncClient.ROOT_CONTAINER_NAME, cc.getBlobContainerName());

        AppendBlobClient bc = cc.getBlobClient("rootblob").getAppendBlobClient();
        bc.create(true);

        assertTrue(bc.exists());
    }

    @Test
    public void serviceClientImplicitRoot() {
        assertEquals(primaryBlobServiceClient.getBlobContainerClient(null).getBlobContainerName(),
            BlobContainerAsyncClient.ROOT_CONTAINER_NAME);
        assertEquals(primaryBlobServiceClient.getBlobContainerClient("").getBlobContainerName(),
            BlobContainerAsyncClient.ROOT_CONTAINER_NAME);
    }

    @Test
    public void webContainer() {
        cc = primaryBlobServiceClient.getBlobContainerClient(BlobContainerClient.STATIC_WEBSITE_CONTAINER_NAME);
        // createroot container if not exist.
        try {
            cc.create();
        } catch (BlobStorageException se) {
            if (se.getErrorCode() != BlobErrorCode.CONTAINER_ALREADY_EXISTS) {
                throw se;
            }
        }

        BlobContainerClient webContainer = primaryBlobServiceClient.getBlobContainerClient(
            BlobContainerClient.STATIC_WEBSITE_CONTAINER_NAME);

        // Validate some basic operation.
        assertDoesNotThrow(() -> webContainer.setAccessPolicy(null, null));
    }

    @Test
    public void getAccountInfo() {
        Response<StorageAccountInfo> response = primaryBlobServiceClient.getAccountInfoWithResponse(null, null);
        assertNotNull(response.getHeaders().getValue(HttpHeaderName.DATE));
        assertNotNull(response.getHeaders().getValue(X_MS_VERSION));
        assertNotNull(response.getHeaders().getValue(X_MS_REQUEST_ID));
        assertNotNull(response.getValue().getAccountKind());
        assertNotNull(response.getValue().getSkuName());
    }

    @Test
    public void getAccountInfoMin() {
        assertResponseStatusCode(primaryBlobServiceClient.getAccountInfoWithResponse(null, null), 200);
    }

    @Test
    public void getAccountInfoBase() {
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName());
        StorageAccountInfo info = cc.getAccountInfo(null);

        assertNotNull(info.getAccountKind());
        assertNotNull(info.getSkuName());
        assertFalse(info.isHierarchicalNamespaceEnabled());
    }

    @Test
    public void getAccountInfoBaseFail() {
        BlobServiceClient serviceClient = instrument(new BlobServiceClientBuilder()
            .endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint())
            .credential(new MockTokenCredential()))
            .buildClient();

        BlobContainerClient containerClient = serviceClient.getBlobContainerClient(generateContainerName());

        BlobStorageException e = assertThrows(BlobStorageException.class, () -> containerClient.getAccountInfo(null));
        assertEquals(BlobErrorCode.INVALID_AUTHENTICATION_INFO, e.getErrorCode());

    }

    @Test
    public void getContainerName() {
        String containerName = generateContainerName();
        BlobContainerClient newcc = primaryBlobServiceClient.getBlobContainerClient(containerName);
        assertEquals(containerName, newcc.getBlobContainerName());
    }

    @Test
    public void builderCpkValidation() {
        URL endpoint = BlobUrlParts.parse(cc.getBlobContainerUrl()).setScheme("http").toUrl();
        BlobContainerClientBuilder builder = new BlobContainerClientBuilder()
            .customerProvidedKey(new CustomerProvidedKey(Base64.getEncoder().encodeToString(getRandomByteArray(256))))
            .endpoint(endpoint.toString());

        assertThrows(IllegalArgumentException.class, builder::buildClient);
    }

    @Test
    public void builderBearerTokenValidation() {
        URL endpoint = BlobUrlParts.parse(cc.getBlobContainerUrl()).setScheme("http").toUrl();
        BlobContainerClientBuilder builder = new BlobContainerClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint.toString());

        assertThrows(IllegalArgumentException.class, builder::buildClient);
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    @Test
    public void perCallPolicy() {
        BlobContainerClient containerClient = getContainerClientBuilder(cc.getBlobContainerUrl())
            .credential(ENVIRONMENT.getPrimaryAccount().getCredential())
            .addPolicy(getPerCallVersionPolicy())
            .buildClient();

        Response<BlobContainerProperties> response = containerClient.getPropertiesWithResponse(null, null, null);
        assertEquals("2017-11-09", response.getHeaders().getValue(X_MS_VERSION));
    }

    @Test
    public void defaultAudience() {
        BlobContainerClient aadContainer = getContainerClientBuilderWithTokenCredential(cc.getBlobContainerUrl())
            .audience(null)
            .buildClient();

        assertTrue(aadContainer.exists());
    }

    @Test
    public void storageAccountAudience() {
        BlobContainerClient aadContainer = getContainerClientBuilderWithTokenCredential(cc.getBlobContainerUrl())
            .audience(BlobAudience.createBlobServiceAccountAudience(cc.getAccountName()))
            .buildClient();

        assertTrue(aadContainer.exists());
    }

    @RequiredServiceVersion(clazz = BlobServiceVersion.class, min = "2024-08-04")
    @LiveOnly
    @Test
    /* This test tests if the bearer challenge is working properly. A bad audience is passed in, the service returns
    the default audience, and the request gets retried with this default audience, making the call function as expected.
     */
    public void audienceErrorBearerChallengeRetry() {
        BlobContainerClient aadContainer = getContainerClientBuilderWithTokenCredential(cc.getBlobContainerUrl())
                .audience(BlobAudience.createBlobServiceAccountAudience("badAudience"))
                .buildClient();

        assertNotNull(aadContainer.getProperties());
    }

    @Test
    public void audienceFromString() {
        String url = String.format("https://%s.blob.core.windows.net/", cc.getAccountName());
        BlobAudience audience = BlobAudience.fromString(url);

        BlobContainerClient aadContainer = getContainerClientBuilderWithTokenCredential(cc.getBlobContainerUrl())
            .audience(audience)
            .buildClient();

        assertTrue(aadContainer.exists());
    }

// TODO: Reintroduce these tests once service starts supporting it.

//    public void Rename() {
//        setup:
//        def newName = generateContainerName()
//
//        when:
//        def renamedContainer = cc.rename(newName)
//
//        then:
//        renamedContainer.getPropertiesWithResponse(null, null, null).getStatusCode() == 200
//
//        cleanup:
//        renamedContainer.delete()
//    }

//    public void Rename sas() {
//        setup:
//        def newName = generateContainerName()
//        def service = new AccountSasService()
//            .setBlobAccess(true)
//        def resourceType = new AccountSasResourceType()
//            .setContainer(true)
//            .setService(true)
//            .setObject(true)
//        def expiryTime = testResourceNamer.now().plusDays(1)
//        def permissions = new AccountSasPermission()
//            .setReadPermission(true)
//            .setWritePermission(true)
//            .setCreatePermission(true)
//            .setDeletePermission(true)
//
//        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType)
//        def sas = primaryBlobServiceClient.generateAccountSas(sasValues)
//        def sasClient = getContainerClient(sas, cc.getBlobContainerUrl())
//
//        when:
//        def renamedContainer = sasClient.rename(newName)
//
//        then:
//        renamedContainer.getPropertiesWithResponse(null, null, null).getStatusCode() == 200
//
//        cleanup:
//        renamedContainer.delete()
//    }

//    @ParameterizedTest
//    public void Rename AC() {
//        setup:
//        leaseID = setupContainerLeaseCondition(cc, leaseID)
//        BlobRequestConditions cac = new BlobRequestConditions()
//            .setLeaseId(leaseID)
//
//        expect:
//        cc.renameWithResponse(new BlobContainerRenameOptions(generateContainerName()).setRequestConditions(cac),
//            null, null).getStatusCode() == 200
//
//        where:
//        leaseID         || _
//        null            || _
//        receivedLeaseID || _
//    }

//    @ParameterizedTest
//    public void Rename AC fail() {
//        setup:
//        BlobRequestConditions cac = new BlobRequestConditions()
//            .setLeaseId(leaseID)
//
//        when:
//        cc.renameWithResponse(new BlobContainerRenameOptions(generateContainerName()).setRequestConditions(cac),
//            null, null)
//
//        then:
//        assertThrows(BlobStorageException.class, () ->
//
//        where:
//        leaseID         || _
//        garbageLeaseID  || _
//    }

//    @ParameterizedTest
//    public void Rename AC illegal() {
//        setup:
//        def ac = new BlobRequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch).setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified).setTagsConditions(tags)
//
//        when:
//        cc.renameWithResponse(new BlobContainerRenameOptions(generateContainerName()).setRequestConditions(ac),
//            null, null)
//
//        then:
//        assertThrows(UnsupportedOperationException.class, () ->
//
//        where:
//        modified | unmodified | match        | noneMatch    | tags
//        oldDate  | null       | null         | null         | null
//        null     | newDate    | null         | null         | null
//        null     | null       | receivedEtag | null         | null
//        null     | null       | null         | garbageEtag  | null
//        null     | null       | null         | null         | "tags"
//    }

//    public void Rename error() {
//        setup:
//        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName())
//        def newName = generateContainerName()
//
//        when:
//        cc.rename(newName)
//
//        then:
//        assertThrows(BlobStorageException.class, () ->
//    }
}
