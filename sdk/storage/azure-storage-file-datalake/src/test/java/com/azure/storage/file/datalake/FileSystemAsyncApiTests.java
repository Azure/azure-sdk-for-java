package com.azure.storage.file.datalake;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.FileSystemProperties;
import com.azure.storage.file.datalake.models.LeaseStateType;
import com.azure.storage.file.datalake.models.LeaseStatusType;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathProperties;
import com.azure.storage.file.datalake.models.PublicAccessType;
import com.azure.storage.file.datalake.options.DataLakePathDeleteOptions;
import com.azure.storage.file.datalake.options.FileSystemEncryptionScopeOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
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

        dataLakeFileSystemAsyncClient.createIfNotExistsWithResponse(null, publicAccess);

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
        String leaseID = setupFileSystemLeaseCondition(dataLakeFileSystemAsyncClient, RECEIVED_LEASE_ID);

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
        leaseID = setupFileSystemLeaseCondition(dataLakeFileSystemAsyncClient, leaseID);
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
            .setLeaseId(setupFileSystemLeaseCondition(dataLakeFileSystemAsyncClient, leaseID))
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
            .setLeaseId(setupFileSystemLeaseCondition(dataLakeFileSystemAsyncClient, leaseID))
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


}
