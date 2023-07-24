// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
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
import com.azure.storage.file.datalake.models.AccessControlChangeResult;
import com.azure.storage.file.datalake.models.AccessTier;
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
import com.azure.storage.file.datalake.models.FileSystemProperties;
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
import com.azure.storage.file.datalake.options.DataLakePathCreateOptions;
import com.azure.storage.file.datalake.options.DataLakePathDeleteOptions;
import com.azure.storage.file.datalake.options.DataLakePathScheduleDeletionOptions;
import com.azure.storage.file.datalake.options.FileParallelUploadOptions;
import com.azure.storage.file.datalake.options.FileQueryOptions;
import com.azure.storage.file.datalake.options.FileScheduleDeletionOptions;
import com.azure.storage.file.datalake.sas.DataLakeServiceSasSignatureValues;
import com.azure.storage.file.datalake.sas.FileSystemSasPermission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
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
import static org.junit.jupiter.api.Assertions.fail;

public class FileApiTest extends DataLakeTestBase {
    private static final PathPermissions PERMISSIONS = new PathPermissions()
        .setOwner(new RolePermissions().setReadPermission(true).setWritePermission(true).setExecutePermission(true))
        .setGroup(new RolePermissions().setReadPermission(true).setExecutePermission(true))
        .setOther(new RolePermissions().setReadPermission(true));
    private static final List<PathAccessControlEntry> PATH_ACCESS_CONTROL_ENTRIES =
        PathAccessControlEntry.parseList("user::rwx,group::r--,other::---,mask::rwx");
    private static final String GROUP = null;
    private static final String OWNER = null;

    private DataLakeFileClient fc;
    private String fileName;

