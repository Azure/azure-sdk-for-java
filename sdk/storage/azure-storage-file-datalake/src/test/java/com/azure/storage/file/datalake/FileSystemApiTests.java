// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.common.Utility;
import com.azure.storage.common.test.shared.extensions.PlaybackOnly;
import com.azure.storage.file.datalake.models.DataLakeAccessPolicy;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeSignedIdentifier;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.FileSystemProperties;
import com.azure.storage.file.datalake.models.LeaseDurationType;
import com.azure.storage.file.datalake.models.LeaseStateType;
import com.azure.storage.file.datalake.models.LeaseStatusType;
import com.azure.storage.file.datalake.models.ListPathsOptions;
import com.azure.storage.file.datalake.models.PathAccessControl;
import com.azure.storage.file.datalake.models.PathAccessControlEntry;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathItem;
import com.azure.storage.file.datalake.models.PathPermissions;
import com.azure.storage.file.datalake.models.PathProperties;
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

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileSystemApiTests extends DataLakeTestBase {
    @Test
    public void createAllNull() {
        // Overwrite the existing dataLakeFileSystemClient, which has already been created
        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());

        Response<?> response = dataLakeFileSystemClient.createWithResponse(null, null, null, null);

        assertEquals(201, response.getStatusCode());
        validateBasicHeaders(response.getHeaders());
    }

    @Test
    public void createMin() {
        dataLakeFileSystemClient = primaryDataLakeServiceClient.createFileSystem(generateFileSystemName());

        assertDoesNotThrow(dataLakeFileSystemClient::getProperties);
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz", "testFoo,testBar,testFizz,testBuzz"},
               nullValues = "null")
    public void createMetadata(String key1, String value1, String key2, String value2) {
        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }

        dataLakeFileSystemClient.createWithResponse(metadata, null, null, null);
        Response<FileSystemProperties> response = dataLakeFileSystemClient.getPropertiesWithResponse(null, null, null);

        assertEquals(metadata, response.getValue().getMetadata());
    }

    @ParameterizedTest
    @MethodSource("publicAccessSupplier")
    public void createPublicAccess(PublicAccessType publicAccess) {
        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());

        dataLakeFileSystemClient.createWithResponse(null, publicAccess, null, null);

        assertEquals(publicAccess, dataLakeFileSystemClient.getProperties().getDataLakePublicAccess());
    }

    private static Stream<PublicAccessType> publicAccessSupplier() {
        return Stream.of(PublicAccessType.BLOB, PublicAccessType.CONTAINER, null);
    }

    @Test
    public void createError() {
        DataLakeStorageException e = assertThrows(DataLakeStorageException.class, dataLakeFileSystemClient::create);

        assertEquals(409, e.getStatusCode());
        assertEquals(BlobErrorCode.CONTAINER_ALREADY_EXISTS.toString(), e.getErrorCode());
        assertEquals("The specified container already exists.", e.getServiceMessage());
    }

    @Test
    public void createEncryptionScope() {
        FileSystemEncryptionScopeOptions encryptionScope = new FileSystemEncryptionScopeOptions()
            .setDefaultEncryptionScope(ENCRYPTION_SCOPE_STRING)
            .setEncryptionScopeOverridePrevented(true);

        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());

        DataLakeFileSystemClient client = getFileSystemClientBuilder(dataLakeFileSystemClient.getFileSystemUrl())
            .credential(getDataLakeCredential())
            .fileSystemEncryptionScopeOptions(encryptionScope)
            .buildClient();

        client.create();
        FileSystemProperties properties = client.getProperties();

        assertEquals(ENCRYPTION_SCOPE_STRING, properties.getEncryptionScope());
        assertTrue(properties.isEncryptionScopeOverridePrevented());
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz", "testFoo,testBar,testFizz,testBuzz"},
               nullValues = "null")
    public void createMetadataEncryptionScope(String key1, String value1, String key2, String value2) {
        FileSystemEncryptionScopeOptions encryptionScope = new FileSystemEncryptionScopeOptions()
            .setDefaultEncryptionScope(ENCRYPTION_SCOPE_STRING)
            .setEncryptionScopeOverridePrevented(true);

        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());

        DataLakeFileSystemClient client = getFileSystemClientBuilder(dataLakeFileSystemClient.getFileSystemUrl())
            .credential(getDataLakeCredential())
            .fileSystemEncryptionScopeOptions(encryptionScope)
            .buildClient();

        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }

        client.createWithResponse(metadata, null, null, null);
        FileSystemProperties properties = client.getProperties();

        assertEquals(ENCRYPTION_SCOPE_STRING, properties.getEncryptionScope());
        assertTrue(properties.isEncryptionScopeOverridePrevented());
        assertEquals(metadata, properties.getMetadata());
    }

    @Test
    public void createIfNotExistsAllNull() {
        // Overwrite the existing dataLakeFileSystemClient, which has already been created
        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());

        Response<?> response = dataLakeFileSystemClient.createIfNotExistsWithResponse(null, null, null, null);

        assertEquals(201, response.getStatusCode());
        validateBasicHeaders(response.getHeaders());
    }

    @Test
    public void createIfNotExistsMin() {
        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());

        assertTrue(dataLakeFileSystemClient.createIfNotExists());
        assertTrue(dataLakeFileSystemClient.exists());
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz", "testFoo,testBar,testFizz,testBuzz"},
               nullValues = "null")
    public void createIfNotExistsMetadata(String key1, String value1, String key2, String value2) {
        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }

        dataLakeFileSystemClient.createIfNotExistsWithResponse(metadata, null, null, null);
        Response<FileSystemProperties> response = dataLakeFileSystemClient.getPropertiesWithResponse(null, null, null);

        assertEquals(metadata, response.getValue().getMetadata());
    }

    @ParameterizedTest
    @MethodSource("publicAccessSupplier")
    public void createIfNotExistsPublicAccess(PublicAccessType publicAccess) {
        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());

        dataLakeFileSystemClient.createIfNotExistsWithResponse(null, publicAccess, null, null);

        assertEquals(publicAccess, dataLakeFileSystemClient.getProperties().getDataLakePublicAccess());
    }

    @Test
    public void createIfNotExistsOnFileSystemThatAlreadyExists() {
        DataLakeFileSystemClient dataLakeFileSystemClient = primaryDataLakeServiceClient
            .getFileSystemClient(generateFileSystemName());

        assertEquals(201, dataLakeFileSystemClient.createIfNotExistsWithResponse(null, null, null, null)
            .getStatusCode());
        assertEquals(409, dataLakeFileSystemClient.createIfNotExistsWithResponse(null, null, null, null)
            .getStatusCode());
    }

    @Test
    public void createIfNotExistsEncryptionScope() {
        FileSystemEncryptionScopeOptions encryptionScope = new FileSystemEncryptionScopeOptions()
            .setDefaultEncryptionScope(ENCRYPTION_SCOPE_STRING)
            .setEncryptionScopeOverridePrevented(true);

        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());

        DataLakeFileSystemClient client = getFileSystemClientBuilder(dataLakeFileSystemClient.getFileSystemUrl())
            .credential(getDataLakeCredential())
            .fileSystemEncryptionScopeOptions(encryptionScope)
            .buildClient();

        client.createIfNotExists();
        FileSystemProperties properties = client.getProperties();

        assertEquals(ENCRYPTION_SCOPE_STRING, properties.getEncryptionScope());
        assertTrue(properties.isEncryptionScopeOverridePrevented());
    }

    @Test
    public void getPropertiesNull() {
        Response<FileSystemProperties> response = dataLakeFileSystemClient.getPropertiesWithResponse(null, null, null);

        validateBasicHeaders(response.getHeaders());
        assertNull(response.getValue().getDataLakePublicAccess());
        assertFalse(response.getValue().hasImmutabilityPolicy());
        assertFalse(response.getValue().hasLegalHold());
        assertNull(response.getValue().getLeaseDuration());
        assertEquals(LeaseStateType.AVAILABLE, response.getValue().getLeaseState());
        assertEquals(LeaseStatusType.UNLOCKED, response.getValue().getLeaseStatus());
        assertEquals(0, response.getValue().getMetadata().size());
    }

    @Test
    public void getPropertiesMin() {
        assertNotNull(dataLakeFileSystemClient.getProperties());
    }

    @Test
    public void getPropertiesLease() {
        String leaseID = setupFileSystemLeaseCondition(dataLakeFileSystemClient, RECEIVED_LEASE_ID);

        assertEquals(200, dataLakeFileSystemClient.getPropertiesWithResponse(leaseID, null, null).getStatusCode());
    }

    @Test
    public void getPropertiesLeaseFail() {
        assertThrows(DataLakeStorageException.class,
            () -> dataLakeFileSystemClient.getPropertiesWithResponse("garbage", null, null));
    }

    @Test
    public void getPropertiesError() {
        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());

        assertThrows(DataLakeStorageException.class, dataLakeFileSystemClient::getProperties);
    }

    @Test
    public void exists() {
        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());
        dataLakeFileSystemClient.create();

        assertTrue(dataLakeFileSystemClient.exists());
    }

    @Test
    public void existsNotExists() {
        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());

        assertFalse(dataLakeFileSystemClient.exists());
    }

    @Test
    public void setMetadata() {
        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());
        Map<String, String> metadata = Collections.singletonMap("key", "value");
        dataLakeFileSystemClient.createWithResponse(metadata, null, null, null);

        Response<?> response = dataLakeFileSystemClient.setMetadataWithResponse(null, null, null, null);

        assertEquals(200, response.getStatusCode());
        validateBasicHeaders(response.getHeaders());
        assertEquals(0, dataLakeFileSystemClient.getProperties().getMetadata().size());
    }

    @Test
    public void setMetadataMin() {
        Map<String, String> metadata = Collections.singletonMap("foo", "bar");

        dataLakeFileSystemClient.setMetadata(metadata);

        assertEquals(metadata, dataLakeFileSystemClient.getProperties().getMetadata());
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

        assertEquals(200, dataLakeFileSystemClient.setMetadataWithResponse(metadata, null, null, null).getStatusCode());
        assertEquals(metadata, dataLakeFileSystemClient.getProperties().getMetadata());
    }

    @ParameterizedTest
    @MethodSource("setMetadataACSupplier")
    public void setMetadataAC(OffsetDateTime modified, String leaseID) {
        leaseID = setupFileSystemLeaseCondition(dataLakeFileSystemClient, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions().setLeaseId(leaseID).setIfModifiedSince(modified);

        assertEquals(200, dataLakeFileSystemClient.setMetadataWithResponse(null, drc, null, null).getStatusCode());
    }

    private static Stream<Arguments> setMetadataACSupplier() {
        return Stream.of(
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

        assertThrows(DataLakeStorageException.class,
            () -> dataLakeFileSystemClient.setMetadataWithResponse(null, drc, null, null));
    }

    private static Stream<Arguments> setMetadataACFailSupplier() {
        return Stream.of(Arguments.of(NEW_DATE, null), Arguments.of(null, GARBAGE_LEASE_ID));
    }

    @ParameterizedTest
    @MethodSource("setMetadataACIllegalSupplier")
    public void setMetadataACIllegal(OffsetDateTime unmodified, String match, String noneMatch) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(UnsupportedOperationException.class,
            () -> dataLakeFileSystemClient.setMetadataWithResponse(null, drc, null, null));
    }

    private static Stream<Arguments> setMetadataACIllegalSupplier() {
        return Stream.of(
            Arguments.of(NEW_DATE, null, null),
            Arguments.of(null, RECEIVED_ETAG, null),
            Arguments.of(null, null, GARBAGE_ETAG)
        );
    }

    @Test
    public void setMetadataError() {
        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());

        assertThrows(DataLakeStorageException.class, () -> dataLakeFileSystemClient.setMetadata(null));
    }

    @Test
    public void delete() {
        Response<?> response = dataLakeFileSystemClient.deleteWithResponse(null, null, null);

        assertEquals(202, response.getStatusCode());
        assertNotNull(response.getHeaders().getValue("x-ms-request-id"));
        assertNotNull(response.getHeaders().getValue("x-ms-version"));
        assertNotNull(response.getHeaders().getValue(HttpHeaderName.DATE));
    }

    @Test
    public void deleteMin() {
        dataLakeFileSystemClient.delete();

        DataLakeStorageException e = assertThrows(DataLakeStorageException.class, dataLakeFileSystemClient::getProperties);
        assertEquals(404, e.getStatusCode());
        assertEquals(BlobErrorCode.CONTAINER_NOT_FOUND.toString(), e.getErrorCode());
        assertTrue(e.getServiceMessage().contains("The specified container does not exist."));
    }

    @ParameterizedTest
    @MethodSource("modifiedAndLeaseIdSupplier")
    public void deleteAC(OffsetDateTime modified, OffsetDateTime unmodified, String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupFileSystemLeaseCondition(dataLakeFileSystemClient, leaseID))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertEquals(202, dataLakeFileSystemClient.deleteWithResponse(drc, null, null).getStatusCode());
    }

    private static Stream<Arguments> modifiedAndLeaseIdSupplier() {
        return Stream.of(
            Arguments.of(null, null, null),
            Arguments.of(OLD_DATE, null, null),
            Arguments.of(null, NEW_DATE, null),
            Arguments.of(null, RECEIVED_LEASE_ID, null)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedAndLeaseIdSupplier")
    public void deleteACFail(OffsetDateTime modified, OffsetDateTime unmodified, String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class,
            () -> dataLakeFileSystemClient.deleteWithResponse(drc, null, null));
    }

    private static Stream<Arguments> invalidModifiedAndLeaseIdSupplier() {
        return Stream.of(
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

        assertThrows(UnsupportedOperationException.class,
            () -> dataLakeFileSystemClient.deleteWithResponse(drc, null, null));
    }

    private static Stream<Arguments> invalidMatchSupplier() {
        return Stream.of(
            Arguments.of(RECEIVED_ETAG, null),
            Arguments.of(null, GARBAGE_ETAG)
        );
    }

    @Test
    public void deleteError() {
        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());

        assertThrows(DataLakeStorageException.class, dataLakeFileSystemClient::delete);
    }

    @Test
    public void deleteIfExists() {
        DataLakeFileSystemClient dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());
        dataLakeFileSystemClient.create();

        Response<?> response = dataLakeFileSystemClient.deleteIfExistsWithResponse(null, null, null);

        assertEquals(202, response.getStatusCode());
        assertNotNull(response.getHeaders().getValue("x-ms-request-id"));
        assertNotNull(response.getHeaders().getValue("x-ms-version"));
        assertNotNull(response.getHeaders().getValue(HttpHeaderName.DATE));
    }

    @Test
    public void deleteIfExistsMin() {
        DataLakeFileSystemClient dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());
        dataLakeFileSystemClient.create();

        assertTrue(dataLakeFileSystemClient.exists());
        assertTrue(dataLakeFileSystemClient.deleteIfExists());

        assertThrows(DataLakeStorageException.class, dataLakeFileSystemClient::getProperties);
        assertFalse(dataLakeFileSystemClient.exists());
    }

    @Test
    public void deleteIfExistsOnFileSystemThatDoesNotExist() {
        DataLakeFileSystemClient dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());

        assertEquals(404, dataLakeFileSystemClient.deleteIfExistsWithResponse(null, null, null).getStatusCode());
        assertFalse(dataLakeFileSystemClient.exists());
    }

    // We can't guarantee that the requests will always happen before the container is garbage collected
    @PlaybackOnly
    def "Delete if exists file system that was already deleted"() {

        def dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())
        dataLakeFileSystemClient.create()
        dataLakeFileSystemClient.getBlobContainerClient().exists()


        def initialResponse = dataLakeFileSystemClient.deleteIfExistsWithResponse(null, null, null)
        def secondResponse = dataLakeFileSystemClient.deleteIfExistsWithResponse(null, null, null)


        !dataLakeFileSystemClient.getBlobContainerClient().exists()
        initialResponse.getStatusCode() == 202
        // Confirming the behavior of the api when the container is in the deleting state.
        // After delete has been called once but before it has been garbage collected
        secondResponse.getStatusCode() == 202
    }

    @ParameterizedTest
    @MethodSource("modifiedAndLeaseIdSupplier")
    public void deleteIfExistsAC(OffsetDateTime modified, OffsetDateTime unmodified, String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupFileSystemLeaseCondition(dataLakeFileSystemClient, leaseID))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertEquals(202, dataLakeFileSystemClient.deleteIfExistsWithResponse(
            new DataLakePathDeleteOptions().setRequestConditions(drc), null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedAndLeaseIdSupplier")
    public void deleteIfExistsACFail(OffsetDateTime modified, OffsetDateTime unmodified, String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class, () -> dataLakeFileSystemClient.deleteIfExistsWithResponse(
            new DataLakePathDeleteOptions().setRequestConditions(drc), null, null));
    }

    @ParameterizedTest
    @MethodSource("invalidMatchSupplier")
    public void deleteIfExistsACIllegal(String match, String noneMatch) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch);

        assertThrows(UnsupportedOperationException.class, () -> dataLakeFileSystemClient.deleteIfExistsWithResponse(
            new DataLakePathDeleteOptions().setRequestConditions(drc), null, null));
    }

    @Test
    public void createFileMin() {
        assertDoesNotThrow(() -> dataLakeFileSystemClient.createFile(generatePathName()));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void createFileOverwrite(boolean overwrite) {
        String pathName = generatePathName();
        dataLakeFileSystemClient.createFile(pathName);

        if (overwrite) {
            assertDoesNotThrow(() -> dataLakeFileSystemClient.createFile(pathName, overwrite));
        } else {
            assertThrows(DataLakeStorageException.class, () -> dataLakeFileSystemClient.createFile(pathName, overwrite));
        }
    }

    @Test
    public void createFileDefaults() {
        Response<?> createResponse = dataLakeFileSystemClient.createFileWithResponse(generatePathName(), null, null,
            null, null, null, null, null);

        assertEquals(201, createResponse.getStatusCode());
        validateBasicHeaders(createResponse.getHeaders());
    }

    @Test
    public void createFileError() {
        assertThrows(DataLakeStorageException.class, () ->dataLakeFileSystemClient.createFileWithResponse(
            generatePathName(), null, null, null, null, new DataLakeRequestConditions().setIfMatch("garbage"), null,
            Context.NONE));
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

        Response<PathProperties> response = dataLakeFileSystemClient.createFileWithResponse(generatePathName(),
            null, null, headers, null, null, null, null)
            .getValue()
            .getPropertiesWithResponse(null, null, null);

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;

        validatePathProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, null, contentType);
    }

    private static Stream<Arguments> cacheAndContentSupplier() {
        return Stream.of(
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

        assertEquals(metadata, dataLakeFileSystemClient.createFileWithResponse(generatePathName(), null, null, null,
            metadata, null, null, null).getValue().getProperties().getMetadata());
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void createFileAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        String pathName = generatePathName();
        DataLakeFileClient client = dataLakeFileSystemClient.getFileClient(pathName);
        client.create();
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(client, leaseID))
            .setIfMatch(setupPathMatchCondition(client, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertEquals(201, dataLakeFileSystemClient.createFileWithResponse(pathName, null, null, null, null, drc, null,
            null).getStatusCode());
    }

    private static Stream<Arguments> modifiedMatchAndLeaseIdSupplier() {
        return Stream.of(
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
        DataLakeFileClient client = dataLakeFileSystemClient.getFileClient(pathName);
        client.create();
        setupPathLeaseCondition(client, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(client, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class, () -> dataLakeFileSystemClient.createFileWithResponse(pathName,
            null, null, null, null, drc, null, Context.NONE));
    }

    private static Stream<Arguments> invalidModifiedMatchAndLeaseIdSupplier() {
        return Stream.of(
            Arguments.of(NEW_DATE, null, null, null, null),
            Arguments.of(null, OLD_DATE, null, null, null),
            Arguments.of(null, null, GARBAGE_ETAG, null, null),
            Arguments.of(null, null, null, RECEIVED_ETAG, null),
            Arguments.of(null, null, null, null, GARBAGE_LEASE_ID)
        );
    }

    @Test
    public void createFilePermissionsAndUmask() {
        assertEquals(201, dataLakeFileSystemClient.createFileWithResponse(generatePathName(), "0777", "0057", null,
            null, null, null, Context.NONE).getStatusCode());
    }

    @DisabledIf("olderThan20210608ServiceVersion")
    @Test
    public void createFileOptionsWithACL() {
        List<PathAccessControlEntry> pathAccessControlEntries = PathAccessControlEntry.parseList("user::rwx,group::r--,other::---,mask::rwx");
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setAccessControlList(pathAccessControlEntries);
        DataLakeFileClient client = dataLakeFileSystemClient.createFileWithResponse(generatePathName(), options, null, null)
            .getValue();

        List<PathAccessControlEntry> acl = client.getAccessControl().getAccessControlList();
        assertEquals(pathAccessControlEntries.get(0), acl.get(0)); // testing if owner is set the same
        assertEquals(pathAccessControlEntries.get(1), acl.get(1)); // testing if group is set the same
    }

    @DisabledIf("olderThan20210608ServiceVersion")
    @Test
    public void createFileOptionsWithOwnerAndGroup() {
        String ownerName = testResourceNamer.randomUuid();
        String groupName = testResourceNamer.randomUuid();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setOwner(ownerName)
            .setGroup(groupName);
        DataLakeFileClient result = dataLakeFileSystemClient.createFileWithResponse(generatePathName(), options, null, null)
            .getValue();

        assertEquals(ownerName, result.getAccessControl().getOwner());
        assertEquals(groupName, result.getAccessControl().getGroup());
    }

    @Test
    public void createFileOptionsWithNullOwnerAndGroup() {
        DataLakeFileClient result = dataLakeFileSystemClient.createFileWithResponse(generatePathName(),
            new DataLakePathCreateOptions(), null, null).getValue();

        assertEquals("$superuser", result.getAccessControl().getOwner());
        assertEquals("$superuser", result.getAccessControl().getGroup());
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
        DataLakeFileClient result = dataLakeFileSystemClient.createFileWithResponse(generatePathName(), options, null, null)
            .getValue();

        validatePathProperties(result.getPropertiesWithResponse(null, null, null),
            cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMD5, contentType);
    }

    private static Stream<Arguments> cacheAndContentWithMd5Supplier() {
        return Stream.of(
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
        Response<DataLakeFileClient> result = dataLakeFileSystemClient.createFileWithResponse(generatePathName(), options, null, null);

        assertEquals(201, result.getStatusCode());

        FileSystemProperties properties = dataLakeFileSystemClient.getProperties();
        // Directory adds a directory metadata value
        for(String k : metadata.keySet()) {
            assertTrue(properties.getMetadata().containsKey(k));
            assertEquals(metadata.get(k), properties.getMetadata().get(k));
        }
    }

    @Test
    public void createFileOptionsWithPermissionsAndUmask() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setPermissions("0777")
            .setUmask("0057");
        DataLakeFileClient result = dataLakeFileSystemClient.createFileWithResponse(generatePathName(), options, null, null)
            .getValue();

        PathAccessControl acl = result.getAccessControlWithResponse(true, null, null, null).getValue();

        assertEquals(PathPermissions.parseSymbolic("rwx-w----").toString(), acl.getPermissions().toString());
    }

    @DisabledIf("olderThan20210608ServiceVersion")
    @Test
    public void createFileOptionsWithLeaseId() {
        String leaseId = CoreUtils.randomUuid().toString();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setProposedLeaseId(leaseId).setLeaseDuration(15);

        assertEquals(201, dataLakeFileSystemClient.createFileWithResponse(generatePathName(), options, null, null)
            .getStatusCode());
    }

    @Test
    public void createFileOptionsWithLeaseIdError() {
        String leaseId = CoreUtils.randomUuid().toString();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setProposedLeaseId(leaseId);

        // lease duration must also be set, or else exception is thrown
        assertThrows(DataLakeStorageException.class,
            () -> dataLakeFileSystemClient.createFileWithResponse(generatePathName(), options, null, null));
    }

    @DisabledIf("olderThan20210608ServiceVersion")
    @Test
    public void createFileOptionsWithLeaseDuration() {
        String leaseId = CoreUtils.randomUuid().toString();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setLeaseDuration(15).setProposedLeaseId(leaseId);
        String fileName = generatePathName();

        assertEquals(201, dataLakeFileSystemClient.createFileWithResponse(fileName, options, null, null).getStatusCode());


        PathProperties fileProps = dataLakeFileSystemClient.getFileClient(fileName).getProperties();

        // assert whether lease has been acquired
        assertEquals(LeaseStatusType.LOCKED, fileProps.getLeaseStatus());
        assertEquals(LeaseStateType.LEASED, fileProps.getLeaseState());
        assertEquals(LeaseDurationType.FIXED, fileProps.getLeaseDuration());
    }

    @DisabledIf("olderThan20210608ServiceVersion")
    @ParameterizedTest
    @MethodSource("createFileOptionsWithTimeExpiresOnAbsoluteAndNeverExpireSupplier")
    public void createFileOptionsWithTimeExpiresOnAbsoluteAndNeverExpire(DataLakePathScheduleDeletionOptions deletionOptions) {
        assertEquals(201, dataLakeFileSystemClient.createFileWithResponse(generatePathName(),
            new DataLakePathCreateOptions().setScheduleDeletionOptions(deletionOptions), null, null).getStatusCode());
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

        assertEquals(201, dataLakeFileSystemClient.createFileWithResponse(fileName, options, null, null).getStatusCode());

        PathProperties fileProps = dataLakeFileSystemClient.getFileClient(fileName).getProperties();
        compareDatesWithPrecision(fileProps.getExpiresOn(), fileProps.getCreationTime().plusDays(6));
    }

    @Test
    public void createIfNotExistsFileMin() {
        assertTrue(dataLakeFileSystemClient.createFileIfNotExists(generatePathName()).exists());
    }

    @Test
    public void createIfNotExistsFileOverwrite() {
        String pathName = generatePathName();

        assertEquals(201, dataLakeFileSystemClient.createFileIfNotExistsWithResponse(pathName, null, null, null)
            .getStatusCode());
        assertEquals(409, dataLakeFileSystemClient.createFileIfNotExistsWithResponse(pathName, null, null, null)
            .getStatusCode());
    }

    @Test
    public void createIfNotExistsFileDefaults() {
        Response<?> createResponse = dataLakeFileSystemClient.createFileIfNotExistsWithResponse(generatePathName(), null, null, null);

        assertEquals(201, createResponse.getStatusCode());
        validateBasicHeaders(createResponse.getHeaders());
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

        Response<PathProperties> response = dataLakeFileSystemClient.createFileIfNotExistsWithResponse(generatePathName(),
            new DataLakePathCreateOptions().setPathHttpHeaders(headers), null, null)
            .getValue()
            .getPropertiesWithResponse(null, null, null);

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;

        validatePathProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, null, contentType);
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

        assertEquals(metadata, dataLakeFileSystemClient.createFileIfNotExistsWithResponse(generatePathName(),
            new DataLakePathCreateOptions().setMetadata(metadata), null, null).getValue().getProperties().getMetadata());
    }

    @Test
    public void createIfNotExistsFilePermissionsAndUmask() {
        assertEquals(201, dataLakeFileSystemClient.createFileIfNotExistsWithResponse(generatePathName(),
            new DataLakePathCreateOptions().setUmask("0057").setPermissions("0777"), null, Context.NONE)
            .getStatusCode());
    }

    @DisabledIf("olderThan20210608ServiceVersion")
    @Test
    public void createIfNotExistsFileOptionsWithACL() {
        List<PathAccessControlEntry> pathAccessControlEntries = PathAccessControlEntry.parseList("user::rwx,group::r--,other::---,mask::rwx");
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setAccessControlList(pathAccessControlEntries);
        DataLakeFileClient client = dataLakeFileSystemClient.createFileIfNotExistsWithResponse(generatePathName(), options, null, null)
            .getValue();

        List<PathAccessControlEntry> acl = client.getAccessControl().getAccessControlList();
        assertEquals(pathAccessControlEntries.get(0), acl.get(0)); // testing if owner is set the same
        assertEquals(pathAccessControlEntries.get(1), acl.get(1)); // testing if group is set the same
    }

    @DisabledIf("olderThan20210608ServiceVersion")
    @Test
    public void createIfNotExistsFileOptionsWithOwnerAndGroup() {
        String ownerName = testResourceNamer.randomUuid();
        String groupName = testResourceNamer.randomUuid();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setOwner(ownerName)
            .setGroup(groupName);
        DataLakeFileClient result = dataLakeFileSystemClient.createFileIfNotExistsWithResponse(generatePathName(), options, null, null)
            .getValue();

        assertEquals(ownerName, result.getAccessControl().getOwner());
        assertEquals(groupName, result.getAccessControl().getGroup());
    }

    @Test
    public void createIfNotExistsFileOptionsWithNullOwnerAndGroup() {
        DataLakeFileClient result = dataLakeFileSystemClient.createFileIfNotExistsWithResponse(generatePathName(), null, null, null)
            .getValue();

        assertEquals("$superuser", result.getAccessControl().getOwner());
        assertEquals("$superuser", result.getAccessControl().getGroup());
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

        DataLakeFileClient result = dataLakeFileSystemClient.createFileIfNotExistsWithResponse(generatePathName(),
            new DataLakePathCreateOptions().setPathHttpHeaders(putHeaders), null, null).getValue();


        validatePathProperties(result.getPropertiesWithResponse(null, null, null), cacheControl, contentDisposition,
            contentEncoding, contentLanguage, contentMD5, contentType);
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

        assertEquals(201, dataLakeFileSystemClient.createFileIfNotExistsWithResponse(generatePathName(),
            new DataLakePathCreateOptions().setMetadata(metadata), null, null).getStatusCode());

        FileSystemProperties properties = dataLakeFileSystemClient.getProperties();
        // Directory adds a directory metadata value
        for(String k : metadata.keySet()) {
            assertTrue(properties.getMetadata().containsKey(k));
            assertEquals(metadata.get(k), properties.getMetadata().get(k));
        }
    }

    def "Create if not exists file options with permissions and umask"() {

        def permissions = "0777"
        def umask = "0057"
        def options = new DataLakePathCreateOptions().setPermissions(permissions).setUmask(umask)
        def result = dataLakeFileSystemClient.createFileIfNotExistsWithResponse(generatePathName(), options, null, null).getValue()


        def acl = result.getAccessControlWithResponse(true, null, null, null).getValue()


        PathPermissions.parseSymbolic("rwx-w----").toString() == acl.getPermissions().toString()
    }

    @DisabledIf("olderThan20210608ServiceVersion")
    def "Create if not exists file options with lease id"() {

        def leaseId = UUID.randomUUID().toString()
        def options = new DataLakePathCreateOptions().setProposedLeaseId(leaseId).setLeaseDuration(15)
        def response = dataLakeFileSystemClient.createFileIfNotExistsWithResponse(generatePathName(), options, null, null)


        response.getStatusCode() == 201
    }

    def "Create if not exists file options with lease id error"() {

        def leaseId = UUID.randomUUID().toString()
        def options = new DataLakePathCreateOptions().setProposedLeaseId(leaseId)
        dataLakeFileSystemClient.createFileIfNotExistsWithResponse(generatePathName(), options, null, null)


        // lease duration must also be set, or else exception is thrown
        thrown(DataLakeStorageException)
    }

    @DisabledIf("olderThan20210608ServiceVersion")
    def "Create if not exists file options with lease duration"() {

        def leaseId = UUID.randomUUID().toString()
        def options = new DataLakePathCreateOptions().setLeaseDuration(15).setProposedLeaseId(leaseId)
        def fileName = generatePathName()
        def response = dataLakeFileSystemClient.createFileIfNotExistsWithResponse(fileName, options, null, null)


        response.getStatusCode() == 201
        def fileProps = dataLakeFileSystemClient.getFileClient(fileName).getProperties()
        // assert whether lease has been acquired
        fileProps.getLeaseStatus() == LeaseStatusType.LOCKED
        fileProps.getLeaseState() == LeaseStateType.LEASED
        fileProps.getLeaseDuration() == LeaseDurationType.FIXED

    }

    @DisabledIf("olderThan20210608ServiceVersion")
    def "Create if not exists file options with time expires on absolute and never expire"() {

        def options = new DataLakePathCreateOptions()
            .setScheduleDeletionOptions(deletionOptions)
        def response = dataLakeFileSystemClient.createFileIfNotExistsWithResponse(generatePathName(), options, null, null)


        response.getStatusCode() == 201

        where:
        deletionOptions                                                             || _
        new DataLakePathScheduleDeletionOptions(OffsetDateTime.now().plusDays(1))   || _
        null                                                                        || _

    }

    @DisabledIf("olderThan20210608ServiceVersion")
    def "Create if not exists file options with time to expire relative to now"() {

        def deletionOptions = new DataLakePathScheduleDeletionOptions(Duration.ofDays(6))
        def options = new DataLakePathCreateOptions()
            .setScheduleDeletionOptions(deletionOptions)
        def fileName = generatePathName()
        def response = dataLakeFileSystemClient.createFileWithResponse(fileName, options, null, null)


        response.getStatusCode() == 201
        def fileProps = dataLakeFileSystemClient.getFileClient(fileName).getProperties()
        def expireTime = fileProps.getExpiresOn()
        def expectedExpire = fileProps.getCreationTime().plusDays(6)
        compareDatesWithPrecision(expireTime, expectedExpire)
    }

    def "Delete file min"() {

        def pathName = generatePathName()
        dataLakeFileSystemClient.createFile(pathName)
        dataLakeFileSystemClient.deleteFileWithResponse(pathName, null, null, null).getStatusCode() == 200
    }

    def "Delete file file does not exist anymore"() {

        def pathName = generatePathName()
        def client = dataLakeFileSystemClient.createFile(pathName)
        dataLakeFileSystemClient.deleteFileWithResponse(pathName, null, null, null)
        client.getPropertiesWithResponse(null, null, null)


        def e = thrown(DataLakeStorageException)
        e.getResponse().getStatusCode() == 404
        e.getErrorCode() == BlobErrorCode.BLOB_NOT_FOUND.toString()
//        e.getServiceMessage().contains("The specified blob does not exist.")
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    def "Delete file AC"() {

        def pathName = generatePathName()
        def client = dataLakeFileSystemClient.createFile(pathName)
        match = setupPathMatchCondition(client, match)
        leaseID = setupPathLeaseCondition(client, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)


        dataLakeFileSystemClient.deleteFileWithResponse(pathName, drc, null, null).getStatusCode() == 200
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    def "Delete file AC fail"() {

        def pathName = generatePathName()
        def client = dataLakeFileSystemClient.createFile(pathName)
        noneMatch = setupPathMatchCondition(client, noneMatch)
        setupPathLeaseCondition(client, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)


        dataLakeFileSystemClient.deleteFileWithResponse(pathName, drc, null, null).getStatusCode()


        thrown(DataLakeStorageException)
    }

    def "Delete if exists file min"() {

        def pathName = generatePathName()
        dataLakeFileSystemClient.createFile(pathName)
        dataLakeFileSystemClient.deleteFileIfExists(pathName)
    }

    def "Delete if exists file null args"() {

        def pathName = generatePathName()
        dataLakeFileSystemClient.createFile(pathName)
        dataLakeFileSystemClient.deleteFileIfExistsWithResponse(pathName, null, null, null).getStatusCode() == 200
    }

    def "Delete if exists file that does not exist"() {

        def pathName = generatePathName()
        def response = dataLakeFileSystemClient.deleteFileIfExistsWithResponse(pathName, null, null, null)


        response.getStatusCode() == 404
    }

    def "Delete if exists file that was already deleted"() {

        def pathName = generatePathName()
        dataLakeFileSystemClient.createFile(pathName)


        def initialResponse = dataLakeFileSystemClient.deleteFileIfExistsWithResponse(pathName, null, null, null)
        def secondResponse = dataLakeFileSystemClient.deleteFileIfExistsWithResponse(pathName, null, null, null)


        initialResponse.getStatusCode() == 200
        secondResponse.getStatusCode() == 404
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    def "Delete if exists file AC"() {

        def pathName = generatePathName()
        def client = dataLakeFileSystemClient.createFile(pathName)
        match = setupPathMatchCondition(client, match)
        leaseID = setupPathLeaseCondition(client, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
        def options = new DataLakePathDeleteOptions().setRequestConditions(drc)


        dataLakeFileSystemClient.deleteFileIfExistsWithResponse(pathName, options, null, null).getStatusCode() == 200
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    def "Delete if exists file AC fail"() {

        def pathName = generatePathName()
        def client = dataLakeFileSystemClient.createFile(pathName)
        noneMatch = setupPathMatchCondition(client, noneMatch)
        setupPathLeaseCondition(client, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
        def options = new DataLakePathDeleteOptions().setRequestConditions(drc)


        dataLakeFileSystemClient.deleteFileIfExistsWithResponse(pathName, options, null, null).getStatusCode()


        thrown(DataLakeStorageException)
    }

    def "Create dir min"() {

        dataLakeFileSystemClient.createDirectory(generatePathName())


        notThrown(DataLakeStorageException)
    }

    @Unroll
    def "Create dir overwrite"() {

        def pathName = generatePathName()
        dataLakeFileSystemClient.createDirectory(pathName)


        def exceptionThrown = false
        try {
            dataLakeFileSystemClient.createDirectory(pathName, overwrite)
        } catch (DataLakeStorageException ignored) {
            exceptionThrown = true
        }


        exceptionThrown != overwrite

        where:
        overwrite || _
        true      || _
        false     || _
    }

    def "Create dir defaults"() {

        def createResponse = dataLakeFileSystemClient.createDirectoryWithResponse(generatePathName(), null, null, null, null, null, null, null)


        createResponse.getStatusCode() == 201
        validateBasicHeaders(createResponse.getHeaders())
    }

    def "Create dir error"() {

        dataLakeFileSystemClient.createDirectoryWithResponse(generatePathName(), null, null, null, null,
            new DataLakeRequestConditions().setIfMatch("garbage"), null,
            Context.NONE)


        thrown(DataLakeStorageException)
    }

    @ParameterizedTest
    @MethodSource("cacheAndContentSupplier")
    def "Create dir headers"() {
        // Create does not set md5

        def headers = new PathHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType)


        def client = dataLakeFileSystemClient.createDirectoryWithResponse(generatePathName(), null, null, headers, null, null, null, null).getValue()
        def response = client.getPropertiesWithResponse(null, null, null)

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType


        validatePathProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, null, contentType)
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz"}, nullValues = "null")
    def "Create dir metadata"() {

        Map<String, String> metadata = new HashMap<String, String>()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }


        def client = dataLakeFileSystemClient.createDirectoryWithResponse(generatePathName(), null, null, null, metadata, null, null, null).getValue()
        def response = client.getProperties()


        // Directory adds a directory metadata value
        for(String k : metadata.keySet()) {
            response.getMetadata().containsKey(k)
            response.getMetadata().get(k) == metadata.get(k)
        }
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    def "Create dir AC"() {

        def pathName = generatePathName()
        def client = dataLakeFileSystemClient.getDirectoryClient(pathName)
        client.create()
        match = setupPathMatchCondition(client, match)
        leaseID = setupPathLeaseCondition(client, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)



        dataLakeFileSystemClient.createDirectoryWithResponse(pathName, null, null, null, null, drc, null, null).getStatusCode() == 201
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    def "Create dir AC fail"() {

        def pathName = generatePathName()
        def client = dataLakeFileSystemClient.getDirectoryClient(pathName)
        client.create()
        noneMatch = setupPathMatchCondition(client, noneMatch)
        setupPathLeaseCondition(client, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)


        dataLakeFileSystemClient.createDirectoryWithResponse(pathName, null, null, null, null, drc, null, Context.NONE)


        thrown(DataLakeStorageException)
    }

    def "Create dir permissions and umask"() {

        def permissions = "0777"
        def umask = "0057"


        dataLakeFileSystemClient.createDirectoryWithResponse(generatePathName(), permissions, umask, null, null, null, null, Context.NONE).getStatusCode() == 201
    }

    @DisabledIf("olderThan20210608ServiceVersion")
    def "Create dir options with ACL"() {

        List<PathAccessControlEntry> pathAccessControlEntries = PathAccessControlEntry.parseList("user::rwx,group::r--,other::---,mask::rwx")
        def options = new DataLakePathCreateOptions().setAccessControlList(pathAccessControlEntries)
        def client = dataLakeFileSystemClient.createDirectoryWithResponse(generatePathName(), options, null, null).getValue()


        notThrown(DataLakeStorageException)
        def acl = client.getAccessControl().getAccessControlList()
        acl.get(0) == pathAccessControlEntries.get(0) // testing if owner is set the same
        acl.get(1) == pathAccessControlEntries.get(1) // testing if group is set the same
    }

    @DisabledIf("olderThan20210608ServiceVersion")
    def "Create dir options with owner and group"() {

        def ownerName = testResourceNamer.randomUuid()
        def groupName = testResourceNamer.randomUuid()
        def options = new DataLakePathCreateOptions().setOwner(ownerName).setGroup(groupName)
        def result = dataLakeFileSystemClient.createDirectoryWithResponse(generatePathName(), options, null, null).getValue()


        notThrown(DataLakeStorageException)
        result.getAccessControl().getOwner() == ownerName
        result.getAccessControl().getGroup() == groupName
    }

    def "Create dir options with null owner and group"() {

        def options = new DataLakePathCreateOptions().setOwner(null).setGroup(null)
        def result = dataLakeFileSystemClient.createDirectoryWithResponse(generatePathName(), options, null, null).getValue()


        notThrown(DataLakeStorageException)
        result.getAccessControl().getOwner() == "\$superuser"
        result.getAccessControl().getGroup() == "\$superuser"
    }

    @ParameterizedTest
    @MethodSource("cacheAndContentWithMd5Supplier")
    def "Create dir options with path http headers"() {

        def putHeaders = new PathHttpHeaders()
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType)

        def options = new DataLakePathCreateOptions().setPathHttpHeaders(putHeaders)

        def result = dataLakeFileSystemClient.createDirectoryWithResponse(generatePathName(), options, null, null).getValue()


        validatePathProperties(
            result.getPropertiesWithResponse(null, null, null),
            cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMD5, contentType)
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz"}, nullValues = "null")
    def "Create dir options with metadata"() {

        Map<String, String> metadata = new HashMap<String, String>()
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2)
        }
        def options = new DataLakePathCreateOptions().setMetadata(metadata)
        def result = dataLakeFileSystemClient.createDirectoryWithResponse(generatePathName(), options, null, null)


        result.getStatusCode() == 201
        def properties = dataLakeFileSystemClient.getProperties()
        // Directory adds a directory metadata value
        for(String k : metadata.keySet()) {
            properties.getMetadata().containsKey(k)
            properties.getMetadata().get(k) == metadata.get(k)
        }
    }

    def "Create dir options with permissions and umask"() {

        def permissions = "0777"
        def umask = "0057"
        def options = new DataLakePathCreateOptions()
            .setPermissions(permissions)
            .setUmask(umask)
        def result = dataLakeFileSystemClient.createDirectoryWithResponse(generatePathName(), options, null, null).getValue()


        def acl = result.getAccessControlWithResponse(true, null, null, null).getValue()


        PathPermissions.parseSymbolic("rwx-w----").toString() == acl.getPermissions().toString()
    }

    def "Create dir options with lease id"() {

        def leaseId = UUID.randomUUID().toString()
        def options = new DataLakePathCreateOptions().setProposedLeaseId(leaseId).setLeaseDuration(15)
        dataLakeFileSystemClient.createDirectoryWithResponse(generatePathName(), options, null, null)


        // lease id not supported for directories
        thrown(IllegalArgumentException)
    }

    def "Create dir options with lease duration"() {

        def leaseId = UUID.randomUUID().toString()
        def options = new DataLakePathCreateOptions().setLeaseDuration(15).setProposedLeaseId(leaseId)
        dataLakeFileSystemClient.createDirectoryWithResponse(generatePathName(), options, null, null)


        // lease duration not supported for directories
        thrown(IllegalArgumentException)
    }

    def "Create dir options with time expires on"() {

        def leaseId = UUID.randomUUID().toString()
        def deletionOptions = new DataLakePathScheduleDeletionOptions(OffsetDateTime.now().plusDays(1))
        def options = new DataLakePathCreateOptions()
            .setProposedLeaseId(leaseId)
            .setScheduleDeletionOptions(deletionOptions)
        dataLakeFileSystemClient.createFileWithResponse(generatePathName(), options, null, null)


        // expires on not supported for directories
        thrown(DataLakeStorageException)
    }

    def "Create dir options with time to expire"() {

        def deletionOptions = new DataLakePathScheduleDeletionOptions(Duration.ofDays(6))
        def options = new DataLakePathCreateOptions()
            .setScheduleDeletionOptions(deletionOptions)

        dataLakeFileSystemClient.createDirectoryWithResponse(generatePathName(), options, null, null)


        // time to expire not supported for directories
        thrown(IllegalArgumentException)

    }

    def "Create if not exists dir min"() {

        def client = dataLakeFileSystemClient.createDirectoryIfNotExists(generatePathName())


        client.exists()
    }

    def "Create if not exists dir defaults"() {

        def createResponse = dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(), new DataLakePathCreateOptions(), null, null)


        createResponse.getStatusCode() == 201
        validateBasicHeaders(createResponse.getHeaders())
    }

    def "Create if not exists dir that already exists"() {

        def dirName = generatePathName()

        def initialResponse = dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(dirName,
            new DataLakePathCreateOptions(), null, Context.NONE)
        def secondResponse = dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(dirName,
            new DataLakePathCreateOptions(), null, Context.NONE)


        initialResponse.getStatusCode() == 201
        secondResponse.getStatusCode() == 409
    }

    @ParameterizedTest
    @MethodSource("cacheAndContentSupplier")
    def "Create if not exists dir headers"() {
        // Create does not set md5

        def headers = new PathHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType)


        def client = dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(),
            new DataLakePathCreateOptions().setPathHttpHeaders(headers), null, null).getValue()
        def response = client.getPropertiesWithResponse(null, null, null)

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType


        validatePathProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, null, contentType)
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz"}, nullValues = "null")
    def "Create if not exists dir metadata"() {

        Map<String, String> metadata = new HashMap<String, String>()
        if (key1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null) {
            metadata.put(key2, value2)
        }


        def client = dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(),
            new DataLakePathCreateOptions().setMetadata(metadata), null, null).getValue()
        def response = client.getProperties()


        // Directory adds a directory metadata value
        for(String k : metadata.keySet()) {
            response.getMetadata().containsKey(k)
            response.getMetadata().get(k) == metadata.get(k)
        }
    }

    def "Create if not exists dir permissions and umask"() {

        def permissions = "0777"
        def umask = "0057"

        dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(),
            new DataLakePathCreateOptions().setPermissions(permissions).setUmask(umask),
            null, Context.NONE).getStatusCode() == 201
    }

    @DisabledIf("olderThan20210608ServiceVersion")
    def "Create if not exists dir options with ACL"() {

        List<PathAccessControlEntry> pathAccessControlEntries = PathAccessControlEntry.parseList("user::rwx,group::r--,other::---,mask::rwx")
        def options = new DataLakePathCreateOptions().setAccessControlList(pathAccessControlEntries)
        def client = dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options, null, null).getValue()


        notThrown(DataLakeStorageException)
        def acl = client.getAccessControl().getAccessControlList()
        acl.get(0) == pathAccessControlEntries.get(0) // testing if owner is set the same
        acl.get(1) == pathAccessControlEntries.get(1) // testing if group is set the same
    }

    @DisabledIf("olderThan20210608ServiceVersion")
    def "Create if not exists dir options with owner and group"() {

        def ownerName = testResourceNamer.randomUuid()
        def groupName = testResourceNamer.randomUuid()
        def options = new DataLakePathCreateOptions()
            .setOwner(ownerName)
            .setGroup(groupName)
        def result = dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options, null, null).getValue()


        notThrown(DataLakeStorageException)
        result.getAccessControl().getOwner() == ownerName
        result.getAccessControl().getGroup() == groupName
    }

    def "Create if not exists dir options with null owner and group"() {

        def options = new DataLakePathCreateOptions()
            .setOwner(null)
            .setGroup(null)
        def result = dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options, null, null).getValue()


        notThrown(DataLakeStorageException)
        result.getAccessControl().getOwner() == "\$superuser"
        result.getAccessControl().getGroup() == "\$superuser"
    }

    @ParameterizedTest
    @MethodSource("cacheAndContentWithMd5Supplier")
    def "Create if not exists dir options with path http headers"() {

        def putHeaders = new PathHttpHeaders()
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType)

        def options = new DataLakePathCreateOptions().setPathHttpHeaders(putHeaders)

        def result = dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options, null, null).getValue()


        validatePathProperties(
            result.getPropertiesWithResponse(null, null, null),
            cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMD5, contentType)
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz"}, nullValues = "null")
    def "Create if not exists dir options with metadata"() {

        Map<String, String> metadata = new HashMap<String, String>()
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2)
        }
        def options = new DataLakePathCreateOptions().setMetadata(metadata)
        def result = dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options, null, null)


        result.getStatusCode() == 201
        def properties = dataLakeFileSystemClient.getProperties()
        // Directory adds a directory metadata value
        for(String k : metadata.keySet()) {
            properties.getMetadata().containsKey(k)
            properties.getMetadata().get(k) == metadata.get(k)
        }
    }

    def "Create if not exists dir options with permissions and umask"() {

        def permissions = "0777"
        def umask = "0057"
        def options = new DataLakePathCreateOptions()
            .setPermissions(permissions)
            .setUmask(umask)
        def result = dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options, null, null).getValue()


        def acl = result.getAccessControlWithResponse(true, null, null, null).getValue()


        PathPermissions.parseSymbolic("rwx-w----").toString() == acl.getPermissions().toString()
    }

    def "Create if not exists dir options with lease id"() {

        def leaseId = UUID.randomUUID().toString()
        def options = new DataLakePathCreateOptions().setProposedLeaseId(leaseId).setLeaseDuration(15)
        dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options, null, null)


        // assert lease id not supported for directory
        thrown(IllegalArgumentException)
    }

    def "Create if not exists dir options with lease id error"() {

        def leaseId = UUID.randomUUID().toString()
        def options = new DataLakePathCreateOptions().setProposedLeaseId(leaseId)
        dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options, null, null)


        // assert lease duration not supported for directory
        thrown(IllegalArgumentException)
    }

    def "Create if not exists dir options with lease duration"() {

        def leaseId = UUID.randomUUID().toString()
        def options = new DataLakePathCreateOptions().setLeaseDuration(15).setProposedLeaseId(leaseId)
        dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options, null, null)


        // assert expires on not supported for directory
        thrown(IllegalArgumentException)
    }

    def "Create if not exists dir options with time expires on absolute"() {

        def deletionOptions = new DataLakePathScheduleDeletionOptions(OffsetDateTime.now().plusDays(1))
        def options = new DataLakePathCreateOptions()
            .setScheduleDeletionOptions(deletionOptions)
        dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options, null, null)


        // assert expires on not supported for directory
        thrown(IllegalArgumentException)
    }

    def "Create if not exists dir options with time to expire relative to now"() {

        def deletionOptions = new DataLakePathScheduleDeletionOptions(Duration.ofDays(6))
        def options = new DataLakePathCreateOptions()
            .setScheduleDeletionOptions(deletionOptions)

        dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options, null, null)


        // assert time to expire not supported for directory
        thrown(IllegalArgumentException)
    }

    def "Delete dir min"() {

        def pathName = generatePathName()
        dataLakeFileSystemClient.createDirectory(pathName)
        dataLakeFileSystemClient.deleteDirectoryWithResponse(pathName, false, null, null, null).getStatusCode() == 200
    }

    def "Delete dir recursive"() {

        def pathName = generatePathName()
        dataLakeFileSystemClient.createDirectory(pathName)
        dataLakeFileSystemClient.deleteDirectoryWithResponse(pathName, true, null, null, null).getStatusCode() == 200
    }

    def "Delete dir dir does not exist anymore"() {

        def pathName = generatePathName()
        def client = dataLakeFileSystemClient.createDirectory(pathName)
        dataLakeFileSystemClient.deleteDirectoryWithResponse(pathName, false, null, null, null)
        client.getPropertiesWithResponse(null, null, null)


        def e = thrown(DataLakeStorageException)
        e.getResponse().getStatusCode() == 404
        e.getErrorCode() == BlobErrorCode.BLOB_NOT_FOUND.toString()
//        e.getServiceMessage().contains("The specified blob does not exist.")
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    def "Delete dir AC"() {

        def pathName = generatePathName()
        def client = dataLakeFileSystemClient.createDirectory(pathName)
        match = setupPathMatchCondition(client, match)
        leaseID = setupPathLeaseCondition(client, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)


        dataLakeFileSystemClient.deleteDirectoryWithResponse(pathName, false, drc, null, null).getStatusCode() == 200
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    def "Delete dir AC fail"() {

        def pathName = generatePathName()
        def client = dataLakeFileSystemClient.createDirectory(pathName)
        noneMatch = setupPathMatchCondition(client, noneMatch)
        setupPathLeaseCondition(client, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)


        dataLakeFileSystemClient.deleteDirectoryWithResponse(pathName, false, drc, null, null).getStatusCode()


        thrown(DataLakeStorageException)
    }

    def "Delete if exists dir min"() {

        def pathName = generatePathName()
        dataLakeFileSystemClient.createDirectory(pathName)
        dataLakeFileSystemClient.deleteDirectoryIfExists(pathName)
    }

    def "Delete if exists dir null args"() {

        def pathName = generatePathName()
        dataLakeFileSystemClient.createDirectory(pathName)
        dataLakeFileSystemClient.deleteDirectoryIfExistsWithResponse(pathName, null, null, null).getStatusCode() == 200
    }

    def "Delete if exists dir recursive"() {

        def pathName = generatePathName()
        dataLakeFileSystemClient.createDirectory(pathName)
        dataLakeFileSystemClient.deleteDirectoryIfExistsWithResponse(pathName, new DataLakePathDeleteOptions().setIsRecursive(true), null, null).getStatusCode() == 200
    }

    def "Delete if exists dir that does not exist"() {

        def pathName = generatePathName()
        def response = dataLakeFileSystemClient.deleteDirectoryIfExistsWithResponse(pathName, null, null, null)


        response.getStatusCode() == 404
        !dataLakeFileSystemClient.getDirectoryClient(pathName).exists()
    }

    def "Delete if exists dir that was already deleted"() {

        def pathName = generatePathName()
        dataLakeFileSystemClient.createDirectory(pathName)


        def initialResponse = dataLakeFileSystemClient.deleteDirectoryIfExistsWithResponse(pathName, null, null, null)
        def secondResponse = dataLakeFileSystemClient.deleteDirectoryIfExistsWithResponse(pathName, null, null, null)


        initialResponse.getStatusCode() == 200
        secondResponse.getStatusCode() == 404

    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    def "Delete if exists dir AC"() {

        def pathName = generatePathName()
        def client = dataLakeFileSystemClient.createDirectory(pathName)
        match = setupPathMatchCondition(client, match)
        leaseID = setupPathLeaseCondition(client, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
        def options = new DataLakePathDeleteOptions().setRequestConditions(drc).setIsRecursive(false)


        dataLakeFileSystemClient.deleteDirectoryIfExistsWithResponse(pathName, options, null, null).getStatusCode() == 200
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    def "Delete if exists dir AC fail"() {

        def pathName = generatePathName()
        def client = dataLakeFileSystemClient.createDirectory(pathName)
        noneMatch = setupPathMatchCondition(client, noneMatch)
        setupPathLeaseCondition(client, leaseID)
        def drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
        def options = new DataLakePathDeleteOptions().setRequestConditions(drc).setIsRecursive(false)


        dataLakeFileSystemClient.deleteDirectoryIfExistsWithResponse(pathName, options, null, null).getStatusCode()


        thrown(DataLakeStorageException)
    }

    def "List paths"() {

        def dirName = generatePathName()
        dataLakeFileSystemClient.getDirectoryClient(dirName).create()

        def fileName = generatePathName()
        def fileClient = dataLakeFileSystemClient.getFileClient(fileName)
        fileClient.create()


        def response = dataLakeFileSystemClient.listPaths().iterator()


        def dirPath = response.next()
        dirPath.getName() == dirName
        dirPath.getETag()
        dirPath.getGroup()
        dirPath.getLastModified()
        dirPath.getOwner()
        dirPath.getPermissions()
//        dirPath.getContentLength() // known issue with service
        dirPath.isDirectory()

        response.hasNext()
        def filePath = response.next()
        filePath.getName() == fileName
        filePath.getETag()
        filePath.getGroup()
        filePath.getLastModified()
        filePath.getOwner()
        filePath.getPermissions()
//        filePath.getContentLength() // known issue with service
        !filePath.isDirectory()

        !response.hasNext()
    }

    @DisabledIf("olderThan20200210ServiceVersion")
    def "List paths expiry and creation"() {

        def dirName = generatePathName()
        dataLakeFileSystemClient.getDirectoryClient(dirName).create()

        def fileName = generatePathName()
        def fileClient = dataLakeFileSystemClient.getFileClient(fileName)
        fileClient.create()
        fileClient.scheduleDeletion(new FileScheduleDeletionOptions(OffsetDateTime.now().plusDays(2)))


        def response = dataLakeFileSystemClient.listPaths().iterator()


        def dirPath = response.next()
        dirPath.getName() == dirName
        dirPath.getCreationTime()
        !dirPath.getExpiryTime()

        def filePath = response.next()
        filePath.getExpiryTime()
        filePath.getCreationTime()
    }

    def "List paths recursive"() {

        def dirName = generatePathName()
        dataLakeFileSystemClient.getDirectoryClient(dirName).create()

        def fileName = generatePathName()
        dataLakeFileSystemClient.getFileClient(fileName).create()


        def response = dataLakeFileSystemClient.listPaths(new ListPathsOptions().setRecursive(true), null).iterator()


        def dirPath = response.next()
        response.hasNext()
        def filePath = response.next()
        !response.hasNext()
    }

    def "List paths return upn"() {

        def dirName = generatePathName()
        dataLakeFileSystemClient.getDirectoryClient(dirName).create()

        def fileName = generatePathName()
        dataLakeFileSystemClient.getFileClient(fileName).create()


        def response = dataLakeFileSystemClient.listPaths(new ListPathsOptions().setUserPrincipalNameReturned(true), null).iterator()


        def dirPath = response.next()
        response.hasNext()
        def filePath = response.next()
        !response.hasNext()
    }

    def "List paths max results"() {

        def dirName = generatePathName()
        dataLakeFileSystemClient.getDirectoryClient(dirName).create()

        def fileName = generatePathName()
        dataLakeFileSystemClient.getFileClient(fileName).create()


        def response = dataLakeFileSystemClient.listPaths(new ListPathsOptions().setMaxResults(1), null).iterator()


        def dirPath = response.next()
        response.hasNext()
        def filePath = response.next()
        !response.hasNext()
    }

    def "List paths max results by page"() {

        def dirName = generatePathName()
        dataLakeFileSystemClient.getDirectoryClient(dirName).create()

        def fileName = generatePathName()
        dataLakeFileSystemClient.getFileClient(fileName).create()


        for (def page : dataLakeFileSystemClient.listPaths(new ListPathsOptions(), null).iterableByPage(1)) {
            assert page.value.size() == 1
        }
    }

    def "List paths encryption scope"() {

        def encryptionScope = new FileSystemEncryptionScopeOptions()
            .setDefaultEncryptionScope(ENCRYPTION_SCOPE_STRING)
            .setEncryptionScopeOverridePrevented(true)

        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())

        def client = getFileSystemClientBuilder(dataLakeFileSystemClient.getFileSystemUrl())
            .credential(getDataLakeCredential())
            .fileSystemEncryptionScopeOptions(encryptionScope)
            .buildClient()

        client.create()

        def dirName = generatePathName()
        client.getDirectoryClient(dirName).create()

        def fileName = generatePathName()
        def fileClient = dataLakeFileSystemClient.getFileClient(fileName)
        fileClient.create()


        def response = dataLakeFileSystemClient.listPaths().iterator()


        def dirPath = response.next()
        dirPath.getName() == dirName
        dirPath.getETag()
        dirPath.getGroup()
        dirPath.getLastModified()
        dirPath.getOwner()
        dirPath.getPermissions()
        dirPath.isDirectory()
        dirPath.getEncryptionScope() == ENCRYPTION_SCOPE_STRING

        response.hasNext()
        def filePath = response.next()
        filePath.getName() == fileName
        filePath.getETag()
        filePath.getGroup()
        filePath.getLastModified()
        filePath.getOwner()
        filePath.getPermissions()
        filePath.getEncryptionScope() == ENCRYPTION_SCOPE_STRING
        !filePath.isDirectory()

        !response.hasNext()
    }

    def "Async list paths max results by page"() {

        def dirName = generatePathName()
        fscAsync.getDirectoryAsyncClient(dirName).create().block()

        def fileName = generatePathName()
        fscAsync.getFileAsyncClient(fileName).create().block()


        for (def page : fscAsync.listPaths(new ListPathsOptions()).byPage(1).collectList().block()) {
            assert page.value.size() == 1
        }
    }

    @Unroll
    def "Create URL special chars encoded"() {
        // This test checks that we handle path names with encoded special characters correctly.

        def fc1 = dataLakeFileSystemClient.getFileClient(name + "file1")
        def fc2 = dataLakeFileSystemClient.getFileClient(name + "file2")
        def dc1 = dataLakeFileSystemClient.getDirectoryClient(name + "dir1")
        def dc2 = dataLakeFileSystemClient.getDirectoryClient(name + "dir2")


        fc1.createWithResponse(null, null, null, null, null, null, null).getStatusCode() == 201
        fc2.create()
        fc2.getPropertiesWithResponse(null, null, null).getStatusCode() == 200
        fc2.appendWithResponse(data.defaultInputStream, 0, data.defaultDataSize, null, null, null, null).getStatusCode() == 202
        dc1.createWithResponse(null, null, null, null, null, null, null).getStatusCode() == 201
        dc2.create()
        dc2.getPropertiesWithResponse(null, null, null).getStatusCode() == 200


        def paths = dataLakeFileSystemClient.listPaths().iterator()


        paths.next().getName() == Utility.urlDecode(name) + "dir1"
        paths.next().getName() == Utility.urlDecode(name) + "dir2"
        paths.next().getName() == Utility.urlDecode(name) + "file1"
        paths.next().getName() == Utility.urlDecode(name) + "file2"

        // Note you cannot use the / character in a path in datalake unless it is to specify an absolute path
        where:
        name                                                     | _
        "%E4%B8%AD%E6%96%87"                                     | _
        "az%5B%5D"                                               | _
        "hello%20world"                                          | _
        "hello%26world"                                          | _
        "%21%2A%27%28%29%3B%3A%40%26%3D%2B%24%2C%3F%23%5B%5D"    | _
    }

    @Unroll
    def "Set access policy"() {

        def response = dataLakeFileSystemClient.setAccessPolicyWithResponse(access, null, null, null, null)


        validateBasicHeaders(response.getHeaders())
        dataLakeFileSystemClient.getProperties().getDataLakePublicAccess() == access

        where:
        access                     | _
        PublicAccessType.BLOB      | _
        PublicAccessType.CONTAINER | _
        null                       | _
    }

    def "Set access policy min access"() {

        dataLakeFileSystemClient.setAccessPolicy(PublicAccessType.CONTAINER, null)


        dataLakeFileSystemClient.getProperties().getDataLakePublicAccess() == PublicAccessType.CONTAINER
    }

    def "Set access policy min ids"() {

        def identifier = new DataLakeSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new DataLakeAccessPolicy()
                .setStartsOn(OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime())
                .setExpiresOn(OffsetDateTime.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime()
                    .plusDays(1))
                .setPermissions("r"))

        def ids = [identifier] as List


        dataLakeFileSystemClient.setAccessPolicy(null, ids)


        dataLakeFileSystemClient.getAccessPolicy().getIdentifiers().get(0).getId() == "0000"
    }

    def "Set access policy ids"() {

        def identifier = new DataLakeSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new DataLakeAccessPolicy()
                .setStartsOn(namer.getUtcNow())
                .setExpiresOn(namer.getUtcNow().plusDays(1))
                .setPermissions("r"))
        def identifier2 = new DataLakeSignedIdentifier()
            .setId("0001")
            .setAccessPolicy(new DataLakeAccessPolicy()
                .setStartsOn(namer.getUtcNow())
                .setExpiresOn(namer.getUtcNow().plusDays(2))
                .setPermissions("w"))
        def ids = [identifier, identifier2] as List


        def response = dataLakeFileSystemClient.setAccessPolicyWithResponse(null, ids, null, null, null)
        def receivedIdentifiers = dataLakeFileSystemClient.getAccessPolicyWithResponse(null, null, null).getValue().getIdentifiers()


        response.getStatusCode() == 200
        validateBasicHeaders(response.getHeaders())
        receivedIdentifiers.get(0).getAccessPolicy().getExpiresOn() == identifier.getAccessPolicy().getExpiresOn().truncatedTo(
            ChronoUnit.SECONDS)
        receivedIdentifiers.get(0).getAccessPolicy().getStartsOn() == identifier.getAccessPolicy().getStartsOn().truncatedTo(ChronoUnit.SECONDS)
        receivedIdentifiers.get(0).getAccessPolicy().getPermissions() == identifier.getAccessPolicy().getPermissions()
        receivedIdentifiers.get(1).getAccessPolicy().getExpiresOn() == identifier2.getAccessPolicy().getExpiresOn().truncatedTo(ChronoUnit.SECONDS)
        receivedIdentifiers.get(1).getAccessPolicy().getStartsOn() == identifier2.getAccessPolicy().getStartsOn().truncatedTo(ChronoUnit.SECONDS)
        receivedIdentifiers.get(1).getAccessPolicy().getPermissions() == identifier2.getAccessPolicy().getPermissions()
    }

    @ParameterizedTest
    @MethodSource("modifiedAndLeaseIdSupplier")
    def "Set access policy AC"() {

        leaseID = setupFileSystemLeaseCondition(dataLakeFileSystemClient, leaseID)
        def cac = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)


        dataLakeFileSystemClient.setAccessPolicyWithResponse(null, null, cac, null, null).getStatusCode() == 200
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedAndLeaseIdSupplier")
    def "Set access policy AC fail"() {

        def cac = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)


        dataLakeFileSystemClient.setAccessPolicyWithResponse(null, null, cac, null, null)


        thrown(DataLakeStorageException)
    }

    @ParameterizedTest
    @MethodSource("invalidMatchSupplier")
    def "Set access policy AC illegal"() {

        def mac = new DataLakeRequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch)


        dataLakeFileSystemClient.setAccessPolicyWithResponse(null, null, mac, null, null)


        thrown(UnsupportedOperationException)
    }

    def "Set access policy error"() {

        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())


        dataLakeFileSystemClient.setAccessPolicy(null, null)


        thrown(DataLakeStorageException)
    }

    def "Get access policy"() {

        def identifier = new DataLakeSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new DataLakeAccessPolicy()
                .setStartsOn(namer.getUtcNow())
                .setExpiresOn(namer.getUtcNow().plusDays(1))
                .setPermissions("r"))
        def ids = [identifier] as List
        dataLakeFileSystemClient.setAccessPolicy(PublicAccessType.BLOB, ids)
        def response = dataLakeFileSystemClient.getAccessPolicyWithResponse(null, null, null)


        response.getStatusCode() == 200
        response.getValue().getDataLakeAccessType() == PublicAccessType.BLOB
        validateBasicHeaders(response.getHeaders())
        response.getValue().getIdentifiers().get(0).getAccessPolicy().getExpiresOn() == identifier.getAccessPolicy().getExpiresOn().truncatedTo(ChronoUnit.SECONDS)
        response.getValue().getIdentifiers().get(0).getAccessPolicy().getStartsOn() == identifier.getAccessPolicy().getStartsOn().truncatedTo(ChronoUnit.SECONDS)
        response.getValue().getIdentifiers().get(0).getAccessPolicy().getPermissions() == identifier.getAccessPolicy().getPermissions()
    }

    def "Get access policy lease"() {

        def leaseID = setupFileSystemLeaseCondition(dataLakeFileSystemClient, RECEIVED_LEASE_ID)


        dataLakeFileSystemClient.getAccessPolicyWithResponse(leaseID, null, null).getStatusCode() == 200
    }

    def "Get access policy lease fail"() {

        dataLakeFileSystemClient.getAccessPolicyWithResponse(GARBAGE_LEASE_ID, null, null)


        thrown(DataLakeStorageException)
    }

    def "Get access policy error"() {

        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())


        dataLakeFileSystemClient.getAccessPolicy()


        thrown(DataLakeStorageException)
    }

    def "Builder bearer token validation"() {
        // Technically no additional checks need to be added to datalake builder since the corresponding blob builder fails

        String endpoint = BlobUrlParts.parse(dataLakeFileSystemClient.getFileSystemUrl()).setScheme("http").toUrl()
        def builder = new DataLakeFileSystemClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)


        builder.buildClient()


        thrown(IllegalArgumentException)
    }

    def "List Paths OAuth"() {

        def client = getOAuthServiceClient()
        def fsClient = client.getFileSystemClient(dataLakeFileSystemClient.getFileSystemName())
        fsClient.createFile(generatePathName())


        Iterator<PathItem> items = fsClient.listPaths().iterator()


        items.hasNext()
    }

    def "Set ACL root directory"() {

        def dc = dataLakeFileSystemClient.getRootDirectoryClient()

        List<PathAccessControlEntry> pathAccessControlEntries = PathAccessControlEntry.parseList("user::rwx,group::r--,other::---,mask::rwx")


        def resp = dc.setAccessControlList(pathAccessControlEntries, null, null)


        notThrown(DataLakeStorageException)
        resp.getETag()
        resp.getLastModified()
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials and auth would fail because we changed a signed header.
    def "Per call policy"() {

        def dataLakeFileSystemClient = getFileSystemClientBuilder(dataLakeFileSystemClient.getFileSystemUrl()).addPolicy(getPerCallVersionPolicy()).credential(getDataLakeCredential()).buildClient()

         "blob endpoint"
        def response = dataLakeFileSystemClient.getPropertiesWithResponse(null, null, null)


        notThrown(DataLakeStorageException)
        response.getHeaders().getValue("x-ms-version") == "2019-02-02"

         "dfs endpoint"
        response = dataLakeFileSystemClient.getAccessPolicyWithResponse(null, null, null)


        notThrown(DataLakeStorageException)
        response.getHeaders().getValue("x-ms-version") == "2019-02-02"
    }

//    def "Rename"() {
//
//        def newName = generateFileSystemName()
//
//
//        def renamedContainer = dataLakeFileSystemClient.rename(newName)
//
//
//        renamedContainer.getPropertiesWithResponse(null, null, null).getStatusCode() == 200
//
//        cleanup:
//        renamedContainer.delete()
//    }

//    def "Rename sas"() {
//
//        def service = new AccountSasService()
//            .setBlobAccess(true)
//        def resourceType = new AccountSasResourceType()
//            .setContainer(true)
//            .setService(true)
//            .setObject(true)
//        def permissions = new AccountSasPermission()
//            .setReadPermission(true)
//            .setCreatePermission(true)
//            .setWritePermission(true)
//            .setDeletePermission(true)
//        def expiryTime = namer.getUtcNow().plusDays(1)
//
//        def newName = generateFileSystemName()
//        def sasValues = new AccountSasSignatureValues(expiryTime, permissions, service, resourceType)
//        def sas = primaryDataLakeServiceClient.generateAccountSas(sasValues)
//        def sasClient = getFileSystemClient(sas, dataLakeFileSystemClient.getFileSystemUrl())
//
//
//        def renamedContainer = sasClient.rename(newName)
//
//
//        renamedContainer.getPropertiesWithResponse(null, null, null).getStatusCode() == 200
//
//        cleanup:
//        renamedContainer.delete()
//    }

//    @Unroll
//    def "Rename AC"() {
//
//        leaseID = setupFileSystemLeaseCondition(dataLakeFileSystemClient, leaseID)
//        def cac = new DataLakeRequestConditions()
//            .setLeaseId(leaseID)
//
//
//        dataLakeFileSystemClient.renameWithResponse(new FileSystemRenameOptions(generateFileSystemName()).setRequestConditions(cac),
//            null, null).getStatusCode() == 200
//
//        where:
//        leaseID         || _
//        null            || _
//        RECEIVED_LEASE_ID || _
//    }

//    @Unroll
//    def "Rename AC fail"() {
//
//        def cac = new DataLakeRequestConditions()
//            .setLeaseId(leaseID)
//
//
//        dataLakeFileSystemClient.renameWithResponse(new FileSystemRenameOptions(generateFileSystemName()).setRequestConditions(cac),
//            null, null)
//
//
//        thrown(DataLakeStorageException)
//
//        where:
//        leaseID         || _
//        GARBAGE_LEASE_ID  || _
//    }

//    @Unroll
//    def "Rename AC illegal"() {
//
//        def ac = new DataLakeRequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch).setIfModifiedSince(modified).setIfUnmodifiedSince(unmodified)
//
//
//        dataLakeFileSystemClient.renameWithResponse(new FileSystemRenameOptions(generateFileSystemName()).setRequestConditions(ac),
//            null, null)
//
//
//        thrown(UnsupportedOperationException)
//
//        where:
//        modified | unmodified | match        | noneMatch
//        OLD_DATE  | null       | null         | null
//        null     | NEW_DATE    | null         | null
//        null     | null       | RECEIVED_ETAG | null
//        null     | null       | null         | GARBAGE_ETAG
//    }

//    def "Rename error"() {
//
//        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName())
//        def newName = generateFileSystemName()
//
//
//        dataLakeFileSystemClient.rename(newName)
//
//
//        thrown(DataLakeStorageException)
//    }

    private static boolean olderThan20200210ServiceVersion() {
        return olderThan(DataLakeServiceVersion.V2020_02_10);
    }

    private static boolean olderThan20210608ServiceVersion() {
        return olderThan(DataLakeServiceVersion.V2021_06_08);
    }
}
