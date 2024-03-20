// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.util.CoreUtils;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.common.Utility;
import com.azure.storage.file.datalake.models.DataLakeAccessPolicy;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeSignedIdentifier;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.LeaseDurationType;
import com.azure.storage.file.datalake.models.LeaseStateType;
import com.azure.storage.file.datalake.models.LeaseStatusType;
import com.azure.storage.file.datalake.models.ListPathsOptions;
import com.azure.storage.file.datalake.models.PathAccessControlEntry;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathPermissions;
import com.azure.storage.file.datalake.models.PublicAccessType;
import com.azure.storage.file.datalake.options.DataLakePathCreateOptions;
import com.azure.storage.file.datalake.options.DataLakePathDeleteOptions;
import com.azure.storage.file.datalake.options.DataLakePathScheduleDeletionOptions;
import com.azure.storage.file.datalake.options.FileScheduleDeletionOptions;
import com.azure.storage.file.datalake.options.FileSystemEncryptionScopeOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileSystemAsyncApiTests extends DataLakeTestBase {

    @Test
    public void createAllNull() {
        // Overwrite the existing dataLakeFileSystemClient, which has already been created
        dataLakeFileSystemAsyncClient = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());

        StepVerifier.create(dataLakeFileSystemAsyncClient.createWithResponse(null, null))
            .assertNext(r -> {
                assertEquals(201, r.getStatusCode());
                validateBasicHeaders(r.getHeaders());
            })
            .verifyComplete();
    }

    @Test
    public void createMin() {
        StepVerifier.create(primaryDataLakeServiceAsyncClient.createFileSystem(generateFileSystemName()))
            .assertNext(r -> assertNotNull(r.getProperties()))
            .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz", "testFoo,testBar,testFizz,testBuzz"},
        nullValues = "null")
    public void createMetadata(String key1, String value1, String key2, String value2) {
        dataLakeFileSystemAsyncClient = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }

        dataLakeFileSystemAsyncClient.createWithResponse(metadata, null).block();

        StepVerifier.create(dataLakeFileSystemAsyncClient.getPropertiesWithResponse(null))
            .assertNext(r -> assertEquals(metadata, r.getValue().getMetadata()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("publicAccessSupplier")
    public void createPublicAccess(PublicAccessType publicAccess) {
        dataLakeFileSystemAsyncClient = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());

        dataLakeFileSystemAsyncClient.createWithResponse(null, publicAccess).block();

        StepVerifier.create(dataLakeFileSystemAsyncClient.getProperties())
            .assertNext(p -> assertEquals(publicAccess, p.getDataLakePublicAccess()))
            .verifyComplete();
    }

    private static Stream<PublicAccessType> publicAccessSupplier() {
        return Stream.of(PublicAccessType.BLOB, PublicAccessType.CONTAINER, null);
    }

    @Test
    public void createError() {
        StepVerifier.create(dataLakeFileSystemAsyncClient.create())
            .verifyErrorSatisfies(r -> {
                DataLakeStorageException e = assertInstanceOf(DataLakeStorageException.class, r);
                assertEquals(409, e.getStatusCode());
                assertEquals(BlobErrorCode.CONTAINER_ALREADY_EXISTS.toString(), e.getErrorCode());
            });
    }

    @Test
    public void createEncryptionScope() {
        FileSystemEncryptionScopeOptions encryptionScope = new FileSystemEncryptionScopeOptions()
            .setDefaultEncryptionScope(ENCRYPTION_SCOPE_STRING)
            .setEncryptionScopeOverridePrevented(true);

        dataLakeFileSystemAsyncClient = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());

        DataLakeFileSystemAsyncClient client = getFileSystemClientBuilder(dataLakeFileSystemAsyncClient.getFileSystemUrl())
            .credential(getDataLakeCredential())
            .fileSystemEncryptionScopeOptions(encryptionScope)
            .buildAsyncClient();

        client.create().block();
        StepVerifier.create(client.getProperties())
            .assertNext(p -> {
                assertEquals(ENCRYPTION_SCOPE_STRING, p.getEncryptionScope());
                assertTrue(p.isEncryptionScopeOverridePrevented());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz", "testFoo,testBar,testFizz,testBuzz"},
        nullValues = "null")
    public void createMetadataEncryptionScope(String key1, String value1, String key2, String value2) {
        FileSystemEncryptionScopeOptions encryptionScope = new FileSystemEncryptionScopeOptions()
            .setDefaultEncryptionScope(ENCRYPTION_SCOPE_STRING)
            .setEncryptionScopeOverridePrevented(true);

        dataLakeFileSystemAsyncClient = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());

        DataLakeFileSystemAsyncClient client = getFileSystemClientBuilder(dataLakeFileSystemAsyncClient.getFileSystemUrl())
            .credential(getDataLakeCredential())
            .fileSystemEncryptionScopeOptions(encryptionScope)
            .buildAsyncClient();

        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }

        client.createWithResponse(metadata, null).block();
        StepVerifier.create(client.getProperties())
            .assertNext(p -> {
                assertEquals(ENCRYPTION_SCOPE_STRING, p.getEncryptionScope());
                assertTrue(p.isEncryptionScopeOverridePrevented());
                assertEquals(metadata, p.getMetadata());
            })
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsAllNull() {
        // Overwrite the existing dataLakeFileSystemClient, which has already been created
        dataLakeFileSystemAsyncClient = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());

        StepVerifier.create(dataLakeFileSystemAsyncClient.createIfNotExistsWithResponse(null, null))
            .assertNext(r -> {
                assertEquals(201, r.getStatusCode());
                validateBasicHeaders(r.getHeaders());
            })
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsMin() {
        dataLakeFileSystemAsyncClient = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());

        StepVerifier.create(dataLakeFileSystemAsyncClient.createIfNotExists())
            .expectNext(true)
            .verifyComplete();

        StepVerifier.create(dataLakeFileSystemAsyncClient.exists())
            .expectNext(true)
            .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz", "testFoo,testBar,testFizz,testBuzz"},
        nullValues = "null")
    public void createIfNotExistsMetadata(String key1, String value1, String key2, String value2) {
        dataLakeFileSystemAsyncClient = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }

        dataLakeFileSystemAsyncClient.createIfNotExistsWithResponse(metadata, null).block();
        StepVerifier.create(dataLakeFileSystemAsyncClient.getPropertiesWithResponse(null))
            .assertNext(r -> assertEquals(metadata, r.getValue().getMetadata()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("publicAccessSupplier")
    public void createIfNotExistsPublicAccess(PublicAccessType publicAccess) {
        dataLakeFileSystemAsyncClient = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());

        dataLakeFileSystemAsyncClient.createIfNotExistsWithResponse(null, publicAccess).block();

        StepVerifier.create(dataLakeFileSystemAsyncClient.getProperties())
            .assertNext(p -> assertEquals(publicAccess, p.getDataLakePublicAccess()))
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsOnFileSystemThatAlreadyExists() {
        DataLakeFileSystemAsyncClient dataLakeFileSystemAsyncClient =
            primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.createIfNotExistsWithResponse(null, null), 201);
        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.createIfNotExistsWithResponse(null, null), 409);
    }

    @Test
    public void createIfNotExistsEncryptionScope() {
        FileSystemEncryptionScopeOptions encryptionScope = new FileSystemEncryptionScopeOptions()
            .setDefaultEncryptionScope(ENCRYPTION_SCOPE_STRING)
            .setEncryptionScopeOverridePrevented(true);

        dataLakeFileSystemAsyncClient = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());

        DataLakeFileSystemAsyncClient client = getFileSystemClientBuilder(dataLakeFileSystemAsyncClient.getFileSystemUrl())
            .credential(getDataLakeCredential())
            .fileSystemEncryptionScopeOptions(encryptionScope)
            .buildAsyncClient();

        client.createIfNotExists().block();
        StepVerifier.create(client.getProperties())
            .assertNext(p -> {
                assertEquals(ENCRYPTION_SCOPE_STRING, p.getEncryptionScope());
                assertTrue(p.isEncryptionScopeOverridePrevented());
            })
            .verifyComplete();
    }

    @Test
    public void getPropertiesNull() {
        StepVerifier.create(dataLakeFileSystemAsyncClient.getPropertiesWithResponse(null))
            .assertNext(r -> {
                validateBasicHeaders(r.getHeaders());
                assertNull(r.getValue().getDataLakePublicAccess());
                assertFalse(r.getValue().hasImmutabilityPolicy());
                assertFalse(r.getValue().hasLegalHold());
                assertNull(r.getValue().getLeaseDuration());
                assertEquals(LeaseStateType.AVAILABLE, r.getValue().getLeaseState());
                assertEquals(LeaseStatusType.UNLOCKED, r.getValue().getLeaseStatus());
                assertEquals(0, r.getValue().getMetadata().size());
            })
            .verifyComplete();
    }

    @Test
    public void getPropertiesMin() {
        assertNotNull(dataLakeFileSystemAsyncClient.getProperties().block());
    }

    @Test
    public void getPropertiesLease() {
        String leaseID = setupFileSystemLeaseAsyncCondition(dataLakeFileSystemAsyncClient, RECEIVED_LEASE_ID);

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.getPropertiesWithResponse(leaseID), 200);
    }

    @Test
    public void getPropertiesLeaseFail() {
        StepVerifier.create(dataLakeFileSystemAsyncClient.getPropertiesWithResponse("garbage"))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void getPropertiesError() {
        dataLakeFileSystemAsyncClient = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());

        StepVerifier.create(dataLakeFileSystemAsyncClient.getProperties())
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void exists() {
        dataLakeFileSystemAsyncClient = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());
        dataLakeFileSystemAsyncClient.create().block();

        StepVerifier.create(dataLakeFileSystemAsyncClient.exists())
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void existsNotExists() {
        dataLakeFileSystemAsyncClient = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());

        StepVerifier.create(dataLakeFileSystemAsyncClient.exists())
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    public void setMetadata() {
        dataLakeFileSystemAsyncClient = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());
        Map<String, String> metadata = Collections.singletonMap("key", "value");
        dataLakeFileSystemAsyncClient.createWithResponse(metadata, null).block();

        StepVerifier.create(dataLakeFileSystemAsyncClient.setMetadataWithResponse(null, null))
            .assertNext(r -> {
                assertEquals(200, r.getStatusCode());
                validateBasicHeaders(r.getHeaders());
            })
            .verifyComplete();

        StepVerifier.create(dataLakeFileSystemAsyncClient.getProperties())
            .assertNext(p -> assertEquals(0, p.getMetadata().size()))
            .verifyComplete();
    }

    @Test
    public void setMetadataMin() {
        Map<String, String> metadata = Collections.singletonMap("foo", "bar");

        dataLakeFileSystemAsyncClient.setMetadata(metadata).block();

        StepVerifier.create(dataLakeFileSystemAsyncClient.getProperties())
            .assertNext(p -> assertEquals(metadata, p.getMetadata()))
            .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz"}, nullValues = "null")
    public void setMetadataMetadata(String key1, String value1, String key2, String value2) {
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.setMetadataWithResponse(metadata, null), 200);

        StepVerifier.create(dataLakeFileSystemAsyncClient.getProperties())
            .assertNext(p -> assertEquals(metadata, p.getMetadata()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("setMetadataACSupplier")
    public void setMetadataAC(OffsetDateTime modified, String leaseID) {
        leaseID = setupFileSystemLeaseAsyncCondition(dataLakeFileSystemAsyncClient, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions().setLeaseId(leaseID).setIfModifiedSince(modified);

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.setMetadataWithResponse(null, drc), 200);
    }

    private static Stream<Arguments> setMetadataACSupplier() {
        return Stream.of(
            // modified | leaseID
            Arguments.of(null, null),
            Arguments.of(OLD_DATE, null),
            Arguments.of(null, RECEIVED_LEASE_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("setMetadataACFailSupplier")
    public void setMetadataACFail(OffsetDateTime modified, String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified);

        StepVerifier.create(dataLakeFileSystemAsyncClient.setMetadataWithResponse(null, drc))
            .verifyError(DataLakeStorageException.class);
    }

    private static Stream<Arguments> setMetadataACFailSupplier() {
        // modified | leaseID
        return Stream.of(Arguments.of(NEW_DATE, null), Arguments.of(null, GARBAGE_LEASE_ID));
    }

    @ParameterizedTest
    @MethodSource("setMetadataACIllegalSupplier")
    public void setMetadataACIllegal(OffsetDateTime unmodified, String match, String noneMatch) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(dataLakeFileSystemAsyncClient.setMetadataWithResponse(null, drc))
            .verifyError(UnsupportedOperationException.class);
    }

    private static Stream<Arguments> setMetadataACIllegalSupplier() {
        return Stream.of(
            // unmodified | match | noneMatch
            Arguments.of(NEW_DATE, null, null),
            Arguments.of(null, RECEIVED_ETAG, null),
            Arguments.of(null, null, GARBAGE_ETAG)
        );
    }

    @Test
    public void setMetadataError() {
        dataLakeFileSystemAsyncClient = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());

        StepVerifier.create(dataLakeFileSystemAsyncClient.setMetadata(null))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void delete() {
        StepVerifier.create(dataLakeFileSystemAsyncClient.deleteWithResponse(null))
            .assertNext(r -> {
                assertEquals(202, r.getStatusCode());
                assertNotNull(r.getHeaders().getValue(X_MS_REQUEST_ID));
                assertNotNull(r.getHeaders().getValue(X_MS_VERSION));
                assertNotNull(r.getHeaders().getValue(HttpHeaderName.DATE));
            })
            .verifyComplete();
    }

    @Test
    public void deleteMin() {
        dataLakeFileSystemAsyncClient.delete().block();

        StepVerifier.create(dataLakeFileSystemAsyncClient.getProperties())
            .verifyErrorSatisfies(r -> {
                DataLakeStorageException e = assertInstanceOf(DataLakeStorageException.class, r);
                assertEquals(404, e.getStatusCode());
                assertEquals(BlobErrorCode.CONTAINER_NOT_FOUND.toString(), e.getErrorCode());
                assertTrue(e.getServiceMessage().contains("The specified container does not exist."));
            });
    }

    @ParameterizedTest
    @MethodSource("modifiedAndLeaseIdSupplier")
    public void deleteAC(OffsetDateTime modified, OffsetDateTime unmodified, String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupFileSystemLeaseAsyncCondition(dataLakeFileSystemAsyncClient, leaseID))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.deleteWithResponse(drc), 202);
    }

    private static Stream<Arguments> modifiedAndLeaseIdSupplier() {
        return Stream.of(
            // modified | unmodified | leaseID
            Arguments.of(null, null, null),
            Arguments.of(OLD_DATE, null, null),
            Arguments.of(null, NEW_DATE, null),
            Arguments.of(null, null, RECEIVED_LEASE_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedAndLeaseIdSupplier")
    public void deleteACFail(OffsetDateTime modified, OffsetDateTime unmodified, String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(dataLakeFileSystemAsyncClient.deleteWithResponse(drc))
            .verifyError(DataLakeStorageException.class);
    }

    private static Stream<Arguments> invalidModifiedAndLeaseIdSupplier() {
        return Stream.of(
            // modified | unmodified | leaseID
            Arguments.of(NEW_DATE, null, null),
            Arguments.of(null, OLD_DATE, null),
            Arguments.of(null, null, GARBAGE_LEASE_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidMatchSupplier")
    public void deleteACIllegal(String match, String noneMatch) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        StepVerifier.create((dataLakeFileSystemAsyncClient.deleteWithResponse(drc)))
            .verifyError(UnsupportedOperationException.class);
    }

    private static Stream<Arguments> invalidMatchSupplier() {
        return Stream.of(
            // match | noneMatch
            Arguments.of(RECEIVED_ETAG, null),
            Arguments.of(null, GARBAGE_ETAG)
        );
    }

    @Test
    public void deleteError() {
        dataLakeFileSystemAsyncClient = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());

        StepVerifier.create(dataLakeFileSystemAsyncClient.delete())
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void deleteIfExists() {
        DataLakeFileSystemAsyncClient dataLakeFileSystemAsyncClient = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());
        dataLakeFileSystemAsyncClient.create().block();

        StepVerifier.create(dataLakeFileSystemAsyncClient.deleteIfExistsWithResponse(null))
            .assertNext(r -> {
                assertEquals(202, r.getStatusCode());
                assertNotNull(r.getHeaders().getValue(X_MS_REQUEST_ID));
                assertNotNull(r.getHeaders().getValue(X_MS_VERSION));
                assertNotNull(r.getHeaders().getValue(HttpHeaderName.DATE));
            })
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsMin() {
        DataLakeFileSystemAsyncClient dataLakeFileSystemAsyncClient = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());
        dataLakeFileSystemAsyncClient.create().block();

        StepVerifier.create(dataLakeFileSystemAsyncClient.exists())
            .expectNext(true)
            .verifyComplete();

        StepVerifier.create(dataLakeFileSystemAsyncClient.deleteIfExists())
            .expectNext(true)
            .verifyComplete();

        StepVerifier.create(dataLakeFileSystemAsyncClient.getProperties())
            .verifyError(DataLakeStorageException.class);

        StepVerifier.create(dataLakeFileSystemAsyncClient.exists())
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsOnFileSystemThatDoesNotExist() {
        DataLakeFileSystemAsyncClient dataLakeFileSystemAsyncClient = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.deleteIfExistsWithResponse(null), 404);

        StepVerifier.create(dataLakeFileSystemAsyncClient.exists())
            .expectNext(false)
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("modifiedAndLeaseIdSupplier")
    public void deleteIfExistsAC(OffsetDateTime modified, OffsetDateTime unmodified, String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupFileSystemLeaseAsyncCondition(dataLakeFileSystemAsyncClient, leaseID))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.deleteIfExistsWithResponse(
            new DataLakePathDeleteOptions().setRequestConditions(drc)), 202);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedAndLeaseIdSupplier")
    public void deleteIfExistsACFail(OffsetDateTime modified, OffsetDateTime unmodified, String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(dataLakeFileSystemAsyncClient.deleteIfExistsWithResponse(
            new DataLakePathDeleteOptions().setRequestConditions(drc)))
            .verifyError(DataLakeStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("invalidMatchSupplier")
    public void deleteIfExistsACIllegal(String match, String noneMatch) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        StepVerifier.create(dataLakeFileSystemAsyncClient.deleteIfExistsWithResponse(new DataLakePathDeleteOptions()
            .setRequestConditions(drc)))
            .verifyError(UnsupportedOperationException.class);
    }

    @Test
    public void createFileMin() {
        assertDoesNotThrow(() -> dataLakeFileSystemAsyncClient.createFile(generatePathName()).block());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void createFileOverwrite(boolean overwrite) {
        String pathName = generatePathName();
        dataLakeFileSystemAsyncClient.createFile(pathName).block();

        if (overwrite) {
            assertDoesNotThrow(() -> dataLakeFileSystemAsyncClient.createFile(pathName, true).block());
        } else {
            StepVerifier.create(dataLakeFileSystemAsyncClient.createFile(pathName, false))
                .verifyError(DataLakeStorageException.class);
        }
    }

    @Test
    public void createFileDefaults() {
        StepVerifier.create(dataLakeFileSystemAsyncClient.createFileWithResponse(generatePathName(), null, null,
            null, null, null))
            .assertNext(r -> {
                assertEquals(201, r.getStatusCode());
                validateBasicHeaders(r.getHeaders());
            })
            .verifyComplete();
    }

    @Test
    public void createFileError() {
        StepVerifier.create(dataLakeFileSystemAsyncClient.createFileWithResponse(
            generatePathName(), null, null, null, null,
            new DataLakeRequestConditions().setIfMatch("garbage")))
            .verifyError(DataLakeStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("cacheAndContentSupplier")
    public void createFileHeaders(String cacheControl, String contentDisposition, String contentEncoding,
                                  String contentLanguage, String contentType) {
        // Create does not set md5
        PathHttpHeaders headers = new PathHttpHeaders()
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType);

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;
        String finalContentType = contentType;

        StepVerifier.create(dataLakeFileSystemAsyncClient.createFileWithResponse(generatePathName(),
            null, null, headers, null, null)
            .flatMap(r -> r.getValue().getPropertiesWithResponse(null)))
            .assertNext(p -> validatePathProperties(p, cacheControl, contentDisposition, contentEncoding, contentLanguage,
                null, finalContentType))
            .verifyComplete();
    }

    private static Stream<Arguments> cacheAndContentSupplier() {
        return Stream.of(
            // cacheControl | contentDisposition | contentEncoding | contentLanguage | contentType
            Arguments.of(null, null, null, null, null),
            Arguments.of("control", "disposition", "encoding", "language", "type")
        );
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz"}, nullValues = "null")
    public void createFileMetadata(String key1, String value1, String key2, String value2) {
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }

        StepVerifier.create(dataLakeFileSystemAsyncClient.createFileWithResponse(generatePathName(), null, null, null,
            metadata, null)
            .flatMap(r -> r.getValue().getProperties()))
            .assertNext(p -> assertEquals(metadata, p.getMetadata()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void createFileAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                             String leaseID) {
        String pathName = generatePathName();
        DataLakeFileAsyncClient client = dataLakeFileSystemAsyncClient.getFileAsyncClient(pathName);
        client.create().block();
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(client, leaseID))
            .setIfMatch(setupPathMatchCondition(client, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.createFileWithResponse(pathName, null, null, null, null, drc), 201);
    }

    private static Stream<Arguments> modifiedMatchAndLeaseIdSupplier() {
        return Stream.of(
            // modified | unmodified | match | noneMatch | leaseID
            Arguments.of(null, null, null, null, null),
            Arguments.of(OLD_DATE, null, null, null, null),
            Arguments.of(null, NEW_DATE, null, null, null),
            Arguments.of(null, null, RECEIVED_ETAG, null, null),
            Arguments.of(null, null, null, GARBAGE_ETAG, null),
            Arguments.of(null, null, null, null, RECEIVED_LEASE_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void createFileACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                 String leaseID) {
        String pathName = generatePathName();
        DataLakeFileAsyncClient client = dataLakeFileSystemAsyncClient.getFileAsyncClient(pathName);
        client.create().block();
        setupPathLeaseCondition(client, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(client, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(dataLakeFileSystemAsyncClient.createFileWithResponse(pathName,
            null, null, null, null, drc))
            .verifyError(DataLakeStorageException.class);
    }

    private static Stream<Arguments> invalidModifiedMatchAndLeaseIdSupplier() {
        return Stream.of(
            // modified | unmodified | match | noneMatch | leaseID
            Arguments.of(NEW_DATE, null, null, null, null),
            Arguments.of(null, OLD_DATE, null, null, null),
            Arguments.of(null, null, GARBAGE_ETAG, null, null),
            Arguments.of(null, null, null, RECEIVED_ETAG, null),
            Arguments.of(null, null, null, null, GARBAGE_LEASE_ID)
        );
    }

    @Test
    public void createFilePermissionsAndUmask() {
        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.createFileWithResponse(generatePathName(), "0777", "0057", null,
            null, null), 201);
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
    @Test
    public void createFileOptionsWithACL() {
        List<PathAccessControlEntry> pathAccessControlEntries = PathAccessControlEntry.parseList("user::rwx,group::r--,other::---,mask::rwx");
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setAccessControlList(pathAccessControlEntries);
        DataLakeFileAsyncClient client = dataLakeFileSystemAsyncClient.createFileWithResponse(generatePathName(), options).block().getValue();

        StepVerifier.create(client.getAccessControl())
            .assertNext(r -> {
                assertEquals(pathAccessControlEntries.get(0), r.getAccessControlList().get(0)); // testing if owner is set the same
                assertEquals(pathAccessControlEntries.get(1), r.getAccessControlList().get(1)); // testing if group is set the same
            })
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
    @Test
    public void createFileOptionsWithOwnerAndGroup() {
        String ownerName = testResourceNamer.randomUuid();
        String groupName = testResourceNamer.randomUuid();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setOwner(ownerName)
            .setGroup(groupName);

        StepVerifier.create(dataLakeFileSystemAsyncClient.createFileWithResponse(generatePathName(), options)
            .flatMap(r -> r.getValue().getAccessControl()))
            .assertNext(acl -> {
                assertEquals(ownerName, acl.getOwner());
                assertEquals(groupName, acl.getGroup());
            })
            .verifyComplete();
    }

    @Test
    public void createFileOptionsWithNullOwnerAndGroup() {
        StepVerifier.create(dataLakeFileSystemAsyncClient.createFileWithResponse(generatePathName(), new DataLakePathCreateOptions())
            .flatMap(r -> r.getValue().getAccessControl()))
            .assertNext(acl -> {
                assertEquals("$superuser", acl.getOwner());
                assertEquals("$superuser", acl.getGroup());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("cacheAndContentWithMd5Supplier")
    public void createFileOptionsWithPathHttpHeaders(String cacheControl, String contentDisposition,
                                                     String contentEncoding, String contentLanguage, byte[] contentMD5, String contentType) {
        PathHttpHeaders putHeaders = new PathHttpHeaders()
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType);

        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setPathHttpHeaders(putHeaders);
        StepVerifier.create(dataLakeFileSystemAsyncClient.createFileWithResponse(generatePathName(), options)
            .flatMap(r -> r.getValue().getPropertiesWithResponse(null)))
            .assertNext(p -> validatePathProperties(p, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMD5, contentType))
            .verifyComplete();
    }

    private static Stream<Arguments> cacheAndContentWithMd5Supplier() {
        return Stream.of(
            // cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5 | contentType
            Arguments.of(null, null, null, null, null, "application/octet-stream"),
            Arguments.of("control", "disposition", "encoding", "language", null, "type")
        );
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz"}, nullValues = "null")
    public void createFileOptionsWithMetadata(String key1, String value1, String key2, String value2) {
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2);
        }
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setMetadata(metadata);

        StepVerifier.create(dataLakeFileSystemAsyncClient.createFileWithResponse(generatePathName(), options)
            .flatMap(r -> {
                assertEquals(201, r.getStatusCode());
                return r.getValue().getProperties();
            }))
            .assertNext(p -> {
                for (String k : metadata.keySet()) {
                    assertTrue(p.getMetadata().containsKey(k));
                    assertEquals(metadata.get(k), p.getMetadata().get(k));
                }
            })
            .verifyComplete();
    }

    @Test
    public void createFileOptionsWithPermissionsAndUmask() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setPermissions("0777")
            .setUmask("0057");

        StepVerifier.create(dataLakeFileSystemAsyncClient.createFileWithResponse(generatePathName(), options)
            .flatMap(r -> r.getValue().getAccessControlWithResponse(true, null)))
            .assertNext(acl -> assertEquals(PathPermissions.parseSymbolic("rwx-w----").toString(), acl.getValue().getPermissions().toString()))
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
    @Test
    public void createFileOptionsWithLeaseId() {
        String leaseId = CoreUtils.randomUuid().toString();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setProposedLeaseId(leaseId).setLeaseDuration(15);

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.createFileWithResponse(generatePathName(), options), 201);
    }

    @Test
    public void createFileOptionsWithLeaseIdError() {
        String leaseId = CoreUtils.randomUuid().toString();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setProposedLeaseId(leaseId);

        // lease duration must also be set, or else exception is thrown
        StepVerifier.create(dataLakeFileSystemAsyncClient.createFileWithResponse(generatePathName(), options))
            .verifyError(DataLakeStorageException.class);
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
    @Test
    public void createFileOptionsWithLeaseDuration() {
        String leaseId = CoreUtils.randomUuid().toString();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setLeaseDuration(15).setProposedLeaseId(leaseId);
        String fileName = generatePathName();

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.createFileWithResponse(fileName, options), 201);


        StepVerifier.create(dataLakeFileSystemAsyncClient.getFileAsyncClient(fileName).getProperties())
            .assertNext(p -> {
                // assert whether lease has been acquired
                assertEquals(LeaseStatusType.LOCKED, p.getLeaseStatus());
                assertEquals(LeaseStateType.LEASED, p.getLeaseState());
                assertEquals(LeaseDurationType.FIXED, p.getLeaseDuration());
            })
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
    @ParameterizedTest
    @MethodSource("createFileOptionsWithTimeExpiresOnAbsoluteAndNeverExpireSupplier")
    public void createFileOptionsWithTimeExpiresOnAbsoluteAndNeverExpire(DataLakePathScheduleDeletionOptions deletionOptions) {
        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.createFileWithResponse(generatePathName(),
            new DataLakePathCreateOptions().setScheduleDeletionOptions(deletionOptions)), 201);
    }

    private static Stream<DataLakePathScheduleDeletionOptions> createFileOptionsWithTimeExpiresOnAbsoluteAndNeverExpireSupplier() {
        return Stream.of(new DataLakePathScheduleDeletionOptions(OffsetDateTime.now().plusDays(1)), null);
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
    @Test
    public void createFileOptionsWithTimeToExpireRelativeToNow() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setScheduleDeletionOptions(new DataLakePathScheduleDeletionOptions(Duration.ofDays(6)));
        String fileName = generatePathName();

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.createFileWithResponse(fileName, options), 201);

        StepVerifier.create(dataLakeFileSystemAsyncClient.getFileAsyncClient(fileName).getProperties())
            .assertNext(p -> compareDatesWithPrecision(p.getExpiresOn(), p.getCreationTime().plusDays(6)))
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsFileMin() {
        StepVerifier.create(dataLakeFileSystemAsyncClient.createFileIfNotExists(generatePathName())
            .flatMap(DataLakePathAsyncClient::exists))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsFileOverwrite() {
        String pathName = generatePathName();

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.createFileIfNotExistsWithResponse(pathName, null), 201);

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.createFileIfNotExistsWithResponse(pathName, null), 409);
    }

    @Test
    public void createIfNotExistsFileDefaults() {
        StepVerifier.create(dataLakeFileSystemAsyncClient.createFileIfNotExistsWithResponse(generatePathName(), null))
            .assertNext(r -> {
                assertEquals(201, r.getStatusCode());
                validateBasicHeaders(r.getHeaders());
            })
            .verifyComplete();

    }

    @ParameterizedTest
    @MethodSource("cacheAndContentSupplier")
    public void createIfNotExistsFileHeaders(String cacheControl, String contentDisposition, String contentEncoding,
                                             String contentLanguage, String contentType) {
        // Create does not set md5
        PathHttpHeaders headers = new PathHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType);

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;
        String finalContentType = contentType;

        StepVerifier.create(dataLakeFileSystemAsyncClient.createFileIfNotExistsWithResponse(generatePathName(),
                new DataLakePathCreateOptions().setPathHttpHeaders(headers))
            .flatMap(r -> r.getValue().getPropertiesWithResponse(null)))
            .assertNext(p -> validatePathProperties(p, cacheControl, contentDisposition, contentEncoding, contentLanguage, null, finalContentType))
            .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz"}, nullValues = "null")
    public void createIfNotExistsFileMetadata(String key1, String value1, String key2, String value2) {
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }

        StepVerifier.create(dataLakeFileSystemAsyncClient.createFileIfNotExistsWithResponse(generatePathName(),
            new DataLakePathCreateOptions().setMetadata(metadata))
            .flatMap(r -> r.getValue().getProperties()))
            .assertNext(p -> assertEquals(metadata, p.getMetadata()))
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsFilePermissionsAndUmask() {
        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.createFileIfNotExistsWithResponse(generatePathName(),
            new DataLakePathCreateOptions().setUmask("0057").setPermissions("0777")), 201);
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
    @Test
    public void createIfNotExistsFileOptionsWithACL() {
        List<PathAccessControlEntry> pathAccessControlEntries = PathAccessControlEntry.parseList("user::rwx,group::r--,other::---,mask::rwx");
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setAccessControlList(pathAccessControlEntries);
        DataLakeFileAsyncClient client = dataLakeFileSystemAsyncClient.createFileIfNotExistsWithResponse(generatePathName(), options).block().getValue();

        StepVerifier.create(client.getAccessControl())
            .assertNext(r -> {
                assertEquals(pathAccessControlEntries.get(0), r.getAccessControlList().get(0)); // testing if owner is set the same
                assertEquals(pathAccessControlEntries.get(1), r.getAccessControlList().get(1)); // testing if group is set the same
            })
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
    @Test
    public void createIfNotExistsFileOptionsWithOwnerAndGroup() {
        String ownerName = testResourceNamer.randomUuid();
        String groupName = testResourceNamer.randomUuid();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setOwner(ownerName)
            .setGroup(groupName);

        StepVerifier.create(dataLakeFileSystemAsyncClient.createFileIfNotExistsWithResponse(generatePathName(), options)
            .flatMap(r -> r.getValue().getAccessControl()))
            .assertNext(acl -> {
                assertEquals(ownerName, acl.getOwner());
                assertEquals(groupName, acl.getGroup());
            })
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsFileOptionsWithNullOwnerAndGroup() {
        StepVerifier.create(dataLakeFileSystemAsyncClient.createFileIfNotExistsWithResponse(generatePathName(), null)
            .flatMap(r -> r.getValue().getAccessControl()))
            .assertNext(acl -> {
                assertEquals("$superuser", acl.getOwner());
                assertEquals("$superuser", acl.getGroup());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("cacheAndContentWithMd5Supplier")
    public void createIfNotExistsFileOptionsWithPathHttpHeaders(String cacheControl, String contentDisposition,
                                                                String contentEncoding, String contentLanguage, byte[] contentMD5, String contentType) {
        PathHttpHeaders putHeaders = new PathHttpHeaders()
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType);

        StepVerifier.create(dataLakeFileSystemAsyncClient.createFileIfNotExistsWithResponse(generatePathName(),
            new DataLakePathCreateOptions().setPathHttpHeaders(putHeaders))
            .flatMap(r -> r.getValue().getPropertiesWithResponse(null)))
            .assertNext(p -> validatePathProperties(p, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMD5, contentType))
            .verifyComplete();

    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz"}, nullValues = "null")
    public void createIfNotExistsFileOptionsWithMetadata(String key1, String value1, String key2, String value2) {
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2);
        }

        StepVerifier.create(dataLakeFileSystemAsyncClient.createFileIfNotExistsWithResponse(generatePathName(),
            new DataLakePathCreateOptions().setMetadata(metadata))
            .flatMap(r -> {
                assertEquals(201, r.getStatusCode());
                return r.getValue().getProperties();
            }))
            .assertNext(p -> {
                // Directory adds a directory metadata value
                for (String k : metadata.keySet()) {
                    assertTrue(p.getMetadata().containsKey(k));
                    assertEquals(metadata.get(k), p.getMetadata().get(k));
                }
            })
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsFileOptionsWithPermissionsAndUmask() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setPermissions("0777").setUmask("0057");

        StepVerifier.create(dataLakeFileSystemAsyncClient.createFileIfNotExistsWithResponse(generatePathName(), options)
            .flatMap(r -> r.getValue().getAccessControlWithResponse(true, null)))
            .assertNext(acl ->  assertEquals(PathPermissions.parseSymbolic("rwx-w----").toString(), acl.getValue().getPermissions().toString()))
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
    @Test
    public void createIfNotExistsFileOptionsWithLeaseId() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setProposedLeaseId(CoreUtils.randomUuid().toString())
            .setLeaseDuration(15);

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.createFileIfNotExistsWithResponse(generatePathName(), options), 201);
    }

    @Test
    public void createIfNotExistsFileOptionsWithLeaseIdError() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setProposedLeaseId(CoreUtils.randomUuid().toString());

        // lease duration must also be set, or else exception is thrown
        StepVerifier.create(dataLakeFileSystemAsyncClient.createFileIfNotExistsWithResponse(generatePathName(), options))
            .verifyError(DataLakeStorageException.class);
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
    @Test
    public void createIfNotExistsFileOptionsWithLeaseDuration() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setLeaseDuration(15)
            .setProposedLeaseId(CoreUtils.randomUuid().toString());
        String fileName = generatePathName();

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.createFileIfNotExistsWithResponse(fileName, options), 201);

        StepVerifier.create(dataLakeFileSystemAsyncClient.getFileAsyncClient(fileName).getProperties())
            .assertNext(p -> {
                // assert whether lease has been acquired
                assertEquals(LeaseStatusType.LOCKED, p.getLeaseStatus());
                assertEquals(LeaseStateType.LEASED, p.getLeaseState());
                assertEquals(LeaseDurationType.FIXED, p.getLeaseDuration());
            })
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
    @ParameterizedTest
    @MethodSource("createIfNotExistsFileOptionsWithTimeExpiresOnAbsoluteAndNeverExpireSupplier")
    public void createIfNotExistsFileOptionsWithTimeExpiresOnAbsoluteAndNeverExpire(DataLakePathScheduleDeletionOptions deletionOptions) {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setScheduleDeletionOptions(deletionOptions);

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.createFileIfNotExistsWithResponse(generatePathName(), options), 201);
    }

    private static Stream<DataLakePathScheduleDeletionOptions> createIfNotExistsFileOptionsWithTimeExpiresOnAbsoluteAndNeverExpireSupplier() {
        return Stream.of(new DataLakePathScheduleDeletionOptions(OffsetDateTime.now().plusDays(1)), null);
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
    @Test
    public void createIfNotExistsFileOptionsWithTimeToExpireRelativeToNow() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setScheduleDeletionOptions(new DataLakePathScheduleDeletionOptions(Duration.ofDays(6)));
        String fileName = generatePathName();

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.createFileWithResponse(fileName, options), 201);

        StepVerifier.create(dataLakeFileSystemAsyncClient.getFileAsyncClient(fileName).getProperties())
            .assertNext(p -> compareDatesWithPrecision(p.getExpiresOn(), p.getCreationTime().plusDays(6)))
            .verifyComplete();
    }

    @Test
    public void deleteFileMin() {
        String pathName = generatePathName();
        dataLakeFileSystemAsyncClient.createFile(pathName).block();

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.deleteFileWithResponse(pathName, null), 200);
    }

    @Test
    public void deleteFileFileDoesNotExistAnymore() {
        String pathName = generatePathName();
        DataLakeFileAsyncClient client = dataLakeFileSystemAsyncClient.createFile(pathName).block();
        dataLakeFileSystemAsyncClient.deleteFileWithResponse(pathName, null).block();

        StepVerifier.create(client.getPropertiesWithResponse(null))
            .verifyErrorSatisfies(r -> {
                DataLakeStorageException e = assertInstanceOf(DataLakeStorageException.class, r);
                assertEquals(404, e.getStatusCode());
                assertEquals(BlobErrorCode.BLOB_NOT_FOUND.toString(), e.getErrorCode());
            });
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void deleteFileAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                             String leaseID) {
        String pathName = generatePathName();
        DataLakeFileAsyncClient client = dataLakeFileSystemAsyncClient.createFile(pathName).block();
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(client, leaseID))
            .setIfMatch(setupPathMatchCondition(client, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.deleteFileWithResponse(pathName, drc), 200);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void deleteFileACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                 String leaseID) {
        String pathName = generatePathName();
        DataLakeFileAsyncClient client = dataLakeFileSystemAsyncClient.createFile(pathName).block();
        setupPathLeaseCondition(client, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(client, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(dataLakeFileSystemAsyncClient.deleteFileWithResponse(pathName, drc))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void deleteIfExistsFileMin() {
        String pathName = generatePathName();
        dataLakeFileSystemAsyncClient.createFile(pathName).block();

        StepVerifier.create(dataLakeFileSystemAsyncClient.deleteFileIfExists(pathName))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsFileNullArgs() {
        String pathName = generatePathName();
        dataLakeFileSystemAsyncClient.createFile(pathName).block();

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.deleteFileIfExistsWithResponse(pathName, null), 200);
    }

    @Test
    public void deleteIfExistsFileThatDoesNotExists() {
        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.deleteFileIfExistsWithResponse(generatePathName(), null), 404);
    }

    @Test
    public void deleteIfExistsFileThatWasAlreadyDelete() {
        String pathName = generatePathName();
        dataLakeFileSystemAsyncClient.createFile(pathName).block();

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.deleteFileIfExistsWithResponse(pathName, null), 200);
        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.deleteFileIfExistsWithResponse(pathName, null), 404);
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void deleteIfExistsFileAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                     String leaseID) {
        String pathName = generatePathName();
        DataLakeFileAsyncClient client = dataLakeFileSystemAsyncClient.createFile(pathName).block();
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(client, leaseID))
            .setIfMatch(setupPathMatchCondition(client, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);
        DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setRequestConditions(drc);

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.deleteFileIfExistsWithResponse(pathName, options), 200);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void deleteIfExistsFileACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                         String noneMatch, String leaseID) {
        String pathName = generatePathName();
        DataLakeFileAsyncClient client = dataLakeFileSystemAsyncClient.createFile(pathName).block();
        setupPathLeaseCondition(client, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(client, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);
        DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setRequestConditions(drc);

        StepVerifier.create(dataLakeFileSystemAsyncClient.deleteFileIfExistsWithResponse(pathName, options))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void createDirMin() {
        assertDoesNotThrow(() -> dataLakeFileSystemAsyncClient.createDirectory(generatePathName()).block());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void createDirOverwrite(boolean overwrite) {
        String pathName = generatePathName();
        dataLakeFileSystemAsyncClient.createDirectory(pathName).block();

        if (overwrite) {
            assertDoesNotThrow(() -> dataLakeFileSystemAsyncClient.createDirectory(pathName, true).block());
        } else {
            StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectory(pathName, false))
                .verifyError(DataLakeStorageException.class);
        }
    }

    @Test
    public void createDirDefaults() {
        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryWithResponse(generatePathName(), null,
            null, null, null, null))
            .assertNext(r -> {
                assertEquals(201, r.getStatusCode());
                validateBasicHeaders(r.getHeaders());
            })
            .verifyComplete();
    }

    @Test
    public void createDirError() {
        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryWithResponse(
            generatePathName(), null, null, null, null, new DataLakeRequestConditions().setIfMatch("garbage")))
            .verifyError(DataLakeStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("cacheAndContentSupplier")
    public void createDirHeaders(String cacheControl, String contentDisposition, String contentEncoding,
                                 String contentLanguage, String contentType) {
        // Create does not set md5
        PathHttpHeaders headers = new PathHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType);

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;
        String finalContentType = contentType;

        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryWithResponse(generatePathName(),
            null, null, headers, null, null)
            .flatMap(r -> r.getValue().getPropertiesWithResponse(null)))
            .assertNext(p -> validatePathProperties(p, cacheControl, contentDisposition, contentEncoding, contentLanguage, null, finalContentType))
            .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz"}, nullValues = "null")
    public void createDirMetadata(String key1, String value1, String key2, String value2) {
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }

        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryWithResponse(generatePathName(), null, null,
                null, metadata, null)
            .flatMap(r -> r.getValue().getProperties()))
            .assertNext(p -> {
                // Directory adds a directory metadata value
                for (String k : metadata.keySet()) {
                    assertTrue(p.getMetadata().containsKey(k));
                    assertEquals(metadata.get(k), p.getMetadata().get(k));
                }
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void createDirAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                            String leaseID) {
        String pathName = generatePathName();
        DataLakeDirectoryAsyncClient client = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(pathName);
        client.create().block();
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(client, leaseID))
            .setIfMatch(setupPathMatchCondition(client, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.createDirectoryWithResponse(pathName, null, null, null, null, drc), 201);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void createDirACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                String leaseID) {
        String pathName = generatePathName();
        DataLakeDirectoryAsyncClient client = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(pathName);
        client.create().block();
        setupPathLeaseCondition(client, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(client, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryWithResponse(pathName, null, null, null, null, drc))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void createDirPermissionsAndUmask() {
        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.createDirectoryWithResponse(generatePathName(), "0777", "0057",
            null, null, null), 201);
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
    @Test
    public void createDirOptionsWithACL() {
        List<PathAccessControlEntry> pathAccessControlEntries = PathAccessControlEntry.parseList("user::rwx,group::r--,other::---,mask::rwx");
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setAccessControlList(pathAccessControlEntries);

        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryWithResponse(generatePathName(), options)
            .flatMap(r -> r.getValue().getAccessControl()))
            .assertNext(acl -> {
                assertEquals(pathAccessControlEntries.get(0), acl.getAccessControlList().get(0)); // testing if owner is set the same
                assertEquals(pathAccessControlEntries.get(1), acl.getAccessControlList().get(1)); // testing if group is set the same
            })
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
    @Test
    public void createDirOptionsWithOwnerAndGroup() {
        String ownerName = testResourceNamer.randomUuid();
        String groupName = testResourceNamer.randomUuid();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setOwner(ownerName).setGroup(groupName);

        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryWithResponse(generatePathName(), options)
            .flatMap(r -> r.getValue().getAccessControl()))
            .assertNext(acl -> {
                assertEquals(ownerName, acl.getOwner());
                assertEquals(groupName, acl.getGroup());
            })
            .verifyComplete();
    }

    @Test
    public void createDirOptionsWithNullOwnerAndGroup() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setOwner(null).setGroup(null);

        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryWithResponse(generatePathName(), options)
                .flatMap(r -> r.getValue().getAccessControl()))
            .assertNext(acl -> {
                assertEquals("$superuser", acl.getOwner());
                assertEquals("$superuser", acl.getGroup());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("cacheAndContentWithMd5Supplier")
    public void createDirOptionsWithPathHttpHeaders(String cacheControl, String contentDisposition,
                                                    String contentEncoding, String contentLanguage, byte[] contentMD5, String contentType) {
        PathHttpHeaders putHeaders = new PathHttpHeaders()
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType);

        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setPathHttpHeaders(putHeaders);

        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryWithResponse(generatePathName(), options)
            .flatMap(r -> r.getValue().getPropertiesWithResponse(null)))
            .assertNext(p -> validatePathProperties(p, cacheControl, contentDisposition,
                contentEncoding, contentLanguage, contentMD5, contentType))
            .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz"}, nullValues = "null")
    public void createDirOptionsWithMetadata(String key1, String value1, String key2, String value2) {
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2);
        }
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setMetadata(metadata);

        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryWithResponse(generatePathName(), options)
            .flatMap(r -> {
                assertEquals(201, r.getStatusCode());
                return r.getValue().getProperties();
            }))
            .assertNext(p -> {
                // Directory adds a directory metadata value
                for (String k : metadata.keySet()) {
                    assertTrue(p.getMetadata().containsKey(k));
                    assertEquals(metadata.get(k), p.getMetadata().get(k));
                }
            })
            .verifyComplete();
    }

    @Test
    public void createDirOptionsWithPermissionsAndUmask() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setPermissions("0777")
            .setUmask("0057");

        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryWithResponse(generatePathName(), options)
            .flatMap(r -> r.getValue().getAccessControlWithResponse(true, null)))
            .assertNext(acl -> assertEquals(PathPermissions.parseSymbolic("rwx-w----").toString(), acl.getValue().getPermissions().toString()))
            .verifyComplete();
    }

    @Test
    public void createDirOptionsWithLeaseId() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setProposedLeaseId(CoreUtils.randomUuid().toString())
            .setLeaseDuration(15);

        // lease id not supported for directories
        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryWithResponse(generatePathName(), options))
            .verifyError(IllegalArgumentException.class);
    }

    @Test
    public void createDirOptionsWithLeaseDuration() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setLeaseDuration(15)
            .setProposedLeaseId(CoreUtils.randomUuid().toString());

        // lease duration not supported for directories
        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryWithResponse(generatePathName(), options))
            .verifyError(IllegalArgumentException.class);
    }

    @Test
    public void createDirOptionsWithTimeExpiresOn() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setProposedLeaseId(CoreUtils.randomUuid().toString())
            .setScheduleDeletionOptions(new DataLakePathScheduleDeletionOptions(OffsetDateTime.now().plusDays(1)));

        // expires on not supported for directories
        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryWithResponse(generatePathName(), options))
            .verifyError(IllegalArgumentException.class);
    }

    @Test
    public void createDirOptionsWithTimeToExpire() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setScheduleDeletionOptions(new DataLakePathScheduleDeletionOptions(Duration.ofDays(6)));

        // time to expire not supported for directories
        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryWithResponse(generatePathName(), options))
            .verifyError(IllegalArgumentException.class);
    }

    @Test
    public void createIfNotExistsDirMin() {
        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryIfNotExists(generatePathName())
            .flatMap(DataLakePathAsyncClient::exists))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsDirDefaults() {
        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryIfNotExistsWithResponse(generatePathName(),
            null))
            .assertNext(r -> {
                assertEquals(201, r.getStatusCode());
                validateBasicHeaders(r.getHeaders());
            })
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsDirThatAlreadyExists() {
        String dirName = generatePathName();

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.createDirectoryIfNotExistsWithResponse(dirName,
            new DataLakePathCreateOptions()), 201);
        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.createDirectoryIfNotExistsWithResponse(dirName,
            new DataLakePathCreateOptions()), 409);
    }

    @ParameterizedTest
    @MethodSource("cacheAndContentSupplier")
    public void createIfNotExistsDirHeaders(String cacheControl, String contentDisposition, String contentEncoding,
                                            String contentLanguage, String contentType) {
        // Create does not set md5
        PathHttpHeaders headers = new PathHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType);

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;
        String finalContentType = contentType;

        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryIfNotExistsWithResponse(generatePathName(),
            new DataLakePathCreateOptions().setPathHttpHeaders(headers))
            .flatMap(r -> r.getValue().getPropertiesWithResponse(null)))
            .assertNext(p -> validatePathProperties(p, cacheControl, contentDisposition, contentEncoding, contentLanguage, null, finalContentType))
            .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz"}, nullValues = "null")
    public void createIfNotExistsDirMetadata(String key1, String value1, String key2, String value2) {
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }

        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryIfNotExistsWithResponse(generatePathName(),
                new DataLakePathCreateOptions().setMetadata(metadata))
            .flatMap(r -> r.getValue().getProperties()))
            .assertNext(p -> {
                // Directory adds a directory metadata value
                for (String k : metadata.keySet()) {
                    assertTrue(p.getMetadata().containsKey(k));
                    assertEquals(metadata.get(k), p.getMetadata().get(k));
                }
            })
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsDirPermissionsAndUmask() {
        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.createDirectoryIfNotExistsWithResponse(generatePathName(),
            new DataLakePathCreateOptions().setPermissions("0777").setUmask("0057")), 201);
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
    @Test
    public void createIfNotExistsDirOptionsWithACL() {
        List<PathAccessControlEntry> pathAccessControlEntries = PathAccessControlEntry.parseList("user::rwx,group::r--,other::---,mask::rwx");
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setAccessControlList(pathAccessControlEntries);

        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options)
            .flatMap(r -> r.getValue().getAccessControl()))
            .assertNext(acl -> {
                assertEquals(pathAccessControlEntries.get(0), acl.getAccessControlList().get(0)); // testing if owner is set the same
                assertEquals(pathAccessControlEntries.get(1), acl.getAccessControlList().get(1)); // testing if group is set the same
            })
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
    @Test
    public void createIfNotExistsDirOptionsWithOwnerAndGroup() {
        String ownerName = testResourceNamer.randomUuid();
        String groupName = testResourceNamer.randomUuid();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setOwner(ownerName)
            .setGroup(groupName);

        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options)
            .flatMap(r -> r.getValue().getAccessControl()))
            .assertNext(acl -> {
                assertEquals(ownerName, acl.getOwner());
                assertEquals(groupName, acl.getGroup());
            })
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsDirOptionsWithNullOwnerAndGroup() {
        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryIfNotExistsWithResponse(generatePathName(), null)
                .flatMap(r -> r.getValue().getAccessControl()))
            .assertNext(acl -> {
                assertEquals("$superuser", acl.getOwner());
                assertEquals("$superuser", acl.getGroup());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("cacheAndContentWithMd5Supplier")
    public void createIfNotExistsDirOptionsWithPathHttpHeaders(String cacheControl, String contentDisposition,
                                                               String contentEncoding, String contentLanguage, byte[] contentMD5, String contentType) {
        PathHttpHeaders putHeaders = new PathHttpHeaders()
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType);

        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setPathHttpHeaders(putHeaders);
        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options)
            .flatMap(r -> r.getValue().getPropertiesWithResponse(null)))
            .assertNext(p -> validatePathProperties(p, cacheControl, contentDisposition,
                contentEncoding, contentLanguage, contentMD5, contentType))
            .verifyComplete();
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz"}, nullValues = "null")
    public void createIfNotExistsDirOptionsWithMetadata(String key1, String value1, String key2, String value2) {
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2);
        }
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setMetadata(metadata);

        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options)
            .flatMap(r -> {
                assertEquals(201, r.getStatusCode());
                return r.getValue().getProperties();
            }))
            .assertNext(p -> {
                // Directory adds a directory metadata value
                for (String k : metadata.keySet()) {
                    assertTrue(p.getMetadata().containsKey(k));
                    assertEquals(metadata.get(k), p.getMetadata().get(k));
                }
            })
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsDirOptionsWithPermissionsAndUmask() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setPermissions("0777")
            .setUmask("0057");

        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options)
            .flatMap(r -> r.getValue().getAccessControlWithResponse(true, null)))
            .assertNext(acl -> assertEquals(PathPermissions.parseSymbolic("rwx-w----").toString(), acl.getValue().getPermissions().toString()))
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsDirOptionsWithLeaseId() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setProposedLeaseId(CoreUtils.randomUuid().toString())
            .setLeaseDuration(15);

        // assert lease id not supported for directory
        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options))
            .verifyError(IllegalArgumentException.class);
    }

    @Test
    public void createIfNotExistsDirOptionsWithLeaseIdError() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setProposedLeaseId(CoreUtils.randomUuid().toString());

        // assert lease duration not supported for directory
        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options))
            .verifyError(IllegalArgumentException.class);
    }

    @Test
    public void createIfNotExistsDirOptionsWithLeaseDuration() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setLeaseDuration(15)
            .setProposedLeaseId(CoreUtils.randomUuid().toString());

        // assert expires on not supported for directory
        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options))
            .verifyError(IllegalArgumentException.class);
    }

    @Test
    public void createIfNotExistsDirOptionsWithTimeExpiresOnAbsolute() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setScheduleDeletionOptions(new DataLakePathScheduleDeletionOptions(OffsetDateTime.now().plusDays(1)));

        // assert expires on not supported for directory
        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options))
            .verifyError(IllegalArgumentException.class);
    }

    @Test
    public void createIfNotExistsDirOptionsWithTimeToExpireRelativeToNow() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setScheduleDeletionOptions(new DataLakePathScheduleDeletionOptions(Duration.ofDays(6)));

        // assert time to expire not supported for directory
        StepVerifier.create(dataLakeFileSystemAsyncClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options))
            .verifyError(IllegalArgumentException.class);
    }

    @Test
    public void deleteDirMin() {
        String pathName = generatePathName();
        dataLakeFileSystemAsyncClient.createDirectory(pathName).block();

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.deleteDirectoryWithResponse(pathName, false, null), 200);
    }

    @Test
    public void deleteDirRecursive() {
        String pathName = generatePathName();
        dataLakeFileSystemAsyncClient.createDirectory(pathName).block();

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.deleteDirectoryWithResponse(pathName, true, null), 200);
    }

    @Test
    public void deleteDirDirDoesNotExistAnymore() {
        String pathName = generatePathName();
        DataLakeDirectoryAsyncClient client = dataLakeFileSystemAsyncClient.createDirectory(pathName).block();
        dataLakeFileSystemAsyncClient.deleteDirectoryWithResponse(pathName, false, null).block();

        StepVerifier.create(client.getPropertiesWithResponse(null))
            .verifyErrorSatisfies(r -> {
                DataLakeStorageException e = assertInstanceOf(DataLakeStorageException.class, r);
                assertEquals(404, e.getResponse().getStatusCode());
                assertEquals(BlobErrorCode.BLOB_NOT_FOUND.toString(), e.getErrorCode());
            });
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void deleteDirAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                            String leaseID) {
        String pathName = generatePathName();
        DataLakeDirectoryAsyncClient client = dataLakeFileSystemAsyncClient.createDirectory(pathName).block();
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(client, leaseID))
            .setIfMatch(setupPathMatchCondition(client, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.deleteDirectoryWithResponse(pathName, false, drc), 200);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void deleteDirACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                String leaseID) {
        String pathName = generatePathName();
        DataLakeDirectoryAsyncClient client = dataLakeFileSystemAsyncClient.createDirectory(pathName).block();
        setupPathLeaseCondition(client, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(client, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(dataLakeFileSystemAsyncClient.deleteDirectoryWithResponse(pathName, false, drc))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void deleteIfExistsDirMin() {
        String pathName = generatePathName();
        dataLakeFileSystemAsyncClient.createDirectory(pathName).block();

        StepVerifier.create(dataLakeFileSystemAsyncClient.deleteDirectoryIfExists(pathName))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsDirNullArgs() {
        String pathName = generatePathName();
        dataLakeFileSystemAsyncClient.createDirectory(pathName).block();

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.deleteDirectoryIfExistsWithResponse(pathName, null), 200);
    }

    @Test
    public void deleteIfExistsDirRecursive() {
        String pathName = generatePathName();
        dataLakeFileSystemAsyncClient.createDirectory(pathName).block();

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.deleteDirectoryIfExistsWithResponse(pathName,
            new DataLakePathDeleteOptions().setIsRecursive(true)), 200);
    }

    @Test
    public void deleteIfExistsDirThatDoesNotExist() {
        String pathName = generatePathName();

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.deleteDirectoryIfExistsWithResponse(pathName, null), 404);
        StepVerifier.create(dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(pathName).exists())
            .expectNext(false)
            .verifyComplete();
    }

    @Test
    public void deleteIfExistsDirThatWasAlreadyDeleted() {
        String pathName = generatePathName();
        dataLakeFileSystemAsyncClient.createDirectory(pathName).block();

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.deleteDirectoryIfExistsWithResponse(pathName, null), 200);
        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.deleteDirectoryIfExistsWithResponse(pathName, null), 404);
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void deleteIfExistsDirAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
                                    String leaseID) {
        String pathName = generatePathName();
        DataLakeDirectoryAsyncClient client = dataLakeFileSystemAsyncClient.createDirectory(pathName).block();
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(client, leaseID))
            .setIfMatch(setupPathMatchCondition(client, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);
        DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setRequestConditions(drc).setIsRecursive(false);

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.deleteDirectoryIfExistsWithResponse(pathName, options), 200);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void deleteIfExistsDirACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
                                        String noneMatch, String leaseID) {
        String pathName = generatePathName();
        DataLakeDirectoryAsyncClient client = dataLakeFileSystemAsyncClient.createDirectory(pathName).block();
        setupPathLeaseCondition(client, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(client, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);
        DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setRequestConditions(drc).setIsRecursive(false);

        StepVerifier.create(dataLakeFileSystemAsyncClient.deleteDirectoryIfExistsWithResponse(pathName, options))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void listPaths() {
        String dirName = generatePathName();
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(dirName).create().block();

        String fileName = generatePathName();
        dataLakeFileSystemAsyncClient.getFileAsyncClient(fileName).create().block();

        StepVerifier.create(dataLakeFileSystemAsyncClient.listPaths())
            .assertNext(r -> {
                assertEquals(dirName, r.getName());
                assertNotNull(r.getETag());
                assertNotNull(r.getGroup());
                assertNotNull(r.getLastModified());
                assertNotNull(r.getOwner());
                assertNotNull(r.getPermissions());
                //assertNotNull(dirPath.getContentLength()); // known issue with service
                assertTrue(r.isDirectory());
            })
            .assertNext(r -> {
                assertEquals(fileName, r.getName());
                assertNotNull(r.getETag());
                assertNotNull(r.getGroup());
                assertNotNull(r.getLastModified());
                assertNotNull(r.getOwner());
                assertNotNull(r.getPermissions());
                //assertNotNull(filePath.getContentLength()); // known issue with service
                assertFalse(r.isDirectory());
            })
            .verifyComplete();
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @Test
    public void listPathsExpiryAndCreation() {
        String dirName = generatePathName();
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(dirName).create().block();

        String fileName = generatePathName();
        DataLakeFileAsyncClient fileClient = dataLakeFileSystemAsyncClient.getFileAsyncClient(fileName);
        fileClient.create().block();
        fileClient.scheduleDeletion(new FileScheduleDeletionOptions(OffsetDateTime.now().plusDays(2))).block();

        StepVerifier.create(dataLakeFileSystemAsyncClient.listPaths())
            .assertNext(r -> {
                assertEquals(dirName, r.getName());
                assertNotNull(r.getCreationTime());
                assertNull(r.getExpiryTime());
            })
            .assertNext(r -> {
                assertEquals(fileName, r.getName());
                assertNotNull(r.getExpiryTime());
                assertNotNull(r.getCreationTime());
            })
            .verifyComplete();
    }

    @Test
    public void listPathsRecursive() {
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName()).create().block();
        dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName()).create().block();

        StepVerifier.create(dataLakeFileSystemAsyncClient.listPaths(new ListPathsOptions().setRecursive(true)))
            .expectNextCount(2)
            .verifyComplete();
    }

    @Test
    public void listPathsReturnUpn() {
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName()).create().block();
        dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName()).create().block();

        StepVerifier.create(dataLakeFileSystemAsyncClient.listPaths(new ListPathsOptions().setUserPrincipalNameReturned(true)))
            .expectNextCount(2)
            .verifyComplete();
    }

    @Test
    public void listPathsMaxResults() {
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName()).create().block();
        dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName()).create().block();

        StepVerifier.create(dataLakeFileSystemAsyncClient.listPaths(new ListPathsOptions().setMaxResults(1)))
            .expectNextCount(2)
            .verifyComplete();
    }

    @Test
    public void listPathsEncryptionScope() {
        FileSystemEncryptionScopeOptions encryptionScope = new FileSystemEncryptionScopeOptions()
            .setDefaultEncryptionScope(ENCRYPTION_SCOPE_STRING)
            .setEncryptionScopeOverridePrevented(true);

        dataLakeFileSystemAsyncClient = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());

        DataLakeFileSystemAsyncClient client = getFileSystemClientBuilder(dataLakeFileSystemAsyncClient.getFileSystemUrl())
            .credential(getDataLakeCredential())
            .fileSystemEncryptionScopeOptions(encryptionScope)
            .buildAsyncClient();
        client.create().block();

        String dirName = generatePathName();
        client.getDirectoryAsyncClient(dirName).create().block();

        String fileName = generatePathName();
        dataLakeFileSystemAsyncClient.getFileAsyncClient(fileName).create().block();

        StepVerifier.create(dataLakeFileSystemAsyncClient.listPaths())
            .assertNext(r -> {
                assertEquals(dirName, r.getName());
                assertNotNull(r.getETag());
                assertNotNull(r.getGroup());
                assertNotNull(r.getLastModified());
                assertNotNull(r.getOwner());
                assertNotNull(r.getPermissions());
                assertTrue(r.isDirectory());
                assertEquals(ENCRYPTION_SCOPE_STRING, r.getEncryptionScope());
            })
            .assertNext(r -> {
                assertEquals(fileName, r.getName());
                assertNotNull(r.getETag());
                assertNotNull(r.getGroup());
                assertNotNull(r.getLastModified());
                assertNotNull(r.getOwner());
                assertNotNull(r.getPermissions());
                assertEquals(ENCRYPTION_SCOPE_STRING, r.getEncryptionScope());
                assertFalse(r.isDirectory());
            })
            .verifyComplete();
    }

    @Test
    public void asyncListPathsMaxResultsByPage() {
        dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(generatePathName()).create().block();
        dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName()).create().block();

        StepVerifier.create(dataLakeFileSystemAsyncClient.listPaths(new ListPathsOptions()).byPage(1))
            .thenConsumeWhile(page -> {
                assertEquals(1, page.getValue().size());
                return true;
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(strings = {"%E4%B8%AD%E6%96%87", "az%5B%5D", "hello%20world", "hello%26world",
        "%21%2A%27%28%29%3B%3A%40%26%3D%2B%24%2C%3F%23%5B%5D"})
    public void createUrlSpecialCharsEncoded(String name) {
        // Note you cannot use the / character in a path in datalake unless it is to specify an absolute path
        // This test checks that we handle path names with encoded special characters correctly.

        DataLakeFileAsyncClient fc1 = dataLakeFileSystemAsyncClient.getFileAsyncClient(name + "file1");
        DataLakeFileAsyncClient fc2 = dataLakeFileSystemAsyncClient.getFileAsyncClient(name + "file2");
        DataLakeDirectoryAsyncClient dc1 = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(name + "dir1");
        DataLakeDirectoryAsyncClient dc2 = dataLakeFileSystemAsyncClient.getDirectoryAsyncClient(name + "dir2");

        assertAsyncResponseStatusCode(fc1.createWithResponse(null, null, null, null, null), 201);
        fc2.create().block();
        assertAsyncResponseStatusCode(fc2.getPropertiesWithResponse(null), 200);
        assertAsyncResponseStatusCode(fc2.appendWithResponse(DATA.getDefaultBinaryData(), 0, null, null), 202);
        assertAsyncResponseStatusCode(dc1.createWithResponse(null, null, null, null, null), 201);
        dc2.create().block();
        assertAsyncResponseStatusCode(dc2.getPropertiesWithResponse(null), 200);

        StepVerifier.create(dataLakeFileSystemAsyncClient.listPaths())
            .assertNext(r -> assertEquals(Utility.urlDecode(name) + "dir1", r.getName()))
            .assertNext(r -> assertEquals(Utility.urlDecode(name) + "dir2", r.getName()))
            .assertNext(r -> assertEquals(Utility.urlDecode(name) + "file1", r.getName()))
            .assertNext(r -> assertEquals(Utility.urlDecode(name) + "file2", r.getName()))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("publicAccessSupplier")
    public void setAccessPolicy(PublicAccessType access) {
        StepVerifier.create(dataLakeFileSystemAsyncClient.setAccessPolicyWithResponse(access, null, null))
            .assertNext(r -> validateBasicHeaders(r.getHeaders()))
            .verifyComplete();

        StepVerifier.create(dataLakeFileSystemAsyncClient.getProperties())
            .assertNext(r -> assertEquals(access, r.getDataLakePublicAccess()))
            .verifyComplete();
    }

    @Test
    public void setAccessPolicyMinAccess() {
        dataLakeFileSystemAsyncClient.setAccessPolicy(PublicAccessType.CONTAINER, null).block();

        StepVerifier.create(dataLakeFileSystemAsyncClient.getProperties())
            .assertNext(r -> assertEquals(PublicAccessType.CONTAINER, r.getDataLakePublicAccess()))
            .verifyComplete();
    }

    @Test
    public void setAccessPolicyMinIds() {
        DataLakeSignedIdentifier identifier = new DataLakeSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new DataLakeAccessPolicy()
                .setStartsOn(OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime())
                .setExpiresOn(OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime()
                    .plusDays(1))
                .setPermissions("r"));

        dataLakeFileSystemAsyncClient.setAccessPolicy(null, Collections.singletonList(identifier)).block();

        StepVerifier.create(dataLakeFileSystemAsyncClient.getAccessPolicy())
            .assertNext(r -> assertEquals(identifier.getId(), r.getIdentifiers().get(0).getId()))
            .verifyComplete();
    }

    @Test
    public void setAccessPolicyIds() {
        DataLakeSignedIdentifier identifier = new DataLakeSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new DataLakeAccessPolicy()
                .setStartsOn(testResourceNamer.now())
                .setExpiresOn(testResourceNamer.now().plusDays(1))
                .setPermissions("r"));
        DataLakeSignedIdentifier identifier2 = new DataLakeSignedIdentifier()
            .setId("0001")
            .setAccessPolicy(new DataLakeAccessPolicy()
                .setStartsOn(testResourceNamer.now())
                .setExpiresOn(testResourceNamer.now().plusDays(2))
                .setPermissions("w"));

        StepVerifier.create(dataLakeFileSystemAsyncClient.setAccessPolicyWithResponse(null,
            Arrays.asList(identifier, identifier2), null))
            .assertNext(r -> {
                assertEquals(200, r.getStatusCode());
                validateBasicHeaders(r.getHeaders());
            })
            .verifyComplete();

        StepVerifier.create(dataLakeFileSystemAsyncClient.getAccessPolicyWithResponse(null))
            .assertNext(r -> {
                assertEquals(identifier.getAccessPolicy().getExpiresOn().truncatedTo(ChronoUnit.SECONDS),
                    r.getValue().getIdentifiers().get(0).getAccessPolicy().getExpiresOn());
                assertEquals(identifier.getAccessPolicy().getStartsOn().truncatedTo(ChronoUnit.SECONDS),
                    r.getValue().getIdentifiers().get(0).getAccessPolicy().getStartsOn());
                assertEquals(identifier.getAccessPolicy().getPermissions(),
                    r.getValue().getIdentifiers().get(0).getAccessPolicy().getPermissions());
                assertEquals(identifier2.getAccessPolicy().getExpiresOn().truncatedTo(ChronoUnit.SECONDS),
                    r.getValue().getIdentifiers().get(1).getAccessPolicy().getExpiresOn());
                assertEquals(identifier2.getAccessPolicy().getStartsOn().truncatedTo(ChronoUnit.SECONDS),
                    r.getValue().getIdentifiers().get(1).getAccessPolicy().getStartsOn());
                assertEquals(identifier2.getAccessPolicy().getPermissions(),
                    r.getValue().getIdentifiers().get(1).getAccessPolicy().getPermissions());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("modifiedAndLeaseIdSupplier")
    public void setAccessPolicyAC(OffsetDateTime modified, OffsetDateTime unmodified, String leaseID) {
        DataLakeRequestConditions cac = new DataLakeRequestConditions()
            .setLeaseId(setupFileSystemLeaseAsyncCondition(dataLakeFileSystemAsyncClient, leaseID))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.setAccessPolicyWithResponse(null, null, cac), 200);
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedAndLeaseIdSupplier")
    public void setAccessPolicyACFail(OffsetDateTime modified, OffsetDateTime unmodified, String leaseID) {
        DataLakeRequestConditions cac = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        StepVerifier.create(dataLakeFileSystemAsyncClient.setAccessPolicyWithResponse(null, null, cac))
            .verifyError(DataLakeStorageException.class);
    }

    @ParameterizedTest
    @MethodSource("invalidMatchSupplier")
    public void setAccessPolicyACIllegal(String match, String noneMatch) {
        DataLakeRequestConditions mac = new DataLakeRequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch);

        StepVerifier.create(dataLakeFileSystemAsyncClient.setAccessPolicyWithResponse(null, null, mac))
            .verifyError(UnsupportedOperationException.class);
    }

    @Test
    public void setAccessPolicyError() {
        dataLakeFileSystemAsyncClient = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());

        StepVerifier.create(dataLakeFileSystemAsyncClient.setAccessPolicy(null, null))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void getAccessPolicy() {
        DataLakeSignedIdentifier identifier = new DataLakeSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new DataLakeAccessPolicy()
                .setStartsOn(testResourceNamer.now())
                .setExpiresOn(testResourceNamer.now().plusDays(1))
                .setPermissions("r"));
        dataLakeFileSystemAsyncClient.setAccessPolicy(PublicAccessType.BLOB, Collections.singletonList(identifier)).block();

        StepVerifier.create(dataLakeFileSystemAsyncClient.getAccessPolicyWithResponse(null))
            .assertNext(r -> {
                assertEquals(200, r.getStatusCode());
                assertEquals(PublicAccessType.BLOB, r.getValue().getDataLakeAccessType());
                validateBasicHeaders(r.getHeaders());
                assertEquals(identifier.getAccessPolicy().getExpiresOn().truncatedTo(ChronoUnit.SECONDS),
                    r.getValue().getIdentifiers().get(0).getAccessPolicy().getExpiresOn());
                assertEquals(identifier.getAccessPolicy().getStartsOn().truncatedTo(ChronoUnit.SECONDS),
                    r.getValue().getIdentifiers().get(0).getAccessPolicy().getStartsOn());
                assertEquals(identifier.getAccessPolicy().getPermissions(),
                    r.getValue().getIdentifiers().get(0).getAccessPolicy().getPermissions());
            })
            .verifyComplete();
    }

    @Test
    public void getAccessPolicyLease() {
        String leaseID = setupFileSystemLeaseAsyncCondition(dataLakeFileSystemAsyncClient, RECEIVED_LEASE_ID);

        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.getAccessPolicyWithResponse(leaseID), 200);
    }

    @Test
    public void getAccessPolicyLeaseFail() {
        StepVerifier.create(dataLakeFileSystemAsyncClient.getAccessPolicyWithResponse(GARBAGE_LEASE_ID))
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void getAccessPolicyError() {
        dataLakeFileSystemAsyncClient = primaryDataLakeServiceAsyncClient.getFileSystemAsyncClient(generateFileSystemName());

        StepVerifier.create(dataLakeFileSystemAsyncClient.getAccessPolicy())
            .verifyError(DataLakeStorageException.class);
    }

    @Test
    public void builderBearerTokenValidation() {
        // Technically no additional checks need to be added to datalake builder since the corresponding blob builder fails
        String endpoint = BlobUrlParts.parse(dataLakeFileSystemAsyncClient.getFileSystemUrl()).setScheme("http").toUrl()
            .toString();

        assertThrows(IllegalArgumentException.class, () -> new DataLakeFileSystemClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildAsyncClient());
    }

    @Test
    public void listPathsOAuth() {
        DataLakeFileSystemAsyncClient fsClient = getOAuthServiceAsyncClient()
            .getFileSystemAsyncClient(dataLakeFileSystemAsyncClient.getFileSystemName());
        fsClient.createFile(generatePathName()).block();

        StepVerifier.create(fsClient.listPaths())
            .thenConsumeWhile(p -> {
                assertNotNull(p);
                return true;
            })
            .verifyComplete();
    }

    @Test
    public void setACLRootDirectory() {
        DataLakeDirectoryAsyncClient dc = dataLakeFileSystemAsyncClient.getRootDirectoryAsyncClient();
        List<PathAccessControlEntry> pathAccessControlEntries = PathAccessControlEntry.parseList("user::rwx,group::r--,other::---,mask::rwx");

        StepVerifier.create(dc.setAccessControlList(pathAccessControlEntries, null, null))
            .assertNext(r -> {
                assertNotNull(r.getETag());
                assertNotNull(r.getLastModified());
            })
            .verifyComplete();
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    @Test
    public void perCallPolicy() {
        DataLakeFileSystemAsyncClient dataLakeFileSystemAsyncClient = getFileSystemClientBuilder(getFileSystemUrl())
            .addPolicy(getPerCallVersionPolicy()).credential(getDataLakeCredential()).buildAsyncClient();

        StepVerifier.create(dataLakeFileSystemAsyncClient.getPropertiesWithResponse(null))
            .assertNext(r -> assertEquals("2019-02-02", r.getHeaders().getValue(X_MS_VERSION)))
            .verifyComplete();

        StepVerifier.create(dataLakeFileSystemAsyncClient.getAccessPolicyWithResponse(null))
            .assertNext(r -> assertEquals("2019-02-02", r.getHeaders().getValue(X_MS_VERSION)))
            .verifyComplete();
    }
}