    @BeforeEach
    public void setup() {
        fileName = generatePathName();
        fc = dataLakeFileSystemClient.createFile(fileName);
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

    private static boolean olderThan20210410ServiceVersion() {
        return olderThan(DataLakeServiceVersion.V2021_04_10);
    }

    @DisabledIf("olderThan20210410ServiceVersion")
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

    private static boolean olderThan20201206ServiceVersion() {
        return olderThan(DataLakeServiceVersion.V2020_12_06);
    }

    @DisabledIf("olderThan20201206ServiceVersion")
    @Test
    public void createOptionsWithACL() {
        List<PathAccessControlEntry> pathAccessControlEntries = PathAccessControlEntry.parseList("user::rwx,group::r--,other::---,mask::rwx");
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setAccessControlList(pathAccessControlEntries);
        fc.createWithResponse(options, null, null);

        List<PathAccessControlEntry> acl = fc.getAccessControl().getAccessControlList();
        assertEquals(pathAccessControlEntries.get(0), acl.get(0)); // testing if owner is set the same
        assertEquals(pathAccessControlEntries.get(1), acl.get(1)); // testing if group is set the same
    }

    @DisabledIf("olderThan20201206ServiceVersion")
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

        FileSystemProperties properties = dataLakeFileSystemClient.getProperties();
        // Directory adds a directory metadata value
        for(String k : metadata.keySet()) {
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

    @DisabledIf("olderThan20201206ServiceVersion")
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

    @DisabledIf("olderThan20201206ServiceVersion")
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

    @DisabledIf("olderThan20201206ServiceVersion")
    @ParameterizedTest
    @MethodSource("timeExpiresOnOptionsSupplier")
    public void createOptionsWithTimeExpiresOn(DataLakePathScheduleDeletionOptions deletionOptions) {
        DataLakePathCreateOptions options = new DataLakePathCreateOptions().setScheduleDeletionOptions(deletionOptions);

        assertEquals(201, fc.createWithResponse(options, null, null).getStatusCode());
    }

    private static Stream<DataLakePathScheduleDeletionOptions> timeExpiresOnOptionsSupplier() {
        return Stream.of(new DataLakePathScheduleDeletionOptions(OffsetDateTime.now().plusDays(1)), null);
    }

    @DisabledIf("olderThan20201206ServiceVersion")
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

    @DisabledIf("olderThan20210410ServiceVersion")
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

    @DisabledIf("olderThan20201206ServiceVersion")
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

    @DisabledIf("olderThan20201206ServiceVersion")
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
        fc.createIfNotExistsWithResponse(options, null, null);;

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

        assertEquals(201, fc.createIfNotExistsWithResponse(options, null, null).getStatusCode());

        FileSystemProperties properties = dataLakeFileSystemClient.getProperties();
        // Directory adds a directory metadata value
        for(String k : metadata.keySet()) {
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

    @DisabledIf("olderThan20201206ServiceVersion")
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

    @DisabledIf("olderThan20201206ServiceVersion")
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

    @DisabledIf("olderThan20201206ServiceVersion")
    @ParameterizedTest
    @MethodSource("timeExpiresOnOptionsSupplier")
    public void createIfNotExistsOptionsWithTimeExpiresOn(DataLakePathScheduleDeletionOptions deletionOptions) {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        DataLakePathCreateOptions options = new DataLakePathCreateOptions()
            .setScheduleDeletionOptions(deletionOptions);

        assertEquals(201, fc.createIfNotExistsWithResponse(options, null, null).getStatusCode());
    }

    @DisabledIf("olderThan20201206ServiceVersion")
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

    private static boolean olderThan20200210ServiceVersion() {
        return olderThan(DataLakeServiceVersion.V2020_02_10);
    }

    @DisabledIf("olderThan20200210ServiceVersion")
    @Test
    public void setACLRecursive() {
        AccessControlChangeResult response = fc.setAccessControlRecursive(PATH_ACCESS_CONTROL_ENTRIES);

        assertEquals(0L, response.getCounters().getChangedDirectoriesCount());
        assertEquals(1L, response.getCounters().getChangedFilesCount());
        assertEquals(0L, response.getCounters().getFailedChangesCount());
    }

    @DisabledIf("olderThan20200210ServiceVersion")
    @Test
    public void updateACLRecursive() {
        AccessControlChangeResult response = fc.updateAccessControlRecursive(PATH_ACCESS_CONTROL_ENTRIES);

        assertEquals(0L, response.getCounters().getChangedDirectoriesCount());
        assertEquals(1L, response.getCounters().getChangedFilesCount());
        assertEquals(0L, response.getCounters().getFailedChangesCount());
    }

    @DisabledIf("olderThan20200210ServiceVersion")
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
        assertFalse(properties.isIncrementalCopy()); // tested in PageBlob."start incremental copy"
        assertEquals(AccessTier.HOT, properties.getAccessTier());
        assertNull(properties.getArchiveStatus());
        assertNull(properties.getMetadata()); // new file does not have default metadata associated
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
        assertNotNull(headers.getValue("Content-Length"));
        assertNotNull(headers.getValue("Content-Type"));
        assertNull(headers.getValue("Content-Range"));
        assertNull(headers.getValue("Content-Encoding"));
        assertNull(headers.getValue("Cache-Control"));
        assertNull(headers.getValue("Content-Disposition"));
        assertNull(headers.getValue("Content-Language"));
        assertNull(headers.getValue("x-ms-blob-sequence-number"));
        assertNull(headers.getValue("x-ms-copy-completion-time"));
        assertNull(headers.getValue("x-ms-copy-status-description"));
        assertNull(headers.getValue("x-ms-copy-id"));
        assertNull(headers.getValue("x-ms-copy-progress"));
        assertNull(headers.getValue("x-ms-copy-source"));
        assertNull(headers.getValue("x-ms-copy-status"));
        assertNull(headers.getValue("x-ms-lease-duration"));
        assertEquals(LeaseStateType.AVAILABLE.toString(), headers.getValue("x-ms-lease-state"));
        assertEquals(LeaseStatusType.UNLOCKED.toString(), headers.getValue("x-ms-lease-status"));
        assertEquals("bytes", headers.getValue("Accept-Ranges"));
        assertNull(headers.getValue("x-ms-blob-committed-block-count"));
        assertNotNull(headers.getValue("x-ms-server-encrypted"));
        assertNull(headers.getValue("x-ms-blob-content-md5"));
        assertNotNull(headers.getValue("x-ms-creation-time"));
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
            .getDeserializedHeaders()
            .getContentMd5();

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
        if (!testFile.exists()) {
            assertTrue(testFile.createNewFile());
            testFile.deleteOnExit();
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
        if (!testFile.exists()) {
            assertTrue(testFile.createNewFile());
            testFile.deleteOnExit();
        }

        fc.append(DATA.getDefaultBinaryData(), 0);
        fc.flush(DATA.getDefaultDataSizeLong(), true);

        fc.readToFile(testFile.getPath(), true);

        assertEquals(DATA.getDefaultText(), new String(Files.readAllBytes(testFile.toPath()), StandardCharsets.UTF_8));
    }

    public void downloadToFileDoesNotExist() throws IOException {
        File testFile = new File(prefix + ".txt");
        if (!testFile.exists()) {
            assertTrue(testFile.createNewFile());
            testFile.deleteOnExit();
        }

        fc.append(DATA.getDefaultBinaryData(), 0);
        fc.flush(DATA.getDefaultDataSizeLong(), true);

        fc.readToFile(testFile.getPath());

        assertEquals(DATA.getDefaultText(), new String(Files.readAllBytes(testFile.toPath()), StandardCharsets.UTF_8));
    }

    @Test
    public void downloadFileDoesNotExistOpenOptions() throws IOException {
        File testFile = new File(prefix + ".txt");
        if (!testFile.exists()) {
            assertTrue(testFile.createNewFile());
            testFile.deleteOnExit();
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
        if (!testFile.exists()) {
            assertTrue(testFile.createNewFile());
            testFile.deleteOnExit();
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

        fc.uploadFromFile(file.toPath().toString(), true);
        File outFile = new File(testResourceNamer.randomName("", 60) + ".txt");
        if (outFile.exists()) {
            assertTrue(outFile.delete());
            outFile.deleteOnExit();
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
        fileClient.uploadFromFile(file.toPath().toString(), true);
        File outFile = new File(testResourceNamer.randomName("", 60) + ".txt");
        if (outFile.exists()) {
            assertTrue(outFile.delete());
            outFile.deleteOnExit();
        }

        PathProperties properties = fileClient.readToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions().setBlockSizeLong(4L * 1024 * 1024), null, null, false, null, null, null)
            .getValue();


        compareFiles(file, outFile, 0, fileSize);
        assertEquals(fileSize, properties.getFileSize());
    }

    @LiveOnly
    @ParameterizedTest
    @MethodSource("downloadFileSupplier")
    public void downloadFileAsyncBufferCopy(int fileSize) {
        String fileSystemName = generateFileSystemName();
        DataLakeServiceAsyncClient datalakeServiceAsyncClient = new DataLakeServiceClientBuilder()
            .endpoint(ENVIRONMENT.getDataLakeAccount().getDataLakeEndpoint())
            .credential(getDataLakeCredential())
            .buildAsyncClient();

        DataLakeFileAsyncClient fileAsyncClient = datalakeServiceAsyncClient.createFileSystem(fileSystemName).block()
            .getFileAsyncClient(generatePathName());

        File file = getRandomFile(fileSize);
        file.deleteOnExit();
        fileAsyncClient.uploadFromFile(file.toPath().toString(), true).block();
        File outFile = new File(testResourceNamer.randomName("", 60) + ".txt");
        if (outFile.exists()) {
            assertTrue(outFile.delete());
            outFile.deleteOnExit();
        }

        StepVerifier.create(fileAsyncClient.readToFileWithResponse(outFile.toPath().toString(), null,
            new ParallelTransferOptions().setBlockSizeLong(4L * 1024 * 1024), null, null, false, null)
            .map(Response::getValue))
            .assertNext(properties -> assertEquals(fileSize, properties.getFileSize()))
            .verifyComplete();

        compareFiles(file, outFile, 0, fileSize);
    }
    @ParameterizedTest
    @MethodSource("downloadFileRangeSupplier")
    public void downloadFileRange(FileRange range) {
        File file = getRandomFile(DATA.getDefaultDataSize());
        file.deleteOnExit();
        fc.uploadFromFile(file.toPath().toString(), true);

        File outFile = new File(testResourceNamer.randomName("", 60));
        if (outFile.exists()) {
            assertTrue(outFile.delete());
            outFile.deleteOnExit();
            ;
            assert outFile.delete();
        }

        fc.readToFileWithResponse(outFile.toPath().toString(), range, null, null, null, false, null, null, null);

        compareFiles(file, outFile, range.getOffset(), range.getCount());
    }

    private static Stream<FileRange> downloadFileRangeSupplier() {
        // The last case is to test a range much much larger than the size of the file to ensure we don't accidentally
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
        fc.uploadFromFile(file.toPath().toString(), true);

        File outFile = new File(prefix);
        if (outFile.exists()) {
            assertTrue(outFile.delete());
            outFile.deleteOnExit();
        }

        assertThrows(DataLakeStorageException.class, () -> fc.readToFileWithResponse(outFile.toPath().toString(),
            new FileRange(DATA.getDefaultDataSizeLong() + 1), null, null, null, false, null, null, null));
    }

    @Test
    public void downloadFileCountNull() {
        File file = getRandomFile(DATA.getDefaultDataSize());
        file.deleteOnExit();
        fc.uploadFromFile(file.toPath().toString(), true);

        File outFile = new File(prefix);
        if (outFile.exists()) {
            assertTrue(outFile.delete());
            outFile.deleteOnExit();;
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
        fc.uploadFromFile(file.toPath().toString(), true);

        File outFile = new File(testResourceNamer.randomName("", 60));
        if (outFile.exists()) {
            assertTrue(outFile.delete());
            outFile.deleteOnExit();
        }

        DataLakeRequestConditions bro = new DataLakeRequestConditions()
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
            .setIfMatch(setupPathMatchCondition(fc, match))
            .setIfNoneMatch(noneMatch)
            .setLeaseId(setupPathLeaseCondition(fc, leaseID));

        assertDoesNotThrow(() -> fc.readToFileWithResponse(outFile.toPath().toString(), null, null, null, bro, false, null, null, null));
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void downloadFileACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        File file = getRandomFile(DATA.getDefaultDataSize());
        file.deleteOnExit();
        fc.uploadFromFile(file.toPath().toString(), true);

        File outFile = new File(prefix);
        if (outFile.exists()) {
            assertTrue(outFile.delete());
            outFile.deleteOnExit();
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

    @LiveOnly
    @Test
    public void downloadFileEtagLock() throws IOException, InterruptedException {
        File file = getRandomFile(Constants.MB);
        file.deleteOnExit();
        fc.uploadFromFile(file.toPath().toString(), true);

        File outFile = new File(prefix);
        Files.deleteIfExists(outFile.toPath());
        outFile.deleteOnExit();

        AtomicInteger counter = new AtomicInteger();

        DataLakeFileAsyncClient facUploading = instrument(new DataLakePathClientBuilder()
            .endpoint(fc.getPathUrl())
            .credential(getDataLakeCredential()))
            .buildFileAsyncClient();

        HttpPipelinePolicy policy = (context, next) -> next.process().flatMap(response -> {
            if (counter.incrementAndGet() == 1) {
                // When the download begins trigger an upload to overwrite the downloading blob so that the download is
                // able to get an ETag before it is changed.
                return facUploading.upload(DATA.getDefaultFlux(), null, true).thenReturn(response);
            }

            return Mono.just(response);
        });

        DataLakeFileAsyncClient facDownloading = instrument(new DataLakePathClientBuilder()
            .addPolicy(policy)
            .endpoint(fc.getPathUrl())
            .credential(getDataLakeCredential()))
            .buildFileAsyncClient();

        // Set up the download to happen in small chunks so many requests need to be sent, this will give the upload
        // time to change the ETag therefore failing the download.
        ParallelTransferOptions options = new ParallelTransferOptions().setBlockSizeLong((long) Constants.KB);

        // This is done to prevent onErrorDropped exceptions from being logged at the error level. If no hook is
        // registered for onErrorDropped the error is logged at the ERROR level.
        //
        // onErrorDropped is triggered once the reactive stream has emitted one element, after that exceptions are
        // dropped.
        Hooks.onErrorDropped(ignored -> { /* do nothing with it */ });

        StepVerifier.create(facDownloading.readToFileWithResponse(outFile.toPath().toString(), null, options, null, null, false, null))
            .verifyErrorSatisfies(ex -> {
                // If an operation is running on multiple threads and multiple return an exception Reactor will combine
                // them into a CompositeException which needs to be unwrapped. If there is only a single exception
                // 'Exceptions.unwrapMultiple' will return a singleton list of the exception it was passed.
                //
                // These exceptions may be wrapped exceptions where the exception we are expecting is contained within
                // ReactiveException that needs to be unwrapped. If the passed exception isn't a 'ReactiveException' it
                // will be returned unmodified by 'Exceptions.unwrap'.
                assertTrue(Exceptions.unwrapMultiple(ex).stream()
                    .anyMatch(ex2 -> {
                        Throwable unwrapped = Exceptions.unwrap(ex2);
                        if (unwrapped instanceof DataLakeStorageException) {
                            return ((DataLakeStorageException) unwrapped).getStatusCode() == 412;
                        }
                        return false;
                    }));
            });

        // Give the file a chance to be deleted by the download operation before verifying its deletion
        Thread.sleep(500);
        assertFalse(outFile.exists());
    }

    @LiveOnly
    @ParameterizedTest
    @ValueSource(ints = {100, 8 * 1026 * 1024 + 10})
    public void downloadFileProgressReceiver(int fileSize) {
        File file = getRandomFile(fileSize);
        file.deleteOnExit();
        fc.uploadFromFile(file.toPath().toString(), true);

        File outFile = new File(prefix);
        if (outFile.exists()) {
            assertTrue(outFile.delete());
            outFile.deleteOnExit();
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
        fc.uploadFromFile(file.toPath().toString(), true);

        File outFile = new File(prefix);
        if (outFile.exists()) {
            assertTrue(outFile.delete());
            outFile.deleteOnExit();
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

        def resp = fc.renameWithResponse(null, generatePathName(), null, null, null, null).getValue()

        def renamedClient = resp.getValue()
        renamedClient.getProperties();


        notThrown(DataLakeStorageException);


        fc.getProperties();


        thrown(DataLakeStorageException);
    }

    public void renameFilesystemWithResponse() {

        def newFileSystem = primaryDataLakeServiceClient.createFileSystem(generateFileSystemName())


        def resp = fc.renameWithResponse(newFileSystem.getFileSystemName(), generatePathName(), null, null, null, null)

        def renamedClient = resp.getValue()
        renamedClient.getProperties();


        notThrown(DataLakeStorageException);


        fc.getProperties();


        thrown(DataLakeStorageException);
    }

    public void renameError() {

        fc = dataLakeFileSystemClient.getFileClient(generatePathName());


        fc.renameWithResponse(null, generatePathName(), null, null, null, null);


        thrown(DataLakeStorageException);
    }

    @Unroll
    public void renameUrlEncoded() {

        fc = dataLakeFileSystemClient.getFileClient(generatePathName() + source);
        fc.create();
        def response = fc.renameWithResponse(null, generatePathName() + destination, null, null, null, null)


        assertEquals(201, response.getStatusCode());


        response = response.getValue().getPropertiesWithResponse(null, null, null);


        assertEquals(200, response.getStatusCode());

        where:
        source     | destination || _
        ""         | ""          || _ /* Both non encoded. */
        "%20%25"   | ""          || _ /* One encoded. */
        ""         | "%20%25"    || _
        "%20%25"   | "%20%25"    || _ /* Both encoded. */
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void renameSourceAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        match = setupPathMatchCondition(fc, match);
        leaseID = setupPathLeaseCondition(fc, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)assertEquals(201, fc.renameWithResponse(null, generatePathName(), drc, null, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void renameSourceACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        fc.create();

        noneMatch = setupPathMatchCondition(fc, noneMatch);
        setupPathLeaseCondition(fc, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)


        fc.renameWithResponse(null, generatePathName(), drc, null, null, null);


        thrown(DataLakeStorageException);
    }

    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void renameDestAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        def pathName = generatePathName()
        def destFile = dataLakeFileSystemClient.getFileClient(pathName)
        destFile.create();
        match = setupPathMatchCondition(destFile, match);
        leaseID = setupPathLeaseCondition(destFile, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)assertEquals(201, fc.renameWithResponse(null, pathName, null, drc, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void renameDestACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        def pathName = generatePathName()
        def destFile = dataLakeFileSystemClient.getFileClient(pathName)
        destFile.create();
        noneMatch = setupPathMatchCondition(destFile, noneMatch);
        setupPathLeaseCondition(destFile, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)


        fc.renameWithResponse(null, pathName, null, drc, null, null);


        thrown(DataLakeStorageException);
    }

    public void renameSasToken() {

        FileSystemSasPermission permissions = new FileSystemSasPermission()
            .setReadPermission(true)
            .setMovePermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setAddPermission(true)
            .setDeletePermission(true)
        def expiryTime = testResourceNamer.now().plusDays(1)

        DataLakeServiceSasSignatureValues sasValues = new DataLakeServiceSasSignatureValues(expiryTime, permissions)
        def sas = dataLakeFileSystemClient.generateSas(sasValues)
        def client = getFileClient(sas, dataLakeFileSystemClient.getFileSystemUrl(), fc.getFilePath())


        def destClient = client.rename(dataLakeFileSystemClient.getFileSystemName(), generatePathName())


        notThrown(DataLakeStorageException);
        destClient.getProperties();
    }

    public void renameSasTokenWithLeadingQuestionMark() {

        FileSystemSasPermission permissions = new FileSystemSasPermission()
            .setReadPermission(true)
            .setMovePermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setAddPermission(true)
            .setDeletePermission(true)
        def expiryTime = testResourceNamer.now().plusDays(1)

        DataLakeServiceSasSignatureValues sasValues = new DataLakeServiceSasSignatureValues(expiryTime, permissions)
        def sas = "?" + dataLakeFileSystemClient.generateSas(sasValues)
        def client = getFileClient(sas, dataLakeFileSystemClient.getFileSystemUrl(), fc.getFilePath())


        def destClient = client.rename(dataLakeFileSystemClient.getFileSystemName(), generatePathName())


        notThrown(DataLakeStorageException);
        destClient.getProperties();
    }

    public void appendDataMin() {

        fc.append(new ByteArrayInputStream(data.defaultBytes), 0, DATA.getDefaultDataSizeLong());


        notThrown(DataLakeStorageException);
    }

    public void appendData() {

        def response = fc.appendWithResponse(data.defaultInputStream, 0, DATA.getDefaultDataSizeLong(), null, null, null, null)
        def headers = response.getHeaders()


        assertEquals(202, response.getStatusCode());
        headers.getValue("x-ms-request-id") != null
        headers.getValue("x-ms-version") != null
        headers.getValue("Date") != null
        Boolean.parseBoolean(headers.getValue("x-ms-request-server-encrypted"));
    }

    public void appendDataMd5() {

        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        fc.create();
        def md5 = MessageDigest.getInstance("MD5").digest(data.defaultText.getBytes())
        def response = fc.appendWithResponse(data.defaultInputStream, 0, DATA.getDefaultDataSizeLong(), md5, null, null, null)
        def headers = response.getHeaders()


        assertEquals(202, response.getStatusCode());
        headers.getValue("x-ms-request-id") != null
        headers.getValue("x-ms-version") != null
        headers.getValue("Date") != null
        Boolean.parseBoolean(headers.getValue("x-ms-request-server-encrypted"));
    }

    @Unroll
    public void appendDataIllegalArguments() {

        fc.append(is == null ? null : is, 0, dataSize);


        thrown(exceptionType);

        where:
        is                      | dataSize                 || exceptionType
        null                    | DATA.getDefaultDataSizeLong()     || NullPointerException
        data.defaultInputStream | DATA.getDefaultDataSizeLong() + 1 || UnexpectedLengthException
        data.defaultInputStream | DATA.getDefaultDataSizeLong() - 1 || UnexpectedLengthException
    }

    public void appendDataEmptyBody() {

        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        fc.create();
        fc.append(new ByteArrayInputStream(new byte[0]), 0, 0);


        thrown(DataLakeStorageException);
    }

    public void appendDataNullBody() {

        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        fc.create();
        fc.append(null, 0, 0);


        thrown(NullPointerException);
    }

    public void appendDataLease() {

        def leaseID = setupPathLeaseCondition(fc, receivedLeaseID)assertEquals(202, fc.appendWithResponse(data.defaultInputStream, 0, DATA.getDefaultDataSizeLong(), null, leaseID, null, null).getStatusCode());
    }

    public void appendDataLeaseFail() {

        setupPathLeaseCondition(fc, receivedLeaseID);


        fc.appendWithResponse(data.defaultInputStream, 0, DATA.getDefaultDataSizeLong(), null, garbageLeaseID, null, null);


        def e = thrown(DataLakeStorageException)assertEquals(412, e.getResponse().getStatusCode());
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_08_04")
    public void appendDataLeaseAcquire() {

        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        fc.createIfNotExists();

        def proposedLeaseId = UUID.randomUUID().toString()
        def duration = 15
        def leaseAction = LeaseAction.ACQUIRE
        DataLakeFileAppendOptions appendOptions = new DataLakeFileAppendOptions()
            .setLeaseAction(leaseAction)
            .setProposedLeaseId(proposedLeaseId)
            .setLeaseDuration(duration)


        def response = fc.appendWithResponse(data.defaultInputStream, 0, DATA.getDefaultDataSizeLong(), appendOptions, null, null)
        def fileProperties = fc.getProperties()


        assertEquals(202, response.getStatusCode());
        fileProperties.getLeaseStatus() == LeaseStatusType.LOCKED
        fileProperties.getLeaseState() == LeaseStateType.LEASED
        fileProperties.getLeaseDuration() == LeaseDurationType.FIXED
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_08_04")
    public void appendDataLeaseAutoRenew() {

        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        fc.createIfNotExists();
        String leaseId = CoreUtils.randomUuid().toString();
        def duration = 15
        def leaseClient = createLeaseClient(fc, leaseId)

        leaseClient.acquireLease(duration);

        def leaseAction = LeaseAction.AUTO_RENEW
        DataLakeFileAppendOptions appendOptions = new DataLakeFileAppendOptions()
            .setLeaseAction(leaseAction)
            .setLeaseId(leaseId)


        def response = fc.appendWithResponse(data.defaultInputStream, 0, DATA.getDefaultDataSizeLong(), appendOptions, null, null)
        def fileProperties = fc.getProperties()


        assertEquals(202, response.getStatusCode());
        fileProperties.getLeaseStatus() == LeaseStatusType.LOCKED
        fileProperties.getLeaseState() == LeaseStateType.LEASED
        fileProperties.getLeaseDuration() == LeaseDurationType.FIXED
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_08_04")
    public void appendDataLeaseRelease() {

        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        fc.createIfNotExists();
        String leaseId = CoreUtils.randomUuid().toString();
        def duration = 15
        def leaseClient = createLeaseClient(fc, leaseId)

        leaseClient.acquireLease(duration);

        def leaseAction = LeaseAction.RELEASE
        DataLakeFileAppendOptions appendOptions = new DataLakeFileAppendOptions()
            .setLeaseAction(leaseAction)
            .setLeaseId(leaseId)
            .setFlush(true)


        def response = fc.appendWithResponse(data.defaultInputStream, 0, DATA.getDefaultDataSizeLong(), appendOptions, null, null)
        def fileProperties = fc.getProperties()


        assertEquals(202, response.getStatusCode());
        fileProperties.getLeaseStatus() == LeaseStatusType.UNLOCKED
        fileProperties.getLeaseState() == LeaseStateType.AVAILABLE
    }

    @RequiredServiceVersion(clazz = DataLakeServiceVersion.class, min = "V2020_08_04")
    public void appendDataLeaseAcquireRelease() {

        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        fc.createIfNotExists();
        def proposedLeaseId = UUID.randomUUID().toString()
        def duration = 15

        def leaseAction = LeaseAction.ACQUIRE_RELEASE
        DataLakeFileAppendOptions appendOptions = new DataLakeFileAppendOptions()
            .setLeaseAction(leaseAction)
            .setProposedLeaseId(proposedLeaseId)
            .setLeaseDuration(duration)
            .setFlush(true)


        def response = fc.appendWithResponse(data.defaultInputStream, 0, DATA.getDefaultDataSizeLong(), appendOptions, null, null)
        def fileProperties = fc.getProperties()


        assertEquals(202, response.getStatusCode());
        fileProperties.getLeaseStatus() == LeaseStatusType.UNLOCKED
        fileProperties.getLeaseState() == LeaseStateType.AVAILABLE
    }

    public void appendDataError() {

        fc = dataLakeFileSystemClient.getFileClient(generatePathName());


        fc.appendWithResponse(data.defaultInputStream, 0, DATA.getDefaultDataSizeLong(), null, null, null, null);


        def e = thrown(DataLakeStorageException)assertEquals(404, e.getResponse().getStatusCode());
    }

    public void appendDataRetryOnTransientFailure() {

        def clientWithFailure = getFileClient(
            environment.dataLakeAccount.credential,
            fc.getFileUrl(),
            new TransientFailureInjectingHttpPipelinePolicy()
        )


        clientWithFailure.append(data.defaultInputStream, 0, DATA.getDefaultDataSizeLong());
        fc.flush(DATA.getDefaultDataSizeLong(), true);


        ByteArrayOutputStream os = new ByteArrayOutputStream()
        fc.read(os);
        os.toByteArray() == data.defaultBytes
    }

    @DisabledIf("olderThan20191212ServiceVersion")
    public void appendDataFlush() {

        DataLakeFileAppendOptions appendOptions = new DataLakeFileAppendOptions().setFlush(true)
        def response = fc.appendWithResponse(data.defaultInputStream, 0, DATA.getDefaultDataSizeLong(), appendOptions, null, null)
        def headers = response.getHeaders()


        assertEquals(202, response.getStatusCode());
        headers.getValue("x-ms-request-id") != null
        headers.getValue("x-ms-version") != null
        headers.getValue("Date") != null
        Boolean.parseBoolean(headers.getValue("x-ms-request-server-encrypted"));
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        fc.read(os);
        os.toByteArray() == data.defaultBytes
    }

    public void appendBinaryDataMin() {

        fc.append(data.defaultBinaryData, 0);


        notThrown(DataLakeStorageException);
    }

    public void appendBinaryData() {

        def response = fc.appendWithResponse(data.defaultBinaryData, 0, null, null, null, null)
        def headers = response.getHeaders()

        assertEquals(202, response.getStatusCode());
        headers.getValue("x-ms-request-id") != null
        headers.getValue("x-ms-version") != null
        headers.getValue("Date") != null
        Boolean.parseBoolean(headers.getValue("x-ms-request-server-encrypted"));
    }

    @DisabledIf("olderThan20191212ServiceVersion")
    public void appendBinaryDataFlush() {

        DataLakeFileAppendOptions appendOptions = new DataLakeFileAppendOptions().setFlush(true)
        def response = fc.appendWithResponse(data.defaultBinaryData, 0, appendOptions, null, null)
        def headers = response.getHeaders()


        assertEquals(202, response.getStatusCode());
        headers.getValue("x-ms-request-id") != null
        headers.getValue("x-ms-version") != null
        headers.getValue("Date") != null
        Boolean.parseBoolean(headers.getValue("x-ms-request-server-encrypted"));
    }

    public void flushDataMin() {

        fc.append(new ByteArrayInputStream(data.defaultBytes), 0, DATA.getDefaultDataSizeLong());
        fc.flush(DATA.getDefaultDataSizeLong(), true);


        notThrown(DataLakeStorageException);
    }

    public void flushClose() {

        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        fc.create();
        fc.append(data.defaultInputStream, 0, DATA.getDefaultDataSizeLong());
        fc.flushWithResponse(DATA.getDefaultDataSizeLong(), false, true, null, null, null, null);


        notThrown(DataLakeStorageException);
    }

    def "Flush retain uncommitted data "() {

        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        fc.create();
        fc.append(data.defaultInputStream, 0, DATA.getDefaultDataSizeLong());
        fc.flushWithResponse(DATA.getDefaultDataSizeLong(), true, false, null, null, null, null);


        notThrown(DataLakeStorageException);
    }

    public void flushIA() {

        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        fc.create();
        fc.append(data.defaultInputStream, 0, DATA.getDefaultDataSizeLong());
        fc.flushWithResponse(4, false, false, null, null, null, null);


        thrown(DataLakeStorageException);
    }

    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null,null", "control,disposition,encoding,language,type"})
    public void flushHeaders(String cacheControl, String contentDisposition, String contentEncoding,
        String contentLanguage, String contentType) {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        fc.create();
        fc.append(data.defaultInputStream, 0, DATA.getDefaultDataSizeLong());
        PathHttpHeaders headers = new PathHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentType(contentType)


        fc.flushWithResponse(DATA.getDefaultDataSizeLong(), false, false, headers, null, null, null);
        def response = fc.getPropertiesWithResponse(null, null, null)

        // If the value isn't set the service will automatically set it
        contentType = (contentType == null) ? "application/octet-stream" : contentType;


        validatePathProperties(response, cacheControl, contentDisposition, contentEncoding, contentLanguage, null, contentType);
    }


    @ParameterizedTest
    @MethodSource("modifiedMatchAndLeaseIdSupplier")
    public void flushAC(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        fc.create();
        fc.append(data.defaultInputStream, 0, DATA.getDefaultDataSizeLong());

        match = setupPathMatchCondition(fc, match);
        leaseID = setupPathLeaseCondition(fc, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)assertEquals(200, fc.flushWithResponse(DATA.getDefaultDataSizeLong(), false, false, null, drc, null, null).getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void flushACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        fc = dataLakeFileSystemClient.getFileClient(generatePathName());
        fc.create();
        fc.append(data.defaultInputStream, 0, DATA.getDefaultDataSizeLong());
        noneMatch = setupPathMatchCondition(fc, noneMatch);
        setupPathLeaseCondition(fc, leaseID);
        DataLakeRequestConditions drc = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)


        fc.flushWithResponse(DATA.getDefaultDataSizeLong(), false, false, null, drc, null, null);

        thrown(DataLakeStorageException);
    }

    public void flushError() {

        fc = dataLakeFileSystemClient.getFileClient(generatePathName());


        fc.flush(1, true);


        thrown(DataLakeStorageException);
    }

    public void flushDataOverwrite() {

        fc.append(new ByteArrayInputStream(data.defaultBytes), 0, DATA.getDefaultDataSizeLong());
        fc.flush(DATA.getDefaultDataSizeLong(), true);


        notThrown(DataLakeStorageException);


        fc.append(new ByteArrayInputStream(data.defaultBytes), 0, DATA.getDefaultDataSizeLong());
        // Attempt to write data without overwrite enabled
        fc.flush(DATA.getDefaultDataSizeLong(), false);


        thrown(DataLakeStorageException);
    }

    public void getFileNameAndBuildClient() {

        DataLakeFileClient client = dataLakeFileSystemClient.getFileClient(originalFileName)


        // Note : Here I use Path because there is a test that tests the use of a /
        client.getFilePath() == finalFileName

        where:
        originalFileName       | finalFileName
        "file"                 | "file"
        "path/to]a file"       | "path/to]a file"
        "path%2Fto%5Da%20file" | "path/to]a file"
        "斑點"                   | "斑點"
        "%E6%96%91%E9%BB%9E"   | "斑點"
    }

    public void builderBearerTokenValidation() {
        // Technically no additional checks need to be added to datalake builder since the corresponding blob builder fails

        String endpoint = BlobUrlParts.parse(fc.getFileUrl()).setScheme("http").toUrl()
        DataLakePathClientBuilder builder = new DataLakePathClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint(endpoint)


        builder.buildFileClient();


        thrown(IllegalArgumentException);
    }

    // "No overwrite interrupted" tests were not ported over for datalake. This is because the access condition check
    // occurs on the create method, so simple access conditions tests suffice.
    @Unroll
    @LiveOnly // Test uploads large amount of data
    public void uploadFromFile() {

        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName())
        def file = getRandomFile(fileSize)


        // Block length will be ignored for single shot.
        StepVerifier.create(fac.uploadFromFile(file.getPath(), new ParallelTransferOptions().setBlockSizeLong(blockSize),
                null, null, null))
            .verifyComplete();


        File outFile = new File(file.getPath().toString() + "result")
        outFile.createNewFile();

        FileOutputStream outStream = new FileOutputStream(outFile)
        outStream.write(FluxUtil.collectBytesInByteBufferStream(fac.read()).block());
        outStream.close();

        compareFiles(file, outFile, 0, fileSize);

        cleanup:
        outFile.delete();
        file.delete();

        where:
        fileSize                                       | blockSize       || commitedBlockCount
        10                                             | null            || 0  // Size is too small to trigger block uploading
        10 * Constants.KB                              | null            || 0  // Size is too small to trigger block uploading
        50 * Constants.MB                              | null            || 0  // Size is too small to trigger block uploading
        101 * Constants.MB                             | 4 * 1024 * 1024 || 0  // Size is too small to trigger block uploading
    }

    public void uploadFromFileWithMetadata() {

        Map<String, String> metadata = Collections.singletonMap("metadata", "value")
        def file = getRandomFile(Constants.KB)
        ByteArrayOutputStream outStream = new ByteArrayOutputStream()


        fc.uploadFromFile(file.getPath(), null, null, metadata, null, null);


        metadata == fc.getProperties().getMetadata()
        fc.read(outStream);
        outStream.toByteArray() == Files.readAllBytes(file.toPath())

        cleanup:
        file.delete();
    }

    public void uploadFromFileDefaultNoOverwrite() {

        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName())
        fac.create().block();

        def file = getRandomFile(50)
        fc.uploadFromFile(file.toPath().toString());


        thrown(DataLakeStorageException);

        and:
        def uploadVerifier = StepVerifier.create(fac.uploadFromFile(getRandomFile(50).toPath().toString()))


        uploadVerifier.verifyError(DataLakeStorageException);

        cleanup:
        file.delete();
    }

    public void uploadFromFileOverwrite() {

        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName())
        fac.create().block();


        def file = getRandomFile(50)
        fc.uploadFromFile(file.toPath().toString(), true);


        notThrown(BlobStorageException);

        and:
        def uploadVerifier = StepVerifier.create(fac.uploadFromFile(getRandomFile(50).toPath().toString(), true))


        uploadVerifier.verifyComplete()

        cleanup:
        file.delete()
    }

    /*
     * Reports the number of bytes sent when uploading a file. This is different than other reporters which track the
     * number of reportings as upload from file hooks into the loading data from disk data stream which is a hard-coded
     * read size.
     */
    class FileUploadReporter implements ProgressReceiver {
        private long reportedByteCount

        @Override
        void reportProgress(long bytesTransferred) {
            this.reportedByteCount = bytesTransferred
        }

        long getReportedByteCount() {
            return this.reportedByteCount
        }
    }

    class FileUploadListener implements ProgressListener {
        private long reportedByteCount

        @Override
        void handleProgress(long bytesTransferred) {
            this.reportedByteCount = bytesTransferred
        }

        long getReportedByteCount() {
            return this.reportedByteCount
        }
    }

    @Unroll
    @LiveOnly
    public void uploadFromFileReporter() {

        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName())


        FileUploadReporter uploadReporter = new FileUploadReporter()
        def file = getRandomFile(size)

        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxConcurrency(bufferCount)
            .setProgressReceiver(uploadReporter).setMaxSingleUploadSizeLong(blockSize - 1)


        StepVerifier.create(fac.uploadFromFile(file.toPath().toString(), parallelTransferOptions,
                null, null, null))
            .verifyComplete()

        uploadReporter.getReportedByteCount() == size

        cleanup:
        file.delete()

        where:
        size              | blockSize         | bufferCount
        10 * Constants.MB | 10 * Constants.MB | 8
        20 * Constants.MB | 1 * Constants.MB  | 5
        10 * Constants.MB | 5 * Constants.MB  | 2
        10 * Constants.MB | 10 * Constants.KB | 100
    }

    @Unroll
    @LiveOnly
    public void uploadFromFileListener() {

        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName())


        FileUploadListener uploadListener = new FileUploadListener()
        def file = getRandomFile(size)

        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxConcurrency(bufferCount)
            .setProgressListener(uploadListener).setMaxSingleUploadSizeLong(blockSize - 1)


        StepVerifier.create(fac.uploadFromFile(file.toPath().toString(), parallelTransferOptions,
                null, null, null))
            .verifyComplete()

        uploadListener.getReportedByteCount() == size

        cleanup:
        file.delete()

        where:
        size              | blockSize         | bufferCount
        10 * Constants.MB | 10 * Constants.MB | 8
        20 * Constants.MB | 1 * Constants.MB  | 5
        10 * Constants.MB | 5 * Constants.MB  | 2
        10 * Constants.MB | 10 * Constants.KB | 100
    }

    @Unroll
    public void uploadFromFileOptions() {

        def file = getRandomFile((int) dataSize)


        fc.uploadFromFile(file.toPath().toString(),
            new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(singleUploadSize), null, null, null, null)


        fc.getProperties().getFileSize() == dataSize


        cleanup:
        file.delete()

        where:
        dataSize | singleUploadSize | blockSize || expectedBlockCount
        100      | 50               | null      || 1 // Test that singleUploadSize is respected
        100      | 50               | 20        || 5 // Test that blockSize is respected
    }

    @Unroll
    public void uploadFromFileWithResponse() {

        def file = getRandomFile((int) dataSize)


        def response = fc.uploadFromFileWithResponse(file.toPath().toString(),
            new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(singleUploadSize), null, null, null, null, null)


        fc.getProperties().getFileSize() == dataSize
        assertEquals(200, response.getStatusCode());
        response.getValue().getETag() != null
        response.getValue().getLastModified() != null


        cleanup:
        file.delete()

        where:
        dataSize | singleUploadSize | blockSize || expectedBlockCount
        100      | 50               | null      || 1 // Test that singleUploadSize is respected
        100      | 50               | 20        || 5 // Test that blockSize is respected
    }

    @LiveOnly
    public void asyncBufferedUploadEmpty() {

        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName())


        StepVerifier.create(fac.upload(Flux.just(ByteBuffer.wrap(new byte[0])), null))
            .verifyErrorSatisfies({it instanceof DataLakeStorageException})
    }

    @Unroll
    @LiveOnly
    public void asyncBufferedUploadEmptyBuffers() {

        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName())


        StepVerifier.create(fac.upload(Flux.fromIterable([buffer1, buffer2, buffer3]), null, true))
            .assertNext({ assert it.getETag() != null })
            .verifyComplete()

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(fac.read()))
            .assertNext({ assert it == expectedDownload })
            .verifyComplete()

        where:
        buffer1                                                   | buffer2                                               | buffer3                                                    || expectedDownload
        ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8)) | ByteBuffer.wrap(" ".getBytes(StandardCharsets.UTF_8)) | ByteBuffer.wrap("world!".getBytes(StandardCharsets.UTF_8)) || "Hello world!".getBytes(StandardCharsets.UTF_8)
        ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8)) | ByteBuffer.wrap(" ".getBytes(StandardCharsets.UTF_8)) | ByteBuffer.wrap(new byte[0])                               || "Hello ".getBytes(StandardCharsets.UTF_8)
        ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8)) | ByteBuffer.wrap(new byte[0])                          | ByteBuffer.wrap("world!".getBytes(StandardCharsets.UTF_8)) || "Helloworld!".getBytes(StandardCharsets.UTF_8)
        ByteBuffer.wrap(new byte[0])                              | ByteBuffer.wrap(" ".getBytes(StandardCharsets.UTF_8)) | ByteBuffer.wrap("world!".getBytes(StandardCharsets.UTF_8)) || " world!".getBytes(StandardCharsets.UTF_8)
    }

    @Unroll
    @LiveOnly // Test uploads large amount of data
    public void asyncBufferedUpload() {

        DataLakeFileAsyncClient facWrite = getPrimaryServiceClientForWrites(bufferSize)
            .getFileSystemAsyncClient(dataLakeFileSystemAsyncClient.getFileSystemName())
            .getFileAsyncClient(generatePathName())
        facWrite.create().block()
        def facRead = dataLakeFileSystemAsyncClient.getFileAsyncClient(facWrite.getFileName())


        def data = getRandomData(dataSize)
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(bufferSize).setMaxConcurrency(numBuffs).setMaxSingleUploadSizeLong(4 * Constants.MB)
        facWrite.upload(Flux.just(data), parallelTransferOptions, true).block()
        data.position(0)


        // Due to memory issues, this check only runs on small to medium sized data sets.
        if (dataSize < 100 * 1024 * 1024) {
            StepVerifier.create(collectBytesInBuffer(facRead.read()))
                .assertNext({ assert it == data })
                .verifyComplete()
        }

        where:
        dataSize           | bufferSize        | numBuffs || blockCount
        35 * Constants.MB  | 5 * Constants.MB  | 2        || 7 // Requires cycling through the same buffers multiple times.
        35 * Constants.MB  | 5 * Constants.MB  | 5        || 7 // Most buffers may only be used once.
        100 * Constants.MB | 10 * Constants.MB | 2        || 10 // Larger data set.
        100 * Constants.MB | 10 * Constants.MB | 5        || 10 // Larger number of Buffs.
        10 * Constants.MB  | 1 * Constants.MB  | 10       || 10 // Exactly enough buffer space to hold all the data.
        50 * Constants.MB  | 10 * Constants.MB | 2        || 5 // Larger data.
        10 * Constants.MB  | 2 * Constants.MB  | 4        || 5
        10 * Constants.MB  | 3 * Constants.MB  | 3        || 4 // Data does not squarely fit in buffers.
    }

    def compareListToBuffer(List<ByteBuffer> buffers, ByteBuffer result) {
        result.position(0)
        for (ByteBuffer buffer : buffers) {
            buffer.position(0)
            result.limit(result.position() + buffer.remaining())
            if (buffer != result) {
                return false
            }
            result.position(result.position() + buffer.remaining())
        }
        return result.remaining() == 0
    }

    /*      Reporter for testing Progress Receiver
     *        Will count the number of reports that are triggered         */

    class Reporter implements ProgressReceiver {
        private final long blockSize
        private long reportingCount

        Reporter(long blockSize) {
            this.blockSize = blockSize
        }

        @Override
        void reportProgress(long bytesTransferred) {
            assert bytesTransferred % blockSize == 0
            this.reportingCount += 1
        }

        long getReportingCount() {
            return this.reportingCount
        }
    }

    class Listener implements ProgressListener {
        private final long blockSize
        private long reportingCount

        Listener(long blockSize) {
            this.blockSize = blockSize
        }

        @Override
        void handleProgress(long bytesTransferred) {
            assert bytesTransferred % blockSize == 0
            this.reportingCount += 1
        }

        long getReportingCount() {
            return this.reportingCount
        }
    }

    @Unroll
    @LiveOnly
    public void bufferedUploadWithReporter() {

        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName())


        Reporter uploadReporter = new Reporter(blockSize)

        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxConcurrency(bufferCount)
            .setProgressReceiver(uploadReporter).setMaxSingleUploadSizeLong(4 * Constants.MB)


        StepVerifier.create(fac.uploadWithResponse(Flux.just(getRandomData(size)),
                parallelTransferOptions, null, null, null))
            .assertNext({
        assert assertEquals(200, it.getStatusCode());

        /*
         * Verify that the reporting count is equal or greater than the size divided by block size in the case
         * that operations need to be retried. Retry attempts will increment the reporting count.
         */
        assert uploadReporter.getReportingCount() >= (long) (size / blockSize)
            }).verifyComplete()

        where:
        size              | blockSize          | bufferCount
        10 * Constants.MB | 10 * Constants.MB  | 8
        20 * Constants.MB | 1 * Constants.MB   | 5
        10 * Constants.MB | 5 * Constants.MB   | 2
        10 * Constants.MB | 512 * Constants.KB | 20
    }

    @Unroll
    @LiveOnly
    public void bufferedUploadWithListener() {

        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName())


        Listener uploadListener = new Listener(blockSize)

        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxConcurrency(bufferCount)
            .setProgressListener(uploadListener).setMaxSingleUploadSizeLong(4 * Constants.MB)


        StepVerifier.create(fac.uploadWithResponse(Flux.just(getRandomData(size)),
                parallelTransferOptions, null, null, null))
            .assertNext({
        assert assertEquals(200, it.getStatusCode());

        /*
         * Verify that the reporting count is equal or greater than the size divided by block size in the case
         * that operations need to be retried. Retry attempts will increment the reporting count.
         */
        assert uploadListener.getReportingCount() >= (long) (size / blockSize)
            }).verifyComplete()

        where:
        size              | blockSize          | bufferCount
        10 * Constants.MB | 10 * Constants.MB  | 8
        20 * Constants.MB | 1 * Constants.MB   | 5
        10 * Constants.MB | 5 * Constants.MB   | 2
        10 * Constants.MB | 512 * Constants.KB | 20
    }

