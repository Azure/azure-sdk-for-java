package com.azure.storage.file.datalake;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.FileSystemProperties;
import com.azure.storage.file.datalake.models.LeaseDurationType;
import com.azure.storage.file.datalake.models.LeaseStateType;
import com.azure.storage.file.datalake.models.LeaseStatusType;
import com.azure.storage.file.datalake.models.PathAccessControl;
import com.azure.storage.file.datalake.models.PathAccessControlEntry;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathPermissions;
import com.azure.storage.file.datalake.models.PathProperties;
import com.azure.storage.file.datalake.models.PublicAccessType;
import com.azure.storage.file.datalake.options.DataLakePathCreateOptions;
import com.azure.storage.file.datalake.options.DataLakePathDeleteOptions;
import com.azure.storage.file.datalake.options.DataLakePathScheduleDeletionOptions;
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

    @DisabledIf("olderThan20210608ServiceVersion")
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

    @DisabledIf("olderThan20210608ServiceVersion")
    @Test
    public void createFileOptionsWithOwnerAndGroup() {
        String ownerName = testResourceNamer.randomUuid();
        String groupName = testResourceNamer.randomUuid();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setOwner(ownerName)
            .setGroup(groupName);

        StepVerifier.create(dataLakeFileSystemAsyncClient.createFileWithResponse(generatePathName(), options)
            .flatMap(r -> r.getValue().getAccessControl()))
            .assertNext(ac -> {
                assertEquals(ownerName, ac.getOwner());
                assertEquals(groupName, ac.getGroup());
            })
            .verifyComplete();
    }

    @Test
    public void createFileOptionsWithNullOwnerAndGroup() {
        StepVerifier.create(dataLakeFileSystemAsyncClient.createFileWithResponse(generatePathName(), new DataLakePathCreateOptions())
            .flatMap(r -> r.getValue().getAccessControl()))
            .assertNext(ac -> {
                assertEquals("$superuser", ac.getOwner());
                assertEquals("$superuser", ac.getGroup());
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

    @DisabledIf("olderThan20210608ServiceVersion")
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

    @DisabledIf("olderThan20210608ServiceVersion")
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

    @DisabledIf("olderThan20210608ServiceVersion")
    @ParameterizedTest
    @MethodSource("createFileOptionsWithTimeExpiresOnAbsoluteAndNeverExpireSupplier")
    public void createFileOptionsWithTimeExpiresOnAbsoluteAndNeverExpire(DataLakePathScheduleDeletionOptions deletionOptions) {
        assertAsyncResponseStatusCode(dataLakeFileSystemAsyncClient.createFileWithResponse(generatePathName(),
            new DataLakePathCreateOptions().setScheduleDeletionOptions(deletionOptions)), 201);
    }

    private static Stream<DataLakePathScheduleDeletionOptions> createFileOptionsWithTimeExpiresOnAbsoluteAndNeverExpireSupplier() {
        return Stream.of(new DataLakePathScheduleDeletionOptions(OffsetDateTime.now().plusDays(1)), null);
    }

    @DisabledIf("olderThan20210608ServiceVersion")
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
            .flatMap(r -> r.exists()))
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

    @DisabledIf("olderThan20210608ServiceVersion")
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

    @DisabledIf("olderThan20210608ServiceVersion")
    @Test
    public void createIfNotExistsFileOptionsWithOwnerAndGroup() {
        String ownerName = testResourceNamer.randomUuid();
        String groupName = testResourceNamer.randomUuid();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setOwner(ownerName)
            .setGroup(groupName);

        StepVerifier.create(dataLakeFileSystemAsyncClient.createFileIfNotExistsWithResponse(generatePathName(), options)
            .flatMap(r -> r.getValue().getAccessControl()))
            .assertNext(ac -> {
                assertEquals(ownerName, ac.getOwner());
                assertEquals(groupName, ac.getGroup());
            })
            .verifyComplete();
    }

    @Test
    public void createIfNotExistsFileOptionsWithNullOwnerAndGroup() {
        StepVerifier.create(dataLakeFileSystemAsyncClient.createFileIfNotExistsWithResponse(generatePathName(), null)
            .flatMap(r -> r.getValue().getAccessControl()))
            .assertNext(ac -> {
                assertEquals("$superuser", ac.getOwner());
                assertEquals("$superuser", ac.getGroup());
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

        StepVerifier.create( dataLakeFileSystemAsyncClient.createFileIfNotExistsWithResponse(generatePathName(),
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
                for (String k : metadata.keySet()) {
                    assertTrue(p.getMetadata().containsKey(k));
                    assertEquals(metadata.get(k), p.getMetadata().get(k));
                }
            })
            .verifyComplete();
    }


    private static boolean olderThan20200210ServiceVersion() {
        return olderThan(DataLakeServiceVersion.V2020_02_10);
    }

    private static boolean olderThan20210608ServiceVersion() {
        return olderThan(DataLakeServiceVersion.V2021_06_08);
    }
}
