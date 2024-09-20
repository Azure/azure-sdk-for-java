// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake;

import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.ProgressListener;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.ProgressReceiver;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import com.azure.storage.common.test.shared.extensions.RequiredServiceVersion;
import com.azure.storage.common.test.shared.policy.MockFailureResponsePolicy;
import com.azure.storage.common.test.shared.policy.MockRetryRangeResponsePolicy;
import com.azure.storage.common.test.shared.policy.TransientFailureInjectingHttpPipelinePolicy;
import com.azure.storage.file.datalake.models.AccessControlChangeResult;
import com.azure.storage.file.datalake.models.AccessTier;
import com.azure.storage.file.datalake.models.DataLakeAudience;
import com.azure.storage.file.datalake.models.DataLakeFileOpenInputStreamResult;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeStorageException;
import com.azure.storage.file.datalake.models.DownloadRetryOptions;
import com.azure.storage.file.datalake.models.FileExpirationOffset;
import com.azure.storage.file.datalake.models.FileQueryArrowField;
import com.azure.storage.file.datalake.models.FileQueryArrowFieldType;
import com.azure.storage.file.datalake.models.FileQueryArrowSerialization;
import com.azure.storage.file.datalake.models.FileQueryDelimitedSerialization;
import com.azure.storage.file.datalake.models.FileQueryError;
import com.azure.storage.file.datalake.models.FileQueryJsonSerialization;
import com.azure.storage.file.datalake.models.FileQueryParquetSerialization;
import com.azure.storage.file.datalake.models.FileQueryProgress;
import com.azure.storage.file.datalake.models.FileQuerySerialization;
import com.azure.storage.file.datalake.models.FileRange;
import com.azure.storage.file.datalake.models.FileReadResponse;
import com.azure.storage.file.datalake.models.LeaseAction;
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
import com.azure.storage.file.datalake.models.PathRemoveAccessControlEntry;
import com.azure.storage.file.datalake.models.RolePermissions;
import com.azure.storage.file.datalake.options.DataLakeFileAppendOptions;
import com.azure.storage.file.datalake.options.DataLakeFileInputStreamOptions;
import com.azure.storage.file.datalake.options.DataLakePathCreateOptions;
import com.azure.storage.file.datalake.options.DataLakePathDeleteOptions;
import com.azure.storage.file.datalake.options.DataLakePathScheduleDeletionOptions;
import com.azure.storage.file.datalake.options.FileParallelUploadOptions;
import com.azure.storage.file.datalake.options.FileQueryOptions;
import com.azure.storage.file.datalake.options.FileScheduleDeletionOptions;
import com.azure.storage.file.datalake.options.PathGetPropertiesOptions;
import com.azure.storage.file.datalake.options.ReadToFileOptions;
import com.azure.storage.file.datalake.sas.DataLakeServiceSasSignatureValues;
import com.azure.storage.file.datalake.sas.FileSystemSasPermission;
import com.azure.storage.file.datalake.specialized.DataLakeLeaseClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileApiTest extends DataLakeTestBase {
    private static final PathPermissions PERMISSIONS = new PathPermissions()
        .setOwner(new RolePermissions().setReadPermission(true).setWritePermission(true).setExecutePermission(true))
        .setGroup(new RolePermissions().setReadPermission(true).setExecutePermission(true))
        .setOther(new RolePermissions().setReadPermission(true));
    private static final List<PathAccessControlEntry> PATH_ACCESS_CONTROL_ENTRIES =
        PathAccessControlEntry.parseList("user::rwx,group::r--,other::---,mask::rwx");
    private static final String GROUP = null;
    private static final String OWNER = null;
    private static final HttpHeaderName X_MS_VERSION = HttpHeaderName.fromString("x-ms-version");
    private static final HttpHeaderName X_MS_REQUEST_ID = HttpHeaderName.fromString("x-ms-request-id");
    private static final HttpHeaderName X_MS_REQUEST_SERVER_ENCRYPTED = HttpHeaderName.fromString("x-ms-request-server-encrypted");

    private DataLakeFileClient fc;
    private final List<File> createdFiles = new ArrayList<>();

    @BeforeEach
    public void setup() {
        fc = dataLakeFileSystemClient.createFile(generatePathName());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @AfterEach
    public void cleanup() {
        createdFiles.forEach(File::delete);
    }

    @Test
    public void createMin() {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        assertDoesNotThrow(() -> fc.create());
    }

    @Test
    public void createDefaults() {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        Response<?> createResponse = fc.createWithResponse(null, null, null, null, null, null, null);


        assertEquals(201, createResponse.getStatusCode());
        validateBasicHeaders(createResponse.getHeaders());
    }

    @Test
    public void createError() {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());

        assertThrows(DataLakeStorageException.class, () -> fc.createWithResponse(null, null, null, null,
            new DataLakeRequestConditions().setIfMatch("garbage"), null, Context.NONE));
    }

    @Test
    public void createOverwrite() {
        fc = dataLakeFileSystemClient.createFile(generatePathName());

        // Try to create the resource again
        assertThrows(DataLakeStorageException.class, () -> fc.create(false));
    }

    @Test
    public void exists() {
        fc = dataLakeFileSystemClient.createFile(generatePathName());

        assertTrue(fc.exists());
    }

    @Test
    public void doesNotExist() {
        assertFalse(dataLakeFileSystemClient.getFileClient(generatePathName()).exists());
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null,null", "control,disposition,encoding,language,type"})
    public void createHeaders(String cacheControl, String contentDisposition, String contentEncoding,
        String contentLanguage, String contentType) {
        // Create does not set md5
        PathHttpHeaders headers = new PathHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType);
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        fc.createWithResponse(null, null, headers, null, null, null, null);
        Response<PathProperties> response = fc.getPropertiesWithResponse(null, null, null);

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;

        validatePathProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, null, contentType);
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz"}, nullValues = "null")
    public void createMetadata(String key1, String value1, String key2, String value2) {
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }

        fc.createWithResponse(null, null, null, metadata, null, null, Context.NONE);

        assertEquals(metadata, fc.getProperties().getMetadata());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2021-04-10")
    @Test
    public void createEncryptionContext() {
        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());
        dataLakeFileSystemClient.create();
        dataLakeFileSystemClient.getDirectoryClient(generatePathName()).create();
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());

        // testing encryption context with create()
        String encryptionContext = "encryptionContext";
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setEncryptionContext(encryptionContext);
        fc.createWithResponse(options, null, Context.NONE);

        assertEquals(encryptionContext, fc.getProperties().getEncryptionContext());

        // testing encryption context with read()
        assertEquals(encryptionContext, fc.readWithResponse(new ByteArrayOutputStream(), null, null, null, false, null,
            Context.NONE).getDeserializedHeaders().getEncryptionContext());

        // testing encryption context with listPaths()
        Iterator<PathItem> response = dataLakeFileSystemClient.listPaths(new ListPathsOptions().setRecursive(true), null).iterator();

        response.next();
        assertTrue(response.hasNext());
        PathItem filePath = response.next();
        assertEquals(encryptionContext, filePath.getEncryptionContext());
        assertFalse(response.hasNext());
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void createAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(fc, leaseID))
            .setIfMatch(setupPathMatchCondition(fc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertEquals(201, fc.createWithResponse(null, null, null, null, drc, null, null).getStatusCode());
    }

    private static Stream<Arguments> modifiedMatchAndLeaseIdSupplier() {
        return Stream.of(
            // modified | unmodified | match        | noneMatch   | leaseID
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
    public void createACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        setupPathLeaseCondition(fc, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(fc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class,
            () -> fc.createWithResponse(null, null, null, null, drc, null, Context.NONE));
    }

    private static Stream<Arguments> invalidModifiedMatchAndLeaseIdSupplier() {
        return Stream.of(
            // modified | unmodified | match        | noneMatch   | leaseID
            Arguments.of(NEW_DATE, null, null, null, null),
            Arguments.of(null, OLD_DATE, null, null, null),
            Arguments.of(null, null, GARBAGE_ETAG, null, null),
            Arguments.of(null, null, null, RECEIVED_ETAG, null),
            Arguments.of(null, null, null, null, GARBAGE_LEASE_ID)
        );
    }

    @Test
    public void createPermissionsAndUmask() {
        assertEquals(201, fc.createWithResponse("0777", "0057", null, null, null, null, Context.NONE).getStatusCode());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-12-06")
    @Test
    public void createOptionsWithACL() {
        List<PathAccessControlEntry> pathAccessControlEntries = PathAccessControlEntry.parseList("user::rwx,group::r--,other::---,mask::rwx");
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setAccessControlList(pathAccessControlEntries);
        fc.createWithResponse(options, null, null);

        List<PathAccessControlEntry> acl = fc.getAccessControl().getAccessControlList();
        assertEquals(pathAccessControlEntries.get(0), acl.get(0)); // testing if owner is set the same
        assertEquals(pathAccessControlEntries.get(1), acl.get(1)); // testing if group is set the same
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-12-06")
    @Test
    public void createOptionsWithOwnerAndGroup() {
        String ownerName = testResourceNamer.randomUuid();
        String groupName = testResourceNamer.randomUuid();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setOwner(ownerName).setGroup(groupName);
        fc.createWithResponse(options, null, null);

        assertEquals(ownerName, fc.getAccessControl().getOwner()); // testing if owner is set the same
        assertEquals(groupName, fc.getAccessControl().getGroup()); // testing if group is set the same
    }

    @Test
    public void createOptionsWithNullOwnerAndGroup() {
        fc.createWithResponse(null, null, null);

        assertEquals("$superuser", fc.getAccessControl().getOwner()); // testing if owner is set the same
        assertEquals("$superuser", fc.getAccessControl().getGroup()); // testing if group is set the same
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null,null,application/octet-stream", "control,disposition,encoding,language,null,type"},
               nullValues = "null")
    public void createOptionsWithPathHttpHeaders(String cacheControl, String contentDisposition, String contentEncoding,
        String contentLanguage, byte[] contentMD5, String contentType) {
        PathHttpHeaders putHeaders = new PathHttpHeaders()
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType);
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setPathHttpHeaders(putHeaders);

        Response<?> response = fc.createWithResponse(options, null, null);

        assertEquals(201, response.getStatusCode());
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz"}, nullValues = "null")
    public void createOptionsWithMetadata(String key1, String value1, String key2, String value2) {
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2);
        }
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setMetadata(metadata);

        assertEquals(201, fc.createWithResponse(options, null, null).getStatusCode());

        PathProperties properties = fc.getProperties();
        // Directory adds a directory metadata value
        for (String k : metadata.keySet()) {
            assertTrue(properties.getMetadata().containsKey(k));
            assertEquals(metadata.get(k), properties.getMetadata().get(k));
        }
    }

    @Test
    public void createOptionsWithPermissionsAndUmask() {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setPermissions("0777").setUmask("0057");
        fc.createWithResponse(options, null, null);

        PathAccessControl acl = fc.getAccessControlWithResponse(true, null, null, null).getValue();

        assertEquals(PathPermissions.parseSymbolic("rwx-w----").toString(), acl.getPermissions().toString());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-12-06")
    @Test
    public void createOptionsWithLeaseId() {
        String leaseId = CoreUtils.randomUuid().toString();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setProposedLeaseId(leaseId).setLeaseDuration(15);

        assertEquals(201, fc.createWithResponse(options, null, null).getStatusCode());
    }

    @Test
    public void createOptionsWithLeaseIdError() {
        String leaseId = CoreUtils.randomUuid().toString();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setProposedLeaseId(leaseId);

        // lease duration must also be set, or else exception is thrown
        assertThrows(DataLakeStorageException.class, () -> fc.createWithResponse(options, null, null));
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-12-06")
    @Test
    public void createOptionsWithLeaseDuration() {
        String leaseId = CoreUtils.randomUuid().toString();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setLeaseDuration(15).setProposedLeaseId(leaseId);

        assertEquals(201, fc.createWithResponse(options, null, null).getStatusCode());

        PathProperties fileProps = fc.getProperties();
        // assert whether lease has been acquired
        assertEquals(LeaseStatusType.LOCKED, fileProps.getLeaseStatus());
        assertEquals(LeaseStateType.LEASED, fileProps.getLeaseState());
        assertEquals(LeaseDurationType.FIXED, fileProps.getLeaseDuration());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-12-06")
    @ParameterizedTest
    @MethodSource("timeExpiresOnOptionsSupplier")
    public void createOptionsWithTimeExpiresOn(DataLakePathScheduleDeletionOptions deletionOptions) {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setScheduleDeletionOptions(deletionOptions);

        assertEquals(201, fc.createWithResponse(options, null, null).getStatusCode());
    }

    private static Stream<DataLakePathScheduleDeletionOptions> timeExpiresOnOptionsSupplier() {
        return Stream.of(new DataLakePathScheduleDeletionOptions(OffsetDateTime.now().plusDays(1)), null);
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-12-06")
    @Test
    public void createOptionsWithTimeToExpireRelativeToNow() {
        DataLakePathScheduleDeletionOptions deletionOptions = new DataLakePathScheduleDeletionOptions(Duration.ofDays(6));
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setScheduleDeletionOptions(deletionOptions);

        assertEquals(201, fc.createWithResponse(options, null, null).getStatusCode());

        PathProperties fileProps = fc.getProperties();

        compareDatesWithPrecision(fileProps.getExpiresOn(), fileProps.getCreationTime().plusDays(6));
    }

    @Test
    public void createIfNotExistsMin() {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        fc.createIfNotExists();

        assertTrue(fc.exists());
    }

    @Test
    public void createIfNotExistsDefaults() {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        Response<?> createResponse = fc.createIfNotExistsWithResponse(new DataLakePathCreateOptions(), null, null);

        assertEquals(201, createResponse.getStatusCode());
        validateBasicHeaders(createResponse.getHeaders());
    }

    @Test
    public void createIfNotExistsOverwrite() {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());

        assertEquals(201, fc.createIfNotExistsWithResponse(new DataLakePathCreateOptions(), null, null).getStatusCode());
        assertTrue(fc.exists());

        // Try to create the resource again
        assertEquals(409, fc.createIfNotExistsWithResponse(new DataLakePathCreateOptions(), null, null).getStatusCode());
    }

    @Test
    public void createIfNotExistsExists() {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        fc.createIfNotExists();

        assertTrue(fc.exists());
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null,null", "control,disposition,encoding,language,type"})
    public void createIfNotExistsHeaders(String cacheControl, String contentDisposition, String contentEncoding,
        String contentLanguage, String contentType) {
        // Create does not set md5
        PathHttpHeaders headers = new PathHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType);
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());

        fc.createIfNotExistsWithResponse(new DataLakePathCreateOptions().setPathHttpHeaders(headers), null, null);
        Response<PathProperties> response = fc.getPropertiesWithResponse(null, null, null);

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;

        validatePathProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, null, contentType);
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz"}, nullValues = "null")
    public void createIfNotExistsMetadata(String key1, String value1, String key2, String value2) {
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }

        DataLakeFileClient client = dataLakeFileSystemClient.getFileClient(generatePathName());
        client.createIfNotExistsWithResponse(new DataLakePathCreateOptions().setMetadata(metadata), null, Context.NONE);

        assertEquals(metadata, client.getProperties().getMetadata());
    }

    @Test
    public void createIfNotExistsPermissionsAndUmask() {
        DataLakeFileClient client = dataLakeFileSystemClient.getFileClient(generatePathName());

        assertEquals(201, client.createIfNotExistsWithResponse(new DataLakePathCreateOptions()
            .setPermissions("0777").setUmask("0057"), null, Context.NONE).getStatusCode());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2021-04-10")
    @Test
    public void createIfNotExistsEncryptionContext() {
        dataLakeFileSystemClient = primaryDataLakeServiceClient.createFileSystem(generateFileSystemName());
        DataLakeDirectoryClient dirClient = dataLakeFileSystemClient.createDirectory(generatePathName());
        fc = dirClient.getFileClient(generatePathName());

        String encryptionContext = "encryptionContext";
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setEncryptionContext(encryptionContext);
        fc.createIfNotExistsWithResponse(options, null, Context.NONE);

        assertEquals(encryptionContext, fc.getProperties().getEncryptionContext());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-12-06")
    @Test
    public void createIfNotExistsOptionsWithACL() {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        List<PathAccessControlEntry> pathAccessControlEntries = PathAccessControlEntry.parseList("user::rwx,group::r--,other::---,mask::rwx");
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setAccessControlList(pathAccessControlEntries);
        fc.createIfNotExistsWithResponse(options, null, null);

        List<PathAccessControlEntry> acl = fc.getAccessControl().getAccessControlList();
        assertEquals(pathAccessControlEntries.get(0), acl.get(0)); // testing if owner is set the same
        assertEquals(pathAccessControlEntries.get(1), acl.get(1)); // testing if group is set the same
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-12-06")
    @Test
    public void createIfNotExistsOptionsWithOwnerAndGroup() {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        String ownerName = testResourceNamer.randomUuid();
        String groupName = testResourceNamer.randomUuid();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setOwner(ownerName).setGroup(groupName);
        fc.createIfNotExistsWithResponse(options, null, null);

        assertEquals(ownerName, fc.getAccessControl().getOwner()); // testing if owner is set the same
        assertEquals(groupName, fc.getAccessControl().getGroup()); // testing if group is set the same
    }

    @Test
    public void createIfNotExistsOptionsWithNullOwnerAndGroup() {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setOwner(null).setGroup(null);
        fc.createIfNotExistsWithResponse(options, null, null);

        assertEquals("$superuser", fc.getAccessControl().getOwner()); // testing if owner is set the same
        assertEquals("$superuser", fc.getAccessControl().getGroup()); // testing if group is set the same
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null,null,application/octet-stream", "control,disposition,encoding,language,null,type"},
               nullValues = "null")
    public void createIfNotExistsOptionsWithPathHttpHeaders(String cacheControl, String contentDisposition,
        String contentEncoding, String contentLanguage, byte[] contentMD5, String contentType) {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        PathHttpHeaders putHeaders = new PathHttpHeaders()
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType);

        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setPathHttpHeaders(putHeaders);

        assertEquals(201, fc.createIfNotExistsWithResponse(options, null, null).getStatusCode());
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz"}, nullValues = "null")
    public void createIfNotExistsOptionsWithMetadata(String key1, String value1, String key2, String value2) {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2);
        }
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setMetadata(metadata);

        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        assertEquals(201, fc.createIfNotExistsWithResponse(options, null, null).getStatusCode());

        PathProperties properties = fc.getProperties();
        // Directory adds a directory metadata value
        for (String k : metadata.keySet()) {
            assertTrue(properties.getMetadata().containsKey(k));
            assertEquals(metadata.get(k), properties.getMetadata().get(k));
        }
    }

    @Test
    public void createIfNotExistsOptionsWithPermissionsAndUmask() {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setPermissions("0777").setUmask("0057");
        fc.createIfNotExistsWithResponse(options, null, null);

        PathAccessControl acl = fc.getAccessControlWithResponse(true, null, null, null).getValue();

        assertEquals(PathPermissions.parseSymbolic("rwx-w----").toString(), acl.getPermissions().toString());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-12-06")
    @Test
    public void createIfNotExistsOptionsWithLeaseId() {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        String leaseId = CoreUtils.randomUuid().toString();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setProposedLeaseId(leaseId).setLeaseDuration(15);

        assertEquals(201, fc.createIfNotExistsWithResponse(options, null, null).getStatusCode());
    }

    @Test
    public void createIfNotExistsOptionsWithLeaseIdError() {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        String leaseId = CoreUtils.randomUuid().toString();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setProposedLeaseId(leaseId);

        // lease duration must also be set, or else exception is thrown
        assertThrows(DataLakeStorageException.class, () -> fc.createIfNotExistsWithResponse(options, null, null));
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-12-06")
    @Test
    public void createIfNotExistsOptionsWithLeaseDuration() {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        String leaseId = CoreUtils.randomUuid().toString();
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setLeaseDuration(15).setProposedLeaseId(leaseId);

        assertEquals(201, fc.createIfNotExistsWithResponse(options, null, null).getStatusCode());

        PathProperties fileProps = fc.getProperties();
        // assert whether lease has been acquired
        assertEquals(LeaseStatusType.LOCKED, fileProps.getLeaseStatus());
        assertEquals(LeaseStateType.LEASED, fileProps.getLeaseState());
        assertEquals(LeaseDurationType.FIXED, fileProps.getLeaseDuration());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-12-06")
    @ParameterizedTest
    @MethodSource("timeExpiresOnOptionsSupplier")
    public void createIfNotExistsOptionsWithTimeExpiresOn(DataLakePathScheduleDeletionOptions deletionOptions) {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setScheduleDeletionOptions(deletionOptions);

        assertEquals(201, fc.createIfNotExistsWithResponse(options, null, null).getStatusCode());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-12-06")
    @Test
    public void createIfNotExistsOptionsWithTimeToExpireRelativeToNow() {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        DataLakePathScheduleDeletionOptions deletionOptions = new DataLakePathScheduleDeletionOptions(Duration.ofDays(6));
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setScheduleDeletionOptions(deletionOptions);

        assertEquals(201, fc.createIfNotExistsWithResponse(options, null, null).getStatusCode());

        PathProperties fileProps = fc.getProperties();
        compareDatesWithPrecision(fileProps.getExpiresOn(), fileProps.getCreationTime().plusDays(6));
    }

    @Test
    public void deleteMin() {
        assertEquals(200, fc.deleteWithResponse(null, null, null).getStatusCode());
    }

    @Test
    public void deleteFileDoesNotExistAnymore() {
        fc.deleteWithResponse(null, null, null);

        DataLakeStorageException e = assertThrows(DataLakeStorageException.class,
            () -> fc.getPropertiesWithResponse(null, null, null));

        assertEquals(404, e.getResponse().getStatusCode());
        assertEquals(BlobErrorCode.BLOB_NOT_FOUND.toString(), e.getErrorCode());
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void deleteAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(fc, leaseID))
            .setIfMatch(setupPathMatchCondition(fc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertEquals(200, fc.deleteWithResponse(drc, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void deleteACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        setupPathLeaseCondition(fc, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(fc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class, () -> fc.deleteWithResponse(drc, null, null));
    }

    @Test
    public void deleteIfExists() {
        assertTrue(fc.deleteIfExists());
    }

    @Test
    public void deleteIfExistsMin() {
        assertEquals(200, fc.deleteIfExistsWithResponse(null, null, null).getStatusCode());
    }

    @Test
    public void deleteIfExistsFileDoesNotExistAnymore() {
        assertEquals(200, fc.deleteIfExistsWithResponse(null, null, null).getStatusCode());
        assertThrows(DataLakeStorageException.class, () -> fc.getPropertiesWithResponse(null, null, null));
    }

    @Test
    public void deleteIfExistsFileThatDoesNotExist() {
        assertEquals(200, fc.deleteIfExistsWithResponse(null, null, null).getStatusCode());
        assertEquals(404, fc.deleteIfExistsWithResponse(null, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void deleteIfExistsAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(fc, leaseID))
            .setIfMatch(setupPathMatchCondition(fc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);
        DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setIsRecursive(false).setRequestConditions(drc);

        assertEquals(200, fc.deleteIfExistsWithResponse(options, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void deleteIfExistsACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        setupPathLeaseCondition(fc, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(fc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);
        DataLakePathDeleteOptions options = new DataLakePathDeleteOptions().setRequestConditions(drc);

        assertThrows(DataLakeStorageException.class, () -> fc.deleteIfExistsWithResponse(options, null, null));
    }

    @Test
    public void setPermissionsMin() {
        PathInfo resp = fc.setPermissions(PERMISSIONS, GROUP, OWNER);

        assertNotNull(resp.getETag());
        assertNotNull(resp.getLastModified());
    }

    @Test
    public void setPermissionsWithResponse() {
        assertEquals(200, fc.setPermissionsWithResponse(PERMISSIONS, GROUP, OWNER, null, null, Context.NONE).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void setPermissionsAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(fc, leaseID))
            .setIfMatch(setupPathMatchCondition(fc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertEquals(200, fc.setPermissionsWithResponse(PERMISSIONS, GROUP, OWNER, drc, null, Context.NONE).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void setPermissionsACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        setupPathLeaseCondition(fc, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(fc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class,
            () -> fc.setPermissionsWithResponse(PERMISSIONS, GROUP, OWNER, drc, null, Context.NONE));
    }

    @Test
    public void setPermissionsError() {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());

        assertThrows(DataLakeStorageException.class,
            () -> fc.setPermissionsWithResponse(PERMISSIONS, GROUP, OWNER, null, null, null));
    }

    @Test
    public void setACLMin() {
        PathInfo resp = fc.setAccessControlList(PATH_ACCESS_CONTROL_ENTRIES, GROUP, OWNER);

        assertNotNull(resp.getETag());
        assertNotNull(resp.getLastModified());
    }

    @Test
    public void setACLWithResponse() {
        assertEquals(200, fc.setAccessControlListWithResponse(PATH_ACCESS_CONTROL_ENTRIES, GROUP, OWNER, null, null, Context.NONE).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void setAclAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(fc, leaseID))
            .setIfMatch(setupPathMatchCondition(fc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertEquals(200, fc.setAccessControlListWithResponse(PATH_ACCESS_CONTROL_ENTRIES, GROUP, OWNER, drc, null, Context.NONE).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void setAclACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        setupPathLeaseCondition(fc, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(fc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class,
            () -> fc.setAccessControlListWithResponse(PATH_ACCESS_CONTROL_ENTRIES, GROUP, OWNER, drc, null, Context.NONE));
    }

    @Test
    public void setACLError() {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());

        assertThrows(DataLakeStorageException.class,
            () -> fc.setAccessControlList(PATH_ACCESS_CONTROL_ENTRIES, GROUP, OWNER));
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void setACLRecursive() {
        AccessControlChangeResult response = fc.setAccessControlRecursive(PATH_ACCESS_CONTROL_ENTRIES);

        assertEquals(0L, response.getCounters().getChangedDirectoriesCount());
        assertEquals(1L, response.getCounters().getChangedFilesCount());
        assertEquals(0L, response.getCounters().getFailedChangesCount());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void updateACLRecursive() {
        AccessControlChangeResult response = fc.updateAccessControlRecursive(PATH_ACCESS_CONTROL_ENTRIES);

        assertEquals(0L, response.getCounters().getChangedDirectoriesCount());
        assertEquals(1L, response.getCounters().getChangedFilesCount());
        assertEquals(0L, response.getCounters().getFailedChangesCount());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-02-10")
    @Test
    public void removeACLRecursive() {
        List<PathRemoveAccessControlEntry> removeAccessControlEntries = PathRemoveAccessControlEntry.parseList(
            "mask,default:user,default:group,user:ec3595d6-2c17-4696-8caa-7e139758d24a,"
            + "group:ec3595d6-2c17-4696-8caa-7e139758d24a,default:user:ec3595d6-2c17-4696-8caa-7e139758d24a,"
            + "default:group:ec3595d6-2c17-4696-8caa-7e139758d24a");

        AccessControlChangeResult response = fc.removeAccessControlRecursive(removeAccessControlEntries);

        assertEquals(0L, response.getCounters().getChangedDirectoriesCount());
        assertEquals(1L, response.getCounters().getChangedFilesCount());
        assertEquals(0L, response.getCounters().getFailedChangesCount());
    }

    @Test
    public void getAccessControlMin() {
        PathAccessControl pac = fc.getAccessControl();

        assertNotNull(pac.getAccessControlList());
        assertNotNull(pac.getPermissions());
        assertNotNull(pac.getOwner());
        assertNotNull(pac.getGroup());
    }

    @Test
    public void getAccessControlWithResponse() {
        assertEquals(200, fc.getAccessControlWithResponse(false, null, null, null).getStatusCode());
    }

    @Test
    public void getAccessControlReturnUpn() {
        assertEquals(200, fc.getAccessControlWithResponse(true, null, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void getAccessControlAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(fc, leaseID))
            .setIfMatch(setupPathMatchCondition(fc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertEquals(200, fc.getAccessControlWithResponse(false, drc, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void getAccessControlACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match,
        String noneMatch, String leaseID) {
        if (GARBAGE_LEASE_ID.equals(leaseID)) {
            return; // known bug in DFS endpoint
        }

        setupPathLeaseCondition(fc, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(fc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class, () -> fc.getAccessControlWithResponse(false, drc, null, null));
    }

    @Test
    public void getPropertiesDefault() {
        Response<PathProperties> response = fc.getPropertiesWithResponse(null, null, null);
        HttpHeaders headers = response.getHeaders();
        PathProperties properties = response.getValue();

        validateBasicHeaders(headers);
        assertEquals("bytes", headers.getValue(HttpHeaderName.ACCEPT_RANGES));
        assertNotNull(properties.getCreationTime());
        assertNotNull(properties.getLastModified());
        assertNotNull(properties.getETag());
        assertTrue(properties.getFileSize() >= 0);
        assertNotNull(properties.getContentType());
        assertNull(properties.getContentMd5()); // tested in "set HTTP headers"
        assertNull(properties.getContentEncoding()); // tested in "set HTTP headers"
        assertNull(properties.getContentDisposition()); // tested in "set HTTP headers"
        assertNull(properties.getContentLanguage()); // tested in "set HTTP headers"
        assertNull(properties.getCacheControl()); // tested in "set HTTP headers"
        assertEquals(LeaseStatusType.UNLOCKED, properties.getLeaseStatus());
        assertEquals(LeaseStateType.AVAILABLE, properties.getLeaseState());
        assertNull(properties.getLeaseDuration()); // tested in "acquire lease"
        assertNull(properties.getCopyId()); // tested in "abort copy"
        assertNull(properties.getCopyStatus()); // tested in "copy"
        assertNull(properties.getCopySource()); // tested in "copy"
        assertNull(properties.getCopyProgress()); // tested in "copy"
        assertNull(properties.getCopyCompletionTime()); // tested in "copy"
        assertNull(properties.getCopyStatusDescription()); // only returned when the service has errors; cannot validate.
        assertTrue(properties.isServerEncrypted());
        assertFalse(properties.isIncrementalCopy() != null && properties.isIncrementalCopy()); // tested in PageBlob."start incremental copy"
        assertEquals(AccessTier.HOT, properties.getAccessTier());
        assertNull(properties.getArchiveStatus());
        assertTrue(CoreUtils.isNullOrEmpty(properties.getMetadata())); // new file does not have default metadata associated
        assertNull(properties.getAccessTierChangeTime());
        assertNull(properties.getEncryptionKeySha256());
        assertFalse(properties.isDirectory());
    }

    @Test
    public void getPropertiesMin() {
        assertEquals(200, fc.getPropertiesWithResponse(null, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void getPropertiesAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(fc, leaseID))
            .setIfMatch(setupPathMatchCondition(fc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertEquals(200, fc.getPropertiesWithResponse(drc, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void getPropertiesACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(fc, leaseID))
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(fc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class, () -> fc.getPropertiesWithResponse(drc, null, null));
    }

    @Test
    public void getPropertiesError() {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());

        DataLakeStorageException ex = assertThrows(DataLakeStorageException.class, () -> fc.getProperties());
        assertTrue(ex.getMessage().contains("BlobNotFound"));
    }

    @Test
    public void setHTTPHeadersNull() {
        Response<?> response = fc.setHttpHeadersWithResponse(null, null, null, null);

        assertEquals(200, response.getStatusCode());
        validateBasicHeaders(response.getHeaders());
    }

    @Test
    public void setHTTPHeadersMin() throws NoSuchAlgorithmException {
        PathProperties properties = fc.getProperties();
        PathHttpHeaders headers = new PathHttpHeaders()
            .setContentEncoding(properties.getContentEncoding())
            .setContentDisposition(properties.getContentDisposition())
            .setContentType("type")
            .setCacheControl(properties.getCacheControl())
            .setContentLanguage(properties.getContentLanguage())
            .setContentMd5(Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(DATA.getDefaultBytes())));

        fc.setHttpHeaders(headers);

        assertEquals("type", fc.getProperties().getContentType());
    }

    @ParameterizedTest
    @MethodSource("setHTTPHeadersHeadersSupplier")
    public void setHTTPHeadersHeaders(String cacheControl, String contentDisposition, String contentEncoding,
        String contentLanguage, byte[] contentMD5, String contentType) {

        fc.append(DATA.getDefaultBinaryData(), 0);
        fc.flush(DATA.getDefaultDataSizeLong(), true);
        PathHttpHeaders putHeaders = new PathHttpHeaders()
            .setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType);

        fc.setHttpHeaders(putHeaders);

        validatePathProperties(fc.getPropertiesWithResponse(null, null, null), cacheControl, contentDisposition,
            contentEncoding, contentLanguage, contentMD5, contentType);
    }

    private static Stream<Arguments> setHTTPHeadersHeadersSupplier() throws NoSuchAlgorithmException {
        return Stream.of(
            // cacheControl, contentDisposition, contentEncoding, contentLanguage, contentMD5, contentType
            Arguments.of(null, null, null, null, null, null),
            Arguments.of("control", "disposition", "encoding", "language",
                Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(DATA.getDefaultBytes())), "type")
        );
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void setHttpHeadersAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(fc, leaseID))
            .setIfMatch(setupPathMatchCondition(fc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertEquals(200, fc.setHttpHeadersWithResponse(null, drc, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void setHttpHeadersACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        setupPathLeaseCondition(fc, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(fc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class, () -> fc.setHttpHeadersWithResponse(null, drc, null, null));
    }

    @Test
    public void setHTTPHeadersError() {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());

        assertThrows(DataLakeStorageException.class, () -> fc.setHttpHeaders(null));
    }

    @Test
    public void setMetadataMin() {
        Map<String, String> metadata = Collections.singletonMap("foo", "bar");
        fc.setMetadata(metadata);

        assertEquals(metadata, fc.getProperties().getMetadata());
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null,200", "foo,bar,fizz,buzz,200"}, nullValues = "null")
    public void setMetadataMetadata(String key1, String value1, String key2, String value2, int statusCode) {
        Map<String, String> metadata = new HashMap<>();
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2);
        }

        assertEquals(statusCode, fc.setMetadataWithResponse(metadata, null, null, null).getStatusCode());
        assertEquals(metadata, fc.getProperties().getMetadata());
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void setMetadataAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(fc, leaseID))
            .setIfMatch(setupPathMatchCondition(fc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertEquals(200, fc.setMetadataWithResponse(null, drc, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void setMetadataACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        setupPathLeaseCondition(fc, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(fc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class, () -> fc.setMetadataWithResponse(null, drc, null, null));
    }

    @Test
    public void setMetadataError() {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());

        assertThrows(DataLakeStorageException.class, () -> fc.setMetadata(null));
    }

    @Test
    public void readAllNull() {
        fc.append(DATA.getDefaultBinaryData(), 0);
        fc.flush(DATA.getDefaultDataSizeLong(), true);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        FileReadResponse response = fc.readWithResponse(stream, null, null, null, false, null, null);
        HttpHeaders headers = response.getHeaders();

        TestUtils.assertArraysEqual(DATA.getDefaultBytes(), stream.toByteArray());
        assertFalse(headers.stream().anyMatch(h -> h.getName().startsWith("x-ms-meta-")));
        assertNotNull(headers.getValue(HttpHeaderName.CONTENT_LENGTH));
        assertNotNull(headers.getValue(HttpHeaderName.CONTENT_TYPE));
        assertNull(headers.getValue(HttpHeaderName.CONTENT_RANGE));
        assertNull(headers.getValue(HttpHeaderName.CONTENT_ENCODING));
        assertNull(headers.getValue(HttpHeaderName.CACHE_CONTROL));
        assertNull(headers.getValue(HttpHeaderName.CONTENT_DISPOSITION));
        assertNull(headers.getValue(HttpHeaderName.CONTENT_LANGUAGE));
        assertNull(headers.getValue(X_MS_BLOB_SEQUENCE_NUMBER));
        assertNull(headers.getValue(X_MS_COPY_COMPLETION_TIME));
        assertNull(headers.getValue(X_MS_COPY_STATUS_DESCRIPTION));
        assertNull(headers.getValue(X_MS_COPY_ID));
        assertNull(headers.getValue(X_MS_COPY_PROGRESS));
        assertNull(headers.getValue(X_MS_COPY_SOURCE));
        assertNull(headers.getValue(X_MS_COPY_STATUS));
        assertNull(headers.getValue(X_MS_LEASE_DURATION));
        assertEquals(LeaseStateType.AVAILABLE.toString(), headers.getValue(X_MS_LEASE_STATE));
        assertEquals(LeaseStatusType.UNLOCKED.toString(), headers.getValue(X_MS_LEASE_STATUS));
        assertEquals("bytes", headers.getValue(HttpHeaderName.ACCEPT_RANGES));
        assertNull(headers.getValue(X_MS_BLOB_COMMITTED_BLOCK_COUNT));
        assertNotNull(headers.getValue(X_MS_SERVER_ENCRYPTED));
        assertNull(headers.getValue(X_MS_BLOB_CONTENT_MD5));
        assertNotNull(headers.getValue(X_MS_CREATION_TIME));
        assertNotNull(response.getDeserializedHeaders().getCreationTime());
    }

    @Test
    public void readEmptyFile() {
        fc = dataLakeFileSystemClient.createFile("emptyFile");

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        fc.read(outStream);

        assertEquals(0, outStream.toByteArray().length);
    }

    // This is to test the appropriate integration of DownloadResponse, including setting the correct range values on
    // HttpGetterInfo.
    @Test
    public void readWithRetryRange() {
        // We are going to make a request for some range on a blob. The Flux returned will throw an exception, forcing
        // a retry per the DownloadRetryOptions. The next request should have the same range header, which was generated
        // from the count and offset values in HttpGetterInfo that was constructed on the initial call to download. We
        // don't need to check the data here, but we want to ensure that the correct range is set each time. This will
        // test the correction of a bug that was found which caused HttpGetterInfo to have an incorrect offset when it
        // was constructed in BlobClient.download().
        DataLakeFileClient fileClient = getFileClient(getDataLakeCredential(), fc.getPathUrl(),
            new MockRetryRangeResponsePolicy("bytes=2-6"));

        fc.append(DATA.getDefaultBinaryData(), 0);
        fc.flush(DATA.getDefaultDataSizeLong(), true);

        // Because the dummy Flux always throws an error. This will also validate that an IllegalArgumentException is
        // NOT thrown because the types would not match.
        RuntimeException e = assertThrows(RuntimeException.class, () -> fileClient.readWithResponse(
            new ByteArrayOutputStream(), new FileRange(2, 5L), new DownloadRetryOptions().setMaxRetryRequests(3), null,
            false, null, null));

        assertInstanceOf(IOException.class, e.getCause());
    }

    @Test
    public void readMin() {
        fc.append(DATA.getDefaultBinaryData(), 0);
        fc.flush(DATA.getDefaultDataSizeLong(), true);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        fc.read(outStream);

        TestUtils.assertArraysEqual(DATA.getDefaultBytes(), outStream.toByteArray());
    }

    @ParameterizedTest
    @MethodSource("readRangeSupplier")
    public void readRange(long offset, Long count, String expectedData) {
        FileRange range = (count == null) ? new FileRange(offset) : new FileRange(offset, count);
        fc.append(DATA.getDefaultBinaryData(), 0);
        fc.flush(DATA.getDefaultDataSizeLong(), true);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        fc.readWithResponse(outStream, range, null, null, false, null, null);

        assertEquals(expectedData, outStream.toString());
    }

    private static Stream<Arguments> readRangeSupplier() {
        return Stream.of(
            // offset | count || expectedData
            Arguments.of(0L, null, DATA.getDefaultText()),
            Arguments.of(0L, 5L, DATA.getDefaultText().substring(0, 5)),
            Arguments.of(3L, 2L, DATA.getDefaultText().substring(3, 3 + 2))
        );
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void readAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(fc, leaseID))
            .setIfMatch(setupPathMatchCondition(fc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertEquals(200, fc.readWithResponse(new ByteArrayOutputStream(), null, null, drc, false, null, null)
            .getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void readACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        setupPathLeaseCondition(fc, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(fc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class,
            () -> fc.readWithResponse(new ByteArrayOutputStream(), null, null, drc, false, null, null));
    }

    @Test
    public void readMd5() throws NoSuchAlgorithmException {
        fc.append(DATA.getDefaultBinaryData(), 0);
        fc.flush(DATA.getDefaultDataSizeLong(), true);

        byte[] contentMD5 = fc.readWithResponse(new ByteArrayOutputStream(), new FileRange(0, 3L), null, null, true, null, null)
            .getHeaders().getValue(HttpHeaderName.CONTENT_MD5)
            .getBytes();

        TestUtils.assertArraysEqual(
            Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(DATA.getDefaultText().substring(0, 3).getBytes())),
            contentMD5);
    }

    @Test
    public void readRetryDefault() {
        fc.append(DATA.getDefaultBinaryData(), 0);
        fc.flush(DATA.getDefaultDataSizeLong(), true);
        DataLakeFileClient failureFileClient = getFileClient(getDataLakeCredential(), fc.getFileUrl(),
            new MockFailureResponsePolicy(5));

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        failureFileClient.read(outStream);

        assertEquals(DATA.getDefaultText(), outStream.toString());
    }

    @Test
    public void readError() {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());

        assertThrows(NullPointerException.class, () -> fc.read(null));
    }

    @Test
    public void downloadToFileExists() throws IOException {
        File testFile = new File(prefix + ".txt");
        testFile.deleteOnExit();
        createdFiles.add(testFile);

        if (!testFile.exists()) {
            assertTrue(testFile.createNewFile());
        }

        fc.append(DATA.getDefaultBinaryData(), 0);
        fc.flush(DATA.getDefaultDataSizeLong(), true);

        // Default overwrite is false so this should fail
        UncheckedIOException ex = assertThrows(UncheckedIOException.class, () -> fc.readToFile(testFile.getPath()));
        assertInstanceOf(FileAlreadyExistsException.class, ex.getCause());
    }

    @Test
    public void downloadToFileExistsSucceeds() throws IOException {
        File testFile = new File(prefix + ".txt");
        testFile.deleteOnExit();
        createdFiles.add(testFile);

        if (!testFile.exists()) {
            assertTrue(testFile.createNewFile());
        }

        fc.append(DATA.getDefaultBinaryData(), 0);
        fc.flush(DATA.getDefaultDataSizeLong(), true);

        fc.readToFile(testFile.getPath(), true);

        assertEquals(DATA.getDefaultText(), new String(Files.readAllBytes(testFile.toPath()), StandardCharsets.UTF_8));
    }

    @Test
    public void downloadToFileDoesNotExist() throws IOException {
        File testFile = new File(prefix + ".txt");
        testFile.deleteOnExit();
        createdFiles.add(testFile);

        if (testFile.exists()) {
            assertTrue(testFile.delete());
        }

        fc.append(DATA.getDefaultBinaryData(), 0);
        fc.flush(DATA.getDefaultDataSizeLong(), true);

        fc.readToFile(testFile.getPath());

        assertEquals(DATA.getDefaultText(), new String(Files.readAllBytes(testFile.toPath()), StandardCharsets.UTF_8));
    }

    @Test
    public void downloadFileDoesNotExistOpenOptions() throws IOException {
        File testFile = new File(prefix + ".txt");
        testFile.deleteOnExit();
        createdFiles.add(testFile);

        if (!testFile.exists()) {
            assertTrue(testFile.createNewFile());
        }

        fc.append(DATA.getDefaultBinaryData(), 0);
        fc.flush(DATA.getDefaultDataSizeLong(), true);

        Set<OpenOption> openOptions = new HashSet<>(Arrays.asList(
            StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE));
        fc.readToFileWithResponse(testFile.getPath(), null, null, null, null, false, openOptions, null, null);

        assertEquals(DATA.getDefaultText(), new String(Files.readAllBytes(testFile.toPath()), StandardCharsets.UTF_8));
    }

    @Test
    public void downloadFileExistOpenOptions() throws IOException {
        File testFile = new File(prefix + ".txt");
        testFile.deleteOnExit();
        createdFiles.add(testFile);

        if (!testFile.exists()) {
            assertTrue(testFile.createNewFile());
        }

        fc.append(DATA.getDefaultBinaryData(), 0);
        fc.flush(DATA.getDefaultDataSizeLong(), true);

        Set<OpenOption> openOptions = new HashSet<>(Arrays.asList(StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.READ, StandardOpenOption.WRITE));
        fc.readToFileWithResponse(testFile.getPath(), null, null, null, null, false, openOptions, null, null);

        assertEquals(DATA.getDefaultText(), new String(Files.readAllBytes(testFile.toPath()), StandardCharsets.UTF_8));
    }

    @LiveOnly
    @ParameterizedTest
    @MethodSource("downloadFileSupplier")
    public void downloadFile(int fileSize) {
        File file = getRandomFile(fileSize);
        file.deleteOnExit();
        createdFiles.add(file);

        fc.uploadFromFile(file.toPath().toString(), true);
        File outFile = new File(testResourceNamer.randomName("", 60) + ".txt");
        outFile.deleteOnExit();
        createdFiles.add(outFile);

        if (outFile.exists()) {
            assertTrue(outFile.delete());
        }

        PathProperties properties = fc.readToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions().setBlockSizeLong(4L * 1024 * 1024), null, null, false, null, null, null)
            .getValue();

        compareFiles(file, outFile, 0, fileSize);
        assertEquals(fileSize, properties.getFileSize());
    }

    private static Stream<Integer> downloadFileSupplier() {
        return Stream.of(
            // fileSize
            20, // small file
            16 * 1024 * 1024, // medium file in several chunks
            8 * 1026 * 1024 + 10, // medium file not aligned to block
            50 * Constants.MB // large file requiring multiple requests
        );
    }

    @LiveOnly
    @ParameterizedTest
    @MethodSource("downloadFileSupplier")
    public void downloadFileSyncBufferCopy(int fileSize) {
        String fileSystemName = generateFileSystemName();
        DataLakeServiceClient datalakeServiceClient = new DataLakeServiceClientBuilder()
            .endpoint(ENVIRONMENT.getDataLakeAccount().getDataLakeEndpoint())
            .credential(getDataLakeCredential())
            .buildClient();

        DataLakeFileClient fileClient = datalakeServiceClient.createFileSystem(fileSystemName)
            .getFileClient(generatePathName());

        File file = getRandomFile(fileSize);
        file.deleteOnExit();
        createdFiles.add(file);

        fileClient.uploadFromFile(file.toPath().toString(), true);
        File outFile = new File(testResourceNamer.randomName("", 60) + ".txt");
        outFile.deleteOnExit();
        createdFiles.add(outFile);

        if (outFile.exists()) {
            assertTrue(outFile.delete());
        }

        PathProperties properties = fileClient.readToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions().setBlockSizeLong(4L * 1024 * 1024), null, null, false, null, null, null)
            .getValue();


        compareFiles(file, outFile, 0, fileSize);
        assertEquals(fileSize, properties.getFileSize());
    }

    @ParameterizedTest
    @MethodSource("downloadFileRangeSupplier")
    public void downloadFileRange(FileRange range) {
        File file = getRandomFile(DATA.getDefaultDataSize());
        file.deleteOnExit();
        createdFiles.add(file);

        fc.uploadFromFile(file.toPath().toString(), true);

        File outFile = new File(testResourceNamer.randomName("", 60));
        outFile.deleteOnExit();
        createdFiles.add(outFile);

        if (outFile.exists()) {
            assertTrue(outFile.delete());
        }

        fc.readToFileWithResponse(outFile.toPath().toString(), range, null, null, null, false, null, null, null);

        compareFiles(file, outFile, range.getOffset(), range.getCount());
    }

    private static Stream<FileRange> downloadFileRangeSupplier() {
        // The last case is to test a range much larger than the size of the file to ensure we don't accidentally
        // send off parallel requests with invalid ranges.
        return Stream.of(
            new FileRange(0, DATA.getDefaultDataSizeLong()), // Exact count
            new FileRange(1, DATA.getDefaultDataSizeLong() - 1), // Offset and exact count
            new FileRange(3, 2L), // Narrow range in middle
            new FileRange(0, DATA.getDefaultDataSizeLong() - 1), // Count that is less than total
            new FileRange(0, 10 * 1024L) // Count much larger than remaining data
        );
    }

    @Test
    public void downloadFileRangeFail() {
        File file = getRandomFile(DATA.getDefaultDataSize());
        file.deleteOnExit();
        createdFiles.add(file);

        fc.uploadFromFile(file.toPath().toString(), true);

        File outFile = new File(prefix);
        outFile.deleteOnExit();
        createdFiles.add(outFile);

        if (outFile.exists()) {
            assertTrue(outFile.delete());
        }

        assertThrows(DataLakeStorageException.class, () -> fc.readToFileWithResponse(outFile.toPath().toString(),
            new FileRange(DATA.getDefaultDataSizeLong() + 1), null, null, null, false, null, null, null));
    }

    @Test
    public void downloadFileCountNull() {
        File file = getRandomFile(DATA.getDefaultDataSize());
        file.deleteOnExit();
        createdFiles.add(file);

        fc.uploadFromFile(file.toPath().toString(), true);

        File outFile = new File(prefix);
        outFile.deleteOnExit();
        createdFiles.add(outFile);

        if (outFile.exists()) {
            assertTrue(outFile.delete());
        }

        fc.readToFileWithResponse(outFile.toPath().toString(), new FileRange(0), null, null, null, false, null, null, null);

        compareFiles(file, outFile, 0, DATA.getDefaultDataSizeLong());
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void downloadFileAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        File file = getRandomFile(DATA.getDefaultDataSize());
        file.deleteOnExit();
        createdFiles.add(file);

        fc.uploadFromFile(file.toPath().toString(), true);

        File outFile = new File(testResourceNamer.randomName("", 60));
        outFile.deleteOnExit();
        createdFiles.add(outFile);

        if (outFile.exists()) {
            assertTrue(outFile.delete());
        }

        DataLakeRequestConditions bro = new DataLakeRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(setupPathMatchCondition(fc, match))
            .setIfNoneMatch(noneMatch)
            .setLeaseId(setupPathLeaseCondition(fc, leaseID));

        assertDoesNotThrow(() -> fc.readToFileWithResponse(outFile.toPath().toString(), null, null, null, bro, false,
            null, null, null));
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void downloadFileACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        File file = getRandomFile(DATA.getDefaultDataSize());
        file.deleteOnExit();
        createdFiles.add(file);

        fc.uploadFromFile(file.toPath().toString(), true);

        File outFile = new File(prefix);
        outFile.deleteOnExit();
        createdFiles.add(outFile);

        if (outFile.exists()) {
            assertTrue(outFile.delete());
        }

        setupPathLeaseCondition(fc, leaseID);
        DataLakeRequestConditions bro = new DataLakeRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(fc, noneMatch))
            .setLeaseId(leaseID);

        DataLakeStorageException e = assertThrows(DataLakeStorageException.class, () -> fc.readToFileWithResponse(
            outFile.toPath().toString(), null, null, null, bro, false, null, null, null));

        assertTrue(Objects.equals(e.getErrorCode(), "ConditionNotMet") || Objects.equals(e.getErrorCode(), "LeaseIdMismatchWithBlobOperation"));
    }



    @SuppressWarnings("deprecation")
    @LiveOnly
    @ParameterizedTest
    @ValueSource(ints = {
        100,
        8 * 1026 * 1024 + 10
    })
    public void downloadFileProgressReceiver(int fileSize) {
        File file = getRandomFile(fileSize);
        file.deleteOnExit();
        createdFiles.add(file);

        fc.uploadFromFile(file.toPath().toString(), true);

        File outFile = new File(prefix);
        outFile.deleteOnExit();
        createdFiles.add(outFile);

        if (outFile.exists()) {
            assertTrue(outFile.delete());
        }

        MockReceiver mockReceiver = new MockReceiver();

        fc.readToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions().setProgressReceiver(mockReceiver),
            new DownloadRetryOptions().setMaxRetryRequests(3), null, false, null, null, null);

        // Should receive at least one notification indicating completed progress, multiple notifications may be
        // received if there are empty buffers in the stream.
        assertTrue(mockReceiver.progresses.stream().anyMatch(progress -> progress == fileSize));

        // There should be NO notification with a larger than expected size.
        assertFalse(mockReceiver.progresses.stream().anyMatch(progress -> progress > fileSize));

        // We should receive at least one notification reporting an intermediary value per block, but possibly more
        // notifications will be received depending on the implementation. We specify numBlocks - 1 because the last
        // block will be the total size as above. Finally, we assert that the number reported monotonically increases.
        long prevCount = -1;
        for (long progress : mockReceiver.progresses) {
            assertTrue(progress >= prevCount, "Reported progress should monotonically increase");
            prevCount = progress;
        }
    }

    @SuppressWarnings("deprecation")
    private static final class MockReceiver implements ProgressReceiver {
        List<Long> progresses = new ArrayList<>();

        @Override
        public void reportProgress(long bytesTransferred) {
            progresses.add(bytesTransferred);
        }
    }

    @LiveOnly
    @ParameterizedTest
    @ValueSource(ints = {100, 8 * 1026 * 1024 + 10})
    public void downloadFileProgressListener(int fileSize) {
        File file = getRandomFile(fileSize);
        file.deleteOnExit();
        createdFiles.add(file);

        fc.uploadFromFile(file.toPath().toString(), true);

        File outFile = new File(prefix);
        outFile.deleteOnExit();
        createdFiles.add(outFile);

        if (outFile.exists()) {
            assertTrue(outFile.delete());
        }

        MockProgressListener mockListener = new MockProgressListener();

        fc.readToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions().setProgressListener(mockListener),
            new DownloadRetryOptions().setMaxRetryRequests(3), null, false, null, null, null);

        // Should receive at least one notification indicating completed progress, multiple notifications may be
        // received if there are empty buffers in the stream.
        assertTrue(mockListener.progresses.stream().anyMatch(progress -> progress == fileSize));

        // There should be NO notification with a larger than expected size.
        assertFalse(mockListener.progresses.stream().anyMatch(progress -> progress > fileSize));

        // We should receive at least one notification reporting an intermediary value per block, but possibly more
        // notifications will be received depending on the implementation. We specify numBlocks - 1 because the last
        // block will be the total size as above. Finally, we assert that the number reported monotonically increases.
        long prevCount = -1;
        for (long progress : mockListener.progresses) {
            assertTrue(progress >= prevCount, "Reported progress should monotonically increase");
            prevCount = progress;
        }
    }

    private static final class MockProgressListener implements ProgressListener {
        List<Long> progresses = new ArrayList<>();

        @Override
        public void handleProgress(long progress) {
            progresses.add(progress);
        }
    }

    @Test
    public void renameMin() {
        assertEquals(201, fc.renameWithResponse(null, generatePathName(), null, null, null, null).getStatusCode());
    }

    @Test
    public void renameWithResponse() {
        DataLakeFileClient renamedClient = fc.renameWithResponse(null, generatePathName(), null, null, null, null).getValue();

        assertDoesNotThrow(() -> renamedClient.getProperties());
        assertThrows(DataLakeStorageException.class, fc::getProperties);
    }

    @Test
    public void renameFilesystemWithResponse() {
        DataLakeFileSystemClient newFileSystem = primaryDataLakeServiceClient.createFileSystem(generateFileSystemName());

        DataLakeFileClient renamedClient = fc.renameWithResponse(newFileSystem.getFileSystemName(), generatePathName(),
            null, null, null, null).getValue();

        assertDoesNotThrow(() -> renamedClient.getProperties());
        assertThrows(DataLakeStorageException.class, fc::getProperties);
    }

    @Test
    public void renameError() {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());

        assertThrows(DataLakeStorageException.class,
            () -> fc.renameWithResponse(null, generatePathName(), null, null, null, null));
    }

    @ParameterizedTest
    @CsvSource({",", "%20%25,%20%25", "%20%25,", ",%20%25"})
    public void renameUrlEncoded(String source, String destination) {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName() + source);
        fc.create();
        Response<DataLakeFileClient> response = fc.renameWithResponse(null, generatePathName() + destination, null, null, null, null);

        assertEquals(201, response.getStatusCode());
        assertEquals(200,  response.getValue().getPropertiesWithResponse(null, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void renameSourceAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(fc, leaseID))
            .setIfMatch(setupPathMatchCondition(fc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertEquals(201, fc.renameWithResponse(null, generatePathName(), drc, null, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void renameSourceACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        fc = dataLakeFileSystemClient.createFile(generatePathName());

        setupPathLeaseCondition(fc, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(fc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class,
            () -> fc.renameWithResponse(null, generatePathName(), drc, null, null, null));
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void renameDestAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        String pathName = generatePathName();
        DataLakeFileClient destFile = dataLakeFileSystemClient.createFile(pathName);

        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(destFile, leaseID))
            .setIfMatch(setupPathMatchCondition(destFile, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertEquals(201, fc.renameWithResponse(null, pathName, null, drc, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void renameDestACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        String pathName = generatePathName();
        DataLakeFileClient destFile = dataLakeFileSystemClient.createFile(pathName);

        setupPathLeaseCondition(destFile, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(destFile, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class, () -> fc.renameWithResponse(null, pathName, null, drc, null, null));
    }

    @Test
    public void renameSasToken() {
        FileSystemSasPermission permissions = new FileSystemSasPermission()
            .setReadPermission(true)
            .setMovePermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setAddPermission(true)
            .setDeletePermission(true);

        String sas = dataLakeFileSystemClient.generateSas(
            new DataLakeServiceSasSignatureValues(testResourceNamer.now().plusDays(1), permissions));
        DataLakeFileClient client = getFileClient(sas, dataLakeFileSystemClient.getFileSystemUrl(), fc.getFilePath());

        DataLakeFileClient destClient = client.rename(dataLakeFileSystemClient.getFileSystemName(), generatePathName());

        assertDoesNotThrow(() -> destClient.getProperties());
    }

    @Test
    public void renameSasTokenWithLeadingQuestionMark() {
        FileSystemSasPermission permissions = new FileSystemSasPermission()
            .setReadPermission(true)
            .setMovePermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setAddPermission(true)
            .setDeletePermission(true);

        String sas = "?" + dataLakeFileSystemClient.generateSas(
            new DataLakeServiceSasSignatureValues(testResourceNamer.now().plusDays(1), permissions));
        DataLakeFileClient client = getFileClient(sas, dataLakeFileSystemClient.getFileSystemUrl(), fc.getFilePath());

        DataLakeFileClient destClient = client.rename(dataLakeFileSystemClient.getFileSystemName(), generatePathName());

        assertDoesNotThrow(() -> destClient.getProperties());
    }

    @Test
    public void appendDataMin() {
        assertDoesNotThrow(() -> fc.append(DATA.getDefaultBinaryData(), 0));
    }

    @Test
    public void appendData() {
        Response<Void> response = fc.appendWithResponse(DATA.getDefaultBinaryData(), 0, null, null, null, null);
        HttpHeaders headers = response.getHeaders();

        assertEquals(202, response.getStatusCode());
        assertNotNull(headers.getValue(X_MS_REQUEST_ID));
        assertNotNull(headers.getValue(X_MS_VERSION));
        assertNotNull(headers.getValue(HttpHeaderName.DATE));
        assertTrue(Boolean.parseBoolean(headers.getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
    }

    @Test
    public void appendDataMd5() throws NoSuchAlgorithmException {
        fc = dataLakeFileSystemClient.createFile(generatePathName());
        byte[] md5 = MessageDigest.getInstance("MD5").digest(DATA.getDefaultText().getBytes());
        Response<Void> response = fc.appendWithResponse(DATA.getDefaultBinaryData(), 0, md5, null, null, null);
        HttpHeaders headers = response.getHeaders();

        assertEquals(202, response.getStatusCode());
        assertNotNull(headers.getValue(X_MS_REQUEST_ID));
        assertNotNull(headers.getValue(X_MS_VERSION));
        assertNotNull(headers.getValue(HttpHeaderName.DATE));
        assertTrue(Boolean.parseBoolean(headers.getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
    }

    @ParameterizedTest
    @MethodSource("appendDataIllegalArgumentsSupplier")
    public void appendDataIllegalArguments(InputStream is, long dataSize, Class<? extends Throwable> exceptionType) {
        assertThrows(exceptionType, () -> fc.append(is, 0, dataSize));
    }

    private static Stream<Arguments> appendDataIllegalArgumentsSupplier() {
        return Stream.of(
            // is | dataSize || exceptionType
            Arguments.of(null, DATA.getDefaultDataSizeLong(), NullPointerException.class),
            Arguments.of(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong() + 1, UnexpectedLengthException.class),
            Arguments.of(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong() - 1, UnexpectedLengthException.class)
        );
    }

    @Test
    public void appendDataEmptyBody() {
        fc = dataLakeFileSystemClient.createFile(generatePathName());

        assertThrows(DataLakeStorageException.class, () -> fc.append(new ByteArrayInputStream(new byte[0]), 0, 0));
    }

    @Test
    public void appendDataNullBody() {
        fc = dataLakeFileSystemClient.createFile(generatePathName());

        assertThrows(NullPointerException.class, () -> fc.append(null, 0, 0));
    }

    @Test
    public void appendDataLease() {
        assertEquals(202, fc.appendWithResponse(DATA.getDefaultBinaryData(), 0, null,
            setupPathLeaseCondition(fc, RECEIVED_LEASE_ID), null, null).getStatusCode());
    }

    @Test
    public void appendDataLeaseFail() {
        setupPathLeaseCondition(fc, RECEIVED_LEASE_ID);

        DataLakeStorageException e = assertThrows(DataLakeStorageException.class,
            () -> fc.appendWithResponse(DATA.getDefaultBinaryData(), 0, null, GARBAGE_LEASE_ID, null, null));
        assertEquals(412, e.getResponse().getStatusCode());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-08-04")
    @Test
    public void appendDataLeaseAcquire() {
        fc = dataLakeFileSystemClient.createFileIfNotExists(generatePathName());

        DataLakeFileAppendOptions appendOptions = new DataLakeFileAppendOptions()
            .setLeaseAction(LeaseAction.ACQUIRE)
            .setProposedLeaseId(CoreUtils.randomUuid().toString())
            .setLeaseDuration(15);

        assertEquals(202, fc.appendWithResponse(DATA.getDefaultBinaryData(), 0, appendOptions, null, null).getStatusCode());

        PathProperties fileProperties = fc.getProperties();
        assertEquals(LeaseStatusType.LOCKED, fileProperties.getLeaseStatus());
        assertEquals(LeaseStateType.LEASED, fileProperties.getLeaseState());
        assertEquals(LeaseDurationType.FIXED, fileProperties.getLeaseDuration());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-08-04")
    @Test
    public void appendDataLeaseAutoRenew() {
        fc = dataLakeFileSystemClient.createFileIfNotExists(generatePathName());
        String leaseId = CoreUtils.randomUuid().toString();

        DataLakeLeaseClient leaseClient = createLeaseClient(fc, leaseId);
        leaseClient.acquireLease(15);

        DataLakeFileAppendOptions appendOptions = new DataLakeFileAppendOptions()
            .setLeaseAction(LeaseAction.AUTO_RENEW)
            .setLeaseId(leaseId);

        assertEquals(202, fc.appendWithResponse(DATA.getDefaultBinaryData(), 0, appendOptions, null, null).getStatusCode());

        PathProperties fileProperties = fc.getProperties();
        assertEquals(LeaseStatusType.LOCKED, fileProperties.getLeaseStatus());
        assertEquals(LeaseStateType.LEASED, fileProperties.getLeaseState());
        assertEquals(LeaseDurationType.FIXED, fileProperties.getLeaseDuration());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-08-04")
    @Test
    public void appendDataLeaseRelease() {
        fc = dataLakeFileSystemClient.createFileIfNotExists(generatePathName());

        String leaseId = CoreUtils.randomUuid().toString();

        DataLakeLeaseClient leaseClient = createLeaseClient(fc, leaseId);
        leaseClient.acquireLease(15);

        DataLakeFileAppendOptions appendOptions = new DataLakeFileAppendOptions()
            .setLeaseAction(LeaseAction.RELEASE)
            .setLeaseId(leaseId)
            .setFlush(true);

        assertEquals(202, fc.appendWithResponse(DATA.getDefaultBinaryData(), 0, appendOptions, null, null).getStatusCode());

        PathProperties fileProperties = fc.getProperties();
        assertEquals(LeaseStatusType.UNLOCKED, fileProperties.getLeaseStatus());
        assertEquals(LeaseStateType.AVAILABLE, fileProperties.getLeaseState());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-08-04")
    @Test
    public void appendDataLeaseAcquireRelease() {
        fc = dataLakeFileSystemClient.createFileIfNotExists(generatePathName());

        DataLakeFileAppendOptions appendOptions = new DataLakeFileAppendOptions()
            .setLeaseAction(LeaseAction.ACQUIRE_RELEASE)
            .setProposedLeaseId(CoreUtils.randomUuid().toString())
            .setLeaseDuration(15)
            .setFlush(true);

        assertEquals(202, fc.appendWithResponse(DATA.getDefaultBinaryData(), 0, appendOptions, null, null).getStatusCode());

        PathProperties fileProperties = fc.getProperties();
        assertEquals(LeaseStatusType.UNLOCKED, fileProperties.getLeaseStatus());
        assertEquals(LeaseStateType.AVAILABLE, fileProperties.getLeaseState());
    }

    @Test
    public void appendDataError() {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());

        DataLakeStorageException e = assertThrows(DataLakeStorageException.class,
            () -> fc.appendWithResponse(DATA.getDefaultBinaryData(), 0, null, null, null, null));
        assertEquals(404, e.getResponse().getStatusCode());
    }

    @Test
    public void appendDataRetryOnTransientFailure() {
        DataLakeFileClient clientWithFailure = getFileClient(getDataLakeCredential(), fc.getFileUrl(),
            new TransientFailureInjectingHttpPipelinePolicy());

        clientWithFailure.append(DATA.getDefaultBinaryData(), 0);
        fc.flush(DATA.getDefaultDataSizeLong(), true);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        fc.read(os);

        TestUtils.assertArraysEqual(DATA.getDefaultBytes(), os.toByteArray());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @Test
    public void appendDataFlush() {
        DataLakeFileAppendOptions appendOptions = new DataLakeFileAppendOptions().setFlush(true);
        Response<Void> response = fc.appendWithResponse(DATA.getDefaultBinaryData(), 0, appendOptions, null, null);
        HttpHeaders headers = response.getHeaders();

        assertEquals(202, response.getStatusCode());
        assertNotNull(headers.getValue(X_MS_REQUEST_ID));
        assertNotNull(headers.getValue(X_MS_VERSION));
        assertNotNull(headers.getValue(HttpHeaderName.DATE));
        assertTrue(Boolean.parseBoolean(headers.getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        fc.read(os);

        TestUtils.assertArraysEqual(DATA.getDefaultBytes(), os.toByteArray());
    }

    @Test
    public void appendBinaryDataMin() {
        assertDoesNotThrow(() -> fc.append(DATA.getDefaultBinaryData(), 0));
    }

    @Test
    public void appendBinaryData() {
        Response<Void> response = fc.appendWithResponse(DATA.getDefaultBinaryData(), 0, null, null, null, null);
        HttpHeaders headers = response.getHeaders();

        assertEquals(202, response.getStatusCode());
        assertNotNull(headers.getValue(X_MS_REQUEST_ID));
        assertNotNull(headers.getValue(X_MS_VERSION));
        assertNotNull(headers.getValue(HttpHeaderName.DATE));
        assertTrue(Boolean.parseBoolean(headers.getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
    }



    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @Test
    public void appendBinaryDataFlush() {
        DataLakeFileAppendOptions appendOptions = new DataLakeFileAppendOptions().setFlush(true);
        Response<Void> response = fc.appendWithResponse(DATA.getDefaultBinaryData(), 0, appendOptions, null, null);
        HttpHeaders headers = response.getHeaders();

        assertEquals(202, response.getStatusCode());
        assertNotNull(headers.getValue(X_MS_REQUEST_ID));
        assertNotNull(headers.getValue(X_MS_VERSION));
        assertNotNull(headers.getValue(HttpHeaderName.DATE));
        assertTrue(Boolean.parseBoolean(headers.getValue(X_MS_REQUEST_SERVER_ENCRYPTED)));
    }

    @Test
    public void flushDataMin() {
        fc.append(DATA.getDefaultBinaryData(), 0);

        assertDoesNotThrow(() -> fc.flush(DATA.getDefaultDataSizeLong(), true));
    }

    @Test
    public void flushClose() {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        fc.create();
        fc.append(DATA.getDefaultBinaryData(), 0);

        assertDoesNotThrow(() -> fc.flushWithResponse(DATA.getDefaultDataSizeLong(), false, true, null, null, null, null));
    }

    @Test
    public void flushRetainUncommittedData() {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        fc.create();
        fc.append(DATA.getDefaultBinaryData(), 0);

        assertDoesNotThrow(() -> fc.flushWithResponse(DATA.getDefaultDataSizeLong(), true, false, null, null, null, null));
    }

    @Test
    public void flushIA() {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        fc.create();
        fc.append(DATA.getDefaultBinaryData(), 0);

        assertThrows(DataLakeStorageException.class, () -> fc.flushWithResponse(4, false, false, null, null, null, null));
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null,null", "control,disposition,encoding,language,type"})
    public void flushHeaders(String cacheControl, String contentDisposition, String contentEncoding,
        String contentLanguage, String contentType) {
        fc = dataLakeFileSystemClient.createFile(generatePathName());
        fc.append(DATA.getDefaultBinaryData(), 0);

        PathHttpHeaders headers = new PathHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType);

        fc.flushWithResponse(DATA.getDefaultDataSizeLong(), false, false, headers, null, null, null);
        Response<PathProperties> response = fc.getPropertiesWithResponse(null, null, null);

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;

        validatePathProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, null, contentType);
    }


    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void flushAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        fc = dataLakeFileSystemClient.createFile(generatePathName());
        fc.append(DATA.getDefaultBinaryData(), 0);

        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(fc, leaseID))
            .setIfMatch(setupPathMatchCondition(fc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertEquals(200, fc.flushWithResponse(DATA.getDefaultDataSizeLong(), false, false, null, drc, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void flushACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        fc = dataLakeFileSystemClient.createFile(generatePathName());
        fc.append(DATA.getDefaultBinaryData(), 0);

        setupPathLeaseCondition(fc, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(fc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);

        assertThrows(DataLakeStorageException.class,
            () -> fc.flushWithResponse(DATA.getDefaultDataSizeLong(), false, false, null, drc, null, null));
    }

    @Test
    public void flushError() {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());

        assertThrows(DataLakeStorageException.class, () -> fc.flush(1, true));
    }

    @Test
    public void flushDataOverwrite() {
        fc.append(DATA.getDefaultBinaryData(), 0);

        assertDoesNotThrow(() -> fc.flush(DATA.getDefaultDataSizeLong(), true));

        fc.append(DATA.getDefaultBinaryData(), 0);

        // Attempt to write data without overwrite enabled
        assertThrows(DataLakeStorageException.class, () -> fc.flush(DATA.getDefaultDataSizeLong(), false));
    }

    @ParameterizedTest
    @CsvSource({"file,file", "path/to]a file,path/to]a file", "path%2Fto%5Da%20file,path%2Fto%5Da%20file", "斑點,斑點",
        "%E6%96%91%E9%BB%9E,%E6%96%91%E9%BB%9E"})
    public void getFileNameAndBuildClient(String originalFileName, String finalFileName) {
        DataLakeFileClient client = dataLakeFileSystemClient.getFileClient(originalFileName);

        // Note : Here I use Path because there is a test that tests the use of a /
        assertEquals(finalFileName, client.getFilePath());
    }

    @Test
    public void builderBearerTokenValidation() {
        // Technically no additional checks need to be added to datalake builder since the corresponding blob builder fails
        String endpoint = BlobUrlParts.parse(fc.getFileUrl()).setScheme("http").toUrl().toString();

        assertThrows(IllegalArgumentException.class, () -> new DataLakePathClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)
            .buildFileClient());
    }

    @Test
    public void uploadFromFileWithMetadata() throws IOException {
        Map<String, String> metadata = Collections.singletonMap("metadata", "value");
        File file = getRandomFile(Constants.KB);
        file.deleteOnExit();
        createdFiles.add(file);

        fc.uploadFromFile(file.getPath(), null, null, metadata, null, null);

        assertEquals(metadata, fc.getProperties().getMetadata());

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        fc.read(outStream);

        TestUtils.assertArraysEqual(Files.readAllBytes(file.toPath()), outStream.toByteArray());
    }

    /*
     * Reports the number of bytes sent when uploading a file. This is different from other reporters which track the
     * number of reports as upload from file hooks into the loading data from disk data stream which is a hard-coded
     * read size.
     */
    @SuppressWarnings("deprecation")
    private static final class FileUploadReporter implements ProgressReceiver {
        private long reportedByteCount;

        @Override
        public void reportProgress(long bytesTransferred) {
            this.reportedByteCount = bytesTransferred;
        }

        long getReportedByteCount() {
            return this.reportedByteCount;
        }
    }

    @ParameterizedTest
    @MethodSource("uploadFromFileOptionsSupplier")
    public void uploadFromFileOptions(int dataSize, long singleUploadSize, Long blockSize) {
        File file = getRandomFile(dataSize);
        file.deleteOnExit();
        createdFiles.add(file);

        fc.uploadFromFile(file.toPath().toString(),
            new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(singleUploadSize), null, null, null, null);

        assertEquals(dataSize, fc.getProperties().getFileSize());
    }

    private static Stream<Arguments> uploadFromFileOptionsSupplier() {
        return Stream.of(
            // dataSize | singleUploadSize | blockSize
            Arguments.of(100, 50L, null), // Test that singleUploadSize is respected
            Arguments.of(100, 50L, 20L) // Test that blockSize is respected
        );
    }

    @ParameterizedTest
    @MethodSource("uploadFromFileOptionsSupplier")
    public void uploadFromFileWithResponse(int dataSize, long singleUploadSize, Long blockSize) {
        File file = getRandomFile(dataSize);
        file.deleteOnExit();
        createdFiles.add(file);

        Response<PathInfo> response = fc.uploadFromFileWithResponse(file.toPath().toString(),
            new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(singleUploadSize), null, null, null, null, null);

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getValue().getETag());
        assertNotNull(response.getValue().getLastModified());

        assertEquals(dataSize, fc.getProperties().getFileSize());
    }

    @Test
    public void uploadFromFileEmptyFile() {
        File file = getRandomFile(0);
        file.deleteOnExit();
        createdFiles.add(file);

        Response<PathInfo> response = fc.uploadFromFileWithResponse(file.toPath().toString(), null, null, null, null,
            null, null);
        // uploadFromFileWithResponse will return 200 for a non-empty file, but since we are uploading an empty file,
        // it will return 201 since only createWithResponse gets called
        assertEquals(201, response.getStatusCode());
        assertNotNull(response.getValue().getETag());

        assertEquals(0, fc.getProperties().getFileSize());
    }

    private static void compareListToBuffer(List<ByteBuffer> buffers, ByteBuffer result) {
        result.position(0);
        for (ByteBuffer buffer : buffers) {
            buffer.position(0);
            result.limit(result.position() + buffer.remaining());

            TestUtils.assertByteBuffersEqual(buffer, result);

            result.position(result.position() + buffer.remaining());
        }

        assertEquals(0, result.remaining());
    }

    // Reporter for testing Progress Receiver
    // Will count the number of reports that are triggered
    @SuppressWarnings("deprecation")
    private static final class Reporter implements ProgressReceiver {
        private final long blockSize;
        private long reportingCount;

        Reporter(long blockSize) {
            this.blockSize = blockSize;
        }

        @Override
        public void reportProgress(long bytesTransferred) {
            assert bytesTransferred % blockSize == 0;
            this.reportingCount += 1;
        }
    }

    @SuppressWarnings("deprecation")
    @LiveOnly
    @ParameterizedTest
    @ValueSource(ints = {11110, 2 * Constants.MB + 11})
    public void bufferedUploadSyncHandlePathingWithTransientFailure(int dataSize) {
        // This test ensures that although we no longer mark and reset the source stream for buffered upload, it still
        // supports retries in all cases for the sync client.
        DataLakeFileClient clientWithFailure = getFileClient(getDataLakeCredential(), fc.getFileUrl(),
            new TransientFailureInjectingHttpPipelinePolicy());

        byte[] data = getRandomByteArray(dataSize);
        clientWithFailure.uploadWithResponse(new FileParallelUploadOptions(new ByteArrayInputStream(data), dataSize)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong(2L * Constants.MB)
                .setBlockSizeLong(2L * Constants.MB)), null, null);

        ByteArrayOutputStream os = new ByteArrayOutputStream(dataSize);
        fc.read(os);

        TestUtils.assertArraysEqual(data, os.toByteArray());
    }

    @Test
    public void bufferedUploadNonMarkableStream() throws FileNotFoundException {
        File file = getRandomFile(10);
        file.deleteOnExit();
        createdFiles.add(file);

        FileInputStream fileStream = new FileInputStream(file);
        File outFile = getRandomFile(10);
        outFile.deleteOnExit();
        createdFiles.add(outFile);

        fc.upload(fileStream, file.length(), true);

        fc.readToFile(outFile.toPath().toString(), true);
        compareFiles(file, outFile, 0, file.length());
    }

    @Test
    public void uploadInputStreamNoLength() {
        assertDoesNotThrow(() ->
            fc.uploadWithResponse(new FileParallelUploadOptions(DATA.getDefaultInputStream()), null, null));

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        fc.read(os);

        TestUtils.assertArraysEqual(DATA.getDefaultBytes(), os.toByteArray());
    }

    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @MethodSource("uploadInputStreamBadLengthSupplier")
    public void uploadInputStreamBadLength(long length) {
        assertThrows(Exception.class, () -> fc.uploadWithResponse(
            new FileParallelUploadOptions(DATA.getDefaultInputStream(), length), null, null));
    }

    private static Stream<Long> uploadInputStreamBadLengthSupplier() {
        return Stream.of(0L, -100L, DATA.getDefaultDataSizeLong() - 1, DATA.getDefaultDataSizeLong() + 1);
    }

    @Test
    public void uploadSuccessfulRetry() {
        DataLakeFileClient clientWithFailure = getFileClient(getDataLakeCredential(), fc.getFileUrl(),
            new TransientFailureInjectingHttpPipelinePolicy());

        assertDoesNotThrow(() -> clientWithFailure.uploadWithResponse(
            new FileParallelUploadOptions(DATA.getDefaultInputStream()), null, null));

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        fc.read(os);

        TestUtils.assertArraysEqual(DATA.getDefaultBytes(), os.toByteArray());
    }

    @Test
    public void uploadBinaryData() {
        DataLakeFileClient client = getFileClient(getDataLakeCredential(), fc.getFileUrl());

        assertDoesNotThrow(
            () -> client.uploadWithResponse(new FileParallelUploadOptions(DATA.getDefaultBinaryData()), null, null));

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        fc.read(os);

        TestUtils.assertArraysEqual(DATA.getDefaultBytes(), os.toByteArray());
    }

    @Test
    public void uploadBinaryDataOverwrite() {
        DataLakeFileClient client = getFileClient(getDataLakeCredential(), fc.getFileUrl());

        assertDoesNotThrow(() -> client.upload(DATA.getDefaultBinaryData(), true));

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        fc.read(os);

        TestUtils.assertArraysEqual(DATA.getDefaultBytes(), os.toByteArray());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2021-04-10")
    @Test
    public void uploadEncryptionContext() {
        String encryptionContext = "encryptionContext";
        FileParallelUploadOptions options = new FileParallelUploadOptions(DATA.getDefaultInputStream())
            .setEncryptionContext(encryptionContext);

        fc.uploadWithResponse(options, null, Context.NONE);

        assertEquals(encryptionContext, fc.getProperties().getEncryptionContext());
    }

    /* Quick Query Tests. */

    // Generates and uploads a CSV file
    private void uploadCsv(FileQueryDelimitedSerialization s, int numCopies) {
        String columnSeparator = Character.toString(s.getColumnSeparator());
        String header = "rn1" + columnSeparator + "rn2" + columnSeparator + "rn3" + columnSeparator + "rn4"
            + s.getRecordSeparator();
        byte[] headers = header.getBytes();

        String csv = "100" + columnSeparator + "200" + columnSeparator + "300" + columnSeparator + "400"
            + s.getRecordSeparator() + "300" + columnSeparator + "400" + columnSeparator + "500" + columnSeparator
            + "600" + s.getRecordSeparator();

        byte[] csvData = csv.getBytes();

        int headerLength = s.isHeadersPresent() ? headers.length : 0;
        byte[] data = new byte[headerLength + csvData.length * numCopies];
        if (s.isHeadersPresent()) {
            System.arraycopy(headers, 0, data, 0, headers.length);
        }

        for (int i = 0; i < numCopies; i++) {
            int o = i * csvData.length + headerLength;
            System.arraycopy(csvData, 0, data, o, csvData.length);
        }

        fc.create(true);
        fc.append(BinaryData.fromBytes(data), 0);
        fc.flush(data.length, true);
    }


    private void uploadSmallJson(int numCopies) {
        StringBuilder b = new StringBuilder();
        b.append("{\n");
        for (int i = 0; i < numCopies; i++) {
            b.append(String.format("\t\"name%d\": \"owner%d\",\n", i, i));
        }
        b.append('}');

        fc.create(true);
        fc.append(BinaryData.fromString(b.toString()), 0);
        fc.flush(b.length(), true);
    }

    @LiveOnly
    @Test
    public void uploadAndDownloadAndUploadAgain() {
        byte[] randomData = getRandomByteArray(20 * Constants.MB);
        ByteArrayInputStream input = new ByteArrayInputStream(randomData);

        String pathName = generatePathName();
        DataLakeFileClient fileClient = dataLakeFileSystemClient.getFileClient(pathName);
        fileClient.createIfNotExists();

        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions()
            .setBlockSizeLong((long) Constants.MB)
            .setMaxSingleUploadSizeLong(2L * Constants.MB)
            .setMaxConcurrency(5);
        FileParallelUploadOptions parallelUploadOptions = new FileParallelUploadOptions(input)
            .setParallelTransferOptions(parallelTransferOptions);

        fileClient.uploadWithResponse(parallelUploadOptions, null, null);

        DataLakeFileOpenInputStreamResult inputStreamResult = fileClient.openInputStream();

        // Upload the downloaded content to a different location
        String pathName2 = generatePathName();

        parallelUploadOptions = new FileParallelUploadOptions(inputStreamResult.getInputStream())
            .setParallelTransferOptions(parallelTransferOptions);

        DataLakeFileClient fileClient2 = dataLakeFileSystemClient.getFileClient(pathName2);
        fileClient2.uploadWithResponse(parallelUploadOptions, null, null);
    }

    private static byte[] readFromInputStream(InputStream stream, int numBytesToRead) {
        byte[] queryData = new byte[numBytesToRead];

        int length = numBytesToRead;
        int bytesRead;

        try {
            while (length > 0 && (bytesRead = stream.read(queryData, numBytesToRead - length, length)) != -1) {
                length -= bytesRead;
            }

            stream.close();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return queryData;
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @ValueSource(ints = {
        1, // 32 bytes
        32, // 1 KB
        256, // 8 KB
        400, // 12 ish KB
        4000 // 125 KB
    })
    public void queryMin(int numCopies) {
        FileQueryDelimitedSerialization ser = new FileQueryDelimitedSerialization()
            .setRecordSeparator('\n')
            .setColumnSeparator(',')
            .setEscapeChar('\0')
            .setFieldQuote('\0')
            .setHeadersPresent(false);
        uploadCsv(ser, numCopies);
        String expression = "SELECT * from BlobStorage";

        ByteArrayOutputStream downloadData = new ByteArrayOutputStream();
        fc.read(downloadData);
        byte[] downloadedData = downloadData.toByteArray();

        liveTestScenarioWithRetry(() -> {
            try (InputStream qqStream = fc.openQueryInputStream(expression)) {
                byte[] queryData = assertDoesNotThrow(() -> readFromInputStream(qqStream, downloadedData.length));
                TestUtils.assertArraysEqual(downloadedData, queryData);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            assertDoesNotThrow(() -> fc.query(os, expression));
            TestUtils.assertArraysEqual(downloadedData, os.toByteArray());
        });
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("queryCsvSerializationSeparatorSupplier")
    public void queryCsvSerializationSeparator(char recordSeparator, char columnSeparator, boolean headersPresentIn,
        boolean headersPresentOut) {
        FileQueryDelimitedSerialization serIn = new FileQueryDelimitedSerialization()
            .setRecordSeparator(recordSeparator)
            .setColumnSeparator(columnSeparator)
            .setEscapeChar('\0')
            .setFieldQuote('\0')
            .setHeadersPresent(headersPresentIn);
        FileQueryDelimitedSerialization serOut = new FileQueryDelimitedSerialization()
            .setRecordSeparator(recordSeparator)
            .setColumnSeparator(columnSeparator)
            .setEscapeChar('\0')
            .setFieldQuote('\0')
            .setHeadersPresent(headersPresentOut);
        uploadCsv(serIn, 32);
        String expression = "SELECT * from BlobStorage";

        ByteArrayOutputStream downloadData = new ByteArrayOutputStream();
        fc.read(downloadData);
        byte[] downloadedData = downloadData.toByteArray();

        liveTestScenarioWithRetry(() -> {
            InputStream qqStream = fc.openQueryInputStreamWithResponse(
                new FileQueryOptions(expression).setInputSerialization(serIn).setOutputSerialization(serOut))
                .getValue();
            byte[] queryData = assertDoesNotThrow(() -> readFromInputStream(qqStream, downloadedData.length));

            if (headersPresentIn && !headersPresentOut) {
                /* Account for 16 bytes of header. */
                TestUtils.assertArraysEqual(downloadedData, 16, queryData, 0, downloadedData.length - 16);

                for (int k = downloadedData.length - 16; k < downloadedData.length; k++) {
                    assertEquals(0, queryData[k]);
                }
            } else {
                TestUtils.assertArraysEqual(downloadedData, queryData);
            }

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            assertDoesNotThrow(() -> fc.queryWithResponse(new FileQueryOptions(expression, os)
                .setInputSerialization(serIn).setOutputSerialization(serOut), null, null));
            byte[] osData = os.toByteArray();

            if (headersPresentIn && !headersPresentOut) {
                assertEquals(downloadedData.length - 16, osData.length);

                /* Account for 16 bytes of header. */
                TestUtils.assertArraysEqual(downloadedData, 16, osData, 0, downloadedData.length - 16);
            } else {
                TestUtils.assertArraysEqual(downloadedData, osData);
            }
        });
    }

    private static Stream<Arguments> queryCsvSerializationSeparatorSupplier() {
        return Stream.of(
            // recordSeparator | columnSeparator | headersPresentIn | headersPresentOut
            Arguments.of('\n', ',', false, false), // Default.
            Arguments.of('\n', ',', true, true), // Headers.
            Arguments.of('\n', ',', true, false), // Headers.
            Arguments.of('\t', ',', false, false), // Record separator.
            Arguments.of('\r', ',', false, false),
            Arguments.of('<', ',', false, false),
            Arguments.of('>', ',', false, false),
            Arguments.of('&', ',', false, false),
            Arguments.of('\\', ',', false, false),
            Arguments.of(',', '.', false, false), // Column separator.
//            Arguments.of(',', '\n', false, false), // Keep getting a qq error: Field delimiter and record delimiter must be different characters.
            Arguments.of(',', ';', false, false),
            Arguments.of('\n', '\t', false, false),
//            Arguments.of('\n', '\r', false, false), // Keep getting a qq error: Field delimiter and record delimiter must be different characters.
            Arguments.of('\n', '<', false, false),
            Arguments.of('\n', '>', false, false),
            Arguments.of('\n', '&', false, false),
            Arguments.of('\n', '\\', false, false)
        );
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @Test
    public void queryCsvSerializationEscapeAndFieldQuote() {
        FileQueryDelimitedSerialization ser = new FileQueryDelimitedSerialization()
            .setRecordSeparator('\n')
            .setColumnSeparator(',')
            .setEscapeChar('\\') /* Escape set here. */
            .setFieldQuote('"')  /* Field quote set here*/
            .setHeadersPresent(false);
        uploadCsv(ser, 32);

        String expression = "SELECT * from BlobStorage";

        ByteArrayOutputStream downloadData = new ByteArrayOutputStream();
        fc.read(downloadData);
        byte[] downloadedData = downloadData.toByteArray();

        liveTestScenarioWithRetry(() -> {
            InputStream qqStream = fc.openQueryInputStreamWithResponse(new FileQueryOptions(expression).setInputSerialization(ser).setOutputSerialization(ser))
                .getValue();
            byte[] queryData = assertDoesNotThrow(() -> readFromInputStream(qqStream, downloadedData.length));
            TestUtils.assertArraysEqual(downloadedData, queryData);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            assertDoesNotThrow(() -> fc.queryWithResponse(new FileQueryOptions(expression, os)
                .setInputSerialization(ser).setOutputSerialization(ser), null, null));
            TestUtils.assertArraysEqual(downloadedData, os.toByteArray());
        });
    }

    /* Note: Input delimited tested everywhere else. */
    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("queryInputJsonSupplier")
    public void queryInputJson(int numCopies, char recordSeparator) {
        FileQueryJsonSerialization ser = new FileQueryJsonSerialization()
            .setRecordSeparator(recordSeparator);
        uploadSmallJson(numCopies);
        String expression = "SELECT * from BlobStorage";

        ByteArrayOutputStream downloadData = new ByteArrayOutputStream();
        fc.read(downloadData);
        downloadData.write(10); /* writing extra new line */
        byte[] downloadedData = downloadData.toByteArray();

        liveTestScenarioWithRetry(() -> {
            FileQueryOptions optionsIs = new FileQueryOptions(expression).setInputSerialization(ser).setOutputSerialization(ser);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            FileQueryOptions optionsOs = new FileQueryOptions(expression, os).setInputSerialization(ser).setOutputSerialization(ser);

            InputStream qqStream = fc.openQueryInputStreamWithResponse(optionsIs).getValue();
            byte[] queryData = assertDoesNotThrow(() -> readFromInputStream(qqStream, downloadedData.length));
            TestUtils.assertArraysEqual(downloadedData, queryData);

            assertDoesNotThrow(() -> fc.queryWithResponse(optionsOs, null, null));
            TestUtils.assertArraysEqual(downloadedData, os.toByteArray());
        });
    }

    private static Stream<Arguments> queryInputJsonSupplier() {
        return Stream.of(
            // numCopies | recordSeparator
            Arguments.of(0, '\n'),
            Arguments.of(10, '\n'),
            Arguments.of(100, '\n'),
            Arguments.of(1000, '\n')
        );
    }

    @SuppressWarnings("DataFlowIssue")
    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-10-02")
    @Test
    public void queryInputParquet() {
        String fileName = "parquet.parquet";
        ClassLoader classLoader = getClass().getClassLoader();
        File f = new File(classLoader.getResource(fileName).getFile());
        FileQueryParquetSerialization ser = new FileQueryParquetSerialization();
        fc.uploadFromFile(f.getAbsolutePath(), true);
        byte[] expectedData = "0,mdifjt55.ea3,mdifjt55.ea3\n".getBytes();

        String expression = "select * from blobstorage where id < 1;";

        liveTestScenarioWithRetry(() -> {
            FileQueryOptions optionsIs = new FileQueryOptions(expression).setInputSerialization(ser);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            FileQueryOptions optionsOs = new FileQueryOptions(expression, os).setInputSerialization(ser);

            InputStream qqStream = fc.openQueryInputStreamWithResponse(optionsIs).getValue();
            byte[] queryData = assertDoesNotThrow(() -> readFromInputStream(qqStream, expectedData.length));
            TestUtils.assertArraysEqual(expectedData, queryData);

            assertDoesNotThrow(() -> fc.queryWithResponse(optionsOs, null, null));
            TestUtils.assertArraysEqual(expectedData, os.toByteArray());
        });
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @Test
    public void queryInputCsvOutputJson() {
        liveTestScenarioWithRetry(() -> {
            FileQueryDelimitedSerialization inSer = new FileQueryDelimitedSerialization()
                .setRecordSeparator('\n')
                .setColumnSeparator(',')
                .setEscapeChar('\0')
                .setFieldQuote('\0')
                .setHeadersPresent(false);
            uploadCsv(inSer, 1);
            FileQueryJsonSerialization outSer = new FileQueryJsonSerialization().setRecordSeparator('\n');
            String expression = "SELECT * from BlobStorage";
            byte[] expectedData = "{\"_1\":\"100\",\"_2\":\"200\",\"_3\":\"300\",\"_4\":\"400\"}".getBytes();
            FileQueryOptions optionsIs = new FileQueryOptions(expression).setInputSerialization(inSer)
                .setOutputSerialization(outSer);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            FileQueryOptions optionsOs = new FileQueryOptions(expression, os).setInputSerialization(inSer)
                .setOutputSerialization(outSer);

            InputStream qqStream = fc.openQueryInputStreamWithResponse(optionsIs).getValue();
            byte[] queryData = assertDoesNotThrow(() -> readFromInputStream(qqStream, expectedData.length));
            TestUtils.assertArraysEqual(expectedData, 0, queryData, 0, expectedData.length);

            assertDoesNotThrow(() -> fc.queryWithResponse(optionsOs, null, null));
            TestUtils.assertArraysEqual(expectedData, 0, os.toByteArray(), 0, expectedData.length);
        });
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @Test
    public void queryInputJsonOutputCsv() {
        liveTestScenarioWithRetry(() -> {
            FileQueryJsonSerialization inSer = new FileQueryJsonSerialization().setRecordSeparator('\n');
            uploadSmallJson(2);

            FileQueryDelimitedSerialization outSer = new FileQueryDelimitedSerialization()
                .setRecordSeparator('\n')
                .setColumnSeparator(',')
                .setEscapeChar('\0')
                .setFieldQuote('\0')
                .setHeadersPresent(false);
            String expression = "SELECT * from BlobStorage";
            byte[] expectedData = "owner0,owner1\n".getBytes();
            FileQueryOptions optionsIs = new FileQueryOptions(expression).setInputSerialization(inSer)
                .setOutputSerialization(outSer);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            FileQueryOptions optionsOs = new FileQueryOptions(expression, os).setInputSerialization(inSer)
                .setOutputSerialization(outSer);

            InputStream qqStream = fc.openQueryInputStreamWithResponse(optionsIs).getValue();
            byte[] queryData = assertDoesNotThrow(() -> readFromInputStream(qqStream, expectedData.length));
            TestUtils.assertArraysEqual(expectedData, queryData);

            assertDoesNotThrow(() -> fc.queryWithResponse(optionsOs, null, null));
            TestUtils.assertArraysEqual(expectedData, os.toByteArray());
        });
    }

    @SuppressWarnings("resource")
    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @Test
    public void queryInputCsvOutputArrow() {
        FileQueryDelimitedSerialization inSer = new FileQueryDelimitedSerialization()
            .setRecordSeparator('\n')
            .setColumnSeparator(',')
            .setEscapeChar('\0')
            .setFieldQuote('\0')
            .setHeadersPresent(false);
        uploadCsv(inSer, 32);
        List<FileQueryArrowField> schema = Collections.singletonList(
            new FileQueryArrowField(FileQueryArrowFieldType.DECIMAL).setName("Name").setPrecision(4).setScale(2));
        FileQueryArrowSerialization outSer = new FileQueryArrowSerialization().setSchema(schema);
        String expression = "SELECT _2 from BlobStorage WHERE _1 > 250;";

        liveTestScenarioWithRetry(() -> {
            OutputStream os = new ByteArrayOutputStream();
            FileQueryOptions options = new FileQueryOptions(expression, os).setOutputSerialization(outSer);

            assertDoesNotThrow(() -> fc.openQueryInputStreamWithResponse(options).getValue());

            assertDoesNotThrow(() -> fc.queryWithResponse(options, null, null));
        });
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @Test
    public void queryNonFatalError() {
        FileQueryDelimitedSerialization base = new FileQueryDelimitedSerialization()
            .setRecordSeparator('\n')
            .setEscapeChar('\0')
            .setFieldQuote('\0')
            .setHeadersPresent(false);
        uploadCsv(base.setColumnSeparator('.'), 32);
        String expression = "SELECT _1 from BlobStorage WHERE _2 > 250";

        liveTestScenarioWithRetry(() -> {
            MockErrorReceiver receiver1 = new MockErrorReceiver("InvalidColumnOrdinal");
            InputStream qqStream = fc.openQueryInputStreamWithResponse(new FileQueryOptions(expression)
                .setInputSerialization(base.setColumnSeparator(','))
                .setOutputSerialization(base.setColumnSeparator(','))
                .setErrorConsumer(receiver1)).getValue();

            assertDoesNotThrow(() -> readFromInputStream(qqStream, Constants.KB));
            assertTrue(receiver1.numErrors > 0);

            MockErrorReceiver receiver2 = new MockErrorReceiver("InvalidColumnOrdinal");

            assertDoesNotThrow(() -> fc.queryWithResponse(new FileQueryOptions(expression, new ByteArrayOutputStream())
                .setInputSerialization(base.setColumnSeparator(','))
                .setOutputSerialization(base.setColumnSeparator(','))
                .setErrorConsumer(receiver2), null, null));
            assertTrue(receiver2.numErrors > 0);
        });
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @Test
    public void queryFatalError() {
        FileQueryDelimitedSerialization base = new FileQueryDelimitedSerialization()
            .setRecordSeparator('\n')
            .setEscapeChar('\0')
            .setFieldQuote('\0')
            .setHeadersPresent(true);
        uploadCsv(base.setColumnSeparator('.'), 32);
        String expression = "SELECT * from BlobStorage";

        liveTestScenarioWithRetry(() -> {
            assertThrows(UncheckedIOException.class, () -> fc.openQueryInputStreamWithResponse(
                new FileQueryOptions(expression).setInputSerialization(new FileQueryJsonSerialization())).getValue());

            assertThrows(RuntimeException.class, () -> fc.queryWithResponse(new FileQueryOptions(expression,
                new ByteArrayOutputStream()).setInputSerialization(new FileQueryJsonSerialization()), null, null));
        });
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @Test
    public void queryProgressReceiver() {
        FileQueryDelimitedSerialization base = new FileQueryDelimitedSerialization()
            .setRecordSeparator('\n')
            .setEscapeChar('\0')
            .setFieldQuote('\0')
            .setHeadersPresent(false);

        uploadCsv(base.setColumnSeparator('.'), 32);

        long sizeofBlobToRead = fc.getProperties().getFileSize();
        String expression = "SELECT * from BlobStorage";

        liveTestScenarioWithRetry(() -> {
            MockProgressReceiver mockReceiver = new MockProgressReceiver();
            FileQueryOptions options = new FileQueryOptions(expression).setProgressConsumer(mockReceiver);

            InputStream qqStream = fc.openQueryInputStreamWithResponse(options).getValue();

            // The Avro stream has the following pattern
            // (data record -> progress record) -> end record
            //
            // 1KB of data will only come back as a single data record.
            //
            // Pretend to read more data because the input stream will not parse records following the data record if it
            // doesn't need to.
            readFromInputStream(qqStream, Constants.MB);

            // At least the size of blob to read will be in the progress list
            assertTrue(mockReceiver.progressList.contains(sizeofBlobToRead));


            mockReceiver = new com.azure.storage.file.datalake.FileApiTest.MockProgressReceiver();
            options = new FileQueryOptions(expression, new ByteArrayOutputStream()).setProgressConsumer(mockReceiver);
            fc.queryWithResponse(options, null, null);

            // At least the size of blob to read will be in the progress list
            assertTrue(mockReceiver.progressList.contains(sizeofBlobToRead));
        });
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @LiveOnly // Large amount of data.
    @Test
    public void queryMultipleRecordsWithProgressReceiver() {
        FileQueryDelimitedSerialization ser = new FileQueryDelimitedSerialization()
            .setRecordSeparator('\n')
            .setColumnSeparator(',')
            .setEscapeChar('\0')
            .setFieldQuote('\0')
            .setHeadersPresent(false);
        String expression = "SELECT * from BlobStorage";

        uploadCsv(ser, 512000);

        liveTestScenarioWithRetry(() -> {
            MockProgressReceiver mockReceiver = new MockProgressReceiver();
            FileQueryOptions options = new FileQueryOptions(expression).setProgressConsumer(mockReceiver);

            InputStream qqStream = fc.openQueryInputStreamWithResponse(options).getValue();

            // The Avro stream has the following pattern
            // (data record -> progress record) -> end record
            //
            // 1KB of data will only come back as a single data record.
            //
            // Pretend to read more data because the input stream will not parse records following the data record if it
            // doesn't need to.
            readFromInputStream(qqStream, 16 * Constants.MB);

            long temp = 0;
            // Make sure they're all increasingly bigger
            for (long progress : mockReceiver.progressList) {
                assertTrue(progress >= temp, "Expected progress to be greater than or equal to previous progress.");
                temp = progress;
            }

            mockReceiver = new com.azure.storage.file.datalake.FileApiTest.MockProgressReceiver();
            temp = 0;
            options = new FileQueryOptions(expression, new ByteArrayOutputStream()).setProgressConsumer(mockReceiver);
            fc.queryWithResponse(options, null, null);

            // Make sure they're all increasingly bigger
            for (long progress : mockReceiver.progressList) {
                assertTrue(progress >= temp, "Expected progress to be greater than or equal to previous progress.");
                temp = progress;
            }
        });
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @CsvSource(value = {"true,false", "false,true"})
    public void queryInputOutputIA(boolean input, boolean output) {
        /* Mock random impl of QQ Serialization*/
        FileQuerySerialization ser = new RandomOtherSerialization();

        FileQuerySerialization inSer = input ? ser : null;
        FileQuerySerialization outSer = output ? ser : null;
        String expression = "SELECT * from BlobStorage";

        liveTestScenarioWithRetry(() -> {
            assertThrows(IllegalArgumentException.class, () -> fc.openQueryInputStreamWithResponse(
                new FileQueryOptions(expression)
                    .setInputSerialization(inSer)
                    .setOutputSerialization(outSer)).getValue());  /* Don't need to call read. */

            assertThrows(IllegalArgumentException.class, () -> fc.queryWithResponse(
                new FileQueryOptions(expression, new ByteArrayOutputStream())
                    .setInputSerialization(inSer)
                    .setOutputSerialization(outSer), null, null));
        });
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @Test
    public void queryArrowInputIA() {
        FileQueryArrowSerialization inSer = new FileQueryArrowSerialization();
        String expression = "SELECT * from BlobStorage";

        liveTestScenarioWithRetry(() -> {
            assertThrows(IllegalArgumentException.class, () -> fc
                .openQueryInputStreamWithResponse(new FileQueryOptions(expression).setInputSerialization(inSer))
                .getValue());  /* Don't need to call read. */

            assertThrows(IllegalArgumentException.class, () -> fc.queryWithResponse(
                new FileQueryOptions(expression, new ByteArrayOutputStream()).setInputSerialization(inSer), null,
                null));
        });
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2020-10-02")
    @Test
    public void queryParquetOutputIA() {
        FileQueryParquetSerialization outSer = new FileQueryParquetSerialization();
        String expression = "SELECT * from BlobStorage";

        liveTestScenarioWithRetry(() -> {
            assertThrows(IllegalArgumentException.class, () -> fc
                .openQueryInputStreamWithResponse(new FileQueryOptions(expression).setOutputSerialization(outSer))
                .getValue());  /* Don't need to call read. */

            assertThrows(IllegalArgumentException.class, () -> fc.queryWithResponse(
                new FileQueryOptions(expression, new ByteArrayOutputStream()).setOutputSerialization(outSer), null,
                null));
        });
    }

    @SuppressWarnings("resource")
    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @Test
    public void queryError() {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());

        liveTestScenarioWithRetry(() -> {
            assertThrows(DataLakeStorageException.class,
                () -> fc.openQueryInputStream("SELECT * from BlobStorage")); /* Don't need to call read. */

            assertThrows(DataLakeStorageException.class,
                () -> fc.query(new ByteArrayOutputStream(), "SELECT * from BlobStorage"));

        });
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void queryAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        DataLakeRequestConditions bac = new DataLakeRequestConditions()
            .setLeaseId(setupPathLeaseCondition(fc, leaseID))
            .setIfMatch(setupPathMatchCondition(fc, match))
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);
        String expression = "SELECT * from BlobStorage";

        liveTestScenarioWithRetry(() -> {
            assertDoesNotThrow(() -> {
                InputStream stream = fc.openQueryInputStreamWithResponse(
                    new FileQueryOptions(expression).setRequestConditions(bac)).getValue();
                stream.read();
                stream.close();
            });

            assertDoesNotThrow(() -> fc.queryWithResponse(new FileQueryOptions(expression, new ByteArrayOutputStream())
                .setRequestConditions(bac), null, null));
        });
    }

    private void liveTestScenarioWithRetry(Runnable runnable) {
        if (!interceptorManager.isLiveMode()) {
            runnable.run();
            return;
        }

        int retry = 0;
        while (retry < 5) {
            try {
                runnable.run();
                break;
            } catch (Exception ex) {
                retry++;
                sleepIfRunningAgainstService(5000);
            }
        }
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void queryACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        setupPathLeaseCondition(fc, leaseID);
        DataLakeRequestConditions bac = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(setupPathMatchCondition(fc, noneMatch))
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified);
        String expression = "SELECT * from BlobStorage";

        assertThrows(DataLakeStorageException.class, () -> fc.openQueryInputStreamWithResponse(
            new FileQueryOptions(expression).setRequestConditions(bac)).getValue()); /* Don't need to call read. */

        assertThrows(DataLakeStorageException.class, () -> fc.queryWithResponse(
            new FileQueryOptions(expression, new ByteArrayOutputStream()).setRequestConditions(bac), null, null));
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @ParameterizedTest
    @MethodSource("scheduleDeletionSupplier")
    public void scheduleDeletion(FileScheduleDeletionOptions fileScheduleDeletionOptions, boolean hasExpiry) {
        DataLakeFileClient fileClient = dataLakeFileSystemClient.getFileClient(generatePathName());
        fileClient.create();

        fileClient.scheduleDeletionWithResponse(fileScheduleDeletionOptions, null, null);

        assertEquals(hasExpiry, fileClient.getProperties().getExpiresOn() != null);
    }

    private static Stream<Arguments> scheduleDeletionSupplier() {
        return Stream.of(
            // fileScheduleDeletionOptions | hasExpiry
            Arguments.of(new FileScheduleDeletionOptions(Duration.ofDays(1), FileExpirationOffset.CREATION_TIME), true),
            Arguments.of(new FileScheduleDeletionOptions(Duration.ofDays(1), FileExpirationOffset.NOW), true),
            Arguments.of(new FileScheduleDeletionOptions(), false),
            Arguments.of(null, false)
        );
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2019-12-12")
    @Test
    public void scheduleDeletionTime() {
        OffsetDateTime now = testResourceNamer.now();
        FileScheduleDeletionOptions fileScheduleDeletionOptions = new FileScheduleDeletionOptions(now.plusDays(1));
        DataLakeFileClient fileClient = dataLakeFileSystemClient.getFileClient(generatePathName());
        fileClient.create();

        fileClient.scheduleDeletionWithResponse(fileScheduleDeletionOptions, null, null);

        assertEquals(now.plusDays(1).truncatedTo(ChronoUnit.SECONDS), fileClient.getProperties().getExpiresOn());
    }

    @Test
    public void scheduleDeletionError() {
        FileScheduleDeletionOptions fileScheduleDeletionOptions = new FileScheduleDeletionOptions(testResourceNamer.now().plusDays(1));
        DataLakeFileClient fileClient = dataLakeFileSystemClient.getFileClient(generatePathName());

        assertThrows(DataLakeStorageException.class,
            () -> fileClient.scheduleDeletionWithResponse(fileScheduleDeletionOptions, null, null));
    }

    static class MockProgressReceiver implements Consumer<FileQueryProgress> {
        List<Long> progressList = new ArrayList<>();

        @Override
        public void accept(FileQueryProgress progress) {
            progressList.add(progress.getBytesScanned());
        }
    }

    static class MockErrorReceiver implements Consumer<FileQueryError> {
        String expectedType;
        int numErrors;

        MockErrorReceiver(String expectedType) {
            this.expectedType = expectedType;
            this.numErrors = 0;
        }

        @Override
        public void accept(FileQueryError error) {
            assertFalse(error.isFatal());
            assertEquals(expectedType, error.getName());
            numErrors++;
        }
    }

    private static final class RandomOtherSerialization implements FileQuerySerialization {
    }

    @Test
    public void uploadInputStreamOverwriteFails() {
        assertThrows(DataLakeStorageException.class, () -> fc.upload(DATA.getDefaultBinaryData()));
    }

    @Test
    public void uploadInputStreamOverwrite() {
        byte[] randomData = getRandomByteArray(Constants.KB);
        ByteArrayInputStream input = new ByteArrayInputStream(randomData);

        fc.upload(input, Constants.KB, true);

        ByteArrayOutputStream stream = new ByteArrayOutputStream(Constants.KB);
        fc.read(stream);

        TestUtils.assertArraysEqual(randomData, stream.toByteArray());
    }

    // Tests an issue found where buffered upload would not deeply copy buffers while determining what upload path to take.
    @ParameterizedTest
    @MethodSource("uploadInputStreamSingleUploadSupplier")
    public void uploadInputStreamSingleUpload() {
        byte[] randomData = getRandomByteArray(20 * Constants.KB);
        ByteArrayInputStream input = new ByteArrayInputStream(randomData);

        fc.upload(input, 20 * Constants.KB, true);

        ByteArrayOutputStream stream = new ByteArrayOutputStream(20 * Constants.KB);
        fc.read(stream);

        TestUtils.assertArraysEqual(randomData, stream.toByteArray());
    }

    private static Stream<Integer> uploadInputStreamSingleUploadSupplier() {
        return Stream.of(
            // size
            Constants.KB, // Less than copyToOutputStream buffer size, Less than maxSingleUploadSize
            8 * Constants.KB, // Equal to copyToOutputStream buffer size, Less than maxSingleUploadSize
            20 * Constants.KB // Greater than copyToOutputStream buffer size, Less than maxSingleUploadSize
        );
    }

    @SuppressWarnings("deprecation")
    @LiveOnly /* Flaky in playback. */
    @Test
    public void uploadInputStreamLargeData() {
        ByteArrayInputStream input = new ByteArrayInputStream(getRandomByteArray(20 * Constants.MB));
        ParallelTransferOptions pto = new ParallelTransferOptions().setMaxSingleUploadSizeLong((long) Constants.MB);

        // Uses blob output stream under the hood.
        assertDoesNotThrow(() -> fc.uploadWithResponse(new FileParallelUploadOptions(input, 20 * Constants.MB)
            .setParallelTransferOptions(pto), null, null));
    }

    @Test
    public void uploadIncorrectSize() {
        assertThrows(IllegalStateException.class,
            () -> fc.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong() - 1, true));

        assertThrows(IllegalStateException.class,
            () -> fc.upload(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong() + 1, true));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void uploadReturnValue() {
        assertNotNull(fc.uploadWithResponse(
            new FileParallelUploadOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong()), null, null)
            .getValue().getETag());
    }

    // Reading from recordings will not allow for the timing of the test to work correctly.
    @SuppressWarnings("deprecation")
    @LiveOnly
    @Test
    public void uploadTimeout() {
        int size = 1024;
        byte[] randomData = getRandomByteArray(size);
        InputStream input = new ByteArrayInputStream(randomData);

        assertThrows(IllegalStateException.class,
            () -> fc.uploadWithResponse(new FileParallelUploadOptions(input, size), Duration.ofNanos(5L), null));
    }

    // This tests the policy is in the right place because if it were added per retry, it would be after the credentials
    // and auth would fail because we changed a signed header.
    @Test
    public void perCallPolicy() {
        DataLakeFileClient fileClient = getPathClientBuilder(getDataLakeCredential(), fc.getFileUrl())
            .addPolicy(getPerCallVersionPolicy())
            .buildFileClient();

        // blob endpoint
        assertEquals("2019-02-02", fileClient.getPropertiesWithResponse(null, null, null).getHeaders()
            .getValue(X_MS_VERSION));

        // dfs endpoint
        assertEquals("2019-02-02", fileClient.getAccessControlWithResponse(false, null, null, null).getHeaders()
            .getValue(X_MS_VERSION));
    }

    @Test
    public void defaultAudience() {
        DataLakeFileClient aadFileClient = getPathClientBuilderWithTokenCredential(
            dataLakeFileSystemClient.getFileSystemUrl(), fc.getFilePath())
            .fileSystemName(dataLakeFileSystemClient.getFileSystemName())
            .audience(null) // should default to "https://storage.azure.com/"
            .buildFileClient();

        assertTrue(aadFileClient.exists());
    }


    @Test
    public void storageAccountAudience() {
        DataLakeFileClient aadFileClient = getPathClientBuilderWithTokenCredential(
            ENVIRONMENT.getDataLakeAccount().getDataLakeEndpoint(), fc.getFilePath())
            .fileSystemName(dataLakeFileSystemClient.getFileSystemName())
            .audience(DataLakeAudience.createDataLakeServiceAccountAudience(dataLakeFileSystemClient.getAccountName()))
            .buildFileClient();

        assertTrue(aadFileClient.exists());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2024-08-04")
    @LiveOnly
    @Test
    /* This test tests if the bearer challenge is working properly. A bad audience is passed in, the service returns
    the default audience, and the request gets retried with this default audience, making the call function as expected.
     */
    public void audienceErrorBearerChallengeRetry() {
        DataLakeFileClient aadFileClient = getPathClientBuilderWithTokenCredential(
            ENVIRONMENT.getDataLakeAccount().getDataLakeEndpoint(), fc.getFilePath())
            .fileSystemName(dataLakeFileSystemClient.getFileSystemName())
            .audience(DataLakeAudience.createDataLakeServiceAccountAudience("badAudience"))
            .buildFileClient();

        assertTrue(aadFileClient.exists());
    }

    @Test
    public void audienceFromString() {
        String url = String.format("https://%s.blob.core.windows.net/", dataLakeFileSystemClient.getAccountName());
        DataLakeAudience audience = DataLakeAudience.fromString(url);

        DataLakeFileClient aadFileClient = getPathClientBuilderWithTokenCredential(
            ENVIRONMENT.getDataLakeAccount().getDataLakeEndpoint(), fc.getFilePath())
            .fileSystemName(dataLakeFileSystemClient.getFileSystemName())
            .audience(audience)
            .buildFileClient();

        assertTrue(aadFileClient.exists());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2024-05-04")
    @Test
    public void aclHeaderTests() {
        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());
        dataLakeFileSystemClient.create();
        dataLakeFileSystemClient.getDirectoryClient(generatePathName()).create();
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());

        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setAccessControlList(PATH_ACCESS_CONTROL_ENTRIES);
        fc.createWithResponse(options, null, Context.NONE);

        //getProperties
        PathProperties getPropertiesResponse = fc.getProperties();
        assertTrue(PATH_ACCESS_CONTROL_ENTRIES.containsAll(getPropertiesResponse.getAccessControlList()));

        //readWithResponse
        FileReadResponse readWithResponse = fc.readWithResponse(new ByteArrayOutputStream(), null,
            null, null, false, null, Context.NONE);
        assertTrue(PATH_ACCESS_CONTROL_ENTRIES.containsAll(readWithResponse.getDeserializedHeaders().getAccessControlList()));

        //readToFileWithResponse
        File outFile = new File(testResourceNamer.randomName("", 60) + ".txt");
        outFile.deleteOnExit();
        createdFiles.add(outFile);

        if (outFile.exists()) {
            assertTrue(outFile.delete());
        }

        Response<PathProperties> readToFileResponse = fc.readToFileWithResponse(outFile.getPath(), null,
            null, null, null, false, null, null,
            null);
        assertTrue(PATH_ACCESS_CONTROL_ENTRIES.containsAll(readToFileResponse.getValue().getAccessControlList()));
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "2024-05-04")
    @ParameterizedTest
    @MethodSource("upnHeaderTestSupplier")
    public void upnHeaderTest(Boolean upnHeader) {
        //feature currently doesn't work in preprod - test uses methods that send the request header. verified in fiddler
        //that the header is being sent and is properly assigned.
        dataLakeFileSystemClient = primaryDataLakeServiceClient.getFileSystemClient(generateFileSystemName());
        dataLakeFileSystemClient.create();
        dataLakeFileSystemClient.getDirectoryClient(generatePathName()).create();
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());

        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setAccessControlList(PATH_ACCESS_CONTROL_ENTRIES);
        fc.createWithResponse(options, null, Context.NONE);

        //getProperties
        PathGetPropertiesOptions propertiesOptions = new PathGetPropertiesOptions().setUserPrincipalName(upnHeader);

        PathProperties getPropertiesResponse = fc.getProperties(propertiesOptions);
        assertNotNull(getPropertiesResponse.getAccessControlList());

        //readToFile
        File outFile = new File(testResourceNamer.randomName("", 60) + ".txt");
        outFile.deleteOnExit();
        createdFiles.add(outFile);

        if (outFile.exists()) {
            assertTrue(outFile.delete());
        }
        ReadToFileOptions readToFileOptions = new ReadToFileOptions(outFile.getPath());
        readToFileOptions.setUserPrincipalName(upnHeader).setRange(null).setParallelTransferOptions(null)
            .setDownloadRetryOptions(null).setDataLakeRequestConditions(null)
            .setRangeGetContentMd5(false).setOpenOptions(null);

        PathProperties readToFileResponse = fc.readToFile(readToFileOptions);
        assertNotNull(readToFileResponse.getAccessControlList());

        if (outFile.exists()) {
            assertTrue(outFile.delete());
        }
        Response<PathProperties> readToFileWithResponse = fc.readToFileWithResponse(readToFileOptions, null, null);
        assertNotNull(readToFileWithResponse.getValue().getAccessControlList());

        //openInputStream
        DataLakeFileInputStreamOptions openInputStreamOptions = new DataLakeFileInputStreamOptions().setUserPrincipalName(upnHeader);

        DataLakeFileOpenInputStreamResult openInputStreamResponse = fc.openInputStream(openInputStreamOptions);
        //no way to pull acl from properties in openInputStream
        //assertNotNull(openInputStreamResponse.getProperties().getAccessControlList());

    }

    private static Stream<Arguments> upnHeaderTestSupplier() {
        return Stream.of(
            Arguments.of(true),
            Arguments.of(true),
            Arguments.of((Boolean) null));
    }
}