    @Unroll
    @LiveOnly // Test uploads large amount of data
    public void bufferedUploadChunkedSource() {

        DataLakeFileAsyncClient facWrite = getPrimaryServiceClientForWrites(bufferSize)
            .getFileSystemAsyncClient(dataLakeFileSystemAsyncClient.getFileSystemName())
            .getFileAsyncClient(generatePathName())
        facWrite.create().block()
        def facRead = dataLakeFileSystemAsyncClient.getFileAsyncClient(facWrite.getFileName())
        /*
        This test should validate that the upload should work regardless of what format the passed data is in because
        it will be chunked appropriately.
         */
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(bufferSize * Constants.MB).setMaxConcurrency(numBuffers).setMaxSingleUploadSizeLong(4 * Constants.MB)
        def dataList = [] as List<ByteBuffer>

        for (def size : dataSizeList) {
            dataList.add(getRandomData(size * Constants.MB))
        }
        def uploadOperation = facWrite.upload(Flux.fromIterable(dataList), parallelTransferOptions, true)


        StepVerifier.create(uploadOperation.then(collectBytesInBuffer(facRead.read())))
            .assertNext({ assert compareListToBuffer(dataList, it) })
            .verifyComplete()

        where:
        dataSizeList          | bufferSize | numBuffers || blockCount
            [7, 7]                | 10         | 2          || 2 // First item fits entirely in the buffer, next item spans two buffers
            [3, 3, 3, 3, 3, 3, 3] | 10         | 2          || 3 // Multiple items fit non-exactly in one buffer.
            [10, 10]              | 10         | 2          || 2 // Data fits exactly and does not need chunking.
            [50, 51, 49]          | 10         | 2          || 15 // Data needs chunking and does not fit neatly in buffers. Requires waiting for buffers to be released.
        // The case of one large buffer needing to be broken up is tested in the previous test.
    }

