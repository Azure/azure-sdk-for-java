// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.common.Utility;
import com.azure.storage.file.datalake.models.DataLakeAccessPolicy;
import com.azure.storage.file.datalake.models.DataLakeAudience;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeSignedIdentifier;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.FileSystemAccessPolicies;
import com.azure.storage.file.datalake.models.FileSystemProperties;
import com.azure.storage.file.datalake.models.LeaseDurationType;
import com.azure.storage.file.datalake.models.LeaseStateType;
import com.azure.storage.file.datalake.models.LeaseStatusType;
import com.azure.storage.file.datalake.models.ListPathsOptions;
import com.azure.storage.file.datalake.models.PathAccessControl;
import com.azure.storage.file.datalake.models.PathAccessControlEntry;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathInfo;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

        assertThrows(DataLakeStorageException.class,
            () -> dataLakeFileSystemClient.setMetadataWithResponse(null, drc, null, null));
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

        assertThrows(UnsupportedOperationException.class,
            () -> dataLakeFileSystemClient.setMetadataWithResponse(null, drc, null, null));
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
        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());

        assertThrows(DataLakeStorageException.class, () -> dataLakeFileSystemClient.setMetadata(null));
    }

    @Test
    public void delete() {
        Response<?> response = dataLakeFileSystemClient.deleteWithResponse(null, null, null);

        assertEquals(202, response.getStatusCode());
        assertNotNull(response.getHeaders().getValue(X_MS_REQUEST_ID));
        assertNotNull(response.getHeaders().getValue(X_MS_VERSION));
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

        assertThrows(DataLakeStorageException.class,
            () -> dataLakeFileSystemClient.deleteWithResponse(drc, null, null));
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

        assertThrows(UnsupportedOperationException.class,
            () -> dataLakeFileSystemClient.deleteWithResponse(drc, null, null));
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
        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());

        assertThrows(DataLakeStorageException.class, dataLakeFileSystemClient::delete);
    }

    @Test
    public void deleteIfExists() {
        DataLakeFileSystemClient dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());
        dataLakeFileSystemClient.create();

        Response<?> response = dataLakeFileSystemClient.deleteIfExistsWithResponse(null, null, null);

        assertEquals(202, response.getStatusCode());
        assertNotNull(response.getHeaders().getValue(X_MS_REQUEST_ID));
        assertNotNull(response.getHeaders().getValue(X_MS_VERSION));
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
            assertDoesNotThrow(() -> dataLakeFileSystemClient.createFile(pathName, true));
        } else {
            assertThrows(DataLakeStorageException.class, () -> dataLakeFileSystemClient.createFile(pathName, false));
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
        assertThrows(DataLakeStorageException.class, () -> dataLakeFileSystemClient.createFileWithResponse(
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
        assertEquals(201, dataLakeFileSystemClient.createFileWithResponse(generatePathName(), "0777", "0057", null,
            null, null, null, Context.NONE).getStatusCode());
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
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

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
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
        Response<DataLakeFileClient> result = dataLakeFileSystemClient.createFileWithResponse(generatePathName(), options, null, null);

        assertEquals(201, result.getStatusCode());

        PathProperties properties = result.getValue().getProperties();
        // Directory adds a directory metadata value
        for (String k : metadata.keySet()) {
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

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
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

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
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

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
    @ParameterizedTest
    @MethodSource("createFileOptionsWithTimeExpiresOnAbsoluteAndNeverExpireSupplier")
    public void createFileOptionsWithTimeExpiresOnAbsoluteAndNeverExpire(DataLakePathScheduleDeletionOptions deletionOptions) {
        assertEquals(201, dataLakeFileSystemClient.createFileWithResponse(generatePathName(),
            new DataLakePathCreateOptions().setScheduleDeletionOptions(deletionOptions), null, null).getStatusCode());
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

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
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

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
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

        Response<DataLakeFileClient> response = dataLakeFileSystemClient.createFileIfNotExistsWithResponse(generatePathName(),
            new DataLakePathCreateOptions().setMetadata(metadata), null, null);
        assertEquals(201, response.getStatusCode());

        PathProperties properties = response.getValue().getProperties();
        // Directory adds a directory metadata value
        for (String k : metadata.keySet()) {
            assertTrue(properties.getMetadata().containsKey(k));
            assertEquals(metadata.get(k), properties.getMetadata().get(k));
        }
    }

    @Test
    public void createIfNotExistsFileOptionsWithPermissionsAndUmask() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setPermissions("0777").setUmask("0057");
        DataLakeFileClient result = dataLakeFileSystemClient.createFileIfNotExistsWithResponse(generatePathName(), options, null, null)
            .getValue();

        PathAccessControl acl = result.getAccessControlWithResponse(true, null, null, null).getValue();

        assertEquals(PathPermissions.parseSymbolic("rwx-w----").toString(), acl.getPermissions().toString());
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
    @Test
    public void createIfNotExistsFileOptionsWithLeaseId() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setProposedLeaseId(CoreUtils.randomUuid().toString())
            .setLeaseDuration(15);

        assertEquals(201, dataLakeFileSystemClient.createFileIfNotExistsWithResponse(generatePathName(), options, null, null)
            .getStatusCode());
    }

    @Test
    public void createIfNotExistsFileOptionsWithLeaseIdError() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setProposedLeaseId(CoreUtils.randomUuid().toString());

        // lease duration must also be set, or else exception is thrown
        assertThrows(DataLakeStorageException.class, () ->
            dataLakeFileSystemClient.createFileIfNotExistsWithResponse(generatePathName(), options, null, null));
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
    @Test
    public void createIfNotExistsFileOptionsWithLeaseDuration() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setLeaseDuration(15)
            .setProposedLeaseId(CoreUtils.randomUuid().toString());
        String fileName = generatePathName();

        assertEquals(201, dataLakeFileSystemClient.createFileIfNotExistsWithResponse(fileName, options, null, null)
            .getStatusCode());

        PathProperties fileProps = dataLakeFileSystemClient.getFileClient(fileName).getProperties();
        // assert whether lease has been acquired
        assertEquals(LeaseStatusType.LOCKED, fileProps.getLeaseStatus());
        assertEquals(LeaseStateType.LEASED, fileProps.getLeaseState());
        assertEquals(LeaseDurationType.FIXED, fileProps.getLeaseDuration());

    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
    @ParameterizedTest
    @MethodSource("createIfNotExistsFileOptionsWithTimeExpiresOnAbsoluteAndNeverExpireSupplier")
    public void createIfNotExistsFileOptionsWithTimeExpiresOnAbsoluteAndNeverExpire(DataLakePathScheduleDeletionOptions deletionOptions) {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setScheduleDeletionOptions(deletionOptions);

        assertEquals(201, dataLakeFileSystemClient.createFileIfNotExistsWithResponse(generatePathName(), options, null, null)
            .getStatusCode());
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

        assertEquals(201, dataLakeFileSystemClient.createFileWithResponse(fileName, options, null, null).getStatusCode());

        PathProperties fileProps = dataLakeFileSystemClient.getFileClient(fileName).getProperties();
        compareDatesWithPrecision(fileProps.getExpiresOn(), fileProps.getCreationTime().plusDays(6));
    }

    @Test
    public void deleteFileMin() {
        String pathName = generatePathName();
        dataLakeFileSystemClient.createFile(pathName);

        assertEquals(200, dataLakeFileSystemClient.deleteFileWithResponse(pathName, null, null, null).getStatusCode());
    }

    @Test
    public void deleteFileFileDoesNotExistAnymore() {
        String pathName = generatePathName();
        DataLakeFileClient client = dataLakeFileSystemClient.createFile(pathName);
        dataLakeFileSystemClient.deleteFileWithResponse(pathName, null, null, null);

        DataLakeStorageException e = assertThrows(DataLakeStorageException.class,
            () -> client.getPropertiesWithResponse(null, null, null));

        assertEquals(404, e.getStatusCode());
        assertEquals(BlobErrorCode.BLOB_NOT_FOUND.toString(), e.getErrorCode());
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void deleteFileAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        String pathName = generatePathName();
        DataLakeFileClient client = dataLakeFileSystemClient.createFile(pathName);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(client, leaseID))
            .setIfMatch(setupPathMatchCondition(client, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertEquals(200, dataLakeFileSystemClient.deleteFileWithResponse(pathName, drc, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void deleteFileACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        String pathName = generatePathName();
        DataLakeFileClient client = dataLakeFileSystemClient.createFile(pathName);
        setupPathLeaseCondition(client, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(client, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class, () ->
            dataLakeFileSystemClient.deleteFileWithResponse(pathName, drc, null, null));
    }

    @Test
    public void deleteIfExistsFileMin() {
        String pathName = generatePathName();
        dataLakeFileSystemClient.createFile(pathName);

        assertTrue(dataLakeFileSystemClient.deleteFileIfExists(pathName));
    }

    @Test
    public void deleteIfExistsFileNullArgs() {
        String pathName = generatePathName();
        dataLakeFileSystemClient.createFile(pathName);

        assertEquals(200, dataLakeFileSystemClient.deleteFileIfExistsWithResponse(pathName, null, null, null).getStatusCode());
    }

    @Test
    public void deleteIfExistsFileThatDoesNotExists() {
        assertEquals(404, dataLakeFileSystemClient.deleteFileIfExistsWithResponse(generatePathName(), null, null, null)
            .getStatusCode());
    }

    @Test
    public void deleteIfExistsFileThatWasAlreadyDelete() {
        String pathName = generatePathName();
        dataLakeFileSystemClient.createFile(pathName);

        assertEquals(200, dataLakeFileSystemClient.deleteFileIfExistsWithResponse(pathName, null, null, null).getStatusCode());
        assertEquals(404, dataLakeFileSystemClient.deleteFileIfExistsWithResponse(pathName, null, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void deleteIfExistsFileAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        String pathName = generatePathName();
        DataLakeFileClient client = dataLakeFileSystemClient.createFile(pathName);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(client, leaseID))
            .setIfMatch(setupPathMatchCondition(client, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);
        DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setRequestConditions(drc);

        assertEquals(200, dataLakeFileSystemClient.deleteFileIfExistsWithResponse(pathName, options, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void deleteIfExistsFileACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
        String noneMatch, String leaseID) {
        String pathName = generatePathName();
        DataLakeFileClient client = dataLakeFileSystemClient.createFile(pathName);
        setupPathLeaseCondition(client, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(client, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);
        DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setRequestConditions(drc);

        assertThrows(DataLakeStorageException.class, () ->
            dataLakeFileSystemClient.deleteFileIfExistsWithResponse(pathName, options, null, null));
    }

    @Test
    public void createDirMin() {
        assertDoesNotThrow(() -> dataLakeFileSystemClient.createDirectory(generatePathName()));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void createDirOverwrite(boolean overwrite) {
        String pathName = generatePathName();
        dataLakeFileSystemClient.createDirectory(pathName);

        if (overwrite) {
            assertDoesNotThrow(() -> dataLakeFileSystemClient.createDirectory(pathName, true));
        } else {
            assertThrows(DataLakeStorageException.class,
                () -> dataLakeFileSystemClient.createDirectory(pathName, false));
        }
    }

    @Test
    public void createDirDefaults() {
        Response<?> createResponse = dataLakeFileSystemClient.createDirectoryWithResponse(generatePathName(), null,
            null, null, null, null, null, null);

        assertEquals(201, createResponse.getStatusCode());
        validateBasicHeaders(createResponse.getHeaders());
    }

    @Test
    public void createDirError() {
        assertThrows(DataLakeStorageException.class, () -> dataLakeFileSystemClient.createDirectoryWithResponse(
            generatePathName(), null, null, null, null, new DataLakeRequestConditions().setIfMatch("garbage"), null,
            Context.NONE));
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

        Response<PathProperties> response = dataLakeFileSystemClient.createDirectoryWithResponse(generatePathName(),
            null, null, headers, null, null, null, null).getValue()
            .getPropertiesWithResponse(null, null, null);

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;

        validatePathProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, null, contentType);
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

        PathProperties response = dataLakeFileSystemClient.createDirectoryWithResponse(generatePathName(), null, null,
            null, metadata, null, null, null).getValue()
            .getProperties();

        // Directory adds a directory metadata value
        for (String k : metadata.keySet()) {
            assertTrue(response.getMetadata().containsKey(k));
            assertEquals(metadata.get(k), response.getMetadata().get(k));
        }
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void createDirAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        String pathName = generatePathName();
        DataLakeDirectoryClient client = dataLakeFileSystemClient.getDirectoryClient(pathName);
        client.create();
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(client, leaseID))
            .setIfMatch(setupPathMatchCondition(client, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertEquals(201, dataLakeFileSystemClient.createDirectoryWithResponse(pathName, null, null, null, null, drc, null, null)
            .getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void createDirACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        String pathName = generatePathName();
        DataLakeDirectoryClient client = dataLakeFileSystemClient.getDirectoryClient(pathName);
        client.create();
        setupPathLeaseCondition(client, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(client, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class, () ->
            dataLakeFileSystemClient.createDirectoryWithResponse(pathName, null, null, null, null, drc, null, Context.NONE));
    }

    @Test
    public void createDirPermissionsAndUmask() {
        assertEquals(201, dataLakeFileSystemClient.createDirectoryWithResponse(generatePathName(), "0777", "0057",
            null, null, null, null, Context.NONE).getStatusCode());
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
    @Test
    public void createDirOptionsWithACL() {
        List<PathAccessControlEntry> pathAccessControlEntries = PathAccessControlEntry.parseList("user::rwx,group::r--,other::---,mask::rwx");
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setAccessControlList(pathAccessControlEntries);
        DataLakeDirectoryClient client = dataLakeFileSystemClient.createDirectoryWithResponse(generatePathName(), options, null, null)
            .getValue();

        List<PathAccessControlEntry> acl = client.getAccessControl().getAccessControlList();
        assertEquals(pathAccessControlEntries.get(0), acl.get(0)); // testing if owner is set the same
        assertEquals(pathAccessControlEntries.get(1), acl.get(1)); // testing if group is set the same
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
    @Test
    public void createDirOptionsWithOwnerAndGroup() {
        String ownerName = testResourceNamer.randomUuid();
        String groupName = testResourceNamer.randomUuid();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setOwner(ownerName).setGroup(groupName);
        DataLakeDirectoryClient result = dataLakeFileSystemClient.createDirectoryWithResponse(generatePathName(), options, null, null)
            .getValue();

        assertEquals(ownerName, result.getAccessControl().getOwner());
        assertEquals(groupName, result.getAccessControl().getGroup());
    }

    @Test
    public void createDirOptionsWithNullOwnerAndGroup() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setOwner(null).setGroup(null);
        DataLakeDirectoryClient result = dataLakeFileSystemClient.createDirectoryWithResponse(generatePathName(), options, null, null)
            .getValue();

        assertEquals("$superuser", result.getAccessControl().getOwner());
        assertEquals("$superuser", result.getAccessControl().getGroup());
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
        DataLakeDirectoryClient result = dataLakeFileSystemClient.createDirectoryWithResponse(generatePathName(), options, null, null)
            .getValue();

        validatePathProperties(result.getPropertiesWithResponse(null, null, null), cacheControl, contentDisposition,
            contentEncoding, contentLanguage, contentMD5, contentType);
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

        Response<DataLakeDirectoryClient> response = dataLakeFileSystemClient.createDirectoryWithResponse(generatePathName(), options, null, null);
        assertEquals(201, response.getStatusCode());

        PathProperties properties = response.getValue().getProperties();
        // Directory adds a directory metadata value
        for (String k : metadata.keySet()) {
            assertTrue(properties.getMetadata().containsKey(k));
            assertEquals(metadata.get(k), properties.getMetadata().get(k));
        }
    }

    @Test
    public void createDirOptionsWithPermissionsAndUmask() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setPermissions("0777")
            .setUmask("0057");
        DataLakeDirectoryClient result = dataLakeFileSystemClient.createDirectoryWithResponse(generatePathName(), options, null, null)
            .getValue();

        PathAccessControl acl = result.getAccessControlWithResponse(true, null, null, null).getValue();

        assertEquals(PathPermissions.parseSymbolic("rwx-w----").toString(), acl.getPermissions().toString());
    }

    @Test
    public void createDirOptionsWithLeaseId() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setProposedLeaseId(CoreUtils.randomUuid().toString())
            .setLeaseDuration(15);

        // lease id not supported for directories
        assertThrows(IllegalArgumentException.class, () ->
            dataLakeFileSystemClient.createDirectoryWithResponse(generatePathName(), options, null, null));
    }

    @Test
    public void createDirOptionsWithLeaseDuration() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setLeaseDuration(15)
            .setProposedLeaseId(CoreUtils.randomUuid().toString());

        // lease duration not supported for directories
        assertThrows(IllegalArgumentException.class, () ->
            dataLakeFileSystemClient.createDirectoryWithResponse(generatePathName(), options, null, null));
    }

    @Test
    public void createDirOptionsWithTimeExpiresOn() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setProposedLeaseId(CoreUtils.randomUuid().toString())
            .setScheduleDeletionOptions(new DataLakePathScheduleDeletionOptions(OffsetDateTime.now().plusDays(1)));

        // expires on not supported for directories
        assertThrows(IllegalArgumentException.class, () ->
            dataLakeFileSystemClient.createDirectoryWithResponse(generatePathName(), options, null, null));
    }

    @Test
    public void createDirOptionsWithTimeToExpire() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setScheduleDeletionOptions(new DataLakePathScheduleDeletionOptions(Duration.ofDays(6)));

        // time to expire not supported for directories
        assertThrows(IllegalArgumentException.class, () ->
            dataLakeFileSystemClient.createDirectoryWithResponse(generatePathName(), options, null, null));
    }

    @Test
    public void createIfNotExistsDirMin() {
        assertTrue(dataLakeFileSystemClient.createDirectoryIfNotExists(generatePathName()).exists());
    }

    @Test
    public void createIfNotExistsDirDefaults() {
        Response<?> createResponse = dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(),
            null, null, null);

        assertEquals(201, createResponse.getStatusCode());
        validateBasicHeaders(createResponse.getHeaders());
    }

    @Test
    public void createIfNotExistsDirThatAlreadyExists() {
        String dirName = generatePathName();

        assertEquals(201, dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(dirName,
            new DataLakePathCreateOptions(), null, Context.NONE).getStatusCode());
        assertEquals(409, dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(dirName,
            new DataLakePathCreateOptions(), null, Context.NONE).getStatusCode());
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

        Response<PathProperties> response = dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(),
            new DataLakePathCreateOptions().setPathHttpHeaders(headers), null, null).getValue()
            .getPropertiesWithResponse(null, null, null);

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;

        validatePathProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, null, contentType);
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

        PathProperties response = dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(),
            new DataLakePathCreateOptions().setMetadata(metadata), null, null).getValue()
            .getProperties();

        // Directory adds a directory metadata value
        for (String k : metadata.keySet()) {
            assertTrue(response.getMetadata().containsKey(k));
            assertEquals(metadata.get(k), response.getMetadata().get(k));
        }
    }

    @Test
    public void createIfNotExistsDirPermissionsAndUmask() {
        assertEquals(201, dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(),
            new DataLakePathCreateOptions().setPermissions("0777").setUmask("0057"),
            null, Context.NONE).getStatusCode());
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
    @Test
    public void createIfNotExistsDirOptionsWithACL() {
        List<PathAccessControlEntry> pathAccessControlEntries = PathAccessControlEntry.parseList("user::rwx,group::r--,other::---,mask::rwx");
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setAccessControlList(pathAccessControlEntries);
        DataLakeDirectoryClient client = dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options, null, null)
            .getValue();

        List<PathAccessControlEntry> acl = client.getAccessControl().getAccessControlList();
        assertEquals(pathAccessControlEntries.get(0), acl.get(0)); // testing if owner is set the same
        assertEquals(pathAccessControlEntries.get(1), acl.get(1)); // testing if group is set the same
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20210608ServiceVersion")
    @Test
    public void createIfNotExistsDirOptionsWithOwnerAndGroup() {
        String ownerName = testResourceNamer.randomUuid();
        String groupName = testResourceNamer.randomUuid();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setOwner(ownerName)
            .setGroup(groupName);
        DataLakeDirectoryClient result = dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options, null, null)
            .getValue();

        assertEquals(ownerName, result.getAccessControl().getOwner());
        assertEquals(groupName, result.getAccessControl().getGroup());
    }

    @Test
    public void createIfNotExistsDirOptionsWithNullOwnerAndGroup() {
        DataLakeDirectoryClient result = dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(), null, null, null)
            .getValue();

        assertEquals("$superuser", result.getAccessControl().getOwner());
        assertEquals("$superuser", result.getAccessControl().getGroup());
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
        DataLakeDirectoryClient result = dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options, null, null)
            .getValue();


        validatePathProperties(result.getPropertiesWithResponse(null, null, null), cacheControl, contentDisposition,
            contentEncoding, contentLanguage, contentMD5, contentType);
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

        Response<DataLakeDirectoryClient> response = dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options, null, null);
        assertEquals(201, response.getStatusCode());

        PathProperties properties = response.getValue().getProperties();
        // Directory adds a directory metadata value
        for (String k : metadata.keySet()) {
            assertTrue(properties.getMetadata().containsKey(k));
            assertEquals(metadata.get(k), properties.getMetadata().get(k));
        }
    }

    @Test
    public void createIfNotExistsDirOptionsWithPermissionsAndUmask() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setPermissions("0777")
            .setUmask("0057");
        DataLakeDirectoryClient result = dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options, null, null)
            .getValue();

        PathAccessControl acl = result.getAccessControlWithResponse(true, null, null, null).getValue();

        assertEquals(PathPermissions.parseSymbolic("rwx-w----").toString(), acl.getPermissions().toString());
    }

    @Test
    public void createIfNotExistsDirOptionsWithLeaseId() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setProposedLeaseId(CoreUtils.randomUuid().toString())
            .setLeaseDuration(15);

        // assert lease id not supported for directory
        assertThrows(IllegalArgumentException.class, () ->
            dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options, null, null));
    }

    @Test
    public void createIfNotExistsDirOptionsWithLeaseIdError() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setProposedLeaseId(CoreUtils.randomUuid().toString());

        // assert lease duration not supported for directory
        assertThrows(IllegalArgumentException.class, () ->
            dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options, null, null));
    }

    @Test
    public void createIfNotExistsDirOptionsWithLeaseDuration() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setLeaseDuration(15)
            .setProposedLeaseId(CoreUtils.randomUuid().toString());

        // assert expires on not supported for directory
        assertThrows(IllegalArgumentException.class, () ->
            dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options, null, null));
    }

    @Test
    public void createIfNotExistsDirOptionsWithTimeExpiresOnAbsolute() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setScheduleDeletionOptions(new DataLakePathScheduleDeletionOptions(OffsetDateTime.now().plusDays(1)));

        // assert expires on not supported for directory
        assertThrows(IllegalArgumentException.class, () ->
            dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options, null, null));
    }

    @Test
    public void createIfNotExistsDirOptionsWithTimeToExpireRelativeToNow() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setScheduleDeletionOptions(new DataLakePathScheduleDeletionOptions(Duration.ofDays(6)));

        // assert time to expire not supported for directory
        assertThrows(IllegalArgumentException.class, () ->
            dataLakeFileSystemClient.createDirectoryIfNotExistsWithResponse(generatePathName(), options, null, null));
    }

    @Test
    public void deleteDirMin() {
        String pathName = generatePathName();
        dataLakeFileSystemClient.createDirectory(pathName);

        assertEquals(200, dataLakeFileSystemClient.deleteDirectoryWithResponse(pathName, false, null, null, null).getStatusCode());
    }

    @Test
    public void deleteDirRecursive() {
        String pathName = generatePathName();
        dataLakeFileSystemClient.createDirectory(pathName);

        assertEquals(200, dataLakeFileSystemClient.deleteDirectoryWithResponse(pathName, true, null, null, null).getStatusCode());
    }

    @Test
    public void deleteDirDirDoesNotExistAnymore() {
        String pathName = generatePathName();
        DataLakeDirectoryClient client = dataLakeFileSystemClient.createDirectory(pathName);
        dataLakeFileSystemClient.deleteDirectoryWithResponse(pathName, false, null, null, null);

        DataLakeStorageException e = assertThrows(DataLakeStorageException.class,
            () -> client.getPropertiesWithResponse(null, null, null));

        assertEquals(404, e.getResponse().getStatusCode());
        assertEquals(BlobErrorCode.BLOB_NOT_FOUND.toString(), e.getErrorCode());
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void deleteDirAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        String pathName = generatePathName();
        DataLakeDirectoryClient client = dataLakeFileSystemClient.createDirectory(pathName);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(client, leaseID))
            .setIfMatch(setupPathMatchCondition(client, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertEquals(200, dataLakeFileSystemClient.deleteDirectoryWithResponse(pathName, false, drc, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void deleteDirACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        String pathName = generatePathName();
        DataLakeDirectoryClient client = dataLakeFileSystemClient.createDirectory(pathName);
        setupPathLeaseCondition(client, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(client, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class,
            () -> dataLakeFileSystemClient.deleteDirectoryWithResponse(pathName, false, drc, null, null));
    }

    @Test
    public void deleteIfExistsDirMin() {
        String pathName = generatePathName();
        dataLakeFileSystemClient.createDirectory(pathName);

        assertTrue(dataLakeFileSystemClient.deleteDirectoryIfExists(pathName));
    }

    @Test
    public void deleteIfExistsDirNullArgs() {
        String pathName = generatePathName();
        dataLakeFileSystemClient.createDirectory(pathName);

        assertEquals(200, dataLakeFileSystemClient.deleteDirectoryIfExistsWithResponse(pathName, null, null, null).getStatusCode());
    }

    @Test
    public void deleteIfExistsDirRecursive() {
        String pathName = generatePathName();
        dataLakeFileSystemClient.createDirectory(pathName);

        assertEquals(200, dataLakeFileSystemClient.deleteDirectoryIfExistsWithResponse(pathName,
            new DataLakePathDeleteOptions().setIsRecursive(true), null, null).getStatusCode());
    }

    @Test
    public void deleteIfExistsDirThatDoesNotExist() {
        String pathName = generatePathName();

        assertEquals(404, dataLakeFileSystemClient.deleteDirectoryIfExistsWithResponse(pathName, null, null, null)
            .getStatusCode());
        assertFalse(dataLakeFileSystemClient.getDirectoryClient(pathName).exists());
    }

    @Test
    public void deleteIfExistsDirThatWasAlreadyDeleted() {
        String pathName = generatePathName();
        dataLakeFileSystemClient.createDirectory(pathName);

        assertEquals(200, dataLakeFileSystemClient.deleteDirectoryIfExistsWithResponse(pathName, null, null, null)
            .getStatusCode());
        assertEquals(404, dataLakeFileSystemClient.deleteDirectoryIfExistsWithResponse(pathName, null, null, null)
            .getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void deleteIfExistsDirAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        String pathName = generatePathName();
        DataLakeDirectoryClient client = dataLakeFileSystemClient.createDirectory(pathName);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(client, leaseID))
            .setIfMatch(setupPathMatchCondition(client, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);
        DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setRequestConditions(drc).setIsRecursive(false);

        assertEquals(200, dataLakeFileSystemClient.deleteDirectoryIfExistsWithResponse(pathName, options, null, null)
            .getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void deleteIfExistsDirACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
        String noneMatch, String leaseID) {
        String pathName = generatePathName();
        DataLakeDirectoryClient client = dataLakeFileSystemClient.createDirectory(pathName);
        setupPathLeaseCondition(client, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(client, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);
        DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setRequestConditions(drc).setIsRecursive(false);

        assertThrows(DataLakeStorageException.class,
            () -> dataLakeFileSystemClient.deleteDirectoryIfExistsWithResponse(pathName, options, null, null));
    }

    @Test
    public void listPaths() {
        String dirName = generatePathName();
        dataLakeFileSystemClient.getDirectoryClient(dirName).create();

        String fileName = generatePathName();
        dataLakeFileSystemClient.getFileClient(fileName).create();

        Iterator<PathItem> response = dataLakeFileSystemClient.listPaths().iterator();

        PathItem dirPath = response.next();
        assertEquals(dirName, dirPath.getName());
        assertNotNull(dirPath.getETag());
        assertNotNull(dirPath.getGroup());
        assertNotNull(dirPath.getLastModified());
        assertNotNull(dirPath.getOwner());
        assertNotNull(dirPath.getPermissions());
//        assertNotNull(dirPath.getContentLength()); // known issue with service
        assertTrue(dirPath.isDirectory());

        assertTrue(response.hasNext());
        PathItem filePath = response.next();
        assertEquals(fileName, filePath.getName());
        assertNotNull(filePath.getETag());
        assertNotNull(filePath.getGroup());
        assertNotNull(filePath.getLastModified());
        assertNotNull(filePath.getOwner());
        assertNotNull(filePath.getPermissions());
//        assertNotNull(filePath.getContentLength()); // known issue with service
        assertFalse(filePath.isDirectory());

        assertFalse(response.hasNext());
    }

    @DisabledIf("com.azure.storage.file.datalake.DataLakeTestBase#olderThan20200210ServiceVersion")
    @Test
    public void listPathsExpiryAndCreation() {
        String dirName = generatePathName();
        dataLakeFileSystemClient.getDirectoryClient(dirName).create();

        String fileName = generatePathName();
        DataLakeFileClient fileClient = dataLakeFileSystemClient.getFileClient(fileName);
        fileClient.create();
        fileClient.scheduleDeletion(new FileScheduleDeletionOptions(OffsetDateTime.now().plusDays(2)));

        Iterator<PathItem> response = dataLakeFileSystemClient.listPaths().iterator();

        PathItem dirPath = response.next();
        assertEquals(dirName, dirPath.getName());
        assertNotNull(dirPath.getCreationTime());
        assertNull(dirPath.getExpiryTime());

        PathItem filePath = response.next();
        assertEquals(fileName, filePath.getName());
        assertNotNull(filePath.getExpiryTime());
        assertNotNull(filePath.getCreationTime());
    }

    @Test
    public void listPathsRecursive() {
        dataLakeFileSystemClient.getDirectoryClient(generatePathName()).create();
        dataLakeFileSystemClient.getFileClient(generatePathName()).create();

        Iterator<PathItem> response = dataLakeFileSystemClient.listPaths(new ListPathsOptions().setRecursive(true), null)
            .iterator();

        response.next();
        assertTrue(response.hasNext());
        response.next();
        assertFalse(response.hasNext());
    }

    @Test
    public void listPathsReturnUpn() {
        dataLakeFileSystemClient.getDirectoryClient(generatePathName()).create();
        dataLakeFileSystemClient.getFileClient(generatePathName()).create();

        Iterator<PathItem> response = dataLakeFileSystemClient.listPaths(new ListPathsOptions().setUserPrincipalNameReturned(true), null)
            .iterator();

        response.next();
        assertTrue(response.hasNext());
        response.next();
        assertFalse(response.hasNext());
    }

    @Test
    public void listPathsMaxResults() {
        dataLakeFileSystemClient.getDirectoryClient(generatePathName()).create();
        dataLakeFileSystemClient.getFileClient(generatePathName()).create();

        Iterator<PathItem> response = dataLakeFileSystemClient.listPaths(new ListPathsOptions().setMaxResults(1), null)
            .iterator();

        response.next();
        assertTrue(response.hasNext());
        response.next();
        assertFalse(response.hasNext());
    }

    @Test
    public void listPathsMaxResultsByPage() {
        dataLakeFileSystemClient.getDirectoryClient(generatePathName()).create();
        dataLakeFileSystemClient.getFileClient(generatePathName()).create();

        for (PagedResponse<?> page : dataLakeFileSystemClient.listPaths(new ListPathsOptions(), null).iterableByPage(1)) {
            assertEquals(1, page.getValue().size());
        }
    }

    @Test
    public void listPathsEncryptionScope() {
        FileSystemEncryptionScopeOptions encryptionScope = new FileSystemEncryptionScopeOptions()
            .setDefaultEncryptionScope(ENCRYPTION_SCOPE_STRING)
            .setEncryptionScopeOverridePrevented(true);

        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());

        DataLakeFileSystemClient client = getFileSystemClientBuilder(dataLakeFileSystemClient.getFileSystemUrl())
            .credential(getDataLakeCredential())
            .fileSystemEncryptionScopeOptions(encryptionScope)
            .buildClient();
        client.create();

        String dirName = generatePathName();
        client.getDirectoryClient(dirName).create();

        String fileName = generatePathName();
        dataLakeFileSystemClient.getFileClient(fileName).create();

        Iterator<PathItem> response = dataLakeFileSystemClient.listPaths().iterator();

        PathItem dirPath = response.next();
        assertEquals(dirName, dirPath.getName());
        assertNotNull(dirPath.getETag());
        assertNotNull(dirPath.getGroup());
        assertNotNull(dirPath.getLastModified());
        assertNotNull(dirPath.getOwner());
        assertNotNull(dirPath.getPermissions());
        assertTrue(dirPath.isDirectory());
        assertEquals(ENCRYPTION_SCOPE_STRING, dirPath.getEncryptionScope());

        assertTrue(response.hasNext());
        PathItem filePath = response.next();
        assertEquals(fileName, filePath.getName());
        assertNotNull(filePath.getETag());
        assertNotNull(filePath.getGroup());
        assertNotNull(filePath.getLastModified());
        assertNotNull(filePath.getOwner());
        assertNotNull(filePath.getPermissions());
        assertEquals(ENCRYPTION_SCOPE_STRING, filePath.getEncryptionScope());
        assertFalse(filePath.isDirectory());

        assertFalse(response.hasNext());
    }

    @ParameterizedTest
    @ValueSource(strings = {"%E4%B8%AD%E6%96%87", "az%5B%5D", "hello%20world", "hello%26world",
        "%21%2A%27%28%29%3B%3A%40%26%3D%2B%24%2C%3F%23%5B%5D"})
    public void createUrlSpecialCharsEncoded(String name) {
        // Note you cannot use the / character in a path in datalake unless it is to specify an absolute path
        // This test checks that we handle path names with encoded special characters correctly.

        DataLakeFileClient fc1 = dataLakeFileSystemClient.getFileClient(name + "file1");
        DataLakeFileClient fc2 = dataLakeFileSystemClient.getFileClient(name + "file2");
        DataLakeDirectoryClient dc1 = dataLakeFileSystemClient.getDirectoryClient(name + "dir1");
        DataLakeDirectoryClient dc2 = dataLakeFileSystemClient.getDirectoryClient(name + "dir2");

        assertEquals(201, fc1.createWithResponse(null, null, null, null, null, null, null).getStatusCode());
        fc2.create();
        assertEquals(200, fc2.getPropertiesWithResponse(null, null, null).getStatusCode());
        assertEquals(202, fc2.appendWithResponse(DATA.getDefaultBinaryData(), 0, null, null, null, null).getStatusCode());
        assertEquals(201, dc1.createWithResponse(null, null, null, null, null, null, null).getStatusCode());
        dc2.create();
        assertEquals(200, dc2.getPropertiesWithResponse(null, null, null).getStatusCode());

        Iterator<PathItem> paths = dataLakeFileSystemClient.listPaths().iterator();

        assertEquals(Utility.urlDecode(name) + "dir1", paths.next().getName());
        assertEquals(Utility.urlDecode(name) + "dir2", paths.next().getName());
        assertEquals(Utility.urlDecode(name) + "file1", paths.next().getName());
        assertEquals(Utility.urlDecode(name) + "file2", paths.next().getName());
    }

    @ParameterizedTest
    @MethodSource("publicAccessSupplier")
    public void setAccessPolicy(PublicAccessType access) {
        Response<?> response = dataLakeFileSystemClient.setAccessPolicyWithResponse(access, null, null, null, null);

        validateBasicHeaders(response.getHeaders());
        assertEquals(access, dataLakeFileSystemClient.getProperties().getDataLakePublicAccess());
    }

    @Test
    public void setAccessPolicyMinAccess() {
        dataLakeFileSystemClient.setAccessPolicy(PublicAccessType.CONTAINER, null);

        assertEquals(PublicAccessType.CONTAINER, dataLakeFileSystemClient.getProperties().getDataLakePublicAccess());
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

        dataLakeFileSystemClient.setAccessPolicy(null, Collections.singletonList(identifier));

        assertEquals(identifier.getId(), dataLakeFileSystemClient.getAccessPolicy().getIdentifiers().get(0).getId());
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

        Response<?> response = dataLakeFileSystemClient.setAccessPolicyWithResponse(null,
            Arrays.asList(identifier, identifier2), null, null, null);
        List<DataLakeSignedIdentifier> receivedIdentifiers = dataLakeFileSystemClient.getAccessPolicyWithResponse(null, null, null)
            .getValue().getIdentifiers();


        assertEquals(200, response.getStatusCode());
        validateBasicHeaders(response.getHeaders());

        assertEquals(identifier.getAccessPolicy().getExpiresOn().truncatedTo(ChronoUnit.SECONDS),
            receivedIdentifiers.get(0).getAccessPolicy().getExpiresOn());
        assertEquals(identifier.getAccessPolicy().getStartsOn().truncatedTo(ChronoUnit.SECONDS),
            receivedIdentifiers.get(0).getAccessPolicy().getStartsOn());
        assertEquals(identifier.getAccessPolicy().getPermissions(), receivedIdentifiers.get(0).getAccessPolicy().getPermissions());
        assertEquals(identifier2.getAccessPolicy().getExpiresOn().truncatedTo(ChronoUnit.SECONDS),
            receivedIdentifiers.get(1).getAccessPolicy().getExpiresOn());
        assertEquals(identifier2.getAccessPolicy().getStartsOn().truncatedTo(ChronoUnit.SECONDS),
            receivedIdentifiers.get(1).getAccessPolicy().getStartsOn());
        assertEquals(identifier2.getAccessPolicy().getPermissions(), receivedIdentifiers.get(1).getAccessPolicy().getPermissions());
    }

    @ParameterizedTest
    @MethodSource("modifiedAndLeaseIdSupplier")
    public void setAccessPolicyAC(OffsetDateTime modified, OffsetDateTime unmodified, String leaseID) {
        DataLakeRequestConditions cac = new DataLakeRequestConditions()
            .setLeaseId(setupFileSystemLeaseCondition(dataLakeFileSystemClient, leaseID))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertEquals(200, dataLakeFileSystemClient.setAccessPolicyWithResponse(null, null, cac, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedAndLeaseIdSupplier")
    public void setAccessPolicyACFail(OffsetDateTime modified, OffsetDateTime unmodified, String leaseID) {
        DataLakeRequestConditions cac = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class,
            () -> dataLakeFileSystemClient.setAccessPolicyWithResponse(null, null, cac, null, null));
    }

    @ParameterizedTest
    @MethodSource("invalidMatchSupplier")
    public void setAccessPolicyACIllegal(String match, String noneMatch) {
        DataLakeRequestConditions mac = new DataLakeRequestConditions().setIfMatch(match).setIfNoneMatch(noneMatch);

        assertThrows(UnsupportedOperationException.class,
            () -> dataLakeFileSystemClient.setAccessPolicyWithResponse(null, null, mac, null, null));
    }

    @Test
    public void setAccessPolicyError() {
        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());

        assertThrows(DataLakeStorageException.class, () -> dataLakeFileSystemClient.setAccessPolicy(null, null));
    }

    @Test
    public void getAccessPolicy() {
        DataLakeSignedIdentifier identifier = new DataLakeSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new DataLakeAccessPolicy()
                .setStartsOn(testResourceNamer.now())
                .setExpiresOn(testResourceNamer.now().plusDays(1))
                .setPermissions("r"));
        dataLakeFileSystemClient.setAccessPolicy(PublicAccessType.BLOB, Collections.singletonList(identifier));
        Response<FileSystemAccessPolicies> response = dataLakeFileSystemClient.getAccessPolicyWithResponse(null, null, null);

        assertEquals(200, response.getStatusCode());
        assertEquals(PublicAccessType.BLOB, response.getValue().getDataLakeAccessType());
        validateBasicHeaders(response.getHeaders());
        assertEquals(identifier.getAccessPolicy().getExpiresOn().truncatedTo(ChronoUnit.SECONDS),
            response.getValue().getIdentifiers().get(0).getAccessPolicy().getExpiresOn());
        assertEquals(identifier.getAccessPolicy().getStartsOn().truncatedTo(ChronoUnit.SECONDS),
            response.getValue().getIdentifiers().get(0).getAccessPolicy().getStartsOn());
        assertEquals(identifier.getAccessPolicy().getPermissions(),
            response.getValue().getIdentifiers().get(0).getAccessPolicy().getPermissions());
    }

    @Test
    public void getAccessPolicyLease() {
        String leaseID = setupFileSystemLeaseCondition(dataLakeFileSystemClient, RECEIVED_LEASE_ID);

        assertEquals(200, dataLakeFileSystemClient.getAccessPolicyWithResponse(leaseID, null, null).getStatusCode());
    }

    @Test
    public void getAccessPolicyLeaseFail() {
        assertThrows(DataLakeStorageException.class, () ->
            dataLakeFileSystemClient.getAccessPolicyWithResponse(GARBAGE_LEASE_ID, null, null));
    }

    @Test
    public void getAccessPolicyError() {
        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());

        assertThrows(DataLakeStorageException.class, dataLakeFileSystemClient::getAccessPolicy);
    }

    @Test
    public void builderBearerTokenValidation() {
        // Technically no additional checks need to be added to datalake builder since the corresponding blob builder fails
        String endpoint = BlobUrlParts.parse(dataLakeFileSystemClient.getFileSystemUrl()).setScheme("http").toUrl()
            .toString();

        assertThrows(IllegalArgumentException.class, () -> new DataLakeFileSystemClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildClient());
    }

    @Test
    public void listPathsOAuth() {
        DataLakeFileSystemClient fsClient = getOAuthServiceClient()
            .getFileSystemClient(dataLakeFileSystemClient.getFileSystemName());
        fsClient.createFile(generatePathName());

        assertTrue(fsClient.listPaths().iterator().hasNext());
    }

    @Test
    public void setACLRootDirectory() {
        DataLakeDirectoryClient dc = dataLakeFileSystemClient.getRootDirectoryClient();
        List<PathAccessControlEntry> pathAccessControlEntries = PathAccessControlEntry.parseList("user::rwx,group::r--,other::---,mask::rwx");

        PathInfo resp = dc.setAccessControlList(pathAccessControlEntries, null, null);

        assertNotNull(resp.getETag());
        assertNotNull(resp.getLastModified());
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    @Test
    public void perCallPolicy() {
        DataLakeFileSystemClient dataLakeFileSystemClient = getFileSystemClientBuilder(getFileSystemUrl())
            .addPolicy(getPerCallVersionPolicy()).credential(getDataLakeCredential()).buildClient();

        // blob endpoint
        assertEquals("2019-02-02", dataLakeFileSystemClient.getPropertiesWithResponse(null, null, null)
            .getHeaders().getValue(X_MS_VERSION));

        // dfs endpoint
        assertEquals("2019-02-02", dataLakeFileSystemClient.getAccessPolicyWithResponse(null, null, null)
            .getHeaders().getValue(X_MS_VERSION));
    }

    @Test
    public void defaultAudience() {
        DataLakeFileSystemClient aadFsClient =
            getFileSystemClientBuilderWithTokenCredential(ENVIRONMENT.getDataLakeAccount().getDataLakeEndpoint())
                .fileSystemName(dataLakeFileSystemClient.getFileSystemName())
                .audience(null) // should default to "https://storage.azure.com/"
                .buildClient();

        assertTrue(aadFsClient.exists());
    }

    @Test
    public void storageAccountAudience() {
        DataLakeFileSystemClient aadFsClient =
            getFileSystemClientBuilderWithTokenCredential(ENVIRONMENT.getDataLakeAccount().getDataLakeEndpoint())
                .fileSystemName(dataLakeFileSystemClient.getFileSystemName())
                .audience(DataLakeAudience.createDataLakeServiceAccountAudience(dataLakeFileSystemClient.getAccountName()))
                .buildClient();

        assertTrue(aadFsClient.exists());
    }

    @Test
    public void audienceError() {
        DataLakeFileSystemClient aadFsClient =
            getFileSystemClientBuilderWithTokenCredential(ENVIRONMENT.getDataLakeAccount().getDataLakeEndpoint())
                .audience(DataLakeAudience.createDataLakeServiceAccountAudience("badAudience"))
                .buildClient();

        DataLakeStorageException e = assertThrows(DataLakeStorageException.class, aadFsClient::exists);
        assertEquals(BlobErrorCode.INVALID_AUTHENTICATION_INFO.toString(), e.getErrorCode());
    }

    @Test
    public void audienceFromString() {
        String url = String.format("https://%s.blob.core.windows.net/", dataLakeFileSystemClient.getAccountName());
        DataLakeAudience audience = DataLakeAudience.fromString(url);

        DataLakeFileSystemClient aadFsClient =
            getFileSystemClientBuilderWithTokenCredential(ENVIRONMENT.getDataLakeAccount().getDataLakeEndpoint())
                .fileSystemName(dataLakeFileSystemClient.getFileSystemName())
                .audience(audience)
                .buildClient();

        assertTrue(aadFsClient.exists());
    }

//    @Test
//    public void rename() {
//        DataLakeFileSystemClient renamedContainer = dataLakeFileSystemClient.rename(generateFileSystemName());
//
//        assertEquals(200, renamedContainer.getPropertiesWithResponse(null, null, null).getStatusCode())
//    }
//
//    @Test
//    public void renameSas() {
//        AccountSasService service = new AccountSasService().setBlobAccess(true);
//        AccountSasResourceType resourceType = new AccountSasResourceType()
//            .setContainer(true)
//            .setService(true)
//            .setObject(true);
//        AccountSasPermission permissions = new AccountSasPermission()
//            .setReadPermission(true)
//            .setCreatePermission(true)
//            .setWritePermission(true)
//            .setDeletePermission(true);
//
//        String sas = primaryDataLakeServiceClient.generateAccountSas(new AccountSasSignatureValues(
//            testResourceNamer.now().plusDays(1), permissions, service, resourceType));
//        DataLakeFileSystemClient sasClient = getFileSystemClient(sas, dataLakeFileSystemClient.getFileSystemUrl());
//
//        DataLakeFileSystemClient renamedContainer = sasClient.rename(generateFileSystemName());
//
//        assertEquals(200, renamedContainer.getPropertiesWithResponse(null, null, null).getStatusCode());
//    }
//
//    @ParameterizedTest
//    @MethodSource("renameACSupplier")
//    public void renameAC(String leaseID) {
//        DataLakeRequestConditions cac = new DataLakeRequestConditions().setLeaseId(setupFileSystemLeaseCondition(dataLakeFileSystemClient, leaseID));
//
//        assertEquals(200, dataLakeFileSystemClient
//            .renameWithResponse(new FileSystemRenameOptions(generateFileSystemName()).setRequestConditions(cac), null,
//                null)
//            .getStatusCode());
//    }
//
//    private static Stream<String> renameACSupplier() {
//        return Stream.of(null, RECEIVED_LEASE_ID);
//    }
//
//    @Test
//    public void renameACFail() {
//        DataLakeRequestConditions cac = new DataLakeRequestConditions().setLeaseId(GARBAGE_LEASE_ID);
//
//        assertThrows(DataLakeStorageException.class, () -> dataLakeFileSystemClient.renameWithResponse(
//            new FileSystemRenameOptions(generateFileSystemName()).setRequestConditions(cac), null, null));
//    }
//
//    @ParameterizedTest
//    @MethodSource("renameACIllegalSupplier")
//    public void renameACIllegal(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch) {
//        DataLakeRequestConditions ac = new DataLakeRequestConditions().setIfMatch(match)
//            .setIfNoneMatch(noneMatch)
//            .setIfModifiedSince(modified)
//            .setIfUnmodifiedSince(unmodified);
//
//        assertThrows(UnsupportedOperationException.class, () -> dataLakeFileSystemClient.renameWithResponse(
//            new FileSystemRenameOptions(generateFileSystemName()).setRequestConditions(ac), null, null));
//    }
//
//    private static Stream<Arguments> renameACIllegalSupplier() {
//        return Stream.of(
//            Arguments.of(OLD_DATE, null, null, null),
//            Arguments.of(null, NEW_DATE, null, null),
//            Arguments.of(null, null, RECEIVED_ETAG, null),
//            Arguments.of(null, null, null, GARBAGE_ETAG)
//        );
//    }
//
//    @Test
//    public void renameError() {
//        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());
//
//        assertThrows(DataLakeStorageException.class, () -> dataLakeFileSystemClient.rename(generateFileSystemName()));
//    }
}