    // These two tests are to test optimizations in buffered upload for small files.
    @Unroll
    @LiveOnly
    public void bufferedUploadHandlePathing() {

        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName())
        def dataList = [] as List<ByteBuffer>
        for (def size : dataSizeList) {
            dataList.add(getRandomData(size))
        }

        def uploadOperation = fac.upload(Flux.fromIterable(dataList), new ParallelTransferOptions().setMaxSingleUploadSizeLong(4 * Constants.MB), true)


        StepVerifier.create(uploadOperation.then(collectBytesInBuffer(fac.read())))
            .assertNext({ assert compareListToBuffer(dataList, it) })
            .verifyComplete()

        where:
        dataSizeList                         | blockCount
            [4 * Constants.MB + 1, 10]           | 2
                                                       [4 * Constants.MB]                   | 0
                                                       [10, 100, 1000, 10000]               | 0
            [4 * Constants.MB, 4 * Constants.MB] | 2
    }

    @Unroll
    @LiveOnly
    public void bufferedUploadHandlePathingHotFlux() {

        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName())
        def dataList = [] as List<ByteBuffer>
        for (def size : dataSizeList) {
            dataList.add(getRandomData(size))
        }
        def uploadOperation = fac.upload(Flux.fromIterable(dataList).publish().autoConnect(),
            new ParallelTransferOptions().setMaxSingleUploadSizeLong(4 * Constants.MB), true)


        StepVerifier.create(uploadOperation.then(collectBytesInBuffer(fac.read())))
            .assertNext({ assert compareListToBuffer(dataList, it) })
            .verifyComplete()

        where:
        dataSizeList                         | blockCount
            [4 * Constants.MB + 1, 10]           | 2
                                                       [4 * Constants.MB]                   | 0
                                                       [10, 100, 1000, 10000]               | 0
            [4 * Constants.MB, 4 * Constants.MB] | 2
    }

    @Unroll
    @LiveOnly
    public void bufferedUploadHandlePathingHotFluxWithTransientFailure() {

        def clientWithFailure = getFileAsyncClient(
            environment.dataLakeAccount.credential,
            fc.getFileUrl(),
            new TransientFailureInjectingHttpPipelinePolicy()
        )


        def dataList = [] as List<ByteBuffer>
                                  dataSizeList.each { size -> dataList.add(getRandomData(size)) }
        def uploadOperation = clientWithFailure.upload(Flux.fromIterable(dataList).publish().autoConnect(),
            new ParallelTransferOptions().setMaxSingleUploadSizeLong(4 * Constants.MB), true)


        def fcAsync = getFileAsyncClient(environment.dataLakeAccount.credential, fc.getFileUrl())
        StepVerifier.create(uploadOperation.then(collectBytesInBuffer(fcAsync.read())))
            .assertNext({ assert compareListToBuffer(dataList, it) })
            .verifyComplete()

        where:
        dataSizeList                         | blockCount
            [10, 100, 1000, 10000]               | 0
            [4 * Constants.MB + 1, 10]           | 2
            [4 * Constants.MB, 4 * Constants.MB] | 2
    }

    @Unroll
    @LiveOnly
    public void bufferedUploadSyncHandlePathingWithTransientFailure() {
        /*
        This test ensures that although we no longer mark and reset the source stream for buffered upload, it still
        supports retries in all cases for the sync client.
         */

        def clientWithFailure = getFileClient(
            environment.dataLakeAccount.credential,
            fc.getFileUrl(),
            new TransientFailureInjectingHttpPipelinePolicy()
        )


        def data = getRandomByteArray(dataSize)
        clientWithFailure.uploadWithResponse(new FileParallelUploadOptions(new ByteArrayInputStream(data), dataSize)
            .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong(2 * Constants.MB)
                .setBlockSizeLong(2 * Constants.MB)), null, null)


        ByteArrayOutputStream os = new ByteArrayOutputStream(dataSize)
        fc.read(os)
        data == os.toByteArray()

        where:
        dataSize              | blockCount
        11110                 | 0
        2 * Constants.MB + 11 | 2
    }

    public void bufferedUploadIllegalArgumentsNull() {

        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName())
        fac.create().block()

        StepVerifier.create(fac.upload((Flux<ByteBuffer>)null, new ParallelTransferOptions().setBlockSizeLong(4).setMaxConcurrency(4), true))
            .verifyErrorSatisfies({ assert it instanceof NullPointerException })
    }

    @Unroll
    @LiveOnly
    public void bufferedUploadHeaders() {

        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName())


        def randomData = getRandomByteArray(dataSize)
        def contentMD5 = validateContentMD5 ? MessageDigest.getInstance("MD5").digest(randomData) : null
        def uploadOperation = fac.uploadWithResponse(Flux.just(ByteBuffer.wrap(randomData)), new ParallelTransferOptions().setMaxSingleUploadSizeLong(4 * Constants.MB), new PathHttpHeaders()
                .setCacheControl(cacheControl)
                .setContentDisposition(contentDisposition)
                .setContentEncoding(contentEncoding)
                .setContentLanguage(contentLanguage)
                .setContentMd5(contentMD5)
                .setContentType(contentType),
            null, null)


        StepVerifier.create(uploadOperation.then(fac.getPropertiesWithResponse(null)))
            .assertNext({
        assert validatePathProperties(it, cacheControl, contentDisposition, contentEncoding, contentLanguage,
            contentMD5, contentType == null ? "application/octet-stream" : contentType)
            }).verifyComplete()
        // HTTP default content type is application/octet-stream.

        where:
        // Depending on the size of the stream either a single append will be called or multiple.
        dataSize              | cacheControl | contentDisposition | contentEncoding | contentLanguage | validateContentMD5 | contentType
        DATA.getDefaultDataSizeLong()  | null         | null               | null            | null            | true               | null
        DATA.getDefaultDataSizeLong()  | "control"    | "disposition"      | "encoding"      | "language"      | true               | "type"
        6 * Constants.MB      | null         | null               | null            | null            | false              | null
        6 * Constants.MB      | "control"    | "disposition"      | "encoding"      | "language"      | true               | "type"
    }

    @LiveOnly
    @ParameterizedTest
    @CsvSource(value = {"null,null,null,null", "foo,bar,fizz,buzz"}, nullValues = "null")
    public void bufferedIploadMetadata(String key1, String value1, String key2, String value2) {
        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName())
        Map<String, String> metadata = [:] as Map<String, String>;
        if (key1 != null) {
            metadata.put(key1, value1);
        }
        if (key2 != null) {
            metadata.put(key2, value2);
        }


        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(10).setMaxConcurrency(10)
        def uploadOperation = fac.uploadWithResponse(Flux.just(getRandomData(10)),
            parallelTransferOptions, null, metadata, null)


        StepVerifier.create(uploadOperation.then(fac.getPropertiesWithResponse(null)))
            .assertNext({
        ;assert assertEquals(200, it.getStatusCode());
        assert it.getValue().getMetadata() == metadata;
            }).verifyComplete();
    }

    @LiveOnly
    @ParameterizedTest
    @MethodSource("uploadNumberOfAppendsSupplier")
    public void bufferedUploadOptions(int dataSize, Long singleUploadSize, Long blockSize, int numAppends) {
        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());
        AtomicInteger appendCount = new AtomicInteger(0);
        DataLakeFileAsyncClient spyClient = new DataLakeFileAsyncClient(fac) {
            @Override
            Mono<Response<Void>> appendWithResponse(Flux<ByteBuffer> data, long fileOffset, long length,
                DataLakeFileAppendOptions appendOptions, Context context) {
                appendCount.incrementAndGet();
                return super.appendWithResponse(data, fileOffset, length, appendOptions, context);
            }
        };

        StepVerifier.create(spyClient.uploadWithResponse(Flux.just(getRandomData(dataSize)),
            new ParallelTransferOptions().setBlockSizeLong(blockSize).setMaxSingleUploadSizeLong(singleUploadSize), null, null, null))
            .expectNextCount(1)
            .verifyComplete();

        StepVerifier.create(fac.getProperties())
            .assertNext(properties -> assertEquals(dataSize, properties.getFileSize()))
            .verifyComplete();

        assertEquals(numAppends, appendCount.get());
    }

    public void bufferedUploadPermissionsAndUmask() {

        def permissions = "0777"
        def umask = "0057"
        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName())


        def uploadOperation = fac.uploadWithResponse(new FileParallelUploadOptions(Flux.just(getRandomData(10))).setPermissions(permissions).setUmask(umask))


        StepVerifier.create(uploadOperation.then(fac.getPropertiesWithResponse(null)))
            .assertNext({
        ;assert assertEquals(200, it.getStatusCode());
        assert it.getValue().getFileSize() == 10;
            }).verifyComplete();
    }

    @Unroll
    @LiveOnly
    public void bufferedUploadAC() {

        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName())
        fac.create().block();

        match = setupPathMatchCondition(fac, match);
        leaseID = setupPathLeaseCondition(fac, leaseID);
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)


        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(10)
        StepVerifier.create(fac.uploadWithResponse(Flux.just(getRandomData(10)),
                parallelTransferOptions, null, null, requestConditions))
            .assertNext({ ;assert assertEquals(200, it.getStatusCode()); })
            .verifyComplete();

        where:
        modified | unmodified | match        | noneMatch   | leaseID
        null     | null       | null         | null        | null
        null     | newDate    | null         | null        | null
        oldDate  | null       | null         | null        | null
        null     | null       | receivedEtag | null        | null
        null     | null       | null         | garbageEtag | null
        null     | null       | null         | null        | receivedLeaseID
    }


    @LiveOnly
    @ParameterizedTest
    @MethodSource("invalidModifiedMatchAndLeaseIdSupplier")
    public void bufferedUploadACFail(OffsetDateTime modified, OffsetDateTime unmodified, String match, String noneMatch,
        String leaseID) {
        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName())
        fac.create().block();
        noneMatch = setupPathMatchCondition(fac, noneMatch);
        leaseID = setupPathLeaseCondition(fac, leaseID);
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions()
            .setLeaseId(leaseID)
            .setIfMatch(match)
            .setIfNoneMatch(noneMatch)
            .setIfModifiedSince(modified)
            .setIfUnmodifiedSince(unmodified)
        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(10)


        StepVerifier.create(fac.uploadWithResponse(Flux.just(getRandomData(10)),
                parallelTransferOptions, null, null, requestConditions))
            .verifyErrorSatisfies({
        ;assert it instanceof DataLakeStorageException
            def; storageException = (DataLakeStorageException) it;
        assert assertEquals(412, storageException.getStatusCode());
            })
    }

    // UploadBufferPool used to lock when the number of failed stageblocks exceeded the maximum number of buffers
    // (discovered when a leaseId was invalid)
    @Unroll
    @LiveOnly
    public void uploadBufferPoolLockThreeOrMoreBuffers() {

        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName())
        fac.create().block();
        def leaseID = setupPathLeaseCondition(fac, garbageLeaseID)
        DataLakeRequestConditions requestConditions = new DataLakeRequestConditions().setLeaseId(leaseID)


        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions().setBlockSizeLong(blockSize)
            .setMaxConcurrency(numBuffers)


        StepVerifier.create(fac.uploadWithResponse(Flux.just(getRandomData(10)),
                parallelTransferOptions, null, null, requestConditions))
            .verifyErrorSatisfies({ ;assert it instanceof DataLakeStorageException; })

        where:
        dataLength | blockSize | numBuffers
        16         | 7         | 2
        16         | 5         | 2
    }

//    /*def "Upload NRF progress"() {
//
//        def data = getRandomData(BlockBlobURL.MAX_UPLOAD_BLOB_BYTES + 1)
//        def numBlocks = data.remaining() / BlockBlobURL.MAX_STAGE_BLOCK_BYTES
//        long prevCount = 0
//        def mockReceiver = Mock(IProgressReceiver)
//
//
//
//        TransferManager.uploadFromNonReplayableFlowable(Flowable.just(data), bu, BlockBlobURL.MAX_STAGE_BLOCK_BYTES, 10,
//            new TransferManagerUploadToBlockBlobOptions(mockReceiver, null, null, null, 20)).blockingGet()
//        data.position(0)
//
//
//        // We should receive exactly one notification of the completed progress.
//        1 * mockReceiver.reportProgress(data.remaining()) */
//
//    /*
//    We should receive at least one notification reporting an intermediary value per block, but possibly more
//    notifications will be received depending on the implementation. We specify numBlocks - 1 because the last block
//    will be the total size as above. Finally, we assert that the number reported monotonically increases.
//     */
//    /*(numBlocks - 1.._) * mockReceiver.reportProgress(!data.remaining()) >> { long bytesTransferred ->
//        if (!(bytesTransferred > prevCount)) {
//            throw new IllegalArgumentException("Reported progress should monotonically increase")
//        } else {
//            prevCount = bytesTransferred
//        }
//    }
//
//    // We should receive no notifications that report more progress than the size of the file.
//    0 * mockReceiver.reportProgress({ it > data.remaining() })
//    notThrown(IllegalArgumentException)
//}*/
//

//    public void bufferedUploadNetworkError() {
//
//        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName())
//
//        /*
//         This test uses a Flowable that does not allow multiple subscriptions and therefore ensures that we are
//         buffering properly to allow for retries even given this source behavior.
//         */
//        fac.upload(Flux.just(defaultData), defaultDataSize, null, true).block()
//
//        // Mock a response that will always be retried.
//        def mockHttpResponse = getStubResponse(500, new HttpRequest(HttpMethod.PUT, new URL("https://www.fake.com")))
//
//        // Mock a policy that will always then check that the data is still the same and return a retryable error.
//        def mockPolicy = { HttpPipelineCallContext context, HttpPipelineNextPolicy next ->
//            return context.getHttpRequest().getBody() == null ? next.process() :
//                collectBytesInBuffer(context.getHttpRequest().getBody())
//                    .map({ it == defaultData })
//                    .flatMap({ it ? Mono.just(mockHttpResponse) : Mono.error(new IllegalArgumentException()) })
//            }
//
//        // Build the pipeline
//        DataLakeServiceClientBuilder fileAsyncClient = new DataLakeServiceClientBuilder()
//            .credential(primaryCredential)
//            .endpoint(String.format(defaultEndpointTemplate, primaryCredential.getAccountName()))
//            .httpClient(getHttpClient())
//            .retryOptions(new RequestRetryOptions(null, 3, null, 500, 1500, null))
//            .addPolicy(mockPolicy).buildAsyncClient()
//            .getFileSystemAsyncClient(fac.getFileSystemName())
//            .getFileAsyncClient(generatePathName())
//
//
//        // Try to upload the flowable, which will hit a retry. A normal upload would throw, but buffering prevents that.
//        ParallelTransferOptions parallelTransferOptions = new ParallelTransferOptions(1024, 4, null, null)
//        // TODO: It could be that duplicates aren't getting made in the retry policy? Or before the retry policy?
//
//
//        // A second subscription to a download stream will
//        StepVerifier.create(fileAsyncClient.upload(fac.read(), defaultDataSize, parallelTransferOptions))
//            .verifyErrorSatisfies({
//                assert it instanceof DataLakeStorageException
//                assert assertEquals(500, it.getStatusCode());
//            })
//    }

    @LiveOnly
    public void bufferedUploadDefaultNoOverwrite() {

        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName())
        fac.upload(data.defaultFlux, null).block();


        StepVerifier.create(fac.upload(data.defaultFlux, null))
            .verifyError(IllegalArgumentException);
    }

    @LiveOnly
    public void bufferedUploadOverwrite() {

        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName())


        def file = getRandomFile(50)
        fc.uploadFromFile(file.toPath().toString(), true);


        notThrown(BlobStorageException);

        and:
        def uploadVerifier = StepVerifier.create(fac.uploadFromFile(getRandomFile(50).toPath().toString(), true))


        uploadVerifier.verifyComplete();

        cleanup:
        file.delete();
    }

    public void bufferedUploadNonMarkableStream() {

        def file = getRandomFile(10)
        FileInputStream fileStream = new FileInputStream(file)
        def outFile = getRandomFile(10)


        fc.upload(fileStream, file.size(), true);


        fc.readToFile(outFile.toPath().toString(), true);
        compareFiles(file, outFile, 0, file.size());
    }

    public void uploadInputStreamNoLength() {

        fc.uploadWithResponse(new FileParallelUploadOptions(data.defaultInputStream), null, null);


        notThrown(Exception);
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        fc.read(os);
        os.toByteArray() == data.defaultBytes
    }

    public void uploadInputStreamBadLength() {

        fc.uploadWithResponse(new FileParallelUploadOptions(data.defaultInputStream, length), null, null);


        thrown(Exception);

        where:
        _ | length
        _ | 0
        _ | -100
        _ | DATA.getDefaultDataSizeLong() - 1
        _ | DATA.getDefaultDataSizeLong() + 1
    }

    public void uploadSuccessfulRetry() {

        def clientWithFailure = getFileClient(
            environment.dataLakeAccount.credential,
            fc.getFileUrl(),
            new TransientFailureInjectingHttpPipelinePolicy())


        clientWithFailure.uploadWithResponse(new FileParallelUploadOptions(data.defaultInputStream), null, null);


        notThrown(Exception);
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        fc.read(os);
        os.toByteArray() == data.defaultBytes
    }

    public void uploadBinaryData() {

        def client = getFileClient(
            environment.dataLakeAccount.credential,
            fc.getFileUrl())


        client.uploadWithResponse(new FileParallelUploadOptions(data.defaultBinaryData), null, null);


        notThrown(Exception);
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        fc.read(os);
        os.toByteArray() == data.defaultBytes
    }

    public void uploadBinaryDataOverwrite() {

        def client = getFileClient(
            environment.dataLakeAccount.credential,
            fc.getFileUrl())


        client.upload(data.defaultBinaryData, true);


        notThrown(Exception);
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        fc.read(os);
        os.toByteArray() == data.defaultBytes
    }

    @DisabledIf("olderThan20210410ServiceVersion")
    public void uploadEncryptionContext() {

        def encryptionContext = "encryptionContext"
        FileParallelUploadOptions options = new FileParallelUploadOptions(data.defaultInputStream).setEncryptionContext(encryptionContext)


        fc.uploadWithResponse(options, null, Context.NONE);
        def response = fc.getProperties()


        response.getEncryptionContext() == encryptionContext
    }

    /* Quick Query Tests. */

    // Generates and uploads a CSV file
    def uploadCsv(FileQueryDelimitedSerialization s, int numCopies) {
        String header = String.join(new String(s.getColumnSeparator()), "rn1", "rn2", "rn3", "rn4")
            .concat(new String(s.getRecordSeparator()))
        byte[] headers = header.getBytes()

        String csv = String.join(new String(s.getColumnSeparator()), "100", "200", "300", "400")
            .concat(new String(s.getRecordSeparator()))
            .concat(String.join(new String(s.getColumnSeparator()), "300", "400", "500", "600")
                .concat(new String(s.getRecordSeparator())))

        byte[] csvData = csv.getBytes()

        int headerLength = s.isHeadersPresent() ? headers.length : 0
        byte[] data = new byte[headerLength + csvData.length * numCopies]
        if (s.isHeadersPresent()) {
            System.arraycopy(headers, 0, data, 0, headers.length);
        }

        for (int i = 0; i < numCopies; i++) {
            int o = i * csvData.length + headerLength
            System.arraycopy(csvData, 0, data, o, csvData.length);
        }

        InputStream inputStream = new ByteArrayInputStream(data)

        fc.create(true);
        fc.append(inputStream, 0, data.length);
        fc.flush(data.length, true);
    }

    def uploadSmallJson(int numCopies) {
        StringBuilder b = new StringBuilder()
        b.append('{\n');
        for(int i = 0; i < numCopies; i++) {
            b.append(String.format('\t"name%d": "owner%d",\n', i, i));
        }
        b.append('}');

        InputStream inputStream = new ByteArrayInputStream(b.toString().getBytes())

        fc.create(true);
        fc.append(inputStream, 0, b.length());
        fc.flush(b.length(), true);
    }

    byte[] readFromInputStream(InputStream stream, int numBytesToRead) {
        byte[] queryData = new byte[numBytesToRead]

        def totalRead = 0
        def bytesRead = 0
        def length = numBytesToRead

        while (bytesRead != -1 && totalRead < numBytesToRead) {
            bytesRead = stream.read(queryData, totalRead, length);
            if (bytesRead != -1) {
                totalRead += bytesRead;
                length -= bytesRead;
            }
        }

        stream.close();
        return queryData;
    }

    @DisabledIf("olderThan20191212ServiceVersion")
    @Unroll
    @Retry(count = 5, delay = 5, condition = { environment.testMode == TestMode.LIVE })
    public void queryMin() {

        FileQueryDelimitedSerialization ser = new FileQueryDelimitedSerialization()
            .setRecordSeparator('\n' as char)
            .setColumnSeparator(',' as ;char)
            .setEscapeChar('\0' as ;char)
            .setFieldQuote('\0' as ;char)
            .setHeadersPresent(false);
        uploadCsv(ser, numCopies);
        def expression = "SELECT * from BlobStorage"

        ByteArrayOutputStream downloadData = new ByteArrayOutputStream()
        fc.read(downloadData);
        byte[] downloadedData = downloadData.toByteArray()

        /* Input Stream. */

        InputStream qqStream = fc.openQueryInputStream(expression)
        byte[] queryData = readFromInputStream(qqStream, downloadedData.length)


        notThrown(IOException);
        queryData == downloadedData

        /* Output Stream. */

        OutputStream os = new ByteArrayOutputStream()
        fc.query(os, expression);
        byte[] osData = os.toByteArray()


        notThrown(BlobStorageException);
        osData == downloadedData

        // To calculate the size of data being tested = numCopies * 32 bytes
        where:
        numCopies | _
        1         | _ // 32 bytes
        32        | _ // 1 KB
        256       | _ // 8 KB
        400       | _ // 12 ish KB
        4000      | _ // 125 KB
    }

    @DisabledIf("olderThan20191212ServiceVersion")
    @Unroll
    @Retry(count = 5, delay = 5, condition = { environment.testMode == TestMode.LIVE })
    public void queryCsvSerializationSeparator() {

        FileQueryDelimitedSerialization serIn = new FileQueryDelimitedSerialization()
            .setRecordSeparator(recordSeparator as char)
            .setColumnSeparator(columnSeparator as ;char)
            .setEscapeChar('\0' as ;char)
            .setFieldQuote('\0' as ;char)
            .setHeadersPresent(headersPresentIn);
        FileQueryDelimitedSerialization serOut = new FileQueryDelimitedSerialization()
            .setRecordSeparator(recordSeparator as char)
            .setColumnSeparator(columnSeparator as ;char)
            .setEscapeChar('\0' as ;char)
            .setFieldQuote('\0' as ;char)
            .setHeadersPresent(headersPresentOut);
        uploadCsv(serIn, 32);
        def expression = "SELECT * from BlobStorage"

        ByteArrayOutputStream downloadData = new ByteArrayOutputStream()
        fc.read(downloadData);
        byte[] downloadedData = downloadData.toByteArray()

        /* Input Stream. */

        InputStream qqStream = fc.openQueryInputStreamWithResponse(new FileQueryOptions(expression).setInputSerialization(serIn).setOutputSerialization(serOut)).getValue()
        byte[] queryData = readFromInputStream(qqStream, downloadedData.length)


        notThrown(IOException);
        if (headersPresentIn && !headersPresentOut) {
            /* Account for 16 bytes of header. */
            for (int j = 16; j < downloadedData.length; j++) {
                assert queryData[j - 16] == downloadedData[j];
            }
            for (int k = downloadedData.length - 16; k < downloadedData.length; k++) {
                assert queryData[k] == 0;
            }
        } else {
            queryData == downloadedData
        }

        /* Output Stream. */

        OutputStream os = new ByteArrayOutputStream()
        fc.queryWithResponse(new FileQueryOptions(expression, os)
            .setInputSerialization(serIn).setOutputSerialization(serOut), null, null);
        byte[] osData = os.toByteArray()


        notThrown(DataLakeStorageException);
        if (headersPresentIn && !headersPresentOut) {
            assert osData.length == downloadedData.length - 16;
            /* Account for 16 bytes of header. */
            for (int j = 16; j < downloadedData.length; j++) {
                assert osData[j - 16] == downloadedData[j];
            }
        } else {
            osData == downloadedData
        }

        where:
        recordSeparator | columnSeparator | headersPresentIn | headersPresentOut || _
        '\n'            | ','             | false            | false             || _ /* Default. */
        '\n'            | ','             | true             | true             || _ /* Headers. */
        '\n'            | ','             | true             | false             || _ /* Headers. */
        '\t'            | ','             | false            | false             || _ /* Record separator. */
        '\r'            | ','             | false            | false             || _
        '<'             | ','             | false            | false             || _
        '>'             | ','             | false            | false             || _
        '&'             | ','             | false            | false             || _
        '\\'            | ','             | false            | false             || _
        ','             | '.'             | false            | false             || _ /* Column separator. */
//        ','             | '\n'            | false          | false               || _ /* Keep getting a qq error: Field delimiter and record delimiter must be different characters. */
        ','             | ';'             | false            | false             || _
        '\n'            | '\t'            | false            | false             || _
//        '\n'            | '\r'            | false          | false               || _ /* Keep getting a qq error: Field delimiter and record delimiter must be different characters. */
        '\n'            | '<'             | false            | false             || _
        '\n'            | '>'             | false            | false             || _
        '\n'            | '&'             | false            | false             || _
        '\n'            | '\\'            | false            | false             || _
    }

    @DisabledIf("olderThan20191212ServiceVersion")
    @Unroll
    @Retry(count = 5, delay = 5, condition = { environment.testMode == TestMode.LIVE })
    public void queryCsvSerializationEscapeAndFieldQuote() {

        FileQueryDelimitedSerialization ser = new FileQueryDelimitedSerialization()
            .setRecordSeparator('\n' as char)
            .setColumnSeparator(',' as ;char)
            .setEscapeChar('\\' as ;char) /* Escape set here. */
            .setFieldQuote('"' as ;char)  /* Field quote set here*/
            .setHeadersPresent(false);
        uploadCsv(ser, 32);

        def expression = "SELECT * from BlobStorage"

        ByteArrayOutputStream downloadData = new ByteArrayOutputStream()
        fc.read(downloadData);
        byte[] downloadedData = downloadData.toByteArray()

        /* Input Stream. */

        InputStream qqStream = fc.openQueryInputStreamWithResponse(new FileQueryOptions(expression).setInputSerialization(ser).setOutputSerialization(ser)).getValue()
        byte[] queryData = readFromInputStream(qqStream, downloadedData.length)


        notThrown(IOException);
        queryData == downloadedData


        /* Output Stream. */

        OutputStream os = new ByteArrayOutputStream()
        fc.queryWithResponse(new FileQueryOptions(expression, os)
            .setInputSerialization(ser).setOutputSerialization(ser), null, null);
        byte[] osData = os.toByteArray()


        notThrown(DataLakeStorageException);
        osData == downloadedData
    }

    /* Note: Input delimited tested everywhere else. */
    @DisabledIf("olderThan20191212ServiceVersion")
    @Unroll
    @Retry(count = 5, delay = 5, condition = { environment.testMode == TestMode.LIVE })
    public void queryInputJson() {

        FileQueryJsonSerialization ser = new FileQueryJsonSerialization()
            .setRecordSeparator(recordSeparator as char)
        uploadSmallJson(numCopies);
        def expression = "SELECT * from BlobStorage"

        ByteArrayOutputStream downloadData = new ByteArrayOutputStream()
        fc.read(downloadData);
        downloadData.write(10); /* writing extra new line */
        byte[] downloadedData = downloadData.toByteArray()
        FileQueryOptions optionsIs = new FileQueryOptions(expression).setInputSerialization(ser).setOutputSerialization(ser)
        OutputStream os = new ByteArrayOutputStream()
        FileQueryOptions optionsOs = new FileQueryOptions(expression, os).setInputSerialization(ser).setOutputSerialization(ser)

        /* Input Stream. */

        InputStream qqStream = fc.openQueryInputStreamWithResponse(optionsIs).getValue()
        byte[] queryData = readFromInputStream(qqStream, downloadedData.length)


        notThrown(IOException);
        queryData == downloadedData

        /* Output Stream. */

        fc.queryWithResponse(optionsOs, null, null);
        byte[] osData = os.toByteArray()


        notThrown(DataLakeStorageException);
        osData == downloadedData

        where:
        numCopies | recordSeparator || _
        0         | '\n'            || _
        10        | '\n'            || _
        100       | '\n'            || _
        1000      | '\n'            || _
    }

    @DisabledIf("olderThan20201002ServiceVersion")
    @Retry(count = 5, delay = 5, condition = { environment.testMode == TestMode.LIVE })
    public void queryInputParquet() {

        String fileName = "parquet.parquet"
        ClassLoader classLoader = getClass().getClassLoader()
        File f = new File(classLoader.getResource(fileName).getFile())
        FileQueryParquetSerialization ser = new FileQueryParquetSerialization()
        fc.uploadFromFile(f.getAbsolutePath(), true);
        byte[] expectedData = "0,mdifjt55.ea3,mdifjt55.ea3\n".getBytes()

        def expression = "select * from blobstorage where id < 1;"

        FileQueryOptions optionsIs = new FileQueryOptions(expression).setInputSerialization(ser)
        OutputStream os = new ByteArrayOutputStream()
        FileQueryOptions optionsOs = new FileQueryOptions(expression, os).setInputSerialization(ser)

        /* Input Stream. */

        InputStream qqStream = fc.openQueryInputStreamWithResponse(optionsIs).getValue()
        byte[] queryData = readFromInputStream(qqStream, expectedData.length)


        notThrown(IOException);
        queryData == expectedData

        /* Output Stream. */

        fc.queryWithResponse(optionsOs, null, null);
        byte[] osData = os.toByteArray()


        notThrown(BlobStorageException);
        osData == expectedData
    }

    @DisabledIf("olderThan20191212ServiceVersion")
    @Retry(count = 5, delay = 5, condition = { environment.testMode == TestMode.LIVE })
    public void queryInputCsvOutputJson() {

        FileQueryDelimitedSerialization inSer = new FileQueryDelimitedSerialization()
            .setRecordSeparator('\n' as char)
            .setColumnSeparator(',' as ;char)
            .setEscapeChar('\0' as ;char)
            .setFieldQuote('\0' as ;char)
            .setHeadersPresent(false);
        uploadCsv(inSer, 1);
        FileQueryJsonSerialization outSer = new FileQueryJsonSerialization()
            .setRecordSeparator('\n' as char)
        def expression = "SELECT * from BlobStorage"
        byte[] expectedData = "{\"_1\":\"100\",\"_2\":\"200\",\"_3\":\"300\",\"_4\":\"400\"}".getBytes()
        FileQueryOptions optionsIs = new FileQueryOptions(expression).setInputSerialization(inSer).setOutputSerialization(outSer)
        OutputStream os = new ByteArrayOutputStream()
        FileQueryOptions optionsOs = new FileQueryOptions(expression, os).setInputSerialization(inSer).setOutputSerialization(outSer)

        /* Input Stream. */

        InputStream qqStream = fc.openQueryInputStreamWithResponse(optionsIs).getValue()
        byte[] queryData = readFromInputStream(qqStream, expectedData.length)


        notThrown(IOException);
        for (int j = 0; j < expectedData.length; j++) {
            assert queryData[j] == expectedData[j];
        }

        /* Output Stream. */

        fc.queryWithResponse(optionsOs, null, null);
        byte[] osData = os.toByteArray()


        notThrown(BlobStorageException);
        for (int j = 0; j < expectedData.length; j++) {
            assert osData[j] == expectedData[j];
        }
    }

    @DisabledIf("olderThan20191212ServiceVersion")
    @Retry(count = 5, delay = 5, condition = { environment.testMode == TestMode.LIVE })
    public void queryInputJsonOutputCsv() {

        FileQueryJsonSerialization inSer = new FileQueryJsonSerialization()
            .setRecordSeparator('\n' as char)
        uploadSmallJson(2);
        FileQueryDelimitedSerialization outSer = new FileQueryDelimitedSerialization()
            .setRecordSeparator('\n' as char)
            .setColumnSeparator(',' as ;char)
            .setEscapeChar('\0' as ;char)
            .setFieldQuote('\0' as ;char)
            .setHeadersPresent(false);
        def expression = "SELECT * from BlobStorage"
        byte[] expectedData = "owner0,owner1\n".getBytes()
        FileQueryOptions optionsIs = new FileQueryOptions(expression).setInputSerialization(inSer).setOutputSerialization(outSer)
        OutputStream os = new ByteArrayOutputStream()
        FileQueryOptions optionsOs = new FileQueryOptions(expression, os).setInputSerialization(inSer).setOutputSerialization(outSer)

        /* Input Stream. */

        InputStream qqStream = fc.openQueryInputStreamWithResponse(optionsIs).getValue()
        byte[] queryData = readFromInputStream(qqStream, expectedData.length)


        notThrown(IOException);
        for (int j = 0; j < expectedData.length; j++) {
            assert queryData[j] == expectedData[j];
        }

        /* Output Stream. */

        fc.queryWithResponse(optionsOs, null, null);
        byte[] osData = os.toByteArray()


        notThrown(DataLakeStorageException);
        for (int j = 0; j < expectedData.length; j++) {
            assert osData[j] == expectedData[j];
        }
    }

    @DisabledIf("olderThan20191212ServiceVersion")
    @Retry(count = 5, delay = 5, condition = { environment.testMode == TestMode.LIVE })
    public void queryInputCsvOutputArrow() {

        FileQueryDelimitedSerialization inSer = new FileQueryDelimitedSerialization()
            .setRecordSeparator('\n' as char)
            .setColumnSeparator(',' as ;char)
            .setEscapeChar('\0' as ;char)
            .setFieldQuote('\0' as ;char)
            .setHeadersPresent(false);
        uploadCsv(inSer, 32);
        List<FileQueryArrowField> schema = new ArrayList<>()
        schema.add(new FileQueryArrowField(FileQueryArrowFieldType.DECIMAL).setName("Name").setPrecision(4).setScale(2));
        FileQueryArrowSerialization outSer = new FileQueryArrowSerialization().setSchema(schema)
        def expression = "SELECT _2 from BlobStorage WHERE _1 > 250;"
        OutputStream os = new ByteArrayOutputStream()
        FileQueryOptions options = new FileQueryOptions(expression, os).setOutputSerialization(outSer)

        /* Input Stream. */

        fc.openQueryInputStreamWithResponse(options).getValue();


        notThrown(IOException);

        /* Output Stream. */

        fc.queryWithResponse(options, null, null);


        notThrown(BlobStorageException);
    }

    @DisabledIf("olderThan20191212ServiceVersion")
    @Retry(count = 5, delay = 5, condition = { environment.testMode == TestMode.LIVE })
    public void queryNonFatalError() {

        FileQueryDelimitedSerialization base = new FileQueryDelimitedSerialization()
            .setRecordSeparator('\n' as char)
            .setEscapeChar('\0' as ;char)
            .setFieldQuote('\0' as ;char)
            .setHeadersPresent(false);
        uploadCsv(base.setColumnSeparator('.' as ;char), 32)
        MockErrorReceiver receiver =
            new com.azure.storage.file.datalake.FileApiTest.MockErrorReceiver("InvalidColumnOrdinal")
        def expression = "SELECT _1 from BlobStorage WHERE _2 > 250"
        FileQueryOptions options = new FileQueryOptions(expression)
            .setInputSerialization(base.setColumnSeparator(',' as char))
            .setOutputSerialization(base.setColumnSeparator(',' as ;char))
            .setErrorConsumer(receiver);

        /* Input Stream. */

        InputStream qqStream = fc.openQueryInputStreamWithResponse(options).getValue()
        readFromInputStream(qqStream, Constants.KB);


        receiver.numErrors > 0
        notThrown(IOException);

        /* Output Stream. */

        receiver = new com.azure.storage.file.datalake.FileApiTest.MockErrorReceiver("InvalidColumnOrdinal");
        options = new FileQueryOptions(expression, new ByteArrayOutputStream())
            .setInputSerialization(base.setColumnSeparator(',' as ;char))
            .setOutputSerialization(base.setColumnSeparator(',' as ;char))
            .setErrorConsumer(receiver);
        fc.queryWithResponse(options, null, null);


        notThrown(IOException);
        receiver.numErrors > 0
    }

    @DisabledIf("olderThan20191212ServiceVersion")
    @Retry(count = 5, delay = 5, condition = { environment.testMode == TestMode.LIVE })
    public void queryFatalError() {

        FileQueryDelimitedSerialization base = new FileQueryDelimitedSerialization()
            .setRecordSeparator('\n' as char)
            .setEscapeChar('\0' as ;char)
            .setFieldQuote('\0' as ;char)
            .setHeadersPresent(true);
        uploadCsv(base.setColumnSeparator('.' as ;char), 32)
        def expression = "SELECT * from BlobStorage"
        FileQueryOptions options = new FileQueryOptions(expression)
            .setInputSerialization(new FileQueryJsonSerialization())

        /* Input Stream. */

        InputStream qqStream = fc.openQueryInputStreamWithResponse(options).getValue()
        readFromInputStream(qqStream, Constants.KB);


        thrown(IOException);

        /* Output Stream. */

        options = new FileQueryOptions(expression, new ByteArrayOutputStream())
            .setInputSerialization(new FileQueryJsonSerialization());
        fc.queryWithResponse(options, null, null);


        thrown(Exceptions.ReactiveException);
    }

    @DisabledIf("olderThan20191212ServiceVersion")
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

    @DisabledIf("olderThan20191212ServiceVersion")
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

    @DisabledIf("olderThan20191212ServiceVersion")
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

    @DisabledIf("olderThan20191212ServiceVersion")
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

    private static boolean olderThan20201002ServiceVersion() {
        return olderThan(DataLakeServiceVersion.V2020_10_02);
    }

    @DisabledIf("olderThan20201002ServiceVersion")
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

    @DisabledIf("olderThan20191212ServiceVersion")
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

    @DisabledIf("olderThan20191212ServiceVersion")
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
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @DisabledIf("olderThan20191212ServiceVersion")
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

    @DisabledIf("olderThan20191212ServiceVersion")
    @ParameterizedTest
    @MethodSource("scheduleDeletionSupplier")
    public void scheduleDeletion(FileScheduleDeletionOptions fileScheduleDeletionOptions, boolean hasExpiry) {
        DataLakeFileClient fileClient = dataLakeFileSystemClient.getFileClient(generatePathName());
        fileClient.create();;

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

    private static boolean olderThan20191212ServiceVersion() {
        return olderThan(DataLakeServiceVersion.V2019_12_12);
    }

    @DisabledIf("olderThan20191212ServiceVersion")
    public void scheduleDeletionTime() {
        OffsetDateTime now = testResourceNamer.now();
        FileScheduleDeletionOptions fileScheduleDeletionOptions = new FileScheduleDeletionOptions(now.plusDays(1));
        DataLakeFileClient fileClient = dataLakeFileSystemClient.getFileClient(generatePathName());
        fileClient.create();;

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

    // Tests an issue found where buffered upload would not deep copy buffers while determining what upload path to take.
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

    @LiveOnly /* Flaky in playback. */
    @ParameterizedTest
    @MethodSource("uploadNumberOfAppendsSupplier")
    public void uploadNumAppends(int dataSize, Long singleUploadSize, Long blockSize, int numAppends) {
        DataLakeFileAsyncClient fac = dataLakeFileSystemAsyncClient.getFileAsyncClient(generatePathName());
        AtomicInteger numAppendsCounter = new AtomicInteger(0);
        DataLakeFileAsyncClient spyClient = new DataLakeFileAsyncClient(fac) {
            @Override
            Mono<Response<Void>> appendWithResponse(Flux<ByteBuffer> data, long fileOffset, long length,
                DataLakeFileAppendOptions appendOptions, Context context) {
                numAppendsCounter.incrementAndGet();
                return super.appendWithResponse(data, fileOffset, length, appendOptions, context);
            }
        };
        ByteArrayInputStream input = new ByteArrayInputStream(getRandomByteArray(dataSize));

        ParallelTransferOptions pto = new ParallelTransferOptions().setBlockSizeLong(blockSize)
            .setMaxSingleUploadSizeLong(singleUploadSize);

        StepVerifier.create(spyClient.uploadWithResponse(new FileParallelUploadOptions(input, dataSize)
                .setParallelTransferOptions(pto)))
            .expectNextCount(1)
            .verifyComplete();

        StepVerifier.create(fac.getProperties())
            .assertNext(properties -> assertEquals(dataSize, properties.getFileSize()))
            .verifyComplete();
        assertEquals(numAppends, numAppendsCounter.get());
    }

    private static Stream<Arguments> uploadNumberOfAppendsSupplier() {
        return Stream.of(
            // dataSize | singleUploadSize | blockSize | numAppends
            Arguments.of((100 * Constants.MB) - 1, null, null, 1),
            Arguments.of((100 * Constants.MB) + 1, null, null, Math.ceil(((double) (100 * Constants.MB) + 1) / (double) (4 * Constants.MB))),
            Arguments.of(100, 50L, null, 1),
            Arguments.of(100, 50L, 20L, 5)
        );
    }


    @Test
    public void uploadReturnValue() {
        assertNotNull(fc.uploadWithResponse(
            new FileParallelUploadOptions(DATA.getDefaultInputStream(), DATA.getDefaultDataSizeLong()), null, null)
            .getValue().getETag());
    }

    // Reading from recordings will not allow for the timing of the test to work correctly.
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
        DataLakeFileClient fileClient = getFileClient(getDataLakeCredential(), fc.getFileUrl(), getPerCallVersionPolicy());

        // blob endpoint
        assertEquals("2019-02-02", fileClient.getPropertiesWithResponse(null, null, null).getHeaders()
            .getValue("x-ms-version"));

        // dfs endpoint
        assertEquals("2019-12-12", fileClient.getAccessControlWithResponse(false, null, null, null).getHeaders()
            .getValue("x-ms-version"));
    }
}
